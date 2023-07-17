package com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StatisticsExceptionTest
{
    @Test
    void getErrorCodeTest()
    {
        NovaException exception = new NovaException(StatisticsError.getUnexpectedError());
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(StatisticsError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(StatisticsError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(StatisticsError.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(StatisticsError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(StatisticsError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(StatisticsError.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(StatisticsError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(StatisticsError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }
}