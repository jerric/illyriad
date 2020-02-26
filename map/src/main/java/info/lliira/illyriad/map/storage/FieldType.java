package info.lliira.illyriad.map.storage;

import net.lliira.illyriad.map.model.DepositType;
import net.lliira.illyriad.map.model.Race;
import net.lliira.illyriad.map.model.Region;
import net.lliira.illyriad.map.model.ResourceType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

class FieldType<T> {

    static final FieldType<Integer> INT = new FieldType<>(PreparedStatement::setInt, ResultSet::getInt);

    static final FieldType<Boolean> BOOLEAN = new FieldType<>(PreparedStatement::setBoolean, ResultSet::getBoolean);

    static final FieldType<String> STRING = new FieldType<>(PreparedStatement::setString, ResultSet::getString);

    static final FieldType<Date> TIMESTAMP = new FieldType<>(
            (statement, index, value) -> statement.setTimestamp(index, new Timestamp(value.getTime())),
            ResultSet::getTimestamp);

    static final FieldType<Race> RACE = new FieldType<>(
            (statement, index, value) -> statement.setInt(index, value.getCode()),
            (resultSet, fieldName) -> Race.valueOf(resultSet.getInt(fieldName)));

    static final FieldType<Region> REGION = new FieldType<>(
            (statement, index, value) -> statement.setInt(index, value.getCode()),
            (resultSet, fieldName) -> Region.valueOf(resultSet.getInt(fieldName)));

    static final FieldType<ResourceType> RESOURCE_TYPE = new FieldType<>(
            (statement, index, value) -> statement.setInt(index, value.getCode()),
            (resultSet, fieldName) -> ResourceType.valueOf(resultSet.getInt(fieldName)));

    static final FieldType<DepositType> DEPOSIT_TYPE = new FieldType<>(
            (statement, index, value) -> statement.setInt(index, value.getCode()),
            (resultSet, fieldName) -> DepositType.valueOf(resultSet.getInt(fieldName)));

    private final FieldSetter<T> setter;
    private final FieldGetter<T> getter;

    private FieldType(FieldSetter<T> setter, FieldGetter<T> getter) {
        this.setter = setter;
        this.getter = getter;
    }

    private interface FieldSetter<T> {
        void set(PreparedStatement statement, int index, T value) throws SQLException;
    }

    private interface FieldGetter<T> {
        T get(ResultSet resultSet, String fieldName) throws SQLException;
    }

    T get(ResultSet resultSet, String fieldName) {
        try {
            return getter.get(resultSet, fieldName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void set(PreparedStatement statement, int index, T value) {
        try {
            setter.set(statement, index, value);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
