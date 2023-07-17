package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.utils;

/**
 * Class dedicated to literals of the physical connector api
 * BBVA - XE30432
 */
public final class Constants
{
    //////////////////////////////////// Literals related to exceptions ////////////////////////////////////////////////
    // Literal exception of
    public static final String ERR_MSG = "Error caught calling [{}] with code [{}]";

    /////////////////////////////////Literals for API names and methods ///////////////////////////////////////////////
    // Literal for api name and method of
    public static final String PHYSICAL_CONNECTOR_API = "PhysicalConnectorAPI";
    // Literal for api name and method of
    public static final String GET_MANAGEMENT_TYPES_METHOD = "getManagementTypes";
    // Literal for api name and method of
    public static final String DELETE_PHYSICAL_CONNECTOR_METHOD = "deletePhysicalConnector";
    // Literal for api name and method of
    public static final String LOGICAL_CONNECTOR_ID_PARAMETER = "LogicalConnectorId";
    // Literal for api name and method of
    public static final String DISASSOCIATE_CONNECTORS_METHOD = "disassociateConnectors";
    // Literal for api name and method of
    public static final String ADD_CONNECTOR_TYPE_PROPERTY_METHOD = "createConnectorTypeProperty";
    // Literal for api name and method of
    public static final String CREATE_NEW_CONNECTOR_TYPE_METHOD = "createNewConnectorType";
    // Literal for api name and method of
    public static final String GET_ALL_PHYSICAL_CONNECTORS_METHOD = "getAllPhysicalConnectors";
    // Literal for api name and method of
    public static final String GET_CONNECTOR_TYPE_METHOD = "getConnectorType";
    // Literal for api name and method of
    public static final String GET_SCOPE_TYPES_METHOD = "getScopeTypes";
    // Literal for api name and method of
    public static final String GET_CONNECTOR_TYPES_PROPERTIES_METHOD = "getConnectorTypesProperties";
    // Literal for api name and method of
    public static final String ASSOCIATE_CONNECTORS_METHOD = "associateConnectors";
    // Literal for api name and method of
    public static final String EDIT_PHYSICAL_CONNECTOR_METHOD = "editPhysicalConnector";
    // Literal for api name and method of
    public static final String GET_CONNECTOR_TYPES_METHOD = "getConnectorTypes";
    // Literal for api name and method of
    public static final String GET_PROPERTY_TYPES_METHOD = "getPropertyTypes";
    // Literal for api name and method of
    public static final String DELETE_CONNECTOR_TYPE_PROPERTY_METHOD = "deleteConnectorTypeProperty";
    // Literal for api name and method of
    public static final String GET_PHYSICAL_CONNECTOR_METHOD = "getPhysicalConnector";
    // Literal for api name and method of
    public static final String DELETE_CONNECTOR_TYPE_METHOD = "deleteConnectorType";
    // Literal for api name and method of
    public static final String CREATE_PHYSICAL_CONNECTOR_METHOD = "createPhysicalConnector";

    ///////////////// Physical connector permissions ///////////////////
    /**
     * Permission name for creating a physical connectors
     */
    public static final String CREATE_PHYSICAL_CONNECTOR = "CREATE_PHYSICAL_CONNECTOR";
    /**
     * Permission nane for deleting a physical connector
     */
    public static final String DELETE_PHYSICAL_CONNECTOR = "DELETE_PHYSICAL_CONNECTOR";
    /**
     * Permission nane for editing a physical connector
     */
    public static final String EDIT_PHYSICAL_CONNECTOR = "EDIT_PHYSICAL_CONNECTOR";
    /**
     * Permission to create a Connector type
     */
    public static final String CREATE_CONNECTOR_TYPE = "CREATE_CONNECTOR_TYPE";
    /**
     * Permission to delete a conecctor type
     */
    public static final String DELETE_CONNECTOR_TYPE = "DELETE_CONNECTOR_TYPE";
    /**
     * Permission to add property to a existing connector type
     */
    public static final String ADD_PROPERTY_CONNECTOR_TYPE = "ADD_PROPERTY_CONNECTOR_TYPE";
    /**
     * Permisison to delete a property from an existing connector type
     */
    public static final String DELETE_PROPERTY_CONNECTOR_TYPE = "DELETE_PROPERTY_CONNECTOR_TYPE";
    /**
     * Permission to remos association of a physical conector to a locial
     * connector
     */
    public static final String DISASSOCIATE_CONNECTORS = "DISASSOCIATE_CONNECTORS";
    /**
     * Permission toa ssociate a physical connector to a logical connector
     */
    public static final String ASSOCIATE_CONNECTORS = "ASSOCIATE_CONNECTORS";

    ///////////////// Other literals //////////////////////////////
    // Literal for V CPD name
    public static final String V_CPD_NAME = "V";

    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }
}
