package com.bbva.enoa.platformservices.coreservice.statisticsapi.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.Date;

@RunWith(Parameterized.class)
public class ValidDateRangeProviderTest
{
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Date NOW = new Date();
    public static final int MILLIS_IN_DAY = 86400000;
    private static final Date YESTERDAY = new Date(System.currentTimeMillis() - MILLIS_IN_DAY);
    private static final ValidDateRangeProvider validDateRangeProvider = new ValidDateRangeProvider();
    private final String testName;
    private final String startDate;
    private final String endDate;
    private final Pair<String, String> expectedResult;

    public ValidDateRangeProviderTest(String testName, String startDate, String endDate, Pair<String, String> expectedResult)
    {
        this.testName = testName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expectedResult = expectedResult;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Object[][] getParameters()
    {
        Object[][] res = null;

        Calendar calendar = Calendar.getInstance();
        YearMonth yearMonthObject = YearMonth.of(calendar.YEAR, calendar.MONTH);
        int daysInMonth = yearMonthObject.lengthOfMonth();
        if (daysInMonth == 30)
        {
            res = new Object[][]{
                    new Object[]{"When start and end dates are populated then return given date range.", "2021-01-10", "2021-01-15", Pair.of("2021-01-10", "2021-01-15")}
                    , new Object[]{"When start date is empty then return given default start date range.", "", "2021-01-15", Pair.of("2020-12-16", "2021-01-15")}
                    , new Object[]{"When start date is null then return given default start date range.", null, "2021-01-15", Pair.of("2020-12-16", "2021-01-15")}
                    , new Object[]{"When end date is empty and start date is less than 30 days ago then return (startDate, now) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), "", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), DATE_FORMAT.format(YESTERDAY))}
                    , new Object[]{"When end date is null and start date is less than 30 days ago then return (startDate, now) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), "", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), DATE_FORMAT.format(YESTERDAY))}
                    , new Object[]{"When end date is 'null' label and start date is less than 30 days ago then return (startDate, now) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), "null", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), DATE_FORMAT.format(YESTERDAY))}
                    , new Object[]{"When start date is 'null' label then return given default start date range.", "null", "2021-01-15", Pair.of("2020-12-16", "2021-01-15")}
                    , new Object[]{"When end date is empty and start date is more than 30 days ago then return (startDate, startDate + 30) range.",
                    DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), "", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))),
                    DATE_FORMAT.format(new Date(NOW.getTime() - (5L * MILLIS_IN_DAY))))}
                    , new Object[]{"When end date is null and start date is more than 30 days ago then return (startDate, startDate + 30) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), null, Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), DATE_FORMAT.format(new Date(NOW.getTime() - (5L * MILLIS_IN_DAY))))}
                    , new Object[]{"When end date is 'null' label and start date is more than 30 days ago then return (startDate, startDate + 30) range.",
                    DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), "null", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))),
                    DATE_FORMAT.format(new Date(NOW.getTime() - (5L * MILLIS_IN_DAY))))}
            };

        }
        else
        {
            res = new Object[][]{
                    new Object[]{"When start and end dates are populated then return given date range.", "2021-01-10", "2021-01-15", Pair.of("2021-01-10", "2021-01-15")}
                    , new Object[]{"When start date is empty then return given default start date range.", "", "2021-01-15", Pair.of("2020-12-16", "2021-01-15")}
                    , new Object[]{"When start date is null then return given default start date range.", null, "2021-01-15", Pair.of("2020-12-16", "2021-01-15")}
                    , new Object[]{"When end date is empty and start date is less than 30 days ago then return (startDate, now) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), "", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), DATE_FORMAT.format(YESTERDAY))}
                    , new Object[]{"When end date is null and start date is less than 30 days ago then return (startDate, now) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), "", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), DATE_FORMAT.format(YESTERDAY))}
                    , new Object[]{"When end date is 'null' label and start date is less than 30 days ago then return (startDate, now) range.", DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), "null", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (15L * MILLIS_IN_DAY))), DATE_FORMAT.format(YESTERDAY))}
                    , new Object[]{"When start date is 'null' label then return given default start date range.", "null", "2021-01-15", Pair.of("2020-12-16", "2021-01-15")}
                    , new Object[]{"When end date is empty and start date is more than 30 days ago then return (startDate, startDate + 30) range.",
                    DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), "", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))),
                    DATE_FORMAT.format(new Date(NOW.getTime() - (6L * MILLIS_IN_DAY))))}
                    , new Object[]{"When end date is null and start date is more than 30 days ago then return (startDate, startDate + 30) range.",
                    DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), null, Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), DATE_FORMAT.format(new Date(NOW.getTime() - (6L * MILLIS_IN_DAY))))}
                    , new Object[]{"When end date is 'null' label and start date is more than 30 days ago then return (startDate, startDate + 30) range.",
                    DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))), "null", Pair.of(DATE_FORMAT.format(new Date(NOW.getTime() - (35L * MILLIS_IN_DAY))),
                    DATE_FORMAT.format(new Date(NOW.getTime() - (6L * MILLIS_IN_DAY))))}
            };
        }
        return res;

    }

    @Test()
    public void runTest()
    {
        Pair<String, String> result = validDateRangeProvider.getValidDateRange(startDate, endDate);

        Assert.assertEquals(expectedResult, result);
    }


}