package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

/**
 * Service of DeploymentManagerCallbackAPI
 */
public interface IDeploymentManagerCallbackApiService
{
    /**
     * callback of a service deploy
     *
     * @param serviceId id of service
     */
    void treatDeployService(Integer serviceId);

    /**
     * treat a callback of a deploy service error
     *
     * @param deploymentServiceId id of service
     */
    void treatDeployServiceError(Integer deploymentServiceId);

    /**
     * treat a callback of a deploy subsystem
     *
     * @param deploymentSubsystemId id of subsystem
     */
    void treatDeploySubsystem(Integer deploymentSubsystemId);

    /**
     * Treat a callback of a deploy subsystem error
     *
     * @param deploymentSubsystemId subsystemId
     */
    void treatDeploySubsystemError(Integer deploymentSubsystemId);

    /**
     * Treat a callback over a replaced plan
     *
     * @param planId    id of original plan
     * @param newPlanId id of current plan
     */
    void treatReplacePlan(Integer planId, Integer newPlanId);

    /**
     * Treat a callback over a removed plan.
     * This method create a new to do task if this operation fails via NOVA Exception
     * If deployment plan exists, will create a UNDEPLOY_ERROR task
     * If not, creates a new INTERNAL ERROR task.
     *
     * @param deploymentPlanId id of the removed plan
     * @param userCode         the user code of the requester
     */
    void treatRemovePlan(Integer deploymentPlanId, String userCode);

    /**
     * Treat a callback over a promoted plan
     *
     * @param deploymentPlanId id of the promoted plan
     */
    void treatPromotePlanError(Integer deploymentPlanId);

    /**
     * This method just creates a new to do task depending on if the deployment plan exists
     * If deployment plan exists, will create a UNDEPLOY_ERROR task
     * If not, creates a new INTERNAL ERROR task
     *
     * @param deploymentPlanId id of the undeploy plan
     * @param userCode         the user code of the requester
     */
    void treatRemovePlanError(Integer deploymentPlanId, String userCode);
}
