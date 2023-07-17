package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.listener;

import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.NewLogicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorService;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListenerLogicalconnectorapiTest
{
    private NovaMetadata novaMetadata = new NovaMetadata();
    @Mock
    private ILogicalConnectorService iLogicalConnectorService;
    @Mock
    private IErrorTaskManager errorTaskManager;
    @InjectMocks
    private ListenerLogicalconnectorapi listenerLogicalconnectorapi;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Arrays.asList(new String[]{"CODE"}));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
    }

    @Test
    public void testLogicalConnector() throws Exception
    {
        String messageTest = "test";
        when(this.iLogicalConnectorService.testLogicalConnector(1, "CODE")).thenReturn(messageTest);

        this.listenerLogicalconnectorapi.testLogicalConnector(novaMetadata, 1);

        verify(this.iLogicalConnectorService, times(1)).testLogicalConnector(1, "CODE");
    }

    @Test
    public void getLogicalConnector() throws Exception
    {
        LogicalConnectorDto logicalConnectorDto = new LogicalConnectorDto();
        when(this.iLogicalConnectorService.getLogicalConnector(1, "CODE")).thenReturn(logicalConnectorDto);

        this.listenerLogicalconnectorapi.getLogicalConnector(novaMetadata, 1);

        verify(this.iLogicalConnectorService, times(1)).getLogicalConnector(1, "CODE");
    }

    @Test
    public void archiveLogicalConnector() throws Exception
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        when(this.iLogicalConnectorService.archiveLogicalConnector(1, "CODE")).thenReturn(logicalConnector);

        this.listenerLogicalconnectorapi.archiveLogicalConnector(novaMetadata, 1);

        verify(this.iLogicalConnectorService, times(1)).archiveLogicalConnector(1, "CODE");
    }

    @Test
    public void deleteLogicalConnector() throws Exception
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        when(this.iLogicalConnectorService.deleteLogicalConnector(1, "CODE")).thenReturn(logicalConnector);

        this.listenerLogicalconnectorapi.deleteLogicalConnector(novaMetadata, 1);

        verify(this.iLogicalConnectorService, times(1)).deleteLogicalConnector(1, "CODE");
    }

    @Test
    public void getAllFromProduct() throws Exception
    {
        LogicalConnectorDto[] logicalConnectorArray = new LogicalConnectorDto[0];
        when(this.iLogicalConnectorService.getAllFromProduct(1, "TYPE", "INT")).thenReturn(logicalConnectorArray);

        this.listenerLogicalconnectorapi.getAllFromProduct( novaMetadata,1, "TYPE", "INT");

        verify(this.iLogicalConnectorService, times(1)).getAllFromProduct(1, "TYPE", "INT");
    }

    @Test
    public void getAllFromProductError2() throws Exception
    {
        doThrow(new RuntimeException()).when(this.iLogicalConnectorService).getAllFromProduct(1, "TYPE", "INT");
        assertThrows(LogAndTraceException.class, () ->
            this.listenerLogicalconnectorapi.getAllFromProduct(novaMetadata, 1, "TYPE", "INT")
        );
    }

    @Test
    public void requestProperties() throws Exception
    {
        when(this.iLogicalConnectorService.requestProperties(1, "CODE")).thenReturn(1);

        this.listenerLogicalconnectorapi.requestProperties(novaMetadata, 1);

        verify(this.iLogicalConnectorService, times(1)).requestProperties(1, "CODE");
    }

    @Test
    public void restoreLogicalConnector() throws Exception
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        when(this.iLogicalConnectorService.restoreLogicalConnector(1, "CODE")).thenReturn(logicalConnector);

        this.listenerLogicalconnectorapi.restoreLogicalConnector(novaMetadata, 1);

        verify(this.iLogicalConnectorService, times(1)).restoreLogicalConnector(1, "CODE");
    }

    @Test
    public void createLogicalConnector() throws Exception
    {
        NewLogicalConnectorDto dto = new NewLogicalConnectorDto();
        when(this.iLogicalConnectorService.createLogicalConnector(dto, 1, "CODE")).thenReturn(1);
        this.listenerLogicalconnectorapi.createLogicalConnector(novaMetadata, dto, 1);
        verify(this.iLogicalConnectorService, times(1)).createLogicalConnector(dto, 1, "CODE");
    }

    @Test
    public void createLogicalConnectorError2() throws Exception
    {
        NewLogicalConnectorDto dto = new NewLogicalConnectorDto();
        doThrow(new RuntimeException()).when(this.iLogicalConnectorService).createLogicalConnector(dto, 1, "CODE");
        assertThrows(LogAndTraceException.class, () ->
            this.listenerLogicalconnectorapi.createLogicalConnector(novaMetadata, dto, 1)
        );
    }

    @Test
    public void getConnectorTypes() throws Exception
    {
        when(this.iLogicalConnectorService.getConnectorTypes()).thenReturn(new String[0]);
        this.listenerLogicalconnectorapi.getConnectorTypes(novaMetadata);
        verify(this.iLogicalConnectorService, times(1)).getConnectorTypes();
    }

    @Test
    public void getLogicalConnectorsStatuses() throws Errors
    {
        when(this.iLogicalConnectorService.getLogicalConnectorsStatuses()).thenReturn(new String[0]);
        this.listenerLogicalconnectorapi.getLogicalConnectorsStatuses(novaMetadata);
        verify(this.iLogicalConnectorService, times(1)).getLogicalConnectorsStatuses();
    }

    @Test
    public void isLogicalConnectorFrozen() throws Errors
    {
        //WHEN
        int id = RandomUtils.nextInt(1, 10);
        Boolean inputBoolean = RandomUtils.nextBoolean();
        when(this.iLogicalConnectorService.isLogicalConnectorFrozen(id)).thenReturn(inputBoolean);

        //THEN
        Boolean result = this.listenerLogicalconnectorapi.isLogicalConnectorFrozen(novaMetadata, id);

        //VERIFY
        verify(this.iLogicalConnectorService, times(1)).isLogicalConnectorFrozen(id);
        assertEquals(inputBoolean, result);

    }
}
