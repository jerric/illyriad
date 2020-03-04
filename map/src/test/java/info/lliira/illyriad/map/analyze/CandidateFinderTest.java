package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.map.entity.ValidPlot;
import info.lliira.illyriad.map.storage.StorageFactory;
import info.lliira.illyriad.map.storage.ValidPlotTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

import static info.lliira.illyriad.common.Constants.MAP_MAX_X;
import static info.lliira.illyriad.common.Constants.MAP_MAX_Y;
import static info.lliira.illyriad.common.Constants.MAP_MIN_X;
import static info.lliira.illyriad.common.Constants.MAP_MIN_Y;

public class CandidateFinderTest {

  private static final int GENERATE_COUNT = 35400;

  private StorageFactory storageFactory;
  private Random random;

  @BeforeEach
  public void setUp() throws IOException, SQLException {
    random = new Random();
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    storageFactory = new StorageFactory(properties);
  }

  @Test
  public void generateValidPlots() {
    ValidPlotTable validPlotTable = storageFactory.validPlotTable();
    validPlotTable.deleteAll();
    for (int i = 0; i < GENERATE_COUNT; i++) {
      var plot =
          new ValidPlot.Builder()
              .foodSum(random.nextInt(847))
              .resourceSum(random.nextInt(3500))
              .x(random.nextInt(MAP_MAX_X - MAP_MIN_X) + MAP_MIN_X)
              .y(random.nextInt(MAP_MAX_Y - MAP_MIN_Y) + MAP_MIN_Y)
          .build();
      validPlotTable.addUpsertBatch(plot);
      if(i % 1000 == 0) validPlotTable.executeUpsertBatch();
    }
    validPlotTable.executeUpsertBatch();
  }
}
