package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.listener;

import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysisStatus;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PerformanceReport;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PlanPerformanceReport;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.ReleaseVersionCodeAnalyses;
import com.bbva.enoa.apirestgen.qualitymanagerapi.server.spring.nova.rest.IRestListenerQualitymanagerapi;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants.QualityManagerErrors;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Quality Manager API Listener
 */
@Service
public class ListenerQualitymanagerapi implements IRestListenerQualitymanagerapi
{

    private final IQualityManagerService qualityManagerService;

    /**
     *
     * @param qualityManagerService quality manager service
     */
    @Autowired
    public ListenerQualitymanagerapi(final IQualityManagerService qualityManagerService)
    {
        this.qualityManagerService = qualityManagerService;
    }

    @Override
    @LogAndTrace(apiName = QualityConstants.QUALITY_MANAGER_API_NAME, runtimeExceptionErrorCode = QualityManagerErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ReleaseVersionCodeAnalyses getReleaseVersionCodeAnalyses(final NovaMetadata novaMetadata, final Integer releaseVersionId) throws Errors
    {
        return this.qualityManagerService.getReleaseVersionCodeAnalyses(releaseVersionId);
    }

    @Override
    @LogAndTrace(apiName = QualityConstants.QUALITY_MANAGER_API_NAME, runtimeExceptionErrorCode = QualityManagerErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public CodeAnalysisStatus[] getCodeAnalysisStatuses(NovaMetadata novaMetadata, int[] releaseVersionIdList) throws Errors
    {
        return this.qualityManagerService.getCodeAnalysisStatuses(releaseVersionIdList);
    }

    @Override
    @LogAndTrace(apiName = QualityConstants.QUALITY_MANAGER_API_NAME, runtimeExceptionErrorCode = QualityManagerErrors.UNEXPECTED_ERROR_CODE)
    public void requestSubsystemCodeAnalysis(final NovaMetadata novaMetadata, Integer subsystemId, String branchName) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.qualityManagerService.requestSubsystemCodeAnalysis(ivUser, subsystemId, branchName);
    }

    @Override
    @LogAndTrace(apiName = QualityConstants.QUALITY_MANAGER_API_NAME, runtimeExceptionErrorCode = QualityManagerErrors.UNEXPECTED_ERROR_CODE)
    public void setPerformanceReport(final NovaMetadata novaMetadata, final PerformanceReport performanceReport, final Integer planId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.qualityManagerService.setPerformanceReport(ivUser, planId, performanceReport);
    }

    @Override
    @LogAndTrace(apiName = QualityConstants.QUALITY_MANAGER_API_NAME, runtimeExceptionErrorCode = QualityManagerErrors.UNEXPECTED_ERROR_CODE)
    public void deletePerformanceReport(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.qualityManagerService.deletePerformanceReport(ivUser, planId);
    }

    @Override
    @LogAndTrace(apiName = QualityConstants.QUALITY_MANAGER_API_NAME, runtimeExceptionErrorCode = QualityManagerErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public PlanPerformanceReport[] getPerformanceReports(final NovaMetadata novaMetadata, final Integer releaseVersionId) throws Errors
    {
        return this.qualityManagerService.getPerformanceReports(releaseVersionId);
    }

}
