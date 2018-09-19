package de.samply.share.broker.utils.db;

import com.google.gson.JsonObject;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.ReplyDao;
import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.tools.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ReplyUtil {


    public static List<Reply> getReplyforInquriy(int inquiryID) {
        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            ReplyDao replyDao = new ReplyDao(configuration);
            List<Reply> reply = replyDao.fetchByInquiryId(inquiryID);
            reply = sortList(reply);
            return reply;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static List<Reply> sortList(List<Reply> replyList) {
        JSONParser parser = new JSONParser();
        try {
            for (int i = 0; i < replyList.size(); i++) {
                for (int j = 1; j < replyList.size()-i; j++) {
                    Reply reply = replyList.get(j-1);
                    JSONObject jsonObject = (JSONObject) parser.parse(reply.getContent());
                    int size = Integer.parseInt(jsonObject.get("sample").toString());
                    Reply reply2 = replyList.get(j);
                    JSONObject jsonObject2 = (JSONObject) parser.parse(reply2.getContent());
                    int size2 = Integer.parseInt(jsonObject2.get("sample").toString());
                    if (size > size2) {
                        replyList.set(j, reply);
                        replyList.set(j-1, reply2);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return replyList;
    }

}
