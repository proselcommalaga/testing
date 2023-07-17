package com.bbva.enoa.platformservices.coreservice.common;

/**
 * Class dedicated to write and store the mail constants as the subject of the all notification or some body mail parts
 * BBVA - XE30432
 */
public final class Constants
{

    /////////////////////////////////////////// PRODUCT SERVICE PROPERTIES /////////////////////////////////////////////
    /**
     * http or https
     */
    public static final String PRODUCT_SERVICE_API_PROTOCOL   = "http";

    /**
     * The service name, is the artifact id: Core Service
     */
    public static final String SERVICE_NAME = "CoreService";

    /**
     * Line separator property name
     */
    public static final String LINE_SEPARATOR = "line.separator";

    /**
     * Not available value
     */
    public static final String NOT_AVAILABLE_VALUE = "N/D";

    /////////////////////////////////////////// NOTIFICATION PROPERTY KEYS /////////////////////////////////////////////
    /**
     * Product name notification property key
     */
    public static final String PRODUCT_NAME = "productName";
    /**
     * UUAA notification property key
     */
    public static final String UUAA           = "uuaa";
    /**
     * Mail addresses property key
     */
    public static final String MAIL_ADDRESSES = "mailAddresses";

    /**
     * JDK version
     */
    public static final String JDK_VERSION = "jdkVersion";

    ///////////////////////////////// NOTIFICATIONS CODES //////////////////////////////////////////////////////////////
    /**
     * Download files to pre response
     */
    public static final Integer DOWNLOAD_FILES_TO_PRE_REQUEST_INFO_CODE = 48;

    /**
     * Delete product notification code
     */
    public static final int DELETE_PRODUCT_NOTIFICATION_CODE = 5;


    ///////////////////////////////// JENKINS JOB PARAMETERS FOR EPHOENIX SUBSYSTEMS TYPE //////////////////////////////
    /**
     * response id
     */
    public static final String RESPONSE_ID_PARAM     = "RESPONSE_ID";
    /**
     * project uuaa
     */
    public static final String PROJECT_UUAA_PARAM    = "PROJECT_UUAA";
    /**
     * Subsystem name
     */
    public static final String SUBSYSTEM_NAME_PARAM  = "SUBSYSTEM_NAME";
    /**
     * Tag
     */
    public static final String TAG_PARAM             = "TAG";
    /**
     * Instance name
     */
    public static final String INSTANCE_NAME_PARAM   = "INSTANCE_NAME";
    /**
     * Instance type
     */
    public static final String INSTANCE_TYPE_PARAM   = "INSTANCE_TYPE";
    /**
     * Image name
     */
    public static final String IMAGE_NAME_PARAM      = "IMAGEN";
    /**
     * Branch SQA
     */
    public static final String BRANCH_SQA_PARAM      = "BRANCH_SQA";
    /**
     * Instance UUAAs
     */
    public static final String INSTANCE_UUAAS_PARAM  = "INSTANCE_UUAAS";
    /**
     * release name
     */
    public static final String RELEASE_NAME_PARAM    = "RELEASE_NAME";
    /**
     * release version
     */
    public static final String RELEASE_VERSION_PARAM = "RELEASE_VERSION";
    /**
     * instance uuaa
     */
    public static final String INSTANCE_UUAA_PARAM   = "INSTANCE_UUAA";

    /**
     * Sonar quality gate param
     */
    public static final String SONAR_QUALITY_GATE_PARAM = "SONAR_QUALITY_GATE";

    //////////////////////////////////////////// SERVICE RUNNER API CONSTANTS /////////////////////////////////////////

    public static final String START_OPERATION   = "el arranque";
    public static final String RESTART_OPERATION = "el reinicio";
    public static final String STOP_OPERATION    = "la parada";
    public static final String PAUSE_OPERATION   = "la pausa";

    public static final String IMMUSER           = "IMM0589";

    //////////////////////////////////////////// Swagger validator API CONSTANTS
    //////////////////////////////////////////// /////////////////////////////////////////

