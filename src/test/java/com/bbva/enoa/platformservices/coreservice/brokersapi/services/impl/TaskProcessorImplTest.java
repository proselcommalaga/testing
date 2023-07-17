package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.todotask.entities.BrokerTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerNodeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBrokerDeploymentClient;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TaskProcessorImplTest
{
    private static final double cpus = 2.0;
    private static final int ramInMB = 512;
    private static final Integer FS_ID = 1111;
    private static final Integer PROD_ID = 2222;
    private static final Integer HW_PACK_ID = 3333;
    private static final Integer BROKER_ID = 420;
    private static final Integer BROKER_NODE_ID = 420;
    private static final String BROKER_NAME = "brokername";
    private static final String BROKER_DESCRIPTION = "Broker Description";
    private static final String IV_USER = "XE70505";
    public static final String GUEST = "guest";
    public static final String MONITORING_URL = "monitoring://url";

    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;

    @Mock
    private IErrorTaskManager errorTaskManager;

    @Mock
    private BrokerTaskRepository brokerTaskRepository;

    @Mock
    private IBrokerDeploymentClient brokerDeploymentClient;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private BrokerNodeRepository brokerNodeRepository;

    @InjectMocks
    private BrokerTaskProcessorImpl taskProcessor;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class startBroker {

        BrokerSupplier brokerSupplier = new BrokerSupplier();

        @Test
        void onTaskReplyStartBrokerDone()
        {
            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_START_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.startBroker(broker.getId())).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            taskProcessor.onTaskreply(broker, null, brokerTask.getId());

            // verify
            verify(brokerDeploymentClient).startBroker(broker.getId());
        }

        @Test
        void onTaskReplyStartBrokerDoneWithErrors()
        {
            BrokerSupplier brokerSupplier = new BrokerSupplier();

            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_START_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.startBroker(broker.getId())).thenReturn(false);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> taskProcessor.onTaskreply(broker, null, brokerTask.getId())
            );

            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_START_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerDeploymentClient).startBroker(broker.getId());
        }
    }

    @Nested
    class restartBroker {

        BrokerSupplier brokerSupplier = new BrokerSupplier();

        @Test
        void onTaskReplyRestartBrokerDone()
        {
            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_RESTART_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.restartBroker(broker.getId())).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            taskProcessor.onTaskreply(broker, null, brokerTask.getId());

            // verify
            verify(brokerDeploymentClient).restartBroker(broker.getId());
        }

        @Test
        void onTaskReplyRestartBrokerDoneWithErrors()
        {
            BrokerSupplier brokerSupplier = new BrokerSupplier();

            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_RESTART_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.restartBroker(broker.getId())).thenReturn(false);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> taskProcessor.onTaskreply(broker, null, brokerTask.getId())
            );

            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_RESTART_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerDeploymentClient).restartBroker(broker.getId());
        }
    }

    @Nested
    class stopBroker {

        BrokerSupplier brokerSupplier = new BrokerSupplier();

        @Test
        void onTaskReplyStopBrokerDone()
        {
            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_STOP_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.stopBroker(broker.getId())).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            taskProcessor.onTaskreply(broker, null, brokerTask.getId());

            // verify
            verify(brokerDeploymentClient).stopBroker(broker.getId());
        }

        @Test
        void onTaskReplyStartBrokerDoneWithErrors()
        {
            BrokerSupplier brokerSupplier = new BrokerSupplier();

            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_STOP_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.stopBroker(broker.getId())).thenReturn(false);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> taskProcessor.onTaskreply(broker, null, brokerTask.getId())
            );

            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_STOP_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerDeploymentClient).stopBroker(broker.getId());
        }
    }

    @Nested
    class startBrokerNode {

        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();

        @Test
        void onTaskReplyStartBrokerNodeDone()
        {
            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_NODE_START_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.startBrokerNode(brokerNode.getId())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(brokerNode)).thenReturn(brokerNode);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            taskProcessor.onTaskreply(broker, brokerNode, brokerTask.getId());

            // verify
            verify(brokerDeploymentClient).startBrokerNode(brokerNode.getId());
        }

        @Test
        void onTaskReplyStartBrokerNodeDoneWithErrors()
        {

            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_NODE_START_REQUEST);
            brokerTask.setBroker(broker);
            brokerTask.setBrokerNode(brokerNode);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.startBrokerNode(brokerNode.getId())).thenReturn(false);
            when(brokerNodeRepository.saveAndFlush(brokerNode)).thenReturn(brokerNode);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> taskProcessor.onTaskreply(broker, brokerNode, brokerTask.getId())
            );

            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_START_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerDeploymentClient).startBrokerNode(brokerNode.getId());
        }
    }

    @Nested
    class restartBrokerNode {

        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();

        @Test
        void onTaskReplyRestartBrokerNodeDone()
        {
            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_NODE_RESTART_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.restartBrokerNode(brokerNode.getId())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(brokerNode)).thenReturn(brokerNode);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            taskProcessor.onTaskreply(broker, brokerNode, brokerTask.getId());

            // verify
            verify(brokerDeploymentClient).restartBrokerNode(brokerNode.getId());
        }

        @Test
        void onTaskReplyRestartBrokerNodeDoneWithErrors()
        {

            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_NODE_RESTART_REQUEST);
            brokerTask.setBroker(broker);
            brokerTask.setBrokerNode(brokerNode);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.restartBrokerNode(brokerNode.getId())).thenReturn(false);
            when(brokerNodeRepository.saveAndFlush(brokerNode)).thenReturn(brokerNode);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> taskProcessor.onTaskreply(broker, brokerNode, brokerTask.getId())
            );

            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_RESTART_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerDeploymentClient).restartBrokerNode(brokerNode.getId());
        }
    }

    @Nested
    class stopBrokerNode {

        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();

        @Test
        void onTaskReplyStopBrokerNodeDone()
        {
            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_NODE_STOP_REQUEST);
            brokerTask.setBroker(broker);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.stopBrokerNode(brokerNode.getId())).thenReturn(true);
            when(brokerNodeRepository.saveAndFlush(brokerNode)).thenReturn(brokerNode);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            taskProcessor.onTaskreply(broker, brokerNode, brokerTask.getId());

            // verify
            verify(brokerDeploymentClient).stopBrokerNode(brokerNode.getId());
        }

        @Test
        void onTaskReplyStopBrokerNodeDoneWithErrors()
        {

            BrokerTask brokerTask = new BrokerTask();
            Broker broker = brokerSupplier.get();
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerTask.setId(RandomUtils.nextInt(1, 100));
            brokerTask.setTaskType(ToDoTaskType.BROKER_NODE_STOP_REQUEST);
            brokerTask.setBroker(broker);
            brokerTask.setBrokerNode(brokerNode);

            // when
            when(brokerTaskRepository.findById(brokerTask.getId())).thenReturn(Optional.of(brokerTask));
            when(brokerDeploymentClient.stopBrokerNode(brokerNode.getId())).thenReturn(false);
            when(brokerNodeRepository.saveAndFlush(brokerNode)).thenReturn(brokerNode);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> taskProcessor.onTaskreply(broker, brokerNode, brokerTask.getId())
            );

            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_NODE_DEPLOYMENT_STOP_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerDeploymentClient).stopBrokerNode(brokerNode.getId());
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
            broker.setEnvironment("INT");
            broker.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)]);
            broker.setStatus(BrokerStatus.CREATING);
            broker.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);

            return broker;
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
            broker.setEnvironment("INT");
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
}
