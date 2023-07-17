package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Release version subsystem repository.
 */
@Transactional(readOnly = true)
public interface ReleaseVersionSubsystemRepository extends JpaRepository<ReleaseVersionSubsystem, Integer>
{
    /**
     * Gets all release version subsystem by releaseVerionId
     *
     * @param releaseVersionId release version id
     * @return List of release version subsystem.
     */
    List<ReleaseVersionSubsystem> findByReleaseVersionId(@Param("releaseVersionId") final Integer releaseVersionId);

    /**
     * Get all release version subsystem filter by subsystem identifier
     *
     * @param subsystemId subsystem identifier
     * @return list of release version subsystems
     */
    List<ReleaseVersionSubsystem> findBySubsystemId(@Param("subsystemId") final Integer subsystemId);
}