package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemAlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesystemAlertRepositoryManagerTest
{
    @Mock
    private FilesystemAlertRepository filesystemAlertRepository;

    @InjectMocks
    private FilesystemAlertRepositoryManager filesystemAlertRepositoryManager;

    @Test
    void givenFilesystemAlert_whenUpdateFilesystemAlertWithoutExceptions_thenFilesystemAlertIsUpdated()
    {
        // Given
        final Integer filesystemId = 0;

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);

        final Integer alertPercentage = 0;
        final Boolean isActive = Boolean.TRUE;
        final Boolean isMail = Boolean.TRUE;
        final Boolean isPatrol = Boolean.TRUE;
        final Integer timeBetweenAlerts = 0;
        final String emailAddresses = "email";

        final FilesystemAlert filesystemAlert = new FilesystemAlert();
        filesystemAlert.setAlertPercentage(alertPercentage);
        filesystemAlert.setIsActive(isActive);
        filesystemAlert.setIsMail(isMail);
        filesystemAlert.setIsPatrol(isPatrol);
        filesystemAlert.setTimeBetweenAlerts(timeBetweenAlerts);
        filesystemAlert.setFilesystemCode(filesystem);
        filesystemAlert.setEmailAddresses(emailAddresses);

        // When
        this.filesystemAlertRepositoryManager.updateFilesystemAlert(filesystemAlert);

        // Then
        verify(this.filesystemAlertRepository, times(1)).updateFilesystemAlertConfiguration(eq(alertPercentage), eq(isActive), eq(isMail),
                eq(isPatrol), eq(timeBetweenAlerts), eq(emailAddresses), eq(filesystemId));
    }

    @Test
    void givenFilesystemId_whenFindFilesystemAlertFromFilesystemIdWithoutExceptions_thenFilesystemAlertAreFound()
    {
        // Given
        final Integer filesystemId = 0;

        final Optional<FilesystemAlert> mockResult = Optional.empty();

        when(filesystemAlertRepository.findByFilesystemCodeId(eq(filesystemId))).thenReturn(mockResult);

        // When
        final Optional<FilesystemAlert> result = this.filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemId);

        // Then
        verify(this.filesystemAlertRepository, times(1)).findByFilesystemCodeId(eq(filesystemId));
        assertEquals(mockResult, result);
    }
}