package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryHistoryItemDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

public interface IDeploymentManagerClient
{
    /**
     * Init de client
     */
    void init();

    /**
     * Remove plan
     *
     * @param plan plan
     * @return true if request has been sent
     */
    boolean removePlan(DeploymentPlan plan);

    /**
     * Restart instance
     *
     * @param instance - instance
     * @throws NovaException if errors
     */
    void restartInstance(DeploymentInstance instance);

    /**
     * Start instance
     *
     * @param instance - instance
     * @throws NovaException if errors
     */
    void startInstance(DeploymentInstance instance);

    /**
     * Stop instance
     *
     * @param instance - instance to stop
     * @throws NovaException if errors
     */
    void stopInstance(DeploymentInstance instance);

    /**
     * Restart plan
     *
     * @param plan Deployment plan.
     * @throws NovaException if errors
     */
    void restartPlan(DeploymentPlan plan);

    /**
     * Start plan
     *
     * @param plan Deployment plan.
     * @throws NovaException if errors
     */
    void startPlan(DeploymentPlan plan);

    /**
     * Ask the container cluster to Stop all services of a deployment plan
     *
     * @param plan Deployment plan.
     * @throws NovaException if errors
     */
    void stopPlan(DeploymentPlan plan);

    /**
     * Deploy plan
     *
     * @param planId plan id
     * @return true if request has been sent
     */
    boolean deployPlan(int planId);

    /**
     * Replace plan
     *
     * @param planId    plan id
     * @param newPlanId new plan id
     */
    void promotePlan(int planId, int newPlanId);

    /**
     * Replace plan
     *
     * @param planId            plan id
     * @param newPlanId         new plan
     * @param servicesToCreate  list of services to create
     * @param servicesToRestart list of services to restart
     * @param serviceToRemove   list of services to remove
     * @return true if request has been sent
     */
    boolean replacePlan(int planId, int newPlanId, List<DeploymentService> servicesToCreate, List<DeploymentService> serviceToRemove, List<DeploymentService> servicesToRestart);

    /**
     * Restart service
     *
     * @param service Service
     * @throws NovaException if errors
     */
    void restartService(DeploymentService service);

    /**
     * Start service
     *
     * @param service Service
     * @throws NovaException if errors
     */
    void startService(DeploymentService service);

    /**
     * Stop service
     *
     * @param service Service.
     * @throws NovaException if errors
     */
    void stopService(DeploymentService service);

    /**
     * Get the number of running instances on a container cloud for a service.
     *
     * @param serviceId Service id.
     * @return Number of running instances.
     */
    ServiceStatusDTO getContainersOfService(Integer serviceId);

    /**
     * Get the number of running services on plan
     *
     * @param planId plan id.
     * @return Number of running services.
     */
    StatusDTO getDeploymentPlanServicesStatus(Integer planId);

    /**
     * Get the number of running services on subsystem
     *
     * @param subsystemIds subsystem id array.
     * @return Number of running services.
     */
    DeploySubsystemStatusDTO[] getDeploymentSubsystemServicesStatus(int[] subsystemIds);

    /**
     * Get all deployment instances statuses from deployment service id
     *
     * @param deploymentServiceId    the deployment service id
     * @return deployment instance status DTO array
     */
    DeployInstanceStatusDTO[] getAllDeploymentInstanceStatus(Integer deploymentServiceId);

    /**
     * Get the deployment instance status from deployment instance id
     *
     * @param deploymentInstanceId   the deployment instance id
     * @return deployment instance status DTO
     */
    DeployInstanceStatusDTO getDeploymentInstanceStatusById(Integer deploymentInstanceId);

    /**
     * Get a deploy service status DTO
     *
     * @param deploymentServiceId    the deployment service id
     * @return a deploy service status Dto
     */
    DeployServiceStatusDTO getServiceStatus(Integer deploymentServiceId);

    /**
     * Deploy service
     *
     * @param serviceId service id
     * @return {@code true} if the call to remote service has been done.
     */
    boolean deployService(Integer serviceId);

    /**
     * Deploy subsystem
     *
     * @param subsystemId subsystem id
     * @return {@code true} if the call to remote service has been done.
     */
    boolean deploySubsystem(Integer subsystemId);

    /**
     * Restart subsystem
     *
     * @param subsystem Subsystem.
     * @throws NovaException if errors
     */
    void restartSubsystem(final DeploymentSubsystem subsystem);

    /**
     * Start subsystem
     *
     * @param subsystem Subsystem.
     * @throws NovaException if errors
     */
    void startSubsystem(final DeploymentSubsystem subsystem);

    /**
     * Stop subsystem
     *
     * @param subsystem - subsystem to stop.
     * @throws NovaException if errors
     */
    void stopSubsystem(final DeploymentSubsystem subsystem);

    /**
     * Gets an array of DTOs having information for memory statistic history loading.
     *
     * @return an array of DTOs having information for memory statistic history loading.
     */
    HostMemoryHistoryItemDTO[] getHostsMemoryHistorySnapshot();

}
