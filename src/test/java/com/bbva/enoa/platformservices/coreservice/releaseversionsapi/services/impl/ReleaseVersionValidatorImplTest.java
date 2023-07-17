package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

public class ReleaseVersionValidatorImplTest
{
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @InjectMocks
    private ReleaseVersionValidatorImpl releaseVersionValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkReleaseVersionExistance()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.checkReleaseVersionExistance(null));
        this.releaseVersionValidator.checkReleaseVersionExistance(releaseVersion);
    }

    @Test
    public void checkRelaseVersionStatusNotCompiling()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.STORAGED);
        this.releaseVersionValidator.checkRelaseVersionStatusNotCompiling(releaseVersion);
        this.releaseVersionValidator.checkReleaseVersionExistance(releaseVersion);
        releaseVersion.setStatus(ReleaseVersionStatus.BUILDING);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.checkRelaseVersionStatusNotCompiling(releaseVersion));
    }

    @Test
    public void existsReleaseVersionWithSameName()
    {
        Release release = new Release();
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setVersionName("Name");
        release.getReleaseVersions().add(releaseVersion);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.existsReleaseVersionWithSameName(release, "Name"));
    }

    @Test
    public void checkIfReleaseVersionCanBeStored()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.getDeployments().add(new DeploymentPlan());
        this.releaseVersionValidator.checkIfReleaseVersionCanBeStored(1, releaseVersion);
        when(this.deploymentPlanRepository.releaseVersionHasPlanNotStorage(1)).thenReturn(true);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.checkIfReleaseVersionCanBeStored(1, releaseVersion));
    }

    @Test
    public void checkMaxReleaseVersions()
    {
        when(this.releaseVersionRepository.countByProductIdAndStatusNot(1, ReleaseVersionStatus.STORAGED)).thenReturn(1);
        this.releaseVersionValidator.checkMaxReleaseVersions(1, 2);
        when(this.releaseVersionRepository.countByProductIdAndStatusNot(1, ReleaseVersionStatus.STORAGED)).thenReturn(6);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.checkMaxReleaseVersions(1, 2));
        when(this.releaseVersionRepository.countByProductIdAndStatusNot(1, ReleaseVersionStatus.STORAGED)).thenReturn(7);
        this.releaseVersionValidator.checkMaxReleaseVersions(1, 3);
        when(this.releaseVersionRepository.countByProductIdAndStatusNot(1, ReleaseVersionStatus.STORAGED)).thenReturn(9);
        this.releaseVersionValidator.checkMaxReleaseVersions(1, 4);
    }

    @Test
    public void checkCompilingReleaseVersions()
    {
        when(this.releaseVersionRepository.countByProductIdAndStatus(1, ReleaseVersionStatus.BUILDING)).thenReturn(0);
        this.releaseVersionValidator.checkCompilingReleaseVersions(1);
        when(this.releaseVersionRepository.countByProductIdAndStatus(1, ReleaseVersionStatus.BUILDING)).thenReturn(2);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.checkCompilingReleaseVersions(1));
    }

    @Test
    public void checkReleaseVersionHasPlan()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        this.releaseVersionValidator.checkReleaseVersionHasPlan(releaseVersion);
        releaseVersion.getDeployments().add(new DeploymentPlan());
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionValidator.checkReleaseVersionHasPlan(releaseVersion));
    }
}