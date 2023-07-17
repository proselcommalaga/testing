package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentOnDemand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeploymentOnDemandRepository extends JpaRepository<DeploymentOnDemand, Integer>
{
}
