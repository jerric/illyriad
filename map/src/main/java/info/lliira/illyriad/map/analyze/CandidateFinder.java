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
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class CandidateFinder {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var finder = new CandidateFinder(new StorageFactory(properties));
    finder.run();
  }

  private static final int CANDIDATE_COUNT = 8;
  private static final int RESOURCE_WEIGHT = 1;
  private static final int FOOD_WEIGHT = 2;

  private static final Logger LOG = LogManager.getLogger(CandidateFinder.class.getSimpleName());

  private final ValidPlotTable validPlotTable;

  public CandidateFinder(StorageFactory storageFactory) {
    this.validPlotTable = storageFactory.validPlotTable();
  }

  public void run() {
    LOG.info("Loading valid plots...");
    var plots = loadPlots();
    var solutions = new ArrayList<Solution>();
    int count = 0;
    for (var plot : plots) {
      if (count % 1000 == 0) LOG.info("Progress {}/{}", count, plots.size());
      var subSolutions = findSolutions(plot, plots);
      var subBest = bestSolution(subSolutions);
      solutions.add(subBest);
      count++;
    }
    var best = bestSolution(solutions);
    LOG.info("Found best: {}", best);
  }

  private List<ValidPlot> loadPlots() {
    var plots = new ArrayList<ValidPlot>();
    var iterator = validPlotTable.selectAll();
    while (iterator.hasNext()) plots.add(iterator.next());
    return plots;
  }

  private List<Solution> findSolutions(ValidPlot plot, List<ValidPlot> allPlots) {
    var solutions = new ArrayList<Solution>();
    solutions.add(new Solution().add(plot));
    // initialize the base pool. The rest of the candidate will only be chosen from this pool
    var basePool = allPlots.stream().filter(plot::inRange).collect(Collectors.toList());

    // Choose the [2..CANDIDATE_COUNT] adjacent plots
    for (int i = 2; i <= CANDIDATE_COUNT; i++) {
      int size = solutions.size();
      for (int j = 0; j < size; j++) {
        Solution solution = solutions.get(j);
        var pool = basePool.stream().filter(solution::minRange).collect(Collectors.toList());
        for (var candidate : pool) {
          solutions.add(solution.copy().add(candidate));
        }
      }
    }
    return solutions;
  }

  private Solution bestSolution(List<Solution> solutions) {
    Solution best = null;
    for (Solution solution : solutions) {
      if (best == null || solution.score() > best.score()) best = solution;
    }
    return best;
  }

  private static class Solution {
    private final List<ValidPlot> plots = new ArrayList<>();
    private int foodSum = 0;
    private int totalSum = 0;

    Solution add(ValidPlot plot) {
      plots.add(plot);
      foodSum += plot.foodSum;
      totalSum += plot.resourceSum;
      return this;
    }

    int score() {
      return (totalSum - foodSum) / 4 * RESOURCE_WEIGHT + foodSum * FOOD_WEIGHT;
    }

    boolean minRange(ValidPlot candidate) {
      for (ValidPlot plot : plots) {
        if (!plot.minRange(candidate)) return false;
      }
      return true;
    }

    Solution copy() {
      var solution = new Solution();
      solution.plots.addAll(plots);
      solution.foodSum = foodSum;
      solution.totalSum = totalSum;
      return solution;
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder("Solution[food=");
      builder.append(foodSum).append(", total=").append(totalSum).append("]\n");
      for (var plot : plots) {
        builder.append("\t").append(plot).append("\n");
      }
      return builder.toString();
    }
  }
}
