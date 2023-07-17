package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

import com.bbva.enoa.apirestgen.productsapi.model.ProductSummaryDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import javax.transaction.Transactional;

/**
 * Services for end point update product
 * Created by BBVA - xe30432 on 02/02/2017.
 */
public interface IUpdateProductService
{
    /**
     * Updates a product information
     *
     * @param updateProduct info for updating
     * @param ivUser        logged user
     * @return the new product info
     */
    Product updateProduct(ProductSummaryDTO updateProduct, String ivUser);

    /**
     * Saves the new product info into bbdd
     *
     * @param ivUser          logged user
     * @param productSummary  info to save
     * @param productToUpdate product to update
     */
    @Transactional
    void updateProductAndSaveBBDD(String ivUser, ProductSummaryDTO productSummary, Product productToUpdate);
}
