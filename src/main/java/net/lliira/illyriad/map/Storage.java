package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.Town;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The Storage acts as a generic inteface to save/delete data into SQL database, and query from the SQL database.
 */
public class Storage {

    public interface ParameterMapper<P> {
        /**
         * Map the params object into the parameters of the preparedStatement.
         *
         * @return  returns true if the mapping is successful; if it returns false, the mapping is failed, and the
         *          prepared statement won't be executed for these params.
         */
        boolean map(PreparedStatement preparedStatement, P params) throws SQLException;
    }

    public interface ResultMapper<R> {
        R map(ResultSet resultSet) throws SQLException;
    }

    private static final Logger LOG = LoggerFactory.getLogger(Storage.class);

    private final ConnectionFactory mConnectionFactory;

    public Storage(ConnectionFactory connectionFactory) {
        mConnectionFactory = connectionFactory;
    }

    public <P> int update(String sql, ParameterMapper<P> parameterMapper, P params) throws SQLException {
        try (PreparedStatement preparedStatement = mConnectionFactory.getConnection().prepareStatement(sql)) {
            if (parameterMapper.map(preparedStatement, params)) {
                return preparedStatement.executeUpdate();
            } else {
                return 0;
            }
        }
    }

    public <P> int batchUpdate(String sql, ParameterMapper<P> parameterMapper, Iterator<P> iterator, int batchSize)
            throws SQLException {
        int sum = 0;
        int count = 0;
        try (PreparedStatement preparedStatement = mConnectionFactory.getConnection().prepareStatement(sql)) {
            while (iterator.hasNext()) {
                if (parameterMapper.map(preparedStatement, iterator.next())) {
                    preparedStatement.addBatch();
                    count++;
                    if (count % batchSize == 0) {
                        sum += Arrays.stream(preparedStatement.executeBatch()).sum();
                    }
                }
            }
            if (count > 0) {
                sum += Arrays.stream(preparedStatement.executeBatch()).sum();
            }
            LOG.trace("total {} modification ops executed in the batch.", count);
        }
        return sum;
    }

    public <R> List<R> query(String sql, ResultMapper<R> resultMapper) throws SQLException {
        return query(sql, null, null, resultMapper);
    }

    public <R, P> List<R> query(String sql, ParameterMapper<P> parameterMapper, P params, ResultMapper<R> resultMapper)
            throws SQLException {
        List<R> results = new ArrayList<>();
        try (PreparedStatement preparedStatement = mConnectionFactory.getConnection().prepareStatement(sql)) {
            if (parameterMapper == null || parameterMapper.map(preparedStatement, params)) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        R result = resultMapper.map(resultSet);
                        results.add(result);
                    }
                }
            }
        }
        return results;
    }

    PreparedStatement prepareStatement(String sql) throws SQLException {
        return mConnectionFactory.getConnection().prepareStatement(sql);
    }
}

