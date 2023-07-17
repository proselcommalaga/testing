package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.platformservices.coreservice.common.repositories.ServiceExecutionHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class NotStartedServicesProviderImplTest
{
    @Mock
    private ServiceExecutionHistoryRepository repository;
    @InjectMocks
    private NotStartedServicesProviderImpl provider;

    @BeforeEach
    public void init() throws NoSuchFieldException
    {
        MockitoAnnotations.initMocks(NotStartedServicesProviderImpl.class);
        ReflectionTestUtils.setField(provider, "daysBeforeNow", 30);
    }

    @Test
    public void when_no_service_executions_found_then_return_same_passed_service_names()
    {
        Mockito.when(repository.findExecutedVersionedFinalNamesMatching(Mockito.anyList(), Mockito.anyString(), Mockito.any(Calendar.class))).thenReturn(Collections.emptyList());

        List<String> serviceNames = Arrays.asList("service1", "service2");
        List<String> result = provider.getNotStartedVersionedNamesFromServiceExecutionHistory(serviceNames, "INT");

        Assertions.assertEquals(serviceNames, result);
    }

    @Test
    public void when_some_service_executions_found_then_return_passed_service_names_minus_executed_services()
    {
        Mockito.when(repository.findExecutedVersionedFinalNamesMatching(Mockito.anyList(), Mockito.anyString(), Mockito.any(Calendar.class))).thenReturn(Collections.singletonList("service1"));

        List<String> serviceNames = new ArrayList<>(2);
        serviceNames.add("service1");
        serviceNames.add("service2");
        List<String> result = provider.getNotStartedVersionedNamesFromServiceExecutionHistory(serviceNames, "INT");

        Assertions.assertEquals(List.of("service2"), result);
    }

    @Test
    public void when_all_service_executions_found_then_return_empty_service_list()
    {
        Mockito.when(repository.findExecutedVersionedFinalNamesMatching(Mockito.anyList(), Mockito.anyString(), Mockito.any(Calendar.class))).thenReturn(List.of("service1", "service2"));

        List<String> serviceNames = new ArrayList<>(2);
        serviceNames.add("service1");
        serviceNames.add("service2");
        List<String> result = provider.getNotStartedVersionedNamesFromServiceExecutionHistory(serviceNames, "INT");

        Assertions.assertEquals(0, result.size());
    }

}