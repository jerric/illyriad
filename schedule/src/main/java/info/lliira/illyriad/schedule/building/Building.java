package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.common.WaitTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Building {
  private static final Logger LOG = LogManager.getLogger(Building.class.getSimpleName());

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
    Unknown,
    Lumberjack,
    ClayPit,
    IronMine,
    Quarry,
    Farmyard,
    Library,
    MageTower,
    ArchitectsOffice,
    Barracks,
    Blacksmith,
    BookBinder,
    Brewery,
    Carpentry,
    CommonGround,
    Consulate,
    Fletcher,
    Forge,
    Foundry,
    Marketplace,
    Paddock,
    Saddlemaker,
    SiegeWorkshop,
    Spearmaker,
    Stonemason,
    Storehouse,
    Tannery,
    Tavern,
    Vault,
    Warehouse;

    private static final Map<String, Type> TYPES =
        Arrays.stream(values())
            .collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));

    public static Type parse(String name) {
      name = name.replaceAll("[\\s']", "").toLowerCase();
      if (TYPES.containsKey(name)) return TYPES.get(name);
      LOG.warn("Invalid Building Type: {}", name);
      return Unknown;
    }
  }
}
