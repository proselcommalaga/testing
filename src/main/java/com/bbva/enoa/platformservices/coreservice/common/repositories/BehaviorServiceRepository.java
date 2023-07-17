package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BehaviorServiceRepository extends JpaRepository<BehaviorService, Integer>
{
    //    BehaviorService findByBehaviorServiceIdAndProductId( Integer behaviorServiceId, Integer productId);
//
    List<BehaviorService> findByBehaviorSubsystemId(Integer behaviorSubsystemId);
//
//    BehaviorService findByBehaviorServiceIdAndBehaviorVersionIdAndProductId(Integer behaviorServiceId, Integer behaviorVersionId, Integer productId);
//
//    BehaviorService findByBehaviorServiceId_BehaviorServiceConfigurationAndStatusOrderByLastModified(Integer behaviorServiceId, String status);
}
