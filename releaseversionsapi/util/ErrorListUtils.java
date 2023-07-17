package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Util class for Error lists
 * @author vbazagad
 *
 */
@Slf4j
public final class ErrorListUtils
{
    private ErrorListUtils()
    {

    }

    /**
     * Adds a validation error to the error list and logs it.
     *
     * @param errorList    List error will be added to.
     * @param serviceName  Service name.
     * @param errorCode    Error code.
     * @param errorMessage Error message.
     */
    public static void addError(List<ValidationErrorDto> errorList, String serviceName, String errorCode, String errorMessage)
    {

        ValidationErrorDto error = new ValidationErrorDto();
        error.setCode(errorCode);
        error.setMessage(errorMessage);

        errorList.add(error);

        log.warn(Constants.LOG_ERROR_MSG, serviceName, errorMessage);
    }
}
