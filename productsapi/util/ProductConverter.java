package com.bbva.enoa.platformservices.coreservice.productsapi.util;

import com.bbva.enoa.apirestgen.productsapi.model.InfrastructureConfigDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductBaseDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductCommonConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductDeploymentConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductManagementConfigurationDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Util class dedicated to convert the Product object from JPA model to Other products objects
 * Created by BBVA - xe30432 on 02/02/2017.
 */
public final class ProductConverter
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductConverter.class);

    /**
     * Empty constructor
     */
    private ProductConverter()
    {
        //Empty constructor
    }

    /**
     * Convert the Product entity to ProductsAPI object to be returned to frontal web
     *
     * @param productEntity Product entity from BBDD
     * @return ProductsAPI instance with only the necessary attributes to frontal web
     */
    public static ProductDTO[] convertProductEntityToProductsAPI(final List<Product> productEntity)
    {
        return convertProductEntityToProductsAPI(productEntity, false);
    }

    /**
     * Convert the Product entity to ProductsAPI object to be returned to frontal web
     *
     * @param productEntity Product entity from BBDD
     * @return ProductsAPI instance with only the necessary attributes to frontal web
     */
    public static ProductDTO[] convertProductEntityToProductsAPI(final List<Product> productEntity, final boolean includeImages)
    {
        List<ProductDTO> productJSONArrayList = new ArrayList<>();
        LOG.debug("ProductsAPI: Converting the following Product list (from BBDD) to ProductJSON: [{}]", productEntity);

        // Convert Product instance to ProductJSON
        for (Product product : productEntity)
        {
            ProductDTO productJSON = new ProductDTO();
            productJSON.setProductId(product.getId());
            productJSON.setName(product.getName());
            productJSON.setDescription(product.getDescription());
            productJSON.setUuaa(product.getUuaa());
            productJSON.setStatus(product.getProductStatus().getProductStatus());
            productJSON.setProductType(product.getType());

            if (includeImages)
            {
                productJSON.setImage(product.getImage());
            }

            // Convert calendar to string dd/MM/yyyy
            if (product.getCreationDate() != null)
            {
                DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                String creationDate = formatter.format(product.getCreationDate().getTime());
                productJSON.setCreationDate(creationDate);
            }


            // Fill the string array wiht all the categories of the product.
            productJSON.setCategories(fillCategories(product.getCategories(), product.getName()));

            // Fill productJSON array list
            productJSONArrayList.add(productJSON);
        }

        // Create the productJSONS Array
        ProductDTO[] productJSONSArray = new ProductDTO[productEntity.size()];
        LOG.trace("ProductsAPI: The productJSON converted list from BBDD were: [{}]", productJSONArrayList);
        productJSONArrayList.sort(Comparator.comparing(ProductDTO::getUuaa));

        return productJSONArrayList.toArray(productJSONSArray);
    }

    /**
     * Convert the Product entity to ProductSummary object to be returned to frontal web
     *
     * @param productEntity Product entity from BBDD
     * @return the product summary converted
     */
    public static ProductSummaryDTO convertProductEntityToProductSummaries(final Product productEntity)
    {
        LOG.debug("ProductsAPI: Converting the following Product (from BBDD) to ProductSummaries: [{}]", productEntity);

        ProductSummaryDTO productSummaryDTO = new ProductSummaryDTO();

        productSummaryDTO.setProductId(productEntity.getId());
        productSummaryDTO.setCategories(fillCategories(productEntity.getCategories(), productEntity.getName()));
        productSummaryDTO.setName(productEntity.getName());
        productSummaryDTO.setImage(productEntity.getImage());
        productSummaryDTO.setDescription(productEntity.getDescription());
        productSummaryDTO.setEmail(productEntity.getEmail());
        productSummaryDTO.setUuaa(productEntity.getUuaa());
        productSummaryDTO.setProductType(productEntity.getType());
        productSummaryDTO.setStatus(productEntity.getProductStatus().getProductStatus());
        productSummaryDTO.setPhone(productEntity.getPhone());
        productSummaryDTO.setRemedySupportGroup(productEntity.getRemedySupportGroup());
        productSummaryDTO.setCreateJiraIssues(productEntity.getCreateJiraIssues());

        String desBoard = "";
        if (productEntity.getDesBoard() != null)
        {
            desBoard = productEntity.getDesBoard();
        }
        productSummaryDTO.setDesBoard(desBoard);

        // Convert calendar to string dd/MM/yyyy
        if (productEntity.getCreationDate() != null)
        {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String creationDate = formatter.format(productEntity.getCreationDate().getTime());
            productSummaryDTO.setCreationDate(creationDate);
        }

        final ProductConfigurationDTO productConfigurationDTO = new ProductConfigurationDTO();
        productConfigurationDTO.setCommon(convertProductEntityToProductCommonConfiguration(productEntity));
        productConfigurationDTO.setDeployment(convertProductEntityToProductDeploymentConfiguration(productEntity));

        ProductManagementConfigurationDTO managementDTO = new ProductManagementConfigurationDTO();
        managementDTO.setDefaultAutodeployInPre(productEntity.getDefaultAutodeployInPre());
        managementDTO.setDefaultAutodeployInPro(productEntity.getDefaultAutodeployInPro());
        managementDTO.setDefaultAutomanageInPre(productEntity.getDefaultAutomanageInPre());
        managementDTO.setDefaultAutomanageInPro(productEntity.getDefaultAutomanageInPro());
        managementDTO.setDefaultDeploymentTypeInPro(String.valueOf(productEntity.getDefaultDeploymentTypeInPro()));
        productConfigurationDTO.setManagement(managementDTO);
        productSummaryDTO.setConfiguration(productConfigurationDTO);

        LOG.trace("ProductsAPI: The productSummary converted from BBDD were: [{}]", productSummaryDTO);

        return productSummaryDTO;
    }

    /**
     * Convert a category entity list (from BBDD) to category string list
     *
     * @param categoryList all categories in NOVA from NOVA products
     * @return string category list
     */
    public static String[] convertCategoryEntityListToStringList(final List<Category> categoryList)
    {
        List<String> categoryJSONArrayList = new ArrayList<>();
        LOG.debug("ProductsAPI: Converting the following Category entity list (from BBDD) to CategoryJSON: [{}]", categoryList);

        // Convert Product instance to ProductJSON
        for (Category category : categoryList)
        {
            categoryJSONArrayList.add(category.getName());
        }

        // Create the string array
        String[] stringArray = new String[categoryList.size()];
        LOG.debug("ProductsAPI: The Category list converted from BBDD were: [{}]", categoryJSONArrayList);

        return categoryJSONArrayList.toArray(stringArray);
    }

    /**
     * Convert the Product entity to ProductBase object to be returned to frontal web
     *
     * @param productEntity Product entity from BBDD
     * @return the product base converted
     */
    public static ProductBaseDTO convertProductEntityToProductBase(final Product productEntity)
    {
        LOG.debug("ProductsAPI: Converting the following Product (from BBDD) to ProductsBase: [{}]", productEntity);

        ProductBaseDTO productBaseDTO = new ProductBaseDTO();

        productBaseDTO.setProductId(productEntity.getId());
        productBaseDTO.setName(productEntity.getName());
        productBaseDTO.setDescription(productEntity.getDescription());
        productBaseDTO.setUuaa(productEntity.getUuaa());
        productBaseDTO.setProductType(productEntity.getType());

        // Fill the string array wiht all the categories of the product.
        productBaseDTO.setCategories(fillCategories(productEntity.getCategories(), productEntity.getName()));

        LOG.trace("ProductsAPI: The productBase converted from BBDD were: [{}]", productBaseDTO);

        return productBaseDTO;
    }

    // ******************************* PRIVATE METHODS ***************************************************


    /**
     * Fill the categories. Convert a list {@code Categories} to array String[]
     *
     * @param categoryList Category list instance
     * @return a string array with the categories
     */
    private static String[] fillCategories(final List<Category> categoryList, final String productName)
    {
        String[] categoriesStringsArray = new String[0];

        if (categoryList == null || categoryList.isEmpty())
        {
            LOG.debug("ProductsAPI: the product name: [{}] does not have categories assigned.", productName);
        }
        else
        {
            categoriesStringsArray = new String[categoryList.size()];

            for (int i = 0; i < categoriesStringsArray.length; i++)
            {
                categoriesStringsArray[i] = categoryList.get(i).getName();
            }
        }

        return categoriesStringsArray;
    }

    /**
     * Transforms a Product entity into a {@link ProductCommonConfigurationDTO} with all the common configurations of the product
     *
     * @param productEntity {@link Product} entity
     * @return {@link ProductCommonConfigurationDTO} containing the common configurations of the product
     */
    private static ProductCommonConfigurationDTO convertProductEntityToProductCommonConfiguration(final Product productEntity)
    {
        final ProductCommonConfigurationDTO productCommonConfigurationDTO = new ProductCommonConfigurationDTO();
        productCommonConfigurationDTO.setCriticalityLevel(productEntity.getCriticalityLevel());
        productCommonConfigurationDTO.setDevelopment(productEntity.getDevelopment());
        productCommonConfigurationDTO.setQualityLevel(productEntity.getQualityLevel());
        productCommonConfigurationDTO.setReleaseSlots(productEntity.getReleaseSlots());
        productCommonConfigurationDTO.setBatchLimitRepeat(productEntity.getBatchLimitRepeat());

        return productCommonConfigurationDTO;
    }

    /**
     * Transforms a Product entity into a an Array of {@link ProductDeploymentConfigurationDTO} for all environments
     *
     * @param productEntity {@link Product} entity
     * @return Array of {@link ProductDeploymentConfigurationDTO} containing the deployment configuration of the product for all the environments
     */
    private static ProductDeploymentConfigurationDTO[] convertProductEntityToProductDeploymentConfiguration(final Product productEntity)
    {
        return EnumSet.allOf(Environment.class).stream()
                // Generate a ProductDeploymentConfigurationDTO for each Environment
                .map(env -> populateProductDeploymentConfiguration(productEntity, env))
                // Collect all ProductDeploymentConfigurationDTO into a List
                .collect(Collectors.toList())
                // Transform the List into an Array
                .toArray(ProductDeploymentConfigurationDTO[]::new);
    }

    /**
     * Transforms a Product entity into a ProductDeploymentConfigurationDTO for a given environment
     *
     * @param product     {@link Product} entity
     * @param environment {@link Environment}
     * @return {@link ProductDeploymentConfigurationDTO} containing the deployment configuration of the product for the given environment
     */
    private static ProductDeploymentConfigurationDTO populateProductDeploymentConfiguration(Product product, Environment environment)
    {
        ProductDeploymentConfigurationDTO pdcDTO = new ProductDeploymentConfigurationDTO();
        pdcDTO.setEnvironment(environment.name());
        pdcDTO.setDeploymentInfraestructure(new InfrastructureConfigDTO[3]);
        pdcDTO.setLoggingInfraestructure(new InfrastructureConfigDTO[3]);

        switch (environment)
        {
            case INT:
                pdcDTO.setDeploymentInfraestructure(mapProductEnvironmentConfiguration(product, ConfigurationType.DEPLOY, environment));
                pdcDTO.setLoggingInfraestructure(mapProductEnvironmentConfiguration(product, ConfigurationType.LOGGING, environment));
                pdcDTO.setEtherNs(product.getEtherNsInt());
                pdcDTO.setMicrogwLogLevel(product.getMgwLogLevelInt().name());
                pdcDTO.setCesEnabled(product.isCesEnabledInt());
                break;
            case PRE:
                pdcDTO.setDeploymentInfraestructure(mapProductEnvironmentConfiguration(product, ConfigurationType.DEPLOY, environment));
                pdcDTO.setLoggingInfraestructure(mapProductEnvironmentConfiguration(product, ConfigurationType.LOGGING, environment));
                pdcDTO.setEtherNs(product.getEtherNsPre());
                pdcDTO.setMicrogwLogLevel(product.getMgwLogLevelPre().name());
                pdcDTO.setCesEnabled(product.isCesEnabledPre());
                break;
            case PRO:
                pdcDTO.setDeploymentInfraestructure(mapProductEnvironmentConfiguration(product,  ConfigurationType.DEPLOY, environment));
                pdcDTO.setLoggingInfraestructure(mapProductEnvironmentConfiguration(product,  ConfigurationType.LOGGING, environment));
                pdcDTO.setEtherNs(product.getEtherNsPro());
                pdcDTO.setMulticpd(product.getMultiCPDInPro());
                pdcDTO.setCpd(product.getCPDInPro() != null ? product.getCPDInPro().getName() : null);
                pdcDTO.setMicrogwLogLevel(product.getMgwLogLevelPro().name());
                pdcDTO.setCesEnabled(product.isCesEnabledPro());
                break;
        }
        return pdcDTO;
    }

    private static InfrastructureConfigDTO[] mapProductEnvironmentConfiguration(Product product, ConfigurationType configurationType, Environment environment)
    {
        // Get from Product config by Environment and ConfigurationType
        List<PlatformConfig> platformConfigList = product.getPlatformConfigList().stream().filter(
                row ->
                        row.getEnvironment().toString().equalsIgnoreCase(environment.toString()) //TODO: por que al comparar Environment y Environment da siempre false?
                        && row.getConfigurationType().equals(configurationType)
                ).collect(Collectors.toList());

        // Map from PlatformConfig to InfrastructureConfigDTO array
        List<InfrastructureConfigDTO> infrastructureConfigDTOList =
                platformConfigList.stream().map(
                        platformConfig -> {
                            InfrastructureConfigDTO infrastructureConfigDTO = new InfrastructureConfigDTO();
                            infrastructureConfigDTO.setInfrastructureName(platformConfig.getPlatform().getName());
                            infrastructureConfigDTO.setIsDefault(platformConfig.getIsDefault());
                            infrastructureConfigDTO.setIsEnabled(true);
                            return infrastructureConfigDTO;
                }).collect(Collectors.toList());

        //TODO: Map Platform that are not present in the list created before (platformConfigList).
        // If one platform is not in that list, it means it is not configured.

        return infrastructureConfigDTOList.toArray(InfrastructureConfigDTO[]::new);
    }
}
