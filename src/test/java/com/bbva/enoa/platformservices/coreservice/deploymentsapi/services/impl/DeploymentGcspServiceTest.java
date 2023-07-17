package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentGcspDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ScheduleControlMClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class DeploymentGcspServiceTest
{
    @Mock
    private ScheduleControlMClient scheduleControlMClient;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @InjectMocks
    private DeploymentGcspServiceImpl IDeploymentGcspService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.IDeploymentGcspService, "machine", "machine");
        ReflectionTestUtils.setField(this.IDeploymentGcspService, "user", "user");
        ReflectionTestUtils.setField(this.IDeploymentGcspService, "launcherScript", "launcherScript");
        ReflectionTestUtils.setField(this.IDeploymentGcspService, "starterScript", "starterScript");
    }

    @Test
    public void updateGcsp()
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        DeploymentGcspDto gcsp = new DeploymentGcspDto();
        gcsp.setExpectedDeploymentDate("2018-10-30T00:00:00.000Z");
        gcsp.setUndeployRelease(2);
        DeploymentPlan undeployPlan = new DeploymentPlan();
        undeployPlan.setId(2);
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        plan.setReleaseVersion(releaseVersion);
        List<DeploymentPlan> planList = new ArrayList<>();
        planList.add(undeployPlan);
        when(this.deploymentPlanRepository.findById(2)).thenReturn(Optional.of(undeployPlan));
        when(this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(
                1,
                Environment.PRO.getEnvironment(),
                DeploymentStatus.DEPLOYED)).thenReturn(planList);
        this.IDeploymentGcspService.updateGcsp(plan, gcsp);
        assertEquals(0, plan.getGcsp().getGcspPass());
    }

    /*@Test
    public void getGcspDto()
    {
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setReleaseVersion(releaseVersion);
        DeploymentGcspDto response = this.IDeploymentGcspService.getGcspDto(plan, "CODE");
        assertNotNull(response);

        DeploymentGcsp gcsp = new DeploymentGcsp();
        plan.setGcsp(gcsp);
        DeploymentGcspDto response2 = this.IDeploymentGcspService.getGcspDto(plan, "CODE");
        assertNotNull(response);
    }*/

    @Test
    public void getDeploymentScript()
    {
        DeploymentGcsp gcsp = new DeploymentGcsp();
        gcsp.setPriorityLevel(DeploymentPriority.PRODUCT);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setGcsp(gcsp);
        String response = this.IDeploymentGcspService.getDeploymentScript(plan);
        assertNotNull(response);
    }

    @Test
    public void validateGcspForDeploy()
    {
        DeploymentGcsp gcsp = new DeploymentGcsp();
        gcsp.setPriorityLevel(DeploymentPriority.PRODUCT);
        gcsp.setGcspPass(1);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        plan.setGcsp(gcsp);
        this.IDeploymentGcspService.validateGcspForDeploy(plan);
    }

}
