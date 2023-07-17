package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.exception;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants.QualityManagerErrors;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

/**
 * Error code with all the internal errors for the service
 */
@ExposeErrorCodes
public class QualityManagerErrorCode
{

    /**
     * Gets unexpected error
     * @return the Nova Error instance
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.UNEXPECTED_ERROR_CODE,
                "Unexpected internal error",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Gets invalid subsystem error
     * @return the Nova Error instance
     */
    public static NovaError getInvalidSubsystemError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.QM_SUBSYSTEM_NOT_FOUND_ERROR_CODE,
                "Cannot find subsystem",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets invalid release version error
     * @return the Nova Error instance
     */
    public static NovaError getInvalidReleaseVersionError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.QM_INVALID_RELEASE_VERSION_ERROR_CODE,
                "Invalid release version",
                "The selected release version id is not valid",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }


    /**
     * Gets calling QA service error
     * @return the Nova Error instance
     */
    public static NovaError getQANotAvailableError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.QA_NOT_AVAILABLE_ERROR_CODE,
                "Error connecting to Quality Assurance service",
                "Check service status",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets user permissions error
     * @return the Nova Error instance
     */
    public static NovaError getForbiddenError() {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.FORBIDDEN_ERROR_CODE,
                "The current user does not have permission for this operation",
                "Use a user with higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets product of subsystem not found error
     * @return the Nova Error instance
     */
    public static NovaError getProductNotFoundError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.QM_PRODUCT_NOT_FOUND_ERROR_CODE,
                "Product associated to subsystem not found in the database",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getServiceValidationError(final String errors)
    {
        return new NovaError(QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.SERVICE_VALIDATION_ERROR_CODE,
                MessageFormat.format("The following errors has been found during service validation. {0}", errors),
                "Fix errors and retry",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getMaximumSubsystemCodeAnalysisError()
    {
        return new NovaError(QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE,
                "The maximum number of quality analysis for the subsystem has been reached",
                "Remove a quality analysis and retry",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * @return a new exception
     */
    public static NovaError getSubsystemAlreadyBuildingError()
    {
        return new NovaError(QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE,
                "A request for the subsystem is already in progress",
                "Wait for this request ends or remove it",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Get invalid plan error
     * @param planId plan id
     * @return the Nova Error instance
     */
    public static NovaError getInvalidPlanError(final Integer planId)
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.PLAN_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("Plan {0} not found in the database", planId),
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets plan not deployed on PRE error
     * @return the Nova Error instance
     */
    public static NovaError getPlanNotDeployedOnPreError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.PLAN_NOT_DEPLOYED_ON_PRE_ERROR_CODE,
                "Plan is not deployed on PRE environment",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }


    public static NovaError getAllServiceTypesHaveNoQualityError()
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.ALL_SERVICE_TYPES_HAVE_NOT_QUALITY_ERROR_CODE,
                "All the services in the subsystem are of a type that does not generate quality",
                "Retry including a service of a type that generates quality",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getEphoenixPomNotFoundError(final String moduleName)
    {
        return new NovaError(
                QualityManagerErrors.QUALITY_MANAGER_ERROR_CODE_CLASS_NAME,
                QualityManagerErrors.EPHOENIX_MODULE_POM_ERROR_CODE,
                String.format("POM of module [%s] not found", moduleName),
                "Check the project repository",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }
}
