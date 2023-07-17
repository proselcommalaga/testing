package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentGcspDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface of Deployment GCSP Service
 */
public interface IDeploymentGcspService
{
    /**
     * Updates with the new values the GCSP information
     *
     * @param deploymentPlan deployment plan
     * @param gcspDto        dcsp dto
     */
    void updateGcsp(DeploymentPlan deploymentPlan, DeploymentGcspDto gcspDto);

    /**
     * Creates a GcspDto object from a GCSP of the deployment plan
     *
     * @param plan   Deployment plan
     * @param ivuser iv-User
     * @return GcspDto of the GCSP
     */
    DeploymentGcspDto getGcspDto(DeploymentPlan plan, String ivuser);

    /**
     * Return a string with the description of the scrip to deploy a plan with Control M
     *
     * @param plan Deployment Plan
     * @return String with the script description
     */
    String getDeploymentScript(DeploymentPlan plan);

    /**
     * Check if the GCSP is valid for deployment
     *
     * @param plan Deployment Plan
     * @throws NovaException deployment exception
     */
    void validateGcspForDeploy(DeploymentPlan plan);
}
