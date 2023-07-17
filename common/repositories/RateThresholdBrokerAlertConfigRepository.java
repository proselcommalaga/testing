package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Broker node alert configuration repository.
 */
public interface RateThresholdBrokerAlertConfigRepository extends JpaRepository<RateThresholdBrokerAlertConfig, Integer>
{
}
