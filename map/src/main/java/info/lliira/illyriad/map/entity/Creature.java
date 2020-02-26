package info.lliira.illyriad.map.entity;

public class Creature extends Location<Creature.Builder> {
  public final String id;
  public final String name;
  public final String amount;

  public Creature(int x, int y, String id, String name, String amount) {
    super(x, y);
    this.id = id;
    this.name = name;
    this.amount = amount;
  }

  @Override
  public String toString() {
    return String.format(
        "Creature{%s id='%s', name=%s, amount=%s}", super.toString(), id, name, amount);
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends Location.Builder<Creature> {
    private String id;
    private String name;
    private String amount;

    public Builder() {}

    private Builder(Creature creature) {
      super(creature);
      this.id = creature.id;
      this.name = creature.name;
      this.amount = creature.amount;
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder amount(String amount) {
      this.amount = amount;
      return this;
    }

    @Override
    public Creature build() {
      return new Creature(x, y, id, name, amount);
    }
  }
}
