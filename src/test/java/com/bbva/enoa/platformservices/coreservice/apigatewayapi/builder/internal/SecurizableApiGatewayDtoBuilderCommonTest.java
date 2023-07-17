package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMConsumedApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.ISecurizableApiVersion;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.ApiGatewayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SecurizableApiGatewayDtoBuilderCommonTest
{
    @Mock
    private ApiGatewayUtils utils;
    @InjectMocks
    private SecurizableApiGatewayDtoBuilderCommon securizableApiGatewayDtoBuilderCommon;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyInteractions()
    {
        verifyNoMoreInteractions(
                utils
        );
    }

    @Nested
    class BuildConsumedApi
    {
        String uuaa = "JGMV";
        String apiName = "apiName";
        String version = "1.0.0";
        String basePath = "basePath";
        String basePathXmas = "basePathXmas";
        String releaseName = "releaseName";
        String serviceLocation = "serviceLocation";
        String deploymentMode = "deploymentMode";
        String environment = "environment";

        @Test
        @DisplayName("Build consumed API -> ok")
        public void ok()
        {
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
            var consumedApiVersion = Mockito.mock(ISecurizableApiVersion.class, RETURNS_DEEP_STUBS);

            when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
            when(consumedApiVersion.getBasePathXmas()).thenReturn(basePathXmas);
            when(consumedApiVersion.getSecurizableApi().getBasePathSwagger()).thenReturn(basePath);
            when(consumedApiVersion.getSecurizableApi().getUuaa()).thenReturn(uuaa);
            when(consumedApiVersion.getSecurizableApi().getName()).thenReturn(apiName);
            when(consumedApiVersion.getVersion()).thenReturn(version);

            when(utils.buildServiceLocation(any(), any(), any(), any())).thenReturn(serviceLocation);

            AGMConsumedApiDTO ret = securizableApiGatewayDtoBuilderCommon.buildConsumedApi(consumedApiVersion, releaseVersionService, deploymentMode, environment);

            verify(utils).buildServiceLocation(releaseVersionService, deploymentMode, releaseName, environment);

            assertEquals(basePathXmas, ret.getConsumedPath());
            assertEquals(serviceLocation, ret.getLocation());
            assertEquals(releaseName, ret.getRelease());
            assertEquals(basePath, ret.getBasepath());
            assertEquals(version, ret.getApiVersion());
            assertEquals(uuaa, ret.getUuaa());
            assertEquals(apiName, ret.getApiName());
            assertNull(ret.getSwaggerContent());
        }

    }

    @Nested
    class BuildNovaExternalConsumedApi
    {
        String uuaa = "JGMV";
        String apiName = "apiName";
        String version = "1.0.0";
        String basePath = "basePath";
        String basePathXmas = "basePathXmas";
        String releaseName = "releaseName";
        String definitioFile = "definitioFile";

        @Test
        @DisplayName("Build NOVA external consumed API -> ok")
        public void ok()
        {
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
            var consumedExternalApiVersion = Mockito.mock(ISecurizableApiVersion.class, RETURNS_DEEP_STUBS);

            when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
            when(consumedExternalApiVersion.getBasePathXmas()).thenReturn(basePathXmas);
            when(consumedExternalApiVersion.getDefinitionFile().getContents()).thenReturn(definitioFile);
            when(consumedExternalApiVersion.getSecurizableApi().getBasePathSwagger()).thenReturn(basePath);
            when(consumedExternalApiVersion.getSecurizableApi().getUuaa()).thenReturn(uuaa);
            when(consumedExternalApiVersion.getSecurizableApi().getName()).thenReturn(apiName);
            when(consumedExternalApiVersion.getVersion()).thenReturn(version);

            AGMConsumedApiDTO ret = securizableApiGatewayDtoBuilderCommon.buildNovaExternalConsumedApi(consumedExternalApiVersion, releaseVersionService);

            assertEquals(basePathXmas, ret.getConsumedPath());
            assertEquals(basePathXmas, ret.getLocation());
            assertEquals(releaseName, ret.getRelease());
            assertEquals(basePath, ret.getBasepath());
            assertEquals(version, ret.getApiVersion());
            assertEquals(uuaa, ret.getUuaa());
            assertEquals(apiName, ret.getApiName());
            assertEquals(Base64.getEncoder().encodeToString(definitioFile.getBytes()), ret.getSwaggerContent());
        }
    }


    @Nested
    class BuildExternalConsumedApi
    {
        String uuaa = "JGMV";
        String apiName = "apiName";
        String version = "1.0.0";
        String basePath = "basePath";
        String basePathXmas = "basePathXmas";
        String definitionFile = "definitionFile";

        @Test
        @DisplayName("Build external consumed API -> ok")
        public void ok()
        {
            var externalApiVersion = Mockito.mock(ISecurizableApiVersion.class, RETURNS_DEEP_STUBS);

            when(externalApiVersion.getBasePathXmas()).thenReturn(basePathXmas);
            when(externalApiVersion.getDefinitionFile().getContents()).thenReturn(definitionFile);
            when(externalApiVersion.getSecurizableApi().getBasePathSwagger()).thenReturn(basePath);
            when(externalApiVersion.getSecurizableApi().getUuaa()).thenReturn(uuaa);
            when(externalApiVersion.getSecurizableApi().getName()).thenReturn(apiName);
            when(externalApiVersion.getVersion()).thenReturn(version);

            AGMConsumedApiDTO ret = securizableApiGatewayDtoBuilderCommon.buildExternalConsumedApi(externalApiVersion);

            assertEquals(basePathXmas, ret.getConsumedPath());
            assertEquals(basePathXmas, ret.getLocation());
            assertNull(ret.getRelease());
            assertEquals(basePath, ret.getBasepath());
            assertEquals(version, ret.getApiVersion());
            assertEquals(uuaa, ret.getUuaa());
            assertEquals(apiName, ret.getApiName());
            assertNull(ret.getSwaggerContent());
        }
    }

    @Nested
    class IsServiceConfiguredToBeDeployedOnInfrastructure
    {
        @Nested
        class SameInfrastructure
        {
            Platform destinationPlatformDeployType = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

            @Test
            @DisplayName("Is service configured to be deployed on infrastructure -> same infra in INT")
            public void integrado()
            {
                var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);

                when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployInt()).thenReturn(destinationPlatformDeployType);

                boolean ret = securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(destinationPlatformDeployType.name(), releaseVersionService, "INT");

                assertTrue(ret);
            }

            @Test
            @DisplayName("Is service configured to be deployed on infrastructure -> same infra in PRE")
            public void pre()
            {
                var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);

                when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployPre()).thenReturn(destinationPlatformDeployType);

                boolean ret = securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(destinationPlatformDeployType.name(), releaseVersionService, "PRE");

                assertTrue(ret);
            }

            @Test
            @DisplayName("Is service configured to be deployed on infrastructure -> same infra in PRO")
            public void pro()
            {
                var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);

                when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployPro()).thenReturn(destinationPlatformDeployType);

                boolean ret = securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(destinationPlatformDeployType.name(), releaseVersionService, "PRO");

                assertTrue(ret);
            }

        }

        @Nested
        class DifferentInfrastructure
        {
            int index1 = RandomUtils.nextInt(0, Platform.values().length);
            int index2 = (index1 + 1) % Platform.values().length;

            @Test
            @DisplayName("Is service configured to be deployed on infrastructure -> different infra in INT")
            public void integrado()
            {
                var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);

                when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployInt()).thenReturn(Platform.values()[index1]);

                boolean ret = securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(Platform.values()[index2].name(), releaseVersionService, "INT");

                assertFalse(ret);
            }

            @Test
            @DisplayName("Is service configured to be deployed on infrastructure -> different infra in PRE")
            public void pre()
            {
                var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);

                when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployPre()).thenReturn(Platform.values()[index1]);

                boolean ret = securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(Platform.values()[index2].name(), releaseVersionService, "PRE");

                assertFalse(ret);
            }

            @Test
            @DisplayName("Is service configured to be deployed on infrastructure -> different infra in PRO")
            public void pro()
            {
                var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);

                when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployPro()).thenReturn(Platform.values()[index1]);

                boolean ret = securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(Platform.values()[index2].name(), releaseVersionService, "PRO");

                assertFalse(ret);
            }

        }


    }


}