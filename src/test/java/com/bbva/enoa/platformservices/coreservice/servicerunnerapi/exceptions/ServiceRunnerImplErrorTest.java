package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServiceRunnerImplErrorTest
{

    @Test
    public void getHttpStatus()
    {
        NovaError error = ServiceRunnerError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getErrorCode()
    {
        NovaError error = ServiceRunnerError.getUnexpectedError();

        String response = error.getErrorCode();

        assertNotNull(response);
        assertEquals("SERVICERUNNER-000", response);
    }

    @Test
    public void getErrorMessage()
    {
        NovaError error = ServiceRunnerError.getUnexpectedError();

        String response = error.getErrorMessage();

        assertNotNull(response);
        assertEquals("Unexpected internal error", response);
    }

    @Test
    public void getActionMessage()
    {
        NovaError error = ServiceRunnerError.getUnexpectedError();

        String response = error.getActionMessage();

        assertNotNull(response);
        assertEquals("Please, contact the NOVA Support team via JIRA: PNOVA", response);
    }

    @Test
    public void getErrorMessageTypeTest()
    {
        NovaError error = ServiceRunnerError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    public void toStringTest()
    {
        NovaError error = ServiceRunnerError.getUnexpectedError();

        assertNotNull(error);
        assertEquals("ServiceRunnerErrors{errorCode=SERVICERUNNER-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Support team via JIRA: PNOVA', "
                + "httpStatus='500', errorMessageType='FATAL'}", error.toString().trim());
    }


}