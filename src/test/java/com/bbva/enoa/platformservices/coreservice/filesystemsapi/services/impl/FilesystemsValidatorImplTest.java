package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGBudgetDTO;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGProductBudgetsDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEther;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemPackRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFilesystemManagerClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FilesystemsValidatorImplTest
{
    @Mock
    private ProductRepository productRepository;

    @Mock
    private FilesystemRepository filesystemRepository;

    @Mock
    private FilesystemPackRepository filesystemPackRepository;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private ManageValidationUtils manageValidationUtils;

    @Mock
    private NovaContext novaContext;

    @Mock
    private IBudgetsService budgetsService;

    @Mock
    private IFilesystemManagerClient filesystemManagerClient;

    @InjectMocks
    private FilesystemsValidatorImpl filesystemsValidator;

    @Before
    void setUp()
    {
        ReflectionTestUtils.setField(this.filesystemsValidator, "maxAllowedFilesystemsPerEnvAndType", 2);
    }

    @ParameterizedTest
    @ArgumentsSource(GetAvailableFilesystemStatusProvider.class)
    void givenFilesystemAvailable_whenCheckIfFilesystemIsAvailable_thenFilesystemAvailabilityIsCheckedWithoutException(final FilesystemStatus status)
    {
        // Given
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setFilesystemStatus(status);

        // When
        filesystemsValidator.checkIfFilesystemIsAvailable(filesystem);

        // Then
        // Nothing to assert
    }

    @ParameterizedTest
    @ArgumentsSource(GetUnavailableFilesystemStatusProvider.class)
    void givenFilesystemUnavailable_whenCheckIfFilesystemIsAvailable_thenFilesystemAvailabilityIsCheckedWithoutException(final FilesystemStatus status)
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setFilesystemStatus(status);

        final NovaError expectedError = FilesystemsError.getFilesystemNotAvailableViewError(filesystemId);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.checkIfFilesystemIsAvailable(filesystem)
        );

        // Then
        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductAndFilesystem_whenValidateFilesystemCreation_thenFilesystemCreationIsValidated()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);
        final Product product = new Product();
        product.setId(productId);

        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String      filesystemName        = RandomStringUtils.randomAlphanumeric(15);
        final String filesystemPackCode = RandomStringUtils.randomAlphanumeric(15);
        final FilesystemType filesystemType = FilesystemType.values()[RandomUtils.nextInt(0, FilesystemType.values().length)];
        final CreateNewFilesystemDto newFilesystem = new CreateNewFilesystemDto();
        newFilesystem.setFilesystemName(filesystemName);
        newFilesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        newFilesystem.setFilesystemPackCode(filesystemPackCode);
        newFilesystem.setFilesystemType(filesystemType.getFileSystemType());

        final int mockFilesystemPackId = RandomUtils.nextInt(0, 100);
        final FilesystemPack mockFilesystemPack = new FilesystemPack();
        mockFilesystemPack.setId(mockFilesystemPackId);

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doNothing().when(spyFilesystemsValidator).validateFilesystemStatusAndProduct(eq(product));
        doNothing().when(spyFilesystemsValidator).validateNameUniqueness(eq(product), eq(filesystemEnvironment), eq(filesystemName));
        doReturn(mockFilesystemPack).when(spyFilesystemsValidator).validateFilesystemPackCode(eq(filesystemPackCode));
        doNothing().when(spyFilesystemsValidator).validateFilesystemBudget(eq(productId), eq(filesystemEnvironment.getEnvironment()), eq(mockFilesystemPackId));
        doNothing().when(spyFilesystemsValidator).validateMaxPerEnvAndType(eq(product), eq(filesystemEnvironment), eq(filesystemType));
        doNothing().when(spyFilesystemsValidator).validateLandingZoneUniqueness(eq(product), eq(filesystemEnvironment), eq(filesystemName));

        // When
        spyFilesystemsValidator.validateFilesystemCreation(product, newFilesystem);

        // Then
        verify(spyFilesystemsValidator, times(1)).validateFilesystemStatusAndProduct(eq(product));
        verify(spyFilesystemsValidator, times(1)).validateNameUniqueness(eq(product), eq(filesystemEnvironment), eq(filesystemName));
        verify(spyFilesystemsValidator, times(1)).validateFilesystemPackCode(eq(filesystemPackCode));
        verify(spyFilesystemsValidator, times(1)).validateFilesystemBudget(eq(productId), eq(filesystemEnvironment.getEnvironment()), eq(mockFilesystemPackId));
        verify(spyFilesystemsValidator, times(1)).validateMaxPerEnvAndType(eq(product), eq(filesystemEnvironment), eq(filesystemType));
        verify(spyFilesystemsValidator, times(1)).validateLandingZoneUniqueness(eq(product), eq(filesystemEnvironment), eq(filesystemName));
    }

    @Test
    void givenFilesystem_whenValidateRestoreFilesystem_thenFilesystemRestoreIsValidated()
    {
        // Given
        final int filesystemProductId = RandomUtils.nextInt(0, 100);
        final Product filesystemProduct = new Product();
        filesystemProduct.setId(filesystemProductId);

        final int filesystemFilesystemPackId = RandomUtils.nextInt(0, 100);
        final FilesystemPack filesystemFilesystemPack = new FilesystemPack();
        filesystemFilesystemPack.setId(filesystemFilesystemPackId);

        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final FilesystemType filesystemType = FilesystemType.FILESYSTEM;
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setName(filesystemName);
        filesystem.setProduct(filesystemProduct);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setFilesystemPack(filesystemFilesystemPack);

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doNothing().when(spyFilesystemsValidator).validateFilesystemBudget(eq(filesystemProductId), eq(filesystemEnvironment.getEnvironment()), eq(filesystemFilesystemPackId));
        doNothing().when(spyFilesystemsValidator).validateMaxPerEnvAndType(eq(filesystemProduct), eq(filesystemEnvironment), eq(filesystemType));
        doNothing().when(spyFilesystemsValidator).validateLandingZoneUniqueness(eq(filesystemProduct), eq(filesystemEnvironment), eq(filesystemName));

        // When
        spyFilesystemsValidator.validateRestoreFilesystem(filesystem);

        // Then
        verify(spyFilesystemsValidator, times(1)).validateFilesystemBudget(eq(filesystemProductId), eq(filesystemEnvironment.getEnvironment()), eq(filesystemFilesystemPackId));
        verify(spyFilesystemsValidator, times(1)).validateMaxPerEnvAndType(eq(filesystemProduct), eq(filesystemEnvironment), eq(filesystemType));
        verify(spyFilesystemsValidator, times(1)).validateLandingZoneUniqueness(eq(filesystemProduct), eq(filesystemEnvironment), eq(filesystemName));
    }

    @Test
    void givenFilesystemId_whenValidateAndGetFilesystemWithoutException_thenFilesystemIsReturned()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);

        final Optional<Filesystem> mockFilesystem = Optional.of(filesystem);

        when(filesystemRepository.findById(eq(filesystemId))).thenReturn(mockFilesystem);

        // When
        final Filesystem result = filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Then
        verify(filesystemRepository, times(1)).findById(eq(filesystemId));

        assertEquals(result, filesystem);
    }

    @Test
    void givenFilesystemId_whenValidateAndGetFilesystemWithException_thenExceptionIsThrown()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);

        final Optional<Filesystem> mockFilesystem = Optional.empty();

        when(filesystemRepository.findById(eq(filesystemId))).thenReturn(mockFilesystem);

        final NovaError expectedError = FilesystemsError.getNoSuchFilesystemError(filesystemId);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateAndGetFilesystem(filesystemId)
        );

        // Then
        verify(filesystemRepository, times(1)).findById(eq(filesystemId));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductId_whenValidateAndGetProductWithoutException_thenProductIsReturned()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);

        final Product product = new Product();
        product.setId(productId);

        final Optional<Product> mockProduct = Optional.of(product);

        when(productRepository.findById(eq(productId))).thenReturn(mockProduct);

        // When
        final Product result = filesystemsValidator.validateAndGetProduct(productId);

        // Then
        verify(productRepository, times(1)).findById(eq(productId));

        assertEquals(result, product);
    }

    @Test
    void givenProductId_whenValidateAndGetProductWithException_thenExceptionIsThrown()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 100);

        final Optional<Product> mockProduct = Optional.empty();

        when(productRepository.findById(eq(productId))).thenReturn(mockProduct);

        final NovaError expectedError = FilesystemsError.getNoSuchProductError(productId);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateAndGetProduct(productId)
        );

        // Then
        verify(productRepository, times(1)).findById(eq(productId));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @ParameterizedTest
    @ArgumentsSource(GetReservedDirectoriesProvider.class)
    void givenFilesystemAndFileLocationPointingToReservedDirectory_whenValidateReservedDirectories_thenExceptionIsThrown(final String path, final String filename)
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String landingZonePath = "/TEST/fsname";
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setLandingZonePath(landingZonePath);

        final FSFileLocationModel fileLocation = new FSFileLocationModel();
        fileLocation.setPath(path);
        fileLocation.setFilename(filename);

        final NovaError expectedError = FilesystemsError.getNotAllowedModifyDirectoriesError(filesystemId, filename);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateReservedDirectories(filesystem, fileLocation)
        );

        // Then
        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemAndFileLocationWithDifferentLandingZonePath_whenValidateReservedDirectories_thenValidated()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String landingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setLandingZonePath(landingZonePath);

        final String fileLocationPath = RandomStringUtils.randomAlphanumeric(25);
        final String fileLocationFilename = RandomStringUtils.randomAlphanumeric(15);
        final FSFileLocationModel fileLocation = new FSFileLocationModel();
        fileLocation.setPath(fileLocationPath);
        fileLocation.setFilename(fileLocationFilename);

        // When
        filesystemsValidator.validateReservedDirectories(filesystem, fileLocation);

        // Then
        // Nothing to assert
    }

    @Test
    void givenFilesystemAndFileLocationWithValidPath_whenValidateReservedDirectories_thenValidated()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String landingZonePath = RandomStringUtils.randomAlphanumeric(25);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setLandingZonePath(landingZonePath);

        final String fileLocationFilename = RandomStringUtils.randomAlphanumeric(15);
        final FSFileLocationModel fileLocation = new FSFileLocationModel();
        fileLocation.setPath(landingZonePath);
        fileLocation.setFilename(fileLocationFilename);

        // When
        filesystemsValidator.validateReservedDirectories(filesystem, fileLocation);

        // Then
        // Nothing to assert
    }

    @Test
    void givenDirectory_whenValidateDirectory_thenDirectoryIsValidated()
    {
        // Given
        final String directoryTrimmed = RandomStringUtils.randomAlphanumeric(25);
        final String directory = "    " + directoryTrimmed + "    ";

        // Then
        final String result = filesystemsValidator.validateDirectory(directory);

        // Then
        assertEquals(directoryTrimmed, result);
    }

    @ParameterizedTest
    @ArgumentsSource(GetEnvironmentsDifferentToPreProvider.class)
    void givenFilesystemNotInPre_whenIsFilesystemFrozen_thenFilesystemIsNotFrozen(final Environment environment)
    {
        // Given
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setEnvironment(environment.getEnvironment());

        // When
        final Boolean result = filesystemsValidator.isFilesystemFrozen(filesystem);

        // Then
        verify(manageValidationUtils, times(0)).checkIfPlanCanBeManagedByUser(any(), any());

        assertFalse(result);
    }

    @Test
    void givenFilesystemInPreDeployedAndManageableByUser_whenIsFilesystemFrozen_thenFilesystemIsNotFrozen()
    {
        // Given
        final DeploymentPlan filesystemDeploymentPlan = new DeploymentPlan();
        filesystemDeploymentPlan.setStatus(DeploymentStatus.DEPLOYED);

        final DeploymentSubsystem filesystemDeploymentSubsystem = new DeploymentSubsystem();
        filesystemDeploymentSubsystem.setDeploymentPlan(filesystemDeploymentPlan);

        final DeploymentService filesystemDeploymentService = new DeploymentService();
        filesystemDeploymentService.setDeploymentSubsystem(filesystemDeploymentSubsystem);

        final DeploymentServiceFilesystem filesystemDeploymentServiceFilesystem = new DeploymentServiceFilesystem();
        filesystemDeploymentServiceFilesystem.setDeploymentService(filesystemDeploymentService);

        final List<DeploymentServiceFilesystem> filesystemDeploymentServiceList = Collections.singletonList(filesystemDeploymentServiceFilesystem);

        final Environment filesystemEnvironment = Environment.PRE;
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setDeploymentServiceFilesystems(filesystemDeploymentServiceList);

        final String ivUser = RandomStringUtils.randomAlphanumeric(10);

        when(novaContext.getIvUser()).thenReturn(ivUser);
        when(manageValidationUtils.checkIfPlanCanBeManagedByUser(eq(ivUser), eq(filesystemDeploymentPlan))).thenReturn(Boolean.TRUE);

        // When
        final Boolean result = filesystemsValidator.isFilesystemFrozen(filesystem);

        // Then
        verify(manageValidationUtils, times(1)).checkIfPlanCanBeManagedByUser(eq(ivUser), eq(filesystemDeploymentPlan));

        assertFalse(result);
    }

    @Test
    void givenFilesystemInPreDeployedAndNotManageableByUser_whenIsFilesystemFrozen_thenFilesystemIsNotFrozen()
    {
        // Given
        final DeploymentPlan filesystemDeploymentPlan = new DeploymentPlan();
        filesystemDeploymentPlan.setStatus(DeploymentStatus.DEPLOYED);

        final DeploymentSubsystem filesystemDeploymentSubsystem = new DeploymentSubsystem();
        filesystemDeploymentSubsystem.setDeploymentPlan(filesystemDeploymentPlan);

        final DeploymentService filesystemDeploymentService = new DeploymentService();
        filesystemDeploymentService.setDeploymentSubsystem(filesystemDeploymentSubsystem);

        final DeploymentServiceFilesystem filesystemDeploymentServiceFilesystem = new DeploymentServiceFilesystem();
        filesystemDeploymentServiceFilesystem.setDeploymentService(filesystemDeploymentService);

        final List<DeploymentServiceFilesystem> filesystemDeploymentServiceList = Collections.singletonList(filesystemDeploymentServiceFilesystem);

        final Environment filesystemEnvironment = Environment.PRE;
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setDeploymentServiceFilesystems(filesystemDeploymentServiceList);

        final String ivUser = RandomStringUtils.randomAlphanumeric(10);

        when(novaContext.getIvUser()).thenReturn(ivUser);
        when(manageValidationUtils.checkIfPlanCanBeManagedByUser(eq(ivUser), eq(filesystemDeploymentPlan))).thenReturn(Boolean.FALSE);

        // When
        final Boolean result = filesystemsValidator.isFilesystemFrozen(filesystem);

        // Then
        verify(manageValidationUtils, times(1)).checkIfPlanCanBeManagedByUser(eq(ivUser), eq(filesystemDeploymentPlan));

        assertTrue(result);
    }

    @Test
    void givenFilesystemInPreNotDeployedAndNotManageableByUser_whenIsFilesystemFrozen_thenFilesystemIsNotFrozen()
    {
        // Given
        final DeploymentPlan filesystemDeploymentPlan = new DeploymentPlan();
        filesystemDeploymentPlan.setStatus(DeploymentStatus.DEFINITION);

        final DeploymentSubsystem filesystemDeploymentSubsystem = new DeploymentSubsystem();
        filesystemDeploymentSubsystem.setDeploymentPlan(filesystemDeploymentPlan);

        final DeploymentService filesystemDeploymentService = new DeploymentService();
        filesystemDeploymentService.setDeploymentSubsystem(filesystemDeploymentSubsystem);

        final DeploymentServiceFilesystem filesystemDeploymentServiceFilesystem = new DeploymentServiceFilesystem();
        filesystemDeploymentServiceFilesystem.setDeploymentService(filesystemDeploymentService);

        final List<DeploymentServiceFilesystem> filesystemDeploymentServiceList = Collections.singletonList(filesystemDeploymentServiceFilesystem);

        final Environment filesystemEnvironment = Environment.PRE;
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setDeploymentServiceFilesystems(filesystemDeploymentServiceList);

        final String ivUser = RandomStringUtils.randomAlphanumeric(10);

        when(novaContext.getIvUser()).thenReturn(ivUser);
        when(manageValidationUtils.checkIfPlanCanBeManagedByUser(eq(ivUser), eq(filesystemDeploymentPlan))).thenReturn(Boolean.FALSE);

        // When
        final Boolean result = filesystemsValidator.isFilesystemFrozen(filesystem);

        // Then
        verify(manageValidationUtils, times(1)).checkIfPlanCanBeManagedByUser(eq(ivUser), eq(filesystemDeploymentPlan));

        assertFalse(result);
    }

    @Test
    void givenFilesystemFrozen_whenCheckIfFilesystemIsFrozen_thenExceptionIsThrown()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setName(filesystemName);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        final String ivUser = RandomStringUtils.randomAlphanumeric(15);

        when(novaContext.getIvUser()).thenReturn(ivUser);

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doReturn(Boolean.TRUE).when(spyFilesystemsValidator).isFilesystemFrozen(eq(filesystem));

        final NovaError expectedError = FilesystemsError.getActionFrozenError(filesystemName, filesystemId, filesystemEnvironment.name(), ivUser);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> spyFilesystemsValidator.checkIfFilesystemIsFrozen(filesystem)
        );

        // Then
        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemNotFrozen_whenCheckIfFilesystemIsFrozen_thenCheckIsOk()
    {
        // Given
        final int filesystemId = RandomUtils.nextInt(0, 100);
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setName(filesystemName);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doReturn(Boolean.FALSE).when(spyFilesystemsValidator).isFilesystemFrozen(eq(filesystem));

        // When
        spyFilesystemsValidator.checkIfFilesystemIsFrozen(filesystem);

        // Then
        // Nothing to assert
    }

    @ParameterizedTest
    @ArgumentsSource(GetFilesystemInUseStatusProvider.class)
    void givenFilesystemInUse_whenIsFilesystemInUse_thenFilesystemIsInUse(final DeploymentStatus filesystemInUseStatus)
    {
        // Given
        final DeploymentPlan filesystemDeploymentPlan = new DeploymentPlan();
        filesystemDeploymentPlan.setStatus(filesystemInUseStatus);

        final DeploymentSubsystem filesystemDeploymentSubsystem = new DeploymentSubsystem();
        filesystemDeploymentSubsystem.setDeploymentPlan(filesystemDeploymentPlan);

        final DeploymentService filesystemDeploymentService = new DeploymentService();
        filesystemDeploymentService.setDeploymentSubsystem(filesystemDeploymentSubsystem);

        final DeploymentServiceFilesystem filesystemDeploymentServiceFilesystem = new DeploymentServiceFilesystem();
        filesystemDeploymentServiceFilesystem.setDeploymentService(filesystemDeploymentService);

        final List<DeploymentServiceFilesystem> filesystemDeploymentServiceList = Collections.singletonList(filesystemDeploymentServiceFilesystem);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setDeploymentServiceFilesystems(filesystemDeploymentServiceList);

        // When
        final Boolean result = filesystemsValidator.isFilesystemInUseByServices(filesystem);

        // Then
        assertTrue(result);
    }

    @Test
    void givenFilesystemNotInUse_whenIsFilesystemInUse_thenFilesystemIsInUse()
    {
        // Given
        final DeploymentPlan filesystemDeploymentPlan = new DeploymentPlan();
        filesystemDeploymentPlan.setStatus(DeploymentStatus.STORAGED);

        final DeploymentSubsystem filesystemDeploymentSubsystem = new DeploymentSubsystem();
        filesystemDeploymentSubsystem.setDeploymentPlan(filesystemDeploymentPlan);

        final DeploymentService filesystemDeploymentService = new DeploymentService();
        filesystemDeploymentService.setDeploymentSubsystem(filesystemDeploymentSubsystem);

        final DeploymentServiceFilesystem filesystemDeploymentServiceFilesystem = new DeploymentServiceFilesystem();
        filesystemDeploymentServiceFilesystem.setDeploymentService(filesystemDeploymentService);

        final List<DeploymentServiceFilesystem> filesystemDeploymentServiceList = Collections.singletonList(filesystemDeploymentServiceFilesystem);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setDeploymentServiceFilesystems(filesystemDeploymentServiceList);

        // When
        final Boolean result = filesystemsValidator.isFilesystemInUseByServices(filesystem);

        // Then
        assertFalse(result);
    }

    @Test
    void givenFilesystemNotInUse_whenValidateFilesystemNotInUse_thenNothingHappens()
    {
        // Given
        final Filesystem filesystem = new FilesystemNova();

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doReturn(Boolean.FALSE).when(spyFilesystemsValidator).isFilesystemInUseByServices(eq(filesystem));

        // When
        spyFilesystemsValidator.validateFilesystemNotInUse(filesystem);

        // Then
        // Nothing to assert
    }

    @Test
    void givenFilesystemInUse_whenValidateFilesystemNotInUse_thenExceptionIsThrown()
    {
        // Given
        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setName(filesystemName);

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doReturn(Boolean.TRUE).when(spyFilesystemsValidator).isFilesystemInUseByServices(eq(filesystem));

        final NovaError expectedError = FilesystemsError.getFilesystemInUseByServicesError(filesystemName);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> spyFilesystemsValidator.validateFilesystemNotInUse(filesystem)
        );

        // Then
        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemAndCorrectPackCode_whenValidatePackToUpdate_thenFilesystemPackIsReturned()
    {
        // Given
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String packCode = RandomStringUtils.randomAlphanumeric(15);

        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setEnvironment(environment.getEnvironment());
        filesystemPack.setCode(packCode);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setFilesystemPack(filesystemPack);

        final FilesystemPack mockFilesystemPack = new FilesystemPack();
        mockFilesystemPack.setCode(packCode);
        mockFilesystemPack.setEnvironment(environment.getEnvironment());

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doReturn(mockFilesystemPack).when(spyFilesystemsValidator).validateFilesystemPackCode(eq(packCode));

        // When
        final FilesystemPack result = spyFilesystemsValidator.validatePackToUpdate(filesystem, packCode);

        // Then
        verify(spyFilesystemsValidator, times(1)).validateFilesystemPackCode(eq(packCode));

        assertEquals(mockFilesystemPack, result);
    }

    @Test
    void givenFilesystemAndIncorrectPackCode_whenValidatePackToUpdate_thenExceptionIsThrown()
    {
        // Given
        final Environment filesystemPackEnvironment = Environment.INT;
        final String packCode = RandomStringUtils.randomAlphanumeric(15);

        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setEnvironment(filesystemPackEnvironment.getEnvironment());
        filesystemPack.setCode(packCode);

        final Filesystem filesystem = new FilesystemNova();
        filesystem.setFilesystemPack(filesystemPack);
        filesystem.setEnvironment(filesystemPackEnvironment.getEnvironment());

        final Environment mockFilesystemPackEnvironment = Environment.PRE;
        final FilesystemPack mockFilesystemPack = new FilesystemPack();
        mockFilesystemPack.setCode(packCode);
        mockFilesystemPack.setEnvironment(mockFilesystemPackEnvironment.getEnvironment());

        final FilesystemsValidatorImpl spyFilesystemsValidator = Mockito.spy(filesystemsValidator);

        doReturn(mockFilesystemPack).when(spyFilesystemsValidator).validateFilesystemPackCode(eq(packCode));

        final NovaError expectedError = FilesystemsError.getFilesystemPackCodeEnvironmentError(mockFilesystemPackEnvironment.getEnvironment(), filesystemPackEnvironment.getEnvironment());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> spyFilesystemsValidator.validatePackToUpdate(filesystem, packCode)
        );

        // Then
        verify(spyFilesystemsValidator, times(1)).validateFilesystemPackCode(eq(packCode));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemNovaType_whenValidateFilesystemIsNovaType_thenNothingHappens()
    {
        // Given
        final FilesystemType filesystemType = FilesystemType.FILESYSTEM;

        // When
        filesystemsValidator.validateFilesystemIsNovaType(filesystemType);

        // Then
        // Nothing to assert
    }

    @ParameterizedTest
    @ArgumentsSource(GetFilesystemNotNovaTypeProvider.class)
    void givenFilesystemNotNovaType_whenValidateFilesystemIsNovaType_thenExceptionIsThrown(final FilesystemType filesystemNotNovaType)
    {
        // Given
        final NovaError expectedError = FilesystemsError.getUpdateQuotaInvalidFilesystemType(filesystemNotNovaType.getFileSystemType());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateFilesystemIsNovaType(filesystemNotNovaType)
        );

        // Then
        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemAndLargerFilesystemPackAndUnavailableBudget_whenCheckFilesystemBudget_thenExceptionIsThrown()
    {
        // Given
        final int newPackSizeMB = RandomUtils.nextInt(512, 1024);
        final int newPackId = RandomUtils.nextInt(1000, 2000);
        final FilesystemPack newPack = new FilesystemPack();
        newPack.setId(newPackId);
        newPack.setSizeMB(newPackSizeMB);

        final int filesystemPackSizeMB = RandomUtils.nextInt(1, 512);
        final int filesystemPackId = RandomUtils.nextInt(0, 1000);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(filesystemPackId);
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final int filesystemProductId = RandomUtils.nextInt(0, 1000);
        final Product filesystemProduct = new Product();
        filesystemProduct.setId(filesystemProductId);

        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setFilesystemPack(filesystemPack);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setProduct(filesystemProduct);

        final double mockPackPrice = RandomUtils.nextDouble(100.0, 200.0);
        final double mockFilesystemPackPrice = RandomUtils.nextDouble(1.0, 50.0);

        when(budgetsService.getFilesystemPackPrice(eq(filesystemPackId))).thenReturn(mockFilesystemPackPrice);
        when(budgetsService.getFilesystemPackPrice(eq(newPackId))).thenReturn(mockPackPrice);

        final double totalAmount = RandomUtils.nextDouble(0.0, 100.0);
        final double availableAmount = RandomUtils.nextDouble(1.0, 50.0);
        final BUDGBudgetDTO mockProductFilesystemBudget = new BUDGBudgetDTO();
        mockProductFilesystemBudget.setTotalAmount(totalAmount);
        mockProductFilesystemBudget.setAvailableAmount(availableAmount);

        final BUDGProductBudgetsDTO mockProductBudget = new BUDGProductBudgetsDTO();
        mockProductBudget.setFilesystemBudget(mockProductFilesystemBudget);

        when(budgetsService.getProductBudgets(eq(filesystemProductId), eq(filesystemEnvironment.getEnvironment()))).thenReturn(mockProductBudget);

        final NovaError expectedError = FilesystemsError.getNoEnoughFilesystemBudgetError(filesystemProductId, newPackId, filesystemEnvironment.getEnvironment());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.checkFilesystemBudget(filesystem, newPack)
        );

        // Then
        verify(budgetsService, times(1)).getFilesystemPackPrice(eq(filesystemPackId));
        verify(budgetsService, times(1)).getFilesystemPackPrice(eq(newPackId));
        verify(budgetsService, times(1)).getProductBudgets(eq(filesystemProductId), eq(filesystemEnvironment.getEnvironment()));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemAndLargerFilesystemPackAndAvailableBudget_whenCheckFilesystemBudget_thenBudgetIsOk()
    {
        // Given
        final int newPackSizeMB = RandomUtils.nextInt(512, 1024);
        final int newPackId = RandomUtils.nextInt(1000, 2000);
        final FilesystemPack newPack = new FilesystemPack();
        newPack.setId(newPackId);
        newPack.setSizeMB(newPackSizeMB);

        final int filesystemPackSizeMB = RandomUtils.nextInt(1, 512);
        final int filesystemPackId = RandomUtils.nextInt(0, 1000);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(filesystemPackId);
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final int filesystemProductId = RandomUtils.nextInt(0, 1000);
        final Product filesystemProduct = new Product();
        filesystemProduct.setId(filesystemProductId);

        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setFilesystemPack(filesystemPack);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());
        filesystem.setProduct(filesystemProduct);

        final double mockPackPrice = RandomUtils.nextDouble(100.0, 200.0);
        final double mockFilesystemPackPrice = RandomUtils.nextDouble(1.0, 50.0);

        when(budgetsService.getFilesystemPackPrice(eq(filesystemPackId))).thenReturn(mockFilesystemPackPrice);
        when(budgetsService.getFilesystemPackPrice(eq(newPackId))).thenReturn(mockPackPrice);

        final double totalAmount = RandomUtils.nextDouble(0.0, 100.0);
        final double availableAmount = RandomUtils.nextDouble(300.0, 500.0);
        final BUDGBudgetDTO mockProductFilesystemBudget = new BUDGBudgetDTO();
        mockProductFilesystemBudget.setTotalAmount(totalAmount);
        mockProductFilesystemBudget.setAvailableAmount(availableAmount);

        final BUDGProductBudgetsDTO mockProductBudget = new BUDGProductBudgetsDTO();
        mockProductBudget.setFilesystemBudget(mockProductFilesystemBudget);

        when(budgetsService.getProductBudgets(eq(filesystemProductId), eq(filesystemEnvironment.getEnvironment()))).thenReturn(mockProductBudget);

        // When
        filesystemsValidator.checkFilesystemBudget(filesystem, newPack);

        // Then
        verify(budgetsService, times(1)).getFilesystemPackPrice(eq(filesystemPackId));
        verify(budgetsService, times(1)).getFilesystemPackPrice(eq(newPackId));
        verify(budgetsService, times(1)).getProductBudgets(eq(filesystemProductId), eq(filesystemEnvironment.getEnvironment()));
    }

    @Test
    void givenFilesystemAndSmallerFilesystemPack_whenCheckFilesystemBudget_thenBudgetIsOk()
    {
        // Given
        final int newPackSizeMB = RandomUtils.nextInt(1, 512);
        final FilesystemPack newPack = new FilesystemPack();
        newPack.setSizeMB(newPackSizeMB);

        final int filesystemPackSizeMB = RandomUtils.nextInt(512, 1024);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final Environment filesystemEnvironment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setFilesystemPack(filesystemPack);
        filesystem.setEnvironment(filesystemEnvironment.getEnvironment());

        // When
        filesystemsValidator.checkFilesystemBudget(filesystem, newPack);

        // Then
        verifyNoInteractions(budgetsService);
    }

    @Test
    void givenFilesystemAndSmalleFilesystemPackAndNotEnoughFreeSpace_whenValidateFilesystemStorage_thenExceptionIsThrown()
    {
        // Given
        final int filesystemPackSizeMB = RandomUtils.nextInt(512, 1024);
        final int filesystemPackId = RandomUtils.nextInt(0, 1000);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(filesystemPackId);
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final int filesystemId = RandomUtils.nextInt(0, 1000);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setFilesystemPack(filesystemPack);

        final int mockFilesystemUsageUsed = RandomUtils.nextInt(256, filesystemPackSizeMB);
        final FSFilesystemUsage mockFilesystemUsage = new FSFilesystemUsage();
        mockFilesystemUsage.setUsed(mockFilesystemUsageUsed + "M");

        final int newPackSizeMB = RandomUtils.nextInt(1, 256);
        final int newPackId = RandomUtils.nextInt(1000, 2000);
        final String newPackCode = RandomStringUtils.randomAlphanumeric(8);
        final FilesystemPack newPack = new FilesystemPack();
        newPack.setId(newPackId);
        newPack.setCode(newPackCode);
        newPack.setSizeMB(newPackSizeMB);

        when(filesystemManagerClient.callGetFileUse(eq(filesystemId))).thenReturn(mockFilesystemUsage);

        final NovaError expectedError = FilesystemsError.getNotEnoughFreeSpaceToUpdateQuotaError(filesystem.getId(), newPackCode);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateFilesystemStorage(filesystem, newPack)
        );

        // Then
        verify(filesystemManagerClient, times(1)).callGetFileUse(eq(filesystemId));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenFilesystemAndSmalleFilesystemPackAndEnoughFreeSpace_whenValidateFilesystemStorage_thenFilesystemStorageIsValidated()
    {
        // Given
        final int newPackSizeMB = RandomUtils.nextInt(256, 512);
        final int newPackId = RandomUtils.nextInt(1000, 2000);
        final FilesystemPack newPack = new FilesystemPack();
        newPack.setId(newPackId);
        newPack.setSizeMB(newPackSizeMB);

        final int filesystemPackSizeMB = RandomUtils.nextInt(512, 1024);
        final int filesystemPackId = RandomUtils.nextInt(0, 1000);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(filesystemPackId);
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final int filesystemId = RandomUtils.nextInt(0, 1000);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setFilesystemPack(filesystemPack);

        final int mockFilesystemUsageUsed = RandomUtils.nextInt(1, newPackSizeMB);
        final FSFilesystemUsage mockFilesystemUsage = new FSFilesystemUsage();
        mockFilesystemUsage.setUsed(mockFilesystemUsageUsed + "M");

        when(filesystemManagerClient.callGetFileUse(eq(filesystemId))).thenReturn(mockFilesystemUsage);

        // When
        filesystemsValidator.validateFilesystemStorage(filesystem, newPack);

        // Then
        verify(filesystemManagerClient, times(1)).callGetFileUse(eq(filesystemId));
    }

    @Test
    void givenFilesystemAndLargerFilesystemPack_whenValidateFilesystemStorage_thenFilesystemStorageIsValidated()
    {
        // Given
        final int newPackSizeMB = RandomUtils.nextInt(512, 1024);
        final int newPackId = RandomUtils.nextInt(1000, 2000);
        final FilesystemPack newPack = new FilesystemPack();
        newPack.setId(newPackId);
        newPack.setSizeMB(newPackSizeMB);

        final int filesystemPackSizeMB = RandomUtils.nextInt(1, 512);
        final int filesystemPackId = RandomUtils.nextInt(0, 1000);
        final FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setId(filesystemPackId);
        filesystemPack.setSizeMB(filesystemPackSizeMB);

        final int filesystemId = RandomUtils.nextInt(0, 1000);
        final Filesystem filesystem = new FilesystemNova();
        filesystem.setId(filesystemId);
        filesystem.setFilesystemPack(filesystemPack);

        // When
        filesystemsValidator.validateFilesystemStorage(filesystem, newPack);

        // Then
        verifyNoInteractions(filesystemManagerClient);
    }

    @Test
    void givenInvalidFilesystemPackCode_whenValidateFilesystemPackCode_thenExceptionIsThrown()
    {
        // Given
        final String packCode = RandomStringUtils.randomAlphanumeric(8);

        when(filesystemPackRepository.findByCode(eq(packCode))).thenReturn(null);

        final NovaError expectedError = FilesystemsError.getFilesystemPackCodeError(packCode);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateFilesystemPackCode(packCode)
        );

        // Then
        verify(filesystemPackRepository, times(1)).findByCode(eq(packCode));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenValidFilesystemPackCode_whenValidateFilesystemPackCode_thenFilesystemPackIsReturned()
    {
        // Given
        final String packCode = RandomStringUtils.randomAlphanumeric(8);

        final FilesystemPack mockFilesystemPack = new FilesystemPack();
        mockFilesystemPack.setCode(packCode);

        when(filesystemPackRepository.findByCode(eq(packCode))).thenReturn(mockFilesystemPack);

        // When
        final FilesystemPack result = filesystemsValidator.validateFilesystemPackCode(packCode);

        // Then
        verify(filesystemPackRepository, times(1)).findByCode(eq(packCode));

        assertEquals(mockFilesystemPack, result);
    }

    @Test
    void givenProductAndEnvironmentAndDuplicatedFilesystemName_whenValidateNameUniqueness_thenExceptionIsThrown()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);

        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);

        when(filesystemRepository.productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(environment.getEnvironment()))).thenReturn(Boolean.TRUE);

        final NovaError expectedError = FilesystemsError.getDuplicatedFilesystemError(productId, environment.name(), filesystemName);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateNameUniqueness(product, environment, filesystemName)
        );

        // Then
        verify(filesystemRepository, times(1)).productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(environment.getEnvironment()));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductAndEnvironmentAndUniqueFilesystemName_whenValidateNameUniqueness_thenFilesystemNameIsValidated()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);

        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final String filesystemName = RandomStringUtils.randomAlphanumeric(15);

        when(filesystemRepository.productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(environment.getEnvironment()))).thenReturn(Boolean.FALSE);


        // When
        filesystemsValidator.validateNameUniqueness(product, environment, filesystemName);

        // Then
        verify(filesystemRepository, times(1)).productHasFilesystemWithSameNameOnEnvironment(eq(productId), eq(filesystemName), eq(environment.getEnvironment()));
    }

    @Test
    void givenProductWithFilesystemCreating_whenValidateFilesystemStatusAndProduct_thenExceptionIsThrown()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);

        final Filesystem mockFilesystem = new FilesystemNova();

        final List<Filesystem> mockFilesystemList = Collections.singletonList(mockFilesystem);

        doReturn(mockFilesystemList).when(filesystemRepository).findByProductIdAndFilesystemStatus(eq(productId), eq(FilesystemStatus.CREATING));

        final NovaError expectedError = FilesystemsError.getFilesystemNotAvailableCreateError(productId);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateFilesystemStatusAndProduct(product)
        );

        // Then
        verify(filesystemRepository, times(1)).findByProductIdAndFilesystemStatus(eq(productId), eq(FilesystemStatus.CREATING));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductWithoutFilesystemCreating_whenValidateFilesystemStatusAndProduct_thenExceptionIsThrown()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);

        doReturn(Collections.emptyList()).when(filesystemRepository).findByProductIdAndFilesystemStatus(eq(productId), eq(FilesystemStatus.CREATING));

        // When
        filesystemsValidator.validateFilesystemStatusAndProduct(product);

        // Then
        verify(filesystemRepository, times(1)).findByProductIdAndFilesystemStatus(eq(productId), eq(FilesystemStatus.CREATING));
    }

    @Test
    void givenProductAndEnvironmentAndFilesystemType_whenValidateMaxPerEnvAndType_thenValidationIsOk()
    {
        // Given
        final Environment environment = Environment.INT;
        final FilesystemType filesystemType = FilesystemType.FILESYSTEM;

        final Filesystem filesystem01 = new FilesystemEther();
        filesystem01.setEnvironment(environment.getEnvironment());

        final Filesystem filesystem02 = new FilesystemNova();
        filesystem02.setFilesystemStatus(FilesystemStatus.CREATE_ERROR);
        filesystem02.setEnvironment(environment.getEnvironment());

        final Filesystem filesystem03 = new FilesystemNova();
        filesystem03.setFilesystemStatus(FilesystemStatus.ARCHIVED);
        filesystem03.setEnvironment(environment.getEnvironment());

        final Filesystem filesystem04 = new FilesystemNova();
        filesystem04.setFilesystemStatus(FilesystemStatus.CREATED);
        filesystem04.setEnvironment(environment.getEnvironment());

        final List<Filesystem> filesystems = Arrays.asList(filesystem01, filesystem02, filesystem03, filesystem04);

        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);
        product.setFilesystems(filesystems);

        filesystemsValidator.setMaxAllowedFilesystemsPerEnvAndType(2);

        // When
        filesystemsValidator.validateMaxPerEnvAndType(product, environment, filesystemType);

        // Then
        // Nothing to assert
    }

    @Test
    void givenProductAndEnvironmentAndFilesystemType_whenValidateMaxPerEnvAndType_thenExceptionIsThrown()
    {
        // Given
        final Environment environment = Environment.INT;
        final FilesystemType filesystemType = FilesystemType.FILESYSTEM;

        final Filesystem filesystem01 = new FilesystemEther();
        filesystem01.setEnvironment(environment.getEnvironment());

        final Filesystem filesystem02 = new FilesystemNova();
        filesystem02.setFilesystemStatus(FilesystemStatus.CREATE_ERROR);
        filesystem02.setEnvironment(environment.getEnvironment());

        final Filesystem filesystem03 = new FilesystemNova();
        filesystem03.setFilesystemStatus(FilesystemStatus.ARCHIVED);
        filesystem03.setEnvironment(environment.getEnvironment());

        final Filesystem filesystem04 = new FilesystemNova();
        filesystem04.setFilesystemStatus(FilesystemStatus.CREATED);
        filesystem04.setEnvironment(environment.getEnvironment());

        final List<Filesystem> filesystems = Arrays.asList(filesystem01, filesystem02, filesystem03, filesystem04);

        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);
        product.setFilesystems(filesystems);

        filesystemsValidator.setMaxAllowedFilesystemsPerEnvAndType(1);

        final NovaError expectedError = FilesystemsError.getTooManyFilesystemsError(productId, environment.name(), filesystemType.name());

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateMaxPerEnvAndType(product, environment, filesystemType)
        );

        // Then
        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductAndEnvironmentAndDuplicateFilesystemName_whenValidateLandingZoneUniqueness_exceptionIsThrown()
    {
        // Given
        final String uuaa = RandomStringUtils.randomAlphanumeric(4);
        final int productId = RandomUtils.nextInt(0, 1000);
        final Product product = new Product();
        product.setId(productId);
        product.setUuaa(uuaa);

        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final String filesystemName = RandomStringUtils.randomAlphanumeric(8);

        final String landingZonePath = "/" + uuaa + "/" + filesystemName;

        when(filesystemRepository.landingZonePathIsUsed(eq(landingZonePath), eq(environment.getEnvironment()))).thenReturn(Boolean.TRUE);

        final NovaError expectedError = FilesystemsError.getDuplicatedLandingZonePathError(productId, environment.name(), filesystemName);

        // When
        final NovaException exception = assertThrows(
                NovaException.class,
                () -> filesystemsValidator.validateLandingZoneUniqueness(product, environment, filesystemName)
        );

        // Then
        verify(filesystemRepository, times(1)).landingZonePathIsUsed(eq(landingZonePath), eq(environment.getEnvironment()));

        assertEquals(expectedError.toString(), exception.getNovaError().toString());
    }

    @Test
    void givenProductAndEnvironmentAndUniqueFilesystemName_whenValidateLandingZoneUniqueness_landingZoneIsValidated()
    {
        // Given
        final String uuaa = RandomStringUtils.randomAlphanumeric(4);
        final Product product = new Product();
        product.setUuaa(uuaa);

        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

        final String filesystemName = RandomStringUtils.randomAlphanumeric(8);

        final String landingZonePath = "/" + uuaa + "/" + filesystemName;

        when(filesystemRepository.landingZonePathIsUsed(eq(landingZonePath), eq(environment.getEnvironment()))).thenReturn(Boolean.FALSE);

        // When
        filesystemsValidator.validateLandingZoneUniqueness(product, environment, filesystemName);

        // Then
        verify(filesystemRepository, times(1)).landingZonePathIsUsed(eq(landingZonePath), eq(environment.getEnvironment()));
    }

    @Test
    void givenProductIdAndEnvironmentAndPackIdAndNotEnoughBudget_whenValidateFilesystemBudget_exceptionIsThrown()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 1000);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final int packId = RandomUtils.nextInt(0, 1000);

        when(budgetsService.checkFilesystemAvailabilityStatus(eq(productId), eq(environment.getEnvironment()), eq(packId))).thenReturn(Boolean.FALSE);

        // When
        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> filesystemsValidator.validateFilesystemBudget(productId, environment.getEnvironment(), packId)
        );

        // Then
        verify(budgetsService, times(1)).checkFilesystemAvailabilityStatus(eq(productId), eq(environment.getEnvironment()), eq(packId));

        assertTrue(exception.getMessage().contains(String.format("[%s]", productId)));
        assertTrue(exception.getMessage().contains(String.format("[%s]", environment.getEnvironment())));
        assertTrue(exception.getMessage().contains(String.format("[%s]", packId)));
    }

    @Test
    void givenProductIdAndEnvironmentAndPackIdAndEnoughBudget_whenValidateFilesystemBudget_filesystenBudgetIsValidated()
    {
        // Given
        final int productId = RandomUtils.nextInt(0, 1000);
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final int packId = RandomUtils.nextInt(0, 1000);

        when(budgetsService.checkFilesystemAvailabilityStatus(eq(productId), eq(environment.getEnvironment()), eq(packId))).thenReturn(Boolean.TRUE);

        // When
        filesystemsValidator.validateFilesystemBudget(productId, environment.getEnvironment(), packId);

        // Then
        verify(budgetsService, times(1)).checkFilesystemAvailabilityStatus(eq(productId), eq(environment.getEnvironment()), eq(packId));
    }

    @ParameterizedTest
    @ArgumentsSource(GetNotReservedPathAndResultProvider.class)
    void testNewDirectoryValidateInsideReservedDirectory_Success(String path)
    {
        Filesystem filesystem = new FilesystemNova();
        filesystem.setName("name");
        filesystem.setLandingZonePath("/TEST/fsname");

        filesystemsValidator.validateNewDirectoryInsideReservedDirectory(filesystem, path);

    }

    @ParameterizedTest
    @ArgumentsSource(GetReservedPathAndResultProvider.class)
    void testNewDirectoryValidateInsideReservedDirectory_WithException(String path)
    {
        Filesystem filesystem = new FilesystemNova();
        filesystem.setName("name");
        filesystem.setLandingZonePath("/TEST/fsname");

        final NovaException exception = assertThrows(NovaException.class,
                () -> filesystemsValidator.validateNewDirectoryInsideReservedDirectory(filesystem, path));

        assertEquals(Constants.FilesystemsErrorConstants.CREATE_DIRECTORY_INSIDE_RESERVED_DIRECTORY_ERROR, exception.getErrorCode().getErrorCode());
    }

    @ParameterizedTest
    @ArgumentsSource(GetNotReservedPathAndResultProvider.class)
    void testNewFileValidateInsideReservedDirectory_Success(String path)
    {
        Filesystem filesystem = new FilesystemNova();
        filesystem.setName("name");
        filesystem.setLandingZonePath("/TEST/fsname");

        filesystemsValidator.validateNewFileInsideReservedDirectory(filesystem, path);

    }

    @ParameterizedTest
    @ArgumentsSource(GetReservedPathAndResultProvider.class)
    void testNewFileValidateInsideReservedDirectory_WithException(String path)
    {
        Filesystem filesystem = new FilesystemNova();
        filesystem.setName("name");
        filesystem.setLandingZonePath("/TEST/fsname");

        final NovaException exception = assertThrows(NovaException.class,
                () -> filesystemsValidator.validateNewFileInsideReservedDirectory(filesystem, path));

        assertEquals(Constants.FilesystemsErrorConstants.UPLOADING_FILE_INSIDE_RESERVED_DIRECTORY_ERROR, exception.getErrorCode().getErrorCode());
    }



    static class GetAvailableFilesystemStatusProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of(FilesystemStatus.CREATED),
                    Arguments.of(FilesystemStatus.CREATE_ERROR),
                    Arguments.of(FilesystemStatus.DELETED),
                    Arguments.of(FilesystemStatus.ARCHIVING),
                    Arguments.of(FilesystemStatus.DELETE_ERROR),
                    Arguments.of(FilesystemStatus.ARCHIVED),
                    Arguments.of(FilesystemStatus.BUSY)
            );
        }
    }

    static class GetUnavailableFilesystemStatusProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of(FilesystemStatus.CREATING),
                    Arguments.of(FilesystemStatus.DELETING)
            );
        }
    }

    static class GetReservedDirectoriesProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of("/TEST/fsname", Constants.FilesystemsLiteralsConstants.OUTGOING_DIRECTORY_NAME),
                    Arguments.of("/TEST/fsname", Constants.FilesystemsLiteralsConstants.INCOMING_DIRECTORY_NAME),
                    Arguments.of("/TEST/fsname", Constants.FilesystemsLiteralsConstants.RESOURCES_DIRECTORY_NAME),
                    Arguments.of("/TEST/fsname/" + Constants.FilesystemsLiteralsConstants.RESOURCES_DIRECTORY_NAME, "/inside"),
                    Arguments.of("/TEST/fsname/" + Constants.FilesystemsLiteralsConstants.RESOURCES_DIRECTORY_NAME, "/inside/another"),
                    Arguments.of("/TEST/fsname/" + Constants.FilesystemsLiteralsConstants.RESOURCES_DIRECTORY_NAME, "//inside/another/")
            );
        }
    }

    static class GetReservedPathAndResultProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of("/TEST/fsname/resources/brokers", true),
                    Arguments.of("/TEST/fsname/resources", true),
                    Arguments.of("/TEST/fsname/resources/brokers/billing", true),
                    Arguments.of("/TEST/fsname/resources/brokers//billing", true),
                    Arguments.of("/TEST/fsname/resources/brokers/billing/", true)
            );
        }
    }

    static class GetNotReservedPathAndResultProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(
                    Arguments.of("/TEST/fsname/incoming/other", false),
                    Arguments.of("/TEST/fsname/other", false),
                    Arguments.of("/TEST/fsname/incoming", false),
                    Arguments.of("/TEST/fsname/outgoing/other", false),
                    Arguments.of("/TEST/fsname/outgoing", false),
                    Arguments.of("/TEST/fsname/resourcesextra", false)
            );
        }
    }

    static class GetEnvironmentsDifferentToPreProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(Environment.values())
                    .filter(e -> e != Environment.PRE)
                    .map(Arguments::of);
        }
    }

    static class GetFilesystemInUseStatusProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(DeploymentStatus.values())
                    .filter(e -> e != DeploymentStatus.STORAGED)
                    .map(Arguments::of);
        }
    }

    static class GetFilesystemNotNovaTypeProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext extensionContext)
        {
            return Stream.of(FilesystemType.values())
                    .filter(e -> e != FilesystemType.FILESYSTEM)
                    .map(Arguments::of);
        }
    }
}
