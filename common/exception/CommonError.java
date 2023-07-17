package com.bbva.enoa.platformservices.coreservice.common.exception;

import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

@ExposeErrorCodes
public class CommonError
{
    private CommonError()
    {
        throw new IllegalStateException("Utility class");
    }

    public static NovaError getCallingUserAdminapiError(String className)
    {
        return new NovaError(className,
                Constants.CommonErrorConstants.FAIL_CALLING_USER_ADMIN_ERROR_CODE,
                "Fail trying to call User Admin API",
                "Check if the User Admin API is working properly",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    public static NovaError getWrongEnvironmentError(String className, String environment)
    {
        return new NovaError(className,
                Constants.CommonErrorConstants.UNKNOWN_ENVIRONMENT_ERROR_CODE,
                MessageFormat.format("Unknown environment [{0}]", environment),
                "Check if environment is set properly [INT, PRE, PRO]",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Get an error when a DocSystem with the given parameters is not found.
     *
     * @param className         The name of the class that actually generated the error.
     * @param docSystemId       The given DocSystem's ID.
     * @param docSystemCategory The given DocSystem's category.
     * @param docSystemType     The given DocSystem's type.
     * @return The error.
     */
    public static NovaError getDocSystemNotFoundError(String className, Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType)
    {
        return new NovaError(
                className,
                Constants.CommonErrorConstants.DOC_SYSTEM_NOT_FOUND_ERROR_CODE,
                String.format("Document with ID [%d], category [%s], and type [%s] does not exist.", docSystemId, docSystemCategory, docSystemType),
                "Please choose another document",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Get an error when calling another API failed.
     *
     * @param className   The name of the class that actually generated the error.
     * @param apiName     The name of the API that could not be called.
     * @param errorCode   An error code representing which API could not be called.
     * @param errorDetail Extra information about the error.
     * @return The error.
     */
    public static NovaError getErrorCallingApi(String className, String apiName, String errorCode, String errorDetail)
    {
        return new NovaError(className,
                errorCode,
                String.format("Failure trying to call [%s] API: %s", apiName, errorDetail),
                String.format("Check if [%s] API is working properly", apiName),
                HttpStatus.SERVICE_UNAVAILABLE,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Get an error when calling "File Transfer Admin API" failed.
     *
     * @param className   The name of the class that actually generated the error.
     * @param errorDetail Extra information about the error.
     * @return The error.
     */
    public static NovaError getErrorCallingFileTransferAdminApi(String className, String errorDetail)
    {
        return CommonError.getErrorCallingApi(
                className,
                "File Transfer Admin",
                Constants.CommonErrorConstants.FAILURE_CALLING_FILE_TRANSFER_ADMIN_API_ERROR_CODE,
                errorDetail);
    }

    /**
     * Get an error when calling "Documents Manager API" failed.
     *
     * @param className   The name of the class that actually generated the error.
     * @param errorDetail Extra information about the error.
     * @return The error.
     */
    public static NovaError getErrorCallingDocumentsManagerApi(String className, String errorDetail)
    {
        return CommonError.getErrorCallingApi(
                className,
                "Documents Manager",
                Constants.CommonErrorConstants.FAILURE_CALLING_DOCUMENTS_MANAGER_API_ERROR_CODE,
                errorDetail);
    }

    /**
     * Get an error when calling "Schedule Control M API" failed.
     *
     * @param className   The name of the class that actually generated the error.
     * @param errorDetail Extra information about the error.
     * @return The error.
     */
    public static NovaError getErrorCallingScheduleControlMApi(String className, String errorDetail)
    {
        return CommonError.getErrorCallingApi(
                className,
                "Schedule Control M",
                Constants.CommonErrorConstants.FAILURE_CALLING_SCHEDULE_CONTROL_M_API_ERROR_CODE,
                errorDetail);
    }

    public static NovaError getWrongNovaAgentConfigurationError(String className, String msg)
    {
        return new NovaError(
                className,
                Constants.CommonErrorConstants.WRONG_NOVA_AGENT_ERROR_CODE,
                msg,
                "Check if environment is set properly in application.yml for profile [INT, PRE, PRO]",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }


}
