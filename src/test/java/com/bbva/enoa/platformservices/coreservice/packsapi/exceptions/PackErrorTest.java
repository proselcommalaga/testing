package com.bbva.enoa.platformservices.coreservice.packsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.packsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class PackErrorTest
{
    @Test
    public void getUnexpectedErrorTest()
    {
        NovaError novaError = PackError.getUnexpectedError();
        Assertions.assertAll("Error generating Pack Error Instance",
                () -> Assertions.assertEquals(Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, novaError.getHttpStatus()),
                () -> Assertions.assertEquals(ErrorMessageType.FATAL, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchHardwarePackError()
    {
        NovaError novaError = PackError.getNoSuchHardwarePackError();
        Assertions.assertAll("Error generating Pack Error Instance",
                () -> Assertions.assertEquals(Constants.PackConstantErrors.NO_SUCH_HARDWARE_ERROR_CODE, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }

    @Test
    public void getNoSuchFilesystemPackError()
    {
        NovaError novaError = PackError.getNoSuchFilesystemPackError();
        Assertions.assertAll("Error generating Pack Error Instance",
                () -> Assertions.assertEquals(Constants.PackConstantErrors.NO_SUCH_FILE_SYSTEM_ERROR_CODE, novaError.getErrorCode()),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getErrorMessage())),
                () -> Assertions.assertTrue(StringUtils.isNotBlank(novaError.getActionMessage())),
                () -> Assertions.assertEquals(HttpStatus.NOT_FOUND, novaError.getHttpStatus()),
                () -> Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType())
        );
    }
}