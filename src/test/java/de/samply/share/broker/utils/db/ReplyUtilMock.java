package de.samply.share.broker.utils.db;

import de.samply.share.broker.model.db.tables.pojos.Reply;

import java.util.Arrays;
import java.util.List;

class ReplyUtilMock extends ReplyUtil {

    private final List<Reply> replies;

    ReplyUtilMock(DonorCountExtractor countExtractor, Reply... replies) {
        super(countExtractor);

        this.replies = Arrays.asList(replies);
    }

    @Override
    List<Reply> fetchReplies(int inquiryID) {
        return replies;
    }
}
