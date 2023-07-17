package com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils;

/**
 * Constants for APIGW manager
 */
public final class Constants
{
    /////////////////////////////////Literals for API names and methods ///////////////////////////////////////////////
    public static final String API_DTO_BUILDER_IMPL = "ApiGatewayServiceDtoBuilderImpl";

    /////////////////////////////////Literals for service logic///////////////////////////////////////////////
    public static final String CONSUMED = "CONSUMED";

    public static final String NOVA_DEPLOYMENT_MODE = "NOVA";

    public static final String ETHER_DEPLOYMENT_MODE = "ETHER";

    public static final String ENTITY_TYPE_DEPLOYMENT = "DEPLOYMENT_PLAN";

    public static final String ENTITY_TYPE_BEHAVIOR = "BEHAVIOR_SERVICE";

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }

    public static class ApiGwErrors
    {
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
        public static final String UNEXPECTED_ERROR_CODE = "APIGATEWAY-000";

        /**
         * Product not found
         */
        public static final String SERVICE_NOT_FOUND_FOR_DOCKERKEY_ERROR_CODE = "APIGATEWAY-001";
    }
}
