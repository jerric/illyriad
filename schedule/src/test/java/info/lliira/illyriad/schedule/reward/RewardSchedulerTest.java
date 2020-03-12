package info.lliira.illyriad.schedule.reward;

import info.lliira.illyriad.schedule.TestHelper;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Calendar;

public class RewardSchedulerTest {

  @Test
  public void parseDifference() {
    var scheduler = new RewardScheduler(TestHelper.AUTHENTICATOR);

    Calendar now = Calendar.getInstance();
    now.set(2020,Calendar.JANUARY, 1,0,20,20);
    assertEquals(Duration.ofHours(2), scheduler.parseDifference(8, 20, 20, now));
    assertEquals(Duration.ofHours(2), scheduler.parseDifference(6, 10, 20, now));
  }
}
