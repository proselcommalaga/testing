package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertInfoDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerMonitoringAlertType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemAlertRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.consumers.impl.UsersClientImpl;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BrokerAlertServiceImplTest
{
    @Mock
    private IBrokerValidator brokerValidator;

    @Mock
    private IProductUsersClient usersClient;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private IAlertServiceApiClient alertServiceClient;

    @Mock
    private INovaActivityEmitter novaActivityEmitter;

    @Mock
    private FilesystemAlertRepository filesystemAlertRepository;

    @InjectMocks
    private BrokerAlertServiceImpl brokerAlertService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class getBrokerAlertsByProduct
    {
        @Test
        void productNotFound()
        {
            int productId = 1;

            when(brokerValidator.validateAndGetProduct(productId)).thenThrow(new NovaException(BrokerError.getProductNotFoundError(productId)));


            NovaException exception = assertThrows(NovaException.class,
                    () -> brokerAlertService.getBrokerAlertsByProduct(productId));

            assertEquals(BrokerConstants.BrokerErrorCode.PRODUCT_NOT_FOUND_ERROR_CODE, exception.getNovaError().getErrorCode());
            verify(brokerValidator).validateAndGetProduct(productId);
        }

        @Test
        void productWithNoBrokers()
        {
            int productId = 1;

            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(new Product());

            // empty list
            when(brokerRepository.findByProductId(productId)).thenReturn(List.of());

            BrokerAlertInfoDTO[] result = brokerAlertService.getBrokerAlertsByProduct(productId);

            assertEquals(0, result.length);
            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerRepository).findByProductId(productId);
        }

        @Test
        void productWithOneNodeBrokerWithoutFilesystem()
        {
            int productId = 1;
            int brokerId = 2;
            int alertId = 3;

            Product product = new Product();
            product.setId(productId);
            product.setUuaa("UUAA");
            Broker broker = new Broker();
            broker.setId(brokerId);
            broker.setProduct(product);
            broker.setFilesystem(null);
            broker.setNumberOfNodes(1);

            ASBasicAlertInfoDTO healthAlert = this.generateBasicAlert(BrokerMonitoringAlertType.APP_HEALTH_BROKER.name(), alertId+1, brokerId);
            ASBasicAlertInfoDTO logEventAlert = this.generateBasicAlert("APP_LOG_EVENT", alertId+2, 1234);

            ASRequestAlertsDTO asRequestAlertsDTO = new ASRequestAlertsDTO();
            asRequestAlertsDTO.setBasicAlertInfo(new ASBasicAlertInfoDTO[]{healthAlert, logEventAlert});

            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(product);
            when(brokerRepository.findByProductId(productId)).thenReturn(List.of(broker));
            when(alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(brokerId)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS)).thenReturn(asRequestAlertsDTO);

            BrokerAlertInfoDTO[] result = brokerAlertService.getBrokerAlertsByProduct(productId);

            assertEquals(1, result.length);

            BrokerAlertInfoDTO brokerAlertInfoDTO = result[0];
            assertEquals(brokerId, brokerAlertInfoDTO.getBrokerId());
            assertEquals(0, brokerAlertInfoDTO.getNodeAlerts().length);
            assertEquals(1, brokerAlertInfoDTO.getBrokerAlerts().length);
            assertEquals(alertId+1, brokerAlertInfoDTO.getBrokerAlerts()[0].getAlertId());
            assertEquals(BrokerMonitoringAlertType.APP_HEALTH_BROKER.name(), brokerAlertInfoDTO.getBrokerAlerts()[0].getAlertType());

            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerRepository).findByProductId(productId);
            verify(alertServiceClient).getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(brokerId)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS);
        }

        @Test
        void productWithOneNodeBrokerWithoutFilesystemAndNoAlerts()
        {
            int productId = 1;
            int brokerId = 2;

            Product product = new Product();
            product.setId(productId);
            product.setUuaa("UUAA");
            Broker broker = new Broker();
            broker.setId(brokerId);
            broker.setProduct(product);
            broker.setFilesystem(null);
            broker.setNumberOfNodes(1);

            ASRequestAlertsDTO asRequestAlertsDTO = new ASRequestAlertsDTO();
            asRequestAlertsDTO.setBasicAlertInfo(null);

            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(product);
            when(brokerRepository.findByProductId(productId)).thenReturn(List.of(broker));
            when(alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(brokerId)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS)).thenReturn(asRequestAlertsDTO);

            BrokerAlertInfoDTO[] result = brokerAlertService.getBrokerAlertsByProduct(productId);

            assertEquals(1, result.length);

            BrokerAlertInfoDTO brokerAlertInfoDTO = result[0];
            assertEquals(brokerId, brokerAlertInfoDTO.getBrokerId());
            assertEquals(0, brokerAlertInfoDTO.getNodeAlerts().length);
            assertEquals(0, brokerAlertInfoDTO.getBrokerAlerts().length);

            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerRepository).findByProductId(productId);
            verify(alertServiceClient).getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(brokerId)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS);
        }

        @Test
        void productWithTwoBrokers()
        {
            int productId = RandomUtils.nextInt(1, 1000);
            int broker1Id = RandomUtils.nextInt(1, 1000);
            int broker2Id = RandomUtils.nextInt(1, 1000);
            int alertId = RandomUtils.nextInt(1, 1000);
            int filesystemId = RandomUtils.nextInt(1, 1000);
            int filesystemAlertId = RandomUtils.nextInt(1, 1000);

            Product product = new Product();
            product.setId(productId);
            product.setUuaa("UUAA");

            Broker broker1 = new Broker();
            int node1Id = RandomUtils.nextInt(1001, 2000);
            broker1.setId(broker1Id);
            broker1.setProduct(product);
            broker1.setFilesystem(null);
            broker1.setNumberOfNodes(1);

            FilesystemAlert filesystemAlertConfig = new FilesystemAlert();
            filesystemAlertConfig.setId(filesystemAlertId);
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(filesystemId);
            BrokerNode node1 = new BrokerNode();
            node1.setId(node1Id);
            BrokerNode node2 = new BrokerNode();
            node2.setId(node1Id + 1);

            Broker broker2 = new Broker();
            broker2.setId(broker2Id);
            broker2.setProduct(product);
            broker2.setFilesystem(filesystem);
            broker2.setNumberOfNodes(2);
            broker2.setNodes(List.of(node1, node2));


            ASBasicAlertInfoDTO healthAlert = this.generateBasicAlert(BrokerMonitoringAlertType.APP_HEALTH_BROKER.name(), alertId, broker1Id);
            ASBasicAlertInfoDTO logEventAlert = this.generateBasicAlert("APP_LOG_EVENT", alertId + 1, 2313);
            ASBasicAlertInfoDTO filesystemAlert = this.generateBasicAlert("APP_FILE_SYSTEM", alertId + 2, filesystemId);
            ASBasicAlertInfoDTO nodeAlert = this.generateBasicAlert(BrokerMonitoringAlertType.APP_UNAVAILABLE_NODE_BROKER.name(), alertId + 3, node1Id);

            ASRequestAlertsDTO asRequestAlertsDTO1 = new ASRequestAlertsDTO();
            asRequestAlertsDTO1.setBasicAlertInfo(new ASBasicAlertInfoDTO[]{healthAlert, logEventAlert});

            ASRequestAlertsDTO asRequestAlertsDTO2 = new ASRequestAlertsDTO();
            asRequestAlertsDTO2.setBasicAlertInfo(new ASBasicAlertInfoDTO[]{healthAlert, logEventAlert});

            ASRequestAlertsDTO asRequestAlertsDTO2fs = new ASRequestAlertsDTO();
            asRequestAlertsDTO2fs.setBasicAlertInfo(new ASBasicAlertInfoDTO[]{filesystemAlert});
            ASRequestAlertsDTO asRequestAlertsDTO2node = new ASRequestAlertsDTO();
            asRequestAlertsDTO2node.setBasicAlertInfo(new ASBasicAlertInfoDTO[]{nodeAlert});

            when(brokerValidator.validateAndGetProduct(productId)).thenReturn(product);
            when(brokerRepository.findByProductId(productId)).thenReturn(List.of(broker1, broker2));
            when(filesystemAlertRepository.findByFilesystemCodeId(filesystemId)).thenReturn(Optional.of(filesystemAlertConfig));
            when(alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(broker1Id)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS)).thenReturn(asRequestAlertsDTO1);
            when(alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(broker2Id)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS)).thenReturn(asRequestAlertsDTO2);
            when(alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{String.valueOf(filesystemAlertId)}, productId, "UUAA", Constants.OPEN_ALERT_STATUS)).thenReturn(asRequestAlertsDTO2fs);
            when(alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{node1Id+"-"+broker2Id, (node1Id + 1) + "-" + broker2Id}, productId, "UUAA", Constants.OPEN_ALERT_STATUS)).thenReturn(asRequestAlertsDTO2node);


            BrokerAlertInfoDTO[] result = brokerAlertService.getBrokerAlertsByProduct(productId);

            assertEquals(2, result.length);

            BrokerAlertInfoDTO brokerAlertInfoDTO1 = result[0];
            assertEquals(broker1Id, brokerAlertInfoDTO1.getBrokerId());
            assertEquals(0, brokerAlertInfoDTO1.getNodeAlerts().length);
            assertEquals(1, brokerAlertInfoDTO1.getBrokerAlerts().length);
            assertEquals(alertId, brokerAlertInfoDTO1.getBrokerAlerts()[0].getAlertId());
            assertEquals(BrokerMonitoringAlertType.APP_HEALTH_BROKER.name(), brokerAlertInfoDTO1.getBrokerAlerts()[0].getAlertType());

            BrokerAlertInfoDTO brokerAlertInfoDTO2 = result[1];
            assertEquals(broker2Id, brokerAlertInfoDTO2.getBrokerId());
            assertEquals(1, brokerAlertInfoDTO2.getNodeAlerts().length);
            assertEquals("APP_UNAVAILABLE_NODE_BROKER", brokerAlertInfoDTO2.getNodeAlerts()[0].getAlertType());
            assertEquals(alertId + 3, brokerAlertInfoDTO2.getNodeAlerts()[0].getAlertId());
            assertEquals(2, brokerAlertInfoDTO2.getBrokerAlerts().length);
            assertEquals(BrokerMonitoringAlertType.APP_HEALTH_BROKER.name(), brokerAlertInfoDTO2.getBrokerAlerts()[0].getAlertType());
            assertEquals(alertId, brokerAlertInfoDTO2.getBrokerAlerts()[0].getAlertId());
            assertEquals("APP_FILE_SYSTEM", brokerAlertInfoDTO2.getBrokerAlerts()[1].getAlertType());
            assertEquals(alertId + 2, brokerAlertInfoDTO2.getBrokerAlerts()[1].getAlertId());

            System.out.println(Arrays.toString(result));


            verify(brokerValidator).validateAndGetProduct(productId);
            verify(brokerRepository).findByProductId(productId);
            verify(alertServiceClient, times(4)).getAlertsByRelatedIdAndStatus(any(), any(), any(), any());
        }

        private ASBasicAlertInfoDTO generateBasicAlert(String alertType, int alertId, int relatedId)
        {
            {
                ASBasicAlertInfoDTO basicAlertInfoDTO = new ASBasicAlertInfoDTO();
                basicAlertInfoDTO.setAlertId(alertId);
                basicAlertInfoDTO.setAlertType(alertType);
                basicAlertInfoDTO.setStatus(Constants.OPEN_ALERT_STATUS);
                basicAlertInfoDTO.setAlertRelatedId(String.valueOf(relatedId));
                return basicAlertInfoDTO;
            }

        }
    }

    @Nested
    class updateBrokerAlertConfiguration
    {
        @Test
        void failedValidateAndGetBroker()
        {
            Integer brokerId = RandomUtils.nextInt(1, 1000);
            String ivUser = RandomStringUtils.randomAlphabetic(7);

            when(brokerValidator.validateAndGetBroker(brokerId)).thenThrow(new NovaException(BrokerError.getBrokerNotFoundError(brokerId)));

            NovaException exception = assertThrows(NovaException.class, () -> brokerAlertService.updateBrokerAlertConfiguration(ivUser, brokerId, null));

            assertEquals(BrokerConstants.BrokerErrorCode.BROKER_NOT_FOUND_ERROR_CODE, exception.getErrorCode().getErrorCode());
            verify(brokerValidator).validateAndGetBroker(brokerId);
        }

        @Test
        void permissionDenied()
        {
            Integer brokerId = RandomUtils.nextInt(1, 1000);
            Integer productId = RandomUtils.nextInt(1, 1000);
            String ivUser = RandomStringUtils.randomAlphabetic(7);
            NovaException PERMISSION_DENIED = new NovaException(BrokerError.getPermissionDeniedError());

            Product product = new Product();
            product.setId(productId);
            Broker broker = new Broker();
            broker.setProduct(product);
            BrokerAlertConfigDTO brokerAlertConfigDTO = new BrokerAlertConfigDTO();

            when(brokerValidator.validateAndGetBroker(brokerId)).thenReturn(broker);
            doThrow(PERMISSION_DENIED).when(usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

            NovaException exception = assertThrows(NovaException.class, () -> brokerAlertService.updateBrokerAlertConfiguration(ivUser, brokerId, brokerAlertConfigDTO));

            assertEquals(BrokerConstants.BrokerErrorCode.PERMISSION_DENIED_ERROR_CODE, exception.getErrorCode().getErrorCode());

            verify(brokerValidator).validateAndGetBroker(brokerId);
        }

        @Test
        void failedValidateBrokerAlertConfig()
        {
            Integer brokerId = RandomUtils.nextInt(1, 1000);
            Integer productId = RandomUtils.nextInt(1, 1000);
            String ivUser = RandomStringUtils.randomAlphabetic(7);

            Product product = new Product();
            product.setId(productId);
            Broker broker = new Broker();
            broker.setProduct(product);
            BrokerAlertConfigDTO brokerAlertConfigDTO = new BrokerAlertConfigDTO();

            when(brokerValidator.validateAndGetBroker(brokerId)).thenReturn(broker);
            doNothing().when(usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
            doThrow(new NovaException(BrokerError.generateInstanceNoValidEmail())).when(brokerValidator).validateBrokerAlertConfig(brokerAlertConfigDTO, broker);

            NovaException exception = assertThrows(NovaException.class, () -> brokerAlertService.updateBrokerAlertConfiguration(ivUser, brokerId, brokerAlertConfigDTO));
            assertEquals(BrokerConstants.BrokerErrorCode.EMAIL_INVALID_ERROR_CODE, exception.getErrorCode().getErrorCode());
        }
    }
}