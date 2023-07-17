package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertInfoDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.GenericBrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.NodeAlertDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.QueueBrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.RateThresholdBrokerAlertConfigDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerMonitoringAlertType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerAlertService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerPermissions;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemAlertRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The broker alert configuration service.
 */
@Service
public class BrokerAlertServiceImpl implements IBrokerAlertService
{
    private static final NovaException PERMISSION_DENIED = new NovaException(BrokerError.getPermissionDeniedError());

    private final IUsersClient usersClient;

    private final IAlertServiceApiClient alertServiceClient;

    private final IBrokerValidator brokerValidator;

    private final BrokerRepository brokerRepository;

    private final FilesystemAlertRepository filesystemAlertRepository;

    private final INovaActivityEmitter novaActivityEmitter;

    private static final Logger LOG = LoggerFactory.getLogger(BrokerAlertServiceImpl.class);

    @Autowired
    public BrokerAlertServiceImpl(
            final IProductUsersClient usersClient,
            final IAlertServiceApiClient alertServiceClient,
            final IBrokerValidator brokerValidator,
            final BrokerRepository brokerRepository,
            final FilesystemAlertRepository filesystemAlertRepository,
            final INovaActivityEmitter novaActivityEmitter
    )
    {
        this.usersClient = usersClient;
        this.alertServiceClient = alertServiceClient;
        this.brokerValidator = brokerValidator;
        this.brokerRepository = brokerRepository;
        this.filesystemAlertRepository = filesystemAlertRepository;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    @Transactional
    @Override
    public void updateBrokerAlertConfiguration(final String ivUser, final Integer brokerId, final BrokerAlertConfigDTO brokerAlertConfigDTO)
    {
        LOG.debug("[BrokerAlertServiceImpl] -> [updateBrokerAlertConfiguration]: Generating the brokers alerts configurations from " +
                "DTO received: [{}], for the broker with Id: [{}]", brokerAlertConfigDTO, brokerId);

        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);

        // Check permissions
        this.usersClient.checkHasPermission(ivUser, BrokerPermissions.UPDATE_BROKER_ALERT_CONFIGURATION, broker.getProduct().getId(), PERMISSION_DENIED);

        // Validate alert configuration
        this.brokerValidator.validateBrokerAlertConfig(brokerAlertConfigDTO, broker);

        // Remove duplicate emails
        String emailList = Arrays.stream(brokerAlertConfigDTO.getEmailAddresses())
                .distinct()
                .collect(Collectors.joining(","));

        broker.getGenericAlertConfigs().forEach(alertConfig -> {
            switch (alertConfig.getType())
            {
                case BROKER_HEALTH:
                    this.updateGenericAlertConfiguration(alertConfig, brokerAlertConfigDTO.getBrokerHealthAlertConfig(), emailList);
                    break;
                case UNAVAILABLE_NODE:
                    this.updateGenericAlertConfiguration(alertConfig, brokerAlertConfigDTO.getUnavailableNodeAlertConfig(), emailList);
                    break;
                case OVERFLOWED_BROKER:
                    this.updateGenericAlertConfiguration(alertConfig, brokerAlertConfigDTO.getOverflowedBrokerAlertConfig(), emailList);
                    break;
            }
        });

        broker.getQueueAlertConfigs().forEach(alertConfig -> {
            if (alertConfig.getType().equals(QueueBrokerAlertType.LENGTH_ABOVE))
            {
                this.updateQueueAlertConfiguration(alertConfig, brokerAlertConfigDTO.getQueueLengthAlertConfig(), emailList);
            }
        });

        broker.getRateAlertConfigs().forEach(alertConfig -> {
            if (alertConfig.getType().equals(RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW))
            {
                this.updateRateThresholdAlertConfiguration(alertConfig, brokerAlertConfigDTO.getConsumerRateAlertConfig(), emailList);
            }
            else if (alertConfig.getType().equals(RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE))
            {
                this.updateRateThresholdAlertConfiguration(alertConfig, brokerAlertConfigDTO.getPublishRateAlertConfig(), emailList);
            }
        });

        // Emit activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(broker.getProduct().getId(), ActivityScope.BROKER, ActivityAction.EDITED)
                .entityId(brokerId)
                .environment(broker.getEnvironment())
                .addParam("brokerName", broker.getName())
                .addParam("changeType", "Alert configuration")
                .build());
    }


    @Override
    @Transactional(readOnly = true)
    public BrokerAlertInfoDTO[] getBrokerAlertsByProduct(Integer productId)
    {
        this.brokerValidator.validateAndGetProduct(productId);

        return this.brokerRepository.findByProductId(productId).stream()
                .map(this::getOpenAlerts)
                .toArray(BrokerAlertInfoDTO[]::new);
    }

    private BrokerAlertInfoDTO getOpenAlerts(Broker broker)
    {
        BrokerAlertInfoDTO brokerAlertInfoDTO = new BrokerAlertInfoDTO();

        brokerAlertInfoDTO.setBrokerId(broker.getId());
        brokerAlertInfoDTO.setBrokerAlerts(getOpenAlertsAssociatedToBroker(broker));
        brokerAlertInfoDTO.setNodeAlerts(getOpenAlertsAssociatedToNodes(broker));

        return brokerAlertInfoDTO;
    }

    private BrokerAlertDTO[] getOpenAlertsAssociatedToBroker(Broker broker)
    {
        // Get all alerts associated to broker
        List<BrokerAlertDTO> brokerAlertDTOs = this.getOpenAlertsFromAlertService(broker, broker.getId().toString()).stream()
                .filter(basicAlertInfo -> EnumUtils.isValidEnum(BrokerMonitoringAlertType.class, basicAlertInfo.getAlertType()))
                .map(this::mapToBrokerAlertDTO)
                .collect(Collectors.toList());

        // Include possible open alert of associated filesystem
        if (broker.getFilesystem() != null)
        {
            // Get associated filesystem alert and check if it has an open alert
            this.filesystemAlertRepository.findByFilesystemCodeId(broker.getFilesystem().getId()).ifPresent(filesystemAlert ->
                    this.getOpenAlertsFromAlertService(broker, filesystemAlert.getId().toString()).stream()
                            .filter(basicAlertInfo -> basicAlertInfo.getAlertType().equals("APP_FILE_SYSTEM"))
                            .map(this::mapToBrokerAlertDTO)
                            .forEach(brokerAlertDTOs::add));
        }

        return brokerAlertDTOs.toArray(new BrokerAlertDTO[0]);
    }

    private NodeAlertDTO[] getOpenAlertsAssociatedToNodes(Broker broker)
    {
        // One-node Brokers don't have node alerts
        if (broker.getNumberOfNodes() == 1)
        {
            return new NodeAlertDTO[0];
        }

        // Related id in alert service for node alerts is "<nodeId>-<brokerId>"
        String[] relatedIds = broker.getNodes().stream().map(node -> node.getId() + "-" + broker.getId()).toArray(String[]::new);

        return this.getOpenAlertsFromAlertService(broker, relatedIds).stream()
                .filter(basicAlertInfo -> basicAlertInfo.getAlertType().equals(BrokerMonitoringAlertType.APP_UNAVAILABLE_NODE_BROKER.name()))
                .map(this::mapToNodeAlertDTO)
                .toArray(NodeAlertDTO[]::new);
    }

    private List<ASBasicAlertInfoDTO> getOpenAlertsFromAlertService(Broker broker, String... relatedIds)
    {
        Product product = broker.getProduct();

        ASRequestAlertsDTO response = alertServiceClient.getAlertsByRelatedIdAndStatus(relatedIds, product.getId(), product.getUuaa(), Constants.OPEN_ALERT_STATUS);

        if (response == null || response.getBasicAlertInfo() == null)
        {
            return new ArrayList<>();
        }

        return Arrays.asList(response.getBasicAlertInfo());
    }

    private NodeAlertDTO mapToNodeAlertDTO(ASBasicAlertInfoDTO basicAlertInfo)
    {
        NodeAlertDTO nodeAlertDTO = new NodeAlertDTO();
        nodeAlertDTO.setAlertType(BrokerMonitoringAlertType.APP_UNAVAILABLE_NODE_BROKER.name());
        Integer nodeId = Integer.parseInt(basicAlertInfo.getAlertRelatedId().split("-")[0]);
        nodeAlertDTO.setNodeId(nodeId);
        nodeAlertDTO.setAlertId(basicAlertInfo.getAlertId());
        return nodeAlertDTO;
    }

    private BrokerAlertDTO mapToBrokerAlertDTO(ASBasicAlertInfoDTO basicAlertInfo)
    {
        BrokerAlertDTO brokerAlertDTO = new BrokerAlertDTO();
        brokerAlertDTO.setAlertType(basicAlertInfo.getAlertType());
        brokerAlertDTO.setAlertId(basicAlertInfo.getAlertId());
        return brokerAlertDTO;
    }

    private void updateGenericAlertConfiguration(
            BrokerAlertConfig brokerAlertConfig,
            final GenericBrokerAlertConfigDTO genericAlertConfigDTO,
            final String emailsAddresses)
    {
        brokerAlertConfig.setEmailAddresses(emailsAddresses);
        brokerAlertConfig.setActive(genericAlertConfigDTO.getIsActive());
        brokerAlertConfig.setSendMail(genericAlertConfigDTO.getSendMail());
        brokerAlertConfig.setSendPatrol(genericAlertConfigDTO.getSendPatrol());
        brokerAlertConfig.setType(GenericBrokerAlertType.valueOf(genericAlertConfigDTO.getAlertType()));
        brokerAlertConfig.setTimeBetweenNotifications(genericAlertConfigDTO.getTimeBetweenNotifications());
        brokerAlertConfig.setOpeningPeriod(genericAlertConfigDTO.getOpeningPeriod());
    }

    private void updateRateThresholdAlertConfiguration(
            RateThresholdBrokerAlertConfig rateAlertConfig,
            final RateThresholdBrokerAlertConfigDTO rateAlertConfigDTO,
            final String emailsAddresses)
    {
        rateAlertConfig.setEmailAddresses(emailsAddresses);
        rateAlertConfig.setActive(rateAlertConfigDTO.getIsActive());
        rateAlertConfig.setSendMail(rateAlertConfigDTO.getSendMail());
        rateAlertConfig.setSendPatrol(rateAlertConfigDTO.getSendPatrol());
        // Cero o mayor, consumo solo mayores a cero
        rateAlertConfig.setThresholdRate(rateAlertConfigDTO.getThresholdRate());
        rateAlertConfig.setCalculationTimeInterval(rateAlertConfigDTO.getCalculationTimeInterval());
        rateAlertConfig.setTimeBetweenNotifications(rateAlertConfigDTO.getTimeBetweenNotifications());
        rateAlertConfig.setType(RateThresholdBrokerAlertType.valueOf(rateAlertConfigDTO.getAlertType()));
    }

    private void updateQueueAlertConfiguration(
            QueueBrokerAlertConfig queueAlertConfig,
            final QueueBrokerAlertConfigDTO queueAlertConfigDTO,
            final String emailsAddresses)
    {
        queueAlertConfig.setEmailAddresses(emailsAddresses);
        queueAlertConfig.setActive(queueAlertConfigDTO.getIsActive());
        queueAlertConfig.setSendMail(queueAlertConfigDTO.getSendMail());
        queueAlertConfig.setSendPatrol(queueAlertConfigDTO.getSendPatrol());
        // Mayor que cero
        queueAlertConfig.setThresholdQueueLength(queueAlertConfigDTO.getThresholdQueueLength());
        queueAlertConfig.setType(QueueBrokerAlertType.valueOf(queueAlertConfigDTO.getAlertType()));
        queueAlertConfig.setTimeBetweenNotifications(queueAlertConfigDTO.getTimeBetweenNotifications());
    }
}
