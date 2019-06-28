/**
 * Copyright (C) 2015 Working Group on Joint Research, University Medical Center Mainz
 * Contact: info@osse-register.de
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 * <p>
 * Additional permission under GNU GPL version 3 section 7:
 * <p>
 * If you modify this Program, or any covered work, by linking or combining it
 * with Jersey (https://jersey.java.net) (or a modified version of that
 * library), containing parts covered by the terms of the General Public
 * License, version 2.0, the licensors of this Program grant you additional
 * permission to convey the resulting work.
 */
package de.samply.share.broker.control;

import com.google.common.base.Joiner;
import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.enums.InquiryStatus;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.rest.InquiryHandler;
import de.samply.share.broker.utils.Config;
import de.samply.share.broker.utils.MailUtils;
import de.samply.share.broker.utils.SimpleQueryDto2ShareXmlTransformer;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.control.uiquerybuilder.AbstractSearchController;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.QueryTreeUtil;
import de.samply.share.common.utils.SamplyShareUtils;
import de.samply.share.model.common.Query;
import de.samply.share.query.entity.*;
import de.samply.share.query.field.*;
import de.samply.share.query.value.*;
import de.samply.share.utils.QueryConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

import javax.faces.application.FacesMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

/**
 * holds methods and information necessary to create and display queries
 */

public class SearchController extends AbstractSearchController {


    private static final long serialVersionUID = 3998468782378633859L;

    private static final String CCP_OFFICE_MAIL = "mail.receiver.ccpoffice";


    private SearchDetailsBean searchDetailsBean;

    private static final Logger logger = LogManager.getLogger(SearchController.class);

    /**
     * Depending on the active page, either store or edit an inquiry
     *
     * @return the resulting navigation case
     */
    @Override
    public String onSubmit() {
        if (searchDetailsBean.isEdit()) {
            return editInquiry();
        } else {
            return storeInquiry();
        }
    }

    /**
     * Store and release an inquiry
     *
     * Release may be held back if confirmation by project management is needed
     *
     * @return the resulting navigation case
     */
    @Override
    public String onStoreAndRelease() {
        if (searchDetailsBean.isEdit()) {
            return editAndReleaseInquiry();
        } else {
            return storeAndReleaseInquiry();
        }
    }

    /**
     * Store the inquiry as draft
     *
     * @return navigation case to drafts list
     */
    private String storeInquiry() {
        logger.info("Storing inquiry...");
        Query query = QueryTreeUtil.treeToQuery(queryTree);

        InquiryHandler inquiryHandler = new InquiryHandler();
        int exposeId;
        try {
            exposeId = searchDetailsBean.getExpose().getId();
        } catch (Exception e) {
            logger.debug("No expose found");
            exposeId = -1;
        }

        int voteId;
        try {
            voteId = searchDetailsBean.getVote().getId();
        } catch (Exception e) {
            logger.debug("No expose found");
            voteId = -1;
        }

        //TODO replace userid=1
        int inquiryId = inquiryHandler.store(query, 1, searchDetailsBean.getInquiryName(), searchDetailsBean.getInquiryDescription(), exposeId, voteId, searchDetailsBean.getResultTypes());
        inquiryHandler.setSitesForInquiry(inquiryId, searchDetailsBean.getSelectedSites());

        logger.debug("Inquiry stored.");
        searchDetailsBean.clearStuff();

        return "drafts?faces-redirect=true";
    }

