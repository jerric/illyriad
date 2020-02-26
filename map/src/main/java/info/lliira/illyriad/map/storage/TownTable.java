package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.Town;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class TownTable extends LocationTable<Town, Town.Builder> {

    private static final ImmutableList<Field<Town, Town.Builder, ?>> DATA_FIELDS = ImmutableList.of(
            new Field<>("id",  FieldType.INT, Town::getId, Town.Builder::setId),
            new Field<>("name", FieldType.STRING, Town::getName, Town.Builder::setName),
            new Field<>("owner_id", FieldType.INT, Town::getOwnerId, Town.Builder::setOwnerId),
            new Field<>("owner_name", FieldType.STRING, Town::getOwnerName, Town.Builder::setOwnerName),
            new Field<>("population", FieldType.INT, Town::getPopulation, Town.Builder::setPopulation),
            new Field<>("alliance", FieldType.STRING, Town::getAlliance, Town.Builder::setAlliance),
            new Field<>("region", FieldType.REGION, Town::getRegion, Town.Builder::setRegion),
            new Field<>("race", FieldType.RACE, Town::getRace, Town.Builder::setRace),
            new Field<>("capital", FieldType.BOOLEAN, Town::isCapital, Town.Builder::setCapital),
            new Field<>("protection", FieldType.BOOLEAN, Town::isProtection, Town.Builder::setProtection),
            new Field<>("misc1", FieldType.BOOLEAN, Town::isMisc1, Town.Builder::setMisc1),
            new Field<>("abandoned", FieldType.BOOLEAN, Town::isAbandoned, Town.Builder::setAbandoned),
            new Field<>("data", FieldType.STRING, Town::getData, Town.Builder::setData));

    private final PreparedStatement selectValidStatement;

    TownTable(Connection connection) throws SQLException {
        super(connection, "towns", DATA_FIELDS);
        this.selectValidStatement = connection.prepareStatement(
                "SELECT * FROM towns WHERE abandoned = false AND population > 0");
    }

    @Override
    public Town.Builder newBuilder() {
        return Town.builder();
    }

    public Iterator<Town> selectValidTowns() throws SQLException {
        ResultSet resultSet = selectValidStatement.executeQuery();
        return new ResultSetIterator<>(resultSet, this::convert);
    }
}
