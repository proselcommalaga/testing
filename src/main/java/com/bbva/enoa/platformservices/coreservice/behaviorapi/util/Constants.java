package com.bbva.enoa.platformservices.coreservice.behaviorapi.util;

/**
 * Constants for Behavior API
 */
public final class Constants
{
    /**
     * Constants of the Behavior API
     */
    public static final String BEHAVIOR_API_NAME = "BehaviorAPI";

    /**
     * Behavior service configuration
     */
    public static final String BEHAVIOR_SERVICE_CONFIGURATION =  "behaviorServiceConfiguration";

    /**
     * Behavior service id
     */
    public static final String BEHAVIOR_SERVICE_ID =  "behaviorServiceId";

    ///////////////////////////////////// API permissions \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public static final String CREATE_BEHAVIOR_PERMISSION = "CREATE_BEHAVIOR_VERSION";
    public static final String DELETE_BEHAVIOR_PERMISSION = "DELETE_BEHAVIOR_VERSION";
    public static final String EDIT_BEHAVIOR_PERMISSION = "EDIT_BEHAVIOR_VERSION";
    public static final String START_BEHAVIOR_INSTANCE = "START_BEHAVIOR_INSTANCE";
    public static final String STOP_BEHAVIOR_INSTANCE = "STOP_BEHAVIOR_INSTANCE";

    /**
     * Maxmum number of release versions
     */
    public static final int MAXIMUM_SIMULTANEOUS_COMPILATIONS = 1;

    ///////////////////////////////////// SUBSYSTEM DTO BUILDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public static final String WRONG_SERVICE_VERSION_MSG = "Error in nova.yml: Current service version does not match with pom.xml";
    public static final String WRONG_SERVICE_NAME_CODE = "WRONG_SERVICE_NAME";
    public static final String SCHEDULER_FILE_ERRORS = "SCHEDULER_FILE_ERRORS";
    public static final String WRONG_SERVICE_NAME_MSG = "Error in nova.yml: Current service name does not match with pom.xml";
    public static final String TREE = "/tree/";

    ///////////////////////////////////// CONSTANTS TO PRINT \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static final String BLANK_SPACE = " ";

    /**
     * Constant class with errors code
     */
    public static class BehaviorErrors
    {
        /**
         * ReleaseError class name for exception
         */
        public static final String CLASS_NAME = "BehaviorError";

        /**
         * Code: Unexpected internal error
         **/
        public static final String UNEXPECTED_ERROR_CODE = "BEHAVIOR-000";
        /**
         * Unexpected internal error
         **/
        public static final String MSG_UNEXPECTED_ERROR = "Unexpected internal error";

        public static final String MSG_UNEXPECTED_COMMUNICATION_ERROR = "Unexpected api communication error, please ensure services are up and working";

        /**
         * Code: The given product doesn't exist
         **/
        public static final String CODE_PRODUCT_DOESNT_EXIST = "BEHAVIOR-001";

        /**
         * Code: The given product doesn't exist
         **/
        public static final String CODE_PRODUCT_NOT_SUBSYSTEM = "BEHAVIOR-002";

        /**
         * Code: The maximum limit of release versions has been reached
         **/
        public static final String CODE_MAX_VERSIONS_LIMIT = "BEHAVIOR-003";

        /**
         * Code: The maximum limit of compiling versions has been reached
         **/
        public static final String CODE_MAX_VERSIONS_COMPILING = "BEHAVIOR-004";

        /**
         * Code: The subsystem tag has no services
         **/
        public static final String CODE_TAG_WITHOUT_SERVICE = "BEHAVIOR-005";

        /**
         * Code: The name of version is duplicated
         **/
        public static final String CODE_DUPLICATED_VERSION_NAME = "BEHAVIOR-006";

        /**
         * Code: Invalid behavior version status value
         **/
        public static final String CODE_INVALID_BEHAVIOR_VERSION_STATUS_VALUE = "BEHAVIOR-007";

        /**
         * Code: No such behavior version
         **/
        public static final String CODE_NO_SUCH_BEHAVIOR_VERSION = "BEHAVIOR-008";

