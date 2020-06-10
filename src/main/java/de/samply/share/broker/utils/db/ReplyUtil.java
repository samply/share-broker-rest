package de.samply.share.broker.utils.db;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.ReplyDao;
import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReplyUtil {

    public List<Reply> getReplyforInquriy(int inquiryID) {
        List<Reply> reply = fetchReplies(inquiryID);

        reply.sort(Comparator.comparingInt(ReplyUtil::extractDonorCount));
        Collections.reverse(reply);

        return reply;
    }

    static int extractDonorCount(Reply reply) {
        try {
            JsonResult result = new Gson().fromJson(reply.getContent(), JsonResult.class);
            JsonResultEntity donor = result.getDonor();
            return donor == null ? 0 : donor.getCount();
        } catch (JsonSyntaxException exception) {
            return extractDonorCountLegacyFormat(reply);
        }
    }

    private static int extractDonorCountLegacyFormat(Reply reply) {
        try {
            JsonResultLegacy result = new Gson().fromJson(reply.getContent(), JsonResultLegacy.class);
            return result.getDonor();
        } catch (JsonSyntaxException exception) {
            return 0;
        }
    }

    List<Reply> fetchReplies(int inquiryID) {
        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            ReplyDao replyDao = new ReplyDao(configuration);
            return replyDao.fetchByInquiryId(inquiryID);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static class JsonResult {
        @SuppressWarnings("unused")
        private JsonResultEntity donor;

        JsonResultEntity getDonor() {
            return donor;
        }
    }

    private static class JsonResultEntity {
        @SuppressWarnings("unused")
        private int count;

        int getCount() {
            return count;
        }
    }

    private static class JsonResultLegacy {
        @SuppressWarnings("unused")
        private int donor;

        int getDonor() {
            return donor;
        }
    }
}
