package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

/**
 * Constants class for Brokers API
 */
public final class BrokerConstants
{

    /**
     * Literal for api name and method of Brokers API
     */
    public static final String BROKERS_API = "BrokersAPI";

    /**
     * Regex for broker name validation
     */
    public static final String BROKER_NAME_VALIDATION_REGEX = "([a-z0-9])+";

    /**
     * Constants for broker permissions
     */
    public static class BrokerPermissions
    {
        /**
         * Name of permission for create broker
         */
        public static final String CREATE_BROKER = "CREATE_BROKER";

        /**
         * Name of permission for start broker
         */
        public static final String START_BROKER = "START_BROKER";

        /**
         * Name of permission for stop broker
         */
        public static final String STOP_BROKER = "STOP_BROKER";

        /**
         * Name of permission for delete broker
         */
        public static final String DELETE_BROKER = "DELETE_BROKER";

        /**
         * Name of permission for restart broker
         */
        public static final String RESTART_BROKER = "RESTART_BROKER";

        /**
         * Name of permission for view admin user password
         */
        public static final String VIEW_BROKER_ADMIN_PASSWORD = "VIEW_BROKER_ADMIN_PASSWORD";

        /**
         * Name of permission for view admin user password
         */
        public static final String VIEW_BROKER_SERVICE_PASSWORD = "VIEW_BROKER_SERVICE_PASSWORD";

        /**
         * The constant DELETE_EXCHANGE_OR_QUEUE.
         */
        public static final String DELETE_EXCHANGE_OR_QUEUE = "DELETE_EXCHANGE_OR_QUEUE";
        /**
         * The constant PURGE_QUEUE.
         */
        public static final String PURGE_QUEUE = "PURGE_QUEUE";
        /**
         * The constant PUBLISH_MESSAGE_ON_EXCHANGE.
         */
        public static final String PUBLISH_MESSAGE_ON_EXCHANGE = "PUBLISH_MESSAGE_ON_EXCHANGE";
        /**
         * The constant SHOW_MESSAGE_FROM_QUEUE.
         */
        public static final String SHOW_MESSAGES_FROM_QUEUE = "SHOW_MESSAGES_FROM_QUEUE";

        /**
         * Name of permission for update broker alerts configuration
         */
        public static final String UPDATE_BROKER_ALERT_CONFIGURATION = "UPDATE_BROKER_ALERT_CONFIGURATION";

        /**
         * Private constructor
         */
        private BrokerPermissions()
        {
            // nothing to do
        }
    }

    /**
     * Class to handle Broker errors
     */
    public static class BrokerErrorCode
    {
        /**
         * Class name
         */
        public static final String CLASS_NAME = "BrokerErrorCode";

        /**
         * Unexpected error msg
         */
        public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";

        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "BROKER-000";

        /**
         * Permission denied error code
         */
        public static final String PERMISSION_DENIED_ERROR_CODE = "BROKER-001";

        /**
         * Broker not found error code
         */
        public static final String BROKER_NOT_FOUND_ERROR_CODE = "BROKER-002";

        /**
         * Unexpected broker status error code
         */
        public static final String UNEXPECTED_BROKER_STATUS_ERROR_CODE = "BROKER-003";

        /**
         * Broker operation not valid error code
         */
        public static final String BROKER_OPERATION_NOT_VALID_ERROR_CODE = "BROKER-004";

        /**
         * Product not found error code
         */
        public static final String PRODUCT_NOT_FOUND_ERROR_CODE = "BROKER-005";

        /**
         * Filesystem not found error code
         */
        public static final String FILESYSTEM_NOT_FOUND_ERROR_CODE = "BROKER-006";

        /**
         * Hardware pack not found error code
         */
        public static final String HARDWAREPACK_NOT_FOUND_ERROR_CODE = "BROKER-007";

        /**
         * Unsupported broker type error code
         */
        public static final String UNSUPPORTED_BROKER_TYPE_ERROR_CODE = "BROKER-008";


        /**
         * Max. number of brokers by product and environment reached error code
         */
        public static final String MAX_BROKERS_BY_ENV_LIMIT_REACHED_ERROR_CODE = "BROKER-009";

        /**
         * Given broker name already exists for this product and environment error code
         */
        public static final String BROKER_NAME_ALREADY_EXISTS_IN_ENV_ERROR_CODE = "BROKER-010";

        /**
         * Failed calling BrokerDeploymentAPI.createBroker error code
         */
        public static final String FAILED_BROKER_DEPLOYMENT_CREATION_ERROR_CODE = "BROKER-011";

        /**
         * Invalid broker name error code
         */
        public static final String INVALID_BROKER_NAME_ERROR_CODE = "BROKER-012";

        /**
         * Invalid filesystem error code
         */
        public static final String INVALID_FILESYSTEM_ERROR_CODE = "BROKER-013";

        /**
         * Invalid hardware pack error code
         */
        public static final String INVALID_HARDWAREPACK_ERROR_CODE = "BROKER-014";

        /**
         * Failed calling BrokerDeploymentAPI.deleteBroker error code
         */
        public static final String FAILED_BROKER_DEPLOYMENT_DELETION_ERROR_CODE = "BROKER-015";

        /**
         * Failed calling BrokerDeploymentAPI.startBroker error code
         */
        public static final String FAILED_BROKER_DEPLOYMENT_START_ERROR_CODE = "BROKER-016";

        /**
         * Failed calling BrokerDeploymentAPI.stopBroker error code
         */
        public static final String FAILED_BROKER_DEPLOYMENT_STOP_ERROR_CODE = "BROKER-017";

        /**
         * Failed calling BrokerDeploymentAPI.restartBroker error code
         */
        public static final String FAILED_BROKER_DEPLOYMENT_RESTART_ERROR_CODE = "BROKER-018";

