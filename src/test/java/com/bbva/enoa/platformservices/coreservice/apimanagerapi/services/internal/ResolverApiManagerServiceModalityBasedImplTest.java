package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeast;

public class ResolverApiManagerServiceModalityBasedImplTest
{
    private IApiManagerServiceModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation> asyncBackToBackApiManagerServiceModalityBased;
    private IApiManagerServiceModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation> asyncBackToFrontApiManagerServiceModalityBased;
    private IApiManagerServiceModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation> syncApiManagerServiceModalityBased;

    private ResolverApiManagerServiceModalityBasedImpl resolverApiManagerServiceModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        asyncBackToBackApiManagerServiceModalityBased = Mockito.mock(AsyncBackToBackApiManagerServiceModalityBasedImpl.class);
        asyncBackToFrontApiManagerServiceModalityBased = Mockito.mock(AsyncBackToFrontApiManagerServiceModalityBasedImpl.class);
        syncApiManagerServiceModalityBased = Mockito.mock(SyncApiManagerServiceModalityBasedImpl.class);

        when(asyncBackToBackApiManagerServiceModalityBased.isModalitySupported(any())).thenCallRealMethod();
        when(asyncBackToFrontApiManagerServiceModalityBased.isModalitySupported(any())).thenCallRealMethod();
        when(syncApiManagerServiceModalityBased.isModalitySupported(any())).thenCallRealMethod();

        resolverApiManagerServiceModalityBased = new ResolverApiManagerServiceModalityBasedImpl(
                List.of(
                        asyncBackToBackApiManagerServiceModalityBased,
                        asyncBackToFrontApiManagerServiceModalityBased,
                        syncApiManagerServiceModalityBased
                )
        );

