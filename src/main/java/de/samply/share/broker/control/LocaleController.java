package de.samply.share.broker.control;

import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;

/**
 * internationalization.
 */
public class LocaleController {

  private Locale locale;

  @PostConstruct
  public void init() {
    locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
  }

  public String getLanguage() {
    return locale.getLanguage();
  }

}
