package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerNodeDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNotificationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl.BrokerAgentApiClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl.BrokerQueueOperator.PERMISSION_DENIED;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerPermissions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrokerQueueOperatorTest
{
    public static final int BROKER_ID = 69;
    public static final String QUEUE_NAME = "queueName";
    public static final String IV_USER = "ivUser";
    public static final int COUNT_1 = 1;

    @Mock
    private IBrokerValidator brokerValidator;

    @Mock
    private IUsersClient usersClient;

    @Mock
    private IBrokerBuilder brokerBuilder;

    @Mock
    private BrokerAgentApiClient brokerAgentApiClient;

    @Mock
    private IBrokerNotificationService brokerNotificationService;

    @InjectMocks
    private BrokerQueueOperator brokerQueueOperator;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(usersClient);
        verifyNoMoreInteractions(brokerBuilder);
        verifyNoMoreInteractions(brokerValidator);
        verifyNoMoreInteractions(brokerAgentApiClient);
        verifyNoMoreInteractions(brokerNotificationService);
    }
    @Test
    void purgeQueue()
    {
        Broker broker = generateBroker();
        BrokerUser brokerUser = generateBrokerUser();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
        doNothing().when(brokerValidator).validateBrokerCanBeOperable(broker);
        doNothing().when(usersClient).checkHasPermission(IV_USER, PURGE_QUEUE, broker.getEnvironment(), broker.getProduct().getId() ,PERMISSION_DENIED);
        when(brokerValidator.validateAndGetBrokerAdminUser(any())).thenReturn(brokerUser);
        when(brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(any(), any())).thenReturn(connectionDTO);
        doNothing().when(brokerAgentApiClient).purgeQueue(broker.getEnvironment(), connectionDTO, QUEUE_NAME);
        doNothing().when(brokerNotificationService).registerBrokerActivity(any(), any(), anyBoolean(), anyString(), anyString());

        brokerQueueOperator.purgeQueue(IV_USER, BROKER_ID, QUEUE_NAME);

        verify(brokerValidator).validateAndGetBroker(BROKER_ID);
        verify(brokerValidator).validateBrokerCanBeOperable(broker);
        verify(usersClient).checkHasPermission(IV_USER, PURGE_QUEUE, broker.getEnvironment(), broker.getProduct().getId() ,PERMISSION_DENIED);
        verify(brokerValidator).validateAndGetBrokerAdminUser(broker);
        verify(brokerBuilder).buildConnectionDTOFromBrokerUserAndBrokerList(any(), any());
        verify(brokerAgentApiClient).purgeQueue(broker.getEnvironment(), connectionDTO, QUEUE_NAME);
        verify(brokerNotificationService).registerBrokerActivity(eq(ActivityAction.PURGE_QUEUE), eq(broker), anyBoolean(), anyString(), eq(IV_USER));

    }

    @Test
    void deleteQueue()
    {
        Broker broker = generateBroker();
        BrokerUser brokerUser = generateBrokerUser();
        ConnectionDTO connectionDTO = new ConnectionDTO();
        when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
        doNothing().when(brokerValidator).validateBrokerCanBeOperable(broker);
        doNothing().when(usersClient).checkHasPermission(IV_USER, DELETE_EXCHANGE_OR_QUEUE, broker.getEnvironment(), broker.getProduct().getId() ,PERMISSION_DENIED);
        when(brokerValidator.validateAndGetBrokerAdminUser(any())).thenReturn(brokerUser);
        when(brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(any(), any())).thenReturn(connectionDTO);
        doNothing().when(brokerAgentApiClient).deleteQueue(broker.getEnvironment(), connectionDTO, QUEUE_NAME);
        doNothing().when(brokerNotificationService).registerBrokerActivity(any(), any(), anyBoolean(), anyString(), anyString());

        brokerQueueOperator.deleteQueue(IV_USER, BROKER_ID, QUEUE_NAME);

        verify(brokerValidator).validateAndGetBroker(BROKER_ID);
        verify(brokerValidator).validateBrokerCanBeOperable(broker);
        verify(usersClient).checkHasPermission(IV_USER, DELETE_EXCHANGE_OR_QUEUE, broker.getEnvironment(), broker.getProduct().getId(), PERMISSION_DENIED);
        verify(brokerValidator).validateAndGetBrokerAdminUser(broker);
        verify(brokerBuilder).buildConnectionDTOFromBrokerUserAndBrokerList(any(), any());
        verify(brokerAgentApiClient).deleteQueue(broker.getEnvironment(), connectionDTO, QUEUE_NAME);
        verify(brokerNotificationService).registerBrokerActivity(eq(ActivityAction.DELETED_QUEUE), eq(broker), anyBoolean(), anyString(), eq(IV_USER));

    }

    @Test
    void getMessagesFromQueue()
    {
        Broker broker = generateBroker();
        BrokerUser brokerUser = generateBrokerUser();
        List<MessageDTO> expected = List.of(new MessageDTO());
        ConnectionDTO connectionDTO = new ConnectionDTO();
        when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
        doNothing().when(brokerValidator).validateBrokerCanBeOperable(broker);
        doNothing().when(usersClient).checkHasPermission(IV_USER, SHOW_MESSAGES_FROM_QUEUE, broker.getEnvironment(), broker.getProduct().getId() ,PERMISSION_DENIED);
        when(brokerValidator.validateAndGetBrokerAdminUser(any())).thenReturn(brokerUser);
        when(brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(any(), any())).thenReturn(connectionDTO);
        when(brokerAgentApiClient.getMessagesFromQueue(broker.getEnvironment(), connectionDTO, QUEUE_NAME, COUNT_1)).thenReturn(expected.toArray(MessageDTO[]::new));
        doNothing().when(brokerNotificationService).registerBrokerActivity(any(), any(), anyBoolean(), anyString(), anyString());


        MessageDTO[] actual = brokerQueueOperator.getMessagesFromQueue(IV_USER, BROKER_ID, QUEUE_NAME, COUNT_1);

        verify(brokerValidator).validateAndGetBroker(BROKER_ID);
        verify(brokerValidator).validateBrokerCanBeOperable(broker);
        verify(usersClient).checkHasPermission(IV_USER, SHOW_MESSAGES_FROM_QUEUE, broker.getEnvironment(), broker.getProduct().getId() ,PERMISSION_DENIED);
        verify(brokerValidator).validateAndGetBrokerAdminUser(broker);
        verify(brokerBuilder).buildConnectionDTOFromBrokerUserAndBrokerList(any(), any());
        verify(brokerAgentApiClient).getMessagesFromQueue(broker.getEnvironment(), connectionDTO, QUEUE_NAME, COUNT_1);
        verify(brokerNotificationService).registerBrokerActivity(eq(ActivityAction.VIEW_MESSAGES_FROM_QUEUE), eq(broker), anyBoolean(), anyString(), eq(IV_USER));
        assertEquals(expected, Arrays.asList(actual));
    }

    private BrokerUser generateBrokerUser()
    {
        return new BrokerUser();
    }
    private Broker generateBroker()
    {
        Product product = new Product();
        product.setId(1);
        Broker broker = new Broker();
        broker.setEnvironment(Environment.PRE.getEnvironment());
        broker.setProduct(product);
        broker.setNodes(List.of(new BrokerNode()));
        return  broker;
    }
}