package com.bbva.enoa.platformservices.coreservice.apigatewayapi.exception;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApiGatewayExceptionTest
{
    @Test
    public void getErrorCodeTest() throws Exception
    {
        NovaException exception = new NovaException(ApiGatewayError.getUnexpectedError());
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(ApiGatewayError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(ApiGatewayError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(ApiGatewayError.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(ApiGatewayError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(ApiGatewayError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(ApiGatewayError.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getNovaError();

        assertNotNull(error);
        assertEquals(ApiGatewayError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(ApiGatewayError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }
}
