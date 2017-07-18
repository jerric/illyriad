package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

/**
 * Find the closest plot of a custom condition from the given point
 */
public class ClosestPlotFinder {
    private static final Logger log = LoggerFactory.getLogger(FoodPlotFinder.class);

    private static final int SEARCH_RADIUS = 500;
    private static final int LIMIT_RADIUS = 10;

    private final Storage mStorage;
    private final TownManager mTownManager;

    public ClosestPlotFinder(Storage storage, TownManager townManager) {
        mStorage = storage;
        mTownManager = townManager;
    }

    public List<net.lliira.illyriad.map.model.Vector> findClosest(Point origin) throws SQLException {
        Map<Integer, Set<Integer>> plots = getPlots(origin);
        List<Town> towns = mTownManager.getTowns();
        limitPlots(plots, towns);
        List<net.lliira.illyriad.map.model.Vector> distances = computeDistances(plots, origin);
        distances.sort((l, r) -> (l.mDistance < r.mDistance) ? -1 : ((l.mDistance > r.mDistance) ? 1 : 0));
        return distances;
    }

    private Map<Integer, Set<Integer>> getPlots(Point origin) throws SQLException {
        List<Point> plots = mStorage.query("SELECT x, y FROM plots" +
                " WHERE x >= ? AND x <= ? AND y >= ? AND y <= ?" +
                "   AND total = 25 AND food = 7 and stone = 3",
                (ps, point) -> {
                    ps.setInt(1, point.mX - SEARCH_RADIUS);
                    ps.setInt(2, point.mX + SEARCH_RADIUS);
                    ps.setInt(3, point.mY - SEARCH_RADIUS);
                    ps.setInt(4, point.mY + SEARCH_RADIUS);
                    return true;
                },
                origin,
                rs -> Point.of(rs.getInt("x"), rs.getInt("y")));
        Map<Integer, Set<Integer>> map = new HashMap<>();
        for(Point plot : plots) {
            Set<Integer> row = map.computeIfAbsent(plot.mY, y -> new HashSet<>());
            row.add(plot.mX);
        }
        return map;
    }

    private void limitPlots(Map<Integer, Set<Integer>> plots, List<Town> towns) {
        int limitSquare = LIMIT_RADIUS * LIMIT_RADIUS;
        // remove the plots too close to any towns;
        for (Point town : towns) {
            int maxX = town.mX + LIMIT_RADIUS;
            int maxY = town.mY + LIMIT_RADIUS;
            for (int y = town.mY - LIMIT_RADIUS; y <= maxY; y++) {
                if (!plots.containsKey(y)) continue;
                int dy = (town.mY - y) * (town.mY - y);
                Set<Integer> row = plots.get(y);
                for (int x = town.mX - LIMIT_RADIUS; x <= maxX; x++) {
                    if (!row.contains(x)) continue;
                    // compute the distance square
//                    int dist = (town.mX - x) * (town.mX - x) + dy;
//                    if (dist < limitSquare) {
                        row.remove(x);
                        if (row.isEmpty()) {
                            plots.remove(y);
                        }
//                    }
                }
            }
        }
    }

    private List<net.lliira.illyriad.map.model.Vector> computeDistances(Map<Integer, Set<Integer>> plots, Point origin) {
        List<net.lliira.illyriad.map.model.Vector> distances = new ArrayList<>();
        for (Map.Entry<Integer, Set<Integer>> entry : plots.entrySet()) {
            int y = entry.getKey();
            int dy = (origin.mY - y) * (origin.mY - y);
            for (int x : entry.getValue()) {
                int distance = (origin.mX - x) * (origin.mX - x) + dy;
                distances.add(new net.lliira.illyriad.map.model.Vector(x, y, distance));
            }
        }
        return distances;
    }
}
