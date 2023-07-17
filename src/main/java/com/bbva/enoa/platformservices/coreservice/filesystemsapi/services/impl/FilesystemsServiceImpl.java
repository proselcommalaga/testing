package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.*;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FTATransferLocationInfo;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FTATransferRelatedInfoAdminDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEpsilonEther;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEther;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
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
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsService;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsValidator;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils.PERMISSION_DENIED;

/**
 * Filesystem service to update, getFilesystemToDelete, create and such operations.
 * NOTE: Set many methods @Transactional (readOnly = true) due to Postgres need to recover the Product to avoid the lob stream exception
 */
@Service
public class FilesystemsServiceImpl implements IFilesystemsService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemsServiceImpl.class);
    /**
     * Unauthorized exception
     */
    static final NovaException PERMISSIONS_EXCEPTION = new NovaException(FilesystemsError.getForbiddenError(), FilesystemsError.getForbiddenError().toString());

    /**
     * Filesystem validator
     */
    private final IFilesystemsValidator filesystemsValidator;

    /**
     * Filesystem Builder
     */
    private final IFilesystemsBuilder filesystemsBuilder;
    /**
     * Filesystem Manager Client
     */
    private final IFilesystemManagerClient filesystemManagerClient;
    /**
     * Filesystem repository
     */
    private final FilesystemRepository filesystemRepository;
    /**
     * User service client
     */
    private final IProductUsersClient usersClient;
    /**
     * DeploymentChange repository
     */
    private final DeploymentChangeRepository deploymentChangeRepository;
    /**
     * Filesystem task repository
     */
    private final FilesystemTaskRepository filesystemTaskRepository;
    /**
     * To do task repository
     */
    private final ToDoTaskRepository toDoTaskRepository;
    /**
     * Mail Service
     */
    private final MailServiceClient mailServiceClient;
    /**
     * Ether service
     */
    private final IEtherService etherService;

    /**
     * Filetrasnfer service as admihn
     */
    private final IFileTransferAdminClient fileTransferAdminClient;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Constructor
     *
     * @param filesystemsValidator       filesystemsValidator
     * @param filesystemsBuilder         filesystemsBuilder
     * @param filesystemManagerClient    filesystemManagerClient
     * @param usersClient                usersClient
     * @param filesystemRepository       filesystemRepository
     * @param deploymentChangeRepository deploymentChangeRepository
     * @param etherService               Service of EtherManager
     * @param mailServiceClient          mail service client
     * @param filesystemTaskRepository   filesystemTaskRepository
     * @param toDoTaskRepository         toDoTaskRepository
     * @param fileTransferAdminClient    file transfer admin client
     * @param novaActivityEmitter        NovaActivity emitter
     */
    @Autowired
    public FilesystemsServiceImpl(final IFilesystemsValidator filesystemsValidator, final IFilesystemsBuilder filesystemsBuilder, final IFilesystemManagerClient filesystemManagerClient,
                                  final IProductUsersClient usersClient, final FilesystemRepository filesystemRepository, final DeploymentChangeRepository deploymentChangeRepository,
                                  final IEtherService etherService, final MailServiceClient mailServiceClient,
                                  final FilesystemTaskRepository filesystemTaskRepository, final ToDoTaskRepository toDoTaskRepository,
                                  IFileTransferAdminClient fileTransferAdminClient, final INovaActivityEmitter novaActivityEmitter)
    {
        this.filesystemsValidator = filesystemsValidator;
        this.filesystemsBuilder = filesystemsBuilder;
        this.filesystemManagerClient = filesystemManagerClient;
        this.usersClient = usersClient;
        this.filesystemRepository = filesystemRepository;
        this.deploymentChangeRepository = deploymentChangeRepository;
        this.etherService = etherService;
        this.mailServiceClient = mailServiceClient;
        this.toDoTaskRepository = toDoTaskRepository;
        this.filesystemTaskRepository = filesystemTaskRepository;
        this.fileTransferAdminClient = fileTransferAdminClient;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    //////////////////////////////////////// IMPLEMENTATIONS /////////////////////////////////////////////////////

    @Override
    public FilesystemDto getFilesystem(final Integer filesystemId) throws NovaException
    {
        // Validate and Get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check if the filesystem is available
        this.filesystemsValidator.checkIfFilesystemIsAvailable(filesystem);

        // Build a filesystem DTO from filesystem
        final FilesystemDto filesystemDto = this.filesystemsBuilder.buildFilesystemDTO(filesystem);
        LOG.trace("[FilesystemsAPI] -> [getFilesystem]: obtained filesystem DTO: [{}] from filesystemId [{}]", filesystemDto, filesystemId);

        return filesystemDto;
    }

    @Transactional
    @Override
    public void deleteFilesystem(final String ivUser, final Integer filesystemId) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [deleteFilesystem]: deleting filesystem with id: [{}] for ivUser: [{}]", filesystemId, ivUser);

        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.REMOVE_FILESYSTEM_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // Validate if filesystem can be managed by user
        this.filesystemsValidator.checkIfFilesystemIsFrozen(filesystem);

        // Validate whether the filesystem is Not being used for any DeploymentPlan, Broker or Filesystem
        this.filesystemsValidator.validateFilesystemNotInUse(filesystem);

        // Validate Namespace
        doCheckEtherConfigurationStatus(filesystem.getProduct().getId(), filesystem.getEnvironment(), filesystem.getType());

        // Call filesystem Manager to delete the volume of the filesystem
        this.filesystemManagerClient.callDeleteFilesystemManager(filesystem);

        // Reject the filesystem task associated to the filesystem
        this.rejectPendingFilesystemTask(filesystemId, FilesystemStatus.DELETED.name());

        // Emit Delete Filesystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM, ActivityAction.ELIMINATED)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("filesystemName", filesystem.getName())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("filesystemType", filesystem.getType())
                .build());

        LOG.debug("[FilesystemsAPI] -> [deleteFilesystem]: deleted filesystem with id: [{}] for ivUser: [{}]", filesystemId, ivUser);
    }

    @Transactional(readOnly = true)
    @Override
    public void createFilesystem(final CreateNewFilesystemDto filesystemToAdd, final String ivUser, final Integer productId) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [createFilesystem]: creating a new filesystem with parameters: [{}] for productId [{}] ivUser: [{}]", filesystemToAdd, productId, ivUser);

        // Validate and get the product
        Product product = this.filesystemsValidator.validateAndGetProduct(productId);

        // Check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.CREATE_FILESYSTEM_PERMISSION, filesystemToAdd.getEnvironment(), product.getId(), PERMISSIONS_EXCEPTION);

        // Validate Namespace
        doCheckEtherConfigurationStatus(product.getId(), filesystemToAdd.getEnvironment(), FilesystemType.valueOf(filesystemToAdd.getFilesystemType()));

        // Validations if the filesystem can be created
        this.filesystemsValidator.validateFilesystemCreation(product, filesystemToAdd);

        // Call Filesystem Manager API - Generate an asynchronous call.
        this.filesystemManagerClient.callCreateFilesystemManager(filesystemToAdd, product);

        LOG.debug("[FilesystemsAPI] -> [createFilesystem]: created the new filesystem with parameters: [{}] for productId [{}] ivUser: [{}]", filesystemToAdd, productId, ivUser);
    }

    @Transactional(readOnly = true)
    @Override
    public void updateFilesystemPlatformConfiguration(final FilesystemConfigurationDto[] configuration, final Integer filesystemId, final String ivUser) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [updateFilesystemPlatformConfiguration]: updating a filesystem platform configuration with parameters: [{}] for filesystemId [{}] ivUser: [{}]", configuration, filesystemId, ivUser);

        // validate and get the filesystem
        final Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.UPDATE_PLAT_FILESYSTEM_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // update the configuration
        this.updateFilesystemConfiguration(configuration, filesystem, ivUser);

        LOG.debug("[FilesystemsAPI] -> [updateFilesystemPlatformConfiguration]: Updated the filesystem platform configuration with parameters: [{}] for productId [{}] ivUser: [{}]", configuration, filesystem.getProduct().getId(), ivUser);
    }

    @Transactional(readOnly = true)
    @Override
    public void updateFilesystemConfiguration(final FilesystemConfigurationDto[] configuration, final Integer filesystemId, final String ivUser) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [updateFilesystemConfiguration]: updating a filesystem configuration with parameters: [{}] for filesystemId [{}] ivUser: [{}]", configuration, filesystemId, ivUser);

        // validate and get the filesystem
        final Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.UPDATE_FILESYSTEM_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // update the configuration
        this.updateFilesystemConfiguration(configuration, filesystem, ivUser);

        LOG.debug("[FilesystemsAPI] -> [updateFilesystemConfiguration]: Updated the filesystem configuration with parameters: [{}] for productId [{}] ivUser: [{}]", configuration, filesystem.getProduct().getId(), ivUser);
    }

    @Transactional
    @Override
    public void archiveFilesystem(final String ivUser, final Integer filesystemId) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [archiveFilesystem]: archiving a filesystem with id: [{}] ivUser: [{}]", filesystemId, ivUser);

        // Validate and get the filesystem
        final Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.ARCHIVE_FILESYSTEM_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // validate the type of filesystem
        this.filesystemsValidator.validateOperationFilesystemObjects(filesystem, "archive");

        // Validate if filesystem can be managed by user
        this.filesystemsValidator.checkIfFilesystemIsFrozen(filesystem);

        // Validate whether the filesystem is Not being used for any DeploymentPlan
        this.filesystemsValidator.validateFilesystemNotInUse(filesystem);

        // Archive the filesystem.
        this.filesystemManagerClient.callArchiveVolume(filesystemId);

        // Reject the filesystem task associated to the filesystem
        this.rejectPendingFilesystemTask(filesystemId, FilesystemStatus.ARCHIVED.name());

        // Emit Archive Filesystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM, ActivityAction.ARCHIVED)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("filesystemName", filesystem.getName())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("filesystemType", filesystem.getType())
                .build());

        LOG.debug("[FilesystemsAPI] -> [archiveFilesystem]: archived the filesystem with id: [{}] ivUser: [{}]", filesystemId, ivUser);
    }

    @Override
    @Transactional(readOnly = true)
    public void downloadToPre(String ivUser, Integer filesystemId, String filename, String filesystemPath) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [downloadToPre]: trying to download a file for filesystem id: [{}] of the path: [{}] and filename: [{}]. ", filesystemId, filesystemPath, filename);

        // Validate and get the filesystem
        final Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check environment, due to the request to PRE just can be done it on PRO environment
        if (!Environment.PRO.getEnvironment().equals(filesystem.getEnvironment()))
        {
            throw new NovaException(FilesystemsError.getNotEnvironmentToDownload(filesystemId, filesystem.getEnvironment()));
        }

        // check the permission
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.DOWNLOAD_FILES_PRO_TO_PRE_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSION_DENIED);

        // Check the existence of the same filesystem name on the preproduction environment
        Product product = filesystem.getProduct();
        if (filesystemRepository.productHasFilesystemWithSameNameOnEnvironment(product.getId(), filesystem.getName(), Environment.PRE.getEnvironment()))
        {
            MailNotificationParams params = (new MailNotificationParams.MailNotificationParamsBuilder())
                    .productName(product.getName()).productId(product.getId()).environment(filesystem.getEnvironment())
                    .ivUser(buildFullUserName(ivUser)).userMailAddress(getUserMailAddress(ivUser)).filesystemPath(filesystemPath)
                    .filename(filename).filesystemName(filesystem.getName()).build();
            // Send mail notification
            this.mailServiceClient.sendDownloadToPreNotification(params);

            FSMFileLocationModel fsmFileLocationModel = new FSMFileLocationModel();
            fsmFileLocationModel.setPath(filesystemPath);
            fsmFileLocationModel.setFilename(filename);

            this.filesystemManagerClient.checkFileToDownloadToPreviousEnv(filesystemId, fsmFileLocationModel);
        }
        else
        {
            throw new NovaException(FilesystemsError.getDuplicatedFilesystemNotFoundError(product.getId(), Environment.PRE.name(), filesystem.getName()));
        }

        // Emit File Download pre request Filesystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM, ActivityAction.FILE_DOWNLOAD_PRE_REQUESTED)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("filesystemName", filesystem.getName())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("filesystemType", filesystem.getType())
                .addParam("filename", filename)
                .addParam("filesystemPath", filesystemPath)
                .build());

        LOG.debug("[FilesystemsAPI] -> [downloadToPre]: request for download to pre successfully done for filesystem id: [{}] of the path: [{}] and filename: [{}]. ", filesystemId, filesystemPath, filename);
    }

    @Transactional(readOnly = true)
    @Override
    public void restoreFilesystem(final String ivUser, final Integer filesystemId) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [restoreFilesystem]: restoring a filesystem with id: [{}] ivUser: [{}]", filesystemId, ivUser);

        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.RESTORE_FILESYSTEM_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // validate the type of filesystem
        this.filesystemsValidator.validateOperationFilesystemObjects(filesystem, "restore");

        // Validate the restore
        this.filesystemsValidator.validateRestoreFilesystem(filesystem);

        // Call Filesystem Manager API - Generate an asynchronous call.
        this.filesystemManagerClient.callRestoreVolume(filesystemId);

        // Emit Reset Filesystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM, ActivityAction.RESET)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("filesystemName", filesystem.getName())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("filesystemType", filesystem.getType())
                .build());

        LOG.debug("[FilesystemsAPI] -> [restoreFilesystem]: restored a filesystem with id: [{}] ivUser: [{}]", filesystemId, ivUser);
    }

    @Override
    public FilesystemDto[] getProductFilesystems(final Integer productId, final String environment, final String filesystemType) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [getProductFilesystems]: getting a filesystems of the product id: [{}] for environment: [{}]", productId, environment);

        // Validate and get the product
        this.filesystemsValidator.validateAndGetProduct(productId);

        // Get the filesystems.
        List<Filesystem> filesystems;

        if (!StringUtils.isEmpty(filesystemType))
        {
            FilesystemType filesystemTypeEnum = FilesystemType.valueOf(filesystemType);
            Class<? extends Filesystem> filesystemTypeClass = null;

            switch (filesystemTypeEnum)
            {
                case FILESYSTEM:
                    filesystemTypeClass = FilesystemNova.class;
                    break;
                case FILESYSTEM_ETHER:
                    filesystemTypeClass = FilesystemEther.class;
                    break;
                case FILESYSTEM_EPSILON_ETHER:
                    filesystemTypeClass = FilesystemEpsilonEther.class;
                    break;
            }

            if (!StringUtils.isEmpty(environment))
            {
                filesystems = this.filesystemRepository.findByProductIdAndEnvironmentAndFilesystemTypeOrderByCreationDateDesc(productId, environment, filesystemTypeClass);
            }
            else
            {
                filesystems = this.filesystemRepository.findByProductIdAndFilesystemTypeOrderByCreationDateDesc(productId, filesystemTypeClass);
            }
        }
        else if (!StringUtils.isEmpty(environment))
        {
            // Get only the filesystems from that environment.
            filesystems = this.filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(productId, environment);
        }
        else
        {
            filesystems = this.filesystemRepository.findByProductIdOrderByCreationDateDesc(productId);
        }

        // Convert filesystem list to filesystemDTO array
        return this.filesystemsBuilder.buildFilesystemDTOArray(filesystems);
    }

    @Transactional
    @Override
    public void deleteFile(final FSFileLocationModel data, final String ivUser, final Integer filesystemId) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [deleteFile]: deleteFile with filesystem id: [{}], path: [{}] and filename: [{}]", filesystemId, data.getPath(), data.getFilename());
        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check if directory is reserved or inside a reserved one
        this.filesystemsValidator.validateReservedDirectories(filesystem, data);

        // Check if the user has permission
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.FILESYSTEM_DELETE_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // Validate if filesystem can be managed by user
        this.filesystemsValidator.checkIfFilesystemIsFrozen(filesystem);

        // Call Filesystem Manager client
        this.filesystemManagerClient.callDeleteFile(filesystemId, data);

        // Add new entry to plan history
        this.addHistoryEntry(ivUser, filesystem, ChangeType.DELETE_FILE_FILESYSTEM, "El fichero con nombre: [" + data.getFilename() + "] " +
                "ha sido eliminado del Sistema de almacenamiento: [" + filesystem.getName() + "]-[" + filesystem.getEnvironment() + "] satisfactoriamente");
    }

    @Override
    public Boolean isFilesystemFrozen(final Integer filesystemId) throws NovaException
    {
        // Validate the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        Boolean isFrozen = this.filesystemsValidator.isFilesystemFrozen(filesystem);
        LOG.debug("[FilesystemsAPI] -> [isFilesystemFrozen]: the filesystem: [{}] is frozen status: [{}].", filesystem.getName(), isFrozen);

        return isFrozen;
    }

    @Transactional
    @Override
    public void createDirectory(final String ivUser, final Integer filesystemId, final String newDirectoryPath) throws NovaException
    {
        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check if new directory is reserved or inside a reserved one
        this.filesystemsValidator.validateNewDirectoryInsideReservedDirectory(filesystem, newDirectoryPath);

        // validate if the filesystem stores objects or files
        this.filesystemsValidator.validateOperationFilesystemObjects(filesystem, "crate a directory");

        // Check if the user has permission
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.FILESYSTEM_CREATE_DIR_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // Validate if filesystem can be managed by user
        this.filesystemsValidator.checkIfFilesystemIsFrozen(filesystem);

        // Call Filesystem Manager client
        this.filesystemManagerClient.callCreateDirectory(this.filesystemsValidator.validateDirectory(newDirectoryPath), filesystemId);

        // Add new entry to plan history
        this.addHistoryEntry(ivUser, filesystem, ChangeType.CREATE_DIRECTORY_FILESYSTEM, "Se ha creado el directorio con ruta: [" + newDirectoryPath + "] " +
                "en el Sistema de almacenamiento: [" + filesystem.getName() + "]-[" + filesystem.getEnvironment() + "] satisfactoriamente");

        // Emit Folder Create Filesystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM, ActivityAction.FOLDER_CREATED)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("filesystemName", filesystem.getName())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("filesystemType", filesystem.getType())
                .addParam("folder", newDirectoryPath)
                .build());
    }

    @Override
    public FSFilesystemUsage getFilesystemUsage(final Integer filesystemId) throws NovaException
    {
        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Call Filesystem Manager client
        FSFilesystemUsage fsFilesystemUsage = this.filesystemManagerClient.callGetFileUse(filesystemId);
        LOG.debug("[FilesystemsAPI] -> [getFilesystemUsage]: obtained a filesystem usage of the 0 filesystem: [{}]. Usage: [{}]", filesystem.getName(), fsFilesystemUsage);

        return fsFilesystemUsage;
    }

    @Override
    public FSFileModelPaged getFiles(final Integer filesystemId, final String filterPath, final String filename,
                                     Integer numberPage, Integer sizePage, String field, String order) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [getFiles]: obtained files from filesystem: [{}], in filterPaht: [{}] with name: [{}] " +
                        ", params number page: [{}], size page: [{}], field order: [{}] and order [{}],", filesystemId, filename,
                filterPath, numberPage, sizePage, field, order);

        if (sizePage < 1 || sizePage > Constants.MAX_SIZE_PAGE)
        {
            throw new NovaException(FilesystemsError.getNumberPageError(sizePage));
        }
        if (numberPage < 1)
        {
            throw new NovaException(FilesystemsError.getSizePageError(numberPage));
        }


        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Call Filesystem Manager client
        FSFileModelPaged fsFileModelArray = this.filesystemManagerClient.callGetFiles(filesystemId, filterPath, filename,
                numberPage, sizePage, field, order);

        LOG.trace("[FilesystemsAPI] -> [getFiles]: obtained a file from filesystem: [{}] with name: [{}] and filter: [{}]", filesystem.getName(), filename, filterPath);

        return fsFileModelArray;
    }

    @Override
    public FilesystemTypeDto[] getAvailableFilesystemTypes(final Integer productId, final String environment) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [getAvailableFilesystemTypes]: getting available filesystem types for the product id: [{}] in environment: [{}]", productId, environment);

        // Validate and get the product
        final Product product = this.filesystemsValidator.validateAndGetProduct(productId);

        final Map<Environment, List<Platform>> desirableAvailableFilesystemTypes = new HashMap<>();

        if (StringUtils.isEmpty(environment))
        {
            desirableAvailableFilesystemTypes.put(Environment.INT, product.getDeployPlatformsAvailableByEnv(Environment.INT.getEnvironment()));
            desirableAvailableFilesystemTypes.put(Environment.PRE, product.getDeployPlatformsAvailableByEnv(Environment.PRE.getEnvironment()));
            desirableAvailableFilesystemTypes.put(Environment.PRO, product.getDeployPlatformsAvailableByEnv(Environment.PRO.getEnvironment()));
        }
        else
        {
            final Environment env = Environment.valueOf(environment);

            switch (env)
            {
                case INT:
                    desirableAvailableFilesystemTypes.put(Environment.INT, product.getDeployPlatformsAvailableByEnv(env.getEnvironment()));
                    break;
                case PRE:
                    desirableAvailableFilesystemTypes.put(Environment.PRE, product.getDeployPlatformsAvailableByEnv(env.getEnvironment()));
                    break;
                case PRO:
                    desirableAvailableFilesystemTypes.put(Environment.PRO, product.getDeployPlatformsAvailableByEnv(env.getEnvironment()));
                    break;
            }
        }

        return getAvailableFilesystemTypes(desirableAvailableFilesystemTypes)
                .toArray(FilesystemTypeDto[]::new);
    }

    @Override
    @Transactional
    public void updateFilesystemQuota(Integer filesystemId, String filesystemPackCode, String ivUser)
    {
        LOG.debug("[FilesystemsAPI] -> [updateFilesystemQuota]: Updating Filesystem id: [{}] quota to packCode: [{}],  ivUser: [{}]", filesystemId, filesystemPackCode, ivUser);

        // Validate and get the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check user permissions
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.FILESYSTEM_MODIFY_QUOTA, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);

        // Validate and get Filesystem pack
        FilesystemPack newFilesystemPack = this.filesystemsValidator.validatePackToUpdate(filesystem, filesystemPackCode);

        // Check if filesystem is available
        this.filesystemsValidator.checkIfFilesystemIsAvailable(filesystem);

        // Check if filesystem is NOVA type
        this.filesystemsValidator.validateFilesystemIsNovaType(filesystem.getType());

        // Check if filesystem is frozen
        this.filesystemsValidator.checkIfFilesystemIsFrozen(filesystem);

        // If new quota is different, then call filesystem manager api
        if (!filesystem.getFilesystemPack().equals(newFilesystemPack))
        {
            // Check if there is enough storage
            this.filesystemsValidator.validateFilesystemStorage(filesystem, newFilesystemPack);

            // Check budgets
            this.filesystemsValidator.checkFilesystemBudget(filesystem, newFilesystemPack);

            // Call Filesystem Manager API.
            this.filesystemManagerClient.callUpdateQuota(filesystemId, filesystemPackCode);

            //Add entry to history
            this.addHistoryEntry(ivUser, filesystem, ChangeType.MODIFY_QUOTA_FILESYSTEM, "Se ha modificado la cuota del sistema de almacenamiento: [" + filesystem.getName() + "]-[" + filesystem.getEnvironment() +
                    "]. Ha sido actualizado de: [" + filesystem.getFilesystemPack().getSizeMB() + "]MB a: [" + newFilesystemPack.getSizeMB() + "]MB de nueva capacidad.");
        }

        // Emit Edit Filesystem Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM, ActivityAction.EDITED)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("filesystemName", filesystem.getName())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("filesystemType", filesystem.getType())
                .addParam("changeType", "updateFilesystemQuota")
                .addParam("lastQuota", filesystem.getFilesystemPack().getSizeMB())
                .addParam("newQuota", newFilesystemPack.getSizeMB())
                .build());

        LOG.debug("[FilesystemsAPI] -> [updateFilesystemQuota]: Updated quota of Filesystem id: [{}] from [{}]MB to [{}]MB,  ivUser: [{}]", filesystemId, filesystem.getFilesystemPack().getSizeMB(), newFilesystemPack.getSizeMB(), ivUser);
    }

    @Override
    public Integer getFilesystemId(String path, String environment) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [getFilesystemId]: Getting filesystem id with landing_zone_path: [{}] and environment: [{}]", path, environment);
        Environment env = Environment.valueOf(environment);
        Integer filesystemId = this.filesystemRepository.filesystemId(path, env.getEnvironment());

        if (filesystemId == null)
        {
            throw new NovaException(FilesystemsError.getFilesystemIdError(path, environment));
        }

        LOG.debug("[FilesystemsAPI] -> [getFilesystemId]: get filesystem id [{}] success", filesystemId);

        return filesystemId;
    }

    @Override
    public String[] getFilesystemsStatuses()
    {
        return Arrays.stream(FilesystemStatus.values()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    public String[] getFilesystemsTypes()
    {
        return Arrays.stream(FilesystemType.values()).map(Enum::toString).toArray(String[]::new);
    }

    @Override
    public FilesystemsUsageReportDTO getFilesystemsUsageReport(Integer productId, String environment)
    {
        // Call Filesystem Manager client
        FilesystemsUsageReportDTO filesystemsUsageReportDTO = this.filesystemManagerClient.callFilesystemsUsageReport(productId, environment);
        LOG.debug("[FilesystemsAPI] -> [getFilesystemsUsageReport]: obtained a filesystem usage of the environment: [{}], productId: [{}]. Usage: [{}]",
                environment, productId, filesystemsUsageReportDTO);

        return filesystemsUsageReportDTO;
    }

    @Override
    @Transactional
    public FFilesystemRelatedTransferInfoDTO[] getFilesystemsTransferInformation(Integer productId, Integer filesystemId)
    {

        // 1. Call filesystem manager to obtain information from environment and productId for the filesystemId. In case not found, throw exception
        LOG.debug("[{}] -> [getFilesystemsTransferInformation]: Obtaining filesystem information for the id [{}].", this.getClass().getSimpleName(), filesystemId);
        Filesystem fsRequested = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // 2. Call the filetransfer manager to obtain the information about the online transfers for product and environment
        List<FTATransferRelatedInfoAdminDTO> ftaTransferRelatedInfoAdminDTOS =
                Arrays.asList(this.fileTransferAdminClient.getFileTransfersInformation(fsRequested.getEnvironment(), fsRequested.getProduct().getUuaa(), fsRequested.getName()));

        LOG.debug("[{}] -> [getFilesystemsTransferInformation]: Filtering online transfer list by name [{}] and environment [{}].",
                this.getClass().getSimpleName(), fsRequested.getName(), fsRequested.getEnvironment());

        // 3. Build the DTO to return for every filesystem. Filtered by same name and in the requested environment.

        LOG.debug("[{}] -> [getFilesystemsTransferInformation]: Filtered list received from file transfer admin:[{}].", this.getClass().getSimpleName(), ftaTransferRelatedInfoAdminDTOS);

        // 4. Return the list to print in frontend service
        return this.convertIntoRelatedTransferInfoDTO(ftaTransferRelatedInfoAdminDTOS);

    }


    /////////////////////////////// PRIVATE METHODS //////////////////////////////////////////////

    /**
     * Add values to Map from Platform list
     * @param desirableAvailableFilesystemTypes Map to add platforms availables
     * @param env environment
     * @param platformList platform availables
     */
    private void addPlatformListToMap(Map<Environment, Platform> desirableAvailableFilesystemTypes, Environment env, List<Platform> platformList)
    {
        platformList.forEach(platform -> desirableAvailableFilesystemTypes.put(env, platform));
    }


    private void doCheckEtherConfigurationStatus(final Integer productId, final String pEnvironment, final FilesystemType type)
    {
        if (type.equals(FilesystemType.FILESYSTEM_ETHER) || type.equals(FilesystemType.FILESYSTEM_EPSILON_ETHER))
        {
            LOG.debug("[FilesystemsAPI] -> [doCheckEtherConfigurationStatus]: Checking configuration Ether Status for"
                    + " productId {} and environment {}", productId, pEnvironment);
            final boolean readyToDeploy = etherService.isReadyToDeploy(productId, pEnvironment);

            if (!readyToDeploy)
            {
                throw new NovaException(FilesystemsError.getCreatingOrDeletingFileSystemOnNonConfiguredPlatformError());
            }
        }
        else
        {
            LOG.debug("[FilesystemsAPI] -> [doCheckEtherConfigurationStatus]: No need to check Ether configurations, productId [{}]", productId);
        }
    }

    /**
     * Add new history plan entry
     *
     * @param ivUser     BBVA user code
     * @param filesystem filesystem
     * @param type       type
     * @param message    message
     */
    void addHistoryEntry(String ivUser, final Filesystem filesystem, final ChangeType type, final String message)
    {
        // Add new entry to plan history
        List<DeploymentPlan> deploymentPlanList = this.filesystemRepository.filesystemUsedOnDeployedPlan(filesystem.getId());

        for (DeploymentPlan deploymentPlan : deploymentPlanList)
        {
            DeploymentChange change = new DeploymentChange(deploymentPlan, type, message);
            change.setUserCode(ivUser);
            deploymentPlan.getChanges().add(change);
            this.deploymentChangeRepository.saveAndFlush(change);
        }
    }

    /**
     * Gets a list of {@link FilesystemTypeDto} available for a product
     *
     * @param deploymentConfig Deployment configuration ({@link Platform}) from which infer the list of available filesystem types.
     *                         The available filesystem types depend on the enabled platforms for deployment
     * @return List of {@link FilesystemTypeDto}
     */
    List<FilesystemTypeDto> getAvailableFilesystemTypes(final Map<Environment, List<Platform>> deploymentConfig)
    {
        final List<FilesystemTypeDto> availableFilesystemTypes = new ArrayList<>();

        deploymentConfig.forEach((env, destinationPlatformDeployTypeList) -> {
            destinationPlatformDeployTypeList.forEach(platform -> {
                if (platform == Platform.ETHER)
                {
                    availableFilesystemTypes.add(getFilesystemTypeDto(FilesystemType.FILESYSTEM_ETHER, "FS Ether", env));
                    availableFilesystemTypes.add(getFilesystemTypeDto(FilesystemType.FILESYSTEM_EPSILON_ETHER, "Epsilon Ether", env));
                }
                else if (platform == Platform.NOVA)
                {
                    availableFilesystemTypes.add(getFilesystemTypeDto(FilesystemType.FILESYSTEM, "FS Nova", env));
                }
                else if (platform == Platform.AWS)
                {
                    LOG.debug("[FilesystemsServiceImpl] -> [getAvailableFilesystemTypes]: No FileSystem available for AWS Infrastructure");
                }
                // Case environment is not specify (ALL).
                else
                {
                    availableFilesystemTypes.add(getFilesystemTypeDto(FilesystemType.FILESYSTEM_ETHER, "FS Ether", env));
                    availableFilesystemTypes.add(getFilesystemTypeDto(FilesystemType.FILESYSTEM, "FS Nova", env));
                    availableFilesystemTypes.add(getFilesystemTypeDto(FilesystemType.FILESYSTEM_EPSILON_ETHER, "Epsilon Ether", env));
                }
            });
        });

        return availableFilesystemTypes;
    }

    /**
     * Gets a new instance of {@link FilesystemTypeDto}
     *
     * @param type        filesystem type
     * @param name        filesystem label
     * @param environment environment where the filesystem type is available
     * @return An instance of {@link FilesystemTypeDto}
     */
    private FilesystemTypeDto getFilesystemTypeDto(final FilesystemType type, final String name, final Environment environment)
    {
        FilesystemTypeDto filesystemTypeDto = new FilesystemTypeDto();
        filesystemTypeDto.setCode(type.getFileSystemType());
        filesystemTypeDto.setLabel(name);
        filesystemTypeDto.setEnvironment(environment.getEnvironment());

        return filesystemTypeDto;
    }

    /**
     * Reject all pending task for a given filesystem id
     *
     * @param filesystemId The filesystem id to reject tasks
     * @param action       represent the action or motive of that the filesystem task will be rejected
     */
    private void rejectPendingFilesystemTask(final Integer filesystemId, final String action)
    {
        LOG.debug("[FilesystemsServiceImpl] -> [rejectPendingFilesystemTask]: rejecting all pending tasks of filesystem id: [{}] due to the filesystem has been: [{}]", filesystemId, action);
        List<Integer> rejectedTaskList = new ArrayList<>();

        // Getting all pending filesystem task of type filesystem of the product filter by filesystem id.
        List<FilesystemTask> filesystemTaskList = this.filesystemTaskRepository.findByFilesystemId(filesystemId);

        // Reject and save the filesystem task
        // add the filesystem task id to the list
        filesystemTaskList.stream().filter(filesystemTask -> filesystemTask.getStatus() == ToDoTaskStatus.PENDING || filesystemTask.getStatus() == ToDoTaskStatus.PENDING_ERROR).forEach(filesystemTask ->
        {
            filesystemTask.setStatus(ToDoTaskStatus.REJECTED);
            filesystemTask.setClosingMotive(Constants.CLOSE_PENDING_FILESYSTEM_TASK_MESSAGE + action);
            this.toDoTaskRepository.save(filesystemTask);
            rejectedTaskList.add(filesystemTask.getId());
        });

        LOG.debug("[rejectedTaskList] -> [rejectPendingFilesystemTask]: rejected pending tasks with IDs: [{}] of filesystemId: [{}]", rejectedTaskList, filesystemId);
    }

    /**
     * Build the full user with the name, surnames and iv user code
     *
     * @param ivUser the iv user code
     * @return the full name built
     */
    String buildFullUserName(final String ivUser)
    {
        USUserDTO usUserDTO = this.usersClient.getUser(ivUser, new NovaException(FilesystemsError.getUserServiceNotFoundError(ivUser)));
        return usUserDTO.getUserName() + " " + usUserDTO.getSurname1() + " " + usUserDTO.getSurname2() + " (" + ivUser + ")";
    }

    /**
     * Get user mail address
     *
     * @param ivUser the iv user code
     * @return the user mail address
     */
    String getUserMailAddress(final String ivUser)
    {
        return this.usersClient.getUser(ivUser, new NovaException(FilesystemsError.getUserServiceNotFoundError(ivUser))).getEmail();
    }

    /**
     * Method to update the configuration of the filesystem
     *
     * @param configuration the configuration to update
     * @param filesystem    the filesystem
     * @throws NovaException if there is any error
     */
    private void updateFilesystemConfiguration(final FilesystemConfigurationDto[] configuration, final Filesystem filesystem, final String ivUser) throws NovaException
    {
        // Validate Namespace
        doCheckEtherConfigurationStatus(filesystem.getProduct().getId(), filesystem.getEnvironment(), filesystem.getType());

        // check the status of the filesystem
        this.filesystemsValidator.checkIfFilesystemIsAvailable(filesystem);

        // Call Filesystem Manager API - Generate an asynchronous call.
        this.filesystemManagerClient.callUpdateConfigurationFilesystemManager(filesystem, configuration, ivUser);
    }

    /**
     * Convert into related transfer info dto f filesystem related transfer info dto [ ].
     *
     * @param filteredList the filtered list
     * @return the f filesystem related transfer info dto [ ]
     */
    private FFilesystemRelatedTransferInfoDTO[] convertIntoRelatedTransferInfoDTO(List<FTATransferRelatedInfoAdminDTO> filteredList)
    {
        List<FFilesystemRelatedTransferInfoDTO> returnedtransfersInfoDto = new ArrayList<>();
        filteredList.forEach((filteredTransferElement) ->
        {
            FFilesystemRelatedTransferInfoDTO returnedTransfersInfoObjectReusable = new FFilesystemRelatedTransferInfoDTO();

            // Set simple information
            returnedTransfersInfoObjectReusable.setTransferId(filteredTransferElement.getTransferId());
            returnedTransfersInfoObjectReusable.setEnvironement(filteredTransferElement.getEnvironement());
            returnedTransfersInfoObjectReusable.setTransferName(filteredTransferElement.getTransferName());
            returnedTransfersInfoObjectReusable.setTransferStatus(filteredTransferElement.getTransferStatus());

            // Set complex information
            returnedTransfersInfoObjectReusable.setTransferLocationSource(this.convertIntoRelatedLocationInfoDto(filteredTransferElement.getTransferLocationSource()));
            returnedTransfersInfoObjectReusable.setTransferLocationTarget(this.convertIntoRelatedLocationInfoDto(filteredTransferElement.getTransferLocationTarget()));

            returnedtransfersInfoDto.add(returnedTransfersInfoObjectReusable);

        });
        return returnedtransfersInfoDto.toArray(new FFilesystemRelatedTransferInfoDTO[0]);
    }

    /**
     * Convert into related location info dto f transfer location info.
     *
     * @param transferLocationSource the transfer location source
     * @return the f transfer location info
     */
    private FTransferLocationInfo convertIntoRelatedLocationInfoDto(FTATransferLocationInfo transferLocationSource)
    {
        // Set information about transfer location
        FTransferLocationInfo fTransferLocationInfo = new FTransferLocationInfo();
        fTransferLocationInfo.setAbsolutePath(transferLocationSource.getAbsolutePath());
        fTransferLocationInfo.setFilesystemLocationName(transferLocationSource.getFilesysteLocationName());
        fTransferLocationInfo.setIsOrigin(transferLocationSource.getIsOrigin());
        fTransferLocationInfo.setTransferLocation(transferLocationSource.getTransferLocation());
        fTransferLocationInfo.setUuaa(transferLocationSource.getUuaa());

        return fTransferLocationInfo;

    }


}
