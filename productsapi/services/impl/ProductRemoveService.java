package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ScheduleRequestRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ToDoTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILogsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.ICategoryService;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductRemoveService;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class used for the methods dedicated to remove a product Created by XE30432 on 07/02/2017.
 */
@Service
public class ProductRemoveService implements IProductRemoveService
{

    /**
     * LOG entity
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(ProductRemoveService.class);

    private static final NovaException MANAGE_PERMISSION_DENIED = new NovaException(ProductsAPIError.getForbiddenError(), ProductsAPIError.getForbiddenError().toString());

    private static final NovaException USERSERVICE_EXCEPTION = new NovaException(
            ProductsAPIError.getCallToUsersApiError());

    /**
     * Product repository
     */
    private final ProductRepository productRepository;

    /**
     * DeploymentTask repository
     */
    private final DeploymentTaskRepository deploymentTaskRepository;

    /**
     * DeploymentPlan repository
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * To do task Repository
     */
    private final ToDoTaskRepository toDoTaskRepository;

    /**
     * Filesystem repository
     */
    private final FilesystemRepository filesystemsRepository;

    /**
     * Nova API repository
     */
    private final SyncApiRepository syncApiRepository;

    /**
     * Schedule Request Repository
     */
    private final ScheduleRequestRepository scheduleRequestRepository;

    /**
     * Budgets
     */
    private final IBudgetsService budgetsService;

    /**
     * Doc system
     */
    private final IDocSystemService docSystemService;

    /**
     * Mail
     */
    private final MailServiceClient mailService;

    /**
     * Category
     */
    private final ICategoryService categoryService;

    /**
     * Users
     */
    private final IProductUsersClient usersClient;

    /**
     * Tools
     */
    private final ToolsClient toolsService;

    /**
     * Quality
     */
    private final IQualityManagerService qaService;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Logs manager client
     */
    private final ILogsClient logsClient;

    /**
     * All args constructor for dependency injection
     *
     * @param budgetsService           budget service
     * @param docSystemService         Servicio de docSystem
     * @param productRepository        Product repository
     * @param deploymentTaskRepository deployment task
     * @param deploymentPlanRepository deployment plan
     * @param toDoTaskRepository       todotask
     * @param syncApiRepository        novaApi repository
     * @param filesystemsRepository    file system
     * @param mailService              Mail service
     * @param categoryService          category
     * @param usersClient              Userclient
     * @param toolsService             tools
     * @param qaService                QAService
     * @param logsClient               logs manager client
     * @param novaActivityEmitter      NovaActivity emitter
     */
    @Autowired
    public ProductRemoveService(IBudgetsService budgetsService, IDocSystemService docSystemService,
                                ProductRepository productRepository, DeploymentTaskRepository deploymentTaskRepository,
                                DeploymentPlanRepository deploymentPlanRepository, ToDoTaskRepository toDoTaskRepository, SyncApiRepository syncApiRepository,
                                FilesystemRepository filesystemsRepository, ScheduleRequestRepository scheduleRequestRepository, MailServiceClient mailService, ICategoryService categoryService,
                                IProductUsersClient usersClient, ToolsClient toolsService, IQualityManagerService qaService,
                                final ILogsClient logsClient, final INovaActivityEmitter novaActivityEmitter)
    {

        this.budgetsService = budgetsService;
        this.docSystemService = docSystemService;
        this.productRepository = productRepository;
        this.deploymentTaskRepository = deploymentTaskRepository;
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.toDoTaskRepository = toDoTaskRepository;
        this.filesystemsRepository = filesystemsRepository;
        this.syncApiRepository = syncApiRepository;
        this.scheduleRequestRepository = scheduleRequestRepository;
        this.mailService = mailService;
        this.categoryService = categoryService;
        this.usersClient = usersClient;
        this.toolsService = toolsService;
        this.qaService = qaService;
        this.logsClient = logsClient;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    @Override
    public void removeProduct(final String ivUser, final Integer productId)
    {

        this.usersClient.checkHasPermission(ivUser, Constants.DELETE_PRODUCT_PERMISSION, productId, MANAGE_PERMISSION_DENIED);

        // Validate and get the product
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        LOG.debug("ProductsAPI: The following product name [{}] - Product [{}] has been required" + " to be removed",
                productId, product);

        // Secondly, check the product deletion
        this.checkProductDeletion(product);
        LOG.debug("ProductsAPI: products/remove - The product [{}] is ready to be removed", product.getName());

        // Remove log rate threshold events
        logsClient.deleteLogRateThresholdEvents(productId);
        // Remove log Events
        logsClient.deleteLogEvents(productId);

        // Remove references in the NOVA platform
        List<USUserDTO> productMembers = this.usersClient.getProductMembers(productId, USERSERVICE_EXCEPTION);
        removeAllProductReferencesTask(product, ivUser, productMembers);
        LOG.debug("ProductsAPI: products/remove - All the references for the product [{}] in the NOVA platform has been"
                + "removed. ", product.getName());

        // Remove product and all its relations from BBDD
        final List<USUserDTO> productOwnersUsers = this.removeProductFromBBDD(ivUser, product);

        // Remove empty categories (if needed)
        this.categoryService.removeEmptyCategories();

        // Finally send emails notification to the product team
        if (productOwnersUsers != null && !productOwnersUsers.isEmpty())
        {
            this.sendRemoveProductNotification(product, productOwnersUsers);
        }

        // Emit Delete Product Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity.Builder(productId, ActivityScope.PRODUCT, ActivityAction.ELIMINATED)
                .entityId(productId)
                .addParam("uuaa", product.getUuaa())
                .addParam("type", product.getType())
                .build());
    }

