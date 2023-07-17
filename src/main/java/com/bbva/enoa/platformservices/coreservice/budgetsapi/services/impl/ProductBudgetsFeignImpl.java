package com.bbva.enoa.platformservices.coreservice.budgetsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGConfiguration;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetail;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceDetailItem;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGServiceSummaryItem;
import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.IRestHandlerProductbudgetsapi;
import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.IRestListenerProductbudgetsapi;
import com.bbva.enoa.apirestgen.productbudgetsapi.client.feign.nova.rest.impl.RestHandlerProductbudgetsapi;
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
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductServiceDetail;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductServiceDetailItem;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.ProductServiceSummaryItem;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.UpdatedProductService;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions.BudgetsError;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by xe68445 on 11/10/2017.
 */
@Service
@Slf4j
public class ProductBudgetsFeignImpl implements IProductBudgetsService
{
    private final IRestHandlerProductbudgetsapi iRestHandlerProductbudgetsapi;

    private RestHandlerProductbudgetsapi restHandlerProductbudgetsapi;

    /**
     * All args constructor for dependency injection
     *
     * @param iRestHandlerProductbudgetsapi IRestHandlerProductbudgetsapi dependency
     */
    @Autowired
    public ProductBudgetsFeignImpl(IRestHandlerProductbudgetsapi iRestHandlerProductbudgetsapi)
    {

        this.iRestHandlerProductbudgetsapi = iRestHandlerProductbudgetsapi;
    }

    /**
     * Bean initializer
     */
    @PostConstruct
    public void init()
    {

        this.restHandlerProductbudgetsapi = new RestHandlerProductbudgetsapi(iRestHandlerProductbudgetsapi);
    }

