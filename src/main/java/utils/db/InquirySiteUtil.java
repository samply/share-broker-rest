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
package utils.db;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.daos.InquirySiteDao;
import de.samply.share.broker.model.db.tables.pojos.InquirySite;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides static methods for CRUD operations for InquirySite
 * Objects
 * 
 * @see de.samply.share.broker.model.db.tables.pojos.InquirySite
 */
public class InquirySiteUtil {
    
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.db.InquirySiteUtil.class);

    // Prevent instantiation
    private InquirySiteUtil() {

    }

    /**
     * Update an inquiry to site association
     *
     * @param inquirySite the inquiry site association to update
     */
    public static void updateInquirySite(InquirySite inquirySite) {
        InquirySiteDao inquirySiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquirySiteDao = new InquirySiteDao(configuration);
            inquirySiteDao.update(inquirySite);
        } catch (SQLException e) {
            logger.error("Error while trying to update InquirySite.", e);
        }
    }

    /**
     * Get all site associations for a given inquiry
     *
     * @param inquiryId the id of the inquiry
     * @return a list of all inquiry to site associations belonging to that inquiry
     */
    public static List<InquirySite> fetchInquirySitesForInquiryId(int inquiryId) {
        List<InquirySite> inquirySites;
        InquirySiteDao inquirySiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquirySiteDao = new InquirySiteDao(configuration);
            
            inquirySites = inquirySiteDao.fetchByInquiryId(inquiryId);
            return inquirySites;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
