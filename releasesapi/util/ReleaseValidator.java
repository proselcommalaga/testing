package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseEnvConfigDto;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions.ReleaseError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Utility for checking business validation on Release operations.
 * <p>
 * Created by xe52580 on 13/02/2017.
 */
@Slf4j
@Service
public class ReleaseValidator
{
    /**
     * Release pattern
     */
    private static final String RELEASE_PATTERN = "^[a-zA-Z0-9]*$";

    /**
     * Deployment plan repository
     */
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Release repository
     */
    @Autowired
    private ReleaseRepository releaseRepository;

    /**
     * Checks if Product does exist or not.
     *
     * @param productId Product ID.
     * @param product   Product to check.
     */
    public void checkProductExistence(final int productId, final Product product)
    {
        if (product == null)
        {
            throw new NovaException(ReleaseError.getNoSuchProductError(), "[ReleasesAPI] -> [checkProductExistence]: the product id: [" + productId + "] does not exist into NOVA BBDD");
        }
        else
        {
            log.debug("[ReleasesAPI] -> [checkProductExistence]: the product with ID: [{}] exist. Continue.", productId);
        }
    }

    /**
     * Checks if Product does exist or not.
     *
     * @param product Product to check.
     */
    public void checkProductExistence(final Product product)
    {
        if (product == null)
        {
            throw new NovaException(ReleaseError.getNoSuchProductError());
        }
        else
        {
            log.debug("[ReleasesAPI] -> [checkProductExistence]: the product with ID: [{}] exist. Continue.", product.getId());
        }
    }

    /**
     * Checks if the Release does exist.
     *
     * @param release Release to check.
     */
    public void checkReleaseExistence(final Release release)
    {
        if (release == null)
        {
            throw new NovaException(ReleaseError.getNoSuchReleaseError());
        }
        else
        {
            log.debug("[ReleasesAPI] -> [checkReleaseExistence]: the release name [{}] exist previously. Successfully", release.getName());
        }
    }

    /**
     * Checks if the Release does exist.
     *
     * @param releaseId release identifier to check.
     * @return release
     */
    public Release checkReleaseExistence(final Integer releaseId)
    {
        Release release = this.releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NovaException(ReleaseError.getNoSuchReleaseError(), "[ReleasesAPI] -> [checkReleaseExistence]: the release id: [" + releaseId + "] does not exists into NOVA BBDD"));

        log.debug("[ReleasesAPI] -> [checkReleaseExistence]: the release name [{}] exist previously. Successfully", release.getName());

