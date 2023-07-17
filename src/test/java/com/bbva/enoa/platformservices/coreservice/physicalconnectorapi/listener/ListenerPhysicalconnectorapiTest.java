package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.listener;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.EditPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorService;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListenerPhysicalconnectorapiTest
{
    private NovaMetadata novaMetadata = new NovaMetadata();

    @Mock
    private IPhysicalConnectorService iPhysicalConnectorService;
    @Mock
    private IErrorTaskManager errorTaskManager;
    @InjectMocks
    private ListenerPhysicalconnectorapi listenerPhysicalconnectorapi;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Arrays.asList(new String[]{"CODE"}));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
    }

    @Test
    public void getManagementTypes() throws Exception
    {
        String[] managementTypeStringArray = new String[0];
        when(this.iPhysicalConnectorService.getManagementTypes()).thenReturn(managementTypeStringArray);

        String[] response = this.listenerPhysicalconnectorapi.getManagementTypes(novaMetadata);

        assertEquals(managementTypeStringArray, response);
    }

    @Test
    public void createPhysicalConnector() throws Exception
    {
        NewPhysicalConnectorDto dto = new NewPhysicalConnectorDto();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        when(this.iPhysicalConnectorService.createPhysicalConnector(dto, "CODE")).thenReturn(physicalConnector);

        this.listenerPhysicalconnectorapi.createPhysicalConnector(novaMetadata, dto);

        verify(this.iPhysicalConnectorService, times(1)).createPhysicalConnector(dto, "CODE");
    }

    @Test
    public void deleteConnectorType() throws Exception
    {
        ConnectorType connectorType = new ConnectorType();
        when(this.iPhysicalConnectorService.deleteConnectorType(1, "CODE")).thenReturn(connectorType);

        this.listenerPhysicalconnectorapi.deleteConnectorType(novaMetadata, 1);

        verify(this.iPhysicalConnectorService, times(1)).deleteConnectorType(1, "CODE");
    }

    @Test
    public void getPropertyTypes() throws Exception
    {
        String[] propertyTypeStringArray = new String[0];
        when(this.iPhysicalConnectorService.getPropertyTypes()).thenReturn(propertyTypeStringArray);

        String[] response = this.listenerPhysicalconnectorapi.getPropertyTypes(novaMetadata);

        assertEquals(propertyTypeStringArray, response);
    }

    @Test
    public void deleteConnectorTypeProperty() throws Exception
    {
        ConnectorTypeProperty connectorTypeProperty = new ConnectorTypeProperty();
        when(this.iPhysicalConnectorService.deleteConnectorTypeProperty(2, 1, "CODE")).thenReturn(connectorTypeProperty);

        this.listenerPhysicalconnectorapi.deleteConnectorTypeProperty(novaMetadata, 1, 2);

        verify(this.iPhysicalConnectorService, times(1)).deleteConnectorTypeProperty(2, 1, "CODE");
    }

    @Test
    public void getPhysicalConnector() throws Exception
    {
        PhysicalConnectorDto physicalConnectorDto = new PhysicalConnectorDto();
        when(this.iPhysicalConnectorService.getPhysicalConnector(1)).thenReturn(physicalConnectorDto);

        PhysicalConnectorDto response = this.listenerPhysicalconnectorapi.getPhysicalConnector(novaMetadata, 1);

        assertEquals(physicalConnectorDto, response);
    }

    @Test
    public void deletePhysicalConnector() throws Exception
    {
        when(this.iPhysicalConnectorService.deletePhysicalConnector(1, "CODE")).thenReturn(1);

        Integer response = this.listenerPhysicalconnectorapi.deletePhysicalConnector(novaMetadata, 1);

        assertEquals(1, response.intValue());
    }

    @Test
    public void disassociateConnectors() throws Exception
    {
        this.listenerPhysicalconnectorapi.disassociateConnectors(novaMetadata, 1, 2);
        verify(this.iPhysicalConnectorService, times(1)).disassociateConnectors(1, 2, "CODE");
    }

    @Test
    public void addConnectorTypeProperty() throws Exception
    {
        ConnectorTypePropertyDto dto = new ConnectorTypePropertyDto();
        ConnectorTypeProperty connectorTypeProperty = new ConnectorTypeProperty();
        when(this.iPhysicalConnectorService.addConnectorTypeProperty(dto, 1, "CODE")).thenReturn(connectorTypeProperty);
        this.listenerPhysicalconnectorapi.addConnectorTypeProperty(novaMetadata, dto, 1);
        verify(this.iPhysicalConnectorService, times(1)).addConnectorTypeProperty(dto, 1, "CODE");
    }

    @Test
    public void getConnectorType() throws Exception
    {
        ConnectorTypeDto connectorTypeDto = new ConnectorTypeDto();
        when(this.iPhysicalConnectorService.getConnectorType(1)).thenReturn(connectorTypeDto);
        ConnectorTypeDto response = this.listenerPhysicalconnectorapi.getConnectorType(novaMetadata, 1);
        assertEquals(connectorTypeDto, response);
    }

    @Test
    public void associateConnectors() throws Exception
    {
        this.listenerPhysicalconnectorapi.associateConnectors(novaMetadata, 1, 2, 3);
        verify(this.iPhysicalConnectorService, times(1)).associateConnectors(1, 2, 3, "CODE");
    }

    @Test
    public void getAllPhysicalConnectors() throws Exception
    {
        PhysicalConnectorDto[] physicalConnectorArray = new PhysicalConnectorDto[0];
        when(this.iPhysicalConnectorService.getAllPhysicalConnector("TYPE", "INT")).thenReturn(physicalConnectorArray);
        PhysicalConnectorDto[] response = this.listenerPhysicalconnectorapi.getAllPhysicalConnectors(novaMetadata,"TYPE", "INT");
        assertEquals(physicalConnectorArray, response);
    }

    @Test
    public void getScopeTypes() throws Exception
    {
        String[] scopeTypeStringArray = new String[0];
        when(this.iPhysicalConnectorService.getScopeTypes()).thenReturn(scopeTypeStringArray);
        String[] response = this.listenerPhysicalconnectorapi.getScopeTypes(novaMetadata);
        assertEquals(scopeTypeStringArray, response);
    }

    @Test
    public void getConnectorTypesProperties() throws Exception
    {
        ConnectorTypePropertyDto[] connectorTypePropertyDtoArray = new ConnectorTypePropertyDto[0];
        when(this.iPhysicalConnectorService.getConnectorTypeProperties(1)).thenReturn(connectorTypePropertyDtoArray);
        ConnectorTypePropertyDto[] response = this.listenerPhysicalconnectorapi.getConnectorTypesProperties(novaMetadata,1);
        assertEquals(connectorTypePropertyDtoArray, response);
    }

    @Test
    public void createNewConnectorType() throws Exception
    {
        NewConnectorTypeDto newConnectorTypeDto = new NewConnectorTypeDto();
        ConnectorType connectorType = new ConnectorType();
        when(this.iPhysicalConnectorService.createNewConnectorType(newConnectorTypeDto, "CODE")).thenReturn(connectorType);
        this.listenerPhysicalconnectorapi.createNewConnectorType(novaMetadata, newConnectorTypeDto);
        verify(this.iPhysicalConnectorService, times(1)).createNewConnectorType(newConnectorTypeDto, "CODE");
    }

    @Test
    public void editPhysicalConnector() throws Exception
    {
        EditPhysicalConnectorDto editPhysicalConnectorDto = new EditPhysicalConnectorDto();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        when(this.iPhysicalConnectorService.editPhysicalConnector(editPhysicalConnectorDto, 1, "CODE")).thenReturn(physicalConnector);
        this.listenerPhysicalconnectorapi.editPhysicalConnector(novaMetadata, editPhysicalConnectorDto,  1);
        verify(this.iPhysicalConnectorService, times(1)).editPhysicalConnector(editPhysicalConnectorDto, 1, "CODE");
    }

    @Test
    public void getConnectorTypes() throws Exception
    {
        ConnectorTypeDto[] connectorTypesDtoArray = new ConnectorTypeDto[0];
        when(this.iPhysicalConnectorService.getConnectorTypes()).thenReturn(connectorTypesDtoArray);
        ConnectorTypeDto[] response = this.listenerPhysicalconnectorapi.getConnectorTypes(novaMetadata);
        assertEquals(connectorTypesDtoArray, response);
    }
}
