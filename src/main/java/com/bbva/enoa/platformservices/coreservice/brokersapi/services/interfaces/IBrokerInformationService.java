package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.*;

/**
 * The interface Broker information service.
 */
public interface IBrokerInformationService
{

    /**
     * Get the brokers list of a product
     *
     * @param productId product id
     * @return array of BrokerDTO objects
     */
    BrokerDTO[] getBrokersByProduct(Integer productId);

    /**
     * Get detailed info of an existing broker
     *
     * @param ivUser   ivUser
     * @param brokerId broker id
     * @return Detailed info as BrokerDTO object (use another type to include extra info in future versions)
     */
    BrokerDTO getBrokerInfo(String ivUser, Integer brokerId);

    /**
     * Get the list of valid number of nodes by environment for the product
     *
     * @param productId product id
     * @return List of valid number of nodes by environment and explanatory message
     */
    ValidNumberOfNodesByEnvironmentDTO getValidNumberOfNodesInfo(Integer productId);

    /**
     * Get queues info queue info dto [ ].
     *
     * @param brokerId the broker id
     * @param age      the age
     * @return the queue info dto [ ]
     */
    QueueInfoDTO[] getQueuesInfo(final Integer brokerId, final Integer age);

    /**
     * Get exchanges info exchange info dto [ ].
     *
     * @param brokerId the broker id
     * @param age      the age
     * @return the exchange info dto [ ]
     */
    ExchangeInfoDTO[] getExchangesInfo(final Integer brokerId, final Integer age);

    /**
     * Get broker statuses info
     *
     * @return array of broker statuses
     */
    String[] getBrokerStatuses();

    /**
     * Get broker tyoes info
     *
     * @return array of broker types
     */
    String[] getBrokerTypes();

}
