package de.samply.share.broker.filter;

import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.UserUtil;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;


@RequestScoped
public class AuthenticatedUserProvider {

  private static final int ANONYMOUS_USER_ID = 1;

  @Produces
  @RequestScoped
  @AuthenticatedUser
  private User authenticatedUser;

  public void handleAuthenticationEvent(@Observes @AuthenticatedUser String username) {
    this.authenticatedUser = findUser(username);
  }

  @PostConstruct
  private void init() {
    this.authenticatedUser = createDefaultAnonymousUser();
  }

  private User findUser(String username) {
    User user = UserUtil.fetchUserByAuthId(username);

    return (user != null) ? user : createDefaultAnonymousUser();
  }

  private User createDefaultAnonymousUser() {
    User user = new User();
    user.setId(ANONYMOUS_USER_ID);
    return user;
  }
}
