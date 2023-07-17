package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Broker repository
 */
public interface BrokerNodeRepository extends JpaRepository<BrokerNode, Integer>
{

}
