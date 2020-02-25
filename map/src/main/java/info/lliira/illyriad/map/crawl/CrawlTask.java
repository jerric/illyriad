package info.lliira.illyriad.map.crawl;

import info.lliira.illyriad.map.MapData;
import info.lliira.illyriad.map.Point;
import info.lliira.illyriad.map.crawl.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.map.crawl.common.net.Authenticator;

import java.util.HashMap;

public class CrawlTask implements Runnable {
  private static final String MAP_DATA_URL = "/World/MapData";
  private static final int ZOOM = 10;

  private final AuthenticatedHttpClient.PostJson<MapData> mapDataClient;
  private final Point center;

  public CrawlTask(Authenticator authenticator, Point center) {
    this.center = center;
    mapDataClient = new AuthenticatedHttpClient.PostJson<>(MAP_DATA_URL, MapData.class, authenticator);
  }

  @Override
  public void run() {
    var data = new HashMap<String, String>();
    data.put("x", Integer.toString(center.x));
    data.put("y", Integer.toString(center.y));
    data.put("zoom", Integer.toString(ZOOM));
    data.put("dir", "");
    var mapData = mapDataClient.call(data).output.get();

  }
}
