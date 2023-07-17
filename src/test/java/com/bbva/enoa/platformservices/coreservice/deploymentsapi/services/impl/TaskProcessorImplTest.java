package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TaskRequestDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentGcspRepo;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentNovaRepo;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentTypeChangeRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRemoverService;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskProcessorImplTest
{
    @Mock
    private DeploymentChangeRepository changeRepository;

    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private DeploymentTypeChangeRepository typeChangeRepository;

    @Mock
    private IRemoverService IRemoverService;

    @Mock
    private DeploymentNovaRepo deploymentNovaRepo;

    @Mock
    private DeploymentGcspRepo deploymentGcspRepo;

    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;

    @Mock
    private DeployerServiceImpl deployerService;

    @Mock
    private IDeploymentsValidator deploymentsValidator;

    @Mock
    private DeploymentTaskRepository deploymentTaskRepository;

    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;

    @Mock
    private IAlertServiceApiClient alertServiceApiClient;

    @Mock
    private IErrorTaskManager errorTaskManager;

    @InjectMocks
    private TaskProcessorImpl taskProcessor;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void createDeploymentTaskCheckEnvironmentVars()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTypeTask(ToDoTaskType.CHECK_ENVIRONMENT_VARS.name());
        TaskRequestDTO[] taskRequestDTOs = {taskRequestDTO};
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);

        //initialize configurationTask
        deploymentPlan.setConfigurationTask(null);
        var deploymentTask = new DeploymentTask();

        //When
        when(this.todoTaskServiceClient.createDeploymentTask(
                any(), any(), any(), any(), any(), any())).thenReturn(taskId);
        when(this.deploymentTaskRepository.existsById(taskId)).thenReturn(true);
        when(this.deploymentTaskRepository.getOne(taskId)).thenReturn(deploymentTask);

        taskProcessor.createConfigManagementTask(taskRequestDTOs, "ivUser", deploymentPlan);

        //Verify
        verify(todoTaskServiceClient, times(1)).
                createDeploymentTask(any(), any(), any(), any(), any(), any());
        verify(this.deploymentTaskRepository, times(1)).existsById(taskId);
        verify(this.deploymentTaskRepository, times(1)).getOne(taskId);
        Assertions.assertEquals(deploymentTask, deploymentPlan.getConfigurationTask());
    }

    @Test
    void createDeploymentTaskDeployPro()
    {
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTypeTask(ToDoTaskType.DEPLOY_PRO.name());
        TaskRequestDTO[] taskRequestDTOs = {taskRequestDTO};
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);

        //initialize configurationTask
        deploymentPlan.setConfigurationTask(null);

        taskProcessor.createConfigManagementTask(taskRequestDTOs, "ivUser", deploymentPlan);

        Assertions.assertNull(deploymentPlan.getConfigurationTask());
    }


    @Test
    void createDeploymentTaskNoDeploymentTask()
    {
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTypeTask(ToDoTaskType.LOG_ACCESS_PRO.name());
        TaskRequestDTO[] taskRequestDTOs = {taskRequestDTO};
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        assertThrows(NovaException.class, () ->
                taskProcessor.createConfigManagementTask(taskRequestDTOs, "ivUser", deploymentPlan)
        );
    }

    @Test
    void createDeploymentTaskBadType()
    {
        TaskRequestDTO taskRequestDTO = new TaskRequestDTO();
        taskRequestDTO.setTypeTask("Bad");
        TaskRequestDTO[] taskRequestDTOs = {taskRequestDTO};
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        assertThrows(NovaException.class, () ->
                taskProcessor.createConfigManagementTask(taskRequestDTOs, "ivUser", deploymentPlan)
        );
    }

    @Test
    void onTaskreplyCheckEnvironmentVars()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.CHECK_ENVIRONMENT_VARS);
        var status = ToDoTaskStatus.PENDING;

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(
                Optional.of(deploymentTask)
        );

        //Then
        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        verify(this.deploymentTaskRepository, times(1)).findById(anyInt());
    }

    @Test
    void onTaskreplyDeployProTodoTaskDone()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.DEPLOY_PRO);
        deploymentTask.setDeploymentPlan(deploymentPlan);
        var status = ToDoTaskStatus.DONE;

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentsValidator, times(1)).checkDeploymentDate(deploymentPlan);
        verify(this.deployerService, times(1)).deploy(any(), any(), eq(true));
    }

    @Test
    void onTaskreplyDeployProTodoTaskDoneWithErrors()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.DEPLOY_PRO);
        deploymentTask.setDeploymentPlan(deploymentPlan);
        String status = ToDoTaskStatus.DONE.toString();

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));
        doThrow(new NovaException(DeploymentError.getUnexpectedError())).when(this.deploymentsValidator).checkDeploymentDate(any());
        when(this.errorTaskManager.createGenericTask(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), DeploymentError.getUnexpectedError().getErrorMessage(), ToDoTaskType.DEPLOY_ERROR,
                Constants.IMMUSER, deploymentPlan.getId(), null, RoleType.PLATFORM_ADMIN.getType())).thenReturn(1);

        taskProcessor.onTaskReply(deploymentPlan, taskId, status);
        Assertions.assertEquals(DeploymentStatus.REJECTED, deploymentPlan.getStatus());
        //verify
        verify(this.alertServiceApiClient, times(1)).registerProductAlert(any());
        verify(this.deploymentsValidator, times(1)).checkDeploymentDate(deploymentPlan);
        verify(this.deployerService, times(0)).deploy(any(), any(), eq(true));
        verify(this.errorTaskManager, times(1)).createGenericTask(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(),
                DeploymentError.getUnexpectedError().getErrorMessage(), ToDoTaskType.DEPLOY_ERROR, Constants.IMMUSER, deploymentPlan.getId(), null, RoleType.PLATFORM_ADMIN.getType());
    }

    @Test
    void onTaskreplyDeployProTodoTaskReject()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.DEPLOY_PRO);
        var status = ToDoTaskStatus.REJECTED;

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        Assertions.assertEquals(DeploymentStatus.REJECTED, deploymentPlan.getStatus());
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
    }

    @Test
    void onTaskreplyDeployUnProTodoTaskDone()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var scheduledPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.UNDEPLOY_PRO);
        deploymentTask.setAssignedUserCode("UserCode");
        deploymentTask.setDeploymentPlan(
                MocksAndUtils.createDeploymentPlan(
                        false, false, false, Environment.PRO)
        );
        var status = ToDoTaskStatus.DONE;
        var deploymentGcsp = new DeploymentGcsp();
        var deploymentNova = MocksAndUtils.createDeploymentNova(0, DeploymentPriority.SERVICE);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));
        when(this.deploymentGcspRepo.findFirstByUndeployRelease(deploymentPlan.getId())).
                thenReturn(deploymentGcsp);
        when(this.deploymentPlanRepository.findByGcsp(deploymentGcsp)).thenReturn(scheduledPlan);
        when(this.deploymentNovaRepo.findFirstByUndeployRelease(deploymentPlan.getId())).
                thenReturn(deploymentNova);
        when(this.deploymentPlanRepository.findByNova(deploymentNova)).thenReturn(scheduledPlan);

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentTaskRepository, times(1)).findById(taskId);
        verify(this.deploymentGcspRepo, times(1)).findFirstByUndeployRelease(deploymentPlan.getId());
        verify(this.deploymentPlanRepository, times(1)).findByGcsp(deploymentGcsp);
        verify(this.deploymentNovaRepo, times(1)).findFirstByUndeployRelease(deploymentPlan.getId());

        verify(this.deploymentsValidator, times(2)).checkDeploymentDate(scheduledPlan);
        verify(this.IRemoverService, times(1)).
                undeployPlan(deploymentTask.getDeploymentPlan(), deploymentTask.getAssignedUserCode());
    }

    @Test
    void onTaskreplyDeploymentTypeChargeTodoTaskDone()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.DEPLOYMENT_TYPE_CHANGE);
        deploymentTask.setId(ThreadLocalRandom.current().nextInt());
        deploymentTask.setDeploymentPlan(deploymentPlan);
        var status = ToDoTaskStatus.DONE;

        var changeDeploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var changeTask = new DeploymentTypeChangeTask();
        changeTask.setDeploymentPlan(changeDeploymentPlan);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));
        when(this.typeChangeRepository.findById(deploymentTask.getId())).thenReturn(Optional.of(changeTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.typeChangeRepository, times(1)).findById(deploymentTask.getId());
        verify(this.deploymentPlanRepository, times(1)).save(changeDeploymentPlan);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any());
        verify(this.changeRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void onTaskreplyShedulePlanningTodoTaskError()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.ERROR;
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.SCHEDULE_PLANNING);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
        Assertions.assertEquals(DeploymentStatus.DEFINITION, deploymentPlan.getStatus());
    }

    @Test
    void onTaskreplyShedulePlanningTodoTaskRejected()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.REJECTED;
        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.SCHEDULE_PLANNING);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
        Assertions.assertEquals(DeploymentStatus.REJECTED, deploymentPlan.getStatus());
    }

    @Test
    void onTaskreplyNovaPlanningTodoTaskDone()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.DONE;

        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.NOVA_PLANNING);
        var changeDeploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        deploymentTask.setDeploymentPlan(changeDeploymentPlan);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentsValidator, times(1)).checkNovaPlannedSchedulingDate(deploymentPlan);
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
        verify(this.deployerService, times(1)).
                activateNovaPlannedDeployment(any(), any());
        Assertions.assertEquals(DeploymentStatus.SCHEDULED, deploymentPlan.getStatus());
    }

    @Test
    void onTaskreplyNovaPlanningTodoTaskDoneWithException()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.DONE;

        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.NOVA_PLANNING);
        var changeDeploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        deploymentTask.setDeploymentPlan(changeDeploymentPlan);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));
        doThrow(new NovaException(DeploymentError.getInvalidInstancesNumberError(List.of("AAA")))).when(deploymentsValidator).checkNovaPlannedSchedulingDate(deploymentPlan);

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentsValidator, times(1)).checkNovaPlannedSchedulingDate(deploymentPlan);
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
        Assertions.assertEquals(DeploymentStatus.REJECTED, deploymentPlan.getStatus());
    }

    @Test
    void onTaskreplyNovaPlanningTodoTaskError()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.ERROR;

        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.NOVA_PLANNING);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
        Assertions.assertEquals(DeploymentStatus.DEFINITION, deploymentPlan.getStatus());
    }

    @Test
    void onTaskreplyNovaPlanningTodoTaskRejected()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.REJECTED;

        var deploymentTask = new DeploymentTask();
        deploymentTask.setTaskType(ToDoTaskType.NOVA_PLANNING);

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deploymentPlanRepository, times(1)).save(deploymentPlan);
        Assertions.assertEquals(DeploymentStatus.REJECTED, deploymentPlan.getStatus());
    }

    //CIBNOVAP-1489: The ProfileOfficeTask won't be create as it is not required
//    @Test
//    void onTaskreplyApprovePlanProfilingTodoTaskDone()
//    {
//        Integer taskId = ThreadLocalRandom.current().nextInt();
//        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
//                false, false, false, Environment.PRO);
//        var status = ToDoTaskStatus.DONE;
//
//        var deploymentTask = new DeploymentTask();
//        deploymentTask.setTaskType(ToDoTaskType.APPROVE_PLAN_PROFILING);
//
//        //when
//        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.of(deploymentTask));
//
//        taskProcessor.onTaskreply(deploymentPlan, taskId, status.toString());
//
//        //verify
//        verify(this.deployerService, times(1)).deployOnPro(any(), any(), any());
//    }

    @Test
    void onTaskreplyEmptyOptional()
    {
        Integer taskId = ThreadLocalRandom.current().nextInt();
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                false, false, false, Environment.PRO);
        var status = ToDoTaskStatus.DONE;

        //when
        when(this.deploymentTaskRepository.findById(taskId)).thenReturn(Optional.empty());

        taskProcessor.onTaskReply(deploymentPlan, taskId, status.toString());

        //verify
        verify(this.deployerService, times(0)).deployOnPro(any(), any(), any());
    }

}
