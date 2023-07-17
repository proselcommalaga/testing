package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerNodeOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerOperation;
import com.bbva.enoa.platformservices.coreservice.brokersapi.model.BrokerValidatedObjects;

/**
 * Broker validator interface
 */
public interface IBrokerValidator
{

    /**
     * Validate that broker id exists in database
     *
     * @param brokerId broker id
     * @return broker entity
     */
    Broker validateAndGetBroker(final Integer brokerId);

    /**
     * Validate thath broker node id exists in database
     *
     * @param brokerNodeId broker node id
     * @return broker node entity
     */
    BrokerNode validateAndGetBrokerNode(final Integer brokerNodeId);

    /**
     * Validate broker operation
     *
     * @param brokerOperation broker operation to check
     * @return broker operation enumerate
     */
    BrokerOperation validateAndGetBrokerOperation(final String brokerOperation);

    /**
     * Validate broker node operation
     *
     * @param brokerNodeOperation broker node operation to check
     * @return broker operation enumerate
     */
    BrokerNodeOperation validateAndGetBrokerNodeOperation(final String brokerNodeOperation);


    /**
     * Get and validate a Generic Alert Config
     *
     * @param broker the broker
     * @param alertType alert type
     * @return the broker alert config
     */
    BrokerAlertConfig getAndValidateGenericAlertConfig(final Broker broker, final GenericBrokerAlertType alertType);

    /**
     * Get and validate a Queue Alert Config
     *
     * @param broker the broker
     * @param alertType alert type
     * @return the broker alert config
     */
    QueueBrokerAlertConfig getAndValidateQueueAlertConfig(final Broker broker, final QueueBrokerAlertType alertType);

    /**
     * Get and validate a Rate Threshold Alert Config
     *
     * @param broker the broker
     * @param alertType alert type
     * @return the broker alert config
     */
    RateThresholdBrokerAlertConfig getAndValidateRateThresholdAlertConfig(final Broker broker, final RateThresholdBrokerAlertType alertType);

    /**
     * Validate if broker dto is construct correctly
     *
     * @param brokerDTO input broker dto to validate
     * @return objets retrieved and validated from database after validation
     */
    BrokerValidatedObjects validateBrokerDTO(BrokerDTO brokerDTO);

    /**
     * Validate broker product
     *
     * @param productId product id
     * @return validated product
     */
    Product validateAndGetProduct(final Integer productId);

    /**
     * Validate broker can be stopped.
     *
     * @param broker the broker
     */
    void validateBrokerCanBeStopped(Broker broker);

    /**
     * Validate broker can be deleted.
     *
     * @param broker the broker
     */
    void validateBrokerCanBeDeleted(Broker broker);

    /**
     * Validate broker can be started.
     *
     * @param broker the broker
     */
    void validateBrokerCanBeStarted(Broker broker);

    /**
     * Validate broker node can be stopped.
     *
     * @param brokerNode the broker node
     */
    void validateBrokerNodeCanBeStopped(BrokerNode brokerNode);

    /**
     * Validate broker node can be started.
     *
     * @param brokerNode the broker node
     */
    void validateBrokerNodeCanBeStarted(BrokerNode brokerNode);

    /**
     * Validate that any broker node is not in transitory status like STARTING, STOPPING or RESTARTING
     *
     * @param broker the broker
     */
    void validateAnyNodeIsNotInTransitoryStatus(final Broker broker);

    /**
     * Get the list of valid number of nodes
     *
     * @param environment environment
     * @param isMonoCPD   true if product deployment infrastructure in production is Mono CPD
     * @return array with valid number of nodes
     */
    int[] getValidNumberOfNodes(Environment environment, boolean isMonoCPD);

    /**
     * Validate broker can be operable.
     * It means if we can make operations over brokers, or get information from their admin console.
     *
     * @param broker the broker
     */
    void validateBrokerCanBeOperable(final Broker broker);

    /**
     * Validate and get broker admin user broker user.
     *
     * @param broker the broker
     * @return the broker user
     */
    BrokerUser validateAndGetBrokerAdminUser(final Broker broker);

    /**
     * Validate broker alert configuration.
     *
     * @param brokerAlertConfigDTO the broker alert configuration
     * @param broker the broker
     */
    void validateBrokerAlertConfig(final BrokerAlertConfigDTO brokerAlertConfigDTO, final Broker broker);

    /**
     * Validate if the user can manage the broker
     * @param broker
     */
    void validateBrokerActionCanBeManagedByUser(final Broker broker, final String user);
}
