package com.bbva.enoa.platformservices.coreservice.brokersapi.listener;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerNotificationDTO;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerInformationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNodeOperator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNotificationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerOperator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ListenerBrokersapiTest
{

    public static final String IV_USER = "xe70505";

    @Mock
    private IBrokerOperator brokerService;

    @Mock
    private IBrokerNotificationService brokerNotificationService;

    @Mock
    private IBrokerInformationService brokerInformationGetter;

    @InjectMocks
    private ListenerBrokersapi listenerBrokersapi;

    @Mock
    private IBrokerNodeOperator brokerNodeService;


    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyMocks()
    {
        verifyNoMoreInteractions(brokerService);
        verifyNoMoreInteractions(brokerNotificationService);
    }

    private static NovaMetadata getNovaMetadata()
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList(IV_USER));
        NovaMetadata metadata = new NovaMetadata();
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        return metadata;
    }

    @Nested
    class getBrokersByProduct
    {
        Integer productId = 69;

        @Test
        void ok() throws Exception
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            BrokerDTO[] expectedDTOs = new BrokerDTO[]{brokerDTO};
            // When
            when(brokerInformationGetter.getBrokersByProduct(productId)).thenReturn(expectedDTOs);
            BrokerDTO[] returnValue = listenerBrokersapi.getBrokersByProduct(getNovaMetadata(), productId);

            verify(brokerInformationGetter).getBrokersByProduct(productId);
            assertEquals(expectedDTOs, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerInformationGetter).getBrokersByProduct(productId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.getBrokersByProduct(novaMetadata, productId));
            verify(brokerInformationGetter).getBrokersByProduct(productId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(brokerInformationGetter).getBrokersByProduct(productId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.getBrokersByProduct(novaMetadata, productId));
            verify(brokerInformationGetter).getBrokersByProduct(productId);
        }
    }


    @Nested
    class deleteBroker
    {
        Integer brokerId = 69;

        @Test
        void ok() throws Exception
        {
            // When
            listenerBrokersapi.deleteBroker(getNovaMetadata(), brokerId);

            verify(brokerService).deleteBroker(IV_USER, brokerId);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerService).deleteBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.deleteBroker(novaMetadata, brokerId));
            verify(brokerService).deleteBroker(IV_USER, brokerId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(brokerService).deleteBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.deleteBroker(novaMetadata, brokerId));
            verify(brokerService).deleteBroker(IV_USER, brokerId);
        }
    }


    @Nested
    class restartBroker
    {
        Integer brokerId = 69;

        @Test
        void ok() throws Exception
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            // When
            when(brokerService.restartBroker(IV_USER, brokerId)).thenReturn(brokerDTO);
            BrokerDTO returnValue = listenerBrokersapi.restartBroker(getNovaMetadata(), brokerId);

            verify(brokerService).restartBroker(IV_USER, brokerId);
            assertEquals(brokerDTO, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerService).restartBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.restartBroker(novaMetadata, brokerId));
            verify(brokerService).restartBroker(IV_USER, brokerId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(brokerService).restartBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.restartBroker(novaMetadata, brokerId));
            verify(brokerService).restartBroker(IV_USER, brokerId);
        }
    }


    @Nested
    class stopBroker
    {
        Integer brokerId = 69;

        @Test
        void ok() throws Exception
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            // When
            when(brokerService.stopBroker(IV_USER, brokerId)).thenReturn(brokerDTO);
            BrokerDTO returnValue = listenerBrokersapi.stopBroker(getNovaMetadata(), brokerId);

            verify(brokerService).stopBroker(IV_USER, brokerId);
            assertEquals(brokerDTO, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerService).stopBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.stopBroker(novaMetadata, brokerId));
            verify(brokerService).stopBroker(IV_USER, brokerId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(brokerService).stopBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.stopBroker(novaMetadata, brokerId));
            verify(brokerService).stopBroker(IV_USER, brokerId);
        }
    }


    @Nested
    class startBroker
    {
        Integer brokerId = 69;

        @Test
        void ok() throws Exception
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            // When
            when(brokerService.startBroker(IV_USER, brokerId)).thenReturn(brokerDTO);
            BrokerDTO returnValue = listenerBrokersapi.startBroker(getNovaMetadata(), brokerId);

            verify(brokerService).startBroker(IV_USER, brokerId);
            assertEquals(brokerDTO, returnValue);
        }

        @Test
        void novaException()
        {
            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerService).startBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.startBroker(novaMetadata, brokerId));
            verify(brokerService).startBroker(IV_USER, brokerId);
        }

        @Test
        void runtimeException()
        {
            doThrow(RuntimeException.class).when(brokerService).startBroker(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.startBroker(novaMetadata, brokerId));
            verify(brokerService).startBroker(IV_USER, brokerId);
        }
    }

    @Nested
    class createBroker
    {

        @Test
        void ok() throws Exception
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            // When
            when(brokerService.createBroker(IV_USER, brokerDTO)).thenReturn(brokerDTO);
            BrokerDTO returnValue = listenerBrokersapi.createBroker(getNovaMetadata(), brokerDTO);

            verify(brokerService).createBroker(IV_USER, brokerDTO);
            assertEquals(brokerDTO, returnValue);
        }

        @Test
        void novaException()
        {
            BrokerDTO brokerDTO = new BrokerDTO();

            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerService).createBroker(IV_USER, brokerDTO);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.createBroker(novaMetadata, brokerDTO));
            verify(brokerService).createBroker(IV_USER, brokerDTO);
        }

        @Test
        void runtimeException()
        {
            BrokerDTO brokerDTO = new BrokerDTO();

            doThrow(RuntimeException.class).when(brokerService).createBroker(IV_USER, brokerDTO);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.createBroker(novaMetadata, brokerDTO));
            verify(brokerService).createBroker(IV_USER, brokerDTO);
        }
    }


    @Nested
    class notifyBrokerOperationResult
    {
        Integer brokerId = 69;
        BrokerNotificationDTO brokerNotificationDTO = new BrokerNotificationDTO();

        @Test
        void ok() throws Exception
        {
            // When;
            listenerBrokersapi.notifyBrokerOperationResult(getNovaMetadata(), brokerNotificationDTO, brokerId);

            verify(brokerNotificationService).notifyBrokerOperationResult(brokerId, brokerNotificationDTO);
        }

        @Test
        void novaException()
        {

            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerNotificationService).notifyBrokerOperationResult(brokerId, brokerNotificationDTO);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.notifyBrokerOperationResult(novaMetadata, brokerNotificationDTO, brokerId));
            verify(brokerNotificationService).notifyBrokerOperationResult(brokerId, brokerNotificationDTO);
        }

        @Test
        void runtimeException()
        {

            doThrow(RuntimeException.class).when(brokerNotificationService).notifyBrokerOperationResult(brokerId, brokerNotificationDTO);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.notifyBrokerOperationResult(novaMetadata, brokerNotificationDTO, brokerId));
            verify(brokerNotificationService).notifyBrokerOperationResult(brokerId, brokerNotificationDTO);
        }
    }


    @Nested
    class getBrokerDetails
    {
        BrokerDTO brokerDTO = new BrokerDTO();
        Integer brokerId = 69;

        @Test
        void ok() throws Exception
        {
            // When
            when(brokerInformationGetter.getBrokerInfo(IV_USER, brokerId)).thenReturn(brokerDTO);
            BrokerDTO returnValue = listenerBrokersapi.getBrokerDetails(getNovaMetadata(), brokerId);

            verify(brokerInformationGetter).getBrokerInfo(IV_USER, brokerId);
            assertEquals(brokerDTO, returnValue);
        }

        @Test
        void novaException()
        {

            doThrow(new NovaException(BrokerError.getUnexpectedError())).when(brokerInformationGetter).getBrokerInfo(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.getBrokerDetails(novaMetadata, brokerId));
            verify(brokerInformationGetter).getBrokerInfo(IV_USER, brokerId);
        }

        @Test
        void runtimeException()
        {

            doThrow(RuntimeException.class).when(brokerInformationGetter).getBrokerInfo(IV_USER, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.getBrokerDetails(novaMetadata, brokerId));
            verify(brokerInformationGetter).getBrokerInfo(IV_USER, brokerId);
        }
    }

    @Nested
    class onBrokerTaskreply
    {
        Integer brokerId = RandomUtils.nextInt(1, 10);
        Integer brokerNodeId = RandomUtils.nextInt(1, 10);
        Integer taskId = RandomUtils.nextInt(1, 10);

        @Test
        void taskBrokerOk() throws Exception
        {
            // When
            listenerBrokersapi.onTaskReply(getNovaMetadata(), brokerId, taskId, 0);

            verify(brokerService).onTaskReply(taskId, brokerId);
        }

        @Test
        void taskBrokerNodeOk() throws Exception
        {
            // When
            listenerBrokersapi.onTaskReply(getNovaMetadata(), brokerId, taskId, brokerNodeId);

            verify(brokerNodeService).onTaskReply(taskId, brokerNodeId);
        }

        @Test
        void taskBrokerNovaException()
        {
            doThrow(new NovaException(BrokerError.getBrokerNotFoundError(brokerId))).when(brokerService).onTaskReply(taskId, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.onTaskReply(novaMetadata, brokerId, taskId, 0));
            verify(brokerService).onTaskReply(taskId, brokerId);
        }

        @Test
        void taskBrokerNodeNovaException()
        {
            doThrow(new NovaException(BrokerError.getBrokerNodeNotFoundError(brokerNodeId))).when(brokerNodeService).onTaskReply(taskId, brokerNodeId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(NovaException.class, () -> listenerBrokersapi.onTaskReply(novaMetadata, brokerId, taskId, brokerNodeId));
            verify(brokerNodeService).onTaskReply(taskId, brokerNodeId);
        }

        @Test
        void TestBrokerRuntimeException()
        {
            doThrow(RuntimeException.class).when(brokerService).onTaskReply(taskId, brokerId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.onTaskReply(novaMetadata, brokerId, taskId, 0));
            verify(brokerService).onTaskReply(taskId, brokerId);
        }

        @Test
        void TestBrokerNodeRuntimeException()
        {
            doThrow(RuntimeException.class).when(brokerNodeService).onTaskReply(taskId, brokerNodeId);

            // When
            NovaMetadata novaMetadata = getNovaMetadata();
            assertThrows(RuntimeException.class, () -> listenerBrokersapi.onTaskReply(novaMetadata, brokerId, taskId, brokerNodeId));
            verify(brokerNodeService).onTaskReply(taskId, brokerNodeId);
        }
    }
}