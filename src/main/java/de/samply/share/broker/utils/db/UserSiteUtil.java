/**
 * Copyright (C) 2015 Working Group on Joint Research, University Medical Center Mainz
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

package de.samply.share.broker.utils.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import de.samply.auth.rest.LocationDTO;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.SiteDao;
import de.samply.share.broker.model.db.tables.daos.UserDao;
import de.samply.share.broker.model.db.tables.daos.UserSiteDao;
import de.samply.share.broker.model.db.tables.pojos.EmailSite;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.model.db.tables.pojos.UserSite;
import de.samply.share.broker.utils.MailUtils;

/**
 * This class provides static methods for CRUD operations for UserSite Objects
 * 
 * @see UserSite
 */
public final class UserSiteUtil {
    
    private static final Logger logger = LogManager.getLogger(UserSiteUtil.class);
    
    // Prevent instantiation
    private UserSiteUtil() {
    }

    /**
     * Get all user to site associations
     *
     * @return a list with all user to site associations
     */
    public static List<UserSite> fetchUserSites() {
        UserSiteDao userSiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userSiteDao = new UserSiteDao(configuration);           

            return userSiteDao.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the user to site association for a user
     *
     * @param user the user
     * @return the user to site association for that user
     */
    public static UserSite fetchUserSiteByUser(User user) {
        List<UserSite> userSites;
        UserSiteDao userSiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userSiteDao = new UserSiteDao(configuration);           
            
            userSites = userSiteDao.fetchByUserId(user.getId());
            // There shall be but one assignment
            if (userSites != null && userSites.size() == 1) {
                return userSites.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Set the user to site association for a user
     *
     * @param user the user
     * @param site the site to link the user to
     * @param approved true if this has been verified before, false if it is tentative
     * @return the user to site association for that user
     */
    public static UserSite setSiteForUser(User user, Site site, boolean approved) {
        UserSite newUserSite = null;
        UserSite oldUserSite = null;
        UserSiteDao userSiteDao;
        
        if (user == null) {
            logger.warn("Tried to set site for user null");
            return null;
        }
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            userSiteDao = new UserSiteDao(configuration);
            List<UserSite> userSites = userSiteDao.fetchByUserId(user.getId());
            
            if (userSites != null && !userSites.isEmpty()) {
                oldUserSite = userSites.get(0);
                userSiteDao.delete(oldUserSite);
            }
            
            if (site == null) {
                logger.debug("New site is null. Just deleted the old one.");
                return null;
            }
            
            newUserSite = new UserSite();
            newUserSite.setApproved(approved);
            newUserSite.setSiteId(site.getId());
            newUserSite.setUserId(user.getId());
            userSiteDao.insert(newUserSite);
            
            if (oldUserSite != null && !oldUserSite.getSiteId().equals(newUserSite.getSiteId()) && !approved) {
                MailUtils.sendUserSiteMail(user, site);
            }
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return newUserSite;
    }

    /**
     * Update a user to site association
     *
     * @param userSite the user to site association to update
     */
    public static void updateUserSite(UserSite userSite) {        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            UserSiteDao userSiteDao = new UserSiteDao(configuration);
            userSiteDao.update(userSite);
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }
    
    /**
     * Check preemptive association table and assign site if necessary
     *
     * @param user the user to check the assignment for
     */
    public static void createUserSiteAssociation(User user) {
        if (user == null || user.getEmail() == null) {
            return;
        }
        EmailSite emailSite = EmailSiteUtil.fetchEmailSite(user.getEmail());
        if (emailSite == null || emailSite.getId() == null || emailSite.getSite() == null || emailSite.getSite() < 1) {
            return;
        }
        
        Integer siteIdForUser = UserUtil.getSiteIdForUser(user);
        if (siteIdForUser != null) {
            logger.info("Reassigning site for user " + user.getId() + ". Was " + siteIdForUser + " - will be " + emailSite.getSite());
        } else {
            logger.info("Assigning site " + emailSite.getSite() + " to user " + user.getId());
        }
        UserUtil.setSiteIdForUser(user, emailSite.getSite(), Boolean.TRUE);
        EmailSiteUtil.deleteEmailSite(emailSite);        
    }

    /**
     * Delete a user to site association
     *
     * @param userSite the user to site association to delete
     */
    private static void deleteUserSite(UserSite userSite) {
        UserSiteDao userSiteDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userSiteDao = new UserSiteDao(configuration);
            
            userSiteDao.delete(userSite);
        } catch(SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Delete the site assignment of a user
     *
     * @param user the user
     */
    public static void deleteSiteFromUser(User user) {
        UserSite userSite = fetchUserSiteByUser(user);
        if (userSite != null) {
            deleteUserSite(userSite);
        }
    }

    /**
     * Get a user id by his auth id
     *
     * @param authId the auth id of the user
     * @return the user id
     */
    public static int getUserIdByAuthId(String authId) {
        User user;
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userDao = new UserDao(configuration);
            user = userDao.fetchOneByAuthid(authId);
            if (user != null) {
                return user.getId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Update the site assignments for a user
     *
     * @param user the user to add the sites to
     * @param userLocations the list of sites to add, as received from the authentication service
     */
    public static void updateUserLocations(User user, List<LocationDTO> userLocations) {
        if (userLocations == null || userLocations.size() < 1) {
            logger.warn("User " + user.getAuthid() + " is not associated with any locations.");
        } else {
            if (userLocations.size() > 1) {
                logger.warn("User " + user.getAuthid() + " has more that one location. Using the first one.");
            }
            LocationDTO userLocation = userLocations.get(0);
            logger.debug("User " + user.getAuthid() + " is associated with location " + userLocation.getId());

            SiteDao siteDao = null;
            Site site = null;
            try (Connection connection = ResourceManager.getConnection() ) {
                Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
                siteDao = new SiteDao(configuration);
                site = siteDao.fetchOneByName(userLocation.getId());
                Integer siteIdForUser = UserUtil.getSiteIdForUser(user);
                if (!Objects.equals(site.getId(), siteIdForUser)) {
                    logger.debug("Updating user location. " + siteIdForUser + " -> " + site.getId());
                    setSiteForUser(user, site, true);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
