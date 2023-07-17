package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.AddressDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ChannelDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ChannelsActionDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ExchangeDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.QueueDTO;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerConfigurator;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerNamingConventionUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBrokerAgentApiClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Broker configurator.
 */
@Component
@Slf4j
public class BrokerConfiguratorImpl implements IBrokerConfigurator
{

    private final IBrokerAgentApiClient brokerAgentClient;
    private final ICipherCredentialService cipherCredentialService;

    /**
     * Instantiates a new Broker configurator.
     *
     * @param brokerAgentClient       the broker agent client
     * @param cipherCredentialService the broker credential service
     */
    @Autowired
    public BrokerConfiguratorImpl(final IBrokerAgentApiClient brokerAgentClient, final ICipherCredentialService cipherCredentialService)
    {
        this.brokerAgentClient = brokerAgentClient;
        this.cipherCredentialService = cipherCredentialService;
    }

    @Override
    public void configureBrokersForDeploymentPlan(final DeploymentPlan deploymentPlan)
    {
        if (existsBrokersInDeploymentPlan(deploymentPlan))
        {
            log.debug("[BrokerConfiguratorImpl] -> [configureBrokersForDeploymentPlan]: Configuring brokers for deployment plan: [{}]", deploymentPlan.getId());

            this.getChannelActionDTOListFromDeploymentPlan(deploymentPlan)
                    .forEach(channelsActionDTO -> this.brokerAgentClient.createChannels(deploymentPlan.getEnvironment(), channelsActionDTO));

            log.debug("[BrokerConfiguratorImpl] -> [configureBrokersForDeploymentPlan]: Configured brokers for deployment plan: [{}]", deploymentPlan.getId());
        }
    }


    /**
     * Check if exists brokers in any service for given deployment plan
     *
     * @param deploymentPlan The {@link DeploymentPlan} plan to check
     * @return true if there are any services that uses a broker
     */
    private boolean existsBrokersInDeploymentPlan(final DeploymentPlan deploymentPlan)
    {
        return deploymentPlan.getDeploymentSubsystems()                                                 // For each subsystem in plan
                .stream()
                .anyMatch(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices()            // For each service in subsystem
                        .stream()
                        .anyMatch(deploymentService ->
                                deploymentService.getBrokers() != null && !deploymentService.getBrokers().isEmpty() && deploymentService.getBrokers()
                                        .stream()
                                        .anyMatch(broker -> BrokerType.PUBLISHER_SUBSCRIBER.equals(broker.getType()))));     // Check if have brokers
    }

    /**
     * Build a list of {@link ChannelsActionDTO} from a {@link DeploymentPlan}.
     *
     * @param deploymentPlan The {@link DeploymentPlan} with all the information necessary to build the list
     * @return list of {@link ChannelsActionDTO}
     */
    private List<ChannelsActionDTO> getChannelActionDTOListFromDeploymentPlan(final DeploymentPlan deploymentPlan)
    {
        List<ChannelsActionDTO> channelsActionDTOList = new ArrayList<>();
        deploymentPlan.getDeploymentSubsystems()
                .forEach(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices()                                                     // For each service
                        .stream()
                        .filter(deploymentService -> !deploymentService.getBrokers().isEmpty())                                                 // If have brokers
                        .forEach(deploymentService -> channelsActionDTOList.add(buildChannelDTOActionForDeploymentService(deploymentService))   // Add ChannelDTOAction build for that service
                        ));

        log.debug("[BrokerConfiguratorImpl] -> [buildChannelsActionDTOFromDeploymentPlan]: Built ChannelsActionDTO List [{}] for deployment plan: [{}]", channelsActionDTOList, deploymentPlan.getId());

        return channelsActionDTOList;
    }

    /**
     * Build a {@link ChannelsActionDTO} from a {@link DeploymentService}
     *
     * @param deploymentService The {@link DeploymentService} with all the information necessary to build the object
     * @return The {@link ChannelsActionDTO} built
     */
    private ChannelsActionDTO buildChannelDTOActionForDeploymentService(final DeploymentService deploymentService)
    {
        ChannelsActionDTO channelsActionDTO = new ChannelsActionDTO();

        ConnectionDTO connectionDTO = buildConnectionDTOFromDeploymentService(deploymentService);  // Note: adapt when it would be able to associate more than one broker to a service
        List<ChannelDTO> channelDTOList = buildChannelDTOListFromDeploymentService(deploymentService);
        channelsActionDTO.setConnection(connectionDTO);
        channelsActionDTO.setChannels(channelDTOList.toArray(ChannelDTO[]::new));

        log.info("[BrokerConfiguratorImpl] -> [buildChannelsActionDTOFromDeploymentPlan]: Built channelsActionDTO [{}] for deployment service: [{}]", channelsActionDTO, deploymentService.getId());

        return channelsActionDTO;
    }

