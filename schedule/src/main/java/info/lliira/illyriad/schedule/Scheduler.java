package info.lliira.illyriad.schedule;

public abstract class Scheduler implements Runnable {
  private boolean running;

  public void stop() {
    running = false;
  }

  public abstract long schedule();

  @Override
  public void run() {
    running = true;
    while (running) {
      long wait = schedule();
      try {
        Thread.sleep(wait);
      } catch (InterruptedException e) {
        // do nothing.
      }
    }
  }
}
