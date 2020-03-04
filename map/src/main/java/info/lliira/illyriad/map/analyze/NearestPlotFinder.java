package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.map.entity.ValidPlot;
import info.lliira.illyriad.map.storage.StorageFactory;
import info.lliira.illyriad.map.storage.ValidPlotTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public class NearestPlotFinder {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var finder = new NearestPlotFinder(new StorageFactory(properties));
    var plot = finder.find(490, 59);
    LOG.info("Found nearest: {}", plot);
  }

  private static final Logger LOG = LogManager.getLogger(NearestPlotFinder.class.getSimpleName());

  private final ValidPlotTable validPlotTable;

  public NearestPlotFinder(StorageFactory storageFactory) {
    this.validPlotTable = storageFactory.validPlotTable();
  }

  public ValidPlot find(int x, int y) {
    var plots = validPlotTable.selectAll();
    var vectors = new ArrayList<Vector>();
    while(plots.hasNext()) {
      vectors.add(vector(plots.next(), x, y));
    }
    Collections.sort(vectors);
    return vectors.get(0).plot;
  }

  private Vector vector(ValidPlot plot, int x, int y) {
    int dx = plot.x - x;
    int dy = plot.y - y;
    return new Vector(plot, dx * dx + dy * dy);
  }

  private static class Vector implements Comparable<Vector> {
    private final ValidPlot plot;
    private final int distanceSq;
    public Vector(ValidPlot plot, int distanceSq) {
      this.plot = plot;
      this.distanceSq = distanceSq;
    }

    @Override
    public int compareTo(Vector vector) {
      return distanceSq - vector.distanceSq;
    }
  }
}
