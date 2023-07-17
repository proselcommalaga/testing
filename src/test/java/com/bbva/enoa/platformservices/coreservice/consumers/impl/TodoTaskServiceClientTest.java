package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.IRestHandlerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskCreationDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskFilterRequestDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskFilterResponseDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ExceptionDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.TaskSummaryList;
import com.bbva.enoa.datamodel.model.product.entities.Product;
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
public class TodoTaskServiceClientTest
{
    @Mock
    private IRestHandlerTodotaskapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private TodoTaskServiceClient client;
    private Product product;

    @BeforeEach
    public void init() throws Exception
    {
        MockitoAnnotations.initMocks(TodoTaskServiceClient.class);
        this.client.init();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);

        product = new Product();
        product.setId(1);
        product.setDesBoard("DES_BOARD");
    }

    @Test
    public void when_create_generic_task_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.isNull(), Mockito.anyString(), Mockito.isNull())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createGenericTask("A", 1, "B", "C", "D", 2));
    }

    @Test
    public void when_create_generic_task_returns_ok_response_then_return_result()
    {
        int expectedResult = 100;
        Mockito.when(restHandler.createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.isNull(), Mockito.anyString(), Mockito.isNull())).thenReturn(new ResponseEntity<>(expectedResult, HttpStatus.OK));

        Integer result = client.createGenericTask("A", 1, "B", "C", "D", 2);

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void when_create_deployment_task_has_product_with_null_des_board_then_throw_exception()
    {
        Assertions.assertThrows(NovaException.class, () -> client.createDeploymentTask(1, 2, "", "", "", new Product()));
    }

    @Test
    public void when_create_deployment_task_has_product_with_empty_des_board_then_throw_exception()
    {
        product.setDesBoard("");
        Assertions.assertThrows(NovaException.class, () -> client.createDeploymentTask(1, 2, "", "", "", product));
    }

    @Test
    public void when_create_deployment_task_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.isNull())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createDeploymentTask(1, 2, "B", "C", "D", product));
        Mockito.verify(restHandler, Mockito.times(1)).createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.isNull());
    }

    @Test
    public void when_create_deployment_task_returns_ok_response_then_return_result()
    {
        int expectedResult = 100;
        Mockito.when(restHandler.createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.isNull())).thenReturn(new ResponseEntity<>(expectedResult, HttpStatus.OK));

        Integer result = client.createDeploymentTask(1, 2, "B", "C", "D", product);

        Assertions.assertEquals(expectedResult, result);
    }


    @Test
    public void when_create_management_task_has_product_with_empty_des_board_then_throw_exception()
    {
        product.setDesBoard("");
        Assertions.assertThrows(NovaException.class, () -> client.createManagementTask("", 1, "", "", "", product, 1));
    }

    @Test
    public void when_create_management_task_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.isNull(), Mockito.anyString(), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createManagementTask("", 1, "", "", "", product, 1));
        Mockito.verify(restHandler, Mockito.times(1)).createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.isNull(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_create_management_task_returns_ok_response_then_return_result()
    {
        int expectedResult = 100;
        Mockito.when(restHandler.createTodoTask(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.isNull(), Mockito.anyString(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(expectedResult, HttpStatus.OK));

        Integer result = client.createManagementTask("", 1, "", "", "", product, 1);

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void when_create_api_task_returns_ko_with_http_code_not_409_response_then_throw_exception()
    {
        Mockito.when(restHandler.createApiTask(Mockito.any(ApiTaskCreationDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.FORBIDDEN));

        Assertions.assertThrows(NovaException.class, () -> client.createApiTask(DummyConsumerDataGenerator.getDummyApiTaskCreationDto()));
    }

    @Test
    public void when_create_api_task_returns_ok_with_http_code_409_response_then_return_to_do_task_already_created_code()
    {
        Mockito.when(restHandler.createApiTask(Mockito.any(ApiTaskCreationDTO.class))).thenReturn(new ResponseEntity(new ExceptionDTO(), HttpStatus.valueOf(409)));

        Integer result = client.createApiTask(DummyConsumerDataGenerator.getDummyApiTaskCreationDto());

        Assertions.assertEquals(-1, result);
    }

    @Test
    public void when_create_api_task_returns_ok_with_http_code_not_409_response_then_return_result()
    {
        int expectedResult = 100;
        Mockito.when(restHandler.createApiTask(Mockito.any(ApiTaskCreationDTO.class))).thenReturn(new ResponseEntity<>(expectedResult, HttpStatus.OK));

        Integer result = client.createApiTask(DummyConsumerDataGenerator.getDummyApiTaskCreationDto());

        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void when_get_api_task_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getApiTask(Mockito.any(ApiTaskKeyDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getApiTask(DummyConsumerDataGenerator.getDummyApiTaskKeyDto()));
    }

    @Test
    public void when_get_api_task_returns_ok_response_then_return_result()
    {
        ApiTaskDTO dto = DummyConsumerDataGenerator.getDummyApiTaskDto();
        Mockito.when(restHandler.getApiTask(Mockito.any(ApiTaskKeyDTO.class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        ApiTaskDTO result = client.getApiTask(DummyConsumerDataGenerator.getDummyApiTaskKeyDto());

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_api_task_list_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.filterApiTask(Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getApiTaskList(new ApiTaskFilterRequestDTO[0]));
    }

    @Test
    public void when_get_api_task_list_returns_ok_response_then_return_result()
    {
        ApiTaskFilterResponseDTO[] dtos = DummyConsumerDataGenerator.getDummyApiTaskFilterResponseDtos();
        Mockito.when(restHandler.filterApiTask(Mockito.any())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        ApiTaskFilterResponseDTO[] result = client.getApiTaskList(new ApiTaskFilterRequestDTO[0]);

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_delete_api_tasks_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.deleteApiTask(Mockito.any(ApiTaskKeyDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.deleteApiTasks(DummyConsumerDataGenerator.getDummyApiTaskKeyDto()));
    }

    @Test
    public void when_delete_api_tasks_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.deleteApiTask(Mockito.any(ApiTaskKeyDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.deleteApiTasks(new ApiTaskKeyDTO());

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.any(ApiTaskKeyDTO.class));
    }

    @Test
    public void when_get_todo_tasks_since_days_ago_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getTodoTasksSinceDaysAgo(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getTodoTasksSinceDaysAgo(1, "A", "B"));
    }

    @Test
    public void when_get_todo_tasks_since_days_ago_returns_ok_response_then_return_result()
    {
        TaskSummaryList taskSummaryList = DummyConsumerDataGenerator.getDummyTaskSummaryList();
        Mockito.when(restHandler.getTodoTasksSinceDaysAgo(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(taskSummaryList, HttpStatus.OK));

        TaskSummaryList result = client.getTodoTasksSinceDaysAgo(1, "A", "B");

        Assertions.assertEquals(taskSummaryList, result);
    }

    //getTodoTasksSinceDaysAgo
}