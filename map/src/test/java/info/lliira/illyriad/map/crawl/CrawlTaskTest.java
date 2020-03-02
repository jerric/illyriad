package info.lliira.illyriad.map.crawl;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.storage.StorageFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;

public class CrawlTaskTest {
  private static final int X = 50;
  private static final int Y = 500;

  private Authenticator authenticator;
  private StorageFactory storageFactory;

  @BeforeEach
  public void setUp() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    authenticator = new Authenticator(properties);
    storageFactory = new StorageFactory(properties);
  }

  @Test
  public void getMapData() {
    Queue<Point> queue = new LinkedList<>();
    queue.add(new Point(X, Y));
    CrawlTask task = new CrawlTask(storageFactory, authenticator, queue);
    task.run();
  }
}
