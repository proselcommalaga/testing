package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class ArchiveReleaseVersionServiceImplTest
{
    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private IReleaseVersionValidator iReleaseVersionValidator;
    @InjectMocks
    private ArchiveReleaseVersionServiceImpl archiveReleaseVersionService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void archiveReleaseVersion()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        this.archiveReleaseVersionService.archiveReleaseVersion("CODE", 1);
        Assertions.assertEquals(ReleaseVersionStatus.STORAGED, releaseVersion.getStatus());
    }
}