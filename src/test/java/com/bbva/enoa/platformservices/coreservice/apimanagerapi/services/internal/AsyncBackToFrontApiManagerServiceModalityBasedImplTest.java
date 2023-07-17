package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRegisterApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemoveApiDTO;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskCreationDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.*;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.ApiUtils;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IDefinitionFileValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AsyncBackToFrontApiRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class AsyncBackToFrontApiManagerServiceModalityBasedImplTest
{
    @Mock
    private IApiGatewayManagerClient apiGatewayManagerClient;
    @Mock
    private TodoTaskServiceClient todoTaskClient;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private NovaContext novaContext;
    @Mock
    private AsyncBackToFrontApiRepository asyncBackToFrontApiRepository;
    @Mock
    private IDefinitionFileValidatorModalityBased<NovaAsyncAPI> definitionFileValidator;
    @Mock
    private IApiManagerValidator apiManagerValidator;
    @Mock
    private SecurizableApiManagerCommonService securizableApiManagerCommonService;
    @Mock
    private Utils utils;
    @Mock
    private ApiUtils apiUtils;
    @InjectMocks
    private AsyncBackToFrontApiManagerServiceModalityBasedImpl asyncBackToFrontApiManagerServiceModalityBasedImpl;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                apiGatewayManagerClient,
                todoTaskClient,
                novaActivityEmitter,
                novaContext,
                asyncBackToFrontApiRepository,
                definitionFileValidator,
                apiManagerValidator,
                securizableApiManagerCommonService,
                utils,
                apiUtils
        );
    }

    @Nested
    class UploadApi
    {
        Integer productId = 909;
        String uuaa = "XXXX";
        String fileContent = "fileContent";
        String asyncTitle = "title";
        String asyncVersion = "1.0.0";
        String asyncDescription = "description";
        String asyncBusinessUnit = "JGMV";
        String basePathXmas = "/JGMV/title/1.0.0";
        String md5 = "34j3i4hiuh53";

        @Test
        @DisplayName(value = "Upload API -> product does not exist error")
        public void productNotExists()
        {
            var apiUploadRequest = this.buildApiUploadRequest();
            when(apiManagerValidator.checkProductExistence(anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerServiceModalityBasedImpl.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @DisplayName(value = "Upload API -> Invalid API type")
        @NullAndEmptySource
        @ValueSource(strings = "yyy")
        public void invalidApiType(String apiType)
        {
            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType);
            var product = new Product();

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerServiceModalityBasedImpl.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> AsyncApi validation error")
        public void asyncApiValidationError(ApiType apiType) throws DefinitionFileException
        {
            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenThrow(new DefinitionFileException(List.of("Error")));

            ApiErrorList ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.uploadApi(apiUploadRequest, productId);

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            assertEquals(1, ret.getErrorList().length);
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> API validation error")
        public void apiValidationError(ApiType apiType) throws DefinitionFileException
        {

            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setUuaa(uuaa);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);

            when(novaAsyncAPI.getAsyncAPI().getInfo().getTitle()).thenReturn(asyncTitle);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getVersion()).thenReturn(asyncVersion);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getDescription()).thenReturn(asyncDescription);
            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(asyncBusinessUnit);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(novaAsyncAPI);
            //buildAndSave
            when(apiUtils.ensureStartingSlash(any())).then(i -> i.getArgument(0));
            when(apiUtils.buildXmasApiBasePath(any(), any(), any())).thenReturn(basePathXmas);
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerServiceModalityBasedImpl.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiUtils).ensureStartingSlash(AsyncBackToFrontApiManagerServiceModalityBasedImpl.ASYNC_BACKTOFRONT_BASEPATH);
            if (apiType.isExternal())
            {
                verify(apiUtils).buildXmasApiBasePath(asyncBusinessUnit, asyncTitle, asyncVersion);
            }
            else
            {
                verify(apiUtils).buildXmasApiBasePath(uuaa, asyncTitle, asyncVersion);
            }
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(any());
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> API version already exists error")
        public void apiVersionExistsError(ApiType apiType) throws DefinitionFileException
        {

            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setUuaa(uuaa);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);

            when(novaAsyncAPI.getAsyncAPI().getInfo().getTitle()).thenReturn(asyncTitle);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getVersion()).thenReturn(asyncVersion);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getDescription()).thenReturn(asyncDescription);
            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(asyncBusinessUnit);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(novaAsyncAPI);
            //buildAndSave
            when(apiUtils.ensureStartingSlash(any())).then(i -> i.getArgument(0));
            when(apiUtils.buildXmasApiBasePath(any(), any(), any())).thenReturn(basePathXmas);
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).then(i -> i.getArgument(0));
            doThrow(NovaException.class).when(apiManagerValidator).assertVersionOfApiNotExists(any(), any());

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerServiceModalityBasedImpl.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiUtils).ensureStartingSlash(AsyncBackToFrontApiManagerServiceModalityBasedImpl.ASYNC_BACKTOFRONT_BASEPATH);
            if (apiType.isExternal())
            {
                verify(apiUtils).buildXmasApiBasePath(asyncBusinessUnit, asyncTitle, asyncVersion);
            }
            else
            {
                verify(apiUtils).buildXmasApiBasePath(uuaa, asyncTitle, asyncVersion);
            }
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(any());
            verify(apiManagerValidator).assertVersionOfApiNotExists(any(), eq(asyncVersion));
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> ok")
        public void ok(ApiType apiType) throws DefinitionFileException, JsonProcessingException
        {

            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setUuaa(uuaa);
            product.setId(productId);
            var api = new AsyncBackToFrontApi();
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);

            when(novaAsyncAPI.getAsyncAPI().getInfo().getTitle()).thenReturn(asyncTitle);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getVersion()).thenReturn(asyncVersion);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getDescription()).thenReturn(asyncDescription);
            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(asyncBusinessUnit);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(novaAsyncAPI);
            //buildAndSave
            when(apiUtils.ensureStartingSlash(any())).then(i -> i.getArgument(0));
            when(apiUtils.buildXmasApiBasePath(any(), any(), any())).thenReturn(basePathXmas);
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).thenReturn(api);
            when(utils.unifyCRLF2LF(any())).then(i -> i.getArgument(0));
            when(utils.calculeMd5Hash(any())).thenReturn(md5);

            ApiErrorList ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.uploadApi(apiUploadRequest, productId);

            ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
            ArgumentCaptor<AsyncBackToFrontApi> apiArgumentCaptor = ArgumentCaptor.forClass(AsyncBackToFrontApi.class);
            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiUtils).ensureStartingSlash(AsyncBackToFrontApiManagerServiceModalityBasedImpl.ASYNC_BACKTOFRONT_BASEPATH);
            if (apiType.isExternal())
            {
                verify(apiUtils).buildXmasApiBasePath(asyncBusinessUnit, asyncTitle, asyncVersion);
            }
            else
            {
                verify(apiUtils).buildXmasApiBasePath(uuaa, asyncTitle, asyncVersion);
            }
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(apiArgumentCaptor.capture());
            verify(apiManagerValidator).assertVersionOfApiNotExists(api, asyncVersion);
            verify(utils).unifyCRLF2LF(fileContent);
            verify(utils).calculeMd5Hash(fileContent.getBytes());
            verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());

            assertEquals(0, ret.getErrorList().length);

            AsyncBackToFrontApi capturedApi = apiArgumentCaptor.getValue();
            assertEquals(asyncTitle, capturedApi.getName());
            assertEquals(product, capturedApi.getProduct());
            assertEquals(apiType.isExternal() ? asyncBusinessUnit : uuaa, capturedApi.getUuaa());
            assertEquals(apiType, capturedApi.getType());

            assertEquals(1, api.getApiVersions().size());
            AsyncBackToFrontApiVersion apiVersion = api.getApiVersions().get(0);
            assertEquals(api, apiVersion.getApi());
            assertEquals(asyncDescription, apiVersion.getDescription());
            assertEquals(asyncVersion, apiVersion.getVersion());
            assertEquals(fileContent, apiVersion.getDefinitionFile().getContents());
            assertEquals(ApiState.DEFINITION, apiVersion.getApiState());
            assertEquals(md5, apiVersion.getDefinitionFileHash());

            assertEquals(3, apiVersion.getApiMethods().size());
            ApiMethod apiMethod = apiVersion.getApiMethods().get(0);
            assertEquals(apiVersion, apiMethod.getApiVersion());
            assertEquals("/info", apiMethod.getEndpoint());
            assertEquals(Verb.GET, apiMethod.getVerb());
            apiMethod = apiVersion.getApiMethods().get(1);
            assertEquals(apiVersion, apiMethod.getApiVersion());
            assertEquals("/{id}/{subId}/eventsource", apiMethod.getEndpoint());
            assertEquals(Verb.GET, apiMethod.getVerb());
            apiMethod = apiVersion.getApiMethods().get(2);
            assertEquals(apiVersion, apiMethod.getApiVersion());
            assertEquals("/{id}/{subId}/xhr_send", apiMethod.getEndpoint());
            assertEquals(Verb.POST, apiMethod.getVerb());

            GenericActivity genericActivityCaptured = genericActivityArgumentCaptor.getValue();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>()
            {
            };
            Map<String, Object> params = new ObjectMapper().readValue(genericActivityCaptured.getSerializedStringParams(), typeRef);
            assertEquals(productId, genericActivityCaptured.getProductId());
            assertEquals(ActivityAction.ADDED, genericActivityCaptured.getAction());
            assertEquals(api.getId(), genericActivityCaptured.getEntityId());
            assertEquals(1, params.size());
            assertEquals(api.getName(), params.get("apiName"));
            switch (apiType)
            {
                case NOT_GOVERNED:
                    assertEquals(ActivityScope.UNGOVERNED_API, genericActivityCaptured.getScope());
                    break;
                case GOVERNED:
                    assertEquals(ActivityScope.GOVERNED_API, genericActivityCaptured.getScope());
                    break;
                case EXTERNAL:
                    assertEquals(ActivityScope.THIRD_PARTY_API, genericActivityCaptured.getScope());
                    break;
            }
        }


        ApiUploadRequestDto buildApiUploadRequest()
        {
            var apiUploadRequest = new ApiUploadRequestDto();
            apiUploadRequest.fillRandomly(4, false, 0, 4);
            return apiUploadRequest;
        }

    }

    @Nested
    class CreateApiTask
    {

        String ivUser = "xe72172";

        @ParameterizedTest(name = "[{index}] toDoTaskStatus -> {0}]")
        @EnumSource(value = ToDoTaskStatus.class, names = {"PENDING", "PENDING_ERROR"})
        @DisplayName("Create API task -> todo task status error")
        public void todoTaskStatusError(ToDoTaskStatus toDoTaskStatus)
        {
            var taskInfoDto = this.buildTaskInfoDto();
            var syncApi = this.buildAsyncBackToFrontApi();
            var apiTaskDTO = new ApiTaskDTO();
            apiTaskDTO.setStatus(toDoTaskStatus.name());

            when(novaContext.getIvUser()).thenReturn(ivUser);
            when(todoTaskClient.getApiTask(any())).thenReturn(apiTaskDTO);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerServiceModalityBasedImpl.createApiTask(taskInfoDto, syncApi)
            );

            ArgumentCaptor<ApiTaskKeyDTO> apiTaskKeyDTOArgumentCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
            verify(novaContext).getIvUser();
            verify(todoTaskClient).getApiTask(apiTaskKeyDTOArgumentCaptor.capture());

            ApiTaskKeyDTO apiTaskKeyDTOCaptured = apiTaskKeyDTOArgumentCaptor.getValue();
            assertEquals(syncApi.getName(), apiTaskKeyDTOCaptured.getApiName());
            assertEquals(syncApi.getBasePathSwagger(), apiTaskKeyDTOCaptured.getBasePath());
            assertEquals(syncApi.getUuaa(), apiTaskKeyDTOCaptured.getUuaa());
        }

        @ParameterizedTest(name = "[{index}] apiType -> {0} / todoTaskId -> {1}")
        @ArgumentsSource(AsyncBackToFrontApiManagerServiceModalityBasedImplTest.CreateApiTaskTodoTaskAlreadyCreatedArgumentsSource.class)
        @DisplayName("Create API task -> ok")
        public void ok(ApiType apiType, int todoTaskId) throws JsonProcessingException
        {
            var taskInfoDto = this.buildTaskInfoDto();
            var api = this.buildAsyncBackToFrontApi();
            api.setType(apiType);
            var apiTaskDTO = new ApiTaskDTO();
            apiTaskDTO.fillRandomly(4, false, 1, 4);
            var msaDocument = new DocSystem();
            msaDocument.setId(1);
            msaDocument.setUrl("msa");
            var araDocument = new DocSystem();
            araDocument.setId(2);
            araDocument.setUrl("ara");

            when(novaContext.getIvUser()).thenReturn(ivUser);
            when(todoTaskClient.getApiTask(any())).thenReturn(apiTaskDTO);
            when(todoTaskClient.createApiTask(any())).thenReturn(todoTaskId);
            when(apiManagerValidator.validateAndGetDocument(taskInfoDto.getMsaDocumentId(), DocumentCategory.MSA, DocumentType.FILE)).thenReturn(msaDocument);
            when(apiManagerValidator.validateAndGetDocument(taskInfoDto.getAraDocumentId(), DocumentCategory.ARA, DocumentType.FILE)).thenReturn(araDocument);

            asyncBackToFrontApiManagerServiceModalityBasedImpl.createApiTask(taskInfoDto, api);

            ArgumentCaptor<ApiTaskKeyDTO> apiTaskKeyDTOArgumentCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
            ArgumentCaptor<ApiTaskCreationDTO> apiTaskCreationDTOArgumentCaptor = ArgumentCaptor.forClass(ApiTaskCreationDTO.class);

            verify(novaContext).getIvUser();
            verify(apiManagerValidator, times(2)).validateAndGetDocument(any(), any(), any());
            verify(todoTaskClient).getApiTask(apiTaskKeyDTOArgumentCaptor.capture());
            if (!apiType.isExternal())
            {
                ArgumentCaptor<AGMRegisterApiDTO> registerApiDTOArgumentCaptor = ArgumentCaptor.forClass(AGMRegisterApiDTO.class);
                verify(apiGatewayManagerClient).createRegister(registerApiDTOArgumentCaptor.capture());

                AGMRegisterApiDTO registerApiDTOCaputured = registerApiDTOArgumentCaptor.getValue();
                assertEquals(api.getName(), registerApiDTOCaputured.getApiName());
                assertEquals(api.getBasePathSwagger(), registerApiDTOCaputured.getBasepath());
                assertEquals(api.getUuaa(), registerApiDTOCaputured.getUuaa());
                assertEquals(api.getType().getApiType(), registerApiDTOCaputured.getApiType());
            }
            verify(todoTaskClient).createApiTask(apiTaskCreationDTOArgumentCaptor.capture());
            if (todoTaskId != -1)
            {
                ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
                verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());
                GenericActivity genericActivityCaptured = genericActivityArgumentCaptor.getValue();
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>(){};
                assertEquals(api.getProduct().getId(), genericActivityCaptured.getProductId());
                assertEquals(api.getId(), genericActivityCaptured.getEntityId());
                assertEquals(ActivityAction.SEND_REQUEST_POLICIES_REQUEST, genericActivityCaptured.getAction());
                Map<String, Object> params = new ObjectMapper().readValue(genericActivityCaptured.getSerializedStringParams(), typeRef);
                assertEquals(params.get("apiName"), api.getName());
                assertEquals(params.get("connectorSecurityDocumentation"), araDocument.getUrl());
                assertEquals(params.get("connectorSolutionsArchitectDocumentation"), msaDocument.getUrl());
                assertEquals(params.get("basePath"), api.getBasePathSwagger());
                assertEquals(params.get("TodoTaskTypeCreated"), ToDoTaskType.API_ESTABLISH_POLICIES.name());
                assertEquals(params.get("TodoTaskId"), todoTaskId);
                switch (apiType)
                {
                    case NOT_GOVERNED:
                        assertEquals(ActivityScope.UNGOVERNED_API, genericActivityCaptured.getScope());
                        break;
                    case GOVERNED:
                        assertEquals(ActivityScope.GOVERNED_API, genericActivityCaptured.getScope());
                        break;
                    case EXTERNAL:
                        assertEquals(ActivityScope.THIRD_PARTY_API, genericActivityCaptured.getScope());

                        break;
                }
            }

            ApiTaskKeyDTO apiTaskKeyDTOCaptured = apiTaskKeyDTOArgumentCaptor.getValue();
            assertEquals(api.getName(), apiTaskKeyDTOCaptured.getApiName());
            assertEquals(api.getBasePathSwagger(), apiTaskKeyDTOCaptured.getBasePath());
            assertEquals(api.getUuaa(), apiTaskKeyDTOCaptured.getUuaa());
            ApiTaskCreationDTO apiTaskCreationDTOCaptured = apiTaskCreationDTOArgumentCaptor.getValue();
            assertEquals(api.getName(), apiTaskCreationDTOCaptured.getApiKey().getApiName());
            assertEquals(api.getBasePathSwagger(), apiTaskCreationDTOCaptured.getApiKey().getBasePath());
            assertEquals(api.getUuaa(), apiTaskCreationDTOCaptured.getApiKey().getUuaa());
            assertEquals(taskInfoDto.getProductId(), apiTaskCreationDTOCaptured.getProductId());
            assertEquals(araDocument.getUrl(), apiTaskCreationDTOCaptured.getLinkARA());
            assertEquals(msaDocument.getUrl(), apiTaskCreationDTOCaptured.getLinkMSA());
            assertEquals(ivUser, apiTaskCreationDTOCaptured.getUserCodeCreationTask());
            assertEquals(ToDoTaskType.API_ESTABLISH_POLICIES.name(), apiTaskCreationDTOCaptured.getTaskType());
            assertEquals(ApiPolicyStatus.PENDING, api.getPolicyStatus());
        }

        private TaskInfoDto buildTaskInfoDto()
        {
            var taskInfoDto = new TaskInfoDto();
            taskInfoDto.fillRandomly(4, false, 1, 3);
            return taskInfoDto;
        }

        private AsyncBackToFrontApi buildAsyncBackToFrontApi()
        {
            var product = new Product();
            product.setId(2408);
            var api = new AsyncBackToFrontApi();
            api.setId(610);
            api.setName("apiName");
            api.setBasePathSwagger("/basePath");
            api.setUuaa("JGMV");
            api.setProduct(product);
            return api;
        }
    }

    @Nested
    class OnPolicyTaskReply
    {
        @ParameterizedTest(name = "[{index}] taskStatus -> {0}")
        @EnumSource(value = ToDoTaskStatus.class, names = {"PENDING", "PENDING_ERROR", "ERROR"})
        @DisplayName("On policy task reply -> do nothing")
        public void doNothing(ToDoTaskStatus taskStatus)
        {
            var api = this.buildAsyncBackToFrontApi();

            asyncBackToFrontApiManagerServiceModalityBasedImpl.onPolicyTaskReply(taskStatus, api);
        }

        @Test
        @DisplayName("On policy task reply -> done")
        public void done()
        {
            var taskStatus = ToDoTaskStatus.DONE;
            var api = this.buildAsyncBackToFrontApi();

            asyncBackToFrontApiManagerServiceModalityBasedImpl.onPolicyTaskReply(taskStatus, api);

            verify(securizableApiManagerCommonService).addPoliciesToApi(api);
            verify(securizableApiManagerCommonService).emitNewPoliciesActivityResponse(taskStatus, api, ActivityAction.POLICIES_REQUEST_DONE);
            assertEquals(ApiPolicyStatus.ESTABLISHED, api.getPolicyStatus());
        }

        @Test
        @DisplayName("On policy task reply -> rejected")
        public void rejected()
        {
            var taskStatus = ToDoTaskStatus.REJECTED;
            var api = this.buildAsyncBackToFrontApi();

            asyncBackToFrontApiManagerServiceModalityBasedImpl.onPolicyTaskReply(taskStatus, api);

            verify(securizableApiManagerCommonService).emitNewPoliciesActivityResponse(taskStatus, api, ActivityAction.POLICIES_REQUEST_REJECTED);
            assertEquals(ApiPolicyStatus.REJECTED, api.getPolicyStatus());
        }

        private AsyncBackToFrontApi buildAsyncBackToFrontApi()
        {
            return new AsyncBackToFrontApi();
        }
    }

    @Nested
    class RemoveApiRegistration
    {
        @Test
        @DisplayName(value = "Remove API registration -> OK")
        public void ok()
        {
            var api = new AsyncBackToFrontApi();
            api.setName("apiName");
            api.setUuaa("JGMV");
            api.setBasePathSwagger("/basePath");

            asyncBackToFrontApiManagerServiceModalityBasedImpl.removeApiRegistration(api);

            ArgumentCaptor<AGMRemoveApiDTO> removeApiDTOArgumentCaptor = ArgumentCaptor.forClass(AGMRemoveApiDTO.class);
            verify(apiGatewayManagerClient).removeRegister(removeApiDTOArgumentCaptor.capture());
            AGMRemoveApiDTO removeApiDTOCaptured = removeApiDTOArgumentCaptor.getValue();
            assertEquals(api.getName(), removeApiDTOCaptured.getApiName());
            assertEquals(api.getUuaa(), removeApiDTOCaptured.getUuaa());
            assertEquals(api.getBasePathSwagger(), removeApiDTOCaptured.getBasepath());
        }
    }

    @Nested
    class DeleteApiTodoTasks
    {
        @Test
        @DisplayName(value = "Delete API s todo tasks -> OK")
        public void ok()
        {
            var api = new AsyncBackToFrontApi();
            api.setName("apiName");
            api.setUuaa("JGMV");
            api.setBasePathSwagger("/basePath");

            asyncBackToFrontApiManagerServiceModalityBasedImpl.deleteApiTodoTasks(api);

            ArgumentCaptor<ApiTaskKeyDTO> apiTaskKeyDTOArgumentCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
            verify(todoTaskClient).deleteApiTasks(apiTaskKeyDTOArgumentCaptor.capture());
            ApiTaskKeyDTO apiTaskKeyDTOCaptured = apiTaskKeyDTOArgumentCaptor.getValue();
            assertEquals(api.getName(), apiTaskKeyDTOCaptured.getApiName());
            assertEquals(api.getUuaa(), apiTaskKeyDTOCaptured.getUuaa());
            assertEquals(api.getBasePathSwagger(), apiTaskKeyDTOCaptured.getBasePath());
        }
    }

    @Nested
    class GetApisUsingMsaDocument
    {
        @Test
        @DisplayName("Get APIs using MSA document -> OK")
        public void ok()
        {
            Integer msaId = 909;
            var api = new AsyncBackToFrontApi();

            when(asyncBackToFrontApiRepository.findByMsaDocumentId(anyInt())).thenReturn(List.of(api));

            List<AsyncBackToFrontApi> ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.getApisUsingMsaDocument(msaId);

            verify(asyncBackToFrontApiRepository).findByMsaDocumentId(msaId);
            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
        }

        @Test
        @DisplayName("Get APIs using MSA document -> Empty OK")
        public void emptyOk()
        {
            Integer msaId = 909;

            when(asyncBackToFrontApiRepository.findByMsaDocumentId(anyInt())).thenReturn(Collections.emptyList());

            List<AsyncBackToFrontApi> ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.getApisUsingMsaDocument(msaId);

            verify(asyncBackToFrontApiRepository).findByMsaDocumentId(msaId);
            assertEquals(0, ret.size());
        }
    }

    @Nested
    class GetApisUsingAraDocument
    {
        @Test
        @DisplayName("Get APIs using ARA document -> OK")
        public void ok()
        {
            Integer msaId = 909;
            var api = new AsyncBackToFrontApi();

            when(asyncBackToFrontApiRepository.findByAraDocumentId(anyInt())).thenReturn(List.of(api));

            List<AsyncBackToFrontApi> ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.getApisUsingAraDocument(msaId);

            verify(asyncBackToFrontApiRepository).findByAraDocumentId(msaId);
            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
        }

        @Test
        @DisplayName("Get APIs using ARA document -> Empty OK")
        public void emptyOk()
        {
            Integer msaId = 909;

            when(asyncBackToFrontApiRepository.findByAraDocumentId(anyInt())).thenReturn(Collections.emptyList());

            List<AsyncBackToFrontApi> ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.getApisUsingAraDocument(msaId);

            verify(asyncBackToFrontApiRepository).findByAraDocumentId(msaId);
            assertEquals(0, ret.size());
        }
    }

    @Nested
    class CreateApiImplementation
    {
        @ParameterizedTest
        @EnumSource(ImplementedAs.class)
        @DisplayName("Create API implementation -> ok")
        public void ok(ImplementedAs implementedAs)
        {
            var apiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class);
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            AsyncBackToFrontApiImplementation ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.createApiImplementation(apiVersion, releaseVersionService, implementedAs);

            assertEquals(apiVersion, ret.getApiVersion());
            assertEquals(releaseVersionService, ret.getService());
            assertEquals(implementedAs, ret.getImplementedAs());
            verifyNoInteractions(apiVersion, releaseVersionService);
        }
    }

    @Nested
    class SetConsumedApisByServedApi
    {
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Set consumed APIs by server API -> do nothing")
        public void doNothing(List<Integer> consumedApis)
        {
            var servedApi = Mockito.mock(AsyncBackToFrontApiImplementation.class);

            asyncBackToFrontApiManagerServiceModalityBasedImpl.setConsumedApisByServedApi(servedApi, consumedApis);

            verifyNoInteractions(servedApi);
        }
    }

    @Nested
    class SetBackwardCompatibleVersionsOfServedApi
    {
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Set backward compatible versions of served API -> do nothing")
        public void doNothing(List<String> backwardCompatibleVersions)
        {
            var servedApi = Mockito.mock(AsyncBackToFrontApiImplementation.class);

            asyncBackToFrontApiManagerServiceModalityBasedImpl.setBackwardCompatibleVersionsOfServedApi(servedApi, backwardCompatibleVersions);

            verifyNoInteractions(servedApi);
        }
    }

    @Nested
    class DownloadProductApi
    {
        String content = "fileContent";

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"XXX"})
        @DisplayName("Download product API -> invalid format")
        public void invalidFormat(String format)
        {
            var apiVersion = new AsyncBackToFrontApiVersion();

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerServiceModalityBasedImpl.downloadProductApi(apiVersion, format, "other")
            );
        }

        @Nested
        class YamlFormat
        {
            @ParameterizedTest
            @CsvSource(value = {"YML, CONSUMED", "YAML, other"})
            @DisplayName("Download product API -> yaml format")
            public void ok(String format, String downloadType)
            {
                var apiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class, Mockito.RETURNS_DEEP_STUBS);

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(content);

                byte[] ret = asyncBackToFrontApiManagerServiceModalityBasedImpl.downloadProductApi(apiVersion, format, downloadType);

                assertArrayEquals(content.getBytes(), ret);
            }
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOFRONT")
        public void backToFront()
        {
            assertTrue(asyncBackToFrontApiManagerServiceModalityBasedImpl.isModalitySupported(ApiModality.ASYNC_BACKTOFRONT));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOBACK"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToFrontApiManagerServiceModalityBasedImpl.isModalitySupported(modality));
        }
    }

    private static class CreateApiTaskTodoTaskAlreadyCreatedArgumentsSource implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Stream.of(
                    Arguments.arguments(ApiType.EXTERNAL, -1),
                    Arguments.arguments(ApiType.GOVERNED, -1),
                    Arguments.arguments(ApiType.NOT_GOVERNED, -1),
                    Arguments.arguments(ApiType.EXTERNAL, 909),
                    Arguments.arguments(ApiType.GOVERNED, 909),
                    Arguments.arguments(ApiType.NOT_GOVERNED, 909)
            );
        }
    }
}
