package de.samply.share.broker.filter;

/**
 * Created on 18.12.2018.
 */

import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.UserUtil;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;


@RequestScoped
public class AuthenticatedUserProvider {

    @Produces
    @RequestScoped
    @AuthenticatedUser
    private User authenticatedUser;

    public void handleAuthenticationEvent(@Observes @AuthenticatedUser String username) {
        this.authenticatedUser = findUser(username);
    }

    /**
     * Hit the the database or a service to find a user by its username and return it
     * Return the User instance
     */
    private User findUser(String username) {
        return UserUtil.fetchUserByAuthId(username);
    }
}