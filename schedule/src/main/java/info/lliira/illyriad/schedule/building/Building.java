package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.schedule.resource.Resource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
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
  public final Duration time;
  public final Map<String, String> upgradeFields;

  public Building(
      String name,
      int level,
      boolean upgrading, int nextLevel, int woodCost,
      int clayCost,
      int ironCost,
      int stoneCost,
      int foodConsumption,
      Duration time, Map<String, String> upgradeFields) {
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
    Lumberjack("Lumberjack", Resource.Type.Wood),
    ClayPit("Clay Pit", Resource.Type.Clay),
    IronMine("Iron Mine", Resource.Type.Iron),
    Quarry("Quarry", Resource.Type.Stone),
    Farm("Farm", Resource.Type.Food);

    private static final Map<String, Type> TYPES =
        Arrays.stream(values()).collect(Collectors.toMap(type -> type.name, type -> type));

    public static Type parse(String name) {
      return TYPES.get(name);
    }

    public final String name;
    public final Resource.Type resourceType;

    Type(String name, Resource.Type resourceType) {
      this.name = name;
      this.resourceType = resourceType;
    }
  }
}
