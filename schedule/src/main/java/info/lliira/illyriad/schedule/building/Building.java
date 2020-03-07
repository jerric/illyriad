package info.lliira.illyriad.schedule.building;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Building {
  public final String name;
  public final Type type;
  public final int level;
  public final int woodCost;
  public final int clayCost;
  public final int ironCost;
  public final int stoneCost;
  public final int foodConsumption;
  public final Duration time;
  public final Map<String, String> updateFields;

  public Building(
      String name,
      int level,
      int woodCost,
      int clayCost,
      int ironCost,
      int stoneCost,
      int foodConsumption,
      Duration time, Map<String, String> updateFields) {
    this.name = name;
    this.type = Type.parse(name);
    this.level = level;
    this.woodCost = woodCost;
    this.clayCost = clayCost;
    this.ironCost = ironCost;
    this.stoneCost = stoneCost;
    this.foodConsumption = foodConsumption;
    this.time = time;
    this.updateFields = updateFields;
  }

  public enum Type {
    Lumberjack("Lumberjack"),
    ClayPit("Clay Pit"),
    IronMine("Iron Mine"),
    Quarry("Quarry"),
    Farm("Farm");

    private static final Map<String, Type> TYPES =
        Arrays.stream(values()).collect(Collectors.toMap(type -> type.name, type -> type));

    public static Type parse(String name) {
      return TYPES.get(name);
    }

    public final String name;

    private Type(String name) {
      this.name = name;
    }
  }
}
