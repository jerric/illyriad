package net.lliira.illyriad.map.model;

/**
 * Represent the coordinate of a plot.
 */
public class Point {
    public final int mX;
    public final int mY;

    public static Point of(int x, int y) {
        return new Point(x, y);
    }

    public static Point of(String coordinate) {
        String[] parts = coordinate.split("\\|");
        int x = Integer.valueOf(parts[1]);
        int y = Integer.valueOf(parts[0]);
        return new Point(x, y);
    }

    Point(int x, int y) {
        mX = x;
        mY = y;
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", mX, mY);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Point) {
            Point point = (Point)obj;
            return mX == point.mX && mY == point.mY;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return mX ^ mY;
    }
}
