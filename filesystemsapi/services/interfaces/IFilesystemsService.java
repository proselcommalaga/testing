package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FFilesystemRelatedTransferInfoDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemConfigurationDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemTypeDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface for Filesystem Service
 *
 * @author XE63267
 */
public interface IFilesystemsService
{
    /**
     * Get a filesystem providing a filesystem id
     *
     * @param filesystemId the filesystem id
     * @return a FilesystemDTO instance
     * @throws NovaException if any error expected happens
     */
    FilesystemDto getFilesystem(Integer filesystemId) throws NovaException;

    /**
     * Delete a filesystem providing a filesystem id
     *
     * @param ivUser       the ivUser requester
     * @param filesystemId the filesystem id
     * @throws NovaException if any error expected happens     *
     */
    void deleteFilesystem(String ivUser, Integer filesystemId) throws NovaException;

    /**
     * Creates a new filesystem
     *
     * @param filesystemToAdd the parameters for creating the filesystem
     * @param ivUser          the ivUser requester
     * @param productId       the product id that will allow the filesystem
     * @throws NovaException if any error expected happens
     */
    void createFilesystem(CreateNewFilesystemDto filesystemToAdd, String ivUser, Integer productId) throws NovaException;

    /**
     * Updates the filesystem platform configuration
     *
     * @param configuration the configuration to be updated
     * @param filesystemId  the filesystem id
     * @param ivUser        the ivUser requester
     * @throws NovaException if any error expected happens
     */
    void updateFilesystemPlatformConfiguration(FilesystemConfigurationDto[] configuration, Integer filesystemId, String ivUser);

    /**
     * Updates the filesystem configuration
     *
     * @param configuration the configuration to be updated
     * @param filesystemId  the filesystem id
     * @param ivUser        the ivUser requester
     * @throws NovaException if any error expected happens
     */
    void updateFilesystemConfiguration(FilesystemConfigurationDto[] configuration, Integer filesystemId, String ivUser) throws NovaException;

    /**
     * Archive a filesystem providing a filesystem id
     *
     * @param ivUser       the ivUser requester
     * @param filesystemId the filesystem id
     * @throws NovaException if any error expected happens     *
     */
    void archiveFilesystem(String ivUser, Integer filesystemId) throws NovaException;

    /**
     * Request a download a file or folder (recursive) to preproduction environment.
     * This method will generate a to do task
     * If the filename is null, means the download is going to be of the folder
     *
     * @param ivUser       the ivUser requester
     * @param filesystemId   the filesystem id
     * @param filename       the file name to download (in case of not null)
     * @param filesystemPath the path to make the download of the folder
     */
    void downloadToPre(String ivUser, Integer filesystemId, String filename, String filesystemPath);

    /**
     * Restore a filesystem providing a filesystem id
     *
     * @param ivUser       the ivUser requester
     * @param filesystemId the filesystem id
     * @throws NovaException if any error expected happens
     */
    void restoreFilesystem(String ivUser, Integer filesystemId) throws NovaException;

    /**
     * Gets all the filesystem list of the product by environment
     *
     * @param productId      the product id where getting the filesystems
     * @param environment    the environment to filter
     * @param filesystemType a string representing a {@link FilesystemType} with the type of filesystems to filter
     * @return a filesystemDTO array instance
     * @throws NovaException if any error expected happens
     */
    FilesystemDto[] getProductFilesystems(final Integer productId, final String environment, final String filesystemType) throws NovaException;

    /**
     * Delete filesystem file
     *
     * @param data         file to delete
     * @param ivUser       BBVA user code
     * @param filesystemId filesystem id
     * @throws NovaException if any error expected happens
     */
    void deleteFile(final FSFileLocationModel data, final String ivUser, final Integer filesystemId) throws NovaException;

    /**
     * Frozen environment validation
     *
     * @param filesystemId filesystemID
     * @return true if environment is frozen, in other case false
     * @throws NovaException if any error expected happens
     */
    Boolean isFilesystemFrozen(Integer filesystemId) throws NovaException;

    /**
     * Create directory in given filesystem
     *
     * @param ivUser       BBVA user code
     * @param filesystemId filesystem id
     * @param directory    directory to create
     * @throws NovaException if any error expected happens
     */
    void createDirectory(String ivUser, Integer filesystemId, String directory) throws NovaException;

    /**
     * Filesystem usage
     *
     * @param filesystemId filesystem
     * @return the percentage of use of the filesystem
     * @throws NovaException if any error expected happens
     */
    FSFilesystemUsage getFilesystemUsage(Integer filesystemId) throws NovaException;

    /**
     * Get filesystem files according given filters
     *
     * @param filesystemId filesystem id
     * @param filterPath   path filter
     * @param filename     filename filter
     * @param numberPage
     * @param sizePage
     * @param field
     * @param order
     * @return Array with files
     * @throws NovaException if any error expected happens
     */
    FSFileModelPaged getFiles(Integer filesystemId, String filterPath, String filename, Integer numberPage, Integer sizePage, String field, String order) throws NovaException;

    /**
     * Gets the list of available filesystem types of a product in an environment. The available filesystem types depend on the enabled platforms for deployment
     *
     * @param productId   product id
     * @param environment environment
     * @return Array of {@link FilesystemTypeDto}
     * @throws NovaException if any error expected happens
     */
    FilesystemTypeDto[] getAvailableFilesystemTypes(Integer productId, String environment) throws NovaException;

    /**
     * Update filesystem quota.
     *
     * @param filesystemId       the filesystem id
     * @param filesystemPackCode the filesystem pack code
     * @param ivUser             the user
     */
    void updateFilesystemQuota(Integer filesystemId, String filesystemPackCode, String ivUser);

    /**
     * Get filesystem id
     *
     * @param path         Landing zone path
     * @param environment  The environment of filesystem
     * @return id filesystem
     * @throws NovaException if any error expected happens
     */
    Integer getFilesystemId(final String path, final String environment);

    /**
     * Get the possible values that can take the field Status (enum {@link FilesystemStatus}).
     *
     * @return The possible values that can take the field Status (enum {@link FilesystemStatus}).
     */
    String[] getFilesystemsStatuses();

    /**
     * Get the possible values that can take the field type (enum {@link FilesystemType}).
     *
     * @return The possible values that can take the field type (enum {@link FilesystemType}).
     */
    String[] getFilesystemsTypes();

    /**
     * Get general filesystems usage info for products.
     *
     * @param productId     the product id
     * @param environment   The environment. If it's empty or absent, it's equals to 'ALL'.
     *
     * @return              General filesystem usage info for products.
     */
    FilesystemsUsageReportDTO getFilesystemsUsageReport(Integer productId, String environment);

    /**
     * Gets filesystems transfer information.
     *
     * @param productId      the product id
     * @param filesystemId   the filesystem id
     * @return the filesystems transfer information
     */
    FFilesystemRelatedTransferInfoDTO[] getFilesystemsTransferInformation( Integer productId, Integer filesystemId);
}
