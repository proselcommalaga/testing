package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.todotask.entities.BrokerTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Broker Task Repository
 */
public interface BrokerTaskRepository extends JpaRepository <BrokerTask, Integer> {

    /**
     * Find the todo task associated with the broker
     *
     * @param brokerId
     * @return
     */
    @Query("select bt from BrokerTask bt where bt.broker.id = :brokerId")
    List<BrokerTask> findByBrokerId(@Param("brokerId") Integer brokerId);
}
