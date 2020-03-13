package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import info.lliira.illyriad.schedule.town.Resource;
import info.lliira.illyriad.schedule.town.Town;
import info.lliira.illyriad.schedule.town.TownEntity;
import info.lliira.illyriad.schedule.town.TownLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class BuildingScheduler extends Scheduler {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var scheduler = new BuildingScheduler(authenticator);
    scheduler.run();
  }

  private static final double FOOD_RATIO = 0.2;
  private static final double STORAGE_RATIO = 0.8;
  private static final String PENDING_BUILDING_FILE_PREFIX = "building.pending.";

  private static final Map<Resource.Type, Double> BUILDING_RESOURCE_TYPES = new HashMap<>();

  static {
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Wood, 1.0);
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Clay, 1.0);
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Iron, 1.0);
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Stone, 1.0);
  }

  private static final Logger LOG = LogManager.getLogger(BuildingScheduler.class.getSimpleName());

  private final TownLoader townLoader;
  private final BuildingLoader buildingLoader;
  private final BuildingUpgrader buildingUpgrader;

  public BuildingScheduler(Authenticator authenticator) {
    super(BuildingScheduler.class.getSimpleName());
    this.townLoader = new TownLoader(authenticator);
    this.buildingLoader = new BuildingLoader(authenticator);
    this.buildingUpgrader = new BuildingUpgrader(authenticator);
  }

  @Override
  public long schedule() {
    LOG.info("=============== Scheduling Constructions ===============");
    var town = townLoader.loadTown();
    long minTime = Long.MAX_VALUE;
    for (var entity : town.towns.values()) {
      long wait = schedule(entity);
      minTime = Math.min(minTime, wait);
    }
    return minTime;
  }

  private long schedule(TownEntity entity) {
    var town = townLoader.changeTown(entity);
    LOG.info("***** {} *****", town);
    LOG.info("{} constructions in progress", town.progress.constructionCount());

    Duration waitDuration;
    if (town.progress.construction1.isEmpty()) {
      var pendingBuildings = loadingPendingBuildings(entity);
      var buildingOptional = findNextToUpgrade(town, pendingBuildings);
      assert buildingOptional.isPresent();
      var building = buildingOptional.get();
      waitDuration = waitTillEnoughResources(town, building);
      if (waitDuration.getSeconds() == 0) {
        LOG.info("Constructing Next building: {} -> level {}", building.name, building.nextLevel);
        buildingUpgrader.upgrade(building);
        savePendingBuildings(entity, building, pendingBuildings);
        // Add 5 sec buffer as the actual time is usually +5 than the estimated time.
        waitDuration = building.time.plusSeconds(5);
      } else {
        LOG.info(
            "Lack resources for Next building: {} -> level {}", building.name, building.nextLevel);
      }
    } else {
      waitDuration =
          Duration.ofMillis(
              Math.max(1, town.progress.constructionTimestamp() - System.currentTimeMillis()));
    }
    LOG.info(
        String.format(
            "Next construction will start in %02d:%02d:%02d",
            waitDuration.toHours(), waitDuration.toMinutesPart(), waitDuration.toSecondsPart()));
    return waitDuration.toMillis();
  }

  private LinkedList<Building.Type> loadingPendingBuildings(TownEntity entity) {
    var buildings = new LinkedList<Building.Type>();
    var pendingFile = new File(PENDING_BUILDING_FILE_PREFIX + entity.id);
    if (pendingFile.exists() && pendingFile.isFile()) {
      try {
        List<String> lines = Files.readAllLines(pendingFile.toPath());
        lines.stream()
            .filter(line -> !line.isBlank())
            .map(Building.Type::parse)
            .forEach(buildings::add);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    LOG.info("Pending buildings: {}", buildings);
    return buildings;
  }

  private void savePendingBuildings(
      TownEntity entity, Building updated, LinkedList<Building.Type> pendingBuildings) {
    if (updated.type == pendingBuildings.getFirst()) {
      // the first pending build was scheduled, remove it from the list and save back
      pendingBuildings.pollFirst();
      var pendingFile = new File(PENDING_BUILDING_FILE_PREFIX + entity.id);
      try (var writer = new PrintWriter(pendingFile)) {
        pendingBuildings.stream().map(Building.Type::name).forEach(writer::println);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Optional<Building> findNextToUpgrade(
      Town town, LinkedList<Building.Type> pendingBuildings) {
    var townBuildings = buildingLoader.loadTownBuildings();
    if (!pendingBuildings.isEmpty()) {
      var pendingBuilding = townBuildings.get(pendingBuildings.getFirst());
      if (pendingBuilding != null) return Optional.of(pendingBuilding);
    }

    var landBuildings = buildingLoader.loadMinLandBuildings();
    long toTimestamp = town.progress.constructionTimestamp();

    // check if the storage is near full, and we need to build more storage
    if (shouldScheduleStorage(town, toTimestamp)) {
      var storageBuilding = findStorageBuilding(townBuildings);
      if (storageBuilding.isPresent()) return storageBuilding;
    }

    // check if we need to produce food
    if (shouldScheduleFood(town, toTimestamp))
      return Optional.ofNullable(landBuildings.get(Resource.Type.Food));

    // choose the lowest resource type at the timestamp
    Resource minResource = null;
    long minAmount = 0;
    for (var entry : BUILDING_RESOURCE_TYPES.entrySet()) {
      Resource resource = town.resources.get(entry.getKey());
      long amount = Math.round(resource.till(toTimestamp) / entry.getValue());
      if (minResource == null || minAmount > amount) {
        minResource = resource;
        minAmount = amount;
      }
    }
    return Optional.ofNullable(landBuildings.get(minResource.type));
  }

  private boolean shouldScheduleFood(Town town, long toTimestamp) {
    var food = town.resources.get(Resource.Type.Food);
    // keep producing food
    if (food.rate <= 0) return true;

    // compare the food with the average of the other resources
    long sum = 0;
    for (Resource.Type type : BUILDING_RESOURCE_TYPES.keySet()) {
      sum += town.resources.get(type).till(toTimestamp);
    }
    return FOOD_RATIO * sum / BUILDING_RESOURCE_TYPES.size() > food.till(toTimestamp);
  }

  private Duration waitTillEnoughResources(Town town, Building building) {
    long max = waitSeconds(building.woodCost, town.resources.get(Resource.Type.Wood));
    max = Math.max(max, waitSeconds(building.clayCost, town.resources.get(Resource.Type.Clay)));
    max = Math.max(max, waitSeconds(building.ironCost, town.resources.get(Resource.Type.Iron)));
    max = Math.max(max, waitSeconds(building.stoneCost, town.resources.get(Resource.Type.Stone)));
    return Duration.ofSeconds(max);
  }

  private long waitSeconds(int cost, Resource resource) {
    int diff = cost - resource.current();
    return Math.max(0, Math.round(Math.ceil(1.0 * diff / resource.rate * 3_600)));
  }

  private boolean shouldScheduleStorage(Town town, long toTimeStamp) {
    var townInfo = townLoader.loadTownInfo();
    LOG.info("Town info: {}", townInfo);
    int max = 0;
    for (var resource : town.resources.values()) {
      max = Math.max(max, resource.till(toTimeStamp));
    }
    return 1.0 * max / townInfo.capacity > STORAGE_RATIO;
  }

  private Optional<Building> findStorageBuilding(Map<Building.Type, Building> townBuildings) {
    var storehouse = townBuildings.get(Building.Type.Storehouse);
    var warehouse = townBuildings.get(Building.Type.Warehouse);
    if (storehouse != null && warehouse != null) {
      return Optional.of(storehouse.nextLevel > warehouse.nextLevel ? warehouse : storehouse);
    } else return Optional.ofNullable(storehouse);
  }
}
