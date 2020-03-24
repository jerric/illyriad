package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.common.WaitTime;

import java.util.Optional;

public class Progress {
  public final Optional<WaitTime> construction1;
  public final Optional<WaitTime> construction2;
  public final Optional<WaitTime> research1;
  public final Optional<WaitTime> research2;

  public Progress(
      Optional<WaitTime> construction1,
      Optional<WaitTime> construction2,
      Optional<WaitTime> research1,
      Optional<WaitTime> research2) {
    this.construction1 = construction1;
    this.construction2 = construction2;
    this.research1 = research1;
    this.research2 = research2;
  }

  public int constructionCount() {
    return construction2.isPresent() ? 2 : construction1.isPresent() ? 1 : 0;
  }

  public WaitTime constructionWaitTime() {
    return construction2.orElse(construction1.orElse(new WaitTime(0)));
  }
}
