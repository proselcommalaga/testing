package com.bbva.enoa.platformservices.coreservice.statisticsapi.listener;

import com.bbva.enoa.apirestgen.statisticsapi.model.AlertsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ApiSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.AvailabilityNovaCoinsDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BrokersSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BudgetSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.BuildJobSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.CategorySummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ConnectorsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.DeploymentPlansSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.FileTransfersInstancesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.FileTransfersSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.FilesystemsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.InstancesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ProductSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.QualityAnalysesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.ReleaseVersionSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.STHistoricalPoint;
import com.bbva.enoa.apirestgen.statisticsapi.model.STHistoricalSerie;
import com.bbva.enoa.apirestgen.statisticsapi.model.ServicesSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.SubsystemSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.TODOTaskSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.UserSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.server.spring.nova.rest.IRestListenerStatisticsapi;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.services.interfaces.IServiceStatisticsapi;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants.StatisticsErrors;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Listener for Statistics API
 */
@Service
public class ListenerStatisticsapi implements IRestListenerStatisticsapi
{
    /**
     * Service Statistics API
     **/
    private final IServiceStatisticsapi serviceStatisticsapi;

    /**
     * Constructor of listener
     *
     * @param serviceStatisticsapi statistics service
     */
    @Autowired
    public ListenerStatisticsapi(final IServiceStatisticsapi serviceStatisticsapi)
    {
        this.serviceStatisticsapi = serviceStatisticsapi;
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public Long getProductsNumber(final NovaMetadata novaMetadata) throws Errors
    {
        return Optional.ofNullable(this.serviceStatisticsapi.getProductsNumber()).orElse(0L);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public FileTransfersSummaryDTO getFileTransfersSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getFileTransfersSummary(environment, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getCloudProductsSummary(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getCloudProductsSummary(startDate, endDate, environment, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getCloudProductsSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String endDate, final String uuaa, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getCloudProductsSummaryExport(startDate, endDate, environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getFileTransfersSummaryExport(final com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata novaMetadata, final String format, final String environment, final String uuaa) throws com.bbva.kltt.apirest.generator.lib.commons.exception.Errors
    {
        return this.serviceStatisticsapi.getFileTransfersSummaryExport(environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getDeployedServicesHistorical(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String language, final String type, final String category, final String startDate, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getDeployedServicesHistorical(startDate, endDate, environment, platform, language, type, uuaa, category);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getDeployedServicesHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String language, final String type, final String category, final String startDate, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getDeployedServicesHistoricalExport(startDate, endDate, environment, platform, language, type, uuaa, format, category);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public FilesystemsSummaryDTO getFilesystemsSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getFilesystemsSummary(environment, uuaa, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getFilesystemsSummaryExport(final com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String status) throws com.bbva.kltt.apirest.generator.lib.commons.exception.Errors
    {
        return this.serviceStatisticsapi.getFilesystemsSummaryExport(environment, uuaa, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalPoint[] getUsersHistorical(NovaMetadata novaMetadata, String role, String endDate, String uuaa, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getUsersHistorical(startDate, endDate, role, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getUsersHistoricalExport(final NovaMetadata novaMetadata, final String format, final String role, final String endDate, final String uuaa, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getUsersHistoricalExport(startDate, endDate, role, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public AlertsSummaryDTO getAlertsSummary(final NovaMetadata novaMetadata, final String environment, final String type, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getAlertsSummary(environment, type, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getAlertsSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String type, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getAlertsSummaryExport(environment, type, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public InstancesSummaryDTO getInstancesSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getInstancesSummary(environment, uuaa, platform, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getInstancesSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getInstancesSummaryExport(environment, uuaa, platform, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ServicesSummaryDTO getServicesSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getServicesSummary(environment, uuaa, platform);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getServicesSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getServicesSummaryExport(environment, uuaa, platform, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getReleaseVersionsHistorical(NovaMetadata novaMetadata, String endDate, String uuaa, String startDate, String status) throws Errors
    {
        return this.serviceStatisticsapi.getReleaseVersionsHistorical(startDate, endDate, status, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getReleaseVersionsHistoricalExport(final NovaMetadata novaMetadata, final String format, final String endDate, final String uuaa, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getReleaseVersionsHistoricalExport(startDate, endDate, status, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalPoint[] getCategoriesHistorical(NovaMetadata novaMetadata, String type, String endDate, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getCategoriesHistorical(startDate, endDate, type);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getCategoriesHistoricalExport(final NovaMetadata novaMetadata, final String format, final String type, final String endDate, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getCategoriesHistoricalExport(startDate, endDate, type, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalPoint[] getMemoryHistorical(NovaMetadata novaMetadata, String environment, String unit, String endDate, String cpd, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getMemoryHistorical(startDate, endDate, cpd, environment, unit);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getMemoryHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String unit, final String endDate, final String cpd, final String format, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getMemoryHistoricalExport(startDate, endDate, cpd, environment, unit, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public Long getServicesNumber(final NovaMetadata novaMetadata) throws Errors
    {
        return Optional.ofNullable(this.serviceStatisticsapi.getServicesNumber()).orElse(0L);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public Long getApisNumber(final NovaMetadata novaMetadata) throws Errors
    {
        return Optional.ofNullable(this.serviceStatisticsapi.getApisNumberInPro()).orElse(0L);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BatchExecutionsSummaryDTO getBatchExecutionsSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String platform, final String origin) throws Errors
    {
        return this.serviceStatisticsapi.getBatchExecutionsSummary(environment, uuaa, platform, origin);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBatchExecutionsSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String platform, final String origin) throws Errors
    {
        return this.serviceStatisticsapi.getBatchExecutionsSummaryExport(environment, uuaa, platform, origin, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String getErrorCodes(NovaMetadata novaMetadata) throws Errors
    {
        return this.serviceStatisticsapi.getErrorCodes();
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public Long getUsersNumber(final NovaMetadata novaMetadata) throws Errors
    {
        return Optional.ofNullable(this.serviceStatisticsapi.getUsersNumber()).orElse(0L);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public SubsystemSummaryDTO getSubsystemsSummary(final NovaMetadata novaMetadata, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getSubsystemsSummary(uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getSubsystemsSummaryExport(final NovaMetadata novaMetadata, final String format, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getSubsystemsSummaryExport(uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getApisHistorical(NovaMetadata novaMetadata, String environment, String endDate, String uuaa, String apiFunctionality, String type, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getApisHistorical(apiFunctionality, environment, endDate,uuaa, type, startDate);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getApisHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String type, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getApisHistoricalExport(startDate, endDate, type, uuaa, environment, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public QualityAnalysesSummaryDTO getQualityAnalysesSummary(NovaMetadata novaMetadata) throws Errors
    {
        return this.serviceStatisticsapi.getQualityAnalysesSummary();
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getQualityAnalysesSummaryExport(final NovaMetadata novaMetadata, final String format) throws Errors
    {
        return this.serviceStatisticsapi.getQualityAnalysesSummaryExport(format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalPoint[] getProductsHistorical(NovaMetadata novaMetadata, String type, String endDate, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getProductsHistorical(startDate, endDate, type);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getProductsHistoricalExport(final NovaMetadata novaMetadata, final String format, final String type, final String endDate, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getProductsHistoricalExport(startDate, endDate, type, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getSubsystemsHistorical(NovaMetadata novaMetadata, String type, String endDate, String uuaa, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getSubsystemsHistorical(startDate, endDate, type, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getSubsystemsHistoricalExport(final NovaMetadata novaMetadata, final String format, final String type, final String endDate, final String uuaa, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getSubsystemsHistoricalExport(startDate, endDate, type, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getFiletransfersHistorical(NovaMetadata novaMetadata, String environment, String endDate, String uuaa, String startDate, String status) throws Errors
    {
        return this.serviceStatisticsapi.getFiletransfersHistorical(startDate, endDate, environment, status, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getFiletransfersHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getFiletransfersHistoricalExport(startDate, endDate, environment, status, uuaa, format);
    }

    @Override
    public STHistoricalSerie[] getAdoptionLevelHistorical(final NovaMetadata novaMetadata, final String environment, final String endDate, final String startDate, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getAdoptionLevelHistorical(startDate, endDate, environment, platform);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getAdoptionLevelHistoricalExport(final NovaMetadata novaMetadata, final String format, final String environment, final String endDate, final String startDate, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getAdoptionLevelHistoricalExport(startDate, endDate, environment, platform, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getInstancesHistorical(NovaMetadata novaMetadata, String environment, String endDate, String uuaa, String type, String startDate, String platform) throws Errors
    {
        return this.serviceStatisticsapi.getInstancesHistorical(startDate, endDate, environment, platform, type, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getInstancesHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String type, final String startDate, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getInstancesHistoricalExport(startDate, endDate, environment, platform, type, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getBatchInstancesHistorical(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String startDate, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBatchExecutionsHistorical(startDate, endDate, environment, platform, uuaa, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBatchInstancesHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String startDate, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBatchExecutionsHistoricalExport(startDate, endDate, environment, platform, uuaa, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getStorageHistorical(NovaMetadata novaMetadata, String property, String environment, String endDate, String uuaa, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getStorageHistorical(startDate, endDate, environment, property, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getStorageHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String property, final String format, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getStorageHistoricalExport(startDate, endDate, environment, property, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getCompilationsHistorical(NovaMetadata novaMetadata, String type, String endDate, String uuaa, String startDate, String status) throws Errors
    {
        return this.serviceStatisticsapi.getCompilationsHistorical(startDate, endDate, status, type, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getCompilationsHistoricalExport(final NovaMetadata novaMetadata, final String endDate, final String uuaa, final String format, final String type, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getCompilationsHistoricalExport(startDate, endDate, status, type, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getDeployedPlansHistorical(NovaMetadata novaMetadata, String environment, String endDate, String uuaa, String startDate, String status) throws Errors
    {
        return this.serviceStatisticsapi.getDeployedPlansHistorical(startDate, endDate, environment, status, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getDeployedPlansHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getDeployedPlansHistoricalExport(startDate, endDate, environment, status, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ApiSummaryDTO getApisSummary(final NovaMetadata novaMetadata, String environment, String apiFunctionality, String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getApisSummary(environment, apiFunctionality, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getApisSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getApisSummaryExport(environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductSummaryDTO getProductsSummary(final NovaMetadata novaMetadata) throws Errors
    {
        return this.serviceStatisticsapi.getProductsSummary();
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getProductsSummaryExport(final NovaMetadata novaMetadata, final String format) throws Errors
    {
        return this.serviceStatisticsapi.getProductsSummaryExport(format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getFilesystemsHistorical(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String type, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getFilesystemsHistorical(startDate, endDate, environment, status, type, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getFilesystemsHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String type, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getFilesystemsHistoricalExport(startDate, endDate, environment, status, type, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalPoint[] getTodotasksHistorical(NovaMetadata novaMetadata, String role, String endDate, String uuaa, String type, String startDate, String status) throws Errors
    {
        return this.serviceStatisticsapi.getTodotasksHistorical(startDate, endDate, status, type, role, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getTodotasksHistoricalExport(final NovaMetadata novaMetadata, final String role, final String endDate, final String uuaa, final String format, final String type, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getTodotasksHistoricalExport(startDate, endDate, status, type, role, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BudgetSummaryDTO getBudgetsSummary(final NovaMetadata novaMetadata, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBudgetsSummary(uuaa, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBudgetsSummaryExport(final NovaMetadata novaMetadata, final String format, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBudgetsSummaryExport(uuaa, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ConnectorsSummaryDTO getConnectorsSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getConnectorsSummary(environment, uuaa, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getConnectorsSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getConnectorsSummaryExport(environment, uuaa, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public AvailabilityNovaCoinsDTO[] getNovaCoinsByAvailability(NovaMetadata novaMetadata, String budgetType, String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getNovaCoinsByAvailability(uuaa, budgetType);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getNovaCoinsByAvailabilityExport(final NovaMetadata novaMetadata, final String budgetType, final String format, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getNovaCoinsByAvailabilityExport(uuaa, budgetType, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BuildJobSummaryDTO getBuildJobsSummary(final NovaMetadata novaMetadata, final String jobType, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getBuildJobsSummary(jobType, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBuildJobsSummaryExport(final NovaMetadata novaMetadata, final String format, final String jobType, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getBuildJobsSummaryExport(jobType, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public FileTransfersInstancesSummaryDTO getFileTransfersInstancesSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getFileTransfersInstancesSummary(environment, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getFileTransfersInstancesSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getFileTransfersInstancesSummaryExport(environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BatchSchedulerExecutionsSummaryDTO getBatchSchedulerExecutionsSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getBatchSchedulerExecutionsSummary(environment, uuaa, platform);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBatchSchedulerExecutionsSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getBatchSchedulerExecutionsSummaryExport(environment, uuaa, platform, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public TODOTaskSummaryDTO getTodotasksSummary(final NovaMetadata novaMetadata, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getTodotasksSummary(uuaa, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getTodotasksSummaryExport(final NovaMetadata novaMetadata, final String format, final String uuaa, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getTodotasksSummaryExport(uuaa, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getHardwareHistorical(NovaMetadata novaMetadata, String property, String environment, String endDate, String uuaa, String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getHardwareHistorical(startDate, endDate, environment, property, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getHardwareHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String property, final String format, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getHardwareHistoricalExport(startDate, endDate, environment, property, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getConnectorsHistorical(NovaMetadata novaMetadata, String environment, String endDate, String uuaa, String type, String startDate, String status) throws Errors
    {
        return this.serviceStatisticsapi.getConnectorsHistorical(startDate, endDate, environment, status, type, uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getConnectorsHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String type, final String startDate, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getConnectorsHistoricalExport(startDate, endDate, environment, status, type, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ReleaseVersionSummaryDTO getReleaseVersionsSummary(final NovaMetadata novaMetadata, final String uuaa, String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getReleaseVersionsSummary(uuaa, platform, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getReleaseVersionsSummaryExport(final NovaMetadata novaMetadata, final String format, final String uuaa, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getReleaseVersionsSummaryExport(uuaa, platform, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public UserSummaryDTO getUsersSummary(final NovaMetadata novaMetadata, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getUsersSummary(uuaa);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getUsersSummaryExport(final NovaMetadata novaMetadata, final String format, final String uuaa) throws Errors
    {
        return this.serviceStatisticsapi.getUsersSummaryExport(uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public DeploymentPlansSummaryDTO getDeploymentPlansSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getDeploymentPlansSummary(environment, uuaa, platform);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getDeploymentPlansSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String platform) throws Errors
    {
        return this.serviceStatisticsapi.getDeploymentPlansSummaryExport(environment, uuaa, platform, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public CategorySummaryDTO getCategoriesSummary(final NovaMetadata novaMetadata, final String categoryName) throws Errors
    {
        return this.serviceStatisticsapi.getCategoriesSummary(categoryName);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getCategoriesSummaryExport(final NovaMetadata novaMetadata, final String format, final String categoryName) throws Errors
    {
        return this.serviceStatisticsapi.getCategoriesSummaryExport(categoryName, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalPoint[] getUsersConnectedHistorical(final NovaMetadata novaMetadata, final String endDate, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getUsersConnectedHistorical(startDate, endDate);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getUsersConnectedHistoricalExport(final NovaMetadata novaMetadata, final String format, final String endDate, final String startDate) throws Errors
    {
        return this.serviceStatisticsapi.getUsersConnectedHistoricalExport(startDate, endDate, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BrokersSummaryDTO getBrokersSummary(final NovaMetadata novaMetadata, final String environment, final String uuaa, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBrokersSummary(environment, uuaa, platform, status);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBrokersSummaryExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBrokersSummaryExport(environment, uuaa, platform, status, format);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public STHistoricalSerie[] getBrokersHistorical(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String type, final String startDate,
                                                    final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBrokersHistorical(startDate, endDate, environment, status, type, uuaa, platform);
    }

    @Override
    @LogAndTrace(apiName = StatisticsConstants.STATISTICS_API, runtimeExceptionErrorCode = StatisticsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getBrokersHistoricalExport(final NovaMetadata novaMetadata, final String environment, final String endDate, final String uuaa, final String format, final String type,
                                             final String startDate, final String platform, final String status) throws Errors
    {
        return this.serviceStatisticsapi.getBrokersHistoricalExport(startDate, endDate, environment, status, type, uuaa, platform, format);
    }
}
