package com.bbva.enoa.platformservices.coreservice.apigatewayapi.exception;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ApiGatewayErrorTest
{
    @Test
    public void getHttpStatus()
    {
        NovaError error = ApiGatewayError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getErrorCode()
    {
        NovaError error = ApiGatewayError.getUnexpectedError();

        String response = error.getErrorCode();

        assertNotNull(response);
        assertEquals("APIGATEWAY-000", response);
    }

    @Test
    public void getErrorMessage()
    {
        NovaError error = ApiGatewayError.getUnexpectedError();

        String response = error.getErrorMessage();

        assertNotNull(response);
        assertEquals("Unexpected internal error", response);
    }

    @Test
    public void getActionMessage()
    {
        NovaError error = ApiGatewayError.getUnexpectedError();

        String response = error.getActionMessage();

        assertNotNull(response);
        assertEquals("Please, contact the NOVA Support team via JIRA: PNOVA", response);
    }

    @Test
    public void getErrorMessageTypeTest()
    {
        NovaError error = ApiGatewayError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    public void toStringTest()
    {
        NovaError error = ApiGatewayError.getUnexpectedError();

        assertNotNull(error);
        assertEquals("ApiGatewayError{errorCode=APIGATEWAY-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Support team via JIRA: PNOVA', "
                + "httpStatus='500', errorMessageType='FATAL'}", error.toString());
    }
}
