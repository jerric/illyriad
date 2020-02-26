package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.lliira.illyriad.map.model.Location;
import net.lliira.illyriad.map.model.Progress;

import java.awt.*;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ProgressTable extends LocationTable<Progress, Progress.Builder> {

    private static final ImmutableList<Field<Progress, Progress.Builder, ?>> DATA_FIELDS = ImmutableList.of(
            new Field<>("last_updated", FieldType.TIMESTAMP, Progress::getLastUpdated, Progress.Builder::setLastUpdated));

    private final PreparedStatement selectRecentStatement;
    private final PreparedStatement deleteExpiredStatement;

    ProgressTable(Connection connection) throws SQLException {
        super(connection, "crawl_progresses", DATA_FIELDS);
        selectRecentStatement = connection.prepareStatement("SELECT * FROM crawl_progresses WHERE last_updated > ?");
        deleteExpiredStatement = connection.prepareStatement("DELETE FROM crawl_progresses WHERE last_updated <= ?");
    }

    @Override
    public Progress.Builder newBuilder() {
        return Progress.builder();
    }

    public Timestamp getCutoffTimestamp(long intervalMinutes) {
        return Timestamp.from(Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(intervalMinutes)));
    }

    public Set<Point> selectRecentProgresses(Timestamp cutoffTimestamp) throws SQLException {
        selectRecentStatement.setTimestamp(1, cutoffTimestamp);
        ImmutableSet.Builder<Point> recentProgresses = ImmutableSet.builder();
        try (ResultSet resultSet = selectRecentStatement.executeQuery()) {
            var iterator = new ResultSetIterator<>(resultSet, this::convert);
            while (iterator.hasNext()) {
                recentProgresses.add(iterator.next().toPoint());
            }
        }
        return recentProgresses.build();
    }

    public void deleteExpiredProgresses(Timestamp cutoffTimestamp) throws SQLException {
        deleteExpiredStatement.setTimestamp(1, cutoffTimestamp);
        deleteExpiredStatement.executeUpdate();
    }



}
