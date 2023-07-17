package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class ReleaseVersionExceptionTest
{
    @Test
    public void getErrorCodeTest() throws Exception
    {
        NovaException exception = new NovaException(ReleaseVersionError.getUnexpectedError());
        NovaError error = exception.getNovaError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(ReleaseVersionError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        Assertions.assertEquals(ReleaseVersionError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(ReleaseVersionError.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getNovaError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(ReleaseVersionError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        Assertions.assertEquals(ReleaseVersionError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(ReleaseVersionError.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getNovaError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(ReleaseVersionError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        Assertions.assertEquals(ReleaseVersionError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }
}