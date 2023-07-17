package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface with all operations that could be done in a broker
 */
public interface IBrokerOperator
{
    /**
     * Create a new broker
     *
     * @param ivUser    ivUser
     * @param newBroker new broker object
     * @return BrokerDTO object
     * @throws NovaException if error occurs
     */
    BrokerDTO createBroker(String ivUser, BrokerDTO newBroker);

    /**
     * Delete an existing broker
     *
     * @param ivUser   ivUser
     * @param brokerId broker id to delete
     * @throws NovaException if error occurs
     */
    void deleteBroker(String ivUser, Integer brokerId);

    /**
     * Stop an existing broker
     *
     * @param ivUser   ivUser
     * @param brokerId broker id to stop
     * @return BrokerDTO object
     * @throws NovaException if error occurs
     */
    BrokerDTO stopBroker(String ivUser, Integer brokerId);

    /**
     * Start an existing broker
     *
     * @param ivUser   ivUser
     * @param brokerId broker id to start
     * @return BrokerDTO object
     * @throws NovaException if error occurs
     */
    BrokerDTO startBroker(String ivUser, Integer brokerId);

    /**
     * Restart an existing broker
     *
     * @param ivUser   ivUser
     * @param brokerId broker id to restart
     * @return BrokerDTO object
     * @throws NovaException if error occurs
     */
    BrokerDTO restartBroker(String ivUser, Integer brokerId);

    /**
     * method invoked by task service when an action is executed/rejected
     *
     * @param taskId       - Task ID
     * @param brokerId     - Broker ID
     */
    void onTaskReply(Integer taskId, Integer brokerId);
}
