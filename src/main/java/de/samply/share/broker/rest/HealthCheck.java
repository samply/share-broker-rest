package de.samply.share.broker.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * REST resource based health check endpoint to monitor status of the component.
 */
@Path("/health")
public class HealthCheck {

  /**
   * Gets the status of the searchbroker.
   *
   * @return health status (code 200, 'OK')
   */
  @GET
  @Produces(TEXT_PLAIN)
  public Response getHealthStatus() {
    return Response.ok("OK").build();
  }
}
