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
import de.samply.share.broker.model.db.tables.daos.BankDao;
import de.samply.share.broker.model.db.tables.daos.BankSiteDao;
import de.samply.share.broker.model.db.tables.daos.SiteDao;
import de.samply.share.broker.model.db.tables.pojos.Bank;
import de.samply.share.broker.model.db.tables.pojos.BankSite;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.utils.MailUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 *  This class provides static methods for CRUD operations for BankSite Objects
 *  
 *  @see de.samply.share.broker.model.db.tables.pojos.BankSite
 */
public final class BankSiteUtil {
    
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.db.BankSiteUtil.class);
    
    // Prevent instantiation
    private BankSiteUtil() {
    }

    /**
     * Update a bank-site relation
     *
     * @param bankSite the bank-site relation to update
     */
    public static void updateBankSite(BankSite bankSite) {
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            BankSiteDao bankSiteDao = new BankSiteDao(configuration);
            bankSiteDao.update(bankSite);
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Assign a bank to a site
     *
     * @param bank the bank to assign
     * @param site the site to assign the bank to
     * @param approved has the assignment been checked and assured that it is correct?
     */
    public static void setSiteForBank(Bank bank, Site site, boolean approved) {
        BankSite newBankSite = null;
        BankSite oldBankSite = null;
        BankSiteDao bankSiteDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);

            bankSiteDao = new BankSiteDao(configuration);
            List<BankSite> bankSites = bankSiteDao.fetchByBankId(bank.getId());
            
            if (bankSites != null && !bankSites.isEmpty()) {
                oldBankSite = bankSites.get(0);
                bankSiteDao.delete(oldBankSite);
            }

            newBankSite = new BankSite();
            newBankSite.setApproved(approved);
            newBankSite.setSiteId(site.getId());
            newBankSite.setBankId(bank.getId());
            bankSiteDao.insert(newBankSite);            
        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
        
        if (oldBankSite != null && !oldBankSite.getSiteId().equals(newBankSite.getSiteId()) && !approved) {
            MailUtils.sendBankSiteMail(bank, site);
        }
    }


    /**
     * Assign a bank to a site
     *
     * @param bank the bank to assign
     * @param siteId the id of the site to assign the bank to
     * @param approved has the assignment been checked and assured that it is correct?
     */
    public static void setSiteIdForBank(Bank bank, int siteId, boolean approved) {
        Site site;
        SiteDao siteDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            
            siteDao = new SiteDao(configuration);
            site = siteDao.fetchOneById(siteId);
            if (site == null) {
                throw new RuntimeException("Site not found");
            } else {
                setSiteForBank(bank, site, approved);
            }

        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Assign a bank to a site
     *
     * @param bankId the id of the bank to assign
     * @param siteId the id of the site to assign the bank to
     * @param approved has the assignment been checked and assured that it is correct?
     */
    public static void setSiteIdForBankId(int bankId, int siteId, boolean approved) {
        Bank bank;
        BankDao bankDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            
            bankDao = new BankDao(configuration);
            bank = bankDao.fetchOneById(bankId);
            if (bank == null) {
                throw new RuntimeException("Bank not found");
            } else {
                setSiteIdForBank(bank, siteId, approved);
            }

        } catch (SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Get all assignments between banks and sites
     *
     * @return list of all assignments between banks and sites
     */
    public static List<BankSite> fetchBankSites() {
        BankSiteDao bankSiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            bankSiteDao = new BankSiteDao(configuration);

            return bankSiteDao.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the assignment between bank and site for a given bank
     *
     * @param bank the bank for which to get the assignment
     * @return the assignment between bank and site
     */
    public static BankSite fetchBankSiteByBank(Bank bank) {
        List<BankSite> bankSites;
        BankSiteDao bankSiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            bankSiteDao = new BankSiteDao(configuration);
            
            bankSites = bankSiteDao.fetchByBankId(bank.getId());
            // There shall be but one assignment
            if (bankSites != null && bankSites.size() == 1) {
                return bankSites.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get the assignment between bank and site for a given bank id
     *
     * @param bankId the id of the bank for which to get the assignment
     * @return the assignment between bank and site
     */
    public static BankSite fetchBankSiteByBankId(int bankId) {
        List<BankSite> bankSites;
        BankSiteDao bankSiteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            bankSiteDao = new BankSiteDao(configuration);
            
            bankSites = bankSiteDao.fetchByBankId(bankId);
            // There shall be but one assignment
            if (bankSites != null && bankSites.size() == 1) {
                return bankSites.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete a bank to site assignment
     *
     * @param bankSite the bank to site assignment to delete
     */
    private static void deleteBankSite(BankSite bankSite) {
        BankSiteDao bankSiteDao;
        
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            bankSiteDao = new BankSiteDao(configuration);
            
            bankSiteDao.delete(bankSite);
        } catch(SQLException e) {
            logger.error("SQL Exception caught", e);
        }
    }

    /**
     * Delete the site assignment of a bank
     *
     * @param bank the bank for which to clear the assignment to a site
     */
    public static void deleteSiteFromBank(Bank bank) {
        BankSite bankSite = fetchBankSiteByBank(bank);
        if (bankSite != null) {
            deleteBankSite(bankSite);
        }
    }
}
