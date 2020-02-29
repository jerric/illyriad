package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public abstract class Table<E extends Entity<B>, B extends Entity.Builder<E>> {
  private final List<Field<E, B, ?>> primaryFields;
  private final List<Field<E, B, ?>> dataFields;
  private final PreparedStatement insertStatement;
  private final PreparedStatement updateStatement;
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
      insertStatement = createInsertStatement(connection, tableName);
      updateStatement = createUpdateStatement(connection, tableName);
      deleteStatement = createDeleteStatement(connection, tableName);
      selectStatement = createSelectStatement(connection, tableName);
      selectAllStatement = connection.prepareStatement("SELECT * FROM " + tableName);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private PreparedStatement createInsertStatement(Connection connection, String tableName)
      throws SQLException {
    StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
    // put in primary fields
    boolean first = true;
    for (Field<E, B, ?> field : primaryFields) {
      if (first) {
        first = false;
      } else {
        sql.append(", ");
      }
      sql.append(field.getName());
    }
    // put in data fields
    for (Field<E, B, ?> field : dataFields) {
      sql.append(", ").append(field.getName());
    }
    sql.append(") VALUES (");
    first = true;
    for (int i = 1; i <= primaryFields.size(); i++) {
      if (first) {
        first = false;
      } else {
        sql.append(", ");
      }
      sql.append("?");
    }
    for (int i = 1; i <= dataFields.size(); i++) {
      sql.append(", ").append("?");
    }
    sql.append(")");
    return connection.prepareStatement(sql.toString());
  }

  private PreparedStatement createUpdateStatement(Connection connection, String tableName)
      throws SQLException {
    if (dataFields.isEmpty()) {
      return null;
    }

    StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
    // put in data fields
    int count = 0;
    for (Field<E, B, ?> field : dataFields) {
      count++;
      sql.append(field.getName()).append(" = ?").append(count == dataFields.size() ? " " : ", ");
    }
    count = 0;
    for (Field<E, B, ?> field : primaryFields) {
      sql.append(count == 0 ? " WHERE " : " AND ").append(field.getName()).append(" = ?");
      count++;
    }
    return connection.prepareStatement(sql.toString());
  }

  private PreparedStatement createDeleteStatement(Connection connection, String tableName)
      throws SQLException {
    StringBuilder sql = new StringBuilder("DELETE FROM ").append(tableName);
    int count = 0;
    for (Field<E, B, ?> field : primaryFields) {
      sql.append(count == 0 ? " WHERE " : " AND ").append(field.getName()).append(" = ?");
      count++;
    }
    return connection.prepareStatement(sql.toString());
  }

  private PreparedStatement createSelectStatement(Connection connection, String tableName)
      throws SQLException {
    StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
    int count = 0;
    for (Field<E, B, ?> field : primaryFields) {
      sql.append(count == 0 ? " WHERE " : " AND ").append(field.getName()).append(" = ?");
      count++;
    }
    return connection.prepareStatement(sql.toString());
  }

  public void insertOrUpdate(E entity) {
    if (select(entity).isPresent()) {
      update(entity);
    } else {
      insert(entity);
    }
  }

  public void insert(E entity) {
    setInsertParams(entity);
    try {
      insertStatement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void addInsertBatch(E entity) {
    setInsertParams(entity);
    try {
      insertStatement.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeInsertBatch() {
    try {
      insertStatement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void setInsertParams(E entity) {
    int index = 1;
    // set primary fields
    for (Field<E, B, ?> field : primaryFields) {
      field.write(insertStatement, index, entity);
      index++;
    }
    // set data fields
    for (Field<E, B, ?> field : dataFields) {
      field.write(insertStatement, index, entity);
      index++;
    }
  }

  public void update(E entity) {
    setUpdateParams(entity);
    try {
      updateStatement.execute();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void addUpdateBatch(E entity) {
    setUpdateParams(entity);
    try {
      updateStatement.addBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void executeUpdateBatch() {
    try {
      updateStatement.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void setUpdateParams(E entity) {
    int index = 1;
    // set data fields
    for (Field<E, B, ?> field : dataFields) {
      field.write(updateStatement, index, entity);
      index++;
    }
    // set primary fields
    for (Field<E, B, ?> field : primaryFields) {
      field.write(updateStatement, index, entity);
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
