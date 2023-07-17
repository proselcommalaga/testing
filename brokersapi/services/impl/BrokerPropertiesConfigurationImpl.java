package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.AsyncBackToBackChannelType;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerProperty;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.config.enumerates.PropertyType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerPropertiesConfiguration;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerNamingConventionUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The type Broker properties configuration.
 */
@Service
@Slf4j
public class BrokerPropertiesConfigurationImpl implements IBrokerPropertiesConfiguration
{

    private static final String CLASS_NAME = "BrokerPropertiesConfigurationImpl";
    private static final String RABBIT_TYPE = "rabbit";
    public static final String COLON_DELIMITER = ":";
    public static final String SEMICOLON_DELIMITER = ";";
    private static final String COMMA_DELIMITER = ",";
    private static final String SPRING_CLOUD_FUNCTION_DEFINITION = "spring.cloud.function.definition";
    private static final String SPRING_CLOUD_STREAM_BINDINGS_DESTINATION_KEY_PATTERN = "spring.cloud.stream.bindings.%s.destination";
    private static final String SPRING_CLOUD_STREAM_BINDINGS_PROPERTY_KEY_PATTERN = "%s-%s-0";
    private static final String MANAGEMENT_HEALTH_RABBIT_ENABLED = "management.health.rabbit.enabled";


    /**
     * Broker credential service
     */
    private final ICipherCredentialService cipherCredentialService;

    /**
     * Instantiates a new Broker properties configuration service
     *
     * @param cipherCredentialService the broker credential service
     */
    @Autowired
    public BrokerPropertiesConfigurationImpl(final ICipherCredentialService cipherCredentialService)
    {
        this.cipherCredentialService = cipherCredentialService;
    }

    @Override
    public List<BrokerProperty> getBrokerSCSPropertiesDependentOnBroker(final Broker broker)
    {
        List<BrokerProperty> brokerSCSProperties = new ArrayList<>();
        brokerSCSProperties.add(getSCSBindersBrokerTypeProperty(broker));
        brokerSCSProperties.add(getSCSBindersEnvironmentSpringAddresses(broker));
        brokerSCSProperties.add(getSCSBindersEnvironmentSpringUsername(broker));
        brokerSCSProperties.add(getSCSBindersEnvironmentSpringPassword(broker));
        return brokerSCSProperties;
    }

    @Override
    // TODO @ santi adaptar al formato del resto de EP, que devuelva una lista de brokerproperty en lugar de mapa
    public Map<String, String> getBrokerSCSPropertiesDependentOnReleaseVersionService(final ReleaseVersionService releaseVersionService)
    {

        log.debug("[{}] -> [getBrokerSCSPropertiesDependentOnReleaseVersionService]: Getting brokerPropertyMap for RVservice: [{}]", CLASS_NAME, releaseVersionService);
        Map<String, String> brokerPropertiesMap = new HashMap<>();
        List<AsyncBackToBackApiImplementation> asyncApiBackToBackList = this.getAsyncApisConsumedAndServedForReleaseVersionService(releaseVersionService);
        asyncApiBackToBackList.forEach(
                // Get broker properties for each asyncapi
                asyncBackToBackApiImplementation -> this.getAsyncApiBrokerPropertiesMap(asyncBackToBackApiImplementation, releaseVersionService)
                        // merge properties of each asyncapi in a single map
                        .forEach((key, value) -> brokerPropertiesMap.merge(key, value, (value1, value2) -> (!value1.equals(value2)) ? String.join(SEMICOLON_DELIMITER, value1, value2) : value1)
                        ));
        log.debug("[{}] -> [getBrokerSCSPropertiesDependentOnReleaseVersionService]: BrokerPropertyMap for RVservice: [{}] and AsyncAPIBackToBackList: [{}]. Result is: [{}]", CLASS_NAME, releaseVersionService.getServiceName(), asyncApiBackToBackList, brokerPropertiesMap);
        return brokerPropertiesMap;
    }

