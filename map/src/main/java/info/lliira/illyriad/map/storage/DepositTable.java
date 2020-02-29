package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Deposit;

import java.sql.Connection;
import java.util.List;

public class DepositTable extends LocationTable<Deposit, Deposit.Builder> {
  private static final String TABLE_NAME = "deposits";
  private static final List<Field<Deposit, Deposit.Builder, ?>> PRIMARY_FIELDS =
      List.of(
          new Field<>("x", FieldType.INT, d -> d.x, Deposit.Builder::x),
          new Field<>("y", FieldType.INT, d -> d.y, Deposit.Builder::y),
          new Field<>("type", FieldType.DEPOSIT_TYPE, d -> d.type, Deposit.Builder::type));

  DepositTable(Connection connection) {
    super(connection, TABLE_NAME, PRIMARY_FIELDS, List.of());
  }

  @Override
  public Deposit.Builder newBuilder() {
    return new Deposit.Builder();
  }
}
