package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerNotificationDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;

/**
 * Interface for broker notification service
 */
public interface IBrokerNotificationService
{

    /**
     * Notify the result of an operation on a broker
     *
     * @param brokerId              broker id
     * @param brokerNotificationDTO notification info
     */
    void notifyBrokerOperationResult(Integer brokerId, BrokerNotificationDTO brokerNotificationDTO);

    /**
     * Notify the result of an operation on a broker node
     *
     * @param brokerNodeId          broker node id
     * @param brokerNotificationDTO notification info
     */
    void notifyBrokerNodeOperationResult(Integer brokerNodeId, BrokerNotificationDTO brokerNotificationDTO);

    /**
     * Register broker activity.
     *
     * @param action                 the action
     * @param broker                 the broker
     * @param wasSuccessfulOperation the was successful operation
     * @param descriptionMessage     the description message
     * @param requesterIvUser        the requester iv user
     */
    void registerBrokerActivity(ActivityAction action, Broker broker, boolean wasSuccessfulOperation, String descriptionMessage, String requesterIvUser);

}
