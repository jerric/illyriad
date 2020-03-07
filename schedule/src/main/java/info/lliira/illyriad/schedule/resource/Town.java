package info.lliira.illyriad.schedule.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Town {
  public final Progress progress;
  public final Map<Integer, TownEntity> towns = new LinkedHashMap<>();
  public final Map<Resource.Type, Resource> resources = new LinkedHashMap<>();

  public Town(Progress progress) {
    this.progress = progress;
  }

  public Town add(TownEntity townEntity) {
    towns.put(townEntity.id, townEntity);
    return this;
  }

  public Town add(Resource resource) {
    resources.put(resource.type, resource);
    return this;
  }

  public Optional<TownEntity> current() {
    for (TownEntity townEntity : towns.values()) {
      if (townEntity.current) return Optional.of(townEntity);
    }
    return Optional.empty();
  }
}