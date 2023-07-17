package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.*;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerProperty;
import com.bbva.enoa.datamodel.model.broker.entities.DeploymentBrokerProperty;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationValue;
import com.bbva.enoa.datamodel.model.config.entities.PropertyDefinition;
import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.MailServiceConstants;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for creating {@link DeploymentPlan} and {@link ConfigurationRevision}.
 */
@Slf4j
@Service
public class DeploymentsServiceImpl implements IDeploymentsService
{
    /**
     * Deployer service
     */
    private final IDeployerService deployerService;
    /**
     * Rmo
     */
    private final IRemoverService removerService;
    /**
     * Deployment Migrator service
     */
    private final IDeploymentMigrator migrator;
    /**
     * Library Manager service
     */
    private final ILibraryManagerService libraryManagerService;
    /**
     * ApiGW service
     */
    private final IApiGatewayService apiGatewayService;
    /**
     * utils
     */
    private final DeploymentUtils deploymentUtils;
    /**
     * DeploymentPlan cloner
     */
    private final IDeploymentPlanCloner deploymentPlanCloner;
    /**
     * Deployment plan copier
     */
    private final IDeploymentPlanCopier deploymentPlanCopier;
    /**
     * validator
     */
    private final IDeploymentsValidator deploymentsValidator;
    /**
     * Entity Manager
     */
    private final EntityManager entityManager;
    /**
     * RepositoryManager
     */
    private final IRepositoryManagerService repositoryManagerService;
    /**
     * repository of GCSP
     */
    private final DeploymentGcspRepo gcspRepo;
    /**
     * repository DeploymentNova
     */
    private final DeploymentNovaRepo novaRepo;
    /**
     * Toolsclient
     */
    private final IToolsClient toolsClient;
    /**
     * Scheduler manager client
     */
    private final ISchedulerManagerClient schedulerManagerClient;
    /**
     * Configuration revision repository
     */
    private final ConfigurationRevisionRepository confRevisionRepo;
    /**
     * repository of HardwarePack
     */
    private final HardwarePackRepository hardwarePackRepository;
    /**
     * repository of FileSystemApi
     */
    private final FilesystemRepository filesystemsApiRepository;
    /**
     * repository of DeploymentPlan
     */
    private final DeploymentPlanRepository deploymentPlanRepository;
    /**
     * Repository of ReleaseVersion
     */
    private final ReleaseVersionRepository versionRepository;
    /**
     * repository of deploymentTask
     */
    private final DeploymentTaskRepository deploymentTaskRepository;
    /**
     * Repository of logical connector
     */
    private final LogicalConnectorRepository logicalConnectorRepository;
    /**
     * repository of ManagementActionTask
     */
    private final ManagementActionTaskRepository managementActionTaskRepository;
    /**
     * DeploymentServiceRepository
     */
    private final DeploymentServiceRepository serviceRepo;
    /**
     * DeploymentChangeRepository
     */
    private final DeploymentChangeRepository changeRepo;
    /**
     * Product repository
     */
    private final ProductRepository productRepository;
    /**
     * User client
     */
    private final IUsersClient usersClient;
    /**
     * Mail service Client
     */
    private final MailServiceClient mailServiceClient;
    /**
     * Plan profiling utils
     */
    private final PlanProfilingUtils planProfilingUtils;
    /**
     * Profiling utils
     */
    private final ProfilingUtils profilingUtils;
    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;
    /**
     * Spring Environment
     */
    private final org.springframework.core.env.Environment springEnvironment;
    private final IDeploymentServiceJdkParametersSetter jdkParametersSetter;
    private final DeploymentServiceAllowedJdkParameterValueRepository jvmParamValuesRepository;
    /**
     * Deployment broker service to manage all related with brokes in a deploymentPlan
     */
    private final IDeploymentBroker deploymentBroker;
    /**
     * Alert service client
     */
    private final IAlertServiceApiClient alertServiceApiClient;
    /**
     * Broker propertyRepository
     */
    private final BrokerPropertyRepository brokerPropertyRepository;
    /**
     * Task Service Client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;
    private final ConfigurationTaskRepository configurationTaskRepository;
    private final JmxParameterRepository jmxParameterRepository;
    /**
     * Number of deployment plans to manage
     */
    @Value("${nova.maxDeployments:10}")
    private int maxdeployments;

    /**
     * Constructor by param
     *
     * @param deployerService                deployer service
     * @param removerService                 remover service
     * @param migrator                       migrartor service
     * @param libraryManagerService          LibraryManager service
     * @param apiGatewayService              ApiGateway service
     * @param deploymentUtils                Deployment Utils
     * @param deploymentPlanCloner           deploymentPlan cloner
     * @param deploymentPlanCopier           deploymentPlan copier
     * @param deploymentsValidator           Validator
     * @param entityManager                  Entity manager
     * @param repositoryManagerService       repository service
     * @param gcspRepo                       GCSP repository
     * @param novaRepo                       NOVA repository
     * @param toolsClient                    Tools client
     * @param schedulerManagerClient         scheduleManager client
     * @param confRevisionRepo               ConfigurationRevision repository
     * @param hardwarePackRepository         HardwarPack repository
     * @param filesystemsApiRepository       FileSystem repository
     * @param deploymentPlanRepository       deploymentPlan repository
     * @param versionRepository              ReleaseVersion repository
     * @param deploymentTaskRepository       DeploymentTask repository
     * @param logicalConnectorRepository     logicalConnector repository
     * @param managementActionTaskRepository ManagementAction repository
     * @param serviceRepo                    Service repository
     * @param changeRepo                     Change repository
     * @param productRepository              Product repository
     * @param usersClient                    users client
     * @param planProfilingUtils             plan profiling utils
     * @param profilingUtils                 profiling utils
     * @param mailServiceClient              the mail service client
     * @param novaActivityEmitter            NovaActivity emitter
     * @param jvmParamValuesRepository       Assigned JVM parameters repository
     * @param deploymentBroker               deploymentBroker
     * @param todoTaskServiceClient          todoTask client
     * @param alertServiceApiClient          The {@link IAlertServiceApiClient} current implementation
     * @param brokerPropertyRepository       Broker Property repository
     * @param configurationTaskRepository    Configuration Task Repository
     * @param jmxParameterRepository         JMX Parameter Repository
     */
    @Autowired
    public DeploymentsServiceImpl(final IDeployerService deployerService, final IRemoverService removerService,
                                  final IDeploymentMigrator migrator, final ILibraryManagerService libraryManagerService,
                                  final IApiGatewayService apiGatewayService, final DeploymentUtils deploymentUtils, final IDeploymentPlanCloner deploymentPlanCloner,
                                  final IDeploymentPlanCopier deploymentPlanCopier, final IDeploymentsValidator deploymentsValidator, final EntityManager entityManager,
                                  final IRepositoryManagerService repositoryManagerService, final DeploymentGcspRepo gcspRepo,
                                  final DeploymentNovaRepo novaRepo, final IToolsClient toolsClient,
                                  final ISchedulerManagerClient schedulerManagerClient, final ConfigurationRevisionRepository confRevisionRepo,
                                  final HardwarePackRepository hardwarePackRepository, final FilesystemRepository filesystemsApiRepository,
                                  final DeploymentPlanRepository deploymentPlanRepository, final ReleaseVersionRepository versionRepository,
                                  final DeploymentTaskRepository deploymentTaskRepository, final LogicalConnectorRepository logicalConnectorRepository,
                                  final ManagementActionTaskRepository managementActionTaskRepository, final DeploymentServiceRepository serviceRepo,
                                  final DeploymentChangeRepository changeRepo, final ProductRepository productRepository,
                                  final IUsersClient usersClient, final PlanProfilingUtils planProfilingUtils,
                                  final ProfilingUtils profilingUtils, final MailServiceClient mailServiceClient,
                                  final INovaActivityEmitter novaActivityEmitter, final org.springframework.core.env.Environment springEnvironment,
                                  final IDeploymentServiceJdkParametersSetter jdkParametersSetter,
                                  final DeploymentServiceAllowedJdkParameterValueRepository jvmParamValuesRepository,
                                  final IDeploymentBroker deploymentBroker, final TodoTaskServiceClient todoTaskServiceClient,
                                  final IAlertServiceApiClient alertServiceApiClient, final BrokerPropertyRepository brokerPropertyRepository,
                                  final ConfigurationTaskRepository configurationTaskRepository, final JmxParameterRepository jmxParameterRepository)

