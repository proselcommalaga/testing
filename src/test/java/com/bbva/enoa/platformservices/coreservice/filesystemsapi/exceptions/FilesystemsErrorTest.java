package com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions;

import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

class FilesystemsErrorTest
{
    @ParameterizedTest
    @ArgumentsSource(FilesystemsErrorsAndErrorCodesArgumentsProvider.class)
    void givenNovaError_whenGetErrorDetails_thenErrorIsCorrect(final NovaError novaError, final String expectedStatusCode)
    {
        // When
        final HttpStatus response = novaError.getHttpStatus();

        // Then
        assertNotNull(response);
        assertEquals("Error code of NovaError is not the expected one", expectedStatusCode, novaError.getErrorCode());
    }

    @Test
    void givenAllNovaError_whenGetErrorCodes_thenNoDuplicatesArePresent()
    {
        // Given
        final List<NovaError> allFilesystemErrors = getAllFilesystemErrors();

        // When
        final Map<String, Long> errorCodesCount = allFilesystemErrors.stream()
                .collect(Collectors.groupingBy(NovaError::getErrorCode, Collectors.counting()));

        final List<String> duplicatedErrorCodes = errorCodesCount.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Then
        if (!duplicatedErrorCodes.isEmpty())
        {
            Assert.fail(String.format("Duplicated error codes: %s", duplicatedErrorCodes));
        }
    }

    static class FilesystemsErrorsAndErrorCodesArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            String errorMessage = "ErrorMessage";

