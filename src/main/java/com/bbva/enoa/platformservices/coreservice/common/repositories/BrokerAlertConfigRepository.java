package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Broker node alert configuration repository.
 */
public interface BrokerAlertConfigRepository extends JpaRepository<BrokerAlertConfig, Integer>
{
}
