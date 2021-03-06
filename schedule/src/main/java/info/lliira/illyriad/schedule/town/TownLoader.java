package info.lliira.illyriad.schedule.town;

import com.google.gson.annotations.SerializedName;
import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TownLoader {
  private static final String RESOURCE_URL = "/Home/UpdateResources";
  private static final String CHANGE_TOWN_FIELD = "ChangeTown";
  private static final String INFO_URL = "/Town/Castle";

  private final AuthenticatedHttpClient.GetJson<ResponseJson> resourceClient;
  private final AuthenticatedHttpClient.PostJson<ResponseJson> channgeTownClient;
  private final AuthenticatedHttpClient.GetHtml infoClient;

  public TownLoader(Authenticator authenticator) {
    this.resourceClient =
        new AuthenticatedHttpClient.GetJson<>(
            RESOURCE_URL, ResponseJson.class, Map.of(), authenticator);
    this.channgeTownClient =
        new AuthenticatedHttpClient.PostJson<>(
            RESOURCE_URL, ResponseJson.class, Map.of(), authenticator);
    this.infoClient = new AuthenticatedHttpClient.GetHtml(INFO_URL, authenticator);
  }

  public Town loadTown() {
    var response = resourceClient.call(Map.of());
    assert response.output.isPresent();
    return parseTown(response.output.get());
  }

  public Town changeTown(int townId) {
    var fields = Map.of(CHANGE_TOWN_FIELD, Integer.toString(townId));
    var response = channgeTownClient.call(fields);
    assert response.output.isPresent();
    return parseTown(response.output.get());
  }

  public TownInfo loadTownInfo() {
    var response = infoClient.call(Map.of());
    assert response.output.isPresent();
    Document document = response.output.get();
    var fields = new HashMap<String, String>();
    for (var row : document.select("div.info table tr")) {
      var cells = row.select("td");
      String name = cells.get(0).text().trim();
      String value =
          name.equals("Rename City:")
              ? cells.get(1).select("input.text").val()
              : cells.get(1).text();
      fields.put(name, value);
    }
    return new TownInfo(
        fields.get("Rename City:"),
        TownInfo.Race.valueOf(fields.get("Race")),
        fields.get("City Founded"),
        Integer.parseInt(fields.get("City Population").replaceAll(",", "")),
        Integer.parseInt(fields.get("City Storehouse(s) capacity").replaceAll(",", "")),
        fields.get("City Size Described"),
        fields.get("Location"));
  }

  private Town parseTown(ResponseJson response) {
    var progress = parseProgress(Jsoup.parse(response.ne));
    Town town = new Town(progress);

    Document document = Jsoup.parse(response.tn);
    parseTowns(document, town);
    parseResources(document, town);

    parseProducts(Jsoup.parse(response.ar), town);

    return town;
  }

  private Progress parseProgress(Document document) {
    var rows = document.select("table tr");
    var construction1 = parseProgress(rows.get(2));
    var construction2 = parseProgress(rows.get(3));
    var research1 = parseProgress(rows.get(4));
    var research2 = parseProgress(rows.get(5));
    return new Progress(construction1, construction2, research1, research2);
  }

  private Optional<WaitTime> parseProgress(Element row) {
    var span = row.select("td span.progTime");
    if (span.isEmpty()) return Optional.empty();
    var data = span.attr("data").split("\\|");
    double diffInSeconds =
        Math.ceil((Long.parseLong(data[1]) - System.currentTimeMillis()) / 1000D);
    return Optional.of(new WaitTime(Math.max(1, Math.round(diffInSeconds))));
  }

  private void parseTowns(Document document, Town town) {
    document
        .select("select#optTown option")
        .forEach(
            option -> {
              int id = Integer.parseInt(option.val());
              String name = option.text();
              boolean current = option.attr("selected").equals("selected");
              town.add(new TownEntity(id, name, current));
            });
  }

  private void parseResources(Document document, Town town) {
    var rows = document.select("table#tbRes tr");
    var amountRow = rows.get(0).select("td.resTxt");
    var rateRow = rows.get(1).select("td.resInc");
    for (int i = 0; i < amountRow.size(); i++) {
      int amount = Integer.parseInt(amountRow.get(i).attr("data"));
      int rate = Integer.parseInt(rateRow.get(i).attr("data"));
      town.add(new Resource(resourceType(i), amount, rate));
    }
  }

  private void parseProducts(Document document, Town town) {
    var cells = document.select("tr").first().select("td.resTxt");
    for (int i = 0; i < cells.size(); i++) {
      var type = productType(i);
      var amount = Integer.parseInt(cells.get(i).attr("data"));
      town.add(new Product(type, amount));
    }
  }

  private Product.Type productType(int i) {
    if (i == 0) return Product.Type.Horse;
    else if (i == 1) return Product.Type.Livestock;
    else if (i == 2) return Product.Type.Beer;
    else if (i == 3) return Product.Type.Book;
    else if (i == 4) return Product.Type.Spear;
    else if (i == 5) return Product.Type.Sword;
    else if (i == 6) return Product.Type.Bow;
    else if (i == 7) return Product.Type.Saddle;
    else if (i == 8) return Product.Type.Leather;
    else if (i == 9) return Product.Type.Chainmail;
    else if (i == 10) return Product.Type.Plate;
    else if (i == 11) return Product.Type.Siege;
    else throw new RuntimeException("Unknown mapping to product type: " + i);
  }

  private Resource.Type resourceType(int i) {
    if (i == 0) return Resource.Type.Gold;
    else if (i == 1) return Resource.Type.Wood;
    else if (i == 2) return Resource.Type.Clay;
    else if (i == 3) return Resource.Type.Iron;
    else if (i == 4) return Resource.Type.Stone;
    else if (i == 5) return Resource.Type.Food;
    else if (i == 6) return Resource.Type.Mana;
    else if (i == 7) return Resource.Type.Research;
    else throw new RuntimeException("Unknown mapping to resource type: " + i);
  }

  private static class ResponseJson {
    private int s;

    @SerializedName("t")
    private int townId;

    private String ne;
    private String tn;
    private String ar;
    private String tl;
  }
}
