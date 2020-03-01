package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class Table<E extends Entity<B>, B extends Entity.Builder<E>> {
  private final List<Field<E, B, ?>> primaryFields;
  private final List<Field<E, B, ?>> dataFields;
  private final PreparedStatement upsertStatement;
  private final PreparedStatement deleteStatement;
  private final PreparedStatement selectStatement;
  private final PreparedStatement selectAllStatement;

  public abstract B newBuilder();

  Table(
      Connection connection,
      String tableName,
      List<Field<E, B, ?>> primaryFields,
      List<Field<E, B, ?>> dataFields) {
    this.primaryFields = primaryFields;
    this.dataFields = dataFields;
    try {
      upsertStatement = createUpsertStatement(connection, tableName);
      deleteStatement = createDeleteStatement(connection, tableName);
      selectStatement = createSelectStatement(connection, tableName);
      selectAllStatement = connection.prepareStatement("SELECT * FROM " + tableName);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private PreparedStatement createUpsertStatement(Connection connection, String tableName)
      throws SQLException {
    return connection.prepareStatement(
        String.format(
            "INSERT INTO %s (%s, %s) VALUES(%s, %s) ON CONFLICT DO UPDATE SET %s",
            tableName,
            concat(primaryFields, Field::name),
            concat(dataFields, Field::name),
            concat(primaryFields, field -> "?"),
            concat(dataFields, field -> "?"),
            concat(dataFields, field -> field.name() + " = EXCLUDED." + field.name())));
  }

  private PreparedStatement createDeleteStatement(Connection connection, String tableName)
      throws SQLException {
    return connection.prepareStatement(
        String.format(
            "DELETE FROM %s WHERE %s",
            tableName, concat(primaryFields, field -> field.name() + " = ?", " AND ")));
  }

  private PreparedStatement createSelectStatement(Connection connection, String tableName)
      throws SQLException {
    return connection.prepareStatement(
        String.format(
            "SELECT * FROM %s WHERE %s",
            tableName, concat(primaryFields, field -> field.name() + " = ?", " AND ")));
  }

  private String concat(List<Field<E, B, ?>> fields, Function<Field<E, B, ?>, String> handle) {
    return concat(fields, handle, ",");
  }

  private String concat(
      List<Field<E, B, ?>> fields, Function<Field<E, B, ?>, String> handle, String separator) {
    StringBuilder builder = new StringBuilder();
    fields.forEach(
        field ->
            builder.append(builder.length() == 0 ? "" : separator).append(handle.apply(field)));
    return builder.toString();
  }

  public void upsert(E entity) {
    setUpsertParams(entity);
    try {
      upsertStatement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void addUpsertBatch(E entity) {
    setUpsertParams(entity);
    try {
      upsertStatement.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int[] executeUpsertBatch() {
    try {
      return upsertStatement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void setUpsertParams(E entity) {
    int index = 1;
    // set primary fields
    for (Field<E, B, ?> field : primaryFields) {
      field.write(upsertStatement, index, entity);
      index++;
    }
    // set data fields
    for (Field<E, B, ?> field : dataFields) {
      field.write(upsertStatement, index, entity);
      index++;
    }
  }

  public void addDeleteBatch(E entity) {
    setDeleteParams(entity);
    try {
      deleteStatement.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public int[] executeDeleteBatch() {
    try {
      return deleteStatement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void delete(E entity) {
    setDeleteParams(entity);
    try {
      deleteStatement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void setDeleteParams(E entity) {
    int index = 1;
    // set primary fields
    for (Field<E, B, ?> field : primaryFields) {
      field.write(deleteStatement, index, entity);
      index++;
    }
  }

  public Optional<E> select(E entity) {
    int index = 1;
    // set primary fields
    for (Field<E, B, ?> field : primaryFields) {
      field.write(selectStatement, index, entity);
      index++;
    }
    try (ResultSet resultSet = selectStatement.executeQuery()) {
      if (resultSet.next()) {
        return Optional.of(convert(resultSet));
      } else {
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Iterator<E> selectAll() {
    try {
      ResultSet resultSet = selectAllStatement.executeQuery();
      return new ResultSetIterator<>(resultSet, this::convert);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  E convert(ResultSet resultSet) {
    B builder = newBuilder();
    // set primary fields
    for (Field<E, B, ?> field : primaryFields) {
      field.read(resultSet, builder);
    }
    // set data fields
    for (Field<E, B, ?> field : dataFields) {
      field.read(resultSet, builder);
    }
    return builder.build();
  }
}
