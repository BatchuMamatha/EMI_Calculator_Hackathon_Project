package com.emicalc.automation.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Minimal Apache POI helper. Writes a 2D list of strings to an .xlsx file
 * with a bold first row. Used by the Home Loan year-on-year extraction test
 * and the Car Loan EMI summary test.
 */
public final class ExcelUtils {

    private static final Logger log = LogManager.getLogger(ExcelUtils.class);

    private ExcelUtils() {}

    /**
     * @param filePath   absolute or relative .xlsx path
     * @param sheetName  sheet name to write into
     * @param data       row 0 is the header, subsequent rows are data
     */
    public static void writeSheet(String filePath, String sheetName, List<List<String>> data) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("No data to write to " + filePath);
        }
        File f = new File(filePath);
        File parent = f.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Cannot create dir " + parent);
        }

        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(f)) {

            XSSFSheet sheet = wb.createSheet(sheetName);
            XSSFCellStyle headerStyle = wb.createCellStyle();
            XSSFFont bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);

            for (int r = 0; r < data.size(); r++) {
                Row row = sheet.createRow(r);
                List<String> rowData = data.get(r);
                for (int c = 0; c < rowData.size(); c++) {
                    Cell cell = row.createCell(c);
                    cell.setCellValue(rowData.get(c));
                    if (r == 0) cell.setCellStyle(headerStyle);
                }
            }
            // Auto-size each column based on widest content
            int cols = data.get(0).size();
            for (int c = 0; c < cols; c++) sheet.autoSizeColumn(c);

            wb.write(out);
            log.info("Wrote {} rows to {} (sheet '{}')", data.size(), f.getAbsolutePath(), sheetName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Excel " + filePath, e);
        }
    }

    /** Read a sheet back to validate write-back round-trips (used in assertions). */
    public static int rowCount(String filePath, String sheetName) {
        try (XSSFWorkbook wb = new XSSFWorkbook(filePath)) {
            Sheet sh = wb.getSheet(sheetName);
            return sh == null ? 0 : (sh.getLastRowNum() + 1);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel " + filePath, e);
        }
    }
}
