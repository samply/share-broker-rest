package de.samply.share.broker.filter;

import de.samply.auth.rest.RoleDTO;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.util.ArrayList;
import java.util.List;


@RequestScoped
public class AuthenticatedUserPermissionsProvider {

    @Produces
    @RequestScoped
    @AuthenticatedUserPermissions
    private List<RoleDTO> roles = new ArrayList<>();

    public void handleAuthenticationEvent(@Observes @AuthenticatedUserPermissions List<RoleDTO> roles) {
        this.roles = roles;
    }
}