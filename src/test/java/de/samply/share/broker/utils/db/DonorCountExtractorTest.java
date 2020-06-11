package de.samply.share.broker.utils.db;

import de.samply.share.broker.model.db.tables.pojos.Reply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.CoreMatchers.is;

public class DonorCountExtractorTest {

    private DonorCountExtractor countExtractor;

    @BeforeEach
    void init() {
        this.countExtractor = new DonorCountExtractor();
    }

    @Test
    void testExtractDonorCount_currentReply() {
        assertThat(countExtractor.extractDonorCount(reply(173055)), is(173055));
    }

    @Test
    void testExtractDonorCount_legacyReply() {
        assertThat(countExtractor.extractDonorCount(replyLegacy(173631)), is(173631));
    }

    @Test
    void testExtractDonorCount_invalidReply_MissingDonor() {
        assertThat(countExtractor.extractDonorCount(replyInvalid_MissingDonor()), is(0));
    }

    @Test
    void testExtractDonorCount_invalidReply_MissingCount() {
        assertThat(countExtractor.extractDonorCount(replyInvalid_MissingCount()), is(0));
    }

    @Test
    void testExtractDonorCount_invalidReply_WrongDonorFormat() {
        assertThat(countExtractor.extractDonorCount(replyInvalid_WrongDonorFormat()), is(0));
    }

    private static Reply reply(int count) {
        return reply("{ donor: { count: " + count + "}}");
    }

    private static Reply replyLegacy(int count) {
        return reply("{ donor: " + count + " }");
    }

    private static Reply replyInvalid_MissingDonor() {
        return reply("{ }");
    }

    private static Reply replyInvalid_MissingCount() {
        return reply("{ donor: {} }");
    }

    private static Reply replyInvalid_WrongDonorFormat() {
        return reply("{ donor: \"a\" }");
    }

    private static Reply reply(String content) {
        Reply reply = new Reply();
        reply.setContent(content);
        return reply;
    }
}
