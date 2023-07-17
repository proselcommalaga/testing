package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.logsapi.client.feign.nova.rest.IRestHandlerLogsapi;
import com.bbva.enoa.apirestgen.logsapi.client.feign.nova.rest.IRestListenerLogsapi;
import com.bbva.enoa.apirestgen.logsapi.client.feign.nova.rest.impl.RestHandlerLogsapi;
import com.bbva.enoa.apirestgen.logsapi.model.LogRateThresholdEventInitialValues;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILogsClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class LogsClient implements ILogsClient
{

    private static final Logger LOG = LoggerFactory.getLogger(LogsClient.class);

    /**
     * Logs rest handler interface
     */
    @Autowired
    private IRestHandlerLogsapi iRestHandlerLogsapi;

    /**
     * Logs rest handler
     */
    private RestHandlerLogsapi restHandlerLogsapi;

    /**
     * Initialize the rest handler
     */
    @PostConstruct
    public void initRestHandler()
    {
        this.restHandlerLogsapi = new RestHandlerLogsapi(this.iRestHandlerLogsapi);
    }

    @Override
    public void createLogRateThresholdEvents(Integer productId, String email)
    {
        LOG.debug("[LogsClient] -> [createLogRateThresholdEvents]: creating log rate threshold events for product [{}] with email [{}]", productId, email);

        LogRateThresholdEventInitialValues initialValues = new LogRateThresholdEventInitialValues();
        initialValues.setEmail(email);

        this.restHandlerLogsapi.createLogRateThresholdEvents(new IRestListenerLogsapi()
        {
            @Override
            public void createLogRateThresholdEvents()
            {
                LOG.debug("[LogsClient] -> [createLogRateThresholdEvents]: created log rate threshold events for product [{}]", productId);
            }

            @Override
            public void createLogRateThresholdEventsErrors(Errors outcome)
            {
                throw new NovaException(ProductsAPIError.getCreatingLogRateThresholdEventsError());
            }
        }, initialValues, productId);
    }

    @Override
    public void deleteLogRateThresholdEvents(Integer productId)
    {
        LOG.debug("[LogsClient] -> [deleteLogRateThresholdEvents]: deleting log rate threshold events for product [{}]", productId);

        this.restHandlerLogsapi.deleteLogRateThresholdEvents(new IRestListenerLogsapi()
        {
            @Override
            public void deleteLogRateThresholdEvents()
            {
                LOG.debug("[LogsClient] -> [deleteLogRateThresholdEvents]: deleting log rate threshold events for product [{}]", productId);
            }

            @Override
            public void deleteLogRateThresholdEventsErrors(Errors outcome)
            {
                throw new NovaException(ProductsAPIError.getDeletingLogRateThresholdEventsError());
            }
        }, productId);
    }

    @Override
    public void deleteLogEvents(final Integer productId)
    {
        LOG.debug("[LogsClient] -> [deleteLogEvents]: deleting log events for product [{}]", productId);

        this.restHandlerLogsapi.deleteLogEvents(new IRestListenerLogsapi()
        {
            @Override
            public void deleteLogEvents()
            {
                LOG.debug("[LogsClient] -> [deleteLogEvents]: deleting log events for product [{}]", productId);
            }

            @Override
            public void deleteLogEventsErrors(Errors errors)
            {
                throw new NovaException(ProductsAPIError.getDeletingLogEventsError());
            }
        }, productId);


    }
}
