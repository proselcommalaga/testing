package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils.*;
import static com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils.CSV_SEPARATOR;
import static com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants.*;

class ExportDataUtilsTest
{
    @Test
    public void when_badFormat_then_throwsException()
    {
        final String badFormat = RandomStringUtils.randomAlphabetic(8);

        Assertions.assertThrows(NovaException.class, () -> ExportDataUtils.exportValuesTo(badFormat, new String[0], new ArrayList<>()));
    }

    @Test
    public void when_excelExport_then_validateData() throws Exception
    {
        final int size = RandomUtils.nextInt(1, 10);
        final int rows = RandomUtils.nextInt(2, 10);
        final List<String[]> values = new ArrayList<>(size);
        String[] header = null;
        String[] rowValues;
        for (int i=0; i<rows; i++)
        {
            rowValues = new String[size];

            for (int j=0; j<size; j++)
            {
                rowValues[j] = RandomStringUtils.randomAlphabetic(5);
            }

            if (i==0)
            {
                header = rowValues;
            }
            else
            {
                values.add(rowValues);
            }
        }

        if (header == null) header = new String[0];

        byte[] response = ExportDataUtils.exportValuesTo(FORMAT_XLSX, header, values);
        assertRecordsOfXLSXHistorical(header, values, response);
    }

    @Test
    public void when_csvExport_then_validateData()
    {
        final int size = RandomUtils.nextInt(1, 10);
        final int rows = RandomUtils.nextInt(2, 10);
        final List<String[]> values = new ArrayList<>(size);
        String[] header = null;
        String[] rowValues;
        for (int i=0; i<rows; i++)
        {
            rowValues = new String[size];

            for (int j=0; j<size; j++)
            {
                rowValues[j] = RandomStringUtils.randomAlphabetic(5);
            }

            if (i==0)
            {
                header = rowValues;
            }
            else
            {
                values.add(rowValues);
            }
        }

        if (header == null) header = new String[0];

        byte[] response = ExportDataUtils.exportValuesTo(FORMAT_CSV, header, values);
        assertRecordsOfCSVHistorical(header, values, response);
    }

    private static void assertRecordsOfXLSXHistorical(final String[] expectedHeader, final List<String[]> mockRecords, final byte[] response) throws Exception
    {
        final Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response));
        final Sheet sheet = book.getSheet(XLSX_SHEET);

        Assertions.assertEquals(mockRecords.size() + 1, sheet.getPhysicalNumberOfRows());

        // header row
        final Row header = sheet.getRow(0);
        for (int i=0; i<expectedHeader.length; i++)
        {
            Assertions.assertEquals(expectedHeader[i], header.getCell(i).toString());
        }

        for (int i=0; i<mockRecords.size(); i++)
        {
            for (int j=0; j<mockRecords.get(i).length; j++)
            {
                Assertions.assertEquals(mockRecords.get(i)[j], sheet.getRow(i+1).getCell(j).toString());
            }
        }
    }

    private static void assertRecordsOfCSVHistorical(final String[] header, final List<String[]> mockRecords, final byte[] response)
    {
        final String[] responseLines = new String(response).split(CSV_NEW_LINE);
        Assertions.assertEquals(mockRecords.size() + 1, responseLines.length);

        for(int j=0; j<header.length; j++)
        {
            Assertions.assertEquals(String.join(CSV_SEPARATOR, header), responseLines[0]);
        }

        for (int i=1; i<mockRecords.size(); i++)
        {
            for(int j=0; j<mockRecords.get(i).length; j++)
            {
                Assertions.assertEquals(String.join(CSV_SEPARATOR, mockRecords.get(i)), responseLines[i+1]);
            }
        }
    }
}