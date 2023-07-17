package com.bbva.enoa.platformservices.coreservice.filesystemsapi.listener;

import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FFilesystemRelatedTransferInfoDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemTypeDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsAlertService;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsService;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListenerFilesystemsapiTest
{
    @Mock
    private IFilesystemsService filesystemsService;

    @Mock
    private IFilesystemsAlertService filesystemsAlertService;

    @InjectMocks
    private ListenerFilesystemsapi listenerFilesystemsapi;

    @Test
    void givenFilesystem_whenGetFilesystemWithoutExceptions_thenFilesystemIsReturned() throws Errors
    {
        // Given
        final Integer filesystemId = 0;

        final NovaMetadata novaMetadata = mockNovaMetadata();

        final FilesystemDto mockResponse = Mockito.mock(FilesystemDto.class);
        when(filesystemsService.getFilesystem(eq(filesystemId))).thenReturn(mockResponse);

        // When
        final FilesystemDto result = this.listenerFilesystemsapi.getFilesystem(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystem(eq(filesystemId));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenFilesystem_whenGetFilesystemWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata();
        when(filesystemsService.getFilesystem(eq(filesystemId))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystem(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).getFilesystem(eq(filesystemId));
    }

    @Test
    void givenFilesystemPathAndEnvironment_whenGetFilesystemIdWithoutExceptions_thenFilesystemIdIsReturned() throws Errors
    {
        // Given
        final String filesystemPath = RandomStringUtils.randomAlphanumeric(15);
        final String environment    = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        final Integer mockResponse = RandomUtils.nextInt(0, 10);
        when(filesystemsService.getFilesystemId(eq(filesystemPath), eq(environment))).thenReturn(mockResponse);

        // When
        final Integer result = this.listenerFilesystemsapi.getFilesystemId(novaMetadata, filesystemPath, environment);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemId(eq(filesystemPath), eq(environment));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenFilesystemPathAndEnvironment_whenGetFilesystemIdWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String filesystemPath = RandomStringUtils.randomAlphanumeric(15);
        final String environment    = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemId(eq(filesystemPath), eq(environment))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystemId(novaMetadata, filesystemPath, environment)
        );

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemId(eq(filesystemPath), eq(environment));
    }

    @Test
    void givenFilesystemIdAndDirectory_whenCreateDirectoryWithoutExceptions_thenDirectoryIsCreated() throws Errors
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String  directory    = RandomStringUtils.randomAlphanumeric(10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).createDirectory(eq(ivUser), eq(filesystemId), eq(directory));

        // When
        this.listenerFilesystemsapi.createDirectory(novaMetadata, filesystemId, directory);

        // Then
        verify(this.filesystemsService, times(1)).createDirectory(eq(ivUser), eq(filesystemId), eq(directory));
    }

    @Test
    void givenFilesystemIdAndDirectory_whenCreateDirectoryWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String  directory    = RandomStringUtils.randomAlphanumeric(10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).createDirectory(eq(ivUser), eq(filesystemId), eq(directory));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.createDirectory(novaMetadata, filesystemId, directory)
        );

        // Then
        verify(this.filesystemsService, times(1)).createDirectory(eq(ivUser), eq(filesystemId), eq(directory));
    }

    @Test
    void givenFilesystemId_whenDeleteFilesystemWithoutExceptions_thenFilesystemIsDeleted() throws Errors
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).deleteFilesystem(eq(ivUser), eq(filesystemId));

        // When
        this.listenerFilesystemsapi.deleteFilesystem(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).deleteFilesystem(eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenFilesystemId_whenDeleteFilesystemWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).deleteFilesystem(eq(ivUser), eq(filesystemId));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.deleteFilesystem(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).deleteFilesystem(eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenNewFilesystem_whenCreateFilesystemWithoutExceptions_thenFilesystemIsCreated() throws Errors
    {
        // Given
        final String                 ivUser           = RandomStringUtils.randomAlphanumeric(7);
        final Integer                productId        = RandomUtils.nextInt(0, 10);
        final CreateNewFilesystemDto newFilesystemDto = new CreateNewFilesystemDto();

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).createFilesystem(eq(newFilesystemDto), eq(ivUser), eq(productId));

        // When
        this.listenerFilesystemsapi.createFilesystem(novaMetadata, newFilesystemDto, productId);

        // Then
        verify(this.filesystemsService, times(1)).createFilesystem(eq(newFilesystemDto), eq(ivUser), eq(productId));
    }

    @Test
    void givenNewFilesystem_whenCreateFilesystemWithException_thenLogAndTraceExceptionIsThrown()
    {
        // Given
        final String                 ivUser           = RandomStringUtils.randomAlphanumeric(7);
        final Integer                productId        = RandomUtils.nextInt(0, 10);
        final CreateNewFilesystemDto newFilesystemDto = new CreateNewFilesystemDto();

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).createFilesystem(eq(newFilesystemDto), eq(ivUser), eq(productId));

        // When
        final LogAndTraceException exception = assertThrows(
                LogAndTraceException.class,
                () -> this.listenerFilesystemsapi.createFilesystem(novaMetadata, newFilesystemDto, productId)
        );

        // Then
        verify(this.filesystemsService, times(1)).createFilesystem(eq(newFilesystemDto), eq(ivUser), eq(productId));
        assertEquals(productId, exception.getProductId());
    }

    @Test
    void givenNothing_whenGetFilesystemsStatusesWithoutExceptions_thenFilesystemsStatusesIsReturned() throws Errors
    {
        // Given
        final String[] mockResponse = new String[0];

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemsStatuses()).thenReturn(mockResponse);

        // When
        final String[] result = this.listenerFilesystemsapi.getFilesystemsStatuses(novaMetadata);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsStatuses();
        assertEquals(mockResponse, result);
    }

    @Test
    void givenNothing_whenGetFilesystemsStatusesWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final NovaMetadata novaMetadata = mockNovaMetadata();

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).getFilesystemsStatuses();

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystemsStatuses(novaMetadata)
        );

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsStatuses();
    }

    @Test
    void givenNothing_whenGetFilesystemsTypesWithoutExceptions_thenFilesystemsTypesIsReturned() throws Errors
    {
        // Given
        final String[] mockResponse = new String[0];

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemsTypes()).thenReturn(mockResponse);

        // When
        final String[] result = this.listenerFilesystemsapi.getFilesystemsTypes(novaMetadata);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsTypes();
        assertEquals(mockResponse, result);
    }

    @Test
    void givenNothing_whenGetFilesystemsTypesWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final NovaMetadata novaMetadata = mockNovaMetadata();

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).getFilesystemsTypes();

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystemsTypes(novaMetadata)
        );

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsTypes();
    }

    @Test
    void givenFilesystemId_whenArchiveFilesystemWithoutExceptions_thenFilesystemIsArchived() throws Errors
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).archiveFilesystem(eq(ivUser), eq(filesystemId));

        // When
        this.listenerFilesystemsapi.archiveFilesystem(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).archiveFilesystem(eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenFilesystemId_whenArchiveFilesystemWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).archiveFilesystem(eq(ivUser), eq(filesystemId));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.archiveFilesystem(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).archiveFilesystem(eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenFilesystemIdAndFilename_whenDownloadToPreWithoutExceptions_thenFileIsDownloaded() throws Errors
    {
        // Given
        final String  ivUser         = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId   = RandomUtils.nextInt(0, 10);
        final String  filename       = RandomStringUtils.randomAlphanumeric(10);
        final String  filesystemPath = RandomStringUtils.randomAlphanumeric(15);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).downloadToPre(eq(ivUser), eq(filesystemId), eq(filename), eq(filesystemPath));

        // When
        this.listenerFilesystemsapi.downloadToPre(novaMetadata, filesystemId, filename, filesystemPath);

        // Then
        verify(this.filesystemsService, times(1)).downloadToPre(eq(ivUser), eq(filesystemId), eq(filename), eq(filesystemPath));
    }

    @Test
    void givenFilesystemIdAndFile_whenDownloadToPreWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser         = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId   = RandomUtils.nextInt(0, 10);
        final String  filename       = RandomStringUtils.randomAlphanumeric(10);
        final String  filesystemPath = RandomStringUtils.randomAlphanumeric(15);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).downloadToPre(eq(ivUser), eq(filesystemId), eq(filename), eq(filesystemPath));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.downloadToPre(novaMetadata, filesystemId, filename, filesystemPath)
        );

        // Then
        verify(this.filesystemsService, times(1)).downloadToPre(eq(ivUser), eq(filesystemId), eq(filename), eq(filesystemPath));
    }

    @Test
    void givenFileAndFilesystem_whenDeleteFileWithoutExceptions_thenFileIsDeleted() throws Errors
    {
        // Given
        final String              ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final FSFileLocationModel file         = new FSFileLocationModel();
        final Integer             filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).deleteFile(eq(file), eq(ivUser), eq(filesystemId));

        // When
        this.listenerFilesystemsapi.deleteFile(novaMetadata, file, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).deleteFile(eq(file), eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenFileAndFilesystem_whenDeleteFileWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String              ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final FSFileLocationModel file         = new FSFileLocationModel();
        final Integer             filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).deleteFile(eq(file), eq(ivUser), eq(filesystemId));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.deleteFile(novaMetadata, file, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).deleteFile(eq(file), eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenFilesystemId_whenIsFilesystemFrozenWithoutExceptions_thenFilesystemIsFrozen() throws Errors
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final Boolean mockResponse = RandomUtils.nextBoolean();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.isFilesystemFrozen(eq(filesystemId))).thenReturn(mockResponse);

        // When
        Boolean result = this.listenerFilesystemsapi.isFilesystemFrozen(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).isFilesystemFrozen(eq(filesystemId));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenFilesystemId_whenIsFilesystemFrozenWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.isFilesystemFrozen(filesystemId)).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.isFilesystemFrozen(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).isFilesystemFrozen(eq(filesystemId));
    }

    @Test
    void givenFilesystemIdAndNewQuota_whenUpdateFilesystemQuotaWithoutExceptions_thenQuotaIsUpdated() throws Errors
    {
        // Given
        final String  ivUser                = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId          = RandomUtils.nextInt(0, 10);
        final String  newFilesystemPackCode = RandomStringUtils.randomAlphanumeric(15);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).updateFilesystemQuota(eq(filesystemId), eq(newFilesystemPackCode), eq(ivUser));

        // When
        this.listenerFilesystemsapi.updateFilesystemQuota(novaMetadata, filesystemId, newFilesystemPackCode);

        // Then
        verify(this.filesystemsService, times(1)).updateFilesystemQuota(eq(filesystemId), eq(newFilesystemPackCode), eq(ivUser));
    }

    @Test
    void givenFilesystemIdAndNewQuota_whenUpdateFilesystemQuotaWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser                = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId          = RandomUtils.nextInt(0, 10);
        final String  newFilesystemPackCode = RandomStringUtils.randomAlphanumeric(15);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);
        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).updateFilesystemQuota(eq(filesystemId), eq(newFilesystemPackCode), eq(ivUser));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.updateFilesystemQuota(novaMetadata, filesystemId, newFilesystemPackCode)
        );

        // Then
        verify(this.filesystemsService, times(1)).updateFilesystemQuota(eq(filesystemId), eq(newFilesystemPackCode), eq(ivUser));
    }

    @Test
    void givenProductAndEnvironmentAndFilesystemType_whenGetProductFilesystemsWithoutExceptions_thenProductFilesystemsAreReturned() throws Errors
    {
        // Given
        final Integer         productId      = RandomUtils.nextInt(0, 10);
        final String          environment    = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String          filesystemType = FilesystemType.values()[RandomUtils.nextInt(0, FilesystemType.values().length)].name();
        final FilesystemDto[] mockResponse   = new FilesystemDto[0];

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getProductFilesystems(eq(productId), eq(environment), eq(filesystemType))).thenReturn(mockResponse);

        // When
        FilesystemDto[] result = this.listenerFilesystemsapi.getProductFilesystems(novaMetadata, productId, environment, filesystemType);

        // Then
        verify(this.filesystemsService, times(1)).getProductFilesystems(eq(productId), eq(environment), eq(filesystemType));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenProductAndEnvironmentAndFilesystemType_whenGetProductFilesystemsWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer productId      = RandomUtils.nextInt(0, 10);
        final String  environment    = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String  filesystemType = FilesystemType.FILESYSTEM.name();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getProductFilesystems(eq(productId), eq(environment), eq(filesystemType))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        final LogAndTraceException exception = assertThrows(
                LogAndTraceException.class,
                () -> this.listenerFilesystemsapi.getProductFilesystems(novaMetadata, productId, environment, filesystemType)
        );

        // Then
        verify(this.filesystemsService, times(1)).getProductFilesystems(eq(productId), eq(environment), eq(filesystemType));
        assertEquals(productId, exception.getProductId());
    }

    @Test
    void givenFilesystemId_whenRestoreFilesystemWithoutExceptions_thenFilesystemIsRestored() throws Errors
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsService).restoreFilesystem(eq(ivUser), eq(filesystemId));

        // When
        this.listenerFilesystemsapi.restoreFilesystem(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).restoreFilesystem(eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenFilesystemId_whenRestoreFilesystemWithException_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).restoreFilesystem(eq(ivUser), eq(filesystemId));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.restoreFilesystem(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).restoreFilesystem(eq(ivUser), eq(filesystemId));
    }

    @Test
    void givenProductAndEnvironment_whenListFilesystemTypesWithoutExceptions_thenFilesystemTypesAreReturned() throws Errors
    {
        // Given
        final Integer             productId    = RandomUtils.nextInt(0, 10);
        final String              environment  = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final FilesystemTypeDto[] mockResponse = new FilesystemTypeDto[0];

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getAvailableFilesystemTypes(eq(productId), eq(environment))).thenReturn(mockResponse);

        // When
        FilesystemTypeDto[] result = this.listenerFilesystemsapi.listFilesystemTypes(novaMetadata, productId, environment);

        // Then
        verify(this.filesystemsService, times(1)).getAvailableFilesystemTypes(eq(productId), eq(environment));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenProductAndEnvironment_whenListFilesystemTypesWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer productId   = RandomUtils.nextInt(0, 10);
        final String  environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getAvailableFilesystemTypes(eq(productId), eq(environment))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.listFilesystemTypes(novaMetadata, productId, environment)
        );

        // Then
        verify(this.filesystemsService, times(1)).getAvailableFilesystemTypes(eq(productId), eq(environment));
    }

    @Test
    void givenFilesystemId_whenGetFilesystemUsageWithoutExceptions_thenFilesystemUsageIsReturned() throws Errors
    {
        // Given
        final Integer           filesystemId = RandomUtils.nextInt(0, 10);
        final FSFilesystemUsage mockResponse = new FSFilesystemUsage();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemUsage(eq(filesystemId))).thenReturn(mockResponse);

        // When
        FSFilesystemUsage result = this.listenerFilesystemsapi.getFilesystemUsage(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemUsage(eq(filesystemId));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenFilesystemId_whenGetFilesystemUsageWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemUsage(eq(filesystemId))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystemUsage(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemUsage(eq(filesystemId));
    }

    @Test
    void givenFilesystemAndPathAndFile_whenGetFilesWithoutExceptions_thenFilesAreReturned() throws Errors
    {
        // Given
        final Integer       filesystemId = RandomUtils.nextInt(0, 10);
        final String        path         = RandomStringUtils.randomAlphanumeric(15);
        final String        filename     = RandomStringUtils.randomAlphanumeric(10);
        final FSFileModel[] mockResponse = new FSFileModel[0];

        int numberPage = 1;
        int sizePage = 500;
        String field = "name";
        String order = "asc";

        final NovaMetadata novaMetadata = mockNovaMetadata();

        FSFileModelPaged fsFileModelPaged = new FSFileModelPaged();
        fsFileModelPaged.setFileRegistry(mockResponse);
        when(filesystemsService.getFiles(eq(filesystemId), eq(path), eq(filename),
                        eq(numberPage), eq(sizePage), eq(field), eq(order)))
                .thenReturn(fsFileModelPaged);

        // When
        FSFileModelPaged result = this.listenerFilesystemsapi.getFiles(novaMetadata, filesystemId, path, filename, field, numberPage, sizePage, order);

        // Then
        verify(this.filesystemsService, times(1)).getFiles(eq(filesystemId), eq(path),
                eq(filename), eq(numberPage), eq(sizePage), eq(field), eq(order));
        assertEquals(fsFileModelPaged, result);
    }

    @Test
    void givenFilesystemAndPathAndFile_whenGetFilesWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String  path         = RandomStringUtils.randomAlphanumeric(15);
        final String  filename     = RandomStringUtils.randomAlphanumeric(10);

        int numberPage = 1;
        int sizePage = 500;
        String field = "name";
        String order = "asc";

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFiles(eq(filesystemId), eq(path), eq(filename), eq(numberPage)
                        , eq( sizePage), eq(field), eq(order)))
                .thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFiles(novaMetadata, filesystemId,
                        path, filename, field, numberPage, sizePage, order)
                //filesystemsService.getFiles(filesystemId, filterPath, filename, numberPage, sizePage, field, order);
        );

        // Then
        verify(this.filesystemsService, times(1)).getFiles(eq(filesystemId), eq(path), eq(filename),
                eq(numberPage), eq(sizePage), eq(field), eq(order));
    }

    @Test
    void givenProductAndEnvironment_whenGetEventsAlertInfoConfigWithoutExceptions_thenEventAlertInfoConfigIsReturned() throws Errors
    {
        // Given
        final String                     ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer                    productId    = RandomUtils.nextInt(0, 10);
        final String                     environment  = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final FSFilesystemAlertInfoDto[] mockResponse = new FSFilesystemAlertInfoDto[0];

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        when(filesystemsAlertService.getAllFilesystemAlertConfigurations(eq(ivUser), eq(productId), eq(environment))).thenReturn(mockResponse);

        // When
        final FSFilesystemAlertInfoDto[] result = this.listenerFilesystemsapi.getEventsAlertInfoConfig(novaMetadata, productId, environment);

        // Then
        verify(this.filesystemsAlertService, times(1)).getAllFilesystemAlertConfigurations(eq(ivUser), eq(productId), eq(environment));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenProductAndEnvironment_whenGetEventsAlertInfoConfigWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final String  ivUser      = RandomStringUtils.randomAlphanumeric(7);
        final Integer productId   = RandomUtils.nextInt(0, 10);
        final String  environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        when(filesystemsAlertService.getAllFilesystemAlertConfigurations(eq(ivUser), eq(productId), eq(environment))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getEventsAlertInfoConfig(novaMetadata, productId, environment)
        );

        // Then
        verify(this.filesystemsAlertService, times(1)).getAllFilesystemAlertConfigurations(eq(ivUser), eq(productId), eq(environment));
    }

    @Test
    void givenProductAndEnvironment_whenGetFilesystemUsageReportWithoutExceptions_thenUsageReportIsReturned() throws Errors
    {
        // Given
        final Integer                   productId    = RandomUtils.nextInt(0, 10);
        final String                    environment  = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final FilesystemsUsageReportDTO mockResponse = new FilesystemsUsageReportDTO();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemsUsageReport(eq(productId), eq(environment))).thenReturn(mockResponse);

        // When
        final FilesystemsUsageReportDTO result = this.listenerFilesystemsapi.getFilesystemsUsageReport(novaMetadata, productId, environment);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsUsageReport(eq(productId), eq(environment));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenProductAndEnvironment_whenGetFilesystemUsageReportWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer productId   = RandomUtils.nextInt(0, 10);
        final String  environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsService.getFilesystemsUsageReport(eq(productId), eq(environment))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystemsUsageReport(novaMetadata, productId, environment)
        );

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsUsageReport(eq(productId), eq(environment));
    }

    @Test
    void givenFilesystemId_whenGetFilesystemAlertInfoWithoutExceptions_thenFilesystemAlertInfoIsReturned() throws Errors
    {
        // Given
        final Integer                  filesystemId = RandomUtils.nextInt(0, 10);
        final FSFilesystemAlertInfoDto mockResponse = new FSFilesystemAlertInfoDto();

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsAlertService.getFilesystemAlertConfiguration(eq(filesystemId))).thenReturn(mockResponse);

        // When
        final FSFilesystemAlertInfoDto result = this.listenerFilesystemsapi.getFilesystemAlertInfo(novaMetadata, filesystemId);

        // Then
        verify(this.filesystemsAlertService, times(1)).getFilesystemAlertConfiguration(eq(filesystemId));
        assertEquals(mockResponse, result);
    }

    @Test
    void givenFilesystemId_whenGetFilesystemAlertInfoWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);

        final NovaMetadata novaMetadata = mockNovaMetadata();

        when(filesystemsAlertService.getFilesystemAlertConfiguration(eq(filesystemId))).thenThrow(new NovaException(FilesystemsError.getUnexpectedError()));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.getFilesystemAlertInfo(novaMetadata, filesystemId)
        );

        // Then
        verify(this.filesystemsAlertService, times(1)).getFilesystemAlertConfiguration(eq(filesystemId));
    }

    @Test
    void givenFilesystemAndAlertInfo_whenSaveFilesystemAlertInfoWithoutExceptions_thenFilesystemAlertInfoIsSaved() throws Errors
    {
        // Given
        final String                   ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer                  filesystemId = RandomUtils.nextInt(0, 10);
        final FSFilesystemAlertInfoDto alertInfo    = new FSFilesystemAlertInfoDto();

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doNothing().when(filesystemsAlertService).updateFilesystemAlertConfiguration(eq(ivUser), eq(alertInfo), eq(filesystemId));

        // When
        this.listenerFilesystemsapi.saveFilesystemAlertInfo(novaMetadata, alertInfo, filesystemId);

        // Then
        verify(this.filesystemsAlertService, times(1)).updateFilesystemAlertConfiguration(eq(ivUser), eq(alertInfo), eq(filesystemId));
    }

    @Test
    void givenFilesystemAndAlertInfo_whenSaveFilesystemAlertInfoWithExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final String                   ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final Integer                  filesystemId = RandomUtils.nextInt(0, 10);
        final FSFilesystemAlertInfoDto alertInfo    = new FSFilesystemAlertInfoDto();

        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsAlertService).updateFilesystemAlertConfiguration(eq(ivUser), eq(alertInfo), eq(filesystemId));

        // When
        assertThrows(
                NovaException.class,
                () -> this.listenerFilesystemsapi.saveFilesystemAlertInfo(novaMetadata, alertInfo, filesystemId)
        );

        // Then
        verify(this.filesystemsAlertService, times(1)).updateFilesystemAlertConfiguration(eq(ivUser), eq(alertInfo), eq(filesystemId));
    }

    @Test
    void getFilesystemsOnlineTransfersRelatedListenerTest() throws Errors
    {
        // Given
        final String       ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);
        final Integer      filesystemId = RandomUtils.nextInt(0, 1000);
        final Integer      productId    = RandomUtils.nextInt(0, 1000);


        // When
        when(this.filesystemsService.getFilesystemsTransferInformation( productId, filesystemId)).thenReturn(new FFilesystemRelatedTransferInfoDTO[0]);

        FFilesystemRelatedTransferInfoDTO[] result = this.listenerFilesystemsapi.getFilesystemsOnlineTransfersRelated(novaMetadata, productId, filesystemId);

        // Then
        verify(this.filesystemsService, times(1)).getFilesystemsTransferInformation( productId, filesystemId);
    }

    @Test
    void getFilesystemsOnlineTransfersRelatedListenerExceptionTest() throws Errors
    {
        // Given
        final String       ivUser       = RandomStringUtils.randomAlphanumeric(7);
        final NovaMetadata novaMetadata = mockNovaMetadata(ivUser);

        final Integer filesystemId = RandomUtils.nextInt(0, 1000);
        final Integer productId    = RandomUtils.nextInt(0, 1000);

        // When
        doThrow(new NovaException(FilesystemsError.getUnexpectedError())).when(filesystemsService).getFilesystemsTransferInformation(productId, filesystemId);

        Assertions.assertThrows(NovaException.class, () -> this.listenerFilesystemsapi.getFilesystemsOnlineTransfersRelated(novaMetadata, productId, filesystemId));

        // Then
        verify(filesystemsService, times(1)).getFilesystemsTransferInformation(productId, filesystemId);
    }


    private NovaMetadata mockNovaMetadata()
    {
        return mockNovaMetadata(RandomStringUtils.randomAlphanumeric(7));
    }

    private NovaMetadata mockNovaMetadata(final String ivUser)
    {
        final NovaMetadata novaMetadata = Mockito.mock(NovaMetadata.class, Mockito.RETURNS_DEEP_STUBS);
        when(novaMetadata.getNovaImplicitHeadersInput().getRawHeaderString(eq("iv-user"))).thenReturn(Collections.singletonList(ivUser));

        return novaMetadata;
    }
}
