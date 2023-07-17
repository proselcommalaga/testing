package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.exception;

import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

@ExposeErrorCodes
public class LogicalConnectorError
{
    private static final String CLASS_NAME = "LogicalConnectorError";
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";
    public static final String UNEXPECTED_ERROR_CODE = "LOGICALCONNECTOR-000";

    public static String getClassName()
    {
        return CLASS_NAME;
    }

    public static NovaError getUnexpectedError()
    {
        return new NovaError(CLASS_NAME, UNEXPECTED_ERROR_CODE, LogicalConnectorError.UNEXPECTED_ERROR_MSG, Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.FATAL);
    }

    public static NovaError getLogicalConnectorNotAvailableError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-001", "Tried to delete a logical connector but is in status creating or archived", "Wait for the logical connector finished the action or restore the logical connector", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchProductError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-002", "The product couldn't be found", "Check if there is a product with the given ID in the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-003", "The logical connector couldn't be found into BBDD", "Check if there is logical connector with the given ID is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getLogicalConnectionDeletionError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-004", "The logical connector couldn't be deleted due to the logical connector is already archived", "The logical connector can not be deleted any case", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getDeleteUsedLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-005", "Tried to delte a logical connector that being used by at least one service of a plan", "It's not allowed to delete a logical connector being used on any not definition plan", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getDuplicatedLogicalConnectorNameError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-006", "There is another logical connector in the product with the same name", "Try using a different name", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    public static NovaError getTriedToArchiveUsedLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-007", "Tried to archive a logical connector that being used by at least one service of a deployed plan", "It's not allowed to archive a logical connector being used on any deployed plan", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchConnectorTypeError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-008", "The connector type name couldn't be found", "Check if there is a connector type name with the given name is on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchCPDNameError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-009", "The CPD name couldn't be found", "Check if there is a CPD name with the given name and environment is on the NOVA DDBB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getHasToDoTaskPendingError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-010", "The logical connector has almost one to do task in PENDING or PENDING_ERROR status associated with this logical connector", "Resolve the to do task before deleting the logical connector", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getTriedToArchiveLogicalConnectorNotCreatedError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-011", "Tried to archive a logical connector that does not have assigned a CREATED status", "It's only allowed to archive a logical connector with CREATED status", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getTriedToArchiveLogicalConnectorNotUsedError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-012", "Tried to archive a logical connector that has never been used by any deployment plan", "It's not allowed to archive a logical connector without having been used in a deployment plant almost once", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getTriedToRestoreLogicalConnectorError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-013", "Tried to restore a logical connector that is not archived status", "It's only allowed to restore a logical connector that is archived status", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getCannotRequestPropertiesError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-014", "Cannot request a check logical connector properties to do task due to the logical connector status is not CREATED", "It's only allowed to request a check properties if the logical connector status is CREATED", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchPortalUserError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-015", "The invocation of the user via user code doesn't exist in data base", "Check if there is a user code given on the NOVA DB", HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchJiraProjectKeyError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-016", "The product does not have a Jira Project Key", "Contact to JIRA team for getting a Jira project key and set it into this NOVA product", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getLogicalConnectorNotCreatedError()
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-017", "The logical connector is not on CREATED status or is not associated to any physical connector", "Wait until the logical connector is created by NOVA platform adminst", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getActionFrozenError(final String logicalConnectorName, final Integer logicalConnectorId, final String environment, final String userCode)
    {
        return new NovaError(CLASS_NAME, "LOGICALCONNECTOR-018","It´s not allowed in the logical connector name: [" + logicalConnectorName +"] - logical connector id: [" + logicalConnectorId + "] " +
                "to make this action in current environment: [" + environment +"] for user code: [" + userCode + "] due to the release is frozen",
                "Please, wait until the release automanage will be true or contact with NOVA Platform Admin if the action is urgently", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * Get an error when a DocSystem with the given parameters is not found.
     *
     * @param docSystemId       The given DocSystem's ID.
     * @param docSystemCategory The given DocSystem's category.
     * @param docSystemType     The given DocSystem's type.
     * @return The error.
     */
    public static NovaError getDocSystemNotFoundError(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType)
    {
        NovaError commonError = CommonError.getDocSystemNotFoundError(CLASS_NAME, docSystemId, docSystemCategory, docSystemType);
        commonError.setErrorCode("LOGICALCONNECTOR-019");
        return commonError;
    }

    public static NovaError getForbiddenError()
    {
        return new NovaError(CLASS_NAME, "USER-002","The current user does not have permission for this operation","Use a user wiht higher privilegies or request permission for your user", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * Get an error when some validation failed.
     *
     * @param errorDetail Error detail
     * @return The error
     */
    public static NovaError getValidationError(String errorDetail)
    {
        return new NovaError(CLASS_NAME,
                "LOGICALCONNECTOR-019",
                errorDetail,
                "Please use valid values and try again",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }
}
