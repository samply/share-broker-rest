package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.pojos.StatisticsQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.impl.DSL.currentTimestamp;

public final class StatisticsQueryUtil {
    private static final Logger logger = LogManager.getLogger(StatisticsQueryUtil.class);
    private StatisticsQueryUtil(){
    }

    public static List<Integer> getLastDayQueryIds() {
        List<Integer> queryList = new ArrayList<>();
        try (Connection conn = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);
            queryList = dslContext.select(Tables.STATISTICS_QUERY.ID)
                    .from(Tables.STATISTICS_QUERY)
                    .where(Tables.STATISTICS_QUERY.CREATED.between(currentTimestamp().sub(2),currentTimestamp())).fetchInto(Integer.class);
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return queryList;
    }

}
