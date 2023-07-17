package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.impl.QualityManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ILibraryValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;

public class DeleteReleaseVersionServiceImplTest
{
    @Mock
    private IReleaseVersionValidator iReleaseVersionValidator;
    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IDeploymentsService service;
    @Mock
    private QualityManagerService qualityManagerService;
    @Mock
    private IApiGatewayService iApiGatewayService;
    @Mock
    private ILibraryManagerService iLibraryManagerService;
    @Mock
    private IBatchScheduleService batchScheduleService;
    @Mock
    private ILibraryValidator libraryValidator;
    @Mock
    private ILibraryManagerService libraryManagerService;
    @Mock
    private IApiManagerService apiManagerService;
    @InjectMocks
    private DeleteReleaseVersionServiceImpl deleteReleaseVersionService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deleteReleaseVersion() throws Exception
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        when(this.releaseVersionRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(releaseVersion));
        doNothing().when(this.libraryManagerService).removeLibraries(releaseVersion);
        doNothing().when(this.apiManagerService).refreshUnimplementedApiVersionsState(Mockito.any(ReleaseVersion.class));
        this.deleteReleaseVersionService.deleteReleaseVersion("CODE", 1);
        verify(this.releaseVersionRepository, times(1)).delete(releaseVersion);
    }

    @Test
    void deleteReleaseVersionWithError() throws Exception
    {
        when(this.releaseVersionRepository.findById(Mockito.anyInt())).thenReturn(Optional.empty());
        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.deleteReleaseVersionService.deleteReleaseVersion("CODE", 1));
        Assertions.assertEquals(ReleaseVersionError.getNoSuchReleaseVersionError(1).getErrorCode(), exception.getNovaError().getErrorCode());
    }
}
