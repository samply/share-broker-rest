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
import de.samply.share.broker.model.EnumProjectType;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.enums.ProjectStatus;
import de.samply.share.broker.model.db.tables.daos.InquiryDao;
import de.samply.share.broker.model.db.tables.daos.ProjectDao;
import de.samply.share.broker.model.db.tables.daos.SiteDao;
import de.samply.share.broker.model.db.tables.daos.UserDao;
import de.samply.share.broker.model.db.tables.pojos.Inquiry;
import de.samply.share.broker.model.db.tables.pojos.Project;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.UserUtil;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This class provides static methods for CRUD operations for Project Objects
 * 
 * @see de.samply.share.broker.model.db.tables.pojos.Project
 */
public final class ProjectUtil {
    
    private static final Logger logger = LogManager.getLogger(de.samply.share.broker.utils.db.ProjectUtil.class);

    // Prevent instantiation
    private ProjectUtil() {
    }

    /**
     * Get the application number of a project
     *
     * @param projectId the id of the project
     * @return the application number as string, or "-" if none is found
     */
    public static String getProjectApplicationNumber(int projectId) {
        Project project = fetchProjectById(projectId);
        if (project != null && project.getApplicationNumber() != null) {
            return Integer.toString(project.getApplicationNumber());
        } else {
            return "-";
        }
    }

