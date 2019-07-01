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
import de.samply.share.broker.model.EnumProjectType;
import de.samply.share.broker.model.db.enums.*;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.rest.InquiryHandler;
import de.samply.share.broker.utils.MailUtils;
import de.samply.share.broker.utils.PdfUtils;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.model.uiquerybuilder.QueryItem;
import de.samply.share.common.utils.QueryTreeUtil;
import de.samply.share.common.utils.SamplyShareUtils;
import de.samply.share.model.common.Query;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * A JSF Managed Bean that is used for most of the project management tasks
 */
@ManagedBean(name = "ProjectController")
@ViewScoped
public class ProjectController implements Serializable {

    private static final long serialVersionUID = 876573067329775658L;

    /**
     * The Constant logger.
     */
    private static final Logger logger = LogManager.getLogger(ProjectController.class);

    private static final String resetFileinputAndCreatePopovers = "$('#documentBoxForm .fileinput-remove-button').trigger('click'); createEventhandlers();";
    private static final String resetFileinput = "$('#documentBoxForm .fileinput-remove-button').trigger('click');";
    private static final String createEventhandlers = "createEventhandlers();";
    private static final String BELL_ICON = "fa-bell";
    private static final String MAIL_ICON = "fa-mail-forward";

    /**
     * The login controller.
     */
    @ManagedProperty(value = "#{loginController}")
    private LoginController loginController;

    private List<Project> projects;
    private Project selectedProject;
    private int selectedProjectId;
    /**
     * A tree holding query items (and conjunction groups). Basically "the inquiry"
     */
    private TreeModel<QueryItem> criteriaTree;
    private String newNote;
    private String projectName;
    private List<String> selectedSites;
    private List<String> projectPartners;
    private Date startDate;
    private Date endDateEstimated;
    private Date endDateActual;
    private Part newDocument;
    private List<Document> documents;
    private List<Action> reminders;
    private List<Action> remindersExternal;
    private List<Action> callbacks;
    private Action newReminder;
    private Action newReminderExternal;
    private Date newReminderDate;
    private Date newReminderExternalDate;
    private List<DocumentType> documentTypes;

