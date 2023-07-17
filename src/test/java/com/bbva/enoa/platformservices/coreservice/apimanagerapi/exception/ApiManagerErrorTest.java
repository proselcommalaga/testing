package com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiManagerErrorTest
{
    @Test
    void getHttpStatus()
    {
        NovaError error = ApiManagerError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    void getErrorCode()
    {
        NovaError error = ApiManagerError.getUnexpectedError();

        String response = error.getErrorCode();

        assertNotNull(response);
        assertEquals("APIMANAGER-000", response);
    }

    @Test
    void getErrorMessage()
    {
        NovaError error = ApiManagerError.getUnexpectedError();

        String response = error.getErrorMessage();

        assertNotNull(response);
        assertEquals("Unexpected internal error", response);
    }

    @Test
    void getActionMessage()
    {
        NovaError error = ApiManagerError.getUnexpectedError();

        String response = error.getActionMessage();

        assertNotNull(response);
        assertEquals("Please, contact the NOVA Support team via JIRA: PNOVA", response);
    }

    @Test
    void getErrorMessageTypeTest()
    {
        NovaError error = ApiManagerError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        assertNotNull(response);
        assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    void toStringTest()
    {
        NovaError error = ApiManagerError.getUnexpectedError();

        assertNotNull(error);
        assertEquals("ApiManagerError{errorCode=APIMANAGER-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Support team via JIRA: PNOVA', "
                + "httpStatus='500', errorMessageType='FATAL'}", error.toString());
    }

    @Test
    void errorMessageOutputTest()
    {
        String getProductNotFoundErrorMsg = "Product with ID 1 not found";
        String getNovaApiNotFoundErrorMsg = "Api with ID 1 not found";
        String getInvalidApiForProductErrorMsg = "The Api with ID 1 does not belong to the product with ID 1";
        String getInvalidNewApiErrorMsg = "The API api with version 1.0.0 is already registered";
        String getServiceNotFoundErrorMsg = "Service with ID 1 not found";
        String getSwaggerToJsonErrorMsg = "Error writing the stored Swagger for UUAA uuaa with apiName api and version 1.0.0 into a file";
        String getInvalidFileFormatErrorMsg = "The requested file format xml is not supported";
        String getCreateRegisterErrorMsg = "There was an error registering api api for uuaa uuaa with base path /";
        String getRemoveRegisterErrorMsg = "There was an error removing the register of api api for uuaa uuaa with base path /";
        String getCreateApiTaskErrorMsg = "There was an error creating policies task for api api for uuaa uuaa with base path /";
        String getRecoverApiTaskErrorMsg = "There was an error recovering policies task for api api for uuaa uuaa with base path /";
        String getDeleteApiTaskListErrorMsg = "There was an error removing policies task for api api for uuaa uuaa with base path /";
        String getPlanApiDetailNotFoundErrorMsg = "There was an error recovering api detail for plan with id 1. The deployment plan not found";
        String getSavePlanProfilingNotFoundErrorMsg = "There was an error saving api profiling for plan with id 1. The deployment plan not found";
        String getPlanProfileNotFoundErrorMsg = "There was an error saving api profiling for plan with id 1. Plan profile not found";
        String getApiMethodNotFoundErrorMsg = "There was an error saving api profiling for plan with id 1. Nova api method with id 1 not found";
        String getApiMethodProfileNotFoundErrorMsg = "There was an error saving api profiling for plan with id 1. Api method profile for nova api method with id 1 not found";
        String getApiMethodProfileNotFoundError2Msg = "The api method profile for plan profile 1 with endpoint /test and verb GET does not exists";
        String getCesRoleNotFoundErrorMsg = "There was an error saving api profiling for plan with id 1. Ces Role with id 1 not found";
        String getRolesFromCESErrorMsg = "Error calling CES to recover roles from uuaa: uuaa in environment INT";
        String getApiPoliciesFromXMASErrorMsg = "Error calling XMAS to recover policies for api api for uuaa uuaa with basepath /";
        String getPoliciesTodoTaskPendingMsg = "A todo task already exists in pending state for the uuaa uuaa and API api";
        String getApiMethodProfileRolesNotFoundMsg = "The api method profile with id 1 for plan profile 1 does not have any role association";

        assertEquals(getProductNotFoundErrorMsg, ApiManagerError.getProductNotFoundError(1).getErrorMessage());
        assertEquals(getInvalidApiForProductErrorMsg, ApiManagerError.getInvalidApiForProductError(1, 1).getErrorMessage());
        assertEquals(getInvalidNewApiErrorMsg, ApiManagerError.getInvalidNewApiError("api", "1.0.0").getErrorMessage());
        assertEquals(getServiceNotFoundErrorMsg, ApiManagerError.getServiceNotFoundError(1).getErrorMessage());
        assertEquals(getSwaggerToJsonErrorMsg, ApiManagerError.getSwaggerToJsonError("uuaa", "api", "1.0.0").getErrorMessage());
        assertEquals(getInvalidFileFormatErrorMsg, ApiManagerError.getInvalidFileFormatError("xml").getErrorMessage());
        assertEquals(getCreateRegisterErrorMsg, ApiManagerError.getCreateRegisterError("uuaa", "/", "api").getErrorMessage());
        assertEquals(getRemoveRegisterErrorMsg, ApiManagerError.getRemoveRegisterError("uuaa", "/", "api").getErrorMessage());
        assertEquals(getCreateApiTaskErrorMsg, ApiManagerError.getCreateApiTaskError("uuaa", "/", "api").getErrorMessage());
        assertEquals(getRecoverApiTaskErrorMsg, ApiManagerError.getRecoverApiTaskError("uuaa", "/", "api").getErrorMessage());
        assertEquals(getDeleteApiTaskListErrorMsg, ApiManagerError.getDeleteApiTaskListError("uuaa", "/", "api").getErrorMessage());
        assertEquals(getPlanApiDetailNotFoundErrorMsg, ApiManagerError.getPlanApiDetailNotFoundError(1).getErrorMessage());
        assertEquals(getSavePlanProfilingNotFoundErrorMsg, ApiManagerError.getSavePlanProfilingNotFoundError(1).getErrorMessage());
        assertEquals(getPlanProfileNotFoundErrorMsg, ApiManagerError.getPlanProfileNotFoundError(1).getErrorMessage());
        assertEquals(getApiMethodNotFoundErrorMsg, ApiManagerError.getApiMethodNotFoundError(1, 1).getErrorMessage());
        assertEquals(getApiMethodProfileNotFoundErrorMsg, ApiManagerError.getApiMethodProfileNotFoundError(1, 1).getErrorMessage());
        assertEquals(getApiMethodProfileNotFoundError2Msg, ApiManagerError.getApiMethodProfileNotFoundError(1, "/test", "GET").getErrorMessage());
        assertEquals(getCesRoleNotFoundErrorMsg, ApiManagerError.getCesRoleNotFoundError(1, 1).getErrorMessage());
        assertEquals(getRolesFromCESErrorMsg, ApiManagerError.getRolesFromCESError("uuaa", "INT").getErrorMessage());
        assertEquals(getApiPoliciesFromXMASErrorMsg, ApiManagerError.getApiPoliciesFromXMASError("uuaa", "/", "api").getErrorMessage());
        assertEquals(getPoliciesTodoTaskPendingMsg, ApiManagerError.getPoliciesTodoTaskPending("uuaa", "api").getErrorMessage());
        assertEquals(getApiMethodProfileRolesNotFoundMsg, ApiManagerError.getApiMethodProfileRolesNotFound(1, 1).getErrorMessage());
    }
}