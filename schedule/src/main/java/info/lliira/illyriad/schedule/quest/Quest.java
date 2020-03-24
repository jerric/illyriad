package info.lliira.illyriad.schedule.quest;

import info.lliira.illyriad.schedule.product.Product;
import info.lliira.illyriad.schedule.town.Resource;

import java.util.Map;

public class Quest {
  public final int id;
  public final int beerNeeded;
  public final Map<Resource.Type, Integer> resourcesNeeded;
  public final Map<Product.Type, Integer> productsNeeds;

  public Quest(
      int id,
      int beerNeeded,
      Map<Resource.Type, Integer> resourcesNeeded,
      Map<Product.Type, Integer> productsNeeds) {
    this.id = id;
    this.beerNeeded = beerNeeded;
    this.resourcesNeeded = resourcesNeeded;
    this.productsNeeds = productsNeeds;
  }
}
