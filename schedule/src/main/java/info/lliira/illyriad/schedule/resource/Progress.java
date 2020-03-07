package info.lliira.illyriad.schedule.resource;

import java.util.Optional;

public class Progress {
  public final Optional<Long> construction1;
  public final Optional<Long> construction2;
  public final Optional<Long> research1;
  public final Optional<Long> research2;

  public Progress(Optional<Long> construction1, Optional<Long> construction2, Optional<Long> research1, Optional<Long> research2) {
    this.construction1 = construction1;
    this.construction2 = construction2;
    this.research1 = research1;
    this.research2 = research2;
  }
}