        Mockito.reset(
                asyncBackToBackApiManagerServiceModalityBased,
                asyncBackToFrontApiManagerServiceModalityBased,
                syncApiManagerServiceModalityBased
        );
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                asyncBackToBackApiManagerServiceModalityBased,
                asyncBackToFrontApiManagerServiceModalityBased,
                syncApiManagerServiceModalityBased
        );
    }

    @Nested
    class Constructor
    {
        @Test
        @DisplayName("ResolverDtoBuilderModalityBasedImpl is instantiated successfully")
        public void constructorOk()
        {
            // Given
            final int numberOfModalities = ApiModality.values().length;

            // When
            resolverApiManagerServiceModalityBased = new ResolverApiManagerServiceModalityBasedImpl(
                    List.of(
                            asyncBackToBackApiManagerServiceModalityBased,
                            asyncBackToFrontApiManagerServiceModalityBased,
                            syncApiManagerServiceModalityBased
                    )
            );

            // Then
            verify(syncApiManagerServiceModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
            verify(asyncBackToFrontApiManagerServiceModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
            verify(asyncBackToBackApiManagerServiceModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
        }
    }

    @Nested
    class UploadApi
    {
        private final Integer productId = 909;

        @Test
        @DisplayName("Upload API -> Sync version OK")
        public void syncApiVersionOk()
        {
            var apiUploadRequestDto = Mockito.mock(ApiUploadRequestDto.class);
            var apiErrorList = new ApiErrorList();

            when(apiUploadRequestDto.getApiModality()).thenReturn(ApiModality.SYNC.getModality());
            when(syncApiManagerServiceModalityBased.uploadApi(any(), anyInt())).thenReturn(apiErrorList);

            final ApiErrorList ret = resolverApiManagerServiceModalityBased.uploadApi(apiUploadRequestDto, productId);

            verify(syncApiManagerServiceModalityBased).uploadApi(apiUploadRequestDto, productId);

            assertEquals(apiErrorList, ret);
        }

        @Test
        @DisplayName("Upload API -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var apiUploadRequestDto = Mockito.mock(ApiUploadRequestDto.class);
            var apiErrorList = new ApiErrorList();

            when(apiUploadRequestDto.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK.getModality());
            when(asyncBackToBackApiManagerServiceModalityBased.uploadApi(any(), anyInt())).thenReturn(apiErrorList);

            final ApiErrorList ret = resolverApiManagerServiceModalityBased.uploadApi(apiUploadRequestDto, productId);

            verify(asyncBackToBackApiManagerServiceModalityBased).uploadApi(apiUploadRequestDto, productId);

            assertEquals(apiErrorList, ret);
        }

        @Test
        @DisplayName("Upload API -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var apiUploadRequestDto = Mockito.mock(ApiUploadRequestDto.class);
            var apiErrorList = new ApiErrorList();

            when(apiUploadRequestDto.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT.getModality());
            when(asyncBackToFrontApiManagerServiceModalityBased.uploadApi(any(), anyInt())).thenReturn(apiErrorList);

            final ApiErrorList ret = resolverApiManagerServiceModalityBased.uploadApi(apiUploadRequestDto, productId);

            verify(asyncBackToFrontApiManagerServiceModalityBased).uploadApi(apiUploadRequestDto, productId);

            assertEquals(apiErrorList, ret);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Upload API -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var apiUploadRequestDto = Mockito.mock(ApiUploadRequestDto.class);

            when(apiUploadRequestDto.getApiModality()).thenReturn(modality.getModality());

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .uploadApi(apiUploadRequestDto, productId)
            );
        }
    }

    @Nested
    class CreateApiTask
    {
        @Test
        @DisplayName("Create API task -> Sync version OK")
        public void syncApiVersionOk()
        {
            var taskInfoDto = Mockito.mock(TaskInfoDto.class);
            var api = Mockito.mock(SyncApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.SYNC);

            resolverApiManagerServiceModalityBased.createApiTask(taskInfoDto, api);

            verify(syncApiManagerServiceModalityBased).createApiTask(taskInfoDto, api);
        }

        @Test
        @DisplayName("Create API task -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var taskInfoDto = Mockito.mock(TaskInfoDto.class);
            var api = Mockito.mock(AsyncBackToBackApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            resolverApiManagerServiceModalityBased.createApiTask(taskInfoDto, api);

            verify(asyncBackToBackApiManagerServiceModalityBased).createApiTask(taskInfoDto, api);
        }

        @Test
        @DisplayName("Create API task -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var taskInfoDto = Mockito.mock(TaskInfoDto.class);
            var api = Mockito.mock(AsyncBackToFrontApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            resolverApiManagerServiceModalityBased.createApiTask(taskInfoDto, api);

            verify(asyncBackToFrontApiManagerServiceModalityBased).createApiTask(taskInfoDto, api);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Create API task -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var taskInfoDto = Mockito.mock(TaskInfoDto.class);
            var api = Mockito.mock(Api.class);

            when(api.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .createApiTask(taskInfoDto, api)
            );
        }
    }

    @Nested
    class OnPolicyTaskReply
    {
        @ParameterizedTest
        @EnumSource(ToDoTaskStatus.class)
        @DisplayName("On policy task reply -> Sync version OK")
        public void syncApiVersionOk(ToDoTaskStatus taskStatus)
        {
            var api = Mockito.mock(SyncApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.SYNC);

            resolverApiManagerServiceModalityBased.onPolicyTaskReply(taskStatus, api);

            verify(syncApiManagerServiceModalityBased).onPolicyTaskReply(taskStatus, api);
        }

        @ParameterizedTest
        @EnumSource(ToDoTaskStatus.class)
        @DisplayName("On policy task reply -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk(ToDoTaskStatus taskStatus)
        {
            var api = Mockito.mock(AsyncBackToBackApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            resolverApiManagerServiceModalityBased.onPolicyTaskReply(taskStatus, api);

            verify(asyncBackToBackApiManagerServiceModalityBased).onPolicyTaskReply(taskStatus, api);
        }

        @ParameterizedTest
        @EnumSource(ToDoTaskStatus.class)
        @DisplayName("On policy task reply -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk(ToDoTaskStatus taskStatus)
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            resolverApiManagerServiceModalityBased.onPolicyTaskReply(taskStatus, api);

            verify(asyncBackToFrontApiManagerServiceModalityBased).onPolicyTaskReply(taskStatus, api);
        }

        @ParameterizedTest
        @ArgumentsSource(OnPolicyTaskReplyArgumentsProvider.class)
        @DisplayName("On policy task reply -> Modality not implemented")
        public void buildMissingModality(ApiModality modality, ToDoTaskStatus taskStatus)
        {
            var api = Mockito.mock(Api.class);

            when(api.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .onPolicyTaskReply(taskStatus, api)
            );
        }
    }

    static class OnPolicyTaskReplyArgumentsProvider implements ArgumentsProvider
    {

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Stream.of(
                    Arguments.of(ApiModality.SYNC, ToDoTaskStatus.REJECTED),
                    Arguments.of(ApiModality.SYNC, ToDoTaskStatus.DONE),
                    Arguments.of(ApiModality.SYNC, ToDoTaskStatus.PENDING_ERROR),
                    Arguments.of(ApiModality.SYNC, ToDoTaskStatus.PENDING),
                    Arguments.of(ApiModality.SYNC, ToDoTaskStatus.ERROR),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ToDoTaskStatus.REJECTED),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ToDoTaskStatus.DONE),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ToDoTaskStatus.PENDING_ERROR),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ToDoTaskStatus.PENDING),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ToDoTaskStatus.ERROR),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ToDoTaskStatus.REJECTED),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ToDoTaskStatus.DONE),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ToDoTaskStatus.PENDING_ERROR),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ToDoTaskStatus.PENDING),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ToDoTaskStatus.ERROR)
            );
        }
    }

    @Nested
    class RemoveApiRegistration
    {
        @Test
        @DisplayName("Remove API registration -> Sync version OK")
        public void syncApiVersionOk()
        {
            var api = Mockito.mock(SyncApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.SYNC);

            resolverApiManagerServiceModalityBased.removeApiRegistration(api);

            verify(syncApiManagerServiceModalityBased).removeApiRegistration(api);
        }

        @Test
        @DisplayName("Remove API registration -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var api = Mockito.mock(AsyncBackToBackApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            resolverApiManagerServiceModalityBased.removeApiRegistration(api);

            verify(asyncBackToBackApiManagerServiceModalityBased).removeApiRegistration(api);
        }

        @Test
        @DisplayName("Remove API registration -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            resolverApiManagerServiceModalityBased.removeApiRegistration(api);

            verify(asyncBackToFrontApiManagerServiceModalityBased).removeApiRegistration(api);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Remove API registration -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var api = Mockito.mock(Api.class);

            when(api.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .removeApiRegistration(api)
            );
        }
    }

    @Nested
    class DeleteApiTodoTasks
    {
        @Test
        @DisplayName("Delete API todo task -> Sync version OK")
        public void syncApiVersionOk()
        {
            var api = Mockito.mock(SyncApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.SYNC);

            resolverApiManagerServiceModalityBased.deleteApiTodoTasks(api);

            verify(syncApiManagerServiceModalityBased).deleteApiTodoTasks(api);
        }

        @Test
        @DisplayName("Delete API todo task -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var api = Mockito.mock(AsyncBackToBackApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            resolverApiManagerServiceModalityBased.deleteApiTodoTasks(api);

            verify(asyncBackToBackApiManagerServiceModalityBased).deleteApiTodoTasks(api);
        }

        @Test
        @DisplayName("Delete API todo task -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class);

            when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            resolverApiManagerServiceModalityBased.deleteApiTodoTasks(api);

            verify(asyncBackToFrontApiManagerServiceModalityBased).deleteApiTodoTasks(api);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Delete API todo task -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var api = Mockito.mock(Api.class);

            when(api.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .deleteApiTodoTasks(api)
            );
        }
    }

    @Nested
    class GetApisUsingMsaDocument
    {
        Integer msaDocumentId = 909;

        @Test
        @DisplayName("Get APIs using MSA document -> Sync version")
        public void syncApiVersion()
        {
            var api = Mockito.mock(SyncApi.class);

            when(syncApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(List.of(api));
            when(asyncBackToBackApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToFrontApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());

            List<Api<?,?,?>> ret = resolverApiManagerServiceModalityBased.getApisUsingMsaDocument(msaDocumentId);

            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
            verify(syncApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            verify(asyncBackToBackApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            verify(asyncBackToFrontApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
        }

        @Test
        @DisplayName("Get APIs using MSA document -> Async back to back version")
        public void asyncBackToBackApiVersion()
        {
            var api = Mockito.mock(AsyncBackToBackApi.class);

            when(syncApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToBackApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(List.of(api));
            when(asyncBackToFrontApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());

            List<Api<?,?,?>> ret = resolverApiManagerServiceModalityBased.getApisUsingMsaDocument(msaDocumentId);

            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
            verify(syncApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            verify(asyncBackToBackApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            verify(asyncBackToFrontApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
        }

        @Test
        @DisplayName("Get APIs using MSA document -> Async back to front version")
        public void asyncBackToFrontApiVersion()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class);

            when(syncApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToBackApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToFrontApiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(List.of(api));

            List<Api<?,?,?>> ret = resolverApiManagerServiceModalityBased.getApisUsingMsaDocument(msaDocumentId);

            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
            verify(syncApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            verify(asyncBackToBackApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            verify(asyncBackToFrontApiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
        }

        @Test
        @DisplayName("Get APIs using MSA document -> Modality not implemented")
        public void buildMissingModality()
        {
            List<Api<?,?,?>> ret = new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                                        .getApisUsingMsaDocument(msaDocumentId);

            assertEquals(0, ret.size());
        }
    }

    @Nested
    class GetApisUsingAraDocument
    {
        private final Integer araDocumentId = 909;

        @Test
        @DisplayName("Get APIs using ARA document -> Sync version")
        public void syncApiVersion()
        {
            var api = Mockito.mock(SyncApi.class);

            when(syncApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(List.of(api));
            when(asyncBackToBackApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToFrontApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());

            List<Api<?,?,?>> ret = resolverApiManagerServiceModalityBased.getApisUsingAraDocument(araDocumentId);

            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
            verify(syncApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            verify(asyncBackToBackApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            verify(asyncBackToFrontApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
        }

        @Test
        @DisplayName("Get APIs using ARA document -> Async back to back version")
        public void asyncBackToBackApiVersion()
        {
            var api = Mockito.mock(AsyncBackToBackApi.class);

            when(syncApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToBackApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(List.of(api));
            when(asyncBackToFrontApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());

            List<Api<?,?,?>> ret = resolverApiManagerServiceModalityBased.getApisUsingAraDocument(araDocumentId);

            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
            verify(syncApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            verify(asyncBackToBackApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            verify(asyncBackToFrontApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
        }

        @Test
        @DisplayName("Get APIs using ARA document -> Async back to front version")
        public void asyncBackToFrontApiVersion()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class);

            when(syncApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToBackApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());
            when(asyncBackToFrontApiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(List.of(api));

            List<Api<?,?,?>> ret = resolverApiManagerServiceModalityBased.getApisUsingAraDocument(araDocumentId);

            assertEquals(1, ret.size());
            assertEquals(api, ret.get(0));
            verify(syncApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            verify(asyncBackToBackApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            verify(asyncBackToFrontApiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
        }

        @Test
        @DisplayName("Get APIs using ARA document -> Modality not implemented")
        public void buildMissingModality()
        {
            List<Api<?,?,?>> ret = new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                                        .getApisUsingAraDocument(araDocumentId);

            assertEquals(0, ret.size());
        }
    }

    @Nested
    class CreateApiImplementation
    {
        @ParameterizedTest
        @EnumSource(ImplementedAs.class)
        @DisplayName("Create API implementation -> Sync version OK")
        public void syncApiVersionOk(ImplementedAs implementedAs)
        {
            var apiVersion = Mockito.mock(SyncApiVersion.class);
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            when(apiVersion.getApiModality()).thenReturn(ApiModality.SYNC);

            resolverApiManagerServiceModalityBased.createApiImplementation(apiVersion, releaseVersionService, implementedAs);

            verify(syncApiManagerServiceModalityBased).createApiImplementation(apiVersion, releaseVersionService, implementedAs);
        }

        @ParameterizedTest
        @EnumSource(ImplementedAs.class)
        @DisplayName("Create API implementation -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk(ImplementedAs implementedAs)
        {
            var apiVersion = Mockito.mock(AsyncBackToBackApiVersion.class);
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            when(apiVersion.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            resolverApiManagerServiceModalityBased.createApiImplementation(apiVersion, releaseVersionService, implementedAs);

            verify(asyncBackToBackApiManagerServiceModalityBased).createApiImplementation(apiVersion, releaseVersionService, implementedAs);
        }

        @ParameterizedTest
        @EnumSource(ImplementedAs.class)
        @DisplayName("Create API implementation -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk(ImplementedAs implementedAs)
        {
            var apiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class);
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            when(apiVersion.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            resolverApiManagerServiceModalityBased.createApiImplementation(apiVersion, releaseVersionService, implementedAs);

            verify(asyncBackToFrontApiManagerServiceModalityBased).createApiImplementation(apiVersion, releaseVersionService, implementedAs);
        }

        @ParameterizedTest
        @ArgumentsSource(CreateApiImplementationArgumentsProvider.class)
        @DisplayName("Create API implementation -> Modality not implemented")
        public void buildMissingModality(ApiModality modality, ImplementedAs implementedAs)
        {
            var apiVersion = Mockito.mock(ApiVersion.class);
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            when(apiVersion.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .createApiImplementation(apiVersion, releaseVersionService, implementedAs)
            );
        }
    }

    static class CreateApiImplementationArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Stream.of(
                    Arguments.of(ApiModality.SYNC, ImplementedAs.SERVED),
                    Arguments.of(ApiModality.SYNC, ImplementedAs.EXTERNAL),
                    Arguments.of(ApiModality.SYNC, ImplementedAs.CONSUMED),
                    Arguments.of(ApiModality.SYNC, ImplementedAs.CONSUMEDBY),
                    Arguments.of(ApiModality.SYNC, ImplementedAs.CONSUMEDBY_EXTERNAL),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ImplementedAs.SERVED),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ImplementedAs.EXTERNAL),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ImplementedAs.CONSUMED),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ImplementedAs.CONSUMEDBY),
                    Arguments.of(ApiModality.ASYNC_BACKTOBACK, ImplementedAs.CONSUMEDBY_EXTERNAL),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ImplementedAs.SERVED),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ImplementedAs.EXTERNAL),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ImplementedAs.CONSUMED),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ImplementedAs.CONSUMEDBY),
                    Arguments.of(ApiModality.ASYNC_BACKTOFRONT, ImplementedAs.CONSUMEDBY_EXTERNAL)
            );
        }
    }

    @Nested
    class SetConsumedApisByServedApi
    {
        final List<Integer> consumedApis = List.of(909, 2408);

        @Test
        @DisplayName("Set consumed APIs by served API -> Sync version OK")
        public void syncApiVersionOk()
        {
            var apiImpl = Mockito.mock(SyncApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(ApiModality.SYNC);

            when(syncApiManagerServiceModalityBased.setConsumedApisByServedApi(any(), any())).then(i -> i.getArgument(0));

            ApiImplementation<?,?,?> ret = resolverApiManagerServiceModalityBased.setConsumedApisByServedApi(apiImpl, consumedApis);

            assertEquals(apiImpl, ret);
            verify(syncApiManagerServiceModalityBased).setConsumedApisByServedApi(apiImpl, consumedApis);
        }

        @Test
        @DisplayName("Delete API todo task -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var apiImpl = Mockito.mock(AsyncBackToBackApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            when(asyncBackToBackApiManagerServiceModalityBased.setConsumedApisByServedApi(any(), any())).then(i -> i.getArgument(0));

            ApiImplementation<?,?,?> ret = resolverApiManagerServiceModalityBased.setConsumedApisByServedApi(apiImpl, consumedApis);

            assertEquals(apiImpl, ret);
            verify(asyncBackToBackApiManagerServiceModalityBased).setConsumedApisByServedApi(apiImpl, consumedApis);
        }

        @Test
        @DisplayName("Delete API todo task -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var apiImpl = Mockito.mock(AsyncBackToFrontApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            when(asyncBackToFrontApiManagerServiceModalityBased.setConsumedApisByServedApi(any(), any())).then(i -> i.getArgument(0));

            ApiImplementation<?,?,?> ret = resolverApiManagerServiceModalityBased.setConsumedApisByServedApi(apiImpl, consumedApis);

            assertEquals(apiImpl, ret);
            verify(asyncBackToFrontApiManagerServiceModalityBased).setConsumedApisByServedApi(apiImpl, consumedApis);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Delete API todo task -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var apiImpl = Mockito.mock(ApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .setConsumedApisByServedApi(apiImpl, consumedApis)
            );
        }
    }

    @Nested
    class SetBackwardCompatibleVersionsOfServedApi
    {
        final List<String> backwardCompatibleVersions = List.of("1.0.2", "1.0.1");

        @Test
        @DisplayName("Set backward compatible versions of served API -> Sync version OK")
        public void syncApiVersionOk()
        {
            var apiImpl = Mockito.mock(SyncApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(ApiModality.SYNC);

            when(syncApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(any(), any())).then(i -> i.getArgument(0));

            ApiImplementation<?,?,?> ret = resolverApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions);

            assertEquals(apiImpl, ret);
            verify(syncApiManagerServiceModalityBased).setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions);
        }

        @Test
        @DisplayName("Set backward compatible versions of served API -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var apiImpl = Mockito.mock(AsyncBackToBackApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            when(asyncBackToBackApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(any(), any())).then(i -> i.getArgument(0));

            ApiImplementation<?,?,?> ret = resolverApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions);

            assertEquals(apiImpl, ret);
            verify(asyncBackToBackApiManagerServiceModalityBased).setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions);
        }

        @Test
        @DisplayName("Set backward compatible versions of served API -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var apiImpl = Mockito.mock(AsyncBackToFrontApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            when(asyncBackToFrontApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(any(), any())).then(i -> i.getArgument(0));

            ApiImplementation<?,?,?> ret = resolverApiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions);

            assertEquals(apiImpl, ret);
            verify(asyncBackToFrontApiManagerServiceModalityBased).setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Set backward compatible versions of served API -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var apiImpl = Mockito.mock(ApiImplementation.class);

            when(apiImpl.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .setBackwardCompatibleVersionsOfServedApi(apiImpl, backwardCompatibleVersions)
            );
        }

    }

    @Nested
    class DownloadProductApi
    {
        final String format = "format";
        final String downloadType = "downloadType";
        final byte[] bytes = new byte[]{9,2};

        @Test
        @DisplayName("Download product API -> Sync version OK")
        public void syncApiVersionOk()
        {
            var apiVersion = Mockito.mock(SyncApiVersion.class);

            when(apiVersion.getApiModality()).thenReturn(ApiModality.SYNC);

            when(syncApiManagerServiceModalityBased.downloadProductApi(any(), any(), any())).thenReturn(bytes);

            byte[] ret = resolverApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, downloadType);

            assertEquals(bytes, ret);
            verify(syncApiManagerServiceModalityBased).downloadProductApi(apiVersion, format, downloadType);
        }

        @Test
        @DisplayName("Download product API -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var apiVersion = Mockito.mock(AsyncBackToBackApiVersion.class);

            when(apiVersion.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

            when(asyncBackToBackApiManagerServiceModalityBased.downloadProductApi(any(), any(), any())).thenReturn(bytes);

            byte[] ret = resolverApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, downloadType);

            assertEquals(bytes, ret);
            verify(asyncBackToBackApiManagerServiceModalityBased).downloadProductApi(apiVersion, format, downloadType);
        }

        @Test
        @DisplayName("Download product API -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var apiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class);

            when(apiVersion.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);

            when(asyncBackToFrontApiManagerServiceModalityBased.downloadProductApi(any(), any(), any())).thenReturn(bytes);

            byte[] ret = resolverApiManagerServiceModalityBased.downloadProductApi(apiVersion, format, downloadType);

            assertEquals(bytes, ret);
            verify(asyncBackToFrontApiManagerServiceModalityBased).downloadProductApi(apiVersion, format, downloadType);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Download product API -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var apiVersion = Mockito.mock(ApiVersion.class);

            when(apiVersion.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerServiceModalityBasedImpl(Collections.emptyList())
                            .downloadProductApi(apiVersion, format, downloadType)
            );
        }

    }

    @Nested
    public class IsModalitySupported
    {
        @ParameterizedTest
        @EnumSource(value = ApiModality.class)
        public void ok(final ApiModality modality)
        {
            // When
            boolean modalitySupported = resolverApiManagerServiceModalityBased.isModalitySupported(modality);

            // Then
            assertEquals(Boolean.TRUE, modalitySupported);
        }
    }


}
