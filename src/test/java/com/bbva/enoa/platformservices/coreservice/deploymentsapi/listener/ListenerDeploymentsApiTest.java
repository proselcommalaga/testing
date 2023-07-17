package com.bbva.enoa.platformservices.coreservice.deploymentsapi.listener;


public class ListenerDeploymentsApiTest
{

}

//TODO Krypton9 -
//{
//    @Rule
//    public final ExpectedException exception = ExpectedException.none();
//
//    @Mock
//    private DeploymentPlanRepository deploymentPlanRepository;
//
//    @Mock
//    private ReleaseVersionRepository versionRepository;
//
//    @Mock
//    private DeploymentServiceRepository deploymentServiceRepository;
//
//    @Mock
//    private IDeploymentsService service;
//
//    @Mock
//    private DeploymentPlanDtoBuilderImpl planBuilder;
//
//    @Mock
//    private ITaskProcessor ITaskProcessor;
//
//    @Mock
//    private DeploymentsValidatorImpl deploymentsValidator;
//
//    @Mock
//    private IDeploymentStatusService statusService;
//
//    @Mock
//    private DeploymentManagerCreateClient deploymentmanagerCreateClient;
//
//    @Mock
//    private IDeploymentChangesDtoBuilder changesBuilder;
//
//    @Mock
//    private IErrorTaskManager taskError;
//
//    @Mock
//    private IProductUsersClient usersClient;
//
//    @Mock
//    private IReleaseVersionValidator releaseVersionValidator;
//
//    @Mock
//    private ILibraryManagerService libraryManagerService;
//
//    @Mock
//    private IDeploymentChangeService deploymentChangeService ;
//
//    @InjectMocks
//    private ListenerDeploymentsApi listenerDeploymentsApi;
//
//    private DeploymentException PERMISSION_DENIED =
//            new DeploymentException(DeploymentError.PERMISSION_DENIED, "Permission Error");
//
//    private DeploymentPlan deploymentPlan;
//
//    private ReleaseVersion releaseVersion;
//
//    private Metadata metadata;
//
//    @Test
//    public void archiveDeploymentPlan() throws StandardHttpException
//    {
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "ARCHIVE_DEPLOYMENT", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.listenerDeploymentsApi.archiveDeploymentPlan(this.metadata, 1);
//
//        verify(this.service, times(1)).archivePlan(1);
//
//        //Given
//        doThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException())).when(this.service).archivePlan(1);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.archiveDeploymentPlan(this.metadata, 1);
//    }
//
//    @Test
//    public void archiveDeploymentPlanException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error")).when(this.service).archivePlan(1);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.archiveDeploymentPlan(this.metadata, 1);
//    }
//
//    @Test
//    public void changeDeploymentType() throws StandardHttpException
//    {
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT_TYPE", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Given
//        DeploymentTypeChangeDto deploymentTypeChangeDto = new DeploymentTypeChangeDto();
//
//        //Then
//        this.listenerDeploymentsApi.changeDeploymentType(this.metadata, deploymentTypeChangeDto, 1);
//        verify(this.service, times(1)).changeDeploymentType("IMM0589", 1, deploymentTypeChangeDto);
//
//        //Given
//        doThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException())).when(this.service).changeDeploymentType("IMM0589", 1, deploymentTypeChangeDto);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.changeDeploymentType(this.metadata, deploymentTypeChangeDto, 1);
//    }
//
//    @Test
//    public void changeDeploymentTypeDeploymentException() throws StandardHttpException
//    {
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT_TYPE", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Given
//        DeploymentTypeChangeDto deploymentTypeChangeDto = new DeploymentTypeChangeDto();
//        doThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "")).when(this.service).changeDeploymentType("IMM0589", 1, deploymentTypeChangeDto);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.changeDeploymentType(this.metadata, deploymentTypeChangeDto, 1);
//    }
//
//    @Test
//    public void copyPlan() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        deploymentPlan.setId(1);
//        //Then
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "COPY_PLAN", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//        when(this.service.copyPlan(deploymentPlan)).thenReturn(deploymentPlan);
//
//        //Then
//        DeploymentPlan response = this.service.copyPlan(deploymentPlan);
//        assertEquals(deploymentPlan, response);
//
//        //Given
//        DeploymentDto deploymentDto = new DeploymentDto();
//        DeploymentPlan deploymentPlan2 = new DeploymentPlan();
//        deploymentPlan2.setId(2);
//        deploymentPlan2.setEnvironment(Environment.INT);
//
//        Release release = new Release();
//        releaseVersion = new ReleaseVersion();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//        deploymentPlan2.setReleaseVersion(releaseVersion);
//
//        when(this.planBuilder.build(deploymentPlan2, "IMM0589")).thenReturn(deploymentDto);
//        when(this.deploymentPlanRepository.findOne(2)).thenReturn(deploymentPlan2);
//
//        //Then
//        DeploymentDto response2 = this.planBuilder.build(deploymentPlan2, "IMM0589");
//        assertEquals(deploymentDto, response2);
//        when(this.service.copyPlan(deploymentPlan2)).thenReturn(deploymentPlan);
//        this.listenerDeploymentsApi.copyPlan(this.metadata, 2);
//
//        //Given
//        when(this.service.copyPlan(deploymentPlan)).thenThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error"));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.copyPlan(this.metadata, 1);
//    }
//
//    @Test
//    public void copyPlanRunntimeException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "COPY_PLAN", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        when(this.service.copyPlan(deploymentPlan)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.copyPlan(this.metadata, 1);
//    }
//
//    @Test
//    public void cratePlanDeploymentException() throws StandardHttpException
//    {
//        //Given
//        doThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error")).when(this.service).createDeployment(1, Environment.LOCAL.getEnvironment(), false);
//        //Then
//        when(this.versionRepository.findOne(1)).thenReturn(releaseVersion);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "CREATE_DEPLOYMENT", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//        //Thens
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.createDeploymentPlan(this.metadata, 1,
//                Environment.LOCAL.getEnvironment());
//    }
//
//    @Test
//    public void createDeploymentPlan() throws StandardHttpException
//    {
//        when(this.versionRepository.findOne(1)).thenReturn(releaseVersion);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "CREATE_DEPLOYMENT", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.listenerDeploymentsApi.createDeploymentPlan(this.metadata, 1,
//                Environment.LOCAL.getEnvironment());
//        verify(this.service, times(1)).createDeployment(1, Environment.LOCAL.getEnvironment(), false);
//
//        //Given
//        doThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException())).when(this.service).createDeployment(1, Environment.LOCAL.getEnvironment(), false);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.createDeploymentPlan(this.metadata, 1,
//                Environment.LOCAL.getEnvironment());
//    }
//
//    @Test
//    public void deletePlan() throws StandardHttpException
//    {
//        //Then
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "DELETE_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.listenerDeploymentsApi.deletePlan(this.metadata, 1);
//        verify(this.service, times(1)).deletePlan(1);
//
//        //Given
//        doThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException())).when(this.service).deletePlan(1);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.deletePlan(this.metadata, 1);
//    }
//
//    @Test
//    public void deletePlanDeploymentException() throws StandardHttpException
//    {
//        //Given
//        doThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error")).when(this.service).deletePlan(1);
//        //Then
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "DELETE_DEPLOYMENT", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.deletePlan(this.metadata, 1);
//    }
//
//    @Test
//    public void deploy() throws Exception
//    {
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "DEPLOY_DEPLOYMENT", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.listenerDeploymentsApi.deploy(this.metadata, 1, true);
//        verify(this.service, times(1)).deploy("IMM0589", 1, true);
//
//        //Given
//        doThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException())).when(this.service).deploy("IMM0589", 1, true);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.deploy(this.metadata, 1, true);
//    }
//
//    @Test
//    public void deployBudgetsException() throws Exception
//    {
//        //Given
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doThrow(new BudgetsException(BudgetsError.UNEXPECTED_ERROR, "Error")).when(this.service).deploy("IMM0589", 1, true);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "DEPLOY_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.deploy(this.metadata, 1, true);
//    }
//
//    @Test
//    public void deployException() throws Exception
//    {
//        //Given
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error")).when(this.service).deploy("IMM0589", 1, true);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "DEPLOY_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.deploy(this.metadata, 1, true);
//    }
//
//    @Test
//    public void getAllDeploymentInstanceStatus() throws StandardHttpException
//    {
//        //Given
//        ActionStatus status = new ActionStatus();
//        when(this.statusService.getAllDeploymentInstanceStatus(1)).thenReturn(status);
//
//        //Then
//        ActionStatus response = this.listenerDeploymentsApi.getAllDeploymentInstanceStatus(this.metadata, 1);
//        assertEquals(status, response);
//
//        //Given
//        when(this.statusService.getAllDeploymentInstanceStatus(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getAllDeploymentInstanceStatus(this.metadata, 1);
//    }
//
//    @Test
//    public void getDeploymentPlan() throws StandardHttpException
//    {
//        //Given
//        DeploymentDto deploymentDto = new DeploymentDto();
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        when(this.planBuilder.build(deploymentPlan, "IMM0589")).thenReturn(deploymentDto);
//
//        //Then
//        this.listenerDeploymentsApi.getDeploymentPlan(this.metadata, 1);
//        verify(this.deploymentsValidator, times(1)).checkPlanExistence(deploymentPlan);
//        DeploymentDto response = this.planBuilder.build(deploymentPlan, "IMM0589");
//        assertEquals(deploymentDto, response);
//
//        //Given
//        when(this.deploymentPlanRepository.findOne(1)).thenThrow(new RuntimeException(DeploymentError.NO_SUCH_SERVICE.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getDeploymentPlan(this.metadata, 1);
//
//    }
//
//    @Test
//    public void getDeploymentPlanDeploymentException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentPlanRepository.findOne(1)).thenThrow(new DeploymentException(DeploymentError.NO_SUCH_SERVICE, ""));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getDeploymentPlan(this.metadata, 1);
//    }
//
//    @Test
//    public void getDeploymentPlansByEnvironment() throws StandardHttpException
//    {
//        //Given
//        List<DeploymentPlan> plans = new ArrayList<>();
//        when(this.deploymentPlanRepository.getByProductAndEnvironment(1, Environment.LOCAL)).thenReturn(plans);
//
//        //Then
//        List<DeploymentPlan> response = this.deploymentPlanRepository.getByProductAndEnvironment(1, Environment.LOCAL);
//        assertEquals(plans, response);
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment(), "");
//
//        //Given
//        List<DeploymentPlan> plans2 = new ArrayList<>();
//        when(this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(1, Environment.LOCAL, DeploymentStatus.DEPLOYED)).thenReturn(plans);
//
//        //Then
//        List<DeploymentPlan> response2 = this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(1, Environment.LOCAL, DeploymentStatus.DEPLOYED);
//        assertEquals(plans2, response2);
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus());
//
//        //Given
//        List<DeploymentPlan> plans3 = new ArrayList<>();
//        DeploymentDto deploymentDto = new DeploymentDto();
//        DeploymentDto[] deploymentDtos = new DeploymentDto[]{deploymentDto};
//        when(this.planBuilder.build(plans3, "IMM0589")).thenReturn(deploymentDtos);
//
//        //Then
//        DeploymentDto[] response3 = this.planBuilder.build(plans3, "IMM0589");
//        assertEquals(deploymentDto, response3[0]);
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus());
//
//        //Given
//        when(this.planBuilder.build(plans, "IMM0589")).thenThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error"));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus());
//    }
//
//    @Test
//    public void getDeploymentPlansByEnvironmentAndFilters() throws StandardHttpException
//    {
//        //Given
//        List<DeploymentPlan> deploymentPlanList = new ArrayList<DeploymentPlan>();
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//        deploymentPlanList.add(deploymentPlan);
//
//        DeploymentSummaryDto deploymentSummaryDto = new DeploymentSummaryDto();
//
//        when(this.service.getDeploymentPlansBetween("Local", 1, null, null)).thenReturn(deploymentPlanList);
//        when(this.planBuilder.buildSummary(deploymentPlan, "IMM0589")).thenReturn(deploymentSummaryDto);
//
//        //Then
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironmentAndFilters(this.metadata, 1, "Local", "",
//                "", "status");
//
//        //Given
//        when(this.service.getDeploymentPlansBetween("Local", 1, null, null)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironmentAndFilters(this.metadata, 1, "Local", "", "", "status");
//    }
//
//    @Test
//    public void getDeploymentPlansByEnvironmentRunntimeException() throws StandardHttpException
//    {
//        //Given
//        List<DeploymentPlan> plans = new ArrayList<>();
//        when(this.planBuilder.build(plans, "IMM0589")).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getDeploymentPlansByEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus());
//    }
//
//    @Test
//    public void getDeploymentPlanStatus() throws StandardHttpException
//    {
//        //Given
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setAction(DeploymentAction.READY);
//        ActionStatus status = new ActionStatus();
//        when(this.statusService.getDeploymentPlanStatus(1)).thenReturn(status);
//
//        //Then
//        ActionStatus response = this.listenerDeploymentsApi.getDeploymentPlanStatus(this.metadata, 1);
//        assertEquals(status, response);
//
//        //Given
//        when(this.statusService.getDeploymentPlanStatus(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getDeploymentPlanStatus(this.metadata, 1);
//    }
//
//    @Test
//    public void getDeploymentService() throws StandardHttpException
//    {
//        //Given
//        DeploymentService deploymentService = new DeploymentService();
//        deploymentService.setId(1);
//        when(this.deploymentServiceRepository.findOne(1)).thenReturn(deploymentService);
//
//        //Then
//        this.listenerDeploymentsApi.getDeploymentService(this.metadata, 1, 1);
//        verify(this.planBuilder, times(1)).buildDtoFromEntity(deploymentService);
//
//        //Given
//        when(this.deploymentServiceRepository.findOne(2)).thenThrow(new DeploymentException(DeploymentError.NO_SUCH_SERVICE, "Error"));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getDeploymentService(this.metadata, 2, 1);
//    }
//
//    @Test
//    public void getDeploymentServiceDeploymentNullException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentServiceRepository.findOne(1)).thenReturn(null);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getDeploymentService(this.metadata, 1, 1);
//    }
//
//
//    @Test
//    public void getDeploymentServiceRunntimeException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentServiceRepository.findOne(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getDeploymentService(this.metadata, 1, 1);
//    }
//
//    @Test
//    public void getDeploymentServiceStatus() throws StandardHttpException
//    {
//        //Given
//        ActionStatus status = new ActionStatus();
//        when(this.statusService.getDeploymentServiceStatus(1)).thenReturn(status);
//
//        //Then
//        ActionStatus response = this.listenerDeploymentsApi.getDeploymentServiceStatus(this.metadata, 1);
//        assertEquals(status, response);
//
//        //Given
//        when(this.statusService.getDeploymentServiceStatus(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getDeploymentServiceStatus(this.metadata, 1);
//    }
//
//    @Test
//    public void getDeploymentSubsystemStatus() throws StandardHttpException
//    {
//        //Given
//        ActionStatus status = new ActionStatus();
//        when(this.statusService.getDeploymentSubsystemStatus(1)).thenReturn(status);
//
//        //Then
//        ActionStatus response = this.listenerDeploymentsApi.getDeploymentSubsystemStatus(this.metadata, 1);
//        assertEquals(status, response);
//
//        //Given
//        when(this.statusService.getDeploymentSubsystemStatus(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getDeploymentSubsystemStatus(this.metadata, 1);
//    }
//
//    @Test
//    public void getHistory() throws StandardHttpException
//    {
//        //Given
//        List<DeploymentChange> deploymentChangeList = new ArrayList<>();
//        DeploymentChange deploymentChange = new DeploymentChange();
//        deploymentChange.setId(1);
//        deploymentChange.setCreationDate(Calendar.getInstance());
//        deploymentChange.setType(ChangeType.CONFIGURATION_CHANGE);
//        deploymentChange.setRefId(1);
//        deploymentChangeList.add(deploymentChange);
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setChanges(deploymentChangeList);
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//
//        //Then
//        when(this.deploymentChangeService.getHistory(anyInt(), anyLong(), anyLong())).thenReturn(new MyPage()) ;
//        DeploymentChangeDtoPage response = this.listenerDeploymentsApi.getHistory(this.metadata, 1, 0l, 5l);
//        verify(this.deploymentsValidator, times(1)).checkPlanExistence(deploymentPlan);
//        assertNull(response);
//
//        //Given
//        when(this.deploymentPlanRepository.findOne(2)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.getHistory(this.metadata, 2, 0l, 5l);
//    }
//
//    @Test
//    public void getHistoryDeploymentException() throws StandardHttpException
//    {
//        //Give
//        when(this.deploymentPlanRepository.findOne(1)).thenThrow(new DeploymentException(DeploymentError.NO_SUCH_SERVICE, ""));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.getHistory(this.metadata, 1, 0l, 5l);
//    }
//
//    @Test
//    public void instanceDeployStatus() throws StandardHttpException
//    {
//        //Given
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
//        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
//        DeploymentService deploymentService = new DeploymentService();
//        deploymentService.setId(1);
//        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
//        when(this.deploymentServiceRepository.findOne(1)).thenReturn(deploymentService);
//
//        //Then
//        this.listenerDeploymentsApi.instanceDeployStatus(this.metadata, 1, "Message", "SUCCESS");
//        verify(this.deploymentServiceRepository, times(1)).save(deploymentService);
//        verify(this.deploymentmanagerCreateClient, times(1)).deployPlan(deploymentPlan);
//
//        //Then
//        this.listenerDeploymentsApi.instanceDeployStatus(this.metadata, 1, "Message", "");
//        verify(this.deploymentServiceRepository, times(1)).save(deploymentService);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.instanceDeployStatus(this.metadata, 2, "Message", "SUCCESS");
//
//    }
//
//    @Test
//    public void instanceDeployStatusDeploymentException() throws StandardHttpException
//    {
//        //Give
//        when(this.deploymentServiceRepository.findOne(1)).thenThrow(new DeploymentException(DeploymentError.NO_SUCH_SERVICE, ""));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.instanceDeployStatus(this.metadata, 1, "Message", "SUCCESS");
//    }
//
//    @Test
//    public void instanceDeployStatusRunntimeException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentServiceRepository.findOne(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.instanceDeployStatus(this.metadata, 1, "Message", "SUCCESS");
//    }
//
//    @Test
//    public void migratePlan() throws StandardHttpException
//    {
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "NEW_DEPLOYMENT_PERMISSION", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Given
//        DeploymentMigrationDto deploymentMigrationDto = new DeploymentMigrationDto();
//        when(this.service.migratePlan(1, 1)).thenReturn(deploymentMigrationDto);
//
//        DeploymentMigrationDto response = this.service.migratePlan(1, 1);
//        assertEquals(deploymentMigrationDto, response);
//
//        //Then
//        this.listenerDeploymentsApi.migratePlan(this.metadata, 1, 1);
//
//        //Given
//        when(this.service.migratePlan(1, 1)).thenThrow(new RuntimeException(DeploymentError.NO_SUCH_SERVICE.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.migratePlan(this.metadata, 1, 1);
//    }
//
//    @Test
//    public void migratePlanDeploymentException() throws StandardHttpException
//    {
//        DeploymentException PERMISSION_DENIED =
//                new DeploymentException(DeploymentError.PERMISSION_DENIED, "Permission Error");
//
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "NEW_DEPLOYMENT_PERMISSION", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Given
//        when(this.service.migratePlan(1, 1)).thenThrow(new DeploymentException(DeploymentError.NO_SUCH_SERVICE, ""));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.migratePlan(this.metadata, 1, 1);
//    }
//
//    @Test
//    public void onTaskReply() throws StandardHttpException
//    {
//        //Given
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//
//        //Then
//        this.listenerDeploymentsApi.onTaskReply(this.metadata, 1, deploymentPlan.getId(), "status");
//        verify(this.ITaskProcessor, times(1)).onTaskreply(deploymentPlan, 1, "status");
//
//        //Given
//        when(this.deploymentPlanRepository.findOne(2)).thenThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error"));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.onTaskReply(this.metadata, 2, 2, "status");
//    }
//
//    @Test
//    public void onTaskReplyRuntimeException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentPlanRepository.findOne(3)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.onTaskReply(this.metadata, 3, 3, "status");
//    }
//
//    @Test
//    public void promotePlanToEnvironment() throws StandardHttpException
//    {
//        //Given
//
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "PROMOTE_PLAN", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//        //Then
//        this.listenerDeploymentsApi.promotePlanToEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment());
//        verify(this.deploymentsValidator, times(1)).checkPlanExistence(deploymentPlan);
//        verify(this.deploymentsValidator, times(1)).checkReleaseVersionStored(deploymentPlan);
//        verify(this.service, times(1)).promotePlanToEnvironment(deploymentPlan, Environment.LOCAL);
//
//        //Given
//        when(this.deploymentPlanRepository.findOne(1)).thenThrow(new RuntimeException(DeploymentError.NO_SUCH_SERVICE.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.promotePlanToEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment());
//    }
//
//    @Test
//    public void promotePlanToEnvironmentDeploymentException() throws StandardHttpException
//    {
//        //Given
//        when(this.deploymentPlanRepository.findOne(1)).thenThrow(new DeploymentException(DeploymentError.NO_SUCH_SERVICE, ""));
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.promotePlanToEnvironment(this.metadata, 1,
//                Environment.LOCAL.getEnvironment());
//    }
//
//    @Test
//    public void remove() throws Exception
//    {
//        //Given
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "DELETE_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.listenerDeploymentsApi.remove(this.metadata, 1);
//        verify(this.service, times(1)).undeployPlan("IMM0589", 1);
//
//        //Given
//        doThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException())).when(this.service).undeployPlan("IMM0589", 1);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.remove(this.metadata, 1);
//    }
//
//    @Test
//    public void removeDeploymentException() throws Exception
//    {
//        //Given
//
//        deploymentPlan.setId(1);
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "REMOVE_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//        doThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error")).when(this.service).undeployPlan("IMM0589", 1);
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.remove(this.metadata, 1);
//    }
//
//    @Before
//    public void setUp() throws StandardHttpException
//    {
//        MockitoAnnotations.initMocks(this);
//        deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setStatus(DeploymentStatus.STORAGED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//        Release release = new Release();
//        releaseVersion = new ReleaseVersion();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//        deploymentPlan.setReleaseVersion(releaseVersion);
//
//        metadata = new Metadata();
//        Set<Map.Entry<String, List<String>>> set = new HashSet<>();
//        Map<String, List<String>> terrible = new HashMap<>();
//        terrible.put("iv-user", Arrays.asList(new String[]{"IMM0589"}));
//        terrible.entrySet().forEach((entry) -> {
//            set.add(entry);
//        });
//        metadata.setImplicitHeadersInput(new ImplicitHeadersInput(set));
//    }
//
//    //TODO: Review this code. Is for config task for library admin (deployments api 3.2.0)
////    @Test
////    public void taskRequest() throws StandardHttpException
////    {
////
////        //Given
////        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
////        TaskRequestDTO taskRequestDto = new TaskRequestDTO();
////        TaskRequestDTO[] taskRequestDtoArray = {taskRequestDto};
////        //Then
////        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "REQUEST_DEPLOYMENT_PROPERTIES", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
////
////        //then
////        this.listenerDeploymentsApi.taskRequest(this.metadata, taskRequestDtoArray, 1);
////        verify(this.taskProcessor, times(1)).createDeploymentTask(taskRequestDtoArray, "IMM0589", deploymentPlan);
////
////        //Given
////        when(this.deploymentPlanRepository.findOne(2)).thenThrow(new DeploymentException(DeploymentError.UNEXPECTED_ERROR, "Error"));
////
////        //Then
////        this.exception.expect(StandardHttpException.class);
////        this.listenerDeploymentsApi.taskRequest(this.metadata, taskRequestDtoArray, 2);
////    }
////
////    @Test
////    public void taskRequestRuntimeException() throws StandardHttpException
////    {
////        //Given
////        TaskRequestDTO taskRequestDto = new TaskRequestDTO();
////        TaskRequestDTO[] taskRequestDtoArray = {taskRequestDto};
////        when(this.deploymentPlanRepository.findOne(2)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
////        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "REQUEST_DEPLOYMENT_PROPERTIES", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
////
////        //Then
////        this.exception.expect(RuntimeException.class);
////        this.listenerDeploymentsApi.taskRequest(this.metadata, taskRequestDtoArray, 2);
////    }
//
//    @Test
//    public void updateDeploymentPlan() throws StandardHttpException
//    {
//        //Given
//        DeploymentDto deploymentDto = new DeploymentDto();
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(this.deploymentPlan);
//        //Then
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.updateDeploymentPlan(this.metadata, deploymentDto, 1);
//
//        //Given
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//
//        ReleaseVersion releaseVersion = new ReleaseVersion();
//        Release release = new Release();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//
//        deploymentPlan.setReleaseVersion(releaseVersion);
//        when(this.deploymentPlanRepository.findOne(2)).thenReturn(deploymentPlan);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.updateDeploymentPlan(this.metadata, deploymentDto, 2);
//    }
//
//    @Test
//    public void updateDeploymentPlanRunntimeException() throws StandardHttpException
//    {
//        //Given
//        DeploymentDto deploymentDto = new DeploymentDto();
//        when(this.deploymentPlanRepository.findOne(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//        //Then
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//
//        ReleaseVersion releaseVersion = new ReleaseVersion();
//        Release release = new Release();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//
//        deploymentPlan.setReleaseVersion(releaseVersion);
//
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.updateDeploymentPlan(this.metadata, deploymentDto, 2);
//    }
//
//    @Test
//    public void updateDeploymentService() throws StandardHttpException
//    {
//        //Given
//        DeploymentServiceDto deploymentServiceDto = new DeploymentServiceDto();
//        DeploymentService deploymentService = new DeploymentService();
//        deploymentService.setId(1);
//        when(this.deploymentServiceRepository.findOne(1)).thenReturn(deploymentService);
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT", Environment.LOCAL.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.listenerDeploymentsApi.updateDeploymentService(this.metadata, deploymentServiceDto, 1, 1);
//        verify(this.service, times(1)).updateServiceFromDto(deploymentServiceDto);
//
//        //Given
//        when(this.deploymentServiceRepository.findOne(2)).thenReturn(null);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.updateDeploymentService(this.metadata, deploymentServiceDto, 2, 1);
//    }
//
//    @Test
//    public void updateDeploymentServiceDeploymentException() throws StandardHttpException
//    {
//        //Given
//        DeploymentServiceDto deploymentServiceDto = new DeploymentServiceDto();
//
//        DeploymentService deploymentService = new DeploymentService();
//        deploymentService.setId(1);
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//
//        ReleaseVersion releaseVersion = new ReleaseVersion();
//        Release release = new Release();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//
//        deploymentPlan.setReleaseVersion(releaseVersion);
//
//        when(this.deploymentServiceRepository.findOne(1)).thenReturn(deploymentService);
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        DeploymentException PERMISSION_DENIED =
//                new DeploymentException(DeploymentError.PERMISSION_DENIED, "Permission Error");
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.updateDeploymentService(this.metadata, deploymentServiceDto, 1, 1);
//    }
//
//    @Test
//    public void updateDeploymentServiceRuntimeException() throws StandardHttpException
//    {
//        //Given
//        DeploymentServiceDto deploymentServiceDto = new DeploymentServiceDto();
//        when(this.deploymentServiceRepository.findOne(1)).thenThrow(new RuntimeException(DeploymentError.UNEXPECTED_ERROR.getStandardHttpException()));
//
//        //Then
//        this.exception.expect(RuntimeException.class);
//        this.listenerDeploymentsApi.updateDeploymentService(this.metadata, deploymentServiceDto, 1, 1);
//    }
//
//    @Test
//    public void updateDeploymentServiceStatusException() throws StandardHttpException
//    {
//        //Given
//        DeploymentServiceDto deploymentServiceDto = new DeploymentServiceDto();
//
//        DeploymentService deploymentService = new DeploymentService();
//        deploymentService.setId(1);
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//
//        ReleaseVersion releaseVersion = new ReleaseVersion();
//        Release release = new Release();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//
//        deploymentPlan.setReleaseVersion(releaseVersion);
//
//        when(this.deploymentServiceRepository.findOne(1)).thenReturn(deploymentService);
//        when(this.deploymentPlanRepository.findOne(1)).thenReturn(deploymentPlan);
//        DeploymentException PERMISSION_DENIED =
//                new DeploymentException(DeploymentError.PERMISSION_DENIED, "Permission Error");
//
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_DEPLOYMENT", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//        //Then
//        this.exception.expect(StandardHttpException.class);
//        this.listenerDeploymentsApi.updateDeploymentService(this.metadata, deploymentServiceDto, 1, 1);
//    }
//
//    @Test
//    public void unSchedule() throws StandardHttpException
//    {
//
//        //Given
//
//        DeploymentService deploymentService = new DeploymentService();
//        deploymentService.setId(1);
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        deploymentPlan.setEnvironment(Environment.LOCAL);
//
//        ReleaseVersion releaseVersion = new ReleaseVersion();
//        Release release = new Release();
//        Product product = new Product();
//        product.setId(1);
//        release.setProduct(product);
//        releaseVersion.setRelease(release);
//
//        deploymentPlan.setReleaseVersion(releaseVersion);
//
//        when(this.deploymentPlanRepository.findById(1)).thenReturn(deploymentPlan);
//        DeploymentException PERMISSION_DENIED =
//                new DeploymentException(DeploymentError.PERMISSION_DENIED, "Permission Error");
//
//        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "MANAGE_DEPLOY", Environment.LAB_INT.getEnvironment(), 1, PERMISSION_DENIED);
//
//
//        //Then
//        this.listenerDeploymentsApi.unschedule (this.metadata,  1);
//
//    }
//
//    class MyPage implements Page<DeploymentChange>
//    {
//		@Override
//		public Iterator<DeploymentChange> iterator() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Pageable previousPageable() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public Pageable nextPageable() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public boolean isLast() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public boolean isFirst() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public boolean hasPrevious() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public boolean hasNext() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public boolean hasContent() {
//			// TODO Auto-generated method stub
//			return false;
//		}
//
//		@Override
//		public Sort getSort() {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public int getSize() {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public int getNumberOfElements() {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public int getNumber() {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public List<DeploymentChange> getContent() {
//			// TODO Auto-generated method stub
//			return new ArrayList<DeploymentChange>();
//		}
//
//		@Override
//		public <S> Page<S> map(Converter<? super DeploymentChange, ? extends S> converter) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		@Override
//		public int getTotalPages() {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//
//		@Override
//		public long getTotalElements() {
//			// TODO Auto-generated method stub
//			return 0;
//		}
//    }
//}
