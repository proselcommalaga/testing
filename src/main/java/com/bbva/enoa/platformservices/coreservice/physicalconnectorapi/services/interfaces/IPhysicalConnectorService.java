package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.*;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Physical connector service interface
 */
public interface IPhysicalConnectorService
{
    /**
     * Get all the physical connector for a product depending on the filters: Environment and connector type
     *
     * @param connectorType connector type
     * @param environment   environment to find the physical connector
     * @return a physical connector DTO list
     */
    PhysicalConnectorDto[] getAllPhysicalConnector(String connectorType, String environment);

    /**
     * Get all the connector types in the NOVA platform from BBDD (connector type repository)
     *
     * @return connector type Dto instance  array
     */
    ConnectorTypeDto[] getConnectorTypes();

    /**
     * Get all the managements type for the connector type properties
     *
     * @return management type string array
     */
    String[] getManagementTypes();

    /**
     * Get a connector type filter by id
     *
     * @param connectorTypeId the connector property id
     * @return a connector type dto instance
     * @throws NovaException physicalConnectorException
     */
    ConnectorTypeDto getConnectorType(Integer connectorTypeId) throws NovaException;

    /**
     * Get all the scope type for the connector type properties
     *
     * @return scope type string array
     */
    String[] getScopeTypes();

    /**
     * Get all the property type for the connector type properties
     *
     * @return property type string array
     */
    String[] getPropertyTypes();

    /**
     * Gets a physical connector from physical connector id
     *
     * @param physicalConnectorId the physical connector id to find into BBDD
     * @return a physical connector DTO instance
     * @throws NovaException physicalConnectorException
     */
    PhysicalConnectorDto getPhysicalConnector(Integer physicalConnectorId) throws NovaException;

    /**
     * Create a new physical connector for a product
     *
     * @param newPhysicalConnectorDto
     *            parameters for creating the new physical connector
     * @param ivUser
     *            User that wants to create the connector
     * @return a physical connector instance
     * @throws NovaException
     *             physicalConnectorException
     */
    PhysicalConnector createPhysicalConnector(NewPhysicalConnectorDto newPhysicalConnectorDto, String ivUser)
            throws NovaException;

    /**
     * Add a new connectorTypePropertyDto to the connector type
     *
     * @param connectorTypePropertyDto a connector type property dto
     * @param connectorTypeId          a connector type id
     * @param ivUser                   user requester
     * @return a connector type property
     * @throws NovaException physicalConnectorException
     */
    ConnectorTypeProperty addConnectorTypeProperty(ConnectorTypePropertyDto connectorTypePropertyDto, Integer connectorTypeId, String ivUser) throws NovaException;

    /**
     * Get a connector type properties DTO array
     *
     * @param connectorTypeId the connector type to obtain the properties
     * @return a connector type properties array dto
     * @throws NovaException physicalConnectorException
     */
    ConnectorTypePropertyDto[] getConnectorTypeProperties(Integer connectorTypeId) throws NovaException;

    /**
     * Delete the connector type property
     *
     * @param connectorTypePropertyId connector type property id to remove
     * @param connectorTypeId         connector type id to remove from
     * @param ivUser                  user requester
     * @return the connector type property removed instance
     * @throws NovaException physicalConnectorException
     */
    ConnectorTypeProperty deleteConnectorTypeProperty(Integer connectorTypePropertyId, Integer connectorTypeId, String ivUser) throws NovaException;

    /**
     * Delete a connector type
     *
     * @param connectorTypeId
     *            the connector type id to remove
     * @param ivUser
     *            the user
     * @return the connector type instance removed
     * @throws NovaException
     *             physicalConnectorException
     */
    ConnectorType deleteConnectorType(Integer connectorTypeId, String ivUser) throws NovaException;

    /**
     * Edit some values of the physical connector
     *
     * @param editPhysicalConnectorDto
     *            Physical connector editable values
     * @param physicalConnectorId
     *            Id of the physical connector
     * @param ivUser
     *            User that made the connector edit.
     * @return a physical connector instance
     * @throws NovaException
     *             physicalConnectorException
     */
    PhysicalConnector editPhysicalConnector(EditPhysicalConnectorDto editPhysicalConnectorDto,
            Integer physicalConnectorId, String ivUser) throws NovaException;

    /**
     * Delete a physical connector
     *
     * @param physicalConnectorId the physical connector id
     * @param ivUser              user that request the deletion of the physical connector
     * @return the to do task id created
     * @throws NovaException physicalConnectorException
     */
    Integer deletePhysicalConnector(Integer physicalConnectorId, String ivUser) throws NovaException;

    /**
     * Create a new connector type
     *
     * @param newConnectorTypeDto
     *            New physical connector type
     * @param ivUser
     *            User that requests this action
     * @return the new connector type instance
     * @throws NovaException
     *             physicalConnectorException
     */
    ConnectorType createNewConnectorType(NewConnectorTypeDto newConnectorTypeDto, String ivUser)
            throws NovaException;

    /**
     * Try to disassociate a logical connector from the physical connector
     *
     * @param physicalConnectorId
     *            Id of the physical connector
     * @param logicalConnectorId
     *            Id of the logical connector
     * @param ivUser
     *            User that requests this action
     * @throws NovaException
     *             physicalConnectorException
     */
    void disassociateConnectors(Integer physicalConnectorId, Integer logicalConnectorId, String ivUser)
            throws NovaException;

    /**
     * Try to associate a logical connector to the physical connector
     *
     * @param physicalConnectorId
     *            Id of the physical connector
     * @param logicalConnectorId
     *            Id of the logical connector
     * @param physicalConnectorPortId
     *            Id of the physical connector port
     * @param ivUser
     *            User that requests this action
     * @throws NovaException
     *             physicalConnectorException
     */
    void associateConnectors(Integer physicalConnectorId, Integer logicalConnectorId, Integer physicalConnectorPortId,
            String ivUser) throws NovaException;
}