        /**
         * Code: Group ID not found
         **/
        public static final String CODE_GROUP_ID_NOT_FOUND = "BEHAVIOR-009";

        /**
         * Code: Artifact ID not found
         **/
        public static final String CODE_ARTIFACT_ID_NOT_FOUND = "BEHAVIOR-010";

        /**
         * Code: Version not found
         **/
        public static final String CODE_VERSION_NOT_FOUND = "BEHAVIOR-011";

        /**
         * Code: Version name not found
         **/
        public static final String CODE_VERSION_NAME_NOT_FOUND = "BEHAVIOR-012";

        /**
         * Code: Service name not found
         **/
        public static final String CODE_SERVICE_NAME_NOT_FOUND = "BEHAVIOR-013";

        /**
         * Code: Service type name not found
         **/
        public static final String CODE_SERVICE_TYPE_NOT_FOUND = "BEHAVIOR-014";

        /**
         * Code: Behavior version without subsystem
         **/
        public static final String CODE_VERSION_WITHOUT_SUBSYSTEM = "BEHAVIOR-015";

        /**
         * Code: Service type name not found
         **/
        public static final String CODE_SUBSYSTEM_WITHOUT_SERVICE = "BEHAVIOR-016";

        /**
         * Code: Duplicated service name
         **/
        public static final String CODE_DUPLICATE_SERVICE_NAME = "BEHAVIOR-017";

        /**
         * Code: Product subsystem not found
         **/
        public static final String CODE_PRODUCT_SUBSYSTEM_NOT_FOUND = "BEHAVIOR-018";

        /**
         * Code: Null Product subsystem ID
         **/
        public static final String CODE_NULL_PRODUCT_SUBSYSTEM_ID = "BEHAVIOR-019";

        /**
         * Code: Behavior version is building
         **/
        public static final String CODE_BEHAVIOR_VERSION_BUILDING = "BEHAVIOR-020";

        /**
         * Code: Behavior subsystem is pending
         **/
        public static final String CODE_BEHAVIOR_SUBSYSTEM_PENDING = "BEHAVIOR-021";

        /**
         * Code: Behavior configuration is editing
         **/
        public static final String CODE_BEHAVIOR_CONFIGURATION_EDITING = "BEHAVIOR-022";

        /**
         * Code: Behavior instance is editing
         **/
        public static final String CODE_BEHAVIOR_INSTANCE_EDITING = "BEHAVIOR-023";


        public static final String CODE_BAD_ARGUMENTS = "BEHAVIOR-024";

        public static final String CODE_RESOURCE_NOT_FOUND = "BEHAVIOR-025";

        public static final String CODE_BAD_PROCESSING = "BEHAVIOR-026";

        public static final String CODE_ALREADY_CONFIGURED_SERVICE = "BEHAVIOR-027";

        public static final String CODE_NO_INITIATIVES_FOUND = "BEHAVIOR-028";

        /**
         * Behavior service configuration not found
         */
        public static final String BS_CONFIGURATION_NOT_FOUND_ERROR_CODE = "BEHAVIOR-029";

        /**
         * Code: Does not have permissions
         **/
        public static final String CODE_FORBIDDEN_ERROR = "USER-002";

        /**
         * Configuration not found message
         */
        public static final String CONFIGURATION_NOT_FOUND_ERROR_MESSAGE = "Behavior service configuration with id: [%d] was not found in NOVA database";

        /**
         * Configuration not found action
         */
        public static final String CONFIGURATION_NOT_FOUND_ACTION = "Check if given behavior service configuration exists in database. Could have been deleted recently";

        /**
         * error code for an operation which cannot be done because the product has no NOVA behavior services canon
         */
        public static final String PRODUCT_WITHOUT_NOVA_SERVICES_CANON = "BEHAVIOR-030";

        /**
         * error code for an operation which cannot be done because the product has no NOVA behavior services canon
         */
        public static final String BEHAVIOR_INSTANCE_NOT_FOUND = "BEHAVIOR-031";
    }

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }

}
