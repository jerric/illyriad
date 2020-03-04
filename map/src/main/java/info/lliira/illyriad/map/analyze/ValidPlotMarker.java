package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.entity.Town;
import info.lliira.illyriad.map.entity.ValidPlot;
import info.lliira.illyriad.map.storage.StorageFactory;
import info.lliira.illyriad.map.storage.TownTable;
import info.lliira.illyriad.map.storage.ValidPlotTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ValidPlotMarker {

  public static void main(String[] args) throws IOException, SQLException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var marker = new ValidPlotMarker(new StorageFactory(properties));
    marker.run();
  }

  private static final int SAFE_DISTANCE = 10;
  private static final Logger LOG = LogManager.getLogger(ValidPlotMarker.class.getSimpleName());
  private final Statement statement;
  private final TownTable townTable;
  private final ValidPlotTable validPlotTable;

  public ValidPlotMarker(StorageFactory storageFactory) throws SQLException {
    this.townTable = storageFactory.townTable();
    this.validPlotTable = storageFactory.validPlotTable();
    this.statement = storageFactory.connection().createStatement();
  }

  public void run() throws SQLException {
    LOG.info("Cleaning up old plots...");
    validPlotTable.deleteAll();
    LOG.info("Loading invalid plots...");
    var invalidPlots = loadInvalidPlots();
    LOG.info("Loading plots and filtering by {} invalid ones...", invalidPlots.size());
    var plots = loadPlots(invalidPlots);
    LOG.info("Saving {} valid plots...", plots.size());
    save(plots);
    LOG.info("Marker done.");
  }

  private Map<Point, ValidPlot> loadPlots(Set<Point> invalidPlots) throws SQLException {
    var plots = new HashMap<Point, ValidPlot>();
    String sql =
        "SELECT x, y, food_sum, total_sum FROM plots WHERE food = 7 AND total= 25 AND sovable = true";
    try (ResultSet resultSet = statement.executeQuery(sql)) {
      while (resultSet.next()) {
        var plot =
            new ValidPlot.Builder()
                .foodSum(resultSet.getInt("food_sum"))
                .resourceSum(resultSet.getInt("total_sum"))
                .x(resultSet.getInt("x"))
                .y(resultSet.getInt("y"))
                .build();
        if (!invalidPlots.contains(plot)) plots.put(plot, plot);
      }
    }
    return plots;
  }

  private Set<Point> loadInvalidPlots() throws SQLException {
    var invalidPlots = new HashSet<Point>();
    var towns = townTable.selectValidTowns();
    while (towns.hasNext()) {
      Town town = towns.next();
      for (int x = town.x - SAFE_DISTANCE; x <= town.x + SAFE_DISTANCE; x++) {
        for (int y = town.y - SAFE_DISTANCE; y <= town.y + SAFE_DISTANCE; y++) {
          invalidPlots.add(new Point(x, y));
        }
      }
    }
    return invalidPlots;
  }

  private void save(Map<Point, ValidPlot> validPlots) {
    validPlots.values().forEach(validPlotTable::addUpsertBatch);
    validPlotTable.executeUpsertBatch();
  }
}
