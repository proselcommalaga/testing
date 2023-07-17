package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeployerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerCallbackApiService;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service of  Listener DeploymentManagerCallbackAPI
 */
@Slf4j
@Service
public class DeploymentManagerCallbackApiServiceImpl implements IDeploymentManagerCallbackApiService
{
    /**
     * Deploy service
     */
    private final IDeployerService deployerService;

    /**
     * Budget service
     */
    private final IBudgetsService budgetsService;

    /**
     * ToDoTask service client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * ConfigurationManager client
     */
    private final ConfigurationmanagerClient configurationmanagerClient;

    /**
     * Repository of DeploymentService
     */
    private final DeploymentServiceRepository deploymentServiceRepository;

    /**
     * Repository of DeploymentSubsystem
     */
    private final DeploymentSubsystemRepository deploymentSubsystemRepository;

    /**
     * Repository of DeploymentPlan
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Api Manager Service
     */
    private final IApiManagerService apiManagerService;

    /**
     * Error task manager
     */
    private final IErrorTaskManager errorTaskManager;

    /**
     * Constructor by params
     *
     * @param deployerService               Deploy service
     * @param budgetsService                budget service
     * @param todoTaskServiceClient         TodoTask service
     * @param configurationManagerClient    client of configurationManager
     * @param deploymentServiceRepository   repository of DeploymentService
     * @param deploymentSubsystemRepository repository of DeploymentSubsystem
     * @param deploymentPlanRepository      repository of deploymentPlan
     * @param apiManagerService             api manager service
     * @param errorTaskManager              error task manager
     */
    @Autowired
    public DeploymentManagerCallbackApiServiceImpl(final IDeployerService deployerService, final IBudgetsService budgetsService,
                                                   final TodoTaskServiceClient todoTaskServiceClient, final ConfigurationmanagerClient configurationManagerClient,
                                                   final DeploymentServiceRepository deploymentServiceRepository, final DeploymentSubsystemRepository deploymentSubsystemRepository,
                                                   final DeploymentPlanRepository deploymentPlanRepository, final IApiManagerService apiManagerService,
                                                   final IErrorTaskManager errorTaskManager)
    {
        this.deployerService = deployerService;
        this.budgetsService = budgetsService;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.configurationmanagerClient = configurationManagerClient;
        this.deploymentServiceRepository = deploymentServiceRepository;
        this.deploymentSubsystemRepository = deploymentSubsystemRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.apiManagerService = apiManagerService;
        this.errorTaskManager = errorTaskManager;
    }


    @Override
    public void treatDeployService(final Integer serviceId)
    {
        log.debug("[deployService] -> Success response for services: {}", serviceId);

        Optional<DeploymentService> optional = deploymentServiceRepository.findById(serviceId);
        if (optional.isEmpty())
        {
            log.error("services with Id: {} not found!", serviceId);
        }
        else
        {
            DeploymentService service = optional.get();
            service.setAction(DeploymentAction.READY);
            deploymentServiceRepository.saveAndFlush(service);
            this.deployerService.updatePlanStatus(service);
        }
    }

