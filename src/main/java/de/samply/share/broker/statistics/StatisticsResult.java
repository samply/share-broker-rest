package de.samply.share.broker.statistics;

import java.util.HashMap;

public class StatisticsResult {

    private int queryCount;
    private HashMap<String, Integer> fieldCount;
    private HashMap<Integer, Integer> selectedFieldsPerQuery;
    private HashMap<String, String> mdrMap = new HashMap<>();


    public StatisticsResult() {
        queryCount = 0;
        fieldCount = new HashMap<>();
        selectedFieldsPerQuery = new HashMap<>();
        initMap();
    }

    private void initMap() {
        mdrMap.put("urn:mdr16:dataelement:31:1", "BMI");
        mdrMap.put("urn:mdr16:dataelement:28:1", "Diag. Alter");
        mdrMap.put("urn:mdr16:dataelement:27:1", "Diagnose");
        mdrMap.put("urn:mdr16:dataelement:26:1", "Diag. Datum");
        mdrMap.put("urn:mdr16:dataelement:33:1", "Fastenst.");
        mdrMap.put("urn:mdr16:dataelement:29:1", "Gewicht");
        mdrMap.put("urn:mdr16:dataelement:30:1", "Größe");
        mdrMap.put("urn:mdr16:dataelement:32:1", "Raucher");
        mdrMap.put("urn:mdr16:dataelement:34:1", "Diag. Tod");
        mdrMap.put("urn:mdr16:dataelement:14:1", "Alter");
        mdrMap.put("urn:mdr16:dataelement:17:1", "Temperatur");
        mdrMap.put("urn:mdr16:dataelement:12:1", "Entnahmedat.");
        mdrMap.put("urn:mdr16:dataelement:16:1", "Material fl.");
        mdrMap.put("urn:mdr16:dataelement:15:1", "Material Gew.");
        mdrMap.put("urn:mdr16:dataelement:18:1", "Analyse");
        mdrMap.put("urn:mdr16:dataelement:23:1", "Geschlecht");
        mdrMap.put("urn:mdr16:dataelement:24:1", "Todesdat.");
    }

    public HashMap<Integer, Integer> getSelectedFieldsPerQuery() {
        return selectedFieldsPerQuery;
    }

    public void setSelectedFieldsPerQuery(HashMap<Integer, Integer> selectedFieldsPerQuery) {
        this.selectedFieldsPerQuery = selectedFieldsPerQuery;
    }

    public void countSelectedFieldsPerQuery(int count) {
        if (selectedFieldsPerQuery.containsKey(count)) {
            selectedFieldsPerQuery.put(count, selectedFieldsPerQuery.get(count) + 1);
        } else {
            selectedFieldsPerQuery.put(count, 1);
        }
    }

    public void countFields(String urn) {
        String readableMdr = mdrMap.get(urn);
        if (fieldCount.containsKey(readableMdr)) {
            fieldCount.put(readableMdr, fieldCount.get(readableMdr) + 1);
        } else {
            fieldCount.put(readableMdr, 1);
        }
    }

    public HashMap<String, Integer> getFieldCount() {
        return fieldCount;
    }

    public void setFieldCount(HashMap<String, Integer> fieldCount) {
        this.fieldCount = fieldCount;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }
}
