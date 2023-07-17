package com.bbva.enoa.platformservices.coreservice.budgetsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.*;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.product.enumerates.GBType;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions.BudgetsError;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.utils.BudgetsConstants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by xe68445 on 11/10/2017.
 */
@Service
@Slf4j
public class BudgetsServiceImpl implements IBudgetsService
{
    /**
     * DeploymentPlan repository
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * ProductBudgets service
     */
    private final IProductBudgetsService productBudgetsService;

    /**
     * User service client
     */
    private final IProductUsersClient usersService;

    /**
     * Tools Service client
     */
    private final IToolsClient toolsService;

    /**
     * All args contructor for dependnecy injection
     *
     * @param productBudgetsService    ProductBudgetsService dependency
     * @param deploymentPlanRepository DeploymentPlanRepository dependency
     * @param usersService             userService dependency
     * @param toolsService             ToolsService dependency
     */
    @Autowired
    public BudgetsServiceImpl(final IProductBudgetsService productBudgetsService, final DeploymentPlanRepository deploymentPlanRepository,
                              final IProductUsersClient usersService, final IToolsClient toolsService)
    {
        this.productBudgetsService = productBudgetsService;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.usersService = usersService;
        this.toolsService = toolsService;
    }

    /**
     * Check the deployability status of a deployment plan
     *
     * @param deploymentId Id of the deployment plan
     * @return {@code true} if it is able to deploy. {@code false} it is not able to deploy
     */
    @Override
    public boolean checkDeploymentPlanDeployabilityStatus(int deploymentId)
    {

        log.debug("Checking deployment plan {} deployability...", deploymentId);

        DeploymentPlan deploymentPlan = deploymentPlanRepository.findById(deploymentId)
                .orElseThrow(() -> new NovaException(BudgetsError.getDeploymentPlanNotFoundError(),
                        MessageFormat.format("Deployment plan {0} not found for checking ability for deploying", deploymentId)));


        DeploymentInfo deploymentInfo = this.getDeploymentInfoFromPlan(deploymentPlan);

        int productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();

        boolean isAbleToDeploy = productBudgetsService.checkDeploymentPlan(deploymentInfo, productId);

        log.debug(isAbleToDeploy ? "Deployment plan {} is able to deploy" : "Deployment plan {} is not able to deploy", deploymentId);

        return isAbleToDeploy;
    }

    /**
     * Check the availability status of a filesystem
     *
     * @param productId   Id of the product
     * @param environment filesystem environment
     * @param packId      Filesystem pack id
     * @return available
     */
    @Override
    public boolean checkFilesystemAvailabilityStatus(int productId, String environment, int packId)
    {

        log.debug("Checking filesystem {} availability...", productId);

        FilesystemInfo filesystemInfo = new FilesystemInfo();
        filesystemInfo.setEnvironment(environment);

        FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setPackId(packId);

        // From now (11/07/2018), anycase always is multiCPD, that means we consider that filesystem has the same cost in one or more CPDs
        filesystemPack.setCpds(1);

        filesystemInfo.setFilesystemPack(filesystemPack);

        boolean available = productBudgetsService.checkFilesystem(filesystemInfo, productId);

        log.debug(available ? "Filesystem is available" : "Filesystem is not available");

        return available;
    }

    /**
     * Check if a product has any service assigned
     *
     * @param productId Id of the product
     * @return true if product has budget
     */
    @Override
    public boolean checkProductBudget(int productId)
    {

        log.debug("Checking product {} budget ...", productId);

        boolean hasBudget = !productBudgetsService.getProductServicesSummary(productId).isEmpty();

        log.debug(hasBudget ? "Product has budget assigned" : "Product has no budget assigned");

        return hasBudget;
    }

    /**
     * Check if a product has at least one of the given services with the given pending status assigned
     *
     * @param productId     Id of the product
     * @param serviceType   Service type
     * @param pendingStatus Service pending status
     * @return exists
     */
    @Override
    public boolean checkProductServices(int productId, GBType serviceType, boolean pendingStatus)
    {

        log.debug("Checking product {} service type {} with {} pending status ...", productId, serviceType.name(),
                pendingStatus);

        boolean exists = false;

        for (BUDGServiceSummaryItem serviceSummary : productBudgetsService.getProductServicesSummary(productId))
        {
            if (pendingStatus == serviceSummary.getPending() && serviceType.name().equals(serviceSummary.getNovaServiceType()))
            {
                exists = true;
            }
        }

        log.debug(exists ? "Product has a service of the given type and status assigned" : "There is no service with the given parameters assigned to that product");

        return exists;
    }

