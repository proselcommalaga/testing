package com.bbva.enoa.platformservices.coreservice.productsapi.util;

import com.bbva.enoa.apirestgen.productsapi.model.ProductBaseDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.release.entities.PlatformConfig;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProductConverterTest
{

    @Test
    public void ConvertProductEntityToProductsAPITest()
    {
        Product product = new Product();
        product.setId(1);
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(new Category());
        product.setCategories(categoryList);
        product.setProductStatus(ProductStatus.READY);
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        ProductDTO[] response = ProductConverter.convertProductEntityToProductsAPI(productList);
        assertEquals(1, response[0].getProductId().intValue());
    }

    @Test
    public void convertCategoryEntityListToStringListTest()
    {
        Category category = new Category();
        category.setName("Name");
        List<Category> categoryList = new ArrayList<>();
        categoryList.add(category);
        String[] response = ProductConverter.convertCategoryEntityListToStringList(categoryList);
        assertEquals("Name", response[0]);
    }


    @Test
    void convertProductEntityToProductSummaries()
    {
        Product product = this.generateProduct();

        ProductSummaryDTO productSummaryDTOToReturn = ProductConverter.convertProductEntityToProductSummaries(product);

        Assertions.assertNotNull(productSummaryDTOToReturn);
        Assertions.assertEquals(product.getId(),productSummaryDTOToReturn.getProductId());
        Assertions.assertEquals(product.getName(),productSummaryDTOToReturn.getName());
        Assertions.assertEquals(product.getDescription(),productSummaryDTOToReturn.getDescription());
        Assertions.assertEquals(product.getUuaa(),productSummaryDTOToReturn.getUuaa());
        Assertions.assertEquals(product.getType(),productSummaryDTOToReturn.getProductType());

    }


    @Test
    void convertProductEntityToProductBaseTest()
    {
        Product product = this.generateProduct();

        ProductBaseDTO productBaseDTOReturned = ProductConverter.convertProductEntityToProductBase(product);

        Assertions.assertEquals(product.getId(),productBaseDTOReturned.getProductId());
        Assertions.assertEquals(product.getName(),productBaseDTOReturned.getName());
        Assertions.assertEquals(product.getDescription(),productBaseDTOReturned.getDescription());
        Assertions.assertEquals(product.getUuaa(),productBaseDTOReturned.getUuaa());
        Assertions.assertEquals(product.getType(),productBaseDTOReturned.getProductType());

    }
    @Test
    void convertProductEntityToProductBaseNullCategoriesTest()
    {
        Product product = this.generateProduct();
        product.setCategories(null);

        ProductBaseDTO productBaseDTOReturned = ProductConverter.convertProductEntityToProductBase(product);

        Assertions.assertEquals(product.getId(),productBaseDTOReturned.getProductId());
        Assertions.assertEquals(product.getName(),productBaseDTOReturned.getName());
        Assertions.assertEquals(product.getDescription(),productBaseDTOReturned.getDescription());
        Assertions.assertEquals(product.getUuaa(),productBaseDTOReturned.getUuaa());
        Assertions.assertEquals(product.getType(),productBaseDTOReturned.getProductType());

    }

    private Product generateProduct()
    {
        Product product = new Product();
        product.setUuaa("UUAA");
        product.setImage("IMAGE");
        product.setName("NAME");
        product.setDescription("DESCRIPTION");
        product.setType("TYPE");
        product.setId(1);
        product.setProductStatus(ProductStatus.READY);
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfig = new PlatformConfig();
        platformConfig.setIsDefault(true);
        platformConfig.setEnvironment(Environment.INT.getEnvironment());
        platformConfig.setPlatform(Platform.NOVAETHER);
        platformConfig.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigList.add(platformConfig);

        product.setPlatformConfigList(platformConfigList);
        product.setCategories(this.generateCategoryList());

        product.setDefaultAutodeployInPre(true);
        product.setDefaultAutodeployInPro(true);
        product.setDefaultAutomanageInPre(true);
        product.setDefaultAutomanageInPro(true);

        product.setDefaultDeploymentTypeInPro(DeploymentType.NOVA_PLANNED);


        return product;
    }

    private List<Category> generateCategoryList()
    {

        List<Category> categoryList = new ArrayList<>();

        categoryList.add(new Category("typedCategory1"));
        categoryList.get(0).setId(1);
        categoryList.add(new Category("typedCategory2"));
        categoryList.get(1).setId(2);
        return categoryList;

    }

}