package info.lliira.illyriad.schedule;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.building.BuildingScheduler;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class SchedulerService {
  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);

    var scheduler = new BuildingScheduler(authenticator);
    scheduler.run();
  }

  private SchedulerService(){};
}
