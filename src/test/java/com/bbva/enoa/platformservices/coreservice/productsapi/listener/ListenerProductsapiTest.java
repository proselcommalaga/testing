package com.bbva.enoa.platformservices.coreservice.productsapi.listener;

import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBProductsUsedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductCommonConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductRequestDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.*;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils.FORMAT_CSV;
import static org.mockito.Mockito.*;


public class ListenerProductsapiTest
{
    private NovaMetadata metadata = new NovaMetadata();

    @Mock
    private ICategoryService categoryService;
    @Mock
    private IProductsAPIService productsAPIService;
    @Mock
    private IProductRemoveService productRemoveService;
    @Mock
    private INewProductRequestService newProductRequestService;
    @Mock
    private IUpdateProductService updateProductService;
    @Mock
    private IProductReportService productReportService;

    @InjectMocks
    private ListenerProductsapi listenerProductsapi;

    @BeforeEach
    public void setUp() throws Exception
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList("CODE"));
        this.metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));

        MockitoAnnotations.initMocks(this);
    }

    private PBProductsUsedResourcesReportDTO generateNewPBProductsUsedResourcesReportDTO()
    {
        PBProductsUsedResourcesReportDTO pbProductsUsedResourcesReportDTO = new PBProductsUsedResourcesReportDTO();

        pbProductsUsedResourcesReportDTO.setTotalHW(2048L);
        pbProductsUsedResourcesReportDTO.setAvailableHW(1024L);
        pbProductsUsedResourcesReportDTO.setUsedHW(1024L);
        pbProductsUsedResourcesReportDTO.setAvailableMemory(512L);

        return pbProductsUsedResourcesReportDTO;
    }

    private ProductRequestDTO generateProductRequestDTO()
    {
        ProductRequestDTO productRequestDTO = new ProductRequestDTO();
        productRequestDTO.fillRandomly(1, false, 0, 1);
        return productRequestDTO;
    }

    private ProductSummaryDTO generateProductSummaryDTO()
    {
        ProductSummaryDTO productSummaryDTO = new ProductSummaryDTO();
        productSummaryDTO.fillRandomly(1, false, 0, 1);
        return productSummaryDTO;
    }

    private ProductConfigurationDTO generateProductconfigurationDTO()
    {
        ProductConfigurationDTO productConfigurationDTO = new ProductConfigurationDTO();
        productConfigurationDTO.fillRandomly(1, false, 0, 1);
        return productConfigurationDTO;
    }

    private ProductCommonConfigurationDTO generateProductCommonConfigurationDTO()
    {
        ProductCommonConfigurationDTO productCommonConfigurationDTO = new ProductCommonConfigurationDTO();
        productCommonConfigurationDTO.fillRandomly(1, false, 0, 1);
        return productCommonConfigurationDTO;
    }

    @Test
    void getProductsUsedResourcesReportTest() throws Errors
    {
        this.listenerProductsapi.getProductsUsedResourcesReport(metadata, "PRO", "CODE");
        verify(this.productReportService, times(1)).getProductsUsedResourcesReport("PRO", "CODE");
    }

    @Test
    void createProductTest() throws Errors
    {
        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        this.listenerProductsapi.createProduct(metadata, productRequestDTO);
        verify(this.productsAPIService, times(1)).createNewProduct(productRequestDTO);
    }

    @Test
    void getProductsFilesytemsUsageReportTest() throws Errors
    {

        this.listenerProductsapi.getProductsFilesytemsUsageReport(metadata, "PRO", "CODE");
        verify(this.productReportService, times(1)).getProductsFilesytemsUsageReport("PRO", "CODE");

    }

    @Test
    void updateCommonConfigurationTest() throws Errors
    {
        ProductConfigurationDTO productConfigurationDTO = this.generateProductconfigurationDTO();
        ProductCommonConfigurationDTO productCommonConfigurationDTO = this.generateProductCommonConfigurationDTO();
        this.listenerProductsapi.updateCommonConfiguration(metadata, productCommonConfigurationDTO, 1);
        verify(this.productsAPIService, times(1)).updateCommonConfiguration("CODE", productCommonConfigurationDTO, 1);
    }

    @Test
    void updateJiraKeyTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.updateJiraKey(metadata, 1, "PROJECTKEY"));
        verify(this.productsAPIService, times(1)).updateProjectKey(1, "PROJECTKEY", "CODE");
    }

    @Test
    void updateJiraKeyErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productsAPIService).updateProjectKey(anyInt(), anyString(), anyString());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.updateJiraKey(metadata, 1, "PROJECTKEY"), "Exception was correclty catched");
        verify(this.productsAPIService, times(1)).updateProjectKey(1, "PROJECTKEY", "CODE");
    }


    @Test
    void getProductTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.getProduct(metadata, 1));
        verify(this.productsAPIService, times(1)).getProductSummary(1);
    }

    @Test
    void getProductErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productsAPIService).getProductSummary(anyInt());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.getProduct(metadata, 1));
        verify(this.productsAPIService, times(1)).getProductSummary(1);
    }

    @Test
    void getProductsByStatusTest() throws Errors
    {
        this.listenerProductsapi.getProductsByStatus(this.metadata, "TYPE", "STATUSCODE");
        verify(this.productsAPIService, times(1)).getAllProductsByStatus("STATUSCODE", "TYPE");
    }


    @Test
    void updateStatusTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.updateStatus(metadata, 1, "STATUS"));
        verify(this.productsAPIService, times(1)).updateProductStatus(1, "STATUS");
    }

    @Test
    void updateStatusErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productsAPIService).updateProductStatus(anyInt(), anyString());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.updateStatus(metadata, 1, "STATUS"));
        verify(this.productsAPIService, times(1)).updateProductStatus(1, "STATUS");
    }

    @Test
    void getTypesTest() throws Errors
    {
        this.listenerProductsapi.getTypes(this.metadata);
        verify(this.productsAPIService, times(1)).getTypes();
    }

    @Test
    void getProductsUsedResourcesReportCSVTest() throws Errors
    {
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.getProductsUsedResourcesReportExport(metadata, FORMAT_CSV, environment, uuaa));
        verify(this.productReportService, times(1)).getProductsUsedResourcesReportExport(anyString(), anyString(), anyString());
    }

    @Test
    void getProductsTest() throws Errors
    {
        this.listenerProductsapi.getProducts(metadata, "TYPE", "CODE");
        verify(this.productsAPIService, times(1)).getProducts("TYPE", "CODE");
    }


    @Test
    void createProductRequestTest() throws Errors
    {
        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        this.listenerProductsapi.createProductRequest(this.metadata, productRequestDTO);
        verify(this.newProductRequestService, times(1)).newProductRequest(productRequestDTO, "CODE");
    }

    @Test
    void newProductRequestTest() throws Errors
    {
        ProductRequestDTO productRequestDTO = this.generateProductRequestDTO();
        this.listenerProductsapi.createProductRequest(metadata, productRequestDTO);
        verify(this.newProductRequestService, times(1)).newProductRequest(productRequestDTO, "CODE");
    }

    @Test
    void getProductsHostsReportTest() throws Errors
    {
        this.listenerProductsapi.getProductsHostsReport(metadata, "PRO", "CPD");
        verify(this.productReportService, times(1)).getProductsHostsReport("PRO", "CPD");
    }

    @Test
    void getCategoriesTest() throws Errors
    {
        this.listenerProductsapi.getCategories(metadata, "TYPE", false);
        verify(this.categoryService, times(1)).getAllCategories("TYPE", false);
    }

    @Test
    void getProductsAssignedResourcesReportCSVTest() throws Errors
    {
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        this.listenerProductsapi.getProductsAssignedResourcesReportExport(metadata, FORMAT_CSV, environment, uuaa);
        verify(this.productReportService, times(1)).getProductsAssignedResourcesReportExport(anyString(), anyString(), anyString());
    }


    @Test
    void getProductsHostsReportCSVTest() throws Errors
    {
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        this.listenerProductsapi.getProductsHostsReportExport(metadata, FORMAT_CSV, environment, uuaa);
        verify(this.productReportService, times(1)).getProductsHostsReportExport(anyString(), anyString(), anyString());
    }

    @Test
    void removeProductMemberTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.removeProductMember(metadata, 1, "USERCODE"));
        verify(this.productsAPIService, times(1)).removeUserFromProduct("CODE", 1, "USERCODE");
    }

    @Test
    void removeProductMemberErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productsAPIService).removeUserFromProduct(anyString(), anyInt(), anyString());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.removeProductMember(metadata, 1, "USERCODE"));
        verify(this.productsAPIService, times(1)).removeUserFromProduct("CODE", 1, "USERCODE");

    }

    @Test
    void addProductMemberTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.addProductMember(metadata, 1, "USERCODE", "ROLE"));
        verify(this.productsAPIService, times(1)).addUserToProduct("CODE", 1, "USERCODE", "ROLE");
    }

    @Test
    void addProductMemberErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productsAPIService).addUserToProduct(anyString(), anyInt(), anyString(), anyString());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.addProductMember(metadata, 1, "USERCODE", "ROLE"));
        verify(this.productsAPIService, times(1)).addUserToProduct("CODE", 1, "USERCODE", "ROLE");

    }

    @Test
    void removeProductTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.removeProduct(metadata, 1));
        verify(this.productRemoveService, times(1)).removeProduct("CODE", 1);
    }

    @Test
    void removeProductErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productRemoveService).removeProduct(anyString(), anyInt());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.removeProduct(metadata, 1));
        verify(this.productRemoveService, times(1)).removeProduct("CODE", 1);

    }

    @Test
    void getBaseProductTest() throws Errors
    {
        Assertions.assertDoesNotThrow(() -> this.listenerProductsapi.getBaseProduct(metadata, 1));
        verify(this.productsAPIService, times(1)).getBaseProduct(1);
    }

    @Test
    void getBaseProductErrorTest() throws Errors
    {
        doThrow(new RuntimeException()).when(this.productsAPIService).getBaseProduct(anyInt());
        Assertions.assertThrows(LogAndTraceException.class, () -> this.listenerProductsapi.getBaseProduct(metadata, 1));
        verify(this.productsAPIService, times(1)).getBaseProduct(1);

    }

    @Test
    void updateProductTest() throws Errors
    {
        ProductSummaryDTO productSummaryDTO = this.generateProductSummaryDTO();
        this.listenerProductsapi.updateProduct(metadata, productSummaryDTO);
        verify(this.updateProductService, times(1)).updateProduct(productSummaryDTO, "CODE");
    }

    @Test
    void getProductsAssignedResourcesReportTest() throws Errors
    {
        this.listenerProductsapi.getProductsAssignedResourcesReport(metadata, 1, "PRO", 0, "UUAA");
        verify(this.productReportService, times(1)).getProductsAssignedResourcesReport("PRO", "UUAA", 1, 0);
    }

    @Test
    void getUuaasTest() throws Errors
    {
        this.listenerProductsapi.getUuaas(metadata);
        verify(this.productsAPIService, times(1)).getUuaas();
    }

    @Test
    void getUuaasNovaEphoenixLegacyTest() throws Errors
    {
        this.listenerProductsapi.getUuaasNovaEphoenixLegacy(metadata);
        verify(this.productsAPIService, times(1)).getUuaasNovaEphoenixLegacy();
    }

    @Test
    void getUuaasEphoenixLegacyTest() throws Errors
    {
        this.listenerProductsapi.getUuaasEphoenixLegacy(metadata);
        verify(this.productsAPIService, times(1)).getUuaasEphoenixLegacy();
    }
}
