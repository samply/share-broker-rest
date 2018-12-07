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

import de.samply.share.broker.model.db.tables.pojos.Inquiry;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.UserUtil;
import de.samply.share.common.model.uiquerybuilder.QueryItem;
import de.samply.share.common.utils.QueryTreeUtil;
import de.samply.share.model.common.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.model.tree.ListTreeModel;
import org.omnifaces.model.tree.TreeModel;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InquiryController implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -5890029855627854464L;

    private static final Logger logger = LogManager.getLogger(InquiryController.class);

    /** The List of inquiry drafts. */
    private List<Inquiry> inquiryDrafts;

    /** The List of released inquiries. */
    private List<Inquiry> releasedInquiries;

    /** The List of released inquiries that are associated with a project. */
    private List<Inquiry> releasedInquiriesWithProjects;

    /** The List of archived inquiries. */
    private List<Inquiry> outdatedInquiries;

    /** The selected inquiry. */
    private Inquiry selectedInquiry;

    /** The id of the selected inquiry. */
    private int selectedInquiryId;
    
    private boolean selectedInquiryHasExpose;

    /** A tree holding query items (and conjunction groups). Basically "the inquiry" */
    private TreeModel<QueryItem> criteriaTree;

    /** Inject the managed bean "login controller". */
    @ManagedProperty(value = "#{loginController}")
    private LoginController loginController;

    @ManagedProperty(value = "#{searchDetailsBean}")
    private SearchDetailsBean searchDetailsBean;

    @ManagedProperty(value = "#{ProjectController}")
    private ProjectController projectController;

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
        if(Objects.equals(loginController.getUser().getId(), selectedInquiry.getAuthorId())) { // only signed in users can see his inqueries
            selectedInquiryHasExpose = InquiryUtil.inquiryHasExpose(selectedInquiryId);

            populateCriteriaTree();
            if (selectedInquiry.getProjectId() != null) {
                projectController.setSelectedProjectId(selectedInquiry.getProjectId());
                projectController.loadProject(false);
            }
        }else{
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("https://google.de");
                FacesContext.getCurrentInstance().responseComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            JAXBContext jaxbContext = JAXBContext.newInstance(Query.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            StringReader stringReader = new StringReader(selectedInquiry.getCriteria());
            Query query = (Query) unmarshaller.unmarshal(stringReader);
            criteriaTree = QueryTreeUtil.queryToTree(query);

        } catch (JAXBException ex) {
           throw new RuntimeException("Error populating criteria tree", ex);
        }
    }

}
