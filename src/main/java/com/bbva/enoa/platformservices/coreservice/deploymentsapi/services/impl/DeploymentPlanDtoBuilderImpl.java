package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.*;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentInstanceType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.utils.HibernateUtils;
import com.bbva.enoa.platformservices.coreservice.common.builders.IBasicAlertDTOBuilder;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ScheduleControlMClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.autoconfig.NovaToolsProperties;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.enums.SimpleServiceType;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.BuildersError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentBroker;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentGcspService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentNovaService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.JdkParametersResultSetMapper;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.MonitoringUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Builds a DeploymentDto from a DeploymentPlan.
 * <p>
 * Created by xe52580 on 09/03/2017.
 */
@Slf4j
@Service
public class DeploymentPlanDtoBuilderImpl implements IDeploymentPlanDtoBuilder
{

    private static final List<ToDoTaskType> PENDING_ACTION_TODOTASK_TYPES = List.of(
            ToDoTaskType.SERVICE_START,
            ToDoTaskType.SERVICE_STOP,
            ToDoTaskType.SERVICE_RESTART,
            ToDoTaskType.BATCH_SCHEDULE_START,
            ToDoTaskType.BATCH_SCHEDULE_STOP,
            ToDoTaskType.START_INSTANCE_BATCH_ERROR,
            ToDoTaskType.START_INSTANCE_BATCH,
            ToDoTaskType.STOP_INSTANCE_BATCH_ERROR,
            ToDoTaskType.STOP_INSTANCE_BATCH,
            ToDoTaskType.UPDATE_INSTANCE_BATCH_ERROR);
    /**
     * url base for INT
     */
    @Value("${nova.mappings.baseUrl.int}")
    private String intBaseUrl;

    /**
     * url base for PRE
     */
    @Value("${nova.mappings.baseUrl.pre}")
    private String preBaseUrl;

    /**
     * url base for PRO
     */
    @Value("${nova.mappings.baseUrl.pro}")
    private String proBaseUrl;

    /**
     * url Cloud Gateway base for INT
     */
    @Value("${nova.cloudGatewayUrl.int}")
    private String intCloudGatewayBaseUrl;

    /**
     * url base Cloud Gateway for PRE
     */
    @Value("${nova.cloudGatewayUrl.pre}")
    private String preCloudGatewayBaseUrl;

    /**
     * url base Cloud Gateway for PRO
     */
    @Value("${nova.cloudGatewayUrl.pro}")
    private String proCloudGatewayBaseUrl;

    /**
     * Whether to validate that the Batches of a Deployment Plan, not in a NOVA Scheduler (i.e. that will be started by Control M),
     * have their associated documentation.
     */
    @Value("${nova.validate-documents-before-deploying-control-m-batches:false}")
    private boolean validateDocumentsBeforeDeployingControlMBatches;


    @Autowired
    private NovaToolsProperties novaToolsProperties;

    /**
     * deployment paln repository
     */
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    /**
     * deployment task repository
     */
    @Autowired
    private DeploymentTaskRepository deploymentTaskRepository;

    /**
     * configuration task repository
     */
    @Autowired
    private ConfigurationTaskRepository configurationTaskRepository;

    /**
     * JDK parameter repository
     */
    @Autowired
    private JdkParameterRepository jdkParameterRepository;

    /**
     * Jmx parameter repository
     */
    @Autowired
    private JmxParameterRepository jmxParameterRepository;

    /**
     * monitoring Utils
     */
    @Autowired
    private MonitoringUtils monitoringUtils;

    /**
     * scheduleBatchTask repository
     */
    @Autowired
    private ScheduleBatchTaskRepository scheduleBatchTaskRepo;

    /**
     * batch services schedule repository
     */
    @Autowired
    private BatchServiceScheduleRepository batchServiceScheduleRepo;

    /**
     * GCSP deployment service
     */
    @Autowired
    private IDeploymentGcspService gcspDeploymentService;

    /**
     * NOVA  deployment service
     */
    @Autowired
    private IDeploymentNovaService novaDeploymentService;

    /**
     * CPD Repository
     */
    @Autowired
    private CPDRepository cpdRepository;

    /**
     * Tools service client
     */
    @Autowired
    public IToolsClient toolsClient;

    /**
     * Deployments utils
     */
    @Autowired
    public DeploymentUtils deploymentUtils;

    /**
     * ProjectFileValidator
     * Manage validation utils
     */
    @Autowired
    public ManageValidationUtils manageValidationUtils;

    @Autowired
    public IAlertServiceApiClient alertServiceClient;

    @Autowired
    private JvmJdkConfigurationChecker jvmJdkConfigurationChecker;

    @Autowired
    private AllowedJdkParameterProductRepository allowedJdkParameterProductRepository;

    /**
     * Scheduler request client
     */
    @Autowired
    private ScheduleControlMClient scheduleControlMClient;

    /**
     * Batch Schedule Service.
     */
    @Autowired
    private IBatchScheduleService batchScheduleService;

    /**
     * Cipher credential service
     */
    @Autowired
    private ICipherCredentialService cipherCredentialService;

    /**
     * Deployemnt broker
     */
    @Autowired
    private IDeploymentBroker deploymentBroker;

    @Autowired
    private Set<IBasicAlertDTOBuilder> alertDTOBuilders;

    @Override
    public DeploymentDto build(DeploymentPlan plan, String ivUser)
    {
        log.debug("[DeploymentPlanDtoBuilderImpl] -> [build]: Building DTO from plan {}", plan.getId());

        // Set the basic data.
        DeploymentDto dto = this.buildBasicData(plan, ivUser);

        // Add plan subsystems.
        dto.setSubsystems(this.getSubsystemsFromPlan(plan));

        //Check the alerts info for the plan
        dto.setHasPendingAlerts(this.buildAlertInfoInformation(plan));

        // Plan has pending resources if any service lacks from hardware pack or
        // is using an archived filesystem or is using an archived logical connector
        dto.setPendingResources(deploymentPlanRepository.planHasPendingResources(plan.getId())
                || deploymentPlanRepository.planHasPendingFilesystems(plan.getId())
                || deploymentPlanRepository.planHasPendingLogicalConnector(plan.getId()));

        log.trace("[DeploymentPlanDtoBuilderImpl] -> [build]: DTO built with value {}", dto);

        return dto;
    }

    @Override
    public DeploymentDto[] build(List<DeploymentPlan> plans, String ivUser)
    {
        log.debug("[DeploymentPlanDtoBuilderImpl] -> [build]: Building DTO from list of plans");

        DeploymentDto[] dtoPlans = new DeploymentDto[plans.size()];

        int i = 0;
        for (DeploymentPlan plan : plans)
        {
            // Copy basic data from the plan.
            DeploymentDto dto = this.buildBasicData(plan, ivUser);

            // Add plan subsystems.
            dto.setSubsystems(this.getSubsystemsFromPlan(plan));

            // Check if any services from the plan has no resources set.
            dto.setPendingResources(deploymentPlanRepository.planHasPendingResources(plan.getId()));

            //Add alert info for the plan
            dto.setHasPendingAlerts(this.buildAlertInfoInformation(plan));

            // Add it to the array.
            dtoPlans[i] = dto;
            i++;
        }

        log.trace("[DeploymentPlanDtoBuilderImpl] -> [build]: DTO list built with value {}", dtoPlans);

        return dtoPlans;
    }


