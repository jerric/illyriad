package info.lliira.illyriad.map.entity;

public class Plot extends Location<Plot.Builder> {
    public final int wood;
    public final int clay;
    public final int iron;
    public final int stone;
    public final int food;
    public final int total;
    public final int background;
    public final int plotType;
    public final int layer;
    public final Region region;
    public final boolean sovable;
    public final boolean passable;
    public final boolean hospital;
    public final boolean npc;
    public final boolean brg;

    private Plot(int x,
            int y,
            int wood,
            int clay,
            int iron,
            int stone,
            int food,
            int total,
            int background,
            int plotType,
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
        this.plotType = plotType;
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
        private int background;
        private int plotType;
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
            plotType = plot.plotType;
            layer = plot.layer;
            region = plot.region;
            sovable = plot.sovable;
            passable = plot.passable;
            hospital = plot.hospital;
            npc = plot.npc;
            brg = plot.brg;
        }

        public Builder setWood(int wood) {
            this.wood = wood;
            return this;
        }

        public Builder setClay(int clay) {
            this.clay = clay;
            return this;
        }

        public Builder setIron(int iron) {
            this.iron = iron;
            return this;
        }

        public Builder setStone(int stone) {
            this.stone = stone;
            return this;
        }

        public Builder setFood(int food) {
            this.food = food;
            return this;
        }

        public Builder setBackground(int background) {
            this.background = background;
            return this;
        }

        public Builder setPlotType(int plotType) {
            this.plotType = plotType;
            return this;
        }

        public Builder setLayer(int layer) {
            this.layer = layer;
            return this;
        }

        public Builder setRegion(Region region) {
            this.region = region;
            return this;
        }

        public Builder setSovable(boolean sovable) {
            this.sovable = sovable;
            return this;
        }

        public Builder setPassable(boolean passable) {
            this.passable = passable;
            return this;
        }

        public Builder setHospital(boolean hospital) {
            this.hospital = hospital;
            return this;
        }

        public Builder setNpc(boolean npc) {
            this.npc = npc;
            return this;
        }

        public Builder setBrg(boolean brg) {
            this.brg = brg;
            return this;
        }

        @Override
        public Plot build() {
            int total = wood + clay + iron + stone + food;
            return new Plot(x, y, wood, clay, iron, stone, food, total, background, plotType, layer, region, sovable,
                    passable, hospital, npc, brg);
        }
    }
}
