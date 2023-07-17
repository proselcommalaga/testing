package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.ValidNumberOfNodesByEnvironmentDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BrokerInformationServiceImplTest
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
    public static final String MONITORING_URL = "monitoring://url";

    @Mock
    private BrokerBuilderImpl brokerBuilder;

    @Mock
    private BrokerValidatorImpl brokerValidator;

    @Mock
    private BrokerRepository brokerRepository;


    @InjectMocks
    private BrokerInformationServiceImpl brokerInformationGetter;

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
        verifyNoMoreInteractions(brokerRepository);
    }


    @Nested
    class getBrokersByProduct
    {
        BrokerInformationServiceImplTest.BrokerSupplier brokerSupplier = new BrokerInformationServiceImplTest.BrokerSupplier();
        BrokerInformationServiceImplTest.BrokerDTOSupplier brokerDTOSupplier = new BrokerInformationServiceImplTest.BrokerDTOSupplier();

        @Test
        void ok()
        {
            Broker broker = brokerSupplier.get();
            BrokerDTO brokerDTO = brokerDTOSupplier.get();
            // cond
            when(brokerRepository.findByProductId(PROD_ID)).thenReturn(List.of(broker));
            when(brokerBuilder.buildBasicBrokerDTOFromEntity(broker)).thenReturn(brokerDTO);
            BrokerDTO[] expectedBrokerDTOs = List.of(brokerDTO).toArray(BrokerDTO[]::new);

            // test
            BrokerDTO[] actualBrokerDTOs = brokerInformationGetter.getBrokersByProduct(PROD_ID);
            // assert
            Assertions.assertEquals(expectedBrokerDTOs[0], actualBrokerDTOs[0]);
            verify(brokerRepository).findByProductId(PROD_ID);
            verify(brokerBuilder).buildBasicBrokerDTOFromEntity(broker);
        }

        @Test
        void okEmptyResult()
        {
            // cond
            when(brokerRepository.findByProductId(PROD_ID)).thenReturn(Collections.emptyList());

            // test
            BrokerDTO[] actualBrokerDTOs = brokerInformationGetter.getBrokersByProduct(PROD_ID);
            // assert
            assertEquals(0, actualBrokerDTOs.length);
            verify(brokerRepository).findByProductId(PROD_ID);
        }

    }

    @Nested
    class getBrokerInfo
    {
        BrokerInformationServiceImplTest.BrokerSupplier brokerSupplier = new BrokerInformationServiceImplTest.BrokerSupplier();
        BrokerInformationServiceImplTest.BrokerDTOSupplier brokerDTOSupplier = new BrokerInformationServiceImplTest.BrokerDTOSupplier();

        @Test
        void ok()
        {
            Broker broker = brokerSupplier.get();
            BrokerDTO expectedBrokerDTO = brokerDTOSupplier.get();

            // cond
            when(brokerValidator.validateAndGetBroker(BROKER_ID)).thenReturn(broker);
            when(brokerBuilder.buildBrokerDTOFromEntity(IV_USER, broker)).thenReturn(expectedBrokerDTO);

            // test
            BrokerDTO actualBrokerDTO = brokerInformationGetter.getBrokerInfo(IV_USER, BROKER_ID);
            // assert
            Assertions.assertEquals(expectedBrokerDTO, actualBrokerDTO);
            verify(brokerValidator).validateAndGetBroker(BROKER_ID);
            verify(brokerBuilder).buildBrokerDTOFromEntity(IV_USER, broker);
        }
    }

    @Nested
    class getValidNumberOfNodesInfo
    {
        @Test
        void productMultiCPD()
        {
            Integer productId = RandomUtils.nextInt(1, 1000);

            Product product = new Product();
            product.setMultiCPDInPro(true);

            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(product);
            when(brokerValidator.getValidNumberOfNodes(Environment.INT, false)).thenReturn(new int[]{1, 2, 3});
            when(brokerValidator.getValidNumberOfNodes(Environment.PRE, false)).thenReturn(new int[]{2, 3});
            when(brokerValidator.getValidNumberOfNodes(Environment.PRO, false)).thenReturn(new int[]{2, 3});


            ValidNumberOfNodesByEnvironmentDTO result = brokerInformationGetter.getValidNumberOfNodesInfo(productId);

            assertArrayEquals(new int[]{1, 2, 3}, result.getValidNumberOfNodesForInt().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_INT_MULTICPD, result.getValidNumberOfNodesForInt().getComment());
            assertArrayEquals(new int[]{2, 3}, result.getValidNumberOfNodesForPre().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_PRE_MULTICPD, result.getValidNumberOfNodesForPre().getComment());
            assertArrayEquals(new int[]{2, 3}, result.getValidNumberOfNodesForPro().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_PRO_MULTICPD, result.getValidNumberOfNodesForPro().getComment());

            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerValidator).getValidNumberOfNodes(Environment.INT, false);
            verify(brokerValidator).getValidNumberOfNodes(Environment.PRE, false);
            verify(brokerValidator).getValidNumberOfNodes(Environment.PRO, false);
        }

        @Test
        void productMonoCPDAndTC()
        {
            Integer productId = RandomUtils.nextInt(1, 1000);

            Product product = new Product();
            product.setMultiCPDInPro(false);
            CPD cpd = new CPD();
            cpd.setName("TC");
            product.setCPDInPro(cpd);


            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(product);
            when(brokerValidator.getValidNumberOfNodes(Environment.INT, true)).thenReturn(new int[]{1, 2});
            when(brokerValidator.getValidNumberOfNodes(Environment.PRE, true)).thenReturn(new int[]{1, 2});
            when(brokerValidator.getValidNumberOfNodes(Environment.PRO, true)).thenReturn(new int[]{1, 2});

            ValidNumberOfNodesByEnvironmentDTO result = brokerInformationGetter.getValidNumberOfNodesInfo(productId);

            assertArrayEquals(new int[]{1, 2}, result.getValidNumberOfNodesForInt().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_INT_MONOCPD, result.getValidNumberOfNodesForInt().getComment());
            assertArrayEquals(new int[]{1, 2}, result.getValidNumberOfNodesForPre().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_PRE_MONOCPD, result.getValidNumberOfNodesForPre().getComment());
            assertArrayEquals(new int[]{1, 2}, result.getValidNumberOfNodesForPro().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_PRO_MONOCPD_TC, result.getValidNumberOfNodesForPro().getComment());

            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerValidator).getValidNumberOfNodes(Environment.INT, true);
            verify(brokerValidator).getValidNumberOfNodes(Environment.PRE, true);
            verify(brokerValidator).getValidNumberOfNodes(Environment.PRO, true);
        }

        @Test
        void productMonoCPDAndV()
        {
            Integer productId = RandomUtils.nextInt(1, 1000);

            Product product = new Product();
            product.setMultiCPDInPro(false);
            CPD cpd = new CPD();
            cpd.setName("V");
            product.setCPDInPro(cpd);


            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(product);
            when(brokerValidator.getValidNumberOfNodes(Environment.INT, true)).thenReturn(new int[]{1, 2});
            when(brokerValidator.getValidNumberOfNodes(Environment.PRE, true)).thenReturn(new int[]{1, 2});
            when(brokerValidator.getValidNumberOfNodes(Environment.PRO, true)).thenReturn(new int[]{1, 2});

            ValidNumberOfNodesByEnvironmentDTO result = brokerInformationGetter.getValidNumberOfNodesInfo(productId);

            assertArrayEquals(new int[]{1, 2}, result.getValidNumberOfNodesForInt().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_INT_MONOCPD, result.getValidNumberOfNodesForInt().getComment());
            assertArrayEquals(new int[]{1, 2}, result.getValidNumberOfNodesForPre().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_PRE_MONOCPD, result.getValidNumberOfNodesForPre().getComment());
            assertArrayEquals(new int[]{1, 2}, result.getValidNumberOfNodesForPro().getValidNumberOfNodes());
            assertEquals(BrokerInformationServiceImpl.COMMENT_FOR_PRO_MONOCPD_V, result.getValidNumberOfNodesForPro().getComment());

            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerValidator).getValidNumberOfNodes(Environment.INT, true);
            verify(brokerValidator).getValidNumberOfNodes(Environment.PRE, true);
            verify(brokerValidator).getValidNumberOfNodes(Environment.PRO, true);
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
            BrokerPack hardwarePack = new BrokerPack();
            product.setId(PROD_ID);
            filesystem.setId(FS_ID);
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setNumCPU(cpus);
            hardwarePack.setRamMB(ramInMB);
            broker.setProduct(product);
            broker.setHardwarePack(hardwarePack);
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
}