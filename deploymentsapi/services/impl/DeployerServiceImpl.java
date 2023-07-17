package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USPermissionDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.common.entities.AbstractEntity;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.GBType;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductType;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerConfigurator;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentGcspService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentNovaService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanReplacerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IEtherService;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.REQUIREMENT_NAME.NAMESPACE;

/**
 * Deployer service
 *
 * @author XE56809
 */
@Slf4j
@Service
public class DeployerServiceImpl implements com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeployerService
{
    public static final String LINE_SEPARATOR = "line.separator";
    public static final String FINALLY_ADD_AN_ENTRY_TO_THE_PLAN_HISTORY = "Finally add an entry to the plan history.";
    /**
     * Max risk level threshold
     */
    @Value("${nova.quality.maxRiskLevelThreshold:3}")
    private int maxRiskLevelThreshold;

    /**
     * Enabled budgets
     */
    @Value("${nova.budgets.useInDeployment:true}")
    private Boolean budgetsEnabled;

    /**
     * Whether to validate that the Logical Connectors of a Deployment Plan have their associated documentation.
     */
    @Value("${nova.validate-documents-before-deploying-logical-connectors:false}")
    private boolean validateDocumentsBeforeDeployingLogicalConnectors;

    /**
     * Whether to validate that the NOVA APIs of a Deployment Plan have their associated documentation.
     */
    @Value("${nova.validate-documents-before-deploying-nova-apis:false}")
    private boolean validateDocumentsBeforeDeployingNovaApis;

    /**
     * Plan profiling utils
     */
    private final PlanProfilingUtils planProfilingUtils;

    /**
     * Profiling utils
     */
    private final ProfilingUtils profilingUtils;

    /**
     * Budgets service
     */
    private final IBudgetsService budgetsService;

    /**
     * Compare service
     */
    private final IDeploymentPlanReplacerService deploymentPlanReplacerService;

    /**
     * Deployment GCSP service
     */
    private final IDeploymentGcspService gcspService;

    /**
     * Deployment Nova Planned service
     */
    private final IDeploymentNovaService novaPlannedService;

    /**
     * User service client
     */
    private final IUsersClient usersService;


    /**
     * Tools service client
     */
    private final IToolsClient toolsService;

    /**
     * Library manager service
     */
    private final ILibraryManagerService libraryManagerService;

    /**
     * Ether service
     */
    private final IEtherService etherService;

    /**
     * Deployment manager service
     */
    private final IDeploymentManagerService deploymentManagerService;

    /**
     * Configuration client
     */
    private final ConfigurationmanagerClient configurationmanagerClient;

    /**
     * Todotask client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Release version repository
     */
    private final ISchedulerManagerClient schedulerManagerClient;

    /**
     * Deployment plan repository
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Deployment subsystem repository
     */
    private final DeploymentSubsystemRepository deploymentSubsystemRepository;

    /**
     * Deployment service repository
     */
    private final DeploymentServiceRepository deploymentServiceRepository;

    /**
     * Deployment utils
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * ApiGW service
     */
    private final IApiGatewayService apiGatewayService;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Broker configurator
     */
    private final IBrokerConfigurator brokerConfigurator;

    @Value("${nova.gatewayServices.cesProfilingEnabled:true}")
    private Boolean cesEnabled;

    /*
     * KKPF is NOVA product for CES. We cannot publish its own profiling because profiling publication is done in
     * the same service that is being deployed. It would always fail.
     */
    @Value("${nova.gatewayServices.cesUuaa:KKPF}")
    private String cesUuaa;


    /**
     * Constructor by params
     *
     * @param budgetsService                service of budgets
     * @param deploymentPlanReplacerService service for action to replace
     * @param gcspService                   service for GCSP
     * @param novaPlannedService            service for nova Plans
     * @param usersService                  service for user
     * @param toolsService                  service of tools
     * @param libraryManagerService         service of LibraryManager
     * @param etherService                  service of EtherManager
     * @param deploymentManagerService      client of DeploymentManagerCreate
     * @param configurationmanagerClient    client of configurationManager
     * @param todoTaskServiceClient         client of TodoTask
     * @param schedulerManagerClient        client of SchedulerManager
     * @param deploymentPlanRepository      repository of DeploymentPlan
     * @param deploymentSubsystemRepository repository of deploymentSubsystem
     * @param apiGatewayService             service of Apigw manager
     * @param planProfilingUtils            utils for plan profiling
     * @param profilingUtils                profiling utils
     * @param deploymentUtils               The deployment Utils
     * @param novaActivityEmitter           NovaActivity emitter
     * @param brokerConfigurator            The broker configurator
     */
    @Autowired
    public DeployerServiceImpl(final IBudgetsService budgetsService,
                               final IDeploymentPlanReplacerService deploymentPlanReplacerService,
                               final IDeploymentGcspService gcspService,
                               final IDeploymentNovaService novaPlannedService,
                               final IUsersClient usersService,
                               final IToolsClient toolsService,
                               final ILibraryManagerService libraryManagerService,
                               final IEtherService etherService,
                               final IDeploymentManagerService deploymentManagerService,
                               final ConfigurationmanagerClient configurationmanagerClient,
                               final TodoTaskServiceClient todoTaskServiceClient,
                               final ISchedulerManagerClient schedulerManagerClient,
                               final DeploymentPlanRepository deploymentPlanRepository,
                               final DeploymentSubsystemRepository deploymentSubsystemRepository,
                               final DeploymentServiceRepository deploymentServiceRepository,
                               final IApiGatewayService apiGatewayService,
                               final PlanProfilingUtils planProfilingUtils,
                               final ProfilingUtils profilingUtils,
                               final DeploymentUtils deploymentUtils,
                               final INovaActivityEmitter novaActivityEmitter,
                               final IBrokerConfigurator brokerConfigurator)
    {
        this.budgetsService = budgetsService;
        this.deploymentPlanReplacerService = deploymentPlanReplacerService;
        this.gcspService = gcspService;
        this.novaPlannedService = novaPlannedService;
        this.usersService = usersService;
        this.toolsService = toolsService;
        this.libraryManagerService = libraryManagerService;
        this.etherService = etherService;
        this.deploymentManagerService = deploymentManagerService;
        this.configurationmanagerClient = configurationmanagerClient;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.schedulerManagerClient = schedulerManagerClient;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.deploymentSubsystemRepository = deploymentSubsystemRepository;
        this.deploymentServiceRepository = deploymentServiceRepository;
        this.apiGatewayService = apiGatewayService;
        this.planProfilingUtils = planProfilingUtils;
        this.profilingUtils = profilingUtils;
        this.deploymentUtils = deploymentUtils;
        this.novaActivityEmitter = novaActivityEmitter;
        this.brokerConfigurator = brokerConfigurator;
    }

    //////////////////////// IMPLEMENTATIONS ////////////////////////////////////////////////

    @Override
    public TodoTaskResponseDTO deployOnEnvironment(final String ivUser, final Integer deploymentPlanId, final Boolean force) throws NovaException
    {
        // Find deployment Plan.
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentPlanId).orElseThrow(() -> new NovaException(DeploymentError.getNoSuchDeploymentError(), "Deployment deploymentPlan with id [" + deploymentPlanId + "] does not exist"));
        log.info("[DeployerServiceImpl] -> [deployOnEnvironment]: starting deployment of deploymentPlan with id: [{}] - environment: [{}] - ces enable status: [{}], is force?: [{}]. IvUser: [{}]",
                deploymentPlanId, deploymentPlan.getEnvironment(), this.cesEnabled, force, ivUser);

