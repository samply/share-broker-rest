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

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.daos.UserConsentDao;
import de.samply.share.broker.model.db.tables.pojos.Consent;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.model.db.tables.pojos.UserConsent;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class provides static methods for CRUD operations for UserConsent
 * Objects
 * 
 * @see de.samply.share.broker.model.db.tables.pojos.UserConsent
 */
public final class UserConsentUtil {
    
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.db.UserConsentUtil.class);

    // Prevent instantiation
    private UserConsentUtil() {
    }

    /**
     * Get the latest consent item
     *
     * @return the latest consent item
     */
    public static Consent getLatestConsent() {
        try (Connection connection = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            Consent consent = dslContext.select()
                                        .from(Tables.CONSENT)
                                        .orderBy(Tables.CONSENT.CREATED.desc())
                                        .limit(1)
                                        .fetchOneInto(Consent.class);
            if (consent == null) {
                return null;
            } else {
                return consent;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Check if a user has given his consent
     *
     * @param user the user
     * @param consent the consent
     * @return true if he agreed, false if not
     */
    private static boolean hasUserGivenConsent(User user, Consent consent) {
        try (Connection connection = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);
            int count = dslContext.selectCount()
                          .from(Tables.USER)
                              .join(Tables.USER_CONSENT).onKey()
                              .join(Tables.CONSENT).onKey()
                          .where(
                              Tables.USER.ID.equal(user.getId())
                              .and(Tables.CONSENT.ID.equal(consent.getId()))
                              .and(Tables.USER_CONSENT.GRANTED.isNotNull())
                              .and(Tables.USER_CONSENT.REVOKED.isNull()
                                  .or(Tables.USER_CONSENT.GRANTED.greaterOrEqual(Tables.USER_CONSENT.REVOKED))
                              )
                          ).fetchOne(0, int.class);
            logger.debug("User " + user.getId() +  " => " + (count > 0));
            return (count > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // TODO: Maybe think of some SQL magic to improve the queries
    /**
     * Check if the user has agreed to the latest consent item
     *
     * @param user the user
     * @return true if he agreed, false if not
     */
    public static boolean hasUserGivenLatestConsent(User user) {
        Consent consent = getLatestConsent();
        if (consent == null) {
            logger.info("Could not find latest consent version. Maybe none is required? Granting access.");
            return true;
        } else {
            logger.debug("Latest Consent is version " + consent.getVersion() + " from " + consent.getCreated());
        }

        return hasUserGivenConsent(user, getLatestConsent());
    }

    /**
     * Deletes a given consent for a user (if present)
     *
     * @param user the user
     * @param consent the consent item
     */
    private static void deleteUserConsent(User user, Consent consent) {
        try (Connection connection = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);
            dslContext.deleteFrom(Tables.USER_CONSENT)
                        .where(Tables.USER_CONSENT.USER_ID.equal(user.getId())
                                .and(Tables.USER_CONSENT.CONSENT_ID.equal(consent.getId()))
                        ).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds the given consent to the user
     *
     * @param user the user
     * @param consent the consent item
     */
    private static void addUserConsent(User user, Consent consent) {
        UserConsent userConsent = null;
        UserConsentDao userConsentDao = null;

        try (Connection connection = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            userConsent = new UserConsent();
            userConsentDao = new UserConsentDao(configuration);

            userConsent.setConsentId(consent.getId());
            userConsent.setUserId(user.getId());
            userConsent.setGranted(SamplyShareUtils.getCurrentSqlTimestamp());

            userConsentDao.insert(userConsent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the consent status for the latest consent item for the user
     *
     * @param user the user
     * @param userConsent true if the user gave his consent, false if not
     */
    public static void updateConsent(User user, boolean userConsent) {
        Consent consent = getLatestConsent();
        if (consent == null) {
            logger.error("Could not find latest consent version. Maybe none is required? Granting access.");
            return;
        } else {
            logger.debug("Latest Consent is version " + consent.getVersion() + " from " + consent.getCreated());
        }

        // As of now, just delete the consent. Maybe set to revoked if necessary.
        if (!userConsent) {
            deleteUserConsent(user, consent);
        } else {
            if (!hasUserGivenConsent(user, getLatestConsent())) {
                addUserConsent(user, consent);
            }
        }
    }

}
