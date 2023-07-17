
//TODO@mpaz arreglar test
//package com.bbva.enoa.platformservices.coreservice.apigatewayapi.services;
//
//import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.*;
//import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
//import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
//import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
//import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
//import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.IApiGatewayDtoBuilder;
//import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.EtherUtils;
//import com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator.IApiGatewayValidator;
//import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
//import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
//import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
//import org.apache.commons.lang3.RandomUtils;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtensionContext;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.*;
//import org.mockito.*;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//public class ApiGatewayServiceImplTest
//{
//
//
//    @Mock
//    private IApiGatewayDtoBuilder apiGatewayServiceDtoBuilder;
//    @Mock
//    private IApiGatewayValidator apiGatewayValidator;
//    @Mock
//    private IApiGatewayManagerClient apiGatewayManagerClient;
//    @Mock
//    private EtherUtils etherUtils;
//    @Mock
//    private DeploymentPlanRepository deploymentPlanRepository;
//    @InjectMocks
//    private ApiGatewayServiceImpl apiGatewayService;
//
//
//    @BeforeEach
//    public void setUp()
//    {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @AfterEach
//    public void verifyAllMocks()
//    {
//        verifyNoMoreInteractions(apiGatewayServiceDtoBuilder);
//        verifyNoMoreInteractions(apiGatewayValidator);
//        verifyNoMoreInteractions(apiGatewayManagerClient);
//        verifyNoMoreInteractions(etherUtils);
//        verifyNoMoreInteractions(deploymentPlanRepository);
//    }
//
//    @Nested
//    class CreatePublication
//    {
//        @ParameterizedTest(name = "[{index}] serviceList: {0}")
//        @NullAndEmptySource
//        @DisplayName("Create publication -> empty services list")
//        public void emptyServices(AGMServiceDTO[] serviceList)
//        {
//            DeploymentPlan deploymentPlan = new DeploymentPlan();
//            AGMCreatePublicationDTO createPublicationDTO = new AGMCreatePublicationDTO();
//            createPublicationDTO.setServices(serviceList);
//
//            when(apiGatewayServiceDtoBuilder.buildCreatePublicationDto(any())).thenReturn(createPublicationDTO);
//
//            apiGatewayService.createPublication(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildCreatePublicationDto(deploymentPlan);
//        }
//
//        @ParameterizedTest(name = "[{index}] env: {0}")
//        @EnumSource(Environment.class)
//        @DisplayName("Create publication -> ok")
//        public void ok(Environment environment)
//        {
//            DeploymentPlan deploymentPlan = new DeploymentPlan();
//            deploymentPlan.setEnvironment(environment.getEnvironment());
//            AGMCreatePublicationDTO createPublicationDTO = new AGMCreatePublicationDTO();
//            createPublicationDTO.setServices(new AGMServiceDTO[]{new AGMServiceDTO()});
//
//            when(apiGatewayServiceDtoBuilder.buildCreatePublicationDto(any())).thenReturn(createPublicationDTO);
//
//            apiGatewayService.createPublication(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildCreatePublicationDto(deploymentPlan);
//            verify(apiGatewayManagerClient).createPublication(createPublicationDTO, environment.getEnvironment());
//        }
//    }
//
//    @Nested
//    class RemovePublication
//    {
//        @ParameterizedTest(name = "[{index}] env: {0}")
//        @EnumSource(Environment.class)
//        @DisplayName("Remove publication -> ok")
//        public void ok(Environment environment)
//        {
//            DeploymentPlan deploymentPlan = new DeploymentPlan();
//            deploymentPlan.setEnvironment(environment.getEnvironment());
//            AGMRemovePublicationDTO removePublicationDTO = new AGMRemovePublicationDTO();
//
//            when(apiGatewayServiceDtoBuilder.buildRemovePublicationDetailDto(any())).thenReturn(removePublicationDTO);
//
//            apiGatewayService.removePublication(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildRemovePublicationDetailDto(deploymentPlan);
//            verify(apiGatewayManagerClient).removePublication(removePublicationDTO, environment.getEnvironment());
//        }
//    }
//
//    @Nested
//    class UpdatePublication
//    {
//
//        @ParameterizedTest(name = "[{index}] serviceType: {0}")
//        @EnumSource(value = ServiceType.class, mode = EnumSource.Mode.EXCLUDE, names = {"BATCH_JAVA_SPRING_BATCH", "BATCH_PYTHON", "BATCH_JAVA_SPRING_CLOUD_TASK", "BEHAVIOR_TEST_JAVA"})
//        @DisplayName("Update publication -> do nothing")
//        public void doNothing(ServiceType serviceType)
//        {
//            List<DeploymentService> createdServices = Collections.emptyList();
//            List<DeploymentService> removedServices = Collections.emptyList();
//            var releaseVersionService = new ReleaseVersionService();
//            releaseVersionService.setServiceType(serviceType.getServiceType());
//            var deploymentService = new DeploymentService();
//            deploymentService.setService(releaseVersionService);
//            var deploymentSubsystem = new DeploymentSubsystem();
//            deploymentSubsystem.setDeploymentServices(List.of(deploymentService));
//            var oldPlan = new DeploymentPlan();
//            var newPlan = new DeploymentPlan();
//            newPlan.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
//            newPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//
//            apiGatewayService.updatePublication(createdServices, removedServices, newPlan, oldPlan);
//        }
//
//        @ParameterizedTest(name = "[{index}] serviceType: {0}")
//        @EnumSource(value = ServiceType.class, mode = EnumSource.Mode.EXCLUDE, names = {"BATCH_JAVA_SPRING_BATCH", "BATCH_PYTHON", "BATCH_JAVA_SPRING_CLOUD_TASK"})
//        @DisplayName("Update publication -> Only created services OK")
//        public void onlyCreatedServices(ServiceType serviceType)
//        {
//            List<DeploymentService> createdServices = List.of(new DeploymentService());
//            List<DeploymentService> removedServices = Collections.emptyList();
//            var releaseVersionService = new ReleaseVersionService();
//            releaseVersionService.setServiceType(serviceType.getServiceType());
//            var deploymentService = new DeploymentService();
//            deploymentService.setService(releaseVersionService);
//            var deploymentSubsystem = new DeploymentSubsystem();
//            deploymentSubsystem.setDeploymentServices(List.of(deploymentService));
//            var oldPlan = new DeploymentPlan();
//            var newPlan = new DeploymentPlan();
//            newPlan.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
//            newPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//            AGMUpdatePublicationDTO updatePublicationDTO = new AGMUpdatePublicationDTO();
//            updatePublicationDTO.fillRandomly(4, false, 0, 4);
//
//            when(apiGatewayServiceDtoBuilder.buildUpdatePublicationDto(any(), any(), any(), any())).thenReturn(updatePublicationDTO);
//
//            apiGatewayService.updatePublication(createdServices, removedServices, newPlan, oldPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildUpdatePublicationDto(createdServices, removedServices, newPlan, oldPlan);
//            verify(apiGatewayManagerClient).updatePublication(updatePublicationDTO, newPlan.getEnvironment());
//
//        }
//
//        @ParameterizedTest(name = "[{index}] serviceType: {0}")
//        @EnumSource(value = ServiceType.class, mode = EnumSource.Mode.EXCLUDE, names = {"BATCH_JAVA_SPRING_BATCH", "BATCH_PYTHON", "BATCH_JAVA_SPRING_CLOUD_TASK"})
//        @DisplayName("Update publication -> Only removed services OK")
//        public void onlyRemovedServices(ServiceType serviceType)
//        {
//            List<DeploymentService> createdServices = Collections.emptyList();
//            List<DeploymentService> removedServices = List.of(new DeploymentService());
//            var releaseVersionService = new ReleaseVersionService();
//            releaseVersionService.setServiceType(serviceType.getServiceType());
//            var deploymentService = new DeploymentService();
//            deploymentService.setService(releaseVersionService);
//            var deploymentSubsystem = new DeploymentSubsystem();
//            deploymentSubsystem.setDeploymentServices(List.of(deploymentService));
//            var oldPlan = new DeploymentPlan();
//            var newPlan = new DeploymentPlan();
//            newPlan.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
//            newPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//            AGMUpdatePublicationDTO updatePublicationDTO = new AGMUpdatePublicationDTO();
//            updatePublicationDTO.fillRandomly(4, false, 0, 4);
//
//            when(apiGatewayServiceDtoBuilder.buildUpdatePublicationDto(any(), any(), any(), any())).thenReturn(updatePublicationDTO);
//
//            apiGatewayService.updatePublication(createdServices, removedServices, newPlan, oldPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildUpdatePublicationDto(createdServices, removedServices, newPlan, oldPlan);
//            verify(apiGatewayManagerClient).updatePublication(updatePublicationDTO, newPlan.getEnvironment());
//
//        }
//
//        @ParameterizedTest(name = "[{index}] serviceType: {0}")
//        @EnumSource(value = ServiceType.class, mode = EnumSource.Mode.INCLUDE, names = {"BATCH_JAVA_SPRING_BATCH", "BATCH_PYTHON", "BATCH_JAVA_SPRING_CLOUD_TASK"})
//        @DisplayName("Update publication -> Only batch services OK")
//        public void onlyBatchServices(ServiceType serviceType)
//        {
//            List<DeploymentService> createdServices = Collections.emptyList();
//            List<DeploymentService> removedServices = Collections.emptyList();
//            var releaseVersionService = new ReleaseVersionService();
//            releaseVersionService.setServiceType(serviceType.getServiceType());
//            var deploymentService = new DeploymentService();
//            deploymentService.setService(releaseVersionService);
//            var deploymentSubsystem = new DeploymentSubsystem();
//            deploymentSubsystem.setDeploymentServices(List.of(deploymentService));
//            var oldPlan = new DeploymentPlan();
//            var newPlan = new DeploymentPlan();
//            newPlan.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
//            newPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//            AGMUpdatePublicationDTO updatePublicationDTO = new AGMUpdatePublicationDTO();
//            updatePublicationDTO.fillRandomly(4, false, 0, 4);
//
//            when(apiGatewayServiceDtoBuilder.buildUpdatePublicationDto(any(), any(), any(), any())).thenReturn(updatePublicationDTO);
//
//            apiGatewayService.updatePublication(createdServices, removedServices, newPlan, oldPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildUpdatePublicationDto(createdServices, removedServices, newPlan, oldPlan);
//            verify(apiGatewayManagerClient).updatePublication(updatePublicationDTO, newPlan.getEnvironment());
//        }
//
//        @ParameterizedTest(name = "[{index}] serviceType: {0}")
//        @EnumSource(value = ServiceType.class, mode = EnumSource.Mode.INCLUDE, names = {"BATCH_JAVA_SPRING_BATCH", "BATCH_PYTHON", "BATCH_JAVA_SPRING_CLOUD_TASK"})
//        @DisplayName("Update publication -> OK")
//        public void ok(ServiceType serviceType)
//        {
//            List<DeploymentService> createdServices = List.of(new DeploymentService());
//            List<DeploymentService> removedServices = List.of(new DeploymentService());
//            var releaseVersionService = new ReleaseVersionService();
//            releaseVersionService.setServiceType(serviceType.getServiceType());
//            var deploymentService = new DeploymentService();
//            deploymentService.setService(releaseVersionService);
//            var deploymentSubsystem = new DeploymentSubsystem();
//            deploymentSubsystem.setDeploymentServices(List.of(deploymentService));
//            var oldPlan = new DeploymentPlan();
//            var newPlan = new DeploymentPlan();
//            newPlan.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
//            newPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//            AGMUpdatePublicationDTO updatePublicationDTO = new AGMUpdatePublicationDTO();
//            updatePublicationDTO.fillRandomly(4, false, 0, 4);
//
//            when(apiGatewayServiceDtoBuilder.buildUpdatePublicationDto(any(), any(), any(), any())).thenReturn(updatePublicationDTO);
//
//            apiGatewayService.updatePublication(createdServices, removedServices, newPlan, oldPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildUpdatePublicationDto(createdServices, removedServices, newPlan, oldPlan);
//            verify(apiGatewayManagerClient).updatePublication(updatePublicationDTO, newPlan.getEnvironment());
//        }
//
//    }
//
//    @Nested
//    class GenerateDockerKey
//    {
//        String groupId = "groupId";
//        String artifactId = "artifactId";
//        String releaseName = "releaseName";
//        String version = "1.0.0";
//        String omegaServiceLocator = "groupid-artifactid-releasename-1";
//        String etherServiceLocator = "etherServiceLocator";
//
//        @Nested
//        class FromDeploymentPlan
//        {
//            @ParameterizedTest(name = "[{index}] serviceType: {0}")
//            @ArgumentsSource(NoMicrogatewayServiceTypeArgumentsProvider.class)
//            @DisplayName("Generate docker key from deployment plan -> Do nothing because only no microgateway service provided")
//            public void doNothing(ServiceType serviceType)
//            {
//                var deploymentService = Mockito.mock(DeploymentService.class, RETURNS_DEEP_STUBS);
//                var deploymentSubsystem = new DeploymentSubsystem();
//                deploymentSubsystem.setDeploymentServices(List.of(deploymentService));
//                var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                var deploymentPlan = new DeploymentPlan();
//                deploymentPlan.setEnvironment(environment.getEnvironment());
//                deploymentPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//
//                when(deploymentService.getService().getServiceType()).thenReturn(serviceType.getServiceType());
//
//                apiGatewayService.generateDockerKey(deploymentPlan);
//
//                verify(deploymentPlanRepository).saveAndFlush(deploymentPlan);
//            }
//
//            @Nested
//            class DeployedInOmega
//            {
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment plan deployed in omega infrastructure -> Invalid dockerValidationDto service name error")
//                public void error(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//
//                    var deploymentPlan = new DeploymentPlan();
//                    deploymentPlan.setEnvironment(environment.getEnvironment());
//                    deploymentPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
//                    when(deploymentSubsystem.getDeploymentPlan().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.NOVA);
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    assertThrows(
//                            NovaException.class,
//                            () -> apiGatewayService.generateDockerKey(deploymentPlan)
//                    );
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals("", deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment plan deployed in omega infrastructure -> OK")
//                public void ok(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//                    dockerValidationDTO.setServiceName(omegaServiceLocator);
//
//                    var deploymentPlan = new DeploymentPlan();
//                    deploymentPlan.setEnvironment(environment.getEnvironment());
//                    deploymentPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
//                    when(deploymentSubsystem.getDeploymentPlan().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.NOVA);
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    apiGatewayService.generateDockerKey(deploymentPlan);
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//                    verify(deploymentPlanRepository).saveAndFlush(deploymentPlan);
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerValidationDTO.getDockerKey(), deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//            }
//
//            @Nested
//            class DeployedInEther
//            {
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment plan deployed in ether infrastructure -> Invalid dockerValidationDto service name error")
//                public void error(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//
//                    var deploymentPlan = new DeploymentPlan();
//                    deploymentPlan.setEnvironment(environment.getEnvironment());
//                    deploymentPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.ETHER);
//                    when(deploymentSubsystem.getDeploymentPlan().getEnvironment()).thenReturn(environment.getEnvironment());
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(etherUtils.getEAEServiceURL(any(), any(), any(), any(), any())).thenReturn(etherServiceLocator);
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    assertThrows(
//                            NovaException.class,
//                            () -> apiGatewayService.generateDockerKey(deploymentPlan)
//                    );
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(etherUtils).getEAEServiceURL(environment.getEnvironment(), groupId, artifactId, releaseName, version);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals("", deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment plan deployed in omega infrastructure -> OK")
//                public void ok(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//                    dockerValidationDTO.setServiceName(etherServiceLocator);
//
//                    var deploymentPlan = new DeploymentPlan();
//                    deploymentPlan.setEnvironment(environment.getEnvironment());
//                    deploymentPlan.setDeploymentSubsystems(List.of(deploymentSubsystem));
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.ETHER);
//                    when(deploymentSubsystem.getDeploymentPlan().getEnvironment()).thenReturn(environment.getEnvironment());
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(etherUtils.getEAEServiceURL(any(), any(), any(), any(), any())).thenReturn(etherServiceLocator);
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    apiGatewayService.generateDockerKey(deploymentPlan);
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(etherUtils).getEAEServiceURL(environment.getEnvironment(), groupId, artifactId, releaseName, version);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//                    verify(deploymentPlanRepository).saveAndFlush(deploymentPlan);
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerValidationDTO.getDockerKey(), deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//            }
//
//
//        }
//
//        @Nested
//        class FromDeploymentServiceList
//        {
//            @ParameterizedTest(name = "[{index}] serviceType: {0}")
//            @ArgumentsSource(NoMicrogatewayServiceTypeArgumentsProvider.class)
//            @DisplayName("Generate docker key from deployment service list -> Do nothing because only no microgateway service provided")
//            public void doNothing(ServiceType serviceType)
//            {
//                var deploymentService = Mockito.mock(DeploymentService.class, RETURNS_DEEP_STUBS);
//                var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//
//                when(deploymentService.getService().getServiceType()).thenReturn(serviceType.getServiceType());
//
//                apiGatewayService.generateDockerKey(List.of(deploymentService), environment.getEnvironment());
//            }
//
//            @Nested
//            class DeployedInOmega
//            {
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment service list deployed in omega infrastructure -> Invalid dockerValidationDto service name error")
//                public void error(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(deploymentSubsystem.getDeploymentPlan().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.NOVA);
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    assertThrows(
//                            NovaException.class,
//                            () -> apiGatewayService.generateDockerKey(List.of(deploymentService), environment.getEnvironment())
//                    );
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals("", deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment service list deployed in omega infrastructure -> OK")
//                public void ok(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//                    dockerValidationDTO.setServiceName(omegaServiceLocator);
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(deploymentSubsystem.getDeploymentPlan().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.NOVA);
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    apiGatewayService.generateDockerKey(List.of(deploymentService), environment.getEnvironment());
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerValidationDTO.getDockerKey(), deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//            }
//
//            @Nested
//            class DeployedInEther
//            {
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment service list deployed in ether infrastructure -> Invalid dockerValidationDto service name error")
//                public void error(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var deploymentPlanEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.ETHER);
//                    when(deploymentSubsystem.getDeploymentPlan().getEnvironment()).thenReturn(deploymentPlanEnvironment.getEnvironment());
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(etherUtils.getEAEServiceURL(any(), any(), any(), any(), any())).thenReturn(etherServiceLocator);
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any())).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any())).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    assertThrows(
//                            NovaException.class,
//                            () -> apiGatewayService.generateDockerKey(List.of(deploymentService), environment.getEnvironment())
//                    );
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(etherUtils).getEAEServiceURL(deploymentPlanEnvironment.getEnvironment(), groupId, artifactId, releaseName, version);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals("", deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//
//                @ParameterizedTest(name = "[{index}] serviceType: {0}")
//                @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
//                @DisplayName("Generate docker key from deployment service list deployed in omega infrastructure -> OK")
//                public void ok(ServiceType serviceType)
//                {
//                    var releaseVersionService = Mockito.mock(ReleaseVersionService.class, RETURNS_DEEP_STUBS);
//                    var deploymentSubsystem = Mockito.mock(DeploymentSubsystem.class, RETURNS_DEEP_STUBS);
//                    var deploymentService = new DeploymentService();
//                    deploymentService.setService(releaseVersionService);
//                    deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//                    var environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var deploymentPlanEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//                    var dockerServiceDTO = new AGMDockerServiceDTO();
//                    var dockerValidationDTO = new AGMDockerValidationDTO();
//                    dockerValidationDTO.fillRandomly(4, false, 0, 3);
//                    dockerValidationDTO.setServiceName(etherServiceLocator);
//
//                    when(releaseVersionService.getServiceType()).thenReturn(serviceType.getServiceType());
//                    when(releaseVersionService.getGroupId()).thenReturn(groupId);
//                    when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
//                    when(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName()).thenReturn(releaseName);
//                    when(deploymentSubsystem.getDeploymentPlan().getSelectedDeploy()).thenReturn(Platform.ETHER);
//                    when(deploymentSubsystem.getDeploymentPlan().getEnvironment()).thenReturn(deploymentPlanEnvironment.getEnvironment());
//                    when(releaseVersionService.getVersion()).thenReturn(version);
//
//                    when(etherUtils.getEAEServiceURL(any(), any(), any(), any(), any())).thenReturn(etherServiceLocator);
//                    when(apiGatewayValidator.checkReleaseVersionServiceHasMicrogateway(any(ReleaseVersionService.class))).thenReturn(true);
//                    when(apiGatewayServiceDtoBuilder.buildDockerServiceDto(any(DeploymentService.class))).thenReturn(dockerServiceDTO);
//                    when(apiGatewayManagerClient.generateDockerKey(any(), any())).thenReturn(new AGMDockerValidationDTO[]{dockerValidationDTO});
//
//                    apiGatewayService.generateDockerKey(List.of(deploymentService), environment.getEnvironment());
//
//                    var dockerServiceDTOArrayArgCaptor = ArgumentCaptor.forClass(AGMDockerServiceDTO[].class);
//                    var environmentArgCaptor = ArgumentCaptor.forClass(String.class);
//                    verify(etherUtils).getEAEServiceURL(deploymentPlanEnvironment.getEnvironment(), groupId, artifactId, releaseName, version);
//                    verify(apiGatewayValidator).checkReleaseVersionServiceHasMicrogateway(releaseVersionService);
//                    verify(apiGatewayServiceDtoBuilder).buildDockerServiceDto(deploymentService);
//                    verify(apiGatewayManagerClient).generateDockerKey(dockerServiceDTOArrayArgCaptor.capture(), environmentArgCaptor.capture());
//
//                    var dockerServiceDTOArrayCaptured = dockerServiceDTOArrayArgCaptor.getValue();
//                    assertEquals(1, dockerServiceDTOArrayCaptured.length);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerServiceDTO, dockerServiceDTOArrayCaptured[0]);
//                    assertEquals(dockerValidationDTO.getDockerKey(), deploymentService.getDockerKey());
//                    assertEquals(environment.getEnvironment(), environmentArgCaptor.getValue());
//                }
//            }
//        }
//    }
//
//    @Nested
//    class CreateProfiling
//    {
//        Integer planId = 909;
//        Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//
//        @Test
//        @DisplayName("Create profiling -> do nothing because there is no profiling information")
//        public void doNothing()
//        {
//            var deploymentPlan = new DeploymentPlan();
//            deploymentPlan.setId(planId);
//            deploymentPlan.setEnvironment(environment.getEnvironment());
//            var createProfilingDTO = new AGMCreateProfilingDTO();
//            createProfilingDTO.setProfilingInfo(new AGMProfilingInfoDTO[]{});
//
//            when(apiGatewayServiceDtoBuilder.buildCreateProfilingDto(any())).thenReturn(createProfilingDTO);
//
//            apiGatewayService.createProfiling(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildCreateProfilingDto(deploymentPlan);
//        }
//
//        @Test
//        @DisplayName("Create profiling -> ok")
//        public void ok()
//        {
//            var deploymentPlan = new DeploymentPlan();
//            deploymentPlan.setId(planId);
//            deploymentPlan.setEnvironment(environment.getEnvironment());
//            var createProfilingDTO = new AGMCreateProfilingDTO();
//            createProfilingDTO.fillRandomly(4, false, 0, 4);
//
//            when(apiGatewayServiceDtoBuilder.buildCreateProfilingDto(any())).thenReturn(createProfilingDTO);
//
//            apiGatewayService.createProfiling(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildCreateProfilingDto(deploymentPlan);
//            verify(apiGatewayManagerClient).createProfiling(createProfilingDTO, environment.getEnvironment());
//        }
//    }
//
//    @Nested
//    class RemoveProfiling
//    {
//        Integer planId = 909;
//        Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
//
//        @Test
//        @DisplayName("Remove profiling -> do nothing because there is no resources information")
//        public void doNothing()
//        {
//            var deploymentPlan = new DeploymentPlan();
//            deploymentPlan.setId(planId);
//            deploymentPlan.setEnvironment(environment.getEnvironment());
//            var removeProfilingDTO = new AGMRemoveProfilingDTO();
//            removeProfilingDTO.setResources(new AGMProfilingInfoDTO[]{});
//
//            when(apiGatewayServiceDtoBuilder.buildRemoveProfilingDto(any())).thenReturn(removeProfilingDTO);
//
//            apiGatewayService.removeProfiling(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildRemoveProfilingDto(deploymentPlan);
//        }
//
//        @Test
//        @DisplayName("Remove profiling -> ok")
//        public void ok()
//        {
//            var deploymentPlan = new DeploymentPlan();
//            deploymentPlan.setId(planId);
//            deploymentPlan.setEnvironment(environment.getEnvironment());
//            var removeProfilingDTO = new AGMRemoveProfilingDTO();
//            removeProfilingDTO.fillRandomly(4, false, 0, 4);
//
//            when(apiGatewayServiceDtoBuilder.buildRemoveProfilingDto(any())).thenReturn(removeProfilingDTO);
//
//            apiGatewayService.removeProfiling(deploymentPlan);
//
//            verify(apiGatewayServiceDtoBuilder).buildRemoveProfilingDto(deploymentPlan);
//            verify(apiGatewayManagerClient).removeProfiling(removeProfilingDTO, environment.getEnvironment());
//        }
//    }
//
//    private static class NoMicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
//    {
//        @Override
//        public Stream<? extends Arguments> provideArguments(final ExtensionContext context)
//        {
//            return Arrays.stream(ServiceType.values()).filter(serviceType -> !serviceType.isMicrogateway()).map(Arguments::of);
//        }
//    }
//
//    private static class MicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
//    {
//        @Override
//        public Stream<? extends Arguments> provideArguments(final ExtensionContext context)
//        {
//            return Arrays.stream(ServiceType.values()).filter(ServiceType::isMicrogateway).map(Arguments::of);
//        }
//    }
//}
