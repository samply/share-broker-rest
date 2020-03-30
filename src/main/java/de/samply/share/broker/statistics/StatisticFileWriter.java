package de.samply.share.broker.statistics;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.samply.share.essentialquery.EssentialSimpleFieldDto;
import de.samply.share.essentialquery.EssentialSimpleQueryDto;
import de.samply.share.essentialquery.EssentialSimpleValueDto;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

public class StatisticFileWriter {
    private HashMap<String, String> mdrMap = new HashMap<>();
    private DateFormat parseFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    Gson gson = new Gson();

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
            file = getFileAsString(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StatisticDto statisticDto = convertFileToStatisticDto(file);
        StatisticQueryDto statisticQueryDto = new StatisticQueryDto();
        statisticQueryDto.setId(inquiryId);
        statisticQueryDto.setStatisticFieldDtos(readFields(queryDto));
        statisticQueryDto.setDate(parseFormat.format(new Date(System.currentTimeMillis())));
        if (statisticDto.getQueryCount() > 0) {
            statisticDto.getStatisticQueryDtos().add(statisticQueryDto);
            statisticDto.setQueryCount(statisticDto.getQueryCount() + 1);
            statisticDto.setMdrFields(countMdrFieldsByName(statisticDto.getMdrFields(), queryDto));
            statisticDto.setMdrFieldsPerQuery(countMdrFields(statisticDto.getMdrFieldsPerQuery(), queryDto));
        } else {
            statisticDto.getStatisticQueryDtos().add(statisticQueryDto);
            statisticDto.setQueryCount(1);
            statisticDto.setMdrFields(countMdrFieldsByName(new HashMap<>(), queryDto));
            statisticDto.setMdrFieldsPerQuery(countMdrFields(new HashMap<>(), queryDto));
        }
        writeToFile(statisticDto);
    }

    private HashMap<String, Integer> countMdrFields(HashMap<String, Integer> mdrFields, EssentialSimpleQueryDto queryDto) {
        int selectedMdrFields = queryDto.getFieldDtos().size();
        String propertyName = "queries with " + selectedMdrFields + " selected fields";
        if (mdrFields.containsKey(propertyName)) {
            mdrFields.put(propertyName, mdrFields.get(propertyName) + selectedMdrFields);
        } else {
            mdrFields.put(propertyName, selectedMdrFields);
        }
        return mdrFields;
    }

    private HashMap<String, Integer> countMdrFieldsByName(HashMap<String, Integer> mdrFields, EssentialSimpleQueryDto queryDto) {
        for (EssentialSimpleFieldDto essentialSimpleFieldDto : queryDto.getFieldDtos()) {
            String urnAsString = mdrMap.get(essentialSimpleFieldDto.getUrn());
            if (mdrFields.containsKey(urnAsString)) {
                mdrFields.put(urnAsString, mdrFields.get(urnAsString) + 1);
            } else {
                mdrFields.put(urnAsString, 1);
            }
        }
        return mdrFields;
    }

    protected StatisticDto convertFileToStatisticDto(String file) {
        JsonParser parser = new JsonParser();
        return (file.equals("")) ? new StatisticDto() : gson.fromJson(parser.parse(file).getAsJsonObject(), StatisticDto.class);
    }


    private List<StatisticFieldDto> readFields(EssentialSimpleQueryDto queryDto) {
        List<StatisticFieldDto> statisticFieldDtos = new ArrayList<>();
        for (EssentialSimpleFieldDto essentialSimpleFieldDto : queryDto.getFieldDtos()) {
            StatisticFieldDto statisticFieldDto = new StatisticFieldDto();
            statisticFieldDto.setMdrField(mdrMap.get(essentialSimpleFieldDto.getUrn()));
            statisticFieldDto.setStatisticValueDtos(readValue(essentialSimpleFieldDto));
            statisticFieldDtos.add(statisticFieldDto);
        }
        return statisticFieldDtos;
    }

    private List<StatisticValueDto> readValue(EssentialSimpleFieldDto essentialSimpleFieldDto) {
        List<StatisticValueDto> statisticValueDtos = new ArrayList<>();
        for (EssentialSimpleValueDto essentialSimpleValueDto : essentialSimpleFieldDto.getValueDtos()) {
            statisticValueDtos.add(new StatisticValueDto(essentialSimpleValueDto));
        }
        return statisticValueDtos;
    }

    private void writeToFile(StatisticDto statisticDto) {
        Writer out;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream( getStatisticFileName(false) + ".txt"), StandardCharsets.UTF_8));
            out.write(gson.toJson(statisticDto));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String getFileAsString(boolean excel) throws IOException {
        String path;
        path = getStatisticFileName(excel) + ".txt";
        File file = new File(path);
        return (file.exists()) ? IOUtils.toString(new FileInputStream(new File(path)), "UTF-8") : "";
    }

    protected String getStatisticFileName(boolean excel) {
        String path = System.getProperty("catalina.base") + File.separator + "logs" + File.separator + "statistics" + File.separator + "statistic_";
        if (excel) {
            return path + parseFormat.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24));
        }
        return path + parseFormat.format(new Date(System.currentTimeMillis()));
    }
}