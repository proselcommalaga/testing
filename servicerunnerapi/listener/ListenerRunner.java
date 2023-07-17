package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.listener;

import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.servicerunnerapi.server.spring.nova.rest.IRestListenerServicerunnerapi;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.ITaskProcessor;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IRunnerApiService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceDeploymentException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.AbstractMap;

/**
 * Runner listener
 */
@Service
@Transactional(rollbackFor = Errors.class)
public class ListenerRunner implements IRestListenerServicerunnerapi
{

    private final IRunnerApiService runnerApiService;
    private final ITaskProcessor taskProcessor;


    /**
     * Constructor by param
     *
     * @param runnerApiService runnerAPI service
     * @param taskProcessor    task processor
     */
    @Autowired
    public ListenerRunner(final IRunnerApiService runnerApiService,
                          final ITaskProcessor taskProcessor)
    {
        this.runnerApiService = runnerApiService;
        this.taskProcessor = taskProcessor;
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO stopBatchScheduleInstance(final NovaMetadata novaMetadata, Integer scheduleInstanceId, Integer deploymentPlanId) throws Errors
    {
        final String ivUser = MetadataUtils.getIvUser(novaMetadata);
        try
        {
            return this.runnerApiService.stopBatchScheduleInstance(scheduleInstanceId, deploymentPlanId, ivUser);
        }
        catch (NovaException exception)
        {
            throw exception.getNovaError().getErrors(
                    new AbstractMap.SimpleImmutableEntry<>("scheduleInstanceId", String.valueOf(scheduleInstanceId)),
                    new AbstractMap.SimpleImmutableEntry<>("deploymentPlanId", String.valueOf(deploymentPlanId))
            );
        }

    }

    /**
     * On task reply
     *
     * @param taskId    task id
     * @param newStatus new status
     * @throws Errors on error
     */
    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public void onServiceRunnerTaskReply(final NovaMetadata novaMetadata, Integer taskId, String newStatus) throws Errors
    {
        this.taskProcessor.onTaskReply(taskId, newStatus);
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO resumeBatchScheduleInstance(final NovaMetadata novaMetadata, Integer scheduleInstanceId, Integer deploymentPlanId) throws Errors
    {
        try
        {
            final String ivUser = MetadataUtils.getIvUser(novaMetadata);
            return this.runnerApiService.resumeBatchScheduleInstance(scheduleInstanceId, deploymentPlanId, ivUser);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentPlanId);
        }
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO pauseBatchScheduleInstance(final NovaMetadata novaMetadata, Integer scheduleInstanceId, Integer deploymentPlanId) throws Errors
    {
        try
        {
            return this.runnerApiService.pauseBatchScheduleInstance(scheduleInstanceId, deploymentPlanId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentPlanId);
        }
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO stopSubsystem(final NovaMetadata novaMetadata, Integer subsystemId) throws Errors
    {
        return this.runnerApiService.stopSubsystem(subsystemId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO startPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        try
        {
            return this.runnerApiService.startPlan(planId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, planId);
        }
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO startBatchScheduleInstance(final NovaMetadata novaMetadata, Integer releaseVersionServiceId, Integer deploymentPlanId) throws Errors
    {
        try
        {
            return this.runnerApiService.startBatchScheduleInstance(releaseVersionServiceId, deploymentPlanId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentPlanId);
        }
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO startBatchSchedule(final NovaMetadata novaMetadata, Integer serviceId) throws Errors
    {
        return this.runnerApiService.startBatchSchedule(serviceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO restartPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        try
        {
            return this.runnerApiService.restartPlan(planId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, planId);
        }
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public Integer restartEphoenixInstanceByPlanification(final NovaMetadata novaMetadata, final String environment, final String hostname, final String releaseName, final String serviceName, final String subsystemName, final String productName) throws Errors
    {
        return this.runnerApiService.restartEphoenixInstanceByPlanification(MetadataUtils.getIvUser(novaMetadata), environment, hostname, productName, releaseName, subsystemName, serviceName);
    }


    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO startInstance(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        return this.runnerApiService.startInstance(instanceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO restartSubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        return this.runnerApiService.restartSubsystem(subsystemId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO restartService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        return this.runnerApiService.restartService(serviceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO startSubsystem(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        return this.runnerApiService.startSubsystem(subsystemId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO restartInstance(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        return this.runnerApiService.restartInstance(instanceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO stopInstance(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        return this.runnerApiService.stopInstance(instanceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO stopPlan(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        try
        {
            return this.runnerApiService.stopPlan(planId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, planId);
        }
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO stopService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        return this.runnerApiService.stopService(serviceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO stopBatchSchedule(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        return this.runnerApiService.stopBatchSchedule(serviceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public String[] getInstancesStatuses(NovaMetadata novaMetadata) throws Errors
    {
        return this.runnerApiService.getInstancesStatuses();
    }

    @Override
    @LogAndTrace(apiName = ServiceRunnerConstants.SERVICE_RUNNER_API_NAME, runtimeExceptionErrorCode = ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO startService(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        return this.runnerApiService.startService(serviceId, MetadataUtils.getIvUser(novaMetadata));
    }

}
