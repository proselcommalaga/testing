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
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.ApiUtils;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IDefinitionFileValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
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
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SyncApiManagerServiceModalityBasedImplTest
{
    @Mock
    private SyncApiRepository apiRepository;
    @Mock
    private SyncApiVersionRepository apiVersionRepository;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private IApiManagerValidator apiManagerValidator;
    @Mock
    private IApiGatewayManagerClient apiGatewayManagerClient;
    @Mock
    private TodoTaskServiceClient todoTaskClient;
    @Mock
    private NovaContext novaContext;
    @Mock
    private IDefinitionFileValidatorModalityBased<Swagger> definitionFileValidator;
    @Mock
    private SecurizableApiManagerCommonService securizableApiManagerCommonService;
    @Mock
    private Utils utils;
    @Mock
    private ApiUtils apiUtils;
    @Mock
    private SwaggerConverter swaggerConverter;
    @InjectMocks
    private SyncApiManagerServiceModalityBasedImpl syncApiManagerServiceModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                apiRepository,
                apiVersionRepository,
                novaActivityEmitter,
                apiManagerValidator,
                apiGatewayManagerClient,
                todoTaskClient,
                novaContext,
                definitionFileValidator,
                securizableApiManagerCommonService,
                utils,
                apiUtils,
                swaggerConverter
        );
    }

    @Nested
    class UploadApi
    {
        Integer productId = 909;
        String uuaa = "XXXX";
        String fileContent = "fileContent";
        String swaggerBasePath = "/basePath";
        String swaggerTitle = "title";
        String swaggerVersion = "1.0.0";
        String swaggerDescription = "description";
        String swaggerBusinessUnit = "JGMV";
        String basePathXmas = "/JGMV/title/1.0.0";

        @Test
        @DisplayName(value = "Upload API -> product does not exist error")
        public void productNotExists()
        {
            var apiUploadRequest = this.buildApiUploadRequest();
            when(apiManagerValidator.checkProductExistence(anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
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
                    () -> syncApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> Swagger validation error")
        public void swaggerValidationError(ApiType apiType) throws DefinitionFileException
        {
            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenThrow(new DefinitionFileException(List.of("Error")));

            ApiErrorList ret = syncApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId);

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

            Map<String, Object> swaggerCustomProperties = new HashMap<>();
            swaggerCustomProperties.put("business-unit", swaggerBusinessUnit);
            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setUuaa(uuaa);
            var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);

            when(swagger.getBasePath()).thenReturn(swaggerBasePath);
            when(swagger.getInfo().getTitle()).thenReturn(swaggerTitle);
            when(swagger.getInfo().getVersion()).thenReturn(swaggerVersion);
            when(swagger.getInfo().getDescription()).thenReturn(swaggerDescription);
            when(swagger.getVendorExtensions().get("x-generator-properties")).thenReturn(swaggerCustomProperties);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(swagger);
            //buildAndSave
            when(apiUtils.ensureStartingSlash(any())).then(i -> i.getArgument(0));
            when(apiUtils.buildXmasApiBasePath(any(), any(), any())).thenReturn(basePathXmas);
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiUtils).ensureStartingSlash(swaggerBasePath);
            if (apiType.isExternal())
            {
                verify(apiUtils).buildXmasApiBasePath(swaggerBusinessUnit, swaggerTitle, swaggerVersion);
            }
            else
            {
                verify(apiUtils).buildXmasApiBasePath(uuaa, swaggerTitle, swaggerVersion);
            }
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(any());
        }

        @ParameterizedTest(name = "[{index}] apiType {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> API version already exists error")
        public void apiVersionExistsError(ApiType apiType) throws DefinitionFileException
        {

            Map<String, Object> swaggerCustomProperties = new HashMap<>();
            swaggerCustomProperties.put("business-unit", swaggerBusinessUnit);
            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setUuaa(uuaa);
            var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);

            when(swagger.getBasePath()).thenReturn(swaggerBasePath);
            when(swagger.getInfo().getTitle()).thenReturn(swaggerTitle);
            when(swagger.getInfo().getVersion()).thenReturn(swaggerVersion);
            when(swagger.getInfo().getDescription()).thenReturn(swaggerDescription);
            when(swagger.getVendorExtensions().get("x-generator-properties")).thenReturn(swaggerCustomProperties);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(swagger);
            //buildAndSave
            when(apiUtils.ensureStartingSlash(any())).then(i -> i.getArgument(0));
            when(apiUtils.buildXmasApiBasePath(any(), any(), any())).thenReturn(basePathXmas);
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).then(i -> i.getArgument(0));
            doThrow(NovaException.class).when(apiManagerValidator).assertVersionOfApiNotExists(any(), any());

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiUtils).ensureStartingSlash(swaggerBasePath);
            if (apiType.isExternal())
            {
                verify(apiUtils).buildXmasApiBasePath(swaggerBusinessUnit, swaggerTitle, swaggerVersion);
            }
            else
            {
                verify(apiUtils).buildXmasApiBasePath(uuaa, swaggerTitle, swaggerVersion);
            }
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(any());
            verify(apiManagerValidator).assertVersionOfApiNotExists(any(), eq(swaggerVersion));
        }

        @ParameterizedTest(name = "[{index}] apiType -> {0}")
        @EnumSource(ApiType.class)
        @DisplayName(value = "Upload API -> ok")
        public void ok(ApiType apiType) throws DefinitionFileException, JsonProcessingException
        {
            Integer apiId = 2408;
            String endpointDescription = "endpointDescription";
            String endpointName = "endpointName";
            Map<String, Object> swaggerCustomProperties = new HashMap<>();
            swaggerCustomProperties.put("business-unit", swaggerBusinessUnit);
            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setId(productId);
            product.setUuaa(uuaa);
            var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
            SyncApi api = new SyncApi();
            api.setId(apiId);
            Map<String, Path> paths = Collections.singletonMap(
                    endpointName,
                    new Path().post(new Operation().description(endpointDescription))
            );

            when(swagger.getBasePath()).thenReturn(swaggerBasePath);
            when(swagger.getInfo().getTitle()).thenReturn(swaggerTitle);
            when(swagger.getInfo().getVersion()).thenReturn(swaggerVersion);
            when(swagger.getInfo().getDescription()).thenReturn(swaggerDescription);
            when(swagger.getVendorExtensions().get("x-generator-properties")).thenReturn(swaggerCustomProperties);
            when(swagger.getPaths()).thenReturn(paths);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(swagger);
            //buildAndSave
            when(apiUtils.ensureStartingSlash(any())).then(i -> i.getArgument(0));
            when(apiUtils.buildXmasApiBasePath(any(), any(), any())).thenReturn(basePathXmas);
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).thenReturn(api);

            ApiErrorList ret = syncApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId);

            ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
            ArgumentCaptor<SyncApi> apiArgumentCaptor = ArgumentCaptor.forClass(SyncApi.class);
            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiUtils).ensureStartingSlash(swaggerBasePath);
            if (apiType.isExternal())
            {
                verify(apiUtils).buildXmasApiBasePath(swaggerBusinessUnit, swaggerTitle, swaggerVersion);
            }
            else
            {
                verify(apiUtils).buildXmasApiBasePath(uuaa, swaggerTitle, swaggerVersion);
            }
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(apiArgumentCaptor.capture());
            verify(apiManagerValidator).assertVersionOfApiNotExists(api, swaggerVersion);
            verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());


            assertEquals(0, ret.getErrorList().length);
            SyncApi capturedApi = apiArgumentCaptor.getValue();
            assertEquals(swaggerTitle, capturedApi.getName());
            assertEquals(product, capturedApi.getProduct());
            assertEquals(apiType.isExternal() ? swaggerBusinessUnit : uuaa, capturedApi.getUuaa());
            assertEquals(swaggerBasePath, capturedApi.getBasePathSwagger());
            assertEquals(apiType, capturedApi.getType());

            assertEquals(0, capturedApi.getApiSecurityPolicies().size());
            assertEquals(1, api.getApiVersions().size());
            SyncApiVersion apiVersion = api.getApiVersions().get(0);
            assertEquals(api, apiVersion.getApi());
            assertEquals(swaggerDescription, apiVersion.getDescription());
            assertEquals(swaggerVersion, apiVersion.getVersion());
            assertEquals(fileContent, apiVersion.getDefinitionFile().getContents());
            assertEquals(ApiState.DEFINITION, apiVersion.getApiState());
            assertEquals(1, apiVersion.getApiMethods().size());
            ApiMethod apiMethod = apiVersion.getApiMethods().get(0);
            assertEquals(apiVersion, apiMethod.getApiVersion());
            assertEquals(endpointName, apiMethod.getEndpoint());
            assertEquals(endpointDescription, apiMethod.getDescription());
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
            TaskInfoDto taskInfoDto = this.buildTaskInfoDto();
            SyncApi syncApi = this.buildSyncApi();
            ApiTaskDTO apiTaskDTO = new ApiTaskDTO();
            apiTaskDTO.setStatus(toDoTaskStatus.name());

            when(novaContext.getIvUser()).thenReturn(ivUser);
            when(todoTaskClient.getApiTask(any())).thenReturn(apiTaskDTO);

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerServiceModalityBased.createApiTask(taskInfoDto, syncApi)
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
        @ArgumentsSource(SyncApiManagerServiceModalityBasedImplTest.CreateApiTaskTodoTaskAlreadyCreatedArgumentsSource.class)
        @DisplayName("Create API task -> ok")
        public void ok(ApiType apiType, int todoTaskId) throws JsonProcessingException
        {
            TaskInfoDto taskInfoDto = this.buildTaskInfoDto();
            SyncApi api = this.buildSyncApi();
            api.setType(apiType);
            ApiTaskDTO apiTaskDTO = new ApiTaskDTO();
            apiTaskDTO.fillRandomly(4, false, 1, 4);
            DocSystem msaDocument = new DocSystem();
            DocSystem araDocument = new DocSystem();
            msaDocument.setId(1);
            msaDocument.setUrl("msa");
            araDocument.setId(2);
            araDocument.setUrl("ara");

            when(novaContext.getIvUser()).thenReturn(ivUser);
            when(todoTaskClient.getApiTask(any())).thenReturn(apiTaskDTO);
            when(todoTaskClient.createApiTask(any())).thenReturn(todoTaskId);
            when(apiManagerValidator.validateAndGetDocument(taskInfoDto.getMsaDocumentId(), DocumentCategory.MSA, DocumentType.FILE)).thenReturn(msaDocument);
            when(apiManagerValidator.validateAndGetDocument(taskInfoDto.getAraDocumentId(), DocumentCategory.ARA, DocumentType.FILE)).thenReturn(araDocument);

            syncApiManagerServiceModalityBased.createApiTask(taskInfoDto, api);

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
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>()
                {
                };
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
            TaskInfoDto taskInfoDto = new TaskInfoDto();
            taskInfoDto.fillRandomly(4, false, 1, 3);
            return taskInfoDto;
        }

        private SyncApi buildSyncApi()
        {
            Product product = new Product();
            product.setId(2408);
            SyncApi api = new SyncApi();
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
            SyncApi api = this.buildSyncApi();

            syncApiManagerServiceModalityBased.onPolicyTaskReply(taskStatus, api);
        }

        @Test
        @DisplayName("On policy task reply -> done")
        public void done()
        {
            ToDoTaskStatus taskStatus = ToDoTaskStatus.DONE;
            SyncApi api = this.buildSyncApi();

            syncApiManagerServiceModalityBased.onPolicyTaskReply(taskStatus, api);

            verify(securizableApiManagerCommonService).addPoliciesToApi(api);
            verify(securizableApiManagerCommonService).emitNewPoliciesActivityResponse(taskStatus, api, ActivityAction.POLICIES_REQUEST_DONE);
            assertEquals(ApiPolicyStatus.ESTABLISHED, api.getPolicyStatus());
        }

        @Test
        @DisplayName("On policy task reply -> rejected")
        public void rejected()
        {
            ToDoTaskStatus taskStatus = ToDoTaskStatus.REJECTED;
            SyncApi api = this.buildSyncApi();

            syncApiManagerServiceModalityBased.onPolicyTaskReply(taskStatus, api);

            verify(securizableApiManagerCommonService).emitNewPoliciesActivityResponse(taskStatus, api, ActivityAction.POLICIES_REQUEST_REJECTED);
            assertEquals(ApiPolicyStatus.REJECTED, api.getPolicyStatus());
        }

        private SyncApi buildSyncApi()
        {
            return new SyncApi();
        }
    }

    @Nested
    class RemoveApiRegistration
    {
        @Test
        @DisplayName(value = "Remove API registration -> OK")
        public void ok()
        {
            SyncApi api = new SyncApi();
            api.setName("apiName");
            api.setUuaa("JGMV");
            api.setBasePathSwagger("/basePath");

            syncApiManagerServiceModalityBased.removeApiRegistration(api);

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
            SyncApi api = new SyncApi();
            api.setName("apiName");
            api.setUuaa("JGMV");
            api.setBasePathSwagger("/basePath");

            syncApiManagerServiceModalityBased.deleteApiTodoTasks(api);

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
            SyncApi api = new SyncApi();

            when(apiRepository.findByMsaDocumentId(anyInt())).thenReturn(List.of(api));

            List<SyncApi> ret = syncApiManagerServiceModalityBased.getApisUsingMsaDocument(msaId);

            verify(apiRepository).findByMsaDocumentId(msaId);
            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
        }

        @Test
        @DisplayName("Get APIs using MSA document -> Empty OK")
        public void emptyOk()
        {
            Integer msaId = 909;

            when(apiRepository.findByMsaDocumentId(anyInt())).thenReturn(Collections.emptyList());

            List<SyncApi> ret = syncApiManagerServiceModalityBased.getApisUsingMsaDocument(msaId);

            verify(apiRepository).findByMsaDocumentId(msaId);
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
            SyncApi api = new SyncApi();

            when(apiRepository.findByAraDocumentId(anyInt())).thenReturn(List.of(api));

            List<SyncApi> ret = syncApiManagerServiceModalityBased.getApisUsingAraDocument(msaId);

            verify(apiRepository).findByAraDocumentId(msaId);
            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
        }

        @Test
        @DisplayName("Get APIs using ARA document -> Empty OK")
        public void emptyOk()
        {
            Integer msaId = 909;

            when(apiRepository.findByAraDocumentId(anyInt())).thenReturn(Collections.emptyList());

            List<SyncApi> ret = syncApiManagerServiceModalityBased.getApisUsingAraDocument(msaId);

            verify(apiRepository).findByAraDocumentId(msaId);
            assertEquals(0, ret.size());
        }
    }

    @Nested
    class CreateApiImplementation
    {
        @ParameterizedTest(name = "[{index}] implementedAs -> {0}")
        @EnumSource(ImplementedAs.class)
        @DisplayName("Create API implementation -> OK")
        public void ok(ImplementedAs implementedAs)
        {
            var apiVersion = new SyncApiVersion();
            var releaseVersionService = new ReleaseVersionService();

            SyncApiImplementation ret = syncApiManagerServiceModalityBased.createApiImplementation(
                    apiVersion, releaseVersionService, implementedAs
            );

            assertEquals(apiVersion, ret.getApiVersion());
            assertEquals(releaseVersionService, ret.getService());
            assertEquals(implementedAs, ret.getImplementedAs());
        }

    }

    @Nested
    class SetConsumedApisByServedApi
    {
        @Test
        @DisplayName("Set consumed APIs by server API -> API version in definition state")
        public void definitionApiVersionOk()
        {
            var apiVersion = new SyncApiVersion();
            apiVersion.setApiState(ApiState.DEFINITION);
            var apiImplementation = new SyncApiImplementation();
            Integer apiVersionId = 909;

            when(apiVersionRepository.findById(anyInt())).thenReturn(Optional.of(apiVersion));

            SyncApiImplementation ret = syncApiManagerServiceModalityBased.setConsumedApisByServedApi(apiImplementation, List.of(apiVersionId));

            verify(apiVersionRepository).findById(apiVersionId);
            assertEquals(apiImplementation, ret);
            assertEquals(1, apiImplementation.getConsumedApis().size());
            assertEquals(apiVersion, apiImplementation.getConsumedApis().get(0));
            assertEquals(ApiState.IMPLEMENTED, apiVersion.getApiState());
        }

        @ParameterizedTest(name = "[{index}] apiState -> {0}")
        @EnumSource(value = ApiState.class, names = {"DEPLOYED", "IMPLEMENTED"})
        @DisplayName("Set consumed APIs version by server API -> API version in deployed/implemented state")
        public void definitionApiVersionOk(ApiState apiState)
        {
            var apiVersion = new SyncApiVersion();
            apiVersion.setApiState(apiState);
            var apiImplementation = new SyncApiImplementation();
            Integer apiVersionId = 909;

            when(apiVersionRepository.findById(anyInt())).thenReturn(Optional.of(apiVersion));

            SyncApiImplementation ret = syncApiManagerServiceModalityBased.setConsumedApisByServedApi(apiImplementation, List.of(apiVersionId));

            verify(apiVersionRepository).findById(apiVersionId);
            assertEquals(apiImplementation, ret);
            assertEquals(1, apiImplementation.getConsumedApis().size());
            assertEquals(apiVersion, apiImplementation.getConsumedApis().get(0));
            assertEquals(apiState, apiVersion.getApiState());
        }

    }

    @Nested
    class SetBackwardCompatibleVersionsOfServedApi
    {
        String version = "1.0.0";
        String apiName = "apiName";
        String uuaa = "JGMV";

        @Test
        @DisplayName("Set backward compatible API versions by server API -> API version already inserted as backward compatible")
        public void backwardVersionAlreadyInserted()
        {
            var backwardApiVersionAlreadyInserted = new SyncApiVersion();
            backwardApiVersionAlreadyInserted.setId(1);
            backwardApiVersionAlreadyInserted.setApiState(ApiState.values()[RandomUtils.nextInt(0, ApiState.values().length)]);
            var apiImplementation = Mockito.mock(SyncApiImplementation.class, RETURNS_DEEP_STUBS);
            var backwardVersions = new ArrayList<SyncApiVersion>();
            backwardVersions.add(backwardApiVersionAlreadyInserted);

            when(apiImplementation.getApiVersion().getApi().getName()).thenReturn(apiName);
            when(apiImplementation.getApiVersion().getApi().getUuaa()).thenReturn(uuaa);
            when(apiImplementation.getBackwardCompatibleApis()).thenReturn(backwardVersions);

            when(apiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(any(), any(), any())).thenReturn(backwardApiVersionAlreadyInserted);

            syncApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImplementation, List.of(version));

            verify(apiVersionRepository).findByApiNameAndVersionAndUuaaAndExternalFalse(apiName, version, uuaa);
            assertEquals(1, backwardVersions.size());
            assertEquals(backwardApiVersionAlreadyInserted, backwardVersions.get(0));
        }

        @Test
        @DisplayName("Set backward compatible API versions by server API -> API version already inserted as backward compatible in definition state")
        public void backwardVersionAlreadyInsertedInDefinitionState()
        {
            var backwardApiVersionAlreadyInserted = new SyncApiVersion();
            backwardApiVersionAlreadyInserted.setId(1);
            backwardApiVersionAlreadyInserted.setApiState(ApiState.DEFINITION);
            var apiImplementation = Mockito.mock(SyncApiImplementation.class, RETURNS_DEEP_STUBS);
            var backwardVersions = new ArrayList<SyncApiVersion>();
            backwardVersions.add(backwardApiVersionAlreadyInserted);

            when(apiImplementation.getApiVersion().getApi().getName()).thenReturn(apiName);
            when(apiImplementation.getApiVersion().getApi().getUuaa()).thenReturn(uuaa);
            when(apiImplementation.getBackwardCompatibleApis()).thenReturn(backwardVersions);

            when(apiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(any(), any(), any())).thenReturn(backwardApiVersionAlreadyInserted);

            syncApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImplementation, List.of(version));

            verify(apiVersionRepository).findByApiNameAndVersionAndUuaaAndExternalFalse(apiName, version, uuaa);
            assertEquals(1, backwardVersions.size());
            assertEquals(backwardApiVersionAlreadyInserted, backwardVersions.get(0));
            assertEquals(ApiState.IMPLEMENTED, backwardApiVersionAlreadyInserted.getApiState());
        }

        @Test
        @DisplayName("Set backward compatible API versions by server API -> API version yet not inserted as backward compatible")
        public void backwardVersionNotInserted()
        {
            var backwardApiVersionInserted = new SyncApiVersion();
            backwardApiVersionInserted.setId(1);
            backwardApiVersionInserted.setApiState(ApiState.values()[RandomUtils.nextInt(0, ApiState.values().length)]);
            var newBackwardApiVersion = new SyncApiVersion();
            newBackwardApiVersion.setId(2);
            newBackwardApiVersion.setApiState(ApiState.values()[RandomUtils.nextInt(0, ApiState.values().length)]);
            var apiImplementation = Mockito.mock(SyncApiImplementation.class, RETURNS_DEEP_STUBS);
            var backwardVersions = new ArrayList<SyncApiVersion>();
            backwardVersions.add(backwardApiVersionInserted);

            when(apiImplementation.getApiVersion().getApi().getName()).thenReturn(apiName);
            when(apiImplementation.getApiVersion().getApi().getUuaa()).thenReturn(uuaa);
            when(apiImplementation.getBackwardCompatibleApis()).thenReturn(backwardVersions);

            when(apiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(any(), any(), any())).thenReturn(newBackwardApiVersion);

            syncApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImplementation, List.of(version));

            verify(apiVersionRepository).findByApiNameAndVersionAndUuaaAndExternalFalse(apiName, version, uuaa);
            assertEquals(2, backwardVersions.size());
        }

        @Test
        @DisplayName("Set backward compatible API versions by server API -> API version in definition state")
        public void definitionApiVersionOk()
        {
            var apiVersion = new SyncApiVersion();
            apiVersion.setApiState(ApiState.DEFINITION);
            var apiImplementation = Mockito.mock(SyncApiImplementation.class, RETURNS_DEEP_STUBS);
            var backwardVersions = new ArrayList<SyncApiVersion>();

            when(apiImplementation.getApiVersion().getApi().getName()).thenReturn(apiName);
            when(apiImplementation.getApiVersion().getApi().getUuaa()).thenReturn(uuaa);
            when(apiImplementation.getBackwardCompatibleApis()).thenReturn(backwardVersions);

            when(apiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(any(), any(), any())).thenReturn(apiVersion);

            SyncApiImplementation ret = syncApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImplementation, List.of(version));

            verify(apiVersionRepository).findByApiNameAndVersionAndUuaaAndExternalFalse(apiName, version, uuaa);
            assertEquals(apiImplementation, ret);
            assertEquals(1, backwardVersions.size());
            assertEquals(apiVersion, backwardVersions.get(0));
            assertEquals(ApiState.IMPLEMENTED, apiVersion.getApiState());
        }

        @ParameterizedTest(name = "[{index}] apiState -> {0}")
        @EnumSource(value = ApiState.class, names = {"DEPLOYED", "IMPLEMENTED"})
        @DisplayName("Set backward compatible API versions by server API -> API version in deployed/implemented state")
        public void deployedApiVersionOk(ApiState apiState)
        {
            var apiVersion = new SyncApiVersion();
            apiVersion.setApiState(apiState);
            var apiImplementation = Mockito.mock(SyncApiImplementation.class, RETURNS_DEEP_STUBS);
            var backwardVersions = new ArrayList<SyncApiVersion>();

            when(apiImplementation.getApiVersion().getApi().getName()).thenReturn(apiName);
            when(apiImplementation.getApiVersion().getApi().getUuaa()).thenReturn(uuaa);
            when(apiImplementation.getBackwardCompatibleApis()).thenReturn(backwardVersions);

            when(apiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(any(), any(), any())).thenReturn(apiVersion);

            SyncApiImplementation ret = syncApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImplementation, List.of(version));

            verify(apiVersionRepository).findByApiNameAndVersionAndUuaaAndExternalFalse(apiName, version, uuaa);
            assertEquals(apiImplementation, ret);
            assertEquals(1, backwardVersions.size());
            assertEquals(apiVersion, backwardVersions.get(0));
            assertEquals(apiState, apiVersion.getApiState());
        }
    }

    @Nested
    class DownloadProductApi
    {
        String rawContent = "rawContent";
        byte[] byteContent = new byte[]{3, 4, 5};
        String basePathXmas = "/uuaa/apiName/apiVersion";
        String basePath = "/basePath";

        @ParameterizedTest
        @EmptySource
        @ValueSource(strings = {"XXX"})
        @DisplayName("Download product API -> invalid format")
        public void invalidFormat(String format)
        {
            var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var swagger = new Swagger();

            when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
            when(apiVersion.getApi().getType()).thenReturn(ApiType.GOVERNED);

            when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, "other")
            );

            verify(swaggerConverter).parseSwaggerFromString(rawContent);
        }

        @Nested
        class YamlFormat
        {
            @ParameterizedTest
            @ValueSource(strings = {"YML", "YAML"})
            @DisplayName("Download product API -> yaml format for client (consumed download type)")
            public void consumedOK(String format)
            {
                var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
                var swagger = new Swagger();

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
                when(apiVersion.getBasePathXmas()).thenReturn(basePathXmas);
                when(apiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                when(swaggerConverter.swaggerYamlToByteArray(any())).thenReturn(byteContent);

                byte[] ret = syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, Constants.CONSUMED);

                verify(swaggerConverter).parseSwaggerFromString(rawContent);
                verify(swaggerConverter).swaggerYamlToByteArray(swagger);
                assertEquals(basePathXmas + basePath, swagger.getBasePath());
                assertEquals(byteContent, ret);
            }

            @ParameterizedTest
            @ValueSource(strings = {"YML", "YAML"})
            @DisplayName("Download product API -> yaml format for client (external API)")
            public void externalOK(String format)
            {
                var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
                var swagger = new Swagger();

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
                when(apiVersion.getBasePathXmas()).thenReturn(basePathXmas);
                when(apiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);
                when(apiVersion.getApi().getType()).thenReturn(ApiType.EXTERNAL);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                when(swaggerConverter.swaggerYamlToByteArray(any())).thenReturn(byteContent);

                byte[] ret = syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, "other");

                verify(swaggerConverter).parseSwaggerFromString(rawContent);
                verify(swaggerConverter).swaggerYamlToByteArray(swagger);
                assertEquals(basePathXmas + basePath, swagger.getBasePath());
                assertEquals(byteContent, ret);
            }

            @ParameterizedTest
            @ValueSource(strings = {"YML", "YAML"})
            @DisplayName("Download product API -> yaml format for server")
            public void forServerOK(String format)
            {
                var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
                var swagger = new Swagger();
                swagger.setBasePath(basePath);

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
                when(apiVersion.getApi().getType()).thenReturn(ApiType.GOVERNED);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                when(swaggerConverter.swaggerYamlToByteArray(any())).thenReturn(byteContent);

                byte[] ret = syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, "other");

                verify(swaggerConverter).parseSwaggerFromString(rawContent);
                verify(swaggerConverter).swaggerYamlToByteArray(swagger);
                assertEquals(basePath, swagger.getBasePath());
                assertEquals(byteContent, ret);
            }

        }

        @Nested
        class JsonFormat
        {
            @ParameterizedTest
            @ValueSource(strings = {"JSON"})
            @DisplayName("Download product API -> json format for client (consumed download type)")
            public void consumedOK(String format)
            {
                var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
                var swagger = new Swagger();

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
                when(apiVersion.getBasePathXmas()).thenReturn(basePathXmas);
                when(apiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                when(swaggerConverter.swaggerJsonToByteArray(any())).thenReturn(byteContent);

                byte[] ret = syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, Constants.CONSUMED);

                verify(swaggerConverter).parseSwaggerFromString(rawContent);
                verify(swaggerConverter).swaggerJsonToByteArray(swagger);
                assertEquals(basePathXmas + basePath, swagger.getBasePath());
                assertEquals(byteContent, ret);
            }

            @ParameterizedTest
            @ValueSource(strings = {"JSON"})
            @DisplayName("Download product API -> json format for client (external API)")
            public void externalOK(String format)
            {
                var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
                var swagger = new Swagger();

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
                when(apiVersion.getBasePathXmas()).thenReturn(basePathXmas);
                when(apiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);
                when(apiVersion.getApi().getType()).thenReturn(ApiType.EXTERNAL);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                when(swaggerConverter.swaggerJsonToByteArray(any())).thenReturn(byteContent);

                byte[] ret = syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, "other");

                verify(swaggerConverter).parseSwaggerFromString(rawContent);
                verify(swaggerConverter).swaggerJsonToByteArray(swagger);
                assertEquals(basePathXmas + basePath, swagger.getBasePath());
                assertEquals(byteContent, ret);
            }

            @ParameterizedTest
            @ValueSource(strings = {"JSON"})
            @DisplayName("Download product API -> json format for server")
            public void forServerOK(String format)
            {
                var apiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
                var swagger = new Swagger();
                swagger.setBasePath(basePath);

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(rawContent);
                when(apiVersion.getApi().getType()).thenReturn(ApiType.GOVERNED);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                when(swaggerConverter.swaggerJsonToByteArray(any())).thenReturn(byteContent);

                byte[] ret = syncApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, "other");

                verify(swaggerConverter).parseSwaggerFromString(rawContent);
                verify(swaggerConverter).swaggerJsonToByteArray(swagger);
                assertEquals(basePath, swagger.getBasePath());
                assertEquals(byteContent, ret);
            }

        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> SYNC")
        public void sync()
        {
            assertTrue(syncApiManagerServiceModalityBased.isModalitySupported(ApiModality.SYNC));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"ASYNC_BACKTOBACK", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> ASYNC")
        public void async(ApiModality modality)
        {
            assertFalse(syncApiManagerServiceModalityBased.isModalitySupported(modality));
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