    @Override
    public List<BrokerProperty> getBrokerSCSPropertiesDependentOnServiceAndBroker(final ReleaseVersionService releaseVersionService, final Broker broker)
    {
        List<BrokerProperty>  brokerPropertyList = getSCSBindingsChannelBinder(releaseVersionService, broker);
        // Set property management.health.rabbit.enabled = false to disable health check verification for the broker
        if(broker.getType().equals(BrokerType.PUBLISHER_SUBSCRIBER)
                && Arrays.asList(ServiceType.API_JAVA_SPRING_BOOT.name(), ServiceType.API_REST_JAVA_SPRING_BOOT.name()).contains(releaseVersionService.getServiceType()))
        {
            brokerPropertyList.add(getBrokerPropertyHealthCheck());
        }
        return brokerPropertyList;
    }

    /**
     * get the binder used by this binding
     *
     * @param releaseVersionService release version service
     * @param broker                broker
     * @return list of broker properties
     */
    private List<BrokerProperty> getSCSBindingsChannelBinder(final ReleaseVersionService releaseVersionService, final Broker broker)
    {
        List<BrokerProperty> brokerPropertyList = new ArrayList<>();
        List<AsyncBackToBackApiImplementation> asyncBackToBackApiImplementationList = this.getAsyncApisConsumedAndServedForReleaseVersionService(releaseVersionService);
        asyncBackToBackApiImplementationList.forEach(asyncBackToBackApiImplementation -> {
            BrokerProperty brokerProperty = this.getBrokerPropertyWithCommonValues();
            String operationId = asyncBackToBackApiImplementation.getApiVersion().getAsyncBackToBackApiChannel().getOperationId();
            boolean subscribe = asyncBackToBackApiImplementation.getApiVersion().getAsyncBackToBackApiChannel().getChannelType().equals(AsyncBackToBackChannelType.SUBSCRIBE);
            String propertyName = String.format("spring.cloud.stream.bindings.%s.binder", String.format(SPRING_CLOUD_STREAM_BINDINGS_PROPERTY_KEY_PATTERN, operationId, subscribe ? "in" : "out"));
            brokerProperty.setName(propertyName);
            brokerProperty.setDefaultName(propertyName);
            brokerProperty.setPropertyValue(getBinderName(broker));
            brokerProperty.setEncrypted(false);
            brokerPropertyList.add(brokerProperty);
        });
        return brokerPropertyList;
    }


    /**
     * Get binder type broker property
     *
     * @param broker broker
     * @return broker property
     */
    private BrokerProperty getSCSBindersBrokerTypeProperty(Broker broker)
    {
        BrokerProperty brokerProperty = this.getBrokerPropertyWithCommonValues();
        String propertyName = String.format("spring.cloud.stream.binders.%s.type", getBinderName(broker));
        brokerProperty.setName(propertyName);
        brokerProperty.setDefaultName(propertyName);
        brokerProperty.setPropertyValue(RABBIT_TYPE);  // Change for support other kinds of brokers or create another implementation
        brokerProperty.setEncrypted(false);
        return brokerProperty;
    }

    /**
     * Get username broker property
     *
     * @param broker broker
     * @return broker property
     */
    private BrokerProperty getSCSBindersEnvironmentSpringUsername(Broker broker)
    {
        BrokerProperty brokerProperty = this.getBrokerPropertyWithCommonValues();
        String propertyName = String.format("spring.cloud.stream.binders.%s.environment.spring.rabbitmq.username", getBinderName(broker));  // Change for support other kinds of brokers or create another implementation
        String propertyValue = broker.getProduct().getUuaa().toLowerCase();
        brokerProperty.setName(propertyName);
        brokerProperty.setDefaultName(propertyName);
        brokerProperty.setPropertyValue(propertyValue);
        brokerProperty.setEncrypted(false);
        return brokerProperty;
    }

