package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces;


import com.bbva.enoa.apirestgen.behavioragentapi.model.BABehaviorExecutionDTO;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;

/**
 * The interface Behavior agent api client.
 */
public interface IBehaviorAgentApiClient
{

    /**
     * Stop behavior instance.
     *
     * @param behaviorInstanceId the behavior instance id
     * @throws Errors the errors
     */
    void stopBehaviorInstance(Integer behaviorInstanceId);

    /**
     * Gets behavior execution by behavior service configuration list.
     *
     * @param behaviorConfigurationId the behavior configuration id
     * @return the behavior execution by behavior configuration list
     */
    BABehaviorExecutionDTO[] getBehaviorExecutionByBehaviorConfigurationList(int[] behaviorConfigurationId);

    /**
     * Remove a corresponding behavior execution for a behavior instance.
     *
     * @param behaviorInstanceId the behavior instance id
     * @return if a behavior execution was removed or not
     */
    boolean removeBehaviorInstanceExecution(int behaviorInstanceId);
}
