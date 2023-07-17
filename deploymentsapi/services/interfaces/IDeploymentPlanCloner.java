package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;

/**
 * DeploymentPlanCloner interface
 */
public interface IDeploymentPlanCloner
{
    /**
     * Clones a {@link DeploymentPlan} to another.
     *
     * @param originalPlan Original {@link DeploymentPlan}.
     * @param environment  - Destination environment
     * @return DeploymentPlan.
     */
    DeploymentPlan clonePlanToEnvironment(DeploymentPlan originalPlan, Environment environment);
}
