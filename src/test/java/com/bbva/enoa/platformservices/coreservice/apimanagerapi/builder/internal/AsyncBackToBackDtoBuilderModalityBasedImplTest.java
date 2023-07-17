package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AsyncBackToBackDtoBuilderModalityBasedImplTest
{
    @InjectMocks
    private AsyncBackToBackDtoBuilderModalityBasedImpl asyncBackToBackDtoBuilderModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class BuildApiDetailDto
    {
        @Test
        @DisplayName("ApiDetailDto is build successfully")
        public void buildOk()
        {
            final AsyncBackToBackApi api = buildAsyncBackToBackApi();

            final ApiDetailDto apiDetailDto = asyncBackToBackDtoBuilderModalityBased.buildApiDetailDto(api);

            assertEquals(1, apiDetailDto.getVersions().length);

            final AsyncBackToBackApiVersion apiVersion = api.getApiVersions().get(0);
            final ApiVersionDto apiVersionDetailDto = apiDetailDto.getVersions()[0];

            assertEquals(apiVersion.getId(), apiVersionDetailDto.getId());
            assertEquals(apiVersion.getVersion(), apiVersionDetailDto.getVersion());
            assertEquals(apiVersion.getApiState().getApiState(), apiVersionDetailDto.getStatus());
        }

        private AsyncBackToBackApi buildAsyncBackToBackApi()
        {
            final AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
            apiVersion.setId(RandomUtils.nextInt(0, 1000));
            apiVersion.setVersion(RandomStringUtils.randomAlphanumeric(6));
            apiVersion.setApiState(ApiState.values()[RandomUtils.nextInt(0, ApiState.values().length)]);

            final ArrayList<AsyncBackToBackApiVersion> apiVersions = new ArrayList<>();
            apiVersions.add(apiVersion);

            final AsyncBackToBackApi api = new AsyncBackToBackApi();
            api.setApiVersions(apiVersions);

            return api;
        }
    }

    @Nested
    class BuildApiPlanDto
    {
        @Test
        @DisplayName("ApiPlanDto is build successfully")
        public void buildOk()
        {
            final AsyncBackToBackApiVersion apiVersion = buildAsyncBackToBackApiVersion();

            final ApiPlanDto apiPlanDto = asyncBackToBackDtoBuilderModalityBased.buildApiPlanDto(apiVersion, null, null);

            assertEquals(apiVersion.getApi().getName() + ":" + apiVersion.getVersion(), apiPlanDto.getName());
        }

        private AsyncBackToBackApiVersion buildAsyncBackToBackApiVersion()
        {
            final AsyncBackToBackApi api = new AsyncBackToBackApi();
            api.setName(RandomStringUtils.randomAlphanumeric(25));

            final AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
            apiVersion.setVersion(RandomStringUtils.randomAlphanumeric(6));
            apiVersion.setApi(api);

            return apiVersion;
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOBACK")
        public void backToBacck()
        {
            assertTrue(asyncBackToBackDtoBuilderModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOBACK));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToBackDtoBuilderModalityBased.isModalitySupported(modality));
        }
    }

}
