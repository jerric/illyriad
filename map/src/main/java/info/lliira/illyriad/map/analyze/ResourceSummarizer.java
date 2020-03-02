package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.map.entity.ValidPlot;
import info.lliira.illyriad.map.storage.StorageFactory;
import info.lliira.illyriad.map.storage.ValidPlotTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ResourceSummarizer {

  private static class PlotData {
    private final int food;
    private final int total;
    private final boolean sovable;

    private PlotData(int food, int total, boolean sovable) {
      this.food = food;
      this.total = total;
      this.sovable = sovable;
    }
  }

  private static final Logger LOG = LogManager.getLogger(ResourceSummarizer.class);
  private static final String REGION_RADIUS_KEY = "analyze.region.radius";
  private static final String DEFAULT_REGION_RADIUS = "10";

  private final int regionRadius;
  private final ValidPlotTable validPlotTable;
  private final PreparedStatement selectPlotDataStatement;
  private final PreparedStatement selectValidPlotStatement;

  ResourceSummarizer(Properties properties, StorageFactory storageFactory) throws SQLException {
    this.regionRadius =
        Integer.parseInt(properties.getProperty(REGION_RADIUS_KEY, DEFAULT_REGION_RADIUS));
    this.validPlotTable = storageFactory.validPlotTable();

    Connection connection = storageFactory.connection();
    this.selectPlotDataStatement =
        connection.prepareStatement(
            "SELECT x, y, food, total, sovable FROM plots WHERE y >= ? AND y <= ?");
    this.selectValidPlotStatement =
        connection.prepareStatement("SELECT x FROM valid_plots WHERE y = ?");
  }

  void run() throws SQLException {
    Map<Integer, Map<Integer, PlotData>> plotData = new HashMap<>();
    // load the initial region
    int minY = Constants.MAP_MIN_Y - regionRadius;
    int maxY = Constants.MAP_MIN_Y + regionRadius;
    loadPlotData(plotData, minY, maxY);

    int currentY = Constants.MAP_MIN_Y;
    while (currentY <= Constants.MAP_MAX_Y) {
      LOG.info("Summarizing row {}", currentY);
      summarizeRow(plotData, currentY);
      // The top row will not needed anymore.
      plotData.remove(minY);
      minY++;
      maxY++;
      currentY++;
      // Only need to load one extra row.
      loadPlotData(plotData, maxY, maxY);
    }
    LOG.info("Summarization finished.");
  }

  private void loadPlotData(Map<Integer, Map<Integer, PlotData>> plotData, int minY, int maxY)
      throws SQLException {
    for (int y = minY; y <= maxY; y++) {
      if (!plotData.containsKey(y)) plotData.put(y, new HashMap<>());
    }
    if (minY > Constants.MAP_MAX_Y) return;

    selectPlotDataStatement.setInt(1, minY);
    selectPlotDataStatement.setInt(2, maxY);
    try (ResultSet resultSet = selectPlotDataStatement.executeQuery()) {
      while (resultSet.next()) {
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        PlotData data =
            new PlotData(
                resultSet.getInt("food"),
                resultSet.getInt("total"),
                resultSet.getBoolean("sovable"));
        plotData.get(y).put(x, data);
      }
    }
  }

  private void summarizeRow(Map<Integer, Map<Integer, PlotData>> plotData, int y)
      throws SQLException {
    var validPlots = getValidPlots(y);
    for (int x = Constants.MAP_MIN_X; x <= Constants.MAP_MAX_X; x++) {
      if (!validPlots.contains(x)) continue;
      var validPlot = summarizePlot(plotData, x, y);
      validPlotTable.addUpsertBatch(validPlot);
    }
    validPlotTable.executeUpsertBatch();
  }

  private Set<Integer> getValidPlots(int y) throws SQLException {
    var validPlots = new HashSet<Integer>();
    selectValidPlotStatement.setInt(1, y);
    try (var resultSet = selectValidPlotStatement.executeQuery()) {
      while (resultSet.next()) {
        validPlots.add(resultSet.getInt("x"));
      }
    }
    return validPlots;
  }

  private ValidPlot summarizePlot(
      Map<Integer, Map<Integer, PlotData>> plotData, int centerX, int centerY) {
    int foodSum = 0;
    int totalSum = 0;
    int sovableCount = 0;
    for (int y = centerY - regionRadius; y <= centerY + regionRadius; y++) {
      var row = plotData.get(y);
      if (row.isEmpty()) continue;
      for (int x = centerX - regionRadius; x <= centerX + regionRadius; x++) {
        var data = row.get(x);
        if (data != null) {
          foodSum += data.food;
          totalSum += data.total;
          sovableCount += data.sovable ? 1 : 0;
        }
      }
    }
    return new ValidPlot.Builder()
        .foodSum(foodSum)
        .resourceSum(totalSum)
        .sovereignCount(sovableCount)
        .build();
  }
}
