package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanProfileRepository extends JpaRepository<PlanProfile, Integer>
{
    PlanProfile findByDeploymentPlan(DeploymentPlan deploymentPlan);
}
