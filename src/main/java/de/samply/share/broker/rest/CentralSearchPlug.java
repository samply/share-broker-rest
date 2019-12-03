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

import de.samply.common.mdrclient.MdrClient;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.DocumentUtil;
import de.samply.share.broker.utils.db.InquiryUtil;
import de.samply.share.broker.utils.db.UserUtil;
import de.samply.share.common.model.uiquerybuilder.QueryItem;
import de.samply.share.common.utils.*;
import de.samply.share.common.utils.oauth2.OAuthUtils;
import de.samply.share.model.common.Query;
import de.samply.share.utils.QueryConverter;
import de.samply.web.mdrFaces.MdrContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.model.tree.TreeModel;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.net.URI;
import java.util.Arrays;

/**
 * The Class CentralSearchPlug.
 *
 * Offers an interface to the central search platform, which allows to post queries to the broker.
 */
@Path("/cs")
public class CentralSearchPlug {

    private static final Logger logger = LogManager.getLogger(CentralSearchPlug.class);

    private final String SERVER_HEADER_KEY = "Server";

    private final String SERVER_HEADER_VALUE = "Samply.Share.Broker/" + ProjectInfo.INSTANCE.getVersionString();

    @Context
    UriInfo uriInfo;

    /**
     * Receive a POST of a search string, transmitted from central search
     *
     * @param queryString the serialized query from central search
     * @param authorizationHeader the authorization string as transmitted from central search
     * @param xmlNamespaceHeader the namespace in which the posted query is
     * @return <CODE>201</CODE> when everything went well
     *         <CODE>400</CODE> on general error
     *         <CODE>403</CODE> on authentication error
     */
    @Path("/request")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response handlePostRequest(String queryString,
                                      @HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                      @HeaderParam(Constants.HEADER_XML_NAMESPACE) String xmlNamespaceHeader) {

        boolean translateToCcpNamespace = SamplyShareUtils.isNullOrEmpty(xmlNamespaceHeader) || xmlNamespaceHeader.equalsIgnoreCase(Constants.VALUE_XML_NAMESPACE_CCP);
        logger.info(queryString);
        if (translateToCcpNamespace) {
            logger.info("Namspace Translation needed.");
        }
        
        // Use a new post request as a trigger to delete old tentative inquiries and unbound exposés.
        // If necessary, create a task for this.
        InquiryUtil.deleteOldTentativeInquiries();
        DocumentUtil.deleteOldUnboundDocuments();

        String userAuthId = OAuthUtils.getUserAuthId(authorizationHeader);
        User user = UserUtil.fetchUserByAuthId(userAuthId);
        
        if (user == null) {
            logger.warn("Invalid access token");
            return Response.status(Status.FORBIDDEN).build();
        }

        Query query;
        try {
            TreeModel<QueryItem> queryTree;
            if (translateToCcpNamespace) {
                de.samply.share.model.ccp.Query ccpQuery = QueryConverter.unmarshal(queryString, JAXBContext.newInstance(de.samply.share.model.ccp.Query.class), de.samply.share.model.ccp.Query.class);
                Query sourceQuery = QueryConverter.convertCcpQueryToCommonQuery(ccpQuery);
                queryTree = QueryTreeUtil.queryToTree(sourceQuery);
            } else {
                queryTree = QueryTreeUtil.queryToTree(QueryConverter.xmlToQuery(queryString));
            }

            MdrClient mdrClient = MdrContext.getMdrContext().getMdrClient();
            QueryValidator queryValidator = new QueryValidator(mdrClient);
            queryValidator.fixBooleans(queryTree);
            
            query = QueryTreeUtil.treeToQuery(queryTree);
        } catch (JAXBException e1) {
            logger.error("Failed to transform query to common namespace");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            logger.debug(QueryConverter.queryToXml(query));
        } catch(JAXBException e) {
            logger.error("Error serializing query: " + Arrays.toString(e.getStackTrace()));
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        InquiryHandler inquiryHandler = new InquiryHandler();
        int inquiryId = inquiryHandler.createTentative(query, user.getId());

        if (inquiryId < 1) {
            logger.warn("Problem while creating inquiry...");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        logger.info("New inquiry created: " + inquiryId);
        String inquiryId_s = String.valueOf(inquiryId);
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        String baseUri = uriInfo.getBaseUri().getPath().replace("rest/", "editInquiry.xhtml");
        URI uri = ub.replacePath(baseUri).queryParam("inquiryId", inquiryId_s).build();
        return Response.status(Response.Status.CREATED).location(uri).header(SERVER_HEADER_KEY, SERVER_HEADER_VALUE).build();
    }
}
