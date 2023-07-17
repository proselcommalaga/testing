package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class SyncApiManagerValidatorModalityBasedImplTest
{
    @Mock
    private SyncApiRepository syncApiRepository;
    @InjectMocks
    private SyncApiManagerValidatorModalityBasedImpl syncApiManagerValidatorModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                syncApiRepository
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
            var api = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            var previousApi = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);
            when(api.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(previousApi.getType()).thenReturn(ApiType.GOVERNED);

            when(syncApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(previousApi);

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api)
            );

            verify(syncApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
        }

        @Test
        @DisplayName("Find API and validate it or persist if missing -> invalid base path error")
        public void invalidBasePath()
        {
            var api = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            var previousApi = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);
            when(api.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(api.getBasePathSwagger()).thenReturn("/basePath1");
            when(previousApi.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(previousApi.getBasePathSwagger()).thenReturn("/basePath2");

            when(syncApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(previousApi);

            assertThrows(
                    NovaException.class,
                    () -> syncApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api)
            );

            verify(syncApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);

        }

        @Test
        @DisplayName("Find API and validate it or persist if missing -> previous API exists")
        public void previousOk()
        {
            var api = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            var previousApi = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);
            when(api.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(api.getBasePathSwagger()).thenReturn("/basePath");
            when(previousApi.getType()).thenReturn(ApiType.NOT_GOVERNED);
            when(previousApi.getBasePathSwagger()).thenReturn("/basePath");

            when(syncApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(previousApi);

            SyncApi ret = syncApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            verify(syncApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
            assertEquals(previousApi, ret);
        }

        @Test
        @DisplayName("Find API and validate it or persist if missing -> new API created")
        public void newApiOk()
        {
            var api = Mockito.mock(SyncApi.class, Mockito.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);
            when(api.getName()).thenReturn(apiName);
            when(api.getUuaa()).thenReturn(uuaa);

            when(syncApiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(null);
            when(syncApiRepository.save(any())).then(i -> i.getArgument(0));

            SyncApi ret = syncApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            verify(syncApiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
            verify(syncApiRepository).save(api);
            assertEquals(api, ret);
        }

    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> SYNC")
        public void sync()
        {
            assertTrue(syncApiManagerValidatorModalityBased.isModalitySupported(ApiModality.SYNC));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"ASYNC_BACKTOBACK", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> ASYNC")
        public void async(ApiModality modality)
        {
            assertFalse(syncApiManagerValidatorModalityBased.isModalitySupported(modality));
        }
    }

}
