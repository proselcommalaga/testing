package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerTaskProcessor;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerNodeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBrokerDeploymentClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BrokerNodeOperatorImplTest
{
    private static final double cpus = 2.0;
    private static final int ramInMB = 512;
    private static final Integer FS_ID = 1111;
    private static final Integer PROD_ID = 2222;
    private static final Integer HW_PACK_ID = 3333;
    private static final Integer BROKER_NODE_ID = 420;
    private static final String BROKER_NAME = "brokername";
    private static final String BROKER_DESCRIPTION = "Broker Description";
    private static final String IV_USER = "XE70505";
    public static final String GUEST = "guest";
    public static final String MONITORING_URL = "monitoring://url";
    private static final Integer BROKER_ID = 420;

    @Mock
    private BrokerNodeRepository brokerNodeRepository;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private BrokerBuilderImpl brokerBuilder;

    @Mock
    private BrokerValidatorImpl brokerValidator;

    @Mock
    private IUsersClient usersService;

    @Mock
    private IBrokerDeploymentClient brokerDeploymentClient;

    @InjectMocks
    private BrokerNodeOperatorImpl brokerNodeOperator;

    @Mock
    private BrokerAPIToDoTaskService  brokerAPIToDoTaskService;

    @Mock
    private IBrokerTaskProcessor taskProcessor;

    @Mock
    private ManageValidationUtils manageValidationUtils;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(brokerBuilder);
        verifyNoMoreInteractions(brokerValidator);
        verifyNoMoreInteractions(usersService);
        verifyNoMoreInteractions(brokerNodeRepository);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoMoreInteractions(brokerDeploymentClient);
    }


    @Nested
    class stopBrokerNode
    {
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("Stop successfully without sibling nodes")
        void ok()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerDeploymentClient.stopBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.stopBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.STOP_BROKER, node.getBroker().getEnvironment(),
                    node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerRepository).saveAndFlush(node.getBroker());
            verify(brokerDeploymentClient).stopBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("Stop successfully with sibling node running")
        void okWithSiblingNodeStopped()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(BROKER_NODE_ID + 1);
            siblingNode.setStatus(BrokerStatus.RUNNING);
            node.getBroker().getNodes().add(siblingNode);

            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.stopBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.stopBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.STOP_BROKER, node.getBroker().getEnvironment(),
                    node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerDeploymentClient).stopBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("Stop successfully with sibling node stopped")
        void okWithSiblingNodeRunning()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(BROKER_NODE_ID + 1);
            siblingNode.setStatus(BrokerStatus.STOPPED);
            node.getBroker().getNodes().add(siblingNode);

            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerDeploymentClient.stopBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.stopBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.STOP_BROKER, node.getBroker().getEnvironment(),
                    node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerRepository).saveAndFlush(node.getBroker());
            verify(brokerDeploymentClient).stopBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("broker node deletion throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.stopBrokerNode(node.getId())).thenReturn(false);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerNodeOperator.stopBrokerNode(IV_USER, BROKER_NODE_ID)
            );
            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_STOP_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.STOP_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(brokerDeploymentClient).stopBrokerNode(node.getId());
            verify(brokerNodeRepository, times(2)).saveAndFlush(node);
        }
    }

    @Nested
    class startBrokerNode
    {
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("Start successfully without sibling nodes")
        void okWithoutSiblingNodes()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.startBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.startBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStarted(node);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.START_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerRepository).saveAndFlush(node.getBroker());
            verify(brokerDeploymentClient).startBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("Start successfully with sibling node running")
        void okWithSiblingNodeRunning()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(BROKER_NODE_ID + 1);
            siblingNode.setStatus(BrokerStatus.RUNNING);
            node.getBroker().getNodes().add(siblingNode);

            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.startBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.startBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStarted(node);


            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.START_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerDeploymentClient).startBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @ParameterizedTest
        @DisplayName("Start successfully with sibling node stopped")
        @EnumSource(value = BrokerStatus.class, names = {"STOPPED", "STOPPING"})
        void okWithSiblingNodeStopped(BrokerStatus siblingStatus)
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(BROKER_NODE_ID + 1);
            siblingNode.setStatus(siblingStatus);
            node.getBroker().getNodes().add(siblingNode);

            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.startBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.startBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStarted(node);


            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.START_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerRepository).saveAndFlush(node.getBroker());
            verify(brokerDeploymentClient).startBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("broker node start throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.startBrokerNode(node.getId())).thenReturn(false);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerNodeOperator.startBrokerNode(IV_USER, BROKER_NODE_ID)
            );
            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_START_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStarted(node);


            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.START_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(brokerDeploymentClient).startBrokerNode(node.getId());
            verify(brokerNodeRepository, times(2)).saveAndFlush(node);
        }
    }

    @Nested
    class restartBrokerNode
    {
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("Restart successfully without sibling nodes")
        void okWithoutSiblingNodes()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.restartBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.restartBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);


            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.RESTART_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerRepository).saveAndFlush(node.getBroker());
            verify(brokerDeploymentClient).restartBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("Restart successfully with sibling node running")
        void okWithSiblingNodeStopped()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(BROKER_NODE_ID + 1);
            siblingNode.setStatus(BrokerStatus.RUNNING);
            node.getBroker().getNodes().add(siblingNode);

            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.restartBrokerNode(node.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker())).thenReturn(expectedBrokerDTO);

            // test
            brokerNodeOperator.restartBrokerNode(IV_USER, BROKER_NODE_ID);

            // asserts
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.RESTART_BROKER, node.getBroker().getEnvironment(),
                    node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);
            verify(brokerNodeRepository).saveAndFlush(node);
            verify(brokerDeploymentClient).restartBrokerNode(node.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(node.getBroker());
        }

        @Test
        @DisplayName("broker node start throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {
            // conditions
            BrokerNode node = brokerNodeSupplier.get();
            when(brokerValidator.validateAndGetBrokerNode(BROKER_NODE_ID)).thenReturn(node);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(node)).thenReturn(node);
            when(brokerDeploymentClient.restartBrokerNode(node.getId())).thenReturn(false);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerNodeOperator.restartBrokerNode(IV_USER, BROKER_NODE_ID)
            );
            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_RESTART_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBrokerNode(BROKER_NODE_ID);
            verify(brokerValidator).validateBrokerNodeCanBeStopped(node);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, node.getBroker());
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.RESTART_BROKER, node.getBroker().getEnvironment(), node.getBroker().getProduct().getId(), BrokerNodeOperatorImpl.PERMISSION_DENIED);

            verify(brokerDeploymentClient).restartBrokerNode(node.getId());
            verify(brokerNodeRepository, times(2)).saveAndFlush(node);
        }
    }

    private static class BrokerNodeSupplier implements Supplier<BrokerNode>
    {

        @Override
        public BrokerNode get()
        {
            Broker broker = new Broker();
            Product product = new Product();
            Filesystem filesystem = new FilesystemNova();
            BrokerPack hardwarePack = new BrokerPack();
            product.setId(PROD_ID);
            filesystem.setId(FS_ID);
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setNumCPU(cpus);
            hardwarePack.setRamMB(ramInMB);
            broker.setProduct(product);
            broker.setHardwarePack(hardwarePack);
            broker.setFilesystem(filesystem);
            broker.setCpu(cpus);
            broker.setMemory(ramInMB);
            broker.setId(BROKER_NODE_ID);
            broker.setName(BROKER_NAME);
            broker.setDescription(BROKER_DESCRIPTION);
            broker.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
            broker.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)]);
            broker.setStatus(BrokerStatus.CREATING);
            broker.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);

            BrokerNode node = new BrokerNode();
            node.setBroker(broker);
            node.setStatus(BrokerStatus.CREATING);
            node.setId(BROKER_NODE_ID);

            broker.setNodes(Lists.newArrayList(node));

            return node;
        }
    }

    @Nested
    class onTaskReply
    {

        BrokerNodeOperatorImplTest.BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeOperatorImplTest.BrokerNodeSupplier();
        Integer taskId = RandomUtils.nextInt(1, 10);

        @Test
        void ok()
        {
            BrokerNode brokerNode = brokerNodeSupplier.get();
            when(brokerNodeRepository.findById(brokerNode.getId())).thenReturn(Optional.of(brokerNode));

            brokerNodeOperator.onTaskReply(taskId, brokerNode.getId());

            verify(brokerNodeRepository).findById(brokerNode.getId());
        }

        @Test
        void throwExceptionBrokerNodeTaskReplyOkFalse()
        {
            BrokerNode brokerNode = brokerNodeSupplier.get();
            when(brokerNodeRepository.findById(brokerNode.getId())).thenThrow(new NovaException(BrokerError.getBrokerNodeNotFoundError(brokerNode.getId()), "BrokerNode [" + brokerNode.getId() + "] not found"));

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerNodeOperator.onTaskReply(taskId, brokerNode.getId())
            );

            assertEquals("The broker node id: [" + brokerNode.getId() + "] was not found in NOVA database", exception.getNovaError().getErrorMessage());
            verify(brokerNodeRepository).findById(brokerNode.getId());
        }

    }

    private static class BrokerDTOSupplier implements Supplier<BrokerDTO>
    {

        @Override
        public BrokerDTO get()
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            brokerDTO.setId(BROKER_ID);
            brokerDTO.setName(BROKER_NAME);
            brokerDTO.setDescription(BROKER_DESCRIPTION);
            brokerDTO.setMonitoringUrl(MONITORING_URL);
            brokerDTO.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name());
            brokerDTO.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)].getType());
            brokerDTO.setStatus(BrokerStatus.CREATING.getStatus());
            brokerDTO.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)].getName());
            brokerDTO.setFilesystemId(FS_ID);
            brokerDTO.setProductId(PROD_ID);
            brokerDTO.setHardwarePackId(HW_PACK_ID);
            brokerDTO.setCpu((float) cpus);
            brokerDTO.setMemory(ramInMB);

            return brokerDTO;
        }
    }

    private static class BrokerSupplier implements Supplier<Broker>
    {

        @Override
        public Broker get()
        {
            Broker broker = new Broker();
            Product product = new Product();
            Filesystem filesystem = new FilesystemNova();
            BrokerPack hardwarePack = new BrokerPack();
            product.setId(PROD_ID);
            filesystem.setId(FS_ID);
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setNumCPU(cpus);
            hardwarePack.setRamMB(ramInMB);
            broker.setProduct(product);
            broker.setHardwarePack(hardwarePack);
            broker.setFilesystem(filesystem);
            broker.setCpu(cpus);
            broker.setMemory(ramInMB);
            broker.setId(BROKER_ID);
            broker.setName(BROKER_NAME);
            broker.setDescription(BROKER_DESCRIPTION);
            broker.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
            broker.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)]);
            broker.setStatus(BrokerStatus.CREATING);
            broker.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);

            return broker;
        }
    }
}