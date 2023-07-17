package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConfigurationRevisionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;

public class DeploymentPlanCopierImplTest
{
    @Mock
    private EntityManager entityManager;
    @Mock
    private FilesystemRepository filesystemsApiRepository;
    @Mock
    private ConfigurationRevisionRepository confRevisionRepo;
    @Mock
    private IApiGatewayService iApiGatewayService;
    @InjectMocks
    private DeploymentPlanCopierImpl deploymentPlanCopier;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    /*@Test
    public void copyPlan()
    {
        ReleaseVersionService releaseVersionService1 = new ReleaseVersionService();
        releaseVersionService1.setArtifactId("Service1");
        releaseVersionService1.setVersion("1");
        DeploymentService deploymentService1 = new DeploymentService();
        deploymentService1.setService(releaseVersionService1);
        TOSubsystemDTO productSubsystem1 = new TOSubsystemDTO();
        productSubsystem1.setSubsystemName("ProductSubsystem1");
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
        plan1.setEnvironment(Environment.PRO);

        ReleaseVersion releaseVersion = new ReleaseVersion();
        List<ReleaseVersionSubsystem> releaseVersionSubsystems = new ArrayList<>();
        releaseVersionSubsystems.add(releaseVersionSubsystem1);
        releaseVersion.setSubsystems(releaseVersionSubsystems);
        plan1.setReleaseVersion(releaseVersion);

        DeploymentPlan response = this.deploymentPlanCopier.copyPlan(plan1);

        assertEquals(Environment.PRO, response.getEnvironment());
    }*/
}
