package info.lliira.illyriad.schedule.reward;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class RewardScheduler extends Scheduler {

  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var scheduler = new RewardScheduler(authenticator);
    scheduler.run();
  }

  private static final String REWARD_URL = "/World/DailyReward";

  private static final Logger LOG = LogManager.getLogger(RewardScheduler.class.getSimpleName());

  private final AuthenticatedHttpClient.GetHtml queryClient;
  private final AuthenticatedHttpClient.PostHtml claimClient;

  public RewardScheduler(Authenticator authenticator) {
    super(RewardScheduler.class.getSimpleName());
    this.queryClient = new AuthenticatedHttpClient.GetHtml(REWARD_URL, authenticator);
    this.claimClient = new AuthenticatedHttpClient.PostHtml(REWARD_URL, authenticator);
  }

  @Override
  public long schedule() {
    LOG.info("=============== Scheduling Reward ===============");
    var response = queryClient.call(Map.of());
    assert response.output.isPresent();
    var reward = parsePrestigeReward(response.output.get());
    Duration waitDuration;
    if (reward.hasReward) {
      LOG.info("Claiming reward...");
      claimClient.call(reward.prestigeFields);
      waitDuration = Duration.ofDays(1);
    } else {
      waitDuration = reward.waitTime;
      LOG.info(
          String.format(
              "Reward will be ready in %02d:%02d:%02d",
              waitDuration.toHours(), waitDuration.toMinutesPart(), waitDuration.toSecondsPart()));
    }
    return waitDuration.toMillis();
  }

  private Reward parsePrestigeReward(Document document) {
    var form = document.select("tr.middle form").get(0);
    var fields = new HashMap<String, String>();
    var hasReward = true;
    var waitDuration = Duration.ofMillis(0);
    for (var input : form.select("input")) {
      String name = input.attr("name");
      if (!name.isBlank()) fields.put(name, input.val());
      if (input.attr("type").equals("submit") && input.attr("disabled").equals("disabled")) {
        hasReward = false;
        waitDuration = parseWaitDuration(document);
      }
    }
    return new Reward(hasReward, fields, waitDuration);
  }

  private Duration parseWaitDuration(Document document) {
    var parts = document.select("div").get(1).text().trim().split("[\\s:.]");
    int hours = Integer.parseInt(parts[parts.length - 3]);
    int minutes = Integer.parseInt(parts[parts.length - 2]);
    int seconds = Integer.parseInt(parts[parts.length - 1]);
    return parseDifference(hours, minutes, seconds, Calendar.getInstance());
  }

  Duration parseDifference(int hours, int minutes, int seconds, Calendar now) {
    int nowHours = now.get(Calendar.HOUR_OF_DAY);
    int nowMinutes = now.get(Calendar.MINUTE);
    int nowSeconds = now.get(Calendar.SECOND);
    long diff =
        (hours - (nowHours + 6) + 24) % 24 * 3600L
            + (minutes - nowMinutes) * 60
            + (seconds - nowSeconds);
    if (diff < 0) diff += TimeUnit.DAYS.toSeconds(1);
    return Duration.ofSeconds(diff);
  }

  private static class Reward {
    private final boolean hasReward;
    private final Map<String, String> prestigeFields;
    private final Duration waitTime;

    private Reward(boolean hasReward, Map<String, String> prestigeFields, Duration waitTime) {
      this.hasReward = hasReward;
      this.prestigeFields = prestigeFields;
      this.waitTime = waitTime;
    }
  }
}
