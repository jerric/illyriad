package info.lliira.illyriad.schedule.resource;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;
import java.util.Optional;

public class TownLoader {
  private static final String RESOURCE_URL = "/Home/UpdateResources";

  private final AuthenticatedHttpClient.GetJson<ResponseJson> resourceClient;

  public TownLoader(Authenticator authenticator) {
    this.resourceClient =
        new AuthenticatedHttpClient.GetJson<>(
            RESOURCE_URL, ResponseJson.class, Map.of(), authenticator);
  }

  public Town loadTowns() {
    var response = resourceClient.call(Map.of());
    assert response.output.isPresent();
    var json = response.output.get();

    var progress = parseProgress(Jsoup.parse(json.ne));
    Town town = new Town(progress);

    Document document = Jsoup.parse(json.tn);
    parseTowns(document, town);
    parseResources(document, town);
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

  private Optional<Long> parseProgress(Element row) {
    var span = row.select("td span.progTime");
    if (span.isEmpty()) return Optional.empty();
    var data = span.attr("data").split("\\|");
    return Optional.of(Long.parseLong(data[1]));
  }

  private void parseTowns(Document document, Town town) {
    document.select("select#optTown option").forEach(option -> {
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
      town.add(new Resource(type(i), amount, rate));
    }
  }

  private Resource.Type type(int i) {
    if (i == 0) return Resource.Type.Gold;
    else if(i == 1) return Resource.Type.Wood;
    else if(i == 2) return Resource.Type.Clay;
    else if(i == 3) return Resource.Type.Iron;
    else if(i == 4) return Resource.Type.Stone;
    else if(i == 5) return Resource.Type.Food;
    else if(i == 6) return Resource.Type.Mana;
    else if(i == 7) return Resource.Type.Research;
    else throw new RuntimeException("Unknown mapping to resource type: " + i);
  }

  private static class ResponseJson {
    private int s;
    private int t;
    private String ne;
    private String tn;
    private String ar;
    private String tl;
  }
}
