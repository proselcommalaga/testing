package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentNovaDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSubsystemDto;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ScheduleControlMClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Junits for DeploymentNovaServiceImplTest
 */
public class DeploymentNovaServiceImplTest
{
    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;
    @Mock
    private IDeploymentPlanDtoBuilder deploymentPlanDtoBuilder;
    @Mock
    private DeploymentUtils deploymentUtils;
    @Mock
    private ScheduleControlMClient scheduleControlMClient;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @InjectMocks
    private DeploymentNovaServiceImpl deploymentNovaService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    private DeploymentNovaDto createDeploymentNovaDto(
            Integer undeployPlanId, DeploymentPriority deploymentPriority)
    {
        DeploymentNovaDto nova = new DeploymentNovaDto ();
        nova.setDeploymentDateTime ("2018-10-30T00:00:00.000Z");
        nova.setUndeployRelease(undeployPlanId);
        nova.setPriorityLevel(deploymentPriority.toString());
        var deploymentIds = ThreadLocalRandom.current().ints( 10 ).toArray();
        nova.setDeploymentList(deploymentIds);
        return nova;
    }

    private void updateNova(DeploymentPriority deploymentPriority)
    {
        // Prepare
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        DeploymentPlan undeployPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);

        DeploymentNovaDto nova = createDeploymentNovaDto(undeployPlan.getId(), deploymentPriority);

        List<DeploymentPlan> planList = Arrays.asList(undeployPlan);

        //When
        when(this.deploymentPlanRepository.findById(undeployPlan.getId())).thenReturn(Optional.of(undeployPlan));
        when(this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(
                plan.getReleaseVersion().getRelease().getProduct().getId(),
                Environment.PRO.getEnvironment(),
                DeploymentStatus.DEPLOYED)).thenReturn(planList);

        // Then
        this.deploymentNovaService.updateNova(plan, nova);

        // Verify
        assertEquals(0, plan.getNova().getBatch());
        if(DeploymentPriority.SERVICE == deploymentPriority ||DeploymentPriority.SUBSYSTEM == deploymentPriority)
        {
            assertEquals(Arrays.stream(nova.getDeploymentList()).boxed().map(Object::toString).collect(Collectors.joining(",")),
                    plan.getNova().getDeploymentList());
        }
        assertEquals(nova.getPriorityLevel(), plan.getNova().getPriorityLevel().toString());
        assertEquals(nova.getUndeployRelease().longValue(), plan.getNova().getUndeployRelease());
        assertEquals(0, plan.getNova().getGcspPass());

