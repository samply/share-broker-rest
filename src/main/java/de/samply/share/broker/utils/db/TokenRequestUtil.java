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

import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.DatePart;
import org.jooq.Record;
import org.jooq.impl.DSL;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.pojos.Tokenrequest;

/**
 * This class provides static methods for CRUD operations for TokenRequest
 * Objects
 * 
 * @see Tokenrequest
 */
public final class TokenRequestUtil {

    private static final Logger logger = LogManager.getLogger(TokenRequestUtil.class);
    
    // Prevent instantiation
    private TokenRequestUtil() {
    }
    
    // Delete Token Requests older than 7 days and those with a future date
    /**
     * Delete old token requests.
     */
    public static void deleteOldTokenRequests() {
        int affectedRows = 0;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            affectedRows = create.delete(Tables.TOKENREQUEST)
                    .where(DSL.currentTimestamp().greaterThan(DSL.timestampAdd(Tables.TOKENREQUEST.ISSUED, 7, DatePart.DAY)))
                    .or(Tables.TOKENREQUEST.ISSUED.greaterThan(DSL.currentTimestamp()))
                    .execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (affectedRows > 0) {
            logger.info("Deleted " + affectedRows + " old tokenRequests");
        }
    }
    
    /**
     * Creates the token request for email.
     *
     * @param email the email
     * @return the tokenrequest
     */
    public static Tokenrequest createTokenRequestForEmail(String email) {
        Tokenrequest tr = new Tokenrequest();
        tr.setAuthcode(SamplyShareUtils.createRandomDigitString(8));
        tr.setEmail(email);

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            Record record =
                    create.insertInto(Tables.TOKENREQUEST, Tables.TOKENREQUEST.AUTHCODE, Tables.TOKENREQUEST.EMAIL)
                          .values(tr.getAuthcode(), tr.getEmail())
                          .returning(Tables.TOKENREQUEST.ID, Tables.TOKENREQUEST.ISSUED)
                          .fetchOne();

            tr.setId(record.getValue(Tables.TOKENREQUEST.ID));
            tr.setIssued(record.getValue(Tables.TOKENREQUEST.ISSUED));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tr;
    }
}
