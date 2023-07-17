package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TaskRequestDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentGcspRepo;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentNovaRepo;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentTypeChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ToDoTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.alertservice.DeploymentScheduleDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeployerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRemoverService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.ITaskProcessor;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IServiceRunner;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * NOTAS:
 * ToDoTaskStatus = PENDING | PENDING_ERROR |  REJECTED | DONE | ERROR
 *
 * @author xe30000
 */
@Service
@Slf4j
public class TaskProcessorImpl implements ITaskProcessor
{
    /**
     * Max length of column 'description' and 'status rejected' on to do task entity and deployment plan entity
     */
    private static final int DATA_BASE_COLUMN_MAX_LENGTH = 8000;
    /**
     * Client of ToDoTask serive
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Deployer service
     */
    private final IDeployerService deployerService;

    /**
     * Remover service
     */
    private final IRemoverService removerService;

    /**
     * Service runner
     */
    private final IServiceRunner serviceRunner;

    /**
     * DeploymentsValidator
     */
    private final IDeploymentsValidator deploymentsValidator;

    /**
     * Repository of DeploymentTypeChange
     */
    private final DeploymentTypeChangeRepository typeChangeRepository;

    /**
     * repository of deploymenttask
     */
    private final DeploymentTaskRepository deploymentTaskRepository;

    /**
     * repository ToDoTask
     */
    private final ToDoTaskRepository toDoTaskRepository;

    /**
     * Repository of DeploymentChange
     */
    private final DeploymentChangeRepository changeRepository;

    /**
     * Repository of DeploymentPlan
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Repository of ManagementAction
     */
    private final ManagementActionTaskRepository managementActionTaskRepository;

    /**
     * Repository of DeploymentGCSP
     */
    private final DeploymentGcspRepo deploymentGcspRepo;

    /**
     * Repository of DeploymentNova
     */
    private final DeploymentNovaRepo deploymentNovaRepo;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Alert service client
     */
    private final IAlertServiceApiClient alertServiceApiClient;

    /**
     * Error task manager
     */
    private final IErrorTaskManager errorTaskManager;

    /**
     * Constructor by params
     *
     * @param todoTaskServiceClient          client for TODOTask service
     * @param deployerService                deployer
     * @param removerService                 remover
     * @param serviceRunner                  runner
     * @param deploymentsValidator           validator for DeploymentPlan
     * @param typeChangeRepository           repository of TypeChange
     * @param deploymentTaskRepository       repository of deploymentTask
     * @param toDoTaskRepository             repository of ToDoTask
     * @param changeRepository               repository of Change
     * @param deploymentPlanRepository       repository of deploymentPlan
     * @param managementActionTaskRepository repository of ManagementActionTask
     * @param deploymentGcspRepo             Repository of DeploymentGCSP
     * @param deploymentNovaRepo             Repository of DeploymentNOVA
     * @param novaActivityEmitter            activity emitter
     * @param alertServiceApiClient          alert service client
     */
    @Autowired
    public TaskProcessorImpl(final TodoTaskServiceClient todoTaskServiceClient, final IDeployerService deployerService, final IRemoverService removerService, final IServiceRunner serviceRunner, final IDeploymentsValidator deploymentsValidator, final DeploymentTypeChangeRepository typeChangeRepository, final DeploymentTaskRepository deploymentTaskRepository, final ToDoTaskRepository toDoTaskRepository, final DeploymentChangeRepository changeRepository, final DeploymentPlanRepository deploymentPlanRepository, final ManagementActionTaskRepository managementActionTaskRepository, final DeploymentGcspRepo deploymentGcspRepo, final DeploymentNovaRepo deploymentNovaRepo, final INovaActivityEmitter novaActivityEmitter, final IAlertServiceApiClient alertServiceApiClient, final IErrorTaskManager errorTaskManager)
    {
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.deployerService = deployerService;
        this.removerService = removerService;
        this.serviceRunner = serviceRunner;
        this.deploymentsValidator = deploymentsValidator;
        this.typeChangeRepository = typeChangeRepository;
        this.deploymentTaskRepository = deploymentTaskRepository;
        this.toDoTaskRepository = toDoTaskRepository;
        this.changeRepository = changeRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.managementActionTaskRepository = managementActionTaskRepository;
        this.deploymentGcspRepo = deploymentGcspRepo;
        this.deploymentNovaRepo = deploymentNovaRepo;
        this.novaActivityEmitter = novaActivityEmitter;
        this.alertServiceApiClient = alertServiceApiClient;
        this.errorTaskManager = errorTaskManager;
    }

