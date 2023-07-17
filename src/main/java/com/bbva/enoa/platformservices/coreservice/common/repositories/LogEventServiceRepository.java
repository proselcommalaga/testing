package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.monitoring.entities.LogEventService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * LogEventService repository
 */
public interface LogEventServiceRepository extends JpaRepository<LogEventService, Integer>
{
    /**
     * Find by release version service id
     *
     * @param releaseVersionServiceId release version service id
     * @return list of log event services
     */
    List<LogEventService> findByReleaseVersionServiceId(@Param("releaseVersionServiceId") final Integer releaseVersionServiceId);
}
