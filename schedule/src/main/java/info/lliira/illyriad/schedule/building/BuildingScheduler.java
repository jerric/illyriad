package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
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

public class BuildingScheduler implements Runnable {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var scheduler = new BuildingScheduler(authenticator);
    scheduler.run();
  }

  private static final int MIN_BUILDING_INDEX = 1;
  private static final int MAX_BUILDING_INDEX = 25;
  private static final int FOOD_PERCENT = 15;

  private static final Logger LOG = LogManager.getLogger(BuildingScheduler.class.getSimpleName());

  private final TownLoader townLoader;
  private final BuildingLoader buildingLoader;
  private final BuildingUpgrader buildingUpgrader;
  private boolean running;

  public BuildingScheduler(Authenticator authenticator) {
    this.townLoader = new TownLoader(authenticator);
    this.buildingLoader = new BuildingLoader(authenticator);
    this.buildingUpgrader = new BuildingUpgrader(authenticator);
  }

  public void stop() {
    running = false;
  }

  @Override
  public void run() {
    running = true;
    while (running) {
      LOG.info("Loading Town Resources...");
      var town = townLoader.loadTown();
      LOG.info(
          "Gold:{}, Wood:{}, Clay:{}, Iron:{}, Stone:{}, Food:{}",
          town.resources.get(Resource.Type.Gold),
          town.resources.get(Resource.Type.Wood),
          town.resources.get(Resource.Type.Clay),
          town.resources.get(Resource.Type.Iron),
          town.resources.get(Resource.Type.Stone),
          town.resources.get(Resource.Type.Food));
      LOG.info("{} constructions in progress", town.progress.constructionCount());
      long toWaitMillis;
      if (town.progress.construction2.isPresent() && town.progress.construction1.isPresent()) {
        // build queue is full, wait for the fist building to finish
        toWaitMillis = town.progress.construction1.get() - System.currentTimeMillis();
      } else {
        Building building = findNextToUpgrade(town);
        LOG.info("Next building to build: {} -> level {}", building.name, building.nextLevel);
        LOG.info(
            "Resource needed: wood:{}, clay:{}, iron:{}, stone:{}",
            building.woodCost,
            building.clayCost,
            building.ironCost,
            building.stoneCost);
        toWaitMillis = waitTillEnoughResources(town, building);
        if (toWaitMillis == 0) buildingUpgrader.upgrade(building);
      }
      if (toWaitMillis > 0) {
        LOG.info("Waiting for {} seconds.", toWaitMillis / 1000);
        try {
          Thread.sleep(toWaitMillis);
        } catch (InterruptedException e) { // do nothing
        }
      }
    }
  }

  private Building findNextToUpgrade(Town town) {
    var resourceBuildings = loadResourceBuildings();
    long toTimestamp = town.progress.construction1.orElse(System.currentTimeMillis());

    // choose the lowest resource type at the timestamp
    final Resource.Type[] types = {
      Resource.Type.Wood, Resource.Type.Clay, Resource.Type.Iron, Resource.Type.Stone
    };
    Resource minResource = null;
    long minAmount = 0;
    for (var type : types) {
      Resource resource = town.resources.get(type);
      int amount = resource.till(toTimestamp);
      if (minResource == null || minAmount > amount) {
        minResource = resource;
        minAmount = amount;
      }
    }
    long percent = 100 * town.resources.get(Resource.Type.Food).current() / minResource.current();
    return resourceBuildings.get((percent < FOOD_PERCENT) ? Resource.Type.Food : minResource.type);
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
