package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNotificationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerQueueOperator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBrokerAgentApiClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BrokerQueueOperator implements IBrokerQueueOperator
{
    protected static final NovaException PERMISSION_DENIED = new NovaException(BrokerError.getPermissionDeniedError());

    private final IBrokerValidator brokerValidator;
    private final IBrokerBuilder brokerBuilder;
    private final IBrokerAgentApiClient brokerAgentClient;
    private final IUsersClient usersService;
    private final IBrokerNotificationService brokerNotificationService;

    @Autowired
    public BrokerQueueOperator(final IBrokerValidator brokerValidator,
                               final IBrokerBuilder brokerBuilder,
                               final IBrokerAgentApiClient brokerAgentClient,
                               final IUsersClient usersService,
                               final IBrokerNotificationService brokerNotificationService)
    {
        this.brokerValidator = brokerValidator;
        this.brokerBuilder = brokerBuilder;
        this.brokerAgentClient = brokerAgentClient;
        this.usersService = usersService;
        this.brokerNotificationService = brokerNotificationService;
    }

    @Override
    public void purgeQueue(final String ivUser, final Integer brokerId, final String queueName)
    {
        // validations and permissions
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        this.brokerValidator.validateBrokerCanBeOperable(broker);
        this.usersService.checkHasPermission(ivUser, BrokerConstants.BrokerPermissions.PURGE_QUEUE, broker.getEnvironment(), broker.getProduct().getId(), PERMISSION_DENIED);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());

        // execute operation
        this.brokerAgentClient.purgeQueue(broker.getEnvironment(), connectionDTO, queueName);

        // register activity
        this.brokerNotificationService.registerBrokerActivity(ActivityAction.PURGE_QUEUE, broker, true, "Queue " + queueName + " has been purged", ivUser);
    }

    @Override
    public void deleteQueue(final String ivUser, final Integer brokerId, final String queueName)
    {
        // validations and permissions
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        this.brokerValidator.validateBrokerCanBeOperable(broker);
        this.usersService.checkHasPermission(ivUser, BrokerConstants.BrokerPermissions.DELETE_EXCHANGE_OR_QUEUE, broker.getEnvironment(), broker.getProduct().getId(), PERMISSION_DENIED);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());

        // execute operation
        this.brokerAgentClient.deleteQueue(broker.getEnvironment(), connectionDTO, queueName);

        // register activity
        this.brokerNotificationService.registerBrokerActivity(ActivityAction.DELETED_QUEUE, broker, true, queueName + " has been deleted", ivUser);
    }

    @Override
    public MessageDTO[] getMessagesFromQueue(final String ivUser, final Integer brokerId, final String queueName, final Integer count)
    {
        // validations and permissions
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        this.brokerValidator.validateBrokerCanBeOperable(broker);
        this.usersService.checkHasPermission(ivUser, BrokerConstants.BrokerPermissions.SHOW_MESSAGES_FROM_QUEUE, broker.getEnvironment(), broker.getProduct().getId(), PERMISSION_DENIED);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());

        // execute operation
        MessageDTO[] messages = this.brokerAgentClient.getMessagesFromQueue(broker.getEnvironment(), connectionDTO, queueName, count);

        // register activity
        this.brokerNotificationService.registerBrokerActivity(ActivityAction.VIEW_MESSAGES_FROM_QUEUE, broker, true, "Has been retrieved " + count + " messages from queue " + queueName, ivUser);

        return messages;
    }

}
