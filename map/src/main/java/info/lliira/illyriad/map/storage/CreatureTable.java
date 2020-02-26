package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.Creature;

import java.sql.Connection;
import java.sql.SQLException;

public class CreatureTable extends LocationTable<Creature, Creature.Builder> {

    private static final ImmutableList<Field<Creature, Creature.Builder, ?>> DATA_FIELDS = ImmutableList.of(
            new Field<>("id", FieldType.STRING, Creature::getId, Creature.Builder::setId),
            new Field<>("name", FieldType.STRING, Creature::getName, Creature.Builder::setName),
            new Field<>("amount", FieldType.STRING, Creature::getAmount, Creature.Builder::setAmount));

    CreatureTable(Connection connection) throws SQLException {
        super(connection, "creatures", DATA_FIELDS);
    }

    @Override
    public Creature.Builder newBuilder() {
        return Creature.builder();
    }
}
