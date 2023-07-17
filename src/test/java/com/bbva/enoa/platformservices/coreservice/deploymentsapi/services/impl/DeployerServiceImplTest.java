package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.enumerates.GBType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.TimeInterval;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerConfigurator;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentGcspService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentNovaService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeployerServiceImplTest
{
    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;
    @Mock
    private IDeploymentNovaService novaPlannedService;
    @Mock
    private IDeploymentGcspService gcspService;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private ISchedulerManagerClient schedulerManagerClient;
    @Mock
    private IToolsClient toolsService;
    @Mock
    private DeploymentSubsystemRepository deploymentSubsystemRepository;
    @Mock
    private IUsersClient usersService;
    @Mock
    private DeploymentUtils deploymentUtils;
    @Mock
    private IApiGatewayService apiGatewayService;
    @Mock
    private ConfigurationmanagerClient configurationmanagerClient;
    @Mock
    private ILibraryManagerService libraryManagerService;
    @Mock
    private IDeploymentManagerService deploymentmanagerService;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IBudgetsService budgetsService;
    @Mock
    private IBrokerConfigurator brokerConfigurator;
    @InjectMocks
    private DeployerServiceImpl deployerService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.deployerService, "maxRiskLevelThreshold", 3);
        ReflectionTestUtils.setField(this.deployerService, "budgetsEnabled", true);
        ReflectionTestUtils.setField(this.deployerService, "cesEnabled", false);
    }

    private void prepareCommonMocks(DeploymentPlan plan, DeploymentPlan deployedplan)
    {
        //WHEN
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(plan));
        when(this.budgetsService.checkProductServices(anyInt(), any(GBType.class))).thenReturn(true);
        when(this.budgetsService.checkDeploymentPlanDeployabilityStatus(anyInt())).thenReturn(true);
        when(this.deploymentPlanRepository
                .findFirstByReleaseVersionReleaseIdAndEnvironmentAndStatus(anyInt(), anyString(), any(DeploymentStatus.class))).
                thenReturn(deployedplan);
        if(Objects.isNull(deployedplan))
        {
            when(this.deploymentPlanRepository.saveAndFlush(plan)).thenReturn(plan);
        }
        else
        {
            when(this.deploymentmanagerService.replacePlan(any(), any())).thenReturn(true);
        }
    }

    private void verifyExecution(DeploymentPlan plan, boolean replace)
    {
        if(replace)
        {
            verify(this.deploymentmanagerService, times(1)).
                    replacePlan(any(), any());
        }
        else
        {
            //VERIFY
            verify(this.deploymentPlanRepository, times(1)).saveAndFlush(plan);
            verify(this.deploymentmanagerService, times(1)).deployPlan(plan);
        }
    }

    @Test
    public void deployWithoutEnvironment() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
        plan.setEnvironment(Environment.PORTAL.getEnvironment());

        //THEN
        assertThrows(NovaException.class, () ->
                this.deployerService.deployOnEnvironment("CODE", 1, true)
        );
    }

    @Test
    public void deployOnIntEnvironmentNovaException() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

        prepareCommonMocks(plan, null);
        when(this.budgetsService.checkProductServices(anyInt(), eq(GBType.SERVICIOS_NOVA))).thenReturn(false);

        assertThrows(NovaException.class, () ->
                this.deployerService.deployOnEnvironment("CODE", 1, true)
        );
    }

    @Test
    public void deployOnIntEnvironment() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
        TOSubsystemDTO subsystemDTO = MocksAndUtils.createTOSubsystemDTO();
        TodoTaskResponseDTO responseMock = new TodoTaskResponseDTO();
        responseMock.setGenerated(false);

        prepareCommonMocks(plan, null);
        when(this.deploymentUtils.builderTodoTaskResponseDTO(false)).thenReturn(responseMock);
        when(this.toolsService.getSubsystemById(any())).thenReturn(subsystemDTO);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //Verify
        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, false);
        verify(this.deploymentUtils, times(1)).builderTodoTaskResponseDTO(false);
        verify(this.deploymentUtils, times(1)).builderTodoTaskResponseDTO(false);
    }

    @Test
    public void deployOnPreEnvironmentNovaException() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);
        TOSubsystemDTO subsystemDTO = MocksAndUtils.createTOSubsystemDTO();

        prepareCommonMocks(plan, null);
        when(this.budgetsService.checkProductServices(anyInt(), eq(GBType.SERVICIOS_SQA))).thenReturn(false);

        assertThrows(NovaException.class, () ->
                this.deployerService.deployOnEnvironment("CODE", 1, true)
        );
    }

    @Test
    public void deployOnPreEnvironment() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);
        TOSubsystemDTO subsystemDTO = MocksAndUtils.createTOSubsystemDTO();

        prepareCommonMocks(plan, null);
        when(this.usersService.hasPermission(any())).thenReturn(true);
        when(this.toolsService.getSubsystemById(any())).thenReturn(subsystemDTO);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, false);
        verify(this.usersService, times(1)).hasPermission(any());
    }

    @Test
    public void deployOnPreEnvironmentWithoutPermissionAndEnabledAutodeployAndQuality() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);
        TOSubsystemDTO subsystemDTO = MocksAndUtils.createTOSubsystemDTO();

        // Set the intervalr from now 1000 seconds for autodeployInPre
        plan.getReleaseVersion().getRelease().setAutodeployInPre(
                new TimeInterval(new Date(),
                        new Date( new Date().getTime() + 1_000_000 )));
        plan.getReleaseVersion().setQualityValidation(true);

        prepareCommonMocks(plan, null);
        when(this.toolsService.getSubsystemById(any())).thenReturn(subsystemDTO);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, false);
        verify(this.usersService, times(1)).hasPermission(any());
    }

    private TodoTaskResponseDTO createTodoTaskResponseDTO(int taskId)
    {
        var todoTask = new TodoTaskResponseDTO();
        todoTask.setTodoTaskId(taskId);
        todoTask.setGenerated(true);
        return todoTask;
    }

    private void prepareTodoTaskMocks(DeploymentPlan plan, int taskId, ToDoTaskType type)
    {
        TodoTaskResponseDTO todoTask = createTodoTaskResponseDTO(taskId);

        when(this.todoTaskServiceClient.createDeploymentTask(
                eq(plan.getId()),
                any(),
                eq(type.toString()),
                any(),
                any(),
                any()
        )).thenReturn(taskId);
        when(this.deploymentUtils.builderTodoTaskResponseDTO(
                eq(true), any(), eq(taskId), eq(type)
        )).thenReturn(todoTask);
    }

    private void verifyTodoTaskMocks(DeploymentPlan plan, int taskId, ToDoTaskType type)
    {
        if(ToDoTaskType.DEPLOY_PRE.equals(type))
        {
            verify(this.usersService, times(1)).hasPermission(any());
        }
        else if(ToDoTaskType.DEPLOY_PRO.equals(type))
        {
            verify(this.usersService, times(1)).isPlatformAdmin(any());
        }

        verify(this.todoTaskServiceClient, times(1)).createDeploymentTask(
                eq(plan.getId()),
                any(),
                eq(type.toString()),
                any(),
                any(),
                any()
        );
        verify(this.deploymentUtils, times(1)).builderTodoTaskResponseDTO(
                eq(true), any(), eq(taskId), eq(type));
    }

    @Test
    public void deployOnPreEnvironmentWithoutPermissionAndEnabledAutodeploy() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.DEPLOY_PRE;

        // Set the interval from now 1000 seconds for autodeployInPre
        plan.getReleaseVersion().getRelease().setAutodeployInPre(
                new TimeInterval(new Date(),
                        new Date( new Date().getTime() + 1_000_000 )));

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnPreEnvironmentWithoutPermission() throws Exception
    {
        var plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.DEPLOY_PRE;

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnProEnvironmentWithDocumentation() throws Exception
    {

        ReflectionTestUtils.setField(this.deployerService, "validateDocumentsBeforeDeployingLogicalConnectors", true);
        ReflectionTestUtils.setField(this.deployerService, "validateDocumentsBeforeDeployingNovaApis", true);
        TOSubsystemDTO subsystemDTO = MocksAndUtils.createTOSubsystemDTO();

        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, true, false, Environment.PRO);

        prepareCommonMocks(plan, null);
        when(this.usersService.isPlatformAdmin(any())).thenReturn(true);
        when(this.toolsService.getSubsystemById(any())).thenReturn(subsystemDTO);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, false);
        verify(this.usersService, times(1)).isPlatformAdmin(any());
    }

    @Test
    public void deployOnProEnvironment() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        TOSubsystemDTO subsystemDTO = MocksAndUtils.createTOSubsystemDTO();

        prepareCommonMocks(plan, null);
        when(this.usersService.isPlatformAdmin(any())).thenReturn(true);
        when(this.toolsService.getSubsystemById(any())).thenReturn(subsystemDTO);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, false);
        verify(this.usersService, times(1)).isPlatformAdmin(any());

    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdmin() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.DEPLOY_PRO;

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlanned() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        plan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.SCHEDULE_PLANNING;

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verify(this.gcspService,times(1)).validateGcspForDeploy(plan);
        verify(this.deploymentPlanRepository,times(1)).save(plan);
        verify(this.gcspService,times(1)).getDeploymentScript(plan);
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    private DeploymentGcsp createGscp(DeploymentPriority deploymentPriority, String deploymentList)
    {
        var gscp = new DeploymentGcsp();
        gscp.setPriorityLevel(deploymentPriority);
        gscp.setDeploymentList(deploymentList);
        return gscp;
    }

    private void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfo(
            DeploymentPriority deploymentPriority, String deploymentList) throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        plan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        plan.setGcsp(createGscp(deploymentPriority, deploymentList));
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.SCHEDULE_PLANNING;

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verify(this.gcspService,times(1)).validateGcspForDeploy(plan);
        verify(this.deploymentPlanRepository,times(1)).save(plan);
        verify(this.gcspService,times(1)).getDeploymentScript(plan);
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfoAsService() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.SERVICE;
        String deploymentList = StringUtils.EMPTY;
        deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfo(deploymentPriority, deploymentList);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfoAsProduct() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.PRODUCT;
        String deploymentList = StringUtils.EMPTY;
        deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfo(deploymentPriority, deploymentList);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfoAsSubsystem() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.SUBSYSTEM;
        String deploymentList = StringUtils.EMPTY;
        deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfo(deploymentPriority, deploymentList);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfoAsServiceWithDeploymentList() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.SERVICE;

        var serviceIds = ThreadLocalRandom.current().ints( 10 ).boxed().collect(Collectors.toSet());
        String deploymentList = serviceIds.stream().map(Object::toString).collect(Collectors.joining(","));

        deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfo(deploymentPriority, deploymentList);

        serviceIds.forEach( id -> {
            verify(this.deploymentServiceRepository,times(1)).getServiceName(id);
        });
    }


    private Set<Integer> prepareExtraInfoAsProductWithDeploymentList()
    {
        var serviceIds = ThreadLocalRandom.current().ints( 10 ).boxed().collect(Collectors.toSet());

        serviceIds.forEach( id -> {
            var subsystemDTO = new TOSubsystemDTO();
            subsystemDTO.setSubsystemName("Name"+id);
            when(this.deploymentSubsystemRepository.getSubsystemId(id)).thenReturn(id);
            when(this.toolsService.getSubsystemById(id)).thenReturn(subsystemDTO);
        });
        return serviceIds;
    }

    private void verifyExtraInfoAsProductWithDeploymentList(Set<Integer> serviceIds)
    {
        serviceIds.forEach( id -> {
            verify(this.deploymentSubsystemRepository, times(1)).getSubsystemId(id);
            verify(this.toolsService, times(1)).getSubsystemById(id);
        });
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfoAsProductWithDeploymentList() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.PRODUCT;
        var serviceIds = prepareExtraInfoAsProductWithDeploymentList();

        String deploymentList = serviceIds.stream().map(Object::toString).collect(Collectors.joining(","));

        deployOnProEnvironmentNotPlatformAdminDeploymentTypePlannedWithExtraInfo(deploymentPriority, deploymentList);

        //Verify
        verifyExtraInfoAsProductWithDeploymentList(serviceIds);
    }

    private void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfo(
            DeploymentPriority deploymentPriority, String deploymentList) throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        plan.setDeploymentTypeInPro(DeploymentType.NOVA_PLANNED);
        plan.setNova(createNova(deploymentPriority, deploymentList));
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.NOVA_PLANNING;

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verify(this.novaPlannedService,times(1)).validateNovaPlannedForDeploy(plan);
        verify(this.novaPlannedService,times(1)).getDeploymentActions(plan);
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfoAsService() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.SERVICE;
        String deploymentList = StringUtils.EMPTY;
        deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfo(deploymentPriority, deploymentList);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfoAsProduct() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.PRODUCT;
        String deploymentList = StringUtils.EMPTY;
        deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfo(deploymentPriority, deploymentList);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfoAsSubsystem() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.SUBSYSTEM;
        String deploymentList = StringUtils.EMPTY;
        deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfo(deploymentPriority, deploymentList);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfoAsServiceWithDeploymentList() throws Exception
    {
        DeploymentPriority deploymentPriority = DeploymentPriority.SERVICE;
        int S1 = ThreadLocalRandom.current().nextInt();
        int S2 = ThreadLocalRandom.current().nextInt();
        int S3 = ThreadLocalRandom.current().nextInt();
        String deploymentList = S1+","+S2+","+S3;

        deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfo(deploymentPriority, deploymentList);

        verify(this.deploymentServiceRepository,times(1)).getServiceName(S1);
        verify(this.deploymentServiceRepository,times(1)).getServiceName(S2);
        verify(this.deploymentServiceRepository,times(1)).getServiceName(S3);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfoAsProductWithDeploymentList() throws Exception
    {

        DeploymentPriority deploymentPriority = DeploymentPriority.PRODUCT;
        var serviceIds = prepareExtraInfoAsProductWithDeploymentList();

        String deploymentList = serviceIds.stream().map(Object::toString).collect(Collectors.joining(","));

        deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlannedWithExtraInfo(deploymentPriority, deploymentList);

        //Verify
        verifyExtraInfoAsProductWithDeploymentList(serviceIds);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminDeploymentTypeNovaPlanned() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        plan.setDeploymentTypeInPro(DeploymentType.NOVA_PLANNED);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.NOVA_PLANNING;

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verify(this.novaPlannedService,times(1)).validateNovaPlannedForDeploy(plan);
        verify(this.novaPlannedService,times(1)).getDeploymentActions(plan);
        verifyTodoTaskMocks(plan, taskId, mockType);
    }











    @Test
    public void deployOnProEnvironmentNotPlatformAdminEnabledAutodeployInProDeploymentTypeNovaPlanned() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        plan.setDeploymentTypeInPro(DeploymentType.NOVA_PLANNED);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.NOVA_PLANNING;

        // Set the interval from now 1000 seconds for autodeployInPro
        plan.getReleaseVersion().getRelease().setAutodeployInPro(
                new TimeInterval(new Date(),
                        new Date( new Date().getTime() + 1_000_000 )));

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verify(this.novaPlannedService,times(1)).validateNovaPlannedForDeploy(plan);
        verify(this.novaPlannedService,times(1)).getDeploymentActions(plan);
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminEnabledAutodeployInProDeploymentTypePlanned() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        plan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.SCHEDULE_PLANNING;

        // Set the interval from now 1000 seconds for autodeployInPro
        plan.getReleaseVersion().getRelease().setAutodeployInPro(
                new TimeInterval(new Date(),
                        new Date( new Date().getTime() + 1_000_000 )));

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verify(this.gcspService,times(1)).validateGcspForDeploy(plan);
        verify(this.deploymentPlanRepository,times(1)).save(plan);
        verify(this.gcspService,times(1)).getDeploymentScript(plan);
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnProEnvironmentNotPlatformAdminEnabledAutodeployInProOnDemmand() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        //plan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        int taskId = ThreadLocalRandom.current().nextInt();
        ToDoTaskType mockType =  ToDoTaskType.DEPLOY_PRO;

        // Set the interval from now 1000 seconds for autodeployInPro
        plan.getReleaseVersion().getRelease().setAutodeployInPro(
                new TimeInterval(new Date(),
                        new Date( new Date().getTime() + 1_000_000 )));

        prepareCommonMocks(plan, null);
        prepareTodoTaskMocks(plan, taskId, mockType);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertEquals(taskId, todoTaskResponseDTO.getTodoTaskId());
        Assertions.assertTrue(todoTaskResponseDTO.getGenerated());
        verifyTodoTaskMocks(plan, taskId, mockType);
    }

    @Test
    public void deployOnIntEnvironmentWithDeployedPlan() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
        DeploymentPlan deployedPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

        prepareCommonMocks(plan, deployedPlan);

        //THEN
        this.deployerService.deployOnEnvironment("CODE", 1, true);

        verifyExecution(plan, true);
    }

    @Test
    public void deployOnPreEnvironmentWithDeployedPlan() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);
        DeploymentPlan deployedPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRE);

        prepareCommonMocks(plan, deployedPlan);
        when(this.usersService.hasPermission(any())).thenReturn(true);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, true);
        verify(this.usersService, times(1)).hasPermission(any());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any());
    }

    @Test
    public void deployOnProEnvironmentWithDeployedPlan() throws Exception
    {
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);
        DeploymentPlan deployedPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.PRO);

        prepareCommonMocks(plan, deployedPlan);
        when(this.usersService.isPlatformAdmin(any())).thenReturn(true);

        //THEN
        TodoTaskResponseDTO todoTaskResponseDTO = this.deployerService.deployOnEnvironment("CODE", 1, true);

        //VERIFY
        Assertions.assertFalse(todoTaskResponseDTO.getGenerated());
        verifyExecution(plan, true);
        verify(this.usersService, times(1)).isPlatformAdmin(any());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any());
    }

    @Test
    public void promotePlan()
    {
        DeploymentPlan plan1 = new DeploymentPlan();
        DeploymentPlan plan2 = new DeploymentPlan();
        this.deployerService.promotePlan(plan1, plan2);
        verify(this.deploymentmanagerService, times(1)).promotePlan(any(), any());
    }


    private DeploymentService createDeploymentService()
    {
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(1);
        DeploymentPlan plan = new DeploymentPlan();
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setSubsystem(releaseVersionSubsystem);
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setDeploymentSubsystem(subsystem);
        service.setId(1);

        ReleaseVersionService rvs = new ReleaseVersionService();
        rvs.setFinalName("FinalName");
        rvs.setServiceName("ServiceName");
        service.setService(rvs);

        return service;
    }

    @Test
    public void updatePlanStatusWithServiceError()
    {
        //PREPARE
        DeploymentService service = createDeploymentService();
        DeploymentSubsystem subsystem = service.getDeploymentSubsystem();
        DeploymentPlan plan = subsystem.getDeploymentPlan();

        service.setAction(DeploymentAction.ERROR);

        //EXECUTE
        this.deployerService.updatePlanStatus(service);

        //VERIFY
        assertEquals(DeploymentStatus.REJECTED, plan.getStatus());
        verify(deploymentSubsystemRepository, times(1)).saveAndFlush(subsystem);
        verify(deploymentPlanRepository, times(1)).saveAndFlush(plan);
    }

    @Test
    public void updatePlanStatusWithServiceReadyAndSubsystemError()
    {
        //PREPARE
        DeploymentService service = createDeploymentService();
        DeploymentSubsystem subsystem = service.getDeploymentSubsystem();
        DeploymentPlan plan = subsystem.getDeploymentPlan();

        service.setAction(DeploymentAction.READY);
        subsystem.setAction(DeploymentAction.ERROR);

        TOSubsystemDTO subsystemDTO = new TOSubsystemDTO();
        subsystemDTO.setSubsystemName("SubsystemName");
        when(this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId())).thenReturn(subsystemDTO);

        //EXECUTE
        this.deployerService.updatePlanStatus(subsystem);

        //VERIFY
        assertEquals(DeploymentStatus.REJECTED, plan.getStatus());
        verify(this.toolsService, times(1)).getSubsystemById(subsystem.getSubsystem().getSubsystemId());
        verify(deploymentPlanRepository, times(1)).saveAndFlush(plan);
    }

    @Test
    public void updatePlanStatusWithSubsystemReady()
    {
        //PREPARE
        DeploymentService service = createDeploymentService();
        DeploymentSubsystem subsystem = service.getDeploymentSubsystem();
        DeploymentPlan plan = subsystem.getDeploymentPlan();

        subsystem.setAction(DeploymentAction.READY);

        TOSubsystemDTO subsystemDTO = new TOSubsystemDTO();
        subsystemDTO.setSubsystemName("SubsystemName");
        when(this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId())).thenReturn(subsystemDTO);

        //EXECUTE
        this.deployerService.updatePlanStatus(service);

        //VERIFY
        assertEquals(DeploymentStatus.DEPLOYED, plan.getStatus());
        verify(this.toolsService, times(1)).getSubsystemById(subsystem.getSubsystem().getSubsystemId());
        verify(this.deploymentPlanRepository, times(1)).saveAndFlush(plan);
        verify(this.deploymentUtils, times(1)).
                addHistoryEntry(eq(ChangeType.DEPLOY_PLAN), any(), any(), anyString());
    }

    private DeploymentNova createNova(DeploymentPriority deploymentPriority, String deploymentList)
    {
        var nova = new DeploymentNova();
        nova.setDeploymentDateTime(new Date());
        nova.setPriorityLevel(deploymentPriority);
        nova.setDeploymentList(deploymentList);
        return nova;
    }

    @Test
    public void activateNovaPlannedDeployment()
    {
        //PREPARE
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
        plan.setNova(createNova(DeploymentPriority.PRODUCT, StringUtils.EMPTY));

        when(schedulerManagerClient.scheduleDeployment(plan)).thenReturn(true);

        //EXECUTE
        this.deployerService.activateNovaPlannedDeployment ("USER", plan);

        //VERIFY
        verify(this.deploymentPlanRepository, times(1)).saveAndFlush(plan);
        verify(this.schedulerManagerClient, times(1)).scheduleDeployment(plan);
        verify(this.deploymentUtils, times(1)).
                addHistoryEntry(eq(ChangeType.SCHEDULE_DEPLOY_PLAN), any(), any(), anyString());
    }

    @Test
    public void unscheduleDeployment()
    {
        String ivUser = "user";
        DeploymentPlan plan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

        //EXECUTE
        this.deployerService.unscheduleDeployment (ivUser, plan);

        //Verify
        verify(this.schedulerManagerClient, times(1)).unscheduleDeployment(plan.getId());
        verify(this.deploymentUtils, times(1)).
                addHistoryEntry(eq(ChangeType.UNSCHEDULE_DEPLOY_PLAN), eq(plan), eq(ivUser), anyString());
    }
}
