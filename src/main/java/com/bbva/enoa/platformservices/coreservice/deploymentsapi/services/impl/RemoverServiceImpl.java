package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.UserValidationService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRemoverService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IServiceRunner;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Remover service
 *
 * @author XE56809
 */
@Slf4j
@Service
public class RemoverServiceImpl implements IRemoverService
{
    /**
     * Deployment plan repository
     */

    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Deployment manager client
     */
    private final IDeploymentManagerClient deploymentManagerClient;

    /**
     * Todotask client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Deployment change repository
     */
    private final DeploymentChangeRepository changeRepository;

    /**
     * Ether manager service client
     */
    private final IEtherManagerClient iEtherManagerClient;

    /**
     * Scheduler manager service client
     */
    private final ISchedulerManagerClient schedulerManagerClient;

    /**
     * Deployment utils service
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * Users Client
     */
    private final IUsersClient usersClient;

    /**
     * Users Utils
     */
    private final UserValidationService userValidationService;

    /**
     * ApiGW service
     */
    private final IApiGatewayService apiGatewayService;

    /**
     * Profiling utils
     */
    private final ProfilingUtils profilingUtils;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Service runner needed to stopping batch scheduler instances
     */
    private final IServiceRunner serviceRunner;

    @Autowired
    public RemoverServiceImpl(final DeploymentPlanRepository deploymentPlanRepository,
                              final IDeploymentManagerClient deploymentManagerClient,
                              final TodoTaskServiceClient todoTaskServiceClient,
                              final DeploymentChangeRepository changeRepository,
                              final IEtherManagerClient iEtherManagerClient,
                              final ISchedulerManagerClient schedulerManagerClient,
                              final DeploymentUtils deploymentUtils,
                              final IUsersClient usersClient,
                              final UserValidationService userValidationService,
                              final IApiGatewayService apiGatewayService,
                              final ProfilingUtils profilingUtils,
                              final INovaActivityEmitter novaActivityEmitter,
                              final IServiceRunner serviceRunner)
    {
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.deploymentManagerClient = deploymentManagerClient;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.changeRepository = changeRepository;
        this.iEtherManagerClient = iEtherManagerClient;
        this.schedulerManagerClient = schedulerManagerClient;
        this.deploymentUtils = deploymentUtils;
        this.usersClient = usersClient;
        this.userValidationService = userValidationService;
        this.apiGatewayService = apiGatewayService;
        this.profilingUtils = profilingUtils;
        this.novaActivityEmitter = novaActivityEmitter;
        this.serviceRunner = serviceRunner;
    }

