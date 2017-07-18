package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.Point;
import net.lliira.illyriad.map.model.ResourceType;
import net.lliira.illyriad.map.model.Town;
import net.lliira.illyriad.map.model.Vector;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Test launch the activities provided by the {@link MapService}.
 */
public class MapServiceTest {

    private final MapService mMapService;

    public MapServiceTest() throws IOException {
        mMapService = new MapService();
    }

    @Test
    public void testMapCrawler() throws ClassNotFoundException, IOException {
        mMapService.getCrawlerManager().crawl();
    }

    @Test
    public void testAuthenticator() throws IOException {
        mMapService.getAuthenticator().login();
    }

    @Test
    public void testResourceAnalyzer() throws ClassNotFoundException, SQLException {
        mMapService.getResourceAnalyzer().analyze();
    }

    @Test
    public void testFindFoodPlots() throws ClassNotFoundException, SQLException {
        List<Vector> foodPlots = mMapService.getFoodPlotFinder().find(Point.of(315, 193));
        for (int i = 1; i <= foodPlots.size(); i++) {
            System.out.printf("%d - %s\n", i, foodPlots.get(i - 1));
        }
    }

    @Test
    public void testClosestPlotFinder() throws ClassNotFoundException, SQLException {
        List<Vector> plots = mMapService.getClosestPlotFinder().findClosest(Point.of(315, 193));
        for (int i = 0; i < 10 && i < plots.size(); i++) {
            System.out.printf("Closest plot: %s\n", plots.get(i));
        }
    }

    @Test
    public void testResourceFinder() throws ClassNotFoundException, SQLException {
        Map<Town, Map<ResourceType, List<Vector>>> resources =
                mMapService.getResourceFinder().findResources("Lady Simbul");
        for (Map.Entry<Town, Map<ResourceType, List<Vector>>> townEntry : resources.entrySet()) {
            System.out.printf("========== %s ==========\n", townEntry.getKey());
            for (ResourceType type : ResourceType.values()) {
                System.out.printf("  [%s]\n", type);
                for (Vector vector : townEntry.getValue().get(type)) {
                    System.out.printf("    %.3f\t(%d,%d)\n", vector.mDistance, vector.mX, vector.mY);
                }
            }
            System.out.println();
        }
    }
}
