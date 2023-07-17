package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.RunningBatchs;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.SpecificInstance;

public interface IBatchManagerClient
{

    /**
     * Get running batch deployment services by deployment service id list
     * For each deployment service id from the list, return a object that indicate if this deployment service is running or not
     *
     * @param deploymentServiceIdList a deployment service list
     * @param environment             the environment to find de batch associated to deployment instance id list
     * @return the same list from deployment service id indicating if a deployment service batch is running or not
     */
    DeploymentServiceBatchStatus[] getRunningBatchDeploymentServices(int[] deploymentServiceIdList, String environment);

    /**
     * Get running instances by deployment service id
     *
     * @param deploymentServiceId the deployment service id
     * @return a running deployment instances by this deployment service batch
     */
    RunningBatchs getRunningInstances(Long deploymentServiceId);

    /**
     * Get all batch running of deployment plan
     *
     * @param deploymentPlanId       a deployment plan id
     * @return the number of batch running into deployment plan
     */
    Integer getRunningInstancesByDeploymentPlan(final Integer deploymentPlanId);

    /**
     * Gets summarized info for batch instance executions.
     *
     * @param environment          The environment to be summarized. "ALL" for all of them.
     * @param deploymentServiceIds An array of deployment service ids for filtering purposes.
     * @param uuaa                Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param platform             The platform to be filtered ("NOVA" or "ETHER").
     * @param origin               The initiator to be filtered.
     * @return Summarized info for batch instance executions.
     */
    BatchManagerBatchExecutionsSummaryDTO getBatchExecutionsSummary(final String environment, final long[] deploymentServiceIds, final String uuaa, final String platform,
                                                                    final String origin);

    /**
     * Get all the UUAAs Ephoenix Legacy
     *
     * @return An array containing all the UUAAs Ephoenix Legacy
     */
    String[] getUuaasEphoenixLegacy();


    /**
     * Gets instance by id.
     *
     * @param batchId     the batch id
     * @param environment the environment
     * @return the instance by id
     */
    SpecificInstance getInstanceById(final Integer batchId, final String environment);
}
