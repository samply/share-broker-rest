package de.samply.share.broker.job;

import de.samply.share.broker.utils.db.InquiryUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class DbCleanupJob implements Job {

    private static final Logger logger = LogManager.getLogger(DbCleanupJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext){
        logger.debug("Executing database cleanup job");
        InquiryUtil.deleteSimpleResultInquiry();
    }

}
