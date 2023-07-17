package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xe72018
 */
@Transactional(readOnly = true)
public interface DeploymentNovaRepo extends JpaRepository<DeploymentNova, Integer>
{
    DeploymentNova findFirstByUndeployRelease(Integer id);
}
