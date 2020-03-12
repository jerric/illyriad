package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.Resource;

import java.sql.Connection;
import java.util.List;

public class ResourceTable extends LocationTable<Resource, Resource.Builder> {
  private static final List<Field<Resource, Resource.Builder, ?>> DATA_FIELDS =
      List.of(
          new Field<>("type", FieldType.RESOURCE_TYPE, r -> r.type, Resource.Builder::type),
          new Field<>("rd", FieldType.STRING, r -> r.rd, Resource.Builder::rd),
          new Field<>("r", FieldType.INT, r -> r.r, Resource.Builder::r));

  ResourceTable(Connection connection) {
    super(connection, "resources", DATA_FIELDS);
  }

  @Override
  public Resource.Builder newBuilder() {
    return new Resource.Builder();
  }
}
