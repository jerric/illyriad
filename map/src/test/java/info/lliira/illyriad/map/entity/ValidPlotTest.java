package info.lliira.illyriad.map.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidPlotTest {

  private static ValidPlot plot(int x, int y) {
    return new ValidPlot.Builder().x(x).y(y).build();
  }

  @Test
  public void outMinRange() {
    var plot = plot(10, 20);
    assertTrue(plot.minRange(plot(20, 15)));
    assertTrue(plot.minRange(plot(17, 27)));
  }

  @Test
  public void inMinRange() {
    var plot = plot(10, 20);
    assertFalse(plot.minRange(plot(15, 25)));
  }

  @Test
  public void inRange() {
    var plot = plot(10, 20);
    assertTrue(plot.inRange(plot(17, 13)));
    assertTrue(plot.inRange(plot(2, 10)));
    assertTrue(plot.inRange(plot(-4, 34)));
    assertTrue(plot.inRange(plot(10, 12)));
    assertTrue(plot.inRange(plot(18, 20)));
  }

  @Test
  public void outRange() {
    var plot = plot(10, 20);
    assertFalse(plot.inRange(plot(5, 15)));
    assertFalse(plot.inRange(plot(15, 15)));
    assertFalse(plot.inRange(plot(31, 20)));
    assertFalse(plot.inRange(plot(29, 39)));
  }
}
