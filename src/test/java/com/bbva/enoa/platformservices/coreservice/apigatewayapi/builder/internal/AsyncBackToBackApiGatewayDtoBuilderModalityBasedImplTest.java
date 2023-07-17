package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;

class AsyncBackToBackApiGatewayDtoBuilderModalityBasedImplTest
{

    private AsyncBackToBackApiGatewayDtoBuilderModalityBasedImpl asyncBackToBackApiGatewayDtoBuilderModalityBased = new AsyncBackToBackApiGatewayDtoBuilderModalityBasedImpl();

    @Nested
    class AddServedApiToServiceDto
    {
        @Test
        @DisplayName("Add served API to service dto -> do nothing")
        public void doNothing()
        {
            var servedApiImplementation = Mockito.mock(AsyncBackToBackApiImplementation.class);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);

            asyncBackToBackApiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            verifyNoInteractions(servedApiImplementation);
            verifyNoInteractions(serviceDTOBuilder);
        }
    }

    @Nested
    class AddConsumedApiToServiceDto
    {
        @Test
        @DisplayName("Add consumed API to service dto -> do nothing")
        public void doNothing()
        {
            var servedApiVersion = Mockito.mock(AsyncBackToBackApiVersion.class);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);

            asyncBackToBackApiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(servedApiVersion, "deploymentMode", "environment", serviceDTOBuilder);

            verifyNoInteractions(servedApiVersion);
            verifyNoInteractions(serviceDTOBuilder);
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOBACK")
        public void backToBacck()
        {
            assertTrue(asyncBackToBackApiGatewayDtoBuilderModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOBACK));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToBackApiGatewayDtoBuilderModalityBased.isModalitySupported(modality));
        }
    }
}