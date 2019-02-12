package de.samply.share.broker.filter;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.samply.auth.rest.RoleDTO;
import de.samply.share.broker.model.db.tables.pojos.User;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.management.relation.Role;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Comes after Authentication to handle Roles
 */
@Secured
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Inject
    @AuthenticatedUser
    User user;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        //TODO: Activate again


        // Get the resource class which matches with the requested URL
        // Extract the permissions declared by it
        Class<?> resourceClass = resourceInfo.getResourceClass();
        List<AccessPermission> classPermissions = extractPermissions(resourceClass);

        // Get the resource method which matches with the requested URL
        // Extract the permissions declared by it
        Method resourceMethod = resourceInfo.getResourceMethod();
        List<AccessPermission> methodPermissions = extractPermissions(resourceMethod);
        Gson gson = new Gson();
        ArrayList<RoleDTO> roles = gson.fromJson(user.getRole(), new TypeToken<ArrayList<RoleDTO>>() {
        }.getType());
        try {

            // Check if the user is allowed to execute the method
            // The method annotations override the class annotations
            if (methodPermissions.isEmpty()) {
                checkPermissions(classPermissions, roles);
            } else {
                checkPermissions(methodPermissions, roles);
            }

        } catch (Exception e) {
            requestContext.abortWith(
                    Response.status(Response.Status.FORBIDDEN).build());
        }
    }

    // Extract the permissions from the annotated element
    private List<AccessPermission> extractPermissions(AnnotatedElement annotatedElement) {
        if (annotatedElement == null) {
            return new ArrayList<AccessPermission>();
        } else {
            Secured secured = annotatedElement.getAnnotation(Secured.class);
            if (secured == null) {
                return new ArrayList<AccessPermission>();
            } else {
                AccessPermission[] allowedPermissions = secured.value();
                return Arrays.asList(allowedPermissions);
            }
        }
    }

    private void checkPermissions(List<AccessPermission> allowedAccessPermissions, List<RoleDTO> roles) throws Exception {
        for (RoleDTO roleDTO : roles) {
            for (AccessPermission accessPermission : allowedAccessPermissions) {
                if (accessPermission.name().equals(roleDTO.getIdentifier())) {
                    return;
                }
            }
        }
        // Check if the user contains one of the allowed permissions
        // Throw an Exception if the user has not permission to execute the method
        throw new Exception(); // Permission denied
    }
}
