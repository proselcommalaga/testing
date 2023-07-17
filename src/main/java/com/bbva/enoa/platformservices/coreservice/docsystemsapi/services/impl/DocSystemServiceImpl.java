package com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.impl;

import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentDTO;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.DMDocumentFolderDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DocSystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDocumentsManagerClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions.DocSystemErrorCode;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.DocSystemUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of DocSystem logic
 *
 * @author vbazagad
 */
@Service
public class DocSystemServiceImpl implements IDocSystemService
{
    /**
     * Logging
     */
    private static final Logger LOG = LoggerFactory.getLogger(DocSystemServiceImpl.class);
    /**
     * DocSystem repository
     */
    private DocSystemRepository repository;
    /**
     * Documents Manager client
     */
    private IDocumentsManagerClient docsClient;

    /**
     * Constructor for SystemServiceImple
     *
     * @param docsClient DocumentsClient to use
     * @param repository DocSystemRepository to use
     */
    public DocSystemServiceImpl(IDocumentsManagerClient docsClient, DocSystemRepository repository)
    {
        this.repository = repository;
        this.docsClient = docsClient;
    }

    /**
     * Creates a Default DocSystem for specified product
     *
     * @param product Product to create defaultDocSystem on
     * @param user    User that requested product creation.
     */
    @Override
    public void createDefaultDocSystem(Product product, USUserDTO user)
    {
        try
        {
            // Delegate the creation of the root document to Documents Manger's client.
            DMDocumentDTO rootDocumentDTO = this.docsClient.createRoot(product, user.getUserCode());
            // Build a root DocSystem from the response to Documents Manger's client, and save it into DB.
            DocSystem rootDocSystem = new DocSystem(
                    DocSystemUtils.getDriveDocSystemName(product),
                    DocSystemUtils.getDriveDocSystemDescription(product),
                    rootDocumentDTO.getActions().getViewUrl(),
                    product,
                    DocumentCategory.GOOGLE_DRIVE,
                    DocumentType.FOLDER,
                    null,
                    true
                    );
            repository.saveAndFlush(rootDocSystem);
            // Build the children DocSystems, attaching them to the root DocSystem, and storing them into DB.
            this.buildAndSaveChildrenFolders(product, rootDocumentDTO, rootDocSystem);
            this.addUserToDefaultDocSystem(product, user, user.getUserCode());
        }
        catch (RuntimeException e)
        {
            LOG.error("[DocSystemServiceServiceImpl] -> [createDefaultDocSystem]: error creating default doc system in product id: [{}]. Error message: [{}]", product.getId(), e.getMessage());
        }
    }

    @Override
    public List<DocSystem> buildAndSaveChildrenFolders(Product product, DMDocumentDTO rootDocumentDTO, DocSystem rootDocSystem)
    {
        List<DocSystem> childrenEntitiesBuilt = new ArrayList<>();
        for (DMDocumentFolderDTO childFolderDocumentDTO : rootDocumentDTO.getFolders())
        {
            DocumentCategory childFolderCategory = DocumentCategory.getValueFromName(childFolderDocumentDTO.getName());
            if (childFolderCategory == null)
            {
                throw new NovaException(DocSystemErrorCode.getUnexpectedError(String.format("[%s] cannot be converted to enum", childFolderDocumentDTO.getName())));
            }
            // Since the name of the folder in Google Drive had its accents stripped, and the original name with the accents should be the human-readable name of its category,
            // we can reverse the stripping of the accents. If we cannot, just use the name of the folder in Google Drive.
            DocumentCategory documentCategory = DocumentCategory.getValueFromName(childFolderDocumentDTO.getName());
            String childFolderDocumentDTOName = documentCategory != null ? documentCategory.getName() : childFolderDocumentDTO.getName();
            DocSystem childFolderDocSystem = new DocSystem(
                    DocSystemUtils.getChildFolderDocSystemName(childFolderDocumentDTOName, product),
                    DocSystemUtils.getChildFolderDocSystemDescription(childFolderDocumentDTOName, product),
                    childFolderDocumentDTO.getUrl(),
                    product,
                    childFolderCategory,
                    DocumentType.FOLDER,
                    rootDocSystem,
                    true
                    );
            repository.saveAndFlush(childFolderDocSystem);
            childrenEntitiesBuilt.add(childFolderDocSystem);
        }
        return childrenEntitiesBuilt;
    }

    /**
     * Adds permissions for the user to access products folder
     *
     * @param product  Product folder belongs to
     * @param user     User requesting the creation.
     * @param userCode user code of the requester
     */
    @Override
    public void addUserToDefaultDocSystem(Product product, USUserDTO user, String userCode)
    {
        try
        {
            this.docsClient.addUser(product, user.getEmail(), userCode);
        }
        catch (RuntimeException e)
        {
            LOG.error("Error adding user to documentsmanager in product id [{}]. Error message: [{}]", product.getId(), e.getMessage());
        }
    }

    /**
     * Removes permission for the user to access the project folder
     *
     * @param product  Product the root folder belongs to
     * @param user     User to remove permissions on the folder
     * @param userCode userCode of the user that requested the user removal.
     */
    @Override
    public void removeUserFromDefaultDocSystem(Product product, USUserDTO user, String userCode)
    {
        try
        {
            this.docsClient.removeUser(product, user.getEmail(), userCode);
        }
        catch (RuntimeException e)
        {
            LOG.error("Error removing user from documentsmanager in product id {}. Error message: [{}]", product.getId(), e.getMessage());
        }
    }

    /**
     * Removes the root folder of the specified Product
     *
     * @param product Product to remove rootFolder for
     */
    @Override
    public void removeDefaultRepository(Product product)
    {

        List<DocSystem> docSystems = repository.findByProduct(product.getId());
        String driveDocSystemName = DocSystemUtils.getDriveDocSystemName(product);
        if (docSystems.stream().anyMatch(doc -> doc.getSystemName().equals(driveDocSystemName)))
        {
            try
            {
                this.docsClient.removeRoot(product);
            }
            catch (RuntimeException e)
            {
                LOG.error("Error deleting docSystem for product with id: [{}]. Error message: [{}] ", product.getId(), e.getMessage());
            }
        }

    }

    @Override
    public Optional<DocSystem> getDocSystemWithIdAndCategoryAndType(Integer docSystemId, DocumentCategory documentCategory, DocumentType documentType)
    {
        return this.repository.findByIdAndCategoryAndType(docSystemId, documentCategory, documentType);
    }

}
