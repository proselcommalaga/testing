package com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocSystemExceptionTest
{
    @Test
    public void getErrorCodeTest()
    {
        NovaException exception = new NovaException(DocSystemErrorCode.getUnexpectedError());
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(DocSystemErrorCode.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(DocSystemErrorCode.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(DocSystemErrorCode.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(DocSystemErrorCode.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(DocSystemErrorCode.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(DocSystemErrorCode.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(DocSystemErrorCode.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(DocSystemErrorCode.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }
}