package com.bbva.enoa.platformservices.coreservice.budgetsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGProductBudgetsDTO;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceSummaryItem;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGUpdatedService;
import com.bbva.enoa.apirestgen.budgetsapi.model.DateObject;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.BudgetInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.DeploymentInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.FilesystemInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.NewDeploymentInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PackInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductBudgets;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.UpdatedProductService;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.GBType;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions.BudgetsError;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



class BudgetsServiceImplTest
{
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IProductBudgetsService productBudgetsService;
    @Mock
    private IProductUsersClient usersClient;
    @InjectMocks
    private BudgetsServiceImpl budgetsServiceImpl;
    @Mock
    private IToolsClient toolsService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void checkDeploymentPlanDeployabilityStatusWithApiRest()
    {
        checkDeploymentPlanDeployabilityStatus(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void checkDeploymentPlanDeployabilityStatusWithApi()
    {
        checkDeploymentPlanDeployabilityStatus(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void checkDeploymentPlanDeployabilityStatus(ServiceType serviceType)
    {
        //Given
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceType(serviceType.getServiceType());
        HardwarePack hardwarePack = new HardwarePack();
        hardwarePack.setId(1);
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setHardwarePack(hardwarePack);
        deploymentService.setService(releaseVersionService);
        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        deploymentServiceList.add(deploymentService);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        productSubsystem.setSubsystemId(1);
        productSubsystem.setSubsystemType(SubsystemType.NOVA.getType());
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(productSubsystem.getSubsystemId());
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentServices(deploymentServiceList);
        deploymentSubsystem.setSubsystem(releaseVersionSubsystem);
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();
        deploymentSubsystemList.add(deploymentSubsystem);
        DeploymentGcsp deploymentGcsp = new DeploymentGcsp();
        deploymentGcsp.setUndeployRelease(0);
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setReleaseVersion(releaseVersion);
        deploymentPlan.setEnvironment(Environment.LOCAL.getEnvironment());
        deploymentPlan.setDeploymentSubsystems(deploymentSubsystemList);
        deploymentPlan.setGcsp(deploymentGcsp);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(deploymentPlan));
        when(this.productBudgetsService.checkDeploymentPlan(any(DeploymentInfo.class), anyInt())).thenReturn(true);

        //Then
        when(this.toolsService.getSubsystemById(productSubsystem.getSubsystemId())).thenReturn(productSubsystem);

        //Then
        when(this.budgetsServiceImpl.checkDeploymentPlanDeployabilityStatus(deploymentPlan.getId())).thenReturn(true);
        when(this.budgetsServiceImpl.checkDeploymentPlanDeployabilityStatus(deploymentPlan.getId())).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));
    }

    @Test
    void checkFilesystemAvailabilityStatus()
    {
        //Then
        this.budgetsServiceImpl.checkFilesystemAvailabilityStatus(1, "LOCAL", 1);
        verify(this.productBudgetsService, times(1)).checkFilesystem(any(FilesystemInfo.class), anyInt());
    }

    @Test
    void checkProductBudget()
    {
        //Given
        BUDGServiceSummaryItem item = new BUDGServiceSummaryItem();
        List<BUDGServiceSummaryItem> items = new ArrayList<>();
        items.add(item);
        when(this.productBudgetsService.getProductServicesSummary(1)).thenReturn(items);

        //Then
        boolean response = this.budgetsServiceImpl.checkProductBudget(1);
        assertTrue(response);

        //Given
        when(this.productBudgetsService.getProductServicesSummary(2)).thenReturn(new ArrayList<>());

        //Then
        boolean response2 = this.budgetsServiceImpl.checkProductBudget(2);
        assertFalse(response2);
    }

    @Test
    void checkProductServices()
    {
        //Given
        BUDGServiceSummaryItem serviceSummaryItem = new BUDGServiceSummaryItem();
        serviceSummaryItem.setNovaServiceType("FILESYSTEM");
        List<BUDGServiceSummaryItem> serviceSummaryItemList = new ArrayList<>();
        serviceSummaryItemList.add(serviceSummaryItem);
        when(this.productBudgetsService.getProductServicesSummary(1)).thenReturn(serviceSummaryItemList);

        //Then
        boolean response = this.budgetsServiceImpl.checkProductServices(1, GBType.FILESYSTEM);
        assertTrue(response);

        //Then
        boolean response2 = this.budgetsServiceImpl.checkProductServices(1, GBType.HARDWARE);
        assertFalse(response2);
    }

    @Test
    void getFilesystemPackPrice()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        packInfo.setPackPrice(100.0);
        when(this.productBudgetsService.getFilesystemPackInfo(1)).thenReturn(packInfo);

        //Then
        double response = this.budgetsServiceImpl.getFilesystemPackPrice(1);
        assertEquals(100.0, response, 0.1);
    }

