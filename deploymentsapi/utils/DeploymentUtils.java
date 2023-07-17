package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.*;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.*;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.product.enumerates.LogLevel;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameter;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.enums.ScopeLevel;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.enums.SimpleServiceType;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.common.Constants.JDK_VERSION;
import static java.lang.Math.subtractExact;

/**
 * Utilities for using the NOVA monitoring service.
 */
@Service
@Slf4j
public class DeploymentUtils
{
    /**
     * Callback service
     */
    private final CallbackService callbackService;

    /**
     * Tools service client
     */
    private final IToolsClient toolsClient;

    /**
     * Change repository
     */
    private final DeploymentChangeRepository changeRepository;

    /**
     * To do task repository
     */
    private final ToDoTaskRepository toDoTaskRepository;

    /**
     * Deployment task repository
     */
    private final DeploymentTaskRepository deploymentTaskRepository;

    /**
     * Management action task repository
     */
    private final ManagementActionTaskRepository managementActionTaskRepository;

    /**
     * JDK parameter repository
     */
    private final JdkParameterRepository jdkParameterRepository;

    /**
     * DeploymentLabelRepository
     */
    private final DeploymentLabelRepository deploymentLabelRepository;

    /**
     * validator
     */
    private final IDeploymentsValidator deploymentsValidator;

    /**
     * URL to GCSP portal pointing to a given pass.
     */
    @Value("${nova.gcspUrl:https://cibreports.es.igrupobbva/GCSP/egsp_es_web/?idPase={idPase}}")
    private String gcspUrl;

    /**
     * Constructor
     *
     * @param callbackService                callback
     * @param toolsClient                    toolsService dependency
     * @param changeRepository               the change repository
     * @param deploymentTaskRepository       deployment task repository
     * @param toDoTaskRepository             to do task repository
     * @param managementActionTaskRepository the management action task repository
     * @param deploymentsValidator           deployments validator
     */
    @Autowired
    public DeploymentUtils(final CallbackService callbackService,
                           final IToolsClient toolsClient,
                           final DeploymentChangeRepository changeRepository,
                           final ToDoTaskRepository toDoTaskRepository,
                           final DeploymentTaskRepository deploymentTaskRepository,
                           final ManagementActionTaskRepository managementActionTaskRepository,
                           final JdkParameterRepository jdkParameterRepository,
                           final DeploymentLabelRepository deploymentLabelRepository,
                           final IDeploymentsValidator deploymentsValidator)
    {
        this.callbackService = callbackService;
        this.toolsClient = toolsClient;
        this.changeRepository = changeRepository;
        this.toDoTaskRepository = toDoTaskRepository;
        this.deploymentTaskRepository = deploymentTaskRepository;
        this.managementActionTaskRepository = managementActionTaskRepository;
        this.jdkParameterRepository = jdkParameterRepository;
        this.deploymentLabelRepository = deploymentLabelRepository;
        this.deploymentsValidator = deploymentsValidator;
    }

    ////////////////////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////////////////

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentPlan  deployment
     * @param successEndpoint success
     * @param errorEndpoint   error
     * @return a EtherDeploymentDTO object
     */
    public EtherDeploymentDTO buildEtherDeploymentDTO(final DeploymentPlan deploymentPlan, final String successEndpoint, final String errorEndpoint)
    {
        //Build the callback for deploymentPlan
        String successUrl = CallbackService.CALLBACK_PLAN + deploymentPlan.getId() + successEndpoint;
        String errorUrl = CallbackService.CALLBACK_PLAN + deploymentPlan.getId() + errorEndpoint;

        //Build EtherDeploymentDTO with basic information
        return this.buildEtherDeploymentDTOByUrl(deploymentPlan, successUrl, errorUrl);
    }

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentPlan deployment
     * @return a EtherDeploymentDTO object
     */
    public EtherDeploymentDTO buildEtherDeploymentDTO(final DeploymentPlan deploymentPlan)
    {
        //Build EtherDeploymentDTO with basic information
        return this.buildEtherDeploymentDTOByUrl(deploymentPlan, null);
    }

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentPlan deployment
     * @param successUrl     success callback url
     * @param errorUrl       error callback url
     * @return a EtherDeploymentDTO object
     */
    public EtherDeploymentDTO buildEtherDeploymentDTOByUrl(final DeploymentPlan deploymentPlan, final String successUrl, final String errorUrl)
    {
        final EtherResponseDTO etherResponseDTO = this.callbackService.buildEtherCallback(successUrl, errorUrl);

        return buildEtherDeploymentDTOByUrl(deploymentPlan, etherResponseDTO);
    }

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentPlan   deployment
     * @param etherResponseDTO {@link EtherResponseDTO} containing the success and error callbacks
     * @return a EtherDeploymentDTO object
     */
    public EtherDeploymentDTO buildEtherDeploymentDTOByUrl(final DeploymentPlan deploymentPlan, final EtherResponseDTO etherResponseDTO)
    {
        //Add all services
        List<EtherServiceDTO> etherServiceDTOList = new ArrayList<>();

        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(deploymentSubsystem.getSubsystem().getSubsystemId());

            if (!subsystemDTO.getSubsystemType().equals(SubsystemType.EPHOENIX.getType()))
            {
                for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
                {
                    if (!ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(deploymentService.getService().getServiceType()))
                    {
                        etherServiceDTOList.add(this.buildEtherServiceDTO(deploymentService));
                    }
                }
            }
        }

