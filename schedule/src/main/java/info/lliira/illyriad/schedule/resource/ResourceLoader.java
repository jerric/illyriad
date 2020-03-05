package info.lliira.illyriad.schedule.resource;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public class ResourceLoader {
  private static final String RESOURCE_URL = "/Home/UpdateResources";

  private final AuthenticatedHttpClient.GetJson<ResponseJson> resourceClient;

  public ResourceLoader(Authenticator authenticator) {
    this.resourceClient =
        new AuthenticatedHttpClient.GetJson<>(
            RESOURCE_URL, ResponseJson.class, Map.of(), authenticator);
  }

  public Towns loadTowns() {
    var response = resourceClient.call(Map.of());
    assert response.output.isPresent();
    var json = response.output.get();
    Document document = Jsoup.parse(json.tn);
    Towns towns = new Towns();
    parseTowns(document, towns);
    parseResources(document, towns);
    return towns;
  }

  private void parseTowns(Document document, Towns towns) {
    document.select("select#optTown option").forEach(option -> {
      int id = Integer.parseInt(option.val());
      String name = option.text();
      boolean current = option.attr("selected").equals("selected");
      towns.add(new Town(id, name, current));
    });
  }

  private void parseResources(Document document, Towns towns) {
    var rows = document.select("table#tbRes tr");
    var amountRow = rows.get(0).select("td.resTxt");
    var rateRow = rows.get(1).select("td.resInc");
    for (int i = 0; i < amountRow.size(); i++) {
      int amount = Integer.parseInt(amountRow.get(i).attr("data"));
      int rate = Integer.parseInt(rateRow.get(i).attr("data"));
      towns.add(new Resource(type(i), amount, rate));
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