    @Test
    void getFilesystemPacks()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        packInfo.setPackPrice(100.0);
        packInfo.setPackId(1);
        List<PackInfo> packInfoList = new ArrayList<>();
        packInfoList.add(packInfo);
        when(this.productBudgetsService.getAllFilesystemPacksInfo()).thenReturn(packInfoList);

        //Then
        List<PackInfo> response = this.budgetsServiceImpl.getFilesystemPacks();
        assertEquals(1, response.get(0).getPackId().intValue());
    }

    @Test
    void getHardwarePackPrice()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        packInfo.setPackPrice(100.0);
        when(this.productBudgetsService.getHardwarePackInfo(1)).thenReturn(packInfo);

        //Then
        double response = this.budgetsServiceImpl.getHardwarePackPrice(1);
        assertEquals(100.0, response, 0.1);
    }

    @Test
    void getHardwarePacks()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        packInfo.setPackPrice(100.0);
        packInfo.setPackId(1);
        List<PackInfo> packInfoList = new ArrayList<>();
        packInfoList.add(packInfo);
        when(this.productBudgetsService.getAllHardwarePacksInfo()).thenReturn(packInfoList);

        //Then
        List<PackInfo> response = this.budgetsServiceImpl.getHardwarePacks();
        assertEquals(1, response.get(0).getPackId().intValue());
    }

    @Test
    void getProductBudgets()
    {
        //Given
        BudgetInfo budgetInfo = new BudgetInfo();
        budgetInfo.setAvailableAmount(100.0);
        budgetInfo.setTotalAmount(100.0);
        ProductBudgets budgetsInfo = new ProductBudgets();
        budgetsInfo.setEphoenixBudget(budgetInfo);
        budgetsInfo.setFilesystemBudget(budgetInfo);
        budgetsInfo.setHardwareBudget(budgetInfo);
        budgetsInfo.setBrokerBudget(budgetInfo);
        when(this.productBudgetsService.getProductBudgets(1, "LOCAL")).thenReturn(budgetsInfo);

        //Then
        BUDGProductBudgetsDTO response = this.budgetsServiceImpl.getProductBudgets(1, "LOCAL");
        assertEquals(100.0, response.getEphoenixBudget().getAvailableAmount(), 0.001);
    }

    @Test
    void getProductServicesDetail()
    {
        //Then
        this.budgetsServiceImpl.getProductServicesDetail(1);
        verify(this.productBudgetsService, times(1)).getProductServicesDetail(1);
    }

    @Test
    void getProductServicesSummary()
    {
        //Then
        this.budgetsServiceImpl.getProductServicesSummary(1);
        verify(this.productBudgetsService, times(1)).getProductServicesSummary(1);
    }

    @Test
    void getServiceDetail()
    {
        //Then
        this.budgetsServiceImpl.getServiceDetail(1);
        verify(this.productBudgetsService, times(1)).getServiceDetail(1);
    }

    @Test
    void synchronizePlanDeploymentWithApiRest()
    {
        synchronizePlanDeployment(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void synchronizePlanDeploymentWithApi()
    {
        synchronizePlanDeployment(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void synchronizePlanDeployment(ServiceType serviceType)
    {
        //Given
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        HardwarePack hardwarePack = new HardwarePack();
        hardwarePack.setId(1);
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setHardwarePack(hardwarePack);

        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceType(serviceType.getServiceType());
        deploymentService.setService(releaseVersionService);

        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        deploymentServiceList.add(deploymentService);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        productSubsystem.setSubsystemType(SubsystemType.NOVA.getType());
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(1);
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentServices(deploymentServiceList);
        deploymentSubsystem.setSubsystem(releaseVersionSubsystem);
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();
        deploymentSubsystemList.add(deploymentSubsystem);
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setReleaseVersion(releaseVersion);
        deploymentPlan.setEnvironment(Environment.LOCAL.getEnvironment());
        deploymentPlan.setDeploymentSubsystems(deploymentSubsystemList);
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(deploymentPlan));

        //Then
        when(this.toolsService.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(productSubsystem);

        //Then
        this.budgetsServiceImpl.synchronizePlanDeployment(1);
        verify(this.productBudgetsService, times(1)).insertDeploymentPlan(any(NewDeploymentInfo.class), anyInt());
    }

    @Test
    void synchronizePlanUndeployment()
    {
        //Then
        this.budgetsServiceImpl.synchronizePlanUndeployment(1);
        verify(this.productBudgetsService, times(1)).deleteDeploymentPlan(1);
    }

    @Test
    void synchronizeProductDeletion()
    {
        //Then
        this.budgetsServiceImpl.synchronizeProductDeletion(1);
        verify(this.productBudgetsService, times(1)).deleteProductInfo(1);
    }

    @Test
    void updateService()
    {
        //Given
        BUDGUpdatedService updatedService = new BUDGUpdatedService();
        NovaException PERMISSION_DENIED = new NovaException(BudgetsError.getForbiddenError(), "Permission Error");

        //Then
        doNothing().when(this.usersClient).checkHasPermission("IMM0589", "EDIT_BUDGET", PERMISSION_DENIED);

        this.budgetsServiceImpl.updateService(updatedService, 1, "IMM0589");

        UpdatedProductService updatedProductService = new UpdatedProductService();
        updatedProductService.setNewStartDate(updatedService.getUpdatedStartDate());

        verify(this.productBudgetsService, times(1)).updateService(updatedProductService, 1);
    }

    @Test
    void calculateFinalDate()
    {
        //Given
        DateObject startDate = new DateObject();
        startDate.setDateValue("2018-03-12T23:00:00.000Z");
        startDate.setDuration(24L);

        //Then
        DateObject response = this.budgetsServiceImpl.calculateFinalDate(startDate);
        assertEquals("2018-03-15T23:00:00Z", response.getDateValue());
    }

    @Test
    void getProductsUsedResourcesReport()
    {
        long[] productIds = {1L,2L,3L,4L,5L,6L};
        //Then
        this.budgetsServiceImpl.getProductsUsedResourcesReport(productIds, "INT");
        verify(this.productBudgetsService, times(1)).getProductsUsedResourcesReport(productIds, "INT");
    }

    @Test
    void getDeploymentInfoFromPlanWithApiRest()
    {
        getDeploymentInfoFromPlan(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void getDeploymentInfoFromPlanWithApi()
    {
        getDeploymentInfoFromPlan(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void getDeploymentInfoFromPlan(ServiceType serviceType)
    {
        //Given
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceType(serviceType.getServiceType());
        HardwarePack hardwarePack = new HardwarePack();
        hardwarePack.setId(1);
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setHardwarePack(hardwarePack);
        deploymentService.setService(releaseVersionService);
        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        deploymentServiceList.add(deploymentService);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        productSubsystem.setSubsystemType(SubsystemType.NOVA.getType());
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(1);
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentServices(deploymentServiceList);
        deploymentSubsystem.setSubsystem(releaseVersionSubsystem);
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();
        deploymentSubsystemList.add(deploymentSubsystem);
        DeploymentGcsp deploymentGcsp = new DeploymentGcsp();
        deploymentGcsp.setUndeployRelease(0);
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setReleaseVersion(releaseVersion);
        deploymentPlan.setEnvironment(Environment.LOCAL.getEnvironment());
        deploymentPlan.setDeploymentSubsystems(deploymentSubsystemList);
        deploymentPlan.setGcsp(deploymentGcsp);

        //Then
        when(this.toolsService.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(productSubsystem);

        //Then
        DeploymentInfo response = this.budgetsServiceImpl.getDeploymentInfoFromPlan(deploymentPlan);
        assertNotNull(response);
        assertEquals("LOCAL", response.getEnvironment());
        assertEquals(1, response.getHardwarePacks()[0].getPackId().intValue());
    }

    @Test
    void getNewDeploymentInfoFromPlanWithApiRest()
    {
        getNewDeploymentInfoFromPlan(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void getNewDeploymentInfoFromPlanWithApi()
    {
        getNewDeploymentInfoFromPlan(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void getNewDeploymentInfoFromPlan(ServiceType serviceType)
    {
        //Given
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        HardwarePack hardwarePack = new HardwarePack();
        hardwarePack.setId(1);
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setHardwarePack(hardwarePack);

        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceType(serviceType.getServiceType());
        deploymentService.setService(releaseVersionService);

        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        deploymentServiceList.add(deploymentService);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        productSubsystem.setSubsystemType(SubsystemType.NOVA.getType());
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(1);
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentServices(deploymentServiceList);
        deploymentSubsystem.setSubsystem(releaseVersionSubsystem);
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();
        deploymentSubsystemList.add(deploymentSubsystem);
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setReleaseVersion(releaseVersion);
        deploymentPlan.setEnvironment(Environment.LOCAL.getEnvironment());
        deploymentPlan.setDeploymentSubsystems(deploymentSubsystemList);

        //Then
        when(this.toolsService.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(productSubsystem);

        //Then
        NewDeploymentInfo response = this.budgetsServiceImpl.getNewDeploymentInfoFromPlan(deploymentPlan);
        assertEquals("LOCAL", response.getEnvironment());
        assertEquals(1, response.getHardwarePacks()[0].getPackId().intValue());
    }

    @Test
    void checkProductServicesPending()
    {
        //Given
        BUDGServiceSummaryItem serviceSummaryItem = new BUDGServiceSummaryItem();
        serviceSummaryItem.setNovaServiceType("FILESYSTEM");
        serviceSummaryItem.setPending(true);
        List<BUDGServiceSummaryItem> serviceSummaryItemList = new ArrayList<>();
        serviceSummaryItemList.add(serviceSummaryItem);
        when(this.productBudgetsService.getProductServicesSummary(1)).thenReturn(serviceSummaryItemList);

        //Then
        boolean response = this.budgetsServiceImpl.checkProductServices(1, GBType.FILESYSTEM, true);
        assertTrue(response);

        //Then
        boolean response2 = this.budgetsServiceImpl.checkProductServices(1, GBType.HARDWARE, true);
        assertFalse(response2);
    }

    @Test
    void getDeploymentInfoFromPlanWithUndeployWithApiRest()
    {
        getDeploymentInfoFromPlanWithUndeploy(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    void getDeploymentInfoFromPlanWithUndeployWithApi()
    {
        getDeploymentInfoFromPlanWithUndeploy(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void getDeploymentInfoFromPlanWithUndeploy(ServiceType serviceType)
    {
        //Given
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceType(serviceType.getServiceType());
        HardwarePack hardwarePack = new HardwarePack();
        hardwarePack.setId(1);
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setHardwarePack(hardwarePack);
        deploymentService.setService(releaseVersionService);
        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        deploymentServiceList.add(deploymentService);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        productSubsystem.setSubsystemType(SubsystemType.NOVA.getType());
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setSubsystemId(1);
        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentServices(deploymentServiceList);
        deploymentSubsystem.setSubsystem(releaseVersionSubsystem);
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();
        deploymentSubsystemList.add(deploymentSubsystem);
        DeploymentGcsp deploymentGcsp = new DeploymentGcsp();
        deploymentGcsp.setUndeployRelease(1);
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setReleaseVersion(releaseVersion);
        deploymentPlan.setEnvironment(Environment.LOCAL.getEnvironment());
        deploymentPlan.setDeploymentSubsystems(deploymentSubsystemList);
        deploymentPlan.setGcsp(deploymentGcsp);

        //Then
        when(this.toolsService.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(productSubsystem);

        // Return Deploymentplan
        DeploymentPlan undeployPlan = new DeploymentPlan();
        undeployPlan.setReleaseVersion(releaseVersion);

        when(this.deploymentPlanRepository.findById(deploymentPlan.getGcsp().getUndeployRelease())).thenReturn(Optional.of(undeployPlan));

        //Then
        DeploymentInfo response = this.budgetsServiceImpl.getDeploymentInfoFromPlan(deploymentPlan);
        assertNotNull(response);
        assertEquals("LOCAL", response.getEnvironment());
        assertEquals(1, response.getHardwarePacks()[0].getPackId().intValue());
    }
}
