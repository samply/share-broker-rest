package de.samply.share.broker.utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.ReplyDao;
import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReplyUtil {


    public static List<Reply> getReplyforInquriy(int inquiryID) {
        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            ReplyDao replyDao = new ReplyDao(configuration);
            List<Reply> reply = replyDao.fetchByInquiryId(inquiryID);
            Collections.sort(reply, new SampleOperator());
            return reply;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static class SampleOperator implements Comparator<Reply> {

        @Override
        public int compare(Reply reply1, Reply reply2) {
            int size = 0;
            int size2 = 0;
            try {
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = (JSONObject) parser.parse(reply1.getContent());
                size = Integer.parseInt(jsonObject.get("sample").toString());
                JSONObject jsonObject2 = (JSONObject) parser.parse(reply2.getContent());
                size2 = Integer.parseInt(jsonObject2.get("sample").toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return size > size2 ? 1 : size == size2 ? 0 : -1;
        }
    }
}
