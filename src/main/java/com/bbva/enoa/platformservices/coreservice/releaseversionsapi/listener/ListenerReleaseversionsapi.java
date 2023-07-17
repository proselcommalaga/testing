package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.listener;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.apirestgen.releaseversionsapi.server.spring.nova.rest.IRestListenerReleaseversionsapi;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener to Release Versions API.
 */
@Service
@Slf4j
public class ListenerReleaseversionsapi implements IRestListenerReleaseversionsapi
{

    private final IReleaseVersionService iReleaseVersionService;

    /**
     * Dependency injection constructor
     *
     * @param iReleaseVersionService release version Service
     */
    @Autowired
    public ListenerReleaseversionsapi(final IReleaseVersionService iReleaseVersionService)
    {
        this.iReleaseVersionService = iReleaseVersionService;
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public void addReleaseVersion(final NovaMetadata novaMetadata, final RVReleaseVersionDTO versionToAdd, final Integer releaseId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iReleaseVersionService.addReleaseVersion(versionToAdd, ivUser, releaseId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public void createReleaseVersion(NovaMetadata novaMetadata, RVVersionDTO releaseVersion, Integer releaseId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iReleaseVersionService.createReleaseVersion(releaseVersion, ivUser, releaseId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public void subsystemBuildStatus(final NovaMetadata novaMetadata, final Integer subsystemId, final String jobName, final String jenkinsJobGroupMessageInfo, final String status) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iReleaseVersionService.subsystemBuildStatus(ivUser, subsystemId, jobName, jenkinsJobGroupMessageInfo, status);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public void archiveReleaseVersion(final NovaMetadata novaMetadata, Integer versionId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iReleaseVersionService.archiveReleaseVersion(ivUser, versionId);
    }

    @Override
    // TODO@async ¿Meter el tipo en el dto de served y consumed? necesario para pintar el detalle de las apis por releaseversion
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public RVReleaseVersionDTO getReleaseVersion(final NovaMetadata novaMetadata, final Integer versionId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iReleaseVersionService.getReleaseVersion(ivUser, versionId);
    }

    @Override
    // TODO@async ¿Meter el tipo en el dto de served y consumed? necesario para pintar el detalle de las apis por releaseversion
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public RVReleaseVersionDTO updateReleaseVersion(final NovaMetadata novaMetadata, Integer versionId, String description) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iReleaseVersionService.updateReleaseVersion(ivUser, versionId, description);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public RVReleaseVersionSubsystemDTO[] getReleaseVersionSubsystem(NovaMetadata novaMetadata, Integer subsystemId) throws Errors
    {
        return this.iReleaseVersionService.getReleaseVersionSubsystems(subsystemId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public void deleteReleaseVersion(NovaMetadata novaMetadata, Integer versionId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iReleaseVersionService.deleteReleaseVersion(ivUser, versionId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public void updateReleaseVersionIssue(NovaMetadata novaMetadata, Integer versionId, String issueId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iReleaseVersionService.updateReleaseVersionIssue(ivUser, versionId, issueId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public NewReleaseVersionDto newReleaseVersion(final NovaMetadata novaMetadata, final Integer releaseId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iReleaseVersionService.newReleaseVersion(ivUser, releaseId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public RVRequestDTO releaseVersionRequest(NovaMetadata novaMetadata, Integer releaseId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iReleaseVersionService.releaseVersionRequest(ivUser, releaseId);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE)
    public RVValidationResponseDTO validateTags(NovaMetadata novaMetadata, RVValidationDTO rvValidationDTO, Integer releaseId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iReleaseVersionService.validateAllTags(ivUser, releaseId, rvValidationDTO);
    }

    @Override
    @LogAndTrace(apiName = Constants.RELEASE_VERSIONS_API_NAME, runtimeExceptionErrorCode = ReleaseVersionError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getReleaseVersionsStatuses(NovaMetadata novaMetadata) throws Errors
    {
        return this.iReleaseVersionService.getStatuses();
    }

}
