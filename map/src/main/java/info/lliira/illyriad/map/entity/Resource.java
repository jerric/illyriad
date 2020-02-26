package info.lliira.illyriad.map.entity;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class Resource extends Location<Resource.Builder> {
  public final ResourceType type;
  public final String rd;
  public final int r;

  private Resource(int x, int y, ResourceType type, String rd, int r) {
    super(x, y);
    this.type = type;
    this.rd = rd;
    this.r = r;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder extends Location.Builder<Resource> {
    private ResourceType type;
    private String rd;
    private int r;

    public Builder() {}

    private Builder(Resource resource) {
      super(resource);
      type = resource.type;
      rd = resource.rd;
      r = resource.r;
    }

    public Builder setType(ResourceType type) {
      this.type = type;
      return this;
    }

    public Builder setRd(String rd) {
      this.rd = rd;
      return this;
    }

    public Builder setR(int r) {
      this.r = r;
      return this;
    }

    @Override
    public Resource build() {
      return new Resource(x, y, type, rd, r);
    }
  }

  public enum ResourceType {
    UNKNOWN(0),
    Wood(1),
    Clay(2),
    Iron(3),
    Stone(4),
    Food(5),
    Gold(6);

    private static final Map<Integer, ResourceType> RESOURCE_TYPES =
        Arrays.stream(values()).collect(Collectors.toMap(type -> type.code, type -> type));

    public static ResourceType valueOf(int code) {
      return RESOURCE_TYPES.getOrDefault(code, UNKNOWN);
    }

    private final int code;

    ResourceType(int code) {
      this.code = code;
    }
  }
}
