package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.batchmanagerapi.client.feign.nova.rest.IRestHandlerBatchmanagerapi;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.RunningBatchs;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
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

@ExtendWith(MockitoExtension.class)
class BatchManagerClientTest
{
    @Mock
    private IRestHandlerBatchmanagerapi restHandler;
    @InjectMocks
    private BatchManagerClient client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(BatchManagerClient.class);
        client.init();
    }

    @Test
    public void when_get_running_batch_deployment_services_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getRunningBatchDeploymentServices(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentServiceBatchStatus[] result = client.getRunningBatchDeploymentServices(new int[]{1, 2, 3}, "A");

        Assertions.assertEquals(0, result.length);
    }

    @Test
    public void when_get_running_batch_deployment_services_returns_ok_response_then_return_result()
    {
        DeploymentServiceBatchStatus[] expected = DummyConsumerDataGenerator.getDummyDeploymentServiceBatchStatuses();
        Mockito.when(restHandler.getRunningBatchDeploymentServices(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        DeploymentServiceBatchStatus[] result = client.getRunningBatchDeploymentServices(new int[]{1, 2, 3}, "A");

        Assertions.assertEquals(expected.length, result.length);
        Assertions.assertEquals(expected[0], result[0]);
    }

    @Test
    public void when_get_running_instances_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getRunningInstances(Mockito.anyLong())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        RunningBatchs result = client.getRunningInstances(1L);

        Assertions.assertNull(result.getRunningBatchs());
        Assertions.assertNull(result.getServiceId());
    }

    @Test
    public void when_get_running_instances_returns_ok_response_then_return_result()
    {
        RunningBatchs expected = DummyConsumerDataGenerator.getDummyRunningBatchs();
        Mockito.when(restHandler.getRunningInstances(Mockito.anyLong())).thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        RunningBatchs result = client.getRunningInstances(1L);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void when_get_running_instances_by_deployment_plan_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getRunningInstancesByDeploymentPlan(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Integer result = client.getRunningInstancesByDeploymentPlan(1);

        Assertions.assertEquals(0, result);
    }

    @Test
    public void when_get_running_instances_by_deployment_plan_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getRunningInstancesByDeploymentPlan(Mockito.anyInt())).thenReturn(new ResponseEntity<>(5, HttpStatus.OK));

        Integer result = client.getRunningInstancesByDeploymentPlan(1);

        Assertions.assertEquals(5, result);
    }

    @Test
    public void when_get_batch_executions_summary_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getBatchExecutionsSummary(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        BatchManagerBatchExecutionsSummaryDTO result = client.getBatchExecutionsSummary("A", new long[]{1L}, "B", "C", "D");

        Assertions.assertNull(result.getElements());
        Assertions.assertNull(result.getPercentage());
        Assertions.assertNull(result.getTotal());
    }

    @Test
    public void when_get_batch_executions_summary_returns_ok_response_then_return_result()
    {
        BatchManagerBatchExecutionsSummaryDTO dto = DummyConsumerDataGenerator.getDummyBatchManagerBatchExecutionsSummaryDto();
        Mockito.when(restHandler.getBatchExecutionsSummary(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        BatchManagerBatchExecutionsSummaryDTO result = client.getBatchExecutionsSummary("A", new long[]{1L}, "B", "C", "D");

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_uuaas_ephoenix_legacy_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getUuaasEphoenixLegacy()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        String[] result = client.getUuaasEphoenixLegacy();

        Assertions.assertEquals(0, result.length);
    }

    @Test
    public void when_get_uuaas_ephoenix_legacy_returns_ok_response_then_return_result()
    {
        String[] expected = new String[]{"A", "B", "C"};
        Mockito.when(restHandler.getUuaasEphoenixLegacy()).thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        String[] result = client.getUuaasEphoenixLegacy();

        Assertions.assertEquals(expected, result);
    }
}