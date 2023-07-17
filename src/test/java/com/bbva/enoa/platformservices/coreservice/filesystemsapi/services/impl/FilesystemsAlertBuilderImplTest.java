package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class FilesystemsAlertBuilderImplTest
{
    @InjectMocks
    private FilesystemsAlertBuilderImpl filesystemsAlertBuilder;

    @Test
    void givenFilesystemAlert_whenBuildFilesystemsAlertInfoDTOWithoutExceptions_thenFSFilesystemAlertInfoDtoIsBuilt()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);
        final String      filesystemDescription = RandomStringUtils.randomAlphanumeric(50);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String      productEmail          = RandomStringUtils.randomAlphanumeric(15);
        final Integer alertId = RandomUtils.nextInt(0, 10);
        final Boolean alertIsActive = RandomUtils.nextBoolean();
        final Boolean alertIsMail = RandomUtils.nextBoolean();
        final Boolean alertIsPatrol = RandomUtils.nextBoolean();
        final Integer alertPercentage = RandomUtils.nextInt(0, 100);
        final Integer alertInterval = RandomUtils.nextInt(0, 300);
        final String emailAddresses = RandomStringUtils.randomAlphanumeric(50);

        final Product product = new Product();
        product.setEmail(productEmail);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setDescription(filesystemDescription);
        filesystem.setName(filesystemName);
        filesystem.setProduct(product);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        final FilesystemAlert alert = new FilesystemAlert();
        alert.setFilesystemCode(filesystem);
        alert.setId(alertId);
        alert.setIsActive(alertIsActive);
        alert.setIsMail(alertIsMail);
        alert.setIsPatrol(alertIsPatrol);
        alert.setAlertPercentage(alertPercentage);
        alert.setTimeBetweenAlerts(alertInterval);
        alert.setEmailAddresses(emailAddresses);

        // When
        final FSFilesystemAlertInfoDto result = filesystemsAlertBuilder.buildFilesystemsAlertInfoDTO(alert);

        // Then
        assertEquals(filesystemId, result.getId());
        assertEquals(alertId, result.getEventId());
        assertEquals(filesystemDescription, result.getEventDescription());
        assertEquals(filesystemName, result.getEventName());
        assertEquals(filesystemEnvironment.getEnvironment(), result.getEnvironment());
        assertEquals(alertIsActive, result.getIsActive());
        assertEquals(alertIsMail, result.getIsMail());
        assertEquals(alertIsPatrol, result.getIsPatrol());
        assertEquals(alertPercentage, result.getAlertPercentage());
        assertEquals(emailAddresses, result.getEmailAddresses());
    }

    @Test
    void givenFilesystemAlertWithoutEmailAddresses_whenBuildFilesystemsAlertInfoDTOWithoutExceptions_thenFSFilesystemAlertInfoDtoIsBuilt()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);
        final String filesystemDescription = RandomStringUtils.randomAlphanumeric(50);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String productEmail = RandomStringUtils.randomAlphanumeric(15);
        final Integer alertId = RandomUtils.nextInt(0, 10);
        final Boolean alertIsActive = RandomUtils.nextBoolean();
        final Boolean alertIsMail = RandomUtils.nextBoolean();
        final Boolean alertIsPatrol = RandomUtils.nextBoolean();
        final Integer alertPercentage = RandomUtils.nextInt(0, 100);
        final Integer alertInterval = RandomUtils.nextInt(0, 300);

        final Product product = new Product();
        product.setEmail(productEmail);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setDescription(filesystemDescription);
        filesystem.setName(filesystemName);
        filesystem.setProduct(product);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        final FilesystemAlert alert = new FilesystemAlert();
        alert.setFilesystemCode(filesystem);
        alert.setId(alertId);
        alert.setIsActive(alertIsActive);
        alert.setIsMail(alertIsMail);
        alert.setIsPatrol(alertIsPatrol);
        alert.setAlertPercentage(alertPercentage);
        alert.setTimeBetweenAlerts(alertInterval);

        // When
        final FSFilesystemAlertInfoDto result = filesystemsAlertBuilder.buildFilesystemsAlertInfoDTO(alert);

        // Then
        assertEquals(filesystemId, result.getId());
        assertEquals(alertId, result.getEventId());
        assertEquals(filesystemDescription, result.getEventDescription());
        assertEquals(filesystemName, result.getEventName());
        assertEquals(filesystemEnvironment.getEnvironment(), result.getEnvironment());
        assertEquals(alertIsActive, result.getIsActive());
        assertEquals(alertIsMail, result.getIsMail());
        assertEquals(alertIsPatrol, result.getIsPatrol());
        assertEquals(alertPercentage, result.getAlertPercentage());
        assertEquals(productEmail, result.getEmailAddresses());
    }

    @Test
    void givenNullFilesystemAlert_whenBuildFilesystemsAlertInfoDTOWithoutExceptions_thenNovaExceptionIsThrown()
    {
        // Given
        final FilesystemAlert alert = null;

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> this.filesystemsAlertBuilder.buildFilesystemsAlertInfoDTO(alert)
        );

        // Then
        assertEquals(FilesystemsError.getNotSuchFSAlertIdError().getErrorCode(), exception.getErrorCode().getErrorCode());
    }

    @Test
    void givenFSFilesystemAlertInfoDtoAndFilesystemAlertWithNullLastAlert_whenBuildFilesystemAlert_thenFilesystemAlertIsUpdated()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);
        final String filesystemDescription = RandomStringUtils.randomAlphanumeric(50);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String productEmail = RandomStringUtils.randomAlphanumeric(15);
        final Integer alertId = RandomUtils.nextInt(0, 10);
        final Boolean originalAlertIsActive = RandomUtils.nextBoolean();
        final Boolean originalAlertIsMail = RandomUtils.nextBoolean();
        final Boolean originalAlertIsPatrol = RandomUtils.nextBoolean();
        final Integer originalAlertPercentage = RandomUtils.nextInt(0, 100);
        final Integer originalAlertInterval = RandomUtils.nextInt(0, 300);

        final Boolean newAlertIsActive = !originalAlertIsActive;
        final Boolean newAlertIsMail = !originalAlertIsMail;
        final Boolean newAlertIsPatrol = !originalAlertIsPatrol;
        final Integer newAlertPercentage = originalAlertPercentage + RandomUtils.nextInt(1, 10);
        final Integer newAlertInterval = originalAlertInterval + RandomUtils.nextInt(1, 10);

        final Product product = new Product();
        product.setEmail(productEmail);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setDescription(filesystemDescription);
        filesystem.setName(filesystemName);
        filesystem.setProduct(product);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        final FilesystemAlert alert = new FilesystemAlert();
        alert.setFilesystemCode(filesystem);
        alert.setId(alertId);
        alert.setIsActive(originalAlertIsActive);
        alert.setIsMail(originalAlertIsMail);
        alert.setIsPatrol(originalAlertIsPatrol);
        alert.setAlertPercentage(originalAlertPercentage);
        alert.setTimeBetweenAlerts(originalAlertInterval);
        alert.setLastAlert(null);

        final FSFilesystemAlertInfoDto alertInfo = new FSFilesystemAlertInfoDto();
        alertInfo.setId(filesystemId);
        alertInfo.setEventId(alertId);
        alertInfo.setEmailAddresses(productEmail);
        alertInfo.setEventDescription(filesystemDescription);
        alertInfo.setEventName(filesystemName);
        alertInfo.setEnvironment(filesystemEnvironment.name());
        alertInfo.setIsActive(newAlertIsActive);
        alertInfo.setIsMail(newAlertIsMail);
        alertInfo.setIsPatrol(newAlertIsPatrol);
        alertInfo.setAlertPercentage(newAlertPercentage);
        alertInfo.setTimeBetweenAlerts(newAlertInterval);

        // When
        filesystemsAlertBuilder.buildFilesystemAlert(alertInfo, alert);

        // Then
        assertNotNull(alert.getLastAlert());
        assertEquals(newAlertIsActive, alert.getIsActive());
        assertEquals(newAlertIsMail, alert.getIsMail());
        assertEquals(newAlertIsPatrol, alert.getIsPatrol());
        assertEquals(newAlertPercentage, alert.getAlertPercentage());
        assertEquals(newAlertInterval, alert.getTimeBetweenAlerts());
    }

    @Test
    void givenFSFilesystemAlertInfoDtoAndFilesystemAlertWithNotNullLastAlert_whenBuildFilesystemAlert_thenFilesystemAlertIsUpdated()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 10);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);
        final String filesystemDescription = RandomStringUtils.randomAlphanumeric(50);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String productEmail = RandomStringUtils.randomAlphanumeric(15);
        final Integer alertId = RandomUtils.nextInt(0, 10);
        final Boolean originalAlertIsActive = RandomUtils.nextBoolean();
        final Boolean originalAlertIsMail = RandomUtils.nextBoolean();
        final Boolean originalAlertIsPatrol = RandomUtils.nextBoolean();
        final Integer originalAlertPercentage = RandomUtils.nextInt(0, 100);
        final Integer originalAlertInterval = RandomUtils.nextInt(0, 300);
        final Date alertLastDate = new Date();

        final Boolean newAlertIsActive = !originalAlertIsActive;
        final Boolean newAlertIsMail = !originalAlertIsMail;
        final Boolean newAlertIsPatrol = !originalAlertIsPatrol;
        final Integer newAlertPercentage = originalAlertPercentage + RandomUtils.nextInt(1, 10);
        final Integer newAlertInterval = originalAlertInterval + RandomUtils.nextInt(1, 10);

        final Product product = new Product();
        product.setEmail(productEmail);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setDescription(filesystemDescription);
        filesystem.setName(filesystemName);
        filesystem.setProduct(product);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        final FilesystemAlert alert = new FilesystemAlert();
        alert.setFilesystemCode(filesystem);
        alert.setId(alertId);
        alert.setIsActive(originalAlertIsActive);
        alert.setIsMail(originalAlertIsMail);
        alert.setIsPatrol(originalAlertIsPatrol);
        alert.setAlertPercentage(originalAlertPercentage);
        alert.setTimeBetweenAlerts(originalAlertInterval);
        alert.setLastAlert(alertLastDate);

        final FSFilesystemAlertInfoDto alertInfo = new FSFilesystemAlertInfoDto();
        alertInfo.setId(filesystemId);
        alertInfo.setEventId(alertId);
        alertInfo.setEmailAddresses(productEmail);
        alertInfo.setEventDescription(filesystemDescription);
        alertInfo.setEventName(filesystemName);
        alertInfo.setEnvironment(filesystemEnvironment.name());
        alertInfo.setIsActive(newAlertIsActive);
        alertInfo.setIsMail(newAlertIsMail);
        alertInfo.setIsPatrol(newAlertIsPatrol);
        alertInfo.setAlertPercentage(newAlertPercentage);
        alertInfo.setTimeBetweenAlerts(newAlertInterval);

        // When
        filesystemsAlertBuilder.buildFilesystemAlert(alertInfo, alert);

        // Then
        assertEquals(alertLastDate, alert.getLastAlert());
        assertEquals(newAlertIsActive, alert.getIsActive());
        assertEquals(newAlertIsMail, alert.getIsMail());
        assertEquals(newAlertIsPatrol, alert.getIsPatrol());
        assertEquals(newAlertPercentage, alert.getAlertPercentage());
        assertEquals(newAlertInterval, alert.getTimeBetweenAlerts());
    }
}
