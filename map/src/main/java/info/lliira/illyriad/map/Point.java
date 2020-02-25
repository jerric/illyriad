package info.lliira.illyriad.map;

import java.util.Objects;

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
    return Objects.hash(x, y);
  }
}
