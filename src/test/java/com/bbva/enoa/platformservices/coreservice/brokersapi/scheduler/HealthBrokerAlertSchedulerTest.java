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

class HealthBrokerAlertSchedulerTest
{
    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private HealthBrokerAlertScheduler.HealthChecker healthChecker;

    @InjectMocks
    private HealthBrokerAlertScheduler healthBrokerAlertScheduler;

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

        healthBrokerAlertScheduler.checkRunningBrokers();

        verify(healthChecker).checkBroker(111);
        verify(healthChecker).checkBroker(222);
        verify(brokerRepository).findByStatus(BrokerStatus.RUNNING);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoMoreInteractions(healthChecker);
    }
}