package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.AddressDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.broker.entities.*;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.entities.BrokerTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.brokersapi.model.BrokerValidatedObjects;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerUtils;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerTaskRepository;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerUsageType;
import com.bbva.enoa.platformservices.coreservice.common.util.MailUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.MonitoringUtils;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * Broker builder service
 */
@Service
public class BrokerBuilderImpl implements IBrokerBuilder
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerBuilderImpl.class);

    /**
     * Class name
     */
    private static final String CLASS_NAME = "BrokerBuilderImpl";

    /**
     * Rabbitmq admin username
     */
    public static final String RABBITMQ_ADMIN_USER = "admin";

    /**
     * Number of characters of the passwords
     */
    public static final int PASSWORD_LENGTH = 15;

    private static final int SIX_HOURS_IN_MILLISECONDS = 3600000 * 6;
    private static final int TWENTY_FOUR_HOURS_IN_MILLISECONDS = 3600000 * 24;
    private static final int ZERO_SECONDS = 0;
    private static final int ONE_MINUTE_IN_SECONDS = 60;

    /**
     * Broker validator
     */
    private final IBrokerValidator brokerValidator;

    /**
     * Monitoring Utils
     */
    private final MonitoringUtils monitoringUtils;

    /**
     * User service client
     */
    private final IUsersClient usersService;

    /**
     * Broker credential service
     */
    private final ICipherCredentialService cipherCredentialService;

    /**
     * Broker task repository
     */
    private final BrokerTaskRepository brokerTaskRepository;

    /**
     * Instantiates a new Broker builder.
     *
     * @param brokerValidator         the broker validator
     * @param monitoringUtils         the monitoring utils
     * @param usersService            the user service
     * @param cipherCredentialService the broker credential service
     * @param brokerTaskRepository    the broker task repository
     */
    @Autowired
    public BrokerBuilderImpl(final IBrokerValidator brokerValidator, final MonitoringUtils monitoringUtils,
                             final IUsersClient usersService, final ICipherCredentialService cipherCredentialService,
                             final BrokerTaskRepository brokerTaskRepository)
    {
        this.brokerValidator = brokerValidator;
        this.monitoringUtils = monitoringUtils;
        this.usersService = usersService;
        this.cipherCredentialService = cipherCredentialService;
        this.brokerTaskRepository = brokerTaskRepository;
    }

    @Override
    public Broker validateAndBuildBrokerEntity(final BrokerDTO brokerDTO)
    {
        LOG.debug("[{}] -> [validateAndBuildBrokerEntity]: Building entity from brokerDTO: [{}]", CLASS_NAME, brokerDTO);

        // Validate input DTO
        BrokerValidatedObjects brokerValidatedObjects = this.brokerValidator.validateBrokerDTO(brokerDTO);

        // Build broker
        Broker broker = this.buildBrokerEntity(brokerDTO, brokerValidatedObjects);

        LOG.debug("[{}] -> [validateAndBuildBrokerEntity]: Built entity [{}] from brokerDTO: [{}]", CLASS_NAME, broker, brokerDTO);

        return broker;
    }

    public BrokerDTO buildBrokerDTOFromEntity(String ivUser, Broker broker)
    {
        LOG.debug("[{}] -> [buildBrokerDTOFromEntity]: Building entity from brokerDTO: [{}]", CLASS_NAME, broker);

        // basic brokerDTO
        BrokerDTO brokerDTO = buildBasicBrokerDTOFromEntity(broker);
        // Broker details
        brokerDTO.setDetails(this.buildBrokerDetailsDTO(ivUser, broker));
        // Broker tasks
        brokerDTO.setHasPendingTask(this.buildBrokerTasksDTO(broker).toArray(PendingTaskDto[]::new));
        LOG.debug("[{}] -> [buildBrokerDTOFromEntity]: Built entity [{}] from brokerDTO: [{}]", CLASS_NAME, broker, brokerDTO);

        return brokerDTO;
    }

    @Override
    public ConnectionDTO buildConnectionDTOFromBrokerUserAndBrokerList(final BrokerUser brokerUser, final List<BrokerNode> brokerNodeList)
    {
        ConnectionDTO connectionDTO = new ConnectionDTO();

        connectionDTO.setPassword(this.cipherCredentialService.decryptPassword(brokerUser.getPassword()));
        connectionDTO.setUser(brokerUser.getName());
        connectionDTO.setAddresses(this.getAddressesFromNodeList(brokerNodeList));

        return connectionDTO;
    }

    private AddressDTO[] getAddressesFromNodeList(final List<BrokerNode> brokerNodeList)
    {
        List<AddressDTO> addressDTOs = new ArrayList<>();
        brokerNodeList.forEach(brokerNode -> {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setHost(brokerNode.getHostName());
            addressDTO.setPort(brokerNode.getManagementPort());
            addressDTOs.add(addressDTO);
        });

        return addressDTOs.toArray(AddressDTO[]::new);
    }

    @Override
    public BrokerDTO buildBasicBrokerDTOFromEntity(final Broker broker)
    {
        LOG.debug("[{}] -> [buildBasicBrokerDTOFromEntity]: Building DTO from broker entity: [{}]", CLASS_NAME, broker);

        BrokerDTO brokerDTO = this.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);

        LOG.debug("[{}] -> [buildBasicBrokerDTOFromEntity]: Built DTO [{}] from broker entity: [{}]", CLASS_NAME, brokerDTO, broker);

        return brokerDTO;
    }

    @Override
    public BrokerDTO buildBasicBrokerDTOFromEntityWithoutMonitoringURL(final Broker broker)
    {
        LOG.debug("[{}] -> [buildBasicBrokerDTOFromEntityWithoutMonitoringURL]: Building DTO from broker entity: [{}]", CLASS_NAME, broker);

        BrokerDTO brokerDTO = new BrokerDTO();
        BeanUtils.copyProperties(broker, brokerDTO);
        brokerDTO.setProductId(broker.getProduct().getId());
        brokerDTO.setEnvironment(broker.getEnvironment());
        brokerDTO.setType(broker.getType().getType());
        brokerDTO.setPlatform(broker.getPlatform().getName());
        brokerDTO.setStatus(broker.getStatus().getStatus());
        brokerDTO.setStatusChanged(BrokerUtils.formatCalendar(broker.getStatusChanged()));
        brokerDTO.setFilesystemId(broker.getFilesystem().getId());
        brokerDTO.setHardwarePackId(broker.getHardwarePack().getId());
        brokerDTO.setCpu(broker.getCpu().floatValue());
        brokerDTO.setCreationDate(BrokerUtils.formatCalendar(broker.getCreationDate()));
        brokerDTO.setLastModified(BrokerUtils.formatCalendar(broker.getLastModified()));
        brokerDTO.setHasPendingTask(this.buildBrokerTasksDTO(broker).toArray(PendingTaskDto[]::new));

        LOG.debug("[{}] -> [buildBasicBrokerDTOFromEntityWithoutMonitoringURL]: Built DTO [{}] from broker entity: [{}]", CLASS_NAME, brokerDTO, broker);

        return brokerDTO;
    }

    private BrokerDetailsDTO buildBrokerDetailsDTO(final String ivUser, final Broker broker)
    {
        BrokerDetailsDTO detailsDTO = new BrokerDetailsDTO();
        detailsDTO.setNodes(
                broker.getNodes().stream()
                        .sorted(Comparator.comparing(BrokerNode::getContainerName))
                        .map(this::buildBrokerNodeDTO)
                        .toArray(BrokerNodeDTO[]::new)
        );
        detailsDTO.setUsers(
                broker.getUsers().stream().map(brokerUser -> this.buildBrokerUserDTO(brokerUser, ivUser)).toArray(BrokerUserDTO[]::new)
        );
        detailsDTO.setDeploymentServices(
                broker.getDeploymentServices().stream().distinct().map(this::buildBrokerDeploymentServiceDTO).toArray(BrokerDeploymentServiceDTO[]::new)
        );
        this.buildAndAddBrokerAlertConfigs(broker, detailsDTO);

        return detailsDTO;
    }

    /**
     * Build broker entity from a given broker DTO and necessary validated objets to from a correct broker entity
     *
     * @param brokerDTO              broker dto
     * @param brokerValidatedObjects object needed in the entity creation, that have to been validated previously
     * @return a broker entity
     */
    private Broker buildBrokerEntity(final BrokerDTO brokerDTO, final BrokerValidatedObjects brokerValidatedObjects)
    {
        Broker broker = new Broker();
        broker.setName(brokerDTO.getName());
        broker.setDescription(brokerDTO.getDescription());
        broker.setProduct(brokerValidatedObjects.getProduct());
        broker.setEnvironment(brokerDTO.getEnvironment());
        broker.setStatus(BrokerStatus.CREATING);
        broker.setStatusChanged(Calendar.getInstance());
        broker.setType(BrokerType.valueOf(brokerDTO.getType()));
        broker.setPlatform(Platform.valueOf(brokerDTO.getPlatform()));
        broker.setFilesystem(brokerValidatedObjects.getFilesystem());
        broker.setNumberOfNodes(brokerDTO.getNumberOfNodes());
        broker.setHardwarePack(brokerValidatedObjects.getHardwarePack());
        broker.setCpu(brokerValidatedObjects.getHardwarePack().getNumCPU());
        broker.setMemory(brokerValidatedObjects.getHardwarePack().getRamMB());

        // Add users
        String uuaa = brokerValidatedObjects.getProduct().getUuaa().toLowerCase();
        BrokerUser serviceUser = this.createBrokerUser(broker, BrokerRole.SERVICE, uuaa);
        BrokerUser adminUser = this.createBrokerUser(broker, BrokerRole.ADMIN, RABBITMQ_ADMIN_USER);
        broker.setUsers(List.of(adminUser, serviceUser));

        // Add alert configuration
        this.addAlertConfiguration(broker);

        return broker;
    }

    private void buildAndAddBrokerAlertConfigs(final Broker broker, BrokerDetailsDTO detailsDTO)
    {
        // Set alerts configuration
        BrokerAlertConfigDTO alertConfigDTO = new BrokerAlertConfigDTO();

        // Set emails addresses
        String emailAddresses = broker.getGenericAlertConfigs().stream()
                .findFirst()
                .map(BrokerAlertConfig::getEmailAddresses)
                .orElse("");
        alertConfigDTO.setEmailAddresses(MailUtils.getEmailAddressesArray(emailAddresses));

        // Set generic alert configs
        broker.getGenericAlertConfigs().forEach(alertConfig -> {
            GenericBrokerAlertConfigDTO genericAlertConfigDTO = this.buildGenericBrokerAlertConfigDTO(alertConfig);

            switch (alertConfig.getType())
            {
                case BROKER_HEALTH:
                    alertConfigDTO.setBrokerHealthAlertConfig(genericAlertConfigDTO);
                    break;
                case UNAVAILABLE_NODE:
                    alertConfigDTO.setUnavailableNodeAlertConfig(genericAlertConfigDTO);
                    break;
                case OVERFLOWED_BROKER:
                    alertConfigDTO.setOverflowedBrokerAlertConfig(genericAlertConfigDTO);
                    break;
            }
        });

        // Set queue alert configs
        broker.getQueueAlertConfigs().forEach(alertConfig -> {
            QueueBrokerAlertConfigDTO queueAlertConfigDTO = this.buildQueueBrokerAlertConfigDTO(alertConfig);

            if (alertConfig.getType().equals(QueueBrokerAlertType.LENGTH_ABOVE))
            {
                alertConfigDTO.setQueueLengthAlertConfig(queueAlertConfigDTO);
            }
        });

        // Set rate alert configs
        broker.getRateAlertConfigs().forEach(alertConfig -> {
            RateThresholdBrokerAlertConfigDTO rateAlertConfigDTO = this.buildRateThresholdBrokerAlertConfigDTO(alertConfig);

            if (alertConfig.getType().equals(RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW))
            {
                alertConfigDTO.setConsumerRateAlertConfig(rateAlertConfigDTO);
            }
            else if (alertConfig.getType().equals(RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE))
            {
                alertConfigDTO.setPublishRateAlertConfig(rateAlertConfigDTO);
            }
        });

        detailsDTO.setAlertsConfig(alertConfigDTO);
    }

    private BrokerUser createBrokerUser(Broker broker, BrokerRole role, String userName)
    {
        BrokerUser user = new BrokerUser();
        user.setBroker(broker);
        user.setRole(role);
        user.setName(userName);
        user.setPassword(cipherCredentialService.generateEncryptedRandomPassword(PASSWORD_LENGTH));
        return user;
    }

    private BrokerAlertConfig createGenericAlertConfiguration(final Broker broker, final GenericBrokerAlertType alertType)
    {
        BrokerAlertConfig brokerAlertConfig = new BrokerAlertConfig();

        brokerAlertConfig.setBroker(broker);
        brokerAlertConfig.setActive(true);
        brokerAlertConfig.setSendMail(true);
        brokerAlertConfig.setType(alertType);
        brokerAlertConfig.setEmailAddresses(broker.getProduct().getEmail());

        if (alertType.equals(GenericBrokerAlertType.BROKER_HEALTH))
        {
            brokerAlertConfig.setTimeBetweenNotifications(SIX_HOURS_IN_MILLISECONDS);

            // Activate Patrol notification only for brokers in PRO and for products with remedy group defined
            boolean sendPatrol = broker.getEnvironment().equals(Environment.PRO.name()) && StringUtils.isNotEmpty(broker.getProduct().getRemedySupportGroup());
            brokerAlertConfig.setSendPatrol(sendPatrol);
        }
        else
        {
            brokerAlertConfig.setTimeBetweenNotifications(TWENTY_FOUR_HOURS_IN_MILLISECONDS);
            brokerAlertConfig.setSendPatrol(false);
        }

        if (alertType.equals(GenericBrokerAlertType.UNAVAILABLE_NODE))
        {
            brokerAlertConfig.setOpeningPeriod(ONE_MINUTE_IN_SECONDS);
        }
        else
        {
            brokerAlertConfig.setOpeningPeriod(ZERO_SECONDS);
        }

        return brokerAlertConfig;
    }

    private void addAlertConfiguration(Broker broker)
    {
        // Generic alerts configuration
        BrokerAlertConfig healthAlertConfig = this.createGenericAlertConfiguration(broker, GenericBrokerAlertType.BROKER_HEALTH);
        BrokerAlertConfig overflowedBrokerAlertConfig = this.createGenericAlertConfiguration(broker, GenericBrokerAlertType.OVERFLOWED_BROKER);
        List<BrokerAlertConfig> genericAlertConfigs = new ArrayList<>(Arrays.asList(healthAlertConfig, overflowedBrokerAlertConfig));

        if (broker.getNumberOfNodes() > 1)
        {
            BrokerAlertConfig unavailableNodeAlertConfig = this.createGenericAlertConfiguration(broker, GenericBrokerAlertType.UNAVAILABLE_NODE);
            Collections.addAll(genericAlertConfigs, unavailableNodeAlertConfig);
        }
        broker.setGenericAlertConfigs(genericAlertConfigs);

        // Queue alerts configuration
        QueueBrokerAlertConfig queueAlertConfig = this.createQueueLengthAlertConfiguration(broker);
        broker.setQueueAlertConfigs(List.of(queueAlertConfig));

        // Rate threshold alerts configuration
        RateThresholdBrokerAlertConfig publishRateAboveAlertConfig = this.createRateThresholdAlertConfiguration(broker, RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE);
        RateThresholdBrokerAlertConfig consumerRateBelowAlertConfig = this.createRateThresholdAlertConfiguration(broker, RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW);
        broker.setRateAlertConfigs(List.of(publishRateAboveAlertConfig, consumerRateBelowAlertConfig));
    }

    private QueueBrokerAlertConfig createQueueLengthAlertConfiguration(final Broker broker)
    {
        QueueBrokerAlertConfig queueAlertConfig = new QueueBrokerAlertConfig();

        queueAlertConfig.setBroker(broker);
        queueAlertConfig.setActive(false);
        queueAlertConfig.setSendMail(true);
        queueAlertConfig.setSendPatrol(false);
        queueAlertConfig.setType(QueueBrokerAlertType.LENGTH_ABOVE);
        queueAlertConfig.setEmailAddresses(broker.getProduct().getEmail());
        queueAlertConfig.setTimeBetweenNotifications(TWENTY_FOUR_HOURS_IN_MILLISECONDS);

        return queueAlertConfig;
    }

    private RateThresholdBrokerAlertConfig createRateThresholdAlertConfiguration(final Broker broker, final RateThresholdBrokerAlertType alertType)
    {
        RateThresholdBrokerAlertConfig rateAlertConfig = new RateThresholdBrokerAlertConfig();

        rateAlertConfig.setBroker(broker);
        rateAlertConfig.setActive(false);
        rateAlertConfig.setSendMail(true);
        rateAlertConfig.setSendPatrol(false);
        rateAlertConfig.setType(alertType);
        rateAlertConfig.setEmailAddresses(broker.getProduct().getEmail());
        rateAlertConfig.setTimeBetweenNotifications(Constants.NO_NOTIFY_UNTIL_CLOSE_ALERT);
        rateAlertConfig.setCalculationTimeInterval(60);

        return rateAlertConfig;
    }

    /**
     * Conversion from BrokerNode entity object to BrokerNodeDTO object
     *
     * @param brokerNode BrokerNode object to convert
     * @return converted BrokerNodeDTO object
     */
    private BrokerNodeDTO buildBrokerNodeDTO(BrokerNode brokerNode)
    {
        BrokerNodeDTO brokerNodeDTO = new BrokerNodeDTO();
        BeanUtils.copyProperties(brokerNode, brokerNodeDTO);
        brokerNodeDTO.setBrokerId(brokerNode.getBroker().getId());
        brokerNodeDTO.setStatus(brokerNode.getStatus().name());
        brokerNodeDTO.setStatusChanged(BrokerUtils.formatCalendar(brokerNode.getStatusChanged()));
        brokerNodeDTO.setMonitoringUrl(this.monitoringUtils.getMonitoringUrlForBrokerNode(brokerNode));

        return brokerNodeDTO;
    }

    private GenericBrokerAlertConfigDTO buildGenericBrokerAlertConfigDTO(final BrokerAlertConfig brokerAlertConfig)
    {
        GenericBrokerAlertConfigDTO genericAlertConfigDTO = new GenericBrokerAlertConfigDTO();

        genericAlertConfigDTO.setIsActive(brokerAlertConfig.isActive());
        genericAlertConfigDTO.setSendMail(brokerAlertConfig.isSendMail());
        genericAlertConfigDTO.setSendPatrol(brokerAlertConfig.isSendPatrol());
        genericAlertConfigDTO.setAlertType(brokerAlertConfig.getType().toString());
        genericAlertConfigDTO.setTimeBetweenNotifications(brokerAlertConfig.getTimeBetweenNotifications());
        genericAlertConfigDTO.setOpeningPeriod(brokerAlertConfig.getOpeningPeriod());

        return genericAlertConfigDTO;
    }

    private QueueBrokerAlertConfigDTO buildQueueBrokerAlertConfigDTO(final QueueBrokerAlertConfig queueAlertConfig)
    {
        QueueBrokerAlertConfigDTO queueAlertConfigDTO = new QueueBrokerAlertConfigDTO();

        queueAlertConfigDTO.setIsActive(queueAlertConfig.isActive());
        queueAlertConfigDTO.setSendMail(queueAlertConfig.isSendMail());
        queueAlertConfigDTO.setSendPatrol(queueAlertConfig.isSendPatrol());
        queueAlertConfigDTO.setAlertType(queueAlertConfig.getType().toString());
        queueAlertConfigDTO.setThresholdQueueLength(queueAlertConfig.getThresholdQueueLength());
        queueAlertConfigDTO.setTimeBetweenNotifications(queueAlertConfig.getTimeBetweenNotifications());

        return queueAlertConfigDTO;
    }

    private RateThresholdBrokerAlertConfigDTO buildRateThresholdBrokerAlertConfigDTO(final RateThresholdBrokerAlertConfig rateAlertConfig)
    {
        RateThresholdBrokerAlertConfigDTO rateAlertConfigDTO = new RateThresholdBrokerAlertConfigDTO();

        rateAlertConfigDTO.setIsActive(rateAlertConfig.isActive());
        rateAlertConfigDTO.setSendMail(rateAlertConfig.isSendMail());
        rateAlertConfigDTO.setSendPatrol(rateAlertConfig.isSendPatrol());
        rateAlertConfigDTO.setAlertType(rateAlertConfig.getType().toString());
        rateAlertConfigDTO.setThresholdRate(rateAlertConfig.getThresholdRate());
        rateAlertConfigDTO.setCalculationTimeInterval(rateAlertConfig.getCalculationTimeInterval());
        rateAlertConfigDTO.setTimeBetweenNotifications(rateAlertConfig.getTimeBetweenNotifications());

        return rateAlertConfigDTO;
    }

    /**
     * Build a DTO that represent a deployment services attached to a broker.
     *
     * @param deploymentService the deploymentService with the necessary information
     * @return The deployment Service DTO that represents a deployment service attached to a broker.
     */
    private BrokerDeploymentServiceDTO buildBrokerDeploymentServiceDTO(DeploymentService deploymentService)
    {
        BrokerDeploymentServiceDTO brokerDeploymentServiceDTO = new BrokerDeploymentServiceDTO();
        brokerDeploymentServiceDTO.setDeploymentPlanId(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getId());
        brokerDeploymentServiceDTO.setDeploymentPlanStatus(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus().getDeploymentStatus());
        brokerDeploymentServiceDTO.setServiceName(deploymentService.getService().getServiceName());
        brokerDeploymentServiceDTO.setServiceType(deploymentService.getService().getServiceType());
        brokerDeploymentServiceDTO.setReleaseName(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getName());
        brokerDeploymentServiceDTO.setReleaseVersionName(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getVersionName());
        brokerDeploymentServiceDTO.setIsRunning(isDeploymentServiceRunning(deploymentService));
        brokerDeploymentServiceDTO.setUsageType(buildBrokerServiceTypeConsume(deploymentService));
        brokerDeploymentServiceDTO.setUuaa(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa());

        return brokerDeploymentServiceDTO;
    }

    private boolean isDeploymentServiceRunning(DeploymentService service)
    {
        if (!DeploymentStatus.DEPLOYED.equals(service.getDeploymentSubsystem().getDeploymentPlan().getStatus()))
        {
            return false;
        }
        return service.getInstances().stream()
                .anyMatch(deploymentInstance -> Boolean.TRUE.equals(deploymentInstance.getStarted()));
    }

    private BrokerUserDTO buildBrokerUserDTO(BrokerUser brokerUser, String ivUser)
    {
        BrokerUserDTO brokerUserDTO = new BrokerUserDTO();

        brokerUserDTO.setName(brokerUser.getName());
        brokerUserDTO.setRole(brokerUser.getRole().name());

        Environment environment = Environment.valueOf(brokerUser.getBroker().getEnvironment());
        Integer productId = brokerUser.getBroker().getProduct().getId();

        if (this.userHasPermissionToViewPassword(ivUser, brokerUser.getRole(), productId, environment))
        {
            String plainTextPassword = cipherCredentialService.decryptPassword(brokerUser.getPassword());
            brokerUserDTO.setPassword(plainTextPassword);
        }

        return brokerUserDTO;
    }

    private boolean userHasPermissionToViewPassword(String ivUser, BrokerRole role, Integer productId, Environment environment)
    {
        switch (role)
        {
            case ADMIN:
                return usersService.hasPermission(ivUser, BrokerConstants.BrokerPermissions.VIEW_BROKER_ADMIN_PASSWORD);
            case SERVICE:
                return usersService.hasPermission(ivUser, BrokerConstants.BrokerPermissions.VIEW_BROKER_SERVICE_PASSWORD, environment.name(), productId);
            default:
                return false; // By default, other user type passwords are hidden
        }
    }

    private String buildBrokerServiceTypeConsume(DeploymentService deploymentService)
    {
        Boolean hasPublisher =  deploymentService.getService().getServers()
                .stream().anyMatch(apiImplementation -> apiImplementation.getApiModality().equals(ApiModality.ASYNC_BACKTOBACK));
        Boolean hasConsumers = deploymentService.getService().getConsumers()
                .stream().anyMatch(apiImplementation -> apiImplementation.getApiModality().equals(ApiModality.ASYNC_BACKTOBACK));

        return hasPublisher && hasConsumers ? BrokerUsageType.PUBLISHER_CONSUMER.getUsageType()
                : (hasPublisher ? BrokerUsageType.PUBLISHER.getUsageType() : (hasConsumers ? BrokerUsageType.CONSUMER.getUsageType() : ""));
    }

    public List<PendingTaskDto> buildBrokerTasksDTO(Broker broker)
    {
        List<BrokerTask> brokerTaskList = brokerTaskRepository.findByBrokerId(broker.getId());
        List<PendingTaskDto> pendingTaskDtoList = new ArrayList<>();
        brokerTaskList.stream().filter(brokerTask -> brokerTask.getStatus() == ToDoTaskStatus.PENDING || brokerTask.getStatus() == ToDoTaskStatus.PENDING_ERROR).forEach(brokerTask -> {
            PendingTaskDto pendingTaskDto = new PendingTaskDto();
            pendingTaskDto.setAssignedRole(brokerTask.getAssignedGroup().name());
            pendingTaskDto.setIsTaskOfError(brokerTask.getTaskType().isTaskOfError());
            pendingTaskDto.setTodoTaskId(brokerTask.getId());
            pendingTaskDto.setTodoTaskType(brokerTask.getTaskType().name());
            pendingTaskDtoList.add(pendingTaskDto);
        });
        return pendingTaskDtoList;
    }

}
