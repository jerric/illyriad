package info.lliira.illyriad.schedule.resource;

public class TownEntity {
  public final int id;
  public final String name;
  public final boolean current;

  public TownEntity(int id, String name, boolean current) {
    this.id = id;
    this.name = name;
    this.current = current;
  }
}
