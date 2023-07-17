package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.todotaskapi.client.feign.nova.rest.IRestHandlerTodotaskapi;
import com.bbva.enoa.apirestgen.todotaskapi.model.TasksProductCreation;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ProductsAPIToDoTaskServiceTest
{

    @Mock
    private IRestHandlerTodotaskapi iRestHandlerTodotaskapi;
    @InjectMocks
    private ProductsAPIToDoTaskService productsAPIToDoTaskService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.productsAPIToDoTaskService.init();
    }

    @Test
    public void createProductCreationRequestTask()
    {
        TasksProductCreation productRequest = new TasksProductCreation();
        ResponseEntity<Integer> responseEntity = new ResponseEntity<>(1, HttpStatus.OK);

        when(this.iRestHandlerTodotaskapi.createProductCreationTask(any())).thenReturn(responseEntity);

        Integer response = this.productsAPIToDoTaskService.createProductCreationRequestTask(productRequest);
        assertEquals(1, response.intValue());
    }

    @Test
    public void createProductCreationRequestTaskError()
    {
        Errors errorBody = this.generateErrorBody();
        TasksProductCreation tasksProductCreation = this.generateTaskProductCreation();

        when(this.iRestHandlerTodotaskapi.createProductCreationTask(any())).thenReturn(new ResponseEntity(errorBody, HttpStatus.BAD_REQUEST));
        Assertions.assertThrows(NovaException.class, () -> this.productsAPIToDoTaskService.createProductCreationRequestTask(tasksProductCreation));
    }


    @Test
    public void createTask()
    {

        Errors errorBody = this.generateErrorBody();
        ResponseEntity<Integer> responseEntity = new ResponseEntity<>(1, HttpStatus.OK);
        when(this.iRestHandlerTodotaskapi.createTodoTask(any(), any(), any(), any(), any(), any(), any())).thenReturn(responseEntity);
        this.productsAPIToDoTaskService.createTask(1, "TYPE", "ROLE", 1, "DESC", "CODE");

        ResponseEntity<Integer> responseEntityError = new ResponseEntity(errorBody, HttpStatus.BAD_REQUEST);
        when(this.iRestHandlerTodotaskapi.createTodoTask(any(), any(), any(), any(), any(), any(), any())).thenReturn(responseEntityError);
        this.productsAPIToDoTaskService.createTask(1, "TYPE", "ROLE", 1, "DESC", "CODE");
    }

    private Errors generateErrorBody()
    {
        Errors errorBody = new Errors();
        List<ErrorMessage> errorMessageList = new ArrayList<>();

        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setCode("500");
        errorMessage.setMessage("MockedErrorMessage");

        errorMessageList.add(errorMessage);
        errorBody.setMessages(errorMessageList);

        return errorBody;
    }

    private TasksProductCreation generateTaskProductCreation()
    {

        TasksProductCreation tasksProductCreation = new TasksProductCreation();
        tasksProductCreation.setUuaa("UUAA");
        tasksProductCreation.setProductName("PRODUCTNAME");
        tasksProductCreation.setJiraKey("JIRAKEY");
        tasksProductCreation.setUserCode("CODE");

        return tasksProductCreation;
    }
}