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

import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.db.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.servlet.http.Part;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  holds all details linked with an inquiry
 */
public class SearchDetailsBean implements Serializable {

    private static final long serialVersionUID = -4671575946697123638L;
    private static final Logger logger = LogManager.getLogger(SearchDetailsBean.class);

    private static final String createEventhandlers = "createEventhandlers();";
    private static final String resetExposeFileinput = "$('#exposeUpload').fileinput('reset');";
    private static final String hideExposeFileinput = "$('#exposeBoxForm').hide();";
    private static final String showExposeFileinput = "$('#exposeBoxForm').show();";
    
    private static final String resetVoteFileinput = "$('#voteUpload').fileinput('reset');";
    private static final String hideVoteFileinput = "$('#voteBoxForm').hide();";
    private static final String showVoteFileinput = "$('#voteBoxForm').show();";

    private String inquiryName;
    private String inquiryDescription;
    private List<String> selectedSites;
    private List<String> resultTypes;
    private Inquiry inquiry;
    private Document expose;
    private Part newExpose;
    private Document vote;
    private Part newVote;
    private boolean cooperationAvailable;
    private boolean edit = false;
    private boolean validationActive = false;

    private String serializedQuery;

    private LoginController loginController;

    @PostConstruct
    public void init() {
        selectedSites = new ArrayList<>();
        resultTypes = new ArrayList<>();
    }

    public String getInquiryName() {
        return inquiryName;
    }

    public String getInquiryDescription() {
        return inquiryDescription;
    }

    public List<String> getSelectedSites() {
        return selectedSites;
    }

    public List<String> getResultTypes() {
        return resultTypes;
    }

    public Inquiry getInquiry() {
        return inquiry;
    }

    public Document getExpose() {
        return expose;
    }

    public Document getVote() {
        return vote;
    }

    public void setCooperationAvailable(boolean cooperationAvailable) {
        this.cooperationAvailable = cooperationAvailable;
    }

    public boolean isEdit() {
        return edit;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }

    public void setSerializedQuery(String serializedQuery) {
        this.serializedQuery = serializedQuery;
    }

    public List<Site> getSites() {
        return SiteUtil.fetchSites();
    }

    /**
     * Load the expose for the inquiry that is being created
     */
    private void loadExpose() {
        if (inquiry != null) {
            expose = DocumentUtil.fetchExposeByInquiry(inquiry);
        }
    }

    /**
     * Load the vote for the inquiry that is being created
     */
    private void loadVote() {
        if (inquiry != null) {
            vote = DocumentUtil.fetchVoteByInquiry(inquiry);
            setCooperationAvailable( (vote != null) && (vote.getId() >=0) );
        }
    }

    /**
     * Clear all variables
     */
    public void clearStuff() {
        inquiryDescription = "";
        inquiryName = "";
        selectedSites = new ArrayList<>();
        resultTypes = new ArrayList<>();
        inquiry = null;
        serializedQuery = "";
        expose = null;
        vote = null;
        cooperationAvailable = false;
        edit = false;
    }

    /**
     * Load the inquiry with the given id from the database
     *
     * @param inquiryId the id of the inquiry to be loaded
     */
    public void loadFromDb(int inquiryId) {
        clearStuff();
        edit = true;
        inquiry = InquiryUtil.fetchInquiryById(inquiryId);
        if (inquiry == null) {
            logger.fatal("Error while trying to load inquiry from db. id=" + inquiryId);
            return;
        }
        
        if (inquiry.getRevision() == 0) {
            logger.debug("Inbound from central search");
            resultTypes = new ArrayList<>();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, Messages.getString("csTransferHeader"), Messages.getString("csTransferMessage"));
            org.omnifaces.util.Messages.addFlashGlobal(msg);
        } else {
            loadExpose();
            loadVote();
            List<Site> sites = SiteUtil.fetchSitesForInquiry(inquiry.getId());

            inquiryDescription = inquiry.getDescription();
            inquiryName = inquiry.getLabel();
            for (Site site : sites) {
                selectedSites.add(String.valueOf(site.getId()));
            }
            
            try {
                List<String> helperList = Arrays.asList(inquiry.getResultType().split(","));
                resultTypes.addAll(helperList);
            } catch (Exception e) {
                logger.debug("Could not split result type. Maybe none was set. This is perfectly normal when transferring from central search. Creating empty list");
                resultTypes = new ArrayList<>();
            }
            
        }
        
        serializedQuery = inquiry.getCriteria();
        logger.debug("Loaded inquiry with id " + inquiryId + " from db.");
    }

    /**
     * Load an inquiry and go to the inquiry details page
     *
     * @param inquiryId the id of the inquiry to load
     * @return navigation to the first page of the inquiry creation process
     */
    public String editQueryGotoStep1(String inquiryId) {
        loadFromDb(Integer.parseInt(inquiryId));
        return "search_details?edit=true&faces-redirect=true";
    }

    /**
     * Check if an expose is needed to create the inquiry
     *
     * @return true, if the user sends the inquiry to his own site only
     */
    private boolean needExpose() {
        User loggedUser = loginController.getUser();
        if (loggedUser == null) {
            return true;
        }
        Integer loggedUserSiteId = UserUtil.getSiteIdForUser(loggedUser);
        UserSite userSite = UserSiteUtil.fetchUserSiteByUser(loggedUser);
        if (loggedUserSiteId == null || userSite == null || !userSite.getApproved()) {
            return true;
        } else {
            return !(selectedSites != null && selectedSites.size() == 1 && selectedSites.contains(Integer.toString(loggedUserSiteId)));
        }
    }

}