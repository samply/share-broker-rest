package de.samply.share.broker.feature;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum ClientFeature implements Feature {

  @Label("Create new site")
  CREATE_NEW_SITE;

  public boolean isActive() {
    return FeatureContext.getFeatureManager().isActive(this);
  }

}
