package net.lliira.illyriad.map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.lliira.illyriad.map.model.Point;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

/**
 * Crawl the neighbor region of a given coordinator, and save the data into database. The size of the neighbor region
 * is determined by the {@value Constants#ZOOM_LEVEL}.
 */
public class CrawlTask implements Runnable {

    private static final String MAP_DATA_URL = Constants.BASE_URL + "/World/MapData";

    private static final String X_FIELD = "x";
    private static final String Y_FIELD = "y";
    private static final String ZOOM_FIELD = "zoom";
    private static final String DIR_FIELD = "dir";
    private static final String DEFAULT_DIR_FIELD = "";

    private static final Logger LOG = LoggerFactory.getLogger(CrawlTask.class);

    private static void setCoordinates(PreparedStatement preparedStatement, Point coordinate) throws SQLException {
        preparedStatement.setInt(1, coordinate.mX);
        preparedStatement.setInt(2, coordinate.mY);
    }

    private final Storage mStorage;
    private final Map<String, String> mCookies;
    private final int mCenterX;
    private final int mCenterY;
    private final int mMinX;
    private final int mMaxX;
    private final int mMinY;
    private final int mMaxY;

    public CrawlTask(Storage storage, Map<String, String> cookies, int centerX, int centerY) {
        mStorage = storage;
        mCookies = cookies;
        mCenterX = centerX;
        mCenterY = centerY;

        mMinX = mCenterX - Constants.ZOOM_LEVEL;
        mMinY = mCenterY - Constants.ZOOM_LEVEL;
        mMaxX = mCenterX + Constants.ZOOM_LEVEL;
        mMaxY = mCenterY + Constants.ZOOM_LEVEL;
    }

