package com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces;

import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertConfigDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerAlertInfoDTO;

/**
 * The interface Filesystems alert service.
 */
public interface IBrokerAlertService
{
    /**
     * Update broker alert configuration.
     *
     * @param ivUser               the iv user
     * @param brokerId             the broker id
     * @param brokerAlertConfigDTO the broker alert configuration dto
     */
    void updateBrokerAlertConfiguration(String ivUser, Integer brokerId, BrokerAlertConfigDTO brokerAlertConfigDTO);

    /**
     *  Get open alerts for all the brokers of a product
     *
     * @param productId product id
     * @return list of open alerts
     */
    BrokerAlertInfoDTO[] getBrokerAlertsByProduct(Integer productId);
}
