package de.samply.share.broker.listener;

import de.samply.share.common.utils.SamplyShareUtils;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpSession;

/**
 * The listener interface for receiving authorization events. The class that is interested in
 * processing a authorization event implements this interface, and the object created with that
 * class is registered with a component using the component's <code>addAuthorizationListener</code>
 * method. When the authorization event occurs, that object's appropriate method is invoked.
 */
public class AuthorizationListener implements PhaseListener {

  /**
   * The Constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  /* (non-Javadoc)
   * @see javax.faces.event.PhaseListener#afterPhase(javax.faces.event.PhaseEvent)
   */
  @Override
  public void afterPhase(PhaseEvent event) {

    FacesContext facesContext = event.getFacesContext();
    String currentPage = facesContext.getViewRoot().getViewId();

    boolean isLoginPage = (currentPage.lastIndexOf("loginRedirect.xhtml") > -1
        || currentPage.lastIndexOf("samplyLogin.xhtml") > -1);
    HttpSession session = (HttpSession) facesContext.getExternalContext()
        .getSession(false);

    if (session == null) {

      NavigationHandler nh = facesContext.getApplication()
          .getNavigationHandler();
      nh.handleNavigation(facesContext, null, "loginRedirect");
    } else {
      Object currentUser = session.getAttribute("username");

      if (!isLoginPage && (SamplyShareUtils.isNullOrEmpty((String) currentUser))) {
        NavigationHandler nh = facesContext.getApplication()
            .getNavigationHandler();
        nh.handleNavigation(facesContext, null, "loginRedirect");
      }
    }
  }

  /* (non-Javadoc)
   * @see javax.faces.event.PhaseListener#beforePhase(javax.faces.event.PhaseEvent)
   */
  @Override
  public void beforePhase(PhaseEvent event) {

  }

  /* (non-Javadoc)
   * @see javax.faces.event.PhaseListener#getPhaseId()
   */
  @Override
  public PhaseId getPhaseId() {
    return PhaseId.RESTORE_VIEW;
  }
}
