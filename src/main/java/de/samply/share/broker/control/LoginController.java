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

import com.google.common.base.Joiner;
import de.samply.auth.client.jwt.JWTAccessToken;
import de.samply.auth.client.jwt.JWTException;
import de.samply.auth.client.jwt.JWTIDToken;
import de.samply.auth.rest.*;
import de.samply.jsf.JsfUtils;
import de.samply.jsf.LoggedUser;
import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.oauth2.OAuthConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.context.OmniPartialViewContext;
import org.omnifaces.util.Faces;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
     * Gets the contact.
     *
     * @return the contact
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * Sets the contact.
     *
     * @param contact
     *            the new contact
     */
    public void setContact(Contact contact) {
        this.contact = contact;
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

    /**
     * @return the newSite
     */
    public int getNewSiteId() {
        return newSiteId;
    }

    /**
     * @param newSiteId the newSiteId to set
     */
    public void setNewSiteId(int newSiteId) {
        this.newSiteId = newSiteId;
    }

    public boolean isUserConsent() {
        return userConsent;
    }

    public void setUserConsent(boolean userConsent) {
        this.userConsent = userConsent;
    }

    public String getCurrentPass() {
        return currentPass;
    }

    public void setCurrentPass(String currentPass) {
        this.currentPass = currentPass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public String getNewPassRepeat() {
        return newPassRepeat;
    }

    public void setNewPassRepeat(String newPassRepeat) {
        this.newPassRepeat = newPassRepeat;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Consent getLatestConsent() {
        return UserConsentUtil.getLatestConsent();
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
     * Handle the login response from the authentication service
     *
     * @return the navigation case
     */
    public String onLoginResponse() {
        logger.debug("Processing authentication server's response...");
        String projectName = ProjectInfo.INSTANCE.getProjectName();
        List<String> roles = new ArrayList<>();
        String rolesConcat;
        boolean isCcpOffice = false;
        
        if (error != null) {
            return "/errors/error.xhtml?faces-redirect=true&error=" + error;
        }

        AccessTokenRequestDTO accessTokenRequest = new AccessTokenRequestDTO();
        accessTokenRequest.setClientId(OAuthConfig.getOAuth2Client(projectName).getClientId());
        accessTokenRequest.setClientSecret(OAuthConfig.getOAuth2Client(projectName).getClientSecret());
        accessTokenRequest.setCode(getCode());

        JWTAccessToken jwtAccesstoken;
        JWTIDToken jwtIdToken;
        try {
            Client client = ClientBuilder.newClient();
            Invocation.Builder invocationBuilder = client.target(OAuthConfig.getOAuth2Client(projectName).getHost() + "/oauth2/access_token").request("application/json").
                    accept(MediaType.APPLICATION_JSON);

            Response response = invocationBuilder.post(Entity.entity(accessTokenRequest, MediaType.APPLICATION_JSON_TYPE));

            AccessTokenDTO accessToken = response.readEntity(AccessTokenDTO.class);
            jwtAccesstoken = new JWTAccessToken(OAuthConfig.getOAuth2Client(projectName), accessToken.getAccessToken());
            jwtIdToken = new JWTIDToken(OAuthConfig.getOAuth2Client(projectName), accessToken.getIdToken());

            if (jwtAccesstoken.isValid() && jwtIdToken.isValid()) {
                User loggedUser = UserUtil.fetchUserByAuthId(jwtIdToken.getSubject());
                if (loggedUser == null) {
                    logger.debug("user not known...creating");
                    loggedUser = UserUtil.createOrUpdateUser(jwtIdToken, accessToken);
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, Messages.getString("welcome"), Messages.getString("newUserCreated"));
                    org.omnifaces.util.Messages.addFlashGlobal(msg);
                }
                UserSiteUtil.createUserSiteAssociation(user);

                for (RoleDTO roleDTO : jwtIdToken.getRoles()) {
                    String role = roleDTO.getIdentifier();
                    if (role.equalsIgnoreCase(CCP_OFFICE_ROLEIDENTIFIER)) {
                        logger.info("CCP Office rights granted");
                        isCcpOffice = true;
                        roles.add(ROLE_CCP_OFFICE);
                    }
                    if (role.equalsIgnoreCase(ADMIN_ROLEIDENTIFIER)) {
                        logger.info("Admin rights granted");
                        roles.add(ROLE_ADMIN);
                    }
                }
                
                if (roles.isEmpty()) {
                    rolesConcat = ROLE_RESEARCHER;
                } else {
                    rolesConcat = Joiner.on(',').join(roles);
                }
                
                logger.info("user: " + loggedUser.getUsername() + " ( " + loggedUser.getAuthid() + " ) signed in");
                login(loggedUser);
                // set the session
                HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
                request.getSession().setAttribute(SESSION_USERNAME, loggedUser.getAuthid());

                StringBuilder stringBuilder = new StringBuilder();

                if (requestParameterMap != null) {
                    String requestedPage = requestParameterMap.get("requestedPage");
                    if (requestedPage != null && !requestedPage.contains("loginRedirect")) {
                        stringBuilder.append(requestedPage);
                        stringBuilder.append(".xhtml");
                        
                        // Check if redirect contains inquiry reference
                        String inquiryId = requestParameterMap.get("inquiryId");
                        if (inquiryId != null && inquiryId.length() > 0) {
                            stringBuilder.append("?inquiryId=");
                            stringBuilder.append(inquiryId);
                            stringBuilder.append("&faces-redirect=true");
                        } else {
                            stringBuilder.append("?faces-redirect=true");
                        }
                        
                        // Check if redirect contains project reference
                        if (isCcpOffice) {
                            String projectId = requestParameterMap.get("projectId");
                            if (projectId != null && projectId.length() > 0) {
                                stringBuilder.append("?projectId=");
                                stringBuilder.append(projectId);
                                stringBuilder.append("&faces-redirect=true");
                            } else {
                                stringBuilder.append("?faces-redirect=true");
                            }
                        }
                    }
                }

                if (projectName.equalsIgnoreCase("osse")) {
                    LocationListDTO locationList = Utils.getLocationList(accessToken);
                    SiteUtil.insertSites(locationList);

                    List<LocationDTO> userLocations = jwtIdToken.getLocations();
                    UserSiteUtil.updateUserLocations(user, userLocations);
                }
                
                request.getSession().setAttribute(SESSION_ROLES, rolesConcat);
                
                if (projectName.equalsIgnoreCase("dktk") && isCcpOffice) {
                    if (stringBuilder.length() > 0) {
                        return stringBuilder.toString();
                    }
                    return "ccpoffice_success";
                } else {
                    if (stringBuilder.length() > 0) {
                        return stringBuilder.toString();
                    }
                    return "success";
                }
            } else {
                logger.debug("token is invalid");
                return "/errors/error.xhtml?faces-redirect=true&error=invalidToken";
            }
        } catch (JWTException e) {
            logger.debug("Error on the processing of the access and id tokens: " + e);
        }
        return "failed";
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
     * Update contact.
     */
    public void updateContact() {
        FacesMessage facesMessage;
        String returnValue = ContactUtil.updateContact(contact);
        if (returnValue.equalsIgnoreCase("success")) {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Ok", Messages.getString("contactUpdated"));
        } else {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.getString("error"), Messages.getString("contactUpdateFailed"));
        }

        updateConsent();
        if (userSite == null || !userSite.getApproved()) {
            updateSite();
        }

        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
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
     * Get the list of all sites that are in the database
     *
     * @return list of all sites
     */
    public List<Site> getSites() {
        return SiteUtil.fetchSites();
    }

    /**
     * Get the (extended) name of the site associated with the current user
     *
     * @return the extended name of the site, or the short name of the site, or "-" if none is available
     */
    public String getUserSiteName() {
        Site siteForUser = UserUtil.getSiteForUser(user);
        if (siteForUser != null) {
            String nameExtended = siteForUser.getNameExtended();
            if (nameExtended != null && nameExtended.length() > 0) {
                return nameExtended;
            } else {
                return siteForUser.getName();
            }
        } else {
            return "-";
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
