package com.bbva.enoa.platformservices.coreservice.brokersapi.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Alert types for brokers as defined in alert service
 */
@AllArgsConstructor
public enum BrokerMonitoringAlertType
{
    APP_HEALTH_BROKER("BROKER-001"),
    APP_UNAVAILABLE_NODE_BROKER("BROKER-002"),
    APP_OVERFLOWED_BROKER("BROKER-003"),
    APP_QUEUE_THRESHOLD_BROKER("BROKER-004"),
    APP_PUBLISH_RATE_THRESHOLD_BROKER("BROKER-005"),
    APP_CONSUMER_RATE_THRESHOLD_BROKER("BROKER-006");

    @Getter
    private final String alertCode;
}
