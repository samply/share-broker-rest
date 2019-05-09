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

import com.google.gson.Gson;
import de.samply.share.broker.control.SearchController;
import de.samply.share.broker.filter.AccessPermission;
import de.samply.share.broker.filter.AuthenticatedUser;
import de.samply.share.broker.filter.Secured;
import de.samply.share.broker.model.db.tables.daos.InquiryDao;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.utils.ResonseBuilderHelper;
import de.samply.share.broker.utils.Utils;
import de.samply.share.broker.utils.db.*;
import de.samply.share.common.model.dto.SiteInfo;
import de.samply.share.common.utils.Constants;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.SamplyShareUtils;
import org.apache.commons.lang.StringUtils;
import org.jooq.tools.json.JSONArray;
import org.jooq.tools.json.JSONObject;
import org.jooq.tools.json.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * The Class Searchbroker provides the REST resources for samply.share.client (and other similar products) to use the decentral search.
 */
@Path("/searchbroker")
public class Searchbroker {

    private static final String CONFIG_PROPERTY_BROKER_NAME = "broker.name";
    private static final String CONTENT_TYPE_PDF = "application/pdf";

    private final Logger logger = LoggerFactory.getLogger(Searchbroker.class);
    private final ResonseBuilderHelper resonseHelper = new ResonseBuilderHelper(logger);

    private final String SERVER_HEADER_VALUE = Constants.SERVER_HEADER_VALUE_PREFIX + ProjectInfo.INSTANCE.getVersionString();

    private final BankRegistration bankRegistration = new BankRegistration();

    private final InquiryHandler inquiryHandler = new InquiryHandler();

    private static Map<Integer, Integer> tickMap = new HashMap<>();

    private Gson gson = new Gson();

    @Context
    UriInfo uriInfo;

    @Inject
    @AuthenticatedUser
    User authenticatedUser;

