package com.bbva.enoa.platformservices.coreservice.filesystemsapi.util;

/**
 * Class dedicated to write and store the filesystem manager constants
 */
public final class Constants
{
    /**
     * Empty constructor
     */
    private Constants()
    {
        // Empty constructor
    }

    /**
     * KO literal
     */
    public static final String KO = "KO";
    /**
     * DEPLOY literal
     */
    public static final String DEPLOY = "DEPLOY";
    /**
     * Environment notification property key
     */
    public static final String ENVIRONMENT = "environment";

    /**
     * Message to show when a pending task is automatically closed because the filesystem has been deleted or archived"
     */
    public static final String CLOSE_PENDING_FILESYSTEM_TASK_MESSAGE = "Cerrado automáticamente debido a que el filesystem asociado ha cambiado su estado a ";

    /**
     * Available status for Alert service client
     */
    public static final String OPEN_ALERT_STATUS = "OPEN";

    /**
     * Number max of register in getFiles
     */
    public static final int MAX_SIZE_PAGE = 100;


    /**
     * Class of constants only for budget errors
     */
    public static class FilesystemsErrorConstants
    {
        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "FILESYSTEMS-000";

        /**
         * Fylesystem Not Available Delete Error
         */
        public static final String FYLESYSTEM_NOT_AVAILABLE_DELETE_ERROR = "FILESYSTEMS-001";

        /**
         * NoSuchProductError
         */
        public static final String NO_SUCH_PRODUCT_ERROR = "FILESYSTEMS-002";

        /**
         * get NoSuchFilesystemError
         */
        public static final String NO_SUCH_FILESYSTEM_ERROR = "FILESYSTEMS-003";

        /**
         * get NoEnoughFilesystemBudgetError
         */
        public static final String NO_ENOUGH_FILESYSTEM_BUDGET_ERROR = "FILESYSTEMS-004";

        /**
         * get FilesystemCreationError
         */
        public static final String FILESYSTEM_CREATION_ERROR = "FILESYSTEMS-005";

        /**
         * get FilesystemDeletionError
         */
        public static final String FILESYSTEM_DELETION_ERROR = "FILESYSTEMS-006";

        /**
         * get DeletedUsedFilesystemError
         */
        public static final String DELETED_USED_FILESYSTEM_ERROR = "FILESYSTEMS-007";

        /**
         * get DuplicatedFilesystemError
         */
        public static final String DUPLICATED_FILESYSTEM_ERROR = "FILESYSTEMS-008";

        /**
         * get TooManyFilesystemsError
         */
        public static final String TOO_MANY_FILESYSTEMS_ERROR = "FILESYSTEMS-009";

        /**
         * get DuplicatedLandingZonePathError
         */
        public static final String DUPLICATED_LANDING_ZONE_PATH_ERROR = "FILESYSTEMS-010";

        /**
         * get TriedToArchiveUsedFilesystemError
         */
        public static final String TRIED_TO_ARCHIVE_USED_FILESYSTEM_ERROR = "FILESYSTEMS-011";
        /**
         * get FilesystemNotAvailableViewError
         */
        public static final String FILESYSTEM_NOT_AVAILABLE_VIEW_ERROR = "FILESYSTEMS-012";

        /**
         * get FilesystemNotAvailableCreateError
         */
        public static final String FILESYSTEM_NOT_AVAILABLE_CREATE_ERROR = "FILESYSTEMS-013";
        /**
         * get GetFilesError
         */
        public static final String GET_FILES_ERROR = "FILESYSTEMS-014";

        /**
         * get DeleteFileError
         */
        public static final String DELETE_FILE_ERROR = "FILESYSTEMS-015";
        /**
         * get DownloadFileError
         */
        public static final String DOWNLOAD_FILE_ERROR = "FILESYSTEMS-016";
        /**
         * get UploadFileError
         */
        public static final String UPLOAD_FILE_ERROR = "FILESYSTEMS-017";

        /**
         * get GetFileUseError
         */
        public static final String GET_FILE_USE_ERROR = "FILESYSTEMS-018";

        /**
         * get ActionPermissionError
         */
        public static final String ACTION_PERMISSION_ERROR = "FILESYSTEMS-019";

        /**
         * get FrozenValidationError
         */
        public static final String FROZEN_VALIDATION_ERROR = "FILESYSTEMS-020";

        /**
         * get CreateDirectoryError
         */
        public static final String CREATE_DIRECTORY_ERROR = "FILESYSTEMS-021";

