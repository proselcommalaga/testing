package com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import static com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants.BehaviorErrors.*;

/**
 * Error code with all the internal errors for the service.
 */
@ExposeErrorCodes
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BehaviorError
{

    public static String getClassName()
    {
        return Constants.BehaviorErrors.CLASS_NAME;
    }

    /**
     * Unexpected error
     *
     * @return Nova Error
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                UNEXPECTED_ERROR_CODE,
                MSG_UNEXPECTED_ERROR,
                com.bbva.enoa.platformservices.coreservice.common.Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * No Product
     *
     * @return Nova Error
     */
    public static NovaError getNoSuchProductError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_PRODUCT_DOESNT_EXIST,
                "The given product doesn't exist",
                "Check the product",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Product without subsystem error
     *
     * @return Nova Error
     */
    public static NovaError getProductWithoutSubsystemsError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_PRODUCT_NOT_SUBSYSTEM,
                "Tried to add a behavior version of a product without subsystems",
                "First of all add subsystems to the product",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Maximum Versions Limit Error
     *
     * @return Nova Error
     */
    public static NovaError getMaxVersionsLimitError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_MAX_VERSIONS_LIMIT,
                "The maximum limit of release versions has been reached",
                "Archive an old release version",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Maximum Version Compiling Error
     *
     * @return Nova Error
     */
    public static NovaError getMaxVersionCompilingError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_MAX_VERSIONS_COMPILING,
                "The maximum limit of compiling versions has been reached",
                "Please wait till the compiling process ends",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Subsystem tag has no services
     *
     * @return Nova Error
     */
    public static NovaError getSubsystemTagWithoutServicesError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_TAG_WITHOUT_SERVICE,
                "In the TAGs selected, there is not any NOVA project with NOVA services type in the subsystem",
                "Please, review the TAG of this subsystem and ensure that contains some project following the NOVA service conditions. You can use NOVA CLI tool for generating a NOVA Service/project scaffold." +
                        "You can find all info about generating NOVA Service into NOVA Site codelabs: https://platform.bbva.com/codelabs/nova/Codelabs#/",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBehaviorVersionNameDuplicatedError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_DUPLICATED_VERSION_NAME,
                "There is a behavior version with the same name in the product",
                "Try with another behavior version name",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Environment invalid error
     *
     * @param status   the status of the behavior version
     * @param statuses the statuses supported by behavior version status
     * @return Nova Error
     */
    public static NovaError getInvalidBehaviorVersionStatus(final String status, final String statuses)

    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_INVALID_BEHAVIOR_VERSION_STATUS_VALUE,
                "Invalid status value provided: [" + status + "] of the behavior version status filter",
                "The values supported are: [" + statuses + "]",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * No such behavior version
     *
     * @return Nova Error
     */
    public static NovaError getNoSuchBehaviorVersionError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_NO_SUCH_BEHAVIOR_VERSION,
                "The behavior version couldn't be found",
                "Check if the behavior version exists in NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getGroupIdNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_GROUP_ID_NOT_FOUND,
                "The group id of the service does not found. Is null or empty.",
                "Review the nova.yml or pom.xml and set the group id or equivalent value.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getArtifactIdNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_ARTIFACT_ID_NOT_FOUND,
                "The artifact id of the service does not found. Is null or empty.",
                "Review the nova.yml or pom.xml and set the artifact id or equivalent value.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getVersionNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_VERSION_NOT_FOUND,
                "The version of the service does not found. Is null or empty.",
                "Review the nova.yml or pom.xml and set the version or equivalent value.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getVersionNameNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_VERSION_NAME_NOT_FOUND,
                "The behavior version name of the service does not found. Is null or empty.",
                "Contact the Nova Admin team",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getServiceNameNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_SERVICE_NAME_NOT_FOUND,
                "The service name of the service does not found. Is null or empty.",
                "Review the nova.yml or pom.xml and set the service name or equivalent value.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getServiceTypeNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_SERVICE_TYPE_NOT_FOUND,
                "The service type of the service does not found. Is null or empty.",
                "Review the nova.yml or pom.xml and set the service type or equivalent value.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getBehaviorVersionWithoutSubsystem()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_VERSION_WITHOUT_SUBSYSTEM,
                "Behavior version without subsystem",
                "Review your behavior version or your subsystem and try again.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getSubsystemWithoutServicesError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_SUBSYSTEM_WITHOUT_SERVICE,
                "empty subsystem selected to  empty",
                "Review subsystems selected into behavior Version and try again.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getDuplicatedServiceNameError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_DUPLICATE_SERVICE_NAME,
                "Invalid behavior version. A service name is duplicated",
                "Please check the service names",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getProductSubsystemNotFoundError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_PRODUCT_SUBSYSTEM_NOT_FOUND,
                "Missed product subsystem in creation of a Behavior version",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getNullProductSubsystemIdError()
    {
        return new NovaError(CLASS_NAME,
                CODE_NULL_PRODUCT_SUBSYSTEM_ID,
                "A null id of a product subsystem has been passed in creation of a Behavior version",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * No such behavior subsystem.
     *
     * @return Nova Error.
     */
    public static NovaError getNoSuchBehaviorSubsystemError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_NO_SUCH_BEHAVIOR_VERSION,
                "The behavior subsystem couldn't be found",
                "Check if the behavior subsystem exists in NOVA",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBehaviorVersionStateBuildingError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BEHAVIOR_VERSION_BUILDING,
                "There is a behavior version with building state",
                "Please wait till the compiling process ends",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getBehaviorSubsystemStatePendingError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BEHAVIOR_SUBSYSTEM_PENDING,
                "There is a behavior subsystem with pending state",
                "Review your behavior version or your subsystem and try again",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getBehaviorServiceConfigurationEditingError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BEHAVIOR_CONFIGURATION_EDITING,
                "There is a behavior configuration with editing state",
                "Please wait till the configuration process ends",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getBehaviorInstanceStateStartingError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BEHAVIOR_INSTANCE_EDITING,
                "There is a behavior instance with starting state",
                "Please wait till the behavior isntance process ends",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }


    public static NovaError getBadParametersError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BAD_ARGUMENTS,
                "The request parameters are not valid.",
                "Please check the provided parameters in the request",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.CRITICAL);
    }

    public static NovaError getBadParametersError(Integer behaviorServiceId)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BAD_ARGUMENTS,
                String.format("The provided behavior service could not be found in database: %s.", behaviorServiceId),
                "Please check the provided parameters in the request",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.CRITICAL);
    }

    public static NovaError getResourceNotFoundError(String resourceName, String resources)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_RESOURCE_NOT_FOUND,
                String.format("The requested resources [%s] could not be found in database with the provided information: [%s].", resourceName, resources),
                "Please check if it exists and try again",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.CRITICAL);
    }

    public static NovaError getNoResourceAvailableError(String resource)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_RESOURCE_NOT_FOUND,
                String.format("The request resource [%s] is not available.", resource),
                "Please, check the requested resource and report it to the NOVA Team",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.CRITICAL);
    }

    public static NovaError getApiCommunicationError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                UNEXPECTED_ERROR_CODE,
                MSG_UNEXPECTED_COMMUNICATION_ERROR,
                com.bbva.enoa.platformservices.coreservice.common.Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    public static NovaError getBadPropertiesProcessingError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_BAD_PROCESSING,
                "Bad properties processing exception, could be wrong properties value",
                com.bbva.enoa.platformservices.coreservice.common.Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING);
    }

    public static NovaError getAlreadyConfiguredServiceError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_ALREADY_CONFIGURED_SERVICE,
                "This behavior environment configuration is already saved without any change",
                "Please, modify at least one configurable resource in your service to modify the behavior environment",
                HttpStatus.NOT_ACCEPTABLE,
                ErrorMessageType.WARNING);
    }

    /**
     * No initiatives for product.
     *
     * @return Nova Error
     */
    public static NovaError getNoSuchInitiativesError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                CODE_NO_INITIATIVES_FOUND,
                "The given product has no initiatives",
                "Check the product initiatives",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }


    /**
     * No such behavior service configuration
     *
     * @param bsConfigurationId behavior service configuration identifier
     * @return error
     */
    public static NovaError getNoSuchBSConfigurationError(final Integer bsConfigurationId)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                BS_CONFIGURATION_NOT_FOUND_ERROR_CODE,
                String.format(CONFIGURATION_NOT_FOUND_ERROR_MESSAGE, bsConfigurationId),
                CONFIGURATION_NOT_FOUND_ACTION,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * User is not authorized to do current action
     *
     * @return error
     */
    public static NovaError getUserNotAuthorizedError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME, CODE_FORBIDDEN_ERROR,
                "The user is not authorized to execute start/stop behavior instances.",
                "Review users permissions for starting/stopping behavior service in this environment.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBehaviorInstanceNotFoundError(int behaviorExecutionId)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                BEHAVIOR_INSTANCE_NOT_FOUND,
                String.format("Behavior instance not found in NOVA database for execution id [{}].", behaviorExecutionId),
                "Check if the instance request is send correctly and if there are no problems with persistence services.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBehaviorInstanceByIdNotFoundError(int behaviorInstanceId)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                BEHAVIOR_INSTANCE_NOT_FOUND,
                String.format("Behavior instance not found in NOVA database for id [{}].", behaviorInstanceId),
                "Check if the instance request is send correctly and if there are no problems with persistence services.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBehaviorGenericNovaAgentError(String errorCode, String message)
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME,
                errorCode,
                message,
                "",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for product without canon
     *
     * @return a NovaError
     */
    public static NovaError getProductWithoutNovaServicesCanonError()
    {
        return new NovaError(Constants.BehaviorErrors.CLASS_NAME, PRODUCT_WITHOUT_NOVA_SERVICES_CANON,
                "The operation cannot be done because the product has no Behavior Test services GB", "Add the Behavior Test services GBs to the budget of the product", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

}
