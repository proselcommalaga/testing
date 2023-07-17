package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemAlertRepository;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemAlertRepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Service for managing filesystem alert repositories
 *
 * @author XE81020
 */
@Service
public class FilesystemAlertRepositoryManager implements IFilesystemAlertRepositoryManager
{
    /**
     * Filesystem Alert Repository
     */
    private final FilesystemAlertRepository filesystemAlertRepository;

    /**
     * Instantiates a new Filesystem alert repository manager.
     *
     * @param filesystemAlertRepository the filesystem alert repository
     */
    @Autowired
    public FilesystemAlertRepositoryManager(final FilesystemAlertRepository filesystemAlertRepository)
    {
        this.filesystemAlertRepository = filesystemAlertRepository;
    }

    /////////////////////////////////////////////////////////// IMPLEMENTATIONS ////////////////////////////////////////////////////////////////////

    @Override
    @Transactional
    public void updateFilesystemAlert(FilesystemAlert filesystemAlertToSave)
    {
        this.filesystemAlertRepository.updateFilesystemAlertConfiguration(filesystemAlertToSave.getAlertPercentage(), filesystemAlertToSave.getIsActive(),
                filesystemAlertToSave.getIsMail(), filesystemAlertToSave.getIsPatrol(), filesystemAlertToSave.getTimeBetweenAlerts(),
                filesystemAlertToSave.getEmailAddresses(), filesystemAlertToSave.getFilesystemCode().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FilesystemAlert> findFilesystemAlertFromFilesystemId(final Integer filesystemId)
    {
        return this.filesystemAlertRepository.findByFilesystemCodeId(filesystemId);
    }



}


