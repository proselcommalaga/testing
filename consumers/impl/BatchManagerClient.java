package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.batchmanagerapi.client.feign.nova.rest.IRestHandlerBatchmanagerapi;
import com.bbva.enoa.apirestgen.batchmanagerapi.client.feign.nova.rest.IRestListenerBatchmanagerapi;
import com.bbva.enoa.apirestgen.batchmanagerapi.client.feign.nova.rest.impl.RestHandlerBatchmanagerapi;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.RunningBatchs;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.SpecificInstance;
import com.bbva.enoa.core.novabootstarter.consumers.ApiClientResponseWrapper;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IBatchManagerClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Optional;

@Service
public class BatchManagerClient implements IBatchManagerClient
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BatchManagerClient.class);

    /**
     * Rest interface
     */
    private final IRestHandlerBatchmanagerapi iRestHandlerBatchmanagerapi;

    /**
     * API service
     */
    private RestHandlerBatchmanagerapi restHandlerBatchmanagerapi;


    @Autowired
    public BatchManagerClient(IRestHandlerBatchmanagerapi iRestHandlerBatchmanagerapi)
    {
        this.iRestHandlerBatchmanagerapi = iRestHandlerBatchmanagerapi;
    }

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerBatchmanagerapi = new RestHandlerBatchmanagerapi(this.iRestHandlerBatchmanagerapi);
    }

    ////////////////////////////////////////////////// IMPLEMENTATIONS /////////////////////////////////////////////////////

    @Override
    public DeploymentServiceBatchStatus[] getRunningBatchDeploymentServices(int[] deploymentServiceIdList, String environment)
    {
        LOG.debug("[BatchManagerClient] -> [getRunningBatchDeploymentServices]: getting a batch deployment service status (running or not) from deployment service id list: [{}] into environment: [{}]",
                deploymentServiceIdList, environment);

        SingleApiClientResponseWrapper<DeploymentServiceBatchStatus[]> singleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();


        // Call Batchmanagerapi client
        this.restHandlerBatchmanagerapi.getRunningBatchDeploymentServices(new IRestListenerBatchmanagerapi()
        {
            // Successful call
            @Override
            public void getRunningBatchDeploymentServices(final DeploymentServiceBatchStatus[] outcome)
            {
                singleApiClientResponseWrapper.set(outcome);
                LOG.debug("[BatchManagerClient] -> [getRunningBatchDeploymentServices]: obtained a batch deployment service status (running or not) from deployment service id list: [{}] into environment: [{}] - Result: [{}]",
                        deploymentServiceIdList, environment, outcome);
            }

            @Override
            public void getRunningBatchDeploymentServicesErrors(final Errors error)
            {
                singleApiClientResponseWrapper.set(new DeploymentServiceBatchStatus[0]);
                LOG.error("[BatchManagerClient] -> [getRunningBatchDeploymentServices]: there was an error trying to get a batch deployment service status (running or not) from deployment service id list: [{}] into environment: [{}] - Error message: [{}]",
                        deploymentServiceIdList, environment, error.getBodyExceptionMessage());
            }
        }, deploymentServiceIdList, environment);

        return singleApiClientResponseWrapper.get();
    }

    @Override
    public RunningBatchs getRunningInstances(final Long deploymentServiceId)
    {
        LOG.debug("[BatchManagerClient] -> [getRunningInstances]: getting a batch deployment service status (running or not) from deployment service id: [{}]", deploymentServiceId);

        SingleApiClientResponseWrapper<RunningBatchs> singleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();

        // Call Batchmanagerapi client
        this.restHandlerBatchmanagerapi.getRunningInstances(new IRestListenerBatchmanagerapi()
        {
            // Successful call
            @Override
            public void getRunningInstances(final RunningBatchs outcome)
            {
                singleApiClientResponseWrapper.set(outcome);
                LOG.debug("[BatchManagerClient] -> [getRunningInstances]: obtained a batch deployment service status (running or not) from deployment service id: [{}]. Result: [{}]", deploymentServiceId, outcome);
            }

            @Override
            public void getRunningInstancesErrors(final Errors error)
            {
                singleApiClientResponseWrapper.set(new RunningBatchs());
                LOG.error("[BatchManagerClient] -> [getRunningInstances]: there was an error trying to get a batch deployment service status (running or not) from deployment service id: [{}]. Error message: [{}]",
                        deploymentServiceId, error.getBodyExceptionMessage());
            }
        }, deploymentServiceId);

        return singleApiClientResponseWrapper.get();
    }

    @Override
    public Integer getRunningInstancesByDeploymentPlan(final Integer deploymentPlanId)
    {
        LOG.debug("[BatchManagerClient] -> [getRunningInstancesByDeploymentPlan]: getting a batch deployment service status (running) from deployment plan id: [{}]", deploymentPlanId);

        SingleApiClientResponseWrapper<Integer> singleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();

        // Call Batchmanagerapi client
        this.restHandlerBatchmanagerapi.getRunningInstancesByDeploymentPlan(new IRestListenerBatchmanagerapi()
        {
            // Successful call
            @Override
            public void getRunningInstancesByDeploymentPlan(final Integer outcome)
            {
                singleApiClientResponseWrapper.set(outcome);
                LOG.debug("[BatchManagerClient] -> [getRunningInstancesByDeploymentPlan]: obtained a running batch from deployment plan id: [{}]. Result: [{}]", deploymentPlanId, outcome);
            }

            @Override
            public void getRunningInstancesByDeploymentPlanErrors(final Errors error)
            {
                singleApiClientResponseWrapper.set(0);
                LOG.error("[BatchManagerClient] -> [getRunningInstancesByDeploymentPlan]: there was an error trying to get a running batch deployment service status from deployment plan id: [{}]. Error message: [{}]",
                        deploymentPlanId, error.getBodyExceptionMessage());
            }
        }, deploymentPlanId);

        return singleApiClientResponseWrapper.get();
    }

    @Override
    public BatchManagerBatchExecutionsSummaryDTO getBatchExecutionsSummary(String environment, long[] deploymentServiceIds, String uuaa, String platform, String origin)
    {
        LOG.debug("[BatchManagerClient] -> [getBatchExecutionInstances]: getting ended batch execution instances from environment: [{}], deployment service ids: {}, uuaa: [{}], platform: [{}], origin: [{}]", environment, deploymentServiceIds, uuaa, platform, origin);
        SingleApiClientResponseWrapper<BatchManagerBatchExecutionsSummaryDTO> singleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();

        // Call Batchmanagerapi clientgetBatchExecutionsSummary
        this.restHandlerBatchmanagerapi.getBatchExecutionsSummary(new IRestListenerBatchmanagerapi()
        {
            // Successful call
            @Override
            public void getBatchExecutionsSummary(final BatchManagerBatchExecutionsSummaryDTO outcome)
            {
                singleApiClientResponseWrapper.set(outcome);
                LOG.debug("[BatchManagerClient] -> [getBatchExecutionInstances]: obtained ended batch instances from environment: [{}], deployment service ids: {}, uuaa: [{}], platform: [{}], origin: [{}]. Result: [{}]",
                        environment, deploymentServiceIds, uuaa, platform, origin, outcome);
            }

            @Override
            public void getBatchExecutionsSummaryErrors(final Errors error)
            {
                singleApiClientResponseWrapper.set(new BatchManagerBatchExecutionsSummaryDTO());
                LOG.error("[BatchManagerClient] -> [getBatchExecutionInstances]: there was an error trying to get a running batch deployment service status from environment: [{}], deployment service ids: {}, uuaa: [{}], platform: [{}], origin: [{}]. Error message: [{}]",
                        environment, deploymentServiceIds, uuaa, platform, origin, error.getBodyExceptionMessage());
            }
        }, environment, deploymentServiceIds, uuaa, platform, origin);

        return singleApiClientResponseWrapper.get();
    }

    @Override
    public String[] getUuaasEphoenixLegacy()
    {
        LOG.debug("[BatchManagerClient] -> [getUuaasEphoenixLegacy]: getting uuaas Ephoenix Legacy");
        SingleApiClientResponseWrapper<String[]> singleApiClientResponseWrapper = new SingleApiClientResponseWrapper<>();

        // Call Batchmanagerapi client
        this.restHandlerBatchmanagerapi.getUuaasEphoenixLegacy(new IRestListenerBatchmanagerapi()
        {
            // Successful call
            @Override
            public void getUuaasEphoenixLegacy(final String[] outcome)
            {
                singleApiClientResponseWrapper.set(outcome);
                LOG.debug("[BatchManagerClient] -> [getUuaasEphoenixLegacy]: obtained uuaas Ephoenix Legacy. Result: [{}]", outcome.length);
            }

            @Override
            public void getUuaasEphoenixLegacyErrors(final Errors error)
            {
                singleApiClientResponseWrapper.set(new String[0]);
                LOG.error("[BatchManagerClient] -> [getUuaasEphoenixLegacy]: there was an error trying to get uuaas Ephoenix Legacy. Error message: [{}]", error.getBodyExceptionMessage());
            }
        });

        return singleApiClientResponseWrapper.get();
    }

    @Override
    public SpecificInstance getInstanceById(final Integer batchId, final String environment) throws NovaException
    {
        LOG.info("[BatchManagerClient] -> [getInstanceById]: calling Batch Manager to get info for batch id: [{}]", batchId);

        var apiClientResponseWrapper = new ApiClientResponseWrapper<SpecificInstance, Errors>();

        // requesting to the batch manager service for the specific instance
        this.restHandlerBatchmanagerapi.getInstanceById(new IRestListenerBatchmanagerapi()
        {

            @Override
            public void getInstanceById(final SpecificInstance outcome)
            {
                apiClientResponseWrapper.setValue(outcome);
            }

            @Override
            public void getInstanceByIdErrors(final Errors exception)
            {
                apiClientResponseWrapper.setError(exception);
            }

        }, Integer.toUnsignedLong(batchId), environment);

        // checking if there are any error associated with the request
        Optional.ofNullable(apiClientResponseWrapper.getError()).ifPresent(errors -> {
            LOG.error("[BatchManagerClient] -> [getInstanceById]: there was an error trying to get the specific instance associated with the batch [{}]. Error message: [{}]", batchId, errors.getBodyExceptionMessage());
            apiClientResponseWrapper.setValue(null);
        });

        // returning the value if everything is ok
        return apiClientResponseWrapper.getValue();
    }
}
