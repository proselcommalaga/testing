package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.enumerates;

import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

public enum DeploymentInstanceStatus {

    RUNNING(true),
    STOPPED(false);

    /**
     * boolean representing whether the instance is started.
     */
    private final boolean started;

    DeploymentInstanceStatus(boolean started)
    {
        this.started = started;
    }

    /**
     * Get started
     *
     * @return started
     */
    public boolean isStarted()
    {
        return started;
    }

    /**
     * Map a String to a {@link DeploymentInstanceStatus}. Throw an exception if it cannot be mapped.
     *
     * @param deploymentInstanceStatus A String representing a {@link DeploymentInstanceStatus}.
     * @return A {@link DeploymentInstanceStatus}.
     * @throws NovaException Thrown when it cannot be mapped.
     */
    public static DeploymentInstanceStatus getValueOf(String deploymentInstanceStatus) throws NovaException
    {
        try
        {
            return DeploymentInstanceStatus.valueOf(deploymentInstanceStatus.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(ServiceRunnerError.getDeploymentInstanceStatusNotValidError(deploymentInstanceStatus));
        }
    }
}
