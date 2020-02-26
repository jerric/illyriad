package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.Deposit;

import java.sql.Connection;
import java.sql.SQLException;

public class DepositTable extends LocationTable<Deposit, Deposit.Builder> {

    private static final String TABLE_NAME = "deposits";
    private static final ImmutableList<Field<Deposit, Deposit.Builder, ?>> PRIMARY_FIELDS = ImmutableList.of(
            new Field<>("x", FieldType.INT, Deposit::getX, Deposit.Builder::setX),
            new Field<>("y", FieldType.INT, Deposit::getY, Deposit.Builder::setY),
            new Field<>("type", FieldType.DEPOSIT_TYPE, Deposit::getType, Deposit.Builder::setType));

    DepositTable(Connection connection) throws SQLException {
        super(connection,
                TABLE_NAME,
                PRIMARY_FIELDS,
                ImmutableList.of());
    }

    @Override
    public Deposit.Builder newBuilder() {
        return Deposit.builder();
    }
}
