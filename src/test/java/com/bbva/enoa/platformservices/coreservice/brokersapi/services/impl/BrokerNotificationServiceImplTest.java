package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerNotificationDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.entities.BrokerTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerNodeOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerOperation;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.impl.ProductBudgetsFeignImpl;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.FilesystemManagerClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.utils.clientsutils.consumers.impl.NovaActivitiesClientImpl;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BrokerNotificationServiceImplTest
{

    @Mock
    private BrokerValidatorImpl brokerValidator;

    @Mock
    private ProductBudgetsFeignImpl budgetsService;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private NovaActivitiesClientImpl novaActivitiesClient;

    @Mock
    FilesystemManagerClientImpl filesystemManagerClient;

    @Mock
    IAlertServiceApiClient alertServiceApiClient;

    @InjectMocks
    private BrokerNotificationServiceImpl brokerNotificationService;

    @Mock
    private BrokerTaskRepository brokerTaskRepository;


    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }


    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(brokerValidator);
        verifyNoMoreInteractions(budgetsService);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoMoreInteractions(novaActivitiesClient);
    }

    @ParameterizedTest(name = "[{index}] => BrokerOperations: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("notificationProviderOKExpectedStateWithResultOKTrue")
    @DisplayName("Should notify the correct broker operation due to operation ResultOK is true -> ok")
    void notifyBrokerOperationResultTestResultOkTrue(BrokerOperation brokerOperation, BrokerStatus brokerStatus, ActivityAction action)
    {
        Integer brokerId = 69;
        String requesterIvUser = "XE70505";
        BrokerNotificationDTO brokerNotificationDTO = new BrokerNotificationDTO();
        brokerNotificationDTO.setOperation(brokerOperation.name());
        brokerNotificationDTO.setRequesterIvUser(requesterIvUser);
        brokerNotificationDTO.setMessage("message test");
        brokerNotificationDTO.setResultOK(true);

        Broker broker = getDefaultBroker(brokerId);
        broker.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerOperation(brokerNotificationDTO.getOperation())).thenReturn(brokerOperation);
        when(brokerValidator.validateAndGetBroker(brokerId)).thenReturn(broker);
        doNothing().when(filesystemManagerClient).callDeleteFile(anyInt(), any());
        doNothing().when(alertServiceApiClient).closeBrokerRelatedAlerts(anyString());

        // test
        brokerNotificationService.notifyBrokerOperationResult(brokerId, brokerNotificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerOperation(brokerNotificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBroker(brokerId);
        if (brokerOperation.equals(BrokerOperation.DELETE))
        {
            verify(budgetsService).deleteBroker(broker.getId());
            verify(brokerRepository).delete(broker);
            FSFileLocationModel locationModel = new FSFileLocationModel();
            locationModel.setPath("/TEST/brokerfs/resources/brokers");
            locationModel.setFilename("mybroker");
            verify(filesystemManagerClient).callDeleteFile(111, locationModel);
        }

        ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);

        verify(novaActivitiesClient).registerActivity(genericActivityArgumentCaptor.capture(), eq(requesterIvUser));
        GenericActivity genericActivityCapture = genericActivityArgumentCaptor.getValue();
        Assertions.assertEquals(action, genericActivityCapture.getAction());
        Assertions.assertFalse(genericActivityCapture.getSerializedStringParams().contains("errorMessage"));
    }

    @ParameterizedTest(name = "[{index}] => BrokerOperations: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("notificationProviderOKExpectedStateWithResultOKFalse")
    @DisplayName("Should notify that the broker operation could not been executed due to resultOK is false -> ok")
    void notifyBrokerOperationResultTestResultOkFalse(BrokerOperation brokerOperation, BrokerStatus brokerStatus, ActivityAction action)
    {
        Integer brokerId = 69;
        String requesterIvUser = "XE70505";
        BrokerNotificationDTO brokerNotificationDTO = new BrokerNotificationDTO();
        brokerNotificationDTO.setOperation(brokerOperation.name());
        brokerNotificationDTO.setRequesterIvUser(requesterIvUser);
        brokerNotificationDTO.setMessage("message test");
        brokerNotificationDTO.setResultOK(false);

        Broker broker = getDefaultBroker(brokerId);
        broker.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerOperation(brokerNotificationDTO.getOperation())).thenReturn(brokerOperation);
        when(brokerValidator.validateAndGetBroker(brokerId)).thenReturn(broker);

        // test
        brokerNotificationService.notifyBrokerOperationResult(brokerId, brokerNotificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerOperation(brokerNotificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBroker(brokerId);

        ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);

        verify(novaActivitiesClient).registerActivity(genericActivityArgumentCaptor.capture(), eq(requesterIvUser));
        GenericActivity genericActivityCapture = genericActivityArgumentCaptor.getValue();
        Assertions.assertEquals(action, genericActivityCapture.getAction());
        Assertions.assertTrue(genericActivityCapture.getSerializedStringParams().contains("errorMessage"));
    }

    @ParameterizedTest(name = "[{index}] => BrokerOperations: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("notificationProviderKONotExpectedStateResultOKTrue")
    @DisplayName("Should not send a notification due to not expected Status, just ignore, resultOk is true -> ok")
    void notifyBrokerOperationResultTestNotExpectedStatusWithResultOkTrue(BrokerOperation brokerOperation, BrokerStatus brokerStatus)
    {
        Integer brokerId = 69;
        BrokerNotificationDTO brokerNotificationDTO = new BrokerNotificationDTO();
        brokerNotificationDTO.setOperation(brokerOperation.name());
        brokerNotificationDTO.setResultOK(true);

        Broker broker = getDefaultBroker(brokerId);
        broker.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerOperation(brokerNotificationDTO.getOperation())).thenReturn(brokerOperation);
        when(brokerValidator.validateAndGetBroker(brokerId)).thenReturn(broker);

        // test
        brokerNotificationService.notifyBrokerOperationResult(brokerId, brokerNotificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerOperation(brokerNotificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBroker(brokerId);
    }

    @ParameterizedTest(name = "[{index}] => BrokerOperations: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("notificationProviderKONotExpectedStateResultOKFalse")
    @DisplayName("Should not send a notification due to not expected Status, just ignore, resultOk is false -> ok")
    void notifyBrokerOperationResultTestNotExpectedStatusWithResultOkFalse(BrokerOperation brokerOperation, BrokerStatus brokerStatus)
    {
        Integer brokerId = 69;
        BrokerNotificationDTO brokerNotificationDTO = new BrokerNotificationDTO();
        brokerNotificationDTO.setOperation(brokerOperation.name());
        brokerNotificationDTO.setResultOK(false);

        Broker broker = getDefaultBroker(brokerId);
        broker.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerOperation(brokerNotificationDTO.getOperation())).thenReturn(brokerOperation);
        when(brokerValidator.validateAndGetBroker(brokerId)).thenReturn(broker);

        // test
        brokerNotificationService.notifyBrokerOperationResult(brokerId, brokerNotificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerOperation(brokerNotificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBroker(brokerId);
    }

    @ParameterizedTest(name = "[{index}] => BrokerNodeOperation: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("brokerNodeNotificationProviderOKExpectedStateWithResultOKTrue")
    @DisplayName("Should notify the correct broker node operation due to operation ResultOK is true -> ok")
    void notifyBrokerNodeOperationResultTestResultOkTrue(BrokerNodeOperation brokerNodeOperation, BrokerStatus brokerStatus, ActivityAction action)
    {
        Integer nodeId = 69;
        String requesterIvUser = "XE70505";
        BrokerNotificationDTO notificationDTO = new BrokerNotificationDTO();
        notificationDTO.setOperation(brokerNodeOperation.name());
        notificationDTO.setRequesterIvUser(requesterIvUser);
        notificationDTO.setMessage("message test");
        notificationDTO.setResultOK(true);

        BrokerNode node = getDefaultBrokerNode(nodeId);
        node.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerNodeOperation(notificationDTO.getOperation())).thenReturn(brokerNodeOperation);
        when(brokerValidator.validateAndGetBrokerNode(nodeId)).thenReturn(node);

        // test
        brokerNotificationService.notifyBrokerNodeOperationResult(nodeId, notificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerNodeOperation(notificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBrokerNode(nodeId);

        ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);

        verify(novaActivitiesClient).registerActivity(genericActivityArgumentCaptor.capture(), eq(requesterIvUser));
        GenericActivity genericActivityCapture = genericActivityArgumentCaptor.getValue();
        Assertions.assertEquals(action, genericActivityCapture.getAction());
        Assertions.assertFalse(genericActivityCapture.getSerializedStringParams().contains("errorMessage"));
    }

    @ParameterizedTest(name = "[{index}] => BrokerNodeOperation: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("brokerNodeNotificationProviderOKExpectedStateWithResultOKFalse")
    @DisplayName("Should notify that the broker node operation could not been executed due to resultOK is false -> ok")
    void notifyBrokerNodeOperationResultTestResultOkFalse(BrokerNodeOperation brokerNodeOperation, BrokerStatus brokerStatus, ActivityAction action)
    {
        Integer nodeId = 69;
        String requesterIvUser = "XE70505";
        BrokerNotificationDTO notificationDTO = new BrokerNotificationDTO();
        notificationDTO.setOperation(brokerNodeOperation.name());
        notificationDTO.setRequesterIvUser(requesterIvUser);
        notificationDTO.setMessage("message test");
        notificationDTO.setResultOK(false);

        BrokerNode node = getDefaultBrokerNode(nodeId);
        node.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerNodeOperation(notificationDTO.getOperation())).thenReturn(brokerNodeOperation);
        when(brokerValidator.validateAndGetBrokerNode(nodeId)).thenReturn(node);

        // test
        brokerNotificationService.notifyBrokerNodeOperationResult(nodeId, notificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerNodeOperation(notificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBrokerNode(nodeId);

        ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);

        verify(novaActivitiesClient).registerActivity(genericActivityArgumentCaptor.capture(), eq(requesterIvUser));
        GenericActivity genericActivityCapture = genericActivityArgumentCaptor.getValue();
        Assertions.assertEquals(action, genericActivityCapture.getAction());
        Assertions.assertTrue(genericActivityCapture.getSerializedStringParams().contains("errorMessage"));
    }

    @ParameterizedTest(name = "[{index}] => BrokerNodeOperation: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("brokerNodeNotificationProviderKONotExpectedStateResultOKTrue")
    @DisplayName("Should not send a notification due to not expected Status, just ignore, resultOk is true -> ok")
    void notifyBrokerNodeOperationResultTestNotExpectedStatusWithResultOkTrue(BrokerNodeOperation brokerNodeOperation, BrokerStatus brokerStatus)
    {
        Integer nodeId = 69;
        String requesterIvUser = "XE70505";
        BrokerNotificationDTO notificationDTO = new BrokerNotificationDTO();
        notificationDTO.setOperation(brokerNodeOperation.name());
        notificationDTO.setRequesterIvUser(requesterIvUser);
        notificationDTO.setResultOK(true);

        BrokerNode node = getDefaultBrokerNode(nodeId);
        node.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerNodeOperation(notificationDTO.getOperation())).thenReturn(brokerNodeOperation);
        when(brokerValidator.validateAndGetBrokerNode(nodeId)).thenReturn(node);

        // test
        brokerNotificationService.notifyBrokerNodeOperationResult(nodeId, notificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerNodeOperation(notificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBrokerNode(nodeId);
    }

    @ParameterizedTest(name = "[{index}] => BrokerNodeOperation: {0}, BrokerStatus: {1}, activityAction: {2}")
    @MethodSource("brokerNodeNotificationProviderKONotExpectedStateResultOKFalse")
    @DisplayName("Should not send a notification due to not expected Status, just ignore, resultOk is false -> ok")
    void notifyBrokerNodeOperationResultTestNotExpectedStatusWithResultOkFalse(BrokerNodeOperation brokerNodeOperation, BrokerStatus brokerStatus)
    {
        Integer nodeId = 69;
        String requesterIvUser = "XE70505";
        BrokerNotificationDTO notificationDTO = new BrokerNotificationDTO();
        notificationDTO.setOperation(brokerNodeOperation.name());
        notificationDTO.setRequesterIvUser(requesterIvUser);
        notificationDTO.setResultOK(false);

        BrokerNode node = getDefaultBrokerNode(nodeId);
        node.setStatus(brokerStatus);

        // conditions
        when(brokerValidator.validateAndGetBrokerNodeOperation(notificationDTO.getOperation())).thenReturn(brokerNodeOperation);
        when(brokerValidator.validateAndGetBrokerNode(nodeId)).thenReturn(node);

        // test
        brokerNotificationService.notifyBrokerNodeOperationResult(nodeId, notificationDTO);

        // asserts
        verify(brokerValidator).validateAndGetBrokerNodeOperation(notificationDTO.getOperation());
        verify(brokerValidator).validateAndGetBrokerNode(nodeId);
    }

    @Test
    void deleteBrokerTodoTaskWithResultOk()
    {
        Integer brokerId = RandomUtils.nextInt(1, 10);
        Broker broker = new Broker();
        broker.setId(brokerId);

        List<BrokerTask> taskList = new ArrayList<>();

        BrokerTask brokerTask1 = new BrokerTask();
        brokerTask1.setId(RandomUtils.nextInt(1, 100));
        brokerTask1.setTaskType(ToDoTaskType.BROKER_NODE_STOP_ERROR);
        brokerTask1.setBroker(broker);
        taskList.add(brokerTask1);

        BrokerTask brokerTask2 = new BrokerTask();
        brokerTask2.setId(RandomUtils.nextInt(1, 100));
        brokerTask2.setTaskType(ToDoTaskType.BROKER_RESTART_ERROR);
        brokerTask2.setBroker(broker);
        taskList.add(brokerTask2);


        // Conditions
        when(brokerTaskRepository.findByBrokerId(brokerId)).thenReturn(taskList);

        // test
        brokerNotificationService.deleteBrokerTodoTask(brokerId);

        // asserts
        verify(brokerTaskRepository).findByBrokerId(brokerId);
        verify(brokerTaskRepository).deleteAll(taskList);
    }

    private static Stream<Arguments> notificationProviderOKExpectedStateWithResultOKTrue()
    {
        return Stream.of(
                Arguments.of(BrokerOperation.CREATE, BrokerStatus.RUNNING, ActivityAction.CREATED),
                Arguments.of(BrokerOperation.START, BrokerStatus.RUNNING, ActivityAction.STARTED),
                Arguments.of(BrokerOperation.RESTART, BrokerStatus.RUNNING, ActivityAction.RESTARTED),
                Arguments.of(BrokerOperation.DELETE, BrokerStatus.DELETING, ActivityAction.DELETED),
                Arguments.of(BrokerOperation.STOP, BrokerStatus.STOPPED, ActivityAction.STOPPED)
        );
    }

    private static Stream<Arguments> brokerNodeNotificationProviderOKExpectedStateWithResultOKTrue()
    {
        return Stream.of(
                Arguments.of(BrokerNodeOperation.START, BrokerStatus.RUNNING, ActivityAction.STARTED),
                Arguments.of(BrokerNodeOperation.RESTART, BrokerStatus.RUNNING, ActivityAction.RESTARTED),
                Arguments.of(BrokerNodeOperation.STOP, BrokerStatus.STOPPED, ActivityAction.STOPPED)
        );
    }

    private static Stream<Arguments> notificationProviderKONotExpectedStateResultOKTrue()
    {
        List<Arguments> argumentsList = new ArrayList<>();
        getBrokerStatusButGiven(BrokerStatus.RUNNING).forEach(brokerStatus -> {
            argumentsList.add(Arguments.of(BrokerOperation.CREATE, brokerStatus));
            argumentsList.add(Arguments.of(BrokerOperation.START, brokerStatus));
            argumentsList.add(Arguments.of(BrokerOperation.RESTART, brokerStatus));
        });
        getBrokerStatusButGiven(BrokerStatus.DELETING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.DELETE, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.STOPPED).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.STOP, brokerStatus)));

        return argumentsList.stream();
    }

    private static Stream<Arguments> brokerNodeNotificationProviderKONotExpectedStateResultOKTrue()
    {
        List<Arguments> argumentsList = new ArrayList<>();
        getBrokerStatusButGiven(BrokerStatus.RUNNING).forEach(brokerStatus -> {
            argumentsList.add(Arguments.of(BrokerNodeOperation.START, brokerStatus));
            argumentsList.add(Arguments.of(BrokerNodeOperation.RESTART, brokerStatus));
        });
        getBrokerStatusButGiven(BrokerStatus.STOPPED).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerNodeOperation.STOP, brokerStatus)));

        return argumentsList.stream();
    }

    private static Stream<Arguments> notificationProviderOKExpectedStateWithResultOKFalse()
    {
        return Stream.of(
                Arguments.of(BrokerOperation.CREATE, BrokerStatus.CREATE_ERROR, ActivityAction.CREATED),
                Arguments.of(BrokerOperation.START, BrokerStatus.STOPPED, ActivityAction.STARTED),
                Arguments.of(BrokerOperation.DELETE, BrokerStatus.DELETE_ERROR, ActivityAction.DELETED),
                Arguments.of(BrokerOperation.RESTART, BrokerStatus.RUNNING, ActivityAction.RESTARTED),
                Arguments.of(BrokerOperation.STOP, BrokerStatus.RUNNING, ActivityAction.STOPPED)
        );
    }

    private static Stream<Arguments> brokerNodeNotificationProviderOKExpectedStateWithResultOKFalse()
    {
        return Stream.of(
                Arguments.of(BrokerNodeOperation.START, BrokerStatus.STOPPED, ActivityAction.STARTED),
                Arguments.of(BrokerNodeOperation.RESTART, BrokerStatus.RUNNING, ActivityAction.RESTARTED),
                Arguments.of(BrokerNodeOperation.STOP, BrokerStatus.RUNNING, ActivityAction.STOPPED)
        );
    }

    private static Stream<Arguments> notificationProviderKONotExpectedStateResultOKFalse()
    {
        List<Arguments> argumentsList = new ArrayList<>();
        getBrokerStatusButGiven(BrokerStatus.CREATE_ERROR).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.CREATE, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.STOPPED, BrokerStatus.RUNNING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.START, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.DELETE_ERROR).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.DELETE, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.RUNNING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.RESTART, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.RUNNING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerOperation.STOP, brokerStatus)));

        return argumentsList.stream();
    }

    private static Stream<Arguments> brokerNodeNotificationProviderKONotExpectedStateResultOKFalse()
    {
        List<Arguments> argumentsList = new ArrayList<>();
        getBrokerStatusButGiven(BrokerStatus.STOPPED, BrokerStatus.RUNNING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerNodeOperation.START, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.RUNNING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerNodeOperation.RESTART, brokerStatus)));
        getBrokerStatusButGiven(BrokerStatus.RUNNING).forEach(brokerStatus -> argumentsList.add(Arguments.of(BrokerNodeOperation.STOP, brokerStatus)));

        return argumentsList.stream();
    }

    private static List<BrokerStatus> getBrokerStatusButGiven(BrokerStatus... brokerStatusToAvoid)
    {
        return Arrays.stream(BrokerStatus.values())
                .filter(bs -> !Arrays.asList(brokerStatusToAvoid).contains(bs))
                .collect(Collectors.toList());
    }

    private Broker getDefaultBroker(Integer brokerId)
    {
        Product product = new Product();
        product.setId(33);
        product.setUuaa("TEST");

        Filesystem fs = new FilesystemNova();
        fs.setId(111);
        fs.setName("brokerFS");

        Broker broker = new Broker();
        broker.setId(brokerId);
        broker.setName("mybroker");
        broker.setType(BrokerType.PUBLISHER_SUBSCRIBER);
        broker.setPlatform(Platform.NOVA);
        broker.setNumberOfNodes(2);
        broker.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
        broker.setProduct(product);
        broker.setFilesystem(fs);
        return broker;
    }

    private BrokerNode getDefaultBrokerNode(Integer nodeId)
    {
        BrokerNode node = new BrokerNode();
        node.setId(nodeId);
        node.setBroker(this.getDefaultBroker(nodeId +1));

        return node;
    }
}