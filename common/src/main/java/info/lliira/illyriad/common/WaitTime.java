package info.lliira.illyriad.common;

import java.time.Duration;

public class WaitTime implements Comparable<WaitTime> {
  private final Duration duration;

  public WaitTime(long seconds) {
    duration = Duration.ofSeconds(Math.max(0, seconds));
  }

  public long millis() {
    return duration.toMillis();
  }

  public WaitTime addSeconds(long seconds) {
    return new WaitTime(duration.toSeconds() + seconds);
  }

  @Override
  public String toString() {
    return String.format(
        "%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }

  @Override
  public int compareTo(WaitTime waitTime) {
    return duration.compareTo(waitTime.duration);
  }
}
