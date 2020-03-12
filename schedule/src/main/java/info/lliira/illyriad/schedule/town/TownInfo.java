package info.lliira.illyriad.schedule.town;

import java.awt.*;
import java.util.Date;

public class TownInfo {
  public final String cityName;
  public final Race race;
  public final String founded;
  public final int population;
  public final int capacity;
  public final String size;
  public final String location;

  public TownInfo(
      String cityName,
      Race race,
      String founded,
      int population,
      int capacity,
      String size,
      String location) {
    this.cityName = cityName;
    this.race = race;
    this.founded = founded;
    this.population = population;
    this.capacity = capacity;
    this.size = size;
    this.location = location;
  }

  public enum Race {
    Human,
    Elf,
    Dwarf,
    Orc
  }
}
