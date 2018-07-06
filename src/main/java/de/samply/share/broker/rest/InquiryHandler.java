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
package de.samply.share.broker.rest;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.itextpdf.awt.geom.misc.Messages;
import com.itextpdf.text.DocumentException;

import de.samply.share.broker.jdbc.ResourceManager;
import de.samply.share.broker.model.db.Tables;
import de.samply.share.broker.model.db.enums.ActionType;
import de.samply.share.broker.model.db.enums.DocumentType;
import de.samply.share.broker.model.db.enums.InquiryStatus;
import de.samply.share.broker.model.db.enums.ProjectStatus;
import de.samply.share.broker.model.db.tables.daos.ContactDao;
import de.samply.share.broker.model.db.tables.daos.InquiryDao;
import de.samply.share.broker.model.db.tables.daos.ReplyDao;
import de.samply.share.broker.model.db.tables.daos.UserDao;
import de.samply.share.broker.model.db.tables.pojos.Action;
import de.samply.share.broker.model.db.tables.pojos.BankSite;
import de.samply.share.broker.model.db.tables.pojos.Document;
import de.samply.share.broker.model.db.tables.pojos.Inquiry;
import de.samply.share.broker.model.db.tables.pojos.Project;
import de.samply.share.broker.model.db.tables.pojos.Reply;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.PdfUtils;
import de.samply.share.broker.utils.db.ActionUtil;
import de.samply.share.broker.utils.db.BankSiteUtil;
import de.samply.share.broker.utils.db.ContactUtil;
import de.samply.share.broker.utils.db.DocumentUtil;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.ProjectUtil;
import de.samply.share.broker.utils.db.SiteUtil;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.model.common.And;
import de.samply.share.model.common.Contact;
import de.samply.share.model.common.Info;
import de.samply.share.model.common.ObjectFactory;
import de.samply.share.model.common.Query;
import de.samply.share.model.common.Where;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.jooq.tools.json.ParseException;

/**
 * The Class InquiryHandler.
 */
public class InquiryHandler {

    private static final Logger logger = LogManager.getLogger(InquiryHandler.class);

    public InquiryHandler() {
    }

    /**
     * Store an inquiry and send it to the ccp office for validation
     *
     * @param query              the query criteria of the inquiry
     * @param userid             the id of the user that releases the inquiry
     * @param inquiryName        the label of the inquiry
     * @param inquiryDescription the description of the inquiry
     * @param exposeId           the id of the expose linked with this inquiry
     * @param voteId             the id of the vote linked with this inquiry
     * @param resultTypes        list of the entities that are searched for
     * @return the id of the inquiry
     */
    public int storeAndRelease(Query query, int userid, String inquiryName, String inquiryDescription, int exposeId, int voteId, List<String> resultTypes) {
        return storeAndRelease(query, userid, inquiryName, inquiryDescription, exposeId, voteId, resultTypes, false);
    }

    /**
     * Store an inquiry and release it (or wait for ccp office authorization first)
     *
     * @param query              the query criteria of the inquiry
     * @param userid             the id of the user that releases the inquiry
     * @param inquiryName        the label of the inquiry
     * @param inquiryDescription the description of the inquiry
     * @param exposeId           the id of the expose linked with this inquiry
     * @param voteId             the id of the vote linked with this inquiry
     * @param resultTypes        list of the entities that are searched for
     * @param bypassExamination  if true, no check by the ccp office is required
     * @return the id of the inquiry
     */
    public int storeAndRelease(Query query, int userid, String inquiryName, String inquiryDescription, int exposeId, int voteId, List<String> resultTypes, boolean bypassExamination) {
        int inquiryId = store(query, userid, inquiryName, inquiryDescription, exposeId, voteId, resultTypes);
        Inquiry inquiry = InquiryUtil.fetchInquiryById(inquiryId);
        release(inquiry, bypassExamination);
        return inquiryId;
    }

