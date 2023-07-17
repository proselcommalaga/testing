package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BehaviorSubsystemRepository extends JpaRepository<BehaviorSubsystem, Integer>
{

    Optional<List<BehaviorSubsystem>> findByBehaviorVersionId(Integer behaviorVersion);

}
