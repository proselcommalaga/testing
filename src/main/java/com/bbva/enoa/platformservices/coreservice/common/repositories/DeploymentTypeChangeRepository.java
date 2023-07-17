package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repo for {@link DeploymentTypeChangeTask}.
 */
public interface DeploymentTypeChangeRepository extends JpaRepository<DeploymentTypeChangeTask, Integer >
{
}
