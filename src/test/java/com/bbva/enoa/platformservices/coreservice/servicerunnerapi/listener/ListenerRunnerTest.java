package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.listener;

import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.ITaskProcessor;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IRunnerApiService;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceDeploymentException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ListenerRunnerTest
{

    @InjectMocks
    private ListenerRunner listenerRunner;
    @Mock
    private IErrorTaskManager errorTaskManager;
    @Mock
    private IRunnerApiService runnerServiceImpl;
    @Mock
    private ITaskProcessor taskProcessor;


    private static NovaMetadata novaMetadata;

    @BeforeEach
    public void setUp() throws Exception
    {
        novaMetadata = new NovaMetadata();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Arrays.asList(new String[]{"ivUser"}));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void onServiceRunnerTaskReply() throws Exception
    {
        this.listenerRunner.onServiceRunnerTaskReply(novaMetadata, 1, "DONE");
        verify(this.taskProcessor, times(1)).onTaskReply(1, "DONE");
    }

    @Test
    public void onServiceRunnerTaskReplyError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.taskProcessor).onTaskReply(1, "DONE");
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.onServiceRunnerTaskReply(novaMetadata, 1, "DONE"));
    }

    @Test
    public void onServiceRunnerTaskReplyError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.taskProcessor).onTaskReply(1, "DONE");
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.onServiceRunnerTaskReply(novaMetadata, 1, "DONE"));
    }

    @Test
    public void stopSubsystem() throws Exception
    {
        this.listenerRunner.stopSubsystem(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).stopSubsystem(1, "ivUser");
    }

    @Test
    public void stopSubsystemError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).stopSubsystem(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.stopSubsystem(novaMetadata, 1));
    }

    @Test
    public void stopSubsystemError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).stopSubsystem(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.stopSubsystem(novaMetadata, 1));
    }

    @Test
    public void startSubsystem() throws Exception
    {
        this.listenerRunner.startSubsystem(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).startSubsystem(1, "ivUser");
    }

    @Test
    public void startSubsystemError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).startSubsystem(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.startSubsystem(novaMetadata, 1));
    }

    @Test
    public void startSubsystemError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).startSubsystem(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.startSubsystem(novaMetadata, 1));
    }

    @Test
    public void restartSubsystem() throws Exception
    {
        this.listenerRunner.restartSubsystem(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).restartSubsystem(1, "ivUser");

    }

    @Test
    public void restartSubsystemError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).restartSubsystem(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.restartSubsystem(novaMetadata, 1));
    }

    @Test
    public void restartSubsystemError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).restartSubsystem(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.restartSubsystem(novaMetadata, 1));
    }

    @Test
    public void startPlan() throws Exception
    {
        this.listenerRunner.startPlan(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).startPlan(1, "ivUser");
    }

    @Test
    public void startPlanError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).startPlan(any(), any());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.startPlan(novaMetadata, 1));
    }

    @Test
    public void startPlanError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).startPlan(any(), any());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.startPlan(novaMetadata, 1));
    }

    @Test
    public void restartPlan() throws Exception
    {
        this.listenerRunner.restartPlan(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).restartPlan(1, "ivUser");

    }

    @Test
    public void restartPlanError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).restartPlan(any(), any());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.restartPlan(novaMetadata, 1));
    }

    @Test
    public void restartPlanError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).restartPlan(any(), any());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.restartPlan(novaMetadata, 1));
    }

    @Test
    public void stopPlan() throws Exception
    {
        this.listenerRunner.stopPlan(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).stopPlan(1, "ivUser");
    }

    @Test
    public void stopPlanError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).stopPlan(any(), any());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.stopPlan(novaMetadata, 1));
    }

    @Test
    public void stopPlanError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).stopPlan(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.stopPlan(novaMetadata, 1));
    }

    @Test
    public void startInstance() throws Exception
    {
        this.listenerRunner.startInstance(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).startInstance(1, "ivUser");
    }

    @Test
    public void startInstanceBatchScheduler() throws Exception
    {
        this.listenerRunner.startBatchScheduleInstance(novaMetadata, 1, 1);
        verify(this.runnerServiceImpl, times(1)).startBatchScheduleInstance(1, 1, "ivUser");
    }

    @Test
    public void startBatchScheduleInstanceErrorTest() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).startBatchScheduleInstance(anyInt(), anyInt(),anyString());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.startBatchScheduleInstance(novaMetadata, 1, 1));
    }

    @Test
    public void startInstanceError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).startInstance(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.startInstance(novaMetadata, 1));
    }

    @Test
    public void startInstanceError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).startInstance(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.startInstance(novaMetadata, 1));
    }

    @Test
    public void restartInstance() throws Exception
    {
        this.listenerRunner.restartInstance(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).restartInstance(1, "ivUser");
    }

    @Test
    public void restartInstanceBatchScheduler() throws Exception
    {
        this.listenerRunner.resumeBatchScheduleInstance(novaMetadata, 1, 1);
        verify(this.runnerServiceImpl, times(1)).resumeBatchScheduleInstance(1, 1, "ivUser");
    }

    @Test
    public void restartInstanceBatchSchedulerErrorTest() throws Exception
    {
        doThrow(RuntimeException.class).when(this.runnerServiceImpl).resumeBatchScheduleInstance(anyInt(), anyInt(), anyString());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.resumeBatchScheduleInstance(novaMetadata, 1, 1));

    }

    @Test
    public void restartInstanceError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).restartInstance(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.restartInstance(novaMetadata, 1));
    }

    @Test
    public void restartInstanceError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).restartInstance(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.restartInstance(novaMetadata, 1));
    }

    @Test
    public void pauseInstanceBatchScheduler() throws Exception
    {
        this.listenerRunner.pauseBatchScheduleInstance(novaMetadata, 1, 1);
        verify(this.runnerServiceImpl, times(1)).pauseBatchScheduleInstance(1, 1, "ivUser");
    }

    @Test
    public void pauseInstanceBatchSchedulerErrorTest() throws Exception
    {
        doThrow(RuntimeException.class).when(this.runnerServiceImpl).pauseBatchScheduleInstance(anyInt(), anyInt(), anyString());
        Assertions.assertThrows(LogAndTraceDeploymentException.class, () -> this.listenerRunner.pauseBatchScheduleInstance(novaMetadata, 1, 1));
    }


    @Test
    public void stopInstance() throws Exception
    {
        this.listenerRunner.stopInstance(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).stopInstance(1, "ivUser");
    }

    @Test
    public void stopInstanceBatchScheduler() throws Exception
    {
        this.listenerRunner.stopBatchScheduleInstance(novaMetadata, 1, 1);
        verify(this.runnerServiceImpl, times(1)).stopBatchScheduleInstance(1, 1, "ivUser");
    }

    @Test
    public void stopInstanceBatchSchedulerErrorTest() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).stopBatchScheduleInstance(anyInt(), anyInt(), anyString());
        Assertions.assertThrows(Errors.class,()->this.listenerRunner.stopBatchScheduleInstance(novaMetadata, 1, 1));

    }

    @Test
    public void stopInstanceError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).stopInstance(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.stopInstance(novaMetadata, 1));
    }

    @Test
    public void stopInstanceError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).stopInstance(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.stopInstance(novaMetadata, 1));
    }

    @Test
    public void restartService() throws Exception
    {
        this.listenerRunner.restartService(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).restartService(1, "ivUser");
    }

    @Test
    public void restartServiceError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).restartService(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.restartService(novaMetadata, 1));
    }

    @Test
    public void restartServiceError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).restartService(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.restartService(novaMetadata, 1));
    }

    @Test
    public void stopService() throws Exception
    {
        this.listenerRunner.stopService(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).stopService(1, "ivUser");
    }

    @Test
    public void stopBatchSchedule() throws Exception
    {
        this.listenerRunner.stopBatchSchedule(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).stopBatchSchedule(1, "ivUser");
    }

    @Test
    public void stopServiceError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).stopService(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.stopService(novaMetadata, 1));
    }

    @Test
    public void stopServiceError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).stopService(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.stopService(novaMetadata, 1));
    }

    @Test
    public void startService() throws Exception
    {
        this.listenerRunner.startService(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).startService(1, "ivUser");
    }

    @Test
    public void startServiceError() throws Exception
    {
        doThrow(new NovaException(ServiceRunnerError.getUnexpectedError())).when(this.runnerServiceImpl).startService(any(), any());
        Assertions.assertThrows(NovaException.class, () -> this.listenerRunner.startService(novaMetadata, 1));
    }

    @Test
    public void startServiceError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.runnerServiceImpl).startService(any(), any());
        Assertions.assertThrows(RuntimeException.class, () -> this.listenerRunner.startService(novaMetadata, 1));
    }

    @Test
    public void startBatchSchedule() throws Exception
    {
        this.listenerRunner.startBatchSchedule(novaMetadata, 1);
        verify(this.runnerServiceImpl, times(1)).startBatchSchedule(1, "ivUser");
    }
    @Test
    public void getInstancesStatusesTest() throws Exception
    {
        this.listenerRunner.getInstancesStatuses(novaMetadata);
        verify(this.runnerServiceImpl, times(1)).getInstancesStatuses();
    }

}
