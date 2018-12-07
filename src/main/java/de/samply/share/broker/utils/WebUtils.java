/*
 * Copyright (C) 2015 Working Group on Joint Research,
 * Division of Medical Informatics,
 * Institute of Medical Biometrics, Epidemiology and Informatics,
 * University Medical Center of the Johannes Gutenberg University Mainz
 *
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
package de.samply.share.broker.utils;

import de.samply.common.mdrclient.MdrClient;
import de.samply.common.mdrclient.MdrConnectionException;
import de.samply.common.mdrclient.MdrInvalidResponseException;
import de.samply.common.mdrclient.domain.Meaning;
import de.samply.common.mdrclient.domain.PermissibleValue;
import de.samply.common.mdrclient.domain.Validations;
import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.EnumProjectType;
import de.samply.share.broker.model.db.enums.InquiryStatus;
import de.samply.share.broker.model.db.tables.pojos.Inquiry;
import de.samply.share.broker.model.db.tables.pojos.Project;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.ProjectUtil;
import de.samply.share.broker.utils.db.SiteUtil;
import de.samply.share.broker.utils.db.UserUtil;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.SamplyShareUtils;
import de.samply.share.common.utils.oauth2.OAuthConfig;
import de.samply.web.mdrFaces.MdrContext;

import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Utilities that are made available via the webutils taglib
 */
public final class WebUtils {

    private static final String VALIDATION_DATATYPE_ENUMERATED = "enumerated";
    private static final String MDRKEY_RESULT_TYPE = "mdrKey_result_type";

    private static final long ONE_MINUTE_IN_MS = 60 * 1000;
    private static final long ONE_HOUR_IN_MS = 60 * ONE_MINUTE_IN_MS;
    private static final long ONE_DAY_IN_MS = 24 * ONE_HOUR_IN_MS;
//    private static final long ONE_WEEK_IN_MS = 7 * ONE_DAY_IN_MS;

    /**
     * Prevent instantiation
     */
    private WebUtils() {
    }

