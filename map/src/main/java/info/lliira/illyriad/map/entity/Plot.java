package info.lliira.illyriad.map.entity;

public class Plot extends Location<Plot.Builder> {
  public final int wood;
  public final int clay;
  public final int iron;
  public final int stone;
  public final int food;
  public final int total;
  public final int background;
  public final String type;
  public final int layer;
  public final Region region;
  public final boolean sovable;
  public final boolean passable;
  public final boolean hospital;
  public final boolean npc;
  public final boolean brg;

  private Plot(
      int x,
      int y,
      int wood,
      int clay,
      int iron,
      int stone,
      int food,
      int total,
      int background,
      String type,
      int layer,
      Region region,
      boolean sovable,
      boolean passable,
      boolean hospital,
      boolean npc,
      boolean brg) {
    super(x, y);
    this.wood = wood;
    this.clay = clay;
    this.iron = iron;
    this.stone = stone;
    this.food = food;
    this.total = total;
    this.background = background;
    this.type = type;
    this.layer = layer;
    this.region = region;
    this.sovable = sovable;
    this.passable = passable;
    this.hospital = hospital;
    this.npc = npc;
    this.brg = brg;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends Location.Builder<Plot> {
    private int wood;
    private int clay;
    private int iron;
    private int stone;
    private int food;
    private int total;
    private int background;
    private String plotType;
    private int layer;
    private Region region;
    private boolean sovable;
    private boolean passable;
    private boolean hospital;
    private boolean npc;
    private boolean brg;

    public Builder() {}

    private Builder(Plot plot) {
      super(plot);
      wood = plot.wood;
      clay = plot.clay;
      iron = plot.iron;
      stone = plot.stone;
      food = plot.food;
      background = plot.background;
      plotType = plot.type;
      layer = plot.layer;
      region = plot.region;
      sovable = plot.sovable;
      passable = plot.passable;
      hospital = plot.hospital;
      npc = plot.npc;
      brg = plot.brg;
    }

    public Builder wood(int wood) {
      this.wood = wood;
      return this;
    }

    public Builder clay(int clay) {
      this.clay = clay;
      return this;
    }

    public Builder iron(int iron) {
      this.iron = iron;
      return this;
    }

    public Builder stone(int stone) {
      this.stone = stone;
      return this;
    }

    public Builder food(int food) {
      this.food = food;
      return this;
    }

    public Builder total(int total) {
      this.total = total;
      return this;
    }

    public Builder background(int background) {
      this.background = background;
      return this;
    }

    public Builder type(String plotType) {
      this.plotType = plotType;
      return this;
    }

    public Builder layer(int layer) {
      this.layer = layer;
      return this;
    }

    public Builder region(Region region) {
      this.region = region;
      return this;
    }

    public Builder sovable(boolean sovable) {
      this.sovable = sovable;
      return this;
    }

    public Builder passable(boolean passable) {
      this.passable = passable;
      return this;
    }

    public Builder hospital(boolean hospital) {
      this.hospital = hospital;
      return this;
    }

    public Builder npc(boolean npc) {
      this.npc = npc;
      return this;
    }

    public Builder brg(boolean brg) {
      this.brg = brg;
      return this;
    }

    @Override
    public Plot build() {
      total = wood + clay + iron + stone + food;
      return new Plot(
          x,
          y,
          wood,
          clay,
          iron,
          stone,
          food,
          total,
          background,
          plotType,
          layer,
          region,
          sovable,
          passable,
          hospital,
          npc,
          brg);
    }
  }
}