    /**
     * Remove product from BBDD
     *
     * @param product product to be removed
     * @return productOwnerUser user to be notified
     */
    @Override
    @Transactional
    public List<USUserDTO> removeProductFromBBDD(final String ivUser, final Product product)
    {

        LOG.debug(
                "[ProductsAPI] -> [removeProductFromBBDD]: Removing all the product users associated to the product: [{}]",
                product.getName());

        // Recovering product owners for mail notification
        List<USUserDTO> productOwners = this.usersClient.getProductUsersByTeam(product.getId(),
                RoleType.PRODUCT_OWNER.getType());

        // Delete all product users
        this.usersClient.deleteProductUsers(ivUser, product.getId());

        LOG.debug("[ProductsAPI] -> [removeProductFromBBDD]: removed from the product team:[{}]", product.getName());

        // Delete Deployment plans associated to the product
        for (Release release : product.getReleases())
        {
            for (ReleaseVersion releaseVersion : release.getReleaseVersions())
            {
                qaService.removeQualityInfo(releaseVersion);
                for (DeploymentPlan deploymentPlan : releaseVersion.getDeployments())
                {
                    deploymentPlan.setConfigurationTask(null);
                    this.deploymentPlanRepository.save(deploymentPlan);
                    List<DeploymentTask> taskList = this.deploymentTaskRepository
                            .findByDeploymentPlanId(deploymentPlan.getId());
                    for (DeploymentTask task : taskList)
                    {
                        this.deploymentTaskRepository.delete(task);
                        LOG.debug(
                                "ProductsAPI: products/remove - Removed deployment taskId: [{}] of the deployment plan ID: [{}] of the release version: [{}]"
                                        + " of the release: [{}]",
                                task.getId(), deploymentPlan.getId(), releaseVersion.getVersionName(),
                                release.getName());
                    }
                    LOG.debug(
                            "ProductsAPI: products/remove - Configuration task of the deployment plan ID: [{}] has been set to null",
                            deploymentPlan.getId());
                }
            }
        }

        // Delete all to do task associated to the product
        List<ToDoTask> tasks = this.toDoTaskRepository.findByProductId(product.getId());
        LOG.debug("ProductsAPI: products/remove - Removing all to do task [{}] associated to the product: [{}]", tasks,
                product.getName());

        for (ToDoTask task : tasks)
        {
            this.toDoTaskRepository.delete(task);
            LOG.debug("ProductsAPI: products/remove - Removed the task [{}] associated to the product: [{}]", task,
                    product.getName());
        }
        this.docSystemService.removeDefaultRepository(product);
        // Delete product from BBDD
        this.productRepository.delete(product);
        LOG.debug("ProductsAPI: products/remove -> Product [{}] has been removed completely from BBDD", product);

        this.synchronizeProductDeletion(product.getId());

        return productOwners;
    }