    /**
     * Store and release an inquiry
     *
     * @return to submitted page
     */
    private String storeAndReleaseInquiry() {
        logger.info("Storing and releasing inquiry...");
        Query query = QueryTreeUtil.treeToQuery(queryTree);

        InquiryHandler inquiryHandler = new InquiryHandler();
        //TODO replace new User()
        User loggedUser = new User();
        Integer loggedUserSiteId = UserUtil.getSiteIdForUser(loggedUser);
        UserSite userSite = UserSiteUtil.fetchUserSiteByUser(loggedUser);

        boolean bypassExamination = userSite != null && userSite.getApproved() && Utils.onlySelf(searchDetailsBean.getSelectedSites(), loggedUserSiteId);

        int exposeId;
        try {
            exposeId = searchDetailsBean.getExpose().getId();
        } catch (Exception e) {
            logger.debug("No expose found");
            exposeId = -1;
        }

        int voteId;
        try {
            voteId = searchDetailsBean.getVote().getId();
        } catch (Exception e) {
            logger.debug("No vote found");
            voteId = -1;
        }
//TODO replace userid=1
        int inquiryId = inquiryHandler.storeAndRelease(query, 1, searchDetailsBean.getInquiryName(), searchDetailsBean.getInquiryDescription(), exposeId, voteId, searchDetailsBean.getResultTypes(), bypassExamination);
        inquiryHandler.setSitesForInquiry(inquiryId, searchDetailsBean.getSelectedSites());

        logger.debug("Inquiry stored and released.");
        searchDetailsBean.clearStuff();

        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk") && !bypassExamination) {
            logger.debug("Sending mail to ccp office");
            MailUtils.sendProjectProposalMail(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
        }

        return "submitted?faces-redirect=true";
    }

    /**
     * Store the changes on an edited inquiry
     *
     * @return navigation to drafts page
     */
    private String editInquiry() {
        logger.info("Editing inquiry...");
        Query query = QueryTreeUtil.treeToQuery(queryTree);

        int exposeId;
        try {
            exposeId = searchDetailsBean.getExpose().getId();
        } catch (Exception e) {
            logger.debug("No expose found");
            exposeId = -1;
        }

        int voteId;
        try {
            voteId = searchDetailsBean.getVote().getId();
        } catch (Exception e) {
            logger.debug("No vote found");
            voteId = -1;
        }

        searchDetailsBean.setCooperationAvailable(voteId >= 0);

        Inquiry inquiry = searchDetailsBean.getInquiry();
        if (inquiry == null) {
            logger.fatal("error while trying to edit inquiry");
            return "drafts?faces-redirect=true";
        }

        inquiry.setLabel(searchDetailsBean.getInquiryName());
        inquiry.setDescription(searchDetailsBean.getInquiryDescription());
        inquiry.setCreated(SamplyShareUtils.getCurrentSqlTimestamp());
        DocumentUtil.setInquiryIdForDocument(exposeId, inquiry.getId());

        try {
            inquiry.setCriteria(QueryConverter.queryToXml(query));
        } catch (JAXBException e) {
            logger.error("Error while trying to convert the query to xml...");
        }

        String joinedResultTypes = "";
        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")) {
            Joiner joiner = Joiner.on(",");
            joinedResultTypes = joiner.join(searchDetailsBean.getResultTypes());
        }
        inquiry.setResultType(joinedResultTypes);

        // If this inquiry came from central search, and the user already made some changes, consider it as revision 1
        if (inquiry.getRevision() == 0) {
            inquiry.setRevision(1);
        }

        InquiryUtil.updateInquiry(inquiry);

        InquiryHandler inquiryHandler = new InquiryHandler();
        inquiryHandler.setSitesForInquiry(inquiry.getId(), searchDetailsBean.getSelectedSites());

