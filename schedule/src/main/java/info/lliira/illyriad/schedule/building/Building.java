package info.lliira.illyriad.schedule.building;

import java.time.Duration;

public class Building {
  private final String name;
  private final int level;
  private final int wood;
  private final int clay;
  private final int iron;
  private final int stone;
  private final int food;
  private final Duration time;

  public Building(String name, int level, int wood, int clay, int iron, int stone, int food, Duration time) {
    this.name = name;
    this.level = level;
    this.wood = wood;
    this.clay = clay;
    this.iron = iron;
    this.stone = stone;
    this.food = food;
    this.time = time;
  }

  public enum Type {
    Lumberjack,

  }
}
