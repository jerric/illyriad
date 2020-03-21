package info.lliira.illyriad.schedule;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.building.BuildingScheduler;
import info.lliira.illyriad.schedule.reward.RewardScheduler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SchedulerService {
  public static void main(String[] args) throws IOException {
    new SchedulerService().run();
  }

  private final List<Scheduler> schedulers;

  public SchedulerService() throws IOException {
    var properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    schedulers =
        List.of(
            new BuildingScheduler(authenticator, properties), new RewardScheduler(authenticator));
  }

  public void run() {
    ExecutorService pool = Executors.newFixedThreadPool(schedulers.size());
    for (var scheduler : schedulers) {
      pool.execute(scheduler);
    }
    while (true) {
      try {
        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
      } catch (InterruptedException e) {
        // do nothing
      }
    }
  }
}
