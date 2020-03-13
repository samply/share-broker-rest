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

import com.google.gson.*;
import de.samply.share.broker.control.SearchController;
import de.samply.share.broker.filter.AuthenticatedUser;
import de.samply.share.broker.filter.Secured;
import de.samply.share.broker.model.db.tables.daos.InquiryDao;
import de.samply.share.broker.model.db.tables.pojos.*;
import de.samply.share.broker.statistics.NTokenHandler;
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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class Searchbroker provides the REST resources for samply.share.client (and other similar products) to use the decentral search.
 */
@Path("/searchbroker")
public class Searchbroker {

    private static final String CONFIG_PROPERTY_BROKER_NAME = "broker.name";
    private static final String CONTENT_TYPE_PDF = "application/pdf";

    private final Logger logger = LoggerFactory.getLogger(Searchbroker.class);

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

    @Path("/version")
    @Produces({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @GET
    public Response getVersion() {
        return Response.ok(new Gson().toJson(ProjectInfo.INSTANCE.getVersionString()))
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .build();
    }

    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getDirectoryID")
    @POST
    public Response getDirectoryID(List<String> biobankNameList) {
        try {
            JSONArray biobank = new JSONArray();
            for (String biobankName : biobankNameList) {
                JSONObject jsonObject = new JSONObject();
                Site site = SiteUtil.fetchSiteByNameIgnoreCase(biobankName);
                jsonObject.put("biobankId", site.getBiobankid());
                jsonObject.put("collectionId", site.getCollectionid());
                biobank.add(jsonObject);
            }
            return Response.ok(biobank)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", HttpMethod.POST)
                    .header("Access-Control-Allow-Headers", "origin, Content-Type, Accept, Authorization")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/getDirectoryID")
    @OPTIONS
    public Response getDirectoryID_OPTIONS(List<String> biobankNameList) {
        try {
            return Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", HttpMethod.POST)
                    .header("Access-Control-Allow-Headers", "origin, Content-Type, Accept, Authorization")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getInquiry")
    @POST
    public Response getInquiry(int id) {
        try {
            Inquiry inquiry = InquiryUtil.fetchInquiryById(id);
            if (inquiry == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            if (inquiry.getAuthorId().equals(authenticatedUser.getId())) {
                return Response.ok(gson.toJson(inquiry)).build();
            }

            return Response.status(Response.Status.UNAUTHORIZED).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Secured
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
            //TODO: Response should be 500. Also e.getMessage() should be deleted
            return Response.status(Response.Status.BAD_REQUEST.getStatusCode(), e.getMessage()).build();
        }
    }


    @Secured
    @Path("/addProject")
    @POST
    public Response addProject(String inquiryJson) {
        int projectId;
        try {
            Inquiry inquiryNew = gson.fromJson(inquiryJson, Inquiry.class);
            Inquiry inquiryOld = InquiryUtil.fetchInquiryById(inquiryNew.getId());
            if (inquiryOld == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else if (!inquiryOld.getAuthorId().equals(authenticatedUser.getId()) || !authenticatedUser.getId().equals(inquiryNew.getAuthorId())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            projectId = ProjectUtil.addProject(inquiryNew);
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().header("id", projectId).build();
    }

    @Secured
    @Path("/addInquiryToProject")
    @POST
    public Response addInquiryToProject(@QueryParam("projectId") int projectId, @QueryParam("projectId") int inquiryId) {
        Inquiry inquiry = InquiryUtil.fetchInquiryById(inquiryId);
        Project project = ProjectUtil.fetchProjectById(projectId);
        if (inquiry.getAuthorId().equals(project.getProjectleaderId()) && inquiry.getAuthorId().equals(authenticatedUser.getId())) {
            inquiry.setProjectId(projectId);
            InquiryDao inquiryDao = new InquiryDao();
            inquiryDao.update(inquiry);
            return Response.status(Response.Status.OK).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }


    @Secured
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

        return Response.status(Response.Status.UNAUTHORIZED).build();
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
     * @return 202 or 500 code
     */
    @POST
    @Path("/sendQuery")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Response sendQuery(String xml, @HeaderParam("ntoken") String ntoken) {
        this.logger.info("sendQuery called");

        SearchController.releaseQuery(xml, ntoken, authenticatedUser);

        this.logger.info("sendQuery with ntoken '" + ntoken + "'is sent");
        return Response.accepted()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.POST)
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, ntoken")
                .header("Access-Control-Expose-Headers", "ntoken")
                .header("ntoken", ntoken)
                .build();
    }

    /**
     * Get query from UI
     *
     * @param xml the query
     * @return 200 or 500 code
     */
    @OPTIONS
    @Path("/sendQuery")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Response sendQueryOPTIONS(String xml, @HeaderParam("ntoken") String ntoken) {
        this.logger.info("sendQuery called (OPTIONS)");
        return Response.ok(new Gson().toJson(ProjectInfo.INSTANCE.getVersionString()))
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.POST)
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, ntoken")
                .header("Access-Control-Expose-Headers", "ntoken")
                .build();
    }

    @GET
    @Path("/getQuery")
    @Produces(MediaType.APPLICATION_XML)
    public Response getQuery(@QueryParam("ntoken") @DefaultValue("") String nToken) {
        this.logger.info("getQuery called");
        String query = new NTokenHandler().findLatestQuery(nToken);

        return Response.ok()
                .entity(query)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .header("Access-Control-Allow-Headers", "origin, content-type, accept")
                .build();
    }

    @OPTIONS
    @Path("/getQuery")
    @Produces(MediaType.APPLICATION_XML)
    public Response getQuery_OPTIONS(@QueryParam("ntoken") @DefaultValue("") String nToken) {
        this.logger.info("getQuery called (OPTIONS)");
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .header("Access-Control-Allow-Headers", "origin, content-type, accept")
                .build();
    }

    /**
     * Get result of the bridgeheads
     *
     * @param id the id of the query
     * @return the result as JSON String
     */
    @Secured
    @GET
    @Path("/getReply")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getReply(
            @QueryParam("id") @DefaultValue("-1") int id,
            @QueryParam("ntoken") @DefaultValue("") String nToken) {
        int usedId = id;
        if (id < 0 && !StringUtils.isEmpty(nToken)) {
            usedId = new NTokenHandler().findLatestInquiryId(nToken);
        }

        String reply = SearchController.getReplysFromQuery(usedId);
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .header("Access-Control-Allow-Headers", "origin, content-type, authorization, reply")
                .header("Access-Control-Expose-Headers", "reply")
                .header("reply", reply)
                .build();
    }

    @OPTIONS
    @Path("/getReply")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getReply_OPTIONS(@QueryParam("id") int id, @QueryParam("ntoken") String nToken) {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .header("Access-Control-Allow-Headers", "origin, content-type, authorization, reply")
                .header("Access-Control-Expose-Headers", "reply")
                .build();
    }

    /**
     * Get aggregated result of the bridgeheads (without login)
     *
     * @param id the id of the query
     * @return the result as JSON String
     */
    @GET
    @Path("/getAnonymousReply")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getAnonymousReply(
            @QueryParam("id") @DefaultValue("-1") int id,
            @QueryParam("ntoken") @DefaultValue("") String nToken) {
        int usedId = id;
        if (id < 0 && !StringUtils.isEmpty(nToken)) {
            usedId = new NTokenHandler().findLatestInquiryId(nToken);
        }

        String reply = SearchController.getReplysFromQuery(usedId);

        JsonParser parser = new JsonParser();
        JsonElement tradeElement = parser.parse(reply);
        JsonArray jsonReply = tradeElement.getAsJsonArray();
        for (int i = 0; i < jsonReply.size(); i++) {
            JsonObject jsonObj = jsonReply.get(i).getAsJsonObject();
            jsonObj.remove("site");
            jsonObj.addProperty("site", "anonymous");
        }

        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, reply")
                .header("Access-Control-Expose-Headers", "reply")
                .header("reply", jsonReply.toString())
                .build();
    }

    /**
     * Get the count of the sites
     *
     * @return the count of the sites
     */
    @GET
    @Path("/getSize")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response getSize() {
        long size = SiteUtil.fetchSites().stream().filter(Site::getActive).count();

        return Response.ok()
                .header("Access-Control-Allow-Methods", HttpMethod.GET)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "size")
                .header("Access-Control-Expose-Headers", "size")
                .header("size", size)
                .build();
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
            this.logger.warn("Registration attempted with invalid email: " + email);
            responseStatus = Response.Status.BAD_REQUEST;
        } else if (authCode != null && authCode.length() > 0) {
            String locationId = null;
            if (accessToken != null && accessToken.length() > 0) {
                locationId = Utils.getLocationIdFromAccessToken(accessToken);
                this.logger.debug("location id = " + locationId);
            }
            tokenId = bankRegistration.activate(email, authCode, locationId);
            if ("error".equals(tokenId)) {
                this.logger.warn("Activation attempted with invalid credentials from: " + email);
                responseStatus = Response.Status.UNAUTHORIZED;
            } else {
                this.logger.info("Bank activated: " + email);
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
        this.logger.debug("Get Status is called for " + email);
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
            this.logger.warn("Could not get site...no need to worry though." + e);
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

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve inquiry list");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int bankId = Utils.getBankId(authorizationHeader);
        sendVersionReport(userAgent, bankId);

        String inquiryList = inquiryHandler.list(bankId);

        if (StringUtils.isEmpty(inquiryList) || inquiryList.equalsIgnoreCase("error")) {
            this.logger.warn("There was an error while retrieving the list of inquiries");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
        return Response.ok().entity(inquiryList).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    private void sendVersionReport(String userAgent, int bankId) {
        if (userAgent != null) {
            this.logger.info("GET /inquiries called from: " + Utils.userAgentAndBankToJson(userAgent, bankId));

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
     * @param inquiryId           the requested inquiry id
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
                               @HeaderParam(Constants.HEADER_KEY_QUERY_LANGUAGE) @DefaultValue("QUERY") String queryLanguage,
                               @PathParam("inquiryid") int inquiryId) {

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve an inquiry");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String ret = inquiryHandler.getInquiry(inquiryId, uriInfo, userAgentHeader, queryLanguage);

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
                this.logger.warn("Could not update InquirySite");
            }
        }

        return response;
    }

    /**
     * Gets the query part of the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryId           the requested inquiry id
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
                             @PathParam("inquiryid") int inquiryId) {

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve an inquiry");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String ret = inquiryHandler.getQuery(inquiryId);
        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }

    /**
     * Gets the ViewFields part of the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryId           the requested inquiry id
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
                                  @PathParam("inquiryid") int inquiryId) {

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve an inquiry");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String ret = inquiryHandler.getViewFields(inquiryId);
        if (StringUtils.isEmpty(ret)) {
            this.logger.debug("No ViewFields were set for inquiry with id " + inquiryId);
            return Response.status(Response.Status.OK).build();
        }
        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }

    /**
     * Gets the contact for the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryId           the requested inquiry id
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
                               @PathParam("inquiryid") int inquiryId) {

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve a contact");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String ret;
        try {
            ret = inquiryHandler.getContact(inquiryId);
        } catch (JAXBException e) {
            this.logger.error("Error getting contact for inquiry id " + inquiryId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
    }


    /**
     * Gets the additional description for the inquiry with the given id.
     *
     * @param authorizationHeader the authorization header
     * @param xmlNamespaceHeader  optional header with xml namespace
     * @param inquiryId           the requested inquiry id
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
                            @PathParam("inquiryid") int inquiryId) {

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve a contact");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String ret;
        try {
            ret = inquiryHandler.getInfo(inquiryId);
        } catch (JAXBException e) {
            this.logger.error("Error getting info for inquiry id " + inquiryId);
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return buildResponse(xmlNamespaceHeader, inquiryId, ret);
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
    public Response isSynopsisAvailable(@PathParam("inquiryid") int inquiryId) {
        Document expose = DocumentUtil.fetchExposeByInquiryId(inquiryId);

        if (expose == null || expose.getData() == null || expose.getData().length < 1) {
            return Response.status(Response.Status.NOT_FOUND).entity("unavailable").header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
        } else {
            return Response.status(Response.Status.OK).build();
        }
    }

    /**
     * Store reply for an inquiry by a bank.
     *
     * @param authorizationHeader the authorization header
     * @param inquiryId           the inquiry id
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
                             @PathParam("inquiryid") int inquiryId,
                             @PathParam("bankemail") String bankEmail,
                             String reply) {

        int bankId = Utils.getBankId(authorizationHeader, bankEmail);

        if (bankId < 0) {
            this.logger.warn("Unauthorized attempt to answer to an inquiry from " + bankEmail);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        if (reply == null || reply.length() < 1) {
            this.logger.warn("Rejecting empty reply to inquiry from " + bankEmail);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!inquiryHandler.saveReply(inquiryId, bankId, reply)) {
            this.logger.warn("An error occurred while trying to store a reply to inquiry " + inquiryId + " by " + bankEmail);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        this.logger.info("Stored reply to inquiry " + inquiryId + " from " + bankEmail);
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
                                @PathParam("inquiryid") int inquiryId) {
        Document expose = DocumentUtil.fetchExposeByInquiryId(inquiryId);

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
                                     @PathParam("inquiryid") int inquiryId) {
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

        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to retrieve list of sites from " + request.getRemoteAddr());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        List<Site> sites = SiteUtil.fetchSites();
        String returnValue;

        Gson gson = new Gson();

        try {
            returnValue = gson.toJson(sites);
        } catch (Exception e) {
            this.logger.warn("Error trying to return site list: " + e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        return Response.ok().entity(returnValue).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    /**
     * Set the site for a bank.
     *
     * @param authorizationHeader the authorization header
     * @param userAgent           the user agent of the client
     * @param request             the http request
     * @param siteId              the id of the site to set
     * @return <CODE>200</CODE> on success
     * <CODE>401</CODE> on authorization error
     * <CODE>404</CODE> if the site or bank could not be found
     */
    @Path("/banks/{email}/site/{siteid}")
    @PUT
    public Response setSite(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                            @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
                            @Context HttpServletRequest request,
                            @PathParam("siteid") int siteId) {
        if (isBankUnauthorized(authorizationHeader)) {
            this.logger.warn("Unauthorized attempt to set a site from " + request.getRemoteAddr());
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        int bankId = Utils.getBankId(authorizationHeader);
        BankSiteUtil.setSiteIdForBankId(bankId, siteId, false);

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
            this.logger.warn("Could not get inquiry");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } else if (ret.equalsIgnoreCase("notFound")) {
            this.logger.warn("Inquiry with id " + inquiryId + " not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        ret = Utils.fixNamespaces(ret, xmlNamespace);
        return Response.ok().entity(ret).header(Constants.SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }

    private boolean isBankUnauthorized(String authorizationHeader) {
        int bankId = Utils.getBankId(authorizationHeader);

        return bankId < 0;
    }

}
