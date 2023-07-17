package com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces;

import com.bbva.enoa.apirestgen.etherapi.model.EtherConfigStatusDTO;
import com.bbva.enoa.apirestgen.etherapi.model.EtherDeploymentServiceInventoryDto;
import com.bbva.enoa.apirestgen.etherapi.model.EtherLandingZoneInventoryDto;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;

import java.util.List;

/**
 * IEtherService
 *
 * @author David Ramirez
 */
public interface IEtherService
{
	/**
	 * Returns the configuration status for the environment (int, pre, pro) and type (deploy or logging) selected
	 * @param productId    - productId
	 * @param environment  - int, pre or pro
	 * @return             - The searched configuration
	 */
	EtherConfigStatusDTO[] getEtherConfigurationStatus(Integer productId, String environment);

	/**
	 * Checks if ether resources are ready to deploy
	 *
	 * @param productId product id
	 * @param rawEnvironment environment (int, pre or pro)
	 * @return true if it is ready
	 */
	boolean isReadyToDeploy(final Integer productId, final String rawEnvironment);

	/**
	 * Execute the configuration for the environment (int, pre, pro) and type (deploy or logging) selected
	 *
	 * @param ivUser - ivUser
	 * @param environment  - int, pre or pro
	 * @param productId    - productId
	 * @param namespace    - The namespace for the given product, environment and configuration type.
	 * @return - The final configuration, of the error if it exists.
	 */
	EtherConfigStatusDTO configureEtherInfrastructure(String ivUser, String environment,
			Integer productId, String namespace);

	/**
	 * Add an user in the namespace
	 *
	 * @param users     - user to add
	 * @param product      - product
	 * @throws Errors the errors
	 */

	void addUsersToGroup(List<String> users, Product product) throws Errors;

	/**
	 * Add the product members of a product to the product group in GIAM
	 *
	 * @param product The product.
	 */
    void addProductMembersToGiamProductGroup(Product product);

    /**
	 * Delete an user in the namespace
	 *
	 * @param users     - user to delete
	 * @param product      - product
	 * @throws Errors the errors
	 */
	void removeUsersFromGroup(List<String> users, Product product) throws Errors;

    /**
     * Gets the list of deployment services deployed in Ether. If an environment is specified, the result is limited
     * to this environment, otherwise all deployment services are returned
     *
     * @param novaMetadata Nova metadata
     * @param environment  Environment (optional)
     * @return List of {@link EtherDeploymentServiceInventoryDto}
     */
	EtherDeploymentServiceInventoryDto[] getDeploymentServicesByEnvironment(NovaMetadata novaMetadata, String environment);

    /**
     * Gets deployment services deployed in Ether related with de instance id specified.
     *
     * @param instanceId - the id of the instance related with de service to recover
     * @return {@link EtherDeploymentServiceInventoryDto}
     */
    EtherDeploymentServiceInventoryDto getDeploymentServiceByInstanceId(Integer instanceId);

	/**
	 * Gets the EtherLandingZone deployed
	 *
	 * @param environment  filtering by environment (nullable)
	 * @param uuaa         filtering by uuaa (nullable)
	 * @return An array of EtherLandingZone
	 */
    EtherLandingZoneInventoryDto[] getEtherLandingZones(String environment, String uuaa);
}
