package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorInstance;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorAction;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorConfigurationStatus;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorVersionStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionValidator;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BehaviorVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ProductReleaseUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants.MAXIMUM_SIMULTANEOUS_COMPILATIONS;


/**
 * Utility for checking business validation on Behavior operations.
 */
@Slf4j
@Service
public class BehaviorVersionValidatorImpl implements IBehaviorVersionValidator
{

    /**
     * Permission exception
     */
    private static final NovaException PERMISSION_DENIED =
            new NovaException(ReleaseVersionError.getForbiddenError(), ReleaseVersionError.getForbiddenError().toString());

    private static final String BEHAVIOR_VERSION_NAME = "behaviorVersionName";
    private static final String BEHAVIOR_VERSION_DESCRIPTION = "behaviorVersionDescription";

    /**
     * Tools service
     */
    private final IToolsClient toolsClient;

    /**
     * User service client
     */
    private final IProductUsersClient usersClient;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Behavior version repository
     */
    private final BehaviorVersionRepository behaviorVersionRepository;

    /**
     * @param toolsClient               Tools service
     * @param usersClient               Users client
     * @param behaviorVersionRepository Behavior version repository
     */
    @Autowired
    public BehaviorVersionValidatorImpl(final IToolsClient toolsClient,
                                        final IProductUsersClient usersClient,
                                        final INovaActivityEmitter novaActivityEmitter,
                                        final BehaviorVersionRepository behaviorVersionRepository)
    {
        this.toolsClient = toolsClient;
        this.usersClient = usersClient;
        this.novaActivityEmitter = novaActivityEmitter;
        this.behaviorVersionRepository = behaviorVersionRepository;
    }

    @Override
    public void checkProductExistence(final Product product)
    {
        if (product == null)
        {
            throw new NovaException(BehaviorError.getNoSuchProductError());
        }
    }

    @Override
    public void checkProductSubsystems(final Integer productId)
    {
        List<TOSubsystemDTO> subsystemDTOList = this.toolsClient.getProductSubsystems(productId, true);

        if (subsystemDTOList.isEmpty())
        {
            throw new NovaException(BehaviorError.getProductWithoutSubsystemsError(),
                    "Product has no subsystems. Impossible to create a Behavior version.");
        }
    }

    @Override
    public void checkMaxBehaviorVersions(int productId, int behaviorSlots)
    {
        // Get maximum number of release versions
        int maxVersion = ProductReleaseUtils.getMaxReleaseVersions(behaviorSlots);
        int versions = this.behaviorVersionRepository.countByProductIdAndStatusNot(productId, BehaviorVersionStatus.STORAGED);

        if (versions >= maxVersion)
        {
            log.error("[BehaviorVersionValidatorImpl] -> [checkMaxBehaviorVersions]: Reached the maximum of behavior version: Currently - [{}] vs Max admitted [{}]", versions, maxVersion);
            throw new NovaException(BehaviorError.getMaxVersionsLimitError(),
                    "There are too many behavior versions from product with id " + productId + ". Please, archive them");
        }
    }

    @Override
    public void checkCompilingBehaviorVersions(int productId)
    {
        int versionsBuilding = this.behaviorVersionRepository.countByProductIdAndStatus(productId, BehaviorVersionStatus.BUILDING);

        if (versionsBuilding >= MAXIMUM_SIMULTANEOUS_COMPILATIONS)
        {
            log.error("[BehaviorVersionValidatorImpl] -> [checkCompilingBehaviorVersions]: Reached the maximum of simultaneous compilation: currently - [{}] vs Max admitted [{}] ", versionsBuilding, MAXIMUM_SIMULTANEOUS_COMPILATIONS);
            throw new NovaException(BehaviorError.getMaxVersionCompilingError(),
                    "There are too many behavior versions compiling at the same time from product with id " + productId + ". Please, wait" +
                            "till the compiling process ends.");
        }
    }

    @Override
    public void existsBehaviorVersionWithSameName(final Product product, final String versionName)
    {
        for (BehaviorVersion behaviorVersion : product.getBehaviorVersions())
        {
            if (behaviorVersion.getVersionName().equals(versionName))
            {
                log.error("[{}] -> [{}]: There is a behavior version with the same name [{}] in the product", "BehaviorVersionValidatorImpl",
                        "existsReleaseVersionWithSameName", versionName);
                throw new NovaException(BehaviorError.getBehaviorVersionNameDuplicatedError(), "Behavior version name is duplicated");
            }
        }

    }

    @Override
    public BehaviorVersion checkBehaviorVersionExistence(Integer behaviorVersionId)
    {
        BehaviorVersion behaviorVersion = this.behaviorVersionRepository.findById(behaviorVersionId)
                .orElseThrow(() -> new NovaException(BehaviorError.getNoSuchBehaviorVersionError(),
                        "The behavior version id " + behaviorVersionId + " does not exists into NOVA BBDD"));

        log.debug("[{}] -> [{}]: the behavior version id [{}] exist previously. Successfully", "BehaviorVersionValidatorImpl",
                "checkBehaviorVersionExistence", behaviorVersionId);

        return behaviorVersion;
    }

