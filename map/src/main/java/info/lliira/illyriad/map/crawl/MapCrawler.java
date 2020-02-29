package info.lliira.illyriad.map.crawl;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.storage.ProgressTable;
import info.lliira.illyriad.map.storage.StorageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static info.lliira.illyriad.common.Constants.ZOOM;

public class MapCrawler {

  public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var storageFactory = new StorageFactory(properties);
    var crawler = new MapCrawler(authenticator, storageFactory, properties);
    crawler.crawl();
  }

  private static final Logger LOG = LogManager.getLogger(MapCrawler.class.getSimpleName());

  private static final String CRAWL_SIZE_KEY = "crawl.pool.size";
  private static final String CRAWL_PLOT_INTERVAL_MINUTES_KEY = "crawl.plot.interval.minutes";

  private final Authenticator authenticator;
  private final int poolSize;
  private final StorageFactory storageFactory;
  private final long crawlIntervalMinutes;

  public MapCrawler(
      Authenticator authenticator, StorageFactory storageFactory, Properties properties) {
    this.authenticator = authenticator;
    this.poolSize = Integer.parseInt(properties.getProperty(CRAWL_SIZE_KEY, "10"));
    this.storageFactory = storageFactory;
    this.crawlIntervalMinutes =
        Long.parseLong(properties.getProperty(CRAWL_PLOT_INTERVAL_MINUTES_KEY, "3600"));
  }

  public void crawl() throws SQLException {
    LOG.info("Checking past progresses...");
    var recentProgresses = getProgresses();
    LOG.info("Creating crawl tasks...");
    var crawlerPool = Executors.newFixedThreadPool(poolSize);
    long total = createTasks(recentProgresses, crawlerPool);
    long pending;
    while ((pending = CrawlTask.PENDING_TASKS.get()) > 0) {
      LOG.info(
          "Plot crawler tasks progress: {}/{}({}%)",
          pending, total, Math.round(1.0 * (total - pending) / total * 10000) / 100.0);
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

  private Set<Point> getProgresses() throws SQLException {
    LOG.info("Checking past progresses...");
    ProgressTable progressTable = storageFactory.progressTable();
    Timestamp cutoffTimestamp = progressTable.getCutoffTimestamp(crawlIntervalMinutes);
    LOG.info("Deleting expired progresses...");
    progressTable.deleteExpiredProgresses(cutoffTimestamp);
    return progressTable.selectRecentProgresses(cutoffTimestamp);
  }

  private long createTasks(Set<Point> recentProgresses, ExecutorService pool) {
    int minX = Constants.MAP_MIN_X + ZOOM;
    int minY = Constants.MAP_MIN_Y + ZOOM;
    long total = 0;
    for (int y = minY; y <= Constants.MAP_MAX_Y; y += ZOOM * 2 + 1) {
      for (int x = minX; x <= Constants.MAP_MAX_X; x += ZOOM * 2 + 1) {
        // Already crawled recently, skip
        if (recentProgresses.contains(new Point(x, y))) continue;
        pool.submit(new CrawlTask(storageFactory, authenticator, new Point(x, y)));
        total++;
      }
    }
    return total;
  }
}
