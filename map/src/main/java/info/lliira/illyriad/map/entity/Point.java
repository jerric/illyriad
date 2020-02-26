package info.lliira.illyriad.map.entity;

public class Point {
  public final int x;
  public final int y;

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Point) {
      Point point = (Point) o;
      return x == point.x && y == point.y;
    } else return false;
  }

  @Override
  public int hashCode() {
    return x ^ y;
  }

  @Override
  public String toString() {
    return String.format("[%d,%d]", x, y);
  }
}
