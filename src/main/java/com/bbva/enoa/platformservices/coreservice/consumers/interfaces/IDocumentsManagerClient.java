package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentDTO;
import com.bbva.enoa.datamodel.model.product.entities.Product;

import java.util.List;

/**
 * Client to communicate to Documents Manager Service
 *
 * @author vbazagad
 */
public interface IDocumentsManagerClient
{

    /**
     * Create a root folder for this product
     *
     * @param product  Product to create root folder for
     * @param userCode User that requested the product creation
     * @return A DMDocumentDTO with the url for the generated folder
     */
    DMDocumentDTO createRoot(Product product, String userCode);

    /**
     * Adds a user to the root folder fo specified product
     *
     * @param product  Product to add user to
     * @param email    Email of the user to be added
     * @param userCode code of the user that requested the user addition.
     */
    void addUser(Product product, String email, String userCode);

    /**
     * Removes user on the products folder
     *
     * @param product  Product to remove user form
     * @param email    Email of the user to remove
     * @param userCode UserCode of the user that requested the user deletion.
     */
    void removeUser(Product product, String email, String userCode);

    /**
     * Removes a rootFolder from documentsManager
     *
     * @param product Product that is being deleted.
     */
    void removeRoot(Product product);

    /**
     * Create the expected documents' folder for a product.
     *
     * @param product The product.
     * @return A DMDocumentDTO which will contain the URLs of the created folders.
     */
    DMDocumentDTO createDocumentFoldersForProduct(Product product, List<String> folders);
}
