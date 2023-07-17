package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerTaskProcessor;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBrokerDeploymentClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrokerOperatorImplTest
{
    private static final double cpus = 2.0;
    private static final int ramInMB = 512;
    private static final Integer FS_ID = 1111;
    private static final Integer PROD_ID = 2222;
    private static final Integer HW_PACK_ID = 3333;
    private static final Integer BROKER_ID = 420;
    private static final String BROKER_NAME = "brokername";
    private static final String BROKER_DESCRIPTION = "Broker Description";
    private static final String IV_USER = "XE70505";
    public static final String GUEST = "guest";
    public static final String MONITORING_URL = "monitoring://url";

    @Mock
    private BrokerBuilderImpl brokerBuilder;

    @Mock
    private BrokerValidatorImpl brokerValidator;

    @Mock
    private IProductBudgetsService budgetsService;

    @Mock
    private IUsersClient usersService;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private IBrokerDeploymentClient brokerDeploymentClient;

    @InjectMocks
    private BrokerOperatorImpl brokerOperator;

    @Mock
    private BrokerAPIToDoTaskService  brokerAPIToDoTaskService;

    @Mock
    private IBrokerTaskProcessor taskProcessor;

    @Mock
    private ManageValidationUtils manageValidationUtils;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(brokerBuilder);
        verifyNoMoreInteractions(brokerValidator);
        verifyNoMoreInteractions(budgetsService);
        verifyNoMoreInteractions(usersService);
        verifyNoMoreInteractions(brokerRepository);
        verifyNoMoreInteractions(brokerDeploymentClient);
    }

    @Nested
    class createBroker
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("broker creation successfully")
        void ok()
        {
            //conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerBuilder.validateAndBuildBrokerEntity(expectedBrokerDTO)).thenReturn(broker);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.createBroker(broker.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker)).thenReturn(expectedBrokerDTO);

            // test
            BrokerDTO actualBrokerDTO = brokerOperator.createBroker(IV_USER, expectedBrokerDTO);

            // asserts
            assertEquals(expectedBrokerDTO, actualBrokerDTO);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.CREATE_BROKER, expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);
            verify(budgetsService).insertBroker(any(), anyInt());
            verify(brokerRepository).saveAndFlush(broker);
            verify(brokerBuilder).validateAndBuildBrokerEntity(expectedBrokerDTO);
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
            verify(brokerDeploymentClient).createBroker(broker.getId());
        }

        @Test
        @DisplayName("broker creation error on deploymentClient")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {

            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO brokerDTO = brokerDTOSupplier.get();
            when(brokerBuilder.validateAndBuildBrokerEntity(brokerDTO)).thenReturn(broker);
            when(brokerDeploymentClient.createBroker(broker.getId())).thenReturn(false);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            doNothing().when(brokerRepository).delete(broker);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.createBroker(IV_USER, brokerDTO)
            );

            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_CREATION_ERROR_CODE, exception.getNovaError().getErrorCode());

            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.CREATE_BROKER, brokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);
            verify(brokerBuilder).validateAndBuildBrokerEntity(brokerDTO);
            verify(brokerRepository).delete(broker);
            verify(brokerRepository).saveAndFlush(broker);
            verify(brokerDeploymentClient).createBroker(broker.getId());
        }

        @Test
        @DisplayName("broker creation error for permissions")
        void throwExceptionPermissions()
        {
            BrokerDTO brokerDTO = brokerDTOSupplier.get();
            // conditions
            doThrow(NovaException.class).when(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.CREATE_BROKER, brokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.createBroker(IV_USER, brokerDTO)
            );

            // asserts
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.CREATE_BROKER, brokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);
        }
    }

    @Nested
    class deleteBroker
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("broker deletion successfully")
        void ok()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.deleteBroker(broker.getId())).thenReturn(true);

            // test
            brokerOperator.deleteBroker(IV_USER, BROKER_ID);

            // asserts
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeDeleted(broker);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.DELETE_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);
            verify(brokerRepository).saveAndFlush(broker);
            verify(brokerDeploymentClient).deleteBroker(broker.getId());
        }

        @Test
        @DisplayName("broker deletion throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {

            // conditions
            // conditions
            Broker broker = brokerSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.deleteBroker(broker.getId())).thenReturn(false);


            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.deleteBroker(IV_USER, BROKER_ID)
            );

            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_DELETION_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeDeleted(broker);

            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.DELETE_BROKER, broker.getEnvironment(), broker.getProduct().getId(), BrokerOperatorImpl.PERMISSION_DENIED);
            verify(brokerRepository, times(2)).saveAndFlush(broker);
            verify(brokerDeploymentClient).deleteBroker(broker.getId());
        }
    }

    @Nested
    class stopBroker
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("broker stop successfully")
        void ok()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.stopBroker(broker.getId())).thenReturn(true);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker)).thenReturn(expectedBrokerDTO);

            // test
            BrokerDTO actualBrokerDTO = brokerOperator.stopBroker(IV_USER, BROKER_ID);

            // asserts
            Assertions.assertEquals(expectedBrokerDTO, actualBrokerDTO);
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeStopped(broker);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.STOP_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);

            verify(brokerRepository).saveAndFlush(broker);
            verify(brokerDeploymentClient).stopBroker(broker.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        @Test
        @DisplayName("broker deletion throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.stopBroker(broker.getId())).thenReturn(false);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.stopBroker(IV_USER, BROKER_ID)
            );
            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_STOP_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeStopped(broker);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.STOP_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);

            verify(brokerDeploymentClient).stopBroker(broker.getId());
            verify(brokerRepository, times(2)).saveAndFlush(broker);
        }
    }

    @Nested
    class startBroker
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("broker start successfully")
        void ok()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.startBroker(broker.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker)).thenReturn(expectedBrokerDTO);

            // test
            BrokerDTO actualBrokerDTO = brokerOperator.startBroker(IV_USER, BROKER_ID);

            // asserts
            Assertions.assertEquals(expectedBrokerDTO, actualBrokerDTO);
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeStarted(broker);

            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.START_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
            verify(brokerRepository).saveAndFlush(broker);
            verify(brokerDeploymentClient).startBroker(broker.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        @Test
        @DisplayName("broker start throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.startBroker(broker.getId())).thenReturn(false);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.startBroker(IV_USER, BROKER_ID)
            );
            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_START_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeStarted(broker);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.START_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);

            verify(brokerDeploymentClient).startBroker(broker.getId());
            verify(brokerRepository, times(2)).saveAndFlush(broker);
        }
    }

    @Nested
    class restartBroker
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        @DisplayName("broker restart successfully")
        void ok()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.restartBroker(broker.getId())).thenReturn(true);
            when(brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker)).thenReturn(expectedBrokerDTO);

            // test
            BrokerDTO actualBrokerDTO = brokerOperator.restartBroker(IV_USER, BROKER_ID);

            // asserts
            Assertions.assertEquals(expectedBrokerDTO, actualBrokerDTO);
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeStopped(broker);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.RESTART_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);
            verify(brokerRepository).saveAndFlush(broker);
            verify(brokerDeploymentClient).restartBroker(broker.getId());
            verify(brokerBuilder).buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
        }

        @Test
        @DisplayName("broker start throwExceptionBrokerDeploymentResultOkFalse")
        void throwExceptionBrokerDeploymentResultOkFalse()
        {
            // conditions
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(brokerRepository.saveAndFlush(broker)).thenReturn(broker);
            when(brokerDeploymentClient.restartBroker(broker.getId())).thenReturn(false);
            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);
            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.restartBroker(IV_USER, BROKER_ID)
            );
            // asserts
            assertEquals(BrokerConstants.BrokerErrorCode.FAILED_BROKER_DEPLOYMENT_RESTART_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerValidator).validateBrokerCanBeStopped(broker);
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
            verify(usersService).checkHasPermission(IV_USER, BrokerConstants.BrokerPermissions.RESTART_BROKER, broker.getEnvironment(), expectedBrokerDTO.getProductId(), BrokerOperatorImpl.PERMISSION_DENIED);

            verify(brokerDeploymentClient).restartBroker(broker.getId());
            verify(brokerRepository, times(2)).saveAndFlush(broker);
        }
    }

    private static class BrokerDTOSupplier implements Supplier<BrokerDTO>
    {

        @Override
        public BrokerDTO get()
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            brokerDTO.setId(BROKER_ID);
            brokerDTO.setName(BROKER_NAME);
            brokerDTO.setDescription(BROKER_DESCRIPTION);
            brokerDTO.setMonitoringUrl(MONITORING_URL);
            brokerDTO.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name());
            brokerDTO.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)].getType());
            brokerDTO.setStatus(BrokerStatus.CREATING.getStatus());
            brokerDTO.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)].getName());
            brokerDTO.setFilesystemId(FS_ID);
            brokerDTO.setProductId(PROD_ID);
            brokerDTO.setHardwarePackId(HW_PACK_ID);
            brokerDTO.setCpu((float) cpus);
            brokerDTO.setMemory(ramInMB);

            return brokerDTO;
        }
    }

    private static class BrokerSupplier implements Supplier<Broker>
    {

        @Override
        public Broker get()
        {
            Broker broker = new Broker();
            Product product = new Product();
            Filesystem filesystem = new FilesystemNova();
            BrokerPack brokerPack = new BrokerPack();
            product.setId(PROD_ID);
            filesystem.setId(FS_ID);
            brokerPack.setId(HW_PACK_ID);
            brokerPack.setNumCPU(cpus);
            brokerPack.setRamMB(ramInMB);
            broker.setProduct(product);
            broker.setHardwarePack(brokerPack);
            broker.setFilesystem(filesystem);
            broker.setCpu(cpus);
            broker.setMemory(ramInMB);
            broker.setId(BROKER_ID);
            broker.setName(BROKER_NAME);
            broker.setDescription(BROKER_DESCRIPTION);
            broker.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
            broker.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)]);
            broker.setStatus(BrokerStatus.CREATING);
            broker.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);

            return broker;
        }
    }

    @Nested
    class onTaskReply
    {
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        Integer taskId = RandomUtils.nextInt(1, 10);

        @Test
        void ok()
        {
            Broker broker = brokerSupplier.get();
            when(brokerRepository.findById(broker.getId())).thenReturn(Optional.of(broker));

            brokerOperator.onTaskReply(taskId, broker.getId());

            verify(brokerRepository).findById(broker.getId());
        }

        @Test
        void throwExceptionBrokerTaskReplyOkFalse()
        {
            Broker broker = brokerSupplier.get();
            when(brokerRepository.findById(broker.getId())).thenThrow(new NovaException(BrokerError.getBrokerNotFoundError(broker.getId()), "Broker [" + broker.getId() + "] not found"));

            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerOperator.onTaskReply(taskId, broker.getId())
            );

            assertEquals("The broker id: [" + broker.getId() + "] was not found in NOVA database", exception.getNovaError().getErrorMessage());
            verify(brokerRepository).findById(broker.getId());
        }

    }
}