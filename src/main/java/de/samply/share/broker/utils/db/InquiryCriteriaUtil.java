package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.enums.InquiryCriteriaType;
import de.samply.share.broker.model.db.tables.daos.InquiryCriteriaDao;
import de.samply.share.broker.model.db.tables.pojos.InquiryCriteria;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InquiryCriteriaUtil {

    public static List<InquiryCriteria> fetchInquiryCriteriaForInquiryId(int inquiryId) {
        List<InquiryCriteria> inquiryCriteria;
        InquiryCriteriaDao inquiryCriteriaDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryCriteriaDao = new InquiryCriteriaDao(configuration);

            inquiryCriteria = inquiryCriteriaDao.fetchByInquiryId(inquiryId);
            return inquiryCriteria;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static String fetchCriteriaForInquiryIdTypeQuery(int inquiryId) {
        Optional<InquiryCriteria> inquiryCriteriaOptional = fetchInquiryCriteriaForInquiryId(inquiryId, InquiryCriteriaType.IC_QUERY);

        return inquiryCriteriaOptional.isPresent() ? inquiryCriteriaOptional.get().getCriteria() : "";
    }

    public static Optional<InquiryCriteria> fetchInquiryCriteriaForInquiryIdTypeQuery(int inquiryId) {
        return fetchInquiryCriteriaForInquiryId(inquiryId, InquiryCriteriaType.IC_QUERY);
    }

    public static Optional<InquiryCriteria> fetchInquiryCriteriaForInquiryId(int inquiryId, InquiryCriteriaType type) {
        List<InquiryCriteria> inquiryCriteria = fetchInquiryCriteriaForInquiryId(inquiryId);

        return inquiryCriteria.stream().filter(inquiryCriteriaTemp -> inquiryCriteriaTemp.getType() == type).findFirst();
    }

    public static void updateInquiryCriteria(InquiryCriteria inquiryCriteria) {
        InquiryCriteriaDao inquiryCriteriaDao;

        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryCriteriaDao = new InquiryCriteriaDao(configuration);
            inquiryCriteriaDao.update(inquiryCriteria);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
