package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiChannel;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.*;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.ApiUtils;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IDefinitionFileValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class AsyncBackToBackApiManagerServiceModalityBasedImplTest
{
    @Mock
    private IApiManagerValidator apiManagerValidator;
    @Mock
    private IDefinitionFileValidatorModalityBased<NovaAsyncAPI> definitionFileValidator;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private Utils utils;
    @Mock
    private ApiUtils apiUtils;
    @InjectMocks
    private AsyncBackToBackApiManagerServiceModalityBasedImpl asyncBackToBackApiManagerServiceModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                apiManagerValidator,
                definitionFileValidator,
                novaActivityEmitter,
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
        String md5 = "34j3i4hiuh53";

        @Test
        @DisplayName(value = "Upload API -> product does not exist error")
        public void productNotExists()
        {
            var apiUploadRequest = this.buildApiUploadRequest();
            when(apiManagerValidator.checkProductExistence(anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToBackApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
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
                    () -> asyncBackToBackApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
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

            ApiErrorList ret = asyncBackToBackApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId);

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
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToBackApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
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
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).then(i -> i.getArgument(0));
            doThrow(NovaException.class).when(apiManagerValidator).assertVersionOfApiNotExists(any(), any());

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToBackApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId)
            );

            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(any());
            verify(apiManagerValidator).assertVersionOfApiNotExists(any(), eq(asyncVersion));
        }

        @ParameterizedTest(name = "[{index}] apiType: {0}, channelItem: {1}")
        @ArgumentsSource(UploadApiOKArgumentsProvider.class)
        @DisplayName(value = "Upload API -> ok")
        public void ok(ApiType apiType, ChannelItem channelItem) throws DefinitionFileException, JsonProcessingException
        {

            var apiUploadRequest = this.buildApiUploadRequest();
            apiUploadRequest.setApiType(apiType.getApiType());
            var product = new Product();
            product.setUuaa(uuaa);
            product.setId(productId);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var api = new AsyncBackToBackApi();
            var channelName = "channelName";
            var channels = new HashMap<String, ChannelItem>();
            channels.put(channelName, channelItem);

            when(novaAsyncAPI.getAsyncAPI().getInfo().getTitle()).thenReturn(asyncTitle);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getVersion()).thenReturn(asyncVersion);
            when(novaAsyncAPI.getAsyncAPI().getInfo().getDescription()).thenReturn(asyncDescription);
            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(asyncBusinessUnit);
            when(novaAsyncAPI.getAsyncAPI().getChannels()).thenReturn(channels);

            when(apiManagerValidator.checkProductExistence(productId)).thenReturn(product);
            when(apiUtils.decodeBase64(any())).thenReturn(fileContent);
            when(definitionFileValidator.parseAndValidate(any(), any(), any())).thenReturn(novaAsyncAPI);
            //buildAndSave
            when(apiManagerValidator.findAndValidateOrPersistIfMissing(any())).thenReturn(api);
            when(utils.unifyCRLF2LF(any())).then(i -> i.getArgument(0));
            when(utils.calculeMd5Hash(any())).thenReturn(md5);


            ApiErrorList ret = asyncBackToBackApiManagerServiceModalityBased.uploadApi(apiUploadRequest, productId);

            var genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
            var apiArgumentCaptor = ArgumentCaptor.forClass(AsyncBackToBackApi.class);
            verify(apiManagerValidator).checkProductExistence(productId);
            verify(apiUtils).decodeBase64(apiUploadRequest.getFile());
            verify(definitionFileValidator).parseAndValidate(fileContent, product, apiType);
            //buildAndSave
            verify(apiManagerValidator).findAndValidateOrPersistIfMissing(apiArgumentCaptor.capture());
            verify(apiManagerValidator).assertVersionOfApiNotExists(api, asyncVersion);
            verify(utils).unifyCRLF2LF(fileContent);
            verify(utils).calculeMd5Hash(fileContent.getBytes());
            verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());

            assertEquals(0, ret.getErrorList().length);

            AsyncBackToBackApi capturedApi = apiArgumentCaptor.getValue();
            assertEquals(asyncTitle, capturedApi.getName());
            assertEquals(product, capturedApi.getProduct());
            assertEquals(apiType.isExternal() ? asyncBusinessUnit : uuaa, capturedApi.getUuaa());
            assertEquals(apiType, capturedApi.getType());

            assertEquals(1, api.getApiVersions().size());
            AsyncBackToBackApiVersion apiVersion = api.getApiVersions().get(0);
            assertEquals(api, apiVersion.getApi());
            assertEquals(asyncDescription, apiVersion.getDescription());
            assertEquals(asyncVersion, apiVersion.getVersion());
            assertEquals(fileContent, apiVersion.getDefinitionFile().getContents());
            assertEquals(ApiState.DEFINITION, apiVersion.getApiState());
            assertEquals(md5, apiVersion.getDefinitionFileHash());
            AsyncBackToBackApiChannel asyncBackToBackApiChannel = apiVersion.getAsyncBackToBackApiChannel();
            assertEquals(channelName, asyncBackToBackApiChannel.getChannelName());
            if(channelItem.getPublish() != null)
            {
                assertEquals(AsyncBackToBackChannelType.PUBLISH, asyncBackToBackApiChannel.getChannelType());
                assertEquals(channelItem.getPublish().getOperationId(), asyncBackToBackApiChannel.getOperationId());
            }
            else
            {
                assertEquals(AsyncBackToBackChannelType.SUBSCRIBE, asyncBackToBackApiChannel.getChannelType());
                assertEquals(channelItem.getSubscribe().getOperationId(), asyncBackToBackApiChannel.getOperationId());
            }

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

    static class UploadApiOKArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            ChannelItem publisher = ChannelItem.builder().publish(Operation.builder().operationId("operationId").build()).build();
            ChannelItem subscriber = ChannelItem.builder().subscribe(Operation.builder().operationId("operationId").build()).build();
            return Stream.of(
                    Arguments.of(ApiType.GOVERNED, publisher),
                    Arguments.of(ApiType.NOT_GOVERNED, publisher),
                    Arguments.of(ApiType.EXTERNAL, publisher),
                    Arguments.of(ApiType.GOVERNED, subscriber),
                    Arguments.of(ApiType.NOT_GOVERNED, subscriber),
                    Arguments.of(ApiType.EXTERNAL, subscriber)
            );
        }
    }

    @Nested
    class CreateApiTask
    {
        @Test
        @DisplayName("Create API task -> nothing to do")
        public void doNothing()
        {
            asyncBackToBackApiManagerServiceModalityBased.createApiTask(null, null);
        }
    }

    @Nested
    class OnPolicyTaskReply
    {
        @Test
        @DisplayName("On policy task reply -> nothing to do")
        public void doNothing()
        {
            asyncBackToBackApiManagerServiceModalityBased.onPolicyTaskReply(null, null);
        }
    }

    @Nested
    class DeleteApiTodoTasks
    {
        @Test
        @DisplayName("Delete API s todo tasks -> nothing to do")
        public void doNothing()
        {
            asyncBackToBackApiManagerServiceModalityBased.deleteApiTodoTasks(null);
        }
    }

    @Nested
    class RemoveApiRegistration
    {
        @Test
        @DisplayName("Remove API registration -> nothing to do")
        public void doNothing()
        {
            asyncBackToBackApiManagerServiceModalityBased.removeApiRegistration(null);
        }
    }

    @Nested
    class GetApisUsingMsaDocument
    {
        @Test
        @DisplayName("Get APIs using MSA document -> Empty list")
        public void empytList()
        {
            List<AsyncBackToBackApi> ret = asyncBackToBackApiManagerServiceModalityBased.getApisUsingMsaDocument(909);

            assertEquals(0, ret.size());
        }
    }

    @Nested
    class GetApisUsingAraDocument
    {
        @Test
        @DisplayName("Get APIs using ARA document -> Empty list")
        public void empytList()
        {
            List<AsyncBackToBackApi> ret = asyncBackToBackApiManagerServiceModalityBased.getApisUsingAraDocument(909);

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
            var apiVersion = Mockito.mock(AsyncBackToBackApiVersion.class);
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            AsyncBackToBackApiImplementation ret = asyncBackToBackApiManagerServiceModalityBased.createApiImplementation(apiVersion, releaseVersionService, implementedAs);

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
            var servedApi = Mockito.mock(AsyncBackToBackApiImplementation.class);

            asyncBackToBackApiManagerServiceModalityBased.setConsumedApisByServedApi(servedApi, consumedApis);

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
            var servedApi = Mockito.mock(AsyncBackToBackApiImplementation.class);

            asyncBackToBackApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(servedApi, backwardCompatibleVersions);

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
            var apiVersion = new AsyncBackToBackApiVersion();

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToBackApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, "other")
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
                var apiVersion = Mockito.mock(AsyncBackToBackApiVersion.class, Mockito.RETURNS_DEEP_STUBS);

                when(apiVersion.getDefinitionFile().getContents()).thenReturn(content);

                byte[] ret = asyncBackToBackApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, downloadType);

                assertArrayEquals(content.getBytes(), ret);
            }
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOBACK")
        public void backToBacck()
        {
            assertTrue(asyncBackToBackApiManagerServiceModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOBACK));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToBackApiManagerServiceModalityBased.isModalitySupported(modality));
        }
    }
}
