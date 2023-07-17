package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMUsageReportHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemConfigurationDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interface with methods to invoke filesystemmanager
 *
 * @author XE63267
 */
public interface IFilesystemManagerClient
{
    /**
     * Call create filesystem manager to create a new filesystem for a product
     *
     * @param createNewFilesystemDto createNewFilesystemDto
     * @param product                product of the filesystem
     */
    @Transactional
    void callCreateFilesystemManager(CreateNewFilesystemDto createNewFilesystemDto, Product product);

    /**
     * Cal filesystem manager to update the configuration of a filesystem
     *
     * @param filesystem    the filesystem
     * @param configuration the new configuration
     */
    void callUpdateConfigurationFilesystemManager(Filesystem filesystem, FilesystemConfigurationDto[] configuration, String ivUser);

    /**
     * Call getFilesystemToDelete filesystem manager
     *
     * @param filesystem filesystem
     */
    @Transactional(readOnly = true)
    void callDeleteFilesystemManager(Filesystem filesystem);

    /**
     * Calls the FilesystemManager service to archive the given {@link Filesystem}.
     *
     * @param filesystemId {@link Filesystem} ID.
     */
    void callArchiveVolume(int filesystemId);

    /**
     * Calls the FilesystemManager service to restore the given {@link Filesystem}.
     *
     * @param filesystemId {@link Filesystem} ID.
     */
    void callRestoreVolume(int filesystemId);

    /**
     * Call the filesystem manager service to get files from filesystem with given id
     *
     * @param filesystemId filesystemId
     * @param filterPath   path to get files
     * @param filename     files with fiven filename
     * @param numberPage number of page
     * @param sizePage size of page
     * @param field field for order
     * @param order asc or desc
     * @return list of files with given parameters
     */
    FSFileModelPaged callGetFiles(Integer filesystemId, String filterPath, String filename,
                               Integer numberPage, Integer sizePage, String field, String order);

    /**
     * Call the filesystem manager service to delete file from filesystem with given id
     *
     * @param filesystemId        filesystem id
     * @param fsFileLocationModel filesystem file location model
     */
    void callDeleteFile(Integer filesystemId, FSFileLocationModel fsFileLocationModel);

    /**
     * Call filesystem manager service to create a directory from filesystem with given id
     *
     * @param directory    directory to create
     * @param filesystemId filesystem id
     */
    void callCreateDirectory(String directory, Integer filesystemId);

    /**
     * Call filesystem manager service to get filesystem use percentage
     *
     * @param filesystemId filesystem id
     * @return the percentage
     */
    FSFilesystemUsage callGetFileUse(Integer filesystemId);

    /**
     * Call filesystem manager service to update filesystem quota.
     *
     * @param filesystemId       the filesystem id
     * @param filesystemPackCode the filesystem pack code
     */
    void callUpdateQuota(Integer filesystemId, String filesystemPackCode);

    /**
     * Check files from filesystem to previous environment
     *
     * @param filesystemId         filesystem id
     * @param fsmFileLocationModel file data to download to previous environment (previous from filesystem id)
     * @throws NovaException On error
     */
    void checkFileToDownloadToPreviousEnv(Integer filesystemId, FSMFileLocationModel fsmFileLocationModel);

    /**
     * Call filesystem amanger service to get general filesystems usage info for products.
     *
     * @param productId   the product id
     * @param environment The environment. If it's empty or absent, it's equals to 'ALL'.
     * @return General filesystems usage info for products.
     */
    FilesystemsUsageReportDTO callFilesystemsUsageReport(Integer productId, String environment);

    /**
     * Gets an array of DTOs having information for file system usage in statistic history loading.
     *
     * @return an array of DTOs having information for file system usage in statistic history loading.
     */
    FSMUsageReportHistorySnapshotDTO[] getFileSystemUsageHistorySnapshot();
}
