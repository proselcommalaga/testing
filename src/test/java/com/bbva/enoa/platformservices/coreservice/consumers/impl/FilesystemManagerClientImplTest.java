package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.filesystemmanagerapi.client.feign.nova.rest.IRestHandlerFilesystemmanagerapi;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMUsageReportHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilesystemManagerClientImplTest
{
    private static final String NOT_AVAILABLE_FS_ERROR_CODE = "FILESYSTEMMANAGER-008";
    private static final String BUSY_FS_ERROR_CODE = "FILESYSTEMMANAGER-021";
    private static final String IN_USE_FS_ERROR_CODE = "FILESYSTEMMANAGER-007";
    private static final String ARCHIVE_IN_USE_FS_ERROR_CODE = "FILESYSTEMMANAGER-010";
    public static final String ACTION_PERMISSION_ERROR_CODE = "FILESYSTEMMANAGER-017";
    public static final String NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE = "FILESYSTEMMANAGER-022";
    @Mock
    private IRestHandlerFilesystemmanagerapi iRestHandler;
    @Mock
    private IErrorTaskManager errorTaskMgr;
    @Mock
    private Logger log;
    @InjectMocks
    private FilesystemManagerClientImpl client;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.client.init();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_call_create_filesystem_manager_returns_ko_response_then_throw_exception()
    {
        when(this.iRestHandler.createNewVolume(any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> this.client.callCreateFilesystemManager(new CreateNewFilesystemDto(), new Product()));
    }

    @Test
    public void when_call_create_filesystem_manager_returns_ok_response_then_execute()
    {
        when(this.iRestHandler.createNewVolume(any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.callCreateFilesystemManager(new CreateNewFilesystemDto(), new Product());

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_call_delete_filesystem_manager_returns_ko_response_with_generic_error_then_throw_generic_exception()
    {
        when(this.iRestHandler.deleteVolume(any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFilesystemManager(DummyConsumerDataGenerator.getDummyFilesystem());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemDeletionError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            Mockito.verify(errorTaskMgr, Mockito.times(1)).createErrorTaskWithRelatedId(Mockito.anyInt(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyInt(), Mockito.anyString());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_filesystem_manager_returns_ko_response_with_not_available_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_AVAILABLE_FS_ERROR_CODE);
        when(this.iRestHandler.deleteVolume(any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFilesystemManager(DummyConsumerDataGenerator.getDummyFilesystem());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemNotAvailableDeleteError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_filesystem_manager_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.deleteVolume(any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFilesystemManager(DummyConsumerDataGenerator.getDummyFilesystem());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_filesystem_manager_returns_ko_response_with_in_use_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.deleteVolume(any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFilesystemManager(DummyConsumerDataGenerator.getDummyFilesystem());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getDeletedUsedFilesystemError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_filesystem_manager_returns_ok_response_then_execute()
    {
        when(this.iRestHandler.deleteVolume(any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.callDeleteFilesystemManager(DummyConsumerDataGenerator.getDummyFilesystem());

        Mockito.verify(log, Mockito.times(0)).error(Mockito.anyString(), Mockito.any(Errors.class));
        Mockito.verifyNoInteractions(errorTaskMgr);
    }

    @Test
    public void when_call_archive_volume_returns_ko_response_with_archive_fs_in_use_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ARCHIVE_IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.archiveVolume(any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callArchiveVolume(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getTriedToArchiveUsedFilesystemError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_archive_volume_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.archiveVolume(any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callArchiveVolume(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_archive_volume_returns_ko_response_with_unknown_error_then_throw_general_exception()
    {
        when(this.iRestHandler.archiveVolume(any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callArchiveVolume(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getUnexpectedError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_archive_volume_returns_ok_response_with_unknown_error_then_execute()
    {
        when(this.iRestHandler.archiveVolume(any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.callArchiveVolume(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_call_restore_volume_returns_ko_response_then_throw_exception()
    {
        when(this.iRestHandler.restoreVolume(any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> this.client.callRestoreVolume(1));
    }

    @Test
    public void when_call_restore_volume_returns_ok_response_with_unknown_error_then_execute()
    {
        when(this.iRestHandler.restoreVolume(any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.callRestoreVolume(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_call_get_files_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any())).
                thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_files_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_files_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_files_returns_ko_response_with_another_error_then_throw_get_files_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(Constants.FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ALERT_INFO_ID);
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getGetFilesError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_files_returns_ok_null_response_with_another_error_then_return_empty_result()
    {
        FSMFileModelPaged fsmFileModelPaged = new FSMFileModelPaged();
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any())).thenReturn(new ResponseEntity<>(fsmFileModelPaged, HttpStatus.OK));

        FSFileModelPaged result = this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");

        Assertions.assertEquals(0, result.getFileRegistry().length);
    }

    @Test
    public void when_call_get_files_returns_ok_empty_response_with_another_error_then_return_empty_result()
    {
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>(new FSMFileModelPaged(), HttpStatus.OK));

        FSFileModelPaged result = this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");

        Assertions.assertEquals(0, result.getFileRegistry().length);
    }

    @Test
    public void when_call_get_files_returns_ok_populated_response_with_another_error_then_return_result()
    {

        FSMFileModelPaged fsFileModelPaged = new FSMFileModelPaged();
        fsFileModelPaged.setFileRegistry(DummyConsumerDataGenerator.getDummyFSMFileModels());
        when(this.iRestHandler.getFiles(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new ResponseEntity<>( fsFileModelPaged, HttpStatus.OK));

        FSFileModelPaged result = this.client.callGetFiles(1, "path", "name", 1, 500, "name", "asc");

        Assertions.assertEquals(1, result.getFileRegistry().length);
        FSFileModel firstResult = result.getFileRegistry()[0];
        Assertions.assertEquals("NOW", firstResult.getCreationDate());
        Assertions.assertEquals("FILENAME", firstResult.getFilename());
        Assertions.assertEquals("GROUP", firstResult.getGroup());
        Assertions.assertEquals("PATH", firstResult.getPath());
        Assertions.assertEquals("PERMISSIONS", firstResult.getPermissions());
        Assertions.assertEquals(1024L, firstResult.getSize());
        Assertions.assertEquals("USER", firstResult.getUser());
        Assertions.assertFalse(firstResult.getIsFolder());
    }

    @Test
    public void when_call_delete_file_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.deleteFile(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFile(1, new FSFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_file_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.deleteFile(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFile(1, new FSFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_file_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.deleteFile(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFile(1, new FSFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_file_returns_ko_response_with_another_error_then_throw_delete_file_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.deleteFile(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callDeleteFile(1, new FSFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getDeleteFileError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_delete_file_returns_ok_response_then_execute()
    {
        when(this.iRestHandler.deleteFile(any(), any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        FSFileLocationModel fsFileLocationModel = new FSFileLocationModel();
        fsFileLocationModel.setPath("PATH");
        fsFileLocationModel.setFilename("FILENAME");
        this.client.callDeleteFile(1, fsFileLocationModel);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void when_call_create_directory_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.createDirectory(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callCreateDirectory("", 1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_create_directory_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.createDirectory(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callCreateDirectory("", 1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_create_directory_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.createDirectory(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callCreateDirectory("", 1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_create_directory_returns_ko_response_with_another_error_then_throw_create_directory_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.createDirectory(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callCreateDirectory("", 1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getCreateDirectoryError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_create_directory_returns_ok_response_then_execute()
    {
        when(this.iRestHandler.createDirectory(any(), any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.callCreateDirectory("", 1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_call_get_file_use_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.getFilesystemUsage(any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFileUse(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_file_use_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.getFilesystemUsage(any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFileUse(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_file_use_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.getFilesystemUsage(any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFileUse(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_file_use_returns_ko_response_with_another_error_then_throw_get_file_use_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.getFilesystemUsage(any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callGetFileUse(1);
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getGetFileUseError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_get_file_use_returns_ok_response_then_return_result()
    {
        when(this.iRestHandler.getFilesystemUsage(any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyFSMFilesystemUsage(), HttpStatus.OK));

        FSFilesystemUsage result = this.client.callGetFileUse(1);

        Assertions.assertEquals("USED", result.getUsed());
        Assertions.assertEquals("AVAILABLE", result.getAvailable());
        Assertions.assertEquals("SIZE", result.getSize());
        Assertions.assertEquals(25, result.getUsagePercentage());
    }

    @Test
    public void when_call_update_quota_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.updateFilesystemQuota(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callUpdateQuota(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_update_quota_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.updateFilesystemQuota(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callUpdateQuota(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_update_quota_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.updateFilesystemQuota(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callUpdateQuota(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_update_quota_returns_ko_response_with_another_error_then_throw_update_quota_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.updateFilesystemQuota(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callUpdateQuota(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getUpdateQuotaError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_update_quota_returns_ok_response_then_execute()
    {
        when(this.iRestHandler.updateFilesystemQuota(any(), any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.callUpdateQuota(1, "");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_check_file_to_download_to_previous_env_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.checkFileToDownloadToPreviousEnv(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.checkFileToDownloadToPreviousEnv(1, new FSMFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_check_file_to_download_to_previous_env_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.checkFileToDownloadToPreviousEnv(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.checkFileToDownloadToPreviousEnv(1, new FSMFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_check_file_to_download_to_previous_env_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.checkFileToDownloadToPreviousEnv(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.checkFileToDownloadToPreviousEnv(1, new FSMFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_check_file_to_download_to_previous_env_returns_ko_response_with_another_error_then_throw_check_file_to_download_to_previous_env_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.checkFileToDownloadToPreviousEnv(any(), any())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.checkFileToDownloadToPreviousEnv(1, new FSMFileLocationModel());
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getDownloadToPreviousEnvironmentFileError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_check_file_to_download_to_previous_env_returns_ok_response_then_execute()
    {
        when(this.iRestHandler.checkFileToDownloadToPreviousEnv(any(), any())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        this.client.checkFileToDownloadToPreviousEnv(1, new FSMFileLocationModel());

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.any(FSMFileLocationModel.class), Mockito.anyInt());
    }

    @Test
    public void when_call_filesystem_usage_report_returns_ko_response_with_action_permission_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ACTION_PERMISSION_ERROR_CODE);
        when(this.iRestHandler.getFilesystemsUsageReport(any(), any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callFilesystemsUsageReport(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getActionPermissionError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_filesystem_usage_report_returns_ko_response_with_busy_fs_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BUSY_FS_ERROR_CODE);
        when(this.iRestHandler.getFilesystemsUsageReport(any(), any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callFilesystemsUsageReport(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getFilesystemBusyError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_filesystem_usage_report_returns_ko_response_with_not_allowed_file_name_pattern_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(NOT_ALLOWED_FILE_NAME_PATTERN_ERROR_CODE);
        when(this.iRestHandler.getFilesystemsUsageReport(any(), any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callFilesystemsUsageReport(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getNotAllowedFileNamePatternError();
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_filesystem_usage_report_returns_ko_response_with_another_error_then_throw_filesystem_usage_report_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(IN_USE_FS_ERROR_CODE);
        when(this.iRestHandler.getFilesystemsUsageReport(any(), any())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            this.client.callFilesystemsUsageReport(1, "");
        }
        catch (NovaException e)
        {
            NovaError expectedNovaError = FilesystemsError.getGetFileUseError("");
            NovaError actualNovaError = e.getErrorCode();
            Assertions.assertEquals(expectedNovaError.getErrorCode(), actualNovaError.getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_call_filesystem_usage_report_returns_ok_response_then_return_result()
    {
        when(this.iRestHandler.getFilesystemsUsageReport(any(), any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyFSMUsageReportDTO(), HttpStatus.OK));

        FilesystemsUsageReportDTO result = this.client.callFilesystemsUsageReport(1, "");

        Assertions.assertEquals(100D, result.getTotalStorageAssigned());
        Assertions.assertEquals(50D, result.getTotalStorageAssignedPercentage());
        Assertions.assertEquals(200D, result.getTotalStorageAvailable());
        Assertions.assertEquals(50D, result.getTotalStorageAvailablePercentage());
    }

    @Test
    public void when_get_filesystem_usage_history_snapshot_returns_ko_response_then_throw_exception()
    {
        when(this.iRestHandler.getFileSystemUsageHistorySnapshot()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> this.client.getFileSystemUsageHistorySnapshot());
    }

    @Test
    public void when_get_filesystem_usage_history_snapshot_returns_ok_response_then_return_result()
    {
        when(this.iRestHandler.getFileSystemUsageHistorySnapshot()).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyFSMUsageReportHistorySnapshotDtos(), HttpStatus.OK));

        FSMUsageReportHistorySnapshotDTO[] result = this.client.getFileSystemUsageHistorySnapshot();

        Assertions.assertEquals(1, result.length);
        FSMUsageReportHistorySnapshotDTO firstResult = result[0];
        Assertions.assertEquals("UUAA", firstResult.getUuaa());
        Assertions.assertEquals(10D, firstResult.getValue());
        Assertions.assertEquals("PROPERTY", firstResult.getProperty());
        Assertions.assertEquals("INT", firstResult.getEnvironment());
    }
}