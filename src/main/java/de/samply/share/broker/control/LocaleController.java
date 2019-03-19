package de.samply.share.broker.control;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import java.util.Locale;

/**
 * internationalization
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
