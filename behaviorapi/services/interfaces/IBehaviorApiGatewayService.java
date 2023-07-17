package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;

/**
 * Api Gateway Service Integration Interface
 */
public interface IBehaviorApiGatewayService
{
    /**
     * For a behavior test in PRE environment, generates the docker key for each configuration
     *
     * @param behaviorServiceConfiguration behavior service configuration
     */
    void generateDockerKey(final BehaviorServiceConfiguration behaviorServiceConfiguration);
}