            return Stream.of(
                    Arguments.of(FilesystemsError.getUnexpectedError(), Constants.FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE),
                    Arguments.of(FilesystemsError.getFilesystemNotAvailableDeleteError(), Constants.FilesystemsErrorConstants.FYLESYSTEM_NOT_AVAILABLE_DELETE_ERROR),
                    Arguments.of(FilesystemsError.getNoSuchProductError(0), Constants.FilesystemsErrorConstants.NO_SUCH_PRODUCT_ERROR),
                    Arguments.of(FilesystemsError.getNoSuchFilesystemError(0), Constants.FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ERROR),
                    Arguments.of(FilesystemsError.getNotEnvironmentToDownload(0, Environment.INT.name()), Constants.FilesystemsErrorConstants.ENVIRONMENT_SELECTED_ERROR),
                    Arguments.of(FilesystemsError.getNoEnoughFilesystemBudgetError(0, 0, Environment.INT.name()), Constants.FilesystemsErrorConstants.NO_ENOUGH_FILESYSTEM_BUDGET_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemCreationError(), Constants.FilesystemsErrorConstants.FILESYSTEM_CREATION_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemDeletionError(), Constants.FilesystemsErrorConstants.FILESYSTEM_DELETION_ERROR),
                    Arguments.of(FilesystemsError.getDeletedUsedFilesystemError(), Constants.FilesystemsErrorConstants.DELETED_USED_FILESYSTEM_ERROR),
                    Arguments.of(FilesystemsError.getDuplicatedFilesystemError(0, Environment.INT.name(), "fs"), Constants.FilesystemsErrorConstants.DUPLICATED_FILESYSTEM_ERROR),
                    Arguments.of(FilesystemsError.getDuplicatedFilesystemNotFoundError(0, Environment.INT.name(), "fs"), Constants.FilesystemsErrorConstants.DUPLICATED_FILESYSTEM_NOT_FOUND_ERROR),
                    Arguments.of(FilesystemsError.getTooManyFilesystemsError(0, Environment.INT.name(), FilesystemType.FILESYSTEM.name()), Constants.FilesystemsErrorConstants.TOO_MANY_FILESYSTEMS_ERROR),
                    Arguments.of(FilesystemsError.getDuplicatedLandingZonePathError(0, Environment.INT.name(), "fs"), Constants.FilesystemsErrorConstants.DUPLICATED_LANDING_ZONE_PATH_ERROR),
                    Arguments.of(FilesystemsError.getTriedToArchiveUsedFilesystemError(), Constants.FilesystemsErrorConstants.TRIED_TO_ARCHIVE_USED_FILESYSTEM_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemNotAvailableViewError(0), Constants.FilesystemsErrorConstants.FILESYSTEM_NOT_AVAILABLE_VIEW_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemNotAvailableCreateError(0), Constants.FilesystemsErrorConstants.FILESYSTEM_NOT_AVAILABLE_CREATE_ERROR),
                    Arguments.of(FilesystemsError.getGetFilesError(errorMessage), Constants.FilesystemsErrorConstants.GET_FILES_ERROR),
                    Arguments.of(FilesystemsError.getDeleteFileError(errorMessage), Constants.FilesystemsErrorConstants.DELETE_FILE_ERROR),
                    Arguments.of(FilesystemsError.getDownloadToPreviousEnvironmentFileError(errorMessage), Constants.FilesystemsErrorConstants.DOWNLOAD_FILE_ERROR),
                    Arguments.of(FilesystemsError.getUploadFileError(errorMessage), Constants.FilesystemsErrorConstants.UPLOAD_FILE_ERROR),
                    Arguments.of(FilesystemsError.getGetFileUseError(errorMessage), Constants.FilesystemsErrorConstants.GET_FILE_USE_ERROR),
                    Arguments.of(FilesystemsError.getActionPermissionError(errorMessage), Constants.FilesystemsErrorConstants.ACTION_PERMISSION_ERROR),
                    Arguments.of(FilesystemsError.getFrozenValidationError(), Constants.FilesystemsErrorConstants.FROZEN_VALIDATION_ERROR),
                    Arguments.of(FilesystemsError.getCreateDirectoryError(errorMessage), Constants.FilesystemsErrorConstants.CREATE_DIRECTORY_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemBusyError(), Constants.FilesystemsErrorConstants.FILESYSTEM_BUSY_ERROR),
                    Arguments.of(FilesystemsError.getNotAllowedFileNamePatternError(), Constants.FilesystemsErrorConstants.NOT_ALLOWED_FILE_NAME_PATTERN_ERROR),
                    Arguments.of(FilesystemsError.getNotAllowedModifyDirectoriesError(0, "dir"), Constants.FilesystemsErrorConstants.NOT_ALLOWED_MODIFY_DIRECTORIES_ERROR),
                    Arguments.of(FilesystemsError.getNotFSAlertError(0), Constants.FilesystemsErrorConstants.NO_FS_ALERT_ERROR),
                    Arguments.of(FilesystemsError.getNotSuchFSAlertInfoDtoError(), Constants.FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ALERT_INFO_DTO),
                    Arguments.of(FilesystemsError.getNotSuchFSAlertIdError(), Constants.FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ALERT_INFO_ID),
                    Arguments.of(FilesystemsError.getDatabaseFSAlertError(0), Constants.FilesystemsErrorConstants.DATABASE_FILESYSTEM_ALERT_ERROR),
                    Arguments.of(FilesystemsError.getDatabaseFSError(), Constants.FilesystemsErrorConstants.DATABASE_FILESYSTEM_ERROR),
                    Arguments.of(FilesystemsError.getForbiddenError(), Constants.FilesystemsErrorConstants.USER_PERMISSIONS_ERROR_CODE),
                    Arguments.of(FilesystemsError.getFilesystemPackCodeError("CODE"), Constants.FilesystemsErrorConstants.FILESYSTEM_PACK_CODE_ERROR),
                    Arguments.of(FilesystemsError.getBadArgumentsError(), Constants.FilesystemsErrorConstants.BAD_ARGUMENTS_EVENTS_CONFIGURATION),
                    Arguments.of(FilesystemsError.getCreatingOrDeletingFileSystemOnNonConfiguredPlatformError(), Constants.FilesystemsErrorConstants.DESTINATION_PLATFORM_KO),
                    Arguments.of(FilesystemsError.getActionFrozenError("fs", 0, Environment.INT.name(), "IMM0589"), Constants.FilesystemsErrorConstants.FROZEN_ACTION_ERROR),
                    Arguments.of(FilesystemsError.getCreationFilesystemTaskError(0, "/path", "fs", Environment.INT.name()), Constants.FilesystemsErrorConstants.CREATE_FILESYSTEM_TASK_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemInUseByServicesError("fs"), Constants.FilesystemsErrorConstants.FILESYSTEM_IN_USE_BY_SERVICES_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemInUseByBrokersError("fs"), Constants.FilesystemsErrorConstants.FILESYSTEM_IN_USE_BY_BROKERS_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemPackCodeEnvironmentError("CODE", Environment.INT.name()), Constants.FilesystemsErrorConstants.FILESYSTEM_PACK_CODE_ENV_ERROR),
                    Arguments.of(FilesystemsError.getUpdateQuotaInvalidFilesystemType(FilesystemType.FILESYSTEM.name()), Constants.FilesystemsErrorConstants.FILESYSTEM_UPDATE_QUOTA_INVALID_FILESYSTEM_TYPE_ERROR),
                    Arguments.of(FilesystemsError.getUnrecognizableSizeUnitError("UNIT"), Constants.FilesystemsErrorConstants.FILESYSTEM_SIZE_UNIT_ERROR),
                    Arguments.of(FilesystemsError.getNotEnoughFreeSpaceToUpdateQuotaError(0, "PACK"), Constants.FilesystemsErrorConstants.FILESYSTEM_NOT_ENOUGH_FREE_STORAGE_ERROR),
                    Arguments.of(FilesystemsError.getFilesystemIdError("/path", Environment.INT.name()), Constants.FilesystemsErrorConstants.FILESYSTEM_ID_NOT_FOUND),
                    Arguments.of(FilesystemsError.getAlertServiceCallError(), Constants.FilesystemsErrorConstants.FILESYSTEM_ALERT_SERVICE_ERROR),
                    Arguments.of(FilesystemsError.getUserServiceNotFoundError("IMM0589"), Constants.FilesystemsErrorConstants.USER_NOT_FOUND),
                    Arguments.of(FilesystemsError.getInvalidSizeError("asdf"), Constants.FilesystemsErrorConstants.FILESYSTEM_INVALID_SIZE_ERROR)
            );
        }
    }

    static List<NovaError> getAllFilesystemErrors()
    {
        Method[] errorMethods = FilesystemsError.class.getDeclaredMethods();

        final List<NovaError> novaErrors = new ArrayList<>();

        for (Method errorMethod : errorMethods)
        {
            boolean errorMethodIsPublic = (errorMethod.getModifiers() & Modifier.PUBLIC) != 0;

            if (errorMethodIsPublic)
            {
                Class[] parameters = errorMethod.getParameterTypes();

                if (parameters.length > 0)
                {
                    Object[] parametersValues = Stream.of(parameters).map(parameter -> {
                        if (parameter == int.class || parameter == Integer.class)
                        {
                            return RandomUtils.nextInt(0, 10);
                        }
                        else if (parameter == long.class || parameter == Long.class)
                        {
                            return RandomUtils.nextLong(0, 10);
                        }
                        else if (parameter == String.class)
                        {
                            return RandomStringUtils.randomAlphanumeric(8);
                        }
                        else
                        {
                            throw new RuntimeException(String.format("Error preparing test data: class '%s' has not defined a random value generator", parameter.toString()));
                        }
                    })
                            .toArray(Object[]::new);

                    try
                    {
                        novaErrors.add((NovaError) errorMethod.invoke(null, parametersValues));
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new RuntimeException("Error preparing test data", e);
                    }
                }
                else
                {
                    try
                    {
                        novaErrors.add((NovaError) errorMethod.invoke(null));
                    }
                    catch (IllegalAccessException | InvocationTargetException e)
                    {
                        throw new RuntimeException("Error preparing test data", e);
                    }
                }
            }
        }

        return novaErrors;
    }
}