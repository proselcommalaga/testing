package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.schedulermanagerapi.client.feign.nova.rest.IRestHandlerSchedulermanagerapi;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.CreateDeploymentBatchScheduleDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.CreateDeploymentPlanContextParamsDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DisabledDateDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.ScheduledDeploymentDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SchedulerDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.StateBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.UpdateDeploymentBatchScheduleDTO;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.common.enums.BatchSchedulerInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.common.base.Charsets;
import com.netflix.hystrix.contrib.javanica.command.GenericCommand;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getDummyDeploymentPlan;
import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getDummyReleaseVersionService;

@ExtendWith(MockitoExtension.class)
class SchedulerManagerClientImplTest
{
    public static final String UNKNOWN_ERROR_CODE = "AAA-000";
    public static final String CRON_EXPRESSION_ERROR_CODE = "SCHEDULERMANAGER-008";
    public static final String CRON_EXPRESSION_REQUIREMENTS_ERROR = "SCHEDULERMANAGER-025";
    private static final String BATCH_AGENT_ERROR_CODE = "SCHEDULERMANAGER-036";
    private static final String BATCH_MANAGER_ERROR_CODE = "SCHEDULERMANAGER-037";
    private InputStream inputStream;
    @Mock
    private IRestHandlerSchedulermanagerapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private SchedulerManagerClientImpl client;

    @BeforeEach
    public void init() throws NoSuchFieldException, IllegalAccessException
    {
        MockitoAnnotations.initMocks(SchedulerManagerClientImpl.class);
        client.init();

        Field field = client.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);

