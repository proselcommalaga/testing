package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BehaviorTagsRepository extends JpaRepository<BehaviorTag, Integer>
{

}
