package de.samply.share.broker.utils.db;

import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class ReplyUtilTest {

    private final Reply reply1 = createReply(1);
    private final Reply reply2 = createReply(2);
    private final Reply reply3 = createReply(3);
    private final Reply replyInvalid = createInvalidReply();

    @Test
    void testGetReplyforInquriy_simpleCase() {
        ReplyUtil replyUtil = new ReplyUtilMock(reply1, reply2, reply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, reply3, reply2, reply1);
    }

    @Test
    void testGetReplyforInquriy_permutatedOrder() {
        ReplyUtil replyUtil = new ReplyUtilMock(reply2, reply3, reply1);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, reply3, reply2, reply1);
    }

    @Test
    void testGetReplyforInquriy_invalidReply() {
        ReplyUtil replyUtil = new ReplyUtilMock(reply1, replyInvalid, reply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, reply3, reply1, replyInvalid);
    }

    private void assertOrder(List<Reply> result, Reply... expectedResults) {
        List<Reply> expectedOrder = Arrays.asList(expectedResults);
        assertThat(result, is(expectedOrder));
    }

    private Reply createReply(int count) {
        Reply reply = new Reply();

        reply.setContent("{ sample: " + count + "}");
        reply.setId(10 * count);

        return reply;
    }

    private Reply createInvalidReply() {
        Reply reply = new Reply();

        reply.setContent(" { XYZsample: " + 2 + " }");
        reply.setId(20);

        return reply;
    }
}
