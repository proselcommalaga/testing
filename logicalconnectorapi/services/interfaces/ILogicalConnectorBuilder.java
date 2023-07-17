package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces;

import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;

import java.util.List;

/**
 * Logical connector builder interface
 */
public interface ILogicalConnectorBuilder
{
    /**
     * Builds an array of {@link LogicalConnectorDto} from the {@link LogicalConnector}
     * of a product.
     *
     * @param logicalConnectorList {@link LogicalConnector}
     * @return a LogicalConnectorDto array
     */
    LogicalConnectorDto[] buildLogicalConnectorDtoList(List<LogicalConnector> logicalConnectorList);

    /**
     * Build a new logical connector Dto from logical connector instance
     *
     * @param logicalConnector the logical connector instance
     * @param userCode         the user code to validate if can view the properties
     * @return a logical connector Dto instance
     */
    LogicalConnectorDto buildLogicalConnectorDto(LogicalConnector logicalConnector, String userCode);
}
