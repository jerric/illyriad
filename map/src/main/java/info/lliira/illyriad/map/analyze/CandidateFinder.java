package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.map.entity.ValidPlot;
import info.lliira.illyriad.map.storage.StorageFactory;
import info.lliira.illyriad.map.storage.ValidPlotTable;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

public class CandidateFinder {
  private static final int MIN_DISTANCE = 8;
  private static final int MAX_DISTANCE = 20;

  public static void main(String[] args) throws IOException, SQLException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var finder = new CandidateFinder(new StorageFactory(properties));
    finder.run();
  }

  private final ValidPlotTable validPlotTable;

  public CandidateFinder(StorageFactory storageFactory) {
    this.validPlotTable = storageFactory.validPlotTable();
  }

  public void run() {
    List<ValidPlot> plots = loadPlots();
  }

  private List<ValidPlot> loadPlots() {
    var plots = new ArrayList<ValidPlot>();
    var iterator = validPlotTable.selectAll();
    while(iterator.hasNext()) plots.add(iterator.next());
    return plots;
  }


  private static class Solution {
    private final Stack<ValidPlot> stack;
    private int foodSum;
    private int totalSum;
  }
}
