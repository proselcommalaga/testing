package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.MessageDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerBuilder;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerExchangeOperator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerNotificationService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerValidator;
import com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBrokerAgentApiClient;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The type Broker exchange operator.
 */
@Component
public class BrokerExchangeOperator implements IBrokerExchangeOperator
{
    protected static final NovaException PERMISSION_DENIED = new NovaException(BrokerError.getPermissionDeniedError());

    private final IBrokerValidator brokerValidator;
    private final IBrokerBuilder brokerBuilder;
    private final IBrokerAgentApiClient brokerAgentClient;
    private final IUsersClient usersService;
    private final IBrokerNotificationService brokerNotificationService;

    /**
     * Instantiates a new Broker exchange operator.
     *
     * @param brokerValidator           the broker validator
     * @param brokerBuilder             the broker builder
     * @param brokerAgentClient         the broker agent client
     * @param usersService
     * @param brokerNotificationService
     */
    @Autowired
    public BrokerExchangeOperator(final IBrokerValidator brokerValidator,
                                  final IBrokerBuilder brokerBuilder,
                                  final IBrokerAgentApiClient brokerAgentClient, final IUsersClient usersService, final IBrokerNotificationService brokerNotificationService)
    {
        this.brokerValidator = brokerValidator;
        this.brokerBuilder = brokerBuilder;
        this.brokerAgentClient = brokerAgentClient;
        this.usersService = usersService;
        this.brokerNotificationService = brokerNotificationService;
    }

    @Override
    public void deleteExchange(final String ivUser, final Integer brokerId, final String exchangeName)
    {
        // validations and permissions
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        this.brokerValidator.validateBrokerCanBeOperable(broker);
        this.usersService.checkHasPermission(ivUser, BrokerConstants.BrokerPermissions.DELETE_EXCHANGE_OR_QUEUE, broker.getEnvironment(), broker.getProduct().getId(), PERMISSION_DENIED);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());

        // execute operation
        this.brokerAgentClient.deleteExchange(broker.getEnvironment(), connectionDTO, exchangeName);

        // register activity
        this.brokerNotificationService.registerBrokerActivity(ActivityAction.DELETED_EXCHANGE, broker, true, "Exchange " + exchangeName + " has been deleted", ivUser);

    }

    @Override
    public void publishMessageOnExchange(final String ivUser, final MessageDTO message, final Integer brokerId, final String exchangeName)
    {

        // validations and permissions
        Broker broker = this.brokerValidator.validateAndGetBroker(brokerId);
        this.brokerValidator.validateBrokerCanBeOperable(broker);
        this.usersService.checkHasPermission(ivUser, BrokerConstants.BrokerPermissions.PUBLISH_MESSAGE_ON_EXCHANGE, broker.getEnvironment(), broker.getProduct().getId(), PERMISSION_DENIED);
        BrokerUser brokerUser = this.brokerValidator.validateAndGetBrokerAdminUser(broker);
        ConnectionDTO connectionDTO = this.brokerBuilder.buildConnectionDTOFromBrokerUserAndBrokerList(brokerUser, broker.getNodes());

        // execute operation
        this.brokerAgentClient.publishMessageOnExchange(broker.getEnvironment(), connectionDTO, message, exchangeName);

        // register activity
        this.brokerNotificationService.registerBrokerActivity(ActivityAction.PUBLISH_MESSAGE_ON_EXCHANGE, broker, true, "Has been published a message in exchange: " + exchangeName + " with Payload: " + message, ivUser);

    }

}
