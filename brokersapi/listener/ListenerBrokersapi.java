package com.bbva.enoa.platformservices.coreservice.brokersapi.listener;

import com.bbva.enoa.apirestgen.brokersapi.model.*;
import com.bbva.enoa.apirestgen.brokersapi.server.spring.nova.rest.IRestListenerBrokersapi;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Brokers API listener
 */
@Service
public class ListenerBrokersapi implements IRestListenerBrokersapi
{
    /**
     * Broker service
     */
    private final IBrokerOperator brokerService;

    /**
     * Broker node service
     */
    private final IBrokerNodeOperator brokerNodeService;

    /**
     * Broker service
     */
    private final IBrokerInformationService brokerInformationGetter;

    /**
     * Broker notification service
     */
    private final IBrokerNotificationService brokerNotificationService;
    private final IBrokerQueueOperator brokerQueueOperator;
    private final IBrokerExchangeOperator brokerExchangeOperator;
    private final IBrokerAlertService brokerAlertService;

    /**
     * Instantiates a new Listener brokersapi.
     *
     * @param brokerService             the broker service
     * @param brokerNodeService         the broker node service
     * @param brokerInformationGetter   the broker information getter
     * @param brokerNotificationService the broker notification service
     * @param brokerQueueOperator       the broker queue operator
     * @param brokerExchangeOperator    the broker exchange operator
     * @param brokerAlertService    the broker alert service
     */
    @Autowired
    public ListenerBrokersapi(final IBrokerOperator brokerService,
                              final IBrokerNodeOperator brokerNodeService,
                              final IBrokerInformationService brokerInformationGetter,
                              final IBrokerNotificationService brokerNotificationService,
                              final IBrokerQueueOperator brokerQueueOperator,
                              final IBrokerExchangeOperator brokerExchangeOperator,
                              final IBrokerAlertService brokerAlertService)
    {
        this.brokerService = brokerService;
        this.brokerNodeService = brokerNodeService;
        this.brokerInformationGetter = brokerInformationGetter;
        this.brokerNotificationService = brokerNotificationService;
        this.brokerQueueOperator = brokerQueueOperator;
        this.brokerExchangeOperator = brokerExchangeOperator;
        this.brokerAlertService = brokerAlertService;
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BrokerDTO[] getBrokersByProduct(NovaMetadata novaMetadata, Integer productId) throws Errors
    {
        return this.brokerInformationGetter.getBrokersByProduct(productId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BrokerDTO getBrokerDetails(NovaMetadata novaMetadata, Integer brokerId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerInformationGetter.getBrokerInfo(ivUser, brokerId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO createBroker(NovaMetadata novaMetadata, BrokerDTO brokerDTO) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerService.createBroker(ivUser, brokerDTO);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO startBroker(NovaMetadata novaMetadata, Integer brokerId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerService.startBroker(ivUser, brokerId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO stopBroker(NovaMetadata novaMetadata, Integer brokerId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerService.stopBroker(ivUser, brokerId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public MessageDTO[] getMessagesFromQueue(final NovaMetadata novaMetadata, final Integer brokerId, final String queueName, final Integer count) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerQueueOperator.getMessagesFromQueue(ivUser, brokerId, queueName, count);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO restartBroker(NovaMetadata novaMetadata, Integer brokerId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerService.restartBroker(ivUser, brokerId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void deleteBroker(NovaMetadata novaMetadata, Integer brokerId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.brokerService.deleteBroker(ivUser, brokerId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void purgeQueue(final NovaMetadata novaMetadata, final Integer brokerId, final String queueName) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.brokerQueueOperator.purgeQueue(ivUser, brokerId, queueName);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public ExchangeInfoDTO[] getExchangesInfo(final NovaMetadata novaMetadata, final Integer brokerId, final Integer age) throws Errors
    {
        return this.brokerInformationGetter.getExchangesInfo(brokerId, age);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO startBrokerNode(NovaMetadata novaMetadata, Integer brokerNodeId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerNodeService.startBrokerNode(ivUser, brokerNodeId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO stopBrokerNode(NovaMetadata novaMetadata, Integer brokerNodeId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerNodeService.stopBrokerNode(ivUser, brokerNodeId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public QueueInfoDTO[] getQueuesInfo(final NovaMetadata novaMetadata, final Integer brokerId, final Integer age) throws Errors
    {
        return this.brokerInformationGetter.getQueuesInfo(brokerId, age);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public String[] getBrokerTypes(NovaMetadata novaMetadata) throws Errors
    {
        return this.brokerInformationGetter.getBrokerTypes();
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerDTO restartBrokerNode(NovaMetadata novaMetadata, Integer brokerNodeId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        return this.brokerNodeService.restartBrokerNode(ivUser, brokerNodeId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void notifyBrokerOperationResult(NovaMetadata novaMetadata, BrokerNotificationDTO brokerNotificationDTO, Integer brokerId) throws Errors
    {
        this.brokerNotificationService.notifyBrokerOperationResult(brokerId, brokerNotificationDTO);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void notifyBrokerNodeOperationResult(NovaMetadata novaMetadata, BrokerNotificationDTO brokerNotificationDTO, Integer brokerNodeId) throws Errors
    {
        this.brokerNotificationService.notifyBrokerNodeOperationResult(brokerNodeId, brokerNotificationDTO);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void deleteQueue(final NovaMetadata novaMetadata, final Integer brokerId, final String queueName) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.brokerQueueOperator.deleteQueue(ivUser, brokerId, queueName);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public String[] getBrokersStatuses(NovaMetadata novaMetadata) throws Errors
    {
        return this.brokerInformationGetter.getBrokerStatuses();
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public ValidNumberOfNodesByEnvironmentDTO getValidNumberOfNodes(NovaMetadata novaMetadata, Integer productId) throws Errors
    {
        return this.brokerInformationGetter.getValidNumberOfNodesInfo(productId);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void deleteExchange(final NovaMetadata novaMetadata, final Integer brokerId, final String exchangeName) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.brokerExchangeOperator.deleteExchange(ivUser, brokerId, exchangeName);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void publishMessageOnExchange(final NovaMetadata novaMetadata, final MessageDTO message, final Integer brokerId, final String exchangeName) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.brokerExchangeOperator.publishMessageOnExchange(ivUser, message, brokerId, exchangeName);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public void updateBrokerAlertConfiguration(final NovaMetadata novaMetadata, final BrokerAlertConfigDTO alertConfig, final Integer brokerId) throws Errors
    {
        String ivUser = MetadataUtils.getIvUser(novaMetadata);
        this.brokerAlertService.updateBrokerAlertConfiguration(ivUser, brokerId, alertConfig);
    }

    @Override
    @LogAndTrace(apiName = BrokerConstants.BROKERS_API, runtimeExceptionErrorCode = BrokerErrorCode.UNEXPECTED_ERROR_CODE)
    public BrokerAlertInfoDTO[] getBrokerAlerts(final NovaMetadata novaMetadata, final Integer productId) throws Errors
    {
        return this.brokerAlertService.getBrokerAlertsByProduct(productId);
    }

    @Override
    public void onTaskReply(NovaMetadata novaMetadata, Integer brokerId, Integer taskId, Integer brokerNodeId) throws Errors {

        if (brokerNodeId != 0)
        {
            this.brokerNodeService.onTaskReply(taskId, brokerNodeId);
        }
        else
        {
            this.brokerService.onTaskReply(taskId, brokerId);
        }
    }
}
