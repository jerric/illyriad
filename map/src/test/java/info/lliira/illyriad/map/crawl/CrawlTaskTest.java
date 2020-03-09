package info.lliira.illyriad.map.crawl;

import info.lliira.illyriad.map.TestHelper;
import info.lliira.illyriad.map.entity.Point;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

public class CrawlTaskTest {
  private static final int X = 50;
  private static final int Y = 500;

  @Test
  public void getMapData() {
    Queue<Point> queue = new LinkedList<>();
    queue.add(new Point(X, Y));
    CrawlTask task = new CrawlTask(TestHelper.STORAGE_FACTORY, TestHelper.AUTHENTICATOR, queue);
    task.run();
  }
}
