package de.samply.share.broker.filter;

/**
 * Created on 18.12.2018.
 */

import de.samply.auth.rest.RoleDTO;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@RequestScoped
public class AuthenticatedUserPermissionsProvider {

    @Produces
    @RequestScoped
    @AuthenticatedUserPermissions
    private List<RoleDTO> roles;

    public void handleAuthenticationEvent(@Observes @AuthenticatedUserPermissions List<RoleDTO> roles) {
        this.roles = roles;
    }
}