package com.bbva.enoa.platformservices.coreservice.etherapi.services.impl;

import com.bbva.enoa.apirestgen.etherapi.model.EtherConfigStatusDTO;
import com.bbva.enoa.apirestgen.etherapi.model.EtherDeploymentServiceInventoryDto;
import com.bbva.enoa.apirestgen.etherapi.model.EtherLandingZoneInventoryDto;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckEtherResourcesStatusRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.CheckReadyToDeployRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherConfigurationRequestDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherManagerConfigStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherUserManagementDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductConfigurationDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.util.ParallelUtils;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEther;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentInstanceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.etherapi.exceptions.EtherError;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IDeploymentServiceInventoryDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IEtherService;
import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants;
import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherValidator;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductsAPIService;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.common.Constants.IMMUSER;
import static com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants.ENVIRONMENTS_TO_CHECK;

/**
 * EtherServiceImpl
 *
 * @author David Ramirez
 */
@Slf4j
@Service
public class EtherServiceImpl implements IEtherService
{
	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(EtherServiceImpl.class);

	/**
	 * Forbidden error
	 */
	private final NovaException PERMISSION_DENIED = new NovaException(EtherError.getDoesNotHavePermissionsError(EtherConstants.PRODUCT_ETHER_CONFIGURATION_MANAGEMENT));

	private final ProductRepository productRepository;
	private final EtherValidator validator;
	private final IUsersClient usersClient;
	private final IEtherManagerClient etherManagerClient;
	private final DeploymentPlanRepository deploymentPlanRepository;
	private final FilesystemRepository filesystemRepository;
	private final IDeploymentServiceInventoryDtoBuilder deploymentServiceInventoryDtoBuilder;
	private final DeploymentInstanceRepository deploymentInstanceRepository;
	private final IProductsAPIService productsAPIService;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    private final Executor asyncExecutor;

	@Autowired
	public EtherServiceImpl(
			final ProductRepository pProductRepository,
			final EtherValidator pValidator, final IUsersClient pUsersClient,
			final IEtherManagerClient pEtherManagerClient, final DeploymentPlanRepository deploymentPlanRepository,
			final FilesystemRepository filesystemRepository, final IDeploymentServiceInventoryDtoBuilder deploymentServiceInventoryDtoBuilder,
			final DeploymentInstanceRepository deploymentInstanceRepository,
			@Lazy final IProductsAPIService productsAPIService,
			final INovaActivityEmitter novaActivityEmitter,
			@Qualifier("taskExecutor") final Executor asyncExecutor)
	{
		this.productRepository = pProductRepository;
		this.validator = pValidator;
		this.usersClient = pUsersClient;
		this.etherManagerClient = pEtherManagerClient;
		this.deploymentPlanRepository = deploymentPlanRepository;
		this.filesystemRepository = filesystemRepository;
		this.deploymentServiceInventoryDtoBuilder = deploymentServiceInventoryDtoBuilder;
		this.deploymentInstanceRepository = deploymentInstanceRepository;
		this.productsAPIService = productsAPIService;
        this.novaActivityEmitter = novaActivityEmitter;
		this.asyncExecutor = asyncExecutor;
	}

	@Override
	public EtherConfigStatusDTO[] getEtherConfigurationStatus(final Integer productId, final String pEnvironment)
	{
		// Validate params
		final Environment environment = validator.checkValidEnvironment(pEnvironment);

		//Get the product
		final Product product = findByIdAndValidateProduct(productId);

		// Create all the required requests
		final List<CheckEtherResourcesStatusRequestDTO> productEtherConfigurationRequests =
				prepareStatusRequests(product, environment);

		// Call to the Ether manager and return results
		return getEtherConfigurationStatusInParallel(productId, productEtherConfigurationRequests);
	}

