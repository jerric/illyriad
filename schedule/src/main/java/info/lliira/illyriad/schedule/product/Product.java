package info.lliira.illyriad.schedule.product;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Product {
  public final Type type;
  public final int amount;

  public Product(Type type, int amount) {
    this.type = type;
    this.amount = amount;
  }

  public enum Type {
    None,
    Horse,
    Cow,
    Beer,
    Book,
    Spear,
    Sword,
    Bow,
    Saddle,
    Leather,
    Chainmail,
    Plate,
    Siege;

    private static Map<String, Type> TYPES =
        Arrays.stream(values())
            .collect(Collectors.toMap(type -> type.name().toLowerCase(), type -> type));

    public static Type parse(String name) {
      name = name.trim().split("\\s+")[0].toLowerCase();
      if (name.endsWith("s")) name = name.substring(0, name.length() - 1);
      return TYPES.getOrDefault(name, None);
    }
  }
}
