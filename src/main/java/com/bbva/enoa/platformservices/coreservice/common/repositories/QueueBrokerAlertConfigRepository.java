package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The interface Broker node alert configuration repository.
 */
public interface QueueBrokerAlertConfigRepository extends JpaRepository<QueueBrokerAlertConfig, Integer>
{
}
