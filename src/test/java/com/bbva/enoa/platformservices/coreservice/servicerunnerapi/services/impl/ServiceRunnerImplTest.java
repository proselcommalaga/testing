package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.impl;

import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentInstanceType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.enums.BatchSchedulerInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ServiceExecutionHistoryRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.EtherManagerClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerUtils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceRunnerImplTest
{
    @Mock
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;
    @Mock
    private DeploymentChangeRepository deploymentChangeRepository;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private DeploymentInstanceRepository deploymentInstanceRepository;
    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;
    @Mock
    private DeploymentSubsystemRepository deploymentSubsystemRepository;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IProductUsersClient usersService;
    @Mock
    private IToolsClient toolsService;
    @Mock
    private ILibraryManagerService libraryManagerService;
    @Mock
    private ISchedulerManagerClient schedulerManagerClient;
    @Mock
    private DeploymentUtils deploymentUtils;
    @Mock
    private EtherManagerClientImpl etherManagerClient;
    @Mock
    private IDeploymentManagerService deploymentManagerService;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private ServiceRunnerUtils serviceRunnerUtils;
    @Mock
    private ServiceExecutionHistoryRepository serviceExecutionHistoryRepository;
    @InjectMocks
    private ServiceRunnerImpl serviceRunnerImpl;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    private ReleaseVersionService generateReleaseVersionService()
    {
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setFinalName("DefaultFinalName");
        releaseVersionService.setServiceName("DefaultServiceName");

        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setCompilationJobName("DefaultCompilationJobName");
        releaseVersionSubsystem.setTagName("DefaultTagName");

        releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);

        return releaseVersionService;
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
        releaseVersionSubsystem.setSubsystemId(1);
        releaseVersionSubsystem.setTagName("SUBSYSTEMTAGNAME");

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setId(2);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setId(2);
        releaseVersionService.setServiceName("SERVICENAME");
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

    private TOSubsystemDTO generateToSubsystemDTO()
    {
        TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
        toSubsystemDTO.fillRandomly(2, false, 0, 3);

        return toSubsystemDTO;
    }

    private TodoTaskResponseDTO generateToDoTaskDTO()
    {
        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();
        todoTaskResponseDTO.fillRandomly(2, false, 0, 3);

        return todoTaskResponseDTO;
    }

    private EtherDeploymentDTO generateEtherDeploymentDTO()
    {
        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();
        etherDeploymentDTO.fillRandomly(2, false, 0, 3);

        return etherDeploymentDTO;
    }

    private DeploymentBatchScheduleInstanceDTO generateDeploymentBatchSchedulerInstanceDTO()
    {
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = new DeploymentBatchScheduleInstanceDTO();
        batchScheduleInstanceDTO.fillRandomly(2, false, 0, 3);
        return batchScheduleInstanceDTO;
    }


    @Ignore
    @Test
        //TODO(JVS): Static method creating conflicts in casting
    void startInstanceNova()
    {
        /*DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);

        NovaDeploymentInstance novaDeploymentInstance = new NovaDeploymentInstance();
        novaDeploymentInstance.setId(1);
        novaDeploymentInstance.setContainerName("CONTAINERNAMENOVA");

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentInstance.getService(), deploymentInstance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment().getEnvironment());

        doNothing().when(this.deploymentManagerService).startInstance(deploymentInstance);

        this.serviceRunnerImpl.startInstance("CODE", deploymentInstance);

        verify(this.deploymentManagerService).startInstance(deploymentInstance);*/
    }

    @Ignore
    @Test
    void stopInstance()
    {
    }

    @Ignore
    @Test
    void restartInstance()
    {

    }

    @Test
    void startInstanceEtherTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        // Forcing ether deployment
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        EtherDeploymentDTO etherDeploymentDTO = this.generateEtherDeploymentDTO();

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentInstance.getService(), deploymentInstance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        when(this.deploymentUtils.buildEtherDeploymentDTO(deploymentInstance.getService(), CallbackService.START, CallbackService.START_ERROR)).thenReturn(etherDeploymentDTO);
        doNothing().when(this.etherManagerClient).startEtherService(etherDeploymentDTO);

        this.serviceRunnerImpl.startInstance("CODE", deploymentInstance);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentService(deploymentInstance.getService(), deploymentInstance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        verify(this.deploymentUtils, times(1)).buildEtherDeploymentDTO(deploymentInstance.getService(), CallbackService.START, CallbackService.START_ERROR);
        verify(this.etherManagerClient, times(1)).startEtherService(etherDeploymentDTO);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void stopInstanceEtherTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        // Forcing ether deployment
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        EtherDeploymentDTO etherDeploymentDTO = this.generateEtherDeploymentDTO();

        when(this.deploymentUtils.buildEtherDeploymentDTO(deploymentInstance.getService(), CallbackService.STOP, CallbackService.STOP_ERROR)).thenReturn(etherDeploymentDTO);

        this.serviceRunnerImpl.stopInstance("CODE", deploymentInstance);

        verify(this.deploymentUtils, times(1)).buildEtherDeploymentDTO(deploymentInstance.getService(), CallbackService.STOP, CallbackService.STOP_ERROR);
        verify(this.etherManagerClient, times(1)).stopEtherService(etherDeploymentDTO);
    }

    @Test
    void restartInstanceEtherTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        // Forcing ether deployment
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        EtherDeploymentDTO etherDeploymentDTO = this.generateEtherDeploymentDTO();

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentInstance.getService(), deploymentInstance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        when(this.deploymentUtils.buildEtherDeploymentDTO(deploymentInstance.getService(), CallbackService.RESTART, CallbackService.RESTART_ERROR)).thenReturn(etherDeploymentDTO);

        this.serviceRunnerImpl.restartInstance("CODE", deploymentInstance);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentService(deploymentInstance.getService(), deploymentInstance.getService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        verify(this.deploymentUtils, times(1)).buildEtherDeploymentDTO(deploymentInstance.getService(), CallbackService.RESTART, CallbackService.RESTART_ERROR);
        verify(this.etherManagerClient, times(1)).restartEtherService(etherDeploymentDTO);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void startServiceWithApiRestTest()
    {
        startServiceTest(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void startServiceWithApiTest()
    {
        startServiceTest(ServiceType.API_JAVA_SPRING_BOOT);
    }
    private void startServiceTest(ServiceType serviceType)
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(serviceType.getServiceType());

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        doNothing().when(this.deploymentManagerService).startService(deploymentService);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startService("CODE", deploymentService);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        verify(this.deploymentManagerService, times(1)).startService(deploymentService);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.serviceExecutionHistoryRepository, times(1)).save(any(ServiceExecutionHistory.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void when_start_service_has_service_batch_type_then_dont_save_service_execution_history()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(ServiceType.BATCH_JAVA_SPRING_BATCH.getServiceType());

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        doNothing().when(this.deploymentManagerService).startService(deploymentService);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startService("CODE", deploymentService);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        verify(this.deploymentManagerService, times(1)).startService(deploymentService);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verifyNoInteractions(this.serviceExecutionHistoryRepository);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void startServiceBatchScheduledTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        doNothing().when(this.schedulerManagerClient).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.ENABLED);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startService("CODE", deploymentService);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        verify(this.schedulerManagerClient, times(1)).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.ENABLED);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void stopServiceWithApiRestTest()
    {
        stopServiceTest(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void stopServiceWithApiTest()
    {
        stopServiceTest(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void stopServiceTest(ServiceType serviceType)
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(serviceType.getServiceType());

        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.stopService("CODE", deploymentService);

        verify(this.deploymentManagerService, times(1)).stopService(deploymentService);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void stopServiceBatchScheduledTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());

        doNothing().when(this.schedulerManagerClient).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.DISABLED);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.stopService("CODE", deploymentService);

        verify(this.schedulerManagerClient, times(1)).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.DISABLED);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void restartServiceWithApiRestTest()
    {
        restartServiceTest(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void restartServiceWithApiTest()
    {
        restartServiceTest(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void restartServiceTest(ServiceType serviceType)
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(serviceType.getServiceType());

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.restartService("CODE", deploymentService);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentService(deploymentService, deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        verify(this.deploymentManagerService, times(1)).restartService(deploymentService);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.serviceExecutionHistoryRepository, times(1)).save(any(ServiceExecutionHistory.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void startSubsystemTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getDeploymentSubsystems().get(0);
        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();


        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentSubsystem(deploymentSubsystem, deploymentSubsystem.getDeploymentPlan().getEnvironment());
        doNothing().when(this.deploymentManagerService).startSubsystem(deploymentSubsystem);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());
        when(this.toolsService.getSubsystemById(deploymentSubsystem.getSubsystem().getId())).thenReturn(toSubsystemDTO);


        this.serviceRunnerImpl.startSubsystem("CODE", deploymentSubsystem);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentSubsystem(deploymentSubsystem, deploymentSubsystem.getDeploymentPlan().getEnvironment());
        verify(this.deploymentManagerService, times(1)).startSubsystem(deploymentSubsystem);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.toolsService, times(1)).getSubsystemById(deploymentSubsystem.getSubsystem().getId());
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.serviceExecutionHistoryRepository, times(1)).saveAll(anyList());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void when_start_subsystem_has_service_with_batch_type_then_dont_save_service_execution_history()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService().setServiceType(ServiceType.BATCH_JAVA_SPRING_BATCH.getServiceType());
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getDeploymentSubsystems().get(0);
        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();


        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentSubsystem(deploymentSubsystem, deploymentSubsystem.getDeploymentPlan().getEnvironment());
        doNothing().when(this.deploymentManagerService).startSubsystem(deploymentSubsystem);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());
        when(this.toolsService.getSubsystemById(deploymentSubsystem.getSubsystem().getId())).thenReturn(toSubsystemDTO);


        this.serviceRunnerImpl.startSubsystem("CODE", deploymentSubsystem);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentSubsystem(deploymentSubsystem, deploymentSubsystem.getDeploymentPlan().getEnvironment());
        verify(this.deploymentManagerService, times(1)).startSubsystem(deploymentSubsystem);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.toolsService, times(1)).getSubsystemById(deploymentSubsystem.getSubsystem().getId());
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verifyNoInteractions(this.serviceExecutionHistoryRepository);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void stopSubsystemTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getDeploymentSubsystems().get(0);
        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();

        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());
        when(this.toolsService.getSubsystemById(deploymentSubsystem.getSubsystem().getId())).thenReturn(toSubsystemDTO);


        this.serviceRunnerImpl.stopSubsystem("CODE", deploymentSubsystem);

        verify(this.deploymentManagerService, times(1)).stopSubsystem(deploymentSubsystem);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.toolsService, times(1)).getSubsystemById(deploymentSubsystem.getSubsystem().getId());
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void restartSubsystemTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getDeploymentSubsystems().get(0);
        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();


        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentSubsystem(deploymentSubsystem, deploymentSubsystem.getDeploymentPlan().getEnvironment());
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());
        when(this.toolsService.getSubsystemById(deploymentSubsystem.getSubsystem().getId())).thenReturn(toSubsystemDTO);


        this.serviceRunnerImpl.restartSubsystem("CODE", deploymentSubsystem);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentSubsystem(deploymentSubsystem, deploymentSubsystem.getDeploymentPlan().getEnvironment());
        verify(this.deploymentManagerService, times(1)).restartSubsystem(deploymentSubsystem);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.toolsService, times(1)).getSubsystemById(deploymentSubsystem.getSubsystem().getId());
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.serviceExecutionHistoryRepository, times(1)).saveAll(anyList());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void startDeploymentPlanTest()
    {

        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());
        doNothing().when(this.deploymentManagerService).startPlan(deploymentPlan);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startDeploymentPlan("CODE", deploymentPlan);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());
        verify(this.deploymentManagerService, times(1)).startPlan(deploymentPlan);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.serviceExecutionHistoryRepository, times(1)).saveAll(anyList());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void when_start_deployment_plan_has_service_with_batch_type_then_dont_save_service_execution_history()
    {

        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService().setServiceType(ServiceType.BATCH_JAVA_SPRING_BATCH.getServiceType());

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());
        doNothing().when(this.deploymentManagerService).startPlan(deploymentPlan);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startDeploymentPlan("CODE", deploymentPlan);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());
        verify(this.deploymentManagerService, times(1)).startPlan(deploymentPlan);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verifyNoInteractions(this.serviceExecutionHistoryRepository);
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void stopDeploymentPlanTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.stopDeploymentPlan("CODE", deploymentPlan);

        verify(this.deploymentManagerService, times(1)).stopPlan(deploymentPlan);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void restartDeploymentPlanTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();

        doNothing().when(this.libraryManagerService).checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.restartDeploymentPlan("CODE", deploymentPlan);

        verify(this.libraryManagerService, times(1)).checkPublishedLibrariesByDeploymentPlan(deploymentPlan, deploymentPlan.getEnvironment());
        verify(this.deploymentManagerService, times(1)).restartPlan(deploymentPlan);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.serviceExecutionHistoryRepository, times(1)).saveAll(anyList());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void startBatchScheduleTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());

        doNothing().when(this.schedulerManagerClient).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.ENABLED);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startBatchSchedule("CODE", deploymentService);

        verify(this.schedulerManagerClient, times(1)).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.ENABLED);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void stopBatchScheduleTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        // Forcing service type
        deploymentService.getService().setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());

        doNothing().when(this.schedulerManagerClient).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.DISABLED);
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.stopBatchSchedule("CODE", deploymentService);

        verify(this.schedulerManagerClient, times(1)).updateDeploymentBatchSchedule(deploymentService.getService().getId(), deploymentPlan.getId(), DeploymentBatchScheduleStatus.DISABLED);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }


    @Test
    void stopBatchScheduleInstanceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());

        when(this.schedulerManagerClient.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.STOP_INSTANCE)).thenReturn(batchScheduleInstanceDTO);
        when(this.deploymentPlanRepository.findById(batchScheduleInstanceDTO.getDeploymentPlanId())).thenReturn(Optional.of(deploymentPlan));
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.stopBatchScheduleInstance("CODE", 1);

        verify(this.schedulerManagerClient, times(1)).stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.STOP_INSTANCE);
        verify(this.deploymentPlanRepository, times(1)).findById(1);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));

        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));

    }

    @Test
    void stopBatchScheduleInstanceNoPlanTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());

        when(this.schedulerManagerClient.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.STOP_INSTANCE)).thenReturn(batchScheduleInstanceDTO);
        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.stopBatchScheduleInstance("CODE", 1));

    }


    @Test
    void resumeBatchScheduleInstanceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());

        when(this.schedulerManagerClient.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.RESUME_INSTANCE)).thenReturn(batchScheduleInstanceDTO);
        when(this.deploymentPlanRepository.findById(batchScheduleInstanceDTO.getDeploymentPlanId())).thenReturn(Optional.of(deploymentPlan));
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.resumeBatchScheduleInstance("CODE", 1);

        verify(this.schedulerManagerClient, times(1)).stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.RESUME_INSTANCE);
        verify(this.deploymentPlanRepository, times(1)).findById(1);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));

        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void resumeBatchScheduleInstanceNoPlanTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());

        when(this.schedulerManagerClient.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.RESUME_INSTANCE)).thenReturn(batchScheduleInstanceDTO);
        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.resumeBatchScheduleInstance("CODE", 1));
    }


    @Test
    void pauseBatchScheduleInstanceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ReleaseVersionService releaseVersionService = this.generateReleaseVersionService();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());

        when(this.schedulerManagerClient.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.PAUSE_INSTANCE)).thenReturn(batchScheduleInstanceDTO);
        when(this.deploymentPlanRepository.findById(batchScheduleInstanceDTO.getDeploymentPlanId())).thenReturn(Optional.of(deploymentPlan));
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());
        when(this.releaseVersionServiceRepository.findById(batchScheduleInstanceDTO.getReleaseVersionServiceId())).thenReturn(Optional.of(releaseVersionService));

        this.serviceRunnerImpl.pauseBatchScheduleInstance("CODE", 1);

        verify(this.schedulerManagerClient, times(1)).stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.PAUSE_INSTANCE);
        verify(this.deploymentPlanRepository, times(1)).findById(1);
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));

        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void pauseBatchScheduleInstanceNoPlanTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());

        when(this.schedulerManagerClient.stateBatchScheduleInstance(1, BatchSchedulerInstanceStatus.PAUSE_INSTANCE)).thenReturn(batchScheduleInstanceDTO);
        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.pauseBatchScheduleInstance("CODE", 1));
    }

    @Test
    void startBatchScheduleInstanceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentBatchScheduleInstanceDTO batchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();
        batchScheduleInstanceDTO.setDeploymentPlanId(deploymentPlan.getId());
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

        doNothing().when(this.schedulerManagerClient).startScheduleInstance(deploymentService.getService().getId(), deploymentPlan.getId());
        when(this.deploymentChangeRepository.saveAndFlush(any(DeploymentChange.class))).thenReturn(new DeploymentChange());

        this.serviceRunnerImpl.startBatchScheduleInstance("CODE", deploymentService, deploymentPlan);

        verify(this.schedulerManagerClient, times(1)).startScheduleInstance(deploymentService.getService().getId(), deploymentPlan.getId());
        verify(this.deploymentChangeRepository, times(1)).saveAndFlush(any(DeploymentChange.class));
        verify(this.usersService, times(1)).checkHasPermission(anyString(), anyString(), anyString(), anyInt(), any(NovaException.class));
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }


    @Test
    void processTaskTest()
    {

        List<ToDoTaskType> toDoTaskTypes = this.generateListToDoTaskType();

        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();

        DeploymentBatchScheduleInstanceDTO deploymentBatchScheduleInstanceDTO = this.generateDeploymentBatchSchedulerInstanceDTO();

        when(this.deploymentInstanceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0)));
        when(this.deploymentServiceRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0)));
        when(this.deploymentSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan.getDeploymentSubsystems().get(0)));
        when(this.deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
        when(this.schedulerManagerClient.stateBatchScheduleInstance(anyInt(), any(BatchSchedulerInstanceStatus.class))).thenReturn(deploymentBatchScheduleInstanceDTO);

        when(this.toolsService.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);


        ManagementActionTask managementActionTask = new ManagementActionTask();
        managementActionTask.setId(1);
        managementActionTask.setAssignedUserCode("CODE");

        for (ToDoTaskType toDoTaskType : toDoTaskTypes)
        {
            managementActionTask.setTaskType(toDoTaskType);
            this.serviceRunnerImpl.processTask(managementActionTask);
        }

        verify(this.deploymentInstanceRepository, times(3)).findById(anyInt());
        verify(this.deploymentServiceRepository, times(6)).findById(anyInt());
        verify(this.deploymentSubsystemRepository, times(3)).findById(anyInt());
        verify(this.deploymentPlanRepository, times(6)).findById(anyInt());
        verify(this.schedulerManagerClient, times(3)).stateBatchScheduleInstance(anyInt(), any(BatchSchedulerInstanceStatus.class));

    }

    @Test
    void createProductionTaskInstanceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.BATCH_SCHEDULE_START;


        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createProductionTask("CODE", deploymentInstance, toDoTaskType);

        Assertions.assertNotNull(todoTaskResponseDTO);
    }

    @Test
    void createProductionTaskInstanceErrorTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.CONTAINER_START;

        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenThrow(new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError()));

        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.createProductionTask("CODE", deploymentInstance, toDoTaskType));
    }


    @Test
    void createProductionTaskServiceTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.SERVICE_START;


        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createProductionTask("CODE", deploymentService, toDoTaskType);

        Assertions.assertNotNull(todoTaskResponseDTO);
    }

    @Test
    void createProductionTaskServiceErrorTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.SERVICE_START;

        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenThrow(new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError()));

        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.createProductionTask("CODE", deploymentService, toDoTaskType));
    }


    @Test
    void createProductionTaskSubsystemTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getDeploymentSubsystems().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.RELEASE_START;
        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();


        when(this.toolsService.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);
        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createProductionTask("CODE", deploymentSubsystem, toDoTaskType);

        Assertions.assertNotNull(todoTaskResponseDTO);
    }

    @Test
    void createProductionTaskSubsystemErrorTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = deploymentPlan.getDeploymentSubsystems().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.RELEASE_START;
        TOSubsystemDTO toSubsystemDTO = this.generateToSubsystemDTO();


        when(this.toolsService.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);
        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenThrow(new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError()));

        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.createProductionTask("CODE", deploymentSubsystem, toDoTaskType));
    }


    @Test
    void createProductionTaskPlanTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ToDoTaskType toDoTaskType = ToDoTaskType.RELEASE_START;


        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createProductionTask("CODE", deploymentPlan, toDoTaskType);

        Assertions.assertNotNull(todoTaskResponseDTO);
    }

    @Test
    void createProductionTaskPlanErrorTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.RELEASE_START;

        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenThrow(new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError()));

        Assertions.assertThrows(NovaException.class, () -> this.serviceRunnerImpl.createProductionTask("CODE", deploymentPlan, toDoTaskType));
    }


    @Test
    void createBatchScheduleServiceProductionTaskTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ToDoTaskType toDoTaskType = ToDoTaskType.RELEASE_START;


        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createBatchScheduleServiceProductionTask("CODE", 3, deploymentPlan, toDoTaskType);

        Assertions.assertNotNull(todoTaskResponseDTO);
        Assertions.assertEquals(true, todoTaskResponseDTO.getGenerated());
    }

    @Test
    void createBatchScheduleServiceProductionTaskErrorTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        DeploymentInstance deploymentInstance = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getInstances().get(0);
        ToDoTaskType toDoTaskType = ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME;

        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(1);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), anyInt(), any(ToDoTaskType.class))).thenThrow(new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError()));
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean())).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createBatchScheduleServiceProductionTask("CODE", 3, deploymentPlan, toDoTaskType);

        Assertions.assertEquals(false, todoTaskResponseDTO.getGenerated());


    }

    @Test
    void createBatchScheduleServiceProductionTaskErrorNullTaskIdTest()
    {
        DeploymentPlan deploymentPlan = this.generateDeploymentPlan();
        ToDoTaskType toDoTaskType = ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME;


        when(this.todoTaskServiceClient.createManagementTask(anyString(), any(), anyString(), anyString(), anyString(), any(Product.class), anyInt())).thenReturn(null);
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean(), any(RoleType.class), any(), any(ToDoTaskType.class))).thenCallRealMethod();
        when(this.serviceRunnerUtils.builderTodoTaskResponseDTO(anyBoolean())).thenCallRealMethod();

        TodoTaskResponseDTO todoTaskResponseDTO = this.serviceRunnerImpl.createBatchScheduleServiceProductionTask("CODE", 3, deploymentPlan, toDoTaskType);

        Assertions.assertEquals(false, todoTaskResponseDTO.getGenerated());


    }

    private List<ToDoTaskType> generateListToDoTaskType()
    {

        List<ToDoTaskType> toDoTaskTypes = new ArrayList<>();
        toDoTaskTypes.addAll(Arrays.asList(
                ToDoTaskType.CONTAINER_START,
                ToDoTaskType.CONTAINER_RESTART,
                ToDoTaskType.CONTAINER_STOP,
                ToDoTaskType.SERVICE_START,
                ToDoTaskType.SERVICE_RESTART,
                ToDoTaskType.SERVICE_STOP,
                ToDoTaskType.SUBSYSTEM_START,
                ToDoTaskType.SUBSYSTEM_RESTART,
                ToDoTaskType.SUBSYSTEM_STOP,
                ToDoTaskType.RELEASE_START,
                ToDoTaskType.RELEASE_RESTART,
                ToDoTaskType.RELEASE_STOP,
                ToDoTaskType.BATCH_SCHEDULE_START,
                ToDoTaskType.BATCH_SCHEDULE_STOP,
                ToDoTaskType.BATCH_SCHEDULE_INSTANCE_START,
                ToDoTaskType.BATCH_SCHEDULE_INSTANCE_STOP,
                ToDoTaskType.BATCH_SCHEDULE_INSTANCE_PAUSE,
                ToDoTaskType.BATCH_SCHEDULE_INSTANCE_RESUME));

        return toDoTaskTypes;
    }
}