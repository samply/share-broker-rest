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
    private final Reply legacyReply1 = createLegacyReply(1);
    private final Reply legacyReply2 = createLegacyReply(2);
    private final Reply legacyReply3 = createLegacyReply(3);

    @Test
    void testGetReplyforInquriy_simpleCase() {
        ReplyUtil replyUtil = new ReplyUtilMock(reply1, reply2, reply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, reply3, reply2, reply1);
    }

    @Test
    void testGetReplyforInquriy_simpleCaseLegacy() {
        ReplyUtil replyUtil = new ReplyUtilMock(legacyReply1, legacyReply2, legacyReply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, legacyReply3, legacyReply2, legacyReply1);
    }

    @Test
    void testGetReplyforInquriy_permutatedOrder() {
        ReplyUtil replyUtil = new ReplyUtilMock(reply2, reply3, reply1);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, reply3, reply2, reply1);
    }

    @Test
    void testGetReplyforInquriy_permutatedOrderLegacy() {
        ReplyUtil replyUtil = new ReplyUtilMock(legacyReply2, legacyReply3, legacyReply1);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, legacyReply3, legacyReply2, legacyReply1);
    }

    @Test
    void testGetReplyforInquriy_invalidReply1() {
        Reply replyInvalid = createReplyWithoutDonorKey();
        ReplyUtil replyUtil = new ReplyUtilMock(legacyReply1, replyInvalid, legacyReply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, legacyReply3, legacyReply1, replyInvalid);
    }

    @Test
    void testGetReplyforInquriy_invalidReply2() {
        Reply replyInvalid = createReplyWithoutDonorKey();
        ReplyUtil replyUtil = new ReplyUtilMock(reply1, replyInvalid, reply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, reply3, reply1, replyInvalid);
    }

    @Test
    void testGetReplyforInquriy_invalidReply3() {
        Reply replyInvalid = createReplyWithoutDonorCountKey();
        ReplyUtil replyUtil = new ReplyUtilMock(legacyReply1, replyInvalid, legacyReply3);
        List<Reply> result = replyUtil.getReplyforInquriy(0);

        assertOrder(result, legacyReply3, legacyReply1, replyInvalid);
    }

    @Test
    void testGetReplyforInquriy_invalidReply4() {
        Reply replyInvalid = createReplyWithoutDonorCountKey();
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

        reply.setContent("{ donor: { count: " + count + "}}");
        reply.setId(10 * count);

        return reply;
    }

    private Reply createLegacyReply(int count) {
        Reply reply = new Reply();
        reply.setContent("{ donor: " + count + "}");
        return reply;
    }

    private Reply createReplyWithoutDonorKey() {
        Reply reply = new Reply();
        reply.setContent("{ other: 1 }");
        return reply;
    }

    private Reply createReplyWithoutDonorCountKey() {
        Reply reply = new Reply();
        reply.setContent("{donor: { other: 1 }}");
        return reply;
    }
}
