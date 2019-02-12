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
import java.util.ArrayList;
import java.util.List;

import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import de.samply.auth.rest.LocationDTO;
import de.samply.auth.rest.LocationListDTO;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.tables.daos.SiteDao;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.utils.Utils;

/**
 * This class provides static methods for CRUD operations for Site Objects
 * 
 * @see Site
 */
public final class SiteUtil {
    
    private static final Logger logger = LogManager.getLogger(SiteUtil.class);

    // Prevent instantiation
    private SiteUtil() {
    }

    /**
     * Insert sites to the database
     *
     * @param locationList The sites (aka locations), as received from the authentication service
     */
    public static void insertSites(LocationListDTO locationList) {
        List<Site> persistedSites = fetchSites();
        List<Site> newSites = new ArrayList<>();
        List<String> siteNames = Utils.getSiteNames(persistedSites);

        for (LocationDTO location : locationList.getLocations()) {
            logger.debug(location.getId());
            if (siteNames.contains(location.getId())) {
                continue;
            }
            Site newSite = new Site();
            newSite.setContact(location.getContact());
            newSite.setDescription(location.getDescription());
            newSite.setName(location.getId());
            newSite.setNameExtended(location.getName());
            newSites.add(newSite);
        }

        SiteDao siteDao;
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);
            siteDao.insert(newSites);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all sites
     *
     * @return a list with all sites
     */
    public static List<Site> fetchSites() {
        List<Site> sites = null;
        SiteDao siteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);
            sites = siteDao.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sites;
    }

    /**
     * Get all sites an inquiry is distributed to
     *
     * @param inquiryId the id of the inquiry
     * @return a list of sites where this inquiry is distributed to
     */
    public static List<Site> fetchSitesForInquiry(int inquiryId) {
        List<Site> sites = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            sites = create.select()
//                    .from(Tables.INQUIRY.join(Tables.INQUIRY_SITE).on(Tables.INQUIRY_SITE.INQUIRY_ID.equal(Tables.INQUIRY.ID)))
                    .from(Tables.INQUIRY.join(Tables.INQUIRY_SITE).onKey().join(Tables.SITE).onKey())
                    .where((Tables.INQUIRY.ID).equal(inquiryId)).fetchInto(Site.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sites;
    }

    /**
     * Insert a list of sites into the database
     *
     * @param sites a list of sites to add
     */
    public static void insertSites(List<Site> sites) {
        List<Site> persistedSites = fetchSites();
        List<Site> newSites = new ArrayList<>();
        List<String> siteNames = Utils.getSiteNames(persistedSites);

        for (Site site : sites) {
            if (siteNames.contains(site.getName())) {
                continue;
            }
            newSites.add(site);
        }

        SiteDao siteDao;
        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);
            siteDao.insert(newSites);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a site by its (short) name
     *
     * @param name (short) name of the site
     * @return the site
     */
    public static Site fetchSiteByName(String name) {
        Site site = null;
        SiteDao siteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);
            site = siteDao.fetchOneByName(name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return site;
    }

    /**
     * Get a site by its id
     *
     * @param id the id of the site
     * @return the site
     */
    public static Site fetchSiteById(int id) {
        Site site = null;
        SiteDao siteDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);
            site = siteDao.fetchOneById(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return site;
    }

    /**
     * Get a site by its (short) name - case insensitive
     *
     * @param siteName (short) name of the site
     * @return the site
     */
    public static Site fetchSiteByNameIgnoreCase(String siteName) {
        Site site = null;
        Record record;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            record = create.select().from(Tables.SITE).where(Tables.SITE.NAME.equalIgnoreCase(siteName)).fetchOne();
            if (record != null) {
                site = record.into(Site.class);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return site;
    }

    /**
     * Get a list of site names for a list of site ids
     *
     * @param siteIdList a list of site ids
     * @return the list of site names in the same order as the ids were
     */
    public static List<String> getSiteNamesByIds(List<String> siteIdList) {
        List<Site> sites;
        SiteDao siteDao;
        List<String> siteNames = new ArrayList<>();

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);           
            
            sites = siteDao.fetchById(SamplyShareUtils.convertStringListToIntegerArray(siteIdList));
            for (Site site : sites) {
                siteNames.add(site.getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return siteNames;
    }
}
