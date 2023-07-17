package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentLabel;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Deployment label repository.
 *
 */
public interface DeploymentLabelRepository extends JpaRepository<DeploymentLabel, Integer>
{
    /**
     * Gets the labels for a service and an environment
     *
     * @param serviceName the name of the service
     * @param environment the environment
     * @return the labels
     */
    DeploymentLabel findFirstByEnvironmentAndServiceName(String environment, String serviceName);
}
