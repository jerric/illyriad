package info.lliira.illyriad.schedule.quest;

import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.Scheduler;
import info.lliira.illyriad.schedule.town.Product;
import info.lliira.illyriad.schedule.town.Resource;
import info.lliira.illyriad.schedule.town.TownLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestScheduler extends Scheduler {
  private static final String QUEST_URL = "/World/Quests";
  private static final String ACCEPT_QUEST_URL = "/World/QuestAccept";
  private static final String SEND_TRADE_QUEST_URL = "/Trade/SendTradeQuest";
  private static final WaitTime DEFAULT_WAIT = new WaitTime(600L);
  private static final Pattern BEER_PATTERN = Pattern.compile("(\\d+)\\s+barrel");

  private static final Logger LOG = LogManager.getLogger(QuestScheduler.class.getSimpleName());

  private final Authenticator authenticator;
  private final AuthenticatedHttpClient.GetHtml questClient;
  private final AuthenticatedHttpClient.PostHtml acceptQuestClient;
  private final AuthenticatedHttpClient.PostHtml sendTradeRequestClient;
  private final TownLoader townLoader;

  public QuestScheduler(Authenticator authenticator, TownLoader townLoader) {
    super(QuestScheduler.class.getSimpleName());
    this.authenticator = authenticator;
    this.questClient = new AuthenticatedHttpClient.GetHtml(QUEST_URL, authenticator);
    this.acceptQuestClient = new AuthenticatedHttpClient.PostHtml(ACCEPT_QUEST_URL, authenticator);
    this.sendTradeRequestClient =
        new AuthenticatedHttpClient.PostHtml(SEND_TRADE_QUEST_URL, authenticator);
    this.townLoader = townLoader;
  }

  @Override
  public String player() {
    return authenticator.player();
  }

  @Override
  public WaitTime schedule() {
    var questStatus = loadQuest();
    String logString = String.format("%s quest ", authenticator.player());
    if (questStatus.quest.isPresent()) {
      var quest = questStatus.quest.get();
      if (hasResources(quest)) {
        var fields = acceptQuest(quest);
        sendTradeQuest(quest, fields);
        logString += "accepted";
      } else logString += "lack res";
    } else logString += "unavailable";
    LOG.info("{}, wait:{}", logString, questStatus.waitTime);
    return questStatus.waitTime;
  }

  private QuestStatus loadQuest() {
    var request = questClient.call(Map.of());
    assert request.output.isPresent();
    // TODO: handle multiple questStatus
    var info = request.output.get().select("div.info div#accordion0");
    var form = info.select("form[name=QuestAccept]");
    Optional<Quest> questOptional;
    if (!form.isEmpty()) {
      // quest is not ready or ongoing,
      questOptional = Optional.empty();
    } else {
      int questId = Integer.parseInt(form.select("input[name=QuestID]").val());
      Map<Resource.Type, Integer> resourcesNeeded = new HashMap<>();
      Map<Product.Type, Integer> productsNeeded = new HashMap<>();
      parseResourcesNeeded(info.select("table.resources"), resourcesNeeded, productsNeeded);
      int beer = parseBeerNeeded(info.select("table.resources ~ br + b"));
      questOptional = Optional.of(new Quest(questId, beer, resourcesNeeded, productsNeeded));
    }
    return new QuestStatus(questOptional, DEFAULT_WAIT);
  }

  private void parseResourcesNeeded(
      Elements resourceTable,
      Map<Resource.Type, Integer> resourcesNeeded,
      Map<Product.Type, Integer> productsNeeded) {
    var rows = resourceTable.select("tr");
    var typeRow = rows.get(0).children();
    var amountRow = rows.get(1).children();
    for (int i = 0; i < typeRow.size(); i++) {
      var src = typeRow.get(i).select("img").attr("src");
      var typeName = src.substring(src.lastIndexOf('/') + 1, src.lastIndexOf('.'));
      var amount = Integer.parseInt(amountRow.get(i).text().trim());
      var resourceType = Resource.Type.parse(typeName);
      if (resourceType != Resource.Type.Unknown) {
        resourcesNeeded.put(resourceType, amount);
      } else {
        var productType = Product.Type.parse(typeName);
        if (productType != Product.Type.Unknown) {
          productsNeeded.put(productType, amount);
        } else {
          LOG.warn("Unknown Resource: {}", src);
        }
      }
    }
  }

  private int parseBeerNeeded(Elements barrel) {
    String text = barrel.text().trim();
    Matcher matcher = BEER_PATTERN.matcher(text);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    } else {
      LOG.warn("Unable to parse barrel: {}", text);
      return 1;
    }
  }

  private Map<String, String> acceptQuest(Quest quest) {
    var params = Map.of("QuestId", Integer.toString(quest.id));
    var response = acceptQuestClient.call(params);
    assert response.output.isPresent();
    // TODO: handle multiple quests
    var info = response.output.get().select("div.info div#accordion0");
    var form = info.select("form[name=dispatchGoods]");
    var fields = new LinkedHashMap<String, String>();
    for (var input : form.select("input[name]")) {
      fields.put(input.attr("name"), input.val());
    }
    return fields;
  }

  private boolean hasResources(Quest quest) {
    var town = townLoader.loadTown();
    if (quest.beerNeeded > town.products.get(Product.Type.Beer).amount) return false;
    for (var entry : quest.resourcesNeeded.entrySet()) {
      if (entry.getValue() > town.resources.get(entry.getKey()).current()) return false;
    }
    for (var entry : quest.productsNeeds.entrySet()) {
      int amount = entry.getValue();
      if (entry.getKey() == Product.Type.Beer) amount += quest.beerNeeded;
      if (amount > town.products.get(entry.getKey()).amount) return false;
    }

    return true;
  }

  private void sendTradeQuest(Quest quest, Map<String, String> fields) {
    var response = sendTradeRequestClient.call(fields);
    assert response.output.isPresent();
  }
}