    {
        this.deployerService = deployerService;
        this.removerService = removerService;
        this.migrator = migrator;
        this.libraryManagerService = libraryManagerService;
        this.apiGatewayService = apiGatewayService;
        this.deploymentUtils = deploymentUtils;
        this.deploymentPlanCloner = deploymentPlanCloner;
        this.deploymentPlanCopier = deploymentPlanCopier;
        this.deploymentsValidator = deploymentsValidator;
        this.entityManager = entityManager;
        this.repositoryManagerService = repositoryManagerService;
        this.gcspRepo = gcspRepo;
        this.novaRepo = novaRepo;
        this.toolsClient = toolsClient;
        this.schedulerManagerClient = schedulerManagerClient;
        this.confRevisionRepo = confRevisionRepo;
        this.hardwarePackRepository = hardwarePackRepository;
        this.filesystemsApiRepository = filesystemsApiRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.versionRepository = versionRepository;
        this.deploymentTaskRepository = deploymentTaskRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.managementActionTaskRepository = managementActionTaskRepository;
        this.serviceRepo = serviceRepo;
        this.changeRepo = changeRepo;
        this.productRepository = productRepository;
        this.usersClient = usersClient;
        this.planProfilingUtils = planProfilingUtils;
        this.profilingUtils = profilingUtils;
        this.mailServiceClient = mailServiceClient;
        this.novaActivityEmitter = novaActivityEmitter;
        this.springEnvironment = springEnvironment;
        this.jdkParametersSetter = jdkParametersSetter;
        this.jvmParamValuesRepository = jvmParamValuesRepository;
        this.deploymentBroker = deploymentBroker;
        this.alertServiceApiClient = alertServiceApiClient;
        this.brokerPropertyRepository = brokerPropertyRepository;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.configurationTaskRepository = configurationTaskRepository;
        this.jmxParameterRepository = jmxParameterRepository;

    }

    /////////////////////////////////////////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////////////////

