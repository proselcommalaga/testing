package com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces;

import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import java.util.List;

/**
 * Service for removing a product
 */
public interface IProductRemoveService
{
    /**
     * Removes a product
     *
     * @param ivUser    the user requesting the removal
     * @param productId the product id
     */
    void removeProduct(String ivUser, Integer productId);

    /**
     * Removes a product from BD
     *
     * @param ivUser  the user
     * @param product the product
     * @return the list of product owners for notification
     */
    List<USUserDTO> removeProductFromBBDD(String ivUser, Product product);

    /**
     * Send a notification to product owners of removed product
     *
     * @param product            the product deleted
     * @param productOwnersUsers the product owners
     */
    void sendRemoveProductNotification(Product product, List<USUserDTO> productOwnersUsers);
}
