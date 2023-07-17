package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface for Filsystem validation logic.
 *
 * @author XE63267
 */
public interface IFilesystemsValidator
{
    /**
     * Check if filesystem is available. If not available, throws exception
     *
     * @param filesystem the filesystem instance to check
     * @throws NovaException if any error
     */
    void checkIfFilesystemIsAvailable(Filesystem filesystem) throws NovaException;

    /**
     * Perform validations on {@link Filesystem} creation and addition to a {@link Product}.
     * Product existence.
     * Duplicated filesystem name on product.
     * Product budget on filesystems.
     *
     * @param product         {@link Product} ID.
     * @param filesystemToAdd {@link CreateNewFilesystemDto}
     * @throws NovaException if any error
     */
    void validateFilesystemCreation(Product product, CreateNewFilesystemDto filesystemToAdd) throws NovaException;

    /**
     * Perform validations for restoring an archived {@link Filesystem}.
     * Name uniqueness is not checked since it is checked on creation
     * no matters the {@link FilesystemStatus} status.
     *
     * @param filesystem {@link Filesystem}
     * @throws NovaException if any error
     */
    void validateRestoreFilesystem(Filesystem filesystem) throws NovaException;

    /**
     * Checks if the given {@link Filesystem} does exist or not.
     *
     * @param filesystemId {@link Filesystem} ID.
     * @return the Filesystem
     * @throws NovaException if any error
     */
    Filesystem validateAndGetFilesystem(Integer filesystemId) throws NovaException;


    /**
     * Checks if a {@link Product} existis in NOVA.
     *
     * @param productId {@link Product} ID.
     * @return product product
     * @throws NovaException if any error
     */
    Product validateAndGetProduct(int productId) throws NovaException;

    /**
     * Check if the file name directory(for removing o creating) is reserved by NOVA
     * The filesystem directory name are:
     * --- incoming
     * --- outgoing
     * --- resources
     *
     * @param filesystem          the filesystem
     * @param fsFileLocationModel the file name and the path
     * @throws NovaException if any error
     */
    void validateReservedDirectories(Filesystem filesystem, FSFileLocationModel fsFileLocationModel) throws NovaException;

    /**
     * Check if the path of new directory is a reserved directory or inside a reserved one by NOVA
     *
     * @param filesystem         the filesystem
     * @param newDirectoryPath   the new directory path
     */
    void validateNewDirectoryInsideReservedDirectory(final Filesystem filesystem, final String newDirectoryPath);

    /**
     * Check if the filesystem stores objects
     *
     * @param filesystem the filesystem
     * @param action     the action to be performed
     * @throws NovaException if the filesystem stores objects
     */
    void validateOperationFilesystemObjects(Filesystem filesystem, String action) throws NovaException;

    /**
     * Check if the path of a new file is a reserved directory or inside a reserved one by NOVA
     *
     * @param filesystem   the filesystem
     * @param newFilePath  the new file path
     */
    void validateNewFileInsideReservedDirectory(final Filesystem filesystem, final String newFilePath);

    /**
     * Validate a directory to create
     *
     * @param directory the directory to create
     * @return validated directory to create
     */
    String validateDirectory(String directory);


    /**
     * Check if a filesystem can be managed by user
     *
     * @param filesystem filesystem
     * @return true if cannot be managed by user
     */
    Boolean isFilesystemFrozen(Filesystem filesystem);

    /**
     * Check if filesystem can be maneged by user and throw permissions error
     *
     * @param filesystem filesystem to manage
     * @throws NovaException when fs can not be managed by user
     */
    void checkIfFilesystemIsFrozen(Filesystem filesystem) throws NovaException;

    /**
     * Whether the filesystem is being used by services
     *
     * @param filesystem the filesystem
     * @return true if is in use
     */
    Boolean isFilesystemInUseByServices(Filesystem filesystem);

    /**
     * Check if filesystem is being used by brokers
     *
     * @param filesystemId the filesystem ID
     * @return true if it is used by any broker
     */
    Boolean isFilesystemInUseByBrokers(Integer filesystemId);

    /**
     * Check whether filesysystem in Not being used and throw NovaException otherwise.
     *
     * @param filesystem the filesystem
     * @throws NovaException Filesystem in use
     */
    void validateFilesystemNotInUse(Filesystem filesystem) throws NovaException;

    /**
     * Validate and get pack to update.
     *
     * @param filesystem         the filesystem to update
     * @param filesystemPackCode the filesystem pack code
     * @return the filesystem pack to update
     */
    FilesystemPack validatePackToUpdate(Filesystem filesystem, String filesystemPackCode);

    /**
     * Validate filesystem is Nova type.
     *
     * @param filesystem the filesystem type
     */
    void validateFilesystemIsNovaType(FilesystemType filesystem);

    /**
     * Check filesystem budget.
     *
     * @param filesystem         the filesystem
     * @param filesystemPackCode the filesystem pack code
     */
    void checkFilesystemBudget(Filesystem filesystem, FilesystemPack filesystemPackCode);

    /**
     * Validate filesystem storage.
     *
     * @param filesystem        the filesystem
     * @param newFilesystemPack the new filesystem pack
     */
    void validateFilesystemStorage(Filesystem filesystem, FilesystemPack newFilesystemPack);
}
