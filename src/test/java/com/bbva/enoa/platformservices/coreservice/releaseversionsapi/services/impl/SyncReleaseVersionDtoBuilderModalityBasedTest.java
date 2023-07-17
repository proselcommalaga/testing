package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class SyncReleaseVersionDtoBuilderModalityBasedTest
{
    @InjectMocks
    private SyncReleaseVersionDtoBuilderModalityBased syncReleaseVersionDtoBuilderModalityBased;

    @BeforeEach
    private void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    @Tag("happy_path")
    @DisplayName("Build a Release Version Api DTO successfully")
    public void testBuildRVApiDTO() {

        // given

        SyncApiVersion syncApiVersion = new SyncApiVersion();
        SyncApiVersion syncApiVersionConsumed = new SyncApiVersion();
        SyncApiVersion syncApiVersionCompatible = new SyncApiVersion();
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        SyncApiImplementation syncApiImplementation = new SyncApiImplementation();
        SyncApi syncApi = new SyncApi();
        Product product = new Product();

        // when
        syncApiImplementation.setApiVersion(syncApiVersion);
        syncApiImplementation.setImplementedAs(ImplementedAs.CONSUMED);
        syncApiImplementation.setId(1);
        syncApiImplementation.setService(releaseVersionService);
        syncApiImplementation.setConsumedApis(List.of(syncApiVersionConsumed));
        syncApiImplementation.setBackwardCompatibleApis(List.of(syncApiVersionCompatible));
        syncApiVersion.setApi(syncApi);
        syncApi.setPolicyStatus(ApiPolicyStatus.ESTABLISHED);
        syncApi.setProduct(product);

        syncApiVersionConsumed.setApi(syncApi);

        // then
        Assertions.assertDoesNotThrow(() -> this.syncReleaseVersionDtoBuilderModalityBased.buildRVApiDTO(syncApiImplementation));

    }

    @Test
    @Tag("happy_path")
    @DisplayName("Build a Release Version External Api DTO successfully")
    public void testBuildRVwithExternalApiDTO() {

        // given

        SyncApiVersion syncApiVersion = new SyncApiVersion();
        SyncApiVersion syncApiVersionConsumed = new SyncApiVersion();
        SyncApiVersion syncApiVersionCompatible = new SyncApiVersion();
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        SyncApiImplementation syncApiImplementation = new SyncApiImplementation();
        SyncApi syncApi = new SyncApi();
        Product product = new Product();

        // when
        syncApiImplementation.setApiVersion(syncApiVersion);
        syncApiImplementation.setImplementedAs(ImplementedAs.EXTERNAL);
        syncApiImplementation.setId(1);
        syncApiImplementation.setService(releaseVersionService);
        syncApiImplementation.setConsumedApis(List.of(syncApiVersionConsumed));
        syncApiImplementation.setBackwardCompatibleApis(List.of(syncApiVersionCompatible));
        syncApiVersion.setApi(syncApi);
        syncApi.setPolicyStatus(ApiPolicyStatus.ESTABLISHED);
        syncApi.setProduct(product);

        syncApiVersionConsumed.setApi(syncApi);
        syncApi.setType(ApiType.EXTERNAL);

        // then
        Assertions.assertDoesNotThrow(() -> this.syncReleaseVersionDtoBuilderModalityBased.buildRVApiDTO(syncApiImplementation));

    }

    @Test
    @Tag("happy_path")
    @DisplayName("Is this modality supported?")
    public void testIsModalitySupported()
    {
        // then
        Assertions.assertTrue(this.syncReleaseVersionDtoBuilderModalityBased.isModalitySupported(ApiModality.SYNC));

    }

}