	@Override
	public boolean isReadyToDeploy(final Integer productId, final String rawEnvironment)
	{
		final Environment environment = validator.checkValidEnvironment(rawEnvironment);

		final Product product = findByIdAndValidateProduct(productId);

		final CheckReadyToDeployRequestDTO checkReadyToDeployRequestDTO = new CheckReadyToDeployRequestDTO();
		checkReadyToDeployRequestDTO.setEnvironment(environment.name());
		checkReadyToDeployRequestDTO.setNamespace(PlatformUtils.getSelectedDeployNSForProductInEnvironment(product, Environment.valueOf(environment.name())));
		checkReadyToDeployRequestDTO.setGroupId(product.getEtherGIAMProductGroup());
		checkReadyToDeployRequestDTO.setProductName(product.getName());

		boolean ready = false;

		try
		{
			ready = this.etherManagerClient.readyToDeploy(checkReadyToDeployRequestDTO);
		}
		catch (Exception e)
		{
			LOG.warn("[EtherAPI] -> [isReadyToDeploy] Error while checking if ether resources are ready for deployment of product [{}]", productId);
		}

		return ready;
	}

	@Override
	public EtherConfigStatusDTO configureEtherInfrastructure(final String ivUser,
			final String pEnvironment,
			final Integer productId, final String namespace)
	{
		// Checking permissions for user and call queries service
		this.usersClient.checkHasPermission(ivUser, EtherConstants.PRODUCT_ETHER_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

		// Check environment is informed and correct.
		Environment environment = validator.checkValidEnvironment(pEnvironment);
		this.validator.checkNamespaceFormat(namespace);

		//Get the product
		Product product = findByIdAndValidateProduct(productId);

		final ProductConfigurationDTO productConfigurationDTO = productsAPIService.getProductSummary(productId).getConfiguration();

		Arrays.stream(productConfigurationDTO.getDeployment())
				.filter(deploymentConfigurationDTO -> this.validator.checkValidEnvironment(deploymentConfigurationDTO.getEnvironment()).equals(environment))
				.findFirst()
				.orElseThrow(() -> new NovaException(EtherError.getUnexpectedError(String.format("Deployment configuration for environment [%s] in product [%d] is not set", environment, productId))))
				.setEtherNs(namespace);

		// Build request DTO
		EtherConfigurationRequestDTO etherConfigurationRequestDTO = new EtherConfigurationRequestDTO();
		etherConfigurationRequestDTO.setUuaa(product.getUuaa());
		etherConfigurationRequestDTO.setNamespace(namespace);
		etherConfigurationRequestDTO.setGroupId(product.getEtherGIAMProductGroup());
		etherConfigurationRequestDTO.setEnvironment(environment.name());
		etherConfigurationRequestDTO.setProductName(product.getName());
		etherConfigurationRequestDTO.setTeamMembers(this.getProductMembersUserCodes(product));

		LOG.debug("[EtherAPI] -> [configureEtherInfrastructure] Calling to etherManager : "
						+ "productId [{}] NS [{}], environment [{}]",
				productId, namespace, environment);

		// Call to EtherManager
		EtherManagerConfigStatusDTO managerResult =
				this.etherManagerClient.configureEtherInfrastructure(etherConfigurationRequestDTO);

		this.productsAPIService.updateEtherGIAMProductGroup(IMMUSER, managerResult.getGroupId(), productId);

		LOG.debug("[EtherAPI] -> [configureEtherInfrastructure] Finished call to etherManager: "
						+ "productId [{}] NS [{}], environment [{}], ether manager result [{}]",
				productId, namespace, environment, managerResult.getStatus());

		// Translate the EtherManagerConfigStatusDTO to EtherConfigStatusDTO
		EtherConfigStatusDTO result = fromEtherManagerConfigStatusDTOtoEtherConfigStatusDTO(managerResult);
		result.setProductId(productId);
		result.setEnvironment(environment.name());

		return result;
	}

	@Override
	public void addUsersToGroup(final List<String> users, final Product product)
	{
		// Create DTO to call Ether Manager
		final EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
		etherUserManagementDTO.setGroupId(product.getEtherGIAMProductGroup());
		etherUserManagementDTO.setUsers(users.toArray(String[]::new));

		LOG.debug("[EtherAPI] -> [addUsersToGroup] Calling to Ether Manager to add the users [{}] to group of product [{}]",
				String.join(", ", users), product.getName());

		// Call to EtherManager
		this.etherManagerClient.addUsersToGroup(etherUserManagementDTO);
	}

	@Override
	public void addProductMembersToGiamProductGroup(final Product product)
	{
		this.addUsersToGroup(Arrays.asList(this.getProductMembersUserCodes(product)), product);
	}

	@Override
	public void removeUsersFromGroup(final List<String> users, final Product product)
	{
		// Create DTO to call Ether Manager
		final EtherUserManagementDTO etherUserManagementDTO = new EtherUserManagementDTO();
		etherUserManagementDTO.setGroupId(product.getEtherGIAMProductGroup());
		etherUserManagementDTO.setUsers(users.toArray(String[]::new));

		LOG.debug("[EtherAPI] -> [removeUsersFromGroup] Calling to Ether Manager to remove the users [{}] from group of product [{}]",
				String.join(", ", users), product.getName());

		// Call to EtherManager
		this.etherManagerClient.removeUsersFromGroup(etherUserManagementDTO);
	}

	@Override
	public EtherDeploymentServiceInventoryDto[] getDeploymentServicesByEnvironment(NovaMetadata novaMetadata, String rawEnvironment)
	{
		Environment environment = null;

		if (rawEnvironment != null)
		{
			environment = Environment.valueOf(rawEnvironment);
		}

		final List<DeploymentPlan> deploymentPlans;

		if (environment != null)
		{
			deploymentPlans = deploymentPlanRepository.findByEnvironmentAndStatusAndSelectedDeploy(environment.getEnvironment(), DeploymentStatus.DEPLOYED, Platform.ETHER);
		}
		else
		{
			deploymentPlans = deploymentPlanRepository.findByStatusAndSelectedDeploy(DeploymentStatus.DEPLOYED, Platform.ETHER);
		}


		return deploymentPlans.stream()
				// Stream over all Subsystems of all Deployment Plans
				.flatMap(dp -> dp.getDeploymentSubsystems().stream())
				// Stream over all Deployment Services of all Subsystems of all Deployment Plans
				.flatMap(ds -> ds.getDeploymentServices().stream())
				// Filter out: batches, batch schedules, dependencies and libraries
				.filter(this::isNotBatchOrDependencyOrBatchScheduleOrLibrary)
				// Transform all Deployment Services to DeploymentServiceInventoryDto
				.map(deploymentServiceInventoryDtoBuilder::build)
				// Collect all stream items into a List
				.collect(Collectors.toList())
				// Transforms the list to an array
				.toArray(EtherDeploymentServiceInventoryDto[]::new);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public EtherDeploymentServiceInventoryDto getDeploymentServiceByInstanceId(final Integer instanceId)
	{
		DeploymentInstance deploymentInstance = this.deploymentInstanceRepository.findById(instanceId)
				.orElseThrow(() -> new NovaException(EtherError.getNoSuchInstanceError()));

		return this.deploymentServiceInventoryDtoBuilder.build(deploymentInstance.getService());
	}

    @Override
    public EtherLandingZoneInventoryDto[] getEtherLandingZones(final String environment, final String uuaa)
    {
		List<Filesystem> filesystems;
    	if (StringUtils.isNotBlank(environment) && StringUtils.isNotBlank(uuaa))
		{
			filesystems = filesystemRepository.findByEnvironmentAndUUAAAndFilesystemType(environment, uuaa, FilesystemEther.class);
		}
    	else if (StringUtils.isNotBlank(environment))
		{
			filesystems = filesystemRepository.findByEnvironmentAndFilesystemType(environment, FilesystemEther.class);
		}
    	else if (StringUtils.isNotBlank(uuaa))
		{
			filesystems = filesystemRepository.findByUUAAAndFilesystemType(uuaa, FilesystemEther.class);
		}
    	else
		{
			filesystems = filesystemRepository.findByFilesystemType(FilesystemEther.class);
		}

    	return filesystems.stream()
				.map(this::fromFilesystemToEtherLandingZoneInventoryDto)
				.toArray(EtherLandingZoneInventoryDto[]::new);
    }

    private boolean isNotBatchOrDependencyOrBatchScheduleOrLibrary(final DeploymentService deploymentService)
	{
		ServiceType serviceType = ServiceType.valueOf(deploymentService.getService().getServiceType());
		return !serviceType.isBatch()
				&& !ServiceType.DEPENDENCY.equals(serviceType)
				&& !ServiceType.BATCH_SCHEDULER_NOVA.equals(serviceType)
				&& !ServiceType.isLibrary(deploymentService.getService().getServiceType());
	}

	/**
	 * Search the product in NOVA. It will throw an exception if not.
	 * @param productId - productId
	 * @return          - The searched product
	 */
	private Product findByIdAndValidateProduct(final Integer productId)
	{
		Product product = this.productRepository.findById(productId).orElse(null);
		this.validator.checkProductExistence(productId, product);
		return product;
	}

	/**
	 * Returns as many {@link CheckEtherResourcesStatusRequestDTO} as environment to check. if Environment is null, all
	 * environment will be checked.
	 *
	 * @param product       the product with the Ether resources to check
	 * @param environment   int, pre, pro or null for all environment
	 * @return a list with all the requests
	 */
	private List<CheckEtherResourcesStatusRequestDTO> prepareStatusRequests(final Product product, final Environment environment)
	{
		// Get the team members of the product
		final String[] userMembers = this.getProductMembersUserCodes(product);

		final List<Environment> environmentsToCheck;

		if (environment == null)
		{
			environmentsToCheck = ENVIRONMENTS_TO_CHECK;
		}
		else
		{
			environmentsToCheck = Collections.singletonList(environment);
		}

		return environmentsToCheck.stream()
				.map(env -> {
					final CheckEtherResourcesStatusRequestDTO checkEtherResourcesStatusRequestDTO = new CheckEtherResourcesStatusRequestDTO();
					checkEtherResourcesStatusRequestDTO.setEnvironment(env.name());
					checkEtherResourcesStatusRequestDTO
							.setNamespace(PlatformUtils.getSelectedDeployNSForProductInEnvironment(product, Environment.valueOf(env.name())));
					checkEtherResourcesStatusRequestDTO.setTeamMembers(userMembers);
					checkEtherResourcesStatusRequestDTO.setGroupId(product.getEtherGIAMProductGroup());
					checkEtherResourcesStatusRequestDTO.setProductName(product.getName());

					return checkEtherResourcesStatusRequestDTO;
				})
				.collect(Collectors.toList());
	}

	/**
	 * Get the user codes of the members of a product.
	 *
	 * @param product The product.
	 * @return	The user codes.
	 */
	private String[] getProductMembersUserCodes(Product product)
	{
		List<USUserDTO> productMembers = this.usersClient.getProductMembers(product.getId(),
				new NovaException(EtherError.getUsersNotFoundError(), String.format("Cannot obtain product members for product: [%s]", product.getId())));

		return productMembers.stream()
				.filter(USUserDTO::getActive)
				.map(USUserDTO::getUserCode)
				.toArray(String[]::new);
	}

	/**
	 * Send the status requests in parallel. It will use the already configured Async Executor in order to propagate
	 * automatically the request headers.
	 *
	 * @param productId                               The id of the product
	 * @param checkEtherResourcesStatusRequestDTOList configuration requests list
	 * @return the result of all the calls
	 */
	private EtherConfigStatusDTO[] getEtherConfigurationStatusInParallel(final Integer productId,
																		 final List<CheckEtherResourcesStatusRequestDTO> checkEtherResourcesStatusRequestDTOList)
	{
		LOG.debug("[EtherAPI] -> [getEtherConfigurationStatusInParallel] Sending [{}] requests to Ether Manager to get the configuration status of product [{}]",
				checkEtherResourcesStatusRequestDTOList.size(), productId);

		List<EtherConfigStatusDTO> configStatusByEnvironment;

		final List<Supplier<EtherConfigStatusDTO>> callables = checkEtherResourcesStatusRequestDTOList.stream()
				.map(statusRequest -> executeStatusRequest(productId, statusRequest))
				.collect(Collectors.toList());

		final AtomicInteger counter = new AtomicInteger(0);
		configStatusByEnvironment = ParallelUtils.executeAsyncFaultTolerant(callables, asyncExecutor).stream()
				.map(asyncResult -> {
					final EtherConfigStatusDTO result;
					if (asyncResult.isErroneous())
					{
						final CheckEtherResourcesStatusRequestDTO request = checkEtherResourcesStatusRequestDTOList.get(counter.getAndIncrement());

						result = buildFailedEtherConfigStatusForEnvironment(request.getEnvironment(), productId, request.getNamespace(),
								"Error comprobando el estado de la configuración del Namespace");
					}
					else
					{
						counter.incrementAndGet();
						result = asyncResult.getValue();
					}
					return result;
				})
				.collect(Collectors.toList());

		LOG.debug("[EtherAPI] -> [getEtherConfigurationStatusInParallel] Sent all [{}] requests to Ether Manager to get the configuration status of product [{}]",
				checkEtherResourcesStatusRequestDTOList.size(), productId);

		return configStatusByEnvironment.toArray(EtherConfigStatusDTO[]::new);
	}

	private Supplier<EtherConfigStatusDTO> executeStatusRequest(final Integer productId, final CheckEtherResourcesStatusRequestDTO checkEtherResourcesStatusRequestDTO)
	{
		return () -> {
			EtherConfigStatusDTO result;

			if (StringUtils.isNotBlank(checkEtherResourcesStatusRequestDTO.getNamespace()))
			{
				try
				{
					final EtherManagerConfigStatusDTO managerResult =
							this.etherManagerClient.checkEtherResourcesStatus(checkEtherResourcesStatusRequestDTO);

					// Translate the EtherManagerConfigStatusDTO to EtherConfigStatusDTO
					result = fromEtherManagerConfigStatusDTOtoEtherConfigStatusDTO(managerResult);
					result.setProductId(productId);
					result.setEnvironment(checkEtherResourcesStatusRequestDTO.getEnvironment());
				}
				catch (Exception e)
				{
					result = buildFailedEtherConfigStatusForEnvironment(checkEtherResourcesStatusRequestDTO.getEnvironment(), productId,
							checkEtherResourcesStatusRequestDTO.getNamespace(), "Error comprobando el estado de la configuración del Namespace");
				}
			}
			else
			{
				result = buildFailedEtherConfigStatusForEnvironment(checkEtherResourcesStatusRequestDTO.getEnvironment(), productId,
						checkEtherResourcesStatusRequestDTO.getNamespace(), "El Namespace no está definido");
			}

			return result;
		};
	}

	private EtherConfigStatusDTO buildFailedEtherConfigStatusForEnvironment(final String environment, final Integer productId,
																			final String namespace, final String errorMessage)
	{
		EtherConfigStatusDTO result = new EtherConfigStatusDTO();
		result.setNamespace(namespace);
		result.setStatus(DeploymentConstants.KO);
		result.setErrorMessage(new String[]{ errorMessage });
		result.setProductId(productId);
		result.setEnvironment(environment);

		return result;
	}

	private EtherConfigStatusDTO fromEtherManagerConfigStatusDTOtoEtherConfigStatusDTO(final EtherManagerConfigStatusDTO emcsDTO)
	{
		EtherConfigStatusDTO result = new EtherConfigStatusDTO();
		result.setNamespace(emcsDTO.getNamespace());
		result.setStatus(emcsDTO.getStatus());
		result.setErrorMessage(emcsDTO.getErrorMessage());
		return result;
	}

	private EtherLandingZoneInventoryDto fromFilesystemToEtherLandingZoneInventoryDto(final Filesystem filesystem)
	{
		FilesystemEther filesystemEther;
		if (filesystem instanceof HibernateProxy)
		{
			filesystemEther = (FilesystemEther) ((HibernateProxy) filesystem).getHibernateLazyInitializer().getImplementation();
		}
		else
		{
			filesystemEther = (FilesystemEther) filesystem;
		}

		final Environment environment = Environment.valueOf(filesystemEther.getEnvironment());

		final EtherLandingZoneInventoryDto etherLandingZoneInventoryDto = new EtherLandingZoneInventoryDto();
		etherLandingZoneInventoryDto.setEnvironment(environment.getEnvironment());

		productRepository.fetchById(filesystemEther.getProduct().getId());

		etherLandingZoneInventoryDto.setProductName(filesystemEther.getProduct().getName());
		etherLandingZoneInventoryDto.setUuaa(filesystem.getProduct().getUuaa());
		etherLandingZoneInventoryDto.setVolumeId(filesystem.getName());

		String namespace;
		switch (environment)
		{
			case INT:
			case STAGING_INT:
			case LAB_INT:
			default:
				namespace = filesystemEther.getProduct().getEtherNsInt();
				break;
			case PRE:
			case STAGING_PRE:
			case LAB_PRE:
				namespace = filesystemEther.getProduct().getEtherNsPre();
				break;
			case PRO:
			case STAGING_PRO:
			case LAB_PRO:
				namespace = filesystemEther.getProduct().getEtherNsPro();
				break;
		}
		etherLandingZoneInventoryDto.setDeployNamespace(namespace);

		return etherLandingZoneInventoryDto;
	}
}
