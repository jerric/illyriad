package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Plot;

import java.sql.Connection;
import java.util.List;

public class PlotTable extends LocationTable<Plot, Plot.Builder> {
  private static final List<Field<Plot, Plot.Builder, ?>> DATA_FIELDS =
      List.of(
          new Field<>("wood", FieldType.INT, p -> p.wood, Plot.Builder::wood),
          new Field<>("clay", FieldType.INT, p -> p.clay, Plot.Builder::clay),
          new Field<>("iron", FieldType.INT, p -> p.iron, Plot.Builder::iron),
          new Field<>("stone", FieldType.INT, p -> p.stone, Plot.Builder::stone),
          new Field<>("food", FieldType.INT, p -> p.food, Plot.Builder::food),
          new Field<>("background", FieldType.INT, p -> p.background, Plot.Builder::background),
          new Field<>("plot_type", FieldType.STRING, p -> p.type, Plot.Builder::type),
          new Field<>("layer", FieldType.INT, p -> p.layer, Plot.Builder::layer),
          new Field<>("region", FieldType.REGION, p -> p.region, Plot.Builder::region),
          new Field<>("sovable", FieldType.BOOLEAN, p -> p.sovable, Plot.Builder::sovable),
          new Field<>("passable", FieldType.BOOLEAN, p -> p.passable, Plot.Builder::passable),
          new Field<>("hospital", FieldType.BOOLEAN, p -> p.hospital, Plot.Builder::hospital),
          new Field<>("npc", FieldType.BOOLEAN, p -> p.npc, Plot.Builder::npc),
          new Field<>("brg", FieldType.BOOLEAN, p -> p.brg, Plot.Builder::brg));

  PlotTable(Connection connection)  {
    super(connection, "plots", DATA_FIELDS);
  }

  @Override
  public Plot.Builder newBuilder() {
    return new Plot.Builder();
  }
}
