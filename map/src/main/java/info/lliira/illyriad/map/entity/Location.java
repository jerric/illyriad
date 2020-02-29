package info.lliira.illyriad.map.entity;

/**
 * Represent the coordinate of a plot.
 */
public abstract class Location<B extends Location.Builder<?>> extends Point implements Entity<B> {

    protected Location(int x, int y) {
        super(x, y);
    }

    public static abstract class Builder<E extends Location<?>> implements Entity.Builder<E> {

        protected int x;
        protected int y;

        protected Builder() {}

        protected Builder(E location) {
            this.x = location.x;
            this.y = location.y;
        }

        public Builder<E> x(int x) {
            this.x = x;
            return this;
        }

        public Builder<E> y(int y) {
            this.y = y;
            return this;
        }

        public Builder<E> coordinate(String coordinate) {
            String[] parts = coordinate.split("\\|");
            this.x = Integer.parseInt(parts[1]);
            this.y = Integer.parseInt(parts[0]);
            return this;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }
    }
}
