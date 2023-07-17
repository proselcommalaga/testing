package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * The interface Filesystems alert service.
 */
public interface IFilesystemsAlertService
{
    /**
     * Get a filesystem alert providing a filesystem id
     *
     * @param filesystemId the filesystem id
     * @return a FilesystemAlertDTO instance
     * @throws NovaException if any error expected happen
     */
    FSFilesystemAlertInfoDto getFilesystemAlertConfiguration(Integer filesystemId) throws NovaException;

    /**
     * Save filesystem alert configuration.
     *
     * @param ivUser                 the iv user
     * @param filesystemAlertInfoDto the filesystem alert info dto
     * @param filesystemId           the filesystem id
     * @throws NovaException if any error expected happen
     */
    void updateFilesystemAlertConfiguration(String ivUser, FSFilesystemAlertInfoDto filesystemAlertInfoDto, Integer filesystemId) throws NovaException;

    /**
     * Save filesystem alert configuration.
     *
     * @param ivUser      the iv user
     * @param productId   the product id
     * @param environment the environment
     * @return the FSFilesystemAlertInfoDto
     * @throws NovaException if any error expected happen
     */
    FSFilesystemAlertInfoDto[] getAllFilesystemAlertConfigurations(String ivUser, Integer productId, String environment) throws NovaException;
}
