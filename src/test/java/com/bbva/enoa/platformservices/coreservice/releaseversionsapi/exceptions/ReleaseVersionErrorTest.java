package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;


public class ReleaseVersionErrorTest
{
    @Test
    public void getHttpStatus()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();
        HttpStatus response = error.getHttpStatus();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response);
    }

    @Test
    public void getErrorCode()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();

        String response = error.getErrorCode();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("RELEASEVERSIONS-000", response);
    }

    @Test
    public void getErrorMessage()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();

        String response = error.getErrorMessage();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Unexpected internal error", response);
    }

    @Test
    public void getActionMessage()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();

        String response = error.getActionMessage();

        Assertions.assertNotNull(response);
        Assertions.assertEquals("Please, contact the NOVA Admin team", response);
    }

    @Test
    public void getErrorMessageTypeTest()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();

        ErrorMessageType response = error.getErrorMessageType();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(ErrorMessageType.FATAL, response);
    }

    @Test
    public void toStringTest()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();

        Assertions.assertNotNull(error);
        Assertions. assertEquals("ReleaseVersionError{errorCode=RELEASEVERSIONS-000, errorMessage='Unexpected internal error', actionMessage='Please, contact the NOVA Admin team', "
                + "httpStatus='500', errorMessageType='FATAL'}", error.toString());
    }

    @Test
    void getUnexpectedError()
    {
        NovaError error = ReleaseVersionError.getUnexpectedError();
        assertNovaError(error, Constants.RVErrorsCodes.UNEXPECTED_ERROR_CODE,  ErrorMessageType.FATAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getReleaseVersionNameDuplicatedError()
    {
        NovaError error = ReleaseVersionError.getReleaseVersionNameDuplicatedError();
        assertNovaError(error, Constants.RVErrorsCodes.RELEASE_VERSION_NAME_DUPLICATED_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getNullProductSubsystemIdError()
    {
        NovaError error = ReleaseVersionError.getNullProductSubsystemIdError();
        assertNovaError(error, Constants.RVErrorsCodes.NULL_PRODUCT_SUBSYSTEM_ID_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getProductSubsystemNotFoundError()
    {
        NovaError error = ReleaseVersionError.getProductSubsystemNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.PRODUCT_SUBSYSTEM_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getVCSApiFailedError()
    {
        NovaError error = ReleaseVersionError.getVCSApiFailedError();
        assertNovaError(error, Constants.RVErrorsCodes.VCS_API_FAILED_ERROR_CODE,  ErrorMessageType.CRITICAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getNullSubsystemStatusError()
    {
        NovaError error = ReleaseVersionError.getNullSubsystemStatusError();
        assertNovaError(error, Constants.RVErrorsCodes.NULL_SUBSYSTEM_STATUS_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getNoSuchReleaseVersionError()
    {
        NovaError error = ReleaseVersionError.getNoSuchReleaseVersionError(0);
        assertNovaError(error, Constants.RVErrorsCodes.NO_SUCH_RELEASE_VERSION_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.NOT_FOUND);
    }

    @Test
    void getNoSuchReleaseError()
    {
        NovaError error = ReleaseVersionError.getNoSuchReleaseError();
        assertNovaError(error, Constants.RVErrorsCodes.NO_SUCH_RELEASE_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDeleteDeployedReleaseError()
    {
        NovaError error = ReleaseVersionError.getDeleteDeployedReleaseError();
        assertNovaError(error, Constants.RVErrorsCodes.DELETE_DEPLOYED_RELEASE_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDuplicatdApiError()
    {
        NovaError error = ReleaseVersionError.getDuplicatdApiError();
        assertNovaError(error, Constants.RVErrorsCodes.DUPLICATED_API_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUnexpectedErrorInVCSRequestError()
    {
        NovaError error = ReleaseVersionError.getUnexpectedErrorInVCSRequestError();
        assertNovaError(error, Constants.RVErrorsCodes.UNEXPECTED_ERROR_IN_VCS_REQUEST_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getReleaseWithNoSubsystemsError()
    {
        NovaError error = ReleaseVersionError.getReleaseWithNoSubsystemsError();
        assertNovaError(error, Constants.RVErrorsCodes.RELEASE_WITH_NO_SUBSYSTEMS_ERROR,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUTF8NotSupportedError()
    {
        NovaError error = ReleaseVersionError.getUTF8NotSupportedError();
        assertNovaError(error, Constants.RVErrorsCodes.UTF8_NOT_SUPPORTED_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getFileError()
    {
        NovaError error = ReleaseVersionError.getFileError();
        assertNovaError(error, Constants.RVErrorsCodes.FILE_READING_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getFolderNotFoundError()
    {
        NovaError error = ReleaseVersionError.getFolderNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.FOLDER_NOT_FOUND_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getEphoenixServiceWithNoMetadataError()
    {
        NovaError error = ReleaseVersionError.getEphoenixServiceWithNoMetadataError();
        assertNovaError(error, Constants.RVErrorsCodes.EPHOENIX_SERVICE_WITH_NO_METADATA_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getNoSuchReleaseVersionSubsystemError()
    {
        NovaError error = ReleaseVersionError.getNoSuchReleaseVersionSubsystemError(0);
        assertNovaError(error, Constants.RVErrorsCodes.NO_SUCH_RELEASE_VERSION_SUBSYSTEM_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.NOT_FOUND);
    }

    @Test
    void getStorageDeployedReleaseError()
    {
        NovaError error = ReleaseVersionError.getStorageDeployedReleaseError();
        assertNovaError(error, Constants.RVErrorsCodes.STORAGE_DEPLOYED_RELEASE_ERROR,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getReleaseStatusBuildingError()
    {
        NovaError error = ReleaseVersionError.getReleaseStatusBuildingError();
        assertNovaError(error, Constants.RVErrorsCodes.RELEASE_STATUS_BUILDING_ERROR,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getMaxVersionsLimitError()
    {
        NovaError error = ReleaseVersionError.getMaxVersionsLimitError();
        assertNovaError(error, Constants.RVErrorsCodes.MAX_VERSIONS_LIMIT_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getNoSuchJiraProjectKeyError()
    {
        NovaError error = ReleaseVersionError.getNoSuchJiraProjectKeyError();
        assertNovaError(error, Constants.RVErrorsCodes.NO_SUCH_JIRA_PROJECT_KEY_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getMaxVersionCompilingError()
    {
        NovaError error = ReleaseVersionError.getMaxVersionCompilingError();
        assertNovaError(error, Constants.RVErrorsCodes.MAX_VERSION_COMPILING_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDependencyGraphContainsCycleError()
    {
        NovaError error = ReleaseVersionError.getDependencyGraphContainsCycleError();
        assertNovaError(error, Constants.RVErrorsCodes.DEPENDENCY_GRAPH_CONTAINS_CYCLE_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDependencyNotFoundError()
    {
        NovaError error = ReleaseVersionError.getDependencyNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.DEPENDENCY_NOT_FOUND_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getDuplicatedServiceNameError()
    {
        NovaError error = ReleaseVersionError.getDuplicatedServiceNameError();
        assertNovaError(error, Constants.RVErrorsCodes.DUPLICATED_SERVICE_NAME_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getNoSuchSchedulerYmlError()
    {
        NovaError error = ReleaseVersionError.getNoSuchSchedulerYmlError();
        assertNovaError(error, Constants.RVErrorsCodes.NO_SUCH_SCHEDULER_YML_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getBatchServiceNotFoundError()
    {
        NovaError error = ReleaseVersionError.getBatchServiceNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.BATCH_SERVICE_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getSchedulerYmlError()
    {
        NovaError error = ReleaseVersionError.getSchedulerYmlError();
        assertNovaError(error, Constants.RVErrorsCodes.SCHEDULER_YML_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getBatchSchedulerSaveError()
    {
        NovaError error = ReleaseVersionError.getBatchSchedulerSaveError("");
        assertNovaError(error, Constants.RVErrorsCodes.BATCH_SCHEDULER_SAVE_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getBatchScheluderDeleteError()
    {
        NovaError error = ReleaseVersionError.getBatchScheluderDeleteError();
        assertNovaError(error, Constants.RVErrorsCodes.BATCH_SCHELUDER_DELETE_ERROR_CODE,  ErrorMessageType.CRITICAL, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getBatchServiceDuplicatedError()
    {
        NovaError error = ReleaseVersionError.getBatchServiceDuplicatedError();
        assertNovaError(error, Constants.RVErrorsCodes.BATCH_SERVICE_DUPLICATED_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.FORBIDDEN);
    }

    @Test
    void getForbiddenError()
    {
        NovaError error = ReleaseVersionError.getForbiddenError();
        assertNovaError(error, Constants.RVErrorsCodes.FORBIDDEN_ERROR_CODE,  ErrorMessageType.ERROR, HttpStatus.FORBIDDEN);
    }

    @Test
    void getGroupIdNotFoundError()
    {
        NovaError error = ReleaseVersionError.getGroupIdNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.GROUP_ID_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getArtifactIdNotFoundError()
    {
        NovaError error = ReleaseVersionError.getArtifactIdNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.ARTIFACT_ID_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getVersionNotFoundError()
    {
        NovaError error = ReleaseVersionError.getVersionNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.VERSION_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getReleaseNameNotFoundError()
    {
        NovaError error = ReleaseVersionError.getReleaseNameNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.RELEASE_NAME_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getServiceNameNotFoundError()
    {
        NovaError error = ReleaseVersionError.getServiceNameNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.SERVICE_NAME_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getServiceTypeNotFoundError()
    {
        NovaError error = ReleaseVersionError.getServiceTypeNotFoundError();
        assertNovaError(error, Constants.RVErrorsCodes.SERVICE_TYPE_NOT_FOUND_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    @Test
    void getMultitagNotAllowedError()
    {
        NovaError error = ReleaseVersionError.getMultitagNotAllowedError();
        assertNovaError(error, Constants.RVErrorsCodes.MULTI_TAG_NOT_ALLOWED_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getReleaseVersionWithoutSubsystem()
    {
        NovaError error = ReleaseVersionError.getReleaseVersionWithoutSubsystem();
        assertNovaError(error, Constants.RVErrorsCodes.RELEASE_VERSION_WITHOUT_SUBSYSTEM,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getSubsystemWithoutServicesError()
    {
        NovaError error = ReleaseVersionError.getSubsystemWithoutServicesError();
        assertNovaError(error, Constants.RVErrorsCodes.SUBSYSTEM_WITHOUT_SERVICES_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.BAD_REQUEST);
    }

    @Test
    void getJiraOptionsError()
    {
        NovaError error = ReleaseVersionError.getJiraOptionsError();
        assertNovaError(error, Constants.RVErrorsCodes.JIRA_OPTIONS_ERROR_CODE,  ErrorMessageType.WARNING, HttpStatus.FORBIDDEN);
    }

    /**
     * Assertions for NovaError
     * @param error NovaError to assert
     * @param expectedCode Expected RVErrorCode of this NovaError
     * @param expectedType Expected ErrorMessageType of this NovaError
     * @param expectedHttpStatus Expected RVErrorCode of this NovaError
     */
    private void assertNovaError(final NovaError error, final String expectedCode, final ErrorMessageType expectedType, final HttpStatus expectedHttpStatus)
    {

        Assertions.assertNotNull(error);
        //code
        Assertions.assertEquals(expectedCode, error.getErrorCode());
        //HttpStatus
        Assertions.assertEquals(expectedHttpStatus, error.getHttpStatus());
        //Type
        Assertions.assertEquals(expectedType, error.getErrorMessageType());
        //Message never null
        Assertions.assertNotNull(error.getErrorMessage(), "Message of NovaError cannot be null");
        // Action
        Assertions.assertNotNull(error.getActionMessage(), "Action message of NovaError cannot be null");
        //ToString never null
        Assertions.assertNotNull(error.toString(), "toString of NovaError cannot be null");
    }
}