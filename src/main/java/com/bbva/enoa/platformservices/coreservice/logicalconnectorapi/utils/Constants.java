package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils;

/**
 * Class dedicated to literals of the logical connector API
 * BBVA - XE30432
 */
public final class Constants
{
    //////////////////////////////////// Literals related to exceptions ////////////////////////////////////////////////
    // Literal exception of
    public static final String ERR_MSG = "Error caught calling [{}] with code [{}]";

    /////////////////////////////////Literals for API names and methods ///////////////////////////////////////////////
    // Literal for api name and method of
    public static final String LOGICAL_CONNECTOR_API = "LogicalConnectorAPI";
    // Literal for api name and method of
    public static final String GET_LOGICAL_CONNECTOR_METHOD = "getLogicalConnector";
    // Literal for api name and method of
    public static final String ARCHIVE_LOGICAL_CONNECTOR_METHOD = "archiveLogicalConnector";
    // Literal for api name and method of
    public static final String TEST_LOGICAL_CONNECTOR_METHOD = "testLogicalConnector";
    // Literal for api name and method of
    public static final String DELETE_LOGICAL_CONNECTOR_METHOD = "deleteLogicalConnector";
    // Literal for api name and method of
    public static final String GET_ALL_FROM_PRODUCT_METHOD = "getAllFromProduct";
    // Literal for api name and method of
    public static final String REQUEST_PROPERTIES_METHOD = "requestProperties";
    // Literal for api name and method of
    public static final String RESTORE_LOGICAL_CONNECTOR_METHOD = "restoreLogicalConnector";
    // Literal for api name and method of
    public static final String CREATE_LOGICAL_CONNECTOR_METHOD = "createLogicalConnector";
    // Literal for api name and method of
    public static final String GET_CONNECTOR_TYPES_METHOD = "getConnectorTypes";

    ///////////////// Other literals //////////////////////////////
    // Literal for V CPD name
    public static final String V_CPD_NAME = "V";

    //////////////// Permissions //////////////////////
    public static final String ARCHIVE_CONNECTOR = "ARCHIVE_CONNECTOR";
    public static final String CONNECTOR_DETAIL = "CONNECTOR_DETAIL";
    public static final String CREATE_CONNECTOR = "CREATE_CONNECTOR";
    public static final String UPDATE_CONNECTOR = "UPDATE_CONNECTOR";
    public static final String DELETE_CONNECTOR = "DELETE_CONNECTOR";
    public static final String RESTORE_CONNECTOR = "RESTORE_CONNECTOR";
    public static final String TEST_CONNECTOR = "TEST_CONNECTOR";

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }
}
