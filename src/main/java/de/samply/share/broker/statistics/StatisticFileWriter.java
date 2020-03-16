package de.samply.share.broker.statistics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class StatisticFileWriter {
    private HashMap<String, String> mdrMap = new HashMap<>();
    private DateFormat parseFormat = new SimpleDateFormat(
            "yyyy-MM-dd");

    public StatisticFileWriter() {
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

    public void save(EssentialSimpleQueryDto queryDto, int inquiryId) {
        String file = "";
        try {
            file = getFileAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonObject statisticObject = convertFileToJson(file);
        JsonObject queryObject = new JsonObject();
        queryObject.addProperty("id",inquiryId);
        queryObject.addProperty("fields", readFields(queryDto).toString());
        queryObject.addProperty("date", parseFormat.format(new Date(System.currentTimeMillis())));
        if ((statisticObject.has("queries"))) {
            statisticObject.getAsJsonArray("queries").add(queryObject);
        } else {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(queryObject);
            statisticObject.add("queries", jsonArray);
        }
        writeToFile(statisticObject);
    }

    private JsonObject convertFileToJson(String file){
        JsonParser parser = new JsonParser();
        return (file.equals("")) ? new JsonObject() : parser.parse(file).getAsJsonObject();
    }


    private JsonArray readFields(EssentialSimpleQueryDto queryDto) {
        JsonArray fieldArray = new JsonArray();
        for (EssentialSimpleFieldDto essentialSimpleFieldDto : queryDto.getFieldDtos()) {
            JsonObject fieldObject = new JsonObject();
            fieldObject.addProperty("mdrField", mdrMap.get(essentialSimpleFieldDto.getUrn()));
            fieldObject.addProperty("values", readValue(essentialSimpleFieldDto).toString());
            fieldArray.add(fieldObject);
        }
        return fieldArray;
    }

    private JsonArray readValue(EssentialSimpleFieldDto essentialSimpleFieldDto) {
        JsonArray valueArray = new JsonArray();
        for (EssentialSimpleValueDto essentialSimpleValueDto : essentialSimpleFieldDto.getValueDtos()) {
            JsonObject valueObject = new JsonObject();
            valueObject.addProperty("condition", essentialSimpleValueDto.getCondition().toString());
            valueObject.addProperty("minValue", essentialSimpleValueDto.getValue());
            valueObject.addProperty("maxValue", essentialSimpleValueDto.getMaxValue());
            valueArray.add(valueObject);
        }
        return valueArray;
    }

    private void writeToFile(JsonObject queryObject) {
        Writer out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath() + "statistic_"+ parseFormat.format(new Date(System.currentTimeMillis()))+".txt"), StandardCharsets.UTF_8));
            out.write(queryObject.toString().replace("\\", "").replace(":\"[", ":[").replace("]\"", "]"));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileAsString() throws IOException {
        String path = Objects.requireNonNull(this.getClass().getClassLoader().getResource("")).getPath() + "statistic_"+ parseFormat.format(new Date(System.currentTimeMillis()))+".txt";
        File file = new File(path);
        return (file.exists())? IOUtils.toString(new FileInputStream(new File(path)),"UTF-8"): "";
    }
}