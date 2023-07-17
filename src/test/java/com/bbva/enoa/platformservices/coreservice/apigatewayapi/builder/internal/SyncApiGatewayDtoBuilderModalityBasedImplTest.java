package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMConsumedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncApiGatewayDtoBuilderModalityBasedImplTest
{
    @Mock
    private SecurizableApiGatewayDtoBuilderCommon securizableApiGatewayDtoBuilderCommon;
    @Mock
    private ReleaseVersionServiceRepository releaseVersionServiceRepo;
    @InjectMocks
    SyncApiGatewayDtoBuilderModalityBasedImpl apiGatewayDtoBuilderModalityBased;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyInteractions()
    {
        verifyNoMoreInteractions(
                securizableApiGatewayDtoBuilderCommon,
                releaseVersionServiceRepo
        );
    }

    @Nested
    class AddServedApiToServiceDto
    {
        String uuaa = "JGMV";
        String apiName = "apiName";
        String version = "1.0.0";
        String basePath = "basePath";
        String definitionFile = "Definition file";

        @Test
        @DisplayName("Add served API to service dto -> ok without backward")
        public void noBackwardOk()
        {
            var servedApiImplementation = Mockito.mock(SyncApiImplementation.class);
            var servedApiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var apiMethod = new ApiMethod(
                    Verb.values()[RandomUtils.nextInt(0, Verb.values().length)],
                    "endpoint",
                    "description",
                    servedApiVersion
            );

            when(servedApiImplementation.getApiVersion()).thenReturn(servedApiVersion);
            when(servedApiVersion.getApi().getUuaa()).thenReturn(uuaa);
            when(servedApiVersion.getApi().getName()).thenReturn(apiName);
            when(servedApiVersion.getVersion()).thenReturn(version);
            when(servedApiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);
            when(servedApiVersion.getDefinitionFile().getContents()).thenReturn(definitionFile);
            when(servedApiVersion.getApiMethods()).thenReturn(List.of(apiMethod));

            apiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            var servedApiDTOArgCaptor = ArgumentCaptor.forClass(AGMServedApiDTO.class);
            verify(serviceDTOBuilder).addServedApi(servedApiDTOArgCaptor.capture());

            var servedApiDTOCaptured = servedApiDTOArgCaptor.getValue();
            assertEquals(uuaa, servedApiDTOCaptured.getUuaa());
            assertEquals(apiName, servedApiDTOCaptured.getApiName());
            assertEquals(version, servedApiDTOCaptured.getApiVersion());
            assertEquals(basePath, servedApiDTOCaptured.getBasepath());
            assertEquals(Base64.getEncoder().encodeToString(definitionFile.getBytes()), servedApiDTOCaptured.getDefinitionFileContent());
            assertEquals(1, servedApiDTOCaptured.getEndpoints().length);
            var endpointDto = servedApiDTOCaptured.getEndpoints()[0];
            assertEquals(apiMethod.getVerb().getVerb(), endpointDto.getMethod());
            assertEquals(apiMethod.getEndpoint(), endpointDto.getPath());
        }

        @Test
        @DisplayName("Add served API to service dto -> ok with backward")
        public void withBackwardOk()
        {
            String backwardVersion = "0.0.5";
            var servedApiImplementation = Mockito.mock(SyncApiImplementation.class);
            var servedApiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var servedBackwardApiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var apiMethod = new ApiMethod(
                    Verb.values()[RandomUtils.nextInt(0, Verb.values().length)],
                    "endpoint",
                    "description",
                    servedApiVersion
            );

            when(servedApiImplementation.getApiVersion()).thenReturn(servedApiVersion);
            when(servedApiImplementation.getBackwardCompatibleApis()).thenReturn(List.of(servedBackwardApiVersion));
            when(servedApiVersion.getApi().getUuaa()).thenReturn(uuaa);
            when(servedApiVersion.getApi().getName()).thenReturn(apiName);
            when(servedApiVersion.getVersion()).thenReturn(version);
            when(servedApiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);
            when(servedApiVersion.getDefinitionFile().getContents()).thenReturn(definitionFile);
            when(servedApiVersion.getApiMethods()).thenReturn(List.of(apiMethod));
            when(servedBackwardApiVersion.getApi().getUuaa()).thenReturn(uuaa);
            when(servedBackwardApiVersion.getApi().getName()).thenReturn(apiName);
            when(servedBackwardApiVersion.getVersion()).thenReturn(backwardVersion);
            when(servedBackwardApiVersion.getApi().getBasePathSwagger()).thenReturn(basePath);
            when(servedBackwardApiVersion.getDefinitionFile().getContents()).thenReturn(definitionFile);
            when(servedBackwardApiVersion.getApiMethods()).thenReturn(List.of(apiMethod));

            apiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);

            var servedApiDTOArgCaptor = ArgumentCaptor.forClass(AGMServedApiDTO.class);
            verify(serviceDTOBuilder, times(2)).addServedApi(servedApiDTOArgCaptor.capture());

            var servedApiDTOCaptured = servedApiDTOArgCaptor.getAllValues().get(0);
            assertEquals(uuaa, servedApiDTOCaptured.getUuaa());
            assertEquals(apiName, servedApiDTOCaptured.getApiName());
            assertEquals(version, servedApiDTOCaptured.getApiVersion());
            assertEquals(basePath, servedApiDTOCaptured.getBasepath());
            assertEquals(Base64.getEncoder().encodeToString(definitionFile.getBytes()), servedApiDTOCaptured.getDefinitionFileContent());
            assertEquals(1, servedApiDTOCaptured.getEndpoints().length);
            var endpointDto = servedApiDTOCaptured.getEndpoints()[0];
            assertEquals(apiMethod.getVerb().getVerb(), endpointDto.getMethod());
            assertEquals(apiMethod.getEndpoint(), endpointDto.getPath());

            var servedBackwardApiDTOCaptured = servedApiDTOArgCaptor.getAllValues().get(1);
            assertEquals(uuaa, servedBackwardApiDTOCaptured.getUuaa());
            assertEquals(apiName, servedBackwardApiDTOCaptured.getApiName());
            assertEquals(backwardVersion, servedBackwardApiDTOCaptured.getApiVersion());
            assertEquals(basePath, servedBackwardApiDTOCaptured.getBasepath());
            assertEquals(Base64.getEncoder().encodeToString(definitionFile.getBytes()), servedBackwardApiDTOCaptured.getDefinitionFileContent());
            assertEquals(1, servedBackwardApiDTOCaptured.getEndpoints().length);
            var backwardEndpointDto = servedBackwardApiDTOCaptured.getEndpoints()[0];
            assertEquals(apiMethod.getVerb().getVerb(), backwardEndpointDto.getMethod());
            assertEquals(apiMethod.getEndpoint(), backwardEndpointDto.getPath());
        }
    }

    @Nested
    class AddConsumedApiToServiceDto
    {
        String infrastructure = "infrastructure";
        String environment = "environment";

        @Test
        @DisplayName("Add consumed API to service dto -> external API")
        public void external()
        {
            var consumedApiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiDTO = new AGMConsumedApiDTO();
            consumedApiDTO.fillRandomly(4, false, 0, 4);

            when(consumedApiVersion.getApi().getType()).thenReturn(ApiType.EXTERNAL);

            when(securizableApiGatewayDtoBuilderCommon.buildExternalConsumedApi(any())).thenReturn(consumedApiDTO);

            apiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(consumedApiVersion, infrastructure, environment, serviceDTOBuilder);

            var consumedApiDTOArgCaptor = ArgumentCaptor.forClass(AGMConsumedApiDTO.class);
            verify(securizableApiGatewayDtoBuilderCommon).buildExternalConsumedApi(consumedApiVersion);
            verify(serviceDTOBuilder).addExternalConsumedApis(consumedApiDTOArgCaptor.capture());
            assertEquals(consumedApiDTO, consumedApiDTOArgCaptor.getValue());
        }

        @ParameterizedTest
        @EnumSource(value = ApiType.class, mode = EnumSource.Mode.EXCLUDE, names = {"EXTERNAL"})
        @DisplayName("Add consumed API to service dto -> no external API deployed in same infrastructure")
        public void noExternalSameInfrastructure(ApiType apiType)
        {
            var id = 909;
            var consumedApiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiDTO = new AGMConsumedApiDTO();
            consumedApiDTO.fillRandomly(4, false, 0, 4);
            var releaseVersionService = new ReleaseVersionService();

            when(consumedApiVersion.getId()).thenReturn(id);
            when(consumedApiVersion.getApi().getType()).thenReturn(apiType);

            when(releaseVersionServiceRepo.findAllDeployedImplementations(any())).thenReturn(List.of(releaseVersionService));
            when(securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(any(), any(), any())).thenReturn(true);
            when(securizableApiGatewayDtoBuilderCommon.buildConsumedApi(any(), any(), any(), any())).thenReturn(consumedApiDTO);

            apiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(consumedApiVersion, infrastructure, environment, serviceDTOBuilder);

            var consumedApiDTOArgCaptor = ArgumentCaptor.forClass(AGMConsumedApiDTO.class);
            verify(releaseVersionServiceRepo).findAllDeployedImplementations(id);
            verify(securizableApiGatewayDtoBuilderCommon).isServiceConfiguredToBeDeployedOnInfrastructure(infrastructure, releaseVersionService, environment);
            verify(securizableApiGatewayDtoBuilderCommon).buildConsumedApi(consumedApiVersion, releaseVersionService, infrastructure, environment);
            verify(serviceDTOBuilder).addConsumedApis(consumedApiDTOArgCaptor.capture());
            assertEquals(consumedApiDTO, consumedApiDTOArgCaptor.getValue());
        }

        @ParameterizedTest
        @EnumSource(value = ApiType.class, mode = EnumSource.Mode.EXCLUDE, names = {"EXTERNAL"})
        @DisplayName("Add consumed API to service dto -> no external API deployed in different infrastructure")
        public void noExternalDifferentInfrastructure(ApiType apiType)
        {
            var id = 909;
            var consumedApiVersion = Mockito.mock(SyncApiVersion.class, RETURNS_DEEP_STUBS);
            var serviceDTOBuilder = Mockito.mock(AGMServiceDtoBuilder.class);
            var consumedApiDTO = new AGMConsumedApiDTO();
            consumedApiDTO.fillRandomly(4, false, 0, 4);
            var releaseVersionService = new ReleaseVersionService();

            when(consumedApiVersion.getId()).thenReturn(id);
            when(consumedApiVersion.getApi().getType()).thenReturn(apiType);

            when(releaseVersionServiceRepo.findAllDeployedImplementations(any())).thenReturn(List.of(releaseVersionService));
            when(securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(any(), any(), any())).thenReturn(false);
            when(securizableApiGatewayDtoBuilderCommon.buildNovaExternalConsumedApi(any(), any())).thenReturn(consumedApiDTO);

            apiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(consumedApiVersion, infrastructure, environment, serviceDTOBuilder);

            var consumedApiDTOArgCaptor = ArgumentCaptor.forClass(AGMConsumedApiDTO.class);
            verify(releaseVersionServiceRepo).findAllDeployedImplementations(id);
            verify(securizableApiGatewayDtoBuilderCommon).isServiceConfiguredToBeDeployedOnInfrastructure(infrastructure, releaseVersionService, environment);
            verify(securizableApiGatewayDtoBuilderCommon).buildNovaExternalConsumedApi(consumedApiVersion, releaseVersionService);
            verify(serviceDTOBuilder).addExternalNovaConsumedApis(consumedApiDTOArgCaptor.capture());
            assertEquals(consumedApiDTO, consumedApiDTOArgCaptor.getValue());
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> SYNC")
        public void backToBack()
        {
            assertTrue(apiGatewayDtoBuilderModalityBased.isModalitySupported(ApiModality.SYNC));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"ASYNC_BACKTOFRONT", "ASYNC_BACKTOBACK"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(apiGatewayDtoBuilderModalityBased.isModalitySupported(modality));
        }
    }

}