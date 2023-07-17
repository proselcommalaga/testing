package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorPortInfo;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.PhysicalConnectorStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypePropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorPortRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PhysicalConnectorBuilderImplTest
{
    @Mock
    private IPhysicalConnectorValidator iPhysicalConnectorValidator;
    @Mock
    private ConnectorTypePropertyRepository connectorTypePropertyRepository;
    @Mock
    private PhysicalConnectorPortRepository physicalConnectorPortRepository;
    @Mock
    private PhysicalConnectorRepository physicalConnectorRepository;
    @InjectMocks
    private PhysicalConnectorBuilderImpl physicalConnectorBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.physicalConnectorBuilder, "connectorMonitoringPort", "8080");
        ReflectionTestUtils.setField(this.physicalConnectorBuilder, "connectorMonitoringUrl", "url");
    }

    @Test
    public void buildNewPhysicalConnector()
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        newPhysicalConnectorDto.setPhysicalConnectorName("Name");
        newPhysicalConnectorDto.setEnvironment("INT");
        when(this.iPhysicalConnectorValidator.validatePhysicalConnectorName("Name", "INT")).thenReturn("NameINT");
        PhysicalConnector response = this.physicalConnectorBuilder.buildNewPhysicalConnector(newPhysicalConnectorDto);
        assertEquals(Environment.INT.getEnvironment(), response.getEnvironment());
    }

    @Test
    public void editAndManagePhysicalConnectorPort()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        ConnectorPortInfo info1 = new ConnectorPortInfo();
        info1.setAction("CREATE");
        info1.setInputPort(1);
        info1.setOutput("1:1");
        ConnectorPortInfo info2 = new ConnectorPortInfo();
        info2.setAction("DELETE");
        info2.setInputPort(2);
        info2.setOutput("2:2");
        ConnectorPortInfo[] connectorPortInfoDTO = new ConnectorPortInfo[]{info1, info2};
        this.physicalConnectorBuilder.editAndManagePhysicalConnectorPort(connectorPortInfoDTO, physicalConnector);
    }

    @Test
    public void buildPhysicalConnectorDtoList()
    {
        ConnectorType connectorType = new ConnectorType();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setId(1);
        physicalConnector.setEnvironment(Environment.INT.getEnvironment());
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setConnectorType(connectorType);
        List<PhysicalConnector> physicalConnectorList = new ArrayList<>();
        physicalConnectorList.add(physicalConnector);
        PhysicalConnectorDto[] response = this.physicalConnectorBuilder.buildPhysicalConnectorDtoList(physicalConnectorList);
        assertEquals(1, response[0].getId().intValue());
    }

    @Test
    public void buildConnectorTypePropertyDtoArray()
    {
        ConnectorTypeProperty property = new ConnectorTypeProperty();
        property.setId(1);
        List<ConnectorTypeProperty> connectorTypePropertyList = new ArrayList<>();
        connectorTypePropertyList.add(property);
        ConnectorTypePropertyDto[] response = this.physicalConnectorBuilder.buildConnectorTypePropertyDtoArray(connectorTypePropertyList);
        assertEquals(1, response[0].getId().intValue());
    }

    @Test
    public void createConnectorTypeProperty()
    {
        ConnectorTypePropertyDto dto = new ConnectorTypePropertyDto();
        dto.setPropertyName("Property");
        dto.setPropertySecurity(false);
        ConnectorTypeProperty response = this.physicalConnectorBuilder.createConnectorTypeProperty(dto, "Type");
        assertEquals("TYPE.Property", response.getName());
    }
}