package com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions;

import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants.StatisticsErrors;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Error code with all the internal errors for the service.
 * <p>
 *
 * @author BBVA
 */
@ExposeErrorCodes
public class StatisticsError
{
    /**
     * @return an error
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.UNEXPECTED_ERROR_CODE,
                "Unexpected internal error",
                "Please, contact the NOVA Admin team",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Returns a NovaError indicating that the Release Version status used to filter is not a valid one.
     *
     * @param status The non-valid status.
     * @return The NovaError.
     */
    public static NovaError getReleaseVersionStatusNotValidError(final String status)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.RELEASE_VERSION_STATUS_NOT_VALID_ERROR_CODE,
                String.format("Release Versions status [%s] is not valid.", status),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by Release Version Status)", Arrays.toString(ReleaseVersionStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    /**
     * Returns a NovaError indicating that there is more than one product with the same UUAA.
     *
     * @param uuaa     The duplicated UUAA.
     * @param products The List of Product that have the same UUAA.
     * @return The NovaError.
     */
    public static NovaError getUaaaNotUniqueError(final String uuaa, final List<Product> products)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.UUAA_NOT_UNIQUE_ERROR_CODE,
                String.format("There is more than one product with UUAA [%s]: %s", uuaa, products),
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Returns a NovaError indicating that an error occurred when calling User Service.
     *
     * @return The NovaError.
     */
    public static NovaError getUserServiceError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.USER_SERVICE_ERROR_CODE,
                "Error calling User Service",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL
        );
    }

    /**
     * Returns a NovaError indicating that an error occurred when calling Task Service.
     *
     * @return The NovaError.
     */
    public static NovaError getTodoTaskServiceError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.TODO_TASK_SERVICE_ERROR_CODE,
                "Error calling TODO Task Service",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL
        );
    }

    /**
     * Returns a NovaError indicating that an error occurred when calling Continuous Integration Service.
     *
     * @return The NovaError.
     */
    public static NovaError getContinuousIntegrationError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.CONTINUOUS_INTEGRATION_SERVICE_ERROR_CODE,
                "Error calling Continuous Integration Service",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL
        );
    }

    /**
     * Returns a NovaError indicating that an error occurred when calling File Transfer Statistics Service.
     *
     * @return The NovaError.
     */
    public static NovaError getFileTransferStatisticsError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.FILE_TRANSFER_STATISTICS_ERROR_CODE,
                "Error calling File Transfer Statistics Service",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL
        );
    }

    /**
     * Returns a NovaError indicating that the requested logic is not implemented yet.
     *
     * @return The NovaError.
     */
    public static NovaError getNotImplementedYetError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.UNEXPECTED_ERROR_CODE,
                "Not implemented yet",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Returns a NovaError indicating that the Environment used to filter is not a valid one.
     *
     * @param environment The non-valid environment.
     * @return The NovaError.
     */
    public static NovaError getEnvironmentNotValidError(final String environment)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.ENVIRONMENT_NOT_VALID_ERROR_CODE,
                String.format("Environment [%s] is not valid.", environment),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by environment)", Arrays.toString(Environment.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    /**
     * Returns a NovaError indicating that the LogicalConnectorStatus used to filter is not a valid one.
     *
     * @param logicalConnectorStatus The non-valid Logical Connector Status.
     * @return The NovaError.
     */
    public static NovaError getLogicalConnectorStatusNotValidError(final String logicalConnectorStatus)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.LOGICAL_CONNECTOR_STATUS_NOT_VALID_ERROR_CODE,
                String.format("Logical Connector Status [%s] is not valid.", logicalConnectorStatus),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by Logical Connector Status)", Arrays.toString(LogicalConnectorStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    /**
     * Returns a NovaError indicating that the Platform used to filter is not a valid one.
     *
     * @param platform The non-valid Platform.
     * @return The NovaError.
     */
    public static NovaError getPlatformNotValidError(final String platform)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.PLATFORM_NOT_VALID_ERROR_CODE,
                String.format("Platform [%s] is not valid.", platform),
                String.format("Please use one of the following valid values: %s", Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList())),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    /**
     * Returns a NovaError indicating that an error occurred when calling Alert Service.
     *
     * @return The NovaError.
     */
    public static NovaError getAlertServiceError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.ALERT_SERVICE_ERROR_CODE,
                "Error calling Alert Service",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL
        );
    }

    /**
     * Returns a NovaError indicating that the Filesystem Status used to filter is not a valid one.
     *
     * @param status The non-valid Filesystem Status.
     * @return The NovaError.
     */
    public static NovaError getFilesystemStatusNotValidError(final String status)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.FILESYSTEM_STATUS_NOT_VALID_ERROR_CODE,
                String.format("Status [%s] is not valid.", status),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by Filesystem Status)", Arrays.toString(FilesystemStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    /**
     * Generate date time parse nova error.
     *
     * @param dateTimeFormat date format
     * @return new exception
     */
    public static NovaError generateDateTimeParseError(String dateTimeFormat)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.DATE_TIME_PARSE_ERROR_CODE,
                "Date format not expected. The given date does not support the expected format." + dateTimeFormat,
                "Review if date is complete.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    public static NovaError generateAlreadyRunningJobError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.ALREADY_RUNNING_JOB_ERROR_CODE,
                "The current job already has another execution in progress.",
                "Please, wait until execution ends, or kill execution.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING
        );
    }

    public static NovaError generateNotRestartableJobError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.NOT_RESTARTABLE_JOB_ERROR_CODE,
                "This job is not restartable, but a restart has been tried.",
                " Please, remove any restart for current job.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING
        );
    }

    public static NovaError generateAlreadyCompletedJobError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.ALREADY_COMPLETED_JOB_ERROR_CODE,
                "This job has been completed in a previous execution, and no more executions are allowed for same parameters.",
                " Please, modify job execution parameters, so a new job instance can be executed.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING
        );
    }

    public static NovaError generateInternalJobExecutionError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.INTERNAL_JOB_EXECUTION_ERROR_CODE,
                "The job suffered an internal execution error.",
                " Please, check job logs to find out the root cause.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING
        );
    }

    public static NovaError generateInvalidJobParametersError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.INVALID_JOB_PARAMETERS_ERROR_CODE,
                "Parameters for running the job are not valid.",
                " Please, check that parameters are not null.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING
        );
    }

    public static NovaError getSonarVersionError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.SONAR_VERSION_ERROR_CODE,
                "Error getting sonar version",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR
        );
    }

    public static NovaError getBadFormatExportError(final String badFormat)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.EXPORT_FORMAT_ERROR_CODE,
                "Impossible to export the data to [" + badFormat + "] file. Bad Format",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR
        );
    }

    public static NovaError getCreatingXLSXError()
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.CREATING_XLSX_ERROR_CODE,
                "Error creating the XLSX file",
                StatisticsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Returns a NovaError indicating that the BrokerStatus used to filter is not a valid one.
     *
     * @param brokerStatus The non-valid Broker Status.
     * @return The NovaError.
     */
    public static NovaError getBrokerStatusNotValidError(final String brokerStatus)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.BROKER_STATUS_NOT_VALID_ERROR_CODE,
                String.format("Broker Status [%s] is not valid.", brokerStatus),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by Broker Status)", Arrays.toString(BrokerStatus.values()),
                        StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    /**
     * Returns a NovaError indicating that the Platform used to filter is not a valid one.
     *
     * @param platform The non-valid Platform.
     * @return The NovaError.
     */
    public static NovaError getPlatformEnumNotValidError(final String platform)
    {
        return new NovaError(
                StatisticsErrors.CLASS_NAME,
                StatisticsErrors.PLATFORM_NOT_VALID_ERROR_CODE,
                String.format("Platform [%s] is not valid.", platform),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by Platform)", Arrays.toString(Platform.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }
}
