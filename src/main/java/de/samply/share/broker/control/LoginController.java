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
package de.samply.share.broker.control;

import de.samply.jsf.JsfUtils;
import de.samply.jsf.LoggedUser;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.tables.pojos.Contact;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.model.db.tables.pojos.UserSite;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.utils.oauth2.OAuthConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.context.OmniPartialViewContext;
import org.omnifaces.util.Faces;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.TimeZone;

/**
 * handles user access.
 */
public class LoginController implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5474963222029599835L;

    /** The Constant SESSION_USERNAME. */
    private static final String SESSION_USERNAME = "username";

    /** The Constant SESSION_ROLE. */
    private static final String SESSION_ROLES = "roles";

    /** The Constant CCP OFFICE. */
    private static final String ROLE_CCP_OFFICE = "ccp_office";

    /** The Constant RESEARCHER. */
    private static final String ROLE_RESEARCHER = "researcher";

    /** The Constant RESEARCHER. */
    private static final String ROLE_ADMIN = "admin";

    /** The Constant that represents the role for the CCP Office in Samply Auth. This will be changed in a future release */
    private static final String CCP_OFFICE_ROLEIDENTIFIER = "CCP Büro";
    private static final String ADMIN_ROLEIDENTIFIER = "DKTK_SEARCHBROKER_ADMIN";

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(LoginController.class);

    /** The user. */
    private User user = new User();

    /** The contact. */
    private Contact contact = new Contact();
    
    /** The user-site relation */
    private UserSite userSite = null;
    private int newSiteId;

    private boolean userConsent = false;

    private String currentPass;
    private String newPass;
    private String newPassRepeat;

    private String code = null;

    private String error = null;

    /**
     * Gets the user.
     *
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the user.
     *
     * @param user
     *            the new user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the userSite
     */
    public UserSite getUserSite() {
        return userSite;
    }

    /**
     * @param userSite the userSite to set
     */
    public void setUserSite(UserSite userSite) {
        this.userSite = userSite;
    }

    public void setUserConsent(boolean userConsent) {
        this.userConsent = userConsent;
    }

    public String getCode() {
        return code;
    }

    /**
     * Log a user from auth in
     *
     * @return the navigation case
     */
    public String login(User user) {
        OmniPartialViewContext context = OmniPartialViewContext.getCurrentInstance();
        boolean loggedIn = true;

        TimeZone.setDefault(TimeZone.getTimeZone("CET"));

        setUser(user);

        setUserConsent(UserConsentUtil.hasUserGivenLatestConsent(user));
        
        reloadUserSite();

        try (Connection connection = ResourceManager.getConnection()) {
            contact = ContactUtil.getContactForUser(user);
            if (contact == null) {
                contact = ContactUtil.createContactForUser(user);
            }

            logger.info("Login successful");
            loggedIn = true;

            // set the session
            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            request.getSession().setAttribute(SESSION_USERNAME, user.getUsername());

            //request.getSession().setAttribute(JsfUtils.SESSION_USER, user);
            request.getSession().setAttribute(JsfUtils.SESSION_USER, userToLoggedUser(user));
            context.addArgument("loggedIn", true);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (loggedIn) {
            return "success";
        } else {
            return "failed";
        }

    }

    /**
     * Transform a User object to a LoggedUser object
     *
     * @param user the user to transform
     * @return the logged user object that will be stored in the session
     */
    private static LoggedUser userToLoggedUser(User user) {
        LoggedUser loggedUser = new LoggedUser();
        loggedUser.setAuthId(user.getAuthid());
        loggedUser.setId(user.getId());
        loggedUser.setName(user.getName());
        loggedUser.setEmail(user.getEmail());
        return loggedUser;
    }

    /**
     * Logout and invalidate session
     */
    public void logout() throws UnsupportedEncodingException {
    	// Delete old unbound exposes...
    	DocumentUtil.deleteOldUnboundDocuments();

    	Faces.invalidateSession();
        user = null;
        String authLogoutUrl = OAuthConfig.getAuthLogoutUrl();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(authLogoutUrl);
        } catch (IOException e) {
            logger.debug("Could not call the authentication server logout URL!");
        }
    }

    /**
     * Get the user name of the current user from the session.
     *
     * @return the user name of the logged in user
     */
    public String getLoggedUsername() {
        // check the session
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getSession().getAttribute(SESSION_USERNAME).toString();
    }

    /**
     * Update consent for a given user
     */
    private void updateConsent() {
        UserConsentUtil.updateConsent(user, userConsent);
    }

    /**
     * Update the site associated with the current user
     */
    private void updateSite() {
        if (newSiteId > 0) {
            Site newSite = SiteUtil.fetchSiteById(newSiteId);
            UserSite newUserSite = UserSiteUtil.setSiteForUser(user, newSite, false);
            setUserSite(newUserSite);
        }
    }

    /**
     * Reload the site for the currently logged in user
     */
    public void reloadUserSite() {
        try {
            UserSite currentUserSite = UserSiteUtil.fetchUserSiteByUser(user);
            setUserSite(currentUserSite);
            newSiteId = getUserSite().getSiteId();
        } catch (NullPointerException npe) {
            newSiteId = -1;
        }
    }
}
