package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.filesystemmanagerapi.client.feign.nova.rest.IRestHandlerFilesystemmanagerapi;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.client.feign.nova.rest.IRestListenerFilesystemmanagerapi;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.client.feign.nova.rest.impl.RestHandlerFilesystemmanagerapi;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.*;
import com.bbva.enoa.apirestgen.filesystemsapi.model.*;
import com.bbva.enoa.core.novabootstarter.consumers.ApiClientResponseWrapper;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFilesystemManagerClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Task service client.
 *
 * @author BBVA - XE30432
 */
@Service
public class FilesystemManagerClientImpl implements IFilesystemManagerClient
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemManagerClientImpl.class);

    /**
     * At this moment this value is set by default to true.
     * In the future, from FRONT view, we will have to request the nfs value via filesystemAPI (createNewFilesystemDto call).
     */
    private static final boolean DEFAULT_NFS_VALUE = true;
    private static final String BUSY_FILE_SYSTEM_ERROR_CODE = "FILESYSTEMMANAGER-021";
    private static final String USED_FILE_SYSTEM_DELETION_ERROR_CODE = "FILESYSTEMMANAGER-007";
    private static final String NOT_EMPTY_EPSILON_ERROR_CODE = "FILESYSTEMMANAGER-063";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Attribute - Automator Rest Handler - Interface
     */
    @Autowired
    private IRestHandlerFilesystemmanagerapi iRestHandler;

    /**
     * Handler of the client of the Mail service API
     */
    private RestHandlerFilesystemmanagerapi restHandlerFilesystemmanagerapi;

    /**
     * To do task service
     */
    @Autowired
    private IErrorTaskManager errorTaskMgr;

    /**
     * Initialize the client RestHandler from injected IRestHandler
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerFilesystemmanagerapi = new RestHandlerFilesystemmanagerapi(this.iRestHandler);
    }

    @Override
    public void callCreateFilesystemManager(final CreateNewFilesystemDto createNewFilesystemDto, final Product product)
    {
        LOG.debug("[FilesystemManagerClient] -> [callCreateFilesystemManager]: creating a new filesystem calling FilesystemManager with parameters: [{}] for the product {}", createNewFilesystemDto,
                product.getName());
        final FSMVolumeModel createNewVolume = this.buildFSMVolumeModel(createNewFilesystemDto, product);

        SingleApiClientResponseWrapper<Errors> response = new SingleApiClientResponseWrapper<>();
        this.restHandlerFilesystemmanagerapi.createNewVolume(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void createNewVolume()
            {
                LOG.debug("[FilesystemManagerClient] -> [callCreateFilesystemManager]: called to Filesystem Manager to createNewVolume with the parameters: [{}] for the product: [{}] successfully",
                        createNewVolume, product.getName());
            }

            @Override
            public void createNewVolumeErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callCreateFilesystemManager]: failed trying to createNewVolume of the filesystem with the parameters: [{}] for the product: [{}] "
                        + "calling Filesystem Manager Service. Response: [{}]", createNewVolume, product.getName(), outcome.getBodyExceptionMessage());
                response.set(outcome);
            }
        }, createNewVolume);

        // check the response
        this.checkCreateResponse(product, createNewVolume, response.get());
    }

    @Override
    public void callUpdateConfigurationFilesystemManager(final Filesystem filesystem, final FilesystemConfigurationDto[] configuration, final String ivUser)
    {
        LOG.debug("[FilesystemManagerClient] -> [callUpdateConfigurationFilesystemManager]: Updating the configuration of filesystem calling FilesystemManager with parameters: [{}] for the filesystem [{}] of UUAA [{}]", configuration,
                filesystem.getName(), filesystem.getProduct().getUuaa());

        // parse the configuration
        final FSMVolumeConfiguration[] fsmVolumeConfiguration = Arrays.stream(configuration)
                        .map(FilesystemManagerClientImpl::buildFSMConfiguration)
                        .toArray(FSMVolumeConfiguration[]::new);

        SingleApiClientResponseWrapper<Errors> response = new SingleApiClientResponseWrapper<>();
        this.restHandlerFilesystemmanagerapi.updateFilesystemConfiguration(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void updateFilesystemConfiguration()
            {
                LOG.info("[FilesystemManagerClient] -> [callUpdateConfigurationFilesystemManager]: called to Filesystem Manager to updateFilesystemConfiguration with the parameters: [{}] for the filesystem [{}] of UUAA [{}] successfully",
                        fsmVolumeConfiguration, filesystem.getName(), filesystem.getProduct().getUuaa());
            }

            @Override
            public void updateFilesystemConfigurationErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callUpdateConfigurationFilesystemManager]: failed trying to updateFilesystemConfiguration of the filesystem with the parameters: [{}] for the filesystem [{}] of UUAA [{}] "
                        + "calling Filesystem Manager Service. Response: [{}]", fsmVolumeConfiguration, filesystem.getName(), filesystem.getProduct().getUuaa(), outcome.getBodyExceptionMessage());
                response.set(outcome);
            }
        }, fsmVolumeConfiguration, filesystem.getId());

        this.checkUpdateConfigurationResponse(filesystem, fsmVolumeConfiguration, response.get(), ivUser);
    }

    @Override
    public void callDeleteFilesystemManager(final Filesystem filesystem)
    {

        LOG.debug(
                "[FilesystemManagerClient] -> [callDeleteFilesystemManager]: calling FilesystemManagerAPI to delete volumen of the filesystem with name [{}] in environment [{}] for product [{}]",
                filesystem.getName(), filesystem.getEnvironment(), filesystem.getProduct().getName());

        SingleApiClientResponseWrapper<Errors> response = new SingleApiClientResponseWrapper<>();
        this.restHandlerFilesystemmanagerapi.deleteVolume(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void deleteVolume()
            {
                LOG.debug("[FilesystemManagerClient] -> [callDeleteFilesystemManager]: called FilesystemManagerAPI to delete volume of the filesystem with name : [{}] successfully", filesystem.getName());
            }

            @Override
            public void deleteVolumeErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callDeleteFilesystemManager]: failed trying to delete a filesystem with the filesystem name: [{}] "
                        + "calling Filesystem Manager Service. Response: [{}]", filesystem.getName(), outcome.getBodyExceptionMessage());
                response.set(outcome);
            }
        }, filesystem.getId());

        this.checkDeleteResponse(filesystem, response.get());
    }

    @Override
    public void callArchiveVolume(final int filesystemId)
    {
        LOG.debug("[FilesystemManagerClient] -> [callArchiveVolume]: archiving the filesystem id [{}] calling FilesystemManager", filesystemId);

        SingleApiClientResponseWrapper<Errors> exception = new SingleApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.archiveVolume(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void archiveVolume()
            {
                LOG.debug("[FilesystemManagerClient] -> [callArchiveVolume]: called archiveVolume of the FilesystemManager for the filesystem id: [{}] successfully", filesystemId);
            }

            @Override
            public void archiveVolumeErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callArchiveVolume]: failed trying to call FilesystemManager to archive the filesystem: [{}] : {}", filesystemId, outcome.getBodyExceptionMessage());
                exception.set(outcome);
            }
        }, filesystemId);

        // Check the errors from FilesystemManager call, if any.
        this.checkArchiveException(filesystemId, exception.get());
    }

    @Override
    public void callRestoreVolume(final int filesystemId)
    {
        LOG.debug("[FilesystemManagerClient] -> [callRestoreVolume]: restoring the filesystem id [{}] calling FilesystemManager", filesystemId);

        SingleApiClientResponseWrapper<Errors> exception = new SingleApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.restoreVolume(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void restoreVolume()
            {
                LOG.debug("[FilesystemManagerClient] -> [callRestoreVolume]: successfully called FilesystemManager to restore the filesystem: [{}]", filesystemId);
            }

            @Override
            public void restoreVolumeErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callRestoreVolume]: failed trying to call FilesystemManager for restoring the filesystem: [{}] : {}", filesystemId, outcome.getBodyExceptionMessage());
                exception.set(outcome);
            }
        }, filesystemId);

        // Check the errors from FilesystemManager call, if any.
        this.checkRestoreException(filesystemId, exception.get());
    }

    @Override
    public FSFileModelPaged callGetFiles(Integer filesystemId, String filterPath, String filename,
                                         Integer numberPage, Integer sizePage, String field, String order)
    {
        LOG.debug("[FilesystemManagerClient] -> [callGetFiles]: trying to get files calling FilesystemManager with params" +
                "filesystemId: [{}], filterPath: [{}], filename: [{}], numberPage: [{}]," +
                " sizePage: [{}], field: [{}], order: [{}]", filesystemId, filterPath, filename,
                numberPage, sizePage, field, order);

        ApiClientResponseWrapper<FSMFileModelPaged, Errors> response = new ApiClientResponseWrapper<>();
        FSFileModel[] fileModels;

        this.restHandlerFilesystemmanagerapi.getFiles(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void getFiles(FSMFileModelPaged outcome)
            {
                LOG.debug("[FilesystemManagerClient] -> [callGetFiles]: Obtained files from filesystem id [{}], path [{}] and filename [{}] successfully.", filesystemId, filterPath, filename);
                response.setValue(outcome);
            }

            @Override
            public void getFilesErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callGetFiles]: Failed getting files from path [{}] and filename [{}]. Error: {}", filterPath, filename, outcome.getBodyExceptionMessage());
                response.setError(outcome);
            }
        }, filesystemId, filterPath, filename, field, numberPage, sizePage, order);

        // Check if there was some exception
        if (response.getError() != null)
        {
            this.checkException(response.getError(),
                    FilesystemsError.getGetFilesError(response.getError().getBodyExceptionMessage().toString()));
        }

        FSFileModelPaged fsFileModelPaged = new FSFileModelPaged();
        fsFileModelPaged.setPage(response.getValue().getPage());
        fsFileModelPaged.setSize(response.getValue().getSize());
        fsFileModelPaged.setNumberElements(response.getValue().getNumberElements());

        // Build the file model if the response was successfully
        fsFileModelPaged.setFileRegistry(this.buildFSFileModel(response.getValue().getFileRegistry()));

        return fsFileModelPaged;
    }

    @Override
    public void callDeleteFile(Integer filesystemId, FSFileLocationModel fsFileLocationModel)
    {
        LOG.debug("[FilesystemManagerClient] -> [callDeleteFile]: calling filesystem manager API for deleting file {} form path {}", fsFileLocationModel.getFilename(), fsFileLocationModel.getPath());

        FSMFileLocationModel fsmFileLocationModel = this.buildLocationModel(fsFileLocationModel);
        SingleApiClientResponseWrapper<Errors> exception = new SingleApiClientResponseWrapper<>();
        this.restHandlerFilesystemmanagerapi.deleteFile(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void deleteFile()
            {
                LOG.debug("[FilesystemManagerClient] -> [callDeleteFile]: deleted file name [{}] successfully.", fsFileLocationModel.getFilename());
            }

            @Override
            public void deleteFileErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callDeleteFile]: Failed trying to delete file name [{}]. Error: {}", fsFileLocationModel.getFilename(), outcome.getBodyExceptionMessage());
                exception.set(outcome);
            }
        }, fsmFileLocationModel, filesystemId);

        if (exception.get() != null)
        {
            this.checkException(exception.get(), FilesystemsError.getDeleteFileError(exception.get().getBodyExceptionMessage().toString()));
        }
    }

    @Override
    public void callCreateDirectory(final String directory, final Integer filesystemId)
    {
        LOG.debug("[FilesystemManagerClient] -> [callCreateDirectory]: calling filesystem manager API to create a directory [{}] in filesystem Id [{}]", directory, filesystemId);

        SingleApiClientResponseWrapper<Errors> exception = new SingleApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.createDirectory(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void createDirectory()
            {
                LOG.debug("[FilesystemManagerClient] -> [callCreateDirectory]: created directory [{}] in filesystemId [{}] successfully.", directory, filesystemId);
            }

            @Override
            public void createDirectoryErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callCreateDirectory]: Failed trying to create a directory [{}]. Error: {}", directory, outcome.getBodyExceptionMessage());
                exception.set(outcome);
            }
        }, filesystemId, directory);

        if (exception.get() != null)
        {
            this.checkException(exception.get(), FilesystemsError.getCreateDirectoryError(exception.get().getBodyExceptionMessage().toString()));
        }
    }

    @Override
    public FSFilesystemUsage callGetFileUse(Integer filesystemId)
    {
        LOG.debug("[FilesystemManagerClient] -> [callGetFileUse]: calling filesystem manager API for get filesystem use with filesystemId [{}]", filesystemId);

        ApiClientResponseWrapper<FSMFilesystemUsage, Errors> response = new ApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.getFilesystemUsage(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void getFilesystemUsage(FSMFilesystemUsage outcome)
            {
                LOG.debug("[FilesystemManagerClient] -> [callGetFileUse]: Obtained filesystem use in filesystemId [{}] successfully", filesystemId);
                response.setValue(outcome);
            }

            @Override
            public void getFilesystemUsageErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callGetFileUse]: Failed trying to get the filesystem use in filesystemId [{}]. Error: {}", filesystemId, outcome.getBodyExceptionMessage());
                response.setError(outcome);
            }
        }, filesystemId);

        if (response.getError() != null)
        {
            this.checkException(response.getError(), FilesystemsError.getGetFileUseError(response.getError().getBodyExceptionMessage().toString()));
        }

        return this.build(response.getValue());
    }

    @Override
    public void callUpdateQuota(Integer filesystemId, String filesystemPackCode)
    {
        LOG.debug("[FilesystemManagerClient] -> [callUpdateQuota]: calling filesystem manager API for updating filesystem quota, id: [{}], packCode: [{}]", filesystemId, filesystemPackCode);

        SingleApiClientResponseWrapper<Errors> exception = new SingleApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.updateFilesystemQuota(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void updateFilesystemQuota()
            {
                LOG.debug("[FilesystemManagerClient] -> [callUpdateQuota]: Updated filesystem quota, filesystem id: [{}], packCode: [{}]", filesystemId, filesystemPackCode);
            }

            @Override
            public void updateFilesystemQuotaErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callUpdateQuota]: Failed trying to update filesystem quota, filesystem id: [{}], packCode: [{}]. Error message: [{}]", filesystemId, filesystemPackCode, outcome.getBodyExceptionMessage());
                exception.set(outcome);
            }
        }, filesystemId, filesystemPackCode);

        if (exception.get() != null)
        {
            this.checkException(exception.get(), FilesystemsError.getUpdateQuotaError(exception.get().getBodyExceptionMessage().toString()));
        }
    }

    @Override
    public void checkFileToDownloadToPreviousEnv(final Integer filesystemId, final FSMFileLocationModel fsmFileLocationModel)
    {
        LOG.debug("[FilesystemManagerClient] -> [checkFileToDownloadToPreviousEnv]: checking file for downloading to Pre from path: [{}] and filesystem Id [{}]", fsmFileLocationModel, filesystemId);

        SingleApiClientResponseWrapper<Errors> exception = new SingleApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.checkFileToDownloadToPreviousEnv(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void checkFileToDownloadToPreviousEnv()
            {
                LOG.debug("[FilesystemManagerClient] -> [checkFileToDownloadToPreviousEnv]: checked file for downloading to Pre from data: [{}], filesystem id: [{}] successfully.", fsmFileLocationModel, filesystemId);
            }

            @Override
            public void checkFileToDownloadToPreviousEnvErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [checkFileToDownloadToPreviousEnv]: Failed trying to check file for downloading to Pre with data: [{}], filesystem id: [{}]. Error message: [{}]",
                        fsmFileLocationModel, filesystemId, outcome.getBodyExceptionMessage());
                exception.set(outcome);
            }
        }, fsmFileLocationModel, filesystemId);

        if (exception.get() != null)
        {
            this.checkException(exception.get(), FilesystemsError.getDownloadToPreviousEnvironmentFileError(exception.get().getBodyExceptionMessage().toString()));
        }
    }

    @Override
    public FilesystemsUsageReportDTO callFilesystemsUsageReport(Integer productId, String environment)
    {
        LOG.debug("[FilesystemManagerClient] -> [callFilesystemsUsageReport]: calling filesystem manager API for get filesystem use with environment [{}], productId [{}]",
                environment, productId);

        ApiClientResponseWrapper<FSMUsageReportDTO, Errors> response = new ApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.getFilesystemsUsageReport(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void getFilesystemsUsageReport(FSMUsageReportDTO outcome)
            {
                LOG.debug("[FilesystemManagerClient] -> [callFilesystemsUsageReport]: Obtained filesystem use in environment [{}], productId [{}] successfully",
                        environment, productId);
                response.setValue(outcome);
            }

            @Override
            public void getFilesystemsUsageReportErrors(Errors outcome)
            {
                LOG.error("[FilesystemManagerClient] -> [callFilesystemsUsageReport]: Failed trying to get the filesystem use in environment [{}], productId [{}]. " +
                        "Error: {}", environment, productId, outcome.getBodyExceptionMessage());
                response.setError(outcome);
            }
        }, productId, environment);

        if (response.getError() != null)
        {
            this.checkException(response.getError(), FilesystemsError.getGetFileUseError(response.getError().getBodyExceptionMessage().toString()));
        }

        return this.buildDTO(response.getValue());
    }

    @Override
    public FSMUsageReportHistorySnapshotDTO[] getFileSystemUsageHistorySnapshot()
    {
        LOG.info("[FilesystemManagerClient] -> [getFileSystemUsageHistorySnapshot]: getting file systems for statistic history loading.");

        SingleApiClientResponseWrapper<FSMUsageReportHistorySnapshotDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerFilesystemmanagerapi.getFileSystemUsageHistorySnapshot(new IRestListenerFilesystemmanagerapi()
        {
            @Override
            public void getFileSystemUsageHistorySnapshot(FSMUsageReportHistorySnapshotDTO[] outcome)
            {
                LOG.info("[FilesystemManagerClient] -> [getFileSystemUsageHistorySnapshot]: got file systems for statistic history loading.");
                response.set(outcome);
            }

            @Override
            public void getFileSystemUsageHistorySnapshotErrors(Errors outcome)
            {
                LOG.error("[[DeploymentManagerClient] -> [getHostsMemoryHistorySnapshot]: Error getting file systems for statistic history loading.: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(FilesystemsError.getUnexpectedError(), outcome);
            }
        });

        return response.get();
    }

    ///////////////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////////////

    /**
     * Build FileSystem MOdel
     *
     * @param fsmFileModels the filesystem model
     * @return a filesystem model instance
     */
    private FSFileModel[] buildFSFileModel(final FSMFileModel[] fsmFileModels)
    {
        String fileModels = Arrays.toString(fsmFileModels);
        LOG.trace("[FilesystemManagerClient] -> [buildFSFileModel]: building FSFileModel[] from FSMFileModel[] [{}]", fileModels);

        FSFileModel[] fsFileModels = new FSFileModel[0];

        if (fsmFileModels != null)
        {
            List<FSFileModel> fsFileModelList = new ArrayList<>();
            for (FSMFileModel fsm : fsmFileModels)
            {
                FSFileModel fs = new FSFileModel();
                BeanUtils.copyProperties(fsm, fs);
                fsFileModelList.add(fs);
            }
            fsFileModels = fsFileModelList.toArray(new FSFileModel[0]);
        }

        return fsFileModels;
    }

    /**
     * Build a location model
     *
     * @param fsFileLocationModel a location model
     * @return a location model instance
     */
    private FSMFileLocationModel buildLocationModel(FSFileLocationModel fsFileLocationModel)
    {
        LOG.debug("[FilesystemManagerClient] -> [buildLocationModel]: building FSMFileLocationModel from FSFileLocationModel");

        FSMFileLocationModel fsmFileLocationModel = new FSMFileLocationModel();
        BeanUtils.copyProperties(fsFileLocationModel, fsmFileLocationModel);

        return fsmFileLocationModel;
    }

    /**
     * Checks if there was any error deleting a {@link Filesystem} from a {@link Product}.
     *
     * @param filesystem The {@link Filesystem}
     * @param errors     {@link Errors}
     */
    private void checkDeleteResponse(final Filesystem filesystem, final Errors errors)
    {
        if (errors != null)
        {
            this.throwFilesystemException(filesystem, errors);
        }
    }

    /**
     * Throws {@link NovaException}.
     *
     * @param filesystem {@link Filesystem}.
     * @param errors     {@link Errors}
     */
    private void throwFilesystemException(final Filesystem filesystem, final Errors errors)
    {
        String errorCode = errors.getFirstErrorMessage().orElse(new ErrorMessage("")).getCode();
        if ("FILESYSTEMMANAGER-008".equals(errorCode))
        {
            String message = MessageFormat.format("[FilesystemManagerClient] -> [throwFilesystemException]: the filesystem is in creating or deleting status. Outcome: ", errors);
            LOG.error(message);
            throw new NovaException(FilesystemsError.getFilesystemNotAvailableDeleteError(), message);
        }
        else if (BUSY_FILE_SYSTEM_ERROR_CODE.equals(errorCode))
        {
            String message = MessageFormat.format("[FilesystemManagerClient] -> [throwFilesystemException]: the filesystem is busy and cannot be deleted. Outcome: ", errors);
            LOG.error(message);
            throw new NovaException(FilesystemsError.getFilesystemBusyError(), message);
        }
        else if (USED_FILE_SYSTEM_DELETION_ERROR_CODE.equals(errorCode))
        {
            String message = MessageFormat.format("[FilesystemManagerClient] -> [throwFilesystemException]: the filesystem has services in use and cannot be deleted. Outcome: ",
                    errors);
            LOG.error(message);
            throw new NovaException(FilesystemsError.getDeletedUsedFilesystemError(), message);
        }
        else if (NOT_EMPTY_EPSILON_ERROR_CODE.equals(errorCode))
        {
            LOG.error("[FilesystemManagerClient] -> [throwFilesystemException]: Impossible to remove the Epsilon filesystem [{}] because it is not empty", filesystem.getName());
            throw new NovaException(FilesystemsError.getEpsilonFSNotEmptyDeletionError(filesystem.getName()));
        }
        else
        {
            // Create a to do task
            String messageException = "[FilesystemManagerClientImpl] -> [throwFilesystemException]: deleteFilesystem/ -> Failed trying to getFilesystemToDelete a filesystem id: [" + filesystem.getId() + "] and " +
                    "filesystem name: [" + filesystem.getName() + "] calling FilesystemManager service. Caused by: [" + errors.getMessage() + "]";

            // Create the to do task, log the error and
            String todoTaskDescription = "[FilesystemManagerClientImpl] -> [throwFilesystemException]: there was an error trying to delete a filesystem with the following parameters:"
                    + LINE_SEPARATOR
                    + " - [Filesystem Name]: " + filesystem.getName()
                    + LINE_SEPARATOR
                    + " - [Description]: " + filesystem.getDescription()
                    + LINE_SEPARATOR
                    + " - [Environment]: " + filesystem.getEnvironment()
                    + LINE_SEPARATOR
                    + " - [Type]: " + filesystem.getType().name()
                    + LINE_SEPARATOR
                    + " - [Filesystem Pack code]: " + filesystem.getFilesystemPack().getCode()
                    + LINE_SEPARATOR
                    + " - [Mensaje de error]: " + errors.getBodyExceptionMessage();

            NovaError novaError = FilesystemsError.getFilesystemDeletionError();
            this.errorTaskMgr.createErrorTaskWithRelatedId(filesystem.getProduct().getId(), novaError, todoTaskDescription, ToDoTaskType.FILESYSTEM_DELETION_ERROR.name(), errors, filesystem.getId(), Constants.IMMUSER);

            // And throw the error.
            throw new NovaException(novaError, messageException);
        }
    }

    /**
     * Translates FilesystemManager API errors to Filesystems API errors,
     * when calling the restoreVolume() function.
     *
     * @param filesystemId {@link Filesystem} ID.
     * @param errors       {@link Errors}.
     */
    private void checkRestoreException(final int filesystemId, final Errors errors)
    {
        // If there was no error, return.
        if (errors != null)
        {
            // Generic failure.
            LOG.error("[FilesystemManagerClient] -> [checkRestoreException]: there was an error trying to restore the filesystem id: [{}]. Error: {}", filesystemId, errors.getFirstErrorMessage());
            throw new NovaException(FilesystemsError.getUnexpectedError(), errors);
        }
    }

    /**
     * Translates FilesystemManager API errors to Filesystems API errors,
     * when calling the archiveVolume() function.
     *
     * @param filesystemId {@link Filesystem} ID.
     * @param errors       {@link Errors}.
     */
    private void checkArchiveException(final int filesystemId, final Errors errors)
    {
        if (errors != null)
        {
            String errorCode = errors.getFirstErrorMessage().orElse(new ErrorMessage("")).getCode();
            // Fail on a validation.
            if ("FILESYSTEMMANAGER-010".equals(errorCode))
            {
                String message = MessageFormat.format("FilesystemAPI: the filesystem is beeing used. Outcome ", errors);
                throw new NovaException(FilesystemsError.getTriedToArchiveUsedFilesystemError(), message);
            }
            else if (BUSY_FILE_SYSTEM_ERROR_CODE.equals(errorCode))
            {
                String message = MessageFormat.format("[FilesystemManagerClient] -> [checkArchiveException]: the filesystem is busy and cannot be archived. Outcome: ", errors);
                LOG.error(message);
                throw new NovaException(FilesystemsError.getFilesystemBusyError(), message);
            }
            // Generic failure.
            else
            {
                LOG.error("[FilesystemManagerClient] -> [checkArchiveException]: there was an error trying to archive the filesystem id: [{}]. Error: ", filesystemId, errors);
                throw new NovaException(FilesystemsError.getUnexpectedError(), errors);
            }
        }
    }

    /**
     * Checks if there was any error creating a {@link Filesystem}.
     *
     * @param product         Product
     * @param createNewVolume New volume
     * @param errors          {@link Errors}
     */
    private void checkCreateResponse(final Product product, final FSMVolumeModel createNewVolume, final Errors errors)
    {
        //Check exception
        if (errors != null)
        {
            String messageException = "[FilesystemManagerClient] -> [checkCreateResponse]: createFilesystem/ -> Failed trying to create a new filesystem with parameters: " + createNewVolume.toString()
                    + ", error calling FilesystemManager service for the product: " + product.getName() + ", with the following error: " + errors.getMessage() + ".";

            // Create the to do task, log the error and
            String todoTaskDescription = "[FilesystemManagerClientImpl] -> [checkCreateResponse]: Impossible to create a new filesystem with the following parameters:"
                    + LINE_SEPARATOR
                    + " - [Filesystem Name]: " + createNewVolume.getFilesystemName()
                    + LINE_SEPARATOR
                    + " - [Description]: " + createNewVolume.getFilesystemDescription()
                    + LINE_SEPARATOR
                    + " - [Environment]: " + createNewVolume.getEnvironment()
                    + LINE_SEPARATOR
                    + " - [Type]: " + createNewVolume.getFilesystemType()
                    + LINE_SEPARATOR
                    + " - [Filesystem Pack code]: " + createNewVolume.getFilesystemPackCode()
                    + LINE_SEPARATOR
                    + " - [Mensaje de error]: " + errors.getBodyExceptionMessage();

            NovaError novaError = FilesystemsError.getFilesystemCreationError();
            this.errorTaskMgr.createErrorTask(product.getId(), novaError, todoTaskDescription, ToDoTaskType.FILESYSTEM_CREATION_ERROR, errors);
            throw new NovaException(novaError, messageException);
        }
    }

    /**
     * Checks if there was any error updating the configuration of a {@link Filesystem}.
     *
     * @param filesystem    the filesystem
     * @param configuration the configuration
     * @param errors        {@link Errors}
     */
    private void checkUpdateConfigurationResponse(final Filesystem filesystem, final FSMVolumeConfiguration[] configuration, final Errors errors,  final String ivUser)
    {
        //Check exception
        if (errors != null)
        {
            String messageException = "[FilesystemManagerClient] -> [checkUpdateConfigurationResponse]: updateFilesystemConfiguration/ -> Failed trying to update the configuration of a filesystem with parameters: " + configuration
                    + ", error calling FilesystemManager service for the product: " + filesystem.getProduct().getName() + ", with the following error: " + errors.getMessage() + ".";

            // Create the to do task, log the error and
            String todoTaskDescription = "[FilesystemManagerClientImpl] -> [checkUpdateConfigurationResponse]: Impossible to update the configuration of a filesystem with the following parameters:"
                    + LINE_SEPARATOR
                    + " - [Filesystem Name]: " + filesystem.getName()
                    + LINE_SEPARATOR
                    + " - [Environment]: " + filesystem.getEnvironment()
                    + LINE_SEPARATOR
                    + " - [Type]: " + filesystem.getType()
                    + LINE_SEPARATOR
                    + " - [Configuration]: " + configuration
                    + LINE_SEPARATOR
                    + " - [Error message]: " + errors.getBodyExceptionMessage();

            final NovaError novaError = FilesystemsError.getFilesystemUpdateConfigurationError();
            this.errorTaskMgr.createErrorTaskWithRelatedId(filesystem.getProduct().getId(), novaError, todoTaskDescription, ToDoTaskType.FILESYSTEM_UPDATE_ERROR.name(), novaError.getErrors(), filesystem.getId(), ivUser);
            throw new NovaException(novaError, messageException);
        }
    }

    /**
     * Creates a new {@link FSMVolumeModel}.
     *
     * @param createNewFilesystemDto DTO
     * @param product                Product
     * @return New volume.
     */
    private FSMVolumeModel buildFSMVolumeModel(final CreateNewFilesystemDto createNewFilesystemDto, final Product product)
    {
        // Create a new volume for the new filesystem
        FSMVolumeModel createNewVolume = new FSMVolumeModel();

        createNewVolume.setFilesystemPackCode(createNewFilesystemDto.getFilesystemPackCode());
        createNewVolume.setProductId(product.getId());
        createNewVolume.setEnvironment(createNewFilesystemDto.getEnvironment());
        createNewVolume.setNfs(DEFAULT_NFS_VALUE);
        createNewVolume.setFilesystemDescription(createNewFilesystemDto.getFilesystemDescription());
        createNewVolume.setFilesystemName(createNewFilesystemDto.getFilesystemName());
        createNewVolume.setFilesystemType(createNewFilesystemDto.getFilesystemType());

        if (FilesystemType.FILESYSTEM_EPSILON_ETHER.name().equals(createNewFilesystemDto.getFilesystemType()))
        {
            // copy the properties
            createNewVolume.setConfiguration(Arrays.stream(Optional.ofNullable(createNewFilesystemDto.getConfiguration()).orElse(new FilesystemConfigurationDto[0]))
                    .map(FilesystemManagerClientImpl::buildFSMConfiguration)
                    .toArray(FSMVolumeConfiguration[]::new));
        }

        return createNewVolume;
    }

    /**
     * Translates FilesystemManager API errors to Filesystems API errors,
     * when calling functions.
     *
     * @param errors exception
     */
    private void checkException(final Errors errors, final NovaError novaError)
    {
        String errorCode = errors.getFirstErrorMessage().orElse(new ErrorMessage("")).getCode();
        // Fail on a validation.
        if ("FILESYSTEMMANAGER-017".equals(errorCode))
        {
            throw new NovaException(FilesystemsError.getActionPermissionError(errors.getBodyExceptionMessage().toString()));
        }
        else if (BUSY_FILE_SYSTEM_ERROR_CODE.equals(errorCode))
        {
            throw new NovaException(FilesystemsError.getFilesystemBusyError(), errors);
        }
        else if ("FILESYSTEMMANAGER-022".equals(errorCode))
        {
            throw new NovaException(FilesystemsError.getNotAllowedFileNamePatternError(), errors);
        }
        // Generic failure.
        else
        {
            LOG.error("[FilesystemManagerClient] -> [checkException]: there was an error in the filesystem. FilesystemError Code: [{}]. Error exception: ", novaError, errors);
            throw new NovaException(novaError, errors);
        }
    }

    /**
     * Get a FSFilesystemUsage from FSMFilesystemUsage
     *
     * @param fsmFilesystemUsage FSMFilesystemUsage
     * @return FSFilesystemUsage
     */
    private FSFilesystemUsage build(FSMFilesystemUsage fsmFilesystemUsage)
    {
        FSFilesystemUsage fsFilesystemUsage = new FSFilesystemUsage();
        BeanUtils.copyProperties(fsmFilesystemUsage, fsFilesystemUsage);
        return fsFilesystemUsage;
    }

    /**
     * Get a FilesystemsUsageReportDTO from FSMUsageReportDTO
     *
     * @param fsmUsageReportDTO FSMUsageReportDTO
     * @return FilesystemsUsageReportDTO
     */
    private FilesystemsUsageReportDTO buildDTO(FSMUsageReportDTO fsmUsageReportDTO)
    {
        FilesystemsUsageReportDTO fsFilesystemUsage = new FilesystemsUsageReportDTO();
        BeanUtils.copyProperties(fsmUsageReportDTO, fsFilesystemUsage);
        return fsFilesystemUsage;
    }

    private static FSMVolumeConfiguration buildFSMConfiguration(final FilesystemConfigurationDto configurationDto)
    {
        final FSMVolumeConfiguration configuration = new FSMVolumeConfiguration();
        configuration.setKey(configurationDto.getKey());
        configuration.setValue(configurationDto.getValue());
        return configuration;
    }
}
