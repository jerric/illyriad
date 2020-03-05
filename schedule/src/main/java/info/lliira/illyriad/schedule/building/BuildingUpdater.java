package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import org.jsoup.nodes.Document;

import java.util.Map;

public class BuildingUpdater {
  private static final String LAND_PARAM = "land";
  private static final String BUILDING_PARAM = "building";
  private static final String QUERY_URL = "/Town/Building/1/{building}";

  private final AuthenticatedHttpClient.GetHtml queryClient;

  public BuildingUpdater(Authenticator authenticator) {
    this.queryClient = new AuthenticatedHttpClient.GetHtml(QUERY_URL, authenticator);
  }

  public Building load(boolean town, int index) {
    var params = Map.of(LAND_PARAM, town? "0": "1", BUILDING_PARAM, Integer.toString(index));
    var response = queryClient.call(params);
    assert response.output.isPresent();
    return parse(response.output.get());
  }

  private Building parse(Document document) {
    String name = document.select("h1").text();
    int level = level(document);

    return null;
  }

  private int level(Document document) {
    String[] levelText = document.select("h2").text().trim().split("\\s");
    return Integer.parseInt(levelText[1]);
  }
}
