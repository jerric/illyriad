package info.lliira.illyriad.map.entity;

public class ValidPlot extends Location<ValidPlot.Builder> {
  private static final int MIN_DISTANCE = 8;
  private static final int MAX_DISTANCE = 20;
  private static final int MIN_DISTANCE_SQ = MIN_DISTANCE * MIN_DISTANCE;
  private static final int MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

  public final int resourceSum;
  public final int foodSum;
  public final int sovereignCount;
  public final boolean restricted;

  private ValidPlot(
      int x, int y, int resourceSum, int foodSum, int sovereignCount, boolean restricted) {
    super(x, y);
    this.resourceSum = resourceSum;
    this.foodSum = foodSum;
    this.sovereignCount = sovereignCount;
    this.restricted = restricted;
  }

  public boolean minRange(ValidPlot candidate) {
    int dx = x - candidate.x;
    int dy = y - candidate.y;
    int distanceSq = dx * dx + dy * dy;
    return distanceSq >= MIN_DISTANCE_SQ;
  }

  public boolean inRange(ValidPlot candidate) {
    int dx = Math.abs(x - candidate.x);
    int dy = Math.abs(y - candidate.y);
    if (dx > MAX_DISTANCE || dy > MAX_DISTANCE) return false;
    int distanceSq = dx * dx + dy * dy;
    return distanceSq >= MIN_DISTANCE_SQ && distanceSq <= MAX_DISTANCE_SQ;
  }

    @Override
    public String toString() {
        return super.toString() + " food=" + foodSum + ", total="+ resourceSum;
    }

    @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends Location.Builder<ValidPlot> {
    private int resourceSum;
    private int foodSum;
    private int sovereignCount;
    private boolean restricted;

    public Builder() {}

    private Builder(ValidPlot validPlot) {
      super(validPlot);
      resourceSum = validPlot.resourceSum;
      foodSum = validPlot.foodSum;
      sovereignCount = validPlot.sovereignCount;
      restricted = validPlot.restricted;
    }

    public Builder resourceSum(int resourceSum) {
      this.resourceSum = resourceSum;
      return this;
    }

    public Builder foodSum(int foodSum) {
      this.foodSum = foodSum;
      return this;
    }

    public Builder sovereignCount(int sovereignCount) {
      this.sovereignCount = sovereignCount;
      return this;
    }

    public Builder restricted(boolean restricted) {
      this.restricted = restricted;
      return this;
    }

    @Override
    public ValidPlot build() {
      return new ValidPlot(x, y, resourceSum, foodSum, sovereignCount, restricted);
    }
  }
}