    @Override
    public void run() {
        LOG.info("Crawling region ({}, {})...", mCenterX, mCenterY);
        Connection connection = Jsoup.connect(MAP_DATA_URL)
                .ignoreContentType(true)
                .method(Connection.Method.POST)
                .header("origin", Constants.BASE_URL)
                .header("referer", Constants.BASE_URL + "/")
                .header("x-requested-with", "XMLHttpRequest")
                .cookies(mCookies)
                .data(X_FIELD, Integer.toString(mCenterX))
                .data(Y_FIELD, Integer.toString(mCenterY))
                .data(ZOOM_FIELD, Integer.toString(Constants.ZOOM_LEVEL))
                .data(DIR_FIELD, DEFAULT_DIR_FIELD);
        try {
            String json = connection.execute().body();
            JsonNode root = new ObjectMapper().readTree(json);

            cleanup();

            // read & save creatures
            saveCreatures(root.path("c"));

            // read & save deposits
            saveDeposits(root.path("d"));

            // read & save map plots
            savePlots(root.path("data"));

            // read & save resources
            saveResources(root.path("n"));

            // read & save towns
            saveTowns(root.path("t"));

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Clean up the rows so that we can simply run insert into the database.
     */
    private void cleanup() throws SQLException {
        int[] params = {mMinX, mMaxX, mMinY, mMaxY};
        Storage.ParameterMapper<int[]> parameterMapper = (ps, p) -> {
            for (int i = 0; i < p.length; i++) {
                ps.setInt(i + 1, p[i]);
            }
            return true;
        };

        Storage.ResultMapper<Integer> resultMapper = (resultSet) -> resultSet.getInt(1);

        String[] tableNames = {"creatures", "deposits", "plots", "resources", "towns"};
        for (String tableName : tableNames) {
            String condition = "x >= ? AND x <= ? AND y >= ? AND y <= ?";
            String deleteSql = String.format("DELETE FROM %s WHERE %s", tableName, condition);
            mStorage.update(deleteSql, parameterMapper, params);
        }

    }

    private int saveCreatures(JsonNode creatures) throws SQLException {
        return mStorage.batchUpdate("INSERT INTO creatures (x, y, description, i, n) VALUES (?, ?, ?, ?, ?)",
                (ps, entry) -> {
                    setCoordinates(ps, Point.of(entry.getKey()));
                    JsonNode creature = entry.getValue();
                    ps.setString(3, creature.path("d").asText());
                    ps.setString(4, creature.path("i").asText());
                    ps.setString(5, creature.path("n").asText());
                    return true;
                },
                creatures.fields(),
                1000);
    }

    private int saveDeposits(JsonNode deposits) throws SQLException {
        return mStorage.batchUpdate("INSERT INTO deposits (x, y, skin, herb, mineral, equipment, elemental_salt, " +
                        "rare_herb, rare_mineral, grape, animal_part) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, entry) -> {
                    setCoordinates(ps, Point.of(entry.getKey()));
                    String[] types = entry.getValue().asText().split("\\|");
                    for (int i = 3; i <= 11; i++) {
                        int value = (i - 3 < types.length) ? Integer.valueOf(types[i - 3]) : 0;
                        ps.setInt(i, value);
                    }

                    return true;
                },
                deposits.fields(),
                1000);
    }

    private int savePlots(JsonNode plots) throws SQLException {
        return mStorage.batchUpdate("INSERT INTO plots " +
                        "(x, y, wood, clay, iron, stone, food, total, b, hos, i, l, r, sov, imp, npc) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (ps, entry) -> {
                    setCoordinates(ps, Point.of(entry.getKey()));
                    JsonNode plot = entry.getValue();
                    String[] resources = plot.path("rs").asText().split("\\|");
                    int wood = Integer.valueOf(resources[0]);
                    int clay = Integer.valueOf(resources[1]);
                    int iron = Integer.valueOf(resources[2]);
                    int stone = Integer.valueOf(resources[3]);
                    int food = Integer.valueOf(resources[4]);
                    int total = wood + clay + iron + stone + food;

                    ps.setInt(3, wood);
                    ps.setInt(4, clay);
                    ps.setInt(5, iron);
                    ps.setInt(6, stone);
                    ps.setInt(7, food);
                    ps.setInt(8, total);
                    ps.setInt(9, plot.path("b").asInt());
                    ps.setInt(10, plot.path("hos").asInt());
                    ps.setInt(11, plot.path("i").asInt());
                    ps.setInt(12, plot.path("l").asInt());
                    ps.setInt(13, plot.path("r").asInt());
                    ps.setInt(14, plot.path("sov").asInt());
                    ps.setInt(15, plot.path("imp").asInt());
                    ps.setInt(16, plot.path("npc").asInt());
                    return true;
                },
                plots.fields(),
                1000);
    }

    private int saveResources(JsonNode resources) throws SQLException {
        return mStorage.batchUpdate("INSERT INTO resources (x, y, description, type, rd, r) VALUES (?, ?, ?, ?, ?, ?)",
                (ps, entry) -> {
                    setCoordinates(ps, Point.of(entry.getKey()));
                    JsonNode resource = entry.getValue();
                    ps.setString(3, resource.path("d").asText());
                    ps.setString(4, resource.path("i").asText());
                    ps.setString(5, resource.path("rd").asText());
                    ps.setInt(6, resource.path("r").asInt());
                    return true;
                },
                resources.fields(),
                1000);
    }

    private int saveTowns(JsonNode towns) throws SQLException {
        return mStorage.batchUpdate("INSERT INTO towns (x, y, name, owner, data) VALUES (?, ?, ?, ?, ?)",
                (ps, entry) -> {
                    // in a rare case the town might be reported from a region it doesn't belong, we will skip it
                    Point coordinate = Point.of(entry.getKey());
                    if (coordinate.mX >= mMinX && coordinate.mX <= mMaxX
                            && coordinate.mY >= mMinY && coordinate.mY <= mMaxY) {
                        setCoordinates(ps, coordinate);
                        JsonNode town = entry.getValue();
                        String data = town.path("t").asText();
                        String[] parts = data.split("\\|");
                        String name = parts[0];
                        String owner = parts[10];
                        ps.setString(3, name);
                        ps.setString(4, owner);
                        ps.setString(5, data);
                        return true;
                    } else {
                        return false;
                    }
                },
                towns.fields(),
                1000);
    }
}