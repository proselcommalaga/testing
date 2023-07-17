package com.bbva.enoa.platformservices.coreservice.productsapi.exception;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

/**
 * Error code with all the internal errors for the Products API
 */
@ExposeErrorCodes
public class ProductsAPIError
{
    private static final String CLASS_NAME = "ProductsError";
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";
    public static final String UNEXPECTED_ERROR_CODE = "PRODUCTS-000";

    /**
     * error code from userservice when it is trying to remove the unique PO into a Product
     */
    public static final String USER_IS_UNIQUE_PRODUCT_OWNER_ERROR_CODE = "USER-005";

    public static String getClassName()
    {
        return CLASS_NAME;
    }

    public static NovaError getUnexpectedError()
    {
        return new NovaError(CLASS_NAME, UNEXPECTED_ERROR_CODE, ProductsAPIError.UNEXPECTED_ERROR_MSG, Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.FATAL);
    }

    public static NovaError getDatabaseConnectionError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-001", "Fail trying to connect or get data from NOVA BBDD", "Check the status or the connection to NOVA BBDD", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    public static NovaError getProductDoesNotExistError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-002", "Fail trying to find a product name from NOVA BBDD", "Check if the product name exist into the BBDD", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getProductAndUUAANotMatchError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-003", "Fail updating the product. The product to be updated does not much with the current uuaa", "Review the properties of the product", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getProductAlreadyExistError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-004", "Fail trying to create a new product. The product name is alredy exist", "Change or choose another product name", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getProductUUAAAlreadyExistError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-005", "Fail trying to create a new product. The uuaa for the new product name is alredy exist in other product", "Change the uuaa name", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getUserNotAllowedToCreateProductError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-006", "Fail trying to create a new product. The user is not product owner", "Please request the product creation to an user with the correct permissions", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getUserCodeDoesNotExistError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-007", "The user code does not exists", "Ensure that user code exist into BBVA master capacities and NOVA database", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getToDoTaskServiceCallError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-008", "Called to Todo task service failed", Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getFailToRemoveDueDeploymentStatusError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-009", "The current product to remove has some deployment plan in status 'DEPLOYED'", "Stop all actives deployment plans of this product and try again", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getProductOwnerRoleDoesNotExistError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-010", "Fail trying to create a new product. The product owner role does not exist into BBDD. The product creation can not continue", "Ensure Batch Master capacities are working well and the Product owner role exist and match", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getNovaToolsError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-011", "Fail trying to create the NOVA tools request", "Review the Automator services and check if it is working well", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    public static NovaError getFailSendMailError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-012", "Fail trying to send a mail", "Review the mail service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    public static NovaError getAddUserToExternalToolOfProductFailError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-013", "Fail trying to add an user to external tool of the product", "Review the Automator services and check if it is working well", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getRemoveExternalToolOfProductFailError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-014", "Fail trying to remove the external tool of the product", "Review the Automator services and check if it is working well", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getIncorrectUUAAError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-015", "Product UUAA format is not valid", "UUAA must have exactly 4 letters or numbers, not special chars", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getIncorrectUUAAErrorBadRequest()
    {
        NovaError novaError = getIncorrectUUAAError();
        novaError.setHttpStatus(HttpStatus.BAD_REQUEST);
        return novaError;
    }

