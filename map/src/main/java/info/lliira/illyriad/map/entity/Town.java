package info.lliira.illyriad.map.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Town data */
public class Town extends Location<Town.Builder> {
  public final int id;
  public final String name;
  public final int ownerId;
  public final String ownerName;
  public final int population;
  public final String alliance;
  public final Region region;
  public final Race race;
  public final boolean capital;
  public final boolean protection;
  public final boolean misc1;
  public final boolean abandoned;
  public final String data;

  private Town(
      int x,
      int y,
      int id,
      String name,
      int ownerId,
      String ownerName,
      int population,
      String alliance,
      Region region,
      Race race,
      boolean capital,
      boolean protection,
      boolean misc1,
      boolean abandoned,
      String data) {
    super(x, y);
    this.id = id;
    this.name = name;
    this.ownerId = ownerId;
    this.ownerName = ownerName;
    this.population = population;
    this.alliance = alliance;
    this.region = region;
    this.race = race;
    this.capital = capital;
    this.protection = protection;
    this.misc1 = misc1;
    this.abandoned = abandoned;
    this.data = data;
  }

  @Override
  public String toString() {
    return String.format("Town{%s %s}", name, super.toString());
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends Location.Builder<Town> {
    private int id;
    private String name;
    private int ownerId;
    private String ownerName;
    private int population;
    private String alliance;
    private Region region;
    private Race race;
    private boolean capital;
    private boolean protection;
    private boolean misc1;
    private boolean abandoned;
    private String data;

    private Builder() {}

    private Builder(Town town) {
      super(town);
      id = town.id;
      name = town.name;
      ownerId = town.ownerId;
      ownerName = town.ownerName;
      population = town.population;
      alliance = town.alliance;
      region = town.region;
      race = town.race;
      capital = town.capital;
      protection = town.protection;
      misc1 = town.misc1;
      abandoned = town.abandoned;
      data = town.data;
    }

    public Builder setId(int id) {
      this.id = id;
      return this;
    }

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setOwnerId(int ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    public Builder setOwnerName(String ownerName) {
      this.ownerName = ownerName;
      return this;
    }

    public Builder setPopulation(int population) {
      this.population = population;
      return this;
    }

    public Builder setAlliance(String alliance) {
      this.alliance = alliance;
      return this;
    }

    public Builder setRegion(Region region) {
      this.region = region;
      return this;
    }

    public Builder setRace(Race race) {
      this.race = race;
      return this;
    }

    public Builder setCapital(boolean capital) {
      this.capital = capital;
      return this;
    }

    public Builder setProtection(boolean protection) {
      this.protection = protection;
      return this;
    }

    public Builder setMisc1(boolean misc1) {
      this.misc1 = misc1;
      return this;
    }

    public void setAbandoned(boolean abandoned) {
      this.abandoned = abandoned;
    }

    public Builder setData(String data) {
      this.data = data;
      return this;
    }

    @Override
    public Town build() {
      return new Town(
          x,
          y,
          id,
          name,
          ownerId,
          ownerName,
          population,
          alliance,
          region,
          race,
          capital,
          protection,
          misc1,
          abandoned,
          data);
    }
  }

  public enum Race {
    Human(1),
    Elf(2),
    Dwarf(3),
    Orc(4);

    private static final Map<Integer, Race> RACES =
        Arrays.stream(values()).collect(Collectors.toMap(race -> race.code, race -> race));

    public static Race valueOf(int code) {
      var race = RACES.get(code);
      if (race == null) throw new IndexOutOfBoundsException("Invalid race code: " + code);
      else return race;
    }

    private final int code;

    Race(int code) {
      this.code = code;
    }
  }
}