    /**
     * Send email notification just only the Product Owner of the product
     *
     * @param product            {@link Product}
     * @param productOwnersUsers product team
     */
    @Override
    @Transactional
    public void sendRemoveProductNotification(final Product product, final List<USUserDTO> productOwnersUsers)
    {

        final List<String> productOwnersMailAddresses = new ArrayList<>();

        // Find the product owners of the product and get the email to add it into the 'productOwnersMailAddresses'
        for (final USUserDTO portalUser : productOwnersUsers)
        {
            productOwnersMailAddresses.add(portalUser.getEmail());
        }
        LOG.debug("ProductsAPI: products/remove - The product owners of the productMemberList [{}] found: [{}]. ",
                productOwnersUsers, productOwnersMailAddresses);

        // Send mail notification when the product is removed
        this.mailService.sendDeleteProductNotification(product.getName(), productOwnersMailAddresses);
    }

    /**
     * Call the Tools APIs for removing products references
     *
     * @param product with the product
     */
    private void removeAllProductReferencesTask(final Product product, final String ivUser,
                                                final List<USUserDTO> productMembers)
    {

        // Remove all product references
        removeAllProductReferencesSubTasks(product, ivUser, productMembers);
    }

    /**
     * Remove all the references in the NOVA platform of the product
     *
     * @param product product to remove
     */
    private void removeAllProductReferencesSubTasks(final Product product, final String ivUser,
                                                    final List<USUserDTO> productMembers)
    {
        TOProductUserDTO productUser = new TOProductUserDTO();
        productUser.setProductId(product.getId());
        // force delete without checking Product Owner
        boolean forceDeletion = true;

        for (USUserDTO user : productMembers)
        {
            productUser.setUserCode(user.getUserCode());
            this.toolsService.removeUserTool(productUser, forceDeletion);
        }

        // Remove all the external tools
        this.toolsService.removeExternalToolsFromProduct(product.getId());

        LOG.debug("ProductsAPI: products/remove/ - All reference for the product [{}] in Registry was removed.",
                product);
    }

    /**
     * Check file systems list
     *
     * @param product product to remove
     */
    private void checkFilesystemList(final Product product)
    {

        // Second. Check if has enable filesystem
        List<Filesystem> filesystemList = this.filesystemsRepository.findByProductId(product.getId());
        for (Filesystem filesystem : filesystemList)
        {
            this.checkCreatedFilesystem(product, filesystem);
        }
    }

    /**
     * Check created file system
     *
     * @param product    product to remove
     * @param filesystem filesystem to check
     */
    private void checkCreatedFilesystem(final Product product, final Filesystem filesystem)
    {

        if (filesystem.getFilesystemStatus().equals(FilesystemStatus.CREATED))
        {
            LOG.error(
                    "ProductsAPI: products/remove/ error. The product [{}] cannot be removed because the"
                            + " product has the following filesystem: [{}] in status: [{}]."
                            + " Review this filesystem and change this status.",
                    product.getName(), filesystem.getName(), filesystem.getFilesystemStatus().name());

            throw new NovaException(ProductsAPIError.getFailToRemoveDueActiveFilesystemError(),
                    "ProductsAPI: products/remove/" + product.getName()
                            + " error. The product can not be initiated to remove because" + " the filesystem: ["
                            + filesystem.getName() + "] is still active. Archive or delete it first.");
        }
    }

    /**
     * Check release list
     *
     * @param product product to remove
     */
    private void checkReleaseList(final Product product)
    {

        for (Release release : product.getReleases())
        {
            this.checkVersionList(product, release);
        }
    }

    /**
     * Check version list
     *
     * @param product product to remove
     * @param release release of the product to check
     */
    private void checkVersionList(final Product product, final Release release)
    {

        for (ReleaseVersion releaseVersion : release.getReleaseVersions())
        {
            this.checkPlanList(product, release, releaseVersion);
        }
    }

    /**
     * Check plan list
     *
     * @param product        product to remove
     * @param release        release to check
     * @param releaseVersion release version to check
     */
    private void checkPlanList(final Product product, final Release release, final ReleaseVersion releaseVersion)
    {

        for (DeploymentPlan deploymentPlan : releaseVersion.getDeployments())
        {
            this.checkDeployedStatus(product, release, releaseVersion, deploymentPlan);
            this.checkPendingStatus(product, release, releaseVersion, deploymentPlan);
        }
    }

