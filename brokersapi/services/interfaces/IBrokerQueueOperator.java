package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;

/**
 * The interface Broker queue operator.
 */
public interface IBrokerQueueOperator
{
    /**
     * Purge queue.
     *  @param ivUser    the iv user
     * @param brokerId  the broker id
     * @param queueName the queue name
     */
    void purgeQueue(final String ivUser, final Integer brokerId, final String queueName);

    /**
     * Delete queue.
     *
     * @param ivUser    the iv user
     * @param brokerId  the broker id
     * @param queueName the queue name
     */
    void deleteQueue(final String ivUser, final Integer brokerId, final String queueName);

    /**
     * Get messages from queue message dto [ ].
     *
     * @param ivUser    the iv user
     * @param brokerId  the broker id
     * @param queueName the queue name
     * @param count     the count
     * @return the message dto [ ]
     */
    MessageDTO[] getMessagesFromQueue(final String ivUser, final Integer brokerId, final String queueName, final Integer count);
}
