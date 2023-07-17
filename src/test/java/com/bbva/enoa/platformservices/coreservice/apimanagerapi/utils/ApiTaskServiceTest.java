//package com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils;
//
//import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
//import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
//import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
//import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class ApiTaskServiceTest
//{
//    @Mock
//    private TodoTaskServiceClient todoTaskServiceClient;
//
//    @InjectMocks
//    private ApiTaskService apiTaskService;
//
//    private SyncApi syncApi;
//
//    private ApiTaskDTO apiTaskDTO;
//
//    @BeforeEach
//    public void setUp() throws Exception
//    {
//        this.syncApi = new SyncApi();
//        syncApi.setName("api");
//        syncApi.setBasePathSwagger("/");
//        syncApi.setUuaa("uuaa");
//
//        ApiTaskKeyDTO apiTaskKeyDTO = new ApiTaskKeyDTO();
//        apiTaskKeyDTO.setApiName("api");
//        apiTaskKeyDTO.setBasePath("/");
//        apiTaskKeyDTO.setUuaa("uuaa");
//        this.apiTaskDTO = new ApiTaskDTO();
//        apiTaskDTO.setApiKey(apiTaskKeyDTO);
//
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @AfterEach
//    public void verifyAllMocks()
//    {
//        verifyNoMoreInteractions(this.todoTaskServiceClient);
//    }
//
//    @Test
//    void getTodoTaskForApi() throws Exception
//    {
//        // When
//        when(this.todoTaskServiceClient.getApiTask(any())).thenReturn(this.apiTaskDTO);
//
//        ApiTaskDTO apiTaskDTO = this.apiTaskService.getTodoTaskForApi(this.syncApi);
//
//        //Then
//        ArgumentCaptor<ApiTaskKeyDTO> argumentCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
//        verify(this.todoTaskServiceClient).getApiTask(argumentCaptor.capture());
//        ApiTaskKeyDTO apiTaskKeyDTO = argumentCaptor.getValue();
//        assertEquals(this.apiTaskDTO, apiTaskDTO);
//        assertEquals(this.syncApi.getName(), apiTaskKeyDTO.getApiName());
//        assertEquals(this.syncApi.getUuaa(), apiTaskKeyDTO.getUuaa());
//        assertEquals(this.syncApi.getBasePathSwagger(), apiTaskKeyDTO.getBasePath());
//    }
//
//    @Test
//    void deleteApiTask() throws Exception
//    {
//        this.apiTaskService.deleteApiTask(this.syncApi);
//
//        // Then
//        ArgumentCaptor<ApiTaskKeyDTO> argumentCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
//        verify(this.todoTaskServiceClient).deleteApiTasks(argumentCaptor.capture());
//        ApiTaskKeyDTO apiTaskKeyDTO = argumentCaptor.getValue();
//        assertEquals(this.syncApi.getName(), apiTaskKeyDTO.getApiName());
//        assertEquals(this.syncApi.getUuaa(), apiTaskKeyDTO.getUuaa());
//        assertEquals(this.syncApi.getBasePathSwagger(), apiTaskKeyDTO.getBasePath());
//    }
//
//}