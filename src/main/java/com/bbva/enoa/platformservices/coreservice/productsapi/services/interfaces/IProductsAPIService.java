package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

import com.bbva.enoa.apirestgen.productsapi.model.*;
import com.bbva.enoa.datamodel.model.product.entities.Product;

/**
 * Task service client
 *
 * @author BBVA - XE30432
 */
public interface IProductsAPIService
{
    /**
     * Update project key
     *
     * @param productId  product identifier
     * @param projectKey project key
     * @param ivUser     iv user
     */
    void updateProjectKey(final Integer productId, final String projectKey, final String ivUser);

    /**
     * Creates a new product
     *
     * @param createNewProduct new product info
     * @return the product created
     */
    Product createNewProduct(ProductRequestDTO createNewProduct);

    /**
     * Returns all NOVA products
     *
     * @return array with all NOVA products
     */
    ProductDTO[] getAllProducts();

    /**
     * Returns all the products with a given status
     *
     * @param statusCode the status
     * @param type       filter could be NOVA or LIBRARY
     * @return the list of products
     */
    ProductDTO[] getAllProductsByStatus(final String statusCode, final String type);

    /**
     * Returns all the products filter by type and user in case of exist
     *
     * @param type     filter could be NOVA or LIBRARY
     * @param userCode filter user
     * @return the list of products
     */
    ProductDTO[] getProducts(final String type, final String userCode);

    /**
     * Edits a product information
     *
     * @param productId the product id
     * @param status    status
     * @return product info
     */
    Product updateProductStatus(Integer productId, String status);

    /**
     * Gets the information for a product
     *
     * @param productId product id
     * @return the product information
     */
    ProductSummaryDTO getProductSummary(Integer productId);

    /**
     * Adds an user to a product
     *
     * @param ivUser    logged user
     * @param productId product id
     * @param userCode  user to add
     * @param role      role for user
     */
    void addUserToProduct(String ivUser, Integer productId, String userCode, String role);

    /**
     * Removes an user from a product
     *
     * @param ivUser    logged user
     * @param productId product id
     * @param userCode  user to remove
     */
    void removeUserFromProduct(String ivUser, Integer productId, String userCode);

    /**
     * Get common product configuration
     *
     * @param productId product ID
     * @return ProductCommonConfigurationDTO
     */
    ProductCommonConfigurationDTO getCommonConfiguration(Integer productId);

    /**
     * Update common product configuration
     *
     * @param ivUser              logged user
     * @param commonConfiguration common configuration to update
     * @param productId           product id
     */
    void updateCommonConfiguration(String ivUser, ProductCommonConfigurationDTO commonConfiguration, Integer productId);

    /**
     * Gets base information for a product
     *
     * @param productId The unique product id
     * @return the product information
     */
    ProductBaseDTO getBaseProduct(Integer productId);

    /**
     * Get all the UUAAs stored in the platform.
     *
     * @return An array containing all the UUAAs stored in the platform.
     */
    String[] getUuaas();

    /**
     * Get all the UUAAs NOVA/Ephoenix Legacy
     *
     * @return An array containing all the UUAAs NOVA/Ephoenix Legacy
     */
    String[] getUuaasNovaEphoenixLegacy();

    /**
     * Get all the UUAAs Ephoenix Legacy
     *
     * @return An array containing all the UUAAs Ephoenix Legacy
     */
    String[] getUuaasEphoenixLegacy();

    /**
     * Get Type of the product. The values could be LIBRARY or NOVA
     *
     * @return An array containing Type of the product
     */
    String[] getTypes();

    /**
     * This is called when a product is created using Ether's console and a Tech Spec "NOVA".
     * NOVA creates a product or updates it accordingly.
     *
     * @param ivUser                                  logged user
     * @param etherConsoleProductCreationNotification Contains the information needed to create or update a product.
     */
    void createFromEtherConsole(String ivUser, EtherConsoleProductCreationNotificationDTO etherConsoleProductCreationNotification);

