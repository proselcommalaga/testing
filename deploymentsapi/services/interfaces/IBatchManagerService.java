package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.RunningBatchs;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.SubsystemBatchServiceStatus;

import java.util.concurrent.CompletableFuture;

public interface IBatchManagerService
{
    /**
     * Get running instances from deployment service id
     *
     * @param deploymentServiceId    the deployment service id
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a {@link RunningBatchs instance}
     */
    RunningBatchs getRunningInstances(Integer deploymentServiceId, boolean isOrchestrationHealthy);

    /**
     * Get a running batch deployment services from deployment service list async
     *
     * @param deploymentServiceIdList a deployment service id list
     * @param environment             the environment to check
     * @param deploymentSubsystemId   the deployment subssytem id
     * @param isOrchestrationHealthy  if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a Completable future of {@link DeploymentServiceBatchStatus instance}
     */
    CompletableFuture<SubsystemBatchServiceStatus> getRunningBatchDeploymentServicesAsync(int[] deploymentServiceIdList, String environment, Integer deploymentSubsystemId, boolean isOrchestrationHealthy);

    /**
     * Get running batch into deployment plan
     *
     * @param deploymentPlanId       the deployment plan id
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a number of the batches in status RUNNING or REGISTERING
     */
    CompletableFuture<Integer> getRunningInstancesByDeploymentPlan(Integer deploymentPlanId, boolean isOrchestrationHealthy);
}
