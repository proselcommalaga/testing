package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;

import java.util.List;

/**
 * Logical connector repository service interface
 */
public interface ILogicalConnectorRepositoriesService
{
    /**
     * Delete a deployment connector property entities from logical connector property list from NOVA BD
     *
     * @param logicalConnectorPropertyList the logical connector properties list to remove of the Deployment Connector Property Entity
     */
    void deleteDeploymentConnectorPropertyList(List<LogicalConnectorProperty> logicalConnectorPropertyList);

    /**
     * Delete a logical connector from NOVA DB
     *
     * @param logicalConnector the logical connector to delete
     */
    void deleteLogicalConnector(LogicalConnector logicalConnector);

    /**
     * Delete the logical connector associated in the deployment service list from relationship matching by logical connector name
     *
     * @param deploymentServiceList deploymentServiceList
     *                              @param logicalConnector logical connector
     */
    void deleteLogicalConnectorOfDeploymentService(List<DeploymentService> deploymentServiceList, LogicalConnector logicalConnector);
}
