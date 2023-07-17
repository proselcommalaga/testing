package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.impl;

import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistoryId;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.datamodel.model.utils.HibernateUtils;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.enums.BatchSchedulerInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ServiceExecutionHistoryRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.EtherManagerClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IServiceRunner;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerUtils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages stopping and starting instances of {@link DeploymentPlan}s,
 * {@link DeploymentSubsystem}s and {@link DeploymentService}s.
 */
@Slf4j
@Service
public class ServiceRunnerImpl implements IServiceRunner
{
    @Autowired
    private IDeploymentManagerService deploymentManagerService;

    @Autowired
    private DeploymentChangeRepository deploymentChangeRepository;

    @Autowired
    private TodoTaskServiceClient todoTaskServiceClient;

    @Autowired
    private DeploymentInstanceRepository deploymentInstanceRepository;

    @Autowired
    private DeploymentServiceRepository deploymentServiceRepository;

    @Autowired
    private DeploymentSubsystemRepository deploymentSubsystemRepository;

    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    @Autowired
    private ISchedulerManagerClient schedulerManagerClient;

    @Autowired
    private ServiceRunnerUtils serviceRunnerUtils;

    @Autowired
    private DeploymentUtils deploymentUtils;

    @Autowired
    private EtherManagerClientImpl etherManagerClient;

    /**
     * User service client
     */
    @Autowired
    private IProductUsersClient usersService;

    /**
     * Tools service client
     */
    @Autowired
    private IToolsClient toolsService;

    /**
     * Library manager service client
     */
    @Autowired
    private ILibraryManagerService libraryManagerService;

    /**
     * Nova activities emitter
     */
    @Autowired
    private INovaActivityEmitter novaActivityEmitter;

    /**
     * ReleaseVersionService repository
     */
    @Autowired
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;

    @Autowired
    private ServiceExecutionHistoryRepository serviceExecutionHistoryRepository;

    /**
     * Permission exception
     */
    private static final NovaException PERMISSION_DENIED =
            new NovaException(ServiceRunnerError.getForbiddenError());

    /**
     * Permission exception
     */
    private static final NovaException BATCH_SCHEDULE_PERMISSION_DENIED =
            new NovaException(ServiceRunnerError.getBatchScheduleForbiddenError());

    @Override
    public void startInstance(final String userCode, final DeploymentInstance instance)
    {

        DeploymentPlan plan = instance.getService().getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();

        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_INSTANCE_PERMISSION, env, productId, PERMISSION_DENIED);

        // Check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentService(instance.getService(), instance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment());

        if (PlatformUtils.isPlanDeployedInEther(plan))
        {

            log.debug("Generating Ether Deployment DTO to request for instances start: " + "-Service used to generate it [{}]", instance.getService());

            // 1.Obtener datos para la creacion de la llamada a Ether Manager
            EtherDeploymentDTO etherDeploymentDTO = this.deploymentUtils.buildEtherDeploymentDTO(instance.getService(), CallbackService.START, CallbackService.START_ERROR);

            log.debug("Generated Ether Deployment DTO to request for instances start: " + "-DTO generated: {}", etherDeploymentDTO);

            // 2.Llamar al servicio EtherManager con la info necesaria
            this.etherManagerClient.startEtherService(etherDeploymentDTO);

        }
        else
        {
            final NovaDeploymentInstance novaDeploymentInstance = HibernateUtils.getNovaDeploymentInstanceFromHibernateProxy(instance);

            // Call service
            this.deploymentManagerService.startInstance(instance);
            DeploymentService service = instance.getService();
            log.debug(
                    "Instance {} of service {} has been started by user {}",
                    novaDeploymentInstance.getClusterName(),
                    service.getService().getServiceName(),
                    userCode
            );
            // History
            this.addChange(
                    service.getDeploymentSubsystem().getDeploymentPlan(),
                    userCode,
                    ChangeType.START_INSTANCE,
                    "Se ha arrancado la instancia con nombre de servicio completo: [" + instance.getService().getService().getFinalName() + "] de tipo: [" + instance.getService().getService().getServiceType()
                            + "] con nombre de contenedor: [" + novaDeploymentInstance.getContainerName() + "] pertenenciente al JOB del subsistema: [" + instance.getService().getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");
        }

        ServiceExecutionHistory serviceExecutionHistory = getServiceExecutionHistoryForInstance(instance, env);
        serviceExecutionHistoryRepository.save(serviceExecutionHistory);

        // Emit Start Deployment Service Instance Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE_INSTANCE, ActivityAction.STARTED)
                .entityId(instance.getId())
                .environment(env)
                .addParam("deploymentServiceId", instance.getService().getId())
                .addParam("deploymentInstanceType", instance.getType().getDeploymentInstanceType())
                .addParam("serviceName", instance.getService().getService().getServiceName())
                .addParam("finalName", instance.getService().getService().getFinalName())
                .addParam("serviceType", instance.getService().getService().getServiceType())
                .addParam("tagName", instance.getService().getDeploymentSubsystem().getSubsystem().getTagName())
                .addParam("CompilationJobName", instance.getService().getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .build());
    }

    private ServiceExecutionHistory getServiceExecutionHistoryForInstance(DeploymentInstance instance, String environment)
    {
        Calendar calendar = Calendar.getInstance();
        ServiceExecutionHistoryId id = new ServiceExecutionHistoryId();
        id.setEnvironment(environment);
        DeploymentService deploymentService = instance.getService();
        ReleaseVersionService releaseVersionService = deploymentService.getService();
        id.setFinalName(releaseVersionService.getFinalName());
        id.setVersion(releaseVersionService.getVersion());
        ServiceExecutionHistory serviceExecutionHistory = new ServiceExecutionHistory();
        serviceExecutionHistory.setId(id);
        serviceExecutionHistory.setLastExecution(calendar);
        serviceExecutionHistory.setDeploymentServiceId(deploymentService.getId());
        return serviceExecutionHistory;
    }

    @Override
    public void stopInstance(final String userCode, final DeploymentInstance instance)
    {
        DeploymentPlan plan = instance.getService().getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.STOP_INSTANCE_PERMISSION, env, productId, PERMISSION_DENIED);