    /**
     * Check if a product has at least one of the given services assigned
     *
     * @param productId   Id of the product
     * @param serviceType Service type
     * @return exists
     */
    @Override
    public boolean checkProductServices(int productId, GBType serviceType)
    {

        log.debug("Checking product {} service type {} ...", productId, serviceType.name());

        boolean exists = false;

        for (BUDGServiceSummaryItem serviceSummary : productBudgetsService.getProductServicesSummary(productId))
        {
            if (serviceType.name().equals(serviceSummary.getNovaServiceType()))
            {
                exists = true;
            }
        }

        log.debug(exists ? "Product has a service of the given type assigned" : "There is no service with the given parameters assigned to that product");

        return exists;
    }

    /**
     * Get the price of a filesystem pack form product budgets
     *
     * @param packId Id of the pack
     * @return pack price
     */
    @Override
    public double getFilesystemPackPrice(int packId)
    {

        log.debug("Getting filesystem pack {} price ...", packId);

        PackInfo packInfo = productBudgetsService.getFilesystemPackInfo(packId);

        double packPrice = packInfo.getPackPrice();

        log.debug("Pack price = {}", packPrice);

        return packPrice;
    }

    /**
     * Get all filesystem packs info from product budgets
     *
     * @return list of pack info
     */
    @Override
    public List<PackInfo> getFilesystemPacks()
    {

        log.debug("Getting all filesystem packs info...");

        List<PackInfo> packsInfo = productBudgetsService.getAllFilesystemPacksInfo();

        log.debug("Packs info got");

        return packsInfo;
    }

    /**
     * Get the price of a hardware pack form product budgets
     *
     * @param packId Id of the pack
     * @return pack price
     */
    @Override
    public double getHardwarePackPrice(int packId)
    {

        log.debug("Getting hardware pack {} price ...", packId);

        PackInfo packInfo = productBudgetsService.getHardwarePackInfo(packId);

        double packPrice = packInfo.getPackPrice();

        log.debug("Pack price = {}", packPrice);

        return packPrice;
    }

    /**
     * Get all hardware packs info from product budgets
     *
     * @return list of pack info
     */
    @Override
    public List<PackInfo> getHardwarePacks()
    {

        log.debug("Getting all hardware packs info...");

        List<PackInfo> packsInfo = productBudgetsService.getAllHardwarePacksInfo();

        log.debug("Packs info got");

        return packsInfo;
    }

    /**
     * Get all product budgets for an environment from product budgets
     *
     * @param productId   Id of the product
     * @param environment Environment
     * @return product budget dto
     */
    @Override
    public BUDGProductBudgetsDTO getProductBudgets(int productId, String environment)
    {

        log.debug("Getting product {} budgets for {} environment ...", productId, environment);

        ProductBudgets budgetsInfo = productBudgetsService.getProductBudgets(productId, environment);

        BUDGProductBudgetsDTO productBudgets = new BUDGProductBudgetsDTO();

        // Ephooenix budget
        BUDGBudgetDTO ephoeniexBudget = new BUDGBudgetDTO();

        BudgetInfo ephoenixBudgetInfo = budgetsInfo.getEphoenixBudget();
        ephoeniexBudget.setAvailableAmount(ephoenixBudgetInfo.getAvailableAmount());
        ephoeniexBudget.setTotalAmount(ephoenixBudgetInfo.getTotalAmount());

        productBudgets.setEphoenixBudget(ephoeniexBudget);

        // Hardware budget
        BUDGBudgetDTO hardwareBudget = new BUDGBudgetDTO();

        BudgetInfo hardwareBudgetInfo = budgetsInfo.getHardwareBudget();
        hardwareBudget.setAvailableAmount(hardwareBudgetInfo.getAvailableAmount());
        hardwareBudget.setTotalAmount(hardwareBudgetInfo.getTotalAmount());

        productBudgets.setHardwareBudget(hardwareBudget);

        // Filesystem budget
        BUDGBudgetDTO filesystemBudget = new BUDGBudgetDTO();

        BudgetInfo filesystemBudgetInfo = budgetsInfo.getFilesystemBudget();
        filesystemBudget.setAvailableAmount(filesystemBudgetInfo.getAvailableAmount());
        filesystemBudget.setTotalAmount(filesystemBudgetInfo.getTotalAmount());

        productBudgets.setFilesystemBudget(filesystemBudget);

        // Broker budget
        BUDGBudgetDTO brokerBudget = new BUDGBudgetDTO();

        BudgetInfo brokerBudgetInfo = budgetsInfo.getBrokerBudget();
        brokerBudget.setAvailableAmount(brokerBudgetInfo.getAvailableAmount());
        brokerBudget.setTotalAmount(brokerBudgetInfo.getTotalAmount());

        productBudgets.setBrokerBudget(brokerBudget);

        log.debug("Budgets got");

        return productBudgets;
    }

