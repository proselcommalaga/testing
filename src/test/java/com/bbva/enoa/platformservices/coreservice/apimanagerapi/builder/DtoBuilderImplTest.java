package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDetailDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal.IDtoBuilderModalityBased;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiImplementationRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DtoBuilderImplTest
{

    @Mock
    private ToolsClient toolsClient;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private ApiImplementationRepository apiImplementationRepository;
    @Mock
    private IDtoBuilderModalityBased dtoBuilderModalityBased;
    @Mock
    private IApiManagerValidator apiManagerValidatorService;
    @InjectMocks
    private DtoBuilderImpl dtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                this.toolsClient,
                this.deploymentPlanRepository,
                this.apiImplementationRepository,
                this.dtoBuilderModalityBased,
                this.apiManagerValidatorService
        );
    }

    @Nested
    class BuildApiPlanDetailDto
    {

        @Test
        @DisplayName("Get API plan detail -> Plan without subsystem")
        void noSubsystem()
        {
            // Given
            final ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setSubsystems(Collections.emptyList());

            final DeploymentPlan deploymentPlan = new DeploymentPlan();
            deploymentPlan.setPlanProfiles(Collections.singletonList(new PlanProfile()));
            deploymentPlan.setReleaseVersion(releaseVersion);

            final ApiPlanDetailDto expectedApiPlanDetailDto = new ApiPlanDetailDto();

            // When
            ApiPlanDetailDto obtainedApiPlanDetailDto = dtoBuilder.buildApiPlanDetailDto(deploymentPlan, null);

            // Then
            assertEquals(expectedApiPlanDetailDto, obtainedApiPlanDetailDto);
        }

        @ParameterizedTest
        @ArgumentsSource(DtoBuilderImplTest.BuildApiPlanDetailDtoArgumentsProvider.class)
        @DisplayName("Get API plan detail -> Full fill plan")
        void ok(final DeploymentStatus deploymentStatus, final ProfileStatus profileStatus, final ApiPolicyStatus apiPolicyStatus)
        {
            // Given
            final SyncApi syncApi = new SyncApi();
            syncApi.setName(RandomStringUtils.randomAlphanumeric(15));
            syncApi.setPolicyStatus(apiPolicyStatus);

            final SyncApiVersion apiVersion = new SyncApiVersion();
            apiVersion.setApi(syncApi);
            apiVersion.setVersion(RandomStringUtils.randomNumeric(5));

            final SyncApiImplementation apiImplementation = new SyncApiImplementation();
            apiImplementation.setApiVersion(apiVersion);
            apiImplementation.setImplementedAs(ImplementedAs.SERVED);

            final List<ServiceType> apiRestServiceTypes = Stream.of(ServiceType.values())
                    .filter(ServiceType::isRest)
                    .collect(Collectors.toList());

            final ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceType(apiRestServiceTypes.get(RandomUtils.nextInt(0, apiRestServiceTypes.size())).getServiceType());
            releaseVersionService.setServiceName(RandomStringUtils.randomAlphanumeric(25));
            releaseVersionService.setServers(Collections.singletonList(apiImplementation));

            final ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setSubsystemId(RandomUtils.nextInt(0, 10000));
            releaseVersionSubsystem.setServices(Collections.singletonList(releaseVersionService));

            final Release release = new Release();
            release.setName(RandomStringUtils.randomAlphanumeric(25));

            final ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setSubsystems(List.of(releaseVersionSubsystem));
            releaseVersion.setRelease(release);

            final PlanProfile planProfile = new PlanProfile();
            planProfile.setStatus(profileStatus);
            planProfile.setActivationDate(new Date());

            final DeploymentPlan deploymentPlan = new DeploymentPlan();
            deploymentPlan.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
            deploymentPlan.setReleaseVersion(releaseVersion);
            deploymentPlan.setStatus(deploymentStatus);
            deploymentPlan.setPlanProfiles(Collections.singletonList(planProfile));

            final CesRole cesRole = new CesRole();
            cesRole.setId(RandomUtils.nextInt(0, 10000));
            cesRole.setRol(RandomStringUtils.randomAlphanumeric(15));

            final CesRole[] cesRolesArray = {cesRole};

            final TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
            toSubsystemDTO.setSubsystemName(RandomStringUtils.randomAlphanumeric(15));

            final ApiPlanDto apiPlanDto = new ApiPlanDto();

            when(toolsClient.getSubsystemById(eq(releaseVersionSubsystem.getSubsystemId())))
                    .thenReturn(toSubsystemDTO);
            when(dtoBuilderModalityBased.buildApiPlanDto(eq(apiImplementation.getApiVersion()), eq(deploymentPlan.getReleaseVersion().getRelease().getName()), eq(planProfile)))
                    .thenReturn(apiPlanDto);

            // When
            final ApiPlanDetailDto result = dtoBuilder.buildApiPlanDetailDto(deploymentPlan, cesRolesArray);

            // Then
            verify(toolsClient).getSubsystemById(releaseVersionSubsystem.getSubsystemId());
            verify(dtoBuilderModalityBased).buildApiPlanDto(apiImplementation.getApiVersion(), deploymentPlan.getReleaseVersion().getRelease().getName(), planProfile);

            assertEquals(1, result.getApiSubsystems().length);
            assertEquals(toSubsystemDTO.getSubsystemName(), result.getApiSubsystems()[0].getName());

            assertEquals(1, result.getApiSubsystems()[0].getApiServices().length);
            assertEquals(releaseVersionService.getServiceName(), result.getApiSubsystems()[0].getApiServices()[0].getName());

            assertEquals(1, result.getApiSubsystems()[0].getApiServices()[0].getApis().length);

            assertEquals(1, result.getRoles().length);
            assertEquals(cesRole.getId(), result.getRoles()[0].getRoleId());
            assertEquals(cesRole.getRol(), result.getRoles()[0].getRoleName());

            assertEquals(ProfileStatus.ACTIVE.equals(planProfile.getStatus()), result.getActive());

            if (ProfileStatus.ACTIVE.equals(profileStatus))
            {
                assertEquals(planProfile.getActivationDate().toString(), result.getActivationDate());
            }
            else
            {
                assertNull(result.getActivationDate());
            }

            assertEquals(DeploymentStatus.DEFINITION.equals(deploymentStatus), result.getEditable());
        }

    }

    @Nested
    class BuildApiPlanDto
    {
        @Test
        @DisplayName("Build API plan DTO -> ok")
        public void ok()
        {
            // Given
            final ApiVersion<?, ?, ?> apiVersion = getApiVersion();
            final String releaseName = RandomStringUtils.randomAlphanumeric(15);
            final PlanProfile planProfile = new PlanProfile();

            ApiPlanDto expectedApiPlanDto = new ApiPlanDto();
            expectedApiPlanDto.fillRandomly(3, false, 1, 6);

            when(dtoBuilderModalityBased.buildApiPlanDto(any(), any(), any()))
                    .thenReturn(expectedApiPlanDto);

            // When
            ApiPlanDto returnedApiPlanDto = dtoBuilder.buildApiPlanDto(apiVersion, releaseName, planProfile);

            // Then
            assertEquals(expectedApiPlanDto, returnedApiPlanDto);

            verify(dtoBuilderModalityBased).buildApiPlanDto(apiVersion, releaseName, planProfile);
        }

    }

    @Nested
    class BuildApiDetailDto
    {
        @Test
        @DisplayName("Build API detail DTO -> ok")
        public void ok()
        {
            // Given
            final Api<?, ?, ?> api = getApi();

            ApiDetailDto expectedApiDetailDto = new ApiDetailDto();
            expectedApiDetailDto.fillRandomly(3, false, 1, 6);

            when(dtoBuilderModalityBased.buildApiDetailDto(any())).thenReturn(expectedApiDetailDto);

            // When
            ApiDetailDto returnedApiDetailDto = dtoBuilder.buildApiDetailDto(api);

            // Then
            assertEquals(expectedApiDetailDto, returnedApiDetailDto);

            verify(dtoBuilderModalityBased).buildApiDetailDto(api);
        }

    }

    @Nested
    class BuildApiVersionDetailDtoArray
    {
        @Test
        @DisplayName("Build API version detail DTO -> no api implementation")
        public void noApiImplementation()
        {
            // Given
            final ApiVersion<?, ?, ?> apiVersion = getApiVersion();
            apiVersion.setId(RandomUtils.nextInt(0, 10000));

            when(apiImplementationRepository.findApiImplementationByReadyReleaseVersion(any()))
                    .thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(any()))
                    .thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsConsumingApiVersion(any()))
                    .thenReturn(Collections.emptyList());

            // When
            final ApiVersionDetailDto returnedApiVersionDetailDto = dtoBuilder.buildApiVersionDetailDto(apiVersion,"all");
            final ApiVersionDetailDto returnedApiVersionDetailDtodeployed = dtoBuilder.buildApiVersionDetailDto(apiVersion,"deployed");
            // Then
            assertEquals(0, returnedApiVersionDetailDto.getUsages().length);
            assertEquals(0, returnedApiVersionDetailDtodeployed.getUsages().length);

            verify(apiImplementationRepository,times(2)).findApiImplementationByReadyReleaseVersion(apiVersion.getId());
            verify(apiImplementationRepository,times(2)).findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId());
            verify(apiImplementationRepository,times(2)).findApiImplementationsConsumingApiVersion(apiVersion.getId());
        }

        @Test
        @DisplayName("Build API version detail DTO -> with implementation by ready release version")
        public void withApiImplementationByReadyReleaseVersion()
        {
            // Given
            final ApiVersion<?, ?, ?> apiVersion = getApiVersion();
            apiVersion.setId(RandomUtils.nextInt(0, 10000));
            final Release release = new Release();
            release.setName(RandomStringUtils.randomAlphanumeric(25));
            Product product = new Product();
            product.setUuaa("UUAA");
            release.setProduct(product);

            final ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setId(RandomUtils.nextInt(0, 10000));
            releaseVersion.setVersionName(RandomStringUtils.randomAlphanumeric(15));
            releaseVersion.setRelease(release);

            final ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);

            final ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceName(RandomStringUtils.randomAlphanumeric(25));
            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);

            final SyncApiImplementation apiImplementation = new SyncApiImplementation();
            apiImplementation.setService(releaseVersionService);
            apiImplementation.setImplementedAs(ImplementedAs.values()[RandomUtils.nextInt(0, ImplementedAs.values().length)]);

            when(apiImplementationRepository.findApiImplementationByReadyReleaseVersion(any()))
                    .thenReturn(Collections.singletonList(apiImplementation));
            when(apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(any()))
                    .thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsConsumingApiVersion(any()))
                    .thenReturn(Collections.emptyList());

            final boolean deployedOnInt = true; // para que al menos haya uno desplegado en un entorno
            final boolean deployedOnPre = RandomUtils.nextBoolean();
            final boolean deployedOnPro = RandomUtils.nextBoolean();

            when(deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(eq(releaseVersion.getId()), eq(Environment.INT.getEnvironment()), eq(DeploymentStatus.DEPLOYED)))
                    .thenReturn(deployedOnInt ? Collections.singletonList(new DeploymentPlan()) : Collections.emptyList());
            when(deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(eq(releaseVersion.getId()), eq(Environment.PRE.getEnvironment()), eq(DeploymentStatus.DEPLOYED)))
                    .thenReturn(deployedOnPre ? Collections.singletonList(new DeploymentPlan()) : Collections.emptyList());
            when(deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(eq(releaseVersion.getId()), eq(Environment.PRO.getEnvironment()), eq(DeploymentStatus.DEPLOYED)))
                    .thenReturn(deployedOnPro ? Collections.singletonList(new DeploymentPlan()) : Collections.emptyList());

            // When
            final ApiVersionDetailDto returnedApiVersionDetailDto = dtoBuilder.buildApiVersionDetailDto(apiVersion, "all");
            // Then
            assertEquals(1, returnedApiVersionDetailDto.getUsages().length);
            assertEquals(releaseVersionService.getServiceName(), returnedApiVersionDetailDto.getUsages()[0].getServiceName());
            assertEquals(releaseVersion.getVersionName(), returnedApiVersionDetailDto.getUsages()[0].getReleaseVersionName());
            assertEquals(release.getName(), returnedApiVersionDetailDto.getUsages()[0].getReleaseName());
            assertEquals(apiImplementation.getImplementedAs().getImplementedAs(), returnedApiVersionDetailDto.getUsages()[0].getImplementedAs());

            final ApiVersionDetailDto returnedApiVersionDetailDtoDeployed = dtoBuilder.buildApiVersionDetailDto(apiVersion, "deployed");
            // Then
            assertEquals(1, returnedApiVersionDetailDtoDeployed.getUsages().length);
            assertEquals(releaseVersionService.getServiceName(), returnedApiVersionDetailDtoDeployed.getUsages()[0].getServiceName());
            assertEquals(releaseVersion.getVersionName(), returnedApiVersionDetailDtoDeployed.getUsages()[0].getReleaseVersionName());
            assertEquals(release.getName(), returnedApiVersionDetailDtoDeployed.getUsages()[0].getReleaseName());
            assertEquals(apiImplementation.getImplementedAs().getImplementedAs(), returnedApiVersionDetailDtoDeployed.getUsages()[0].getImplementedAs());

            verify(apiImplementationRepository,times(2)).findApiImplementationByReadyReleaseVersion(apiVersion.getId());
            verify(apiImplementationRepository,times(2)).findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId());
            verify(apiImplementationRepository,times(2)).findApiImplementationsConsumingApiVersion(apiVersion.getId());
            verify(deploymentPlanRepository,times(2)).getByReleaseVersionAndEnvironmentAndStatus(releaseVersion.getId(), Environment.INT.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(deploymentPlanRepository,times(2)).getByReleaseVersionAndEnvironmentAndStatus(releaseVersion.getId(), Environment.PRE.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(deploymentPlanRepository,times(2)).getByReleaseVersionAndEnvironmentAndStatus(releaseVersion.getId(), Environment.PRO.getEnvironment(), DeploymentStatus.DEPLOYED);


        }
    }

    @Nested
    class BuildApiDtoArray
    {
        @Test
        @DisplayName("Get APIs detail of product -> no APIs found for product")
        public void noApisForProduct()
        {
            // Given
            final int productId = RandomUtils.nextInt(0, 10000);

            when(apiManagerValidatorService.filterByProductId(anyInt())).thenReturn(Collections.emptyList());

            // When
            final ApiDto[] returnValue = dtoBuilder.buildApiDtoArray(productId);

            // Then
            verify(apiManagerValidatorService).filterByProductId(productId);

            assertEquals(0, returnValue.length);
        }

        @Test
        @DisplayName("Get APIs detail of product -> OK")
        public void ok()
        {
            // Given
            final int productId = RandomUtils.nextInt(0, 10000);

            final List<Api<?, ?, ?>> apiList = Arrays.asList(
                    this.buildSyncApi(2, "b", "JGMV", ApiType.NOT_GOVERNED),
                    this.buildSyncApi(3, "c", "jgmv", ApiType.EXTERNAL),
                    this.buildSyncApi(1, "a", null, ApiType.GOVERNED)
            );

            when(apiManagerValidatorService.filterByProductId(anyInt()))
                    .thenReturn(apiList);

            // When
            final ApiDto[] returnedApiDto = dtoBuilder.buildApiDtoArray(productId);

            // Then
            verify(apiManagerValidatorService).filterByProductId(productId);
            assertEquals(3, returnedApiDto.length);

            //Fist
            assertEquals(1, returnedApiDto[0].getId());
            assertEquals("a", returnedApiDto[0].getApiName());
            assertEquals(ApiType.GOVERNED.getApiType(), returnedApiDto[0].getApiType());
            assertNull(returnedApiDto[0].getUuaa());
            //Second
            assertEquals(2, returnedApiDto[1].getId());
            assertEquals("b", returnedApiDto[1].getApiName());
            assertEquals(ApiType.NOT_GOVERNED.getApiType(), returnedApiDto[1].getApiType());
            assertEquals("JGMV", returnedApiDto[1].getUuaa());
            //Third
            assertEquals(3, returnedApiDto[2].getId());
            assertEquals("c", returnedApiDto[2].getApiName());
            assertEquals(ApiType.EXTERNAL.getApiType(), returnedApiDto[2].getApiType());
            assertEquals("JGMV", returnedApiDto[2].getUuaa());
        }

        private Api<?, ?, ?> buildSyncApi(int id, String apiName, String uuaa, ApiType apiType)
        {
            SyncApi syncApi = new SyncApi();
            syncApi.setType(apiType);
            syncApi.setName(apiName);
            syncApi.setUuaa(uuaa);
            syncApi.setId(id);
            return syncApi;
        }

    }

    static class BuildApiPlanDetailDtoArgumentsProvider implements ArgumentsProvider
    {

        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context)
        {
            return Stream.of(
                    Arguments.of(DeploymentStatus.DEFINITION, ProfileStatus.ACTIVE, ApiPolicyStatus.ESTABLISHED),
                    Arguments.of(DeploymentStatus.DEFINITION, ProfileStatus.ACTIVE, ApiPolicyStatus.PENDING),
                    Arguments.of(DeploymentStatus.DEFINITION, ProfileStatus.ACTIVE, ApiPolicyStatus.REJECTED),
                    Arguments.of(DeploymentStatus.DEPLOYED, ProfileStatus.ACTIVE, ApiPolicyStatus.ESTABLISHED),
                    Arguments.of(DeploymentStatus.DEPLOYED, ProfileStatus.PENDING, ApiPolicyStatus.ESTABLISHED),
                    Arguments.of(DeploymentStatus.DEPLOYED, ProfileStatus.DEPRECATED, ApiPolicyStatus.ESTABLISHED),
                    Arguments.of(DeploymentStatus.DEPLOYED, ProfileStatus.DEPRECATED, ApiPolicyStatus.ESTABLISHED)
            );
        }
    }

    private Api<?, ?, ?> getApi()
    {
        return new Api()
        {
            @Override
            public List<ApiVersion<?, ?, ?>> getApiVersions()
            {
                return null;
            }

            @Override
            public boolean hasMissingDocumentation()
            {
                return false;
            }

            @Override
            public ApiModality getApiModality()
            {
                return null;
            }

            @Override
            public String getBasePathSwagger()
            {
                return null;
            }
        };
    }

    private ApiVersion<?, ?, ?> getApiVersion()
    {
        return new ApiVersion()
        {
            @Override
            public Api<?, ?, ?> getApi()
            {
                return null;
            }

            @Override
            public void setApi(final Api api)
            {

            }

            @Override
            public ApiModality getApiModality()
            {
                return null;
            }

            @Override
            public List<?> getApiImplementations()
            {
                return null;
            }
        };
    }


}