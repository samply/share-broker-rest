package de.samply.share.broker.statistics;

import com.google.gson.*;
import de.samply.share.broker.jdbc.ResourceManager;
import org.jooq.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;


public class StatisticReader {
    HashMap<String, String> mdrMap = new HashMap<>();

    public StatisticReader() {
        init();
    }

    private void init() {
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

    public String getStatistic(Filter filterJson) {
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("fieldValue",parser.parse(gson.toJson(readFieldValue(filterJson))).getAsJsonObject());
        jsonObject.add("fieldCount",parser.parse(gson.toJson(readFieldCount(filterJson))).getAsJsonObject());
        jsonObject.add("queryCount", parser.parse(gson.toJson(readQueryCount(filterJson))).getAsJsonObject());
        return jsonObject.toString();
    }

    /**
     * Read how often a field was selected
     * @param filter
     * @return the count of the fieldname (Hashmap<Name of the field, count>)
     */
    private HashMap<String,Integer> readFieldValue(Filter filter){
        HashMap<String,Integer> map = new HashMap<>();
        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);
            Result<Record> result  = dslContext.resultQuery(
                    "select s_field.urn,count(s_field.urn) from samply.statistics_query as s_query " +
                    "left join samply.statistics_field as s_field on (s_query.id = s_field.statistic_query_id) " +
                    "where s_query.created " +
                    "between '"+filter.getStartDate()+"'::timestamp and '"+filter.getEndDate()+"'::timestamp " +
                    "group by s_field.urn").fetch();
            for (Record record : result) {
                map.put(mdrMap.get(record.getValue(0).toString()), Integer.parseInt(record.getValue(1).toString()));
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read how many queries were created
     * @param filter
     * @return the count of the queries by day (Hashmap<Day,Count>)
     */
    private HashMap<String,Integer> readQueryCount(Filter filter) {
        HashMap<String,Integer> map = new HashMap<>();
        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);
            Result<Record> result  = dslContext.resultQuery(
                    "select to_char(s_query.created, 'yyyy-MM-dd'),count(to_char(s_query.created, 'yyyy-MM-dd')) " +
                            "from samply.statistics_query as s_query where s_query.created " +
                            "BETWEEN '"+filter.getStartDate()+"'::timestamp AND '"+filter.getEndDate()+"'::timestamp " +
                            "group by to_char(s_query.created, 'yyyy-MM-dd')").fetch();
            for (Record record : result) {
                map.put(record.getValue(0).toString(), Integer.parseInt(record.getValue(1).toString()));
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Read how many queries with how many search criterias were created
     * @param filter
     * @return the counts of the search criterias. (e.g. Hashmap<2, 5> means there are 5 queries with 2 search criterias)
     */
    private HashMap<String,Integer> readFieldCount(Filter filter) {
        HashMap<String,Integer> map = new HashMap<>();
        try (Connection connection = ResourceManager.getConnection()) {
            DSLContext dslContext = ResourceManager.getDSLContext(connection);
            Result<Record> result  = dslContext.resultQuery(
                    "select counts,count(counts) from " +
                            "(select s_field.statistic_query_id, count(s_field.statistic_query_id) as counts from samply.statistics_query as s_query " +
                            "left join samply.statistics_field as s_field on (s_query.id = s_field.statistic_query_id) where s_query.created " +
                            "BETWEEN '"+filter.getStartDate()+"'::timestamp AND '"+filter.getEndDate()+"'::timestamp " +
                            "group by s_field.statistic_query_id) " +
                            "countField group by counts ; ").fetch();
            for (Record record : result) {
                map.put(record.getValue(0).toString(), Integer.parseInt(record.getValue(1).toString()));
            }
            return map;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
