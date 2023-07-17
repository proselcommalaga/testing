package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ResolverDtoBuilderModalityBasedImplTest
{
    private IDtoBuilderModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation> asyncBackToBackDtoBuilderModalityBased;
    private IDtoBuilderModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation> asyncBackToFrontDtoBuilderModalityBased;
    private IDtoBuilderModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation> syncDtoBuilderModalityBased;

    private ResolverDtoBuilderModalityBasedImpl resolverDtoBuilderModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        asyncBackToBackDtoBuilderModalityBased = Mockito.mock(AsyncBackToBackDtoBuilderModalityBasedImpl.class);

        when(asyncBackToBackDtoBuilderModalityBased.isModalitySupported(any()))
                .thenCallRealMethod();

        asyncBackToFrontDtoBuilderModalityBased = Mockito.mock(AsyncBackToFrontDtoBuilderModalityBasedImpl.class);

        when(asyncBackToFrontDtoBuilderModalityBased.isModalitySupported(any()))
                .thenCallRealMethod();

        syncDtoBuilderModalityBased = Mockito.mock(SyncDtoBuilderModalityBasedImpl.class);

        when(syncDtoBuilderModalityBased.isModalitySupported(any()))
                .thenCallRealMethod();

        resolverDtoBuilderModalityBased = new ResolverDtoBuilderModalityBasedImpl(List.of(asyncBackToBackDtoBuilderModalityBased, asyncBackToFrontDtoBuilderModalityBased, syncDtoBuilderModalityBased));

        Mockito.reset(asyncBackToBackDtoBuilderModalityBased, asyncBackToFrontDtoBuilderModalityBased, syncDtoBuilderModalityBased);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                asyncBackToBackDtoBuilderModalityBased,
                asyncBackToFrontDtoBuilderModalityBased,
                syncDtoBuilderModalityBased
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
            new ResolverDtoBuilderModalityBasedImpl(List.of(asyncBackToBackDtoBuilderModalityBased, asyncBackToFrontDtoBuilderModalityBased, syncDtoBuilderModalityBased));

            // Then
            verify(syncDtoBuilderModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
            verify(asyncBackToBackDtoBuilderModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
            verify(asyncBackToFrontDtoBuilderModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
        }
    }

    @Nested
    class BuildApiPlanDto
    {
        @Test
        @DisplayName("ApiPlanDto is build successfully -> Sync")
        public void buildOkForSyncApiVersion()
        {
            final SyncApiVersion apiVersion = new SyncApiVersion();

            final ApiPlanDto expectedApiPlanDto = new ApiPlanDto();

            when(syncDtoBuilderModalityBased.buildApiPlanDto(any(), any(), any()))
                    .thenReturn(expectedApiPlanDto);

            final ApiPlanDto returnedApiPlanDto = resolverDtoBuilderModalityBased.buildApiPlanDto(apiVersion, null, null);

            verify(syncDtoBuilderModalityBased).buildApiPlanDto(apiVersion, null, null);

            assertEquals(expectedApiPlanDto, returnedApiPlanDto);
        }

        @Test
        @DisplayName("ApiPlanDto is build successfully -> Async back to back")
        public void buildOkForAsyncBackToBackApiVersion()
        {
            final AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();

            final ApiPlanDto expectedApiPlanDto = new ApiPlanDto();

            when(asyncBackToBackDtoBuilderModalityBased.buildApiPlanDto(any(), any(), any()))
                    .thenReturn(expectedApiPlanDto);

            final ApiPlanDto returnedApiPlanDto = resolverDtoBuilderModalityBased.buildApiPlanDto(apiVersion, null, null);

            verify(asyncBackToBackDtoBuilderModalityBased).buildApiPlanDto(apiVersion, null, null);

            assertEquals(expectedApiPlanDto, returnedApiPlanDto);
        }

        @Test
        @DisplayName("ApiPlanDto is build successfully -> Async back to front")
        public void buildOkForAsyncBackToFrontApiVersion()
        {
            final AsyncBackToFrontApiVersion apiVersion = new AsyncBackToFrontApiVersion();

            final ApiPlanDto expectedApiPlanDto = new ApiPlanDto();

            when(asyncBackToFrontDtoBuilderModalityBased.buildApiPlanDto(any(), any(), any()))
                    .thenReturn(expectedApiPlanDto);

            final ApiPlanDto returnedApiPlanDto = resolverDtoBuilderModalityBased.buildApiPlanDto(apiVersion, null, null);

            verify(asyncBackToFrontDtoBuilderModalityBased).buildApiPlanDto(apiVersion, null, null);

            assertEquals(expectedApiPlanDto, returnedApiPlanDto);
        }

        @Test
        @DisplayName("Error in ApiPlanDto build -> Modality implementation is missing")
        public void buildMissingModality()
        {
            final AsyncBackToFrontApiVersion apiVersion = new AsyncBackToFrontApiVersion();

            final ApiPlanDto expectedApiPlanDto = new ApiPlanDto();

            when(asyncBackToFrontDtoBuilderModalityBased.buildApiPlanDto(any(), any(), any()))
                    .thenReturn(expectedApiPlanDto);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverDtoBuilderModalityBasedImpl(Collections.emptyList())
                            .buildApiPlanDto(apiVersion, null, null)
            );
        }
    }

    @Nested
    class BuildApiDetailDto
    {
        @Test
        @DisplayName("ApiDetailDto is build successfully -> Sync")
        public void buildOkForSyncApiVersion()
        {
            final SyncApi api = new SyncApi();

            final ApiDetailDto expectedApiDetailDto = new ApiDetailDto();

            when(syncDtoBuilderModalityBased.buildApiDetailDto(any()))
                    .thenReturn(expectedApiDetailDto);

            final ApiDetailDto returnedApiDetailDto = resolverDtoBuilderModalityBased.buildApiDetailDto(api);

            verify(syncDtoBuilderModalityBased).buildApiDetailDto(api);

            assertEquals(expectedApiDetailDto, returnedApiDetailDto);
        }

        @Test
        @DisplayName("ApiDetailDto is build successfully -> Async back to back")
        public void buildOkForAsyncBackToBackApiVersion()
        {
            final AsyncBackToBackApi api = new AsyncBackToBackApi();

            final ApiDetailDto expectedApiDetailDto = new ApiDetailDto();

            when(asyncBackToBackDtoBuilderModalityBased.buildApiDetailDto(any()))
                    .thenReturn(expectedApiDetailDto);

            final ApiDetailDto returnedApiDetailDto = resolverDtoBuilderModalityBased.buildApiDetailDto(api);

            verify(asyncBackToBackDtoBuilderModalityBased).buildApiDetailDto(api);

            assertEquals(expectedApiDetailDto, returnedApiDetailDto);
        }

        @Test
        @DisplayName("ApiDetailDto is build successfully -> Async back to front")
        public void buildOkForAsyncBackToFrontApiVersion()
        {
            final AsyncBackToFrontApi api = new AsyncBackToFrontApi();

            final ApiDetailDto expectedApiDetailDto = new ApiDetailDto();

            when(asyncBackToFrontDtoBuilderModalityBased.buildApiDetailDto(any()))
                    .thenReturn(expectedApiDetailDto);

            final ApiDetailDto returnedApiDetailDto = resolverDtoBuilderModalityBased.buildApiDetailDto(api);

            verify(asyncBackToFrontDtoBuilderModalityBased).buildApiDetailDto(api);

            assertEquals(expectedApiDetailDto, returnedApiDetailDto);
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
            boolean modalitySupported = resolverDtoBuilderModalityBased.isModalitySupported(modality);

            // Then
            assertEquals(Boolean.TRUE, modalitySupported);
        }
    }
}
