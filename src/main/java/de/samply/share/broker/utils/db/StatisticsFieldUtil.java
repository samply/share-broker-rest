package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.pojos.StatisticsField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.currentTimestamp;

public final class StatisticsFieldUtil {
    private static final Logger logger = LogManager.getLogger(StatisticsField.class);
    private StatisticsFieldUtil(){
    }

    public static List<StatisticsField> getFieldsByQueryIds(List<Integer> statisticsQueryIds) {
        List<StatisticsField> statisticsFields = new ArrayList<>();
        try (Connection conn = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);
            statisticsFields = dslContext.select()
                    .from(Tables.STATISTICS_FIELD)
                    .where(Tables.STATISTICS_FIELD.QUERYID.in(statisticsQueryIds)).fetchInto(StatisticsField.class);
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return statisticsFields;
    }

}
