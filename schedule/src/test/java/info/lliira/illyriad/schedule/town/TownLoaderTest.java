package info.lliira.illyriad.schedule.town;

import info.lliira.illyriad.schedule.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TownLoaderTest {

  private TownLoader loader = new TownLoader(TestHelper.AUTHENTICATOR);

  @Test
  public void loadTowns() {
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

  @Test
  void loadTownInfo() {
    var townInfo = loader.loadTownInfo();
    assertTrue(townInfo.capacity > 0);
    assertFalse(townInfo.cityName.isBlank());
    assertFalse(townInfo.founded.isBlank());
    assertFalse(townInfo.location.isBlank());
    assertTrue(townInfo.population > 0);
    assertNotNull(townInfo.race);
    assertFalse(townInfo.size.isBlank());
  }
}
