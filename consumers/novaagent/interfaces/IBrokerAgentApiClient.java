package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces;

import com.bbva.enoa.apirestgen.brokeragentapi.model.BAConnectionInfoDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.BANodeInfoDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ChannelsActionDTO;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.ExchangeInfoDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.QueueInfoDTO;

/**
 * The interface Broker agent api client.
 */
public interface IBrokerAgentApiClient
{
    /**
     * Create channels.
     *
     * @param environment       the environment
     * @param channelsActionDTO the channels action dto
     */
    void createChannels(String environment, ChannelsActionDTO channelsActionDTO);


    /**
     * Get queues info queue info dto [ ].
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param age           the age
     * @return the queue info dto [ ]
     */
    QueueInfoDTO[] getQueuesInfo(String environment, ConnectionDTO connectionDTO, Integer age);

    /**
     * Get messages from queue message dto [ ].
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param queueName     the queue name
     * @param count         the count
     * @return the message dto [ ]
     */
    MessageDTO[] getMessagesFromQueue(String environment, ConnectionDTO connectionDTO, String queueName, Integer count);

    /**
     * Get exchanges info exchange info dto [ ].
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param age           the age
     * @return the exchange info dto [ ]
     */
    ExchangeInfoDTO[] getExchangesInfo(String environment, ConnectionDTO connectionDTO, Integer age);

    /**
     * Delete exchange.
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param exchangeName  the exchange name
     */
    void deleteExchange(String environment, ConnectionDTO connectionDTO, String exchangeName);

    /**
     * Publish message on exchange.
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param message       the message
     * @param exchangeName  the exchange name
     */
    void publishMessageOnExchange(String environment, ConnectionDTO connectionDTO, MessageDTO message, String exchangeName);

    /**
     * Delete queue.
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param queueName     the queue name
     */
    void deleteQueue(String environment, ConnectionDTO connectionDTO, String queueName);

    /**
     * Purge queue.
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @param queueName     the queue name
     */
    void purgeQueue(String environment, ConnectionDTO connectionDTO, String queueName);

    /**
     * Get nodes info
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @return list of node info
     */
    BANodeInfoDTO[] getNodesInfo(final String environment, final ConnectionDTO connectionDTO);

    /**
     * Get connections info
     *
     * @param environment   the environment
     * @param connectionDTO the connection dto
     * @return list of connection info
     */
    BAConnectionInfoDTO[] getConnectionsInfo(final String environment, final ConnectionDTO connectionDTO);

    /**
     *  Check if node or cluster is up and responsive
     *
     * @param environment the environment
     * @param connectionDTO the connection dto
     * @return true if node or cluster is up
     */
    boolean isUp(final String environment, final ConnectionDTO connectionDTO);
}
