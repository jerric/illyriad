package info.lliira.illyriad.map.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class StorageFactory {

    private static final String DB_DRIVER_KEY = "db.driver";
    private static final String DB_CONNECTION_STRING_KEY = "db.connection";
    private static final String DB_USERNAME_KEY = "db.username";
    private static final String DB_PASSWORD_KEY = "db.password";

    private final Connection connection;
    private final CreatureTable creatureTable;
    private final DepositTable depositTable;
    private final PlotTable plotTable;
    private final ProgressTable progressTable;
    private final ResourceTable resourceTable;
    private final TownTable townTable;
    private final ValidPlotTable validPlotTable;

    public StorageFactory(Properties properties)  {
        this(open(properties));
    }

    private static Connection open(Properties properties) {
        String connectionString = properties.getProperty(DB_CONNECTION_STRING_KEY);
        String username = properties.getProperty(DB_USERNAME_KEY);
        String password = properties.getProperty(DB_PASSWORD_KEY);
        String dbDriver = properties.getProperty(DB_DRIVER_KEY);
        try {
            Class.forName(dbDriver);
            return DriverManager.getConnection(connectionString, username, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public StorageFactory(Connection connection) {
        this.connection = connection;
        creatureTable = new CreatureTable(connection);
        depositTable = new DepositTable(connection);
        plotTable = new PlotTable(connection);
        progressTable = new ProgressTable(connection);
        resourceTable = new ResourceTable(connection);
        townTable = new TownTable(connection);
        validPlotTable = new ValidPlotTable(connection);
    }

    public Connection connection() {
        return connection;
    }

    public CreatureTable creatureTable() {
        return new CreatureTable(connection);
    }

    public DepositTable depositTable() {
        return new DepositTable(connection);
    }

    public PlotTable plotTable() {
        return new PlotTable(connection);
    }

    public ProgressTable progressTable() {
        return new ProgressTable(connection);
    }

    public ResourceTable resourceTable() {
        return new ResourceTable(connection);
    }

    public TownTable townTable() {
        return new TownTable(connection);
    }

    public ValidPlotTable validPlotTable() {
        return new ValidPlotTable(connection);
    }
}
