package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author xe30000
 */
public interface DeploymentSubsystemRepository extends JpaRepository<DeploymentSubsystem, Integer>
{
	DeploymentSubsystem findByDeploymentPlanIdAndSubsystem  (@Param("planId") Integer planId, @Param("releaseVersionSubsystem") ReleaseVersionSubsystem releaseVersionSubsystem );

    /**
     * Get subsystem name
     * @param subsystemId  subsystem id
     */
    @Query(value=
            "select rvs.subsystem_id " +
            "from deployment_subsystem ds " +
            "join release_version_subsystem rvs on ds.subsystem_id = rvs.id " +
            "where ds.id = :subsystemId",
            nativeQuery = true)
    Integer getSubsystemId(@Param("subsystemId") Integer subsystemId);
}
