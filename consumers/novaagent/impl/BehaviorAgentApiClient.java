package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.enoa.apirestgen.behavioragentapi.model.BABehaviorExecutionDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.IBehaviorAgentApiClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BehaviorAgentApiClient implements IBehaviorAgentApiClient
{

    private final BehaviorAgentCaller behaviorNovaAgentCaller;

    @Autowired
    public BehaviorAgentApiClient(final BehaviorAgentCaller behaviorNovaAgentCaller)
    {
        this.behaviorNovaAgentCaller = behaviorNovaAgentCaller;
    }

    @Override
    public void stopBehaviorInstance(Integer behaviorInstanceId)
    {
        // The environment for behavior services are always PRE
        behaviorNovaAgentCaller.callOnEnvironment(
                Environment.PRE.name(),
                (novaConfig, iRestHandlerBehavioragentapi) ->
                        iRestHandlerBehavioragentapi.stopBehaviorInstance(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), behaviorInstanceId));
    }

    @Override
    public BABehaviorExecutionDTO[] getBehaviorExecutionByBehaviorConfigurationList(int[] behaviorConfigurationId)
    {
        return behaviorNovaAgentCaller.callOnEnvironmentWithReturn(
                Environment.PRE.name(),
                (novaConfig, iRestHandlerBehavioragentapi) ->
                        iRestHandlerBehavioragentapi.getBehaviorExecutionByBehaviorConfigurationList(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), behaviorConfigurationId));
    }

    @Override
    public boolean removeBehaviorInstanceExecution(int behaviorInstanceId) {
        return behaviorNovaAgentCaller.callOnEnvironmentWithReturn(
                Environment.PRE.name(),
                (novaConfig, iRestHandlerBehavioragentapi) ->
                        iRestHandlerBehavioragentapi.removeBehaviorInstanceExecution(novaConfig.buildRequestConfig(), novaConfig.buildMetadata(), behaviorInstanceId));
    }

}
