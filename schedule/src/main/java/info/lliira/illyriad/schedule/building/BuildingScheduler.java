package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import info.lliira.illyriad.schedule.town.Resource;
import info.lliira.illyriad.schedule.town.Town;
import info.lliira.illyriad.schedule.town.TownEntity;
import info.lliira.illyriad.schedule.town.TownInfo;
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
import java.util.Random;
import java.util.stream.Collectors;

public class BuildingScheduler extends Scheduler {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var scheduler = new BuildingScheduler(authenticator, properties);
    scheduler.run();
  }

  private static final String FOOD_RATIO_FIELD = "schedule.food.ratio";
  private static final String STORAGE_RATIO_FIELD = "schedule.storage.ratio";
  private static final String MAX_BUILDING_LEVEL_FIELD = "schedule.max.building.level";
  private static final double DEFAULT_FOOD_RATIO = 0.2;
  private static final double DEFAULT_STORAGE_RATIO = 0.8;
  private static final int MAX_BUILDING_LEVEL = 20;
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
  private final double foodRatio;
  private final double storageRatio;
  private final int maxBuildingLevel;
  private final Random random;

  public BuildingScheduler(Authenticator authenticator, Properties properties) {
    super(BuildingScheduler.class.getSimpleName());
    this.townLoader = new TownLoader(authenticator);
    this.buildingLoader = new BuildingLoader(authenticator);
    this.buildingUpgrader = new BuildingUpgrader(authenticator);
    this.foodRatio =
        Double.parseDouble(
            properties.getProperty(FOOD_RATIO_FIELD, Double.toString(DEFAULT_FOOD_RATIO)));
    this.storageRatio =
        Double.parseDouble(
            properties.getProperty(STORAGE_RATIO_FIELD, Double.toString(DEFAULT_STORAGE_RATIO)));
    this.maxBuildingLevel =
        Integer.parseInt(
            properties.getProperty(MAX_BUILDING_LEVEL_FIELD, Integer.toString(MAX_BUILDING_LEVEL)));
    this.random = new Random();
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
      var townInfo = townLoader.loadTownInfo();
      LOG.info("Town info: {}", townInfo);
      var pendingBuildings = loadingPendingBuildings(entity);
      var buildingOptional = findNextToUpgrade(town, townInfo, pendingBuildings);
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
    if (!pendingBuildings.isEmpty() && updated.type == pendingBuildings.getFirst()) {
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
      Town town, TownInfo townInfo, LinkedList<Building.Type> pendingBuildings) {
    var townBuildings = buildingLoader.loadTownBuildings();
    if (!pendingBuildings.isEmpty()) {
      var pendingBuilding = townBuildings.get(pendingBuildings.getFirst());
      if (pendingBuilding != null) return Optional.of(pendingBuilding);
    }

    var landBuildings = buildingLoader.loadMinLandBuildings();

    // check if the storage is near full, and we need to build more storage
    if (shouldScheduleStorage(town, townInfo)) {
      var storageBuilding = findStorageBuilding(townBuildings);
      if (storageBuilding.isPresent()) return storageBuilding;
    }

    // check if we need to produce food
    if (shouldScheduleFood(town)) {
      var foodBuilding = landBuildings.get(Resource.Type.Food);
      if (foodBuilding != null && foodBuilding.nextLevel <= maxBuildingLevel)
        return Optional.of(foodBuilding);
    }

    // choose the resource that takes the longest to fill the storage
    Resource.Type minType = Resource.Type.Wood;
    long longestTime = 0;
    for (var entry : BUILDING_RESOURCE_TYPES.entrySet()) {
      Resource resource = town.resources.get(entry.getKey());
      long timeToFull = 3600L * (townInfo.capacity - resource.current()) / resource.rate;
      if (timeToFull > longestTime) {
        minType = resource.type;
        longestTime = timeToFull;
      }
    }
    var landBuilding = landBuildings.get(minType);
    if (landBuilding != null && landBuilding.nextLevel <= maxBuildingLevel)
      return Optional.of(landBuilding);

    // no land building available, pick a random town building;
    var candidates =
        townBuildings.values().stream()
            .filter(building -> building.nextLevel <= maxBuildingLevel)
            .collect(Collectors.toList());
    return candidates.isEmpty()
        ? Optional.empty()
        : Optional.of(candidates.get(random.nextInt(candidates.size())));
  }

  private boolean shouldScheduleFood(Town town) {
    var food = town.resources.get(Resource.Type.Food);
    // keep producing food
    if (food.rate <= 0) return true;

    // compare the food with the average of the other resources
    long sum = 0;
    for (Resource.Type type : BUILDING_RESOURCE_TYPES.keySet()) {
      sum += town.resources.get(type).current();
    }
    return foodRatio * sum / BUILDING_RESOURCE_TYPES.size() > food.current();
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

  private boolean shouldScheduleStorage(Town town, TownInfo townInfo) {
    int max = 0;
    for (var resource : town.resources.values()) {
      // skip Gold as it doesn't use storage
      if (resource.type == Resource.Type.Gold) continue;
      max = Math.max(max, resource.current());
    }
    return 1.0 * max / townInfo.capacity > storageRatio;
  }

  private Optional<Building> findStorageBuilding(Map<Building.Type, Building> townBuildings) {
    var storehouse = townBuildings.get(Building.Type.Storehouse);
    var warehouse = townBuildings.get(Building.Type.Warehouse);
    if (storehouse != null && warehouse != null) {
      return Optional.of(storehouse.nextLevel > warehouse.nextLevel ? warehouse : storehouse);
    } else return Optional.ofNullable(storehouse);
  }
}
