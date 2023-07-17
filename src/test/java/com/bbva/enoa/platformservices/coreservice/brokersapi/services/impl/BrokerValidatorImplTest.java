package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.QueueBrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.RateThresholdBrokerAlertConfigDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEther;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.datamodel.model.resource.enumerates.HardwarePackType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerNodeOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerNodeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.HardwarePackRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BrokerValidatorImplTest
{

    private static final double cpus = 2.0;
    private static final int ramInMB = 512;
    private static final Integer FS_ID = 1111;
    private static final Integer PROD_ID = 2222;
    private static final Integer HW_PACK_ID = 3333;
    private static final Integer BROKER_ID = 420;
    private static final String BROKER_NAME = "brokername";
    private static final String BROKER_DESCRIPTION = "Broker Description";
    public static final String GUEST = "guest";
    public static final String MONITORING_URL = "monitoring://url";

    private static final String IV_USER = "XE70505";

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FilesystemRepository filesystemRepository;

    @Mock
    private HardwarePackRepository hardwarePackRepository;

    @Mock
    private IProductBudgetsService budgetsService;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private BrokerNodeRepository brokerNodeRepository;

    @InjectMocks
    private BrokerValidatorImpl brokerValidator;

    @Mock
    private ManageValidationUtils manageValidationUtils;

    @Mock
    private BrokerPackRepository brokerPackRepository;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(filesystemRepository);
        verifyNoMoreInteractions(budgetsService);
        verifyNoMoreInteractions(hardwarePackRepository);
        verifyNoMoreInteractions(brokerRepository);
    }

    @Nested
    class validateAndGetBroker
    {
        @Test
        @DisplayName("Broker id not found in database")
        void testValidateAndGetBrokerWithException()
        {
            Integer brokerId = RandomUtils.nextInt(1, 1000);

            when(brokerRepository.findById(brokerId)).thenReturn(Optional.empty());

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateAndGetBroker(brokerId));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NOT_FOUND_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerRepository, Mockito.times(1)).findById(brokerId);
            verifyNoMoreInteractions(brokerRepository);
        }

        @Test
        @DisplayName("Broker id found in database")
        void testValidateAndGetBrokerSuccessfully()
        {
            Integer brokerId = RandomUtils.nextInt(1, 1000);

            Broker broker = new Broker();
            broker.setId(brokerId);
            when(brokerRepository.findById(brokerId)).thenReturn(Optional.of(broker));

            Broker result = brokerValidator.validateAndGetBroker(brokerId);

            assertEquals(result.getId(), brokerId);
            verify(brokerRepository, Mockito.times(1)).findById(brokerId);
            verifyNoMoreInteractions(brokerRepository);
        }
    }

    @Nested
    class validateAndGetBrokerNode
    {
        @Test
        @DisplayName("Broker node id not found in database")
        void testValidateAndGetBrokerWithException()
        {
            Integer brokerNodeId = RandomUtils.nextInt(1, 1000);

            when(brokerNodeRepository.findById(brokerNodeId)).thenReturn(Optional.empty());

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateAndGetBrokerNode(brokerNodeId));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NODE_NOT_FOUND_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerNodeRepository, Mockito.times(1)).findById(brokerNodeId);
            verifyNoMoreInteractions(brokerNodeRepository);
        }

        @Test
        @DisplayName("Broker node id found in database")
        void testValidateAndGetBrokerSuccessfully()
        {
            Integer brokerNodeId = RandomUtils.nextInt(1, 1000);

            BrokerNode node = new BrokerNode();
            node.setId(brokerNodeId);
            when(brokerNodeRepository.findById(brokerNodeId)).thenReturn(Optional.of(node));

            BrokerNode result = brokerValidator.validateAndGetBrokerNode(brokerNodeId);

            assertEquals(result.getId(), brokerNodeId);
            verify(brokerNodeRepository, Mockito.times(1)).findById(brokerNodeId);
            verifyNoMoreInteractions(brokerNodeRepository);
        }
    }

    @Nested
    class validateAndGetBrokerOperation
    {
        @ParameterizedTest
        @DisplayName("Validate invalid broker operation")
        @ValueSource(strings = {"REMOVE", "CREATION"})
        void testValidateAndGetBrokerOperationWithException(String brokerOperation)
        {
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateAndGetBrokerOperation(brokerOperation));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_OPERATION_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @DisplayName("Validate valid broker operation")
        @ValueSource(strings = {"CREATE", "START", "RESTART", "STOP", "DELETE"})
        void testValidateAndGetBrokerOperationSuccessfully(String brokerOperation)
        {
            BrokerOperation result = brokerValidator.validateAndGetBrokerOperation(brokerOperation);

            assertEquals(BrokerOperation.class, result.getClass());
        }
    }

    @Nested
    class validateAndGetBrokerNodeOperation
    {
        @ParameterizedTest
        @DisplayName("Validate invalid broker node operation")
        @ValueSource(strings = {"CREATE", "DELETE"})
        void testValidateAndGetBrokerNodeOperationWithException(String brokerNodeOperation)
        {
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateAndGetBrokerNodeOperation(brokerNodeOperation));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NODE_OPERATION_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @DisplayName("Validate valid broker node operation")
        @ValueSource(strings = {"START", "RESTART", "STOP"})
        void testValidateAndGetBrokerNodeOperationSuccessfully(String brokerNodeOperation)
        {
            BrokerNodeOperation result = brokerValidator.validateAndGetBrokerNodeOperation(brokerNodeOperation);

            assertEquals(BrokerNodeOperation.class, result.getClass());
        }
    }

    @Nested
    class validateAndGetBrokerAdminUser
    {
        @Test
        void ko()
        {
            BrokerUser serviceUser = getBrokerUser(BrokerRole.SERVICE);
            Broker broker = new Broker();
            broker.setUsers(List.of(serviceUser));

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateAndGetBrokerAdminUser(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.USER_ADMIN_NOT_FOUND_IN_BROKER, exception.getNovaError().getErrorCode());
        }

        @Test
        void ok()
        {
            BrokerUser serviceUser = getBrokerUser(BrokerRole.SERVICE);
            BrokerUser adminUser = getBrokerUser(BrokerRole.ADMIN);
            Broker broker = new Broker();
            broker.setUsers(List.of(serviceUser, adminUser));

            BrokerUser result = brokerValidator.validateAndGetBrokerAdminUser(broker);

            assertEquals(BrokerRole.ADMIN, result.getRole());
        }
    }


    @Nested
    class validateBrokerCanBeOperable
    {
        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"RUNNING"}, mode = EnumSource.Mode.EXCLUDE)
        void ok(BrokerStatus brokerStatus)
        {
            Broker broker = new Broker();
            broker.setStatus(brokerStatus);

            NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerCanBeOperable(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.CANT_RETRIEVE_INFORMATION_ON_STOPPED_BROKER_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void ok()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.RUNNING);

            assertDoesNotThrow(() -> brokerValidator.validateBrokerCanBeOperable(broker));
        }
    }

    @Nested
    class validateBrokerDTO
    {
        @BeforeEach
        void setUp()
        {
            ReflectionTestUtils.setField(brokerValidator, "maxBrokersByEnvAndProductLimit", 2);
        }

        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();

        @Test
        void koBrokerTypeValidationsError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            // Forced error
            validBrokerDTO.setType(BrokerType.STREAMING.getType());


            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.UNSUPPORTED_BROKER_TYPE_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koBrokerPlatformValidationsError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());

            //Forced Error
            validBrokerDTO.setPlatform(Platform.ETHER.getName());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.UNSUPPORTED_BROKER_PLATFORM_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koBrokerNameValidationsError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            //Forced Error
            validBrokerDTO.setName("InvalidName6767");

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert

            assertEquals(BrokerConstants.BrokerErrorCode.INVALID_BROKER_NAME_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koByProductNotFoundError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());

            // forced error
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.empty());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(productRepository).findById(validBrokerDTO.getProductId());
            assertEquals(BrokerConstants.BrokerErrorCode.PRODUCT_NOT_FOUND_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koByMaxBrokersReachedError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(new Product()));
            //Forced Error
            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(List.of(new Broker(), new Broker()));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.MAX_BROKERS_BY_ENV_LIMIT_REACHED_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
        }

        @Test
        void koByBrokerNameAlreadyExistInEnvError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(new Product()));
            //Forced Error
            Broker secondBroker = new Broker();
            secondBroker.setName(validBrokerDTO.getName());
            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.singletonList(secondBroker));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NAME_ALREADY_EXISTS_IN_ENV_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void koByInvalidNumberOfNodesWhenMonoCPD(Environment environment)
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setEnvironment(environment.name());
            validBrokerDTO.setNumberOfNodes(3);

            Product product = new Product();
            product.setMultiCPDInPro(false);

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            //Forced Error
            when(brokerRepository.findByProductIdAndEnvironment(PROD_ID, validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.UNSUPPORTED_NUMBER_OF_NODES_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(PROD_ID, validBrokerDTO.getEnvironment());
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"PRE", "PRO"})
        void koByInvalidNumberOfNodesWhenMultiCPD(Environment environment)
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setEnvironment(environment.name());
            validBrokerDTO.setNumberOfNodes(1);

            Product product = new Product();
            product.setMultiCPDInPro(true);

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            //Forced Error
            when(brokerRepository.findByProductIdAndEnvironment(PROD_ID, validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.UNSUPPORTED_NUMBER_OF_NODES_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(PROD_ID, validBrokerDTO.getEnvironment());
        }

        @Test
        void koByNoBrokerWithNameOnEnvironmentPREError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setEnvironment(Environment.PRO.name());

            Product product = new Product();
            product.setMultiCPDInPro(false);

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            //Forced Error
            when(brokerRepository.findByProductIdAndEnvironment(PROD_ID, Environment.PRO.getEnvironment())).thenReturn(Collections.emptyList());
            when(brokerRepository.findByProductIdAndNameAndEnvironment(PROD_ID, validBrokerDTO.getName(), Environment.PRE.getEnvironment())).thenReturn(Optional.empty());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.IMPOSSIBLE_CREATE_DUE_TO_NO_SAME_BROKER, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(PROD_ID, Environment.PRO.getEnvironment());
            verify(brokerRepository).findByProductIdAndNameAndEnvironment(PROD_ID, validBrokerDTO.getName(), Environment.PRE.getEnvironment());
        }

        @Test
        void koByNoSameNumberOfNodesBrokerWithNameOnEnvironmentPRE()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.getType());
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setEnvironment(Environment.PRO.name());
            validBrokerDTO.setNumberOfNodes(2);

            Product product = new Product();
            product.setMultiCPDInPro(false);

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            //Forced Error
            when(brokerRepository.findByProductIdAndEnvironment(PROD_ID, Environment.PRO.getEnvironment())).thenReturn(Collections.emptyList());
            Broker brokerInPre = new Broker();
            brokerInPre.setNumberOfNodes(1);

            when(brokerRepository.findByProductIdAndNameAndEnvironment(PROD_ID, validBrokerDTO.getName(), Environment.PRE.getEnvironment())).thenReturn(Optional.of(brokerInPre));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.NOT_SAME_NUMBER_OF_NODES_AS_PREPRODUCTION_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(PROD_ID, Environment.PRO.getEnvironment());
            verify(brokerRepository).findByProductIdAndNameAndEnvironment(PROD_ID, validBrokerDTO.getName(), Environment.PRE.getEnvironment());
        }

        @Test
        void koByNotEnoughBudgetError()
        {
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());

            Product product = new Product();
            product.setMultiCPDInPro(false);

            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(false);

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            assertEquals(BrokerConstants.BrokerErrorCode.NOT_ENOUGH_BROKER_BUDGET_ERROR, exception.getNovaError().getErrorCode());
            verify(productRepository).findById(PROD_ID);
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
        }

        @Test
        void koByFilesystemNotFoundError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            // forced error
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.empty());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(PROD_ID);
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());

            assertEquals(BrokerConstants.BrokerErrorCode.FILESYSTEM_NOT_FOUND_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koByInvalidFilesystemProductError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            // forced error
            Product product1 = new Product();
            product1.setId(12);
            filesystem.setProduct(product1);
            filesystem.setEnvironment(Environment.valueOf(validBrokerDTO.getEnvironment()).getEnvironment());
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(PROD_ID)).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(PROD_ID);
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());

            assertEquals(BrokerConstants.BrokerErrorCode.INVALID_FILESYSTEM_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koByInvalidFilesystemTypeError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            // forced error;
            Filesystem filesystem = new FilesystemEther();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setEnvironment(Environment.valueOf(validBrokerDTO.getEnvironment()).getEnvironment());
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());

            assertEquals(BrokerConstants.BrokerErrorCode.INVALID_FILESYSTEM_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koByInvalidFilesystemEnvironmentError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);
            // forced error
            filesystem.setEnvironment(Environment.INT.getEnvironment());

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());

            assertEquals(BrokerConstants.BrokerErrorCode.INVALID_FILESYSTEM_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest(name = "[{index}] Filesystem status: {0}")
        @EnumSource(value = FilesystemStatus.class, mode = EnumSource.Mode.EXCLUDE, names = {"CREATED"})
        @DisplayName("Broker validation invalid due to wrong filesystem status")
        void koByInvalidFilesystemStatusError(FilesystemStatus filesystemStatus)
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setEnvironment(validBrokerDTO.getEnvironment());
            // forced error
            filesystem.setFilesystemStatus(filesystemStatus);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());

            assertEquals(BrokerConstants.BrokerErrorCode.INVALID_FILESYSTEM_ERROR_CODE, exception.getNovaError().getErrorCode());
        }


        @Test
        void koByHardwarePackNotFoundError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setEnvironment(Environment.valueOf(validBrokerDTO.getEnvironment()).getEnvironment());
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));
            // forced error
            when(brokerPackRepository.findById(validBrokerDTO.getHardwarePackId())).thenReturn(Optional.empty());

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());
            verify(brokerPackRepository).findById(validBrokerDTO.getHardwarePackId());

            assertEquals(BrokerConstants.BrokerErrorCode.HARDWAREPACK_NOT_FOUND_ERROR_CODE, exception.getNovaError().getErrorCode());
        }


        @Test
        void koByHardwarePackTypeIncorrectForPlatformError()
        {
            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRE.getEnvironment());
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setEnvironment(Environment.valueOf(validBrokerDTO.getEnvironment()).getEnvironment());
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);

            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);
            BrokerPack hardwarePack = new BrokerPack();
            hardwarePack.setId(HW_PACK_ID);
            // Forced error
            hardwarePack.setHardwarePackType(HardwarePackType.PACK_ETHER);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));
            when(brokerPackRepository.findById(validBrokerDTO.getHardwarePackId())).thenReturn(Optional.of(hardwarePack));

            // test
            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerDTO(validBrokerDTO));

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());
            verify(brokerPackRepository).findById(validBrokerDTO.getHardwarePackId());
            verify(brokerPackRepository).findById(validBrokerDTO.getHardwarePackId());

            assertEquals(BrokerConstants.BrokerErrorCode.INVALID_HARDWAREPACK_ERROR_CODE, exception.getNovaError().getErrorCode());
        }


        @ParameterizedTest(name = "[{index}] env: {0}")
        @EnumSource(value = Environment.class, names = {"INT", "PRE"})
        @DisplayName("Broker dto valid for all envs but PRO")
        void okAllEnvsButPro(Environment environment)
        {

            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();
            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(String.valueOf(environment));
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setEnvironment(validBrokerDTO.getEnvironment());
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);
            BrokerPack hardwarePack = new BrokerPack();
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setHardwarePackType(HardwarePackType.PACK_NOVA);
            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));
            when(brokerPackRepository.findById(validBrokerDTO.getHardwarePackId())).thenReturn(Optional.of(hardwarePack));

            // test
            brokerValidator.validateBrokerDTO(validBrokerDTO);

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());
            verify(brokerPackRepository).findById(validBrokerDTO.getHardwarePackId());
        }

        @Test
        @DisplayName("Broker dto valid for PRO")
        void okEnvPro()
        {

            // when
            BrokerDTO validBrokerDTO = brokerDTOSupplier.get();

            validBrokerDTO.setPlatform(Platform.NOVA.getName());
            validBrokerDTO.setType(BrokerType.PUBLISHER_SUBSCRIBER.name());
            validBrokerDTO.setEnvironment(Environment.PRO.getEnvironment());
            validBrokerDTO.setNumberOfNodes(2);
            Product product = new Product();
            product.setId(PROD_ID);
            product.setMultiCPDInPro(false);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            filesystem.setProduct(product);
            filesystem.setEnvironment(Environment.valueOf(validBrokerDTO.getEnvironment()).getEnvironment());
            filesystem.setFilesystemStatus(FilesystemStatus.CREATED);
            BrokerPack hardwarePack = new BrokerPack();
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setHardwarePackType(HardwarePackType.PACK_NOVA);

            Broker brokerInPre = new Broker();
            brokerInPre.setNumberOfNodes(2);

            when(brokerRepository.findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment())).thenReturn(Collections.emptyList());
            when(brokerRepository.findByProductIdAndNameAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getName(), Environment.PRE.getEnvironment())).thenReturn(Optional.of(brokerInPre));
            when(budgetsService.checkBroker(any(), anyInt())).thenReturn(true);
            when(productRepository.findById(validBrokerDTO.getProductId())).thenReturn(Optional.of(product));
            when(filesystemRepository.findById(validBrokerDTO.getFilesystemId())).thenReturn(Optional.of(filesystem));
            when(brokerPackRepository.findById(validBrokerDTO.getHardwarePackId())).thenReturn(Optional.of(hardwarePack));

            // test
            brokerValidator.validateBrokerDTO(validBrokerDTO);

            // assert
            verify(brokerRepository).findByProductIdAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getEnvironment());
            verify(brokerRepository).findByProductIdAndNameAndEnvironment(validBrokerDTO.getProductId(), validBrokerDTO.getName(), Environment.PRE.getEnvironment());
            verify(budgetsService).checkBroker(any(), anyInt());
            verify(productRepository).findById(validBrokerDTO.getProductId());
            verify(filesystemRepository).findById(validBrokerDTO.getFilesystemId());
            verify(brokerPackRepository).findById(validBrokerDTO.getHardwarePackId());
        }
    }

    @Nested
    class validateBrokerCanBeStopped
    {
        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = "RUNNING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Broker cannot be stopped when status is not the expected one")
        void testValidateBrokerCanBeStoppedInUnexpectedStatus(BrokerStatus currentStatus)
        {
            Broker broker = new Broker();
            broker.setStatus(currentStatus);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerCanBeStopped(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_STATUS_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        @DisplayName("Broker can be stopped")
        void testValidateBrokerCanBeStoppedSuccessfully()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.RUNNING);

            DeploymentService deploymentService1 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            DeploymentService deploymentService2 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);

            broker.setDeploymentServices(List.of(deploymentService1, deploymentService2));

            assertDoesNotThrow(() -> brokerValidator.validateBrokerCanBeStopped(broker));
        }
    }

    @Nested
    class validateBrokerCanBeDeleted
    {
        @Test
        void okStoppedStatus()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.STOPPED);

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerCanBeDeleted(broker), "valid state don't throw a exception");
        }

        @Test
        void okDELETE_ERRORStatus()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.DELETE_ERROR);

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerCanBeDeleted(broker), "valid state don't throw a exception");
        }

        @Test
        void okCREATE_ERRORStatus()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.CREATE_ERROR);

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerCanBeDeleted(broker), "valid state dont throw a exception");
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"STOPPED", "CREATE_ERROR", "DELETE_ERROR"}, mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Broker cannot be stopped when status is not the expected one")
        void koUnexpectedCurrentStatusError(BrokerStatus currentStatus)
        {
            Broker broker = new Broker();
            broker.setStatus(currentStatus);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerCanBeDeleted(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_STATUS_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @Test
        void koBrokerUsedByServicesError()
        {
            DeploymentService deploymentService = new DeploymentService();
            Broker broker = new Broker();
            broker.setDeploymentServices(List.of(deploymentService));
            broker.setStatus(BrokerStatus.CREATE_ERROR);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerCanBeDeleted(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.IMPOSSIBLE_STOP_BROKER_USED_BY_DEPLOYMENT_SERVICES_ERROR, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class validateBrokerCanBeStarted
    {
        @Test
        void ok()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.STOPPED);

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerCanBeStarted(broker), "valid state don't throw a exception");
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = "STOPPED", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Broker cannot be stopped when status is not the expected one")
        void koUnexpectedCurrentStatusError(BrokerStatus currentStatus)
        {
            Broker broker = new Broker();
            broker.setStatus(currentStatus);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerCanBeStarted(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_STATUS_ERROR_CODE, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class validateBrokerNodeCanBeStarted
    {
        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"RUNNING", "STOPPED", "STARTING", "STOPPING", "RESTARTING"})
        void ok(BrokerStatus brokerStatus)
        {
            BrokerNode node = new BrokerNode();
            node.setStatus(BrokerStatus.STOPPED);
            Broker broker = new Broker();
            broker.setStatus(brokerStatus);
            node.setBroker(broker);

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerNodeCanBeStarted(node), "valid state don't throw a exception");
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = "STOPPED", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Broker node cannot be stopped when status is not the expected one")
        void koUnexpectedNodeStatus(BrokerStatus nodeStatus)
        {
            BrokerNode node = new BrokerNode();
            node.setStatus(nodeStatus);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerNodeCanBeStarted(node));

            assertEquals(BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_NODE_STATUS_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"RUNNING", "STOPPED", "STARTING", "STOPPING", "RESTARTING"}, mode = EnumSource.Mode.EXCLUDE)
        void koUnexpectedBrokerStatus(BrokerStatus brokerStatus)
        {
            BrokerNode node = new BrokerNode();
            node.setStatus(BrokerStatus.STOPPED);
            Broker broker = new Broker();
            broker.setStatus(brokerStatus);
            node.setBroker(broker);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerNodeCanBeStarted(node));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NOT_CREATED_AND_STABLE_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

    }


    @Nested
    class validateBrokerNodeCanBeStopped
    {
        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"RUNNING", "STOPPED", "STARTING", "STOPPING", "RESTARTING"})
        void okNotLastNodeRunning(BrokerStatus brokerStatus)
        {
            Broker broker = new Broker();
            broker.setStatus(brokerStatus);

            BrokerNode node = new BrokerNode();
            node.setId(1);
            node.setStatus(BrokerStatus.RUNNING);
            node.setBroker(broker);

            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(2);
            siblingNode.setStatus(BrokerStatus.RUNNING);
            siblingNode.setBroker(broker);

            broker.setNodes(List.of(node, siblingNode));

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerNodeCanBeStopped(node), "valid state don't throw a exception");
        }

        @Test
        void okLastNodeRunning()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.RUNNING);

            BrokerNode node = new BrokerNode();
            node.setId(1);
            node.setStatus(BrokerStatus.RUNNING);
            node.setBroker(broker);

            BrokerNode siblingNode = new BrokerNode();
            siblingNode.setId(2);
            siblingNode.setStatus(BrokerStatus.STOPPED);
            siblingNode.setBroker(broker);

            broker.setNodes(List.of(node, siblingNode));

            DeploymentService deploymentService1 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            DeploymentService deploymentService2 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            broker.setDeploymentServices(List.of(deploymentService1, deploymentService2));

            //assert
            assertDoesNotThrow(() -> brokerValidator.validateBrokerNodeCanBeStopped(node), "valid state don't throw a exception");
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = "RUNNING", mode = EnumSource.Mode.EXCLUDE)
        @DisplayName("Broker node cannot be stopped when status is not the expected one")
        void koUnexpectedNodeStatus(BrokerStatus nodeStatus)
        {
            BrokerNode node = new BrokerNode();
            node.setStatus(nodeStatus);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerNodeCanBeStopped(node));

            assertEquals(BrokerConstants.BrokerErrorCode.UNEXPECTED_BROKER_NODE_STATUS_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"RUNNING", "STOPPED", "STARTING", "STOPPING", "RESTARTING"}, mode = EnumSource.Mode.EXCLUDE)
        void koUnexpectedBrokerStatus(BrokerStatus brokerStatus)
        {
            BrokerNode node = new BrokerNode();
            node.setStatus(BrokerStatus.RUNNING);
            Broker broker = new Broker();
            broker.setStatus(brokerStatus);
            node.setBroker(broker);

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerNodeCanBeStopped(node));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NOT_CREATED_AND_STABLE_ERROR_CODE, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class validateAnyNodeIsNotInTransitoryStatus
    {
        @Test
        void ok()
        {
            Broker broker = new Broker();
            BrokerNode node1 = new BrokerNode();
            node1.setStatus(BrokerStatus.RUNNING);
            BrokerNode node2 = new BrokerNode();
            node2.setStatus(BrokerStatus.STOPPED);
            BrokerNode node3 = new BrokerNode();
            node3.setStatus(BrokerStatus.RUNNING);

            broker.setNodes(List.of(node1, node2, node3));

            assertDoesNotThrow(() -> brokerValidator.validateAnyNodeIsNotInTransitoryStatus(broker));
        }

        @ParameterizedTest
        @EnumSource(value = BrokerStatus.class, names = {"STARTING", "RESTARTING", "STOPPING"})
        void koWhenOneNodeIsInTransientStatus(BrokerStatus status)
        {
            Broker broker = new Broker();
            BrokerNode node1 = new BrokerNode();
            node1.setStatus(BrokerStatus.RUNNING);
            BrokerNode node2 = new BrokerNode();
            node2.setStatus(BrokerStatus.STOPPED);
            BrokerNode node3 = new BrokerNode();
            node3.setStatus(status);

            broker.setNodes(List.of(node1, node2, node3));

            final NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateAnyNodeIsNotInTransitoryStatus(broker));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NODE_IN_TRANSITORY_STATUS_ERROR, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class getValidNumberOfNodes
    {
        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void testMonoCPD(Environment environment)
        {
            int[] result = brokerValidator.getValidNumberOfNodes(environment, true);

            assertEquals(2, result.length);
            assertTrue(ArrayUtils.contains(result, 1));
            assertTrue(ArrayUtils.contains(result, 2));
        }

        @Test
        void testIntMultiCPD()
        {
            int[] result = brokerValidator.getValidNumberOfNodes(Environment.INT, false);

            assertEquals(3, result.length);
            assertTrue(ArrayUtils.contains(result, 1));
            assertTrue(ArrayUtils.contains(result, 2));
            assertTrue(ArrayUtils.contains(result, 3));
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"PRE", "PRO"})
        void testPreAndProMultiCPD(Environment environment)
        {
            int[] result = brokerValidator.getValidNumberOfNodes(environment, false);

            assertEquals(2, result.length);
            assertTrue(ArrayUtils.contains(result, 2));
            assertTrue(ArrayUtils.contains(result, 3));
        }

        @Test
        void testInvalidEnvironment()
        {
            int[] result = brokerValidator.getValidNumberOfNodes(Environment.LOCAL, false);

            assertEquals(0, result.length);
        }
    }

    @Nested
    class getAndValidateGenericAlertConfig
    {

        @ParameterizedTest
        @EnumSource(value = GenericBrokerAlertType.class)
        void ok(GenericBrokerAlertType type)
        {
            Broker broker = new Broker();
            broker.setId(1);
            setBrokerAlertConfig(broker);

            assertDoesNotThrow(() -> brokerValidator.getAndValidateGenericAlertConfig(broker, type));
        }

        @Test
        void ko()
        {
            Broker broker = new Broker();
            broker.setId(1);

            broker.setGenericAlertConfigs(List.of(getGenericBrokerAlertConfig(GenericBrokerAlertType.BROKER_HEALTH),
                    getGenericBrokerAlertConfig(GenericBrokerAlertType.OVERFLOWED_BROKER)));

            NovaException exception = assertThrows(NovaException.class,
                    () -> brokerValidator.getAndValidateGenericAlertConfig(broker, GenericBrokerAlertType.UNAVAILABLE_NODE));

            assertEquals(BrokerConstants.BrokerErrorCode.ALERT_CONFIG_OCCURRENCES_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class getAndValidateRateThresholdAlertConfig
    {

        @ParameterizedTest
        @EnumSource(value = RateThresholdBrokerAlertType.class)
        void ok(RateThresholdBrokerAlertType type)
        {
            Broker broker = new Broker();
            broker.setId(1);
            setBrokerAlertConfig(broker);

            assertDoesNotThrow(() -> brokerValidator.getAndValidateRateThresholdAlertConfig(broker, type));
        }

        @Test
        void ko()
        {
            Broker broker = new Broker();
            broker.setId(1);

            broker.setRateAlertConfigs(List.of(getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW)));

            NovaException exception = assertThrows(NovaException.class,
                    () -> brokerValidator.getAndValidateRateThresholdAlertConfig(broker, RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE));

            assertEquals(BrokerConstants.BrokerErrorCode.ALERT_CONFIG_OCCURRENCES_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class getAndValidateQueueAlertConfig
    {

        @Test
        void ok()
        {
            Broker broker = new Broker();
            broker.setId(1);
            broker.setQueueAlertConfigs(List.of(getQueueBrokerAlertConfig()));

            assertDoesNotThrow(() -> brokerValidator.getAndValidateQueueAlertConfig(broker, QueueBrokerAlertType.LENGTH_ABOVE));
        }

        @Test
        void ko()
        {
            Broker broker = new Broker();
            broker.setId(1);
            broker.setQueueAlertConfigs(Collections.emptyList());

            NovaException exception = assertThrows(NovaException.class,
                    () -> brokerValidator.getAndValidateQueueAlertConfig(broker, QueueBrokerAlertType.LENGTH_ABOVE));

            assertEquals(BrokerConstants.BrokerErrorCode.ALERT_CONFIG_OCCURRENCES_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class validateBrokerAlertConfig
    {
        @Test
        void checkEmailIsInvalid()
        {
            Broker broker = new Broker();
            broker.setId(1);

            BrokerAlertConfigDTO alertConfig = new BrokerAlertConfigDTO();
            alertConfig.setEmailAddresses(new String[]{"valid@bbva.com", "invalid@bbva"});

            NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerAlertConfig(alertConfig, broker));

            assertEquals(BrokerConstants.BrokerErrorCode.EMAIL_INVALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1})
        @NullSource
        void checkQueueThresholdIsValid(Integer threshold)
        {
            Broker broker = new Broker();
            broker.setId(1);

            BrokerAlertConfigDTO alertConfig = new BrokerAlertConfigDTO();
            alertConfig.setEmailAddresses(new String[]{"valid@bbva.com"});

            QueueBrokerAlertConfigDTO queueAlertConfig = new QueueBrokerAlertConfigDTO();
            queueAlertConfig.setIsActive(true);
            queueAlertConfig.setThresholdQueueLength(threshold);
            alertConfig.setQueueLengthAlertConfig(queueAlertConfig);

            NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerAlertConfig(alertConfig, broker));

            assertEquals(BrokerConstants.BrokerErrorCode.QUEUE_THRESHOLD_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @ValueSource(doubles = {-0.01, -1.0})
        @NullSource
        void checkPublishRateThresholdIsValid(Double threshold)
        {
            Broker broker = new Broker();
            broker.setId(1);

            BrokerAlertConfigDTO alertConfig = new BrokerAlertConfigDTO();
            alertConfig.setEmailAddresses(new String[]{"valid@bbva.com"});

            RateThresholdBrokerAlertConfigDTO publishRateAlertConfig = new RateThresholdBrokerAlertConfigDTO();
            publishRateAlertConfig.setIsActive(true);
            publishRateAlertConfig.setThresholdRate(threshold);
            alertConfig.setPublishRateAlertConfig(publishRateAlertConfig);

            NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerAlertConfig(alertConfig, broker));

            assertEquals(BrokerConstants.BrokerErrorCode.PUBLISH_RATE_THRESHOLD_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @ValueSource(doubles = {0.0, -1.0})
        @NullSource
        void checkConsumerRateThresholdIsValid(Double threshold)
        {
            Broker broker = new Broker();
            broker.setId(1);

            BrokerAlertConfigDTO alertConfig = new BrokerAlertConfigDTO();
            alertConfig.setEmailAddresses(new String[]{"valid@bbva.com"});

            RateThresholdBrokerAlertConfigDTO consumerRateAlertConfig = new RateThresholdBrokerAlertConfigDTO();
            consumerRateAlertConfig.setIsActive(true);
            consumerRateAlertConfig.setThresholdRate(threshold);
            alertConfig.setConsumerRateAlertConfig(consumerRateAlertConfig);

            NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerAlertConfig(alertConfig, broker));

            assertEquals(BrokerConstants.BrokerErrorCode.CONSUMER_RATE_THRESHOLD_NOT_VALID_ERROR_CODE, exception.getNovaError().getErrorCode());
        }

        @ParameterizedTest
        @NullSource
        @EmptySource
        void checkRemedyGroup(String remedyGroup)
        {
            Broker broker = new Broker();
            broker.setId(1);
            Product product = new Product();
            product.setRemedySupportGroup(remedyGroup);
            broker.setProduct(product);

            BrokerAlertConfigDTO alertConfig = new BrokerAlertConfigDTO();
            alertConfig.setEmailAddresses(new String[]{"valid@bbva.com"});

            RateThresholdBrokerAlertConfigDTO consumerRateAlertConfigDTO = new RateThresholdBrokerAlertConfigDTO();
            consumerRateAlertConfigDTO.setIsActive(true);
            consumerRateAlertConfigDTO.setSendPatrol(true);
            consumerRateAlertConfigDTO.setThresholdRate(1.0);
            alertConfig.setConsumerRateAlertConfig(consumerRateAlertConfigDTO);

            NovaException exception = assertThrows(NovaException.class, () -> brokerValidator.validateBrokerAlertConfig(alertConfig, broker));

            assertEquals(BrokerConstants.BrokerErrorCode.PRODUCT_HAS_NOT_REMEDY_GROUP_ERROR_CODE, exception.getNovaError().getErrorCode());
        }
    }

    @Nested
    class validateEnvironment
    {
        @Test
        @DisplayName("Broker can be managed in an INT environment")
        void testValidateBrokerCanBeManagedINTSuccessfully()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.RUNNING);
            broker.setEnvironment("INT");

            DeploymentService deploymentService1 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            DeploymentService deploymentService2 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            broker.setDeploymentServices(List.of(deploymentService1, deploymentService2));

            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);

            brokerValidator.validateBrokerActionCanBeManagedByUser(broker, IV_USER);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
        }

        @Test
        @DisplayName("Broker can be managed in an PRE environment")
        void testValidateBrokerCanBeManagedPRESuccessfully()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.RUNNING);
            broker.setEnvironment("PRE");

            DeploymentService deploymentService1 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            DeploymentService deploymentService2 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            broker.setDeploymentServices(List.of(deploymentService1, deploymentService2));

            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(true);

            brokerValidator.validateBrokerActionCanBeManagedByUser(broker, IV_USER);

            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
        }

        @Test
        @DisplayName("Broker can't be managed in an PRO environment")
        void throwExceptionBrokerCanBeManagedPROOkFalse()
        {
            Broker broker = new Broker();
            broker.setStatus(BrokerStatus.RUNNING);
            broker.setEnvironment("PRO");

            DeploymentService deploymentService1 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            DeploymentService deploymentService2 = generateDeploymentService(DeploymentStatus.DEPLOYED, false);
            broker.setDeploymentServices(List.of(deploymentService1, deploymentService2));

            when(manageValidationUtils.checkIfBrokerActionCanBeManagedByUser(IV_USER, broker)).thenReturn(false);

            // test
            NovaException exception = assertThrows(
                    NovaException.class,
                    () -> brokerValidator.validateBrokerActionCanBeManagedByUser(broker, IV_USER)
            );

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_ENVIRONMENT_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(manageValidationUtils).checkIfBrokerActionCanBeManagedByUser(IV_USER, broker);
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
            brokerDTO.setNumberOfNodes(2);
            brokerDTO.setFilesystemId(FS_ID);
            brokerDTO.setProductId(PROD_ID);
            brokerDTO.setHardwarePackId(HW_PACK_ID);
            brokerDTO.setCpu((float) cpus);
            brokerDTO.setMemory(ramInMB);

            return brokerDTO;
        }
    }

    private static DeploymentService generateDeploymentService(DeploymentStatus status, boolean withRunningInstance)
    {
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(status);

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(plan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setStarted(withRunningInstance);
        deploymentService.setInstances(List.of(instance));

        return deploymentService;
    }

    public static void setBrokerAlertConfig(Broker broker)
    {
        broker.setQueueAlertConfigs(List.of(getQueueBrokerAlertConfig()));
        broker.setRateAlertConfigs(List.of(getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW),
                getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE)));

        broker.setGenericAlertConfigs(List.of(getGenericBrokerAlertConfig(GenericBrokerAlertType.BROKER_HEALTH),
                getGenericBrokerAlertConfig(GenericBrokerAlertType.UNAVAILABLE_NODE),
                getGenericBrokerAlertConfig(GenericBrokerAlertType.OVERFLOWED_BROKER)));
    }

    public static QueueBrokerAlertConfig getQueueBrokerAlertConfig()
    {
        QueueBrokerAlertConfig queueBrokerAlertConfig = new QueueBrokerAlertConfig();
        queueBrokerAlertConfig.setType(QueueBrokerAlertType.LENGTH_ABOVE);
        queueBrokerAlertConfig.setSendMail(true);
        queueBrokerAlertConfig.setSendPatrol(false);
        queueBrokerAlertConfig.setActive(false);
        queueBrokerAlertConfig.setTimeBetweenNotifications(0);
        queueBrokerAlertConfig.setThresholdQueueLength(null);
        return queueBrokerAlertConfig;
    }

    public static RateThresholdBrokerAlertConfig getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType type)
    {
        RateThresholdBrokerAlertConfig rateThresholdBrokerAlertConfig = new RateThresholdBrokerAlertConfig();
        rateThresholdBrokerAlertConfig.setType(type);
        rateThresholdBrokerAlertConfig.setSendMail(true);
        rateThresholdBrokerAlertConfig.setSendPatrol(false);
        rateThresholdBrokerAlertConfig.setActive(false);
        rateThresholdBrokerAlertConfig.setThresholdRate(null);
        rateThresholdBrokerAlertConfig.setTimeBetweenNotifications(0);
        return rateThresholdBrokerAlertConfig;
    }

    public static BrokerAlertConfig getGenericBrokerAlertConfig(GenericBrokerAlertType type)
    {
        BrokerAlertConfig brokerAlertConfig = new BrokerAlertConfig();
        brokerAlertConfig.setType(type);
        brokerAlertConfig.setSendMail(true);
        brokerAlertConfig.setSendPatrol(false);
        brokerAlertConfig.setActive(true);
        brokerAlertConfig.setTimeBetweenNotifications(0);
        return brokerAlertConfig;
    }

    public static BrokerUser getBrokerUser(BrokerRole brokerRole)
    {
        BrokerUser brokerUser = new BrokerUser();
        brokerUser.setRole(brokerRole);
        brokerUser.setPassword(GUEST);
        brokerUser.setName(GUEST);
        return brokerUser;
    }
}