    @Override
    @Transactional
    public void checkBehaviorVersionToDelete(String ivUser, Integer behaviorVersionId)
    {
        // Check behavior version existence and get
        BehaviorVersion behaviorVersion = this.checkBehaviorVersionExistence(behaviorVersionId);

        // Check if given user has permission to current action
        Product product = behaviorVersion.getProduct();
        this.checkProductExistence(product);
        this.usersClient.checkHasPermission(ivUser, Constants.DELETE_BEHAVIOR_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Check behavior version status
        this.checkBehaviorVersionStatus(behaviorVersion);

        // Check behavior subsystem status
        this.checkBehaviorSubsystemStatus(behaviorVersion);

        this.behaviorVersionRepository.delete(behaviorVersion);

        // Emit Delete Release Version Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.BEHAVIOR_VERSION, ActivityAction.DELETED)
                .entityId(behaviorVersionId)
                .addParam(BEHAVIOR_VERSION_NAME, behaviorVersion.getVersionName())
                .addParam(BEHAVIOR_VERSION_DESCRIPTION, behaviorVersion.getDescription())
                .build());
    }

    @Override
    public void validateBehaviorServiceConfigurationExecution(final BehaviorServiceConfiguration bsConfiguration) throws NovaException
    {
        this.checkBehaviorConfigurationStatus(bsConfiguration);
    }

    @Override
    public void validateBehaviorInstanceStop(final BehaviorInstance bInstance) throws NovaException
    {
        //Current configuration
        this.checkBehaviorInstanceStatus(bInstance);
    }

    /**
     * Check behavior version status
     *
     * @param behaviorVersion behavior version
     */
    private void checkBehaviorVersionStatus(BehaviorVersion behaviorVersion)
    {
        if (BehaviorVersionStatus.BUILDING.equals(behaviorVersion.getStatus()))
        {
            log.error("[{}] -> [{}]: There is a behavior version status is [{}]", "BehaviorVersionValidatorImpl",
                    "existsReleaseVersionWithSameName", behaviorVersion.getStatus());
            throw new NovaException(BehaviorError.getBehaviorVersionStateBuildingError(), "Behavior version state is BUILDING");
        }
    }

    /**
     * Check behavior subsystem status
     *
     * @param behaviorVersion behavior version
     */
    private void checkBehaviorSubsystemStatus(BehaviorVersion behaviorVersion)
    {
        behaviorVersion.getSubsystems().forEach(behaviorSubsystem ->
        {
            if (AsyncStatus.PENDING.equals(behaviorSubsystem.getStatus()))
            {
                log.error("[BehaviorVersionValidatorImpl] -> [checkBehaviorVersionToDelete]: There is a behavior version status is [{}]",
                        behaviorSubsystem.getStatus());
                throw new NovaException(BehaviorError.getBehaviorSubsystemStatePendingError(), "Behavior subsystem state is PENDING");
            }

            // Check behavior service status
            this.checkBehaviorServicesStatus(behaviorSubsystem);
        });
    }

    /**
     * Check behavior services status
     *
     * @param behaviorSubsystem behavior subsystem
     */
    private void checkBehaviorServicesStatus(BehaviorSubsystem behaviorSubsystem)
    {
        behaviorSubsystem.getServices().forEach(behaviorService -> behaviorService.getBehaviorServiceConfigurationList().forEach(behaviorServiceConfiguration ->
        {
            if (BehaviorConfigurationStatus.EDITING.equals(behaviorServiceConfiguration.getStatus()))
            {
                log.error("[BehaviorVersionValidatorImpl] -> [checkBehaviorVersionToDelete]: There is a behavior configuration status is [{}]",
                        behaviorServiceConfiguration.getStatus());
                throw new NovaException(BehaviorError.getBehaviorServiceConfigurationEditingError(), "Behavior configuration state is EDITING");
            }

            // Check behavior configuration status
            this.checkBehaviorConfigurationStatus(behaviorServiceConfiguration);
        }));
    }

    /**
     * Check behavior configuration status
     *
     * @param behaviorServiceConfiguration behavior service configuration
     */
    private void checkBehaviorConfigurationStatus(BehaviorServiceConfiguration behaviorServiceConfiguration)
    {
        behaviorServiceConfiguration.getBehaviorInstanceList().forEach(behaviorInstance ->
        {
            if (BehaviorAction.STARTING.equals(behaviorInstance.getAction()))
            {
                log.error("[BehaviorVersionValidatorImpl] -> [checkBehaviorConfigurationStatus]: There is a behavior instance with status [{}]",
                        behaviorInstance.getAction());
                throw new NovaException(BehaviorError.getBehaviorInstanceStateStartingError(), "Behavior instance state is STARTING");
            }
        });
    }

    /**
     * Check behavior instance status.
     *
     * @param behaviorInstance the behavior instance
     */
    private void checkBehaviorInstanceStatus(BehaviorInstance behaviorInstance)
    {
        if (BehaviorAction.STOPPING.equals(behaviorInstance.getAction()))
        {
            log.error("[BehaviorVersionValidatorImpl] -> [checkBehaviorInstanceStatus]: There is a behavior instance with status [{}]",
                    behaviorInstance.getAction());
            throw new NovaException(BehaviorError.getBehaviorInstanceStateStartingError(), "Behavior instance state is STOPPING");
        }
    }
}
