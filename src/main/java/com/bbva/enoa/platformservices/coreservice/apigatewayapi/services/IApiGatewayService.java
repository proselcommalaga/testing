package com.bbva.enoa.platformservices.coreservice.apigatewayapi.services;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;

import java.util.List;

/**
 * Api Gateway Service Integration Interface
 */
public interface IApiGatewayService
{

    /**
     * Registers a new release version with all its Services and API's into the APIGateway
     *
     * @param deploymentPlan Deployment Plan
     */
    void createPublication(final DeploymentPlan deploymentPlan);

    /**
     * Registers a new version with all its Services and API's into the APIGateway
     *
     * @param bsConfiguration behavior service configuration
     */
    void createPublication(final BehaviorServiceConfiguration bsConfiguration);

    /**
     * Unregisters API from release from API Gateway
     *
     * @param deploymentPlan Deployment Plan
     */
    void removePublication(final DeploymentPlan deploymentPlan);

    /**
     * Updates a publication when replacing a plan
     *
     * @param createdServices list of new services
     * @param removedServices list of removed services
     * @param newPlan         new plan
     * @param oldPlan         old plan
     */
    void updatePublication(final List<DeploymentService> createdServices, final List<DeploymentService> removedServices, final DeploymentPlan newPlan, final DeploymentPlan oldPlan);

    /**
     * For a Deployment Plan on an Environment, generates the docker key for each ReleaseVersionService (service) of the
     * DeploymentPlan
     *
     * @param deploymentPlan Deployment Plan
     */
    void generateDockerKey(final DeploymentPlan deploymentPlan);

    /**
     * For a Deployment Service list on an Environment, generates the docker key for each service
     *
     * @param deploymentServiceList Deployment service list
     * @param environment environment
     */
    void generateDockerKey(final List<DeploymentService> deploymentServiceList, final String environment);

    /**
     * Publishing plan profiling at CES
     *
     * @param deploymentPlan plan
     */
    void createProfiling(final DeploymentPlan deploymentPlan);

    /**
     * Removing plan profiling from CES
     *
     * @param deploymentPlan plan
     */
    void removeProfiling(final DeploymentPlan deploymentPlan);

}