    /**
     * Build {@link ConnectionDTO} from a {@link DeploymentService}
     *
     * @param deploymentService The {@link DeploymentService}
     * @return The {@link ConnectionDTO} built
     */
    private ConnectionDTO buildConnectionDTOFromDeploymentService(final DeploymentService deploymentService)
    {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        Broker pubSubBroker = deploymentService.getBrokers()                                              // Get Broker
                .stream()
                .filter(broker -> BrokerType.PUBLISHER_SUBSCRIBER.equals(broker.getType()))                   // Get PubSub brokers
                .findFirst()
                .orElseThrow(() -> new NovaException(BrokerError.getBrokerNotFoundInDeploymentService(deploymentService.getId())));

        BrokerUser brokerAdminUser = pubSubBroker.getUsers()                                                          // Get brokerAdminUser
                .stream()
                .filter(brokerUser -> brokerUser.getRole().equals(BrokerRole.ADMIN))
                .findFirst()
                .orElseThrow(() -> new NovaException(BrokerError.getUserAdminNotFoundInBroker(pubSubBroker.getId())));

        List<AddressDTO> addressDTOList = pubSubBroker.getNodes()                                                 // Build addressDTOList from broker nodes
                .stream()
                .sorted(Comparator.comparing(BrokerNode::getContainerName))
                .map(this::buildAddressDTOFromBrokerNode)
                .collect(Collectors.toList());

        connectionDTO.setAddresses(addressDTOList.toArray(AddressDTO[]::new));                                  // Set Addresses
        connectionDTO.setPassword(this.cipherCredentialService.decryptPassword(brokerAdminUser.getPassword())); // Decrypt and set password
        connectionDTO.setUser(brokerAdminUser.getName());                                                       // Set user admin name

        return connectionDTO;
    }

    /**
     * Build {@link AddressDTO} from a {@link BrokerNode}
     *
     * @param brokerNode The {@link BrokerNode} that have all the information necessary
     * @return The {@link AddressDTO} built
     */
    private AddressDTO buildAddressDTOFromBrokerNode(final BrokerNode brokerNode)
    {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setHost(brokerNode.getHostName());
        addressDTO.setPort(brokerNode.getServicePort());
        return addressDTO;
    }

    /**
     * Build a List of {@link ChannelDTO} from a {@link DeploymentService}
     *
     * @param deploymentService The {@link DeploymentService} that have all the information necessary
     * @return The List of {@link ChannelDTO} built
     */
    private List<ChannelDTO> buildChannelDTOListFromDeploymentService(final DeploymentService deploymentService)
    {
        List<ChannelDTO> channelDTOList = new ArrayList<>();

        String uuaa = deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct().getUuaa();
        String serviceName = deploymentService.getService().getServiceName();
        channelDTOList.addAll(
                // get channel info necessaries for subscribers -> exchange and queue
                deploymentService.getService().getConsumers()                                                                       // For each CONSUMED(equivalent to subscriber) api in service
                        .stream()
                        .filter(apiImplementation -> ApiModality.ASYNC_BACKTOBACK.equals(apiImplementation.getApiModality()))       // Filter only BackToBack modality
                        .map(AsyncBackToBackApiImplementation.class::cast)
                        .map(apiImplementationPublish -> buildChannelDTOForSubscriber(uuaa, serviceName, apiImplementationPublish)) // Build channelDTO necessary for subscriber (queue+exchange info)
                        .collect(Collectors.toList()));

        channelDTOList.addAll(
                // get channel info necessaries for publishers -> only exchanges
                deploymentService.getService().getServers()                                                                         // For each SERVED(equivalent to publish) api in service
                        .stream()
                        .filter(apiImplementation -> ApiModality.ASYNC_BACKTOBACK.equals(apiImplementation.getApiModality()))       // Filter only BackToBack modality
                        .map(AsyncBackToBackApiImplementation.class::cast)
                        .map(this::buildChannelDTOForPublisher)                                                                     // Build channelDTO necessary for publisher (exchange info)
                        .collect(Collectors.toList()));
        return channelDTOList;
    }

    /**
     * Build a List of {@link ChannelDTO} from a {@link DeploymentService} used as subscriber
     * For a subscriber is mandatory to have the {@link QueueDTO} and the {@link ExchangeDTO} where the queue have to be binded.
     *
     * @param apiImplementationSubscribe The {@link AsyncBackToBackApiImplementation} that have all the information necessary
     * @return The {@link ChannelDTO} built
     */
    private ChannelDTO buildChannelDTOForSubscriber(final String uuaa, final String serviceName, final AsyncBackToBackApiImplementation apiImplementationSubscribe)
    {
        ChannelDTO channelDTO = buildChannelDTOForPublisher(apiImplementationSubscribe);
        QueueDTO queueDTO = new QueueDTO();
        queueDTO.setName(BrokerNamingConventionUtils.getPubSubQueueName(
                uuaa,
                serviceName,
                apiImplementationSubscribe.getApiVersion()));
        queueDTO.setExchangeName(BrokerNamingConventionUtils.getPubSubChannelName(apiImplementationSubscribe.getApiVersion()));
        channelDTO.setQueue(queueDTO);
        return channelDTO;
    }

    /**
     * Build a {@link ChannelDTO} from a {@link AsyncBackToBackApiImplementation} used as publisher
     * For a publisher is only mandatory to have the {@link ExchangeDTO} where the service is going to publish.
     *
     * @param apiImplementationPublish The {@link AsyncBackToBackApiImplementation} that have all the information necessary
     * @return The {@link ChannelDTO} built
     */
    private ChannelDTO buildChannelDTOForPublisher(final AsyncBackToBackApiImplementation apiImplementationPublish)
    {
        ChannelDTO channelDTO = new ChannelDTO();
        ExchangeDTO exchangeDTO = new ExchangeDTO();
        exchangeDTO.setName(BrokerNamingConventionUtils.getPubSubChannelName(apiImplementationPublish.getApiVersion()));
        channelDTO.setExchange(exchangeDTO);
        return channelDTO;
    }


}
