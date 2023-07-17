package com.bbva.enoa.platformservices.coreservice.behaviorapi.listener;

import com.bbva.enoa.apirestgen.behaviorapi.model.*;
import com.bbva.enoa.apirestgen.behaviorapi.server.spring.nova.rest.IRestListenerBehaviorapi;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorConfigurationService;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorService;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener to Behavior API.
 */
@Service
@Slf4j
public class ListenerBehaviorapi implements IRestListenerBehaviorapi
{

    private final IBehaviorService iBehaviorService;
    private final IBehaviorConfigurationService iBehaviorConfigurationService;

    /**
     * Dependency injection constructor
     *
     * @param iBehaviorService              release version Service
     * @param iBehaviorConfigurationService behavior configuration service
     */
    @Autowired
    public ListenerBehaviorapi(final IBehaviorService iBehaviorService, IBehaviorConfigurationService iBehaviorConfigurationService)
    {
        this.iBehaviorService = iBehaviorService;
        this.iBehaviorConfigurationService = iBehaviorConfigurationService;
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public BVRequestDTO newBehaviorVersionRequest(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iBehaviorService.newBehaviorVersionRequest(ivUser, productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public BVValidationResponseDTO validateBehaviorVersionRequest(final NovaMetadata novaMetadata, final BVSubsystemTagDTO behaviorVersionRequest, final Integer productId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.iBehaviorService.validateRequestTag(ivUser, productId, behaviorVersionRequest);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVExecutionPageableDTO getBehaviorServiceExecutions(NovaMetadata novaMetadata, Integer behaviorServiceId, Long behaviorServiceExecFrom, Integer pageSize, Integer pageNumber, Long behaviorServiceExecTo) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorServiceExecutions(behaviorServiceId, behaviorServiceExecFrom, behaviorServiceExecTo, pageNumber, pageSize);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void buildBehaviorVersion(final NovaMetadata novaMetadata, final BVBehaviorVersionDTO behaviorVersion, final Integer productId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iBehaviorService.buildBehaviorVersion(behaviorVersion, ivUser, productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void removeBehaviorInstanceExecution(final NovaMetadata novaMetadata, final Integer behaviorInstanceId) throws Errors
    {
        this.iBehaviorConfigurationService.removeBehaviorInstanceExecution(behaviorInstanceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void executeBehaviorConfiguration(final NovaMetadata novaMetadata, final BVBehaviorParamsDTO behaviorBatchParams, final Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.executeBehaviorConfiguration(MetadataUtils.getIvUser(novaMetadata), behaviorBatchParams, behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVHardwareResourceDto getBehaviorHardwareConfigurationResource(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorHardwareConfigurationResource(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorHardwareConfigurationResource(NovaMetadata novaMetadata, BVConfigurableHardware resourceDtoToUpdate, Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.updateBehaviorHardwareConfigurationResource(resourceDtoToUpdate, behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void deleteBehaviorVersion(final NovaMetadata novaMetadata, final Integer behaviorVersionId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iBehaviorService.deleteBehaviorVersion(ivUser, behaviorVersionId);
    }


    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBehaviorVersionSummaryInfoDTO[] getAllBehaviorVersions(final NovaMetadata novaMetadata, final Integer productId, final String status) throws Errors
    {
        return this.iBehaviorService.getAllBehaviorVersions(productId, status);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBehaviorInstanceDTO getBehaviorInstance(final NovaMetadata novaMetadata, final Integer behaviorInstanceId) throws Errors
    {
        return this.iBehaviorService.getBehaviorInstance(behaviorInstanceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVPropertiesResourceDto getBehaviorPropertiesConfigurationResource(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorPropertiesConfigurationResource(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorPropertiesConfigurationResource(NovaMetadata novaMetadata, BVConfigurableProperty[] resourceDtoToUpdate, Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.updateBehaviorPropertiesConfigurationResource(resourceDtoToUpdate, behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBrokersResourceDto getBehaviorBrokerConfigurationResource(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorBrokerConfigurationResource(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorBrokerConfigurationResource(NovaMetadata novaMetadata, BVConfigurableBroker[] resourceDtoToUpdate, Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.updateBehaviorBrokerConfigurationResource(resourceDtoToUpdate, behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public Boolean checkBehaviorBudgets(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorService.checkBehaviorBudgets(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBehaviorServiceConfigurationSummary getBehaviorServiceSummaryResources(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorServiceSummaryResources(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void stopBehaviorConfigurationExecution(final NovaMetadata novaMetadata, final Integer behaviorExecutionId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.iBehaviorConfigurationService.stopBehaviorConfigurationExecution(ivUser, behaviorExecutionId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBehaviorServiceBudgetInfo getBehaviorVersionCostsInfo(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorVersionCostsInfo(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVFilesystemResourceDto getBehaviorFilesystemsConfigurationResource(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorFilesystemsConfigurationResource(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorFilesystemsConfigurationResource(final NovaMetadata novaMetadata, final BVConfigurableFilesystem[] resourceDtoToUpdate, final Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.updateBehaviorFilesystemsConfigurationResource(resourceDtoToUpdate, behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVConnectorsResourceDto getBehaviorConnectorsConfigurationResource(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorConnectorsConfigurationResource(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorConnectorsConfigurationResource(NovaMetadata novaMetadata, BVConfigurableConnector[] resourceDtoToUpdate, Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.updateBehaviorConnectorsConfigurationResource(resourceDtoToUpdate, behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVExecutionPageableDTO getBehaviorVersionsExecutions(NovaMetadata novaMetadata, Integer behaviorVersionId, Integer pageSize, Integer pageNumber, String behaviorServiceName) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorVersionExecutions(behaviorVersionId, behaviorServiceName, pageSize, pageNumber);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVExecutionInfoDto getBehaviorExecutionInfo(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorExecutionInfo(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorTestSubsystemBuildStatus(final NovaMetadata novaMetadata, final BVBehaviorSubsystemBuildStatus behaviorTestSubsystemBuildStatus, final Integer behaviorTestSubsystemId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        iBehaviorService.updateBehaviorTestSubsystemBuildStatus(ivUser, behaviorTestSubsystemId, behaviorTestSubsystemBuildStatus);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBehaviorVersionSubsystemDTO[] getBehaviorVersionsSubsystems(NovaMetadata novaMetadata, Integer behaviorVersionId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorVersionsSubsystems(behaviorVersionId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVBehaviorVersionSummaryInfoDTO getBehaviorVersion(final NovaMetadata novaMetadata, final Integer behaviorVersionId) throws Errors
    {
        return this.iBehaviorService.getBehaviorVersion(behaviorVersionId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BVJVMResourceDto getBehaviorJvmConfigurationResource(NovaMetadata novaMetadata, Integer behaviorServiceId) throws Errors
    {
        return this.iBehaviorConfigurationService.getBehaviorJvmConfigurationResource(behaviorServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.BEHAVIOR_API_NAME, runtimeExceptionErrorCode = Constants.BehaviorErrors.UNEXPECTED_ERROR_CODE)
    public void updateBehaviorJvmConfigurationResource(NovaMetadata novaMetadata, BVConfigurableJvm resourceDtoToUpdate, Integer behaviorServiceId) throws Errors
    {
        this.iBehaviorConfigurationService.updateBehaviorJvmConfigurationResource(resourceDtoToUpdate, behaviorServiceId);
    }
}
