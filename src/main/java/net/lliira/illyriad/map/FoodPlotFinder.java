package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.Point;
import net.lliira.illyriad.map.model.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Find the nearby food plots from a given coordinate.
 *
 * It expects {@value TOP_K_KEY} from the properties.
 */
public class FoodPlotFinder {

    private static final String TOP_K_KEY = "find.top.k";

    private static final int RANGE = 20;

    private static final Logger log = LoggerFactory.getLogger(FoodPlotFinder.class);

    private final Storage mStorage;
    private final int mTopK;

    public FoodPlotFinder(Properties properties, Storage storage) {
        mStorage = storage;
        mTopK = Integer.valueOf(properties.getProperty(TOP_K_KEY));
    }

    public List<Vector> find(Point center) throws SQLException {
        // look in the initial region, which is a square centered around the given point
        int totalRange = RANGE;
        log.info("Seaching food plots at {}, range={}", center, totalRange);
        Point min = Point.of(center.mX - RANGE, center.mY - RANGE);
        Point max = Point.of(center.mX + RANGE, center.mY + RANGE);
        List<Vector> foodPlots = searchRegion(min, max);
        Collections.sort(foodPlots);

        while (foodPlots.size() < mTopK || foodPlots.get(mTopK - 1).mDistance > totalRange) {
            totalRange += RANGE;
            log.info("Seaching food plots at {}, range={}", center, totalRange);
            Point topMin = Point.of(min.mX - RANGE, min.mY - RANGE);
            Point bottomMax = Point.of(max.mX + RANGE, max.mY + RANGE);

            // search in the top band
            foodPlots.addAll(searchRegion(topMin, Point.of(bottomMax.mX, min.mY)));
            // search in the bottom band
            foodPlots.addAll(searchRegion(Point.of(topMin.mX, max.mY), bottomMax));
            // search in the left band
            foodPlots.addAll(searchRegion(Point.of(topMin.mX, min.mY), Point.of(min.mX, max.mY)));
            // search in the right band
            foodPlots.addAll(searchRegion(Point.of(max.mX, min.mY), Point.of(bottomMax.mX, max.mY)));

            Collections.sort(foodPlots);

            min = topMin;
            max = bottomMax;
        }

        // remove the extra plots
        while (foodPlots.size() > mTopK) {
            foodPlots.remove(foodPlots.size() - 1);
        }

        return foodPlots;
    }

    private List<Vector> searchRegion(Point min, Point max) throws SQLException {
        if (max.mX < Constants.MIN_X || min.mX > Constants.MAX_X
                || max.mY < Constants.MIN_Y || min.mY > Constants.MAX_Y) {
            return new ArrayList<>();
        }

        List<Vector> foodPlots = mStorage.query("SELECT p.x, p.y FROM plots p, valid_plots vp " +
                "  WHERE p.x = vp.x AND p.y = vp.y AND p.total = 25 AND p.food = 7 " +
                "    AND vp.x >= ? AND vp.x <= ? AND vp.y >= ? AND vp.y <= ? ",
                (ps, points) -> {
                    ps.setInt(1, points[0].mX);
                    ps.setInt(2, points[1].mX);
                    ps.setInt(3, points[0].mY);
                    ps.setInt(4, points[1].mY);
                    return true;
                },
                new Point[]{min, max},
                (rs) -> {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    double distance = Math.sqrt(x *x + y * y);
                    return new Vector(x, y, distance);
                });

        return foodPlots;
    }
}
