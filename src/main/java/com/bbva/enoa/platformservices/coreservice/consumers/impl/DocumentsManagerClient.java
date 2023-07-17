package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.documentsmanagerapi.client.feign.nova.rest.IRestHandlerDocumentsmanagerapi;
import com.bbva.enoa.apirestgen.documentsmanagerapi.client.feign.nova.rest.IRestListenerDocumentsmanagerapi;
import com.bbva.enoa.apirestgen.documentsmanagerapi.client.feign.nova.rest.impl.RestHandlerDocumentsmanagerapi;
import com.bbva.enoa.apirestgen.documentsmanagerapi.model.*;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDocumentsManagerClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions.DocSystemErrorCode;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Implementation of documentsmanagerclient
 *
 * @author vbazagad
 */
@Slf4j
@Service
public class DocumentsManagerClient implements IDocumentsManagerClient
{

    private static final String PRODUCT_ROOT_TYPE = "PRODUCT";
    private RestHandlerDocumentsmanagerapi handlerImpl;

    /**
     * Constructor for DocumentManagerClient
     *
     * @param handler The Handler to use by this client
     */
    public DocumentsManagerClient(IRestHandlerDocumentsmanagerapi handler)
    {

        handlerImpl = new RestHandlerDocumentsmanagerapi(handler);

    }

    @Override
    public DMDocumentDTO createRoot(Product product, String userCode)
    {

        Integer productId = product.getId();
        DMRootRequestDTO request = new DMRootRequestDTO();
        DMRootKeyDTO rootKey = buildRootKey(productId);
        request.setRootKey(rootKey);
        request.setRootName(product.getName().trim());
        request.setFolders(Arrays.stream(DocumentCategory.values()).filter(documentCategory -> !documentCategory.getIsRootCategory()).map(DocumentCategory::getName).toArray(String[]::new));
        RestListenecrDocumentsManager listener = new RestListenecrDocumentsManager();
        log.debug("[DocumentsManagerClient] -> [createRoot]: creating root for productId: {}", productId);
        handlerImpl.createRoot(listener, request);
        log.debug("[DocumentsManagerClient] -> [createRoot]: successfully created root for productId: {}", productId);
        return listener.getResult(DMDocumentDTO.class);
    }

    @Override
    public void addUser(Product product, String email, String userCode)
    {

        RestListenecrDocumentsManager restListener = new RestListenecrDocumentsManager();
        DMDocUserDTO docUser = new DMDocUserDTO();
        docUser.setUserEmail(email);
        Integer productId = product.getId();
        log.debug("[DocumentsManagerClient] -> [addUser]: adding user {} with email {} to productId: {}", userCode, email, productId);
        this.handlerImpl.createUserPermission(restListener, docUser, product.getId(), PRODUCT_ROOT_TYPE);
        restListener.getResult(Void.class);
        log.debug("[DocumentsManagerClient] -> [addUser]: successfully added user {} with email {} to productId: {}", userCode, email, productId);

    }

    @Override
    public void removeUser(Product product, String email, String userCode)
    {

        RestListenecrDocumentsManager restListener = new RestListenecrDocumentsManager();
        DMDocUserDTO docUser = new DMDocUserDTO();
        docUser.setUserEmail(email);
        Integer productId = product.getId();
        log.debug("[DocumentsManagerClient] -> [removeUser]: removing user {} with email {} from productId: {}", userCode, email, productId);
        this.handlerImpl.deleteUserPermission(restListener, docUser, product.getId(), PRODUCT_ROOT_TYPE);
        restListener.getResult(Void.class);
        log.debug("[DocumentsManagerClient] -> [removeUser]: successfully removed user {} with email {} from productId: {}", userCode, email, productId);

    }

    @Override
    public void removeRoot(Product product)
    {

        RestListenecrDocumentsManager restListener = new RestListenecrDocumentsManager();
        Integer productId = product.getId();
        log.debug("[DocumentsManagerClient] -> [removeRoot]: removing root for productId: {}", productId);
        this.handlerImpl.deleteRoot(restListener, product.getId(), PRODUCT_ROOT_TYPE);
        restListener.getResult(Void.class);
        log.debug("[DocumentsManagerClient] -> [removeRoot]: successfully removed root for productId: {}", productId);

    }

