package com.bbva.enoa.platformservices.coreservice.productsapi.exception;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ProductsAPIErrorTest
{
    @Test
    public void getHttpStatus()
    {
        NovaError error = ProductsAPIError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getErrorCode()
    {
        NovaError error = ProductsAPIError.getUnexpectedError();

        String response = error.getErrorCode();

        assertNotNull(response);
        assertEquals("PRODUCTS-000", response);
    }

    @Test
    public void getErrorMessage()
    {
        NovaError error = ProductsAPIError.getUnexpectedError();

        String response = error.getErrorMessage();

        assertNotNull(response);
        assertEquals("Unexpected internal error", response);
    }

    @Test
    public void getActionMessage()
    {
        NovaError error = ProductsAPIError.getUnexpectedError();

        String response = error.getActionMessage();

        assertNotNull(response);
        assertEquals("Please, contact the NOVA Support team via JIRA: PNOVA", response);
    }

    @Test
    public void getErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    public void toStringTest()
    {
        NovaError error = ProductsAPIError.getUnexpectedError();

        assertNotNull(error);
        assertEquals("ProductsError{errorCode=PRODUCTS-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Support team via JIRA: PNOVA', "
                + "httpStatus='500', errorMessageType='FATAL'}", error.toString());
    }

    @Test
    public void getClassNameTest()
    {
        assertEquals(ProductsAPIError.getClassName(), "ProductsError");
    }



//###############################################################################//
    @Test
    public void getDatabaseConnectionErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getDatabaseConnectionError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getDatabaseConnectionErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getDatabaseConnectionError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.CRITICAL, response);
    }

//###############################################################################//

    @Test
    public void getProductDoesNotExistErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getProductDoesNotExistError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getProductDoesNotExistErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getProductDoesNotExistError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

//###############################################################################//

    @Test
    public void getProductAndUUAANotMatchErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getProductAndUUAANotMatchError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getProductAndUUAANotMatchErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getProductAndUUAANotMatchError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

//###############################################################################//

    @Test
    public void getProductAlreadyExistErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getProductAlreadyExistError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getProductAlreadyExistErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getProductAlreadyExistError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

//###############################################################################//

    @Test
    public void getProductUUAAAlreadyExistErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getProductUUAAAlreadyExistError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getProductUUAAAlreadyExistErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getProductUUAAAlreadyExistError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }


//###############################################################################//

    @Test
    public void getUserNotAllowedToCreateProductErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getUserNotAllowedToCreateProductError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getUserNotAllowedToCreateProductErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUserNotAllowedToCreateProductError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

//###############################################################################//

    @Test
    public void getUserCodeDoesNotExistErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getUserCodeDoesNotExistError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getUserCodeDoesNotExistErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUserCodeDoesNotExistError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }


//###############################################################################//

    @Test
    public void getToDoTaskServiceCallErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getToDoTaskServiceCallError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getToDoTaskServiceCallErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getToDoTaskServiceCallError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    @Test
    public void getToDoTaskServiceCallErrorActionMessage()
    {
        NovaError error = ProductsAPIError.getToDoTaskServiceCallError();

        String response = error.getActionMessage();

        assertNotNull(response);
        assertEquals("Please, contact the NOVA Support team via JIRA: PNOVA", response);
    }

//###############################################################################//


    @Test
    public void getFailToRemoveDueDeploymentStatusErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDueDeploymentStatusError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getFailToRemoveDueDeploymentStatusErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDueDeploymentStatusError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

