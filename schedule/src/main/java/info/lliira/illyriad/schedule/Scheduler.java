package info.lliira.illyriad.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public abstract class Scheduler implements Runnable {
  // max is 10 minutes
  private static final long MAX_WAIT_TIME_MILLIS = TimeUnit.MINUTES.toMillis(10);

  private final Logger log;
  private boolean running;

  protected Scheduler(String name) {
    this.log = LogManager.getLogger(name);
  }

  public void stop() {
    running = false;
  }

  public abstract long schedule();

  @Override
  public void run() {
    running = true;
    while (running) {
      long wait = Math.min(MAX_WAIT_TIME_MILLIS, schedule());
      Duration duration = Duration.ofMillis(wait);
      log.info(
          String.format("Wait for %02d:%02d", duration.toMinutesPart(), duration.toSecondsPart()));
      try {
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        // do nothing.
      }
    }
  }
}
