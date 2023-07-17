package com.bbva.enoa.platformservices.coreservice.apigatewayapi.exception;

import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.Constants.ApiGwErrors;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

@ExposeErrorCodes
public class ApiGatewayError
{
    /**
     * Api gateway error
     */
    private static final String CLASS_NAME = "ApiGatewayError";

    public static String getClassName()
    {
        return CLASS_NAME;
    }

    public static NovaError getUnexpectedError()
    {
        return new NovaError(CLASS_NAME, ApiGwErrors.UNEXPECTED_ERROR_CODE, "Unexpected internal error", Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.FATAL);
    }

    public static NovaError getUnexpectedServiceDockerKeyError()
    {
        return new NovaError(CLASS_NAME, ApiGwErrors.SERVICE_NOT_FOUND_FOR_DOCKERKEY_ERROR_CODE, "Unexpected docker key generated. The service name does not match with any pretended service",
                Constants.MSG_CONTACT_NOVA + " and " + Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }
}
