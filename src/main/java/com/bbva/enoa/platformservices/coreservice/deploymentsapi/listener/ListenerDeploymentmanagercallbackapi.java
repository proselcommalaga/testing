package com.bbva.enoa.platformservices.coreservice.deploymentsapi.listener;

import com.bbva.enoa.apirestgen.deploymentmanagercallbackapi.server.spring.nova.rest.IRestListenerDeploymentmanagercallbackapi;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerCallbackApiService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants.DeployErrors;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceDeploymentException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener deployment manager callback api
 */
@Slf4j
@Service
public class ListenerDeploymentmanagercallbackapi implements IRestListenerDeploymentmanagercallbackapi
{
    /**
     * Service of this listener
     */
    private final IDeploymentManagerCallbackApiService deploymentCallbackService;

    /**
     * Budget service
     */
    private final IBudgetsService budgetsService;

    /**
     * Api Manager Service
     */
    private final IApiManagerService apiManagerService;


    /**
     * Constructor by param
     *
     * @param deploymentCallbackService {@link IDeploymentManagerCallbackApiService}
     * @param budgetsService            service of budgets
     * @param apiManagerService         api management service
     */
    @Autowired
    public ListenerDeploymentmanagercallbackapi(final IDeploymentManagerCallbackApiService deploymentCallbackService, final IBudgetsService budgetsService,
                                                final IApiManagerService apiManagerService)
    {
        this.deploymentCallbackService = deploymentCallbackService;
        this.budgetsService = budgetsService;
        this.apiManagerService = apiManagerService;
    }


