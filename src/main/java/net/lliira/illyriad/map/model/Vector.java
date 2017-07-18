package net.lliira.illyriad.map.model;

/**
 * Represent the coordinate of a plot and the distance to the reference point.
 */
public class Vector extends Point implements Comparable<Vector> {

    public final double mDistance;

    public Vector(int x, int y, double distance) {
        super(x, y);
        mDistance = distance;
    }

    @Override
    public int compareTo(Vector vector) {
        return (mDistance > vector.mDistance) ? 1 : (mDistance < vector.mDistance) ? -1 : 0;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)=%.3f", mX, mY, mDistance);
    }
}
