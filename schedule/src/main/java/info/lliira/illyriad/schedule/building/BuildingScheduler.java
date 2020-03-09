package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import info.lliira.illyriad.schedule.resource.Resource;
import info.lliira.illyriad.schedule.resource.Town;
import info.lliira.illyriad.schedule.resource.TownLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BuildingScheduler extends Scheduler {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var scheduler = new BuildingScheduler(authenticator);
    scheduler.run();
  }

  private static final int MIN_BUILDING_INDEX = 1;
  private static final int MAX_BUILDING_INDEX = 25;
  private static final double FOOD_RATIO = 0.2;
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
    this.townLoader = new TownLoader(authenticator);
    this.buildingLoader = new BuildingLoader(authenticator);
    this.buildingUpgrader = new BuildingUpgrader(authenticator);
  }

  @Override
  public long schedule() {
    LOG.info("=============== Loading Town Resources ===============");
    var town = townLoader.loadTown();
    LOG.info(town);
    LOG.info("{} constructions in progress", town.progress.constructionCount());

    long toWaitMillis = 0;
    if (town.progress.construction1.isEmpty()) {
      Building building = findNextToUpgrade(town);
      toWaitMillis = waitTillEnoughResources(town, building);
      if (toWaitMillis == 0) {
        LOG.info("Constructing Next building: {} -> level {}", building.name, building.nextLevel);
        buildingUpgrader.upgrade(building);
        toWaitMillis = building.time.toMillis();
      } else {
        LOG.info(
            "Lack resources for Next building: {} -> level {}", building.name, building.nextLevel);
      }
    } else toWaitMillis = town.progress.constructionTimestamp() - System.currentTimeMillis();
    LOG.info("Waiting for {} seconds", String.format("%,d", toWaitMillis / 1000));
    return toWaitMillis;
  }

  private Building findNextToUpgrade(Town town) {
    var resourceBuildings = loadResourceBuildings();
    long toTimestamp = town.progress.constructionTimestamp();

    // check if we need to produce food
    if (shouldScheduleFood(town, toTimestamp)) return resourceBuildings.get(Resource.Type.Food);

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
    return resourceBuildings.get(minResource.type);
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

  // Loads one of build of each resource with the lowest nextLevel.
  private Map<Resource.Type, Building> loadResourceBuildings() {
    var buildings = new HashMap<Resource.Type, Building>(5);
    for (int index = MIN_BUILDING_INDEX; index <= MAX_BUILDING_INDEX; index++) {
      var building = buildingLoader.load(true, index);
      Resource.Type type = building.type.resourceType;
      var previous = buildings.get(type);
      if (previous == null || previous.nextLevel > building.nextLevel)
        buildings.put(type, building);
    }
    return buildings;
  }

  private long waitTillEnoughResources(Town town, Building building) {
    long waitMillis = waitTill(building.woodCost, town.resources.get(Resource.Type.Wood));
    waitMillis =
        Math.max(waitMillis, waitTill(building.clayCost, town.resources.get(Resource.Type.Clay)));
    waitMillis =
        Math.max(waitMillis, waitTill(building.ironCost, town.resources.get(Resource.Type.Iron)));
    waitMillis =
        Math.max(waitMillis, waitTill(building.stoneCost, town.resources.get(Resource.Type.Stone)));
    return waitMillis;
  }

  private long waitTill(int cost, Resource resource) {
    int diff = cost - resource.current();
    return (diff <= 0) ? 0 : Math.round(Math.ceil(1.0 * diff / resource.rate * 3_600_000));
  }
}
