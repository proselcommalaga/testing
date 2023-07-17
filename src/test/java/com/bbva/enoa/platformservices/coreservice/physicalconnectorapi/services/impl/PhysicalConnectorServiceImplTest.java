package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.EditPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewConnectorTypeDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypePropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.exception.PhysicalConnectorError;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorBuilder;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorRepositoriesService;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorValidator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PhysicalConnectorServiceImplTest
{

    @Mock
    private IProductUsersClient usersClient;
    @InjectMocks
    private PhysicalConnectorServiceImpl service;
    @Mock
    private PhysicalConnectorRepository physicalConnectorRepository;
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @Mock
    private ConnectorTypeRepository connectorTypeRepository;
    @Mock
    private ConnectorTypePropertyRepository connectorTypePropertyRepository;
    @Mock
    private IPhysicalConnectorValidator iPhysicalConnectorValidator;
    @Mock
    private IPhysicalConnectorBuilder iPhysicalConnectorBuilder;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private IPhysicalConnectorRepositoriesService iPhysicalConnectorRepositoriesService;
    @Mock
    private LogicalConnectorPropertyRepository logicalConnectorPropertyRepository;
    @Mock
    private DeploymentChangeRepository deploymentChangeRepository;

    @BeforeEach
    public void setUp() throws Exception
    {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreatePhysicalConnectorUserCodeRequired() throws Exception
    {

        doThrow(new IllegalArgumentException("userCode required")).when(usersClient)
                .checkHasPermission(nullable(String.class), any(String.class), any(Exception.class));
        assertThrows(IllegalArgumentException.class, () ->
                service.createPhysicalConnector(null, null)
        );
    }

    @Test
    public void testCreatePhysicalConnectorUUnauthroized() throws Exception
    {

        doThrow(new NovaException(PhysicalConnectorError.getForbiddenError())).when(usersClient)
                .checkHasPermission(nullable(String.class), any(String.class), any(Exception.class));

        assertThrows(NovaException.class, () ->
                service.createPhysicalConnector(null, null)
        );
    }

    @Test
    public void testCreateNullPhysicalConnector() throws Exception
    {
        doThrow(new NovaException(PhysicalConnectorError.getPhysicalConnectorRequiredError()))
                .when(iPhysicalConnectorValidator).checkNewPhysicalConnectorInput(null);
        assertThrows(NovaException.class, () ->
                service.createPhysicalConnector(null, null)
        );
    }

    @Test
    public void testCreateNotNullPhysicalConnector() throws Exception
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        when(iPhysicalConnectorBuilder.buildNewPhysicalConnector(any(NewPhysicalConnectorDto.class)))
                .thenReturn(new PhysicalConnector());
        when(physicalConnectorRepository.saveAndFlush(any(PhysicalConnector.class)))
                .thenReturn(new PhysicalConnector());
        PhysicalConnector response = service.createPhysicalConnector(newPhysicalConnectorDto, null);
        assertNotNull(response);
    }

    @Test
    public void testAddConnectorTypePropertyEmptyProperty()
    {
        when(iPhysicalConnectorValidator.validateConnectorType(any(Integer.class))).thenReturn(new ConnectorType());
        service.addConnectorTypeProperty(new ConnectorTypePropertyDto(), 0, null);
    }

    @Test
    public void testAddConnectorTypePropertyWithProperties()
    {
        //When
        when(iPhysicalConnectorValidator.validateConnectorType(any(Integer.class))).thenReturn(new ConnectorType());

        ConnectorTypeProperty connectorTypeProperty = new ConnectorTypeProperty();
        connectorTypeProperty.setDescription(RandomStringUtils.randomAlphabetic(6));
        connectorTypeProperty.setManagement(RandomStringUtils.randomAlphabetic(6));
        connectorTypeProperty.setPropertyType(RandomStringUtils.randomAlphabetic(6));
        connectorTypeProperty.setScope(RandomStringUtils.randomAlphabetic(6));
        connectorTypeProperty.setSecurity(RandomUtils.nextBoolean());
        connectorTypeProperty.setCpdName(Constants.V_CPD_NAME);
        connectorTypeProperty.setName(RandomStringUtils.randomAlphabetic(6));
        when(this.iPhysicalConnectorBuilder.createConnectorTypeProperty(any(ConnectorTypePropertyDto.class), any())).
                thenReturn(connectorTypeProperty);

        LogicalConnector logicalConnectorInt = new LogicalConnector();
        logicalConnectorInt.setEnvironment(Environment.INT.getEnvironment());
        LogicalConnector logicalConnectorPro = new LogicalConnector();
        logicalConnectorPro.setEnvironment(Environment.PRO.getEnvironment());
        logicalConnectorPro.setName(RandomStringUtils.randomAlphabetic(6));
        List<LogicalConnector> logicalConnectorList = Arrays.asList(logicalConnectorInt, logicalConnectorPro);
        when( this.logicalConnectorRepository.findByConnectorTypeName(any())).thenReturn(logicalConnectorList);

        //Then
        service.addConnectorTypeProperty(new ConnectorTypePropertyDto(), 0, null);

        // Validate logicalConnectorPropertyRepository.saveAndFlush
        LogicalConnectorProperty logicalConnectorProperty = new LogicalConnectorProperty();
        logicalConnectorProperty.setDescription(connectorTypeProperty.getDescription());
        logicalConnectorProperty.setManagement(connectorTypeProperty.getManagement());
        logicalConnectorProperty.setPropertyType(connectorTypeProperty.getPropertyType());
        logicalConnectorProperty.setScope(connectorTypeProperty.getScope());
        logicalConnectorProperty.setSecurity(connectorTypeProperty.isSecurity());
        logicalConnectorProperty.setDefaultName(MessageFormat.format("{0}.{1}",
                logicalConnectorPro.getName().toUpperCase(), connectorTypeProperty.getName()));
        logicalConnectorProperty.setName(MessageFormat.format("{0}.{1}",
                logicalConnectorPro.getName().toUpperCase(), connectorTypeProperty.getName()));
        verify(this.logicalConnectorPropertyRepository).saveAndFlush(eq(logicalConnectorProperty));
    }

    @Test
    public void testDeleteConnectorTypeProperty()
    {

        when(this.iPhysicalConnectorValidator.validateConnectorType(nullable(Integer.class)))
                .thenReturn(new ConnectorType());
        when(this.iPhysicalConnectorValidator.validateConnectorTypeProperty(nullable(Integer.class))).thenReturn(new ConnectorTypeProperty());
        service.deleteConnectorTypeProperty(null, null, null);

    }

    @Test
    public void testDeleteConnectorType()
    {

        when(this.iPhysicalConnectorValidator.validateConnectorType(nullable(Integer.class)))
                .thenReturn(new ConnectorType());
        service.deleteConnectorType(null, null);
    }

    @Test
    public void testEditPhysicalConnector()
    {

        when(this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(nullable(Integer.class)))
                .thenReturn(new PhysicalConnector());
        service.editPhysicalConnector(new EditPhysicalConnectorDto(), null, null);
    }

    @Test
    public void testDeletePhysicalConnector()
    {

        PhysicalConnector connector = new PhysicalConnector();
        connector.setName("Test connector");
        connector.setEnvironment(Environment.INT.getEnvironment());
        ConnectorType connType = new ConnectorType();
        connType.setName("TEST");
        connector.setConnectorType(connType);
        when(iPhysicalConnectorValidator.validateAndGetPhysicalConnector(nullable(Integer.class)))
                .thenReturn(connector);
        service.deletePhysicalConnector(null, null);
    }

    @Test
    public void testCreateNewConnectorTypey()
    {

        when(iPhysicalConnectorValidator.validateConnectorType(nullable(Integer.class))).thenReturn(new ConnectorType());
        NewConnectorTypeDto newType = new NewConnectorTypeDto();
        newType.setPhysicalConnectorTypeDescription("Description");
        newType.setPhysicalConnectorTypeName("TESTNAME");
        service.createNewConnectorType(newType, null);

    }

    @Test
    public void testDisassociateConnectorsNullConnectorPort()
    {
        when(iPhysicalConnectorValidator.validateAndGetPhysicalConnector(nullable(Integer.class)))
                .thenReturn(new PhysicalConnector());
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setPhysicalConnectorPort(new PhysicalConnectorPort());
        logicalConnector.getPhysicalConnectorPort().setPortName("Test port");
        when(iPhysicalConnectorValidator.validateAndGetLogicalConnector(any(Integer.class)))
                .thenReturn(logicalConnector);
        service.disassociateConnectors(null, 0, null);
    }

    @Test
    public void testAssociateConnectors()
    {

        when(iPhysicalConnectorValidator.validateAndGetPhysicalConnector(nullable(Integer.class)))
                .thenReturn(new PhysicalConnector());
        when(iPhysicalConnectorValidator.validateAndGetLogicalConnector(any(Integer.class)))
                .thenReturn(new LogicalConnector());
        when(iPhysicalConnectorValidator.validateAndGetPhysicalConnectorPort(anyInt()))
                .thenReturn(new PhysicalConnectorPort());
        service.associateConnectors(null, 0, 0, null);
    }

    @Test
    public void getAllPhysicalConnector()
    {
        this.service.getAllPhysicalConnector(null, null);
        verify(this.physicalConnectorRepository, times(1)).findAll();
        this.service.getAllPhysicalConnector(null, "INT");
        verify(this.physicalConnectorRepository, times(1)).findByEnvironment(Environment.INT.getEnvironment());
        this.service.getAllPhysicalConnector("Type", null);
        verify(this.physicalConnectorRepository, times(1)).findByConnectorTypeName("Type");
        this.service.getAllPhysicalConnector("Type", "INT");
        verify(this.physicalConnectorRepository, times(1)).findByEnvironmentAndConnectorTypeName(Environment.INT.getEnvironment(), "Type");
    }

    @Test
    public void getConnectorTypes()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setId(1);
        List<ConnectorType> connectorTypeList = new ArrayList<>();
        connectorTypeList.add(connectorType);
        when(this.connectorTypeRepository.findAll()).thenReturn(connectorTypeList);
        ConnectorTypeDto[] response = this.service.getConnectorTypes();
        assertEquals(1, response[0].getId().intValue());
    }

    @Test
    public void getManagementTypes()
    {
        String[] response = this.service.getManagementTypes();
        assertEquals(4, response.length);
    }

    @Test
    public void getConnectorType()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setId(1);
        when(this.iPhysicalConnectorValidator.validateConnectorType(1)).thenReturn(connectorType);
        ConnectorTypeDto response = this.service.getConnectorType(1);
        assertEquals(1, response.getId().intValue());
    }

    @Test
    public void getScopeTypes()
    {
        String[] response = this.service.getScopeTypes();
        assertEquals(2, response.length);
    }

    @Test
    public void getPropertyTypes()
    {
        String[] response = this.service.getPropertyTypes();
        assertEquals(3, response.length);
    }

    @Test
    public void getPhysicalConnector()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        PhysicalConnectorDto dto = new PhysicalConnectorDto();
        when(this.iPhysicalConnectorValidator.validateAndGetPhysicalConnector(1)).thenReturn(physicalConnector);
        when(this.iPhysicalConnectorBuilder.buildPhysicalConnectorDto(physicalConnector)).thenReturn(dto);
        PhysicalConnectorDto response = this.service.getPhysicalConnector(1);
        assertEquals(dto, response);
    }

    @Test
    public void getConnectorTypeProperties()
    {
        ConnectorTypePropertyDto[] dtos = new ConnectorTypePropertyDto[0];
        ConnectorType connectorType = new ConnectorType();
        connectorType.setId(1);
        when(this.iPhysicalConnectorBuilder.buildConnectorTypePropertyDtoArray(connectorType.getConnectorTypeProperties())).thenReturn(dtos);
        when(this.iPhysicalConnectorValidator.validateConnectorType(1)).thenReturn(connectorType);
        ConnectorTypePropertyDto[] response = this.service.getConnectorTypeProperties(1);
        assertEquals(dtos, response);
    }

}
