package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.issuetrackerapi.client.feign.nova.rest.IRestHandlerIssuetrackerapi;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewDeploymentRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewProjectRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
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
class IssueTrackerClientTest
{
    @Mock
    private IRestHandlerIssuetrackerapi restHandler;
    @Mock
    private IErrorTaskManager errorTaskMgr;
    @Mock
    private Logger log;
    @InjectMocks
    private IssueTrackerClient client;

    @BeforeEach
    public void init() throws IllegalAccessException, NoSuchFieldException
    {
        MockitoAnnotations.initMocks(IssueTrackerClient.class);
        client.init();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_create_deployment_request_returns_ko_response_then_generate_to_do_task()
    {
        Mockito.when(restHandler.createDeploymentRequest(Mockito.any(ITNewDeploymentRequest.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.createDeploymentRequest(new ITNewDeploymentRequest(), 1);

        Mockito.verify(errorTaskMgr, Mockito.times(1)).createErrorTask(Mockito.anyInt(), Mockito.any(), Mockito.anyString(), Mockito.any(ToDoTaskType.class), Mockito.any());
    }

    @Test
    public void when_create_deployment_request_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.createDeploymentRequest(Mockito.any(ITNewDeploymentRequest.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyIssueTrackerItems(), HttpStatus.OK));

        IssueTrackerItem[] result = client.createDeploymentRequest(new ITNewDeploymentRequest(), 1);

        Assertions.assertEquals(1, result.length);
        IssueTrackerItem firstResult = result[0];
        Assertions.assertEquals("projectkey", firstResult.getProjectKey());
    }

    @Test
    public void when_create_project_request_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.createProjectRequest(Mockito.any(ITNewProjectRequest.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.createProjectRequest(new ITNewProjectRequest());

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(ITNewProjectRequest.class), Mockito.any());
    }

    @Test
    public void when_create_project_request_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.createProjectRequest(Mockito.any(ITNewProjectRequest.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyIssueTrackerItems()[0], HttpStatus.OK));

        IssueTrackerItem result = client.createProjectRequest(new ITNewProjectRequest());

        Assertions.assertEquals("projectkey", result.getProjectKey());
    }
}