        logger.debug("Inquiry edited.");
        searchDetailsBean.clearStuff();
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", Messages.getString("inquiryEdited"));
        org.omnifaces.util.Messages.addFlashGlobal(msg);
        return "drafts?faces-redirect=true";
    }

    /**
     * Store the changes on an edited inquiry and release it
     *
     * @return redirection to dashboard
     */
    private String editAndReleaseInquiry() {
        logger.info("Editing and releasing inquiry...");
        Query query = QueryTreeUtil.treeToQuery(queryTree);

        if (!QueryTreeUtil.isQueryValid(queryTree)) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, Messages.getString("query_empty"), Messages.getString("query_empty"));
            org.omnifaces.util.Messages.addGlobal(msg);
            return "";
        }

        //TODO replace new User()
        User loggedUser = new User();
        Integer loggedUserSiteId = UserUtil.getSiteIdForUser(loggedUser);
        UserSite userSite = UserSiteUtil.fetchUserSiteByUser(loggedUser);

        boolean bypassExamination = userSite != null && userSite.getApproved() && Utils.onlySelf(searchDetailsBean.getSelectedSites(), loggedUserSiteId);

        int exposeId;
        try {
            exposeId = searchDetailsBean.getExpose().getId();
        } catch (Exception e) {
            logger.debug("No expose found");
            exposeId = -1;
        }

        Inquiry inquiry = searchDetailsBean.getInquiry();
        if (inquiry == null) {
            logger.fatal("error while trying to edit inquiry");
            return "drafts?faces-redirect=true";
        }

        inquiry.setLabel(searchDetailsBean.getInquiryName());
        inquiry.setDescription(searchDetailsBean.getInquiryDescription());
        java.sql.Date expiryDate = new java.sql.Date(SamplyShareUtils.getCurrentDate().getTime() + InquiryUtil.INQUIRY_TTL);
        inquiry.setExpires(expiryDate);
        inquiry.setCreated(SamplyShareUtils.getCurrentSqlTimestamp());
        inquiry.setStatus(InquiryStatus.IS_RELEASED);
        DocumentUtil.setInquiryIdForDocument(exposeId, inquiry.getId());

        try {
            inquiry.setCriteria(QueryConverter.queryToXml(query));
        } catch (JAXBException e) {
            logger.error("Error while trying to convert the query to xml...");
        }

        String joinedResultTypes = "";
        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk")) {
            Joiner joiner = Joiner.on(",");
            joinedResultTypes = joiner.join(searchDetailsBean.getResultTypes());
        }
        inquiry.setResultType(joinedResultTypes);

        // Increment the revision if a project is linked to it
        Integer projectId = inquiry.getProjectId();
        boolean hadProjectBefore = false;

        if ((projectId != null && projectId > 0) || inquiry.getRevision() == 0) {
            inquiry.setRevision(inquiry.getRevision() + 1);
        }
        InquiryUtil.updateInquiry(inquiry);
        if (projectId != null && projectId > 0) {
            //TODO replace new User()
            ActionUtil.addInquiryEditActionToProject(new User(), projectId);
            hadProjectBefore = true;
        }

        InquiryHandler inquiryHandler = new InquiryHandler();
        inquiryHandler.setSitesForInquiry(inquiry.getId(), searchDetailsBean.getSelectedSites());

        spawnProjectIfNeeded(bypassExamination, inquiry, hadProjectBefore, inquiryHandler);

        logger.debug("Inquiry edited.");
        searchDetailsBean.clearStuff();
        FacesMessage msg;
        if (bypassExamination) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", Messages.getString("inquiryEditedAndReleased"));
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", Messages.getString("inquiryEditedAndSentToExamination"));
        }
        org.omnifaces.util.Messages.addFlashGlobal(msg);
        return "dashboard?faces-redirect=true";
    }

    /**
     * If necessary, spawn a project linked with the inquiry
     *
     * @param bypassExamination if true, spawning of a project will be skipped
     * @param inquiry the inquiry to check
     * @param hadProjectBefore if true, the inquiry already is linked to a project
     * @param inquiryHandler the inquiry handler to use
     */
    private void spawnProjectIfNeeded(boolean bypassExamination, Inquiry inquiry, boolean hadProjectBefore, InquiryHandler inquiryHandler) {
        if (ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk") && !bypassExamination) {
            logger.debug("Spawning project and sending mail to ccp office");
            //TODO replace userid
            inquiryHandler.spawnProjectIfNeeded(inquiry, searchDetailsBean.getResultTypes(), 1);
            if (hadProjectBefore) {
                MailUtils.sendModifiedProjectProposalMail(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
            } else {
                MailUtils.sendProjectProposalMail(ProjectInfo.INSTANCE.getConfig().getProperty(CCP_OFFICE_MAIL));
            }
        }
    }

    /**
     * Validate inquiry.
     *
     * @param inquiry the inquiry
     * @return A list of strings for each validation error, or an empty list if the query is ok
     */
    private List<String> validateInquiry(Inquiry inquiry) {
        List<String> errorMsg = new ArrayList<>();

        if (inquiry == null) {
            logger.error("Inquiry is NOT OK");
            errorMsg.add("inquiryNotFound");
            return errorMsg;
        }

        boolean isDktk = ProjectInfo.INSTANCE.getProjectName().equalsIgnoreCase("dktk");

        //TODO replace new User()
        User loggedUser = new User();
        Integer loggedUserSiteId = UserUtil.getSiteIdForUser(loggedUser);
        UserSite userSite = UserSiteUtil.fetchUserSiteByUser(loggedUser);

        boolean bypassExamination = userSite != null && userSite.getApproved() && Utils.onlySelf(searchDetailsBean.getSelectedSites(), loggedUserSiteId);
        int exposeId;
        try {
            exposeId = searchDetailsBean.getExpose().getId();
        } catch (Exception e) {
            logger.debug("No expose found");
            exposeId = -1;
        }

        if (!bypassExamination && exposeId < 1) {
            errorMsg.add("noExpose");
        }

        if (StringUtils.isEmpty(inquiry.getLabel())) {
            errorMsg.add("noLabel");
        }

        if (StringUtils.isEmpty(inquiry.getDescription())) {
            errorMsg.add("noDescription");
        }

        if (isDktk && StringUtils.isEmpty(inquiry.getResultType())) {
            errorMsg.add("resultTypeRequired");
        }

        try {
            Query query = QueryConverter.xmlToQuery(inquiry.getCriteria());
            if (query.getWhere().getAndOrEqOrLike().isEmpty()) {
                errorMsg.add("noCriteria");
            }
        } catch (JAXBException | NullPointerException e) {
            errorMsg.add("errorInQuery");
        }

        List<Site> sites = SiteUtil.fetchSitesForInquiry(inquiry.getId());
        if (sites == null || sites.size() < 1) {
            errorMsg.add("siteRequired");
        }

        if (errorMsg.isEmpty()) {
            logger.info("Inquiry is OK");
        } else {
            logger.error("Inquiry is NOT OK");
        }

        return errorMsg;
    }

    /**
     * release query from UI for bridgeheads
     * @param simpleQueryDtoXml the query
     * @param loggedUser the logged User
     * @return the query ID
     * @throws JAXBException
     */

    public static int releaseQuery(String simpleQueryDtoXml, User loggedUser) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(SimpleQueryDto.class);
        SimpleQueryDto simpleQueryDto=QueryConverter.unmarshal(simpleQueryDtoXml,jaxbContext,SimpleQueryDto.class);
        Query query = new SimpleQueryDto2ShareXmlTransformer().toQuery(simpleQueryDto);
        InquiryHandler inquiryHandler = new InquiryHandler();
        int inquiryId = inquiryHandler.storeAndRelease(query, loggedUser.getId(), "", "", -1, -1, new ArrayList<String>(), true);
        List<String> siteIds = new ArrayList<>();
        for (Site site : SiteUtil.fetchSites()) {
            siteIds.add(site.getId().toString());
        }
        inquiryHandler.setSitesForInquiry(inquiryId, siteIds);
        return inquiryId;
    }

    /**
     * get replys from the bridgeheads of the query
     * @param id the id of the query
     * @return all results as JSON String
     */
    public static String getReplysFromQuery(int id) {
        List<Reply> replyList = ReplyUtil.getReplyforInquriy(id);
        JSONArray jsonArray = new JSONArray();

        for (Reply reply : replyList) {
            JSONParser parser = new JSONParser();
            JSONObject json;
            try {
                json = (JSONObject) parser.parse(reply.getContent());
                jsonArray.put(json);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        logger.info("Sending replys to UI...");
        return jsonArray.toString();
    }

    @Override
    public String getSerializedQuery() {
        return searchDetailsBean.getSerializedQuery();
    }

    @Override
    public void setSerializedQuery(String serializedQuery) {
        searchDetailsBean.setSerializedQuery(serializedQuery);
    }

}
