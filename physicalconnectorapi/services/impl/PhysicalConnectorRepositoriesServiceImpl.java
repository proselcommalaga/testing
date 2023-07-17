package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.interfaces.IPhysicalConnectorRepositoriesService;
import com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Class dedicated to validate operations of the physical connector
 */
@Service
public class PhysicalConnectorRepositoriesServiceImpl implements IPhysicalConnectorRepositoriesService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PhysicalConnectorRepositoriesServiceImpl.class);

    /**
     * Deployment connector property repository
     */
    private final DeploymentConnectorPropertyRepository deploymentConnectorPropertyRepository;

    /**
     * Dependency injection constructor
     *
     * @param deploymentConnectorPropertyRepository deployment Connector Property Repository
     */
    @Autowired
    public PhysicalConnectorRepositoriesServiceImpl(final DeploymentConnectorPropertyRepository deploymentConnectorPropertyRepository)
    {
        this.deploymentConnectorPropertyRepository = deploymentConnectorPropertyRepository;
    }

    //////////////////////////////////// IMPLEMENT ////////////////////////////////////////////////////////////////

    @Override
    @Transactional
    public void deleteDeploymentConnectorProperty(final LogicalConnectorProperty logicalConnectorProperty)
    {
        long deploymentConnectorPropertiesNumberDeleted = this.deploymentConnectorPropertyRepository.deleteByLogicalConnectorPropertyId(logicalConnectorProperty.getId());
        LOG.debug("[{}] -> [deleteDeploymentConnectorProperty]: the number of deployment Connector Properties Number Deleted is: [{}]",
                Constants.PHYSICAL_CONNECTOR_API, deploymentConnectorPropertiesNumberDeleted);

    }
}

