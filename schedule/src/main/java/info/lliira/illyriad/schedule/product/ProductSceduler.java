package info.lliira.illyriad.schedule.product;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.schedule.building.Building;
import info.lliira.illyriad.schedule.town.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ProductSceduler {
  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new Authenticator(properties);
    var scheduler = new ProductSceduler(authenticator);
    scheduler.schedule();
  }

  private static final String PRODUCT_LIST_URL = "/Town/Production";
  private static final String SCHEDULE_PRODUCT_URL = "/Town/QueueResourceBuild";
  private static final WaitTime DEFAULT_WAIT = new WaitTime(TimeUnit.MINUTES.toSeconds(10));
  private static final String QUANTITY_FIELD = "Quantity";
  private static final int MIN_CAN_BUILD = 10;

  private static final Logger LOG = LogManager.getLogger(ProductSceduler.class.getSimpleName());

  private final Authenticator authenticator;
  private final AuthenticatedHttpClient.GetHtml productListClient;
  private final AuthenticatedHttpClient.PostJson<ScheduleResponse> scheduleProductClient;

  public ProductSceduler(Authenticator authenticator) {
    this.authenticator = authenticator;
    this.productListClient = new AuthenticatedHttpClient.GetHtml(PRODUCT_LIST_URL, authenticator);
    this.scheduleProductClient =
        new AuthenticatedHttpClient.PostJson<>(
            SCHEDULE_PRODUCT_URL, ScheduleResponse.class, Map.of(), authenticator);
  }

  public WaitTime schedule() {
    LOG.info("=============== Scheduling Products ===============");
    var progresses = parseProgresses();
    LOG.info("Total {} products.", progresses.size());
    WaitTime minWait = null;
    for (var progress : progresses) {
      var waitTime = progress.waitTime;
      if (waitTime.millis() == 0) {
        var requirement = parseRequirement(progress);
        if (requirement.satisfied()) {
          LOG.info("Scheduling product in {}: {}", progress.type, requirement.type);
          waitTime = scheduleProduct(progress, requirement);
        } else {
          LOG.info("Not enough resources for product {}", progress.type);
          waitTime = DEFAULT_WAIT;
        }
      }
      if (minWait == null || minWait.compareTo(waitTime) > 0) minWait = waitTime;
    }
    LOG.info("Next production starts in: {}", minWait);
    return minWait;
  }

  private Collection<Progress> parseProgresses() {
    var response = productListClient.call(Map.of());
    assert response.output.isPresent();
    Map<Building.Type, Progress> progresses = new HashMap<>();
    for (var fieldSet : response.output.get().select("fieldset")) {
      var link = fieldSet.select("legend span a");
      var buildingType = Building.Type.parse(link.text().trim());
      // skip the leading #
      var url = link.attr("href").substring(1);

      WaitTime waitTime;
      var table = fieldSet.select("table");
      if (table.isEmpty()) {
        waitTime = new WaitTime(0);
      } else {
        long timestamp = Long.parseLong(table.select("span.progTime").attr("data").split("\\|")[1]);
        waitTime =
            new WaitTime(Math.round(Math.ceil((timestamp - System.currentTimeMillis()) / 1000D)));
      }
      var existing = progresses.get(buildingType);
      if (existing == null || existing.waitTime.compareTo(waitTime) < 0) {
        progresses.put(buildingType, new Progress(buildingType, url, waitTime));
      }
    }
    return progresses.values();
  }

  private Requirement parseRequirement(Progress progress) {
    AuthenticatedHttpClient.GetHtml requirementClient =
        new AuthenticatedHttpClient.GetHtml(progress.url, authenticator);
    var response = requirementClient.call(Map.of());
    assert response.output.isPresent();
    var document = response.output.get();
    var stored = parseStored(document.select("table.info tr"));

    var rows = document.select("div.info table tr");
    Requirement minRequirment = null;
    // skip the title row
    for (int i = 1; i < rows.size(); i++) {
      var requirement = parseRequirement(rows.get(i), stored);
      if (minRequirment == null || minRequirment.amount > requirement.amount) {
        minRequirment = requirement;
      }
    }
    return minRequirment;
  }

  private Map<Product.Type, Integer> parseStored(Elements rows) {
    Map<Product.Type, Integer> stored = new HashMap<>();
    for (int i = 1; i < rows.size(); i++) {
      var row = rows.get(i);
      var type = Product.Type.parse(row.child(1).text());
      var amount = Integer.parseInt(row.child(2).text());
      stored.put(type, amount);
    }
    return stored;
  }

  private Requirement parseRequirement(Element row, Map<Product.Type, Integer> stored) {
    var productType = Product.Type.parse(row.child(1).text());

    Map<Product.Type, Integer> productsNeeded = new HashMap<>();
    Map<Resource.Type, Integer> resourcesNeeded = new HashMap<>();
    var parts = row.child(2).text().trim().split("\\s+");
    for (int i = 0; i < parts.length - 1; i += 2) {
      int amount = Integer.parseInt(parts[i]);
      String typeString = parts[i + 1].split("=")[1];
      typeString = typeString.substring(0, typeString.length() - 1);
      if (typeString.equals("1|1")) {
        resourcesNeeded.put(Resource.Type.Wood, amount);
      } else if (typeString.equals("1|2")) {
        resourcesNeeded.put(Resource.Type.Clay, amount);
      } else if (typeString.equals("1|3")) {
        resourcesNeeded.put(Resource.Type.Iron, amount);
      } else if (typeString.equals("1|4")) {
        resourcesNeeded.put(Resource.Type.Stone, amount);
      } else if (typeString.equals("1|5")) {
        resourcesNeeded.put(Resource.Type.Food, amount);
      } else if (typeString.equals("2|2")) {
        resourcesNeeded.put(Resource.Type.Research, amount);
      } else if (typeString.equals("3|1")) {
        productsNeeded.put(Product.Type.Horse, amount);
      } else if (typeString.equals("3|2")) {
        productsNeeded.put(Product.Type.Cow, amount);
      } else if (typeString.equals("3|7")) {
        productsNeeded.put(Product.Type.Book, amount);
      } else if (typeString.equals("4|1")) {
        resourcesNeeded.put(Resource.Type.Gold, amount);
      } else {
        LOG.warn(
            "Unknown resource/product needed for {}: type={}, amount={}",
            productType,
            typeString,
            amount);
      }
    }

    Map<String, String> fields = new HashMap<>();
    for (var input : row.select("form input[name]")) {
      String name = input.attr("name");
      String amount = name.equals(QUANTITY_FIELD) ? input.attr("placeholder") : input.val();
      fields.put(name, amount);
    }

    return new Requirement(
        productType, stored.get(productType), resourcesNeeded, productsNeeded, fields);
  }

  private WaitTime scheduleProduct(Progress progress, Requirement requirement) {
    var fields = requirement.fields;
    fields.put(QUANTITY_FIELD, "1");
    var response = scheduleProductClient.call(fields);
    assert response.output.isPresent();

    AuthenticatedHttpClient.GetHtml timeClient =
        new AuthenticatedHttpClient.GetHtml(progress.url, authenticator);
    var timeResponse = timeClient.call(Map.of());
    assert timeResponse.output.isPresent();
    var progTime = timeResponse.output.get().select("div.info span.progTime").attr("data");
    long diffMillis =
        Math.max(0, Long.parseLong(progTime.split("\\|")[1]) - System.currentTimeMillis());
    return new WaitTime(Math.round(Math.ceil(diffMillis / 1000D)));
  }

  private static class Progress {
    private final Building.Type type;
    private final String url;
    private final WaitTime waitTime;

    private Progress(Building.Type type, String url, WaitTime waitTime) {
      this.type = type;
      this.url = url;
      this.waitTime = waitTime;
    }
  }

  private static class Requirement {
    private final Product.Type type;
    private final int amount;
    private final Map<Resource.Type, Integer> resources;
    private final Map<Product.Type, Integer> products;
    private final Map<String, String> fields;

    private Requirement(
        Product.Type type,
        int amount,
        Map<Resource.Type, Integer> resources,
        Map<Product.Type, Integer> products,
        Map<String, String> fields) {
      this.type = type;
      this.amount = amount;
      this.resources = resources;
      this.products = products;
      this.fields = fields;
    }

    private boolean satisfied() {
      int canBuild = Integer.parseInt(fields.get(QUANTITY_FIELD));
      return canBuild >= MIN_CAN_BUILD;
    }
  }

  private static class ScheduleResponse {
    int r;
  }
}
