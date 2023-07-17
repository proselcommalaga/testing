package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResolverApiGatewayDtoBuilderModalityBasedImplTest
{
    private IApiGatewayDtoBuilderModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation> asyncBackToBackApiGatewayDtoBuilderModalityBased;
    private IApiGatewayDtoBuilderModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation> asyncBackToFrontApiGatewayDtoBuilderModalityBased;
    private IApiGatewayDtoBuilderModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation> syncApiGatewayDtoBuilderModalityBased;

    private ResolverApiGatewayDtoBuilderModalityBasedImpl resolverApiGatewayDtoBuilderModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        asyncBackToBackApiGatewayDtoBuilderModalityBased = Mockito.mock(AsyncBackToBackApiGatewayDtoBuilderModalityBasedImpl.class);
        asyncBackToFrontApiGatewayDtoBuilderModalityBased = Mockito.mock(AsyncBackToFrontApiGatewayDtoBuilderModalityBasedImpl.class);
        syncApiGatewayDtoBuilderModalityBased = Mockito.mock(SyncApiGatewayDtoBuilderModalityBasedImpl.class);

        when(asyncBackToBackApiGatewayDtoBuilderModalityBased.isModalitySupported(any())).thenCallRealMethod();
        when(asyncBackToFrontApiGatewayDtoBuilderModalityBased.isModalitySupported(any())).thenCallRealMethod();
        when(syncApiGatewayDtoBuilderModalityBased.isModalitySupported(any())).thenCallRealMethod();

        resolverApiGatewayDtoBuilderModalityBased = new ResolverApiGatewayDtoBuilderModalityBasedImpl(
                List.of(
                        asyncBackToBackApiGatewayDtoBuilderModalityBased,
                        asyncBackToFrontApiGatewayDtoBuilderModalityBased,
                        syncApiGatewayDtoBuilderModalityBased
                )
        );

        Mockito.reset(
                asyncBackToBackApiGatewayDtoBuilderModalityBased,
                asyncBackToFrontApiGatewayDtoBuilderModalityBased,
                syncApiGatewayDtoBuilderModalityBased
        );
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                asyncBackToBackApiGatewayDtoBuilderModalityBased,
                asyncBackToFrontApiGatewayDtoBuilderModalityBased,
                syncApiGatewayDtoBuilderModalityBased
        );
    }


    @Nested
    public class AddServedApiToServiceDto
    {

        @Test
        @DisplayName("Add served API to service dto -> Sync version OK")
        public void syncApiVersionOk()
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var servedApiImplementation = new SyncApiImplementation();

            resolverApiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            verify(syncApiGatewayDtoBuilderModalityBased).addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);
        }

        @Test
        @DisplayName("Add served API to service dto -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var servedApiImplementation = new AsyncBackToBackApiImplementation();

            resolverApiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            verify(asyncBackToBackApiGatewayDtoBuilderModalityBased).addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);
        }

        @Test
        @DisplayName("Add served API to service dto -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var servedApiImplementation = new AsyncBackToFrontApiImplementation();

            resolverApiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            verify(asyncBackToFrontApiGatewayDtoBuilderModalityBased).addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Add served API to service dto -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var servedApiImplementation = Mockito.mock(ApiImplementation.class);

            when(servedApiImplementation.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiGatewayDtoBuilderModalityBasedImpl(Collections.emptyList())
                            .addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder)
            );
        }
    }

    @Nested
    public class AddConsumedApiToServiceDto
    {
        String deploymentMode = "deploymentMode";
        String environment = "environment";

        @Test
        @DisplayName("Add served API to service dto -> Sync version OK")
        public void syncApiVersionOk()
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiVersion = new SyncApiVersion();

            resolverApiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);

            verify(syncApiGatewayDtoBuilderModalityBased).addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);
        }

        @Test
        @DisplayName("Add served API to service dto -> Async back to back version OK")
        public void asyncBackToBackApiVersionOk()
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiVersion = new AsyncBackToBackApiVersion();

            resolverApiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);

            verify(asyncBackToBackApiGatewayDtoBuilderModalityBased).addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);
        }

        @Test
        @DisplayName("Add served API to service dto -> Async back to front version OK")
        public void asyncBackToFrontApiVersionOk()
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiVersion = new AsyncBackToFrontApiVersion();

            resolverApiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);

            verify(asyncBackToFrontApiGatewayDtoBuilderModalityBased).addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);
        }

        @ParameterizedTest
        @EnumSource(ApiModality.class)
        @DisplayName("Add served API to service dto -> Modality not implemented")
        public void buildMissingModality(ApiModality modality)
        {
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiVersion = Mockito.mock(ApiVersion.class);

            when(consumedApiVersion.getApiModality()).thenReturn(modality);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiGatewayDtoBuilderModalityBasedImpl(Collections.emptyList())
                            .addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder)
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
            boolean modalitySupported = resolverApiGatewayDtoBuilderModalityBased.isModalitySupported(modality);

            // Then
            assertEquals(Boolean.TRUE, modalitySupported);
        }
    }


}