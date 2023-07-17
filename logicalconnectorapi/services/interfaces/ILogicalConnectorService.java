package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces;

import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorUpdateDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.NewLogicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.List;

/**
 * Logical connector service interface
 */
public interface ILogicalConnectorService
{
    /**
     * Create request logical connector properties
     *
     * @param logicalConnectorId logical connector
     * @param ivUser             user that requested
     * @return the to do task id
     * @throws NovaException logical connector exception
     */
    Integer requestProperties(Integer logicalConnectorId, String ivUser) throws NovaException;

    /**
     * Get all the logical connector for a product depending on the filters: Environment and connector type
     *
     * @param productId     product id of the product
     * @param connectorType connector type
     * @param environment   environment to find the logical connector
     * @return a logical connector Dto list
     * @throws NovaException logical connector exception
     */
    LogicalConnectorDto[] getAllFromProduct(Integer productId, String connectorType, String environment) throws NovaException;

    /**
     * Delete a logical connector
     *
     * @param logicalConnectorId logical connector id to delete
     * @param ivUser             user that generate the request
     * @return a logical connector
     * @throws NovaException logical connector exception
     */
    LogicalConnector deleteLogicalConnector(Integer logicalConnectorId, String ivUser) throws NovaException;

    /**
     * Archive a logical connector
     * logicalConnectorId logical connector id to delete
     *
     * @param logicalConnectorId the logical connector id
     * @param ivUser             User code
     * @return a logical connector instance
     * @throws NovaException logicalConnectorException
     */
    LogicalConnector archiveLogicalConnector(Integer logicalConnectorId, String ivUser) throws NovaException;

    /**
     * Restore a logical connector
     *
     * @param logicalConnectorId the logical connector to restore
     * @param ivUser             User code
     * @return a logical connector instance
     * @throws NovaException logicalConnectorException
     */
    LogicalConnector restoreLogicalConnector(Integer logicalConnectorId, String ivUser) throws NovaException;

    /**
     * Get all the connector types in the NOVA platform from BBDD (connector type repository)
     *
     * @return connector type string array
     */
    String[] getConnectorTypes();

    /**
     * This method test the connexion between logical connector and physical connector
     *
     * @param logicalConnectorId the logical connector to check
     * @param ivUser             User code
     * @return message with the result of the connexion
     * @throws NovaException a NovaException
     */
    String testLogicalConnector(Integer logicalConnectorId, String ivUser) throws NovaException;

    /**
     * Gets a logical connector from logical connector id
     *
     * @param logicalConnectorId the logical connector id to find into BBDD
     * @param ivUser             user that generate the request
     * @return a logical connector instance
     * @throws NovaException logicalConnectorException
     */
    LogicalConnectorDto getLogicalConnector(Integer logicalConnectorId, String ivUser) throws NovaException;

    /**
     * Create a new logical connector for a product
     * This method is transaction due to if the to do task fails, must cancel the transaction into BBDD
     *
     * @param newLogicalConnectorDto parameters for creating the new logical connector
     * @param productId              the product id
     * @param ivUser                 user that generate the request
     * @return the to do task id created for creating the logical connector
     * @throws NovaException logicalConnectorException
     */
    Integer createLogicalConnector(NewLogicalConnectorDto newLogicalConnectorDto, Integer productId,
                                   String ivUser) throws NovaException;

    /**
     * Returns whether the logical connector is frozen in PRE
     *
     * @param logicalConnectorId the logical connector id
     * @return true if the logical connector is frozen in PRE, false otherwise
     */
    Boolean isLogicalConnectorFrozen(final Integer logicalConnectorId);

    /**
     * Get the possible values that can take the field Status (see {@link LogicalConnectorStatus}).
     *
     * @return An array of String with the possible values that can take the field Status (see {@link LogicalConnectorStatus}).
     */
    String[] getLogicalConnectorsStatuses();

    /**
     * Update an already existing logical connector, identified by its ID, with a DTO containing the fields to update.
     *
     * @param logicalConnectorUpdateDto The DTO containing the fields to update.
     * @param logicalConnectorId        The ID of the logical connector.
     * @param ivUser                    BBVA user's code.
     */
    void updateLogicalConnector(LogicalConnectorUpdateDto logicalConnectorUpdateDto, Integer logicalConnectorId, String ivUser);

    /**
     * Get the logical connectors that are using a MSA document given by its ID.
     *
     * @param msaDocumentId The ID of the MSA document.
     * @return A List of logical connectors.
     */
    List<LogicalConnector> getLogicalConnectorUsingMsaDocument(Integer msaDocumentId);

    /**
     * Get the logical connectors that are using a ARA document given by its ID.
     *
     * @param araDocumentId The ID of the ARA document.
     * @return A List of logical connectors.
     */
    List<LogicalConnector> getLogicalConnectorUsingAraDocument(Integer araDocumentId);
}