    @Override
    @Transactional
    public DeploymentServiceDto buildDtoFromEntity(final DeploymentService deploymentService)
    {
        final DeploymentServiceDto dto = new DeploymentServiceDto();
        try
        {
            final ReleaseVersionService service = deploymentService.getService();
            final DeploymentPlan plan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
            final ReleaseVersion version = plan.getReleaseVersion();
            final Release release = version.getRelease();
            final Product product = release.getProduct();

            // Copy properties from original service.
            dto.setServiceName(service.getServiceName());
            dto.setVersion(service.getVersion());
            dto.setArtifactId(service.getArtifactId());
            dto.setGroupId(service.getGroupId());
            dto.setServeApiIds(ArrayUtils.toPrimitive(this.getServeApiIds(service).toArray(new Integer[0])));
            dto.setHasAsyncBackToBack(this.hasAsyncApiBackToBack(deploymentService.getService()));
            dto.setServiceId(deploymentService.getId());
            //Action
            if (deploymentService.getAction() != null)
            {
                dto.setAction(deploymentService.getAction().getAction());
            }
            // Copy properties from deployment service.
            // Must be the last to override the id and dates.
            dto.setNumberOfInstances(deploymentService.getNumberOfInstances());
            dto.setMemoryFactor(deploymentService.getMemoryFactor());
            // Copy different properties.
            dto.setId(deploymentService.getId());
            dto.setServiceFinalname(service.getFinalName());
            dto.setFolder(service.getFolder());
            dto.setServiceType(service.getServiceType());
            dto.setDescription(service.getDescription());
            dto.setDockerKey(deploymentService.getDockerKey());
            // If it has a hardware pack, copy it.
            if (deploymentService.getHardwarePack() != null)
            {
                dto.setHardwarePackCode(deploymentService.getHardwarePack().getCode());
                dto.setRamMb(deploymentService.getHardwarePack().getRamMB());
            }
            dto.setJvmParameters(this.getJvmParameters(deploymentService));
            // If it has filesystems, copy them.
            dto.setFilesystems(this.getDeploymentServiceFilesystemDtos(deploymentService));
            dto.setSupportsMultiFilesystem(service.getSupportsMultiFilesystem());
            // If has a logical connector list, copy it.
            List<LogicalConnector> logicalConnectorList = deploymentService.getLogicalConnectors();
            if (logicalConnectorList != null && !logicalConnectorList.isEmpty())
            {
                dto.setLogicalConnectors(this.getDeploymentLogicalConnectorDtos(logicalConnectorList));
            }
            // Service name in NOVA.
            String novaServiceName = ServiceNamingUtils.getNovaServiceName(service.getGroupId(), service.getArtifactId(), service.getVersionSubsystem().getReleaseVersion().getRelease().getName(), service.getVersion().split("\\.")[0]);

            // Get NovaYml. In some Services is Null (Old)
            LobFile novaYml = service.getNovaYml();
            String novaYmlContents = "";
            if (novaYml != null)
            {
                novaYmlContents = novaYml.getContents();
            }
            // Endpoint in NOVA to access the application - will work only if deployed.
            this.setEndpoint(deploymentService, dto, service, novaServiceName, novaYmlContents);
            // Set pending action task
            dto.setPendingActionTask(this.buildPendingActionTask(deploymentService.getId(), PENDING_ACTION_TODOTASK_TYPES));
            // Add instances.
            updateInstancesFromServiceToDto(dto, deploymentService);
            if (ServiceType.valueOf(service.getServiceType()).isBatch())
            {
                DeploymentTask scheduleTask = scheduleBatchTaskRepo.findLastScheduleTask(deploymentService);
                dto.setPendingScheduleTask(buildDtoFromEntity(scheduleTask));
                BatchServiceSchedule currentSchedule = batchServiceScheduleRepo.findByIsActiveTrueAndBatchService(deploymentService);
                dto.setCurrentSchedule(buildDtoFromEntity(currentSchedule));
            }
            dto.setEnvironment(plan.getProfilingEnv());
            dto.setDeploymentPlanId(plan.getId());
            dto.setReleaseVersionId(version.getId());
            dto.setReleaseVersionName(version.getVersionName());
            dto.setReleaseId(release.getId());
            dto.setReleaseName(release.getName());
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
            dto.setReleaseServiceId(service.getId());

            if (PlatformUtils.isPlanLoggingInEther(deploymentService.getDeploymentSubsystem().getDeploymentPlan()))
            {
                dto.setMonitoredResourceId(EtherServiceNamingUtils.getMRMonitoredResourceId(release.getName(), service.getServiceName()));
            }

            final SimpleServiceType serviceType = deploymentUtils.getServiceType(deploymentService);

            // Atenea URL is established when:
            //   - Logging in ether is activated
            //   - Service is: CDN, API REST, DAEMON or BATCH => Anything different from UNKNOWN
            if (PlatformUtils.isPlanLoggingInEther(plan) && serviceType != SimpleServiceType.UNKNOWN)
            {
                final String ateneaLogUrl = PlatformUtils.buildAteneaLogURL(novaToolsProperties.getAtenea(), Environment.valueOf(plan.getEnvironment()), plan.getEtherNs(), plan.getReleaseVersion().getRelease().getName(), service.getServiceName());
                dto.setAteneaUrl(ateneaLogUrl);
            }
            if (jvmJdkConfigurationChecker.isMultiJdk(service))
            {
                final AllowedJdk allowedJdk = service.getAllowedJdk();
                final String jvmVersion = allowedJdk.getJvmVersion();
                dto.setReleaseServiceJvmVersion(jvmVersion);
                dto.setReleaseServiceJdkName(allowedJdk.getReadableName());
                final List<Object[]> appliableJdkParameters = allowedJdkParameterProductRepository.getSelectableJdkParameters(product.getId(), deploymentService.getId(), jvmVersion, allowedJdk.getJdk());
                final JdkParameterTypeDto[] appliableParametersArrayDto = JdkParametersResultSetMapper.getJdkParametersFrom(appliableJdkParameters);
                JdkTypedParametersDto appliableParametersDto = new JdkTypedParametersDto();
                appliableParametersDto.setTypedParameters(appliableParametersArrayDto);
                dto.setAppliableJvmParameters(appliableParametersDto);

                if (dto.getJvmParameters().contains(DeploymentConstants.JMX_PARAMS_NAME))
                {
                    JmxParametersDTO[] jmxParametersDTO = this.getJmxParamsInstances(deploymentService);
                    dto.setJmxParameters(jmxParametersDTO);

                }
            }
            // Set Schedule Request info.
            dto.setScheduleRequest(this.buildScheduleRequest(deploymentService.getDeploymentSubsystem().getDeploymentPlan()));
            // Set whether this plan is required to have an schedule request with its associated document.
            dto.setScheduleRequestAndDocumentRequired(this.isScheduleRequestAndDocumentRequired(deploymentService.getDeploymentSubsystem().getDeploymentPlan()));
            // Set brokers
            dto.setBrokers(this.deploymentBroker.buildDeploymentServiceBrokerDTOsFromDeploymentServiceEntity(deploymentService));

        }
        catch (Exception e)
        {
            log.error("[DeploymentPlanDtoBuilderImpl] -> [buildDtoFromEntity]: Error building depoymentServiceDto", e);
        }
        return dto;
    }

