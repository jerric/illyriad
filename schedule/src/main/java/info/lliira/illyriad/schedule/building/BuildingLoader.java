package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildingLoader {
  private static final String LAND_PARAM = "land";
  private static final String BUILDING_PARAM = "building";
  private static final String QUERY_URL = "/Town/Building/{land}/{building}";

  private final AuthenticatedHttpClient.GetHtml queryClient;

  public BuildingLoader(Authenticator authenticator) {
    this.queryClient = new AuthenticatedHttpClient.GetHtml(QUERY_URL, authenticator);
  }

  public Building load(boolean land, int index) {
    var params = Map.of(LAND_PARAM, land ? "1" : "0", BUILDING_PARAM, Integer.toString(index));
    var response = queryClient.call(params);
    assert response.output.isPresent();
    return parse(response.output.get());
  }

  private Building parse(Document document) {
    String name = document.select("h1").text();
    int level = Integer.parseInt(document.select("h2").text().trim().split("\\s")[1]);
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

    return new Building(
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
        upgradeFields);
  }

  private int parseCost(String cost) {
    return Integer.parseInt(cost.trim().replaceAll(",", ""));
  }

  private int parseConsumption(String consumption) {
    return Integer.parseInt(consumption.trim().split(" ")[0]);
  }

  private Map<String, String> parseUploadFields(Document document) {
    return document.select("fieldset table form#UpgradeForm input").stream()
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
