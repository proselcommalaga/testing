package com.bbva.enoa.platformservices.coreservice.statisticsapi.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ValidDateRangeProvider
{
    private static final Logger LOG = LoggerFactory.getLogger(ValidDateRangeProvider.class);
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final int MILLIS_IN_A_DAY = 86400000;
    public static final long DAYS_BEFORE = 30L;

    public Pair<String, String> getValidDateRange(final String startDate, final String endDate)
    {
        String appliedStartDate = startDate;
        String appliedEndDate = endDate;
        final boolean existsStartDate = startDate != null && !"".equals(startDate) && !"null".equalsIgnoreCase(startDate);
        final boolean existsEndDate = endDate != null && !"".equals(endDate) && !"null".equalsIgnoreCase(endDate);
        Date today = new Date();
        final Date now = new Date(today.getTime() - MILLIS_IN_A_DAY);
        final Date defaultStartDate = new Date(now.getTime() - (DAYS_BEFORE * MILLIS_IN_A_DAY));
        if (!(existsStartDate && existsEndDate))
        {
            if (!existsStartDate && !existsEndDate)
            {
                appliedStartDate = DATE_FORMAT.format(defaultStartDate);
                appliedEndDate = DATE_FORMAT.format(now);
            }
            if (!existsStartDate && existsEndDate)
            {
                Date end = getParsedDateFrom(endDate);
                appliedStartDate = DATE_FORMAT.format(new Date(end.getTime() - (DAYS_BEFORE * MILLIS_IN_A_DAY)));
            }
            if (existsStartDate)
            {
                Date start = getParsedDateFrom(startDate);
                int daysUntilNow = (int) ((now.getTime() - start.getTime()) / MILLIS_IN_A_DAY);
                appliedEndDate = DATE_FORMAT.format(daysUntilNow < DAYS_BEFORE ? now : new Date(start.getTime() + (DAYS_BEFORE * MILLIS_IN_A_DAY)));
            }
        }
        return Pair.of(appliedStartDate, appliedEndDate);
    }

    private Date getParsedDateFrom(String date)
    {
        Date parsedDate;
        try
        {
            parsedDate = DATE_FORMAT.parse(date);
        }
        catch (ParseException e)
        {
            LOG.error("[ValidDateRangeProvider]: [getParsedDateFrom] -> Error parsing date " + date + ": " + e.getMessage());
            parsedDate = new Date();
        }
        return parsedDate;
    }
}
