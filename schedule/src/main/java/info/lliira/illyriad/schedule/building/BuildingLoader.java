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
    int level = level(document);
    var costRow = document.select("fieldset table tr").get(1).children();
    int woodCost = parseCost(costRow.get(0).text());
    int clayCost = parseCost(costRow.get(1).text());
    int ironCost = parseCost(costRow.get(2).text());
    int stoneCost = parseCost(costRow.get(3).text());
    int foodConsumption = parseConsumption(costRow.get(4).text());
    Duration time = parseTime(costRow.get(7).text());
    var updateFields = parseUploadFields(document);
    return new Building(
        name, level, woodCost, clayCost, ironCost, stoneCost, foodConsumption, time, updateFields);
  }

  private int level(Document document) {
    String[] levelText = document.select("h2").text().trim().split("\\s");
    return Integer.parseInt(levelText[1]);
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
