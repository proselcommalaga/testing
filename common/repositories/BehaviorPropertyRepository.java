package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorProperty;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehaviorPropertyRepository extends JpaRepository<BehaviorProperty, Integer>
{
}
