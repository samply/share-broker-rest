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
package de.samply.share.broker.filter;

import com.google.common.base.Splitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.filter.HttpFilter;
import org.omnifaces.util.Servlets;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/** Access Control Filter.
 *
 *  If a user is trying to go to an area he is not supposed to, redirect him to his start page
 */
@WebFilter("/ccp_office/*")
public class AccessControlFilterCcpOffice extends HttpFilter {

    private static final Logger logger = LogManager.getLogger(AccessControlFilterCcpOffice.class);

    /** The Constant SESSION_ROLE. */
    private static final String SESSION_ROLES = "roles";

    /** The Constant CCP OFFICE. */
    private static final String ROLE_CCP_OFFICE = "ccp_office";

    private static final String SESSION_USERNAME = "username";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, HttpSession session, FilterChain chain) throws ServletException, IOException {
        String loginURL = request.getContextPath() + "/loginRedirect.xhtml";
        String dashboardURL = request.getContextPath() + "/dashboard.xhtml";
        boolean loggedIn = (session != null) && (session.getAttribute(SESSION_USERNAME) != null);
        boolean loginRequest = request.getRequestURI().equals(loginURL);
        boolean resourceRequest = Servlets.isFacesResourceRequest(request);
        
        String roleConcat = (String) request.getSession().getAttribute(SESSION_ROLES);
        List<String> roles = Splitter.on(',').trimResults().omitEmptyStrings().splitToList(roleConcat);
        
        if (loggedIn || loginRequest || resourceRequest) {
            if (!resourceRequest) { // Prevent browser from caching restricted resources. See also http://stackoverflow.com/q/4194207/157882
                Servlets.setNoCacheHeaders(response);
            }
            if (roles != null && roles.contains(ROLE_CCP_OFFICE)) {
                chain.doFilter(request, response); // So, just continue request.
            } else {
                String username = (String) request.getSession().getAttribute(SESSION_USERNAME);
                logger.warn("User " + username + " tried to access ccp office area. Access was denied.");
                Servlets.facesRedirect(request, response, dashboardURL);
            }
            
        }
        else {
            Servlets.facesRedirect(request, response, loginURL);
        }
    }


}