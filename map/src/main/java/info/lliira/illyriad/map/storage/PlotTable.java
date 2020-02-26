package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.Plot;

import java.sql.Connection;
import java.sql.SQLException;

public class PlotTable extends LocationTable<Plot, Plot.Builder> {

    private static final ImmutableList<Field<Plot, Plot.Builder, ?>> DATA_FIELDS = ImmutableList.of(
            new Field<>("wood", FieldType.INT, Plot::getWood, Plot.Builder::setWood),
            new Field<>("clay", FieldType.INT, Plot::getClay, Plot.Builder::setClay),
            new Field<>("iron", FieldType.INT, Plot::getIron, Plot.Builder::setIron),
            new Field<>("stone", FieldType.INT, Plot::getStone, Plot.Builder::setStone),
            new Field<>("food", FieldType.INT, Plot::getFood, Plot.Builder::setFood),
            new Field<>("background", FieldType.INT, Plot::getBackground, Plot.Builder::setBackground),
            new Field<>("plot_type", FieldType.INT, Plot::getPlotType, Plot.Builder::setPlotType),
            new Field<>("layer", FieldType.INT, Plot::getLayer, Plot.Builder::setLayer),
            new Field<>("region", FieldType.REGION, Plot::getRegion, Plot.Builder::setRegion),
            new Field<>("sovable", FieldType.BOOLEAN, Plot::isSovable, Plot.Builder::setSovable),
            new Field<>("passable", FieldType.BOOLEAN, Plot::isPassable, Plot.Builder::setPassable),
            new Field<>("hospital", FieldType.BOOLEAN, Plot::isHospital, Plot.Builder::setHospital),
            new Field<>("npc", FieldType.BOOLEAN, Plot::isNpc, Plot.Builder::setNpc),
            new Field<>("brg", FieldType.BOOLEAN, Plot::isBrg, Plot.Builder::setBrg));

    PlotTable(Connection connection) throws SQLException {
        super(connection, "plots", DATA_FIELDS);
    }

    @Override
    public Plot.Builder newBuilder() {
        return Plot.builder();
    }
}
