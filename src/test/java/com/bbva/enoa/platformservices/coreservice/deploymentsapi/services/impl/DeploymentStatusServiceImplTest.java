package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class DeploymentStatusServiceImplTest
{
    @Mock
    private DeploymentPlanRepository planRepository;
    @Mock
    private DeploymentSubsystemRepository subsystemRepository;
    @Mock
    private DeploymentServiceRepository serviceRepository;
    @Mock
    private DeploymentInstanceRepository instanceRepository;
    @InjectMocks
    private DeploymentStatusServiceImpl deploymentStatusService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getDeploymentPlanStatus()
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEPLOYED);
        plan.setAction(DeploymentAction.READY);
        when(this.planRepository.findById(1)).thenReturn(Optional.of(plan));
        ActionStatus response = this.deploymentStatusService.getDeploymentPlanStatus(1);
        assertEquals("READY", response.getAction());
        assertEquals("DEPLOYED", response.getStatus());
    }

    @Test
    public void getDeploymentSubsystemStatus()
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEPLOYED);
        plan.setAction(DeploymentAction.READY);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        subsystem.setAction(DeploymentAction.READY);
        when(this.subsystemRepository.findById(1)).thenReturn(Optional.of(subsystem));
        ActionStatus response = this.deploymentStatusService.getDeploymentSubsystemStatus(1);
        assertEquals("READY", response.getAction());
        assertEquals("DEPLOYED", response.getStatus());
    }

    @Test
    public void getDeploymentServiceStatus()
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEPLOYED);
        plan.setAction(DeploymentAction.READY);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        subsystem.setAction(DeploymentAction.READY);
        DeploymentService service = new DeploymentService();
        service.setDeploymentSubsystem(subsystem);
        when(this.serviceRepository.findById(1)).thenReturn(Optional.of(service));
        ActionStatus response = this.deploymentStatusService.getDeploymentServiceStatus(1);
        assertEquals("READY", response.getAction());
        assertEquals("DEPLOYED", response.getStatus());
    }

    @Test
    public void getDeploymentInstanceStatus()
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEPLOYED);
        plan.setAction(DeploymentAction.READY);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        subsystem.setAction(DeploymentAction.READY);
        DeploymentService service = new DeploymentService();
        service.setDeploymentSubsystem(subsystem);
        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setService(service);
        when(this.instanceRepository.findById(1)).thenReturn(Optional.of(instance));
        ActionStatus response = this.deploymentStatusService.getDeploymentInstanceStatus(1);
        assertEquals("READY", response.getAction());
        assertEquals("DEPLOYED", response.getStatus());
    }
}
