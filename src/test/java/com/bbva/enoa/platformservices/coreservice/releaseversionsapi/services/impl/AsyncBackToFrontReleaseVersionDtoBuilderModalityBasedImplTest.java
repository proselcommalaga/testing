package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class AsyncBackToFrontReleaseVersionDtoBuilderModalityBasedImplTest
{
    @InjectMocks
    private AsyncBackToFrontReleaseVersionDtoBuilderModalityBasedImpl releaseVersionDtoBuilderModalityBased;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class buildRVApiDTO
    {

        @Test
        void buildConsumedRVApiDTO()
        {
            // given
            AsyncBackToFrontApiImplementation apiImplementation = Mockito.mock(AsyncBackToFrontApiImplementation.class);
            AsyncBackToFrontApiVersion asyncBackToBackApiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class);
            AsyncBackToFrontApi asyncBackToBackApi = Mockito.mock(AsyncBackToFrontApi.class);
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);
            Product product = Mockito.mock(Product.class);
            Integer assertionId = 10;

            // when
            Mockito.when(apiImplementation.getApiVersion()).thenReturn(asyncBackToBackApiVersion);
            Mockito.when(asyncBackToBackApiVersion.getApi()).thenReturn(asyncBackToBackApi);
            Mockito.when(apiImplementation.getService()).thenReturn(releaseVersionService);
            Mockito.when(apiImplementation.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);
            Mockito.when(apiImplementation.getImplementedAs()).thenReturn(ImplementedAs.CONSUMED);
            Mockito.when(asyncBackToBackApi.getProduct()).thenReturn(product);
            Mockito.when(asyncBackToBackApiVersion.getId()).thenReturn(assertionId);

            // then
            RVApiDTO rvApiDTO = Assertions.assertDoesNotThrow(() -> releaseVersionDtoBuilderModalityBased.buildRVApiDTO(apiImplementation));
            Assertions.assertEquals(assertionId, rvApiDTO.getId());
        }

        @Test
        void buildExternalRVApiDTO()
        {
            // given
            AsyncBackToFrontApiImplementation apiImplementation = Mockito.mock(AsyncBackToFrontApiImplementation.class);
            AsyncBackToFrontApiVersion asyncBackToBackApiVersion = Mockito.mock(AsyncBackToFrontApiVersion.class);
            AsyncBackToFrontApi asyncBackToBackApi = Mockito.mock(AsyncBackToFrontApi.class);
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);
            Product product = Mockito.mock(Product.class);
            Integer assertionId = 10;

            // when
            Mockito.when(apiImplementation.getApiVersion()).thenReturn(asyncBackToBackApiVersion);
            Mockito.when(asyncBackToBackApiVersion.getApi()).thenReturn(asyncBackToBackApi);
            Mockito.when(apiImplementation.getService()).thenReturn(releaseVersionService);
            Mockito.when(apiImplementation.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);
            Mockito.when(apiImplementation.getImplementedAs()).thenReturn(ImplementedAs.EXTERNAL);
            Mockito.when(asyncBackToBackApi.getProduct()).thenReturn(product);
            Mockito.when(asyncBackToBackApiVersion.getId()).thenReturn(assertionId);

            // then
            RVApiDTO rvApiDTO = Assertions.assertDoesNotThrow(() -> releaseVersionDtoBuilderModalityBased.buildRVApiDTO(apiImplementation));
            Assertions.assertEquals(assertionId, rvApiDTO.getId());
        }
    }


    @Test
    void isModalitySupported()
    {
        // given
        ApiModality apiModality = ApiModality.ASYNC_BACKTOFRONT;

        // then
        Assertions.assertTrue(this.releaseVersionDtoBuilderModalityBased.isModalitySupported(apiModality));
    }
}