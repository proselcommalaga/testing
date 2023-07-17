package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

public interface ILogsClient
{
    /**
     * Creates log rate threshold events for the three environments (INT, PRE and PRO)
     *
     * @param productId product id
     * @param email     product email
     */
    void createLogRateThresholdEvents(Integer productId, String email);

    /**
     * Deletes log rate threshold events for the three environments (INT, PRE and PRO)
     *
     * @param productId product id
     */
    void deleteLogRateThresholdEvents(Integer productId);

    /**
     * Deletes log events by product
     *
     * @param productId product id
     */
    void deleteLogEvents(Integer productId);

}
