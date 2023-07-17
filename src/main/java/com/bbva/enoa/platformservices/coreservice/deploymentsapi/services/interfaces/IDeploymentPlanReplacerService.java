package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Services for replace a deployment plan
 */
public interface IDeploymentPlanReplacerService
{
    /**
     * Build a NOVA Exception with the message when the deployment plan cannot be replaced
     *
     * @param oldPlan old deployment plan
     * @param newPlan new deployment plan
     * @throws NovaException exception
     */
    void buildReplacePlanNovaException(DeploymentPlan oldPlan, DeploymentPlan newPlan) throws NovaException;
}
