package info.lliira.illyriad.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WaitTimeTest {

  @Test
  public void testMin() {
    var small = new WaitTime(1000);
    var large = new WaitTime(2000);
    assertEquals(small, small.min(large));
    assertEquals(small, large.min(small));
  }
}
