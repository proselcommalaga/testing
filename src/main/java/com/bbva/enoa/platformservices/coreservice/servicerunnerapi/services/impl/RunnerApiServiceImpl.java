package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.impl;

import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.ITaskProcessor;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IRunnerApiService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IServiceRunner;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.enumerates.DeploymentInstanceStatus;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class RunnerApiServiceImpl implements IRunnerApiService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(RunnerApiServiceImpl.class);

    /**
     * Instance repository
     */
    @Autowired
    private DeploymentInstanceRepository deploymentInstanceRepository;

    /**
     * Service repository
     */
    @Autowired
    private DeploymentServiceRepository deploymentServiceRepository;

    /**
     * Subsystem repository
     */
    @Autowired
    private DeploymentSubsystemRepository deploymentSubsystemRepository;

    /**
     * Plan repository
     */
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    /**
     * ReleaseVersionService repository
     */
    @Autowired
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;

    /**
     * Pending check service
     */
    @Autowired
    private PendingCheckService pendingCheckService;

    /**
     * User service client
     */
    @Autowired
    private IProductUsersClient usersService;

    /**
     * Service runner
     */
    @Autowired
    private IServiceRunner serviceRunner;

    /**
     * Task processor
     */
    @Autowired
    private ITaskProcessor taskProcessor;

    /**
     * Task processor
     */
    @Autowired
    private ManageValidationUtils manageValidationUtils;

    /**
     * Nova activities emitter
     */
    @Autowired
    private INovaActivityEmitter novaActivityEmitter;

    @Autowired
    private IDeploymentsService deploymentService;

    /**
     * Permission exception
     */
    private static final NovaException PERMISSION_DENIED = new NovaException(ServiceRunnerError.getForbiddenError(), ServiceRunnerError.getForbiddenError().toString());

    @Override
    public TodoTaskResponseDTO stopBatchScheduleInstance(final Integer scheduleInstanceId, final Integer deploymentPlanId, final String ivUser)
    {
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + deploymentPlanId));

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.STOP_BATCH_SCHEDULE_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.stopBatchScheduleInstance(ivUser, scheduleInstanceId);
        }
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

            this.pendingCheckService.checkPendingTasks(scheduleInstanceId, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createBatchScheduleServiceProductionTask(ivUser, scheduleInstanceId, plan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("[RunnerApiServiceImpl] -> [stopBatchScheduleInstance]: Production task for stopping instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Stop Batch Schedule Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.SEND_REQUEST_STOPPED)
                        .entityId(scheduleInstanceId)
                        .environment(plan.getEnvironment())
                        .addParam("deploymentPlanId", plan.getId())
                        .addParam("serviceType", ChangeType.STOP_INSTANCE)
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            LOG.error("[RunnerApiServiceImpl] -> [stopBatchScheduleInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, deploymentPlanId, plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO resumeBatchScheduleInstance(final Integer scheduleInstanceId, final Integer deploymentPlanId, final String ivUser)
    {
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + deploymentPlanId));

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.RESUME_BATCH_SCHEDULE_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.resumeBatchScheduleInstance(ivUser, scheduleInstanceId);
        }
        // If can not be manage by user and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

            this.pendingCheckService.checkPendingTasks(scheduleInstanceId, toDoTaskTypeList);

            todoTaskResponseDTO = this.serviceRunner.createBatchScheduleServiceProductionTask(ivUser, scheduleInstanceId, plan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("[RunnerListener] -> [resumeBatchScheduleInstance]: Production task for resuming instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Restart Batch Schedule Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.SEND_REQUEST_RESTARTED)
                        .entityId(scheduleInstanceId)
                        .environment(plan.getEnvironment())
                        .addParam("deploymentPlanId", plan.getId())
                        .addParam("serviceType", ChangeType.RESTART_INSTANCE)
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [resumeBatchScheduleInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, deploymentPlanId, plan.getEnvironment());
            throw PERMISSION_DENIED;
        }

        return todoTaskResponseDTO;
    }

    @Override
    public void onServiceRunnerTaskReply(final Integer taskId, final String newStatus) throws Errors
    {
        this.taskProcessor.onTaskReply(taskId, newStatus);
    }

    @Override
    public TodoTaskResponseDTO pauseBatchScheduleInstance(final Integer scheduleInstanceId, final Integer deploymentPlanId, final String ivUser)
    {
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + deploymentPlanId));

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.PAUSE_BATCH_SCHEDULE_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.pauseBatchScheduleInstance(ivUser, scheduleInstanceId);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {

            List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

            this.pendingCheckService.checkPendingTasks(scheduleInstanceId, toDoTaskTypeList);
            // If production and auto management disabled -> create a TodoTask to Service support or POwner
            todoTaskResponseDTO = this.serviceRunner.createBatchScheduleServiceProductionTask(ivUser, scheduleInstanceId, plan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE);
            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for pausing instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Pause Batch Schedule Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.SEND_REQUEST_PAUSED)
                        .entityId(scheduleInstanceId)
                        .environment(plan.getEnvironment())
                        .addParam("deploymentPlanId", plan.getId())
                        .addParam("serviceType", ChangeType.PAUSE_INSTANCE)
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [pauseBatchScheduleInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, deploymentPlanId, plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO stopSubsystem(final Integer subsystemId, final String ivUser)
    {
        // Get the Subsystem
        final DeploymentSubsystem subsystem = this.deploymentSubsystemRepository.findById(subsystemId).orElse(null);
        if (subsystem == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchSubsystemError(), "Subsystem not found: " + subsystemId);
        }

        // Gets the plan and Product
        final DeploymentPlan plan = subsystem.getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.STOP_SUBSYSTEM_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.stopSubsystem(ivUser, subsystem);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_START);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_STOP);

            this.pendingCheckService.checkPendingTasks(subsystem, toDoTaskTypeList);

            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, subsystem, ToDoTaskType.SUBSYSTEM_STOP);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for stopping subsystem created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Stop Deployment Subsystem Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SUBSYSTEM, ActivityAction.SEND_REQUEST_STOPPED)
                        .entityId(subsystem.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionName", subsystem.getSubsystem().getReleaseVersion().getVersionName())
                        .addParam("TagName", subsystem.getSubsystem().getTagName())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [stopSubsystem]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[stopSubsystem] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO startPlan(final Integer planId, final String ivUser) throws Errors
    {
        // Get the Deployment Plan
        final DeploymentPlan plan = this.deploymentPlanRepository.findById(planId).orElse(null);
        if (plan == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + planId);
        }

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.START_PLAN_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.startDeploymentPlan(ivUser, plan);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.RELEASE_START);
            toDoTaskTypeList.add(ToDoTaskType.RELEASE_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.RELEASE_STOP);

            this.pendingCheckService.checkPendingTasks(plan, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, plan, ToDoTaskType.RELEASE_START);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for starting plan created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Start Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_STARTED)
                        .entityId(plan.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                        .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [startPlan]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[startPlan] -> Finished");
        return todoTaskResponseDTO;
    }


    @Override
    public TodoTaskResponseDTO startBatchScheduleInstance(final Integer releaseVersionServiceId, final Integer deploymentPlanId, final String ivUser)
    {
        // Get the Deployment Plan
        final DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentPlanId).orElse(null);
        if (plan == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + deploymentPlanId);
        }

        // Get the Release Version Service
        final ReleaseVersionService releaseVersionService = this.releaseVersionServiceRepository.findById(releaseVersionServiceId).orElse(null);
        if (releaseVersionService == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchReleaseVersionError(), "Release version service id not found: " + releaseVersionServiceId);
        }

        // Get the Deployment Subsystem
        final DeploymentSubsystem deploymentSubsystem = this.deploymentSubsystemRepository.findByDeploymentPlanIdAndSubsystem(plan.getId(), releaseVersionService.getVersionSubsystem());
        if (deploymentSubsystem == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchSubsystemError(), "Deployment subsystem id not found: " + releaseVersionService.getVersionSubsystem().getId());
        }
        // Get the deployment service
        final DeploymentService service = this.deploymentServiceRepository.findByServiceIdAndDeploymentSubsystem(releaseVersionService.getId(), deploymentSubsystem);
        if (service == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchServiceError(), "Service not found: " + releaseVersionService.getId());
        }

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.START_BATCH_SCHEDULE_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.startBatchScheduleInstance(ivUser, service, plan);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START);

            this.pendingCheckService.checkPendingTasks(service, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, service, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for starting scheduler batch instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Start Batch Schedule Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.SEND_REQUEST_STARTED)
                        .entityId(service.getService().getId())
                        .environment(plan.getEnvironment())
                        .addParam("deploymentPlanId", plan.getId())
                        .addParam("serviceName", service.getService().getServiceName())
                        .addParam("serviceType", ChangeType.START_INSTANCE)
                        .addParam("releaseVersionName", service.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getVersionName())
                        .addParam("releaseName", service.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [startBatchScheduleInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[startBatchSchedulerInstance] -> Finished");

        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO startBatchSchedule(final Integer serviceId, final String ivUser)
    {
        // Get the service
        final DeploymentService service = this.deploymentServiceRepository.findById(serviceId).orElse(null);
        if (service == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchServiceError(), "Service not found: " + serviceId);
        }

        // Gets the plan and Product
        final DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.START_BATCH_SCHEDULE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.startBatchSchedule(ivUser, service);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_START);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_STOP);

            this.pendingCheckService.checkPendingTasks(service, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, service, ToDoTaskType.BATCH_SCHEDULE_START);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for starting service created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Active Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.SEND_REQUEST_ACTIVED)
                        .entityId(service.getId())
                        .environment(plan.getEnvironment())
                        .addParam("serviceName", service.getService().getServiceName())
                        .addParam("serviceType", service.getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [startBatchSchedule]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[startBatchSchedule] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO restartPlan(final Integer planId, final String ivUser) throws Errors
    {
        final DeploymentPlan plan = this.deploymentPlanRepository.findById(planId).orElse(null);

        if (plan == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + planId);
        }

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.RESTART_PLAN_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.restartDeploymentPlan(ivUser, plan);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.RELEASE_START);
            toDoTaskTypeList.add(ToDoTaskType.RELEASE_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.RELEASE_STOP);

            this.pendingCheckService.checkPendingTasks(plan, toDoTaskTypeList);

            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, plan, ToDoTaskType.RELEASE_RESTART);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("[RunnerListener] -> [restartPlan]: Production task for restarting plan created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Restart Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_RESTARTED)
                        .entityId(plan.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                        .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [restartPlan]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }

        LOG.debug("[RunnerListener] -> [restartPlan]: Restarting plan with value for parameter 'ivUser': {} and 'planId': {} " +
                "restarted", ivUser, planId);
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO startInstance(final Integer instanceId, final String ivUser) throws Errors
    {
        // Validate instance
        final DeploymentInstance instance = this.deploymentInstanceRepository.findById(instanceId).orElse(null);
        if (instance == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchInstanceError(), "Instance not found: " + instanceId);
        }
        // Gets the plan
        final DeploymentPlan plan = instance.getService().getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.START_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.startInstance(ivUser, instance);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_START);
            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_STOP);

            this.pendingCheckService.checkPendingTasks(instance, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, instance, ToDoTaskType.CONTAINER_START);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for starting instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Start Deployment Service Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE_INSTANCE, ActivityAction.SEND_REQUEST_STARTED)
                        .entityId(instance.getId())
                        .environment(plan.getEnvironment())
                        .addParam("deploymentServiceId", instance.getService().getId())
                        .addParam("deploymentInstanceType", instance.getType().getDeploymentInstanceType())
                        .addParam("serviceName", instance.getService().getService().getServiceName())
                        .addParam("serviceType", instance.getService().getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [startInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[startInstance] -> Finished");

        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO restartSubsystem(final Integer subsystemId, final String ivUser) throws Errors
    {
        final DeploymentSubsystem subsystem = this.deploymentSubsystemRepository.findById(subsystemId).orElse(null);
        if (subsystem == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchSubsystemError(), "Subsystem not found: " + subsystemId);
        }

        final DeploymentPlan plan = subsystem.getDeploymentPlan();
        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.RESTART_SUBSYSTEM_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.restartSubsystem(ivUser, subsystem);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_START);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_STOP);

            this.pendingCheckService.checkPendingTasks(subsystem, toDoTaskTypeList);

            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, subsystem, ToDoTaskType.SUBSYSTEM_RESTART);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("[RunnerListener] -> [restartSubsystem]: Production task for restarting subsystem created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Restart Deployment Subsystem Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SUBSYSTEM, ActivityAction.SEND_REQUEST_RESTARTED)
                        .entityId(subsystem.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionName", subsystem.getSubsystem().getReleaseVersion().getVersionName())
                        .addParam("TagName", subsystem.getSubsystem().getTagName())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [restartSubsystem]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[RunnerListener] -> [restartSubsystem]: Finished");

        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO restartService(final Integer serviceId, final String ivUser) throws Errors
    {
        final DeploymentService service = this.deploymentServiceRepository.findById(serviceId).orElse(null);
        if (service == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchServiceError(), "Service not found: " + serviceId);
        }

        final DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();
        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.RESTART_SERVICE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);
        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.restartService(ivUser, service);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.SERVICE_START);
            toDoTaskTypeList.add(ToDoTaskType.SERVICE_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.SERVICE_STOP);

            this.pendingCheckService.checkPendingTasks(service, toDoTaskTypeList);

            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, service, ToDoTaskType.SERVICE_RESTART);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("[RunnerListener] -> [restartService]: Production task for restarting service created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Restart Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.SEND_REQUEST_RESTARTED)
                        .entityId(service.getId())
                        .environment(plan.getEnvironment())
                        .addParam("serviceName", service.getService().getServiceName())
                        .addParam("serviceType", service.getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [restartService]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[RunnerListener] -> [restartService]: Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO startSubsystem(final Integer subsystemId, final String ivUser) throws Errors
    {
        // Get the Subsystem
        final DeploymentSubsystem subsystem = this.deploymentSubsystemRepository.findById(subsystemId).orElse(null);
        if (subsystem == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchSubsystemError(), "Subsystem not found: " + subsystemId);
        }
        // Gets the plan and Product
        final DeploymentPlan plan = subsystem.getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.START_SUBSYSTEM_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.startSubsystem(ivUser, subsystem);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_START);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_STOP);

            this.pendingCheckService.checkPendingTasks(subsystem, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, subsystem, ToDoTaskType.SUBSYSTEM_START);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for starting subsystem created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Start Deployment Subsystem Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SUBSYSTEM, ActivityAction.SEND_REQUEST_STARTED)
                        .entityId(subsystem.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionName", subsystem.getSubsystem().getReleaseVersion().getVersionName())
                        .addParam("TagName", subsystem.getSubsystem().getTagName())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [startSubsystem]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[startSubsystem] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO restartInstance(final Integer instanceId, final String ivUser) throws Errors
    {
        final DeploymentInstance instance = this.deploymentInstanceRepository.findById(instanceId).orElse(null);
        if (instance == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchInstanceError(), "Instance not found: " + instanceId);
        }

        final DeploymentPlan plan = instance.getService().getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.RESTART_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.restartInstance(ivUser, instance);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_START);
            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_STOP);

            this.pendingCheckService.checkPendingTasks(instance, toDoTaskTypeList);

            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, instance, ToDoTaskType.CONTAINER_RESTART);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("[RunnerListener] -> [restartInstance]: Production task for restarting instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Restart Deployment Service Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE_INSTANCE, ActivityAction.SEND_REQUEST_RESTARTED)
                        .entityId(instance.getId())
                        .environment(plan.getEnvironment())
                        .addParam("deploymentServiceId", instance.getService().getId())
                        .addParam("deploymentInstanceType", instance.getType().getDeploymentInstanceType())
                        .addParam("serviceName", instance.getService().getService().getServiceName())
                        .addParam("serviceType", instance.getService().getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [restartInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[RestartInstance] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO stopInstance(final Integer instanceId, final String ivUser)
    {
        // Validate instance
        final DeploymentInstance instance = this.deploymentInstanceRepository.findById(instanceId).orElse(null);
        if (instance == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchInstanceError(), "Instance not found: " + instanceId);
        }
        // Gets the plan and Product
        final DeploymentPlan plan = instance.getService().getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.STOP_INSTANCE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.stopInstance(ivUser, instance);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_START);
            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.CONTAINER_STOP);

            this.pendingCheckService.checkPendingTasks(instance, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, instance, ToDoTaskType.CONTAINER_STOP);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for stopping instance created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Stop Deployment Service Instance Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE_INSTANCE, ActivityAction.SEND_REQUEST_STOPPED)
                        .entityId(instance.getId())
                        .environment(plan.getEnvironment())
                        .addParam("deploymentServiceId", instance.getService().getId())
                        .addParam("deploymentInstanceType", instance.getType().getDeploymentInstanceType())
                        .addParam("serviceName", instance.getService().getService().getServiceName())
                        .addParam("serviceType", instance.getService().getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [stopInstance]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[stopInstance] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO stopPlan(final Integer planId, final String ivUser)
    {
        // Get the Deployment Plan
        final DeploymentPlan plan = this.deploymentPlanRepository.findById(planId).orElse(null);
        if (plan == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + planId);
        }

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.STOP_PLAN_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.stopDeploymentPlan(ivUser, plan);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.RELEASE_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.RELEASE_START);
            toDoTaskTypeList.add(ToDoTaskType.RELEASE_STOP);

            this.pendingCheckService.checkPendingTasks(plan, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, plan, ToDoTaskType.RELEASE_STOP);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for stopping plan created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Stop Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_STOPPED)
                        .entityId(plan.getId())
                        .environment(plan.getEnvironment())
                        .addParam("releaseVersionId", plan.getReleaseVersion().getId())
                        .addParam("deploymentStatus", plan.getStatus().getDeploymentStatus())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [stopPlan]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[stopPlan] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO stopService(final Integer serviceId, final String ivUser)
    {
        // Get the service
        final DeploymentService service = this.deploymentServiceRepository.findById(serviceId).orElse(null);
        if (service == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchServiceError(), "Service not found: " + serviceId);
        }

        // Gets the plan and Product
        final DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.STOP_SERVICE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.stopService(ivUser, service);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.SERVICE_START);
            toDoTaskTypeList.add(ToDoTaskType.SERVICE_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.SERVICE_STOP);

            this.pendingCheckService.checkPendingTasks(service, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, service, ToDoTaskType.SERVICE_STOP);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for stopping service created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Stop Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.SEND_REQUEST_STOPPED)
                        .entityId(service.getId())
                        .environment(plan.getEnvironment())
                        .addParam("serviceName", service.getService().getServiceName())
                        .addParam("serviceType", service.getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [stopService]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[stopService] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO stopBatchSchedule(final Integer serviceId, final String ivUser)
    {
        // Get the service
        final DeploymentService service = this.deploymentServiceRepository.findById(serviceId).orElse(null);
        if (service == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchServiceError(), "Service not found: " + serviceId);
        }

        // Gets the plan and Product
        final DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.STOP_BATCH_SCHEDULE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.stopBatchSchedule(ivUser, service);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_STOP);
            toDoTaskTypeList.add(ToDoTaskType.BATCH_SCHEDULE_START);

            this.pendingCheckService.checkPendingTasks(service, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, service, ToDoTaskType.BATCH_SCHEDULE_STOP);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for stopping service created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Deactive Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.SEND_REQUEST_DEACTIVED)
                        .entityId(service.getId())
                        .environment(plan.getEnvironment())
                        .addParam("serviceName", service.getService().getServiceName())
                        .addParam("serviceType", service.getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [stopBatchSchedule]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[stopBatchSchedule] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO startService(final Integer serviceId, final String ivUser) throws Errors
    {
        // Get the service
        final DeploymentService service = this.deploymentServiceRepository.findById(serviceId).orElse(null);
        if (service == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchServiceError(), "Service not found: " + serviceId);
        }
        // Gets the plan and Product
        final DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();

        // Checks if the user is allow to execute the action
        this.checkPermissions(plan, ivUser, ServiceRunnerConstants.START_SERVICE_PERMISSION);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check if plan can be managed by user
        // INT always can be manage, check for PRE and PRO environment, if false, depends on environment what to do
        if (manageValidationUtils.checkIfServiceActionCanBeManagedByUser(ivUser, plan))
        {
            this.serviceRunner.startService(ivUser, service);
        }
        // If can not be manage and environment is pro, generate todotask
        else if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            final List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();

            toDoTaskTypeList.add(ToDoTaskType.SERVICE_START);
            toDoTaskTypeList.add(ToDoTaskType.SERVICE_RESTART);
            toDoTaskTypeList.add(ToDoTaskType.SERVICE_STOP);

            this.pendingCheckService.checkPendingTasks(service, toDoTaskTypeList);

            // If production and auto management disabled -> create a TodoTask to Service support
            todoTaskResponseDTO = this.serviceRunner.createProductionTask(ivUser, service, ToDoTaskType.SERVICE_START);

            if (todoTaskResponseDTO.getGenerated())
            {
                LOG.debug("Production task for starting service created with id {}", todoTaskResponseDTO.getTodoTaskId());

                // Emit Send Request Start Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(plan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.SEND_REQUEST_STARTED)
                        .entityId(service.getId())
                        .environment(plan.getEnvironment())
                        .addParam("serviceName", service.getService().getServiceName())
                        .addParam("serviceType", service.getService().getServiceType())
                        .addParam("DeploymentPlanId", plan.getId())
                        .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                        .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                        .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                        .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                        .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                        .build());
            }
        }
        else
        {
            // If can not be manage by user and environment is PRE, Permissions error, not allowed
            LOG.error("[RunnerApiServiceImpl] -> [startService]: User [{}] is not allowed to manage actions in plan [{}] at environment [{}] ", ivUser, plan.getId(), plan.getEnvironment());
            throw PERMISSION_DENIED;
        }
        LOG.debug("[startService] -> Finished");
        return todoTaskResponseDTO;
    }

    @Override
    public String[] getInstancesStatuses()
    {
        return Arrays.stream(DeploymentInstanceStatus.values()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    public Integer restartEphoenixInstanceByPlanification(final String ivUser, final String environment, final String hostname, final String productName, final String releaseName, final String subsystemName, final String serviceName) throws Errors
    {
        LOG.debug("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Trying to restart ePhoenix instance by control-M with input parameters. user: [{}], env: [{}], hostname: [{}], productName [{}], releaseName: [{}], subsystemName:[{}], serviceName:[{}]", ivUser, environment, hostname, productName, releaseName, subsystemName, serviceName);

        // Validate user
        if (!StringUtils.equals(ivUser, Constants.IMMUSER))
        {
            LOG.error("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Error: Executor user is not a valid user, action available only for NOVA IMM user. Current user:[{}]", ivUser);
            throw new NovaException(ServiceRunnerError.getForbiddenError());
        }

        // Validate if environment have a correct value
        if (!EnumUtils.isValidEnum(Environment.class, environment))
        {
            LOG.error("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Error: Environment parameter have and invalid value: [{}]", environment);
            throw new NovaException(ServiceRunnerError.InvalidInputParameter("environment", environment));
        }

        // Validate input parameters and obtain deployment service
        DeploymentService deploymentServiceToRestart = this.deploymentService.getDeploymentServiceByName(productName, environment, releaseName, subsystemName, serviceName);

        // Validate deployment service.
        if (deploymentServiceToRestart == null || deploymentServiceToRestart.getService() == null)
        {
            LOG.error("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Error: deployment service not found with input parameters");
            throw new NovaException(ServiceRunnerError.getNoSuchDeploymentServiceError(serviceName));
        }

        // Validate if service is ePhoenix, this functionality is only available for ePhoenix services.
        if (!ServiceType.EPHOENIX_BATCH.getServiceType().equals(deploymentServiceToRestart.getService().getServiceType()) &&
                !ServiceType.EPHOENIX_ONLINE.getServiceType().equals(deploymentServiceToRestart.getService().getServiceType()))
        {
            LOG.error("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Error: deployment service is service type [{}], is not an ePhoenix service. Forbidden action", deploymentServiceToRestart.getService().getServiceType());
            throw new NovaException(ServiceRunnerError.getRestartEphoenixInstanceForbiddenError(serviceName, productName));
        }

        LOG.info("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Restarting ePhoenix instance by control-M. user: [{}], hostname:[{}], serviceName:[{}], environment :[{}], productName:[{}]", ivUser, hostname, serviceName, environment, productName);

        // Obtain deployment Id to restart
        DeploymentInstance deploymentInstance = this.deploymentInstanceRepository.getDeploymentInstanceByHostNameAndServiceId(hostname, deploymentServiceToRestart.getId())
                .orElseThrow(() -> new NovaException(ServiceRunnerError.getNoSuchInstanceError()));

        // Restart Instance
        this.restartInstance(deploymentInstance.getId(), ivUser);

        LOG.debug("[RunnerApiServiceImpl] ->[restartEphoenixInstanceByPlanification]: Restarted ePhoenix instance by control-M with id [{}] and input parameters. user: [{}], env: [{}], hostname: [{}], productName [{}], relaseName: [{}], subsystemName:[{}], serviceName:[{}]", deploymentInstance.getId(), ivUser, environment, hostname, productName, releaseName, subsystemName, serviceName);

        return deploymentInstance.getId();
    }

    /**
     * @param deploymentPlan with the deployment plan
     * @param userCode       with the user code
     * @param permissionName with the permission name
     */
    private void checkPermissions(final DeploymentPlan deploymentPlan, final String userCode, final String permissionName)
    {
        String env = deploymentPlan.getEnvironment();
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, permissionName, env, productId, PERMISSION_DENIED);
    }

}
