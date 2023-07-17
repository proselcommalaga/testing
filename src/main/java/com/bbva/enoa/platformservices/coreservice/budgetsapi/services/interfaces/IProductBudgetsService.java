package com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces;


import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetail;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetailItem;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceSummaryItem;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.BrokerInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.DeploymentInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.FilesystemInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.NewBrokerInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.NewDeploymentInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBAvailabilityNovaCoinsDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBHardwareBudgetSnapshot;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBProductsUsedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PackInfo;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductBudgets;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductServiceDetailItem;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.UpdatedProductService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Created by xe68445 on 11/10/2017.
 */
public interface IProductBudgetsService
{
    /**
     * Check a deployment plan budget availability from product budgets
     *
     * @param deploymentInfo Info about the deployment plan
     * @param productId      Id of the product
     * @return deployable
     * @throws NovaException on error
     */
    boolean checkDeploymentPlan(DeploymentInfo deploymentInfo, int productId) throws NovaException;

    /**
     * Check a filesystem budget availability from product budgets
     *
     * @param filesystemInfo Info about the filesystem
     * @param productId      Id of the product
     * @return available
     * @throws NovaException on error
     */
    boolean checkFilesystem(FilesystemInfo filesystemInfo, int productId) throws NovaException;

    /**
     * Check a broker budget availability from product budgets
     *
     * @param brokerInfo Info about the broker
     * @param productId      Id of the product
     * @return available
     * @throws NovaException on error
     */
    boolean checkBroker(BrokerInfo brokerInfo, int productId) throws NovaException;

    /**
     * Delete a deployment plan info from product budgets
     *
     * @param deploymentId Id of the deployment plan
     * @throws NovaException on error
     */
    void deleteDeploymentPlan(int deploymentId) throws NovaException;

    /**
     * Delete a broker from product budgets
     *
     * @param brokerId Id of the broker
     * @throws NovaException on error
     */
    void deleteBroker(int brokerId) throws NovaException;

    /**
     * Delete a product info from product budgets
     *
     * @param productId Id of the product
     * @throws NovaException on error
     */
    void deleteProductInfo(int productId) throws NovaException;

    /**
     * Get info about all filesystem packs from product budgets
     *
     * @return list of pack info
     * @throws NovaException on error
     */
    List<PackInfo> getAllFilesystemPacksInfo() throws NovaException;

    /**
     * Get info about all hardware packs from product budgets
     *
     * @return list of pack info
     * @throws NovaException on error
     */
    List<PackInfo> getAllHardwarePacksInfo() throws NovaException;

    /**
     * Get info about a filesystem pack from product budgets
     *
     * @param packId Id of the pack
     * @return pack info
     * @throws NovaException on error
     */
    PackInfo getFilesystemPackInfo(int packId) throws NovaException;

    /**
     * Get info about a hardware pack from product budgets
     *
     * @param packId Id of the pack
     * @return pack info
     * @throws NovaException on error
     */
    PackInfo getHardwarePackInfo(int packId) throws NovaException;

    /**
     * Get a product budgets in a environment from product budgets
     *
     * @param productId   Id of the product
     * @param environment Environment
     * @return product budget
     * @throws NovaException on error
     */
    ProductBudgets getProductBudgets(int productId, String environment) throws NovaException;

    /**
     * Get a detail of all product services from product budgets
     *
     * @param productId Id of the product
     * @return list of service detail item
     * @throws NovaException on error
     */
    List<BUDGServiceDetailItem> getProductServicesDetail(int productId) throws NovaException;

    /**
     * Get a summary of all product services from product budgets
     *
     * @param productId Id of the product
     * @return list of service summary item
     * @throws NovaException on error
     */
    List<BUDGServiceSummaryItem> getProductServicesSummary(int productId) throws NovaException;

    /**
     * Get a detail of product service from product budgets
     *
     * @param serviceId Id of the product service
     * @return service detail
     * @throws NovaException on error
     */
    BUDGServiceDetail getServiceDetail(long serviceId) throws NovaException;

    /**
     * Insert a new deployment plan info into product budgets
     *
     * @param deploymentInfo Info about the deployment plan
     * @param productId      Id of the product
     * @throws NovaException on error
     */
    void insertDeploymentPlan(NewDeploymentInfo deploymentInfo, int productId) throws NovaException;

    /**
     * Insert a new broker into product budgets
     *
     * @param brokerInfo Info about the broker
     * @param productId  Id of the product
     * @throws NovaException on error
     */
    void insertBroker(NewBrokerInfo brokerInfo, int productId) throws NovaException;

    /**
     * Update a product service
     *
     * @param updatedProductService Updated product service
     * @param serviceId             Id of the product service
     * @throws NovaException on error
     */
    void updateService(UpdatedProductService updatedProductService, long serviceId) throws NovaException;

    /**
     * Get a array of ProductServiceDetailItem. The results can be filtered by Product ID and Status.
     *
     * @param productId Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @param status    Filter the results for a specific Status. If it's equals to "ALL", or it's null, or it's empty, no Status filtering is applied.
     * @return An array of ProductServiceDetailItem
     * @throws NovaException on error
     */
    ProductServiceDetailItem[] getProductServicesDetail(final String productId, final String status) throws NovaException;

    /**
     * Gets the response of a call to product budgets service, responsible of getting assigned, used and available hardware (in nova coins).
     *
     * @param productIds  An array of product ids.
     * @param environment The environment.
     * @return The response of a call to product budgets service, responsible of getting assigned, used and available hardware (in nova coins).
     */
    PBProductsUsedResourcesReportDTO getProductsUsedResourcesReport(long[] productIds, String environment);

    /**
     * Gets an array of DTOs having information for hardware budget in statistic history loading.
     *
     * @return an array of DTOs having information for hardware budget in statistic history loading.
     */
    PBHardwareBudgetSnapshot[] getHardwareBudgetHistorySnapshot();

    /**
     * Returns an array with DTOs representing used and available NOVA coins, grouped by environment.
     *
     * @param productId  The product id.
     * @param budgetType Budget type. Options are HARDWARE (cpu and memory) and FILESYSTEM. If no budget type is provided, it will return NOVA coins for both of types.
     * @return An array with DTOs representing used and available NOVA coins, grouped by environment.
     */
    PBAvailabilityNovaCoinsDTO[] getNovaCoinsByAvailability(Integer productId, String budgetType);


    /**
     * Get info about a broker hardware pack from product budgets
     *
     * @param packId Id of the pack
     * @return pack info
     * @throws NovaException on error
     */
    PackInfo getBrokerPackInfo(int packId) throws NovaException;
}