    /**
     * Updates GIAM's group (Ether) stored in a product.
     *
     * @param ivUser                Logged user.
     * @param etherGIAMProductGroup Product group in GIAM (Ether).
     * @param productId             The ID of the product to update.
     */
    void updateEtherGIAMProductGroup(String ivUser, String etherGIAMProductGroup, Integer productId);

    /**
     * Find the Product for the given Release Version Service Id
     *
     * @param releaseVersionServiceId The Id of the Release Version Service
     * @return The ProductBaseDTO which the given Release Version Service Id belongs to
     */
    ProductBaseDTO findProductByReleaseVersionServiceId(Integer releaseVersionServiceId);

    /**
     * Find the Product for the given Behavior Version Service Id
     *
     * @param behaviorVersionServiceId The Id of the Behavior Version Service
     * @return The ProductBaseDTO which the given Behavior Version Service Id belongs to
     */
    ProductBaseDTO findProductByBehaviorVersionServiceId(Integer behaviorVersionServiceId);

    /**
     * Find the Product for the given Release Version Id
     *
     * @param releaseVersionId The Id of the Release Version
     * @return The ProductBaseDTO which the given Release Version Id belongs to
     */
    ProductBaseDTO findProductByReleaseVersionId(Integer releaseVersionId);

    /**
     * Find the Product for the given UUAA
     *
     * @param uuaa The UUAA of the Product
     * @return The ProductBaseDTO for the given UUAA
     */
    ProductBaseDTO findProductByUUAA(String uuaa);

    /**
     * Find the Product for the given UUAA
     *
     * @param uuaa The UUAA of the Product
     * @return The {@link ProductSummaryDTO} for the given UUAA
     */
    ProductSummaryDTO findProductSummaryByUUAA(String uuaa);

    /**
     * Get all products count
     *
     * @return products count in long
     */
    Long getProductsCount();

    /**
     * Get All Product Configuration by environment
     * (Infrastructures selected, Management and ServiceConf)
     *
     * @param productId   product id
     * @param environment configuration environment
     */
    ProductConfigurationByEnvDTO getProductConfigurationByEnvironment(Integer productId, String environment);

    /**
     * Update Product Management Configuration
     *
     * @param managementConfigurationDTO Product management configuration
     * @param productId                  product ID
     * @param environment                environment
     */
    void updateManagementConfigurationByEnv(String ivUser, ProductManagementConfByEnvDTO managementConfigurationDTO, Integer productId, String environment);

    /**
     * Update Infrastructure selected in a Product
     *
     * @param generalInfrastructureConfig Infrastructure selected
     * @param productId                   product ID
     * @param environment                 environment
     */
    void updateGeneralInfrastructureConfigurationByEnv(String ivUser, InfrastructureConfiguredByEnvDTO generalInfrastructureConfig, Integer productId, String environment);

    /**
     * Update Product Service Configuration (Ces and MGW Log level)
     *
     * @param productServiceConfig Product Service configuration
     * @param productId            product ID
     * @param environment          environment
     */
    void updateProductServiceConfigurationByEnv(String ivUser, ProductServiceConfigurationDTO productServiceConfig, Integer productId, String environment);

    /**
     * Get Nova Specific Configuration
     *
     * @param productId   product ID
     * @param environment environment
     * @return NovaConfDTO
     */
    NovaConfigurationDTO getNovaInfrastructureConfByEnv(Integer productId, String environment);

    /**
     * Update Nova Specific Configuration
     *
     * @param novaConfigurationDTO nova configuration
     * @param productId            product ID
     * @param environment          environment
     */
    void updateNovaInfrastructureConfByEnv(String ivUser, NovaConfigurationDTO novaConfigurationDTO, Integer productId, String environment);

    /**
     * Get Ether Specific Configuration
     *
     * @param productId   product ID
     * @param environment environment
     * @return EtherConfDTO
     */
    EtherConfigurationDTOResponse getEtherInfrastructureConfByEnv(Integer productId, String environment);

    /**
     * Update Ether Specific Configuration
     *
     * @param etherConfigurationDTO Ether configuration
     * @param productId             product ID
     * @param environment           environment
     */
    void updateEtherInfrastructureConfByEnv(String ivUser, EtherConfigurationDTO etherConfigurationDTO, Integer productId, String environment);

}
