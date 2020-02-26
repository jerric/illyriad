package info.lliira.illyriad.map.entity;

public class ValidPlot extends Location<ValidPlot.Builder> {
    public final int resourceSum;
    public final int foodSum;
    public final int sovereignCount;
    public final boolean restricted;

    private ValidPlot(int x, int y, int resourceSum, int foodSum, int sovereignCount, boolean restricted) {
        super(x, y);
        this.resourceSum = resourceSum;
        this.foodSum = foodSum;
        this.sovereignCount = sovereignCount;
        this.restricted = restricted;
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

        public Builder setResourceSum(int resourceSum) {
            this.resourceSum = resourceSum;
            return this;
        }

        public Builder setFoodSum(int foodSum) {
            this.foodSum = foodSum;
            return this;
        }

        public Builder setSovereignCount(int sovereignCount) {
            this.sovereignCount = sovereignCount;
            return this;
        }

        public Builder setRestricted(boolean restricted) {
            this.restricted = restricted;
            return this;
        }

        @Override
        public ValidPlot build() {
            return new ValidPlot(x, y, resourceSum, foodSum, sovereignCount, restricted);
        }
    }
}
