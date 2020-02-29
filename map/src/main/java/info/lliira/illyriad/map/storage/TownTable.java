package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Town;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class TownTable extends LocationTable<Town, Town.Builder> {

  private static final List<Field<Town, Town.Builder, ?>> DATA_FIELDS =
      List.of(
          new Field<>("id", FieldType.INT, t -> t.id, Town.Builder::id),
          new Field<>("name", FieldType.STRING, t -> t.name, Town.Builder::name),
          new Field<>("owner_id", FieldType.INT, t -> t.ownerId, Town.Builder::ownerId),
          new Field<>("owner_name", FieldType.STRING, t -> t.ownerName, Town.Builder::ownerName),
          new Field<>("population", FieldType.INT, t -> t.population, Town.Builder::population),
          new Field<>("alliance", FieldType.STRING, t -> t.alliance, Town.Builder::alliance),
          new Field<>("region", FieldType.REGION, t -> t.region, Town.Builder::region),
          new Field<>("race", FieldType.RACE, t -> t.race, Town.Builder::race),
          new Field<>("capital", FieldType.BOOLEAN, t -> t.capital, Town.Builder::capital),
          new Field<>("protection", FieldType.BOOLEAN, t -> t.protection, Town.Builder::protection),
          new Field<>("misc1", FieldType.BOOLEAN, t -> t.misc1, Town.Builder::misc1),
          new Field<>("abandoned", FieldType.BOOLEAN, t -> t.abandoned, Town.Builder::abandoned),
          new Field<>("data", FieldType.STRING, t -> t.data, Town.Builder::data));

  private final PreparedStatement selectValidStatement;

  TownTable(Connection connection) {
    super(connection, "towns", DATA_FIELDS);
    try {
      this.selectValidStatement =
          connection.prepareStatement(
              "SELECT * FROM towns WHERE abandoned = false AND population > 0");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Town.Builder newBuilder() {
    return new Town.Builder();
  }

  public Iterator<Town> selectValidTowns() throws SQLException {
    ResultSet resultSet = selectValidStatement.executeQuery();
    return new ResultSetIterator<>(resultSet, this::convert);
  }
}
