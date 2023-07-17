package com.bbva.enoa.platformservices.coreservice.budgetsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetail;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetailItem;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceSummaryItem;
import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.IRestHandlerProductbudgetsapi;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.*;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


class ProductBudgetsFeignImplTest
{
    @Mock
    private IRestHandlerProductbudgetsapi iRestHandlerProductbudgetsapi;
    @InjectMocks
    private ProductBudgetsFeignImpl productBudgetsFeign;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
        this.productBudgetsFeign.init();
    }

    @Test
    void checkDeploymentPlanOK()
    {
        //Given
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        when(this.iRestHandlerProductbudgetsapi.checkDeploymentPlan(deploymentInfo, 1)).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        //Then
        boolean response = this.productBudgetsFeign.checkDeploymentPlan(deploymentInfo, 1);
        assertTrue(response);
    }

    @Test
    void checkDeploymentPlanKO() throws NovaException
    {
        //Given
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        when(this.iRestHandlerProductbudgetsapi.checkDeploymentPlan(deploymentInfo, 2)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.checkDeploymentPlan(deploymentInfo, 2));
    }

    @Test
    void checkFilesystemOK()
    {
        //Given
        FilesystemInfo filesystemInfo = new FilesystemInfo();
        when(this.iRestHandlerProductbudgetsapi.checkFilesystem(filesystemInfo, 1)).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        //Then
        boolean response = this.productBudgetsFeign.checkFilesystem(filesystemInfo, 1);
        assertTrue(response);
    }

    @Test
    void checkFilesystemKO()
    {
        //Given
        FilesystemInfo filesystemInfo = new FilesystemInfo();
        when(this.iRestHandlerProductbudgetsapi.checkFilesystem(filesystemInfo, 2)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.checkFilesystem(filesystemInfo, 2));
    }

    @Test
    void deleteDeploymentPlanOK()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.deleteDeploymentPlan(1)).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        //Then
        this.productBudgetsFeign.deleteDeploymentPlan(1);
    }

    @Test
    void deleteDeploymentPlanKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.deleteDeploymentPlan(2)).thenReturn(new ResponseEntity<>(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.deleteDeploymentPlan(2));
    }

    @Test
    void deleteProductInfoOK()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.deleteProduct(1)).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        //Then
        this.productBudgetsFeign.deleteProductInfo(1);
    }

    @Test
    void deleteProductInfoKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.deleteProduct(2)).thenReturn(new ResponseEntity<>(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.deleteProductInfo(2));
    }

    @Test
    void getAllFilesystemPacksInfoOK()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        when(this.iRestHandlerProductbudgetsapi.getAllFilesystemPacks()).thenReturn(new ResponseEntity<>(new PackInfo[]{packInfo}, HttpStatus.OK));
        //Then
        this.productBudgetsFeign.getAllFilesystemPacksInfo();

        //Given
        when(this.iRestHandlerProductbudgetsapi.getAllFilesystemPacks()).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getAllFilesystemPacksInfoKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getAllFilesystemPacks()).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getAllFilesystemPacksInfo());
    }

    @Test
    void getAllHardwarePacksInfoOK()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        when(this.iRestHandlerProductbudgetsapi.getAllHardwarePacks()).thenReturn(new ResponseEntity<>(new PackInfo[]{packInfo}, HttpStatus.OK));

        //Then
        this.productBudgetsFeign.getAllHardwarePacksInfo();
    }

    @Test
    void getAllHardwarePacksInfoKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getAllHardwarePacks()).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getAllHardwarePacksInfo());
    }

    @Test
    void getFilesystemPackInfoOK()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        when(this.iRestHandlerProductbudgetsapi.getFilesystemPack(1)).thenReturn(new ResponseEntity<>(packInfo,
                HttpStatus.OK));
        //Then
        this.productBudgetsFeign.getFilesystemPackInfo(1);
    }

    @Test
    void getFilesystemPackInfoKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getFilesystemPack(1)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getFilesystemPackInfo(1));
    }

    @Test
    void getHardwarePackInfoOK()
    {
        //Given
        PackInfo packInfo = new PackInfo();
        when(this.iRestHandlerProductbudgetsapi.getHardwarePack(1)).thenReturn(new ResponseEntity<>(packInfo, HttpStatus.OK));
        //Then
        this.productBudgetsFeign.getHardwarePackInfo(1);
    }

    @Test
    void getHardwarePackInfoKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getHardwarePack(1)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getHardwarePackInfo(1));
    }

    @Test
    void getProductBudgetsOK()
    {
        //Given
        BudgetInfo budgetInfo = new BudgetInfo();
        budgetInfo.setTotalAmount(100.0);
        ProductBudgets productBudgets = new ProductBudgets();
        productBudgets.setHardwareBudget(budgetInfo);
        when(this.iRestHandlerProductbudgetsapi.getProductBudgets(1, "LOCAL")).thenReturn(new ResponseEntity<>(productBudgets, HttpStatus.OK));
        //Then
        ProductBudgets response = this.productBudgetsFeign.getProductBudgets(1, "LOCAL");
        assertEquals(100.0, response.getHardwareBudget().getTotalAmount(), 0.001);
    }

    @Test
    void getProductBudgetsKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getProductBudgets(1, "LOCAL")).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getProductBudgets(1, "LOCAL"));
    }

    @Test
    void getProductServicesDetailOK()
    {
        //Given
        ProductServiceDetailItem productServiceDetailItem = new ProductServiceDetailItem();
        productServiceDetailItem.setStatus("Status");

        when(this.iRestHandlerProductbudgetsapi.getAllProductServicesDetail(1)).thenReturn(new ResponseEntity<>(
                new ProductServiceDetailItem[]{productServiceDetailItem},
                HttpStatus.OK));

        //Then
        List<BUDGServiceDetailItem> response = this.productBudgetsFeign.getProductServicesDetail(1);
        assertEquals("Status", response.get(0).getStatus());
    }

    @Test
    void getProductServicesDetailKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getAllProductServicesDetail(1)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getProductServicesDetail(1));
    }

    @Test
    void getProductServicesSummaryOK()
    {
        //Given
        ProductServiceSummaryItem productServiceSummaryItem = new ProductServiceSummaryItem();
        productServiceSummaryItem.setStatus("Status");

        when(this.iRestHandlerProductbudgetsapi.getAllProductServicesSummary(1)).thenReturn(new ResponseEntity<>(
                new ProductServiceSummaryItem[]{productServiceSummaryItem},
                HttpStatus.OK));

        //Then
        List<BUDGServiceSummaryItem> response = this.productBudgetsFeign.getProductServicesSummary(1);
        assertEquals("Status", response.get(0).getStatus());
    }

    @Test
    void getProductServicesSummaryKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getAllProductServicesSummary(1)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getProductServicesSummary(1));
    }

    @Test
    void getServiceDetailOK()
    {
        //Given
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        ProductServiceDetail productServiceDetail = new ProductServiceDetail();
        productServiceDetail.setStatus("Status");
        productServiceDetail.setConfigurations(new ServiceConfiguration[]{serviceConfiguration});

        when(this.iRestHandlerProductbudgetsapi.getProductServiceDetail(1L)).thenReturn(new ResponseEntity<>(
                productServiceDetail,
                HttpStatus.OK));

        //Then
        BUDGServiceDetail response = this.productBudgetsFeign.getServiceDetail(1L);
        assertEquals("Status", response.getStatus());
    }

    @Test
    void getServiceDetailKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getProductServiceDetail(1L)).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getServiceDetail(1L));
    }

    @Test
    void insertDeploymentPlanOK()
    {
        //Given
        NewDeploymentInfo newDeploymentInfo = new NewDeploymentInfo();
        when(this.iRestHandlerProductbudgetsapi.insertDeploymentPlan(newDeploymentInfo, 1)).thenReturn(new ResponseEntity<>(
                newDeploymentInfo,
                HttpStatus.OK));

        //Then
        this.productBudgetsFeign.insertDeploymentPlan(newDeploymentInfo, 1);
    }

    @Test
    void insertDeploymentPlanKO()
    {
        //Given
        NewDeploymentInfo newDeploymentInfo = new NewDeploymentInfo();
        when(this.iRestHandlerProductbudgetsapi.insertDeploymentPlan(newDeploymentInfo, 1)).thenReturn(new ResponseEntity<>(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.insertDeploymentPlan(newDeploymentInfo, 1));

    }

    @Test
    void updateServiceOK()
    {
        //Given
        UpdatedProductService updatedProductService = new UpdatedProductService();
        when(this.iRestHandlerProductbudgetsapi.updateProductService(updatedProductService, 1L)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //Then
        this.productBudgetsFeign.updateService(updatedProductService, 1L);
    }

    @Test
    void updateServiceKO()
    {
        //Given
        UpdatedProductService updatedProductService = new UpdatedProductService();
        when(this.iRestHandlerProductbudgetsapi.updateProductService(updatedProductService, 1L)).thenReturn(new ResponseEntity<>(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.updateService(updatedProductService, 1L));
    }

    @Test
    void getProductServicesDetail1OK()
    {
        //Given
        ProductServiceDetailItem productServiceDetailItem = new ProductServiceDetailItem();
        productServiceDetailItem.setStatus("Status");

        when(this.iRestHandlerProductbudgetsapi.getProductServices("1", "Status")).thenReturn(new ResponseEntity<>(
                new ProductServiceDetailItem[]{productServiceDetailItem},
                HttpStatus.OK));

        //Then
        ProductServiceDetailItem[] response = this.productBudgetsFeign.getProductServicesDetail("1", "Status");
        assertEquals("Status", response[0].getStatus());
    }

    @Test
    void getProductServicesDetail1KO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getProductServices("1", "Status")).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getProductServicesDetail("1", "Status"));
    }

    @Test
    void getProductsUsedResourcesReportOK()
    {
        //Given
        long[] productsId = {0,1,2,3};
        PBProductsUsedResourcesReportDTO productsUsedResourcesReportDTO = new PBProductsUsedResourcesReportDTO();
        productsUsedResourcesReportDTO.setAvailableHW(1L);
        productsUsedResourcesReportDTO.setAvailableMemory(2L);
        productsUsedResourcesReportDTO.setTotalHW(3L);
        productsUsedResourcesReportDTO.setUsedHW(4L);

        when(this.iRestHandlerProductbudgetsapi.getProductsUsedResourcesReport(productsId, "INT")).thenReturn(new ResponseEntity<>(
                productsUsedResourcesReportDTO,
                HttpStatus.OK));

        //Then
        PBProductsUsedResourcesReportDTO response = this.productBudgetsFeign.getProductsUsedResourcesReport(productsId, "INT");
        assertEquals(1L, response.getAvailableHW());
    }

    @Test
    void getProductsUsedResourcesReportKO()
    {
        //Given
        long[] productsId = {0,1,2,3};
        when(this.iRestHandlerProductbudgetsapi.getProductsUsedResourcesReport(productsId, "INT")).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getProductsUsedResourcesReport(productsId, "INT"));
    }

    @Test
    void getHardwareBudgetHistorySnapshotOK()
    {
        //Given
        PBHardwareBudgetSnapshot pbHardwareBudgetSnapshot = new PBHardwareBudgetSnapshot();
        pbHardwareBudgetSnapshot.setProductId(1);
        pbHardwareBudgetSnapshot.setEnvironment("INT");
        pbHardwareBudgetSnapshot.setUuaa("FLOW");
        pbHardwareBudgetSnapshot.setValue(2.0);
        pbHardwareBudgetSnapshot.setValueType("value");


        when(this.iRestHandlerProductbudgetsapi.getHardwareBudgetHistorySnapshot()).thenReturn(new ResponseEntity<>(
                new PBHardwareBudgetSnapshot[]{pbHardwareBudgetSnapshot},
                HttpStatus.OK));

        //Then
        PBHardwareBudgetSnapshot[] response = this.productBudgetsFeign.getHardwareBudgetHistorySnapshot();
        assertEquals("FLOW", response[0].getUuaa());

        //--------------------------------
        //Given
        when(this.iRestHandlerProductbudgetsapi.getHardwareBudgetHistorySnapshot()).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @Test
    void getHardwareBudgetHistorySnapshotKO()
    {
        //Given
        when(this.iRestHandlerProductbudgetsapi.getHardwareBudgetHistorySnapshot()).thenReturn(new ResponseEntity(new Errors(), HttpStatus.BAD_REQUEST));

        //Then
        assertThrows(NovaException.class, () -> this.productBudgetsFeign.getHardwareBudgetHistorySnapshot());
    }
}
