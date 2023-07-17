package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorPropertyValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BehaviorPropertyValueRepository extends JpaRepository<BehaviorPropertyValue, Integer>
{
    Optional<List<BehaviorPropertyValue>> findByBehaviorServiceConfigurationId(Integer behaviorServiceConfigurationId);
}
