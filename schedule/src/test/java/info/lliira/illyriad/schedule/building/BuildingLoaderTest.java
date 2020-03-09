package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.schedule.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildingLoaderTest {

  @Test
  public void loadBuilding() {
    var buildingLoader = new BuildingLoader(TestHelper.AUTHENTICATOR);
    for (int i = 1; i <= 25; i++) {
      var building = buildingLoader.load(true, i);
      assertFalse(building.name.isBlank());
      assertTrue(building.level >= 0);
      assertTrue(building.woodCost > 0);
      assertTrue(building.clayCost > 0);
      assertTrue(building.ironCost > 0);
      assertTrue(building.stoneCost > 0);
      if (building.upgrading) {
        assertEquals(building.level + 2, building.nextLevel);
      } else {
        assertEquals(building.level + 1, building.nextLevel);
      }
      assertTrue(building.time.getSeconds() > 0);
      assertEquals(4, building.upgradeFields.size());
    }
  }
}
