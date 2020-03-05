package info.lliira.illyriad.schedule.resource;

public class Resource {

  public final Type type;
  public final int amount;
  public final int rate;

  public Resource(Type type, int amount, int rate) {
    this.type = type;
    this.amount = amount;
    this.rate = rate;
  }

  public enum Type {
    Wood,
    Clay,
    Iron,
    Stone,
    Food,
    Gold,
    Mana,
    Research
  }
}
