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

import de.samply.share.broker.model.db.enums.DocumentType;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.ActionUtil;
import de.samply.share.broker.utils.db.DocumentUtil;
import de.samply.share.broker.utils.db.ProjectUtil;
import de.samply.share.broker.utils.db.SiteUtil;
import de.samply.share.common.model.uiquerybuilder.QueryItem;
import de.samply.share.common.utils.QueryTreeUtil;
import de.samply.share.model.common.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;
import org.omnifaces.util.Ajax;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.servlet.http.Part;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * for most of the project management tasks
 */
public class ProjectController implements Serializable {

    private static final long serialVersionUID = 876573067329775658L;

    /** The Constant logger. */
    private static final Logger logger = LogManager.getLogger(ProjectController.class);

    private static final String resetFileinput = "$('#documentBoxForm .fileinput-remove-button').trigger('click');";
    private static final String createEventhandlers = "createEventhandlers();";
    private static final String BELL_ICON = "fa-bell";
    private static final String MAIL_ICON = "fa-mail-forward";

    /** The login controller. */
    @ManagedProperty(value = "#{loginController}")
    private LoginController loginController;

    private List<Project> projects;
    private Project selectedProject;
    private int selectedProjectId;
    /** A tree holding query items (and conjunction groups). Basically "the inquiry" */
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

    public void setSelectedProjectId(int selectedProjectId) {
        this.selectedProjectId = selectedProjectId;
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
                StringReader stringReader = new StringReader(firstInquiryForProject.getCriteria());
                Query query = (Query) unmarshaller.unmarshal(stringReader);
                criteriaTree = QueryTreeUtil.queryToTree(query);
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
     * Save a file part received from the client
     *
     * @param part the file part to save
     * @return the new file
     */
    private File save(Part part) throws IOException {
        return Utils.savePartToTmpFile("project_doc", part);
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

}
