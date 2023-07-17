package com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DocSystemErrorCodeTest
{
    @Test
    public void getHttpStatus()
    {
        NovaError error = DocSystemErrorCode.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getErrorCode()
    {
        NovaError error = DocSystemErrorCode.getUnexpectedError();

        String response = error.getErrorCode();

        assertNotNull(response);
        assertEquals("DOCSYSTEMS-000", response);
    }

    @Test
    public void getErrorMessage()
    {
        NovaError error = DocSystemErrorCode.getUnexpectedError();

        String response = error.getErrorMessage();

        assertNotNull(response);
        assertEquals("Unexpected internal error", response);
    }

    @Test
    public void getActionMessage()
    {
        NovaError error = DocSystemErrorCode.getUnexpectedError();

        String response = error.getActionMessage();

        assertNotNull(response);
        assertEquals("Please, contact the NOVA Admin team", response);
    }

    @Test
    public void getErrorMessageTypeTest()
    {
        NovaError error = DocSystemErrorCode.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    public void toStringTest()
    {
        NovaError error = DocSystemErrorCode.getUnexpectedError();

        assertNotNull(error);
        assertEquals("DocSystemErrorCode{errorCode=DOCSYSTEMS-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Admin team', "
                        + "httpStatus='500', errorMessageType='FATAL'}",
                error.toString());
    }
}