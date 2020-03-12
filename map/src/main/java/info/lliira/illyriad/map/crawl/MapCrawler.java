package info.lliira.illyriad.map.crawl;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.storage.StorageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static info.lliira.illyriad.map.Constants.MAP_MAX_X;
import static info.lliira.illyriad.map.Constants.MAP_MAX_Y;
import static info.lliira.illyriad.map.Constants.MAP_MIN_X;
import static info.lliira.illyriad.map.Constants.MAP_MIN_Y;
import static info.lliira.illyriad.map.Constants.ZOOM;

public class MapCrawler {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var storageFactory = new StorageFactory(properties);
    var crawler = new MapCrawler(authenticator, storageFactory, properties);
    crawler.crawl();
  }

  private static final Logger LOG = LogManager.getLogger(MapCrawler.class.getSimpleName());

  private static final String POOL_SIZE_KEY = "crawl.pool.size";
  private static final String DEFAULT_POOL_SIZE = "10";

  private final Authenticator authenticator;
  private final int poolSize;
  private final StorageFactory storageFactory;

  public MapCrawler(
      Authenticator authenticator, StorageFactory storageFactory, Properties properties) {
    this.authenticator = authenticator;
    this.poolSize = Integer.parseInt(properties.getProperty(POOL_SIZE_KEY, DEFAULT_POOL_SIZE));
    this.storageFactory = storageFactory;
  }

  public void crawl() {
    LOG.info("Cleaning up old data");
    cleanUp();
    LOG.info("Creating crawl tasks...");
    Queue<Point> queue = createTasks();
    int total = queue.size();
    // Starting tasks
    var crawlerPool = Executors.newFixedThreadPool(poolSize);
    for (int i = 0; i < poolSize; i++) {
      crawlerPool.execute(new CrawlTask(storageFactory, authenticator, queue));
    }
    while (!queue.isEmpty()) {
      int current = queue.size();
      LOG.info(
          "Plot crawler tasks progress: {}/{}({}%)",
          total - current, total, Math.round(1.0 * (total - current) / total * 10000) / 100.0);
      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
      } catch (InterruptedException ignore) {
      }
    }
    crawlerPool.shutdown();
    while (true) {
      try {
        if (crawlerPool.awaitTermination(1, TimeUnit.SECONDS)) break;
      } catch (InterruptedException ignore) {
      }
    }
    LOG.info("Plot crawler finished.");
  }

  private void cleanUp() {
    storageFactory.creatureTable().deleteAll();
    storageFactory.depositTable().deleteAll();
    storageFactory.plotTable().deleteAll();
    storageFactory.resourceTable().deleteAll();
    storageFactory.townTable().deleteAll();
  }

  private Queue<Point> createTasks() {
    Queue<Point> queue = new LinkedList<>();
    int minX = MAP_MIN_X + ZOOM;
    int minY = MAP_MIN_Y + ZOOM;
    for (int y = minY; y <= MAP_MAX_Y; y += ZOOM * 2 + 1) {
      for (int x = minX; x <= MAP_MAX_X; x += ZOOM * 2 + 1) {
        queue.offer(new Point(x, y));
      }
    }
    return queue;
  }
}
