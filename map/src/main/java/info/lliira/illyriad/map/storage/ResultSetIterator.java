package info.lliira.illyriad.map.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.function.Function;

public class ResultSetIterator<E> implements Iterator<E> {
    private final ResultSet resultSet;
    private Function<ResultSet, E> converter;
    private E next;

    public ResultSetIterator(ResultSet resultSet, Function<ResultSet, E> converter) {
        this.resultSet = resultSet;
        this.converter = converter;
        this.next = moveToNext();
    }

    @Override
    public synchronized boolean hasNext() {
        return next != null;
    }

    @Override
    public synchronized E next() {
        E entity = next;
        next = moveToNext();
        return entity;
    }

    private E moveToNext() {
        try {
            if (!resultSet.isClosed() && resultSet.next()) {
                return converter.apply(resultSet);
            } else {
                resultSet.close();
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
