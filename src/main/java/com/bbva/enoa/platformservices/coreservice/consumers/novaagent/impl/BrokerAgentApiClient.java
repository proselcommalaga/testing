package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.*;
import com.bbva.enoa.apirestgen.brokersapi.model.ExchangeInfoDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.QueueInfoDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.RateDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBrokerAgentApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BrokerAgentApiClient implements IBrokerAgentApiClient
{
    private final BrokerAgentCaller novaAgentCaller;

    @Autowired
    public BrokerAgentApiClient(final BrokerAgentCaller novaAgentCaller)
    {
        this.novaAgentCaller = novaAgentCaller;
    }

    @Override
    public void createChannels(final String environment, final ChannelsActionDTO channelsActionDTO)
    {
        novaAgentCaller.callOnEnvironment(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.createChannels(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), channelsActionDTO));
    }

    @Override
    public QueueInfoDTO[] getQueuesInfo(final String environment, final ConnectionDTO connectionDTO, final Integer age)
    {

        BAQueueInfoDTO[] baQueueInfoDTO = novaAgentCaller.callOnEnvironmentWithReturn(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.getQueuesInfo(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO, age));

        List<QueueInfoDTO> queueInfoDTOList = this.baQueueInfoDTOListToQueueInfoDTOList(Arrays.asList(baQueueInfoDTO));

        return queueInfoDTOList.toArray(QueueInfoDTO[]::new);
    }

    @Override
    public MessageDTO[] getMessagesFromQueue(final String environment, final ConnectionDTO connectionDTO, final String queueName, final Integer count)
    {
        BAMessageDTO[] baMessageDTOS = novaAgentCaller.callOnEnvironmentWithReturn(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.getMessagesFromQueue(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO, queueName, count));

        List<MessageDTO> messageDTOList = this.buildMessageDTOListFromBAMessageDTOList(Arrays.asList(baMessageDTOS));

        return messageDTOList.toArray(MessageDTO[]::new);
    }

    @Override
    public ExchangeInfoDTO[] getExchangesInfo(final String environment, final ConnectionDTO connectionDTO, final Integer age)
    {
        BAExchangeInfoDTO[] baExchangeInfoDTO = novaAgentCaller.callOnEnvironmentWithReturn(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.getExchangesInfo(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO, age));

        List<ExchangeInfoDTO> exchangeInfoDTOList = this.buildExchangesInfoDTOListFromBAExchangeInfoDTOList(Arrays.asList(baExchangeInfoDTO));

        return exchangeInfoDTOList.toArray(ExchangeInfoDTO[]::new);
    }

    @Override
    public void deleteExchange(final String environment, final ConnectionDTO connectionDTO, final String exchangeName)
    {
        novaAgentCaller.callOnEnvironment(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.deleteExchange(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO, exchangeName));
    }

    @Override
    public void publishMessageOnExchange(final String environment, final ConnectionDTO connectionDTO, final MessageDTO message, final String exchangeName)
    {
        MessageActionDTO messageActionDTO = new MessageActionDTO();
        messageActionDTO.setConnection(connectionDTO);
        messageActionDTO.setMessage(this.buildBAMessageDTOFromMessageDTO(message));

        novaAgentCaller.callOnEnvironment(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.publishMessageOnExchange(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), messageActionDTO, exchangeName));

    }

    @Override
    public void deleteQueue(final String environment, final ConnectionDTO connectionDTO, final String queueName)
    {
        novaAgentCaller.callOnEnvironment(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.deleteQueue(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO, queueName));

    }

    @Override
    public void purgeQueue(final String environment, final ConnectionDTO connectionDTO, final String queueName)
    {
        novaAgentCaller.callOnEnvironment(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.purgeQueue(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO, queueName));

    }

    @Override
    public BANodeInfoDTO[] getNodesInfo(final String environment, final ConnectionDTO connectionDTO)
    {
        return novaAgentCaller.callOnEnvironmentWithReturn(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.getNodesInfo(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO));
    }

    @Override
    public BAConnectionInfoDTO[] getConnectionsInfo(final String environment, final ConnectionDTO connectionDTO)
    {
        return novaAgentCaller.callOnEnvironmentWithReturn(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.getConnectionsInfo(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO));
    }

    @Override
    public boolean isUp(final String environment, final ConnectionDTO connectionDTO)
    {
        return novaAgentCaller.callOnEnvironmentWithReturn(
                environment,
                (novaConfig, iRestHandlerBrokeragentapi) ->
                        iRestHandlerBrokeragentapi.isUp(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), connectionDTO));
    }

    private List<ExchangeInfoDTO> buildExchangesInfoDTOListFromBAExchangeInfoDTOList(final List<BAExchangeInfoDTO> baExchangeInfoDTOList)
    {
        return baExchangeInfoDTOList.stream().map(baExchangeInfoDTO -> {
            ExchangeInfoDTO exchangeInfoDTO = new ExchangeInfoDTO();
            exchangeInfoDTO.setName(baExchangeInfoDTO.getName());
            exchangeInfoDTO.setMessagesOutRate(this.getRateDTOFromBARateDTO(baExchangeInfoDTO.getMessagesOutRate()));
            exchangeInfoDTO.setMessagesInRate(this.getRateDTOFromBARateDTO(baExchangeInfoDTO.getMessagesInRate()));
            return exchangeInfoDTO;
        }).collect(Collectors.toList());
    }

    private List<MessageDTO> buildMessageDTOListFromBAMessageDTOList(final List<BAMessageDTO> baMessageDTOList)
    {
        return baMessageDTOList.stream().map(baMessageDTO -> {
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setMessage(baMessageDTO.getMessage());
            return messageDTO;
        }).collect(Collectors.toList());
    }

    private BAMessageDTO buildBAMessageDTOFromMessageDTO(final MessageDTO messageDTO)
    {
        BAMessageDTO baMessageDTO = new BAMessageDTO();
        baMessageDTO.setMessage(messageDTO.getMessage());
        return baMessageDTO;
    }

    private List<QueueInfoDTO> baQueueInfoDTOListToQueueInfoDTOList(final List<BAQueueInfoDTO> baQueueInfoDTOList)
    {
        return baQueueInfoDTOList.stream().map(baQueueInfoDTO -> {
            QueueInfoDTO queueInfoDTO = new QueueInfoDTO();
            queueInfoDTO.setName(baQueueInfoDTO.getName());
            queueInfoDTO.setConsumerACKRate(this.getRateDTOFromBARateDTO(baQueueInfoDTO.getConsumerACKRate()));
            queueInfoDTO.setPublishRate(this.getRateDTOFromBARateDTO(baQueueInfoDTO.getPublishRate()));
            queueInfoDTO.setTotalConsumers(baQueueInfoDTO.getTotalConsumers());
            queueInfoDTO.setQueuedMessages(baQueueInfoDTO.getQueuedMessages());
            return queueInfoDTO;
        }).collect(Collectors.toList());
    }

    private RateDTO getRateDTOFromBARateDTO(final BARateDTO baRateDTO)
    {
        RateDTO rateDTO = new RateDTO();
        rateDTO.setUnit(baRateDTO.getUnit());
        rateDTO.setValue(baRateDTO.getValue());
        return rateDTO;
    }
}
