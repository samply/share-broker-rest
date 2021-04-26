package de.samply.share.broker.control;

import de.samply.share.broker.feature.ClientConfiguration;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.togglz.core.manager.FeatureManager;

@ManagedBean(name = "applicationBean", eager = true)
@ApplicationScoped
public class ApplicationBean {

  public static FeatureManager featureManager;

  public static FeatureManager getFeatureManager() {
    return featureManager;
  }

  public void setFeatureManager(FeatureManager featureManager) {
    this.featureManager = featureManager;
  }

  @PostConstruct
  public void init() {
    featureManager = new ClientConfiguration().getFeatureManager();
  }
}
