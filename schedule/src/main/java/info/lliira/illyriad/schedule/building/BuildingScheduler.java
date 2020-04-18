package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.common.net.AuthenticatorManager;
import info.lliira.illyriad.schedule.town.Resource;
import info.lliira.illyriad.schedule.town.Town;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

public class BuildingScheduler {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new AuthenticatorManager(properties).first();
    var scheduler = new BuildingScheduler(authenticator);
    var townLoader = new TownLoader(authenticator);
    scheduler.schedule(
        townLoader.loadTown(), townLoader.loadTownInfo(), new Params(true, 0.2F, 0.8F));
  }

  private static final int MAX_LEVEL_LIMITED = 12;
  private static final int MAX_LEVEL_UNLIMITED = 20;
  private static final String PENDING_BUILDING_FILE_PREFIX = "building.pending.";

  private static final Map<Resource.Type, Double> BUILDING_RESOURCE_TYPES = new HashMap<>();

  static {
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Wood, 1.0);
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Clay, 1.0);
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Iron, 1.0);
    BUILDING_RESOURCE_TYPES.put(Resource.Type.Stone, 1.0);
  }

  private static final Logger LOG = LogManager.getLogger(BuildingScheduler.class.getSimpleName());

  private final Authenticator authenticator;
  private final BuildingLoader buildingLoader;
  private final BuildingUpgrader buildingUpgrader;
  private final Random random;

  public BuildingScheduler(Authenticator authenticator) {
    this.authenticator = authenticator;
    this.buildingLoader = new BuildingLoader(authenticator);
    this.buildingUpgrader = new BuildingUpgrader(authenticator);
    this.random = new Random();
  }

  public WaitTime schedule(Town town, TownInfo townInfo, Params params) {
    String logString = String.format("+++ %s {%d}%s ", authenticator.player(), town.id(), townInfo);
    WaitTime waitTime;
    if (town.progress.building1.isEmpty()) {
      var pendingBuildings = loadingPendingBuildings(town.id());
      var buildingOptional = findNextToUpgrade(town, townInfo, pendingBuildings, params);
      assert buildingOptional.isPresent();
      var building = buildingOptional.get();
      String buildingTitle = String.format("%s lvl:->%d ", building.name, building.nextLevel);
      waitTime = waitTillEnoughResources(town, building);
      boolean ready = waitTime.expired();
      if (ready) {
        buildingUpgrader.upgrade(building);
        savePendingBuildings(town.id(), building, pendingBuildings);
        // Add 5 sec buffer as the actual time is usually +5 than the estimated time.
        waitTime = building.time.addSeconds(5);
      }
      logString += buildingTitle + (ready ? "upgrading" : "lack res");
    } else {
      waitTime = town.progress.buildingWaitTime();
      logString += "construction pending";
    }
    LOG.info("{} -> to wait:{}", logString, waitTime);
    return waitTime;
  }

  private LinkedList<Building.Type> loadingPendingBuildings(int townId) {
    var buildings = new LinkedList<Building.Type>();
    var pendingFile = new File(PENDING_BUILDING_FILE_PREFIX + townId);
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
    return buildings;
  }

  private void savePendingBuildings(
      int townId, Building updated, LinkedList<Building.Type> pendingBuildings) {
    if (!pendingBuildings.isEmpty() && updated.type == pendingBuildings.getFirst()) {
      // the first pending build was scheduled, remove it from the list and save back
      pendingBuildings.pollFirst();
      var pendingFile = new File(PENDING_BUILDING_FILE_PREFIX + townId);
      try (var writer = new PrintWriter(pendingFile)) {
        pendingBuildings.stream().map(Building.Type::name).forEach(writer::println);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private Optional<Building> findNextToUpgrade(
      Town town, TownInfo townInfo, LinkedList<Building.Type> pendingBuildings, Params params) {
    var townBuildings = buildingLoader.loadTownBuildings();
    var landBuildings = buildingLoader.loadMinLandBuildings();

    // check if the storage is near full, and we need to build more storage
    if (shouldScheduleStorage(town, townInfo, params)) {
      var storageBuilding = findStorageBuilding(townBuildings);
      if (storageBuilding.isPresent()) return storageBuilding;
    }

    // check if we need to produce food
    if (shouldScheduleFood(town, params)) {
      var foodBuilding = landBuildings.get(Building.Type.Farmyard);
      if (foodBuilding != null) return Optional.of(foodBuilding);
    }

    for (var pendingType : pendingBuildings) {
      var pendingBuilding = townBuildings.get(pendingType);
      if (pendingBuilding != null) return Optional.of(pendingBuilding);
      pendingBuilding = landBuildings.get(pendingType);
      if (pendingBuilding != null) return Optional.of(pendingBuilding);
    }

    var maxBuildingLevel = params.limited ? MAX_LEVEL_LIMITED : MAX_LEVEL_UNLIMITED;

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
    var landBuilding = landBuildings.get(minType.building);
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

  private boolean shouldScheduleFood(Town town, Params params) {
    var food = town.resources.get(Resource.Type.Food);
    // keep producing food
    if (food.rate <= 0) return true;

    // compare the food with the average of the other resources
    long sum = 0;
    for (Resource.Type type : BUILDING_RESOURCE_TYPES.keySet()) {
      sum += town.resources.get(type).current();
    }
    return params.foodRatio * sum / BUILDING_RESOURCE_TYPES.size() > food.current();
  }

  private WaitTime waitTillEnoughResources(Town town, Building building) {
    long max = waitSeconds(building.woodCost, town.resources.get(Resource.Type.Wood));
    max = Math.max(max, waitSeconds(building.clayCost, town.resources.get(Resource.Type.Clay)));
    max = Math.max(max, waitSeconds(building.ironCost, town.resources.get(Resource.Type.Iron)));
    max = Math.max(max, waitSeconds(building.stoneCost, town.resources.get(Resource.Type.Stone)));
    return new WaitTime(max);
  }

  private long waitSeconds(int cost, Resource resource) {
    int diff = cost - resource.current();
    return Math.max(0, Math.round(Math.ceil(1.0 * diff / resource.rate * 3_600)));
  }

  private boolean shouldScheduleStorage(Town town, TownInfo townInfo, Params params) {
    int max = 0;
    for (var resource : town.resources.values()) {
      // skip Gold as it doesn't use storage
      if (resource.type == Resource.Type.Gold) continue;
      max = Math.max(max, resource.current());
    }
    return 1.0 * max / townInfo.capacity > params.storageRatio;
  }

  private Optional<Building> findStorageBuilding(Map<Building.Type, Building> townBuildings) {
    var storehouse = townBuildings.get(Building.Type.Storehouse);
    var warehouse = townBuildings.get(Building.Type.Warehouse);
    if (storehouse != null && warehouse != null) {
      return Optional.of(storehouse.nextLevel > warehouse.nextLevel ? warehouse : storehouse);
    } else return Optional.ofNullable(storehouse);
  }

  public static class Params {
    public final boolean limited;
    public final float foodRatio;
    public final float storageRatio;

    public Params(boolean limited, float foodRatio, float storageRatio) {
      this.limited = limited;
      this.foodRatio = foodRatio;
      this.storageRatio = storageRatio;
    }
  }
}
