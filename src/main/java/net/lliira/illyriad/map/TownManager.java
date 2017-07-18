package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.Town;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

/**
 * Helper class to get town related information.
 */
public class TownManager {

    private Storage mStorage;

    public TownManager(Storage storage) {
        mStorage = storage;
    }

    /**
     * Returns the towns that belong to other players. Abandoned and whitelisted users are ignored.
     */
    public List<Town> getTowns() throws SQLException {
        return mStorage.query("SELECT x, y, name, owner FROM towns " +
                        " WHERE owner NOT IN (SELECT user_name FROM whitelist_users) " +
                        "   AND owner NOT LIKE '%(Abandoned)' ",
                rs -> new Town(rs.getInt("x"), rs.getInt("y"), rs.getString("name"), rs.getString("owner")));
    }

    public Map<Integer, Set<Integer>> getOwnedPlots(int radius) throws SQLException {
        List<Town> towns = getTowns();
        Map<Integer, Set<Integer>> plots = new HashMap<>();

        Function<Integer, Set<Integer>> rowCreator = y -> new HashSet<>();

        for (Town town : towns) {
            int maxX = town.mX + radius;
            int maxY = town.mY + radius;
            for (int y = town.mY - radius; y <= maxY; y++) {
                Set<Integer> row = plots.computeIfAbsent(y, rowCreator);
                for (int x = town.mX - radius; x <= maxX; x++) {
                    row.add(x);
                }
            }
        }

        return plots;
    }

}
