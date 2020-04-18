package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.schedule.TestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildingLoaderTest {

  private final BuildingLoader buildingLoader = new BuildingLoader(TestHelper.AUTHENTICATOR);

  @Test
  public void loadBuilding() {
    for (int i = 1; i <= 25; i++) {
      var buildingOptional = buildingLoader.load(true, i);
      assertTrue(buildingOptional.isPresent());
      var building = buildingOptional.get();
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
      assertFalse(building.time.expired());
      assertEquals(4, building.upgradeFields.size());
    }
  }

  @Test
  public void loadMinLandBuildings() {
    var buildings = buildingLoader.loadMinLandBuildings();
    assertEquals(5, buildings.size());
    assertEquals(Building.Type.Lumberjack, buildings.get(Building.Type.Lumberjack).type);
    assertEquals(Building.Type.ClayPit, buildings.get(Building.Type.ClayPit).type);
    assertEquals(Building.Type.IronMine, buildings.get(Building.Type.IronMine).type);
    assertEquals(Building.Type.Quarry, buildings.get(Building.Type.Quarry).type);
    assertEquals(Building.Type.Farmyard, buildings.get(Building.Type.Farmyard).type);
  }
}
