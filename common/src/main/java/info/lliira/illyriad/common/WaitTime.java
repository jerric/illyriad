package info.lliira.illyriad.common;

import java.time.Duration;

public class WaitTime {
  private final Duration duration;

  public WaitTime(long seconds) {
    duration = Duration.ofSeconds(Math.max(0, seconds));
  }

  public boolean expired() {
    return duration.toSeconds() <= 0;
  }

  public long seconds() {
    return duration.toSeconds();
  }

  public WaitTime addSeconds(long seconds) {
    return new WaitTime(duration.toSeconds() + seconds);
  }

  public WaitTime min(WaitTime waitTime) {
    return (duration.compareTo(waitTime.duration) <= 0) ? this : waitTime;
  }

  @Override
  public String toString() {
    return String.format(
        "%02d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
  }
}
