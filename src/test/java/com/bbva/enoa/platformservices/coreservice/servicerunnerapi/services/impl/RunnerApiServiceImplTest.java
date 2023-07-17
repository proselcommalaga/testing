package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.impl;

import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentInstanceType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.ITaskProcessor;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces.IServiceRunner;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.enumerates.DeploymentInstanceStatus;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RunnerApiServiceImplTest
{

    @Mock
    private DeploymentInstanceRepository deploymentInstanceRepository;
    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;
    @Mock
    private DeploymentSubsystemRepository deploymentSubsystemRepository;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;
    @Mock
    private PendingCheckService pendingCheckService;
    @Mock
    private IProductUsersClient usersService;
    @Mock
    private IServiceRunner serviceRunner;
    @Mock
    private ITaskProcessor taskProcessor;
    @Mock
    private ManageValidationUtils manageValidationUtils;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private IDeploymentsService deploymentsService;

    @InjectMocks
    private RunnerApiServiceImpl runnerApiService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
        doNothing().when(this.usersService).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));
    }

    private DeploymentPlan generateDeploymentPlan()
    {

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());
        deploymentPlan.setAction(DeploymentAction.READY);

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(1);
        releaseVersion.setVersionName("RELEASEVERSIONNAME");

        Release release = new Release();
        release.setId(2);
        release.setName("RELEASENAME");

        Product product = new Product();
        product.setId(3);
        product.setName("PRODUCTNAME");


        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setId(1);
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setId(1);
        releaseVersionSubsystem.setTagName("SUBSYSTEMTAGNAME");

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setId(2);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setId(2);
        releaseVersionService.setFinalName("RELEASEVERSIONSERVICEFINALNAME");
        releaseVersionService.setDescription("Mocked release version service");



        DeploymentInstance deploymentInstance = new DeploymentInstance()
        {
            @Override
            public DeploymentInstanceType getType()
            {
                return DeploymentInstanceType.DEPLOYMENT_INSTANCE_NOVA;
            }

        };
        deploymentInstance.setId(3);

        deploymentInstance.setService(deploymentService);
        deploymentService.setInstances(Arrays.asList(deploymentInstance));
        deploymentSubsystem.setDeploymentServices(Arrays.asList(deploymentService));

        product.setReleases(Arrays.asList(release));
        release.setProduct(product);

        release.setReleaseVersions(Arrays.asList(releaseVersion));
        releaseVersion.setRelease(release);
        releaseVersion.setDeployments(Arrays.asList(deploymentPlan));

        releaseVersionSubsystem.setReleaseVersion(releaseVersion);

        releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);

        deploymentPlan.setReleaseVersion(releaseVersion);
        deploymentPlan.setDeploymentSubsystems(Arrays.asList(deploymentSubsystem));

        deploymentSubsystem.setDeploymentServices(Arrays.asList(deploymentService));
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        deploymentSubsystem.setSubsystem(releaseVersionSubsystem);

        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
        deploymentService.setService(releaseVersionService);
        deploymentService.setInstances(Arrays.asList(deploymentInstance));

        deploymentInstance.setService(deploymentService);


        return deploymentPlan;

    }

    private TodoTaskResponseDTO generateToDoTaskDTO()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.fillRandomly(2, false, 0, 3);

        return todoTaskResponseDTO;
    }

    @Test
    void stopBatchScheduleInstanceAutoManageByStatus()
    {
        //Must be int environment
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();

        deploymentPlan.setStatus(DeploymentStatus.UNDEPLOYED);

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        doNothing().when(this.serviceRunner).stopBatchScheduleInstance("CODE", 1);
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenCallRealMethod();

        com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void stopBatchScheduleInstanceAutoManageByEnvironment()
    {
        //Must be int environment
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();

        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        doNothing().when(this.serviceRunner).stopBatchScheduleInstance("CODE", 1);
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenCallRealMethod();

        com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void stopBatchScheduleInstanceNoPlan()
    {
        // If not plan mocked, throws an exception due to could not find any plan for th ID
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopBatchScheduleInstance(1, 1, "CODE"));

    }

    @Test
    void stopBatchScheduleInstanceUserNotAllowed()
    {
        //Must be int environment
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopBatchScheduleInstance(1, deploymentPlan.getId(), "CODE"));


    }

    @Test
    void stopBatchScheduleInstanceAutoManagePre()
    {
        //Must be pre environment and isAutomanageInPre
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        deploymentPlan.setEnvironment(Environment.PRE.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        doNothing().when(this.serviceRunner).stopBatchScheduleInstance("CODE", 1);
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);


        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");
        verify(this.serviceRunner, times(1)).stopBatchScheduleInstance("CODE", 1);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void stopBatchScheduleInstanceAutoManagePro() throws Errors
    {
        //Must be pre environment and isAutomanageInPre
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

        doNothing().when(this.serviceRunner).stopBatchScheduleInstance("CODE", 1);
        doNothing().when(this.pendingCheckService).checkPendingTasks(1, toDoTaskTypeList);
        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        when(this.serviceRunner.createBatchScheduleServiceProductionTask("CODE", 1, deploymentPlan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");

        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(1, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createBatchScheduleServiceProductionTask("CODE", 1, deploymentPlan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void resumeBatchScheduleInstanceNoPlan()
    {
        // If not plan mocked, throws an exception due to could not find any plan for th ID
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.resumeBatchScheduleInstance(1, 1, "CODE"));

    }

    @Test
    void resumeBatchScheduleInstanceUserNotAllowed()
    {
        //Must be int environment
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.resumeBatchScheduleInstance(1, deploymentPlan.getId(), "CODE"));


    }

    @Test
    void resumeBatchScheduleInstanceAutoManagePre()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        deploymentPlan.setEnvironment(Environment.PRE.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        doNothing().when(this.serviceRunner).resumeBatchScheduleInstance("CODE", 1);
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);


        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.resumeBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");
        verify(this.serviceRunner, times(1)).resumeBatchScheduleInstance("CODE", 1);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void resumeBatchScheduleInstanceAutoManagePro() throws Errors
    {
        //Must be pre environment and isAutomanageInPre
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

        doNothing().when(this.serviceRunner).stopBatchScheduleInstance("CODE", 1);
        doNothing().when(this.pendingCheckService).checkPendingTasks(1, toDoTaskTypeList);
        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        when(this.serviceRunner.createBatchScheduleServiceProductionTask("CODE", 1, deploymentPlan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.resumeBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");

        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(1, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createBatchScheduleServiceProductionTask("CODE", 1, deploymentPlan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void onServiceRunnerTaskReply() throws Errors
    {
        doNothing().when(this.taskProcessor).onTaskReply(anyInt(), anyString());
        this.runnerApiService.onServiceRunnerTaskReply(1, ToDoTaskStatus.DONE.name());
        verify(this.taskProcessor, times(1)).onTaskReply(1, ToDoTaskStatus.DONE.name());
    }

    @Test
    void pauseBatchScheduleInstanceNoPlan()
    {
        // If not plan mocked, throws an exception due to could not find any plan for th ID
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.pauseBatchScheduleInstance(1, 1, "CODE"));

    }

    @Test
    void pauseBatchScheduleInstanceUserNotAllowed()
    {
        //Must be int environment
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.pauseBatchScheduleInstance(1, deploymentPlan.getId(), "CODE"));


    }

    @Test
    void pauseBatchScheduleInstanceAutoManagePre()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        deploymentPlan.setEnvironment(Environment.PRE.getEnvironment());

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        doNothing().when(this.serviceRunner).pauseBatchScheduleInstance("CODE", 1);
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);


        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.pauseBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");
        verify(this.serviceRunner, times(1)).pauseBatchScheduleInstance("CODE", 1);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void pauseBatchScheduleInstanceAutoManagePro() throws Errors
    {
        //Must be pre environment and isAutomanageInPre
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP);

        doNothing().when(this.serviceRunner).pauseBatchScheduleInstance("CODE", 1);
        doNothing().when(this.pendingCheckService).checkPendingTasks(1, toDoTaskTypeList);
        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        when(this.serviceRunner.createBatchScheduleServiceProductionTask("CODE", 1, deploymentPlan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.pauseBatchScheduleInstance(1, deploymentPlan.getId(), "CODE");

        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(1, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createBatchScheduleServiceProductionTask("CODE", 1, deploymentPlan, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void startBatchScheduleInstanceNoPlan()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchScheduleInstance(1, 1, "CODE"));

    }

    @Test
    void startBatchScheduleInstanceNoRelease()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.releaseVersionServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.empty());

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchScheduleInstance(2, 1, "CODE"));

    }

    @Test
    void startBatchScheduleInstanceNoSubsystem()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.releaseVersionServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(releaseVersionService));
        when(this.deploymentSubsystemRepository.findByDeploymentPlanIdAndSubsystem(deploymentPlan.getId(), releaseVersionService.getVersionSubsystem())).thenReturn(null);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchScheduleInstance(2, 1, "CODE"));

    }

    @Test
    void startBatchScheduleInstanceNoService()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);


        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.releaseVersionServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(releaseVersionService));
        when(this.deploymentSubsystemRepository.findByDeploymentPlanIdAndSubsystem(deploymentPlan.getId(), releaseVersionService.getVersionSubsystem())).thenReturn(deploymentSubsystem);
        when(this.deploymentServiceRepository.findByServiceIdAndDeploymentSubsystem(releaseVersionService.getId(), deploymentSubsystem)).thenReturn(null);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchScheduleInstance(2, 1, "CODE"));

    }

    @Test
    void startBatchScheduleInstanceUserNotAllowed()
    {

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition to not allowed
        deploymentPlan.setEnvironment(Environment.PRE.getEnvironment());

        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.releaseVersionServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(releaseVersionService));
        when(this.deploymentSubsystemRepository.findByDeploymentPlanIdAndSubsystem(deploymentPlan.getId(), releaseVersionService.getVersionSubsystem())).thenReturn(deploymentSubsystem);
        when(this.deploymentServiceRepository.findByServiceIdAndDeploymentSubsystem(releaseVersionService.getId(), deploymentSubsystem)).thenReturn(deploymentService);

        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchScheduleInstance(2, 1, "CODE"));

    }

    @Test
    void startBatchScheduleInstanceAutoManagePre()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.releaseVersionServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(releaseVersionService));
        when(this.deploymentSubsystemRepository.findByDeploymentPlanIdAndSubsystem(deploymentPlan.getId(), releaseVersionService.getVersionSubsystem())).thenReturn(deploymentSubsystem);
        when(this.deploymentServiceRepository.findByServiceIdAndDeploymentSubsystem(releaseVersionService.getId(), deploymentSubsystem)).thenReturn(deploymentService);

        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startBatchScheduleInstance(2, 1, "CODE");
        verify(this.serviceRunner, times(1)).startBatchScheduleInstance("CODE", deploymentService, deploymentPlan);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void startBatchScheduleInstanceAutoManagePro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START);


        when(this.deploymentPlanRepository.findById(deploymentPlan.getId())).thenReturn(Optional.of(deploymentPlan));
        when(this.releaseVersionServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(releaseVersionService));
        when(this.deploymentSubsystemRepository.findByDeploymentPlanIdAndSubsystem(deploymentPlan.getId(), releaseVersionService.getVersionSubsystem())).thenReturn(deploymentSubsystem);
        when(this.deploymentServiceRepository.findByServiceIdAndDeploymentSubsystem(releaseVersionService.getId(), deploymentSubsystem)).thenReturn(deploymentService);

        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentService, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentService, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startBatchScheduleInstance(2, deploymentPlan.getId(), "CODE");


        verify(this.deploymentPlanRepository, times(1)).findById(deploymentPlan.getId());
        verify(this.releaseVersionServiceRepository, times(1)).findById(releaseVersionService.getId());
        verify(this.deploymentSubsystemRepository, times(1)).findByDeploymentPlanIdAndSubsystem(deploymentPlan.getId(), releaseVersionService.getVersionSubsystem());
        verify(this.deploymentServiceRepository, times(1)).findByServiceIdAndDeploymentSubsystem(releaseVersionService.getId(), deploymentSubsystem);
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentService, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentService, ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void startBatchScheduleNoService()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchSchedule(1, "CODE"));
    }

    @Test
    void startBatchScheduleNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);


        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startBatchSchedule(2, "CODE"));


    }

    @Test
    void startBatchScheduleNotAllowedAutoManagedPre()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startBatchSchedule(1, "CODE");

        verify(this.serviceRunner, times(1)).startBatchSchedule("CODE", deploymentService);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void startBatchScheduleNotAllowedAutoManagedPro()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.BATCH_SCHEDULE_START, ToDoTaskType.BATCH_SCHEDULE_STOP);


        when(this.deploymentServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentService, ToDoTaskType.BATCH_SCHEDULE_START)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startBatchSchedule(2, "CODE");

        verify(this.deploymentServiceRepository, times(1)).findById(releaseVersionService.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentService, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentService, ToDoTaskType.BATCH_SCHEDULE_START);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void stopSubsystemNoSubsystem()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopSubsystem(1, "CODE"));
    }

    @Test
    void stopSubsystemNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);


        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopSubsystem(1, "CODE"));

    }

    @Test
    void stopSubsystemNotAllowedAutoManagedPre()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);

        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopSubsystem(1, "CODE");

        verify(this.serviceRunner, times(1)).stopSubsystem("CODE", deploymentSubsystem);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void stopSubsystemNotAllowedAutoManagedPro()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.SUBSYSTEM_START, ToDoTaskType.SUBSYSTEM_RESTART, ToDoTaskType.SUBSYSTEM_STOP);


        when(this.deploymentSubsystemRepository.findById(deploymentSubsystem.getId())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentSubsystem, ToDoTaskType.SUBSYSTEM_STOP)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopSubsystem(1, "CODE");

        verify(this.deploymentSubsystemRepository, times(1)).findById(deploymentSubsystem.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentSubsystem, ToDoTaskType.SUBSYSTEM_STOP);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void startPlanNoPlan()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startPlan(1, "CODE"));
    }

    @Test
    void startPlanNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startPlan(1, "CODE"));

    }

    @Test
    void startPlanNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startPlan(1, "CODE");

        verify(this.serviceRunner, times(1)).startDeploymentPlan("CODE", deploymentPlan);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void startPlanNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.RELEASE_START, ToDoTaskType.RELEASE_RESTART, ToDoTaskType.RELEASE_STOP);

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentPlan, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentPlan, ToDoTaskType.RELEASE_START)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startPlan(1, "CODE");

        verify(this.deploymentPlanRepository, times(1)).findById(deploymentPlan.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentPlan, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentPlan, ToDoTaskType.RELEASE_START);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void restartPlanNoPlan()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartPlan(1, "CODE"));
    }

    @Test
    void restartPlanNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartPlan(1, "CODE"));

    }

    @Test
    void restartPlanNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartPlan(1, "CODE");

        verify(this.serviceRunner, times(1)).restartDeploymentPlan("CODE", deploymentPlan);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void restartPlanNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.RELEASE_START, ToDoTaskType.RELEASE_RESTART, ToDoTaskType.RELEASE_STOP);

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentPlan, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentPlan, ToDoTaskType.RELEASE_RESTART)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartPlan(1, "CODE");

        verify(this.deploymentPlanRepository, times(1)).findById(deploymentPlan.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentPlan, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentPlan, ToDoTaskType.RELEASE_RESTART);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void stoptPlanNoPlan()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopPlan(1, "CODE"));
    }

    @Test
    void stopPlanNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopPlan(1, "CODE"));

    }

    @Test
    void stopPlanNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopPlan(1, "CODE");

        verify(this.serviceRunner, times(1)).stopDeploymentPlan("CODE", deploymentPlan);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void stopPlanNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.RELEASE_RESTART, ToDoTaskType.RELEASE_START, ToDoTaskType.RELEASE_STOP);

        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentPlan, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentPlan, ToDoTaskType.RELEASE_STOP)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopPlan(1, "CODE");

        verify(this.deploymentPlanRepository, times(1)).findById(deploymentPlan.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentPlan, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentPlan, ToDoTaskType.RELEASE_STOP);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void startInstanceNoInstance()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startInstance(3, "CODE"));
    }

    @Test
    void startInstanceNotAllowed()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startInstance(3, "CODE"));

    }

    @Test
    void startInstanceNotAllowedAutoManagedPre() throws Errors
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startInstance(3, "CODE");

        verify(this.serviceRunner, times(1)).startInstance("CODE", deploymentInstance);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void startInstanceNotAllowedAutoManagedPro() throws Errors
    {

        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.CONTAINER_START,
                        ToDoTaskType.CONTAINER_RESTART,
                        ToDoTaskType.CONTAINER_STOP);

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentInstance, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentInstance, ToDoTaskType.CONTAINER_START)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startInstance(3, "CODE");

        verify(this.deploymentInstanceRepository, times(1)).findById(deploymentInstance.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentInstance, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentInstance, ToDoTaskType.CONTAINER_START);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void restartInstanceNoInstance()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartInstance(3, "CODE"));
    }

    @Test
    void restartInstanceNotAllowed()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartInstance(3, "CODE"));

    }

    @Test
    void restartInstanceNotAllowedAutoManagedPre() throws Errors
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartInstance(3, "CODE");

        verify(this.serviceRunner, times(1)).restartInstance("CODE", deploymentInstance);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void restartInstanceNotAllowedAutoManagedPro() throws Errors
    {

        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.CONTAINER_START,
                        ToDoTaskType.CONTAINER_RESTART,
                        ToDoTaskType.CONTAINER_STOP);

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentInstance, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentInstance, ToDoTaskType.CONTAINER_RESTART)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartInstance(3, "CODE");

        verify(this.deploymentInstanceRepository, times(1)).findById(deploymentInstance.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentInstance, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentInstance, ToDoTaskType.CONTAINER_RESTART);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }


    @Test
    void stopInstanceNoInstance()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopInstance(3, "CODE"));
    }

    @Test
    void stopInstanceNotAllowed()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopInstance(3, "CODE"));

    }

    @Test
    void stopInstanceNotAllowedAutoManagedPre() throws Errors
    {
        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopInstance(3, "CODE");

        verify(this.serviceRunner, times(1)).stopInstance("CODE", deploymentInstance);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void stopInstanceNotAllowedAutoManagedPro() throws Errors
    {

        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.CONTAINER_START,
                        ToDoTaskType.CONTAINER_RESTART,
                        ToDoTaskType.CONTAINER_STOP);

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentInstance, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentInstance, ToDoTaskType.CONTAINER_STOP)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopInstance(3, "CODE");

        verify(this.deploymentInstanceRepository, times(1)).findById(deploymentInstance.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentInstance, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentInstance, ToDoTaskType.CONTAINER_STOP);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void startSubsystemNoSubsystem()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startSubsystem(1, "CODE"));
    }

    @Test
    void startSubsystemNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);


        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startSubsystem(1, "CODE"));

    }

    @Test
    void startSubsystemNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);

        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startSubsystem(1, "CODE");

        verify(this.serviceRunner, times(1)).startSubsystem("CODE", deploymentSubsystem);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void startSubsystemNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.SUBSYSTEM_START, ToDoTaskType.SUBSYSTEM_RESTART, ToDoTaskType.SUBSYSTEM_STOP);


        when(this.deploymentSubsystemRepository.findById(deploymentSubsystem.getId())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentSubsystem, ToDoTaskType.SUBSYSTEM_START)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startSubsystem(1, "CODE");

        verify(this.deploymentSubsystemRepository, times(1)).findById(deploymentSubsystem.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentSubsystem, ToDoTaskType.SUBSYSTEM_START);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void restartSubsystemNoSubsystem()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartSubsystem(1, "CODE"));
    }

    @Test
    void restartSubsystemNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);


        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);

        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartSubsystem(1, "CODE"));

    }

    @Test
    void restartSubsystemNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);

        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartSubsystem(1, "CODE");

        verify(this.serviceRunner, times(1)).restartSubsystem("CODE", deploymentSubsystem);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());

    }

    @Test
    void restartSubsystemNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.SUBSYSTEM_START, ToDoTaskType.SUBSYSTEM_RESTART, ToDoTaskType.SUBSYSTEM_STOP);


        when(this.deploymentSubsystemRepository.findById(deploymentSubsystem.getId())).thenReturn(Optional.of(deploymentSubsystem));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentSubsystem, ToDoTaskType.SUBSYSTEM_RESTART)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartSubsystem(1, "CODE");

        verify(this.deploymentSubsystemRepository, times(1)).findById(deploymentSubsystem.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentSubsystem, ToDoTaskType.SUBSYSTEM_RESTART);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void stopBatchScheduleNoService()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopBatchSchedule(1, "CODE"));
    }

    @Test
    void stopBatchScheduleNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);


        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopBatchSchedule(2, "CODE"));


    }

    @Test
    void stopBatchScheduleNotAllowedAutoManagedPre()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopBatchSchedule(1, "CODE");

        verify(this.serviceRunner, times(1)).stopBatchSchedule("CODE", deploymentService);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void stopBatchScheduleNotAllowedAutoManagedPro()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.BATCH_SCHEDULE_STOP, ToDoTaskType.BATCH_SCHEDULE_START);

        when(this.deploymentServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentService, ToDoTaskType.BATCH_SCHEDULE_STOP)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopBatchSchedule(2, "CODE");

        verify(this.deploymentServiceRepository, times(1)).findById(releaseVersionService.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentService, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentService, ToDoTaskType.BATCH_SCHEDULE_STOP);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void startServiceNoService()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startService(2, "CODE"));
    }

    @Test
    void startServiceNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);


        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.startService(2, "CODE"));


    }

    @Test
    void startServiceNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startService(2, "CODE");

        verify(this.serviceRunner, times(1)).startService("CODE", deploymentService);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void startServiceNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.SERVICE_START, ToDoTaskType.SERVICE_RESTART, ToDoTaskType.SERVICE_STOP);

        when(this.deploymentServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentService, ToDoTaskType.SERVICE_START)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.startService(2, "CODE");

        verify(this.deploymentServiceRepository, times(1)).findById(releaseVersionService.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentService, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentService, ToDoTaskType.SERVICE_START);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void restartServiceNoService()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartService(2, "CODE"));
    }

    @Test
    void restartServiceNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);


        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartService(2, "CODE"));


    }

    @Test
    void restartServiceNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartService(2, "CODE");

        verify(this.serviceRunner, times(1)).restartService("CODE", deploymentService);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void restartServiceNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.SERVICE_START, ToDoTaskType.SERVICE_RESTART, ToDoTaskType.SERVICE_STOP);

        when(this.deploymentServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentService, ToDoTaskType.SERVICE_RESTART)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.restartService(2, "CODE");

        verify(this.deploymentServiceRepository, times(1)).findById(releaseVersionService.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentService, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentService, ToDoTaskType.SERVICE_RESTART);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void stopServiceNoService()
    {
        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopService(2, "CODE"));
    }

    @Test
    void stopServiceNotAllowed()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        //Forcing condition not allowed
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);


        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);


        Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.stopService(2, "CODE"));


    }

    @Test
    void stopServiceNotAllowedAutoManagedPre() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(true);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopService(2, "CODE");

        verify(this.serviceRunner, times(1)).stopService("CODE", deploymentService);
        verify(this.manageValidationUtils, times(1)).checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan);
        Assertions.assertEquals(false, taskResponseDTOReturned.getGenerated());
    }

    @Test
    void stopServiceNotAllowedAutoManagedPro() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0);
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Forcing to get correct condition
        deploymentPlan.setEnvironment(Environment.PRO.getEnvironment());

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        // Forcing to mock result
        todoTaskResponseDTO.setGenerated(true);

        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.SERVICE_START, ToDoTaskType.SERVICE_RESTART, ToDoTaskType.SERVICE_STOP);

        when(this.deploymentServiceRepository.findById(releaseVersionService.getId())).thenReturn(Optional.of(deploymentService));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("CODE", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(deploymentSubsystem, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("CODE", deploymentService, ToDoTaskType.SERVICE_STOP)).thenReturn(todoTaskResponseDTO);

        TodoTaskResponseDTO taskResponseDTOReturned = this.runnerApiService.stopService(2, "CODE");

        verify(this.deploymentServiceRepository, times(1)).findById(releaseVersionService.getId());
        verify(this.pendingCheckService, times(1)).checkPendingTasks(deploymentService, toDoTaskTypeList);
        verify(this.serviceRunner, times(1)).createProductionTask("CODE", deploymentService, ToDoTaskType.SERVICE_STOP);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
        Assertions.assertEquals(true, taskResponseDTOReturned.getGenerated());


    }

    @Test
    void getInstancesStatuses()
    {
        String[] returnResult = this.runnerApiService.getInstancesStatuses();
        Assertions.assertNotNull(returnResult);

        //Check all the defined values in DeploymentInstanceStatus class have been obtained
        Assertions.assertEquals(2, returnResult.length);
        Assertions.assertEquals(DeploymentInstanceStatus.getValueOf("RUNNING").toString(), Arrays.stream(returnResult).filter("RUNNING"::equals).findFirst().get());
        Assertions.assertEquals(DeploymentInstanceStatus.getValueOf("STOPPED").toString(), Arrays.stream(returnResult).filter("STOPPED"::equals).findFirst().get());

    }

    @Test
    void restartEphoenixInstanceByPlanification_WrongUser_ForbiddenThrown()
    {
        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartEphoenixInstanceByPlanification("XE70505","INT","hostname","productName","releaseName","subsystemName","serviceName"));

        // Check exception
        Assertions.assertEquals(exception.getErrorCode().getErrorCode(), ServiceRunnerError.getForbiddenError().getErrorCode(), "getForbidden Error thrown when user is not IMM0589");

    }

    @Test
    void restartEphoenixInstanceByPlanification_WrongEnvironment_BadRequestThrown()
    {
        String environment = "WRONG_ENV";
        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartEphoenixInstanceByPlanification("IMM0589",environment,"hostname","productName","releaseName","subsystemName","serviceName"));

        // Check exception
        Assertions.assertEquals(exception.getErrorCode().getErrorCode(), ServiceRunnerError.InvalidInputParameter("environment", environment).getErrorCode(), "getForbidden Error thrown when user is not IMM0589");

    }

    @Test
    void restartEphoenixInstanceByPlanification_WrongServiceName_NoSuchDeploymentServiceErrorThrown_With_Rest_Api()
    {
        restartEphoenixInstanceByPlanification_WrongServiceName_NoSuchDeploymentServiceErrorThrown(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void restartEphoenixInstanceByPlanification_WrongServiceName_NoSuchDeploymentServiceErrorThrown_With_Api()
    {
        restartEphoenixInstanceByPlanification_WrongServiceName_NoSuchDeploymentServiceErrorThrown(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void restartEphoenixInstanceByPlanification_WrongServiceName_NoSuchDeploymentServiceErrorThrown(ServiceType serviceType)
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Force test condition (not ePhoenix service type)
        releaseVersionService.setServiceType(serviceType.getServiceType());
        deploymentService.setService(releaseVersionService);

        when(this.deploymentsService.getDeploymentServiceByName("productName", "INT", "releaseName", "subsystemName", "serviceName")).thenReturn(null);

        // Check exception
        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartEphoenixInstanceByPlanification("IMM0589","INT","hostname","productName","releaseName","subsystemName","serviceName"));
        Assertions.assertEquals(exception.getErrorCode().getErrorCode(), ServiceRunnerError.getNoSuchDeploymentServiceError("serviceName").getErrorCode(), "getNoSuchDeploymentServiceError thrown when service is not found");

    }

    @Test
    void restartEphoenixInstanceByPlanification_NotEphoenixService_ForbiddenThrown_With_Api_Rest()
    {
        restartEphoenixInstanceByPlanification_NotEphoenixService_ForbiddenThrown(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void restartEphoenixInstanceByPlanification_NotEphoenixService_ForbiddenThrown_With_Api()
    {
        restartEphoenixInstanceByPlanification_NotEphoenixService_ForbiddenThrown(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void restartEphoenixInstanceByPlanification_NotEphoenixService_ForbiddenThrown(ServiceType serviceType)
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Force test condition (not ePhoenix service type)
        releaseVersionService.setServiceType(serviceType.getServiceType());
        deploymentService.setService(releaseVersionService);

        when(this.deploymentsService.getDeploymentServiceByName("productName", "INT", "releaseName", "subsystemName", "serviceName")).thenReturn(deploymentService);

        // Check exception
        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartEphoenixInstanceByPlanification("IMM0589","INT","hostname","productName","releaseName","subsystemName","serviceName"));
        Assertions.assertEquals(exception.getErrorCode().getErrorCode(), ServiceRunnerError.getRestartEphoenixInstanceForbiddenError(deploymentService.getService().getServiceName(), "PRODUCT").getErrorCode(), "getForbidden Error thrown when service type is not ePhoenix type");

    }

    @Test
    void restartEphoenixInstanceByPlanification_NoDeploymentInstanceFound_getNoSuchInstanceErrorThrown()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Force test condition (ePhoenix service type)
        releaseVersionService.setServiceType(ServiceType.EPHOENIX_BATCH.getServiceType());
        deploymentService.setService(releaseVersionService);

        when(this.deploymentsService.getDeploymentServiceByName("productName", "INT", "releaseName", "subsystemName", "serviceName")).thenReturn(deploymentService);
        when(this.deploymentInstanceRepository.getDeploymentInstanceByHostNameAndServiceId("hostname", deploymentService.getId())).thenReturn(Optional.of(new NovaDeploymentInstance()));

        // Check exception
        NovaException exception = Assertions.assertThrows(NovaException.class, () -> this.runnerApiService.restartEphoenixInstanceByPlanification("IMM0589","INT","hostname","productName","releaseName","subsystemName","serviceName"));
        Assertions.assertEquals(exception.getErrorCode().getErrorCode(), ServiceRunnerError.getNoSuchInstanceError().getErrorCode(), "getNoSuchInstanceError thrown when could not find a deploymentInstances whit hostname and serviceId values");

    }


    @Test
    void restartEphoenixInstanceByPlanification_Ok() throws Errors
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService();
        DeploymentService deploymentService = deploymentPlan.getReleaseVersion().getDeployments().get(0).getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        // Force test condition (ePhoenix service type)
        releaseVersionService.setServiceType(ServiceType.EPHOENIX_BATCH.getServiceType());
        deploymentService.setService(releaseVersionService);

        TodoTaskResponseDTO todoTaskResponseDTO = this.generateToDoTaskDTO();
        todoTaskResponseDTO.setGenerated(false);

        NovaDeploymentInstance novaDeploymentInstance = new NovaDeploymentInstance();
        novaDeploymentInstance.setService(deploymentService);
        novaDeploymentInstance.setId(deploymentService.getInstances().get(0).getId());
        novaDeploymentInstance.setHostName("hostname");
        List<ToDoTaskType> toDoTaskTypeList =
                Arrays.asList(ToDoTaskType.RESTART_INSTANCE_ERROR);

        when(this.deploymentsService.getDeploymentServiceByName("productName", "INT", "releaseName", "subsystemName", "serviceName")).thenReturn(deploymentService);
        when(this.deploymentInstanceRepository.getDeploymentInstanceByHostNameAndServiceId("hostname", deploymentService.getId())).thenReturn(Optional.of(novaDeploymentInstance));
        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of((DeploymentInstance) novaDeploymentInstance));
        when(this.manageValidationUtils.checkIfServiceActionCanBeManagedByUser("IMM0589", deploymentPlan)).thenReturn(false);
        doNothing().when(this.pendingCheckService).checkPendingTasks(novaDeploymentInstance, toDoTaskTypeList);
        when(this.serviceRunner.createProductionTask("IMM0589", novaDeploymentInstance, ToDoTaskType.CONTAINER_RESTART)).thenReturn(todoTaskResponseDTO);
        when(this.runnerApiService.restartInstance(anyInt(),"IMM0589")).thenReturn(todoTaskResponseDTO);

        // Call method
        Integer instanceId = this.runnerApiService.restartEphoenixInstanceByPlanification("IMM0589","INT","hostname","productName","releaseName","subsystemName","serviceName");

        Assertions.assertEquals(deploymentService.getInstances().get(0).getId(), instanceId, "Instance restarted should be the same as instance returned by repository");
    }
}