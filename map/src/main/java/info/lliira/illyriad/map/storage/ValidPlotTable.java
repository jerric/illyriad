package info.lliira.illyriad.map.storage;

import info.lliira.illyriad.map.entity.ValidPlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ValidPlotTable extends LocationTable<ValidPlot, ValidPlot.Builder> {
    private static final List<Field<ValidPlot, ValidPlot.Builder, ?>> DATA_FIELDS =
            List.of(new Field<>("total_sum",
                                         FieldType.INT,
                                         v->v.totalSum,
                                         ValidPlot.Builder::totalSum),
                             new Field<>("food_sum",
                                         FieldType.INT,
                                         v->v.foodSum,
                                         ValidPlot.Builder::foodSum),
                             new Field<>("sov_count",
                                         FieldType.INT,
                                         v->v.sovereignCount,
                                         ValidPlot.Builder::sovereignCount),
                             new Field<>("restricted",
                                         FieldType.BOOLEAN,
                                         v->v.restricted,
                                         ValidPlot.Builder::restricted));

    private final PreparedStatement selectCandidateStatement;

    ValidPlotTable(Connection connection) {
        super(connection, "valid_plots", DATA_FIELDS);
        try {
            selectCandidateStatement = connection.prepareStatement("SELECT v.* FROM valid_plots v, plots p " +
                    "WHERE v.x = p.x AND v.y = p.y AND p.food = 7 AND v.restricted = false " +
                    "AND v.food_sum >= ? AND v.total_sum >= ? AND v.sov_count >= ?");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ValidPlot.Builder newBuilder() {
        return new ValidPlot.Builder();
    }

    public Iterator<ValidPlot> selectCandidates(int foodThreshold, int totalThreshold, int sovereignThreshold) throws SQLException {
        selectCandidateStatement.setInt(1, foodThreshold);
        selectCandidateStatement.setInt(2, totalThreshold);
        selectCandidateStatement.setInt(3, sovereignThreshold);
        ResultSet resultSet = selectCandidateStatement.executeQuery();
        return new ResultSetIterator<>(resultSet, this::convert);
    }
}
