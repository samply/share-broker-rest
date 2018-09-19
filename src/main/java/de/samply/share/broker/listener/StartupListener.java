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
package de.samply.share.broker.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;

import de.samply.share.broker.job.DbCleanupJob;
import de.samply.share.broker.utils.db.Migration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.samply.common.http.HttpConnector;
import de.samply.common.mdrclient.MdrClient;
import de.samply.share.broker.utils.Config;
import de.samply.share.broker.utils.ScheduledMailSending;
import de.samply.share.broker.utils.Utils;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.web.mdrFaces.MdrContext;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * The listener interface for receiving startup events.
 * The class that is interested in processing a startup
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addStartupListener</code> method. When
 * the startup event occurs, that object's appropriate
 * method is invoked.
 *
 */
public class StartupListener implements javax.servlet.ServletContextListener {

    /** The Constant logger. */
    private static final Logger LOGGER = LogManager.getLogger(StartupListener.class);

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks to this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                LOGGER.info("Deregistering jdbc driver: " + driver);
            } catch (SQLException e) {
                LOGGER.fatal("Error deregistering driver:" + driver + "\n" + e.getMessage());
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ProjectInfo.INSTANCE.initProjectMetadata(sce);
        LOGGER.info("Loading Samply.Share.Broker v" + ProjectInfo.INSTANCE.getVersionString() + " for " + ProjectInfo.INSTANCE.getProjectName());

        LOGGER.debug("Listener to the web application startup is running. Checking configuration...");
        Config c = Config.instance;
        ProjectInfo.INSTANCE.setConfig(c);
        Migration.doUpgrade();
        String mdrUrl = c.getProperty("mdr.url");
        HttpConnector httpConnector = Proxy.getHttpConnector();
        MdrClient mdrClient = new MdrClient(mdrUrl, httpConnector.getJerseyClient(mdrUrl));
        MdrContext.getMdrContext().init(mdrClient);
        spawnSchedulerJobs();
    }

    private void spawnSchedulerJobs() {
        try {
            SchedulerFactory sf = new StdSchedulerFactory();
            Scheduler sched = null;
            sched = sf.getScheduler();
            sched.start();
            JobDetail job = newJob(DbCleanupJob.class)
                    .withIdentity("dbCleanup", "group1")
                    .build();
            CronTrigger trigger = newTrigger()
                    .withIdentity("trigger1", "group1")
                    .withSchedule(cronSchedule("0 0 2 * * ?"))
                    .build();
            sched.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

}