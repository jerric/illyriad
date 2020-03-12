package info.lliira.illyriad.schedule.town;

import java.util.Optional;

public class Progress {
  public final Optional<Long> construction1;
  public final Optional<Long> construction2;
  public final Optional<Long> research1;
  public final Optional<Long> research2;

  public Progress(
      Optional<Long> construction1,
      Optional<Long> construction2,
      Optional<Long> research1,
      Optional<Long> research2) {
    this.construction1 = construction1;
    this.construction2 = construction2;
    this.research1 = research1;
    this.research2 = research2;
  }

  public int constructionCount() {
    return construction2.isPresent() ? 2 : construction1.isPresent() ? 1 : 0;
  }

  public long constructionTimestamp() {
    return construction2.orElse(construction1.orElse(System.currentTimeMillis()));
  }
}