    @Override
    public DeploymentSummaryDto buildSummary(DeploymentPlan plan, String ivUser)
    {
        DeploymentSummaryDto dto = new DeploymentSummaryDto();
        this.mapDtoSummary(plan, dto, ivUser);
        return dto;
    }

    @Override
    public Boolean isScheduleRequestAndDocumentRequired(DeploymentPlan deploymentPlan)
    {
        // If there is a batch which name is not covered by any scheduler, then a "schedule request" with an associated document may be required (depending on
        // the property acting as a flag).
        return this.validateDocumentsBeforeDeployingControlMBatches && !this.namesOfBatchesServicesNotInSchedulers(deploymentPlan).isEmpty();
    }

    @Override
    public List<String> namesOfBatchesServicesNotInSchedulers(DeploymentPlan deploymentPlan)
    {
        String className = this.getClass().getSimpleName();
        String methodName = "namesOfBatchesServicesNotInSchedulers";

        // For this deployment plan: get the name of all services of type "batch", and get all the services of type scheduler.
        Set<String> namesOfBatchesServices = new HashSet<>();
        List<ReleaseVersionService> schedulerServices = new ArrayList<>();
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
            {
                if (ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch())
                {
                    namesOfBatchesServices.add(deploymentService.getService().getServiceName());
                }
                else if (deploymentService.getService().getServiceType().equals(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType()))
                {
                    schedulerServices.add(deploymentService.getService());
                }
            }
        }

        // Get the name of all the batches that are part of the services of type scheduler.
        List<String> namesOfBatchesInSchedulers = new ArrayList<>();
        for (ReleaseVersionService schedulerService : schedulerServices)
        {
            String schedulerYmlFileString = this.batchScheduleService.getSchedulerYmlStringFile(schedulerService);
            if (!Strings.isNullOrEmpty(schedulerYmlFileString))
            {
                namesOfBatchesInSchedulers.addAll(this.batchScheduleService.buildBatchNameList(schedulerYmlFileString, schedulerService.getServiceName()));
            }
        }

        log.debug("[DeploymentPlanDtoBuilderImpl] -> [namesOfBatchesServicesNotInSchedulers]: Batches in deployment plan: [{}]", String.join(", ", namesOfBatchesServices));
        log.debug("[DeploymentPlanDtoBuilderImpl] -> [namesOfBatchesServicesNotInSchedulers]: Batches in a scheduler: [{}]", String.join(", ", namesOfBatchesInSchedulers));

