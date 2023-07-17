package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository of the Logical connector.
 */
public interface LogicalConnectorRepository extends JpaRepository<LogicalConnector, Integer>
{
    /**
     * Gets all logical connector of a product.
     *
     * @param productId {@link Product} ID
     * @return List of Filesystem.
     */
    List<LogicalConnector> findByProductId(int productId);

    /**
     * Gets all logical connector of a product of the provided environment.
     *
     * @param productId   {@link Product} ID
     * @param environment {@link Environment}
     * @return List of Logical connector.
     */
    List<LogicalConnector> findByProductIdAndEnvironment(int productId, String environment);

    /**
     * Gets all logical connector of a product of the provided connector type.
     *
     * @param productId         {@link Product} ID
     * @param connectorTypeName connector type name of the logical connector
     * @return List of Logical connector filter by connector type
     */
    List<LogicalConnector> findByProductIdAndConnectorTypeName(int productId, String connectorTypeName);

    /**
     * Gets all logical connector filter by connector type
     *
     * @param connectorTypeName connector type name of the logical connector
     * @return List of Logical connector filter by connector type
     */
    List<LogicalConnector> findByConnectorTypeName(String connectorTypeName);

    /**
     * Gets all logical connector of a product of the provided environment of the one connector type.
     *
     * @param productId         {@link Product} ID
     * @param environment       {@link Environment}
     * @param connectorTypeName connector type name of the logical connector
     * @return List of Logical connector.
     */
    List<LogicalConnector> findByProductIdAndEnvironmentAndConnectorTypeName(int productId, String environment, String connectorTypeName);


    /**
     * Gets the logical connector of a product of the provided environment of the one connector type and name
     *
     * @param productId         {@link Product} ID
     * @param environment       {@link Environment}
     * @param connectorTypeName connector type name of the logical connector
     * @param name              Name of the {@link LogicalConnector}
     * @return List of Logical connector.
     */
    LogicalConnector findByProductIdAndEnvironmentAndConnectorTypeNameAndName(int productId, String environment, String connectorTypeName, String name);


    /**
     * Gets the only {@link LogicalConnector} of a {@link Product}
     * of the same name on a given {@link Environment}.
     *
     * @param productId   {@link Product} ID
     * @param environment {@link Environment}
     * @param name        Name of the {@link LogicalConnector}
     * @return LogicalConnector
     */
    LogicalConnector findByProductIdAndEnvironmentAndName(
            int productId,
            String environment,
            String name);

    /**
     * Find Logical Connector by Logical Connector Status.
     *
     * @param logicalConnectorStatus The given Logical Connector Status.
     * @return A List of Logical Connector.
     */
    List<LogicalConnector> findByLogicalConnectorStatus(LogicalConnectorStatus logicalConnectorStatus);


    /**
     * Find Logical Connector by UUAA and Logical Connector Status.
     *
     * @param productId                 The given Product ID.
     * @param logicalConnectorStatus    The given Logical Connector Status.
     * @return A List of Logical Connector.
     */
    List<LogicalConnector> findByProductIdAndLogicalConnectorStatus(int productId, LogicalConnectorStatus logicalConnectorStatus);

    /**
     * Find Logical Connector by Environment.
     *
     * @param environment The given Environment.
     * @return A List of Logical Connector.
     */
    List<LogicalConnector> findByEnvironment(String environment);

    /**
     * Find Logical Connector by Environment and Logical Connector Status.
     *
     * @param environment               The given Environment.
     * @param logicalConnectorStatus    The given Logical Connector Status.
     * @return A List of Logical Connector.
     */
    List<LogicalConnector> findByEnvironmentAndLogicalConnectorStatus(String environment, LogicalConnectorStatus logicalConnectorStatus);

    /**
     * Find Logical Connector by Environment and UUAA.
     *
     * @param environment               The given Environment.
     * @param productId                 The given Product ID.
     * @param logicalConnectorStatus    The given Logical Connector Status.
     * @return A List of Logical Connector.
     */
    List<LogicalConnector> findByEnvironmentAndProductIdAndLogicalConnectorStatus(String environment, int productId, LogicalConnectorStatus logicalConnectorStatus);

    /**
     * Get the logical connectors that are using a MSA document given by its ID.
     *
     * @param msaDocumentId The ID of the DocSystem entity.
     * @return A List of logical connectors.
     */
    List<LogicalConnector> findByMsaDocumentId(Integer msaDocumentId);

    /**
     * Get the logical connectors that are using a ARA document given by its ID.
     *
     * @param araDocumentId The ID of the DocSystem entity.
     * @return A List of logical connectors.
     */
    List<LogicalConnector> findByAraDocumentId(Integer araDocumentId);

    List<LogicalConnector> findByIdIn(@Param("ids") Set<Integer> ids);

}
