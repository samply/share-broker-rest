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
import java.util.stream.Collectors;

public class InquiryCriteriaUtil {

    public static List<InquiryCriteria> fetchInquiryCriteriaForInquiryId(int inquiryId) {
        List<InquiryCriteria> inquiryCriteria;
        InquiryCriteriaDao inquiryCriteriaDao;

        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryCriteriaDao = new InquiryCriteriaDao(configuration);

            inquiryCriteria = inquiryCriteriaDao.fetchByInquiryId(inquiryId);
            return inquiryCriteria;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public static List<InquiryCriteria> fetchCriteriaListForInquiryIdTypeCql(int inquiryId) {
        List<InquiryCriteria> inquiryCriteria = fetchInquiryCriteriaForInquiryId(inquiryId);

        return inquiryCriteria.stream().filter(inquiryCriteriaTemp -> inquiryCriteriaTemp.getType() == InquiryCriteriaType.IC_CQL).collect(Collectors.toList());
    }

    public static String fetchCriteriaForInquiryIdTypeQuery(int inquiryId) {
        List<InquiryCriteria> inquiryCriteria = fetchInquiryCriteriaForInquiryId(inquiryId);

        Optional<InquiryCriteria> inquiryCriteriaOptional =
                inquiryCriteria.stream().
                        filter(inquiryCriteriaTemp -> inquiryCriteriaTemp.getType() == InquiryCriteriaType.IC_QUERY).
                        findFirst();

        return inquiryCriteriaOptional.isPresent() ? inquiryCriteriaOptional.get().getCriteria() : "";
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