        inputStream = SchedulerManagerClientImpl.class.getClassLoader().getResourceAsStream("consumers/nova.yml");
    }

    @AfterEach
    public void end() throws IOException
    {
        inputStream.close();
    }

    @Test
    public void when_save_batch_scheduler_service_returns_ko_response_then_throw_exception() throws IOException
    {
        Mockito.when(restHandler.saveSchedule(Mockito.any(SchedulerDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        byte[] bytes = new byte[1024];
        inputStream.read(bytes);
        String novaFileContent = new String(bytes);

        Assertions.assertThrows(NovaException.class, () -> client.saveBatchSchedulerService(1, bytes, "A", novaFileContent, Map.of()));
    }

    @Test
    public void when_save_batch_scheduler_service_returns_ok_response_then_log_debug() throws IOException
    {
        Mockito.when(restHandler.saveSchedule(Mockito.any(SchedulerDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        byte[] bytes = new byte[1024];
        inputStream.read(bytes);
        String novaFileContent = new String(bytes);

        client.saveBatchSchedulerService(1, bytes, "A", novaFileContent, Map.of(1, "service1", 2, "service2"));

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_get_deployment_batch_schedule_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.getDeploymentBatchSchedule(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.getDeploymentBatchSchedule(1, 1);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void when_get_deployment_batch_schedule_returns_ok_response_then_return_result()
    {
        DeploymentBatchScheduleDTO dto = DummyConsumerDataGenerator.getDummyDeploymentBatchScheduleDto();
        Mockito.when(restHandler.getDeploymentBatchSchedule(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        DeploymentBatchScheduleDTO result = client.getDeploymentBatchSchedule(1, 1);

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_remove_batch_scheduler_service_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeSchedule(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeBatchSchedulerService(1));
    }

    @Test
    public void when_remove_batch_scheduler_service_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeSchedule(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeBatchSchedulerService(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_create_deployment_plan_context_params_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createDeploymentPlanContextParams(Mockito.any(CreateDeploymentPlanContextParamsDTO.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.isNull(), Mockito.isNull())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createDeploymentPlanContextParams(1, 2, "A"));
    }

    @Test
    public void when_create_deployment_plan_context_params_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.createDeploymentPlanContextParams(Mockito.any(CreateDeploymentPlanContextParamsDTO.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.isNull(), Mockito.isNull())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.createDeploymentPlanContextParams(1, 2, "A");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_create_deployment_batch_schedule_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createDeploymentBatchSchedule(Mockito.any(CreateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createDeploymentBatchSchedule(1, 2, 3, "A"));
    }

    @Test
    public void when_create_deployment_batch_schedule_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.createDeploymentBatchSchedule(Mockito.any(CreateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.createDeploymentBatchSchedule(1, 2, 3, "A");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ko_response_and_status_is_undeployed_then_throw_exception()
    {
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.UNDEPLOYED);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(DeploymentConstants.DeployErrors.UPDATE_DEPLOYMENT_BATCH_SCHEDULE_ERROR, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ko_response_and_status_is_disabled_then_throw_exception()
    {
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.DISABLED);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.DISABLE_BATCH_SCHEDULE_SERVICE_ERROR, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ko_response_and_status_is_enabled_and_has_no_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.setMessages(Collections.emptyList());
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ENABLE_BATCH_SCHEDULE_SERVICE_ERROR, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ko_response_and_status_is_enabled_and_has_another_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(UNKNOWN_ERROR_CODE);
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ENABLE_BATCH_SCHEDULE_SERVICE_ERROR, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ko_response_and_status_is_enabled_and_has_cron_expression_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(CRON_EXPRESSION_ERROR_CODE);
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.CRON_EXPRESSION_ERROR, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ko_response_and_status_is_enabled_and_has_cron_expression_requirements_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(CRON_EXPRESSION_REQUIREMENTS_ERROR);
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.CRON_EXPRESSION_REQUIREMENTS, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_update_deployment_batch_schedule_throws_hystrix_runtime_exception_then_throw_exception()
    {
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenThrow(new HystrixRuntimeException(HystrixRuntimeException.FailureType.BAD_REQUEST_EXCEPTION, GenericCommand.class, "E", new IllegalAccessException(), new IllegalAccessException()));

        client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(DeploymentBatchScheduleStatus.class), Mockito.anyString());
    }

    @Test
    public void when_update_deployment_batch_schedule_throws_feign_exception_then_throw_exception()
    {
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenThrow(new FeignException.ServiceUnavailable("A", Request.create(Request.HttpMethod.GET, "A", Map.of(), new byte[0], Charsets.UTF_8, new RequestTemplate()), new byte[0]));

        client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any(DeploymentBatchScheduleStatus.class), Mockito.anyString());
    }

    @Test
    public void when_update_deployment_batch_schedule_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.updateDeploymentBatchSchedule(Mockito.any(UpdateDeploymentBatchScheduleDTO.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.updateDeploymentBatchSchedule(1, 2, DeploymentBatchScheduleStatus.ENABLED);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_remove_deployment_batch_schedule_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeDeploymentBatchSchedule(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeDeploymentBatchSchedule(1, 2));
    }

    @Test
    public void when_remove_deployment_batch_schedule_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeDeploymentBatchSchedule(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeDeploymentBatchSchedule(1, 2);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_remove_deployment_plan_context_params_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.deleteDeploymentPlanContextParams(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeDeploymentPlanContextParams(1));
    }

    @Test
    public void when_remove_deployment_plan_context_params_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.deleteDeploymentPlanContextParams(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeDeploymentPlanContextParams(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_copy_deployment_plan_context_params_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createDeploymentPlanContextParams(Mockito.any(CreateDeploymentPlanContextParamsDTO.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.copyDeploymentPlanContextParams(1, 2, 3, "A", 4));
    }

    @Test
    public void when_copy_deployment_plan_context_params_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.createDeploymentPlanContextParams(Mockito.any(CreateDeploymentPlanContextParamsDTO.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.copyDeploymentPlanContextParams(1, 2, 3, "A", 4);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_state_batch_schedule_instance_returns_ko_response_with_no_found_status_and_client_exception_message_then_throw_exception() throws NoSuchFieldException, IllegalAccessException
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors(HttpStatus.NOT_FOUND.value());
        Field detailMessageField = Errors.class.getSuperclass().getSuperclass().getDeclaredField("detailMessage");
        detailMessageField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(detailMessageField, detailMessageField.getModifiers() & ~Modifier.PRIVATE);
        detailMessageField.set(errors, "java.lang.RuntimeException: com.netflix.client.ClientException");
        Mockito.when(restHandler.stateBatchScheduleInstance(Mockito.any(StateBatchScheduleInstanceDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.START_INSTANCE);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_SCHEDULER, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_state_batch_schedule_instance_returns_ko_response_with_start_instance_type_then_throw_exception() throws NoSuchFieldException, IllegalAccessException
    {
        Mockito.when(restHandler.stateBatchScheduleInstance(Mockito.any(StateBatchScheduleInstanceDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.START_INSTANCE);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_INSTANCE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_state_batch_schedule_instance_returns_ko_response_with_stop_instance_type_then_throw_exception() throws NoSuchFieldException, IllegalAccessException
    {
        Mockito.when(restHandler.stateBatchScheduleInstance(Mockito.any(StateBatchScheduleInstanceDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.STOP_INSTANCE);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_STOP_INSTANCE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_state_batch_schedule_instance_returns_ko_response_with_pause_instance_type_then_throw_exception() throws NoSuchFieldException, IllegalAccessException
    {
        Mockito.when(restHandler.stateBatchScheduleInstance(Mockito.any(StateBatchScheduleInstanceDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.PAUSE_INSTANCE);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_PAUSE_INSTANCE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_state_batch_schedule_instance_returns_ko_response_with_resume_instance_type_then_throw_exception() throws NoSuchFieldException, IllegalAccessException
    {
        Mockito.when(restHandler.stateBatchScheduleInstance(Mockito.any(StateBatchScheduleInstanceDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.RESUME_INSTANCE);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_RESUME_INSTANCE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_state_batch_schedule_instance_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.createDeploymentPlanContextParams(Mockito.any(CreateDeploymentPlanContextParamsDTO.class), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.copyDeploymentPlanContextParams(1, 2, 3, "A", 4);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_start_schedule_instance_returns_ko_response_without_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.setMessages(Collections.emptyList());
        Mockito.when(restHandler.startScheduleInstance(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.startScheduleInstance(1, 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_INSTANCE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_start_schedule_instance_returns_ko_response_with_different_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.setMessages(Collections.emptyList());
        Mockito.when(restHandler.startScheduleInstance(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.startScheduleInstance(1, 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_INSTANCE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_start_schedule_instance_returns_ko_response_with_not_found_status_and_client_exception_message_then_throw_exception() throws NoSuchFieldException, IllegalAccessException
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors(HttpStatus.NOT_FOUND.value());
        Field detailMessageField = Errors.class.getSuperclass().getSuperclass().getDeclaredField("detailMessage");
        detailMessageField.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(detailMessageField, detailMessageField.getModifiers() & ~Modifier.PRIVATE);
        detailMessageField.set(errors, "java.lang.RuntimeException: com.netflix.client.ClientException");
        Mockito.when(restHandler.startScheduleInstance(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.startScheduleInstance(1, 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_SCHEDULER, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_start_schedule_instance_returns_ko_response_with_batch_agent_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BATCH_AGENT_ERROR_CODE);
        Mockito.when(restHandler.startScheduleInstance(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.startScheduleInstance(1, 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_AGENT, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_start_schedule_instance_returns_ko_response_with_batch_manager_error_message_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(BATCH_MANAGER_ERROR_CODE);
        Mockito.when(restHandler.startScheduleInstance(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.startScheduleInstance(1, 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_MANAGER, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_start_schedule_instance_returns_ok_then_log_debug()
    {
        Mockito.when(restHandler.startScheduleInstance(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.startScheduleInstance(1, 1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt());
    }

    @Test
    public void when_schedule_deployment_returns_ko_response_then_return_false()
    {
        Mockito.when(restHandler.scheduleDeployment(Mockito.any(ScheduledDeploymentDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = client.scheduleDeployment(getDummyDeploymentPlan());

        Assertions.assertFalse(result);
    }

    @Test
    public void when_schedule_deployment_returns_ok_response_then_return_true()
    {
        Mockito.when(restHandler.scheduleDeployment(Mockito.any(ScheduledDeploymentDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        boolean result = client.scheduleDeployment(getDummyDeploymentPlan());

        Assertions.assertTrue(result);
    }

    @Test
    public void when_unschedule_deployment_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.unscheduleDeployment(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.unscheduleDeployment(1));
    }

    @Test
    public void when_unschedule_deployment_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.unscheduleDeployment(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.unscheduleDeployment(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_get_deployment_batch_schedule_instances_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getDeploymentBatchScheduleInstances(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getDeploymentBatchScheduleInstances(getDummyReleaseVersionService(), getDummyDeploymentPlan()));
    }

    @Test
    public void when_get_deployment_batch_schedule_instances_returns_ok_response_then_return_result()
    {
        DeploymentBatchScheduleInstanceDTO[] dtos = DummyConsumerDataGenerator.getDummyDeploymentBatchScheduleInstanceDtos();
        Mockito.when(restHandler.getDeploymentBatchScheduleInstances(Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        DeploymentBatchScheduleInstanceDTO[] result = client.getDeploymentBatchScheduleInstances(getDummyReleaseVersionService(), getDummyDeploymentPlan());

        Assertions.assertEquals(dtos, result);
    }

    @Test
    public void when_is_disabled_date_for_deploy_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getDisabledDates(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.isDisabledDateForDeploy(new Date(), "A", Platform.NOVA));
    }

    @Test
    public void when_is_disabled_date_for_deploy_returns_ok_response_with_empty_dtos_then_return_true()
    {
        Mockito.when(restHandler.getDisabledDates(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(new DisabledDateDTO[0], HttpStatus.OK));

        boolean result = client.isDisabledDateForDeploy(new Date(), "A", Platform.NOVA);

        Assertions.assertFalse(result);
    }

    @Test
    public void when_is_disabled_date_for_deploy_returns_ok_response_with_populated_dtos_then_return_true()
    {
        Mockito.when(restHandler.getDisabledDates(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyDisabledDateDtos(), HttpStatus.OK));

        boolean result = client.isDisabledDateForDeploy(new Date(), "A", Platform.NOVA);

        Assertions.assertTrue(result);
    }

    @Test
    public void when_get_batch_scheduler_executions_summary_returns_ko_response_then_log_error_and_return_empty_result()
    {
        Mockito.when(restHandler.getBatchSchedulerExecutionsSummary(Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        SMBatchSchedulerExecutionsSummaryDTO result = client.getBatchSchedulerExecutionsSummary(new int[]{1, 2, 3});

        Assertions.assertEquals(new SMBatchSchedulerExecutionsSummaryDTO(), result);
        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_get_batch_scheduler_executions_summary_returns_ok_response_then_return_result()
    {
        SMBatchSchedulerExecutionsSummaryDTO dto = DummyConsumerDataGenerator.getDummySMBatchSchedulerExecutionsSummaryDto();
        Mockito.when(restHandler.getBatchSchedulerExecutionsSummary(Mockito.any())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        SMBatchSchedulerExecutionsSummaryDTO result = client.getBatchSchedulerExecutionsSummary(new int[]{1, 2, 3});

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_deployment_batch_schedule_instance_by_id_returns_ko_response_then_log_error_and_return_empty_result()
    {
        Mockito.when(restHandler.getDeploymentBatchScheduleInstanceById(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        DeploymentBatchScheduleInstanceDTO result = client.getDeploymentBatchScheduleInstanceById(1);

        Assertions.assertEquals(new DeploymentBatchScheduleInstanceDTO(), result);
        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void when_get_deployment_batch_schedule_instance_by_id_returns_ok_response_then_return_result()
    {
        DeploymentBatchScheduleInstanceDTO dto = DummyConsumerDataGenerator.getDummyDeploymentBatchScheduleInstanceDtos()[0];
        Mockito.when(restHandler.getDeploymentBatchScheduleInstanceById(Mockito.anyInt())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        DeploymentBatchScheduleInstanceDTO result = client.getDeploymentBatchScheduleInstanceById(1);

        Assertions.assertEquals(dto, result);
    }
}