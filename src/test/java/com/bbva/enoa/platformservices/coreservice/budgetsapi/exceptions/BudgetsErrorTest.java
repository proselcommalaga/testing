package com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions;

import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class BudgetsErrorTest
{
    @Test
    void getUnexpectedError()
    {
        NovaError error = BudgetsError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=BUDGETSAPI-000, " +
                        "errorMessage='Unexpected internal error', " +
                        "actionMessage='Please, contact the NOVA Support team via JIRA: PNOVA', " +
                        "httpStatus='500', " +
                        "errorMessageType='FATAL'}",
                error.toString());
    }

    @Test
    void getProductBudgetsApiError()
    {
        NovaError error = BudgetsError.getProductBudgetsApiError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=BUDGETSAPI-001, " +
                        "errorMessage='Error in ProductBudgets integration', " +
                        "actionMessage='Check product budgets service', " +
                        "httpStatus='500', " +
                        "errorMessageType='ERROR'}",
                error.toString());
    }

    @Test
    void getDeploymentPlanNotFoundError()
    {
        NovaError error = BudgetsError.getDeploymentPlanNotFoundError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=BUDGETSAPI-002, " +
                        "errorMessage='Deployment plan not found', " +
                        "actionMessage='Check plan existence', " +
                        "httpStatus='404', " +
                        "errorMessageType='ERROR'}",
                error.toString());
    }

    @Test
    void getInvalidDateValueError()
    {
        NovaError error = BudgetsError.getInvalidDateValueError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=BUDGETSAPI-003, " +
                        "errorMessage='Invalid date value', " +
                        "actionMessage='Check date format', " +
                        "httpStatus='400', " +
                        "errorMessageType='WARNING'}",
                error.toString());
    }

    @Test
    void getClusterNotFoundError()
    {
        NovaError error = BudgetsError.getClusterNotFoundError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=BUDGETSAPI-004, " +
                        "errorMessage='Cluster not found', " +
                        "actionMessage='Check clusters', " +
                        "httpStatus='400', " +
                        "errorMessageType='ERROR'}",
                error.toString());
    }

    @Test
    void getInvalidDateFormatError()
    {
        NovaError error = BudgetsError.getInvalidDateFormatError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=BUDGETSAPI-005, " +
                        "errorMessage='Invalid date format', " +
                        "actionMessage='Check date format', " +
                        "httpStatus='400', " +
                        "errorMessageType='WARNING'}",
                error.toString());
    }

    @Test
    void getForbiddenError()
    {
        NovaError error = BudgetsError.getForbiddenError();
        HttpStatus response = error.getHttpStatus();

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response);

        assertNotNull(error);
        assertEquals("BudgetsError{errorCode=USER-002, " +
                        "errorMessage='The current user does not have permission for this operation', " +
                        "actionMessage='Use a user with higher privileges or request permission for your user', " +
                        "httpStatus='403', " +
                        "errorMessageType='ERROR'}",
                error.toString());
    }
}
