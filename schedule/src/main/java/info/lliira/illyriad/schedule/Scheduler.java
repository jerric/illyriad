package info.lliira.illyriad.schedule;

import info.lliira.illyriad.common.WaitTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public abstract class Scheduler implements Runnable {
  // max is 10 minutes
  private static final long MAX_WAIT_TIME_SECONDS = TimeUnit.MINUTES.toSeconds(10);
  // min is 1 second
  private static final long MIN_WAIT_TIME_SECONDS = 1L;

  private final Logger log;
  private boolean running;

  protected Scheduler(String name) {
    this.log = LogManager.getLogger(name);
  }

  public void stop() {
    running = false;
  }

  /** Schedule the task, and returns wait time for next task in milliseconds. */
  public abstract WaitTime schedule();

  public abstract String player();

  @Override
  public void run() {
    running = true;
    while (running) {
      try {
        var waitTime = schedule();
        long wait =
            Math.max(MIN_WAIT_TIME_SECONDS, Math.min(MAX_WAIT_TIME_SECONDS, waitTime.seconds()));
        log.info("{} Wait for {}", player(), new WaitTime(wait));
        Thread.sleep(wait * 1000);
      } catch (InterruptedException e) {
        // do nothing.
      } catch (RuntimeException e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}
