package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Deposit;
import info.lliira.illyriad.map.entity.Region;
import info.lliira.illyriad.map.entity.Resource;
import info.lliira.illyriad.map.entity.Town;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

class FieldType<T> {

  static final FieldType<Integer> INT =
      new FieldType<>(PreparedStatement::setInt, ResultSet::getInt);

  static final FieldType<Boolean> BOOLEAN =
      new FieldType<>(PreparedStatement::setBoolean, ResultSet::getBoolean);

  static final FieldType<String> STRING =
      new FieldType<>(PreparedStatement::setString, ResultSet::getString);

  static final FieldType<Date> TIMESTAMP =
      new FieldType<>(
          (statement, index, value) ->
              statement.setTimestamp(index, new Timestamp(value.getTime())),
          ResultSet::getTimestamp);

  static final FieldType<Town.Race> RACE =
      new FieldType<>(
          (statement, index, value) -> statement.setInt(index, value.code),
          (resultSet, fieldName) -> Town.Race.parse(resultSet.getInt(fieldName)));

  static final FieldType<Region> REGION =
      new FieldType<>(
          (statement, index, value) -> statement.setInt(index, value.code),
          (resultSet, fieldName) -> Region.parse(resultSet.getInt(fieldName)));

  static final FieldType<Resource.ResourceType> RESOURCE_TYPE =
      new FieldType<>(
          (statement, index, value) -> statement.setInt(index, value.code),
          (resultSet, fieldName) -> Resource.ResourceType.parse(resultSet.getInt(fieldName)));

  static final FieldType<Deposit.DepositType> DEPOSIT_TYPE =
      new FieldType<>(
          (statement, index, value) -> statement.setInt(index, value.code),
          (resultSet, fieldName) -> Deposit.DepositType.parse(resultSet.getInt(fieldName)));

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