        verify(this.deploymentPlanRepository).findById(undeployPlan.getId());
        verify(this.deploymentPlanRepository).getByProductAndEnvironmentAndStatus(
                plan.getReleaseVersion().getRelease().getProduct().getId(),
                Environment.PRO.getEnvironment(),
                DeploymentStatus.DEPLOYED);
    }

    @Test
    public void updateNovaWithService()
    {
        updateNova(DeploymentPriority.SERVICE);
    }

    @Test
    public void updateNovaWithSubsystem()
    {
        updateNova(DeploymentPriority.SUBSYSTEM);
    }

    @Test
    public void updateNovaWithProduct()
    {
        updateNova(DeploymentPriority.PRODUCT);
    }

    @Test
    public void getEmptyNovaDto()
    {
        //Prepare
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);

        // Then
        var novaDto = this.deploymentNovaService.getNovaDto(plan, "ivUser");

        // Verify
        assertNotNull(novaDto);
    }

    @Test
    public void getNovaDto()
    {
        //Prepare
        var deploymentService = DeploymentPriority.SERVICE;
        var plan = MocksAndUtils.createDeploymentPlanWithNova(deploymentService);

        ScheduleRequest scheduleRequest = new ScheduleRequest();
        long scheduleRequestId = ThreadLocalRandom.current().nextInt();
        scheduleRequest.setId( scheduleRequestId );

        // When
        when(this.deploymentUtils.buildGcspPassUrl(String.valueOf(plan.getNova().getGcspPass()))).
                thenReturn(String.valueOf(plan.getNova().getGcspPass()));
        when(this.scheduleControlMClient.getActiveRequestAt(any(), any(), any())).
                thenReturn(scheduleRequest);

        // Then
        var novaDto = this.deploymentNovaService.getNovaDto(plan, "ivUser");

        // Verify
        assertEquals(plan.getNova().getUndeployRelease(), novaDto.getUndeployRelease().longValue());
        assertEquals(deploymentService.toString(), novaDto.getPriorityLevel());
        assertEquals(plan.getNova().getDeploymentDateTime().toInstant().toString(), novaDto.getDeploymentDateTime());
        assertEquals(plan.getNova().getDeploymentList(),
                Arrays.stream(novaDto.getDeploymentList()).
                        boxed().map(Objects::toString).collect(Collectors.joining(",")));
        assertEquals(scheduleRequest.getId().toString(), novaDto.getBatchPlanId().toString());
    }

    @Test
    public void getNovaDtoWithEmptyNova()
    {
        //Prepare
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        plan.setNova(new DeploymentNova());

        // When
        when(this.deploymentUtils.buildGcspPassUrl(String.valueOf(plan.getNova().getGcspPass()))).
                thenReturn(String.valueOf(plan.getNova().getGcspPass()));
        when(this.scheduleControlMClient.getActiveRequestAt(any(), any(), any())).
                thenReturn(null);

        // Then
        var novaDto = this.deploymentNovaService.getNovaDto(plan, "ivUser");

        // Verify
        assertEquals(0, novaDto.getUndeployRelease().longValue());
        assertEquals(null, novaDto.getPriorityLevel());

    }

    @Test
    public void getDeploymentActionsWithService()
    {
        //Prepare
        var plan = MocksAndUtils.createDeploymentPlanWithNova(DeploymentPriority.SERVICE);
        var deploymentDto = new DeploymentDto();
        var deploymentService = new DeploymentService();
        var releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceName("serviceName");
        deploymentService.setService(releaseVersionService);

        //when
        when(this.deploymentPlanDtoBuilder.build(plan, Constants.IMMUSER)).
                thenReturn(deploymentDto);

        when(this.deploymentServiceRepository.findById(any())).thenReturn(
                Optional.of(deploymentService)
        );

        // Then
        var result = this.deploymentNovaService.getDeploymentActions(plan);

        // Verify
        assertTrue(result.contains("UNDEPLOY_PRO"));
        assertTrue(result.contains("DEPLOY_PRO"));
        assertTrue(result.contains("SERVICE_START"));
        assertTrue(result.contains("serviceName"));
    }

    @Test
    public void getDeploymentActionsWithSubsystem()
    {
        //Prepare
        var plan = MocksAndUtils.createDeploymentPlanWithNova(DeploymentPriority.SUBSYSTEM);
        var deploymentDto = new DeploymentDto();
        var deploymentSubsystemDto = new DeploymentSubsystemDto();
        DeploymentSubsystemDto[] deploymentSubsystemDtos= {deploymentSubsystemDto};
        deploymentDto.setSubsystems(deploymentSubsystemDtos);

        //when
        when(this.deploymentPlanDtoBuilder.build(plan, Constants.IMMUSER)).
                thenReturn(deploymentDto);

        // Then
        var result = this.deploymentNovaService.getDeploymentActions(plan);

        // Verify
        assertTrue(result.contains("UNDEPLOY_PRO"));
        assertTrue(result.contains("DEPLOY_PRO"));
        assertTrue(result.contains("SUBSYSTEM_START"));
    }

    @Test
    public void getDeploymentActionsWithProduct()
    {
        //Prepare
        var plan = MocksAndUtils.createDeploymentPlanWithNova(DeploymentPriority.PRODUCT);

        // Then
        var result = this.deploymentNovaService.getDeploymentActions(plan);

        // Verify
        assertTrue(result.contains("UNDEPLOY_PRO"));
        assertTrue(result.contains("DEPLOY_PRO"));
        assertTrue(result.contains("RELEASE_START"));
    }

    @Test
    public void validateNovaPlannedForDeploy()
    {

        var plan = MocksAndUtils.createDeploymentPlanWithNova(DeploymentPriority.SERVICE);
        this.deploymentNovaService.validateNovaPlannedForDeploy(plan);
    }

    @Test
    public void validateNovaPlannedForDeployWithNovaNull()
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);

        //THEN
        assertThrows(NovaException.class, () ->
                this.deploymentNovaService.validateNovaPlannedForDeploy(plan)
        );

    }

    @Test
    public void validateNovaPlannedForDeployWithStatusScheduled()
    {
        var plan = MocksAndUtils.createDeploymentPlanWithNova(DeploymentPriority.SERVICE);
        plan.setStatus(DeploymentStatus.SCHEDULED);

        //THEN
        assertThrows(NovaException.class, () ->
                this.deploymentNovaService.validateNovaPlannedForDeploy(plan)
        );
    }

    @Test
    public void validateNovaPlannedForDeployWithPriorityLevelNull()
    {
        var plan = MocksAndUtils.createDeploymentPlanWithNova(DeploymentPriority.SERVICE);
        plan.getNova().setPriorityLevel(null);

        //THEN
        assertThrows(NovaException.class, () ->
                this.deploymentNovaService.validateNovaPlannedForDeploy(plan)
        );
    }
}
