package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

import com.bbva.enoa.apirestgen.productsapi.model.EtherConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.InfrastructureConfiguredByEnvDTO;
import com.bbva.enoa.apirestgen.productsapi.model.NovaConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductCommonConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductManagementConfByEnvDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceConfigurationDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import java.util.Map;

public interface IProductConfigService
{
	/**
	 * Save common configuration of a product
	 *
	 * @param product               product
	 * @param commonConfiguration   common configuration to update
	 */
	void updateCommonConfiguration(Product product, ProductCommonConfigurationDTO commonConfiguration);

	/**
	 * Get Product common configuration (General Tab)
	 *
	 * @param product Product entity
	 * @return ProductCommonConfigurationDTO
	 */
	ProductCommonConfigurationDTO getCommonConfigurationFromProductEntity(Product product);

	/**
	 * Get Product Management Configuration
	 *
	 * @param product     product
	 * @param environment environment
	 * @return ProductManagementConfByEnvDTO
	 */
	ProductManagementConfByEnvDTO getManagementConfigurationFromProductEntityByEnv(Product product, Environment environment);

	/**
	 * Update product management configuration
	 *
	 * @param managementConfigurationDTO management configuration
	 * @param product                    product entity
	 * @param environment                environment
	 */
	void updateManagementConfigurationByEnv(ProductManagementConfByEnvDTO managementConfigurationDTO, Product product, Environment environment);

	/**
	 * Get Infrastructure selected in a product
	 *
	 * @param product     product entity
	 * @param environment environment
	 * @return InfrastructureConfiguredByEnvDTO
	 */
	InfrastructureConfiguredByEnvDTO getGeneralInfrastructureConfigFromProductEntityByEnv(Product product, Environment environment);

	/**
	 * Update Infrastructure selected in a product
	 *
	 * @param generalInfrastructureConfig Infrastructure selected
	 * @param product                     product entity
	 * @param environment                 environment
	 */
	void updateGeneralInfrastructureConfigurationByEnv(InfrastructureConfiguredByEnvDTO generalInfrastructureConfig, Product product, Environment environment);

	/**
	 * Get Product Service Configuration (Ces and MGW Log level)
	 *
	 * @param product     product entity
	 * @param environment environment
	 * @return ProductServiceConfigurationDTO
	 */
	ProductServiceConfigurationDTO getServiceConfigurationFromProductEntityByEnv(Product product, Environment environment);

	/**
	 * Product Service Configuration (Ces and MGW Log level)
	 *
	 * @param productServiceConfig service configuration
	 * @param product              product entity
	 * @param environment          environment
	 */
	void updateProductServiceConfigurationByEnv(ProductServiceConfigurationDTO productServiceConfig, Product product, Environment environment);

	/**
	 * Get Nova Specific Configuration
	 *
	 * @param product     product
	 * @param environment environment
	 * @return NovaConfDTO
	 */
	NovaConfigurationDTO getNovaConfigurationFromProductEntity(Product product, Environment environment);

	/**
	 * Update Nova specific configuration
	 *
	 * @param product     product entity
	 * @param env         environment
	 * @param novaConfDTO nova configuration
	 */
	void updateProductNovaInfrastructureConf(Product product, Environment env, NovaConfigurationDTO novaConfDTO);

	/**
	 * Get Ether Specific Configuration
	 *
	 * @param product     product Entity
	 * @param environment environment
	 * @return EtherConfDTO
	 */
	EtherConfigurationDTO getEtherConfigurationFromProductEntity(Product product, Environment environment);

	/**
	 * Update Ether Specific Configuration
	 *
	 * @param product      product entity
	 * @param env          environment
	 * @param etherConfDTO ether configuration
	 */
	void updateProductEtherInfrastructureConf(Product product, Environment env, EtherConfigurationDTO etherConfDTO);

	/**
	 * Get Differences between Old ProductCommonConfigurationDTO and new One
	 *
	 * @param product                       product entity
	 * @param productCommonConfigurationDTO new configuration
	 * @return map with differences and param to emit activity
	 */
	Map<String, Map<String, String>> checkAndReturnCommonConfigurationDifferences(Product product, ProductCommonConfigurationDTO productCommonConfigurationDTO);

	/**
	 * Get Differences between Old ProductManagementConfByEnvDTO and new One
	 *
	 * @param product                       product entity
	 * @param productManagementConfByEnvDTO new configuration
	 * @param environment                   Environment
	 * @return map with differences and param to emit activity
	 */
	Map<String, Map<String, String>> checkAndReturnManagementConfigurationDifferences(Product product, ProductManagementConfByEnvDTO productManagementConfByEnvDTO, Environment environment);

	/**
	 * Get Differences between Old ProductServiceConfigurationDTO and new One
	 *
	 * @param product                        product entity
	 * @param productServiceConfigurationDTO new configuration
	 * @param env                            Environment
	 * @return map with differences and param to emit activity
	 */
	Map<String, Map<String, String>> checkAndReturnProductServiceConfigurationDifferences(Product product, ProductServiceConfigurationDTO productServiceConfigurationDTO, Environment env);

	/**
	 * Get Differences between Old InfrastructureConfiguredByEnvDTO and new One
	 *
	 * @param product                          product entity
	 * @param infrastructureConfiguredByEnvDTO new configuration
	 * @param env                              Environment
	 * @return map with differences and param to emit activity
	 */
	Map<String, Map<String, String>> checkAndReturnInfrastructureConfigurationDifferences(Product product, InfrastructureConfiguredByEnvDTO infrastructureConfiguredByEnvDTO, Environment env);

	/**
	 * Get Differences between Old NovaConfigurationDTO and new One
	 *
	 * @param product              product entity
	 * @param novaConfigurationDTO new configuration
	 * @param env                  Environment
	 * @return map with differences and param to emit activity
	 */
	Map<String, Map<String, String>> checkAndReturnNovaConfigurationDifferences(Product product, NovaConfigurationDTO novaConfigurationDTO, Environment env);

	/**
	 * Get Differences between Old EtherConfigurationDTO and new One
	 *
	 * @param product               product entity
	 * @param etherConfigurationDTO new configuration
	 * @param env                   Environment
	 * @return map with differences and param to emit activity
	 */
	Map<String, Map<String, String>> checkAndReturnEtherConfigurationDifferences(Product product, EtherConfigurationDTO etherConfigurationDTO, Environment env);
}
