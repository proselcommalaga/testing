package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMConsumedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPublicationDetailDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServiceDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal.IApiGatewayDtoBuilderModalityBased;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.ApiGatewayUtils;
import com.bbva.enoa.platformservices.coreservice.common.model.PublicationParams;
import com.bbva.enoa.platformservices.coreservice.common.model.ServicePublicationParams;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.Constants.ENTITY_TYPE_DEPLOYMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ApiGatewayDtoBuilderUtilsTest
{
    @Mock
    private IApiGatewayDtoBuilderModalityBased apiGatewayDtoBuilderModalityBased;
    @Mock
    private ApiGatewayUtils utils;
    @InjectMocks
    private ApiGatewayDtoBuilderUtils builderUtils;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyInteractions()
    {
        verifyNoMoreInteractions(
                apiGatewayDtoBuilderModalityBased,
                utils
        );
    }

    @Nested
    class FillPublicationDetailInfo
    {
        String releaseName = "releaseName";
        String uuaa = "JGMV";
        Platform destinationPlatformDeployType = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

        @Test
        @DisplayName("Fill publication detail info -> OK")
        public void ok()
        {
            var deploymentPlan = Mockito.mock(DeploymentPlan.class, RETURNS_DEEP_STUBS);
            var release = Mockito.mock(Release.class, RETURNS_DEEP_STUBS);
            var publicationDetailDTO = Mockito.mock(AGMPublicationDetailDTO.class);

            when(deploymentPlan.getSelectedDeploy()).thenReturn(destinationPlatformDeployType);
            when(deploymentPlan.getReleaseVersion().getRelease()).thenReturn(release);
            when(release.getName()).thenReturn(releaseName);
            when(release.getProduct().getUuaa()).thenReturn(uuaa);

            builderUtils.fillPublicationDetailInfo(publicationDetailDTO, deploymentPlan.getSelectedDeploy(), deploymentPlan.getReleaseVersion());

            verify(publicationDetailDTO).setRelease(releaseName);
            verify(publicationDetailDTO).setUuaa(uuaa);
            verify(publicationDetailDTO).setPlatform(destinationPlatformDeployType.name());
            verifyNoMoreInteractions(publicationDetailDTO);
        }
    }

    @Nested
    class BuildProfilingResourceValue
    {
        @Test
        @DisplayName("Build profiling resource value -> OK")
        public void ok()
        {
            var apiMethod = Mockito.mock(ApiMethod.class, RETURNS_DEEP_STUBS);
            var verb = Verb.values()[RandomUtils.nextInt(0, Verb.values().length)];
            var basePath = "basePath";
            var endpoint = "endpoint";
            var releaseName = "releaseName";

            when(apiMethod.getVerb()).thenReturn(verb);
            when(apiMethod.getSecurizableApiVersion().getSecurizableApi().getBasePathSwagger()).thenReturn(basePath);
            when(apiMethod.getEndpoint()).thenReturn(endpoint);

            String ret = builderUtils.buildProfilingResourceValue(releaseName, apiMethod);

            assertEquals(releaseName + ":" + verb.getVerb() + ":" + basePath + endpoint, ret);

        }
    }

    @Nested
    class GetAllBatchesFromPlan
    {

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(BatchesWithMicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Get all batches from plan -> Plan with batches")
        public void planWithBatches(ServiceType serviceType)
        {
            var plan = buildDeploymentPlan(serviceType);

            List<DeploymentService> ret = builderUtils.getAllBatchesFromPlan(plan);

            assertEquals(1, ret.size());
        }

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(NeitherBatchesNorMicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Get all batches from plan -> Plan without batches")
        public void planWithoutBatches(ServiceType serviceType)
        {
            var plan = buildDeploymentPlan(serviceType);

            List<DeploymentService> ret = builderUtils.getAllBatchesFromPlan(plan);

            assertEquals(0, ret.size());
        }

        DeploymentPlan buildDeploymentPlan(final ServiceType serviceType)
        {
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceType(serviceType.getServiceType());
            var deploymentPlan = new DeploymentPlan();
            var deploymentService = new DeploymentService();
            deploymentService.setService(releaseVersionService);
            var deploymentSubsystem = new DeploymentSubsystem();
            deploymentSubsystem.setDeploymentServices(Collections.singletonList(deploymentService));
            deploymentPlan.setDeploymentSubsystems(Collections.singletonList(deploymentSubsystem));

            return deploymentPlan;
        }
    }

    @Nested
    class BuildServiceDto
    {
        String releaseName = "releaseName";
        String serviceLocation = "serviceLocation";
        Integer releaseVersionServiceId = 909;
        Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        Platform destinationPlatformDeployType = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

        @Test
        @DisplayName("Build service dto -> Do nothing because service is null")
        public void nullService()
        {
            PublicationParams publicationParams = this.buildPublicationParams();

            Optional<AGMServiceDTO> ret = builderUtils.buildServiceDto(publicationParams, null);

            assertTrue(ret.isEmpty());
        }

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(NoMicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Build service dto -> Do nothing because service does not use microgw")
        public void doNothing(ServiceType serviceType)
        {
            PublicationParams publicationParams = this.buildPublicationParams();
            var service = new ServicePublicationParams();
            service.setServiceType(serviceType.getServiceType());

            Optional<AGMServiceDTO> ret = builderUtils.buildServiceDto(publicationParams, service);

            assertTrue(ret.isEmpty());
        }

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Build service dto -> Microgw service with served and consumed APIs")
        public void servedAPIs(ServiceType serviceType)
        {
            var servedApiImplementation = Mockito.mock(ApiImplementation.class);
            var servedApiVersion = Mockito.mock(ApiVersion.class);
            var servedApiDTO = buidAGMServedApiDTO();

            var consumedApiImplementation = Mockito.mock(ApiImplementation.class);
            var consumedApiVersion = Mockito.mock(ApiVersion.class);
            var consumedApiDTO = buidAGMConsumedApiDTO();

            when(servedApiImplementation.getApiVersion()).thenReturn(servedApiVersion);
            when(servedApiImplementation.getImplementedAs()).thenReturn(ImplementedAs.SERVED);
            when(consumedApiImplementation.getApiVersion()).thenReturn(consumedApiVersion);
            when(consumedApiImplementation.getImplementedAs()).thenReturn(ImplementedAs.CONSUMED);

            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setId(releaseVersionServiceId);
            releaseVersionService.setServiceType(serviceType.getServiceType());
            releaseVersionService.setGroupId(RandomStringUtils.randomAlphabetic(6));
            releaseVersionService.setArtifactId(RandomStringUtils.randomAlphabetic(6));
            releaseVersionService.setVersion(RandomStringUtils.randomAlphabetic(6));
            releaseVersionService.setServers(List.of(servedApiImplementation));
            releaseVersionService.setConsumers(List.of(consumedApiImplementation));

            var plan = this.buildPublicationParams();

            var servicePublicationParams = new ServicePublicationParams();
            servicePublicationParams.setVersionServiceId(releaseVersionService.getId());
            servicePublicationParams.setGroupId(releaseVersionService.getGroupId());
            servicePublicationParams.setArtifactId(releaseVersionService.getArtifactId());
            servicePublicationParams.setVersion(releaseVersionService.getVersion());
            servicePublicationParams.setServiceType(releaseVersionService.getServiceType());
            servicePublicationParams.setServedApisList(releaseVersionService.getServers());
            servicePublicationParams.setConsumedApiList(releaseVersionService.getAllConsumedApiVersions());

            when(utils.buildServiceLocationByParams(anyString(), anyString(), anyString(), any(), any(), any())).thenReturn(serviceLocation);
            doAnswer(i -> i.getArgument(1, AGMServiceDtoBuilder.class).addServedApi(servedApiDTO))
                    .when(apiGatewayDtoBuilderModalityBased).addServedApiToServiceDto(any(), any());
            doAnswer(i -> i.getArgument(3, AGMServiceDtoBuilder.class).addConsumedApis(consumedApiDTO))
                    .when(apiGatewayDtoBuilderModalityBased).addConsumedApiToServiceDto(any(), any(), any(), any());

            Optional<AGMServiceDTO> ret = builderUtils.buildServiceDto(plan, servicePublicationParams);

            verify(utils).buildServiceLocationByParams(releaseVersionService.getGroupId(), releaseVersionService.getArtifactId(), releaseVersionService.getVersion(), destinationPlatformDeployType.name(), releaseName, environment.getEnvironment());
            verify(apiGatewayDtoBuilderModalityBased).addServedApiToServiceDto(eq(servedApiImplementation), any());
            verify(apiGatewayDtoBuilderModalityBased).addConsumedApiToServiceDto(eq(consumedApiVersion), eq(destinationPlatformDeployType.name()), eq(environment.getEnvironment()), any());

            assertTrue(ret.isPresent());
            AGMServiceDTO serviceDTO = ret.get();
            assertEquals(serviceLocation, serviceDTO.getLocation());
            assertEquals(releaseVersionServiceId, serviceDTO.getServiceId());
            assertEquals(destinationPlatformDeployType.name(), serviceDTO.getPlatform());
            assertEquals(1, serviceDTO.getServedApis().length);
            assertEquals(servedApiDTO, serviceDTO.getServedApis()[0]);
            assertEquals(1, serviceDTO.getConsumedApis().length);
            assertEquals(consumedApiDTO, serviceDTO.getConsumedApis()[0]);
            assertEquals(0, serviceDTO.getAsyncApis().length);
            assertEquals(0, serviceDTO.getExternalConsumedApis().length);
            assertEquals(0, serviceDTO.getExternalNovaConsumedApis().length);
        }

        private AGMServedApiDTO buidAGMServedApiDTO()
        {
            var servedApiDTO = new AGMServedApiDTO();
            servedApiDTO.fillRandomly(4, false, 0, 4);
            return servedApiDTO;
        }

        private AGMConsumedApiDTO buidAGMConsumedApiDTO()
        {
            var consumedApiDTO = new AGMConsumedApiDTO();
            consumedApiDTO.fillRandomly(4, false, 0, 4);
            return consumedApiDTO;
        }

        private DeploymentPlan buildDeploymentPlan()
        {
            var deploymentPlan = new DeploymentPlan();
            deploymentPlan.setEnvironment(environment.getEnvironment());
            deploymentPlan.setReleaseVersion(this.buildReleaseVersion());
            deploymentPlan.setSelectedDeploy(destinationPlatformDeployType);

            return deploymentPlan;
        }

        private PublicationParams buildPublicationParams()
        {
            PublicationParams publicationParams = new PublicationParams();
            publicationParams.setEnvironment(environment.getEnvironment());
            publicationParams.setReleaseVersion(this.buildReleaseVersion());
            publicationParams.setReleaseName(releaseName);
            publicationParams.setPlatform(destinationPlatformDeployType);
            publicationParams.setEntityType(ENTITY_TYPE_DEPLOYMENT);
            publicationParams.setServicePublicationParamsList(Collections.singletonList(Mockito.mock(ServicePublicationParams.class)));

            return publicationParams;
        }

        private ReleaseVersion buildReleaseVersion()
        {
            var release = new Release();
            release.setName(releaseName);
            var releaseVersion = new ReleaseVersion();
            releaseVersion.setRelease(release);

            return releaseVersion;
        }
    }

    private static class NoMicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Arrays.stream(ServiceType.values()).filter(serviceType -> !serviceType.isMicrogateway()).map(Arguments::of);
        }
    }

    private static class MicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Arrays.stream(ServiceType.values()).filter(ServiceType::isMicrogateway).map(Arguments::of);
        }
    }

    private static class BatchesWithMicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Arrays.stream(ServiceType.values()).filter(serviceType -> serviceType.isMicrogateway() && serviceType.isBatch()).map(Arguments::of);
        }
    }

    private static class NeitherBatchesNorMicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Arrays.stream(ServiceType.values()).filter(serviceType -> !serviceType.isMicrogateway() || !serviceType.isBatch()).map(Arguments::of);
        }
    }
}