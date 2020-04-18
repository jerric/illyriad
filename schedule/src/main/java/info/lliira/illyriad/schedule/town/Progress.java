package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.common.WaitTime;

import java.util.Optional;

public class Progress {
  public final Optional<WaitTime> building1;
  public final Optional<WaitTime> building2;
  public final Optional<WaitTime> research1;
  public final Optional<WaitTime> research2;

  public Progress(
      Optional<WaitTime> building1,
      Optional<WaitTime> building2,
      Optional<WaitTime> research1,
      Optional<WaitTime> research2) {
    this.building1 = building1;
    this.building2 = building2;
    this.research1 = research1;
    this.research2 = research2;
  }

  public int buildingCount() {
    return building2.isPresent() ? 2 : building1.isPresent() ? 1 : 0;
  }

  public int researchCount() {
    return research2.isPresent() ? 2 : research1.isPresent() ? 1 : 0;
  }

  public WaitTime buildingWaitTime() {
    return building2.orElse(building1.orElse(new WaitTime(0)));
  }

  public WaitTime researchWaitTime() {
    return research1.orElse(new WaitTime(0));
  }

  @Override
  public String toString() {
    return String.format(
        "Progress: B: %d R: %d, to finish B: %s, R: %s",
        buildingCount(), researchCount(), buildingWaitTime(), researchWaitTime());
  }
}
