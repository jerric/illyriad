package info.lliira.illyriad.map.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class StorageFactory {

    private static final String DB_DRIVER_KEY = "db.driver";
    private static final String DB_CONNECTION_STRING_KEY = "db.connection";
    private static final String DB_USERNAME_KEY = "db.username";
    private static final String DB_PASSWORD_KEY = "db.password";

    private final Connection connection;

    public StorageFactory(Properties properties) throws ClassNotFoundException, SQLException {
        String connectionString = properties.getProperty(DB_CONNECTION_STRING_KEY);
        String username = properties.getProperty(DB_USERNAME_KEY);
        String password = properties.getProperty(DB_PASSWORD_KEY);

        String dbDriver = properties.getProperty(DB_DRIVER_KEY);
        Class.forName(dbDriver);

        connection = DriverManager.getConnection(connectionString, username, password);
    }

    public StorageFactory(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public CreatureTable getCreatureTable() throws SQLException {
        return new CreatureTable(connection);
    }

    public DepositTable getDepositTable() throws SQLException {
        return new DepositTable(connection);
    }

    public PlotTable getPlotTable() throws SQLException {
        return new PlotTable(connection);
    }

    public ProgressTable getProgressTable() throws SQLException {
        return new ProgressTable(connection);
    }

    public ResourceTable getResourceTable() throws SQLException {
        return new ResourceTable(connection);
    }

    public TownTable getTownTable() throws SQLException {
        return new TownTable(connection);
    }

    public ValidPlotTable getValidPlotTable() throws SQLException {
        return new ValidPlotTable(connection);
    }
}
