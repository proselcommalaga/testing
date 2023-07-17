package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.*;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatus;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDtoPage;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDtoPage;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentInstanceStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardServiceStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardStatusesDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSubsystemStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSummaryDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentTypeChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkParameterTypeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkTypedParametersDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.LMLibraryRequirementFulfilledDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.LMLibraryRequirementsFulfilledDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.MinimalDeploymentSubsystemStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemServicesStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.TaskRequestDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckReadyToDeployRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.common.entities.AbstractEntity;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentInstanceType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.common.enums.ServiceGroupingNames;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.JdkParameterRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.enums.LanguageVersions;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.RequirementNamespace;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.SubsystemBatchServiceStatus;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IBatchManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentChangeService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentChangesDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentGcspService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentNovaService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentServicesNumberDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentStatusService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsApiService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.ITaskProcessor;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentServiceAccountableTypeProvider;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.IErrorCode;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import com.google.common.base.Strings;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants.ACTION_NOT_ALLOWED;
import static com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils.PERMISSION_DENIED;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.REQUIREMENT_NAME.NAMESPACE;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.REQUIREMENT_TYPE;
import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.RESOURCE_REQUIREMENT_NAME;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;

/**
 * The type Deployments api service.
 */
@Log4j2
@Service
public class DeploymentsApiServiceImpl implements IDeploymentsApiService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DeploymentsApiServiceImpl.class);
    private static final String NOT_FOUND_SUFFIX = "] not found";
    public static final String DEPLOYMENT_PLAN_MESSAGE_PREFIX = "The deployment plan [";
    public static final String RELEASE_VERSION_ID_PARAM = "releaseVersionId";
    public static final String DEPLOYMENT_STATUS_PARAM = "deploymentstatus";
    public static final String RELEASE_VERSION_NAME_PARAM = "releaseVersionName";
    public static final String RELEASE_NAME_PARAM = "releaseName";
    public static final String UNDEPLOY_PLAN_ID_PARAM = "undeployPlanId";
    public static final String DEPLOYMENT_DATE_PARAM = "deploymentDate";

    private static final String[] LANGUAGE_VERSIONS_VALUES = Arrays.stream(LanguageVersions.values()).map(LanguageVersions::name).toArray(String[]::new);

    /**
     * Deployments service
     */
    private final IDeploymentsService deploymentsService;

    /**
     * DTO builder
     */
    private final IDeploymentPlanDtoBuilder planBuilder;

    /**
     * ServiceNumberDTO builder
     */
    private final IDeploymentServicesNumberDtoBuilder numberBuilder;

    /**
     * client of ProductUser
     */
    private final IProductUsersClient usersService;

    /**
     * Service of DeploymentStatus
     */
    private final IDeploymentStatusService statusService;

    /**
     * LibraryManager service
     */
    private final ILibraryManagerService libraryManagerService;


    /**
     * DeploymentGCSP service
     */
    private final IDeploymentGcspService gcspService;

    /**
     * deploymentNova service
     */
    private final IDeploymentNovaService novaPlannedService;

    /**
     * DeploymentChange servce
     */
    private final IDeploymentChangeService deploymentChangeService;

    /**
     * Deployment manager service
     */
    private final IDeploymentManagerService deploymentManagerService;

    /**
     * TaskProcessor
     */
    private final ITaskProcessor taskProcessor;

    /**
     * Deployment change dto builder
     */
    private final IDeploymentChangesDtoBuilder changesBuilder;

    /**
     * validator
     */
    private final IDeploymentsValidator deploymentsValidator;

    /**
     * repository of deployment service
     */
    private final DeploymentServiceRepository deploymentServiceRepository;

    /**
     * repository of deploymentPlan
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Repository of Release Version
     */
    private final ReleaseVersionRepository releaseVersionRepository;

    /**
     * Manage validation utils
     */
    private final ManageValidationUtils manageValidationUtils;

    /**
     * Repository of DeploymentSubsystem
     */
    private final DeploymentSubsystemRepository deploymentSubsystemRepository;

    /**
     * manager for Error task
     */
    private final IErrorTaskManager errorTaskManager;

    /**
     * Management action task repository
     */
    private final ManagementActionTaskRepository managementActionTaskRepository;

    /**
     * JDK parameter repository
     */
    private final JdkParameterRepository jdkParameterRepository;

    /**
     * CDP repository
     */
    private final CPDRepository cpdRepository;

    /**
     * Batch schedule service
     */
    private final IBatchScheduleService batchScheduleService;

    /**
     * Batch Manager service
     */
    private final IBatchManagerService batchManagerService;

    /**
     * Ether manager client
     */
    private final IEtherManagerClient etherManagerClient;

    /**
     * Deployment utils
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * Scheduler Manager client
     */
    private final ISchedulerManagerClient schedulerManagerClient;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    private final DeploymentServiceAccountableTypeProvider deploymentServiceAccountableTypeProvider;

    /**
     * Constructor by params
     *
     * @param deploymentsService             deployment service
     * @param planBuilder                    plan dto builder
     * @param numberBuilder                  ServiceNumberDTO Builder
     * @param usersService                   client of Product User
     * @param statusService                  deploymentStatus service
     * @param libraryManagerService          library manager service
     * @param gcspService                    GCSP service
     * @param novaPlannedService             NOVA planned service
     * @param deploymentChangeService        change service
     * @param deploymentManagerService       client of DeploymentManagerCreate
     * @param taskProcessor                  task processor
     * @param changesBuilder                 Change DTO builder
     * @param deploymentsValidator           validator
     * @param deploymentServiceRepository    repository of deployment service
     * @param deploymentPlanRepository       repository of deploymentPlan
     * @param versionRepository              repository of Release version
     * @param deploymentSubsystemRepository  repository of DeploymentSubsystem
     * @param manageValidationUtils          Manage validation utils service
     * @param errorTaskManager               manager for ErrorTask
     * @param managementActionTaskRepository managementActionTaskRepository
     * @param jdkParameterRepository         JDK parameter repository
     * @param cpdRepository                  CPD repository
     * @param batchScheduleService           batchScheduleService
     * @param batchManagerService            batch manager service
     * @param etherManagerClient             ether manager client
     * @param deploymentUtils                deployment utils
     * @param schedulerManagerClient         scheduler manager client
     * @param novaActivityEmitter            NovaActivity emitter
     */
    @Autowired
    public DeploymentsApiServiceImpl(final IDeploymentsService deploymentsService,
                                     final IDeploymentPlanDtoBuilder planBuilder,
                                     final IDeploymentServicesNumberDtoBuilder numberBuilder,
                                     final IProductUsersClient usersService,
                                     final IDeploymentStatusService statusService,
                                     final ILibraryManagerService libraryManagerService,
                                     final IDeploymentGcspService gcspService,
                                     final IDeploymentNovaService novaPlannedService,
                                     final IDeploymentChangeService deploymentChangeService,
                                     final IDeploymentManagerService deploymentManagerService,
                                     final ITaskProcessor taskProcessor,
                                     final IDeploymentChangesDtoBuilder changesBuilder,
                                     final IDeploymentsValidator deploymentsValidator,
                                     final DeploymentServiceRepository deploymentServiceRepository,
                                     final DeploymentPlanRepository deploymentPlanRepository,
                                     final ReleaseVersionRepository versionRepository,
                                     final DeploymentSubsystemRepository deploymentSubsystemRepository,
                                     final ManageValidationUtils manageValidationUtils,
                                     final IErrorTaskManager errorTaskManager,
                                     final ManagementActionTaskRepository managementActionTaskRepository,
                                     final JdkParameterRepository jdkParameterRepository,
                                     final CPDRepository cpdRepository,
                                     final IBatchScheduleService batchScheduleService,
                                     final IBatchManagerService batchManagerService,
                                     final IEtherManagerClient etherManagerClient,
                                     final DeploymentUtils deploymentUtils,
                                     final ISchedulerManagerClient schedulerManagerClient,
                                     final INovaActivityEmitter novaActivityEmitter,
                                     final DeploymentServiceAccountableTypeProvider deploymentServiceAccountableTypeProvider)
    {
        this.deploymentsService = deploymentsService;
        this.planBuilder = planBuilder;
        this.numberBuilder = numberBuilder;
        this.usersService = usersService;
        this.statusService = statusService;
        this.libraryManagerService = libraryManagerService;
        this.gcspService = gcspService;
        this.novaPlannedService = novaPlannedService;
        this.deploymentChangeService = deploymentChangeService;
        this.deploymentManagerService = deploymentManagerService;
        this.taskProcessor = taskProcessor;
        this.changesBuilder = changesBuilder;
        this.deploymentsValidator = deploymentsValidator;
        this.deploymentServiceRepository = deploymentServiceRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.releaseVersionRepository = versionRepository;
        this.deploymentSubsystemRepository = deploymentSubsystemRepository;
        this.manageValidationUtils = manageValidationUtils;
        this.errorTaskManager = errorTaskManager;
        this.managementActionTaskRepository = managementActionTaskRepository;
        this.jdkParameterRepository = jdkParameterRepository;
        this.cpdRepository = cpdRepository;
        this.batchScheduleService = batchScheduleService;
        this.batchManagerService = batchManagerService;
        this.etherManagerClient = etherManagerClient;
        this.deploymentUtils = deploymentUtils;
        this.schedulerManagerClient = schedulerManagerClient;
        this.novaActivityEmitter = novaActivityEmitter;
        this.deploymentServiceAccountableTypeProvider = deploymentServiceAccountableTypeProvider;
    }

    @Override
    public DeploymentServiceDto getDeploymentService(final Integer deploymentServiceId, final Integer deploymentId) throws NovaException
    {
        // Get the service
        DeploymentService deploymentService = deploymentServiceRepository.findById(deploymentServiceId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(deploymentServiceId),
                        "Service " + deploymentServiceId + " not found when trying to get deployment service info"));

        // Build the DTO and return.
        return planBuilder.buildDtoFromEntity(deploymentService);
    }

    @Override
    @Transactional
    public DeploymentDto[] getDeploymentPlansByEnvironment(final String ivUser, final Integer productId, final String environment, final String status)
    {
        List<DeploymentPlan> plans;

        // By default, get all from the environment. EXCEPT Stored Plans
        if (StringUtils.isEmpty(status))
        {
            plans = this.deploymentPlanRepository.getByProductAndEnvironment(productId, environment);
            LOG.debug("status isEmpty, plans recovered {} : ", plans);
        }
        // If status is not empty, get only those plans that have that status.
        else
        {
            plans = this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(productId, environment, DeploymentStatus.valueOf(status));
            LOG.debug("status is NOT Empty, plans recovered {} : ", plans);
        }

        // Build a DTO with info from all the plans.
        return this.planBuilder.build(plans, ivUser);

    }

    @Transactional
    public DeploymentDtoPage getDeploymentPlansByEnvironmentsAndStatuses(final String ivUser, final Integer productId, String environments, String statuses, Integer pageSize, Integer pageNumber)
    {
        final char SEPARATOR_CHAR = ':';
        final int DEFAULT_PAGESIZE = 10;
        final int DEFAULT_PAGENUMBER = 0;

        // Set default values when not present
        if (pageNumber == null || pageNumber < 0)
        {
            pageNumber = DEFAULT_PAGENUMBER;
        }

        if (pageSize == null || pageSize < 1)
        {
            pageSize = DEFAULT_PAGESIZE;
        }

        List<String> filteredEnvironments = environments != null && environments.length() > 0 ? Arrays.stream(StringUtils.split(environments, SEPARATOR_CHAR)).collect(Collectors.toList()) : null;

        List<DeploymentStatus> filteredStatuses = statuses != null && statuses.length() > 0 ? Arrays.stream(StringUtils.split(statuses, SEPARATOR_CHAR)).map(DeploymentStatus::valueOf).collect(Collectors.toList())
                : List.of(DeploymentStatus.STORAGED, DeploymentStatus.UNDEPLOYED, DeploymentStatus.REJECTED);

        LOG.debug("[{}] -> [getDeploymentPlansPaginated]: Running for params: ivUser: [{}], productId: [{}], environments: [{}], statuses: [{}], pageSize: [{}], pageNumber: [{}]",
                this.getClass().getSimpleName(), ivUser, productId, environments, statuses, pageSize, pageNumber);

        // Found plans list
        Page<DeploymentPlan> deploymentPlans = this.deploymentPlanRepository.getByProductAndEnvironmentsAndStatuses(
                productId,
                filteredEnvironments,
                filteredStatuses,
                PageRequest.of(pageNumber, pageSize));

        // Build DTOs with all info from the plans
        DeploymentDto[] deploymentDtos = this.planBuilder.build(deploymentPlans.getContent(), ivUser);

        // Prepare the page object to return
        DeploymentDtoPage page = new DeploymentDtoPage();
        page.setTotalElementsCount((int) deploymentPlans.getTotalElements());
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        page.setElements(deploymentDtos);

        return page;
    }

    @Override
    public ServiceStatusDTO getServiceStatus(final Integer subsystemId, final Integer serviceId, final Integer deploymentId, final String environment)
    {
        return this.numberBuilder.buildServiceStatusDTO(deploymentId, serviceId);
    }

    @Override
    public TodoTaskResponseDTO changeDeploymentType(final String ivUser, final DeploymentTypeChangeDto changeType, final Integer deploymentId)
    {
        // Check permissions
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "deployment plan [" + deploymentId + NOT_FOUND_SUFFIX));
        String env = plan.getEnvironment();

        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(ivUser, DeploymentConstants.EDIT_DEPLOYMENT_TYPE_PERMISSION, env, productId, PERMISSION_DENIED);

        // Change the type.
        return this.deploymentsService.changeDeploymentType(ivUser, deploymentId, changeType);
    }

    @Override
    public ActionStatus getDeploymentPlanStatus(final Integer deploymentId) throws NovaException
    {
        return this.statusService.getDeploymentPlanStatus(deploymentId);
    }

    @Override
    public void promotePlanToEnvironment(final String ivUser, final Integer deploymentId, final String environment)
    {
        // Get the plan.
        DeploymentPlan originalPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "Deployment plan [" + deploymentId + NOT_FOUND_SUFFIX));

        // If the plan does not exist, throw exception.
        this.deploymentsValidator.checkPlanExistence(originalPlan);
        // If the plan has any ePhoenix for development environment not promotable, it cannot be promoted, throw exception.
        this.deploymentsValidator.checkPlanWithEphoenixDevelopmentEnvironmentNotPromotable(originalPlan);
        // Check stored release version
        this.deploymentsValidator.checkReleaseVersionStored(originalPlan);
        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentPlan(originalPlan, environment);
        // Check permissions
        String env = originalPlan.getEnvironment();
        Integer productId = originalPlan.getReleaseVersion().getRelease().getProduct().getId();

        this.usersService.checkHasPermission(ivUser, DeploymentConstants.PROMOTE_PLAN_PERMISSION, env, productId,
                PERMISSION_DENIED);

        // Check if all deployment services has deployment instances (Only in NOVA deployment mode)
        this.deploymentsValidator.checkDeploymentInstances(originalPlan);

        // Promote the plan.
        this.deploymentsService.promotePlanToEnvironment(originalPlan, Environment.valueOf(environment));
    }

    @Override
    @Transactional
    public void onTaskReply(final Integer taskId, final Integer deploymentId, final String status)
    {
        // Get the plan.
        DeploymentPlan plan = deploymentPlanRepository.findById(deploymentId).orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "Deployment plan [" + deploymentId + NOT_FOUND_SUFFIX));
        LOG.debug("[DeploymentsApiService] -> [onTaskReply]: find this plan: [{}]", plan);

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(plan);

        // Process the task.
        taskProcessor.onTaskReply(plan, taskId, status);

        LOG.debug("[DeploymentsApiService] -> [onTaskReply]: DONE this on task reply: [{}]", plan);
    }

    @Override
    public DeploymentServiceDto getDeploymentServiceByName(final String environment, final String serviceName,
                                                           final String subsystemName, final String releaseName, final String productName)
    {
        DeploymentService deploymentService = deploymentsService.getDeploymentServiceByName(productName, environment, releaseName, subsystemName, serviceName);
        return this.planBuilder.buildDtoFromEntity(deploymentService);
    }

    @Override
    public SubsystemServicesStatusDTO getDeploymentSubsystemServicesStatus(final Integer subsystemId, final Integer deploymentId, final String environment)
    {
        // Retrieve the deployment plan and the deployment subsystem
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlan(deploymentId);

        // Build the DTO and return.
        return this.numberBuilder.buildSubsystemServiceDTO(deploymentPlan, subsystemId, this.checkClusterOrchestrationHealthy(deploymentPlan));
    }

    @Override
    public void updateDeploymentService(final String ivUser, final DeploymentServiceDto deploymentServiceDto, final Integer deploymentServiceId, final Integer deploymentId)
    {
        // check the deploymentServiceDto.
        deploymentServiceRepository.findById(deploymentServiceId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(deploymentServiceId),
                        "DeploymentService [" + deploymentServiceId + NOT_FOUND_SUFFIX));

        // Get the plan.
        DeploymentPlan originalPlan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(originalPlan);

        String env = originalPlan.getEnvironment();
        Integer productId = originalPlan.getReleaseVersion().getRelease().getProduct().getId();
        // Check if plan can be managed by user
        if (!manageValidationUtils.checkIfPlanCanBeManagedByUser(ivUser, originalPlan))
        {
            throw PERMISSION_DENIED;
        }

        this.usersService.checkHasPermission(ivUser, DeploymentConstants.EDIT_DEPLOYMENT_PERMISSION, env, productId,
                PERMISSION_DENIED);


        DeploymentService deploymentServiceOriginal = originalPlan.getDeploymentSubsystems().stream().map(DeploymentSubsystem::getDeploymentServices).flatMap(Collection::stream).filter(x -> x.getId().equals(deploymentServiceId)).findAny().orElse(null);

        if (deploymentServiceOriginal != null && deploymentServiceDto.getAppliableJvmParameters() != null)
        {
            Boolean jmxNewPlan = this.checkJdkParameterIsSelected(deploymentServiceDto.getAppliableJvmParameters());

            boolean jmxOriginalplan = this.checkJvmParameter(DeploymentConstants.JMX_PARAMS_NAME, deploymentServiceOriginal);

            LOG.debug("[DeploymentsApiServiceImpl] -> [updateDeploymentService]: jmxOriginalplan: [{}] and newPlan : [{}]", jmxOriginalplan, jmxNewPlan);

            if (jmxNewPlan != null && jmxNewPlan != jmxOriginalplan)
            {
                deploymentServiceDto.setIsJmxUpdated(Boolean.TRUE);
            }
        }


        // A deployment Service Dto of a plan from PRO cannot be updated.
        if (Environment.PRO.getEnvironment().equals(originalPlan.getEnvironment()) && deploymentServiceDto.getIsJmxUpdated() == Boolean.FALSE)
        {
            throw new NovaException(DeploymentError.getTriedToUpdateServiceInProError());
        }

        // If the plan was already deployed, cannot be updated.
        if (DeploymentStatus.DEPLOYED == originalPlan.getStatus()
                || DeploymentStatus.UNDEPLOYED == originalPlan.getStatus())
        {
            throw new NovaException(DeploymentError.getTriedToUpdateDeployedPlanError());
        }

        this.deploymentsValidator.checkFilesystemVolumeBindsAreUnique(deploymentServiceDto);

        // Update the deployment Service Dto using data from the DTO.
        this.deploymentsService.updateServiceFromDto(deploymentServiceDto, env);
    }

    private Boolean checkJdkParameterIsSelected(JdkTypedParametersDto appliableJvmParameters)
    {
        Boolean jmxNewPlan = null;

        // Save new jvm options for the deployment service id
        for (JdkParameterTypeDto jdkParameterTypeDto : appliableJvmParameters.getTypedParameters())
        {
            JdkParameterDto[] parameters = jdkParameterTypeDto.getParameters();

            for (JdkParameterDto jdkParameterDto : parameters)
            {
                if (jdkParameterDto.getName().contains(DeploymentConstants.JMX_PARAMS_NAME))
                {
                    jmxNewPlan = jdkParameterDto.getIsSelected();
                    break;
                }
            }
        }
        return jmxNewPlan;
    }

    @Override
    public String[] getLanguagesVersions()
    {
        return LANGUAGE_VERSIONS_VALUES;
    }

    @Override
    public DeploymentPlanCardStatusesDto getDeploymentPlanCardStatuses(final Integer deploymentPlanId, final String userCode) throws NovaException
    {
        // Check deployment plan and build the deployment subsystem status dto depending on the platform of this plan is deployed
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlanDeployed(deploymentPlanId);

        // Start build DeploymentPlanCardStatusesDto result
        DeploymentPlanCardStatusesDto deploymentPlanCardStatusesDto = new DeploymentPlanCardStatusesDto();

        // Set if cluster is available
        final boolean isOrchestrationHealthy = this.checkClusterOrchestrationHealthy(deploymentPlan);
        deploymentPlanCardStatusesDto.setIsOrchestrationHealthy(isOrchestrationHealthy);

        // Set deployment plan action
        deploymentPlanCardStatusesDto.setDeploymentPlanAction(deploymentPlan.getAction().name());

        // Set deployment plan execution date
        if (deploymentPlan.getExecutionDate() != null)
        {
            deploymentPlanCardStatusesDto.setExecutionDate(deploymentPlan.getExecutionDate().getTimeInMillis());
        }

        // Create the "four balls"
        DeploymentPlanCardServiceStatusDto batchServices = new DeploymentPlanCardServiceStatusDto();
        batchServices.setServiceType("batch");
        // This value at this moment is always 0 (it does not show or required in front end)
        batchServices.setTotal(0);
        // Call async to batch manager to get the current running batch in this deployment plan
        CompletableFuture<Integer> completableBatchRunningFuture = this.batchManagerService.getRunningInstancesByDeploymentPlan(deploymentPlanId, isOrchestrationHealthy);

        int totalServicesForButtonAction = 0;
        int totalRunningForButtonAction = 0;
        int totalStoppedForButtonAction = 0;

        Map<String, DeploymentPlanCardServiceStatusDto> serviceTypeCounterMap = deploymentServiceAccountableTypeProvider.getServiceTypeCounterEmptyMap();

        // Iterating for counting the number of each service running (excepting on batch, batch scheduler, libraries and dependencies)
        List<DeploySubsystemStatusDTO> deploymentSubsystemStatusDtoArray = this.buildDeploySubsystemStatusDtoList(deploymentPlan, isOrchestrationHealthy);
        for (DeploySubsystemStatusDTO deploySubsystemStatusDTO : deploymentSubsystemStatusDtoArray)
        {
            for (DeployServiceStatusDTO deployServiceStatusDTO : deploySubsystemStatusDTO.getServices())
            {
                String deploymentServiceType = deployServiceStatusDTO.getServiceType();
                if (!deploymentServiceAccountableTypeProvider.isValidType(deploymentServiceType))
                {
                    LOG.warn("[DeploymentsApiServiceImpl] -> [getDeploymentPlanCardStatuses]: the service type: [{}] is not valid", deploymentServiceType);
                }
                else if (!deploymentServiceAccountableTypeProvider.isAccountableType(deploymentServiceType))
                {
                    LOG.debug("[DeploymentsApiServiceImpl] -> [getDeploymentPlanCardStatuses]: this service type: [{}] will be ignored and not counter.", deploymentServiceType);
                }
                else
                {
                    String accountableType = deploymentServiceAccountableTypeProvider.getAccountableTypeFrom(deploymentServiceType);
                    DeploymentPlanCardServiceStatusDto counterMapDto = serviceTypeCounterMap.get(accountableType);
                    DeployStatusDTO currentServiceStatus = deployServiceStatusDTO.getStatus();
                    counterMapDto.setRunning(counterMapDto.getRunning() + currentServiceStatus.getRunning());
                    counterMapDto.setTotal(counterMapDto.getTotal() + currentServiceStatus.getTotal());

                    // Obtain the final counter (avoid this kind of service type)
                    ServiceType serviceType = ServiceType.valueOf(deploymentServiceType);
                    if (this.deploymentsValidator.isBatchOrDependencyOrBatchScheduleOrLibrary(serviceType))
                    {
                        LOG.debug("[DeploymentsApiServiceImpl] -> [getDeploymentPlanCardStatuses]: this service type: [{}] will be ignored and not counter. Batch services are counted separately in Batch Manager.", serviceType);
                    }
                    else
                    {
                        totalStoppedForButtonAction = totalStoppedForButtonAction + currentServiceStatus.getExited();
                        totalRunningForButtonAction = totalRunningForButtonAction + currentServiceStatus.getRunning();
                        totalServicesForButtonAction = totalServicesForButtonAction + currentServiceStatus.getTotal();
                    }
                }
            }
        }

        // Set the DeploymentPlanCardServiceStatus Dto
        List<DeploymentPlanCardServiceStatusDto> deploymentPlanCardServiceStatusDtoList = new ArrayList<>(serviceTypeCounterMap.values());
        // Complete batch card and add to the dto list async way
        try
        {
            batchServices.setRunning(completableBatchRunningFuture.get());
            deploymentPlanCardServiceStatusDtoList.add(batchServices);
        }
        catch (InterruptedException | ExecutionException e)
        {
            batchServices.setRunning(0);
            LOG.error("[DeploymentsApiServiceImpl] -> [getDeploymentPlanCardStatuses]: there was an error trying to get the future for batch running. Set to 0");
        }

        deploymentPlanCardStatusesDto.setDeploymentPlanServicesStatuses(deploymentPlanCardServiceStatusDtoList.toArray(new DeploymentPlanCardServiceStatusDto[0]));

        // Set deployment plan start bottom
        deploymentPlanCardStatusesDto.setCouldBeStarted((totalServicesForButtonAction > 0 && totalStoppedForButtonAction > 0)
                && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId())
                && this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.START_PLAN_PERMISSION));

        // Set deployment plan stop bottom
        deploymentPlanCardStatusesDto.setCouldBeStopped((totalServicesForButtonAction > 0 && totalRunningForButtonAction > 0)
                && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId())
                && this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.STOP_PLAN_PERMISSION));

        // Set deployment plan restart bottom
        deploymentPlanCardStatusesDto.setCouldBeRestarted((totalServicesForButtonAction > 0 && totalRunningForButtonAction > 0)
                && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId())
                && this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.RESTART_INSTANCE_PERMISSION));

        return deploymentPlanCardStatusesDto;
    }

    @Override
    public DeploymentSubsystemStatusDto[] getDeploymentSubsystemServiceCardStatuses(final Integer deploymentPlanId, final String userCode) throws NovaException
    {
        // Create DTO result and check and get the deployment plan
        List<DeploymentSubsystemStatusDto> result = new ArrayList<>();
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlanDeployed(deploymentPlanId);

        // First of all, calling Batch Manager to get the batch status services (async way)
        List<CompletableFuture<SubsystemBatchServiceStatus>> deploymentServiceBatchStatusList = new ArrayList<>();

        // Check cluster orchestration healthy
        final boolean isClusterOrchestrationHealthy = this.checkClusterOrchestrationHealthy(deploymentPlan);

        deploymentPlan.getDeploymentSubsystems().forEach(deploymentSubsystem ->
        {
            List<Integer> deploymentServiceIdList = deploymentSubsystem.getDeploymentServices().stream()
                    .filter(deploymentService -> ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch()).map(AbstractEntity::getId).collect(Collectors.toList());

            if (deploymentServiceIdList.isEmpty())
            {
                LOG.debug("[DeploymentsApiServiceImpl] -> [getDeploymentSubsystemServiceCardStatuses]: the deployment subsystem id: [{}] does not have any deployment service of batch type", deploymentSubsystem.getId());
            }
            else
            {
                // Call async to get the batch status service of this subsystem
                deploymentServiceBatchStatusList.add(this.batchManagerService.getRunningBatchDeploymentServicesAsync(deploymentServiceIdList.stream().mapToInt(Integer::intValue).toArray(),
                        deploymentPlan.getEnvironment(), deploymentSubsystem.getId(), isClusterOrchestrationHealthy));
            }
        });

        // Next build the deploy subsystem status dto list depending on the platform the plan is deployed
        List<DeploySubsystemStatusDTO> deploymentSubsystemStatusDtoArray = this.buildDeploySubsystemStatusDtoList(deploymentPlan, isClusterOrchestrationHealthy);

        ////////////// Starting to calculate all status of the Subsystem and iterate from each services
        // Get user permissions for subsystems
        boolean canStartSubsystemUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.START_SUBSYSTEM_PERMISSION);
        boolean canStopSubsystemUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.STOP_SUBSYSTEM_PERMISSION);
        boolean canRestartSubsystemUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.RESTART_SUBSYSTEM_PERMISSION);

        // Get user permissions for deployment service
        boolean canStartServiceUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.START_SERVICE_PERMISSION);
        boolean canStopServiceUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.STOP_SERVICE_PERMISSION);
        boolean canRestartServiceUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.RESTART_SERVICE_PERMISSION);

        deploymentSubsystemStatusDtoArray.forEach(deploySubsystemStatusDTO ->
        {
            DeploymentSubsystem deploymentSubsystem = this.deploymentsValidator.validateAndGetDeploymentSubsystem(deploySubsystemStatusDTO.getSubsystemId());

            DeploymentSubsystemStatusDto deploymentSubsystemStatusDto = new DeploymentSubsystemStatusDto();
            deploymentSubsystemStatusDto.setDeploymentSubsystemId(deploySubsystemStatusDTO.getSubsystemId());
            deploymentSubsystemStatusDto.setDeploymentSubsystemAction(deploymentSubsystem.getAction().name());

            // Set if cluster orchestration is healthy
            deploymentSubsystemStatusDto.setIsOrchestrationHealthy(isClusterOrchestrationHealthy);

            // Set subsystem start button
            deploymentSubsystemStatusDto.setCouldBeStarted(canStartSubsystemUserPermission
                    && (deploySubsystemStatusDTO.getStatus().getTotal() > 0 && deploySubsystemStatusDTO.getStatus().getExited() > 0)
                    && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                    && this.isEmptyManagementActionTask(deploymentSubsystem.getId()));

            // Set subsystem stop button
            deploymentSubsystemStatusDto.setCouldBeStopped(canStopSubsystemUserPermission
                    && (deploySubsystemStatusDTO.getStatus().getTotal() > 0 && deploySubsystemStatusDTO.getStatus().getRunning() > 0)
                    && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                    && this.isEmptyManagementActionTask(deploymentSubsystem.getId()));

            // Set subsystem restart button
            deploymentSubsystemStatusDto.setCouldBeRestarted(canRestartSubsystemUserPermission
                    && (deploySubsystemStatusDTO.getStatus().getTotal() > 0 && deploySubsystemStatusDTO.getStatus().getRunning() > 0)
                    && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                    && this.isEmptyManagementActionTask(deploymentSubsystem.getId()));

            List<DeploymentServiceStatusDto> deploymentServiceStatusDtoList = new ArrayList<>();

            ///////// Iterate for each of them deployment service status of the subsystem
            Arrays.stream(deploySubsystemStatusDTO.getServices()).forEach(deployServiceStatusDTO ->
            {
                DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(deployServiceStatusDTO.getServiceId());

                DeploymentServiceStatusDto deploymentServiceStatusDto = new DeploymentServiceStatusDto();
                deploymentServiceStatusDto.setDeploymentServiceId(deployServiceStatusDTO.getServiceId());
                deploymentServiceStatusDto.setDeploymentServiceAction(deploymentService.getAction().name());

                // Set if cluster orchestration is healthy
                deploymentServiceStatusDto.setIsOrchestrationHealthy(isClusterOrchestrationHealthy);

                // Set the appropriate actions for this deployment service
                this.setDeploymentServiceStatusAction(userCode, deploymentPlan, deploymentSubsystem, deployServiceStatusDTO, deploymentService, deploymentServiceStatusDto, false,
                        canStartServiceUserPermission, canStopServiceUserPermission, canRestartServiceUserPermission, isClusterOrchestrationHealthy);
                deploymentServiceStatusDtoList.add(deploymentServiceStatusDto);
            });

            deploymentSubsystemStatusDto.setDeploymentSubsystemServicesStatuses(deploymentServiceStatusDtoList.toArray(new DeploymentServiceStatusDto[0]));
            result.add(deploymentSubsystemStatusDto);
        });

        /////////////// Last part. Add the deployment services of type Batch (this call was made async at the beginning of this method)
        if (deploymentServiceBatchStatusList.isEmpty())
        {
            LOG.debug("[DeploymentsApiServiceImpl] -> [getDeploymentSubsystemServiceCardStatuses]: any deployment subsystem of the deployment plan id: [{}] does not have any deployment service of batch type", deploymentPlan.getId());
        }
        else
        {
            try
            {
                // Wait until all deployment subsystem has been completed all the batch deployment service
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(deploymentServiceBatchStatusList.toArray(new CompletableFuture[0]));

                // When all the Futures are completed, call `future.join()` to get their results and collect the results in a list
                CompletableFuture<List<SubsystemBatchServiceStatus>> allDeploymentServiceBatchStatusListFuture = allFutures.thenApply(v ->
                {
                    LOG.debug("[DeploymentsApiServiceImpl] -> [getDeploymentSubsystemServiceCardStatuses]: waiting to collect all futures");
                    return deploymentServiceBatchStatusList.stream().map(CompletableFuture::join).collect(Collectors.toList());
                });

                // Get the results of all of them
                List<SubsystemBatchServiceStatus> listCompletableFuture = allDeploymentServiceBatchStatusListFuture.toCompletableFuture().get();

                // Iterate for the subsystem list. If if the same subsystem, iterate for each deploymentServiceBatchStatus, find the deployment service id from subsystem status dto
                listCompletableFuture.forEach(subsystemBatchServiceStatus -> result.stream()
                        .filter(deploymentSubsystemStatusDto -> deploymentSubsystemStatusDto.getDeploymentSubsystemId().equals(subsystemBatchServiceStatus.getDeploymentSubsystemId()))
                        .forEach(deploymentSubsystemStatusDto -> Arrays.stream(subsystemBatchServiceStatus.getDeploymentServiceBatchStatusList()).forEach(deploymentServiceBatchStatus ->
                        {
                            DeploymentServiceStatusDto deploymentServiceStatusDto = Arrays.stream(deploymentSubsystemStatusDto.getDeploymentSubsystemServicesStatuses())
                                    .filter(serviceStatusDto -> serviceStatusDto.getDeploymentServiceId().equals(deploymentServiceBatchStatus.getDeploymentServiceId())).findFirst().orElse(null);

                            if (deploymentServiceStatusDto == null)
                            {
                                LOG.warn("[DeploymentsApiServiceImpl] -> [getDeploymentSubsystemServiceCardStatuses]: the deployment service id: [{}] as batch service not found", deploymentServiceBatchStatus.getDeploymentServiceId());
                            }
                            else
                            {
                                // Set is running status
                                deploymentServiceStatusDto.setIsRunning(deploymentServiceBatchStatus.getIsRunning());
                            }
                        })));
            }
            catch (InterruptedException | ExecutionException e)
            {
                LOG.error("[DeploymentsApiServiceImpl] -> [getDeploymentSubsystemServiceCardStatuses]: there was an error trying to get all futures for deplyoment service status waiting to collect all futures");
            }
        }

        return result.toArray(new DeploymentSubsystemStatusDto[0]);
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentInstanceStatusDto[] getDeploymentInstanceCardStatuses(final Integer deploymentServiceId, final String userCode) throws NovaException
    {
        // Create and initialize response
        List<DeploymentInstanceStatusDto> response = new ArrayList<>();

        // Check deployment service and validate deployment instance view conditions
        DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(deploymentServiceId);

        // Initialize empty array, after we'll fill it
        DeployInstanceStatusDTO[] deployInstanceStatusDTOSArray = new DeployInstanceStatusDTO[0];

        // In case the plan was deployed in ether, we check in ether manager, in other case, in the deployment manager
        final boolean isClusterOrchestrationHealthy = this.checkClusterOrchestrationHealthy(deploymentService.getDeploymentSubsystem().getDeploymentPlan());

        if (PlatformUtils.isServiceDeployedInEther(deploymentService))
        {
            // Call Ether manager to get the status of the all deployment instances associated to the deployment service
            deployInstanceStatusDTOSArray = this.deploymentManagerService.getEtherServiceDeploymentInstancesStatus(deploymentService);
        }
        else if (this.deploymentsValidator.checkDeploymentInstanceView(deploymentService))
        {
            // Call Deployment manager to get the status of the all deployment instances associated to the deployment service
            deployInstanceStatusDTOSArray = this.deploymentManagerService.getAllDeploymentInstanceStatus(deploymentServiceId, isClusterOrchestrationHealthy);
        }
        // Get user permissions for deployment instances
        boolean canStartInstanceUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.START_INSTANCE_PERMISSION);
        boolean canStopInstanceUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.STOP_INSTANCE_PERMISSION);
        boolean canRestartInstanceUserPermission = this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.RESTART_INSTANCE_PERMISSION);

        // Build the response from each deployment instance status DTO
        Arrays.stream(deployInstanceStatusDTOSArray)
                .filter(instance -> instance.getInstanceId() != null)
                .forEach(deployInstanceStatusDTO ->
                {
                    DeploymentInstance deploymentInstance = this.deploymentsValidator.validateAndGetDeploymentInstance(deployInstanceStatusDTO.getInstanceId());
                    response.add(this.setDeploymentInstanceStatusDto(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), deploymentService.getDeploymentSubsystem(), deployInstanceStatusDTO,
                            deploymentInstance, canStartInstanceUserPermission, canStopInstanceUserPermission, canRestartInstanceUserPermission, isClusterOrchestrationHealthy));
                });

        // Build response array
        return response.toArray(new DeploymentInstanceStatusDto[0]);
    }

    @Override
    @Transactional(readOnly = true)
    public ActionStatusDTO getDeploymentPlanCardActionStatus(final Integer deploymentPlanId) throws NovaException
    {
        // Check deployment plan
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlanDeployed(deploymentPlanId);

        ActionStatusDTO actionStatusDTO = new ActionStatusDTO();
        actionStatusDTO.setActionStatus(deploymentPlan.getAction().name());
        actionStatusDTO.setRelatedId(deploymentPlanId);

        return actionStatusDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ActionStatusDTO getDeploymentSubsystemCardActionStatus(final Integer deploymentSubsystemId) throws NovaException
    {
        // Check deployment subsystem
        DeploymentSubsystem deploymentSubsystem = this.deploymentsValidator.validateAndGetDeploymentSubsystem(deploymentSubsystemId);

        ActionStatusDTO actionStatusDTO = new ActionStatusDTO();
        actionStatusDTO.setActionStatus(deploymentSubsystem.getAction().name());
        actionStatusDTO.setRelatedId(deploymentSubsystemId);

        return actionStatusDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ActionStatusDTO getDeploymentServiceCardActionStatus(Integer deploymentServiceId) throws NovaException
    {
        // Check deployment service
        DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(deploymentServiceId);

        ActionStatusDTO actionStatusDTO = new ActionStatusDTO();
        actionStatusDTO.setActionStatus(deploymentService.getAction().name());
        actionStatusDTO.setRelatedId(deploymentServiceId);

        return actionStatusDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ActionStatusDTO getDeploymentInstanceCardActionStatus(Integer deploymentInstanceId) throws NovaException
    {
        // Check deployment instance
        DeploymentInstance deploymentInstance = this.deploymentsValidator.validateAndGetDeploymentInstance(deploymentInstanceId);

        ActionStatusDTO actionStatusDTO = new ActionStatusDTO();
        actionStatusDTO.setActionStatus(deploymentInstance.getAction().name());
        actionStatusDTO.setRelatedId(deploymentInstanceId);

        return actionStatusDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public MinimalDeploymentSubsystemStatusDto getDeploymentSubsystemCardRefresh(final String userCode, final Integer deploymentSubsystemId) throws NovaException
    {
        // Get the deployment subsystem and check the deployment plan
        DeploymentSubsystem deploymentSubsystem = this.deploymentsValidator.validateAndGetDeploymentSubsystem(deploymentSubsystemId);
        DeploymentPlan deploymentPlan = deploymentSubsystem.getDeploymentPlan();

        this.deploymentsValidator.validateAndGetDeploymentPlanDeployed(deploymentPlan.getId());

        MinimalDeploymentSubsystemStatusDto minimalDeploymentSubsystemStatusDto = new MinimalDeploymentSubsystemStatusDto();

        DeploySubsystemStatusDTO deploymentSubsystemDtoArray = null;

        // Depending on the platform, build the deployment subsystem status dto
        final boolean isClusterOrchestrationHealthy = this.checkClusterOrchestrationHealthy(deploymentPlan);

        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            if (!ACTION_NOT_ALLOWED.contains(deploymentPlan.getAction()))
            {
                deploymentSubsystemDtoArray = this.deploymentUtils.buildDeploySubsystemStatusDtoList(this.etherManagerClient.getSubsystemStatus(new EtherSubsystemDTO[]{this.deploymentUtils.buildEtherSubsystemDTO(deploymentSubsystem)})).get(0);
            }
        }
        else
        {
            // Call Deployment manager to get the status of the plan just for one
            deploymentSubsystemDtoArray = this.deploymentManagerService.getDeploymentSubsystemServicesStatus(new int[]{deploymentSubsystemId}, isClusterOrchestrationHealthy)[0];
        }

        // Set cluster orchestration healthy
        minimalDeploymentSubsystemStatusDto.setIsOrchestrationHealthy(isClusterOrchestrationHealthy);

        // Create minimal Deployment Subsystem Status Dto result
        minimalDeploymentSubsystemStatusDto.setDeploymentSubsystemId(deploymentSubsystemId);
        minimalDeploymentSubsystemStatusDto.setDeploymentSubsystemAction(deploymentSubsystem.getAction().name());

        // Set actions values
        if (deploymentSubsystemDtoArray == null || deploymentSubsystemDtoArray.getStatus() == null)
        {
            // empty response. Set all to false
            minimalDeploymentSubsystemStatusDto.setCouldBeRestarted(false);
            minimalDeploymentSubsystemStatusDto.setCouldBeStarted(false);
            minimalDeploymentSubsystemStatusDto.setCouldBeStopped(false);
        }
        else
        {
            // Set can be started this deployment subsystem
            minimalDeploymentSubsystemStatusDto.setCouldBeStarted((deploymentSubsystemDtoArray.getStatus().getTotal() > 0 && deploymentSubsystemDtoArray.getStatus().getExited() > 0)
                    && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId())
                    && this.isDeploymentSubsystemInteractive(deploymentSubsystem) && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                    && this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.START_SUBSYSTEM_PERMISSION));

            // Set stop button of deployment subsystem
            minimalDeploymentSubsystemStatusDto.setCouldBeStopped((deploymentSubsystemDtoArray.getStatus().getTotal() > 0 && deploymentSubsystemDtoArray.getStatus().getRunning() > 0)
                    && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId())
                    && this.isDeploymentSubsystemInteractive(deploymentSubsystem) && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                    && this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.STOP_SUBSYSTEM_PERMISSION));

            // Set restart button of deployment subsystem
            minimalDeploymentSubsystemStatusDto.setCouldBeRestarted((deploymentSubsystemDtoArray.getStatus().getTotal() > 0 && deploymentSubsystemDtoArray.getStatus().getRunning() > 0)
                    && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId())
                    && this.isDeploymentSubsystemInteractive(deploymentSubsystem) && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                    && this.canUserInteractWithDeploymentPlan(userCode, deploymentPlan, ServiceRunnerConstants.RESTART_SUBSYSTEM_PERMISSION));
        }

        return minimalDeploymentSubsystemStatusDto;
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentServiceStatusDto getDeploymentServiceCardRefresh(final String userCode, final Integer deploymentServiceId) throws NovaException
    {
        // Obtain and validate deployment service
        DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(deploymentServiceId);

        // Obtain and check deploymentPlan
        DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
        this.deploymentsValidator.validateAndGetDeploymentPlanDeployed(deploymentPlan.getId());

        // Response object to return
        DeploymentServiceStatusDto deploymentServiceStatusDto = new DeploymentServiceStatusDto();

        // The new DTO to fill with the result of the services call
        DeployServiceStatusDTO deployServiceStatusDTO;

        // Depending of the platform, build the deployment service status dto (calling deployment manager or ether manager)
        boolean isOrchestrationHealthy = this.checkClusterOrchestrationHealthy(deploymentPlan);

        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            deployServiceStatusDTO = this.deploymentManagerService.getEtherServiceStatus(deploymentService);
        }
        else
        {
            // Call Deployment manager to get the status of the all deployment instances associated to the deployment service
            deployServiceStatusDTO = this.deploymentManagerService.getServiceStatus(deploymentServiceId, isOrchestrationHealthy);
        }

        // Set deployment service action and deployment service id
        deploymentServiceStatusDto.setDeploymentServiceId(deploymentServiceId);
        deploymentServiceStatusDto.setDeploymentServiceAction(deploymentService.getAction().name());

        // Set the appropriate actions for this deployment service
        this.setDeploymentServiceStatusAction(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), deploymentService.getDeploymentSubsystem(),
                deployServiceStatusDTO, deploymentService, deploymentServiceStatusDto, true,
                this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.START_SERVICE_PERMISSION),
                this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.STOP_SERVICE_PERMISSION),
                this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.RESTART_SERVICE_PERMISSION),
                isOrchestrationHealthy);

        // Set is orchestration healthy
        deploymentServiceStatusDto.setIsOrchestrationHealthy(isOrchestrationHealthy);

        return deploymentServiceStatusDto;
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentInstanceStatusDto getDeploymentInstanceCardRefresh(final String userCode, final Integer deploymentInstanceId) throws NovaException
    {
        // Check deployment instance and the check instance view
        DeploymentInstance deploymentInstance = this.deploymentsValidator.validateAndGetDeploymentInstance(deploymentInstanceId);

        // Getting deployment service
        DeploymentService deploymentService = deploymentInstance.getService();

        // Initialize variable response to return
        DeploymentInstanceStatusDto response = new DeploymentInstanceStatusDto();

        // Initialize the instanceStatus to check
        DeployInstanceStatusDTO deployInstanceStatusDTO = new DeployInstanceStatusDTO();

        // Depending of the platform, build the deployment service status dto (calling deployment manager or ether manager)
        boolean isOrchestrationHealthy = this.checkClusterOrchestrationHealthy(deploymentService.getDeploymentSubsystem().getDeploymentPlan());

        // Obtaining the deployment instance status DTO in correct client call depending on the nature of the deployed plan
        if (this.deploymentsValidator.checkDeploymentInstanceView(deploymentService))
        {
            if (PlatformUtils.isServiceDeployedInEther(deploymentService))
            {
                //Always must be a list of 1 element, return first
                deployInstanceStatusDTO = this.deploymentManagerService.getEtherServiceDeploymentInstancesStatus(deploymentInstance)[0];
            }
            else
            {
                // Call Deployment manager to get the status of the deployment instances associated to the deployment instance
                deployInstanceStatusDTO = this.deploymentManagerService.getDeploymentInstanceStatusById(deploymentInstanceId, isOrchestrationHealthy);

            }
        }

        if (deployInstanceStatusDTO != null)
        {
            // Return the deployment instance status dto
            response = this.setDeploymentInstanceStatusDto(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), deploymentService.getDeploymentSubsystem(),
                    deployInstanceStatusDTO, deploymentInstance,
                    this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.START_INSTANCE_PERMISSION),
                    this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.STOP_INSTANCE_PERMISSION),
                    this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(), ServiceRunnerConstants.RESTART_INSTANCE_PERMISSION),
                    isOrchestrationHealthy);
        }

        return response;
    }

    @Override
    public void unschedule(final String ivUser, final Integer deploymentId)
    {
        // Check deployment plan
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));
        this.deploymentsValidator.checkPlanExistence(deploymentPlan);

        // Check permissions
        String env = deploymentPlan.getEnvironment();
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(ivUser, DeploymentConstants.UNSCHEDULE_PLAN, env, productId, PERMISSION_DENIED);
        this.deploymentsService.unschedule(ivUser, deploymentId);
    }

    @Override
    @Transactional
    public void updateDeploymentPlan(final String ivUser, final DeploymentDto deploymentDto, final Integer deploymentId)
    {
        // Get the original deployment plan .
        DeploymentPlan originalDeploymentPlan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));

        // If the deploymentDto does not exist, throw exception.
        this.deploymentsValidator.checkPlanExistence(originalDeploymentPlan);

        String env = originalDeploymentPlan.getEnvironment();
        Integer productId = originalDeploymentPlan.getReleaseVersion().getRelease().getProduct().getId();

        // Check if plan can be managed by user
        if (!manageValidationUtils.checkIfPlanCanBeManagedByUser(ivUser, originalDeploymentPlan))
        {
            throw PERMISSION_DENIED;
        }

        this.usersService.checkHasPermission(ivUser, DeploymentConstants.EDIT_DEPLOYMENT_PERMISSION, env, productId,
                PERMISSION_DENIED);

        // If the deploymentDto was already deployed, cannot be updated.
        if (DeploymentStatus.DEPLOYED == originalDeploymentPlan.getStatus()
                || DeploymentStatus.UNDEPLOYED == originalDeploymentPlan.getStatus()
                || DeploymentStatus.SCHEDULED == originalDeploymentPlan.getStatus())
        {
            throw new NovaException(DeploymentError.getTriedToUpdateDeployedPlanError(), "");
        }

        this.deploymentsValidator.checkFilesystemVolumeBindsAreUnique(deploymentDto);

        if (originalDeploymentPlan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            if (originalDeploymentPlan.getDeploymentTypeInPro() == DeploymentType.PLANNED)
            {
                // Validate date deployments calendar
                this.validateDateInDeploymentsCalendar(deploymentDto.getGcsp().getExpectedDeploymentDate(), deploymentDto.getUuaa(), originalDeploymentPlan.getSelectedDeploy());

                this.gcspService.updateGcsp(originalDeploymentPlan, deploymentDto.getGcsp());

                Integer undeployPlanId = 0;
                String deploymentDate = "";

                if (deploymentDto.getGcsp() != null)
                {
                    undeployPlanId = deploymentDto.getGcsp().getUndeployRelease();
                    deploymentDate = deploymentDto.getGcsp().getExpectedDeploymentDate();
                }

                // Emit Deployment Type Configured Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(productId, ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYMENT_CONFIGURED)
                        .entityId(deploymentId)
                        .environment(env)
                        .addParam(RELEASE_VERSION_ID_PARAM, originalDeploymentPlan.getReleaseVersion().getId())
                        .addParam(DEPLOYMENT_STATUS_PARAM, originalDeploymentPlan.getStatus().getDeploymentStatus())
                        .addParam(RELEASE_VERSION_NAME_PARAM, originalDeploymentPlan.getReleaseVersion().getVersionName())
                        .addParam(RELEASE_NAME_PARAM, originalDeploymentPlan.getReleaseVersion().getRelease().getName())
                        .addParam(UNDEPLOY_PLAN_ID_PARAM, undeployPlanId)
                        .addParam(DEPLOYMENT_DATE_PARAM, deploymentDate)
                        .build());
            }
            else if (originalDeploymentPlan.getDeploymentTypeInPro() == DeploymentType.NOVA_PLANNED)
            {
                // validate date deployments calendar
                validateDateInDeploymentsCalendar(deploymentDto.getNova().getDeploymentDateTime(), deploymentDto.getUuaa(), originalDeploymentPlan.getSelectedDeploy());

                this.novaPlannedService.updateNova(originalDeploymentPlan, deploymentDto.getNova());

                Integer undeployPlanId = 0;
                String deploymentDate = "";

                if (deploymentDto.getNova() != null)
                {
                    undeployPlanId = deploymentDto.getNova().getUndeployRelease();
                    deploymentDate = deploymentDto.getNova().getDeploymentDateTime();
                }

                // Emit Deployment Type Configured Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(productId, ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYMENT_CONFIGURED)
                        .entityId(deploymentId)
                        .environment(env)
                        .addParam(RELEASE_VERSION_ID_PARAM, originalDeploymentPlan.getReleaseVersion().getId())
                        .addParam(DEPLOYMENT_STATUS_PARAM, originalDeploymentPlan.getStatus().getDeploymentStatus())
                        .addParam(RELEASE_VERSION_NAME_PARAM, originalDeploymentPlan.getReleaseVersion().getVersionName())
                        .addParam(RELEASE_NAME_PARAM, originalDeploymentPlan.getReleaseVersion().getRelease().getName())
                        .addParam(UNDEPLOY_PLAN_ID_PARAM, undeployPlanId)
                        .addParam(DEPLOYMENT_DATE_PARAM, deploymentDate)
                        .build());
            }
            else if (originalDeploymentPlan.getDeploymentTypeInPro() == DeploymentType.ON_DEMAND)
            {

                // Emit Deployment Type Configured Deployment Plan Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(productId, ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYMENT_CONFIGURED)
                        .entityId(deploymentId)
                        .environment(env)
                        .addParam(RELEASE_VERSION_ID_PARAM, originalDeploymentPlan.getReleaseVersion().getId())
                        .addParam(DEPLOYMENT_STATUS_PARAM, originalDeploymentPlan.getStatus().getDeploymentStatus())
                        .addParam(RELEASE_VERSION_NAME_PARAM, originalDeploymentPlan.getReleaseVersion().getVersionName())
                        .addParam(RELEASE_NAME_PARAM, originalDeploymentPlan.getReleaseVersion().getRelease().getName())
                        .build());
            }
        }

        // Update the deploymentDto using data from the DTO.
        this.deploymentsService.updatePlanFromDto(deploymentDto, env);


    }

    @Override
    public void updateServiceState(final ServiceStateDTO serviceStateDTO, final Integer serviceId)
    {
        this.deploymentsService.updateServiceState(serviceStateDTO, serviceId);
    }

    @Override
    public void updateSubsystemState(final SubsystemStateDTO subsystemStateDTO, final Integer subsystemId)
    {
        this.deploymentsService.updateSubsystemState(subsystemStateDTO, subsystemId);
    }

    @Override
    public void updateDeploymentPlanState(DeploymentStateDTO deploymentStatusAction, Integer deploymentId)
    {
        this.deploymentsService.updateDeploymentPlanState(deploymentStatusAction, deploymentId);
    }

    @Override
    public DeploymentMigrationDto migratePlan(final String ivUser, final Integer deploymentId, final Integer versionId)
    {
        // Check permissions
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));

        String env = plan.getEnvironment();
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();

        this.usersService.checkHasPermission(ivUser, DeploymentConstants.NEW_DEPLOYMENT_PERMISSION, env, productId,
                PERMISSION_DENIED);

        //check if the release version had been started in the previous environments
        this.deploymentsValidator.checkExistingPlansOfRV(versionId, Environment.valueOf(plan.getEnvironment()));

        //check if the product is configured in MultiCPD and the original plan is on MonoCPD
        this.deploymentsValidator.validateSameCPDConfigOnPRO(plan);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentPlan(plan, plan.getEnvironment());

        // Create a new plan for relase version and copy all data from original plan where possible.
        return this.deploymentsService.migratePlan(deploymentId, versionId);
    }

    @Transactional
    @Override
    public DeploymentChangeDtoPage getHistory(final Integer deploymentId, final Long pageSize, final Long pageNumber)
    {

        // Get the plan.
        final DeploymentPlan plan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));


        // If the plan does not exist, throw exception.
        this.deploymentsValidator.checkPlanExistence(plan);

        // Get a pageable deployment change
        final Page<DeploymentChange> deploymentChangePageable = this.deploymentChangeService.getHistory(deploymentId, pageNumber, pageSize);

        // Build the DTO and return.
        return this.changesBuilder.build(deploymentChangePageable);
    }

    @Override
    public void addDeploymentChange(final DeploymentChangeDto change, final Integer deploymentId)
    {
        deploymentsService.addDeploymentChange(change, deploymentId);
    }

    @Override
    public void deletePlan(final String user, final Integer deploymentId)
    {
        //Check deployment plan
        DeploymentPlan deploymentPlan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));
        this.deploymentsValidator.checkPlanExistence(deploymentPlan);

        // Check permissions
        String env = deploymentPlan.getEnvironment();
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();

        this.usersService.checkHasPermission(user, DeploymentConstants.DELETE_DEPLOYMENT_PERMISSION, env, productId, PERMISSION_DENIED);

        this.deploymentsService.deletePlan(deploymentId);

    }

    @Override
    public ActionStatus getDeploymentSubsystemStatus(final Integer subsystemId)
    {
        return this.statusService.getDeploymentSubsystemStatus(subsystemId);
    }

    @Override
    public DeploymentSummaryDto[] getDeploymentPlansByEnvironmentAndFilters(final String ivUser, final Integer productId, final String environment,
                                                                            final String endDate, final String startDate, final String status)
    {
        List<DeploymentPlan> plans = this.deploymentsService.getDeploymentPlansBetween(environment, productId, parseOptionalDate(startDate), parseOptionalDate(endDate), status);
        List<DeploymentSummaryDto> summaryList = plans.stream().map(plan -> planBuilder.buildSummary(plan, ivUser)).collect(Collectors.toList());
        return summaryList.toArray(DeploymentSummaryDto[]::new);
    }

    @Override
    public void archiveDeploymentPlan(final String user, final Integer deploymentId)
    {
        //Check deployment plan
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));
        this.deploymentsValidator.checkPlanExistence(deploymentPlan);

        // Check permissions
        String env = deploymentPlan.getEnvironment();
        Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(user, DeploymentConstants.ARCHIVE_DEPLOYMENT_PERMISSION, env, productId, PERMISSION_DENIED);

        this.deploymentsService.archivePlan(deploymentId);
    }

    @Transactional
    @Override
    public DeploymentDto copyPlan(final String user, final Integer deploymentId)
    {
        // Check original plan
        DeploymentPlan originalPlan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));
        this.deploymentsValidator.checkPlanExistence(originalPlan);

        //check if services of plan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentPlan(originalPlan, originalPlan.getEnvironment());


        // Check permissions
        String env = originalPlan.getEnvironment();
        Integer productId = originalPlan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(user, DeploymentConstants.COPY_PLAN_PERMISSION, env, productId, PERMISSION_DENIED);

        // Copy the plan from original
        DeploymentPlan copiedPlan = this.deploymentsService.copyPlan(originalPlan);

        // Convert it to a DTO.
        return this.planBuilder.build(copiedPlan, user);
    }

    @Transactional
    @Override
    public DeploymentDto getDeploymentPlan(final String user, final Integer deploymentId)
    {

        // Get the plan.
        DeploymentPlan plan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(plan);

        // Convert it to a DTO.
        return this.planBuilder.build(plan, user);
    }

    @Override
    public TodoTaskResponseDTO deploy(final String user, final Integer deploymentId, final Boolean force) throws NovaException
    {
        // Check plan
        DeploymentPlan plan = this.deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));

        // Check permissions
        String env = plan.getEnvironment();
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();
        this.usersService.checkHasPermission(user, DeploymentConstants.DEPLOY_DEPLOYMENT_PERMISSION, env, productId, PERMISSION_DENIED);

        // Try to deploy. If cannot be done, a request via task will be created.
        return this.deploymentsService.deploy(user, deploymentId, force);
    }

    @Override
    public DeploymentSummaryDto createDeploymentPlan(final String user, final Integer releaseVersionId, final String environment)
    {
        // Check releaseVersion existence and get
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        // check Permissions
        Integer productId;
        if (releaseVersion == null)
        {
            productId = null;
        }
        else
        {
            productId = releaseVersion.getRelease().getProduct().getId();
        }

        this.usersService.checkHasPermission(user, DeploymentConstants.CREATE_DEPLOYMENT_PERMISSION, environment, productId, PERMISSION_DENIED);

        // Create the plan.
        DeploymentPlan deploymentPlan = this.deploymentsService.createDeployment(releaseVersionId, environment, false);

        // Convert deploy plan into DeploymentSummaryDto and return it
        return this.planBuilder.buildSummary(deploymentPlan, user);
    }

    @Override
    public StatusDTO getDeploymentPlanServicesStatus(final Integer deploymentId, final String environment)
    {
        // Build the DTO and return.
        return this.numberBuilder.buildStatusDTO(deploymentId);
    }

    @Override
    public TaskRequestDTO[] getAllConfigurationManagement(final Integer deploymentId)
    {
        return this.deploymentsService.getAllConfigurationManagement(deploymentId);
    }

    @Transactional
    @Override
    public void taskRequest(final String user, final TaskRequestDTO[] task, final Integer deploymentId)
    {
        // [1] Find the Deployment
        DeploymentPlan plan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(),
                        DEPLOYMENT_PLAN_MESSAGE_PREFIX + deploymentId + NOT_FOUND_SUFFIX));

        // If the plan does not exist, throw exception.
        deploymentsValidator.checkPlanExistence(plan);

        // Check Permissions
        String env = plan.getEnvironment();
        Integer productId = plan.getReleaseVersion().getRelease().getProduct().getId();

        this.usersService.checkHasPermission(user, DeploymentConstants.REQUEST_DEPLOYMENT_PROPERTIES_PERMISSION,
                env, productId, PERMISSION_DENIED);

        // Create the Task
        taskProcessor.createConfigManagementTask(task, user, plan);
    }

    @Override
    public ActionStatus getDeploymentInstanceStatus(final Integer instanceId)
    {
        LOG.debug("[getAllDeploymentInstanceStatus] -> Received value for parameter 'instanceId': {}", instanceId);
        ActionStatus outcome = this.statusService.getDeploymentInstanceStatus(instanceId);
        LOG.debug("[getAllDeploymentInstanceStatus] -> 'outcome' value: {}", outcome);
        return outcome;
    }

    @Override
    @Transactional
    public void instanceDeployStatus(final Integer deploymentServiceId, final String statusMessage, final String status)
    {
        DeploymentService deploymentService = deploymentServiceRepository.findById(deploymentServiceId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(deploymentServiceId),
                        "service [" + deploymentServiceId + NOT_FOUND_SUFFIX));

        DeploymentPlan plan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
        if (DeploymentConstants.DEPLOY_SERVICE_STATUS_SUCCESS.equalsIgnoreCase(status))
        {
            deploymentService.setAction(DeploymentAction.READY);
            deploymentServiceRepository.save(deploymentService);
            if (this.checkPlanReady(plan))
            {
                this.deploymentManagerService.deployPlan(plan);
            }
        }
        else
        {
            plan.setStatus(DeploymentStatus.DEFINITION);
            plan.setAction(DeploymentAction.ERROR);
            deploymentService.setAction(DeploymentAction.ERROR);
            deploymentPlanRepository.save(plan);
        }
    }

    @Override
    public SubsystemServicesStatusDTO[] getDeploymentPlanSubsystemsServicesStatus(final Integer deploymentPlanId, final String environment)
    {
        // Retrieve the deployment plan.
        DeploymentPlan deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlan(deploymentPlanId);

        // Build the DTO and return.
        return this.numberBuilder.buildSubsystemsServiceStatusDTO(deploymentPlan, this.checkClusterOrchestrationHealthy(deploymentPlan));
    }

    @Override
    public ActionStatus getDeploymentServiceStatus(final Integer serviceId)
    {
        return this.statusService.getDeploymentServiceStatus(serviceId);
    }

    @Override
    public TodoTaskResponseDTO remove(final String userCode, final Integer deploymentPlanId) throws NovaException
    {
        DeploymentPlan deploymentPlan = null;
        try
        {
            deploymentPlan = this.deploymentsValidator.validateAndGetDeploymentPlanToRemove(deploymentPlanId);
            String environment = deploymentPlan.getEnvironment();
            Integer productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();

            // Check if user have permissions for this product
            this.usersService.checkHasPermission(userCode, DeploymentConstants.UNDEPLOY_DEPLOYMEMT_PERMISSION, environment, productId, PERMISSION_DENIED);

            return this.deploymentsService.undeployPlan(userCode, deploymentPlanId);
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
    public DeploymentDtoPage getDeploymentPlansBySearchTextFilter(String ivUser, Integer productId, String statuses, String searchText, Integer pageSize, Integer pageNumber)
    {

        final char SEPARATOR_CHAR = ':';
        final int DEFAULT_PAGESIZE = 10;
        final int DEFAULT_PAGENUMBER = 0;

        // Set default values when not present
        if (pageNumber == null || pageNumber < 0)
        {
            pageNumber = DEFAULT_PAGENUMBER;
        }

        if (pageSize == null || pageSize < 1)
        {
            pageSize = DEFAULT_PAGESIZE;
        }

        LOG.debug("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]: Running for params: ivUser: [{}], productId: [{}], statuses: [{}], searchText: [{}] pageSize: [{}], pageNumber: [{}]",
                this.getClass().getSimpleName(), ivUser, productId, statuses, searchText, pageSize, pageNumber);

        if (StringUtils.isEmpty(statuses))
        {
            LOG.warn("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : List of statuses to search is empty, returned empty page of deploymentPlans", this.getClass().getSimpleName());
            DeploymentDtoPage page = new DeploymentDtoPage();
            page.setTotalElementsCount(0);
            page.setPageNumber(pageNumber);
            page.setPageSize(pageSize);
            page.setElements(new DeploymentDto[0]);
            return page;
        }

        // check if deploymentStatus list is valid
        List<String> statusList = asList(StringUtils.split(statuses, SEPARATOR_CHAR));
        statusList.stream().filter(status -> !EnumUtils.isValidEnum(DeploymentStatus.class, status)).forEach(status -> {
            LOG.error("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : Error: Invalid status: [{}]", this.getClass().getSimpleName(), status);
            throw new NovaException(DeploymentError.getDeploymentStatusNotValid(status), "The status: [" + status + "] does not exists");
        });

        // Build DTOs with all info from the plans

        Page<DeploymentPlan> deploymentPlans = null;
        if (searchText != null && searchText.length() > 0)
        {
            // Obtain IDs from text
            List<Integer> ids = this.getDigitsFromSearchText(searchText);
            LOG.debug("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : List of IDS to search {} ", this.getClass().getSimpleName(), ids);

            // Obtain words to search from text
            List<String> words = this.getWordsFromSearchText(searchText);

            // Validate params to search
            this.validateSearchText(searchText, ids, words);

            // Search Ids in repository
            if (!ids.isEmpty())
            {
                deploymentPlans = this.deploymentPlanRepository.findByProductAndIdInAndStatusIn(
                        productId,
                        ids,
                        statusList.stream().map(DeploymentStatus::valueOf).collect(Collectors.toList()),
                        PageRequest.of(pageNumber, pageSize));
            }
            try
            {
                // If no IDs and there are words to search -> search condition
                if (deploymentPlans == null && !words.isEmpty())
                {
                    deploymentPlans = this.deploymentPlanRepository.getDeploymentPlanBySearchFilterAndStatusIn(
                            productId,
                            words.get(0),
                            statusList,
                            PageRequest.of(pageNumber, pageSize));
                }
            }
            catch (RuntimeException e)
            {
                Throwable rootCause = com.google.common.base.Throwables.getRootCause(e);
                if (rootCause instanceof SQLException && Constants.POSTGRESQL_SYNTAX_ERROR_CODE.equals(((SQLException) rootCause).getSQLState()))
                {
                    LOG.warn("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : Error sql grammar exception trying to search specific text in plans. Error: {}", this.getClass().getSimpleName(), e.getMessage());
                    LOG.debug("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses] Exception: {}", this.getClass().getSimpleName(), e);
                    throw new NovaException(DeploymentError.getBadGrammarError());
                }
                else
                {
                    LOG.error("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : Exception trying to search specific text in plans. Error: {}", this.getClass().getSimpleName(), e.getMessage());
                    LOG.debug("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses] Exception: {}", this.getClass().getSimpleName(), e);
                    throw e;
                }
            }

            if (deploymentPlans != null && !deploymentPlans.isEmpty())
            {
                LOG.debug("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : Total of Plans found: [{}]", this.getClass().getSimpleName(), deploymentPlans.getTotalElements());
                deploymentPlans.forEach(deploymentPlan -> LOG.debug("[{}] -> [getDeploymentPlansPaginated]  : Plan [{}] found ", this.getClass().getSimpleName(), deploymentPlan.getId()));

            }
            else
            {
                LOG.warn("[{}] -> [getDeploymentPlansByEnvironmentsAndStatuses]  : NO Plans found", this.getClass().getSimpleName());
            }
        }
        // Prepare the page object to return
        DeploymentDtoPage page = new DeploymentDtoPage();
        page.setTotalElementsCount(0);
        page.setElements(new DeploymentDto[0]);
        // Build DTOs with all info from the plans
        if (deploymentPlans != null)
        {
            DeploymentDto[] deploymentDtos = this.planBuilder.build(deploymentPlans.getContent(), ivUser);
            page.setTotalElementsCount((int) deploymentPlans.getTotalElements());
            page.setElements(deploymentDtos);
        }
        page.setPageNumber(pageNumber);
        page.setPageSize(pageSize);
        return page;
    }

    @Override
    public void treatDeployError(final String user, final Integer deploymentId, final Exception exception)
    {
        Integer productId = null;
        String extraInfo = "No se ha podido desplegar el plan con identificador [" + deploymentId + "]";
        IErrorCode errorCode = DeploymentError.getUnexpectedError();
        ErrorMessageType errorMessageType = null;

        if (exception instanceof NovaException)
        {
            NovaError novaError = ((NovaException) exception).getErrorCode();
            extraInfo = extraInfo + ": " + novaError.getErrorMessage() + " || " + novaError.getActionMessage() + " || Http Status Code: " + novaError.getHttpStatus().value();
            errorCode = ((NovaException) exception).getErrorCode();
            errorMessageType = ((NovaException) exception).getNovaError().getErrorMessageType();
        }

        // In case of the error is of type WARN, we do not want to create a to do task.
        if (ErrorMessageType.WARNING == errorMessageType)
        {
            LOG.warn("[DeploymentApiService] -> [treatDeployError]: Deployment plan [{}] could not be deployed because: [{}]", deploymentId, extraInfo);
        }
        // if the error message type is ERROR and the error code is FORCE REPLACE PLAN, we do not want to create a to do task
        else if (DeploymentConstants.DeployErrors.FORCE_REPLACE_PLAN.equals(errorCode.getErrorCode()))
        {
            LOG.warn("[DeploymentApiService] -> [treatDeployError]: expected and known error: [{}] while trying to force the replace plan [{}]", DeploymentConstants.DeployErrors.FORCE_REPLACE_PLAN, deploymentId);
        }
        else
        {
            // Any other case, we want to create a to do task
            Optional<DeploymentPlan> plan = this.deploymentPlanRepository.findById(deploymentId);
            if (plan.isPresent())
            {
                productId = plan.get().getReleaseVersion().getRelease().getProduct().getId();
                LOG.debug("[DeploymentApiService] -> [treatDeployError]: Error deploying [{}] plan of product [{}] " +
                                "with error code [{}] and message: [{}]",
                        deploymentId, productId, errorCode.getErrorCode(), extraInfo);
            }
            else
            {
                LOG.error("[DeploymentApiService] -> [treatDeployError]: Error deploying [{}] plan over unknown product" +
                                "with error code [{}] and message: [{}]",
                        deploymentId, errorCode.getErrorCode(), extraInfo);
            }

            // Create the to do task of type DEPLOY ERROR
            Integer todoTaskId = this.errorTaskManager.createErrorTask(productId, errorCode, extraInfo, ToDoTaskType.DEPLOY_ERROR, exception, user, deploymentId);
            LOG.warn("[DeploymentApiService] -> [treatDeployError]: TODO Task created with id: [{}]", todoTaskId);
        }
    }

    @Override
    public String[] getPlatforms()
    {
        // We are selecting specific values instead of returning them all, for a better control.
        return new String[]{
                Platform.NOVA.name(),
                Platform.ETHER.name(),
                Platform.AWS.getName()
        };
    }

    /**
     * Get minimum requirements of a list of deployment services for libraries or services
     *
     * @param deploymentServiceIds deployment service id list of libraries or services
     * @return DTO with minimum requirements
     */
    @Transactional
    @Override
    public LMLibraryRequirementsFulfilledDTO[] getAllRequirementsOfUsedLibraries(int[] deploymentServiceIds)
    {
        // From the deploymen service ids, get the release version service ids
        Set<Integer> deploymentServiceIdSet = Arrays.stream(deploymentServiceIds).boxed().collect(Collectors.toSet());
        LOG.debug("[{}] -> [getAllRequirementsOfUsedLibraries]: validating requirements for deploymentServiceIds [{}]",
                this.getClass().getSimpleName(), deploymentServiceIdSet);
        List<DeploymentService> deploymentServiceList = deploymentServiceRepository.findByIdIn(deploymentServiceIdSet);

        // Call the library manager with the release version service ids
        int[] releaseVersionServiceIdArray = deploymentServiceList.stream().mapToInt(ds -> ds.getService().getId()).toArray();
        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS =
                libraryManagerService.getAllRequirementsOfUsedLibraries(releaseVersionServiceIdArray);

        Map<Integer, DeploymentService> deploymentServiceMap = createDeploymentServiceByServiceIdsMap(deploymentServiceList);

        List<RequirementNamespace> namespaceRequirementFulfilledDTOList = new ArrayList<>();
        // Validate the requirements
        List<LMLibraryRequirementsFulfilledDTO> requirementsFulfilledDTOList = validateLibraryRequirements(lmLibraryRequirementsDTOS,
                deploymentServiceMap, namespaceRequirementFulfilledDTOList);

        // Group library Namespace requirements by product and environment
        Map<Pair<Product, Environment>, List<RequirementNamespace>> requirementsByProductEnvironment = namespaceRequirementFulfilledDTOList.stream()
                .collect(Collectors.groupingBy(requirement -> new ImmutablePair<>(requirement.getProduct(), requirement.getEnvironment())));

        buildNamespaceRequirementsFulfilled(requirementsFulfilledDTOList, requirementsByProductEnvironment);


        return requirementsFulfilledDTOList.toArray(new LMLibraryRequirementsFulfilledDTO[0]);
    }

    /**
     * Get minimum requirements of all the services of a deployment plan ids array
     *
     * @param deploymentIds deployment plan ids array
     * @return DTO with minimum requirements for all the deployment plans
     */
    @Transactional
    @Override
    public DeploymentPlanLibraryRequirementsDTO[] getAllRequirementsOfUsedLibrariesForPlans(int[] deploymentIds)
    {
        List<DeploymentPlanLibraryRequirementsDTO> deploymentPlanLibraryRequirementsList = new ArrayList<>();

        // Get all the deployment plans
        Set<Integer> deploymentPlanIdSet = Arrays.stream(deploymentIds).boxed().collect(Collectors.toSet());
        LOG.debug("[{}] -> [getAllRequirementsOfUsedLibrariesForPlans]: validating requirements for deploymentPlanIdSet [{}]",
                this.getClass().getSimpleName(), deploymentPlanIdSet);
        List<DeploymentPlan> deploymentPlanList = deploymentPlanRepository.findByIdIn(deploymentPlanIdSet);
        List<RequirementNamespace> namespaceRequirementFulfilledDTOList = new ArrayList<>();
        Map<Integer, List<LMLibraryRequirementsFulfilledDTO>> requirementsDeploymentMap = new HashMap<>();
        // Get all the requirements of each deployment plan
        deploymentPlanList.forEach(
                dp -> {
                    DeploymentPlanLibraryRequirementsDTO dpRequirements = new DeploymentPlanLibraryRequirementsDTO();
                    dpRequirements.setDeploymentPlanId(dp.getId());
                    dpRequirements.setDeploymentRequirementsFulfilled(getAllRequirementsOfUsedLibrariesForPlan(dp, namespaceRequirementFulfilledDTOList));
                    requirementsDeploymentMap.put(dp.getId(), Arrays.asList(dpRequirements.getDeploymentRequirementsFulfilled()));

                    // Check all the requirements
                    dpRequirements.setFulfilled(Arrays.stream(dpRequirements.getDeploymentRequirementsFulfilled())
                            .allMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled)
                    );

                    deploymentPlanLibraryRequirementsList.add(dpRequirements);
                }
        );

        getLibraryRequirementsWithNamespaces(deploymentPlanLibraryRequirementsList, namespaceRequirementFulfilledDTOList, requirementsDeploymentMap);

        return deploymentPlanLibraryRequirementsList.toArray(new DeploymentPlanLibraryRequirementsDTO[0]);
    }

    @Override
    public String[] getServiceGroupingNames(final String filterByDeployed)
    {
        String filterLowerByDeployed = filterByDeployed.toLowerCase();
        switch (filterLowerByDeployed)
        {
            case "all":
                String[] all = Arrays.stream(ServiceGroupingNames.values()).map(Enum::name).sorted().toArray(String[]::new);
                List<String> allWithoutInvalid = new ArrayList<String>(Arrays.asList(all));
                allWithoutInvalid.remove("INVALID");
                all = allWithoutInvalid.toArray(new String[0]);
                return all;
            case "deployed":
                return Arrays.stream(ServiceGroupingNames.values()).filter(ServiceGroupingNames::isDeployed).map(Enum::name).sorted().toArray(String[]::new);
            case "not_deployed":
                return Arrays.stream(ServiceGroupingNames.values()).filter(not(ServiceGroupingNames::isDeployed)).map(Enum::name).sorted().toArray(String[]::new);
            default:
                throw new NovaException(DeploymentError.getInvalidServicesTypes());
        }

    }

    @Override
    public String[] getSubsystemTypes()
    {
        return Arrays.stream(SubsystemType.values()).filter(SubsystemType -> SubsystemType != SubsystemType.INVALID).map(Enum::toString).sorted().toArray(String[]::new);
    }

    @Override
    public String[] getDeploymentStatus()
    {
        return Arrays.stream(DeploymentStatus.values()).filter(status -> status != DeploymentStatus.PENDING_TASKS).map(Enum::toString).sorted().toArray(String[]::new);
    }


    //--------------------------------------------
    //          Private methods
    //--------------------------------------------

    /**
     * Get minimum requirements of all the services of a deployment plan
     *
     * @param deploymentPlan
     * @param namespaceRequirementFulfilledDTOList
     * @return
     */
    private LMLibraryRequirementsFulfilledDTO[] getAllRequirementsOfUsedLibrariesForPlan(DeploymentPlan deploymentPlan, List<RequirementNamespace> namespaceRequirementFulfilledDTOList)
    {
        LOG.debug("[{}] -> [getAllRequirementsOfUsedLibrariesForPlan]: validating requirements for deploymentPlan [{}]",
                this.getClass().getSimpleName(), deploymentPlan.getId());
        List<Integer> releaseVersionServiceIdList = new ArrayList<>();

        // Retrieve the release version service ids.
        deploymentPlan.getDeploymentSubsystems().forEach(
                deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().forEach(
                        deploymentService -> releaseVersionServiceIdList.add(deploymentService.getService().getId())
                )
        );

        // Call the library manager
        int[] releaseVersionServiceIdArray = releaseVersionServiceIdList.stream().mapToInt(i -> i).toArray();
        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = libraryManagerService.
                getAllRequirementsOfUsedLibraries(releaseVersionServiceIdArray);

        Map<Integer, DeploymentService> deploymentServiceMap = findDeploymentServiceByServiceIds(deploymentPlan);

        // validate all the requirements
        List<LMLibraryRequirementsFulfilledDTO> requirementsFulfilledDTOList = validateLibraryRequirements(lmLibraryRequirementsDTOS,
                deploymentServiceMap, namespaceRequirementFulfilledDTOList);

        return requirementsFulfilledDTOList.toArray(new LMLibraryRequirementsFulfilledDTO[0]);
    }

    /**
     * From a deploymentServiceList, create a Map with the deploymentServices as values, and the serviceIds as keys
     *
     * @param deploymentServiceList list of deploymentServices
     * @return Map of deploymentServices with ServiceIds as keys
     */
    private Map<Integer, DeploymentService> createDeploymentServiceByServiceIdsMap(List<DeploymentService> deploymentServiceList)
    {
        Map<Integer, DeploymentService> deploymentServiceMap = new HashMap<>();
        deploymentServiceList.forEach(ds -> deploymentServiceMap.put(ds.getService().getId(), ds));
        return deploymentServiceMap;
    }

    /**
     * From a deploymentPlan, search their associated deployment services.
     *
     * @param deploymentPlan deploymentPlan
     * @return Map of deploymentServices with ServiceIds as keys
     */
    private Map<Integer, DeploymentService> findDeploymentServiceByServiceIds(DeploymentPlan deploymentPlan)
    {
        Map<Integer, DeploymentService> deploymentServiceMap = new HashMap<>();

        List<DeploymentService> deploymentServiceList = deploymentPlan.getDeploymentSubsystems().stream()
                .flatMap(ds -> ds.getDeploymentServices().stream()).collect(Collectors.toList());

        deploymentServiceList
                .forEach(deploymentService -> deploymentServiceMap.putIfAbsent(deploymentService.getService().getId(), deploymentService));
        return deploymentServiceMap;
    }

    /**
     * Validate the lmLibraryRequirementsDTOS requirements array with the deploymentServiceMap map
     *
     * @param lmLibraryRequirementsDTOS
     * @param deploymentServiceMap
     * @param namespaceRequirementFulfilledDTOList
     * @return
     */
    private List<LMLibraryRequirementsFulfilledDTO> validateLibraryRequirements
    (LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS, Map<Integer, DeploymentService> deploymentServiceMap, List<RequirementNamespace> namespaceRequirementFulfilledDTOList)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("[{}] -> [validateLibraryRequirements]: validating requirements for lmLibraryRequirementsDTOS [{}] with deploymentServiceMap [{}]",
                    this.getClass().getSimpleName(), lmLibraryRequirementsDTOS, deploymentServiceMap);
        }

        List<LMLibraryRequirementsFulfilledDTO> requirementsFulfilledDTOList = new ArrayList<>();

        if (lmLibraryRequirementsDTOS != null)
        {
            Arrays.stream(lmLibraryRequirementsDTOS).forEach(libraryRequirementsDTO -> requirementsFulfilledDTOList.
                    add(validateLMLibraryRequirementsFulfilledDTO(libraryRequirementsDTO, deploymentServiceMap, namespaceRequirementFulfilledDTOList)));
        }

        return requirementsFulfilledDTOList;
    }

    /**
     * Get a list of requirements for libraries with namespaces and search Ether by environment and product if they're ready to deploy
     *
     * @param requirementsFulfilledDTOList
     * @param requirementsByProductEnvironment
     */
    private void buildNamespaceRequirementsFulfilled(List<LMLibraryRequirementsFulfilledDTO> requirementsFulfilledDTOList, Map<Pair<Product, Environment>, List<RequirementNamespace>> requirementsByProductEnvironment)
    {
        requirementsByProductEnvironment.forEach((productEnvironmentPair, requirementsNamespaceList) ->
        {
            // check Ether Namespace by product and environment
            checkEtherNamespaceIsConfigured(productEnvironmentPair.getLeft(), productEnvironmentPair.getRight(), requirementsNamespaceList);

            // group namespace requirements by release version
            Map<Integer, List<RequirementNamespace>> listRequirementsNamespaceGroup = requirementsNamespaceList.stream().collect(Collectors.groupingBy(requirementNamespace -> requirementNamespace.getReleaseVersionServiceId()));

            // get the requirements by service that need namespace and set the result returned by Ether
            listRequirementsNamespaceGroup.forEach((releaseVersionId, listRequirementsNamespace) -> {
                List<LMLibraryRequirementFulfilledDTO> libraryRequirementFulfilledDTOList = new ArrayList<>();

                listRequirementsNamespace.forEach(requirementNamespace -> {
                   if (!libraryRequirementFulfilledDTOList.stream().anyMatch(libraryRequirementFulfilled ->
                            libraryRequirementFulfilled.getRequirementName()
                                    .equals(requirementNamespace.getLmLibraryRequirementFulfilledDTO().getRequirementName())))
                   {
                       libraryRequirementFulfilledDTOList.add(requirementNamespace.getLmLibraryRequirementFulfilledDTO());
                   }
                });

                List<LMLibraryRequirementsFulfilledDTO> requirementsFilterList = requirementsFulfilledDTOList.stream()
                        .filter(libraryRequirementFulfilledDTO -> libraryRequirementFulfilledDTO.getReleaseVersionServiceId().compareTo(releaseVersionId) == 0).collect(Collectors.toList());

                requirementsFilterList.forEach(requirementsFulfilledDTO -> {
                    requirementsFulfilledDTO.setRequirements(libraryRequirementFulfilledDTOList.toArray(new LMLibraryRequirementFulfilledDTO[0]));

                    // Check all the requirements
                    requirementsFulfilledDTO.setFulfilled(libraryRequirementFulfilledDTOList.stream().allMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                    );
                });
            });
        });
    }

    /**
     * Validate the libraryRequirementsDTO requirement with the deploymentServiceMap map
     *
     * @param libraryRequirementsDTO
     * @param deploymentServiceMap
     * @param namespaceRequirementFulfilledDTOList
     * @return
     */
    private LMLibraryRequirementsFulfilledDTO validateLMLibraryRequirementsFulfilledDTO
    (LMLibraryRequirementsDTO libraryRequirementsDTO, Map<Integer, DeploymentService> deploymentServiceMap,
     List<RequirementNamespace> namespaceRequirementFulfilledDTOList)
    {
        LMLibraryRequirementsFulfilledDTO requirementsFulfilledDTO = new LMLibraryRequirementsFulfilledDTO();
        requirementsFulfilledDTO.setFullName(libraryRequirementsDTO.getFullName());
        requirementsFulfilledDTO.setReleaseVersionServiceId(libraryRequirementsDTO.getReleaseVersionServiceId());
        requirementsFulfilledDTO.setDeploymentServiceId(
                deploymentServiceMap.get(libraryRequirementsDTO.getReleaseVersionServiceId()).getId()
        );

        List<LMLibraryRequirementFulfilledDTO> libraryRequirementFulfilledDTOList = new ArrayList<>();

        Arrays.stream(libraryRequirementsDTO.getRequirements()).forEach(
                lmLibraryRequirementDTO -> {
                    DeploymentService deploymentService = deploymentServiceMap.get(libraryRequirementsDTO.getReleaseVersionServiceId());
                    LMLibraryRequirementFulfilledDTO lmLibraryRequirementFulfilledDTO = validateLibraryRequirement(lmLibraryRequirementDTO, deploymentService);
                    if (isResourceAndNameValid(lmLibraryRequirementDTO))
                    {
                        lmLibraryRequirementFulfilledDTO.setFulfilled(checkRequirementLibrary(lmLibraryRequirementDTO, deploymentService));
                        libraryRequirementFulfilledDTOList.add(lmLibraryRequirementFulfilledDTO);
                    }
                    else if (isInstallationType(lmLibraryRequirementDTO.getRequirementType()) &&
                            NAMESPACE.name().equals(lmLibraryRequirementDTO.getRequirementName()))
                    {
                        RequirementNamespace requirementNamespace = new RequirementNamespace();
                        // get Product
                        final Product product = deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct();

                        // get environment
                        final Environment environment = Environment.valueOf(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());

                        requirementNamespace.setDeploymentService(deploymentService);
                        requirementNamespace.setLmLibraryRequirementFulfilledDTO(lmLibraryRequirementFulfilledDTO);
                        requirementNamespace.setEnvironment(environment);
                        requirementNamespace.setProduct(product);
                        requirementNamespace.setReleaseVersionServiceId(deploymentService.getService().getId());

                        // add in memory the libraries when they have namespaces requirements, after group by product and environment
                        namespaceRequirementFulfilledDTOList.add(requirementNamespace);
                    }
                    else
                    {
                        lmLibraryRequirementFulfilledDTO.setFulfilled(true);
                        libraryRequirementFulfilledDTOList.add(lmLibraryRequirementFulfilledDTO);
                    }
                }
        );

        requirementsFulfilledDTO.setRequirements(libraryRequirementFulfilledDTOList.toArray(new LMLibraryRequirementFulfilledDTO[0]));

        // Check all the requirements
        requirementsFulfilledDTO.setFulfilled(
                libraryRequirementFulfilledDTOList.stream().allMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
        );

        return requirementsFulfilledDTO;
    }


    /**
     * Check if the library requirement is a resource and if it is valid.
     *
     * @param lmLibraryRequirementDTO
     * @return
     */
    private boolean isResourceAndNameValid(LMLibraryRequirementDTO lmLibraryRequirementDTO)
    {
        return isResourceType(lmLibraryRequirementDTO.getRequirementType()) &&
                        lmLibraryRequirementDTO.getRequirementName() != null &&
                        EnumUtils.isValidEnum(RESOURCE_REQUIREMENT_NAME.class, lmLibraryRequirementDTO.getRequirementName());
    }

    /**
     * Validate the libraryRequirementsDTO requirement with the associated deploymentService
     *
     * @param lmLibraryRequirementDTO requirement
     * @param deploymentService       deploymentService
     * @return LMLibraryRequirementFulfilledDTO
     */
    private LMLibraryRequirementFulfilledDTO validateLibraryRequirement
    (LMLibraryRequirementDTO lmLibraryRequirementDTO, DeploymentService deploymentService)
    {
        LOG.debug("[{}] -> [validateLibraryRequirements]: validating requirement [{}]",
                this.getClass().getSimpleName(), lmLibraryRequirementDTO.getRequirementName());
        LMLibraryRequirementFulfilledDTO requirementFulfilledDTO = new LMLibraryRequirementFulfilledDTO();
        requirementFulfilledDTO.setRequirementName(lmLibraryRequirementDTO.getRequirementName());
        requirementFulfilledDTO.setRequirementValue(lmLibraryRequirementDTO.getRequirementValue());
        requirementFulfilledDTO.setRequirementDescription(lmLibraryRequirementDTO.getRequirementDescription());
        requirementFulfilledDTO.setRequirementType(lmLibraryRequirementDTO.getRequirementType());
        if (ServiceType.isLibrary(deploymentService.getService().getServiceType()))
        {
            requirementFulfilledDTO.setFulfilled(true);
        }
        return requirementFulfilledDTO;
    }

    /**
     * Validate the if the libraryRequirementsDTO requirement fulfills
     * This method tests the requirements of name RESOURCE_REQUIREMENT_NAME with the values configured in the deployment plan:
     * -CPU: The deployment plan has configured the required cpu.
     * -MEMORY: The deployment plan has configured the required memory.
     * -FILE_SYSTEM: The deployment plan has configured a file system.
     * -CONNECTORS: The deployment plan has configured the required connectors.
     * <p>
     * If the requirement is not a RESOURCE_REQUIREMENT_NAME, returns true.
     *
     * @param lmLibraryRequirementDTO requirement
     * @param deploymentService       deploymentService
     * @return true if fulfills
     */
    private boolean checkRequirementLibrary(LMLibraryRequirementDTO lmLibraryRequirementDTO, DeploymentService deploymentService)
    {
        boolean result = true;

        RESOURCE_REQUIREMENT_NAME requirementName = RESOURCE_REQUIREMENT_NAME.valueOf(lmLibraryRequirementDTO.getRequirementName());

        switch (requirementName)
          {
                case CPU:
                    result = checkCPU(lmLibraryRequirementDTO.getRequirementValue(), deploymentService);
                    break;
                case MEMORY:
                    result = checkMemory(lmLibraryRequirementDTO.getRequirementValue(), deploymentService);
                    break;
                case FILE_SYSTEM:
                    result = checkFileSystem(deploymentService);
                    break;
                case CONNECTORS:
                    result = checkConnector(lmLibraryRequirementDTO.getRequirementValue(), deploymentService);
                    break;
                case INSTANCES_NUMBER:
                    result = checkInstancesNumber(lmLibraryRequirementDTO.getRequirementValue(), deploymentService);
                    break;
                case JVM:
                    result = checkJVM(lmLibraryRequirementDTO.getRequirementValue(), deploymentService);
                    break;
                case JVM_PARAMETER:
                    result = checkJvmParameter(lmLibraryRequirementDTO.getRequirementValue(), deploymentService);
                    break;
            }

            if (LOG.isDebugEnabled())
            {
                LOG.debug("[{}] -> [checkRequirement]: validating requirement [{}] with value [{}] results [{}]",
                        this.getClass().getSimpleName(), requirementName.name(), lmLibraryRequirementDTO.getRequirementValue(), result);
            }

        // By default, if this requirement does not have validation, return true
        return result;
    }

    /**
     * Check in Ether if the namespace is configured by environment and product when exists library requirements that needs an ether namespace.
     *
     * @param product
     * @param environment
     * @param requirementNamespaceList
     */
    private void checkEtherNamespaceIsConfigured(final Product product, final Environment environment, final List<RequirementNamespace> requirementNamespaceList)
    {
        final CheckReadyToDeployRequestDTO checkReadyToDeployRequestDTO = new CheckReadyToDeployRequestDTO();
        checkReadyToDeployRequestDTO.setEnvironment(environment.name());
        checkReadyToDeployRequestDTO.setNamespace(PlatformUtils.getSelectedDeployNSForProductInEnvironment(product, Environment.valueOf(environment.name())));
        checkReadyToDeployRequestDTO.setGroupId(product.getEtherGIAMProductGroup());
        checkReadyToDeployRequestDTO.setProductName(product.getName());

        // check if it is ready
        boolean isReady = this.etherManagerClient.readyToDeploy(checkReadyToDeployRequestDTO);

        requirementNamespaceList.forEach(requirementNamespace -> {
            requirementNamespace.getLmLibraryRequirementFulfilledDTO().setFulfilled(isReady);
        });
    }

    /**
     * Check if the requirementType is RESOURCE
     *
     * @param requirementType requirementType
     * @return true if it is a RESOURCE
     */
    private boolean isResourceType(String requirementType)
    {
        return requirementType != null &&
                EnumUtils.isValidEnum(REQUIREMENT_TYPE.class, requirementType) &&
                REQUIREMENT_TYPE.RESOURCE.equals(REQUIREMENT_TYPE.valueOf(requirementType));
    }

    /**
     * Check if the requirementType is INSTALLATION
     *
     * @param requirementType requirementType
     * @return true if it is INSTALLATION
     */
    private boolean isInstallationType(String requirementType)
    {
        return requirementType != null &&
                EnumUtils.isValidEnum(REQUIREMENT_TYPE.class, requirementType) &&
                REQUIREMENT_TYPE.INSTALLATION.equals(REQUIREMENT_TYPE.valueOf(requirementType));
    }

    /**
     * Check the number of instances of the requirement
     *
     * @param value             value
     * @param deploymentService deploymentService
     * @return true if fulfills the number of instances
     */
    private boolean checkInstancesNumber(String value, DeploymentService deploymentService)
    {
        try
        {
            //Returns true if the required instances number is less or equals to the configured instances in the plan
            int instancesNumberRequired = Integer.parseInt(value);
            return instancesNumberRequired <= deploymentService.getNumberOfInstances();
        }
        catch (NumberFormatException e)
        {
            LOG.error("[{}] -> [checkInstancesNumber]: error validating instance number requirement for service [{}]. Unsupported value [{}]. "
                            + "This value is validated and informed by librarymanager. Please, check if librarymanager has a bug.",
                    this.getClass().getSimpleName(), deploymentService.getId(), value);
            //unsupported value
            return false;
        }

    }

    /**
     * Check the JVM percent requirement
     *
     * @param value             value
     * @param deploymentService deploymentService
     * @return true if fulfills the number of instances
     */
    private boolean checkJVM(String value, DeploymentService deploymentService)
    {
        try
        {
            //Returns true if the required JVM is less or equals to the configured JVM memory factor in the plan
            int jvmRequired = Integer.parseInt(value);
            return jvmRequired <= deploymentService.getMemoryFactor();
        }
        catch (NumberFormatException e)
        {
            LOG.error("[{}] -> [checkInstancesNumber]: error validating JVM factor requirement for service [{}]. Unsupported value [{}]. "
                            + "This value validated and is informed by librarymanager. Please, check if librarymanager has a bug.",
                    this.getClass().getSimpleName(), deploymentService.getId(), value);
            //unsupported value
            return false;
        }

    }

    /**
     * Check the JVM parameter requirement
     *
     * @param jvmParameter      JVM parameter to check
     * @param deploymentService deploymentService
     * @return true if JVM parameter is set
     */
    private boolean checkJvmParameter(String jvmParameter, DeploymentService deploymentService)
    {
        return jdkParameterRepository.existsByNameAndDeploymentService(jvmParameter, deploymentService.getId());
    }

    /**
     * Check the CPU requirement
     *
     * @param value             value
     * @param deploymentService deploymentService
     * @return true if fulfills the cpu
     */
    private boolean checkCPU(String value, DeploymentService deploymentService)
    {
        //Returns true if the required CPU is less or equals to the configured CPU in the plan
        BigDecimal cpuRequired = new BigDecimal(value);
        return deploymentService.getHardwarePack() != null &&
                cpuRequired.compareTo(BigDecimal.valueOf(deploymentService.getHardwarePack().getNumCPU())) <= 0;

    }

    /**
     * Check the MEMORY requirement
     *
     * @param value             value
     * @param deploymentService deploymentService
     * @return true if fulfills the memory
     */
    private boolean checkMemory(String value, DeploymentService deploymentService)
    {
        try
        {
            //Returns true if the required memory is less or equals to the configured memory in the plan
            int memoryRequired = Integer.parseInt(value);
            return deploymentService.getHardwarePack() != null &&
                    memoryRequired <= deploymentService.getHardwarePack().getRamMB();
        }
        catch (NumberFormatException e)
        {
            LOG.error("[{}] -> [checkInstancesNumber]: error validating Memory requirement for service [{}]. Unsupported value [{}]. "
                            + "This value is validated and informed by librarymanager. Please, check if librarymanager has a bug.",
                    this.getClass().getSimpleName(), deploymentService.getId(), value);
            //unsupported value
            return false;
        }
    }

    /**
     * Check the FILE_SYSTEM requirement
     *
     * @param deploymentService deploymentService
     * @return true if has a fs
     */
    private boolean checkFileSystem(DeploymentService deploymentService)
    {
        //True if the deployment service contains a FS
        return deploymentService.getDeploymentServiceFilesystems() != null &&
                !deploymentService.getDeploymentServiceFilesystems().isEmpty();
    }


    /**
     * Check the Connectors requirement
     *
     * @param value             value
     * @param deploymentService deploymentService
     * @return true has all the conectors
     */
    private boolean checkConnector(String value, DeploymentService deploymentService)
    {
        //True if the deployment service contains a connector with this name
        return deploymentService.getLogicalConnectors() != null &&
                deploymentService.getLogicalConnectors().stream().anyMatch(c -> c.getConnectorType().getName().equalsIgnoreCase(value));
    }

    /**
     * Validate if have a valid search
     *
     * @param searchText The full text to validate
     * @param ids        List of Ids to search to validate
     * @param words      Words to search to validate
     */
    private void validateSearchText(String searchText, List<Integer> ids, List<String> words)
    {
        LOG.debug("[{}] -> [validateSearchText]: validating Seachtext [{}], IDs:[{}] and words to search:[{}]", this.getClass().getSimpleName(), searchText, ids, words);

        // Validate if the searchText param is too long
        if (searchText.length() > DeploymentConstants.MAX_LENGTH_SEARCH_TEXT)
        {
            throw new NovaException(DeploymentError.getSearchTextTooLongError());
        }

        // Check if only of them have data
        if (!ids.isEmpty() && !words.isEmpty())
        {
            LOG.error("[{}] -> [validateSearchText]  : Error: Can only search IDs, or One condition at a time", this.getClass().getSimpleName());
            throw new NovaException(DeploymentError.getInvalidSearchError());
        }

        if (!words.isEmpty() && words.size() > 1)
        {
            LOG.error("[{}] -> [validateSearchText]  : Error: Can only search One condition at a time", this.getClass().getSimpleName());
            throw new NovaException(DeploymentError.getInvalidSearchError());
        }

    }

    /**
     * get words or conditions from SearchText string
     *
     * @param searchText The search text
     * @return The list of conditions to search
     */
    private List<String> getWordsFromSearchText(String searchText)
    {
        List<String> fullList = asList(searchText.trim().split(","));
        List<String> onlyWords = fullList.stream().map(String::trim).filter(str -> !str.matches("[0-9]+")).collect(Collectors.toList());

        LOG.trace("[DeploymentsApiServiceImpl] -> [getWordsFromSearchText]  : Words to search {}", onlyWords);
        return onlyWords;
    }

    /**
     * get digits from SearchText string
     *
     * @param searchText The search text
     * @return The list of IDs to search
     */
    private List<Integer> getDigitsFromSearchText(String searchText)
    {
        List<String> fullList = asList(searchText.trim().split(","));
        List<Integer> onlyNumbers = fullList.stream().map(String::trim).filter(str -> str.matches("[0-9]+")).map(Integer::parseInt).collect(Collectors.toList());

        LOG.trace("[DeploymentsApiServiceImpl] -> [getDigitsFromSearchText]  : Numbers to search {}", onlyNumbers);
        return onlyNumbers;
    }


    /**
     * Get a String and convert to Calendar
     *
     * @param strDate date as String
     * @return {@code null} if strDate is null or {@code Calendar}
     */
    private Calendar parseOptionalDate(String strDate)
    {
        if (StringUtils.isEmpty(strDate))
        {
            return null;
        }
        return DatatypeConverter.parseDateTime(strDate);
    }

    /**
     * Check if a deployment plan is ready
     *
     * @param plan plan to cjeck
     * @return {@code true} if all subsystem are ready. {@code false} another case
     */
    private boolean checkPlanReady(final DeploymentPlan plan)
    {
        boolean allSubsystemsReady = true;
        boolean allServicesReady;
        for (DeploymentSubsystem subsystem : plan.getDeploymentSubsystems())
        {
            allServicesReady = true;
            for (DeploymentService deploymentService : subsystem.getDeploymentServices())
            {
                allServicesReady = allServicesReady && DeploymentAction.READY == deploymentService.getAction();
            }
            if (allServicesReady)
            {
                subsystem.setAction(DeploymentAction.READY);
                this.deploymentSubsystemRepository.save(subsystem);
            }
            allSubsystemsReady = allSubsystemsReady && allServicesReady;
        }
        return allSubsystemsReady;
    }

    /**
     * Build deploy subsystem status dto list from deployment plan depending on the platform deployed
     *
     * @param deploymentPlan         a deployment plan
     * @param isOrchestrationHealthy if cluster orchestration CPD (in NOVA Platform) is healthy
     * @return a deploy subsystem statys dto list
     */
    private List<DeploySubsystemStatusDTO> buildDeploySubsystemStatusDtoList(final DeploymentPlan deploymentPlan, final boolean isOrchestrationHealthy) throws NovaException
    {
        List<DeploySubsystemStatusDTO> deploymentSubsystemStatusDtoArray = new ArrayList<>();

        // Depending on the platform, build the deployment subsystem status dto
        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            if (!ACTION_NOT_ALLOWED.contains(deploymentPlan.getAction()))
            {
                deploymentSubsystemStatusDtoArray = this.deploymentUtils.buildDeploySubsystemStatusDtoList(this.etherManagerClient.getSubsystemStatus(this.deploymentUtils.buildEtherSubsystemDTO(deploymentPlan)));
            }
        }
        else
        {
            // First com.bbva.enoa.platformservices.historicalloaderservice.step, calling async to Deployment Manager to get deployment subsystem status dto
            List<CompletableFuture<DeploySubsystemStatusDTO>> completableFutureDeploySubsystemStatusDTOList = new ArrayList<>();
            for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
            {
                completableFutureDeploySubsystemStatusDTOList.add(this.deploymentManagerService.getAsyncDeploymentSubsystemServicesStatus(deploymentSubsystem.getId(), isOrchestrationHealthy));
            }

            // Finally, wait until all deployment subsystem status has been completed from deployment manager service
            try
            {
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(completableFutureDeploySubsystemStatusDTOList.toArray(new CompletableFuture[0]));

                // When all the Futures are completed, call `future.join()` to get their results and collect the results in a list
                CompletableFuture<List<DeploySubsystemStatusDTO>> allDeploySubsystemStatusDTOListFuture = allFutures.thenApply(v ->
                {
                    LOG.debug("[DeploymentsApiServiceImpl] -> [getDeploymentPlanCardStatuses]: waiting to collect all futures calls for getting deploy subsystem status dto");
                    return completableFutureDeploySubsystemStatusDTOList.stream().map(CompletableFuture::join).collect(Collectors.toList());
                });

                // Get the results of all of them
                deploymentSubsystemStatusDtoArray = allDeploySubsystemStatusDTOListFuture.toCompletableFuture().get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                LOG.error("[DeploymentsApiServiceImpl] -> [getDeploymentPlanCardStatuses]: there was an error trying to get all futures for deploy subsystem status dto waiting to collect " +
                        "all futures calling deployment manager method: [getDeploymentSubsystemServicesStatus]. Error message: [{}]", e.getMessage());
            }
        }

        return deploymentSubsystemStatusDtoArray;
    }

    /**
     * Set to deployment service status, the appropriate actions depending on the type of service
     *
     * @param userCode                        the user code
     * @param deploymentPlan                  the deployment plan
     * @param deploymentSubsystem             the deployment
     * @param deployServiceStatusDTO          deploy service status DTO for knowing the status
     * @param deploymentService               deployment service
     * @param deploymentServiceStatusDto      deployment service status DTO for setting all the actions (start,stop, etc) depending on the status of deployServiceStatusDTO
     * @param calculateBatchServiceStatus     flag that indicate if the batch services have to be calculated
     * @param canRestartServiceUserPermission true if the deployment service can be restart by user permissions, false any case
     * @param canStopServiceUserPermission    true if the deployment service can be stop by user permissions, false any case
     * @param canStartServiceUserPermission   true if the deployment service can be start by user permissions, false any case
     * @param isClusterOrchestrationHealthy   true if the cluster orchestration is healthy. False any case
     */
    private void setDeploymentServiceStatusAction(final String userCode, final DeploymentPlan deploymentPlan, final DeploymentSubsystem deploymentSubsystem, final DeployServiceStatusDTO deployServiceStatusDTO,
                                                  final DeploymentService deploymentService, final DeploymentServiceStatusDto deploymentServiceStatusDto, final boolean calculateBatchServiceStatus,
                                                  final boolean canStartServiceUserPermission, final boolean canStopServiceUserPermission, final boolean canRestartServiceUserPermission,
                                                  final boolean isClusterOrchestrationHealthy)
    {
        switch (ServiceType.valueOf(deploymentService.getService().getServiceType()))
        {
            case NOVA:
            case API_REST_JAVA_SPRING_BOOT:
            case API_JAVA_SPRING_BOOT:
            case EPHOENIX_ONLINE:
            case EPHOENIX_BATCH:
            case API_REST_NODE_JS_EXPRESS:
            case API_REST_PYTHON_FLASK:
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
            case THIN2:
            case NODE:
            case CDN_POLYMER_CELLS:
            case CDN_ANGULAR_THIN2:
            case CDN_ANGULAR_THIN3:
            case DAEMON_JAVA_SPRING_BOOT:
                // Set can be started this deployment service
                deploymentServiceStatusDto.setCouldBeStarted(canStartServiceUserPermission
                        && (deployServiceStatusDTO.getStatus().getTotal() > 0 && deployServiceStatusDTO.getStatus().getExited() > 0)
                        && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                        && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                        && this.isDeploymentServiceInteractive(deploymentService) && this.isEmptyManagementActionTask(deploymentService.getId()));

                // Set stop this deployment service
                deploymentServiceStatusDto.setCouldBeStopped(canStopServiceUserPermission
                        && (deployServiceStatusDTO.getStatus().getTotal() > 0 && deployServiceStatusDTO.getStatus().getRunning() > 0)
                        && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                        && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                        && this.isDeploymentServiceInteractive(deploymentService) && this.isEmptyManagementActionTask(deploymentService.getId()));

                // Set restart this deployment service
                deploymentServiceStatusDto.setCouldBeRestarted(canRestartServiceUserPermission
                        && (deployServiceStatusDTO.getStatus().getTotal() > 0 && deployServiceStatusDTO.getStatus().getRunning() > 0)
                        && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                        && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                        && this.isDeploymentServiceInteractive(deploymentService) && this.isEmptyManagementActionTask(deploymentService.getId()));

                // Set if the service is running, schedule and un-schedule
                deploymentServiceStatusDto.setIsRunning(deployServiceStatusDTO.getStatus().getRunning() > 0);
                deploymentServiceStatusDto.setCouldBeUnscheduled(false);
                deploymentServiceStatusDto.setCouldBeScheduled(false);
                break;

            case BATCH_JAVA_SPRING_BATCH:
            case NOVA_BATCH:
            case NOVA_SPRING_BATCH:
            case BATCH_PYTHON:
            case BATCH_JAVA_SPRING_CLOUD_TASK:
                deploymentServiceStatusDto.setCouldBeRestarted(false);
                deploymentServiceStatusDto.setCouldBeScheduled(false);
                deploymentServiceStatusDto.setCouldBeUnscheduled(false);
                deploymentServiceStatusDto.setCouldBeStopped(false);
                deploymentServiceStatusDto.setCouldBeStarted(false);

                if (calculateBatchServiceStatus)
                {
                    // Call sync to get the batch status service of this deployment service and set the deployment service status dto
                    deploymentServiceStatusDto.setIsRunning(this.batchManagerService.getRunningInstances(deploymentService.getId(), isClusterOrchestrationHealthy).getRunningBatchs() > 0);
                }
                break;

            case DEPENDENCY:
            case LIBRARY_JAVA:
            case LIBRARY_NODE:
            case LIBRARY_PYTHON:
            case LIBRARY_THIN2:
            case LIBRARY_TEMPLATE:
                deploymentServiceStatusDto.setCouldBeRestarted(false);
                deploymentServiceStatusDto.setCouldBeScheduled(false);
                deploymentServiceStatusDto.setCouldBeUnscheduled(false);
                deploymentServiceStatusDto.setCouldBeStopped(false);
                deploymentServiceStatusDto.setCouldBeStarted(false);
                deploymentServiceStatusDto.setIsRunning(false);
                break;

            case BATCH_SCHEDULER_NOVA:

                // Batch scheduler instance cannot be started, restarted or stopped
                deploymentServiceStatusDto.setCouldBeRestarted(false);
                deploymentServiceStatusDto.setCouldBeStopped(false);
                deploymentServiceStatusDto.setCouldBeStarted(false);

                // Get the deployment batch schedule instance to know if can be schedule, un-schedule and the status
                DeploymentBatchScheduleDTO deploymentBatchScheduleDTO = this.batchScheduleService.getDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId());

                if (Strings.isNullOrEmpty(deploymentBatchScheduleDTO.getState()))
                {
                    deploymentServiceStatusDto.setIsRunning(false);
                    deploymentServiceStatusDto.setCouldBeScheduled(false);
                    deploymentServiceStatusDto.setCouldBeUnscheduled(false);
                    LOG.warn("[DeploymentsApiServiceImpl] -> [setDeploymentServiceStatusAction]: the deployment batch instance was not found for release version id: [{}] and deployment plan: [{}]. " +
                            "Set all parameters of the deployment batch schedule service: [{}] to false", deploymentService.getService().getId(), deploymentPlan.getId(), deploymentServiceStatusDto);
                }
                else
                {
                    if (deploymentBatchScheduleDTO.getState().trim().equalsIgnoreCase(DeploymentBatchScheduleStatus.ENABLED.name()))
                    {
                        deploymentServiceStatusDto.setIsRunning(true);
                        deploymentServiceStatusDto.setCouldBeScheduled(false);
                        deploymentServiceStatusDto.setCouldBeUnscheduled(this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(),
                                ServiceRunnerConstants.STOP_BATCH_SCHEDULE_PERMISSION));
                    }
                    else
                    {
                        deploymentServiceStatusDto.setIsRunning(false);
                        deploymentServiceStatusDto.setCouldBeScheduled(this.canUserInteractWithDeploymentPlan(userCode, deploymentService.getDeploymentSubsystem().getDeploymentPlan(),
                                ServiceRunnerConstants.START_BATCH_SCHEDULE_PERMISSION));
                        deploymentServiceStatusDto.setCouldBeUnscheduled(false);
                    }
                }

                break;

            default:
                LOG.warn("[DeploymentsApiServiceImpl] -> [setDeploymentServiceStatusAction]: the service type: [{}] is not valid", deploymentService.getService().getServiceType());
                break;
        }
    }

    /**
     * Get the deployment instance status dto
     *
     * @param deploymentService                deployment service
     * @param deploymentPlan                   a deployment plan
     * @param deploymentSubsystem              the deployment
     * @param deployInstanceStatusDTO          a deploy instance status DTO
     * @param deploymentInstance               a deployment instance
     * @param canRestartInstanceUserPermission true if the user can restart a instance by permissions. False any case.
     * @param canStartInstanceUserPermission   true if the user can start a instance by permissions. False any case.
     * @param canStopInstanceUserPermission    true if the user can stop a instance by permissions. False any case.
     * @param isClusterOrchestrationHealthy    true if the cluster orchestration is healthy. False any case
     * @return a new Deployment Instance status dto
     */
    private DeploymentInstanceStatusDto setDeploymentInstanceStatusDto(final DeploymentService deploymentService, final DeploymentPlan deploymentPlan,
                                                                       final DeploymentSubsystem deploymentSubsystem, final DeployInstanceStatusDTO deployInstanceStatusDTO, final DeploymentInstance deploymentInstance,
                                                                       final boolean canStartInstanceUserPermission, final boolean canStopInstanceUserPermission, final boolean canRestartInstanceUserPermission,
                                                                       final boolean isClusterOrchestrationHealthy)
    {
        DeploymentInstanceStatusDto deploymentInstanceStatusDto = new DeploymentInstanceStatusDto();

        // Set deployment instance id and deployment instance action
        deploymentInstanceStatusDto.setDeploymentInstanceId(deployInstanceStatusDTO.getInstanceId());
        deploymentInstanceStatusDto.setDeploymentInstanceAction(deploymentInstance.getAction().name());

        // Set starting instance button
        deploymentInstanceStatusDto.setCouldBeStarted(canStartInstanceUserPermission
                && (deployInstanceStatusDTO.getStatus().getTotal() > 0 && deployInstanceStatusDTO.getStatus().getExited() > 0)
                && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                && this.isDeploymentServiceInteractive(deploymentService) && this.isEmptyManagementActionTask(deploymentService.getId())
                && this.isDeploymentInstanceInteractive(deploymentInstance) && this.isEmptyManagementActionTask(deploymentInstance.getId()));

        /// Set stopping instance button
        deploymentInstanceStatusDto.setCouldBeStopped(canStopInstanceUserPermission
                && (deployInstanceStatusDTO.getStatus().getTotal() > 0 && deployInstanceStatusDTO.getStatus().getRunning() > 0)
                && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                && this.isDeploymentServiceInteractive(deploymentService) && this.isEmptyManagementActionTask(deploymentService.getId())
                && this.isDeploymentInstanceInteractive(deploymentInstance) && this.isEmptyManagementActionTask(deploymentInstance.getId()));

        // Set restarting instance button
        deploymentInstanceStatusDto.setCouldBeRestarted(canRestartInstanceUserPermission
                && (deployInstanceStatusDTO.getStatus().getTotal() > 0 && deployInstanceStatusDTO.getStatus().getRunning() > 0)
                && this.isDeploymentPlanInteractive(deploymentPlan) && this.isEmptyManagementActionTask(deploymentPlan.getId()) && this.isDeploymentSubsystemInteractive(deploymentSubsystem)
                && this.isEmptyManagementActionTask(deploymentSubsystem.getId())
                && this.isDeploymentServiceInteractive(deploymentService) && this.isEmptyManagementActionTask(deploymentService.getId())
                && this.isDeploymentInstanceInteractive(deploymentInstance) && this.isEmptyManagementActionTask(deploymentInstance.getId()));

        // Set is running
        if (deploymentInstance.getType().equals(DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA))
        {

            deploymentInstanceStatusDto.setIsRunning(deployInstanceStatusDTO.getStatus().getRunning() > 0);
            deploymentInstanceStatusDto.setInstanceServiceType(DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA.toString());

        }
        else if (deploymentInstance.getType().equals(DeploymentInstanceType.DEPLOYMENT_INSTANCE_ETHER))
        {

            // Ether DTO extra params
            deploymentInstanceStatusDto.setIsRunning(deployInstanceStatusDTO.getStatus().getRunning() > 0
                    && (deployInstanceStatusDTO.getStatus().getRunning().equals(deployInstanceStatusDTO.getStatus().getTotal())));

            deploymentInstanceStatusDto.setExternalRunningInstances(deployInstanceStatusDTO.getStatus().getRunning());
            deploymentInstanceStatusDto.setExternalTotalInstances(deployInstanceStatusDTO.getStatus().getTotal());
            deploymentInstanceStatusDto.setInstanceServiceType(DeploymentInstanceType.DEPLOYMENT_INSTANCE_ETHER.toString());
        }

        // Set deployment instance status cluster healthy
        deploymentInstanceStatusDto.setIsOrchestrationHealthy(isClusterOrchestrationHealthy);

        return deploymentInstanceStatusDto;
    }

    private boolean isEmptyManagementActionTask(final Integer relatedId)
    {
        return CollectionUtils.isEmpty(this.managementActionTaskRepository.findByRelatedIdAndStatusIn(relatedId, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)));
    }

    /**
     * This method returns true if the user can interact with the deployment plan. False in any case:
     * Requirements to be able to interact with deployment plan are all included at the same time:
     * -- A. The user must has permissions to the product / environment / and action (start, stop or restart)
     * -- B. The deployment plan must only be in DEPLOYED status
     * -- C. In INT and PRO environment, the button always can be showed to the user.
     * -- D. In PRE environment, the button can be showed only if the user can manged the deployment plan
     *
     * @param permission     the permission to check
     * @param userCode       the user code to check
     * @param deploymentPlan the deployment plan
     * @return true if the user can interact with the deployment plan, false any case
     */
    private boolean canUserInteractWithDeploymentPlan(final String userCode, final DeploymentPlan deploymentPlan, final String permission)
    {
        boolean canBeManagedByUser = false;

        if (deploymentPlan.getStatus().equals(DeploymentStatus.DEPLOYED))
        {
            canBeManagedByUser = this.isCanBeManagedByUser(userCode, deploymentPlan)
                    && this.usersService.hasPermission(userCode, permission, deploymentPlan.getEnvironment(), deploymentPlan.getReleaseVersion().getRelease().getProduct().getId());
        }

        return canBeManagedByUser;
    }

    /**
     * Check just PRE environment if the user can be managed
     * In INT the user can always manage and interact with the environment
     * In PRO the user can make click and depending status of the release, they will do the action or will generate a to do task
     *
     * @param userCode       the user code
     * @param deploymentPlan the deployment plan
     * @return true if the user can be managed by user
     */
    private boolean isCanBeManagedByUser(final String userCode, final DeploymentPlan deploymentPlan)
    {
        boolean canBeManagedByUser;

        // Just check the PRE environment, due to in INT and PRO environment the users always can interact with the actions
        if (Environment.PRE.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            canBeManagedByUser = this.manageValidationUtils.checkIfPlanCanBeManagedByUser(userCode, deploymentPlan);
        }
        else
        {
            canBeManagedByUser = true;
        }

        return canBeManagedByUser;
    }

    private boolean isDeploymentPlanInteractive(final DeploymentPlan deploymentPlan)
    {
        return deploymentPlan.getAction() == DeploymentAction.READY || deploymentPlan.getAction() == DeploymentAction.ERROR;
    }

    private boolean isDeploymentSubsystemInteractive(final DeploymentSubsystem deploymentSubsystem)
    {
        return deploymentSubsystem.getAction() == DeploymentAction.READY || deploymentSubsystem.getAction() == DeploymentAction.ERROR;
    }

    private boolean isDeploymentServiceInteractive(final DeploymentService deploymentService)
    {
        return deploymentService.getAction() == DeploymentAction.READY || deploymentService.getAction() == DeploymentAction.ERROR;
    }

    private boolean isDeploymentInstanceInteractive(final DeploymentInstance deploymentInstance)
    {
        return deploymentInstance.getAction() == DeploymentAction.READY || deploymentInstance.getAction() == DeploymentAction.ERROR;
    }

    private void validateDateInDeploymentsCalendar(final String deploymentDateTime, final String uuaa, final Platform platform)
    {
        LOG.debug("[DeploymentsApiService] -> [validateDateInDeploymentsCalendar]: Checking deployments calendar to validate the selected deployment date [{}] for UUAA [{}]", deploymentDateTime, uuaa);

        if (Strings.isNullOrEmpty(deploymentDateTime))
        {
            throw new NovaException(DeploymentError.getInvalidDateFormatError(), "[DeploymentsApiService] -> [validateDateInDeploymentsCalendar]: Error parsing date time provided: [" + deploymentDateTime + "] due to is null or empty");
        }

        Date deploymentDate;
        try
        {
            // same parse in DeploymentNovaServiceImpl.updateNova
            deploymentDate = DateUtils.parseDate(deploymentDateTime.replaceAll("Z$", "+0000"), DeploymentConstants.DATE_FORMAT_PATTERNS);
        }
        catch (ParseException e)
        {
            throw new NovaException(DeploymentError.getInvalidDateFormatError(), "[DeploymentsApiService] -> [validateDateInDeploymentsCalendar]: Error parsing date time [" + deploymentDateTime + "]: [" + e.getMessage() + "]");
        }

        if (this.schedulerManagerClient.isDisabledDateForDeploy(deploymentDate, uuaa, platform))
        {
            LOG.warn("[DeploymentsApiService] -> [validateDateInDeploymentsCalendar]: selected deployment date [{}] is disabled", deploymentDateTime);
            throw new NovaException(DeploymentError.getSelectedDateForDeploymentIsDisabledError());
        }
        else
        {
            LOG.debug("[DeploymentsApiService] -> [validateDateInDeploymentsCalendar]: selected deployment date [{}] is enabled", deploymentDateTime);
        }
    }

    /**
     * Check if cluster orchestration is healthy
     * For NOVA Platform, we have a way to check if the CPD o CPD list (in PRO) is any healthy
     * In ETHER Platform case, we do not know this information, so always return true.
     *
     * @param deploymentPlan the deployment plan to check
     * @return true if some CPD in NOVA platform is healthy or is ETHER platform
     * false any other case
     */
    private boolean checkClusterOrchestrationHealthy(final DeploymentPlan deploymentPlan)
    {
        boolean isClusterAvailable;

        // Depending on the infrastructure, check if cluster orchestration is healthy
        if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
        {
            //TODO@jorgeSanchez+Fernando: revisar si hay alguna forma de saber el estado del cluster/orquestador de ETHER
            // Currently, we do not have any way to know if ETHER platform is available or not. So, always return true.
            log.debug("[DeploymentsApiService] -> [checkClusterOrchestrationHealthy]: the deployment plan id: [{}] belongs to ETHER PLATFORM. It Can not be checked. Continue", deploymentPlan.getId());
            isClusterAvailable = true;
        }
        else
        {
            // NOVA Platform: Obtain CPD list where the deployment plan is deployed or will be deployed.
            List<CPD> cpdList = this.cpdRepository.getByEnvironment(deploymentPlan.getEnvironment());
            isClusterAvailable = cpdList.stream().anyMatch(CPD::isActive);
            log.debug("[DeploymentsApiService] -> [checkClusterOrchestrationHealthy]: the CPD list: [{}] is healthy: [{}] for deployment plan id: [{}]", cpdList, isClusterAvailable, deploymentPlan.getId());
        }

        log.debug("[DeploymentsApiService] -> [checkClusterOrchestrationHealthy]: cluster healthy: [{}] for deployment plan id: [{}]", isClusterAvailable, deploymentPlan.getId());
        return isClusterAvailable;
    }

    /**
     * Get library requirements with configured namespaces
     *
     * @param deploymentPlanLibraryRequirementsList
     * @param namespaceRequirementFulfilledDTOList
     * @param requirementsDeploymentMap
     */
    private void getLibraryRequirementsWithNamespaces(List<DeploymentPlanLibraryRequirementsDTO> deploymentPlanLibraryRequirementsList, List<RequirementNamespace> namespaceRequirementFulfilledDTOList, Map<Integer, List<LMLibraryRequirementsFulfilledDTO>> requirementsDeploymentMap)
    {
        // Group library Namespace requirements by product and environment
        Map<Pair<Product, Environment>, List<RequirementNamespace>> requirementsByProductEnvironment = namespaceRequirementFulfilledDTOList.stream()
                .collect(Collectors.groupingBy(requirement -> new ImmutablePair<>(requirement.getProduct(), requirement.getEnvironment())));

        // Build list with all requirements for all deployment plans
        List<LMLibraryRequirementsFulfilledDTO> requirementsFulfilledDTOList = new ArrayList<>();
        requirementsDeploymentMap.forEach((deploymentPlan, requirements) -> {
            requirementsFulfilledDTOList.addAll(requirements);
        });

        // Find ether requirements by product and environment
        buildNamespaceRequirementsFulfilled(requirementsFulfilledDTOList, requirementsByProductEnvironment);

        // Put the list of requirements in their respective deployment plan
        deploymentPlanLibraryRequirementsList.forEach(deploymentPlanLibraryRequirements -> {
            List<LMLibraryRequirementsFulfilledDTO> LMLibraryRequirementsFulfilledList = requirementsDeploymentMap.get(deploymentPlanLibraryRequirements.getDeploymentPlanId());
            deploymentPlanLibraryRequirements.setDeploymentRequirementsFulfilled(
                    LMLibraryRequirementsFulfilledList.toArray(new LMLibraryRequirementsFulfilledDTO[0])
            );

            // Check all the requirements
            deploymentPlanLibraryRequirements.setFulfilled(
                    LMLibraryRequirementsFulfilledList.stream().allMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        });
    }
}
