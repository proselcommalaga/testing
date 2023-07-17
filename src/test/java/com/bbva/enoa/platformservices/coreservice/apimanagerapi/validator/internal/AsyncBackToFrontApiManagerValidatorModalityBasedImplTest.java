package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AsyncBackToFrontApiRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class AsyncBackToFrontApiManagerValidatorModalityBasedImplTest
{
    @Mock
    private AsyncBackToFrontApiRepository asyncBackToFrontApiRepository;
    @InjectMocks
    private AsyncBackToFrontApiManagerValidatorModalityBasedImpl asyncBackToFrontApiManagerValidatorModalityBased;
    
    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                asyncBackToFrontApiRepository
        );
    }    
    
    @Nested
    class FindAndValidateOrPersistIfMissing
    {
        Integer productId = 2408;
        String apiName = "Daniela";
        String uuaa = "JGMV";
        
        @Test
        @DisplayName("Find API and validate it or persist if missing -> invalid type error")
        public void invalidApiType()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class, Mockito.RETURNS_DEEP_STUBS);
            var previousApi = Mockito.mock(AsyncBackToFrontApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);
            when(api.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(previousApi.getType()).thenReturn(ApiType.GOVERNED);

            when(asyncBackToFrontApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(previousApi);

            assertThrows(
                    NovaException.class,
                    () -> asyncBackToFrontApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api)
            );

            verify(asyncBackToFrontApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
        }
        
        @Test
        @DisplayName("Find API and validate it or persist if missing -> previous API exists")
        public void previousOk()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class, Mockito.RETURNS_DEEP_STUBS);
            var previousApi = Mockito.mock(AsyncBackToFrontApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);
            when(api.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(previousApi.getType()).thenReturn(ApiType.NOT_GOVERNED);

            when(asyncBackToFrontApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(previousApi);

            AsyncBackToFrontApi ret = asyncBackToFrontApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            verify(asyncBackToFrontApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
            assertEquals(previousApi, ret);
        }

        @Test
        @DisplayName("Find API and validate it or persist if missing -> new API created")
        public void newApiOk()
        {
            var api = Mockito.mock(AsyncBackToFrontApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);

            when(asyncBackToFrontApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(null);
            when(asyncBackToFrontApiRepository.save(any())).then(i -> i.getArgument(0));

            AsyncBackToFrontApi ret = asyncBackToFrontApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            verify(asyncBackToFrontApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
            verify(asyncBackToFrontApiRepository).save(api);
            assertEquals(api, ret);
        }
    }
    
    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOFRONT")
        public void backToFront()
        {
            assertTrue(asyncBackToFrontApiManagerValidatorModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOFRONT));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOBACK"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToFrontApiManagerValidatorModalityBased.isModalitySupported(modality));
        }
    }
    
}
