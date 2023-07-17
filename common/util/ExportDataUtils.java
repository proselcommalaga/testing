package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Util class for exporting data to CSV and Excel
 */
@Slf4j
public final class ExportDataUtils
{
    /**
     * Format for exporting CSV file
     */
    public static final String FORMAT_CSV = "csv";

    /**
     * Format for exporting XLSX file
     */
    public static final String FORMAT_XLSX = "xlsx";

    /**
     * XLSX Sheet name
     */
    public static final String XLSX_SHEET = "data";

    /**
     * CSV separator
     */
    public static final String CSV_SEPARATOR = ";";

    /**
     * CSV new line
     */
    public static final String CSV_NEW_LINE = "\n";

    /**
     * Private constructor
     */
    private ExportDataUtils()
    {
        // nothing to do
    }

    /**
     * Method to export a list of String array to CSV or EXCEL file
     *
     * @param format  csv or excel format
     * @param header  the header of the rows
     * @param rowList the list of rows
     * @return bytes with the file
     */
    public static byte[] exportValuesTo(final String format, final String[] header, final List<String[]> rowList)
    {
        final byte[] fileInBytes;
        if (FORMAT_CSV.equalsIgnoreCase(format))
        {
            // create CSV file
            fileInBytes = exportToCSV(header, rowList);
        }
        else if (FORMAT_XLSX.equalsIgnoreCase(format))
        {
            // create XLS file
            fileInBytes = exportToXLSX(header, rowList);
        }
        else
        {
            throw new NovaException(StatisticsError.getBadFormatExportError(format));
        }

        return fileInBytes;
    }

    private static byte[] exportToCSV(final String[] header, final List<String[]> rowList)
    {
        final StringBuilder csv = new StringBuilder(String.join(CSV_SEPARATOR, header) + CSV_NEW_LINE);

        for (String[] row : rowList)
        {
            csv.append(String.join(CSV_SEPARATOR, row)).append(CSV_NEW_LINE);
        }
        log.debug("[ExportDataUtils] -> [exportToCSV] values [{}]", Arrays.toString(csv.toString().getBytes()));

        return csv.toString().getBytes();
    }

    private static byte[] exportToXLSX(final String[] header, final List<String[]> rowList)
    {
        final Workbook workbook = new XSSFWorkbook();
        final Sheet sheet = workbook.createSheet(XLSX_SHEET);

        // Create the header
        final Row headerRow = sheet.createRow(0);
        Cell cell;
        for (int i = 0; i < header.length; i++) {
            cell = headerRow.createCell(i);
            cell.setCellValue(header[i]);
        }

        int rowNum = 1;
        Row row;
        for (String[] rowData : rowList)
        {
            row = sheet.createRow(rowNum++);
            for (int i=0; i<rowData.length; i++)
            {
                row.createCell(i).setCellValue(rowData[i]);
            }
        }

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            // to byte array
            workbook.write(bos);
            return bos.toByteArray();
        }
        catch (IOException e)
        {
            throw new NovaException(StatisticsError.getCreatingXLSXError(), e);
        }
    }
}