    @Override
    public void treatDeployServiceError(final Integer deploymentServiceId)
    {
        // Printout the received parameter 'serviceId'
        log.error("[deployServiceError] -> Error response for service: {}", deploymentServiceId);
        DeploymentService service = deploymentServiceRepository.findById(deploymentServiceId)
                .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(deploymentServiceId),
                        "Error in callback of deployServiceError, Service [" + deploymentServiceId + "] not found"));
        service.setAction(DeploymentAction.ERROR);
        deploymentServiceRepository.saveAndFlush(service);
        this.deployerService.updatePlanStatus(service);
    }

    @Override
    public void treatDeploySubsystem(final Integer deploymentSubsystemId)
    {
        // Printout the received parameter 'subsystemId'
        log.debug("[deploySubsystem] -> Success response for subsystem: {}", deploymentSubsystemId);

        DeploymentSubsystem subsystem = deploymentSubsystemRepository.findById(deploymentSubsystemId)
                .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(deploymentSubsystemId),
                        "Error in callback of deploySubsystem, Subsystem [" + deploymentSubsystemId + "] not found"));
        subsystem.setAction(DeploymentAction.READY);
        deploymentSubsystemRepository.saveAndFlush(subsystem);
        this.deployerService.updatePlanStatus(subsystem);
    }

    @Override
    public void treatDeploySubsystemError(final Integer deploymentSubsystemId)
    {
        // Printout the received parameter 'subsystemId'
        log.error("[deploySubsystemError] -> Error response for subsystem: {}", deploymentSubsystemId);

        DeploymentSubsystem subsystem = deploymentSubsystemRepository.findById(deploymentSubsystemId)
                .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(deploymentSubsystemId),
                        "Error in callback of deploySubsystemError, Subsystem [" + deploymentSubsystemId + "] not found"));

        subsystem.setAction(DeploymentAction.ERROR);
        deploymentSubsystemRepository.saveAndFlush(subsystem);
        this.deployerService.updatePlanStatus(subsystem);
    }

    @Override
    public void treatReplacePlan(final Integer planId, final Integer newPlanId)
    {
        // Printout the received parameter 'planId'
        log.debug("[replacePlan] -> Success response for plan {} and  new plan {}", planId, newPlanId);
        budgetsService.synchronizePlanUndeployment(planId);
        budgetsService.synchronizePlanDeployment(newPlanId);
        this.apiManagerService.refreshUndeployedApiVersionsState(planId);
        this.apiManagerService.refreshDeployedApiVersionsState(newPlanId);
    }

    @Override
    public void treatRemovePlan(final Integer deploymentPlanId, final String userCode)
    {
        DeploymentPlan deploymentPlan = null;
        try
        {
            // Printout the received parameter 'planId'
            log.debug("[removePlan] -> Success response for deploymentPlan: {}", deploymentPlanId);

            // Find deploymentPlan.
            deploymentPlan = this.deploymentPlanRepository.findById(deploymentPlanId)
                    .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(deploymentPlanId),
                            "Error in callback of removePlan, deploymentPlan [" + deploymentPlanId + "] not found"));

            // Check the budgets service
            this.budgetsService.synchronizePlanUndeployment(deploymentPlanId);

            // Finally remove all the properties of the configuration revision (template and connectors) from Config Server data base
            if (!this.configurationmanagerClient.deleteCurrentConfigurationRevision(deploymentPlan))
            {
                // Create the to do task, log the error and
                String todoTaskDescription = "[DeploymentsAPI] -> [treatRemovePlan]: there was an error trying to delete the properties of the current configuration revision."
                        + java.lang.System.getProperty("line.separator")
                        + " - [Configuration Revision id]: " + deploymentPlan.getCurrentRevision().getId()
                        + java.lang.System.getProperty("line.separator")
                        + " - [Deployment Plan id]: " + deploymentPlan.getId()
                        + java.lang.System.getProperty("line.separator")
                        + " - [Product name]: " + deploymentPlan.getReleaseVersion().getRelease().getProduct().getName()
                        + java.lang.System.getProperty("line.separator")
                        + "Review the Configuration manger service status and ckeck if all instances is alive and running.";

                this.todoTaskServiceClient.createGenericTask(Constants.IMMUSER, null, ToDoTaskType.DELETION_PROPERTIES_FROM_CONFIG_SERVER_ERROR.name(),
                        RoleType.PLATFORM_ADMIN.name(), todoTaskDescription, deploymentPlan.getReleaseVersion().getRelease().getProduct().getId());
            }

            this.apiManagerService.refreshUndeployedApiVersionsState(deploymentPlanId);
        }
        catch (NovaException e)
        {
            if (deploymentPlan == null)
            {
                this.errorTaskManager.createGenericTask(null, e.getNovaError().toString(), ToDoTaskType.INTERNAL_ERROR, userCode, deploymentPlanId);
            }
            else
            {
                this.errorTaskManager.createErrorTask(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), e.getErrorCode(), e.getNovaError().getErrorMessage(), ToDoTaskType.UNDEPLOY_ERROR, e, userCode, deploymentPlanId);
            }
            throw e;
        }
    }

    @Override
    public void treatPromotePlanError(final Integer deploymentPlanId)
    {
        // Printout the received parameter 'planId'
        log.debug("[treatPromotePlanError] -> Error response for deploymentPlan: {}", deploymentPlanId);

        // Find deploymentPlan.
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(deploymentPlanId),
                        "Error in callback of removePlan, deploymentPlan [" + deploymentPlanId + "] not found"));
        deploymentPlan.setStatus(DeploymentStatus.REJECTED);
        deploymentPlan.setRejectionMessage("The deployment plan has failed trying to promote the plan. Copy this deployment plan and try it again");

        deploymentPlan.setAction(DeploymentAction.ERROR);
        this.deploymentPlanRepository.save(deploymentPlan);
    }

    @Override
    public void treatRemovePlanError(final Integer deploymentPlanId, final String userCode)
    {
        DeploymentPlan deploymentPlan = null;

        try
        {
            deploymentPlan = this.deploymentPlanRepository.findById(deploymentPlanId).orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(deploymentPlanId),
                    "Error in callback of removePlanError method for deploymentPlan id: [" + deploymentPlanId + "]. This deployment plan id does not found"));
        }
        catch (NovaException e)
        {
            this.errorTaskManager.createGenericTask(null, e.getNovaError().toString(), ToDoTaskType.INTERNAL_ERROR, userCode, deploymentPlanId);
        }
        finally
        {
            if (deploymentPlan == null)
            {
                log.debug("[DeploymentManagerCallbackApiServiceImpl] -> [treatRemovePlanError]: finished calling to treat remove plan error for deployment plan id: [{}]", deploymentPlanId);
            }
            else
            {
                this.errorTaskManager.createGenericTask(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), "The deployment plan cannot been undeployed. " +
                        "Please, try it again if the issues continues, contact with to NOVA Admin team via JIRA:PNOVA", ToDoTaskType.UNDEPLOY_ERROR, userCode, deploymentPlanId);
            }
        }
    }
}
