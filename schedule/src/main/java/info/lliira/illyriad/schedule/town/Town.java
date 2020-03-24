package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.schedule.product.Product;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Town {
  public final Progress progress;
  public final Map<Integer, TownEntity> towns = new LinkedHashMap<>();
  public final Map<Resource.Type, Resource> resources = new LinkedHashMap<>();
  public final Map<Product.Type, Product> products = new LinkedHashMap<>();

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

  public Town add(Product product) {
    products.put(product.type, product);
    return this;
  }

  public Optional<TownEntity> current() {
    for (TownEntity townEntity : towns.values()) {
      if (townEntity.current) return Optional.of(townEntity);
    }
    return Optional.empty();
  }

  @Override
  public String toString() {
    return String.format(
        "Town#%d: %s %s %s %s %s %s",
        current().orElse(new TownEntity(0, "N/A", true)).id,
        resources.get(Resource.Type.Gold),
        resources.get(Resource.Type.Wood),
        resources.get(Resource.Type.Clay),
        resources.get(Resource.Type.Iron),
        resources.get(Resource.Type.Stone),
        resources.get(Resource.Type.Food));
  }
}
