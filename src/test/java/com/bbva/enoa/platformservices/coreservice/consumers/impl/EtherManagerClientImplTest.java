package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.client.feign.nova.rest.IRestHandlerEthermanagerapi;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckEtherResourcesStatusRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckReadyToDeployRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherConfigurationRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherManagerConfigStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherRedeploymentDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherServiceDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherServiceStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherUserManagementDTO;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EtherManagerClientImplTest
{
    @Mock
    private CallbackService callbackService;
    @Mock
    private IRestHandlerEthermanagerapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private EtherManagerClientImpl client;

    @BeforeEach
    public void init() throws NoSuchFieldException, IllegalAccessException
    {
        MockitoAnnotations.initMocks(EtherManagerClientImpl.class);
        client.init();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_deploy_ether_plan_returns_ko_response_then_throw_exception()
    {
        Mockito.when(this.callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(DummyConsumerDataGenerator.getDummyCallbackInfoDto());
        Mockito.when(this.restHandler.deployService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(),
                HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        assertThrows(NovaException.class, () ->  this.client.deployEtherPlan(etherDeploymentDTO));

        verify(this.callbackService, times(1)).buildCallback(Mockito.anyString(), Mockito.anyString());
        verify(this.restHandler, times(1)).deployService(Mockito.any(EtherDeploymentDTO.class));
    }

    @Test
    public void when_deploy_ether_plan_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(DummyConsumerDataGenerator.getDummyCallbackInfoDto());
        Mockito.when(restHandler.deployService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        boolean result = client.deployEtherPlan(etherDeploymentDTO);

        Assertions.assertTrue(result);
    }

    @Test
    public void when_configure_services_for_logging_returns_ko_response_then_return_false()
    {
        Mockito.when(restHandler.configureServicesForLogging(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        boolean result = client.configureServicesForLogging(etherDeploymentDTO);

        Assertions.assertFalse(result);
    }

    @Test
    public void when_configure_services_for_logging_returns_ok_response_then_return_true()
    {
        Mockito.when(restHandler.configureServicesForLogging(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        boolean result = client.configureServicesForLogging(etherDeploymentDTO);

        Assertions.assertTrue(result);
    }

    @Test
    public void when_remove_ether_plan_returns_ko_response_then_return_false()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(DummyConsumerDataGenerator.getDummyCallbackInfoDto());
        Mockito.when(restHandler.removeService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        boolean result = client.removeEtherPlan(etherDeploymentDTO);

        Assertions.assertFalse(result);
    }

    @Test
    public void when_remove_ether_plan_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(DummyConsumerDataGenerator.getDummyCallbackInfoDto());
        Mockito.when(restHandler.removeService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        boolean result = client.removeEtherPlan(etherDeploymentDTO);

        Assertions.assertTrue(result);
    }

    @Test
    public void when_replace_ether_plan_returns_ko_response_then_return_false()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(DummyConsumerDataGenerator.getDummyCallbackInfoDto());
        Mockito.when(restHandler.redeployService(Mockito.any(EtherRedeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO oldEtherDeploymentDTO = new EtherDeploymentDTO();
        oldEtherDeploymentDTO.setDeploymentId(1);
        EtherDeploymentDTO newEtherDeploymentDTO = new EtherDeploymentDTO();
        newEtherDeploymentDTO.setDeploymentId(2);
        boolean result = client.replaceEtherPlan(oldEtherDeploymentDTO, newEtherDeploymentDTO);

        Assertions.assertFalse(result);
    }

    @Test
    public void when_replace_ether_plan_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(DummyConsumerDataGenerator.getDummyCallbackInfoDto());
        Mockito.when(restHandler.redeployService(Mockito.any(EtherRedeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO oldEtherDeploymentDTO = new EtherDeploymentDTO();
        oldEtherDeploymentDTO.setDeploymentId(1);
        EtherDeploymentDTO newEtherDeploymentDTO = new EtherDeploymentDTO();
        newEtherDeploymentDTO.setDeploymentId(2);
        boolean result = client.replaceEtherPlan(oldEtherDeploymentDTO, newEtherDeploymentDTO);

        Assertions.assertTrue(result);
    }

    @Test
    public void when_get_service_status_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getServiceStatus(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        ServiceStatusDTO result = client.getServiceStatus(DummyConsumerDataGenerator.getDummyEtherDeploymentDto());

        Assertions.assertEquals(1, result.getServiceId());
        Assertions.assertNull(result.getInstances());
        StatusDTO status = result.getStatus();
        Assertions.assertEquals(0, status.getRunning());
        Assertions.assertEquals(0, status.getTotal());
    }

    @Test
    public void when_get_service_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getServiceStatus(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyEtherStatusDto(), HttpStatus.OK));

        ServiceStatusDTO result = client.getServiceStatus(DummyConsumerDataGenerator.getDummyEtherDeploymentDto());

        Assertions.assertEquals(1, result.getServiceId());
        Assertions.assertNull(result.getInstances());
        StatusDTO status = result.getStatus();
        Assertions.assertEquals(1, status.getRunning());
        Assertions.assertEquals(3, status.getTotal());
    }

    @Test
    public void when_ready_to_deploy_returns_ko_response_then_return_false()
    {
        Mockito.when(restHandler.readyToDeploy(Mockito.any(CheckReadyToDeployRequestDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.readyToDeploy(new CheckReadyToDeployRequestDTO());

        Assertions.assertFalse(result);
    }

    @Test
    public void when_ready_to_deploy_returns_ok_response_with_false_value_then_return_false()
    {
        Mockito.when(restHandler.readyToDeploy(Mockito.any(CheckReadyToDeployRequestDTO.class))).thenReturn(new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK));

        boolean result = client.readyToDeploy(new CheckReadyToDeployRequestDTO());

        Assertions.assertFalse(result);
    }

    @Test
    public void when_ready_to_deploy_returns_ok_response_with_true_value_then_return_true()
    {
        Mockito.when(restHandler.readyToDeploy(Mockito.any(CheckReadyToDeployRequestDTO.class))).thenReturn(new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK));

        boolean result = client.readyToDeploy(new CheckReadyToDeployRequestDTO());

        Assertions.assertTrue(result);
    }

    @Test
    public void when_get_deployment_status_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getDeploymentStatus(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        StatusDTO result = client.getDeploymentStatus(etherDeploymentDTO);

        Assertions.assertEquals(0, result.getRunning());
        Assertions.assertEquals(0, result.getTotal());
    }

    @Test
    public void when_get_deployment_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getDeploymentStatus(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyEtherStatusDto(), HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setDeploymentId(1);
        StatusDTO result = client.getDeploymentStatus(etherDeploymentDTO);

        Assertions.assertEquals(1, result.getRunning());
        Assertions.assertEquals(3, result.getTotal());
    }

    @Test
    public void when_get_subsystem_status_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getSubsystemStatus(Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherSubsystemStatusDTO[] result = client.getSubsystemStatus(new EtherSubsystemDTO[0]);

        Assertions.assertEquals(0, result.length);
    }

    @Test
    public void when_get_subsystem_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getSubsystemStatus(Mockito.any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyEtherSubsystemStatusDtos(), HttpStatus.OK));

        EtherSubsystemStatusDTO[] result = client.getSubsystemStatus(new EtherSubsystemDTO[0]);

        Assertions.assertEquals(1, result.length);
        EtherSubsystemStatusDTO firstResult = result[0];
        Assertions.assertEquals(1, firstResult.getSubsystemId());
        EtherStatusDTO subsystemStatus = firstResult.getStatus();
        Assertions.assertEquals(1, subsystemStatus.getRunningContainers());
        Assertions.assertEquals(3, subsystemStatus.getTotalContainers());
        EtherServiceStatusDTO[] services = firstResult.getServices();
        Assertions.assertEquals(1, services.length);
        EtherServiceStatusDTO firstService = services[0];
        Assertions.assertEquals(1, firstService.getServiceId());
        EtherStatusDTO serviceStatus = firstService.getStatus();
        Assertions.assertEquals(3, serviceStatus.getTotalContainers());
        Assertions.assertEquals(1, subsystemStatus.getRunningContainers());
    }

    @Test
    public void when_start_ether_service_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.startService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setEtherServices(new EtherServiceDTO[0]);
        etherDeploymentDTO.setDeploymentId(1);
        client.startEtherService(etherDeploymentDTO);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void when_start_ether_service_returns_ok_response_then_log_error()
    {
        Mockito.when(restHandler.startService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setEtherServices(new EtherServiceDTO[0]);
        etherDeploymentDTO.setDeploymentId(1);
        client.startEtherService(etherDeploymentDTO);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_stop_ether_service_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.stopService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setEtherServices(new EtherServiceDTO[0]);
        etherDeploymentDTO.setDeploymentId(1);
        client.stopEtherService(etherDeploymentDTO);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void when_stop_ether_service_returns_ok_response_then_log_error()
    {
        Mockito.when(restHandler.stopService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setEtherServices(new EtherServiceDTO[0]);
        etherDeploymentDTO.setDeploymentId(1);
        client.stopEtherService(etherDeploymentDTO);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_restart_ether_service_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.restartService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setEtherServices(new EtherServiceDTO[0]);
        etherDeploymentDTO.setDeploymentId(1);
        client.restartEtherService(etherDeploymentDTO);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void when_restart_ether_service_returns_ok_response_then_log_error()
    {
        Mockito.when(restHandler.restartService(Mockito.any(EtherDeploymentDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.setEtherServices(new EtherServiceDTO[0]);
        etherDeploymentDTO.setDeploymentId(1);
        client.restartEtherService(etherDeploymentDTO);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_check_ether_resources_status_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.checkEtherResourcesStatus(Mockito.any(CheckEtherResourcesStatusRequestDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.checkEtherResourcesStatus(new CheckEtherResourcesStatusRequestDTO()));
    }

    @Test
    public void when_check_ether_resources_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.checkEtherResourcesStatus(Mockito.any(CheckEtherResourcesStatusRequestDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyEtherManagerConfigStatusDto(), HttpStatus.OK));

        EtherManagerConfigStatusDTO result = client.checkEtherResourcesStatus(new CheckEtherResourcesStatusRequestDTO());

        Assertions.assertEquals("Status", result.getStatus());
        Assertions.assertEquals("GroupId", result.getGroupId());
        Assertions.assertEquals("NameSpace", result.getNamespace());
        Assertions.assertEquals(1, result.getErrorMessage().length);
        Assertions.assertEquals("ErrorMessage", result.getErrorMessage()[0]);
    }

    @Test
    public void when_configure_ether_infrastructure_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.configureEtherInfrastructure(Mockito.any(EtherConfigurationRequestDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherConfigurationRequestDTO etherManagerConfigurationRequest = new EtherConfigurationRequestDTO();
        etherManagerConfigurationRequest.setEnvironment("INT");
        etherManagerConfigurationRequest.setProductName("Product");
        etherManagerConfigurationRequest.setNamespace("NameSpace");

        Assertions.assertThrows(NovaException.class, () -> client.configureEtherInfrastructure(etherManagerConfigurationRequest));
    }

    @Test
    public void when_configure_ether_infrastructure_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.configureEtherInfrastructure(Mockito.any(EtherConfigurationRequestDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyEtherManagerConfigStatusDto(), HttpStatus.OK));

        EtherConfigurationRequestDTO etherManagerConfigurationRequest = new EtherConfigurationRequestDTO();
        etherManagerConfigurationRequest.setEnvironment("INT");
        etherManagerConfigurationRequest.setProductName("Product");
        etherManagerConfigurationRequest.setNamespace("NameSpace");
        EtherManagerConfigStatusDTO result = client.configureEtherInfrastructure(etherManagerConfigurationRequest);

        Assertions.assertEquals("Status", result.getStatus());
        Assertions.assertEquals("GroupId", result.getGroupId());
        Assertions.assertEquals("NameSpace", result.getNamespace());
        Assertions.assertEquals(1, result.getErrorMessage().length);
        Assertions.assertEquals("ErrorMessage", result.getErrorMessage()[0]);
    }

    @Test
    public void when_no_users_are_found_then_dont_add_users_to_group()
    {
        EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
        etherUserManagementDTO.setUsers(new String[0]);
        client.addUsersToGroup(etherUserManagementDTO);

        Mockito.verifyNoInteractions(restHandler);
    }

    @Test
    public void when_add_users_to_group_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.addUsersToGroup(Mockito.any(EtherUserManagementDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
        etherUserManagementDTO.setUsers(new String[]{"A"});
        Assertions.assertThrows(NovaException.class, () -> client.addUsersToGroup(etherUserManagementDTO));
    }

    @Test
    public void when_add_users_to_group_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.addUsersToGroup(Mockito.any(EtherUserManagementDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
        etherUserManagementDTO.setUsers(new String[]{"A"});
        client.addUsersToGroup(etherUserManagementDTO);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_no_users_are_found_then_dont_remove_users_from_group()
    {
        EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
        etherUserManagementDTO.setUsers(new String[0]);
        client.removeUsersFromGroup(etherUserManagementDTO);

        Mockito.verifyNoInteractions(restHandler);
    }

    @Test
    public void when_remove_users_from_group_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeUsersFromGroup(Mockito.any(EtherUserManagementDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
        etherUserManagementDTO.setUsers(new String[]{"A"});
        Assertions.assertThrows(NovaException.class, () -> client.removeUsersFromGroup(etherUserManagementDTO));
    }

    @Test
    public void when_remove_users_from_group_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeUsersFromGroup(Mockito.any(EtherUserManagementDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
        etherUserManagementDTO.setUsers(new String[]{"A"});
        client.removeUsersFromGroup(etherUserManagementDTO);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.any(), Mockito.any());
    }
}