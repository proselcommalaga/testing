package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.IRestHandlerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackInfoDto;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryHistoryItemDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.ReplacePlanInfoDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.InstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by xe56809 on 14/03/2018.
 */
@ExtendWith(MockitoExtension.class)
class DeploymentManagerClientImplTest
{
    @Mock
    private IRestHandlerDeploymentmanagerapi restHandler;
    @Mock
    private CallbackService callbackService;
    @Mock
    private Logger log;
    @InjectMocks
    private DeploymentManagerClient client;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.client.init();

        Field field = client.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    void when_remove_plan_returns_ko_response_then_return_false()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.removePlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        boolean result = client.removePlan(plan);

        Assertions.assertFalse(result);
    }

    @Test
    void when_remove_plan_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.removePlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        boolean result = client.removePlan(plan);

        Assertions.assertTrue(result);
    }

    @Test
    void when_restart_instance_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartInstance(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        client.restartInstance(instance);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_restart_instance_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartInstance(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        client.restartInstance(instance);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_start_instance_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startInstance(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        client.startInstance(instance);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_start_instance_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startInstance(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        client.startInstance(instance);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_stop_instance_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopInstance(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        client.stopInstance(instance);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_stop_instance_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopInstance(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        client.stopInstance(instance);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_restart_plan_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        client.restartPlan(plan);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_restart_plan_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        client.restartPlan(plan);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_start_plan_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        client.startPlan(plan);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_start_plan_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        client.startPlan(plan);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_stop_plan_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        client.stopPlan(plan);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_stop_plan_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentPlan plan = new DeploymentPlan();
        plan.setId(1);
        client.stopPlan(plan);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_deploy_plan_returns_ko_response_then_throw_exception()
    {
        Mockito.when(this.callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(this.restHandler.deployPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(NovaException.class, () -> this.client.deployPlan(1));
        verify(this.callbackService, times(1)).buildCallback(Mockito.anyString(), Mockito.anyString());
        verify(this.restHandler, times(1)).deployPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt());
    }

    @Test
    void when_deploy_plan_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.deployPlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        boolean result = client.deployPlan(1);

        Assertions.assertTrue(result);
    }

    @Test
    void when_promote_plan_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.promotePlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.promotePlan(1, 2);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_promote_plan_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.promotePlan(Mockito.any(CallbackInfoDto.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.promotePlan(1, 2);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_replace_plan_returns_ko_response_then_return_false()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.replacePlan(Mockito.any(ReplacePlanInfoDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.replacePlan(1, 2, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        Assertions.assertFalse(result);
    }

    @Test
    void when_replace_plan_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.replacePlan(Mockito.any(ReplacePlanInfoDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        boolean result = client.replacePlan(1, 2, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        Assertions.assertTrue(result);
    }

    @Test
    void when_restart_service_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentService service = new DeploymentService();
        service.setId(1);
        client.restartService(service);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_restart_service_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentService service = new DeploymentService();
        service.setId(1);
        client.restartService(service);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_start_service_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentService service = new DeploymentService();
        service.setId(1);
        client.startService(service);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_start_service_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentService service = new DeploymentService();
        service.setId(1);
        client.startService(service);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_stop_service_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentService service = new DeploymentService();
        service.setId(1);
        client.stopService(service);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_stop_service_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentService service = new DeploymentService();
        service.setId(1);
        client.stopService(service);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_get_containers_of_service_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getServiceStatus(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        ServiceStatusDTO result = client.getContainersOfService(1);

        Assertions.assertEquals(0, result.getInstances().length);
        Assertions.assertEquals(0, result.getServiceId());
        StatusDTO status = result.getStatus();
        Assertions.assertEquals(0, status.getRunning());
        Assertions.assertEquals(0, status.getTotal());
    }

    @Test
    void when_get_containers_of_service_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getServiceStatus(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDeployServiceStatusDto(), HttpStatus.OK));

        ServiceStatusDTO result = client.getContainersOfService(1);

        InstanceStatusDTO[] instances = result.getInstances();
        Assertions.assertEquals(1, instances.length);
        Assertions.assertEquals(1, result.getServiceId());
        StatusDTO serviceStatus = result.getStatus();
        Assertions.assertEquals(1, serviceStatus.getRunning());
        Assertions.assertEquals(2, serviceStatus.getTotal());
        InstanceStatusDTO firstInstance = instances[0];
        Assertions.assertEquals(1, firstInstance.getInstanceId());
        StatusDTO instanceStatus = firstInstance.getStatus();
        Assertions.assertEquals(1, instanceStatus.getTotal());
        Assertions.assertEquals(0, instanceStatus.getRunning());
    }

    @Test
    void when_get_deployment_plan_service_status_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getDeploymentPlanServicesStatus(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        StatusDTO result = client.getDeploymentPlanServicesStatus(1);

        Assertions.assertEquals(0, result.getRunning());
        Assertions.assertEquals(0, result.getTotal());
    }

    @Test
    void when_get_deployment_plan_service_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getDeploymentPlanServicesStatus(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDeployStatusDtoForService(), HttpStatus.OK));

        StatusDTO result = client.getDeploymentPlanServicesStatus(1);

        Assertions.assertEquals(1, result.getRunning());
        Assertions.assertEquals(2, result.getTotal());
    }

    @Test
    void when_get_deployment_subsystems_services_status_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getSubsystemStatus(Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploySubsystemStatusDTO[] result = client.getDeploymentSubsystemServicesStatus(new int[]{1, 2, 3});

        Assertions.assertEquals(0, result.length);
    }

    @Test
    void when_get_deployment_subsystems_services_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getSubsystemStatus(Mockito.any())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDeploySubsystemStatusDtos(), HttpStatus.OK));

        DeploySubsystemStatusDTO[] result = client.getDeploymentSubsystemServicesStatus(new int[]{1, 2, 3});

        Assertions.assertEquals(1, result.length);
        DeploySubsystemStatusDTO firstResult = result[0];
        Assertions.assertEquals(1, firstResult.getSubsystemId());
        DeployStatusDTO subsystemStatus = firstResult.getStatus();
        Assertions.assertEquals(2, subsystemStatus.getRunning());
        Assertions.assertEquals(2, subsystemStatus.getExited());
        Assertions.assertEquals(4, subsystemStatus.getTotal());
        DeployServiceStatusDTO[] services = firstResult.getServices();
        Assertions.assertEquals(1, services.length);
        DeployServiceStatusDTO firstService = services[0];
        Assertions.assertEquals(1, firstService.getServiceId());
        Assertions.assertEquals("ServiceName", firstService.getServiceName());
        Assertions.assertEquals("ServiceType", firstService.getServiceType());
        DeployStatusDTO serviceStatus = firstService.getStatus();
        Assertions.assertEquals(2, serviceStatus.getTotal());
        Assertions.assertEquals(1, serviceStatus.getRunning());
        Assertions.assertEquals(1, serviceStatus.getExited());
        DeployInstanceStatusDTO[] instances = firstService.getInstances();
        Assertions.assertEquals(1, instances.length);
        DeployInstanceStatusDTO firstInstance = instances[0];
        Assertions.assertEquals(1, firstInstance.getInstanceId());
        DeployStatusDTO instanceStatus = firstInstance.getStatus();
        Assertions.assertEquals(0, instanceStatus.getRunning());
        Assertions.assertEquals(1, instanceStatus.getTotal());
        Assertions.assertEquals(1, instanceStatus.getExited());
    }

    @Test
    void when_get_all_deployment_instance_status_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getAllDeploymentInstanceStatus(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(NovaException.class, () -> client.getAllDeploymentInstanceStatus(1));
    }

    @Test
    void when_get_all_deployment_instance_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getAllDeploymentInstanceStatus(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDeployInstanceStatusDtos(), HttpStatus.OK));

        DeployInstanceStatusDTO[] result = client.getAllDeploymentInstanceStatus(1);

        Assertions.assertEquals(1, result.length);
        DeployInstanceStatusDTO firstResult = result[0];
        Assertions.assertEquals(1, firstResult.getInstanceId());
        DeployStatusDTO instanceStatus = firstResult.getStatus();
        Assertions.assertEquals(1, instanceStatus.getTotal());
        Assertions.assertEquals(1, instanceStatus.getExited());
        Assertions.assertEquals(0, instanceStatus.getRunning());
    }

    @Test
    void when_get_deployment_instance_status_by_id_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getDeploymentInstanceStatusById(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeployInstanceStatusDTO result = client.getDeploymentInstanceStatusById(1);

        Assertions.assertNull(result.getInstanceId());
        Assertions.assertNull(result.getStatus());
    }

    @Test
    void when_get_deployment_instance_status_by_id_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getDeploymentInstanceStatusById(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDeployInstanceStatusDtos()[0], HttpStatus.OK));

        DeployInstanceStatusDTO result = client.getDeploymentInstanceStatusById(1);

        Assertions.assertEquals(1, result.getInstanceId());
        DeployStatusDTO status = result.getStatus();
        Assertions.assertEquals(0, status.getRunning());
        Assertions.assertEquals(1, status.getTotal());
        Assertions.assertEquals(1, status.getExited());
    }

    @Test
    void when_get_service_status_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getServiceStatus(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeployServiceStatusDTO result = client.getServiceStatus(1);

        Assertions.assertNull(result.getServiceType());
        Assertions.assertNull(result.getServiceName());
        Assertions.assertNull(result.getServiceId());
        Assertions.assertNull(result.getStatus());
        Assertions.assertNull(result.getInstances());
    }

    @Test
    void when_get_service_status_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getServiceStatus(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDeployServiceStatusDto(), HttpStatus.OK));

        DeployServiceStatusDTO result = client.getServiceStatus(1);

        Assertions.assertEquals(1, result.getServiceId());
        Assertions.assertEquals("ServiceType", result.getServiceType());
        Assertions.assertEquals("ServiceName", result.getServiceName());
        DeployInstanceStatusDTO[] instances = result.getInstances();
        Assertions.assertEquals(1, instances.length);
        DeployInstanceStatusDTO firstInstance = instances[0];
        Assertions.assertEquals(1, firstInstance.getInstanceId());
        DeployStatusDTO instanceStatus = firstInstance.getStatus();
        Assertions.assertEquals(0, instanceStatus.getRunning());
        Assertions.assertEquals(1, instanceStatus.getTotal());
        Assertions.assertEquals(1, instanceStatus.getExited());
        DeployStatusDTO serviceStatus = result.getStatus();
        Assertions.assertEquals(1, serviceStatus.getExited());
        Assertions.assertEquals(1, serviceStatus.getRunning());
        Assertions.assertEquals(2, serviceStatus.getTotal());
    }

    @Test
    void when_deploy_service_returns_ko_response_then_return_false()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.deployService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.deployService(1);

        Assertions.assertFalse(result);
    }

    @Test
    void when_deploy_service_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.deployService(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        boolean result = client.deployService(1);

        Assertions.assertTrue(result);
    }

    @Test
    void when_deploy_subsystem_returns_ko_response_then_return_false()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.deploySubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.deploySubsystem(1);

        Assertions.assertFalse(result);
    }

    @Test
    void when_deploy_subsystem_returns_ok_response_then_return_true()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.deploySubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        boolean result = client.deploySubsystem(1);

        Assertions.assertTrue(result);
    }

    @Test
    void when_restart_subsystem_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartSubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setId(1);
        client.restartSubsystem(subsystem);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_restart_subsystem_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.restartSubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setId(1);
        client.restartSubsystem(subsystem);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_start_subsystem_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startSubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setId(1);
        client.startSubsystem(subsystem);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_start_subsystem_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.startSubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setId(1);
        client.startSubsystem(subsystem);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_stop_subsystem_returns_ko_response_then_log_error()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopSubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setId(1);
        client.stopSubsystem(subsystem);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    void when_stop_subsystem_returns_ok_response_then_log_debug()
    {
        Mockito.when(callbackService.buildCallback(Mockito.anyString(), Mockito.anyString())).thenReturn(new CallbackInfoDto());
        Mockito.when(restHandler.stopSubsystem(Mockito.any(CallbackInfoDto.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setId(1);
        client.stopSubsystem(subsystem);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    void when_get_hosts_memory_history_snapshot_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getHostsMemoryHistorySnapshot()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(NovaException.class, () -> client.getHostsMemoryHistorySnapshot());
    }

    @Test
    void when_get_hosts_memory_history_snapshot_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getHostsMemoryHistorySnapshot()).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyHostMemoryHistoryItemDtos(), HttpStatus.OK));

        HostMemoryHistoryItemDTO[] result = client.getHostsMemoryHistorySnapshot();

        Assertions.assertEquals(1, result.length);
        HostMemoryHistoryItemDTO firstResult = result[0];
        Assertions.assertEquals("CPD", firstResult.getCpd());
        Assertions.assertEquals("INT", firstResult.getEnvironment());
        Assertions.assertEquals("UNIT", firstResult.getUnit());
        Assertions.assertEquals(10D, firstResult.getValue());
    }
}