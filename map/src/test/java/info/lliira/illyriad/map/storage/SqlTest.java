package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.common.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

public class SqlTest {

  private StorageFactory storageFactory;
  private Random random;

  @BeforeEach
  public void setUp() throws IOException, SQLException {
    random = new Random();
    Properties properties = new Properties();
    properties.load(new FileReader(new File(Constants.PROPERTY_FILE)));
    storageFactory = new StorageFactory(properties);
    Statement statement = storageFactory.connection().createStatement();
    statement.executeUpdate("DELETE FROM test");
    for (int x = 0; x < 100; x++) {
      for (int y = 0; y < 100; y++) {
        statement.executeUpdate(
            String.format(
                "INSERT INTO test(x, y, content) VALUES(%d, %d, %d)", x, y, random.nextInt()));
      }
    }
  }

  @Test
  public void deleteBatchThenInsertBatch() throws SQLException {

    PreparedStatement delete =
        storageFactory.connection().prepareStatement("DELETE FROM test WHERE x= ? AND y = ?");
    PreparedStatement insert =
        storageFactory
            .connection()
            .prepareStatement("INSERT INTO test (x, y, content) VALUES (?, ?, ?)");
    for (int i = 0; i < 1000; i++) {
      int x = random.nextInt(100);
      int y = random.nextInt(100);

      delete.setInt(1, x);
      delete.setInt(2, y);
      delete.addBatch();
      delete.executeBatch();

      insert.setInt(1, x);
      insert.setInt(2, y);
      insert.setInt(3, random.nextInt());
      insert.addBatch();
      insert.executeBatch();
    }
  }
}
