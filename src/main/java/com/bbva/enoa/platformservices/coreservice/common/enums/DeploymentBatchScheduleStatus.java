package com.bbva.enoa.platformservices.coreservice.common.enums;

/**
 * Possibles Deployment batch schedule service status
 */
public enum DeploymentBatchScheduleStatus
{
    /**
     * Activate a batch schedule service
     */
    ENABLED("ENABLED"),
    /**
     * Disable a batch schedule service
     */
    DISABLED("DISABLED"),
    /**
     * Remove a batch schedule serviec
     */
    UNDEPLOYED("UNDEPLOYED");

    /**
     * Deployment batch schedule service status
     */
    private String deploymentBatchScheduleStatus;

    /**
     * Constructor
     * @param deploymentBatchScheduleStatus deploymentBatchScheduleStatus
     */
    DeploymentBatchScheduleStatus (String deploymentBatchScheduleStatus)
    {
        this.deploymentBatchScheduleStatus = deploymentBatchScheduleStatus;
    }

    /**
     * Get the Deployment batch schedule service status
     * @return a deployment batch schedule service status String
     */
    public String getDeploymentBatchScheduleStatus ()
    {
        return this.deploymentBatchScheduleStatus;
    }
}
