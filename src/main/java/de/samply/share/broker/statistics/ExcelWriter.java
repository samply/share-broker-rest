package de.samply.share.broker.statistics;

import de.samply.share.broker.model.db.tables.pojos.StatisticsField;
import de.samply.share.broker.utils.MailUtils;
import de.samply.share.broker.utils.db.StatisticsFieldUtil;
import de.samply.share.broker.utils.db.StatisticsQueryUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class ExcelWriter {
    private HashMap<String, List<String>> columns = new HashMap<>();
    private static DateFormat parseFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    private int rowCount = 1;

    public ExcelWriter() {
        initColumns();
    }

    private void initColumns() {
        columns.put("Statistics", Arrays.asList("Statistics"));
    }

    public void sendExcel() throws IOException {

        Workbook workbook = new XSSFWorkbook();
        setStatistic(getStatisticsData(), workbook);
        FileOutputStream fileOut = new FileOutputStream(getPath() + ".xlsx");
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        MailUtils.sendStatistics();
    }

    private StatisticsResult getStatisticsData() {
        StatisticsResult statisticsResult = new StatisticsResult();
        List<Integer> queryIds = StatisticsQueryUtil.getLastDayQueryIds();
        List<QueryField> queryFields = queryIds.stream()
                .map(QueryField::new)
                .collect(toList());
        List<StatisticsField> statisticsFields = StatisticsFieldUtil.getFieldsByQueryIds(queryIds);
        queryFields = addFieldsToQuery(statisticsFields, queryFields);
        statisticsResult.setQueryCount(queryIds.size());
        for (QueryField queryField : queryFields) {
            for (StatisticsField statisticsField : queryField.getStatisticsFields()) {
                statisticsResult.countFields(statisticsField.getUrn());
            }
            statisticsResult.countSelectedFieldsPerQuery(queryField.getStatisticsFields().size());
        }
        return statisticsResult;
    }

    private List<QueryField> addFieldsToQuery(List<StatisticsField> statisticsFields, List<QueryField> queryFields) {
        for (StatisticsField statisticsField : statisticsFields) {
            for (QueryField queryField : queryFields) {
                if (queryField.getId() == statisticsField.getQueryid()) {
                    queryField.getStatisticsFields().add(statisticsField);
                }
            }
        }
        return queryFields;
    }

    protected String getPath() throws IOException {
        String path = System.getProperty("catalina.base") + File.separator + "logs" + File.separator + "statistics";
        File dir = new File(path);
        if (!dir.exists()) {
            Files.createDirectories(Paths.get(path));
        }
        return path + File.separator + "statistic_"
                + parseFormat.format(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24));
    }

    private void setStatistic(StatisticsResult statisticDto, Workbook workbook) {
        for (String sheetName : columns.keySet()) {
            Sheet sheet = createSheet(workbook, sheetName);
            totalQueryCount(statisticDto, sheet, workbook);
            mdrFieldCount(statisticDto, sheet, workbook);
            mdrFieldByName(statisticDto, sheet, workbook);
            // Resize all columns to fit the content size
            for (int i = 0; i < columns.get(sheetName).size(); i++) {
                sheet.autoSizeColumn(i);
            }
        }
    }

    private void totalQueryCount(StatisticsResult statisticsResult, Sheet sheet, Workbook workbook) {
        Row row = sheet.createRow(rowCount++);
        row.createCell(0)
                .setCellValue("Total Query Count");

        row.createCell(1)
                .setCellValue(statisticsResult.getQueryCount());
        sheet.createRow(rowCount++);
    }

    private Sheet createSheet(Workbook workbook, String sheetName) {
        Sheet sheet = workbook.createSheet(sheetName);
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);
        List<String> column = columns.get(sheetName);
        // Create cells
        for (int i = 0; i < column.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(column.get(i));
            cell.setCellStyle(headerCellStyle);
        }
        return sheet;
    }

    private void mdrFieldCount(StatisticsResult statisticsResult, Sheet sheet, Workbook workbook) {
        boldHeader(sheet, workbook, "Queries");
        HashMap<Integer, Integer> result = statisticsResult.getSelectedFieldsPerQuery().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
            Row row = sheet.createRow(rowCount++);

            row.createCell(0)
                    .setCellValue(entry.getKey());

            row.createCell(1)
                    .setCellValue(entry.getValue());
        }
        sheet.createRow(rowCount++);
    }

    private void boldHeader(Sheet sheet, Workbook workbook, String description) {
        CellStyle backgroundStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        backgroundStyle.setFont(font);
        Row row = sheet.createRow(rowCount++);
        Cell cell = row.createCell(0);
        cell.setCellStyle(backgroundStyle);
        cell.setCellValue(description);
        cell = row.createCell(1);
        cell.setCellStyle(backgroundStyle);
        cell.setCellValue("Count");
    }

    private void mdrFieldByName(StatisticsResult statisticsResult, Sheet sheet, Workbook workbook) {
        boldHeader(sheet, workbook, "MdrFields");
        HashMap<String, Integer> result = statisticsResult.getFieldCount().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            Row row = sheet.createRow(rowCount++);

            row.createCell(0)
                    .setCellValue(entry.getKey());

            row.createCell(1)
                    .setCellValue(entry.getValue());
        }
        sheet.createRow(rowCount++);
    }

}