    /**
     * Get all projects that end in a certain amount of days
     *
     * The estimated end date is used, since the real end date will be applied when the project is closed
     *
     * @param days the threshold for project end date in days
     * @return a list of projects that end in the given amount of days
     */
    public static List<Project> getProjectsThatEndInDays(int days) {
        List<Project> projects = null;
        java.sql.Date threshold = new java.sql.Date(SamplyShareUtils.getCurrentDate().getTime() + TimeUnit.DAYS.toMillis(days) );

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            projects = create.select(Tables.PROJECT.fields())
                    .from(Tables.PROJECT)
                    .where(Tables.PROJECT.END_ESTIMATED.equal(threshold))
                    .fetchInto(Project.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Get all list of all projects with a given type
     *
     * @param projectType the type of projects to get
     * @return a list of projects
     */
    public static List<Project> fetchProjectsByType(EnumProjectType projectType) {
        List<Project> projects = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            switch (projectType) {
            case PT_NEW: projects = create.select().from(Tables.PROJECT)
                    .where((Tables.PROJECT.STATUS).in(ProjectStatus.PS_NEW, ProjectStatus.PS_OPEN_MOREINFO_PROVIDED)).fetchInto(Project.class);
                    break;
            case PT_OPEN: projects = create.select().from(Tables.PROJECT)
                    .where((Tables.PROJECT.STATUS).in(ProjectStatus.PS_OPEN_DISTRIBUTION, ProjectStatus.PS_OPEN_MOREINFO_NEEDED)).fetchInto(Project.class);
                    break;
            case PT_ACTIVE: projects = create.select().from(Tables.PROJECT)
                    .where((Tables.PROJECT.STATUS).equal(ProjectStatus.PS_ACTIVE)).fetchInto(Project.class);
                    break;
            case PT_ARCHIVED: projects = create.select().from(Tables.PROJECT)
                    .where((Tables.PROJECT.STATUS).in(ProjectStatus.PS_CLOSED, ProjectStatus.PS_REJECTED)).fetchInto(Project.class);
                    break;
            default: logger.debug("Unknown Project Type (" + projectType.toString() + "). Returning all projects.");
                    projects = create.select().from(Tables.PROJECT).fetchInto(Project.class);
                    break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Get all projects belonging to a user
     *
     * @param user the user whose projects to get
     * @return the list of all projects belonging to that user
     */
    public static List<Project> fetchProjectsByUser(User user) {
        List<Project> projects = null;
        ProjectDao projectDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            projectDao = new ProjectDao(configuration);
            projects = projectDao.fetchByProjectleaderId(user.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    /**
     * Get the project leader for a project
     *
     * @param project the project
     * @return the project leader
     */
    public static User getProjectLeader(Project project) {
        User user = null;
        UserDao userDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            userDao = new UserDao(configuration);
            user = userDao.fetchOneById(project.getProjectleaderId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    /**
     * Get the site of the project leader
     *
     * @param project the project
     * @return the site of the project leader
     */
    public static Site getSite(Project project) {
        return getSite(project.getId());
    }

    /**
     * Get the site of the project leader
     *
     * @param projectId the id of the project
     * @return the site of the project leader
     */
    public static Site getSite(int projectId) {
        Site site = null;
        SiteDao siteDao = null;
        Inquiry inquiry = fetchFirstInquiryForProject(projectId);
        User user = InquiryUtil.getUserForInquiry(inquiry);

        Integer siteIdForUser = UserUtil.getSiteIdForUser(user);
        if (user == null || siteIdForUser == null) {
            return null;
        }

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            siteDao = new SiteDao(configuration);
            site = siteDao.fetchOneById(siteIdForUser);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return site;
    }

    /**
     * Count all projects of the given type
     *
     * @param projectType the type of projects to count
     * @return the amount of projects with that type
     */
    public static Integer countProjects(EnumProjectType projectType) {
        Integer count = 0;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            switch (projectType) {
            case PT_NEW: count = create.fetchCount(Tables.PROJECT, (Tables.PROJECT.STATUS).in(ProjectStatus.PS_NEW, ProjectStatus.PS_OPEN_MOREINFO_PROVIDED));
                    break;
            case PT_OPEN: count = create.fetchCount(Tables.PROJECT, (Tables.PROJECT.STATUS).in(ProjectStatus.PS_OPEN_DISTRIBUTION, ProjectStatus.PS_OPEN_MOREINFO_NEEDED));
                    break;
            case PT_ACTIVE: count = create.fetchCount(Tables.PROJECT, (Tables.PROJECT.STATUS).equal(ProjectStatus.PS_ACTIVE));
                    break;
            case PT_ARCHIVED: count = create.fetchCount(Tables.PROJECT, (Tables.PROJECT.STATUS).in(ProjectStatus.PS_CLOSED, ProjectStatus.PS_REJECTED));
                    break;
            default: logger.debug("Unknown Project Type (" + projectType.toString() + "). Counting all projects.");
                    count = create.fetchCount(Tables.PROJECT);
                    break;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Update a project
     *
     * @param project the project to update
     */
    public static void updateProject(Project project) {
        ProjectDao projectDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            projectDao = new ProjectDao(configuration);
            projectDao.update(project);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all inquiries that belong to a project
     *
     * @param projectId the id of the project
     * @return the list of all inquiries for that project
     */
    public static List<Inquiry> fetchInquiriesForProject(int projectId) {
        List<Inquiry> inquiries = null;
        InquiryDao inquiryDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryDao = new InquiryDao(configuration);
            inquiries = inquiryDao.fetchByProjectId(projectId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inquiries;
    }

    /**
     * Get the first inquiry for a project
     *
     * @param projectId the id of the project
     * @return the first inquiry for that project
     */
    public static Inquiry fetchFirstInquiryForProject(int projectId) {
        List<Inquiry> inquiries = null;
        InquiryDao inquiryDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryDao = new InquiryDao(configuration);
            inquiries = inquiryDao.fetchByProjectId(projectId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (inquiries == null || inquiries.size() == 0) {
            logger.error("No Inquiry associated with project " + projectId);
            return null;
        } else {
            return inquiries.get(0);
        }
    }

    /**
     * Get all project partners for a project
     *
     * @param project the project
     * @return a list of all project partners
     */
    public static List<Site> fetchProjectPartners(Project project) {
        List<Site> sites = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            sites = create.select()
                    .from(Tables.PROJECT.join(Tables.PROJECT_SITE).onKey().join(Tables.SITE).onKey())
                    .where((Tables.PROJECT.ID).equal(project.getId())).fetchInto(Site.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sites;
    }

    /**
     * Set all project partners for a project
     *
     * @param projectId the id of the project
     * @param partnerIds the ids of the partner sites
     * @return true on success, false on error
     */
    public static boolean setProjectPartnersForProject(int projectId, List<String> partnerIds) {
        boolean ret = true;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext dslContext = ResourceManager.getDSLContext(conn);

            for (String site : partnerIds) {
                dslContext.insertInto(Tables.PROJECT_SITE, Tables.PROJECT_SITE.PROJECT_ID, Tables.PROJECT_SITE.SITE_ID)
                        .values(projectId, Integer.parseInt(site)).execute();
            }

        } catch (SQLException e) {
            logger.error("Error adding project partners for project " + projectId);
            ret = false;
        }

        return ret;
    }

    /**
     * Get a project by its id
     *
     * @param projectId the id of the project
     * @return the project
     */
    public static Project fetchProjectById(int projectId) {
        Project project = null;
        ProjectDao projectDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            projectDao = new ProjectDao(configuration);
            project = projectDao.fetchOneById(projectId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return project;
    }

    /**
     * Get the project belonging to an inquiry
     *
     * @param inquiry the inquiry
     * @return the project belonging to this inquiry, or null if no project belongs to the inquiry
     */
    public static Project fetchProjectByInquiry(Inquiry inquiry) {
        Integer projectId = inquiry.getProjectId();
        if (projectId == null || projectId == 0) {
            return null;
        } else {
            return fetchProjectById(projectId);
        }
    }

    /**
     * Get the project belonging to an inquiry
     *
     * @param inquiryId the id of the inquiry
     * @return the project belonging to this inquiry, or null if no project belongs to the inquiry
     */
    public static Project fetchProjectByInquiryId(int inquiryId) {
        Inquiry inquiry;
        InquiryDao inquiryDao;
        Project project = null;
        ProjectDao projectDao;

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.fetchOneById(inquiryId);
            
            Integer projectId = inquiry.getProjectId();
            if (projectId == null || projectId == 0) {
                logger.debug("No project id found for inquiry with id " + inquiryId);
                return null;
            }
            
            projectDao = new ProjectDao(configuration);
            project = projectDao.fetchOneById(projectId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return project;
    }

    /**
     * Get the title of a project
     *
     * @param projectId the id of the project
     * @return the title of the project
     */
    public static String getProjectTitleById(int projectId) {
        Project project;
        ProjectDao projectDao;
        String projectTitle = "";

        try (Connection conn = ResourceManager.getConnection() ) {
            Configuration configuration = new DefaultConfiguration().set(conn).set(SQLDialect.POSTGRES);
            projectDao = new ProjectDao(configuration);
            project = projectDao.fetchOneById(projectId);
            projectTitle = project.getName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projectTitle;
    }

    /**
     * Get all project partners for a project
     *
     * @param projectId the id of the project
     * @return the list of partner sites
     */
    public static List<Site> fetchProjectPartnersForProject(int projectId) {
        List<Site> sites = null;

        try (Connection conn = ResourceManager.getConnection() ) {
            DSLContext create = ResourceManager.getDSLContext(conn);

            sites = create.select()
                    .from(Tables.PROJECT.join(Tables.PROJECT_SITE).onKey().join(Tables.SITE).onKey())
                    .where((Tables.PROJECT.ID).equal(projectId)).fetchInto(Site.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sites;
    }

}
