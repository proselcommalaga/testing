package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behavioragentapi.model.BABehaviorExecutionDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.*;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBBehaviorExecutionInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBBehaviorPotentialCostInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBCheckExecutionInfo;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.behavior.entities.*;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorAction;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorConfigurationStatus;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.common.entities.AbstractEntity;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.config.enumerates.PropertySourceType;
import com.bbva.enoa.datamodel.model.config.enumerates.PropertyType;
import com.bbva.enoa.datamodel.model.config.enumerates.ScopeType;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBehaviorManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductBudgetsClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBehaviorAgentApiClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.MonitoringUtils;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorConfigurationStatus.CONFIGURED;
import static com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorConfigurationStatus.EDITING;
import static com.bbva.enoa.datamodel.model.config.enumerates.PropertySourceType.CONNECTOR;
import static com.bbva.enoa.datamodel.model.config.enumerates.PropertySourceType.TEMPLATE;
import static com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction.PROPERTIES_CONFIGURED;

/**
 * The type Behavior service configuration service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BehaviorServiceConfigurationServiceImpl implements IBehaviorConfigurationService
{
    private static final Logger LOG = LoggerFactory.getLogger(BehaviorServiceConfigurationServiceImpl.class);

    /**
     * Activity constant for behavior version name.
     */
    private static final String BEHAVIOR_VERSION_NAME = "behaviorVersionName";

    /**
     * Activity constant for behavior version description.
     */
    private static final String BEHAVIOR_VERSION_DESCRIPTION = "behaviorVersionDescription";

    /**
     * The Behavior version dto builder.
     */
    public final IBehaviorVersionDtoBuilder behaviorVersionDtoBuilder;

    /**
     * The Behavior service repository.
     */
    public final BehaviorVersionRepository behaviorVersionRepository;

    /**
     * The Behavior service repository.
     */
    public final BehaviorServiceRepository behaviorServiceRepository;

    /**
     * The Behavior service repository.
     */
    public final BehaviorServiceConfigurationRepository behaviorServiceConfigurationRepository;

    /**
     * The Behavior instance repository.
     */
    public final BehaviorInstanceRepository behaviorInstanceRepository;

    /**
     * The Behavior service repository.
     */
    public final BSConfigurationFilesystemRepository bsConfigurationFilesystemRepository;

    /**
     * The Behavior service repository.
     */
    public final BehaviorPropertyValueRepository behaviorPropertyValueRepository;

    /**
     * The Behavior subsystem repository.
     */
    public final BehaviorSubsystemRepository behaviorSubsystemRepository;

    /**
     * The Filesystem repository.
     */
    public final FilesystemRepository filesystemRepository;

    /**
     * The Broker repository.
     */
    public final BrokerRepository brokerRepository;

    /**
     * The Hardware pack repository.
     */
    public final HardwarePackRepository hardwarePackRepository;

    /**
     * The Logical connector repository.
     */
    public final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Jvm/Jdk configuration checker
     */
    public final JvmJdkConfigurationChecker jvmJdkConfigurationChecker;

    /**
     * The Behavior version dto builder modality based.
     */
    public final IBehaviorVersionDtoBuilderModalityBased behaviorVersionDtoBuilderModalityBased;

    /**
     * The Release version repository.
     */
    public final ReleaseVersionRepository releaseVersionRepository;

    /**
     * The Release repository.
     */
    public final ReleaseRepository releaseRepository;

    /**
     * The Behavior tags repository.
     */
    public final BehaviorTagsRepository behaviorTagsRepository;
    /**
     * Nova activity emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Product Budgets API client.
     */
    private final IProductBudgetsClientImpl productBudgetsClient;

    /**
     * Tools service
     */
    private final IToolsClient toolsClient;

    /**
     * Behavior version validator
     */
    private final IBehaviorVersionValidator iBehaviorVersionValidator;

    /**
     * ApiGW service
     */
    private final IApiGatewayService apiGatewayService;

    /**
     * User client
     */
    private final IUsersClient userClient;

    /**
     * Behavior Manager APIClient
     */
    private final IBehaviorManagerClient behaviorManagerClient;

    /**
     * Behavior Service Dto Builder
     */
    private final IBehaviorServiceDtoBuilder iBehaviorServiceDtoBuilder;

    /**
     * ApiGW service
     */
    private final IBehaviorApiGatewayService iBehaviorApiGatewayService;

    private final IBehaviorAgentApiClient behaviorAgentApiClient;

    /**
     * Monitoring utils.
     */
    private final MonitoringUtils monitoringUtils;

    @PersistenceContext
    private EntityManager entityManager;

    //#################### GETTERS ####################

    //@Override
    //public BVBehaviorVersionSummaryInfoDTO[] getAllBehaviorVersions(Integer productId, String status)
    //{
    //    if (null == productId || productId <= 0 || status == null || status.equals(""))
    //    {
    //        LOG.error("[{}] -> [getAllBehaviorVersions]: Bad behavior service id provided", this.getClass().getSimpleName());
    //        throw new NovaException(BehaviorError.getBadParametersError());
    //    }

    //    // Status could be useful for filters
    //    // Find all versions for a product
    //    List<BehaviorVersion> persistedBehaviorVersions = this.behaviorVersionRepository
    //            .findByProductIdAndStatus(productId, BehaviorVersionStatus.valueOf(status))
    //            .orElseThrow(() -> new NovaException(BehaviorError.getResourceNotFoundError("behaviorVersion", "productId", productId.toString(), "status", status)));

    //    return this.convertListToBehaviorVersionSummaryInfo(persistedBehaviorVersions);
    //}

    @Override
    @Transactional(readOnly = true)
    public BVBrokersResourceDto getBehaviorBrokerConfigurationResource(Integer behaviorServiceId)
    {
        // At the moment, throw an information error
//        throw new NovaException(BehaviorError.getNoResourceAvailableError("BROKERS Resource"));

        checkResourceId(behaviorServiceId, "getBehaviorBrokerConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::getResourceNotFoundError);

        BVBrokersResourceDto bvBrokersResourceDto = new BVBrokersResourceDto();

        // Get the configuration info persisted in the database for a behavior service
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorVersion behaviorVersion = behaviorServiceConfigurationOptional.get().getBehaviorService().getBehaviorSubsystem().getBehaviorVersion();
            int productId = behaviorVersion.getProduct().getId();

            // Check initiatives for product
            checkProductInitiatives(productId);

            // This resource could be empty, so not throw error needed
            List<Broker> availableBrokers =
                    this.brokerRepository.findByProductIdAndEnvironmentAndStatus(productId, Environment.PRE.name(), BrokerStatus.RUNNING);

            bvBrokersResourceDto.setProductId(productId);
            bvBrokersResourceDto.setBehaviorVersionId(behaviorVersion.getId());
            bvBrokersResourceDto.setBehaviorServiceId(behaviorServiceId);
//            bvBrokersResourceDto.setSelectedBroker(this.convertListToBrokerConfigurable(behaviorServiceConfiguration));
            bvBrokersResourceDto.setBrokerOptions(this.convertListToBrokerConfigurable(availableBrokers));
        }
        else
        {
            bvBrokersResourceDto.setProductId(-1);
        }

        return bvBrokersResourceDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BVConnectorsResourceDto getBehaviorConnectorsConfigurationResource(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorConnectorsConfigurationResource]: Getting the connector configuration from behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "getBehaviorConnectorsConfigurationResource");

        BehaviorService behaviorService = behaviorServiceRepository.findById(behaviorServiceId)
                .orElseThrow(() -> new NovaException(getResourceNotFoundError(behaviorServiceId)));

        BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();
        int productId = behaviorVersion.getProduct().getId();

        // Check initiatives for product
        checkProductInitiatives(productId);

        BVConnectorsResourceDto bvConnectorsResourceDto = new BVConnectorsResourceDto();

        // Getting the available connector for the product in preproduction (already set the product id in the return dto, so reused it). Could be null
        LOG.debug("[{}] -> [getBehaviorConnectorsConfigurationResource]: Getting the available connectors from database in PRE environment for product [{}]",
                this.getClass().getSimpleName(), productId);
        List<LogicalConnector> availableLogicalConnectors = this.logicalConnectorRepository
                .findByEnvironmentAndProductIdAndLogicalConnectorStatus(Environment.PRE.name(), productId, LogicalConnectorStatus.CREATED);
        bvConnectorsResourceDto.setConnectorOptions(
                availableLogicalConnectors.stream().map(this::convertLogicalConnectorToDTO).toArray(BVConfigurableConnector[]::new));

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();

            LOG.info("[{}] -> [getBehaviorConnectorsConfigurationResource]: Converting to DTO response...", getClass().getSimpleName());

            // Info fields
            bvConnectorsResourceDto.setProductId(productId);
            bvConnectorsResourceDto.setBehaviorVersionId(behaviorVersion.getId());
            bvConnectorsResourceDto.setBehaviorServiceId(behaviorService.getId());
            if (!behaviorServiceConfiguration.getLogicalConnectorList().isEmpty())
            {
                bvConnectorsResourceDto.setSelectedConnector(
                        behaviorServiceConfiguration.getLogicalConnectorList()
                                .stream().map(this::convertLogicalConnectorToDTO).toArray(BVConfigurableConnector[]::new));
            }
        }
        else
        {
            bvConnectorsResourceDto.setProductId(-1);
        }

        LOG.info("[{}] -> [getBehaviorConnectorsConfigurationResource]: Generated response from persisted data [{}]",
                this.getClass().getSimpleName(), bvConnectorsResourceDto);

        return bvConnectorsResourceDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BVFilesystemResourceDto getBehaviorFilesystemsConfigurationResource(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorFilesystemsConfigurationResource]: Getting the filesystem configuration from behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "getBehaviorFilesystemsConfigurationResource");

        BehaviorService behaviorService = behaviorServiceRepository.findById(behaviorServiceId)
                .orElseThrow(() -> new NovaException(getResourceNotFoundError(behaviorServiceId)));
        BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();
        int productId = behaviorVersion.getProduct().getId();

        // Check initiatives for product
        checkProductInitiatives(productId);

        BVFilesystemResourceDto bvFilesystemResourceDtoToReturn = new BVFilesystemResourceDto();
        // Getting the available filesystems for the product in preproduction (already set the product id in the return dto, so reused it). Could be null
        LOG.debug("[{}] -> [getBehaviorFilesystemsConfigurationResource]: Getting the available file systems from database in PRE environment, status CREATED, for product [{}]",
                this.getClass().getSimpleName(), productId);
        List<Filesystem> availableFilesystems =
                this.filesystemRepository.findByEnvironmentAndFilesystemStatusAndProductId(Environment.PRE.name(), FilesystemStatus.CREATED,
                        productId);

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);

        LOG.info("[{}] -> [getBehaviorFilesystemsConfigurationResource]: Converting to DTO response...",
                this.getClass().getSimpleName());
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();

            // Info fields
            bvFilesystemResourceDtoToReturn.setProductId(productId);
            bvFilesystemResourceDtoToReturn.setBehaviorVersionId(behaviorVersion.getId());
            bvFilesystemResourceDtoToReturn.setBehaviorServiceId(behaviorService.getId());

            if (!behaviorServiceConfiguration.getBsConfigurationFilesystemList().isEmpty())
            {
                bvFilesystemResourceDtoToReturn.setSelectedFilesystem(
                        behaviorServiceConfiguration.getBsConfigurationFilesystemList()
                                .stream().map(this::convertBSConfigurationFileSystemToConfigurableDto).toArray(BVConfigurableFilesystem[]::new));
            }
            bvFilesystemResourceDtoToReturn.setFilesystemOptions(
                    createBsConfigurationFilesystemList(availableFilesystems, behaviorServiceConfiguration, null)
                            .stream().map(this::convertBSConfigurationFileSystemToConfigurableDto).toArray(BVConfigurableFilesystem[]::new));
        }
        else
        {
            bvFilesystemResourceDtoToReturn.setProductId(-1);
            bvFilesystemResourceDtoToReturn.setFilesystemOptions(availableFilesystems.stream()
                    .map(this::convertFilesystemToConfigurableFileSystemDto).toArray(BVConfigurableFilesystem[]::new));
        }

        LOG.info("[{}] -> [getBehaviorFilesystemsConfigurationResource]: Generated response from persisted data [{}]",
                this.getClass().getSimpleName(), bvFilesystemResourceDtoToReturn);

        return bvFilesystemResourceDtoToReturn;
    }

    @Override
    @Transactional(readOnly = true)
    public BVHardwareResourceDto getBehaviorHardwareConfigurationResource(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorHardwareConfigurationResource]: Getting the hardware configuration from behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "getBehaviorHardwareConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::getResourceNotFoundError);

        LOG.debug("[{}] -> [getBehaviorHardwareConfigurationResource]: Getting the available hardware packs from type PACK_NOVA_BEHAVIOR",
                this.getClass().getSimpleName());

        List<HardwarePack> availableHardwarePack = this.hardwarePackRepository.findAllByHardwarePackType(HardwarePackType.PACK_NOVA_BEHAVIOR);
        BVHardwareResourceDto bvHardwareResourceDto = new BVHardwareResourceDto();
        bvHardwareResourceDto.setHardwareOptions(availableHardwarePack.stream().map(this::convertHardwarePackToDto).toArray(BVConfigurableHardware[]::new));

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();
            BehaviorService behaviorService = behaviorServiceConfiguration.getBehaviorService();
            BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();
            int productId = behaviorVersion.getProduct().getId();

            // Check initiatives for product
            checkProductInitiatives(productId);

            LOG.info("[{}] -> [getBehaviorHardwareConfigurationResource]: Converting to DTO response...",
                    this.getClass().getSimpleName());

            bvHardwareResourceDto.setProductId(productId);
            bvHardwareResourceDto.setBehaviorVersionId(behaviorVersion.getId());
            bvHardwareResourceDto.setBehaviorServiceId(behaviorService.getId());
            if (behaviorServiceConfiguration.getHardwarePack() != null)
            {
                bvHardwareResourceDto.setSelectedHardware(convertHardwarePackToDto(behaviorServiceConfiguration.getHardwarePack()));
            }
        }
        else
        {
            bvHardwareResourceDto.setProductId(-1);
        }

        LOG.info("[{}] -> [getBehaviorHardwareConfigurationResource]: Generated response from persisted data [{}]",
                this.getClass().getSimpleName(), bvHardwareResourceDto);

        return bvHardwareResourceDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BVJVMResourceDto getBehaviorJvmConfigurationResource(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorJvmConfigurationResource]: Getting the JVM configuration from behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "[{}] -> [getBehaviorJvmConfigurationResource]: Bad behavior service id provided");
        checkBehaviorServiceExists(behaviorServiceId, this::getResourceNotFoundError);

        BVJVMResourceDto jvmResourceDto = new BVJVMResourceDto();

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();
            BehaviorService behaviorService = behaviorServiceConfiguration.getBehaviorService();
            BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();
            int productId = behaviorVersion.getProduct().getId();

            // Check initiatives for product
            checkProductInitiatives(productId);

            LOG.info("[{}] -> [getBehaviorJvmConfigurationResource]: Converting to DTO response...",
                    this.getClass().getSimpleName());

            jvmResourceDto.setProductId(productId);
            jvmResourceDto.setBehaviorVersionId(behaviorVersion.getId());
            jvmResourceDto.setBehaviorServiceId(behaviorService.getId());
            jvmResourceDto.setSelectedJvmMemoryPercentage(this.convertResourceToJvmConfigurable(behaviorServiceConfiguration.getJvmMemory()));
        }
        else
        {
            jvmResourceDto.setProductId(-1);
        }

        LOG.info("[{}] -> [getBehaviorJvmConfigurationResource]: Generated response from persisted data [{}]",
                this.getClass().getSimpleName(), jvmResourceDto);

        return jvmResourceDto;
    }

    @Override
    @Transactional(readOnly = true)
    public BVPropertiesResourceDto getBehaviorPropertiesConfigurationResource(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorPropertiesConfigurationResource]: Getting the properties configuration from behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "[{}] -> [getBehaviorPropertiesConfigurationResource]: Bad behavior service id provided");
        checkBehaviorServiceExists(behaviorServiceId, this::getResourceNotFoundError);

        BVPropertiesResourceDto bvPropertiesResourceDtoToReturn = new BVPropertiesResourceDto();

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();
            BehaviorService behaviorService = behaviorServiceConfiguration.getBehaviorService();
            BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();
            int productId = behaviorVersion.getProduct().getId();

            // Check initiatives for product
            checkProductInitiatives(productId);

            List<BVConfigurableProperty> completePropertiesList;
            if (!behaviorServiceConfiguration.getPropertyValueList().isEmpty())
            {
                LOG.debug("[{}] -> [getBehaviorPropertiesConfigurationResource]: Loading service property values", getClass().getSimpleName());

                completePropertiesList = behaviorServiceConfiguration.getPropertyValueList()
                        .stream().filter(behaviorPropertyValue -> behaviorPropertyValue.getBehaviorProperty().getSourceType() == TEMPLATE)
                        .map(this::convertBehaviorPropertyValueToConfigurablePropertyDto).collect(Collectors.toList());
            }
            else
            {
                LOG.info("[{}] -> [getBehaviorPropertiesConfigurationResource]: No behavior property values found for configuration. Loading service properties...",
                        this.getClass().getSimpleName());

                completePropertiesList = behaviorService.getProperties()
                        .stream().filter(property -> property.getSourceType() == TEMPLATE).map(this::convertBehaviorPropertyToConfigurablePropertyDto).collect(Collectors.toList());
            }
            LOG.debug("[{}] -> [getBehaviorPropertiesConfigurationResource]: Adding properties from connectors", getClass().getSimpleName());
            completePropertiesList.addAll(convertConnectorPropToDto(behaviorServiceConfiguration));
            loadBehaviorPropertiesFromService(behaviorService, bvPropertiesResourceDtoToReturn, behaviorVersion, completePropertiesList);
        }
        else
        {
            Optional<BehaviorService> behaviorServiceOptional = behaviorServiceRepository.findById(behaviorServiceId);
            if (behaviorServiceOptional.isPresent())
            {
                BehaviorVersion behaviorVersion = behaviorServiceOptional.get().getBehaviorSubsystem().getBehaviorVersion();
                List<BVConfigurableProperty> behaviorPropertyList = behaviorServiceOptional.get().getProperties()
                        .stream().filter(property -> property.getSourceType() == TEMPLATE).map(this::convertBehaviorPropertyToConfigurablePropertyDto).collect(Collectors.toList());
                loadBehaviorPropertiesFromService(behaviorServiceOptional.get(), bvPropertiesResourceDtoToReturn, behaviorVersion, behaviorPropertyList);
            }
            else
            {
                throw new NovaException(BehaviorError.getBadParametersError(behaviorServiceId));
            }
        }

        LOG.info("[{}] -> [getBehaviorPropertiesConfigurationResource]: Generated response from persisted data [{}]",
                this.getClass().getSimpleName(), bvPropertiesResourceDtoToReturn);

        return bvPropertiesResourceDtoToReturn;
    }

    @Override
    @Transactional(readOnly = true)
    public BVBehaviorServiceConfigurationSummary getBehaviorServiceSummaryResources(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorServiceSummaryResources]: Getting the summary resources from behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "getBehaviorServiceSummaryResources");
        checkBehaviorServiceExists(behaviorServiceId, this::getResourceNotFoundError);

        BVBehaviorServiceConfigurationSummary bvResourcesDtoToReturn = new BVBehaviorServiceConfigurationSummary();

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId);
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();
            BehaviorService behaviorService = behaviorServiceConfiguration.getBehaviorService();
            BehaviorVersion behaviorVersion = behaviorService.getBehaviorSubsystem().getBehaviorVersion();
            int productId = behaviorVersion.getProduct().getId();

            LOG.info("[{}] -> [getBehaviorServiceSummaryResources]: Generating the DTO to send...",
                    this.getClass().getSimpleName());

            // Info fields
            bvResourcesDtoToReturn.setProductId(productId);
            bvResourcesDtoToReturn.setBehaviorVersionId(behaviorVersion.getId());
            bvResourcesDtoToReturn.setBehaviorServiceId(behaviorService.getId());
            bvResourcesDtoToReturn.setBehaviorServiceName(behaviorServiceConfiguration.getBehaviorService().getServiceName());

            // Complex objects
            // It's correct to send only the first, reused method
            if (behaviorServiceConfiguration.getHardwarePack() != null)
            {
                bvResourcesDtoToReturn.setBehaviorServiceHardware(convertHardwarePackToDto(behaviorServiceConfiguration.getHardwarePack()));
            }

            // Simple field objects
            bvResourcesDtoToReturn.setBehaviorServiceJvm(this.convertResourceToJvmConfigurable(behaviorServiceConfiguration.getJvmMemory()));

            if (!behaviorServiceConfiguration.getBsConfigurationFilesystemList().isEmpty())
            {
                bvResourcesDtoToReturn.setBehaviorServiceFilesystems(
                        behaviorServiceConfiguration.getBsConfigurationFilesystemList().stream()
                                .map(this::convertBSConfigurationFileSystemToConfigurableDto).toArray(BVConfigurableFilesystem[]::new));
            }
            if (!behaviorServiceConfiguration.getLogicalConnectorList().isEmpty())
            {
                bvResourcesDtoToReturn.setBehaviorServiceConnectors(
                        behaviorServiceConfiguration.getLogicalConnectorList().stream()
                                .map(this::convertLogicalConnectorToDTO).toArray(BVConfigurableConnector[]::new));
            }
            bvResourcesDtoToReturn.setBehaviorProperties(behaviorServiceConfiguration.getPropertyValueList()
                    .stream().map(this::convertBehaviorPropertyValueToConfigurablePropertyDto).toArray(BVConfigurableProperty[]::new));

            // It's correct to send only the first, due to is mandatory to only have one broker. In the future could be more
            //bvResourcesDtoToReturn.setBehaviorServiceBrokers(Arrays.stream(this.convertListToBrokerConfigurable(behaviorServiceConfiguration.getBrokerResource())).findFirst().orElse(null));
        }
        else
        {
            bvResourcesDtoToReturn.setProductId(-1);
        }

        LOG.info("[{}] -> [getBehaviorServiceSummaryResources]: Dto generated [{}]", this.getClass().getSimpleName(),
                bvResourcesDtoToReturn);

        return bvResourcesDtoToReturn;
    }

    @Override
    public BVBehaviorVersionSummaryInfoDTO getBehaviorVersion(Integer behaviorVersionId)
    {
        LOG.info("[{}] -> [getBehaviorVersion]: Getting concrete information about the behavior service cost configuration from behavior version [{}]",
                this.getClass().getSimpleName(), behaviorVersionId);

        checkResourceId(behaviorVersionId, "[{}] -> [getBehaviorVersion]: Bad behavior version id provided");

        // Get the information persisted in the database for the behavior subsystem
        BehaviorVersion behaviorVersion = this.behaviorVersionRepository
                .findById(behaviorVersionId)
                .orElseThrow(() -> new NovaException(BehaviorError.getResourceNotFoundError("behaviorVersions", String.join(", ", "behaviorVersionId", behaviorVersionId.toString()))));


        return this.convertBehaviorVersionToSummaryDto(behaviorVersion);
    }

    @Override
    @Transactional
    public BVBehaviorVersionSubsystemDTO[] getBehaviorVersionsSubsystems(Integer behaviorVersionId)
    {
        LOG.info("[{}] -> [getBehaviorVersionsSubsystems]: Getting the behavior versions subsystems from behavior version [{}]",
                this.getClass().getSimpleName(), behaviorVersionId);

        checkResourceId(behaviorVersionId, "[{}] -> [getBehaviorServiceSummaryResources]: Bad behavior version id provided");

        // Get the information persisted in the database for the behavior subsystem
        List<BehaviorSubsystem> behaviorSubsystems = this.behaviorSubsystemRepository.findByBehaviorVersionId(behaviorVersionId)
                .orElseThrow(() -> new NovaException(BehaviorError.getResourceNotFoundError("behaviorSubsystems", String.join(", ", "behaviorVersionId", behaviorVersionId.toString()))));

        return this.convertBehaviorSubsystemsToDto(behaviorSubsystems);
    }

    //#################### UPDATES ####################

    //TODO@JVS -> At the moment not brokers available
    @Override
    public void updateBehaviorBrokerConfigurationResource(BVConfigurableBroker[] resourceDtoToUpdate, Integer behaviorServiceId)
    {
        // At the moment, throw an information error
        throw new NovaException(BehaviorError.getNoResourceAvailableError("BROKERS Resource"));

        /*
        // First gets the brokers by all the ids in the DTO
        // The return list is the one to set as new broker in the configuration
        List<Broker> alreadyPersistedBrokersToSet = this.brokerRepository.findByIdIn(Arrays.stream(resourceDtoToUpdate).map(BVConfigurableBroker::getId).collect(Collectors.toSet()));

        //Get the actual configuration
        BehaviorService persistedBehaviorService = this.behaviorServiceRepository.findById(behaviorServiceId).get();

        // Not needed, only got for traces
        List<Broker> alreadyConfiguredBrokers = persistedBehaviorService.getBehaviorServiceConfiguration().getBrokerResource();

        LOG.debug("[{}] -> [updateBehaviorBrokerConfigurationResource]: Replacing brokers from [{}] to [{}]",
                this.getClass().getSimpleName(), alreadyConfiguredBrokers, alreadyPersistedBrokersToSet);

        // Replacing brokers
        persistedBehaviorService.getBehaviorServiceConfiguration().setBrokerResource(alreadyPersistedBrokersToSet);

        // Saving in database
        this.behaviorServiceRepository.saveAndFlush(persistedBehaviorService);
        */
    }

    @Override
    @Transactional
    public void updateBehaviorConnectorsConfigurationResource(BVConfigurableConnector[] resourceDtoToUpdate, Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [updateBehaviorConnectorsConfigurationResource]: Updating connectors configuration from behavior service id [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "updateBehaviorConnectorsConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::behaviorServiceNotFoundError);

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional =
                behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId, EDITING);
        // The return list is the one to set as new connector in the configuration
        List<LogicalConnector> alreadyPersistedConnectorsToSet = this.logicalConnectorRepository
                .findByIdIn(Arrays.stream(resourceDtoToUpdate).map(BVConfigurableConnector::getId).collect(Collectors.toSet()));

        BehaviorServiceConfiguration behaviorServiceConfiguration;
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();

            // Not needed, only got for traces
            List<LogicalConnector> alreadyConfiguredConnector = behaviorServiceConfiguration.getLogicalConnectorList();

            LOG.debug("[{}] -> [updateBehaviorConnectorsConfigurationResource]: Replacing connectors from [{}] to [{}]",
                    this.getClass().getSimpleName(), alreadyConfiguredConnector, alreadyPersistedConnectorsToSet);

            behaviorServiceConfiguration.setLastModified(Calendar.getInstance());
        }
        else
        {
            LOG.info("[{}] -> [updateBehaviorConnectorsConfigurationResource]: There is no configuration with EDITING status for behavior service with id: {}. Creating new one.",
                    this.getClass().getSimpleName(), behaviorServiceId);

            behaviorServiceConfiguration = newBehaviorServiceConfiguration(behaviorServiceId);
        }

        behaviorServiceConfiguration.getLogicalConnectorList().clear();
        behaviorServiceConfiguration.getLogicalConnectorList().addAll(alreadyPersistedConnectorsToSet);

        saveAndFlushElement(behaviorServiceConfigurationRepository, behaviorServiceConfiguration);

        // Emit update resource Activity
        emitActivity(behaviorServiceConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion(), ActivityAction.CONNECTOR_CONFIGURED);

        LOG.info("[{}] -> [updateBehaviorConnectorsConfigurationResource]: updated connectors configuration for behavior service: [{}].",
                this.getClass().getSimpleName(), behaviorServiceId);
    }

    @Override
    @Transactional
    public void updateBehaviorFilesystemsConfigurationResource(BVConfigurableFilesystem[] resourceDtoToUpdate, Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [updateBehaviorFilesystemsConfigurationResource]: Updating filesystems configuration from behavior service id [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "updateBehaviorFilesystemsConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::behaviorServiceNotFoundError);

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional =
                behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId, EDITING);

        // Obtains all the filesystems from the configured in the received DTO
        List<Filesystem> filesystemsToSet =
                this.filesystemRepository.findByIdIn(Arrays.stream(resourceDtoToUpdate).map(BVConfigurableFilesystem::getId).collect(Collectors.toSet()));

        BehaviorServiceConfiguration behaviorServiceConfiguration;
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();

            // Not needed, only got for traces
            List<Filesystem> alreadyConfiguredFilesystems = this.bsConfigurationFilesystemRepository
                    .findByBehaviorServiceConfigurationId(behaviorServiceConfiguration.getId()).stream()
                    .map(BSConfigurationFilesystem::getFilesystem).collect(Collectors.toList());

            LOG.debug("[{}] -> [updateBehaviorFilesystemsConfigurationResource]: Replacing filesystems from [{}] to [{}]",
                    this.getClass().getSimpleName(), alreadyConfiguredFilesystems, filesystemsToSet);

            behaviorServiceConfiguration.setLastModified(Calendar.getInstance());
        }
        else
        {
            LOG.info("[{}] -> [updateBehaviorFilesystemsConfigurationResource]: There is no configuration with EDITING status for behavior service with id: {}. Creating new one.",
                    this.getClass().getSimpleName(), behaviorServiceId);

            behaviorServiceConfiguration = newBehaviorServiceConfiguration(behaviorServiceId);
            saveAndFlushElement(behaviorServiceConfigurationRepository, behaviorServiceConfiguration);
        }

        behaviorServiceConfiguration.setBsConfigurationFilesystemList(mergeFileSystems(behaviorServiceConfiguration, filesystemsToSet, resourceDtoToUpdate));

        LOG.debug("[{}] -> [updateBehaviorFilesystemsConfigurationResource]: bsConfigurationFilesystemList size [{}]",
                this.getClass().getSimpleName(), behaviorServiceConfiguration.getBsConfigurationFilesystemList().size());

        saveAndFlushElement(behaviorServiceConfigurationRepository, behaviorServiceConfiguration);

        // Emit update resource Activity
        emitActivity(behaviorServiceConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion(), ActivityAction.FILESYSTEM_CONFIGURED);

        LOG.info("[{}] -> [updateBehaviorFilesystemsConfigurationResource]: updated filesystems configuration for behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);
    }

    @Override
    @Transactional
    public void updateBehaviorHardwareConfigurationResource(BVConfigurableHardware resourceDtoToUpdate, Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [updateBehaviorHardwareConfigurationResource]: Updating hardware configuration from behavior service id [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "updateBehaviorHardwareConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::behaviorServiceNotFoundError);

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional =
                behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId, EDITING);

        // The return list is the one to set as new hardware in the configuration. It could not be null due to is a catalog in DB
        HardwarePack alreadyPersistedHardwareToSet =
                this.hardwarePackRepository.findById(resourceDtoToUpdate.getPackId())
                        .orElseThrow(() -> new NovaException(BehaviorError.getNoResourceAvailableError("Pack Hardware")));

        BehaviorServiceConfiguration behaviorServiceConfiguration;
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();

            // Not needed, only got for traces
            HardwarePack alreadyConfiguredHardware = behaviorServiceConfiguration.getHardwarePack();

            LOG.debug("[{}] -> [updateBehaviorHardwareConfigurationResource]: Replacing hardware from [{}] to [{}]",
                    this.getClass().getSimpleName(), alreadyConfiguredHardware, alreadyPersistedHardwareToSet);

            behaviorServiceConfiguration.setLastModified(Calendar.getInstance());
        }
        else
        {
            LOG.info("[{}] -> [updateBehaviorHardwareConfigurationResource]: There is no configuration with EDITING status for behavior service with id: {}. Creating new one.",
                    this.getClass().getSimpleName(), behaviorServiceId);

            behaviorServiceConfiguration = newBehaviorServiceConfiguration(behaviorServiceId);
        }

        behaviorServiceConfiguration.setHardwarePack(alreadyPersistedHardwareToSet);
        saveAndFlushElement(behaviorServiceConfigurationRepository, behaviorServiceConfiguration);

        // Update potential cost for hardware configure
        this.updateBehaviorVersionPotentialCost(behaviorServiceConfiguration, alreadyPersistedHardwareToSet);

        // Emit update resource Activity
        emitActivity(behaviorServiceConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion(), ActivityAction.HARDWARE_CONFIGURED);

        LOG.info("[{}] -> [updateBehaviorHardwareConfigurationResource]: updated hardware configuration for behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);
    }

    @Override
    @Transactional
    public void updateBehaviorJvmConfigurationResource(BVConfigurableJvm resourceDtoToUpdate, Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [updateBehaviorJvmConfigurationResource]: Updating jvm configuration from behavior service id [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "updateBehaviorJvmConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::behaviorServiceNotFoundError);

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional =
                behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId, EDITING);

        BehaviorServiceConfiguration behaviorServiceConfiguration;
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();

            // Not needed, only got for traces
            Integer alreadyConfiguredJvm = behaviorServiceConfiguration.getJvmMemory();

            LOG.debug("[{}] -> [updateBehaviorJvmConfigurationResource]: Replacing JVM configuration from [{}] to [{}]",
                    this.getClass().getSimpleName(), alreadyConfiguredJvm, resourceDtoToUpdate.getJvmPercentage());
        }
        else
        {
            LOG.info("[{}] -> [updateBehaviorJvmConfigurationResource]: There is no configuration with EDITING status for behavior service with id: {}. Creating new one.",
                    this.getClass().getSimpleName(), behaviorServiceId);

            behaviorServiceConfiguration = newBehaviorServiceConfiguration(behaviorServiceId);
        }

        behaviorServiceConfiguration.setJvmMemory(resourceDtoToUpdate.getJvmPercentage());
        saveAndFlushElement(behaviorServiceConfigurationRepository, behaviorServiceConfiguration);

        // Emit update resource Activity
        emitActivity(behaviorServiceConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion(), ActivityAction.JVM_CONFIGURED);

        LOG.info("[{}] -> [updateBehaviorJvmConfigurationResource]: updated JVM configuration for behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);
    }

    @Override
    @Transactional
    public void updateBehaviorPropertiesConfigurationResource(BVConfigurableProperty[] resourceDtoToUpdate, Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [updateBehaviorPropertiesConfigurationResource]: Updating property configuration from behavior service id [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "updateBehaviorPropertiesConfigurationResource");
        checkBehaviorServiceExists(behaviorServiceId, this::behaviorServiceNotFoundError);

        // Get the information persisted in the database for the already configured connector
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional =
                behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId, EDITING);

        BehaviorServiceConfiguration behaviorServiceConfiguration;
        // If configuration with EDITING status exists, update or create property values associated
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();
            updateOrAddPropertyValues(resourceDtoToUpdate, behaviorServiceId, behaviorServiceConfiguration);
        }
        // If no configuration with EDITING status exists, creates a new configuration or clone an existing with CONFIGURED status
        else
        {
            LOG.info("[{}] -> [updateBehaviorPropertiesConfigurationResource]: There is no configuration with EDITING status for behavior service with id: {}. Creating new one.",
                    this.getClass().getSimpleName(), behaviorServiceId);

            Optional<BehaviorService> behaviorServiceOptional = behaviorServiceRepository.findById(behaviorServiceId);
            if (behaviorServiceOptional.isPresent())
            {
                behaviorServiceConfiguration = newBehaviorServiceConfiguration(behaviorServiceId);
                updateOrAddPropertyValues(resourceDtoToUpdate, behaviorServiceId, behaviorServiceConfiguration);
            }
            else
            {
                throw new NovaException(BehaviorError.getBadParametersError(behaviorServiceId));
            }
        }
        // behaviorServiceConfiguration.setLastModified(Calendar.getInstance());
        saveAndFlushElement(behaviorServiceConfigurationRepository, behaviorServiceConfiguration);

        // Emit update resource Activity
        emitActivity(behaviorServiceConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion(), PROPERTIES_CONFIGURED);

        LOG.info("[{}] -> [updateBehaviorPropertiesConfigurationResource]: updated properties for behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);
    }

    @Transactional
    public BVBehaviorServiceBudgetInfo getBehaviorVersionCostsInfo(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [getBehaviorVersionCostsInfo]: Getting costs information about behavior service [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        // First gets the service from the id provided
        BehaviorServiceConfiguration persistedBehaviorServiceConfig = this.behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId,
                        BehaviorConfigurationStatus.CONFIGURED)
                .orElseThrow(() -> new NovaException(BehaviorError.getBadParametersError()));

        BVBehaviorServiceBudgetInfo bvBehaviorServiceBudgetInfoToReturn = new BVBehaviorServiceBudgetInfo();
        bvBehaviorServiceBudgetInfoToReturn.setBehaviorServiceId(persistedBehaviorServiceConfig.getBehaviorService().getId());
        bvBehaviorServiceBudgetInfoToReturn.setBehaviorServiceName(persistedBehaviorServiceConfig.getBehaviorService().getServiceName());
        bvBehaviorServiceBudgetInfoToReturn.setBehaviorVersionId(persistedBehaviorServiceConfig.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion().getId());
        bvBehaviorServiceBudgetInfoToReturn.setBehaviorServiceHardwarePackConfigured(persistedBehaviorServiceConfig.getHardwarePack().getId());

        LOG.debug("[{}] -> [getBehaviorVersionCostsInfo]: Got information [{}] about behavior service with id [{}]",
                this.getClass().getSimpleName(), bvBehaviorServiceBudgetInfoToReturn, behaviorServiceId);

        return bvBehaviorServiceBudgetInfoToReturn;
    }


    @Override
    public BehaviorServiceConfiguration configureBehaviorEnvironment(Integer behaviorServiceId)
    {
        LOG.info("[{}] -> [configureBehaviorEnvironment]: Saving behavior service configuration for behavior service id: [{}]",
                this.getClass().getSimpleName(), behaviorServiceId);

        checkResourceId(behaviorServiceId, "configureBehaviorEnvironment");
        checkBehaviorServiceExists(behaviorServiceId, this::behaviorServiceNotFoundError);

        // First gets the latest configuration with status EDITING, to check if we have a new possible edited configuration

        BehaviorServiceConfiguration configuration = behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId)
                .orElseThrow(() -> new NovaException(BehaviorError.getNoResourceAvailableError("Behavior configuration")));
        configuration.setLastModified(Calendar.getInstance());
        if (configuration.getStatus() == EDITING)
        {
            configuration.setStatus(CONFIGURED);

            LOG.info("[{}] -> [configureBehaviorEnvironment]: Configuration with id {} saved with with status CONFIGURED",
                    this.getClass().getSimpleName(), configuration.getId());
        }
        saveLogicalConnectorListProperties(configuration);
        LOG.info("[{}] -> [configureBehaviorEnvironment]: Configuration with id {} saved with new property info",
                this.getClass().getSimpleName(), configuration.getId());

        return behaviorServiceConfigurationRepository.save(configuration);

    }

    @Override
    public BVExecutionInfoDto getBehaviorExecutionInfo(Integer behaviorServiceId)
    {
        BVExecutionInfoDto bvExecutionInfoDtoToReturn = new BVExecutionInfoDto();
        // 1. First get the behavior service
        BehaviorService behaviorService = this.behaviorServiceRepository.findById(behaviorServiceId).orElseThrow(() -> new NovaException(BehaviorError.getBadParametersError()));

        // 2.1 Get the deploy release versions for the product
        List<ReleaseVersion> releaseList = this.releaseVersionRepository.findAllBehaviorReleasesDeployedInPre(behaviorService.getBehaviorSubsystem().getBehaviorVersion().getProduct().getId());
        bvExecutionInfoDtoToReturn.setReleaseVersionOptions(this.convertReleaseListIntoDto(releaseList));

        // 2.2 Get the release version set in yml from behavior service (default one)
        BVReleaseVersionInfoDto bvReleaseVersionInfoDto = new BVReleaseVersionInfoDto();
        bvReleaseVersionInfoDto.setReleaseVersionId(behaviorService.getReleaseVersion().getId());
        bvReleaseVersionInfoDto.setReleaseVersionName(behaviorService.getReleaseVersion().getVersionName());

        bvExecutionInfoDtoToReturn.setReleaseVersionSelected(bvReleaseVersionInfoDto);

        // 2.3 Get the tags from the tag entity -> Gets all the names for the tags
        bvExecutionInfoDtoToReturn.setTagOptions(behaviorService.getBehaviorTagList().stream()
                .map(BehaviorTag::getName)
                .toArray(String[]::new));

        return bvExecutionInfoDtoToReturn;
    }

    @Transactional
    @Override
    public void executeBehaviorConfiguration(final String userCode, final BVBehaviorParamsDTO behaviorParamsDTO, final Integer behaviorServiceId) throws NovaException
    {
        // 1. Save all configuration (with connector values)
        BehaviorServiceConfiguration bsConfiguration = this.configureBehaviorEnvironment(behaviorServiceId);

        Product product = bsConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion().getProduct();
        // 2. Validate user permission
        this.validateUserPermission(userCode, product.getId(), Constants.START_BEHAVIOR_INSTANCE);

        // 3. Check budgets to execute a behavior test
        this.validateProductBudgets(bsConfiguration);

        // 4. Validate if behavior service is not running
        this.iBehaviorVersionValidator.validateBehaviorServiceConfigurationExecution(bsConfiguration);

        // 5.Registering APIs in API Gateway Service.
        this.apiGatewayService.createPublication(bsConfiguration);

        // 6.Creates docker Keys for current plan services
        this.iBehaviorApiGatewayService.generateDockerKey(bsConfiguration);

        // 7. Call to Deployment Manager service to create and start the container.
        this.behaviorManagerClient.startBehaviorConfiguration(this.iBehaviorServiceDtoBuilder.buildBMBehaviorParamsDTO(behaviorParamsDTO), bsConfiguration.getId());
    }

    @Override
    public void stopBehaviorConfigurationExecution(String ivUser, Integer behaviorInstanceId)
    {
        // 0. Get the complete behavior instance from id
        BehaviorInstance behaviorInstance = this.behaviorInstanceRepository.findById(behaviorInstanceId.intValue())
                .orElseThrow(() -> new NovaException(BehaviorError.getBehaviorInstanceNotFoundError(behaviorInstanceId.intValue())));

        // 1. Validate user permission
        this.validateUserPermission(ivUser,
                behaviorInstance.getBehaviorServiceConfiguration().getBehaviorService().getReleaseVersion().getRelease().getProduct().getId(),
                Constants.STOP_BEHAVIOR_INSTANCE);

        // 2. Validate behavior service is running
        this.iBehaviorVersionValidator.validateBehaviorInstanceStop(behaviorInstance);

        // 3. Stop the container in the behaviormanagerapi (DeploymentManager) -
        this.behaviorManagerClient.stopBehaviorConfiguration(behaviorInstanceId);

        // 4. Call the batchagentapi to change the status for the execution instance
        this.behaviorAgentApiClient.stopBehaviorInstance(behaviorInstanceId);

        // 5. Call the budgets service to discount version cost (STOPPING action)
        this.productBudgetsClient.updateBehaviorVersionCurrentCost(this.generatePBBehaviorExecutionInfo(behaviorInstance), BehaviorAction.STOPPING.name());
    }

    @Override
    @Transactional
    public void removeBehaviorInstanceExecution(Integer behaviorInstanceId)
    {
        LOG.info("[{}] -> [removeBehaviorInstanceExecution]: Removing behavior execution for behavior instance id: [{}]",
                this.getClass().getSimpleName(), behaviorInstanceId);

        // Delete behavior execution from Behavior Agent
        behaviorAgentApiClient.removeBehaviorInstanceExecution(behaviorInstanceId);

        // Delete behavior instance from DB
        this.behaviorInstanceRepository.deleteById(behaviorInstanceId);
    }

    @Override
    @Transactional(readOnly = true)
    public BVExecutionPageableDTO getBehaviorServiceExecutions(Integer behaviorServiceId, Long behaviorServiceExecFrom, Long behaviorServiceExecTo, Integer pageNumber, Integer pageSize)
    {
        LOG.info("[{}] -> [getBehaviorServiceExecutions]: Getting behavior service executions for behavior service id: [{}], pageNumber: {}, pageSize: {}",
                getClass().getSimpleName(), behaviorServiceId, pageNumber, pageSize);

        checkResourceId(behaviorServiceId, "getBehaviorServiceExecutions");
        checkBehaviorServiceExists(behaviorServiceId, this::getResourceNotFoundError);

        // Get the behavior service instances
        Page<BehaviorInstance> behaviorInstancesPage = this.behaviorInstanceRepository.findByBehaviorServiceId(behaviorServiceId, PageRequest.of(pageNumber, pageSize));
        Set<BehaviorServiceConfiguration> configurationSet = getDistinctConfigurations(behaviorInstancesPage.getContent());

        // Get executions from Nova agent finding by configuration ids
        BABehaviorExecutionDTO[] behaviorExecutionDTOArray = findBehaviorExecutionsByConfigurationSet(configurationSet);
        return newExecutionPageableData(behaviorInstancesPage, pageNumber, pageSize, behaviorExecutionDTOArray);
    }

    @Override
    @Transactional(readOnly = true)
    public BVExecutionPageableDTO getBehaviorVersionExecutions(Integer behaviorVersionId, String behaviorServiceName, Integer pageSize, Integer pageNumber)
    {
        LOG.info("[{}] -> [getBehaviorVersionExecutions]: Getting behavior version executions for behavior version id: [{}], " +
                        "behavior service name: [{}]",
                this.getClass().getSimpleName(), behaviorVersionId, behaviorServiceName);

        checkResourceId(behaviorVersionId, "getBehaviorVersionExecutions");
        checkBehaviorVersionExists(behaviorVersionId, this::getResourceNotFoundError);

        BehaviorVersion behaviorVersion = behaviorVersionRepository.findById(behaviorVersionId)
                .orElseThrow(() -> new NovaException(
                        BehaviorError.getResourceNotFoundError("Behavior Version", behaviorVersionId.toString())));

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<BehaviorInstance> behaviorInstancesPage;

        if (StringUtils.isNotBlank(behaviorServiceName))
        {
            LOG.info("[{}] -> [getBehaviorVersionExecutions]: Finding behavior instances from DB for behavior version: [{}] and behavior service name: [{}]",
                    getClass().getSimpleName(), behaviorVersionId, behaviorServiceName);
            behaviorInstancesPage =
                    behaviorInstanceRepository.findInstancesByBehaviorVersionAndBehaviorServiceName(behaviorVersion.getSubsystems(), behaviorServiceName, pageable);
        }
        else
        {
            LOG.info("[{}] -> [getBehaviorVersionExecutions]: Finding behavior instances from DB for behavior version: [{}]",
                    getClass().getSimpleName(), behaviorVersionId);
            behaviorInstancesPage =
                    behaviorInstanceRepository.findInstancesByBehaviorVersion(behaviorVersion.getSubsystems(), pageable);
        }

        Set<BehaviorServiceConfiguration> configurationSet = getDistinctConfigurations(behaviorInstancesPage.getContent());

        LOG.info("[{}] -> [getBehaviorVersionExecutions]: Finding executions from Batch Agent for behavior version: [{}]", getClass().getSimpleName(), behaviorVersionId);

        // Get executions from Nova agent finding by configuration ids
        BABehaviorExecutionDTO[] behaviorExecutionDTOArray = findBehaviorExecutionsByConfigurationSet(configurationSet);
        return newExecutionPageableData(behaviorInstancesPage, pageNumber, pageSize, behaviorExecutionDTOArray);
    }


    //#################### PRIVATE METHODS ####################

    private BABehaviorExecutionDTO[] findBehaviorExecutionsByConfigurationSet(Set<BehaviorServiceConfiguration> configurationSet)
    {
        if (!configurationSet.isEmpty())
        {
            LOG.info("[{}] -> [findBehaviorExecutions]: Finding behavior executions from DB for behavior configurations: [{}]",
                    getClass().getSimpleName(), configurationSet);
            // Get executions from batch agent for required instances
            return behaviorAgentApiClient.getBehaviorExecutionByBehaviorConfigurationList(configurationSet.stream().mapToInt(AbstractEntity::getId).toArray());
        }
        return new BABehaviorExecutionDTO[0];
    }

    private Set<BehaviorServiceConfiguration> getDistinctConfigurations(List<BehaviorInstance> behaviorInstanceList) {
        return behaviorInstanceList.stream()
                .map(BehaviorInstance::getBehaviorServiceConfiguration)
                .collect(Collectors.toSet());
    }

    private BVExecutionPageableDTO newExecutionPageableData(Page<BehaviorInstance> behaviorInstancePage, Integer pageNumber, Integer pageSize, BABehaviorExecutionDTO[] behaviorExecutionDTOArray)
    {
        BVExecutionPageableDTO executionPageableDTO = new BVExecutionPageableDTO();
        executionPageableDTO.setPageNumber(pageNumber);
        executionPageableDTO.setSize(pageSize);
        executionPageableDTO.setExecutions(convertBehaviorServiceExecutionsToDto(behaviorInstancePage.getContent(), behaviorExecutionDTOArray));
        executionPageableDTO.setNumberOfElements(executionPageableDTO.getExecutions().length);
        executionPageableDTO.setTotalElements(behaviorInstancePage.getTotalElements());
        return executionPageableDTO;
    }

    private PBBehaviorExecutionInfo generatePBBehaviorExecutionInfo(BehaviorInstance behaviorInstance)
    {
        PBBehaviorExecutionInfo pbBehaviorExecutionInfo = new PBBehaviorExecutionInfo();

        // IMPORTANT -> The budgets are updated with the service associated to the instance, not with the instance itself
        pbBehaviorExecutionInfo.setProductExecutedId(behaviorInstance.getBehaviorServiceConfiguration().getBehaviorService().getBehaviorSubsystem().getBehaviorVersion().getProduct().getId());
        pbBehaviorExecutionInfo.setBehaviorServiceExecutedId(behaviorInstance.getBehaviorServiceConfiguration().getBehaviorService().getId());
        pbBehaviorExecutionInfo.setHasEnded(true);

        return pbBehaviorExecutionInfo;
    }

    /**
     * Emit an activity when a resource is updated.
     *
     * @param behaviorVersion Behavior version.
     * @param activityAction  Activity action.
     */
    private void emitActivity(BehaviorVersion behaviorVersion, ActivityAction activityAction)
    {
        novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(behaviorVersion.getProduct().getId(), ActivityScope.BEHAVIOR_VERSION, activityAction)
                .entityId(behaviorVersion.getId())
                .addParam(BEHAVIOR_VERSION_NAME, behaviorVersion.getVersionName())
                .addParam(BEHAVIOR_VERSION_DESCRIPTION, behaviorVersion.getDescription())
                .build());
    }

    /**
     * Update existing property values or creates them if they don't exist.
     *
     * @param configurableProperties       Property values to save.
     * @param behaviorServiceId            Behavior service id.
     * @param behaviorServiceConfiguration Behavior service configuration.
     */
    private void updateOrAddPropertyValues(BVConfigurableProperty[] configurableProperties, Integer behaviorServiceId, BehaviorServiceConfiguration behaviorServiceConfiguration)
    {
        if (!behaviorServiceConfiguration.getPropertyValueList().isEmpty())
        {
            LOG.info("[{}] -> [updateBehaviorPropertiesConfigurationResource]: Updating property values [{}]",
                    this.getClass().getSimpleName(), behaviorServiceId);
            updateExistingPropertyValueList(behaviorServiceConfiguration.getPropertyValueList(), configurableProperties);
        }
        else
        {
            LOG.info("[{}] -> [updateBehaviorPropertiesConfigurationResource]: Saving new property values [{}]",
                    this.getClass().getSimpleName(), behaviorServiceId);
            List<BehaviorProperty> behaviorPropertyList = behaviorServiceConfiguration.getBehaviorService().getProperties();
            behaviorServiceConfiguration.getPropertyValueList().addAll(
                    convertConfigurablePropertyToBehaviorPropertyValue(configurableProperties, behaviorPropertyList, behaviorServiceConfiguration));
        }
    }

    /**
     * Updates the existing property values with the new values.
     *
     * @param propertyValueList Property values from DB.
     * @param propertyDtoArray  Array with new property values to set.
     */
    private void updateExistingPropertyValueList(List<BehaviorPropertyValue> propertyValueList, BVConfigurableProperty[] propertyDtoArray)
    {
        // For every resource to update, check if the property id exists. Need to exist, you cant add new properties not defined in nova.yml
        Arrays.stream(propertyDtoArray).forEach(configurablePropertyDto ->
        {
            Optional<BehaviorPropertyValue> propertyValueOptional =
                    propertyValueList.stream().filter(propertySaved -> configurablePropertyDto.getId().intValue() == propertySaved.getBehaviorProperty().getId().intValue())
                            .findAny();

            propertyValueOptional.ifPresentOrElse(propertyValue -> {
                LOG.debug("[{}] -> [updateBehaviorPropertiesConfigurationResource]: Replacing property from [{}]:[{}] to [{}]:[{}]",
                        this.getClass().getSimpleName(), propertyValue.getBehaviorProperty().getName(), propertyValue.getCurrentValue(), configurablePropertyDto.getName(),
                        configurablePropertyDto.getValue());

                propertyValue.setCurrentValue(configurablePropertyDto.getValue());
            }, () -> {
                throw new NovaException(BehaviorError.getResourceNotFoundError(
                        "NOVA Property", String.join(", ", configurablePropertyDto.getId().toString(),
                                configurablePropertyDto.getName(), configurablePropertyDto.getValue())));
            });
        });
    }

    private BVReleaseVersionInfoDto[] convertReleaseListIntoDto(List<ReleaseVersion> releasesList)
    {
        List<BVReleaseVersionInfoDto> releaseListMap = new ArrayList<>();

        if (releasesList.isEmpty())
        {
            // If no elements, return empty array
            return new BVReleaseVersionInfoDto[0];
        }
        else
        {
            releasesList.forEach(releasesListElement -> {

                BVReleaseVersionInfoDto bvReleaseVersionInfoDto = new BVReleaseVersionInfoDto();
                bvReleaseVersionInfoDto.setReleaseVersionId(releasesListElement.getId());
                bvReleaseVersionInfoDto.setReleaseVersionName(releasesListElement.getVersionName());

                releaseListMap.add(bvReleaseVersionInfoDto);
            });

            return releaseListMap.toArray(new BVReleaseVersionInfoDto[0]);
        }

    }

    private void checkBehaviorServiceExists(Integer behaviorServiceId, IntFunction<NovaError> throwsOnError)
    {
        if (!behaviorServiceRepository.existsById(behaviorServiceId))
        {
            throw new NovaException(throwsOnError.apply(behaviorServiceId));
        }
    }

    private void checkBehaviorVersionExists(Integer behaviorVersionId, IntFunction<NovaError> throwsOnError)
    {
        if (!behaviorVersionRepository.existsById(behaviorVersionId))
        {
            throw new NovaException(throwsOnError.apply(behaviorVersionId));
        }
    }

    private NovaError getResourceNotFoundError(Integer behaviorServiceId)
    {
        return BehaviorError.getResourceNotFoundError(
                Constants.BEHAVIOR_SERVICE_CONFIGURATION, String.join(", ", Constants.BEHAVIOR_SERVICE_ID, behaviorServiceId.toString()));
    }

    private NovaError behaviorServiceNotFoundError(Integer behaviorServiceId)
    {
        return BehaviorError.getBadParametersError(behaviorServiceId);
    }

    private void checkResourceId(Integer resourceId, String methodName)
    {
        if (null == resourceId || resourceId <= 0)
        {
            LOG.error("[{}] -> [{}]: Bad resource id provided", this.getClass().getSimpleName(), methodName);
            throw new NovaException(BehaviorError.getBadParametersError());
        }
    }

    /**
     * Check if product has initiatives for behavior services.
     *
     * @param productId Product id.
     */
    private void checkProductInitiatives(int productId)
    {
        if (!productBudgetsClient.checkBehaviorProductInitiatives(productId))
        {
            throw new NovaException(BehaviorError.getNoSuchInitiativesError());
        }
    }

    /**
     * Convert behavior services to summary dto bv service summary dto [ ].
     *
     * @param bvBehaviorServiceId the bv behavior service id
     * @return the bv service summary dto [ ]
     */
    private BVServiceSummaryDTO[] convertBehaviorServicesToSummaryDto(List<BehaviorService> bvBehaviorServiceId)
    {
        LOG.debug("[{}] -> [convertBehaviorServicesToSummaryDto]: Converting object: [{}]",
                this.getClass().getSimpleName(), bvBehaviorServiceId);

        BVServiceSummaryDTO[] serviceSummaryDTOArray = bvBehaviorServiceId.stream()
                .map(persistedServiceInfo ->
                {
                    BVServiceSummaryDTO bvServiceSummaryDto = new BVServiceSummaryDTO();
                    bvServiceSummaryDto.setBehaviorServiceName(persistedServiceInfo.getServiceName());
                    bvServiceSummaryDto.setBehaviorServiceId(persistedServiceInfo.getId());
                    bvServiceSummaryDto.setVersion(persistedServiceInfo.getVersion());
                    bvServiceSummaryDto.setTestFramework(persistedServiceInfo.getTestFramework());
                    bvServiceSummaryDto.setTestFrameworkVersion(persistedServiceInfo.getTestFrameworkVersion());

                    Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional =
                            behaviorServiceConfigurationRepository.findFirstByBehaviorServiceIdOrderByLastModifiedDesc(persistedServiceInfo.getId());
                    if (behaviorServiceConfigurationOptional.isPresent())
                    {
                        BehaviorServiceConfiguration behaviorServiceConfiguration = behaviorServiceConfigurationOptional.get();
                        if (behaviorServiceConfiguration.getHardwarePack() != null)
                        {
                            bvServiceSummaryDto.setBehaviorHardware(convertHardwarePackToDto(behaviorServiceConfiguration.getHardwarePack()));
                        }
                    }

                    bvServiceSummaryDto.setBehaviorServiceDescription(persistedServiceInfo.getDescription());
                    bvServiceSummaryDto.setStatus(persistedServiceInfo.getReleaseVersion().getStatus().name());
                    bvServiceSummaryDto.setBehaviorServiceType(persistedServiceInfo.getServiceType());

                    persistedServiceInfo.getReleaseVersion().getAllReleaseVersionServices().forEach(releaseVersionService ->
                    {
                        final boolean isMultiJdk = jvmJdkConfigurationChecker.isMultiJdk(releaseVersionService);

                        if (isMultiJdk)
                        {
                            final AllowedJdk allowedJdk = releaseVersionService.getAllowedJdk();
                            bvServiceSummaryDto.setBehaviorJvmVersion(allowedJdk.getJvmVersion());
                            bvServiceSummaryDto.setBehaviorJdkVersion(allowedJdk.getReadableName());
                        }
                    });

                    if (persistedServiceInfo.getProjectDefinitionFile() != null)
                    {
                        bvServiceSummaryDto.setBehaviorProjectDefinitionUrl(persistedServiceInfo.getProjectDefinitionFile().getUrl());
                        bvServiceSummaryDto.setProjectDefinitionFile(persistedServiceInfo.getProjectDefinitionFile().getContents());
                    }

                    if (persistedServiceInfo.getNovaYml() != null)
                    {
                        bvServiceSummaryDto.setBehaviorNovaYamlUrl(persistedServiceInfo.getNovaYml().getUrl());
                    }

                    // Specific data for API.
                    List<ApiImplementation<?, ?, ?>> novaApiClient = persistedServiceInfo.getConsumers();
                    if (novaApiClient != null)
                    {
                        List<BVServiceApiDTO> bvServiceApiDTOS = this.getListBVServiceApiDTO(novaApiClient);
                        bvServiceSummaryDto.setBehaviorApisConsumed(bvServiceApiDTOS.toArray(new BVServiceApiDTO[0]));
                    }
                    return bvServiceSummaryDto;
                }).toArray(BVServiceSummaryDTO[]::new);

        LOG.debug("[{}] -> [convertBehaviorServicesToSummaryDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), serviceSummaryDTOArray);

        return serviceSummaryDTOArray;
    }

    private BehaviorServiceConfiguration newBehaviorServiceConfiguration(Integer behaviorServiceId)
    {
        Optional<BehaviorServiceConfiguration> behaviorServiceConfigurationOptional = behaviorServiceConfigurationRepository
                .findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(behaviorServiceId, CONFIGURED);
        Calendar actualDateTime = Calendar.getInstance();
        if (behaviorServiceConfigurationOptional.isPresent())
        {
            BehaviorServiceConfiguration configuration = behaviorServiceConfigurationOptional.get();
            initializeConfigurationLists(configuration);

            // Clone object
            BehaviorServiceConfiguration newConfiguration = detachAndCopyObject(configuration, BehaviorServiceConfiguration.class);
            List<BSConfigurationFilesystem> configurationFilesystemList = newConfiguration.getBsConfigurationFilesystemList();
            List<LogicalConnector> logicalConnectorList = newConfiguration.getLogicalConnectorList();
            List<BehaviorPropertyValue> propertyValueList =
                    newConfiguration.getPropertyValueList().stream()
                            .filter(behaviorPropertyValue -> behaviorPropertyValue.getBehaviorProperty().getSourceType() == TEMPLATE).collect(Collectors.toList());
            newConfiguration.setId(null);
            newConfiguration.setStatus(EDITING);
            newConfiguration.setCreationDate(actualDateTime);
            newConfiguration.setLastModified(actualDateTime);
            newConfiguration.setBsConfigurationFilesystemList(null);
            newConfiguration.setLogicalConnectorList(null);
            newConfiguration.setPropertyValueList(null);
            newConfiguration.setBehaviorInstanceList(null);
            saveAndFlushElement(behaviorServiceConfigurationRepository, newConfiguration);

            // Copy filesystems
            List<BSConfigurationFilesystem> newConfigurationFilesystemList =
                    copyResourceList(newConfiguration, configurationFilesystemList, this::copyBSConfigurationFilesystem);
            bsConfigurationFilesystemRepository.saveAll(newConfigurationFilesystemList);
            newConfiguration.setBsConfigurationFilesystemList(newConfigurationFilesystemList);

            // Copy connectors
            List<LogicalConnector> newLogicalConnectorList = copyLogicalConnectorList(logicalConnectorList);
            newConfiguration.setLogicalConnectorList(newLogicalConnectorList);

            // Copy property values
            List<BehaviorPropertyValue> newPropertyValuesList = copyResourceList(newConfiguration, propertyValueList, this::copyPropertyValue);
            behaviorPropertyValueRepository.saveAll(newPropertyValuesList);
            newConfiguration.setPropertyValueList(newPropertyValuesList);

            return newConfiguration;
        }
        else
        {
            BehaviorServiceConfiguration newConfiguration = new BehaviorServiceConfiguration();
            newConfiguration.setBehaviorService(behaviorServiceRepository.getOne(behaviorServiceId));
            newConfiguration.setStatus(EDITING);
            newConfiguration.setJvmMemory(100);
            newConfiguration.setCreationDate(actualDateTime);
            newConfiguration.setLastModified(actualDateTime);
            return newConfiguration;
        }
    }

    private <T> List<T> copyResourceList(BehaviorServiceConfiguration newConfiguration, List<T> resourceList, BiFunction<T, BehaviorServiceConfiguration, T> function)
    {
        return resourceList.stream().map(resource -> function.apply(resource, newConfiguration)).collect(Collectors.toList());
    }

    private BSConfigurationFilesystem copyBSConfigurationFilesystem(BSConfigurationFilesystem configFilesystem, BehaviorServiceConfiguration newConfiguration)
    {
        BSConfigurationFilesystem newConfigurationFileSystem = detachAndCopyObject(configFilesystem, BSConfigurationFilesystem.class);
        newConfigurationFileSystem.setId(new BSConfigurationFilesystemId(newConfiguration.getId(),
                newConfigurationFileSystem.getFilesystem().getId()));
        newConfigurationFileSystem.setBehaviorServiceConfiguration(newConfiguration);
        newConfigurationFileSystem.setFilesystem(configFilesystem.getFilesystem());
        return newConfigurationFileSystem;
    }

    private BehaviorPropertyValue copyPropertyValue(BehaviorPropertyValue behaviorPropertyValue, BehaviorServiceConfiguration newConfiguration)
    {
        BehaviorPropertyValue newBehaviorPropertyValue = detachAndCopyObject(behaviorPropertyValue, BehaviorPropertyValue.class);
        newBehaviorPropertyValue.setId(new BehaviorPropertyValueId(newConfiguration.getId(),
                newBehaviorPropertyValue.getBehaviorProperty().getId()));
        newBehaviorPropertyValue.setBehaviorServiceConfiguration(newConfiguration);
        newBehaviorPropertyValue.setBehaviorProperty(behaviorPropertyValue.getBehaviorProperty());
        return newBehaviorPropertyValue;
    }

    private List<LogicalConnector> copyLogicalConnectorList(List<LogicalConnector> logicalConnectorList)
    {
        List<LogicalConnector> newLogicalConnectorList = new ArrayList<>();
        logicalConnectorList.forEach(logicalConnector -> {
            entityManager.detach(logicalConnector);
            newLogicalConnectorList.add(logicalConnector);
        });
        return newLogicalConnectorList;
    }

    /**
     * Detaches an object from JPA (the object will no longer be handled by the entity manager) and copy its data to a new object.
     *
     * @param object Object to detach from JPA.
     * @param clazz  Object Class.
     * @param <T>    Object class.
     * @return The cloned object.
     */
    private <T> T detachAndCopyObject(T object, Class<T> clazz)
    {
        entityManager.detach(object);
        T newObject;
        try
        {
            newObject = clazz.getConstructor().newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e)
        {
            throw new NovaException(BehaviorError.getUnexpectedError());
        }

        BeanUtils.copyProperties(object, newObject);
        return newObject;
    }

    /**
     * Loads list data from lazy relations.
     *
     * @param configuration Behavior service configuration.
     */
    private void initializeConfigurationLists(BehaviorServiceConfiguration configuration)
    {
        Hibernate.initialize(configuration.getBsConfigurationFilesystemList());
        Hibernate.initialize(configuration.getLogicalConnectorList());
        Hibernate.initialize(configuration.getPropertyValueList());
    }

    /**
     * Convert behavior subsystems to dto bv behavior version subsystem dto [ ].
     *
     * @param behaviorSubsystems the behavior subsystems
     * @return the bv behavior version subsystem dto [ ]
     */
    private BVBehaviorVersionSubsystemDTO[] convertBehaviorSubsystemsToDto(List<BehaviorSubsystem> behaviorSubsystems)
    {
        LOG.debug("[{}] -> [convertBehaviorSubsystemsToDto]: Converting object: [{}]",
                this.getClass().getSimpleName(), behaviorSubsystems);

        List<BVBehaviorVersionSubsystemDTO> behaviorVersionSubsystemDtoToReturn = new ArrayList<>();

        behaviorSubsystems.forEach(persistedBehaviorSubsystem ->
        {
            BVBehaviorVersionSubsystemDTO bvBehaviorVersionSubsystemDTO = new BVBehaviorVersionSubsystemDTO();
            bvBehaviorVersionSubsystemDTO.setCompilationJobName(persistedBehaviorSubsystem.getCompilationJobName());
            bvBehaviorVersionSubsystemDTO.setTagName(persistedBehaviorSubsystem.getTagName());
            bvBehaviorVersionSubsystemDTO.setTagUrl(persistedBehaviorSubsystem.getTagUrl());
            bvBehaviorVersionSubsystemDTO.setProductSubsystemId(persistedBehaviorSubsystem.getId());

            bvBehaviorVersionSubsystemDTO.setServices(this.convertBehaviorServicesToSummaryDto(this.behaviorServiceRepository.findByBehaviorSubsystemId(persistedBehaviorSubsystem.getId())));

            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(persistedBehaviorSubsystem.getSubsystemId());
            bvBehaviorVersionSubsystemDTO.setProductSubsystemType(subsystemDTO.getSubsystemType());
            bvBehaviorVersionSubsystemDTO.setCompilationJobUrl(subsystemDTO.getJenkinsUrl());
            bvBehaviorVersionSubsystemDTO.setProductSubsystemDesc(subsystemDTO.getDescription());
            bvBehaviorVersionSubsystemDTO.setProductSubsystemName(subsystemDTO.getSubsystemName());

            behaviorVersionSubsystemDtoToReturn.add(bvBehaviorVersionSubsystemDTO);
        });

        LOG.debug("[{}] -> [convertBehaviorSubsystemsToDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), behaviorVersionSubsystemDtoToReturn);

        return behaviorVersionSubsystemDtoToReturn.toArray(new BVBehaviorVersionSubsystemDTO[0]);
    }

    /**
     * Convert behavior version to summary dto bv behavior version summary info dto.
     *
     * @param behaviorVersion the behavior version
     * @return the bv behavior version summary info dto
     */
    private BVBehaviorVersionSummaryInfoDTO convertBehaviorVersionToSummaryDto(BehaviorVersion behaviorVersion)
    {
        LOG.debug("[{}] -> [convertBehaviorVersionToSummaryDto]: Converting object: [{}]",
                this.getClass().getSimpleName(), behaviorVersion);

        BVBehaviorVersionSummaryInfoDTO bvBehaviorVersionSummaryInfoDTO = new BVBehaviorVersionSummaryInfoDTO();

        bvBehaviorVersionSummaryInfoDTO.setId(behaviorVersion.getId());
        bvBehaviorVersionSummaryInfoDTO.setVersionName(behaviorVersion.getVersionName());
        bvBehaviorVersionSummaryInfoDTO.setQualityValidation(behaviorVersion.getQualityValidation());
        bvBehaviorVersionSummaryInfoDTO.setCreationDate(behaviorVersion.getCreatedAt().getTimeInMillis());
        bvBehaviorVersionSummaryInfoDTO.setDeletionDate(behaviorVersion.getDeletionAt().getTimeInMillis());
        bvBehaviorVersionSummaryInfoDTO.setDescription(behaviorVersion.getDescription());
        bvBehaviorVersionSummaryInfoDTO.setStatus(behaviorVersion.getStatus().toString());

        LOG.debug("[{}] -> [convertBehaviorVersionToSummaryDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), bvBehaviorVersionSummaryInfoDTO);

        return bvBehaviorVersionSummaryInfoDTO;
    }

    /**
     * Convert list to broker configurable bv configurable broker [ ].
     *
     * @param brokerResource the broker resource
     * @return the bv configurable broker [ ]
     */
    private BVConfigurableBroker[] convertListToBrokerConfigurable(List<Broker> brokerResource)
    {
        LOG.debug("[{}] -> [convertListToBrokerConfigurable]: Converting object: [{}]",
                this.getClass().getSimpleName(), brokerResource);

        if (brokerResource.isEmpty())
        {
            return new BVConfigurableBroker[0];
        }

        // Brokers is a special case due to in a future, could be selected more than one. At the moment only one
        List<BVConfigurableBroker> bvConfigurableBrokerList = new ArrayList<>();

        brokerResource.forEach(brokerInList ->
        {
            BVConfigurableBroker bvConfigurableBroker = new BVConfigurableBroker();
            bvConfigurableBroker.setId(brokerInList.getId());
            bvConfigurableBroker.setName(brokerInList.getName());
            bvConfigurableBroker.setType(brokerInList.getType().getType());
            bvConfigurableBroker.setDescription(brokerInList.getDescription());

            bvConfigurableBrokerList.add(bvConfigurableBroker);
        });

        LOG.debug("[{}] -> [convertListToBrokerConfigurable]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), bvConfigurableBrokerList);

        return bvConfigurableBrokerList.toArray(new BVConfigurableBroker[0]);
    }

    /**
     * Convert list to configurable connector bv configurable connector [ ].
     *
     * @param logicalConnector the connectors
     * @return the bv configurable connector [ ]
     */
    private BVConfigurableConnector convertLogicalConnectorToDTO(LogicalConnector logicalConnector)
    {
        LOG.debug("[{}] -> [convertLogicalConnectorToDTO]: Converting object: [{}]", this.getClass().getSimpleName(), logicalConnector);

        BVConfigurableConnector bvConfigurableConnector = new BVConfigurableConnector();
        bvConfigurableConnector.setId(logicalConnector.getId());
        bvConfigurableConnector.setName(logicalConnector.getName());
        if (logicalConnector.getConnectorType() != null)
        {
            bvConfigurableConnector.setType(logicalConnector.getConnectorType().getName());
        }
        bvConfigurableConnector.setDescription(logicalConnector.getDescription());

        LOG.debug("[{}] -> [convertLogicalConnectorToDTO]: Object converted to return: [{}]", this.getClass().getSimpleName(), bvConfigurableConnector);

        return bvConfigurableConnector;
    }

    /**
     * Convert resource to configurable hardware bv configurable hardware.
     *
     * @param hardwarePack the hardware pack
     * @return the bv configurable hardware
     */
    private BVConfigurableHardware convertHardwarePackToDto(HardwarePack hardwarePack)
    {
        LOG.debug("[{}] -> [convertHardwarePackToDto]: Converting object: [{}]", getClass().getSimpleName(), hardwarePack);

        BVConfigurableHardware bvConfigurableHardware = new BVConfigurableHardware();
        bvConfigurableHardware.setPackId(hardwarePack.getId());
        if (hardwarePack.getHardwarePackType() != null)
        {
            bvConfigurableHardware.setPackName(hardwarePack.getHardwarePackType().name());
        }
        bvConfigurableHardware.setTotalMemory((double) hardwarePack.getRamMB());
        bvConfigurableHardware.setTotalCpu(hardwarePack.getNumCPU());

        LOG.debug("[{}] -> [convertHardwarePackToDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), bvConfigurableHardware);

        return bvConfigurableHardware;
    }

    /**
     * Create bs configuration filesystem list list.
     *
     * @param alreadyPersistedFilesystemsToSet      the already persisted filesystems to set
     * @param persistedBehaviorServiceConfiguration the persisted behavior service confgiruation
     * @return the list
     */
    private List<BSConfigurationFilesystem> createBsConfigurationFilesystemList(
            List<Filesystem> alreadyPersistedFilesystemsToSet, BehaviorServiceConfiguration persistedBehaviorServiceConfiguration, BVConfigurableFilesystem[] resourceDtoToUpdate)
    {
        LOG.debug("[{}] -> [createBsConfigurationFilesystemList]: Creating bs configuration filesystem [{}] in configuration: [{}]",
                this.getClass().getSimpleName(), alreadyPersistedFilesystemsToSet, persistedBehaviorServiceConfiguration);

        List<BSConfigurationFilesystem> bsConfigurationFilesystemsListToSet = new ArrayList<>();
        alreadyPersistedFilesystemsToSet.forEach(filesystemToSet ->
        {
            BSConfigurationFilesystemId id = new BSConfigurationFilesystemId(persistedBehaviorServiceConfiguration.getId(), filesystemToSet.getId());
            BSConfigurationFilesystem bsConfigurationFilesystem = new BSConfigurationFilesystem();

            bsConfigurationFilesystem.setId(id);
            bsConfigurationFilesystem.setFilesystem(filesystemToSet);
            bsConfigurationFilesystem.setBehaviorServiceConfiguration(persistedBehaviorServiceConfiguration);

            if (resourceDtoToUpdate != null)
            {
                Optional<BVConfigurableFilesystem> bvConfigurableFilesystem = Arrays.stream(resourceDtoToUpdate).filter(r -> r.getId().intValue() == filesystemToSet.getId().intValue()).findFirst();

                bvConfigurableFilesystem.ifPresent(configurableFilesystem -> bsConfigurationFilesystem.setVolumeBind(configurableFilesystem.getVolumeBind()));

            }
            bsConfigurationFilesystemsListToSet.add(bsConfigurationFilesystem);
        });

        LOG.debug("[{}] -> [createBsConfigurationFilesystemList]: Created bs configuration filesystem list: [{}]",
                this.getClass().getSimpleName(), bsConfigurationFilesystemsListToSet);

        return bsConfigurationFilesystemsListToSet;
    }

    protected List<BSConfigurationFilesystem> mergeFileSystems(
            BehaviorServiceConfiguration configuration, List<Filesystem> filesystemListToSet, BVConfigurableFilesystem[] resourceDtoToUpdate)
    {
        LOG.debug("[{}] -> [createBsConfigurationFilesystemList]: Merging filesystems in configuration: [{}]. Filesystems DTO: [{}]. ",
                this.getClass().getSimpleName(), configuration.getId(), resourceDtoToUpdate);

        List<BSConfigurationFilesystem> bsConfigurationFilesystemsList = configuration.getBsConfigurationFilesystemList();
        deleteRemovedFilesystems(filesystemListToSet, bsConfigurationFilesystemsList);
        updateOrAddFilesystems(configuration, filesystemListToSet, resourceDtoToUpdate, bsConfigurationFilesystemsList);

        LOG.debug("[{}] -> [createBsConfigurationFilesystemList]: Created bs configuration filesystem list: [{}]",
                this.getClass().getSimpleName(), bsConfigurationFilesystemsList);

        return bsConfigurationFilesystemsList;
    }

    private static void deleteRemovedFilesystems(List<Filesystem> filesystemListToSet, List<BSConfigurationFilesystem> bsConfigurationFilesystemsList)
    {
        List<BSConfigurationFilesystem> bsConfigurationFilesystemToRemoveList = new ArrayList<>();
        bsConfigurationFilesystemsList.forEach(bsConfigurationFilesystem -> {
            if (!filesystemListToSet.contains(bsConfigurationFilesystem.getFilesystem()))
            {
                bsConfigurationFilesystemToRemoveList.add(bsConfigurationFilesystem);
            }
        });
        bsConfigurationFilesystemsList.removeAll(bsConfigurationFilesystemToRemoveList);
    }

    private void updateOrAddFilesystems(BehaviorServiceConfiguration configuration, List<Filesystem> filesystemListToSet, BVConfigurableFilesystem[] resourceDtoToUpdate, List<BSConfigurationFilesystem> bsConfigurationFilesystemsList)
    {
        filesystemListToSet.forEach(filesystem -> {
            Optional<BSConfigurationFilesystem> bsConfigurationFilesystemOptional =
                    findBSConfigFilesystemByFilesystemEntity(bsConfigurationFilesystemsList, filesystem);

            Optional<BVConfigurableFilesystem> configurableFilesystemDTO = findDtoByFileSystem(resourceDtoToUpdate, filesystem);
            // If BSConfigurationFilesystem already exists, update volume bind
            if (bsConfigurationFilesystemOptional.isPresent())
            {
                configurableFilesystemDTO.ifPresent(bvConfigurableFilesystem -> bsConfigurationFilesystemOptional.get().setVolumeBind(bvConfigurableFilesystem.getVolumeBind()));
            }
            // If BSConfigurationFilesystem does not exist, create it and add it to configuration
            else
            {
                configurableFilesystemDTO.ifPresent(bvConfigurableFilesystem -> {
                    BSConfigurationFilesystem bsConfigurationFilesystem =
                            new BSConfigurationFilesystem(
                                    new BSConfigurationFilesystemId(configuration.getId(), filesystem.getId()),
                                    configuration, filesystem, bvConfigurableFilesystem.getVolumeBind());
                    bsConfigurationFilesystemsList.add(bsConfigurationFilesystem);
                });
            }
        });
    }

    private Optional<BSConfigurationFilesystem> findBSConfigFilesystemByFilesystemEntity(List<BSConfigurationFilesystem> bsConfigurationFilesystemsList, Filesystem filesystem)
    {
        return bsConfigurationFilesystemsList.stream()
                .filter(bsConfigurationFilesystem ->
                        bsConfigurationFilesystem.getFilesystem() == filesystem)
                .findFirst();
    }

    private Optional<BVConfigurableFilesystem> findDtoByFileSystem(BVConfigurableFilesystem[] resourceDtoToUpdate, Filesystem filesystem)
    {
        return Arrays.stream(resourceDtoToUpdate)
                .filter(bsConfigurationFilesystem ->
                        bsConfigurationFilesystem.getId().intValue() == filesystem.getId().intValue())
                .findFirst();
    }

    /**
     * Create bs configuration filesystem list list.
     *
     * @param fileSystem File system to convert.
     * @return the Dto created
     */
    private BVConfigurableFilesystem convertFilesystemToConfigurableFileSystemDto(Filesystem fileSystem)
    {
        LOG.debug("[{}] -> [convertFilesystemToConfigurableFileSystemDto]: Converting object [{}]",
                this.getClass().getSimpleName(), fileSystem);

        BVConfigurableFilesystem bvConfigurableFilesystem = new BVConfigurableFilesystem();
        bvConfigurableFilesystem.setId(fileSystem.getId());
        bvConfigurableFilesystem.setName(fileSystem.getName());
        bvConfigurableFilesystem.setVolumeBind(fileSystem.getLandingZonePath());

        LOG.debug("[{}] -> [convertFilesystemToConfigurableFileSystemDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), bvConfigurableFilesystem);

        return bvConfigurableFilesystem;
    }

    /**
     * Convert to filesystem configurable bv configurable filesystem.
     *
     * @param bsConfigurationFilesystem the bs configuration filesystem
     * @return the bv configurable filesystem [ ]
     */
    private BVConfigurableFilesystem convertBSConfigurationFileSystemToConfigurableDto(BSConfigurationFilesystem bsConfigurationFilesystem)
    {
        LOG.debug("[{}] -> [convertBSConfigurationFileSystemToConfigurableDto]: Converting object: [{}]", getClass().getSimpleName(), bsConfigurationFilesystem);

        BVConfigurableFilesystem bvConfigurableFilesystem = new BVConfigurableFilesystem();
        bvConfigurableFilesystem.setId(bsConfigurationFilesystem.getFilesystem().getId());
        bvConfigurableFilesystem.setName(bsConfigurationFilesystem.getFilesystem().getName());
        bvConfigurableFilesystem.setVolumeBind(bsConfigurationFilesystem.getVolumeBind());

        LOG.debug("[{}] -> [convertBSConfigurationFileSystemToConfigurableDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), bvConfigurableFilesystem);

        return bvConfigurableFilesystem;
    }

    /**
     * Convert resource to jvm configurable bv configurable jvm.
     *
     * @param jvmResource the jvm resource
     * @return the bv configurable jvm
     */
    private BVConfigurableJvm convertResourceToJvmConfigurable(Integer jvmResource)
    {
        LOG.debug("[{}] -> [convertResourceToJvmConfigurable]: Converting object: [{}]",
                this.getClass().getSimpleName(), jvmResource);

        BVConfigurableJvm bvConfigurableJvm = new BVConfigurableJvm();

        bvConfigurableJvm.setJvmPercentage(Objects.requireNonNullElse(jvmResource, 0));

        LOG.debug("[{}] -> [convertResourceToJvmConfigurable]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), bvConfigurableJvm);

        return bvConfigurableJvm;
    }

    private List<BVConfigurableProperty> convertConnectorPropToDto(BehaviorServiceConfiguration behaviorServiceConfiguration)
    {
        LOG.debug("[{}] -> [convertConnectorPropToDto]: Converting object: [{}]",
                this.getClass().getSimpleName(), behaviorServiceConfiguration);

        List<BVConfigurableProperty> connectorPropsAsConfigurationProperties = new ArrayList<>();
        behaviorServiceConfiguration.getLogicalConnectorList().forEach(connector ->
        {
            connector.getLogConnProp().forEach(logicalConnectorProperty ->
            {
                BVConfigurableProperty configurableProperty = new BVConfigurableProperty();

                configurableProperty.setId(logicalConnectorProperty.getId());
                configurableProperty.setName(logicalConnectorProperty.getName());
                configurableProperty.setDefaultValue(logicalConnectorProperty.getDefaultName());
                configurableProperty.setValue(logicalConnectorProperty.getPropertyValue());
                configurableProperty.setEncrypted(logicalConnectorProperty.isSecurity());
                configurableProperty.setManagement(logicalConnectorProperty.getManagement());
                configurableProperty.setDescription(logicalConnectorProperty.getDescription());
                configurableProperty.setEditable(false);
                configurableProperty.setSourceType(PropertySourceType.CONNECTOR.name());
                configurableProperty.setScope(logicalConnectorProperty.getScope());
                configurableProperty.setPropertyType(logicalConnectorProperty.getPropertyType());

                connectorPropsAsConfigurationProperties.add(configurableProperty);

            });
        });

        LOG.debug("[{}] -> [convertConnectorPropToDto]: Object converted to return: [{}]",
                this.getClass().getSimpleName(), connectorPropsAsConfigurationProperties);

        return connectorPropsAsConfigurationProperties;
    }

    private BVConfigurableProperty convertBehaviorPropertyValueToConfigurablePropertyDto(BehaviorPropertyValue behaviorPropertyValue)
    {
        LOG.debug("[{}] -> [convertBehaviorPropertyValueToConfigurablePropertyDto]: Converting object: [{}]",
                this.getClass().getSimpleName(), behaviorPropertyValue);

        BVConfigurableProperty configurableProperty = new BVConfigurableProperty();
        BehaviorProperty behaviorProperty = behaviorPropertyValue.getBehaviorProperty();
        configurableProperty.setId(behaviorProperty.getId());
        configurableProperty.setName(behaviorProperty.getName());
        configurableProperty.setDefaultValue(behaviorProperty.getDefaultValue());
        configurableProperty.setValue(behaviorPropertyValue.getCurrentValue());
        configurableProperty.setEncrypted(behaviorProperty.getEncrypted());
        configurableProperty.setManagement(behaviorProperty.getManagementType().toString());
        configurableProperty.setDescription(behaviorProperty.getDescription());
        configurableProperty.setSourceType(TEMPLATE.name());
        configurableProperty.setPropertyType(behaviorProperty.getType().getPropertyType());
        configurableProperty.setEditable(true);
        if (behaviorProperty.getScope() != null)
        {
            configurableProperty.setScope(behaviorProperty.getScope().toString());
        }

        LOG.debug("[{}] -> [convertBehaviorPropertyValueToConfigurablePropertyDto]: Object converted to return: [{}]",
                getClass().getSimpleName(), configurableProperty);

        return configurableProperty;
    }

    private <X, K> void saveAndFlushElement(JpaRepository<X, K> repository, X element)
    {
        LOG.info("[{}]->[saveAndFlushElement]: Saving element [{}] in repository", this.getClass().getSimpleName(), element);
        repository.saveAndFlush(element);
    }

    /**
     * Processes List of APIS Client AND Server
     *
     * @param novaApis List of Apis (Client or Server)
     */
    private List<BVServiceApiDTO> getListBVServiceApiDTO(List<ApiImplementation<?, ?, ?>> novaApis)
    {
        return novaApis.stream()
                .map(apiImplementation -> this.behaviorVersionDtoBuilderModalityBased.buildBVServiceApiDTO(apiImplementation))
                .collect(Collectors.toList());
    }

    /**
     * Convert behavior property to configurable property DTO.
     *
     * @param behaviorProperty Behavior property entity.
     * @return the bv configurable property DTO.
     */
    private BVConfigurableProperty convertBehaviorPropertyToConfigurablePropertyDto(BehaviorProperty behaviorProperty)
    {
        LOG.debug("[{}] -> [convertBehaviorPropertyToConfigurablePropertyDto]: Converting object: [{}]",
                getClass().getSimpleName(), behaviorProperty);

        BVConfigurableProperty bvConfigurableProperty = new BVConfigurableProperty();
        bvConfigurableProperty.setId(behaviorProperty.getId());
        bvConfigurableProperty.setName(Optional.ofNullable(behaviorProperty.getName()).orElse(""));
        bvConfigurableProperty.setValue(Optional.ofNullable(behaviorProperty.getDefaultValue()).orElse(""));
        bvConfigurableProperty.setDefaultValue(Optional.ofNullable(behaviorProperty.getDefaultValue()).orElse(""));
        bvConfigurableProperty.setEncrypted(Optional.ofNullable(behaviorProperty.getEncrypted()).orElse(false));
        bvConfigurableProperty.setPropertyType(behaviorProperty.getType().getPropertyType());
        bvConfigurableProperty.setManagement(behaviorProperty.getManagementType().name());
        bvConfigurableProperty.setSourceType(behaviorProperty.getSourceType().name());
        if (behaviorProperty.getScope() != null)
        {
            bvConfigurableProperty.setScope(behaviorProperty.getScope().toString());
        }
        bvConfigurableProperty.setEditable(behaviorProperty.getSourceType() == TEMPLATE);

        LOG.debug("[{}] -> [convertBehaviorPropertyToConfigurablePropertyDto]: Object converted to return: [{}]",
                getClass().getSimpleName(), behaviorProperty);

        return bvConfigurableProperty;
    }

    private void loadBehaviorPropertiesFromService(BehaviorService behaviorService, BVPropertiesResourceDto propertiesResourceDto,
                                                   BehaviorVersion behaviorVersion, List<BVConfigurableProperty> configurablePropertyList)
    {
        propertiesResourceDto.setProductId(behaviorVersion.getProduct().getId());
        propertiesResourceDto.setBehaviorServiceId(behaviorService.getId());
        propertiesResourceDto.setBehaviorVersionId(behaviorVersion.getId());
        propertiesResourceDto.setPropertiesOptions(configurablePropertyList.toArray(new BVConfigurableProperty[0]));
    }

    private List<BehaviorPropertyValue> convertConfigurablePropertyToBehaviorPropertyValue(
            BVConfigurableProperty[] configurablePropertyArray, List<BehaviorProperty> behaviorPropertyList,
            BehaviorServiceConfiguration behaviorServiceConfiguration)
    {
        List<BehaviorPropertyValue> behaviorPropertyValueList = new ArrayList<>(configurablePropertyArray.length);
        Arrays.stream(configurablePropertyArray).forEach(configurableProperty -> {
            Optional<BehaviorProperty> behaviorPropertyOptional =
                    behaviorPropertyList.stream()
                            .filter(behaviorProperty -> configurableProperty.getId().intValue() == behaviorProperty.getId().intValue())
                            .findFirst();
            // Find the matching BehaviorProperty by id
            if (behaviorPropertyOptional.isPresent())
            {
                BehaviorPropertyValue behaviorPropertyValue = new BehaviorPropertyValue();
                BehaviorPropertyValueId behaviorPropertyValueId = new BehaviorPropertyValueId(behaviorServiceConfiguration.getId(), behaviorPropertyOptional.get().getId());
                behaviorPropertyValue.setId(behaviorPropertyValueId);
                behaviorPropertyValue.setBehaviorProperty(behaviorPropertyOptional.get());
                behaviorPropertyValue.setCurrentValue(configurableProperty.getValue());
                behaviorPropertyValue.setBehaviorServiceConfiguration(behaviorServiceConfiguration);
                behaviorPropertyValueList.add(behaviorPropertyValue);
            }
        });
        return behaviorPropertyValueList;
    }

    /**
     * Validate user permission
     *
     * @param ivUser     user
     * @param productId  product
     * @param permission permission to validate
     * @throws NovaException in case of unauthorized
     */
    private void validateUserPermission(final String ivUser, final int productId, final String permission) throws NovaException
    {
        this.userClient.checkHasPermission(ivUser, permission, Environment.PRE.name(), productId, new NovaException(BehaviorError.getUserNotAuthorizedError(), BehaviorError.getUserNotAuthorizedError().toString()));
    }

    private void validateProductBudgets(final BehaviorServiceConfiguration bsConfiguration) throws NovaException
    {
        PBCheckExecutionInfo infoAboutExecution = new PBCheckExecutionInfo();

        BehaviorService behaviorService = bsConfiguration.getBehaviorService();
        BehaviorVersion behaviorVersion = bsConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion();

        infoAboutExecution.setBehaviorServiceId(behaviorService.getId());
        infoAboutExecution.setBehaviorVersionId(behaviorVersion.getId());
        infoAboutExecution.setProductId(behaviorVersion.getProduct().getId());

        // This field is important, due to defines the cost to add and determines if the cost exceed the budget
        infoAboutExecution.setPackIdConfigured(bsConfiguration.getHardwarePack().getId());

        if (!this.productBudgetsClient.checkBehaviorBudgets(infoAboutExecution))
        {
            throw new NovaException(BehaviorError.getProductWithoutNovaServicesCanonError());
        }
    }

    private void updateBehaviorVersionPotentialCost(BehaviorServiceConfiguration behaviorServiceConfiguration, HardwarePack hardwarePack)
    {
        PBBehaviorPotentialCostInfo pbBehaviorPotentialCostInfo = new PBBehaviorPotentialCostInfo();
        BehaviorVersion behaviorVersion = behaviorServiceConfiguration.getBehaviorService().getBehaviorSubsystem().getBehaviorVersion();
        pbBehaviorPotentialCostInfo.setProductId(behaviorVersion.getProduct().getId());
        pbBehaviorPotentialCostInfo.setBehaviorVersionId(behaviorVersion.getId());
        pbBehaviorPotentialCostInfo.setPackId(hardwarePack.getId());
        this.productBudgetsClient.updateBehaviorVersionPotentialCost(pbBehaviorPotentialCostInfo);
    }

    private void saveLogicalConnectorListProperties(BehaviorServiceConfiguration configuration)
    {
        for (LogicalConnector logicalConnector : configuration.getLogicalConnectorList())
        {
            updateOrAddConnectorProperties(configuration.getBehaviorService().getProperties(), logicalConnector);
            saveAndFlushElement(behaviorServiceConfigurationRepository, configuration);
            updateOrAddConnectorPropertyValues(configuration.getBehaviorService().getProperties(), configuration.getPropertyValueList(), configuration);
            saveAndFlushElement(behaviorServiceConfigurationRepository, configuration);
        }
    }

    private void updateOrAddConnectorProperties(List<BehaviorProperty> behaviorPropertyList, LogicalConnector logicalConnector)
    {
        for (LogicalConnectorProperty connectorProperty : logicalConnector.getLogConnProp())
        {
            Optional<BehaviorProperty> behaviorPropertyOptional = behaviorPropertyList.stream()
                    .filter(behaviorProperty -> behaviorProperty.getSourceType() == CONNECTOR
                            && behaviorProperty.getSourceId().intValue() == logicalConnector.getId().intValue()
                            && behaviorProperty.getName().equals(connectorProperty.getName())).findFirst();
            behaviorPropertyOptional.ifPresentOrElse(
                    behaviorProperty -> behaviorProperty.setDefaultValue(connectorProperty.getPropertyValue()),
                    () -> behaviorPropertyList.add(createBehaviorPropertyForConnector(connectorProperty, logicalConnector.getId()))
            );
        }
    }

    private void updateOrAddConnectorPropertyValues(
            List<BehaviorProperty> behaviorPropertyList, List<BehaviorPropertyValue> propertyValueList, BehaviorServiceConfiguration configuration)
    {
        for (BehaviorProperty behaviorProperty : behaviorPropertyList)
        {
            Optional<BehaviorPropertyValue> propertyValueOptional = propertyValueList.stream()
                    .filter(propertyValue -> propertyValue.getBehaviorProperty() == behaviorProperty).findFirst();
            propertyValueOptional.ifPresentOrElse(
                    propertyValue -> {
                        if (propertyValue.getBehaviorProperty().getSourceType() == CONNECTOR)
                        {
                            propertyValue.setCurrentValue(behaviorProperty.getDefaultValue());
                        }
                    },
                    () -> propertyValueList.add(createBehaviorPropertyValue(behaviorProperty, configuration))
            );
        }
    }

    private BehaviorProperty createBehaviorPropertyForConnector(LogicalConnectorProperty connectorProperty, Integer connectorId)
    {
        BehaviorProperty behaviorProperty = new BehaviorProperty();
        behaviorProperty.setName(connectorProperty.getName());
        behaviorProperty.setType(PropertyType.valueOf(connectorProperty.getPropertyType()));
        behaviorProperty.setDefaultValue(connectorProperty.getPropertyValue());
        behaviorProperty.setSourceType(CONNECTOR);
        behaviorProperty.setSourceId(connectorId);
        behaviorProperty.setManagementType(ManagementType.valueOf(connectorProperty.getManagement()));
        behaviorProperty.setDescription(connectorProperty.getDescription());
        behaviorProperty.setScope(ScopeType.valueOf(connectorProperty.getScope()));
        return behaviorProperty;
    }

    private BehaviorPropertyValue createBehaviorPropertyValue(BehaviorProperty behaviorProperty, BehaviorServiceConfiguration configuration)
    {
        BehaviorPropertyValue behaviorPropertyValue = new BehaviorPropertyValue();
        behaviorPropertyValue.setId(new BehaviorPropertyValueId(configuration.getId(), behaviorProperty.getId()));
        behaviorPropertyValue.setBehaviorProperty(behaviorProperty);
        behaviorPropertyValue.setCurrentValue(behaviorProperty.getDefaultValue());
        behaviorPropertyValue.setBehaviorServiceConfiguration(configuration);
        return behaviorPropertyValue;
    }

    private void updateLogicalConnectorListProperties(BehaviorServiceConfiguration configuration)
    {
        List<BehaviorPropertyValue> connectorPropertyValueList = configuration.getPropertyValueList().stream()
                .filter(behaviorPropertyValue -> behaviorPropertyValue.getBehaviorProperty().getSourceType() == CONNECTOR).collect(Collectors.toList());
        for (LogicalConnector logicalConnector : configuration.getLogicalConnectorList())
        {
            updateBehaviorPropertyValuesForConnector(logicalConnector, connectorPropertyValueList);
        }
    }

    private List<BehaviorProperty> createBehaviorPropertiesForConnector(LogicalConnector logicalConnector)
    {
        return logicalConnector.getLogConnProp().stream().map(logicalConnectorProperty ->
        {
            BehaviorProperty behaviorProperty = new BehaviorProperty();
            behaviorProperty.setName(logicalConnectorProperty.getName());
            behaviorProperty.setType(PropertyType.valueOf(logicalConnectorProperty.getPropertyType()));
            behaviorProperty.setDefaultValue(logicalConnectorProperty.getPropertyValue());
            behaviorProperty.setSourceType(CONNECTOR);
            behaviorProperty.setSourceId(logicalConnector.getId());
            behaviorProperty.setManagementType(ManagementType.valueOf(logicalConnectorProperty.getManagement()));
            behaviorProperty.setDescription(logicalConnectorProperty.getDescription());
            behaviorProperty.setScope(ScopeType.valueOf(logicalConnectorProperty.getScope()));
            return behaviorProperty;
        }).collect(Collectors.toList());
    }

    private void updateBehaviorPropertyValuesForConnector(LogicalConnector logicalConnector, List<BehaviorPropertyValue> connectorPropertyValueList)
    {
        logicalConnector.getLogConnProp().forEach(logicalConnectorProperty ->
        {
            Optional<BehaviorPropertyValue> behaviorPropertyValue =
                    connectorPropertyValueList.stream().filter(propertyValue -> propertyValue.getBehaviorProperty().getSourceId().intValue() == logicalConnector.getId().intValue()).findFirst();
            behaviorPropertyValue.ifPresent(propertyValue -> propertyValue.setCurrentValue(logicalConnectorProperty.getPropertyValue()));
        });
    }

    private List<BehaviorPropertyValue> createBehaviorPropertyValues(List<BehaviorProperty> behaviorConnectorPropertyList, BehaviorServiceConfiguration configuration)
    {
        return behaviorConnectorPropertyList.stream().map(behaviorProperty ->
        {
            BehaviorPropertyValue behaviorPropertyValue = new BehaviorPropertyValue();
            behaviorPropertyValue.setId(new BehaviorPropertyValueId(configuration.getId(), behaviorProperty.getId()));
            behaviorPropertyValue.setBehaviorProperty(behaviorProperty);
            behaviorPropertyValue.setCurrentValue(behaviorProperty.getDefaultValue());
            behaviorPropertyValue.setBehaviorServiceConfiguration(configuration);
            return behaviorPropertyValue;
        }).collect(Collectors.toList());
    }

    /**
     * Convert behavior instances to dto bv behavior execution dto [ ].
     *
     * @param behaviorInstances the behavior instances
     * @return the bv behavior execution dto [ ]
     */
    private BVBehaviorExecution[] convertBehaviorServiceExecutionsToDto(List<BehaviorInstance> behaviorInstances, BABehaviorExecutionDTO[] behaviorExecutionDTOArray)
    {
        LOG.debug("[{}] -> [convertBehaviorServiceExecutionsToDto]: Converting objects: [{}], [{}]",
                this.getClass().getSimpleName(), behaviorInstances, behaviorExecutionDTOArray);

        List<BVBehaviorExecution> behaviorVersionExecutionDtoToReturn = new ArrayList<>();

        behaviorInstances.forEach(instance ->
        {
            Optional<BABehaviorExecutionDTO> baExecutionDtoOptional =
                    Arrays.stream(behaviorExecutionDTOArray).filter(execution ->
                            execution.getBehaviorInstanceId() != null &&
                                    execution.getBehaviorInstanceId().intValue() == instance.getId().intValue()).findFirst();

            baExecutionDtoOptional.ifPresentOrElse(baBehaviorExecutionDTO -> behaviorVersionExecutionDtoToReturn.add(convertNovaAgentDtoToBehaviorExecutionDto(instance, baBehaviorExecutionDTO)),
                    () -> {
                        BVBehaviorExecution executionDto = new BVBehaviorExecution();
                        executionDto.setExecutionStatus(BehaviorAction.REGISTER_ERROR.name());
                        fillCommonExecutionDataFromInstance(executionDto, instance);
                        behaviorVersionExecutionDtoToReturn.add(executionDto);
                    });
        });

        LOG.debug("[{}] -> [convertBehaviorServiceExecutionsToDto]: Object converted to return: [{}]", getClass().getSimpleName(), behaviorVersionExecutionDtoToReturn);

        return behaviorVersionExecutionDtoToReturn.toArray(new BVBehaviorExecution[0]);
    }

    /**
     * Converts a single execution DTO.
     *
     * @param instance            Behavior instance.
     * @param baBehaviorExecution Nova agent behavior execution DTO.
     * @return Behavior execution DTO.
     */
    private BVBehaviorExecution convertNovaAgentDtoToBehaviorExecutionDto(BehaviorInstance instance, BABehaviorExecutionDTO baBehaviorExecution)
    {
        BVBehaviorExecution executionDto = new BVBehaviorExecution();
        executionDto.setReportUrl(baBehaviorExecution.getReport());
        executionDto.setExitCode(baBehaviorExecution.getExitCode());
        executionDto.setStartDate(baBehaviorExecution.getStartDate());
        executionDto.setEndDate(baBehaviorExecution.getEndDate());
        executionDto.setExecutionStatus(baBehaviorExecution.getStatus());
        fillCommonExecutionDataFromInstance(executionDto, instance);

        executionDto.setMonitoringUrl(monitoringUtils.getMonitoringUrlForInstance(
                instance.getContainerName(),
                instance.getBehaviorServiceConfiguration().getBehaviorService().getBehaviorSubsystem().getBehaviorVersion().getProduct().getUuaa()));

        return executionDto;
    }

    /**
     * Fill common execution DTO fields from instance.
     * @param executionDto Execution DTO.
     * @param instance Behavior instance.
     */
    private void fillCommonExecutionDataFromInstance(BVBehaviorExecution executionDto, BehaviorInstance instance) {
        BehaviorService behaviorService = instance.getBehaviorServiceConfiguration().getBehaviorService();
        executionDto.setExecutionId(instance.getId());
        executionDto.setBehaviorServiceName(behaviorService.getServiceName());
        if (behaviorService.getReleaseVersion() != null)
        {
            executionDto.setReleaseAssociatedName(behaviorService.getReleaseVersion().getVersionName());
        }
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(behaviorService.getBehaviorSubsystem().getSubsystemId());
        executionDto.setBehaviorSubsystemName(subsystemDTO.getSubsystemName());
    }
}
