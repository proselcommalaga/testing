package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConfigurationRevisionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeploymentPlanClonerImplTest
{
    @Mock
    private EntityManager entityManager;
    @Mock
    private FilesystemRepository filesystemsApiRepository;
    @Mock
    private BrokerPropertyRepository brokerPropertyRepository;
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @Mock
    private ConfigurationRevisionRepository confRevisionRepo;
    @Mock
    private IApiGatewayService iApiGatewayService;
    @Mock
    private ISchedulerManagerClient schedulerManagerClient;
    @Mock
    private IDeploymentsValidator deploymentsValidator;
    @Mock
    private JvmJdkConfigurationChecker jvmJdkConfigurationChecker;
    @InjectMocks
    private DeploymentPlanClonerImpl deploymentPlanCloner;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void clonePlanToEnvironment()
    {
        ReleaseVersionService releaseVersionService1 = new ReleaseVersionService();
        releaseVersionService1.setArtifactId("Service1");
        releaseVersionService1.setVersion("1");
        releaseVersionService1.setId(1);
        DeploymentService deploymentService1 = new DeploymentService();
        deploymentService1.setService(releaseVersionService1);
        TOSubsystemDTO productSubsystem1 = new TOSubsystemDTO();
        productSubsystem1.setSubsystemName("ProductSubsystem1");
        Release release = new Release();
        release.setProduct(new Product());
        ReleaseVersionSubsystem releaseVersionSubsystem1 = new ReleaseVersionSubsystem();
        releaseVersionSubsystem1.setSubsystemId(1);
        releaseVersionSubsystem1.setTagName("TAG1");
        List<ReleaseVersionService> releaseVersionServices = new ArrayList<>();
        releaseVersionServices.add(releaseVersionService1);
        releaseVersionSubsystem1.setServices(releaseVersionServices);
        DeploymentSubsystem subsystem1 = new DeploymentSubsystem();
        subsystem1.setSubsystem(releaseVersionSubsystem1);
        List<DeploymentSubsystem> subsystemList1 = new ArrayList<>();
        subsystemList1.add(subsystem1);
        List<DeploymentService> serviceList1 = new ArrayList<>();
        serviceList1.add(deploymentService1);
        subsystem1.setDeploymentServices(serviceList1);
        DeploymentPlan plan1 = new DeploymentPlan();
        plan1.setDeploymentSubsystems(subsystemList1);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        List<ReleaseVersionSubsystem> releaseVersionSubsystems = new ArrayList<>();
        releaseVersionSubsystems.add(releaseVersionSubsystem1);
        releaseVersion.setSubsystems(releaseVersionSubsystems);
        plan1.setReleaseVersion(releaseVersion);

        DeploymentPlan response = this.deploymentPlanCloner.clonePlanToEnvironment(plan1, Environment.PRO);

        Assertions.assertEquals(Environment.PRO.getEnvironment(), response.getEnvironment());
    }

    @Test
    @DisplayName("Testing the number of instances copy in PRE environment with Api Rest")
    public void clonePlanToEnvironmentInPreWithApiRest()
    {
        clonePlanToEnvironmentInPre(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    @DisplayName("Testing the number of instances copy in PRE environment with Api")
    public void clonePlanToEnvironmentInPreWithApi()
    {
        clonePlanToEnvironmentInPre(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void clonePlanToEnvironmentInPre(ServiceType serviceType)
    {
        // Given
        DeploymentPlan originalPlan = new DeploymentPlan();
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        originalPlan.setReleaseVersion(releaseVersionMock);
        originalPlan.setDeploymentTypeInPro(DeploymentType.ON_DEMAND);
        originalPlan.setMultiCPDInPro(true);
        CPD cpdMock = Mockito.mock(CPD.class);
        originalPlan.setCpdInPro(cpdMock);
        Release releaseMock = Mockito.mock(Release.class);
        lenient().when(releaseVersionMock.getRelease()).thenReturn(releaseMock);
        lenient().when(releaseMock.getSelectedDeployPre()).thenReturn(Platform.NOVA);
        lenient().when(releaseMock.getSelectedLoggingPre()).thenReturn(Platform.NOVA);
        Product productMock = Mockito.mock(Product.class);
        lenient().when(releaseMock.getProduct()).thenReturn(productMock);
        lenient().when(productMock.getEtherNsPre()).thenReturn("test");
        lenient().doNothing().when(entityManager).persist(any(DeploymentPlan.class));
        originalPlan.setPlanProfiles(List.of());

        DeploymentSubsystem deploymentSubsystem1 = generateDeploymentSubsystem();
        originalPlan.setDeploymentSubsystems(List.of(deploymentSubsystem1));

        DeploymentService deploymentService1 = generateDeploymentService();
        DeploymentService deploymentService2 = generateDeploymentService();
        lenient().when(deploymentSubsystem1.getDeploymentServices()).thenReturn(List.of(deploymentService1, deploymentService2));
        lenient().when(deploymentService1.getService().getServiceType()).thenReturn(serviceType.getServiceType());
        lenient().when(deploymentService1.getNumberOfInstances()).thenReturn(2);
        lenient().when(deploymentService2.getService().getServiceType()).thenReturn(ServiceType.BATCH_JAVA_SPRING_BATCH.getServiceType());
        lenient().when(deploymentService2.getNumberOfInstances()).thenReturn(1);

        // Then
        DeploymentPlan response = this.deploymentPlanCloner.clonePlanToEnvironment(originalPlan, Environment.PRE);

        // Asserts
        Assertions.assertEquals(Environment.PRE.getEnvironment(), response.getEnvironment());

        Assertions.assertTrue(response.getDeploymentSubsystems().stream()
                .map(DeploymentSubsystem::getDeploymentServices)
                .flatMap(List::stream)
                .filter(deploymentService -> ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch())
                .allMatch(deploymentService -> deploymentService.getNumberOfInstances() == 1), "There was an error copying the number of instances in Batches services");

        Assertions.assertTrue(response.getDeploymentSubsystems().stream()
                .map(DeploymentSubsystem::getDeploymentServices)
                .flatMap(List::stream)
                .filter(deploymentService -> !ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch())
                .allMatch(deploymentService -> deploymentService.getNumberOfInstances() == 0), "There was an error copying the number of instances in Non Batches services");
    }

    /**
     * Generate and fill a DeploymentSubsystem object for testing clonePlanToEnvironment method purpose
     *
     * @return a generated mock for deployment subsystem
     * @author created by <a href="mailto:rodrigo.puerto.contractor@bbva.com">Rodrigo Puerto Pedrera</a>
     * @since on version 9.15.0 - 13/09/2021
     */
    private static DeploymentSubsystem generateDeploymentSubsystem()
    {
        DeploymentSubsystem deploymentSubsystemMock = Mockito.mock(DeploymentSubsystem.class);
        ReleaseVersionSubsystem releaseVersionSubsystem = Mockito.mock(ReleaseVersionSubsystem.class);
        when(deploymentSubsystemMock.getSubsystem()).thenReturn(releaseVersionSubsystem);


        return deploymentSubsystemMock;
    }

    /**
     * Generate and fill a DeploymentService object for testing clonePlanToEnvironment method purpose
     *
     * @return a generate mock for deployment service
     * @author created by <a href="mailto:rodrigo.puerto.contractor@bbva.com">Rodrigo Puerto Pedrera</a>
     * @since on version 9.15.0 - 13/09/2021
     */
    private static DeploymentService generateDeploymentService()
    {
        DeploymentService deploymentServiceMock = Mockito.mock(DeploymentService.class);
        ReleaseVersionService releaseVersionServiceMock = Mockito.mock(ReleaseVersionService.class);
        when(deploymentServiceMock.getService()).thenReturn(releaseVersionServiceMock);

        return deploymentServiceMock;
    }
}
