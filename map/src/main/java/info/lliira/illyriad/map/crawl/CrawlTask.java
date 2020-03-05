package info.lliira.illyriad.map.crawl;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.entity.Location;
import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.storage.CreatureTable;
import info.lliira.illyriad.map.storage.DepositTable;
import info.lliira.illyriad.map.storage.LocationTable;
import info.lliira.illyriad.map.storage.PlotTable;
import info.lliira.illyriad.map.storage.ResourceTable;
import info.lliira.illyriad.map.storage.StorageFactory;
import info.lliira.illyriad.map.storage.TownTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import static info.lliira.illyriad.map.Constants.ZOOM;

class CrawlTask implements Runnable {
  static final AtomicLong PENDING_TASKS = new AtomicLong(0);

  private static final String MAP_DATA_URL = "/World/MapData";
  private static final Logger LOG = LogManager.getLogger(CrawlTask.class.getSimpleName());
  private static final Map<Type, JsonDeserializer<?>> DESERIALIZERS =
      Map.of(MapData.Stub.class, new StubDeserializer());

  private final AuthenticatedHttpClient.PostJson<MapData> mapDataClient;
  private final Queue<Point> queue;
  private final CreatureTable creatureTable;
  private final DepositTable depositTable;
  private final PlotTable plotTable;
  private final ResourceTable resourceTable;
  private final TownTable townTable;

  CrawlTask(StorageFactory storageFactory, Authenticator authenticator, Queue<Point> queue) {
    this.queue = queue;
    this.creatureTable = storageFactory.creatureTable();
    this.depositTable = storageFactory.depositTable();
    this.plotTable = storageFactory.plotTable();
    this.resourceTable = storageFactory.resourceTable();
    this.townTable = storageFactory.townTable();
    this.mapDataClient =
        new AuthenticatedHttpClient.PostJson<>(
            MAP_DATA_URL, MapData.class, DESERIALIZERS, authenticator);
    PENDING_TASKS.incrementAndGet();
  }

  @Override
  public void run() {
    Point center;
    while ((center = queue.poll()) != null) {
      try {
        var mapData = fetch(center);
        saveMapData(mapData);
      } catch (Exception e) {
        e.printStackTrace();
        // failure, send back to queue
        queue.offer(center);
      } finally {
        PENDING_TASKS.decrementAndGet();
      }
    }
  }

  private MapData fetch(Point center) {
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
    upsert(mapData.creatures(), creatureTable);
    upsert(mapData.deposits(), depositTable);
    upsert(mapData.plots(), plotTable);
    upsert(mapData.resources(), resourceTable);
    upsert(mapData.towns(), townTable);
  }

  private <E extends Location<B>, B extends Location.Builder<E>> void upsert(
      Collection<E> locations, LocationTable<E, B> table) {
    // remove everything in range.
    // table.delete(minX, minY, maxX, maxY);

    locations.forEach(table::addUpsertBatch);
    table.executeUpsertBatch();
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
