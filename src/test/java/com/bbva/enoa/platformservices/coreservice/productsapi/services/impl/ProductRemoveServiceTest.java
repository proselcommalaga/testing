//package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;
//
//import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
//import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
//import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
//import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
//import com.bbva.enoa.datamodel.model.common.enumerates.Environment;
//import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
//import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
//import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
//import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
//import com.bbva.enoa.datamodel.model.product.entities.Product;
//import com.bbva.enoa.datamodel.model.release.entities.CPD;
//import com.bbva.enoa.datamodel.model.release.entities.Release;
//import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
//import com.bbva.enoa.datamodel.model.release.enumerates.VersionStatus;
//import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
//import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
//import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
//import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
//import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
//import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
//import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
//import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
//import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILogsClient;
//import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
//import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
//import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.ICategoryService;
//import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
//import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
//import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
//import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.mockito.Spy;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.mockito.Mockito.*;
//
//public class ProductRemoveServiceTest
//{
//
//    @Mock
//    private ProductRepository productRepository;
//    @Mock
//    private DeploymentTaskRepository deploymentTaskRepository;
//    @Mock
//    private DeploymentPlanRepository deploymentPlanRepository;
//    @Mock
//    private ToDoTaskRepository toDoTaskRepository;
//    @Mock
//    private FilesystemRepository filesystemsRepository;
//    @Mock
//    private IBudgetsService budgetsService;
//    @Mock
//    private IDocSystemService docSystemService;
//    @Mock
//    private MailServiceClient mailService;
//    @Mock
//    private IQualityManagerService qaService;
//    @Mock
//    private ICategoryService categoryService;
//    @Mock
//    private IProductUsersClient usersClient;
//    @Mock
//    private ToolsClient toolsService;
//    @Mock
//    private INovaActivityEmitter novaActivityEmitter;
//    @Mock
//    private SyncApiRepository syncApiRepository;
//    @Mock
//    private ILogsClient logsClient;
//
//    @Spy
//    @InjectMocks
//    private ProductRemoveService productRemoveService;
//
//    @BeforeEach
//    public void setUp() throws Exception
//    {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    private Product generateProduct()
//    {
//        Product productToReturn = new Product();
//        productToReturn.setName("NAME");
//        productToReturn.setId(1);
//        productToReturn.setCPDInPro(this.generateCpd());
//        productToReturn.setCriticalityLevel(0);
//        productToReturn.setImage("IMAGE");
//
//        return productToReturn;
//    }
//
//    private Product generateCompleteProduct()
//    {
//        // Product generated with release, release version and plan related
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan();
//        deploymentPlan.setId(1);
//        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
//        List<DeploymentPlan> dpList = new ArrayList<>();
//        dpList.add(deploymentPlan);
//
//        ReleaseVersion releaseVersion = new ReleaseVersion();
//        releaseVersion.setId(2);
//        releaseVersion.setVersionName("VersionNameTest");
//        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
//        releaseVersion.setDeployments(dpList);
//        List<ReleaseVersion> rvList = new ArrayList<>();
//        rvList.add(releaseVersion);
//
//        Release release = new Release();
//        release.setId(3);
//        release.setName("ReleaseName");
//        release.setReleaseVersions(rvList);
//        List<Release> rList = new ArrayList<>();
//        rList.add(release);
//
//        Product productToReturn = new Product();
//        productToReturn.setName("NAME");
//        productToReturn.setId(1);
//        productToReturn.setCPDInPro(this.generateCpd());
//        productToReturn.setCriticalityLevel(0);
//        productToReturn.setImage("IMAGE");
//        productToReturn.setReleases(rList);
//        release.setProduct(productToReturn);
//
//
//        return productToReturn;
//    }
//
//    private CPD generateCpd()
//    {
//
//        CPD cpd = new CPD();
//        cpd.setId(1);
//        cpd.setActive(true);
//        cpd.setAddress("ADRESS");
//        cpd.setElasticSearchCPDName("CPDNAME");
//        cpd.setEnvironment(Environment.PRO);
//        cpd.setFilesystem("FILESYSTEM");
//        cpd.setLabel("LABEL");
//        cpd.setName("NAME");
//        cpd.setRegistry("REGISTRY");
//
//
//        return cpd;
//    }
//
//    private List<SyncApi> generateNovaApi()
//    {
//
//        SyncApi syncApi = new SyncApi();
//        syncApi.setId(1);
//        syncApi.setName("APINAME");
//        syncApi.setType(ApiType.NOT_GOVERNED);
//        syncApi.setProduct(this.generateProduct());
//
//        List<SyncApi> novaApiList = new ArrayList<>();
//        novaApiList.add(syncApi);
//
//        return novaApiList;
//    }
//
//    private List<USUserDTO> generateUSUserDTOList()
//    {
//        List<USUserDTO> usUserDTOList = new ArrayList<>();
//        USUserDTO usUserDTO1 = new USUserDTO();
//        usUserDTO1.setUserCode("USERCODE1");
//        usUserDTO1.setTeams(new String[]{"TEAM1"});
//        usUserDTO1.setEmail("email1@bbva.com");
//        usUserDTO1.setActive(false);
//        usUserDTO1.setUserName("NAME1");
//        usUserDTO1.setSurname1("SURNAME1");
//        usUserDTO1.setSurname2("SURNAME21");
//
//        usUserDTOList.add(usUserDTO1);
//
//        USUserDTO usUserDTO2 = new USUserDTO();
//        usUserDTO2.setUserCode("USERCODE2");
//        usUserDTO2.setTeams(new String[]{"TEAM1"});
//        usUserDTO2.setEmail("email2@bbva.com");
//        usUserDTO2.setActive(false);
//        usUserDTO2.setUserName("NAME2");
//        usUserDTO2.setSurname1("SURNAME2");
//        usUserDTO2.setSurname2("SURNAME22");
//
//        usUserDTOList.add(usUserDTO2);
//
//        USUserDTO usUserDTO3 = new USUserDTO();
//        usUserDTO3.setUserCode("USERCODE3");
//        usUserDTO3.setTeams(new String[]{"TEAM2"});
//        usUserDTO3.setEmail("email3@bbva.com");
//        usUserDTO3.setActive(false);
//        usUserDTO3.setUserName("NAME3");
//        usUserDTO3.setSurname1("SURNAME3");
//        usUserDTO3.setSurname2("SURNAME23");
//
//        usUserDTOList.add(usUserDTO3);
//
//        return usUserDTOList;
//    }
//
//    private List<Filesystem> generateFilesystemList()
//    {
//        Filesystem f = new Filesystem()
//        {
//            @Override
//            public FilesystemType getType()
//            {
//                return FilesystemType.FILESYSTEM;
//            }
//        };
//        f.setName("FS1");
//        f.setProduct(this.generateProduct());
//        f.setEnvironment(Environment.PRO);
//        f.setFilesystemStatus(FilesystemStatus.CREATED);
//
//        List<Filesystem> filesystemList = new ArrayList<>();
//        filesystemList.add(f);
//
//        return filesystemList;
//    }
//
//    private List<DeploymentTask> generateDepploymentTask()
//    {
//        //Partial filled
//        DeploymentTask dptask = new DeploymentTask();
//        dptask.setId(1);
//        dptask.setProduct(this.generateProduct());
//        dptask.setStatus(ToDoTaskStatus.DONE);
//        dptask.setTaskType(ToDoTaskType.INTERNAL_ERROR);
//
//        List<DeploymentTask> dpTaskList = new ArrayList<>();
//        dpTaskList.add(dptask);
//
//        return dpTaskList;
//    }
//
//    private List<ToDoTask> generateToDoTaskList()
//    {
//        //Partial filled
//        ToDoTask toDoTask = new ToDoTask();
//        toDoTask.setId(1);
//        toDoTask.setProduct(this.generateProduct());
//        toDoTask.setStatus(ToDoTaskStatus.DONE);
//        toDoTask.setTaskType(ToDoTaskType.INTERNAL_ERROR);
//
//        List<ToDoTask> toDoTaskList = new ArrayList<>();
//        toDoTaskList.add(toDoTask);
//
//        return toDoTaskList;
//    }
//
//
//    @Test
//    public void removeProductTest()
//    {
//
//        //Used objects
//        Product validProduct = this.generateProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//
//        //BBDD queries
//        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(validProduct));
//        when(this.syncApiRepository.findAllByProductId(anyInt())).thenReturn(Collections.emptyList());
//
//        //Void methods
//        doNothing().when(this.logsClient).deleteLogRateThresholdEvents(anyInt());
//        doNothing().when(this.usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
//        doNothing().when(this.toolsService).removeUserTool(any(TOProductUserDTO.class));
//        doNothing().when(this.toolsService).removeExternalToolsFromProduct(anyInt());
//        doNothing().when(this.categoryService).removeEmptyCategories();
//        doNothing().when(this.productRemoveService).sendRemoveProductNotification(any(Product.class), any(List.class));
//        doNothing().when(this.novaActivityEmitter).emitNewActivity(any(GenericActivity.class));
//
//        //Non void methods
//        doReturn(usUserDTOList).when(this.usersClient).getProductMembers(anyInt(), any(NovaException.class));
//        doReturn(usUserDTOList).when(this.productRemoveService).removeProductFromBBDD(anyString(), any(Product.class));
//
//        //Call
//        this.productRemoveService.removeProduct("CODE", validProduct.getId());
//
//        //Verifying all the methods are called, with the correct parameter
//        verify(this.productRepository, times(1)).findById(validProduct.getId());
//        verify(this.usersClient, times(1)).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));
//        verify(this.syncApiRepository, times(1)).findAllByProductId(validProduct.getId());
//        verify(this.logsClient, times(1)).deleteLogRateThresholdEvents(validProduct.getId());
//        verify(this.usersClient, times(1)).getProductMembers(anyInt(), any(NovaException.class));
//        verify(this.productRemoveService, times(1)).removeProductFromBBDD("CODE", validProduct);
//        verify(this.categoryService, times(1)).removeEmptyCategories();
//        verify(this.productRemoveService, times(1)).sendRemoveProductNotification(validProduct, usUserDTOList);
//        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(GenericActivity.class));
//
//
//    }
//
//    @Test
//    public void removeProductNoProductTest()
//    {
//
//        //Used objects
//        Product validProduct = this.generateProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//
//        //BBDD queries
//        when(this.productRepository.findById(anyInt())).thenReturn(Optional.empty());
//        when(this.syncApiRepository.findAllByProductId(anyInt())).thenReturn(Collections.emptyList());
//
//        //Call
//        Assertions.assertThrows(NovaException.class, () -> this.productRemoveService.removeProduct("CODE", validProduct.getId()));
//
//
//    }
//
//    @Test
//    public void removeProductApisNotDeletedTest()
//    {
//
//        //Used objects
//        Product validProduct = this.generateProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//
//        //BBDD queries
//        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(validProduct));
//        when(this.syncApiRepository.findAllByProductId(anyInt())).thenReturn(this.generateNovaApi());
//
//        //Call
//        Assertions.assertThrows(NovaException.class, () -> this.productRemoveService.removeProduct("CODE", validProduct.getId()));
//
//
//    }
//
//    @Test
//    public void removeProductFilesystemsNoDeletedTest()
//    {
//
//        //Used objects
//        Product validProduct = this.generateProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//        List<Filesystem> filesystemList = this.generateFilesystemList();
//
//        //BBDD queries
//        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(validProduct));
//        when(this.filesystemsRepository.findByProductId(validProduct.getId())).thenReturn(filesystemList);
//
//        //Call
//        Assertions.assertThrows(NovaException.class, () -> this.productRemoveService.removeProduct("CODE", validProduct.getId()));
//
//
//    }
//
//    @Test
//    public void removeProductRelasesNoDeletedTest()
//    {
//
//        //Used objects
//        Product validProductComplete = this.generateCompleteProduct();
//
//        //BBDD queries
//        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(validProductComplete));
//
//        //Call
//        Assertions.assertThrows(NovaException.class, () -> this.productRemoveService.removeProduct("CODE", validProductComplete.getId()));
//
//
//    }
//
//    @Test
//    public void removeProductRelasesNoDeletedNoDeployedTest()
//    {
//
//        //Used objects
//        Product validProductComplete = this.generateCompleteProduct();
//
//        //Avoiding DEPLOY check, to check second one reusing the object
//        validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0).setStatus(DeploymentStatus.UNDEPLOYED);
//
//        //BBDD queries
//        when(this.productRepository.findById(anyInt())).thenReturn(Optional.of(validProductComplete));
//        when(this.deploymentTaskRepository.countPendingAll(anyInt())).thenReturn(2L);
//
//        //Call
//        Assertions.assertThrows(NovaException.class, () -> this.productRemoveService.removeProduct("CODE", validProductComplete.getId()));
//
//
//    }
//
//    @Test
//    public void sendRemoveProductNotificationTest()
//    {
//
//        //Used objects
//        Product validProduct = this.generateProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//
//        doNothing().when(this.mailService).sendDeleteProductNotification(anyString(), any(List.class));
//        this.productRemoveService.sendRemoveProductNotification(validProduct, usUserDTOList);
//        verify(this.mailService, times(1)).sendDeleteProductNotification(anyString(), any(List.class));
//    }
//
//    @Test
//    public void removeProductFromBBDDErrorTest()
//    {
//
//        //Used objects
//        Product validProductComplete = this.generateCompleteProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//        List<DeploymentTask> deploymentTaskList = this.generateDepploymentTask();
//        List<ToDoTask> toDoTaskList = this.generateToDoTaskList();
//
//        when(this.usersClient.getProductMembers(anyInt(), any(NovaException.class))).thenReturn(usUserDTOList);
//        when(this.deploymentPlanRepository.save(any(DeploymentPlan.class))).thenReturn(validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0));
//        when(this.deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(deploymentTaskList);
//        when(this.toDoTaskRepository.findByProductId(anyInt())).thenReturn(toDoTaskList);
//
//        doNothing().when(this.usersClient).deleteProductUsers(anyString(), anyInt());
//        doNothing().when(this.qaService).removeQualityInfo(any(ReleaseVersion.class));
//        doNothing().when(this.deploymentTaskRepository).delete(any(DeploymentTask.class));
//        doNothing().when(this.toDoTaskRepository).delete(any(ToDoTask.class));
//        doNothing().when(this.docSystemService).removeDefaultRepository(any(Product.class));
//        doNothing().when(this.productRepository).delete(any(Product.class));
//        doNothing().when(this.budgetsService).synchronizeProductDeletion(anyInt());
//
//        this.productRemoveService.removeProductFromBBDD("CODE", validProductComplete);
//
//
//        verify(this.usersClient, times(1)).deleteProductUsers("CODE", validProductComplete.getId());
//        verify(this.qaService, times(1)).removeQualityInfo(validProductComplete.getReleases().get(0).getReleaseVersions().get(0));
//        verify(this.deploymentPlanRepository, times(1)).save(validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0));
//        verify(this.deploymentTaskRepository, times(1)).findByDeploymentPlanId(validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0).getId());
//        verify(this.deploymentTaskRepository, times(1)).delete(any(DeploymentTask.class));
//        verify(this.toDoTaskRepository, times(1)).findByProductId(validProductComplete.getId());
//        verify(this.toDoTaskRepository, times(1)).delete(any(ToDoTask.class));
//        verify(this.docSystemService, times(1)).removeDefaultRepository(validProductComplete);
//        verify(this.productRepository, times(1)).delete(validProductComplete);
//        verify(this.budgetsService, times(1)).synchronizeProductDeletion(validProductComplete.getId());
//    }
//
//    @Test
//    public void removeProductFromBBDDTest()
//    {
//
//        //Used objects
//        Product validProductComplete = this.generateCompleteProduct();
//        List<USUserDTO> usUserDTOList = this.generateUSUserDTOList();
//        List<DeploymentTask> deploymentTaskList = this.generateDepploymentTask();
//        List<ToDoTask> toDoTaskList = this.generateToDoTaskList();
//
//        when(this.usersClient.getProductMembers(anyInt(), any(NovaException.class))).thenReturn(usUserDTOList);
//        when(this.deploymentPlanRepository.save(any(DeploymentPlan.class))).thenReturn(validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0));
//        when(this.deploymentTaskRepository.findByDeploymentPlanId(anyInt())).thenReturn(deploymentTaskList);
//        when(this.toDoTaskRepository.findByProductId(anyInt())).thenReturn(toDoTaskList);
//
//        doNothing().when(this.usersClient).deleteProductUsers(anyString(), anyInt());
//        doNothing().when(this.qaService).removeQualityInfo(any(ReleaseVersion.class));
//        doNothing().when(this.deploymentTaskRepository).delete(any(DeploymentTask.class));
//        doNothing().when(this.toDoTaskRepository).delete(any(ToDoTask.class));
//        doNothing().when(this.docSystemService).removeDefaultRepository(any(Product.class));
//        doNothing().when(this.productRepository).delete(any(Product.class));
//
//        //Forcing catch error
//        doThrow(NovaException.class).when(this.budgetsService).synchronizeProductDeletion(anyInt());
//
//        this.productRemoveService.removeProductFromBBDD("CODE", validProductComplete);
//
//        verify(this.usersClient, times(1)).deleteProductUsers("CODE", validProductComplete.getId());
//        verify(this.qaService, times(1)).removeQualityInfo(validProductComplete.getReleases().get(0).getReleaseVersions().get(0));
//        verify(this.deploymentPlanRepository, times(1)).save(validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0));
//        verify(this.deploymentTaskRepository, times(1)).findByDeploymentPlanId(validProductComplete.getReleases().get(0).getReleaseVersions().get(0).getDeployments().get(0).getId());
//        verify(this.deploymentTaskRepository, times(1)).delete(any(DeploymentTask.class));
//        verify(this.toDoTaskRepository, times(1)).findByProductId(validProductComplete.getId());
//        verify(this.toDoTaskRepository, times(1)).delete(any(ToDoTask.class));
//        verify(this.docSystemService, times(1)).removeDefaultRepository(validProductComplete);
//        verify(this.productRepository, times(1)).delete(validProductComplete);
//        verify(this.budgetsService, times(1)).synchronizeProductDeletion(validProductComplete.getId());
//
//
//    }
//
//
//}
