package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import info.lliira.illyriad.schedule.building.BuildingScheduler;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class TownScheduler extends Scheduler {

  private static final String TOWNS_BUILD_LIMITED_PROPERTY = "towns.build.limited";
  private static final String TOWNS_PRODUCT_LIMITED_PROPERTY = "town.product.limited";
  private static final String FOOD_RATIO_FIELD = "schedule.food.ratio";
  private static final String STORAGE_RATIO_FIELD = "schedule.storage.ratio";
  private static final String DEFAULT_FOOD_RATIO = "0.15";
  private static final String DEFAULT_STORAGE_RATIO = "0.8";

  private final Authenticator authenticator;
  private final TownLoader townLoader;
  private final BuildingScheduler buildingScheduler;
  private final ProductSceduler productSceduler;

  public TownScheduler(Authenticator authenticator) {
    super(TownScheduler.class.getSimpleName());
    this.authenticator = authenticator;
    this.townLoader = new TownLoader(authenticator);
    this.buildingScheduler = new BuildingScheduler(authenticator);
    this.productSceduler = new ProductSceduler(authenticator);
  }

  @Override
  public String player() {
    return authenticator.player();
  }

  @Override
  public WaitTime schedule() {
    var properties = new Properties();
    try {
      properties.load(new FileReader(Constants.PROPERTY_FILE));
      var townsBuildLimited =
          loadTownList(properties.getProperty(TOWNS_BUILD_LIMITED_PROPERTY, ""));
      var townsProductLimited =
          loadTownList(properties.getProperty(TOWNS_PRODUCT_LIMITED_PROPERTY, ""));
      var minWaitTime = new WaitTime(Long.MAX_VALUE);
      float foodRatio =
          Float.parseFloat(properties.getProperty(FOOD_RATIO_FIELD, DEFAULT_FOOD_RATIO));
      float storageRatio =
          Float.parseFloat(properties.getProperty(STORAGE_RATIO_FIELD, DEFAULT_STORAGE_RATIO));

      var townEntities = townLoader.loadTown().towns;
      for (int townId : townEntities.keySet()) {
        Town town = townLoader.changeTown(townId);
        TownInfo townInfo = townLoader.loadTownInfo();
        var waitTime =
            buildingScheduler.schedule(
                town,
                townInfo,
                new BuildingScheduler.Params(
                    townsBuildLimited.contains(townId), foodRatio, storageRatio));
        minWaitTime = minWaitTime.min(waitTime);
        if (!townsProductLimited.contains(townId)) {
          waitTime = productSceduler.schedule(townInfo);
          minWaitTime = minWaitTime.min(waitTime);
        }
      }
      return minWaitTime;
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  private Set<Integer> loadTownList(String townsString) {
    return Arrays.stream(townsString.split(","))
        .map(String::trim)
        .filter(id -> !id.isEmpty())
        .map(Integer::parseInt)
        .collect(Collectors.toSet());
  }
}
