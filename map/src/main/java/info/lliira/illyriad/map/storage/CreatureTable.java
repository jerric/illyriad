package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Creature;

import java.sql.Connection;
import java.util.List;

public class CreatureTable extends LocationTable<Creature, Creature.Builder> {
  private static final List<Field<Creature, Creature.Builder, ?>> DATA_FIELDS =
      List.of(
          new Field<>("id", FieldType.STRING, c -> c.id, Creature.Builder::id),
          new Field<>("name", FieldType.STRING, c -> c.name, Creature.Builder::name),
          new Field<>("amount", FieldType.STRING, c -> c.amount, Creature.Builder::amount));

  CreatureTable(Connection connection) {
    super(connection, "creatures", DATA_FIELDS);
  }

  @Override
  public Creature.Builder newBuilder() {
    return new Creature.Builder();
  }
}
