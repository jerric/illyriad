package info.lliira.illyriad.map.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.function.BiConsumer;
import java.util.function.Function;

class Field<E extends Entity<E>, B extends Entity.Builder<E>, T> {

    private final String name;
    private final FieldType<T> type;
    private final Function<E, T> getter;
    private final BiConsumer<B, T> setter;

    Field(String name, FieldType<T> type, Function<E, T> getter, BiConsumer<B, T> setter) {
        this.name = name;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    String getName() {
        return name;
    }

    void read(ResultSet resultSet, B builder) {
        T value = type.get(resultSet, name);
        setter.accept(builder, value);
    }

    void write(PreparedStatement statement, int index, E entity) {
        T value = getter.apply(entity);
        type.set(statement, index, value);
    }
}
