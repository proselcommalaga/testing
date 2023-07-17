package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RepositoryManagerServiceImplTest
{
    @Mock
    private DeploymentPlanRepository planRepository;
    @InjectMocks
    private RepositoryManagerServiceImpl repositoryManagerService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void savePlan()
    {
        DeploymentPlan plan = new DeploymentPlan();
        this.repositoryManagerService.savePlan(plan);
        verify(this.planRepository, times(1)).saveAndFlush(plan);
    }

    @Test
    public void findPlan()
    {
        this.repositoryManagerService.findPlan(1);
        verify(this.planRepository, times(1)).findById(1);
    }
}
