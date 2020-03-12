package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.town.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuildingLoader {
  private static final String LAND_PARAM = "land";
  private static final String BUILDING_PARAM = "building";
  private static final String QUERY_URL = "/Town/Building/{land}/{building}";
  private static final int MIN_BUILDING_INDEX = 1;
  private static final int MAX_BUILDING_INDEX = 25;

  private static final Logger LOG = LogManager.getLogger(BuildingLoader.class.getSimpleName());

  private final AuthenticatedHttpClient.GetHtml queryClient;

  public BuildingLoader(Authenticator authenticator) {
    this.queryClient = new AuthenticatedHttpClient.GetHtml(QUERY_URL, authenticator);
  }

  // Loads one of build of each resource with the lowest nextLevel.
  public Map<Resource.Type, Building> loadMinLandBuildings() {
    var buildings = new HashMap<Resource.Type, Building>(5);
    for (int index = MIN_BUILDING_INDEX; index <= MAX_BUILDING_INDEX; index++) {
      var buildingOptional = load(true, index);
      if (buildingOptional.isEmpty()) continue;
      var building = buildingOptional.get();
      if (building.type == null) continue;
      Resource.Type type = building.type.resourceType;
      var previous = buildings.get(type);
      if (previous == null || previous.nextLevel > building.nextLevel)
        buildings.put(type, building);
    }
    return buildings;
  }

  public Map<Building.Type, Building> loadTownBuildings() {
    var buildings = new HashMap<Building.Type, Building>(MAX_BUILDING_INDEX);
    for (int index = MIN_BUILDING_INDEX; index <= MAX_BUILDING_INDEX; index++) {
      var buildingOptional = load(false, index);
      if (buildingOptional.isEmpty()) continue;
      var building = buildingOptional.get();
      if (building.type != null) {
        buildings.put(building.type, building);
      } else {
        LOG.warn("Building with unknown type: {}", building.name);
      }
    }
    return buildings;
  }

  public Optional<Building> load(boolean land, int index) {
    var params = Map.of(LAND_PARAM, land ? "1" : "0", BUILDING_PARAM, Integer.toString(index));
    var response = queryClient.call(params);
    assert response.output.isPresent();
    return parse(response.output.get());
  }

  private Optional<Building> parse(Document document) {
    String name = document.select("h1").text();
    String levelString = document.select("h2").text().trim().toLowerCase();
    if (!levelString.startsWith("level ")) return Optional.empty();
    int level = Integer.parseInt(levelString.split("\\s")[1]);
    boolean upgrading = false;
    int nextLevel = level + 1;
    int woodCost = 0, clayCost = 0, ironCost = 0, stoneCost = 0, foodConsumption = 0;
    Duration time = Duration.ofSeconds(0);
    Map<String, String> upgradeFields = Map.of();
    for (var fieldSet : document.select("fieldset")) {
      String legend = fieldSet.select("legend").text().trim();
      if (legend.equals("Building works in progress")) upgrading = true;
      else if (legend.equals("Upgrade Further")) {
        var rows = fieldSet.select("table tr");
        nextLevel = Integer.parseInt(rows.get(0).child(0).text().trim().split(" ")[1]);
        var costCells = rows.get(1).children();
        woodCost = parseCost(costCells.get(0).text());
        clayCost = parseCost(costCells.get(1).text());
        ironCost = parseCost(costCells.get(2).text());
        stoneCost = parseCost(costCells.get(3).text());
        foodConsumption = parseConsumption(costCells.get(4).text());
        time = parseTime(costCells.get(7).text());
        upgradeFields = parseUploadFields(document);
      }
    }

    return Optional.of(new Building(
        name,
        level,
        upgrading,
        nextLevel,
        woodCost,
        clayCost,
        ironCost,
        stoneCost,
        foodConsumption,
        time,
        upgradeFields));
  }

  private int parseCost(String cost) {
    return Integer.parseInt(cost.trim().replaceAll(",", ""));
  }

  private int parseConsumption(String consumption) {
    return Integer.parseInt(consumption.trim().split(" ")[0]);
  }

  private Map<String, String> parseUploadFields(Document document) {
    return document.select("fieldset table form#UpgradeForm input").stream()
        .filter(input -> !input.attr("name").isBlank())
        .collect(Collectors.toMap(input -> input.attr("name"), Element::val));
  }

  private Duration parseTime(String time) {
    var parts = time.trim().split(" ");
    int totalSec = 0;
    for (var part : parts) {
      char field = part.charAt(part.length() - 1);
      int number = Integer.parseInt(part.substring(0, part.length() - 1));
      if (field == 'h') totalSec += number * 3600;
      if (field == 'm') totalSec += number * 60;
      if (field == 's') totalSec += number;
    }
    return Duration.ofSeconds(totalSec);
  }
}
