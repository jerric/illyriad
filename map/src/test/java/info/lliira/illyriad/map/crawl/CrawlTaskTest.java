package info.lliira.illyriad.map.crawl;

import info.lliira.illyriad.map.Point;
import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.map.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CrawlTaskTest {
  private static final int X = 50;
  private static final int Y = 500;

  private Authenticator authenticator;

  @BeforeEach
  public void setUp() throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    authenticator = new Authenticator(properties);
  }

  @Test
  public void getMapData() {
    Storage storage = mapData -> {
      System.out.println(mapData);
      assertEquals(X, mapData.x());
      assertEquals(Y, mapData.y());
      assertTrue(mapData != null);
    };
    CrawlTask task = new CrawlTask(storage, authenticator, new Point(X, Y));
    task.run();
  }
}
