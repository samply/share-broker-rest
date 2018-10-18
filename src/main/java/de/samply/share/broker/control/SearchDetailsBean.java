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
import de.samply.share.broker.model.db.enums.DocumentType;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.utils.ProjectInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    public void setInquiryName(String inquiryName) {
        this.inquiryName = inquiryName;
    }

    public String getInquiryDescription() {
        return inquiryDescription;
    }

    public void setInquiryDescription(String inquiryDescription) {
        this.inquiryDescription = inquiryDescription;
    }

    public List<String> getSelectedSites() {
        return selectedSites;
    }

    public void setSelectedSites(List<String> selectedSites) {
        this.selectedSites = selectedSites;
    }

    public List<String> getResultTypes() {
        return resultTypes;
    }

    public void setResultTypes(List<String> resultTypes) {
        this.resultTypes = resultTypes;
    }

    public Inquiry getInquiry() {
        return inquiry;
    }

    public void setInquiry(Inquiry inquiry) {
        this.inquiry = inquiry;
    }

    public Document getExpose() {
        return expose;
    }

    public void setExpose(Document expose) {
        this.expose = expose;
    }

    public Part getNewExpose() {
        return newExpose;
    }

    public void setNewExpose(Part newExpose) {
        this.newExpose = newExpose;
    }

    public Document getVote() {
        return vote;
    }

    public void setVote(Document vote) {
        this.vote = vote;
    }

    public Part getNewVote() {
        return newVote;
    }

    public void setNewVote(Part newVote) {
        this.newVote = newVote;
    }

    public boolean isCooperationAvailable() {
        return cooperationAvailable;
    }

    public void setCooperationAvailable(boolean cooperationAvailable) {
        this.cooperationAvailable = cooperationAvailable;
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean isValidationActive() {
        return validationActive;
    }

    public void setValidationActive(boolean validationActive) {
        this.validationActive = validationActive;
    }

    public String getSerializedQuery() {
        return serializedQuery;
    }

    public void setSerializedQuery(String serializedQuery) {
        this.serializedQuery = serializedQuery;
    }

    public LoginController getLoginController() {
        return loginController;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
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
     * Load the expose with the given expose_id
     *
     * The id is transmitted via http request parameter
     */
    public void loadExposeWithId() {
        String id_string = Faces.getRequestParameter("expose_id");
        try {
            int id = Integer.parseInt(id_string);
            expose = DocumentUtil.getDocumentById(id);
            Ajax.oncomplete(createEventhandlers, hideExposeFileinput);
        } catch (NumberFormatException e) {
            logger.debug("Couldn't parse expose id" + id_string);
        }
    }

    /**
     * Load the vote with the given expose_id
     *
     * The id is transmitted via http request parameter
     */
    public void loadVoteWithId() {
        String id_string = Faces.getRequestParameter("vote_id");
        try {
            int id = Integer.parseInt(id_string);
            vote = DocumentUtil.getDocumentById(id);
            Ajax.oncomplete(createEventhandlers, hideVoteFileinput);
        } catch (NumberFormatException e) {
            logger.debug("Couldn't parse vote id" + id_string);
        }
    }

    /**
     * Export the expose of the current inquiry
     */
    public void exportExpose() throws IOException {
        if (expose != null && expose.getId() > 0) {
            ByteArrayOutputStream bos =  DocumentUtil.getDocumentOutputStreamById(expose.getId());
            Faces.sendFile(bos.toByteArray(), "Exposee_" + inquiry + ".pdf", true);
        } else {
            logger.warn("Tried to export expose but couldn't find it");
        }
    }

    /**
     * Export the vote of the current inquiry
     */
    public void exportVote() throws IOException {
        if (vote != null && vote.getId() > 0) {
            ByteArrayOutputStream bos =  DocumentUtil.getDocumentOutputStreamById(vote.getId());
            Faces.sendFile(bos.toByteArray(), "vote_" + inquiry + ".pdf", true);
        } else {
            logger.warn("Tried to export vote but couldn't find it");
        }
    }

    /**
     * Delete a document with the given elementId
     *
     * The id is transmitted via http request parameter
     */
    public void deleteDocument() {
        String documentIdString = Faces.getRequestParameter("elementId");
        try {
            int documentId = Integer.parseInt(documentIdString);
            DocumentType documentType = DocumentUtil.deleteDocument(documentId);
            if (documentType == DocumentType.DT_EXPOSE) {
                expose = null;
                Ajax.oncomplete(resetExposeFileinput, createEventhandlers, showExposeFileinput);
            } else if (documentType == DocumentType.DT_VOTE) {
                vote = null;
                Ajax.oncomplete(resetVoteFileinput, createEventhandlers, showVoteFileinput);
            } else {
                logger.debug("unknown document type");
            }
            logger.debug("Deleted document with id: " + documentId + " (type: " + documentType + ")");
        } catch (NumberFormatException e) {
            logger.warn("Could not parse expose id: " + documentIdString);
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
     * Clear all variables and go to the create inquiry details page
     *
     * This is the standard action when the new inquiry link is clicked
     *
     * @return navigation to the first page of the inquiry creation process
     */
    public String clearSerializedQueryAndGotoStep1() {
        clearStuff();
        // Pre-fill all the sites
        for (Site site : getSites()) {
            selectedSites.add(Integer.toString(site.getId()));
        }
        return "search_details?faces-redirect=true";
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

    /**
     * Check if all necessary details on the inquiry details page are filled and navigate to the query builder
     *
     * @return navigation to query builder if all fields are filled
     */
    public String goToQueryBuilder() {
        boolean showError = false;
        boolean isDktk = ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk");
        
        if (getExpose() == null && needExpose()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.getString("noExpose"), Messages.getString("noExposeDetail"));
            org.omnifaces.util.Messages.addGlobal(message);
            showError = true;
        }
        if (isDktk && (getVote() == null && cooperationAvailable)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.getString("noVote"), Messages.getString("noVoteDetail"));
            org.omnifaces.util.Messages.addGlobal(message);
            showError = true;
        }
        if (isDktk && (resultTypes == null || resultTypes.size() < 1)) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.getString("resultTypeRequired"), Messages.getString("resultTypeRequiredDetail"));
            org.omnifaces.util.Messages.addGlobal(message);
            showError = true;
        }
        if (selectedSites == null || selectedSites.size() < 1) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.getString("siteRequired"), Messages.getString("siteRequired"));
            org.omnifaces.util.Messages.addGlobal(message);
            showError = true;
        }

        if (showError) {
            return "";            
        } else {
            return "search_query?faces-redirect=true";
        }
    }

}