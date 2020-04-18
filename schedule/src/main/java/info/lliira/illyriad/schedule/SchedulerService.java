package info.lliira.illyriad.schedule;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.AuthenticatorManager;
import info.lliira.illyriad.schedule.reward.RewardScheduler;
import info.lliira.illyriad.schedule.town.TownScheduler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SchedulerService {
  public static void main(String[] args) throws IOException {
    new SchedulerService().run();
  }

  private final Properties properties;
  private final AuthenticatorManager authenticatorManager;

  public SchedulerService() throws IOException {
    this.properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    this.authenticatorManager = new AuthenticatorManager(properties);
  }

  public void run() {
    var schedulers = new ArrayList<Scheduler>();
    for (var authenticator : authenticatorManager.all()) {
      schedulers.add(new TownScheduler(authenticator));
      schedulers.add(new RewardScheduler(authenticator));
    }
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
