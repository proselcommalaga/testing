package com.bbva.enoa.platformservices.coreservice.common.util;

/**
 * Class dedicated to write and store the mail constants and some the notification codes
 * BBVA - XE30432
 */
public final class MailServiceConstants
{
    /////////////////////////////////////////// NOTIFICATION PROPERTY KEYS /////////////////////////////////////////////
    /**
     * Product name notification property key
     */
    public static final String PRODUCT_NAME = "productName";
    /**
     * To do task id notification property key
     */
    public static final String TODO_TASK_ID = "todoTaskId";
    /**
     * Product id notification property key
     */
    public static final String PRODUCT_ID = "productId";
    /**
     * UUAA notification property key
     */
    public static final String UUAA = "uuaa";
    /**
     * Mail addresses property key
     */
    public static final String MAIL_ADDRESSES = "mailAddresses";
    /**
     * User code property key
     */
    public static final String USER_CODE = "userCode";
    /**
     * User role property key
     */
    public static final String USER_ROLE = "userRole";
    /**
     * Password property key
     */
    public static final String PASSWRD = "passwrd";
    /**
     * Mail addresses property key
     */
    public static final String MAIL_ADDRESS = "mailAddress";
    /**
     * Subsystem name notification property key
     */
    public static final String SUBSYSTEM_NAME = "subsystemName";
    /**
     * Change by (person who changed the status of to do task) notification property key
     */
    public static final String CHANGED_BY = "changedBy";
    /**
     * Plan id notification property key
     */
    public static final String PLAN_ID = "planId";
    /**
     * Plan action (deploy or undeploy) notification property key
     */
    public static final String PLAN_ACTION = "planAction";
    /**
     * Environment notification property key
     */
    public static final String ENVIRONMENT = "environment";
    /**
     * Deploy plan subject
     */
    public static final String DEPLOY_PLAN_SUBJECT = "despliegue";
    /**
     * Undeploy plan subject
     */
    public static final String UNDEPLOY_PLAN_SUBJECT = "repliegue";
    /**
     * File path
     */
    public static final String FILE_PATH= "filePath";
    /**
     * File name
     */
    public static final String FILE_NAME = "fileName";
    /**
     * Filesystem name
     */
    public static final String FILESYSTEM_NAME = "filesystemName";

    /**
     * Iv user
     */
    public static final String IV_USER = "ivUser";

    ///////////////////////////////// NOTIFICATIONS CODES //////////////////////////////////////////////////////////////
    /**
     * Product request notification code
     */
    public static final int USER_TOOLS_INFO_NOTIFICATION_CODE = 24;
    /**
     * Product request notification code
     */
    public static final int USER_DELETED_NOTIFICATION_CODE = 7;
    /**
     * Product request notification code
     */
    public static final int SUBSYSTEM_DELETED_NOTIFICATION_CODE = 6;


    public static final int JIRA_KEY_PROJECT_REQUEST_CODE = 37;
    /**
     * Plan manager response notification code
     */
    public static final int PLAN_MANAGER_RESPONSE_NOTIFICATION_CODE = 12;

    /**
     * Empty constructor
     */
    private MailServiceConstants()
    {
        // Empty constructor
    }
}
