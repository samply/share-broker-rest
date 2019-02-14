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
package de.samply.share.broker.listener;

import de.samply.share.common.utils.SamplyShareUtils;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

/**
 * The listener interface for receiving authorization events.
 * The class that is interested in processing a authorization
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addAuthorizationListener</code> method. When
 * the authorization event occurs, that object's appropriate
 * method is invoked.
 */
public class AuthorizationListener implements PhaseListener {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void afterPhase(PhaseEvent event) {

        FacesContext facesContext = event.getFacesContext();
        String currentPage = facesContext.getViewRoot().getViewId();

        boolean isLoginPage = (currentPage.lastIndexOf("loginRedirect.xhtml") > -1 || currentPage.lastIndexOf("samplyLogin.xhtml") > -1);
        HttpSession session = (HttpSession) facesContext.getExternalContext()
                .getSession(false);

        if (session == null) {

            NavigationHandler nh = facesContext.getApplication()
                    .getNavigationHandler();
            nh.handleNavigation(facesContext, null, "loginRedirect");
        }

        else {
            Object currentUser = session.getAttribute("username");

            if (!isLoginPage && (SamplyShareUtils.isNullOrEmpty((String)currentUser)) ) {
                NavigationHandler nh = facesContext.getApplication()
                        .getNavigationHandler();
                nh.handleNavigation(facesContext, null, "loginRedirect");
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
     */
    @Override
    public void beforePhase(PhaseEvent event) {

    }

    /* (non-Javadoc)
     * @see javax.faces.event.PhaseListener#getPhaseId()
     */
    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}