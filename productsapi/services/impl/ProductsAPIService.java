package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.etherapi.model.EtherConfigStatusDTO;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.ITNewProjectRequest;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.apirestgen.productsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductType;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.UserValidationService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.platformservices.coreservice.etherapi.exceptions.EtherError;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IEtherService;
import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherConstants;
import com.bbva.enoa.platformservices.coreservice.etherapi.util.EtherValidator;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.INewProductRequestService;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductConfigService;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductsAPIService;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.ProductConverter;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions.getCleanUuaa;

/**
 * Services for end point product service
 * Created by BBVA - xe30432 on 02/02/2017.
 */
@Service
public class ProductsAPIService implements IProductsAPIService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductsAPIService.class);

    /**
     * Exception
     */
    private static final NovaException PERMISSION_DENIED = new NovaException(ProductsAPIError.getForbiddenError(), ProductsAPIError.getForbiddenError().toString());

    /**
     * Product repository
     */
    private final ProductRepository productRepository;

    /**
     * Error manager
     */
    private final IErrorTaskManager errorTaskMgr;

    /**
     * Users
     */
    private final IProductUsersClient usersClient;

    /**
     * Doc systems
     */
    private final IDocSystemService docSystemService;

    /**
     * User validation
     */
    private final UserValidationService userValidationService;

    /**
     * Tools service
     */
    private final IToolsClient toolsService;

    /**
     * Issue tracker client
     */
    private final IIssueTrackerClient issueTrackerClient;

    /**
     * New product request service
     */
    private final INewProductRequestService newProductRequestService;

    /**
     * Product Infrastructure Configuration for writing deploy/logging in Nova/Ether
     */
    private final IProductConfigService productInfraConfigService;

    /**
     * Ether service
     */
    private final IEtherService etherService;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Logs manager client
     */
    private final ILogsClient logsClient;

    /**
     * Ether Validator.
     */
    private final EtherValidator etherValidator;

    /**
     * Batch manager client
     */
    private IBatchManagerClient batchManagerClient;

    /**
     * Constructor
     *
     * @param productRepository          productRepository
     * @param errorTaskMgr               errorTaskMgr
     * @param usersClient                usersClient
     * @param docSystemService           docSystemService
     * @param userValidationService      userValidationService
     * @param toolsService               toolsService
     * @param issueTrackerClient         issueTrackerClient
     * @param newProductRequestService   newProductRequestService
     * @param pProductInfraConfigService the p product infra config service
     * @param etherService               the ether service
     * @param logsClient                 logmanager client
     * @param etherValidator             Ether Validator.
     * @param novaActivityEmitter        NovaActivity emitter
     * @param batchManagerClient         batch manager client
     */
    @Autowired
    public ProductsAPIService(final ProductRepository productRepository, final IErrorTaskManager errorTaskMgr,
                              final IProductUsersClient usersClient, final IDocSystemService docSystemService,
                              final UserValidationService userValidationService, final IToolsClient toolsService,
                              final IIssueTrackerClient issueTrackerClient, final INewProductRequestService newProductRequestService,
                              final IProductConfigService pProductInfraConfigService, final IEtherService etherService,
                              final ILogsClient logsClient, final EtherValidator etherValidator, final INovaActivityEmitter novaActivityEmitter,
                              final IBatchManagerClient batchManagerClient)
    {
        this.productRepository = productRepository;
        this.errorTaskMgr = errorTaskMgr;
        this.usersClient = usersClient;
        this.docSystemService = docSystemService;
        this.userValidationService = userValidationService;
        this.toolsService = toolsService;
        this.issueTrackerClient = issueTrackerClient;
        this.newProductRequestService = newProductRequestService;
        this.productInfraConfigService = pProductInfraConfigService;
        this.etherService = etherService;
        this.logsClient = logsClient;
        this.etherValidator = etherValidator;
        this.novaActivityEmitter = novaActivityEmitter;
        this.batchManagerClient = batchManagerClient;
    }

    ////////////////////////////////////////////////////////////// IMPLEMENTATIONS /////////////////////////////////////////////////////

    @Override
    public void updateProjectKey(Integer productId, String projectKey, String ivUser)
    {
        LOG.debug("[{}] -> [updateProjectKey]: Begin updating Project Key", Constants.PRODUCT_SERVICE);
        if (!"".equals(projectKey))
        {
            LOG.debug("[ProductsAPIService] -> [updateProjectKey]: project Key [{}]", projectKey);

            Product product = this.productRepository.findById(productId).orElse(null);

            if (product != null)
            {
                product.setDesBoard(projectKey);
                productRepository.saveAndFlush(product);
            }
            else
            {
                LOG.error("[{}] -> [updateProjectKey]: product is NULL", Constants.PRODUCT_SERVICE);
            }
        }
        else
        {
            LOG.error("[{}] -> [updateProjectKey]: productProjectChangeKey is NULL", Constants.PRODUCT_SERVICE);
        }
        LOG.debug("[{}] -> [updateProjectKey]: End updating Project Key", Constants.PRODUCT_SERVICE);
    }

    @Override
    public Product createNewProduct(final ProductRequestDTO createNewProduct)
    {
        LOG.debug("[{}] ->  [createNewProduct]: Begin creating new Product", Constants.PRODUCT_SERVICE);
        // Validate if the user code exists from to do task creation (it means, the product owner) to associate it to the product
        USUserDTO usUser = this.validateUser(createNewProduct.getUserCode());

        // Validate that the  product name and the uuaa name do not exists
        CommonsFunctions.validateProductNameAndUuaaName(createNewProduct.getName(), createNewProduct.getUuaa(), this.productRepository);

        // call to JIRA for getting the JIRA KEY for the Product
        this.getProjectKey(createNewProduct);

        // Success. Create the product for the portal user (the product owner) and save it into BBDD. Initial status: CREATING
        Product product = this.newProductRequestService.createAndSaveNewProduct(createNewProduct, usUser);

        // Emit Creation Product Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity.Builder(product.getId(), ActivityScope.PRODUCT, ActivityAction.CREATED)
                .entityId(product.getId())
                .addParam("uuaa", product.getUuaa())
                .addParam("productType", product.getType())
                .addParam("productName", product.getName())
                .addParam("productDescription", product.getDescription())
                .build());

        this.logsClient.createLogRateThresholdEvents(product.getId(), product.getEmail());
        this.toolsService.addExternalToolsToProduct(product.getId());
        this.docSystemService.createDefaultDocSystem(product, usUser);

        this.createProductTeam(usUser, product);

        LOG.debug("[{}] ->  [createNewProduct]: End creating new Product", Constants.PRODUCT_SERVICE);

        return product;
    }

    @Override
    public ProductDTO[] getAllProducts()
    {
        // Validate and get all the products
        List<Product> productsJSONS = this.validateProducts();

        // Check and convert product entity to ProductJSON object
        return ProductConverter.convertProductEntityToProductsAPI(productsJSONS);
    }

    @Override
    public ProductDTO[] getAllProductsByStatus(String statusCode, String type)
    {
        // Get all products by Status from BBDD
        List<Product> productList = this.productRepository
                .findByProductStatusType(ProductStatus.valueOf(statusCode.toUpperCase()), ProductType.getValueOf(type).getType());

        // Check and convert product entity list to ProductJSON list object
        return ProductConverter.convertProductEntityToProductsAPI(productList);
    }

    @Override
    public ProductDTO[] getProducts(final String type, final String userCode)
    {
        ProductDTO[] productDTO;

        //First of all, validate platform admin
        if (Strings.isNullOrEmpty(userCode))
        {
            return this.getAllProducts();
        }
        else
        {
            // Get the products ids
            int[] productIds = this.usersClient.getProductsByUser(userCode);

            // Get all products from BBDD filtered by type
            List<Product> productList = this.productRepository.findByIdsType(productIds, ProductType.getValueOf(type).getType());

            // Check and convert product entity list to ProductJSON list object
            productDTO = ProductConverter.convertProductEntityToProductsAPI(productList, true);

            LOG.debug("[ProductsAPIService] -> [getProducts]: the user code: [{}] obtained: [{}] of type: [{}}",
                    userCode, productDTO.length, type);
            LOG.trace("[ProductsAPIService] -> [getProducts]: products list: [{}] of the userCode: [{}] - of type: [{}]",
                    productDTO, userCode, type);
            return productDTO;
        }
    }

    @Override
    public Product updateProductStatus(Integer productId, String status)
    {
        // Validate product.
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        // Update the product status
        product.setProductStatus(ProductStatus.valueOf(status));
        LOG.debug("Changed the Product status to: [{}] of the product: [{}]", product.getProductStatus(), product.getName());

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity.Builder(productId, ActivityScope.PRODUCT, ActivityAction.CONFIGURATED)
                .entityId(productId)
                .addParam("type", product.getType())
                .addParam("productStatus", product.getProductStatus().getProductStatus())
                .build());

        // Save product changes into BBDD
        this.productRepository.saveAndFlush(product);

        return product;
    }

    @Override
    public ProductSummaryDTO getProductSummary(final Integer productId)
    {
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        // Check and convert product entity to ProductJSON object
        return ProductConverter.convertProductEntityToProductSummaries(product);
    }

    @Override
    public void addUserToProduct(final String ivUser, final Integer productId, final String userCode, final String role)
    {
        LOG.debug("[{}] -> [addUserToProduct]: adding user [{}] to the product: [{}]", Constants.PRODUCT_SERVICE, userCode, productId);

        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CREATE_MEMBER_PERMISSION, productId, PERMISSION_DENIED);

        LOG.debug("[{}] -> [addUserToProduct]: validating the product: [{}]", Constants.PRODUCT_SERVICE, productId);
        // Validate product.
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        LOG.debug("[{}] -> [addUserToProduct]: validating the user: [{}]", Constants.PRODUCT_SERVICE, userCode);
        USUserDTO usUser = this.validateUser(userCode);

        // Add the new user to the Ether namespace
        addUserToProductInEther(userCode, product);

        try
        {
            // Call Tools API for creating all the tools for user
            TOProductUserDTO productUser = this.getPtProductUser(productId, userCode);
            this.toolsService.addUserTool(productUser);
        }
        catch (NovaException exception)
        {
            LOG.error("[{}] -> [addUserToProduct]: Error adding all user [{}] tools.", Constants.PRODUCT_SERVICE, userCode);

            LOG.error(exception.getMessage());
            throw exception;
        }

        // Creating Product User DTO
        USProductUserDTO usProductUser = new USProductUserDTO();
        usProductUser.setUserCode(userCode);
        usProductUser.setTeamCode(role);
        this.usersClient.addUserToProduct(usProductUser, ivUser, productId);

        // Add user to defaultDocSystem
        this.docSystemService.addUserToDefaultDocSystem(product, usUser, userCode);
    }

    @Override
    public void removeUserFromProduct(String ivUser, Integer productId, String userCode)
    {

        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_DELETE_MEMBER_PERMISSION, productId, PERMISSION_DENIED);

        // Validate product.
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        LOG.debug("[{}] -> [removeUserFromProduct]: removing the user [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, userCode, product);

        // Validate user
        USUserDTO usUser = this.userValidationService.getUser(userCode);

        // Remove the user from the Ether namespace
        removeUserFromProductInEther(userCode, product);

        //Removing the user from the product
        USProductUserDTO usProductUser = new USProductUserDTO();
        usProductUser.setUserCode(userCode);
        this.usersClient.removeUserFromProduct(ivUser, productId, usProductUser);

        // Call Tools API for removing all the tools for user
        TOProductUserDTO productUser = getPtProductUser(productId, userCode);
        // Do not force delete without checking Product Owner
        boolean forceDeletion = false;
        this.toolsService.removeUserTool(productUser, forceDeletion);

        // Remove user form default DocSystem
        this.docSystemService.removeUserFromDefaultDocSystem(product, usUser, userCode);
    }

    @Override
    public ProductCommonConfigurationDTO getCommonConfiguration(Integer productId)
    {
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        return this.productInfraConfigService.getCommonConfigurationFromProductEntity(product);
    }

    @Override
    @Transactional
    public void updateCommonConfiguration(final String ivUser, final ProductCommonConfigurationDTO commonConfiguration, final Integer productId)
    {
        LOG.debug("[{}] -> [updateCommonConfiguration]: Updating common configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, commonConfiguration, productId);

        // Checking permissions for user and call queries service
        this.usersClient.checkHasPermission(ivUser, Constants.EDIT_PRODUCT_COMMON_CONFIGURATION_PERMISSION, PERMISSION_DENIED);

        // Validate product.
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        Map<String, Map<String, String>> differences = this.productInfraConfigService.checkAndReturnCommonConfigurationDifferences(product, commonConfiguration);

        this.productInfraConfigService.updateCommonConfiguration(product, commonConfiguration);

        this.productRepository.save(product);

        this.generateNewActivity(differences, product, null, ActivityAction.COMMON_CONFIGURATION_CONFIGURED);

        LOG.debug("[{}] -> [updateCommonConfiguration]: Updated configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, commonConfiguration, productId);
    }

    /**
     * Gets base information for a product
     *
     * @param productId The unique product id
     * @return the product information
     */
    @Override
    public ProductBaseDTO getBaseProduct(Integer productId)
    {
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        // Check and convert product entity to ProductJSON object
        return ProductConverter.convertProductEntityToProductBase(product);
    }

    @Override
    @Transactional
    public String[] getUuaas()
    {
        return this.productRepository.findAllByOrderByUuaaAsc().stream().map(Product::getUuaa).toArray(String[]::new);
    }

    @Override
    public String[] getUuaasNovaEphoenixLegacy()
    {
        return Stream.of(this.getUuaas(), this.batchManagerClient.getUuaasEphoenixLegacy()).flatMap(Stream::of).distinct().toArray(String[]::new);
    }

    @Override
    public String[] getUuaasEphoenixLegacy()
    {
        return this.batchManagerClient.getUuaasEphoenixLegacy();
    }

    @Override
    @Transactional
    public String[] getTypes()
    {
        return this.productRepository.findAllByOrderByTypeAsc().stream().map(Product::getType).distinct().toArray(String[]::new);
    }

    @Override
    @Transactional
    public void createFromEtherConsole(String ivUser, EtherConsoleProductCreationNotificationDTO etherConsoleProductCreationNotification)
    {
        // TODO@Adrián Hay que ver qué credenciales se necesitarán realmente.
        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

        String uuaa = getCleanUuaa(etherConsoleProductCreationNotification.getUuaa());
        Product product = this.productRepository.findOneByUuaa(uuaa);
        if (product == null)
        {
            //TODO@Adrián Manejar el caso de creación de producto.
        }
        else
        {
            this.etherValidator.validateConsoleProductCreationNotification(etherConsoleProductCreationNotification);

            //TODO@Adrián ¿Debería estar en etherapi, ethermanager, o en la librería de utilidades de Ether? Ver con Diego.
            //TODO@Adrián ¿Finalmente nos van a pasar el entorno en la petición?
            String namespace = etherConsoleProductCreationNotification.getNamespace();
            if (namespace.endsWith(".dev")) //TODO@Adrián Sacar a constantes.
            {
                product.setEtherNsInt(namespace);
            }
            else if (namespace.endsWith(".au")) //TODO@Adrián Sacar a constantes.
            {
                product.setEtherNsPre(namespace);
            }
            else if (namespace.endsWith(".pro")) //TODO@Adrián Sacar a constantes.
            {
                product.setEtherNsPro(namespace);
            }
            else
            {
                // TODO@Adrián Lanzar excepción.
            }

            product.setEtherGIAMProductGroup(etherConsoleProductCreationNotification.getProductGroup());
            this.etherService.addProductMembersToGiamProductGroup(product);

        }

        this.productRepository.save(product);
    }

    @Override
    @Transactional
    public void updateEtherGIAMProductGroup(String ivUser, String etherGIAMProductGroup, Integer productId)
    {
        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CONFIGURATION_MANAGEMENT, productId, PERMISSION_DENIED);

        // Validate product.
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        product.setEtherGIAMProductGroup(etherGIAMProductGroup);

        this.productRepository.save(product);
    }

    @Override
    public ProductBaseDTO findProductByReleaseVersionServiceId(Integer releaseVersionServiceId)
    {
        Integer productId = this.productRepository.findProductIdByReleaseVersionServiceId(releaseVersionServiceId);

        if (productId == null)
        {
            LOG.warn("[{}] -> [findProductIdByReleaseVersionServiceId]: Product not found for ReleaseVersionServiceId [{}].",
                    Constants.PRODUCT_SERVICE, releaseVersionServiceId);

            throw new NovaException(ProductsAPIError.getProductDoesNotExistError(), "Product not found for ReleaseVersionServiceId " + releaseVersionServiceId);
        }

        return this.getBaseProduct(productId);
    }

    @Override
    public ProductBaseDTO findProductByBehaviorVersionServiceId(final Integer behaviorVersionServiceId)
    {
        Integer productId = this.productRepository.findProductIdByBehaviorVersionServiceId(behaviorVersionServiceId);

        if (productId == null)
        {
            LOG.warn("[{}] -> [findProductByBehaviorVersionServiceId]: Product not found for behaviorVersionServiceId [{}].",
                    Constants.PRODUCT_SERVICE, behaviorVersionServiceId);

            throw new NovaException(ProductsAPIError.getProductDoesNotExistError(), "Product not found for behaviorVersionServiceId " + behaviorVersionServiceId);
        }

        return this.getBaseProduct(productId);
    }

    @Override
    public ProductBaseDTO findProductByReleaseVersionId(Integer releaseVersionId)
    {
        Integer productId = this.productRepository.findProductIdByReleaseVersionId(releaseVersionId);

        if (productId == null)
        {
            LOG.warn("[{}] -> [findProductByReleaseVersionId]: Product not found for ReleaseVersionId [{}].",
                    Constants.PRODUCT_SERVICE, releaseVersionId);

            throw new NovaException(ProductsAPIError.getProductDoesNotExistError(), "Product not found for ReleaseVersionId " + releaseVersionId);
        }

        return this.getBaseProduct(productId);
    }

    @Override
    public ProductBaseDTO findProductByUUAA(String uuaa)
    {
        Integer productId = this.productRepository.findProductIdByUuaa(uuaa);

        if (productId == null)
        {
            LOG.warn("[{}] -> [findProductByUUAA]: Product not found for UUAA [{}].",
                    Constants.PRODUCT_SERVICE, uuaa);

            throw new NovaException(ProductsAPIError.getProductDoesNotExistError(), "Product not found for UUAA " + uuaa);
        }

        return this.getBaseProduct(productId);
    }

    @Override
    public ProductSummaryDTO findProductSummaryByUUAA(final String uuaa)
    {
        final var productId = Optional.ofNullable(this.productRepository.findProductIdByUuaa(uuaa)).orElseThrow(() -> new NovaException(ProductsAPIError.getProductDoesNotExistError(), "Product not found for UUAA " + uuaa));
        return this.getProductSummary(productId);
    }

    @Override
    public Long getProductsCount()
    {
        return this.productRepository.count();
    }

    @Override
    public ProductConfigurationByEnvDTO getProductConfigurationByEnvironment(Integer productId, String environment)
    {

        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        Environment env = CommonsFunctions.validateEnvironment(environment);

        ProductConfigurationByEnvDTO productConfigurationByEnvDTO = new ProductConfigurationByEnvDTO();

        productConfigurationByEnvDTO.setManagementCofiguration(this.productInfraConfigService.getManagementConfigurationFromProductEntityByEnv(product, env));
        productConfigurationByEnvDTO.setInfrastructureGeneralConfiguration(this.productInfraConfigService.getGeneralInfrastructureConfigFromProductEntityByEnv(product, env));
        productConfigurationByEnvDTO.setServiceConfiguration(this.productInfraConfigService.getServiceConfigurationFromProductEntityByEnv(product, env));

        return productConfigurationByEnvDTO;
    }

    @Override
    public void updateManagementConfigurationByEnv(String ivUser, ProductManagementConfByEnvDTO managementConfigurationDTO, Integer productId, String environment)
    {
        LOG.debug("[{}] -> [updateManagementConfigurationByEnv]: Updating Management Configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, managementConfigurationDTO, productId);

        // Checking permissions for user and call queries service
        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        Environment env = CommonsFunctions.validateEnvironment(environment);

        Map<String, Map<String, String>> differences = this.productInfraConfigService.checkAndReturnManagementConfigurationDifferences(product, managementConfigurationDTO, env);

        this.productInfraConfigService.updateManagementConfigurationByEnv(managementConfigurationDTO, product, env);

        this.generateNewActivity(differences, product, environment, ActivityAction.MANAGEMENT_CONFIGURATION_CONFIGURED);

        LOG.debug("[{}] -> [updateManagementConfigurationByEnv]: Updated Management Configuration from the product: [{}]", Constants.PRODUCT_SERVICE, productId);
    }

    @Override
    @Transactional
    public void updateGeneralInfrastructureConfigurationByEnv(String ivUser, InfrastructureConfiguredByEnvDTO generalInfrastructureConfig, Integer productId, String environment)
    {

        LOG.debug("[{}] -> [updateGeneralInfrastructureConfigurationByEnv]: Updating General Infrastructure Configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, generalInfrastructureConfig, productId);

        // Checking permissions for user and call queries service
        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        Environment env = CommonsFunctions.validateEnvironment(environment);

        Map<String, Map<String, String>> differences = this.productInfraConfigService.checkAndReturnInfrastructureConfigurationDifferences(product, generalInfrastructureConfig, env);

        this.productInfraConfigService.updateGeneralInfrastructureConfigurationByEnv(generalInfrastructureConfig, product, env);

        this.generateNewActivity(differences, product, environment, ActivityAction.INFRASTRUCTURE_CONFIGURED);

        LOG.debug("[{}] -> [updateGeneralInfrastructureConfigurationByEnv]: Updated General Infrastructure Configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, generalInfrastructureConfig, productId);
    }

    @Override
    @Transactional
    public void updateProductServiceConfigurationByEnv(String ivUser, ProductServiceConfigurationDTO productServiceConfig, Integer productId, String environment)
    {
        LOG.debug("[{}] -> [updateProductServiceConfigurationByEnv]: Updating Product Service Configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, productServiceConfig, productId);

        // Checking permissions for user and call queries service
        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        Environment env = CommonsFunctions.validateEnvironment(environment);

        Map<String, Map<String, String>> differences = this.productInfraConfigService.checkAndReturnProductServiceConfigurationDifferences(product, productServiceConfig, env);

        this.productInfraConfigService.updateProductServiceConfigurationByEnv(productServiceConfig, product, env);

        this.generateNewActivity(differences, product, environment, ActivityAction.INFRASTRUCTURE_CONFIGURED);
        LOG.debug("[{}] -> [updateProductServiceConfigurationByEnv]: Updated Product Service Configuration from the product: [{}]", Constants.PRODUCT_SERVICE, productId);
    }


    @Override
    public NovaConfigurationDTO getNovaInfrastructureConfByEnv(Integer productId, String environment)
    {
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        return this.productInfraConfigService.getNovaConfigurationFromProductEntity(product, CommonsFunctions.validateEnvironment(environment));
    }

    @Override
    @Transactional
    public void updateNovaInfrastructureConfByEnv(String ivUser, NovaConfigurationDTO novaConfigurationDTO, Integer productId, String environment)
    {
        LOG.debug("[{}] -> [updateNovaInfrastructureConfByEnv]: Updating Nova Configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, novaConfigurationDTO, productId);
        // Checking permissions for user and call queries service
        this.usersClient.checkHasPermission(ivUser, Constants.PRODUCT_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

        // validations
        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        Environment env = CommonsFunctions.validateEnvironment(environment);
        this.validateNovaConfiguration(env, novaConfigurationDTO);

        Map<String, Map<String, String>> differences = this.productInfraConfigService.checkAndReturnNovaConfigurationDifferences(product, novaConfigurationDTO, env);

        this.productInfraConfigService.updateProductNovaInfrastructureConf(product, env, novaConfigurationDTO);

        // Emit new Activity
        this.generateNewActivity(differences, product, environment, ActivityAction.NOVA_INFRASTRUCTURE_CONFIGURED);

        LOG.debug("[{}] -> [updateNovaInfrastructureConfByEnv]: Updated Nova Configuration from the product: [{}]", Constants.PRODUCT_SERVICE, productId);
    }

    @Override
    public EtherConfigurationDTOResponse getEtherInfrastructureConfByEnv(Integer productId, String environment)
    {
        EtherConfigurationDTOResponse response = new EtherConfigurationDTOResponse();

        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);

        String namespaceInEnvironment = this.productInfraConfigService.getEtherConfigurationFromProductEntity(product, CommonsFunctions.validateEnvironment(environment)).getNamespace();
        response.setNamespace(namespaceInEnvironment);

        if (namespaceInEnvironment != null)
        {
            EtherConfigStatusDTO[] etherStatus = etherService.getEtherConfigurationStatus(productId, environment);
            response.setEtherConfigStatusDTO(this.buildEtherConfigurationDTOResponseFromEtherConfig(etherStatus[0]));
        }

        return response;
    }

    @Override
    @Transactional
    public void updateEtherInfrastructureConfByEnv(String ivUser, EtherConfigurationDTO etherConfDTO, Integer productId, String environment)
    {
        LOG.debug("[{}] -> [updateEtherInfrastructureConfByEnv]: Updating Ether Configuration [{}] from the product: [{}]", Constants.PRODUCT_SERVICE, etherConfDTO, productId);
        // Checking permissions for user and call queries service
        this.usersClient.checkHasPermission(ivUser, EtherConstants.PRODUCT_ETHER_CONFIGURATION_MANAGEMENT, PERMISSION_DENIED);

        Product product = CommonsFunctions.validateProduct(this.productRepository, productId);
        Environment env = CommonsFunctions.validateEnvironment(environment);

        Map<String, Map<String, String>> differences =
                this.productInfraConfigService.checkAndReturnEtherConfigurationDifferences(product, etherConfDTO, env);

        //call ether service.
        this.etherService.configureEtherInfrastructure(ivUser, environment, productId, etherConfDTO.getNamespace());

        this.productInfraConfigService.updateProductEtherInfrastructureConf(product, env, etherConfDTO);

        // Emit new Activity
        this.generateNewActivity(differences, product, environment, ActivityAction.NAMESPACE_CONFIGURED);

        LOG.debug("[{}] -> [updateEtherInfrastructureConfByEnv]: Updated Ether Configuration from the product: [{}]", Constants.PRODUCT_SERVICE, productId);
    }

    /////////////////////////////// PRIVATE METHODS ////////////////////////////////

    /**
     * Add a new user to group of product in Ether
     *
     * @param userCode The new user to add
     * @param product  The product
     */
    private void addUserToProductInEther(final String userCode, final Product product)
    {
        if (StringUtils.isNotBlank(product.getEtherGIAMProductGroup()))
        {
            // Add the new user
            LOG.debug("[{}] -> [addUserToProductInEther]: Adding user [{}] to group of product [{}] in Ether", Constants.PRODUCT_SERVICE, userCode, product.getName());

            try
            {
                etherService.addUsersToGroup(Collections.singletonList(userCode), product);
            }
            catch (Exception e)
            {
                LOG.warn("[{}] -> [addUserToProductInEther]: Error adding user {} to group of product [{}] in Ether.", Constants.PRODUCT_SERVICE,
                        userCode, product.getName(), e);

                throw new NovaException(EtherError.getAddingUsersError(product.getName()), e);
            }
        }
    }

    /**
     * Remove a new user to group from product in Ether
     *
     * @param userCode The new user to remove
     * @param product  The product
     */
    private void removeUserFromProductInEther(final String userCode, final Product product)
    {
        if (StringUtils.isNotBlank(product.getEtherGIAMProductGroup()))
        {
            // Remove the new user
            LOG.debug("[{}] -> [removeUserFromProductInEther]: Remove user [{}] from group of product [{}] in Ether", Constants.PRODUCT_SERVICE, userCode, product.getName());

            try
            {
                etherService.removeUsersFromGroup(Collections.singletonList(userCode), product);
            }
            catch (Exception e)
            {
                LOG.warn("[{}] -> [removeUserFromProductInEther]: Error removing user {} from group of product [{}] in Ether.", Constants.PRODUCT_SERVICE,
                        userCode, product.getName(), e);

                throw new NovaException(EtherError.getRemovingUsersError(product.getName()), e);
            }
        }
    }


    /**
     * Check product list from product list
     *
     * @param productList the product list to check
     */
    private void checkProductList(final List<Product> productList)
    {
        if (productList == null)
        {
            LOG.error("Failed the request to BBDD trying to get all NOVA products. The product instance is null. Error code: [{}]",
                    ProductsAPIError.getDatabaseConnectionError());

            // Create an error task
            String message = "Error trying to connect to data base trying to get the products.";
            errorTaskMgr.createErrorTask(null, ProductsAPIError.getDatabaseConnectionError(), message,
                    ToDoTaskType.INTERNAL_ERROR, null, null, null);

            throw new NovaException(ProductsAPIError.getDatabaseConnectionError(),
                    "ProductsAPI: Failed the request to BBDD trying to get data info. The info is null.");
        }
    }

    /**
     * Validate if a products list exists into BBDD
     *
     * @return a product list
     */
    private List<Product> validateProducts()
    {
        // Get the products list entity from BBDD
        List<Product> productList = this.productRepository.findAll();
        LOG.trace("ProductsAPI: products via products/all. Getting products from BBDD. [{}]", productList);

        this.checkProductList(productList);

        return productList;
    }

    /**
     * Creates a product team. Initially the team has only the product owner member.
     *
     * @param usUser  product owner user
     * @param product to associate to the team
     */
    private void createProductTeam(final USUserDTO usUser, final Product product)
    {
        LOG.debug("[ProductsAPI]: [createProductTeam] -> Creating new product team for the product name [{}] and id [{}] " +
                "with only this portal user code (product owner) [{}]", product.getName(), product.getId(), usUser.getUserCode());

        this.addUserToProduct(com.bbva.enoa.platformservices.coreservice.common.Constants.IMMUSER, product.getId(), usUser.getUserCode(), RoleType.PRODUCT_OWNER.name());

        LOG.debug("[ProductsAPI]: [createProductTeam] -> The tools were added for the user: [{}] for product [{}]", usUser.getUserCode(), product.getName());
    }

    /**
     * call to JIRA for getting a Temporal PROJECT KEY for the Product
     *
     * @param createNewProduct createNewProduct
     */
    private void getProjectKey(ProductRequestDTO createNewProduct)
    {
        try
        {
            String userCode = createNewProduct.getUserCode();

            // call to issue tracker. createProjectRequest for getting temporal Project JIRA key
            IssueTrackerItem issueTrackerItems;
            ITNewProjectRequest newProjectRequest = new ITNewProjectRequest();

            //Administrators, 'Lead' e 'IT Manager' : mandatory
            newProjectRequest.setProjectAdministrators(userCode);
            newProjectRequest.setLead(userCode);
            newProjectRequest.setItManager(userCode);
            newProjectRequest.setProductOwner(userCode);

            newProjectRequest.setUuaa(createNewProduct.getUuaa());

            newProjectRequest.setDescription(createNewProduct.getDescription());
            newProjectRequest.setProjectName(createNewProduct.getName());
            newProjectRequest.setSummary(createNewProduct.getUuaa() + " - " + createNewProduct.getName());
            issueTrackerItems = this.issueTrackerClient.createProjectRequest(newProjectRequest);

            if (issueTrackerItems != null && !StringUtils.isEmpty(issueTrackerItems.getProjectKey()))
            {
                createNewProduct.setJiraKey(issueTrackerItems.getProjectKey());
            }
        }
        catch (Exception e)
        {
            //this com.bbva.enoa.platformservices.historicalloaderservice.step (get the JIRA KEY) is not critical, so if something was wrong, we can go on
            LOG.error("[ProductsAPIService] -> [getProjectKey]: there was something wrong trying to get the JIRA KEY for product request DTO: [{}]. Error: ", createNewProduct);
        }
    }

    private USUserDTO validateUser(String userCode) throws NovaException
    {
        // Obtaining user information
        USUserDTO usUserDTO = this.userValidationService.getUser(userCode);

        LOG.debug("[{}] -> [formatUserCode]: User with id: [{}] information: [{}].", Constants.PRODUCT_SERVICE, userCode, usUserDTO);

        // Check if user has email. Mandatory
        if (StringUtils.isBlank(usUserDTO.getEmail()) || usUserDTO.getEmail().equalsIgnoreCase(com.bbva.enoa.platformservices.coreservice.common.Constants.NOT_AVAILABLE_VALUE))
        {
            LOG.error("[{}] -> [validateUser]: The user must contain an email value: [{}] assigned for user code: [{}] valid.", Constants.PRODUCT_SERVICE, usUserDTO.getEmail(), userCode);
            throw new NovaException(ProductsAPIError.getEmailNotAvailableError());
        }

        return usUserDTO;
    }

    /**
     * Format the user code and try to avoid duplicates user codes due to Zuul
     * header
     *
     * @param userCode the user code to format
     * @return a user code formatted
     */
    private String formatUserCode(final String userCode)
    {

        LOG.trace("[{}] -> [formatUserCode]: formatting the userCode: [{}].", Constants.PRODUCT_SERVICE, userCode);
        String userCodeResult;

        // Just in case. Try to avoid user codes duplicated user codes for the
        // Zuul IV-USER header
        if (userCode != null && userCode.contains(","))
        {
            userCodeResult = userCode.substring(0, userCode.indexOf(','));
        }
        else
        {
            userCodeResult = userCode;
        }

        LOG.debug("[{}] -> [formatUserCode]: formatted the userCode finished: [{}].", Constants.PRODUCT_SERVICE, userCodeResult);

        return userCodeResult;
    }

    private TOProductUserDTO getPtProductUser(Integer productId, String userCode)
    {
        TOProductUserDTO productUser = new TOProductUserDTO();
        productUser.setUserCode(userCode);
        productUser.setProductId(productId);
        return productUser;
    }

    private void generateNewActivity(Map<String, Map<String, String>> differences, Product product, String environment, ActivityAction activityAction)
    {
        GenericActivity.Builder builder = new GenericActivity.Builder(product.getId(), ActivityScope.PRODUCT, activityAction)
                .entityId(product.getId())
                .addParam("type", product.getType());

        if (!ActivityAction.CONFIGURATED.equals(activityAction)) //update CommonAction
        {
            builder.environment(environment);
        }

        if (differences != null)
        {
            differences.forEach((k, v) -> builder.addParam(k, v.entrySet().iterator().next().getValue()));
        }

        // Emit Send Request Add File Transfer Activity
        this.novaActivityEmitter.emitNewActivity(builder.build());
    }

    private PEtherConfigStatusDTO buildEtherConfigurationDTOResponseFromEtherConfig(EtherConfigStatusDTO etherConfigStatusDTO)
    {
        PEtherConfigStatusDTO pEtherConfigStatusDTO = new PEtherConfigStatusDTO();
        pEtherConfigStatusDTO.setEnvironment(etherConfigStatusDTO.getEnvironment());
        pEtherConfigStatusDTO.setNamespace(etherConfigStatusDTO.getNamespace());
        pEtherConfigStatusDTO.setProductId(etherConfigStatusDTO.getProductId());
        pEtherConfigStatusDTO.setStatus(etherConfigStatusDTO.getStatus());
        pEtherConfigStatusDTO.setErrorMessage(etherConfigStatusDTO.getErrorMessage());

        return pEtherConfigStatusDTO;

    }

    /**
     * Validate if Nova Configuration is correct. Do not have specif configuration
     * in INT and PRE environment.
     *
     * @param env         environment
     * @param novaConfDTO NovaConfigurationDTO
     */
    private void validateNovaConfiguration(Environment env, NovaConfigurationDTO novaConfDTO)
    {
        // Only if Environment is PRO (INT and PRE do not have specif configuration)
        if (Environment.PRO.equals(env))
        {
            if (!novaConfDTO.getMulticpd())
            {
                // If is MonoCPD, and CPD is not send, throw exception
                if (novaConfDTO.getCpd() == null || novaConfDTO.getCpd().trim().isEmpty())
                {
                    LOG.error("[{}] -> [validateNovaUpdateConfiguration]: MonoCPD is enabled and there is no CPD selected.", Constants.PRODUCT_SERVICE);
                    throw new NovaException(ProductsAPIError.getNoCPDsetForMonoCPDInPro());
                }
            }
        }
    }

}
