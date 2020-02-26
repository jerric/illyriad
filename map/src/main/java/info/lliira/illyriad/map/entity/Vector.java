package info.lliira.illyriad.map.entity;

/**
 * Represent the coordinate of a plot and the distance to the reference point.
 */
public class Vector extends Location<Vector.Builder> implements Comparable<Vector> {
    public final double distance;

    private Vector(int x, int y, double distance) {
        super(x, y);
        this.distance = distance;
    }

    @Override
    public int compareTo(Vector vector) {
        return Double.compare(distance, vector.distance);
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]=%.3f", x, y, distance);
    }

    @Override
    public Builder toBuilder() {
        return new Builder(this);
    }

    public static class Builder extends Location.Builder<Vector> {
        private double distance;

        public Builder(){}

        private Builder(Vector vector) {
            super(vector);
            this.distance = vector.distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        @Override
        public Vector build() {
            return new Vector(x, y, distance);
        }
    }
}
