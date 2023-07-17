package com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class BudgetsExceptionTest
{
    @Test
    void getErrorCodeTest()
    {
        NovaException exception = new NovaException(BudgetsError.getUnexpectedError());
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(BudgetsError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(BudgetsError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(BudgetsError.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(BudgetsError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(BudgetsError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(BudgetsError.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(BudgetsError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(BudgetsError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }
}
