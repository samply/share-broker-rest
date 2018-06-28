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
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.daos.BankDao;
import de.samply.share.broker.model.db.tables.pojos.Bank;
import de.samply.share.broker.model.db.tables.pojos.Site;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *  This class provides static methods for CRUD operations for Bank Objects
 *  
 *  @see de.samply.share.broker.model.db.tables.pojos.Bank
 */
public final class BankUtil {
    
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.db.BankUtil.class);
    
    // Prevent instantiation
    private BankUtil() {
    }

    /**
     * Get the site for a bank
     *
     * @param bank the bank for which to get the site
     * @return the site the bank is assigned to
     */
    public static Site getSiteForBank(Bank bank) {
        Site site = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);
            
            site = dslContext.select()
                  .from(Tables.BANK.join(Tables.BANK_SITE).onKey().join(Tables.SITE).onKey())
                  .where(Tables.BANK.ID.equal(bank.getId()))
                  .fetchOneInto(Site.class);
            
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return site;
    }

    /**
     * Get the site for a bank
     *
     * @param bankId the id of the bank for which to get the site
     * @return the site the bank is assigned to
     */
    public static Site getSiteForBankId(int bankId) {
        Site site;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);
            
            site = dslContext.select()
                  .from(Tables.BANK.join(Tables.BANK_SITE).onKey().join(Tables.SITE).onKey())
                  .where(Tables.BANK.ID.equal(bankId))
                  .fetchOneInto(Site.class);
            
            return site;
            
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        return null;
    }

    /**
     * Get the site id for a bank
     *
     * @param bank the bank for which to get the site
     * @return the id of the site the bank is assigned to
     */
    public static Integer getSiteIdForBank(Bank bank) {
        Site site = getSiteForBank(bank);
        if (site == null) {
            return null;
        } else {
            return site.getId();
        }
    }

    /**
     * Get the site id for a bank
     *
     * @param bankId the id of the bank for which to get the site
     * @return the id of the site the bank is assigned to
     */
    public static Integer getSiteIdForBankId(int bankId) {
        Site site = getSiteForBankId(bankId);
        if (site == null) {
            return null;
        } else {
            return site.getId();
        }
    }

    /**
     * Get all banks
     *
     * @return a list of all banks
     */
    public static List<Bank> fetchBanks() {
        List<Bank> banks = null;
        BankDao bankDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            bankDao = new BankDao(configuration);
            banks = bankDao.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return banks;
    }

}
