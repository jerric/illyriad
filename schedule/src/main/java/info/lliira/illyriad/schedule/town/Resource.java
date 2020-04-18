package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.schedule.building.Building;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Resource {
  public static final Resource UNKNOWN = new Resource(Type.Unknown, 0, 0);

  private static final Logger LOG = LogManager.getLogger(Resource.class.getSimpleName());

  public final Type type;
  public final int amount;
  public final int rate;
  public final long timestamp;

  public Resource(Type type, int amount, int rate) {
    this.type = type;
    this.amount = amount;
    this.rate = rate;
    this.timestamp = System.currentTimeMillis();
  }

  public int current() {
    return till(System.currentTimeMillis());
  }

  public int till(long toTimestamp) {
    double delta = rate * (toTimestamp - this.timestamp) / 3_600_000.0;
    return amount + (int) Math.round(delta);
  }

  @Override
  public String toString() {
    return String.format("%s:%,d/%,d", type, current(), rate);
  }

  public enum Type {
    Unknown(Building.Type.Unknown),
    Wood(Building.Type.Lumberjack),
    Clay(Building.Type.ClayPit),
    Iron(Building.Type.IronMine),
    Stone(Building.Type.Quarry),
    Food(Building.Type.Farmyard),
    Gold(Building.Type.Unknown),
    Mana(Building.Type.MageTower),
    Research(Building.Type.Library);

    private static final Map<String, Type> TYPES =
        Arrays.stream(values())
            .collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));

    public static Type parse(String name) {
      name = name.trim().toLowerCase();
      if (name.endsWith("s")) name = name.substring(0, name.length() - 1);
      if (TYPES.containsKey(name)) return TYPES.get(name);
      LOG.warn("Invalid Resource type: {}", name);
      return Unknown;
    }

    public final Building.Type building;

    Type(Building.Type building) {
      this.building = building;
    }
  }
}
