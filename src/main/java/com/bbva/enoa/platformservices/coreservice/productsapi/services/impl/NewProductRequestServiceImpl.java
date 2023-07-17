package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.productsapi.model.ProductRequestDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.TasksProductCreation;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.INewProductRequestService;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CategoryUtils;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * Service for creating new products
 */
@Service
public class NewProductRequestServiceImpl implements INewProductRequestService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(NewProductRequestServiceImpl.class);

    /**
     * CREATE PERMISSION
     */
    private static final NovaException CREATE_PERMISSION_DENIED = new NovaException(ProductsAPIError.getUserNotAllowedToCreateProductError(), ProductsAPIError.getUserNotAllowedToCreateProductError().toString());

    /**
     * Product repository instance
     */
    private ProductRepository productRepository;

    /**
     * Product API instance
     */
    private ProductsAPIToDoTaskService toDoTaskService;

    /**
     * Product user client
     */
    private IProductUsersClient usersClient;

    /**
     * Category repository
     */
    private CategoryRepository categoryRepository;

    /**
     *
     */
    private final CPDRepository cpdRepository;


    /**
     * Constructor
     *
     * @param productRepository                 product repository
     * @param toDoTaskService                   to do task service
     * @param usersClient                       user client
     * @param categoryRepository                category repository
     * @param cpdRepository                     cpd repository
     */
    @Autowired
    public NewProductRequestServiceImpl(final ProductRepository productRepository, final ProductsAPIToDoTaskService toDoTaskService, final IProductUsersClient usersClient,
                                        final CategoryRepository categoryRepository, final CPDRepository cpdRepository)
    {
        this.productRepository = productRepository;
        this.toDoTaskService = toDoTaskService;
        this.usersClient = usersClient;
        this.categoryRepository = categoryRepository;
        this.cpdRepository = cpdRepository;
    }

    /////////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////////

    @Override
    public Integer newProductRequest(final ProductRequestDTO newProductCreationRequest, String ivUser) throws NovaException
    {
        this.usersClient.checkHasPermission(ivUser, Constants.CREATE_PRODUCT_PERMISSION, CREATE_PERMISSION_DENIED);

        // Validate the request parameters: userCode, role of the user code, product name and uuaa name
        validateNewProductRequest(newProductCreationRequest, this.productRepository);

        // Success. Create new to do task: new creation product request
        return newCreationProductRequestTask(newProductCreationRequest, this.toDoTaskService);

    }

    @Override
    @Transactional
    public Product createAndSaveNewProduct(final ProductRequestDTO createNewProduct, final USUserDTO usUser) throws NovaException
    {
        LOG.debug("[ProductsAPI] -> [createAndSaveNewProduct]: Creating and saving into BBDD a new product via -> products/create. Product name: [{}] - Product owner code: [{}]" +
                "with initial status: [{}]", createNewProduct.getName(), usUser.getUserCode(), ProductStatus.CREATING);

        Product product = new Product();

        // Associate the main parameters of the product
        product.setName(createNewProduct.getName());
        product.setUuaa(createNewProduct.getUuaa().toUpperCase());
        product.setDescription(createNewProduct.getDescription());
        product.setType(createNewProduct.getProductType());

        // Set the initial status to the product: CREATING
        product.setProductStatus(ProductStatus.CREATING);
        LOG.debug("[ProductsAPI] -> [createAndSaveNewProduct]: The product name [{}] created with status [{}]", createNewProduct.getName(), ProductStatus.CREATING);

        // Iterate for each category and create a new one category in case of not exists
        product.setCategories(CategoryUtils.findAndInsertCategory(createNewProduct.getCategories(), categoryRepository));

        // Associate the email of the user code (product owner)
        product.setEmail(usUser.getEmail());

        // Two new mandatory fields. phone and issueKey
        if (createNewProduct.getPhone() != null)
        {
            product.setPhone(createNewProduct.getPhone());
        }
        if (createNewProduct.getJiraKey() != null)
        {
            product.setDesBoard(createNewProduct.getJiraKey());
        }

        // Set default creation product configurations.
        // by default when product is created, auto-deploy in pre is ACTIVE
        product.setDefaultAutodeployInPre(true);
        // by default when product is created, auto-deploy in pro is NOT ACTIVE
        product.setDefaultAutodeployInPro(false);
        // by default when product is created, auto-manage in pre is ACTIVE
        product.setDefaultAutomanageInPre(true);
        // by default when product is created, auto-manage in pro is NOT ACTIVE
        product.setDefaultAutomanageInPro(false);
        // by default when a new product is created, deployment type in pro is PLANNED (Control-M)
        product.setDefaultDeploymentTypeInPro(DeploymentType.PLANNED);
        // by default when a new product is created, CPD in pro is TC
        product.setCPDInPro(this.cpdRepository.getByNameAndEnvironmentAndMainSwarmCluster(Constants.TC_PRODUCTION_NAME, Environment.PRO.getEnvironment(), true));
        // by default when a new product is created, Multi CPD in PRO is false
        product.setMultiCPDInPro(false);

        // Save the product into BBDD
        product = productRepository.saveAndFlush(product);

        LOG.debug("[ProductsAPI] -> [createAndSaveNewProduct]: New product has been created and saved into BBDD with the following parameters: [{}]",
                product);

        return product;
    }

    ///////////////////////////////////////////// PRIVATE METHODS /////////////////////////////////////////////////////

    /**
     * Validate the product creation request parameters. The user code, if this user code is product owner and the
     * product name and uuaa name
     *
     * @param newProductCreationRequest    newProductCreationRequest
     * @param productsApiProductRepository productsApiProductRepository
     * @throws NovaException productsAPIException
     */
    private void validateNewProductRequest(final ProductRequestDTO newProductCreationRequest,
                                           final ProductRepository productsApiProductRepository) throws NovaException
    {
        // Validate if the product name and the uuaa name do not exists
        CommonsFunctions.validateProductNameAndUuaaName(newProductCreationRequest.getName().trim(), newProductCreationRequest.getUuaa().toUpperCase(),
                productsApiProductRepository);
    }

    /**
     * Create a new product request to do task
     *
     * @param newProductCreationRequest product to create
     * @param toDoTaskService           toDoTaskService
     * @return task id created
     * @throws NovaException productsAPIException
     */
    private Integer newCreationProductRequestTask(final ProductRequestDTO newProductCreationRequest,
                                                  final ProductsAPIToDoTaskService toDoTaskService) throws NovaException
    {

        LOG.debug("ProductsAPI: /products/newRequest -> creating a new product request with the following parameters: [{}]",
                newProductCreationRequest);

        // Create a new product request (fot create new to do task - creation new product task)
        TasksProductCreation productRequest = new TasksProductCreation();
        productRequest.setOwnerCode(newProductCreationRequest.getUserCode());
        productRequest.setUuaa(newProductCreationRequest.getUuaa().toUpperCase());
        productRequest.setDescription(newProductCreationRequest.getDescription());
        productRequest.setProductName(newProductCreationRequest.getName().trim());
        productRequest.setUserCode(newProductCreationRequest.getUserCode());
        productRequest.setCategories(newProductCreationRequest.getCategories());
        productRequest.setProductType(newProductCreationRequest.getProductType());

        // Two new mandatory fields. phone and issueKey
        if (newProductCreationRequest.getPhone() != null)
        {
            productRequest.setPhone(newProductCreationRequest.getPhone());
        }
        if (newProductCreationRequest.getJiraKey() != null)
        {
            productRequest.setJiraKey(newProductCreationRequest.getJiraKey());
        }

        // Call to do task service
        Integer todoTaskId = toDoTaskService.createProductCreationRequestTask(productRequest);

        LOG.debug("ProductsAPI: /products/newRequest -> new product request has benn created with the parameters: [{}] " +
                "and the task id has been created [{}].", newProductCreationRequest, todoTaskId);

        return todoTaskId;
    }
}
