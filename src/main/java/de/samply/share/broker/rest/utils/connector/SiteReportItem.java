package de.samply.share.broker.rest.utils.connector;

import java.util.HashMap;
import java.util.Map;

public class SiteReportItem {

    public static final String PROPERTY_LDM = "Lokales Datenmanagement";
    public static final String PROPERTY_IDM = "ID-Manager";
    public static final String PROPERTY_SHARE = "Samply.Share";

    private String id;
    private String name;
    private Map<String, String> versions;

    public SiteReportItem() {
        versions = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getVersions() {
        return versions;
    }

    public void setVersions(Map<String, String> versions) {
        this.versions = versions;
    }
}
