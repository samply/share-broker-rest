package de.samply.share.broker.filter;

import de.samply.bbmri.auth.client.jwt.JwtException;
import de.samply.bbmri.auth.client.jwt.JwtIdToken;
import de.samply.bbmri.auth.rest.RoleDto;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.ContactUtil;
import de.samply.share.broker.utils.db.UserUtil;
import de.samply.share.common.utils.ProjectInfo;
import de.samply.share.common.utils.oauth2.OAuthConfig;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ContainerRequestContext can be used to access the HTTP request headers and then extract the
 * token.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

  private static final String REALM = "example";
  private static final String AUTHENTICATION_SCHEME = "Bearer";
  final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
  @Inject
  @AuthenticatedUser
  Event<String> userAuthenticatedEvent;
  @Inject
  @AuthenticatedUserPermissions
  Event<List<RoleDto>> userAuthenticatedPermissionsEvent;

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    // Get the Authorization header from the request
    String authorizationHeader =
        requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

    // Validate the Authorization header
    if (!isTokenBasedAuthentication(authorizationHeader)) {
      logger.debug("Authorization header not valid");
      abortWithUnauthorized(requestContext);
      return;
    }

    // Extract the token from the Authorization header
    String token = authorizationHeader
        .substring(AUTHENTICATION_SCHEME.length()).trim();

    // Create instance of JwtIdToken by combining public key + client id of configuration file and
    // of course the token itself
    JwtIdToken jwtIdToken;
    try {
      String[] fallbacks = {System.getProperty("catalina.base") + File.separator + "conf",
          ProjectInfo.INSTANCE.getServletContext().getRealPath("/WEB-INF")};
      jwtIdToken = new JwtIdToken(
          OAuthConfig.getOAuth2Client(ProjectInfo.INSTANCE.getProjectName(), fallbacks), token);
    } catch (JwtException e) {
      logger.error("Create instance of JwtIdToken failed. OAuth2Client.xml? JWT-ID-Token?", e);
      abortWithUnauthorized(requestContext);
      return;
    }

    if (!jwtIdToken.isValid()) {
      logger.debug("token not valid");
      abortWithUnauthorized(requestContext);
      return;
    }

    //send user to AuthenticatedUserProvider to get access to the user from everywhere
    userAuthenticatedEvent.fire(jwtIdToken.getSubject());
    //send permissions to AuthenticatedUserPermissionsProvider to get access to the permissions from
    // everywhere
    userAuthenticatedPermissionsEvent.fire(jwtIdToken.getRoles());
  }

  private boolean isTokenBasedAuthentication(String authorizationHeader) {
    // Check if the Authorization header is valid
    // It must not be null and must be prefixed with "Bearer" plus a whitespace
    // The authentication scheme comparison must be case-insensitive
    return authorizationHeader != null && authorizationHeader.toLowerCase()
        .startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
  }

  private void abortWithUnauthorized(ContainerRequestContext requestContext) {
    // Abort the filter chain with a 401 status code response
    // The WWW-Authenticate header is sent along with the response
    requestContext.abortWith(
        Response.status(Response.Status.UNAUTHORIZED)
            .header(HttpHeaders.WWW_AUTHENTICATE,
                AUTHENTICATION_SCHEME + " realm=\"" + REALM + "\"")
            .build());
  }

  private void storeInDb(ContainerRequestContext requestContext, JwtIdToken jwtIdToken) {
    // Make sure the user and contact are saved in db
    User user = null;
    user = UserUtil.createOrUpdateUser(jwtIdToken);
    if (user == null) {
      logger.debug("User could not be created or updated");
      abortWithUnauthorized(requestContext);
      return;
    }
    if (ContactUtil.getContactForUser(user) == null) {
      ContactUtil.createContactForUser(user);
    }
  }

}
