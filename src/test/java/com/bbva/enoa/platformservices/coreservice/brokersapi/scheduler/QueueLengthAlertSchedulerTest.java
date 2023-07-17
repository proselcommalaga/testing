package com.bbva.enoa.platformservices.coreservice.brokersapi.scheduler;

import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class QueueLengthAlertSchedulerTest
{
    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private QueueLengthAlertScheduler.QueueLengthChecker queueLengthChecker;

    @InjectMocks
    private QueueLengthAlertScheduler queueLengthAlertScheduler;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void ok()
    {
        Broker broker1 = new Broker();
        broker1.setId(111);
        Broker broker2 = new Broker();
        broker2.setId(222);

        when(brokerRepository.findByStatus(BrokerStatus.RUNNING)).thenReturn(List.of(broker1, broker2));

        queueLengthAlertScheduler.checkQueueLength();

        verify(queueLengthChecker).checkBroker(111);
        verify(queueLengthChecker).checkBroker(222);
        verify(brokerRepository).findByStatus(BrokerStatus.RUNNING);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoMoreInteractions(queueLengthChecker);
    }
}