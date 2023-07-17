package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGProductBudgetsDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStorageType;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemPackRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFilesystemManagerClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsValidator;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemsLiteralsConstants;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Utils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Validator for FilesystemsAPI operations.
 */
@Service
public class FilesystemsValidatorImpl implements IFilesystemsValidator
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemsValidatorImpl.class);

    /**
     * Product Repository
     */
    private final ProductRepository productRepository;

    /**
     * Filesystem Repository
     */
    private final FilesystemRepository filesystemRepository;

    /**
     * Filesystem Pack Repository
     */
    private final FilesystemPackRepository filesystemPackRepository;

    /**
     * Broker Repository
     */
    private final BrokerRepository brokerRepository;

    /**
     * Manage Validation utils service
     */
    private final ManageValidationUtils manageValidationUtils;

    /**
     * Nova Context for getting user code
     */
    private final NovaContext novaContext;

    /**
     * Budgets Service
     */
    private final IBudgetsService budgetsService;

    /**
     * Filesystem manager client
     */
    private final IFilesystemManagerClient filesystemManagerClient;

    /**
     * Max allowed filesystems by environment by product
     */
    private int maxAllowedFilesystemsPerEnvAndType;

    /**
     * All args constructor for dependency injection
     *
     * @param productRepository        ProductRepository dependency
     * @param filesystemRepository     FilesystemRepository dependency
     * @param filesystemPackRepository FilesystemPackRepository dependency
     * @param brokerRepository         BrokerRepository dependency
     * @param budgetsService           BudgetsService dependency
     * @param novaContext              the nova context
     * @param manageValidationUtils    Manage validation service dependency
     * @param filesystemManagerClient  the filesystem manager client
     */
    @Autowired
    public FilesystemsValidatorImpl(final ProductRepository productRepository,
                                    final FilesystemRepository filesystemRepository,
                                    final FilesystemPackRepository filesystemPackRepository,
                                    final BrokerRepository brokerRepository,
                                    final IBudgetsService budgetsService,
                                    final NovaContext novaContext,
                                    final ManageValidationUtils manageValidationUtils,
                                    final IFilesystemManagerClient filesystemManagerClient)
    {
        this.productRepository = productRepository;
        this.filesystemRepository = filesystemRepository;
        this.filesystemPackRepository = filesystemPackRepository;
        this.brokerRepository = brokerRepository;
        this.budgetsService = budgetsService;
        this.novaContext = novaContext;
        this.manageValidationUtils = manageValidationUtils;
        this.filesystemManagerClient = filesystemManagerClient;
    }
    ////////////////////////////////////////// IMPLEMENTATIONS /////////////////////////////////////////////////////

    @Override
    public void checkIfFilesystemIsAvailable(final Filesystem filesystem) throws NovaException
    {
        LOG.debug("[FilesystemsAPI] -> [checkIfFilesystemIsAvailable]: checking if the filesystem: [{}] is available", filesystem.getName());
        if (filesystem.getFilesystemStatus() == FilesystemStatus.CREATING || filesystem.getFilesystemStatus() == FilesystemStatus.DELETING)
        {
            String message = MessageFormat.format("[FilesystemsAPI] -> [checkIfFilesystemIsAvailable]: the filesystem name: [{0}] in: [{1}] has the status: [{2}]. "
                    + "Cannot view filesystem detail until the status finished/changed.", filesystem.getName(), filesystem.getEnvironment(), filesystem.getFilesystemStatus());
            LOG.error(message);
            throw new NovaException(FilesystemsError.getFilesystemNotAvailableViewError(filesystem.getId()), message);
        }
        else
        {
            LOG.debug("[FilesystemsAPI] -> [checkIfFilesystemIsAvailable]: checked. The filesystem: [{}] is available", filesystem.getName());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void validateFilesystemCreation(final Product product, final CreateNewFilesystemDto filesystemToAdd) throws NovaException
    {
        Environment environment = Environment.valueOf(filesystemToAdd.getEnvironment());
        this.validateFilesystemStatusAndProduct(product);
        this.validateNameUniqueness(product, environment, filesystemToAdd.getFilesystemName());
        this.validateFilesystemBudget(product.getId(), environment.getEnvironment(), this.validateFilesystemPackCode(filesystemToAdd.getFilesystemPackCode()).getId());
        this.validateMaxPerEnvAndType(product, environment, FilesystemType.valueOf(filesystemToAdd.getFilesystemType()));
        this.validateLandingZoneUniqueness(product, environment, filesystemToAdd.getFilesystemName());
    }

    @Override
    @Transactional(readOnly = true)
    public void validateRestoreFilesystem(final Filesystem filesystem) throws NovaException
    {
        this.validateFilesystemBudget(filesystem.getProduct().getId(), filesystem.getEnvironment(), filesystem.getFilesystemPack().getId());
        this.validateMaxPerEnvAndType(filesystem.getProduct(), Environment.valueOf(filesystem.getEnvironment()), filesystem.getType());
        this.validateLandingZoneUniqueness(filesystem.getProduct(), Environment.valueOf(filesystem.getEnvironment()), filesystem.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public Filesystem validateAndGetFilesystem(final Integer filesystemId) throws NovaException
    {
        Filesystem filesystem = this.filesystemRepository.findById(filesystemId)
                .orElseThrow(() -> new NovaException(
                        FilesystemsError.getNoSuchFilesystemError(filesystemId),
                        MessageFormat.format("[FilesystemsAPI] -> [validateAndGetFilesystem]: the filesystem ID [{0}] does not exists.", filesystemId)));
        LOG.debug("[FilesystemsAPI] -> [validateAndGetFilesystem]: the filesystem: [{}] has been validated.", filesystem.getName());
        return filesystem;
    }

    @Override
    @Transactional(readOnly = true)
    public Product validateAndGetProduct(int productId) throws NovaException
    {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NovaException(
                        FilesystemsError.getNoSuchProductError(productId),
                        MessageFormat.format("[FilesystemsAPI] - > [validateAndGetProduct]: the product ID [{0}] does not exists.", productId)
                ));
        LOG.debug("[FilesystemsAPI] -> [validateAndGetProduct]: the product name: [{}] has been validated.", product.getName());
        return product;
    }

    @Override
    public void validateReservedDirectories(final Filesystem filesystem, final FSFileLocationModel fsFileLocationModel) throws NovaException
    {
        // Must be a directory and just make the validation in the root path for this filesystem = landinzone path
        if (filesystem.getLandingZonePath().equals(fsFileLocationModel.getPath()) &&
                (fsFileLocationModel.getFilename().equals(FilesystemsLiteralsConstants.OUTGOING_DIRECTORY_NAME) ||
                        fsFileLocationModel.getFilename().equals(FilesystemsLiteralsConstants.INCOMING_DIRECTORY_NAME)))
        {
            String message = "[FilesystemsAPI] -> [validateReservedDirectories]: the filesystem directory name: [" + fsFileLocationModel.getFilename()
                    + "] from path: [" + fsFileLocationModel.getPath() + "] cannot be modified or deleted";
            LOG.error(message);
            throw new NovaException(FilesystemsError.getNotAllowedModifyDirectoriesError(filesystem.getId(), fsFileLocationModel.getFilename()), message);
        }

        // Must be a directory not inside or equal to "resources" directory
        String fullDirectoryPath = MessageFormat.format("{0}/{1}/", fsFileLocationModel.getPath(), fsFileLocationModel.getFilename()).replaceAll("/+", "/");
        String reservedDirectory = filesystem.getLandingZonePath() + "/" + FilesystemsLiteralsConstants.RESOURCES_DIRECTORY_NAME + "/";

        if (fullDirectoryPath.startsWith(reservedDirectory))
        {
            String message = "[FilesystemsAPI] -> [validateReservedDirectories]: the filesystem directory name: [" + fsFileLocationModel.getFilename()
                    + "] from path: [" + fsFileLocationModel.getPath() + "] cannot be modified or deleted";
            LOG.error(message);
            throw new NovaException(FilesystemsError.getNotAllowedModifyDirectoriesError(filesystem.getId(), fsFileLocationModel.getFilename()), message);
        }
    }

    @Override
    public void validateNewDirectoryInsideReservedDirectory(final Filesystem filesystem, final String newDirectoryPath)
    {
        if (this.validateInsideReservedDirectory(filesystem, newDirectoryPath))
        {
            int lastIndex = newDirectoryPath.lastIndexOf("/");
            String newDirectory = newDirectoryPath.substring(lastIndex + 1);
            String parentDirectory = newDirectoryPath.substring(0, lastIndex);

            throw new NovaException(FilesystemsError.getCreatingDirectoryInsideReservedOneError(newDirectory, parentDirectory));
        }
    }

    @Override
    public void validateOperationFilesystemObjects(final Filesystem filesystem, final String action) throws NovaException
    {
        if (FilesystemStorageType.OBJECTS.equals(filesystem.getType().getStorageType()))
        {
            throw new NovaException(FilesystemsError.getOperationNotAllowedForFilesystemTypeError(filesystem.getType().getFileSystemType(), action));
        }
    }

    @Override
    public void validateNewFileInsideReservedDirectory(final Filesystem filesystem, final String newFilePath)
    {
        if (this.validateInsideReservedDirectory(filesystem, newFilePath))
        {
            int lastIndex = newFilePath.lastIndexOf("/");
            String filename = newFilePath.substring(lastIndex + 1);
            String parentDirectory = newFilePath.substring(0, lastIndex);
            throw new NovaException(FilesystemsError.getUploadingFileInsideReservedDirectoryError(filename, parentDirectory));
        }
    }

    @Override
    public String validateDirectory(String directory)
    {
        // Add any validation that consider
        return directory.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isFilesystemFrozen(Filesystem filesystem)
    {
        final AtomicBoolean isFileSystemFrozen = new AtomicBoolean(false);

        // Check if the filesystem is on Environment PRE is available (flag automanage must be true or userCode must be Platform admin or SQA admin)
        if (Environment.PRE.getEnvironment().equals(filesystem.getEnvironment()))
        {
            // Check if this file system  has any deployment service being used and the deployment plan is DEPLOYED
            isFileSystemFrozen.set(filesystem.getDeploymentServiceFilesystems().stream()
                    .map(deploymentServiceFilesystem -> deploymentServiceFilesystem.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan())
                    .anyMatch(deploymentPlan -> !this.manageValidationUtils.checkIfPlanCanBeManagedByUser(this.novaContext.getIvUser(), deploymentPlan)
                            && deploymentPlan.getStatus() == DeploymentStatus.DEPLOYED));
            LOG.debug("[FilesystemsValidatorImpl] -> [isFilesystemFrozen]: the filesystem id: [{}] - environment: [{}] is frozen due to the deployment service is being used and " +
                    "deployment plan is deployed", filesystem.getId(), filesystem.getEnvironment());
        }
        else
        {
            LOG.debug("[FilesystemsValidatorImpl] -> [isFilesystemFrozen]: the file system id: [{}] is not on PRE environment. Environment: [{}] frozen always be false",
                    filesystem.getId(), filesystem.getEnvironment());
        }

        return isFileSystemFrozen.get();
    }

    @Override
    public void checkIfFilesystemIsFrozen(Filesystem filesystem) throws NovaException
    {
        if (this.isFilesystemFrozen(filesystem))
        {
            throw new NovaException(FilesystemsError.getActionFrozenError(filesystem.getName(), filesystem.getId(), filesystem.getEnvironment(), this.novaContext.getIvUser()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isFilesystemInUseByServices(Filesystem filesystem)
    {
        final AtomicBoolean isFilesystemInUse = new AtomicBoolean(true);

        isFilesystemInUse.set(filesystem.getDeploymentServiceFilesystems().stream()
                .map(deploymentServiceFilesystem -> deploymentServiceFilesystem.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan())
                .anyMatch(deploymentPlan -> deploymentPlan.getStatus() != DeploymentStatus.STORAGED));

        LOG.debug("[FilesystemsValidatorImpl] -> [isFilesystemInUseByServices]: the filesystem [{}] is currently being used [{}]", filesystem.getId(), isFilesystemInUse.get());

        return isFilesystemInUse.get();
    }

    @Override
    @Transactional(readOnly = true)
    public Boolean isFilesystemInUseByBrokers(Integer filesystemId)
    {
        boolean isFileSystemInUseByBrokers = !this.brokerRepository.findByFilesystemId(filesystemId).isEmpty();

        LOG.debug("[FilesystemsValidatorImpl] -> [isFilesystemInUseByBrokers]: the filesystem [{}] is currently being used [{}]", filesystemId, isFileSystemInUseByBrokers);

        return isFileSystemInUseByBrokers;
    }

    @Override
    public void validateFilesystemNotInUse(Filesystem filesystem) throws NovaException
    {
        if (this.isFilesystemInUseByServices(filesystem))
        {
            throw new NovaException(FilesystemsError.getFilesystemInUseByServicesError(filesystem.getName()));
        }

        if (this.isFilesystemInUseByBrokers(filesystem.getId()))
        {
            throw new NovaException(FilesystemsError.getFilesystemInUseByBrokersError(filesystem.getName()));
        }
    }

    @Override
    public FilesystemPack validatePackToUpdate(Filesystem filesystem, String filesystemPackCode)
    {
        LOG.debug("[FilesystemsValidatorImpl] -> [validatePackToUpdate]: Validating for filesystemId: [{}] pack to update: [{}]", filesystem.getId(), filesystemPackCode);

        // Validate and get FilesystemPackCode
        FilesystemPack filesystemPack = this.validateFilesystemPackCode(filesystemPackCode);

        // Check if filesystemPack code Env is the same as Filesystem to update
        if (!filesystem.getFilesystemPack().getEnvironment().equals(filesystemPack.getEnvironment()))
        {
            LOG.error("[FilesystemsValidatorImpl] -> [validatePackToUpdate]: Error validating filesystem pack code to update, due to different environment from original pack, actual pack:[{}], new pack [{}]",
                    filesystem.getFilesystemPack().getCode(), filesystemPackCode);
            throw new NovaException(FilesystemsError.getFilesystemPackCodeEnvironmentError(filesystemPack.getEnvironment(), filesystem.getEnvironment()));
        }

        LOG.debug("[FilesystemsValidatorImpl] -> [validatePackToUpdate]: Validated and obtained pack for update [{}]", filesystemPackCode);

        return filesystemPack;
    }

    @Override
    public void validateFilesystemIsNovaType(FilesystemType filesystemType)
    {
        if (!FilesystemType.FILESYSTEM.equals(filesystemType))
        {
            LOG.error("[FilesystemsValidatorImpl] -> [validateFilesystemIsNovaType]: Filesystem is not NOVA type, types is: [{}]", filesystemType.getFileSystemType());
            throw new NovaException(
                    FilesystemsError.getUpdateQuotaInvalidFilesystemType(filesystemType.getFileSystemType())
            );
        }
    }

    @Override
    public void checkFilesystemBudget(Filesystem actualFilesystem, FilesystemPack newfilesystemPackCode)
    {
        LOG.debug("[FilesystemsValidatorImpl] -> [checkFilesystemBudget]: Check filesystem budgets for update filesystem [{}] with pack [{}]", actualFilesystem.getId(), newfilesystemPackCode);
        if (actualFilesystem.getFilesystemPack().getSizeMB() < newfilesystemPackCode.getSizeMB())
        {
            // Get actual and new fs cost
            double actualFilesystemPrice = this.budgetsService.getFilesystemPackPrice(actualFilesystem.getFilesystemPack().getId());
            double newFilesystemPrice = this.budgetsService.getFilesystemPackPrice(newfilesystemPackCode.getId());
            LOG.debug("[FilesystemsValidatorImpl] -> [checkFilesystemBudget]: Actual FS prices: [{}], new FS price: [{}]", actualFilesystemPrice, newFilesystemPrice);

            // Get Full product Budgets
            BUDGProductBudgetsDTO productBudgetsDTO = this.budgetsService.getProductBudgets(actualFilesystem.getProduct().getId(), actualFilesystem.getEnvironment());
            double filesystemTotalAmount = productBudgetsDTO.getFilesystemBudget().getTotalAmount();
            double filesystemAvailableAmount = productBudgetsDTO.getFilesystemBudget().getAvailableAmount();
            LOG.debug("[FilesystemsValidatorImpl] -> [checkFilesystemBudget]: Filesystem budget Total amount: [{}], filesystem budget total available: [{}]", filesystemTotalAmount, filesystemAvailableAmount);

            // Check if have enough budget to do the change
            if (newFilesystemPrice > (filesystemAvailableAmount + actualFilesystemPrice))
            {
                LOG.warn("[FilesystemsValidatorImpl] -> [checkFilesystemBudget]: Not enough budgets for  update filesystem [{}] with pack [{}]", actualFilesystem.getId(), newfilesystemPackCode);
                throw new NovaException(
                        FilesystemsError.getNoEnoughFilesystemBudgetError(actualFilesystem.getProduct().getId(), newfilesystemPackCode.getId(), actualFilesystem.getEnvironment())
                );
            }
        }
        LOG.debug("[FilesystemsValidatorImpl] -> [checkFilesystemBudget]: Enough budgets for update filesystem [{}] with pack [{}]", actualFilesystem.getId(), newfilesystemPackCode);
    }

    @Override
    public void validateFilesystemStorage(Filesystem filesystem, FilesystemPack newFilesystemPack)
    {
        LOG.debug("[FilesystemsValidatorImpl] -> [validateFilesystemStorage]: Validating if enough storage for update quota of filesystemId: [{}] with packCode: [{}]", filesystem.getId(), newFilesystemPack.getCode());
        // Only check if new storage size is lower than actual
        if (filesystem.getFilesystemPack().getSizeMB() > newFilesystemPack.getSizeMB())
        {
            // Get storage values
            FSFilesystemUsage fsFilesystemUsage = this.filesystemManagerClient.callGetFileUse(filesystem.getId());

            // Transform storage values in KBs
            Integer fsUsedInKB = Utils.getSizeInKB(fsFilesystemUsage.getUsed());
            Integer newFileSystemSizeInKB = newFilesystemPack.getSizeMB() * 1000;
            LOG.debug("[FilesystemsValidatorImpl] -> [validateFilesystemStorage]: fsUsedInKB: [{}], newFileSystemSizeInKB: [{}]", fsUsedInKB, newFileSystemSizeInKB);

            // Compare storage values
            if (fsUsedInKB > newFileSystemSizeInKB)
            {
                LOG.warn("[FilesystemsValidatorImpl] -> [validateFilesystemStorage]: Not enough space in FS for update filesystem [{}] with pack [{}]", filesystem.getId(), newFilesystemPack);
                throw new NovaException(
                        FilesystemsError.getNotEnoughFreeSpaceToUpdateQuotaError(filesystem.getId(), newFilesystemPack.getCode())
                );
            }
        }
        LOG.debug("[FilesystemsValidatorImpl] -> [validateFilesystemStorage]: Enough storage for update quota of filesystemId: [{}] with packCode: [{}]", filesystem.getId(), newFilesystemPack.getCode());
    }

    @Value("${nova.filesystems.maxPerEnvAndType:2}")
    public void setMaxAllowedFilesystemsPerEnvAndType(int maxAllowedFilesystemsPerEnvAndType)
    {
        this.maxAllowedFilesystemsPerEnvAndType = maxAllowedFilesystemsPerEnvAndType;
    }

    ////////////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////


    private boolean validateInsideReservedDirectory(final Filesystem filesystem, final String path)
    {
        String canonicalPath = path.concat("/").replaceAll("/+", "/");
        String reservedDirectory = filesystem.getLandingZonePath() + "/" + FilesystemsLiteralsConstants.RESOURCES_DIRECTORY_NAME + "/";

        return canonicalPath.startsWith(reservedDirectory);
    }

    /**
     * Validate filesystem pack code
     *
     * @param filesystemPackCode the filesystem pack code name
     */
    FilesystemPack validateFilesystemPackCode(final String filesystemPackCode)
    {
        FilesystemPack filesystemPack = this.filesystemPackRepository.findByCode(filesystemPackCode);

        if (filesystemPack == null)
        {
            throw new NovaException(FilesystemsError.getFilesystemPackCodeError(filesystemPackCode));
        }

        return filesystemPack;
    }

    /**
     * {@link Filesystem} name must be unique on an {@link Environment}.
     *
     * @param product        {@link Product}
     * @param environment    {@link Environment}
     * @param filesystemName {@link Filesystem} name
     */
    void validateNameUniqueness(final Product product, final Environment environment, final String filesystemName)
    {
        // Check if filesystem is duplicated or exists for this product
        if (filesystemRepository.productHasFilesystemWithSameNameOnEnvironment(product.getId(), filesystemName, environment.getEnvironment()))
        {
            String message = MessageFormat.format("[FilesystemsAPI] -> [validateNameUniqueness]: The product [{0}] has a filesystem created with the same name: [{1}] in the environment [{2}]",
                    product.getName(), filesystemName, environment);
            throw new NovaException(FilesystemsError.getDuplicatedFilesystemError(product.getId(), environment.name(), filesystemName), message);
        }
        else
        {
            LOG.debug("[FilesystemsAPI] -> [validateNameUniqueness]: the filesystem name: [{}] for the product: [{}] and ENV: [{}] has been validated successfully.",
                    filesystemName, product.getName(), environment);
        }
    }

    /**
     * Validate if there are this filesystem in this product in CREATING status
     *
     * @param product the product
     */
    void validateFilesystemStatusAndProduct(final Product product)
    {
        List<Filesystem> filesystemList = filesystemRepository.findByProductIdAndFilesystemStatus(product.getId(), FilesystemStatus.CREATING);
        if (!filesystemList.isEmpty())
        {
            String message = MessageFormat.format("[FilesystemsAPI] -> [validateFilesystemStatusAndProduct]: the product with id: [{0}] has already a filesystem in status creating.",
                    product.getId());
            LOG.error(message);
            throw new NovaException(FilesystemsError.getFilesystemNotAvailableCreateError(product.getId()), message);
        }
        else
        {
            LOG.debug("[FilesystemsAPI] -> [validateFilesystemStatusAndProduct]: the product name: [{}] does not have any other filesystem in status CREATING", product.getName());
        }
    }

    /**
     * There can be no more filesystems of type {@code filesystemType} than {@code maxAllowedFilesystemsPerEnvAndType} on the given {@code environment}.
     *
     * @param product        {@link Product}
     * @param environment    {@link Environment}
     * @param filesystemType {@link FilesystemType}
     */
    void validateMaxPerEnvAndType(final Product product, final Environment environment, final FilesystemType filesystemType)
    {
        // A product can't have more than 3 active filesystems on the same environment.
        long currentNumberOfFilesystemsOfTypeInEnv = product.getFilesystems().stream().filter(f -> (f.getEnvironment().equals(environment.getEnvironment())) && (f.getFilesystemStatus() != FilesystemStatus.ARCHIVED)
                && (f.getFilesystemStatus() != FilesystemStatus.CREATE_ERROR) && f.getType() == filesystemType).count();
        if (currentNumberOfFilesystemsOfTypeInEnv >= this.maxAllowedFilesystemsPerEnvAndType)
        {
            String message = MessageFormat.format("[FilesystemsAPI] -> [validateMaxPerEnvAndType]: tried to create more than {0} for product {1} in the environment {2}",
                    maxAllowedFilesystemsPerEnvAndType, product.getName(), environment);
            LOG.error(message);
            throw new NovaException(FilesystemsError.getTooManyFilesystemsError(product.getId(), environment.name(), filesystemType.name()), message);
        }
        else
        {
            LOG.debug("[FilesystemsAPI] -> [validateMaxPerEnvAndType]: the product name: [{}] does not have more than 3 filesystem in this environment: [{}]", product.getName(), environment);
        }
    }

    /**
     * The landing path must be unique in all filesystems of the product and environment,
     * no matter their states.
     *
     * @param product        {@link Product}
     * @param environment    {@link Environment}
     * @param filesystemName {@link Filesystem} name
     */
    void validateLandingZoneUniqueness(final Product product, final Environment environment, final String filesystemName)
    {
        String landingZonePath = "/" + product.getUuaa() + "/" + filesystemName;
        if (filesystemRepository.landingZonePathIsUsed(landingZonePath, environment.getEnvironment()))
        {
            String message = MessageFormat.format("[FilesystemsAPI] -> [validateLandingZoneUniqueness]: tried to create a filesystem for product {0} and environment {1} with a landing zone path already "
                    + "being used: {2}", product.getName(), environment, landingZonePath);
            LOG.error(message);
            throw new NovaException(FilesystemsError.getDuplicatedLandingZonePathError(product.getId(), environment.name(), filesystemName), message);
        }
        else
        {
            LOG.debug("[FilesystemsAPI] -> [validateLandingZoneUniqueness]: the product name: [{}] has landing zones uniques in this environment: [{}] for this filesystem name: [{}]",
                    product.getName(), environment, filesystemName);
        }
    }

    /**
     * Validate a filesystem budget in product budgets
     *
     * @param productId   Id of the product
     * @param environment Filesystem environment
     * @param packId      Id of the pack
     */
    void validateFilesystemBudget(final Integer productId, final String environment, final Integer packId)
    {
        try
        {
            if (!this.budgetsService.checkFilesystemAvailabilityStatus(productId, environment, packId))
            {
                String message = MessageFormat.format("[FilesystemsAPI] -> [validateFilesystemBudget]: Product [{0}] does not have enough money in their filesystem pack [{1}] by the environment:" +
                        " [{2}]", productId, packId, environment);
                LOG.error(message);
                throw new NovaException(FilesystemsError.getNoEnoughFilesystemBudgetError(productId, packId, environment), message);
            }
            else
            {
                LOG.debug("[FilesystemsAPI] -> [validateFilesystemBudget]: the product id: [{}] has budget in this environment: [{}] with pack id: [{}]", productId, environment, packId);
            }
        }
        catch (NovaException ex)
        {
            LOG.error("[FilesystemsAPI] -> [validateFilesystemBudget]: unable to validateServiceProjectFiles filesystem budget: {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}