    /**
     * Check pending status
     *
     * @param product        product to remove
     * @param release        release to check
     * @param releaseVersion release version to chek
     * @param deploymentPlan deployment plan
     */
    private void checkPendingStatus(final Product product, final Release release, final ReleaseVersion releaseVersion,
                                    final DeploymentPlan deploymentPlan)
    {

        // Check deployment task in status = PENDING or PENDING_ERROR
        if (this.deploymentTaskRepository.countPendingAll(deploymentPlan.getId()) > 0)
        {
            LOG.error(
                    "ProductsAPI: products/remove/ error. The product [{}] can not be removed because the"
                            + " product has the following release: [{}] - releaseVersion: [{}] - deploymentPlan: [{}] "
                            + " has PENDING TASKS. Remove it before removing the product.",
                    product.getName(), release.getName(), releaseVersion.getVersionName(), deploymentPlan.getId());
            throw new NovaException(ProductsAPIError.getFailToRemoveDuePendingTaskError(),
                    "ProductsAPI: products/remove/" + product.getName() + " error. The product can not removed because"
                            + " the release: [" + release.getName() + "] - releaseVersion: ["
                            + releaseVersion.getVersionName() + "] - deploymentPlan: [" + deploymentPlan.getId()
                            + "] has PENDING TASK. Remove it before removing the product.");
        }
    }

    /**
     * Check deployed status
     *
     * @param product        product to remove
     * @param release        release to check
     * @param releaseVersion release version to check
     * @param deploymentPlan deployment plan
     */
    private void checkDeployedStatus(final Product product, final Release release, final ReleaseVersion releaseVersion,
                                     final DeploymentPlan deploymentPlan)
    {

        if (deploymentPlan.getStatus() == DeploymentStatus.DEPLOYED)
        {
            LOG.error("ProductsAPI: products/remove/ error. The product [{}] can not be removed because the"
                            + " product has the following release: [{}] - releaseVersion: [{}] - deploymentPlan: [{}] in status: [{}]."
                            + " Review this plan and change this status.", product.getName(), release.getName(),
                    releaseVersion.getVersionName(), deploymentPlan.getId(), deploymentPlan.getStatus().name());
            throw new NovaException(ProductsAPIError.getFailToRemoveDueDeploymentStatusError(),
                    "ProductsAPI: products/remove/" + product.getName() + " error. The product can not removed because"
                            + " the release: [" + release.getName() + "] - releaseVersion: ["
                            + releaseVersion.getVersionName() + "] - deploymentPlan: [" + deploymentPlan.getId()
                            + "] = Status: [" + deploymentPlan.getStatus().name() + "]");
        }
    }

    /**
     * Synchronize a product deletion in product budgets
     *
     * @param productId Id of the product
     */
    private void synchronizeProductDeletion(int productId)
    {

        try
        {
            budgetsService.synchronizeProductDeletion(productId);
        }
        catch (NovaException ex)
        {
            LOG.error("Error synchronizing product deletion", ex);
        }
    }

    /**
     * Validate api list
     *
     * @param product product to validate
     */
    private void checkApiList(final Product product)
    {
        if (!this.syncApiRepository.findAllByProductId(product.getId()).isEmpty())
        {
            LOG.error("ProductsAPI: products/remove/ error. The product [{}] cannot be removed because the"
                    + " product has NOVA apis.", product.getName());
            throw new NovaException(ProductsAPIError.getFailToRemoveDueNovaApiError(),
                    "ProductsAPI: products/remove/" + product.getName() + " error. The product cannot be removed because"
                            + " the product has NOVA apis.");
        }
    }

    /**
     * Validate document list
     *
     * @param product product to validate
     */
    private void checkBatchScheduleList(final Product product)
    {
        if (!this.scheduleRequestRepository.findByProductId(product.getId()).isEmpty())
        {
            LOG.error("ProductsAPI: products/remove/ error. The product [{}] cannot be removed because the"
                    + " product has the product has batch schedule document associated. Delete Batch schedules first.", product.getName());
            throw new NovaException(ProductsAPIError.getFailToRemoveBatchScheduleError(),
                    "ProductsAPI: products/remove/" + product.getName() + " error. The product cannot be removed because"
                            + " the product the product has has batch schedule document associated. Delete Batch schedules first..");
        }
    }

    /**
     * Check if a product can be removed
     *
     * @param product product itself
     */
    private void checkProductDeletion(final Product product)
    {
        // First. Check if the product has some plan of the release deployed in some environmnet
        this.checkReleaseList(product);
        this.checkFilesystemList(product);
        this.checkApiList(product);
        this.checkBatchScheduleList(product);
    }

}
