package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsAlertBuilder;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class FilesystemsAlertBuilderImpl implements IFilesystemsAlertBuilder
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemsAlertBuilderImpl.class);

    @Override
    public FSFilesystemAlertInfoDto buildFilesystemsAlertInfoDTO(FilesystemAlert filesystemsAlert) throws NovaException
    {
        LOG.debug("[FilesystemsAlertBuilderImpl] -> [buildFilesystemsAlertDTO]: Building a filesystemAlertInfo DTO  from the filesystem alert: [{}]", filesystemsAlert);
        FSFilesystemAlertInfoDto filesystemAlertInfoDto = new FSFilesystemAlertInfoDto();

        if (filesystemsAlert == null)
        {
            LOG.debug("[FilesystemsAlertBuilderImpl] -> [buildFilesystemsAlertDTOArray]: The filesystem alert can't be null");
            throw new NovaException(FilesystemsError.getNotSuchFSAlertIdError());
        }
        else
        {
            //Values obtained out of FSAlert entity
            filesystemAlertInfoDto.setId(filesystemsAlert.getFilesystemCode().getId());
            filesystemAlertInfoDto.setEventId(filesystemsAlert.getId());
            if (filesystemsAlert.getEmailAddresses() == null || filesystemsAlert.getEmailAddresses().isEmpty())
            {
                filesystemAlertInfoDto.setEmailAddresses(filesystemsAlert.getFilesystemCode().getProduct().getEmail()); // at least the Product email.
            }
            else
            {
                filesystemAlertInfoDto.setEmailAddresses(filesystemsAlert.getEmailAddresses());
            }
            filesystemAlertInfoDto.setEventDescription(filesystemsAlert.getFilesystemCode().getDescription());
            filesystemAlertInfoDto.setEventName(filesystemsAlert.getFilesystemCode().getName());
            filesystemAlertInfoDto.setEnvironment(filesystemsAlert.getFilesystemCode().getEnvironment());

            //Values from FSAlert entity
            filesystemAlertInfoDto.setIsActive(filesystemsAlert.getIsActive());
            filesystemAlertInfoDto.setAlertPercentage(filesystemsAlert.getAlertPercentage());
            filesystemAlertInfoDto.setIsMail(filesystemsAlert.getIsMail());
            filesystemAlertInfoDto.setIsPatrol(filesystemsAlert.getIsPatrol());
            filesystemAlertInfoDto.setTimeBetweenAlerts(filesystemsAlert.getTimeBetweenAlerts());
        }

        LOG.debug("[FilesystemsAlertBuilderImpl] -> [buildFilesystemDTO]: Built the following filesystem alert DTO array: [{}]", filesystemAlertInfoDto);
        return filesystemAlertInfoDto;
    }

    @Override
    public FilesystemAlert buildFilesystemAlert(FSFilesystemAlertInfoDto filesystemAlertInfoDto, FilesystemAlert oldFilesystemAlertConfiguration)
    {
        LOG.debug("[FilesystemsAlertBuilderImpl] -> [buildFilesystemAlert]: Building a filesystem alert from the filesystemAlertInfo DTO: [{}] with Id: [{}]",
                filesystemAlertInfoDto.toString(), oldFilesystemAlertConfiguration.getFilesystemCode().getId());

        //New fields to update in the new configuration
        oldFilesystemAlertConfiguration.setAlertPercentage(filesystemAlertInfoDto.getAlertPercentage());
        oldFilesystemAlertConfiguration.setIsMail(filesystemAlertInfoDto.getIsMail());
        oldFilesystemAlertConfiguration.setIsPatrol(filesystemAlertInfoDto.getIsPatrol());
        oldFilesystemAlertConfiguration.setIsActive(filesystemAlertInfoDto.getIsActive());
        oldFilesystemAlertConfiguration.setEmailAddresses(filesystemAlertInfoDto.getEmailAddresses());

        //Time persisted in milliseconds
        oldFilesystemAlertConfiguration.setTimeBetweenAlerts(filesystemAlertInfoDto.getTimeBetweenAlerts());

        //Only go in, if the alert creating failed during its generation, cause this value could't be null.
        if (oldFilesystemAlertConfiguration.getLastAlert() == null)
        {
            LOG.debug("[FilesystemsAlertBuilderImpl] -> [buildFilesystemAlert]: The alert to update suffer an error during its creation, " +
                    "the default field for last alert generation will be restored.");
            Date defaultDate = new Date();
            defaultDate.setTime(0);
            oldFilesystemAlertConfiguration.setLastAlert(defaultDate);
        }

        return oldFilesystemAlertConfiguration;
    }
}
