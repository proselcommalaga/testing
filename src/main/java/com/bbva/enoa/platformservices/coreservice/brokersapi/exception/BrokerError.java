package com.bbva.enoa.platformservices.coreservice.brokersapi.exception;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

/**
 * Errors for BrokersApi
 */
@ExposeErrorCodes
public class BrokerError
{
    /**
     * The constant TRY_AGAIN_OR_CONTACT_SUPPORT.
     */
    public static final String TRY_AGAIN_OR_CONTACT_SUPPORT = "Please try again, or contact the NOVA Support team";

    /**
     * Unexpected error
     *
     * @return error unexpected error
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.UNEXPECTED_ERROR_CODE,
                BrokerConstants.BrokerErrorCode.UNEXPECTED_ERROR_MSG,
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Gets permission denied error.
     *
     * @return a new exception
     */
    public static NovaError getPermissionDeniedError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.PERMISSION_DENIED_ERROR_CODE,
                "User does not have permission for the action requested",
                "Check if the user belongs to the group authorized for this action",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Broker not found error
     *
     * @param brokerId the missing broker id
     * @return error broker not found error
     */
    public static NovaError getBrokerNotFoundError(final Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NOT_FOUND_ERROR_CODE,
                String.format("The broker id: [%d] was not found in NOVA database", brokerId),
                "Review broker id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Unexpected broker status error
     *
     * @param expectedStatus expected status
     * @return error unexpected broker status error
     */
    public static NovaError getUnexpectedBrokerStatusError(final String expectedStatus)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_STATUS_ERROR_CODE,
                String.format("Broker is in an unexpected status for executing the action. Status must be [%s]", expectedStatus),
                "Check broker status allows the action",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Unexpected broker node status error
     *
     * @param expectedStatus expected status
     * @return error unexpected broker node status error
     */
    public static NovaError getUnexpectedBrokerNodeStatusError(final String expectedStatus)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_NODE_STATUS_ERROR_CODE,
                String.format("Broker node is in an unexpected status for executing the action. Status must be [%s]", expectedStatus),
                "Check broker node status allows the action",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Get broker operation not valid error
     *
     * @param invalidOperation the invalid operation
     * @return a new exception
     */
    public static NovaError getBrokerOperationNotValidError(String invalidOperation)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_OPERATION_NOT_VALID_ERROR_CODE,
                MessageFormat.format("Broker operation {0} not valid", invalidOperation),
                "Check if broker operation being notified is on the valid list",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Get broker node operation not valid error
     *
     * @param invalidOperation the invalid operation
     * @return a new exception
     */
    public static NovaError getBrokerNodeOperationNotValidError(String invalidOperation)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NODE_OPERATION_NOT_VALID_ERROR_CODE,
                MessageFormat.format("Broker node operation {0} not valid", invalidOperation),
                "Check if broker node operation being notified is on the valid list",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Product not found error
     *
     * @param productId the missing product id
     * @return error product not found error
     */
    public static NovaError getProductNotFoundError(final Integer productId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.PRODUCT_NOT_FOUND_ERROR_CODE,
                String.format("The product id [%d] was not found in NOVA database", productId),
                "Review product id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Filesystem not found error
     *
     * @param filesystemId the missing filesystem id
     * @return error filesystem not found error
     */
    public static NovaError getFilesystemNotFoundError(final Integer filesystemId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FILESYSTEM_NOT_FOUND_ERROR_CODE,
                String.format("The filesystem id [%d] was not found in NOVA database", filesystemId),
                "Review filesystem id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Invalid filesystem error
     *
     * @param filesystemId the invalid filesystem id
     * @return error invalid filesystem error
     */
    public static NovaError getInvalidFilesystemError(final Integer filesystemId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.INVALID_FILESYSTEM_ERROR_CODE,
                String.format("Given filesystem product, environment, platform or current status is not valid (id [%d])", filesystemId),
                "Review filesystem id is correct",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Hardware pack not found error
     *
     * @param hardwarePackId the missing hardware pack id
     * @return error hardware pack not found error
     */
    public static NovaError getHardwarePackNotFoundError(final Integer hardwarePackId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.HARDWAREPACK_NOT_FOUND_ERROR_CODE,
                String.format("The hardware pack id [%d] was not found in NOVA database", hardwarePackId),
                "Review hardware pack id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Invalid hardware pack error
     *
     * @param hardwarePackId the invalid hardwarePack id
     * @return error invalid hardware pack error
     */
    public static NovaError getInvalidHardwarePackError(final Integer hardwarePackId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.INVALID_HARDWAREPACK_ERROR_CODE,
                String.format("Given hardware pack type is not valid (id [%d])", hardwarePackId),
                "Review hardware pack id is correct",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }


    /**
     * Broker type not supported
     *
     * @param brokerType the broker type
     * @return error unsupported broker type or platform error
     */
    public static NovaError getUnsupportedBrokerTypeError(final String brokerType)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.UNSUPPORTED_BROKER_TYPE_ERROR_CODE,
                MessageFormat.format("Given broker type [{0}] is not supported", brokerType),
                "Review broker type ",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Platform not supported
     *
     * @param platform given platform
     * @return error unsupported broker type or platform error
     */
    public static NovaError getUnsupportedBrokerPlatformError(final String platform)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.UNSUPPORTED_BROKER_PLATFORM_ERROR_CODE,
                MessageFormat.format("Given broker platform [{0}] is not supported", platform),
                "Review broker platform",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Get max. number of brokers reached error
     *
     * @return a new exception
     */
    public static NovaError getMaxBrokersReachedError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.MAX_BROKERS_BY_ENV_LIMIT_REACHED_ERROR_CODE,
                "Reached max. number of brokers allowed for a product in an environment",
                "Delete any unused existing broker for this environment, or use it",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Given broker name already exists error
     *
     * @param brokerName broker name
     * @return error given broker name already exists error
     */
    public static NovaError getGivenBrokerNameAlreadyExistsError(String brokerName)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NAME_ALREADY_EXISTS_IN_ENV_ERROR_CODE,
                MessageFormat.format("Given broker name [{0}] already exists for this product and environment", brokerName),
                "Choose another name for the new broker",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Invalid broker name error
     *
     * @param brokerName broker name
     * @return error invalid broker name error
     */
    public static NovaError getInvalidBrokerNameError(String brokerName)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.INVALID_BROKER_NAME_ERROR_CODE,
                MessageFormat.format("Given broker name [{0}] is not valid", brokerName),
                "Choose another name with lowercase letters and/or numbers",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.createBroker
     *
     * @param brokerId the brokerId
     * @return error failed broker deployment create error
     */
    public static NovaError getFailedBrokerDeploymentCreateError(Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_CREATION_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.createBroker for broker Id [%d]", brokerId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.deleteBroker
     *
     * @param brokerId the brokerId
     * @return error failed broker deployment delete error
     */
    public static NovaError getFailedBrokerDeploymentDeleteError(Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_DELETION_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.deleteBroker for broker Id [%d]", brokerId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.startBroker
     *
     * @param brokerId the brokerId
     * @return error failed broker deployment start error
     */
    public static NovaError getFailedBrokerDeploymentStartError(Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_START_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.startBroker for broker Id [%d]", brokerId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.stopBroker
     *
     * @param brokerId the brokerId
     * @return error failed broker deployment stop error
     */
    public static NovaError getFailedBrokerDeploymentStopError(Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_STOP_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.stopBroker for broker Id [%d]", brokerId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.restartBroker
     *
     * @param brokerId the brokerId
     * @return error failed broker deployment restart error
     */
    public static NovaError getFailedBrokerDeploymentRestartError(Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_RESTART_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.restartBroker for broker Id [%d]", brokerId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.startBrokerNode
     *
     * @param brokerNodeId the broker node id
     * @return error failed broker node deployment start error
     */
    public static NovaError getFailedBrokerNodeDeploymentStartError(Integer brokerNodeId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_START_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.startBrokerNode for broker node id [%d]", brokerNodeId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.stopBrokerNode
     *
     * @param brokerNodeId the broker node id
     * @return error failed broker node deployment stop error
     */
    public static NovaError getFailedBrokerNodeDeploymentStopError(Integer brokerNodeId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_STOP_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.stopBrokerNode for broker node id [%d]", brokerNodeId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * An error occurred while calling BrokerDeploymentAPI.restartBrokerNode
     *
     * @param brokerNodeId the broker node id
     * @return error failed broker node deployment restart error
     */
    public static NovaError getFailedBrokerNodeDeploymentRestartError(Integer brokerNodeId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_RESTART_ERROR_CODE,
                String.format("Error calling BrokerDeploymentAPI.restartBrokerNode for broker node id [%d]", brokerNodeId),
                TRY_AGAIN_OR_CONTACT_SUPPORT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Gets not enough broker budget error.
     *
     * @param environment the environment
     * @return the error code
     */
    public static NovaError getNotEnoughBrokerBudgetError(final String environment)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.NOT_ENOUGH_BROKER_BUDGET_ERROR,
                MessageFormat.format("The product in the environment {0} has not enough budget to create this broker", environment),
                "Request more budget for brokers to NOVA Administrators for this product and this environment.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets credentials not found in database for broker role error.
     *
     * @param role     the role
     * @param brokerId the broker id
     * @return the error code
     */
    public static NovaError getCredentialsNotFoundForRoleError(final String role, Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_CREDENTIALS_NOT_FOUND_ERROR,
                MessageFormat.format("The broker user or password credential for role {0} has not been found in database for brokerId {1}", role, brokerId),
                "Contact with NOVA Administrators and ask them to check broker credentials for this broker.",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets impossible stop broker used by running services error
     *
     * @param brokerName the broker name
     * @return the error code
     */
    public static NovaError getBrokerUsedByRunningServicesError(final String brokerName)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.IMPOSSIBLE_STOP_BROKER_USED_BY_RUNNING_SERVICES_ERROR,
                MessageFormat.format("Impossible do the action over the broker {0}, because is being used by running services", brokerName),
                "Check your environments and stop all services that have been using the broker to perform that action over the broker",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets impossible delete broker used by services error
     *
     * @param brokerName the broker name
     * @return the error code
     */
    public static NovaError getBrokerUsedByServicesError(final String brokerName)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.IMPOSSIBLE_STOP_BROKER_USED_BY_DEPLOYMENT_SERVICES_ERROR,
                MessageFormat.format("Impossible do the action over the broker {0}, because is being used by at least one service in a deployment plan", brokerName),
                "Please, review if there is any deployment plan using this broker",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets not valid platform error
     *
     * @param platform the platform name
     * @return the error code
     */
    public static NovaError getNotValidPlatformError(final Platform platform)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.NOT_VALID_PLATFORM_ERROR,
                MessageFormat.format("Impossible do action over given platform: {0}", platform),
                "Please, review if the platform name is correct",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error no broker in pro with the same name
     *
     * @param brokerName broker name
     * @return error error creation broker no broker with name on environment
     */
    public static NovaError getErrorCreationBrokerNoBrokerWithNameOnEnvironment(final String brokerName)
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.IMPOSSIBLE_CREATE_DUE_TO_NO_SAME_BROKER,
                "Can not create broker with name: [" + brokerName + "] in Production, is mandatory to have a broker with the same name in preproduction",
                "Before trying to create de Broker in production, create one with the same name in Preproduction",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets error calling broker agent on environment.
     *
     * @param environment the environment
     * @param msg         the msg
     * @return the error calling broker agent on environment
     */
    public static NovaError getErrorCallingBrokerAgentOnEnvironment(final String environment, final String msg)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_AGENT_ERROR,
                "Error calling NOVA Broker Agent service into the environment: [" + environment + "]. Error: [" + msg + "]",
                "Deployments with brokers will not be available. Wait several minutes and try it again. If the problems continues, please, contact with NOVA Platform Support",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets broker not found in deployment service.
     *
     * @param deploymentServiceId the deployment service id
     * @return the broker not found in deployment service
     */
    public static NovaError getBrokerNotFoundInDeploymentService(final Integer deploymentServiceId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NOT_FOUND_IN_DEPLOYMENT_SERVICE,
                "Error getting brokers in deployment service: [" + deploymentServiceId + "]. This service haven`t got any broker attached.",
                "The deployment service must have at least one broker attached to perform this action",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets user admin not found in broker.
     *
     * @param brokerId the broker id
     * @return the user admin not found in broker
     */
    public static NovaError getUserAdminNotFoundInBroker(final Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.USER_ADMIN_NOT_FOUND_IN_BROKER,
                "Error getting user Admin in broker: [" + brokerId + "]. This broker haven`t got any user admin created.",
                "The broker must have a user admin to perform this action. If the problems continues, please, contact with NOVA Platform Support",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets invalid inputs for convention name creation.
     *
     * @param methodName the method name
     * @return the invalid inputs for convention name creation
     */
    public static NovaError getInvalidInputsForConventionNameCreation(String methodName)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.INVALID_INPUT_FOR_CONVENTION_NAME_CREATION,
                "Error creating object name with convention in method: [" + methodName + "]",
                "Please, contact with NOVA Platform Support",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Broker node not found error
     *
     * @param brokerNodeId the missing broker node id
     * @return broker not found error
     */
    public static NovaError getBrokerNodeNotFoundError(final Integer brokerNodeId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NODE_NOT_FOUND_ERROR_CODE,
                String.format("The broker node id: [%d] was not found in NOVA database", brokerNodeId),
                "Review broker node id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }


    /**
     * Gets broker node is in transitory status error
     *
     * @return the error code
     */
    public static NovaError getBrokerNodeInTransitoryStatusError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NODE_IN_TRANSITORY_STATUS_ERROR,
                "A node is finishing an operation",
                "Wait node operation ends and retry",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Number of nodes not valid or supported
     *
     * @param numberOfNodes given number of nodes
     * @return unsupported broker type or platform error
     */
    public static NovaError getUnsupportedNumberOfNodesError(final Integer numberOfNodes)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.UNSUPPORTED_NUMBER_OF_NODES_ERROR_CODE,
                MessageFormat.format("Given number of nodes [{0}] is not valid or supported", numberOfNodes),
                "Choose a number of nodes valid for deployment infrastructure configured at product",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Broker is not successfully created or is being deleted
     *
     * @return error broker not created and stable error
     */
    public static NovaError getBrokerNotCreatedAndStableError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_NOT_CREATED_AND_STABLE_ERROR_CODE,
                "Broker is not successfully created or is being deleted",
                "Check broker status allows the action",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Error not same number of nodes as broker in preproduction
     *
     * @return error not same number of nodes as preproduction
     */
    public static NovaError getErrorNotSameNumberOFNodesAsPreproduction()
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.NOT_SAME_NUMBER_OF_NODES_AS_PREPRODUCTION_ERROR_CODE,
                "It is not possible to create a broker in Production with a different number of nodes than the broker with the same name in Preproduction",
                "Try to create again the broker choosing the same number of nodes as the broker in Preproduction with the same name",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets can't operate over broker error.
     *
     * @return the cant operate over broker error
     */
    public static NovaError getCantOperateOverBrokerError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.CANT_RETRIEVE_INFORMATION_ON_STOPPED_BROKER_CODE,
                "Broker is not running, so information can´t be retrieved.",
                "Please, start broker if you want to operate and get information.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getGenericNovaAgentError(String errorCode, String message)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                errorCode,
                message,
                "",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets database broker alert config error.
     *
     * @param brokerId the broker id
     * @return broker alert config database error
     */
    public static NovaError getDatabaseBrokerAlertConfigError(final Integer brokerId)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.DATABASE_BROKER_ALERT_CONFIG_ERROR,
                MessageFormat.format("The broker alert configuration could not be save due to a database exception for broker id: {0}", brokerId),
                "Refresh or update the broker and try it again. If the problem persist, please, contact the NOVA Support team",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Get broker does not have a valid configuration error for a specific alert type
     *
     * @param brokerId  the broker id
     * @param alertType the alert type
     * @return alert config occurrences not valid error
     */
    public static NovaError getAlertConfigOccurrencesNotValidError(Integer brokerId, String alertType)
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.ALERT_CONFIG_OCCURRENCES_NOT_VALID_ERROR_CODE,
                MessageFormat.format("Broker id {0} does not have one and only one alert configuration of type {1}", brokerId, alertType),
                "Please, contact the NOVA Support team",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    public static NovaError generateInstanceNoValidEmail()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.EMAIL_INVALID_ERROR_CODE,
                "No valid email address",
                "Change email address for a valid one",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError generateInvalidQueueThreshold()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.QUEUE_THRESHOLD_NOT_VALID_ERROR_CODE,
                "Queue threshold must be a greater than 0 messages",
                "Change threshold for a valid one",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError generateInvalidPublishRateThreshold()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.PUBLISH_RATE_THRESHOLD_NOT_VALID_ERROR_CODE,
                "Publish rate threshold must be a greater or equal than 0 messages/minute",
                "Change threshold for a valid one",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError generateInvalidConsumerRateThreshold()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.CONSUMER_RATE_THRESHOLD_NOT_VALID_ERROR_CODE,
                "Consumer rate threshold must be a greater than 0 messages/minute",
                "Change threshold for a valid one",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError generateProductWithoutRemedyGroupError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.PRODUCT_HAS_NOT_REMEDY_GROUP_ERROR_CODE,
                "Product must have configured a remedy group for using notifications by Patrol",
                "Set a valid remedy group in product configuration or uncheck Patrol notification/s",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError generateEnvironmentError()
    {
        return new NovaError(BrokerConstants.BrokerErrorCode.CLASS_NAME,
                BrokerConstants.BrokerErrorCode.BROKER_ENVIRONMENT_ERROR_CODE,
                "Broker events are only allowed by the product owner in PRO.",
                "Please, contact with the product owner",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }


    /**
     * Hidden constructor
     */
    private BrokerError()
    {
        super();
    }
}
