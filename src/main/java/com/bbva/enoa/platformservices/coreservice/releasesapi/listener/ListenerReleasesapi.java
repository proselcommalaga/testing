package com.bbva.enoa.platformservices.coreservice.releasesapi.listener;

import com.bbva.enoa.apirestgen.releasesapi.model.NewReleaseRequest;
import com.bbva.enoa.apirestgen.releasesapi.model.RELReleaseInfo;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseConfigDto;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseDto;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseDtoByEnvironment;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseVersionInListDto;
import com.bbva.enoa.apirestgen.releasesapi.server.spring.nova.rest.IRestListenerReleasesapi;
import com.bbva.enoa.platformservices.coreservice.releasesapi.services.interfaces.IReleasesService;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Angel Pinazo
 */
@Slf4j
@Service
public class ListenerReleasesapi implements IRestListenerReleasesapi
{
    /**
     * ReleasesService bussiness
     */
    private final IReleasesService releasesService;

    /**
     * Constructor
     *
     * @param pReleasesService service
     */
    @Autowired
    public ListenerReleasesapi(final IReleasesService pReleasesService)
    {
        this.releasesService = pReleasesService;
    }

    ////////////////////////// IMPLEMENTATIONS ////////////////////////////////////////

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
    public void updateReleaseConfig(NovaMetadata novaMetadata, ReleaseConfigDto releaseConfig, Integer releaseId, String environment) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        releasesService.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.valueOf(environment));
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public String[] getCpdsHistorical(NovaMetadata novaMetadata) throws Errors
    {
        return releasesService.getCpdsHistorical();
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public String[] getCpds(final NovaMetadata novaMetadata, final String environment) throws Errors
    {
        return releasesService.getCpds(environment);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
    public void createRelease(final NovaMetadata novaMetadata, final NewReleaseRequest releaseToAdd) throws Errors
    {
        releasesService.createRelease(MetadataUtils.getIvUser(novaMetadata), releaseToAdd);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public ReleaseDto releaseInfo(final NovaMetadata novaMetadata, Integer releaseId) throws Errors
    {
        return releasesService.releaseInfo(releaseId);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
    public void deleteRelease(final NovaMetadata novaMetadata, final Integer releaseId) throws Errors
    {
        releasesService.deleteRelease(MetadataUtils.getIvUser(novaMetadata), releaseId);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public ReleaseVersionInListDto[] getReleaseVersions(final NovaMetadata novaMetadata, final Integer releaseId, final String status) throws Errors
    {
        return releasesService.getReleaseVersions(releaseId, status);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR)
    public Integer getReleasesMaxVersions(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        return releasesService.getReleasesMaxVersions(productId);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public RELReleaseInfo[] getAllReleasesAndServices(final NovaMetadata novaMetadata, Integer productId) throws Errors
    {
        return releasesService.getAllReleasesAndServices(productId);
    }

    @Override
    @LogAndTrace(apiName = ReleaseConstants.RELEASE_API, runtimeExceptionErrorCode = ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR, debugLogLevel = true)
    public ReleaseDto[] getProductReleases(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        return releasesService.getProductReleases(MetadataUtils.getIvUser(novaMetadata), productId);
    }

    @Override
    public ReleaseDtoByEnvironment[] getProductConfiguration(NovaMetadata novaMetadata, Integer productId, String environment) throws Errors
    {
        return this.releasesService.getReleasesProductConfiguration(MetadataUtils.getIvUser(novaMetadata), productId, environment);
    }
}
