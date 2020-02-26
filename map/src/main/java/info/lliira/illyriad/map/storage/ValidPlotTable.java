package info.lliira.illyriad.map.storage;

import com.google.common.collect.ImmutableList;
import net.lliira.illyriad.map.model.ValidPlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ValidPlotTable extends LocationTable<ValidPlot, ValidPlot.Builder> {

    private static final ImmutableList<Field<ValidPlot, ValidPlot.Builder, ?>> DATA_FIELDS =
            ImmutableList.of(new Field<>("total_sum",
                                         FieldType.INT,
                                         ValidPlot::getResourceSum,
                                         ValidPlot.Builder::setResourceSum),
                             new Field<>("food_sum",
                                         FieldType.INT,
                                         ValidPlot::getFoodSum,
                                         ValidPlot.Builder::setFoodSum),
                             new Field<>("sov_count",
                                         FieldType.INT,
                                         ValidPlot::getSovereignCount,
                                         ValidPlot.Builder::setSovereignCount),
                             new Field<>("restricted",
                                         FieldType.BOOLEAN,
                                         ValidPlot::isRestricted,
                                         ValidPlot.Builder::setRestricted));

    private final PreparedStatement selectCandidateStatement;

    ValidPlotTable(Connection connection) throws SQLException {
        super(connection, "valid_plots", DATA_FIELDS);
        selectCandidateStatement = connection.prepareStatement("SELECT v.* FROM valid_plots v, plots p " +
                "WHERE v.x = p.x AND v.y = p.y AND p.food = 7 AND v.restricted = false " +
                "AND v.food_sum >= ? AND v.total_sum >= ? AND v.sov_count >= ?");
    }

    @Override
    public ValidPlot.Builder newBuilder() {
        return ValidPlot.builder();
    }

    public Iterator<ValidPlot> selectCandidates(int foodThreshold, int totalThreshold, int sovereignThreshold) throws SQLException {
        selectCandidateStatement.setInt(1, foodThreshold);
        selectCandidateStatement.setInt(2, totalThreshold);
        selectCandidateStatement.setInt(3, sovereignThreshold);
        ResultSet resultSet = selectCandidateStatement.executeQuery();
        return new ResultSetIterator<>(resultSet, this::convert);
    }
}
