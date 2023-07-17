package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;

/**
 * Interface that define all action over an broker, to configure it properly
 */
public interface IBrokerConfigurator
{
    /**
     * Configure brokers for deployment plan.
     *
     * @param deploymentPlan the deployment plan
     */
    void configureBrokersForDeploymentPlan(DeploymentPlan deploymentPlan);
}
