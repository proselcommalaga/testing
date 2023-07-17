package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiChannel;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.AsyncBackToBackChannelType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerProperty;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.config.enumerates.PropertyType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class BrokerPropertiesConfigurationImplTest
{

    @Mock
    private ICipherCredentialService cipherCredentialService;

    @InjectMocks
    private BrokerPropertiesConfigurationImpl brokerPropertiesConfiguration;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class getBrokerSCSPropertiesDependentOnBroker
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();

        @Test
        void okWithOneBrokerNode()
        {
            // when
            Broker broker = brokerSupplier.get();

            BrokerUser brokerUserService = getBrokerUser(BrokerRole.SERVICE, "m4bu");
            brokerUserService.setBroker(broker);
            BrokerUser brokerUserAdmin = getBrokerUser(BrokerRole.ADMIN, "admin");
            brokerUserAdmin.setBroker(broker);
            broker.setUsers(List.of(brokerUserService, brokerUserAdmin));

            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerNode.setBroker(broker);
            broker.setNodes(List.of(brokerNode));

            when(cipherCredentialService.decryptPassword(anyString())).thenReturn("decryptedPass");

            // expected
            List<BrokerProperty> expectedBrokerPropertyList = List.of(
                    new BrokerProperty(
                            "rabbit",
                            "spring.cloud.stream.binders.m4bu-BrokerName.type",
                            "spring.cloud.stream.binders.m4bu-BrokerName.type",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty(
                            "lxprd500.igrupobbva:6969",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.addresses",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.addresses",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty("m4bu",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.username",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.username",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty("decryptedPass",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.password",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.password",
                            null, PropertyType.STRING, ManagementType.BROKER, true
                    )
            );
            // test
            List<BrokerProperty> actualBrokerPropertyList = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnBroker(broker);
            // asserts
            Assertions.assertEquals(expectedBrokerPropertyList, actualBrokerPropertyList);
        }

        @Test
        void okWithTwoBrokerNode()
        {
            // when
            Broker broker = brokerSupplier.get();

            BrokerUser brokerUserService = getBrokerUser(BrokerRole.SERVICE, "m4bu");
            brokerUserService.setBroker(broker);
            BrokerUser brokerUserAdmin = getBrokerUser(BrokerRole.ADMIN, "admin");
            brokerUserAdmin.setBroker(broker);
            broker.setUsers(List.of(brokerUserService, brokerUserAdmin));

            BrokerNode node1 = brokerNodeSupplier.get();
            node1.setBroker(broker);
            node1.setContainerName("node-0");
            BrokerNode node2 = brokerNodeSupplier.get();
            node2.setBroker(broker);
            node2.setContainerName("node-1");
            broker.setNodes(List.of(node1, node2));

            when(cipherCredentialService.decryptPassword(anyString())).thenReturn("decryptedPass");

            // expected
            List<BrokerProperty> expectedBrokerPropertyList = List.of(
                    new BrokerProperty(
                            "rabbit",
                            "spring.cloud.stream.binders.m4bu-BrokerName.type",
                            "spring.cloud.stream.binders.m4bu-BrokerName.type",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty(
                            "lxprd500.igrupobbva:6969,lxprd500.igrupobbva:6969",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.addresses",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.addresses",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty("m4bu",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.username",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.username",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty("decryptedPass",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.password",
                            "spring.cloud.stream.binders.m4bu-BrokerName.environment.spring.rabbitmq.password",
                            null, PropertyType.STRING, ManagementType.BROKER, true
                    )
            );
            // test
            List<BrokerProperty> actualBrokerPropertyList = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnBroker(broker);
            // asserts
            Assertions.assertEquals(expectedBrokerPropertyList, actualBrokerPropertyList);
        }
    }

    @Nested
    class getBrokerSCSPropertiesDependentOnReleaseVersionService
    {
        @Test
        void getReleaseVersionBrokerProperties_two_channels_publisher_ok()
        {

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();

            // api 1
            List<ApiImplementation<?, ?, ?>> servers = new ArrayList<>();
            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
            AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiImplementation.setImplementedAs(ImplementedAs.SERVED);
            apiVersion.setVersion("1.0.0");
            asyncBackToBackApi.setUuaa("abcd");
            asyncBackToBackApiChannel.setChannelType(AsyncBackToBackChannelType.PUBLISH);
            asyncBackToBackApiChannel.setOperationId("myChannel1Operation");
            asyncBackToBackApiChannel.setChannelName("mychannel1");
            asyncBackToBackApiImplementation.setApiVersion(apiVersion);
            apiVersion.setApi(asyncBackToBackApi);
            apiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            // api 2
            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation1 = new AsyncBackToBackApiImplementation();
            AsyncBackToBackApiVersion apiVersion1 = new AsyncBackToBackApiVersion();
            AsyncBackToBackApi asyncBackToBackApi1 = new AsyncBackToBackApi();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel1 = new AsyncBackToBackApiChannel();
            asyncBackToBackApiImplementation1.setImplementedAs(ImplementedAs.SERVED);
            apiVersion1.setVersion("2.0.0");
            asyncBackToBackApi1.setUuaa("abcd");
            asyncBackToBackApiChannel1.setChannelType(AsyncBackToBackChannelType.PUBLISH);
            asyncBackToBackApiChannel1.setOperationId("myChannel2Operation");
            asyncBackToBackApiChannel1.setChannelName("mychannel2");
            asyncBackToBackApiImplementation1.setApiVersion(apiVersion1);
            apiVersion1.setApi(asyncBackToBackApi1);
            apiVersion1.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel1);


            servers.add(asyncBackToBackApiImplementation);
            servers.add(asyncBackToBackApiImplementation1);

            releaseVersionService.setServers(servers);
            Product product = new Product();
            product.setUuaa("uuaa");
            Release release = new Release();
            release.setProduct(product);
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setRelease(release);
            ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);
            releaseVersionService.setServiceName("serviceName");


            Map<String, String> asyncApiBrokerPropertiesMapExpected = new HashMap<>();
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel1Operation-out-0.destination", "abcd_mychannel1_v_1");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel2Operation-out-0.destination", "abcd_mychannel2_v_2");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.function.definition", "myChannel1Operation;myChannel2Operation");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.producer.bindQueue", "false");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.producer.declareExchange", "false");

            // call method function
            Map<String, String> asyncApiBrokerPropertiesMapResult = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnReleaseVersionService(releaseVersionService);

            // check
            Assertions.assertEquals(asyncApiBrokerPropertiesMapExpected, asyncApiBrokerPropertiesMapResult);
        }

        @Test
        void getReleaseVersionBrokerProperties_two_channels_publisher_and_subscriber_ok()
        {

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();

            // api 1
            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
            AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiImplementation.setImplementedAs(ImplementedAs.SERVED);
            apiVersion.setVersion("1.0.0");
            asyncBackToBackApi.setUuaa("abcd");
            asyncBackToBackApiChannel.setChannelType(AsyncBackToBackChannelType.PUBLISH);
            asyncBackToBackApiChannel.setOperationId("myChannel1Operation");
            asyncBackToBackApiChannel.setChannelName("mychannel1");
            asyncBackToBackApiImplementation.setApiVersion(apiVersion);
            apiVersion.setApi(asyncBackToBackApi);
            apiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            // api 2
            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation1 = new AsyncBackToBackApiImplementation();
            AsyncBackToBackApiVersion apiVersion1 = new AsyncBackToBackApiVersion();
            AsyncBackToBackApi asyncBackToBackApi1 = new AsyncBackToBackApi();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel1 = new AsyncBackToBackApiChannel();
            asyncBackToBackApiImplementation1.setImplementedAs(ImplementedAs.CONSUMED);
            apiVersion1.setVersion("2.0.0");
            asyncBackToBackApi1.setUuaa("abcd");
            asyncBackToBackApiChannel1.setChannelType(AsyncBackToBackChannelType.SUBSCRIBE);
            asyncBackToBackApiChannel1.setOperationId("myChannel2Operation");
            asyncBackToBackApiChannel1.setChannelName("mychannel2");
            asyncBackToBackApiImplementation1.setApiVersion(apiVersion1);
            apiVersion1.setApi(asyncBackToBackApi1);
            apiVersion1.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel1);

            releaseVersionService.setConsumers(List.of(asyncBackToBackApiImplementation1));
            releaseVersionService.setServers(List.of(asyncBackToBackApiImplementation));
            releaseVersionService.setServiceName("serviceName");
            Product product = new Product();
            product.setUuaa("uuaa");
            Release release = new Release();
            release.setProduct(product);
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setRelease(release);
            ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);
            releaseVersionService.setServiceName("serviceName");


            Map<String, String> asyncApiBrokerPropertiesMapExpected = new HashMap<>();
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel2Operation-in-0.group", "uuaa_serviceName");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel1Operation-out-0.destination", "abcd_mychannel1_v_1");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel2Operation-in-0.destination", "abcd_mychannel2_v_2");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.function.definition", "myChannel1Operation;myChannel2Operation");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.producer.bindQueue", "false");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.producer.declareExchange", "false");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.consumer.bindQueue", "false");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.consumer.declareExchange", "false");

            // call method function
            Map<String, String> asyncApiBrokerPropertiesMapResult = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnReleaseVersionService(releaseVersionService);

            // check
            Assertions.assertEquals(asyncApiBrokerPropertiesMapExpected, asyncApiBrokerPropertiesMapResult);
        }


        @Test
        void getReleaseVersionBrokerProperties_one_channels_publisher_ok()
        {

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();

            // api 1
            List<ApiImplementation<?, ?, ?>> servers = new ArrayList<>();
            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
            AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiImplementation.setImplementedAs(ImplementedAs.SERVED);
            apiVersion.setVersion("1.0.0");
            asyncBackToBackApi.setUuaa("abcd");
            asyncBackToBackApiChannel.setChannelType(AsyncBackToBackChannelType.PUBLISH);
            asyncBackToBackApiChannel.setOperationId("myChannel1Operation");
            asyncBackToBackApiChannel.setChannelName("mychannel1");
            asyncBackToBackApiImplementation.setApiVersion(apiVersion);
            apiVersion.setApi(asyncBackToBackApi);
            apiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            servers.add(asyncBackToBackApiImplementation);

            releaseVersionService.setServers(servers);
            releaseVersionService.setServiceName("serviceName");
            Product product = new Product();
            product.setUuaa("uuaa");
            Release release = new Release();
            release.setProduct(product);
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setRelease(release);
            ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);
            releaseVersionService.setServiceName("serviceName");

            Map<String, String> asyncApiBrokerPropertiesMapExpected = new HashMap<>();
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel1Operation-out-0.destination", "abcd_mychannel1_v_1");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.function.definition", "myChannel1Operation");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.producer.bindQueue", "false");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.producer.declareExchange", "false");

            // call method function
            Map<String, String> asyncApiBrokerPropertiesMapResult = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnReleaseVersionService(releaseVersionService);

            // check
            Assertions.assertEquals(asyncApiBrokerPropertiesMapResult, asyncApiBrokerPropertiesMapExpected);
        }


        @Test
        void getReleaseVersionBrokerProperties_one_channels_subscriber_ok()
        {
            ReleaseVersionService releaseVersionService = new ReleaseVersionService();

            // api 1
            List<ApiImplementation<?, ?, ?>> servers = new ArrayList<>();
            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
            AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiImplementation.setImplementedAs(ImplementedAs.CONSUMED);
            apiVersion.setVersion("1.0.0");
            asyncBackToBackApi.setUuaa("abcd");
            asyncBackToBackApiChannel.setChannelType(AsyncBackToBackChannelType.SUBSCRIBE);
            asyncBackToBackApiChannel.setOperationId("myChannel1Operation");
            asyncBackToBackApiChannel.setChannelName("mychannel1");
            asyncBackToBackApiImplementation.setApiVersion(apiVersion);
            apiVersion.setApi(asyncBackToBackApi);
            apiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            servers.add(asyncBackToBackApiImplementation);

            releaseVersionService.setServers(servers);
            releaseVersionService.setServiceName("serviceName");
            Product product = new Product();
            product.setUuaa("uuaa");
            Release release = new Release();
            release.setProduct(product);
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setRelease(release);
            ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);
            releaseVersionService.setServiceName("serviceName");


            Map<String, String> asyncApiBrokerPropertiesMapExpected = new HashMap<>();
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel1Operation-in-0.group", "uuaa_serviceName");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.bindings.myChannel1Operation-in-0.destination", "abcd_mychannel1_v_1");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.function.definition", "myChannel1Operation");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.consumer.bindQueue", "false");
            asyncApiBrokerPropertiesMapExpected.put("spring.cloud.stream.rabbit.default.consumer.declareExchange", "false");

            // call method function
            Map<String, String> asyncApiBrokerPropertiesMapResult = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnReleaseVersionService(releaseVersionService);

            // check
            Assertions.assertEquals(asyncApiBrokerPropertiesMapResult, asyncApiBrokerPropertiesMapExpected);
        }
    }

    @Nested
    class getBrokerSCSPropertiesDependentOnServiceAndBroker
    {

        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();

        @ParameterizedTest(name = "[{index}] implementedAs: {0} backToBackChannelType: {1}")
        @ArgumentsSource(BrokerPropertiesChannelTypeAndImplementedAsArgumentsProvider.class)
        @DisplayName("getBrokerSCSPropertiesDependentOnServiceAndBroker -> ok")
        void okOneAsyncBackToBackImplemented(ImplementedAs implementedAs, AsyncBackToBackChannelType asyncBackToBackChannelType)
        {
            // when
            Broker broker = brokerSupplier.get();

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiChannel.setChannelName("channelName");
            asyncBackToBackApiChannel.setChannelType(asyncBackToBackChannelType);
            asyncBackToBackApiChannel.setOperationId("ChannelOperationId");

            AsyncBackToBackApiVersion asyncBackToBackApiVersion = new AsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            asyncBackToBackApiImplementation.setImplementedAs(implementedAs);
            asyncBackToBackApiImplementation.setApiVersion(asyncBackToBackApiVersion);

            asyncBackToBackApiVersion.setApiImplementations(List.of(asyncBackToBackApiImplementation));
            releaseVersionService.setApiImplementations(List.of(asyncBackToBackApiImplementation));

            // expected
            String propertyName = null;
            if (AsyncBackToBackChannelType.SUBSCRIBE.equals(asyncBackToBackChannelType))
            {
                propertyName = "spring.cloud.stream.bindings.ChannelOperationId-in-0.binder";
            }
            else if (AsyncBackToBackChannelType.PUBLISH.equals(asyncBackToBackChannelType))
            {
                propertyName = "spring.cloud.stream.bindings.ChannelOperationId-out-0.binder";
            }
            List<BrokerProperty> expectedBrokerPropertyList = List.of(
                    new BrokerProperty(
                            "m4bu-BrokerName",
                            propertyName,
                            propertyName,
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    )
            );
            // test
            List<BrokerProperty> actualBrokerPropertyList = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnServiceAndBroker(releaseVersionService, broker);

            // asserts
            Assertions.assertEquals(expectedBrokerPropertyList, actualBrokerPropertyList);
        }

        @ParameterizedTest(name = "[{index}] implementedAs: {0} backToBackChannelType: {1}")
        @ArgumentsSource(BrokerPropertiesChannelTypeAndImplementedAsArgumentsProvider.class)
        @DisplayName("getBrokerSCSPropertiesDependentOnServiceAndBrokerHealthCheckApi -> ok")
        void okPropertiesDependentOnServiceAndBrokerHealthCheckApi(ImplementedAs implementedAs, AsyncBackToBackChannelType asyncBackToBackChannelType)
        {
            // when
            Broker broker = brokerSupplier.get();

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiChannel.setChannelName("channelName");
            asyncBackToBackApiChannel.setChannelType(asyncBackToBackChannelType);
            asyncBackToBackApiChannel.setOperationId("ChannelOperationId");

            AsyncBackToBackApiVersion asyncBackToBackApiVersion = new AsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            asyncBackToBackApiImplementation.setImplementedAs(implementedAs);
            asyncBackToBackApiImplementation.setApiVersion(asyncBackToBackApiVersion);

            asyncBackToBackApiVersion.setApiImplementations(List.of(asyncBackToBackApiImplementation));
            releaseVersionService.setApiImplementations(List.of(asyncBackToBackApiImplementation));
            releaseVersionService.setServiceType(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());

            // expected
            String propertyName = null;
            if (AsyncBackToBackChannelType.SUBSCRIBE.equals(asyncBackToBackChannelType))
            {
                propertyName = "spring.cloud.stream.bindings.ChannelOperationId-in-0.binder";
            }
            else if (AsyncBackToBackChannelType.PUBLISH.equals(asyncBackToBackChannelType))
            {
                propertyName = "spring.cloud.stream.bindings.ChannelOperationId-out-0.binder";
            }
            List<BrokerProperty> expectedBrokerPropertyList = List.of(
                    new BrokerProperty(
                            "m4bu-BrokerName",
                            propertyName,
                            propertyName,
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty(
                            "false",
                            "management.health.rabbit.enabled",
                            "management.health.rabbit.enabled",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    )
            );
            // test
            List<BrokerProperty> actualBrokerPropertyList = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnServiceAndBroker(releaseVersionService, broker);

            // asserts
            Assertions.assertEquals(expectedBrokerPropertyList, actualBrokerPropertyList);
        }

        @ParameterizedTest(name = "[{index}] implementedAs: {0} backToBackChannelType: {1}")
        @ArgumentsSource(BrokerPropertiesChannelTypeAndImplementedAsArgumentsProvider.class)
        @DisplayName("getBrokerSCSPropertiesDependentOnServiceAndBrokerHealthCheckApiRest -> ok")
        void okPropertiesDependentOnServiceAndBrokerHealthCheckApiRest(ImplementedAs implementedAs, AsyncBackToBackChannelType asyncBackToBackChannelType)
        {
            // when
            Broker broker = brokerSupplier.get();

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
            asyncBackToBackApiChannel.setChannelName("channelName");
            asyncBackToBackApiChannel.setChannelType(asyncBackToBackChannelType);
            asyncBackToBackApiChannel.setOperationId("ChannelOperationId");

            AsyncBackToBackApiVersion asyncBackToBackApiVersion = new AsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);

            AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
            asyncBackToBackApiImplementation.setImplementedAs(implementedAs);
            asyncBackToBackApiImplementation.setApiVersion(asyncBackToBackApiVersion);

            asyncBackToBackApiVersion.setApiImplementations(List.of(asyncBackToBackApiImplementation));
            releaseVersionService.setApiImplementations(List.of(asyncBackToBackApiImplementation));
            releaseVersionService.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType());

            // expected
            String propertyName = null;
            if (AsyncBackToBackChannelType.SUBSCRIBE.equals(asyncBackToBackChannelType))
            {
                propertyName = "spring.cloud.stream.bindings.ChannelOperationId-in-0.binder";
            }
            else if (AsyncBackToBackChannelType.PUBLISH.equals(asyncBackToBackChannelType))
            {
                propertyName = "spring.cloud.stream.bindings.ChannelOperationId-out-0.binder";
            }
            List<BrokerProperty> expectedBrokerPropertyList = List.of(
                    new BrokerProperty(
                            "m4bu-BrokerName",
                            propertyName,
                            propertyName,
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    ),
                    new BrokerProperty(
                            "false",
                            "management.health.rabbit.enabled",
                            "management.health.rabbit.enabled",
                            null, PropertyType.STRING, ManagementType.BROKER, false
                    )
            );
            // test
            List<BrokerProperty> actualBrokerPropertyList = brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnServiceAndBroker(releaseVersionService, broker);

            // asserts
            Assertions.assertEquals(expectedBrokerPropertyList, actualBrokerPropertyList);
        }
    }

    private static class BrokerSupplier implements Supplier<Broker>
    {

        @Override
        public Broker get()
        {
            Product product = new Product();
            product.setId(420);
            product.setUuaa("M4BU");

            Broker broker = new Broker();
            broker.setId(420);
            broker.setName("BrokerName");
            broker.setType(BrokerType.PUBLISHER_SUBSCRIBER);
            broker.setProduct(product);

            return broker;
        }
    }

    private static class BrokerNodeSupplier implements Supplier<BrokerNode>
    {

        @Override
        public BrokerNode get()
        {
            BrokerNode brokerNode = new BrokerNode();
            brokerNode.setHostName("lxprd500.igrupobbva");
            brokerNode.setServicePort(6969);
            return brokerNode;
        }

    }

    public static BrokerUser getBrokerUser(BrokerRole brokerRole, String name)
    {
        BrokerUser brokerUser = new BrokerUser();
        brokerUser.setRole(brokerRole);
        brokerUser.setName(name);
        brokerUser.setPassword("brokerUserPassword");
        return brokerUser;
    }


    private static class BrokerPropertiesChannelTypeAndImplementedAsArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Stream.of(
                    Arguments.of(ImplementedAs.SERVED, AsyncBackToBackChannelType.SUBSCRIBE),
                    Arguments.of(ImplementedAs.SERVED, AsyncBackToBackChannelType.PUBLISH),
                    Arguments.of(ImplementedAs.CONSUMED, AsyncBackToBackChannelType.SUBSCRIBE),
                    Arguments.of(ImplementedAs.CONSUMED, AsyncBackToBackChannelType.PUBLISH)
            );
        }
    }

}