        return release;

    }

    /**
     * Checks if there is another Release with the same name
     * on the same Product.
     *
     * @param productId   Product ID.
     * @param releaseName Release name.
     */
    public void existsReleaseWithSameName(int productId, String releaseName)
    {
        if (this.releaseRepository.existsReleaseWithSameName(productId, releaseName))
        {
            throw new NovaException(ReleaseError.getReleaseNameDuplicatedError(), "[ReleasesAPI] -> [existsReleaseWithSameName]: the release name: [" + releaseName +"] into the product id: [" + productId + "] is already exits");
        }
        else
        {
            log.debug("[ReleasesAPI] -> [existsReleaseWithSameName]: the release name [{}] does not exist previously. Successfully", releaseName);
        }
    }

    /**
     * Check release name
     *
     * @param releaseName release name
     */
    public void checkReleaseName(final String releaseName)
    {
        if (StringUtils.isEmpty(releaseName) || !releaseName.matches(RELEASE_PATTERN))
        {
            throw new NovaException(ReleaseError.getReleaseNameInvalidError(), "[ReleasesAPI] -> [checkReleaseName]: the release name provided: [" + releaseName + "] contains character not allowed. Patter allowed: [" + RELEASE_PATTERN + "]");
        }
        else
        {
            log.debug("[ReleasesAPI] -> [checkReleaseName]: the release name [{}] has been validated successfully", releaseName);
        }
    }

    /**
     * Check if the input value for Deployment Platform is not null/empty
     *
     * @param availablePlatformDeploy the available platform deploy
     * @param releaseEnvConfig        new input release configuration
     * @param oldValue                the last configured value
     * @param environment             environment
     * @return The new Platform if the platform input data is not empty,         the old value if the input data is empty.
     */
    public Platform checkInputDeploymentPlatformValue(
            List<Platform> availablePlatformDeploy,
            ReleaseEnvConfigDto releaseEnvConfig,
            Platform oldValue,
            String environment)
    {
        if (releaseEnvConfig != null &&
                releaseEnvConfig.getSelectedPlatforms() != null &&
                StringUtils.isNotBlank( releaseEnvConfig.getSelectedPlatforms().getDeploymentPlatform() )
        )
        {
            Platform newValue = getValidDeploymentPlatformValue(
                    releaseEnvConfig.getSelectedPlatforms().getDeploymentPlatform());

            return defaultDeployValidation(availablePlatformDeploy, newValue, environment);
        }
        else
        {
            return oldValue;
        }
    }

    /**
     * Check if the input value for Logging Platform is not null/empty
     *
     * @param availablePlatformLogging available platform logging
     * @param releaseEnvConfig new input release configuration
     * @param oldValue old value
     * @param environment environment
     * @param destinationDeploy destination deploy
     * @return The new Platform if the logging input data is not empty,
     *      the old valueif the input data is empty
     */
    public Platform checkInputLoggingPlatformValue(
            List<Platform> availablePlatformLogging,
            ReleaseEnvConfigDto releaseEnvConfig,
            Platform oldValue,
            Platform destinationDeploy,
            String environment)
    {
        if(releaseEnvConfig != null &&
                releaseEnvConfig.getSelectedPlatforms() != null &&
                StringUtils.isNotBlank( releaseEnvConfig.getSelectedPlatforms().getLoggingPlatform() )
        )
        {
            Platform newValue = getValidLoggingPlatformValue(
                    releaseEnvConfig.getSelectedPlatforms().getLoggingPlatform());
            return defaultLoggingValidation(availablePlatformLogging, newValue, destinationDeploy, environment);
        }
        else
        {
            return oldValue;
        }
    }

    /**
     * Get the enumerator
     * @param value String
     * @return Enum
     */
    public Platform getValidDeploymentPlatformValue(String value)
    {
        try
        {
            return Platform.valueOf(value);

        }
        catch (IllegalArgumentException e)
        {
            throw new NovaException(ReleaseError.getInvalidDeploymentPlatform(), e);
        }
    }

    /**
     * Get the enumerator
     * @param value String
     * @return Enum
     */
    public Platform getValidLoggingPlatformValue(String value)
    {
        try
        {
            return Platform.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            throw new NovaException(ReleaseError.getInvalidLoggingPlatform(), e);
        }
    }

    /**
     * Check that the default value is a valid available value
     * @param availablePlatformDeploy available values
     * @param defaultPlatformDeploy   configured value
     * @return the defaultPlatformDeploy
     */
    private Platform defaultDeployValidation(List<Platform> availablePlatformDeploy,
            Platform defaultPlatformDeploy, String environment)
    {
        // check if defaultPlatformDeploy is inside the list of selected Platforms.
        if (!availablePlatformDeploy.contains(defaultPlatformDeploy))
        {
            throw new NovaException(ProductsAPIError.getForbiddenPlatformDeployError(environment));
        }

        //check if new value is valid to Deploy
        if(!defaultPlatformDeploy.getIsValidToDeploy())
        {
            log.error("[ReleaseValidator] -> [defaultDeployValidation]: Platform [{}] is not available to select Deploy.", defaultPlatformDeploy);
            throw new NovaException(ReleaseError.getInvalidDeploymentPlatform());
        }

        return defaultPlatformDeploy;
    }

    /**
     * Check that the default value is a valid available value
     * @param availablePlatformLogging available values
     * @param defaultPlatformLogging   configured value
     * @return the DestinationPlatformLoggingType
     */
    private Platform defaultLoggingValidation(List<Platform> availablePlatformLogging,
                                                                    Platform defaultPlatformLogging, Platform destinationDeploy, String environment)
    {
        // check if platform selected is valid to logging
        if(!defaultPlatformLogging.getIsValidToLogging())
        {
            log.error("[ReleaseValidator] -> [defaultLoggingValidation]: Platform [{}] is not available to select Logs.", defaultPlatformLogging);
            throw new NovaException(ReleaseError.getInvalidLoggingPlatform());
        }

        // check if PlatformConfig list from product (LOGGING) contains selected value, or selected value is NOVA & ETHER option
        if (!availablePlatformLogging.contains(defaultPlatformLogging))
        {
            // only throw exception if defaultPlatformLogging (value selected) is not NOVAETHER
            if (Platform.NOVAETHER != defaultPlatformLogging)
            {
                throw new NovaException(ProductsAPIError.getForbidenPlatformLoggingError(environment));
            }
        }

        if (destinationDeploy == Platform.ETHER
                && defaultPlatformLogging != Platform.ETHER)
        {
            throw new NovaException(ProductsAPIError.getInvalidLoggingPlatformForSelectedDeploymentPlatformError(defaultPlatformLogging.name(), destinationDeploy.name()));
        }

        return defaultPlatformLogging;
    }
}
