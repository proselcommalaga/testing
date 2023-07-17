package com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.impl;


import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator.ApiGatewayValidatorImpl;
import com.bbva.kltt.apirest.generator.lib.commons.random.APIRestGeneratorRandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiGatewayValidatorImplTest
{

    /**
     * Max array size for the tests
     */
    private static int              MAX_ARRAY_SIZE = 2;

    private DeploymentPlan          deploymentPlan;

    private String                  uuaa           = "ENOA";

    @InjectMocks
    private ApiGatewayValidatorImpl service;

    @BeforeEach
    public void setUp()
    {

        this.deploymentPlan = new DeploymentPlan();
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        DeploymentService deploymentService = new DeploymentService();
        ReleaseVersionService service = new ReleaseVersionService();
        service.setFinalName(APIRestGeneratorRandomUtils.getXmlValidString(MAX_ARRAY_SIZE));
        deploymentService.setService(service);
        deploymentServiceList.add(deploymentService);
        deploymentSubsystem.setDeploymentServices(deploymentServiceList);
        deploymentSubsystemList.add(deploymentSubsystem);
        deploymentPlan.setDeploymentSubsystems(deploymentSubsystemList);

        List<String> uuaaLIst = new ArrayList<>();
        uuaaLIst.add(uuaa);

        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void checkReleaseVersionHasRESTServices_NoSubsystem()
    {

        ReleaseVersion releaseVersion = new ReleaseVersion();
        // releaseVersion.setSubsystems(subsystems);
        boolean result = this.service.checkReleaseVersionHasRESTServices(releaseVersion);
        assertFalse(result);
    }

    @Test
    public void checkReleaseVersionHasRESTServices_NoSservices()
    {

        ReleaseVersion releaseVersion = new ReleaseVersion();
        List<ReleaseVersionSubsystem> subsystems = new ArrayList<>();
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        subsystems.add(releaseVersionSubsystem);
        releaseVersion.setSubsystems(subsystems);
        boolean result = this.service.checkReleaseVersionHasRESTServices(releaseVersion);
        assertFalse(result);
    }

    @Test
    public void checkReleaseVersionHasRESTServices_ServiceTypeNotRest()
    {

        ReleaseVersion releaseVersion = new ReleaseVersion();
        List<ReleaseVersionSubsystem> subsystems = new ArrayList<>();
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        List<ReleaseVersionService> services = new ArrayList<>();
        ReleaseVersionService serviceSub = new ReleaseVersionService();
        serviceSub.setServiceType(ServiceType.EPHOENIX_BATCH.getServiceType());
        services.add(serviceSub);
        releaseVersionSubsystem.setServices(services);
        subsystems.add(releaseVersionSubsystem);
        releaseVersion.setSubsystems(subsystems);
        boolean result = this.service.checkReleaseVersionHasRESTServices(releaseVersion);
        assertFalse(result);
    }

    @Test
    public void checkReleaseVersionHasRESTServicesWithApiRest()
    {
        checkReleaseVersionHasRESTServices(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void checkReleaseVersionHasRESTServicesWithApi()
    {
        checkReleaseVersionHasRESTServices(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void checkReleaseVersionHasRESTServices(ServiceType serviceType)
    {

        ReleaseVersion releaseVersion = new ReleaseVersion();
        List<ReleaseVersionSubsystem> subsystems = new ArrayList<>();
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        List<ReleaseVersionService> services = new ArrayList<>();
        ReleaseVersionService serviceSub = new ReleaseVersionService();
        serviceSub.setServiceType(serviceType.getServiceType());
        services.add(serviceSub);
        releaseVersionSubsystem.setServices(services);
        subsystems.add(releaseVersionSubsystem);
        releaseVersion.setSubsystems(subsystems);
        boolean result = this.service.checkReleaseVersionHasRESTServices(releaseVersion);
        assertTrue(result);
    }

}