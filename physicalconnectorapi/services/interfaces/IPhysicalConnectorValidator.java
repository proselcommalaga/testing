package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorPortInfo;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.*;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Physical connector validator interface
 */
public interface IPhysicalConnectorValidator
{
    /**
     * Checks if a {@link PhysicalConnector} exist in the BBDD of the NOVA. If the Physical Connector exists, return it
     *
     * @param physicalConnectorId {@link PhysicalConnector} ID.
     * @return a physical connector instance
     * @throws NovaException physicalConnectorException
     */
    PhysicalConnector validateAndGetPhysicalConnector(Integer physicalConnectorId) throws NovaException;

    /**
     * Checks if a {@link ConnectorTypeProperty} name exist in the BBDD of the NOVA.
     *
     * @param connectorType             the connector type to validate
     * @param connectorTypePropertyName the property name to validate
     * @throws NovaException physicalConnectorException
     */
    void validateConnectorTypePropertyName(ConnectorType connectorType, String connectorTypePropertyName) throws NovaException;

    /**
     * Checks if a {@link ConnectorTypeProperty} exist in the BBDD of the NOVA.
     *
     * @param connectorTypePropertyId {@link ConnectorTypeProperty} ID.
     * @return a connector type property instance
     * @throws NovaException physicalConnectorException
     */
    ConnectorTypeProperty validateConnectorTypeProperty(Integer connectorTypePropertyId) throws NovaException;

    /**
     * Validate if exits a connector type filter by connector type name
     *
     * @param connectorTypeName the connector type name
     * @return a connector type name instance
     * @throws NovaException physicalConnectorException
     */
    ConnectorType validateConnectorType(String connectorTypeName) throws NovaException;

    /**
     * Validate if exits a connector type filter by connector type id
     *
     * @param connectorTypeId the connector type id
     * @return a connector type name instance
     * @throws NovaException physicalConnectorException
     */
    ConnectorType validateConnectorType(Integer connectorTypeId) throws NovaException;

    /**
     * Validate if the physical connector name does not exists in the same environment
     *
     * @param physicalConnectorNameDTO the new physical connector name to validate
     * @param environment              environment to check if the name exits
     * @return a string that represent the physical connector name
     * @throws NovaException physicalConnectorException
     */
    String validatePhysicalConnectorName(String physicalConnectorNameDTO, String environment) throws NovaException;

    /**
     * Validate if port range. The port must be between 0 - 65535
     *
     * @param port the port inserted
     * @throws NovaException physicalConnectorException
     */
    void validatePortRange(String port) throws NovaException;

    /**
     * Check if there is some logical connector or physical connector created of same kind of the connector type name to remove
     * If exist some of then, throws exception
     *
     * @param connectorTypeName connector type to check if it removable
     * @throws NovaException physicalConnectorException
     */
    void isConnectorTypeRemovable(String connectorTypeName) throws NovaException;

    /**
     * Validate if a logical connector is compatible to associate with a physical connector
     *
     * @param logicalConnector  the  logical connector
     * @param physicalConnector the physical connector
     * @param physicalConnectorPort the physical connector port to associate
     * @throws NovaException physicalConnectorException
     */
    void validateIfConnectorsCanBeAssociated(LogicalConnector logicalConnector, PhysicalConnector physicalConnector, PhysicalConnectorPort physicalConnectorPort) throws NovaException;

    /**
     * Validate if a logical connector is compatible to associate with a physical connector
     *
     * @param logicalConnector  the  logical connector
     * @param physicalConnector the physical connector
     * @throws NovaException physicalConnectorException
     */
    void validateIfConnectorsCanBeDisassociated(LogicalConnector logicalConnector, PhysicalConnector physicalConnector) throws NovaException;

    /**
     * Checks if a {@link LogicalConnector} exist in the BBDD of the NOVA. If the Logical Connector exists, return it
     *
     * @param logicalConnectorId {@link LogicalConnector} ID.
     * @return a logical connector instance
     * @throws NovaException physicalConnectorException
     */
    LogicalConnector validateAndGetLogicalConnector(int logicalConnectorId) throws NovaException;

    /**
     * Checks if a {@link PhysicalConnectorPort} exist in the BBDD of the NOVA. If the Physical connector port exists, return it
     *
     * @param physicalConnectorPortId {@link LogicalConnector} ID.
     * @return a logical connector instance
     * @throws NovaException physicalConnectorException
     */
    PhysicalConnectorPort validateAndGetPhysicalConnectorPort(int physicalConnectorPortId) throws NovaException;

    /**
     * Validate if a connector type exist into the BBDD
     *
     * @param connectorTypeName connector type name
     * @throws NovaException physicalConnectorException
     */
    void validateNewConnectorType(String connectorTypeName) throws NovaException;

    /**
     * Validate if a physical connector can be deleted. A physical connector can be deleted if does not have any logical connector associated.
     * Any case, throws exception
     *
     * @param physicalConnector the physical connector to check
     * @throws NovaException physicalConnectorException
     */
    void validatePhysicalConnectorCanBeDeleted(PhysicalConnector physicalConnector) throws NovaException;

    /**
     * Check if a physical connector has delete physical connector to do task type associated in PENDING or PENDING_ERROR status
     *
     * @param physicalConnector the logical connector to check
     * @return a to do task id integer
     */
    Integer checkPendingDeletePhysicalConnector(PhysicalConnector physicalConnector);

    /**
     * Validate if a input port is not used by the physical connector
     *
     * @param physicalConnector the physical connector
     * @param connectorPortInfo the physical connector port provided to check
     * @throws NovaException NovaException
     */
    void validateNewPhysicalConnectorPort(PhysicalConnector physicalConnector, ConnectorPortInfo connectorPortInfo) throws NovaException;

    /**
     * Check if a logical connector has been created before. This means, check if this logical connector has some to do task type = CREATION_LOGICAL_CONNECTOR in DONE status
     *
     * @param logicalConnector the logical connector to check
     * @return true if this logical connector has been created and has a creation logical connector to do task associated in DONE status or false any case
     */
    boolean checkCreationLogicalConnectorTodoTask(final LogicalConnector logicalConnector);

    /**
     * Check that the newPhysicalConnectorDto contains data, to protect against nullPointers
     * @param newPhysicalConnectorDto newPhysicalConnectorDto
     * @throws NovaException physicalConnectorException
     */
    void checkNewPhysicalConnectorInput(final NewPhysicalConnectorDto newPhysicalConnectorDto);
}
