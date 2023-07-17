package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;

/**
 * The interface Broker exchange operator.
 */
public interface IBrokerExchangeOperator
{
    /**
     * Delete exchange.
     *
     * @param ivUser       the iv user
     * @param brokerId     the broker id
     * @param exchangeName the exchange name
     */
    void deleteExchange(final String ivUser, final Integer brokerId, final String exchangeName);

    /**
     * Publish message on exchange.
     *
     * @param ivUser       the iv user
     * @param message      the message
     * @param brokerId     the broker id
     * @param exchangeName the exchange name
     */
    void publishMessageOnExchange(final String ivUser, final MessageDTO message, final Integer brokerId, final String exchangeName);

}
