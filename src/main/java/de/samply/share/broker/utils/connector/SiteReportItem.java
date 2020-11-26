package de.samply.share.broker.utils.connector;

import java.util.HashMap;
import java.util.Map;

public class SiteReportItem {

  private String id;
  private String name;
  private Map<String, String> versions;

  public SiteReportItem() {
    versions = new HashMap<>();
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, String> getVersions() {
    return versions;
  }

}
