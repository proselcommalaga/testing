package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.AddressDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ChannelDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ChannelsActionDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ExchangeDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.QueueDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiChannel;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBrokerAgentApiClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class BrokerConfiguratorImplTest
{

    private static final String BROKER_USER_NAME = "BrokerUserName";
    private static final String PASSWORD = "password";
    private static final String HOST_IP = "10.0.0.0";
    private static final Integer PORT = 6969;
    public static final String UUAA = "UUAA";
    public static final String SERVICENAME = "servicename";
    public static final String VERSION = "1.0.0";
    public static final String      CHANNEL_NAME  = "channelName";
    public static final Environment ENVIRONMENT   = Environment.PRO;
    public static final String      EXCHANGE_NAME = "uuaa_channelName_v_1";
    public static final String QUEUE_NAME = "uuaa_channelName_v_1.uuaa_servicename";

    @Mock
    IBrokerAgentApiClient brokerAgentApiClient;

    @Mock
    ICipherCredentialService cipherCredentialService;

    @Captor
    ArgumentCaptor<ChannelsActionDTO> channelsActionDTOArgumentCaptor;

    @InjectMocks
    BrokerConfiguratorImpl brokerConfigurator;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(brokerAgentApiClient);
        verifyNoMoreInteractions(cipherCredentialService);
    }


    @Nested
    class configureBrokersForDeploymentPlan
    {

        @Test
        void ok()
        {
            // Given
            DeploymentPlan deploymentPlan = generateDeploymentPlan();
            ChannelsActionDTO expected = generateChannelsActionDTO();
            expected.setChannels(new ChannelDTO[]{generateChannelDTOConsumer(), generateChannelDTOPublisher()});

            // Then
            brokerConfigurator.configureBrokersForDeploymentPlan(deploymentPlan);

            // verify
            verify(cipherCredentialService).decryptPassword(PASSWORD);
            verify(brokerAgentApiClient).createChannels(eq(ENVIRONMENT.getEnvironment()), channelsActionDTOArgumentCaptor.capture());
            ChannelsActionDTO actual = channelsActionDTOArgumentCaptor.getValue();
            assertEquals(Arrays.asList(expected.getChannels()), Arrays.asList(actual.getChannels()));
        }

        @Test
        void ok_emptyBrokersList()
        {
            // Given
            DeploymentPlan deploymentPlan = generateDeploymentPlan();
            deploymentPlan.getDeploymentSubsystems()
                    .forEach(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices()
                            .forEach(deploymentService -> deploymentService.setBrokers(List.of())));

            // Then
            brokerConfigurator.configureBrokersForDeploymentPlan(deploymentPlan);

            // verify
            verify(cipherCredentialService, times(0)).decryptPassword(PASSWORD);
            verify(brokerAgentApiClient, times(0)).createChannels(ArgumentMatchers.eq(ENVIRONMENT.getEnvironment()), any());
        }

        @Test
        void ok_nullBrokersList()
        {
            // Given
            DeploymentPlan deploymentPlan = generateDeploymentPlan();
            deploymentPlan.getDeploymentSubsystems()
                    .forEach(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices()
                            .forEach(deploymentService -> deploymentService.setBrokers(null)));

            // Then
            brokerConfigurator.configureBrokersForDeploymentPlan(deploymentPlan);

            // verify
            verify(cipherCredentialService, times(0)).decryptPassword(PASSWORD);
            verify(brokerAgentApiClient, times(0)).createChannels(ArgumentMatchers.eq(ENVIRONMENT.getEnvironment()), any());
        }
    }

    private ChannelsActionDTO generateChannelsActionDTO()
    {
        ChannelsActionDTO channelsActionDTO = new ChannelsActionDTO();

        channelsActionDTO.setConnection(generateConnectionDTO());
        return channelsActionDTO;
    }

    private ChannelDTO generateChannelDTOConsumer()
    {
        ChannelDTO channelDTO = new ChannelDTO();
        channelDTO.setExchange(generateExchangeDTO());
        channelDTO.setQueue(generateQueue());

        return channelDTO;
    }
    private ChannelDTO generateChannelDTOPublisher()
    {
        ChannelDTO channelDTO = new ChannelDTO();
        channelDTO.setExchange(generateExchangeDTO());

        return channelDTO;
    }

    private QueueDTO generateQueue()
    {
        QueueDTO queueDTO = new QueueDTO();
        queueDTO.setExchangeName(EXCHANGE_NAME);
        queueDTO.setName(QUEUE_NAME);

        return queueDTO;
    }

    private ExchangeDTO generateExchangeDTO()
    {
        ExchangeDTO exchangeDTO = new ExchangeDTO();
        exchangeDTO.setName(EXCHANGE_NAME);

        return exchangeDTO;
    }

    private ConnectionDTO generateConnectionDTO()
    {
        ConnectionDTO connectionDTO = new ConnectionDTO();
        connectionDTO.setAddresses(new AddressDTO[]{generateAddressDTO()});
        connectionDTO.setUser(BROKER_USER_NAME);
        connectionDTO.setPassword(PASSWORD);

        return connectionDTO;
    }

    private AddressDTO generateAddressDTO()
    {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setPort(PORT);
        addressDTO.setHost(HOST_IP);

        return addressDTO;
    }

    private DeploymentPlan generateDeploymentPlan()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setEnvironment(ENVIRONMENT.getEnvironment());
        deploymentPlan.setReleaseVersion(generateReleaseVersion());
        deploymentPlan.setDeploymentSubsystems(List.of(generateDeploymentSubsystem().setDeploymentPlan(deploymentPlan)));

        return deploymentPlan;
    }

    private ReleaseVersion generateReleaseVersion()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(generateRelease());

        return releaseVersion;
    }

    private Release generateRelease()
    {
        Release release = new Release();
        release.setProduct(generateProduct());
        return release;
    }

    private Product generateProduct()
    {
        Product product = new Product();
        product.setUuaa(UUAA);
        return product;
    }

    private DeploymentSubsystem generateDeploymentSubsystem()
    {
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentServices(List.of(generateDeploymentService().setDeploymentSubsystem(deploymentSubsystem)));

        return deploymentSubsystem;
    }

    private DeploymentService generateDeploymentService()
    {
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setBrokers(List.of(generateBroker()));
        deploymentService.setService(generateReleaseVersionService());
        return deploymentService;
    }

    private ReleaseVersionService generateReleaseVersionService()
    {
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceName(SERVICENAME);
        // apis
        AsyncBackToBackApiImplementation apiConsumed = generateBackToBackApiImplementation();
        apiConsumed.setImplementedAs(ImplementedAs.CONSUMED);
        AsyncBackToBackApiImplementation apiServed = generateBackToBackApiImplementation();
        apiServed.setImplementedAs(ImplementedAs.SERVED);
        releaseVersionService.setApiImplementations(List.of(apiServed, apiConsumed));

        return releaseVersionService;
    }

    private AsyncBackToBackApiImplementation generateBackToBackApiImplementation()
    {
        AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
        asyncBackToBackApiImplementation.setApiVersion(generateApiVersion());

        return asyncBackToBackApiImplementation;
    }

    private AsyncBackToBackApiVersion generateApiVersion()
    {
        AsyncBackToBackApiVersion asyncBackToBackApiVersion = new AsyncBackToBackApiVersion();
        asyncBackToBackApiVersion.setVersion(VERSION);
        asyncBackToBackApiVersion.setApi(generateApi());
        asyncBackToBackApiVersion.setAsyncBackToBackApiChannel(generateAsyncBackToBackApiChannel());

        return asyncBackToBackApiVersion;
    }

    private AsyncBackToBackApiChannel generateAsyncBackToBackApiChannel()
    {
        AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
        asyncBackToBackApiChannel.setChannelName(CHANNEL_NAME);

        return asyncBackToBackApiChannel;
    }

    private AsyncBackToBackApi generateApi()
    {
        AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
        asyncBackToBackApi.setUuaa(UUAA);

        return asyncBackToBackApi;
    }

    private Broker generateBroker()
    {
        Broker broker = new Broker();
        broker.setType(BrokerType.PUBLISHER_SUBSCRIBER);
        broker.setUsers(List.of(generateBrokerUserAdmin()));
        broker.setNodes(List.of(generateBrokerNode()));

        return broker;
    }

    private BrokerNode generateBrokerNode()
    {
        BrokerNode node = new BrokerNode();
        node.setHostName(HOST_IP);
        node.setServicePort(PORT);

        return node;
    }

    private BrokerUser generateBrokerUserAdmin()
    {
        BrokerUser brokerUser = new BrokerUser();
        brokerUser.setRole(BrokerRole.ADMIN);
        brokerUser.setName(BROKER_USER_NAME);
        brokerUser.setPassword(PASSWORD);


        return brokerUser;
    }
}