    /**
     * Store a new inquiry draft
     *
     * @param query              the query criteria of the inquiry
     * @param userid             the id of the user that releases the inquiry
     * @param inquiryName        the label of the inquiry
     * @param inquiryDescription the description of the inquiry
     * @param exposeId           the id of the expose linked with this inquiry
     * @param voteId             the id of the vote linked with this inquiry
     * @param resultTypes        list of the entities that are searched for
     * @return the id of the inquiry draft
     */
    public int store(Query query, int userid, String inquiryName, String inquiryDescription, int exposeId, int voteId, List<String> resultTypes) {
        int returnValue = 0;
        UserDao userDao;
        User user;
        Inquiry inquiry;

        String projectName = ProjectInfo.INSTANCE.getProjectName();
        boolean isDktk = projectName.equalsIgnoreCase("dktk");

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            userDao = new UserDao(configuration);
            user = userDao.fetchOneById(userid);
            if (user == null) {
                return 0;
            }

            inquiry = new Inquiry();
            inquiry.setAuthorId(user.getId());
            inquiry.setRevision(1);
            inquiry.setLabel(inquiryName);
            inquiry.setDescription(inquiryDescription);

            // If the query is null, create an empty query and fill it with the default AND
            if (query == null) {
                query = new Query();
                Where where = new Where();
                And and = new And();
                where.getAndOrEqOrLike().add(and);
                query.setWhere(where);
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(query, stringWriter);

            inquiry.setCriteria(stringWriter.toString());
            inquiry.setStatus(InquiryStatus.IS_DRAFT);


            String joinedResultTypes = "";
            if (isDktk) {
                Joiner joiner = Joiner.on(",");
                joinedResultTypes = joiner.join(resultTypes);
            }
            inquiry.setResultType(joinedResultTypes);

            Record record = dslContext
                    .insertInto(Tables.INQUIRY, Tables.INQUIRY.AUTHOR_ID, Tables.INQUIRY.CRITERIA, Tables.INQUIRY.STATUS,
                            Tables.INQUIRY.REVISION, Tables.INQUIRY.LABEL, Tables.INQUIRY.DESCRIPTION, Tables.INQUIRY.RESULT_TYPE,
                            Tables.INQUIRY.EXPIRES)
                    .values(inquiry.getAuthorId(), inquiry.getCriteria(), inquiry.getStatus(), inquiry.getRevision(),
                            inquiry.getLabel(), inquiry.getDescription(), inquiry.getResultType(), null)
                    .returning(Tables.INQUIRY.ID, Tables.INQUIRY.CREATED)
                    .fetchOne();

            inquiry.setCreated(record.getValue(Tables.INQUIRY.CREATED));
            inquiry.setId(record.getValue(Tables.INQUIRY.ID));

            if (exposeId > 0) {
                Document expose = DocumentUtil.getDocumentById(exposeId);
                // TODO: this threw an NPE
                expose.setInquiryId(inquiry.getId());
                DocumentUtil.updateDocument(expose);
            }

            if (voteId > 0) {
                Document vote = DocumentUtil.getDocumentById(voteId);
                vote.setInquiryId(inquiry.getId());
                DocumentUtil.updateDocument(vote);
            }

            returnValue = inquiry.getId();
        } catch (JAXBException e1) {
            e1.printStackTrace();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return returnValue;
    }


    /**
     * Release an inquiry and spawn a new project if needed
     *
     * @param inquiry           the inquiry (must be in draft status)
     * @param bypassExamination if the inquiry shall only be sent to the own site, no examination is necessary
     */
    public void release(Inquiry inquiry, boolean bypassExamination) {
        if (inquiry == null || !inquiry.getStatus().equals(InquiryStatus.IS_DRAFT)) {
            logger.debug("Tried to release an inquiry that is not a draft. Skipping.");
            return;
        }
        InquiryDao inquiryDao;


        String projectName = ProjectInfo.INSTANCE.getProjectName();
        boolean isDktk = projectName.equalsIgnoreCase("dktk");

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            boolean createProject = (isDktk && !bypassExamination);

            inquiry.setStatus(InquiryStatus.IS_RELEASED);
            java.sql.Date expiryDate = new java.sql.Date(SamplyShareUtils.getCurrentDate().getTime() + InquiryUtil.INQUIRY_TTL);
            inquiry.setExpires(expiryDate);
            inquiry.setCreated(SamplyShareUtils.getCurrentSqlTimestamp());

            if (inquiry.getRevision() < 1) {
                inquiry.setRevision(1);
            }

            if (createProject) {
                // Use the inquiry name as a preliminary project name. Has to be changed later when it becomes a project.
                Record record = dslContext
                        .insertInto(Tables.PROJECT, Tables.PROJECT.PROJECTLEADER_ID, Tables.PROJECT.STATUS, Tables.PROJECT.NAME)
                        .values(inquiry.getAuthorId(), ProjectStatus.PS_NEW, inquiry.getLabel())
                        .returning(Tables.PROJECT.ID, Tables.PROJECT.APPLICATION_NUMBER).fetchOne();

                int projectId = record.getValue(Tables.PROJECT.ID);
                inquiry.setProjectId(projectId);

                inquiryDao = new InquiryDao(configuration);
                inquiryDao.update(inquiry);
                int appNr = record.getValue(Tables.PROJECT.APPLICATION_NUMBER);

                int inquiryId = inquiry.getId();
                Integer exposeId = DocumentUtil.getExposeIdByInquiryId(inquiryId);

                if (appNr > 0 && exposeId > 0) {
                    try {
                        PdfUtils.injectApplicationNumber(exposeId, appNr, DocumentUtil.getDocumentOutputStreamById(exposeId));
                    } catch (IOException | DocumentException e) {
                        e.printStackTrace();
                    }
                }
                DocumentUtil.setProjectIdForDocumentByInquiryId(inquiryId, projectId);

            } else {
                inquiryDao = new InquiryDao(configuration);
                inquiryDao.update(inquiry);
            }

        } catch (SQLException e) {
            logger.warn("SQL Exception when trying to spawn project.");
        }
    }

