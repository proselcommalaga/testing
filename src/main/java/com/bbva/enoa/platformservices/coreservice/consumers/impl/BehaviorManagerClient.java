package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.behaviormanagerapi.client.feign.nova.rest.IRestHandlerBehaviormanagerapi;
import com.bbva.enoa.apirestgen.behaviormanagerapi.client.feign.nova.rest.IRestListenerBehaviormanagerapi;
import com.bbva.enoa.apirestgen.behaviormanagerapi.client.feign.nova.rest.impl.RestHandlerBehaviormanagerapi;
import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMBehaviorParamsDTO;
import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMDockerContainerResponse;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBehaviorManagerClient;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Deployment manager client
 */
@Slf4j
@Service
public class BehaviorManagerClient implements IBehaviorManagerClient
{

    @Autowired
    private IRestHandlerBehaviormanagerapi iRestHandlerBehaviormanagerapi;

    /**
     * Callback service
     */
    @Autowired
    private CallbackService callbackService;

    /**
     * API services.
     */
    private RestHandlerBehaviormanagerapi restHandlerBehaviormanagerapi;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerBehaviormanagerapi = new RestHandlerBehaviormanagerapi(this.iRestHandlerBehaviormanagerapi);
    }

    @Override
    public void startBehaviorConfiguration(final BMBehaviorParamsDTO behaviorParamsDTO, final Integer bsConfigurationId)
    {
        log.debug("[{}}] -> [startInstance]: Calling Behavior Manager to start behavior service configuration with id {} and params: [{}]",
                this.getClass().getSimpleName(), bsConfigurationId, behaviorParamsDTO);

        behaviorParamsDTO.setResponse(this.callbackService.buildBehaviorCallback(CallbackService.CALLBACK_INSTANCE + bsConfigurationId + CallbackService.START,
                CallbackService.CALLBACK_INSTANCE + bsConfigurationId + CallbackService.START_ERROR));

        this.restHandlerBehaviormanagerapi.startBehaviorConfiguration(new IRestListenerBehaviormanagerapi()
        {
            @Override
            public void startBehaviorConfiguration()
            {
                log.debug("[{}}] -> [startBehaviorConfiguration]: launched starting configuration {}", this.getClass().getSimpleName(), bsConfigurationId);
            }

            @Override
            public void startBehaviorConfigurationErrors(final Errors outcome)
            {
                log.error("[{}] -> [startBehaviorConfiguration]: error starting configuration: {} errorMessage: [{}]", this.getClass().getSimpleName(), bsConfigurationId, outcome.getFirstErrorMessage());

            }
        }, behaviorParamsDTO, bsConfigurationId);
    }

    @Override
    public void stopBehaviorConfiguration(final Integer behaviorInstanceId)
    {
        log.debug("[{}}] -> [stopBehaviorConfiguration]: Calling Behavior Manager to stop a behavior test with behavior instance id {}",
                this.getClass().getSimpleName(), behaviorInstanceId);

        this.callbackService.buildBehaviorCallback(CallbackService.CALLBACK_INSTANCE + behaviorInstanceId + CallbackService.STOP,
                CallbackService.CALLBACK_INSTANCE + behaviorInstanceId + CallbackService.STOP_ERROR);

        this.restHandlerBehaviormanagerapi.stopBehaviorInstance(new IRestListenerBehaviormanagerapi()
        {
            @Override
            public void stopBehaviorInstance(final BMDockerContainerResponse outcome)
            {
                log.debug("[{}] -> [stopBehaviorInstance]: Launched stopping instance {}", this.getClass().getSimpleName(), behaviorInstanceId);

            }

            @Override
            public void stopBehaviorInstanceErrors(final Errors outcome)
            {
                log.error("[{}] -> [stopBehaviorInstance]: Error stopping instance: {} errorMessage: [{}]", this.getClass().getSimpleName(), behaviorInstanceId, outcome.getFirstErrorMessage());
            }
        }, behaviorInstanceId);
    }

}
