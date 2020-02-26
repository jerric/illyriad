package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public abstract class LocationTable<E extends Location<E>, B extends Location.Builder<E>> extends Table<E, B> {

    private final PreparedStatement selectStatement;
    private final PreparedStatement deleteStatement;

    LocationTable(Connection connection, String tableName, ImmutableList<Field<E, B, ?>> dataFields)
    throws SQLException {
        this(connection,
                tableName,
                ImmutableList.of(
                        new Field<>("x", FieldType.INT, Location::getX, Location.Builder::setX),
                        new Field<>("y", FieldType.INT, Location::getY, Location.Builder::setY)),
                dataFields);
    }

    LocationTable(Connection connection, String tableName, ImmutableList<Field<E, B, ?>> primaryFields,
            ImmutableList<Field<E, B, ?>> dataFields)
    throws SQLException {
        super(connection, tableName, primaryFields, dataFields);
        this.selectStatement = createSelectStatement(connection, tableName);
        this.deleteStatement = createDeleteStatement(connection, tableName);
    }

    private PreparedStatement createSelectStatement(Connection connection, String tableName) throws SQLException {
        return connection.prepareStatement(
                "SELECT * FROM " + tableName + " WHERE x >= ? AND x <= ? AND y >= ? AND y <= ?");
    }

    private PreparedStatement createDeleteStatement(Connection connection, String tableName) throws SQLException {
        return connection.prepareStatement(
                "DELETE FROM " + tableName + " WHERE x >= ? AND x <= ? AND y >= ? AND y <= ?");
    }

    public Optional<E> select(int x, int y) throws SQLException {
        return select(newBuilder().setX(x).setY(y).build());
    }

    public synchronized Iterator<E> select(int minX, int minY, int maxX, int maxY) throws SQLException {
        selectStatement.setInt(1, minX);
        selectStatement.setInt(2, maxX);
        selectStatement.setInt(3, minY);
        selectStatement.setInt(4, maxY);
        ResultSet resultSet = selectStatement.executeQuery();
        return new ResultSetIterator<>(resultSet, this::convert);
    }

    public synchronized void delete(int minX, int minY, int maxX, int maxY) throws SQLException {
        deleteStatement.setInt(1, minX);
        deleteStatement.setInt(2, maxX);
        deleteStatement.setInt(3, minY);
        deleteStatement.setInt(4, maxY);
        deleteStatement.execute();
    }
}
