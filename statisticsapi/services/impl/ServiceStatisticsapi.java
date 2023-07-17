package com.bbva.enoa.platformservices.coreservice.statisticsapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJobDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigsStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransfersInstancesStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBAvailabilityNovaCoinsDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBHardwareBudgetSnapshot;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductServiceDetailItem;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QASubsystemCodeAnalysis;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsapi.model.*;
import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.IRestHandlerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.IRestListenerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.client.feign.nova.rest.impl.RestHandlerStatisticsuserapi;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.TeamCountDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.TaskSummary;
import com.bbva.enoa.apirestgen.todotaskapi.model.TaskSummaryList;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.statistic.enumerates.StatisticParamName;
import com.bbva.enoa.datamodel.model.statistic.enumerates.StatisticType;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.enums.ServiceGroupingNames;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.ICategoryService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.enumerates.DeploymentInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.model.BrokerExportObject;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.services.interfaces.IServiceStatisticsapi;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.ServiceTypeGroupProvider;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.ValidDateRangeProvider;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.autoconfig.ExposeErrorCodeConfig;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants.*;
import static java.util.stream.Collectors.groupingBy;

/**
 * ------------------------------------------------
 *
 * @author NOVA
 * ------------------------------------------------
 */
@Service
public class ServiceStatisticsapi implements IServiceStatisticsapi
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceStatisticsapi.class);

    /**
     * The NOVA service name starts with group id and enoa
     */
    private static final String INITIAL_NOVA_GROUP_ID_NAME = "com.bbva.enoa.";

    /**
     * NOVA 9 release name
     **/
    private static final String NOVA_9_RELEASE_VERSION = "-nova9-";

    /**
     * Endpoint to get the error code of the service
     **/
    private static final String ACTUATOR_ERROR_CODES_ENDPOINT = "/actuator/errorcodes";

    /**
     * Attribute - Rest Handler - Interface
     */
    private final IRestHandlerStatisticsuserapi iRestHandlerStatisticsUserApi;

    /**
     * Expose error code config
     **/
    private final ExposeErrorCodeConfig exposeErrorCodeConfig;

    /**
     * Attribute - Statistics Repository
     */
    private final StatisticsRepository statisticsRepository;

    /**
     * Release Version repository
     */
    private final ReleaseVersionRepository releaseVersionRepository;

    /**
     * Product repository
     */
    private final ProductRepository productRepository;

    /**
     * Category Repository
     */
    private final CategoryRepository categoryRepository;

    /**
     * NOVA API Repository
     */
    private final ApiVersionRepository apiVersionRepository;

    /**
     * Logical Connector Repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Deployment Service Repository
     */
    private final DeploymentServiceRepository deploymentServiceRepository;

    /**
     * Deployment plan repository
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Filesystem repository
     */
    private final FilesystemRepository filesystemRepository;

    /**
     * Nova Deployment Instance Repository
     */
    private final NovaDeploymentInstanceRepository novaDeploymentInstanceRepository;

    /**
     * Ether Deployment Instance Repository
     */
    private final EtherDeploymentInstanceRepository etherDeploymentInstanceRepository;

    /**
     * Statistic Repository
     */
    private final StatisticRepository statisticRepository;

    /**
     * Broker Repository
     */
    private final BrokerRepository brokerRepository;
    /**
     * Eureka Discovery client
     */
    private final DiscoveryClient discoveryClient;
    /**
     * Tools Service client
     */
    private final IToolsClient toolsClient;
    /**
     * Users Service client
     */
    private final IUsersClient usersClient;
    /**
     * User Statistics Service client
     */
    private final IUserStatisticsClient userStatisticsClient;
    /**
     * Task Service Client
     */
    private final TodoTaskServiceClient todoTaskServiceClient;
    /**
     * Continuous Integration Client
     */
    private final IContinuousintegrationClient continuousintegrationClient;
    /**
     * Alert Service Client
     */
    private final IAlertServiceApiClient alertServiceApiClient;
    /**
     * Quality Assurance Client
     */
    private final IQualityAssuranceClient qualityAssuranceClient;
    /**
     * File Transfer Statistics Client
     */
    private final IFileTransferStatisticsClient fileTransferStatisticsClient;
    /**
     * Category Service
     */
    private final ICategoryService categoryService;
    /**
     * Product Budgets Service
     */
    private final IProductBudgetsService productBudgetsService;
    private final IBatchManagerClient batchManagerClient;
    private final ISchedulerManagerClient schedulerManagerClient;
    private final ValidDateRangeProvider validDateRangeProvider;
    /**
     * Attribute - Rest Handler - Implementation
     */
    private RestHandlerStatisticsuserapi restHandlerStatisticsUserApi;
    /**
     * Base URL of Zuul
     */
    @Value("${nova.mappings.baseUrl.zuul:http://localhost:35420}")
    private String zuulUrl;

    /**
     * Core Service error list stored in memory
     **/
    private List<NovaError> coreServiceErrorCodesList;

    /**
     * @param iRestHandlerProductbudgetsapi     IRestHandlerStatisticsuserapi dependency
     * @param statisticsRepository              statistics repository
     * @param releaseVersionRepository          Release Version repository
     * @param productRepository                 Product repository
     * @param categoryRepository                Category Repository
     * @param apiVersionRepository              Api Version repository
     * @param logicalConnectorRepository        Logical Connector Repository
     * @param deploymentPlanRepository          Deployment Plan Repository
     * @param deploymentServiceRepository       Deployment Service Repository
     * @param filesystemRepository              File System Repository
     * @param novaDeploymentInstanceRepository  Nova Deployment Instance Repository
     * @param etherDeploymentInstanceRepository Ether Deployment Instance Repository
     * @param statisticRepository               Statistic Repository
     * @param brokerRepository                  Broker Repository
     * @param exposeErrorCodeConfig             expose error code config
     * @param discoveryClient                   discovery client
     * @param toolsClient                       Tools Service client
     * @param usersClient                       Users Service client
     * @param userStatisticsClient              User Statistics Service client
     * @param todoTaskServiceClient             Task Service Client
     * @param continuousintegrationClient       Continuous Integration Client
     * @param alertServiceApiClient             Alert Service Client
     * @param qualityAssuranceClient            Quality Assurance Client
     * @param fileTransferStatisticsClient      File Transfer Statistics Client
     * @param categoryService                   Category Service
     * @param productBudgetsService             Product Budgets Service
     * @param batchManagerClient                Batch Manager Client
     * @param schedulerManagerClient            Scheduler Manager Client
     * @param validDateRangeProvider            Valid date range provider
     */
    @Autowired
    public ServiceStatisticsapi(final IRestHandlerStatisticsuserapi iRestHandlerProductbudgetsapi,
                                final StatisticsRepository statisticsRepository,
                                final ReleaseVersionRepository releaseVersionRepository,
                                final ProductRepository productRepository,
                                final CategoryRepository categoryRepository,
                                final ApiVersionRepository apiVersionRepository,
                                final LogicalConnectorRepository logicalConnectorRepository,
                                final DeploymentPlanRepository deploymentPlanRepository,
                                final DeploymentServiceRepository deploymentServiceRepository,
                                final FilesystemRepository filesystemRepository,
                                final NovaDeploymentInstanceRepository novaDeploymentInstanceRepository,
                                final EtherDeploymentInstanceRepository etherDeploymentInstanceRepository,
                                final StatisticRepository statisticRepository,
                                final BrokerRepository brokerRepository,
                                final ExposeErrorCodeConfig exposeErrorCodeConfig,
                                final DiscoveryClient discoveryClient,
                                final IToolsClient toolsClient,
                                final IUsersClient usersClient,
                                final IUserStatisticsClient userStatisticsClient,
                                final TodoTaskServiceClient todoTaskServiceClient,
                                final IContinuousintegrationClient continuousintegrationClient,
                                final IAlertServiceApiClient alertServiceApiClient,
                                final IQualityAssuranceClient qualityAssuranceClient,
                                final IFileTransferStatisticsClient fileTransferStatisticsClient,
                                final ICategoryService categoryService,
                                final IProductBudgetsService productBudgetsService,
                                final IBatchManagerClient batchManagerClient,
                                final ISchedulerManagerClient schedulerManagerClient,
                                final ValidDateRangeProvider validDateRangeProvider)
    {
        this.iRestHandlerStatisticsUserApi = iRestHandlerProductbudgetsapi;
        this.statisticsRepository = statisticsRepository;
        this.releaseVersionRepository = releaseVersionRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.apiVersionRepository = apiVersionRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.deploymentServiceRepository = deploymentServiceRepository;
        this.filesystemRepository = filesystemRepository;
        this.novaDeploymentInstanceRepository = novaDeploymentInstanceRepository;
        this.etherDeploymentInstanceRepository = etherDeploymentInstanceRepository;
        this.statisticRepository = statisticRepository;
        this.brokerRepository = brokerRepository;
        this.exposeErrorCodeConfig = exposeErrorCodeConfig;
        this.discoveryClient = discoveryClient;
        this.toolsClient = toolsClient;
        this.usersClient = usersClient;
        this.userStatisticsClient = userStatisticsClient;
        this.todoTaskServiceClient = todoTaskServiceClient;
        this.continuousintegrationClient = continuousintegrationClient;
        this.alertServiceApiClient = alertServiceApiClient;
        this.qualityAssuranceClient = qualityAssuranceClient;
        this.fileTransferStatisticsClient = fileTransferStatisticsClient;
        this.categoryService = categoryService;
        this.productBudgetsService = productBudgetsService;
        this.batchManagerClient = batchManagerClient;
        this.schedulerManagerClient = schedulerManagerClient;
        this.validDateRangeProvider = validDateRangeProvider;
    }

    /**
     * Bean initializer
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerStatisticsUserApi = new RestHandlerStatisticsuserapi(iRestHandlerStatisticsUserApi);
        // Get all NOVA Error codes catalog
        this.coreServiceErrorCodesList = this.exposeErrorCodeConfig.getErrorCodeMap();
    }
    /////////////////////////////// IMPLEMENTATIONS ///////////////////////////////////////////////

    @Override
    public Long getProductsNumber()
    {
        return this.statisticsRepository.getCountProductsDeployed();
    }

    @Override
    public Long getServicesNumber()
    {
        return this.statisticsRepository.getCountServicesDeployed();
    }

    @Override
    @Transactional
    public Long getApisNumberInPro()
    {
        return this.getApisSummary(Environment.PRO.name(), null, null).getTotal();
    }

    @Override
    public Long getUsersNumber()
    {
        AtomicReference<Long> response = new AtomicReference<>();
        this.restHandlerStatisticsUserApi.getUsersNumber(new IRestListenerStatisticsuserapi()
        {
            @Override
            public void getUsersNumber(Long outcome)
            {
                response.set(outcome);
                LOG.debug("[Statistic User Client API] -> [getUsersNumber]: got users number: [{}]", outcome);
            }

            @Override
            public void getUsersNumberErrors(Errors outcome)
            {
                throw new NovaException(StatisticsError.getUnexpectedError(), "[Statistic User Client API] -> [getUsersNumber]: Unable to get users number. Error message: [" + outcome.getMessages() + "]");
            }
        });
        return response.get();
    }

    @Override
    public String getErrorCodes()
    {
        // Nova Error list and String builder to return the catalog
        StringBuilder infoToReturn = new StringBuilder(StatisticsConstants.HEADER_ERROR_CODES_CATALOG);

        // Initially, we only add the core service error codes to the catalog.
        // Krypton9: Next version, we will add more error codes from other services
        List<NovaError> novaErrorList = new ArrayList<>(this.coreServiceErrorCodesList);
        LOG.debug("[ServiceStatisticsapi] -> [getErrorCodesCatalog]: added the following Core Service Error Code list: [{}]", this.coreServiceErrorCodesList);

        // Iterate by NOVA 9 Krypton Services for getting theirs NOVA Errors codes
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set(Constants.IV_USER_HEADER_KEY, Constants.IMMUSER);
        HttpEntity entity = new HttpEntity(headers);

        List<String> serviceNameList = this.discoveryClient.getServices();
        LOG.debug("[ServiceStatisticsapi] -> [getErrorCodesCatalog]: discovery client service name list: [{}]", serviceNameList);

        for (String serviceName : serviceNameList)
        {
            if (serviceName.contains(INITIAL_NOVA_GROUP_ID_NAME) && serviceName.contains(NOVA_9_RELEASE_VERSION))
            {
                ResponseEntity<NovaError[]> response;
                String resourceUrl = this.zuulUrl + "/" + serviceName + ACTUATOR_ERROR_CODES_ENDPOINT;
                try
                {
                    response = restTemplate.exchange(resourceUrl, HttpMethod.GET, entity, NovaError[].class);
                    if (response.getBody() != null)
                    {
                        novaErrorList.addAll(Arrays.asList(response.getBody()));
                        LOG.debug("[ServiceStatisticsapi] -> [getErrorCodesCatalog]: added the following NOVA Error list: [{}] from service name: [{}]", response.getBody(), serviceName);
                    }
                    else
                    {
                        LOG.warn("[ServiceStatisticsapi] -> [getErrorCodesCatalog]: the service name: [{}] does not have or cannot be added any NOVA Error code due to the body response is null. Endpoint Url: [{}]", serviceName, resourceUrl);
                    }
                }
                catch (RestClientException e)
                {
                    LOG.warn("[ServiceStatisticsapi] -> [getErrorCodesCatalog]: the service name: [{}] does not have or cannot be added any NOVA Error code. Endpoint Url: [{}] Error message: [{}]", serviceName, resourceUrl, e.getMessage());
                }
            }
        }

        // Order by error code ascending NOVA Error List and insert int CSV to return
        novaErrorList.sort(Comparator.comparing(NovaError::getErrorCode));
        for (NovaError novaError : novaErrorList)
        {
            infoToReturn.append(novaError.toStringCSVFormat()).append('\n');
        }

        LOG.debug("[ServiceStatisticsapi] -> [getErrorCodesCatalog]: generated the error codes CSV: [{}]", infoToReturn);
        return infoToReturn.toString();
    }

    @Override
    @Transactional
    public ReleaseVersionSummaryDTO getReleaseVersionsSummary(String uuaa, String platform, String releaseVersionStatus)
    {
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        boolean filterByReleaseVersionStatus = isParameterBeingFiltered(releaseVersionStatus);
        boolean filterByPlatform = isParameterBeingFiltered(platform);

        List<Object[]> releaseVersionsResultSet;

        if (filterByUuaa || filterByReleaseVersionStatus || filterByPlatform)
        {
            if (filterByUuaa)
            {
                if (filterByReleaseVersionStatus)
                {
                    if (filterByPlatform)
                    {
                        // Filtering by uuaa, release version status and platform
                        LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by UUAA [{}], status [{}] and Deploy Platfrom [{}]", uuaa, releaseVersionStatus, platform);
                        releaseVersionsResultSet = this.releaseVersionRepository.findAllByUuaaAndReleaseVersionStatusAndDeployPlatform(uuaa, releaseVersionStatus, platform);
                    }
                    else
                    {
                        // Filtering by UUAA + RV Status
                        LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by UUAA [{}] and status [{}]", uuaa, releaseVersionStatus);
                        releaseVersionsResultSet = this.releaseVersionRepository.findAllByUuaaAndReleaseVersionStatus(uuaa, releaseVersionStatus);
                    }
                }
                else if (filterByPlatform)
                {
                    // Filtering by UUAA+PLATFORM
                    LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by UUAA [{}] and Deploy Platfrom [{}]", uuaa, platform);
                    releaseVersionsResultSet = this.releaseVersionRepository.findAllByUuaaAndDeployPlatform(uuaa, platform);
                }
                else
                {
                    //Filtering only by UUAA
                    LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by UUAA [{}]", uuaa);
                    releaseVersionsResultSet = this.releaseVersionRepository.findNotStoragedByUuaa(uuaa);
                }

            }
            else if (filterByReleaseVersionStatus)
            {
                if (filterByPlatform)
                {
                    // Filtering by RV+PLATFORM
                    LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by Release Version Status [{}] and Deploy Platfrom [{}]", releaseVersionStatus, platform);
                    releaseVersionsResultSet = this.releaseVersionRepository.findAllByReleaseVersionStatusAndDeployPlatform(releaseVersionStatus, platform);
                }
                else
                {
                    //Filtering by RV Status
                    LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by status [{}]", releaseVersionStatus);
                    releaseVersionsResultSet = this.releaseVersionRepository.findByReleaseVersionStatus(releaseVersionStatus);
                }
            }
            else
            {
                // Filtering by PLATFORM
                LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: filtering by Deploy Platform [{}]", platform);
                releaseVersionsResultSet = this.releaseVersionRepository.findByPlatform(platform);
            }
        }
        else
        {
            //Don't filter, i.e., return all
            LOG.debug("[ServiceStatisticsapi] -> [getReleaseVersionsSummary]: Returning all releaseVersionsResultSet");
            releaseVersionsResultSet = this.releaseVersionRepository.findAllNotStoragedElements();
        }

        return this.mapFromObjectsArrayToReleaseVersionSummaryDTO(releaseVersionsResultSet);
    }

    @Override
    @Transactional
    public byte[] getReleaseVersionsSummaryExport(final String uuaa, final String platform, final String status, final String format)
    {
        // get release version summary
        final ReleaseVersionSummaryDTO releaseVersionsResultSet = this.getReleaseVersionsSummary(uuaa, platform, status);

        final List<String[]> exportData = Arrays.stream(releaseVersionsResultSet.getServices())
                .map(release -> new String[]{release.getServiceType(), String.valueOf(release.getTotal())})
                .collect(Collectors.toList());

        // export to file
        return ExportDataUtils.exportValuesTo(format, new String[]{"SERVICE", "TOTAL"}, exportData);
    }

    @Override
    @Transactional
    public SubsystemSummaryDTO getSubsystemsSummary(final String uuaa)
    {
        List<TOSubsystemDTO> subsystemDTOS;
        // Filter by UUAA
        if (isParameterBeingFiltered(uuaa))
        {
            LOG.debug("[ServiceStatisticsapi] -> [getSubsystemsSummary]: filtering by UUAA [{}]", uuaa);
            List<Product> productList = this.productRepository.findByUuaa(uuaa.toUpperCase());
            if (productList.size() == 0)
            {
                LOG.debug("[ServiceStatisticsapi] -> [getSubsystemsSummary]: Returning empty result");
                subsystemDTOS = new ArrayList<>();
            }
            else if (productList.size() > 1)
            {
                LOG.error("[ServiceStatisticsapi] -> [getSubsystemsSummary]: There is more than one product with UUAA [{}]: {}", uuaa, productList);
                throw new NovaException(StatisticsError.getUaaaNotUniqueError(uuaa, productList));
            }
            else
            {
                subsystemDTOS = this.toolsClient.getProductSubsystems(productList.get(0).getId(), null);
            }
        }
        // Don't filter, i.e., return all
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getSubsystemsSummary]: Returning all results");
            subsystemDTOS = this.toolsClient.getAllSubsystems();
        }
        return this.mapSubsystemDTOsToSubsystemSummaryDTO(subsystemDTOS);
    }

    @Override
    public byte[] getSubsystemsSummaryExport(final String uuaa, final String format)
    {
        // get subsystem
        final SubsystemSummaryDTO subsystemDTOS = this.getSubsystemsSummary(uuaa);

        final List<String[]> subsystemList = Arrays.stream(subsystemDTOS.getSubsystems())
                .map(subsystem -> new String[]{subsystem.getSubsystemType(), String.valueOf(subsystem.getTotal())})
                .collect(Collectors.toList());

        // export to file
        return ExportDataUtils.exportValuesTo(format, new String[]{"TYPE", "TOTAL"}, subsystemList);
    }

    @Override
    public UserSummaryDTO getUsersSummary(String uuaa)
    {
        List<USUserDTO> userDTOS;
        // Filter by UUAA
        if (isParameterBeingFiltered(uuaa))
        {
            LOG.debug("[ServiceStatisticsapi] -> [getUsersSummary]: filtering by UUAA [{}]", uuaa);
            List<Product> productList = this.productRepository.findByUuaa(uuaa.toUpperCase());
            if (productList.isEmpty())
            {
                LOG.debug("[ServiceStatisticsapi] -> [getUsersSummary]: Returning empty result");
                userDTOS = new ArrayList<>();
            }
            else if (productList.size() > 1)
            {
                LOG.error("[ServiceStatisticsapi] -> [getUsersSummary]: There is more than one product with UUAA [{}]: {}", uuaa, productList);
                throw new NovaException(StatisticsError.getUaaaNotUniqueError(uuaa, productList));
            }
            else
            {
                userDTOS = this.usersClient.getProductMembers(productList.get(0).getId(), new NovaException(StatisticsError.getUserServiceError()));
            }
            return this.mapUserDTOsToUserSummaryDTO(userDTOS);
        }
        // Don't filter, i.e., return all
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getUsersSummary]: Returning all results");
            UserSummaryDTO userSummaryDTO = new UserSummaryDTO();
            // Set the total of Users
            userSummaryDTO.setTotal(this.getUsersNumber());
            // Set the Teams
            userSummaryDTO.setTeams(this.mapTeamCountDTOsToTeamDTOs(this.userStatisticsClient.countUsersInTeams()));
            return userSummaryDTO;
        }

    }

    @Override
    public byte[] getUsersSummaryExport(final String uuaa, final String format)
    {
        List<String[]> userList;
        if (isParameterBeingFiltered(uuaa))
        {
            userList = this.getUsersInfo(uuaa).stream()
                    .map(user -> new String[]{user.getUserCode(), user.getUserName(), user.getSurname1(), user.getSurname2(),
                            user.getEmail(), Arrays.toString(user.getTeams()), user.getActive().toString()})
                    .collect(Collectors.toList());
        }
        else
        {
            userList = Arrays.stream(userStatisticsClient.getProductUsers())
                    .map(user -> new String[]{user.getUserCode(), user.getUserName(), user.getSurname1(), user.getSurname2(),
                            user.getEmail(), Arrays.toString(user.getTeams()), user.getActive().toString()})
                    .collect(Collectors.toList());

        }

        return ExportDataUtils.exportValuesTo(
                format, new String[]{"USER CODE", "NAME", "SURNAME 1", "SURNAME 2", "EMAIL", "TEAMS", "ACTIVE"}, userList);
    }

    @Override
    public TODOTaskSummaryDTO getTodotasksSummary(String uuaa, String status)
    {
        // If uuaa = ALL or NOT_ASSIGNED, set productId with the same value as uuaa.
        // Otherwise, set productId with the Product ID corresponding to the UUAA.
        String productId = uuaa;
        if (isParameterBeingFiltered(uuaa))
        {
            if (!uuaa.equals(StatisticsConstants.FILTER_BY_NULL_PRODUCT_PARAMETER))
            {
                List<Product> productList = this.productRepository.findByUuaa(uuaa.toUpperCase());
                if (productList.size() == 0)
                {
                    LOG.debug("[ServiceStatisticsapi] -> [getTodotasksSummary]: Returning empty result");
                    TODOTaskSummaryDTO todoTaskSummaryDTO = new TODOTaskSummaryDTO();
                    todoTaskSummaryDTO.setTotal(0L);
                    todoTaskSummaryDTO.setTasksByType(new TaskTypeDTO[]{});
                    todoTaskSummaryDTO.setTasksByTeam(new TaskTeamDTO[]{});
                    return todoTaskSummaryDTO;
                }
                else if (productList.size() > 1)
                {
                    LOG.error("[ServiceStatisticsapi] -> [getTodotasksSummary]: There is more than one product with UUAA [{}]: {}", uuaa, productList);
                    throw new NovaException(StatisticsError.getUaaaNotUniqueError(uuaa, productList));
                }
                else
                {
                    productId = String.valueOf(productList.get(0).getId());
                }
            }
        }
        return this.mapTaskListToTODOTaskSummaryDTO(this.todoTaskServiceClient.getTodoTasksSinceDaysAgo(StatisticsConstants.SINCE_DAYS_AGO, productId, status));
    }

    @Override
    public byte[] getTodotasksSummaryExport(final String uuaa, final String status, final String format)
    {
        final TODOTaskSummaryDTO taskSummaryDTO = this.getTodotasksSummary(uuaa, status);

        final List<String[]> taskSummaryList = Arrays.stream(taskSummaryDTO.getTasksByTeam())
                .map(team -> new String[]{team.getTeam(), String.valueOf(team.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"TEAM", "TOTAL"}, taskSummaryList);
    }

    @Override
    public ProductSummaryDTO getProductsSummary()
    {
        List<Product> products = this.productRepository.findAll();

        Long libraryCount = products.stream().filter(product -> StatisticsConstants.LIBRARY.equals(product.getType())).count();
        Long novaCount = products.stream().filter(product -> StatisticsConstants.NOVA.equals(product.getType())).count();

        ProductSummaryDTO productSummaryDTO = new ProductSummaryDTO();

        if (products.isEmpty())
        {
            productSummaryDTO.setLibraries(0L);
            productSummaryDTO.setNovaProducts(0L);
            productSummaryDTO.setTotal(0L);
        }
        else
        {
            productSummaryDTO.setLibraries(libraryCount);
            productSummaryDTO.setNovaProducts(novaCount);
            productSummaryDTO.setTotal((long) products.size());
        }

        return productSummaryDTO;
    }

    @Override
    public byte[] getProductsSummaryExport(final String format)
    {
        final ProductSummaryDTO productSummary = getProductsSummary();

        return ExportDataUtils.exportValuesTo(format, new String[]{"TYPE", "TOTAL"}, Arrays.asList(new String[]{"PRODUCTS", String.valueOf(productSummary.getNovaProducts())},
                new String[]{"LIBRARIES", String.valueOf(productSummary.getLibraries())}));
    }

    @Override
    public BuildJobSummaryDTO getBuildJobsSummary(String jobType, String uuaa)
    {
        return this.mapJobDTOsToBuildJobSummaryDTO(this.continuousintegrationClient.getJobsSinceDaysAgo(StatisticsConstants.SINCE_DAYS_AGO, jobType, uuaa));
    }

    @Override
    public byte[] getBuildJobsSummaryExport(final String jobType, final String uuaa, final String format)
    {
        final BuildJobSummaryDTO buildJobSummaryDTO = this.getBuildJobsSummary(jobType, uuaa);

        final List<String[]> jobStatus = Arrays.stream(buildJobSummaryDTO.getJobStatuses())
                .map(status -> new String[]{status.getStatus(), String.valueOf(status.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, jobStatus);
    }

    @Override
    @Transactional
    public CategorySummaryDTO getCategoriesSummary(String categoryName)
    {
        // The best solution would be to use "Regex" keyword in a repository derived query: findDistinctByCategoriesNameRegex, but
        // it's not supported in our current version of Spring Data JPA (2.2.6.RELEASE). See method build in JpaQueryCreator.PredicateBuilder
        // class in spring-data-jpa dependency.
        // Another solution would be to use a "Query by Example" with a regex() matcher, e.g.:
        //   Category category = new Category();
        //   category.setName("(?i)^(Amb|Ambito|Ámbito|Dom|Dominio|Geografía|Subdom|Sub-dominio):.*"); // Not tested.
        //   ExampleMatcher matcher = ExampleMatcher.matching()
        //           .withMatcher("name", regex());

        //   Example<Category> example = Example.of(category, matcher);
        //   List<Category> categories = categoryRepository.findAll(example);
        // But it's also not supported. See method getPredicates in QueryByExamplePredicateBuilder class in spring-data-jpa dependency.
        if (categoryName == null || categoryName.isEmpty())
        {
            String[] functionalCategories = this.categoryService.getAllCategories(null, true);
            return this.mapProductsToCategorySummaryDTO(this.productRepository.findDistinctByCategoriesNameIn(Arrays.asList(functionalCategories)), null);
        }
        else
        {
            return this.mapProductsToCategorySummaryDTO(this.productRepository.findDistinctByCategoriesName(categoryName), this.categoryRepository.findByName(categoryName));
        }
    }

    @Override
    public byte[] getCategoriesSummaryExport(String categoryName, final String format)
    {
        // convert ALL to NULL
        categoryName = convertFilterToNull(categoryName);

        final CategorySummaryDTO categorySummaryDTO = this.getCategoriesSummary(categoryName);

        final List<String[]> categories = Arrays.stream(categorySummaryDTO.getCategories())
                .map(category -> new String[]{category.getCategory(), String.valueOf(category.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"CATEGORY", "TOTAL"}, categories);
    }

    @Override
    @Transactional
    public ApiSummaryDTO getApisSummary(String environment, String functionality, String uuaa)
    {
        // Treat 'ALL' filter as if it was null.
        uuaa = this.convertFilterToNull(uuaa);
        functionality = this.convertFilterToNull(functionality);
        environment = this.convertFilterToNull(environment);

        List<Object[]> apiVersions = this.apiVersionRepository.findAllApisSummary(uuaa, environment, functionality);

        return this.mapApisSummary(apiVersions);
    }

    @Override
    public byte[] getApisSummaryExport(final String environment, final String uuaa, final String format)
    {

        final ApiSummaryDTO apiSummaryDTO = this.getApisSummary(environment, null, uuaa);

        final List<String[]> apiList = Arrays.stream(apiSummaryDTO.getApis())
                .map(api -> new String[]{api.getApiType(), String.valueOf(api.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"API", "TOTAL"}, apiList);
    }

    @Override
    public QualityAnalysesSummaryDTO getQualityAnalysesSummary()
    {
        return this.mapQASubsystemCodeAnalysesToQualityAnalysesSummaryDTOs(this.qualityAssuranceClient.getSubsystemCodeAnalysesSinceDaysAgo(StatisticsConstants.SINCE_DAYS_AGO, StatisticsConstants.SUBSYSTEM_CODE_ANALYSES_STATUS));
    }

    @Override
    public byte[] getQualityAnalysesSummaryExport(final String format)
    {
        final QualityAnalysesSummaryDTO qualityAnalysesSummary = this.getQualityAnalysesSummary();

        final List<String[]> statusList = Arrays.asList(new String[]{"OK", String.valueOf(qualityAnalysesSummary.getNumAnalysesOK())},
                new String[]{"KO", String.valueOf(qualityAnalysesSummary.getNumAnalysesKO())});

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, statusList);
    }

    @Override
    public BudgetSummaryDTO getBudgetsSummary(String uuaa, String status)
    {
        String productId = null;
        Product product = this.productRepository.findByUuaaIgnoreCase(uuaa);
        if (product != null)
        {
            productId = String.valueOf(product.getId());
        }
        return this.mapProductServiceDetailItemsToBudgetSummaryDTO(this.productBudgetsService.getProductServicesDetail(productId, status));
    }

    @Override
    public byte[] getBudgetsSummaryExport(final String uuaa, final String status, final String format)
    {
        final BudgetSummaryDTO budgetSummaryDTO = this.getBudgetsSummary(uuaa, status);

        final List<String[]> budgetList = Arrays.stream(budgetSummaryDTO.getTeams())
                .map(team -> new String[]{team.getServiceType(), String.valueOf(team.getTotalNovaCoins())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"TYPE", "TOTAL"}, budgetList);
    }

    @Override
    @Transactional
    public ConnectorsSummaryDTO getConnectorsSummary(String environment, String uuaa, String status)
    {
        // 1. Check which filters are being applied.

        boolean filterByEnvironment = isParameterBeingFiltered(environment);
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        boolean filterByStatus = isParameterBeingFiltered(status);

        // 2. Convert input parameters to the types required by the repositories.

        com.bbva.enoa.core.novabootstarter.enumerate.Environment environmentEnum = null;
        if (filterByEnvironment)
        {
            environmentEnum = mapEnvironmentStringToDatamodelEnvironment(environment);
        }

        Integer productId = null;
        if (filterByUuaa)
        {
            List<Product> products = this.productRepository.findByUuaa(uuaa.toUpperCase());
            if (products.size() == 0)
            {
                LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: Returning empty result");
                return this.mapLogicalConnectorsToConnectorsSummaryDTO(new ArrayList<>());
            }
            else if (products.size() > 1)
            {
                LOG.error("[ServiceStatisticsapi] -> [getConnectorsSummary]: There is more than one product with UUAA [{}]: {}", uuaa, products);
                throw new NovaException(StatisticsError.getUaaaNotUniqueError(uuaa, products));
            }
            else
            {
                productId = products.get(0).getId();
            }
        }

        LogicalConnectorStatus statusEnum = null;
        if (filterByStatus)
        {
            statusEnum = this.mapLogicalConnectorStatusStringToEnum(status);
        }

        // 3. Find the entities using the repositories.
        List<LogicalConnector> logicalConnectors;
        // No filter
        if (!filterByEnvironment && !filterByUuaa && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: returning all results");
            logicalConnectors = this.logicalConnectorRepository.findAll();
        }
        // Filtered by status
        else if (!filterByEnvironment && !filterByUuaa)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by status [{}]", statusEnum);
            logicalConnectors = this.logicalConnectorRepository.findByLogicalConnectorStatus(statusEnum);
        }
        // Filtered by uuaa
        else if (!filterByEnvironment && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by Product ID [{}]", productId);
            logicalConnectors = this.logicalConnectorRepository.findByProductId(productId);
        }
        // Filtered by uuaa and status
        else if (!filterByEnvironment)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by Product ID [{}] and status [{}]", productId, statusEnum);
            logicalConnectors = this.logicalConnectorRepository.findByProductIdAndLogicalConnectorStatus(productId, statusEnum);
        }
        // Filtered by environment
        else if (!filterByUuaa && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by environment [{}]", environmentEnum);
            logicalConnectors = this.logicalConnectorRepository.findByEnvironment(environmentEnum.getEnvironment());
        }
        // Filtered by environment and status
        else if (!filterByUuaa)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by environment [{}] and status [{}]", environmentEnum, statusEnum);
            logicalConnectors = this.logicalConnectorRepository.findByEnvironmentAndLogicalConnectorStatus(environmentEnum.getEnvironment(), statusEnum);
        }
        // Filtered by uuaa and environment
        else if (!filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by environment [{}] and Product ID [{}]", environmentEnum, productId);
            logicalConnectors = this.logicalConnectorRepository.findByProductIdAndEnvironment(productId, environmentEnum.getEnvironment());
        }
        // Filtered by environment, uuaa and status
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getConnectorsSummary]: filtering by environment [{}], Product ID [{}] and status [{}]", environmentEnum, productId, statusEnum);
            logicalConnectors = this.logicalConnectorRepository.findByEnvironmentAndProductIdAndLogicalConnectorStatus(environmentEnum.getEnvironment(), productId, statusEnum);
        }

        // 4. Convert the entities to the DTO.
        return this.mapLogicalConnectorsToConnectorsSummaryDTO(logicalConnectors);
    }

    @Override
    public byte[] getConnectorsSummaryExport(final String environment, final String uuaa, final String status, final String format)
    {
        final ConnectorsSummaryDTO connectorsSummaryDTO = this.getConnectorsSummary(environment, uuaa, status);

        final List<String[]> connectorList = Arrays.stream(connectorsSummaryDTO.getElements())
                .map(connector -> new String[]{connector.getConnectorType(), String.valueOf(connector.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"CONNECTORS", "TOTAL"}, connectorList);
    }

    @Override
    @Transactional
    public ServicesSummaryDTO getServicesSummary(String environment, String uuaa, String platform)
    {
        // 1. Treat 'ALL' filter as it it was null.
        environment = this.convertFilterToNull(environment);
        uuaa = this.convertFilterToNull(uuaa);
        platform = this.convertFilterToNull(platform);

        // 2. Find the results using the repositories and convert them to the summary DTO.
        return this.mapDeploymentServicesCountToServicesSummaryDTO(this.deploymentServiceRepository.countStatusesFilteringByDeployed(environment, uuaa, platform));
    }

    @Override
    public byte[] getServicesSummaryExport(final String environment, final String uuaa, final String platform, final String format)
    {
        final ServicesSummaryDTO services = this.getServicesSummary(environment, uuaa, platform);

        final List<String[]> serviceList = Arrays.stream(services.getElements())
                .map(service -> new String[]{service.getServiceType(), String.valueOf(service.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"SERVICES", "TOTAL"}, serviceList);
    }

    @Override
    @Transactional
    public DeploymentPlansSummaryDTO getDeploymentPlansSummary(String environment, String uuaa, String platform)
    {
        // 1. Treat 'ALL' filter as it it was null.
        environment = this.convertFilterToNull(environment);
        uuaa = this.convertFilterToNull(uuaa);
        platform = this.convertFilterToNull(platform);

        // 2. Find the results using the repositories and convert them to the summary DTO.
        return this.mapDeploymentPlansCountToDeploymentPlansSummaryDTO(this.deploymentPlanRepository.countStatuses(environment, uuaa, platform));
    }

    @Override
    public byte[] getDeploymentPlansSummaryExport(final String environment, final String uuaa, final String platform, final String format)
    {
        final DeploymentPlansSummaryDTO plansSummaryDTO = this.getDeploymentPlansSummary(environment, uuaa, platform);

        final List<String[]> deploymentPlanList = Arrays.stream(plansSummaryDTO.getElements())
                .map(deployment -> new String[]{deployment.getStatus(), String.valueOf(deployment.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, deploymentPlanList);
    }

    @Override
    public AlertsSummaryDTO getAlertsSummary(String environment, String type, String uuaa)
    {
        return this.mapASBasicAlertInfoDTOsToAlertsSummaryDTO(this.alertServiceApiClient.getProductAlertsSinceDaysAgo(StatisticsConstants.SINCE_DAYS_AGO, environment, type, uuaa));
    }

    @Override
    public byte[] getAlertsSummaryExport(final String environment, final String type, final String uuaa, final String format)
    {
        final AlertsSummaryDTO alertsSummaryDTO = this.getAlertsSummary(environment, type, uuaa);

        final List<String[]> alertList = Arrays.stream(alertsSummaryDTO.getElements())
                .map(alert -> new String[]{alert.getStatus(), String.valueOf(alert.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, alertList);
    }

    @Override
    @Transactional(readOnly = true)
    public FilesystemsSummaryDTO getFilesystemsSummary(String environment, String uuaa, String status)
    {
        // 1. Check which filters are being applied.

        boolean filterByEnvironment = isParameterBeingFiltered(environment);
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        boolean filterByStatus = isParameterBeingFiltered(status);

        // 2. Convert input parameters to the types required by the repositories.

        com.bbva.enoa.core.novabootstarter.enumerate.Environment environmentEnum = null;
        if (filterByEnvironment)
        {
            environmentEnum = mapEnvironmentStringToDatamodelEnvironment(environment);
        }

        FilesystemStatus statusEnum = null;
        if (filterByStatus)
        {
            statusEnum = mapFilesystemStatusStringToFilesystemStatusEnum(status);
        }

        // 3. Find the entities using the repositories.
        List<Filesystem> filesystemList;
        // No filter
        if (!filterByEnvironment && !filterByUuaa && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: returning all results");
            filesystemList = this.filesystemRepository.findAll();
        }
        // Filtered by status
        else if (!filterByEnvironment && !filterByUuaa)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: filtering by status [{}]", statusEnum);
            filesystemList = this.filesystemRepository.findByFilesystemStatus(statusEnum);
        }
        // Filtered by uuaa
        else if (!filterByEnvironment && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: filtering by UUAA [{}]", uuaa);
            filesystemList = this.filesystemRepository.findByProductUuaa(uuaa);
        }
        // Filtered by uuaa and status
        else if (!filterByEnvironment)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: filtering by UUAA [{}] and status [{}]", uuaa, statusEnum);
            filesystemList = this.filesystemRepository.findByProductUuaaAndFilesystemStatus(uuaa, statusEnum);
        }
        // Filtered by environment
        else if (!filterByUuaa && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: filtering by environment [{}]", environmentEnum);
            filesystemList = this.filesystemRepository.findByEnvironment(environmentEnum.getEnvironment());
        }
        // Filtered by environment and status
        else if (!filterByUuaa)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: filtering by environment [{}] and status [{}]", environmentEnum, statusEnum);
            filesystemList = this.filesystemRepository.findByEnvironmentAndFilesystemStatus(environmentEnum.getEnvironment(), statusEnum);
        }
        // Filtered by environment and uuaa
        else if (!filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getFilesystemsSummary]: filtering by environment [{}] and UUAA [{}]", environmentEnum, uuaa);
            filesystemList = this.filesystemRepository.findByEnvironmentAndProductUuaa(environmentEnum.getEnvironment(), uuaa);
        }
        // Filtered by environment, uuaa and status
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getDeploymentPlansSummary]: filtering by environment [{}], UUAA [{}] and status [{}]", environmentEnum, uuaa, statusEnum);
            filesystemList = this.filesystemRepository.findByEnvironmentAndProductUuaaAndFilesystemStatus(environmentEnum.getEnvironment(), uuaa, statusEnum);
        }

        // 4. Convert the entities to the DTO.
        return this.mapFilesystemListToFilesystemsSummaryDTO(filesystemList);
    }

    @Override
    public byte[] getFilesystemsSummaryExport(final String environment, final String uuaa, final String status, final String format)
    {
        final FilesystemsSummaryDTO filesystemsSummaryDTO = this.getFilesystemsSummary(environment, uuaa, status);

        final List<String[]> filesystemList = Arrays.stream(filesystemsSummaryDTO.getElements())
                .map(filesystem -> new String[]{filesystem.getFilesystemType(), String.valueOf(filesystem.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"TYPE", "TOTAL"}, filesystemList);
    }

    @Override
    public FileTransfersSummaryDTO getFileTransfersSummary(String environment, String uuaa)
    {
        return this.mapFTMFileTransferConfigsStatisticsSummaryDTOToFileTransfersSummaryDTO(this.fileTransferStatisticsClient.getFileTransferConfigsSummary(environment, uuaa));
    }

    @Override
    public byte[] getFileTransfersSummaryExport(final String environment, final String uuaa, final String format)
    {
        final FileTransfersSummaryDTO fileTransfersSummaryDTO = this.getFileTransfersSummary(environment, uuaa);

        final List<String[]> fileTransferList = Arrays.stream(fileTransfersSummaryDTO.getElements())
                .map(filesystem -> new String[]{filesystem.getStatus(), String.valueOf(filesystem.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, fileTransferList);
    }

    @Override
    public FileTransfersInstancesSummaryDTO getFileTransfersInstancesSummary(String environment, String uuaa)
    {
        return this.mapFTMFileTransferInstancesStatisticsSummaryDTOToFileTransfersSummaryDTO(this.fileTransferStatisticsClient.getFileTransferInstancesSummary(environment, uuaa));
    }

    @Override
    public byte[] getFileTransfersInstancesSummaryExport(final String environment, final String uuaa, final String format)
    {
        final FileTransfersInstancesSummaryDTO instancesSummaryDTO = this.getFileTransfersInstancesSummary(environment, uuaa);

        final List<String[]> fileTransferList = Arrays.stream(instancesSummaryDTO.getStatuses())
                .map(filesystem -> new String[]{filesystem.getStatus(), String.valueOf(filesystem.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, fileTransferList);
    }

    @Override
    public BatchExecutionsSummaryDTO getBatchExecutionsSummary(String environment, String uuaa, String platform, String origin)
    {
        long[] deploymentServiceIds = getDeploymentServiceIdsFrom(uuaa);
        final String environmentValue = getFilterValue(environment);
        final String platformValue = getFilterValue(platform);
        final String originValue = getFilterValue(origin);
        final BatchManagerBatchExecutionsSummaryDTO batchManagerBatchExecutionsSummaryDTO = this.batchManagerClient.getBatchExecutionsSummary(environmentValue, deploymentServiceIds, uuaa, platformValue, originValue);
        return this.mapToBatchExecutionsSummaryDTO(batchManagerBatchExecutionsSummaryDTO);
    }

    @Override
    public byte[] getBatchExecutionsSummaryExport(final String environment, final String uuaa, final String platform, final String origin, final String format)
    {
        final BatchExecutionsSummaryDTO batchManagerBatchExecutionDTO = this.getBatchExecutionsSummary(environment, uuaa, platform, origin);

        final List<String[]> batchList = Arrays.stream(batchManagerBatchExecutionDTO.getElements())
                .map(batch -> new String[]{batch.getStatus(), String.valueOf(batch.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, batchList);
    }

    @Override
    public BatchSchedulerExecutionsSummaryDTO getBatchSchedulerExecutionsSummary(String environment, String uuaa, String platform)
    {
        String selectedEnvironment = getFilterValue(environment);
        String selectedUuaa = getFilterValue(uuaa);
        String selectedPlatform = getFilterValue(platform);
        List<Integer> deploymentPlanIdsResultSet = this.deploymentPlanRepository.findByStatisticsSummaryFilters(selectedEnvironment, selectedUuaa, selectedPlatform);
        final boolean existsAnyFilter = !(!isParameterBeingFiltered(environment) && !isParameterBeingFiltered(uuaa) && !isParameterBeingFiltered(platform));
        int[] deploymentPlanIdsArray;
        if (existsAnyFilter && deploymentPlanIdsResultSet.isEmpty())
        {
            deploymentPlanIdsArray = new int[]{-1};
        }
        else
        {
            deploymentPlanIdsArray = new int[deploymentPlanIdsResultSet.size()];
            for (int i = 0; i < deploymentPlanIdsResultSet.size(); i++)
            {
                deploymentPlanIdsArray[i] = deploymentPlanIdsResultSet.get(i);
            }

        }
        final SMBatchSchedulerExecutionsSummaryDTO batchSchedulerExecutionsSummary = this.schedulerManagerClient.getBatchSchedulerExecutionsSummary(deploymentPlanIdsArray);
        return this.mapSchedulerSummaryDTO(batchSchedulerExecutionsSummary);
    }

    @Override
    public byte[] getBatchSchedulerExecutionsSummaryExport(final String environment, final String uuaa, final String platform, final String format)
    {
        final BatchSchedulerExecutionsSummaryDTO batchSchedulerExecutionsSummaryDTO = this.getBatchSchedulerExecutionsSummary(environment, uuaa, platform);

        final List<String[]> schedulerList = Arrays.stream(batchSchedulerExecutionsSummaryDTO.getElements())
                .map(scheduler -> new String[]{scheduler.getStatus(), String.valueOf(scheduler.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"STATUS", "TOTAL"}, schedulerList);
    }

    @Override
    public InstancesSummaryDTO getInstancesSummary(String environment, String uuaa, String platform, String status)
    {
        boolean filterByPlatform = isParameterBeingFiltered(platform);

        List<List<Map<String, Object>>> instancesCountList;
        if (filterByPlatform)
        {
            Platform destinationPlatformDeployType = mapPlatformStringToDestinationPlatformDeployType(platform);
            if (Platform.NOVA.equals(destinationPlatformDeployType))
            {
                instancesCountList = List.of(this.getNovaInstancesSummary(environment, uuaa, status));
            }
            else if (Platform.ETHER.equals(destinationPlatformDeployType))
            {
                instancesCountList = List.of(this.getEtherInstancesSummary(environment, uuaa, status));
            }
            else
            {
                LOG.error("[ServiceStatisticsapi] -> [getInstancesSummary]: Platform [{}] not valid.", platform);
                throw new NovaException(StatisticsError.getPlatformNotValidError(platform));
            }
        }
        else
        {
            instancesCountList = List.of(this.getNovaInstancesSummary(environment, uuaa, status), this.getEtherInstancesSummary(environment, uuaa, status));
        }

        return this.mapInstancesCountToInstancesSummaryDTO(instancesCountList);

    }

    @Override
    public byte[] getInstancesSummaryExport(final String environment, final String uuaa, final String platform, final String status, final String format)
    {
        final InstancesSummaryDTO instancesSummaryDTO = this.getInstancesSummary(environment, uuaa, platform, status);

        final List<String[]> instanceList = Arrays.stream(instancesSummaryDTO.getElements())
                .map(instance -> new String[]{instance.getServiceInstanceType(), String.valueOf(instance.getTotal())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"SERVICE", "STATUS"}, instanceList);
    }

    @Override
    @Transactional(readOnly = true)
    public STHistoricalPoint[] getProductsHistorical(String startDate, String endDate, String type)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalPoint(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.PRODUCTS.name(),
                StatisticParamName.PRODUCTS_TYPE.name(),
                type,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getProductsHistoricalExport(String startDate, String endDate, String type, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.PRODUCTS.name(),
                        StatisticParamName.PRODUCTS_TYPE.name(),
                        type,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER, values);
    }

    @Override
    @Transactional(readOnly = true)
    public STHistoricalSerie[] getDeployedServicesHistorical(String startDate, String endDate, String environment, String platform, String language, String type, String uuaa, String category)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);
        language = this.convertFilterToNull(language);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.DEPLOYED_SERVICES.name(),
                StatisticParamName.DEPLOYED_SERVICES_ENVIRONMENT.name(),
                environment,
                StatisticParamName.DEPLOYED_SERVICES_PLATFORM.name(),
                platform,
                StatisticParamName.DEPLOYED_SERVICES_TYPE.name(),
                type,
                StatisticParamName.DEPLOYED_SERVICES_UUAA.name(),
                uuaa,
                StatisticParamName.DEPLOYED_SERVICES_LANGUAGE.name(),
                language,
                category));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getDeployedServicesHistoricalExport(String startDate, String endDate, String environment, String platform, String language, String type, String uuaa, final String format, String category)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);
        category = this.convertFilterToNull(category);
        language = this.convertFilterToNull(language);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.DEPLOYED_SERVICES.name(),
                        StatisticParamName.DEPLOYED_SERVICES_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.DEPLOYED_SERVICES_PLATFORM.name(),
                        platform,
                        StatisticParamName.DEPLOYED_SERVICES_TYPE.name(),
                        type,
                        StatisticParamName.DEPLOYED_SERVICES_UUAA.name(),
                        uuaa,
                        StatisticParamName.DEPLOYED_SERVICES_LANGUAGE.name(),
                        language,
                        category).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getInstancesHistorical(String startDate, String endDate, String environment, String platform, String type, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.INSTANCES.name(),
                StatisticParamName.INSTANCES_ENVIRONMENT.name(),
                environment,
                StatisticParamName.INSTANCES_PLATFORM.name(),
                platform,
                StatisticParamName.INSTANCES_TYPE.name(),
                type,
                StatisticParamName.INSTANCES_UUAA.name(),
                uuaa,
                null,
                null,
                StatisticParamName.INSTANCES_TYPE.name()));
    }

    @Override
    public byte[] getInstancesHistoricalExport(String startDate, String endDate, String environment, String platform, String type, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.INSTANCES.name(),
                        StatisticParamName.INSTANCES_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.INSTANCES_PLATFORM.name(),
                        platform,
                        StatisticParamName.INSTANCES_TYPE.name(),
                        type,
                        StatisticParamName.INSTANCES_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        StatisticParamName.INSTANCES_TYPE.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalPoint[] getCategoriesHistorical(String startDate, String endDate, String type)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalPoint(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.CATEGORIES.name(),
                StatisticParamName.CATEGORIES_TYPE.name(),
                type,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Override
    public byte[] getCategoriesHistoricalExport(String startDate, String endDate, String type, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.CATEGORIES.name(),
                        StatisticParamName.CATEGORIES_TYPE.name(),
                        type,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER, values);
    }

    @Override
    public STHistoricalPoint[] getUsersHistorical(String startDate, String endDate, String role, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        final String convertedRole = this.convertFilterToNull(role);
        final String convertedUuaa = this.convertFilterToNull(uuaa);
        String appliedRole;
        String appliedUuaa;
        if (convertedRole == null)
        {
            appliedRole = convertedUuaa == null ? "ALL" : null;
            appliedUuaa = convertedUuaa == null ? "ALL" : convertedUuaa;
        }
        else
        {
            appliedRole = convertedRole;
            appliedUuaa = convertedUuaa != null ? convertedUuaa : "ALL";
        }

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalPoint(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.USERS.name(),
                StatisticParamName.USERS_ROLE.name(),
                appliedRole,
                StatisticParamName.USERS_UUAA.name(),
                appliedUuaa,
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Override
    public byte[] getUsersHistoricalExport(String startDate, String endDate, String role, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        final String convertedRole = this.convertFilterToNull(role);
        final String convertedUuaa = this.convertFilterToNull(uuaa);
        String appliedRole;
        String appliedUuaa;
        if (convertedRole == null)
        {
            appliedRole = convertedUuaa == null ? "ALL" : null;
            appliedUuaa = convertedUuaa == null ? "ALL" : convertedUuaa;
        }
        else
        {
            appliedRole = convertedRole;
            appliedUuaa = convertedUuaa != null ? convertedUuaa : "ALL";
        }

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.USERS.name(),
                        StatisticParamName.USERS_ROLE.name(),
                        appliedRole,
                        StatisticParamName.USERS_UUAA.name(),
                        appliedUuaa,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), BigDecimal.valueOf((Double) map[1]).setScale(2, RoundingMode.HALF_EVEN).toString()})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER, values);
    }

    @Override
    public STHistoricalSerie[] getConnectorsHistorical(String startDate, String endDate, String environment, String status, String type, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.CONNECTORS.name(),
                StatisticParamName.CONNECTORS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.CONNECTORS_STATUS.name(),
                status,
                StatisticParamName.CONNECTORS_TYPE.name(),
                type,
                StatisticParamName.CONNECTORS_UUAA.name(),
                uuaa,
                null,
                null,
                StatisticParamName.CONNECTORS_ENVIRONMENT.name()));
    }

    @Override
    public byte[] getConnectorsHistoricalExport(String startDate, String endDate, String environment, String status, String type, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.CONNECTORS.name(),
                        StatisticParamName.CONNECTORS_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.CONNECTORS_STATUS.name(),
                        status,
                        StatisticParamName.CONNECTORS_TYPE.name(),
                        type,
                        StatisticParamName.CONNECTORS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        StatisticParamName.CONNECTORS_ENVIRONMENT.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getFilesystemsHistorical(String startDate, String endDate, String environment, String status, String type, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.FILESYSTEMS.name(),
                StatisticParamName.FILESYSTEMS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.FILESYSTEMS_STATUS.name(),
                status,
                StatisticParamName.FILESYSTEMS_TYPE.name(),
                type,
                StatisticParamName.FILESYSTEMS_UUAA.name(),
                uuaa,
                null,
                null,
                StatisticParamName.FILESYSTEMS_TYPE.name()));
    }

    @Override
    public byte[] getFilesystemsHistoricalExport(String startDate, String endDate, String environment, String status, String type, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.FILESYSTEMS.name(),
                        StatisticParamName.FILESYSTEMS_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.FILESYSTEMS_STATUS.name(),
                        status,
                        StatisticParamName.FILESYSTEMS_TYPE.name(),
                        type,
                        StatisticParamName.FILESYSTEMS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        StatisticParamName.FILESYSTEMS_TYPE.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getSubsystemsHistorical(String startDate, String endDate, String type, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.SUBSYSTEMS.name(),
                StatisticParamName.SUBSYSTEMS_TYPE.name(),
                type,
                StatisticParamName.SUBSYSTEMS_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                null,
                null,
                StatisticParamName.SUBSYSTEMS_TYPE.name()));
    }

    @Override
    public byte[] getSubsystemsHistoricalExport(String startDate, String endDate, String type, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);


        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.SUBSYSTEMS.name(),
                        StatisticParamName.SUBSYSTEMS_TYPE.name(),
                        type,
                        StatisticParamName.SUBSYSTEMS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.SUBSYSTEMS_TYPE.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getApisHistorical(String apiFunctionality, String environment, String endDate, String uuaa, String type, String startDate)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);
        apiFunctionality = this.convertFilterToNull(apiFunctionality);
        environment = this.convertFilterToNull(environment);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.APIS.name(),
                StatisticParamName.APIS_TYPE.name(),
                type,
                StatisticParamName.APIS_UUAA.name(),
                uuaa,
                StatisticParamName.APIS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.APIS_DISCRIMINATOR.name(),
                apiFunctionality,
                null,
                null,
                StatisticParamName.APIS_TYPE.name()));
    }

    @Override
    public byte[] getApisHistoricalExport(String startDate, String endDate, String type, String uuaa, String environment, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);
        environment = this.convertFilterToNull(environment);


        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.APIS.name(),
                        StatisticParamName.APIS_TYPE.name(),
                        type,
                        StatisticParamName.APIS_UUAA.name(),
                        uuaa,
                        StatisticParamName.APIS_ENVIRONMENT.name(),
                        environment,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.APIS_TYPE.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getCompilationsHistorical(String startDate, String endDate, String status, String type, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.COMPILATIONS.name(),
                StatisticParamName.COMPILATIONS_STATUS.name(),
                status,
                StatisticParamName.COMPILATIONS_TYPE.name(),
                type,
                StatisticParamName.COMPILATIONS_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                StatisticParamName.COMPILATIONS_TYPE.name()));
    }

    @Override
    public byte[] getCompilationsHistoricalExport(String startDate, String endDate, String status, String type, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.COMPILATIONS.name(),
                        StatisticParamName.COMPILATIONS_STATUS.name(),
                        status,
                        StatisticParamName.COMPILATIONS_TYPE.name(),
                        type,
                        StatisticParamName.COMPILATIONS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.COMPILATIONS_TYPE.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getFiletransfersHistorical(String startDate, String endDate, String environment, String status, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.FILETRANSFERS.name(),
                StatisticParamName.FILETRANSFERS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.FILETRANSFERS_STATUS.name(),
                status,
                StatisticParamName.FILETRANSFERS_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                StatisticParamName.FILETRANSFERS_STATUS.name()));
    }

    @Override
    public byte[] getFiletransfersHistoricalExport(String startDate, String endDate, String environment, String status, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.FILETRANSFERS.name(),
                        StatisticParamName.FILETRANSFERS_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.FILETRANSFERS_STATUS.name(),
                        status,
                        StatisticParamName.FILETRANSFERS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.FILETRANSFERS_STATUS.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalPoint[] getTodotasksHistorical(String startDate, String endDate, String status, String type, String role, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        role = this.convertFilterToNull(role);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalPoint(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.TODOTASKS.name(),
                StatisticParamName.TODOTASKS_STATUS.name(),
                status,
                StatisticParamName.TODOTASKS_TYPE.name(),
                type,
                StatisticParamName.TODOTASKS_ROLE.name(),
                role,
                StatisticParamName.TODOTASKS_UUAA.name(),
                uuaa,
                null,
                null,
                null));
    }

    @Override
    public byte[] getTodotasksHistoricalExport(String startDate, String endDate, String status, String type, String role, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        role = this.convertFilterToNull(role);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.TODOTASKS.name(),
                        StatisticParamName.TODOTASKS_STATUS.name(),
                        status,
                        StatisticParamName.TODOTASKS_TYPE.name(),
                        type,
                        StatisticParamName.TODOTASKS_ROLE.name(),
                        role,
                        StatisticParamName.TODOTASKS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER, values);
    }

    @Override
    public STHistoricalPoint[] getMemoryHistorical(String startDate, String endDate, String cpd, String environment, String unit)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        cpd = this.convertFilterToNull(cpd);
        environment = this.convertFilterToNull(environment);
        unit = this.convertFilterToNull(unit);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalPoint(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.MEMORY.name(),
                StatisticParamName.MEMORY_CPD.name(),
                cpd,
                StatisticParamName.MEMORY_ENVIRONMENT.name(),
                environment,
                StatisticParamName.MEMORY_UNIT.name(),
                unit,
                null,
                null,
                null,
                null,
                null));
    }

    @Override
    public byte[] getMemoryHistoricalExport(String startDate, String endDate, String cpd, String environment, String unit, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        cpd = this.convertFilterToNull(cpd);
        environment = this.convertFilterToNull(environment);
        unit = this.convertFilterToNull(unit);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.MEMORY.name(),
                        StatisticParamName.MEMORY_CPD.name(),
                        cpd,
                        StatisticParamName.MEMORY_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.MEMORY_UNIT.name(),
                        unit,
                        null,
                        null,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER, values);
    }

    @Override
    public STHistoricalSerie[] getHardwareHistorical(String startDate, String endDate, String environment, String property, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        property = this.convertFilterForPropertyHardwareFilter(property);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        STHistoricalSerie[] stHistoricalSerieArray = this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.HARDWARE.name(),
                StatisticParamName.HARDWARE_ENVIRONMENT.name(),
                environment,
                StatisticParamName.HARDWARE_PROPERTY.name(),
                property,
                StatisticParamName.HARDWARE_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                StatisticParamName.HARDWARE_PROPERTY.name()));

        // 4. Filter result by given property. There is a mixed values for one filter
        STHistoricalSerie[] result;
        if (AVAILABLE_RAM.equals(property))
        {
            result = Arrays.stream(stHistoricalSerieArray).filter(serie -> AVAILABLE_RAM.equals(serie.getCategory())).toArray(STHistoricalSerie[]::new);
        }
        else
        {
            // multivalue case
            result = Arrays.stream(stHistoricalSerieArray).filter(serie -> !AVAILABLE_RAM.equals(serie.getCategory())).toArray(STHistoricalSerie[]::new);
        }

        return result;
    }

    @Override
    public byte[] getHardwareHistoricalExport(String startDate, String endDate, String environment, String property, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        property = this.convertFilterForPropertyHardwareFilter(property);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> currentValues = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.HARDWARE.name(),
                        StatisticParamName.HARDWARE_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.HARDWARE_PROPERTY.name(),
                        property,
                        StatisticParamName.HARDWARE_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.HARDWARE_PROPERTY.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), BigDecimal.valueOf((Double) map[1]).setScale(2, RoundingMode.HALF_UP).toString()})
                .collect(Collectors.toList());

        List<String[]> values;
        if (AVAILABLE_RAM.equals(property))
        {
            values = currentValues.stream().filter(serie -> AVAILABLE_RAM.equals(serie[1])).collect(Collectors.toList());
        }
        else
        {
            // multivalue caseServiceStatisticsapiTest
            values = currentValues.stream().filter(serie -> !AVAILABLE_RAM.equals(serie[1])).collect(Collectors.toList());
        }

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getStorageHistorical(String startDate, String endDate, String environment, String property, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        property = this.convertFilterToNull(property);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        List<Object[]> categorizedList = this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.STORAGE.name(),
                StatisticParamName.STORAGE_ENVIRONMENT.name(),
                environment,
                StatisticParamName.STORAGE_PROPERTY.name(),
                property,
                StatisticParamName.STORAGE_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                StatisticParamName.STORAGE_PROPERTY.name());

        List<Object[]> dateAndValuePairsList = this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.STORAGE.name(),
                StatisticParamName.STORAGE_ENVIRONMENT.name(),
                environment,
                StatisticParamName.STORAGE_PROPERTY.name(),
                property,
                StatisticParamName.STORAGE_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                null);

        return this.buildSTHistoricalSerieForStorageHistorical(categorizedList, dateAndValuePairsList);
    }

    @Override
    public byte[] getStorageHistoricalExport(String startDate, String endDate, String environment, String property, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        property = this.convertFilterToNull(property);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.STORAGE.name(),
                        StatisticParamName.STORAGE_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.STORAGE_PROPERTY.name(),
                        property,
                        StatisticParamName.STORAGE_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.STORAGE_PROPERTY.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), BigDecimal.valueOf((Double) map[1]).setScale(2, RoundingMode.HALF_UP).toString()})
                .collect(Collectors.toList());

        values.addAll(this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.STORAGE.name(),
                        StatisticParamName.STORAGE_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.STORAGE_PROPERTY.name(),
                        property,
                        StatisticParamName.STORAGE_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), StatisticsConstants.TOTAL_PARAMETER, BigDecimal.valueOf((Double) map[1]).setScale(2, RoundingMode.HALF_UP).toString()})
                .collect(Collectors.toList()));

        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // sort values
        values = values.stream()
                .sorted((d1, d2) -> {
                    int compared = 0;
                    try
                    {
                        compared = dateFormat.parse(d1[0]).compareTo(dateFormat.parse(d2[0]));
                    }
                    catch (ParseException e)
                    {
                        LOG.error("[ServiceStatisticapi] -> [getStorageHistoricalExport]: Error parsing the dates {} and {}", d1[0], d2[0], e);
                    }

                    return compared;
                })
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public UserProductRoleHistoryDTO[] getUserProductRoleHistorySnapshot()
    {
        UserProductRoleHistoryDTO[] userProductRoleSnapshot = this.userStatisticsClient.getUserProductRoleHistorySnapshot();
        Map<Integer, String> productIdUuaaMap = getProductIdUuaaMap();
        Map<String, Map<String, Integer>> dtosGroupedByUuaaAndRoleMap = getUuaaRoleDtosMap(userProductRoleSnapshot, productIdUuaaMap);
        List<UserProductRoleHistoryDTO> groupedDtos = getDtoListGroupedByUuaaAndRole(dtosGroupedByUuaaAndRoleMap);
        return groupedDtos.toArray(new UserProductRoleHistoryDTO[0]);
    }

    @Override
    public PBHardwareBudgetSnapshot[] getHardwareBudgetHistorySnapshot()
    {
        PBHardwareBudgetSnapshot[] hardwareBudgetSnapshot = this.productBudgetsService.getHardwareBudgetHistorySnapshot();
        Map<PBHardwareBudgetSnapshot, List<PBHardwareBudgetSnapshot>> groupedDtosMap = getUniqueKeyValueDtosMap(hardwareBudgetSnapshot);
        Set<Map.Entry<PBHardwareBudgetSnapshot, List<PBHardwareBudgetSnapshot>>> groupedDtosEntries = groupedDtosMap.entrySet();
        return getDtosWithUuaaValues(groupedDtosMap, groupedDtosEntries);
    }

    @Override
    public TOSubsystemsCombinationDTO[] getSubsystemsHistorySnapshot()
    {
        TOSubsystemsCombinationDTO[] subsystemsHistorySnapshot = this.toolsClient.getSubsystemsHistorySnapshot();
        Map<Integer, String> productIdUuaaMap = getProductIdUuaaMap();
        return getMappedTOSubsystemsCombinationDTOs(subsystemsHistorySnapshot, productIdUuaaMap);
    }

    @Override
    public STHistoricalSerie[] getDeployedPlansHistorical(String startDate, String endDate, String environment, String status, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.DEPLOYMENT_PLANS.name(),
                StatisticParamName.DEPLOYMENT_PLANS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.DEPLOYMENT_PLANS_STATUS.name(),
                status,
                StatisticParamName.DEPLOYMENT_PLANS_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()));
    }

    @Override
    public byte[] getDeployedPlansHistoricalExport(String startDate, String endDate, String environment, String status, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.DEPLOYMENT_PLANS.name(),
                        StatisticParamName.DEPLOYMENT_PLANS_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.DEPLOYMENT_PLANS_STATUS.name(),
                        status,
                        StatisticParamName.DEPLOYMENT_PLANS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getReleaseVersionsHistorical(String startDate, String endDate, String status, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        status = this.convertFilterToNull(status);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.RELEASE_VERSIONS.name(),
                StatisticParamName.RELEASE_VERSIONS_STATUS.name(),
                status,
                StatisticParamName.RELEASE_VERSIONS_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                null,
                null,
                StatisticParamName.RELEASE_VERSIONS_STATUS.name()));
    }

    @Override
    public byte[] getReleaseVersionsHistoricalExport(String startDate, String endDate, String status, String uuaa, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        status = this.convertFilterToNull(status);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.RELEASE_VERSIONS.name(),
                        StatisticParamName.RELEASE_VERSIONS_STATUS.name(),
                        status,
                        StatisticParamName.RELEASE_VERSIONS_UUAA.name(),
                        uuaa,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.RELEASE_VERSIONS_STATUS.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getBatchExecutionsHistorical(String startDate, String endDate, String environment, String platform, String uuaa, String status)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);
        uuaa = this.convertFilterToNull(uuaa);
        status = this.convertFilterToNull(status);


        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.BATCH_INSTANCES.name(),
                StatisticParamName.BATCH_INSTANCES_ENVIRONMENT.name(),
                environment,
                StatisticParamName.BATCH_INSTANCES_PLATFORM.name(),
                platform,
                StatisticParamName.BATCH_INSTANCES_UUAA.name(),
                uuaa,
                StatisticParamName.BATCH_INSTANCES_STATUS.name(),
                status,
                null,
                null,
                StatisticParamName.BATCH_INSTANCES_PLATFORM.name()));
    }

    @Override
    public byte[] getBatchExecutionsHistoricalExport(String startDate, String endDate, String environment, String platform, String uuaa, String status, final String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);
        uuaa = this.convertFilterToNull(uuaa);
        status = this.convertFilterToNull(status);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.BATCH_INSTANCES.name(),
                        StatisticParamName.BATCH_INSTANCES_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.BATCH_INSTANCES_PLATFORM.name(),
                        platform,
                        StatisticParamName.BATCH_INSTANCES_UUAA.name(),
                        uuaa,
                        StatisticParamName.BATCH_INSTANCES_STATUS.name(),
                        status,
                        null,
                        null,
                        StatisticParamName.BATCH_INSTANCES_PLATFORM.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        // Added total for NEXT_GEN and LEGACY

        Double nextGen = values.stream().filter(row -> row[1].equalsIgnoreCase("NOVA") || row[1].equalsIgnoreCase("ETHER") || row[1].equalsIgnoreCase("EPHOENIX_NOVA"))
                .map(row -> Double.parseDouble(row[2])).reduce((double) 0, Double::sum);
        Double total = values.stream().map(row -> Double.parseDouble(row[2])).reduce((double) 0, Double::sum);

        Double legacy = values.stream().filter(row -> !row[1].equalsIgnoreCase("NOVA") && !row[1].equalsIgnoreCase("ETHER") && !row[1].equalsIgnoreCase("EPHOENIX_NOVA"))
                .map(row -> Double.parseDouble(row[2])).reduce((double) 0, Double::sum);

        values.add(EMPTY_EXPORT);
        values.add(ADOPTION_LEVEL_EXPORT_HEADER);
        values.add(new String[]{"", NEXT_GEN_PARAMETER, String.format("%.2f", (nextGen / total) * 100)});
        values.add(new String[]{"", LEGACY_PARAMETER, String.format("%.2f", (legacy / total) * 100)});

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public AvailabilityNovaCoinsDTO[] getNovaCoinsByAvailability(String uuaa, String budgetType)
    {
        Integer productId = null;
        if (uuaa != null && !"".equals(uuaa) && !"ALL".equals(uuaa))
        {
            productId = -1;
            Long[] productIdsByUuaa = this.productRepository.findProductIdsByUuaa(uuaa);
            if (productIdsByUuaa != null && productIdsByUuaa.length > 0)
            {
                productId = productIdsByUuaa[0].intValue();
            }
        }
        PBAvailabilityNovaCoinsDTO[] novaCoins = this.productBudgetsService.getNovaCoinsByAvailability(productId, budgetType);
        AvailabilityNovaCoinsDTO[] dtos = new AvailabilityNovaCoinsDTO[novaCoins.length];
        for (int i = 0; i < novaCoins.length; i++)
        {
            PBAvailabilityNovaCoinsDTO novaCoin = novaCoins[i];
            AvailabilityNovaCoinsDTO dto = new AvailabilityNovaCoinsDTO();
            dto.setAvailable(novaCoin.getAvailable());
            dto.setUsed(novaCoin.getUsed());
            dto.setEnvironment(novaCoin.getEnvironment());
            dto.setTotal(novaCoin.getTotal());
            dtos[i] = dto;
        }
        return dtos;
    }

    @Override
    public byte[] getNovaCoinsByAvailabilityExport(final String uuaa, final String budgetType, final String format)
    {
        final AvailabilityNovaCoinsDTO[] novaCoinsDTOS = this.getNovaCoinsByAvailability(uuaa, budgetType);

        final List<String[]> values = Arrays.stream(novaCoinsDTOS)
                .map(coin -> new String[]{coin.getEnvironment(), String.valueOf(coin.getAvailable()), String.valueOf(coin.getUsed())})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, new String[]{"ENVIRONMENT", "AVAILABLE", "USED"}, values);
    }

    @Override
    public STHistoricalSerie[] getAdoptionLevelHistorical(String startDate, String endDate, String environment, String platform)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.ADOPTION_LEVEL.name(),
                StatisticParamName.ADOPTION_LEVEL_ENVIRONMENT.name(),
                environment,
                StatisticParamName.ADOPTION_LEVEL_PLATFORM.name(),
                platform,
                null,
                null,
                null,
                null,
                null,
                null,
                StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()));
    }

    @Override
    public byte[] getAdoptionLevelHistoricalExport(String startDate, String endDate, String environment, String platform, String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        platform = this.convertFilterToNull(platform);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.ADOPTION_LEVEL.name(),
                        StatisticParamName.ADOPTION_LEVEL_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.ADOPTION_LEVEL_PLATFORM.name(),
                        platform,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()).stream()
                .map(map -> new String[]{map[0].toString(), map[2].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        LOG.info("[PRUEBA MPAZ] -> [getAdoptionLevelHistoricalExport] values [{}]", values);

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_CATEGORY, values);
    }

    @Override
    public STHistoricalSerie[] getCloudProductsSummary(String startDate, String endDate, String environment, String uuaa)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.CLOUD_PRODUCTS.name(),
                StatisticParamName.CLOUD_PRODUCTS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.CLOUD_PRODUCTS_UUAA.name(),
                uuaa,
                null,
                null,
                null,
                null,
                null,
                null,
                StatisticParamName.CLOUD_PRODUCTS_PLATFORM.name()));
    }

    @Override
    public byte[] getCloudProductsSummaryExport(String startDate, String endDate, String environment, String uuaa, String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        uuaa = this.convertFilterToNull(uuaa);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getCloudProductsSummaryExport(
                        startDate,
                        endDate,
                        environment,
                        uuaa).stream()
                .map(map -> new String[]{map[0].toString(), map[1].toString(), map[2].toString(), map[3].toString()})
                .collect(Collectors.toList());

        LOG.debug("[ServiceStatisticsapi] -> [getCloudProductsSummaryExport] values [{}]", values);

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_PRODUCT, values);
    }

    @Override
    public STHistoricalPoint[] getUsersConnectedHistorical(String startDate, String endDate)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalPoint(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.USERS_CONNECTED.name(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null));
    }

    @Override
    public byte[] getUsersConnectedHistoricalExport(String startDate, String endDate, String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.USERS_CONNECTED.name(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null).stream()
                .map(map -> new String[]{map[0].toString(), String.valueOf(map[1])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER, values);
    }

    @Override
    @Transactional(readOnly = true)
    public BrokersSummaryDTO getBrokersSummary(String environment, String uuaa, String platform, String status)
    {
        // Get brokers filtered.
        List<Broker> brokers = this.getBrokersFiltered(environment, uuaa, platform, status);

        // Convert the entities to the DTO.
        return this.mapBrokersToBrokersSummaryDTO(brokers);
    }

    @Override
    @Transactional
    public byte[] getBrokersSummaryExport(final String environment, final String uuaa, final String platform, final String status, final String format)
    {
        // Get brokers filtered.
        List<Broker> brokers = this.getBrokersFiltered(environment, uuaa, platform, status);

        // Get product info avoiding lob image access error
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        if (!filterByUuaa)
        {
            for (Broker broker : brokers)
            {
                this.productRepository.fetchById(broker.getProduct().getId());
            }
        }

        // Convert the entities to the exported data object.
        BrokerExportObject[] brokersExported = mapBrokersToBrokersSummaryExport(brokers);

        final List<String[]> brokerList = Arrays.stream(brokersExported)
                .map(broker -> new String[]{
                        broker.getUuaa(),
                        broker.getEnvironment(),
                        broker.getName(),
                        broker.getType(),
                        broker.getPlatform(),
                        broker.getStatus(),
                        String.valueOf(broker.getNumberOfNodes()),
                        String.valueOf(broker.getCpu()),
                        String.valueOf(broker.getMemory()),
                        String.valueOf(broker.getCreationDate())}
                )
                .collect(Collectors.toList());

        // Export data in required format
        return ExportDataUtils.exportValuesTo(
                format,
                new String[]{"UUAA", "ENTORNO", "NOMBRE", "TIPO", "PLATAFORMA", "ESTADO", "NÚMERO DE NODOS", "CPU", "MEMORIA", "FECHA DE CREACIÓN"},
                brokerList);
    }

    @Override
    public STHistoricalSerie[] getBrokersHistorical(String startDate, String endDate, String environment, String status, String type, String uuaa, String platform)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);
        platform = this.convertFilterToNull(platform);

        // 3. Find the results using the repositories and convert them to the response DTO.
        return this.mapListOfValuesByDateToSTHistoricalSerie(this.statisticRepository.getTotalValueBetweenDates(
                startDate,
                endDate,
                StatisticType.BROKERS.name(),
                StatisticParamName.BROKERS_ENVIRONMENT.name(),
                environment,
                StatisticParamName.BROKERS_STATUS.name(),
                status,
                StatisticParamName.BROKERS_TYPE.name(),
                type,
                StatisticParamName.BROKERS_UUAA.name(),
                uuaa,
                StatisticParamName.BROKERS_PLATFORM.name(),
                platform,
                StatisticParamName.BROKERS_ENVIRONMENT.name()));
    }

    @Override
    public byte[] getBrokersHistoricalExport(String startDate, String endDate, String environment, String status, String type, String uuaa, String platform, String format)
    {
        // 1. Format date to UTC
        Pair<String, String> validDateRange = validDateRangeProvider.getValidDateRange(startDate, endDate);
        startDate = validDateRange.getLeft();
        endDate = validDateRange.getRight();

        // 2. Treat 'ALL' filter as if it was null.
        environment = this.convertFilterToNull(environment);
        status = this.convertFilterToNull(status);
        type = this.convertFilterToNull(type);
        uuaa = this.convertFilterToNull(uuaa);
        platform = this.convertFilterToNull(platform);

        // 3. Find the results using the repositories and convert them to the response DTO.
        final List<String[]> values = this.statisticRepository.getStatisticsHistoricalTotalValueBetweenDates(
                        startDate,
                        endDate,
                        StatisticType.BROKERS.name(),
                        StatisticParamName.BROKERS_UUAA.name(),
                        uuaa,
                        StatisticParamName.BROKERS_ENVIRONMENT.name(),
                        environment,
                        StatisticParamName.BROKERS_TYPE.name(),
                        type,
                        StatisticParamName.BROKERS_PLATFORM.name(),
                        platform,
                        StatisticParamName.BROKERS_STATUS.name(),
                        status)
                .stream()
                .map(map -> new String[]{map[0].toString(), map[1].toString(), map[2].toString(), map[3].toString(), map[4].toString(),
                        map[5].toString(), String.valueOf(map[6])})
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, EXPORT_HEADER_BROKER, values);
    }

    private long[] getDeploymentServiceIdsFrom(String uuaa)
    {
        long[] deploymentServiceIds = new long[]{};
        if (isParameterBeingFiltered(uuaa))
        {
            final List<Integer> serviceIds = this.deploymentServiceRepository.findDeploymentServiceIdsByUuaa(uuaa.toUpperCase());
            // If no result is returned, we force the empty resultset inserting a dummy -1 as id
            deploymentServiceIds = !serviceIds.isEmpty() ? serviceIds.stream().mapToLong(e -> e).toArray() : new long[]{-1};
        }
        return deploymentServiceIds;
    }

    private String getFilterValue(String value)
    {
        return isParameterBeingFiltered(value) ? value.toUpperCase() : null;
    }

    private BatchSchedulerExecutionsSummaryDTO mapSchedulerSummaryDTO(final SMBatchSchedulerExecutionsSummaryDTO batchSchedulerExecutionsSummary)
    {
        BatchSchedulerExecutionsSummaryDTO summaryDTO = new BatchSchedulerExecutionsSummaryDTO();
        summaryDTO.setTotal(batchSchedulerExecutionsSummary.getTotal());
        final int elementsLength = batchSchedulerExecutionsSummary.getElements().length;
        BatchSchedulerExecutionDTO[] elements = new BatchSchedulerExecutionDTO[elementsLength];
        for (int i = 0; i < elementsLength; i++)
        {
            final SMBatchSchedulerExecutionDTO currentElement = batchSchedulerExecutionsSummary.getElements()[i];
            BatchSchedulerExecutionDTO mappedElement = new BatchSchedulerExecutionDTO();
            mappedElement.setStatus(currentElement.getStatus());
            mappedElement.setTotal(currentElement.getTotal());
            elements[i] = mappedElement;
        }
        summaryDTO.setElements(elements);
        return summaryDTO;
    }

    /**
     * Map a list from Apis database to ApiSummaryDTO in order to show data in
     * diagrams.
     *
     * @param apiSummary Objects[] list (ApiType-String and TotalNumber-BigInteger)
     * @return ApiSummaryDTO with APIType and total numbers
     */
    private ApiSummaryDTO mapApisSummary(List<Object[]> apiSummary)
    {
        ApiSummaryDTO apiSummaryDTO = new ApiSummaryDTO();
        List<ApiDTO> apiDTOList = new ArrayList<>();
        long totalNumber = 0;

        for (Object[] record : apiSummary)
        {
            ApiDTO apiDTO = new ApiDTO();
            apiDTO.setApiType((String) record[1]);
            apiDTO.setTotal(((BigInteger) record[0]).longValue());
            apiDTOList.add(apiDTO);

            totalNumber += ((BigInteger) record[0]).longValue();
        }

        apiSummaryDTO.setTotal(totalNumber);
        apiSummaryDTO.setApis(apiDTOList.toArray(new ApiDTO[0]));

        return apiSummaryDTO;
    }

    private Map<String, Map<String, Integer>> getUuaaRoleDtosMap(UserProductRoleHistoryDTO[] userProductRoleSnapshot, Map<Integer, String> productIdUuaaMap)
    {
        Map<String, Map<String, Integer>> dtosGroupedByUuaaAndRoleMap = new HashMap<>();
        for (UserProductRoleHistoryDTO userProductRoleHistoryDTO : userProductRoleSnapshot)
        {
            Integer productId = userProductRoleHistoryDTO.getProductId();
            String role = userProductRoleHistoryDTO.getRole();
            String uuaa = productIdUuaaMap.get(productId);
            Map<String, Integer> roleValueMap = dtosGroupedByUuaaAndRoleMap.getOrDefault(uuaa, new HashMap<>());
            roleValueMap.put(role, roleValueMap.getOrDefault(role, 0) + userProductRoleHistoryDTO.getValue());
            dtosGroupedByUuaaAndRoleMap.put(uuaa, roleValueMap);
        }
        return dtosGroupedByUuaaAndRoleMap;
    }

    private List<UserProductRoleHistoryDTO> getDtoListGroupedByUuaaAndRole(Map<String, Map<String, Integer>> dtosGroupedByRoleAndUuaaMap)
    {
        List<UserProductRoleHistoryDTO> groupedDtos = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> uuaaEntry : dtosGroupedByRoleAndUuaaMap.entrySet())
        {
            String uuaa = uuaaEntry.getKey();
            for (Map.Entry<String, Integer> roleEntry : uuaaEntry.getValue().entrySet())
            {
                UserProductRoleHistoryDTO dto = new UserProductRoleHistoryDTO();
                dto.setValue(roleEntry.getValue());
                dto.setRole(roleEntry.getKey());
                dto.setUuaa(uuaa);
                groupedDtos.add(dto);
            }
        }
        return groupedDtos;
    }

    private Map<Integer, String> getProductIdUuaaMap()
    {
        List<Object[]> idUuaaPairs = this.productRepository.findIdUuaaPairs();
        Map<Integer, String> idUuaaMap = new HashMap<>(idUuaaPairs.size());
        for (Object[] pair : idUuaaPairs)
        {
            idUuaaMap.put((Integer) pair[0], (String) pair[1]);
        }
        return idUuaaMap;
    }

    private TOSubsystemsCombinationDTO[] getMappedTOSubsystemsCombinationDTOs(TOSubsystemsCombinationDTO[] subsystemsHistorySnapshot, Map<Integer, String> productIdUuaaMap)
    {
        List<TOSubsystemsCombinationDTO> mappedDtos = new ArrayList<>();
        Map<TOSubsystemsCombinationDTO, Integer> mapping = new HashMap<>();
        for (TOSubsystemsCombinationDTO dto : subsystemsHistorySnapshot)
        {
            Integer productId = dto.getProductId();
            String uuaa = productIdUuaaMap.getOrDefault(productId, "");
            if ("".equals(uuaa))
            {
                continue;
            }
            TOSubsystemsCombinationDTO key = getMappedDto(dto.getSubsystemType(), uuaa);
            Integer mappedCount = mapping.getOrDefault(key, 0);
            mapping.put(key, dto.getCount() + mappedCount);
        }
        for (Map.Entry<TOSubsystemsCombinationDTO, Integer> mappingEntry : mapping.entrySet())
        {
            TOSubsystemsCombinationDTO mappedDto = mappingEntry.getKey();
            mappedDto.setCount(mappingEntry.getValue());
            mappedDtos.add(mappedDto);
        }
        return mappedDtos.toArray(new TOSubsystemsCombinationDTO[0]);
    }

    private TOSubsystemsCombinationDTO getMappedDto(String subsystemType, String uuaa)
    {
        TOSubsystemsCombinationDTO mappedDto = new TOSubsystemsCombinationDTO();
        mappedDto.setSubsystemType(subsystemType);
        mappedDto.setUuaa(uuaa);
        return mappedDto;
    }

    private Map<PBHardwareBudgetSnapshot, List<PBHardwareBudgetSnapshot>> getUniqueKeyValueDtosMap(PBHardwareBudgetSnapshot[] hardwareBudgetSnapshot)
    {
        Map<Integer, String> productIdUuaaMap = getProductIdUuaaMap();
        return Arrays.stream(hardwareBudgetSnapshot).collect(groupingBy(e -> {
            PBHardwareBudgetSnapshot dto = new PBHardwareBudgetSnapshot();
            dto.setEnvironment(e.getEnvironment());
            dto.setValueType(e.getValueType());
            dto.setUuaa(productIdUuaaMap.get(e.getProductId()));
            return dto;
        }));
    }

    private PBHardwareBudgetSnapshot[] getDtosWithUuaaValues(Map<PBHardwareBudgetSnapshot, List<PBHardwareBudgetSnapshot>> groupedDtosMap, Set<Map.Entry<PBHardwareBudgetSnapshot, List<PBHardwareBudgetSnapshot>>> groupedDtosEntries)
    {
        PBHardwareBudgetSnapshot[] mappedDtos = new PBHardwareBudgetSnapshot[groupedDtosMap.keySet().size()];
        int i = 0;
        for (Map.Entry<PBHardwareBudgetSnapshot, List<PBHardwareBudgetSnapshot>> groupedDtosEntry : groupedDtosEntries)
        {
            PBHardwareBudgetSnapshot mappedDto = groupedDtosEntry.getKey();
            double value = groupedDtosEntry.getValue().stream().mapToDouble(PBHardwareBudgetSnapshot::getValue).sum();
            mappedDto.setValue(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
            mappedDtos[i] = mappedDto;
            i++;
        }
        return mappedDtos;
    }

    /**
     * Get how many services are of each type for the Nova Instances deployed in the platform.
     * The results can be filtered by Environment, UUAA and Status.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A List representing the count of service types.
     */
    private List<Map<String, Object>> getNovaInstancesSummary(String environment, String uuaa, String status)
    {
        // 1. Check which filters are being applied.

        boolean filterByEnvironment = isParameterBeingFiltered(environment);
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        boolean filterByStatus = isParameterBeingFiltered(status);

        // 2. Convert or check input parameters.

        if (filterByEnvironment)
        {
            mapEnvironmentStringToDatamodelEnvironment(environment);
        }

        Boolean started = null;
        if (filterByStatus)
        {
            started = DeploymentInstanceStatus.getValueOf(status).isStarted();
        }

        if (filterByUuaa)
        {
            uuaa = uuaa.toUpperCase();
        }

        // 3. Get the results using the repositories.
        if (!filterByEnvironment && !filterByUuaa && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: returning all results");
            return this.novaDeploymentInstanceRepository.countServiceTypes();
        }
        else if (!filterByEnvironment && !filterByUuaa) // !filterByEnvironment && !filterByUuaa && filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by status [{}]", status);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByStarted(started);
        }
        else if (!filterByEnvironment && !filterByStatus) // !filterByEnvironment && filterByUuaa && !filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by UUAA [{}]", uuaa);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByUuaa(uuaa);
        }
        else if (!filterByEnvironment) // !filterByEnvironment && filterByUuaa && filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by UUAA [{}] and status [{}]", uuaa, status);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByUuaaAndStarted(uuaa, started);
        }
        else if (!filterByUuaa && !filterByStatus) // filterByEnvironment && !filterByUuaa && !filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by environment [{}]", environment);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByEnvironment(environment);
        }
        else if (!filterByUuaa) // filterByEnvironment && !filterByUuaa && filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by environment [{}] and status [{}]", environment, status);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByEnvironmentAndStarted(environment, started);
        }
        else if (!filterByStatus) // filterByEnvironment && filterByUuaa && !filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by environment [{}] and UUAA [{}]", environment, uuaa);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByEnvironmentAndUuaa(environment, uuaa);
        }
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getNovaInstancesSummary]: filtering by environment [{}], UUAA [{}] and status [{}]", environment, uuaa, status);
            return this.novaDeploymentInstanceRepository.countServiceTypesFilteringByEnvironmentAndUuaaAndStarted(environment, uuaa, started);
        }
    }

    /**
     * Get how many services are of each type for the Ether Instances deployed in the platform.
     * The results can be filtered by Environment, UUAA and Status.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status      Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return A List representing the count of service types.
     */
    private List<Map<String, Object>> getEtherInstancesSummary(String environment, String uuaa, String status)
    {
        // 1. Check which filters are being applied.

        boolean filterByEnvironment = isParameterBeingFiltered(environment);
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        boolean filterByStatus = isParameterBeingFiltered(status);

        // 2. Convert or check input parameters.

        if (filterByEnvironment)
        {
            mapEnvironmentStringToDatamodelEnvironment(environment);
        }

        Boolean started = null;
        if (filterByStatus)
        {
            started = DeploymentInstanceStatus.getValueOf(status).isStarted();
        }

        if (filterByUuaa)
        {
            uuaa = uuaa.toUpperCase();
        }

        // 3. Get the results using the repositories.
        if (!filterByEnvironment && !filterByUuaa && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: returning all results");
            return this.etherDeploymentInstanceRepository.countServiceTypes();
        }
        else if (!filterByEnvironment && !filterByUuaa) // !filterByEnvironment && !filterByUuaa && filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by status [{}]", status);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByStarted(started);
        }
        else if (!filterByEnvironment && !filterByStatus) // !filterByEnvironment && filterByUuaa && !filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by UUAA [{}]", uuaa);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByUuaa(uuaa);
        }
        else if (!filterByEnvironment) // !filterByEnvironment && filterByUuaa && filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by UUAA [{}] and status [{}]", uuaa, status);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByUuaaAndStarted(uuaa, started);
        }
        else if (!filterByUuaa && !filterByStatus) // filterByEnvironment && !filterByUuaa && !filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by environment [{}]", environment);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByEnvironment(environment);
        }
        else if (!filterByUuaa) // filterByEnvironment && !filterByUuaa && filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by environment [{}] and status [{}]", environment, status);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByEnvironmentAndStarted(environment, started);
        }
        else if (!filterByStatus) // filterByEnvironment && filterByUuaa && !filterByStatus
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by environment [{}] and UUAA [{}]", environment, uuaa);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByEnvironmentAndUuaa(environment, uuaa);
        }
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getEtherInstancesSummary]: filtering by environment [{}], UUAA [{}] and status [{}]", environment, uuaa, status);
            return this.etherDeploymentInstanceRepository.countServiceTypesFilteringByEnvironmentAndUuaaAndStarted(environment, uuaa, started);
        }
    }

    /**
     * Whether to filter some results by this parameter.
     *
     * @param parameter The value of the filter to apply.
     * @return False if the parameter takes a special value (see {@value StatisticsConstants#NO_FILTER_PARAMETER}), or it's null, or it's empty.
     */
    private boolean isParameterBeingFiltered(String parameter)
    {
        return parameter != null && !parameter.isEmpty() && !parameter.equalsIgnoreCase(StatisticsConstants.NO_FILTER_PARAMETER);
    }

    /**
     * Map a List of TeamCountDTO to an array of TeamDTO
     *
     * @param teamCountDTOS The List of List of TeamCountDTO.
     * @return An array of TeamDTO.
     */
    private TeamDTO[] mapTeamCountDTOsToTeamDTOs(List<TeamCountDTO> teamCountDTOS)
    {
        Integer size = teamCountDTOS.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapTeamCountDTOsToTeamDTOs]: Mapping [{}] elements", size);
        TeamDTO[] teamDTOS = new TeamDTO[size];
        int i = 0;
        for (TeamCountDTO teamCountDTO : teamCountDTOS)
        {
            TeamDTO teamDTO = new TeamDTO();
            teamDTO.setTotal(teamCountDTO.getTotal());
            teamDTO.setTeam(teamCountDTO.getTeam());
            teamDTOS[i] = teamDTO;
            i++;
        }
        return teamDTOS;
    }

    /**
     * Map a List of TOSubsystemDTO to a SubsystemSummaryDTO.
     *
     * @param subsystemDTOsFromToolService The List of TOSubsystemDTO.
     * @return A SubsystemSummaryDTO.
     */
    private SubsystemSummaryDTO mapSubsystemDTOsToSubsystemSummaryDTO(List<TOSubsystemDTO> subsystemDTOsFromToolService)
    {
        Integer size = subsystemDTOsFromToolService.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapSubsystemDTOsToSubsystemSummaryDTO]: Mapping [{}] elements", size);

        SubsystemSummaryDTO subsystemSummaryDTO = new SubsystemSummaryDTO();

        // Set the total of Subsystems
        subsystemSummaryDTO.setTotal((long) size);

        Map<String, SubsystemDTO> subsystemsMap = new TreeMap<>();
        // For each Subsystem, increment the number of Subsystems of its type.
        for (TOSubsystemDTO subsystemDTOFromToolService : subsystemDTOsFromToolService)
        {
            String subsystemType = subsystemDTOFromToolService.getSubsystemType();
            if (subsystemsMap.containsKey(subsystemType))
            {
                SubsystemDTO subsystemDTO = subsystemsMap.get(subsystemType);
                subsystemDTO.setTotal(subsystemDTO.getTotal() + 1);
            }
            else
            {
                SubsystemDTO subsystemDTO = new SubsystemDTO();
                subsystemDTO.setTotal(1L);
                subsystemDTO.setSubsystemType(subsystemType);
                subsystemsMap.put(subsystemType, subsystemDTO);
            }
        }

        subsystemSummaryDTO.setSubsystems(subsystemsMap.values().toArray(new SubsystemDTO[0]));
        return subsystemSummaryDTO;
    }

    /**
     * Map a List of USUserDTO to a UserSummaryDTO.
     *
     * @param userDTOsFromUserService The List of USUserDTO.
     * @return A UserSummaryDTO.
     */
    private UserSummaryDTO mapUserDTOsToUserSummaryDTO(List<USUserDTO> userDTOsFromUserService)
    {
        Integer size = userDTOsFromUserService.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapUserDTOsToUserSummaryDTO]: Mapping [{}] elements", size);

        UserSummaryDTO userSummaryDTO = new UserSummaryDTO();

        // Set the total of Users
        userSummaryDTO.setTotal((long) size);

        Map<String, TeamDTO> teamsMap = new TreeMap<>();
        // For each User, increment the number of Users in his teams.
        for (USUserDTO userDTOFromUserService : userDTOsFromUserService)
        {
            String[] teams = userDTOFromUserService.getTeams();
            for (String team : teams)
            {
                if (teamsMap.containsKey(team))
                {
                    TeamDTO teamDTO = teamsMap.get(team);
                    teamDTO.setTotal(teamDTO.getTotal() + 1);
                }
                else
                {
                    TeamDTO teamDTO = new TeamDTO();
                    teamDTO.setTotal(1L);
                    teamDTO.setTeam(team);
                    teamsMap.put(team, teamDTO);
                }
            }
        }

        userSummaryDTO.setTeams(teamsMap.values().toArray(new TeamDTO[0]));
        return userSummaryDTO;
    }

    /**
     * Map a TaskList to a TODOTaskSummaryDTO.
     *
     * @param taskSummaryList The TaskList.
     * @return A UserSummaryDTO.
     */
    private TODOTaskSummaryDTO mapTaskListToTODOTaskSummaryDTO(TaskSummaryList taskSummaryList)
    {
        Long size = taskSummaryList.getTotalSize();
        LOG.debug("[ServiceStatisticsapi] -> [mapTaskListToTODOTaskSummaryDTO]: Mapping [{}] elements", size);

        TODOTaskSummaryDTO todoTaskSummaryDTO = new TODOTaskSummaryDTO();
        todoTaskSummaryDTO.setTotal(size);

        Map<String, TaskTypeDTO> typesMap = new TreeMap<>();
        Map<String, TaskTeamDTO> teamsMap = new TreeMap<>();
        for (TaskSummary taskSummary : taskSummaryList.getTasks())
        {
            String type = taskSummary.getTaskType();
            if (typesMap.containsKey(type))
            {
                TaskTypeDTO taskTypeDTO = typesMap.get(type);
                taskTypeDTO.setTotal(taskTypeDTO.getTotal() + 1);
            }
            else
            {
                TaskTypeDTO taskTypeDTO = new TaskTypeDTO();
                taskTypeDTO.setTotal(1L);
                taskTypeDTO.setTodotaskType(type);
                typesMap.put(type, taskTypeDTO);
            }

            String team = taskSummary.getAssignedGroup();
            if (teamsMap.containsKey(team))
            {
                TaskTeamDTO taskTeamDTO = teamsMap.get(team);
                taskTeamDTO.setTotal(taskTeamDTO.getTotal() + 1);
            }
            else
            {
                TaskTeamDTO taskTeamDTO = new TaskTeamDTO();
                taskTeamDTO.setTotal(1L);
                taskTeamDTO.setTeam(team);
                teamsMap.put(team, taskTeamDTO);
            }
        }

        todoTaskSummaryDTO.setTasksByType(typesMap.values().toArray(new TaskTypeDTO[0]));
        todoTaskSummaryDTO.setTasksByTeam(teamsMap.values().toArray(new TaskTeamDTO[0]));
        return todoTaskSummaryDTO;
    }

    /**
     * Map an array of CIJobDTO to a BuildJobSummaryDTO.
     *
     * @param jobDTOs The array of CIJobDTO.
     * @return A BuildJobSummaryDTO.
     */
    private BuildJobSummaryDTO mapJobDTOsToBuildJobSummaryDTO(CIJobDTO[] jobDTOs)
    {
        Integer size = jobDTOs.length;
        LOG.debug("[ServiceStatisticsapi] -> [mapJobDTOsToBuildJobSummaryDTO]: Mapping [{}] elements", size);

        BuildJobSummaryDTO buildJobSummaryDTO = new BuildJobSummaryDTO();

        buildJobSummaryDTO.setTotal((long) size);

        Map<String, JobStatusDTO> jobStatusMap = new TreeMap<>();
        for (CIJobDTO ciJobDTO : jobDTOs)
        {
            String jobStatus = ciJobDTO.getStatus();
            if (jobStatus == null)
            {
                LOG.debug("[ServiceStatisticsapi] -> [mapJobDTOsToBuildJobSummaryDTO]: Excluding element with null status");
                buildJobSummaryDTO.setTotal(buildJobSummaryDTO.getTotal() - 1);
                continue;
            }
            if (jobStatusMap.containsKey(jobStatus))
            {
                JobStatusDTO jobStatusDTO = jobStatusMap.get(jobStatus);
                jobStatusDTO.setTotal(jobStatusDTO.getTotal() + 1);
            }
            else
            {
                JobStatusDTO jobStatusDTO = new JobStatusDTO();
                jobStatusDTO.setTotal(1L);
                jobStatusDTO.setStatus(jobStatus);
                jobStatusMap.put(jobStatus, jobStatusDTO);
            }
        }

        buildJobSummaryDTO.setJobStatuses(jobStatusMap.values().toArray(new JobStatusDTO[0]));
        return buildJobSummaryDTO;

    }

    /**
     * Map a List of Product to a CategorySummaryDTO.
     *
     * @param products The List of Product.
     * @return A CategorySummaryDTO
     */
    private CategorySummaryDTO mapProductsToCategorySummaryDTO(List<Product> products, Category filterByCategory)
    {
        Integer size = products.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapProductsToCategorySummaryDTO]: Mapping [{}] elements", size);

        CategorySummaryDTO categorySummaryDTO = new CategorySummaryDTO();

        Map<Integer, CategoryDTO> categoriesMap = new TreeMap<>();
        for (Product product : products)
        {
            List<Category> categories;
            if (filterByCategory != null)
            {
                categories = List.of(filterByCategory);
            }
            else
            {
                categories = product.getCategories();
            }
            for (Category category : categories)
            {
                if (!this.categoryService.isFunctionalCategory(category.getName()))
                {
                    continue;
                }
                Integer categoryId = category.getId();
                String uuaa = product.getUuaa();
                if (categoriesMap.containsKey(categoryId))
                {
                    String[] currentUuaas = categoriesMap.get(categoryId).getUuaas();
                    String[] newUuaas = Arrays.copyOf(currentUuaas, currentUuaas.length + 1);
                    newUuaas[newUuaas.length - 1] = uuaa;

                    CategoryDTO categoryDTO = categoriesMap.get(categoryId);
                    categoryDTO.setUuaas(newUuaas);
                    categoryDTO.setTotal((long) newUuaas.length);
                }
                else
                {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setId(Long.valueOf(categoryId));
                    categoryDTO.setCategory(category.getName());
                    categoryDTO.setTotal(1L);
                    categoryDTO.setUuaas(new String[]{uuaa});
                    categoriesMap.put(categoryId, categoryDTO);
                }
            }
        }

        categorySummaryDTO.setTotal((long) categoriesMap.size());
        categorySummaryDTO.setCategories(categoriesMap.values().toArray(new CategoryDTO[0]));
        return categorySummaryDTO;
    }

    /**
     * Map a List of NovaApi to a ApiSummaryDTO.
     *
     * @param syncApiVersions The List of NovaApi.
     * @return A ApiSummaryDTO.
     */
    private ApiSummaryDTO mapNovaApisToApiSummaryDTO(List<ApiVersion<?, ?, ?>> syncApiVersions)
    {
        int size = syncApiVersions.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapNovaApisToApiSummaryDTO]: Mapping [{}] elements", size);

        ApiSummaryDTO apiSummaryDTO = new ApiSummaryDTO();

        apiSummaryDTO.setTotal((long) size);

        Map<String, ApiDTO> apisMap = new TreeMap<>();
        for (ApiVersion<?, ?, ?> apiVersion : syncApiVersions)
        {
            // Group and count by type.
            String type;
            if (ApiType.EXTERNAL == apiVersion.getApi().getType())
            {
                type = com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants.EXTERNAL_API_TYPE;
            }
            else if (ApiType.GOVERNED == apiVersion.getApi().getType())
            {
                type = com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants.GOVERNED_API_TYPE;
            }
            else
            {
                type = com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants.NOT_GOVERNED_API_TYPE;
            }
            if (apisMap.containsKey(type))
            {
                ApiDTO apiDTO = apisMap.get(type);
                apiDTO.setTotal(apiDTO.getTotal() + 1);
            }
            else
            {
                ApiDTO apiDTO = new ApiDTO();
                apiDTO.setTotal(1L);
                apiDTO.setApiType(type);
                apisMap.put(type, apiDTO);
            }
        }

        apiSummaryDTO.setApis(apisMap.values().toArray(new ApiDTO[0]));
        return apiSummaryDTO;
    }

    /**
     * Map a List of QASubsystemCodeAnalysis to a QualityAnalysesSummaryDTO.
     *
     * @param qaSubsystemCodeAnalyses The array of QASubsystemCodeAnalysis.
     * @return A QualityAnalysesSummaryDTO.
     */
    private QualityAnalysesSummaryDTO mapQASubsystemCodeAnalysesToQualityAnalysesSummaryDTOs(QASubsystemCodeAnalysis[] qaSubsystemCodeAnalyses)
    {
        int size = qaSubsystemCodeAnalyses.length;
        LOG.debug("[ServiceStatisticsapi] -> [mapQASubsystemCodeAnalysesToQualityAnalysesSummaryDTOs]: Mapping [{}] elements", size);

        QualityAnalysesSummaryDTO qualityAnalysesSummaryDTO = new QualityAnalysesSummaryDTO();

        qualityAnalysesSummaryDTO.setTotal((long) size);

        long numAnalysesOK = 0L;
        long numAnalysesKO = 0L;
        for (QASubsystemCodeAnalysis qaSubsystemCodeAnalysis : qaSubsystemCodeAnalyses)
        {
            if (StatisticsConstants.SUBSYSTEM_CODE_ANALYSES_SQA_STATE_OK.equals(qaSubsystemCodeAnalysis.getSqaState()))
            {
                numAnalysesOK++;
            }
            else
            {
                numAnalysesKO++;
            }
        }
        qualityAnalysesSummaryDTO.setNumAnalysesOK(numAnalysesOK);
        qualityAnalysesSummaryDTO.setNumAnalysesKO(numAnalysesKO);

        return qualityAnalysesSummaryDTO;

    }

    /**
     * Map an array of ProductServiceDetailItem to a BudgetSummaryDTO.
     *
     * @param productServiceDetailItems The array of ProductServiceDetailItem.
     * @return A BudgetSummaryDTO.
     */
    private BudgetSummaryDTO mapProductServiceDetailItemsToBudgetSummaryDTO(ProductServiceDetailItem[] productServiceDetailItems)
    {
        int size = productServiceDetailItems.length;
        LOG.debug("[ServiceStatisticsapi] -> [mapProductServiceDetailItemsToBudgetSummaryDTO]: Mapping [{}] elements", size);

        BudgetSummaryDTO budgetSummaryDTO = new BudgetSummaryDTO();

        Set<Long> initiativeIds = new HashSet<>();
        long totalNovaCoins = 0L;

        Map<String, ServiceBudgetDTO> serviceBudgetsMap = new TreeMap<>();
        for (ProductServiceDetailItem productServiceDetailItem : productServiceDetailItems)
        {
            initiativeIds.add(productServiceDetailItem.getInitiativeId());
            totalNovaCoins += productServiceDetailItem.getCost().longValue();

            String serviceType = productServiceDetailItem.getServiceType().replaceFirst("^\\d{2}\\s", "");
            if (serviceBudgetsMap.containsKey(serviceType))
            {
                ServiceBudgetDTO serviceBudgetDTO = serviceBudgetsMap.get(serviceType);
                serviceBudgetDTO.setNumServices(serviceBudgetDTO.getNumServices() + 1);
                serviceBudgetDTO.setTotalNovaCoins(serviceBudgetDTO.getTotalNovaCoins() + productServiceDetailItem.getCost().longValue());
            }
            else
            {
                ServiceBudgetDTO serviceBudgetDTO = new ServiceBudgetDTO();
                serviceBudgetDTO.setNumServices(1L);
                serviceBudgetDTO.setServiceType(serviceType);
                serviceBudgetDTO.setTotalNovaCoins(productServiceDetailItem.getCost().longValue());
                serviceBudgetsMap.put(serviceType, serviceBudgetDTO);
            }
        }

        budgetSummaryDTO.setTeams(serviceBudgetsMap.values().toArray(new ServiceBudgetDTO[0]));
        budgetSummaryDTO.setNumInitiatives((long) initiativeIds.size());
        budgetSummaryDTO.setTotalNovaCoins(totalNovaCoins);

        return budgetSummaryDTO;
    }

    /**
     * Map a List of LogicalConnector to a ConnectorsSummaryDTO.
     *
     * @param logicalConnectors The List of LogicalConnector.
     * @return A ConnectorsSummaryDTO.
     */
    private ConnectorsSummaryDTO mapLogicalConnectorsToConnectorsSummaryDTO(List<LogicalConnector> logicalConnectors)
    {
        int size = logicalConnectors.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapLogicalConnectorsToConnectorsSummaryDTO]: Mapping [{}] elements", size);

        ConnectorsSummaryDTO connectorsSummaryDTO = new ConnectorsSummaryDTO();

        connectorsSummaryDTO.setTotal((long) size);

        Map<String, ConnectorDTO> connectorsMap = new TreeMap<>();
        for (LogicalConnector logicalConnector : logicalConnectors)
        {
            String connectorType = logicalConnector.getConnectorType().getName();
            if (connectorsMap.containsKey(connectorType))
            {
                ConnectorDTO connectorDTO = connectorsMap.get(connectorType);
                connectorDTO.setTotal(connectorDTO.getTotal() + 1);
            }
            else
            {
                ConnectorDTO connectorDTO = new ConnectorDTO();
                connectorDTO.setTotal(1L);
                connectorDTO.setConnectorType(connectorType);
                connectorsMap.put(connectorType, connectorDTO);
            }
        }

        connectorsSummaryDTO.setElements(connectorsMap.values().toArray(new ConnectorDTO[0]));
        return connectorsSummaryDTO;
    }

    /**
     * Map a String to a Environment enum (see {@link Environment}). Throw an exception if it cannot be mapped.
     *
     * @param environment A String representing a Environment.
     * @return Environment enum (see {@link Environment}) or NovaException.
     */
    private Environment mapEnvironmentStringToProductsApiEnvironment(String environment) throws NovaException
    {
        try
        {
            return Environment.valueOf(environment.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getEnvironmentNotValidError(environment));
        }
    }

    /**
     * Map a {@link Environment} to a {@link com.bbva.enoa.core.novabootstarter.enumerate.Environment}. Throw an exception if it cannot be mapped.
     *
     * @param productsApiEnvironment A {@link Environment}.
     * @return {@link com.bbva.enoa.core.novabootstarter.enumerate.Environment} or NovaException.
     */
    private com.bbva.enoa.core.novabootstarter.enumerate.Environment mapProductsApiEnvironmentToDatamodelEnvironment(Environment productsApiEnvironment) throws NovaException
    {
        try
        {
            return com.bbva.enoa.core.novabootstarter.enumerate.Environment.valueOf(productsApiEnvironment.name().toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getUnexpectedError(), "Cannot convert " + productsApiEnvironment.name());
        }
    }

    /**
     * Map a String to a {@link com.bbva.enoa.core.novabootstarter.enumerate.Environment}. Throw an exception if it cannot be mapped.
     *
     * @param environment A String representing a Environment.
     * @return {@link com.bbva.enoa.core.novabootstarter.enumerate.Environment} or NovaException.
     */
    private com.bbva.enoa.core.novabootstarter.enumerate.Environment mapEnvironmentStringToDatamodelEnvironment(String environment) throws NovaException
    {
        return this.mapProductsApiEnvironmentToDatamodelEnvironment(this.mapEnvironmentStringToProductsApiEnvironment(environment));
    }

    /**
     * Map a List of Release Version to a ReleaseVersionSummaryDTO.
     *
     * @param releaseVersionsResultSet The array containing a ResultSet from database.
     * @return An instance of {@link ReleaseVersionSummaryDTO} .
     */
    private ReleaseVersionSummaryDTO mapFromObjectsArrayToReleaseVersionSummaryDTO(List<Object[]> releaseVersionsResultSet)
    {
        Map<String, Long> serviceTypeGroupMap = new HashMap<>(releaseVersionsResultSet.size());
        List<ServiceDTO> serviceDTOS = new ArrayList<>(releaseVersionsResultSet.size());
        boolean isStoredReleaseVersionsSize = false;
        ReleaseVersionSummaryDTO releaseVersionSummaryDTO = new ReleaseVersionSummaryDTO();
        long totalServices = 0L;
        releaseVersionSummaryDTO.setTotal(totalServices);
        for (Object[] releaseVersionRecord : releaseVersionsResultSet)
        {
            if (!isStoredReleaseVersionsSize)
            {
                releaseVersionSummaryDTO.setTotal(((BigInteger) releaseVersionRecord[2]).longValue());
                isStoredReleaseVersionsSize = true;
            }
            String serviceType = (String) releaseVersionRecord[0];
            String serviceTypeGroup = ServiceTypeGroupProvider.getServiceGroupNameFor(serviceType);
            if (serviceTypeGroup == null)
            {
                continue;
            }
            if (!serviceTypeGroupMap.containsKey(serviceTypeGroup))
            {
                serviceTypeGroupMap.put(serviceTypeGroup, 0L);
            }
            long numberOfCurrentTypeServices = ((BigInteger) releaseVersionRecord[1]).longValue();
            totalServices += numberOfCurrentTypeServices;
            serviceTypeGroupMap.put(serviceTypeGroup, serviceTypeGroupMap.get(serviceTypeGroup) + numberOfCurrentTypeServices);
        }

        for (Map.Entry<String, Long> serviceTypeGroupEntry : serviceTypeGroupMap.entrySet())
        {
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setServiceType(serviceTypeGroupEntry.getKey());
            serviceDTO.setTotal(serviceTypeGroupEntry.getValue());
            serviceDTOS.add(serviceDTO);
        }
        releaseVersionSummaryDTO.setServices(serviceDTOS.toArray(new ServiceDTO[0]));
        releaseVersionSummaryDTO.setTotalServices(totalServices);
        return releaseVersionSummaryDTO;
    }

    /**
     * Map a String to a LogicalConnectorStatus enum (see {@link LogicalConnectorStatus}). Throw an exception if it cannot be mapped.
     *
     * @param logicalConnectorStatus A String representing a LogicalConnectorStatus.
     * @return LogicalConnectorStatus enum (see {@link LogicalConnectorStatus}) or NovaException.
     */
    private LogicalConnectorStatus mapLogicalConnectorStatusStringToEnum(String logicalConnectorStatus) throws NovaException
    {
        try
        {
            return LogicalConnectorStatus.valueOf(logicalConnectorStatus.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getLogicalConnectorStatusNotValidError(logicalConnectorStatus));
        }
    }

    /**
     * Map a "Deployment Services Count" to a ServicesSummaryDTO.
     *
     * @param deploymentServicesCount The "Deployment Services Count".
     * @return A ServicesSummaryDTO.
     */
    private ServicesSummaryDTO mapDeploymentServicesCountToServicesSummaryDTO(List<Map<String, Object>> deploymentServicesCount)
    {
        ServicesSummaryDTO servicesSummaryDTO = new ServicesSummaryDTO();

        servicesSummaryDTO.setTotal(0L);

        Map<String, ServiceDTO> deploymentServicesMap = new TreeMap<>();
        for (Map<String, Object> serviceTypeCount : deploymentServicesCount)
        {
            String serviceType = ServiceTypeGroupProvider.getServiceGroupNameFor(String.valueOf(serviceTypeCount.get("service_type")));
            long count = ((BigInteger) serviceTypeCount.get("count")).longValue();

            servicesSummaryDTO.setTotal(servicesSummaryDTO.getTotal() + count);

            if (deploymentServicesMap.containsKey(serviceType))
            {
                ServiceDTO serviceDTO = deploymentServicesMap.get(serviceType);
                serviceDTO.setTotal(serviceDTO.getTotal() + count);
            }
            else
            {
                ServiceDTO serviceDTO = new ServiceDTO();
                serviceDTO.setTotal(count);
                serviceDTO.setServiceType(serviceType);
                deploymentServicesMap.put(serviceType, serviceDTO);
            }
        }

        servicesSummaryDTO.setElements(deploymentServicesMap.values().toArray(new ServiceDTO[0]));
        return servicesSummaryDTO;
    }

    /**
     * Map a String to a Platform enum (see {@link Platform}). Throw an exception if it cannot be mapped.
     *
     * @param platform A String representing a Platform.
     * @return Platform enum (see {@link Platform}) or NovaException.
     */
    private Platform mapPlatformStringToDestinationPlatformDeployType(String platform)
    {
        try
        {
            return Platform.valueOf(platform.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getPlatformNotValidError(platform));
        }
    }

    /**
     * Map a String to a Platform enum (see {@link Platform}). Throw an exception if it cannot be mapped.
     *
     * @param platform A String representing a Platform.
     * @return Platform enum (see {@link Platform}) or NovaException.
     */
    private Platform mapPlatformStringToPlatformEnum(String platform)
    {
        try
        {
            return Platform.valueOf(platform.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getPlatformEnumNotValidError(platform));
        }
    }

    /**
     * Map a "Deployment Plans Count" to a DeploymentPlansSummaryDTO.
     *
     * @param deploymentPlansCount The "Deployment Plans Count".
     * @return A DeploymentPlansSummaryDTO.
     */
    private DeploymentPlansSummaryDTO mapDeploymentPlansCountToDeploymentPlansSummaryDTO(List<Map<String, Object>> deploymentPlansCount)
    {
        DeploymentPlansSummaryDTO deploymentPlansSummaryDTO = new DeploymentPlansSummaryDTO();

        deploymentPlansSummaryDTO.setTotal(0L);

        Map<String, DeploymentPlanDTO> deploymentPlansMap = new TreeMap<>();
        for (Map<String, Object> statusCount : deploymentPlansCount)
        {
            String status = String.valueOf(statusCount.get("status"));
            long count = ((BigInteger) statusCount.get("count")).longValue();

            deploymentPlansSummaryDTO.setTotal(deploymentPlansSummaryDTO.getTotal() + count);

            if (deploymentPlansMap.containsKey(status))
            {
                DeploymentPlanDTO deploymentPlanDTO = deploymentPlansMap.get(status);
                deploymentPlanDTO.setTotal(deploymentPlanDTO.getTotal() + count);
            }
            else
            {
                DeploymentPlanDTO deploymentPlanDTO = new DeploymentPlanDTO();
                deploymentPlanDTO.setTotal(count);
                deploymentPlanDTO.setStatus(status);
                deploymentPlansMap.put(status, deploymentPlanDTO);
            }
        }

        deploymentPlansSummaryDTO.setElements(deploymentPlansMap.values().toArray(new DeploymentPlanDTO[0]));
        return deploymentPlansSummaryDTO;
    }

    /**
     * Map an array of ASBasicAlertInfoDTO to a AlertsSummaryDTO.
     *
     * @param alerts The array of ASBasicAlertInfoDTO.
     * @return A AlertsSummaryDTO.
     */
    private AlertsSummaryDTO mapASBasicAlertInfoDTOsToAlertsSummaryDTO(ASBasicAlertInfoDTO[] alerts)
    {
        Integer size = alerts.length;
        LOG.debug("[ServiceStatisticsapi] -> [mapASBasicAlertInfoDTOsToAlertsSummaryDTO]: Mapping [{}] elements", size);

        AlertsSummaryDTO alertsSummaryDTO = new AlertsSummaryDTO();

        alertsSummaryDTO.setTotal((long) size);

        Map<String, AlertDTO> alertStatusMap = new TreeMap<>();
        for (ASBasicAlertInfoDTO alert : alerts)
        {
            String alertStatus = alert.getStatus();

            if (alertStatusMap.containsKey(alertStatus))
            {
                AlertDTO alertDTO = alertStatusMap.get(alertStatus);
                alertDTO.setTotal(alertDTO.getTotal() + 1);
            }
            else
            {
                AlertDTO alertDTO = new AlertDTO();
                alertDTO.setTotal(1L);
                alertDTO.setStatus(alertStatus);
                alertStatusMap.put(alertStatus, alertDTO);
            }
        }

        alertsSummaryDTO.setElements(alertStatusMap.values().toArray(new AlertDTO[0]));
        return alertsSummaryDTO;
    }

    /**
     * Map a String to a {@link FilesystemStatus}. Throw an exception if it cannot be mapped.
     *
     * @param status A String representing a {@link FilesystemStatus}.
     * @return {@link FilesystemStatus} or NovaException.
     */
    private FilesystemStatus mapFilesystemStatusStringToFilesystemStatusEnum(String status)
    {
        try
        {
            return FilesystemStatus.valueOf(status.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getFilesystemStatusNotValidError(status));
        }
    }

    /**
     * Map a List of Filesystem to a FilesystemsSummaryDTO.
     *
     * @param filesystemList The List of Filesystem.
     * @return A FilesystemsSummaryDTO.
     */
    private FilesystemsSummaryDTO mapFilesystemListToFilesystemsSummaryDTO(List<Filesystem> filesystemList)
    {
        int size = filesystemList.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapFilesystemListToFilesystemsSummaryDTO]: Mapping [{}] elements", size);

        FilesystemsSummaryDTO filesystemsSummaryDTO = new FilesystemsSummaryDTO();

        filesystemsSummaryDTO.setTotal((long) size);

        Map<FilesystemType, FilesystemDTO> filesystemsMap = new TreeMap<>();
        for (Filesystem filesystem : filesystemList)
        {
            FilesystemType type = filesystem.getType();
            if (filesystemsMap.containsKey(type))
            {
                FilesystemDTO filesystemDTO = filesystemsMap.get(type);
                filesystemDTO.setTotal(filesystemDTO.getTotal() + 1);
            }
            else
            {
                FilesystemDTO filesystemDTO = new FilesystemDTO();
                filesystemDTO.setTotal(1L);
                filesystemDTO.setFilesystemType(type.toString());
                filesystemsMap.put(type, filesystemDTO);
            }
        }

        filesystemsSummaryDTO.setElements(filesystemsMap.values().toArray(new FilesystemDTO[0]));
        return filesystemsSummaryDTO;
    }

    /**
     * Map a FTMFileTransferConfigsStatisticsSummaryDTO to a FileTransfersSummaryDTO.
     *
     * @param fileTransferConfigsSummary The FTMFileTransferConfigsStatisticsSummaryDTO.
     * @return A FileTransfersSummaryDTO.
     */
    private FileTransfersSummaryDTO mapFTMFileTransferConfigsStatisticsSummaryDTOToFileTransfersSummaryDTO(FTMFileTransferConfigsStatisticsSummaryDTO fileTransferConfigsSummary)
    {
        FileTransfersSummaryDTO fileTransfersSummaryDTO = new FileTransfersSummaryDTO();

        fileTransfersSummaryDTO.setTotal(fileTransferConfigsSummary.getTotal());
        FileTransferDTO[] elements = Arrays.stream(fileTransferConfigsSummary.getElements()).map(ftmFileTransferConfigDTO -> {
            FileTransferDTO fileTransferDTO = new FileTransferDTO();
            BeanUtils.copyProperties(ftmFileTransferConfigDTO, fileTransferDTO);
            return fileTransferDTO;
        }).toArray(FileTransferDTO[]::new);
        fileTransfersSummaryDTO.setElements(elements);

        return fileTransfersSummaryDTO;
    }

    /**
     * Map a FTMFileTransfersInstancesStatisticsSummaryDTO to a FileTransfersInstancesSummaryDTO.
     *
     * @param fileTransferInstancesSummary The FTMFileTransfersInstancesStatisticsSummaryDTO.
     * @return A FileTransfersInstancesSummaryDTO.
     */
    private FileTransfersInstancesSummaryDTO mapFTMFileTransferInstancesStatisticsSummaryDTOToFileTransfersSummaryDTO(FTMFileTransfersInstancesStatisticsSummaryDTO fileTransferInstancesSummary)
    {
        FileTransfersInstancesSummaryDTO fileTransfersInstancesSummaryDTO = new FileTransfersInstancesSummaryDTO();

        BeanUtils.copyProperties(fileTransferInstancesSummary, fileTransfersInstancesSummaryDTO);
        FileTransferInstanceStatusDTO[] elements = Arrays.stream(fileTransferInstancesSummary.getStatuses()).map(fileTransferInstanceStatusStatisticsDTO -> {
            FileTransferInstanceStatusDTO fileTransferInstanceStatusDTO = new FileTransferInstanceStatusDTO();
            BeanUtils.copyProperties(fileTransferInstanceStatusStatisticsDTO, fileTransferInstanceStatusDTO);
            return fileTransferInstanceStatusDTO;
        }).toArray(FileTransferInstanceStatusDTO[]::new);
        fileTransfersInstancesSummaryDTO.setStatuses(elements);

        return fileTransfersInstancesSummaryDTO;
    }

    /**
     * Map a List of "instances count" to a InstancesSummaryDTO.
     *
     * @param instancesCountList The "instances count".
     * @return A InstancesSummaryDTO.
     */
    private InstancesSummaryDTO mapInstancesCountToInstancesSummaryDTO(List<List<Map<String, Object>>> instancesCountList)
    {
        InstancesSummaryDTO instancesSummaryDTO = new InstancesSummaryDTO();

        instancesSummaryDTO.setTotal(0L);

        Map<String, InstanceDTO> instancesServiceTypesMap = new TreeMap<>();
        for (List<Map<String, Object>> instancesCount : instancesCountList)
        {
            for (Map<String, Object> serviceTypeCount : instancesCount)
            {
                String serviceType = ServiceTypeGroupProvider.getServiceGroupNameFor(String.valueOf(serviceTypeCount.get("service_type")));
                if (List.of(ServiceGroupingNames.BATCH.name(), ServiceGroupingNames.BATCH_SCHEDULER.name()).contains(serviceType))
                {
                    continue;
                }
                long count = ((BigInteger) serviceTypeCount.get("count")).longValue();

                instancesSummaryDTO.setTotal(instancesSummaryDTO.getTotal() + count);

                if (instancesServiceTypesMap.containsKey(serviceType))
                {
                    InstanceDTO instanceDTO = instancesServiceTypesMap.get(serviceType);
                    instanceDTO.setTotal(instanceDTO.getTotal() + count);
                }
                else
                {
                    InstanceDTO instanceDTO = new InstanceDTO();
                    instanceDTO.setTotal(count);
                    instanceDTO.setServiceInstanceType(serviceType);
                    instancesServiceTypesMap.put(serviceType, instanceDTO);
                }
            }
        }

        instancesSummaryDTO.setElements(instancesServiceTypesMap.values().toArray(new InstanceDTO[0]));

        return instancesSummaryDTO;
    }

    /**
     * If a filter is equals to "ALL" or empty, return null. Otherwise, return the original value of the filter.
     *
     * @param filter The given filter.
     * @return Null or the original value of the filter.
     */
    private String convertFilterToNull(String filter)
    {
        return (StatisticsConstants.NO_FILTER_PARAMETER.equalsIgnoreCase(filter) || "".equals(filter)) ? null : filter;
    }

    /**
     * If a filter is equals to "ALL" or empty or NOVA Coins, return null. Otherwise, return the original value of the filter.
     *
     * @param filter The given filter.
     * @return Null or the original value of the filter.
     */
    private String convertFilterForPropertyHardwareFilter(String filter)
    {
        return (NOVA_COINS.equalsIgnoreCase(filter) || StatisticsConstants.NO_FILTER_PARAMETER.equalsIgnoreCase(filter) || "".equals(filter)) ? null : filter;
    }

    private BatchExecutionsSummaryDTO mapToBatchExecutionsSummaryDTO(BatchManagerBatchExecutionsSummaryDTO batchManagerBatchExecutionsSummaryDTO)
    {
        BatchExecutionsSummaryDTO batchExecutionsSummaryDTO = new BatchExecutionsSummaryDTO();
        batchExecutionsSummaryDTO.setTotal(batchManagerBatchExecutionsSummaryDTO.getTotal());
        batchExecutionsSummaryDTO.setPercentage(batchManagerBatchExecutionsSummaryDTO.getPercentage());
        final BatchManagerBatchExecutionDTO[] elements = batchManagerBatchExecutionsSummaryDTO.getElements();
        List<BatchExecutionDTO> batchExecutionDTOs = new ArrayList<>(elements.length);
        for (BatchManagerBatchExecutionDTO element : elements)
        {
            BatchExecutionDTO batchExecutionDTO = new BatchExecutionDTO();
            batchExecutionDTO.setStatus(element.getStatus());
            batchExecutionDTO.setTotal(element.getTotal());
            batchExecutionDTOs.add(batchExecutionDTO);
        }
        batchExecutionsSummaryDTO.setElements(batchExecutionDTOs.toArray(new BatchExecutionDTO[0]));
        return batchExecutionsSummaryDTO;
    }


    private STHistoricalPoint[] mapListOfValuesByDateToSTHistoricalPoint(List<Object[]> dateAndValuePairsList)
    {
        STHistoricalPoint[] result = new STHistoricalPoint[dateAndValuePairsList.size()];
        int i = 0;
        for (Object[] statisticCustom : dateAndValuePairsList)
        {
            STHistoricalPoint historicalPoint = new STHistoricalPoint();
            historicalPoint.setDate(statisticCustom[0].toString());
            BigDecimal bigDecimal = BigDecimal.valueOf((Double) statisticCustom[1]).setScale(2, RoundingMode.HALF_UP);
            historicalPoint.setValue(bigDecimal.doubleValue());
            result[i] = historicalPoint;
            i++;
        }
        return result;
    }

    private STHistoricalSerie[] mapListOfValuesByDateToSTHistoricalSerie(List<Object[]> categorizedList)
    {
        STHistoricalSerie[] result = new STHistoricalSerie[categorizedList.size()];
        int i = 0;
        for (Object[] statisticCustom : categorizedList)
        {
            STHistoricalSerie historicalSerie = new STHistoricalSerie();
            historicalSerie.setDate(statisticCustom[0].toString());
            BigDecimal bigDecimal = BigDecimal.valueOf((Double) statisticCustom[1]).setScale(2, RoundingMode.HALF_UP);
            historicalSerie.setValue(bigDecimal.doubleValue());
            historicalSerie.setCategory(statisticCustom[2].toString());
            result[i] = historicalSerie;
            i++;
        }
        return result;
    }

    private STHistoricalSerie[] buildSTHistoricalSerieForStorageHistorical(List<Object[]> categorizedList, List<Object[]> dateAndValuePairsList)
    {
        STHistoricalSerie[] result = new STHistoricalSerie[dateAndValuePairsList.size() + categorizedList.size()];
        int i = 0;
        for (Object[] statisticCustom : dateAndValuePairsList)
        {
            STHistoricalSerie historicalSerie = new STHistoricalSerie();
            historicalSerie.setDate(statisticCustom[0].toString());
            BigDecimal bigDecimal = BigDecimal.valueOf((Double) statisticCustom[1]).setScale(2, RoundingMode.HALF_UP);
            historicalSerie.setValue(bigDecimal.doubleValue());
            historicalSerie.setCategory(StatisticsConstants.TOTAL_PARAMETER);
            result[i] = historicalSerie;
            i++;
        }

        for (Object[] statisticCustom : categorizedList)
        {
            STHistoricalSerie historicalSerie = new STHistoricalSerie();
            historicalSerie.setDate(statisticCustom[0].toString());
            BigDecimal bigDecimal = BigDecimal.valueOf((Double) statisticCustom[1]).setScale(2, RoundingMode.HALF_UP);
            historicalSerie.setValue(bigDecimal.doubleValue());
            historicalSerie.setCategory(statisticCustom[2].toString());
            result[i] = historicalSerie;
            i++;
        }

        return result;
    }

    private List<USUserDTO> getUsersInfo(String uuaa)
    {
        List<USUserDTO> userDTOS;
        LOG.debug("[ServiceStatisticsapi] -> [getUsersInfo]: filtering by UUAA [{}]", uuaa);
        List<Product> productList = this.productRepository.findByUuaa(uuaa.toUpperCase());
        if (productList.isEmpty())
        {
            LOG.debug("[ServiceStatisticsapi] -> [getUsersInfo]: Returning empty result");
            userDTOS = new ArrayList<>();
        }
        else if (productList.size() > 1)
        {
            LOG.error("[ServiceStatisticsapi] -> [getUsersInfo]: There is more than one product with UUAA [{}]: {}", uuaa, productList);
            throw new NovaException(StatisticsError.getUaaaNotUniqueError(uuaa, productList));
        }
        else
        {
            userDTOS = this.usersClient.getProductMembers(productList.get(0).getId(), new NovaException(StatisticsError.getUserServiceError()));
        }
        return userDTOS;
    }

    /**
     * Map a List of Brokers to a BrokersSummaryDTO.
     *
     * @param brokers The List of Brokers.
     * @return A BrokersSummaryDTO.
     */
    private BrokersSummaryDTO mapBrokersToBrokersSummaryDTO(List<Broker> brokers)
    {
        int size = brokers.size();
        LOG.debug("[ServiceStatisticsapi] -> [mapBrokersToBrokersSummaryDTO]: Mapping [{}] elements", size);

        BrokersSummaryDTO brokersSummaryDTO = new BrokersSummaryDTO();
        brokersSummaryDTO.setTotal((long) size);

        Map<String, BrokerDTO> brokersMap = new TreeMap<>();
        for (Broker broker : brokers)
        {
            String brokerType = broker.getType().getType();
            if (brokersMap.containsKey(brokerType))
            {
                BrokerDTO brokerDTO = brokersMap.get(brokerType);
                brokerDTO.setTotal(brokerDTO.getTotal() + 1);
            }
            else
            {
                BrokerDTO brokerDTO = new BrokerDTO();
                brokerDTO.setTotal(1L);
                brokerDTO.setBrokerType(brokerType);
                brokersMap.put(brokerType, brokerDTO);
            }
        }
        brokersSummaryDTO.setElements(brokersMap.values().toArray(new BrokerDTO[0]));

        return brokersSummaryDTO;
    }

    /**
     * Map a List of Brokers to a BrokersSummaryDTO.
     *
     * @param brokers The List of Brokers.
     * @return A BrokersSummaryDTO.
     */
    private BrokerExportObject[] mapBrokersToBrokersSummaryExport(List<Broker> brokers)
    {
        int size = brokers.size();
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        LOG.debug("[ServiceStatisticsapi] -> [mapBrokersToBrokersSummaryExport]: Mapping [{}] elements", size);

        Map<String, BrokerExportObject> brokersExportMap = new TreeMap<>();
        for (Broker broker : brokers)
        {
            BrokerExportObject brokerExportObject = new BrokerExportObject();
            brokerExportObject.setId(broker.getId());
            brokerExportObject.setUuaa(broker.getProduct().getUuaa());
            brokerExportObject.setEnvironment(broker.getEnvironment());
            brokerExportObject.setName(broker.getName());
            brokerExportObject.setType(broker.getType().getType());
            brokerExportObject.setPlatform(broker.getPlatform().getName());
            brokerExportObject.setStatus(broker.getStatus().getStatus());
            brokerExportObject.setNumberOfNodes(broker.getNumberOfNodes());
            brokerExportObject.setCpu(broker.getCpu());
            brokerExportObject.setMemory(broker.getMemory());
            brokerExportObject.setCreationDate(formatDate.format(broker.getCreationDate().getTime()));

            brokersExportMap.put(String.valueOf(brokerExportObject.getId()), brokerExportObject);
        }
        return brokersExportMap.values().toArray(new BrokerExportObject[0]);
    }

    /**
     * Map a String to a BrokerStatus enum (see {@link BrokerStatus}). Throw an exception if it cannot be mapped.
     *
     * @param brokerStatus A String representing a BrokerStatus.
     * @return BrokerStatus enum (see {@link BrokerStatus}) or NovaException.
     */
    private BrokerStatus mapBrokerStatusStringToEnum(String brokerStatus) throws NovaException
    {
        try
        {
            return BrokerStatus.valueOf(brokerStatus.toUpperCase());
        }
        catch (IllegalArgumentException exception)
        {
            throw new NovaException(StatisticsError.getBrokerStatusNotValidError(brokerStatus));
        }
    }

    private List<Broker> getBrokersFiltered(String environment, String uuaa, String platform, String status)
    {
        // 1. Check which filters are being applied.
        boolean filterByEnvironment = isParameterBeingFiltered(environment);
        boolean filterByUuaa = isParameterBeingFiltered(uuaa);
        boolean filterByPlatform = isParameterBeingFiltered(platform);
        boolean filterByStatus = isParameterBeingFiltered(status);

        // 2. Convert input parameters to the types required by the repositories.
        String environmentName = null;
        if (filterByEnvironment)
        {
            environmentName = mapEnvironmentStringToDatamodelEnvironment(environment).getEnvironment();
        }

        Integer productId = null;
        if (filterByUuaa)
        {
            List<Product> products = this.productRepository.findByUuaa(uuaa.toUpperCase());
            if (products.isEmpty())
            {
                LOG.debug("[ServiceStatisticsapi] -> [getBrokersFiltered]: Returning empty result");
                return new ArrayList<>();
            }
            else if (products.size() > 1)
            {
                LOG.error("[ServiceStatisticsapi] -> [getBrokersFiltered]: There is more than one product with UUAA [{}]: {}", uuaa, products);
                throw new NovaException(StatisticsError.getUaaaNotUniqueError(uuaa, products));
            }
            else
            {
                productId = products.get(0).getId();
            }
        }
        Platform platformEnum = null;
        if (filterByPlatform)
        {
            platformEnum = this.mapPlatformStringToPlatformEnum(platform);
        }

        BrokerStatus statusEnum = null;
        if (filterByStatus)
        {
            statusEnum = this.mapBrokerStatusStringToEnum(status);
        }
        // 3. Find the entities using the repositories.
        List<Broker> brokers;
        // No filter
        if (!filterByEnvironment && !filterByUuaa && !filterByPlatform && !filterByStatus)
        {
            LOG.debug("[ServiceStatisticsapi] -> [getBrokersFiltered]: returning all results");
            brokers = this.brokerRepository.findAll();
        }
        // Filter
        else
        {
            LOG.debug("[ServiceStatisticsapi] -> [getBrokersFiltered]: filtering by environment [{}], Product ID [{}], platform [{}] and status [{}]",
                    environmentName, productId, platform, statusEnum);
            brokers = this.brokerRepository.findAllBrokersSummary(environmentName, productId, platformEnum, statusEnum);
        }

        return brokers;
    }
}