    /**
     * Get a detail of all product services
     *
     * @param productId Id of the product
     * @return list of service detail items
     */
    @Override
    public List<BUDGServiceDetailItem> getProductServicesDetail(int productId)
    {
        return productBudgetsService.getProductServicesDetail(productId);
    }

    /**
     * Get a summary of all product services
     *
     * @param productId Id of the product
     * @return list of service summary items
     */
    @Override
    public List<BUDGServiceSummaryItem> getProductServicesSummary(int productId)
    {
        return productBudgetsService.getProductServicesSummary(productId);
    }

    /**
     * Get a detail of a product service
     *
     * @param serviceId Id of the product service
     * @return service detail
     */
    @Override
    public BUDGServiceDetail getServiceDetail(long serviceId)
    {
        return productBudgetsService.getServiceDetail(serviceId);
    }

    /**
     * Synchronize a new deployment plan in product budgets
     *
     * @param deploymentId Id of the deployment plan
     */
    @Override
    public void synchronizePlanDeployment(int deploymentId)
    {

        log.debug("Synchronizing plan {} deployment ...", deploymentId);

        DeploymentPlan deploymentPlan = deploymentPlanRepository.findById(deploymentId).orElseThrow(() ->new NovaException(BudgetsError.getDeploymentPlanNotFoundError(),
                MessageFormat.format("Deployment plan {0} not found for synchronizing plan deployment", deploymentId)));

        NewDeploymentInfo newDeploymentInfo = this.getNewDeploymentInfoFromPlan(deploymentPlan);
        int productId = deploymentPlan.getReleaseVersion().getRelease().getProduct().getId();

        productBudgetsService.insertDeploymentPlan(newDeploymentInfo, productId);

        log.debug("Plan deployment synchronized");
    }

    /**
     * Synchronize an old deployment plan deletion in product budgets
     *
     * @param deploymentId Id of the old deployment plan
     */
    @Override
    public void synchronizePlanUndeployment(int deploymentId)
    {

        log.debug("Synchronizing plan {} undeployment ...", deploymentId);

        productBudgetsService.deleteDeploymentPlan(deploymentId);

        log.debug("Plan undeployment synchronized");
    }

    /**
     * Synchronize an old product deletion in product budgets
     *
     * @param productId product id
     */
    @Override
    public void synchronizeProductDeletion(int productId)
    {

        log.debug("Synchronizing product {} deletetion ...", productId);

        productBudgetsService.deleteProductInfo(productId);

        log.debug("Product deletion synchronized");
    }

    /**
     * Update a product service in product budgets
     *
     * @param updatedService Updated service
     * @param serviceId      Id of the product service
     */
    @Override
    public void updateService(BUDGUpdatedService updatedService, long serviceId, String ivUser)
    {

        this.usersService.checkHasPermission(ivUser, BudgetsConstants.EDIT_BUDGET_PERMISSION, updatedService.getProductId(),
                new NovaException(BudgetsError.getForbiddenError(), "Permission Error"));

        log.debug("Updating service {} in product budgets ...", serviceId);

        UpdatedProductService updatedProductService = new UpdatedProductService();
        updatedProductService.setNewStartDate(updatedService.getUpdatedStartDate());

        productBudgetsService.updateService(updatedProductService, serviceId);

        log.debug("Service updated");
    }

