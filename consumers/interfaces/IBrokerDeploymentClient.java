package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

/**
 * Interface for Broker Deployment API client
 */
public interface IBrokerDeploymentClient {

    /**
     * Create broker
     *
     * @param brokerId  Identifier of the new broker to create, currently in CREATING status
     * @return          true if call was OK
     */
    boolean createBroker (Integer brokerId);

    /**
     * Delete broker
     *
     * @param brokerId  Identifier of the broker to delete, currently in DELETING status
     * @return          true if call was OK
     */
    boolean deleteBroker (Integer brokerId);

    /**
     * Start broker
     *
     * @param brokerId  Identifier of the broker to start, currently in STOPPED status
     * @return          true if call was OK
     */
    boolean startBroker (Integer brokerId);

    /**
     * Stop broker
     *
     * @param brokerId  Identifier of the broker to stop, currently in RUNNING status
     * @return          true if call was OK
     */
    boolean stopBroker (Integer brokerId);

    /**
     * Restart broker
     *
     * @param brokerId  Identifier of the broker to restart, currently in RUNNING status
     * @return          true if call was OK
     */
    boolean restartBroker (Integer brokerId);

    /**
     * Start broker node
     *
     * @param brokerNodeId  Identifier of the broker node to start, currently in STOPPED status
     * @return          true if call was OK
     */
    boolean startBrokerNode (Integer brokerNodeId);

    /**
     * Stop broker node
     *
     * @param brokerNodeId  Identifier of the broker node to stop, currently in RUNNING status
     * @return          true if call was OK
     */
    boolean stopBrokerNode (Integer brokerNodeId);

    /**
     * Restart broker node
     *
     * @param brokerNodeId  Identifier of the broker node to restart, currently in RUNNING status
     * @return          true if call was OK
     */
    boolean restartBrokerNode (Integer brokerNodeId);
}
