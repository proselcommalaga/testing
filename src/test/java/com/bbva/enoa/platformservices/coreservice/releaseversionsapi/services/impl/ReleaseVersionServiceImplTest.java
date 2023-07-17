package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlRequirement;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates.ValidationStatus;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.*;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.mockito.Mockito.*;

public class ReleaseVersionServiceImplTest
{

    @Mock
    private INewReleaseVersionDtoBuilder iNewReleaseVersionDtoBuilder;
    @Mock
    private IReleaseVersionDtoBuilder iReleaseVersionDtoBuilder;
    @Mock
    private ReleaseRepository releaseRepository;
    @Mock
    private ReleaseValidator releaseValidator;
    @Mock
    private IReleaseVersionValidator iReleaseVersionValidator;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private IReleaseVersionEntityBuilderService iReleaseVersionEntityBuilderService;
    @Mock
    private IReleaseVersionDtoBuilderService iReleaseVersionDtoBuilderService;
    @Mock
    private ISubsystemValidator iSubsystemValidator;
    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private IErrorTaskManager errorTaskManager;
    @Mock
    private IDeleteReleaseVersionService iDeleteReleaseVersionService;
    @Mock
    private ReleaseVersionSubsystemRepository releaseVersionSubsystemRepository;
    @Mock
    private IArchiveReleaseVersionService iArchiveReleaseVersionService;
    @Mock
    private IProductUsersClient usersService;
    @Mock
    private IApiGatewayService iApiGatewayService;

    @Mock
    private TagValidatorImpl tagValidator;

    @Mock
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;

    @Mock
    private ServiceDtoBuilderImpl serviceDtoBuilder;

    @Mock
    private ILibraryManagerService iLibraryManagerService;

