package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces;

import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;

import java.util.Optional;

/**
 * Interface of Filesystem alert repository manager
 */
public interface IFilesystemAlertRepositoryManager
{
    /**
     * Save/Update filesystem alert.
     *
     * @param filesystemAlertToSave the filesystem alert to save
     */
    void updateFilesystemAlert(FilesystemAlert filesystemAlertToSave);

    /**
     * Find a filesystem alert by its filesystem id referred
     *
     * @param filesystemId the filesystem id
     * @return the filesystem alert
     */
    Optional<FilesystemAlert> findFilesystemAlertFromFilesystemId(Integer filesystemId);


}
