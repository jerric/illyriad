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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ResourceSummarizer {

  private static final Logger LOG = LogManager.getLogger(ResourceSummarizer.class);
  private static final String REGION_RADIUS_KEY = "analyze.region.radius";
  private static final String DEFAULT_REGION_RADIUS = "10";

  private final int regionRadius;
  private final PreparedStatement selectStatement;
  private final PreparedStatement updateStatement;

  ResourceSummarizer(Properties properties, StorageFactory storageFactory) throws SQLException {
    this.regionRadius =
        Integer.parseInt(properties.getProperty(REGION_RADIUS_KEY, DEFAULT_REGION_RADIUS));
    var connection = storageFactory.connection();
    this.selectStatement =
        connection.prepareStatement("SELECT x, y, food, total FROM plots WHERE x = ?");
    this.updateStatement =
        connection.prepareStatement(
            "UPDATE plots SET food_sum = ?, total_sum = ? WHERE x = ? AND y = ?");
  }

  void run() throws SQLException {
    var dataMap = new PlotMap();
    var sumMap = new PlotMap();
    for (int x = Constants.MAP_MIN_X - regionRadius; x < Constants.MAP_MAX_X; x++) {
      // load the new column
      int edgeX = x + regionRadius;
      dataMap.add(x, load(x));


    }
  }

  private Map<Integer, PlotData> load(int x) {

  }

  private static class PlotMap {
    private final Map<Integer, Map<Integer, PlotData>> xMap = new LinkedHashMap<>();

    PlotData get(int x, int y) {
      return xMap.getOrDefault(x, Map.of()).getOrDefault(y, PlotData.EMPTY);
    }

    void add(int x, Map<Integer, PlotData> yMap) {
      xMap.put(x, yMap);
    }

    void remove(int x) {
      xMap.remove(x);
    }
  }

  private static class PlotData {
    private static final PlotData EMPTY = new PlotData(0, 0);
    private final int food;
    private final int total;

    private PlotData(int food, int total) {
      this.food = food;
      this.total = total;
    }
  }
}
