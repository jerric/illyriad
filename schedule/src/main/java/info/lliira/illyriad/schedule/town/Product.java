package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.schedule.building.Building;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Product {
  public static final Product UNKNOWN = new Product(Type.Unknown, 0);

  public final Type type;
  public final int amount;

  public Product(Type type, int amount) {
    this.type = type;
    this.amount = amount;
  }

  public enum Type {
    Unknown(Building.Type.Unknown),
    Beer(Building.Type.Brewery),
    Book(Building.Type.BookBinder),
    Bow(Building.Type.Fletcher),
    Chainmail(Building.Type.Blacksmith),
    Grape(Building.Type.Unknown),
    Horse(Building.Type.Paddock),
    Leather(Building.Type.Tannery),
    Livestock(Building.Type.CommonGround),
    Plate(Building.Type.Forge),
    Saddle(Building.Type.Saddlemaker),
    Siege(Building.Type.SiegeWorkshop),
    Spear(Building.Type.Spearmaker),
    Sword(Building.Type.Blacksmith),
    Wine(Building.Type.Brewery);

    private static final Map<String, Type> TYPES =
        Arrays.stream(values())
            .collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));

    private static final Map<Building.Type, List<Type>> TYPES_BY_BUILDING =
        Arrays.stream(values()).collect(Collectors.groupingBy(type -> type.buildingType));

    public static Type parse(String name) {
      name = name.trim().split("\\s+")[0].toLowerCase();
      if (name.endsWith("s")) name = name.substring(0, name.length() - 1);
      return TYPES.getOrDefault(name, Unknown);
    }

    public static List<Type> findByBuilding(Building.Type building) {
      return TYPES_BY_BUILDING.getOrDefault(building, List.of());
    }

    public final Building.Type buildingType;

    Type(Building.Type buildingType) {
      this.buildingType = buildingType;
    }
  }
}
