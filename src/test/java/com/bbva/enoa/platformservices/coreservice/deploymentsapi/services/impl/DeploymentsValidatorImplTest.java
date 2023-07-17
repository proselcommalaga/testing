package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.EnvironmentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.INotStartedServicesProvider;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeploymentsValidatorImplTest
{
    @Mock
    private INotStartedServicesProvider notStartedServicesProvider;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private DeploymentSubsystemRepository deploymentSubsystemRepository;
    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;
    @Mock
    private DeploymentInstanceRepository deploymentInstanceRepository;
    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private IErrorTaskManager errorTaskManager;
    @InjectMocks
    private DeploymentsValidatorImpl deploymentsValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(DeploymentsValidatorImpl.class);
    }

    @Test
    void checkPlanCreationBuilding()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.BUILDING);

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkReleaseVersionStatus(1, releaseVersion));
    }

    @Test
    void test_checkDeploymentDate_OK()
    {
        DeploymentGcsp gcsp = new DeploymentGcsp();
        gcsp.setGcspPass(0);
        Date fecha = new Date();
        gcsp.setExpectedDeploymentDate(fecha);

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        deploymentPlan.setGcsp(gcsp);

        this.deploymentsValidator.checkDeploymentDate(deploymentPlan);
    }

    @Test
    void test_checkDeploymentDate_KO_DateBefore()
    {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, +1);
        Date fecha = cal.getTime();

        DeploymentGcsp gcsp = new DeploymentGcsp();
        gcsp.setGcspPass(0);
        gcsp.setExpectedDeploymentDate(fecha);

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        deploymentPlan.setGcsp(gcsp);

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkDeploymentDate(deploymentPlan));
    }

    @Test
    void checkPlanCreationErrors()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkReleaseVersionStatus(1, releaseVersion));
    }

    @Test
    void checkPlanCreationStoraged()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.STORAGED);

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkReleaseVersionStatus(1, releaseVersion));
    }

    @Test
    void checkPlanExistence_NULL()
    {
        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkPlanExistence(null));
    }

    @Test
    void checkPlanExistence_OK()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        this.deploymentsValidator.checkPlanExistence(deploymentPlan);
    }

    @Test
    void checkPlanStored()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.STORAGED);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setReleaseVersion(releaseVersion);

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkReleaseVersionStored(plan));
    }

    @Test
    void checkPlanStored_OK()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.BUILDING);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setReleaseVersion(releaseVersion);
        this.deploymentsValidator.checkReleaseVersionStored(plan);
    }

    @Test
    void checkPlanNotStoragedAndNotReadyToDeploy()
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEPLOYED);

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsValidator.checkPlanNotStoragedAndNotReadyToDeploy(plan));
    }

    @Test
    void when_no_runnable_service_types_are_found_then_dont_do_service_execution_validations()
    {
        String environment = Environment.INT.getEnvironment();

        Map<String, ServiceType> serviceNameTypesMap = new HashMap<>();
        serviceNameTypesMap.put("service1", ServiceType.DEPENDENCY);
        serviceNameTypesMap.put("service2", ServiceType.LIBRARY_JAVA);
        serviceNameTypesMap.put("service3", ServiceType.BATCH_SCHEDULER_NOVA);

        deploymentsValidator.checkDeploymentInstances(getDummyPlan(serviceNameTypesMap, environment));

        Mockito.verifyNoInteractions(notStartedServicesProvider);
    }

    @Test
    void when_all_services_have_been_started_then_dont_throw_exception_with_api_rest()
    {
        when_all_services_have_been_started_then_dont_throw_exception(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void when_all_services_have_been_started_then_dont_throw_exception_with_api()
    {
        when_all_services_have_been_started_then_dont_throw_exception(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_all_services_have_been_started_then_dont_throw_exception(ServiceType serviceType)
    {
        String environment = Environment.INT.getEnvironment();

        Map<String, ServiceType> serviceNameTypesMap = new HashMap<>();
        serviceNameTypesMap.put("service1", serviceType);
        serviceNameTypesMap.put("service2", serviceType);

        deploymentsValidator.checkDeploymentInstances(getDummyPlan(serviceNameTypesMap, environment));

        Mockito.verify(notStartedServicesProvider, Mockito.times(1)).getNotStartedVersionedNamesFromServiceExecutionHistory(Mockito.anyList(), Mockito.anyString());
    }

    @Test
    void when_some_services_has_not_been_started_then_throw_exception_with_services_and_subsystems_with_api_rest()
    {
        when_some_services_has_not_been_started_then_throw_exception_with_services_and_subsystems(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void when_some_services_has_not_been_started_then_throw_exception_with_services_and_subsystems_with_api()
    {
        when_some_services_has_not_been_started_then_throw_exception_with_services_and_subsystems(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_some_services_has_not_been_started_then_throw_exception_with_services_and_subsystems(ServiceType serviceType)
    {
        Mockito.when(notStartedServicesProvider.getNotStartedVersionedNamesFromServiceExecutionHistory(Mockito.anyList(), Mockito.anyString())).thenReturn(List.of("com.bbva-service1:1.0.0", "com.bbva-service2:1.0.0"));

        String environment = Environment.INT.getEnvironment();

        Map<String, ServiceType> serviceNameTypesMap = new HashMap<>();
        serviceNameTypesMap.put("service1", serviceType);
        serviceNameTypesMap.put("service2", serviceType);

        try
        {
            deploymentsValidator.checkDeploymentInstances(getDummyPlan(serviceNameTypesMap, environment));
        }
        catch (NovaException e)
        {
            final String expectedExceptionMessage = "[DeploymentsValidatorImpl] -> [checkDeploymentInstances]: Not Allowed. The services : [service1, service2] have never been started." +
                    " Cannot promote due of is mandatory create instance of not started services of the plan.";
            Assertions.assertEquals(expectedExceptionMessage, e.getMessage());
            NovaError novaError = e.getNovaError();
            Assertions.assertEquals(DeploymentConstants.DeployErrors.SERVICES_NOT_STARTED, novaError.getErrorCode());
            final String expectedErrorMessage = "Cannot promote due is mandatory to start these services before promoting: service1, service2";
            Assertions.assertEquals(expectedErrorMessage, novaError.getErrorMessage());
            final String expectedActionMessage = "First, run all specified services in the environment before trying to promote";
            Assertions.assertEquals(expectedActionMessage, novaError.getActionMessage());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class CheckValidToCopy
    {
        @Test
        @DisplayName("Can I copy a release version in PRE with no started services in previous environment using api rest?")
        void notValidToCopyWithApiRest()
        {
            notValidToCopy(ServiceType.API_REST_JAVA_SPRING_BOOT);
        }

        @Test
        @DisplayName("Can I copy a release version in PRE with no started services in previous environment using api?")
        void notValidToCopyWithApi()
        {
            notValidToCopy(ServiceType.API_JAVA_SPRING_BOOT);
        }

        private void notValidToCopy(ServiceType serviceType)
        {
            // Given
            ReleaseVersion releaseVersionMock = mock(ReleaseVersion.class);
            ReleaseVersionService releaseVersionServiceMock = mock(ReleaseVersionService.class);
            ReleaseVersionSubsystem releaseVersionSubsystemMock = mock(ReleaseVersionSubsystem.class);

            // And
            when(releaseVersionRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionMock));
            when(releaseVersionMock.getSubsystems()).thenReturn(List.of(releaseVersionSubsystemMock));
            when(releaseVersionSubsystemMock.getServices()).thenReturn(List.of(releaseVersionServiceMock));
            when(releaseVersionServiceMock.getServiceType()).thenReturn(serviceType.getServiceType());
            when(releaseVersionServiceMock.getFinalName()).thenReturn("example.test.copy");
            when(releaseVersionServiceMock.getVersion()).thenReturn("1.0.0");
            when(notStartedServicesProvider.getNotStartedVersionedNamesFromServiceExecutionHistory(anyList(), anyString())).thenReturn(List.of("example.test.copy:1.0.0"));

            // Then
            NovaException exception = Assertions.assertThrows(NovaException.class, () -> deploymentsValidator.checkExistingPlansOfRV(1, Environment.PRE));
            Assertions.assertEquals(DeploymentConstants.DeployErrors.PENDING_TO_START_SERVICES_IN_COPY_OPERATION, exception.getNovaError().getErrorCode());
            Assertions.assertEquals(HttpStatus.FORBIDDEN, exception.getNovaError().getHttpStatus());
        }

        @Test
        @DisplayName("Can I copy a release version in PRE with no runnable services?")
        void validToCopyNoRunnableServices()
        {
            // Given
            ReleaseVersion releaseVersionMock = mock(ReleaseVersion.class);
            DeploymentPlan deploymentPlanMock = mock(DeploymentPlan.class);
            DeploymentSubsystem deploymentSubsystemMock = mock(DeploymentSubsystem.class);

            // And
            lenient().when(releaseVersionRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionMock));
            lenient().when(releaseVersionMock.getDeployments()).thenReturn(List.of(deploymentPlanMock));
            lenient().when(deploymentPlanMock.getEnvironment()).thenReturn(EnvironmentUtils.getPrevious(Environment.PRE).orElse(Environment.INT).getEnvironment());
            lenient().when(deploymentPlanMock.getStatus()).thenReturn(DeploymentStatus.DEPLOYED);
            lenient().when(deploymentPlanMock.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystemMock));
            lenient().when(deploymentSubsystemMock.getDeploymentServices()).thenReturn(List.of());

            // Then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.checkExistingPlansOfRV(1, Environment.PRE));
        }

        @ParameterizedTest
        @MethodSource("provideParameters")
        @DisplayName("Can I copy a release version with a plan in the same environment (PRE or PRO)?")
        void notValidToCopyCauseAnyPlanAssociated(Integer releaseVersionId, Environment environment)
        {
            // Given
            ReleaseVersion releaseVersionMock = mock(ReleaseVersion.class);
            DeploymentPlan deploymentPlanMock = mock(DeploymentPlan.class);

            // And
            when(releaseVersionRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionMock));
            when(releaseVersionMock.getDeployments()).thenReturn(List.of(deploymentPlanMock));
            when(deploymentPlanMock.getEnvironment()).thenReturn(environment.equals(Environment.PRE) ? Environment.PRE.getEnvironment() : Environment.PRO.getEnvironment());

            // Then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.checkExistingPlansOfRV(releaseVersionId, environment));
        }

        @Test
        @DisplayName("Can I copy a release version in INT?")
        void validToCopyCauseINT()
        {
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.checkExistingPlansOfRV(1, Environment.INT));
        }


        private Stream<Arguments> provideParameters()
        {
            return Stream.of(
                    Arguments.of(1, Environment.PRE),
                    Arguments.of(1, Environment.PRO)
            );
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class ValidateSamePlatformOnPREtoPRO
    {
        @Test
        @DisplayName("(ValidateSamePlatformOnPREtoPRO) -> is the happy path working?")
        void validateSamePlatformOnPREtoPRO()
        {
            // given
            DeploymentPlan originalPlan = mock(DeploymentPlan.class);
            Environment environment = Environment.PRO;
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            Release release = mock(Release.class);

            Platform selectedDeploy = Platform.NOVA;

            // when
            when(originalPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(originalPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(selectedDeploy);
            when(originalPlan.getSelectedDeploy()).thenReturn(Platform.ETHER);

            // then
            NovaException exception = Assertions.assertThrows(NovaException.class, () -> deploymentsValidator.validateSamePlatformOnPREtoPRO(originalPlan, environment));
            NovaError expectedError = DeploymentError.getTriedToPromotePlanChangingDeploymentPlatform(originalPlan.getId(), originalPlan.getSelectedDeploy().toString(), selectedDeploy.toString());
            Assertions.assertEquals(expectedError.getErrorCode(), exception.getNovaError().getErrorCode());
        }

        @Test
        @DisplayName("(ValidateSamePlatformOnPREtoPRO) -> no validation on INT to PRE promotion")
        void validateSamePlatformOnPREtoPRONOEnvironments()
        {
            // given
            DeploymentPlan originalPlan = mock(DeploymentPlan.class);
            Environment environment = Environment.PRO;
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            Release release = mock(Release.class);

            // when
            when(originalPlan.getEnvironment()).thenReturn(Environment.INT.getEnvironment());
            when(originalPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.NOVA);

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSamePlatformOnPREtoPRO(originalPlan, environment));
        }

        @Test
        @DisplayName("(ValidateSamePlatformOnPREtoPRO) -> is throwing on the same infrastructure NOVA to NOVA?")
        void validateSamePlatformOnPREtoPROSameInfrastructure()
        {
            // given
            DeploymentPlan originalPlan = mock(DeploymentPlan.class);
            Environment environment = Environment.PRO;
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            Release release = mock(Release.class);

            // when
            when(originalPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(originalPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.NOVA);
            when(originalPlan.getSelectedDeploy()).thenReturn(Platform.NOVA);

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSamePlatformOnPREtoPRO(originalPlan, environment));
        }

        @Test
        @DisplayName("(ValidateSamePlatformOnPREtoPRO) -> is throwing on the same infrastructure ETHER to ETHER?")
        void validateSamePlatformOnPREtoPROSameInfrastructureETHER()
        {
            // given
            DeploymentPlan originalPlan = mock(DeploymentPlan.class);
            Environment environment = Environment.PRO;
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            Release release = mock(Release.class);

            // when
            when(originalPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(originalPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.ETHER);
            when(originalPlan.getSelectedDeploy()).thenReturn(Platform.ETHER);

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSamePlatformOnPREtoPRO(originalPlan, environment));
        }


    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class ValidateInstancesNumberForMultiCPD
    {
        @Test
        @DisplayName("(ValidateInstancesNumberForMultiCPD) -> is the happy path (two instances on an API JAVA SPRING BOOT in MultiCPD mode) working?")
        void happyPath()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.NOVA);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(true);

            // services
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getNumberOfInstances()).thenReturn(2);
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateInstancesNumberForMultiCPD(deploymentPlan));
        }

        @Test
        @DisplayName("(ValidateInstancesNumberForMultiCPD) -> is the validation running well with odd number of instances in MultiCPD mode?")
        void validateOddNumberInMultiCPDMode()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.NOVA);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(true);

            // services
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getNumberOfInstances()).thenReturn(1);
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());
            when(releaseVersionService.getServiceName()).thenReturn("testingService");

            // then
            var ex = Assertions.assertThrows(NovaException.class, () -> deploymentsValidator.validateInstancesNumberForMultiCPD(deploymentPlan));
            var wrongServicesNames = List.of(releaseVersionService).stream().map(ReleaseVersionService::getServiceName).collect(Collectors.toList());
            Assertions.assertEquals(DeploymentError.getInvalidInstancesNumberError(wrongServicesNames).getErrorCode(), ex.getNovaError().getErrorCode());
        }

        @Test
        @DisplayName("(ValidateInstancesNumberForMultiCPD) -> is the validation working on INT to PRE promotion?")
        void validatePromotionFromIntToPre(){
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.NOVA);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateInstancesNumberForMultiCPD(deploymentPlan));
        }

        @Test
        @DisplayName("(ValidateInstancesNumberForMultiCPD) -> is the validation working (shouldn't it) on ETHER platform?")
        void validateInstancesOnEther()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.ETHER);

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateInstancesNumberForMultiCPD(deploymentPlan));
        }

        @Test
        @DisplayName("(ValidateInstancesNumberForMultiCPD) -> is the validation working (shouldn't it) in MonoCPD mode?")
        void validateOnMonoCPDMode()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getSelectedDeployPro()).thenReturn(Platform.NOVA);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(false);

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateInstancesNumberForMultiCPD(deploymentPlan));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class ValidateSameCPDConfigOnPRO
    {
        @Test
        @DisplayName("(ValidateSameCPDConfigOnPRO) -> is the happy path working?")
        void happyPath()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRO.getEnvironment());
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.name());

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSameCPDConfigOnPRO(deploymentPlan));

        }

        @Test
        @DisplayName("(ValidateSameCPDConfigOnPRO) -> is the different CPD mode validation working (deployment plan in MonoCPD and product in MultiCPD)?")
        void validateSameCPDConfigOnPRONotSame()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRO.getEnvironment());
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getMultiCPDInPro()).thenReturn(false);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.name());

            // then
            var ex = Assertions.assertThrows(NovaException.class, () -> deploymentsValidator.validateSameCPDConfigOnPRO(deploymentPlan));
            Assertions.assertEquals(DeploymentError.getNoSameCPDConfigurationInPROEnvironment(deploymentPlan.getId()).getErrorCode(), ex.getNovaError().getErrorCode());
        }

        @Test
        @DisplayName("(ValidateSameCPDConfigOnPRO) -> does it throw (shouldn't it) if the copy is from MultiCPD to MonoCPD?")
        void validateSameCPDConfigOnPROMultiToMono()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRO.getEnvironment());
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(false);
            when(deploymentPlan.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.name());

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSameCPDConfigOnPRO(deploymentPlan));
        }

        @Test
        @DisplayName("(ValidateSameCPDConfigOnPRO) -> is the validation working (shouldn't it) on PRE environment?")
        void validateSameCPDConfigOnPROInPREEnvironment()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(false);
            when(deploymentPlan.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.name());

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSameCPDConfigOnPRO(deploymentPlan));
        }

        @Test
        void onlyEphoenixServices()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRO.getEnvironment());
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getMultiCPDInPro()).thenReturn(false);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.EPHOENIX_BATCH.name());

            // then
            Assertions.assertDoesNotThrow(() -> deploymentsValidator.validateSameCPDConfigOnPRO(deploymentPlan));
        }

        @Test
        void notOnlyEphoenixServices()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRO.getEnvironment());
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getMultiCPDInPro()).thenReturn(true);
            when(deploymentPlan.getMultiCPDInPro()).thenReturn(false);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getServiceType()).thenReturn(ServiceType.API_JAVA_SPRING_BOOT.name());

            // then
            var ex = Assertions.assertThrows(NovaException.class, () -> deploymentsValidator.validateSameCPDConfigOnPRO(deploymentPlan));
            Assertions.assertEquals(DeploymentError.getNoSameCPDConfigurationInPROEnvironment(deploymentPlan.getId()).getErrorCode(), ex.getNovaError().getErrorCode());
        }
    }

    private DeploymentPlan getDummyPlan(Map<String, ServiceType> serviceNameTypesMap, String environment)
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setEnvironment(environment);
        deploymentPlan.setDeploymentSubsystems(Collections.singletonList(getDummySubsystem(serviceNameTypesMap)));
        return deploymentPlan;
    }

    private DeploymentSubsystem getDummySubsystem(Map<String, ServiceType> serviceNameTypesMap)
    {
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
        subsystem.setSubsystemId(1);
        deploymentSubsystem.setSubsystem(subsystem);
        List<DeploymentService> deploymentServices = deploymentSubsystem.getDeploymentServices();
        for (Map.Entry<String, ServiceType> entry : serviceNameTypesMap.entrySet())
        {
            deploymentServices.add(getDummyService(entry.getKey(), entry.getValue()));
        }
        return deploymentSubsystem;
    }

    private DeploymentService getDummyService(String finalName, ServiceType serviceType)
    {
        DeploymentService deploymentService = new DeploymentService();
        ReleaseVersionService service = new ReleaseVersionService();
        service.setServiceType(serviceType.getServiceType());
        service.setFinalName(finalName);
        service.setVersion("1.0.0");
        deploymentService.setService(service);
        return deploymentService;
    }

    private TOSubsystemDTO getDummySubsystemDTO()
    {
        TOSubsystemDTO dto = new TOSubsystemDTO();
        dto.setSubsystemName("subsystem");
        return dto;
    }
}
