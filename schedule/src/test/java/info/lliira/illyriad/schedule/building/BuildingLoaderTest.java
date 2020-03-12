package info.lliira.illyriad.schedule.building;

import info.lliira.illyriad.schedule.TestHelper;
import info.lliira.illyriad.schedule.town.Resource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildingLoaderTest {

  private BuildingLoader      buildingLoader = new BuildingLoader(TestHelper.AUTHENTICATOR);

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
      assertTrue(building.time.getSeconds() > 0);
      assertEquals(4, building.upgradeFields.size());
    }
  }

  @Test
  public void loadMinLandBuildings() {
    var buildings = buildingLoader.loadMinLandBuildings();
    assertEquals(5, buildings.size());
    assertEquals(Building.Type.Lumberjack, buildings.get(Resource.Type.Wood).type);
    assertEquals(Building.Type.ClayPit, buildings.get(Resource.Type.Clay).type);
    assertEquals(Building.Type.IronMine, buildings.get(Resource.Type.Iron).type);
    assertEquals(Building.Type.Quarry, buildings.get(Resource.Type.Stone).type);
    assertEquals(Building.Type.Farmyard, buildings.get(Resource.Type.Food).type);
  }
}
