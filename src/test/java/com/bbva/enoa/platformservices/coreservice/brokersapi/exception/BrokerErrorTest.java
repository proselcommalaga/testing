package com.bbva.enoa.platformservices.coreservice.brokersapi.exception;

import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_AGENT_ERROR;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_CREDENTIALS_NOT_FOUND_ERROR;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_NAME_ALREADY_EXISTS_IN_ENV_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_NOT_FOUND_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_NOT_FOUND_IN_DEPLOYMENT_SERVICE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_OPERATION_NOT_VALID_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_CREATION_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_DELETION_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_RESTART_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_START_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_STOP_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.FILESYSTEM_NOT_FOUND_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.HARDWAREPACK_NOT_FOUND_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.IMPOSSIBLE_STOP_BROKER_USED_BY_DEPLOYMENT_SERVICES_ERROR;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.IMPOSSIBLE_STOP_BROKER_USED_BY_RUNNING_SERVICES_ERROR;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.INVALID_BROKER_NAME_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.INVALID_FILESYSTEM_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.INVALID_HARDWAREPACK_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.MAX_BROKERS_BY_ENV_LIMIT_REACHED_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.NOT_ENOUGH_BROKER_BUDGET_ERROR;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.PERMISSION_DENIED_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.PRODUCT_NOT_FOUND_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_STATUS_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.UNSUPPORTED_BROKER_PLATFORM_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.UNSUPPORTED_BROKER_TYPE_ERROR_CODE;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.USER_ADMIN_NOT_FOUND_IN_BROKER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BrokerErrorTest
{
    private static final Integer BROKER_ID = 1;
    private static final Integer PRODUCT_ID = 2;

    @Test
    void getHttpStatus()
    {
        NovaError error = BrokerError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    void getErrorCode()
    {
        NovaError error = BrokerError.getUnexpectedError();

        String response = error.getErrorCode();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("BROKER-000", response);
    }

    @Test
    void getErrorMessage()
    {
        NovaError error = BrokerError.getUnexpectedError();

        String response = error.getErrorMessage();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Unexpected internal error", response);
    }

    @Test
    void getActionMessage()
    {
        NovaError error = BrokerError.getUnexpectedError();

        String response = error.getActionMessage();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Please, contact the NOVA Support team via JIRA: PNOVA", response);
    }

    @Test
    void getUnexpectedError()
    {
        NovaError error = BrokerError.getUnexpectedError();

        String response = error.getErrorCode();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("BROKER-000", response);
    }

    @Test
    void getErrorMessageTypeTest()
    {
        NovaError error = BrokerError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    void toStringTest()
    {
        NovaError error = BrokerError.getUnexpectedError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals("BrokerErrorCode{errorCode=BROKER-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Support team via JIRA: PNOVA', "
                + "httpStatus='500', errorMessageType='FATAL'}", error.toString());
    }

    @Nested
    class getPermissionDeniedError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getPermissionDeniedError();
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(PERMISSION_DENIED_ERROR_CODE, error.getErrorCode());

        }
    }

    @Nested
    class getBrokerNotFoundError
    {
        Integer brokerId = 1234;

        @Test
        void ok()
        {
            NovaError error = BrokerError.getBrokerNotFoundError(brokerId);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(BROKER_NOT_FOUND_ERROR_CODE, error.getErrorCode());

        }

    }

    @Nested
    class getUnexpectedBrokerStatusError
    {

        @Test
        void ok()
        {
            NovaError error = BrokerError.getUnexpectedBrokerStatusError("status");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(UNEXPECTED_BROKER_STATUS_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getBrokerOperationNotValidError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getBrokerOperationNotValidError("invalidOperation");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(BROKER_OPERATION_NOT_VALID_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getProductNotFoundError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getProductNotFoundError(PRODUCT_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(PRODUCT_NOT_FOUND_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getFilesystemNotFoundError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getFilesystemNotFoundError(1);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(FILESYSTEM_NOT_FOUND_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getInvalidFilesystemError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getInvalidFilesystemError(1);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(INVALID_FILESYSTEM_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getHardwarePackNotFoundError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getHardwarePackNotFoundError(1);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(HARDWAREPACK_NOT_FOUND_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getInvalidHardwarePackError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getInvalidHardwarePackError(1);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(INVALID_HARDWAREPACK_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getUnsupportedBrokerTypeError
    {
        @Test
        void ErrorHTTPStatusTest()
        {
            NovaError error = BrokerError.getUnsupportedBrokerTypeError("brokerType");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(UNSUPPORTED_BROKER_TYPE_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getUnsupportedBrokerPlatformError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getUnsupportedBrokerPlatformError("platform");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(UNSUPPORTED_BROKER_PLATFORM_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getMaxBrokersReachedError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getMaxBrokersReachedError();
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(MAX_BROKERS_BY_ENV_LIMIT_REACHED_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getGivenBrokerNameAlreadyExistsError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getGivenBrokerNameAlreadyExistsError("brokername");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(BROKER_NAME_ALREADY_EXISTS_IN_ENV_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getInvalidBrokerNameError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getInvalidBrokerNameError("brokerName");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(INVALID_BROKER_NAME_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getFailedBrokerDeploymentCreateError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getFailedBrokerDeploymentCreateError(BROKER_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
            assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
            Assertions.assertEquals(FAILED_BROKER_DEPLOYMENT_CREATION_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getFailedBrokerDeploymentDeleteError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getFailedBrokerDeploymentDeleteError(BROKER_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
            assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
            Assertions.assertEquals(FAILED_BROKER_DEPLOYMENT_DELETION_ERROR_CODE, error.getErrorCode());
        }


    }

    @Nested
    class getFailedBrokerDeploymentStartError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getFailedBrokerDeploymentStartError(BROKER_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
            assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
            Assertions.assertEquals(FAILED_BROKER_DEPLOYMENT_START_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getFailedBrokerDeploymentStopError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getFailedBrokerDeploymentStopError(BROKER_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
            assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
            Assertions.assertEquals(FAILED_BROKER_DEPLOYMENT_STOP_ERROR_CODE, error.getErrorCode());
        }
    }

    @Nested
    class getFailedBrokerDeploymentRestartError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getFailedBrokerDeploymentRestartError(BROKER_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
            assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
            Assertions.assertEquals(FAILED_BROKER_DEPLOYMENT_RESTART_ERROR_CODE, error.getErrorCode());
        }

    }

    @Nested
    class getNotEnoughBrokerBudgetError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getNotEnoughBrokerBudgetError("PRO");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(NOT_ENOUGH_BROKER_BUDGET_ERROR, error.getErrorCode());
        }

    }

    @Nested
    class getCredentialsNotFoundForRoleError
    {

        @Test
        void ok()
        {
            NovaError error = BrokerError.getCredentialsNotFoundForRoleError("Role", BROKER_ID);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(BROKER_CREDENTIALS_NOT_FOUND_ERROR, error.getErrorCode());
        }


    }

    @Nested
    class getBrokerUsedByRunningServicesError
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getBrokerUsedByRunningServicesError("brokername");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(IMPOSSIBLE_STOP_BROKER_USED_BY_RUNNING_SERVICES_ERROR, error.getErrorCode());
        }

    }

    @Nested
    class getBrokerUsedByServicesError
    {
        @Test
        void ErrorHTTPStatusTest()
        {
            NovaError error = BrokerError.getBrokerUsedByServicesError("brokername");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            assertEquals(HttpStatus.FORBIDDEN, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
            Assertions.assertEquals(IMPOSSIBLE_STOP_BROKER_USED_BY_DEPLOYMENT_SERVICES_ERROR, error.getErrorCode());
        }

    }

    @Nested
    class getErrorCallingBrokerAgentOnEnvironment
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getErrorCallingBrokerAgentOnEnvironment(Environment.PRE.name(), "msg");
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            Assertions.assertEquals(BROKER_AGENT_ERROR, error.getErrorCode());
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
            assertEquals(ErrorMessageType.CRITICAL, error.getErrorMessageType());
        }

    }

    @Nested
    class getBrokerNotFoundInDeploymentService
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getBrokerNotFoundInDeploymentService(1);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            Assertions.assertEquals(BROKER_NOT_FOUND_IN_DEPLOYMENT_SERVICE, error.getErrorCode());
            assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        }

    }

    @Nested
    class getUserAdminNotFoundInBroker
    {
        @Test
        void ok()
        {
            NovaError error = BrokerError.getUserAdminNotFoundInBroker(1);
            HttpStatus response = error.getHttpStatus();

            assertNotNull(response);
            Assertions.assertEquals(USER_ADMIN_NOT_FOUND_IN_BROKER, error.getErrorCode());
            assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
            assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        }
    }

}