package de.samply.share.broker.utils.db;

import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static de.samply.share.broker.utils.db.ReplyUtil.extractDonorCount;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

class ReplyUtilTest {

    private final Reply reply1 = currentReply(1);
    private final Reply reply2 = currentReply(2);
    private final Reply reply3 = currentReply(3);

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
    void testExtractDonorCount_currentReply() {
        assertThat(extractDonorCount(currentReply(173055)), is(173055));
    }

    @Test
    void testExtractDonorCount_legacyReply() {
        assertThat(extractDonorCount(reply("{ donor: " + 173631 + "}")), is(173631));
    }

    @Test
    void testExtractDonorCount_missingDonorKey() {
        assertThat(extractDonorCount(reply("{ other: 1 }")), is(0));
    }

    @Test
    void testExtractDonorCount_missingDonorCount() {
        assertThat(extractDonorCount(reply("{ donor: { other: 1 } }")), is(0));
    }

    private void assertOrder(List<Reply> result, Reply... expectedResults) {
        List<Reply> expectedOrder = Arrays.asList(expectedResults);
        assertThat(result, is(expectedOrder));
    }

    private static Reply currentReply(int count) {
        return reply("{ donor: { count: " + count + "}}");
    }

    private static Reply reply(String content) {
        Reply reply = new Reply();
        reply.setContent(content);
        return reply;
    }
}
