package info.lliira.illyriad.map.entity;

public class ValidPlot extends Location<ValidPlot.Builder> implements Comparable<ValidPlot> {
  private static final int MIN_DISTANCE = 9;
  private static final int MAX_DISTANCE = 40;
  private static final int MIN_DISTANCE_SQ = MIN_DISTANCE * MIN_DISTANCE;
  private static final int MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;
  public final int totalSum;
  public final int foodSum;
  public final int sovereignCount;
  public final boolean restricted;

  private ValidPlot(
      int x, int y, int totalSum, int foodSum, int sovereignCount, boolean restricted) {
    super(x, y);
    this.totalSum = totalSum;
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
        return super.toString() + " food=" + foodSum + ", total="+ totalSum;
    }

    @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public int compareTo(ValidPlot plot) {
      int food = plot.foodSum - foodSum;
      return (food != 0) ? food : plot.totalSum - totalSum;
  }

  public static class Builder extends Location.Builder<ValidPlot> {
    private int totalSum;
    private int foodSum;
    private int sovereignCount;
    private boolean restricted;

    public Builder() {}

    private Builder(ValidPlot validPlot) {
      super(validPlot);
      totalSum = validPlot.totalSum;
      foodSum = validPlot.foodSum;
      sovereignCount = validPlot.sovereignCount;
      restricted = validPlot.restricted;
    }

    public Builder totalSum(int resourceSum) {
      this.totalSum = resourceSum;
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
      return new ValidPlot(x, y, totalSum, foodSum, sovereignCount, restricted);
    }
  }
}
