package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.map.storage.StorageFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static info.lliira.illyriad.map.Constants.MAP_MAX_X;
import static info.lliira.illyriad.map.Constants.MAP_MAX_Y;
import static info.lliira.illyriad.map.Constants.MAP_MIN_X;
import static info.lliira.illyriad.map.Constants.MAP_MIN_Y;

public class ResourceSummarizer {

  public static void main(String[] args) throws IOException, SQLException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var summarizer = new ResourceSummarizer(properties);
    summarizer.run();
  }

  static final String REGION_RADIUS_KEY = "analyze.region.radius";

  private static final Logger LOG = LogManager.getLogger(ResourceSummarizer.class);
  private static final String DEFAULT_REGION_RADIUS = "5";

  private final int regionRadius;
  private final Storage storage;
  private final int minX;
  private final int minY;
  private final int maxX;
  private final int maxY;

  public ResourceSummarizer(Properties properties) throws SQLException {
    this(properties, new StorageFactory(properties));
  }

  public ResourceSummarizer(Properties properties, StorageFactory storageFactory)
      throws SQLException {
    this(properties, new Storage(storageFactory), MAP_MIN_X, MAP_MIN_Y, MAP_MAX_X, MAP_MAX_Y);
  }

  ResourceSummarizer(
      Properties properties, Storage storage, int minX, int minY, int maxX, int maxY) {
    this.storage = storage;
    this.regionRadius =
        Integer.parseInt(properties.getProperty(REGION_RADIUS_KEY, DEFAULT_REGION_RADIUS));
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;
  }

  public void run() throws SQLException {
    var plotMap = new PlotMap();
    for (int x = minX - regionRadius; x <= maxX; x++) {
      if (x % 100 == 0) LOG.info("Summarizing column: {}", x);

      // load the new edge column
      int edgeX = x + regionRadius;
      plotMap.add(edgeX, storage.load(edgeX));
      // sum the new edge into sum and subtract old edge from sum
      var sums = sum(plotMap, x);
      if (x >= minX) storage.save(x, sums);
      // Remove the old edge column since it's no longer needed
      plotMap.remove(x - regionRadius - 1);
    }
    LOG.info("Done summarizer.");
  }

  public Map<Integer, SumData> sum(PlotMap plots, int x) {
    if (x < minX) return Map.of();
    var sums = new LinkedHashMap<Integer, SumData>(maxY - minY + 1);
    for (int y = minY; y <= maxY; y++) {
      sums.put(y, sum(plots, x, y));
    }
    return sums;
  }

  private SumData sum(PlotMap plots, final int x, final int y) {
    SumData sumData = new SumData(0, 0);
    for (int dy = y - regionRadius; dy <= y+regionRadius; dy++) {
      int diff = regionRadius - Math.abs(y - dy);
      for (int dx = x - diff; dx <= x+diff; dx++) {
        sumData.add(plots.get(dx, dy));
      }
    }
    return sumData;
  }

  static class Storage {
    private final PreparedStatement selectStatement;
    private final PreparedStatement updateStatement;

    Storage(StorageFactory storageFactory) throws SQLException {
      var connection = storageFactory.connection();
      this.selectStatement =
          connection.prepareStatement("SELECT y, food, total FROM plots WHERE x = ?");
      this.updateStatement =
          connection.prepareStatement(
              "UPDATE plots SET food_sum = ?, total_sum = ? WHERE x = ? AND y = ?");
    }

    Map<Integer, PlotData> load(int x) throws SQLException {
      var column = new LinkedHashMap<Integer, PlotData>(MAP_MAX_Y - MAP_MIN_Y + 1);
      selectStatement.setInt(1, x);
      try (ResultSet resultSet = selectStatement.executeQuery()) {
        while (resultSet.next()) {
          var plotData = new PlotData(resultSet.getInt("food"), resultSet.getInt("total"));
          column.put(resultSet.getInt("y"), plotData);
        }
      }
      return column;
    }

    int[] save(int x, Map<Integer, SumData> sums) throws SQLException {
      for (int y = MAP_MIN_Y; y <= MAP_MAX_Y; y++) {
        SumData sum = sums.get(y);
        updateStatement.setInt(1, sum.foodSum);
        updateStatement.setInt(2, sum.totalSum);
        updateStatement.setInt(3, x);
        updateStatement.setInt(4, y);
        updateStatement.addBatch();
      }
      return updateStatement.executeBatch();
    }
  }

  static class PlotMap {
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

  static class PlotData {
    private static final PlotData EMPTY = new PlotData(0, 0);
    private final int food;
    private final int total;

    PlotData(int food, int total) {
      this.food = food;
      this.total = total;
    }

    @Override
    public String toString() {
      return String.format("P[%s, %s]", food, total);
    }
  }

  static class SumData {
    private int foodSum;
    private int totalSum;

    SumData(int foodSum, int totalSum) {
      this.foodSum = foodSum;
      this.totalSum = totalSum;
    }

    void add(PlotData plotData) {
      foodSum += plotData.food;
      totalSum += plotData.total;
    }

    @Override
    public String toString() {
      return String.format("S[%s, %s]", foodSum, totalSum);
    }

    public SumData copy() {
      return new SumData(foodSum, totalSum);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof SumData)) return false;
      SumData sumData = (SumData) o;
      return foodSum == sumData.foodSum && totalSum == sumData.totalSum;
    }

    @Override
    public int hashCode() {
      return Objects.hash(foodSum, totalSum);
    }
  }
}
