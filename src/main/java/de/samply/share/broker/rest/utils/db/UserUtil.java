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

package de.samply.share.broker.rest.utils.db;

import de.samply.auth.client.jwt.JWTIDToken;
import de.samply.auth.rest.AccessTokenDTO;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.daos.SiteDao;
import de.samply.share.broker.model.db.tables.daos.UserDao;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.UserSiteUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *  This class provides static methods for CRUD operations for User Objects
 *  
 *  @see de.samply.share.broker.model.db.tables.pojos.User
 */
public final class UserUtil {
    
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.db.UserUtil.class);

    // Prevent instantiation
    private UserUtil() {
    }
    
    /**
     * Update user.
     *
     * @param user the user to update
     */
    public static void updateUser(User user) {
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            userDao = new UserDao(configuration);
            userDao.update(user);

        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Creates a new user with the information from the id token
     *
     * @param jwtIdToken the id token from the auth service
     * @return the newly created user
     */
    public static User createUser(JWTIDToken jwtIdToken) {
        User user = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            UserDao userDao = new UserDao(configuration);

            user = new User();
            user.setAuthid(jwtIdToken.getSubject());
            user.setEmail(jwtIdToken.getEmail());
            user.setName(jwtIdToken.getName());
            user.setUsername(jwtIdToken.getEmail());

            userDao.insert(user);
            user = userDao.fetchOneByAuthid(user.getAuthid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return user;
    }

    /**
     * Updates a user.
     *
     * @param user the user to update
     * @param jwtIdToken the id token from the auth service
     * @return the updated user
     */
    public static User updateUser(User user, JWTIDToken jwtIdToken) {
        logger.info("User changed auth id: " + jwtIdToken.getEmail() + " - is now: " + jwtIdToken.getSubject() + " . Was before: " + user.getAuthid());
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            UserDao userDao = new UserDao(configuration);
            user.setAuthid(jwtIdToken.getSubject());
            user.setEmail(jwtIdToken.getEmail());
            user.setName(jwtIdToken.getName());
            user.setUsername(jwtIdToken.getEmail());

            userDao.update(user);
            user = userDao.fetchOneByAuthid(user.getAuthid());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }

    /**
     * Creates or updates a user.
     *
     * @param jwtIdToken the id token from the auth service
     * @param accessToken the access token from the auth service
     * @return the created or updated user
     */
    public static User createOrUpdateUser(JWTIDToken jwtIdToken, AccessTokenDTO accessToken) {
        User user = fetchUserByUserNameIgnoreCase(jwtIdToken.getEmail());
        if (user == null) {
            return createUser(jwtIdToken);
        } else {
            return updateUser(user, jwtIdToken);
        }
    }

    /**
     * Get the site of the user
     *
     * @param user the user
     * @return the site he belongs to
     */
    public static Site getSiteForUser(User user) {
        Site site = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);
            
            site = dslContext.select()
                  .from(Tables.USER.join(Tables.USER_SITE).onKey().join(Tables.SITE).onKey())
                  .where(Tables.USER.ID.equal(user.getId()))
                  .fetchOneInto(Site.class);
            
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return site;
    }

    /**
     * Get the site of the user
     *
     * @param user the user
     * @return the id of the site he belongs to
     */
    public static Integer getSiteIdForUser(User user) {
        Site site = getSiteForUser(user);
        if (site == null) {
            return null;
        } else {
            return site.getId();
        }
    }

    /**
     * Get the site of the user
     *
     * @param userId the id of the user
     * @return the id of the site he belongs to
     */
    public static Integer getSiteIdForUserId(int userId) {
        Site site ;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);
            
            site = dslContext.select()
                  .from(Tables.USER.join(Tables.USER_SITE).onKey().join(Tables.SITE).onKey())
                  .where(Tables.USER.ID.equal(userId))
                  .fetchOneInto(Site.class);
            
            return site.getId();
            
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return null;
    }

    /**
     * Set the site for a user
     *
     * @param user the user
     * @param siteId the id of the site
     * @param approved true if this has been verified before, false if not
     */
    public static void setSiteIdForUser(User user, int siteId, boolean approved) {
        Site site;
        SiteDao siteDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            
            siteDao = new SiteDao(configuration);
            site = siteDao.fetchOneById(siteId);
            if (site == null) {
                throw new RuntimeException("Site not found");
            } else {
                UserSiteUtil.setSiteForUser(user, site, approved);
            }

        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Set the site for a user
     *
     * @param userId the id of the user
     * @param siteId the id of the site
     * @param approved true if this has been verified before, false if not
     */
    public static void setSiteIdForUserId(int userId, int siteId, boolean approved) {
        User user;
        UserDao userDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            
            userDao = new UserDao(configuration);
            user = userDao.fetchOneById(userId);
            if (user == null) {
                throw new RuntimeException("User not found");
            } else {
                setSiteIdForUser(user, siteId, approved);
            }

        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Get all users
     *
     * @return a list with all users
     */
    public static List<User> fetchUsers() {
        List<User> users = null;
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userDao = new UserDao(configuration);
            users = userDao.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Fetch user by name (case insensitive)
     *
     * @param userName the user name
     * @return the user
     */
    public static User fetchUserByUserNameIgnoreCase(String userName) {
        User user = null;
        Record record;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            record = create.select().from(Tables.USER).where(Tables.USER.USERNAME.equalIgnoreCase(userName)).fetchOne();
            if (record != null) {
                user = record.into(User.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Fetch user by his auth id.
     *
     * @param authId the id from the auth service
     * @return the user
     */
    public static User fetchUserByAuthId(String authId) {
        User user = null;
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userDao = new UserDao(configuration);
            user = userDao.fetchOneByAuthid(authId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Get the user name for a user id
     *
     * @param userId the id of the user
     * @return the name of the user or "Unbekannt" if none found
     */
    public static String getUsernameById(int userId) {
        User user;
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userDao = new UserDao(configuration);
            user = userDao.fetchOneById(userId);
            if (user != null) {
                return user.getName();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unbekannt";
    }

}