    /////////////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////

    @Override
    public void createConfigManagementTask(final TaskRequestDTO[] taskRequestDtoArray, final String ivUser, final DeploymentPlan deploymentPlan)
    {
        for (TaskRequestDTO taskRequestDTO : taskRequestDtoArray)
        {
            log.debug("[TaskProcessorImpl] -> [createConfigManagementTask]: Creating todo task [{}] for plan with id [{}] by iv-user: [{}]", taskRequestDTO.getManagement(), deploymentPlan.getId(), ivUser);
            this.checkVarsType(taskRequestDTO, deploymentPlan, this.checkAndGetDeploymentTask(taskRequestDTO.getTypeTask()));
        }
    }

    @Override
    public void onTaskReply(final DeploymentPlan deploymentPlan, final Integer todoTaskId, final String todoTaskStatus)
    {
        Optional<DeploymentTask> optional = deploymentTaskRepository.findById(todoTaskId);
        if (optional.isEmpty())
        {
            log.error("[TaskProcessorImpl] -> [onTaskReply]: the to do task with id: [{}] does not found of deployment plan id: [{}] and to do task status: [{}]. Not processed.", todoTaskId, deploymentPlan.getId(), todoTaskStatus);
            return;
        }
        DeploymentTask deploymentTask = optional.get();
        ToDoTaskStatus toDoTaskStatus = ToDoTaskStatus.valueOf(todoTaskStatus);
        log.debug("[TaskProcessorImpl] -> [onTaskReply]: processing deployment task: [{}] with deployment to do task Id: [{}] - status: [{}] and type: [{}]]", deploymentTask, todoTaskId, todoTaskStatus, deploymentTask.getTaskType());

        switch (deploymentTask.getTaskType())
        {
            case CHECK_ENVIRONMENT_VARS:
                processCheckEnvironmentVarsTask(toDoTaskStatus);
                break;
            // Request of deployment on PRE and PRO:
            case DEPLOY_PRE:
            case DEPLOY_PRO:
                this.closeDeployOnEnvTask(deploymentPlan, deploymentTask, toDoTaskStatus);
                break;
            case UNDEPLOY_PRE:
            case UNDEPLOY_PRO:
                this.closeUndeployOnEnvTask(deploymentPlan, deploymentTask, toDoTaskStatus);
                break;
            case DEPLOYMENT_TYPE_CHANGE:
                this.closeDeploymentTypeChangeTask(deploymentTask, toDoTaskStatus);
                break;
            case SCHEDULE_PLANNING:
                this.closeSchedulePlanningTask(deploymentPlan, deploymentTask, toDoTaskStatus);
                break;
            case NOVA_PLANNING:
                this.closeNovaPlanningTask(deploymentPlan, deploymentTask, toDoTaskStatus);
                break;
            case APPROVE_PLAN_PROFILING:
                // This to do task has been deprecated: CIBNOVAP-1489: The ProfileOfficeTask won't be created as it is not required
                log.debug("[TaskProcessorImpl] -> [onTaskReply]: Not processed. The Profile Office Task won't be create due to it is currently not required. Review CIBNOVAP-1489");
                break;
            default:
                log.error("[TaskProcessorImpl] -> [onTaskReply]: error trying to process a deployment todo task id: [{}] of wrong / unknown to do task type: [{}]. Not processed.", todoTaskId, deploymentTask.getTaskType());
        }
    }

    @Override
    public void addHistoryEntry(final ChangeType changeType, final DeploymentPlan deploymentPlan, final String userCode, final String changeMessage)
    {
        DeploymentChange change = new DeploymentChange(deploymentPlan, changeType, changeMessage);
        change.setUserCode(userCode);
        deploymentPlan.getChanges().add(change);
        this.changeRepository.saveAndFlush(change);

        log.debug("[TaskProcessorImpl] -> [addHistoryEntry]: added change type for deployment plan id: [{}] of type: [{}] - user code: [{}] and message: [{}]", deploymentPlan.getId(), changeType, userCode, changeMessage);
    }

