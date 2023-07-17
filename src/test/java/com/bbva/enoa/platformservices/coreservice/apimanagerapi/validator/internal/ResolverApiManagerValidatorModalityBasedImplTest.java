package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.Api;
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

public class ResolverApiManagerValidatorModalityBasedImplTest
{
    private IApiManagerValidatorModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation> asyncBackToBackApiManagerValidatorModalityBased;
    private IApiManagerValidatorModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation> asyncBackToFrontApiManagerValidatorModalityBased;
    private IApiManagerValidatorModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation> syncApiManagerValidatorModalityBased;

    private ResolverApiManagerValidatorModalityBasedImpl resolverApiManagerValidatorModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        asyncBackToBackApiManagerValidatorModalityBased = Mockito.mock(AsyncBackToBackApiManagerValidatorModalityBasedImpl.class);

        when(asyncBackToBackApiManagerValidatorModalityBased.isModalitySupported(any()))
                .thenCallRealMethod();

        asyncBackToFrontApiManagerValidatorModalityBased = Mockito.mock(AsyncBackToFrontApiManagerValidatorModalityBasedImpl.class);

        when(asyncBackToFrontApiManagerValidatorModalityBased.isModalitySupported(any()))
                .thenCallRealMethod();

        syncApiManagerValidatorModalityBased = Mockito.mock(SyncApiManagerValidatorModalityBasedImpl.class);

        when(syncApiManagerValidatorModalityBased.isModalitySupported(any()))
                .thenCallRealMethod();

        resolverApiManagerValidatorModalityBased = new ResolverApiManagerValidatorModalityBasedImpl(List.of(asyncBackToBackApiManagerValidatorModalityBased, asyncBackToFrontApiManagerValidatorModalityBased, syncApiManagerValidatorModalityBased));

        Mockito.reset(asyncBackToBackApiManagerValidatorModalityBased, asyncBackToFrontApiManagerValidatorModalityBased, syncApiManagerValidatorModalityBased);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                asyncBackToBackApiManagerValidatorModalityBased,
                asyncBackToFrontApiManagerValidatorModalityBased,
                syncApiManagerValidatorModalityBased
        );
    }

    @Nested
    class Constructor
    {
        @Test
        @DisplayName("ResolverApiManagerValidatorModalityBasedImpl is instantiated successfully")
        public void constructorOk()
        {
            // Given
            final int numberOfModalities = ApiModality.values().length;

            // When
            new ResolverApiManagerValidatorModalityBasedImpl(List.of(asyncBackToBackApiManagerValidatorModalityBased, asyncBackToFrontApiManagerValidatorModalityBased, syncApiManagerValidatorModalityBased));

            // Then
            verify(syncApiManagerValidatorModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
            verify(asyncBackToBackApiManagerValidatorModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
            verify(asyncBackToFrontApiManagerValidatorModalityBased, atLeast(numberOfModalities)).isModalitySupported(any());
        }
    }

    @Nested
    class FindAndValidateOrPersistIfMissing
    {
        @Test
        @DisplayName("Api is validated successfully -> Sync")
        public void buildOkForSyncApiVersion()
        {
            // Given
            final SyncApi api = new SyncApi();

            final SyncApi expectedApi = new SyncApi();

            when(syncApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(any()))
                    .thenReturn(expectedApi);

            // When
            final Api returnedApi = resolverApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            // Then
            verify(syncApiManagerValidatorModalityBased).findAndValidateOrPersistIfMissing(api);

            assertEquals(expectedApi, returnedApi);
        }

        @Test
        @DisplayName("Api is validated successfully -> Async back to back")
        public void buildOkForAsyncBackToBackApiVersion()
        {
            // Given
            final AsyncBackToBackApi api = new AsyncBackToBackApi();

            final AsyncBackToBackApi expectedApi = new AsyncBackToBackApi();

            when(asyncBackToBackApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(any()))
                    .thenReturn(expectedApi);

            // When
            final Api returnedApi = resolverApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            // Then
            verify(asyncBackToBackApiManagerValidatorModalityBased).findAndValidateOrPersistIfMissing(api);

            assertEquals(expectedApi, returnedApi);
        }

        @Test
        @DisplayName("Api is validated successfully -> Async back to front")
        public void buildOkForAsyncBackToFrontApiVersion()
        {
            // Given
            final AsyncBackToFrontApi api = new AsyncBackToFrontApi();

            final AsyncBackToFrontApi expectedApi = new AsyncBackToFrontApi();

            when(asyncBackToFrontApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(any()))
                    .thenReturn(expectedApi);

            // When
            final Api returnedApi = resolverApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(api);

            // Then
            verify(asyncBackToFrontApiManagerValidatorModalityBased).findAndValidateOrPersistIfMissing(api);

            assertEquals(expectedApi, returnedApi);
        }

        @Test
        @DisplayName("Error in api validation -> Modality implementation is missing")
        public void buildMissingModality()
        {
            final AsyncBackToFrontApi api = new AsyncBackToFrontApi();

            final AsyncBackToFrontApi expectedApi = new AsyncBackToFrontApi();

            when(asyncBackToFrontApiManagerValidatorModalityBased.findAndValidateOrPersistIfMissing(any()))
                    .thenReturn(expectedApi);

            assertThrows(
                    NotImplementedException.class,
                    () -> new ResolverApiManagerValidatorModalityBasedImpl(Collections.emptyList())
                            .findAndValidateOrPersistIfMissing(api)
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
            boolean modalitySupported = resolverApiManagerValidatorModalityBased.isModalitySupported(modality);

            // Then
            assertEquals(Boolean.TRUE, modalitySupported);
        }
    }
}
