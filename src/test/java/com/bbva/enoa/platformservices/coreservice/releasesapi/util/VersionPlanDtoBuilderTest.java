package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

import com.bbva.enoa.apirestgen.releasesapi.model.RELVersionPlanDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VersionPlanDtoBuilderTest
{

    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @InjectMocks
    private VersionPlanDtoBuilder versionPlanDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void buildNoPlan()
    {
        Assertions.assertNull(versionPlanDtoBuilder.build(1, Environment.INT, DeploymentStatus.DEFINITION));
    }

    @Test
    public void build()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(RandomUtils.nextInt(0,1000));
        deploymentPlan.setCreationDate(Calendar.getInstance());
        deploymentPlan.setExecutionDate(Calendar.getInstance());
        List<DeploymentPlan> planList = new ArrayList<>();
        planList.add(deploymentPlan);

        RELVersionPlanDto current;
        int releaseVersion = 0;
        for(Environment env : Environment.values())
        {
            for(DeploymentStatus status : DeploymentStatus.values())
            {
                releaseVersion++;
                deploymentPlan.setEnvironment(env.getEnvironment());
                Mockito.when(this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(releaseVersion, env.getEnvironment(), status)).thenReturn(planList);
                current = this.versionPlanDtoBuilder.build(releaseVersion, env, status);

                //verify
                Mockito.verify(this.deploymentPlanRepository, Mockito.times(1)).getByReleaseVersionAndEnvironmentAndStatus(releaseVersion,env.getEnvironment(),status);
                Assertions.assertEquals(env.name(), current.getEnvironment());
                Assertions.assertEquals(deploymentPlan.getId(),current.getId());
            }
        }
    }
}