    @Override
    public void onTaskReply(final Integer todoTaskId, final String todoTaskStatus) throws Errors
    {
        Optional<ToDoTask> optional = toDoTaskRepository.findById(todoTaskId);
        if (optional.isEmpty())
        {
            log.error("[TaskProcessorImpl] -> [onTaskReply]: the to do task with id: [{}] does not with the to do task status: [{}]. Not processed.", todoTaskId, todoTaskStatus);
        }
        else
        {
            ToDoTask toDoTask = optional.get();
            ToDoTaskStatus toDoTaskStatus = ToDoTaskStatus.valueOf(todoTaskStatus);
            log.debug("[TaskProcessorImpl] -> [onTaskReply]: processing deployment task: [{}] with to do task Id: [{}] - status: [{}] and type: [{}]]", toDoTask, todoTaskId, todoTaskStatus, toDoTask.getTaskType());

            switch (toDoTask.getTaskType())
            {
                case CONTAINER_START:
                case CONTAINER_RESTART:
                case CONTAINER_STOP:
                case SERVICE_START:
                case SERVICE_RESTART:
                case SERVICE_STOP:
                case SUBSYSTEM_START:
                case SUBSYSTEM_RESTART:
                case SUBSYSTEM_STOP:
                case RELEASE_START:
                case RELEASE_RESTART:
                case RELEASE_STOP:
                case BATCH_SCHEDULE_START:
                case BATCH_SCHEDULE_STOP:
                case BATCH_SCHEDULE_INSTANCE_START:
                case BATCH_SCHEDULE_INSTANCE_STOP:
                case BATCH_SCHEDULE_INSTANCE_PAUSE:
                case BATCH_SCHEDULE_INSTANCE_RESUME:
                    this.closeManagementTypeTask(toDoTask, toDoTaskStatus);
                    break;
                default:
                    log.error("[TaskProcessorImpl] -> [onTaskReply]: error trying to process a todo task id: [{}] of wrong / unknown to do task type: [{}]. Not processed.", todoTaskId, toDoTask.getTaskType());
            }
        }
    }

    //------------------------------------------------------
    //                   Private Method
    //------------------------------------------------------

    /**
     * Check environment vars type
     *
     * @param taskRequestDto taskRequestDto
     * @param deployment     deployment
     * @param taskType       taskType
     */
    private void checkVarsType(final TaskRequestDTO taskRequestDto, final DeploymentPlan deployment, final ToDoTaskType taskType)
    {
        if (ToDoTaskType.getConfigurationManagementTask().contains(taskType))
        {
            this.createConfigurationManagementTask(deployment, taskRequestDto);
        }
        else
        {
            log.error("[TaskProcessorImpl] -> [checkVarsType]: the todo task type unsupported for this action. ToDo Task Type: [{}]", taskType);
        }
    }

    /**
     * Check if it is actually a deployment task
     *
     * @param todoTaskType task type
     * @throws NovaException if error
     */
    private ToDoTaskType checkAndGetDeploymentTask(String todoTaskType) throws NovaException
    {
        if (!EnumUtils.isValidEnum(ToDoTaskType.class, todoTaskType) || !ToDoTaskType.valueOf(todoTaskType).isDeploymentTask())
        {
            throw new NovaException(DeploymentError.getWrongTaskTypeError(), "[TaskProcessorImpl] -> [checkAndGetDeploymentTask]: there was an error, the task type is not a deployment ToDoTask");
        }
        return ToDoTaskType.valueOf(todoTaskType);
    }