        final EtherDeploymentScopeDTO scope = new EtherDeploymentScopeDTO();
        scope.setLevel(ScopeLevel.DEPLOYMENT_PLAN.name());
        scope.setIdentifier(deploymentPlan.getId());

        //Build EtherDeploymentDTO with basic information
        return this.buildBasicEtherDeploymentDTO(deploymentPlan, deploymentPlan.getReleaseVersion().getRelease(), etherServiceDTOList, etherResponseDTO, scope);
    }


    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentSubsystem deployment subsystem
     * @param successEndpoint     success
     * @param errorEndpoint       error
     * @return a EtherDeploymentDTO object
     */
    public EtherDeploymentDTO buildEtherDeploymentDTO(final DeploymentSubsystem deploymentSubsystem, final String successEndpoint, final String errorEndpoint)
    {
        DeploymentPlan deploymentPlan = deploymentSubsystem.getDeploymentPlan();

        //Add all services
        List<EtherServiceDTO> etherServiceDTOList = new ArrayList<>();
        for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
        {
            etherServiceDTOList.add(this.buildEtherServiceDTO(deploymentService));
        }

        //Build the callback for deploymentPlan
        String successUrl = CallbackService.CALLBACK_SUBSYSTEM + deploymentSubsystem.getId() + successEndpoint;
        String errorUrl = CallbackService.CALLBACK_SUBSYSTEM + deploymentSubsystem.getId() + errorEndpoint;

        final EtherDeploymentScopeDTO scope = new EtherDeploymentScopeDTO();
        scope.setLevel(ScopeLevel.SUBSYSTEM.name());
        scope.setIdentifier(deploymentSubsystem.getId());

        return this.buildBasicEtherDeploymentDTO(deploymentPlan, deploymentPlan.getReleaseVersion().getRelease(), etherServiceDTOList, this.callbackService.buildEtherCallback(successUrl, errorUrl), scope);
    }

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentSubsystem deployment subsystem
     * @return a EtherDeploymentDTO object
     */
    public EtherSubsystemDTO buildEtherSubsystemDTO(final DeploymentSubsystem deploymentSubsystem)
    {
        //Add all services
        List<EtherServiceDTO> etherServiceDTOList = new ArrayList<>();
        for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
        {
            etherServiceDTOList.add(this.buildEtherServiceDTO(deploymentService));
        }

        return this.buildBasicEtherSubsystemDTO(deploymentSubsystem, etherServiceDTOList);
    }

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentPlan deployment plan
     * @return a EtherDeploymentDTO object
     */
    public EtherSubsystemDTO[] buildEtherSubsystemDTO(final DeploymentPlan deploymentPlan)
    {
        List<EtherSubsystemDTO> etherSubsystemDTOList = new ArrayList<>();

        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            etherSubsystemDTOList.add(this.buildEtherSubsystemDTO(deploymentSubsystem));
        }

        return etherSubsystemDTOList.toArray(new EtherSubsystemDTO[0]);
    }

    /**
     * Builds an object with all the necessary information for deploying on cloud environment
     *
     * @param deploymentService service
     * @param successEndpoint   success
     * @param errorEndpoint     error
     * @return a EtherDeploymentDTO object
     */
    public EtherDeploymentDTO buildEtherDeploymentDTO(final DeploymentService deploymentService, final String successEndpoint, final String errorEndpoint)
    {
        DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();

        //Add all services
        List<EtherServiceDTO> etherServiceDTOList = new ArrayList<>();
        etherServiceDTOList.add(this.buildEtherServiceDTO(deploymentService));
        log.debug("[DeploymentUtils] -> [buildEtherDeploymentDTO]: ether service DTO list: [{}]", etherServiceDTOList);

        //Build the callback for deploymentPlan
        String successUrl = CallbackService.CALLBACK_SERVICE + deploymentService.getId() + successEndpoint;
        String errorUrl = CallbackService.CALLBACK_SERVICE + deploymentService.getId() + errorEndpoint;

        //Build EtherDeploymentDTO with basic information
        return this.buildBasicEtherDeploymentDTO(deploymentPlan, deploymentPlan.getReleaseVersion().getRelease(), etherServiceDTOList, this.callbackService.buildEtherCallback(successUrl, errorUrl));
    }

    public SubsystemServicesStatusDTO[] buildSubsystemServicesStatus(final EtherSubsystemStatusDTO[] etherSubsystemStatusDTOArray)
    {
        List<SubsystemServicesStatusDTO> subsystemServicesStatusDtoList = new ArrayList<>();

        for (EtherSubsystemStatusDTO etherSubsystemStatusDTO : etherSubsystemStatusDTOArray)
        {
            SubsystemServicesStatusDTO subsystemServicesStatusDto = new SubsystemServicesStatusDTO();

            subsystemServicesStatusDto.setSubsystemId(etherSubsystemStatusDTO.getSubsystemId());
            subsystemServicesStatusDto.setServices(this.buildServiceStatus(etherSubsystemStatusDTO.getServices()));
            subsystemServicesStatusDto.setStatus(this.buildStatusDTO(etherSubsystemStatusDTO));

            subsystemServicesStatusDtoList.add(subsystemServicesStatusDto);
        }

        return subsystemServicesStatusDtoList.toArray(new SubsystemServicesStatusDTO[0]);
    }

    public SubsystemServicesStatusDTO[] buildSubsystemServicesStatus(final DeploySubsystemStatusDTO[] deploySubsystemStatus)
    {
        List<SubsystemServicesStatusDTO> subsystemServicesStatusDtoList = new ArrayList<>();

        for (DeploySubsystemStatusDTO deploySubsystemStatusDTO : deploySubsystemStatus)
        {
            SubsystemServicesStatusDTO subsystemServicesStatusDto = new SubsystemServicesStatusDTO();

            subsystemServicesStatusDto.setSubsystemId(deploySubsystemStatusDTO.getSubsystemId());
            subsystemServicesStatusDto.setServices(this.buildServiceStatus(deploySubsystemStatusDTO.getServices()));
            subsystemServicesStatusDto.setStatus(this.buildStatusDTO(deploySubsystemStatusDTO.getStatus()));

            subsystemServicesStatusDtoList.add(subsystemServicesStatusDto);
        }

        return subsystemServicesStatusDtoList.toArray(new SubsystemServicesStatusDTO[0]);
    }

    /**
     * Build list with ToDoTaskType CHECK_LIBRARY_VARS.
     * @param lmLibraryEnvironmentsDTOList libraries used by plan
     * @return list with TaskRequestDTO object
     */
    public List<TaskRequestDTO> buildTaskRequestDTOLibraryVars(final List<LMLibraryEnvironmentsDTO> lmLibraryEnvironmentsDTOList, boolean canGenerate)
    {
        List<TaskRequestDTO> taskRequestDTOList = new ArrayList<>();

        //All deployment plan has configuration environment variables
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();

        //In case of deployment plan use libraries
        if (lmLibraryEnvironmentsDTOList != null && !lmLibraryEnvironmentsDTOList.isEmpty())
        {
            taskRequestDTO = new TaskRequestDTO();

            taskRequestDTO.setDescription(Constants.LIB_GENERAL_DESCRIPTION);
            taskRequestDTO.setTypeTask(ToDoTaskType.CHECK_LIBRARY_VARS.name());
            taskRequestDTO.setManagement(ManagementType.LIBRARY.name());
            taskRequestDTO.setCanGenerate(canGenerate);

            taskRequestDTOList.add(taskRequestDTO);
            log.debug("[DeploymentUtils] -> [buildTaskRequestDTOLibraryVars]: added CHECK_LIBRARY_VARS to Management Task.");
        }

        return taskRequestDTOList;
    }

    /**
     * Build list with ToDoTaskType CHECK_ENVIRONMENT_VARS.
     * @return list with TaskRequestDTO object
     */
    public List<TaskRequestDTO> buildTaskRequestDTOEnvironmentVars(boolean canGenerate)
    {

        List<TaskRequestDTO> taskRequestDTOList = new ArrayList<>();

        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();

        taskRequestDTO.setDescription(Constants.ENVIRONMENT_DESCRIPTION);
        taskRequestDTO.setTypeTask(ToDoTaskType.CHECK_ENVIRONMENT_VARS.name());
        taskRequestDTO.setManagement(ManagementType.ENVIRONMENT.name());
        taskRequestDTO.setCanGenerate(canGenerate);

        taskRequestDTOList.add(taskRequestDTO);

        log.debug("[DeploymentUtils] -> [buildTaskRequestDTOLibraryVars]: added CHECK_ENVIRONMENT_VARS to Management Task.");

        return taskRequestDTOList;
    }

    /**
     * Reject all pending task for a given deploymentPlan
     *
     * @param deploymentPlan The deploymentPlan to reject tasks
     */
    public void rejectPlanPendingTask(DeploymentPlan deploymentPlan)
    {
        log.debug("[DeploymentUtils] -> [rejectPlanPendingTask]: Rejecting all pending tasks of deploymentPlan: [{}]", deploymentPlan.getId());
        List<Integer> rejectedTaskList = new ArrayList<>();

        // Getting all pending to do task of type deployment task of the product filter by deployment plan id.
        List<DeploymentTask> deploymentTaskList = this.deploymentTaskRepository.planPendingDeploymentTasks(deploymentPlan.getId());

        // For each pending task associated to deployment plan, rejected them
        deploymentTaskList.stream().map(deploymentTask -> this.toDoTaskRepository.findById(deploymentTask.getId())).filter(Optional::isPresent).map(Optional::get).forEach(deploymentTask ->
        {
            // reject the to do task and update task status
            deploymentTask.setStatus(ToDoTaskStatus.REJECTED);
            deploymentTask.setClosingMotive(DeploymentConstants.CLOSE_PENDING_TASK_MESSAGE);
            this.toDoTaskRepository.save(deploymentTask);

            // add the to do task id to the list
            rejectedTaskList.add(deploymentTask.getId());
        });

        // Getting all management to do task associated to the deployment plan
        List<ManagementActionTask> managementActionTasks = new ArrayList<>(this.managementActionTaskRepository.findByRelatedId(deploymentPlan.getId()));

        deploymentPlan.getDeploymentSubsystems().forEach(deploymentSubsystem ->
        {
            managementActionTasks.addAll(this.managementActionTaskRepository.findByRelatedId(deploymentSubsystem.getId()));

            deploymentSubsystem.getDeploymentServices().forEach(deploymentService ->
            {
                managementActionTasks.addAll(this.managementActionTaskRepository.findByRelatedId(deploymentService.getId()));

                deploymentService.getInstances().stream().map(deploymentInstance ->
                        this.managementActionTaskRepository.findByRelatedId(deploymentInstance.getId())).forEach(managementActionTasks::addAll);
            });
        });

        // For each pending management task associated to deployment plan, rejected them
        managementActionTasks.stream().map(managementActionTask -> this.toDoTaskRepository.findById(managementActionTask.getId())).filter(Optional::isPresent).map(Optional::get).forEach(managementTask ->
        {
            // reject the to do task and update task status
            managementTask.setStatus(ToDoTaskStatus.REJECTED);
            managementTask.setClosingMotive(DeploymentConstants.CLOSE_PENDING_TASK_MESSAGE);
            this.toDoTaskRepository.save(managementTask);

            // add the to do task id to the list
            rejectedTaskList.add(managementTask.getId());
        });

        log.debug("[DeploymentUtils] -> [rejectPlanPendingTask]: Rejected pending tasks with IDs: [{}] of deploymentPlan: [{}]", rejectedTaskList, deploymentPlan.getId());
    }

    /**
     * Add an entry to a plan history.
     *
     * @param type     {@link ChangeType}
     * @param plan     {@link DeploymentPlan}
     * @param userCode user
     * @param message  Message
     */
    public void addHistoryEntry(
            final ChangeType type,
            final DeploymentPlan plan,
            final String userCode,
            final String message)
    {
        DeploymentChange change = new DeploymentChange(plan, type, message);
        change.setUserCode(userCode);
        plan.getChanges().add(change);
        changeRepository.saveAndFlush(change);
    }

    /**
     * Build a subsystem status dto from ether manager service
     *
     * @param etherSubsystemStatusDTOArray a ether subsystem status DTO array
     * @return a deploy subsystem status DTO array
     */
    public List<DeploySubsystemStatusDTO> buildDeploySubsystemStatusDtoList(final EtherSubsystemStatusDTO[] etherSubsystemStatusDTOArray)
    {
        List<DeploySubsystemStatusDTO> deploySubsystemStatusDTOList = new ArrayList<>();

        for (EtherSubsystemStatusDTO etherSubsystemStatusDTO : etherSubsystemStatusDTOArray)
        {
            DeploySubsystemStatusDTO deploySubsystemStatusDTO = new DeploySubsystemStatusDTO();

            deploySubsystemStatusDTO.setSubsystemId(etherSubsystemStatusDTO.getSubsystemId());
            deploySubsystemStatusDTO.setServices(this.buildDeployServiceStatusDTO(etherSubsystemStatusDTO.getServices()));
            deploySubsystemStatusDTO.setStatus(this.buildDeployServiceStatusDTO(etherSubsystemStatusDTO.getStatus()));

            // Save into deploy subsystem status dto list
            deploySubsystemStatusDTOList.add(deploySubsystemStatusDTO);
        }

        return deploySubsystemStatusDTOList;
    }

    /**
     * Build a deploy service status DTO from service status DTO from ether service
     *
     * @param serviceStatusDTO a service status dto
     * @return a deploy service status Dto
     */
    public DeployServiceStatusDTO buildEtherDeployServiceStatusDTO(final ServiceStatusDTO serviceStatusDTO)
    {
        DeployServiceStatusDTO deployServiceStatusDTO = new DeployServiceStatusDTO();

        // In Ether platform, does not exists instance concept. Set a empty array
        deployServiceStatusDTO.setInstances(new DeployInstanceStatusDTO[0]);
        deployServiceStatusDTO.setStatus(this.buildDeployServiceStatusDTO(serviceStatusDTO.getStatus()));
        deployServiceStatusDTO.setServiceId(serviceStatusDTO.getServiceId());

        return deployServiceStatusDTO;
    }

    public DeployInstanceStatusDTO buildDeployInstanceStatusDTO(final ServiceStatusDTO serviceStatusDTO, final DeploymentService deploymentService)
    {

        // 1.For every instance stopped in the service checked, obtain its status and add to the list
        // In this case there are one or more instances in Ether platform matching with one in NOVA
        DeployStatusDTO deployStatusDTO = new DeployStatusDTO();
        deployStatusDTO.setRunning(serviceStatusDTO.getStatus().getRunning());
        deployStatusDTO.setTotal(serviceStatusDTO.getStatus().getTotal());
        deployStatusDTO.setExited(subtractExact(serviceStatusDTO.getStatus().getTotal(), serviceStatusDTO.getStatus().getRunning()));

        final DeployInstanceStatusDTO deployInstanceStatusDTO = new DeployInstanceStatusDTO();
        deployInstanceStatusDTO.setStatus(deployStatusDTO);

        final List<DeploymentInstance> instances = deploymentService.getInstances();
        if (instances != null && !instances.isEmpty())
        {
            deployInstanceStatusDTO.setInstanceId(deploymentService.getInstances().get(0).getId());
        }

        return deployInstanceStatusDTO;
    }

    public int getNumberOfInstancesForEphoenixService(final String environment, final String serviceName)
    {
        DeploymentLabel deploymentLabel = this.deploymentLabelRepository.findFirstByEnvironmentAndServiceName(
                environment, serviceName);

        if (deploymentLabel == null || Strings.isNullOrEmpty(deploymentLabel.getLabels()))
        {
            log.error("[DeploymentUtils] -> [getNumberOfInstancesForEphoenixService]: the ephoenix service: [{}] " +
                    "doesn't have the labels parameterized for environment [{}] in NOVA Platform", serviceName, environment);
            throw new NovaException(DeploymentError.getNoLabelEphoenixServiceError(environment, serviceName));
        }
        log.debug("[DeploymentUtils] -> [getNumberOfInstancesForEphoenixService]: the ephoenix service: [{}] " +
                "has the labels parameterized: [{}] for environment [{}] in NOVA Platform", serviceName, deploymentLabel.getLabels(), environment);
        Long numberOfinstancesEphoenix = Arrays.stream(StringUtils.split(deploymentLabel.getLabels(), ",")).count();

        return numberOfinstancesEphoenix.intValue();
    }


    //////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////

    /**
     * Build basic ether deployment dto
     *
     * @param deploymentPlan      plan
     * @param release             release
     * @param etherServiceDTOList service list
     * @param etherResponseDTO    callback
     * @return a basic ether deployment dto
     */
    private EtherDeploymentDTO buildBasicEtherDeploymentDTO(final DeploymentPlan deploymentPlan, final Release release, final List<EtherServiceDTO> etherServiceDTOList, final EtherResponseDTO etherResponseDTO)
    {
        return this.buildBasicEtherDeploymentDTO(deploymentPlan, release, etherServiceDTOList, etherResponseDTO, null);
    }

    /**
     * Build basic ether deployment dto
     *
     * @param deploymentPlan      plan
     * @param release             release
     * @param etherServiceDTOList service list
     * @param etherResponseDTO    callback
     * @param deploymentScopeDTO  scope
     * @return a basic ether deployment dto
     */
    private EtherDeploymentDTO buildBasicEtherDeploymentDTO(final DeploymentPlan deploymentPlan, final Release release, final List<EtherServiceDTO> etherServiceDTOList, final EtherResponseDTO etherResponseDTO, final EtherDeploymentScopeDTO deploymentScopeDTO)
    {
        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        log.debug("[DeploymentUtils] -> [buildBasicEtherDeploymentDTO]: building a ether deployment DTO wiht deployment plan id: [{}] - release name: [{}] - ether service DTO list: [{}] - ether response DTO: [{}] - ether scpoe DTO: [{}]",
                deploymentPlan.getId(), release.getName(), etherServiceDTOList, etherResponseDTO, deploymentScopeDTO);

        etherDeploymentDTO.setDeploymentId(deploymentPlan.getId());
        etherDeploymentDTO.setEnvironment(deploymentPlan.getEnvironment());
        etherDeploymentDTO.setReleaseName(release.getName());
        etherDeploymentDTO.setReleaseVersionName(deploymentPlan.getReleaseVersion().getVersionName());
        etherDeploymentDTO.setUuaa(release.getProduct().getUuaa());
        etherDeploymentDTO.setProductName(release.getProduct().getName());
        etherDeploymentDTO.setNamespaceDeploy(deploymentPlan.getEtherNs());
        
        boolean isCesEnabled = false;
        LogLevel mgwLogLevel = LogLevel.INFO;
        switch (Environment.valueOf(deploymentPlan.getEnvironment()))
        {
            case INT:
            case LAB_INT:
            case STAGING_INT:
                isCesEnabled = release.getProduct().isCesEnabledInt();
                mgwLogLevel = release.getProduct().getMgwLogLevelInt();
                break;
            case PRE:
            case LAB_PRE:
            case STAGING_PRE:
                isCesEnabled = release.getProduct().isCesEnabledPre();
                mgwLogLevel = release.getProduct().getMgwLogLevelPre();
                break;
            case PRO:
            case LAB_PRO:
            case STAGING_PRO:
                isCesEnabled = release.getProduct().isCesEnabledPro();
                mgwLogLevel = release.getProduct().getMgwLogLevelPro();
                break;
        }
        etherDeploymentDTO.setCesEnabled(isCesEnabled);
        etherDeploymentDTO.setMicrogwLogLevel(mgwLogLevel.name());

        etherDeploymentDTO.setEtherServices(etherServiceDTOList.toArray(new EtherServiceDTO[0]));
        etherDeploymentDTO.setEtherCallback(etherResponseDTO);

        if (deploymentScopeDTO != null)
        {
            etherDeploymentDTO.setDeploymentScope(deploymentScopeDTO);
        }

        log.debug("[DeploymentUtils] -> [buildBasicEtherDeploymentDTO]: built a ether deployment DTO: [{}]", etherDeploymentDTO);
        return etherDeploymentDTO;
    }

    /**
     * Build basic ether subsystem dto
     *
     * @param deploymentSubsystem subsystem
     * @param etherServiceDTOList service list
     * @return a basic ether deployment subsystem dto
     */
    private EtherSubsystemDTO buildBasicEtherSubsystemDTO(final DeploymentSubsystem deploymentSubsystem, final List<EtherServiceDTO> etherServiceDTOList)
    {
        ReleaseVersion releaseVersion = deploymentSubsystem.getDeploymentPlan().getReleaseVersion();
        Release release = releaseVersion.getRelease();
        EtherSubsystemDTO etherSubsystemDTO = new EtherSubsystemDTO();

        etherSubsystemDTO.setSubsystemId(deploymentSubsystem.getId());
        etherSubsystemDTO.setEnvironment(deploymentSubsystem.getDeploymentPlan().getEnvironment());
        etherSubsystemDTO.setReleaseName(release.getName());
        etherSubsystemDTO.setReleaseVersionName(releaseVersion.getVersionName());
        etherSubsystemDTO.setUuaa(release.getProduct().getUuaa());
        etherSubsystemDTO.setProductName(release.getProduct().getName());
        etherSubsystemDTO.setNamespace(deploymentSubsystem.getDeploymentPlan().getEtherNs());

        etherSubsystemDTO.setEtherServices(etherServiceDTOList.toArray(new EtherServiceDTO[0]));

        return etherSubsystemDTO;
    }

    /**
     * Builds a list with all the necessary information for deploying on cloud environment
     *
     * @param deploymentService service
     * @return a EtherServiceDTO object
     */
    private EtherServiceDTO buildEtherServiceDTO(final DeploymentService deploymentService)
    {
        log.debug("DeploymentsUtils] -> [buildEtherServiceDTO]: building a ether service DTO from deployment service id: [{}]", deploymentService.getService());
        EtherServiceDTO etherServiceDTO = new EtherServiceDTO();

        HardwarePack hardwarePack = deploymentService.getHardwarePack();
        ReleaseVersionService releaseVersionService = deploymentService.getService();

        etherServiceDTO.setArtifactId(releaseVersionService.getArtifactId());
        etherServiceDTO.setDockerKey(deploymentService.getDockerKey());

        //Filesystems
        FileSystemAttachDTO[] fileSystemAttachDTOS = deploymentService.getDeploymentServiceFilesystems().stream().map(deploymentServiceFilesystem -> {
            FileSystemAttachDTO filesystemAttachDTO = new FileSystemAttachDTO();
            filesystemAttachDTO.setVolumeBind(deploymentServiceFilesystem.getVolumeBind());
            filesystemAttachDTO.setVolumeId(deploymentServiceFilesystem.getFilesystem().getName());
            return filesystemAttachDTO;
        }).toArray(FileSystemAttachDTO[]::new);
        etherServiceDTO.setFilesystems(fileSystemAttachDTOS);

        etherServiceDTO.setGroupId(releaseVersionService.getGroupId());

        //Hardware
        HardwareDTO hardwareDTO = new HardwareDTO();
        if (hardwarePack != null)
        {
            hardwareDTO.setInstance(hardwarePack.getCode());
            hardwareDTO.setMemoryFactor(deploymentService.getMemoryFactor());
            hardwareDTO.setRamMB(hardwarePack.getRamMB());
        }
        etherServiceDTO.setHardware(hardwareDTO);

        //JVM Parameters
        String jvmParametersList = jdkParameterRepository.findByDeploymentService(deploymentService.getId()).stream()
                .map(JdkParameter::getName)
                .collect(Collectors.joining(" "));
        etherServiceDTO.setJvmParameters(jvmParametersList);

        if(ServiceType.isJdkSelectable(ServiceType.valueOf(deploymentService.getService().getServiceType())))
        {
            String jdkVersion = "8";
            if(releaseVersionService.getAllowedJdk() != null)
            {
                jdkVersion = releaseVersionService.getAllowedJdk().getJvmVersion().split("\\.")[0];
            }
            final PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setKey(JDK_VERSION);
            propertyDTO.setValue(jdkVersion);
            etherServiceDTO.setProperties(new PropertyDTO[]{ propertyDTO });
        }

        etherServiceDTO.setNumInstances(deploymentService.getNumberOfInstances());
        etherServiceDTO.setServiceId(deploymentService.getService().getId());
        etherServiceDTO.setDeploymentServiceId(deploymentService.getId());
        etherServiceDTO.setReleaseVersionId(releaseVersionService.getId());
        etherServiceDTO.setServiceName(releaseVersionService.getServiceName());
        etherServiceDTO.setVersion(releaseVersionService.getVersion());
        etherServiceDTO.setDomainSnippet(this.generateEtherDomainSnippet(deploymentService));
        etherServiceDTO.setProtocol(Constants.PRODUCT_SERVICE_API_PROTOCOL);

        etherServiceDTO.setServiceType(getServiceType(deploymentService).name());

        log.debug("DeploymentsUtils] -> [buildEtherServiceDTO]: built a ether service DTO from deployment service id: [{}]. Ether service DTO: [{}]", deploymentService.getService(), etherServiceDTO);
        return etherServiceDTO;
    }

    public SimpleServiceType getServiceType(final DeploymentService deploymentService)
    {
        final SimpleServiceType serviceType;
        if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch())
        {
            serviceType = SimpleServiceType.BATCH;
        }
        else if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isRest())
        {
            serviceType = SimpleServiceType.API_REST;
        }
        else if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isCdn())
        {
            serviceType = SimpleServiceType.CDN;
        }
        else if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isDaemon())
        {
            serviceType = SimpleServiceType.DAEMON;
        }
        else
        {
            serviceType = SimpleServiceType.UNKNOWN;
        }

        return serviceType;
    }

    private ServiceStatusDTO[] buildServiceStatus(final EtherServiceStatusDTO[] etherServiceStatusDTOArray)
    {
        List<ServiceStatusDTO> serviceStatusDTOList = new ArrayList<>();

        for (EtherServiceStatusDTO etherServiceStatusDTO : etherServiceStatusDTOArray)
        {
            ServiceStatusDTO serviceStatusDTO = new ServiceStatusDTO();

            serviceStatusDTO.setServiceId(etherServiceStatusDTO.getServiceId());
            serviceStatusDTO.setStatus(this.buildStatusDTO(etherServiceStatusDTO.getStatus()));

            serviceStatusDTOList.add(serviceStatusDTO);
        }

        return serviceStatusDTOList.toArray(new ServiceStatusDTO[0]);
    }

    /**
     * Builds a service status dto
     *
     * @param etherSubsystemStatusDTO subsystem status
     * @return service status dto
     */
    private StatusDTO buildStatusDTO(final EtherSubsystemStatusDTO etherSubsystemStatusDTO)
    {

        StatusDTO statusDTO = new StatusDTO();

        statusDTO.setTotal(etherSubsystemStatusDTO.getStatus().getTotalContainers());
        statusDTO.setRunning(etherSubsystemStatusDTO.getStatus().getRunningContainers());

        return statusDTO;
    }

    /**
     * Builds a service status dto
     *
     * @param etherStatusDTO subsystem status
     * @return service status dto
     */
    private StatusDTO buildStatusDTO(final EtherStatusDTO etherStatusDTO)
    {
        StatusDTO statusDTO = new StatusDTO();

        statusDTO.setTotal(etherStatusDTO.getTotalContainers());
        statusDTO.setRunning(etherStatusDTO.getRunningContainers());

        return statusDTO;
    }

    private ServiceStatusDTO[] buildServiceStatus(final DeployServiceStatusDTO[] etherServiceStatusDTOArray)
    {
        List<ServiceStatusDTO> serviceStatusDTOList = new ArrayList<>();

        for (DeployServiceStatusDTO deployServiceStatusDTO : etherServiceStatusDTOArray)
        {
            ServiceStatusDTO serviceStatusDTO = new ServiceStatusDTO();

            serviceStatusDTO.setServiceId(deployServiceStatusDTO.getServiceId());
            serviceStatusDTO.setStatus(this.buildStatusDTO(deployServiceStatusDTO.getStatus()));
            serviceStatusDTO.setInstances(this.buildInstancesDTO(deployServiceStatusDTO.getInstances()));

            serviceStatusDTOList.add(serviceStatusDTO);
        }

        return serviceStatusDTOList.toArray(new ServiceStatusDTO[0]);
    }

    /**
     * Builds a status dto
     *
     * @param deployStatusDTO deploy status
     * @return service status dto
     */
    private StatusDTO buildStatusDTO(final DeployStatusDTO deployStatusDTO)
    {
        StatusDTO statusDTO = new StatusDTO();

        BeanUtils.copyProperties(deployStatusDTO, statusDTO);

        return statusDTO;
    }

    /**
     * Builds a status dto
     *
     * @param deployInstanceStatusDTOArray deploy instance status
     * @return service status dto
     */
    private InstanceStatusDTO[] buildInstancesDTO(final DeployInstanceStatusDTO[] deployInstanceStatusDTOArray)
    {
        List<InstanceStatusDTO> instanceStatusDTOList = new ArrayList<>();

        for (DeployInstanceStatusDTO deployInstanceStatusDTO : deployInstanceStatusDTOArray)
        {
            InstanceStatusDTO instanceStatusDTO = new InstanceStatusDTO();

            instanceStatusDTO.setInstanceId(deployInstanceStatusDTO.getInstanceId());
            instanceStatusDTO.setStatus(this.buildStatusDTO(deployInstanceStatusDTO.getStatus()));

            instanceStatusDTOList.add(instanceStatusDTO);
        }
        return instanceStatusDTOList.toArray(new InstanceStatusDTO[0]);
    }

    /**
     * Generates a domain snippet (i.e. a subdomain) for the service in AppEngine
     *
     * @param deploymentService Deployment Service
     * @return The domain snippet follows the pattern: groupId-serviceName-releaseName-majorVersion
     */
    private String generateEtherDomainSnippet(final DeploymentService deploymentService)
    {
        return EtherServiceNamingUtils.getEAEServiceDomainSnippet(deploymentService.getService().getGroupId(),
                deploymentService.getService().getServiceName(),
                deploymentService.getDeploymentSubsystem().getSubsystem().getReleaseVersion().getRelease().getName(),
                deploymentService.getService().getVersion(), deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
    }

    /**
     * Permission exception
     */
    public static final NovaException PERMISSION_DENIED = new NovaException(DeploymentError.getForbiddenError());


    /**
     * Builder todotask response dto todotask response dto.
     *
     * @param generated    the generated
     * @param assignedRole the assigned role
     * @param taskId       the task id
     * @param toDoTaskType the to do task type
     * @return the todotask response dto
     */
    public TodoTaskResponseDTO builderTodoTaskResponseDTO(boolean generated, RoleType assignedRole, Integer taskId, ToDoTaskType toDoTaskType)
    {

        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();

        todoTaskResponseDTO.setGenerated(generated);
        if (generated)
        {
            if (taskId == null)
            {
                log.error("[{}] -> [buildTodoTaskResponseDTO]  : Error: Task id null when building todotaskResponseDto", this.getClass().getSimpleName());
                throw new NovaException(DeploymentError.getTaskIdIsNullError());
            }

            todoTaskResponseDTO.setTodoTaskId(taskId);

            if (assignedRole != null)
            {
                todoTaskResponseDTO.setAssignedRole(assignedRole.name());
            }
            if (toDoTaskType != null)
            {
                todoTaskResponseDTO.setTodoTaskType(toDoTaskType.name());
            }
        }
        return todoTaskResponseDTO;
    }

    /**
     * Builder todotask response dto todotask response dto without values.
     *
     * @param generated the generated
     * @return the todotask response dto
     */
    public TodoTaskResponseDTO builderTodoTaskResponseDTO(boolean generated)
    {

        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();

        todoTaskResponseDTO.setGenerated(generated);

        return todoTaskResponseDTO;
    }

    /**
     * Build the URL pointing to the pass in GCSP's portal for a given GCSP's pass ID.
     *
     * @param gcspPassId The given GCSP's pass ID.
     * @return The URL pointing to the pass in GCSP's portal
     */
    public String buildGcspPassUrl(String gcspPassId)
    {
        return (Strings.isNullOrEmpty(gcspPassId) || "0".equals(gcspPassId)) ? null : UriComponentsBuilder.fromHttpUrl(this.gcspUrl).buildAndExpand(gcspPassId).toUriString();
    }

    /**
     * Build a deploy service status DTO array from ether
     *
     * @param etherServiceStatusDTOArray the ether service status DTO array
     * @return a deploy service status DTO array
     */
    private DeployServiceStatusDTO[] buildDeployServiceStatusDTO(final EtherServiceStatusDTO[] etherServiceStatusDTOArray)
    {
        List<DeployServiceStatusDTO> deployServiceStatusDTOList = new ArrayList<>();

        for (EtherServiceStatusDTO etherServiceStatusDTO : etherServiceStatusDTOArray)
        {
            DeployServiceStatusDTO deployServiceStatusDTO = new DeployServiceStatusDTO();

            // Check and get a deployment service
            DeploymentService deploymentService = this.deploymentsValidator.validateAndGetDeploymentService(etherServiceStatusDTO.getServiceId());
            deployServiceStatusDTO.setServiceId(etherServiceStatusDTO.getServiceId());
            deployServiceStatusDTO.setServiceName(deploymentService.getService().getServiceName());
            deployServiceStatusDTO.setServiceType(deploymentService.getService().getServiceType());
            deployServiceStatusDTO.setStatus(this.buildDeployServiceStatusDTO(etherServiceStatusDTO.getStatus()));

            // In Ether platform, does not exists instance concept. Set a empty array
            deployServiceStatusDTO.setInstances(new DeployInstanceStatusDTO[0]);

            // Save into deploy service status dto list
            deployServiceStatusDTOList.add(deployServiceStatusDTO);
        }

        return deployServiceStatusDTOList.toArray(new DeployServiceStatusDTO[0]);
    }

    /**
     * Build a deploy service status DTO array from ether status
     *
     * @param etherStatusDTO a ether status DTO
     * @return a deploy status DTO
     */
    private DeployStatusDTO buildDeployServiceStatusDTO(final EtherStatusDTO etherStatusDTO)
    {
        DeployStatusDTO deployStatusDTO = new DeployStatusDTO();

        deployStatusDTO.setTotal(etherStatusDTO.getTotalContainers());
        deployStatusDTO.setRunning(etherStatusDTO.getRunningContainers());
        deployStatusDTO.setExited(etherStatusDTO.getTotalContainers() - etherStatusDTO.getRunningContainers());

        return deployStatusDTO;
    }

    /**
     * Build a deploy service status DTO array from ether service status
     *
     * @param statusDTO a status DTO
     * @return a deploy status DTO
     */
    private DeployStatusDTO buildDeployServiceStatusDTO(final StatusDTO statusDTO)
    {
        DeployStatusDTO deployStatusDTO = new DeployStatusDTO();

        deployStatusDTO.setTotal(statusDTO.getTotal());
        deployStatusDTO.setRunning(statusDTO.getRunning());
        deployStatusDTO.setExited(statusDTO.getTotal() - statusDTO.getRunning());

        return deployStatusDTO;
    }

}
