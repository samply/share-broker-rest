package de.samply.share.broker.jobs;

import de.samply.share.broker.statistics.ExcelWriter;
import java.io.IOException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class StatisticJob implements Job {


  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    ExcelWriter excelWriter = new ExcelWriter();
    try {
      excelWriter.sendExcel();
    } catch (IOException e) {
      throw new JobExecutionException(e);
    }
  }
}
