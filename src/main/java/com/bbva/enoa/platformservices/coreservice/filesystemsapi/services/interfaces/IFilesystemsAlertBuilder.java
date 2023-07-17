package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Builder for filesystemAlerts DTO to send and new values set in database
 *
 * @author XE81020
 */
public interface IFilesystemsAlertBuilder
{
    /**
     * Builds an array of {@link FSFilesystemAlertInfoDto} from the {@link FilesystemAlert}
     * of a product.
     *
     * @param filesystemsAlert {@link FilesystemAlert}
     * @return the fs filesystem alert info dto
     * @throws NovaException if any error expected happen
     */
    FSFilesystemAlertInfoDto buildFilesystemsAlertInfoDTO(FilesystemAlert filesystemsAlert) throws NovaException;

    /**
     * Build filesystem alert filesystem alert.
     *
     * @param filesystemAlertInfoDto          the filesystem alert info dto
     * @param oldFilesystemAlertConfiguration the old filesystem alert configuration
     * @return the filesystem alert
     */
    FilesystemAlert buildFilesystemAlert(FSFilesystemAlertInfoDto filesystemAlertInfoDto, FilesystemAlert oldFilesystemAlertConfiguration);

}
