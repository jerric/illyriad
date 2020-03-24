package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.WaitTime;
import info.lliira.illyriad.schedule.product.Product;
import info.lliira.illyriad.schedule.town.Resource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Building {
  public final String name;
  public final Type type;
  public final int level;
  public final boolean upgrading;
  public final int nextLevel;
  public final int woodCost;
  public final int clayCost;
  public final int ironCost;
  public final int stoneCost;
  public final int foodConsumption;
  public final WaitTime time;
  public final Map<String, String> upgradeFields;

  public Building(
      String name,
      int level,
      boolean upgrading,
      int nextLevel,
      int woodCost,
      int clayCost,
      int ironCost,
      int stoneCost,
      int foodConsumption,
      WaitTime time,
      Map<String, String> upgradeFields) {
    this.name = name;
    this.type = Type.parse(name);
    this.level = level;
    this.upgrading = upgrading;
    this.nextLevel = nextLevel;
    this.woodCost = woodCost;
    this.clayCost = clayCost;
    this.ironCost = ironCost;
    this.stoneCost = stoneCost;
    this.foodConsumption = foodConsumption;
    this.time = time;
    this.upgradeFields = upgradeFields;
  }

  public enum Type {
    Lumberjack(Resource.Type.Wood),
    ClayPit(Resource.Type.Clay),
    IronMine(Resource.Type.Iron),
    Quarry(Resource.Type.Stone),
    Farmyard(Resource.Type.Food),
    Library(Resource.Type.Research),
    MageTower(Resource.Type.Mana),
    ArchitectsOffice(Resource.Type.None),
    Barracks(Resource.Type.None),
    Blacksmith(Product.Type.Sword, Product.Type.Chainmail),
    BookBinder(Product.Type.Book),
    Brewery(Product.Type.Beer),
    Carpentry(Resource.Type.None),
    CommonGround(Product.Type.Cow),
    Consulate(Resource.Type.None),
    Fletcher(Product.Type.Bow),
    Forge(Product.Type.Plate),
    Foundry(Resource.Type.None),
    Marketplace(Resource.Type.None),
    Paddock(Product.Type.Horse),
    Saddlemaker(Product.Type.Saddle),
    SiegeWorkshop(Product.Type.Siege),
    Spearmaker(Product.Type.Spear),
    Stonemason(Resource.Type.None),
    Storehouse(Resource.Type.None),
    Tannery(Product.Type.Leather),
    Tavern(Resource.Type.None),
    Vault(Resource.Type.None),
    Warehouse(Resource.Type.None);

    private static final Map<String, Type> TYPES =
        Arrays.stream(values()).collect(Collectors.toMap(Type::name, type -> type));

    public static Type parse(String name) {
      name = name.replaceAll("[\\s']", "");
      return TYPES.get(name);
    }

    public final Resource.Type resourceType;
    public final Set<Product.Type> productTypes;

    Type(Resource.Type resourceType) {
      this(resourceType, List.of());
    }

    Type(Product.Type... productTypes) {
      this(Resource.Type.None, Arrays.asList(productTypes));
    }

    Type(Resource.Type resourceType, List<Product.Type> productTypes) {
      this.resourceType = resourceType;
      this.productTypes = new HashSet<>(productTypes);
    }
  }
}
