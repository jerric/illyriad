package info.lliira.illyriad.map.analyze;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static info.lliira.illyriad.map.analyze.ResourceSummarizer.REGION_RADIUS_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResourceSummarizerTest {

  private static final int MIN_X = -3;
  private static final int MAX_X = 3;
  private static final int MIN_Y = -4;
  private static final int MAX_Y = 3;

  private static final int[][] DATA = {
    {1, 3, 3, 2, 1, 8, 5},
    {5, 9, 3, 4, 0, 8, 4},
    {6, 3, 3, 3, 9, 5, 5},
    {2, 1, 1, 4, 7, 3, 6},
    {0, 6, 0, 4, 4, 9, 8},
    {2, 6, 0, 6, 1, 7, 3},
    {0, 2, 6, 3, 9, 9, 1},
    {2, 1, 5, 6, 8, 0, 2}
  };

  private static final int[][] SUMS = {
    {1, 4, 7, 8, 6, 11, 14, 13, 5},
    {6, 18, 24, 24, 13, 23, 26, 25, 9},
    {12, 27, 36, 33, 28, 40, 45, 35, 14},
    {13, 26, 33, 31, 34, 43, 47, 31, 15},
    {8, 18, 22, 25, 35, 48, 56, 36, 19},
    {4, 17, 18, 28, 27, 45, 48, 36, 17},
    {2, 16, 22, 33, 33, 52, 51, 37, 12},
    {4, 13, 24, 35, 44, 49, 40, 22, 6},
    {2, 5, 16, 23, 37, 35, 29, 12, 3},
    {2, 3, 8, 12, 19, 14, 10, 2, 2}
  };

  @Captor private ArgumentCaptor<Integer> xyCaptor;

  @Captor private ArgumentCaptor<Map<Integer, ResourceSummarizer.SumData>> sumDataCaptor;

  private ResourceSummarizer.Storage storage = mock(ResourceSummarizer.Storage.class);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private Map<Integer, ResourceSummarizer.PlotData> load(int x) {
    var column = new LinkedHashMap<Integer, ResourceSummarizer.PlotData>();
    if (x > MAX_X) return column;
    for (int y = MIN_Y; y <= MAX_Y; y++) {
      int dx = x - MIN_X;
      int dy = y - MIN_Y;
      column.put(y, new ResourceSummarizer.PlotData(DATA[dy][dx], DATA[dy][dx]));
    }
    return column;
  }

  private Map<Integer, ResourceSummarizer.SumData> sum(int x) {
    var column = new LinkedHashMap<Integer, ResourceSummarizer.SumData>();
    for (int y = MIN_Y - 1; y <= MAX_Y + 1; y++) {
      int dx = x - (MIN_X - 1);
      int dy = y - (MIN_Y - 1);
      column.put(y, new ResourceSummarizer.SumData(SUMS[dy][dx], SUMS[dy][dx]));
    }
    return column;
  }

  @Test
  public void summarize() throws SQLException {
    when(storage.load(anyInt())).then(invocation -> load(invocation.getArgument(0)));
    // copy the content, before it's modified in the next iteration.
    Map<Integer, Map<Integer, ResourceSummarizer.SumData>> sums = new HashMap<>();
    when(storage.save(anyInt(), any(Map.class)))
        .then(
            invocation -> {
              var yMap = new LinkedHashMap<>((Map<Integer, ResourceSummarizer.SumData>) invocation.getArgument(1));
              for (var entry : yMap.entrySet()) {
                entry.setValue(entry.getValue().copy());
              }
              sums.put(invocation.getArgument(0), yMap);
              return new int[0];
              });

    Properties properties = new Properties();
    properties.setProperty(REGION_RADIUS_KEY, "1");
    var summarizer = new ResourceSummarizer(properties, storage, MIN_X, MIN_Y, MAX_X, MAX_Y);
    summarizer.run();
    verify(storage, times(7)).save(xyCaptor.capture(), sumDataCaptor.capture());
    var expectedXs = List.of(-3, -2, -1, 0, 1, 2, 3);
    assertEquals(expectedXs, xyCaptor.getAllValues());
    assertEquals(expectedXs.size(), sums.size());
    for (int x : expectedXs) {
      assertEquals(sum(x), sums.get(x), "x=" + x);
    }
  }

  public void print() {
    Random random = new Random();
    System.out.println("{");
    for (int y = MIN_Y; y <= MAX_Y; y++) {
      System.out.print("  {");
      for (int x = MIN_X; x <= MAX_X; x++) {
        if (x > 0) System.out.print(", ");
        System.out.print(random.nextInt(10));
      }
      System.out.print("}");
      if (y < MAX_Y) System.out.print(",");
      System.out.println();
    }
    System.out.println("};");
  }
}
