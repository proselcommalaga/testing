package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemServicesStatusDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;

/**
 * DeploymentServiceNumberDTO Builder interface
 */
public interface IDeploymentServicesNumberDtoBuilder
{
    /**
     * Builds a DTO with some numbers from a deployment services:
     * <ul>
     * <li>Number of total services of the deployment: will match all the services from the deployed release version.</li>
     * <li>Number of running services: those which have running instances and how many.</li>
     * </ul>
     *
     * @param deploymentPlanId    DeploymentPlan ID.
     * @param deploymentServiceId DeploymentService ID.
     * @return {@link ServiceStatusDTO}
     */
    ServiceStatusDTO buildServiceStatusDTO(int deploymentPlanId, int deploymentServiceId);

    /**
     * Builds a DTO with some numbers from a deployment plan
     * Number of total services of the deployment: will match all the services from the deployed release version.
     * Number of running services: those which have running instances and how many.
     *
     * @param deploymentPlanId DeploymentPlan ID.
     * @return a ServiceStatusDto object
     */
    StatusDTO buildStatusDTO(int deploymentPlanId);

    /**
     * Builds a DTO with some numbers from a deployment subsystem
     * Number of total services of the deployment: will match all the services from the deployed release version.
     * Number of running services: those which have running instances and how many.
     *
     * @param deploymentPlan         deployment plan
     * @param deploymentSubsystemId  DeploymentPlan ID.
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a ServiceStatusDto object
     */
    SubsystemServicesStatusDTO buildSubsystemServiceDTO(DeploymentPlan deploymentPlan, int deploymentSubsystemId, boolean isOrchestrationHealthy);

    /**
     * Building an array with all subsystem services status of deployment plan
     *
     * @param deploymentPlan         deployment plan
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return SubsystemServicesStatusDto object array
     */
    SubsystemServicesStatusDTO[] buildSubsystemsServiceStatusDTO(DeploymentPlan deploymentPlan, boolean isOrchestrationHealthy);
}
