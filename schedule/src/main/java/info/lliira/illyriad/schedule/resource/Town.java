package info.lliira.illyriad.schedule.resource;

public class Town {
  public final int id;
  public final String name;
  public final boolean current;

  public Town(int id, String name, boolean current) {
    this.id = id;
    this.name = name;
    this.current = current;
  }
}
