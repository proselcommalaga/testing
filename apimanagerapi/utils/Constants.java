package com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils;

/**
 * Constants for API manager
 */
public final class Constants
{

    /////////////////////////////////Literals for API names and methods ///////////////////////////////////////////////
    // Literal for api name and method of Api manager
    public static final String LISTENER_API_NAME = "ApiManager";
    public static final String GET_PRODUCT_APIS = "getProductApis";
    public static final String API_DTO_BUILDER_IMPL = "ApiDtoBuilderImpl";
    public static final String API_METHOD_DTO_BUILDER_IMPL = "ApiMethodDtoBuilderImpl";
    public static final String DOWNLOAD_API_SERVICE_IMPL = "DownloadApiServiceImpl";
    public static final String API_MANAGER_VALIDATOR_SERVICE_IMPL = "ApiManagerValidatorServiceImpl";
    public static final String GENERATOR_EXTERNAL_API_PARAM = "x-external-api";


    /////////////////////////////////Literals for Permissions names///////////////////////////////////////////////
    public static final String UPLOAD_API_PERMISSION = "UPLOAD_API";
    public static final String DELETE_API_PERMISSION = "DELETE_API";
    public static final String CREATE_POLICY_TASK_PERMISSION = "CREATE_POLICY_TASK";
    public static final String UPLOAD_GOVERNED_API_PERMISSION = "UPLOAD_GOVERNED_API";
    public static final String DELETE_GOVERNED_API_PERMISSION = "DELETE_GOVERNED_API";
    public static final String UPLOAD_EXTERNAL_API_PERMISSION = "UPLOAD_EXTERNAL_API";
    public static final String DELETE_EXTERNAL_API_PERMISSION = "DELETE_EXTERNAL_API";


    /////////////////////////////////Literals for service logic///////////////////////////////////////////////
    public static final String CONSUMED = "CONSUMED";

    public static final String EXTERNAL_API_TYPE = "EXTERNAL";
    public static final String GOVERNED_API_TYPE = "GOVERNED";
    public static final String NOT_GOVERNED_API_TYPE = "NOT_GOVERNED";

    public static final String ROUTING_POLICY = "Routing";
    public static final String DEFAULT_POLICY_TYPE = "default";
    public static final String CUSTOM_POLICY_TYPE = "custom";

    public static final String TODO_TASK_PENDING_STATUS = "PENDING";
    public static final String TODO_TASK_PENDING_ERROR_STATUS = "PENDING_ERROR";

    //Path slash
    public static final String PATH_SLASH = "/";
    //Description length
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    //Name length
    public static final int MAX_NAME_LENGTH = 255;
    //Code used for indicating a to do task is already created
    public static final int TODO_TASK_ALREADY_CREATED_CODE = -1;
    //Url path
    public static final String URL_PATH = "#/products/";

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }

    public static class ApiManagerErrors
    {
        private ApiManagerErrors()
        {
        }

        /**
         * Class Name
         */
        public static final String CLASS_NAME = "ApiGatewayManagerError";

        /**
         * Unexpected error msg
         */
        public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";

        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "APIMANAGER-000";

        /**
         * Product not found
         */
        public static final String PRODUCT_NOT_FOUND_ERROR_CODE = "APIMANAGER-001";

        /**
         * Api version not found code
         */
        public static final String API_VERSION_NOT_FOUND_ERROR_CODE = "APIMANAGER-002";

        /**
         * Api doesnt belong to product error code
         */
        public static final String API_NOT_BELONGS_TO_PRODUCT_ERROR_CODE = "APIMANAGER-003";

        /**
         * Api already exists error code
         */
        public static final String API_ALREADY_EXISTS_ERROR_CODE = "APIMANAGER-004";

        /**
         * Invalid Api Type error code
         */
        public static final String API_INVALID_TYPE_ERROR_CODE = "APIMANAGER-005";

        /**
         * Invalid Api basepath error code
         */
        public static final String API_INVALID_BASEPATH_ERROR_CODE = "APIMANAGER-006";

        /**
         * Service not found error code
         */
        public static final String SERVICE_NOT_FOUND_ERROR_CODE = "APIMANAGER-007";

        /**
         * Swagger download error code
         */
        public static final String SWAGGER_DOWNLOAD_ERROR_CODE = "APIMANAGER-008";

        /**
         * Invalid file format error code
         */
        public static final String INVALID_FILE_FORMAT_ERROR_CODE = "APIMANAGER-009";

        public static final String REGISTER_API_ERROR_CODE = "APIMANAGER-010";

        public static final String REMOVE_REGISTERED_API_ERROR_CODE = "APIMANAGER-011";

        public static final String CREATE_POLICIES_TASK_ERROR_CODE = "APIMANAGER-012";

        public static final String GET_POLICIES_TASK_ERROR_CODE = "APIMANAGER-013";

        public static final String GET_POLICIES_TASK_LIST_ERROR_CODE = "APIMANAGER-014";

        public static final String REMOVE_POLICIES_TASK_LIST_ERROR_CODE = "APIMANAGER-015";

        public static final String API_DETAIL_PLAN_NOT_FOUND_ERROR_CODE = "APIMANAGER-016";

        public static final String SAVE_PLAN_PROFILING_PLAN_NOT_FOUND_ERROR_CODE = "APIMANAGER-017";

        public static final String PLAN_PROFILE_NOT_FOUND_ERROR_CODE = "APIMANAGER-018";

        public static final String API_METHOD_NOT_FOUND_ERROR_CODE = "APIMANAGER-019";

        public static final String API_METHOD_PROFILE_NOT_FOUND_ERROR_CODE = "APIMANAGER-020";

        public static final String CES_ROLE_NOT_FOUND_ERROR_CODE = "APIMANAGER-021";

        public static final String GET_ROLES_FROM_CES_ERROR_CODE = "APIMANAGER-022";

        public static final String GET_API_POLICIES_FROM_XMAS_ERROR_CODE = "APIMANAGER-023";

        public static final String POLICIES_TODO_TASK_PENDING = "APIMANAGER-024";

        public static final String API_METHOD_PROFILE_ROLES_NOT_FOUND_ERROR_CODE = "APIMANAGER-025";

        public static final String REQUEST_CREATION_POLICIES_PARAMETERS_ERROR = "APIMANAGER-026";

        public static final String REQUEST_CREATION_POLICIES_DOCUMENTS_ARE_MANDATORY_ERROR = "APIMANAGER-027";

        public static final String DOC_SYSTEM_NOT_FOUND_ERROR_CODE = "APIMANAGER-028";

        public static final String API_MODALITY_ERROR_CODE = "APIMANAGER-029";

        public static final String API_TYPE_ERROR_CODE = "APIMANAGER-030";

        public static final String TASK_STATUS_ERROR_CODE = "APIMANAGER-031";

        public static final String API_NOT_FOUND_ERROR_CODE = "APIMANAGER-032";

        public static final String API_DATA_BASE_ERROR_CODE = "APIMANAGER-033";

    }
}
