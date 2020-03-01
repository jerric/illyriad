package info.lliira.illyriad.map.crawl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.entity.Location;
import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.entity.Progress;
import info.lliira.illyriad.map.storage.LocationTable;
import info.lliira.illyriad.map.storage.ProgressTable;
import info.lliira.illyriad.map.storage.StorageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static info.lliira.illyriad.common.Constants.ZOOM;

 class CrawlTask implements Runnable {
   static final AtomicLong PENDING_TASKS = new AtomicLong(0);

  private static final String MAP_DATA_URL = "/World/MapData";
  private static final int MAX_DELAY_MS = 500;
  private static final Logger LOG = LogManager.getLogger(CrawlTask.class.getSimpleName());
  private static final Map<Type, JsonDeserializer<?>> DESERIALIZERS =
      Map.of(MapData.Stub.class, new StubDeserializer());

  private final StorageFactory storageFactory;
  private final AuthenticatedHttpClient.PostJson<MapData> mapDataClient;
  private final Random random;
  private final Point center;
  private final int minX;
  private final int maxX;
  private final int minY;
  private final int maxY;

   CrawlTask(StorageFactory storageFactory, Authenticator authenticator, Point center) {
    this.storageFactory = storageFactory;
    this.random = new Random();
    this.center = center;
    this.minX = center.x - ZOOM;
    this.minY = center.y - ZOOM;
    this.maxX = center.x + ZOOM;
    this.maxY = center.y + ZOOM;
    this.mapDataClient =
        new AuthenticatedHttpClient.PostJson<>(
            MAP_DATA_URL, MapData.class, DESERIALIZERS, authenticator);
    PENDING_TASKS.incrementAndGet();
  }

  @Override
  public void run() {
    try {
      // introduce a bit of delayed start
      Thread.sleep(random.nextInt(MAX_DELAY_MS));

      var mapData = fetch();
      saveMapData(mapData);

      // save the progress
      saveProgress();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      PENDING_TASKS.decrementAndGet();
    }
  }

  private MapData fetch() {
    var data = new HashMap<String, String>();
    data.put("x", Integer.toString(center.x));
    data.put("y", Integer.toString(center.y));
    data.put("zoom", Integer.toString(ZOOM));
    data.put("dir", "");
    var mapData = mapDataClient.call(data).output;
    assert mapData.isPresent();
    return mapData.get();
  }

  private void saveMapData(MapData mapData) {
    upsert(mapData.creatures(), storageFactory.creatureTable());
    upsert(mapData.deposits(), storageFactory.depositTable());
    upsert(mapData.plots(), storageFactory.plotTable());
    upsert(mapData.resources(), storageFactory.resourceTable());
    upsert(mapData.towns(), storageFactory.townTable());
  }

  private <E extends Location<B>, B extends Location.Builder<E>> void upsert(
      Collection<E> locations, LocationTable<E, B> table) {
    synchronized (table) {
      // remove everything in range.
      table.delete(minX, minY, maxX, maxY);

      locations.forEach(table::addUpsertBatch);
      table.executeUpsertBatch();
    }
  }

  private void saveProgress() {
    ProgressTable progressTable = storageFactory.progressTable();
    progressTable.upsert(
        new Progress.Builder().lastUpdated(new Date()).x(center.x).y(center.y).build());
  }

  public static class StubDeserializer implements JsonDeserializer<MapData.Stub> {
    @Override
    public MapData.Stub deserialize(
        JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
        throws JsonParseException {
      var stub = new MapData.Stub(jsonElement.toString());
      if (!stub.content.endsWith("{}")) LOG.warn("Unknown Element: {}", stub.content);
      return stub;
    }
  }
}