    /**
     * Processes the closing of a {@link DeploymentTask} requesting
     * deployment of a {@link DeploymentPlan} on an {@link Environment}.
     * Is it was accepted, the plan will be executed immediately.
     * If it was rejected, nothing will happen.
     *
     * @param deploymentTask {@link DeploymentTask}.
     */
    private void closeDeployOnEnvTask(final DeploymentPlan deploymentPlan, final DeploymentTask deploymentTask, final ToDoTaskStatus toDoTaskStatus)
    {
        log.debug("[TaskProcessorImpl] -> [closeDeployOnEnvTask]: closing deployment request deployment task: [{}]", deploymentTask);

        // If task was accepted:
        if (toDoTaskStatus == ToDoTaskStatus.DONE)
        {
            try
            {
                //check deployment date
                this.deploymentsValidator.checkDeploymentDate(deploymentPlan);

                // Launch real deployment and change plan status to DEPLOYED.
                log.info("[TaskProcessorImpl] -> [closeDeployOnEnvTask]: deployment task of type: [{}] accepted: [{}], launching deployment plan with id: [{}]", deploymentTask.getTaskType().name(), toDoTaskStatus.name(), deploymentTask.getDeploymentPlan().getId());
                deployerService.deploy(
                        deploymentTask.getCreationUserCode(),
                        deploymentTask.getDeploymentPlan(),
                        true
                );

            }
            catch (NovaException e)
            {
                log.error("[TaskProcessorImpl] -> [closeDeployOnEnvTask]: there was an error: [{}] for the deployment plan id: [{}] for deployment task type: [{}] - previously status: [{}]",
                        e.getNovaError().getErrorCode(), deploymentTask.getDeploymentPlan().getId(), deploymentTask.getTaskType().name(), toDoTaskStatus.name());

                // building the associated alert info with the current dto
                final var alertDTO = DeploymentScheduleDTO.builder()
                        .deploymentDate(LocalDateTime.now().toString())
                        .identifier(deploymentTask.getDeploymentPlan().getId())
                        .deploymentPlatform(deploymentTask.getDeploymentPlan().getSelectedDeploy().name())
                        .error(e.getMessage())
                        .environment(Environment.valueOf(deploymentTask.getDeploymentPlan().getEnvironment()))
                        .build();

                // calling to the api to store a new product alert
                this.alertServiceApiClient.registerProductAlert(alertDTO);

                // Change status to rejected
                deploymentPlan.setStatus(DeploymentStatus.REJECTED);
                String novaExceptionErrorMessage = e.getNovaError().getErrorMessage();

                if (novaExceptionErrorMessage.length() > DATA_BASE_COLUMN_MAX_LENGTH)
                {
                    novaExceptionErrorMessage = novaExceptionErrorMessage.substring(0, DATA_BASE_COLUMN_MAX_LENGTH);
                }

                // Set rejection message  any case
                deploymentPlan.setRejectionMessage(novaExceptionErrorMessage);
                log.warn("[TaskProcessorImpl] -> [closeDeployOnEnvTask]: the deployment plan id: [{}] has been: [{}}] due to rejection message: [{}]. A todo task error will be created ...", deploymentPlan.getId(), DeploymentStatus.REJECTED, novaExceptionErrorMessage);

                Integer todoTaskId = this.errorTaskManager.createGenericTask(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), novaExceptionErrorMessage, ToDoTaskType.DEPLOY_ERROR,
                        Constants.IMMUSER, deploymentPlan.getId(), null, RoleType.PLATFORM_ADMIN.getType());
                log.warn("[TaskProcessorImpl] -> [closeDeployOnEnvTask]: Todo task of type: [DEPLOY_ERROR] has been created with id: [{}]", todoTaskId);
            }
        }

