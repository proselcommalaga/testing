package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.exception;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class QualityManagerErrorCodeTest
{

    @Test
    void getUnexpectedError()
    {
        //NovaError as Unexpected Error
        NovaError error = QualityManagerErrorCode.getUnexpectedError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.UNEXPECTED_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
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
    void getInvalidSubsystemError()
    {
        NovaError error = QualityManagerErrorCode.getInvalidSubsystemError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.QM_SUBSYSTEM_NOT_FOUND_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertEquals(Constants.MSG_CONTACT_NOVA, error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidReleaseVersionError()
    {
        NovaError error = QualityManagerErrorCode.getInvalidReleaseVersionError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.QM_INVALID_RELEASE_VERSION_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getQANotAvailableError()
    {
        NovaError error = QualityManagerErrorCode.getQANotAvailableError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_NOT_AVAILABLE_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.CRITICAL, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getForbiddenError()
    {
        NovaError error = QualityManagerErrorCode.getForbiddenError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.FORBIDDEN_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getProductNotFoundError()
    {
        NovaError error = QualityManagerErrorCode.getProductNotFoundError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.QM_PRODUCT_NOT_FOUND_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertEquals(Constants.MSG_CONTACT_NOVA, error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getServiceValidationError()
    {
        String errorToCheck = "check_it_" + RandomStringUtils.randomAlphabetic(10);
        NovaError error = QualityManagerErrorCode.getServiceValidationError(errorToCheck);
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.SERVICE_VALIDATION_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        Assertions.assertTrue( error.getErrorMessage().contains(errorToCheck));
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getMaximumSubsystemCodeAnalysisError()
    {
        NovaError error = QualityManagerErrorCode.getMaximumSubsystemCodeAnalysisError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getSubsystemAlreadyBuildingError()
    {
        NovaError error = QualityManagerErrorCode.getSubsystemAlreadyBuildingError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getInvalidPlanError()
    {
        int planId = RandomUtils.nextInt(100, 1000);
        NovaError error = QualityManagerErrorCode.getInvalidPlanError(planId);
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.PLAN_NOT_FOUND_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        Assertions.assertTrue( error.getErrorMessage().contains(String.valueOf(planId)));
        // Action
        Assertions.assertEquals(Constants.MSG_CONTACT_NOVA, error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getPlanNotDeployedOnPreError()
    {
        NovaError error = QualityManagerErrorCode.getPlanNotDeployedOnPreError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.PLAN_NOT_DEPLOYED_ON_PRE_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertEquals(Constants.MSG_CONTACT_NOVA, error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }

    @Test
    void getAllServiceTypesHaveNoQualityError()
    {
        NovaError error = QualityManagerErrorCode.getAllServiceTypesHaveNoQualityError();
        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(QualityConstants.QualityManagerErrors.ALL_SERVICE_TYPES_HAVE_NOT_QUALITY_ERROR_CODE, error.getErrorCode());
        //Message
        Assertions.assertNotNull( error.getErrorMessage());
        // Action
        Assertions.assertNotNull(error.getActionMessage());
        //HttpStatus
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        //Type
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        //ToString never null
        Assertions.assertNotNull(error.toString());
    }
}