        /**
         * get FilesystemBusyError
         */
        public static final String FILESYSTEM_BUSY_ERROR = "FILESYSTEMS-022";
        /**
         * get NotAllowedFileNamePatternError
         */
        public static final String NOT_ALLOWED_FILE_NAME_PATTERN_ERROR = "FILESYSTEMS-023";
        /**
         * get NotAllowedModifyDirectoriesError
         */
        public static final String NOT_ALLOWED_MODIFY_DIRECTORIES_ERROR = "FILESYSTEMS-024";

        /**
         * get no fs alert
         */
        public static final String NO_FS_ALERT_ERROR = "FILESYSTEMS-025";

        /**
         * get such filesystem alert info dto
         */
        public static final String NO_SUCH_FILESYSTEM_ALERT_INFO_DTO = "FILESYSTEMS-026";

        /**
         * get such filesystem alert info id
         */
        public static final String NO_SUCH_FILESYSTEM_ALERT_INFO_ID = "FILESYSTEMS-027";

        /**
         * get data base filesystem alert error
         */
        public static final String DATABASE_FILESYSTEM_ALERT_ERROR = "FILESYSTEMS-028";

        /**
         * get filesystem pack error
         */
        public static final String FILESYSTEM_PACK_CODE_ERROR = "FILESYSTEMS-029";
        /**
         * Bad arguments in the request
         */
        public static final String BAD_ARGUMENTS_EVENTS_CONFIGURATION = "FILESYSTEMS-030";

        /**
         * Filesystem database bad query error
         */
        public static final String DATABASE_FILESYSTEM_ERROR = "FILESYSTEMS-031";
        /**
         * Filesystem namespace KO
         */
        public static final String DESTINATION_PLATFORM_KO = "FILESYSTEMS-032";

        /**
         * Filesystem action frozen error
         */
        public static final String FROZEN_ACTION_ERROR = "FILESYSTEMS-033";

        /**
         * Filesystem environment selected
         */
        public static final String ENVIRONMENT_SELECTED_ERROR = "FILESYSTEMS-034";

        /**
         * Filesystem duplicated not found
         */
        public static final String DUPLICATED_FILESYSTEM_NOT_FOUND_ERROR = "FILESYSTEMS-035";

        /**
         * Filesystem duplicated not found
         */
        public static final String CREATE_FILESYSTEM_TASK_ERROR = "FILESYSTEMS-036";

        /**
         * Filesystem is being used by a service
         */
        public static final String FILESYSTEM_IN_USE_BY_SERVICES_ERROR = "FILESYSTEMS-037";

        /**
         * Filesystem pack for wrong environment
         */
        public static final String FILESYSTEM_PACK_CODE_ENV_ERROR = "FILESYSTEMS-038";

        /**
         * Filesystem size unit unrecognizable error
         */
        public static final String FILESYSTEM_SIZE_UNIT_ERROR = "FILESYSTEMS-039";

        /**
         * Filesystem error if not enough free storage for update quota
         */
        public static final String FILESYSTEM_NOT_ENOUGH_FREE_STORAGE_ERROR = "FILESYSTEMS-040";

        /**
         * get Filesystem Id Error
         */
        public static final String FILESYSTEM_ID_NOT_FOUND = "FILESYSTEMS-041";

        /**
         * get call alert service Error
         */
        public static final String FILESYSTEM_ALERT_SERVICE_ERROR = "FILESYSTEMS-042";

        /**
         * Filesystem invalid filesystem type while updating quota
         */
        public static final String FILESYSTEM_UPDATE_QUOTA_INVALID_FILESYSTEM_TYPE_ERROR = "FILESYSTEMS-043";

        /**
         * Filesystem size invalid
         */
        public static final String FILESYSTEM_INVALID_SIZE_ERROR = "FILESYSTEMS-044";

        /**
         * Filesystem size invalid
         */
        public static final String FILESYSTEM_UPDATE_QUOTA_ERROR = "FILESYSTEMS-045";

        /**
         * Filesystem is being used by a broker
         */
        public static final String FILESYSTEM_IN_USE_BY_BROKERS_ERROR = "FILESYSTEMS-046";

        /**
         * Error code trying to create a directory inside a reserved one
         */
        public static final String CREATE_DIRECTORY_INSIDE_RESERVED_DIRECTORY_ERROR = "FILESYSTEMS-047";

        /**
         * Error code trying to upload a file inside a reserved directory
         */
        public static final String UPLOADING_FILE_INSIDE_RESERVED_DIRECTORY_ERROR = "FILESYSTEMS-048";

