package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.enums.InquiryDetailsType;
import de.samply.share.broker.model.db.tables.daos.InquiryDetailsDao;
import de.samply.share.broker.model.db.tables.pojos.InquiryDetails;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InquiryDetailsUtil {

    public static List<InquiryDetails> fetchInquiryDetailsForInquiryId(int inquiryId) {
        List<InquiryDetails> inquiryDetails;
        InquiryDetailsDao inquiryDetailsDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryDetailsDao = new InquiryDetailsDao(configuration);

            inquiryDetails = inquiryDetailsDao.fetchByInquiryId(inquiryId);
            return inquiryDetails;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static String fetchCriteriaForInquiryIdTypeQuery(int inquiryId) {
        Optional<InquiryDetails> inquiryDetailsOptional = fetchInquiryDetailsForInquiryId(inquiryId, InquiryDetailsType.QUERY);

        return inquiryDetailsOptional.isPresent() ? inquiryDetailsOptional.get().getCriteria() : "";
    }

    public static Optional<InquiryDetails> fetchInquiryDetailsForInquiryIdTypeQuery(int inquiryId) {
        return fetchInquiryDetailsForInquiryId(inquiryId, InquiryDetailsType.QUERY);
    }

    public static Optional<InquiryDetails> fetchInquiryDetailsForInquiryId(int inquiryId, InquiryDetailsType type) {
        List<InquiryDetails> inquiryDetails = fetchInquiryDetailsForInquiryId(inquiryId);

        return inquiryDetails.stream().filter(inquiryDetailsTemp -> inquiryDetailsTemp.getType() == type).findFirst();
    }

    public static void updateInquiryDetails(InquiryDetails inquiryDetails) {
        InquiryDetailsDao inquiryDetailsDao;

        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryDetailsDao = new InquiryDetailsDao(configuration);
            inquiryDetailsDao.update(inquiryDetails);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
