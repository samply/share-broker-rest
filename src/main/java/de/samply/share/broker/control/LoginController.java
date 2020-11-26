package de.samply.share.broker.control;

import de.samply.share.broker.filter.AuthenticatedUser;
import de.samply.share.broker.model.db.tables.pojos.Contact;
import de.samply.share.broker.model.db.tables.pojos.Site;
import de.samply.share.broker.model.db.tables.pojos.User;
import de.samply.share.broker.utils.db.SiteUtil;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * A JSF Managed Bean that is existent for a whole session. It handles user access.
 */
@Named
@SessionScoped
public class LoginController implements Serializable {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = -5474963222029599835L;

  /**
   * The user.
   */
  @Inject
  @AuthenticatedUser
  User authenticatedUser;
  private User user = authenticatedUser;

  /**
   * The contact.
   */
  private Contact contact = new Contact();

  private String code = null;

  private String error = null;

  /**
   * Gets the user.
   *
   * @return the user
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the user.
   *
   * @param user the new user
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Gets the contact.
   *
   * @return the contact
   */
  public Contact getContact() {
    return contact;
  }

  /**
   * Sets the contact.
   *
   * @param contact the new contact
   */
  public void setContact(Contact contact) {
    this.contact = contact;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  /**
   * Get the list of all sites that are in the database.
   *
   * @return list of all sites
   */
  public List<Site> getSites() {
    return SiteUtil.fetchSites();
  }

}
