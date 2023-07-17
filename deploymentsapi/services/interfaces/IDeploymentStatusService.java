package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatus;

/**
 * Interface of  DeploymentStatusService
 */
public interface IDeploymentStatusService
{
    /**
     * Get deployment plan status
     *
     * @param deploymentId deployment id
     * @return action status
     */
    ActionStatus getDeploymentPlanStatus(Integer deploymentId);

    /**
     * Get deployment subsystem status
     *
     * @param subsystemId subsystem id
     * @return action status
     */
    ActionStatus getDeploymentSubsystemStatus(Integer subsystemId);

    /**
     * Get deployment service status
     *
     * @param serviceId service id
     * @return action status
     */
    ActionStatus getDeploymentServiceStatus(Integer serviceId);

    /**
     * Get deployment instance status
     *
     * @param instanceId instance id
     * @return action status
     */
    ActionStatus getDeploymentInstanceStatus(Integer instanceId);
}
