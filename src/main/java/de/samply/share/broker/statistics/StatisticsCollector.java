package de.samply.share.broker.statistics;

import java.util.HashMap;

public class StatisticsCollector {

    private int queryCount;
    private HashMap<String,Integer> mdrCount;
    private HashMap<Integer,Integer> mdrFieldPerQueryCount;
    private HashMap<String, String> mdrMap = new HashMap<>();


    public StatisticsCollector(){
        queryCount = 0;
        mdrCount = new HashMap<>();
        mdrFieldPerQueryCount = new HashMap<>();
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

    public HashMap<Integer, Integer> getMdrFieldPerQueryCount() {
        return mdrFieldPerQueryCount;
    }

    public void setMdrFieldPerQueryCount(HashMap<Integer, Integer> mdrFieldPerQueryCount) {
        this.mdrFieldPerQueryCount = mdrFieldPerQueryCount;
    }

    public void checkMdrCountPerQuery(int mdrCount){
        if(mdrFieldPerQueryCount.containsKey(mdrCount)){
            mdrFieldPerQueryCount.put(mdrCount, mdrFieldPerQueryCount.get(mdrCount)+1);
        }else{
            mdrFieldPerQueryCount.put(mdrCount,1);
        }
    }

    public void checkMdrCount(String urn){
        String readableMdr = mdrMap.get(urn);
        if(mdrCount.containsKey(readableMdr)){
            mdrCount.put(readableMdr, mdrCount.get(readableMdr)+1);
        }else{
            mdrCount.put(readableMdr,1);
        }
    }

    public HashMap<String, Integer> getMdrCount() {
        return mdrCount;
    }

    public void setMdrCount(HashMap<String, Integer> mdrCount) {
        this.mdrCount = mdrCount;
    }

    public int getQueryCount() {
        return queryCount;
    }

    public void setQueryCount(int queryCount) {
        this.queryCount = queryCount;
    }
}
