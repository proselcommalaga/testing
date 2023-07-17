package com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions;

import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


class StatisticsErrorTest
{
    @Test
    void getHttpStatus()
    {
        NovaError error = StatisticsError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    void getErrorCode()
    {
        NovaError error = StatisticsError.getUnexpectedError();

        String response = error.getErrorCode();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("STATISTICS-000", response);
    }

    @Test
    void getErrorMessage()
    {
        NovaError error = StatisticsError.getUnexpectedError();

        String response = error.getErrorMessage();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Unexpected internal error", response);
    }

    @Test
    void getActionMessage()
    {
        NovaError error = StatisticsError.getUnexpectedError();

        String response = error.getActionMessage();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Please, contact the NOVA Admin team", response);
    }

    @Test
    void getErrorMessageTypeTest()
    {
        NovaError error = StatisticsError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    void toStringTest()
    {
        NovaError error = StatisticsError.getUnexpectedError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals("StatisticsError{errorCode=STATISTICS-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Admin team', httpStatus='500', errorMessageType='FATAL'}", error.toString());
    }

    @Test
    void getUnexpectedErrorTest()
    {
        NovaError error = StatisticsError.getUnexpectedError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.UNEXPECTED_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Unexpected internal error", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.FATAL, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getReleaseVersionStatusNotValidErrorTest()
    {
        final String status = "test";
        NovaError error = StatisticsError.getReleaseVersionStatusNotValidError(status);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.RELEASE_VERSION_STATUS_NOT_VALID_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("Release Versions status [%s] is not valid.", status), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(String.format("Please use one of the following valid values: %s, or %s (to not filter by Release Version Status)", Arrays.toString(ReleaseVersionStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER), error.getActionMessage());
    }

    @Test
    void getUaaaNotUniqueErrorTest()
    {
        final String uuaa = "nova";
        final List<Product> products = new ArrayList<>();
        NovaError error = StatisticsError.getUaaaNotUniqueError(uuaa, products);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.UUAA_NOT_UNIQUE_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("There is more than one product with UUAA [%s]: %s", uuaa, products), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getUserServiceErrorTest()
    {
        NovaError error = StatisticsError.getUserServiceError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.USER_SERVICE_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Error calling User Service", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.CRITICAL, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getTodoTaskServiceErrorTest()
    {
        NovaError error = StatisticsError.getTodoTaskServiceError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.TODO_TASK_SERVICE_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Error calling TODO Task Service", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.CRITICAL, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getContinuousIntegrationErrorTest()
    {
        NovaError error = StatisticsError.getContinuousIntegrationError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.CONTINUOUS_INTEGRATION_SERVICE_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Error calling Continuous Integration Service", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.CRITICAL, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getNotImplementedYetErrorTest()
    {
        NovaError error = StatisticsError.getNotImplementedYetError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.UNEXPECTED_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Not implemented yet", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getEnvironmentNotValidErrorTest()
    {
        final String environment = "PRO";
        NovaError error = StatisticsError.getEnvironmentNotValidError(environment);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.ENVIRONMENT_NOT_VALID_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("Environment [%s] is not valid.", environment), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(String.format("Please use one of the following valid values: %s, or %s (to not filter by environment)", Arrays.toString(Environment.values()), StatisticsConstants.NO_FILTER_PARAMETER), error.getActionMessage());
    }

    @Test
    void getLogicalConnectorStatusNotValidErrorTest()
    {
        final String logicalConnectorStatus = "testing";
        NovaError error = StatisticsError.getLogicalConnectorStatusNotValidError(logicalConnectorStatus);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.LOGICAL_CONNECTOR_STATUS_NOT_VALID_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("Logical Connector Status [%s] is not valid.", logicalConnectorStatus), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(String.format("Please use one of the following valid values: %s, or %s (to not filter by Logical Connector Status)", Arrays.toString(LogicalConnectorStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER), error.getActionMessage());
    }

    @Test
    void getPlatformNotValidErrorTest()
    {
        final String platform = "platform";
        NovaError error = StatisticsError.getPlatformNotValidError(platform);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.PLATFORM_NOT_VALID_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("Platform [%s] is not valid.", platform), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(String.format("Please use one of the following valid values: %s", Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList())), error.getActionMessage());
    }

    @Test
    void getAlertServiceErrorTest()
    {
        NovaError error = StatisticsError.getAlertServiceError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.ALERT_SERVICE_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Error calling Alert Service", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.CRITICAL, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getFilesystemStatusNotValidErrorTest()
    {
        final String status = "statusTest";
        NovaError error = StatisticsError.getFilesystemStatusNotValidError(status);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.FILESYSTEM_STATUS_NOT_VALID_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("Status [%s] is not valid.", status), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(String.format("Please use one of the following valid values: %s, or %s (to not filter by Filesystem Status)", Arrays.toString(FilesystemStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER), error.getActionMessage());
    }

    @Test
    void generateDateTimeParseErrorTest()
    {
        final String dateTimeFormat = "formatTest";
        NovaError error = StatisticsError.generateDateTimeParseError(dateTimeFormat);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.DATE_TIME_PARSE_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Date format not expected. The given date does not support the expected format."+ dateTimeFormat, error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals("Review if date is complete.", error.getActionMessage());
    }

    @Test
    void generateAlreadyRunningJobErrorTest()
    {
        NovaError error = StatisticsError.generateAlreadyRunningJobError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.ALREADY_RUNNING_JOB_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("The current job already has another execution in progress.", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals("Please, wait until execution ends, or kill execution.", error.getActionMessage());
    }

    @Test
    void generateNotRestartableJobErrorTest()
    {
        NovaError error = StatisticsError.generateNotRestartableJobError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.NOT_RESTARTABLE_JOB_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("This job is not restartable, but a restart has been tried.", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(" Please, remove any restart for current job.", error.getActionMessage());
    }

    @Test
    void generateAlreadyCompletedJobErrorTest()
    {
        NovaError error = StatisticsError.generateAlreadyCompletedJobError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.ALREADY_COMPLETED_JOB_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("This job has been completed in a previous execution, and no more executions are allowed for same parameters.", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(" Please, modify job execution parameters, so a new job instance can be executed.", error.getActionMessage());
    }

    @Test
    void generateInternalJobExecutionErrorTest()
    {
        NovaError error = StatisticsError.generateInternalJobExecutionError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.INTERNAL_JOB_EXECUTION_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("The job suffered an internal execution error.", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(" Please, check job logs to find out the root cause.", error.getActionMessage());
    }

    @Test
    void generateInvalidJobParametersErrorTest()
    {
        NovaError error = StatisticsError.generateInvalidJobParametersError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.INVALID_JOB_PARAMETERS_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Parameters for running the job are not valid.", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(" Please, check that parameters are not null.", error.getActionMessage());
    }

    @Test
    void getSonarVersionErrorTest()
    {
        NovaError error = StatisticsError.getSonarVersionError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.SONAR_VERSION_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Error getting sonar version", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getBadFormatExportErrorTest()
    {
        final String badFormat = "badFormatTest";
        NovaError error = StatisticsError.getBadFormatExportError(badFormat);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(NovaError.class, error.getClass());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.EXPORT_FORMAT_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Impossible to export the data to [" + badFormat + "] file. Bad Format", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getCreatingXLSXErrorTest()
    {
        NovaError error = StatisticsError.getCreatingXLSXError();

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.CREATING_XLSX_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals("Error creating the XLSX file", error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.ERROR, error.getErrorMessageType());
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.MSG_CONTACT_NOVA, error.getActionMessage());
    }

    @Test
    void getBrokerStatusNotValidErrorTest()
    {
        final String brokerStatus = "brokerStatusTest";
        NovaError error = StatisticsError.getBrokerStatusNotValidError(brokerStatus);

        Assertions.assertNotNull(error);
        Assertions.assertEquals(StatisticsConstants.StatisticsErrors.BROKER_STATUS_NOT_VALID_ERROR_CODE, error.getErrorCode());
        Assertions.assertEquals(String.format("Broker Status [%s] is not valid.", brokerStatus), error.getErrorMessage());
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, error.getHttpStatus());
        Assertions.assertEquals(ErrorMessageType.WARNING, error.getErrorMessageType());
        Assertions.assertEquals(String.format("Please use one of the following valid values: %s, or %s (to not filter by Broker Status)", Arrays.toString(BrokerStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                error.getActionMessage());
    }

}