    @Override
    public void addDeploymentChange(DeploymentChangeDto change, int deploymentPlanId)
    {

        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentPlanId).orElse(null);
        DeploymentChange depChange = new DeploymentChange();
        depChange.setDeploymentPlan(plan);
        depChange.setCreationDate(new GregorianCalendar());
        depChange.getCreationDate().setTimeInMillis(change.getDate());
        if (change.getConfRevisionId() != null)
        {
            ConfigurationRevision confChange = this.confRevisionRepo.findById(change.getConfRevisionId()).orElse(null);
            depChange.setConfigurationRevision(confChange);
        }
        depChange.setDescription(change.getConfRevisionDesc());
        depChange.setRefId(change.getRefId());
        depChange.setType(ChangeType.valueOf(change.getTypeChange()));
        depChange.setUserCode(change.getUserCode());
        changeRepo.saveAndFlush(depChange);
    }

    @Override
    @Transactional
    public void archivePlan(int deploymentId)
    {
        log.debug("archivePlan the Deployment plan ID: [{}]", deploymentId);
        // Get the plan.
        DeploymentPlan originalPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "The deployment plan [" + deploymentId + "] not found"));

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(originalPlan);
        //Check stored release version
        deploymentsValidator.checkPlanNotStoragedAndNotReadyToDeploy(originalPlan);

        // If this plan does not have child is NOT relevant for archiving a Plan.
        originalPlan.setStatus(DeploymentStatus.STORAGED);

        this.deploymentPlanRepository.save(originalPlan);

        // Emit Archive Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(originalPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.ARCHIVED)
                .entityId(deploymentId)
                .environment(originalPlan.getEnvironment())
                .addParam("releaseVersionId", originalPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", originalPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", originalPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", originalPlan.getReleaseVersion().getRelease().getName())
                .build());
    }

    @Override
    public TodoTaskResponseDTO changeDeploymentType(final String userCode, int deploymentId, final DeploymentTypeChangeDto changeType)
    {
        // Get the deploymentPlan.
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        "The deployment plan [" + deploymentId + "] not found"));

        // Only plans never executed can be changed.
        if (DeploymentStatus.DEPLOYED == deploymentPlan.getStatus() || DeploymentStatus.UNDEPLOYED == deploymentPlan.getStatus())
        {
            throw new NovaException(DeploymentError.getTriedToUpdateDeployedPlanError());
        }

        // Deployment type cannot be changed if a request for that is pending yet.
        if (this.deploymentPlanRepository.planHasPendingDeploymentTypeChangeTask(deploymentId))
        {
            throw new NovaException(DeploymentError.getDeploymentTypeChangePendingError());
        }

        // task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        List<DeploymentTask> deploymentTaskList = this.deploymentTaskRepository.planPendingDeploymentTasks(deploymentId);

        if (!deploymentTaskList.isEmpty())
        {
            String message = "[DEPLOYMENTSAPI] -> [changeDeploymentType]: there are a deployment task pending for the plan id: [" + deploymentId + "] and cannot change the deployment type";
            log.error(message);
            throw new NovaException(DeploymentError.getDeploymentRequestPendingError(deploymentId, Arrays.toString(deploymentTaskList.toArray()), deploymentPlan.getEnvironment()), message);
        }

        // Set the change data.
        changeType.setDeploymentId(deploymentId);

        // Since 12/2020 don´t need todotask to change deployment Type
        // Modify the type of plan.
        String deploymentType = changeType.getDeploymentType();
        deploymentPlan.setDeploymentTypeInPro(DeploymentType.valueOf(deploymentType));
        // And save the plan.
        deploymentPlanRepository.save(deploymentPlan);
        // Create and Save history.
        this.deploymentUtils.addHistoryEntry(
                ChangeType.DEPLOYMENT_TYPE_CHANGE,
                deploymentPlan,
                userCode,
                "El tipo de despliegue de este plan de despliegue ha sido actualizado y ha cambiado a: " + deploymentType);

        // Emit Deployment Type Configured Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYMENT_TYPE_CHANGE)
                .entityId(deploymentId)
                .environment(deploymentPlan.getEnvironment())
                .addParam("deploymentTypeChange", deploymentType)
                .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                .addParam("deploymentPlanStatus", deploymentPlan.getStatus().getDeploymentStatus())
                .build());

        log.debug("[DeploymentsServiceImpl] -> [changeDeploymentType]: Changed deployment type by [{}] to [{}] for plan [{}]",
                userCode,
                deploymentType,
                deploymentId);

        return todoTaskResponseDTO;

    }

    @Override
    public DeploymentPlan copyPlan(final DeploymentPlan originalDeploymentPlan)
    {
        //Check stored
        this.deploymentsValidator.checkReleaseVersionStored(originalDeploymentPlan);

        //Check number of plans
        this.checkNumberOfPlans(originalDeploymentPlan.getEnvironment(), originalDeploymentPlan.getReleaseVersion().getRelease().getProduct().getId());

        DeploymentPlan copiedPlan = this.deploymentPlanCopier.copyPlan(originalDeploymentPlan);

        if (originalDeploymentPlan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            if (copiedPlan.getDeploymentTypeInPro().equals(DeploymentType.PLANNED))
            {
                copiedPlan.setGcsp(new DeploymentGcsp());
            }
            if (copiedPlan.getDeploymentTypeInPro().equals(DeploymentType.NOVA_PLANNED))
            {
                copiedPlan.setNova(new DeploymentNova());
            }
        }

        log.debug("Copied plan {} to plan {} with this values \n{}", originalDeploymentPlan.getId(), copiedPlan.getId(), copiedPlan.toJSON());

        return copiedPlan;
    }

    @Override
    @Transactional
    public DeploymentPlan createDeployment(final int releaseVersionId, final String environment,
                                           final boolean isMigrated) throws NovaException
    {
        log.debug("Creating a new deployment for release releaseVersion '{}' in environment {}", releaseVersionId, environment);

        // Get the release releaseVersion
        ReleaseVersion releaseVersion = this.versionRepository.findById(releaseVersionId)
                .orElseThrow(() -> new NovaException(DeploymentError.getUnexpectedError()));

        //Check number of plans
        this.checkNumberOfPlans(environment, releaseVersion.getRelease().getProduct().getId());

        // Check if the release releaseVersion status is ReadyToDeploy
        this.deploymentsValidator.checkReleaseVersionStatus(releaseVersionId, releaseVersion);

        // Create the deploymentPlan
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        deploymentPlan.setReleaseVersion(releaseVersion);
        Environment env = Environment.valueOf(environment);
        deploymentPlan.setEnvironment(env.getEnvironment());

        // Set deployment type on pro, when deployment is created in INT, always ON demand, this will be changed when promote to PRO.
        deploymentPlan.setDeploymentTypeInPro(DeploymentType.ON_DEMAND);

        deploymentPlan.setSelectedDeploy(PlatformUtils.getSelectedDeployForReleaseInEnvironment(releaseVersion.getRelease(), env));
        deploymentPlan.setEtherNs(PlatformUtils.getSelectedDeployNSForProductInEnvironment(releaseVersion.getRelease().getProduct(), env));
        deploymentPlan.setSelectedLogging(PlatformUtils.getSelectedLoggingForReleaseInEnvironment(releaseVersion.getRelease(), env));

        // Set if the deploymentPlan is mono o multiCPD and set the CPD depending of the value of the Product corresponding to the Release of this deployment plan
        deploymentPlan.setMultiCPDInPro(releaseVersion.getRelease().getProduct().getMultiCPDInPro());
        deploymentPlan.setCpdInPro(releaseVersion.getRelease().getProduct().getCPDInPro());

        // Store the deploymentPlan
        this.deploymentPlanRepository.save(deploymentPlan);

        // Create the initial default configuration.
        ConfigurationRevision revision = this.createInitialRevision(deploymentPlan);
        deploymentPlan.getRevisions().add(revision);
        deploymentPlan.setCurrentRevision(revision);

        // Create the subsystems and services attached to the deploymentPlan.
        this.addSubsystemsToPlan(deploymentPlan);

        // Add context params for batch schedule service
        if (!isMigrated)
        {
            this.createDeploymentContextParams(deploymentPlan);

            if (this.profilingUtils.isPlanExposingApis(deploymentPlan))
            {
                deploymentPlan.addPlanProfile(this.planProfilingUtils.createPlanProfile(deploymentPlan));
            }
        }

        log.debug("Created DeploymentPlan with values: \n{}", deploymentPlan.toJSON());

        this.deploymentPlanRepository.save(deploymentPlan);

        if (!isMigrated)
        {
            // Emit Create Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(releaseVersion.getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.CREATED)
                    .entityId(deploymentPlan.getId())
                    .environment(environment)
                    .addParam("releaseVersionId", releaseVersionId)
                    .addParam("deploymentstatus", deploymentPlan.getStatus().getDeploymentStatus())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .build());
        }

        return deploymentPlan;
    }

    @Override
    @Transactional
    public void deletePlan(int deploymentId) throws NovaException
    {
        // Get the plan.
        DeploymentPlan originalPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError()));

        // Only plans on definition or storaged can be deleted.
        if (DeploymentStatus.DEFINITION != originalPlan.getStatus()
                && DeploymentStatus.STORAGED != originalPlan.getStatus()
                && DeploymentStatus.REJECTED != originalPlan.getStatus())
        {
            throw new NovaException(DeploymentError.getTriedToDeleteDeployedPlanError());
        }

        log.debug("Checking that the Deployment plan ID: [{}] has not child. DeploymentPlan: [{}]", deploymentId, originalPlan);
        List<DeploymentPlan> deploymentPlanList = this.deploymentPlanRepository.findByParent(originalPlan);

        // Check this plan does not have child.
        if (!deploymentPlanList.isEmpty())
        {
            // Filter child plans which are in process (some action is being executed)
            final List<DeploymentPlan> childrenPlansInProcess = deploymentPlanList.stream()
                    .filter(plan -> Stream.of(DeploymentAction.READY, DeploymentAction.ERROR, DeploymentAction.INVALID)
                            .noneMatch(action -> action == plan.getAction()))
                    .collect(Collectors.toList());

            // If there is a child plan which has an action in process, the parent plan cannot be deleted, so exception is thrown
            if (!childrenPlansInProcess.isEmpty())
            {
                final String childrenInProcessAsText = childrenPlansInProcess.stream()
                        .map(deploymentPlan -> String.format("Plan %s in %s is %s", deploymentPlan.getId(), deploymentPlan.getEnvironment(), deploymentPlan.getAction()))
                        .collect(Collectors.joining(", "));

                throw new NovaException(DeploymentError.getTriedToDeleteDeploymentPlanWithProcessingChildren(originalPlan.getId(), childrenInProcessAsText));
            }

            for (DeploymentPlan plan : deploymentPlanList)
            {
                plan.setParent(originalPlan.getParent());
                this.deploymentPlanRepository.save(plan);
            }
        }

        // If the plan has an active profiling must be deleted
        if (originalPlan.getPlanProfiles().stream().anyMatch(planProfile -> ProfileStatus.ACTIVE.equals(planProfile.getStatus())))
        {
            this.apiGatewayService.removeProfiling(originalPlan);
        }

        //Set configurations to null if there is any
        if (originalPlan.getConfigurationTask() != null)
        {
            originalPlan.setConfigurationTask(null);
            this.deploymentPlanRepository.save(originalPlan);
        }
        // Delete the task associated previously
        this.deleteTasks(originalPlan);

        //Delete GCSP
        if (originalPlan.getGcsp() != null)
        {
            this.gcspRepo.deleteById(originalPlan.getGcsp().getId());
        }

        List<JmxParameter> jmxParameter = jmxParameterRepository.findAllByDeploymentPlanId(deploymentId);
        if (!jmxParameter.isEmpty())
        {
            this.jmxParameterRepository.deleteAll(jmxParameter);
        }


        //Delete NOVA
        if (originalPlan.getNova() != null)
        {
            this.novaRepo.deleteById(originalPlan.getNova().getId());
        }

        // Delete deployment connector properties to empty
        originalPlan.getCurrentRevision().getDeploymentConnectorProperties().clear();

        // Delete deployment broker properties to empty
        List<BrokerProperty> brokerPropertyListToRemove = getBrokerPropertiesToDeleteAndClearBrokerPropertiesFromPlan(originalPlan);

        // Delete Deployment batch schedule and context params associated for batch scheduler service
        this.deleteDeploymentBatchScheduleAndContextParams(originalPlan);

        Set<Integer> deploymentServiceIds = originalPlan.getDeploymentSubsystems().stream()
                .flatMap(sub -> sub.getDeploymentServices().stream())
                .map(DeploymentService::getId).collect(Collectors.toSet());
        jvmParamValuesRepository.deleteByDeploymentServiceIds(deploymentServiceIds);

        // closing every related deployment plan alert
        this.alertServiceApiClient.closePlanRelatedAlerts(originalPlan);

        // This plan do not have child. Can be deleted
        this.deploymentPlanRepository.deleteById(deploymentId);

        // Delete orphan broker properties
        this.brokerPropertyRepository.deleteAll(brokerPropertyListToRemove);

        // Emit Delete Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(originalPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DELETED)
                .entityId(deploymentId)
                .environment(originalPlan.getEnvironment())
                .addParam("releaseVersionId", originalPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", originalPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", originalPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", originalPlan.getReleaseVersion().getRelease().getName())
                .build());
    }

    @Override
    public TodoTaskResponseDTO deploy(final String ivUser, final Integer deploymentId, final Boolean force) throws NovaException
    {
        return this.deployerService.deployOnEnvironment(ivUser, deploymentId, force);
    }

    @Override
    public List<DeploymentPlan> getDeploymentPlansBetween(String environment, int productId, Calendar startDate, Calendar endDate, String deploymentStatusList)
    {
        Environment env = Environment.valueOf(environment);
        Calendar endDate2;
        if (endDate != null)
        {
            endDate2 = endDate;
        }
        else
        {
            endDate2 = Calendar.getInstance();
        }
        return this.deploymentPlanRepository.getByProductAndEnvironmentAndStatusBetweenDates(productId, this.convertDeploymentStatusStringToEnumList(deploymentStatusList), env.getEnvironment(), startDate, endDate2);
    }

    @Override
    @Transactional
    public DeploymentService getDeploymentServiceByName(String productName, String environment, String releaseName, String subsystemName, String serviceName) throws NovaException
    {
        if (Strings.isNullOrEmpty(productName))
        {
            log.error("[DeploymentService] -> [getDeploymentServiceByName]: the product name is null or empty. Product name: [{}]", productName);
            throw new NovaException(DeploymentError.getNoSuchProductError(), "product name is null or empty.");
        }

        // Get the product name (by UUAA or Product Name)
        String finalProductName = this.getProductNameByUUAAorProductName(productName);
        Product product = this.productRepository.findByName(finalProductName.toUpperCase());

        if (product == null)
        {
            log.error("[DeploymentService] -> [getDeploymentServiceByName]: the product name: [{}] not found in NOVA DB", finalProductName);
            throw new NovaException(DeploymentError.getNoSuchProductError(), "product name: [" + finalProductName + "] not found in NOVA DB. Review the Product by Name Query.");
        }

        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemByProductAndName(subsystemName, product.getId());
        DeploymentService deploymentService = this.serviceRepo.findDeploymentServiceByServiceInfo(product.getName(), environment, releaseName, subsystemDTO.getSubsystemId(), serviceName);

        if (deploymentService == null)
        {
            throw new NovaException(DeploymentError.getNoSuchServiceError(null), "[DeploymentService] -> [getDeploymentServiceByName]: deployment service does not found with product name: [" + productName + "], " +
                    "environment: [" + Environment.valueOf(environment) + "] - release name: [" + releaseName + "], subsystem id: [" + subsystemDTO.getSubsystemId() + "] and deployment service name: [" + serviceName + "]");
        }
        else
        {
            log.debug("[DeploymentService] -> [getDeploymentServiceByName]: found the following deployment service: [{}]", deploymentService);
        }

        return deploymentService;
    }

    @Override
    @Transactional
    public DeploymentMigrationDto migratePlan(Integer deploymentId, Integer versionId) throws NovaException
    {
        // Get the original plan.
        DeploymentPlan originalPlan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "deploymentPlan [" + deploymentId + "] not found"));

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(originalPlan);
        //Check stored release version
        deploymentsValidator.checkReleaseVersionStored(originalPlan);

        // Create the target plan using the given release version.
        DeploymentPlan targetPlan = this.createDeployment(versionId, originalPlan.getEnvironment(), true);

        // Copy all data from the original to the target where business rules allow it.
        return migrator.migratePlan(originalPlan, targetPlan);
    }

    @Override
    public void promotePlanToEnvironment(DeploymentPlan originalPlan, Environment environment)
    {
        // INT plans can only be promoted to PRE.
        if (originalPlan.getEnvironment().equals(Environment.INT.getEnvironment()) && !environment.equals(Environment.PRE))
        {
            throw new NovaException(DeploymentError.getTriedToPromoteIntPlanToWrongEnvironmentError());
        }

        // PRE plans can only be promoted to PRO.
        else if (originalPlan.getEnvironment().equals(Environment.PRE.getEnvironment()) && !environment.equals(Environment.PRO))
        {
            throw new NovaException(DeploymentError.getTriedToPromotePrePlanToWrongEnvironmentError());
        }

        // Only deployed and undeployed plans can be promoted.
        if (DeploymentStatus.DEPLOYED != originalPlan.getStatus() && DeploymentStatus.UNDEPLOYED != originalPlan.getStatus())
        {
            throw new NovaException(DeploymentError.getTriedToPromoteNotDeployedPlanError());
        }

        // Check if the promotion is from PRE to PRO and the selected deployment infrastructure is not the same
        this.deploymentsValidator.validateSamePlatformOnPREtoPRO(originalPlan, environment);

        // Check the instances number for deployment plan multiCPD. The instances number must be pairs.
        this.deploymentsValidator.validateInstancesNumberForMultiCPD(originalPlan);

        DeploymentPlan copiedPlan = this.deploymentPlanCloner.clonePlanToEnvironment(originalPlan, environment);


        // Persist plan
        this.repositoryManagerService.savePlan(copiedPlan);

        // Promote images from PRE to PRO.
        this.deployerService.promotePlan(originalPlan, copiedPlan);

        // Emit Undeploy Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(originalPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.PROMOTED)
                .entityId(originalPlan.getId())
                .environment(environment.getEnvironment())
                .addParam("releaseVersionId", originalPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", originalPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", originalPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", originalPlan.getReleaseVersion().getRelease().getName())
                .build());

        log.debug("Copied plan {} to environment {} ", originalPlan.getId(), environment);
    }

    @Override
    public TodoTaskResponseDTO undeployPlan(final String ivUser, final Integer deploymentId) throws NovaException
    {
        return this.removerService.undeployPlanOnEnvironment(ivUser, deploymentId);
    }

    @Override
    public void updatePlanFromDto(DeploymentDto deploymentDto, String env)
    {
        // Update subsystems and services only if set in the DTO.
        if (deploymentDto.getSubsystems() != null)
        {
            this.updatePlanSubsystemsAndServices(deploymentDto.getSubsystems(), env);
        }

        log.debug("Updated deployment plan {}", deploymentDto.getId());
    }

    @Override
    @Transactional
    public void updateServiceFromDto(final DeploymentServiceDto deploymentServiceDto, String env)
    {
        // Get the original deploymentService from the plan.
        Integer deploymentServiceDtoId = deploymentServiceDto.getId();
        DeploymentService deploymentService = entityManager.find(DeploymentService.class, deploymentServiceDtoId);

        if (deploymentService == null)
        {
            throw new NovaException(DeploymentError.getNoSuchServiceError(deploymentServiceDtoId));
        }

        // Number of instances and Hardware pack
        // Emit activity if the hardware pack has changed
        ReleaseVersionService releaseVersionService = deploymentService.getService();
        Integer productId = releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getId();
        ReleaseVersion releaseVersion = deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion();
        if (deploymentService.getHardwarePack() == null)
        {
            if (StringUtils.isNotEmpty(deploymentServiceDto.getHardwarePackCode()))
            {
                // Emit Hardware configuration Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.HARDWARE_CONFIGURED)
                        .entityId(deploymentService.getId())
                        .environment(env)
                        .addParam("serviceName", releaseVersionService.getServiceName())
                        .addParam("serviceType", releaseVersionService.getServiceType())
                        .addParam("hardwarePackCode", deploymentServiceDto.getHardwarePackCode())
                        .addParam("numberOfInstances", deploymentServiceDto.getNumberOfInstances())
                        .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                        .addParam("releaseVersionName", releaseVersion.getVersionName())
                        .addParam("releaseName", releaseVersion.getRelease().getName())
                        .build());
            }
            else
            {
                log.debug("Hardware pack is null or empty");
            }
        }
        else
        {
            if (deploymentServiceDto.getHardwarePackCode().equalsIgnoreCase(deploymentService.getHardwarePack().getCode()) &&
                    deploymentService.getNumberOfInstances() == deploymentServiceDto.getNumberOfInstances())
            {
                log.debug("Hardware and number of instances are equal");
            }
            else
            {
                // Emit Hardware configuration Deployment Service Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.HARDWARE_CONFIGURED)
                        .entityId(deploymentService.getId())
                        .environment(env)
                        .addParam("serviceName", releaseVersionService.getServiceName())
                        .addParam("serviceType", releaseVersionService.getServiceType())
                        .addParam("hardwarePackCode", deploymentServiceDto.getHardwarePackCode())
                        .addParam("numberOfInstances", deploymentServiceDto.getNumberOfInstances())
                        .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                        .addParam("releaseVersionName", releaseVersion.getVersionName())
                        .addParam("releaseName", releaseVersion.getRelease().getName())
                        .build());
            }
        }

        switch (ServiceType.valueOf(releaseVersionService.getServiceType()))
        {
            // NOVA batchs have only 1 instance ALWAYS.
            case NOVA_BATCH:
            case NOVA_SPRING_BATCH:
                deploymentService.setNumberOfInstances(1);
                break;
            default:
                Integer numberOfInstance = deploymentServiceDto.getNumberOfInstances();

                if (numberOfInstance == null)
                {
                    deploymentService.setNumberOfInstances(1);
                    log.warn("[DeploymentsSerivceImpl] -> [updateServiceFromDto]: the deployment service DTO: [{}], the number of instances fromo DTO is null. Set 1 (as number of instance) by default", deploymentServiceDto);
                }
                else
                {
                    deploymentService.setNumberOfInstances(numberOfInstance);
                }
        }
        deploymentService.setHardwarePack(hardwarePackRepository.findByCode(deploymentServiceDto.getHardwarePackCode()));

        // Filesystems
        DeploymentServiceFilesystemDto[] filesystems = deploymentServiceDto.getFilesystems();
        List<DeploymentServiceFilesystemDto> checkSizeList = filesystems == null ? List.of() : Arrays.asList(filesystems);
        if (deploymentService.getDeploymentServiceFilesystems().size() == checkSizeList.size())
        {
            for (DeploymentServiceFilesystemDto deploymentServiceFilesystemDto : checkSizeList)
            {
                boolean checkFilesystemChange = deploymentService.getDeploymentServiceFilesystems().stream().noneMatch(filesystem ->
                        filesystem.getFilesystem().getId().equals(deploymentServiceFilesystemDto.getFilesystemId()) &&
                                filesystem.getVolumeBind().equals(deploymentServiceFilesystemDto.getVolumeBind()));

                if (checkFilesystemChange)
                {
                    // Emit Filesystem Configured Deployment Service Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.FILESYSTEM_CONFIGURED)
                            .entityId(deploymentService.getId())
                            .environment(env)
                            .addParam("serviceName", releaseVersionService.getServiceName())
                            .addParam("serviceType", releaseVersionService.getServiceType())
                            .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                            .addParam("releaseVersionName", releaseVersion.getVersionName())
                            .addParam("releaseName", releaseVersion.getRelease().getName())
                            .addParam("FilesystemNameAdded", Arrays.stream(filesystems).map(DeploymentServiceFilesystemDto::getFilesystemName).collect(Collectors.toList()))
                            .build());
                    break;
                }
            }
        }
        else
        {
            // Emit Filesystem Configured Deployment Service Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.FILESYSTEM_CONFIGURED)
                    .entityId(deploymentService.getId())
                    .environment(env)
                    .addParam("serviceName", releaseVersionService.getServiceName())
                    .addParam("serviceType", releaseVersionService.getServiceType())
                    .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                    .addParam("releaseVersionName", releaseVersion.getVersionName())
                    .addParam("releaseName", releaseVersion.getRelease().getName())
                    .addParam("FilesystemNameAdded", Arrays.stream(filesystems).map(DeploymentServiceFilesystemDto::getFilesystemName).collect(Collectors.toList()))
                    .build());
        }

        deploymentService.getDeploymentServiceFilesystems().clear();
        this.repositoryManagerService.saveService(deploymentService);
        this.repositoryManagerService.flushServiceRepository();
        DeploymentServiceFilesystemDto[] filesystemsDto = filesystems;
        if (filesystemsDto != null && filesystemsDto.length > 0)
        {
            log.debug("[DeploymentsAPI] -> [updateServiceFromDto]: Updating FileSystem list in this deployment services: [{}]", releaseVersionService.getServiceName());

            List<DeploymentServiceFilesystem> deploymentServiceFilesystems = Arrays.stream(filesystemsDto).map(deploymentServiceFilesystemDto ->
            {
                Filesystem fileSystem = filesystemsApiRepository.findById(deploymentServiceFilesystemDto.getFilesystemId()).orElseThrow(() ->
                        new NovaException(DeploymentError.getFilesystemNotFoundError(deploymentServiceFilesystemDto.getFilesystemId())));

                DeploymentServiceFilesystem deploymentServiceFilesystem = new DeploymentServiceFilesystem();
                deploymentServiceFilesystem.setId(new DeploymentServiceFilesystemId(deploymentService.getId(), fileSystem.getId()));
                deploymentServiceFilesystem.setFilesystem(fileSystem);
                deploymentServiceFilesystem.setVolumeBind(deploymentServiceFilesystemDto.getVolumeBind());
                deploymentServiceFilesystem.setDeploymentService(deploymentService);

                return deploymentServiceFilesystem;
            }).collect(Collectors.toList());
            deploymentService.getDeploymentServiceFilesystems().addAll(deploymentServiceFilesystems);
        }

        // Logical Connectors and deployment connector properties
        // Emit activity for logical connectors
        if (deploymentServiceDto.getLogicalConnectors() == null)
        {
            log.trace("[DeploymentsServiceImpl] -> [updateServiceFromDto]: NO logical connector selected in this deployment services: [{}]. Continue", releaseVersionService.getServiceName());
        }
        else if (deploymentServiceDto.getLogicalConnectors().length == deploymentService.getLogicalConnectors().size())
        {
            for (DeploymentLogicalConnectorDto deploymentLogicalConnectorDto : deploymentServiceDto.getLogicalConnectors())
            {
                boolean isConnectorOnTheList = deploymentService.getLogicalConnectors().stream().anyMatch(logicalConnector -> logicalConnector.getName().equalsIgnoreCase(deploymentLogicalConnectorDto.getLogicalConnectorName()));

                if (!isConnectorOnTheList)
                {
                    // Emit Connector config Deployment Service Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.CONNECTOR_CONFIGURED)
                            .entityId(deploymentService.getId())
                            .environment(env)
                            .addParam("serviceName", releaseVersionService.getServiceName())
                            .addParam("serviceType", releaseVersionService.getServiceType())
                            .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                            .addParam("releaseVersionName", releaseVersion.getVersionName())
                            .addParam("ConnectorNameAdded", Arrays.stream(deploymentServiceDto.getLogicalConnectors()).map(DeploymentLogicalConnectorDto::getLogicalConnectorName).collect(Collectors.toList()))
                            .build());
                    break;
                }
            }
        }
        else
        {
            // Emit Connector config Deployment Service Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.CONNECTOR_CONFIGURED)
                    .entityId(deploymentService.getId())
                    .environment(env)
                    .addParam("serviceName", releaseVersionService.getServiceName())
                    .addParam("serviceType", releaseVersionService.getServiceType())
                    .addParam("DeploymentPlanId", deploymentServiceDto.getDeploymentPlanId())
                    .addParam("releaseVersionName", releaseVersion.getVersionName())
                    .addParam("releaseName", releaseVersion.getRelease().getName())
                    .addParam("ConnectorNameAdded", Arrays.stream(deploymentServiceDto.getLogicalConnectors()).map(DeploymentLogicalConnectorDto::getLogicalConnectorName).collect(Collectors.toList()))
                    .build());
        }

        // First at all, undeployPlan the deployment connector properties of this deployment service from configuration revision = DeploymentConnectorProperty (list)
        ConfigurationRevision configurationRevision = deploymentService.getDeploymentSubsystem().getDeploymentPlan().getCurrentRevision();
        configurationRevision.getDeploymentConnectorProperties().removeIf(deploymentConnectorProperty -> deploymentConnectorProperty.getDeploymentService().equals(deploymentService));

        // Reset the logical connector list of the deployment service
        deploymentService.setLogicalConnectors(new ArrayList<>());

        // Get the logical connector array from this deployment services
        DeploymentLogicalConnectorDto[] deploymentLogicalConnectorDtoArray = deploymentServiceDto.getLogicalConnectors();

        if (deploymentLogicalConnectorDtoArray == null || (deploymentLogicalConnectorDtoArray.length <= 0))
        {
            log.debug("[DeploymentsAPI] -> [updateServiceFromDto]: NO logical connector selected in this deployment services: [{}]", releaseVersionService.getServiceName());
        }
        else
        {
            // Create the 'Logical Connector List' and 'Deployment Connector Property list' for this Deployment Service
            List<LogicalConnector> logicalConnectorList = new ArrayList<>();
            List<DeploymentConnectorProperty> deploymentConnectorPropertyList = new ArrayList<>();

            // Iterate for each logical connector Dto to find the logical connector and get the logical connector properties to add it to this deployment services
            for (DeploymentLogicalConnectorDto logicalConnectorDto : deploymentLogicalConnectorDtoArray)
            {
                Optional<LogicalConnector> optionalLogicalConnector = this.logicalConnectorRepository.findById(logicalConnectorDto.getLogicalConnectorId());
                if (optionalLogicalConnector.isPresent())
                {
                    LogicalConnector logicalConnector = optionalLogicalConnector.get();
                    logicalConnectorList.add(optionalLogicalConnector.get());
                    log.trace("[DeploymentsAPI] -> [updateServiceFromDto]: added a new logical connector: [{}] to logical connector list of this deployment services: [{}]", logicalConnector.getName(), releaseVersionService.getServiceName());
                    // Manage the logical connector properties for this deployment services
                    for (LogicalConnectorProperty logicalConnectorProperty : logicalConnector.getLogConnProp())
                    {
                        // Create the deployment definition and save it as new property from logical connector
                        DeploymentConnectorProperty deploymentConnectorProperty = new DeploymentConnectorProperty();

                        deploymentConnectorProperty.setLogicalConnectorProperty(logicalConnectorProperty);
                        deploymentConnectorProperty.setDeploymentService(deploymentService);
                        deploymentConnectorProperty.setRevision(configurationRevision);

                        // Add the new Deployment Connector Property to the list of the Configuration Revision -> For all the plan
                        deploymentConnectorPropertyList.add(deploymentConnectorProperty);
                        log.debug("DeploymentsAPI -> updateServiceFromDto: added the deployment connector property: [{}] from logical connector: [{}]" + "to the deployment connector properties list.",
                                deploymentConnectorProperty.getLogicalConnectorProperty().getName(), logicalConnector.getName());
                    }
                }
                else
                {
                    log.warn("[DeploymentsAPI] -> [updateServiceFromDto]: logical connector with id [{}]  missing when trying to add to logical connector list of this deployment services: [{}]",
                            logicalConnectorDto.getLogicalConnectorId(), releaseVersionService.getServiceName());
                }
            }

            // Set the deployment connector property list to the configuration revision -> add all the deployment connector list to the configuration revision
            configurationRevision.getDeploymentConnectorProperties().addAll(deploymentConnectorPropertyList);
            // Set the new logical connector list to the deployment service
            deploymentService.setLogicalConnectors(logicalConnectorList);

            log.debug("[DeploymentsAPI] -> [updateServiceFromDto]: added a logical connector list: [{}] with all this deployment" + " connector properties list: [{}] to the current revision ID: " + "[{}] and to deployment services name: [{}]", logicalConnectorList, deploymentConnectorPropertyList, configurationRevision.getId(), releaseVersionService.getServiceName());
        }

        // Set JVM options (memory bar and jvm options)
        this.jdkParametersSetter.setJvmOptionsForDeploymentService(deploymentService, deploymentServiceDto, deploymentServiceDto.getAppliableJvmParameters());

        // DEPLOYMENT SERVICE -- BROKERS //
        List<GenericActivity> deploymentBrokerActivitiesToEmmit = this.deploymentBroker.getActivityAttachedDeploymentServiceBrokerChange(deploymentService, deploymentServiceDto);
        List<Broker> brokerList = this.deploymentBroker.getBrokersEntitiesFromDeploymentServiceDTO(deploymentServiceDto);
        this.updateBrokersPropertiesToDeploymentService(deploymentService, brokerList);
        deploymentService.setBrokers(brokerList);
        deploymentBrokerActivitiesToEmmit.forEach(this.novaActivityEmitter::emitNewActivity);
    }

    @Override
    @Transactional
    public void updateServiceState(final ServiceStateDTO serviceStateDTO, Integer serviceId) throws NovaException
    {

        log.debug("[Deployment API] -> [updateServiceState]: Updating service {} with action {}", serviceId, serviceStateDTO.getAction());

        DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(serviceId);

        final DeploymentAction deploymentAction = DeploymentAction.valueOf(serviceStateDTO.getAction());

        if (!DeploymentAction.INVALID.equals(deploymentAction))
        {
            deploymentService.setAction(deploymentAction);

            switch (deploymentAction)
            {
                case DEPLOYING:
                    if (isCDNOrApiRestOrDaemon(deploymentService))
                    {
                        if (deploymentService.getInstances() == null)
                        {
                            deploymentService.setInstances(new ArrayList<>(1));
                        }

                        if (deploymentService.getInstances().isEmpty())
                        {
                            addNewDeploymentInstanceToDeploymentService(deploymentService);
                        }
                    }
                    break;

                case UNDEPLOYING:
                    if (deploymentService.getInstances() != null)
                    {
                        deploymentService.getInstances().forEach(di -> di.setDeletionDate(Calendar.getInstance()));
                    }
                    break;

                case STARTING:
                    if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch())
                    {
                        addNewDeploymentInstanceToDeploymentService(deploymentService);
                    }
                    if (isCDNOrApiRestOrDaemon(deploymentService) && deploymentService.getInstances() != null)
                    {
                        deploymentService.getInstances().forEach(di -> di.setStarted(Boolean.TRUE));
                    }
                    break;

                case STOPPING:
                    if (isCDNOrApiRestOrDaemon(deploymentService) && deploymentService.getInstances() != null)
                    {
                        deploymentService.getInstances().forEach(di -> di.setStarted(Boolean.FALSE));
                    }
                    break;

                default:
                    break;
            }
        }

        this.repositoryManagerService.saveService(deploymentService);
    }

    @Override
    @Transactional
    public void updateSubsystemState(final SubsystemStateDTO subsystemStateDTO, final Integer subsystemId) throws NovaException
    {

        log.debug("[Deployment API] -> [updateSubsystemState]: Updating subsystem [{}] with state [{}] ...", subsystemId, subsystemStateDTO);

        final DeploymentAction deploymentAction = DeploymentAction.valueOf(subsystemStateDTO.getAction());

        if (!DeploymentAction.INVALID.equals(deploymentAction))
        {
            final DeploymentSubsystem deploymentSubsystem = this.deploymentsValidator.validateAndGetDeploymentSubsystem(subsystemId);

            deploymentSubsystem.setAction(deploymentAction);
            this.repositoryManagerService.saveSubsystem(deploymentSubsystem);

            log.debug("[Deployment API] -> [updateSubsystemState]: Updated subsystem [{}] state", subsystemId);
        }
        else
        {
            log.debug("[Deployment API] -> [updateSubsystemState]: Subsystem state was not update because the received state was [{}]", deploymentAction);
        }
    }

    @Override
    @Transactional
    public void updateDeploymentPlanState(DeploymentStateDTO deploymentStatusAction, Integer deploymentId) throws NovaException
    {
        log.debug("[Deployment API] -> [updateDeploymentPlanState]: Updating deployment plan {} with state {}", deploymentId, deploymentStatusAction);

        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlan(deploymentId);

        final DeploymentStatus deploymentStatus = parseDeploymentStatus(deploymentStatusAction.getStatus());
        final DeploymentAction deploymentAction = DeploymentAction.valueOf(deploymentStatusAction.getAction());

        // Update deployment plan dates (execution date, undeployment date)
        if ((deploymentAction == DeploymentAction.READY || deploymentAction == DeploymentAction.ERROR)
                && deploymentStatus != null)
        {
            if (deploymentStatus == DeploymentStatus.DEPLOYED)
            {
                log.debug(
                        "[Deployment API] -> [updateDeploymentPlanState]: Deployed plan [{}], execution date will be updated",
                        deploymentPlan.getId());
                deploymentPlan.setExecutionDate(Calendar.getInstance());
            }
            else if (deploymentStatus == DeploymentStatus.UNDEPLOYED)
            {
                log.debug(
                        "[Deployment API] -> [updateDeploymentPlanState]: Undeployed plan [{}], undeployment date will be updated",
                        deploymentPlan.getId());
                deploymentPlan.setUndeploymentDate(Calendar.getInstance());
            }
        }

        // Update deployment plan status only if it is not null
        if (deploymentStatus != null)
        {
            deploymentPlan.setStatus(deploymentStatus);
        }
        deploymentPlan.setAction(deploymentAction);
        repositoryManagerService.savePlan(deploymentPlan);
    }

    @Override
    public TaskRequestDTO[] getAllConfigurationManagement(Integer deploymentPlanId)
    {
        //First of all, check and get deployment plan
        DeploymentPlan deploymentPlan = deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "The deployment plan [" + deploymentPlanId + "] not found"));
        this.deploymentsValidator.checkPlanExistence(deploymentPlan);

        //Get all release version service of plan to know if they use libraries
        List<Integer> rvServiceIdList = this.getAllReleaseVersionService(deploymentPlan);

        List<TaskRequestDTO> listManagementTask = new ArrayList<>();

        List<LMLibraryEnvironmentsDTO> lmLibraryEnvironmentsDTOList = this.libraryManagerService.getUsedLibraries(rvServiceIdList, com.bbva.enoa.platformservices.coreservice.librarymanagerapi.util.Constants.USAGE);

        for (ToDoTaskType type : ToDoTaskType.getConfigurationManagementTask())
        {
            boolean canGenerate = this.todoTaskServiceClient.getDeploymentPlanManagementConfigurationTask(deploymentPlanId, type.name());

            switch (type.name())
            {
                case "CHECK_ENVIRONMENT_VARS":
                    //All deployment plan has configuration environment variables
                    listManagementTask.addAll(this.deploymentUtils.buildTaskRequestDTOEnvironmentVars(canGenerate));
                    break;
                case "CHECK_LIBRARY_VARS":
                    //Add
                    listManagementTask.addAll(this.deploymentUtils.buildTaskRequestDTOLibraryVars(lmLibraryEnvironmentsDTOList, canGenerate));
                    break;
            }
        }
        // convert list with management task to array
        TaskRequestDTO[] taskRequestDTOArray = listManagementTask.toArray(new TaskRequestDTO[0]);

        log.debug("[DeploymentsAPI] -> [getAllConfigurationManagement]: Building TaskRequest list with used libraries. TasRequestDTO array result: [{}]", Arrays.toString(taskRequestDTOArray));

        return taskRequestDTOArray;
    }

    @Override
    @Transactional
    public List<Integer> getAllReleaseVersionService(DeploymentPlan deploymentPlan)
    {
        log.debug("[DeploymentsAPI] -> [getAllReleaseVersionService]: Getting all services of plan [{}] to get used libraries", deploymentPlan.getId());

        // Get all release version service id of given plan
        List<Integer> releaseVersionServiceIdList = deploymentPlan.getDeploymentSubsystems().stream()
                .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                .map(deploymentService -> deploymentService.getService().getId())
                .collect(Collectors.toList());

        log.debug("[DeploymentsAPI] -> [getAllReleaseVersionService]: All services of plan [{}] are [{}]", deploymentPlan.getId(), releaseVersionServiceIdList);

        return releaseVersionServiceIdList;
    }

    @Override
    public void unschedule(final String ivUser, Integer deploymentId)
    {
        log.debug("unschedule deployment plan ID: [{} of type nova planned]", deploymentId);

        // Get the plan.
        DeploymentPlan originalPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "The deployment plan [" + deploymentId + "] not found"));

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(originalPlan);

        boolean isPendingNovaPlanned = false;

        // Close all pending tasks
        List<DeploymentTask> taskList = this.deploymentTaskRepository.findByDeploymentPlanId(deploymentId);
        for (DeploymentTask task : taskList)
        {
            if (task.getStatus() == ToDoTaskStatus.PENDING || task.getStatus() == ToDoTaskStatus.PENDING_ERROR)
            {
                // Check if we are't started a nova planned schedule
                isPendingNovaPlanned = task.getTaskType() == ToDoTaskType.NOVA_PLANNING;

                task.setClosingDate(Calendar.getInstance());
                task.setAssignedUserCode(ivUser);
                task.setClosingMotive(isPendingNovaPlanned ?
                        "Nova deployment planning canceled by the user" :
                        "The planned deployment of the plan has been canceled by user");
                // Set the new status for the task
                task.setStatus(ToDoTaskStatus.REJECTED);

                // Save into the BBDD
                this.deploymentTaskRepository.save(task);

                // Send notifications depending on the to do task type - type Deploy
                sendPlanManagerResolvedNotification(ivUser, task);

                log.debug(isPendingNovaPlanned ?
                        "Nova Deployment planning taskId: [{}] of the deployment plan ID: [{}] canceled by user" :
                        "The planned deployment of the plan width taskId: [{}] of the deployment plan ID: [{}] canceled by user", task.getId(), deploymentId);
            }
        }

        if (!isPendingNovaPlanned)
        {
            // Call to unSchedule
            deployerService.unscheduleDeployment(ivUser, originalPlan);
        }

        // Set status to DEFINITION.
        originalPlan.setStatus(DeploymentStatus.DEFINITION);
        this.deploymentPlanRepository.save(originalPlan);

        // Emit Cancellation Request Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(originalPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.CANCELLATION_REQUESTED)
                .entityId(deploymentId)
                .environment(originalPlan.getEnvironment())
                .addParam("releaseVersionId", originalPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", originalPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", originalPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", originalPlan.getReleaseVersion().getRelease().getName())
                .build());

        log.debug("unscheduled deployment plan ID: [{} of type nova planned]", deploymentId);
    }

    /**
     * Create the deployment context params only for batch scheduler services
     *
     * @param deploymentPlan the deployment plan associated to the batch scheduler service
     */
    private void createDeploymentContextParams(final DeploymentPlan deploymentPlan)
    {
        deploymentPlan.getReleaseVersion().getSubsystems()
                .stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.createDeploymentPlanContextParams(releaseVersionService.getId(), deploymentPlan.getId(),
                        deploymentPlan.getEnvironment()));
    }

    private List<BrokerProperty> getBrokerPropertiesToDeleteAndClearBrokerPropertiesFromPlan(final DeploymentPlan originalPlan)
    {
        originalPlan.getCurrentRevision().getDeploymentBrokerProperties().clear();
        List<BrokerProperty> brokerPropertyListToRemove = new ArrayList<>();
        originalPlan.getDeploymentSubsystems().forEach(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().forEach(deploymentService -> {
            // Get all old broker properties for being deleted
            brokerPropertyListToRemove.addAll(deploymentService.getDeploymentBrokerProperties().stream().map(DeploymentBrokerProperty::getBrokerProperty).collect(Collectors.toList()));
            // clear deploymentBrokers associated to deploymentService
            deploymentService.getDeploymentBrokerProperties().clear();
        }));
        return brokerPropertyListToRemove;
    }

    private void updateBrokersPropertiesToDeploymentService(final DeploymentService deploymentService, final List<Broker> brokerList)
    {
        ConfigurationRevision configurationRevision = deploymentService.getDeploymentSubsystem().getDeploymentPlan().getCurrentRevision();
        // Clear the deployment broker properties of this deployment service from configuration revision
        configurationRevision.getDeploymentBrokerProperties().removeIf(deploymentBrokerProperty -> deploymentBrokerProperty.getDeploymentService().equals(deploymentService));
        // Get all old broker properties for being deleted
        List<BrokerProperty> brokerPropertyListToRemove = deploymentService.getDeploymentBrokerProperties().stream().map(DeploymentBrokerProperty::getBrokerProperty).collect(Collectors.toList());
        deploymentService.getDeploymentBrokerProperties().clear();
        // this save and flush is necessary before delete all broker properties attached at deploymentBrokerProperties attached at configuration revision.
        this.repositoryManagerService.saveService(deploymentService);
        this.repositoryManagerService.flushServiceRepository();
        // remove all brokerProperties not used anymore by this deploymentService
        this.brokerPropertyRepository.deleteAll(brokerPropertyListToRemove);

        List<DeploymentBrokerProperty> deploymentBrokerProperties = this.deploymentBroker.createAndPersistBrokerPropertiesOfDeploymentService(deploymentService, configurationRevision, brokerList);

        deploymentService.getDeploymentBrokerProperties().addAll(deploymentBrokerProperties);
        configurationRevision.getDeploymentBrokerProperties().addAll(deploymentBrokerProperties);
    }

    private void addNewDeploymentInstanceToDeploymentService(final DeploymentService deploymentService)
    {
        final EtherDeploymentInstance etherDeploymentInstance = new EtherDeploymentInstance();
        etherDeploymentInstance.setService(deploymentService);
        etherDeploymentInstance.setMemory(deploymentService.getHardwarePack().getRamMB());
        etherDeploymentInstance.setCpu(deploymentService.getHardwarePack().getNumCPU());

        final ReleaseVersionService releaseVersionService = deploymentService.getService();
        final Release release = deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease();
        final String imageName = EtherServiceNamingUtils.getEAEServiceVersionImageName(this.springEnvironment.getActiveProfiles()[0], releaseVersionService.getGroupId(), releaseVersionService.getArtifactId(), release.getName());
        final String imageVersion = releaseVersionService.getVersion();
        etherDeploymentInstance.setImage(String.format("%s:%s", imageName, imageVersion));

        etherDeploymentInstance.setAction(DeploymentAction.READY);

        deploymentService.getInstances().add(etherDeploymentInstance);
    }

    private boolean isCDNOrApiRestOrDaemon(final DeploymentService deploymentService)
    {
        final ServiceType serviceType = ServiceType.valueOf(deploymentService.getService().getServiceType());

        return serviceType.isCdn() || serviceType.isRest() || serviceType.isDaemon();
    }

    /**
     * Delete a deployment Batch schedule and the context params associated to batch schedule services
     *
     * @param deploymentPlan a deployment plan
     */
    private void deleteDeploymentBatchScheduleAndContextParams(final DeploymentPlan deploymentPlan)
    {
        deploymentPlan.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .forEachOrdered(releaseVersionService ->
                {
                    this.schedulerManagerClient.removeDeploymentPlanContextParams(deploymentPlan.getId());
                    this.schedulerManagerClient.removeDeploymentBatchSchedule(releaseVersionService.getId(), deploymentPlan.getId());
                });
    }

    /**
     * Add a {@link DeploymentService} for each {@link ReleaseVersionService}
     * of the {@link ReleaseVersionSubsystem} attached to the {@link DeploymentSubsystem}.
     *
     * @param deploymentSubsystem {@link DeploymentSubsystem}
     */
    private void addServicesToSubsystem(DeploymentSubsystem deploymentSubsystem)
    {
        for (ReleaseVersionService service : deploymentSubsystem.getSubsystem().getServices())
        {
            // Create the deployment service using default values.
            DeploymentService deploymentService = new DeploymentService();

            // Attach the release version service.
            deploymentService.setService(service);

            // Add it to the deployment subsystem.
            deploymentService.setDeploymentSubsystem(deploymentSubsystem);
            deploymentSubsystem.getDeploymentServices().add(deploymentService);

            switch (ServiceType.valueOf(deploymentService.getService().getServiceType()))
            {
                case NOVA_BATCH:
                case NOVA_SPRING_BATCH:
                    deploymentService.setNumberOfInstances(DeploymentConstants.BATCH_MAX_INSTANCES);
                    break;
                case EPHOENIX_BATCH:
                case EPHOENIX_ONLINE:
                    deploymentService.setNumberOfInstances(this.deploymentUtils.getNumberOfInstancesForEphoenixService(deploymentSubsystem.getDeploymentPlan().getEnvironment(),
                            deploymentService.getService().getFinalName()));
                    break;
                default:
                    break;
            }

            entityManager.persist(deploymentService);
        }
    }

    /**
     * Add a {@link DeploymentSubsystem} to the {@link DeploymentPlan} for each
     * {@link ReleaseVersionSubsystem} of the plan.
     *
     * @param plan {@link DeploymentPlan}.
     */
    private void addSubsystemsToPlan(DeploymentPlan plan)
    {
        // Get the attached release version.
        ReleaseVersion version = plan.getReleaseVersion();

        // For each subsystem:
        for (ReleaseVersionSubsystem subsystem : version.getSubsystems())
        {
            // Create a DeploymentSubsystem.
            DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
            deploymentSubsystem.setSubsystem(subsystem);

            // Add it to the plan.
            deploymentSubsystem.setDeploymentPlan(plan);
            plan.getDeploymentSubsystems().add(deploymentSubsystem);

            // Save it so it can be used to add services.
            entityManager.persist(deploymentSubsystem);

            // Add all its services.
            this.addServicesToSubsystem(deploymentSubsystem);
        }
    }

    //////////////////////////////////////// PRIVATE METHODS //////////////////////////////////////////////////////////

    /**
     * Check deployment line
     *
     * @param service release version service
     * @param value   value
     */
    private void checkDeploymentLine(ReleaseVersionService service, ConfigurationValue value)
    {
        //Check deployment line
        if (Constants.EPHOENIX_DEPLOYMENT.equalsIgnoreCase(value.getDefinition().getName()))
        {
            value.setValue(service.getEphoenixData().getDeploymentLine());
        }
    }

    /**
     * Check number of plans
     *
     * @param environment environment
     * @param productId   product id
     */
    private void checkNumberOfPlans(String environment, int productId)
    {
        //check number of deploymentPlanList
        List<DeploymentPlan> deploymentPlanList = this.deploymentPlanRepository.getByProductIdAndEnvironmentAndStatusNot(productId, environment, DeploymentStatus.STORAGED);
        if (deploymentPlanList.size() >= this.maxdeployments)
        {
            throw new NovaException(DeploymentError.getMaxPlanReachedError(productId, environment, this.maxdeployments));
        }
    }

    /**
     * Create initial revision.
     *
     * @param deployment - {@link DeploymentPlan}
     * @return ConfigurationRevision
     */
    private ConfigurationRevision createInitialRevision(final DeploymentPlan deployment)
    {
        log.debug("Creating initial revision for deployment plan {} of release {}", deployment.getId(), deployment.getReleaseVersion().getRelease().getName());


        ConfigurationRevision newRevision = new ConfigurationRevision();
        newRevision.setDescription("Initial revision for Deployment plan: " + deployment.getId());
        newRevision.setDeploymentPlan(deployment);
        newRevision.setDeploymentConnectorProperties(new ArrayList<>());
        this.confRevisionRepo.save(newRevision);

        List<ConfigurationValue> values = new ArrayList<>();

        for (ReleaseVersionSubsystem subsystem : deployment.getReleaseVersion().getSubsystems())
        {
            for (ReleaseVersionService service : subsystem.getServices())
            {
                for (PropertyDefinition definition : service.getProperties())
                {
                    ConfigurationValue value = new ConfigurationValue();
                    value.setDefinition(definition);
                    value.setRevision(newRevision);
                    value.setValue(definition.getDefaultValue());
                    this.checkDeploymentLine(service, value);
                    values.add(value);
                }
            }
        }
        newRevision.setConfigurations(values);

        return newRevision;
    }

    /**
     * Delete all plan related tasks
     *
     * @param deploymentPlan deployment plan
     */
    private void deleteTasks(DeploymentPlan deploymentPlan)
    {
        //First, undeployPlan all related tasks
        List<DeploymentTask> taskList = this.deploymentTaskRepository.findByDeploymentPlanId(deploymentPlan.getId());
        for (DeploymentTask task : taskList)
        {
            this.deploymentTaskRepository.delete(task);
            log.debug("Removed deployment taskId: [{}] of the deployment plan ID: [{}]", task.getId(), deploymentPlan.getId());
        }
        //First, undeployPlan all related tasks from plan
        List<ManagementActionTask> actionTaskList = this.managementActionTaskRepository.findByRelatedId(deploymentPlan.getId());
        this.deleteTasksFromRepo(deploymentPlan.getId(), actionTaskList);
        //Remove all related tasks from subsystems
        this.deleteTasksFromSubsystems(deploymentPlan);
    }

    /**
     * Delete tasks from instance
     *
     * @param deploymentService service
     */
    private void deleteTasksFromInstance(DeploymentService deploymentService)
    {
        for (DeploymentInstance deploymentInstance : deploymentService.getInstances())
        {
            List<ManagementActionTask> instanceTaskList = this.managementActionTaskRepository.findByRelatedId(deploymentInstance.getId());
            this.deleteTasksFromRepo(deploymentInstance.getId(), instanceTaskList);
        }
    }

    /**
     * Delete tasks from repository
     *
     * @param relatedId      related id
     * @param actionTaskList task list
     */
    private void deleteTasksFromRepo(int relatedId, List<ManagementActionTask> actionTaskList)
    {
        for (ManagementActionTask task : actionTaskList)
        {
            this.managementActionTaskRepository.delete(task);
            log.debug("Removed management action taskId: [{}] with related id: [{}]", task.getId(), relatedId);
        }
    }

    /**
     * Delete tasks from services
     *
     * @param deploymentSubsystem subsystem
     */
    private void deleteTasksFromServices(DeploymentSubsystem deploymentSubsystem)
    {
        for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
        {
            List<ManagementActionTask> serviceTaskList = this.managementActionTaskRepository.findByRelatedId(deploymentService.getId());
            this.deleteTasksFromRepo(deploymentService.getId(), serviceTaskList);
            configurationTaskRepository.deleteAll(
                    this.configurationTaskRepository.findByDeploymentService(deploymentService)
            );
            //Remove all related tasks from instances
            this.deleteTasksFromInstance(deploymentService);
        }
    }

    /**
     * Delete tasks from subsystems
     *
     * @param deploymentPlan depployment plan
     */
    private void deleteTasksFromSubsystems(DeploymentPlan deploymentPlan)
    {
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            List<ManagementActionTask> subsystemTaskList = this.managementActionTaskRepository.findByRelatedId(deploymentSubsystem.getId());
            this.deleteTasksFromRepo(deploymentSubsystem.getId(), subsystemTaskList);
            //Remove all related tasks from services
            this.deleteTasksFromServices(deploymentSubsystem);
        }
    }

    /**
     * Updates a plan subsystem and services resources.
     *
     * @param subsystemDtos {@link DeploymentSubsystemDto}[]
     * @param env           environment
     */
    private void updatePlanSubsystemsAndServices(DeploymentSubsystemDto[] subsystemDtos, String env)
    {
        for (DeploymentSubsystemDto subsystemDto : subsystemDtos)
        {
            DeploymentServiceDto[] services = subsystemDto.getServices();
            if (services != null)
            {
                for (DeploymentServiceDto serviceDto : services)
                {
                    this.updateServiceFromDto(serviceDto, env);
                }
            }
        }
    }

    /**
     * Gets the product name from control-m parameter value. This parameter is called 'product name', and the value could be:
     * -- Or Product name
     * -- Or UUAA of the product
     * Depending on the Batch planning (from Control M)
     *
     * @param name could be or product name or UUAA name
     * @return the product name
     */
    private String getProductNameByUUAAorProductName(final String name)
    {
        log.debug("[DeploymentServiceImpl] -> [getProductNameByUUAAorProductName]: the parameters value for product name is: [{}]", name);
        String productName = name;

        Product product = this.productRepository.findByUuaaIgnoreCase(name);
        if (product != null)
        {
            productName = product.getName();
        }
        log.info("[DeploymentServiceImpl] -> [getProductNameByUUAAorProductName]: the final product name is: [{}]", productName);
        return productName;
    }

    /**
     * Parses {@code status} and returns a {@link DeploymentStatus} if it is valid or {@code null} if it is not
     *
     * @param status String containing the status value
     * @return {@link DeploymentStatus} or {@code null}
     */
    private DeploymentStatus parseDeploymentStatus(final String status)
    {
        final DeploymentStatus deploymentStatus;

        if (EnumUtils.isValidEnum(DeploymentStatus.class, status))
        {
            deploymentStatus = DeploymentStatus.valueOf(status);
        }
        else
        {
            deploymentStatus = null;
        }

        return deploymentStatus;
    }

    private void sendPlanManagerResolvedNotification(String ivUser, DeploymentTask task)
    {
        if (task.getTaskType() == ToDoTaskType.DEPLOY_PRE || task.getTaskType() == ToDoTaskType.DEPLOY_PRO || task.getTaskType() == ToDoTaskType.SCHEDULE_PLANNING
                || task.getTaskType() == ToDoTaskType.NOVA_PLANNING)
        {
            this.mailServiceClient.sendPlanManagerResolveNotification(task.getProduct().getName(), task.getId(), task
                            .getDeploymentPlan().getEnvironment(), this.mailServiceClient.getFullName(getUser(ivUser)) + " - " + ivUser,
                    task.getProduct().getId(), task.getDeploymentPlan().getId(), MailServiceConstants.DEPLOY_PLAN_SUBJECT);
        }

        // Send notifications depending on the to do task type - type Undeploy
        if (task.getTaskType() == ToDoTaskType.UNDEPLOY_PRO || task.getTaskType() == ToDoTaskType.UNDEPLOY_PRE)
        {
            this.mailServiceClient.sendPlanManagerResolveNotification(task.getProduct().getName(), task.getId(), task
                            .getDeploymentPlan().getEnvironment(), this.mailServiceClient.getFullName(getUser(ivUser)) + " - " + ivUser,
                    task.getProduct().getId(), task.getDeploymentPlan().getId(), MailServiceConstants.UNDEPLOY_PLAN_SUBJECT);
        }
    }

    private USUserDTO getUser(final String ivUser)
    {
        USUserDTO user = new USUserDTO();
        try
        {
            user = this.usersClient.getUser(ivUser, new Errors());
            user.setUserName(user.getUserName());
            user.setUserCode(user.getUserCode());
            user.setSurname1(user.getSurname1());
            user.setSurname2(user.getSurname2());
            user.setEmail(user.getEmail());

            log.debug("[[BatchManagerService]] -> [getUser]: found the user code provided: [{}] by UserService", ivUser);
        }
        catch (Errors e)
        {
            log.warn("[BatchManagerService] -> [getUser]: the user code provided: [{}] have not been validated by UserService. Error found: [{}]. Return just the user code", ivUser, e.getMessage());
        }

        return user;
    }

    /**
     * Convert a comma separated String of Deployment Status to a List of DeploymentStatus enums.
     *
     * @param deploymentStatusString The given String.
     * @return The List of DeploymentStatus enums.
     */
    private List<DeploymentStatus> convertDeploymentStatusStringToEnumList(String deploymentStatusString)
    {
        if (!Strings.isNullOrEmpty(deploymentStatusString))
        {
            return Arrays.stream(deploymentStatusString.split(",")).map(DeploymentStatus::valueOf).collect(Collectors.toList());
        }
        return null;
    }

}
