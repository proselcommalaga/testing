package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorConfigurationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BehaviorServiceConfigurationRepository extends JpaRepository<BehaviorServiceConfiguration, Integer>
{
    Optional<BehaviorServiceConfiguration> findFirstByBehaviorServiceIdOrderByLastModifiedDesc(Integer behaviorServiceId);

    Optional<BehaviorServiceConfiguration> findFirstByBehaviorServiceIdAndStatusOrderByLastModifiedDesc(Integer behaviorServiceId, BehaviorConfigurationStatus status);
}