    @Override
    public TodoTaskResponseDTO undeployPlanOnEnvironment(final String ivUser, final Integer deploymentPlanId) throws NovaException
    {
        log.debug("Removing plan with Id: {}", deploymentPlanId);

        // Find plan.
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "Deployment plan with id [" + deploymentPlanId + "] does not exist"));

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Behaviour depends on environment.
        switch (Environment.valueOf(plan.getEnvironment()))
        {
            case INT:
                // INT: direct undeployPlan.
                this.undeployPlan(plan, ivUser);
                break;
            case PRE:
                // PRE: check autodeploy, could create task to SQA.
                todoTaskResponseDTO = this.undeployPlanInPre(ivUser, plan);
                break;
            // PRO: check autodeploy, could create task to PS or SS.
            case PRO:
                todoTaskResponseDTO = this.undeployPlanInPro(ivUser, plan);
                break;
        }

        return todoTaskResponseDTO;
    }

    @Override
    @Transactional
    public void undeployPlan(final DeploymentPlan deploymentPlan, final String ivUser) throws NovaException
    {
        // check if there are running batch scheduler instances
        List<DeploymentBatchScheduleInstanceDTO> runningBatchSchedulerInstances = new ArrayList<>();

        deploymentPlan.getReleaseVersion().getSubsystems()
                .forEach(subsystem -> subsystem.getServices().stream()
                        .filter(service -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getServiceType()))
                        .forEach(batchScheduleService ->
                        {
                            log.debug("[RemoverService] -> [undeployPlan]: checking running batch scheduler instances for relVerServiceId: [{}] - release version service name: [{}]", batchScheduleService.getId(), batchScheduleService.getServiceName());
                            DeploymentBatchScheduleInstanceDTO[] instances = this.schedulerManagerClient.getDeploymentBatchScheduleInstances(batchScheduleService, deploymentPlan);
                            log.debug("[RemoverService] -> [undeployPlan]: checking instances: [{}]", Arrays.toString(instances));

                            runningBatchSchedulerInstances.addAll(
                                    Arrays.stream(instances)
                                            .filter(instance -> "RUNNING".equals(instance.getState()))
                                            .collect(Collectors.toList()));
                        })
                );

        log.info("[RemoverServiceImpl] -> [undeployPlan]: there is the following batch schedule instance in RUNNING status: [{}] for deployment plan id: [{}] to undeploy", runningBatchSchedulerInstances.size(), deploymentPlan.getId());

        // Stop all batch scheduler instances that are running before undeploying deployment plan (if mandatory to be able to undeploy the deployment plan)
        for (DeploymentBatchScheduleInstanceDTO deploymentBatchScheduleInstanceDTO : runningBatchSchedulerInstances)
        {
            log.info("[RemoverServiceImpl] -> [undeployPlan]: stopping batch deployment instance: [{}]", deploymentBatchScheduleInstanceDTO);
            this.serviceRunner.stopBatchScheduleInstance(ivUser, deploymentBatchScheduleInstanceDTO.getId());
            log.info("[RemoverServiceImpl] -> [undeployPlan]: stopped batch deployment instance: [{}]", deploymentBatchScheduleInstanceDTO);
        }

        // Undeploy the deployment plan
        log.debug("[RemoverServiceImpl] -> [undeployPlan]: removing deployment plan id: [{}]", deploymentPlan.getId());
        if (this.undeployPlanByDeploymentMode(deploymentPlan))
        {
            // The API's publication must be removed if applies
            if (this.profilingUtils.isPlanContainingServicesWithMgw(deploymentPlan))
            {
                this.apiGatewayService.removePublication(deploymentPlan);
            }

            // Remove deployment batch and context params for batch scheduler
            this.undeployDeploymentBatchSchedule(deploymentPlan);

            // Reject all pending todotask
            this.deploymentUtils.rejectPlanPendingTask(deploymentPlan);

            // Add an entry to the plan history.
            DeploymentChange change = new DeploymentChange(deploymentPlan, ChangeType.REMOVE_PLAN, "El plan de despliegue ha sido replegado satisfacotiamente");
            change.setUserCode(ivUser);
            deploymentPlan.getChanges().add(change);
            changeRepository.saveAndFlush(change);
        }

        // Emit Undeploy Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.RETRACTED)
                .entityId(deploymentPlan.getId())
                .environment(deploymentPlan.getEnvironment())
                .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                .addParam("deploymentstatus", deploymentPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .build());
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO undeployPlanInPre(String ivUser, DeploymentPlan plan)
    {
        log.debug("[{}] -> [undeployPlanInPre]: Undeploying plan {} from environment {}", this.getClass().getSimpleName(), plan.getId(), plan.getEnvironment());

        Product product = plan.getReleaseVersion().getRelease().getProduct();

        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // If plan's release has active auto deploy in PRO OR (User is SS or PS or PAdmin) then undeploy without TodoTask
        List<String> sqaUserCodes = userValidationService.getUserCodesByTeam(RoleType.SQA_ADMIN);
        if (plan.getReleaseVersion().getRelease().isEnabledAutodeployInPre() ||
                (usersClient.isPlatformAdmin(ivUser) || sqaUserCodes.contains(ivUser.toUpperCase())))
        {
            log.debug("[{}] -> [undeployPlanInPre]: Release {} from plan {} has enabled auto deploy in {}, proceeding to execute undeployment",
                    this.getClass().getSimpleName(), plan.getReleaseVersion().getRelease().getName(), plan.getId(), plan.getEnvironment());
            this.undeployPlan(plan, ivUser);

            // Emit Undeploy Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(product.getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.RETRACTED)
                    .entityId(plan.getId())
                    .environment(plan.getEnvironment())
                    .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                    .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                    .build());
        }
        else
        {
            log.debug("[{}] -> [undeployPlanInPre]: Creating undeployment request of plan {} to SQA", this.getClass().getSimpleName(), plan.getId());

            // Create a task requesting un deployment to Service Support.
            String taskDescription = "El usuario " + ivUser + " solicita replegar en PRE el plan de despliegue " + plan.getId() + " de la release " + plan.getReleaseVersion().getRelease().getName() + " " +
                    plan.getReleaseVersion().getVersionName() + " del producto " + product.getName();

            todoTaskResponseDTO = this.createTask(ivUser, plan, ToDoTaskType.UNDEPLOY_PRE, taskDescription, RoleType.SQA_ADMIN);

            if (todoTaskResponseDTO.getGenerated())
            {
                log.debug("Production task for undeploy plan in Pre created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Undeploy Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_RETRACTED)
                        .entityId(plan.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                        .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("TodoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }

        return todoTaskResponseDTO;
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO undeployPlanInPro(final String ivUser, final DeploymentPlan plan) throws NovaException
    {
        log.debug("Undeploying plan {} from environment {}", plan.getId(), plan.getEnvironment());

        Product product = plan.getReleaseVersion().getRelease().getProduct();

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // If the user belongs to Service Support, or is Platform Admin, then undeploy without ToDoTask
        List<String> serviceUserCodes = userValidationService.getUserCodesByTeam(RoleType.SERVICE_SUPPORT);
        if (usersClient.isPlatformAdmin(ivUser) || serviceUserCodes.contains(ivUser.toUpperCase()))
        {
            log.debug("User [{}] belongs to Service Support or is Platform Admin", ivUser);
            this.undeployPlan(plan, ivUser);

            // Emit Undeploy Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(product.getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.RETRACTED)
                    .entityId(plan.getId())
                    .environment(plan.getEnvironment())
                    .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                    .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                    .build());
        }
        // Otherwise, generate a ToDoTask
        else
        {
            String taskDescription = String.format("El usuario %s solicita replegar en PRO el plan de despliegue %s de la release %s %s del producto %s",
                    ivUser, plan.getId(), plan.getReleaseVersion().getRelease().getName(), plan.getReleaseVersion().getVersionName(), product.getName());
            // If Autodeploy in PRO is active, then generate the ToDoTask to the Product Owners
            if (plan.getReleaseVersion().getRelease().isEnabledAutodeployInPro())
            {
                log.debug("User [{}] does not belong to Service Support neither is Platform Admin, but Autodeploy in PRO is active for the release [{}] of the plan [{}]",
                        ivUser, plan.getReleaseVersion().getRelease().getName(), plan.getId());
                todoTaskResponseDTO = this.createTask(ivUser, plan, ToDoTaskType.UNDEPLOY_PRO, taskDescription, RoleType.PRODUCT_OWNER);
            }
            // If Autodeploy in PRO is not active, then generate the ToDoTask to Service Support or Project Support
            else
            {
                log.debug("User [{}] does not belong to Service Support, neither is Platform Admin, and Autodeploy in PRO is not active for the release [{}] of the plan [{}]",
                        ivUser, plan.getReleaseVersion().getRelease().getName(), plan.getId());

                RoleType taskDestination = RoleType.SERVICE_SUPPORT;
                todoTaskResponseDTO = this.createTask(ivUser, plan, ToDoTaskType.UNDEPLOY_PRO, taskDescription, taskDestination);
            }

            if (todoTaskResponseDTO.getGenerated())
            {
                // Emit Send Request Undeploy Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_RETRACTED)
                        .entityId(plan.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                        .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("TodoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());

                log.debug("Production task for undeploy plan in Pro created with id {}", todoTaskResponseDTO.getTodoTaskId());
            }
        }

        return todoTaskResponseDTO;
    }

    private void undeployDeploymentBatchSchedule(final DeploymentPlan deploymentPlan)
    {
        deploymentPlan.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.updateDeploymentBatchSchedule(releaseVersionService.getId(), deploymentPlan.getId(),
                        DeploymentBatchScheduleStatus.UNDEPLOYED));
    }

    private boolean undeployPlanByDeploymentMode(DeploymentPlan plan)
    {
        if (PlatformUtils.isPlanDeployedInEther(plan))
        {
            // Call remove plan to Ether Manager
            return this.iEtherManagerClient.removeEtherPlan(this.deploymentUtils.buildEtherDeploymentDTOByUrl(plan, CallbackService.CALLBACK_DELETE_PLAN + plan.getId(), CallbackService.REMOVE_ERROR));
        }
        else
        {
            // Call remove plan to Deployment Manager
            return this.deploymentManagerClient.removePlan(plan);
        }
    }

    /**
     * Creates a {@link DeploymentTask} requesting undeployment of a {@link DeploymentPlan},
     *
     * @param ivUser          User code.
     * @param deploymentPlan  {@link DeploymentPlan} to undeployPlan.
     * @param taskType        Task type.
     * @param taskDescription Task description.
     * @return A {@link DeploymentTask} ID.
     */
    private TodoTaskResponseDTO createTask(final String ivUser, final DeploymentPlan deploymentPlan, final ToDoTaskType taskType, final String taskDescription, final RoleType roleType)
    {
        // Check if there is already a task requesting undeployment.
        boolean hasPendingTask = deploymentPlanRepository.planHasPendingDeploymentRequestOnEnvTask(deploymentPlan.getId(), taskType);

        // Only one is allowed.
        if (hasPendingTask)
        {
            log.debug("Tried to create task requesting undeployment of deploymentPlan {} on environment {} while there is yet an open task for that", deploymentPlan.getId(), deploymentPlan.getEnvironment());
            throw new NovaException(DeploymentError.getUndeploymentRequestPendingError());
        }
        // Create the task.
        Integer taskId = todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, taskType.toString(), roleType.name(), taskDescription, deploymentPlan.getReleaseVersion().getRelease().getProduct());

        return this.deploymentUtils.builderTodoTaskResponseDTO(true, roleType, taskId, taskType);
    }
}
