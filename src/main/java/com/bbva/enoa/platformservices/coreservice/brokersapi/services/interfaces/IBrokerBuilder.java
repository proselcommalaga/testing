package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokeragentapi.model.ConnectionDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;

import java.util.List;

/**
 * Broker builder Service
 */
public interface IBrokerBuilder
{
    /**
     * Build a broker entity from a brokerDTO well-formed
     *
     * @param brokerDTO input dto
     * @return well formed broker entity
     */
    Broker validateAndBuildBrokerEntity(BrokerDTO brokerDTO);

    /**
     * Build a broker dto from a broker entity
     *
     * @param broker broker entity
     * @return broker dto
     */
    BrokerDTO buildBasicBrokerDTOFromEntity(Broker broker);

    /**
     * Build a broker dto from a broker entity without monitoringURL
     *
     * @param broker broker entity
     * @return broker dto without monitoring url
     */
    BrokerDTO buildBasicBrokerDTOFromEntityWithoutMonitoringURL(Broker broker);

    /**
     * Build a broker dto from a broker entity with full information (nodes, users, etc.)
     *
     * @param ivUser ivUser
     * @param broker broker entity
     * @return broker dto with full information
     */
    BrokerDTO buildBrokerDTOFromEntity(String ivUser, Broker broker);

    /**
     * Build connection dto from broker user and broker list.
     *
     * @param brokerUser     the broker user
     * @param brokerNodeList the broker node list
     * @return the connection dto
     */
    ConnectionDTO buildConnectionDTOFromBrokerUserAndBrokerList(BrokerUser brokerUser, List<BrokerNode> brokerNodeList);

}