    @Override
    public DateObject calculateFinalDate(DateObject startDate)
    {
        try
        {
            DateObject endDate = new DateObject();
            Date startLocalDate = DateUtils.parseDate(startDate.getDateValue().replaceAll("Z$", "+0000"), BudgetsConstants.DATE_FORMAT_PATTERNS);
            endDate.setDateValue(DateUtils.addDays(startLocalDate, this.getDays(startDate.getDuration(), startLocalDate))
                    .toInstant().toString());

            log.debug("End date {}", endDate.getDateValue());

            return endDate;
        }
        catch (ParseException e)
        {
            log.error("Error parsing date [{}]: [{}]", startDate.getDateValue(), e);
            throw new NovaException(BudgetsError.getInvalidDateFormatError(),
                    MessageFormat.format("Unable to parse string date ''{0}''", startDate.getDateValue()));
        }
    }

    @Override
    public PBProductsUsedResourcesReportDTO getProductsUsedResourcesReport(long[] productIds, String environment)
    {
        return this.productBudgetsService.getProductsUsedResourcesReport(productIds, environment);
    }

    /**
     * Get the DeploymentInfo from a deployment plan
     *
     * @param deploymentPlan deployment plan
     * @return deployment info
     */
    protected DeploymentInfo getDeploymentInfoFromPlan(DeploymentPlan deploymentPlan)
    {
        DeploymentInfo deploymentInfo = new DeploymentInfo();
        deploymentInfo.setEnvironment(deploymentPlan.getEnvironment());
        deploymentInfo.setUndeployReleaseId(0);
        if (deploymentInfo.getEnvironment().contains("PRO") &&
                deploymentPlan.getGcsp() != null &&
                deploymentPlan.getGcsp().getUndeployRelease() > 0
                )
        {
            Optional<DeploymentPlan> undeployPlan = this.deploymentPlanRepository.findById(deploymentPlan.getGcsp().getUndeployRelease());
            undeployPlan.ifPresent(plan -> deploymentInfo.setUndeployReleaseId(plan.getReleaseVersion().getRelease().getId()));
        }

        deploymentInfo.setReleaseId(deploymentPlan.getReleaseVersion().getRelease().getId());

        List<DeploymentPack> hardwarePacks = new ArrayList<>();
        List<DeploymentPack> ephoenixPacks = new ArrayList<>();

        deploymentPlan.getDeploymentSubsystems().forEach(subsystem ->
        {
            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());
            SubsystemType subsystemType = SubsystemType.getValueOf(subsystemDTO.getSubsystemType());

            subsystem.getDeploymentServices().forEach(service ->
            {
                if (service.getHardwarePack() != null && !ServiceType.valueOf(service.getService().getServiceType()).isBatch())
                {
                    DeploymentPack pack = new DeploymentPack();
                    pack.setInstances(service.getNumberOfInstances());
                    pack.setPackId(service.getHardwarePack().getId());

                    switch (subsystemType)
                    {
                        case EPHOENIX:
                            ephoenixPacks.add(pack);
                            break;
                        case NOVA:
                        case FRONTCAT:
                            hardwarePacks.add(pack);
                            break;
                        case BEHAVIOR_TEST:
                            //TODO@behaviorTest not defined budget for behavior services yet.
                            break;
                        default:
                            break;
                    }
                }
            });
        });

        deploymentInfo.setHardwarePacks(hardwarePacks.toArray(new DeploymentPack[0]));
        deploymentInfo.setEphoenixPacks(ephoenixPacks.toArray(new DeploymentPack[0]));

