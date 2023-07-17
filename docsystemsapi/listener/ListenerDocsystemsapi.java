package com.bbva.enoa.platformservices.coreservice.docsystemsapi.listener;

import com.bbva.enoa.apirestgen.docsystemsapi.model.DocNodeDto;
import com.bbva.enoa.apirestgen.docsystemsapi.model.DocSystemDto;
import com.bbva.enoa.apirestgen.docsystemsapi.server.spring.nova.rest.IRestListenerDocsystemsapi;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.DocSystemApiService;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.Constants.DocSystemsErrors;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ListenerDocsystemsapi implements IRestListenerDocsystemsapi
{
    private DocSystemApiService docSystemApiService;

    /**
     * @param docSystemApiService Docs system API service
     */
    @Autowired
    public ListenerDocsystemsapi(DocSystemApiService docSystemApiService)
    {
        this.docSystemApiService = docSystemApiService;
    }


    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE)
    public void deleteDocSystem(final NovaMetadata novaMetadata, final Integer docSystemId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.docSystemApiService.deleteDocSystem(ivUser, docSystemId);
    }

    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public DocNodeDto getDocSystem(NovaMetadata novaMetadata, Integer docSystemId) throws Errors
    {
        return this.docSystemApiService.getDocSystem(docSystemId);
    }

    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE)
    public void updateDocSystem(NovaMetadata novaMetadata, DocSystemDto docSystemUpdated, Integer docSystemId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.docSystemApiService.updateDocSystem(docSystemUpdated, docSystemId, ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE)
    public void createDocSystemsHierarchy(NovaMetadata novaMetadata) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.docSystemApiService.createDocSystemsHierarchy(ivUser);
    }

    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public DocNodeDto[] getProductDocSystems(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        try
        {
            return this.docSystemApiService.getProductDocSystems(productId);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE)
    public void createDocSystem(final NovaMetadata novaMetadata, final DocSystemDto docSystemToAdd, final Integer productId) throws Errors
    {
        try
        {
            String ivUser = MetadataUtils.getIvUser(novaMetadata);
            this.docSystemApiService.createDocSystem(docSystemToAdd, ivUser, productId);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = Constants.DOC_SYSTEMS_API_NAME, runtimeExceptionErrorCode = DocSystemsErrors.UNEXPECTED_ERROR_CODE)
    public void createDocSystemProductsFolder(final NovaMetadata novaMetadata, final String folderCategory) throws Errors
    {
        this.docSystemApiService.createDocSystemProductsFolder(folderCategory);

    }
}