    /**
     * Get password broker property
     *
     * @param broker broker
     * @return broker property
     */
    private BrokerProperty getSCSBindersEnvironmentSpringPassword(Broker broker)
    {
        BrokerProperty brokerProperty = this.getBrokerPropertyWithCommonValues();
        String propertyName = String.format("spring.cloud.stream.binders.%s.environment.spring.rabbitmq.password", getBinderName(broker)); // Change for support other kinds of brokers or create another implementation
        String propertyValue = this.getBrokerServicePassword(broker);
        brokerProperty.setName(propertyName);
        brokerProperty.setDefaultName(propertyName);
        brokerProperty.setPropertyValue(propertyValue);
        brokerProperty.setEncrypted(true);
        return brokerProperty;
    }

    private String getBrokerServicePassword(final Broker broker)
    {
        String uuaa = broker.getProduct().getUuaa().toLowerCase();

        String encryptedPassword = broker.getUsers().stream()
                .filter(brokerUser -> BrokerRole.SERVICE.equals(brokerUser.getRole()))
                .filter(brokerUser -> brokerUser.getName().equals(uuaa))
                .findFirst()
                .map(BrokerUser::getPassword)
                .orElseThrow(() -> new NovaException(BrokerError.getCredentialsNotFoundForRoleError(BrokerRole.SERVICE.getRole(), broker.getId())));

        return cipherCredentialService.decryptPassword(encryptedPassword);
    }

    /**
     * Get addresses broker property
     *
     * @param broker broker
     * @return broker property
     */
    private BrokerProperty getSCSBindersEnvironmentSpringAddresses(Broker broker)
    {
        BrokerProperty brokerProperty = this.getBrokerPropertyWithCommonValues();
        String propertyName = String.format("spring.cloud.stream.binders.%s.environment.spring.rabbitmq.addresses", getBinderName(broker)); // Change for support other kinds of brokers or create another implementation
        String propertyValue = String.join(COMMA_DELIMITER, this.getBrokerAddresses(broker));
        brokerProperty.setName(propertyName);
        brokerProperty.setDefaultName(propertyName);
        brokerProperty.setPropertyValue(propertyValue);
        brokerProperty.setEncrypted(false);
        return brokerProperty;
    }

    /**
     * Get binder name using UUAA and broker name
     *
     * @param broker broker
     * @return binder name
     */
    private String getBinderName(Broker broker)
    {
        return String.format("%s-%s", broker.getProduct().getUuaa().toLowerCase(), broker.getName());
    }

    private List<String> getBrokerAddresses(final Broker broker)
    {
        return broker.getNodes()
                .stream()
                .sorted(Comparator.comparing(BrokerNode::getContainerName))
                .map(brokerNode -> String.join(COLON_DELIMITER, brokerNode.getHostName(), brokerNode.getServicePort().toString()))
                .collect(Collectors.toList());
    }

    /**
     * Get broker property list map for a single asyncapi, for a given asyncBackToBackApiImplementation
     *
     * @param asyncBackToBackApiImplementation async api implementation
     * @return mapa de propiedades de un asincapi
     */
    private Map<String, String> getAsyncApiBrokerPropertiesMap(final AsyncBackToBackApiImplementation asyncBackToBackApiImplementation, final ReleaseVersionService releaseVersionService)
    {

        Map<String, String> asyncApiBrokerPropertiesMap = new HashMap<>();

        // Retrieve necessary info from asyncAPI and generate properties
        String operationId = asyncBackToBackApiImplementation.getApiVersion().getAsyncBackToBackApiChannel().getOperationId();
        boolean subscribe = asyncBackToBackApiImplementation.getApiVersion().getAsyncBackToBackApiChannel().getChannelType().equals(AsyncBackToBackChannelType.SUBSCRIBE);

        // Set Map from Broker properties for this AsyncApi
        // Set property: spring.cloud.function.definition = operationId
        asyncApiBrokerPropertiesMap.put(SPRING_CLOUD_FUNCTION_DEFINITION, operationId);
        // Set property: spring.cloud.function.stream.bindings.{operationId-out-0}.destination = uuaa_channelName_v_majorApiVersion
        asyncApiBrokerPropertiesMap.put(
                String.format(SPRING_CLOUD_STREAM_BINDINGS_DESTINATION_KEY_PATTERN, String.format(SPRING_CLOUD_STREAM_BINDINGS_PROPERTY_KEY_PATTERN, operationId, subscribe ? "in" : "out")),
                BrokerNamingConventionUtils.getPubSubChannelName(asyncBackToBackApiImplementation.getApiVersion()));
        // if subscribe add consumer group property
        if (subscribe)
        {
            asyncApiBrokerPropertiesMap.putAll(getConsumerGroupBrokerProperty(releaseVersionService, operationId));
        }

        // Set properties to avoid create exchanges and queues
        asyncApiBrokerPropertiesMap.putAll(getAvoidResourceCreationBrokerProperties(subscribe));

        return asyncApiBrokerPropertiesMap;
    }

