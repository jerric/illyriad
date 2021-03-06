package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.map.TestHelper;
import info.lliira.illyriad.map.entity.ValidPlot;
import info.lliira.illyriad.map.storage.ValidPlotTable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static info.lliira.illyriad.map.Constants.MAP_MAX_X;
import static info.lliira.illyriad.map.Constants.MAP_MAX_Y;
import static info.lliira.illyriad.map.Constants.MAP_MIN_X;
import static info.lliira.illyriad.map.Constants.MAP_MIN_Y;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CandidateFinderTest {

  private static final int GENERATE_COUNT = 35400;

  private Random random = new Random();

  @Test
  public void valid() {
    var plots = List.of(plot(10, 10), plot(18, 10), plot(10, 18), plot(18, 18));
    assertTrue(CandidateFinder.validate(plots));
  }

  @Test
  public void invalid() {
    // too close
    assertFalse(CandidateFinder.validate(List.of(plot(10, 10), plot(17, 10))));
    assertFalse(CandidateFinder.validate(List.of(plot(10, 10), plot(18, 10), plot(6, 10))));
    // too far
    assertFalse(CandidateFinder.validate(List.of(plot(10, 10), plot(20, 10), plot(35, 10))));
  }

  private ValidPlot plot(int x, int y) {
    return new ValidPlot.Builder().x(x).y(y).build();
  }

  public void generateValidPlots() {
    ValidPlotTable validPlotTable = TestHelper.STORAGE_FACTORY.validPlotTable();
    validPlotTable.deleteAll();
    for (int i = 0; i < GENERATE_COUNT; i++) {
      var plot =
          new ValidPlot.Builder()
              .foodSum(random.nextInt(847))
              .totalSum(random.nextInt(3500))
              .x(random.nextInt(MAP_MAX_X - MAP_MIN_X) + MAP_MIN_X)
              .y(random.nextInt(MAP_MAX_Y - MAP_MIN_Y) + MAP_MIN_Y)
              .build();
      validPlotTable.addUpsertBatch(plot);
      if (i % 1000 == 0) validPlotTable.executeUpsertBatch();
    }
    validPlotTable.executeUpsertBatch();
  }
}