    public LoginController getLoginController() {
        return loginController;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    public int getSelectedProjectId() {
        return selectedProjectId;
    }

    public void setSelectedProjectId(int selectedProjectId) {
        this.selectedProjectId = selectedProjectId;
    }

    public TreeModel<QueryItem> getCriteriaTree() {
        return criteriaTree;
    }

    public void setCriteriaTree(TreeModel<QueryItem> criteriaTree) {
        this.criteriaTree = criteriaTree;
    }

    public String getNewNote() {
        return newNote;
    }

    public void setNewNote(String newNote) {
        this.newNote = newNote;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getSelectedSites() {
        return selectedSites;
    }

    public void setSelectedSites(List<String> selectedSites) {
        this.selectedSites = selectedSites;
    }

    public List<String> getProjectPartners() {
        return projectPartners;
    }

    public void setProjectPartners(List<String> projectPartners) {
        this.projectPartners = projectPartners;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDateEstimated() {
        return endDateEstimated;
    }

    public void setEndDateEstimated(Date endDateEstimated) {
        this.endDateEstimated = endDateEstimated;
    }

    public Date getEndDateActual() {
        return endDateActual;
    }

    public void setEndDateActual(Date endDateActual) {
        this.endDateActual = endDateActual;
    }

    public Part getNewDocument() {
        return newDocument;
    }

    public void setNewDocument(Part newDocument) {
        this.newDocument = newDocument;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<Action> getReminders() {
        return reminders;
    }

    public void setReminders(List<Action> reminders) {
        this.reminders = reminders;
    }

    public List<Action> getRemindersExternal() {
        return remindersExternal;
    }

    public void setRemindersExternal(List<Action> remindersExternal) {
        this.remindersExternal = remindersExternal;
    }

    public List<Action> getCallbacks() {
        return callbacks;
    }

    public void setCallbacks(List<Action> callbacks) {
        this.callbacks = callbacks;
    }

    public Action getNewReminder() {
        return newReminder;
    }

    public void setNewReminder(Action newReminder) {
        this.newReminder = newReminder;
    }

    public Action getNewReminderExternal() {
        return newReminderExternal;
    }

    public void setNewReminderExternal(Action newReminderExternal) {
        this.newReminderExternal = newReminderExternal;
    }

    public Date getNewReminderDate() {
        return newReminderDate;
    }

    public void setNewReminderDate(Date newReminderDate) {
        this.newReminderDate = newReminderDate;
    }

    public Date getNewReminderExternalDate() {
        return newReminderExternalDate;
    }

    public void setNewReminderExternalDate(Date newReminderExternalDate) {
        this.newReminderExternalDate = newReminderExternalDate;
    }

    public List<DocumentType> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<DocumentType> documentTypes) {
        this.documentTypes = documentTypes;
    }

    public List<Site> getSites() {
        return SiteUtil.fetchSites();
    }

    @PostConstruct
    public void init() {
        projects = new ArrayList<>();
        newReminder = new Action();
        newReminderExternal = new Action();
        documentTypes = new ArrayList<>(EnumSet.allOf(DocumentType.class));
        // remove expose-type
        documentTypes.remove(DocumentType.DT_EXPOSE);
    }

    /**
     * Load the list of sites, the selected project is (or shall be when it is released) distributed to
     */
    private void loadSelectedSites() {
        selectedSites = new ArrayList<>();
        Inquiry firstInquiryForProject = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
        if (firstInquiryForProject != null) {
            List<Site> sites = SiteUtil.fetchSitesForInquiry(firstInquiryForProject.getId());
            if (sites != null) {
                for (Site site : sites) {
                    selectedSites.add(Integer.toString(site.getId()));
                }
            } else {
                logger.error("Sites is null");
            }
        } else {
            logger.error("First inquiry for project is null");
        }
    }

    /**
     * Load the list of sites that are defined as partner sites for the selected project
     */
    private void loadProjectPartners() {
        projectPartners = new ArrayList<>();
        List<Site> sites = ProjectUtil.fetchProjectPartnersForProject(selectedProjectId);
        if (sites != null) {
            for (Site site : sites) {
                projectPartners.add(Integer.toString(site.getId()));
            }
        } else {
            logger.error("No Project Partners");
        }
    }

    /**
     * Load all projects with a given type
     *
     * @param projectType the project type to load
     */
    public void loadProjects(EnumProjectType projectType) {
        projects = ProjectUtil.fetchProjectsByType(projectType);
    }

    /**
     * Load a project and fill all associated entities as well
     *
     * @param setSeen set to true if the loaded project shall be marked as seen
     */
    public void loadProject(boolean setSeen) {
        selectedProject = ProjectUtil.fetchProjectById(selectedProjectId);

        if (selectedProject != null) {
            populateCriteriaTree();
            loadSelectedSites();
            loadProjectPartners();
            loadDocuments();
            loadReminders();
            loadRemindersExternal();
            loadCallbacks();
            startDate = selectedProject.getStarted();
            endDateActual = selectedProject.getEndActual();
            endDateEstimated = selectedProject.getEndEstimated();
            selectedProject.setSeen(setSeen);
            ProjectUtil.updateProject(selectedProject);
            // pre-fill the projectname with the inquiry name
            projectName = selectedProject.getName();
        } else {
            logger.warn("Selected project is null...");
        }

    }

    /**
     * Get the criteria from the first inquiry associated with the project and transform it to a tree
     */
    private void populateCriteriaTree() {
        criteriaTree = new ListTreeModel<>();

        if (selectedProject == null) {
            return;
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Query.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Inquiry firstInquiryForProject = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
            if (firstInquiryForProject != null) {
                Integer inquiryId = firstInquiryForProject.getId();
                String criteria = InquiryDetailsUtil.fetchCriteriaForInquiryIdTypeQuery(inquiryId);

                if (!StringUtils.isEmpty(criteria)) {
                    StringReader stringReader = new StringReader(criteria);
                    Query query = (Query) unmarshaller.unmarshal(stringReader);
                    criteriaTree = QueryTreeUtil.queryToTree(query);
                    logger.debug("Loaded inquiry details with inqiuryId " + inquiryId + " and type '" + InquiryDetailsType.CQL  + "' from db.");
                } else {
                    criteriaTree = new ListTreeModel<>();
                    logger.debug("Inquiry details for with inquiryId " + inquiryId + " and type '" + InquiryDetailsType.CQL  + "' not found.");
                }

            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }

    /**
     * Load all documents that are linked with the selected project
     */
    private void loadDocuments() {
        documents = DocumentUtil.getDocumentsForProject(selectedProjectId);
    }

    /**
     * Load all reminders that are linked with the selected project
     */
    private void loadReminders() {
        reminders = ActionUtil.getRemindersForProject(selectedProjectId);
    }

    /**
     * Load all external reminders that are linked with the selected project
     */
    private void loadRemindersExternal() {
        remindersExternal = ActionUtil.getRemindersExternalForProject(selectedProjectId);
    }

    /**
     * Load all callbacks that are linked with the selected project
     */
    private void loadCallbacks() {
        callbacks = ActionUtil.getCallbacksForProject(selectedProjectId);
    }

    /**
     * Export the expose for the selected project to pdf and send it to the user
     *
     * @param projectId the id of the project of which the expose shall be exported
     */
    public void exportExposee(int projectId) throws IOException {
        Inquiry firstInquiryForProject = ProjectUtil.fetchFirstInquiryForProject(projectId);
        if (firstInquiryForProject != null) {
            int inquiryId = firstInquiryForProject.getId();
            int applicationNumber = ProjectUtil.fetchProjectById(projectId).getApplicationNumber();
            ByteArrayOutputStream bos = DocumentUtil.getExposeOutputStreamByInquiryId(inquiryId);
            if (applicationNumber > 0) {
                Faces.sendFile(bos.toByteArray(), "Antrag_" + applicationNumber + PdfUtils.FILENAME_SUFFIX_PDF, true);
            } else {
                Faces.sendFile(bos.toByteArray(), "Antrag_unbekannt" + projectId + PdfUtils.FILENAME_SUFFIX_PDF, true);
            }
        } else {
            logger.error("Inquiry is null");
        }
    }

    /**
     * Export the project info of the selected project as pdf and send it to the user
     */
    public void exportProject() throws IOException, DocumentException {
        logger.debug("Export Project called for project " + selectedProject.getId());
        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
        ByteArrayOutputStream bos = PdfUtils.createPdfOutputstream(inquiry);
        Faces.sendFile(bos.toByteArray(), ProjectUtil.getProjectTitleById(selectedProjectId) + PdfUtils.FILENAME_SUFFIX_PDF, true);
    }

    /**
     * Send the document with the given id to the user
     *
     * @param documentId the id of the document to send
     */
    public void exportDocument(int documentId) throws IOException {
        logger.debug("Export Document called for document id " + documentId);
        Document document = DocumentUtil.getDocumentById(documentId);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(document.getData());
        bos.close();
        Faces.sendFile(bos.toByteArray(), document.getFilename(), true);
    }

    /**
     * Accept a project proposal, allowing the inquiry to be downloaded by clients
     *
     * @return a navigation case to redirect to the pending projects list
     */
    public String grantProject() {
        int userId = loginController.getUser().getId();
        NoteUtil.addNoteToProject(newNote, userId, selectedProjectId);
        selectedProject.setApproved(SamplyShareUtils.getCurrentSqlTimestamp());
        selectedProject.setStatus(ProjectStatus.PS_OPEN_DISTRIBUTION);
        ProjectUtil.updateProject(selectedProject);

        MailUtils.sendProjectGrantedInfo(selectedProject, newNote, selectedSites);

        Action action = new Action();
        action.setProjectId(selectedProjectId);
        action.setMessage("Kollaborationsanfrage freigegeben");
        action.setType(ActionType.AT_PROJECT_OPENED);
        action.setTime(SamplyShareUtils.getCurrentTime());
        action.setUserId(userId);
        ActionUtil.insertAction(action);
        InquiryHandler inquiryHandler = new InquiryHandler();
        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
        if (inquiry != null) {
            inquiryHandler.setSitesForInquiry(inquiry.getId(), selectedSites);
        } else {
            logger.error("Inquiry is null");
        }

        return "projects_pending?faces-redirect=true";
    }

    /**
     * Reject a project proposal
     *
     * @return a navigation case to redirect to the dashboard
     */
    public String rejectProject() {
        int userId = loginController.getUser().getId();
        NoteUtil.addNoteToProject(newNote, userId, selectedProjectId);
        selectedProject.setStatus(ProjectStatus.PS_REJECTED);
        ProjectUtil.updateProject(selectedProject);

        // If a project is rejected, consider the inquiry outdated
        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
        if (inquiry != null) {
            inquiry.setStatus(InquiryStatus.IS_OUTDATED);
            InquiryUtil.updateInquiry(inquiry);
        }

        MailUtils.sendProjectRejectedInfo(selectedProject, newNote);

        Action action = new Action();
        action.setProjectId(selectedProjectId);
        action.setMessage("Kollaborationsanfrage abgelehnt");
        action.setType(ActionType.AT_PROJECT_REJECTED);
        action.setTime(SamplyShareUtils.getCurrentTime());
        action.setUserId(userId);
        ActionUtil.insertAction(action);
        return "dashboard?faces-redirect=true";
    }

    /**
     * Post a call back to the inquirer in order to ask for further information
     *
     * @return a navigation case to redirect to the pending projects list
     */
    public String callbackProject() {
        int userId = loginController.getUser().getId();
        NoteUtil.addNoteToProject(newNote, loginController.getUser().getId(), selectedProjectId);
        selectedProject.setStatus(ProjectStatus.PS_OPEN_MOREINFO_NEEDED);
        ProjectUtil.updateProject(selectedProject);

        MailUtils.sendProjectCallbackInfo(selectedProject, newNote);

        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
        if (inquiry != null) {
            inquiry.setStatus(InquiryStatus.IS_DRAFT);
            InquiryUtil.updateInquiry(inquiry);
        }

        Action action = new Action();
        action.setProjectId(selectedProjectId);
        action.setMessage(newNote);
        action.setType(ActionType.AT_PROJECT_CALLBACK_SENT);
        action.setTime(SamplyShareUtils.getCurrentTime());
        action.setUserId(userId);
        ActionUtil.insertAction(action);
        return "projects_pending?faces-redirect=true";
    }

    /**
     * Activate a project that was spawned by a previously distributed inquiry
     *
     * @return a navigation case to redirect to the active projects list
     */
    public String activateProject() {
        int userId = loginController.getUser().getId();
        selectedProject.setStatus(ProjectStatus.PS_ACTIVE);
        selectedProject.setName(projectName);
        java.sql.Date sqlDate;
        if (startDate != null) {
            sqlDate = new java.sql.Date(startDate.getTime());
            selectedProject.setStarted(sqlDate);
        }
        if (endDateEstimated != null) {
            sqlDate = new java.sql.Date(endDateEstimated.getTime());
            selectedProject.setEndEstimated(sqlDate);
        }
        ProjectUtil.updateProject(selectedProject);

        MailUtils.sendProjectActivatedInfo(selectedProject, projectPartners);

        Action action = new Action();
        action.setProjectId(selectedProjectId);
        action.setMessage("Projekt aktiviert");
        action.setType(ActionType.AT_PROJECT_ACTIVATED);
        action.setTime(SamplyShareUtils.getCurrentTime());
        action.setUserId(userId);
        ActionUtil.insertAction(action);
        ProjectUtil.setProjectPartnersForProject(selectedProjectId, projectPartners);
        return "projects_active?faces-redirect=true";
    }

    /**
     * Close a project when it's finished
     *
     * @return a navigation case to redirect to the archived projects list
     */
    public String closeProject() {
        int userId = loginController.getUser().getId();
        selectedProject.setStatus(ProjectStatus.PS_CLOSED);
        java.sql.Date sqlDate;
        if (endDateActual != null) {
            sqlDate = new java.sql.Date(endDateActual.getTime());
            selectedProject.setEndActual(sqlDate);
        }
        ProjectUtil.updateProject(selectedProject);

        MailUtils.sendProjectArchivedInfo(selectedProject, newNote, DocumentUtil.fetchFinalReportByProjectId(selectedProjectId) != null);

        // If a project is closed, consider the inquiry outdated
        Inquiry inquiry = ProjectUtil.fetchFirstInquiryForProject(selectedProjectId);
        if (inquiry != null) {
            inquiry.setStatus(InquiryStatus.IS_OUTDATED);
            InquiryUtil.updateInquiry(inquiry);
        }

        Action action = new Action();
        action.setProjectId(selectedProjectId);
        action.setMessage("Projekt als abgeschlossen markiert.");
        action.setType(ActionType.AT_PROJECT_ARCHIVED);
        action.setTime(SamplyShareUtils.getCurrentTime());
        action.setUserId(userId);
        ActionUtil.insertAction(action);
        return "projects_archive?faces-redirect=true";
    }

    /**
     * Handle the upload of a document from the client
     *
     * @param event the ajax event associated with this listener
     */
    public void handleDocumentUpload(AjaxBehaviorEvent event) {
        logger.debug("file size: " + newDocument.getSize());
        logger.debug("file type: " + newDocument.getContentType());
        logger.debug("file info: " + newDocument.getHeader("Content-Disposition"));
        try {
            File documentFile = save(newDocument);
            InquiryHandler inquiryHandler = new InquiryHandler();
            // By default, new documents are declared as "other". The selection of the document type follows later
            inquiryHandler.addDocument(selectedProjectId, null, loginController.getUser().getId(), documentFile, SamplyShareUtils.getFilenameFromContentDisposition(newDocument.getHeader("Content-Disposition")), newDocument.getContentType(), DocumentType.DT_OTHER);
            loadDocuments();
            documentFile.delete();
            newDocument = null;
            Ajax.oncomplete(resetFileinput, createEventhandlers);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Document upload failed.");
        }
    }

    /**
     * Delete a reminder and re-bind the eventhandlers
     */
    public void deleteReminder() {
        String reminderIdString = Faces.getRequestParameter("elementId");
        try {
            int reminderId = Integer.parseInt(reminderIdString);
            ActionUtil.deleteReminder(reminderId);
            loadReminders();
            loadRemindersExternal();
            logger.debug("Deleted reminder with id: " + reminderId);
            Ajax.oncomplete(createEventhandlers);
        } catch (NumberFormatException e) {
            logger.warn("Could not parse reminder id: " + reminderIdString);
        }
    }

    /**
     * Reset the new reminder fields and re-bind the eventhandlers
     */
    public void resetNewReminder() {
        newReminder = new Action();
        newReminderDate = null;
        Ajax.oncomplete(createEventhandlers);
    }

    /**
     * Reset the new external reminder fields and re-bind the eventhandlers
     */
    public void resetNewReminderExternal() {
        newReminderExternal = new Action();
        newReminderExternalDate = null;
        Ajax.oncomplete(createEventhandlers);
    }

    /**
     * Store the new reminder and re-bind the eventhandlers
     */
    public void storeNewReminder() {
        newReminder.setDate(new java.sql.Date(newReminderDate.getTime()));
        newReminder.setProjectId(selectedProjectId);
        newReminder.setType(ActionType.AT_REMINDER);
        newReminder.setUserId(loginController.getUser().getId());
        newReminder.setIcon(BELL_ICON);
        ActionUtil.insertAction(newReminder);
        loadReminders();
        Ajax.oncomplete(createEventhandlers);
        resetNewReminder();
    }

    /**
     * Store the new external reminder and re-bind the eventhandlers
     */
    public void storeNewReminderExternal() {
        newReminderExternal.setDate(new java.sql.Date(newReminderExternalDate.getTime()));
        newReminderExternal.setProjectId(selectedProjectId);
        newReminderExternal.setType(ActionType.AT_REMINDER_EXTERNAL);
        newReminderExternal.setUserId(loginController.getUser().getId());
        newReminderExternal.setIcon(MAIL_ICON);
        ActionUtil.insertAction(newReminderExternal);
        loadRemindersExternal();
        Ajax.oncomplete(createEventhandlers);
        resetNewReminderExternal();
    }

    /**
     * Delete the selected document and re-bind the eventhandlers
     */
    public void deleteDocument() {
        String documentIdString = Faces.getRequestParameter("elementId");
        try {
            int documentId = Integer.parseInt(documentIdString);
            DocumentUtil.deleteDocument(documentId);
            loadDocuments();
            logger.debug("Deleted document with id: " + documentId);
            Ajax.oncomplete(createEventhandlers);
        } catch (NumberFormatException e) {
            logger.warn("Could not parse document id: " + documentIdString);
        }
    }

    /**
     * Send a mail, informing the inquirer that his request was forwarded for external assessment
     */
    public void sendAssessmentMail() {
        MailUtils.sendAssessmentInfo(selectedProject);
        selectedProject.setExternalAssessment(true);
        ProjectUtil.updateProject(selectedProject);

        int userId = loginController.getUser().getId();
        Action action = new Action();
        action.setProjectId(selectedProjectId);
        action.setMessage("Mail bzgl. Begutachtung verschickt.");
        action.setType(ActionType.AT_USER_MESSAGE);
        action.setTime(SamplyShareUtils.getCurrentTime());
        action.setUserId(userId);
        ActionUtil.insertAction(action);
    }

    /**
     * Save a file part received from the client
     *
     * @param part the file part to save
     * @return the new file
     */
    private File save(Part part) throws IOException {
        return Utils.savePartToTmpFile("project_doc", part);
    }

    /**
     * Change the document type for a given document id and refresh the list of documents afterwards
     *
     * @param docId        the id of the document to change
     * @param documentType the new document type
     */
    public void setDocType(int docId, DocumentType documentType) {
        Document document = DocumentUtil.getDocumentById(docId);
        document.setDocumentType(documentType);
        DocumentUtil.updateDocument(document);
        loadDocuments();
    }

    /**
     * Check if there is a final report available for the given project
     *
     * @param projectId the project for which to check
     * @return true if there is a final report available, false if not
     */
    public boolean projectHasFinalReport(int projectId) {
        return (getFinalReport(projectId) != null);
    }

    /**
     * Get the final report
     *
     * @param projectId the id of the project for which the final report shall be received
     * @return the final report
     */
    public Document getFinalReport(int projectId) {
        return DocumentUtil.fetchFinalReportByProjectId(projectId);
    }

    /**
     * Check if the selected project has a local vote
     *
     * @return true if it has a vote, false otherwise
     */
    public boolean selectedProjectHasVote() {
        return !SamplyShareUtils.isNullOrEmpty(DocumentUtil.getDocumentsForProject(selectedProjectId, DocumentType.DT_VOTE));
    }

}
