package com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces;

import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;

import java.util.List;
import java.util.Optional;

/**
 * Service to operate on default DocSystem
 *
 * @author vbazagad
 */
public interface IDocSystemService
{

    /**
     * Creates a Default DocSystem for specified product
     *
     * @param product Product to create defaultDocSystem on
     * @param user    User that requested product creation.
     */
    void createDefaultDocSystem(Product product, USUserDTO user);

    /**
     * Build the entities for the children folders of root document, save them and associate them with a product.
     *
     * @param product            The given product.
     * @param rootDocumentDTO    The root document DTO where the children folders are defined.
     * @param rootDocumentEntity The root document entity that the children belong to.
     * @return The List of children entities.
     */
    List<DocSystem> buildAndSaveChildrenFolders(Product product, DMDocumentDTO rootDocumentDTO, DocSystem rootDocumentEntity);

    /**
     * Adds permissions for the user to access products folder
     *
     * @param product  Product folder belongs to
     * @param user     User requesting the creation.
     * @param userCode user code of the requester
     */
    void addUserToDefaultDocSystem(Product product, USUserDTO user, String userCode);

    /**
     * Removes permission for the user to access the project folder
     *
     * @param product  Product the root folder belongs to
     * @param user     User to undeployPlan permissions on the folder
     * @param userCode userCode of the user that requested the user removal.
     */
    void removeUserFromDefaultDocSystem(Product product, USUserDTO user, String userCode);

    /**
     * Removes the root folder of the specified Product
     *
     * @param product Product to undeployPlan rootFolder for
     */
    void removeDefaultRepository(Product product);

    /**
     * Get a DocSystem with a specific ID, category and type.
     *
     * @param docSystemId      The given ID.
     * @param documentCategory The given category.
     * @param documentType     The given type.
     * @return The DocSystem.
     */
    Optional<DocSystem> getDocSystemWithIdAndCategoryAndType(Integer docSystemId, DocumentCategory documentCategory, DocumentType documentType);


}
