package com.bbva.enoa.platformservices.coreservice.packsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.packsapi.util.Constants.PackConstantErrors;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

@ExposeErrorCodes
public class PackError
{

    public static NovaError getUnexpectedError()
    {
        return new NovaError(
                PackConstantErrors.PACK_ERROR_CODE_CLASS_NAME,
                PackConstantErrors.UNEXPECTED_ERROR_CODE,
                "Unexpected internal error",
                PackConstantErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    public static NovaError getNoSuchHardwarePackError()
    {
        return new NovaError(
                PackConstantErrors.PACK_ERROR_CODE_CLASS_NAME,
                PackConstantErrors.NO_SUCH_HARDWARE_ERROR_CODE,
                "The hardware pack couldn't be found",
                "Check if the hardware pack does exist in the NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchFilesystemPackError()
    {
        return new NovaError(
                PackConstantErrors.PACK_ERROR_CODE_CLASS_NAME,
                PackConstantErrors.NO_SUCH_FILE_SYSTEM_ERROR_CODE,
                "The filesystem pack couldn't be found",
                "Check if the filesystem pack does exist in the NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchBrokerPackError()
    {
        return new NovaError(
                PackConstantErrors.PACK_ERROR_CODE_CLASS_NAME,
                PackConstantErrors.NO_SUCH_BROKER_HARDWARE_ERROR_CODE,
                "The broker pack couldn't be found",
                "Check if the broker pack does exist in the NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

}
