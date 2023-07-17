package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PackInfo;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.datastorage.entities.*;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.LandingZoneStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.datamodel.model.todotask.entities.FilesystemTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemAlertRepositoryManager;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilesystemsBuilderImplTest
{
    @Mock
    private FilesystemRepository filesystemRepository;

    @Mock
    private FilesystemTaskRepository filesystemTaskRepository;

    @Mock
    private IProductBudgetsService productBudgetsService;

    @Mock
    private ManageValidationUtils manageValidationUtils;

    @Mock
    private NovaContext novaContext;

    @Mock
    private IAlertServiceApiClient alertServiceClient;

    @Mock
    private IFilesystemAlertRepositoryManager filesystemAlertRepositoryManager;

    @InjectMocks
    private FilesystemsBuilderImpl filesystemsBuilder;

    @Test
    void givenFilesystems_whenBuildFilesystemDTOWithoutExceptions_thenFilesystemDtosAreBuilt()
    {
        // Given
        final FilesystemNova filesystem = new FilesystemNova();

        final List<Filesystem> filesystems = Collections.singletonList(filesystem);

        final FilesystemsBuilderImpl mockedFilesystemsBuilder = Mockito.mock(FilesystemsBuilderImpl.class);

        final FilesystemDto filesystemDto = new FilesystemDto();

        when(mockedFilesystemsBuilder.buildFilesystemDTO(any())).thenReturn(filesystemDto);
        when(mockedFilesystemsBuilder.buildFilesystemDTOArray(any())).thenCallRealMethod();

        // When
        final FilesystemDto[] result = mockedFilesystemsBuilder.buildFilesystemDTOArray(filesystems);

        // Then
        assertEquals(filesystems.size(), result.length);
    }

    @Test
    void givenNovaFilesystem_whenBuildFilesystemDTOWithoutExceptions_thenFilesystemDtoIsBuilt()
    {
        // Given
        final Environment[] validEnvironments = new Environment[]{Environment.INT, Environment.PRE, Environment.PRO};

        final Integer productId = RandomUtils.nextInt(0, 10);
        final String productUuaa = RandomStringUtils.randomAlphanumeric(4);
        final String productName = RandomStringUtils.randomAlphanumeric(25);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);
        final String filesystemDescription = RandomStringUtils.randomAlphanumeric(50);
        final FilesystemStatus filesystemStatus = FilesystemStatus.values()[RandomUtils.nextInt(0, FilesystemStatus.values().length)];
        final Environment filesystemEnvironment = validEnvironments[RandomUtils.nextInt(0, validEnvironments.length)];
        final Calendar filesystemCreationDate = Calendar.getInstance();
        final Calendar filesystemDeletionDate = Calendar.getInstance();
        final Calendar filesystemLastModifiedDate = Calendar.getInstance();

        final boolean filesystemUsedInDeploymentPlan = RandomUtils.nextBoolean();
        final boolean filesystemUsed = RandomUtils.nextBoolean();

        String environmentFolder = null;

        switch (filesystemEnvironment)
        {
            case INT:
                environmentFolder = "ei";
                break;
            case PRE:
                environmentFolder = "pp";
                break;
            case PRO:
                environmentFolder = "pr";
                break;
        }

        final String volumeId = RandomStringUtils.randomAlphanumeric(15);
        final boolean nfs = RandomUtils.nextBoolean();
        final String landingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final String landingZonePathIncoming = "/usr/local/" + environmentFolder + "/nova/landingzone" + landingZonePath + "/incoming/";
        final String landingZonePathOutgoing = "/usr/local/" + environmentFolder + "/nova/outgoing" + landingZonePath + "/outgoing/";
        final String landingZonePathTracesIncoming = "/usr/local/" + environmentFolder + "/nova/filewatcher" + landingZonePath + "/incoming/";
        final String landingZonePathTracesOutgoing = "/usr/local/" + environmentFolder + "/nova/filewatcher" + landingZonePath + "/outgoing/";

        final String brickGlusterNodeHostName = RandomStringUtils.randomAlphanumeric(15);

        final Product product = new Product();
        product.setId(productId);
        product.setUuaa(productUuaa);
        product.setName(productName);

        final GlusterNode brickGlusterNode = new GlusterNode();
        brickGlusterNode.setHostName(brickGlusterNodeHostName);

        final Brick brick = new Brick();
        brick.setGlusterNode(brickGlusterNode);

        final Set<Brick> bricks = new HashSet<>();
        bricks.add(brick);

        final String landingZoneHostPort = RandomStringUtils.randomAlphanumeric(15);
        final LandingZone landingZone = new LandingZone();
        landingZone.setHostPort(landingZoneHostPort);

        final LandingZoneStatus filesystemLandingZoneStatus = LandingZoneStatus.CREATED;
        final FilesystemLandingZone filesystemLandingZone = new FilesystemLandingZone();
        filesystemLandingZone.setLandingZoneStatus(filesystemLandingZoneStatus);
        filesystemLandingZone.setLandingZone(landingZone);

        final Set<FilesystemLandingZone> filesystemLandingZones = new HashSet<>();
        filesystemLandingZones.add(filesystemLandingZone);

        final int filesystemPackId = RandomUtils.nextInt(0, 100);
        final String filesystemPackCode = RandomStringUtils.randomAlphanumeric(4);
        final int filesystemPackSizeMB = RandomUtils.nextInt(10, 1000);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(filesystemPackId);
        filesystemPack.setCode(filesystemPackCode);
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final int releaseId = RandomUtils.nextInt(0, 100);
        final String releaseName = RandomStringUtils.randomAlphanumeric(15);
        final Release release = new Release();
        release.setId(releaseId);
        release.setName(releaseName);
        release.setProduct(product);

        final int releaseVersionId = RandomUtils.nextInt(0, 100);
        final String releaseVersionName = RandomStringUtils.randomAlphanumeric(15);
        final ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(releaseVersionId);
        releaseVersion.setVersionName(releaseVersionName);
        releaseVersion.setRelease(release);

        final int deploymentPlanId = RandomUtils.nextInt(0, 100);
        final DeploymentStatus deploymentPlanStatus = DeploymentStatus.DEPLOYED;
        final DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(deploymentPlanId);
        deploymentPlan.setStatus(deploymentPlanStatus);
        deploymentPlan.setReleaseVersion(releaseVersion);

        final DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        final String releaseVersionServiceServiceName = RandomStringUtils.randomAlphanumeric(15);
        final String releaseVersionServiceVersion = RandomStringUtils.randomAlphanumeric(15);
        final String releaseVersionServiceArtifactId = RandomStringUtils.randomAlphanumeric(15);
        final String releaseVersionServiceGroupId = RandomStringUtils.randomAlphanumeric(15);
        final String      releaseVersionServiceFinalName   = RandomStringUtils.randomAlphanumeric(15);
        final ServiceType releaseVersionServiceServiceType = ServiceType.values()[RandomUtils.nextInt(0, ServiceType.values().length)];
        final String      releaseVersionServiceDescription = RandomStringUtils.randomAlphanumeric(15);
        final ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceName(releaseVersionServiceServiceName);
        releaseVersionService.setVersion(releaseVersionServiceVersion);
        releaseVersionService.setArtifactId(releaseVersionServiceArtifactId);
        releaseVersionService.setGroupId(releaseVersionServiceGroupId);
        releaseVersionService.setFinalName(releaseVersionServiceFinalName);
        releaseVersionService.setServiceType(releaseVersionServiceServiceType.getServiceType());
        releaseVersionService.setDescription(releaseVersionServiceDescription);

        final int deploymentServiceId = RandomUtils.nextInt(0, 100);
        final int deploymentServiceNumberOfInstances = RandomUtils.nextInt(1, 9);
        final DeploymentService deploymentService = new DeploymentService();
        deploymentService.setId(deploymentServiceId);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
        deploymentService.setService(releaseVersionService);
        deploymentService.setNumberOfInstances(deploymentServiceNumberOfInstances);

        final String deploymentServiceFilesystemVolumenBind = RandomStringUtils.randomAlphanumeric(15);
        final DeploymentServiceFilesystem deploymentServiceFilesystem = new DeploymentServiceFilesystem();
        deploymentServiceFilesystem.setDeploymentService(deploymentService);
        deploymentServiceFilesystem.setVolumeBind(deploymentServiceFilesystemVolumenBind);

        final List<DeploymentServiceFilesystem> deploymentServiceFilesystems = Collections.singletonList(deploymentServiceFilesystem);

        final FilesystemNova filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setDescription(filesystemDescription);
        filesystem.setName(filesystemName);
        filesystem.setProduct(product);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setVolumeId(volumeId);
        filesystem.setNfs(nfs);
        filesystem.setBricks(bricks);
        filesystem.setLandingZonePath(landingZonePath);
        filesystem.setFilesystemLandingZones(filesystemLandingZones);
        filesystem.setCreationDate(filesystemCreationDate);
        filesystem.setDeletionDate(filesystemDeletionDate);
        filesystem.setLastModified(filesystemLastModifiedDate);
        filesystem.setFilesystemStatus(filesystemStatus);
        filesystem.setFilesystemPack(filesystemPack);
        filesystem.setDeploymentServiceFilesystems(deploymentServiceFilesystems);

        final double packPrice = RandomUtils.nextDouble(1.0, 100.0);
        final PackInfo packInfo = new PackInfo();
        packInfo.setPackId(filesystemPackId);
        packInfo.setPackPrice(packPrice);

        final int filesystemTaskId = RandomUtils.nextInt(0, 100);
        final ToDoTaskType filesystemTaskType = ToDoTaskType.values()[RandomUtils.nextInt(0, ToDoTaskType.values().length)];
        final RoleType filesystemTaskGroup = RoleType.values()[RandomUtils.nextInt(0, RoleType.values().length)];
        final FilesystemTask filesystemTask = new FilesystemTask();
        filesystemTask.setStatus(ToDoTaskStatus.PENDING);
        filesystemTask.setAssignedGroup(filesystemTaskGroup);
        filesystemTask.setTaskType(filesystemTaskType);
        filesystemTask.setId(filesystemTaskId);

        final List<FilesystemTask> filesystemTasks = Collections.singletonList(filesystemTask);

        final Integer filesystemAlertId = RandomUtils.nextInt(0, 100);
        final FilesystemAlert filesystemAlert = new FilesystemAlert();
        filesystemAlert.setId(filesystemAlertId);

        final Optional<FilesystemAlert> optionalFilesystemAlert = Optional.of(filesystemAlert);

        final ASRequestAlertsDTO asRequestAlertsDTO = new ASRequestAlertsDTO();

        when(productBudgetsService.getFilesystemPackInfo(eq(filesystemPackId))).thenReturn(packInfo);
        when(filesystemRepository.filesystemIsUsedOnDeployedPlan(eq(filesystemId))).thenReturn(filesystemUsedInDeploymentPlan);
        when(filesystemRepository.filesystemIsUsed(eq(filesystemId))).thenReturn(filesystemUsed);
        when(manageValidationUtils.checkIfPlanCanBeManagedByUser(any(), eq(deploymentPlan))).thenReturn(Boolean.FALSE);
        when(filesystemTaskRepository.findByFilesystemId(eq(filesystemId))).thenReturn(filesystemTasks);
        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(eq(filesystemId))).thenReturn(optionalFilesystemAlert);
        when(alertServiceClient.getAlertsByRelatedIdAndStatus(eq(new String[]{filesystemAlertId.toString()}), eq(productId), eq(productUuaa), eq(Constants.OPEN_ALERT_STATUS))).thenReturn(asRequestAlertsDTO);

        // When
        final FilesystemDto result = filesystemsBuilder.buildFilesystemDTO(filesystem);

        // Then
        assertEquals(productId, result.getProductId());
        assertEquals(volumeId, result.getVolumeId());
        assertEquals(nfs, result.getNfs());
        assertEquals(bricks.size(), result.getBriks().length);
        assertEquals(brickGlusterNodeHostName, result.getBriks()[0]);
        assertEquals(filesystemEnvironment.getEnvironment(), result.getEnvironment());
        assertEquals(FilesystemType.FILESYSTEM.getFileSystemType(), result.getFilesystemType());
        assertEquals(landingZonePath, result.getFileTransferProperties().getLandingZonePath());
        assertEquals(landingZonePathIncoming, result.getFileTransferProperties().getLandingZonePathIncoming());
        assertEquals(landingZonePathOutgoing, result.getFileTransferProperties().getLandingZonePathOutgoing());
        assertEquals(landingZonePathTracesIncoming, result.getFileTransferProperties().getLandingZonePathTracesIncoming());
        assertEquals(landingZonePathTracesOutgoing, result.getFileTransferProperties().getLandingZonePathTracesOutgoing());
        assertEquals(filesystemLandingZones.size(), result.getFileTransferProperties().getLandingZones().length);
        assertEquals(filesystemCreationDate.getTimeInMillis(), result.getCreationDate());
        assertEquals(filesystemDeletionDate.getTimeInMillis(), result.getDeletionDate());
        assertEquals(filesystemLastModifiedDate.getTimeInMillis(), result.getLastModified());
        assertEquals(filesystemDescription, result.getFilesystemDescription());
        assertEquals(filesystemName, result.getFilesystemName());
        assertEquals(filesystemStatus.name(), result.getStatus());
        assertEquals(filesystemPackCode, result.getFilesystemPackCode());
        assertEquals(filesystemPackSizeMB, result.getFilesystemPackSize());
        assertEquals(packPrice, result.getFilesystemPackPrice());

        assertEquals(deploymentServiceFilesystems.size(), result.getServices().length);
        assertEquals(releaseVersionServiceServiceName, result.getServices()[0].getServiceName());
        assertEquals(releaseVersionServiceVersion, result.getServices()[0].getVersion());
        assertEquals(releaseVersionServiceArtifactId, result.getServices()[0].getArtifactId());
        assertEquals(releaseVersionServiceGroupId, result.getServices()[0].getGroupId());
        assertEquals(deploymentServiceNumberOfInstances, result.getServices()[0].getNumberOfInstances());
        assertEquals(deploymentServiceFilesystemVolumenBind, result.getServices()[0].getVolumeBind());
        assertEquals(deploymentServiceId, result.getServices()[0].getId());
        assertEquals(releaseVersionServiceFinalName, result.getServices()[0].getServiceFinalname());
        assertEquals(releaseVersionServiceServiceType.name(), result.getServices()[0].getServiceType());
        assertEquals(releaseVersionServiceDescription, result.getServices()[0].getDescription());
        assertEquals(deploymentPlanId, result.getServices()[0].getPlanId());
        assertEquals(deploymentPlanStatus.name(), result.getServices()[0].getPlanStatus());
        assertEquals(releaseId, result.getServices()[0].getReleaseId());
        assertEquals(releaseName, result.getServices()[0].getReleaseName());
        assertEquals(releaseVersionId, result.getServices()[0].getReleaseVersionId());
        assertEquals(releaseVersionName, result.getServices()[0].getReleaseVersionName());
        assertEquals(productId, result.getServices()[0].getProductId());
        assertEquals(productName, result.getServices()[0].getProductName());

        assertEquals(!filesystemUsedInDeploymentPlan, result.getCanBeArchived());
        assertEquals(!filesystemUsed, result.getCanBeDeleted());
        assertEquals(Boolean.FALSE, result.getCanBeManagedByUser());

        assertEquals(filesystemTasks.size(), result.getHasPendingTask().length);
        assertEquals(filesystemTaskId, result.getHasPendingTask()[0].getTodoTaskId());
        assertEquals(filesystemTaskGroup.name(), result.getHasPendingTask()[0].getAssignedRole());
        assertEquals(filesystemTaskType.name(), result.getHasPendingTask()[0].getTodoTaskType());
        assertEquals(filesystemTaskType.isTaskOfError(), result.getHasPendingTask()[0].getIsTaskOfError());

        verify(productBudgetsService, times(1)).getFilesystemPackInfo(eq(filesystemPackId));
        verify(filesystemRepository, times(1)).filesystemIsUsedOnDeployedPlan(eq(filesystemId));
        verify(filesystemRepository, times(1)).filesystemIsUsed(eq(filesystemId));
        verify(manageValidationUtils, times(1)).checkIfPlanCanBeManagedByUser(any(), eq(deploymentPlan));
        verify(filesystemTaskRepository, times(1)).findByFilesystemId(eq(filesystemId));
        verify(filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemId));
        verify(alertServiceClient, times(1)).getAlertsByRelatedIdAndStatus(eq(new String[]{filesystemAlertId.toString()}), eq(productId), eq(productUuaa), eq(Constants.OPEN_ALERT_STATUS));
    }
}
