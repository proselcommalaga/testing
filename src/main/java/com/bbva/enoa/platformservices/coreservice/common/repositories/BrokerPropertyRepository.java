package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.broker.entities.BrokerProperty;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Broker property repository.
 */
public interface BrokerPropertyRepository extends JpaRepository<BrokerProperty, Integer>
{
}
