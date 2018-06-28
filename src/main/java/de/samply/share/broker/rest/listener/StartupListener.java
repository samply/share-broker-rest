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
package de.samply.share.broker.rest.listener;

import de.samply.common.http.HttpConnector;
import de.samply.common.mdrclient.MdrClient;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.web.mdrFaces.MdrContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.samply.share.broker.rest.utils.Config;
import de.samply.share.broker.rest.utils.ScheduledMailSending;
import de.samply.share.broker.rest.utils.db.Migration;

import javax.servlet.ServletContextEvent;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

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
        
        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")) {
            LOGGER.info("Shutting down Mail Service");
            ScheduledMailSending.shutdownService();
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

        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")||ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("samply") )  {
            LOGGER.info("Starting mail service");
            ScheduledMailSending.scheduleMailSendingDaily();
        }
    }

}