package com.bbva.enoa.platformservices.coreservice.productsapi.util;

import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Services for commons function
 * Created by BBVA - xe30432 on 02/02/2017.
 */
public final class CommonsFunctions
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CommonsFunctions.class);

    /**
     * UUAA length
     */
    public static final int UUAA_LENGTH = 4;

    /**
     * Allowed length for the name of a product
     */
    public static final int NAME_MIN_LENGTH = 1;
    public static final int NAME_MAX_LENGTH = 64;

    /** Regular expression patter for a valid UUAA */
    public static final String UUAA_REGEX_PATTERN = "[a-zA-Z0-9]+";

    /** Regular expression to validate emails */
    public static final String EMAIL_REGEX_PATTERN = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    public static final String PRODUCT_NAME_FORBIDDEN_CHARS = "()";

    /**
     * Empty constructor
     */
    private CommonsFunctions()
    {
        // Empty constructor
    }

    /**
     * Validate product name and uuaa name
     *
     * @param productName                  product name to validate
     * @param uuaaName                     uuaa name to validate
     * @param productsApiProductRepository productsApiProductRepository
     */
    public static void validateProductNameAndUuaaName(final String productName, final String uuaaName, final ProductRepository productsApiProductRepository)
    {
        LOG.debug("ProductsAPI: products/acceptProductName: Validating the product name [{}]", productName);
        checkProductNameHasForbiddenCharacters(productName);
        validateProductName(productName, productsApiProductRepository);
        validateProductNameLength(productName);
        checkProductNameExistingUUAA(productName, productsApiProductRepository);

        LOG.debug("ProductsAPI: products/acceptUUAACode: Validating the uuaa name [{}]", uuaaName);
        checkUuaaNull(uuaaName);
        checkUuaaExistance(uuaaName, productsApiProductRepository);
    }

    /**
     * Trim and convert to upper case.
     *
     * @param uuaa UUAA
     * @throws NovaException If the UUAA is null or has an invalid format.
     */
    public static String getCleanUuaa(String uuaa)
    {
        if (uuaa == null)
        {
            throw new NovaException(ProductsAPIError.getIncorrectUUAAErrorBadRequest());
        }
        uuaa = uuaa.trim().toUpperCase(Locale.ROOT);
        checkUuaaNull(uuaa);
        return uuaa;
    }

    /**
     * Check if product name is an existing uuaa
     *
     * @param productName                  uuaa name
     * @param productsApiProductRepository product repository
     */
    private static void checkProductNameExistingUUAA(String productName, ProductRepository productsApiProductRepository)
    {
        // Check if the uuaa exists
        if (productsApiProductRepository.findByUuaaIgnoreCase(productName.toUpperCase()) == null)
        {
            LOG.debug("ProductsAPI: products/acceptProductName: Accepted. The product name [{}] does not exist as UUAA into BBDD", productName);
        }
        else
        {
            LOG.error("ProductsAPI: products/acceptProductName -> The product name [{}] already exists as UUAA of other product into the BBDD. Error code: [{}]",
                    productName, ProductsAPIError.getProductNameExistingUUAAError());
            throw new NovaException(ProductsAPIError.getProductNameExistingUUAAError(), "ProductsAPI: products/newRequest"
                    + productName + "-> The product name already exists as UUAA of other product into the BBDD");
        }
    }



    /**
     * Check if uuaa already exists
     *
     * @param uuaaName                     uuaa name
     * @param productsApiProductRepository product repository
     */
    private static void checkUuaaExistance(String uuaaName, ProductRepository productsApiProductRepository)
    {
        // Check if the uuaa exists
        if (productsApiProductRepository.findByUuaaIgnoreCase(uuaaName.toUpperCase()) == null)
        {
            LOG.debug("ProductsAPI: products/acceptProductName: Accepted. The uuaa name [{}] does not exist into BBDD", uuaaName);
        }
        else
        {
            LOG.error("ProductsAPI: products/acceptUUAACode -> The uuaa name [{}] is already exist into the BBDD. Error code: [{}]",
                    uuaaName, ProductsAPIError.getProductUUAAAlreadyExistError());
            throw new NovaException(ProductsAPIError.getProductUUAAAlreadyExistError(), "ProductsAPI: products/newRequest"
                    + uuaaName + "-> The uuaa name is already exists into the BBDD.");
        }
    }

    /**
     * Check if uuaa name is null
     *
     * @param uuaaName uuaa name
     */
    private static void checkUuaaNull(String uuaaName)
    {
        //Check if uuaa is null
        if (!StringUtils.hasText(uuaaName) || (uuaaName.length() != UUAA_LENGTH) || (!uuaaName.matches(UUAA_REGEX_PATTERN)))
        {
            LOG.error("ProductsAPI: products/acceptUUAACode -> The uuaa name [{}] is incorrect. Error code: [{}]",
                    uuaaName, ProductsAPIError.getIncorrectUUAAError());
            throw new NovaException(ProductsAPIError.getIncorrectUUAAError(),
                    "ProductsAPI: products/newRequest -> The uuaa name [" + uuaaName + "] is incorrect.");
        }
    }


    /**
     * Check if product name contains forbidden characters
     *
     * @param productName product name
     */
    private static void checkProductNameHasForbiddenCharacters(String productName)
    {
        if (productName.matches(".*[" + PRODUCT_NAME_FORBIDDEN_CHARS +"].*"))
        {
            LOG.error("ProductsAPI: products/acceptProductName -> The product name [{}] contains forbidden characters. Error code: [{}]",
                    productName, ProductsAPIError.getIncorrectProductNameError());
            throw new NovaException(ProductsAPIError.getIncorrectProductNameError(),
                    "ProductsAPI: products/newRequest -> The product name [" + productName + "] contains forbidden characters.");
        }
    }

    /**
     * Validate product name
     *
     * @param productName                  product name
     * @param productsApiProductRepository product repository
     */
    private static void validateProductName(String productName, ProductRepository productsApiProductRepository)
    {
        // If the product object is null means that product name does not exist into BBDD - case insensitive.
        if (productsApiProductRepository.findByName(productName.toUpperCase()) == null)
        {
            LOG.debug("ProductsAPI: products/acceptProductName: Accepted. The product name [{}] does not exist into BBDD", productName);
        }
        else
        {
            LOG.error("ProductsAPI: products/newRequest -> The product name [{}] is already exist into the BBDD. Error code [{}]",
                    productName, ProductsAPIError.getProductAlreadyExistError());
            throw new NovaException(ProductsAPIError.getProductAlreadyExistError(), "ProductsAPI: products/newRequest"
                    + productName + "-> The product name is already exist into the BBDD.");
        }
    }

    /**
     * Validates the length of the name of a product
     * @param productName The name of a product
     */
    private static void validateProductNameLength(String productName)
    {
        if (productName.length() < NAME_MIN_LENGTH || productName.length() > NAME_MAX_LENGTH)
        {
            throw new NovaException(ProductsAPIError.getInvalidProductNameLengthError(productName, NAME_MIN_LENGTH, NAME_MAX_LENGTH));
        }
    }

    /**
     * Validate product
     *
     * @param productsApiProductRepository productsApiProductRepository
     * @param productId                    product id to validate
     * @return the product validated
     */
    public static Product validateProduct(final ProductRepository productsApiProductRepository, final Integer productId)
    {
        try
        {
            // Validate product.
            Product product = productsApiProductRepository.findById(productId).orElse(null);
            checkProduct(productId, product);
            return product;

        }catch(NovaException e){
            LOG.error("[CommonsFunctions] -> [validateProduct]: Error obtaining product with id [{}] from database", productId);

            throw new NovaException(ProductsAPIError.getDatabaseConnectionError());
        }
    }

    /**
     * Validate a ProductSummaryDTO before update a product
     *
     * @param updateProduct ProductSummary with new information to set into product
     * @throws NovaException if there is any invalid information
     */
    public static void validateProductSummaryUpdate(final ProductSummaryDTO updateProduct) throws NovaException
    {
       //check mail

        if(StringUtils.hasText(updateProduct.getEmail()) && !Pattern.matches(EMAIL_REGEX_PATTERN, updateProduct.getEmail().trim()))
        {
            LOG.error("ProductsAPI: products/updateProduct error. The new email to set into product [{}] has an invalid format: [{}] ",
                    updateProduct.getProductId(), updateProduct.getEmail());
            throw new NovaException(ProductsAPIError.getWrongEmailFormatError(), "ProductsAPI: products/updateProduct/"
                    + updateProduct.getProductId() + "-> Email with wrong format [" + updateProduct.getEmail() + "]");
        }
    }

    /**
     * Validate product uuaa
     *
     * @param productsApiProductRepository  productsApiProductRepository
     * @param uuaa                          Product logic name UUAA identifies the product in BBVA
     * @return                              the product validated
     */
    public static Product validateProductUuaa(final ProductRepository productsApiProductRepository, final String uuaa)
    {
        // Validate product.
        Product product = productsApiProductRepository.findOneByUuaa(uuaa);
        checkProductUuaa(uuaa, product);
        return product;
    }

    public static Environment validateEnvironment(String env)
    {
        Environment environment = null;
        try
        {
            environment = Environment.valueOf(env);
        }
        catch (IllegalArgumentException e)
        {
            LOG.error("");
            throw new NovaException(ProductsAPIError.getInvalidEnvironmentError(env));
        }

        return environment;
    }

    public static DeploymentType validateDeploymentType(String deploymentTypeString)
    {
        DeploymentType deploymentType = null;
        try
        {
            deploymentType = DeploymentType.valueOf(deploymentTypeString);
        }
        catch (IllegalArgumentException e)
        {
            LOG.error("");
            throw new NovaException(ProductsAPIError.getInvalidDeploymentTypeError(deploymentTypeString));
        }
        return deploymentType;
    }

    /**
     * Check if product is null
     *
     * @param productId product id
     * @param product   product
     */
    private static void checkProduct(final Integer productId, final Product product)
    {
        // Check if the product exist into the BBDD
        if (product == null)
        {
            LOG.error("ProductsAPI: products/validateProduct error. The product id [{}] does not exist into the BBDD. Error Code: [{}]",
                    productId, ProductsAPIError.getProductDoesNotExistError());
            throw new NovaException(ProductsAPIError.getProductDoesNotExistError(), "ProductsAPI: products/validateProduct/"
                    + productId + "-> The product id does not exist into the BBDD.");
        }
    }

    /**
     * Check if product is null
     *
     * @param uuaa      Product logic name UUAA identifies the product in BBVA
     * @param product   product
     */
    private static void checkProductUuaa(final String uuaa, final Product product)
    {
        // Check if the product exist into the BBDD
        if (product == null)
        {
            LOG.error("ProductsAPI: products/validateProduct error. The product with uuaa [{}] does not exist into the BBDD. Error Code: [{}]",
                    uuaa, ProductsAPIError.getProductDoesNotExistError());
            throw new NovaException(ProductsAPIError.getProductDoesNotExistError(), "ProductsAPI: products/validateProduct/"
                    + uuaa + "-> The product uuaa does not exist into the BBDD.");
        }
    }
}
