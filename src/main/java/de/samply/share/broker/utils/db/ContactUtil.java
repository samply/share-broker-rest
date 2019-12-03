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

import com.google.common.base.Joiner;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.daos.ContactDao;
import de.samply.share.broker.model.db.tables.daos.UserDao;
import de.samply.share.broker.model.db.tables.pojos.Contact;
import de.samply.share.broker.model.db.tables.pojos.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * This class provides static methods for CRUD operations for Contact Objects
 * 
 * @see Contact
 */
public final class ContactUtil {
    
    private static final Logger logger = LogManager.getLogger(ContactUtil.class);

    // Prevent instantiation
    private ContactUtil() {
    }
    
    /**
     * Gets the contact for a user.
     *
     * @param user the user
     * @return the contact for the user
     */
    public static Contact getContactForUser(User user) {
        ContactDao contactDao;
        Contact contact = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            contactDao = new ContactDao(configuration);
            contact = contactDao.fetchOneById(user.getContactId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contact;
    }

    /**
     * Update a contact.
     *
     * @param contact the contact to update
     * @return "success" or "error" depending on the outcome
     */
    public static String updateContact(Contact contact) {
        ContactDao contactDao;
        String ret = "success";

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            contactDao = new ContactDao(configuration);
            contactDao.update(contact);

        } catch (SQLException e) {
            e.printStackTrace();
            ret = "error";
        }
        return ret;
    }
    
    /**
     * Creates a contact for a user.
     *
     * @param user the user
     * @return the contact
     */
    public static Contact createContactForUser(User user) {
        ContactDao contactDao;
        Contact contact = null;
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            DSLContext create = ResourceManager.getDSLContext(conn);

            Record record = create.insertInto(Tables.CONTACT, Tables.CONTACT.LASTNAME).values(" ").returning(Tables.CONTACT.ID).fetchOne();

            int contactId = record.getValue(Tables.INQUIRY.ID);

            userDao = new UserDao(configuration);
            user.setContactId(contactId);
            userDao.update(user);

            contactDao = new ContactDao(configuration);
            contact = contactDao.fetchOneById(contactId);
            
            contact.setEmail(user.getEmail());
            
            try {
                String[] names = user.getName().split("\\s+");
                if (names.length == 2) {
                    contact.setFirstname(names[0]);
                    contact.setLastname(names[1]);
                } else if (names.length == 1) {
                    contact.setLastname(names[0]);
                } else if (names.length < 1) {
                    logger.warn("No name provided");
                } else {
                    String lastName = names[names.length - 1];
                    String[] firstNames = Arrays.copyOf(names, names.length - 1);
                    contact.setLastname(lastName);
                    contact.setFirstname(Joiner.on(" ").join(firstNames));
                }
            } catch (Exception e) {
                logger.error("Error trying to set names to contact.", e);
            }
            
            contactDao.update(contact);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contact;
    }
}
