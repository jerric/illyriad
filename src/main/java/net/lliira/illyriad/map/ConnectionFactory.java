package net.lliira.illyriad.map;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages the creation of Database connections.
 *
 * It expects the properties to have the following keys: {@value DB_DRIVER_KEY}, {@value DB_CONNECTION_STRING_KEY},
 * {@value DB_USERNAME_KEY}, {@value DB_PASSWORD_KEY}.
 */
public class ConnectionFactory {

    private static final String DB_DRIVER_KEY = "db.driver";
    private static final String DB_CONNECTION_STRING_KEY = "db.connection";
    private static final String DB_USERNAME_KEY = "db.username";
    private static final String DB_PASSWORD_KEY = "db.password";

    private final String mDbConnectionString;
    private final String mDbUsername;
    private final String mDbPassword;

    private Connection mConnection;

    public ConnectionFactory(Properties properties) throws ClassNotFoundException {
        mDbConnectionString = properties.getProperty(DB_CONNECTION_STRING_KEY);
        mDbUsername = properties.getProperty(DB_USERNAME_KEY);
        mDbPassword = properties.getProperty(DB_PASSWORD_KEY);

        String dbDriver = properties.getProperty(DB_DRIVER_KEY);
        Class.forName(dbDriver);
    }

    public synchronized Connection getConnection() throws SQLException {
        if (mConnection == null) {
            mConnection = DriverManager.getConnection(mDbConnectionString, mDbUsername, mDbPassword);
        }
        return mConnection;
    }

    public synchronized void closeConnection() throws SQLException {
        if (mConnection != null) {
            mConnection.close();
            mConnection = null;
        }
    }
}
