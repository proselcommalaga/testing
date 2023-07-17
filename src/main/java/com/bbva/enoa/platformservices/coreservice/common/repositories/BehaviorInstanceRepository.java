package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorInstance;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BehaviorInstanceRepository extends JpaRepository<BehaviorInstance, Integer>
{
    Optional<List<BehaviorInstance>> findByBehaviorExecutionId(Integer behaviorExecutionId);

    @Query("select b from BehaviorInstance b where b.behaviorServiceConfiguration.behaviorService.id = :behaviorServiceId")
    Page<BehaviorInstance> findByBehaviorServiceId(@Param("behaviorServiceId") Integer behaviorServiceId, Pageable pageable);

    @Query("select b from BehaviorInstance b " +
            "where b.behaviorServiceConfiguration.behaviorService.behaviorSubsystem in :behaviorSubsystems")
    Page<BehaviorInstance> findInstancesByBehaviorVersion(@Param("behaviorSubsystems") Collection<BehaviorSubsystem> behaviorSubsystems, Pageable pageable);

    @Query("select b from BehaviorInstance b " +
            "where b.behaviorServiceConfiguration.behaviorService.behaviorSubsystem in :behaviorSubsystems and b.behaviorServiceConfiguration.behaviorService.serviceName = :behaviorServiceName")
    Page<BehaviorInstance> findInstancesByBehaviorVersionAndBehaviorServiceName(@Param("behaviorSubsystems") Collection<BehaviorSubsystem> behaviorSubsystems,
                                                                                @Param("behaviorServiceName") String serviceName, Pageable pageable);
}
