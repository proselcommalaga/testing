package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.logsapi.client.feign.nova.rest.IRestHandlerLogsapi;
import com.bbva.enoa.apirestgen.logsapi.model.LogRateThresholdEventInitialValues;
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
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@ExtendWith(MockitoExtension.class)
class LogsClientTest
{
    @Mock
    private IRestHandlerLogsapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private LogsClient client;

    @BeforeEach
    public void init() throws IllegalAccessException, NoSuchFieldException
    {
        MockitoAnnotations.initMocks(LogsClient.class);
        client.initRestHandler();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_create_log_rate_threshold_events_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createLogRateThresholdEvents(Mockito.any(LogRateThresholdEventInitialValues.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createLogRateThresholdEvents(1, "A"));
    }

    @Test
    public void when_create_log_rate_threshold_events_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.createLogRateThresholdEvents(Mockito.any(LogRateThresholdEventInitialValues.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.createLogRateThresholdEvents(1, "A");

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_delete_log_rate_threshold_events_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.deleteLogRateThresholdEvents(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.deleteLogRateThresholdEvents(1));
    }

    @Test
    public void when_delete_log_rate_threshold_events_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.deleteLogRateThresholdEvents(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.deleteLogRateThresholdEvents(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }
}