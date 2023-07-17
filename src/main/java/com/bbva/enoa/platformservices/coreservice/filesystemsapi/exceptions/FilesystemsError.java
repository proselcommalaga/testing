package com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemsErrorConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

/**
 * The type Filesystems error.
 */
@ExposeErrorCodes
public class FilesystemsError
{
    /**
     * Gets unexpected error.
     *
     * @return the unexpected error
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE,
                FilesystemsErrorConstants.UNEXPECTED_ERROR_MSG,
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Gets filesystem not available delete error.
     *
     * @return the filesystem not available delete error
     */
    public static NovaError getFilesystemNotAvailableDeleteError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FYLESYSTEM_NOT_AVAILABLE_DELETE_ERROR,
                "Tried to delete a filesystem but is in status creating or deleting",
                "Wait for the filesystem finished the action",
                HttpStatus.FORBIDDEN
                , ErrorMessageType.ERROR);
    }

    /**
     * Gets no such product error.
     *
     * @param productId the product id
     * @return the no such product error
     */
    public static NovaError getNoSuchProductError(final Integer productId)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NO_SUCH_PRODUCT_ERROR,
                MessageFormat.format("Product with the given id: {0} not found into data base.", productId),
                "Refresh or update the page, and check if there is a product with the given ID in the NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets no such filesystem error.
     *
     * @param filesystemId the filesystem id
     * @return the no such filesystem error
     */
    public static NovaError getNoSuchFilesystemError(final Integer filesystemId)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ERROR,
                MessageFormat.format("Filesystem with the given id: {0} not found into NOVA data base.", filesystemId),
                "Check if there is a filesystem with the given ID in the NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets not environment to download.
     *
     * @param filesystemId the filesystem id
     * @param environment  the environment
     * @return the not environment to download
     */
    public static NovaError getNotEnvironmentToDownload(final Integer filesystemId, final String environment)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.ENVIRONMENT_SELECTED_ERROR,
                MessageFormat.format("The download to pre of the filesystem id [{0}] is not the appropriate environment selected: [{1}].", filesystemId, environment),
                "The request to download to PRE can only do it from PRODUCTION environment",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets no enough filesystem budget error.
     *
     * @param productId   the product id
     * @param packId      the pack id
     * @param environment the environment
     * @return the no enough filesystem budget error
     */
    public static NovaError getNoEnoughFilesystemBudgetError(final Integer productId, final Integer packId, final String environment)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NO_ENOUGH_FILESYSTEM_BUDGET_ERROR,
                MessageFormat.format("The product with the given id: {0} and environment: {1} has no budget enough to create this filesystem. Pack id associated: {2}", productId, environment, packId),
                "Request  more filesystem GBs to NOVA Administrators for this product.",
                HttpStatus.INSUFFICIENT_STORAGE,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets filesystem creation error.
     *
     * @return the filesystem creation error
     */
    public static NovaError getFilesystemCreationError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME, FilesystemsErrorConstants.FILESYSTEM_CREATION_ERROR, "The filesystem couldn't be created in the storage system", "Check if storage system is available", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * Gets filesystem deletion error.
     *
     * @return the filesystem deletion error
     */
    public static NovaError getFilesystemDeletionError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME, FilesystemsErrorConstants.FILESYSTEM_DELETION_ERROR, "The filesystem couldn't be deleted from the storage system", "Check if storage system is available", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * Gets deleted used filesystem error.
     *
     * @return the deleted used filesystem error
     */
    public static NovaError getDeletedUsedFilesystemError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME, FilesystemsErrorConstants.DELETED_USED_FILESYSTEM_ERROR, "Tried to delete a filesystem being used by at least one service of a plan or a broker", "It's not allowed to delete a filesystem being used on any not undeployed plan or broker", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * Gets duplicated filesystem error.
     *
     * @param productId      the product id
     * @param environment    the environment
     * @param filesystemName the filesystem name
     * @return the duplicated filesystem error
     */
    public static NovaError getDuplicatedFilesystemError(final Integer productId, final String environment, final String filesystemName)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DUPLICATED_FILESYSTEM_ERROR,
                MessageFormat.format("There is another filesystem in the product with id: {0} environment: {1} with this name: {2}", productId, environment, filesystemName),
                "Choose another different name for creating a Filesystem",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets duplicated filesystem not found error.
     *
     * @param productId      the product id
     * @param environment    the environment
     * @param filesystemName the filesystem name
     * @return the duplicated filesystem not found error
     */
    public static NovaError getDuplicatedFilesystemNotFoundError(final Integer productId, final String environment, final String filesystemName)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DUPLICATED_FILESYSTEM_NOT_FOUND_ERROR,
                MessageFormat.format("There is NOT another filesystem in the product with id: {0} environment: {1} with this name: {2}", productId, environment, filesystemName),
                "For downloads to PRE, must have a filesystem with the same name than the filesystem of the origin (means the same name than the production filesystem)",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets too many filesystems error.
     *
     * @param productId      the product id
     * @param environment    the environment
     * @param filesystemType the filesystem type
     * @return the too many filesystems error
     */
    public static NovaError getTooManyFilesystemsError(final Integer productId, final String environment, final String filesystemType)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.TOO_MANY_FILESYSTEMS_ERROR,
                MessageFormat.format("Reach the the maximum allowed filesystems on the same environment: {0} in product: {1} filesystem type name: {2}", environment, productId, filesystemType),
                "Try deleting or archiving a filesystem first",
                HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * Gets duplicated landing zone path error.
     *
     * @param productId      the product id
     * @param environment    the environment
     * @param filesystemName the filesystem name
     * @return the duplicated landing zone path error
     */
    public static NovaError getDuplicatedLandingZonePathError(final Integer productId, final String environment, final String filesystemName)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DUPLICATED_LANDING_ZONE_PATH_ERROR,
                MessageFormat.format("Tried to create a filesystem with a landing zone path already being used in the same environment: {1} of product with id: {0} with filesystem nae name: {2}", productId, environment, filesystemName),
                "Try deleting or archiving a filesystem first",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets tried to archive used filesystem error.
     *
     * @return the tried to archive used filesystem error
     */
    public static NovaError getTriedToArchiveUsedFilesystemError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME, FilesystemsErrorConstants.TRIED_TO_ARCHIVE_USED_FILESYSTEM_ERROR, "Tried to archive a filesystem being used by at least one service of a deployed plan", "It's not allowed to archive a filesystem being used on any deployed plan", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * Gets filesystem not available view error.
     *
     * @param filesystemId the filesystem id
     * @return the filesystem not available view error
     */
    public static NovaError getFilesystemNotAvailableViewError(final Integer filesystemId)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_NOT_AVAILABLE_VIEW_ERROR,
                MessageFormat.format("Tried to view a filesystem detail but is in status creating or deleting of filesystem with id: {0}", filesystemId),
                "Wait until the filesystem finished the action",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets filesystem not available create error.
     *
     * @param productId the product id
     * @return the filesystem not available create error
     */
    public static NovaError getFilesystemNotAvailableCreateError(final Integer productId)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_NOT_AVAILABLE_CREATE_ERROR,
                MessageFormat.format("Tried to create a new filesystem but is already a filesystem in status creating or deleting in product with id: {0}", productId),
                "Wait for the filesystem finished the action",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * Gets get files error.
     *
     * @param errorMessage the error message
     * @return the get files error
     */
    public static NovaError getGetFilesError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.GET_FILES_ERROR,
                "There was an error trying to get a file. Error message: [" + errorMessage + "]",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets delete file error.
     *
     * @param errorMessage the error message
     * @return the delete file error
     */
    public static NovaError getDeleteFileError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DELETE_FILE_ERROR, "There was an error trying to delete a file. Error message: [" + errorMessage + "]",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets download file error from PRO to PRE.
     *
     * @param errorMessage the error message
     * @return the download file error
     */
    public static NovaError getDownloadToPreviousEnvironmentFileError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DOWNLOAD_FILE_ERROR,
                "There was an error trying to download from PRO to PRE file. Error message: [" + errorMessage + "]",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets upload file error.
     *
     * @return the upload file error
     */
    public static NovaError getUploadFileError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.UPLOAD_FILE_ERROR,
                "There was an error trying to upload a file. Error message: [" + errorMessage + "]",
                "Please, refresh the status of filesystem and try it again. If the problems continues, please, contact with NOVA Support Team",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets get file use error.
     *
     * @param errorMessage the error message
     * @return the get file use error
     */
    public static NovaError getGetFileUseError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.GET_FILE_USE_ERROR,
                "There was an error trying to get the file use of this filesystem. Error message: [" + errorMessage + "]",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets action permission error.
     *
     * @param errorMessage the error message
     * @return the action permission error
     */
    public static NovaError getActionPermissionError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.ACTION_PERMISSION_ERROR,
                "It´s not allowed to make this action in current environment. Error message: [" + errorMessage + "]",
                "The current user cannot do this action in the environment. Maybe the 'autoconfig' or 'autodeploy' flag is not activated for this environment. Please, review the product configuration on the environment contacting to the NOVA Support team",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.WARNING);
    }

    /**
     * Gets frozen validation error.
     *
     * @return the frozen validation error
     */
    public static NovaError getFrozenValidationError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME, FilesystemsErrorConstants.FROZEN_VALIDATION_ERROR, "Error validating if filesystem is frozen", "There was some error in the Filesystem Manager Service. Review the logs", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * Gets create directory error.
     *
     * @param errorMessage the error message
     * @return the create directory error
     */
    public static NovaError getCreateDirectoryError(final String errorMessage)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.CREATE_DIRECTORY_ERROR,
                "There was an error trying to create a directory in filesystem. Error message: [" + errorMessage + "]",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * Gets filesystem busy error.
     *
     * @return the filesystem busy error
     */
    public static NovaError getFilesystemBusyError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_BUSY_ERROR,
                "The filesystem is busy making some issue (uploading, downloading or deleting a file)",
                "Wait until the filesystem ends the issue", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    /**
     * Gets not allowed file name pattern error.
     *
     * @return the not allowed file name pattern error
     */
    public static NovaError getNotAllowedFileNamePatternError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NOT_ALLOWED_FILE_NAME_PATTERN_ERROR,
                "The filename contains special character not allowed",
                "Rename the file without special characters",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * Gets not allowed modify directories error.
     *
     * @param filesystemId  the filesystem id
     * @param directoryName the directory name
     * @return the not allowed modify directories error
     */
    public static NovaError getNotAllowedModifyDirectoriesError(final Integer filesystemId, final String directoryName)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NOT_ALLOWED_MODIFY_DIRECTORIES_ERROR,
                MessageFormat.format("The file directory name: {0} can not be modified. It is reserved name of the NOVA in filesystem with id: {0}", directoryName, filesystemId),
                "This file directory name is reserved. Cannot create a directory with this name",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets not fs alert error.
     *
     * @param filesystemId the filesystem id
     * @return the not fs alert error
     */
    public static NovaError getNotFSAlertError(final Integer filesystemId)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NO_FS_ALERT_ERROR,
                MessageFormat.format("The filesystem with id: {0} does not have alert associated", filesystemId),
                "This filesystem has been created before including the changes of FS Alert and their data from BBDD is inconsistent. Delete and create it again.",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets not such fs alert info dto error.
     *
     * @return the not such fs alert info dto error
     */
    @Deprecated
    public static NovaError getNotSuchFSAlertInfoDtoError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ALERT_INFO_DTO,
                "A DTO is needed for obtain the data",
                "Check if the client is defining the DTO object",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets not such fs alert id error.
     *
     * @return the not such fs alert id error
     */
    public static NovaError getNotSuchFSAlertIdError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.NO_SUCH_FILESYSTEM_ALERT_INFO_ID,
                "A filesystem id is need to operate in the database",
                "Check if the client is defining the filesystem id",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets database fs alert error.
     *
     * @param filesystemId the filesystem id
     * @return the database fs alert error
     */
    public static NovaError getDatabaseFSAlertError(final Integer filesystemId)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DATABASE_FILESYSTEM_ALERT_ERROR,
                MessageFormat.format("The filesystem alert could not be save due to a database exception for filesystem id: {0}", filesystemId),
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets database fs error.
     *
     * @return the database fs error
     */
    public static NovaError getDatabaseFSError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DATABASE_FILESYSTEM_ERROR,
                "The filesystem couldn't be checked due to an internal error.",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets forbidden error.
     *
     * @return the forbidden error
     */
    public static NovaError getForbiddenError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.USER_PERMISSIONS_ERROR_CODE,
                "The current user does not have permission for this operation",
                "Use a user wiht higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets filesystem pack code error.
     *
     * @param filesystemPackCode the filesystem pack code
     * @return the filesystem pack code error
     */
    public static NovaError getFilesystemPackCodeError(final String filesystemPackCode)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_PACK_CODE_ERROR,
                "The filesystem pack code: [" + filesystemPackCode + "] does not found into NOVA BBDD.",
                "Review with NOVA Administrators if this filesystem pack name is enabled or exists on the NOVA Platform",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets bad arguments error.
     *
     * @return the bad arguments error
     */
    public static NovaError getBadArgumentsError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.BAD_ARGUMENTS_EVENTS_CONFIGURATION,
                "The parameters used in the request are not valid",
                "Check the value and type for the arguments sent.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }
    /**
     * Gets creating or deleting file system on non configured platform error.
     *
     * @return the creating or deleting file system on non configured platform error
     */
    public static NovaError getCreatingOrDeletingFileSystemOnNonConfiguredPlatformError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.DESTINATION_PLATFORM_KO,
                "Unable to create or delete filesystem because the destination platform is not configured.",
                "Configure the platform and try again.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets action frozen error.
     *
     * @param filesystemName the filesystem name
     * @param filesystemId   the filesystem id
     * @param env            the env
     * @param ivUser         the iv user
     * @return the action frozen error
     */
    public static NovaError getActionFrozenError(String filesystemName, Integer filesystemId, String env, String ivUser)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FROZEN_ACTION_ERROR,
                "It´s not allowed in the File system name: [" + filesystemName + "] - file systme id: [" + filesystemId + "] " +
                        "to make this action in current environment: [" + env + "] for user code: [" + ivUser + "] due to a release without automanage have using this Files system ",
                "Please, change management mode of the release that have been using this file system",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);

    }

    /**
     * Gets creation filesystem task error.
     *
     * @param filesystemId the filesystem id
     * @param path         the path
     * @param fileName     the file name
     * @param env          the env
     * @return the creation filesystem task error
     */
    public static NovaError getCreationFilesystemTaskError(final Integer filesystemId, final String path, final String fileName, final String env)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.CREATE_FILESYSTEM_TASK_ERROR,
                "Error creating a filesystem Task for file system id: [" + filesystemId + "] - filesystem path: [" + path + "] " +
                        "file name: [" + fileName + "] to make this action in current environment: [" + env + "] calling Todo Task Service client.",
                "Please, review if the filesystem exists or the to do task service is working and if the problem persist, contact with NOVA Team. ",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);

    }

    /**
     * Gets filesystem in use by services error.
     *
     * @param filesystemName the filesystem name
     * @return the filesystem in use error
     */
    public static NovaError getFilesystemInUseByServicesError(final String filesystemName)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_IN_USE_BY_SERVICES_ERROR,
                "It is not allowed to make this action because the filesystem [" + filesystemName + "] is currently in use.",
                "Please, review if there is any associated Plan with status different from STORAGED. If the problem persist contact with NOVA Team.",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);

    }

    /**
     * Gets filesystem in use by brokers error.
     *
     * @param filesystemName the filesystem name
     * @return the filesystem in use error
     */
    public static NovaError getFilesystemInUseByBrokersError(final String filesystemName)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_IN_USE_BY_BROKERS_ERROR,
                "Filesystem [" + filesystemName + "] is currently being used by a broker.",
                "Please, delete the associated broker and then try again.",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);

    }

    /**
     * Gets filesystem pack code environment error.
     *
     * @param packCodeEnvironment   the pack code environment
     * @param filesystemEnvironment the filesystem environment
     * @return the filesystem pack code environment error
     */
    public static NovaError getFilesystemPackCodeEnvironmentError(String packCodeEnvironment, String filesystemEnvironment)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_PACK_CODE_ENV_ERROR,
                "It is not allowed to make this action because the filesystem environment [" + filesystemEnvironment + "] don´t match with new pack environment [" + packCodeEnvironment + "].",
                "Please, contact with NOVA Team.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets update quota invalid filesystem type.
     *
     * @param fileSystemType the file system type
     * @return the update quota invalid filesystem type
     */
    public static NovaError getUpdateQuotaInvalidFilesystemType(String fileSystemType)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_UPDATE_QUOTA_INVALID_FILESYSTEM_TYPE_ERROR,
                "It is not allowed to make this action because the filesystem type is: [" + fileSystemType + "].",
                "Please, try to upload quota only in NOVA type filesystem",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets unrecognizable size unit error.
     *
     * @param sizeUnit the size unit
     * @return the unrecognizable size unit error
     */
    public static NovaError getUnrecognizableSizeUnitError(String sizeUnit)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_SIZE_UNIT_ERROR,
                "It is not allowed to make this action because size unit is unrecognizable: [" + sizeUnit + "].",
                "Please, contact with NOVA Team.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets invalid size error.
     *
     * @param size the size unit
     * @return the unrecognizable size unit error
     */
    public static NovaError getInvalidSizeError(String size)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_INVALID_SIZE_ERROR,
                "It is not allowed to make this action because size is invalid: [" + size + "].",
                "Please, contact with NOVA Team.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets not enough free space to update quota error.
     *
     * @param filesystemId the filesystem id
     * @param packCode     the pack code
     * @return the not enough free space to update quota error
     */
    public static NovaError getNotEnoughFreeSpaceToUpdateQuotaError(Integer filesystemId, String packCode)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_NOT_ENOUGH_FREE_STORAGE_ERROR,
                "Storage space too full for resizing filesystem [" + filesystemId + "] with pack [" + packCode + "]",
                "Please, Free up space before resizing the filesystem.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets filesystem Id error
     *
     * @param path        the path
     * @param environment the env
     * @return the filesystem id not found
     */
    public static NovaError getFilesystemIdError(final String path, final String environment)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_ID_NOT_FOUND,
                "Filesystem id for path: [" + path + "] and environment: [" + environment + "] not found into NOVA data base.",
                "Check if there are a path and environment in the NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets filesystem Id error
     *
     * @return the filesystem id not found
     */
    public static NovaError getAlertServiceCallError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_ALERT_SERVICE_ERROR,
                "Error calling alert service",
                "Check if Platform Health Monitor is working properly. Contact with NOVA administrator",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets an error indicating a directory can not be created inside a reserved directory
     *
     * @param newDirectory new directory name
     * @param parentDirectory parent directory path
     * @return the error
     */
    public static NovaError getCreatingDirectoryInsideReservedOneError(final String newDirectory, final String parentDirectory)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.CREATE_DIRECTORY_INSIDE_RESERVED_DIRECTORY_ERROR,
                MessageFormat.format("The directory {0} can not be created into directory {1} because it is inside a reserved directory by NOVA platform", newDirectory, parentDirectory),
                "Select another directory or contact NOVA support",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets an error indicating a file cannot be uploaded inside a reserved directory
     *
     * @param filename file name to upload
     * @param parentDirectory parent directory path
     * @return the error
     */
    public static NovaError getUploadingFileInsideReservedDirectoryError(final String filename, final String parentDirectory)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.UPLOADING_FILE_INSIDE_RESERVED_DIRECTORY_ERROR,
                MessageFormat.format("The file {0} can not be uploaded to directory {1} because it is inside a reserved directory by NOVA platform", filename, parentDirectory),
                "Select another directory or contact NOVA support",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Update quota error
     *
     * @param errorMessage the iv user requester
     * @return the Nova Error
     */
    public static NovaError getUpdateQuotaError(final String errorMessage)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_UPDATE_QUOTA_ERROR,
                "There was an error trying to update the quota of the filesystem. Error message: [" + errorMessage + "]",
                FilesystemsErrorConstants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets filesystem update the configuration error.
     *
     * @return the filesystem update the configuration error
     */
    public static NovaError getFilesystemUpdateConfigurationError()
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_UPDATE_ERROR,
                "The filesystem's configuration couldn't be updated in the storage system",
                "Check if storage system is available",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * Gets not allowed operation for type of filesystem
     *
     * @param filesystemType the type of filesystem
     * @param action         the action
     * @return the filesystem error
     */
    public static NovaError getOperationNotAllowedForFilesystemTypeError(final String filesystemType, final String action)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.FILESYSTEM_OPERATION_NOT_ALLOWED_ERROR,
                "It is not allowed to " + action + " filesystems of type [" + filesystemType + "]",
                "Try with a valid filesystem",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets deletion error for Epsilon not empty FS
     *
     * @param filesystemName the name of filesystem
     * @return the filesystem error
     */
    public static NovaError getEpsilonFSNotEmptyDeletionError(final String filesystemName)
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.EPSILON_FS_NOT_EMPTY_DELETION_ERROR,
                "It is not possible to delete the filesystem : [" + filesystemName + "] because it is not empty.",
                "Please, remove all files/objects.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets Service not found error
     *
     * @param ivUser the iv user requester
     * @return the filesystem id not found
     */
    public static NovaError getUserServiceNotFoundError(final String ivUser)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.USER_NOT_FOUND,
                "The user with iv-user: [" + ivUser + "] does not found",
                "Check if the user service is running or the iv-user exists. Contact with NOVA administrator",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.CRITICAL);
    }
    /**
     * Gets Product Without Remedy Group not found error
     *
     * @return the filesystem error
     */
    public static NovaError generateProductWithoutRemedyGroupError()
    {
        return new NovaError(FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.PRODUCT_HAS_NOT_REMEDY_GROUP_ERROR_CODE,
                "Product must have configured a remedy group for using notifications by Patrol",
                "Set a valid remedy group in product configuration or uncheck Patrol notification/s",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Gets size page error
     *
     * @param sizePage        the size passed
     * @return the size is not valid
     */
    public static NovaError getSizePageError(final int sizePage)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.BAD_SIZE_PAGE_GETFILES_REQUEST,
                "Size page passed ["+sizePage+"] bigger than max allowed ["+ Constants.MAX_SIZE_PAGE +"] " ,
                "Change the size page, the value range is [1, 100]",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }


    /**
     * Gets number page error
     *
     * @param numberPage        the number page passed
     * @return the number page is not valid
     */
    public static NovaError getNumberPageError(final int numberPage)
    {
        return new NovaError(
                FilesystemsErrorConstants.FILESYSTEMS_ERROR_CLASS_NAME,
                FilesystemsErrorConstants.BAD_NUMBER_PAGE_GETFILES_REQUEST,
                "Number page passed ["+numberPage+"] is less than 0" ,
                "Change the number page, the value has to be greater than 0",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }
}