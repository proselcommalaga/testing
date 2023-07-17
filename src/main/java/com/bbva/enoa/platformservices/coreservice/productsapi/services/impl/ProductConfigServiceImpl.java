package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.productsapi.model.EtherConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.InfrastructureConfigDTO;
import com.bbva.enoa.apirestgen.productsapi.model.InfrastructureConfiguredByEnvDTO;
import com.bbva.enoa.apirestgen.productsapi.model.NovaConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductCommonConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductDeploymentConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductManagementConfByEnvDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceConfigurationDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.LogLevel;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductConfigService;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProductConfigServiceImpl implements IProductConfigService
{
	/**
	 * Logger
	 */
    private static final Logger LOG = LoggerFactory.getLogger(ProductConfigServiceImpl.class);


	private final ProductRepository productRepository;
    private final CPDRepository cpdRepository;


	@Autowired
    public ProductConfigServiceImpl(final ProductRepository pProductRepository, final CPDRepository pCpdRepository)
	{
		this.productRepository = pProductRepository;
		this.cpdRepository = pCpdRepository;
	}

	@Override
	public void updateCommonConfiguration(final Product product, final ProductCommonConfigurationDTO commonConfiguration)
	{
		LOG.debug("[ProductConfigServiceImpl] -> [updateCommonConfiguration]: Updating common configuration [{}]", commonConfiguration);
		if (commonConfiguration != null)
		{
			product.setDevelopment(commonConfiguration.getDevelopment());
			product.setQualityLevel(commonConfiguration.getQualityLevel());
			product.setCriticalityLevel(commonConfiguration.getCriticalityLevel());
			product.setBatchLimitRepeat(commonConfiguration.getBatchLimitRepeat());
			if(commonConfiguration.getReleaseSlots() > 4 || commonConfiguration.getReleaseSlots() < 2)
			{
				throw new NovaException(ProductsAPIError.getInvalidReleaseSlotsNumberError());
			}
			else
			{
				product.setReleaseSlots(commonConfiguration.getReleaseSlots());
			}

			LOG.debug("[ProductConfigServiceImpl] -> [updateCommonConfiguration] Updating product common configuration: productId [{}]", product.getId());

			//Persist the product
			this.productRepository.save(product);
		}
	}

	@Override
	public ProductCommonConfigurationDTO getCommonConfigurationFromProductEntity(Product product)
	{
		ProductCommonConfigurationDTO productCommonConfigurationDTO = new ProductCommonConfigurationDTO();
		productCommonConfigurationDTO.setCriticalityLevel(product.getCriticalityLevel());
		productCommonConfigurationDTO.setDevelopment(product.getDevelopment());
		productCommonConfigurationDTO.setQualityLevel(product.getQualityLevel());
		productCommonConfigurationDTO.setReleaseSlots(product.getReleaseSlots());
		productCommonConfigurationDTO.setBatchLimitRepeat(product.getBatchLimitRepeat());


		return productCommonConfigurationDTO;
	}

	@Override
	public ProductManagementConfByEnvDTO getManagementConfigurationFromProductEntityByEnv(Product product, Environment environment)
	{
		ProductManagementConfByEnvDTO productManagementConfByEnvDTO = new ProductManagementConfByEnvDTO();

		switch (environment)
		{
			case INT:
				//No Management configuration in INT environment
				LOG.info("[ProductConfigServiceImpl] -> [getManagementConfigurationFromProductEntityByEnv]: there is no Management Configuration available in environment [INT].");
				break;
			case PRE:
				productManagementConfByEnvDTO.setDefaultAutodeploy(product.getDefaultAutodeployInPre());
				productManagementConfByEnvDTO.setDefaultAutomanage(product.getDefaultAutomanageInPre());
				break;
			case PRO:
				productManagementConfByEnvDTO.setDefaultAutodeploy(product.getDefaultAutodeployInPro());
				productManagementConfByEnvDTO.setDefaultAutomanage(product.getDefaultAutomanageInPro());
				productManagementConfByEnvDTO.setDefaultDeploymentTypeInPro(product.getDefaultDeploymentTypeInPro().getType());
				break;
		}

		return productManagementConfByEnvDTO;
	}

	@Override
	public void updateManagementConfigurationByEnv(ProductManagementConfByEnvDTO managementConfigurationDTO, Product product, Environment environment)
	{
		LOG.debug("[ProductConfigServiceImpl] -> [updateManagementConfigurationByEnv]: Updating management configuration in envrionment [{}] for product [{}]", environment.getEnvironment(),
				product.getId());
		switch (environment)
		{
			case INT:
				//No Management configuration in INT environment
				LOG.debug("[ProductConfigServiceImpl] -> [updateManagementConfigurationByEnv]: There is no management information for environment [INT].");
				break;
			case PRE:
				product.setDefaultAutodeployInPre(managementConfigurationDTO.getDefaultAutodeploy());
				product.setDefaultAutomanageInPre(managementConfigurationDTO.getDefaultAutomanage());
				break;
			case PRO:
				product.setDefaultAutodeployInPro(managementConfigurationDTO.getDefaultAutodeploy());
				product.setDefaultAutomanageInPro(managementConfigurationDTO.getDefaultAutomanage());
				product.setDefaultDeploymentTypeInPro(CommonsFunctions.validateDeploymentType(managementConfigurationDTO.getDefaultDeploymentTypeInPro()));
				break;
		}

		this.productRepository.save(product);
		LOG.debug("[ProductConfigServiceImpl] -> [updateManagementConfigurationByEnv]: Success! Updated management configuration in envrionment [{}] for product [{}]", environment.getEnvironment(),
				product.getId());
	}

	@Override
	public InfrastructureConfiguredByEnvDTO getGeneralInfrastructureConfigFromProductEntityByEnv(Product product, Environment environment)
	{
		InfrastructureConfiguredByEnvDTO infrastructureConfiguredByEnvDTO = new InfrastructureConfiguredByEnvDTO();

		infrastructureConfiguredByEnvDTO.setDeployInfra(this.mapPlatformConfigListToInfrastructureConfigDTO(product.getPlatformConfigListByEnv(environment.getEnvironment()), ConfigurationType.DEPLOY));
		infrastructureConfiguredByEnvDTO.setLoggingInfra(this.mapPlatformConfigListToInfrastructureConfigDTO(product.getPlatformConfigListByEnv(environment.getEnvironment()), ConfigurationType.LOGGING));

		return infrastructureConfiguredByEnvDTO;
	}

	@Override
	public void updateGeneralInfrastructureConfigurationByEnv(InfrastructureConfiguredByEnvDTO generalInfrastructureConfig, Product product, Environment environment)
	{
		LOG.debug("[ProductConfigServiceImpl] -> [updateGeneralInfrastructureConfigurationByEnv]: Updating infrastructure selected in product with productID [{}]", product.getId());
		// validate only one Deploy Infrastructure is selected
		defaultPlatformValidation(generalInfrastructureConfig.getDeployInfra(), environment.toString(), ConfigurationType.DEPLOY);

		// validate only one Logging Infrastructure is selected
		defaultPlatformValidation(generalInfrastructureConfig.getLoggingInfra(), environment.toString(), ConfigurationType.LOGGING);

		// Get current configuration and compare/update with new one.
		updatePlatformConfig(
				product,
				mapProductDeploymentConfigurationDTOToPlatformConfigEntity(generalInfrastructureConfig, environment, product.getId()),
				environment
		);

		this.productRepository.saveAndFlush(product);
		LOG.debug("[ProductConfigServiceImpl] -> [updateGeneralInfrastructureConfigurationByEnv]: Infrastructure selected in product [{}] is updates successfully", product.getId());
	}

	@Override
	public ProductServiceConfigurationDTO getServiceConfigurationFromProductEntityByEnv(Product product, Environment environment)
	{
		ProductServiceConfigurationDTO productServiceConfigurationDTO = new ProductServiceConfigurationDTO();

		switch (environment)
		{
			case INT:
				productServiceConfigurationDTO.setMicrogwLogLevel(product.getMgwLogLevelInt().name());
				productServiceConfigurationDTO.setCesEnabled(product.isCesEnabledInt());
				break;
			case PRE:
				productServiceConfigurationDTO.setMicrogwLogLevel(product.getMgwLogLevelPre().name());
				productServiceConfigurationDTO.setCesEnabled(product.isCesEnabledPre());
				break;
			case PRO:
				productServiceConfigurationDTO.setMicrogwLogLevel(product.getMgwLogLevelPro().name());
				productServiceConfigurationDTO.setCesEnabled(product.isCesEnabledPro());
				break;
		}
		return productServiceConfigurationDTO;
	}

	@Override
	public void updateProductServiceConfigurationByEnv(ProductServiceConfigurationDTO productServiceConfig, Product product, Environment environment)
	{
		switch (environment)
		{
			case INT:
				product.setMgwLogLevelInt(LogLevel.valueOf(productServiceConfig.getMicrogwLogLevel()));
				product.setCesEnabledInt(productServiceConfig.getCesEnabled());
				break;
			case PRE:
				product.setMgwLogLevelPre(LogLevel.valueOf(productServiceConfig.getMicrogwLogLevel()));
				product.setCesEnabledPre(productServiceConfig.getCesEnabled());
				break;
			case PRO:
				product.setMgwLogLevelPro(LogLevel.valueOf(productServiceConfig.getMicrogwLogLevel()));
				product.setCesEnabledPro(productServiceConfig.getCesEnabled());
				break;
		}

		this.productRepository.saveAndFlush(product);
	}

	@Override
	public void updateProductNovaInfrastructureConf(Product product, Environment env, NovaConfigurationDTO novaConfDTO)
	{

		if(Environment.PRO == env)
		{
			product.setMultiCPDInPro(novaConfDTO.getMulticpd());
			product.setCPDInPro(this.cpdRepository.getByNameAndEnvironmentAndMainSwarmCluster(novaConfDTO.getCpd(), Environment.PRO.getEnvironment(), true));
		}
		this.productRepository.saveAndFlush(product);
	}

	@Override
	public void updateProductEtherInfrastructureConf(Product product, Environment env, EtherConfigurationDTO etherConfDTO)
	{
		String namespace = etherConfDTO.getNamespace();
		switch (env)
		{
			case INT:
				validateEtherConfiguration(
						Stream.of(namespace, product.getEtherNsPre(), product.getEtherNsPro()));
				product.setEtherNsInt(namespace);
				break;
			case PRE:
				validateEtherConfiguration(
						Stream.of(namespace, product.getEtherNsInt(), product.getEtherNsPro()));
				product.setEtherNsPre(namespace);
				break;
			case PRO:
				validateEtherConfiguration(
						Stream.of(namespace, product.getEtherNsInt(), product.getEtherNsPre()));
				product.setEtherNsPro(namespace);
				break;
		}
		this.productRepository.save(product);
	}

	@Override
	public Map<String, Map<String, String>> checkAndReturnCommonConfigurationDifferences(Product product, ProductCommonConfigurationDTO productCommonConfigurationDTO)
	{
		LOG.debug("[ProductConfigServiceImpl] -> [checkAndReturnCommonConfigurationDifferences]: getting Common Configuration differences for product [{}]", product.getId());
		final Map<String, Map<String, String>> differences = new HashMap<>();
		ProductCommonConfigurationDTO oldCommonConfiguration = this.getCommonConfigurationFromProductEntity(product);

		if (!oldCommonConfiguration.getDevelopment().equals(productCommonConfigurationDTO.getDevelopment()))
		{
			differences.put("Development", Map.of(oldCommonConfiguration.getDevelopment().toString(), productCommonConfigurationDTO.getDevelopment().toString()));
		}
		if (!oldCommonConfiguration.getCriticalityLevel().equals(productCommonConfigurationDTO.getCriticalityLevel()))
		{
			differences.put("CriticalityLevel", Map.of(oldCommonConfiguration.getCriticalityLevel().toString(), productCommonConfigurationDTO.getCriticalityLevel().toString()));
		}
		if (!oldCommonConfiguration.getQualityLevel().equals(productCommonConfigurationDTO.getQualityLevel()))
		{
			differences.put("QualityLevel", Map.of(oldCommonConfiguration.getQualityLevel().toString(), productCommonConfigurationDTO.getQualityLevel().toString()));
		}
		if (!oldCommonConfiguration.getReleaseSlots().equals(productCommonConfigurationDTO.getReleaseSlots()))
		{
			differences.put("ReleaseSlots", Map.of(oldCommonConfiguration.getReleaseSlots().toString(), productCommonConfigurationDTO.getReleaseSlots().toString()));
		}
		if (!oldCommonConfiguration.getBatchLimitRepeat().equals(productCommonConfigurationDTO.getBatchLimitRepeat()))
		{
			differences.put("BatchLimitRepeat", Map.of(oldCommonConfiguration.getBatchLimitRepeat().toString(), productCommonConfigurationDTO.getBatchLimitRepeat().toString()));
		}
		LOG.debug("[ProductConfigServiceImpl] -> [checkAndReturnCommonConfigurationDifferences]: Saved differences Common Configuration differences for product [{}]", product.getId());

		return differences;
	}

	@Override
	public Map<String, Map<String, String>> checkAndReturnManagementConfigurationDifferences(Product product, ProductManagementConfByEnvDTO productManagementConfByEnvDTO, Environment env)
	{
		final Map<String, Map<String, String>> differences = new HashMap<>();
		ProductManagementConfByEnvDTO oldProductManagementConfByEnvDTO = this.getManagementConfigurationFromProductEntityByEnv(product, env);

		if (!oldProductManagementConfByEnvDTO.getDefaultAutodeploy().equals(productManagementConfByEnvDTO.getDefaultAutodeploy()))
		{
			differences.put("DefaultAutodeploy", Map.of(oldProductManagementConfByEnvDTO.getDefaultAutodeploy().toString(), productManagementConfByEnvDTO.getDefaultAutodeploy().toString()));
		}
		if (!oldProductManagementConfByEnvDTO.getDefaultAutomanage().equals(productManagementConfByEnvDTO.getDefaultAutomanage()))
		{
			differences.put("DefaultAutomanage", Map.of(oldProductManagementConfByEnvDTO.getDefaultAutomanage().toString(), productManagementConfByEnvDTO.getDefaultAutomanage().toString()));
		}
		if (Environment.PRO.equals(env) && !oldProductManagementConfByEnvDTO.getDefaultDeploymentTypeInPro().equals(productManagementConfByEnvDTO.getDefaultDeploymentTypeInPro()))
		{
			differences.put("DefaultDeploymentTypeInPro", Map.of(oldProductManagementConfByEnvDTO.getDefaultDeploymentTypeInPro(),
					productManagementConfByEnvDTO.getDefaultDeploymentTypeInPro()));
		}

		return differences;
	}

	@Override
	public Map<String, Map<String, String>> checkAndReturnProductServiceConfigurationDifferences(Product product, ProductServiceConfigurationDTO productServiceConfigurationDTO, Environment env)
	{
		final Map<String, Map<String, String>> differences = new HashMap<>();
		ProductServiceConfigurationDTO oldProductServiceConfigurationDTO = this.getServiceConfigurationFromProductEntityByEnv(product, env);

		if (!oldProductServiceConfigurationDTO.getCesEnabled().equals(productServiceConfigurationDTO.getCesEnabled()))
		{
			differences.put("CesEnabled", Map.of(oldProductServiceConfigurationDTO.getCesEnabled().toString(), productServiceConfigurationDTO.getCesEnabled().toString()));
		}
		if (!oldProductServiceConfigurationDTO.getMicrogwLogLevel().equals(productServiceConfigurationDTO.getMicrogwLogLevel()))
		{
			differences.put("MicrogwLogLevel", Map.of(oldProductServiceConfigurationDTO.getMicrogwLogLevel(), productServiceConfigurationDTO.getMicrogwLogLevel()));
		}

		return differences;
	}

	@Override
	public Map<String, Map<String, String>> checkAndReturnInfrastructureConfigurationDifferences(Product product, InfrastructureConfiguredByEnvDTO infrastructureConfiguredByEnvDTO, Environment env)
	{
		final Map<String, Map<String, String>> differences = new HashMap<>();
		InfrastructureConfiguredByEnvDTO oldInfrastructureConfiguredByEnvDTO = this.getGeneralInfrastructureConfigFromProductEntityByEnv(product, env);
		String newPlatformDeploy = Arrays.stream(infrastructureConfiguredByEnvDTO.getDeployInfra())
				.filter(InfrastructureConfigDTO::getIsEnabled)
				.map(InfrastructureConfigDTO::getInfrastructureName)
				.collect(Collectors.joining(",","", "."));
		String oldPlatformDeploy = Arrays.stream(oldInfrastructureConfiguredByEnvDTO.getDeployInfra())
				.filter(InfrastructureConfigDTO::getIsEnabled)
				.map(InfrastructureConfigDTO::getInfrastructureName)
				.collect(Collectors.joining(",","", "."));
		String newPlatformLog = Arrays.stream(infrastructureConfiguredByEnvDTO.getLoggingInfra())
				.filter(InfrastructureConfigDTO::getIsEnabled)
				.map(InfrastructureConfigDTO::getInfrastructureName)
				.collect(Collectors.joining(",","", "."));
		String oldPlatformLog = Arrays.stream(oldInfrastructureConfiguredByEnvDTO.getLoggingInfra())
				.filter(InfrastructureConfigDTO::getIsEnabled)
				.map(InfrastructureConfigDTO::getInfrastructureName)
				.collect(Collectors.joining(",","", "."));

		differences.put("Deploy Infra", Map.of(oldPlatformDeploy, newPlatformDeploy));
		differences.put("Logging Infra", Map.of(oldPlatformLog, newPlatformLog));

		return differences;
	}

	@Override
	public Map<String, Map<String, String>> checkAndReturnNovaConfigurationDifferences(Product product, NovaConfigurationDTO novaConfigurationDTO, Environment env)
	{
		final Map<String, Map<String, String>> differences = new HashMap<>();

		//Old configuration
		NovaConfigurationDTO oldNovaConfigurationDTO = this.getNovaConfigurationFromProductEntity(product, env);

		if (!oldNovaConfigurationDTO.getMulticpd().equals(novaConfigurationDTO.getMulticpd()))
		{
			differences.put("Multicpd", Map.of(oldNovaConfigurationDTO.getMulticpd().toString(), novaConfigurationDTO.getMulticpd().toString()));
		}

		if (Environment.PRO.equals(env) && oldNovaConfigurationDTO.getCpd() != null && !oldNovaConfigurationDTO.getCpd().equals(novaConfigurationDTO.getCpd()))
		{
			if (novaConfigurationDTO.getMulticpd())
			{
				differences.put("Cpd", Map.of(oldNovaConfigurationDTO.getCpd(), "All CPDs"));
			}
			else
			{
				differences.put("Cpd", Map.of(oldNovaConfigurationDTO.getCpd(), novaConfigurationDTO.getCpd()));
			}

		}

		return differences;
	}

	@Override
	public Map<String, Map<String, String>> checkAndReturnEtherConfigurationDifferences(Product product, EtherConfigurationDTO etherConfigurationDTO, Environment env)
	{
		final Map<String, Map<String, String>> differences = new HashMap<>();
		EtherConfigurationDTO oldEtherConfigurationDTO = this.getEtherConfigurationFromProductEntity(product, env);

		if (oldEtherConfigurationDTO.getNamespace() == null || Objects.equals(oldEtherConfigurationDTO.getNamespace(), ""))
		{
			differences.put("Namespace", Map.of("", etherConfigurationDTO.getNamespace()));
		}

		if (oldEtherConfigurationDTO.getNamespace() != null && !oldEtherConfigurationDTO.getNamespace().equalsIgnoreCase(etherConfigurationDTO.getNamespace()))
		{
			differences.put("Namespace", Map.of(oldEtherConfigurationDTO.getNamespace(), String.valueOf(Optional.ofNullable(etherConfigurationDTO.getNamespace()))));
		}

		return differences;
	}

	/**
	 * Get specific NOVA configuration from Product Entity
	 *
	 * @param product     product entity
	 * @param environment environment
	 * @return NovaConfDTO
	 */
	@Override
	public NovaConfigurationDTO getNovaConfigurationFromProductEntity(Product product, Environment environment)
	{
		NovaConfigurationDTO novaConfDTO = new NovaConfigurationDTO();

		if(Environment.PRO == environment)
		{
			novaConfDTO.setMulticpd(product.getMultiCPDInPro());
			if (product.getCPDInPro() != null)
			{
				novaConfDTO.setCpd(product.getCPDInPro().getName());
			}
		}
		return novaConfDTO;
	}

	/**
	 * Get specific Ether configuration from Product Entity
	 *
	 * @param product     product entity
	 * @param environment environment
	 * @return EtherCongDTO
	 */
	@Override
	public EtherConfigurationDTO getEtherConfigurationFromProductEntity(Product product, Environment environment)
	{
		EtherConfigurationDTO etherConfDTO = new EtherConfigurationDTO();
		switch (environment)
		{
			case INT:
				etherConfDTO.setNamespace(product.getEtherNsInt());
				break;
			case PRE:
				etherConfDTO.setNamespace(product.getEtherNsPre());
				break;
			case PRO:
				etherConfDTO.setNamespace(product.getEtherNsPro());
				break;
		}
		return etherConfDTO;
	}




//	####################### PRIVATE METHODS ###########################

	private void validateDeploymentConfiguration(final ProductDeploymentConfigurationDTO[] deploymentConfigurations)
	{
		Arrays.stream(deploymentConfigurations).forEach(deploymentConfiguration -> {
			if (Environment.valueOf(deploymentConfiguration.getEnvironment()).equals(Environment.PRO) &&
					(deploymentConfiguration.getCpd() == null || deploymentConfiguration.getCpd().trim().isEmpty()) &&
					Boolean.TRUE.equals(!deploymentConfiguration.getMulticpd()) )
			{
				throw new NovaException(ProductsAPIError.getNoCPDsetForMonoCPDInPro());
			}
		});

	}

	/**
	 * validate that namespace is unique
	 * @param namespaces Stream of namespaces.
	 */
	private void validateEtherConfiguration(final Stream<String> namespaces)
    {

        final boolean deploymentNamespacesAreDifferent = namespacesAreDifferent(namespaces);

        if (deploymentNamespacesAreDifferent)
        {
			throw new NovaException(ProductsAPIError.getDuplicatedDeployNamespaceError());
        }
    }


    private boolean namespacesAreDifferent(final Stream<String> namespaces)
    {
        final Map<String, Long> nsCount = namespaces
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return nsCount.entrySet().stream()
                .anyMatch(entry -> entry.getValue() > 1);
    }


	/**
	 * Validate default platform selected in DTO.
	 * For LOGGING, could be 3 possibilities: NOVA, ETHER, NOVA & ETHER
	 * For DEPLOY, only could be: NOVA, ETHER, AWS
	 *
	 * @param infrastructureConfigDTOArray InfrastructureConfigDTO array
	 * @param environment                  environment
	 * @param configurationType            ConfigurationType (DEPLOY or LOGGING)
	 */
	private void defaultPlatformValidation(final InfrastructureConfigDTO[] infrastructureConfigDTOArray, String environment, ConfigurationType configurationType)
	{
		LOG.debug("[ProductConfigServiceImpl] -> [defaultPlatformValidation]: Validating default Platform selected at [{}] at [{}] environment.", configurationType, environment);

		long count = Arrays.stream(infrastructureConfigDTOArray).filter(config -> config.getIsEnabled() && config.getIsDefault()).count();

		if (count > 2 || count == 0)
		{
			LOG.error("[ProductConfigServiceImpl] -> [defaultPlatformValidation]: validation failed. [{}] do not have selected well default platform at [{}] environment.", configurationType, environment);
			switch (configurationType)
			{
				case DEPLOY:
					throw new NovaException(ProductsAPIError.getForbiddenPlatformDeployError(environment));
				case LOGGING:
					throw new NovaException(ProductsAPIError.getForbidenPlatformLoggingError(environment));
			}
		}
	}

	/**
	 * Map InfrastructureConfigDTO to PlatformConfig entity
	 *
	 * @param productId         product that is being configuring
	 * @param platform          DTO with new configuration
	 * @param configurationType type of configuration {@link ConfigurationType}
	 * @param env               environment that infrastructureConfigDTO belongs to.
	 * @param isDefaultSelected if is default or not.
	 */
	private PlatformConfig buildPlatformConfigEntity(Integer productId, String platform, ConfigurationType configurationType, Environment env, Boolean isDefaultSelected)
	{
		PlatformConfig platformConfig = new PlatformConfig();
		try
		{
			platformConfig.setProductId(productId);
			platformConfig.setConfigurationType(configurationType);
			platformConfig.setPlatform(Platform.valueOf(platform));
			platformConfig.setEnvironment(env.getEnvironment());
			platformConfig.setIsDefault(isDefaultSelected);
		}
		catch (IllegalArgumentException e)
		{
			LOG.error("[ProductConfigServiceImpl] -> [buildPlatformConfigEntity]: error converting [{}] to Platform Type.", platform);
			throw new NovaException(ProductsAPIError.getInvalidPlatformType(platform));
		}

		return platformConfig;
	}

	/**
	 * Method to save all PlatformConfig list associate with product.
	 * Check what values of new configuration exist and take it, and add new ones.
	 * Old values not present in new list are deleted
	 *
	 * @param product                    product
	 * @param updatingPlatformConfigList list with new information to be save
	 * @param env                        environment
	 */
	private void updatePlatformConfig(Product product, List<PlatformConfig> updatingPlatformConfigList, Environment env)
	{
		LOG.debug("[ProductConfigServiceImpl] -> [updatePlatformConfig]: checking existing PlatformConfig list and compare with new one");
		List<PlatformConfig> existingPlatformConfigList = product.getPlatformConfigList();

		//update values that are the same.
		updatingPlatformConfigList.forEach(
				updatedPlatformConfig ->
						existingPlatformConfigList.stream()
								.filter(existPlatformConfig ->
										existPlatformConfig.getPlatform().equals(updatedPlatformConfig.getPlatform())
												&& existPlatformConfig.getConfigurationType().equals(updatedPlatformConfig.getConfigurationType())
												&& existPlatformConfig.getEnvironment().equals(updatedPlatformConfig.getEnvironment())
								)
								.findFirst().ifPresent(presentConfig -> updatedPlatformConfig.setId(presentConfig.getId()))
		);

		LOG.debug("[ProductConfigServiceImpl] -> [updatePlatformConfig]: Adding other environment configurations.");
		//Add other environments configurations.
		existingPlatformConfigList.forEach(
				otherEnvConf ->
				{
					if (!otherEnvConf.getEnvironment().equals(env.getEnvironment()))
					{
						updatingPlatformConfigList.add(otherEnvConf);
					}
				}
		);

		// Clear old list
		product.getPlatformConfigList().clear();
		// save new Configuration
		product.getPlatformConfigList().addAll(updatingPlatformConfigList);
		LOG.debug("[ProductConfigServiceImpl] -> [updatePlatformConfig]: Success updating product PlatformConfigList with new values and old ones that have not modifications.");
	}

	/**
	 * Private method to map Request InfrastructureConfiguredByEnvDTO to Platform Config entity
	 *
	 * @param generalInfrastructureConfig InfrastructureConfiguredByEnvDTO
	 * @param env                         environment
	 * @param productId                   product id
	 * @return list Platform Config entity
	 */
	private List<PlatformConfig> mapProductDeploymentConfigurationDTOToPlatformConfigEntity(InfrastructureConfiguredByEnvDTO generalInfrastructureConfig, Environment env, Integer productId)
	{

		LOG.debug("[ProductConfigServiceImpl] -> [mapProductDeploymentConfigurationDTOToPlatformConfigEntity]: mapping InfrastructureConfiguredByEnvDTO to Platform Config DTO");

		List<PlatformConfig> platformConfigList = new ArrayList<>();

		// map All Deploy infrastructures
		for (InfrastructureConfigDTO configDeployDTO : generalInfrastructureConfig.getDeployInfra())
		{
			if (configDeployDTO.getIsEnabled())
			{

				PlatformConfig platformConfigDeploy =
						buildPlatformConfigEntity(productId, configDeployDTO.getInfrastructureName(), ConfigurationType.DEPLOY, env, configDeployDTO.getIsDefault());
				platformConfigList.add(platformConfigDeploy);
			}
		}

		// map All Logging infrastructures
		for (InfrastructureConfigDTO configLoggingDTO : generalInfrastructureConfig.getLoggingInfra())
		{
			if (configLoggingDTO.getIsEnabled())
			{
				PlatformConfig platformConfigLogging =
						buildPlatformConfigEntity(productId, configLoggingDTO.getInfrastructureName(), ConfigurationType.LOGGING, env, configLoggingDTO.getIsDefault());
				platformConfigList.add(platformConfigLogging);
			}
		}
		LOG.debug("[ProductConfigServiceImpl] -> [mapProductDeploymentConfigurationDTOToPlatformConfigEntity]: Success! Mapping InfrastructureConfiguredByEnvDTO to Platform Config DTO");

		return platformConfigList;
	}

	/**
	 * Map PlatformConfigList to InfrastructureConfigDTO.
	 *
	 * @param productPlatformConfigList Product PlatformConfigList already filter by env
	 * @param configurationType         configuration Type (DEPLOY or LOGGING)
	 * @return InfrastructureConfigDTO array
	 */
	private InfrastructureConfigDTO[] mapPlatformConfigListToInfrastructureConfigDTO(List<PlatformConfig> productPlatformConfigList, ConfigurationType configurationType)
	{
		// Get DEPLOY or LOGGING platforms
		List<PlatformConfig> platformConfigList =
				productPlatformConfigList
						.stream()
						.filter(
								platformConfig -> configurationType.equals(platformConfig.getConfigurationType()))
						.collect(Collectors.toList());

		// For each para poner el enable o no.
		List<String> platforms = EnumSet.allOf(Platform.class)
				.stream()
				.map(Platform::getName)
				.collect(Collectors.toList());

		//List to return with all Platforms checked
		List<InfrastructureConfigDTO> resultList = new ArrayList<>();

		// For each platform in Platform Enum, check if is on PlatformConfigList.
		platforms.forEach(
				platform ->
				{
					InfrastructureConfigDTO infrastructureConfigDTO = new InfrastructureConfigDTO();
					infrastructureConfigDTO.setInfrastructureName(platform);
					if (checkIfPlatformIsPresentInPlatformConfigList(platform, platformConfigList))
					{
						// means it is configured
						boolean isDefault =
								platformConfigList.stream().filter(platformConfig -> platform.equalsIgnoreCase(platformConfig.getPlatform().getName())).findFirst().get().getIsDefault();
						infrastructureConfigDTO.setIsDefault(isDefault);
						infrastructureConfigDTO.setIsEnabled(true);
					}
					else
					{
						// mean it is not configured
						infrastructureConfigDTO.setIsEnabled(false);
						infrastructureConfigDTO.setIsDefault(false);
					}
					resultList.add(infrastructureConfigDTO);
				}
		);

		return resultList.toArray(InfrastructureConfigDTO[]::new);
	}

	/**
	 * Method that check if one specific platform is on a list.
	 *
	 * @param platform           platform string
	 * @param platformConfigList Product PlatformConfigList
	 * @return boolean
	 */
	private boolean checkIfPlatformIsPresentInPlatformConfigList(String platform, List<PlatformConfig> platformConfigList)
	{
		return platformConfigList.stream().map(PlatformConfig::getPlatform).map(Platform::getName).collect(Collectors.toList()).contains(platform);
	}
}
