package de.samply.share.broker.statistics;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import de.samply.share.broker.utils.MailUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExcelWriter {
    private HashMap<String, List<String>> columns = new HashMap<>();
    private static DateFormat parseFormat = new SimpleDateFormat(
            "yyyy-MM-dd");
    static Gson gson = new Gson();
    private int rowCount = 1;

    public ExcelWriter() {
        initColumns();
    }

    private void initColumns() {
        columns.put("Statistics", Arrays.asList("Statistics"));
    }

    public void createExcel() throws IOException {
        StatisticFileWriter statisticFileWriter = new StatisticFileWriter();
        StatisticDto statisticDto = statisticFileWriter.convertFileToStatisticDto(statisticFileWriter.getFileAsString(true));
        Workbook workbook = new XSSFWorkbook();
        setStatistic(statisticDto, workbook);
        try {
            FileOutputStream fileOut = new FileOutputStream(statisticFileWriter.getStatisticFileName(true) +".xlsx");
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();
            MailUtils.sentStatistics();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStatistic(StatisticDto statisticDto, Workbook workbook) {
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

    private void totalQueryCount(StatisticDto statisticDto, Sheet sheet, Workbook workbook) {
        Row row = sheet.createRow(rowCount++);
        row.createCell(0)
                .setCellValue("Total Query Count");

        row.createCell(1)
                .setCellValue(statisticDto.getQueryCount());
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

    private void mdrFieldCount(StatisticDto statisticDto, Sheet sheet, Workbook workbook) {
        boldHeader(sheet, workbook, "Queries");
        Row row;
        HashMap<String, Integer> result = statisticDto.getMdrFieldsPerQuery().entrySet().stream()
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

    private void mdrFieldByName(StatisticDto statisticDto, Sheet sheet, Workbook workbook) {
        boldHeader(sheet, workbook, "MdrFields");
        Row row;
        HashMap<String, Integer> result = statisticDto.getMdrFields().entrySet().stream()
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
