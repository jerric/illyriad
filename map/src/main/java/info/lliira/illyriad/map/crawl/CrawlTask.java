package info.lliira.illyriad.map.crawl;

import com.google.gson.JsonDeserializer;
import info.lliira.illyriad.map.Point;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.storage.Storage;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class CrawlTask implements Runnable {
  private static final String MAP_DATA_URL = "/World/MapData";
  private static final int ZOOM = 10;

  private final Storage storage;
  private final AuthenticatedHttpClient.PostJson<MapData> mapDataClient;
  private final Point center;

  public CrawlTask(Storage storage, Authenticator authenticator, Point center) {
    this.storage = storage;
    this.center = center;
    Map<Type, JsonDeserializer<?>> deserializers = Map.of();
    mapDataClient =
        new AuthenticatedHttpClient.PostJson<>(
            MAP_DATA_URL, MapData.class, deserializers, authenticator);
  }

  @Override
  public void run() {
    var data = new HashMap<String, String>();
    data.put("x", Integer.toString(center.x));
    data.put("y", Integer.toString(center.y));
    data.put("zoom", Integer.toString(ZOOM));
    data.put("dir", "");
    var mapData = mapDataClient.call(data).output;
    assert mapData.isPresent();
    storage.saveMapData(mapData.get());
  }
}
