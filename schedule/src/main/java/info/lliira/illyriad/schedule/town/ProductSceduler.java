package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.common.Constants;
import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.common.net.AuthenticatedHttpClient;
import info.lliira.illyriad.common.net.Authenticator;
import info.lliira.illyriad.common.net.AuthenticatorManager;
import info.lliira.illyriad.schedule.building.Building;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ProductSceduler {
  public static void main(String[] args) throws IOException {
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    var authenticator = new AuthenticatorManager(properties).first();
    var townLoader = new TownLoader(authenticator);
    var scheduler = new ProductSceduler(authenticator);
    scheduler.schedule(townLoader.loadTownInfo());
  }

  private static final String PRODUCT_LIST_URL = "/Town/Production";
  private static final String SCHEDULE_PRODUCT_URL = "/Town/QueueResourceBuild";
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

  public WaitTime schedule(TownInfo townInfo) {
    var progresses = parseProgresses();

    // Collects the min waitTime from all the ongoing products
    WaitTime minWaitTime =
        progresses.values().stream()
            .map(progress -> progress.waitTime)
            .filter(waitTime -> !waitTime.expired())
            .collect(() -> new WaitTime(Long.MAX_VALUE), WaitTime::min, WaitTime::min);

    // Filter idle buildings and convert to products, then sort them, product-dependent ones first,
    // then by amount
    var productTypes =
        progresses.values().stream()
            .filter(progress -> progress.waitTime.expired())
            .flatMap(progress -> fetchRequirements(progress).values().stream())
            .sorted(this::compareRequirements)
            .map(requirement -> requirement.productType)
            .collect(Collectors.toList());

    var queuedBuildings = new LinkedHashMap<Building.Type, Product.Type>();
    boolean insufficientProduct = false;
    for (var productType : productTypes) {
      // check if there are new productions in the building
      if (queuedBuildings.containsKey(productType.buildingType)) continue;

      var progress = progresses.get(productType.buildingType);
      // Fetch requirement again since the availability may change due to the resource uses.
      var requirement = fetchRequirements(progress).get(productType);
      if (requirement.satisfied()) {
        if (!requirement.needProducts() || !insufficientProduct) {
          var waitTime = scheduleProduct(progress, requirement);
          queuedBuildings.put(requirement.buildingType, requirement.productType);
          minWaitTime = minWaitTime.min(waitTime);
        } // else skip the rest of the product-dependent ones.
      } else if (requirement.needProducts()) {
        insufficientProduct = true;
      } // else skip the rest of the resource-dependent ones;
    }
    String queuedProducts =
        queuedBuildings.values().stream()
            .map(Product.Type::toString)
            .collect(Collectors.joining(","));
    LOG.info(
        "*** {} {} scheduled: {}; wait:{}",
        authenticator.player(),
        townInfo,
        queuedProducts,
        minWaitTime);
    return minWaitTime;
  }

  private int compareRequirements(Requirement left, Requirement right) {
    if (left.needProducts() && !right.needProducts()) return -1;
    else if (!left.needProducts() && right.needProducts()) return 1;
    else return left.amount - right.amount;
  }

  private Map<Building.Type, Progress> parseProgresses() {
    var response = productListClient.call(Map.of());
    assert response.output.isPresent();
    var progresses = new HashMap<Building.Type, Progress>();
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
      progresses.put(buildingType, new Progress(buildingType, url, waitTime));
    }
    return progresses;
  }

  private Map<Product.Type, Requirement> fetchRequirements(Progress progress) {
    AuthenticatedHttpClient.GetHtml requirementClient =
        new AuthenticatedHttpClient.GetHtml(progress.url, authenticator);
    var response = requirementClient.call(Map.of());
    assert response.output.isPresent();
    var document = response.output.get();
    var stored = parseStored(document.select("table.info tr"));

    var requirements = new HashMap<Product.Type, Requirement>();
    var rows = document.select("div.info table tr");
    // skip the title row
    for (int i = 1; i < rows.size(); i++) {
      var requirement = parseRequirement(progress.buildingType, rows.get(i), stored);
      requirements.put(requirement.productType, requirement);
    }
    return requirements;
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

  private Requirement parseRequirement(
      Building.Type buildingType, Element row, Map<Product.Type, Integer> stored) {
    var productType = Product.Type.parse(row.child(1).text());
    if (productType == Product.Type.Unknown) {
      LOG.warn("Unknown product type {}", row.child(1).text());
    }

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
        productsNeeded.put(Product.Type.Livestock, amount);
      } else if (typeString.equals("3|7")) {
        productsNeeded.put(Product.Type.Book, amount);
      } else if (typeString.equals("4|1")) {
        resourcesNeeded.put(Resource.Type.Gold, amount);
      } else if (typeString.equals("253")) {
        productsNeeded.put(Product.Type.Grape, amount);
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
        buildingType,
        productType,
        stored.getOrDefault(productType, 0),
        resourcesNeeded,
        productsNeeded,
        fields);
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
    private final Building.Type buildingType;
    private final String url;
    private final WaitTime waitTime;

    private Progress(Building.Type buildingType, String url, WaitTime waitTime) {
      this.buildingType = buildingType;
      this.url = url;
      this.waitTime = waitTime;
    }

    @Override
    public String toString() {
      return String.format("Progress[%s] %s", buildingType, waitTime);
    }
  }

  private static class Requirement {
    private final Building.Type buildingType;
    private final Product.Type productType;
    private final int amount;
    private final Map<Resource.Type, Integer> resources;
    private final Map<Product.Type, Integer> products;
    private final Map<String, String> fields;

    private Requirement(
        Building.Type buildingType,
        Product.Type productType,
        int amount,
        Map<Resource.Type, Integer> resources,
        Map<Product.Type, Integer> products,
        Map<String, String> fields) {
      this.buildingType = buildingType;
      this.productType = productType;
      this.amount = amount;
      this.resources = resources;
      this.products = products;
      this.fields = fields;
    }

    private boolean satisfied() {
      int canBuild = Integer.parseInt(fields.get(QUANTITY_FIELD));
      return canBuild >= MIN_CAN_BUILD;
    }

    private boolean needProducts() {
      return !products.isEmpty();
    }
  }

  private static class ScheduleResponse {
    int r;
  }
}