    public static final String MSG_API_NOT_REGISTERED  = "Api not registered";
    public static final String MSG_CONTENT_VERSION     = " with version ";


    /////////////////////////////////////////// Atenea constants //////////////////////////////////////
    public static final String ATENEA_LOG_ENDPOINT = "/omega/logs";
    public static final String ATENEA_METRICS_ENDPOINT = "/mu/metrics";

    /**
     * Literal for BBVA header for user code
     */
    public static final String IV_USER_HEADER_KEY = "iv-user";

    /////////////////////////////////// REGULAR EXPRESSIONS FOR NOVA VERSIONS ///////////////////////////////////

    public static final String PATTERN_VERSION_REGEX = "^([\\d]{1,3})\\.([\\d]{1,3})\\.([\\d]{1,3})$";

    //////////////////////////////////////////// DEPLOYMENTS API CONSTANTS /////////////////////////////////////////

    /**
     * Ephoenix deployment.
     */
    public static final String EPHOENIX_DEPLOYMENT = "ephoenix.deployment.line";

    /**
     * Description for condiguration management
     */
    public static final String ENVIRONMENT_DESCRIPTION = "Establecer configuración dependiente de entorno";
    public static final String LIB_GENERAL_DESCRIPTION = "Establecer configuración dependiente de librerías";



    /**
     * Generic definition for unexpected error.
     */
    public static final String UNEXPECTED_ERROR_DEFINITION = "Unexpected internal error";


    /**
     * Generic message for unexpected error.
     */
    public static final String MSG_CONTACT_NOVA = "Please, contact the NOVA Support team via JIRA: PNOVA";

    /**
     * Generic message for unexpected error.
     */
    public static final String MSG_CONTACT_XMAS = "Please, contact the XMAS Support Admin Team via mail: xmas.cib@bbva.com or JIRA: XSP";

    /**
     * PostgreSQL syntax error
     */
    public static final String POSTGRESQL_SYNTAX_ERROR_CODE = "42601";

    /**
     * Ephoenix version incompatible
     */
    public static final String INCOMPATIBLE_EPHOENIX_MODULE_VERSION_CODE =  "Incompatible module version";

    /**
     * Big number for simulate no notification until the alert is closed
     */
    public static final Integer NO_NOTIFY_UNTIL_CLOSE_ALERT = 2000000000;

    /**
     * Api Broker name
     */
    public static final String BROKER_CORESERVICE_API_NAME = "BrokerCoreserviceApi";

    // Common errors
    public static class CommonErrorConstants {

        // Class names of common classes
        public static final String COMMON_ERROR_CLASS_NAME = "CommonError";

        // Common errors codes
        /**
         * Fail calling user admin
         */
        public static final String FAIL_CALLING_USER_ADMIN_ERROR_CODE = "COMMON-001";

        /**
         * Unknown environment Error code
         */
        public static final String UNKNOWN_ENVIRONMENT_ERROR_CODE = "COMMON-002";

        /**
         * DocSystem not found error code.
         */
        public static final String DOC_SYSTEM_NOT_FOUND_ERROR_CODE = "COMMON-003";

        /**
         * Call to "File Transfer Admin API" error code.
         */
        public static final String FAILURE_CALLING_FILE_TRANSFER_ADMIN_API_ERROR_CODE = "COMMON-004";

        /**
         * Call to "Documents Manager API" error code.
         */
        public static final String FAILURE_CALLING_DOCUMENTS_MANAGER_API_ERROR_CODE = "COMMON-005";

        /**
         * Call to "Schedule Control M API" error code.
         */
        public static final String FAILURE_CALLING_SCHEDULE_CONTROL_M_API_ERROR_CODE = "COMMON-006";
        /**
         * Call to "WRONG_NOVA_AGENT_ERROR_CODE" error code.
         */
        public static final String WRONG_NOVA_AGENT_ERROR_CODE = "COMMON-007";

    }
    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }
}