    /**
     * Get the application name to be displayed
     *
     * @return the name of the project to display
     */
    public static String getDisplayName() {
        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("osse")) {
            return "OSSE.Share Broker";
        } else if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")) {
            return "DKTK.Search Broker";
        } else {
            return "Samply.Share Broker";
        }
    }

    /**
     * Gets the designation of a dataelement
     *
     * @param dataElement the data element mdr id
     * @param languageCode the language code
     * @return the designation of the dataelement
     */
    public static String getDesignation(String dataElement, String languageCode) {

        MdrClient mdrClient = MdrContext.getMdrContext().getMdrClient();

        try {
            return mdrClient.getDataElementDefinition(dataElement, languageCode).getDesignations().get(0).getDesignation();
        } catch (MdrConnectionException | MdrInvalidResponseException | ExecutionException e) {
            e.printStackTrace();
            return ("??" + dataElement + "??");
        }

    }

    /**
     * Get the designation of a value of a dataelement
     *
     * @param dataElement the data element mdr id
     * @param value the permitted value for which the designation shall be got
     * @param languageCode the language code
     * @return the designation of the value
     */
    public static String getValueDesignation(String dataElement, String value, String languageCode) {
        MdrClient mdrClient = MdrContext.getMdrContext().getMdrClient();

        try {
            Validations validations = mdrClient.getDataElementValidations(dataElement, languageCode);
            String dataType = validations.getDatatype();
            if (dataType.equalsIgnoreCase(VALIDATION_DATATYPE_ENUMERATED)) {
                List<PermissibleValue> permissibleValues = validations.getPermissibleValues();
                for (PermissibleValue pv : permissibleValues) {
                    List<Meaning> meanings = pv.getMeanings();
                    if (pv.getValue().equals(value)) {
                        for (Meaning m : meanings) {
                            if (m.getLanguage().equalsIgnoreCase(languageCode)) {
                                return m.getDesignation();
                            }
                        }
                    }
                }
            }
        } catch (MdrConnectionException | MdrInvalidResponseException | ExecutionException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Convert timestamp to "dd.MM.yyyy HH:mm"
     *
     * @param timestamp the timestamp to convert
     * @return the string representation of the timestamp in format "dd.MM.yyyy HH:mm"
     */
    public static String convertTimestamp(Timestamp timestamp) {
        if (timestamp == null)
            return "";
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(timestamp);
    }

    /**
     * Convert time to "HH:mm" format
     *
     * @param time the time to convert
     * @return the string representation of the time in format "HH:mm"
     */
    public static String convertTime(Time time) {
        if (time == null)
            return "";
        return new SimpleDateFormat("HH:mm").format(time);
    }

    /**
     * Convert a time difference between the current timestamp and an sql date and a time to human readable format
     *
     * @param date the date
     * @param time the time
     * @return the difference between the given date+time and today in human readable format
     */
    public static String convertTimedifference(Date date, Time time) {
        boolean dateOnly = (time == null);
        if (date == null)
            return "";

        long dateDiff = SamplyShareUtils.getDateDiff(SamplyShareUtils.getCurrentDate(), date, TimeUnit.DAYS);

        if (dateDiff == 0) {
            if (dateOnly) {
                return Messages.getString("tu_today");
            } else {
                long timeDiff = date.getTime() + time.getTime() - SamplyShareUtils.getCurrentSqlTimestamp().getTime();
                if (timeDiff < ONE_MINUTE_IN_MS) {
                    return Messages.getString("tu_now");
                } else if (timeDiff < (2 * ONE_MINUTE_IN_MS) ) {
                    return "in " + timeDiff / ONE_MINUTE_IN_MS  + " " + Messages.getString("tu_minute");
                } else if (timeDiff < ONE_HOUR_IN_MS) {
                    return "in " + timeDiff / ONE_MINUTE_IN_MS  + " " + Messages.getString("tu_minutes");
                } else if (timeDiff < (2 * ONE_HOUR_IN_MS) ) {
                    return "in " + timeDiff / ONE_HOUR_IN_MS  + " " + Messages.getString("tu_hour");
                } else if (timeDiff < ONE_DAY_IN_MS) {
                    return "in " + timeDiff / ONE_HOUR_IN_MS  + " " + Messages.getString("tu_hours");
                } else if (timeDiff < (2 * ONE_DAY_IN_MS)) {
                    return Messages.getString("tu_tomorrow");
                } else if (timeDiff < (3 * ONE_DAY_IN_MS)) {
                    return Messages.getString("tu_dayAfterTomorrow");
                } else {
                    return "in " + timeDiff / ONE_DAY_IN_MS  + " " + Messages.getString("tu_days");
                }
            }
        } else if (dateDiff == -1) {
            return Messages.getString("tu_yesterday");
        } else if (dateDiff == 1) {
            return Messages.getString("tu_tomorrow");
        } else if (dateDiff < 0) {
            return "vor " + Math.abs(dateDiff) + " Tagen";
        } else {
            return "in " + Math.abs(dateDiff) + " Tagen";
        }

    }

    /**
     * Get a css class for a table, depending on the timestamp mentioned in this table row
     *
     * @param time the timestamp mentioned in the table row
     * @return "danger" if it's less than a day away
     *         "warning" if it's between one and three days away
     *         "" if there's still more time
     */
    public static String getTableRowClass(Timestamp time) {
        if (time == null) {
            return "";
        }
        long timeDiff = time.getTime() - SamplyShareUtils.getCurrentSqlTimestamp().getTime();
        if (timeDiff < ONE_DAY_IN_MS) {
            return "danger";
        } else if (timeDiff < (3 * ONE_DAY_IN_MS)) {
            return "warning";
        } else {
            return "";
        }
    }

    /**
     * Get a css class for a table, depending on the date mentioned in this table row
     *
     * @param date the date mentioned in the table row
     * @return "danger" if it's less than a day away
     *         "warning" if it's between one and three days away
     *         "" if there's still more time
     */
    public static String getTableRowClass(Date date) {
        if (date == null) {
            return "";
        }
        long dateDiff = SamplyShareUtils.getDateDiff(SamplyShareUtils.getCurrentDate(), date, TimeUnit.DAYS);
        if (dateDiff <= 0 ) {
            return "danger";
        } else if (dateDiff <= 2 ) {
            return "warning";
        } else {
            return "";
        }
    }

    /**
     * Convert a date to German ISO format
     *
     * @param isoDate the date to format
     * @return dd.MM.yyyy representation of the given date as a string
     */
    public static String isoToGermanDateString(Date isoDate) {
        if (isoDate == null)
            return "";
        return new SimpleDateFormat("dd.MM.yyyy").format(isoDate);
    }

    /**
     * Gets the operator.
     *
     * @param operator the operator
     * @return the operator
     */
    public static String getOperator(String operator) {
        String ret = operator;

        if (operator.equalsIgnoreCase("eq")) {
            ret = " == ";
        } else if (operator.equalsIgnoreCase("neq")) {
            ret = " != ";
        } else if (operator.equalsIgnoreCase("leq")) {
            ret = " ≤ ";
        } else if (operator.equalsIgnoreCase("geq")) {
            ret = " ≥ ";
        } else if (operator.equalsIgnoreCase("gt")) {
            ret = " > ";
        } else if (operator.equalsIgnoreCase("lt")) {
            ret = " < ";
        } else if (operator.equalsIgnoreCase("isnull")) {
            ret = " ist null";
        } else if (operator.equalsIgnoreCase("isnotnull")) {
            ret = " ist nicht null";
        } else if (operator.equalsIgnoreCase("like")) {
            ret = " ~ ";
        }

        return ret;
    }

    /**
     * Gets the project name.
     *
     * @return the project name
     */
    public static String getProjectName() {
        return ProjectInfo.INSTANCE.getProjectName();
    }

    /**
     * Gets the version string.
     *
     * @return the version string
     */
    public static String getVersionString() {
        return ProjectInfo.INSTANCE.getVersionString();
    }

    /**
     * Gets the alert classes.
     *
     * @param severity the severity
     * @return the alert classes
     */
    public static String getAlertClasses(String severity) {

        if (severity.toLowerCase().startsWith("info")) {
            return "alert alert-info alert-dismissible";
        } else if (severity.toLowerCase().startsWith("warn")) {
            return "alert alert-warning alert-dismissible";
        } else {
            return "alert alert-danger alert-dismissible";
        }
    }

    /**
     * Get the Login URL for the authentication service
     *
     * @return the Login URL
     */
    public static String getAuthUrl() throws UnsupportedEncodingException {
        return OAuthConfig.getAuthLoginUrl();
    }

    /**
     * Get the author name of an inquiry
     *
     * @param inquiry the inquiry
     * @return the name of the author
     */
    public static String getAuthorName(Inquiry inquiry) {
        if (inquiry == null) {
            return "Unbekannt";
        }
        User user = InquiryUtil.getUserForInquiry(inquiry);
        if (user != null) {
            return user.getName();
        } else {
            return "Unbekannt";
        }
    }

    /**
     * Get the author username of an inquiry
     *
     * @param inquiry the inquiry
     * @return the username of the author
     */
    public static String getAuthorUsername(Inquiry inquiry) {
        User user = InquiryUtil.getUserForInquiry(inquiry);
        if (user != null) {
            return user.getUsername();
        } else {
            return "Unbekannt";
        }
    }

    /**
     * Get the project leader name of an inquiry
     *
     * @param project the project
     * @return the name of the project leader
     */
    public static String getProjectLeaderName(Project project) {
        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(project.getId());
        if (inquiry == null) {
            return "Unbekannt";
        }
        return getAuthorName(inquiry);
    }

    /**
     * Get the project leader username of an inquiry
     *
     * @param project the project
     * @return the username of the project leader
     */
    public static String getProjectLeaderUsername(Project project) {
        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(project.getId());
        if (inquiry == null) {
            return "Unbekannt";
        }
        return getAuthorUsername(inquiry);
    }

    /**
     * Get the site of the author of a project
     *
     * @param project the project
     * @return the name of the site of the project author
     */
    public static String getSite(Project project) {
        Site site = ProjectUtil.getSite(project);
        if (site != null) {
            return site.getName();
        } else {
            return "Unbekannt";
        }
    }

    /**
     * Count all projects with a given type
     *
     * @param projectType the type of project to count
     * @return the amount of projects with this type
     */
    public static Integer countProjects(EnumProjectType projectType) {
        return ProjectUtil.countProjects(projectType);
    }

    /**
     * Count all inquiries with a given status belonging to a user
     *
     * @param inquiryStatus the inquiry status to count
     * @param userId the id of the user the inquiry should belong to
     * @return the amount of inquiries, belonging to the user, with a given status
     */
    public static Integer countInquiries(InquiryStatus inquiryStatus, int userId) {
        return InquiryUtil.countInquiries(inquiryStatus, userId);
    }

    /**
     * Count released inquiries belonging to a user
     *
     * @param withProjects if true, only count the inquiries that belong to any project
     * @param userId the id of the user the inquiry should belong to
     * @return the amount of released inquiries, belonging to the user
     */
    public static Integer countReleasedInquiries(boolean withProjects, int userId) {
        return InquiryUtil.countReleasedInquiries(withProjects, userId);
    }

    /**
     * Get the user name for a user id
     *
     * @param userId the id of the user
     * @return the name of the user or "Unbekannt" if none found
     */
    public static String getUsernameById(int userId) {
        return UserUtil.getUsernameById(userId);
    }

    /**
     * Get the project belonging to an inquiry
     *
     * @param inquiry the inquiry
     * @return the project belonging to this inquiry, or null if no project belongs to the inquiry
     */
    public static Project getProjectForInquiry(Inquiry inquiry) {
        return ProjectUtil.fetchProjectByInquiry(inquiry);
    }

    /**
     * Get the first inquiry for a project
     *
     * @param project the project
     * @return the first inquiry for that project
     */
    public static Inquiry getFirstInquiryForProject(Project project) {
        return ProjectUtil.fetchFirstInquiryForProject(project.getId());
    }

    /**
     * Get all project partners for a project
     *
     * @param project the project
     * @return a list of all project partners
     */
    public static String getProjectPartners(Project project) {
        List<Site> partners = ProjectUtil.fetchProjectPartners(project);
        StringBuilder sb = new StringBuilder();
        for (Site site : partners) {
            sb.append(site.getName());
            sb.append(", ");
        }
        if (sb.length() > 2) {
            sb.delete(sb.length() -2 , sb.length());
        }
        return sb.toString();
    }

    /**
     * Get the title of a project
     *
     * @param projectId the id of the project
     * @return the title of the project
     */
    public static String getProjectTitle(int projectId) {
        return ProjectUtil.getProjectTitleById(projectId);
    }

    /**
     * Get the application number of a project
     *
     * @param projectId the id of the project
     * @return the application number as string, or "-" if none is found
     */
    public static String getProjectApplicationNumber(int projectId) {
        return ProjectUtil.getProjectApplicationNumber(projectId);
    }

    /**
     * Check if an inquiry is about to expire (in < 3 days)
     *
     * @param inquiry the inquiry to check
     * @return true if it expires in 3 days or less
     */
    public static boolean expiryImminent(Inquiry inquiry) {
        long timediff = inquiry.getExpires().getTime() - SamplyShareUtils.getCurrentSqlTimestamp().getTime();
        return (timediff < TimeUnit.DAYS.toMillis(3));
    }

    /**
     * Check if an inquiry is still active
     *
     * @param inquiry the inquiry to check
     * @return true if it is not expired yet
     */
    public static boolean isInquiryStillActive(Inquiry inquiry) {
        return (inquiry.getExpires().getTime() > SamplyShareUtils.getCurrentSqlTimestamp().getTime());
    }

    /**
     * Get the mdr key for the result type dataelement from the config
     *
     * @return the mdr key for the result type dataelement
     */
    public static String getResultTypeMdrKey() {
    	return Config.instance.getProperty(MDRKEY_RESULT_TYPE);
    }

    /**
     * Get the extended name of a site
     *
     * @param namespace the site namespace
     * @return the extended name if defined, the namespace itself otherwise
     */
    public static String getExtendedNamespace(String namespace) {
        Site site = SiteUtil.fetchSiteByNameIgnoreCase(namespace);
        if (site == null) {
            return namespace;
        }
        String extendedName = site.getNameExtended();
        if (extendedName == null || extendedName.length() < 1) {
            return namespace;
        } else {
            return extendedName;
        }
    }

    /**
     * Get the site id for a given namespace
     *
     * @param namespace the namespace of the site
     * @return the id of the site
     */
    public static String getNamespaceSiteId(String namespace) {
        Site site = SiteUtil.fetchSiteByNameIgnoreCase(namespace);
        if (site == null) {
            // This occurs for "standard" namespaces like "osse-cds" or "adt" which don't belong to a site.
            // return "default"
            return "default";
        }
        return String.valueOf(site.getId());
    }

    /**
     * Get the site for the currently logged in user
     *
     * @return the site
     */
    public static Site getSiteForLoggedUser() {
        try {
            User loggedUser = Utils.getLoginController().getUser();
            return UserUtil.getSiteForUser(loggedUser);
        } catch (NullPointerException ignored) {
        }
        return null;
    }

    /**
     * Get the combined string of user and site
     *
     * @return Username (Sitename)
     */
    public static String getUserStringWithSite() {
        try {
            User loggedUser = Utils.getLoginController().getUser();
            Site userSite = UserUtil.getSiteForUser(loggedUser);
            
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(loggedUser.getName());
            if (userSite != null) {
                stringBuilder.append(" (");
                stringBuilder.append(userSite.getName());
                stringBuilder.append(")");
            }
            return stringBuilder.toString();
        } catch (NullPointerException ignored) {
        }
        return "";
    }
}
