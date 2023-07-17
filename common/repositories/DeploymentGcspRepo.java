package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xe72018
 */
@Transactional(readOnly = true)
public interface DeploymentGcspRepo extends JpaRepository<DeploymentGcsp, Integer>
{
    DeploymentGcsp findFirstByUndeployRelease(Integer id);
}
