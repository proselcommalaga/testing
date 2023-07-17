package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;

/**
 * Physical connector repositories service interface
 */
public interface IPhysicalConnectorRepositoriesService
{
    /**
     * Delete a deployment connector property entities from logical connector property from NOVA BBDD
     *
     * @param logicalConnectorProperty the logical connector properties to remove of the Deployment Connector Property Entity
     */
    void deleteDeploymentConnectorProperty(LogicalConnectorProperty logicalConnectorProperty);
}