    /**
     * Check a deployment plan budget availability from product budgets API
     *
     * @param deploymentInfo Info about the deployment plan
     * @param productId      Id of the product
     * @return deployable
     * @throws NovaException on error
     */
    @Override
    public boolean checkDeploymentPlan(DeploymentInfo deploymentInfo, int productId) throws NovaException
    {

        log.debug("Checking deployment plan for product {} from ProductBudgets API ...", productId);

        AtomicReference<Boolean> response = new AtomicReference<>();

        this.restHandlerProductbudgetsapi.checkDeploymentPlan(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void checkDeploymentPlan(Boolean outcome)
            {
                response.set(outcome);

                log.debug("Deployment plan checked");
            }

            @Override
            public void checkDeploymentPlanErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [checkDeploymentPlan]: there was an error in ProductBudgets integration with productId: [{}]. Deployment info: [{}]. Error message: [{}]", productId, deploymentInfo, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to check deployment plan");
            }

        }, deploymentInfo, productId);

        return response.get();
    }

    /**
     * Check a filesystem budget availability from product budgets API
     *
     * @param filesystemInfo Info about the filesystem
     * @param productId      Id of the product
     * @return available
     * @throws NovaException on error
     */
    @Override
    public boolean checkFilesystem(FilesystemInfo filesystemInfo, int productId) throws NovaException
    {
        log.debug("Checking filesystem for product {} from ProductBudgets API ...", productId);

        AtomicReference<Boolean> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.checkFilesystem(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void checkFilesystem(Boolean outcome)
            {
                response.set(outcome);

                log.debug("Filesystem checked");
            }

            @Override
            public void checkFilesystemErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [checkFilesystemErrors]: Error in ProductBudgets integration checkFilesystem for product id: [{}] - filesystem info: [{}]. Error message: [{}]", productId, filesystemInfo, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to check filesystem");
            }
        }, filesystemInfo, productId);

        return response.get();
    }

    /**
     * Check a filesystem budget availability from product budgets API
     *
     * @param brokerInfo Info about the broker
     * @param productId      Id of the product
     * @return available
     * @throws NovaException on error
     */
    @Override
    public boolean checkBroker(BrokerInfo brokerInfo, int productId) throws NovaException
    {
        log.debug("Checking broker for product {} from ProductBudgets API ...", productId);

        AtomicReference<Boolean> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.checkBroker(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void checkBroker(Boolean outcome)
            {
                response.set(outcome);

                log.debug("Broker checked");
            }

            @Override
            public void checkBrokerErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [checkBrokerErrors]: Error in ProductBudgets integration checkBroker for product id: [{}] - filesystem info: [{}]. Error message: [{}]", productId, brokerInfo, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to check broker");
            }
        }, brokerInfo, productId);

        return response.get();
    }

    /**
     * Delete a broker from product budgets
     *
     * @param brokerId Id of the broker
     * @throws NovaException on error
     */
    @Override
    public void deleteBroker(int brokerId) throws NovaException
    {
        log.debug("Deleting broker id {} from product budgets ...", brokerId);

        this.restHandlerProductbudgetsapi.deleteBroker(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void deleteBroker()
            {
                log.debug("Deleted broker from product budgets successfully");
            }

            @Override
            public void deleteBrokerErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [deleteBroker]:  Error in ProductBudgets integration deleteBroker with id: [{}]. Error message: [{}]", brokerId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to delete broker");
            }
        }, brokerId);
    }

    /**
     * Delete a broker info from product budgets API
     *
     * @param deploymentId Id of the broker
     * @throws NovaException on error
     */
    @Override
    public void deleteDeploymentPlan(int deploymentId) throws NovaException
    {
        log.debug("Sending unbroker {} request to ProductBudgets API ...", deploymentId);

        this.restHandlerProductbudgetsapi.deleteDeploymentPlan(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void deleteDeploymentPlan()
            {
                log.debug("Unbroker request sent");
            }

            @Override
            public void deleteDeploymentPlanErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [deleteDeploymentPlan]:  Error in ProductBudgets integration deleteDeploymentPlan with id: [{}]. Error message: [{}]", deploymentId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to send unbroker request");
            }
        }, deploymentId);

    }

    /**
     * Delete a product info from product budgets API
     *
     * @param productId Id of the product
     * @throws NovaException on error
     */
    @Override
    public void deleteProductInfo(int productId) throws NovaException
    {
        log.debug("Deleting product {} info from ProductBudgets API ...", productId);

        this.restHandlerProductbudgetsapi.deleteProduct(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void deleteProduct()
            {
                log.debug("Product deleted");
            }

            @Override
            public void deleteProductErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [deleteProductInfo]: Error in ProductBudgets integration deleteProduct with id: [{}]. Error message: [{}]", productId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        MessageFormat.format("Unable to delete product {0} info", productId));
            }

        }, productId);

    }

    /**
     * Get info about all filesystem packs from product budgets API
     *
     * @return list of pack info
     * @throws NovaException on error
     */
    @Override
    public List<PackInfo> getAllFilesystemPacksInfo() throws NovaException
    {
        log.debug("Getting all filesystem packs info...");

        AtomicReference<PackInfo[]> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getAllFilesystemPacks(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getAllFilesystemPacks(PackInfo[] outcome)
            {
                response.set(outcome);

                log.debug("Packs info got");
            }

            @Override
            public void getAllFilesystemPacksErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getAllFilesystemPacksInfo]: Error in ProductBudgets integration getAllFilesystemPacksInfo. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        "Unable to get filesystem packs info");
            }
        });

        return Arrays.asList(response.get());
    }

    /**
     * Get info about all hardware packs from product budgets API
     *
     * @return list of pack info
     * @throws NovaException on error
     */
    @Override
    public List<PackInfo> getAllHardwarePacksInfo() throws NovaException
    {
        log.debug("Getting all hardware packs info...");

        AtomicReference<PackInfo[]> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getAllHardwarePacks(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getAllHardwarePacks(PackInfo[] outcome)
            {
                response.set(outcome);

                log.debug("Packs info got");
            }

            @Override
            public void getAllHardwarePacksErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getAllHardwarePacksInfo]: Error in ProductBudgets integration getAllHardwarePacksInfo. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        "Unable to get hardware packs info");
            }
        });

        return Arrays.asList(response.get());
    }

    /**
     * Get info about a filesystem pack from product budgets API
     *
     * @param packId Id of the pack
     * @return pack info
     * @throws NovaException on error
     */
    @Override
    public PackInfo getFilesystemPackInfo(int packId) throws NovaException
    {
        log.debug("Getting filesystem pack {} info ...", packId);

        AtomicReference<PackInfo> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getFilesystemPack(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getFilesystemPack(PackInfo outcome)
            {
                response.set(outcome);

                log.debug("Info got");
            }

            @Override
            public void getFilesystemPackErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getFilesystemPackInfo]: Error in ProductBudgets integration getFilesystemPackInfo with id: [{}]. Error message: [{}]", packId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), MessageFormat.format("Unable to get filesystem pack {0} info", packId));
            }
        }, packId);

        return response.get();
    }

    /**
     * Get info about a hardware pack from product budgets API
     *
     * @param packId Id of the pack
     * @return pack info
     * @throws NovaException on error
     */
    @Override
    public PackInfo getHardwarePackInfo(int packId) throws NovaException
    {
        log.debug("Getting hardware pack {} info ...", packId);

        AtomicReference<PackInfo> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getHardwarePack(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getHardwarePack(PackInfo outcome)
            {
                response.set(outcome);

                log.debug("Info got");
            }

            @Override
            public void getHardwarePackErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getHardwarePackInfo]: Error in ProductBudgets integration getHardwarePackInfo with pack id: [{}]. Error message: [{}]", packId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        MessageFormat.format("Unable to get hardware pack {0} info", packId));
            }
        }, packId);

        return response.get();
    }

    /**
     * Get a product budgets in a environment from product budgets API
     *
     * @param productId   Id of the product
     * @param environment Environment
     * @return product budget
     * @throws NovaException on error
     */
    @Override
    public ProductBudgets getProductBudgets(int productId, String environment) throws NovaException
    {
        log.debug("Getting product {} bugets for {} environment  from ProductBudgets API ...", productId, environment);

        AtomicReference<ProductBudgets> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getProductBudgets(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getProductBudgets(ProductBudgets outcome)
            {
                response.set(outcome);

                log.debug("Budgets got");
            }

            @Override
            public void getProductBudgetsErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getProductBudgets]: error in ProductBudgets integration getProductBudgets for product id: [{}] and environment: [{}]. Error message: [{}]", productId, environment, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), MessageFormat.format("Unable to get product {0} budgets", productId));
            }
        }, productId, environment);

        return response.get();
    }

    /**
     * Get a detail of all product services from product budgets API
     *
     * @param productId Id of the product
     * @return list of service detail item
     * @throws NovaException on error
     */
    @Override
    public List<BUDGServiceDetailItem> getProductServicesDetail(int productId) throws NovaException
    {
        log.debug("Getting product {} services detail from ProductBudgets API ...", productId);

        AtomicReference<List<BUDGServiceDetailItem>> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getAllProductServicesDetail(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getAllProductServicesDetail(ProductServiceDetailItem[] outcome)
            {
                response.set(Arrays.stream(outcome).map(serviceDetail ->
                {
                    BUDGServiceDetailItem item = new BUDGServiceDetailItem();
                    item.setStatus(serviceDetail.getStatus());
                    item.setPending(serviceDetail.getPending());
                    item.setEndDate(serviceDetail.getEndDate());
                    item.setContractDate(serviceDetail.getContractDate());
                    item.setCost(serviceDetail.getCost());
                    item.setInitiativeId(serviceDetail.getInitiativeId());
                    item.setInitiativeUrl(serviceDetail.getInitiativeUrl());
                    item.setProductServiceId(serviceDetail.getProductServiceId());
                    item.setServiceType(serviceDetail.getServiceType());
                    item.setStartDate(serviceDetail.getStartDate());

                    return item;

                }).collect(Collectors.toList()));

                log.debug("Product services detail got");
            }

            @Override
            public void getAllProductServicesDetailErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getProductServicesDetail]: Error in ProductBudgets integration getProductServicesDetail for product id: [{}]. Error message: [{}]", productId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        MessageFormat.format("Unable to get product {0} services detail", productId));
            }

        }, productId);

        return response.get();
    }

    /**
     * Get a summary of all product services from product budgets API
     *
     * @param productId Id of the product
     * @return list of summary items
     * @throws NovaException on error
     */
    @Override
    public List<BUDGServiceSummaryItem> getProductServicesSummary(int productId) throws NovaException
    {
        log.debug("Getting product {} services summary from ProductBudgets API ...", productId);

        AtomicReference<List<BUDGServiceSummaryItem>> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getAllProductServicesSummary(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getAllProductServicesSummary(ProductServiceSummaryItem[] outcome)
            {
                response.set(Arrays.stream(outcome).map(serviceSummary ->
                {

                    BUDGServiceSummaryItem item = new BUDGServiceSummaryItem();
                    item.setEndDate(serviceSummary.getEndDate());
                    item.setNovaServiceType(serviceSummary.getNovaServiceType());
                    item.setPending(serviceSummary.getPending());
                    item.setStatus(serviceSummary.getStatus());

                    return item;

                }).collect(Collectors.toList()));

                log.debug("Product services summary got");
            }

            @Override
            public void getAllProductServicesSummaryErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getAllProductServicesSummary]: There was an error trying to get all product services summary. Error message: [{}]", outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        MessageFormat.format("Unable to get product {0} services summary", productId));
            }

        }, productId);

        return response.get();
    }

    /**
     * Get a detail of product service from product budgets API
     *
     * @param serviceId Id of the product service
     * @return service detail
     * @throws NovaException on error
     */
    @Override
    public BUDGServiceDetail getServiceDetail(long serviceId) throws NovaException
    {
        log.debug("Getting service {} detail from ProductBudgets API ...", serviceId);

        AtomicReference<BUDGServiceDetail> response = new AtomicReference<>();
        this.restHandlerProductbudgetsapi.getProductServiceDetail(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getProductServiceDetail(ProductServiceDetail serviceDetail)
            {
                BUDGServiceDetail detail = new BUDGServiceDetail();

                detail.setStartDate(serviceDetail.getStartDate());
                detail.setStatus(serviceDetail.getStatus());
                detail.setServiceType(serviceDetail.getServiceType());
                detail.setServiceCategory(serviceDetail.getServiceCategory());
                detail.setInitiativeUrl(serviceDetail.getInitiativeUrl());
                detail.setConditionsLink(serviceDetail.getConditionsLink());
                detail.setConditionsLinkLabel(serviceDetail.getConditionsLinkLabel());
                detail.setContractDate(serviceDetail.getContractDate());
                detail.setEndDate(serviceDetail.getEndDate());
                detail.setMaxEndDate(serviceDetail.getMaxEndDate());
                detail.setInitiativeId(serviceDetail.getInitiativeId());
                detail.setInitiativeName(serviceDetail.getInitiativeName());
                detail.setMaxStartDate(serviceDetail.getMaxStartDate());
                detail.setNotes(serviceDetail.getNotes());
                detail.setPending(serviceDetail.getPending());
                detail.setDuration(serviceDetail.getDuration());

                List<BUDGConfiguration> configurations = Arrays.stream(serviceDetail.getConfigurations()).map(configItem ->
                {

                    BUDGConfiguration configuration = new BUDGConfiguration();
                    configuration.setParameterName(configItem.getParameterName());
                    configuration.setParameterValue(configItem.getParameterValue());

                    return configuration;
                }).collect(Collectors.toList());

                detail.setConfigurations(configurations.toArray(BUDGConfiguration[]::new));

                response.set(detail);

                log.debug("Service detail got");
            }

            @Override
            public void getProductServiceDetailErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [getProductServiceDetailErrors]: Error in ProductBudgets integration getServiceDetail with service id: [{}]. Error message: [{}]", serviceId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        MessageFormat.format("Unable to get service {0} detail", serviceId));
            }

        }, serviceId);

        return response.get();
    }

    /**
     * Insert a new dpeloyment plan info into product budgets API
     *
     * @param deploymentInfo Info about the deploymen plan
     * @param productId      Id of the product
     * @throws NovaException on error
     */
    @Override
    public void insertDeploymentPlan(NewDeploymentInfo deploymentInfo, int productId) throws NovaException
    {
        log.debug("Sending deployment plan {} info to ProductBudgets API ...", deploymentInfo.getDeploymentPlanId());
        this.restHandlerProductbudgetsapi.insertDeploymentPlan(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void insertDeploymentPlan()
            {

                log.debug("Deployment plan info sent");
            }

            @Override
            public void insertDeploymentPlanErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [insertDeploymentPlan]: Error in ProductBudgets integration insertDeploymentPlan with deployment info: [{}] and product id: [{}]. Error message: [{}]", deploymentInfo, productId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to send deployment plan info");
            }
        }, deploymentInfo, productId);

    }

    /**
     * Insert a new broker into product budgets API
     *
     * @param brokerInfo Info about the new broker
     * @param productId  Id of the product
     * @throws NovaException on error
     */
    @Override
    public void insertBroker(NewBrokerInfo brokerInfo, int productId) throws NovaException
    {
        log.debug("[ProductBudgetsAPI Client] -> [insertBroker]: Adding broker {} id to ProductBudgets API ...", brokerInfo.getBrokerId());
        this.restHandlerProductbudgetsapi.insertBroker(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void insertBroker()
            {
                log.debug("[ProductBudgetsAPI Client] -> [insertBroker]: Added new broker successfully");
            }

            @Override
            public void insertBrokerErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [insertBroker]: Error in ProductBudgets integration insertBroker with broker info: [{}] and product id: [{}]. Error message: [{}]", brokerInfo, productId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to insert new broker");
            }
        }, brokerInfo, productId);
    }

    /**
     * Update a product service
     *
     * @param updatedProductService Updated product service
     * @param serviceId             Id of the product service
     * @throws NovaException on error
     */
    @Override
    public void updateService(UpdatedProductService updatedProductService, long serviceId) throws NovaException
    {
        log.debug("Updating service {} through ProductBudgets API ...", serviceId);

        this.restHandlerProductbudgetsapi.updateProductService(new IRestListenerProductbudgetsapi()
        {

            @Override
            public void updateProductService()
            {
                log.debug("Service updated");
            }

            @Override
            public void updateProductServiceErrors(Errors outcome)
            {
                log.error("[ProductBudgetsAPI Client] -> [updateService]: Error in ProductBudgets integration updateService with deployment service id: [{}] - update product service instance: [{}]. Error message: [{}]", serviceId, updatedProductService, outcome.getBodyExceptionMessage());
                if (outcome.getCause() != null && outcome.getCause().getLocalizedMessage() != null
                        && "Invalid date value".equalsIgnoreCase(outcome.getCause().getLocalizedMessage()))
                {
                    log.error("Error Invalid date value");
                    throw new NovaException(BudgetsError.getInvalidDateValueError(), "Invalid date value");
                }
                else
                {
                    log.error("General Error");
                    throw new NovaException(BudgetsError.getProductBudgetsApiError(), "Unable to update service");
                }
            }

        }, updatedProductService, serviceId);
    }

    @Override
    public ProductServiceDetailItem[] getProductServicesDetail(String productId, String status) throws NovaException
    {
        SingleApiClientResponseWrapper<ProductServiceDetailItem[]> response = new SingleApiClientResponseWrapper<>();

        log.debug("[ProductBudgetsFeignImpl] -> [getProductServicesDetail]: getting Product Services for Product ID [{}] and Status [{}]", productId, status);

        this.restHandlerProductbudgetsapi.getProductServices(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getProductServices(ProductServiceDetailItem[] outcome)
            {
                log.debug("[ProductBudgetsFeignImpl] -> [getProductServicesDetail]: successfully got Product Services for Product ID [{}] and Status [{}]", productId, status);
                response.set(outcome);
            }

            @Override
            public void getProductServicesErrors(Errors outcome)
            {
                log.error("[ProductBudgetsFeignImpl] -> [getProductServicesDetail]: Error trying to get Product Services for Product ID [{}] and Status [{}]: {}", productId, status, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), String.format("[ProductBudgetsFeignImpl] -> [getProductServicesDetail]: Error trying to get Product Services for Product ID [%s] and Status [%s]", productId, status));
            }

        }, productId, status);

        return response.get();
    }

    @Override
    public PBProductsUsedResourcesReportDTO getProductsUsedResourcesReport(long[] productIds, String environment)
    {
        SingleApiClientResponseWrapper<PBProductsUsedResourcesReportDTO> response = new SingleApiClientResponseWrapper<>();

        log.debug("[ProductBudgetsFeignImpl] -> [getProductsUsedResourcesReport]: getting product used resources for Product IDs [{}] and environment [{}]", productIds, environment);

        this.restHandlerProductbudgetsapi.getProductsUsedResourcesReport(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getProductsUsedResourcesReport(PBProductsUsedResourcesReportDTO outcome)
            {
                log.debug("[ProductBudgetsFeignImpl] -> [getProductsUsedResourcesReport]: successfully got product used resources for Product IDs [{}] and environment [{}]", productIds, environment);
                response.set(outcome);
            }

            @Override
            public void getProductsUsedResourcesReportErrors(Errors outcome)
            {
                log.error("[ProductBudgetsFeignImpl] -> [getProductsUsedResourcesReport]: Error trying to get product used resources for Product IDs [{}] and environment [{}]: {}", productIds, environment, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), String.format("[ProductBudgetsFeignImpl] -> [getProductsUsedResourcesReport]: Error trying to get Product Services for Product IDs [%s] and environment [%s]", productIds, environment));
            }

        }, productIds, environment);

        return response.get();
    }

    @Override
    public PBHardwareBudgetSnapshot[] getHardwareBudgetHistorySnapshot()
    {
        SingleApiClientResponseWrapper<PBHardwareBudgetSnapshot[]> response = new SingleApiClientResponseWrapper<>();

        log.info("[ProductBudgetsFeignImpl] -> [getHardwareBudgetSnapshot]: getting hardware information for statistic history loading");

        this.restHandlerProductbudgetsapi.getHardwareBudgetHistorySnapshot(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getHardwareBudgetHistorySnapshot(PBHardwareBudgetSnapshot[] outcome)
            {
                log.info("[ProductBudgetsFeignImpl] -> [getHardwareBudgetHistorySnapshot]: successfully got hardware information for statistic history loading");
                response.set(outcome);
            }

            @Override
            public void getHardwareBudgetHistorySnapshotErrors(Errors outcome)
            {
                log.error("[ProductBudgetsFeignImpl] -> [getHardwareBudgetHistorySnapshot]: Error trying to get hardware information for statistic history loading: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(), "[ProductBudgetsFeignImpl] -> [getProductsUsedResourcesReport]: Error trying to get hardware information for statistic history loading");
            }

        });

        return response.get();
    }

    @Override
    public PBAvailabilityNovaCoinsDTO[] getNovaCoinsByAvailability(Integer productId, String budgetType)
    {
        SingleApiClientResponseWrapper<PBAvailabilityNovaCoinsDTO[]> response = new SingleApiClientResponseWrapper<>();

        log.info("[ProductBudgetsFeignImpl] -> [getHardwareBudgetSnapshot]: getting hardware information for statistic history loading");

        this.restHandlerProductbudgetsapi.getNovaCoinsByAvailability(new IRestListenerProductbudgetsapi()
        {
            @Override
            public void getNovaCoinsByAvailability(PBAvailabilityNovaCoinsDTO[] outcome)
            {
                log.info("[ProductBudgetsFeignImpl] -> [getNovaCoinsByAvailability]: successfully got NOVA coins by environment for productId [{}] and budgetType [{}]", productId, budgetType);
                response.set(outcome);
            }

            @Override
            public void getNovaCoinsByAvailabilityErrors(Errors outcome)
            {
                log.error("[ProductBudgetsFeignImpl] -> [getNovaCoinsByAvailability]: Error getting NOVA coins by environment for productId [{}] and budgetType [{}]: {}", productId, budgetType, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getUnexpectedError(), "[ProductBudgetsFeignImpl] -> [getNovaCoinsByAvailability]: Error getting NOVA coins by environment");
            }

        }, budgetType, productId);

        return response.get();
    }

    /**
     * Get info about a broker hardware pack from product budgets API
     *
     * @param packId Id of the pack
     * @return pack info
     * @throws NovaException on error
     */
    @Override
    public PackInfo getBrokerPackInfo(int packId) throws NovaException
    {
        log.debug("Getting broker hardware pack {} info ...", packId);

        AtomicReference<PackInfo> response = new AtomicReference<>();

        this.restHandlerProductbudgetsapi.getBrokerPack(new IRestListenerProductbudgetsapi() {
            @Override
            public void getBrokerPack(PackInfo outcome) {
                response.set(outcome);

                log.debug("Info got");
            }

            @Override
            public void getBrokerPackErrors(Errors outcome) {
                log.error("[ProductBudgetsAPI Client] -> [getBrokerPackInfo]: Error in ProductBudgets integration getBrokerPackInfo with pack id: [{}]. Error message: [{}]", packId, outcome.getBodyExceptionMessage());
                throw new NovaException(BudgetsError.getProductBudgetsApiError(),
                        MessageFormat.format("Unable to get broker hardware pack {0} info", packId));
            }
        }, packId);

        return response.get();
    }

}
