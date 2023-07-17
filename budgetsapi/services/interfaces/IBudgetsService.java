package com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGProductBudgetsDTO;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetail;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetailItem;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceSummaryItem;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGUpdatedService;
import com.bbva.enoa.apirestgen.budgetsapi.model.DateObject;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBProductsUsedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PackInfo;
import com.bbva.enoa.datamodel.model.product.enumerates.GBType;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Created by xe68445 on 11/10/2017.
 */
public interface IBudgetsService
{
    /**
     * Check the deployability status of a deployment plan
     *
     * @param deploymentId Id of the deployment plan
     * @return deployable
     * @throws NovaException on error
     */
    boolean checkDeploymentPlanDeployabilityStatus(int deploymentId) throws NovaException;

    /**
     * Check the availability status of a filesystem
     *
     * @param productId   Id of the product
     * @param environment filesystem environment
     * @param packId      Filesystem pack id
     * @return available
     * @throws NovaException on error
     */
    boolean checkFilesystemAvailabilityStatus(int productId, String environment, int packId) throws NovaException;

    /**
     * Check if a product has some budget assigned
     *
     * @param productId Id of the product
     * @return true if product has budget
     * @throws NovaException on error
     */
    boolean checkProductBudget(int productId) throws NovaException;

    /**
     * Check if a product has at least one of the given services with the given pending status
     *
     * @param productId     Id of the product
     * @param serviceType   Service type
     * @param pendingStatus Service pending status
     * @return exists
     * @throws NovaException on error
     */
    boolean checkProductServices(int productId, GBType serviceType, boolean pendingStatus) throws NovaException;

    /**
     * Check if a product has at least one of the given services
     *
     * @param productId   Id of the product
     * @param serviceType Service type
     * @return exists
     * @throws NovaException on error
     */
    boolean checkProductServices(int productId, GBType serviceType) throws NovaException;

    /**
     * Get the price of a filesystem pack
     *
     * @param packId Id of the pack
     * @return pack price
     * @throws NovaException on error
     */
    double getFilesystemPackPrice(int packId) throws NovaException;

    /**
     * Get all filesystem packs info
     *
     * @return list of pack info
     * @throws NovaException on error
     */
    List<PackInfo> getFilesystemPacks() throws NovaException;

    /**
     * Get the price of a hardware pack
     *
     * @param packId Id of the pack
     * @return pack price
     * @throws NovaException on error
     */
    double getHardwarePackPrice(int packId) throws NovaException;

    /**
     * Get all hardware packs info
     *
     * @return list of pack info
     * @throws NovaException on error
     */
    List<PackInfo> getHardwarePacks() throws NovaException;

    /**
     * Get all product budgets for an environment
     *
     * @param productId   Id of the product
     * @param environment Environment
     * @return product budget dto
     * @throws NovaException on error
     */
    BUDGProductBudgetsDTO getProductBudgets(int productId, String environment) throws NovaException;

    /**
     * Get a detail of all product services
     *
     * @param productId Id of the product
     * @return list of service detail item
     * @throws NovaException on error
     */
    List<BUDGServiceDetailItem> getProductServicesDetail(int productId) throws NovaException;

    /**
     * Get a summary of all product services
     *
     * @param productId Id of the product
     * @return list of service summary item
     * @throws NovaException on error
     */
    List<BUDGServiceSummaryItem> getProductServicesSummary(int productId) throws NovaException;

    /**
     * Get a detail of a product service
     *
     * @param serviceId Id of the product service
     * @return service detail
     * @throws NovaException on error
     */
    BUDGServiceDetail getServiceDetail(long serviceId) throws NovaException;

    /**
     * Synchronize a new deployment plan in product budgets
     *
     * @param deploymentId Id of the deployment plan
     * @throws NovaException on error
     */
    void synchronizePlanDeployment(int deploymentId) throws NovaException;

    /**
     * Synchronize an old deployment plan deletion in product budgets
     *
     * @param deploymentId Id of the old deployment plan
     * @throws NovaException on error
     */
    void synchronizePlanUndeployment(int deploymentId) throws NovaException;

    /**
     * Synchronize an old product deletion in product budgets
     *
     * @param productId product id
     * @throws NovaException on error
     */
    void synchronizeProductDeletion(int productId) throws NovaException;

    /**
     * Update a product service in product budgets
     *
     * @param updatedService Updated service
     * @param serviceId      Id of the product service
     * @param ivUser         user
     * @throws NovaException on error
     */
    void updateService(BUDGUpdatedService updatedService, long serviceId, String ivUser) throws NovaException;

    /**
     * @param startDate Start event date
     * @return final event date
     * @throws NovaException on error
     */
    DateObject calculateFinalDate(DateObject startDate) throws NovaException;

    /**
     * Gets an object with the response of the assigned, used and available hardware information product budget api endpoint.
     *
     * @param productIds  An array of product ids.
     * @param environment The environment.
     * @return An object with the response of the assigned, used and available hardware information product budget api endpoint.
     */
    PBProductsUsedResourcesReportDTO getProductsUsedResourcesReport(long[] productIds, String environment);


    /**
     * Get the price of a broker hardware pack
     *
     * @param packId Id of the pack
     * @return pack price
     * @throws NovaException on error
     */
    double getBrokerPackPrice(int packId) throws NovaException;
}
