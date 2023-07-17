package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IProjectFileValidator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceDtoBuilderImplTest
{
    @Mock
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;
    @Mock
    private IProjectFileValidator iProjectFileValidator;
    @InjectMocks
    private ServiceDtoBuilderImpl serviceDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void buildServicesFromSubsystemTag()
    {
        List<String> projectNames = new ArrayList<>();
        projectNames.add("project");
        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(1, "TAG")).thenReturn(projectNames);
        SubsystemTagDto tag = new SubsystemTagDto();
        tag.setTagName("TAG");
        tag.setValidationErrors(new ValidationErrorDto[0]);
        List<String> dependencies = new ArrayList<>();
        dependencies.add("project");
        when(this.iVersioncontrolsystemClient.getDependencies(1, "TAG")).thenReturn(dependencies);
        List<NewReleaseVersionServiceDto> response = this.serviceDtoBuilder.buildServicesFromSubsystemTag(1, tag, SubsystemType.NOVA,
                "CODE", "release", buildProduct(), "subsystemTest");
        Assertions.assertEquals("Service with errors parsing pom.xml", response.get(0).getServiceName());

        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(1, "TAG")).thenReturn(new ArrayList<>());
        List<NewReleaseVersionServiceDto> response2 = this.serviceDtoBuilder.buildServicesFromSubsystemTag(1, tag, SubsystemType.NOVA,
                "CODE", "release", buildProduct(), "subsystemTest");
        Assertions.assertEquals(0, response2.size());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class BuildServiceGeneralInfo
    {
        @Test
        @DisplayName("(BuildServiceGeneralInfo) -> Is createServiceFromPom green path running?")
        void buildServiceGeneralInfoFromPom()
        {
            // given
            String releaseName = "releaseName";
            ValidatorInputs validatorInputs = mock(ValidatorInputs.class);
            PomXML pomXML = mock(PomXML.class);
            Properties properties = mock(Properties.class);

            // when
            when(validatorInputs.getPom()).thenReturn(pomXML);
            when(validatorInputs.isLatestVersion()).thenReturn(false);
            when(pomXML.isPomValid()).thenReturn(true);
            when(pomXML.getArtifactId()).thenReturn("artifactId");
            when(pomXML.getVersion()).thenReturn("version");
            when(pomXML.getGroupId()).thenReturn("groupId");
            when(pomXML.getDescription()).thenReturn("description");
            when(pomXML.getName()).thenReturn("name");
            when(pomXML.getFinalName()).thenReturn("finalName");
            when(pomXML.getProperties()).thenReturn(properties);

            when(properties.getProperty(anyString())).thenReturn(ServiceType.NOVA.getServiceType());

            // then
            Assertions.assertDoesNotThrow(() -> serviceDtoBuilder.buildServiceGeneralInfo(releaseName, validatorInputs));
        }

        @Test
        void when_service_has_jdk_and_jvm_version_then_return_jdk_and_jvm_in_response()
        {
            final NovaYml novaYml = getDummyNovaYml("11.0.7", "Dummy JDK");
            final NewReleaseVersionServiceDto result = serviceDtoBuilder.buildServiceGeneralInfo("Release", getDummyValidatorInputs(novaYml));

            Assertions.assertEquals("11.0.7", result.getJvmVersion());
            Assertions.assertEquals("Dummy JDK", result.getJdkVersion());
        }

        @Test
        void when_service_has_empty_jdk_then_return_response_without_jdk_and_jvm()
        {
            final NovaYml novaYml = getDummyNovaYml("11.0.7", "");
            final NewReleaseVersionServiceDto result = serviceDtoBuilder.buildServiceGeneralInfo("Release", getDummyValidatorInputs(novaYml));

            Assertions.assertNull(result.getJvmVersion());
            Assertions.assertNull(result.getJdkVersion());
        }

        @Test
        void when_service_has_null_jdk_then_return_response_without_jdk_and_jvm()
        {
            final NovaYml novaYml = getDummyNovaYml("11.0.7", null);
            final NewReleaseVersionServiceDto result = serviceDtoBuilder.buildServiceGeneralInfo("Release", getDummyValidatorInputs(novaYml));

            Assertions.assertNull(result.getJvmVersion());
            Assertions.assertNull(result.getJdkVersion());
        }
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class BuildServiceGenerics
    {
        @ParameterizedTest
        @MethodSource("argumentsTest")
        @DisplayName("(BuildServiceGenerics) -> test every type of service on green path")
        void testBuildServiceGenerics(ServiceType serviceType)
        {
            // given
            String[] modules = new String[]{};
            String tagUrl = "tagUrl";
            String projectPath = "projectPath";
            NewReleaseVersionServiceDto serviceDto = mock(NewReleaseVersionServiceDto.class);

            // when
            when(serviceDto.getServiceType()).thenReturn(serviceType.getServiceType());

            // then
            Assertions.assertDoesNotThrow(() -> serviceDtoBuilder.buildServiceGenerics(modules, tagUrl, projectPath, serviceDto));
        }

        private Stream<Arguments> argumentsTest() {
            return Stream.of(
                    arguments(ServiceType.LIBRARY_JAVA),
                    arguments(ServiceType.LIBRARY_NODE),
                    arguments(ServiceType.LIBRARY_TEMPLATE),
                    arguments(ServiceType.LIBRARY_PYTHON),
                    arguments(ServiceType.BATCH_SCHEDULER_NOVA),
                    arguments(ServiceType.INVALID)
            );
        }
    }

    private static Product buildProduct()
    {

        Product product = new Product();
        product.setUuaa("TEST");
        return product;
    }

    private static ValidatorInputs getDummyValidatorInputs(NovaYml novaYml)
    {
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);
        validatorInputs.setLatestVersion(true);
        return validatorInputs;
    }

    private static NovaYml getDummyNovaYml(String jvmVersion, String jdkVersion)
    {
        NovaYml novaYml = new NovaYml();
        novaYml.setUuaa("UUAA");
        novaYml.setName("NAME");
        novaYml.setServiceType("API_REST_JAVA_SPRING_BOOT");
        novaYml.setVersion("1.0.0");
        novaYml.setNovaVersion("1.0");
        novaYml.setDescription("Description");
        novaYml.setLanguageVersion(jvmVersion);
        novaYml.setJdkVersion(jdkVersion);
        return novaYml;
    }
}