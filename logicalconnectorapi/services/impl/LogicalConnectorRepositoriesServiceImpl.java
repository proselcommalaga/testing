package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;


import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorRepositoriesService;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Class dedicated to validate operations of the physical connector
 */
@Service
public class LogicalConnectorRepositoriesServiceImpl implements ILogicalConnectorRepositoriesService
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(LogicalConnectorRepositoriesServiceImpl.class);

    /**
     * Deployment connector property repository
     */
    private final DeploymentConnectorPropertyRepository deploymentConnectorPropertyRepository;

    /**
     * Logical Connector repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * Dependency injection constructor
     *
     * @param deploymentConnectorPropertyRepository deploymentConnectorPropertyRepository
     *                                              @param logicalConnectorRepository logicalConnectorRepository
     */
    @Autowired
    public LogicalConnectorRepositoriesServiceImpl(final DeploymentConnectorPropertyRepository deploymentConnectorPropertyRepository,
                                                   final LogicalConnectorRepository logicalConnectorRepository)
    {
        this.deploymentConnectorPropertyRepository = deploymentConnectorPropertyRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
    }

    //////////////////////////////////// IMPLEMENT ////////////////////////////////////////////////////////////////

    @Override
    @Transactional
    public void deleteDeploymentConnectorPropertyList(final List<LogicalConnectorProperty> logicalConnectorPropertyList)
    {
        for (LogicalConnectorProperty logicalConnectorProperty : logicalConnectorPropertyList)
        {
            long deploymentConnectorPropertiesNumberDeleted = this.deploymentConnectorPropertyRepository.deleteByLogicalConnectorPropertyId(logicalConnectorProperty.getId());
            LOG.debug("[{}] -> [deleteDeploymentConnectorPropertyList]: the number of deployment Connector Properties Number Deleted is: [{}]", Constants.LOGICAL_CONNECTOR_API,
                    deploymentConnectorPropertiesNumberDeleted);
        }
    }

    @Override
    @Transactional
    public void deleteLogicalConnector(LogicalConnector logicalConnector)
    {
        this.logicalConnectorRepository.delete(logicalConnector);
    }

    @Override
    @Transactional
    public void deleteLogicalConnectorOfDeploymentService(final List<DeploymentService> deploymentServiceList, final LogicalConnector logicalConnector)
    {
        for (DeploymentService deploymentService : deploymentServiceList)
        {
            if (deploymentService.getLogicalConnectors().removeIf(deploymentServiceLogicalConnector -> deploymentServiceLogicalConnector.getName().equalsIgnoreCase(logicalConnector.getName())))
            {
                LOG.debug("[{}] -> [deleteLogicalConnectorOfDeploymentService]: deleted the logical connector associated of the deployment service name: [{}] " +
                        "with logical connector name: [{}]", Constants.LOGICAL_CONNECTOR_API, deploymentService.getService().getServiceName(), logicalConnector.getName());
            }
            else
            {
                LOG.debug("[{}] -> [deleteLogicalConnectorOfDeploymentService]: the deployment service name: [{}] does not have associated the" +
                        " logical connector name: [{}]. Not deleted", Constants.LOGICAL_CONNECTOR_API, deploymentService.getService().getServiceName(), logicalConnector.getName());
            }
        }
    }

}

