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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        List<StatisticsField> statisticsFields = StatisticsFieldUtil.getFieldsByQueryIds(queryIds);
        statisticsResult.setQueryCount(queryIds.size());
        int mdrCountPerQuery = 0;
        int queryId = 0;
        int queriesWithFields = 0;
        for (StatisticsField statisticsField : statisticsFields) {
            if (queryId == 0 || queryId == statisticsField.getQueryid()) {
                if (queriesWithFields == 0) {
                    queryId = statisticsField.getQueryid();
                    queriesWithFields++;
                }
                mdrCountPerQuery++;
            } else {
                statisticsResult.countSelectedFieldsPerQuery(mdrCountPerQuery);
                queryId = statisticsField.getQueryid();
                mdrCountPerQuery = 1;
                queriesWithFields++;
            }
            statisticsResult.countFields(statisticsField.getUrn());
        }
        statisticsResult.countSelectedFieldsPerQuery(mdrCountPerQuery);
        int queriesWithZeroFields = queryIds.size() - queriesWithFields;
        statisticsResult.getSelectedFieldsPerQuery().put(0, queriesWithZeroFields);
        return statisticsResult;
    }

    protected String getPath() {
        return System.getProperty("catalina.base") + File.separator + "logs"
                + File.separator + "statistics" + File.separator + "statistic_"
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
        Row row;
        HashMap<Integer, Integer> result = statisticsResult.getSelectedFieldsPerQuery().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        for (Map.Entry<Integer, Integer> entry : result.entrySet()) {
            row = sheet.createRow(rowCount++);

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
        Row row;
        HashMap<String, Integer> result = statisticsResult.getFieldCount().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            row = sheet.createRow(rowCount++);

            row.createCell(0)
                    .setCellValue(entry.getKey());

            row.createCell(1)
                    .setCellValue(entry.getValue());
        }
        sheet.createRow(rowCount++);
    }

}
