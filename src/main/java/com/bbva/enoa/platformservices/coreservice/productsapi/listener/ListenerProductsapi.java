package com.bbva.enoa.platformservices.coreservice.productsapi.listener;

import com.bbva.enoa.apirestgen.productsapi.model.*;
import com.bbva.enoa.apirestgen.productsapi.server.spring.nova.rest.IRestListenerProductsapi;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ------------------------------------------------
 * Listener that implements all the functionality the Products API REST Controller
 *
 * @author BBVA - XE30432
 * ------------------------------------------------
 */
@Component
public class ListenerProductsapi implements IRestListenerProductsapi
{

    /**
     * Category service
     */
    private final ICategoryService categoryService;

    /**
     * productsAPIService
     */
    private final IProductsAPIService productsAPIService;

    /**
     * productRemoveService
     */
    private final IProductRemoveService productRemoveService;

    /**
     * newProductRequestService
     */
    private final INewProductRequestService newProductRequestService;

    /**
     * updateProductService
     */
    private final IUpdateProductService updateProductService;

    /**
     * Product report service
     */
    private final IProductReportService productReportService;

    /**
     * Constructor
     *
     * @param categoryService       Category service dependency
     * @param productsAPIService    Productsapi dependency
     * @param updateProductService  update product service
     * @param productRemoveService  remove product service
     * @param productRequestService product request service
     * @param productReportService  product report service
     */
    @Autowired
    public ListenerProductsapi(final ICategoryService categoryService, final IProductsAPIService productsAPIService, final IUpdateProductService updateProductService,
                               final IProductRemoveService productRemoveService, final INewProductRequestService productRequestService,
                               final IProductReportService productReportService)
    {
        this.categoryService = categoryService;
        this.productsAPIService = productsAPIService;
        this.productRemoveService = productRemoveService;
        this.newProductRequestService = productRequestService;
        this.updateProductService = updateProductService;
        this.productReportService = productReportService;
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductsUsedResourcesReportDTO getProductsUsedResourcesReport(NovaMetadata novaMetadata, String environment, String uuaa) throws Errors
    {
        return this.productReportService.getProductsUsedResourcesReport(environment, uuaa);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductsFilesystemsUsageReportDTO getProductsFilesytemsUsageReport(NovaMetadata novaMetadata, String environment, String uuaa) throws Errors
    {
        return this.productReportService.getProductsFilesytemsUsageReport(environment, uuaa);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getProductsFilesytemsUsageReportExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa) throws Errors
    {
        return this.productReportService.getProductsFilesytemsUsageReportExport(environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void createProduct(final NovaMetadata novaMetadata, final ProductRequestDTO productRequestDTO) throws Errors
    {
        this.productsAPIService.createNewProduct(productRequestDTO);
    }

    @Override
    public ProductCommonConfigurationDTO getCommonConfiguration(NovaMetadata novaMetadata, Integer productId) throws Errors
    {
        return this.productsAPIService.getCommonConfiguration(productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void updateCommonConfiguration(NovaMetadata novaMetadata, ProductCommonConfigurationDTO commonConfiguration, Integer productId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.updateCommonConfiguration(ivUser, commonConfiguration, productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void updateJiraKey(final NovaMetadata novaMetadata, final Integer productId, final String projectKey) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        try
        {
            this.productsAPIService.updateProjectKey(productId, projectKey, ivUser);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductSummaryDTO getProduct(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        try
        {
            return this.productsAPIService.getProductSummary(productId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    public ProductSummaryDTO getProductSummaryByUUAA(final NovaMetadata novaMetadata, final String uuaa) throws Errors
    {
        return this.productsAPIService.findProductSummaryByUUAA(uuaa);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductDTO[] getProductsByStatus(final NovaMetadata novaMetadata, final String type, final String statusCode) throws Errors
    {
        return this.productsAPIService.getAllProductsByStatus(statusCode, type);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void updateStatus(final NovaMetadata novaMetadata, final Integer productId, final String status) throws Errors
    {
        try
        {
            this.productsAPIService.updateProductStatus(productId, status);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }

    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getTypes(NovaMetadata novaMetadata) throws Errors
    {
        return this.productsAPIService.getTypes();
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public String[] getUuaasNovaEphoenixLegacy(NovaMetadata novaMetadata) throws Errors
    {
        return this.productsAPIService.getUuaasNovaEphoenixLegacy();
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void createFromEtherConsole(NovaMetadata novaMetadata, EtherConsoleProductCreationNotificationDTO etherConsoleProductCreationNotification) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.createFromEtherConsole(ivUser, etherConsoleProductCreationNotification);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getProductsUsedResourcesReportExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa) throws Errors
    {
        return productReportService.getProductsUsedResourcesReportExport(environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductDTO[] getProducts(final NovaMetadata novaMetadata, final String type, final String userCode) throws Errors
    {
        return this.productsAPIService.getProducts(type, userCode);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void createProductRequest(final NovaMetadata novaMetadata, final ProductRequestDTO productRequestDTO) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.newProductRequestService.newProductRequest(productRequestDTO, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductsHostsReportDTO getProductsHostsReport(NovaMetadata novaMetadata, String environment, String cpd) throws Errors
    {
        return this.productReportService.getProductsHostsReport(environment, cpd);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getProductsHostsReportExport(final NovaMetadata novaMetadata, final String format, final String environment, final String cpd) throws Errors
    {
        return this.productReportService.getProductsHostsReportExport(environment, cpd, format);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getCategories(final NovaMetadata novaMetadata, final String type, Boolean functional) throws Errors
    {
        return this.categoryService.getAllCategories(type, functional);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public byte[] getProductsAssignedResourcesReportExport(final NovaMetadata novaMetadata, final String format, final String environment, final String uuaa) throws Errors
    {
        return this.productReportService.getProductsAssignedResourcesReportExport(environment, uuaa, format);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void removeProductMember(final NovaMetadata novaMetadata, Integer productId, String userCode) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        try
        {
            this.productsAPIService.removeUserFromProduct(ivUser, productId, userCode);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public String[] getUuaasEphoenixLegacy(NovaMetadata novaMetadata) throws Errors
    {
        return this.productsAPIService.getUuaasEphoenixLegacy();
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public Long getProductsCount(NovaMetadata novaMetadata) throws Errors
    {
        return this.productsAPIService.getProductsCount();
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void addProductMember(final NovaMetadata novaMetadata, final Integer productId, final String userCode, final String role) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        try
        {
            this.productsAPIService.addUserToProduct(ivUser, productId, userCode, role);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void removeProduct(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        try
        {
            this.productRemoveService.removeProduct(ivUser, productId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductBaseDTO getBaseProduct(NovaMetadata novaMetadata, Integer productId) throws Errors
    {
        try
        {
            return this.productsAPIService.getBaseProduct(productId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE)
    public void updateProduct(final NovaMetadata novaMetadata, final ProductSummaryDTO updateProduct) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.updateProductService.updateProduct(updateProduct, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductsAssignedResourcesReport getProductsAssignedResourcesReport(NovaMetadata novaMetadata, Integer pageSize, String environment, Integer pageNumber, String uuaa) throws Errors
    {
        return this.productReportService.getProductsAssignedResourcesReport(environment, uuaa, pageSize, pageNumber);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public String[] getUuaas(NovaMetadata novaMetadata) throws Errors
    {
        return this.productsAPIService.getUuaas();
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductBaseDTO getProductByReleaseVersionService(NovaMetadata novaMetadata, Integer releaseVersionServiceId) throws Errors
    {
        return this.productsAPIService.findProductByReleaseVersionServiceId(releaseVersionServiceId);
    }
    
    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductBaseDTO getProductByBehaviorVersionService(final NovaMetadata novaMetadata, final Integer behaviorVersionServiceId) throws Errors
    {
        return this.productsAPIService.findProductByBehaviorVersionServiceId(behaviorVersionServiceId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductBaseDTO getProductByReleaseVersion(NovaMetadata novaMetadata, Integer releaseVersionId) throws Errors
    {
        return this.productsAPIService.findProductByReleaseVersionId(releaseVersionId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductBaseDTO getProductByUUAA(NovaMetadata novaMetadata, String uuaa) throws Errors
    {
        return this.productsAPIService.findProductByUUAA(uuaa);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ProductConfigurationByEnvDTO getProductConfigurationByEnvironment(NovaMetadata novaMetadata, Integer productId, String environment) throws Errors
    {
        return this.productsAPIService.getProductConfigurationByEnvironment(productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public void updateProductServiceConfigurationByEnv(NovaMetadata novaMetadata, ProductServiceConfigurationDTO productServiceConfig, Integer productId, String environment) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.updateProductServiceConfigurationByEnv(ivUser, productServiceConfig, productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public void updateManagementConfigurationByEnv(NovaMetadata novaMetadata, ProductManagementConfByEnvDTO managementConfigurationDTO, Integer productId, String environment) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.updateManagementConfigurationByEnv(ivUser, managementConfigurationDTO, productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public void updateGeneralInfrastructureConfigurationByEnv(NovaMetadata novaMetadata, InfrastructureConfiguredByEnvDTO generalInfrastructureConfig, Integer productId, String environment) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.updateGeneralInfrastructureConfigurationByEnv(ivUser, generalInfrastructureConfig, productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public NovaConfigurationDTO getNovaInfrastructureConfigurationByEnv(NovaMetadata novaMetadata, Integer productId, String environment) throws Errors
    {
        return this.productsAPIService.getNovaInfrastructureConfByEnv(productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public void updateNovaInfrastructureConfigurationByEnv(NovaMetadata novaMetadata, NovaConfigurationDTO novaConfigurationDTO, Integer productId, String environment) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.updateNovaInfrastructureConfByEnv(ivUser, novaConfigurationDTO, productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public EtherConfigurationDTOResponse getEtherInfrastructureConfigurationByEnv(NovaMetadata novaMetadata, Integer productId, String environment) throws Errors
    {
        return this.productsAPIService.getEtherInfrastructureConfByEnv(productId, environment);
    }

    @Override
    @LogAndTrace(apiName = Constants.PRODUCT_API, runtimeExceptionErrorCode = ProductsAPIError.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public void updateEtherInfrastructureConfigurationByEnv(NovaMetadata novaMetadata, EtherConfigurationDTO etherConfigurationDTO, Integer productId, String environment) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.productsAPIService.updateEtherInfrastructureConfByEnv(ivUser, etherConfigurationDTO, productId, environment);
    }
}
