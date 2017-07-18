package net.lliira.illyriad.map;

import net.lliira.illyriad.map.model.Town;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * analyze the mResources and locate the top ranking plots
 */
public class ResourceAnalyzer {

    private static class Resource {
        int mTotalSum = 0;
        int mFoodSum = 0;
        int mSovCount = 0;

        Resource add(int total, int food, boolean sovable) {
            mTotalSum += total;
            mFoodSum += food;
            if (sovable) {
                mSovCount++;
            }
            return this;
        }
    }

    private static class ResourceMap {

        private final int mRegionRadius;
        private final Map<Integer, Map<Integer, Resource>> mResources;

        ResourceMap(int regionRadius) {
            mRegionRadius = regionRadius;
            mResources = new HashMap<>(mRegionRadius * 2);
        }

        void add(int x, int y, int res, int food, boolean sovable) {
            // determine the  cells that needs update.
            int minX = Math.max(Constants.MIN_X, x - mRegionRadius);
            int minY = Math.max(Constants.MIN_Y, y - mRegionRadius);
            int maxX = Math.min(Constants.MAX_X, x + mRegionRadius);
            int maxY = Math.min(Constants.MAX_Y, y + mRegionRadius);
            for (int rowY = minY; rowY <= maxY; rowY++) {
                Map<Integer, Resource> row = mResources.computeIfAbsent(rowY, (r) -> new HashMap<>());
                for (int colX = minX; colX <= maxX; colX++) {
                    Resource resource = row.computeIfAbsent(colX, (cx) -> new Resource());
                    resource.add(res, food, sovable);
                }
            }
        }

        Map<Integer, Resource> getRow(int y) {
            return mResources.get(y);
        }

        /**
         * Remove the previous rows that have been fully processed
         * @param fromY starting from this row, inclusive.
         * @param toY   ending at this row, exclusive.
         */
        void cleanup(int fromY, int toY) {
            for (int y = fromY; y < toY; y++) {
                mResources.remove(y);
            }
        }
    }

    private static final String REGION_RADIUS_KEY = "analyze.region.radius";
    private static final String OWNER_RADIUS_KEY = "analyze.owner.radius";

    private static final Logger LOG = LoggerFactory.getLogger(ResourceAnalyzer.class);

    private final Storage mStorage;
    private final TownManager mTownManager;

    private final int mRegionRadius;
    private final int mOwnerRadius;

    public ResourceAnalyzer(Properties properties, Storage storage, TownManager townManager) {
        mStorage = storage;
        mTownManager = townManager;

        mRegionRadius = Integer.valueOf(properties.getProperty(REGION_RADIUS_KEY));
        mOwnerRadius = Integer.valueOf(properties.getProperty(OWNER_RADIUS_KEY));
    }

    public void analyze() throws SQLException {
        LOG.info("Cleaning up valid plots...");
        mStorage.update("DELETE FROM valid_plots", (ps, p) -> true, null);
        LOG.info("Copying plots...");
        mStorage.update("INSERT INTO valid_plots (x, y) SELECT x, y FROM plots WHERE total != 0", (ps, p) -> true, null);
        LOG.info("Removing plots near other towns...");
        removePlotsNearTowns();
        LOG.info("Computing totals...");
        computingTotals();
    }

    private void removePlotsNearTowns() throws SQLException {
        List<Town> towns = mTownManager.getTowns();
        LOG.debug("total {} towns to be processed.", towns.size());
        mStorage.batchUpdate("DELETE FROM valid_plots WHERE x >= ? AND x <= ? AND y >= ? AND y <= ?",
                (ps, p) -> {
                    ps.setInt(1, p.mX - mOwnerRadius);
                    ps.setInt(2, p.mX + mOwnerRadius);
                    ps.setInt(3, p.mY - mOwnerRadius);
                    ps.setInt(4, p.mY + mOwnerRadius);
                    return true;
                },
                towns.iterator(),
                1);
    }

    private void computingTotals() throws SQLException {
        final ResourceMap resourceMap = new ResourceMap(mRegionRadius);
        String sql = "SELECT p.x, p.y, p.food, p.total, p.sov FROM plots p, valid_plots vp " +
                " WHERE vp.y >= ? AND vp.y <= ? AND p.x = vp.x AND p.y = vp.y";
        try (PreparedStatement preparedStatement = mStorage.prepareStatement(sql)) {
            // starting at the same row as fromX, so that for the first iteration, we don't need to save & clean up any
            // rows.
            int previousFromY = Constants.MIN_Y;
            for (int fromY = Constants.MIN_Y; fromY <= Constants.MAX_X; fromY += mRegionRadius) {
                int toY = fromY + mRegionRadius;
                if (fromY % 100 == 0) {
                    LOG.debug("Processing rows between [{}-{})...", fromY, toY);
                }
                preparedStatement.setInt(1, fromY);
                preparedStatement.setInt(2, toY);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        resourceMap.add(resultSet.getInt("x"),
                                resultSet.getInt("y"),
                                resultSet.getInt("total"),
                                resultSet.getInt("food"),
                                (resultSet.getInt("sov") == 1));
                    }
                }

                // the row above fromX should already have all the resources needed, save into the database;
                saveResources(resourceMap, previousFromY, fromY);
                previousFromY = fromY;
            }
            // process the last batch
            saveResources(resourceMap, previousFromY, Constants.MAX_Y + 1);
        }
    }

    private void saveResources(final ResourceMap resourceMap, int fromY, int toY) throws SQLException {
        for (int y = fromY; y < toY; y++) {
            final int ry = y;   // need a final in order to be used in the closure.
            Map<Integer, Resource> row = resourceMap.getRow(y);
            if (row != null) {
                mStorage.batchUpdate(
                        "UPDATE valid_plots SET total_sum = ?, food_sum = ?, sov_count = ? WHERE x = ? AND y = ?",
                        (ps, entry) -> {
                            int x = entry.getKey();
                            Resource resource = entry.getValue();
                            ps.setInt(1, resource.mTotalSum);
                            ps.setInt(2, resource.mFoodSum);
                            ps.setInt(3, resource.mSovCount);
                            ps.setInt(4, x);
                            ps.setInt(5, ry);
                            return true;
                        },
                        row.entrySet().iterator(),
                        1000);
            }
        }

        // clean up the rows that are already saved.
        resourceMap.cleanup(fromY, toY);
    }
}
