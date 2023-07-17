package com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DeploymentExceptionTest
{
    @Test
    public void getErrorCodeTest() throws Exception
    {
        NovaException exception = new NovaException(DeploymentError.getUnexpectedError());
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(DeploymentError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(DeploymentError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(DeploymentError.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(DeploymentError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(DeploymentError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(DeploymentError.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(DeploymentError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(DeploymentError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }
}