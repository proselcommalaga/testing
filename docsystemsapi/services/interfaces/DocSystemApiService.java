package com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.docsystemsapi.model.DocNodeDto;
import com.bbva.enoa.apirestgen.docsystemsapi.model.DocSystemDto;

public interface DocSystemApiService
{
    /**
     * Deletes a DocSystem from a Product.
     *
     * @param ivUser      BBVA user code.
     * @param docSystemId The ID of the DocSystem to validateFilesystemDeletion.
     *                    If everything is ok - Success
     */
    void deleteDocSystem(final String ivUser, final Integer docSystemId);

    /**
     * Gets all the DocSystem attached to a Product.
     *
     * @param productId The ID of the Product the DocSystems belongs to.
     * @return Success.
     */
    DocNodeDto[] getProductDocSystems(final Integer productId);

    /**
     * Adds a new DocSystem to a Product.
     *
     * @param docSystemToAdd DocSystem to create and add to a Product.
     * @param ivUser         BBVA user code.
     * @param productId      The ID of the Product the DocSystems belong to.
     *                       If everything is ok - Success
     */
    void createDocSystem(final DocSystemDto docSystemToAdd, final String ivUser, final Integer productId);

    /**
     * Update an existing DocSystem
     *
     * @param docSystemUpdated DTO containing the new values for the DocSystem being updated.
     * @param docSystemId      ID of the DocSystem being updated.
     * @param ivUser           BBVA user code.
     */
    void updateDocSystem(final DocSystemDto docSystemUpdated, final Integer docSystemId, final String ivUser);

    /**
     * Get a DocNodeDto.
     *
     * @param docSystemId The ID of the DocSystem.
     * @return The DocNodeDto.
     */
    DocNodeDto getDocSystem(final Integer docSystemId);

    /**
     * Creates the necessary doc systems hierarchy for all the products (currently, this hierarchy is a set ot folders in Google Drive),
     * and migrates the already existing doc systems to a default category.
     *
     * @param ivUser BBVA user code.
     */
    void createDocSystemsHierarchy(String ivUser);

    /**
     * Creates a new folder in doc systems hierarchy for all existing products
     *
     * @param folderCategory folder to create (the category must exist)
     */
    void createDocSystemProductsFolder(String folderCategory);
}
