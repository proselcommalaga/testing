package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.ApiGatewayDtoBuilderUtils;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.ApiGatewayUtils;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.EtherUtils;
import com.bbva.enoa.platformservices.coreservice.common.model.PublicationParams;
import com.bbva.enoa.platformservices.coreservice.common.model.ServicePublicationParams;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.Constants.ENTITY_TYPE_DEPLOYMENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ApiGatewayDtoBuilderImplTest
{
    @Mock
    private EtherUtils etherUtils;
    @Mock
    private ApiGatewayUtils utils;
    @Mock
    private ApiGatewayDtoBuilderUtils builderUtils;
    @InjectMocks
    private ApiGatewayDtoBuilderImpl apiGatewayDtoBuilder;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyInteractions()
    {
        verifyNoMoreInteractions(
                builderUtils,
                utils,
                etherUtils
        );
    }

    @Nested
    class BuildCreatePublicationDto
    {

        @Test
        @DisplayName("Build create publication Dto -> Empty service lists")
        public void emptyServiceList()
        {
            final PublicationParams publicationParams = new PublicationParams();
            publicationParams.setId(RandomUtils.nextInt());
            publicationParams.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);
            publicationParams.setReleaseName(RandomStringUtils.randomAlphabetic(5));
            publicationParams.setReleaseVersion(Mockito.mock(ReleaseVersion.class));
            publicationParams.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name());
            publicationParams.setEntityType(ENTITY_TYPE_DEPLOYMENT);
            publicationParams.setServicePublicationParamsList(Collections.singletonList(Mockito.mock(ServicePublicationParams.class)));

            when(builderUtils.buildServiceDto(any(), any())).thenReturn(Optional.empty());

            AGMCreatePublicationDTO ret = apiGatewayDtoBuilder.buildCreatePublicationDto(publicationParams);

            verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            verify(builderUtils).buildServiceDto(any(), any());

            assertEquals(0, ret.getServices().length);
        }

        @Test
        @DisplayName("Build create publication Dto -> Filled service lists")
        public void filledServiceList()
        {
            final PublicationParams publicationParams = new PublicationParams();
            publicationParams.setId(RandomUtils.nextInt());
            publicationParams.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);
            publicationParams.setReleaseName(RandomStringUtils.randomAlphabetic(5));
            publicationParams.setReleaseVersion(Mockito.mock(ReleaseVersion.class));
            publicationParams.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name());
            publicationParams.setEntityType(ENTITY_TYPE_DEPLOYMENT);
            publicationParams.setServicePublicationParamsList(Collections.singletonList(mock(ServicePublicationParams.class)));

            when(builderUtils.buildServiceDto(eq(publicationParams), any())).thenReturn(Optional.of(Mockito.mock(AGMServiceDTO.class)));

            AGMCreatePublicationDTO ret = apiGatewayDtoBuilder.buildCreatePublicationDto(publicationParams);

            verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            verify(builderUtils).buildServiceDto(any(), any());

            assertEquals(1, ret.getServices().length);
        }

    }

    @Nested
    class BuildRemovePublicationDetailDto
    {
        @Test
        @DisplayName("Build remove publication detail Dto -> Ok")
        public void ok()
        {
            var deploymentPlan = new DeploymentPlan();

            AGMRemovePublicationDTO ret = apiGatewayDtoBuilder.buildRemovePublicationDetailDto(deploymentPlan);

            verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            assertNotNull(ret);
        }

    }

    @Nested
    class BuildUpdatePublicationDto
    {
        String releaseName = "releaseName";
        Platform destinationPlatformDeployType = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        @Test
        @DisplayName("Build update publication dto -> Neither created nor removed services without any batch")
        public void empty()
        {
            var newPlan = this.buildDeploymentPlan();
            var oldPlan = this.buildDeploymentPlan();

            when(builderUtils.getAllBatchesFromPlan(any())).thenReturn(Collections.emptyList());

            AGMUpdatePublicationDTO ret = apiGatewayDtoBuilder.buildUpdatePublicationDto(Collections.emptyList(), Collections.emptyList(), newPlan, oldPlan);

            InOrder inOrder = inOrder(builderUtils);
            verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(newPlan);
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(oldPlan);

            assertEquals(0, ret.getAddedServices().length);
            assertEquals(0, ret.getRemovedServicesIds().length);
            assertEquals(0, ret.getRemovedServicesLocation().length);
        }

        @Test
        @DisplayName("Build update publication dto -> Neither created nor removed services with batches in both new and old plans")
        public void withBatches()
        {
            var newBatchService = this.buildDeploymentService(909);
            var oldBatchService = this.buildDeploymentService(2408);
            String oldBatchServiceLocation = "oldBatchServiceLocation";
            var newPlan = this.buildDeploymentPlan();
            var oldPlan = this.buildDeploymentPlan();
            var batchServiceDTO = buidAGMServiceDTO();

            when(builderUtils.getAllBatchesFromPlan(any()))
                    .thenReturn(List.of(newBatchService))
                    .thenReturn(List.of(oldBatchService));
            when(builderUtils.buildServiceDto(any(), any())).thenReturn(Optional.of(batchServiceDTO));
            when(utils.buildServiceLocation(any(), any(), any(), any())).thenReturn(oldBatchServiceLocation);

            AGMUpdatePublicationDTO ret = apiGatewayDtoBuilder.buildUpdatePublicationDto(new ArrayList<>(), new ArrayList<>(), newPlan, oldPlan);

            InOrder inOrder = inOrder(builderUtils);
            inOrder.verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(newPlan);
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(oldPlan);
            inOrder.verify(builderUtils).buildServiceDto(any(), any());
            verify(utils).buildServiceLocation(oldBatchService.getService(), destinationPlatformDeployType.name(), releaseName, environment.getEnvironment());

            assertEquals(1, ret.getAddedServices().length);
            assertEquals(batchServiceDTO, ret.getAddedServices()[0]);
            assertEquals(1, ret.getRemovedServicesIds().length);
            assertEquals(oldBatchService.getService().getId(), ret.getRemovedServicesIds()[0]);
            assertEquals(1, ret.getRemovedServicesLocation().length);
            assertEquals(oldBatchServiceLocation, ret.getRemovedServicesLocation()[0]);

        }

        @Test
        @DisplayName("Build update publication dto -> Created and removed no microgateway services without any batch")
        public void withCreatedAndRemovedNoMicrogwServices()
        {
            var createService = this.buildDeploymentService(909);
            var removedService = this.buildDeploymentService(2408);
            String removedServiceLocation = "removedServiceLocation";
            var newPlan = this.buildDeploymentPlan();
            var oldPlan = this.buildDeploymentPlan();


            when(builderUtils.getAllBatchesFromPlan(any()))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Collections.emptyList());
            when(utils.buildServiceLocation(any(), any(), any(), any())).thenReturn(removedServiceLocation);

            AGMUpdatePublicationDTO ret = apiGatewayDtoBuilder.buildUpdatePublicationDto(List.of(createService), List.of(removedService), newPlan, oldPlan);

            InOrder inOrder = inOrder(builderUtils);
            verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(newPlan);
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(oldPlan);
            verify(builderUtils).buildServiceDto(any(), any());
            verify(utils).buildServiceLocation(removedService.getService(), destinationPlatformDeployType.name(), releaseName, environment.getEnvironment());

            assertEquals(0, ret.getAddedServices().length);
            assertEquals(1, ret.getRemovedServicesIds().length);
            assertEquals(removedService.getService().getId(), ret.getRemovedServicesIds()[0]);
            assertEquals(1, ret.getRemovedServicesLocation().length);
            assertEquals(removedServiceLocation, ret.getRemovedServicesLocation()[0]);

        }

        @Test
        @DisplayName("Build update publication dto -> Created and removed microgateway services without any batch")
        public void withCreatedAndRemovedMicrogwServices()
        {
            var createdService = this.buildDeploymentService(909);
            var removedDeploymentService = this.buildDeploymentService(2408);
            String removedServiceLocation = "removedServiceLocation";
            var newPlan = this.buildDeploymentPlan();
            var oldPlan = this.buildDeploymentPlan();
            var createdServiceDTO = buidAGMServiceDTO();

            when(builderUtils.getAllBatchesFromPlan(any()))
                    .thenReturn(Collections.emptyList())
                    .thenReturn(Collections.emptyList());
            when(builderUtils.buildServiceDto(any(), any())).thenReturn(Optional.of(createdServiceDTO));
            when(utils.buildServiceLocation(any(), any(), any(), any())).thenReturn(removedServiceLocation);

            AGMUpdatePublicationDTO ret = apiGatewayDtoBuilder.buildUpdatePublicationDto(List.of(createdService), List.of(removedDeploymentService), newPlan, oldPlan);

            InOrder inOrder = inOrder(builderUtils);
            verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(newPlan);
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(oldPlan);
            verify(builderUtils).buildServiceDto(any(), any());
            verify(utils).buildServiceLocation(removedDeploymentService.getService(), destinationPlatformDeployType.name(), releaseName, environment.getEnvironment());

            assertEquals(1, ret.getAddedServices().length);
            assertEquals(createdServiceDTO, ret.getAddedServices()[0]);
            assertEquals(1, ret.getRemovedServicesIds().length);
            assertEquals(removedDeploymentService.getService().getId(), ret.getRemovedServicesIds()[0]);
            assertEquals(1, ret.getRemovedServicesLocation().length);
            assertEquals(removedServiceLocation, ret.getRemovedServicesLocation()[0]);

        }

        @Test
        @DisplayName("Build update publication dto -> Created and removed microgateway services with batches")
        public void withCreatedAndRemovedMicrogwServicesAndBatches()
        {
            var newService = this.buildDeploymentService(610);
            var oldService = this.buildDeploymentService(1409);
            var createdService = this.buildDeploymentService(909);
            var removedService = this.buildDeploymentService(2408);
            String removedServiceLocation = "removedServiceLocation";
            String oldBatchServiceLocation = "oldBatchServiceLocation";
            var newPlan = this.buildDeploymentPlan();
            var oldPlan = this.buildDeploymentPlan();
            var batchServiceDTO = buidAGMServiceDTO();
            var createdServiceDTO = buidAGMServiceDTO();

            when(builderUtils.getAllBatchesFromPlan(any()))
                    .thenReturn(List.of(newService))
                    .thenReturn(List.of(oldService));
            when(builderUtils.buildServiceDto(any(), any()))
                    .thenReturn(Optional.of(createdServiceDTO))
                    .thenReturn(Optional.of(batchServiceDTO));
            when(utils.buildServiceLocation(any(), any(), any(), any()))
                    .thenReturn(removedServiceLocation)
                    .thenReturn(oldBatchServiceLocation);

            AGMUpdatePublicationDTO ret = apiGatewayDtoBuilder.buildUpdatePublicationDto(this.newList(createdService), this.newList(removedService), newPlan, oldPlan);

            InOrder inOrder = inOrder(builderUtils, utils);
            inOrder.verify(builderUtils).fillPublicationDetailInfo(any(), any(), any());
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(newPlan);
            inOrder.verify(builderUtils, calls(1)).getAllBatchesFromPlan(oldPlan);
            inOrder.verify(builderUtils, calls(1)).buildServiceDto(any(), any());
            inOrder.verify(builderUtils, calls(1)).buildServiceDto(any(), any());
            inOrder.verify(utils, calls(1)).buildServiceLocation(removedService.getService(), destinationPlatformDeployType.name(), releaseName, environment.getEnvironment());
            inOrder.verify(utils, calls(1)).buildServiceLocation(oldService.getService(), destinationPlatformDeployType.name(), releaseName, environment.getEnvironment());

            assertEquals(2, ret.getAddedServices().length);
            assertEquals(createdServiceDTO, ret.getAddedServices()[0]);
            assertEquals(batchServiceDTO, ret.getAddedServices()[1]);
            assertEquals(2, ret.getRemovedServicesIds().length);
            assertEquals(removedService.getService().getId(), ret.getRemovedServicesIds()[0]);
            assertEquals(oldService.getService().getId(), ret.getRemovedServicesIds()[1]);
            assertEquals(2, ret.getRemovedServicesLocation().length);
            assertEquals(removedServiceLocation, ret.getRemovedServicesLocation()[0]);
            assertEquals(oldBatchServiceLocation, ret.getRemovedServicesLocation()[1]);

        }

        private <T> List<T> newList(T... items)
        {
            return new ArrayList<T>(Arrays.asList(items));
        }


        DeploymentService buildDeploymentService(final Integer releaseVersionServiceId)
        {
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setId(releaseVersionServiceId);
            var deploymentService = new DeploymentService();
            deploymentService.setService(releaseVersionService);

            return deploymentService;
        }

        DeploymentPlan buildDeploymentPlan()
        {
            var deploymentPlan = new DeploymentPlan();
            deploymentPlan.setEnvironment(environment.getEnvironment());
            deploymentPlan.setSelectedDeploy(destinationPlatformDeployType);
            deploymentPlan.setReleaseVersion(this.buildReleaseVersion());

            return deploymentPlan;
        }

        ReleaseVersion buildReleaseVersion()
        {
            var release = new Release();
            release.setName(releaseName);
            var releaseVersion = new ReleaseVersion();
            releaseVersion.setRelease(release);

            return releaseVersion;
        }
    }

    @Nested
    class BuildDockerServiceDto
    {
        String uuaa = "JGMV";
        String releaseName = "releaseName";
        String groupId = "groupId";
        String artifactId = "artifactId";
        String version = "1.0.0";

        @Test
        @DisplayName("Build docker service dto -> For NOVA infrastructure ok")
        public void nova()
        {
            var deploymentService = Mockito.mock(DeploymentService.class, RETURNS_DEEP_STUBS);

            when(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa()).thenReturn(uuaa);
            when(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
            when(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.NOVA);
            when(deploymentService.getDeploymentSubsystem().getSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
            when(deploymentService.getService().getGroupId()).thenReturn(groupId);
            when(deploymentService.getService().getArtifactId()).thenReturn(artifactId);
            when(deploymentService.getService().getVersion()).thenReturn(version);

            AGMDockerServiceDTO ret = apiGatewayDtoBuilder.buildDockerServiceDto(deploymentService);

            assertEquals(uuaa, ret.getUuaa());
            assertEquals(releaseName, ret.getReleaseName());
            assertEquals("groupid-artifactid-releasename-1", ret.getServiceName());
            assertEquals(Platform.NOVA.name(), ret.getPlatform());

        }

        @Test
        @DisplayName("Build docker service dto -> For ETHER infrastructure ok")
        public void ether()
        {
            var serviceName = "serviceName";
            var env = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
            var deploymentService = Mockito.mock(DeploymentService.class, RETURNS_DEEP_STUBS);

            when(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa()).thenReturn(uuaa);
            when(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment()).thenReturn(env.getEnvironment());
            when(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
            when(deploymentService.getService().getGroupId()).thenReturn(groupId);
            when(deploymentService.getService().getArtifactId()).thenReturn(artifactId);
            when(deploymentService.getService().getVersion()).thenReturn(version);

            when(etherUtils.getEAEServiceURL(any(), any(), any(), any(), any())).thenReturn(serviceName);

            AGMDockerServiceDTO ret = apiGatewayDtoBuilder.buildDockerServiceDto(deploymentService);

            verify(etherUtils).getEAEServiceURL(env.getEnvironment(), groupId, artifactId, releaseName, version);

            assertEquals(uuaa, ret.getUuaa());
            assertEquals(releaseName, ret.getReleaseName());
            assertEquals(serviceName, ret.getServiceName());
            assertEquals(Platform.ETHER.name(), ret.getPlatform());
        }
    }

    @Nested
    class BuildCreateProfilingDto
    {
        String uuaa = "JGMV";

        @Test
        @DisplayName("Build create profiling dto -> Plan without roles")
        public void withoutRoles()
        {
            var deploymentPlan = Mockito.mock(DeploymentPlan.class, RETURNS_DEEP_STUBS);
            var release = Mockito.mock(Release.class, RETURNS_DEEP_STUBS);
            var apiMethodProfile = new ApiMethodProfile();
            var planProfile = new PlanProfile();
            planProfile.setApiMethodProfiles(List.of(apiMethodProfile));

            when(deploymentPlan.getReleaseVersion().getRelease()).thenReturn(release);
            when(deploymentPlan.getPlanProfiles()).thenReturn(List.of(planProfile));
            when(release.getProduct().getUuaa()).thenReturn(uuaa);

            AGMCreateProfilingDTO ret = apiGatewayDtoBuilder.buildCreateProfilingDto(deploymentPlan);

            assertEquals(uuaa, ret.getUuaa());
            assertEquals(0, ret.getProfilingInfo().length);
        }

        @Test
        @DisplayName("Build create profiling dto -> Plan with roles")
        public void withRoles()
        {
            var rol = "ROLE_XXX";
            var serviceName = "serviceName";
            var releaseName = "releaseName";

            var deploymentPlan = Mockito.mock(DeploymentPlan.class, RETURNS_DEEP_STUBS);
            var release = Mockito.mock(Release.class, RETURNS_DEEP_STUBS);

            var cesRole = new CesRole();
            cesRole.setRol(rol);
            var apiMethodProfile = new ApiMethodProfile();
            apiMethodProfile.setRoles(Collections.singleton(cesRole));
            var apiMethod = new ApiMethod();
            apiMethodProfile.setApiMethod(apiMethod);
            var planProfile = new PlanProfile();
            planProfile.setApiMethodProfiles(List.of(apiMethodProfile));

            when(deploymentPlan.getReleaseVersion().getRelease()).thenReturn(release);
            when(deploymentPlan.getPlanProfiles()).thenReturn(List.of(planProfile));
            when(release.getProduct().getUuaa()).thenReturn(uuaa);
            when(release.getName()).thenReturn(releaseName);

            when(builderUtils.buildProfilingResourceValue(any(), any())).thenReturn(serviceName);

            AGMCreateProfilingDTO ret = apiGatewayDtoBuilder.buildCreateProfilingDto(deploymentPlan);

            verify(builderUtils).buildProfilingResourceValue(releaseName, apiMethod);

            assertEquals(uuaa, ret.getUuaa());
            assertEquals(1, ret.getProfilingInfo().length);
            AGMProfilingInfoDTO profilingInfoDTO = ret.getProfilingInfo()[0];
            assertEquals(1, profilingInfoDTO.getRoles().length);
            assertEquals(rol, profilingInfoDTO.getRoles()[0]);
            assertEquals(serviceName, profilingInfoDTO.getName());
            assertEquals(serviceName, profilingInfoDTO.getValue());
        }
    }

    @Nested
    class BuildRemoveProfilingDto
    {
        String uuaa = "JGMV";

        @Test
        @DisplayName("Build remove profiling dto -> Plan without roles")
        public void withoutRoles()
        {
            var deploymentPlan = Mockito.mock(DeploymentPlan.class, RETURNS_DEEP_STUBS);
            var release = Mockito.mock(Release.class, RETURNS_DEEP_STUBS);
            var apiMethodProfile = new ApiMethodProfile();
            var planProfile = new PlanProfile();
            planProfile.setApiMethodProfiles(List.of(apiMethodProfile));

            when(deploymentPlan.getReleaseVersion().getRelease()).thenReturn(release);
            when(deploymentPlan.getPlanProfiles()).thenReturn(List.of(planProfile));
            when(release.getProduct().getUuaa()).thenReturn(uuaa);

            AGMRemoveProfilingDTO ret = apiGatewayDtoBuilder.buildRemoveProfilingDto(deploymentPlan);

            assertEquals(uuaa, ret.getUuaa());
            assertEquals(0, ret.getResources().length);
        }

        @Test
        @DisplayName("Build remove profiling dto -> Plan with roles")
        public void withRoles()
        {
            var serviceName = "serviceName";
            var releaseName = "releaseName";

            var deploymentPlan = Mockito.mock(DeploymentPlan.class, RETURNS_DEEP_STUBS);
            var release = Mockito.mock(Release.class, RETURNS_DEEP_STUBS);

            var apiMethodProfile = new ApiMethodProfile();
            apiMethodProfile.setRoles(Collections.singleton(new CesRole()));
            var apiMethod = new ApiMethod();
            apiMethodProfile.setApiMethod(apiMethod);
            var planProfile = new PlanProfile();
            planProfile.setApiMethodProfiles(List.of(apiMethodProfile));

            when(deploymentPlan.getReleaseVersion().getRelease()).thenReturn(release);
            when(deploymentPlan.getPlanProfiles()).thenReturn(List.of(planProfile));
            when(release.getProduct().getUuaa()).thenReturn(uuaa);
            when(release.getName()).thenReturn(releaseName);

            when(builderUtils.buildProfilingResourceValue(any(), any())).thenReturn(serviceName);

            AGMRemoveProfilingDTO ret = apiGatewayDtoBuilder.buildRemoveProfilingDto(deploymentPlan);

            verify(builderUtils).buildProfilingResourceValue(releaseName, apiMethod);

            assertEquals(uuaa, ret.getUuaa());
            assertEquals(1, ret.getResources().length);
            AGMProfilingResourceDTO profilingInfoDTO = ret.getResources()[0];
            assertEquals(serviceName, profilingInfoDTO.getName());
            assertEquals(serviceName, profilingInfoDTO.getValue());
        }

    }

    private AGMServiceDTO buidAGMServiceDTO()
    {
        var serviceApiDTO = new AGMServiceDTO();
        serviceApiDTO.fillRandomly(4, false, 0, 4);
        return serviceApiDTO;
    }
}