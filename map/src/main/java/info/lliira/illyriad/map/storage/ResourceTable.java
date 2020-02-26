package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.Location;
import net.lliira.illyriad.map.model.Resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

public class ResourceTable extends LocationTable<Resource, Resource.Builder> {

    private static final ImmutableList<Field<Resource, Resource.Builder, ?>> DATA_FIELDS = ImmutableList.of(
            new Field<>("type", FieldType.RESOURCE_TYPE, Resource::getType, Resource.Builder::setType),
            new Field<>("rd", FieldType.STRING, Resource::getRd, Resource.Builder::setRd),
            new Field<>("r", FieldType.INT, Resource::getR, Resource.Builder::setR));

    ResourceTable(Connection connection) throws SQLException {
        super(connection, "resources", DATA_FIELDS);
    }

    @Override
    public Resource.Builder newBuilder() {
        return Resource.builder();
    }
}