        /**
         * get FilesystemUpdateConfigurationError
         */
        public static final String FILESYSTEM_UPDATE_ERROR = "FILESYSTEMS-049";

        /**
         * get OperationNotAllowedForFilesystemTypeError
         */
        public static final String FILESYSTEM_OPERATION_NOT_ALLOWED_ERROR = "FILESYSTEMS-050";

        /**
         * get EpsilonFSNotEmptyDeletionError
         */
        public static final String EPSILON_FS_NOT_EMPTY_DELETION_ERROR = "FILESYSTEMS-051";

        /**
         * Bad arguments in the request due to syncronization
         */
        public static final String BAD_ARGUMENTS_IN_REQUEST_SYNC = "FILESYSTEMS-052";

        /**
         * Bad size page in getFiles
         */
        public static final String BAD_SIZE_PAGE_GETFILES_REQUEST = "FILESYSTEMS-053";

        /**
         * Bad size page number in getFiles
         */
        public static final String BAD_NUMBER_PAGE_GETFILES_REQUEST = "FILESYSTEMS-054";

        /**
         * Product has not remedy group error code
         */
        public static final String PRODUCT_HAS_NOT_REMEDY_GROUP_ERROR_CODE = "FILESYSTEMS-055";

        /**
         * Filesystems error class name
         */
        public static final String FILESYSTEMS_ERROR_CLASS_NAME = "FilesystemsError";

        /**
         * Generic message contact NOVA team
         */
        public static final String MSG_CONTACT_NOVA = "Refresh or update the filesystem and try it again. If the problem persist, please, contact the NOVA Support team";

        /**
         * Unexptected internal error
         */
        public static final String UNEXPECTED_ERROR_MSG = "There was an unexpected internal error";

        /**
         * User without permissions
         */
        public static final String USER_PERMISSIONS_ERROR_CODE = "USER-002";

        /**
         * User without permissions
         */
        public static final String USER_NOT_FOUND = "USER-001";
    }

    /**
     * Filesystem literal constants class
     */
    public static class FilesystemsLiteralsConstants
    {
        /**
         * Literals of
         */
        public static final String FILESYSTEMS_API_NAME = "FilesystemsAPI";
        /**
         * Literals of
         */
        public static final String OUTGOING_DIRECTORY_NAME = "outgoing";
        /**
         * Literals of
         */
        public static final String INCOMING_DIRECTORY_NAME = "incoming";
        /**
         * Literals of
         */
        public static final String RESOURCES_DIRECTORY_NAME = "resources";
    }

    /**
     * Filesystem permissions constants class
     */
    public static class FilesystemPermissionsConstants
    {
        /**
         * Permission name for
         */
        public static final String CREATE_FILESYSTEM_PERMISSION = "CREATE_FILESYSTEM";
        /**
         * Permission name for
         */
        public static final String UPDATE_FILESYSTEM_PERMISSION = "UPDATE_FILESYSTEM";
        /**
         * Permission name for
         */
        public static final String UPDATE_PLAT_FILESYSTEM_PERMISSION = "UPDATE_PLATFORM_FILESYSTEM";
        /**
         * Permission name for
         */
        public static final String FILESYSTEM_CREATE_DIR_PERMISSION = "FILESYSTEM_CREATE_DIR";
        /**
         * Permission name for
         */
        public static final String REMOVE_FILESYSTEM_PERMISSION = "REMOVE_FILESYSTEM";
        /**
         * Permission name for
         */
        public static final String ARCHIVE_FILESYSTEM_PERMISSION = "ARCHIVE_FILESYSTEM";
        /**
         * Permission name for
         */
        public static final String RESTORE_FILESYSTEM_PERMISSION = "RESTORE_FILESYSTEM";
        /**
         * Permission name for
         */
        public static final String DOWNLOAD_FILES_PRO_TO_PRE_PERMISSION = "DOWNLOAD_FILES_PRO_TO_PRE";
        /**
         * Permission name for
         */
        public static final String FILESYSTEM_DELETE_PERMISSION = "FILESYSTEM_DELETE";
        /**
         * Permission name for
         */
        public static final String FILESYSTEM_MODIFY_ALERT_PERMISSION = "FILESYSTEM_MODIFY_ALERT";
        /**
         * Permission name for
         */
        public static final String FILESYSTEM_MODIFY_QUOTA = "FILESYSTEM_MODIFY_QUOTA";
    }
}
