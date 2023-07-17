package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorPortInfo;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorTypePropertyDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.PhysicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Physical connector builder interface
 */
public interface IPhysicalConnectorBuilder
{
    /**
     * Build a new physical connector. Fill all the logical connector with the beginning properties
     *
     * @param newPhysicalConnectorDto physical connector properties from the form
     * @return a physical connector instance
     * @throws NovaException physicalConnectorException
     */
    PhysicalConnector buildNewPhysicalConnector(NewPhysicalConnectorDto newPhysicalConnectorDto) throws NovaException;

    /**
     * Build a connector port list for the physical connectors and save the connector port list into BBDD
     *
     * @param connectorPortInfoDTO connectorPortInfoDTO of the physical connector Dto
     * @param physicalConnector    the physical connector
     * @throws NovaException physicalConnectorException
     */
    void editAndManagePhysicalConnectorPort(ConnectorPortInfo[] connectorPortInfoDTO, PhysicalConnector physicalConnector) throws NovaException;

    /**
     * Builds an array of {@link PhysicalConnectorDto} from the {@link PhysicalConnector}
     * of a product.
     *
     * @param physicalConnectorList {@link PhysicalConnector}
     * @return a PhysicalConnectorDto array
     */
    PhysicalConnectorDto[] buildPhysicalConnectorDtoList(List<PhysicalConnector> physicalConnectorList);

    /**
     * Build a new physical connector Dto from physical connector instance
     *
     * @param physicalConnector the physical connector instance
     * @return a physical connector Dto instandce
     */
    PhysicalConnectorDto buildPhysicalConnectorDto(PhysicalConnector physicalConnector);

    /**
     * Build the connector type property dto list
     *
     * @param connectorTypePropertyList a connector type property list original from BBDD
     * @return a connector type property dto array
     */
    ConnectorTypePropertyDto[] buildConnectorTypePropertyDtoArray(List<ConnectorTypeProperty> connectorTypePropertyList);

    /**
     * Create a new connector type property to the connector type
     * The name of the connector type property must follow the patter: {'CONNECTOR_TYPE_NAME'_'PROPERTY_NAME'}
     *
     * @param connectorTypeName        connector type name
     * @param connectorTypePropertyDto property to add the connector type
     * @return a ConnectorTypeProperty instance
     */
    ConnectorTypeProperty createConnectorTypeProperty(ConnectorTypePropertyDto connectorTypePropertyDto, String connectorTypeName);
}
