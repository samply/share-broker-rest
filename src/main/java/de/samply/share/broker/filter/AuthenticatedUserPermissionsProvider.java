package de.samply.share.broker.filter;

/**
 * Created on 18.12.2018.
 */

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
    private List<AccessPermission> accessPermissions;

    public void handleAuthenticationEvent(@Observes @AuthenticatedUserPermissions List<String> permissionsAsString) {

        String permissionsAsStringSeperated = permissionsAsString.stream()
                .map(String::toUpperCase)
                .collect(Collectors.joining(","));

        List<AccessPermission> accessPermissions = Arrays.stream(permissionsAsStringSeperated.split(",\\s+"))
                .map(AccessPermission::valueOf)
                .collect(Collectors.toList());

        this.accessPermissions = accessPermissions;
    }
}