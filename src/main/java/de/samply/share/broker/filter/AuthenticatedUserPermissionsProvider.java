package de.samply.share.broker.filter;

import de.samply.bbmri.auth.rest.RoleDto;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

@RequestScoped
public class AuthenticatedUserPermissionsProvider {

  @Produces
  @RequestScoped
  @AuthenticatedUserPermissions
  private List<RoleDto> roles = new ArrayList<>();

  public void handleAuthenticationEvent(
      @Observes @AuthenticatedUserPermissions List<RoleDto> roles) {
    this.roles = roles;
  }
}
