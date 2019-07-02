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
import de.samply.share.broker.filter.AuthenticatedUser;
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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
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
 * A JSF Managed Bean that is existent for a whole session. It handles user access.
 */
@ManagedBean(name = "loginController")
@SessionScoped
public class LoginController implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -5474963222029599835L;

    /**
     * The user.
     */
    @Inject
    @AuthenticatedUser
    User authenticatedUser;
    private User user = authenticatedUser;

    /**
     * The contact.
     */
    private Contact contact = new Contact();

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
     * @param user the new user
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
     * @param contact the new contact
     */
    public void setContact(Contact contact) {
        this.contact = contact;
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

    /**
     * Get the list of all sites that are in the database
     *
     * @return list of all sites
     */
    public List<Site> getSites() {
        return SiteUtil.fetchSites();
    }

}