        // Get the names of the batches that are not part of any service of type scheduler.
        List<String> namesOfBatchesServicesNotInSchedulers = new ArrayList<>();
        for (String nameOfBatchService : namesOfBatchesServices)
        {
            if (!namesOfBatchesInSchedulers.contains(nameOfBatchService))
            {
                namesOfBatchesServicesNotInSchedulers.add(nameOfBatchService);
            }
        }
        log.debug("[DeploymentPlanDtoBuilderImpl] -> [namesOfBatchesServicesNotInSchedulers]: Batches not in a scheduler: [{}]", String.join(", ", namesOfBatchesServicesNotInSchedulers));
        return namesOfBatchesServicesNotInSchedulers;
    }

    /**
     * Calling Platform Health Monitor and building the alert information about every plan
     *
     * @param deploymentPlan the {@link DeploymentPlan} that we want to check
     */
    private RequestAlertsDTO buildAlertInfoInformation(@NotNull final DeploymentPlan deploymentPlan)
    {
        log.trace("[DeploymentPlanDtoBuilderImpl] -> [buildAlertInfoInformation]: Building alert Info Information for deploymentPlan [{}]", deploymentPlan.getId());


        //New objects instantation
        RequestAlertsDTO requestAlertsDTOToReturn = new RequestAlertsDTO();

        final var asRequestAlertsDTO = this.alertServiceClient.checkDeployPlanAlertInfo(deploymentPlan.getId(), new String[]{DeploymentConstants.OPEN_ALERT});

        // building each alert info
        final var alertsInfoArray = Optional.ofNullable(asRequestAlertsDTO).map(ASRequestAlertsDTO::getBasicAlertInfo)
                .map(asBasicAlertInfoDTOS -> Arrays.stream(asBasicAlertInfoDTOS)
                        .map(asBasicAlertInfoDTO -> this.findBuilderAndConvert(deploymentPlan, asBasicAlertInfoDTO))
                        .toArray(BasicAlertInfoDTO[]::new))
                .orElse(new BasicAlertInfoDTO[]{});

        requestAlertsDTOToReturn.setBasicAlertInfo(alertsInfoArray);
        requestAlertsDTOToReturn.setHaveAlerts(alertsInfoArray.length != 0);

        log.trace("[DeploymentPlanDtoBuilderImpl] -> [buildAlertInfoInformation]: built alert Info Information for deploymentPlan [{}], result: [{}]", deploymentPlan.getId(), requestAlertsDTOToReturn);

        return requestAlertsDTOToReturn;
    }

    /**
     * This method is on charge of searching the associated builder for the alert passed by parameter and convert the alert-service dto into our core-service dto.
     *
     * @param deploymentPlan      the {@link DeploymentPlan} has the open alert
     * @param asBasicAlertInfoDTO the {@link ASBasicAlertInfoDTO} received from the platform-health-monitor
     * @return a new {@link BasicAlertInfoDTO} initialized
     * @implNote we need specialization on the builders because we don't receive the alert associated service on the response.
     * So, depending on each alert code we will choose the related {@link IBasicAlertDTOBuilder}
     */
    private BasicAlertInfoDTO findBuilderAndConvert(final DeploymentPlan deploymentPlan, final ASBasicAlertInfoDTO asBasicAlertInfoDTO)
    {
        log.debug("[DeploymentPlanDtoBuilderImpl] -> [findBuilderAndConvert]: Searching for the associated alert type builder for the alert [{}]", this.getClass().getSimpleName(), asBasicAlertInfoDTO.getAlertId());
        return this.alertDTOBuilders
                .stream()
                .filter(iBasicAlertDTOBuilder -> iBasicAlertDTOBuilder.isSupported(asBasicAlertInfoDTO.getAlertType()))
                .findAny().orElseThrow(() -> new NovaException(BuildersError.getNoAvailableBuilderImplementation()))
                .convert(deploymentPlan, asBasicAlertInfoDTO);
    }


    /**
     * Builds a {@link DeploymentDto} from a {@link DeploymentPlan},
     * using only properties from the entity and none from its
     * related entities.
     *
     * @param deploymentPlan {@link DeploymentPlan}
     * @param ivUser         iv-User
     * @return DeploymentDto
     */
    private DeploymentDto buildBasicData(DeploymentPlan deploymentPlan, String ivUser)
    {
        DeploymentDto dto = new DeploymentDto();
        this.mapDtoSummary(deploymentPlan, dto, ivUser);
        // Common properties.

        dto.setRejectionMessage(deploymentPlan.getRejectionMessage());

        // Different properties.
        if (deploymentPlan.getParent() != null)
        {
            dto.setParentId(deploymentPlan.getParent().getId());
            dto.setParentEnvironment(deploymentPlan.getParent().getEnvironment());
        }

        if (deploymentPlan.getCreationDate() != null)
        {
            dto.setCreationDate(deploymentPlan.getCreationDate().getTimeInMillis());
        }

        // Set deployment type in pro
        dto.setDeploymentType(deploymentPlan.getDeploymentTypeInPro().getType());

        // Set if plan can be managed by user who is requesting information
        dto.setCanBeManagedByUser(manageValidationUtils.checkIfPlanCanBeManagedByUser(ivUser, deploymentPlan));

        // Release
        dto.setReleaseId(deploymentPlan.getReleaseVersion().getRelease().getId());
        dto.setReleaseName(deploymentPlan.getReleaseVersion().getRelease().getName());

        // Product UUAA
        dto.setUuaa(deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa());

        // ReleaseVersion.
        dto.setReleaseVersionId(deploymentPlan.getReleaseVersion().getId());
        dto.setReleaseVersionName(deploymentPlan.getReleaseVersion().getVersionName());

        // Configuration TodoTask information
        dto.setPendingConfiguration(buildDtoFromEntity(deploymentPlan.getConfigurationTask()));

        // Check if has pending tasks.
        dto.setHasPendingTasks(deploymentTaskRepository.planHasPendingDeploymentTasks(deploymentPlan.getId()));

        // Get the first of the pending tasks if any.
        dto.setFirstPendingTask(buildFirstPendingTask(deploymentPlan.getId()));

        // Get the pending action task if any.
        List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
        toDoTaskTypeList.add(ToDoTaskType.RELEASE_START);
        toDoTaskTypeList.add(ToDoTaskType.RELEASE_RESTART);
        toDoTaskTypeList.add(ToDoTaskType.RELEASE_STOP);

        dto.setPendingActionTask(this.buildPendingActionTask(deploymentPlan.getId(), toDoTaskTypeList));

        // Set the URL to the doc with the planning, not null only if type is PLANNED.
        dto.setPlanningDocUrl(deploymentPlan.getPlanningDocUrl());

        // Get the pending action task if any.
        dto.setPendingDeploymentTypeChangeTask(buildPendingDeploymentTypeChangeTask(deploymentPlan.getId()));

        // Get the pending configuration change task if any.
        dto.setPendingConfigurationChange(buildPendingConfigurationTask(deploymentPlan.getId()));

        // URL of the NOVA monitoring service for all products.
        dto.setMonitoringUrl(this.monitoringUtils.getMonitoringUrlForProducts(deploymentPlan));

        // Current Revision Id
        if (deploymentPlan.getCurrentRevision() != null)
        {
            dto.setCurrentRevision(deploymentPlan.getCurrentRevision().getId());
        }

        //Action
        if (deploymentPlan.getAction() != null)
        {
            dto.setAction(deploymentPlan.getAction().getAction());
        }

        dto.setMultiCPD(deploymentPlan.getMultiCPDInPro());

        if (deploymentPlan.getCpdInPro() != null)
        {
            dto.setProductionCPD(deploymentPlan.getCpdInPro().getName());
        }

        dto.setEtherNSDeploy(deploymentPlan.getEtherNs());
        dto.setSelectedDeploy(deploymentPlan.getSelectedDeploy().name());
        dto.setEtherNSLogging(deploymentPlan.getEtherNs());
        dto.setSelectedLogging(deploymentPlan.getSelectedLogging().name());

        // Atenea URL is established when:
        //   - Logging in ether is activated
        if (PlatformUtils.isPlanLoggingInEther(deploymentPlan))
        {
            final String ateneaLogUrl = PlatformUtils.buildAteneaLogURL(novaToolsProperties.getAtenea(), Environment.valueOf(deploymentPlan.getEnvironment()), dto.getEtherNSLogging(), dto.getReleaseName());
            dto.setAteneaUrl(ateneaLogUrl);
        }

        // Number of CPDs
        if (deploymentPlan.getMultiCPDInPro())
        {
            // TODO Revisar lógica de número de CPDs dependiendo del entorno.
            List<CPD> cpdList = this.cpdRepository.getActiveCpdByEnvironment(deploymentPlan.getEnvironment());
            if (cpdList == null)
            {
                dto.setNumCpds(0);
            }
            else
            {
                dto.setNumCpds(Math.toIntExact(cpdList.stream().map(CPD::getName).distinct().count()));
            }
        }
        else
        {
            dto.setNumCpds(1);
        }

        //Set alert info
        dto.setHasPendingAlerts(this.buildAlertInfoInformation(deploymentPlan));

        // Set Schedule Request info.
        dto.setScheduleRequest(this.buildScheduleRequest(deploymentPlan));

        // Set whether this plan is required to have an schedule request with its associated document.
        dto.setScheduleRequestAndDocumentRequired(this.isScheduleRequestAndDocumentRequired(deploymentPlan));

        return dto;
    }


    private Schedule buildDtoFromEntity(BatchServiceSchedule currentSchedule)
    {
        Schedule schedule = new Schedule();
        if (currentSchedule == null)
        {
            schedule.setActive(false);
        }
        else
        {
            schedule.setActive(currentSchedule.isActive());
            schedule.setId(currentSchedule.getId());
            schedule.setParameters(currentSchedule.getParameters());
            schedule.setSchedExpression(currentSchedule.getScheduleExpression());
        }
        return schedule;
    }


    /**
     * Builds a {@link PendingTask} from a {@link DeploymentTask}
     *
     * @param task - the {@code DeploymentTask}
     * @return a {@code PendingTask}  genereated from {@code DeploymentTask} entity
     */
    private PendingTask buildDtoFromEntity(DeploymentTask task)
    {
        PendingTask configTask = new PendingTask();
        configTask.setPending(false);

        if (task == null)
        {
            configTask.setMessage("No se ha creado ninguna solicitud hasta el momento");
        }
        else
        {
            configTask.setTaskId(task.getId());
            configTask.setTaskType(task.getTaskType().name());

            ToDoTaskStatus status = task.getStatus();
            if (status == ToDoTaskStatus.PENDING)
            {
                configTask.setPending(true);
                configTask.setMessage("Solicitud en proceso. Consulte la tarea " + task.getId());
            }
            else if (status == ToDoTaskStatus.PENDING_ERROR)
            {
                configTask.setPending(true);
                configTask.setMessage("La solicitud ha fallado durante el proceso. Aún sigue pendiente de solución. Consulte la tarea " + task.getId());
            }
            else
            {
                configTask.setPending(false);
                if (status == ToDoTaskStatus.DONE)
                {
                    configTask.setMessage("Solicitud aceptada. Consulte la tarea " + task.getId());
                }
                else
                {
                    configTask.setMessage("Solicitud rechazada. Por favor, consulte la tarea " + task.getId());
                }
            }
        }
        return configTask;
    }


    /**
     * Checks if a {@link DeploymentPlan} has any pending {@link DeploymentTask}
     * and if so return the first of them.
     *
     * @param planId {@link DeploymentPlan} ID.
     * @return A {@link PendingTask} with the first of them.
     */
    private PendingTask buildFirstPendingTask(int planId)
    {
        // Get all the pending tasks of the plan if any.
        List<DeploymentTask> pendingTasks = deploymentTaskRepository.planPendingDeploymentTasks(planId);

        if (pendingTasks.isEmpty())
        {
            return null;
        }

        // If there were any, get the first of them.
        DeploymentTask task = pendingTasks.get(0);

        return buildPendingTask(task);
    }


    /**
     * Gets a pending {@link ManagementActionTask} of a {@link DeploymentPlan}
     * component, if there is any.
     *
     * @param planId {@link DeploymentPlan} ID.
     * @return The {@link PendingTask} or null if there was none.
     */
    private PendingTask buildPendingActionTask(int planId, List<ToDoTaskType> toDoTaskTypeList)
    {
        PendingTask pendingTask = null;

        for (ToDoTaskType toDoTaskType : toDoTaskTypeList)
        {
            List<ManagementActionTask> managementActionTaskList = this.deploymentPlanRepository.getPendingActionTask(planId, toDoTaskType);

            if (!managementActionTaskList.isEmpty())
            {
                pendingTask = this.buildPendingTask(managementActionTaskList.get(0));
                break;
            }
        }
        return pendingTask;
    }


    /**
     * Gets a pending action task of type {@link DeploymentTypeChangeTask}
     * if there is any.
     *
     * @param planId {@link DeploymentPlan} ID.
     * @return The {@link PendingTask} or null if there was none.
     */
    private PendingTask buildPendingConfigurationTask(int planId)
    {
        // Get the pending action task, if any.
        List<DeploymentTask> configurationTaskList = deploymentTaskRepository.planPendingDeploymentTasks(planId);

        // Build the DTO.
        PendingTask pendingTask = null;
        if (!configurationTaskList.isEmpty())
        {
            pendingTask = this.buildPendingTaskFromToDoTask(configurationTaskList.get(0));
        }

        return pendingTask;
    }


    /**
     * Gets a pending action task of type {@link DeploymentTypeChangeTask}
     * if there is any.
     *
     * @param planId {@link DeploymentPlan} ID.
     * @return The {@link PendingTask} or null if there was none.
     */
    private PendingTask buildPendingDeploymentTypeChangeTask(int planId)
    {
        // Get the pending action task, if any.
        List<DeploymentTypeChangeTask> changeTaskList = deploymentPlanRepository.getPendingDeploymentTypeChangeTask(planId);

        // Build the DTO.
        PendingTask pendingTask = null;
        if (!changeTaskList.isEmpty())
        {
            pendingTask = this.buildPendingTaskFromToDoTask(changeTaskList.get(0));
        }
        return pendingTask;
    }


    /**
     * Builds a {@link PendingTask} from a {@link ToDoTask} of any type.
     *
     * @param task {@link ToDoTask}.
     * @return The {@link PendingTask}.
     */
    private PendingTask buildPendingTask(ToDoTask task)
    {
        PendingTask pendingTask = null;

        // Build the DTO.
        if (task != null)
        {
            pendingTask = new PendingTask();
            pendingTask.setTaskId(task.getId());
            pendingTask.setTaskType(task.getTaskType().name());
            pendingTask.setMessage(task.getDescription());
            pendingTask.setPending(true);
        }

        return pendingTask;
    }


    /**
     * @return The {@link PendingTask} or null if there was none.
     */
    private PendingTask buildPendingTaskFromToDoTask(ToDoTask todoTask)
    {
        PendingTask pendingTask = new PendingTask();
        pendingTask.setTaskType(todoTask.getTaskType().name());
        pendingTask.setPending(true);
        pendingTask.setMessage(todoTask.getDescription());
        pendingTask.setTaskId(todoTask.getId());
        return pendingTask;
    }

    /**
     * Builds an array of {@link DeploymentInstanceDto} creating one
     * for each {@link DeploymentInstance} related to the {@link DeploymentService} and set to {@link DeploymentServiceDto} param
     * <p>
     * At same time, it calculates the number of instances running, and create a InstanceNumber object to set to {@link DeploymentServiceDto}
     *
     * @param serviceDto {@link DeploymentServiceDto} to update
     * @param service    {@link DeploymentService} with data to dto
     */
    private void updateInstancesFromServiceToDto(final DeploymentServiceDto serviceDto, final DeploymentService service)
    {
        log.debug("[DeploymentPlanDtoBuilderImpl] -> [updateInstancesFromServiceToDto]: Updating DeploymentInstances of DeploymentService [{}]", service.getId());

        List<DeploymentInstanceDto> instanceDtos = new ArrayList<>();

        if (ServiceType.valueOf(service.getService().getServiceType()).isBatch())
        {
            serviceDto.setInstances(new DeploymentInstanceDto[]{});

            InstanceNumberDto instanceNumberDto = new InstanceNumberDto();
            instanceNumberDto.setTotal(0);
            instanceNumberDto.setRunning(0);
            serviceDto.setInstanceNumber(instanceNumberDto);
        }
        else
        {
            //number of instance running currently
            final AtomicInteger running = new AtomicInteger();

            service.getInstances().stream()
                    // Only not deleted instances.
                    .filter(deploymentInstance -> deploymentInstance.getDeletionDate() == null).forEach(deploymentInstance -> {
                        log.debug("[DeploymentPlanDtoBuilderImpl] -> [updateInstancesFromServiceToDto]: Updating DeploymentInstance [{}] of DeploymentService [{}]", deploymentInstance.getId(), service.getId());

                        DeploymentInstanceDto instance = new DeploymentInstanceDto();

                        // Copy properties.
                        instance.setId(deploymentInstance.getId());

                        if (deploymentInstance.getType() == DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA)
                        {
                            log.debug("[DeploymentPlanDtoBuilderImpl] -> [updateInstancesFromServiceToDto]: DeploymentInstance [{}] of DeploymentService [{}] is of Type [{}], casting it to [{}]", instance.getId(), service.getId(), DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA.name(), NovaDeploymentInstance.class.getSimpleName());

                            final NovaDeploymentInstance novaDeploymentInstance = HibernateUtils.getNovaDeploymentInstanceFromHibernateProxy(deploymentInstance);

                            instance.setClusterName(novaDeploymentInstance.getClusterName());
                            instance.setContainerId(novaDeploymentInstance.getContainerId());
                            instance.setContainerName(novaDeploymentInstance.getContainerName());
                            instance.setHostName(novaDeploymentInstance.getHostName());
                            instance.setPortMapping(novaDeploymentInstance.getPortMapping());

                            // URL of the NOVA monitoring service for this instance.
                            instance.setMonitoringUrl(this.monitoringUtils.getMonitoringUrlForInstance(service, novaDeploymentInstance));

                            final ReleaseVersionService releaseVersionService = service.getService();
                            if (ServiceType.valueOf(releaseVersionService.getServiceType()).isEPhoenix())
                            {
                                // URL of the NOVA monitoring service for this instance.
                                EphoenixService ephoenixService = service.getService().getEphoenixData();

                                StringBuilder url = new StringBuilder();
                                url.append("http://");
                                url.append(novaDeploymentInstance.getHostName());
                                if (!url.toString().contains(".igrupobbva:"))
                                {
                                    url.append(".igrupobbva:");
                                }
                                url.append(ephoenixService.getInstancePort());
                                url.append('/');
                                url.append(ephoenixService.getInstanceName());
                                url.append("/system/console/bundles");
                                instance.setEndpointUrl(url.toString());
                            }

                        }
                        else
                        {
                            instance.setMonitoringUrl(this.monitoringUtils.getMonitoringUrlForInstance(service));
                        }

                        //Action
                        if (deploymentInstance.getAction() != null)
                        {
                            instance.setAction(deploymentInstance.getAction().getAction());
                        }

                        // Get the pending action task if any.
                        List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
                        toDoTaskTypeList.add(ToDoTaskType.CONTAINER_START);
                        toDoTaskTypeList.add(ToDoTaskType.CONTAINER_STOP);
                        toDoTaskTypeList.add(ToDoTaskType.CONTAINER_RESTART);
                        instance.setPendingActionTask(this.buildPendingActionTask(deploymentInstance.getId(), toDoTaskTypeList));

                        if (deploymentInstance.getStarted() != null && deploymentInstance.getStarted())
                        {
                            running.getAndIncrement();
                        }

                        // Add the DTO.
                        instanceDtos.add(instance);

                        log.debug("[DeploymentPlanDtoBuilderImpl] -> [updateInstancesFromServiceToDto]: Updated DeploymentInstance [{}] of DeploymentService [{}]", deploymentInstance.getId(), service.getId());
                    });

            InstanceNumberDto instanceNumberDto = new InstanceNumberDto();
            instanceNumberDto.setTotal(instanceDtos.size());
            instanceNumberDto.setRunning(running.get());

            //set params
            serviceDto.setInstances(instanceDtos.toArray(DeploymentInstanceDto[]::new));
            serviceDto.setInstanceNumber(instanceNumberDto);

            log.debug("[DeploymentPlanDtoBuilderImpl] -> [updateInstancesFromServiceToDto]: Updated DeploymentInstances [{}] of DeploymentService [{}]", serviceDto.getInstances().length, service.getId());
        }
    }

    /**
     * Builds an array of {@link DeploymentServiceDto} creating one
     * for each {@link DeploymentService} related to the {@link DeploymentSubsystem}.
     *
     * @param subsystem {@link DeploymentSubsystem}
     * @return DeploymentServiceDto[]
     */
    private DeploymentServiceDto[] getServicesFromSubsystem(DeploymentSubsystem subsystem)
    {
        DeploymentServiceDto[] serviceDtos = new DeploymentServiceDto[subsystem.getDeploymentServices().size()];

        int i = 0;
        for (DeploymentService deploymentService : subsystem.getDeploymentServices())
        {
            DeploymentServiceDto dto = buildDtoFromEntity(deploymentService);
            // Add the DTO.
            serviceDtos[i] = dto;
            i++;
        }

        return serviceDtos;
    }

    /**
     * Builds an array of {@link DeploymentSubsystemDto} creating one
     * for each {@link DeploymentSubsystem} related to the {@link DeploymentPlan}.
     *
     * @param plan {@link DeploymentPlan}
     * @return DeploymentSubsystemDto[]
     */
    private DeploymentSubsystemDto[] getSubsystemsFromPlan(DeploymentPlan plan)
    {
        List<DeploymentSubsystemDto> subsystemDtos = new ArrayList<>();

        // For each plan subsytem:
        for (DeploymentSubsystem deploymentSubsystem : plan.getDeploymentSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(deploymentSubsystem.getSubsystem().getSubsystemId());

            DeploymentSubsystemDto dto = new DeploymentSubsystemDto();

            // Copy properties.
            dto.setId(deploymentSubsystem.getId());
            dto.setSubsystemId(subsystemDTO.getSubsystemId());
            dto.setSubsystemName(subsystemDTO.getSubsystemName());
            dto.setSubsystemType(subsystemDTO.getSubsystemType());
            dto.setSubsystemTag(deploymentSubsystem.getSubsystem().getTagName());
            //Action
            if (deploymentSubsystem.getAction() != null)
            {
                dto.setAction(deploymentSubsystem.getAction().getAction());
            }

            // Get the pending action task if any.
            List<ToDoTaskType> toDoTaskTypeList = new ArrayList<>();
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_START);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_STOP);
            toDoTaskTypeList.add(ToDoTaskType.SUBSYSTEM_RESTART);

            dto.setPendingActionTask(this.buildPendingActionTask(deploymentSubsystem.getId(), toDoTaskTypeList));

            // Add services from subsystem.
            DeploymentServiceDto[] deploymentServiceDtos = this.getServicesFromSubsystem(deploymentSubsystem);
            dto.setServices(deploymentServiceDtos);
            dto.setInstanceNumber(this.buildInstanceNumberByServices(deploymentServiceDtos));

            // Atenea URL is stablished when:
            //   - Logging in ether is activated
            if (PlatformUtils.isPlanLoggingInEther(plan))
            {
                // Atenea URL is stablished only for services: CDN, API REST, DAEMON or BATCH => Anything different from UNKNOWN
                final List<String> subsystemServiceNames = deploymentSubsystem.getDeploymentServices().stream()
                        // UNKNOWN services are filtered out
                        .filter(deploymentService -> deploymentUtils.getServiceType(deploymentService) != SimpleServiceType.UNKNOWN)
                        // Service name is retrieved
                        .map(deploymentService -> deploymentService.getService().getServiceName())
                        // The final result is a list of service names in the subsystem that are CDN, API REST, DAEMON or BATCH
                        .collect(Collectors.toList());

                final String ateneaLogUrl = PlatformUtils.buildAteneaLogURL(novaToolsProperties.getAtenea(), Environment.valueOf(plan.getEnvironment()), plan.getEtherNs(), plan.getReleaseVersion().getRelease().getName(), subsystemServiceNames.toArray(String[]::new));
                dto.setAteneaUrl(ateneaLogUrl);
            }

            // Add the DTO.
            subsystemDtos.add(dto);
        }

        return subsystemDtos.toArray(new DeploymentSubsystemDto[0]);
    }

    private void mapDtoSummary(DeploymentPlan plan, DeploymentSummaryDto dto, String ivUser)
    {
        dto.setId(plan.getId());
        if (plan.getExecutionDate() != null)
        {
            dto.setExecutionDate(plan.getExecutionDate().getTimeInMillis());
        }

        if (plan.getUndeploymentDate() != null)
        {
            dto.setUndeploymentDate(plan.getUndeploymentDate().getTimeInMillis());
        }
        dto.setStatus(plan.getStatus().getDeploymentStatus());
        dto.setEnvironment(plan.getEnvironment());
        if (plan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            dto.setGcsp(this.gcspDeploymentService.getGcspDto(plan, ivUser));
            dto.setNova(this.novaDeploymentService.getNovaDto(plan, ivUser));
        }
    }


    /**
     * Set ePhoenix endpoint
     *
     * @param deploymentService deployment service
     * @param dto               dto
     */
    private void setEphoenixEndpoint(DeploymentService deploymentService, DeploymentServiceDto dto)
    {
        StringBuilder url = new StringBuilder();
        EphoenixService ephoenixService = deploymentService.getService().getEphoenixData();
        if (deploymentService.getInstances().isEmpty())
        {
            log.debug("[DeploymentPlanDtoBuilderImpl] -> [setEphoenixEndpoint]: there is not deployed instaces for deploytemnt service id: [{}]:", deploymentService.getId());
        }
        else
        {

            final ReleaseVersionService rvs = deploymentService.getService();
            if (ServiceType.valueOf(rvs.getServiceType()).isEPhoenix())
            {
                Collections.sort((List<NovaDeploymentInstance>) (List<?>) deploymentService.getInstances(), new Comparator<NovaDeploymentInstance>()
                {
                    @Override
                    public int compare(NovaDeploymentInstance p1, NovaDeploymentInstance p2)
                    {
                        return p1.getHostName().compareTo(p2.getHostName());
                    }
                });
            }

            DeploymentInstance instance = deploymentService.getInstances().get(0);

            if (instance.getType() == DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA)
            {
                final NovaDeploymentInstance novaDeploymentInstance = HibernateUtils.getNovaDeploymentInstanceFromHibernateProxy(instance);

                url.append("http://");
                url.append(novaDeploymentInstance.getHostName());
                if (!url.toString().contains(".igrupobbva:"))
                {
                    url.append(".igrupobbva:");
                }
                url.append(ephoenixService.getInstancePort());
                url.append('/');
                url.append(ephoenixService.getInstanceName());
                url.append("/system/console/bundles");
                dto.setEndpointUrl(url.toString());
            }
        }
    }

    /**
     * Set FrontCat endpoint
     *
     * @param deploymentService deployment service
     * @param dto               dto
     */
    private void setFrontCatEndpoint(DeploymentService deploymentService, DeploymentServiceDto dto, String novaYmlContents)
    {
        DeploymentPlan plan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
        byte[] novaYmlFile = novaYmlContents.getBytes();

        Yaml yaml = new Yaml();
        Map<String, Object> map = new LinkedHashMap<>();

        if (novaYmlFile.length > 0)
        {
            try
            {
                String s = new String(novaYmlFile);
                s = s.replace("@", "");
                map.putAll(yaml.load(s));
                // Service MAP
                Map<String, Object> frontcatMap = (Map<String, Object>) map.get(Constants.FRONTCAT);

                // junction
                String junction = (String) frontcatMap.get("junction");

                // contextPath
                String contextPath = (String) frontcatMap.get("contextPath");

                switch (Environment.valueOf(plan.getEnvironment()))
                {
                    case INT:
                        dto.setEndpointUrl(intCloudGatewayBaseUrl + "/" + junction + "/" + contextPath);
                        break;
                    case PRE:
                        dto.setEndpointUrl(preCloudGatewayBaseUrl + "/" + junction + "/" + contextPath);
                        break;
                    case PRO:
                    default:
                        dto.setEndpointUrl(proCloudGatewayBaseUrl + "/" + junction + "/" + contextPath);
                        break;
                }
            }
            catch (YAMLException | NullPointerException e)
            {
                log.error("[DeploymentPlanDtoBuilderImpl] -> [setFrontCatEndpoint]: There was and error parsing nova.yml from project path");
                log.debug("[DeploymentPlanDtoBuilderImpl] -> [setFrontCatEndpoint]: Nova.yml parser exception: ", e);
            }
        }
    }


    /**
     * Set NOVA endpoint
     *
     * @param deploymentService deployment service
     * @param dto               dto
     * @param novaServiceName   service name
     */
    private void setNovaEndpoint(DeploymentService deploymentService, DeploymentServiceDto dto, String
            novaServiceName)
    {
        switch (Environment.valueOf(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment()))
        {
            case PRE:
                dto.setEndpointUrl(preBaseUrl + "/" + novaServiceName);
                break;
            case PRO:
                dto.setEndpointUrl(proBaseUrl + "/" + novaServiceName);
                break;
            case INT:
            default:
                dto.setEndpointUrl(intBaseUrl + "/" + novaServiceName);
                break;
        }
    }

    /**
     * Create an instance of InstanceNumberDto from list of services
     *
     * @param services array of service with number of instance per service
     * @return InstanceNumberDto
     */
    private InstanceNumberDto buildInstanceNumberByServices(final DeploymentServiceDto[] services)
    {
        InstanceNumberDto instanceNumberDto = new InstanceNumberDto();
        int total = 0;
        int running = 0;

        if (services != null)
        {
            for (DeploymentServiceDto service : services)
            {
                if (service.getInstanceNumber() == null)
                {
                    total += service.getNumberOfInstances();
                }
                else
                {
                    total += service.getInstanceNumber().getTotal();
                    running += service.getInstanceNumber().getRunning();
                }
            }
        }

        instanceNumberDto.setRunning(running);
        instanceNumberDto.setTotal(total);
        return instanceNumberDto;
    }

    /**
     * Build a DeploymentScheduleRequestDto from a DeploymentPlan entity.
     *
     * @param deploymentPlan The DeploymentPlan entity.
     * @return A DeploymentScheduleRequestDto.
     */
    private DeploymentScheduleRequestDto buildScheduleRequest(DeploymentPlan deploymentPlan)
    {
        ScheduleRequest[] scheduleRequestArray = this.scheduleControlMClient.getValidForDeployment(
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(),
                String.valueOf(deploymentPlan.getEnvironment()));
        if (scheduleRequestArray != null && scheduleRequestArray.length > 0)
        {
            ScheduleRequest scheduleRequest = scheduleRequestArray[0];
            if (scheduleRequest != null)
            {
                DeploymentScheduleRequestDto dto = new DeploymentScheduleRequestDto();
                dto.setScheduleRequestId(Math.toIntExact(scheduleRequest.getId()));
                if (scheduleRequest.getScheduleRequestDocument() != null)
                {
                    DeploymentScheduleRequestDocumentDto documentDto = new DeploymentScheduleRequestDocumentDto();
                    documentDto.setId(scheduleRequest.getScheduleRequestDocument().getId());
                    documentDto.setSystemName(scheduleRequest.getScheduleRequestDocument().getSystemName());
                    documentDto.setUrl(scheduleRequest.getScheduleRequestDocument().getUrl());
                    dto.setScheduleRequestDocument(documentDto);
                }
                return dto;
            }
        }
        return null;
    }

    /**
     * Set endpoint in dto passed as argument where application is going to be deployed, usually used for services that have a front application, cdns, frontcat, ePhoenix(felix instance url)
     *
     * @param deploymentService deploymentService
     * @param dto               dto to being fullfilled
     * @param service           service
     * @param novaServiceName   novaService
     */
    private void setEndpoint(final DeploymentService deploymentService, final DeploymentServiceDto dto, final ReleaseVersionService service, final String novaServiceName, final String novaYmlContents)
    {
        ServiceType serviceType = ServiceType.valueOf(service.getServiceType());

        if (serviceType.isEPhoenix())
        {
            this.setEphoenixEndpoint(deploymentService, dto);
        }
        else if (ServiceType.isFrontcat(serviceType))
        {
            this.setFrontCatEndpoint(deploymentService, dto, novaYmlContents);
        }
        else
        {
            this.setNovaEndpoint(deploymentService, dto, novaServiceName);
        }
    }

    /**
     * Get deploymentLogicalConnectorDTO array list
     *
     * @param logicalConnectorList list of logicalConector entities
     * @return DeploymentLogicalConnectorDto array
     */
    private DeploymentLogicalConnectorDto[] getDeploymentLogicalConnectorDtos(final List<LogicalConnector> logicalConnectorList)
    {
        DeploymentLogicalConnectorDto[] deploymentLogicalConnectorDtoArray = new DeploymentLogicalConnectorDto[logicalConnectorList.size()];
        IntStream.range(0, logicalConnectorList.size()).forEach(i -> {
            DeploymentLogicalConnectorDto deploymentLogicalConnectorDto = new DeploymentLogicalConnectorDto();
            LogicalConnector logicalConnector = logicalConnectorList.get(i);

            deploymentLogicalConnectorDto.setLogicalConnectorEnvironment(logicalConnector.getEnvironment());
            deploymentLogicalConnectorDto.setLogicalConnectorId(logicalConnector.getId());
            deploymentLogicalConnectorDto.setLogicalConnectorName(logicalConnector.getName());

            // Add the logical connector dto to  logical Connector Dto Array
            deploymentLogicalConnectorDtoArray[i] = deploymentLogicalConnectorDto;

            log.trace("[DeploymentPlanDtoBuilderImpl] -> [getDeploymentLogicalConnectorDtos]: added a new logical connector DTO to the logical connector dto list: [{}]", deploymentLogicalConnectorDto);
        });
        return deploymentLogicalConnectorDtoArray;
    }

    /**
     * Get deploymentServiceFilesystemDto array
     *
     * @param deploymentService deploymentService entity
     * @return array with all deploymentServciceFilesystemDTO used in this deploymentService
     */
    private DeploymentServiceFilesystemDto[] getDeploymentServiceFilesystemDtos(final DeploymentService deploymentService)
    {
        return deploymentService.getDeploymentServiceFilesystems().stream().map(deploymentServiceFilesystem -> {
            DeploymentServiceFilesystemDto deploymentServiceFilesystemDto = new DeploymentServiceFilesystemDto();
            deploymentServiceFilesystemDto.setVolumeBind(deploymentServiceFilesystem.getVolumeBind());
            deploymentServiceFilesystemDto.setFilesystemId(deploymentServiceFilesystem.getFilesystem().getId());
            deploymentServiceFilesystemDto.setFilesystemName(deploymentServiceFilesystem.getFilesystem().getName());
            deploymentServiceFilesystemDto.setFilesystemType(deploymentServiceFilesystem.getFilesystem().getType().name());
            if (deploymentServiceFilesystem.getFilesystem().getType().equals(FilesystemType.FILESYSTEM))
            {
                FilesystemNova filesystemNova = HibernateUtils.getFSNovaFromHibernateProxy(deploymentServiceFilesystem.getFilesystem());
                deploymentServiceFilesystemDto.setVolumeId(filesystemNova.getVolumeId());
            }
            return deploymentServiceFilesystemDto;
        }).toArray(DeploymentServiceFilesystemDto[]::new);
    }

    /**
     * Get jvm parameters
     *
     * @param deploymentService deploymentService
     * @return string with jvmparameters to set in deploymentServiceDTO
     */
    private String getJvmParameters(final DeploymentService deploymentService)
    {
        // If it has JVM parameters, copy them.
        return jdkParameterRepository.findByDeploymentService(deploymentService.getId()).stream()
                .map(JdkParameter::getName)
                .collect(Collectors.joining(" "));
    }

    /**
     * return list of serveApi Ids
     *
     * @param service service serving apis
     * @return the list of served apis ids
     */
    private List<Integer> getServeApiIds(final ReleaseVersionService service)
    {
        // apis
        List<Integer> ids = new ArrayList<>();
        for (ApiImplementation apiImplementation : service.getServers())
        {
            ids.add(apiImplementation.getApiVersion().getId());
        }
        return ids;
    }

    private boolean hasAsyncApiBackToBack(final ReleaseVersionService releaseVersionService)
    {
        return releaseVersionService.getApiImplementations().stream().anyMatch(apiImplementation -> ApiModality.ASYNC_BACKTOBACK.equals(apiImplementation.getApiModality()));
    }

    /**
     * Get JmxParametersDTO array
     *
     * @param deploymentService deploymentService entity
     * @return array with all JmxParametersDTO used in this deploymentService
     */
    private JmxParametersDTO[] getJmxParamsInstances(DeploymentService deploymentService)
    {
        JmxParametersDTO[] jmxDtos = new JmxParametersDTO[deploymentService.getNumberOfInstances()];

        int i = 0;
        for (DeploymentInstance instance : deploymentService.getInstances())
        {
            NovaDeploymentInstance novaDeploymentInstance = HibernateUtils.getNovaDeploymentInstanceFromHibernateProxy(instance);
            JmxParameter jmxParameter = jmxParameterRepository.findByDeploymentInstance(instance).orElseThrow(() -> new NovaException(BuildersError.getNoAvailableJmxParameter()));
            JmxParametersDTO dto = new JmxParametersDTO();
            dto.setContainerName(novaDeploymentInstance.getContainerName());
            dto.setHostName(novaDeploymentInstance.getHostIp());
            dto.setId(novaDeploymentInstance.getId());
            dto.setPassword(this.cipherCredentialService.decryptPassword(jmxParameter.getPassword()));
            dto.setUsername(jmxParameter.getUsername());
            dto.setPort(jmxParameter.getPort());
            // Add the DTO.
            jmxDtos[i] = dto;
            i++;
        }

        return jmxDtos;
    }
}
