package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemAlertRepositoryManager;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsAlertBuilder;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsValidator;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl.FilesystemsAlertServiceImpl.PERMISSIONS_EXCEPTION;
import static com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants.FILESYSTEM_MODIFY_ALERT_PERMISSION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesystemsAlertServiceImplTest
{
    @Mock
    private IFilesystemsAlertBuilder filesystemsAlertBuilder;

    @Mock
    private FilesystemRepository filesystemRepository;

    @Mock
    private IFilesystemAlertRepositoryManager filesystemAlertRepositoryManager;

    @Mock
    private IFilesystemsValidator filesystemsValidator;

    @Mock
    private IProductUsersClient usersClient;

    @Mock
    private INovaActivityEmitter activityEmitter;

    @InjectMocks
    private FilesystemsAlertServiceImpl filesystemsAlertService;

    @Test
    void givenMissingFilesystemId_whenGetFilesystemAlertConfiguration_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = null;

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> this.filesystemsAlertService.getFilesystemAlertConfiguration(filesystemId)
        );

        // Then
        assertEquals(FilesystemsError.getNoSuchFilesystemError(filesystemId).getErrorCode(), exception.getErrorCode().getErrorCode());
    }

    @Test
    void givenNonExistingFilesystemAlert_whenGetFilesystemAlertConfiguration_thenNovaExceptionIsThrown()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 100);

        final Optional<FilesystemAlert> mockResult = Optional.empty();

        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemId)).thenReturn(mockResult);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> this.filesystemsAlertService.getFilesystemAlertConfiguration(filesystemId)
        );

        // Then
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemId));
        assertEquals(FilesystemsError.getNotFSAlertError(filesystemId).getErrorCode(), exception.getErrorCode().getErrorCode());
    }

    @Test
    void givenFilesystemId_whenGetFilesystemAlertConfiguration_thenAlertConfigurationIsReturned()
    {
        // Given
        final Integer filesystemId = RandomUtils.nextInt(0, 100);

        final FilesystemAlert alert = new FilesystemAlert();
        final Optional<FilesystemAlert> mockAlert = Optional.of(alert);
        final FSFilesystemAlertInfoDto mockAlertInfo = new FSFilesystemAlertInfoDto();

        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemId)).thenReturn(mockAlert);
        when(filesystemsAlertBuilder.buildFilesystemsAlertInfoDTO(eq(alert))).thenReturn(mockAlertInfo);

        // When
        final FSFilesystemAlertInfoDto result = this.filesystemsAlertService.getFilesystemAlertConfiguration(filesystemId);

        // Then
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemId));
        verify(this.filesystemsAlertBuilder, times(1)).buildFilesystemsAlertInfoDTO(eq(alert));
        assertEquals(mockAlertInfo, result);
    }

    @Test
    void givenNonExistingFilesystemAlert_whenUpdateFilesystemAlertConfiguration_thenNovaExceptionIsThrown()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final FSFilesystemAlertInfoDto alertInfo = new FSFilesystemAlertInfoDto();
        final Integer filesystemId = RandomUtils.nextInt(0, 100);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Integer productId = RandomUtils.nextInt(0, 100);

        final Product product = new Product();
        product.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setEnvironment(environment.getEnvironment());
        mockFilesystem.setProduct(product);
        alertInfo.setIsActive(false);
        alertInfo.setIsPatrol(false);

        final Optional<FilesystemAlert> mockResponse = Optional.empty();

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(eq(filesystemId))).thenReturn(mockResponse);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsAlertService.updateFilesystemAlertConfiguration(ivUser, alertInfo, filesystemId)
        );

        // Then
        verify(this.filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(this.usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_MODIFY_ALERT_PERMISSION), eq(environment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemId));
        assertEquals(FilesystemsError.getNotFSAlertError(filesystemId).getErrorCode(), exception.getErrorCode().getErrorCode());
        verify(this.activityEmitter, times(0)).emitNewActivity(any());
    }

    @Test
    void givenFilesystemAlert_whenUpdateFilesystemAlertConfigurationWithoutException_thenAlerConfigurationIsUpdated() throws JsonProcessingException
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final FSFilesystemAlertInfoDto alertInfo = new FSFilesystemAlertInfoDto();
        alertInfo.setAlertPercentage(50);
        alertInfo.setTimeBetweenAlerts(80);
        alertInfo.setIsActive(true);
        alertInfo.setIsPatrol(false);
        alertInfo.setIsMail(true);
        alertInfo.setEmailAddresses("testing");
        final Integer productId = RandomUtils.nextInt(0, 100);
        final Integer filesystemId = RandomUtils.nextInt(0, 100);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);

        final Product product = new Product();
        product.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(product);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);

        final FilesystemAlert mockedAlert = new FilesystemAlert();
        final Optional<FilesystemAlert> mockedResponse = Optional.of(mockedAlert);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(eq(filesystemId))).thenReturn(mockedResponse);
        when(filesystemsAlertBuilder.buildFilesystemAlert(eq(alertInfo), eq(mockedAlert))).thenReturn(mockedAlert);

        final Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("fileSystemType", mockFilesystem.getType().getFileSystemType());
        activityParams.put("landingZonePath", mockFilesystem.getLandingZonePath());
        activityParams.put("fileSystemName", filesystemName);
        activityParams.put("changeType", "updateFilesystemAlertConfiguration");
        activityParams.put("alertPercent", "50");
        activityParams.put("timeBetweenAlerts", "80");
        activityParams.put("isAlertEnable", "true");
        activityParams.put("isMailEnable", "true");
        activityParams.put("emailAddresses", "testing");

        final String serializedActivityParams = new ObjectMapper().writeValueAsString(activityParams);

        // When
        filesystemsAlertService.updateFilesystemAlertConfiguration(ivUser, alertInfo, filesystemId);

        // Then
        verify(this.filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(this.usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_MODIFY_ALERT_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemId));
        verify(this.filesystemsAlertBuilder, times(1)).buildFilesystemAlert(eq(alertInfo), eq(mockedAlert));
        verify(this.filesystemAlertRepositoryManager, times(1)).updateFilesystemAlert(eq(mockedAlert));

        final ArgumentCaptor<GenericActivity> genericActivityCaptor = ArgumentCaptor.forClass(GenericActivity.class);
        verify(this.activityEmitter, times(1)).emitNewActivity(genericActivityCaptor.capture());
        assertEquals(productId, genericActivityCaptor.getValue().getProductId());
        assertEquals(ActivityScope.FILESYSTEM_EVENT, genericActivityCaptor.getValue().getScope());
        assertEquals(ActivityAction.EDITED, genericActivityCaptor.getValue().getAction());
        assertEquals(filesystemId, genericActivityCaptor.getValue().getEntityId());
        assertEquals(filesystemEnvironment.getEnvironment(), genericActivityCaptor.getValue().getEnvironment());
        assertEquals(serializedActivityParams, genericActivityCaptor.getValue().getSerializedStringParams());
    }

    @Test
    void givenFilesystemAlert_whenUpdateFilesystemAlertConfigurationWithException_thenAlerConfigurationIsNotUpdated()
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final FSFilesystemAlertInfoDto alertInfo = new FSFilesystemAlertInfoDto();
        final Integer productId = RandomUtils.nextInt(0, 100);
        final Integer filesystemId = RandomUtils.nextInt(0, 100);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String filesystemLandingZonePath = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(10);

        final Product product = new Product();
        product.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();
        mockFilesystem.setName(filesystemName);
        mockFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        mockFilesystem.setProduct(product);
        mockFilesystem.setLandingZonePath(filesystemLandingZonePath);
        alertInfo.setIsActive(false);
        alertInfo.setIsPatrol(false);

        final FilesystemAlert mockedAlert = new FilesystemAlert();
        final Optional<FilesystemAlert> mockedResponse = Optional.of(mockedAlert);

        when(filesystemsValidator.validateAndGetFilesystem(eq(filesystemId))).thenReturn(mockFilesystem);
        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(eq(filesystemId))).thenReturn(mockedResponse);
        when(filesystemsAlertBuilder.buildFilesystemAlert(eq(alertInfo), eq(mockedAlert))).thenReturn(mockedAlert);
        doThrow(new RuntimeException("Unexpected exception")).when(filesystemAlertRepositoryManager).updateFilesystemAlert(eq(mockedAlert));

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsAlertService.updateFilesystemAlertConfiguration(ivUser, alertInfo, filesystemId)
        );

        // Then
        verify(this.filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemId));
        verify(this.usersClient, times(1)).checkHasPermission(eq(ivUser), eq(FILESYSTEM_MODIFY_ALERT_PERMISSION), eq(filesystemEnvironment.getEnvironment()), eq(productId), eq(PERMISSIONS_EXCEPTION));
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemId));
        verify(this.filesystemsAlertBuilder, times(1)).buildFilesystemAlert(eq(alertInfo), eq(mockedAlert));
        verify(this.filesystemAlertRepositoryManager, times(1)).updateFilesystemAlert(eq(mockedAlert));
        assertEquals(FilesystemsError.getDatabaseFSAlertError(filesystemId).getErrorCode(), exception.getErrorCode().getErrorCode());
        verify(this.activityEmitter, times(0)).emitNewActivity(any());
    }

    @ParameterizedTest
    @ArgumentsSource(GetAllFilesystemAlerConfigurationBadArgumentsProvider.class)
    void givenBadArguments_whenGetAllFilesystemAlertConfigurations_thenNovaExceptionIsThrown(final Integer productId, final String environment)
    {
        // Given
        final String ivUser = RandomStringUtils.randomAlphanumeric(7);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsAlertService.getAllFilesystemAlertConfigurations(ivUser, productId, environment)
        );

        // Then
        assertEquals(FilesystemsError.getBadArgumentsError().getErrorCode(), exception.getErrorCode().getErrorCode());
    }

    @Test
    void givenValidProductAndEnvironmentAndNullFilesystemsToCheck_whenGetAllFilesystemAlertConfigurations_thenNovaExceptionIsThrown()
    {
        // Given
        final Environment[] validEnvironment = new Environment[]{Environment.INT, Environment.PRE, Environment.PRO};

        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final Integer productId = RandomUtils.nextInt(0, 100);
        final Environment environment = validEnvironment[RandomUtils.nextInt(0, validEnvironment.length)];

        when(filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()))).thenReturn(null);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsAlertService.getAllFilesystemAlertConfigurations(ivUser, productId, environment.getEnvironment())
        );

        // Then
        assertEquals(FilesystemsError.getDatabaseFSError().getErrorCode(), exception.getErrorCode().getErrorCode());
        verify(this.filesystemRepository, times(1)).findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()));
    }

    @Test
    void givenValidProductAndEnvironmentAndEmptyFilesystemsToCheck_whenGetAllFilesystemAlertConfigurations_thenEmptyArrayIsReturned()
    {
        // Given
        final Environment[] validEnvironment = new Environment[]{Environment.INT, Environment.PRE, Environment.PRO};

        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final Integer productId = RandomUtils.nextInt(0, 100);
        final Environment environment = validEnvironment[RandomUtils.nextInt(0, validEnvironment.length)];

        when(filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()))).thenReturn(Collections.emptyList());

        // When
        final FSFilesystemAlertInfoDto[] result = filesystemsAlertService.getAllFilesystemAlertConfigurations(ivUser, productId, environment.getEnvironment());

        // Then
        verify(this.filesystemRepository, times(1)).findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()));
        assertEquals(0, result.length);
    }

    @Test
    void givenValidProductAndEnvironmentAndOneFilesystemsToCheckWithoutAlertConfiguration_whenGetAllFilesystemAlertConfigurations_thenNovaExceptionIsThrown()
    {
        // Given
        final Environment[] validEnvironment = new Environment[]{Environment.INT, Environment.PRE, Environment.PRO};

        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final Integer productId = RandomUtils.nextInt(0, 100);
        final Environment environment = validEnvironment[RandomUtils.nextInt(0, validEnvironment.length)];

        final int filesystemToCheckId = RandomUtils.nextInt(0, 100);
        final Filesystem filesystemToCheck = new FilesystemNova();
        filesystemToCheck.setId(filesystemToCheckId);

        final List<Filesystem> filesystemsToCheck = new ArrayList<>();
        filesystemsToCheck.add(filesystemToCheck);

        when(filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()))).thenReturn(filesystemsToCheck);
        when(filesystemsValidator.validateAndGetFilesystem(filesystemToCheckId)).thenReturn(filesystemToCheck);
        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemToCheckId)).thenReturn(Optional.empty());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsAlertService.getAllFilesystemAlertConfigurations(ivUser, productId, environment.getEnvironment())
        );

        // Then
        assertEquals(FilesystemsError.getNotFSAlertError(filesystemToCheckId).getErrorCode(), exception.getErrorCode().getErrorCode());
        verify(this.filesystemRepository, times(1)).findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()));
        verify(this.filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemToCheckId));
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemToCheckId));
    }

    @Test
    void givenValidProductAndEnvironmentAndOneFilesystemsToCheckWithAlertConfiguration_whenGetAllFilesystemAlertConfigurations_thenOneFilesystemAlertConfigurationIsReturned()
    {
        // Given
        final Environment[] validEnvironment = new Environment[]{Environment.INT, Environment.PRE, Environment.PRO};

        final String ivUser = RandomStringUtils.randomAlphanumeric(7);
        final Integer productId = RandomUtils.nextInt(0, 100);
        final Environment environment = validEnvironment[RandomUtils.nextInt(0, validEnvironment.length)];

        final int filesystemToCheckId = RandomUtils.nextInt(0, 100);
        final Filesystem filesystemToCheck = new FilesystemNova();
        filesystemToCheck.setId(filesystemToCheckId);

        final List<Filesystem> filesystemsToCheck = new ArrayList<>();
        filesystemsToCheck.add(filesystemToCheck);

        final int filesystemAlertId = RandomUtils.nextInt(0, 100);
        final FilesystemAlert filesystemAlert = new FilesystemAlert();
        filesystemAlert.setFilesystemCode(filesystemToCheck);
        filesystemAlert.setId(filesystemAlertId);
        final Optional<FilesystemAlert> optionalFilesystemAlert = Optional.of(filesystemAlert);

        when(filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()))).thenReturn(filesystemsToCheck);
        when(filesystemsValidator.validateAndGetFilesystem(filesystemToCheckId)).thenReturn(filesystemToCheck);
        when(filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemToCheckId)).thenReturn(optionalFilesystemAlert);

        // When
        final FSFilesystemAlertInfoDto[] result = filesystemsAlertService.getAllFilesystemAlertConfigurations(ivUser, productId, environment.getEnvironment());

        // Then
        verify(this.filesystemRepository, times(1)).findByProductIdAndEnvironmentOrderByCreationDateDesc(eq(productId), eq(environment.getEnvironment()));
        assertEquals(filesystemsToCheck.size(), result.length);
        verify(this.filesystemsValidator, times(1)).validateAndGetFilesystem(eq(filesystemToCheckId));
        verify(this.filesystemAlertRepositoryManager, times(1)).findFilesystemAlertFromFilesystemId(eq(filesystemToCheckId));
        verify(this.filesystemsAlertBuilder, times(1)).buildFilesystemsAlertInfoDTO(eq(filesystemAlert));
    }

    static class GetAllFilesystemAlerConfigurationBadArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of(null, null),
                    Arguments.of(null, Environment.INT.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), null),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.LAB_INT.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.LAB_PRO.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.LAB_PRE.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.STAGING_INT.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.STAGING_PRE.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.STAGING_PRO.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.LOCAL.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.PORTAL.getEnvironment()),
                    Arguments.of(RandomUtils.nextInt(0, 100), Environment.PRECON.getEnvironment())
            );
        }
    }
}
