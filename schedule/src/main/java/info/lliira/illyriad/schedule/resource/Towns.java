package info.lliira.illyriad.schedule.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Towns {
  public final Map<Integer, Town> towns = new LinkedHashMap<>();
  public final Map<Resource.Type, Resource> resources = new LinkedHashMap<>();

  public Towns add(Town town) {
    towns.put(town.id, town);
    return this;
  }

  public Towns add(Resource resource) {
    resources.put(resource.type, resource);
    return this;
  }

  public Optional<Town> current() {
    for (Town town : towns.values()) {
      if (town.current) return Optional.of(town);
    }
    return Optional.empty();
  }
}
