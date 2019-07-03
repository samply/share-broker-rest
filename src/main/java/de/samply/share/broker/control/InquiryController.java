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

import com.itextpdf.text.DocumentException;
import de.samply.share.broker.messages.Messages;
import de.samply.share.broker.model.db.enums.InquiryStatus;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.PdfUtils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.model.uiquerybuilder.QueryItem;
import de.samply.share.common.utils.QueryTreeUtil;
import de.samply.share.model.common.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;
import org.omnifaces.util.Faces;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A JSF Managed Bean that is valid for the lifetime of a view.
 */
@ManagedBean(name = "inquiryController")
@ViewScoped
public class InquiryController implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -5890029855627854464L;

    private static final Logger logger = LogManager.getLogger(InquiryController.class);

    /**
     * The List of inquiry drafts.
     */
    private List<Inquiry> inquiryDrafts;

    /**
     * The List of released inquiries.
     */
    private List<Inquiry> releasedInquiries;

    /**
     * The List of released inquiries that are associated with a project.
     */
    private List<Inquiry> releasedInquiriesWithProjects;

    /**
     * The List of archived inquiries.
     */
    private List<Inquiry> outdatedInquiries;

    /**
     * The selected inquiry.
     */
    private Inquiry selectedInquiry;

    /**
     * The id of the selected inquiry.
     */
    private int selectedInquiryId;

    private boolean selectedInquiryHasExpose;

    /**
     * A tree holding query items (and conjunction groups). Basically "the inquiry"
     */
    private TreeModel<QueryItem> criteriaTree;

    /**
     * Inject the managed bean "login controller".
     */
    @ManagedProperty(value = "#{loginController}")
    private LoginController loginController;

    @ManagedProperty(value = "#{searchDetailsBean}")
    private SearchDetailsBean searchDetailsBean;

    @ManagedProperty(value = "#{ProjectController}")
    private ProjectController projectController;

    public List<Inquiry> getInquiryDrafts() {
        return inquiryDrafts;
    }

    public void setInquiryDrafts(List<Inquiry> inquiryDrafts) {
        this.inquiryDrafts = inquiryDrafts;
    }

    /**
     * Gets the list of inquiries.
     *
     * @return the list of inquiries
     */
    public List<Inquiry> getReleasedInquiries() {
        return releasedInquiries;
    }

    /**
     * Sets the list of inquiries.
     *
     * @param releasedInquiries the new list of inquiries
     */
    public void setReleasedInquiries(List<Inquiry> releasedInquiries) {
        this.releasedInquiries = releasedInquiries;
    }

    /**
     * Sets the list of inquiries that are associated with a project.
     *
     * @param releasedInquiriesWithProjects the new list of inquiries
     */
    public void setReleasedInquiriesWithProjects(List<Inquiry> releasedInquiriesWithProjects) {
        this.releasedInquiriesWithProjects = releasedInquiriesWithProjects;
    }

    /**
     * Gets the list of inquiries that are associated with a project.
     *
     * @return the list of inquiries
     */
    public List<Inquiry> getReleasedInquiriesWithProjects() {
        return releasedInquiriesWithProjects;
    }

    public List<Inquiry> getOutdatedInquiries() {
        return outdatedInquiries;
    }

    public void setOutdatedInquiries(List<Inquiry> outdatedInquiries) {
        this.outdatedInquiries = outdatedInquiries;
    }

    /**
     * Gets the selected inquiry.
     *
     * @return the selected inquiry
     */
    public Inquiry getSelectedInquiry() {
        return selectedInquiry;
    }

    /**
     * Sets the selected inquiry.
     *
     * @param selectedInquiry the new selected inquiry
     */
    public void setSelectedInquiry(Inquiry selectedInquiry) {
        this.selectedInquiry = selectedInquiry;
    }

    /**
     * Gets the id of the selected inquiry.
     *
     * @return the id of the selected inquiry
     */
    public int getSelectedInquiryId() {
        return selectedInquiryId;
    }

    /**
     * Sets the id of the selected inquiry.
     *
     * @param selectedInquiryId the new id of the selected inquiry
     */
    public void setSelectedInquiryId(int selectedInquiryId) {
        this.selectedInquiryId = selectedInquiryId;
    }

    /**
     * Check if the selected inquiry has an expose stored. Used to control whether a link shall be displayed or not
     *
     * @return true if the selected inquiry has an associated expose,
     * false otherwise
     */
    public boolean isSelectedInquiryHasExpose() {
        return selectedInquiryHasExpose;
    }

    /**
     * Gets the criteria tree.
     *
     * @return the criteria tree
     */
    public TreeModel<QueryItem> getCriteriaTree() {
        return criteriaTree;
    }

    /**
     * Sets the criteria tree.
     *
     * @param criteriaTree the new criteria tree
     */
    public void setCriteriaTree(TreeModel<QueryItem> criteriaTree) {
        this.criteriaTree = criteriaTree;
    }

    /**
     * Gets the login controller.
     *
     * @return the login controller
     */
    public LoginController getLoginController() {
        return loginController;
    }

    /**
     * Sets the login controller.
     *
     * @param loginController the new login controller
     */
    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    public SearchDetailsBean getSearchDetailsBean() {
        return searchDetailsBean;
    }

    public void setSearchDetailsBean(SearchDetailsBean searchDetailsBean) {
        this.searchDetailsBean = searchDetailsBean;
    }

    public ProjectController getProjectController() {
        return projectController;
    }

    public void setProjectController(ProjectController projectController) {
        this.projectController = projectController;
    }

    /**
     * Initializes the Inquiry Controller by loading the list of inquiries from the database.
     */
    @PostConstruct
    public void init() {
        inquiryDrafts = new ArrayList<>();
        releasedInquiries = new ArrayList<>();
        releasedInquiriesWithProjects = new ArrayList<>();
        outdatedInquiries = new ArrayList<>();
        loadInquiries();
    }

    /**
     * Load the list of inquiries from the database. Only those that belong to the current user are loaded.
     */
    private void loadInquiries() {
        User user = UserUtil.fetchUserByAuthId(loginController.getLoggedUsername());
        inquiryDrafts = InquiryUtil.fetchInquiryDraftsFromUserOrderByDate(user.getId());
        releasedInquiries = InquiryUtil.fetchReleasedInquiriesFromUserOrderByDate(user.getId());
        releasedInquiriesWithProjects = InquiryUtil.fetchReleasedInquiriesWithProjectsFromUserOrderByDate(user.getId());
        outdatedInquiries = InquiryUtil.fetchArchivedInquiriesFromUserOrderByDate(user.getId());
    }

    /**
     * Populates the criteria tree for the selected inquiry.
     */
    public void loadInquiry() {
        selectedInquiry = InquiryUtil.fetchInquiryById(selectedInquiryId);
        if (Objects.equals(loginController.getUser().getId(), selectedInquiry.getAuthorId())) { // only signed in users can see his inqueries
            selectedInquiryHasExpose = InquiryUtil.inquiryHasExpose(selectedInquiryId);

            populateCriteriaTree();
            if (selectedInquiry.getProjectId() != null) {
                projectController.setSelectedProjectId(selectedInquiry.getProjectId());
                projectController.loadProject(false);
            }
        } else {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("https://google.de");
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load the inquiry with the id that was previously set via query parameter and redirect to the query builder
     *
     * @return the navigation outcome to the query details page
     */
    public String editInquiry() {
        User user = loginController.getUser();
        if (user == null) {
            logger.warn("Unknown user");
            return "dashboard";
        }
        loadInquiry();

        if (selectedInquiry == null || (!selectedInquiry.getAuthorId().equals(user.getId()))) {
            if (selectedInquiry == null) {
                logger.error("selectedInquiry is null");
            } else {
                logger.error("User has no access to inquiry. inquiryId=" + selectedInquiryId + " inquiry.authorid=" + selectedInquiry.getAuthorId() + " loggedUser.id=" + user.getId());
            }
            return "dashboard";
        }
        return searchDetailsBean.editQueryGotoStep1(String.valueOf(selectedInquiryId));
    }

    /**
     * Populate the criteria tree for the currently selected inquiry.
     */
    private void populateCriteriaTree() {
        criteriaTree = new ListTreeModel<>();

        if (selectedInquiry == null) {
            return;
        }

        try {
            String criteria = InquiryDetailsUtil.fetchCriteriaForInquiryIdTypeQuery(selectedInquiryId);
            StringReader stringReader = new StringReader(criteria);

            JAXBContext jaxbContext = JAXBContext.newInstance(Query.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Query query = (Query) unmarshaller.unmarshal(stringReader);
            criteriaTree = QueryTreeUtil.queryToTree(query);

        } catch (JAXBException ex) {
            throw new RuntimeException("Error populating criteria tree", ex);
        }
    }

    /**
     * Delete the selected inquiry from the database (only allowed for drafts)
     *
     * @return the navigation outcome to the drafts page
     */
    public String deleteSelectedInquiry() {
        FacesMessage msg;
        if (!InquiryUtil.deleteInquiryDraft(selectedInquiry)) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR", Messages.getString("inquiryDraftDeleteError"));
            org.omnifaces.util.Messages.addGlobal(msg);
            return "";
        } else {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "OK", Messages.getString("inquiryDraftDeleted"));
            org.omnifaces.util.Messages.addFlashGlobal(msg);
            return "drafts?faces-redirect=true";
        }
    }

    public List<Site> getSitesForSelectedInquiry() {
        return SiteUtil.fetchSitesForInquiry(selectedInquiryId);
    }

    public List<Site> getPartnersForSelectedInquiry() {
        return InquiryUtil.fetchPartnersForInquiry(selectedInquiryId);
    }

    public void extendSelectedInquiry() {
        InquiryUtil.extendInquiryById(selectedInquiryId);
        selectedInquiry = InquiryUtil.fetchInquiryById(selectedInquiryId);
    }

    public void expireSelectedInquiry() {
        InquiryUtil.expireInquiryById(selectedInquiryId);
        selectedInquiry = InquiryUtil.fetchInquiryById(selectedInquiryId);
    }

    public boolean isDeleteable() {
        if (selectedInquiry == null) {
            return false;
        }
        if (selectedInquiry.getStatus() != InquiryStatus.IS_DRAFT) {
            return false;
        }
        Integer projectId = selectedInquiry.getProjectId();
        return projectId == null || projectId <= 0;
    }

    /**
     * Export the inquiry info of as pdf and send it to the user
     */
    public void exportInquiry() throws IOException, DocumentException {
        logger.debug("Export Inquiry called for inquiry " + selectedInquiry.getId());
        ByteArrayOutputStream bos = PdfUtils.createPdfOutputstream(selectedInquiry);
        String filename = "Unbenannt";
        if (selectedInquiry.getLabel() != null) {
            filename = selectedInquiry.getLabel();
        }
        Faces.sendFile(bos.toByteArray(), filename + PdfUtils.FILENAME_SUFFIX_PDF, true);
    }

    public ArrayList<List<Object>> getReply(List<Site> sites) {
        ArrayList<List<Object>> result = new ArrayList<>();
        List<Reply> reply = ReplyUtil.getReplyforInquriy(selectedInquiryId);
        for (Reply replyTmp : reply) {
            BankSite bankSite = BankSiteUtil.fetchBankSiteByBankId(replyTmp.getBankId());
            for (Site siteTmp : sites) {
                if (Objects.equals(bankSite.getSiteId(), siteTmp.getId())) {
                    List<Object> tmp = new ArrayList<>();
                    tmp.add(siteTmp.getName());
                    tmp.add(replyTmp.getContent());
                    result.add(tmp);
                }
            }
        }
        return result;
    }
}
