package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemServicesStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentServicesNumberDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants.ACTION_NOT_ALLOWED;


/**
 * Builds a DeploymentServicesNumberDto with the numbers
 * of running and total services from a deployment of a product
 * on an environment.
 */
@Slf4j
@Service
public class DeploymentServicesNumberDtoBuilderImpl implements IDeploymentServicesNumberDtoBuilder
{
    /** Validator */
    private final IDeploymentsValidator deploymentsValidator;

    /** Deployment manager client */
    private final IDeploymentManagerClient deploymentManagerClient;

    /** Ether manager client */
    private final IEtherManagerClient etherManagerClient;

    /** Deployment utils */
    private final DeploymentUtils deploymentUtils;

    /**
     * Constructor
     *
     * @param deploymentManagerClient        service client
     * @param etherManagerClient   ether manager client
     * @param deploymentsValidator deployment validator
     * @param deploymentUtils      deployment utils
     */
    @Autowired
    public DeploymentServicesNumberDtoBuilderImpl(final IDeploymentManagerClient deploymentManagerClient,
                                                  final IEtherManagerClient etherManagerClient, final DeploymentUtils deploymentUtils,
                                                  final IDeploymentsValidator deploymentsValidator)
    {

        this.deploymentManagerClient = deploymentManagerClient;
        this.etherManagerClient = etherManagerClient;
        this.deploymentUtils = deploymentUtils;
        this.deploymentsValidator = deploymentsValidator;
    }

    @Override
    public ServiceStatusDTO buildServiceStatusDTO(int deploymentPlanId, int deploymentServiceId)
    {
        log.debug("[Deployment API] -> [buildServiceStatusDTO]: Building DTO with number of containers of deployment plan id: [{}] for deployment service id: [{}]", deploymentPlanId, deploymentServiceId);
        ServiceStatusDTO serviceStatusDTO=null;

        // Retrieve the deployment plan.
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlan(deploymentPlanId);
        DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(deploymentServiceId);

        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            serviceStatusDTO = this.etherManagerClient.getServiceStatus(this.deploymentUtils.buildEtherDeploymentDTO(deploymentService, "", ""));
        }
        else
        {
            serviceStatusDTO = this.deploymentManagerClient.getContainersOfService(deploymentServiceId);
        }