    @Secured({AccessPermission.GBA_SEARCHBROKER_USER, AccessPermission.DKTK_SEARCHBROKER_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getBiobankID")
    @POST
    public Response getBiobankID(List<String> biobankNameList) {
        try {
            List<Integer> biobankID = new ArrayList<>();
            for (String biobankName : biobankNameList) {
                Site site = SiteUtil.fetchSiteByNameIgnoreCase(biobankName);
                biobankID.add(site.getId());
            }
            return Response.ok(gson.toJson(biobankID)).build();
        } catch (Exception e) {
            return resonseHelper.createInternalServerErrorResponse();
        }
    }

    @Secured({AccessPermission.GBA_SEARCHBROKER_USER, AccessPermission.DKTK_SEARCHBROKER_ADMIN})
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getInquiry")
    @POST
    public Response getInquiry(int id) {
        try {
            Inquiry inquiry = InquiryUtil.fetchInquiryById(id);
            if (inquiry == null) {
                return resonseHelper.createNotFoundResponse();
            }

            if (inquiry.getAuthorId().equals(authenticatedUser.getId())) {
                return Response.ok(gson.toJson(inquiry)).build();
            }

            return resonseHelper.createUnauthorizedResponse();

        } catch (Exception e) {
            return resonseHelper.createInternalServerErrorResponse();
        }
    }

    @Secured({AccessPermission.GBA_SEARCHBROKER_USER, AccessPermission.DKTK_SEARCHBROKER_ADMIN})
    @Path("/getProject")
    @GET
    public Response getProject() {
        try {
            List<Project> projectList = ProjectUtil.fetchProjectByProjectLeaderId(authenticatedUser.getId());
            JSONArray jsonArray = new JSONArray();
            JSONObject userProjectsJson = new JSONObject();
            JSONParser parser = new JSONParser();
            for (Project project : projectList) {
                JSONObject tmpJsonObj = new JSONObject();
                JSONObject projectJson = (JSONObject) parser.parse(gson.toJson(project));
                JSONArray inquiryListJson = new JSONArray(InquiryUtil.fetchInquiryByProjectId(project.getId()));
                tmpJsonObj.put("project", projectJson);
                tmpJsonObj.put("inquiry", inquiryListJson);
                jsonArray.add(tmpJsonObj);
            }
            userProjectsJson.put("projects", jsonArray);
            return Response.ok(userProjectsJson).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage()).build();
        }
    }


    @Secured({AccessPermission.GBA_SEARCHBROKER_USER, AccessPermission.DKTK_SEARCHBROKER_ADMIN})
    @Path("/addProject")
    @POST
    public Response addProject(String inquiryJson) {
        int projectId;
        try {
            Inquiry inquiryNew = gson.fromJson(inquiryJson, Inquiry.class);
            Inquiry inquiryOld = InquiryUtil.fetchInquiryById(inquiryNew.getId());
            if (inquiryOld == null) {
                return resonseHelper.createBadRequestResponse();
            } else if (!inquiryOld.getAuthorId().equals(authenticatedUser.getId()) || !authenticatedUser.getId().equals(inquiryNew.getAuthorId())) {
                return resonseHelper.createUnauthorizedResponse();
            }
            projectId = ProjectUtil.addProject(inquiryNew);
        } catch (SQLException e) {
            e.printStackTrace();
            return resonseHelper.createInternalServerErrorResponse();
        }
        return Response.ok().header("id", projectId).build();
    }

    @Secured({AccessPermission.GBA_SEARCHBROKER_USER, AccessPermission.DKTK_SEARCHBROKER_ADMIN})
    @Path("/addInquiryToProject")
    @POST
    public Response addInquiryToProject(@QueryParam("projectId") int projectId, @QueryParam("projectId") int inquiryId) {
        Inquiry inquiry = InquiryUtil.fetchInquiryById(inquiryId);
        Project project = ProjectUtil.fetchProjectById(projectId);
        if (inquiry.getAuthorId().equals(project.getProjectleaderId()) && inquiry.getAuthorId().equals(authenticatedUser.getId())) {
            inquiry.setProjectId(projectId);
            InquiryDao inquiryDao = new InquiryDao();
            inquiryDao.update(inquiry);
            return resonseHelper.createOkResponse();
        }
        return resonseHelper.createBadRequestResponse();
    }


    @Secured({AccessPermission.GBA_SEARCHBROKER_USER, AccessPermission.DKTK_SEARCHBROKER_ADMIN})
    @Path("/checkProject")
    @GET
    public Response checkProject(@QueryParam("queryId") int queryId) {
        Project project = ProjectUtil.fetchProjectByInquiryId(queryId);

        if (project == null) {
            return Response.ok().header("id", 0).build();
        }

        if (project.getProjectleaderId().equals(authenticatedUser.getId())) {
            return Response.ok().header("id", project.getId()).build();
        }

        return resonseHelper.createUnauthorizedResponse();
    }

    /**
     * Gets the name of the searchbroker as given in the config file.
     *
     * @return the name
     */
    @Path("/name")
    @GET
    public Response getName() {
        return Response.ok(ProjectInfo.INSTANCE.getConfig().getProperty(CONFIG_PROPERTY_BROKER_NAME)).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    /**
     * Get query from UI
     *
     * @param xml the query
     * @return 200 or 500 code
     */
    @POST
    @Path("/sendQuery")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Response sendQuery(String xml) {
        logger.info("sendQuery called");

        int id;
        try {
            id = SearchController.releaseQuery(xml, authenticatedUser);
        } catch (JAXBException e) {
            return resonseHelper.createInternalServerErrorResponse("sendQuery id internal error: " + e);
        }

        logger.info("sendQuery with id is sent");
        return Response.accepted().header("id", id).build();
    }

    /**
     * Get result of the bridgeheads
     *
     * @param id the id of the query
     * @return the result as JSON String
     */
    @GET
    @Path("/getReply")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getReply(@QueryParam("id") int id) {
        String reply = SearchController.getReplysFromQuery(id);
        return Response.ok().header("reply", reply).build();
    }

    /**
     * Get the count of the sites
     *
     * @param id Inquiry ID
     * @return the count of the sites
     */
    @GET
    @Path("/getSize")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getSize(@QueryParam("id") int id) {
        int size = InquirySiteUtil.fetchInquirySitesForInquiryId(id).size();
        return Response.ok().header("size", size).build();
    }


    /**
     * Handle registration and activation of a new bank.
     *
     * @param email          the email of the new bank
     * @param authCodeHeader the auth code header
     * @return <CODE>201</CODE> if a new bank was registered
     * <CODE>400</CODE> if an invalid email was entered
     * <CODE>401</CODE> if an invalid request token was provided
     */
    @Path("/banks/{email}")
    @PUT
    public Response handlePutBank(@PathParam("email") String email,
                                  @HeaderParam(HttpHeaders.AUTHORIZATION) String authCodeHeader,
                                  @HeaderParam("Accesstoken") String accessToken) {
        Response response;
        Response.Status responseStatus;
        String tokenId = "";

        String authCode = SamplyShareUtils.getAuthCodeFromHeader(authCodeHeader, Constants.REGISTRATION_HEADER_VALUE);

        if (!SamplyShareUtils.isEmail(email)) {
            logger.warn("Registration attempted with invalid email: " + email);
            responseStatus = Response.Status.BAD_REQUEST;
        } else if (authCode != null && authCode.length() > 0) {
            String locationId = null;
            if (accessToken != null && accessToken.length() > 0) {
                locationId = Utils.getLocationIdFromAccessToken(accessToken);
                logger.debug("location id = " + locationId);
            }
            tokenId = bankRegistration.activate(email, authCode, locationId);
            if ("error".equals(tokenId)) {
                logger.warn("Activation attempted with invalid credentials from: " + email);
                responseStatus = Response.Status.UNAUTHORIZED;
            } else {
                logger.info("Bank activated: " + email);
                responseStatus = Response.Status.CREATED;
            }
        } else {
            responseStatus = bankRegistration.register(email);
        }

        if (responseStatus == Response.Status.CREATED) {
            response = Response.status(responseStatus).entity(tokenId).build();
        } else {
            response = Response.status(responseStatus).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
            if (responseStatus == Response.Status.UNAUTHORIZED) {
                response.getMetadata().add(HttpHeaders.WWW_AUTHENTICATE, Constants.REGISTRATION_AUTH_HEADER_VALUE);
            }
        }
        return response;
    }

    /**
     * Delete a bank from the database.
     *
     * @param email          the email of the bank to delete
     * @param authCodeHeader the auth code header
     * @return <CODE>204</CODE> on success
     * <CODE>401</CODE> if the credentials don't belong to this bank
     * <CODE>404</CODE> if no bank was found for this email address
     */
    @Path("/banks/{email}")
    @DELETE
    public Response handleDeleteBank(@PathParam("email") String email,
                                     @HeaderParam(HttpHeaders.AUTHORIZATION) String authCodeHeader) {
        Response response;
        Response.Status responseStatus;

        String authCode = SamplyShareUtils.getAuthCodeFromHeader(authCodeHeader, "Samply ");
        responseStatus = bankRegistration.delete(email, authCode);
        response = Response.status(responseStatus).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        return response;
    }

    /**
     * Get the registration status of a bank
     *
     * @param email          the email of the bank to check
     * @param authCodeHeader the api key header
     * @return <CODE>200</CODE> if email and authcode are ok
     * <CODE>202</CODE> if registration is started but waiting for confirmation via token
     * <CODE>403</CODE> if email is registered but authcode is wrong
     * <CODE>404</CODE> if email is unknown
     */
    @Path("/banks/{email}/status")
    @GET
    @Produces
    public Response getRegistrationstatus(@PathParam("email") String email,
                                          @HeaderParam(HttpHeaders.AUTHORIZATION) String authCodeHeader) {
        logger.debug("Get Status is called for " + email);
        Response response;
        String authCode = SamplyShareUtils.getAuthCodeFromHeader(authCodeHeader, "Samply ");

        Response.Status responseStatus = bankRegistration.checkStatus(email, authCode);

        String siteString;

        try {
            int bankId = Utils.getBankId(authCodeHeader);
            BankSite bankSite = BankSiteUtil.fetchBankSiteByBankId(bankId);

            Site site = SiteUtil.fetchSiteById(bankSite.getSiteId());
            SiteInfo siteInfo = Utils.siteToSiteInfo(site);
            siteInfo.setApproved(bankSite.getApproved());

            Gson gson = new Gson();
            siteString = gson.toJson(siteInfo);
            response = Response.status(responseStatus).entity(siteString).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } catch (Exception e) {
            logger.warn("Could not get site...no need to worry though." + e);
            response = Response.status(responseStatus).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        }


        return response;
    }

    /**
     * Gets the list of inquiries.
     * <p>
     * Only get inquiries that are linked with the requesting bank (site) and are not expired, yet
     * <p>
     * Also send client version information to icinga on every 12th call (~each minute)
     *
     * @param authorizationHeader the authorization header
     * @param userAgent           user agent of the requesting client
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @return <CODE>200</CODE> and a serialized list of inquiry ids/revisions on success
     * <CODE>401</CODE> if no bank is found for the supplied api key
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInquiries(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                 @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
                                 @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader) {

        if (isUnauthorized(authorizationHeader)) {
            return resonseHelper.createUnauthorizedResponse("Unauthorized attempt to retrieve inquiry list");
        }

        int bankId = Utils.getBankId(authorizationHeader);
        sendVersionReport(userAgent, bankId);

        String inquiryList = Utils.fixNamespaces(inquiryHandler.list(bankId), xmlNamespaceHeader);

        if (StringUtils.isEmpty(inquiryList) || inquiryList.equalsIgnoreCase("error")) {
            return resonseHelper.createInternalServerErrorResponse("There was an error while retrieving the list of inquiries");
        }
        return Response.ok().entity(inquiryList).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    private void sendVersionReport(@HeaderParam(HttpHeaders.USER_AGENT) String userAgent, int bankId) {
        if (userAgent != null) {
            logger.info("GET /inquiries called from: " + Utils.userAgentAndBankToJson(userAgent, bankId));

            int bankTick = tickMap.getOrDefault(bankId, 1);

            // Only report each 12th call to icinga (~1 minute interval)
            if (bankTick % 12 == 0) {
                Utils.sendVersionReportsToIcinga(userAgent, bankId);
            }

            tickMap.put(bankId, bankTick + 1);
        }
    }

    /**
     * Gets the inquiry with the given id.
     *
     * @param authorizationHeader the api key of the client
     * @param userAgentHeader     the user agent of the client
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryIdString     the requested inquiry id
     * @return <CODE>200</CODE> and the serialized inquiry on success
     * <CODE>400</CODE> if the inquiry id can't be parsed
     * <CODE>401</CODE> if the provided credentials do not allow to read this inquiry
     * <CODE>404</CODE> if no inquiry with this id was found
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries/{inquiryid}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInquiry(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                               @HeaderParam(HttpHeaders.USER_AGENT) String userAgentHeader,
                               @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader,
                               @PathParam("inquiryid") String inquiryIdString) {

        if (isUnauthorized(authorizationHeader)) {
            return resonseHelper.createUnauthorizedResponse("Unauthorized attempt to retrieve an inquiry");
        }

        if (StringUtils.isEmpty(inquiryIdString)) {
            return resonseHelper.createNotFoundResponse();
        }

        int inquiryId;

        try {
            inquiryId = Integer.parseInt(inquiryIdString);
        } catch (NumberFormatException e) {
            return resonseHelper.createBadRequestResponse("Could not parse inquiry id: " + inquiryIdString);
        }

        String ret = inquiryHandler.getInquiry(inquiryId, uriInfo, userAgentHeader);

        Response response = buildResponse(xmlNamespaceHeader, inquiryId, ret);
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            try {
                List<InquirySite> inquirySites = InquirySiteUtil.fetchInquirySitesForInquiryId(inquiryId);
                int bankId = Utils.getBankId(authorizationHeader);
                for (InquirySite is : inquirySites) {
                    if (is.getSiteId().equals(BankUtil.getSiteIdForBankId(bankId))) {
                        is.setRetrievedAt(SamplyShareUtils.getCurrentSqlTimestamp());
                        InquirySiteUtil.updateInquirySite(is);
                    }
                }
            } catch (Exception e) {
                // Just catch everything for now
                logger.warn("Could not update InquirySite");
            }
        }

        return response;
    }

    /**
     * Gets the query part of the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryIdString     the requested inquiry id
     * @return <CODE>200</CODE> and the serialized query of the inquiry on success
     * <CODE>400</CODE> if the inquiry id can't be parsed
     * <CODE>401</CODE> if the provided credentials do not allow to read this inquiry
     * <CODE>404</CODE> if no inquiry with this id was found
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries/{inquiryid}/query")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getQuery(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                             @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader,
                             @PathParam("inquiryid") String inquiryIdString) {

        return createResponseForGetInquiryDetail(
                this::createResponseForQuery,
                authorizationHeader,
                xmlNamespaceHeader,
                inquiryIdString,
                "Unauthorized attempt to retrieve an inquiry");
    }

    private Response createResponseForQuery(String xmlNamespaceHeader, int inquiryId) {
        String ret = inquiryHandler.getQuery(inquiryId);
        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }

    /**
     * Gets the ViewFields part of the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryIdString     the requested inquiry id
     * @return <CODE>200</CODE> and the serialized viewfields for the inquiry on success
     * <CODE>204</CODE> if no viewfields are specified for this inquiry
     * <CODE>400</CODE> if the inquiry id can't be parsed
     * <CODE>401</CODE> if the provided credentials do not allow to read this inquiry
     * <CODE>404</CODE> if no inquiry with this id was found
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries/{inquiryid}/viewfields")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getViewFields(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                  @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader,
                                  @PathParam("inquiryid") String inquiryIdString) {

        return createResponseForGetInquiryDetail(
                this::createResponseForViewFields,
                authorizationHeader,
                xmlNamespaceHeader,
                inquiryIdString,
                "Unauthorized attempt to retrieve an inquiry");
    }

    private Response createResponseForViewFields(@HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader, int inquiryId) {
        String ret = inquiryHandler.getViewFields(inquiryId);
        if (StringUtils.isEmpty(ret)) {
            logger.debug("No ViewFields were set for inquiry with id " + inquiryId);
            return resonseHelper.createOkResponse();
        }
        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }

    /**
     * Gets the contact for the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryIdString     the requested inquiry id
     * @return <CODE>200</CODE> and the serialized contact information for the inquiry on success
     * <CODE>400</CODE> if the inquiry id can't be parsed
     * <CODE>401</CODE> if the provided credentials do not allow to read this inquiry
     * <CODE>404</CODE> if no inquiry with this id was found
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries/{inquiryid}/contact")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getContact(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                               @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader,
                               @PathParam("inquiryid") String inquiryIdString) {

        return createResponseForGetInquiryDetail(
                this::createResonseForContact,
                authorizationHeader,
                xmlNamespaceHeader,
                inquiryIdString,
                "Unauthorized attempt to retrieve a contact");
    }

    private Response createResonseForContact(String xmlNamespaceHeader, int inquiryId) {
        String ret;
        try {
            ret = inquiryHandler.getContact(inquiryId);
        } catch (JAXBException e) {
            logger.error("Error getting contact for inquiry id " + inquiryId);
            return resonseHelper.createNotFoundResponse();
        }

        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }


    /**
     * Gets the additional description for the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryIdString     the requested inquiry id
     * @return <CODE>200</CODE> and the serialized details for the inquiry on success
     * <CODE>400</CODE> if the inquiry id can't be parsed
     * <CODE>401</CODE> if the provided credentials do not allow to read this inquiry
     * <CODE>404</CODE> if no inquiry with this id was found
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries/{inquiryid}/info")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getInfo(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                            @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader,
                            @PathParam("inquiryid") String inquiryIdString) {

        return createResponseForGetInquiryDetail(
                this::createResponseForInfo,
                authorizationHeader,
                xmlNamespaceHeader,
                inquiryIdString,
                "Unauthorized attempt to retrieve a contact");
    }

    private Response createResponseForInfo(String xmlNamespaceHeader, int inquiryId) {
        String ret;
        try {
            ret = inquiryHandler.getInfo(inquiryId);
        } catch (JAXBException e) {
            logger.error("Error getting info for inquiry id " + inquiryId);
            return resonseHelper.createNotFoundResponse();
        }

        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }

    private Response createResponseForGetInquiryDetail(
            BiFunction<String, Integer, Response> responseBiFunction,
            String authorizationHeader,
            String xmlNamespaceHeader,
            String inquiryIdString,
            String messageUnauthorized) {
        if (isUnauthorized(authorizationHeader)) {
            return resonseHelper.createUnauthorizedResponse(messageUnauthorized);
        }

        if (StringUtils.isEmpty(inquiryIdString)) {
            return resonseHelper.createNotFoundResponse();
        }

        try {
            int inquiryId = Integer.parseInt(inquiryIdString);
            return responseBiFunction.apply(xmlNamespaceHeader, inquiryId);
        } catch (NumberFormatException e) {
            return resonseHelper.createBadRequestResponse("Could not parse inquiry id: " + inquiryIdString);
        }
    }

    /**
     * Check if an expose is present for the given inquiry
     *
     * @param inquiryId the inquiry id to check
     * @return <CODE>200</CODE> if the inquiry has an expose
     * <CODE>400</CODE> if the inquiry id can't be parsed
     * <CODE>404</CODE> if no expose is available for this inquiry
     */
    @Path("/inquiries/{inquiryid}/hasexpose")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response isSynopsisAvailable(@PathParam("inquiryid") String inquiryId) {
        int inqId;

        if (StringUtils.isEmpty(inquiryId)) {
            return resonseHelper.createNotFoundResponse();
        }

        try {
            inqId = Integer.parseInt(inquiryId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return resonseHelper.createBadRequestResponse();
        }

        Document expose = DocumentUtil.fetchExposeByInquiryId(inqId);

        if (expose == null || expose.getData() == null || expose.getData().length < 1) {
            return Response.status(Response.Status.NOT_FOUND).entity("unavailable").header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } else {
            return resonseHelper.createOkResponse();
        }
    }

    /**
     * Store reply for an inquiry by a bank.
     *
     * @param authorizationHeader the authorization header
     * @param inquiryIdString     the inquiry id
     * @param bankEmail           the bank email
     * @param reply               the reply content
     * @return <CODE>200</CODE> on success
     * <CODE>400</CODE> if reply is empty
     * <CODE>401</CODE> if the provided credentials do not allow to access this inquiry
     * <CODE>404</CODE> if no inquiry with this id was found
     * <CODE>500</CODE> on any other error
     */
    @Path("/inquiries/{inquiryid}/replies/{bankemail}")
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response putReply(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                             @PathParam("inquiryid") String inquiryIdString,
                             @PathParam("bankemail") String bankEmail,
                             String reply) {

        int bankId = Utils.getBankId(authorizationHeader, bankEmail);
        int inquiryId = -1;

        if (bankId < 0) {
            return resonseHelper.createUnauthorizedResponse("Unauthorized attempt to answer to an inquiry from " + bankEmail);
        }

        if (reply == null || reply.length() < 1) {
            return resonseHelper.createBadRequestResponse("Rejecting empty reply to inquiry from " + bankEmail);
        }

        if (inquiryIdString != null && inquiryIdString.length() > 0) {
            try {
                inquiryId = Integer.parseInt(inquiryIdString);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return resonseHelper.createBadRequestResponse("Could not parse inquiry id: " + inquiryIdString);
            }
        } else {
            return resonseHelper.createNotFoundResponse("Inquiry with id " + inquiryId + " not found");
        }

        if (!inquiryHandler.saveReply(inquiryId, bankId, reply)) {
            return resonseHelper.createInternalServerErrorResponse("An error occurred while trying to store a reply to inquiry " + inquiryId + " by " + bankEmail);
        }

        logger.info("Stored reply to inquiry " + inquiryId + " from " + bankEmail);
        return Response.ok().header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }


    /**
     * Get the expose file for an inquiry
     *
     * @param authKeyHeader the api key
     * @param inquiryId     the id of the inquiry for which the expose shall be gotten
     * @return <CODE>200</CODE> and the expose on success
     * <CODE>400</CODE> if the inquiry id could not be parsed
     * <CODE>404</CODE> if no inquiry with this id was found
     */
    @Path("/exposes/{inquiryid}")
    @GET
    @Produces(CONTENT_TYPE_PDF)
    public Response getSynopsis(@HeaderParam(HttpHeaders.AUTHORIZATION) String authKeyHeader,
                                @PathParam("inquiryid") String inquiryId) {
        int inqId;

        if (inquiryId != null && inquiryId.length() > 0) {
            try {
                inqId = Integer.parseInt(inquiryId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return resonseHelper.createBadRequestResponse();
            }
        } else {
            return resonseHelper.createNotFoundResponse();
        }

        Document expose = DocumentUtil.fetchExposeByInquiryId(inqId);

        if (expose == null) {
            return Response.status(Response.Status.NOT_FOUND).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } else {
            return Response.ok().header("Content-Disposition", "attachment; filename=" + expose.getFilename())
                    .header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).entity(expose.getData()).build();
        }
    }

    /**
     * Get the expose file for an inquiry (alias)
     *
     * @param authKeyHeader the api key
     * @param inquiryId     the id of the inquiry for which the expose shall be gotten
     * @return <CODE>200</CODE> and the expose on success
     * <CODE>400</CODE> if the inquiry id could not be parsed
     * <CODE>404</CODE> if no inquiry with this id was found
     */
    @Path("/inquiries/{inquiryid}/expose")
    @GET
    @Produces(CONTENT_TYPE_PDF)
    public Response getSynopsisAlias(@HeaderParam(HttpHeaders.AUTHORIZATION) String authKeyHeader,
                                     @PathParam("inquiryid") String inquiryId) {
        return this.getSynopsis(authKeyHeader, inquiryId);
    }

    /**
     * Gets the list of sites available.
     * <p>
     * This does not guarantee that each site has a registered bank.
     *
     * @param authorizationHeader the authorization header
     * @return <CODE>200</CODE> and the list of sites on success
     * <CODE>401</CODE> on authorization error
     * <CODE>500</CODE> on any other error
     */
    @Path("/sites")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Response getSites(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                             @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
                             @Context HttpServletRequest request) {

        if (isUnauthorized(authorizationHeader)) {
            return resonseHelper.createUnauthorizedResponse("Unauthorized attempt to retrieve list of sites from " + request.getRemoteAddr());
        }

        List<Site> sites = SiteUtil.fetchSites();
        String returnValue;

        Gson gson = new Gson();

        try {
            returnValue = gson.toJson(sites);
        } catch (Exception e) {
            return resonseHelper.createInternalServerErrorResponse("Error trying to return site list: " + e);
        }

        return Response.ok().entity(returnValue).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    /**
     * Set the site for a bank.
     *
     * @param authorizationHeader the authorization header
     * @param userAgent           the user agent of the client
     * @param request             the http request
     * @param siteIdString        the id of the site to set
     * @return <CODE>200</CODE> on success
     * <CODE>401</CODE> on authorization error
     * <CODE>404</CODE> if the site or bank could not be found
     */
    @Path("/banks/{email}/site/{siteid}")
    @PUT
    public Response setSite(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                            @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
                            @Context HttpServletRequest request,
                            @PathParam("siteid") String siteIdString) {
        if (isUnauthorized(authorizationHeader)) {
            return resonseHelper.createUnauthorizedResponse("Unauthorized attempt to set a site from " + request.getRemoteAddr());
        }

        int bankId = Utils.getBankId(authorizationHeader);
        try {
            int siteId = Integer.parseInt(siteIdString);
            BankSiteUtil.setSiteIdForBankId(bankId, siteId, false);
        } catch (Exception e) {
            logger.error("Error trying to set site" + e);
            return resonseHelper.createNotFoundResponse();
        }

        return Response.ok().header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    /**
     * Build a Response object depending on the given value
     *
     * @param xmlNamespace desired output namespace (will be converted to lower case)
     * @param inquiryId    id of the inquiry
     * @param ret          the payload
     * @return the Response to send back to the client
     */
    private Response buildResponse(String xmlNamespace, int inquiryId, String ret) {
        if (ret.equalsIgnoreCase("error")) {
            return resonseHelper.createInternalServerErrorResponse("Could not get inquiry");
        } else if (ret.equalsIgnoreCase("notFound")) {
            return resonseHelper.createNotFoundResponse("Inquiry with id " + inquiryId + " not found");
        }

        ret = Utils.fixNamespaces(ret, xmlNamespace);
        return Response.ok().entity(ret).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    private boolean isUnauthorized(String authorizationHeader) {
        int bankId = Utils.getBankId(authorizationHeader);

        return (bankId < 0);
    }

}