    public static NovaError getFailToRemoveDuePendingTaskError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-016", "The current product to remove has some pending task active", "Remove all pending task of this product and try again", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getFailToRemoveDueActiveFilesystemError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-017", "Product cannot be deleted since it has at least 1 active filesystem", "Archive or delete all active filesystems and try again", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getUserCodeIsNotSecurityOrPlatformError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-018", "Fail trying to set the critical level in a product. The user code is not Platform Admin or Security", "Select a user code who be a Plaftorm Admin or Security Role for setting the critical level", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getCallToUsersApiError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-019", "Error in invocation to Users Api", "Review the Users Service and check if it is working well", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    public static NovaError getFailToRemoveDueNovaApiError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-020", "You are trying to remove a product with some NOVA apis", "Delete all NOVA apis and try again", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getRemoveProductOwnerUserFailError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-021", "Fail trying to remove a user from the product that is the only product owner", "Before removing a product owner, ensure that exists almost another product owner. It means, you must first add a new product owner before removing any product owner", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getSubsystemAlreadyExistsError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-022", "The subsystem name already exists into the product", "Create a new subsystem with other subsystem name", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getSubsysteDoesntExistError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-023", "The subsystem does not exist into the product", "There is no subsystem to manage", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getUserAlreadyExistsProductError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-024", "The user is already exists into the product", "The user cannot be added due is already in the product", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getCallToProductToolsApiError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-025", "Error in invocation to ProductTools Api", "Review the Tools Service and check if it is working well", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    public static NovaError getProductNameExistingUUAAError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-026", "The product name already exists as UUAA of other product into the BBDD", "Fail trying to create a new product. The product name already exists as UUAA of other product into the BBDD", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    public static NovaError getForbiddenError()
    {
        return new NovaError(CLASS_NAME, "USER-002", "The current user does not have permission for this operation", "Use a user wiht higher privilegies or request permission for your user", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getIncorrectProductNameError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-027", "Product name format is not valid", "Product name must not contain forbidden characters like parenthesis", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getUserIsNotInTeamRoleError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-028", "User cannot be added due is not in the team for the role", "Check user allows to the team associated to the role", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getFailToRemoveBatchScheduleError()
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-029", "You are trying to remove a product with some batch schedule document associated", "Delete Control-M Batch schedules and try again", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    /**
     * Error when trying to configure a forbidden deployment platform
     *
     * @param environment the environment
     * @return Nova Error
     */
    public static NovaError getForbiddenPlatformDeployError(String environment)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-030",
                "The selected deployment platform is not an enabled option in " + environment,
                "Change the deployment platform options to enable it",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to configure a forbidden logging platform
     *
     * @param environment the environment
     * @return Nova Error
     */
    public static NovaError getForbidenPlatformLoggingError(String environment)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-030",
                "The selected logging platform is not an enabled option in " + environment,
                "Change the logging platform options to enable it",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to use a product name with an invalid length
     *
     * @param productName The name of a product
     * @param minLength   The minimum length allowed for a product name
     * @param maxLength   The maximum length allowed for a product name
     * @return Nova Error
     */
    public static NovaError getInvalidProductNameLengthError(String productName, int minLength, int maxLength)
    {
        return new NovaError(CLASS_NAME, "PRODUCTS-031", String.format("The length of the name %s is not valid", productName), String.format("The minimum length is %s, and the maximum %s", minLength, maxLength), HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }


    /**
     * Trying to remove the unique Product Owner of a product
     *
     * @param userCode  code to delete
     * @param productId product id
     * @return a NovaError
     */
    public static NovaError getUniqueProductOwnerError(final String userCode, final int productId)
    {
        return new NovaError(CLASS_NAME,
                USER_IS_UNIQUE_PRODUCT_OWNER_ERROR_CODE,
                "User [" + userCode + "] is the only product owner of the product [" + productId + "]",
                "Contact the NOVA Admin team",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to configure an invalid logging platform
     *
     * @param loggingType selected logging platform
     * @param deployType  selected deployment platform
     * @return Nova Error
     */
    public static NovaError getInvalidLoggingPlatformForSelectedDeploymentPlatformError(final String loggingType, final String deployType)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-032",
                "The selected logging platform [" + loggingType + "] is not a valid option when the deployment platform is [" + deployType + "]",
                "Change the selected logging platform",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to configure an invalid logging platform
     *
     * @param loggingType selected logging platform
     * @param deployType  selected deployment platform
     * @param environment the environment
     * @return Nova Error
     */
    public static NovaError getInvalidLoggingPlatformForSelectedDeploymentPlatformInEnvironmentError(final String loggingType, final String deployType, final String environment)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-033",
                "The selected logging platform [" + loggingType + "] in environment [" + environment + "] is not a valid option when the deployment platform is [" + deployType + "]",
                "Change the selected logging platform",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to configure same deployment namespace in multiple environments
     *
     * @return Nova Error
     */
    public static NovaError getDuplicatedDeployNamespaceError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-034",
                "Namespace for deployments cannot be used in multiple environments",
                "Change the repeated namespace",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to configure same logging namespace in multiple environments
     *
     * @return Nova Error
     */
    public static NovaError getDuplicatedLoggingNamespaceError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-035",
                "Namespace for logging cannot be used in multiple environments",
                "Change the repeated namespace",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to configure same logging namespace in multiple environments
     *
     * @param errorMessage the error message
     * @param environment  the environment
     * @param productId    the product id
     * @return Nova Error
     */
    public static NovaError getFilesystemReport(final String errorMessage, final Integer productId, final String environment)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-036",
                "The filesystem usage report cannot be generated for product id: [" + productId + "] - environment: [" + environment + "] due to a error in Filesystem manager, error message: [" + errorMessage + "]",
                "Please, wait several minutes and try it again later",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when creating log rate threshold events for a product
     *
     * @return Nova Error
     */
    public static NovaError getCreatingLogRateThresholdEventsError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-037",
                "Error creating log rate threshold events",
                "Check logsmanager service is running ok",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when  creating log rate threshold events for a product
     *
     * @return Nova Error
     */
    public static NovaError getDeletingLogRateThresholdEventsError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-038",
                "Error deleting log rate threshold events",
                "Check logsmanager service is running ok",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    public static NovaError getEmailNotAvailableError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-039",
                "User has not email assigned from capacities master user files. Review the action list for solving this issue.",
                "Action list: 1er Option- Wait a few days until the BBVA registration process is completed or contact NOVA support team if user has an email assigned and it is not registered in the portal." +
                        "2º option: The user code could be disabled or not in use. In this case, this user code cannot be register into NOVA Platform. Please, try to do this action with a enabled user code.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Error when trying to configure same logging namespace in multiple environments
     *
     * @param errorMessage the error message
     * @param uuaa         the uuaa
     * @param environment  the environment
     * @return Nova Error
     */
    public static NovaError getProductsAssignedResourcesReportError(final String errorMessage, final String uuaa, final String environment)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-040",
                "Error obtaining the information about the product resource report for uuaa: [" + uuaa + "] and environment: [" + environment + "] due to a error in Product Report Service, error message: " +
                        "[" + errorMessage + "]",
                "Please, wait several minutes and try it again later. If the error persist, contact with the NOVA admin team",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * Error when trying to update a product with a wrong email field
     *
     * @return Nova Error
     */
    public static NovaError getWrongEmailFormatError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-041",
                "Product email with wrong format",
                "Please, check the email format and remember to use only one email account",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError getNoCPDsetForMonoCPDInPro()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-42",
                "Product deployment configuration must have CPD assigned in monoCPD",
                "Please, before safe product configuration check if monoCPD in PRO have a CPD assigned",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    public static NovaError getInvalidReleaseSlotsNumberError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-043",
                "The number of release slots cannot be higher than 4 neither less than 2",
                "Please, insert a amount between 2 and 4",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Error when  deleting log events for a product
     *
     * @return Nova Error
     */
    public static NovaError getDeletingLogEventsError()
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-044",
                "Error deleting log events",
                "Check logsmanager service is running ok",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    public static NovaError getInvalidEnvironmentError(String environment)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-045",
                "Error getting Environment value from [" + environment + "] string",
                "Check environment value",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    public static NovaError getInvalidDeploymentTypeError(String deploymentType)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-046",
                "Error getting DeploymentType value from [" + deploymentType + "] string",
                "Check DeploymentType value",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    public static NovaError getInvalidPlatformType(String platform)
    {
        return new NovaError(CLASS_NAME,
                "PRODUCTS-047",
                "Error getting Platform Enum from [" + platform + "]",
                "Check Platform value",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

}