        log.debug("[Deployment API] -> [buildServiceStatusDTO]: built services status DTO: [{}] for deployment plan id: [{}] for deployment service id: [{}]", serviceStatusDTO, deploymentPlanId, deploymentServiceId);
        return serviceStatusDTO;

    }

    @Override
    public StatusDTO buildStatusDTO(int deploymentPlanId)
    {
        log.debug("[Deployment API] -> [buildStatusDTO]: building DTO with services status of deployment plan id: [{}]", deploymentPlanId);
        StatusDTO statusDTO=null;

        // Retrieve the deployment plan.
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlan(deploymentPlanId);

        // Get the service numbers depends of deployment mode
        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            statusDTO = this.etherManagerClient.getDeploymentStatus(this.deploymentUtils.buildEtherDeploymentDTO(deploymentPlan, "", ""));
        }
        else
        {
            statusDTO = this.deploymentManagerClient.getDeploymentPlanServicesStatus(deploymentPlanId);
        }

        log.debug("[Deployment API] -> [buildStatusDTO]: built services status DTO: [{}] of deployment plan id: [{}]", statusDTO, deploymentPlanId);

        return statusDTO;
    }

    @Override
    public SubsystemServicesStatusDTO buildSubsystemServiceDTO(final DeploymentPlan deploymentPlan, int deploymentSubsystemId, final boolean isOrchestrationHealthy)
    {
        final Integer deploymentPlanId = deploymentPlan.getId();
        SubsystemServicesStatusDTO subsystemServicesStatusDTO = new SubsystemServicesStatusDTO();
        log.debug("[Deployment API] -> [buildSubsystemServiceDTO]: building DTO with subsystem service status of deployment Subsystem id: [{}], deployment plan id: [{}]", deploymentSubsystemId, deploymentPlanId);

        DeploymentSubsystem deploymentSubsystem = this.deploymentsValidator.validateAndGetDeploymentSubsystem(deploymentSubsystemId);

        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            SubsystemServicesStatusDTO[] subsystemServicesStatusDTOArray = null;

            if (!ACTION_NOT_ALLOWED.contains(deploymentPlan.getAction()))
            {
                subsystemServicesStatusDTOArray =
                        this.deploymentUtils.buildSubsystemServicesStatus(this.etherManagerClient.getSubsystemStatus(new EtherSubsystemDTO[]{this.deploymentUtils.buildEtherSubsystemDTO(deploymentSubsystem)}));
            }
            if(subsystemServicesStatusDTOArray == null || subsystemServicesStatusDTOArray.length == 0)
            {
                log.warn("[Deployment API] -> [buildSubsystemServiceDTO]: the subsystem services DTO array length is 0 for Ether. "
                                + "Not found a subsystem services status for deployment id: [{}] and deployment subsystem id: [{}]",
                        deploymentPlanId, deploymentSubsystemId);
            }
            else
            {
                subsystemServicesStatusDTO = subsystemServicesStatusDTOArray[0];
            }
        }
        else
        {
            // If subsystem DTO arrays == 0, there is a warning
            int [] deploymentSubsystemIdArray = new int[]{deploymentSubsystemId};
            log.debug("[Deployment API] -> [buildSubsystemServiceDTO]: built a int deployment subsystem id array: [{}]", deploymentSubsystemIdArray);

            SubsystemServicesStatusDTO[] subsystemServicesStatusDTOArray = this.getSubsystemServicesStatusDTO(deploymentPlanId, deploymentSubsystemIdArray, isOrchestrationHealthy);
            if (subsystemServicesStatusDTOArray.length == 0)
            {
                log.warn("[Deployment API] -> [buildSubsystemServiceDTO]: the subsystem services DTO array length is 0. Not found a subsystem services status for deployment id: [{}] and deployment subsystem id array: [{}]",
                        deploymentPlanId, deploymentSubsystemIdArray);
            }
            else
            {
                subsystemServicesStatusDTO = subsystemServicesStatusDTOArray[0];
            }
        }

        return subsystemServicesStatusDTO;
    }

    @Override
    public SubsystemServicesStatusDTO[] buildSubsystemsServiceStatusDTO(final DeploymentPlan deploymentPlan, final boolean isOrchestrationHealthy)
    {
        final Integer deploymentPlanId = deploymentPlan.getId();
        log.debug("[Deployment API] -> [buildSubsystemsServiceStatusDTO]: building DTO with all subsystem services status for deployment plan id [{}]", deploymentPlanId);
        SubsystemServicesStatusDTO[] subsystemServicesStatusDTOS;

        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            if (ACTION_NOT_ALLOWED.contains(deploymentPlan.getAction()))
            {
                subsystemServicesStatusDTOS = new SubsystemServicesStatusDTO[0];
            }
            else
            {
                subsystemServicesStatusDTOS = this.deploymentUtils.buildSubsystemServicesStatus(this.etherManagerClient.getSubsystemStatus(this.deploymentUtils.buildEtherSubsystemDTO(deploymentPlan)));
            }
        }
        else
        {
            subsystemServicesStatusDTOS = this.getSubsystemServicesStatusDTO(deploymentPlanId, new int[deploymentPlan.getDeploymentSubsystems().size()], isOrchestrationHealthy);
        }

        log.debug("[Deployment API] -> [buildSubsystemsServiceStatusDTO]: building DTO with all subsystem services status for deployment plan id [{}]", deploymentPlanId);
        return subsystemServicesStatusDTOS;
    }

    ///////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * Get a subsystem services status DTO
     *
     * @param deploymentId               the deployment id
     * @param deploymentSubsystemIdArray an array of deployment subsystem ids
     * @param isOrchestrationHealthy     if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return the array of subsystem services status DTO
     */
    private SubsystemServicesStatusDTO[] getSubsystemServicesStatusDTO(int deploymentId, int[] deploymentSubsystemIdArray, final boolean isOrchestrationHealthy)
    {
        log.debug("[Deployment API] -> [getSubsystemServicesStatusDTO]: building a subsystem services status DTO array for deployment subsystem ids arrays: [{}] of the deployment plan id: [{}]", Arrays.toString(deploymentSubsystemIdArray), deploymentId);

        DeploySubsystemStatusDTO[] deployStatusDTOArray;
        if (isOrchestrationHealthy)
        {
            deployStatusDTOArray = this.deploymentManagerClient.getDeploymentSubsystemServicesStatus(deploymentSubsystemIdArray);
            log.debug("[Deployment API] -> [getSubsystemServicesStatusDTO]: built the deployment status DTO array: [{}]", Arrays.toString(deployStatusDTOArray));
        }
        else
        {
            deployStatusDTOArray = new DeploySubsystemStatusDTO[0];
            log.warn("[Deployment API] -> [getSubsystemServicesStatusDTO]: the cluster orchestration is unhealthy. Return and empty DeploySubsystemStatusDTO array: [{}]", Arrays.toString(deployStatusDTOArray));
        }

        SubsystemServicesStatusDTO[] subsystemServicesStatusDTOArray = this.deploymentUtils.buildSubsystemServicesStatus(deployStatusDTOArray);
        log.debug("[Deployment API] -> [getSubsystemServicesStatusDTO]: built a subsystem services status DTO array: [{}]", Arrays.toString(subsystemServicesStatusDTOArray));

        return subsystemServicesStatusDTOArray;
    }
}
