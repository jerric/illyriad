package info.lliira.illyriad.map.analyze;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.math.CombinatorialIterator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
  private static final int SOLUTION_COUNT = 10;
  private static final int RESOURCE_WEIGHT = 10;
  private static final int FOOD_WEIGHT = 20;
  private static final int TOP_K_PCT = 15;

  private static final Logger LOG = LogManager.getLogger(CandidateFinder.class.getSimpleName());

  static boolean validate(List<ValidPlot> plots) {
    return validate(plots, 0);
  }

  private static boolean validate(List<ValidPlot> plots, int start) {
    if (start == plots.size() - 1) return true;
    for (int i = start; i < plots.size() - 1; i++) {
      var plot = plots.get(start);
      for (int j = start + 1; j < plots.size(); j++) {
        if (!plot.inRange(plots.get(j)) || !validate(plots, j)) return false;
      }
    }
    return true;
  }

  private final ValidPlotTable validPlotTable;

  public CandidateFinder(StorageFactory storageFactory) {
    this.validPlotTable = storageFactory.validPlotTable();
  }

  public void run() {
    LOG.info("Loading valid plots...");
    var plots = loadPlots();
    LOG.info("Filtering {} plots...", plots.size());
    plots = filterTopK(plots);
    var solutions = new HashSet<Solution>();
    LOG.info("Iterating on {} filtered plots..", plots.size());
    for (int i = 0; i < plots.size(); i++) {
      var plot = plots.get(i);
      var candidatePool = plots.stream().filter(plot::inRange).collect(Collectors.toList());
      if (i % 10 == 0) LOG.info("Progress: {}/{}", i, plots.size());
      if (candidatePool.size() >= CANDIDATE_COUNT - 1) {
        var solution = findSolution(plot, candidatePool);
        if (solution.isPresent()) {
          LOG.info("{}/{} Found solution at Center:{}", i, plots.size(), plot);
          solutions.add(solution.get());
        }
      }
    }
    LOG.info("Best solutions: ");
    List<Solution> bests = new ArrayList<>(solutions);
    Collections.sort(bests);
    int limit = Math.min(bests.size(), SOLUTION_COUNT);
    for (int i = 0; i < limit; i++) {
      LOG.info("{}", bests.get(i));
    }
  }

  private List<ValidPlot> loadPlots() {
    var plots = new ArrayList<ValidPlot>();
    var iterator = validPlotTable.selectAll();
    while (iterator.hasNext()) {
      var plot = iterator.next();
      // if (plot.x >= -500 && plot.y >= -1000)
        plots.add(plot);
    }
    return plots;
  }

  private List<ValidPlot> filterTopK(List<ValidPlot> plots) {
    int limit = plots.size() * TOP_K_PCT / 100;
    // sort by total
    plots.sort((l, r) -> r.totalSum - l.totalSum);
    int totalThreshold = plots.get(limit).totalSum;

    // sort by food
    plots.sort((l, r) -> r.foodSum - l.foodSum);
    int foodThreshold = plots.get(limit).foodSum;

    var filtered = new ArrayList<ValidPlot>(limit);
    for (var plot : plots) {
      if (plot.foodSum >= foodThreshold) {
        if (plot.totalSum >= totalThreshold) filtered.add(plot);
      } else break;
    }
    return filtered;
  }

  private Optional<Solution> findSolution(ValidPlot center, List<ValidPlot> candidatePool) {
    var combinator = new CombinatorialIterator<>(CANDIDATE_COUNT - 1, candidatePool);
    Solution best = null;
    for (var candidates : combinator) {
      if (validate(candidates)) {
        var solution = new Solution(candidates).add(center);
        if (best == null || best.score() < solution.score()) best = solution;
      }
    }
    return Optional.ofNullable(best);
  }

  private static class Solution implements Comparable<Solution> {
    private final List<ValidPlot> plots = new ArrayList<>();
    private int foodSum = 0;
    private int totalSum = 0;

    Solution(List<ValidPlot> plots) {
      plots.forEach(this::add);
    }

    Solution add(ValidPlot plot) {
      this.plots.add(plot);
      foodSum += plot.foodSum;
      totalSum += plot.totalSum;
      Collections.sort(plots);
      return this;
    }

    int score() {
      return (totalSum - foodSum) / 4 * RESOURCE_WEIGHT + foodSum * FOOD_WEIGHT;
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

    @Override
    public int compareTo(Solution o) {
      return o.score() - score();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Solution)) return false;
      Solution solution = (Solution) o;
      if (foodSum != solution.foodSum
          || totalSum != solution.totalSum
          || plots.size() != solution.plots.size()) return false;
      for (int i = 0; i < plots.size(); i++) {
        if (!plots.get(i).equals(solution.plots.get(i))) return false;
      }
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(plots, foodSum, totalSum);
    }
  }
}
