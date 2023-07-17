package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;

public interface IBrokerTaskProcessor
{

    /**
     * Processes the response of a BrokerTask when is closed.
     * Logic will be different depending on the task type.
     *
     * @param broker
     * @param node
     * @param taskId
     */
    void onTaskreply(Broker broker, BrokerNode node, Integer taskId);
}
