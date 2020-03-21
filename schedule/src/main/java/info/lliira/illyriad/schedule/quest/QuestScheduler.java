package info.lliira.illyriad.schedule.quest;

import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import info.lliira.illyriad.schedule.town.Resource;
import info.lliira.illyriad.schedule.town.TownLoader;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class QuestScheduler extends Scheduler {
  private static final String QUEST_URL = "/World/Quests";
  private static final String ACCEPT_QUEST_URL = "/World/QuestAccept";
  private static final String SEND_TRADE_QUEST_URL = "/Trade/SendTradeQuest";
  private static final long DEFAULT_WAIT_SECONDS = 600L;

  private final AuthenticatedHttpClient.GetHtml questClient;
  private final AuthenticatedHttpClient.PostHtml acceptQuestClient;
  private final AuthenticatedHttpClient.PostHtml sendTradeRequestUrl;
  private final TownLoader townLoader;

  public QuestScheduler(Authenticator authenticator, TownLoader townLoader) {
    super(QuestScheduler.class.getSimpleName());
    this.questClient = new AuthenticatedHttpClient.GetHtml(QUEST_URL, authenticator);
    this.acceptQuestClient = new AuthenticatedHttpClient.PostHtml(ACCEPT_QUEST_URL, authenticator);
    this.sendTradeRequestUrl =
        new AuthenticatedHttpClient.PostHtml(SEND_TRADE_QUEST_URL, authenticator);
    this.townLoader = townLoader;
  }

  @Override
  public long schedule() {
    var questStatus = loadQuest();
    if (questStatus.quest.isPresent()) {
      var quest = questStatus.quest.get();
      if (hasResources(quest)) {
        acceptQuest(quest);
        sendTradeQuest(quest);
      }
    }
    return questStatus.waitTimeSeconds * 1000L;
  }

  private QuestStatus loadQuest() {
    var request = questClient.call(Map.of());
    assert request.output.isPresent();
    var info = request.output.get().select("div.info");
    var form = info.select("form[name=QuestAccept]");
    Optional<Quest> questOptional;
    if (!form.isEmpty()) {
      // quest is not ready or ongoing,
      questOptional = Optional.empty();
    } else {
      int questId = Integer.parseInt(form.select("input[name=QuestID]").val());
      Map<Resource.Type, Integer> resources = parseResourcesNeeded(info.select("table.resources"));
      int ales = parseAlesNeeded(info.select("table.resources ~ br + b"));
      questOptional = Optional.of(new Quest(questId, ales, resources));
    }
    return new QuestStatus(questOptional, DEFAULT_WAIT_SECONDS);
  }

  private Map<Resource.Type, Integer> parseResourcesNeeded(Elements resourceTable) {
    var rows = resourceTable.select("tr");
    var typeRow = rows.get(0).children();
    var countRow = rows.get(1).children();
    var resources = new LinkedHashMap<Resource.Type, Integer>();
    for (int i = 0; i < typeRow.size(); i++) {}

    return resources;
  }

  private int parseAlesNeeded(Elements barrel) {
    return 1;
  }

  private void acceptQuest(Quest quest) {}

  private boolean hasResources(Quest quest) {
    return false;
  }

  private void sendTradeQuest(Quest quest) {}
}