    @Override
    public DMDocumentDTO createDocumentFoldersForProduct(Product product, List<String> folders)
    {
        String className = this.getClass().getSimpleName();
        String listenerMethodName = "addFoldersToRootKey";

        Integer productId = product.getId();
        String uuaa = product.getUuaa();
        DMRootRequestDTO request = new DMRootRequestDTO();
        DMRootKeyDTO rootKey = buildRootKey(productId);
        request.setRootKey(rootKey);
        request.setRootName(product.getName().trim());
        request.setFolders(folders.toArray(String[]::new));
        SingleApiClientResponseWrapper<DMDocumentDTO> response = new SingleApiClientResponseWrapper<>();

        log.info("[{}] -> [{}]: adding [{}] folders to product [{} - {}]", className, listenerMethodName, String.join(", ", folders), productId, uuaa);

        this.handlerImpl.addFoldersToRootKey(new IRestListenerDocumentsmanagerapi()
        {
            @Override
            public void addFoldersToRootKey(DMDocumentDTO outcome)
            {
                log.info("[{}] -> [{}]: successfully added [{} - {}] folders to product [{}]", className, listenerMethodName, String.join(", ", folders), productId, uuaa);
                response.set(outcome);
            }

            @Override
            public void addFoldersToRootKeyErrors(Errors outcome)
            {
                log.error("[{}] -> [{}]: error adding [{}] folders to product [{} - {}]: {}", className, listenerMethodName, String.join(", ", folders), productId, uuaa, outcome.getBodyExceptionMessage());
                throw new NovaException(CommonError.getErrorCallingDocumentsManagerApi(className, outcome.getBodyExceptionMessage() != null ? outcome.getBodyExceptionMessage().toString() : null), outcome);
            }
        }, request);

        return response.get();
    }

    private DMRootKeyDTO buildRootKey(Integer productId)
    {

        DMRootKeyDTO rootKey = new DMRootKeyDTO();
        rootKey.setRootId(productId);
        rootKey.setRootType(PRODUCT_ROOT_TYPE);
        return rootKey;
    }

    private static class RestListenecrDocumentsManager implements IRestListenerDocumentsmanagerapi
    {


        @Override
        public void unlinkRoots()
        {
            //Unnecessary implementation
        }

        private Errors error;

        @Override
        public void uploadDocumentErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void downloadDocumentErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void deleteUserPermissionErrors(Errors outcome)
        {

            error = outcome;
        }

        private Object result;

        /**
         * @return the result
         */
        public <T> T getResult(Class<T> type)
        {

            if (error != null)
            {
                throw new NovaException(DocSystemErrorCode.getUnexpectedError(), error);
            }
            if (result != null)
            {
                if (type.isAssignableFrom(result.getClass()))
                {
                    return type.cast(result);
                }
                else
                {
                    throw new IllegalArgumentException("Specified type is incompatible with result " + result);
                }
            }
            else
            {
                return null;
            }
        }

        @Override
        public void deleteDocument()
        {
            //Unnecessary implementation
        }

        @Override
        public void createRootErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void downloadDocument(byte[] outcome)
        {

            result = outcome.clone();
        }

        @Override
        public void createUserPermissionErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void setFrozen()
        {
            //Unnecessary implementation
        }

        @Override
        public void setFrozenErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void uploadDocument(DMDocumentDTO outcome)
        {

            result = outcome;
        }

        @Override
        public void deleteUserPermission()
        {
            //Unnecessary implementation
        }

        @Override
        public void deleteDocumentErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void createRoot(DMDocumentDTO outcome)
        {

            result = outcome;
        }

        @Override
        public void createUserPermission()
        {
            //Unnecessary implementation
        }


        @Override
        public void listDocumentsErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void deleteRoot()
        {
            //Unnecessary implementation
        }

        @Override
        public void deleteRootErrors(Errors outcome)
        {

            this.error = outcome;
        }

        @Override
        public void listDocuments(DMDocumentPageDTO outcome)
        {

            result = outcome;
        }

        @Override
        public void linkRootsErrors(Errors outcome)
        {

            error = outcome;
        }

        @Override
        public void linkRoots()
        {
            //Unnecessary implementation
        }

        @Override
        public void unlinkRootsErrors(Errors outcome)
        {

            this.error = outcome;
        }

    }


}