        if (PlatformUtils.isPlanDeployedInEther(plan))
        {
            log.debug("Generating Ether Deployment DTO to request for instances stop: " + "-Service used to generate it [{}]", instance.getService());

            // 1.Obtener datos para la creacion de la llamada a Ether Manager
            EtherDeploymentDTO etherDeploymentDTO = this.deploymentUtils.buildEtherDeploymentDTO(instance.getService(), CallbackService.STOP, CallbackService.STOP_ERROR);

            log.debug("Generated Ether Deployment DTO to request for instances start: " + "-DTO generated: {}", etherDeploymentDTO);

            // 2.Llamar al servicio EtherManager con la info necesaria
            this.etherManagerClient.stopEtherService(etherDeploymentDTO);

        }
        else
        {
            final NovaDeploymentInstance novaDeploymentInstance = HibernateUtils.getNovaDeploymentInstanceFromHibernateProxy(instance);

            // Call service
            this.deploymentManagerService.stopInstance(instance);
            DeploymentService service = instance.getService();
            log.debug(
                    "Instance {} of service {} has been stopped by user {}",
                    novaDeploymentInstance.getClusterName(),
                    service.getService().getServiceName(),
                    userCode
            );
            // History
            this.addChange(
                    service.getDeploymentSubsystem().getDeploymentPlan(),
                    userCode,
                    ChangeType.STOP_INSTANCE,
                    "Se ha parado la instancia con nombre de servicio completo: [" + instance.getService().getService().getFinalName() + "] de tipo: [" + instance.getService().getService().getServiceType()
                            + "] con nombre de contenedor: [" + novaDeploymentInstance.getContainerName() + "] pertenenciente al JOB del subsistema: [" + instance.getService().getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");

            // Emit Stop Deployment Service Instance Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE_INSTANCE, ActivityAction.STOPPED)
                    .entityId(instance.getId())
                    .environment(env)
                    .addParam("deploymentServiceId", instance.getService().getId())
                    .addParam("deploymentInstanceType", instance.getType().getDeploymentInstanceType())
                    .addParam("serviceName", instance.getService().getService().getServiceName())
                    .addParam("finalName", instance.getService().getService().getFinalName())
                    .addParam("serviceType", instance.getService().getService().getServiceType())
                    .addParam("DeploymentPlanId", plan.getId())
                    .addParam("tagName", instance.getService().getDeploymentSubsystem().getSubsystem().getTagName())
                    .addParam("CompilationJobName", instance.getService().getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                    .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                    .build());
        }
    }

