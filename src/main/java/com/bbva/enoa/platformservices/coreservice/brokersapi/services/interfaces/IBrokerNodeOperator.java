package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface with all operations that could be done in a broker node
 */
public interface IBrokerNodeOperator
{
    /**
     * Stop a broker node
     *
     * @param ivUser       ivUser
     * @param brokerNodeId broker node id to stop
     * @throws NovaException if error occurs
     * @return
     */
    BrokerDTO stopBrokerNode(String ivUser, Integer brokerNodeId);

    /**
     * Start a broker node
     *
     * @param ivUser       ivUser
     * @param brokerNodeId broker node id to start
     * @throws NovaException if error occurs
     * @return
     */
    BrokerDTO startBrokerNode(String ivUser, Integer brokerNodeId);

    /**
     * Restart a broker node
     *
     * @param ivUser       ivUser
     * @param brokerNodeId broker node id to restart
     * @throws NovaException if error occurs
     * @return
     */
    BrokerDTO restartBrokerNode(String ivUser, Integer brokerNodeId);

    /**
     * method invoked by task service when an action is executed/rejected
     *
     * @param taskId           - Task ID
     * @param brokerNodeId     - Broker Node ID
     */
    void onTaskReply(Integer taskId, Integer brokerNodeId);
}
