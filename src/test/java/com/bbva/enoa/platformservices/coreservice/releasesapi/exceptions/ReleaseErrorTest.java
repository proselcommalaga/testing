package com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


public class ReleaseErrorTest
{
    @Test
    void getClassName()
    {
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CLASS_NAME, ReleaseError.getClassName());
    }

    @Test
    void getUnexpectedError()
    {
        //NovaError as Unexpected Error
        NovaError error = ReleaseError.getUnexpectedError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, error.getErrorCode());
        //Message
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.MSG_UNEXPECTED_ERROR, error.getErrorMessage());
        // Action
        Assertions.assertEquals(Constants.MSG_CONTACT_NOVA, error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getNoSuchProductError()
    {
        //NovaError as NoSuchProductError
        NovaError error = ReleaseError.getNoSuchProductError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_PRODUCT_DOESNT_EXIST, error.getErrorCode());
        //Message, Action as not Null
        Assertions.assertNotNull( error.getErrorMessage());
        Assertions.assertNotNull( error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getNoSuchReleaseError()
    {
        //NovaError as ReleaseError
        NovaError error = ReleaseError.getNoSuchReleaseError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_NO_SUCH_RELEASE, error.getErrorCode());
        //Message and  Action as not Null
        Assertions.assertNotNull( error.getErrorMessage());
        Assertions.assertNotNull( error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getReleaseWithVersionsError()
    {
        //NovaError as ReleaseWithVersionsError
        NovaError error = ReleaseError.getReleaseWithVersionsError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_REMOVE_RELEASE_WITH_VERSION, error.getErrorCode());
        //Message and  Action as not Null
        Assertions.assertNotNull( error.getErrorMessage());
        Assertions.assertNotNull( error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getReleaseNameDuplicatedError()
    {
        //NovaError as ReleaseNameDuplicatedError
        NovaError error = ReleaseError.getReleaseNameDuplicatedError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_RELEASE_NAME_DUPLICATED, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.CONFLICT, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getReleaseNameInvalidError()
    {
        //NovaError as ReleaseNameInvalid Error
        NovaError error = ReleaseError.getReleaseNameInvalidError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_SPECIAL_CHARACTERS, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.CONFLICT, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidDateFormatError()
    {
        //NovaError as InvalidDateFormat Error
        NovaError error = ReleaseError.getInvalidDateFormatError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_DATE_NOT_VALID, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.CONFLICT, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getForbiddenError()
    {
        //NovaError as Forbidden Error
        NovaError error = ReleaseError.getForbiddenError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_USER_WITHOUT_PERMISSIONS, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidDeploymentPlatform()
    {
        //NovaError as InvalidDeploymentPlatform Error
        NovaError error = ReleaseError.getInvalidDeploymentPlatform();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.INVALID_DEPLOYMENT_PLATFORM, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidLoggingPlatform()
    {
        //NovaError as InvalidLoggingPlatform Error
        NovaError error = ReleaseError.getInvalidLoggingPlatform();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.INVALID_LOGGING_PLATFORM, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getDefaultAutoManageInPreError()
    {
        //NovaError as DefaultAutoManageInPre Error
        NovaError error = ReleaseError.getDefaultAutoManageInPreError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTOMANAGE_IN_PRE_DISABLED, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getDefaultAutoManageInProError()
    {
        //NovaError as DefaultAutoManageInPro Error
        NovaError error = ReleaseError.getDefaultAutoManageInProError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTOMANAGE_IN_PRO_DISABLED, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getDefaultAutoDeployInPreError()
    {
        //NovaError as DefaultAutoDeployInPre Error
        NovaError error = ReleaseError.getDefaultAutoDeployInPreError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTODEPLOY_IN_PRE_DISABLED, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getDefaultAutoDeployInProError()
    {
        //NovaError as DefaultAutoDeployInPro Error
        NovaError error = ReleaseError.getDefaultAutoDeployInProError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTODEPLOY_IN_PRO_DISABLED, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidDeploymentTypeError()
    {
        //NovaError as InvalidDeploymentType Error
        NovaError error = ReleaseError.getInvalidDeploymentTypeError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_INVALID_DEPLOYMENT_TYPE, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidEnvironmentError()
    {
        //NovaError as InvalidEnvironment Error
        NovaError error = ReleaseError.getInvalidEnvironmentError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_INVALID_ENVIRONMENT_TYPE, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

//    /**
//     * Environment invalid error
//     *
//     * @param status   the status of the release version
//     * @param statuses the statuses supported by release version status
//     * @return Nova Error
//     */
//    public static NovaError getInvalidReleaseVersionStatus(final String status, final String statuses)
//    {
//        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
//                ReleaseConstants.ReleaseErrors.CODE_INVALID_ENVIRONMENT_TYPE,
//                "Invalid status value provided: [" + status + "] of the release version status filter",
//                "The values supported are: [" + statuses + "]",
//                HttpStatus.BAD_REQUEST,
//                ErrorMessageType.ERROR);
//    }


    @Test
    void getInvalidReleaseVersionStatus()
    {
        String status = "OK";
        String statuses = "KO, NO_OK, BAD";

        //NovaError as InvalidEnvironment Error
        NovaError error = ReleaseError.getInvalidReleaseVersionStatus(status, statuses);
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_INVALID_RELEASE_STATUS_VALUE, error.getErrorCode());
        //Message and action as not Null
        Assertions.assertNotNull(error.getErrorMessage());
        Assertions.assertNotNull(error.getActionMessage());
        //Http Status
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }
}