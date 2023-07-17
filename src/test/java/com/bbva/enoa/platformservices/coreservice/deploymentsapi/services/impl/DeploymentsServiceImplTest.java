package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSubsystemDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentTypeChangeDto;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.datamodel.model.release.entities.JmxParameter;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.todotask.entities.ConfigurationTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.MailServiceConstants;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IQualityAssuranceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentMigrator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentServiceJdkParametersSetter;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRemoverService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeploymentsServiceImplTest
{
    @Mock
    private DeploymentGcspRepo gcspRepo;
    @Mock
    private ConfigurationRevisionRepository confRevisionRepo;
    @Mock
    private DeployerServiceImpl deployerService;
    @Mock
    private IRemoverService IRemoverService;
    @Mock
    private HardwarePackRepository hardwarePackRepository;
    @Mock
    private FilesystemRepository filesystemsApiRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private DeploymentPlanClonerImpl deploymentPlanCloner;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IAlertServiceApiClient alertServiceApiClient;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private DeploymentPlanCopierImpl deploymentPlanCopier;
    @Mock
    private DeploymentsValidatorImpl deploymentsValidator;
    @Mock
    private ReleaseVersionRepository versionRepository;
    @Mock
    private IDeploymentMigrator migrator;
    @Mock
    private RepositoryManagerServiceImpl repositoryManagerService;
    @Mock
    private DeploymentTaskRepository deploymentTaskRepository;
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @Mock
    private PropertyDefinitionRepository propertyDefinitionRepository;
    @Mock
    private BrokerPropertyRepository brokerPropertyRepository;
    @Mock
    private DeploymentBrokerImpl deploymentBroker;
    @Mock
    private ConfigurationValueRepository configurationValueRepository;
    @Mock
    private ManagementActionTaskRepository managementActionTaskRepository;
    @Mock
    private IApiGatewayService iApiGatewayService;
    @Mock
    private ISchedulerManagerClient schedulerManagerClient;
    @Mock
    private ProfilingUtils profilingUtils;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private DeploymentUtils deploymentUtils;
    @Mock
    private IDeploymentServiceJdkParametersSetter jdkParametersSetter;
    @Mock
    private DeploymentServiceAllowedJdkParameterValueRepository jvmParamValuesRepository;
    @Mock
    private DeploymentNovaRepo novaRepo;
    @Mock
    private MailServiceClient mailServiceClient;
    @Mock
    private IUsersClient usersClient;
    @Mock
    private IQualityAssuranceClient qualityAssuranceClient;
    @Mock
    private ConfigurationTaskRepository configurationTaskRepository;
    @Mock
    private JmxParameterRepository jmxParameterRepository;
    @InjectMocks
    private DeploymentsServiceImpl deploymentsService;

    @BeforeEach
    void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(DeploymentsServiceImpl.class);
        ReflectionTestUtils.setField(this.deploymentsService, "maxdeployments", 10);
    }

    @Test
    void when_create_deployment_does_not_find_release_version_then_throw_exception() throws Exception
    {
        when(versionRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NovaException.class, () -> this.deploymentsService.createDeployment(1, "INT", false));

        verify(deploymentPlanRepository, times(0)).getByProductIdAndEnvironmentAndStatusNot(anyInt(), anyString(), any(DeploymentStatus.class));
    }

    @Test
    void when_create_deployment_creates_more_deployments_than_allowed_then_throw_exception() throws Exception
    {
        ReflectionTestUtils.setField(deploymentsService, "maxdeployments", 1);
        when(versionRepository.findById(anyInt())).thenReturn(Optional.of(getDummyReleaseVersion()));
        DeploymentPlan dummyPlan = getDummyDeploymentPlan();
        when(deploymentPlanRepository.getByProductIdAndEnvironmentAndStatusNot(anyInt(), anyString(), any(DeploymentStatus.class))).thenReturn(List.of(dummyPlan, dummyPlan));

        assertThrows(NovaException.class, () -> this.deploymentsService.createDeployment(1, "INT", false));

        verifyNoInteractions(deploymentsValidator);
    }

    @Test
    void when_create_deployment_has_been_saved_then_return_result() throws Exception
    {
        when(this.versionRepository.findById(anyInt())).thenReturn(Optional.of(getDummyReleaseVersion()));

        DeploymentPlan result = this.deploymentsService.createDeployment(1, "INT", false);

        assertEquals(Environment.INT.getEnvironment(), result.getEnvironment());
    }

    @Test
    void when_update_plan_from_dto_has_no_subsystems_in_dto_then_do_nothing()
    {
        DeploymentDto deploymentDto = getDummyDeploymentDto();
        deploymentDto.setSubsystems(null);
        this.deploymentsService.updatePlanFromDto(deploymentDto, "INT");

        verifyNoInteractions(entityManager);
    }

    @Test
    void when_update_plan_from_dto_has_no_services_in_dto_then_do_nothing()
    {
        DeploymentDto deploymentDto = getDummyDeploymentDto();
        DeploymentSubsystemDto subsystem = deploymentDto.getSubsystems()[0];
        subsystem.setServices(null);
        this.deploymentsService.updatePlanFromDto(deploymentDto, "INT");

        verifyNoInteractions(entityManager);
    }

    @Test
    void when_update_plan_from_dto_points_to_non_existing_deployment_service_then_throw_exception()
    {
        when(entityManager.find(any(), anyInt())).thenReturn(null);

        assertThrows(NovaException.class, () -> deploymentsService.updatePlanFromDto(getDummyDeploymentDto(), "INT"));
        verifyNoInteractions(hardwarePackRepository);
    }

    @Test
    void when_update_plan_from_dto_gets_deployment_service_without_hardware_pack_and_with_hardware_pack_code_then_check_alert_emission()
    {
        DeploymentDto deploymentDto = getDummyDeploymentDto();
        deploymentDto.getSubsystems()[0].getServices()[0].setHardwarePackCode("CODE");
        DeploymentService deploymentService = getDummyDeploymentService();
        DeploymentPlan deploymentPlan = getDummyDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = getDummyDeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
        when(entityManager.find(any(), anyInt())).thenReturn(deploymentService);

        deploymentsService.updatePlanFromDto(deploymentDto, "INT");
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_update_plan_from_dto_gets_deployment_service_without_hardware_pack_and_without_hardware_pack_code_then_ignore_alert_emission()
    {
        DeploymentService deploymentService = getDummyDeploymentService();
        DeploymentPlan deploymentPlan = getDummyDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = getDummyDeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
        when(entityManager.find(any(), anyInt())).thenReturn(deploymentService);

        deploymentsService.updatePlanFromDto(getDummyDeploymentDto(), "INT");
        verifyNoInteractions(novaActivityEmitter);
    }

    @Test
    void when_update_plan_from_dto_gets_deployment_service_with_same_hardware_pack_as_dto_then_ignore_alert_emission()
    {
        HardwarePack hardwarePack = getDummyHardwarePack();
        DeploymentDto deploymentDto = getDummyDeploymentDto();
        deploymentDto.getSubsystems()[0].getServices()[0].setHardwarePackCode(hardwarePack.getCode());
        DeploymentService deploymentService = getDummyDeploymentService();
        deploymentService.setHardwarePack(hardwarePack);
        DeploymentPlan deploymentPlan = getDummyDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = getDummyDeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
        when(entityManager.find(any(), anyInt())).thenReturn(deploymentService);

        deploymentsService.updatePlanFromDto(deploymentDto, "INT");
        verifyNoInteractions(novaActivityEmitter);
    }

    @Test
    void when_update_plan_from_dto_gets_deployment_service_with_distinct_hardware_pack_as_dto_then_check_alert_emission()
    {
        HardwarePack hardwarePack = getDummyHardwarePack();
        DeploymentDto deploymentDto = getDummyDeploymentDto();
        deploymentDto.getSubsystems()[0].getServices()[0].setHardwarePackCode("DUMMY");
        DeploymentService deploymentService = getDummyDeploymentService();
        deploymentService.setHardwarePack(hardwarePack);
        DeploymentPlan deploymentPlan = getDummyDeploymentPlan();
        DeploymentSubsystem deploymentSubsystem = getDummyDeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
        when(entityManager.find(any(), anyInt())).thenReturn(deploymentService);

        deploymentsService.updatePlanFromDto(deploymentDto, "INT");
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void deploy() throws Exception
    {
        this.deploymentsService.deploy("CODE", 1, true);

        verify(this.deployerService, times(1)).deployOnEnvironment("CODE", 1, true);
    }

    @Test
    void remove() throws Exception
    {
        this.deploymentsService.undeployPlan("CODE", 1);
        verify(this.IRemoverService, times(1)).undeployPlanOnEnvironment("CODE", 1);
    }

    @Test
    void when_promote_plan_to_environment_tries_to_promote_from_int_to_non_pre_environment_then_throw_exception()
    {
        assertThrows(NovaException.class, () -> this.deploymentsService.promotePlanToEnvironment(getDummyDeploymentPlan(), Environment.INT));
    }

    @Test
    void when_promote_plan_to_environment_tries_to_promote_from_pre_to_non_pro_environment_then_throw_exception()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setEnvironment(Environment.PRE.getEnvironment());
        assertThrows(NovaException.class, () -> this.deploymentsService.promotePlanToEnvironment(plan, Environment.INT));
    }

    @Test
    void when_promote_plan_to_environment_tries_to_promote_neither_deployed_nor_undeployed_plans_then_throw_exception()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        assertThrows(NovaException.class, () -> this.deploymentsService.promotePlanToEnvironment(plan, Environment.PRE));
    }

    @Test
    void when_promote_plan_to_environment_tries_to_promote_invalid_multi_cpd_plan_then_throw_exception()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setMultiCPDInPro(Boolean.TRUE);
        doThrow(new NovaException(DeploymentError.getInvalidInstancesNumberError(List.of()))).when(deploymentsValidator).validateInstancesNumberForMultiCPD(any(DeploymentPlan.class));

        assertThrows(NovaException.class, () -> this.deploymentsService.promotePlanToEnvironment(getDummyDeploymentPlan(), Environment.PRE));
        verify(deploymentPlanCloner, times(0)).clonePlanToEnvironment(any(DeploymentPlan.class), any(Environment.class));
        verify(repositoryManagerService, times(0)).savePlan(any(DeploymentPlan.class));
        verifyNoInteractions(repositoryManagerService);
    }

    @Test
    void when_promote_plan_to_environment_tries_to_promote_valid_plan_then_do_all_logic()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setMultiCPDInPro(Boolean.TRUE);
        when(deploymentPlanCloner.clonePlanToEnvironment(any(DeploymentPlan.class), any(Environment.class))).thenReturn(plan);

        this.deploymentsService.promotePlanToEnvironment(getDummyDeploymentPlan(), Environment.PRE);

        verify(repositoryManagerService, times(1)).savePlan(any(DeploymentPlan.class));
        verify(deployerService, times(1)).promotePlan(any(DeploymentPlan.class), any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_copy_plan_copies_has_stored_release_version_then_throw_exception()
    {
        doThrow(new NovaException(DeploymentError.getStoredReleaseVersionError(), "")).when(deploymentsValidator).checkReleaseVersionStored(any(DeploymentPlan.class));

        assertThrows(NovaException.class, () -> this.deploymentsService.copyPlan(getDummyDeploymentPlan()));

        verifyNoInteractions(deploymentPlanCopier);
    }

    @Test
    void when_copy_plan_copies_non_production_environment_plan_then_return_plan_without_gcsp_nor_nova_deployment()
    {
        when(this.deploymentPlanCopier.copyPlan(any(DeploymentPlan.class))).thenReturn(new DeploymentPlan());

        DeploymentPlan result = this.deploymentsService.copyPlan(getDummyDeploymentPlan());

        assertNull(result.getGcsp());
        assertNull(result.getNova());
    }

    @Test
    void when_copy_plan_copies_planned_production_environment_plan_then_return_plan_with_gcsp()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setEnvironment(Environment.PRO.getEnvironment());
        DeploymentPlan copiedPlan = new DeploymentPlan();
        copiedPlan.setDeploymentTypeInPro(DeploymentType.PLANNED);
        when(this.deploymentPlanCopier.copyPlan(any(DeploymentPlan.class))).thenReturn(copiedPlan);

        DeploymentPlan result = this.deploymentsService.copyPlan(plan);

        assertNotNull(result.getGcsp());
    }

    @Test
    void when_copy_plan_copies_nova_planned_production_environment_plan_then_return_plan_with_nova_deployment()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setEnvironment(Environment.PRO.getEnvironment());
        DeploymentPlan copiedPlan = new DeploymentPlan();
        copiedPlan.setDeploymentTypeInPro(DeploymentType.NOVA_PLANNED);
        when(this.deploymentPlanCopier.copyPlan(any(DeploymentPlan.class))).thenReturn(copiedPlan);

        DeploymentPlan result = this.deploymentsService.copyPlan(plan);

        assertNotNull(result.getNova());
    }

    @Test
    void when_archive_plan_points_to_non_existing_deployment_plan_then_throw_exception()
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NovaException.class, () -> deploymentsService.archivePlan(1));
        verifyNoInteractions(deploymentsValidator);
    }

    @Test
    void when_archive_plan_points_to_existing_deployment_plan_then_do_all_logic()
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(getDummyDeploymentPlan()));

        deploymentsService.archivePlan(1);

        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_delete_plan_points_to_non_existing_deployment_plan_then_throw_exception()
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NovaException.class, () -> deploymentsService.deletePlan(1));
        verify(deploymentPlanRepository, times(0)).findByParent(any(DeploymentPlan.class));
    }

    @Test
    void when_delete_plan_points_to_deployment_plan_with_invalid_status_then_throw_exception()
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(getDummyDeploymentPlan()));

        assertThrows(NovaException.class, () -> deploymentsService.deletePlan(1));
        verify(deploymentPlanRepository, times(0)).findByParent(any(DeploymentPlan.class));
    }

    @Test
    void when_delete_plan_points_to_deployment_plan_with_children_plans_in_process_then_throw_exception()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(plan));
        when(deploymentPlanRepository.findByParent(any(DeploymentPlan.class))).thenReturn(List.of(plan, plan));

        assertThrows(NovaException.class, () -> deploymentsService.deletePlan(1));
        verify(deploymentPlanRepository, times(1)).findByParent(any(DeploymentPlan.class));
    }

    @Test
    void when_delete_plan_has_valid_plan_for_deletion_then_delete()
    {
        DeploymentPlan plan = getDeploymentPlanForDeletion();
        List<ConfigurationTask> configurationTasks = List.of(new ConfigurationTask());
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(plan));
        when(deploymentPlanRepository.findByParent(any(DeploymentPlan.class))).thenReturn(List.of(plan, plan));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(new DeploymentTask()));
        when(managementActionTaskRepository.findByRelatedId(anyInt())).thenReturn(List.of(new ManagementActionTask()));
        when(configurationTaskRepository.findByDeploymentService(any())).thenReturn(configurationTasks);
        when(jmxParameterRepository.findAllByDeploymentPlanId(any())).thenReturn(List.of(new JmxParameter()));


        deploymentsService.deletePlan(1);

        verify(deploymentPlanRepository, times(3)).save(any(DeploymentPlan.class));
        verify(iApiGatewayService, times(1)).removeProfiling(any(DeploymentPlan.class));
        verify(deploymentTaskRepository, times(1)).delete(any(DeploymentTask.class));
        verify(managementActionTaskRepository, times(4)).delete(any(ManagementActionTask.class));
        verify(configurationTaskRepository).deleteAll(configurationTasks);
        verify(jvmParamValuesRepository, times(1)).deleteByDeploymentServiceIds(anySet());
        verify(deploymentPlanRepository, times(1)).deleteById(anyInt());
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    private DeploymentPlan getDeploymentPlanForDeletion()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        plan.setAction(DeploymentAction.READY);
        plan.getPlanProfiles().add(getDummyPlanProfile());
        plan.setConfigurationTask(new DeploymentTask());
        plan.setGcsp(new DeploymentGcsp());
        DeploymentNova deploymentNova = new DeploymentNova();
        deploymentNova.setId(1);
        plan.setNova(deploymentNova);
        return plan;
    }

    @Test
    void when_change_deployment_type_points_to_non_existing_deployment_plan_then_throw_exception()
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NovaException.class, () -> deploymentsService.changeDeploymentType("", 1, new DeploymentTypeChangeDto()));
    }

    @Test
    void when_change_deployment_type_has_plan_with_deployed_status_then_throw_exception()
    {
        DeploymentPlan dummyPlan = getDummyDeploymentPlan();
        dummyPlan.setStatus(DeploymentStatus.DEPLOYED);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(dummyPlan));
        Assertions.assertThrows(NovaException.class, () -> this.deploymentsService.changeDeploymentType("CODE", 1, new DeploymentTypeChangeDto()));
    }

    @Test
    void when_change_deployment_type_has_plan_with_undeployed_status_then_throw_exception()
    {
        DeploymentPlan dummyPlan = getDummyDeploymentPlan();
        dummyPlan.setStatus(DeploymentStatus.UNDEPLOYED);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(dummyPlan));

        Assertions.assertThrows(NovaException.class, () -> this.deploymentsService.changeDeploymentType("CODE", 1, new DeploymentTypeChangeDto()));
    }

    @Test
    void when_change_deployment_type_has_deployment_plan_with_pending_deployment_change_type_task_then_throw_exception()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(plan));
        when(deploymentPlanRepository.planHasPendingDeploymentTypeChangeTask(anyInt())).thenReturn(true);

        assertThrows(NovaException.class, () -> deploymentsService.changeDeploymentType("", 1, new DeploymentTypeChangeDto()));
    }

    @Test
    void when_change_deployment_type_has_deployment_plan_with_pending_deployment_tasks_then_throw_exception()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(plan));
        when(deploymentPlanRepository.planHasPendingDeploymentTypeChangeTask(anyInt())).thenReturn(false);
        when(deploymentTaskRepository.planPendingDeploymentTasks(anyInt())).thenReturn(List.of(new DeploymentTask()));

        assertThrows(NovaException.class, () -> deploymentsService.changeDeploymentType("", 1, new DeploymentTypeChangeDto()));
        verify(deploymentTaskRepository, times(1)).planPendingDeploymentTasks(anyInt());
    }

    @Test
    void when_change_deployment_type_has_valid_deployment_plan_then_save_plan()
    {
        DeploymentPlan plan = getDummyDeploymentPlan();
        plan.setStatus(DeploymentStatus.DEFINITION);
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(plan));
        when(deploymentPlanRepository.planHasPendingDeploymentTypeChangeTask(anyInt())).thenReturn(false);

        deploymentsService.changeDeploymentType("", 1, getDummyDeploymentTypeChangeDto());

        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(deploymentUtils, times(1)).addHistoryEntry(any(ChangeType.class), any(DeploymentPlan.class), anyString(), anyString());
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_migrate_plan_points_to_non_existing_deployment_plan_then_throw_exception() throws Exception
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(NovaException.class, () -> deploymentsService.migratePlan(1, 1));
        verifyNoInteractions(deploymentsValidator);
    }

    @Test
    void when_migrate_plan_for_valid_deployment_plan_then_return_migration_result() throws Exception
    {
        when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(versionRepository.findById(anyInt())).thenReturn(Optional.of(getDummyReleaseVersion()));

        deploymentsService.migratePlan(1, 1);

        verify(migrator, times(1)).migratePlan(any(DeploymentPlan.class), any(DeploymentPlan.class));
    }

    @Test
    void when_get_deployment_plans_between_has_null_end_date_then_return_plans_until_current_date()
    {
        this.deploymentsService.getDeploymentPlansBetween("INT", 1, Calendar.getInstance(), null, "DEPLOYED,UNDEPLOYED");

        verify(this.deploymentPlanRepository, times(1)).getByProductAndEnvironmentAndStatusBetweenDates(anyInt(),
                anyList(),anyString(), any(Calendar.class), any(Calendar.class));
    }

    @Test
    void when_get_deployment_plans_between_has_end_date_then_return_plans_until_current_date()
    {
        this.deploymentsService.getDeploymentPlansBetween("INT", 1, Calendar.getInstance(), Calendar.getInstance(), "DEPLOYED,UNDEPLOYED");

        verify(this.deploymentPlanRepository, times(1)).getByProductAndEnvironmentAndStatusBetweenDates(anyInt(),
                anyList(),anyString(), any(Calendar.class), any(Calendar.class));
    }

    @Test
    void when_unschedule_points_to_non_existing_deployment_plan_then_throw_exception()
    {
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NovaException.class, () -> this.deploymentsService.unschedule("USER", 1));
        verifyNoInteractions(deploymentsValidator);
    }

    @Test
    void when_unschedule_has_deploy_pre_todo_task_type_then_notification_mail_is_sent_with_deploy_subject_and_plan_is_unscheduled() throws Errors
    {
        Product product = getDummyProduct();
        DeploymentTask deploymentTask = getDummyDeploymentTask(ToDoTaskType.DEPLOY_PRE);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(deploymentTask));
        when(mailServiceClient.getFullName(any(USUserDTO.class))).thenReturn("USER_NAME");
        when(usersClient.getUser(anyString(), any(Errors.class))).thenReturn(getDummyUSUserDto());

        this.deploymentsService.unschedule("USER", 1);

        verify(deploymentTaskRepository, times(1)).save(any(DeploymentTask.class));
        verify(mailServiceClient, times(1)).sendPlanManagerResolveNotification(product.getName(), deploymentTask.getId(), deploymentTask.getDeploymentPlan().getEnvironment(), "USER_NAME - USER", deploymentTask.getProduct().getId(), deploymentTask.getDeploymentPlan().getId(), MailServiceConstants.DEPLOY_PLAN_SUBJECT);
        verify(deployerService, times(1)).unscheduleDeployment(anyString(), any(DeploymentPlan.class));
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_unschedule_has_deploy_pro_todo_task_type_then_notification_mail_is_sent_with_deploy_subject_and_plan_is_unscheduled() throws Errors
    {
        //private static List<ToDoTaskType> DEPLOY_SUBJECT_TODO_TASK_TYPES = List.of(ToDoTaskType.DEPLOY_PRE, ToDoTaskType.DEPLOY_PRO, ToDoTaskType.SCHEDULE_PLANNING, ToDoTaskType.NOVA_PLANNING);
        Product product = getDummyProduct();
        DeploymentTask deploymentTask = getDummyDeploymentTask(ToDoTaskType.DEPLOY_PRO);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(deploymentTask));
        when(mailServiceClient.getFullName(any(USUserDTO.class))).thenReturn("USER_NAME");
        when(usersClient.getUser(anyString(), any(Errors.class))).thenReturn(getDummyUSUserDto());

        this.deploymentsService.unschedule("USER", 1);

        verify(deploymentTaskRepository, times(1)).save(any(DeploymentTask.class));
        verify(mailServiceClient, times(1)).sendPlanManagerResolveNotification(product.getName(), deploymentTask.getId(), deploymentTask.getDeploymentPlan().getEnvironment(), "USER_NAME - USER", deploymentTask.getProduct().getId(), deploymentTask.getDeploymentPlan().getId(), MailServiceConstants.DEPLOY_PLAN_SUBJECT);
        verify(deployerService, times(1)).unscheduleDeployment(anyString(), any(DeploymentPlan.class));
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_unschedule_has_schedule_planning_todo_task_type_then_notification_mail_is_sent_with_deploy_subject_and_plan_is_unscheduled() throws Errors
    {
        Product product = getDummyProduct();
        DeploymentTask deploymentTask = getDummyDeploymentTask(ToDoTaskType.SCHEDULE_PLANNING);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(deploymentTask));
        when(mailServiceClient.getFullName(any(USUserDTO.class))).thenReturn("USER_NAME");
        when(usersClient.getUser(anyString(), any(Errors.class))).thenReturn(getDummyUSUserDto());

        this.deploymentsService.unschedule("USER", 1);

        verify(deploymentTaskRepository, times(1)).save(any(DeploymentTask.class));
        verify(mailServiceClient, times(1)).sendPlanManagerResolveNotification(product.getName(), deploymentTask.getId(), deploymentTask.getDeploymentPlan().getEnvironment(), "USER_NAME - USER", deploymentTask.getProduct().getId(), deploymentTask.getDeploymentPlan().getId(), MailServiceConstants.DEPLOY_PLAN_SUBJECT);
        verify(deployerService, times(1)).unscheduleDeployment(anyString(), any(DeploymentPlan.class));
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_unschedule_has_nova_planning_todo_task_type_then_notification_mail_is_sent_with_deploy_subject_and_plan_is_not_unscheduled() throws Errors
    {
        //private static List<ToDoTaskType> DEPLOY_SUBJECT_TODO_TASK_TYPES = List.of(ToDoTaskType.SCHEDULE_PLANNING, ToDoTaskType.NOVA_PLANNING);
        Product product = getDummyProduct();
        DeploymentTask deploymentTask = getDummyDeploymentTask(ToDoTaskType.NOVA_PLANNING);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(deploymentTask));
        when(mailServiceClient.getFullName(any(USUserDTO.class))).thenReturn("USER_NAME");
        when(usersClient.getUser(anyString(), any(Errors.class))).thenReturn(getDummyUSUserDto());

        this.deploymentsService.unschedule("USER", 1);

        verify(deploymentTaskRepository, times(1)).save(any(DeploymentTask.class));
        verify(mailServiceClient, times(1)).sendPlanManagerResolveNotification(product.getName(), deploymentTask.getId(), deploymentTask.getDeploymentPlan().getEnvironment(), "USER_NAME - USER", deploymentTask.getProduct().getId(), deploymentTask.getDeploymentPlan().getId(), MailServiceConstants.DEPLOY_PLAN_SUBJECT);
        verifyNoInteractions(deployerService);
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_unschedule_has_undeploy_pro_todo_task_type_then_notification_mail_is_sent_with_undeploy_subject_and_plan_is_unscheduled() throws Errors
    {
        Product product = getDummyProduct();
        DeploymentTask deploymentTask = getDummyDeploymentTask(ToDoTaskType.UNDEPLOY_PRO);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(deploymentTask));
        when(mailServiceClient.getFullName(any(USUserDTO.class))).thenReturn("USER_NAME");
        when(usersClient.getUser(anyString(), any(Errors.class))).thenReturn(getDummyUSUserDto());

        this.deploymentsService.unschedule("USER", 1);

        verify(deploymentTaskRepository, times(1)).save(any(DeploymentTask.class));
        verify(mailServiceClient, times(1)).sendPlanManagerResolveNotification(product.getName(), deploymentTask.getId(), deploymentTask.getDeploymentPlan().getEnvironment(), "USER_NAME - USER", deploymentTask.getProduct().getId(), deploymentTask.getDeploymentPlan().getId(), MailServiceConstants.UNDEPLOY_PLAN_SUBJECT);
        verify(deployerService, times(1)).unscheduleDeployment(anyString(), any(DeploymentPlan.class));
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    @Test
    void when_unschedule_has_undeploy_pre_todo_task_type_then_notification_mail_is_sent_with_undeploy_subject_and_plan_is_unscheduled() throws Errors
    {
        Product product = getDummyProduct();
        DeploymentTask deploymentTask = getDummyDeploymentTask(ToDoTaskType.UNDEPLOY_PRE);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(getDummyDeploymentPlan()));
        when(deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(List.of(deploymentTask));
        when(mailServiceClient.getFullName(any(USUserDTO.class))).thenReturn("USER_NAME");
        when(usersClient.getUser(anyString(), any(Errors.class))).thenReturn(getDummyUSUserDto());

        this.deploymentsService.unschedule("USER", 1);

        verify(deploymentTaskRepository, times(1)).save(any(DeploymentTask.class));
        verify(mailServiceClient, times(1)).sendPlanManagerResolveNotification(product.getName(), deploymentTask.getId(), deploymentTask.getDeploymentPlan().getEnvironment(), "USER_NAME - USER", deploymentTask.getProduct().getId(), deploymentTask.getDeploymentPlan().getId(), MailServiceConstants.UNDEPLOY_PLAN_SUBJECT);
        verify(deployerService, times(1)).unscheduleDeployment(anyString(), any(DeploymentPlan.class));
        verify(deploymentPlanRepository, times(1)).save(any(DeploymentPlan.class));
        verify(novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
    }

    private DeploymentTask getDummyDeploymentTask(ToDoTaskType todoTaskType)
    {
        DeploymentTask deploymentTask = new DeploymentTask();
        deploymentTask.setId(1);
        deploymentTask.setStatus(ToDoTaskStatus.PENDING);
        deploymentTask.setTaskType(todoTaskType);
        deploymentTask.setProduct(getDummyProduct());
        deploymentTask.setDeploymentPlan(getDummyDeploymentPlan());
        return deploymentTask;
    }

    private DeploymentPlan getDummyDeploymentPlan()
    {
        DeploymentPlan item = new DeploymentPlan();
        item.setId(1);
        item.setReleaseVersion(getDummyReleaseVersion());
        item.setEnvironment(Environment.INT.getEnvironment());
        item.setStatus(DeploymentStatus.UNDEPLOYED);
        item.getDeploymentSubsystems().add(getDummyDeploymentSubsystem());
        item.setCurrentRevision(getDummyConfigurationRevision());
        item.setAction(DeploymentAction.STARTING);
        return item;
    }

    private ConfigurationRevision getDummyConfigurationRevision()
    {
        ConfigurationRevision item = new ConfigurationRevision();
        item.setId(1);
        item.getDeploymentConnectorProperties().add(getDummyDeploymentConnectorProperty());
        return item;
    }

    private DeploymentConnectorProperty getDummyDeploymentConnectorProperty()
    {
        DeploymentConnectorProperty item = new DeploymentConnectorProperty();
        item.setId(1);
        item.setDeploymentService(getDummyDeploymentService());
        return item;
    }

    private DeploymentSubsystem getDummyDeploymentSubsystem()
    {
        DeploymentSubsystem item = new DeploymentSubsystem();
        item.setId(1);
        item.getDeploymentServices().add(getDummyDeploymentService());
        return item;
    }

    private DeploymentService getDummyDeploymentService()
    {
        DeploymentService item = new DeploymentService();
        item.setId(1);
        item.setNumberOfInstances(2);
        NovaDeploymentInstance instance = new NovaDeploymentInstance();
        instance.setId(1);
        item.getInstances().add(instance);
        item.setService(getDummyReleaseVersionService());
        return item;
    }

    private Release getDummyRelease()
    {
        Release item = new Release();
        item.setId(1);
        item.setProduct(getDummyProduct());
        return item;
    }

    private ReleaseVersion getDummyReleaseVersion()
    {
        ReleaseVersion item = new ReleaseVersion();
        item.setId(1);
        item.setVersionName("VERSION_NAME");
        item.setRelease(getDummyRelease());
        return item;
    }

    private ReleaseVersionSubsystem getDummyReleaseVersionSubsystem()
    {
        ReleaseVersionSubsystem item = new ReleaseVersionSubsystem();
        item.setId(1);
        item.setReleaseVersion(getDummyReleaseVersion());
        return item;
    }

    private ReleaseVersionService getDummyReleaseVersionService()
    {
        ReleaseVersionService item = new ReleaseVersionService();
        item.setId(1);
        item.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType());
        item.setVersionSubsystem(getDummyReleaseVersionSubsystem());
        return item;
    }

    private Product getDummyProduct()
    {
        Product item = new Product();
        item.setId(1);
        item.setName("PRODUCT_NAME");
        return item;
    }

    private DeploymentDto getDummyDeploymentDto()
    {
        DeploymentDto dto = new DeploymentDto();
        dto.setId(1);
        dto.setSubsystems(new DeploymentSubsystemDto[]{getDummyDeploymentSubsystemDto()});
        return dto;
    }

    private DeploymentSubsystemDto getDummyDeploymentSubsystemDto()
    {
        DeploymentSubsystemDto dto = new DeploymentSubsystemDto();
        dto.setId(1);
        dto.setServices(new DeploymentServiceDto[]{getDummyDeploymentServiceDto()});
        return dto;
    }

    private DeploymentServiceDto getDummyDeploymentServiceDto()
    {
        DeploymentServiceDto dto = new DeploymentServiceDto();
        dto.setId(1);
        dto.setNumberOfInstances(2);
        return dto;
    }

    private HardwarePack getDummyHardwarePack()
    {
        HardwarePack item = new HardwarePack();
        item.setId(1);
        item.setCode("CODE");
        return item;
    }

    private PlanProfile getDummyPlanProfile()
    {
        PlanProfile item = new PlanProfile();
        item.setStatus(ProfileStatus.ACTIVE);
        return item;
    }

    private DeploymentTypeChangeDto getDummyDeploymentTypeChangeDto()
    {
        DeploymentTypeChangeDto dto = new DeploymentTypeChangeDto();
        dto.setDeploymentType(DeploymentType.NOVA_PLANNED.getType());
        return dto;
    }

    private USUserDTO getDummyUSUserDto()
    {
        USUserDTO dto = new USUserDTO();
        dto.setUserName("USER_NAME");
        dto.setSurname2("SURNAME2");
        dto.setUserCode("USER");
        dto.setSurname1("SURNAME1");
        dto.setActive(Boolean.TRUE);
        dto.setEmail("EMAIL");
        dto.setTeams(new String[0]);
        return dto;
    }
}
