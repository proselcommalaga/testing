package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.concurrent.CompletableFuture;

/**
 * The interface Deployment manager service.
 */
public interface IDeploymentManagerService
{
    /**
     * Deploy plan
     *
     * @param plan plan
     * @return true if request has been sent
     */
    boolean deployPlan(DeploymentPlan plan);

    /**
     * Replace plan
     *
     * @param plan    plan
     * @param newPlan new plan
     * @return true if request has been sent
     * @throws NovaException the nova exception
     */
    boolean replacePlan(DeploymentPlan plan, DeploymentPlan newPlan) throws NovaException;

    /**
     * Promote plan
     *
     * @param plan    plan
     * @param newPlan new plan
     */
    void promotePlan(DeploymentPlan plan, DeploymentPlan newPlan);

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
     * Restart instance
     *
     * @param instance - instance
     * @throws NovaException if errors
     */
    void restartInstance(DeploymentInstance instance);

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
     * Restart service
     *
     * @param service Service
     * @throws NovaException if errors
     */
    void restartService(DeploymentService service);

    /**
     * Start subsystem
     *
     * @param subsystem Subsystem.
     * @throws NovaException if errors
     */
    void startSubsystem(DeploymentSubsystem subsystem);

    /**
     * Stop subsystem
     *
     * @param subsystem - subsystem to stop.
     * @throws NovaException if errors
     */
    void stopSubsystem(DeploymentSubsystem subsystem);

    /**
     * Restart subsystem
     *
     * @param subsystem Subsystem.
     * @throws NovaException if errors
     */
    void restartSubsystem(DeploymentSubsystem subsystem);

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
     * Restart plan
     *
     * @param plan Deployment plan.
     * @throws NovaException if errors
     */
    void restartPlan(DeploymentPlan plan);

    /**
     * Get a deployment subsystem services status
     *
     * @param subsystemIdList        the deployment subsystem id list
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a DeploySubsystemStatusDTO array
     * @throws NovaException if any error
     */
    DeploySubsystemStatusDTO[] getDeploymentSubsystemServicesStatus(int[] subsystemIdList, boolean isOrchestrationHealthy) throws NovaException;

    /**
     * Async get a deployment subsystem services status
     *
     * @param deploymentSubsystemId  the deployment subsystem id
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a DeploySubsystemStatusDTO array
     * @throws NovaException if any error
     */
    CompletableFuture<DeploySubsystemStatusDTO> getAsyncDeploymentSubsystemServicesStatus(int deploymentSubsystemId, boolean isOrchestrationHealthy) throws NovaException;

    /**
     * Get all deployment instances status from deployment instance id
     *
     * @param deploymentServiceId    a deployment instance id
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a deploy instance status dto array
     * @throws NovaException the nova exception
     */
    DeployInstanceStatusDTO[] getAllDeploymentInstanceStatus(Integer deploymentServiceId, boolean isOrchestrationHealthy) throws NovaException;

    /**
     * Get all ether deployment instances state from deployment instance provided.
     *
     * @param deploymentInstance the deployment instance
     * @return the deploy instance status dto [ ]
     * @throws NovaException the nova exception
     */
    DeployInstanceStatusDTO[] getEtherServiceDeploymentInstancesStatus(final DeploymentInstance deploymentInstance) throws NovaException;

    /**
     * Get all ether deployment instance status from deployment service provided.
     *
     * @param deploymentService the deployment service
     * @return the deploy instance status dto [ ]
     * @throws NovaException the nova exception
     */
    DeployInstanceStatusDTO[] getEtherServiceDeploymentInstancesStatus(final DeploymentService deploymentService) throws NovaException;

    /**
     * Get the deployment instance status by deployment instnace id
     *
     * @param deploymentInstanceId   the deployment instance id
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a Deployment instance status DTO
     * @throws NovaException the nova exception
     */
    DeployInstanceStatusDTO getDeploymentInstanceStatusById(Integer deploymentInstanceId, boolean isOrchestrationHealthy) throws NovaException;


    /**
     * Get deployment service status DTO from deployment service id
     *
     * @param deploymentServiceId    a deployment instance id
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a deploy instance status dto array
     * @throws NovaException the nova exception
     */
    DeployServiceStatusDTO getServiceStatus(Integer deploymentServiceId, boolean isOrchestrationHealthy) throws NovaException;

    /**
     * Get ETHER deployment service status DTO from deployment service id
     *
     * @param deploymentService the deployment service
     * @return a deploy instance status dto array
     * @throws NovaException the nova exception
     */
    DeployServiceStatusDTO getEtherServiceStatus(DeploymentService deploymentService) throws NovaException;
}
