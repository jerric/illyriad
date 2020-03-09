package info.lliira.illyriad.schedule.resource;

import info.lliira.illyriad.schedule.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TownLoaderTest {

  @Test
  public void loadTowns() {
    var loader = new TownLoader(TestHelper.AUTHENTICATOR);
    var towns = loader.loadTown();
    assertFalse(towns.towns.isEmpty());
    assertTrue(towns.current().isPresent());
    assertEquals(8, towns.resources.size());
    for (var type : Resource.Type.values()) {
      if (type != Resource.Type.Mana) {
        var resource = towns.resources.get(type);
        assertTrue(resource.amount > 0);
        assertTrue(resource.rate > 0);
      }
    }
  }
}
