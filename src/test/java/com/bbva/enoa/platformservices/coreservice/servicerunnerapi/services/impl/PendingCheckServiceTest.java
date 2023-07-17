package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.impl;

import com.bbva.enoa.apirestgen.batchmanagerapi.model.Instance;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentInstanceType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class PendingCheckServiceTest
{

    @Mock
    private ManagementActionTaskRepository actionTaskRepository;
    @InjectMocks
    private PendingCheckService pendingCheckService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkPendingTasksTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getId(), toDoTaskTypesList.get(0),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        this.pendingCheckService.checkPendingTasks(deploymentPlan, toDoTaskTypesList);

        verify(this.actionTaskRepository, times(21)).findByRelatedIdAndTaskTypeAndStatusIn(anyInt(), any(ToDoTaskType.class), any(List.class));
    }

    @Test
    public void checkPendingTasksAnyPlanPendingTaskTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();
        List<ManagementActionTask> managementActionTaskList = this.generateManagementActionTaskList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getId(), toDoTaskTypesList.get(0),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(managementActionTaskList);

        Assertions.assertThrows(NovaException.class, () -> this.pendingCheckService.checkPendingTasks(deploymentPlan, toDoTaskTypesList));

    }

    @Test
    public void checkPendingTasksAnySubsystemPendingTaskTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();
        List<ManagementActionTask> managementActionTaskList = this.generateManagementActionTaskList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getId(), toDoTaskTypesList.get(0),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getId(), toDoTaskTypesList.get(3),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(managementActionTaskList);

        Assertions.assertThrows(NovaException.class, () -> this.pendingCheckService.checkPendingTasks(deploymentPlan, toDoTaskTypesList));

    }

    @Test
    public void checkPendingTasksAnyServicePendingTaskTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();
        List<ManagementActionTask> managementActionTaskList = this.generateManagementActionTaskList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getId(), toDoTaskTypesList.get(0),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getId(), toDoTaskTypesList.get(3),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(
                deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getId(), toDoTaskTypesList.get(6),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(managementActionTaskList);

        Assertions.assertThrows(NovaException.class, () -> this.pendingCheckService.checkPendingTasks(deploymentPlan, toDoTaskTypesList));

    }

    @Test
    public void checkPendingTasksAnyInstancePendingTaskTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();
        List<ManagementActionTask> managementActionTaskList = this.generateManagementActionTaskList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getId(), toDoTaskTypesList.get(0),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getId(), toDoTaskTypesList.get(3),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getId(), toDoTaskTypesList.get(6),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0).getId(), toDoTaskTypesList.get(9),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(managementActionTaskList);

        Assertions.assertThrows(NovaException.class, () -> this.pendingCheckService.checkPendingTasks(deploymentPlan, toDoTaskTypesList));

    }

    @Test
    public void checkPendingTasksSubsystemTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getId(),
                toDoTaskTypesList.get(3),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        this.pendingCheckService.checkPendingTasks(deploymentPlan.getDeploymentSubsystems().get(0), toDoTaskTypesList);

        verify(this.actionTaskRepository, times(21)).findByRelatedIdAndTaskTypeAndStatusIn(anyInt(), any(ToDoTaskType.class), any(List.class));
    }

    @Test
    public void checkPendingTasksServiceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getId(),
                toDoTaskTypesList.get(6),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        this.pendingCheckService.checkPendingTasks(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0), toDoTaskTypesList);

        verify(this.actionTaskRepository, times(21)).findByRelatedIdAndTaskTypeAndStatusIn(anyInt(), any(ToDoTaskType.class), any(List.class));
    }

    @Test
    public void checkPendingTasksDeploymentInstanceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0).getId(),
                toDoTaskTypesList.get(9),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        this.pendingCheckService.checkPendingTasks(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0), toDoTaskTypesList);

        verify(this.actionTaskRepository, times(21)).findByRelatedIdAndTaskTypeAndStatusIn(anyInt(), any(ToDoTaskType.class), any(List.class));
    }

    @Test
    public void checkPendingTasksInstanceTest()
    {
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();
        Instance instance = this.generateInstance();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(instance.getId().intValue(),
                toDoTaskTypesList.get(9),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(Collections.emptyList());

        this.pendingCheckService.checkPendingTasks(instance.getId().intValue(), toDoTaskTypesList);

        verify(this.actionTaskRepository, times(12)).findByRelatedIdAndTaskTypeAndStatusIn(anyInt(), any(ToDoTaskType.class), any(List.class));
    }

    @Test
    public void checkPendingTasksInstanceErrorTest()
    {
        List<ToDoTaskType> toDoTaskTypesList = this.generateToDoTaskTypeList();
        Instance instance = this.generateInstance();

        when(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(instance.getId().intValue(),
                toDoTaskTypesList.get(9),
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)))
                .thenReturn(this.generateManagementActionTaskList());

        Assertions.assertThrows(NovaException.class, () -> this.pendingCheckService.checkPendingTasks(instance.getId().intValue(), toDoTaskTypesList));
    }

    private List<ToDoTaskType> generateToDoTaskTypeList()
    {

        List<ToDoTaskType> toDoTaskTypes = new ArrayList<>();

        //Release
        toDoTaskTypes.add(ToDoTaskType.RELEASE_START);
        toDoTaskTypes.add(ToDoTaskType.RELEASE_RESTART);
        toDoTaskTypes.add(ToDoTaskType.RELEASE_STOP);
        //Subsystem
        toDoTaskTypes.add(ToDoTaskType.SUBSYSTEM_START);
        toDoTaskTypes.add(ToDoTaskType.SUBSYSTEM_RESTART);
        toDoTaskTypes.add(ToDoTaskType.SUBSYSTEM_STOP);
        //Service
        toDoTaskTypes.add(ToDoTaskType.SERVICE_START);
        toDoTaskTypes.add(ToDoTaskType.SERVICE_RESTART);
        toDoTaskTypes.add(ToDoTaskType.SERVICE_STOP);
        //Instances
        toDoTaskTypes.add(ToDoTaskType.CONTAINER_START);
        toDoTaskTypes.add(ToDoTaskType.CONTAINER_RESTART);
        toDoTaskTypes.add(ToDoTaskType.CONTAINER_STOP);

        return toDoTaskTypes;

    }

    private List<ManagementActionTask> generateManagementActionTaskList()
    {

        List<ManagementActionTask> managementActionTaskList = new ArrayList<>();
        ManagementActionTask managementActionTaskToAdd = new ManagementActionTask();
        managementActionTaskToAdd.setId(1);
        managementActionTaskToAdd.setDescription("DESCRIPTION");
        managementActionTaskToAdd.setRelatedId(2);
        managementActionTaskToAdd.setStatus(ToDoTaskStatus.DONE);
        managementActionTaskToAdd.setTaskType(ToDoTaskType.CONFIGURATION_TASK);

        managementActionTaskList.add(managementActionTaskToAdd);

        return managementActionTaskList;

    }

    private DeploymentPlan generateDeploymentPlan()
    {

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());
        deploymentPlan.setAction(DeploymentAction.READY);


        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setId(2);
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setId(3);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        DeploymentInstance deploymentInstance = new DeploymentInstance()
        {
            @Override
            public DeploymentInstanceType getType()
            {
                return DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA;
            }

        };
        deploymentInstance.setId(4);

        deploymentInstance.setService(deploymentService);
        deploymentService.setInstances(Arrays.asList(deploymentInstance));
        deploymentSubsystem.setDeploymentServices(Arrays.asList(deploymentService));
        deploymentPlan.setDeploymentSubsystems(Arrays.asList(deploymentSubsystem));


        return deploymentPlan;

    }

    private Instance generateInstance()
    {
        Instance instance = new Instance();
        instance.setId(5L);
        instance.setState("STATE");
        instance.setBatchName("BATCHNAME");
        instance.setContainerName("CONTAINERNAME");
        instance.setNovaInstanceId(5L);

        return instance;
    }

}