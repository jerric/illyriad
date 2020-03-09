package info.lliira.illyriad.schedule.reward;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Map;

public class RewardScheduler extends Scheduler {
  private static final String REWARD_URL = "/World/DailyReward";

  private static final Logger LOG = LogManager.getLogger(RewardScheduler.class.getSimpleName());

  private final AuthenticatedHttpClient.GetHtml queryClient;
  private final AuthenticatedHttpClient.PostHtml claimClient;

  public RewardScheduler(Authenticator authenticator) {
    this.queryClient = new AuthenticatedHttpClient.GetHtml(REWARD_URL, authenticator);
    this.claimClient = new AuthenticatedHttpClient.PostHtml(REWARD_URL, authenticator);
  }

 @Override
  public long schedule() {
    LOG.info("=============== Checking Reward Page ===============");
    var response = queryClient.call(Map.of());
    assert response.output.isPresent();

    return 0;
  }

  private static class Reward {
    private final boolean hasReward;
    private final Map<String, String> prestigeField;
    private final Duration waitTime;

    private Reward(boolean hasReward, Map<String, String> prestigeField, Duration waitTime) {
      this.hasReward = hasReward;
      this.prestigeField = prestigeField;
      this.waitTime = waitTime;
    }
  }
}
