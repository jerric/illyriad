package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.ResourceType;
import net.lliira.illyriad.map.model.Town;
import net.lliira.illyriad.map.model.Vector;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

/**
 * Find the resources close to the towns of a given user
 */
public class ResourceFinder {

    private static final int SEARCH_RADIUS = 1000;
    private static final int OWNER_RADIUS = 5;

    private static final String MAX_RESOURCE_COUNT_KEY = "find.max.resource.count";

    private final Storage mStorage;
    private final TownManager mTownManager;
    private final int mMaxResourceCount;

    public ResourceFinder(Properties properties, Storage storage, TownManager townManager) {
        mStorage = storage;
        mTownManager = townManager;
        mMaxResourceCount = Integer.valueOf(properties.getProperty(MAX_RESOURCE_COUNT_KEY));
    }

    public Map<Town, Map<ResourceType, List<Vector>>> findResources(String owner) throws SQLException {
        List<Town> towns = getTowns(owner);
        Map<Town, Map<ResourceType, List<Vector>>> resources = new HashMap<>(towns.size());
        for (Town town : towns) {
            Map<ResourceType, List<Vector>> resourcesPerTown = new HashMap<>();
            resourcesPerTown.putAll(findBasicResources(town));
            resourcesPerTown.putAll(findDeposits(town));
            processResources(resourcesPerTown);
            resources.put(town, resourcesPerTown);
        }
        return resources;
    }

    private List<Town> getTowns(String owner) throws SQLException {
        return mStorage.query("SELECT x, y, name FROM towns WHERE owner = ?",
                (ps, ownerName) -> {
                    ps.setString(1, ownerName);
                    return true;
                },
                owner,
                rs -> new Town(rs.getInt("x"), rs.getInt("y"), rs.getString("name"), owner));
    }

    private Map<ResourceType, List<Vector>> findBasicResources(Town town) throws SQLException {
        final Map<ResourceType, List<Vector>> resources = new HashMap<>();
        for (ResourceType type : ResourceType.BASICS) {
            resources.put(type, new ArrayList<>());
        }

        mStorage.query("SELECT x, y, type FROM resources WHERE type != '' AND x >= ? AND x <= ? AND y >= ? AND y <= ?",
                (ps, center) -> {
                    ps.setInt(1, center.mX - SEARCH_RADIUS);
                    ps.setInt(2, center.mX + SEARCH_RADIUS);
                    ps.setInt(3, center.mY - SEARCH_RADIUS);
                    ps.setInt(4, center.mY + SEARCH_RADIUS);
                    return true;
                },
                town,
                rs -> {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    ResourceType type = ResourceType.toBasicResource(rs.getString("type"));
                    double distance = Math.sqrt((town.mX - x) * (town.mX - x) + (town.mY - y) * (town.mY - y));
                    Vector vector = new Vector(x, y, distance);
                    resources.get(type).add(vector);
                    return vector;
                });

        return resources;
    }

    private Map<ResourceType, List<Vector>> findDeposits(Town town) throws SQLException {

        Map<ResourceType, List<Vector>> resources = new HashMap<>();

        Function<ResourceType, List<Vector>> resourceCreator = type -> new ArrayList<>();

        mStorage.query("SELECT * FROM deposits WHERE x >= ? AND x <= ? AND y >= ? AND y <= ?",
                (ps, center) -> {
                    ps.setInt(1, center.mX - SEARCH_RADIUS);
                    ps.setInt(2, center.mX + SEARCH_RADIUS);
                    ps.setInt(3, center.mY - SEARCH_RADIUS);
                    ps.setInt(4, center.mY + SEARCH_RADIUS);
                    return true;
                },
                town,
                rs -> {
                    int x = rs.getInt("x");
                    int y = rs.getInt("y");
                    double distance = Math.sqrt((town.mX - x) * (town.mX - x) + (town.mY - y) * (town.mY - y));
                    Vector vector = new Vector(x, y, distance);

                    for (ResourceType type : ResourceType.DEPOSITS) {
                        if (rs.getInt(type.mName) == 1) {
                            resources.computeIfAbsent(type, resourceCreator).add(vector);
                        }
                    }
                    return vector;
                });

        return resources;
    }

    private void processResources(Map<ResourceType, List<Vector>> resourcesPerTown) throws SQLException {
        Map<Integer, Set<Integer>> ownedPlots = mTownManager.getOwnedPlots(OWNER_RADIUS);
        for (List<Vector> resources : resourcesPerTown.values()) {
            // remove the resources that are in the owned plots
            for (int i = resources.size() - 1; i >= 0; i--) {
                Vector resource = resources.get(i);
                if (ownedPlots.containsKey(resource.mY) && ownedPlots.get(resource.mY).contains(resource.mX)) {
                    resources.remove(i);
                }
            }
            // sort the resources
            Collections.sort(resources);
            // drop the excess ones
            while (resources.size() > mMaxResourceCount) {
                resources.remove(resources.size() - 1);
            }
        }
    }
}
