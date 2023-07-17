package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.schedulecontrolmapi.client.feign.nova.rest.IRestHandlerSchedulecontrolmapi;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Created by XE56809 on 14/03/2018.
 */
@ExtendWith({MockitoExtension.class})
public class ScheduleControlMClientTest
{
    @Mock
    private IRestHandlerSchedulecontrolmapi restHandler;
    @InjectMocks
    private ScheduleControlMClient client;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.client.init();
    }

    @Test
    public void when_get_schedule_active_request_returns_ko_response_then_return_null()
    {
        Mockito.when(restHandler.getActiveRequestAt(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        ScheduleRequest result = client.getActiveRequestAt(1L, "A", "B");

        Assertions.assertNull(result);
    }

    @Test
    public void when_get_schedule_active_request_returns_ok_response_then_return_result()
    {
        ScheduleRequest scheduleRequest = DummyConsumerDataGenerator.getDummyScheduleRequest();
        Mockito.when(restHandler.getActiveRequestAt(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(scheduleRequest, HttpStatus.OK));

        ScheduleRequest result = client.getActiveRequestAt(1L, "A", "B");

        Assertions.assertEquals(scheduleRequest, result);
    }

    @Test
    public void when_get_valid_for_deployment_returns_ko_response_then_throws_exception()
    {
        Mockito.when(restHandler.getValidForDeployment(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getValidForDeployment(1, "A"));
    }

    @Test
    public void when_get_valid_for_deployment_returns_ok_response_then_return_result()
    {
        ScheduleRequest[] scheduleRequests = new ScheduleRequest[]{DummyConsumerDataGenerator.getDummyScheduleRequest()};
        Mockito.when(restHandler.getValidForDeployment(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(scheduleRequests, HttpStatus.OK));

        ScheduleRequest[] result = client.getValidForDeployment(1, "A");

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(scheduleRequests[0], result[0]);
    }
}