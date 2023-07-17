package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceBrokerDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.DeploymentBrokerProperty;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;

import java.util.List;

/**
 * Interface to manage brokers in deployments plans, services or instances
 */
public interface IDeploymentBroker
{

    /**
     * Build a deploymentServiceBrokerDTO list from a given deploymentService entity
     *
     * @param deploymentService deploymentService entity
     * @return deploymentServiceBrokerDTO list
     */
    DeploymentServiceBrokerDTO[] buildDeploymentServiceBrokerDTOsFromDeploymentServiceEntity(DeploymentService deploymentService);

    /**
     * Get a broker entity list from a given deploymentServiceDto
     *
     * @param deploymentServiceDto deploymentServiceDTO
     * @return list of brokers used by this deploymentService DTO
     */
    List<Broker> getBrokersEntitiesFromDeploymentServiceDTO(DeploymentServiceDto deploymentServiceDto);

    /**
     * Get the activity if attached brokers of deploymentService entity have any changes in input deploymentServiceDTO
     *
     * @param deploymentService    the deployment service where brokers belongs to
     * @param deploymentServiceDto the deployment service dto with new info about the deploymentService
     * @return list of activities to emit
     */
    List<GenericActivity> getActivityAttachedDeploymentServiceBrokerChange(DeploymentService deploymentService, DeploymentServiceDto deploymentServiceDto);

    /**
     * Create and persist in bbdd broker properties for given deploymentService, used in copy, promotion, adn attack of brokers on a deployment service
     *
     * @param deploymentService the deployment service
     * @param configurationRevision configuration revision
     * @param brokerList brokers attacked to deployment service
     * @return List of deployment broker property attached to deployment service and configuration revision.
     */
    List<DeploymentBrokerProperty> createAndPersistBrokerPropertiesOfDeploymentService(DeploymentService deploymentService, ConfigurationRevision configurationRevision, List<Broker> brokerList);

}
