package utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.ReplyDao;
import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ReplyUtil {


    public static List<Reply> getReplyforInquriy(int inquiryID ){
        try (Connection conn = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            ReplyDao replyDao = new ReplyDao(configuration);
            List<Reply> reply = replyDao.fetchByInquiryId(inquiryID);
            return reply;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
