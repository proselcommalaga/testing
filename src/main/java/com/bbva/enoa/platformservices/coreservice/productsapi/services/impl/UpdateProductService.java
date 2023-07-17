package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.datamodel.model.product.entities.Category;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CategoryRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IUpdateProductService;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CategoryUtils;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.CommonsFunctions;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.stream.Collectors;

/**
 * Service for updating product status
 */
@Service
public class UpdateProductService implements IUpdateProductService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(UpdateProductService.class);

    /**
     * Permission error
     */
    private static final NovaException MANAGE_PERMISSION_DENIED = new NovaException(ProductsAPIError.getForbiddenError(), ProductsAPIError.getForbiddenError().toString());

    /**
     * Product repository
     */
    private ProductRepository productRepository;

    /**
     * Category repository instance
     */
    private CategoryRepository categoryRepository;

    /**
     * Users client
     */
    private IProductUsersClient usersClient;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    @Autowired
    public UpdateProductService(final ProductRepository productRepository, final CategoryRepository categoryRepository, final IProductUsersClient usersClient,
                                final INovaActivityEmitter novaActivityEmitter)
    {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.usersClient = usersClient;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    /**
     * Updates a product information
     *
     * @param updateProduct info for updating
     * @param ivUser        logged user
     * @return the new product info
     */
    @Override
    public Product updateProduct(ProductSummaryDTO updateProduct, final String ivUser)
    {

        this.usersClient.checkHasPermission(ivUser, Constants.EDIT_PRODUCT_PERMISSION, updateProduct.getProductId(), MANAGE_PERMISSION_DENIED);

        // Validate product.
        Product product = CommonsFunctions.validateProduct(this.productRepository, updateProduct.getProductId());

        //Validate ProductSummary with new info to set
        CommonsFunctions.validateProductSummaryUpdate(updateProduct);

        // Check if the product name and uuaa are the same. In this case, update into BBDD
        if (product.getUuaa().equals(updateProduct.getUuaa()))
        {
            // Update the product
            this.updateProductAndSaveBBDD(ivUser, updateProduct, product);

            LOG.debug("ProductsAPI: products/update - The product [{}] has been updated into BBDD successful. New product updated: [{}]", product.getName(), product);
        }
        else
        {
            LOG.error("ProductsAPI: products/update error. The product name [{}] and uuaa [{}] are not the same in the BBDD." +
                            " Both must macht into bbdd and formulary. Info product from BBDD: Name [{}] - UUAA [{}]. Error Code: [{}]",
                    updateProduct.getName(), updateProduct.getUuaa(), product.getName(), product.getUuaa(), ProductsAPIError.getProductAndUUAANotMatchError());
            throw new NovaException(ProductsAPIError.getProductAndUUAANotMatchError(), "ProductsAPI: products/update"
                    + updateProduct.getName() + "-> The product name and the uuaa does not macht into the BBDD.");
        }
        return product;
    }

    /**
     * Update only the fields provided from ProductSummary via frontal web (edit products) for this product into BBDD
     *
     * @param productSummaryDTO product with the new changes in the product.
     * @param productToUpdate   product to be updated
     */
    @Override
    @Transactional
    public void updateProductAndSaveBBDD(final String ivUser, final ProductSummaryDTO productSummaryDTO, final Product productToUpdate)
    {
        LOG.debug("ProductsAPI: Updating the this current product before updating [{}] with the new data from web [{}]",
                productToUpdate, productSummaryDTO);

        // Iterate for each category and create a new one category in case of not exists
        productToUpdate.setCategories(CategoryUtils.findAndInsertCategory(productSummaryDTO.getCategories(),
                this.categoryRepository));

        // Insert the others values
        productToUpdate.setDescription(productSummaryDTO.getDescription());
        productToUpdate.setEmail(productSummaryDTO.getEmail());
        productToUpdate.setImage(productSummaryDTO.getImage());
        productToUpdate.setPhone(productSummaryDTO.getPhone());
        productToUpdate.setDesBoard(productSummaryDTO.getDesBoard());
        productToUpdate.setRemedySupportGroup(productSummaryDTO.getRemedySupportGroup());
        productToUpdate.setType(productSummaryDTO.getProductType());

        // Save changes into BBDD
        this.productRepository.saveAndFlush(productToUpdate);

        String categories = productToUpdate.getCategories().stream().map(Category::getName).collect(Collectors.joining(", "));

        // Create new activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(productToUpdate.getId(), ActivityScope.PRODUCT, ActivityAction.EDITED)
                .entityId(productToUpdate.getId())
                .addParam("description", productToUpdate.getDescription())
                .addParam("email", productToUpdate.getEmail())
                .addParam("image", productToUpdate.getImage())
                .addParam("phone", productToUpdate.getPhone())
                .addParam("desBoard", productToUpdate.getDesBoard())
                .addParam("remedySupportGroup", productToUpdate.getRemedySupportGroup())
                .addParam("type", productToUpdate.getType())
                .addParam("categories", categories)
                .build());

        LOG.debug("ProductsAPI: The Product was updated: Product after updating [{}]", productToUpdate);
    }
}