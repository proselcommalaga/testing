package com.bbva.enoa.platformservices.coreservice.docsystemsapi.listener;

import com.bbva.enoa.apirestgen.docsystemsapi.model.DocNodeDto;
import com.bbva.enoa.apirestgen.docsystemsapi.model.DocSystemDto;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions.DocSystemErrorCode;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.impl.DocSystemApiServiceImpl;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ListenerDocsystemsapiTest
{

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private DocSystemApiServiceImpl docSystemRepoApiService;

    @InjectMocks
    private ListenerDocsystemsapi listenerDocsystemsapi;

    private NovaMetadata novaMetadata = new NovaMetadata();

    @BeforeEach
    public void setUp() throws Exception
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Arrays.asList(new String[]{"CODE"}));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deleteDocSystem() throws Errors
    {
        this.listenerDocsystemsapi.deleteDocSystem(this.novaMetadata, 1);
        verify(this.docSystemRepoApiService, times(1)).deleteDocSystem("CODE", 1);
    }

    @Test
    public void deleteDocSystemError() throws Errors
    {
        doThrow(new NovaException(DocSystemErrorCode.getUnexpectedError())).when(this.docSystemRepoApiService).deleteDocSystem("CODE", 1);

        // In case we catch a runtime exception, we throw an Errors exception
        Assertions.assertThrows(Errors.class, () -> {
            try
            {
                this.listenerDocsystemsapi.deleteDocSystem(this.novaMetadata, 1);
            }
            catch (RuntimeException e)
            {
                throw new Errors();
            }
        });


    }

    @Test
    public void getProductDocSystems() throws Errors
    {
        DocNodeDto[] docNodeDtosArray = new DocNodeDto[0];
        when(this.docSystemRepoApiService.getProductDocSystems(1)).thenReturn(docNodeDtosArray);

        DocSystemDto[] response = this.listenerDocsystemsapi.getProductDocSystems(this.novaMetadata, 1);
        Assertions.assertArrayEquals(docNodeDtosArray, response);
    }

    @Test
    public void getProductDocSystemsError()
    {
        // Throw error cause somthing wrong has done in the service
        doThrow(new NovaException(DocSystemErrorCode.getUnexpectedError())).when(this.docSystemRepoApiService).getProductDocSystems(1);

        // In case we catch a runtime exception, we throw an Errors exception
        Assertions.assertThrows(Errors.class, () -> {
            try
            {
                this.listenerDocsystemsapi.getProductDocSystems(this.novaMetadata, 1);
            }
            catch (RuntimeException e)
            {
                throw new Errors();
            }
        });
    }

    @Test
    public void createDocSystem() throws Errors
    {
        DocSystemDto docSystem = new DocSystemDto();

        this.listenerDocsystemsapi.createDocSystem(this.novaMetadata, docSystem, 1);
        verify(this.docSystemRepoApiService, times(1)).createDocSystem(docSystem, "CODE", 1);
    }

    @Test
    public void createDocSystemError() throws Errors
    {
        DocSystemDto docSystem = new DocSystemDto();
        doThrow(new NovaException(DocSystemErrorCode.getUnexpectedError())).when(this.docSystemRepoApiService).createDocSystem(docSystem, "CODE", 1);
        // In case we catch a runtime exception, we throw an Errors exception
        Assertions.assertThrows(Errors.class, () -> {
            try
            {
                this.listenerDocsystemsapi.createDocSystem(this.novaMetadata, docSystem, 1);
            }
            catch (RuntimeException e)
            {
                throw new Errors();
            }
        });

    }

    @Test
    public void getDocSystem() throws Errors
    {
        Integer docSystemId = 1;
        this.listenerDocsystemsapi.getDocSystem(this.novaMetadata, docSystemId);
        verify(this.docSystemRepoApiService, times(1)).getDocSystem(docSystemId);
    }

    @Test
    public void updateDocSystem() throws Errors
    {
        Integer docSystemId = 1;
        DocSystemDto docSystemUpdated = new DocSystemDto();
        String ivUser = MetadataUtils.getIvUser(this.novaMetadata);
        this.listenerDocsystemsapi.updateDocSystem(this.novaMetadata, docSystemUpdated, docSystemId);
        verify(this.docSystemRepoApiService, times(1)).updateDocSystem(docSystemUpdated, docSystemId, ivUser);
    }

    @Test
    public void createDocSystemsHierarchy() throws Errors
    {
        this.listenerDocsystemsapi.createDocSystemsHierarchy(this.novaMetadata);
        String ivUser = MetadataUtils.getIvUser(this.novaMetadata);
        verify(this.docSystemRepoApiService, times(1)).createDocSystemsHierarchy(ivUser);
    }

    @Test
    public void createDocSystemProductsFolder() throws Errors
    {
        this.listenerDocsystemsapi.createDocSystemProductsFolder(this.novaMetadata, DocumentCategory.PERFORMANCE_REPORTS.getName());
        verify(this.docSystemRepoApiService, times(1)).createDocSystemProductsFolder(DocumentCategory.PERFORMANCE_REPORTS.getName());
    }

}
