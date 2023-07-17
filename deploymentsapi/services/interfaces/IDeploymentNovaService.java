package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentNovaDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

public interface IDeploymentNovaService
{
    /**
     * Updates with the new values the Nova Planned information
     *
     * @param deploymentPlan deployment plan
     * @param novaDto        nova planned dto
     */
    void updateNova(DeploymentPlan deploymentPlan, DeploymentNovaDto novaDto);

    /**
     * Creates a DeploymentNovaDto object from a DeploymentNova of the deployment plan
     *
     * @param plan   Deployment plan
     * @param ivuser iv-User
     * @return DeploymentNovaDto of the Nova Planned
     */
    DeploymentNovaDto getNovaDto(DeploymentPlan plan, String ivuser);

    /**
     * Return a string with the description of the actions to deploy a plan with Nova Planned
     *
     * @param plan Deployment Plan
     * @return String with the actions description
     */
    String getDeploymentActions(DeploymentPlan plan);

    /**
     * Check if the Nova Planned is valid for deployment
     *
     * @param plan Deployment Plan
     * @throws NovaException deployment exception
     */
    void validateNovaPlannedForDeploy(DeploymentPlan plan);
}
