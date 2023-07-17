package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FFilesystemRelatedTransferInfoDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemTypeDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FTATransferLocationInfo;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FTATransferRelatedInfoAdminDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.datamodel.model.todotask.entities.FilesystemTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.common.model.param.MailNotificationParams;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ToDoTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFileTransferAdminClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFilesystemManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IEtherService;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsBuilder;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsValidator;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl.FilesystemsServiceImpl.PERMISSIONS_EXCEPTION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.ARCHIVE_FILESYSTEM_PERMISSION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.CREATE_FILESYSTEM_PERMISSION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.FILESYSTEM_CREATE_DIR_PERMISSION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.FILESYSTEM_DELETE_PERMISSION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.FILESYSTEM_MODIFY_QUOTA;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.REMOVE_FILESYSTEM_PERMISSION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.RESTORE_FILESYSTEM_PERMISSION;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesystemsServiceImplTest
{
    @Mock
    private IFilesystemsValidator filesystemsValidator;

    @Mock
    private IFilesystemsBuilder filesystemsBuilder;

    @Mock
    private IFilesystemManagerClient filesystemManagerClient;

    @Mock
    private FilesystemRepository filesystemRepository;

    @Mock
    private IProductUsersClient usersClient;

    @Mock
    private DeploymentChangeRepository deploymentChangeRepository;

    @Mock
    private FilesystemTaskRepository filesystemTaskRepository;

    @Mock
    private ToDoTaskRepository toDoTaskRepository;

    @Mock
    private MailServiceClient mailServiceClient;

    @Mock
    private IEtherService etherService;

    @Mock
    private INovaActivityEmitter novaActivityEmitter;

    @Mock
    private IFileTransferAdminClient fileTransferAdminClient;

    @InjectMocks
    private FilesystemsServiceImpl filesystemsService;

    @Test
    void givenFilesystemId_whenGetFilesystem_thenFilesystemIsReturned()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        mockFilesystemDto.setId(filesystemId);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        doNothing().when(filesystemsValidator).checkIfFilesystemIsAvailable(eq(mockFilesystem));
        when(filesystemsBuilder.buildFilesystemDTO(eq(mockFilesystem))).thenReturn(mockFilesystemDto);

        // When
        final FilesystemDto result = filesystemsService.getFilesystem(filesystemId);

        // Then
        assertEquals(mockFilesystemDto, result);
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsAvailable(eq(mockFilesystem));
        verify(filesystemsBuilder, times(1)).buildFilesystemDTO(eq(mockFilesystem));
    }

    @Test
    void givenUserAndFilesystemIdWithoutPendingTasks_whenDeleteFilesystem_thenFilesystemIsDeleted() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final int productId = RandomUtils.nextInt(0, 100);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        mockFilesystemDto.setId(filesystemId);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        doNothing().when(usersClient).checkHasPermission(eq(ivUser), eq(REMOVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        doNothing().when(filesystemsValidator).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        doNothing().when(filesystemsValidator).validateFilesystemNotInUse(eq(mockFilesystem));
        doNothing().when(filesystemManagerClient).callDeleteFilesystemManager(eq(mockFilesystem));
        when(filesystemTaskRepository.findByFilesystemId(eq(filesystemId))).thenReturn(Collections.emptyList());

        // When
        filesystemsService.deleteFilesystem(ivUser, filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(REMOVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateFilesystemNotInUse(eq(mockFilesystem));
        verify(filesystemManagerClient, times(1)).callDeleteFilesystemManager(eq(mockFilesystem));

        verify(toDoTaskRepository, times(0)).save(any());

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.ELIMINATED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenUserAndFilesystemIdWitPendingTasks_whenDeleteFilesystem_thenFilesystemIsDeleted() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final int productId = RandomUtils.nextInt(0, 100);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        mockFilesystemDto.setId(filesystemId);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        final FilesystemTask pendingTask = new FilesystemTask();
        pendingTask.setStatus(ToDoTaskStatus.PENDING);

        final List<FilesystemTask> pendingTasks = Collections.singletonList(pendingTask);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        doNothing().when(usersClient).checkHasPermission(eq(ivUser), eq(REMOVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        doNothing().when(filesystemsValidator).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        doNothing().when(filesystemsValidator).validateFilesystemNotInUse(eq(mockFilesystem));
        doNothing().when(filesystemManagerClient).callDeleteFilesystemManager(eq(mockFilesystem));
        when(filesystemTaskRepository.findByFilesystemId(eq(filesystemId))).thenReturn(pendingTasks);

        // When
        filesystemsService.deleteFilesystem(ivUser, filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(REMOVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateFilesystemNotInUse(eq(mockFilesystem));
        verify(filesystemManagerClient, times(1)).callDeleteFilesystemManager(eq(mockFilesystem));

        final ArgumentCaptor<FilesystemTask> rejectedTaskCaptor = ArgumentCaptor.forClass(FilesystemTask.class);
        verify(toDoTaskRepository, times(1)).save(rejectedTaskCaptor.capture());
        assertEquals(ToDoTaskStatus.REJECTED, rejectedTaskCaptor.getValue().getStatus());
        assertEquals(Constants.CLOSE_PENDING_FILESYSTEM_TASK_MESSAGE + FilesystemStatus.DELETED.name(), rejectedTaskCaptor.getValue().getClosingMotive());

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.ELIMINATED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenFilesystemAndUserAndProductIdAndProductIsNotReadyToDeployInEther_whenCreateFilesystem_thenFilesystemIsCreated()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int productId = RandomUtils.nextInt(0, 100);
        final String filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment();


        final Product mockProduct = new Product();
        mockProduct.setId(productId);

        final CreateNewFilesystemDto filesystemToAdd = new CreateNewFilesystemDto();
        filesystemToAdd.setEnvironment(filesystemEnvironment);
        filesystemToAdd.setFilesystemType(FilesystemType.FILESYSTEM_ETHER.getFileSystemType());

        when(filesystemsValidator.validateAndGetProduct(eq(productId))).thenReturn(mockProduct);
        when(etherService.isReadyToDeploy(eq(productId), eq(filesystemEnvironment))).thenReturn(Boolean.FALSE);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsService.createFilesystem(filesystemToAdd, ivUser, productId)
        );

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(CREATE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment), eq(productId), eq(PERMISSIONS_EXCEPTION));
        assertEquals(FilesystemsError.getCreatingOrDeletingFileSystemOnNonConfiguredPlatformError().getErrorCode(), exception.getErrorCode().getErrorCode());
    }

    @Test
    void givenFilesystemAndUserAndProductIdAndProductIsReadyToDeployInEther_whenCreateFilesystem_thenFilesystemIsCreated()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int productId = RandomUtils.nextInt(0, 100);
        final String filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment();


        final Product mockProduct = new Product();
        mockProduct.setId(productId);

        final CreateNewFilesystemDto filesystemToAdd = new CreateNewFilesystemDto();
        filesystemToAdd.setEnvironment(filesystemEnvironment);
        filesystemToAdd.setFilesystemType(FilesystemType.FILESYSTEM_ETHER.getFileSystemType());

        when(filesystemsValidator.validateAndGetProduct(eq(productId))).thenReturn(mockProduct);
        when(etherService.isReadyToDeploy(eq(productId), eq(filesystemEnvironment))).thenReturn(Boolean.TRUE);
        doNothing().when(filesystemsValidator).validateFilesystemCreation(eq(mockProduct), eq(filesystemToAdd));
        doNothing().when(filesystemManagerClient).callCreateFilesystemManager(eq(filesystemToAdd), eq(mockProduct));

        // When
        filesystemsService.createFilesystem(filesystemToAdd, ivUser, productId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(CREATE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).validateFilesystemCreation(eq(mockProduct), eq(filesystemToAdd));
        verify(filesystemManagerClient, times(1)).callCreateFilesystemManager(eq(filesystemToAdd), eq(mockProduct));
    }

    @Test
    void givenUserAndFilesystemIdWithoutPendingTasks_whenArchiveFilesystem_thenFilesystemIsArchived() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final int productId = RandomUtils.nextInt(0, 100);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        mockFilesystemDto.setId(filesystemId);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        doNothing().when(usersClient).checkHasPermission(eq(ivUser), eq(ARCHIVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        doNothing().when(filesystemsValidator).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        doNothing().when(filesystemsValidator).validateFilesystemNotInUse(eq(mockFilesystem));
        doNothing().when(filesystemManagerClient).callArchiveVolume(eq(filesystemId));
        when(filesystemTaskRepository.findByFilesystemId(eq(filesystemId))).thenReturn(Collections.emptyList());

        // When
        filesystemsService.archiveFilesystem(ivUser, filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(ARCHIVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateFilesystemNotInUse(eq(mockFilesystem));
        verify(filesystemManagerClient, times(1)).callArchiveVolume(eq(filesystemId));

        verify(toDoTaskRepository, times(0)).save(any());

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.ARCHIVED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenUserAndFilesystemIdWithPendingTasks_whenArchiveFilesystem_thenFilesystemIsArchived() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final int productId = RandomUtils.nextInt(0, 100);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        mockFilesystemDto.setId(filesystemId);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        final FilesystemTask pendingTask = new FilesystemTask();
        pendingTask.setStatus(ToDoTaskStatus.PENDING);

        final List<FilesystemTask> pendingTasks = Collections.singletonList(pendingTask);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        doNothing().when(usersClient).checkHasPermission(eq(ivUser), eq(ARCHIVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        doNothing().when(filesystemsValidator).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        doNothing().when(filesystemsValidator).validateFilesystemNotInUse(eq(mockFilesystem));
        doNothing().when(filesystemManagerClient).callArchiveVolume(eq(filesystemId));
        when(filesystemTaskRepository.findByFilesystemId(eq(filesystemId))).thenReturn(pendingTasks);

        // When
        filesystemsService.archiveFilesystem(ivUser, filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(ARCHIVE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateFilesystemNotInUse(eq(mockFilesystem));
        verify(filesystemManagerClient, times(1)).callArchiveVolume(eq(filesystemId));

        final ArgumentCaptor<FilesystemTask> rejectedTaskCaptor = ArgumentCaptor.forClass(FilesystemTask.class);
        verify(toDoTaskRepository, times(1)).save(rejectedTaskCaptor.capture());
        assertEquals(ToDoTaskStatus.REJECTED, rejectedTaskCaptor.getValue().getStatus());
        assertEquals(Constants.CLOSE_PENDING_FILESYSTEM_TASK_MESSAGE + FilesystemStatus.ARCHIVED.name(), rejectedTaskCaptor.getValue().getClosingMotive());

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.ARCHIVED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenUserAndFileAndFilesystemNotInPro_whenDownloadToPre_thenNovaExceptionIsThrown()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filename = RandomStringUtils.randomAlphanumeric(10);
        final String filepath = RandomStringUtils.randomAlphanumeric(25);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.INT;

        final int productId = RandomUtils.nextInt(0, 100);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);

        final NovaError expectedError = FilesystemsError.getNotEnvironmentToDownload(filesystemId, filesystemEnvironment.getEnvironment());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsService.downloadToPre(ivUser, filesystemId, filename, filepath)
        );

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());

        verify(novaActivityEmitter, times(0)).emitNewActivity(any());
    }

    @Test
    void givenUserAndFileAndWithoutSameFilesystemInPre_whenDownloadToPre_thenNovaExceptionIsThrown()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filename = RandomStringUtils.randomAlphanumeric(10);
        final String filepath = RandomStringUtils.randomAlphanumeric(25);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.PRO;

        final int productId = RandomUtils.nextInt(0, 100);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemRepository.productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(Environment.PRE.getEnvironment()))).thenReturn(Boolean.FALSE);

        final NovaError expectedError = FilesystemsError.getDuplicatedFilesystemNotFoundError(productId, Environment.PRE.name(), filesystemName);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsService.downloadToPre(ivUser, filesystemId, filename, filepath)
        );

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemRepository, times(1)).productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(Environment.PRE.getEnvironment()));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());

        verify(novaActivityEmitter, times(0)).emitNewActivity(any());
    }

    @Test
    void givenUserAndFileAndFilesystem_whenDownloadToPre_thenFileIsDownloaded() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final String ivUserFullName = RandomStringUtils.randomAlphanumeric(25);
        final String ivUserEmail = RandomStringUtils.randomAlphanumeric(25);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filename = RandomStringUtils.randomAlphanumeric(10);
        final String filepath = RandomStringUtils.randomAlphanumeric(25);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.PRO;

        final int productId = RandomUtils.nextInt(0, 100);
        final String productName = RandomStringUtils.randomAlphanumeric(15);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);
        mockFilesystemProduct.setName(productName);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemRepository.productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(Environment.PRE.getEnvironment()))).thenReturn(Boolean.TRUE);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        doReturn(ivUserFullName).when(spyFilesystemsService).buildFullUserName(eq(ivUser));
        doReturn(ivUserEmail).when(spyFilesystemsService).getUserMailAddress(eq(ivUser));

        doNothing().when(mailServiceClient).sendDownloadToPreNotification(any(MailNotificationParams.class));

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());
        activityParams.put("filename", filename);
        activityParams.put("filesystemPath", filepath);

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        // When
        spyFilesystemsService.downloadToPre(ivUser, filesystemId, filename, filepath);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemRepository, times(1)).productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(Environment.PRE.getEnvironment()));
        verify(mailServiceClient, times(1)).sendDownloadToPreNotification(any(MailNotificationParams.class));

        final ArgumentCaptor<FSMFileLocationModel> captor = ArgumentCaptor.forClass(FSMFileLocationModel.class);
        verify(filesystemManagerClient, times(1)).checkFileToDownloadToPreviousEnv(eq(filesystemId), captor.capture());

        assertEquals(filepath, captor.getValue().getPath());
        assertEquals(filename, captor.getValue().getFilename());

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.FILE_DOWNLOAD_PRE_REQUESTED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenUserAndFilesystem_whenRestoreFilesystem_thenFilesystemIsRestored() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.PRO;

        final int productId = RandomUtils.nextInt(0, 100);
        final String productName = RandomStringUtils.randomAlphanumeric(15);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);
        mockFilesystemProduct.setName(productName);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        doNothing().when(usersClient).checkHasPermission(eq(ivUser), eq(RESTORE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        doNothing().when(filesystemsValidator).validateRestoreFilesystem(eq(mockFilesystem));
        doNothing().when(filesystemManagerClient).callRestoreVolume(eq(filesystemId));

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        // When
        filesystemsService.restoreFilesystem(ivUser, filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(RESTORE_FILESYSTEM_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).validateRestoreFilesystem(eq(mockFilesystem));
        verify(filesystemManagerClient, times(1)).callRestoreVolume(eq(filesystemId));

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.RESET, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenProductIdAndEnvironmentAndFilesystemType_whenGetProductFilesystems_thenProductFilesystemsAreReturned()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final FilesystemType filesystemType = FilesystemType.FILESYSTEM;

        final Filesystem mockFilesystem = new FilesystemNova();
        final List<Filesystem> mockFilesystems = Collections.singletonList(mockFilesystem);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        final FilesystemDto[] mockFilesystemDtos = new FilesystemDto[]{mockFilesystemDto};

        when(filesystemRepository.findByProductIdAndEnvironmentAndFilesystemTypeOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()), eq(FilesystemNova.class))).thenReturn(mockFilesystems);
        when(filesystemsBuilder.buildFilesystemDTOArray(eq(mockFilesystems))).thenReturn(mockFilesystemDtos);

        // When
        final FilesystemDto[] result = filesystemsService.getProductFilesystems(productId, environment.getEnvironment(), filesystemType.getFileSystemType());

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        verify(filesystemRepository, times(1)).findByProductIdAndEnvironmentAndFilesystemTypeOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()), eq(FilesystemNova.class));
        verify(filesystemsBuilder, times(1)).buildFilesystemDTOArray(eq(mockFilesystems));
        assertArrayEquals(mockFilesystemDtos, result);
    }

    @Test
    void givenProductIdAndEnvironment_whenGetProductFilesystems_thenProductFilesystemsAreReturned()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final Filesystem mockFilesystem = new FilesystemNova();
        final List<Filesystem> mockFilesystems = Collections.singletonList(mockFilesystem);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        final FilesystemDto[] mockFilesystemDtos = new FilesystemDto[]{mockFilesystemDto};

        when(filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()))).thenReturn(mockFilesystems);
        when(filesystemsBuilder.buildFilesystemDTOArray(eq(mockFilesystems))).thenReturn(mockFilesystemDtos);

        // When
        final FilesystemDto[] result = filesystemsService.getProductFilesystems(productId, environment.getEnvironment(), null);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        verify(filesystemRepository, times(1)).findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()));
        verify(filesystemsBuilder, times(1)).buildFilesystemDTOArray(eq(mockFilesystems));
        assertArrayEquals(mockFilesystemDtos, result);
    }

    @Test
    void givenProductIdAndFilesystemType_whenGetProductFilesystems_thenProductFilesystemsAreReturned()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);
        final FilesystemType filesystemType = FilesystemType.FILESYSTEM;

        final Filesystem mockFilesystem = new FilesystemNova();
        final List<Filesystem> mockFilesystems = Collections.singletonList(mockFilesystem);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        final FilesystemDto[] mockFilesystemDtos = new FilesystemDto[]{mockFilesystemDto};

        when(filesystemRepository.findByProductIdAndFilesystemTypeOrderByCreationDateDesc(eq(productId), eq(FilesystemNova.class))).thenReturn(mockFilesystems);
        when(filesystemsBuilder.buildFilesystemDTOArray(eq(mockFilesystems))).thenReturn(mockFilesystemDtos);

        // When
        final FilesystemDto[] result = filesystemsService.getProductFilesystems(productId, null, filesystemType.getFileSystemType());

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        verify(filesystemRepository, times(1)).findByProductIdAndFilesystemTypeOrderByCreationDateDesc(eq(productId), eq(FilesystemNova.class));
        verify(filesystemsBuilder, times(1)).buildFilesystemDTOArray(eq(mockFilesystems));
        assertArrayEquals(mockFilesystemDtos, result);
    }

    @Test
    void givenProductId_whenGetProductFilesystems_thenProductFilesystemsAreReturned()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);

        final Filesystem mockFilesystem = new FilesystemNova();
        final List<Filesystem> mockFilesystems = Collections.singletonList(mockFilesystem);

        final FilesystemDto mockFilesystemDto = new FilesystemDto();
        final FilesystemDto[] mockFilesystemDtos = new FilesystemDto[]{mockFilesystemDto};

        when(filesystemRepository.findByProductIdOrderByCreationDateDesc(eq(productId))).thenReturn(mockFilesystems);
        when(filesystemsBuilder.buildFilesystemDTOArray(eq(mockFilesystems))).thenReturn(mockFilesystemDtos);

        // When
        final FilesystemDto[] result = filesystemsService.getProductFilesystems(productId, null, null);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        verify(filesystemRepository, times(1)).findByProductIdOrderByCreationDateDesc(eq(productId));
        verify(filesystemsBuilder, times(1)).buildFilesystemDTOArray(eq(mockFilesystems));
        assertArrayEquals(mockFilesystemDtos, result);
    }

    @Test
    void givenUserAndFilesystemIdAndFile_whenDeleteFile_thenFileIsDeleted()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filename = RandomStringUtils.randomAlphanumeric(15);
        final FSFileLocationModel file = new FSFileLocationModel();
        file.setFilename(filename);

        final int productId = RandomUtils.nextInt(0, 100);
        final String productName = RandomStringUtils.randomAlphanumeric(15);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);
        mockFilesystemProduct.setName(productName);

        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        doNothing().when(spyFilesystemsService).addHistoryEntry(eq(ivUser), eq(mockFilesystem), eq(ChangeType.DELETE_FILE_FILESYSTEM), any());

        // When
        spyFilesystemsService.deleteFile(file, ivUser, filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemsValidator, times(1)).validateReservedDirectories(eq(mockFilesystem), eq(file));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_DELETE_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        verify(filesystemManagerClient, times(1)).callDeleteFile(eq(filesystemId), eq(file));
        verify(spyFilesystemsService, times(1)).addHistoryEntry(eq(ivUser), eq(mockFilesystem), eq(ChangeType.DELETE_FILE_FILESYSTEM), ArgumentMatchers.contains(filename));
    }

    @Test
    void givenFilesystemId_whenIsFilesystemFrozen_thenFilesystemFrozenIsReturned()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);

        final boolean mockFrozen = RandomUtils.nextBoolean();

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemsValidator.isFilesystemFrozen(eq(mockFilesystem))).thenReturn(mockFrozen);

        // When
        boolean result = filesystemsService.isFilesystemFrozen(filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemsValidator, times(1)).isFilesystemFrozen(eq(mockFilesystem));
        assertEquals(mockFrozen, result);
    }

    @Test
    void givenUserAndFilesystemIdAndDirectory_whenCreateDirectory_thenDirectoryIsCreated() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String directory = RandomStringUtils.randomAlphanumeric(10);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.PRO;

        final int productId = RandomUtils.nextInt(0, 100);
        final String productName = RandomStringUtils.randomAlphanumeric(15);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);
        mockFilesystemProduct.setName(productName);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemsValidator.validateDirectory(eq(directory))).thenReturn(directory);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());
        activityParams.put("folder", directory);

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        doNothing().when(spyFilesystemsService).addHistoryEntry(eq(ivUser), eq(mockFilesystem), eq(ChangeType.CREATE_DIRECTORY_FILESYSTEM), any());

        // When
        spyFilesystemsService.createDirectory(ivUser, filesystemId, directory);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_CREATE_DIR_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateDirectory(eq(directory));
        verify(filesystemManagerClient, times(1)).callCreateDirectory(eq(directory), eq(filesystemId));
        verify(spyFilesystemsService, times(1)).addHistoryEntry(eq(ivUser), eq(mockFilesystem), eq(ChangeType.CREATE_DIRECTORY_FILESYSTEM), ArgumentMatchers.contains(directory));

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.FOLDER_CREATED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenFilesystemId_whenGetFilesystemUsage_thenUsageIsReturned()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);

        final FSFilesystemUsage mockUsage = new FSFilesystemUsage();

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemManagerClient.callGetFileUse(eq(filesystemId))).thenReturn(mockUsage);

        // When
        FSFilesystemUsage result = filesystemsService.getFilesystemUsage(filesystemId);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemManagerClient, times(1)).callGetFileUse(eq(filesystemId));
        assertEquals(mockUsage, result);
    }

    @Test
    void givenFilesystemIdAndPathAndFile_whenGetFiles_thenFilesAreReturned()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String path = RandomStringUtils.randomAlphanumeric(15);
        final String file = RandomStringUtils.randomAlphanumeric(15);

        int numberPage = 1;
        int sizePage = 100;
        String field = "name";
        String order = "asc";

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);

        final FSFileModel mockFile = new FSFileModel();
        FSFileModelPaged fsFileModelPaged = new FSFileModelPaged();
        fsFileModelPaged.setFileRegistry(new FSFileModel[]{mockFile});

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemManagerClient.callGetFiles(eq(filesystemId), eq(path), eq(file),
                eq(numberPage), eq(sizePage), eq(field), eq(order))).thenReturn(fsFileModelPaged);

        // When
        FSFileModelPaged result = filesystemsService.getFiles(filesystemId, path, file, numberPage, sizePage, field, order);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(filesystemManagerClient, times(1)).callGetFiles(eq(filesystemId), eq(path), eq(file),
                eq(numberPage), eq(sizePage), eq(field), eq(order));
        assertEquals(fsFileModelPaged, result);
    }

    @Test
    void givenFilesystemIdAndPathAndFile_whenGetFiles_sizePageBig()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String path = RandomStringUtils.randomAlphanumeric(15);
        final String file = RandomStringUtils.randomAlphanumeric(15);

        int numberPage = 1;
        int sizePage = 101;
        String field = "name";
        String order = "asc";

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);

        final FSFileModel mockFile = new FSFileModel();
        FSFileModelPaged fsFileModelPaged = new FSFileModelPaged();
        fsFileModelPaged.setFileRegistry(new FSFileModel[]{mockFile});

        assertThrows(NovaException.class, () -> filesystemsService.getFiles(filesystemId, path, file, numberPage, sizePage, field, order));

        verify(filesystemsValidator, times(0)).validateAndGetFilesystem(any());
    }

    @Test
    void givenFilesystemIdAndPathAndFile_whenGetFiles_numberPage0()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String path = RandomStringUtils.randomAlphanumeric(15);
        final String file = RandomStringUtils.randomAlphanumeric(15);

        int numberPage = 0;
        int sizePage = 50;
        String field = "name";
        String order = "asc";

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);

        final FSFileModel mockFile = new FSFileModel();
        FSFileModelPaged fsFileModelPaged = new FSFileModelPaged();
        fsFileModelPaged.setFileRegistry(new FSFileModel[]{mockFile});

        assertThrows(NovaException.class, () -> filesystemsService.getFiles(filesystemId, path, file, numberPage, sizePage, field, order));

        verify(filesystemsValidator, times(0)).validateAndGetFilesystem(any());
    }

    @ParameterizedTest
    @ArgumentsSource(GetAllValidEnvironmentsProvider.class)
    void givenProductIdAndEnvironment_whenGetAvailableFilesystemTypes_thenFilesystemTypesAreReturned(final Environment environment)
    {
        // Given
        final int                         productId        = RandomUtils.nextInt(0, 100);
        final Platform enabledDeployInt = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Platform enabledDeployPre = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Platform enabledDeployPro = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

        final Map<Environment, Platform> enabledDeploys = Map.of(
                Environment.INT, enabledDeployInt,
                Environment.PRE, enabledDeployPre,
                Environment.PRO, enabledDeployPro
        );

        final Product mockProduct = new Product();
        mockProduct.setId(productId);
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigInt = new PlatformConfig();
        platformConfigInt.setEnvironment(Environment.INT.getEnvironment());
        platformConfigInt.setProductId(productId);
        platformConfigInt.setPlatform(enabledDeployInt);
        platformConfigInt.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigInt.setIsDefault(true);
        platformConfigList.add(platformConfigInt);

        PlatformConfig platformConfigPre = new PlatformConfig();
        platformConfigPre.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigPre.setProductId(productId);
        platformConfigPre.setPlatform(enabledDeployPre);
        platformConfigPre.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigPre.setIsDefault(true);
        platformConfigList.add(platformConfigPre);

        PlatformConfig platformConfigPro = new PlatformConfig();
        platformConfigPro.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigPro.setProductId(productId);
        platformConfigPro.setPlatform(enabledDeployPro);
        platformConfigPro.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigPro.setIsDefault(true);
        platformConfigList.add(platformConfigPro);

        mockProduct.setPlatformConfigList(platformConfigList);


        when(filesystemsValidator.validateAndGetProduct(eq(productId))).thenReturn(mockProduct);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        final List<FilesystemTypeDto> mockFilesystemTypes = Collections.emptyList();

        doReturn(mockFilesystemTypes).when(spyFilesystemsService).getAvailableFilesystemTypes(anyMap());

        // When
        FilesystemTypeDto[] result = spyFilesystemsService.getAvailableFilesystemTypes(productId, environment.getEnvironment());

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        assertArrayEquals(mockFilesystemTypes.toArray(FilesystemTypeDto[]::new), result);

        final ArgumentCaptor<Map<Environment, List<Platform>>> parameterCaptor = ArgumentCaptor.forClass(Map.class);
        verify(spyFilesystemsService, times(1)).getAvailableFilesystemTypes(parameterCaptor.capture());

        assertEquals(1, parameterCaptor.getValue().size());
        assertTrue(parameterCaptor.getValue().containsKey(environment));
        assertEquals(enabledDeploys.get(environment), parameterCaptor.getValue().get(environment).get(0));
    }

    @Test
    void givenProductId_whenGetAvailableFilesystemTypes_thenFilesystemTypesAreReturned()
    {
        // Given
        final int                         productId        = RandomUtils.nextInt(0, 100);
        final Platform enabledDeployInt = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Platform enabledDeployPre = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];
        final Platform enabledDeployPro = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

        final Map<Environment, Platform> enabledDeploys = Map.of(
                Environment.INT, enabledDeployInt,
                Environment.PRE, enabledDeployPre,
                Environment.PRO, enabledDeployPro
        );

        final Product mockProduct = new Product();
        mockProduct.setId(productId);
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigInt = new PlatformConfig();
        platformConfigInt.setEnvironment(Environment.INT.getEnvironment());
        platformConfigInt.setProductId(productId);
        platformConfigInt.setPlatform(enabledDeployInt);
        platformConfigInt.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigInt.setIsDefault(true);
        platformConfigList.add(platformConfigInt);

        PlatformConfig platformConfigPre = new PlatformConfig();
        platformConfigPre.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigPre.setProductId(productId);
        platformConfigPre.setPlatform(enabledDeployPre);
        platformConfigPre.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigPre.setIsDefault(true);
        platformConfigList.add(platformConfigPre);

        PlatformConfig platformConfigPro = new PlatformConfig();
        platformConfigPro.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigPro.setProductId(productId);
        platformConfigPro.setPlatform(enabledDeployPro);
        platformConfigPro.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigPro.setIsDefault(true);
        platformConfigList.add(platformConfigPro);

        mockProduct.setPlatformConfigList(platformConfigList);

        when(filesystemsValidator.validateAndGetProduct(eq(productId))).thenReturn(mockProduct);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        final List<FilesystemTypeDto> mockFilesystemTypes = Collections.emptyList();

        doReturn(mockFilesystemTypes).when(spyFilesystemsService).getAvailableFilesystemTypes(anyMap());

        // When
        FilesystemTypeDto[] result = spyFilesystemsService.getAvailableFilesystemTypes(productId, null);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetProduct(eq(productId));
        assertArrayEquals(mockFilesystemTypes.toArray(FilesystemTypeDto[]::new), result);

        final ArgumentCaptor<Map<Environment, List<Platform>>> parameterCaptor = ArgumentCaptor.forClass(Map.class);
        verify(spyFilesystemsService, times(1)).getAvailableFilesystemTypes(parameterCaptor.capture());

        assertEquals(3, parameterCaptor.getValue().size());
        assertTrue(parameterCaptor.getValue().containsKey(Environment.INT));
        assertTrue(parameterCaptor.getValue().containsKey(Environment.PRE));
        assertTrue(parameterCaptor.getValue().containsKey(Environment.PRO));
        assertEquals(enabledDeploys.get(Environment.INT), parameterCaptor.getValue().get(Environment.INT).get(0));
        assertEquals(enabledDeploys.get(Environment.PRE), parameterCaptor.getValue().get(Environment.PRE).get(0));
        assertEquals(enabledDeploys.get(Environment.PRO), parameterCaptor.getValue().get(Environment.PRO).get(0));
    }

    @Test
    void givenUserAndFilesystemIdAndPackCodeAndSameQuota_whenUpdateFilesystemQuota_thenFilesystemQuotaIsUpdated() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String packCode = RandomStringUtils.randomAlphanumeric(4);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.PRO;

        final int productId = RandomUtils.nextInt(0, 100);
        final String productName = RandomStringUtils.randomAlphanumeric(15);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);
        mockFilesystemProduct.setName(productName);

        final FilesystemPack mockFilesystemPack = new FilesystemPack();
        mockFilesystemPack.setCode(packCode);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);
        mockFilesystem.setFilesystemPack(mockFilesystemPack);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemsValidator.validatePackToUpdate(eq(mockFilesystem), eq(packCode))).thenReturn(mockFilesystemPack);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());
        activityParams.put("changeType", "updateFilesystemQuota");
        activityParams.put("lastQuota", 0);
        activityParams.put("newQuota", 0);

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        // When
        spyFilesystemsService.updateFilesystemQuota(filesystemId, packCode, ivUser);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_MODIFY_QUOTA), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).validatePackToUpdate(eq(mockFilesystem), eq(packCode));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsAvailable(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateFilesystemIsNovaType(eq(mockFilesystem.getType()));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));

        verify(filesystemsValidator, times(0)).validateFilesystemStorage(any(), any());
        verify(filesystemsValidator, times(0)).checkFilesystemBudget(any(), any());
        verify(filesystemManagerClient, times(0)).callUpdateQuota(any(), any());
        verify(spyFilesystemsService, times(0)).addHistoryEntry(any(), any(), any(), any());

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.EDITED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenUserAndFilesystemIdAndPackCodeAndDifferentQuota_whenUpdateFilesystemQuota_thenFilesystemQuotaIsUpdated() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String oldPackCode = RandomStringUtils.randomAlphanumeric(8);
        final String newPackCode = RandomStringUtils.randomAlphanumeric(8);

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Environment filesystemEnvironment = Environment.PRO;

        final int productId = RandomUtils.nextInt(0, 100);
        final String productName = RandomStringUtils.randomAlphanumeric(15);
        final Product mockFilesystemProduct = new Product();
        mockFilesystemProduct.setId(productId);
        mockFilesystemProduct.setName(productName);

        final FilesystemPack mockFilesystemFilesystemPack = new FilesystemPack();
        mockFilesystemFilesystemPack.setCode(oldPackCode);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setId(filesystemId);
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(mockFilesystemProduct);
        mockFilesystem.setFilesystemPack(mockFilesystemFilesystemPack);

        final FilesystemPack mockFilesystemPack = new FilesystemPack();
        mockFilesystemPack.setCode(newPackCode);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemsValidator.validatePackToUpdate(eq(mockFilesystem), eq(newPackCode))).thenReturn(mockFilesystemPack);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("filesystemName", filesystemName);
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("filesystemType", mockFilesystem.getType().getFileSystemType());
        activityParams.put("changeType", "updateFilesystemQuota");
        activityParams.put("lastQuota", 0);
        activityParams.put("newQuota", 0);

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        final FilesystemsServiceImpl spyFilesystemsService = Mockito.spy(filesystemsService);

        // When
        spyFilesystemsService.updateFilesystemQuota(filesystemId, newPackCode, ivUser);

        // Then
        verify(filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_MODIFY_QUOTA), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(filesystemsValidator, times(1)).validatePackToUpdate(eq(mockFilesystem), eq(newPackCode));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsAvailable(eq(mockFilesystem));
        verify(filesystemsValidator, times(1)).validateFilesystemIsNovaType(eq(mockFilesystem.getType()));
        verify(filesystemsValidator, times(1)).checkIfFilesystemIsFrozen(eq(mockFilesystem));

        verify(filesystemsValidator, times(1)).validateFilesystemStorage(eq(mockFilesystem), eq(mockFilesystemPack));
        verify(filesystemsValidator, times(1)).checkFilesystemBudget(eq(mockFilesystem), eq(mockFilesystemPack));
        verify(filesystemManagerClient, times(1)).callUpdateQuota(eq(filesystemId), eq(newPackCode));
        verify(spyFilesystemsService, times(1)).addHistoryEntry(eq(ivUser), eq(mockFilesystem), eq(ChangeType.MODIFY_QUOTA_FILESYSTEM), ArgumentMatchers.contains(filesystemName));

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(novaActivityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        Assertions.assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        Assertions.assertEquals(ActivityScope.FILESYSTEM, genericActivityCaptor.getValue().getScope());
        Assertions.assertEquals(ActivityAction.EDITED, genericActivityCaptor.getValue().getAction());
        Assertions.assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        Assertions.assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        Assertions.assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenKnownPathAndEnvironment_whenGetFilesystemId_thenFilesystemIdIsReturned()
    {
        // Given
        final String path = RandomStringUtils.randomAlphanumeric(15);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final Integer mockFilesystemId = RandomUtils.nextInt(0, 100);

        when(filesystemRepository.filesystemId(eq(path), eq(environment.getEnvironment()))).thenReturn(mockFilesystemId);

        // When
        final Integer result = filesystemsService.getFilesystemId(path, environment.getEnvironment());

        // Then
        verify(filesystemRepository, times(1)).filesystemId(eq(path), eq(environment.getEnvironment()));
        assertEquals(mockFilesystemId, result);
    }

    @Test
    void givenUnknownPathAndEnvironment_whenGetFilesystemId_thenNovaExceptionIsThrown()
    {
        // Given
        final String path = RandomStringUtils.randomAlphanumeric(15);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        when(filesystemRepository.filesystemId(eq(path), eq(environment.getEnvironment()))).thenReturn(null);

        final NovaError expectedError = FilesystemsError.getFilesystemIdError(path, environment.getEnvironment());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsService.getFilesystemId(path, environment.getEnvironment())
        );

        // Then
        verify(filesystemRepository, times(1)).filesystemId(eq(path), eq(environment.getEnvironment()));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductIdAndEnvironment_whenGetFilesystemUsageReport_thenReportIsReturned()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final FilesystemsUsageReportDTO mockUsageReport = new FilesystemsUsageReportDTO();

        when(filesystemManagerClient.callFilesystemsUsageReport(eq(productId), eq(environment.getEnvironment()))).thenReturn(mockUsageReport);

        // When
        final FilesystemsUsageReportDTO result = filesystemsService.getFilesystemsUsageReport(productId, environment.getEnvironment());

        // Then
        verify(filesystemManagerClient, times(1)).callFilesystemsUsageReport(eq(productId), eq(environment.getEnvironment()));

        assertEquals(mockUsageReport, result);
    }

    @Test
    void givenUserAndFilesystemAndChangeAndMessage_whenAddHistoryEntry_thenHistoryEntryIsAdded()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        final ChangeType change = ChangeType.values()[RandomUtils.nextInt(0, ChangeType.values().length)];
        final String message = RandomStringUtils.randomAlphanumeric(25);

        final int deploymentPlanId1 = RandomUtils.nextInt(0, 100);
        final DeploymentPlan mockDeploymentPlan1 = new DeploymentPlan();
        mockDeploymentPlan1.setId(deploymentPlanId1);
        final int deploymentPlanId2 = RandomUtils.nextInt(0, 100);
        final DeploymentPlan mockDeploymentPlan2 = new DeploymentPlan();
        mockDeploymentPlan2.setId(deploymentPlanId2);
        final List<DeploymentPlan> mockDeploymentPlanList = Arrays.asList(mockDeploymentPlan1, mockDeploymentPlan2);

        when(filesystemRepository.filesystemUsedOnDeployedPlan(eq(filesystemId))).thenReturn(mockDeploymentPlanList);

        // When
        filesystemsService.addHistoryEntry(ivUser, filesystem, change, message);

        // Then
        verify(filesystemRepository, times(1)).filesystemUsedOnDeployedPlan(filesystemId);

        ArgumentCaptor<DeploymentChange> deploymentChangeCaptor = ArgumentCaptor.forClass(DeploymentChange.class);
        verify(deploymentChangeRepository, times(2)).saveAndFlush(deploymentChangeCaptor.capture());
        assertEquals(ivUser, deploymentChangeCaptor.getValue().getUserCode());
        assertEquals(change, deploymentChangeCaptor.getValue().getType());
        assertEquals(message, deploymentChangeCaptor.getValue().getDescription());

        final List<DeploymentPlan> capturedPlans = deploymentChangeCaptor.getAllValues().stream()
                .map(DeploymentChange::getDeploymentPlan)
                .collect(Collectors.toList());

        assertEquals(mockDeploymentPlan1, capturedPlans.get(0));
        assertEquals(mockDeploymentPlan2, capturedPlans.get(1));
    }

    @Test
    void givenMapWithEnvironmentAndAvailablePlatformDeploymentType1_whenGetAvailableFilesystemTypes_thenAvailableFilesystemTypesAreReturned()
    {
        // Given
        final Environment environment = Environment.INT;
        final List<Platform> platformList = new ArrayList<>();
        platformList.add(Platform.NOVA);
        final Map<Environment, List<Platform>> map = Map.of(environment, platformList);

        // When
        final List<FilesystemTypeDto> result = filesystemsService.getAvailableFilesystemTypes(map);

        // Then
        assertEquals(1, result.size());
        assertEquals(environment.getEnvironment(), result.get(0).getEnvironment());
        assertEquals(FilesystemType.FILESYSTEM.getFileSystemType(), result.get(0).getCode());
        assertEquals("FS Nova", result.get(0).getLabel());
    }

    @Test
    void givenMapWithEnvironmentAndAvailablePlatformDeploymentType2_whenGetAvailableFilesystemTypes_thenAvailableFilesystemTypesAreReturned()
    {
        // Given
        final Environment environment = Environment.PRE;
        final List<Platform> platformList = new ArrayList<>();
        platformList.add(Platform.ETHER);
        final Map<Environment, List<Platform>> map = Map.of(environment, platformList);

        // When
        final List<FilesystemTypeDto> result = filesystemsService.getAvailableFilesystemTypes(map);

        // Then
        assertEquals(2, result.size());
        assertEquals(environment.getEnvironment(), result.get(0).getEnvironment());
        assertEquals(environment.getEnvironment(), result.get(1).getEnvironment());
        assertEquals(FilesystemType.FILESYSTEM_ETHER.getFileSystemType(), result.get(0).getCode());
        assertEquals("FS Ether", result.get(0).getLabel());
        assertEquals(FilesystemType.FILESYSTEM_EPSILON_ETHER.getFileSystemType(), result.get(1).getCode());
        assertEquals("Epsilon Ether", result.get(1).getLabel());
    }

    @Test
    void givenIvUser_whenBuildFullUserName_thenFullUsernameIsReturned() throws Exception
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);

        final String username = RandomStringUtils.randomAlphanumeric(15);
        final String surname1 = RandomStringUtils.randomAlphanumeric(15);
        final String surname2 = RandomStringUtils.randomAlphanumeric(15);
        final USUserDTO mockUser = new USUserDTO();
        mockUser.setUserName(username);
        mockUser.setSurname1(surname1);
        mockUser.setSurname2(surname2);

        final String expectedUsername = String.format("%s %s %s (%s)", username, surname1, surname2, ivUser);

        when(usersClient.getUser(eq(ivUser), any())).thenReturn(mockUser);

        // When
        final String result = filesystemsService.buildFullUserName(ivUser);

        // Then
        assertEquals(expectedUsername, result);
    }

    @Test
    void givenIvUser_whenGetUserMailAddress_thenMailAddressIsReturned() throws Exception
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(15);

        final String mailAddress = RandomStringUtils.randomAlphanumeric(15);
        final USUserDTO mockUser = new USUserDTO();
        mockUser.setEmail(mailAddress);

        when(usersClient.getUser(eq(ivUser), any())).thenReturn(mockUser);

        // When
        final String result = filesystemsService.getUserMailAddress(ivUser);

        // Then
        assertEquals(mailAddress, result);
    }


    @Test
    void getFilesystemsTransferInformationTest()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 1000);
        final String environment = "INT";
        final String uuaa = "UUAA";
        final String filesystemName = "filesystemName";
        final Integer productId = RandomUtils.nextInt(0, 1000);

        // Create Filesystem mock
        Filesystem fs = this.generateFSObject();

        // Create used DTO mocked objects
        FTATransferRelatedInfoAdminDTO[] ftaList = this.generateFTAListObject();


        when(this.filesystemsValidator.validateAndGetFilesystem(anyInt())).thenReturn(fs);
        when(this.fileTransferAdminClient.getFileTransfersInformation(environment, uuaa, filesystemName)).thenReturn(ftaList);

        // When
        FFilesystemRelatedTransferInfoDTO[] result = this.filesystemsService.getFilesystemsTransferInformation(productId, filesystemId);

        // Then
        Assertions.assertEquals(result[0].getTransferId(), ftaList[0].getTransferId());
        Assertions.assertEquals(result[0].getEnvironement(), ftaList[0].getEnvironement());
        Assertions.assertEquals(result[0].getTransferLocationSource().getFilesystemLocationName(), ftaList[0].getTransferLocationSource().getFilesysteLocationName());
        Assertions.assertEquals(result[0].getTransferLocationSource().getFilesystemLocationName(), fs.getName());

    }

    @Test
    void getFilesystemsTransferInformationNotSameNameTest()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 1000);
        final Integer productId = RandomUtils.nextInt(0, 1000);


        // Create Filesystem mock
        Filesystem fs = this.generateFSObject();

        // Create used DTO mocked objects
        FTATransferRelatedInfoAdminDTO[] ftaList = this.generateFTAListObject();

        // Force differnt name
        ftaList[0].getTransferLocationTarget().setFilesysteLocationName("differentName");
        ftaList[0].getTransferLocationSource().setFilesysteLocationName("differentName");

        // The call must not generate any match, due to different name
        when(this.filesystemsValidator.validateAndGetFilesystem(anyInt())).thenReturn(fs);
        when(fileTransferAdminClient.getFileTransfersInformation("INT", fs.getProduct().getUuaa(), fs.getName())).thenReturn(new FTATransferRelatedInfoAdminDTO[0]);

        // When
        FFilesystemRelatedTransferInfoDTO[] result = this.filesystemsService.getFilesystemsTransferInformation(productId, filesystemId);

        // Then
        Assertions.assertEquals(0, result.length);

    }

    @Test
    void getFilesystemsTransferInformationNoFSValidTest()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 1000);
        final String environment = "INT";
        final Integer productId = RandomUtils.nextInt(0, 1000);

        when(this.filesystemsValidator.validateAndGetFilesystem(filesystemId)).thenThrow(new NovaException(
                FilesystemsError.getNoSuchFilesystemError(filesystemId),
                MessageFormat.format("[FilesystemsAPI] -> [validateAndGetFilesystem]: the filesystem ID [{0}] does not exists.", filesystemId)));

        // When- Then
        Assertions.assertThrows(NovaException.class, () -> this.filesystemsService.getFilesystemsTransferInformation(productId, filesystemId));
    }

    // Generate a Filesystem object. Can be edited to add more information.
    private Filesystem generateFSObject()
    {
        // Create product mock
        Product product = new Product();
        product.setId(2);
        product.setUuaa("UUAA");

        Filesystem fs = new Filesystem()
        {
            @Override
            public FilesystemType getType()
            {
                return FilesystemType.FILESYSTEM;
            }
        };
        fs.setId(1);
        fs.setEnvironment(Environment.INT.getEnvironment());
        fs.setName("filesystemName");
        fs.setProduct(product);

        return fs;
    }

    // Generate an FTATransferRelatedInfoAdminDTO array. Can be edited to add more information
    private FTATransferRelatedInfoAdminDTO[] generateFTAListObject()
    {
        FTATransferRelatedInfoAdminDTO[] ftaList = new FTATransferRelatedInfoAdminDTO[1];
        FTATransferLocationInfo ftaLocationInfo = new FTATransferLocationInfo();
        FTATransferRelatedInfoAdminDTO fta = new FTATransferRelatedInfoAdminDTO();
        ftaLocationInfo.setUuaa("NOVA");
        ftaLocationInfo.setTransferLocation("NOVA");
        ftaLocationInfo.setIsOrigin(true);
        ftaLocationInfo.setFilesysteLocationName("filesystemName");
        ftaLocationInfo.setAbsolutePath("/path");
        fta.setTransferId(1);
        fta.setTransferStatus("REJECTED");
        fta.setTransferName("transferName");
        fta.setEnvironement("INT");
        fta.setTransferLocationTarget(ftaLocationInfo);
        fta.setTransferLocationSource(ftaLocationInfo);
        ftaList[0] = fta;

        return ftaList;
    }

    static class GetAllValidEnvironmentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of(Environment.INT),
                    Arguments.of(Environment.PRE),
                    Arguments.of(Environment.PRO)
            );
        }
    }
}