        // Check deploymentPlan validations - will throw exception if failed.
        this.checkPlanStatus(deploymentPlan);

        //check if services of deploymentPlan are using libraries and this libraries are ready in environment
        this.libraryManagerService.checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());

        // Check if services of deploymentPlan are using Brokers and configure theirs brokers if they are not ready in environment, create channels
        this.brokerConfigurator.configureBrokersForDeploymentPlan(deploymentPlan);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Behaviour depends on environment.
        switch (Environment.valueOf(deploymentPlan.getEnvironment()))
        {
            // INT: direct deployment.
            case INT:
                todoTaskResponseDTO = this.deployOnInt(ivUser, deploymentPlan, force);
                break;

            // PRE: must check if autodeploy, could create task.
            case PRE:
                todoTaskResponseDTO = this.deployOnPre(ivUser, deploymentPlan, force);
                break;
            case PRO:
                //CIBNOVAP-1489: The ProfileOfficeTask won't be create as it is not required
/*
                if (this.cesEnabled && this.profilingUtils.isPlanExposingApis(deploymentPlan) && this.planProfilingUtils.checkProProfileNeedsApproval(deploymentPlan))
                {
                    todoTaskResponseDTO = this.createProfileOfficeTask(deploymentPlan);
                    log.info("[DeployerServiceImpl] -> [deployOnEnvironment]: the deploy plan with id: [{}] must be checked by PROFILING TEAM before deploying on PRO. A To do task: [{}] has been created.", deploymentPlanId, todoTaskResponseDTO);
                }
                else
                {
*/
                todoTaskResponseDTO = this.deployOnPro(ivUser, deploymentPlan, force);
//                }
                break;

            default:
                log.error("Environment not found {}", deploymentPlan.getEnvironment());
                throw new NovaException(DeploymentError.getEnvironmentNotFoundError(), deploymentPlan.getEnvironment());
        }

        log.info("[DeployerServiceImpl] -> [deployOnEnvironment]: finished deployment of deploymentPlan with id: [{}] - environment: [{}] - ces enable status: [{}], is force?: [{}]. IvUser: [{}]. Response generated: [{}]",
                deploymentPlanId, deploymentPlan.getEnvironment(), this.cesEnabled, force, ivUser, todoTaskResponseDTO);
        return todoTaskResponseDTO;
    }

    @Override
    public TodoTaskResponseDTO deployOnInt(final String userCode, final DeploymentPlan deploymentPlan, final Boolean force)
    {
        log.debug("[DeployerServiceImpl] -> [deployOnInt]: Trying to deploy deploymentPlan [{}] in [{}]", deploymentPlan.getId(), deploymentPlan.getEnvironment());
        // Check if product has the NOVA services canon.
        Product product = deploymentPlan.getReleaseVersion().getRelease().getProduct();
        if (!ProductType.LIBRARY.getType().equals(product.getType()) && !budgetsService.checkProductServices(product.getId(), GBType.SERVICIOS_NOVA))
        {
            throw new NovaException(DeploymentError.getProductWithoutNovaServicesCanonError());
        }
        // Execute the deploymentPlan.
        this.deploy(userCode, deploymentPlan, force);

        return this.deploymentUtils.builderTodoTaskResponseDTO(false);
    }

    @Override
    public TodoTaskResponseDTO deployOnPre(final String userCode, final DeploymentPlan deploymentPlan, final Boolean force)
    {
        log.debug("[DeployerServiceImpl] -> [deployOnPre]: Trying to deploy deploymentPlan [{}] in [{}]", deploymentPlan.getId(), deploymentPlan.getEnvironment());

        // A deploymentPlan cannot be deployed on PRE if has no SQA services GB.
        Product product = deploymentPlan.getReleaseVersion().getRelease().getProduct();

        if (!ProductType.LIBRARY.getType().equals(product.getType()) && !budgetsService.checkProductServices(product.getId(), GBType.SERVICIOS_SQA))
        {
            throw new NovaException(DeploymentError.getProductWithoutSQACanonError());
        }

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        // Check permissions
        USPermissionDTO permission = new USPermissionDTO();
        permission.setEnvironment(deploymentPlan.getEnvironment());
        permission.setPermissionName(DeploymentConstants.MANAGE_DEPLOY);
        permission.setUserCode(userCode);

        // Case 1. SQA and PAdmin who have MANAGE_DEPLOY permissions can always able to deploy in PRE
        if (this.usersService.hasPermission(permission))
        {
            log.debug("[DeployerServiceImpl] -> [deployOnPre]: User [{}] can deploy deploymentPlan [{}] in PRE without generating todotasks.", userCode, deploymentPlan.getId());
            this.deploy(userCode, deploymentPlan, force);
        }
        else
        {
            // Check if any other to do task is created and if not, create common extra info message in case of creating a to do task
            String extraInfo = this.checkAndBuildTaskExtraInfoMessage(deploymentPlan);

            // Case 2. User is not SQA or P.Admin and AutoDeploy enable
            if (deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPre())
            {
                // Case 2.A: Autodeploy + Quality
                if (deploymentPlan.getReleaseVersion().getQualityValidation())
                {
                    log.debug("[DeployerServiceImpl] -> [deployOnPre]: deployment plan [{}] is able to auto deploy in PRE and passes quality validation. Proceeding to execute deployment.", deploymentPlan);
                    this.deploy(userCode, deploymentPlan, force);
                }
                // Case 2.B: Autodeploy + NO Quality
                else
                {
                    log.debug("[DeployerServiceImpl] -> [deployOnPre]: deployment [{}] is able to auto deploy in PRE but doesn't pass quality validation. Creating task.", deploymentPlan);
                    todoTaskResponseDTO = this.createDeploymentInPreTask(deploymentPlan, extraInfo, RoleType.PRODUCT_OWNER);

                    // Emit activity
                    this.emitTodoTaskActivity(deploymentPlan, todoTaskResponseDTO);
                }
            }
            // Case 3. User is not SQA or P.Admin and AutoDeploy disabled
            else
            {
                // Case 3.A: In this case, the quality report does matter, we will always create a TO DO Task for SQA Admin
                log.debug("[DeployerServiceImpl] -> [deployOnPre]: deployment Plan [{}] is NOt auto deploy. Creating task for SQA Admin", deploymentPlan);
                todoTaskResponseDTO = this.createDeploymentInPreTask(deploymentPlan, extraInfo, RoleType.SQA_ADMIN);

                // Emit activity
                this.emitTodoTaskActivity(deploymentPlan, todoTaskResponseDTO);
            }
        }

        return todoTaskResponseDTO;
    }

    @Override
    @Transactional
    public TodoTaskResponseDTO deployOnPro(final String userCode, final DeploymentPlan deploymentPlan, final Boolean force)
    {
        log.info("[DeployerServiceImpl] -> [deployOnPro]: the deploy plan with id: [{}] | env: [{}] | deployment type: [{}] - user code: [{}] and force status: [{}] has been processed for deploying on PRO.",
                deploymentPlan.getId(), deploymentPlan.getEnvironment(), deploymentPlan.getDeploymentTypeInPro(), userCode, force);

        // initially task is not generated
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.setGenerated(false);

        this.validateResourcesDocumentation(deploymentPlan);

        // 1. Fist case: user is platform admin, in none case will generate any to do task and will always try to deploy
        if (this.usersService.isPlatformAdmin(userCode))
        {
            log.debug("[DeployerServiceImpl] -> [deployOnPro]: trying to deploy deploymentPlan id: [{}] - of type: [{}] due to has been done by platform admin user code: [{}]", deploymentPlan.getId(), deploymentPlan.getDeploymentTypeInPro(), userCode);
            this.deploy(userCode, deploymentPlan, force);
            log.debug("[DeployerServiceImpl] -> [deployOnPro]: called to deploy deploymentPlan id: [{}] - of type: [{}] due to has been done by platform admin user code: [{}]", deploymentPlan.getId(), deploymentPlan.getDeploymentTypeInPro(), userCode);
        }
        // Second case: autodeploy in PRO is enabled
        else if (deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPro())
        {
            String todoTaskExtraInfo = this.checkAndBuildTaskExtraInfoMessage(deploymentPlan);

            // 2. Autodeploy + (Quality or NO Quality, does not matter)
            // 2.1: NOVA Planned - generate to do task of type NOVA PLANNING to product owner
            if (DeploymentType.NOVA_PLANNED == deploymentPlan.getDeploymentTypeInPro())
            {
                // Validate NOVA PLANNED
                this.novaPlannedService.validateNovaPlannedForDeploy(deploymentPlan);

                // Adding script to the description task
                todoTaskExtraInfo += this.novaPlannedService.getDeploymentActions(deploymentPlan) + java.lang.System.getProperty(LINE_SEPARATOR);

                // Create TodoTask in pro
                todoTaskResponseDTO = this.deploymentUtils.builderTodoTaskResponseDTO(
                        true,
                        RoleType.PRODUCT_OWNER,
                        this.todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, ToDoTaskType.NOVA_PLANNING.name(), RoleType.PRODUCT_OWNER.name(),
                                todoTaskExtraInfo, deploymentPlan.getReleaseVersion().getRelease().getProduct()),
                        ToDoTaskType.NOVA_PLANNING);
            }
            // 2.2: Planned (ControlM) - standard flow - generate to do task to Project / service of type SCHEDULED
            else if (DeploymentType.PLANNED == deploymentPlan.getDeploymentTypeInPro())
            {
                // Validate GCSP
                this.gcspService.validateGcspForDeploy(deploymentPlan);

                // Set the deployment deploymentPlan status to scheduled and update the deployment plan
                deploymentPlan.setStatus(DeploymentStatus.SCHEDULED);
                this.deploymentPlanRepository.save(deploymentPlan);

                // Adding script to the description task
                todoTaskExtraInfo += this.gcspService.getDeploymentScript(deploymentPlan) + java.lang.System.getProperty(LINE_SEPARATOR);

                // Create TodoTask in pro
                todoTaskResponseDTO = this.deploymentUtils.builderTodoTaskResponseDTO(
                        true,
                        RoleType.SERVICE_SUPPORT,
                        this.todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, ToDoTaskType.SCHEDULE_PLANNING.name(), RoleType.SERVICE_SUPPORT.name(),
                                todoTaskExtraInfo, deploymentPlan.getReleaseVersion().getRelease().getProduct()),
                        ToDoTaskType.SCHEDULE_PLANNING);
            }
            // 2.3: On Demand. Generate TO DO TASK assigned to Product Owner
            else
            {
                todoTaskResponseDTO = this.deploymentUtils.builderTodoTaskResponseDTO(
                        true,
                        RoleType.PRODUCT_OWNER,
                        this.todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, ToDoTaskType.DEPLOY_PRO.name(), RoleType.PRODUCT_OWNER.name(),
                                this.checkAndBuildTaskExtraInfoMessage(deploymentPlan), deploymentPlan.getReleaseVersion().getRelease().getProduct()),
                        ToDoTaskType.DEPLOY_PRO);
            }

            if (todoTaskResponseDTO.getGenerated())
            {
                // Emit activity
                this.emitTodoTaskActivity(deploymentPlan, todoTaskResponseDTO);
            }

        }
        // Third case: NO autodeploy: in this case, depending on each deployment type (Quality does not matters). TO DO Task will always be generated
        else
        {
            String todoTaskExtraInfo = this.checkAndBuildTaskExtraInfoMessage(deploymentPlan);

            // 3.A: Control M (PLANNED). Generate SCHEDULE planning to do task, assigned to service support
            if (DeploymentType.PLANNED == deploymentPlan.getDeploymentTypeInPro())
            {
                // Validate GCSP
                this.gcspService.validateGcspForDeploy(deploymentPlan);

                // Set the deployment deploymentPlan status to scheduled and update the deployment plan
                deploymentPlan.setStatus(DeploymentStatus.SCHEDULED);
                this.deploymentPlanRepository.save(deploymentPlan);

                // Adding script to the description task
                todoTaskExtraInfo += this.gcspService.getDeploymentScript(deploymentPlan) + java.lang.System.getProperty(LINE_SEPARATOR);

                // Create TodoTask in pro
                todoTaskResponseDTO = this.deploymentUtils.builderTodoTaskResponseDTO(
                        true,
                        RoleType.SERVICE_SUPPORT,
                        this.todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, ToDoTaskType.SCHEDULE_PLANNING.name(), RoleType.SERVICE_SUPPORT.name(),
                                todoTaskExtraInfo, deploymentPlan.getReleaseVersion().getRelease().getProduct()),
                        ToDoTaskType.SCHEDULE_PLANNING);
            }
            // 3.B: NOVA Planned. Generate NOVA Planning TO DO task assigned to Platform Admin
            else if (DeploymentType.NOVA_PLANNED == deploymentPlan.getDeploymentTypeInPro())
            {
                // Validate NOVA PLANNED
                this.novaPlannedService.validateNovaPlannedForDeploy(deploymentPlan);

                // Adding script to the description task
                todoTaskExtraInfo += this.novaPlannedService.getDeploymentActions(deploymentPlan) + java.lang.System.getProperty(LINE_SEPARATOR);

                // Create TodoTask in pro
                todoTaskResponseDTO = this.deploymentUtils.builderTodoTaskResponseDTO(
                        true,
                        RoleType.PLATFORM_ADMIN,
                        this.todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, ToDoTaskType.NOVA_PLANNING.name(), RoleType.PLATFORM_ADMIN.name(),
                                todoTaskExtraInfo, deploymentPlan.getReleaseVersion().getRelease().getProduct()),
                        ToDoTaskType.NOVA_PLANNING);
            }
            // 3.C: On Demand. Generate DEPLOY PRO TO DO task
            else
            {
                // 3.C.1: If the plan is deployed in NOVA infrastructure the task is assigned to Service Support
                RoleType assignedRole = RoleType.SERVICE_SUPPORT;

                // 3.C.1: If the plan is deployed in ETHER infrastructure the task is assigned to Product Owner
                if (PlatformUtils.isPlanDeployedInEther(deploymentPlan))
                {
                    assignedRole = RoleType.PRODUCT_OWNER;
                }

                todoTaskResponseDTO = this.deploymentUtils.builderTodoTaskResponseDTO(
                        true,
                        assignedRole,
                        this.todoTaskServiceClient.createDeploymentTask(deploymentPlan.getId(), null, ToDoTaskType.DEPLOY_PRO.name(), RoleType.SERVICE_SUPPORT.name(),
                                this.checkAndBuildTaskExtraInfoMessage(deploymentPlan), deploymentPlan.getReleaseVersion().getRelease().getProduct()),
                        ToDoTaskType.DEPLOY_PRO);

            }

            // Emit activity
            this.emitTodoTaskActivity(deploymentPlan, todoTaskResponseDTO);
        }

        log.info("[DeployerServiceImpl] -> [deployOnPro]: the deploy plan with id: [{}] | env: [{}] | deployment type: [{}] - user code: [{}] and force status: [{}] has been processed for deploying on PRO." +
                "Depending on use case, will be generated a response: [{}].", deploymentPlan.getId(), deploymentPlan.getEnvironment(), deploymentPlan.getDeploymentTypeInPro(), userCode, force, todoTaskResponseDTO);
        return todoTaskResponseDTO;
    }

    @Override
    public void deploy(final String userCode, DeploymentPlan deploymentPlan, final Boolean force)
    {
        log.debug("Executing deployment of plan {} on {}", deploymentPlan.getId(), deploymentPlan.getEnvironment());

        // Check costs and property value for budgets in case of NOVA products
        if (!ProductType.LIBRARY.getType().equals(deploymentPlan.getReleaseVersion().getRelease().getProduct().getType()))
        {
            this.checkBudgets(deploymentPlan);
        }

        // Check that the destiny is Ether and the namespace is well configured
        this.checkEtherConfigurationStatus(deploymentPlan);

        // Check available slots
        DeploymentPlan deployedPlan = this.deploymentPlanRepository.findFirstByReleaseVersionReleaseIdAndEnvironmentAndStatus(
                deploymentPlan.getReleaseVersion().getRelease().getId(), deploymentPlan.getEnvironment(), DeploymentStatus.DEPLOYED);
        log.debug("deploy -> deployedPlan [{}]", deployedPlan);

        boolean hasChanged;


        if (deployedPlan == null)
        {
            // Store configuration - in case of error, will throw an exception and deployment will rollback.
            this.configurationmanagerClient.saveCurrentConfigurationRevision(deploymentPlan.getId());

            // Checking if the product has enough free slots. By default, all products have 2, but this parameters is configured by product via data base
            // Entity: product - column: release_slots - scheme: coreservice
            this.checkSlots(deploymentPlan);

            // Registering APIs in API Gateway Service.
            this.apiGatewayService.createPublication(deploymentPlan);

            //Creates docker Keys for current plan services
            this.apiGatewayService.generateDockerKey(deploymentPlan);

            if (this.cesEnabled && !deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa().equalsIgnoreCase(this.cesUuaa) && this.profilingUtils.isPlanExposingApis(deploymentPlan))
            {
                this.planProfilingUtils.checkPlanProfileChange(deploymentPlan);
            }

            // Save and flush the deployment plan to ensure saving the docker key
            deploymentPlan = this.deploymentPlanRepository.saveAndFlush(deploymentPlan);

            // Deploy
            hasChanged = this.deploymentManagerService.deployPlan(deploymentPlan);

        }
        else
        {
            log.debug(
                    "There is a deployed plan of the same release {}, trying to replace it",
                    deploymentPlan.getReleaseVersion().getRelease().getName());

            if (force)
            {
                // Check if the plan that is being replaced is ready to be replaced
                // If the action of the plan is different from READY and ERROR, it means that the plan cannot be replaced
                if (deployedPlan.getAction() != DeploymentAction.READY && deployedPlan.getAction() != DeploymentAction.ERROR)
                {
                    throw new NovaException(DeploymentError.getReplacePlanConflictDeployedStatusError(deployedPlan.getId()));
                }

                // Plan being deployed is the same as plan replaced
                if (deploymentPlan.getId().equals(deployedPlan.getId()))
                {
                    throw new NovaException(DeploymentError.getReplacePlanConflictSamePlanError(deploymentPlan.getId()));
                }

                // Plan being must be in DEFINITION (INT, PRE or PRO environment) status or in SCHEDULED status (this status could be from PRO environment)
                if (deploymentPlan.getStatus() == DeploymentStatus.DEFINITION || deploymentPlan.getStatus() == DeploymentStatus.SCHEDULED)
                {
                    // Remove properties
                    this.configurationmanagerClient.deleteCurrentConfigurationRevision(deployedPlan);

                    // Reject all pending to do task
                    this.deploymentUtils.rejectPlanPendingTask(deployedPlan);

                    // Store configuration - in case of error, will throw an exception and deployment will rollback.
                    this.configurationmanagerClient.saveCurrentConfigurationRevision(deploymentPlan.getId());

                    // Unschedule every batch scheduler service of the plan that is being replaced
                    deployedPlan.getReleaseVersion().getSubsystems().stream() // deployed plan subsystems
                            .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream()) // deployed plan services
                            .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType())) // batch scheduler services
                            .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.updateDeploymentBatchSchedule(
                                    releaseVersionService.getId(), deployedPlan.getId(), DeploymentBatchScheduleStatus.UNDEPLOYED)); // unschedule it
                }
                else
                {
                    throw new NovaException(DeploymentError.getReplacePlanConflictDeployingStatusError(deploymentPlan.getId(), deployedPlan.getEnvironment(), deploymentPlan.getStatus().name()));
                }
            }

            hasChanged = this.replace(deploymentPlan, force, deployedPlan);
        }

        // Si el plan es de servicios NOVA
        if (hasChanged)
        {
            // Deploy batch schedule
            this.deployBatchScheduleServices(deploymentPlan);

            // Finally add an entry to the plan history.
            log.debug(FINALLY_ADD_AN_ENTRY_TO_THE_PLAN_HISTORY);
            this.deploymentUtils.addHistoryEntry(ChangeType.DEPLOY_PLAN, deploymentPlan, userCode, "Se ha iniciado el despliegue del plan en el entorno");

            // Emit activity
            this.emitDeployActivity(deploymentPlan);
        }
    }

    @Override
    public void activateNovaPlannedDeployment(final String userCode, DeploymentPlan deploymentPlan)
    {
        log.debug("Executing scheduled planned nova deployment of deploymentPlan {} on {}", deploymentPlan.getId(), deploymentPlan.getEnvironment());

        if (deploymentPlan.getNova() != null && deploymentPlan.getNova().getDeploymentDateTime() != null && deploymentPlan.getNova().getPriorityLevel() != null)
        {
            //Update status to SCHEDULED
            deploymentPlan.setStatus(DeploymentStatus.SCHEDULED);
            this.deploymentPlanRepository.saveAndFlush(deploymentPlan);

            // Call scheduler manager client
            if (!this.schedulerManagerClient.scheduleDeployment(deploymentPlan))
            {
                //Update status to SCHEDULED
                deploymentPlan.setStatus(DeploymentStatus.DEFINITION);
                this.deploymentPlanRepository.saveAndFlush(deploymentPlan);

                // throw exception
                throw new NovaException(ServiceRunnerError.getPlannedScheduleError());
            }

            // Finally add an entry to the deploymentPlan history.
            log.debug("Finally add an entry to the deploymentPlan history.");
            this.deploymentUtils.addHistoryEntry(ChangeType.SCHEDULE_DEPLOY_PLAN, deploymentPlan, userCode, "El plan de despliegue, configurado como 'Planificado NOVA' ha sido desplegado automáticamente de manera satisfactoria");
            log.debug("Executed scheduled planned nova deployment of deploymentPlan {} on {}", deploymentPlan.getId(), deploymentPlan.getEnvironment());
        }
        else
        {
            log.error("This deployment deploymentPlan: [{}] on [{}] is not a nova scheduled planned deployment OR the deployment deploymentPlan is not configured. NOVA Planned: [{}]", deploymentPlan.getId(), deploymentPlan.getEnvironment(), deploymentPlan.getNova());
            if (deploymentPlan.getNova().getDeploymentDateTime() == null)
            {
                throw new NovaException(DeploymentError.getInvalidNovaPlannedPlanError(deploymentPlan.getId(), deploymentPlan.getDeploymentTypeInPro().name(), deploymentPlan.getStatus().name(), null));
            }
            else
            {
                throw new NovaException(DeploymentError.getInvalidNovaPlannedPlanError(deploymentPlan.getId(), deploymentPlan.getDeploymentTypeInPro().name(), deploymentPlan.getStatus().name(), deploymentPlan.getNova().getDeploymentDateTime().toString()));
            }
        }
    }

    @Override
    public void updatePlanStatus(DeploymentService service)
    {
        log.debug("updatePlanStatus for service {} ", service.getId());

        DeploymentSubsystem subsystem = service.getDeploymentSubsystem();
        DeploymentPlan plan = subsystem.getDeploymentPlan();

        // Si se ha producido un error en el servicio, se propaga el error hacia arriba.
        if (service.getAction() == DeploymentAction.ERROR)
        {
            subsystem.setAction(DeploymentAction.ERROR);
            plan.setStatus(DeploymentStatus.REJECTED);
            String errorMessage = "The deployment service with id: [" + service.getId() + "] and service name: [" + service.getService().getServiceName() + "]" +
                    "and final name: [" + service.getService().getFinalName() + "] has failed and their action status is ERROR";
            plan.setRejectionMessage(errorMessage);

            log.error("[DeployerServiceImpl] -> [updatePlanStatus]: [{}]. The deployment plan with id: [{}] has been REJECTED", errorMessage, plan.getId());

            deploymentSubsystemRepository.saveAndFlush(subsystem);
            deploymentPlanRepository.saveAndFlush(plan);

        }
        else if (service.getAction() == DeploymentAction.READY)
        {
            boolean subsystemDone = true;
            for (DeploymentService serviceI : subsystem.getDeploymentServices())
            {
                if (serviceI.getAction() != DeploymentAction.READY)
                {
                    subsystemDone = false;
                    break;
                }
            }
            if (subsystemDone)
            {
                subsystem.setAction(DeploymentAction.READY);
                deploymentSubsystemRepository.saveAndFlush(subsystem);
                updatePlanStatus(subsystem);
            }
        }
    }

    @Override
    @Transactional
    public void updatePlanStatus(final DeploymentSubsystem subsystem)
    {
        log.debug("updatePlanStatus for subsystem {} ", subsystem.getId());

        DeploymentPlan plan = subsystem.getDeploymentPlan();
        TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());
        // Si se ha producido un error en el servicio, se propaga el error hacia arriba.
        if (subsystem.getAction() == DeploymentAction.ERROR)
        {
            log.debug("Deployment of Subsystem {} with errors!", subsystemDTO.getSubsystemName());
            plan.setStatus(DeploymentStatus.REJECTED);
            String errorMessage = "The deployment subsystem with id: [" + subsystem.getId() + "] and name: [" + subsystemDTO.getSubsystemName() + "] has failed and their action status is ERROR";
            plan.setRejectionMessage(errorMessage);

            log.error("[DeployerServiceImpl] -> [updatePlanStatus]: [{}]. The deployment plan with id: [{}] has been REJECTED", errorMessage, plan.getId());

            this.deploymentPlanRepository.saveAndFlush(plan);
        }
        else if (subsystem.getAction() == DeploymentAction.READY)
        {
            log.debug("Subsystem {} Ready!", subsystemDTO.getSubsystemName());
            boolean planDone = true;
            for (DeploymentSubsystem subsystemI : plan.getDeploymentSubsystems())
            {
                if (subsystemI.getAction() != DeploymentAction.READY)
                {
                    planDone = false;
                    break;
                }
            }
            if (planDone)
            {
                log.debug("Plan has been deployed. Setting status...");
                plan.setAction(DeploymentAction.READY);
                plan.setStatus(DeploymentStatus.DEPLOYED);
                deploymentPlanRepository.saveAndFlush(plan);

                log.debug(FINALLY_ADD_AN_ENTRY_TO_THE_PLAN_HISTORY);

                this.deploymentUtils.addHistoryEntry(ChangeType.DEPLOY_PLAN, plan, null, "El plan de despliegue ha sido desplegado satisfactoriamente en el entorno");
            }
        }
    }

    @Override
    public void unscheduleDeployment(final String ivUser, DeploymentPlan plan)
    {
        // Call scheduler Manager to unSchedule
        this.schedulerManagerClient.unscheduleDeployment(plan.getId());

        log.debug(FINALLY_ADD_AN_ENTRY_TO_THE_PLAN_HISTORY);
        this.deploymentUtils.addHistoryEntry(ChangeType.UNSCHEDULE_DEPLOY_PLAN, plan, ivUser, "El plan de despliegue, configurado como 'Planificado NOVA' ha sido cancelado. No se desplegará en el entorno");
    }

    @Override
    public void promotePlan(DeploymentPlan plan, DeploymentPlan newPlan)
    {
        log.debug("Promotion of plan {} into plan {}", plan.getId(), newPlan.getId());

        this.deploymentManagerService.promotePlan(plan, newPlan);
    }

    ////////////////////////////////////// PRIVATE METHODS ///////////////////////

    private void emitTodoTaskActivity(final DeploymentPlan deploymentPlan, final TodoTaskResponseDTO todoTaskResponseDTO)
    {
        if (Environment.INT.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            // Emit Deploy Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_DEPLOYED)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", deploymentPlan.getStatus().name())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("deploymentQuality", deploymentPlan.getReleaseVersion().getQualityValidation())
                    .addParam("logDestinationPlatform", deploymentPlan.getSelectedLogging().name())
                    .addParam("deploymentDestinationPlatform", deploymentPlan.getSelectedDeploy().name())
                    .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                    .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                    .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                    .build());
        }
        else if (Environment.PRE.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            // Emit Deploy Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_DEPLOYED)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", deploymentPlan.getStatus().name())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("AutoDeploy", deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPre())
                    .addParam("AutoManage", deploymentPlan.getReleaseVersion().getRelease().isAutomanageInPre())
                    .addParam("deploymentQuality", deploymentPlan.getReleaseVersion().getQualityValidation())
                    .addParam("logDestinationPlatform", deploymentPlan.getSelectedLogging().name())
                    .addParam("deploymentDestinationPlatform", deploymentPlan.getSelectedDeploy().name())
                    .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                    .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                    .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                    .build());
        }
        else
        {
            // Emit Deploy Deployment Plan Activity for PRO
            // Check CPD name
            String cpdName = deploymentPlan.getCpdInPro() == null ? "TC & V" : deploymentPlan.getCpdInPro().getName();

            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.SEND_REQUEST_DEPLOYED)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", deploymentPlan.getStatus().name())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("deploymentTypeInPro", deploymentPlan.getDeploymentTypeInPro().name())
                    .addParam("AutoDeploy", deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPro())
                    .addParam("AutoManage", deploymentPlan.getReleaseVersion().getRelease().isEnabledAutomanageInPro())
                    .addParam("deploymentQuality", deploymentPlan.getReleaseVersion().getQualityValidation())
                    .addParam("logDestinationPlatform", deploymentPlan.getSelectedLogging().name())
                    .addParam("deploymentDestinationPlatform", deploymentPlan.getSelectedDeploy().name())
                    .addParam("CPD", cpdName)
                    .addParam("MultiCPD", deploymentPlan.getMultiCPDInPro())
                    .addParam("todoTaskId", todoTaskResponseDTO.getTodoTaskId())
                    .addParam("todoTaskType", todoTaskResponseDTO.getTodoTaskType())
                    .addParam("TodoTaskAssignedRole", todoTaskResponseDTO.getAssignedRole())
                    .build());
        }
    }

    private void emitDeployActivity(final DeploymentPlan deploymentPlan)
    {
        if (Environment.INT.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            // Emit Deploy Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYED)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", deploymentPlan.getStatus().name())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("deploymentQuality", deploymentPlan.getReleaseVersion().getQualityValidation())
                    .addParam("logDestinationPlatform", deploymentPlan.getSelectedLogging().name())
                    .addParam("deploymentDestinationPlatform", deploymentPlan.getSelectedDeploy().name())
                    .build());
        }
        else if (Environment.PRE.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            // Emit Deploy Deployment Plan Activity
            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYED)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", deploymentPlan.getStatus().name())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("AutoDeploy", deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPre())
                    .addParam("AutoManage", deploymentPlan.getReleaseVersion().getRelease().isAutomanageInPre())
                    .addParam("deploymentQuality", deploymentPlan.getReleaseVersion().getQualityValidation())
                    .addParam("logDestinationPlatform", deploymentPlan.getSelectedLogging().name())
                    .addParam("deploymentDestinationPlatform", deploymentPlan.getSelectedDeploy().name())
                    .build());
        }
        else
        {
            // Emit Deploy Deployment Plan Activity for PRO
            // Check CPD name
            String cpdName = deploymentPlan.getCpdInPro() == null ? "TC & V" : deploymentPlan.getCpdInPro().getName();

            this.novaActivityEmitter.emitNewActivity(new GenericActivity
                    .Builder(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.DEPLOYED)
                    .entityId(deploymentPlan.getId())
                    .environment(deploymentPlan.getEnvironment())
                    .addParam("releaseVersionId", deploymentPlan.getReleaseVersion().getId())
                    .addParam("deploymentStatus", deploymentPlan.getStatus().name())
                    .addParam("releaseVersionName", deploymentPlan.getReleaseVersion().getVersionName())
                    .addParam("releaseName", deploymentPlan.getReleaseVersion().getRelease().getName())
                    .addParam("deploymentTypeInPro", deploymentPlan.getDeploymentTypeInPro().name())
                    .addParam("AutoDeploy", deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPro())
                    .addParam("AutoManage", deploymentPlan.getReleaseVersion().getRelease().isEnabledAutomanageInPro())
                    .addParam("deploymentQuality", deploymentPlan.getReleaseVersion().getQualityValidation())
                    .addParam("logDestinationPlatform", deploymentPlan.getSelectedLogging().name())
                    .addParam("deploymentDestinationPlatform", deploymentPlan.getSelectedDeploy().name())
                    .addParam("CPD", cpdName)
                    .addParam("MultiCPD", deploymentPlan.getMultiCPDInPro())
                    .build());
        }
    }

    /**
     * Create deployment task task for PRE
     *
     * @param deploymentPlan  plan
     * @param taskDescription task description
     * @return task id
     */
    private TodoTaskResponseDTO createDeploymentInPreTask(final DeploymentPlan deploymentPlan, final String taskDescription, final RoleType destinationRol)
    {
        // Create the task.
        Integer taskId = this.todoTaskServiceClient.createDeploymentTask(
                deploymentPlan.getId(),
                null,
                ToDoTaskType.DEPLOY_PRE.toString(),
                destinationRol.name(),
                taskDescription,
                deploymentPlan.getReleaseVersion().getRelease().getProduct());

        return this.deploymentUtils.builderTodoTaskResponseDTO(true, destinationRol, taskId, ToDoTaskType.DEPLOY_PRE);
    }

    private void checkDeploymentPendingTask(final DeploymentPlan deploymentPlan)
    {
        // Check if there is already a task requesting deployment or if there is already a nova planned task requesting deployment.
        boolean hasPendingScheduleTask = false;
        boolean hasPendingNovaPlannedTask = false;
        boolean hasPendingDeployTask = false;
        boolean hasPendingPreTask = false;

        if (Environment.PRO.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            hasPendingScheduleTask = this.deploymentPlanRepository.planHasPendingDeploymentRequestOnEnvTask(deploymentPlan.getId(), ToDoTaskType.SCHEDULE_PLANNING);
            hasPendingNovaPlannedTask = this.deploymentPlanRepository.planHasPendingDeploymentRequestOnEnvTask(deploymentPlan.getId(), ToDoTaskType.NOVA_PLANNING);
            hasPendingDeployTask = this.deploymentPlanRepository.planHasPendingDeploymentRequestOnEnvTask(deploymentPlan.getId(), ToDoTaskType.DEPLOY_PRO);
        }
        else
        {
            hasPendingPreTask = this.deploymentPlanRepository.planHasPendingDeploymentRequestOnEnvTask(deploymentPlan.getId(), ToDoTaskType.DEPLOY_PRE);
        }

        if (hasPendingDeployTask)
        {
            throw new NovaException(DeploymentError.getDeploymentRequestPendingError(deploymentPlan.getId(), ToDoTaskType.DEPLOY_PRO.name(), deploymentPlan.getEnvironment()));
        }

        if (hasPendingScheduleTask)
        {
            throw new NovaException(DeploymentError.getDeploymentRequestPendingError(deploymentPlan.getId(), ToDoTaskType.SCHEDULE_PLANNING.name(), deploymentPlan.getEnvironment()));
        }

        if (hasPendingNovaPlannedTask)
        {
            throw new NovaException(DeploymentError.getDeploymentRequestPendingError(deploymentPlan.getId(), ToDoTaskType.NOVA_PLANNING.name(), deploymentPlan.getEnvironment()));
        }

        if (hasPendingPreTask)
        {
            throw new NovaException(DeploymentError.getDeploymentRequestPendingError(deploymentPlan.getId(), ToDoTaskType.DEPLOY_PRE.name(), deploymentPlan.getEnvironment()));
        }
    }

    /**
     * Check if there is another to do task created first.
     * If not, build extra info message common to all deploy tasks
     *
     * @param deploymentPlan The deployment Plan
     * @return The extra info message
     */
    private String checkAndBuildTaskExtraInfoMessage(final DeploymentPlan deploymentPlan)
    {
        // Check if deployment deploymentPlan has some pending task
        this.checkDeploymentPendingTask(deploymentPlan);

        log.debug("[{}] -> [checkAndBuildTaskExtraInfoMessage]: Building task extra info message for planId [{}]", this.getClass().getSimpleName(), deploymentPlan.getId());
        // Convert boolean result to string for quality
        String releaseVersionQuality = deploymentPlan.getReleaseVersion().getQualityValidation() ? "Cumple calidad" : "No cumple calidad";

        // Convert boolean result to string for autodeploy and automanage
        String autoManage;
        String autoDeploy;
        if (Environment.PRE.getEnvironment().equals(deploymentPlan.getEnvironment()))
        {
            autoManage = deploymentPlan.getReleaseVersion().getRelease().isAutomanageInPre() ? "Activado" : "Desactivado";
            autoDeploy = deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPre() ? "Activado" : "Desactivado";
        }
        else
        {
            autoManage = deploymentPlan.getReleaseVersion().getRelease().isEnabledAutomanageInPro() ? "Activado" : "Desactivado";
            autoDeploy = deploymentPlan.getReleaseVersion().getRelease().isEnabledAutodeployInPro() ? "Activado" : "Desactivado";
        }

        StringBuilder todoTaskExtraInfo = new StringBuilder(System.getProperty(Constants.LINE_SEPARATOR)
                + "La informacion asociada al plan de despliegue es la siguiente"
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [ID del deploymentPlan]: " + deploymentPlan.getId()
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Estado del deploymentPlan]: " + deploymentPlan.getStatus().name()
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Despliegue del plan autogestionado]: " + autoDeploy
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Modo autogestionado (acciones del plan)]: " + autoManage
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Entorno]: " + deploymentPlan.getEnvironment()
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Tipo de despliegue]: " + deploymentPlan.getDeploymentTypeInPro().name()
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Estado Calidad]: " + releaseVersionQuality
                + System.getProperty(Constants.LINE_SEPARATOR)
                + " - [Plataforma de despliegue]: " + deploymentPlan.getSelectedDeploy().name()
                + System.getProperty(Constants.LINE_SEPARATOR));

        if (DeploymentType.NOVA_PLANNED == deploymentPlan.getDeploymentTypeInPro())
        {
            if (deploymentPlan.getNova() != null)
            {
                DeploymentNova nova = deploymentPlan.getNova();

                todoTaskExtraInfo.append(" - [Fecha de despliegue]: ").append(nova.getDeploymentDateTime()).append(System.getProperty(Constants.LINE_SEPARATOR));
                todoTaskExtraInfo.append(" - [Criterio de despliegue]: ").append(nova.getPriorityLevel().getPriority()).append(System.getProperty(Constants.LINE_SEPARATOR));

                if (Strings.isNullOrEmpty(nova.getDeploymentList()) && nova.getPriorityLevel() == DeploymentPriority.PRODUCT)
                {
                    todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue]: todos los servicios del plan de despliegue han sido seleccionados para ser desplegados").append(System.getProperty(Constants.LINE_SEPARATOR));
                }
                else if (Strings.isNullOrEmpty(nova.getDeploymentList()) && nova.getPriorityLevel() == DeploymentPriority.SUBSYSTEM)
                {
                    todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue]: no se ha seleccionado ningún subsistema para este plan de despliegue").append(System.getProperty(Constants.LINE_SEPARATOR));
                }
                else if (Strings.isNullOrEmpty(nova.getDeploymentList()) && nova.getPriorityLevel() == DeploymentPriority.SERVICE)
                {
                    todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue]: no se ha seleccionado ningún servicio para este plan de despliegue").append(System.getProperty(Constants.LINE_SEPARATOR));
                }
                else
                {
                    int[] deploymentList = Arrays.stream(nova.getDeploymentList().split(",")).mapToInt(Integer::parseInt).toArray();

                    if (nova.getPriorityLevel() == DeploymentPriority.SERVICE)
                    {
                        todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue: lista de servicios seleccionados para ser desplegados]: ").append(System.getProperty(Constants.LINE_SEPARATOR));
                    }
                    else
                    {
                        todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue: lista de subsistemas seleccionados para ser desplegados]: ").append(System.getProperty(Constants.LINE_SEPARATOR));
                    }

                    for (int services : deploymentList)
                    {
                        if (nova.getPriorityLevel() == DeploymentPriority.SERVICE)
                        {
                            todoTaskExtraInfo.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " + deploymentServiceRepository.getServiceName(services)).append(System.getProperty(Constants.LINE_SEPARATOR));
                        }
                        else
                        {
                            todoTaskExtraInfo.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " + this.toolsService.getSubsystemById(deploymentSubsystemRepository.getSubsystemId(services)).getSubsystemName()).append(System.getProperty(Constants.LINE_SEPARATOR));
                        }
                    }
                }
            }
        }
        else if (DeploymentType.PLANNED == deploymentPlan.getDeploymentTypeInPro())
        {
            if (deploymentPlan.getGcsp() != null)
            {
                DeploymentGcsp gcsp = deploymentPlan.getGcsp();

                todoTaskExtraInfo.append(" - [Fecha de despliegue]: ").append(gcsp.getExpectedDeploymentDate()).append(System.getProperty(Constants.LINE_SEPARATOR));
                todoTaskExtraInfo.append(" - [Criterio de despliegue]: ").append(gcsp.getPriorityLevel().getPriority()).append(System.getProperty(Constants.LINE_SEPARATOR));

                if (Strings.isNullOrEmpty(gcsp.getDeploymentList()) && gcsp.getPriorityLevel() == DeploymentPriority.PRODUCT)
                {
                    todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue]: todos los servicios del plan de despliegue han sido seleccionados para ser desplegados").append(System.getProperty(Constants.LINE_SEPARATOR));
                }
                else if (Strings.isNullOrEmpty(gcsp.getDeploymentList()) && gcsp.getPriorityLevel() == DeploymentPriority.SUBSYSTEM)
                {
                    todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue]: no se ha seleccionado ningún subsistema para este plan de despliegue").append(System.getProperty(Constants.LINE_SEPARATOR));
                }
                else if (Strings.isNullOrEmpty(gcsp.getDeploymentList()) && gcsp.getPriorityLevel() == DeploymentPriority.SERVICE)
                {
                    todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue]: no se ha seleccionado ningún servicio para este plan de despliegue").append(System.getProperty(Constants.LINE_SEPARATOR));
                }
                else
                {
                    int[] deploymentList = Arrays.stream(gcsp.getDeploymentList().split(",")).mapToInt(Integer::parseInt).toArray();

                    if (gcsp.getPriorityLevel() == DeploymentPriority.SERVICE)
                    {
                        todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue: lista de servicios seleccionados para ser desplegados]: ").append(System.getProperty(Constants.LINE_SEPARATOR));
                    }
                    else
                    {
                        todoTaskExtraInfo.append(" - [Información adicional del plan de despliegue: lista de subsistemas seleccionados para ser desplegados]: ").append(System.getProperty(Constants.LINE_SEPARATOR));
                    }

                    for (int services : deploymentList)
                    {
                        if (gcsp.getPriorityLevel() == DeploymentPriority.SERVICE)
                        {
                            todoTaskExtraInfo.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " + deploymentServiceRepository.getServiceName(services)).append(System.getProperty(Constants.LINE_SEPARATOR));
                        }
                        else
                        {
                            todoTaskExtraInfo.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;- " + this.toolsService.getSubsystemById(deploymentSubsystemRepository.getSubsystemId(services)).getSubsystemName()).append(System.getProperty(Constants.LINE_SEPARATOR));
                        }
                    }
                }
            }
        }

        log.debug("[{}] -> [checkAndBuildTaskExtraInfoMessage]: Built task extra info message for planId [{}], Result:[{}]", this.getClass().getSimpleName(), deploymentPlan.getId(), todoTaskExtraInfo);
        return todoTaskExtraInfo + System.getProperty(Constants.LINE_SEPARATOR);
    }

    /**
     * Create a deployment batch scheudule for the batch schedule services of the plan
     *
     * @param deploymentPlan the deployment plan
     */
    private void deployBatchScheduleServices(final DeploymentPlan deploymentPlan)
    {
        deploymentPlan.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.createDeploymentBatchSchedule(releaseVersionService.getId(), deploymentPlan.getId(),
                        releaseVersionService.getVersionSubsystem().getId(), deploymentPlan.getEnvironment()));
    }

    /**
     * If the platform deploy /logging is Ether, check the namespace is well configured.
     *
     * @param plan plan
     */
    private void checkEtherConfigurationStatus(DeploymentPlan plan)
    {
        Map<String, String> namespacesMap = new HashMap<>();
        // If the namespace for deploy and logging is the same, only do one check for the namespace.
        // If the namespaces for deploy and logging are different, do two checks, one for each namespace.
        if (PlatformUtils.isPlanDeployedInEther(plan) || this.hasEtherLibraryRequirements(plan))
        {
            namespacesMap.put(plan.getEtherNs(), DeploymentConstants.DEPLOY);
        }

        if (PlatformUtils.isPlanLoggingInEther(plan))
        {
            namespacesMap.putIfAbsent(plan.getEtherNs(), DeploymentConstants.LOGGING);
        }

        namespacesMap.forEach((ns, type) -> this.doCheckEtherConfigurationStatus(plan));

    }

    private void doCheckEtherConfigurationStatus(final DeploymentPlan plan)
    {
        log.debug("[{}] -> [doCheckEtherConfigurationStatus]: Checking configuration Ether Status for plan {}", Constants.SERVICE_NAME, plan.getId());

        final boolean readyToDeploy = etherService.isReadyToDeploy(plan.getReleaseVersion().getRelease().getProduct().getId(), plan.getEnvironment());

        if (!readyToDeploy)
        {
            throw new NovaException(DeploymentError.getDestinationPlatformWithStatusKo());
        }
    }

    private boolean hasEtherLibraryRequirements(final DeploymentPlan plan)
    {
        boolean hasEtherRequirements = false;

        // get the release version service ids
        final int[] releaseVersionServiceIds = plan.getReleaseVersion().getAllReleaseVersionServices()
                .filter(service -> !ServiceType.isLibrary(service.getServiceType()))
                .mapToInt(AbstractEntity::getId)
                .toArray();

        if (releaseVersionServiceIds != null && releaseVersionServiceIds.length > 0)
        {
            // get requirements of service
            final LMLibraryRequirementsDTO[] requirementsDTO = this.libraryManagerService.getAllRequirementsOfUsedLibraries(releaseVersionServiceIds);

            if (requirementsDTO != null && requirementsDTO.length > 0)
            {
                // check if there is a NAMESPACE requirement
                hasEtherRequirements = Stream.of(requirementsDTO)
                        .filter(req -> req.getRequirements() != null && req.getRequirements().length > 0)
                        .anyMatch(req -> (Stream.of(req.getRequirements())
                                .anyMatch(req2 -> NAMESPACE.name().equals(req2.getRequirementName())))
                        );
            }
        }

        return hasEtherRequirements;
    }

    /**
     * Replace plan if needed
     *
     * @param force        force
     * @param plan         deployment plan
     * @param deployedPlan deployed plan
     * @throws NovaException if the force flag is false or null
     */
    private boolean replace(DeploymentPlan plan, Boolean force, DeploymentPlan deployedPlan) throws NovaException
    {
        boolean hasChanged = false;

        //Check if plan must be replaced
        if ((force == null) || (!force))
        {
            // If force is false, build a Nova Exception
            this.deploymentPlanReplacerService.buildReplacePlanNovaException(deployedPlan, plan);
        }
        else
        {
            // Prepare for replacing
            hasChanged = this.deploymentManagerService.replacePlan(deployedPlan, plan);
        }
        return hasChanged;
    }

    /**
     * Check if the status of a deployment Plan is correct
     *
     * @throws NovaException if status is not ready
     */
    private void checkPlanStatus(final DeploymentPlan deploymentPlan) throws NovaException
    {
        //Check deployment status
        if (!(deploymentPlan.getStatus() == DeploymentStatus.DEFINITION || deploymentPlan.getStatus() == DeploymentStatus.SCHEDULED))
        {
            throw new NovaException(DeploymentError.getPlanNotReadyError(deploymentPlan.getId(), deploymentPlan.getStatus().name()));
        }
    }

    /**
     * Check slots
     *
     * @param deploymentPlan deployment plan
     */
    private void checkSlots(DeploymentPlan deploymentPlan)
    {
        List<DeploymentPlan> deployedPlans = this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), deploymentPlan.getEnvironment(), DeploymentStatus.DEPLOYED);

        if (deployedPlans.size() >= deploymentPlan.getReleaseVersion().getRelease().getProduct().getReleaseSlots())
        {
            throw new NovaException(DeploymentError.getNoSlotsError(deploymentPlan.getId(), deploymentPlan.getEnvironment(), deploymentPlan.getReleaseVersion().getRelease().getName()));
        }
    }

    /**
     * Check budgets
     *
     * @param plan plan
     */
    private void checkBudgets(DeploymentPlan plan)
    {
        if (Boolean.TRUE.equals(budgetsEnabled))
        {
            this.checkDeployability(plan);
        }
    }

    /**
     * Check deployability
     *
     * @param plan deployment plan
     */
    private void checkDeployability(DeploymentPlan plan)
    {
        if (!budgetsService.checkDeploymentPlanDeployabilityStatus(plan.getId()))
        {
            log.error("There is an error with the deployment plan's budget with id {}", plan.getId());
            throw new NovaException(DeploymentError.getBudgetError(), "Plan is not deployable due to its budget");
        }

    }

    //CIBNOVAP-1489: The ProfileOfficeTask won't be create as it is not required
