package de.samply.share.broker.utils;

import org.apache.commons.lang.StringUtils;

import javax.ws.rs.core.Response;
import org.slf4j.Logger;

public class ResonseBuilderHelper {

    private final Logger logger;

    public ResonseBuilderHelper(Logger logger) {
        this.logger = logger;
    }

    public Response createOkResponse() {
        return createResponse(StringUtils.EMPTY, Response.Status.OK);
    }

    public Response createUnauthorizedResponse() {
        return createUnauthorizedResponse(StringUtils.EMPTY);
    }

    public Response createUnauthorizedResponse(String logMessage) {
        return createResponse(logMessage, Response.Status.UNAUTHORIZED);
    }

    public Response createBadRequestResponse() {
        return createBadRequestResponse(StringUtils.EMPTY);
    }

    public Response createBadRequestResponse(String logMessage) {
        return createResponse(logMessage, Response.Status.BAD_REQUEST);
    }

    public Response createNotFoundResponse() {
        return createNotFoundResponse(StringUtils.EMPTY);
    }

    public Response createNotFoundResponse(String logMessage) {
        return createResponse(logMessage, Response.Status.NOT_FOUND);
    }

    public Response createInternalServerErrorResponse() {
        return createInternalServerErrorResponse(StringUtils.EMPTY);
    }

    public Response createInternalServerErrorResponse(String logMessage) {
        return createResponse(logMessage, Response.Status.INTERNAL_SERVER_ERROR);
    }

    private Response createResponse(String logMessage, Response.Status status) {
        if (!StringUtils.isEmpty(logMessage)) {
            logger.warn(logMessage);
        }
        return Response.status(status).build();
    }
}