    @Override
    public void promotePlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.debug("[promotePlan] -> Success response for plan: {}", planId);
    }


    @Override
    public void startInstanceError(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        log.error("[startInstanceError] -> Error response for instance: {}", instanceId);
    }


    @Override
    public void restartInstanceError(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        log.error("[ListenerDeploymentmanagercallbackapi] -> [restartInstanceError]: Error response for instance: {}", instanceId);
    }


    @Override
    public void stopPlanError(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.error("[stopPlanError] -> Error response for plan: {}", planId);
    }


    @Override
    public void removeService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        log.debug("[removeService] -> Success response for service: {}", serviceId);
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void deployService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        deploymentCallbackService.treatDeployService(serviceId);
    }


    @Override
    public void stopServiceError(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        // Printout the received parameter 'serviceId'
        log.error("[stopServiceError] -> Error response for services: {}", serviceId);
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void deployPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.debug("[deployPlan] -> Callback deploy plan id: [{}] from deployment manager", planId);
        try
        {
            this.budgetsService.synchronizePlanDeployment(planId);
            this.apiManagerService.refreshDeployedApiVersionsState(planId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, planId);
        }
        log.debug("[deployPlan] -> Callback DONE deploy plan id: [{}] from deployment manager", planId);
    }

    @Override
    public void promotePlanError(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        // Printout the received parameter 'planId'
        log.error("[promotePlanError] -> Error response for plan: {}", planId);
        try
        {
            this.deploymentCallbackService.treatPromotePlanError(planId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, planId);
        }
    }


    @Override
    public void startSubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        // Printout the received parameter 'subsystemId'
        log.info("[startSubsystem] -> Success response for subsystem: {}", subsystemId);
    }


    @Override
    public void restartSubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        log.info("[ListenerDeploymentmanagercallbackapi] -> [restartSubsystem]: Success response for subsystem: {}", subsystemId);
    }


    @Override
    public void startSubsystemError(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        // Printout the received parameter 'subsystemId'
        log.error("[startSubsystemError] -> Error response for subsystem: {}", subsystemId);
    }


    @Override
    public void restartSubsystemError(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        log.error("[{}] -> [restartSubsystemError]: Error response for subsystem: {}", DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, subsystemId);
    }


    @Override
    public void startPlanError(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        // Printout the received parameter 'planId'
        log.error("[startPlanError] -> Error response for plan: {}", planId);
    }


    @Override
    public void restartPlanError(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.error("[{}] -> [restartPlanError]: Error response for plan: {}", DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, planId);
    }


    @Override
    public void stopPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.info("[stopPlan] -> Success response for plan: {}", planId);
    }


    @Override
    public void stopSubsystemError(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        // Printout the received parameter 'subsystemId'
        log.error("[stopSubsystemError] -> Error response for subsystem: {}", subsystemId);

    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void deploySubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        deploymentCallbackService.treatDeploySubsystem(subsystemId);
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void deploySubsystemError(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        deploymentCallbackService.treatDeploySubsystemError(subsystemId);
    }


    @Override
    public void removeServiceError(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        // Printout the received parameter 'serviceId'
        log.error("[removeServiceError] -> Error response for services: {}", serviceId);
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void deployServiceError(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        deploymentCallbackService.treatDeployServiceError(serviceId);
    }


    @Override
    public void stopSubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        // Printout the received parameter 'subsystemId'
        log.info("[stopSubsystem] -> Success response for subsystem: {}", subsystemId);
    }


    @Override
    public void startPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        // Printout the received parameter 'planId'
        log.info("[startPlan] -> Success response for plan: {}", planId);
    }


    @Override
    public void restartPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.info("[{}] -> [restartPlan]: Success response for plan: {}", DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, planId);
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void replacePlan(final NovaMetadata novaMetadata, final Integer planId, final Integer newPlanId) throws Errors
    {
        deploymentCallbackService.treatReplacePlan(planId, newPlanId);
    }


    @Override
    public void stopInstanceError(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        // Printout the received parameter 'instanceId'
        log.error("[stopInstanceError] -> Error response for instance: {}", instanceId);
    }


    @Override
    public void deployPlanError(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        log.error("[deployPlanError]: There was errors deploying the deployment plan id: [{}]. Review the deployment manager to find the reason.", planId);
    }


    @Override
    public void startInstance(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        // Printout the received parameter 'instanceId'
        log.info("[startInstance] -> Success response for instance: {}", instanceId);
    }


    @Override
    public void restartInstance(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        // Printout the received parameter 'instanceId'
        log.info("[ListenerDeploymentmanagercallbackapi] -> [restartInstance]: Success response for instance: {}", instanceId);
    }


    @Override
    public void removeSubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        // Printout the received parameter 'subsystemId'
        log.info("[removeSubsystem] -> Success response for subsystem: {}", subsystemId);
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void replacePlanError(final NovaMetadata novaMetadata, final Integer planId, final Integer newPlanId) throws Errors
    {
        try
        {
            log.error("[replacePlanError] -> Error response for plan: {}", planId);
            budgetsService.synchronizePlanUndeployment(planId);
        }
        catch (RuntimeException ex)
        {
            throw new LogAndTraceDeploymentException(ex, planId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, runtimeExceptionErrorCode = DeployErrors.UNEXPECTED_ERROR)
    public void removePlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        this.deploymentCallbackService.treatRemovePlan(planId, MetadataUtils.getIvUser(novaMetadata));
    }


    @Override
    public void removeSubsystemError(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        // Printout the received parameter 'subsystemId'
        log.error("[removeSubsystemError] -> Error response for subsystem: {}", subsystemId);
    }


    @Override
    public void startServiceError(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        // Printout the received parameter 'serviceId'
        log.error("[startServiceError] -> Error response for service: {}", serviceId);
    }


    @Override
    public void restartServiceError(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        log.error("[ListenerDeploymentmanagercallbackapi] -> [restartServiceError]: Error response for service: {}", serviceId);
    }

    @Override
    public void removePlanError(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        this.deploymentCallbackService.treatRemovePlanError(planId, MetadataUtils.getIvUser(novaMetadata));
    }


    @Override
    public void stopInstance(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        // Printout the received parameter 'instanceId'
        log.info("[stopInstance] -> Success response for instance: {}", instanceId);
    }


    @Override
    public void stopService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        // Printout the received parameter 'serviceId'
        log.info("[stopService] -> Success response for service: {}", serviceId);
    }


    @Override
    public void startService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        // Printout the received parameter 'serviceId'
        log.info("[startService] -> Success response for service: {}", serviceId);
    }


    @Override
    public void restartService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        log.info("[{}] -> [restartService]:Success response for service: {}", DeploymentConstants.DEPLOYMENT_CALLBACK_API_NAME, serviceId);
    }

}
