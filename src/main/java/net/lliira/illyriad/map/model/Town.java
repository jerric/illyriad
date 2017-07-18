package net.lliira.illyriad.map.model;

/**
 * Town data
 */
public class Town extends Point {

    public final String name;
    public final String owner;

    public Town(int x, int y, String name, String owner) {
        super(x, y);
        this.name = name;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return String.format("%s%s", name, super.toString());
    }
}
