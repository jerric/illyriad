package info.lliira.illyriad.schedule.quest;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;

public class QuestScheduler extends Scheduler {
  private static final String QUEST_URL = "/World/Quests";
  private static final String ACCEPT_QUEST_URL = "/World/QuestAccept";
  private static final String SEND_TRADE_QUEST_URL = "/Trade/SendTradeQuest";

  private final AuthenticatedHttpClient.GetHtml questClient;
  private final AuthenticatedHttpClient.PostHtml acceptQuestClient;
  private final AuthenticatedHttpClient.PostHtml sendTradeRequestUrl;

  public QuestScheduler(Authenticator authenticator) {
    super(QuestScheduler.class.getSimpleName());
    this.questClient = new AuthenticatedHttpClient.GetHtml(QUEST_URL, authenticator);
    this.acceptQuestClient = new AuthenticatedHttpClient.PostHtml(ACCEPT_QUEST_URL, authenticator);
    this.sendTradeRequestUrl = new AuthenticatedHttpClient.PostHtml(SEND_TRADE_QUEST_URL, authenticator);
  }

  @Override
  public long schedule() {
    return 0;
  }

  private QuestStatus loadQuest() {
    return null;
  }

  private void acceptQuest(Quest quest) {

  }

  private void sendTradeQuest(Quest quest) {

  }
}