        return deploymentInfo;
    }

    /**
     * Get the NewDeploymentInfo from a deployment plan
     *
     * @param deploymentPlan deployment plan
     * @return new deployment info
     */
    protected NewDeploymentInfo getNewDeploymentInfoFromPlan(DeploymentPlan deploymentPlan)
    {
        NewDeploymentInfo deploymentInfo = new NewDeploymentInfo();
        deploymentInfo.setEnvironment(deploymentPlan.getEnvironment());

        deploymentInfo.setReleaseId(deploymentPlan.getReleaseVersion().getRelease().getId());

        List<DeploymentPack> hardwarePacks = new ArrayList<>();
        List<DeploymentPack> ephoenixPacks = new ArrayList<>();

        deploymentPlan.getDeploymentSubsystems().forEach(subsystem ->
        {
            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystem().getSubsystemId());
            SubsystemType subsystemType = SubsystemType.getValueOf(subsystemDTO.getSubsystemType());

            subsystem.getDeploymentServices().forEach(service ->
            {

                if (service.getHardwarePack() != null && !ServiceType.valueOf(service.getService().getServiceType()).isBatch())
                {
                    DeploymentPack pack = new DeploymentPack();
                    pack.setInstances(service.getNumberOfInstances());
                    pack.setPackId(service.getHardwarePack().getId());

                    switch (subsystemType)
                    {
                        case EPHOENIX:
                            ephoenixPacks.add(pack);
                            break;
                        case NOVA:
                        case FRONTCAT:
                            hardwarePacks.add(pack);
                            break;
                        case BEHAVIOR_TEST:
                            //TODO@behaviorTest not defined budget for behavior services yet.
                            break;
                        default:
                            break;
                    }
                }
            });

        });

        deploymentInfo.setHardwarePacks(hardwarePacks.toArray(new DeploymentPack[0]));
        deploymentInfo.setEphoenixPacks(ephoenixPacks.toArray(new DeploymentPack[0]));

        deploymentInfo.setDeploymentPlanId(deploymentPlan.getId());

        return deploymentInfo;
    }

    /////////////////////////////////////  PRIVATE  //////////////////////////////////////////

    /**
     * Parse a String to LocalDate (Optional format)
     *
     * @param hours       how many hours is the service actived
     * @param fechaInicio fechaInicio   date from the service was activated
     * @return long how many days the service will be activated
     */
    private int getDays(final long hours, final Date fechaInicio)
    {
        // cada 8 horas es un dia
        Date initDate = fechaInicio;

        if (hours <= 8)
        {
            return 1;
        }
        else
        {
            int diasLaborales = Math.toIntExact(hours / 8);
            int diasExtra = 0;

            for (int i = 0; i < diasLaborales; i++)
            {
                initDate = DateUtils.addDays(initDate, 1);
                // si es Sabado o domingo, sumamos uno a los dias, ya que las horas son laborales
                log.debug("fechaInicio_ : " + initDate);
                Calendar c = Calendar.getInstance();
                c.setTime(initDate);
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                // In case the day is saturday or sunday, an extra day is added, because those days are not working days.
                if (dayOfWeek == 1 || dayOfWeek == 7)
                {
                    diasExtra++;
                }
                // Si no es fin de semana, compruebo que si es festivo
                if (this.isHoliday(initDate))
                {
                    diasExtra++;

                }
            }

            log.debug(MessageFormat.format("getDays DiasLaborales  ''{0}'' , Dias Extra ''{1}''  , DiasTotales ''{2}''",
                    diasLaborales, diasExtra, diasLaborales + diasExtra));
            return diasLaborales + diasExtra;
        }
    }

    /**
     * Get all holidays in format -> Key: {month}"-"{dayOfTheMonth}, Value: holiday description
     *
     * @return A map with all holidays values
     */
    private Map<String, String> getHolidays()
    {
        Map<String, String> holidaysMap = new HashMap<>();

        holidaysMap.put("01-01", "New year");
        holidaysMap.put("01-06", "Three wise kings");
        holidaysMap.put("03-30", "Freedom day");
        holidaysMap.put("05-01", "Worker day");
        holidaysMap.put("08-15", "The Assumption");
        holidaysMap.put("10-12", "Hispanity day");
        holidaysMap.put("11-01", "All saints day");
        holidaysMap.put("12-06", "Spanish Constitution day");
        holidaysMap.put("12-08", "Inmaculate Conception day");
        holidaysMap.put("12-25", "Christmas");
        return holidaysMap;
    }

    /**
     * Validate date is holiday or not
     *
     * @param localDate Date in local date format
     * @return true if date is holiday, in other case false
     */
    private Boolean isHoliday(Date localDate)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd");
        String localDateString = simpleDateFormat.format(localDate);
        log.debug("[FormatterService] -> [isHoliday]: LocalDate: {}, Month-DayOfMonth: {}", localDate, localDateString);

        return this.getHolidays().containsKey(localDateString);
    }

    /**
     * Get the price of a broker hardware pack form product budgets
     *
     * @param packId Id of the pack
     * @return pack price
     */
    @Override
    public double getBrokerPackPrice(int packId) throws NovaException {

        log.debug("Getting hardware pack {} price ...", packId);

        PackInfo packInfo = productBudgetsService.getBrokerPackInfo(packId);

        double packPrice = packInfo.getPackPrice();

        log.debug("Pack price = {}", packPrice);

        return packPrice;
    }

}
