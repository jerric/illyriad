package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Point;
import info.lliira.illyriad.map.entity.Progress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProgressTable extends LocationTable<Progress, Progress.Builder> {
  private static final List<Field<Progress, Progress.Builder, ?>> DATA_FIELDS =
      List.of(
          new Field<>(
              "last_updated",
              FieldType.TIMESTAMP,
              p -> p.lastUpdated,
              Progress.Builder::lastUpdated));

  private final PreparedStatement selectRecentStatement;
  private final PreparedStatement deleteExpiredStatement;

  ProgressTable(Connection connection) {
    super(connection, "crawl_progresses", DATA_FIELDS);
    try {
      selectRecentStatement =
          connection.prepareStatement("SELECT * FROM crawl_progresses WHERE last_updated > ?");
      deleteExpiredStatement =
          connection.prepareStatement("DELETE FROM crawl_progresses WHERE last_updated <= ?");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Progress.Builder newBuilder() {
    return new Progress.Builder();
  }

  public Timestamp getCutoffTimestamp(long intervalMinutes) {
    return Timestamp.from(Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(intervalMinutes)));
  }

  public Set<Point> selectRecentProgresses(Timestamp cutoffTimestamp) throws SQLException {
    selectRecentStatement.setTimestamp(1, cutoffTimestamp);
    Set<Point> recentProgresses = new HashSet<>();
    try (ResultSet resultSet = selectRecentStatement.executeQuery()) {
      var iterator = new ResultSetIterator<>(resultSet, this::convert);
      while (iterator.hasNext()) {
        recentProgresses.add(iterator.next());
      }
    }
    return Set.copyOf(recentProgresses);
  }

  public void deleteExpiredProgresses(Timestamp cutoffTimestamp) throws SQLException {
    deleteExpiredStatement.setTimestamp(1, cutoffTimestamp);
    deleteExpiredStatement.executeUpdate();
  }
}