        if (toDoTaskStatus == ToDoTaskStatus.REJECTED && (deploymentTask.getTaskType().equals(ToDoTaskType.SCHEDULE_PLANNING)
                || deploymentTask.getTaskType().equals(ToDoTaskType.NOVA_PLANNING) || deploymentTask.getTaskType().equals(ToDoTaskType.DEPLOY_PRO) || deploymentTask.getTaskType().equals(ToDoTaskType.DEPLOY_PRE)))
        {
            deploymentPlan.setStatus(DeploymentStatus.REJECTED);
            String errorMessage = "The TODO task associated with id: [" + deploymentTask.getId() + "] to deploy this plan is on status: [" + toDoTaskStatus.name() + "] due to closing motive: [" + deploymentTask.getClosingMotive() + "] " +
                    "and task status message: [" + deploymentTask.getStatusMessage() + "]";
            deploymentPlan.setRejectionMessage(errorMessage);

            this.deploymentPlanRepository.save(deploymentPlan);
            log.warn("[TaskProcessorImpl] -> [closeDeployOnEnvTask]: the deployment plan request with id: [{}] and deployment task id: [{}] - deployment task type: [{}] has been: [{}] due to: [{}]",
                    deploymentPlan.getId(), deploymentTask.getId(), deploymentTask.getTaskType(), DeploymentStatus.REJECTED, errorMessage);
        }
    }

    /**
     * Processes the closing of a {@link DeploymentTask} requesting
     * undeployment of a {@link DeploymentPlan} on an {@link Environment}.
     * If it was accepted, the plan will be undeployed immediately.
     * If it was rejected, nothing will happen.
     *
     * @param deploymentPlan deployment plan to deploy
     * @param deploymentTask {@link DeploymentTask}.
     * @param toDoTaskStatus status
     */
    private void closeUndeployOnEnvTask(final DeploymentPlan deploymentPlan, final DeploymentTask deploymentTask, final ToDoTaskStatus toDoTaskStatus)
    {
        // If task was accepted:
        if (toDoTaskStatus == ToDoTaskStatus.DONE)
        {
            // Get the gscp from deployment plan to undeploy
            DeploymentGcsp deploymentGcsp = this.deploymentGcspRepo.findFirstByUndeployRelease(deploymentPlan.getId());
            log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: Obtained the following deployment gscp: [{}] from deployment plan id to undeploy: [{}]", deploymentGcsp, deploymentPlan.getId());
            if (deploymentGcsp != null)
            {
                // Get the new deployment plan to deploy
                DeploymentPlan scheduledPlan = this.deploymentPlanRepository.findByGcsp(deploymentGcsp);
                log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]:Obtained the deployment plan to deploy: [{}] to check the date", scheduledPlan);

                // Validate if new scheduled plan can be deployed due to date
                this.deploymentsValidator.checkDeploymentDate(scheduledPlan);
                log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: The deployment plan id: [{}] is validated. Can be deployed. Continue.", scheduledPlan.getId());
            }

            // Get the nova from deployment plan to undeploy
            DeploymentNova deploymentNova = this.deploymentNovaRepo.findFirstByUndeployRelease(deploymentPlan.getId());
            log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: Obtained the following deployment nova: [{}] from deployment plan id to undeploy: [{}]", deploymentNova, deploymentPlan.getId());

            if (deploymentNova != null)
            {
                // Get the new deployment plan to deploy
                DeploymentPlan scheduledPlan = this.deploymentPlanRepository.findByNova(deploymentNova);
                log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]:Obtained the deployment plan to deploy: [{}] to check the date", scheduledPlan);
                // Validate if new scheduled plan can be deployed due to date
                this.deploymentsValidator.checkDeploymentDate(scheduledPlan);
                log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: The deployment plan id: [{}] is validated. Can be deployed. Continue.", scheduledPlan.getId());
            }

            // Launch real deployment and change plan status to DEPLOYED.
            log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: Launching un deployment of plan: [{}]", deploymentTask.getDeploymentPlan().getId());

            // Get the user code to assign the to-do task and remove
            String userCode = deploymentTask.getAssignedUserCode();
            log.debug("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: Remove the deployment plan from task: [{}] and requester: [{}]", deploymentTask, userCode);
            this.removerService.undeployPlan(deploymentTask.getDeploymentPlan(), userCode);
        }
        else
        {
            log.warn("[TaskProcessorImpl] -> [closeUndeployOnEnvTask]: The deployment plan id: [{}] will not be removed due to the deployment to do task: [{}] is NOT DONE:", deploymentPlan.getId(), deploymentTask);
        }
    }

    private void closeDeploymentTypeChangeTask(final DeploymentTask deploymentTask, final ToDoTaskStatus toDoTaskStatus)
    {
        DeploymentTypeChangeTask changeTask = typeChangeRepository.findById(deploymentTask.getId())
                .orElseThrow(() -> new NovaException(DeploymentError.getUnexpectedError()));
        // If task was accepted:
        if (toDoTaskStatus == ToDoTaskStatus.DONE)
        {
            // Get deployment plan
            DeploymentPlan deploymentPlan = changeTask.getDeploymentPlan();

            // Modify the type of plan.
            deploymentPlan.setDeploymentTypeInPro(changeTask.getDeploymentType());

            // Set the URL to the doc with the planning.
            deploymentPlan.setPlanningDocUrl(changeTask.getPlanningDocUrl());

            // And save the plan.
            deploymentPlanRepository.save(deploymentPlan);

            // Save history.
            this.addHistoryEntry(
                    ChangeType.DEPLOYMENT_TYPE_CHANGE,
                    deploymentTask.getDeploymentPlan(),
                    deploymentTask.getAssignedUserCode(),
                    "Se ha cambiado el tipo de despliegue del plan de despliegue a: [" + changeTask.getDeploymentType() + "]"
            );

            // Emit Deployment Type Configured Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYMENT_TYPE_CHANGE)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("deploymentTypeChange", changeTask.getDeploymentType())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("deploymentPlanStatus", deploymentPlan.getStatus().getDeploymentStatus())
                    .build());
        }
    }

    /**
     * @param toDoTask       - the TODoTask to close
     * @param toDoTaskStatus - the new status
     */
    private void closeManagementTypeTask(final ToDoTask toDoTask, final ToDoTaskStatus toDoTaskStatus) throws Errors
    {
        // If task was accepted:
        if (toDoTaskStatus == ToDoTaskStatus.DONE)
        {
            ManagementActionTask managementActionTask = managementActionTaskRepository.findById(toDoTask.getId()).orElseThrow(() -> new NovaException(DeploymentError.getUnexpectedError()));
            serviceRunner.processTask(managementActionTask);
        }
        else
        {
            log.warn("[TaskProcessorImpl] -> [closeManagementTypeTask]: the to do task id: [{}] - type: [{}] has been REJECTED: [{}]", toDoTask.getId(), toDoTask.getTaskType(), toDoTask.getClosingMotive());
        }
    }

    /**
     * Method to create a "CHECK_ENVIRONMENT_VARS"  task.
     *
     * @param deploymentPlan - Deployment plan of the task
     * @param taskRequestDTO - Description for the new task. Message for administrators.
     */
    private void createConfigurationManagementTask(final DeploymentPlan deploymentPlan, final TaskRequestDTO taskRequestDTO)
    {
        log.debug("[TaskProcessorImpl] -> [createConfigurationManagementTask]: creating a configuration management for the deployment Plan Id: [{}] - and message: [{}] )", deploymentPlan.getId(), taskRequestDTO);
        // Si ya tenemos una tarea de configuración pendiente de ejecutar rechazamos la nueva petición.
        this.checkConfigurationTask(deploymentPlan, taskRequestDTO);
        // Determinar el roleType en función de la tarea y el entorno
        RoleType roleType = this.getRole(taskRequestDTO.getManagement());
        // Crear la tarea
        Integer taskId = todoTaskServiceClient.createDeploymentTask(
                deploymentPlan.getId(),
                null,
                taskRequestDTO.getTypeTask(),
                roleType.name(),
                taskRequestDTO.getMessage(),
                deploymentPlan.getReleaseVersion().getRelease().getProduct());
        if (taskId != null)
        {
            this.setDeploymentData(deploymentPlan, taskId);
        }
    }

    /**
     * Set deployment data
     *
     * @param deploymentPlan deployment plan
     * @param todoTaskId     task id
     */
    private void setDeploymentData(final DeploymentPlan deploymentPlan, final Integer todoTaskId)
    {
        if (deploymentTaskRepository.existsById(todoTaskId))
        {
            // Get obtiene la referencia, Find devuelve el objeto completo.
            deploymentPlan.setConfigurationTask(deploymentTaskRepository.getOne(todoTaskId));
        }
    }

    /**
     * Get role depending on environment
     *
     * @param todoTaskType the to do task type
     * @return role
     */
    private RoleType getRole(final String todoTaskType)
    {
        RoleType roleType;
        if (ManagementType.ENVIRONMENT.name().equals(todoTaskType))
        {

            roleType = RoleType.SERVICE_SUPPORT;

        }
        else
        {
            roleType = RoleType.LIBRARY_ADMIN;
        }
        return roleType;
    }

    /**
     * Check configuration task
     *
     * @param deploymentPlan deployment plan
     */
    private void checkConfigurationTask(final DeploymentPlan deploymentPlan, final TaskRequestDTO taskRequestDTO)
    {
        if (!ToDoTaskType.CHECK_LIBRARY_VARS.name().equals(taskRequestDTO.getTypeTask()) && deploymentPlan.getConfigurationTask() != null &&
                (deploymentPlan.getConfigurationTask().getStatus() == ToDoTaskStatus.PENDING || deploymentPlan.getConfigurationTask().getStatus() == ToDoTaskStatus.PENDING_ERROR))
        {
            log.error("[TaskProcessorImpl] -> [checkConfigurationTask]: can not check configuration for task request DTO: [{}] of the deployment plan id: [{}]", taskRequestDTO, deploymentPlan.getId());
            throw new NovaException(DeploymentError.getAlreadyPendingTaskInCreateTaskError(),
                    "[TaskProcessorImpl] -> [checkConfigurationTask]: there is a previous Configuration TODO Task in PENDING or PENDING ERROR status to execute for deployment plan id: [" + deploymentPlan.getId() + "]");
        }
    }

    /**
     * Procesa el resultado de una tarea de configuración de variables de entorno.
     *
     * @param toDoTaskStatus - the new {@code ToDoTaskStatus}
     */
    private void processCheckEnvironmentVarsTask(final ToDoTaskStatus toDoTaskStatus)
    {
        // ToDoTaskStatus = PENDING | PENDING_ERROR |  REJECTED | DONE | ERROR
        this.checkTaskStatus(toDoTaskStatus);
    }

    /**
     * check task status
     *
     * @param toDoTaskStatus status
     */
    private void checkTaskStatus(final ToDoTaskStatus toDoTaskStatus)
    {
        if (toDoTaskStatus == ToDoTaskStatus.ERROR || toDoTaskStatus == ToDoTaskStatus.REJECTED || toDoTaskStatus == ToDoTaskStatus.PENDING_ERROR)
        {
            log.error("[TaskProcessorImpl] -> [checkTaskStatus]: To do task status provided: [{}] to perform configuration has been REJECTED or contains errors.", toDoTaskStatus);
        }
    }

    /**
     * Close a Schedule Planning Task
     *
     * @param deploymentPlan deployment plan Id
     * @param deploymentTask the to do task associated
     * @param toDoTaskStatus the new status. Must be one of {@code ToDoTaskStatus}
     */
    private void closeSchedulePlanningTask(final DeploymentPlan deploymentPlan, final DeploymentTask deploymentTask, final ToDoTaskStatus toDoTaskStatus)
    {
        if (toDoTaskStatus == ToDoTaskStatus.ERROR || toDoTaskStatus == ToDoTaskStatus.PENDING_ERROR)
        {
            deploymentPlan.setStatus(DeploymentStatus.DEFINITION);
            log.error("[TaskProcessorImpl] -> [closeSchedulePlanningTask]: The To do task status: [{}] for deployment plan id: [{}] contains errors. Deployment task: [{}]", toDoTaskStatus, deploymentPlan.getId(), deploymentTask);
            this.deploymentPlanRepository.save(deploymentPlan);
        }
        else if (toDoTaskStatus == ToDoTaskStatus.REJECTED)
        {
            deploymentPlan.setStatus(DeploymentStatus.REJECTED);
            String errorMessage = "The TODO task associated with id: [" + deploymentTask.getId() + "] to deploy this plan is on status: [" + toDoTaskStatus.name() + "] due to closing motive: [" + deploymentTask.getClosingMotive() + "] " +
                    "and task status message: [" + deploymentTask.getStatusMessage() + "]";
            deploymentPlan.setRejectionMessage(errorMessage);

            log.error("[TaskProcessorImpl] -> [closeSchedulePlanningTask]: Task to deploy scheduled plan has been rejected for deployment plan id: [{}]. Error message: [{}]", deploymentPlan.getId(), errorMessage);
            this.deploymentPlanRepository.save(deploymentPlan);
        }
    }

    /**
     * Close a Nova Planning Task
     *
     * @param deploymentPlan           - deployment plan Id
     * @param deploymentTask           - the task Id
     * @param toDoTaskStatus the new status. Must be one of {@code ToDoTaskStatus}
     */
    private void closeNovaPlanningTask(final DeploymentPlan deploymentPlan, final DeploymentTask deploymentTask, final ToDoTaskStatus toDoTaskStatus)
    {
        // If task was accepted:
        if (toDoTaskStatus == ToDoTaskStatus.DONE)
        {
            try
            {
                // Check deployment date as future date at scheduling time (not at deploying time):
                // This validates a future deployment date while scheduling, unlike checkDeploymentDate(), that validates a non-future deployment date while deploying */
                this.deploymentsValidator.checkNovaPlannedSchedulingDate(deploymentPlan);

                //Update status to SCHEDULED
                deploymentPlan.setStatus(DeploymentStatus.SCHEDULED);
                this.deploymentPlanRepository.save(deploymentPlan);

                // Launch real deployment and change plan status to DEPLOYED.
                log.debug("[TaskProcessorImpl] -> [closeNovaPlanningTask]: NOVA PLANNED task accepted, launching NOVA PLANNEDscheduled of deployment plan id: [{}]", deploymentTask.getDeploymentPlan().getId());
                deployerService.activateNovaPlannedDeployment(deploymentTask.getCreationUserCode(), deploymentTask.getDeploymentPlan());
            }
            catch (NovaException e)
            {
                deploymentPlan.setStatus(DeploymentStatus.REJECTED);
                String errorMessage = "The deployment task type: [" + deploymentTask.getTaskType().name() + "] in DONE status to deploy NOVA PLANNED contains errors: [" + e.getNovaError().getErrorMessage() + "] ";
                deploymentPlan.setRejectionMessage(errorMessage);
                log.error("[TaskProcessorImpl] -> [closeNovaPlanningTask]: the deployment task: [{}] for deployment plan id: [{}] to deploy NOVA PLANNED contains errors: [{}]", deploymentTask, deploymentPlan.getId(), e.getNovaError().getErrorMessage());
                this.deploymentPlanRepository.save(deploymentPlan);
            }
        }
        else if (toDoTaskStatus == ToDoTaskStatus.ERROR || toDoTaskStatus == ToDoTaskStatus.PENDING_ERROR)
        {
            deploymentPlan.setStatus(DeploymentStatus.DEFINITION);
            String errorMessage = "The todo task associated with id : [" + deploymentTask.getId() + "] " +
                    "is: [" + toDoTaskStatus.name() + "] " +
                    "due to close motive: [" + deploymentTask.getClosingMotive() + "]" +
                    " - task status" + " message: [" + deploymentTask.getStatusMessage() + "]";
            deploymentPlan.setRejectionMessage(errorMessage);
            log.error("[TaskProcessorImpl] -> [closeNovaPlanningTask]: the deployment task: [{}] for deployment plan id: [{}] to deploy NOVA PLANNED contains errors: [{}]", deploymentTask, deploymentPlan.getId(), errorMessage);
            this.deploymentPlanRepository.save(deploymentPlan);
        }
        else if (toDoTaskStatus == ToDoTaskStatus.REJECTED)
        {
            deploymentPlan.setStatus(DeploymentStatus.REJECTED);
            String errorMessage = "The todo task associated with id : [" + deploymentTask.getId() + "]" +
                    " has been REJECTED due to close motive: [" + deploymentTask.getClosingMotive() + "]" +
                    " - task status message: [" + deploymentTask.getStatusMessage() + "]";
            deploymentPlan.setRejectionMessage(errorMessage);

            log.error("[TaskProcessorImpl] -> [closeNovaPlanningTask]: the deployment task: [{}] for deployment plan id: [{}] to deploy NOVA PLANNED has been REJECTED due to contains errors: [{}]", deploymentTask, deploymentPlan.getId(), errorMessage);
            this.deploymentPlanRepository.save(deploymentPlan);
        }
    }
}
