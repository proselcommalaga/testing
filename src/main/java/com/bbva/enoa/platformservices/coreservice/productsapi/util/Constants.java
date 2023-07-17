package com.bbva.enoa.platformservices.coreservice.productsapi.util;

/**
 * Constants
 */
public final class Constants
{

    public static final String PRODUCT_API = "Productsapi";

    public static final String PRODUCT_SERVICE = "ProductsAPIService";

    public static final String USER_VALIDATION_SERVICE = "UserValidationService";

    public static final String CREATE_PRODUCT_PERMISSION = "CREATE_PRODUCT";

    public static final String EDIT_PRODUCT_PERMISSION = "EDIT_PRODUCT";

    public static final String EDIT_PRODUCT_COMMON_CONFIGURATION_PERMISSION = "EDIT_PRODUCT_COMMON_CONFIGURATION";

    public static final String DELETE_PRODUCT_PERMISSION = "DELETE_PRODUCT";

    public static final String PRODUCT_CREATE_MEMBER_PERMISSION = "PRODUCT_CREATE_MEMBER";

    public static final String PRODUCT_DELETE_MEMBER_PERMISSION = "PRODUCT_DELETE_MEMBER";

    public static final String USER_NOT_EXISTS_ERROR = "USER-001";

    public static final String CREATE_SUBSYSTEM_ACTION = "CREATE";

    public static final String UPDATE_SUBSYSTEM_ACTION = "UPDATE";

    public static final String DELETE_SUBSYSTEM_ACTION = "DELETE";

    public static final String DEPLOYMENT_ON_CLOUD_PERMISSION = "DEPLOYMENT_ON_CLOUD";

    public static final String ADD_USER_TO_ETHER_NAMESPACE = "addUserToProduct";


    public static final String OK = "OK";

    /**
     * Constant for literal TC
     **/
    public static final String TC_PRODUCTION_NAME = "TC";

    /**
     * Functional Categories Regex
     */
    public static final String FUNCTIONAL_CATEGORIES_REGEX = "(?i)^(Amb|Ambito|Ámbito|Dom|Dominio|Geografía|Subdom|Sub-dominio):.*";

    /**
     * Environment
     */
    public enum ENVIRONMENT
    {
        INT, PRE, PRO
    }

    /**
     * Ether type platform
     */
    public enum TYPE_PLATFORM
    {
        DEPLOY, LOGGING
    }

    /**
     * Code for admin permissions
     */
    public static final String PRODUCT_CONFIGURATION_MANAGEMENT = "PRODUCT_CONFIGURATION_MANAGEMENT";
}