    @Override
    public void startService(final String userCode, final DeploymentService service)
    {
        DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();

        // Check permissions depending on the service type,
        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType()))
        {
            this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_SERVICE_PERMISSION, env, productId, BATCH_SCHEDULE_PERMISSION_DENIED);
        }
        else
        {
            this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_SERVICE_PERMISSION, env, productId, PERMISSION_DENIED);
        }

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentService(service, service.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());

        // Call service depending of the service type. Batch schedule activate the batch schedule service. Start service (start containers of the service) in any case
        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType()))
        {
            this.schedulerManagerClient.updateDeploymentBatchSchedule(service.getService().getId(), plan.getId(), DeploymentBatchScheduleStatus.ENABLED);
        }
        else
        {
            this.deploymentManagerService.startService(service);
        }

        log.debug(
                "Service {} of plan {} has been started by user {}",
                service.getService().getServiceName(),
                service.getDeploymentSubsystem().getDeploymentPlan().getId(),
                userCode
        );
        // History
        this.addChange(
                service.getDeploymentSubsystem().getDeploymentPlan(),
                userCode,
                ChangeType.START_SERVICE,
                "Se ha arrancado el servicio con nombre: [" + service.getService().getServiceName() + "]-[" + service.getService().getFinalName() + "] de tipo: [" + service.getService().getServiceType()
                        + "] con nombre de imagen: [" + service.getService().getImageName() + "] pertenenciente al JOB del subsistema: [" + service.getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");

        ServiceExecutionHistory serviceExecutionHistory = getServiceExecutionHistoryForService(service, env);
        if (serviceExecutionHistory != null)
        {
            serviceExecutionHistoryRepository.save(serviceExecutionHistory);
        }

        // Emit Start Deployment Service Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.STARTED)
                .entityId(service.getId())
                .environment(env)
                .addParam("serviceName", service.getService().getServiceName())
                .addParam("finalName", service.getService().getFinalName())
                .addParam("serviceType", service.getService().getServiceType())
                .addParam("tagName", service.getDeploymentSubsystem().getSubsystem().getTagName())
                .addParam("CompilationJobName", service.getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .build());
    }

    private ServiceExecutionHistory getServiceExecutionHistoryForService(DeploymentService deploymentService, String environment)
    {
        ReleaseVersionService releaseVersionService = deploymentService.getService();
        if (ServiceType.valueOf(releaseVersionService.getServiceType()).isBatch())
        {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        ServiceExecutionHistoryId id = new ServiceExecutionHistoryId();
        id.setEnvironment(environment);
        id.setFinalName(releaseVersionService.getFinalName());
        id.setVersion(releaseVersionService.getVersion());
        ServiceExecutionHistory serviceExecutionHistory = new ServiceExecutionHistory();
        serviceExecutionHistory.setId(id);
        serviceExecutionHistory.setLastExecution(calendar);
        serviceExecutionHistory.setDeploymentServiceId(deploymentService.getId());
        return serviceExecutionHistory;
    }

    @Override
    public void stopService(final String userCode, final DeploymentService service)
    {

        DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();

        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType()))
        {
            this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.STOP_SERVICE_PERMISSION, env, productId, BATCH_SCHEDULE_PERMISSION_DENIED);
        }
        else
        {
            this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.STOP_SERVICE_PERMISSION, env, productId, PERMISSION_DENIED);
        }

        // Call service depending of the service type. Batch schedule activate the batch schedule service. If not, stop service (stop containers of the service) in any case
        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType()))
        {
            this.schedulerManagerClient.updateDeploymentBatchSchedule(service.getService().getId(), plan.getId(), DeploymentBatchScheduleStatus.DISABLED);
        }
        else
        {
            this.deploymentManagerService.stopService(service);
        }

        log.debug(
                "Service {} of plan {} has been stopped by user {}",
                service.getService().getServiceName(),
                service.getDeploymentSubsystem().getDeploymentPlan().getId(),
                userCode
        );
        // History
        this.addChange(
                service.getDeploymentSubsystem().getDeploymentPlan(),
                userCode,
                ChangeType.STOP_SERVICE,
                "Se ha parado el servicio con nombre: [" + service.getService().getServiceName() + "]-[" + service.getService().getFinalName() + "] de tipo: [" + service.getService().getServiceType()
                        + "] con nombre de imagen: [" + service.getService().getImageName() + "] pertenenciente al JOB del subsistema: [" + service.getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");

        // Emit Stop Deployment Service Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.STOPPED)
                .entityId(service.getId())
                .environment(env)
                .addParam("serviceName", service.getService().getServiceName())
                .addParam("serviceType", service.getService().getServiceType())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .addParam("tagName", service.getDeploymentSubsystem().getSubsystem().getTagName())
                .addParam("CompilationJobName", service.getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("finalName", service.getService().getFinalName())
                .build());
    }

    @Override
    public void startSubsystem(final String userCode, final DeploymentSubsystem subsystem)
    {
        DeploymentPlan plan = subsystem.getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_SUBSYSTEM_PERMISSION, env, productId, PERMISSION_DENIED);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentSubsystem(subsystem, subsystem.getDeploymentPlan().getEnvironment());

        // Call service
        TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());

        this.deploymentManagerService.startSubsystem(subsystem);
        log.debug(
                "Subsystem {} of plan {} has been started by user {}",
                subsystemDTO.getSubsystemName(),
                subsystem.getDeploymentPlan().getId(),
                userCode
        );

        //If control-M starts the subsystem, then force to auto start BatchScheduler services into the plan
        this.autoEnableBatchScheduleByControlMScript(userCode, subsystem.getDeploymentPlan().getId(), List.of(subsystem));

        // History
        this.addChange(
                subsystem.getDeploymentPlan(),
                userCode,
                ChangeType.START_SUBSYSTEM,
                "Se han arrancado el subsistema pertenenciente al JOB del subsistema: [" + subsystem.getSubsystem().getCompilationJobName() + "]");

        List<ServiceExecutionHistory> serviceExecutionHistories = getServiceExecutionHistoriesForSubsystem(subsystem, env);
        if (!serviceExecutionHistories.isEmpty())
        {
            serviceExecutionHistoryRepository.saveAll(serviceExecutionHistories);
        }

        // Emit Start Deployment Subsystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SUBSYSTEM, ActivityAction.STARTED)
                .entityId(subsystem.getId())
                .environment(env)
                .addParam("releaseVersionName", subsystem.getSubsystem().getReleaseVersion().getVersionName())
                .addParam("TagName", subsystem.getSubsystem().getTagName())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .addParam("CompilationJobName", subsystem.getSubsystem().getCompilationJobName())
                .build());
    }

    private List<ServiceExecutionHistory> getServiceExecutionHistoriesForSubsystem(DeploymentSubsystem subsystem, String environment)
    {
        List<ServiceExecutionHistory> serviceExecutionHistories = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        for (DeploymentService deploymentService : subsystem.getDeploymentServices())
        {
            ReleaseVersionService releaseVersionService = deploymentService.getService();
            if (ServiceType.valueOf(releaseVersionService.getServiceType()).isBatch())
            {
                continue;
            }
            ServiceExecutionHistoryId id = new ServiceExecutionHistoryId();
            id.setEnvironment(environment);
            id.setFinalName(releaseVersionService.getFinalName());
            id.setVersion(releaseVersionService.getVersion());
            ServiceExecutionHistory serviceExecutionHistory = new ServiceExecutionHistory();
            serviceExecutionHistory.setId(id);
            serviceExecutionHistory.setLastExecution(calendar);
            serviceExecutionHistory.setDeploymentServiceId(deploymentService.getId());
            serviceExecutionHistories.add(serviceExecutionHistory);
        }
        return serviceExecutionHistories;
    }

    @Override
    public void stopSubsystem(final String userCode, final DeploymentSubsystem subsystem)
    {
        //check if precong
        DeploymentPlan plan = subsystem.getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();

        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.STOP_SUBSYSTEM_PERMISSION, env, productId, PERMISSION_DENIED);

        // Call service
        TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());

        this.deploymentManagerService.stopSubsystem(subsystem);
        log.debug(
                "Subsystem {} of plan {} has been stopped by user {}",
                subsystemDTO.getSubsystemName(),
                subsystem.getDeploymentPlan().getId(),
                userCode
        );
        // History
        this.addChange(
                subsystem.getDeploymentPlan(),
                userCode,
                ChangeType.STOP_SUBSYSTEM,
                "Se han parado el subsistema pertenenciente al JOB del subsistema: [" + subsystem.getSubsystem().getCompilationJobName() + "]");

        // Emit Stop Deployment Subsystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SUBSYSTEM, ActivityAction.STOPPED)
                .entityId(subsystem.getId())
                .environment(env)
                .addParam("releaseVersionName", subsystem.getSubsystem().getReleaseVersion().getVersionName())
                .addParam("TagName", subsystem.getSubsystem().getTagName())
                .addParam("CompilationJobName", subsystem.getSubsystem().getCompilationJobName())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .build());
    }

    @Override
    public void startDeploymentPlan(final String userCode, final DeploymentPlan deploymentPlan)
    {
        String env = deploymentPlan.getEnvironment();
        // Check permissions
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_PLAN_PERMISSION, env, productId, PERMISSION_DENIED);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());

        // Call service
        this.deploymentManagerService.startPlan(deploymentPlan);
        log.debug(
                "Plan {} of product {} has been started by user {}",
                deploymentPlan.getId(),
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getName(),
                userCode
        );

        //If control-M starts the plan, then force to auto start BatchScheduler services into the plan
        this.autoEnableBatchScheduleByControlMScript(userCode, deploymentPlan.getId(), deploymentPlan.getDeploymentSubsystems());

        // History
        this.addChange(deploymentPlan, userCode, ChangeType.START_PLAN, "Se han arrancando el plan de despliegue");

        List<ServiceExecutionHistory> serviceExecutionHistories = getServiceExecutionHistoriesForPlan(deploymentPlan);
        if (!serviceExecutionHistories.isEmpty())
        {
            serviceExecutionHistoryRepository.saveAll(serviceExecutionHistories);
        }

        // Emit Start Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_PLAN, ActivityAction.STARTED)
                .entityId(deploymentPlan.getId())
                .environment(env)
                .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", deploymentPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .build());
    }

    private List<ServiceExecutionHistory> getServiceExecutionHistoriesForPlan(DeploymentPlan deploymentPlan)
    {
        List<ServiceExecutionHistory> serviceExecutionHistories = new ArrayList<>();
        String environment = deploymentPlan.getEnvironment();
        Calendar calendar = Calendar.getInstance();
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
            {
                ReleaseVersionService releaseVersionService = deploymentService.getService();
                if (ServiceType.valueOf(releaseVersionService.getServiceType()).isBatch())
                {
                    continue;
                }
                ServiceExecutionHistoryId id = new ServiceExecutionHistoryId();
                id.setEnvironment(environment);
                id.setFinalName(releaseVersionService.getFinalName());
                id.setVersion(releaseVersionService.getVersion());
                ServiceExecutionHistory serviceExecutionHistory = new ServiceExecutionHistory();
                serviceExecutionHistory.setId(id);
                serviceExecutionHistory.setLastExecution(calendar);
                serviceExecutionHistory.setDeploymentServiceId(deploymentService.getId());
                serviceExecutionHistories.add(serviceExecutionHistory);
            }
        }
        return serviceExecutionHistories;
    }

    @Override
    public void stopDeploymentPlan(final String userCode, final DeploymentPlan deploymentPlan)
    {
        String env = deploymentPlan.getEnvironment();
        // Check permissions
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.STOP_PLAN_PERMISSION, env, productId, PERMISSION_DENIED);
        // Call service
        this.deploymentManagerService.stopPlan(deploymentPlan);
        log.debug("Plan {} of product {} has been stopped by user {}", deploymentPlan.getId(),
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getName(), userCode);
        // History
        this.addChange(deploymentPlan, userCode, ChangeType.STOP_PLAN, "Se han parado el plan de despliegue");

        // Emit Stop Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_PLAN, ActivityAction.STOPPED)
                .entityId(deploymentPlan.getId())
                .environment(env)
                .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", deploymentPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .build());
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO createProductionTask(final String userCode, final DeploymentInstance instance, final ToDoTaskType toDoTaskType)
            throws NovaException
    {
        String operation;
        if (toDoTaskType == ToDoTaskType.CONTAINER_START)
        {
            operation = Constants.START_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.CONTAINER_RESTART)
        {
            operation = Constants.RESTART_OPERATION;
        }
        else
        {
            operation = Constants.STOP_OPERATION;
        }

        String description =
                "El usuario " + userCode + " ha solicitado " + operation + " de la instancia " + instance.getId()
                        + " del " +
                        "servicio " + instance.getService().getService().getServiceName() + " en el entorno de Producción. Por favor, " +
                        "acepte o rechace la tarea.";

        try
        {
            Integer taskId = this.todoTaskServiceClient.createManagementTask(
                    userCode, null, toDoTaskType.name(), this.getProductionTaskDestinationRole(instance.getService().getDeploymentSubsystem().getDeploymentPlan(), toDoTaskType).name(),
                    description, instance.getService().getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct(), instance.getId());

            return this.serviceRunnerUtils.builderTodoTaskResponseDTO(true, this.getProductionTaskDestinationRole(instance.getService().getDeploymentSubsystem().getDeploymentPlan(), toDoTaskType), taskId, toDoTaskType);
        }
        catch (NovaException e)
        {
            String message = "[ServiceRunnerAPI] -> [createProductionTask]: this product: [" +
                    instance.getService().getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct().getName() + "]" +
                    " cannot create to do tasks due to the product does not have Jira project associated";
            log.error(message);
            throw new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError(), e, message);
        }
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO createProductionTask(final String userCode, final DeploymentService service, final ToDoTaskType toDoTaskType)
            throws NovaException
    {
        String operation;
        if (toDoTaskType == ToDoTaskType.SERVICE_START || toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_START || toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START)
        {
            operation = Constants.START_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.SERVICE_RESTART)
        {
            operation = Constants.RESTART_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_STOP)
        {
            operation = Constants.STOP_OPERATION;
        }
        else
        {
            operation = Constants.STOP_OPERATION;
        }

        String description = "El usuario " + userCode + " ha solicitado " + operation + " del servicio "
                + service.getService
                ().getServiceName() + " en el entorno de Producción. Por favor, acepte o rechace la tarea.";

        try
        {
            // TODO: Pending to resolve. For to do task BATCH_SCHEDULE_START, BATCH_SCHEDULE_STOP, BATCH_SCHEDULE_INSTANCE_START, BATCH_SCHEDULE_INSTANCE_PAUSE, BATCH_SCHEDULE_INSTANCE_RESUME and BATCH_SCHEDULE_INSTANCE_STOP set for PLATFORM ADMINs
            Integer taskId = this.todoTaskServiceClient.createManagementTask(
                    userCode, null, toDoTaskType.name(), this.getProductionTaskDestinationRole(service.getDeploymentSubsystem().getDeploymentPlan(), toDoTaskType).name(),
                    description, service.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct(), service.getId());

            return this.serviceRunnerUtils.builderTodoTaskResponseDTO(true, this.getProductionTaskDestinationRole(service.getDeploymentSubsystem().getDeploymentPlan(), toDoTaskType), taskId, toDoTaskType);
        }
        catch (NovaException e)
        {
            String message = "[ServiceRunnerAPI] -> [createProductionTask]: this product: [" +
                    service.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct().getName() + "]" +
                    " cannot create to do tasks due to the product does not have Jira project associated";

            if (toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_START || toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START || toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_STOP)
            {
                log.warn(message);
                return this.serviceRunnerUtils.builderTodoTaskResponseDTO(false);
            }
            else
            {
                log.error(message);
                throw new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError(), e, message);
            }
        }
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO createProductionTask(final String userCode, final DeploymentSubsystem subsystem, final ToDoTaskType toDoTaskType)
            throws NovaException
    {
        String operation;
        if (toDoTaskType == ToDoTaskType.RELEASE_START)
        {
            operation = Constants.START_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.RELEASE_RESTART)
        {
            operation = Constants.RESTART_OPERATION;
        }
        else
        {
            operation = Constants.STOP_OPERATION;
        }

        TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());

        String description = "El usuario " + userCode + " ha solicitado " + operation + " del subsistema " + subsystemDTO.getSubsystemName() +
                " en el entorno de Producción. Por favor, acepte o rechace la tarea.";

        try
        {

            Integer taskId = this.todoTaskServiceClient.createManagementTask(
                    userCode, null, toDoTaskType.name(), this.getProductionTaskDestinationRole(subsystem.getDeploymentPlan(), toDoTaskType).name(),
                    description, subsystem.getDeploymentPlan().getReleaseVersion().getRelease().getProduct(), subsystem.getId());

            return this.serviceRunnerUtils.builderTodoTaskResponseDTO(true, this.getProductionTaskDestinationRole(subsystem.getDeploymentPlan(), toDoTaskType), taskId, toDoTaskType);
        }
        catch (NovaException e)
        {
            String message = "[ServiceRunnerAPI] -> [createProductionTask]: this product: [" +
                    subsystem.getDeploymentPlan().getReleaseVersion().getRelease().getProduct().getName() + "]" +
                    " cannot create to do tasks due to the product does not have Jira project associated";
            log.error(message);
            throw new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError(), e, message);
        }

    }

    @Override
    @Transactional
    public TodoTaskResponseDTO createProductionTask(final String userCode, final DeploymentPlan plan, final ToDoTaskType toDoTaskType)
            throws NovaException
    {
        String operation;
        if (toDoTaskType == ToDoTaskType.RELEASE_START)
        {
            operation = Constants.START_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.RELEASE_RESTART)
        {
            operation = Constants.RESTART_OPERATION;
        }
        else
        {
            operation = Constants.STOP_OPERATION;
        }

        String description = "El usuario " + userCode + " ha solicitado " + operation + " de la release " + plan
                .getReleaseVersion().getRelease().getName() + " - " + plan.getReleaseVersion().getVersionName() + " en el entorno de " +
                "Producción. Por favor, acepte o rechace la tarea.";

        try
        {

            Integer taskId = this.todoTaskServiceClient.createManagementTask(
                    userCode, null, toDoTaskType.name(), this.getProductionTaskDestinationRole(plan, toDoTaskType).name(),
                    description, plan.getReleaseVersion().getRelease().getProduct(), plan.getId());

            return this.serviceRunnerUtils.builderTodoTaskResponseDTO(true, this.getProductionTaskDestinationRole(plan, toDoTaskType), taskId, toDoTaskType);
        }
        catch (NovaException e)
        {
            String message = "[ServiceRunnerAPI] -> [createProductionTask]: this product: [" + plan.getReleaseVersion().getRelease().getProduct().getName() + "]" +
                    " cannot create to do tasks due to the product does not have Jira project associated";
            log.error(message);
            throw new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError(), e, message);
        }
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO createBatchScheduleServiceProductionTask(final String userCode, final Integer scheduleInstanceId, final DeploymentPlan deploymentPlan, final ToDoTaskType toDoTaskType) throws NovaException
    {
        String operation;
        if (toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME)
        {
            operation = Constants.RESTART_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE)
        {
            operation = Constants.PAUSE_OPERATION;
        }
        else if (toDoTaskType == ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP)
        {
            operation = Constants.STOP_OPERATION;
        }
        else
        {
            operation = Constants.STOP_OPERATION;
        }

        String description = "El usuario " + userCode + " ha solicitado " + operation + " del manejador de planificador de batch con id " + scheduleInstanceId + " de la release  " + deploymentPlan
                .getReleaseVersion().getRelease().getName() + " - " + deploymentPlan.getReleaseVersion().getVersionName() + " en el entorno de " +
                "Producción. Por favor, acepte o rechace la tarea.";

        try
        {
            Integer taskId = this.todoTaskServiceClient.createManagementTask(
                    userCode, null, toDoTaskType.name(), this.getProductionTaskDestinationRole(deploymentPlan, toDoTaskType).name(),
                    description, deploymentPlan.getReleaseVersion().getRelease().getProduct(), scheduleInstanceId);

            return this.serviceRunnerUtils.builderTodoTaskResponseDTO(true, this.getProductionTaskDestinationRole(deploymentPlan, toDoTaskType), taskId, toDoTaskType);
        }
        catch (NovaException e)
        {
            String message = "[ServiceRunnerAPI] -> [createBatchScheduleServiceProductionTask]: this product: [" + deploymentPlan.getReleaseVersion().getRelease().getProduct().getName() + "]" +
                    " cannot create to do tasks due to the product does not have Jira project associated";

            log.warn(message);
            return this.serviceRunnerUtils.builderTodoTaskResponseDTO(false);
        }
    }

    /**
     * Obtain destination role for production task:
     * - By default in PRO todotask destination is Service Support
     * - Platform Admin: If the task type have to be managed by Platform Admin team
     * - Product Owner: If release have enabled automanaged in PRO, destination always are for POwner
     *
     * @param deploymentPlan The deployment plan whome generate the task
     * @param toDoTaskType   To do task generated
     * @return Role type destination for the task
     */
    private RoleType getProductionTaskDestinationRole(final DeploymentPlan deploymentPlan, final ToDoTaskType toDoTaskType)
    {
        // By default in PRO todoTask destination is SS
        RoleType roleDestination = RoleType.SERVICE_SUPPORT;

        // If the task have to be managed by Platform Admin team
        if (ToDoTaskType.BATCH_SCHEDULE_START == toDoTaskType
                || ToDoTaskType.BATCH_SCHEDULE_STOP == toDoTaskType
                || ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE == toDoTaskType
                || ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME == toDoTaskType
                || ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP == toDoTaskType
                || ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START == toDoTaskType)
        {
            roleDestination = RoleType.PLATFORM_ADMIN;
        }
        //  If release have enabled autoManaged in PRO, destination always are for POwner
        if (deploymentPlan.getReleaseVersion().getRelease().isEnabledAutomanageInPro())
        {
            roleDestination = RoleType.PRODUCT_OWNER;
        }
        log.info("[ServiceRunnerImpl] -> [getProductionTaskDestinationRole]: Plan: [{}] with autoManageInPro [{}],Todo Task of type [{}], destination role: [{}]",
                deploymentPlan.getId(), deploymentPlan.getReleaseVersion().getRelease().isEnabledAutomanageInPro(), toDoTaskType, roleDestination);

        return roleDestination;
    }

    @Override
    @Transactional
    public void processTask(final ManagementActionTask task) throws NovaException
    {
        Integer relatedId = task.getRelatedId();
        DeploymentInstance instance;
        DeploymentService service;
        DeploymentSubsystem subsystem;
        DeploymentPlan plan;
        switch (task.getTaskType())
        {
            case CONTAINER_START:
                instance = deploymentInstanceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchContainerError(),
                                "container [" + relatedId + "] not found"));
                startInstance(task.getCreationUserCode(), instance);
                break;
            case CONTAINER_RESTART:
                instance = deploymentInstanceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchContainerError(),
                                "container [" + relatedId + "] not found"));
                restartInstance(task.getCreationUserCode(), instance);
                break;
            case CONTAINER_STOP:
                instance = deploymentInstanceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchContainerError(),
                                "container [" + relatedId + "] not found"));
                stopInstance(task.getCreationUserCode(), instance);
                break;
            case SERVICE_START:
                service = deploymentServiceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(relatedId)));
                startService(task.getCreationUserCode(), service);
                break;
            case SERVICE_RESTART:
                service = deploymentServiceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(relatedId)));
                restartService(task.getCreationUserCode(), service);
                break;
            case SERVICE_STOP:
                service = deploymentServiceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(relatedId)));
                stopService(task.getCreationUserCode(), service);
                break;
            case SUBSYSTEM_START:
                subsystem = deploymentSubsystemRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(relatedId)));
                startSubsystem(task.getCreationUserCode(), subsystem);
                break;
            case SUBSYSTEM_RESTART:
                subsystem = deploymentSubsystemRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(relatedId)));
                restartSubsystem(task.getCreationUserCode(), subsystem);
                break;
            case SUBSYSTEM_STOP:
                subsystem = deploymentSubsystemRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(relatedId)));
                stopSubsystem(task.getCreationUserCode(), subsystem);
                break;
            case RELEASE_START:
                plan = deploymentPlanRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(relatedId)));
                startDeploymentPlan(task.getCreationUserCode(), plan);
                break;
            case RELEASE_RESTART:
                plan = deploymentPlanRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(relatedId)));
                restartDeploymentPlan(task.getCreationUserCode(), plan);
                break;
            case RELEASE_STOP:
                plan = deploymentPlanRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(relatedId)));
                stopDeploymentPlan(task.getCreationUserCode(), plan);
                break;
            case BATCH_SCHEDULE_START:
                service = deploymentServiceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(relatedId)));
                startBatchSchedule(task.getCreationUserCode(), service);
                break;
            case BATCH_SCHEDULE_STOP:
                service = deploymentServiceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(relatedId)));
                stopBatchSchedule(task.getCreationUserCode(), service);
                break;
            case BATCH_SCHEDULE_INSTANCE_START:
                service = deploymentServiceRepository.findById(relatedId)
                        .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(relatedId)));
                startBatchScheduleInstance(task.getCreationUserCode(), service, service.getDeploymentSubsystem().getDeploymentPlan());
                break;
            case BATCH_SCHEDULE_INSTANCE_STOP:
                stopBatchScheduleInstance(task.getCreationUserCode(), relatedId);
                break;
            case BATCH_SCHEDULE_INSTANCE_PAUSE:
                pauseBatchScheduleInstance(task.getCreationUserCode(), relatedId);
                break;
            case BATCH_SCHEDULE_INSTANCE_RESUME:
                resumeBatchScheduleInstance(task.getCreationUserCode(), relatedId);
                break;

        }
    }

    /**
     * Starts a whole {@link DeploymentPlan}: all of its subsystems, services and instances.
     *
     * @param userCode       - {@code PortalUser} who invokes the operation
     * @param deploymentPlan - {@code DeploymentPlan} to restart
     */
    public void restartDeploymentPlan(final String userCode, final DeploymentPlan deploymentPlan)
    {

        String env = deploymentPlan.getEnvironment();
        // Check permissions
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.RESTART_PLAN_PERMISSION, env, productId, PERMISSION_DENIED);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());

        this.deploymentManagerService.restartPlan(deploymentPlan);
        log.debug(
                "Plan {} of product {} has been restarted by user {}",
                deploymentPlan.getId(),
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getName(),
                userCode
        );
        // History
        this.addChange(deploymentPlan, userCode, ChangeType.RESTART_PLAN, "Se han reiniciado el plan de despliegue");

        List<ServiceExecutionHistory> serviceExecutionHistories = getServiceExecutionHistoriesForPlan(deploymentPlan);
        if (!serviceExecutionHistories.isEmpty())
        {
            serviceExecutionHistoryRepository.saveAll(serviceExecutionHistories);
        }

        // Emit Restart Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_PLAN, ActivityAction.RESTARTED)
                .entityId(deploymentPlan.getId())
                .environment(env)
                .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", deploymentPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .build());
    }

    /**
     * Restarts the given instance.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param instance - DeploymentInstance to restart
     */
    public void restartInstance(final String userCode, final DeploymentInstance instance)
    {
        DeploymentPlan plan = instance.getService().getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.RESTART_INSTANCE_PERMISSION, env, productId, PERMISSION_DENIED);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentService(instance.getService(), instance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment());

        if (PlatformUtils.isPlanDeployedInEther(plan))
        {
            log.debug("Generating Ether Deployment DTO to request for instances start: " + "-Service used to generate it [{}]", instance.getService());

            // 1.Obtener datos para la creacion de la llamada a Ether Manager
            EtherDeploymentDTO etherDeploymentDTO = this.deploymentUtils.buildEtherDeploymentDTO(instance.getService(), CallbackService.RESTART, CallbackService.RESTART_ERROR);

            log.debug("Generated Ether Deployment DTO to request for instances start: " + "-DTO generated: {}", etherDeploymentDTO);


            // 2.Llamar al servicio EtherManager con la info necesaria
            this.etherManagerClient.restartEtherService(etherDeploymentDTO);

        }
        else
        {
            final NovaDeploymentInstance novaDeploymentInstance = HibernateUtils.getNovaDeploymentInstanceFromHibernateProxy(instance);

            this.deploymentManagerService.restartInstance(instance);
            DeploymentService service = instance.getService();
            log.debug("Instance {} of service {} has been restarted by user {}", novaDeploymentInstance.getClusterName(), service.getService().getServiceName(), userCode);
            // History
            this.addChange(service.getDeploymentSubsystem().getDeploymentPlan(), userCode, ChangeType.RESTART_INSTANCE,
                    "Se ha reiniciado la instancia con nombre de servicio completo: [" + instance.getService().getService().getFinalName() + "] de tipo: [" + instance.getService().getService().getServiceType()
                            + "] con nombre de contenedor: [" + novaDeploymentInstance.getContainerName() + "] pertenenciente al JOB del subsistema: [" + instance.getService().getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");
        }

        ServiceExecutionHistory serviceExecutionHistory = getServiceExecutionHistoryForInstance(instance, env);
        serviceExecutionHistoryRepository.save(serviceExecutionHistory);

        // Emit Restart Deployment Service Instance Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE_INSTANCE, ActivityAction.RESTARTED)
                .entityId(instance.getId())
                .environment(env)
                .addParam("deploymentServiceId", instance.getService().getId())
                .addParam("deploymentInstanceType", instance.getType().getDeploymentInstanceType())
                .addParam("serviceName", instance.getService().getService().getServiceName())
                .addParam("finalName", instance.getService().getService().getFinalName())
                .addParam("serviceType", instance.getService().getService().getServiceType())
                .addParam("tagName", instance.getService().getDeploymentSubsystem().getSubsystem().getTagName())
                .addParam("CompilationJobName", instance.getService().getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .build());
    }

    /**
     * Restarts a whole subsystem: all of its services and the instances belonging to them.
     *
     * @param userCode  - {@code PortalUser} who invokes the operation
     * @param subsystem - {@code DeploymentSubsystem} to restart
     */
    public void restartSubsystem(final String userCode, final DeploymentSubsystem subsystem)
    {
        DeploymentPlan plan = subsystem.getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.RESTART_SUBSYSTEM_PERMISSION, env, productId, PERMISSION_DENIED);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentSubsystem(subsystem, subsystem.getDeploymentPlan().getEnvironment());

        TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());

        this.deploymentManagerService.restartSubsystem(subsystem);
        log.debug(
                "Subsystem {} of plan {} has been restarted by user {}",
                subsystemDTO.getSubsystemName(),
                subsystem.getDeploymentPlan().getId(),
                userCode
        );
        this.addChange(
                subsystem.getDeploymentPlan(),
                userCode,
                ChangeType.RESTART_SUBSYSTEM,
                "Se ha reiniciado el subsistema pertenenciente al JOB del subsistema: [" + subsystem.getSubsystem().getCompilationJobName() + "]");

        List<ServiceExecutionHistory> serviceExecutionHistories = getServiceExecutionHistoriesForSubsystem(subsystem, env);
        if (!serviceExecutionHistories.isEmpty())
        {
            serviceExecutionHistoryRepository.saveAll(serviceExecutionHistories);
        }

        // Emit Restart Deployment Subsystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SUBSYSTEM, ActivityAction.RESTARTED)
                .entityId(subsystem.getId())
                .environment(env)
                .addParam("releaseVersionName", subsystem.getSubsystem().getReleaseVersion().getVersionName())
                .addParam("TagName", subsystem.getSubsystem().getTagName())
                .addParam("CompilationJobName", subsystem.getSubsystem().getCompilationJobName())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .build());
    }

    /**
     * Restarts all instances of a single service.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to restart
     */
    public void restartService(final String userCode, final DeploymentService service)
    {
        DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.RESTART_SERVICE_PERMISSION, env, productId, PERMISSION_DENIED);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentService(service, service.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());

        this.deploymentManagerService.restartService(service);
        log.debug(
                "Service {} of plan {} has been started by user {}",
                service.getService().getServiceName(),
                service.getDeploymentSubsystem().getDeploymentPlan().getId(),
                userCode
        );
        this.addChange(
                service.getDeploymentSubsystem().getDeploymentPlan(),
                userCode,
                ChangeType.RESTART_SERVICE,
                "Se ha reiniciado el servicio con nombre: [" + service.getService().getServiceName() + "]-[" + service.getService().getFinalName() + "] de tipo: [" + service.getService().getServiceType()
                        + "] con nombre de imagen: [" + service.getService().getImageName() + "] pertenenciente al JOB del subsistema: [" + service.getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");

        ServiceExecutionHistory serviceExecutionHistory = getServiceExecutionHistoryForService(service, env);
        if (serviceExecutionHistory != null)
        {
            serviceExecutionHistoryRepository.save(serviceExecutionHistory);
        }

        // Emit Restart Deployment Service Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.RESTARTED)
                .entityId(service.getId())
                .environment(env)
                .addParam("serviceName", service.getService().getServiceName())
                .addParam("serviceType", service.getService().getServiceType())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .addParam("tagName", service.getDeploymentSubsystem().getSubsystem().getTagName())
                .addParam("CompilationJobName", service.getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("finalName", service.getService().getFinalName())
                .build());
    }

    /**
     * Starts an instance of a batch schedule.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to start
     */
    public void startBatchSchedule(final String userCode, DeploymentService service)
    {
        DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_BATCH_SCHEDULE_PERMISSION, env, productId, PERMISSION_DENIED);

        // Call service depending of the service type. Batch schedule activate the batch schedule service. Start service (start containers of the service) in any case
        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType()))
        {
            // Call schedule service
            this.schedulerManagerClient.updateDeploymentBatchSchedule(service.getService().getId(), plan.getId(), DeploymentBatchScheduleStatus.ENABLED);

            // History
            this.addChange(
                    service.getDeploymentSubsystem().getDeploymentPlan(),
                    userCode,
                    ChangeType.START_SERVICE,
                    "Se ha activado el servicio con nombre: [" + service.getService() + "] de tipo: [" + service.getService().getServiceType()
                            + "] pertenenciente al JOB del subsistema: [" + service.getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");
        }

        // Emit Active Deployment Service Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.ACTIVED)
                .entityId(service.getId())
                .environment(env)
                .addParam("serviceName", service.getService().getServiceName())
                .addParam("serviceType", service.getService().getServiceType())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .addParam("finalName", service.getService().getFinalName())
                .addParam("CompilationJobName", service.getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("tagName", service.getDeploymentSubsystem().getSubsystem().getTagName())
                .build());
    }

    /**
     * Stops an instance of a batch schedule.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to stop
     */
    public void stopBatchSchedule(final String userCode, final DeploymentService service)
    {
        DeploymentPlan plan = service.getDeploymentSubsystem().getDeploymentPlan();
        String env = plan.getEnvironment();
        // Check permissions
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.STOP_BATCH_SCHEDULE_PERMISSION, env, productId, PERMISSION_DENIED);

        // Call service depending of the service type. Batch schedule activate the batch schedule service. If not, stop service (stop containers of the service) in any case
        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType()))
        {
            // Call schedule manager
            this.schedulerManagerClient.updateDeploymentBatchSchedule(service.getService().getId(), plan.getId(), DeploymentBatchScheduleStatus.DISABLED);

            // History
            this.addChange(
                    service.getDeploymentSubsystem().getDeploymentPlan(),
                    userCode,
                    ChangeType.STOP_SERVICE,
                    "Se ha desactivado el servicio con nombre: [" + service.getService() + "] de tipo: [" + service.getService().getServiceType()
                            + "] pertenenciente al JOB del subsistema: [" + service.getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");
        }

        // Emit Deactive Deployment Service Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.DEACTIVED)
                .entityId(service.getId())
                .environment(env)
                .addParam("serviceName", service.getService().getServiceName())
                .addParam("serviceType", service.getService().getServiceType())
                .addParam("DeploymentPlanId", plan.getId())
                .addParam("releaseVersionName", plan.getReleaseVersion().getVersionName())
                .addParam("releaseName", plan.getReleaseVersion().getRelease().getName())
                .addParam("finalName", service.getService().getFinalName())
                .addParam("CompilationJobName", service.getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("tagName", service.getDeploymentSubsystem().getSubsystem().getTagName())
                .build());
    }

    @Override
    @Transactional
    public void stopBatchScheduleInstance(final String userCode, Integer scheduleInstanceId)
    {
        // Call service
        DeploymentBatchScheduleInstanceDTO deploymentBatchScheduleInstanceDTO = this.schedulerManagerClient.stateBatchScheduleInstance(scheduleInstanceId, BatchSchedulerInstanceStatus.STOP_INSTANCE);

        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentBatchScheduleInstanceDTO.getDeploymentPlanId())
                .orElseThrow(() -> new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + deploymentBatchScheduleInstanceDTO.getDeploymentPlanId()));

        // Get service associated into the plan
        String serviceName = Constants.NOT_AVAILABLE_VALUE;
        String finalName = Constants.NOT_AVAILABLE_VALUE;
        String tagName = Constants.NOT_AVAILABLE_VALUE;
        String compilationJobName = Constants.NOT_AVAILABLE_VALUE;

        ReleaseVersionService releaseVersionService = this.releaseVersionServiceRepository.findById(deploymentBatchScheduleInstanceDTO.getReleaseVersionServiceId()).orElse(null);
        if (releaseVersionService != null)
        {
            serviceName = releaseVersionService.getServiceName();
            finalName = releaseVersionService.getFinalName();
            tagName = releaseVersionService.getVersionSubsystem().getTagName();
            compilationJobName = releaseVersionService.getVersionSubsystem().getCompilationJobName();
        }

        // History
        this.addChange(
                deploymentPlan,
                userCode,
                ChangeType.STOP_INSTANCE,
                "Se ha parado la instancia con id de planificacion: [" + scheduleInstanceId + "] de planificación del servicio con nombre: [" + serviceName + "] de tipo: [" + ServiceType.BATCH_SCHEDULER_NOVA.name()
                        + "] pertenenciente al JOB del subsistema: [" + compilationJobName + "]");

        // Emit Stop Batch Schedule Instance Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.STOPPED)
                .entityId(scheduleInstanceId)
                .environment(deploymentPlan.getEnvironment())
                .addParam("deploymentPlanId", deploymentPlan.getId())
                .addParam("serviceType", ChangeType.STOP_INSTANCE)
                .addParam("DeploymentPlanId", deploymentPlan.getId())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .addParam("serviceName", serviceName)
                .addParam("finalName", finalName)
                .addParam("CompilationJobName", compilationJobName)
                .addParam("tagName", tagName)
                .addParam("ScheduleInstanceId", scheduleInstanceId)
                .build());
    }

    @Override
    @Transactional
    public void resumeBatchScheduleInstance(final String userCode, final Integer scheduleInstanceId)
    {
        // Call service
        DeploymentBatchScheduleInstanceDTO deploymentBatchScheduleInstanceDTO = this.schedulerManagerClient.stateBatchScheduleInstance(scheduleInstanceId, BatchSchedulerInstanceStatus.RESUME_INSTANCE);

        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentBatchScheduleInstanceDTO.getDeploymentPlanId())
                .orElseThrow(() -> new NovaException(ServiceRunnerError.getNoSuchDeploymentError(),
                        "Deployment plan not found: " + deploymentBatchScheduleInstanceDTO.getDeploymentPlanId()));

        // Get service associated into the plan
        String serviceName = Constants.NOT_AVAILABLE_VALUE;
        String finalName = Constants.NOT_AVAILABLE_VALUE;
        String tagName = Constants.NOT_AVAILABLE_VALUE;
        String compilationJobName = Constants.NOT_AVAILABLE_VALUE;

        ReleaseVersionService releaseVersionService = this.releaseVersionServiceRepository.findById(deploymentBatchScheduleInstanceDTO.getReleaseVersionServiceId()).orElse(null);
        if (releaseVersionService != null)
        {
            serviceName = releaseVersionService.getServiceName();
            finalName = releaseVersionService.getFinalName();
            tagName = releaseVersionService.getVersionSubsystem().getTagName();
            compilationJobName = releaseVersionService.getVersionSubsystem().getCompilationJobName();
        }

        // History
        this.addChange(
                deploymentPlan,
                userCode,
                ChangeType.RESTART_INSTANCE,
                "Se ha relanzado la instancia con id de planificacion: [" + scheduleInstanceId + "] del servicio con nombre: [" + serviceName + "] de tipo: [" + ServiceType.BATCH_SCHEDULER_NOVA.name()
                        + "] pertenenciente al JOB del subsistema: [" + compilationJobName + "]");

        // Emit Restart Batch Schedule Instance Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.RESTARTED)
                .entityId(scheduleInstanceId)
                .environment(deploymentPlan.getEnvironment())
                .addParam("deploymentPlanId", deploymentPlan.getId())
                .addParam("serviceType", ChangeType.RESTART_INSTANCE)
                .addParam("DeploymentPlanId", deploymentPlan.getId())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .addParam("serviceName", serviceName)
                .addParam("finalName", finalName)
                .addParam("CompilationJobName", compilationJobName)
                .addParam("tagName", tagName)
                .addParam("ScheduleInstanceId", scheduleInstanceId)
                .build());
    }

    @Override
    @Transactional
    public void pauseBatchScheduleInstance(final String userCode, final Integer scheduleInstanceId)
    {
        // Call service
        DeploymentBatchScheduleInstanceDTO deploymentBatchScheduleInstanceDTO = this.schedulerManagerClient.stateBatchScheduleInstance(scheduleInstanceId, BatchSchedulerInstanceStatus.PAUSE_INSTANCE);

        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentBatchScheduleInstanceDTO.getDeploymentPlanId()).orElse(null);
        if (deploymentPlan == null)
        {
            throw new NovaException(ServiceRunnerError.getNoSuchDeploymentError(), "Deployment plan not found: " + deploymentBatchScheduleInstanceDTO.getDeploymentPlanId());
        }

        // Get service associated into the plan
        String serviceName = Constants.NOT_AVAILABLE_VALUE;
        String finalName = Constants.NOT_AVAILABLE_VALUE;
        String tagName = Constants.NOT_AVAILABLE_VALUE;
        String compilationJobName = Constants.NOT_AVAILABLE_VALUE;

        ReleaseVersionService releaseVersionService = this.releaseVersionServiceRepository.findById(deploymentBatchScheduleInstanceDTO.getReleaseVersionServiceId()).orElse(null);
        if (releaseVersionService != null)
        {
            serviceName = releaseVersionService.getServiceName();
            finalName = releaseVersionService.getFinalName();
            tagName = releaseVersionService.getVersionSubsystem().getTagName();
            compilationJobName = releaseVersionService.getVersionSubsystem().getCompilationJobName();
        }

        // History
        this.addChange(
                deploymentPlan,
                userCode,
                ChangeType.PAUSE_INSTANCE,
                "Se ha parado la instancia con id de planificacion: [" + scheduleInstanceId + "] del servicio con nombre: [" + serviceName + "] de tipo: [" + ServiceType.BATCH_SCHEDULER_NOVA.name()
                        + "] pertenenciente al JOB del subsistema: [" + compilationJobName + "]");

        // Emit Pause Batch Schedule Instance Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.PAUSED)
                .entityId(scheduleInstanceId)
                .environment(deploymentPlan.getEnvironment())
                .addParam("deploymentPlanId", deploymentPlan.getId())
                .addParam("serviceType", ChangeType.PAUSE_INSTANCE)
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .addParam("serviceName", serviceName)
                .addParam("finalName", finalName)
                .addParam("CompilationJobName", compilationJobName)
                .addParam("tagName", tagName)
                .addParam("ScheduleInstanceId", scheduleInstanceId)
                .build());
    }

    @Override
    public void startBatchScheduleInstance(final String userCode, final DeploymentService deploymentService, final DeploymentPlan plan)
    {
        String env = plan.getEnvironment();
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(userCode, ServiceRunnerConstants.START_BATCH_SCHEDULE_INSTANCE_PERMISSION, env, productId, PERMISSION_DENIED);

        this.schedulerManagerClient.startScheduleInstance(deploymentService.getService().getId(), plan.getId());

        // History
        this.addChange(plan, userCode, ChangeType.START_INSTANCE,
                "Se ha arrancado una instancia de planificación del servicio con nombre: [" + deploymentService.getService().getServiceName() + "] de tipo: [" + ServiceType.BATCH_SCHEDULER_NOVA.name()
                        + "] pertenenciente al JOB del subsistema: [" + deploymentService.getDeploymentSubsystem().getSubsystem().getCompilationJobName() + "]");

        // Emit Start Batch Schedule Instance Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productId, ActivityScope.BATCH_SCHEDULE_INSTANCE, ActivityAction.STARTED)
                .entityId(deploymentService.getService().getId())
                .environment(env)
                .addParam("deploymentPlanId", plan.getId())
                .addParam("serviceName", deploymentService.getService().getServiceName())
                .addParam("serviceType", ChangeType.START_INSTANCE)
                .addParam("releaseVersionName", deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getName())
                .addParam("serviceName", deploymentService.getService().getServiceName())
                .addParam("finalName", deploymentService.getService().getFinalName())
                .addParam("CompilationJobName", deploymentService.getDeploymentSubsystem().getSubsystem().getCompilationJobName())
                .addParam("tagName", deploymentService.getDeploymentSubsystem().getSubsystem().getTagName())
                .build());
    }

    /**
     * Create a deployment change of the specified type and add it to the deployment changes of the plan.
     *
     * @param plan     - the deployment plan
     * @param userCode - user who request the change
     * @param type     - {@code ChangeType}
     * @param message  - Descriptive message of the change
     */
    private void addChange(final DeploymentPlan plan, final String userCode, final ChangeType type, final String message)
    {
        DeploymentChange change = new DeploymentChange(plan, type, message);
        change.setUserCode(userCode);
        deploymentChangeRepository.saveAndFlush(change);
    }

    /**
     * Check if user is Control-M and try to set Enable every BatchScheduler service into the plan automatically
     *
     * @param userCode         user to check if it is an impersonal user (control-M)
     * @param deploymentPlanId plan id with deployment services.
     * @param subsystems       list of subsystem started
     */
    private void autoEnableBatchScheduleByControlMScript(final String userCode,
                                                         final int deploymentPlanId,
                                                         final List<DeploymentSubsystem> subsystems)
    {
        //check for Control M user IMM0589
        if (StringUtils.equals(userCode, Constants.IMMUSER))
        {
            log.info("DeploymentPlan started by Control-M, user = [{}]", userCode);

            //collect all services into this deploymentPlan
            List<DeploymentService> flatDeploymentServicesList = subsystems.stream()
                    .map(DeploymentSubsystem::getDeploymentServices)
                    .flatMap(List::stream)
                    .filter(service -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getService().getServiceType())) //only batch scheduler services
                    .collect(Collectors.toList());

            int serviceId;
            for (DeploymentService service : flatDeploymentServicesList)
            {
                serviceId = service.getService().getId();
                log.info("BatchScheduler service [{}] is auto enabled by Control-M, user = [{}]", serviceId, userCode);
                // Call schedule service
                this.schedulerManagerClient.updateDeploymentBatchSchedule(serviceId, deploymentPlanId, DeploymentBatchScheduleStatus.ENABLED);
            }
        }
    }
}
