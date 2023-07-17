package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces;

import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Logical connector validator interface
 */
public interface ILogicalConnectorValidator
{
    /**
     * Checks if a {@link Product} exist in NOVA. If the product exists, return it
     *
     * @param productId {@link Product} ID.
     * @return a product instance
     * @throws NovaException logicalConnectorException
     */
    Product validateAndGetProduct(int productId) throws NovaException;

    /**
     * Checks if a {@link LogicalConnector} exist in the BBDD of the NOVA. If the Logical Connector exists, return it
     *
     * @param logicalConnectorId {@link LogicalConnector} ID.
     * @return a logical connector instance
     * @throws NovaException logicalConnectorException
     */
    LogicalConnector validateAndGetLogicalConnector(int logicalConnectorId) throws NovaException;

    /**
     * Checks if a {@link LogicalConnector} is on CREATED status.
     * Throws exception if not.
     *
     * @param logicalConnector {@link LogicalConnector}.
     * @throws NovaException logicalConnectorException
     */
    void validateLogicalConnectorCreatedStatus(LogicalConnector logicalConnector) throws NovaException;



    /**
     * Validate if the logical connector name does not exists in the same environment for the product
     *
     * @param logicalConnectorNameDTO the new logical connector name to validate
     * @param environment             environment to check if the name exits
     * @param productId               product id to find the logical connector name
     * @return a string with the logical connector name
     * @throws NovaException logicalConnectorException
     */
    String validateLogicalConnectorName(String logicalConnectorNameDTO, String environment, Integer productId) throws NovaException;

    /**
     * Validate if exits a connector type filter by connector type name
     *
     * @param connectorTypeName the connector type name
     * @return a connector type name instance
     * @throws NovaException logicalConnectorException
     */
    ConnectorType validateConnectorType(String connectorTypeName) throws NovaException;

    /**
     * Validate if a user can request a check properties task.
     * A user can only request a check properties to do task if the logical connector status = CREATED.
     * Any case, thrown exception
     *
     * @param logicalConnectorStatus the logical connector status
     */
    void canRequestCheckPropertiesTask(LogicalConnectorStatus logicalConnectorStatus);

    /**
     * Check if a logical connector has some to do task associated in PENDING or PENDING_ERROR status
     *
     * @param logicalConnector the logical connector to check
     * @throws NovaException logicalConnectorException
     */
    void checkPendingTodoTaskByLogicalConnector(LogicalConnector logicalConnector) throws NovaException;

    /**
     * Check if a logical connector has a check logical connector properties to do task associated in PENDING or PENDING_ERROR status
     *
     * @param logicalConnector the logical connector to check
     * @return the to do task id associated
     */
    Integer checkPendingPropertyTask(LogicalConnector logicalConnector);

    /**
     * Check if logical connector is frozen
     * A logical connector is frozen just in PRE environment and when any release, the auto manage is false
     * Auto manage = false, means any user cannot modify a logical connector
     *
     * @param logicalConnector logical connector
     * @return true if logical connector has a release frozen in other case false
     */
    boolean isLogicalConnectorFrozen(LogicalConnector logicalConnector);

    /**
     * Check if a logical connector is frozen
     * @param logicalConnector a logical connector to check
     */
    void checkIfLogicalConnectorIsFrozen(LogicalConnector logicalConnector) throws NovaException;

    /**
     * Validate if exists a DocSystem with the given ID, category and type. If it exists, get it.
     * @param docSystemId       The given ID.
     * @param docSystemCategory The given category.
     * @param docSystemType     The given type.
     * @return The DocSystem.
     * @throws NovaException If the DocSystem doesn't exist.
     */
    DocSystem validateAndGetDocument(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType);

    /**
     * Get a DocSystem with the given ID, category and type.
     * @param docSystemId       The given ID.
     * @param docSystemCategory The given category.
     * @param docSystemType     The given type.
     * @return The DocSystem.
     */
    DocSystem getDocument(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType);
}
