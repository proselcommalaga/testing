package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsyncBackToFrontApiGatewayDtoBuilderModalityBasedImplTest
{
    AsyncBackToFrontApiGatewayDtoBuilderModalityBasedImpl asyncBackToFrontApiGatewayDtoBuilderModalityBased = new AsyncBackToFrontApiGatewayDtoBuilderModalityBasedImpl();

    @Nested
    class AddServedApiToServiceDto
    {
        String uuaa = "JGMV";
        String apiName = "apiName";
        String version = "1.0.0";
        String basePath = "basePath";
        String definitionFile = "Definition file";

        @Test
        @DisplayName("Add served API to service dto -> ok")
        public void ok()
        {
            var servedApiImplementation = Mockito.mock(AsyncBackToFrontApiImplementation.class);
            var servedApiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class, RETURNS_DEEP_STUBS);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var apiMethod = new ApiMethod(
                    Verb.values()[RandomUtils.nextInt(0, Verb.values().length)],
                    "endpoint",
                    "description",
                    servedApiVersion
            );

            when(servedApiImplementation.getApiVersion()).thenReturn(servedApiVersion);
            when(servedApiVersion.getApi().getUuaa()).thenReturn(uuaa);
            when(servedApiVersion.getApi().getName()).thenReturn(apiName);
            when(servedApiVersion.getVersion()).thenReturn(version);
            when(servedApiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);
            when(servedApiVersion.getDefinitionFile().getContents()).thenReturn(definitionFile);
            when(servedApiVersion.getApiMethods()).thenReturn(List.of(apiMethod));

            asyncBackToFrontApiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            var servedApiDTOArgCaptor = ArgumentCaptor.forClass(AGMServedApiDTO.class);
            verify(serviceDTOBuilder).addAsyncApi(servedApiDTOArgCaptor.capture());

            var servedApiDTOCaptured = servedApiDTOArgCaptor.getValue();
            assertEquals(uuaa, servedApiDTOCaptured.getUuaa());
            assertEquals(apiName, servedApiDTOCaptured.getApiName());
            assertEquals(version, servedApiDTOCaptured.getApiVersion());
            assertEquals(basePath, servedApiDTOCaptured.getBasepath());
            assertEquals(Base64.getEncoder().encodeToString(definitionFile.getBytes()), servedApiDTOCaptured.getDefinitionFileContent());
            assertEquals(1, servedApiDTOCaptured.getEndpoints().length);
            var endpointDto = servedApiDTOCaptured.getEndpoints()[0];
            assertEquals(apiMethod.getVerb().getVerb(), endpointDto.getMethod());
            assertEquals(apiMethod.getEndpoint(), endpointDto.getPath());
        }
    }

    @Nested
    class AddConsumedApiToServiceDto
    {
        @Test
        @DisplayName("Add consumed API to service dto -> do nothing")
        public void doNothing()
        {
            var servedApiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);

            asyncBackToFrontApiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(servedApiVersion, "deploymentMode", "environment", serviceDTOBuilder);

            verifyNoInteractions(servedApiVersion);
            verifyNoInteractions(serviceDTOBuilder);
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOFRONT")
        public void backToBack()
        {
            assertTrue(asyncBackToFrontApiGatewayDtoBuilderModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOFRONT));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOBACK"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToFrontApiGatewayDtoBuilderModalityBased.isModalitySupported(modality));
        }
    }

}