package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.IRestHandlerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryInfo;
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

@ExtendWith(MockitoExtension.class)
class ReportDeploymentManagerClientTest
{
    @Mock
    private IRestHandlerDeploymentmanagerapi restHandler;
    @InjectMocks
    private ReportDeploymentManagerClient client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(ReportDeploymentManagerClient.class);
        client.init();
    }

    @Test
    public void when_report_host_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getHostReportState(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.reportHost("A"));
    }

    @Test
    public void when_report_host_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getHostReportState(Mockito.anyString())).thenReturn(new ResponseEntity<>("RESULT", HttpStatus.OK));

        String result = client.reportHost("A");

        Assertions.assertEquals("RESULT", result);
    }

    @Test
    public void when_get_products_hosts_report_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getHostsMemoryInfo(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getProductsHostsReport("A", "B"));
    }

    @Test
    public void when_get_products_hosts_report_returns_ok_response_then_return_result()
    {
        HostMemoryInfo[] hostMemoryInfos = DummyConsumerDataGenerator.getDummyHostMemoryInfos();
        Mockito.when(restHandler.getHostsMemoryInfo(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(hostMemoryInfos, HttpStatus.OK));

        HostMemoryInfo[] result = client.getProductsHostsReport("A", "B");

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(hostMemoryInfos[0], result[0]);
    }
}