    @InjectMocks
    private ReleaseVersionServiceImpl releaseVersionService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createReleaseVersion()
    {

        // Given
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        RVVersionDTO rvVersionDTO = new RVVersionDTO();
        rvVersionDTO.setVersionName("KRIPTON");
        ArgumentCaptor<Release> releaseCaptor = ArgumentCaptor.forClass(Release.class);
        ArgumentCaptor<ReleaseVersion> releaseVersionArgumentCaptor = ArgumentCaptor.forClass(ReleaseVersion.class);
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        // And
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(releaseVersionMock.getId()).thenReturn(1);
        Mockito.when(this.releaseRepository.findById(anyInt())).thenReturn(Optional.of(releaseMock));
        Mockito.doNothing().when(this.releaseValidator).checkReleaseExistence(any(Release.class));
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);
        Mockito.doNothing().when(this.releaseValidator).checkProductExistence(any(Product.class));
        Mockito.when(productMock.getId()).thenReturn(1);
        Mockito.doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        Mockito.doNothing().when(this.iReleaseVersionValidator).existsReleaseVersionWithSameName(any(Release.class), anyString());
        Mockito.when(this.usersService.isPlatformAdmin(anyString())).thenReturn(true);
        Mockito.when(this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(any(Release.class), any(RVVersionDTO.class), anyString(), anyBoolean())).thenReturn(releaseVersionMock);
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).processTemplates(any(ReleaseVersion.class), anyString());
        Mockito.doNothing().when(this.iLibraryManagerService).storeNovaLibrariesRequirements(releaseVersionMock);
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).buildSubsystems(any(Product.class), any(ReleaseVersion.class), anyString());
        Mockito.doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));

        // Then
        this.releaseVersionService.createReleaseVersion(rvVersionDTO, "XE11111", 1);
        Mockito.verify(this.iSubsystemValidator).checkReleaseSubsystems(releaseCaptor.capture());
        Assertions.assertEquals(1, releaseCaptor.getValue().getId());
        Mockito.verify(this.iLibraryManagerService).storeNovaLibrariesRequirements(releaseVersionArgumentCaptor.capture());
        Assertions.assertEquals(1, releaseVersionArgumentCaptor.getValue().getId());
        Mockito.verify(this.releaseValidator).checkProductExistence(productArgumentCaptor.capture());
        Assertions.assertEquals(productMock.getId(), productArgumentCaptor.getValue().getId());
    }

    @Test
    public void createReleaseVersionReleaseExistenceError()
    {

        // Given
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        RVVersionDTO rvVersionDTO = new RVVersionDTO();
        rvVersionDTO.setVersionName("KRIPTON");

        // And
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(releaseVersionMock.getId()).thenReturn(1);
        Mockito.when(this.releaseRepository.findById(anyInt())).thenReturn(Optional.of(releaseMock));
        Mockito.doThrow(NovaException.class).when(this.releaseValidator).checkReleaseExistence(any(Release.class));
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.createReleaseVersion(rvVersionDTO, "XE11111", 1), "The releaseValidator component didn't throw a NovaException in the checkReleaseExistence service");
    }

    @Test
    public void createReleaseVersionHasPermissionError()
    {

        // Given
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        RVVersionDTO rvVersionDTO = new RVVersionDTO();
        rvVersionDTO.setVersionName("KRIPTON");

        // And
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(releaseVersionMock.getId()).thenReturn(1);
        Mockito.when(this.releaseRepository.findById(anyInt())).thenReturn(Optional.of(releaseMock));
        Mockito.doNothing().when(this.releaseValidator).checkReleaseExistence(any(Release.class));
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);
        Mockito.doNothing().when(this.releaseValidator).checkProductExistence(any(Product.class));
        Mockito.when(productMock.getId()).thenReturn(1);
        Mockito.doThrow(IllegalArgumentException.class).when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

        // Then
        Assertions.assertThrows(IllegalArgumentException.class, () -> this.releaseVersionService.createReleaseVersion(rvVersionDTO, "XE11111", 1), "The usersService component didn't throw an IllegalArgumentException in checkHasPermission service");
    }

    @Test
    public void createReleaseVersionWithSameNameError()
    {

        // Given
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        RVVersionDTO rvVersionDTO = new RVVersionDTO();
        rvVersionDTO.setVersionName("KRIPTON");


        // And
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(releaseVersionMock.getId()).thenReturn(1);
        Mockito.when(this.releaseRepository.findById(anyInt())).thenReturn(Optional.of(releaseMock));
        Mockito.doNothing().when(this.releaseValidator).checkReleaseExistence(any(Release.class));
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);
        Mockito.doNothing().when(this.releaseValidator).checkProductExistence(any(Product.class));
        Mockito.when(productMock.getId()).thenReturn(1);
        Mockito.doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        Mockito.doThrow(NovaException.class).when(this.iReleaseVersionValidator).existsReleaseVersionWithSameName(any(Release.class), anyString());
        Mockito.when(this.usersService.isPlatformAdmin(anyString())).thenReturn(true);
        Mockito.when(this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(any(Release.class), any(RVVersionDTO.class), anyString(), anyBoolean())).thenReturn(releaseVersionMock);

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.createReleaseVersion(rvVersionDTO, "XE11111", 1), "The ReleaseVersionValidator component didn't throw a NovaException in the existsReleaseVersionWithSameName service");

    }

    @Test
    public void createReleaseVersionFailedStoreLibrariesRequirements()
    {
        // Given
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        RVVersionDTO rvVersionDTO = new RVVersionDTO();
        rvVersionDTO.setVersionName("KRIPTON");

        // And
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(releaseVersionMock.getId()).thenReturn(1);
        Mockito.when(this.releaseRepository.findById(anyInt())).thenReturn(Optional.of(releaseMock));
        Mockito.doNothing().when(this.releaseValidator).checkReleaseExistence(any(Release.class));
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);
        Mockito.doNothing().when(this.releaseValidator).checkProductExistence(any(Product.class));
        Mockito.when(productMock.getId()).thenReturn(1);
        Mockito.doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        Mockito.doNothing().when(this.iReleaseVersionValidator).existsReleaseVersionWithSameName(any(Release.class), anyString());
        Mockito.when(this.usersService.isPlatformAdmin(anyString())).thenReturn(true);
        Mockito.when(this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(any(Release.class), any(RVVersionDTO.class), anyString(), anyBoolean())).thenReturn(releaseVersionMock);
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).processTemplates(any(ReleaseVersion.class), anyString());
        Mockito.doThrow(NovaException.class).when(this.iLibraryManagerService).storeNovaLibrariesRequirements(releaseVersionMock);
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).buildSubsystems(any(Product.class), any(ReleaseVersion.class), anyString());
        Mockito.doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.createReleaseVersion(rvVersionDTO, "XE11111", 1), "The LibraryManagerService component didn't throw a NovaException in the storeNovaLibrariesRequirements service");

    }

    @Test
    public void addReleaseVersionError()
    {

        // Given
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        ReleaseVersion releaseVersionMock = Mockito.mock(ReleaseVersion.class);
        RVReleaseVersionDTO releaseVersionDto = new RVReleaseVersionDTO();
        ArgumentCaptor<Release> releaseCaptor = ArgumentCaptor.forClass(Release.class);
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        // And
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);
        Mockito.when(productMock.getId()).thenReturn(1);
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).processTemplates(any(ReleaseVersion.class), anyString());
        Mockito.doNothing().when(this.iLibraryManagerService).storeNovaLibrariesRequirements(any(ReleaseVersion.class));
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).buildSubsystems(any(Product.class), any(ReleaseVersion.class), anyString());
        Mockito.when(this.releaseRepository.findById(anyInt())).thenReturn(Optional.of(releaseMock));
        Mockito.doNothing().when(this.releaseValidator).checkReleaseExistence(any(Release.class));
        Mockito.doNothing().when(this.releaseValidator).checkProductExistence(any(Product.class));
        Mockito.doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        Mockito.doNothing().when(this.iReleaseVersionValidator).existsReleaseVersionWithSameName(any(Release.class), anyString());
        Mockito.doNothing().when(this.iSubsystemValidator).checkReleaseSubsystems(any(Release.class));
        Mockito.when(this.usersService.isPlatformAdmin(anyString())).thenReturn(true);
        Mockito.when(this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(any(Release.class), any(RVReleaseVersionDTO.class), anyString(), anyBoolean())).thenReturn(releaseVersionMock);
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).processTemplates(any(ReleaseVersion.class), anyString());
        Mockito.doNothing().when(this.iLibraryManagerService).storeNovaLibrariesRequirements(any(ReleaseVersion.class));
        Mockito.doNothing().when(this.iReleaseVersionDtoBuilderService).buildSubsystems(any(Product.class), any(ReleaseVersion.class), anyString());

        // Then
        this.releaseVersionService.addReleaseVersion(releaseVersionDto, "CODE", 1);
        Mockito.verify(this.releaseValidator).checkReleaseExistence(releaseCaptor.capture());
        Assertions.assertEquals(releaseMock.getId(), releaseCaptor.getValue().getId());
        Mockito.verify(this.iReleaseVersionDtoBuilderService).buildSubsystems(productArgumentCaptor.capture(), any(ReleaseVersion.class), anyString());
        Assertions.assertEquals(productMock.getId(), productArgumentCaptor.getValue().getId());

    }

    @Test
    public void addReleaseVersionError2()
    {
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setId(1);
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        when(this.releaseRepository.findById(1)).thenReturn(Optional.of(release));
        RVReleaseVersionDTO releaseVersionDto = new RVReleaseVersionDTO();
        when(this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(release, releaseVersionDto, "XE0000", false)).thenReturn(releaseVersion);
        doThrow(NovaException.class).when(this.releaseRepository).findById(1);

        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.addReleaseVersion(releaseVersionDto, "CODE", 1), "The ReleaseRepository didn't throw a NovaException in the findById service");
    }

    @Test
    public void subsystemBuildStatus()
    {
        this.releaseVersionService.subsystemBuildStatus("CODE", 1, "JOB", "MSG", "SUCCESS");
        verify(this.iReleaseVersionDtoBuilderService, times(1)).subsystemBuildStatus(1, "MSG", "SUCCESS", "CODE");

        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.subsystemBuildStatus("CODE", 1, "JOB", "MSG", null), "The releaseVersionService component didn't throw a NovaException when we pass a null status in subsystemBuildStatus service");
    }

    @Test
    public void subsystemBuildStatusError()
    {
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).when(this.iReleaseVersionDtoBuilderService
        ).subsystemBuildStatus(1, "MSG", "SUCCESS", "CODE");
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.subsystemBuildStatus("CODE", 1, "JOB", "MSG", null), "The releaseVersionService component didn't throw a NovaException when we pass a null status in the subsystemBuildStatus service");
    }

    @Test
    public void subsystemBuildStatusError2()
    {
        doThrow(new RuntimeException()).when(this.iReleaseVersionDtoBuilderService).subsystemBuildStatus(1, "MSG", "SUCCESS", "CODE");
        Assertions.assertThrows(RuntimeException.class, () -> this.releaseVersionService.subsystemBuildStatus("CODE", 1, "JOB", "MSG", null), "The releaseVersionService component didn't throw a RuntimeException when we pass a null status in the subsystemBuildStatus service");
    }

    @Test
    public void archiveReleaseVersion()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        doNothing().when(this.novaActivityEmitter).emitNewActivity(Mockito.any(GenericActivity.class));
        this.releaseVersionService.archiveReleaseVersion("CODE", 1);
        verify(this.iArchiveReleaseVersionService, times(1)).archiveReleaseVersion("CODE", 1);


        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).when(
                this.iArchiveReleaseVersionService).archiveReleaseVersion("CODE", 1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.archiveReleaseVersion("CODE", 1), "The ArchiveReleaseVersionService component didn't throw a NovaException in archiveReleaseVersion service");
    }

    @Test
    public void archiveReleaseVersionRepositoryError()
    {
        // Given
        Mockito.when(this.releaseVersionRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.archiveReleaseVersion("XE11111", 1), "The releaseVersionService component didn't throw a NovaException in the archiveReleaseVersion service");
    }

    @Test
    public void archiveReleaseVersionError() throws Exception
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        doThrow(NovaException.class).when(this.iArchiveReleaseVersionService).archiveReleaseVersion("CODE", 1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.archiveReleaseVersion("CODE", 1), "The ArchiveReleaseVersionService component didn't throw a NovaException in the archiveReleaseVersion service");
    }

    @Test
    public void getReleaseVersionRepositoryError()
    {
        // Given
        Mockito.when(this.releaseVersionRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.getReleaseVersion("CODE", 1), "The releaseVersionService component didn't throw a NovaException in the getReleaseVersion service");
    }

    @Test
    public void getReleaseVersion()
    {
        RVReleaseVersionDTO releaseVersionDto = new RVReleaseVersionDTO();
        ReleaseVersion releaseVersion = new ReleaseVersion();
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        when(this.iReleaseVersionDtoBuilder.build(releaseVersion)).thenReturn(releaseVersionDto);
        RVReleaseVersionDTO response = this.releaseVersionService.getReleaseVersion("CODE", 1);
        Assertions.assertEquals(releaseVersionDto, response);
        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).
                when(this.releaseVersionRepository).findById(1);

        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.getReleaseVersion("CODE", 1), "The releaseVersionRepository didn't throw a NovaException in the findById service");
    }

    @Test
    public void getReleaseVersionError() throws Exception
    {
        RVReleaseVersionDTO releaseVersionDto = new RVReleaseVersionDTO();
        ReleaseVersion releaseVersion = new ReleaseVersion();
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        when(this.iReleaseVersionDtoBuilder.build(releaseVersion)).thenReturn(releaseVersionDto);
        doThrow(NovaException.class).when(this.releaseVersionRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.getReleaseVersion("CODE", 1), "The releaseVersionRepository didn't throw a NovaException in the findById service");
    }

    @Test
    public void updateReleaseVersion() throws Exception
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        RVReleaseVersionDTO response = this.releaseVersionService.updateReleaseVersion("CODE", 1, "Desc");
        Assertions.assertEquals("Desc", releaseVersion.getDescription());

        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).
                when(this.releaseVersionRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.updateReleaseVersion("CODE", 1, "Desc"), "The releaseVersionRepository didn't throw a NovaException in the findById service");
    }

    @Test
    public void updateReleaseVersionRepositoryError()
    {
        // Given
        Mockito.when(this.releaseVersionRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.updateReleaseVersion("CODE", 1, "Desc"), "The releaseVersionService component didn't throw a NovaException in the updateReleaseVersion service");
    }

    @Test
    public void updateReleaseVersionError()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        doThrow(NovaException.class).when(this.releaseVersionRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.updateReleaseVersion("CODE", 1, "Desc"), "The releaseVersionRepository component didn't throw a NovaException in the findById service");
    }

    @Test
    public void deleteReleaseVersion()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        this.releaseVersionService.deleteReleaseVersion("CODE", 1);
        verify(this.iDeleteReleaseVersionService, times(1)).deleteReleaseVersion("CODE", 1);

        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).
                when(this.iDeleteReleaseVersionService).deleteReleaseVersion("CODE", 1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.deleteReleaseVersion("CODE", 1), "The DeleteReleaseVersionService component didn't throw a NovaException in the deleteReleaseVersion service");
    }

    @Test
    public void deleteReleaseVersionRepositoryError()
    {
        // Given
        Mockito.when(this.releaseVersionRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.deleteReleaseVersion("CODE", 1), "The releaseVersionService component didn't throw a NovaException in the deleteReleaseVersion service");
    }

    @Test
    public void deleteReleaseVersionError()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        doThrow(NovaException.class).when(this.iDeleteReleaseVersionService).deleteReleaseVersion("CODE", 1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.deleteReleaseVersion("CODE", 1), "The DeleteReleaseVersionService component didn't throw a NovaException in the deleteReleaseVersion service");
    }

    @Test
    public void updateReleaseVersionIssue()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        this.releaseVersionService.updateReleaseVersionIssue("CODE", 1, "KEY");
        Assertions.assertEquals("KEY", releaseVersion.getIssueID());

        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).
                when(this.releaseVersionRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.updateReleaseVersionIssue("CODE", 1, "KEY"), "The releaseVersionRepository component didn't throw a NovaException in the findById service");
    }

    @Test
    public void updateReleaseVersionIssueRepositoryError()
    {
        // Given
        Mockito.when(this.releaseVersionRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Then
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.updateReleaseVersionIssue("CODE", 1, "KEY"), "The releaseVersionService component didn't throw a NovaException in the updateReleaseVersionIssue service");
    }

    @Test
    public void updateReleaseVersionIssueError()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        when(this.releaseVersionRepository.findById(1)).thenReturn(Optional.of(releaseVersion));
        doThrow(NovaException.class).when(this.releaseVersionRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.updateReleaseVersionIssue("CODE", 1, "KEY"), "The releaseVersionRepository component didn't throw a NovaException in the findById service");
    }

    @Test
    public void getStatuses()
    {
        Assertions.assertEquals(4, this.releaseVersionService.getStatuses().length);
    }

    @Test
    public void newReleaseVersion()
    {
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        when(this.releaseRepository.findById(1)).thenReturn(Optional.of(release));
        NewReleaseVersionDto newReleaseVersionDto = new NewReleaseVersionDto();
        when(this.iNewReleaseVersionDtoBuilder.build(release, product, "CODE")).thenReturn(newReleaseVersionDto);
        NewReleaseVersionDto response = this.releaseVersionService.newReleaseVersion("CODE", 1);
        Assertions.assertEquals(newReleaseVersionDto, response);

        doThrow(new NovaException(ReleaseVersionError.getUnexpectedError(), "")).
                when(this.releaseRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.newReleaseVersion("CODE", 1), "The releaseRepository component didn't throw a NovaException in the findById service");
    }

    @Test
    public void newReleaseVersionError()
    {
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        when(this.releaseRepository.findById(1)).thenReturn(Optional.of(release));
        NewReleaseVersionDto newReleaseVersionDto = new NewReleaseVersionDto();
        when(this.iNewReleaseVersionDtoBuilder.build(release, product, "CODE")).thenReturn(newReleaseVersionDto);
        doThrow(NovaException.class).when(this.releaseRepository).findById(1);
        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.newReleaseVersion("CODE", 1), "The releaseRepository component didn't throw a NovaException in the findById service");
    }

    @Test
    public void validateTagWithSameServices()
    {
        Integer releaseId = RandomUtils.nextInt(0, 9999999);
        String ivUser = RandomStringUtils.randomAlphanumeric(10);

        RVValidationDTO validationDTO = new RVValidationDTO();

        validationDTO.setSubsystem(this.getRandomLibraryRVSubsystemBaseDTO());

        RVTagDTO tagDTO1 = this.getRandomRVTagDTO();
        RVTagDTO tagDTO2 = this.getRandomRVTagDTO();

        validationDTO.setTags(new RVTagDTO[]{tagDTO1, tagDTO2});

        List<String> projectNames = this.getRandomProjectNameList(5);

        List<RVServiceValidationDTO> serviceValidationDTOList = this.getServiceValidationDTO();
        RVErrorDTO[] rvErrorDTOArray = new RVErrorDTO[0];

        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO1, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO1, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);


        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO2, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO2, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);

        NovaYml novaYml = this.getNovaYMLByServiceDto(serviceValidationDTOList.get(0).getService());
        // pom without dependencies
        PomXML pom = new PomXML();

        pom.setGroupId("com.bbva." + novaYml.getUuaa());
        pom.setArtifactId(novaYml.getApplicationName());
        pom.setVersion(novaYml.getVersion());
        pom.setDescription(novaYml.getDescription());

        ValidatorInputs validatorInputs1 = new ValidatorInputs();
        validatorInputs1.setNovaYml(novaYml);
        validatorInputs1.setPom(pom);

        ValidatorInputs validatorInputs2 = new ValidatorInputs();
        validatorInputs2.setNovaYml(novaYml);
        validatorInputs2.setPom(pom);

        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName()))
                .thenReturn(validatorInputs1);
        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName()))
                .thenReturn(validatorInputs2);

        RVValidationResponseDTO response = this.releaseVersionService.validateAllTags(ivUser, releaseId, validationDTO);

        Assertions.assertTrue(Arrays.stream(response.getTagValidation())
                .flatMap(rvTagValidationDTO -> Arrays.stream(rvTagValidationDTO.getServiceValidation()))
                .flatMap(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()))
                .anyMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.WARNING.name())));

        Assertions.assertEquals(response.getStatus(), ValidationStatus.WARNING.name());
    }


    @Test
    public void validateTagSameVersionOverrideUuaa()
    {
        Integer releaseId = RandomUtils.nextInt(0, 9999999);
        String ivUser = RandomStringUtils.randomAlphanumeric(10);

        RVValidationDTO validationDTO = new RVValidationDTO();

        validationDTO.setSubsystem(this.getRandomLibraryRVSubsystemBaseDTO());

        RVTagDTO tagDTO1 = this.getRandomRVTagDTO();
        RVTagDTO tagDTO2 = this.getRandomRVTagDTO();

        validationDTO.setTags(new RVTagDTO[]{tagDTO1, tagDTO2});

        List<String> projectNames = this.getRandomProjectNameList(5);

        List<RVServiceValidationDTO> serviceValidationDTOList = this.getServiceValidationDTO();
        RVErrorDTO[] rvErrorDTOArray = this.getRandomErrorArray(3);

        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO1, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO1, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);


        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO2, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO2, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);

        NovaYml novaYml = this.getNovaYMLByServiceDto(serviceValidationDTOList.get(0).getService());

        ValidatorInputs validatorInputs1 = new ValidatorInputs();
        validatorInputs1.setNovaYml(novaYml);

        NovaYml copiedNovaYml = this.copyNovaYml(novaYml);
        copiedNovaYml.setUuaa("MPAZ");

        ValidatorInputs validatorInputs2 = new ValidatorInputs();
        validatorInputs2.setNovaYml(copiedNovaYml);

        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName()))
                .thenReturn(validatorInputs1);
        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName()))
                .thenReturn(validatorInputs2);

        RVValidationResponseDTO response = this.releaseVersionService.validateAllTags(ivUser, releaseId, validationDTO);

        Assertions.assertTrue(Arrays.stream(response.getTagValidation())
                .flatMap(rvTagValidationDTO -> Arrays.stream(rvTagValidationDTO.getServiceValidation()))
                .flatMap(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()))
                .anyMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.ERROR.name())));

        Assertions.assertEquals(response.getStatus(), ValidationStatus.ERROR.name());

    }

    @Test
    public void validateTagSameVersionOverrideRequirements()
    {
        Integer releaseId = RandomUtils.nextInt(0, 9999999);
        String ivUser = RandomStringUtils.randomAlphanumeric(10);

        RVValidationDTO validationDTO = new RVValidationDTO();

        validationDTO.setSubsystem(this.getRandomLibraryRVSubsystemBaseDTO());

        RVTagDTO tagDTO1 = this.getRandomRVTagDTO();
        RVTagDTO tagDTO2 = this.getRandomRVTagDTO();

        validationDTO.setTags(new RVTagDTO[]{tagDTO1, tagDTO2});

        List<String> projectNames = this.getRandomProjectNameList(5);

        List<RVServiceValidationDTO> serviceValidationDTOList = this.getServiceValidationDTO();
        RVErrorDTO[] rvErrorDTOArray = this.getRandomErrorArray(3);

        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO1, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO1, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);


        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO2, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO2, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);

        NovaYml novaYml = this.getNovaYMLByServiceDtoWithRequirements(serviceValidationDTOList.get(0).getService());

        ValidatorInputs validatorInputs1 = new ValidatorInputs();
        validatorInputs1.setNovaYml(novaYml);

        NovaYml copiedNovaYml = this.copyNovaYml(novaYml);

        ValidatorInputs validatorInputs2 = new ValidatorInputs();
        copiedNovaYml.getRequirements().forEach(req -> req.setValue(RandomStringUtils.randomAlphanumeric(5)));
        validatorInputs2.setNovaYml(copiedNovaYml);

        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName()))
                .thenReturn(validatorInputs1);
        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName()))
                .thenReturn(validatorInputs2);

        RVValidationResponseDTO response = this.releaseVersionService.validateAllTags(ivUser, releaseId, validationDTO);

        Assertions.assertTrue(Arrays.stream(response.getTagValidation())
                .flatMap(rvTagValidationDTO -> Arrays.stream(rvTagValidationDTO.getServiceValidation()))
                .flatMap(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()))
                .anyMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.ERROR.name())));

        Assertions.assertEquals(response.getStatus(), ValidationStatus.ERROR.name());

    }

    @Test
    public void validateTagOkResponse()
    {
        Integer releaseId = RandomUtils.nextInt(0, 9999999);
        String ivUser = RandomStringUtils.randomAlphanumeric(10);

        RVValidationDTO validationDTO = new RVValidationDTO();

        validationDTO.setSubsystem(this.getRandomLibraryRVSubsystemBaseDTO());

        RVTagDTO tagDTO1 = this.getRandomRVTagDTO();
        RVTagDTO tagDTO2 = this.getRandomRVTagDTO();

        validationDTO.setTags(new RVTagDTO[]{tagDTO1, tagDTO2});

        List<String> projectNames = this.getRandomProjectNameList(5);

        List<RVServiceValidationDTO> serviceValidationDTOList = this.getServiceValidationDTO();
        List<RVServiceValidationDTO> serviceValidationDTOList2 = this.getServiceValidationDTO();
        RVErrorDTO[] rvErrorDTOArray = this.getRandomErrorArray(3);

        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO1, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList);
        when(this.tagValidator.buildTagValidation(tagDTO1, validationDTO.getSubsystem(), serviceValidationDTOList, projectNames)).thenReturn(rvErrorDTOArray);


        when(this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName())).thenReturn(projectNames);
        when(this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO2, validationDTO.getSubsystem(), projectNames)).thenReturn(serviceValidationDTOList2);
        when(this.tagValidator.buildTagValidation(tagDTO2, validationDTO.getSubsystem(), serviceValidationDTOList2, projectNames)).thenReturn(rvErrorDTOArray);

        NovaYml novaYml = this.getNovaYMLByServiceDtoWithRequirements(serviceValidationDTOList.get(0).getService());

        ValidatorInputs validatorInputs1 = new ValidatorInputs();
        validatorInputs1.setNovaYml(novaYml);

        NovaYml novaYml2 = this.getNovaYMLByServiceDtoWithRequirements(serviceValidationDTOList2.get(0).getService());

        ValidatorInputs validatorInputs2 = new ValidatorInputs();
        validatorInputs2.setNovaYml(novaYml2);

        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO1.getTagName()))
                .thenReturn(validatorInputs1);
        when(this.serviceDtoBuilder.buildValidationFileInputs(serviceValidationDTOList2.get(0).getService().getFolder(), validationDTO.getSubsystem().getRepoId(), tagDTO2.getTagName()))
                .thenReturn(validatorInputs2);

        RVValidationResponseDTO response = this.releaseVersionService.validateAllTags(ivUser, releaseId, validationDTO);

        Assertions.assertTrue(Arrays.stream(response.getTagValidation())
                .flatMap(rvTagValidationDTO -> Arrays.stream(rvTagValidationDTO.getServiceValidation()))
                .flatMap(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()))
                .allMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.OK.name())));

        Assertions.assertEquals(response.getStatus(), ValidationStatus.OK.name(), "The response wasn't OK");
    }

    @Test
    public void validateAllTagsUnsupportMultitag()
    {
        Integer releaseId = RandomUtils.nextInt(0, 9999999);
        String ivUser = RandomStringUtils.randomAlphanumeric(10);

        RVValidationDTO validationDTO = new RVValidationDTO();

        RVTagDTO tagDTO1 = this.getRandomRVTagDTO();
        RVTagDTO tagDTO2 = this.getRandomRVTagDTO();

        validationDTO.setTags(new RVTagDTO[]{tagDTO1, tagDTO2});

        validationDTO.setSubsystem(this.getRandomNovaRVSubsystemBaseDTO());

        doCallRealMethod().when(tagValidator).validateAllowedMultitag(validationDTO);

        Assertions.assertThrows(NovaException.class, () -> this.releaseVersionService.validateAllTags(ivUser, releaseId, validationDTO), "The TAGValidator service didn't throw any NovaException in the validateAllowedMultitag service");
    }

    @Test
    public void getReleaseVersionSubsystems()
    {

        // Given
        ReleaseVersionSubsystem releaseVersionSubsystemMock1 = Mockito.mock(ReleaseVersionSubsystem.class);
        ReleaseVersionSubsystem releaseVersionSubsystemMock2 = Mockito.mock(ReleaseVersionSubsystem.class);

        // And
        Mockito.when(this.releaseVersionSubsystemRepository.findBySubsystemId(anyInt())).thenReturn(List.of(releaseVersionSubsystemMock1, releaseVersionSubsystemMock2));
        Mockito.when(releaseVersionSubsystemMock1.getTagName()).thenReturn("tag:release-1.0.0");
        Mockito.when(releaseVersionSubsystemMock2.getTagName()).thenReturn("tag:release-2.0.0");

        // Then
        Assertions.assertTrue(Arrays.stream(this.releaseVersionService.getReleaseVersionSubsystems(1)).parallel().anyMatch(r -> r.getTagName().equals("tag:release-1.0.0")), "There was changes in the list retrieved from persistence the releaseVersionSubsystemRepository");
    }

    @Test
    public void releaseVersionRequest()
    {

        // Given
        Release releaseMock = Mockito.mock(Release.class);
        Product productMock = Mockito.mock(Product.class);
        RVRequestDTO rvRequestDTO = new RVRequestDTO();

        ArgumentCaptor<Release> releaseCaptor = ArgumentCaptor.forClass(Release.class);
        ArgumentCaptor<Product> productArgumentCaptor = ArgumentCaptor.forClass(Product.class);

        // And
        Mockito.when(releaseMock.getProduct()).thenReturn(productMock);
        Mockito.when(releaseMock.getId()).thenReturn(1);
        Mockito.when(productMock.getId()).thenReturn(1);
        Mockito.when(this.releaseValidator.checkReleaseExistence(anyInt())).thenReturn(releaseMock);
        Mockito.doNothing().when(this.iSubsystemValidator).checkReleaseSubsystems(any(Release.class));
        Mockito.doNothing().when(this.releaseValidator).checkProductExistence(any(Product.class));
        Mockito.doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
        Mockito.when(this.iNewReleaseVersionDtoBuilder.build(any(Product.class), any(Release.class))).thenReturn(rvRequestDTO);
        Mockito.doNothing().when(this.iReleaseVersionValidator).checkMaxReleaseVersions(anyInt(), anyInt());
        Mockito.doNothing().when(this.iReleaseVersionValidator).checkCompilingReleaseVersions(anyInt());

        // Then
        this.releaseVersionService.releaseVersionRequest("XE11111", 1);
        Mockito.verify(this.iSubsystemValidator).checkReleaseSubsystems(releaseCaptor.capture());
        Mockito.verify(this.releaseValidator).checkProductExistence(productArgumentCaptor.capture());
        Assertions.assertEquals(releaseMock.getId(), releaseCaptor.getValue().getId(), "The release was changed in the checkReleaseSubsystems call");
        Assertions.assertEquals(productMock.getId(), productArgumentCaptor.getValue().getId(), "The product was changed in the checkProductExistence call");

    }

    private RVSubsystemBaseDTO getRandomLibraryRVSubsystemBaseDTO()
    {
        RVSubsystemBaseDTO rvSubsystemBaseDTO = new RVSubsystemBaseDTO();

        rvSubsystemBaseDTO.setId(RandomUtils.nextInt(0, 9999999));
        rvSubsystemBaseDTO.setRepoId(RandomUtils.nextInt(0, 9999999));
        rvSubsystemBaseDTO.setSubsystemName(RandomStringUtils.randomAlphanumeric(10));
        rvSubsystemBaseDTO.setSubsystemType(SubsystemType.LIBRARY.name());
        rvSubsystemBaseDTO.setDescription(RandomStringUtils.randomAlphanumeric(10));

        return rvSubsystemBaseDTO;
    }

    private RVSubsystemBaseDTO getRandomNovaRVSubsystemBaseDTO()
    {
        RVSubsystemBaseDTO rvSubsystemBaseDTO = new RVSubsystemBaseDTO();

        rvSubsystemBaseDTO.setId(RandomUtils.nextInt(0, 9999999));
        rvSubsystemBaseDTO.setRepoId(RandomUtils.nextInt(0, 9999999));
        rvSubsystemBaseDTO.setSubsystemName(RandomStringUtils.randomAlphanumeric(10));
        rvSubsystemBaseDTO.setSubsystemType(SubsystemType.NOVA.name());
        rvSubsystemBaseDTO.setDescription(RandomStringUtils.randomAlphanumeric(10));

        return rvSubsystemBaseDTO;
    }

    private RVTagDTO getRandomRVTagDTO()
    {
        RVTagDTO rvTagDTO = new RVTagDTO();

        rvTagDTO.setTagUrl(RandomStringUtils.randomAlphanumeric(10));
        rvTagDTO.setTagName(RandomStringUtils.randomAlphanumeric(10));
        rvTagDTO.setMessage(RandomStringUtils.randomAlphanumeric(10));

        return rvTagDTO;
    }

    private List<String> getRandomProjectNameList(int size)
    {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < size; i++)
        {
            result.add(RandomStringUtils.randomAlphanumeric(5));
        }
        return result;
    }

    private RVErrorDTO[] getRandomErrorArray(int size)
    {
        RVErrorDTO[] result = new RVErrorDTO[size];

        for (int i = 0; i < size; i++)
        {
            RVErrorDTO errorDTO = new RVErrorDTO();

            errorDTO.setStatus(ValidationStatus.values()[RandomUtils.nextInt(0, ValidationStatus.values().length)].name());
            errorDTO.setCode(RandomStringUtils.randomAlphanumeric(5));
            errorDTO.setMessage(RandomStringUtils.randomAlphanumeric(5));

            result[i] = errorDTO;
        }

        return result;
    }

    private List<RVServiceValidationDTO> getServiceValidationDTO()
    {
        List<RVServiceValidationDTO> serviceValidationDTOList = new ArrayList<>();

        RVServiceValidationDTO serviceValidationDTO = new RVServiceValidationDTO();

        serviceValidationDTO.setError(new RVErrorDTO[0]);

        RVServiceDTO service = new RVServiceDTO();
        service.fillRandomly(10, false, 0, 2);
        serviceValidationDTO.setService(service);

        serviceValidationDTOList.add(serviceValidationDTO);

        return serviceValidationDTOList;
    }

    private NovaYml getNovaYMLByServiceDto(RVServiceDTO serviceDTO)
    {
        NovaYml novaYml = new NovaYml();

        novaYml.setServiceType(serviceDTO.getServiceType());
        novaYml.setLanguage("JAVA");
        novaYml.setLanguageVersion("1.0");
        novaYml.setApplicationName(serviceDTO.getFinalName());
        novaYml.setName(serviceDTO.getServiceName());
        novaYml.setNovaVersion("19.04");
        novaYml.setProjectName(serviceDTO.getNovaServiceName());
        novaYml.setUuaa(serviceDTO.getUuaa());
        novaYml.setValid(true);
        novaYml.setDescription(serviceDTO.getDescription());

        return novaYml;
    }

    private NovaYml getNovaYMLByServiceDtoWithRequirements(RVServiceDTO serviceDTO)
    {
        NovaYml novaYml = new NovaYml();

        novaYml.setServiceType(serviceDTO.getServiceType());
        novaYml.setLanguage("JAVA");
        novaYml.setLanguageVersion("1.0");
        novaYml.setApplicationName(serviceDTO.getFinalName());
        novaYml.setName(serviceDTO.getServiceName());
        novaYml.setNovaVersion("19.04");
        novaYml.setProjectName(serviceDTO.getNovaServiceName());
        novaYml.setUuaa(serviceDTO.getUuaa());
        novaYml.setValid(true);
        novaYml.setDescription(serviceDTO.getDescription());

        this.fillRandomRequirementsWithRandomRelation(novaYml);

        return novaYml;
    }

    /**
     * Fill random number of requirements with null type
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithRandomRelation(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        for (int i = 0; i < 100; i++)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(randomRequirementName());
            novaYmlRequirement.setType(randomRequirementType());
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Generate random valid requirement name
     *
     * @return Random requirement name
     */
    private String randomRequirementName()
    {
        List<Constants.REQUIREMENT_NAME> names = Collections.unmodifiableList(Arrays.asList(Constants.REQUIREMENT_NAME.CONNECTORS, Constants.REQUIREMENT_NAME.CPU, Constants.REQUIREMENT_NAME.FILE_SYSTEM, Constants.REQUIREMENT_NAME.MEMORY,
                Constants.REQUIREMENT_NAME.PRECONF, Constants.REQUIREMENT_NAME.USES_C));
        Random r = new Random();
        return names.get(r.nextInt(names.size())).toString();
    }

    /**
     * Generate random valid requirement type
     *
     * @return Random requirement type
     */
    private String randomRequirementType()
    {
        List<Constants.REQUIREMENT_TYPE> types = Collections.unmodifiableList(Arrays.asList(Constants.REQUIREMENT_TYPE.RESOURCE, Constants.REQUIREMENT_TYPE.INSTALLATION));
        Random r = new Random();
        return types.get(r.nextInt(types.size())).toString();
    }

    private NovaYml copyNovaYml(NovaYml novaYml)
    {
        NovaYml copy = new NovaYml();
        copy.setUuaa(novaYml.getUuaa());
        copy.setProjectName(novaYml.getProjectName());
        copy.setName(novaYml.getName());
        copy.setDescription(novaYml.getDescription());
        copy.setVersion(novaYml.getVersion());
        copy.setServiceType(novaYml.getServiceType());
        copy.setLanguage(novaYml.getLanguage());
        copy.setLanguageVersion(novaYml.getLanguageVersion());
        copy.setNovaVersion(novaYml.getNovaVersion());
        return copy;
    }

}