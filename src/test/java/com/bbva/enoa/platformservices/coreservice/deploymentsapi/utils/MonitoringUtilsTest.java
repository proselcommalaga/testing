package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

public class MonitoringUtilsTest
{
    @InjectMocks
    private MonitoringUtils monitoringUtils;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.monitoringUtils, "proBaseUrl", "proBaseUrl/");
        ReflectionTestUtils.setField(this.monitoringUtils, "novaMonitoringProductsInt", "{environment}/{uuaa}/int");
        ReflectionTestUtils.setField(this.monitoringUtils, "novaMonitoringProductsPre", "{environment}/{uuaa}/pre");
        ReflectionTestUtils.setField(this.monitoringUtils, "novaMonitoringProductsPro", "{environment}/{uuaa}/pro");
        ReflectionTestUtils.setField(this.monitoringUtils, "novaMonitoringContainersInt", "{environment}/{uuaa}/{container}/int");
        ReflectionTestUtils.setField(this.monitoringUtils, "novaMonitoringContainersPre", "{environment}/{uuaa}/{container}/pre");
        ReflectionTestUtils.setField(this.monitoringUtils, "novaMonitoringContainersPro", "{environment}/{uuaa}/{container}/pro");
    }

    /*@Test
    public void getMonitoringUrlForProducts()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setEnvironment(Environment.INT);
        deploymentPlan.setSelectedDeploy(DestinationPlatformDeployType.NOVA);
        deploymentPlan.setReleaseVersion(releaseVersion);

        String response1 = this.monitoringUtils.getMonitoringUrlForProducts(deploymentPlan);

        deploymentPlan.setEnvironment(Environment.PRE);
        String response2 = this.monitoringUtils.getMonitoringUrlForProducts(deploymentPlan);

        deploymentPlan.setEnvironment(Environment.PRO);
        String response3 = this.monitoringUtils.getMonitoringUrlForProducts(deploymentPlan);

        deploymentPlan.setEnvironment(Environment.LOCAL);
        String response4 = this.monitoringUtils.getMonitoringUrlForProducts(deploymentPlan);
        assertEquals("proBaseUrl/int/uuaa/int", response1);
        assertEquals("proBaseUrl/pre/uuaa/pre", response2);
        assertEquals("proBaseUrl/pro/uuaa/pro", response3);
        assertEquals("proBaseUrl/local/uuaa/int", response4);
    }

    @Test
    public void getMonitoringUrlForInstance()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setEnvironment(Environment.INT);
        deploymentPlan.setSelectedDeploy(DestinationPlatformDeployType.NOVA);
        deploymentPlan.setReleaseVersion(releaseVersion);

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        NovaDeploymentInstance novaDeploymentInstance = new NovaDeploymentInstance();
        novaDeploymentInstance.setContainerName("container");

        String response1 = this.monitoringUtils.getMonitoringUrlForInstance(deploymentService, novaDeploymentInstance);

        deploymentPlan.setEnvironment(Environment.PRE);
        String response2 = this.monitoringUtils.getMonitoringUrlForInstance(deploymentService, novaDeploymentInstance);

        deploymentPlan.setEnvironment(Environment.PRO);
        String response3 = this.monitoringUtils.getMonitoringUrlForInstance(deploymentService, novaDeploymentInstance);

        deploymentPlan.setEnvironment(Environment.LOCAL);
        String response4 = this.monitoringUtils.getMonitoringUrlForInstance(deploymentService, novaDeploymentInstance);

        assertEquals("proBaseUrl/INT/uuaa/instance/int", response1);
        assertEquals("proBaseUrl/PRE/uuaa/instance/pre", response2);
        assertEquals("proBaseUrl/PRO/uuaa/instance/pro", response3);
        assertEquals("proBaseUrl/LOCAL/uuaa/instance/int", response4);
    }*/
}