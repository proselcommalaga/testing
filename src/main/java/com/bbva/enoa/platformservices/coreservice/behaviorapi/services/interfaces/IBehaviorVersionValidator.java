package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorInstance;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;


/**
 * Interface for checking business validation on Behavior operations.
 */
public interface IBehaviorVersionValidator
{

    /**
     * Check if the product exists
     *
     * @param product product
     */
    void checkProductExistence(final Product product);

    /**
     * Check if the product has any subsystem created
     *
     * @param productId product Id
     */
    void checkProductSubsystems(final Integer productId);

    /**
     * Check maximum number opf Behavior versions
     *
     * @param productId     product Id
     * @param behaviorSlots behavior slots of product
     */
    void checkMaxBehaviorVersions(int productId, int behaviorSlots);

    /**
     * Check maximum number opf behavior versions
     *
     * @param productId product Id
     */
    void checkCompilingBehaviorVersions(int productId);

    /**
     * Checks if there is another Behavior Version with the same name
     * on the same product.
     *
     * @param product     product of version.
     * @param versionName Behavior version name.
     */
    void existsBehaviorVersionWithSameName(Product product, String versionName);

    /**
     * Check if behavior version does exist
     *
     * @param behaviorVersionId behavior version id
     * @return BehaviorVersion
     */
    BehaviorVersion checkBehaviorVersionExistence(final Integer behaviorVersionId);

    /**
     * Check behavior version to delete
     *
     * @param ivUser            BBVA user code.
     * @param behaviorVersionId The ID of the behavior version.
     */
    void checkBehaviorVersionToDelete(final String ivUser, final Integer behaviorVersionId);

    /**
     * Validate if given behavior service configuration could be executed.
     *
     * @param bsConfiguration behavior service configuration
     */
    void validateBehaviorServiceConfigurationExecution(final BehaviorServiceConfiguration bsConfiguration) throws NovaException;

    /**
     * Validate if given behavior service configuration could be executed.
     *
     * @param bInstance behavior instance
     */
    void validateBehaviorInstanceStop(final BehaviorInstance bInstance) throws NovaException;
}
