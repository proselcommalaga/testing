package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentLogicalConnectorDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSummaryDto;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.datamodel.model.todotask.entities.ConfigurationTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ICipherCredentialService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ScheduleControlMClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.enums.SimpleServiceType;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentGcspService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentNovaService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.MonitoringUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DeploymentPlanDtoBuilderImplTest
{
    @Mock
    private AllowedJdkParameterProductRepository allowedJdkParameterProductRepository;
    @Mock
    private JvmJdkConfigurationChecker jvmJdkConfigurationChecker;
    @Mock
    private DeploymentUtils deploymentUtils;
    @Mock
    private JdkParameterRepository jdkParameterRepository;
    @Mock
    private JmxParameterRepository jmxParameterRepository;
    @Mock
    private IToolsClient toolsClient;
    @Mock
    private ScheduleControlMClient scheduleControlMClient;
    @Mock
    private IAlertServiceApiClient alertServiceApiClient;
    @Mock
    private CPDRepository cpdRepository;
    @Mock
    private MonitoringUtils monitoringUtils;
    @Mock
    private ConfigurationTaskRepository configurationTaskRepository;
    @Mock
    private DeploymentTaskRepository deploymentTaskRepository;
    @Mock
    private ManageValidationUtils manageValidationUtils;
    @Mock
    private IDeploymentNovaService deploymentNovaService;
    @Mock
    private IDeploymentGcspService gcspDeploymentService;
    @Mock
    private ICipherCredentialService cipherCredentialService;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;

    @InjectMocks
    private DeploymentPlanDtoBuilderImpl deploymentPlanDtoBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.deploymentPlanDtoBuilder, "intBaseUrl", "intBaseUrl");
        ReflectionTestUtils.setField(this.deploymentPlanDtoBuilder, "preBaseUrl", "preBaseUrl");
        ReflectionTestUtils.setField(this.deploymentPlanDtoBuilder, "proBaseUrl", "proBaseUrl");
    }

    private void compareFirstPendingTask(DeploymentDto planDto, List<DeploymentTask> deploymentTask)
    {
        Assertions.assertEquals(deploymentTask.get(0).getId(), planDto.getFirstPendingTask().getTaskId());
        Assertions.assertEquals(deploymentTask.get(0).getTaskType().toString(), planDto.getFirstPendingTask().getTaskType());
        Assertions.assertTrue(planDto.getFirstPendingTask().getPending());
    }

    private void compareDeploymentChangeTaskList(DeploymentDto planDto, List<DeploymentTask> deploymentTasksList)
    {
        Assertions.assertEquals(deploymentTasksList.get(0).getId(), planDto.getPendingConfigurationChange().getTaskId());
        Assertions.assertEquals(deploymentTasksList.get(0).getTaskType().toString(), planDto.getPendingDeploymentTypeChangeTask().getTaskType());
        Assertions.assertTrue(planDto.getPendingConfigurationChange().getPending());
    }

    private void compareDeploymentTypeChangeTask(DeploymentDto planDto, List<DeploymentTypeChangeTask> changeTaskList)
    {
        Assertions.assertEquals(changeTaskList.get(0).getId(),
                planDto.getPendingDeploymentTypeChangeTask().getTaskId());
        Assertions.assertEquals(changeTaskList.get(0).getTaskType().toString(),
                planDto.getPendingDeploymentTypeChangeTask().getTaskType());
        Assertions.assertTrue(planDto.getPendingDeploymentTypeChangeTask().getPending());
    }

    private void compareManagementActionTaskList(DeploymentDto planDto, List<ManagementActionTask> managementActionTaskList)
    {
        Assertions.assertEquals(managementActionTaskList.get(0).getId(), planDto.getPendingActionTask().getTaskId());
        Assertions.assertEquals(managementActionTaskList.get(0).getTaskType().toString(),
                planDto.getPendingActionTask().getTaskType());
        Assertions.assertTrue(planDto.getPendingActionTask().getPending());
    }

    private void compareASRequestAlertsDTO(DeploymentDto planDto, ASRequestAlertsDTO aSRequestAlertsDTO)
    {
        Assertions.assertEquals(aSRequestAlertsDTO.getHaveAlerts(), planDto.getHasPendingAlerts().getHaveAlerts());
    }

    private void compareDeploymentPlanVsDeploymentDto(DeploymentPlan plan, DeploymentDto planDto, int numCpds,
                                                      boolean hasFilesystem, boolean hasConnector, boolean isMultiJdk)
    {
        Assertions.assertEquals(plan.getId(), planDto.getId());
        Assertions.assertEquals(plan.getExecutionDate().getTimeInMillis(), planDto.getExecutionDate());
        Assertions.assertEquals(plan.getUndeploymentDate().getTimeInMillis(), planDto.getUndeploymentDate());
        Assertions.assertEquals(plan.getEnvironment(), planDto.getEnvironment());
        Assertions.assertEquals(plan.getRejectionMessage(), planDto.getRejectionMessage());
        Assertions.assertEquals(plan.getReleaseVersion().getRelease().getName(), planDto.getReleaseName());
        Assertions.assertEquals(plan.getReleaseVersion().getRelease().getId(), planDto.getReleaseId());
        Assertions.assertEquals(plan.getAction().toString(), planDto.getAction());
        Assertions.assertEquals(plan.getStatus().toString(), planDto.getStatus());
        Assertions.assertEquals(plan.getSelectedDeploy().toString(), planDto.getSelectedDeploy());
        Assertions.assertEquals(plan.getSelectedLogging().toString(), planDto.getSelectedLogging());
        Assertions.assertEquals(plan.getEtherNs(), planDto.getEtherNSDeploy());
        Assertions.assertEquals(plan.getEtherNs(), planDto.getEtherNSLogging());
        Assertions.assertEquals(plan.getDeploymentSubsystems().get(0).getId(), planDto.getSubsystems()[0].getId());
        Assertions.assertEquals(plan.getDeploymentTypeInPro().getType(), planDto.getDeploymentType());
        Assertions.assertEquals(plan.getCurrentRevision().getId(), planDto.getCurrentRevision());
        Assertions.assertEquals(plan.getMultiCPDInPro(), planDto.getMultiCPD());
        Assertions.assertEquals(plan.getPlanningDocUrl(), planDto.getPlanningDocUrl());
        Assertions.assertEquals(numCpds, planDto.getNumCpds());
        Assertions.assertEquals(plan.getReleaseVersion().getId(), planDto.getReleaseVersionId());
        Assertions.assertEquals(plan.getDeploymentTypeInPro().getType(), planDto.getDeploymentType());
        Assertions.assertEquals(plan.getReleaseVersion().getRelease().getName(), planDto.getReleaseName());
        Assertions.assertEquals(plan.getReleaseVersion().getRelease().getProduct().getUuaa(), planDto.getUuaa());
        Assertions.assertTrue(planDto.getCanBeManagedByUser());
        Assertions.assertTrue(planDto.getHasPendingTasks());
        Assertions.assertTrue(planDto.getPendingConfiguration().getPending());
        Assertions.assertEquals(planDto.getProductionCPD(), plan.getCpdInPro().getName());

        if (hasFilesystem)
        {
            Assertions.assertEquals(planDto.getSubsystems()[0].getServices()[0].getFilesystems()[0].getFilesystemId(),
                    plan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).
                            getDeploymentServiceFilesystems().get(0).getFilesystem().getId()
            );
            Assertions.assertEquals(planDto.getSubsystems()[0].getServices()[0].getFilesystems()[0].getFilesystemName(),
                    plan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).
                            getDeploymentServiceFilesystems().get(0).getFilesystem().getName()
            );
        }

        if (hasConnector)
        {
            DeploymentLogicalConnectorDto connectorDto = planDto.getSubsystems()[0].getServices()[0].getLogicalConnectors()[0];
            LogicalConnector logicalConnector = plan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getLogicalConnectors().get(0);
            Assertions.assertEquals(connectorDto.getLogicalConnectorId(), logicalConnector.getId());
            Assertions.assertEquals(connectorDto.getLogicalConnectorEnvironment(),
                    logicalConnector.getEnvironment());
        }

        if (isMultiJdk)
        {
            Assertions.assertEquals(planDto.getSubsystems()[0].getServices()[0].getReleaseServiceJvmVersion(),
                    plan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService().getAllowedJdk().getJvmVersion()
            );
        }
    }

    private void prepareCommonMocks(String ivUser, DeploymentPlan deploymentPlan,
                                    List<ManagementActionTask> managementActionTaskList, ASRequestAlertsDTO aSRequestAlertsDTO,
                                    List<DeploymentTypeChangeTask> changeTaskList, List<ConfigurationTask> configurationTaskList,
                                    List<DeploymentTask> deploymentTaskList, boolean isMultiJdk)
    {
        TOSubsystemDTO productSubsystem = MocksAndUtils.createTOSubsystemDTO();

        //When
        when(this.deploymentPlanRepository.planHasPendingResources(deploymentPlan.getId())).thenReturn(true);
        when(this.toolsClient.getSubsystemById(any())).thenReturn(productSubsystem);
        when(this.jdkParameterRepository.findByDeploymentService(any())).thenReturn(new ArrayList<>());
        when(this.deploymentUtils.getServiceType(any())).thenReturn(SimpleServiceType.DAEMON);
        when(this.jvmJdkConfigurationChecker.isMultiJdk(any())).thenReturn(isMultiJdk);

        when(this.deploymentPlanRepository.getPendingActionTask(deploymentPlan.getId(), ToDoTaskType.RELEASE_START)).
                thenReturn(managementActionTaskList);
        when(this.alertServiceApiClient.checkDeployPlanAlertInfo(deploymentPlan.getId(),
                new String[]{DeploymentConstants.OPEN_ALERT})).thenReturn(aSRequestAlertsDTO);
        when(this.deploymentPlanRepository.getPendingDeploymentTypeChangeTask(deploymentPlan.getId())).
                thenReturn(changeTaskList);
        when(this.manageValidationUtils.checkIfPlanCanBeManagedByUser(ivUser, deploymentPlan)).thenReturn(true);
        when(this.deploymentTaskRepository.planHasPendingDeploymentTasks(deploymentPlan.getId())).thenReturn(true);
        when(this.deploymentTaskRepository.planPendingDeploymentTasks(deploymentPlan.getId())).
                thenReturn(deploymentTaskList);
        if (isMultiJdk)
        {
            when(this.allowedJdkParameterProductRepository.getSelectableJdkParameters(
                    any(), any(), any(), any())).
                    thenReturn(List.of());
        }
    }

    private void verifyCommonMocks(String ivUser, DeploymentPlan deploymentPlan, DeploymentDto result,
                                   List<ManagementActionTask> managementActionTaskList, ASRequestAlertsDTO aSRequestAlertsDTO,
                                   List<DeploymentTypeChangeTask> changeTaskList, List<ConfigurationTask> configurationTaskList,
                                   List<DeploymentTask> deploymentTaskList, boolean addFilesystem, boolean hasConnector, boolean isMultiJdk)
    {
        verify(this.deploymentPlanRepository, times(1)).planHasPendingResources(deploymentPlan.getId());
        verify(this.toolsClient, times(1)).getSubsystemById(any());
        verify(this.jdkParameterRepository, times(1)).findByDeploymentService(any());
        verify(this.deploymentUtils, times(1)).getServiceType(any());
        verify(jvmJdkConfigurationChecker, times(1)).isMultiJdk(any());
        verify(this.deploymentPlanRepository, times(1)).getPendingActionTask(deploymentPlan.getId(), ToDoTaskType.RELEASE_START);
        verify(this.alertServiceApiClient, times(2)).checkDeployPlanAlertInfo(deploymentPlan.getId(),
                new String[]{DeploymentConstants.OPEN_ALERT});
        verify(deploymentPlanRepository, times(1)).getPendingDeploymentTypeChangeTask(deploymentPlan.getId());
        verify(manageValidationUtils, times(1)).checkIfPlanCanBeManagedByUser(ivUser, deploymentPlan);
        verify(deploymentTaskRepository, times(1)).planHasPendingDeploymentTasks(deploymentPlan.getId());
//        verify(configurationTaskRepository, times(1)).getPendingConfigurationTask(deploymentPlan.getId());
        verify(deploymentTaskRepository, times(2)).planPendingDeploymentTasks(deploymentPlan.getId());

        if (isMultiJdk)
        {
            verify(this.allowedJdkParameterProductRepository, times(1)).
                    getSelectableJdkParameters(any(), any(), any(), any());
        }

        Assertions.assertTrue(result.getPendingResources());
        compareDeploymentPlanVsDeploymentDto(deploymentPlan, result, 1, addFilesystem, hasConnector, isMultiJdk);
        compareManagementActionTaskList(result, managementActionTaskList);
        compareASRequestAlertsDTO(result, aSRequestAlertsDTO);
        compareDeploymentTypeChangeTask(result, changeTaskList);
        compareDeploymentChangeTaskList(result, deploymentTaskList);
        compareFirstPendingTask(result, deploymentTaskList);
    }

    private DeploymentDto commonBuildWithPlan(DeploymentPlan deploymentPlan,
                                              boolean addFilesystem, boolean hasConnector, boolean isMultiJdk)
    {
        String ivUser = "ivUser";

        var managementActionTaskList = MocksAndUtils.createManagementActionTaskList(
                ToDoTaskType.CONFIGURATION_TASK, ToDoTaskStatus.PENDING);
        var aSRequestAlertsDTO = MocksAndUtils.createASRequestAlertsDTO();
        var changeTaskList = MocksAndUtils.createDeploymentTypeChangeTaskList(
                ToDoTaskType.DEPLOY_PRO, ToDoTaskStatus.PENDING);
        var configurationTaskList = MocksAndUtils.createConfigurationTaskList(
                ToDoTaskType.DEPLOY_PRO, ToDoTaskStatus.PENDING);
        var deploymentTaskList = MocksAndUtils.createDeploymentTaskList(
                ToDoTaskType.DEPLOY_PRO, ToDoTaskStatus.PENDING);
        //When
        prepareCommonMocks(ivUser, deploymentPlan, managementActionTaskList, aSRequestAlertsDTO,
                changeTaskList, configurationTaskList, deploymentTaskList, isMultiJdk);

        //Then
        DeploymentDto result = deploymentPlanDtoBuilder.build(deploymentPlan, ivUser);

        //Verify
        verifyCommonMocks(ivUser, deploymentPlan, result, managementActionTaskList, aSRequestAlertsDTO,
                changeTaskList, configurationTaskList, deploymentTaskList, addFilesystem, hasConnector, isMultiJdk);

        return result;
    }

    private void buildWithNovaPlan(Environment environment)
    {
        boolean addFilesystem = true;
        boolean hasConnector = true;
        boolean isMultiJdk = true;
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                addFilesystem, hasConnector, isMultiJdk, environment);
        deploymentPlan.setMultiCPDInPro(false);
        commonBuildWithPlan(deploymentPlan, addFilesystem, hasConnector, isMultiJdk);
    }

    @Test
    public void buildWithNovaPlanPro()
    {
        buildWithNovaPlan(Environment.PRO);
    }

    @Test
    public void buildWithNovaPlanPre()
    {
        buildWithNovaPlan(Environment.PRE);
    }

    @Test
    public void buildWithNovaPlanInt()
    {
        buildWithNovaPlan(Environment.INT);
    }

    private LobFile creatFrontcatYaml()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Object> frontcatMap = new LinkedHashMap<>();
        frontcatMap.put("junction", "theJunction");
        frontcatMap.put("contextPath", "theContextPath");
        frontcatMap.put("networkHostEnabled", false);
        map.put(Constants.FRONTCAT, frontcatMap);
        Yaml yaml = new Yaml();
        LobFile file = new LobFile("frontcatName", "frontcatUrl", yaml.dump(map));
        return file;
    }

    private void buildWithFrontcatPlan(Environment environment)
    {
        boolean addFilesystem = true;
        boolean hasConnector = true;
        boolean isMultiJdk = true;
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                addFilesystem, hasConnector, isMultiJdk, environment);
        deploymentPlan.setMultiCPDInPro(false);
        deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0).getService()
                .setServiceType(ServiceType.FRONTCAT_JAVA_SPRING_MVC.getServiceType());

        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        ReleaseVersionService service = deploymentService.getService();
        service.setNovaYml(creatFrontcatYaml());

        var result = commonBuildWithPlan(deploymentPlan, addFilesystem, hasConnector, isMultiJdk);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(ServiceType.FRONTCAT_JAVA_SPRING_MVC.getServiceType(), result.getSubsystems()[0].getServices()[0].getServiceType());
        Assertions.assertTrue(result.getSubsystems()[0].getServices()[0].getEndpointUrl().contains("theJunction"));
        Assertions.assertTrue(result.getSubsystems()[0].getServices()[0].getEndpointUrl().contains("theContextPath"));
    }

    @Test
    public void buildWithFrontcatPlanPro()
    {
        buildWithFrontcatPlan(Environment.PRO);
    }

    @Test
    public void buildWithFrontcatPlanPre()
    {
        buildWithFrontcatPlan(Environment.PRE);
    }

    @Test
    public void buildWithFrontcatPlanInt()
    {
        buildWithFrontcatPlan(Environment.INT);
    }

    private void buildWithEphoenixPlan(Environment environment)
    {
        boolean addFilesystem = true;
        boolean hasConnector = true;
        boolean isMultiJdk = true;
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                addFilesystem, hasConnector, isMultiJdk, environment);
        deploymentPlan.setMultiCPDInPro(false);

        //Add deploymentInstance
        DeploymentService deploymentService = deploymentPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
        deploymentService.setInstances(Arrays.asList(MocksAndUtils.createDeploymentInstance()));

        //Add Ephoenix data
        String instancePort = "port" + ThreadLocalRandom.current().nextInt();
        String instanceName = "name" + ThreadLocalRandom.current().nextInt();
        EphoenixService ephoenixService = new EphoenixService();
        ephoenixService.setInstancePort(instancePort);
        ephoenixService.setInstanceName(instanceName);

        ReleaseVersionService service = deploymentService.getService();
        service.setServiceType(ServiceType.EPHOENIX_ONLINE.getServiceType());
        service.setEphoenixData(ephoenixService);

        var result = commonBuildWithPlan(deploymentPlan, addFilesystem, hasConnector, isMultiJdk);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(ServiceType.EPHOENIX_ONLINE.getServiceType(), result.getSubsystems()[0].getServices()[0].getServiceType());
        Assertions.assertTrue(result.getSubsystems()[0].getServices()[0].getEndpointUrl().contains("igrupobbva"));
        Assertions.assertTrue(result.getSubsystems()[0].getServices()[0].getEndpointUrl().contains(instancePort));
        Assertions.assertTrue(result.getSubsystems()[0].getServices()[0].getEndpointUrl().contains(instanceName));
    }

    @Test
    public void buildWithEphoenixPlanPro()
    {
        buildWithEphoenixPlan(Environment.PRO);
    }

    @Test
    public void buildWithEphoenixPlanPre()
    {
        buildWithEphoenixPlan(Environment.PRE);
    }

    @Test
    public void buildWithEphoenixPlanInt()
    {
        buildWithEphoenixPlan(Environment.INT);
    }

    @Test
    public void buildPlanWithScheduleRequest()
    {
        boolean addFilesystem = false;
        boolean hasConnector = false;
        boolean isMultiJdk = false;
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                addFilesystem, hasConnector, isMultiJdk, Environment.INT);
        deploymentPlan.setMultiCPDInPro(false);
        ScheduleRequest scheduleRequest = MocksAndUtils.createScheduleRequest();
        ScheduleRequest[] scheduleRequestArray = {scheduleRequest};

        when(this.scheduleControlMClient.getValidForDeployment(
                deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(),
                String.valueOf(deploymentPlan.getEnvironment()))).
                thenReturn(scheduleRequestArray);

        var result = commonBuildWithPlan(deploymentPlan, addFilesystem, hasConnector, isMultiJdk);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(scheduleRequest.getId().intValue(),
                result.getSubsystems()[0].getServices()[0].getScheduleRequest().getScheduleRequestId().intValue());
        Assertions.assertEquals(scheduleRequest.getScheduleRequestDocument().getId(),
                result.getSubsystems()[0].getServices()[0].getScheduleRequest().getScheduleRequestDocument().getId());
        Assertions.assertEquals(scheduleRequest.getScheduleRequestDocument().getSystemName(),
                result.getSubsystems()[0].getServices()[0].getScheduleRequest().getScheduleRequestDocument().getSystemName());
    }

    @Test
    public void buildPlans()
    {
        boolean addFilesystem = false;
        boolean hasConnector = false;
        boolean isMultiJdk = false;
        String ivUser = "ivUser";
        var plan = MocksAndUtils.createDeploymentPlan(
                addFilesystem, hasConnector, isMultiJdk, Environment.PRO);
        plan.setMultiCPDInPro(false);
        var plans = Arrays.asList(plan);

        var managementActionTaskList = MocksAndUtils.createManagementActionTaskList(
                ToDoTaskType.CONFIGURATION_TASK, ToDoTaskStatus.PENDING);
        var aSRequestAlertsDTO = MocksAndUtils.createASRequestAlertsDTO();
        var changeTaskList = MocksAndUtils.createDeploymentTypeChangeTaskList(
                ToDoTaskType.DEPLOY_PRO, ToDoTaskStatus.PENDING);
        var configurationTaskList = MocksAndUtils.createConfigurationTaskList(
                ToDoTaskType.DEPLOY_PRO, ToDoTaskStatus.PENDING);
        var deploymentTaskList = MocksAndUtils.createDeploymentTaskList(
                ToDoTaskType.DEPLOY_PRO, ToDoTaskStatus.PENDING);

        //When
        prepareCommonMocks(ivUser, plan, managementActionTaskList, aSRequestAlertsDTO,
                changeTaskList, configurationTaskList, deploymentTaskList, isMultiJdk);

        //Then
        DeploymentDto result = deploymentPlanDtoBuilder.build(plans, ivUser)[0];

        //Verify
        verifyCommonMocks(ivUser, plan, result, managementActionTaskList, aSRequestAlertsDTO,
                changeTaskList, configurationTaskList, deploymentTaskList, addFilesystem, hasConnector, isMultiJdk);
    }


    @Test
    public void buildDtoFromEntityJMXService()
    {

        DeploymentService deploymentService = this.createDeploymentService();
        ReleaseVersionService service = deploymentService.getService();
        DeploymentPlan plan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();
        ReleaseVersion version = plan.getReleaseVersion();
        Release release = version.getRelease();
        Product product = release.getProduct();
        AllowedJdk allowedJdk = service.getAllowedJdk();
        String jvmVersion = allowedJdk.getJvmVersion();
        List<JdkParameter> jdkParameterList = new ArrayList<>();
        JdkParameter jdkParameter = new JdkParameter();
        jdkParameter.setName("JmxParams");
        jdkParameterList.add(jdkParameter);

        JmxParameter jmxParameter = new JmxParameter();

        //When
        when(this.jvmJdkConfigurationChecker.isMultiJdk(any())).thenReturn(true);
        when(this.jdkParameterRepository.findByDeploymentService(any())).thenReturn(jdkParameterList);
        when(this.jmxParameterRepository.findByDeploymentInstance(any())).thenReturn(Optional.of(jmxParameter));
        when(this.cipherCredentialService.decryptPassword(any())).thenReturn("Password1");

        //Then
        DeploymentServiceDto result = deploymentPlanDtoBuilder.buildDtoFromEntity(deploymentService);

        //Verify
        verify(this.allowedJdkParameterProductRepository, times(1)).getSelectableJdkParameters(product.getId(), deploymentService.getId(), jvmVersion, allowedJdk.getJdk());
        verify(this.jdkParameterRepository, times(1)).findByDeploymentService(any());
        verify(this.jmxParameterRepository, times(3)).findByDeploymentInstance(any());
        verify(this.jvmJdkConfigurationChecker, times(1)).isMultiJdk(any());
        Assertions.assertEquals(result.getJmxParameters().length, 3);
    }

    @Test
    public void buildDtoFromEntityNotJMXService()
    {

        DeploymentService deploymentService = this.createDeploymentService();
        List<JdkParameter> jdkParameterList = new ArrayList<>();
        JdkParameter jdkParameter = new JdkParameter();
        jdkParameter.setName("OtherParam");
        jdkParameterList.add(jdkParameter);

        JmxParameter jmxParameter = new JmxParameter();

        //When
        when(this.jvmJdkConfigurationChecker.isMultiJdk(any())).thenReturn(true);
        when(this.jdkParameterRepository.findByDeploymentService(any())).thenReturn(jdkParameterList);
        when(this.jmxParameterRepository.findByDeploymentInstance(any())).thenReturn(Optional.of(jmxParameter));

        //Then
        DeploymentServiceDto result = deploymentPlanDtoBuilder.buildDtoFromEntity(deploymentService);

        //Verify
        verify(this.jvmJdkConfigurationChecker, times(1)).isMultiJdk(any());
        Assertions.assertEquals(result.getJmxParameters(), null);

    }


    @Test
    public void buildSummary()
    {
        boolean addFilesystem = false;
        boolean hasConnector = false;
        boolean isMultiJdk = false;
        var deploymentPlan = MocksAndUtils.createDeploymentPlan(
                addFilesystem, hasConnector, isMultiJdk, Environment.INT);

        DeploymentSummaryDto result = deploymentPlanDtoBuilder.buildSummary(deploymentPlan, "ivUser");

        Assertions.assertEquals(deploymentPlan.getId(), result.getId());
        Assertions.assertEquals(deploymentPlan.getExecutionDate().getTimeInMillis(), result.getExecutionDate());
        Assertions.assertEquals(deploymentPlan.getUndeploymentDate().getTimeInMillis(), result.getUndeploymentDate());
        Assertions.assertEquals(deploymentPlan.getStatus().toString(), result.getStatus());
        Assertions.assertEquals(deploymentPlan.getEnvironment(), result.getEnvironment());
    }

    private DeploymentService createDeploymentService()
    {
        Product product = new Product();
        product.setId(123);
        Release release = new Release();
        release.setName("Bravo");
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        DeploymentPlan plan = new DeploymentPlan();
        plan.setReleaseVersion(releaseVersion);
        plan.setEnvironment("INT");
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(1);
        releaseVersionSubsystem.setReleaseVersion(releaseVersion);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setSubsystem(releaseVersionSubsystem);
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setDeploymentSubsystem(subsystem);
        service.setId(1);
        service.setNumberOfInstances(3);

        DeploymentInstance deploymentInstance1 = new NovaDeploymentInstance();
        deploymentInstance1.setId(101);
        DeploymentInstance deploymentInstance2 = new NovaDeploymentInstance();
        deploymentInstance2.setId(202);
        DeploymentInstance deploymentInstance3 = new NovaDeploymentInstance();
        deploymentInstance3.setId(303);
        service.setInstances(Arrays.asList(deploymentInstance1, deploymentInstance2, deploymentInstance3));

        List<DeploymentServiceAllowedJdkParameterValue> values = new ArrayList<>();
        var value = new DeploymentServiceAllowedJdkParameterValue();

        var allowed = new AllowedJdkParameterProduct();
        allowed.setId(213);
        value.setAllowedJdkParameterProduct(allowed);

        values.add(value);
        service.setParamValues(values);

        AllowedJdk allowedJdk = new AllowedJdk();
        allowedJdk.setJdk("zulu11.48.21");
        allowedJdk.setJvmVersion("11.0.7");

        ReleaseVersionService rvs = new ReleaseVersionService();
        rvs.setFinalName("FinalName");
        rvs.setServiceName("ServiceName");
        rvs.setGroupId("com.bbva.enoa.platformservices");
        rvs.setArtifactId("coreservice");
        rvs.setVersion("1.0.0");
        rvs.setAllowedJdk(allowedJdk);
        rvs.setVersionSubsystem(releaseVersionSubsystem);
        rvs.setServiceType(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());

        service.setService(rvs);


        return service;
    }
}