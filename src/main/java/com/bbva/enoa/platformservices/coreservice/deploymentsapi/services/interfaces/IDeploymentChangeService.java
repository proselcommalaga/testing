package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import org.springframework.data.domain.Page;

/**
 * Deployment change service interface
 */
public interface IDeploymentChangeService
{
    /**
     * Deployments history
     *
     * @param deploymentId with the deployment identifier
     * @param pageNumber Number of page to retrieve
     * @param pageSize   Number of results by page
     * @return page with notifications
     */
    Page<DeploymentChange> getHistory(Integer deploymentId, Long pageNumber, Long pageSize);
}