    /**
     * Spawn a project for an edited inquiry. Or change the status if needed.
     *
     * @param inquiry     the inquiry
     * @param resultTypes a list of expected result types from the inquiry
     * @param userid      the id of the user that created the inquiry
     */
    public void spawnProjectIfNeeded(Inquiry inquiry, List<String> resultTypes, int userid) {
        UserDao userDao;
        User user;
        InquiryDao inquiryDao;


        String projectName = ProjectInfo.INSTANCE.getProjectName();
        boolean isDktk = projectName.equalsIgnoreCase("dktk");

        if (!isDktk) {
            logger.debug("Not dktk...no project will be spawned");
            return;
        } else if (inquiry.getProjectId() != null) {
            logger.debug("Project is already spawned...change status");
            Project project = ProjectUtil.fetchProjectById(inquiry.getProjectId());
            project.setStatus(ProjectStatus.PS_OPEN_MOREINFO_PROVIDED);
            project.setSeen(false);
            ProjectUtil.updateProject(project);

            Action action = new Action();
            action.setProjectId(project.getId());
            action.setMessage(Messages.getString(ActionType.AT_INQUIRY_EDITED.toString()));
            action.setType(ActionType.AT_PROJECT_CALLBACK_RECEIVED);
            action.setTime(SamplyShareUtils.getCurrentTime());
            action.setUserId(userid);
            ActionUtil.insertAction(action);
            return;
        }

        logger.debug("Spawning project");

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            userDao = new UserDao(configuration);
            user = userDao.fetchOneById(userid);
            if (user == null) {
                return;
            }

            inquiry.setStatus(InquiryStatus.IS_RELEASED);

            String joinedResultTypes = "";
            try {
                Joiner joiner = Joiner.on(",");
                joinedResultTypes = joiner.join(resultTypes);
            } catch (Exception e) {
                logger.warn("couldn't join result types: " + e);

            }
            inquiry.setResultType(joinedResultTypes);

            // Use the inquiry name as a preliminary project name. Has to be
            // changed later when it becomes a project.
            Record record = dslContext
                    .insertInto(Tables.PROJECT, Tables.PROJECT.PROJECTLEADER_ID, Tables.PROJECT.STATUS,
                            Tables.PROJECT.NAME)
                    .values(inquiry.getAuthorId(), ProjectStatus.PS_NEW, inquiry.getLabel())
                    .returning(Tables.PROJECT.ID, Tables.PROJECT.APPLICATION_NUMBER).fetchOne();

            inquiryDao = new InquiryDao(configuration);
            // Reload the inquiry to fill the expires field
            int projectId = record.getValue(Tables.PROJECT.ID);
            inquiry.setProjectId(projectId);
            inquiryDao.update(inquiry);
            int appNr = record.getValue(Tables.PROJECT.APPLICATION_NUMBER);
            int inquiryId = inquiry.getId();
            int exposeId = DocumentUtil.getExposeIdByInquiryId(inquiryId);
            if (appNr > 0 && exposeId > 0) {
                try {
                    PdfUtils.injectApplicationNumber(exposeId, appNr, DocumentUtil.getDocumentOutputStreamById(exposeId));
                } catch (IOException e) {
                    logger.debug("IOException caught: " + e);
                } catch (DocumentException e) {
                    logger.debug("DocumentException caught: " + e);
                }
            }
            DocumentUtil.setProjectIdForDocumentByInquiryId(inquiryId, projectId);
        } catch (SQLException e) {
            logger.debug("Sql exception caught: " + e);
        }
    }

    /**
     * Creates a new tentative inquiry.
     * <p>
     * Tentative inquiries are those that are submitted by the central search tool.
     * They will be purged after a short period of time if no action by the user is taken.
     *
     * @param query  the query criteria
     * @param userid the id of the user that cretaed the inquiry
     * @return the id of the newly created tentative inquiry
     */
    public int createTentative(Query query, int userid) {
        int returnValue = 0;
        UserDao userDao;
        User user;
        Inquiry inquiry;

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            userDao = new UserDao(configuration);
            user = userDao.fetchOneById(userid);
            if (user == null) {
                return 0;
            }

            inquiry = new Inquiry();
            inquiry.setAuthorId(user.getId());

            // Revision 0 = tentative
            inquiry.setRevision(0);

            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(query, stringWriter);

            inquiry.setCriteria(stringWriter.toString());
            inquiry.setStatus(InquiryStatus.IS_DRAFT);

            Record record = dslContext
                    .insertInto(Tables.INQUIRY, Tables.INQUIRY.AUTHOR_ID, Tables.INQUIRY.CRITERIA, Tables.INQUIRY.STATUS,
                            Tables.INQUIRY.REVISION)
                    .values(inquiry.getAuthorId(), inquiry.getCriteria(), inquiry.getStatus(), inquiry.getRevision())
                    .returning(Tables.INQUIRY.ID, Tables.INQUIRY.CREATED).fetchOne();

            returnValue = record.getValue(Tables.INQUIRY.ID);
        } catch (JAXBException e1) {
            e1.printStackTrace();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * Link a document with a project and/or inquiry
     *
     * @param projectId        the id of the project to link the document with, if any
     * @param inquiryId        the id of the inquiry to link the document with
     * @param userId           the id of the user that uploaded the document
     * @param documentData     the document itself
     * @param documentFilename the name of the document
     * @param documentFiletype the filetype of the document
     * @param documentType     the type of the document (expose, vote, report...)
     * @return the id of the newly added document
     */
    public int addDocument(Integer projectId, Integer inquiryId, int userId, File documentData, String documentFilename, String documentFiletype, DocumentType documentType) {
        int returnValue = 0;

        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            if (documentData != null) {
                Path path = documentData.toPath();
                Record documentRecord = dslContext.insertInto(Tables.DOCUMENT, Tables.DOCUMENT.DATA,
                        Tables.DOCUMENT.FILENAME, Tables.DOCUMENT.FILETYPE, Tables.DOCUMENT.PROJECT_ID,
                        Tables.DOCUMENT.INQUIRY_ID, Tables.DOCUMENT.USER_ID, Tables.DOCUMENT.DOCUMENT_TYPE)
                        .values(Files.readAllBytes(path),
                                documentFilename, documentFiletype, projectId,
                                inquiryId, userId, documentType).returning(Tables.DOCUMENT.ID).fetchOne();

                returnValue = documentRecord.getValue(Tables.DOCUMENT.ID);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    /**
     * Link the inquiry with the sites it will be sent to
     *
     * @param inquiryId the id of the inquiry
     * @param siteIds   a list of the ids of the sites to send the inquiry to
     * @return true on success, false on error
     */
    public boolean setSitesForInquiry(int inquiryId, List<String> siteIds) {
        boolean ret = true;

        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);

            // Clear all sites first. In case this is a modification by the ccp office.

            dslContext.deleteFrom(Tables.INQUIRY_SITE)
                    .where(Tables.INQUIRY_SITE.INQUIRY_ID.equal(inquiryId))
                    .execute();

            for (String site : siteIds) {
                dslContext.insertInto(Tables.INQUIRY_SITE, Tables.INQUIRY_SITE.INQUIRY_ID, Tables.INQUIRY_SITE.SITE_ID)
                        .values(inquiryId, Integer.parseInt(site)).execute();
            }

        } catch (SQLException e) {
            logger.error("Error adding sites for distribution for inquiry " + inquiryId);
            ret = false;
        }
        return ret;
    }

    /**
     * Update an inquiry.
     *
     * @param idString       the id of the inquiry to update in string format
     * @param query          the query criteria
     * @param userid         the id of the user that updates the inquiry
     * @param expose         the expose file
     * @param exposeFilename the expose filename
     * @param exposeFiletype the expose filetype
     * @return the response code of the update action
     */
    public Response.Status update(String idString, Query query, int userid, File expose, String exposeFilename, String exposeFiletype) {
        Response.Status responseStatus;
        Inquiry inquiry;
        InquiryDao inquiryDao;
        int id;

        try {
            id = Integer.parseInt(idString);
        } catch (NumberFormatException e2) {
            e2.printStackTrace();
            return Response.Status.BAD_REQUEST;
        }

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);

            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.findById(id);

            if (inquiry == null) {
                return Response.Status.NOT_FOUND;
            }

            if (query != null) {
                JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
                StringWriter stringWriter = new StringWriter();
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                marshaller.marshal(query, stringWriter);

                inquiry.setCriteria(stringWriter.toString());
            }

            inquiry.setRevision(inquiry.getRevision() + 1);
            inquiryDao.update(inquiry);
            responseStatus = Response.Status.OK;
        } catch (JAXBException | SQLException e1) {
            e1.printStackTrace();
            responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
        }
        return responseStatus;
    }

    /**
     * List all (non-tentative) inquiries.
     * <p>
     * Tentative inquiries are identified by a revision nr of 0.
     *
     * @param bankId the id of the bank that wants to list the inquiries
     * @return the serialized list of inquiry ids
     */
    protected String list(int bankId) {
        StringBuilder returnValue = new StringBuilder();
        List<Inquiry> inquiries;

        try (Connection connection = ResourceManager.getConnection()) {
            BankSite bankSite = BankSiteUtil.fetchBankSiteByBankId(bankId);
            Site site = SiteUtil.fetchSiteById(bankSite.getSiteId());
            Integer siteIdForBank = site.getId();

            if (siteIdForBank == null || siteIdForBank < 1) {
                logger.warn("Bank " + bankId + " is not associated with a site. Not providing any inquiries.");
                return "<Inquiries />";
            }

            if (!bankSite.getApproved()) {
                logger.warn("Bank " + bankId + " is associated with a site, but that association has not been approved. Not providing any inquiries.");
                return "<Inquiries />";
            }

            inquiries = InquiryUtil.fetchInquiriesForSite(siteIdForBank);

            returnValue.append("<Inquiries>");
            for (Inquiry inquiry : inquiries) {
                if ((inquiry.getRevision() < 1)) {
                    continue;
                }

                returnValue.append("<Inquiry>");
                returnValue.append("<Id>").append(inquiry.getId()).append("</Id>");
                if (inquiry.getRevision() != null) {
                    returnValue.append("<Revision>").append(inquiry.getRevision()).append("</Revision>");
                } else {
                    returnValue.append("<Revision>1</Revision>");
                }
                returnValue.append("</Inquiry>");
            }
            returnValue.append("</Inquiries>");

        } catch (SQLException e) {
            e.printStackTrace();
            returnValue.replace(0, returnValue.length(), "error");
        } catch (NullPointerException npe) {
            logger.warn("Nullpointer exception caught while trying to list inquiries. This might be caused by a missing connection between bank and site. Check the DB. Returning empty list for now.");
            return "<Inquiries />";
        }

        return returnValue.toString();
    }

    /**
     * Gets an inquiry.
     *
     * @param inquiryId the id of the inquiry to get
     * @param bankId    the id of the bank that wants to get the inquiry
     * @param uriInfo   the uri info object linked with the request
     * @return the serialized inquiry
     */
    protected String getInquiry(int inquiryId, int bankId, UriInfo uriInfo) {
        return getInquiry(inquiryId, bankId, uriInfo, null);
    }

    // TODO: bankId parameter is currently basically useless - may change with access restrictions

    /**
     * Gets an inquiry.
     *
     * @param inquiryId       the id of the inquiry to get
     * @param bankId          the id of the bank that wants to get the inquiry
     * @param uriInfo         the uri info object linked with the request
     * @param userAgentHeader the user agent header of the requesting client
     * @return the serialized inquiry
     */
    protected String getInquiry(int inquiryId, int bankId, UriInfo uriInfo, String userAgentHeader) {
        StringBuilder returnValue = new StringBuilder();
        Inquiry inquiry;
        InquiryDao inquiryDao;
        User author;
        UserDao userDao;

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);

            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.findById(inquiryId);

            if (inquiry == null) {
                return "notFound";
            }

            userDao = new UserDao(configuration);
            author = userDao.findById(inquiry.getAuthorId());

            if (author == null) {
                return "";
            }

            de.samply.share.model.common.Inquiry inq = new de.samply.share.model.common.Inquiry();

            UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
            URI uri = uriBuilder.build();
            inq.setExposeURL(uri + "searchbroker/exposes/" + inquiryId);
            inq.setId(Integer.toString(inquiryId));
            if (inquiry.getRevision() != null && inquiry.getRevision() > 0) {
                inq.setRevision(Integer.toString(inquiry.getRevision()));
            } else {
                inq.setRevision("1");
            }

            inq.setLabel(inquiry.getLabel());
            inq.setDescription(inquiry.getDescription());

            // Unmarshal the criteria String into a Query Object
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringReader stringReader = new StringReader(inquiry.getCriteria());
            Query query = (Query) unmarshaller.unmarshal(stringReader);

            inq.setQuery(query);

            Contact authorInfo = getContact(author);
            inq.setAuthor(authorInfo);

            // Check if user agent of share client is at least 1.1.2. Otherwise skip result types
            String clientVersion = SamplyShareUtils.getVersionFromUserAgentHeader(userAgentHeader);
            if (userAgentHeader != null && clientVersion != null && SamplyShareUtils.versionCompare(clientVersion, "1.1.2") >= 0) {
                try {
                    inq.getSearchFor().addAll(Splitter.on(',').splitToList(inquiry.getResultType()));
                } catch (NullPointerException npe) {
                    logger.debug("No result type set...omitting in reply");
                }
            }

            StringWriter stringWriter = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

            ObjectFactory objectFactory = new ObjectFactory();

            marshaller.marshal(objectFactory.createInquiry(inq), stringWriter);

            returnValue.append(stringWriter.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            returnValue.replace(0, returnValue.length(), "error");
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return returnValue.toString();
    }

    /**
     * Gets the contact for a given user.
     *
     * @param user the user for which the contact is wanted
     * @return the contact of the user
     */
    private Contact getContact(User user) {
        de.samply.share.broker.model.db.tables.pojos.Contact contactPojo = ContactUtil.getContactForUser(user);
        Contact contact = null;

        if (contactPojo != null) {
            contact = new Contact();
            contact.setEmail(contactPojo.getEmail());
            contact.setFirstname(contactPojo.getFirstname());
            contact.setLastname(contactPojo.getLastname());
            contact.setPhone(contactPojo.getPhone());
            contact.setTitle(contactPojo.getTitle());
            contact.setOrganization(contactPojo.getOrganizationname());
        }

        return contact;
    }

    // TODO: bankId parameter is currently basically useless - may change with access restrictions

    /**
     * Gets the query for an inquiry.
     *
     * @param inquiryId the id of the inquiry
     * @param bankId    the id of the requesting bank
     * @param uriInfo   the uri info object linked with the request
     * @return the serialized query
     */
    protected String getQuery(int inquiryId, int bankId, UriInfo uriInfo) {
        StringBuilder returnValue = new StringBuilder();
        Inquiry inquiry;
        InquiryDao inquiryDao;

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);

            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.findById(inquiryId);

            if (inquiry == null) {
                return "notFound";
            }

            returnValue.append(inquiry.getCriteria());
        } catch (SQLException e) {
            e.printStackTrace();
            returnValue.replace(0, returnValue.length(), "error");
        }
        return returnValue.toString();
    }

    /**
     * Gets the ViewFields for an inquiry.
     *
     * @param inquiryId the id of the inquiry
     * @param bankId    the id of the requesting bank
     * @param uriInfo   the uri info object linked with the request
     * @return the viewfields
     */
    protected String getViewFields(int inquiryId, int bankId, UriInfo uriInfo) {
        StringBuilder returnValue = new StringBuilder();
        Inquiry inquiry;
        InquiryDao inquiryDao;

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);

            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.findById(inquiryId);

            if (inquiry == null) {
                return "notFound";
            }
            if (inquiry.getViewfields() == null) {
                return null;
            }
            returnValue.append(inquiry.getViewfields());
        } catch (SQLException e) {
            e.printStackTrace();
            returnValue.replace(0, returnValue.length(), "error");
        }
        return returnValue.toString();
    }

    /**
     * Gets the contact for a given inquiry id.
     *
     * @param inquiryId the id of the inquiry for which the author will be returned
     * @return the contact of the author
     */
    protected String getContact(int inquiryId) throws JAXBException {
        de.samply.share.broker.model.db.tables.pojos.Contact contactPojo;
        Contact contact = new Contact();
        ContactDao contactDao;
        Inquiry inquiry;
        InquiryDao inquiryDao;
        User author;
        UserDao userDao;

        String ret = "";

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);

            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.findById(inquiryId);

            if (inquiry == null) {
                logger.warn("Inquiry for requested contact not found. Inquiry id " + inquiryId + ". Returning empty contact...");
                contact.setEmail("-");
                contact.setFirstname("-");
                contact.setLastname("-");
                contact.setOrganization("-");
                contact.setPhone("-");
                contact.setTitle("-");
            } else {
                userDao = new UserDao(configuration);
                author = userDao.findById(inquiry.getAuthorId());

                contactDao = new ContactDao(configuration);
                contactPojo = contactDao.fetchOneById(author.getContactId());

                contact.setEmail(contactPojo.getEmail());
                contact.setFirstname(contactPojo.getFirstname());
                contact.setLastname(contactPojo.getLastname());
                contact.setOrganization(contactPojo.getOrganizationname());
                contact.setPhone(contactPojo.getPhone());
                contact.setTitle(contactPojo.getTitle());
            }

            JAXBContext jaxbContext = JAXBContext.newInstance(Contact.class);
            ObjectFactory objectFactory = new ObjectFactory();
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(objectFactory.createContact(contact), stringWriter);

            ret = stringWriter.toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Gets the description for a given inquiry id.
     *
     * @param inquiryId the id of the inquiry for which the description will be returned
     * @return the description
     */
    protected String getInfo(int inquiryId) throws JAXBException {
        Inquiry inquiry;
        InquiryDao inquiryDao;

        String ret = "";

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);

            inquiryDao = new InquiryDao(configuration);
            inquiry = inquiryDao.findById(inquiryId);

            Info info = new Info();

            if (inquiry == null) {
                logger.warn("Inquiry for requested info not found. Inquiry id " + inquiryId + ". Returning empty info...");

                info.setDescription("-");
                info.setLabel("-");
                info.setRevision("0");
            } else {
                info.setDescription(inquiry.getDescription());
                info.setLabel(inquiry.getLabel());
                info.setRevision(Integer.toString(inquiry.getRevision()));
            }


            JAXBContext jaxbContext = JAXBContext.newInstance(Info.class);
            ObjectFactory objectFactory = new ObjectFactory();
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            marshaller.marshal(objectFactory.createInfo(info), stringWriter);

            ret = stringWriter.toString();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Save a reply to a given inquiry.
     *
     * @param inquiryId the id of the inquiry the reply is sent to
     * @param bankId    the id of the bank that sent the reply
     * @param content   the content of the reply
     * @return true, if successful
     */
    protected boolean saveReply(int inquiryId, int bankId, String content) {
        boolean returnValue = true;
        Reply reply;
        ReplyDao replyDao;
        JSONParser parser = new JSONParser();
        JSONObject json= new JSONObject();
        try {
            json = (JSONObject) parser.parse(content);
            json.put("site", SiteUtil.fetchSiteById(BankSiteUtil.fetchBankSiteByBankId(bankId).getSiteId()).getName());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        content=json.toString();

        try (Connection connection = ResourceManager.getConnection()) {
            Configuration configuration = new DefaultConfiguration().set(connection).set(SQLDialect.POSTGRES);
            DSLContext dslContext = ResourceManager.getDSLContext(connection);
            replyDao = new ReplyDao(configuration);

            Record record = dslContext.select().from(Tables.REPLY).where(Tables.REPLY.BANK_ID.equal(bankId).and(Tables.REPLY.INQUIRY_ID.equal(inquiryId)))
                    .fetchAny();

            if (record == null) {
                reply = new Reply();
                reply.setBankId(bankId);
                reply.setInquiryId(inquiryId);
                reply.setContent(content);
                replyDao.insert(reply);
            } else {
                reply = replyDao.findById(record.getValue(Tables.REPLY.ID));
                reply.setContent(content);
                replyDao.update(reply);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            returnValue = false;
        }

        return returnValue;
    }
}
