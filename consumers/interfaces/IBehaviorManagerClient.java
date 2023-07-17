package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMBehaviorParamsDTO;

public interface IBehaviorManagerClient
{
    /**
     * Init de client
     */
    void init();

    /**
     * Start a behavior test execution
     *
     * @param behaviorParamsDTO tags and release version to test
     * @param bsConfigurationId behavior service configuration
     */
    void startBehaviorConfiguration(final BMBehaviorParamsDTO behaviorParamsDTO, final Integer bsConfigurationId);

    /**
     * Stop a behavior test execution
     *
     * @param behaviorInstanceId a behavior instance configuration identifier
     */
    void stopBehaviorConfiguration(final Integer behaviorInstanceId);

}
