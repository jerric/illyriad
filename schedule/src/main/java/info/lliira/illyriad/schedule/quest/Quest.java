package info.lliira.illyriad.schedule.quest;

import info.lliira.illyriad.schedule.town.Resource;

import java.util.Map;

public class Quest {
  public final int id;
  public final int aleNeeded;
  public final Map<Resource.Type, Integer> resourcesNeeded;

  public Quest(int id, int aleNeeded, Map<Resource.Type, Integer> resourcesNeeded) {
    this.id = id;
    this.aleNeeded = aleNeeded;
    this.resourcesNeeded = resourcesNeeded;
  }
}