//###############################################################################//


    @Test
    public void getProductOwnerRoleDoesNotExistErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getProductOwnerRoleDoesNotExistError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getProductOwnerRoleDoesNotExistErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getProductOwnerRoleDoesNotExistError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }



    //###############################################################################//


    @Test
    public void getNovaToolsErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getNovaToolsError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getNovaToolsErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getNovaToolsError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.CRITICAL, response);
    }

    //###############################################################################//


    @Test
    public void getFailSendMailErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getFailSendMailError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getFailSendMailErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getFailSendMailError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.CRITICAL, response);
    }

    //###############################################################################//


    @Test
    public void getAddUserToExternalToolOfProductFailErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getAddUserToExternalToolOfProductFailError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getAddUserToExternalToolOfProductFailErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getAddUserToExternalToolOfProductFailError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getRemoveExternalToolOfProductFailErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getRemoveExternalToolOfProductFailError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getRemoveExternalToolOfProductFailErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getRemoveExternalToolOfProductFailError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getIncorrectUUAAErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getIncorrectUUAAError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getIncorrectUUAAErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getIncorrectUUAAError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

    //###############################################################################//


    @Test
    public void getFailToRemoveDuePendingTaskErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDuePendingTaskError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getFailToRemoveDuePendingTaskErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDuePendingTaskError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

    //###############################################################################//


    @Test
    public void getFailToRemoveDueActiveFilesystemErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDueActiveFilesystemError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getFailToRemoveDueActiveFilesystemErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDueActiveFilesystemError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }


    //###############################################################################//


    @Test
    public void getUserCodeIsNotSecurityOrPlatformErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getUserCodeIsNotSecurityOrPlatformError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getUserCodeIsNotSecurityOrPlatformErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUserCodeIsNotSecurityOrPlatformError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }


    //###############################################################################//


    @Test
    public void getCallToUsersApiErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getCallToUsersApiError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getCallToUsersApiErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getCallToUsersApiError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.CRITICAL, response);
    }


    //###############################################################################//


    @Test
    public void getFailToRemoveDueNovaApiErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDueNovaApiError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getFailToRemoveDueNovaApiErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getFailToRemoveDueNovaApiError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }


    //###############################################################################//


    @Test
    public void getRemoveProductOwnerUserFailErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getRemoveProductOwnerUserFailError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getRemoveProductOwnerUserFailErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getRemoveProductOwnerUserFailError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }


    //###############################################################################//


    @Test
    public void getSubsystemAlreadyExistsErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getSubsystemAlreadyExistsError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getSubsystemAlreadyExistsErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getSubsystemAlreadyExistsError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

    //###############################################################################//


    @Test
    public void getSubsysteDoesntExistErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getSubsysteDoesntExistError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getSubsysteDoesntExistErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getSubsysteDoesntExistError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getUserAlreadyExistsProductErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getUserAlreadyExistsProductError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getUserAlreadyExistsProductErrorrMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUserAlreadyExistsProductError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

    //###############################################################################//


    @Test
    public void getCallToProductToolsApiErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getCallToProductToolsApiError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getCallToProductToolsApiErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getCallToProductToolsApiError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.CRITICAL, response);
    }


    //###############################################################################//


    @Test
    public void getForbiddenErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getForbiddenError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response);
    }

    @Test
    public void getForbiddenErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getForbiddenError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getIncorrectProductNameErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getIncorrectProductNameError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getIncorrectProductNameErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getIncorrectProductNameError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getUserIsNotInTeamRoleErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getUserIsNotInTeamRoleError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getUserIsNotInTeamRoleErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUserIsNotInTeamRoleError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

    //###############################################################################//


    @Test
    public void getCreatingLogRateThresholdEventsErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getCreatingLogRateThresholdEventsError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getCreatingLogRateThresholdEventsErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getCreatingLogRateThresholdEventsError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getDeletingLogRateThresholdEventsErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getDeletingLogRateThresholdEventsError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getDeletingLogRateThresholdEventsErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getDeletingLogRateThresholdEventsError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getHTTPTest()
    {
        NovaError error = ProductsAPIError.getForbiddenError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response);
    }

    @Test
    public void getMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getForbiddenError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }


    //###############################################################################//


    @Test
    public void getForbidenPlatformDeployErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getForbiddenPlatformDeployError("PRO");
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getForbidenPlatformDeployErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getForbiddenPlatformDeployError("PRO");

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getForbidenPlatformLoggingErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getForbidenPlatformLoggingError("PRO");
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getForbidenPlatformLoggingErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getForbidenPlatformLoggingError("PRO");

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getInvalidProductNameLengthErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getInvalidProductNameLengthError("PRODUCT",4,10);
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getInvalidProductNameLengthErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getInvalidProductNameLengthError("PRODUCT",4,10);

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }



    //###############################################################################//


    @Test
    public void getUniqueProductOwnerErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getUniqueProductOwnerError("XE00004", 12345);
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getUniqueProductOwnerErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getUniqueProductOwnerError("XE00004", 12345);

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    @Test
    public void getInvalidProductNameLengthErrorErrorCodeTest()
    {
        NovaError error = ProductsAPIError.getUniqueProductOwnerError("XE00004", 12345);

        String response = error.getErrorCode();

        assertNotNull(response);
        assertEquals("USER-005", response);
    }


    //###############################################################################//



    @Test
    public void getInvalidLoggingPlatformForSelectedDeploymentPlatformErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getInvalidLoggingPlatformForSelectedDeploymentPlatformError("NOVA","NOVA");
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getInvalidLoggingPlatformForSelectedDeploymentPlatformErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getInvalidLoggingPlatformForSelectedDeploymentPlatformError("NOVA","NOVA");

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getInvalidLoggingPlatformForSelectedDeploymentPlatformInEnvironmentErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getInvalidLoggingPlatformForSelectedDeploymentPlatformInEnvironmentError("NOVA","NOVA","PRO");
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getInvalidLoggingPlatformForSelectedDeploymentPlatformInEnvironmentErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getInvalidLoggingPlatformForSelectedDeploymentPlatformInEnvironmentError("NOVA","NOVA", "PRO");

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//



    @Test
    public void getDuplicatedDeployNamespaceErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getDuplicatedDeployNamespaceError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getDuplicatedDeployNamespaceErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getDuplicatedDeployNamespaceError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//



    @Test
    public void getDuplicatedLoggingNamespaceErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getDuplicatedLoggingNamespaceError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);
    }

    @Test
    public void getDuplicatedLoggingNamespaceErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getDuplicatedLoggingNamespaceError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.ERROR, response);
    }

    //###############################################################################//


    @Test
    public void getProductNameExistingUUAAErrorHTTPTest()
    {
        NovaError error = ProductsAPIError.getProductNameExistingUUAAError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getProductNameExistingUUAAErrorMessageTypeTest()
    {
        NovaError error = ProductsAPIError.getProductNameExistingUUAAError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

    @Test
    public void getWrongEmailFormatErrorTest()
    {
        NovaError error = ProductsAPIError.getWrongEmailFormatError();
        ErrorMessageType response = error.getErrorMessageType();

        assertEquals("PRODUCTS-041", error.getErrorCode());
        assertNotNull(response);
        assertEquals(ErrorMessageType.WARNING, response);
    }

}