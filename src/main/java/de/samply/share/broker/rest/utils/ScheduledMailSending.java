/*
 * Copyright (C) 2015 Working Group on Joint Research,
 * Division of Medical Informatics,
 * Institute of Medical Biometrics, Epidemiology and Informatics,
 * University Medical Center of the Johannes Gutenberg University Mainz
 *
 * Contact: info@osse-register.de
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */
package de.samply.share.broker.rest.utils;

import de.samply.share.broker.thread.MailSendingTask;
import de.samply.share.common.utils.ProjectInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Handles the scheduled sending of mails if necessary
 */
public class ScheduledMailSending {

    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.ScheduledMailSending.class);

    protected static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Handler created when a task is scheduled. Enables cancelling that task.
     */
    private static ScheduledFuture<?> scheduledMailSender;

    /**
     * Cancels already scheduled uploads if any and schedules new uploads according to the parameters.
     */
    public static void reScheduleMailSending() {
        cancelScheduledMailSending();
        scheduleMailSending();
    }

    /**
     * Shutdown service.
     */
    public static void shutdownService() {
        scheduler.shutdownNow();
        try {
            scheduler.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cancels already scheduled uploads if any.
     */
    private static void cancelScheduledMailSending() {
        if (scheduledMailSender != null) {
            logger.debug("Cancelling previously scheduled mailsending...");
            scheduledMailSender.cancel(false); // cancel previous
        }
    }

    public static void scheduleMailSendingDaily() {
        de.samply.share.broker.utils.ScheduledMailSending.reScheduleMailSending();
    }

    /**
     * Schedule mail sending tasks.
     */
    private static void scheduleMailSending() {
        logger.debug("Scheduling the mail sender...");

        // runnable upload action
        final Runnable runnableMailSender = new Runnable() {
            @Override
            public void run() {
                MailSendingTask mailSendingTask = new MailSendingTask();
                mailSendingTask.doIt();
            }
        };
        long initialDelay;

        // get the minutes passed since the beginning of the day
        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long msSinceMidnight = now - calendar.getTimeInMillis();
        long minSinceMidnight = msSinceMidnight / (60 * 1000);

        // scheduled time since midnight
        Calendar scheduled = Calendar.getInstance();

        int mailCheckHour;
        int mailCheckMinute;
        try {
            mailCheckHour = Integer.parseInt(ProjectInfo.INSTANCE.getConfig().getProperty("daily.mailcheck.hour"));
            mailCheckMinute = Integer.parseInt(ProjectInfo.INSTANCE.getConfig().getProperty("daily.mailcheck.minute"));
        } catch (NumberFormatException e) {
            logger.error("Couldn't parse mail check time - use default (7am)");
            mailCheckHour = 7;
            mailCheckMinute = 0;
        }

        scheduled.set(Calendar.HOUR_OF_DAY, mailCheckHour);
        scheduled.set(Calendar.MINUTE, mailCheckMinute);
        long scheduledMsSinceMidnight = scheduled.getTimeInMillis() - calendar.getTimeInMillis();
        long scheduledMinSinceMidnight = scheduledMsSinceMidnight / (60 * 1000);

        // calculate delay to next scheduled run
        if (scheduledMinSinceMidnight > minSinceMidnight) {
            initialDelay = scheduledMinSinceMidnight - minSinceMidnight;
        } else {
            initialDelay = (TimeUnit.DAYS.toMinutes(1) - minSinceMidnight) + scheduledMinSinceMidnight;
        }

        logger.debug("Next scheduled mail check will run in " + initialDelay + " minutes...");
        scheduledMailSender = scheduler.scheduleAtFixedRate(runnableMailSender, initialDelay, TimeUnit.DAYS.toMinutes(1), TimeUnit.MINUTES);

    }
}