        /**
         * Not enough broker budget error code
         */
        public static final String NOT_ENOUGH_BROKER_BUDGET_ERROR = "BROKER-019";

        /**
         * Credentials for role not founds error code
         */
        public static final String BROKER_CREDENTIALS_NOT_FOUND_ERROR = "BROKER-020";

        /**
         * Impossible to stop a brker that is being used by running services
         */
        public static final String IMPOSSIBLE_STOP_BROKER_USED_BY_RUNNING_SERVICES_ERROR = "BROKER-021";

        /**
         * Not valid platform error code
         */
        public static final String NOT_VALID_PLATFORM_ERROR = "BROKER-022";

        /**
         * Unsupported broker platform error code
         */
        public static final String UNSUPPORTED_BROKER_PLATFORM_ERROR_CODE = "BROKER-023";

        /**
         * The constant IMPOSSIBLE_CREATE_DUE_TO_NO_SAME_BROKER.
         */
        public static final String IMPOSSIBLE_CREATE_DUE_TO_NO_SAME_BROKER = "BROKER-024";

        /**
         * The constant IMPOSSIBLE_STOP_BROKER_USED_BY_DEPLOYMENT_SERVICES_ERROR.
         */
        public static final String IMPOSSIBLE_STOP_BROKER_USED_BY_DEPLOYMENT_SERVICES_ERROR = "BROKER-025";

        /**
         * The constant BROKER_AGENT_ERROR.
         */
        public static final String BROKER_AGENT_ERROR = "BROKER-026";

        /**
         * The constant BROKER_NOT_FOUND_IN_DEPLOYMENT_SERVICE.
         */
        public static final String BROKER_NOT_FOUND_IN_DEPLOYMENT_SERVICE = "BROKER-027";

        /**
         * The constant USER_ADMIN_NOT_FOUND_IN_BROKER.
         */
        public static final String USER_ADMIN_NOT_FOUND_IN_BROKER = "BROKER-028";

        /**
         * The constant INVALID_INPUT_FOR_CONVENTION_NAME_CREATION.
         */
        public static final String INVALID_INPUT_FOR_CONVENTION_NAME_CREATION = "BROKER-029";

        /**
         * Broker node not found error code
         */
        public static final String BROKER_NODE_NOT_FOUND_ERROR_CODE = "BROKER-030";

        /**
         * Unexpected broker node status error code
         */
        public static final String UNEXPECTED_BROKER_NODE_STATUS_ERROR_CODE = "BROKER-031";

        /**
         * Failed calling BrokerDeploymentAPI.startBrokerNode error code
         */
        public static final String FAILED_BROKER_NODE_DEPLOYMENT_START_ERROR_CODE = "BROKER-032";

        /**
         * Failed calling BrokerDeploymentAPI.stopBrokerNode error code
         */
        public static final String FAILED_BROKER_NODE_DEPLOYMENT_STOP_ERROR_CODE = "BROKER-033";

        /**
         * Failed calling BrokerDeploymentAPI.restartBrokerNode error code
         */
        public static final String FAILED_BROKER_NODE_DEPLOYMENT_RESTART_ERROR_CODE = "BROKER-034";

        /**
         * Broker node is in transitory status error code
         */
        public static final String BROKER_NODE_IN_TRANSITORY_STATUS_ERROR = "BROKER-035";

        /**
         * Broker operation not valid error code
         */
        public static final String BROKER_NODE_OPERATION_NOT_VALID_ERROR_CODE = "BROKER-036";

        /**
         * Number of nodes not supported error code
         */
        public static final String UNSUPPORTED_NUMBER_OF_NODES_ERROR_CODE = "BROKER-037";

        /**
         * Broker not created and stable error code
         */
        public static final String BROKER_NOT_CREATED_AND_STABLE_ERROR_CODE = "BROKER-038";

        /**
         * Not same number of nodes as broker in preproduction error code
         */
        public static final String NOT_SAME_NUMBER_OF_NODES_AS_PREPRODUCTION_ERROR_CODE = "BROKER-039";

        /**
         * Can't retrieve information on stopped broker code error
         */
        public static final String CANT_RETRIEVE_INFORMATION_ON_STOPPED_BROKER_CODE = "BROKER-040";

        /**
         * Can't save and flush the broker alert configuration error code
         */
        public static final String DATABASE_BROKER_ALERT_CONFIG_ERROR = "BROKER-041";

        /**
         * Broker operation not valid error code
         */
        public static final String ALERT_CONFIG_OCCURRENCES_NOT_VALID_ERROR_CODE = "BROKER-042";

        /**
         * Email invalid error code
         */
        public static final String EMAIL_INVALID_ERROR_CODE = "BROKER-043";

        /**
         * Queue threshold not valid error code
         */
        public static final String QUEUE_THRESHOLD_NOT_VALID_ERROR_CODE = "BROKER-044";

        /**
         * Publish rate threshold not valid error code
         */
        public static final String PUBLISH_RATE_THRESHOLD_NOT_VALID_ERROR_CODE = "BROKER-045";

        /**
         * Consumer rate threshold not valid error code
         */
        public static final String CONSUMER_RATE_THRESHOLD_NOT_VALID_ERROR_CODE = "BROKER-046";

        /**
         * Product has not remedy group error code
         */
        public static final String PRODUCT_HAS_NOT_REMEDY_GROUP_ERROR_CODE = "BROKER-047";

        /**
         * Environment error code
         */
        public static final String BROKER_ENVIRONMENT_ERROR_CODE = "BROKER-048";

        /**
         * Private constructor
         */
        private BrokerErrorCode()
        {
            // nothing to do
        }
    }

    private BrokerConstants()
    {
        throw new IllegalStateException("Constants class");
    }
}
