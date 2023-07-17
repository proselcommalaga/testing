package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.brokerdeploymentapi.client.feign.nova.rest.IRestHandlerBrokerdeploymentapi;
import com.bbva.enoa.apirestgen.brokerdeploymentapi.client.feign.nova.rest.IRestListenerBrokerdeploymentapi;
import com.bbva.enoa.apirestgen.brokerdeploymentapi.client.feign.nova.rest.impl.RestHandlerBrokerdeploymentapi;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBrokerDeploymentClient;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Broker Deployment API client
 */
@Service
public class BrokerDeploymentClientImpl implements IBrokerDeploymentClient
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerDeploymentClientImpl.class);

    /**
     * API services
     */
    private final IRestHandlerBrokerdeploymentapi iRestHandlerBrokerdeploymentapi;

    /**
     * Handler
     */
    private RestHandlerBrokerdeploymentapi restHandlerBrokerdeploymentapi;

    /**
     * Constructor
     *
     * @param iRestHandlerBrokerdeploymentapi   rest handler interface
     */
    @Autowired
    public BrokerDeploymentClientImpl(final IRestHandlerBrokerdeploymentapi iRestHandlerBrokerdeploymentapi)
    {
        this.iRestHandlerBrokerdeploymentapi = iRestHandlerBrokerdeploymentapi;
    }

    /**
     * Init
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerBrokerdeploymentapi = new RestHandlerBrokerdeploymentapi(this.iRestHandlerBrokerdeploymentapi);
    }

    @Override
    public boolean createBroker(Integer brokerId)
    {
        LOG.debug("[{}] -> [createBroker]: Calling for brokerId [{}]", this.getClass().getSimpleName(), brokerId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.createBroker(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void createBroker()
            {
                LOG.debug("[{}] -> [createBroker]: Called OK for brokerId [{}] in async way", this.getClass().getSimpleName(), brokerId);
                response.set(true);
            }

            @Override
            public void createBrokerErrors(Errors outcome)
            {
                LOG.error("[{}] -> [createBroker]: Error occurred calling for brokerId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerId);

        return response.get();
    }

    @Override
    public boolean deleteBroker(Integer brokerId)
    {
        LOG.debug("[{}] -> [deleteBroker]: Calling for brokerId [{}]", this.getClass().getSimpleName(), brokerId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.deleteBroker(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void deleteBroker()
            {
                LOG.debug("[{}] -> [deleteBroker]: Called OK for brokerId [{}] in async way", this.getClass().getSimpleName(), brokerId);
                response.set(true);
            }

            @Override
            public void deleteBrokerErrors(Errors outcome)
            {
                LOG.error("[{}] -> [deleteBroker]: Error occurred calling for brokerId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerId);

        return response.get();
    }

    @Override
    public boolean startBroker(Integer brokerId)
    {
        LOG.debug("[{}] -> [startBroker]: Calling for brokerId [{}]", this.getClass().getSimpleName(), brokerId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.startBroker(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void startBroker()
            {
                LOG.debug("[{}] -> [startBroker]: Called OK for brokerId [{}] in async way", this.getClass().getSimpleName(), brokerId);
                response.set(true);
            }

            @Override
            public void startBrokerErrors(Errors outcome)
            {
                LOG.error("[{}] -> [startBroker]: Error occurred calling for brokerId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerId);

        return response.get();
    }

    @Override
    public boolean stopBroker(Integer brokerId)
    {
        LOG.debug("[{}] -> [stopBroker]: Calling for brokerId [{}]", this.getClass().getSimpleName(), brokerId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.stopBroker(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void stopBroker()
            {
                LOG.debug("[{}] -> [stopBroker]: Called OK for brokerId [{}] in async way", this.getClass().getSimpleName(), brokerId);
                response.set(true);
            }

            @Override
            public void stopBrokerErrors(Errors outcome)
            {
                LOG.error("[{}] -> [stopBroker]: Error occurred calling for brokerId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerId);

        return response.get();
    }

    @Override
    public boolean restartBroker(Integer brokerId)
    {
        LOG.debug("[{}] -> [restartBroker]: Calling for brokerId [{}]", this.getClass().getSimpleName(), brokerId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.restartBroker(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void restartBroker()
            {
                LOG.debug("[{}] -> [restartBroker]: Called OK for brokerId [{}] in async way", this.getClass().getSimpleName(), brokerId);
                response.set(true);
            }

            @Override
            public void restartBrokerErrors(Errors outcome)
            {
                LOG.error("[{}] -> [restartBroker]: Error occurred calling for brokerId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerId);

        return response.get();
    }

    @Override
    public boolean startBrokerNode(Integer brokerNodeId)
    {
        LOG.debug("[{}] -> [startBrokerNode]: Calling for brokerNodeId [{}]", this.getClass().getSimpleName(), brokerNodeId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.startBrokerNode(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void startBrokerNode()
            {
                LOG.debug("[{}] -> [startBrokerNode]: Called OK for brokerNodeId [{}] in async way", this.getClass().getSimpleName(), brokerNodeId);
                response.set(true);
            }

            @Override
            public void startBrokerNodeErrors(Errors outcome)
            {
                LOG.error("[{}] -> [startBrokerNode]: Error occurred calling for brokerNodeId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerNodeId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerNodeId);

        return response.get();
    }

    @Override
    public boolean stopBrokerNode(Integer brokerNodeId)
    {
        LOG.debug("[{}] -> [stopBrokerNode]: Calling for brokerNodeId [{}]", this.getClass().getSimpleName(), brokerNodeId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.stopBrokerNode(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void stopBrokerNode()
            {
                LOG.debug("[{}] -> [stopBrokerNode]: Called OK for brokerNodeId [{}] in async way", this.getClass().getSimpleName(), brokerNodeId);
                response.set(true);
            }

            @Override
            public void stopBrokerNodeErrors(Errors outcome)
            {
                LOG.error("[{}] -> [stopBrokerNode]: Error occurred calling for brokerNodeId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerNodeId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerNodeId);

        return response.get();
    }

    @Override
    public boolean restartBrokerNode(Integer brokerNodeId)
    {
        LOG.debug("[{}] -> [restartBrokerNode]: Calling for brokerNodeId [{}]", this.getClass().getSimpleName(), brokerNodeId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerBrokerdeploymentapi.restartBrokerNode(new IRestListenerBrokerdeploymentapi()
        {
            @Override
            public void restartBrokerNode()
            {
                LOG.debug("[{}] -> [restartBrokerNode]: Called OK for brokerNodeId [{}] in async way", this.getClass().getSimpleName(), brokerNodeId);
                response.set(true);
            }

            @Override
            public void restartBrokerNodeErrors(Errors outcome)
            {
                LOG.error("[{}] -> [restartBrokerNode]: Error occurred calling for brokerNodeId [{}]. Error message: [{}]",
                        this.getClass().getSimpleName(), brokerNodeId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, brokerNodeId);

        return response.get();
    }
}