    private Map<String, String> getConsumerGroupBrokerProperty(final ReleaseVersionService releaseVersionService, final String operationId)
    {
        var uuaa = releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa();
        return Map.of(
                String.format("spring.cloud.stream.bindings.%s.group", String.format(SPRING_CLOUD_STREAM_BINDINGS_PROPERTY_KEY_PATTERN, operationId, "in")),
                BrokerNamingConventionUtils.getPubSubConsumerGroupName(uuaa, releaseVersionService.getServiceName())
        );
    }

    /**
     * Avoid exchange and queue automatic creation on RabbitMQ
     * @param subscribe if is subscriber
     * @return property map
     */
    private Map<String, String> getAvoidResourceCreationBrokerProperties(boolean subscribe)
    {
        String clientType = subscribe ? "consumer" : "producer";
        return Map.of(
                String.format("spring.cloud.stream.rabbit.default.%s.declareExchange", clientType), "false",
                String.format("spring.cloud.stream.rabbit.default.%s.bindQueue", clientType), "false"
                );
    }

    private List<AsyncBackToBackApiImplementation> getAsyncApisConsumedAndServedForReleaseVersionService(ReleaseVersionService releaseVersionService)
    {
        List<AsyncBackToBackApiImplementation> asyncApiBackToBackListServed =
                releaseVersionService.getServers()
                        .stream()
                        .filter(apiImplementation -> apiImplementation.getApiModality().equals(ApiModality.ASYNC_BACKTOBACK))
                        .map(AsyncBackToBackApiImplementation.class::cast)
                        .collect(Collectors.toList());

        List<AsyncBackToBackApiImplementation> asyncApiBackToBackListConsumed =
                releaseVersionService.getConsumers()
                        .stream()
                        .filter(apiImplementation -> apiImplementation.getApiModality().equals(ApiModality.ASYNC_BACKTOBACK))
                        .map(AsyncBackToBackApiImplementation.class::cast)
                        .collect(Collectors.toList());

        List<AsyncBackToBackApiImplementation> asyncApiBackToBackList = new ArrayList<>();
        asyncApiBackToBackList.addAll(asyncApiBackToBackListServed);
        asyncApiBackToBackList.addAll(asyncApiBackToBackListConsumed);

        return asyncApiBackToBackList;
    }

    private BrokerProperty getBrokerPropertyWithCommonValues()
    {
        BrokerProperty brokerProperty = new BrokerProperty();
        brokerProperty.setManagement(ManagementType.BROKER);
        brokerProperty.setPropertyType(PropertyType.STRING);
        return brokerProperty;
    }

    /**
     * Get rabbit health check property
     *
     * @return broker property
     */
    private BrokerProperty getBrokerPropertyHealthCheck()
    {
        BrokerProperty brokerProperty = this.getBrokerPropertyWithCommonValues();
        String propertyName = MANAGEMENT_HEALTH_RABBIT_ENABLED;
        String propertyValue = String.valueOf(Boolean.FALSE);
        brokerProperty.setName(propertyName);
        brokerProperty.setDefaultName(propertyName);
        brokerProperty.setPropertyValue(propertyValue);
        brokerProperty.setEncrypted(false);
        return brokerProperty;
    }
}
