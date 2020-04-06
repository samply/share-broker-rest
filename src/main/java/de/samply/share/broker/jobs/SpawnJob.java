package de.samply.share.broker.jobs;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

public class SpawnJob {

    public void spawnStatisticJob(){
        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = null;
            sched = sf.getScheduler();
            sched.start();
            JobDetail job = newJob(StatisticJob.class)
                    .withIdentity("statisticJob", "group1")
                    .build();
            CronTrigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .withSchedule(cronSchedule("0 0 2 ? * * *"))
                    .build();
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
