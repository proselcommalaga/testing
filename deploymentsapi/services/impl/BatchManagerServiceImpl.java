package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;


import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.RunningBatchs;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBatchManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.SubsystemBatchServiceStatus;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IBatchManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Service for managing Batch Manager service
 *
 * @author BBVA
 */
@Service
@Slf4j
public class BatchManagerServiceImpl implements IBatchManagerService
{
    /**
     * Batch manager client
     */
    private final IBatchManagerClient batchManagerClient;

    /**
     * Constructor
     *
     * @param batchManagerClient batch manager client
     */
    @Autowired
    public BatchManagerServiceImpl(final IBatchManagerClient batchManagerClient)
    {
        this.batchManagerClient = batchManagerClient;
    }

    /////////////////////////////////////////// IMPLEMENTATIONS ////////////////////////////////////////////////////

    @Override
    public RunningBatchs getRunningInstances(final Integer deploymentServiceId, final boolean isOrchestrationHealthy)
    {
        RunningBatchs runningBatchs = new RunningBatchs();
        if (isOrchestrationHealthy)
        {
            runningBatchs = this.batchManagerClient.getRunningInstances(deploymentServiceId.longValue());
        }
        else
        {
            log.warn("[BatchManagerServiceImpl] -> [getRunningInstances]: the cluster Orchestration is unhealthy for checking batch status of the deployment Service Id: [{}]. Return an empty running batch", deploymentServiceId);
            runningBatchs.setRunningBatchs(0L);
            runningBatchs.setServiceId(0);
        }

        return runningBatchs;
    }

    @Override
    public CompletableFuture<SubsystemBatchServiceStatus> getRunningBatchDeploymentServicesAsync(final int[] deploymentServiceIdList, final String environment, final Integer deploymentSubsystemId, final boolean isOrchestrationHealthy)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            SubsystemBatchServiceStatus subsystemBatchServiceStatus = new SubsystemBatchServiceStatus();
            subsystemBatchServiceStatus.setDeploymentSubsystemId(deploymentSubsystemId);

            if (isOrchestrationHealthy)
            {
                subsystemBatchServiceStatus.setDeploymentServiceBatchStatusList(this.batchManagerClient.getRunningBatchDeploymentServices(deploymentServiceIdList, environment));
            }
            else
            {
                log.warn("[BatchManagerServiceImpl] -> [getRunningBatchDeploymentServicesAsync]: the cluster Orchestration is unhealthy for checking batch status of the deployment Service list Ids: [{}] - environment: [{}]. Return an empty DeploymentServiceBatchStatus array",
                        deploymentServiceIdList, environment);
                subsystemBatchServiceStatus.setDeploymentServiceBatchStatusList(new DeploymentServiceBatchStatus[0]);
            }

            return subsystemBatchServiceStatus;
        });
    }

    @Override
    public CompletableFuture<Integer> getRunningInstancesByDeploymentPlan(final Integer deploymentPlanId, final boolean isOrchestrationHealthy)
    {
        return CompletableFuture.supplyAsync(() ->
        {
            Integer runningInstances;
            if (isOrchestrationHealthy)
            {
                runningInstances = this.batchManagerClient.getRunningInstancesByDeploymentPlan(deploymentPlanId);
            }
            else
            {
                log.warn("[BatchManagerServiceImpl] -> [getRunningInstancesByDeploymentPlan]: the cluster Orchestration is unhealthy for checking batch status of the deployment plan Id: [{}]. Return [0] batch instances running", deploymentPlanId);
                runningInstances = 0;
            }

            return runningInstances;
        });
    }
}
