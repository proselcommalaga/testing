package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

/**
 * Physical connector error code.
 */
@ExposeErrorCodes
public class PhysicalConnectorError
{

    private static final String CLASS_NAME = "PhysicalConnectorError";
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";
    public static final String UNEXPECTED_ERROR_CODE = "PHYSICALCONNECTOR-000";

    public static String getClassName()
    {
        return CLASS_NAME;
    }

    public static NovaError getUnexpectedError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-000", PhysicalConnectorError.UNEXPECTED_ERROR_MSG, Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.FATAL);
    }

    public static NovaError getUnableDeleteConnectorTypeDueLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-001", "The connector type can not be delete due some logical connector is being used this kind of connector type", "Remove the logical connector before deleting the connector type", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getUnableDeleteConnectorTypeDuePhysicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-002", "The connector type can not be delete due some physical connector is being used this kind of connector physical", "Remove the physical connector before deleting the connector type", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchPhysicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-003", "The physical connector couldn't be found into BBDD", "Check if there is physical connector with the given ID is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.CRITICAL);
    }

    public static NovaError getUnableToDeletePhysicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-004", "Tried to delete a physical connector that being used by at least one logical connector", "It's not allowed to delete a physical connector being used on any logical connector", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getHasDeletePhysicalConnectorTaskPendingError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-005", "The physical connector has almost one delete physical connector to do task in PENDING or PENDING_ERROR status", "Resolve the to do task before associate the logical connector of the physical connector", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getDuplicatedPhysicalConnectorNameError(final String name, final String environment)
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-006", "There is another physical connector with the same name: [" + name + "] in the same environment: [" + environment + "]", "Try to use a different name", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    public static NovaError getIpNotValidError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-007", "The virtual ip does not have a valid ip format", "Try to insert again a virtual ip that follow the ip pattern", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchConnectorTypeError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-008", "The connector type name couldn't be found", "Check if there is a connector type name with the given name is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getDuplicatedConnectorTypeError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-009", "The connector type name is already in use", "Try to use another connector type name", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchCPDNameError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-010", "The CPD name couldn't be found", "Check if there is a CPD name with the given name and environment is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getUnableToDisassociateLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-011", "Unable to disassociate the physical connector from the physical connector", "Check if the physical connector is in use or has a pending to-do task, or it is not associated to any logical connector", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getUnableToAssociateLogicalConnectorDueEnvironmentError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-012", "Unable to associate the physical connector to the logical connector due has the same environment", "Change the physical connector and the logical connector to have the same environment", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getDuplicatedConnectorTypePropertyNameError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-013", "There is another connector type property in that connector type with the same name", "Try using a different name for this property", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchConnectorTypePropertyIdError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-014", "The connector type property id does not exist into NOVA BBDD", "Check if there is a connector type property  with the given ID on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getUnableToAssociateLogicalConnectorDueConnectorTypeError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-015", "Unable to associate the physical connector to the logical connector due to both do not have the same connector type", "Change the physical connector type and the logical connector type to be the same connector type", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getUnableToAssociateLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-016", "Unable to associate the physical connector to the logical connector due is already associated to the same physical connector port", "Change the physical connector or the logical connector to avoid is already associated", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-017", "The logical connector couldn't be found into BBDD", "Check if there is logical connector with the given ID is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.CRITICAL);
    }

    public static NovaError getHasCheckLogicalConnectorPropertyTaskPendingError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-018", "The logical connector has almost one logical connector check properties to do task in PENDING or PENDING_ERROR status associated with this logical connector", "Resolve the to do task before disassociate the logical connector of the physical connector", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getConnectorTypeRequiredError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-019", "Connector type required for creation", "Provide a not null ConnectorType for creation", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getNoFoundPhysicalConnectorPortError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-020", "Unable to edit a physical connector port due to the physical connector has not established some port", "Edit the physical connector port and try to add a new port", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getPendingPortPhysicalConnectorStatusError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-021", "The connector can not be associated to any logical connector in PENDING_PORT status", "Edit the physical connector, port section and try to add a new port for changing the physical connector status to CREATED", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getInputPortValueAlreadyInUseError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-022", "The input port value for the physical connector is already in use by some physical connector port", "Edit the physical connector, port section and try to change the input port value", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getInputPortNameAlreadyInUseError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-023", "The input port name for the physical connector is already in use by some physical connector port", "Edit the physical connector, port section and try to change the input port value", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getConnectorPropertyDeletionError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-024", "The connector type property cannot be deleted due to is already in use by some deployment service in status DEPLOYED", "Undeploy the deployment plan that belongs the deployment service before deleting the property", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchPortalUserError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-025", "The invocation of the user via user code doesn't exist in data base", "Review the NOVA BBDD and check if exist the iv user code provided", HttpStatus.NOT_FOUND, ErrorMessageType.WARNING);
    }

    public static NovaError getUnableToAssociateLogicalConnectorErrorStatusError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-026", "The logical connector cannot be associated due to the logical connector is ERROR status", "Delete this logical connector and request a new one and after try to associate again", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getDuplicatedVirtualIpError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-027", "There is another physical connector with the same virtual ip", "Try using a different virtual ip", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getPhysicalConnectorPortAlreadyInUseError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-028", "The physical connector port has almost one logical connector associated or in use", "For removing a physical connector port ensure that any logical connector are using this physical connector port", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchPhysicalConnectorPortError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-029", "The physical connector port couldn't be found into BBDD", "Check if there is a physical connector port with the given ID is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.CRITICAL);
    }

    public static NovaError getIpPortFormatNotValidError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-030", "The physical connector port output is not following the format ip:port", "Check if there is a physical connector port with the given ID is on the NOVA DB", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getNumberPortNotValidError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-031", "The physical connector port output port is not in the appropriate format. It must be a number between 0-65535", "Check the output field port and set a number port", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    public static NovaError getRangeNumberPortNotValidError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-032", "The physical connector port output port is not in the range the port allowed", "Check the output field port and change the number port", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getConnectorTypePropertyRequiredError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-033", "ConnectorType property parameters is required", "Provide a valid ConnectorTypeProperty object", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getPhysicalConnectorRequiredError()
    {
        return new NovaError(CLASS_NAME, "PHYSICALCONNECTOR-034", "Edit physical connector is required for this operation", "Provide a not null PhysicalConnector data", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getForbiddenError()
    {
        return new NovaError(CLASS_NAME, "USER-002","The current user does not have permission for this operation","Use a user wiht higher privilegies or request permission for your user", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

}