//    private TodoTaskResponseDTO createProfileOfficeTask(final DeploymentPlan plan)
//    {
//        RoleType assignedRole = RoleType.PROFILING_OFFICE_CIB;
//        ToDoTaskType toDoTaskType = ToDoTaskType.APPROVE_PLAN_PROFILING;
//        Integer taskId = this.todoTaskServiceClient.createDeploymentTask(
//                plan.getId(),
//                null,
//                toDoTaskType.name(),
//                assignedRole.name(),
//                "",
//                plan.getReleaseVersion().getRelease().getProduct());
//
//        return this.deploymentUtils.builderTodoTaskResponseDTO(true, assignedRole, taskId, toDoTaskType);
//    }

    /**
     * Check if all the resources have their documentation.
     *
     * @param deploymentPlan A Deployment Plan.
     * @throws NovaException In case a resources does not have its documentation.
     */
    private void validateResourcesDocumentation(DeploymentPlan deploymentPlan)
    {
        // Check documentation for Logical Connectors and NOVA APIs.
        if (this.validateDocumentsBeforeDeployingLogicalConnectors || this.validateDocumentsBeforeDeployingNovaApis) // Optimization.
        {
            Map<String, List<Integer>> resourcesWithoutDocumentation = new HashMap<>();

            List<Integer> logicalConnectorIdsWithoutDocumentation = new ArrayList<>();
            resourcesWithoutDocumentation.put("LOGICAL_CONNECTOR", logicalConnectorIdsWithoutDocumentation);

            List<Integer> novaApiIdsWithoutDocumentation = new ArrayList<>();
            resourcesWithoutDocumentation.put("NOVA_API", novaApiIdsWithoutDocumentation);

            deploymentPlan.getDeploymentSubsystems().forEach(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().forEach(deploymentService -> {
                if (this.validateDocumentsBeforeDeployingLogicalConnectors)
                {
                    for (LogicalConnector logicalConnector : deploymentService.getLogicalConnectors())
                    {
                        if (logicalConnector.getMsaDocument() == null)
                        {
                            logicalConnectorIdsWithoutDocumentation.add(logicalConnector.getId());
                        }
                    }
                }
                if (this.validateDocumentsBeforeDeployingNovaApis)
                {

                    for (ApiImplementation<?, ?, ?> apiImplementation : deploymentService.getService().getApiImplementations())
                    {
                        Api<?, ?, ?> api = apiImplementation.getApiVersion().getApi();
                        if (api.hasMissingDocumentation())
                        {
                            novaApiIdsWithoutDocumentation.add(api.getId());
                        }
                    }
                }
            }));

            if (resourcesWithoutDocumentation.values().stream().anyMatch(resources -> !resources.isEmpty()))
            {
                throw new NovaException(DeploymentError.getResourcesHaveNoDocumentationError(resourcesWithoutDocumentation));
            }

        }

    }

}
