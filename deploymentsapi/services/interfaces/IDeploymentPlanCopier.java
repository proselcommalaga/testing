package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;

/**
 * DeploymentPlanCopier
 */
public interface IDeploymentPlanCopier
{
    /**
     * Copies a {@link DeploymentPlan} to another.
     *
     * @param originalPlan Original {@link DeploymentPlan}.
     * @return DeploymentPlan.
     */
    DeploymentPlan copyPlan(DeploymentPlan originalPlan);
}
