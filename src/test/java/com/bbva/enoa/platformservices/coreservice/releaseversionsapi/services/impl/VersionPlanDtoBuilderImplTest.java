package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVVersionPlanDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;


import java.util.Calendar;
import java.util.List;

import static org.mockito.Mockito.*;

public class VersionPlanDtoBuilderImplTest
{
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @InjectMocks
    private VersionPlanDtoBuilderImpl versionPlanDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildEmptyPath()
    {
        // Given
        Mockito.when(this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(anyInt(), anyString(), any(DeploymentStatus.class))).thenReturn(List.of());

        //Then
        Assertions.assertNull(this.versionPlanDtoBuilder.build(1, Environment.PORTAL, DeploymentStatus.SCHEDULED), "The build service didn't return a null when the getByReleaseVersionAndEnvironmentAndStatus (DeploymentPlanRepository component) retrieve an empty list");
    }

    @Test
    public void build()
    {
        // Given
        DeploymentPlan deploymentPlanMock = Mockito.mock(DeploymentPlan.class);

        // And
        Mockito.when(this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(anyInt(), anyString(), any(DeploymentStatus.class))).thenReturn(List.of(deploymentPlanMock));
        Mockito.when(deploymentPlanMock.getId()).thenReturn(1);
        Mockito.when(deploymentPlanMock.getEnvironment()).thenReturn(Environment.PORTAL.getEnvironment());
        Mockito.when(deploymentPlanMock.getCreationDate()).thenReturn(Calendar.getInstance());
        Mockito.when(deploymentPlanMock.getExecutionDate()).thenReturn(Calendar.getInstance());

        //Then
        RVVersionPlanDTO result = Assertions.assertDoesNotThrow(() -> this.versionPlanDtoBuilder.build(1, Environment.PORTAL, DeploymentStatus.SCHEDULED), "There was an unsupported error in build service");
        Assertions.assertEquals(deploymentPlanMock.getId(), result.getId());
        Assertions.assertEquals(deploymentPlanMock.getEnvironment().toString(), result.getEnvironment());
        Assertions.assertEquals(deploymentPlanMock.getCreationDate().getTimeInMillis(), result.getCreationDate());
        Assertions.assertEquals(deploymentPlanMock.getExecutionDate().getTimeInMillis(), result.getExecutionDate());
    }
}
