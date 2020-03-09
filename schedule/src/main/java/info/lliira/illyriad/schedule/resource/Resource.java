package info.lliira.illyriad.schedule.resource;

public class Resource {

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
