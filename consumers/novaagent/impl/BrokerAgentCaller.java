package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.client.jaxrs.rest.IRestHandlerBrokeragentapi;
import com.bbva.enoa.apirestgen.brokeragentapi.client.jaxrs.rest.impl.RestHandlerBrokeragentapi;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.consumers.autoconfig.NovaAgentClientProperties;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.Callable;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.CallableReturnable;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.INovaAgentCaller;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.client.exception.request.NovaApiClientRequestException;
import com.bbva.kltt.apirest.generator.lib.commons.client.exception.response.NovaApiClientResponseException;
import com.bbva.kltt.apirest.generator.lib.commons.client.exception.response.NovaApiClientResponseTimeoutException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The type Nova agent caller.
 */
@Service
@Slf4j
public class BrokerAgentCaller implements INovaAgentCaller<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerAgentCaller.class);
    /**
     * The constant BROKER_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL.
     */
    public static final String BROKER_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL = "[BrokerAgentCaller] -> [callOnEnvironment]: Error detail: ";
    /**
     * The constant FAILED_CALLING_TO_BROKER_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2.
     */
    public static final String FAILED_CALLING_TO_BROKER_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2 = "Failed calling to Broker Agent hostPort [{0}], environment [{1}]. Error: [{2}] ";


    private final BrokerAgentProvider brokerAgentProvider;
    private final NovaAgentClientProperties novaAgentClientProperties;
    private final NovaAgentUtils novaAgentUtils;

    /**
     * Instantiates a new Broker agent api client.
     *
     * @param brokerAgentProvider       the broker agent provider
     * @param novaAgentClientProperties the nova agent client properties
     * @param novaAgentUtils            the nova agent utils
     */
    @Autowired
    public BrokerAgentCaller(final BrokerAgentProvider brokerAgentProvider,
                             final NovaAgentClientProperties novaAgentClientProperties,
                             final NovaAgentUtils novaAgentUtils)
    {
        this.brokerAgentProvider = brokerAgentProvider;
        this.novaAgentClientProperties = novaAgentClientProperties;
        this.novaAgentUtils = novaAgentUtils;
    }

    /**
     * Call on Environment, Execute the callable input, over Nova agent with an jaxRs client on BrokersAgentAPI on the specific environment.
     *
     * @param environment    the environment
     * @param methodCallable the method callable
     * @throws NovaException the nova exception
     */
    public <R> R callOnEnvironmentWithReturn(final String environment, final CallableReturnable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi, R> methodCallable)
    {
        NovaAgentClientProperties.NovaAgentByEnvironment brokerAgentByEnvironment = this.novaAgentClientProperties.getNovaAgentByEnvironment(environment);
        Iterator<String> iterator = brokerAgentByEnvironment.getHostPort().iterator();
        boolean anySuccess = false;
        String lastExceptionMessage = "";
        NovaJaxRsConfigUtils novaConfig = new NovaJaxRsConfigUtils();
        R res = null;
        do
        {
            String hostPort = iterator.next();
            IRestHandlerBrokeragentapi restHandlerBrokeragentapi = this.brokerAgentProvider.getBrokerAgentRestHandler(hostPort);
            try
            {
                res = methodCallable.call(novaConfig, restHandlerBrokeragentapi);
                anySuccess = true;
            }
            catch (NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BROKER_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironmentWithReturn]: Unexpected NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException - {}", logMessage);
                log.debug(BROKER_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }
            catch (Exception e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BROKER_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironmentWithReturn]: Unexpected Exception {}", logMessage);
                log.debug(BROKER_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }
        }
        while (iterator.hasNext() && !anySuccess);

        if (!anySuccess)
        {
            try
            {
                ErrorMessage errorMessage = this.novaAgentUtils.getErrorMessageFromJson(lastExceptionMessage);
                throw new NovaException(BrokerError.getGenericNovaAgentError(errorMessage.getCode(), errorMessage.getMessage()));
            }
            catch (JsonSyntaxException e)
            {
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironmentWithReturn]: errorMessage [{}] can´t be transformed to Nova exception", lastExceptionMessage);
                throw new NovaException(BrokerError.getErrorCallingBrokerAgentOnEnvironment(environment, lastExceptionMessage), "There was an error trying to call Broker Agent or trying to execute operation over specific broker. Review the LOG traces of CoreService and NovaAgent Services.");
            }
        }
        return res;
    }

    /**
     * Call on Environment, Execute the callable input, over Nova agent with an jaxRs client on BrokersAgentAPI on the specific environment.
     *
     * @param environment    the environment
     * @param methodCallable the method callable
     * @throws NovaException the nova exception
     */
    public void callOnEnvironment(final String environment, final Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable)
    {
        NovaAgentClientProperties.NovaAgentByEnvironment brokerAgentByEnvironment = this.novaAgentClientProperties.getNovaAgentByEnvironment(environment);
        Iterator<String> iterator = brokerAgentByEnvironment.getHostPort().iterator();
        boolean anySuccess = false;
        String lastExceptionMessage = "";
        NovaJaxRsConfigUtils novaConfig = new NovaJaxRsConfigUtils();
        do
        {
            String hostPort = iterator.next();
            IRestHandlerBrokeragentapi restHandlerBrokeragentapi = this.brokerAgentProvider.getBrokerAgentRestHandler(hostPort);
            try
            {
                methodCallable.call(novaConfig, restHandlerBrokeragentapi);
                anySuccess = true;
            }
            catch (NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BROKER_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironment]: Unexpected NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException - {}", logMessage);
                log.debug(BROKER_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }
            catch (Exception e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BROKER_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironment]: Unexpected Exception {}", logMessage);
                log.debug(BROKER_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }

        }
        while (iterator.hasNext() && !anySuccess);

        if (!anySuccess)
        {
            try
            {
                ErrorMessage errorMessage = this.novaAgentUtils.getErrorMessageFromJson(lastExceptionMessage);
                throw new NovaException(BrokerError.getGenericNovaAgentError(errorMessage.getCode(), errorMessage.getMessage()));
            }
            catch (JsonSyntaxException e)
            {
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironment]: errorMessage [{}] can´t be transformed to Nova exception", lastExceptionMessage);
                throw new NovaException(BrokerError.getErrorCallingBrokerAgentOnEnvironment(environment, lastExceptionMessage), "There was an error trying to call Broker Agent or trying to execute operation over specific broker. Review the LOG traces of CoreService and NovaAgent Services.");
            }
        }
    }

    /**
     * The type Broker agent provider, this class is use to provide a RestHandlerBrokerAgentApi on a given host port in a synchronized way
     */
    @Component
    protected static class BrokerAgentProvider
    {

        private final Map<String, IRestHandlerBrokeragentapi> brokerAgentRestHandlerByHostPort = new HashMap<>();

        /**
         * Gets the BrokerAgent rest handler for the URL (host:port) indicated by the parameter
         * Creates a new rest handler if it does not exists previously into the broker agent handlers map
         *
         * @param hostPort The host:port to get the broker agent rest handler
         * @return a RestHandlerBrokeragentapi instance
         */
        public IRestHandlerBrokeragentapi getBrokerAgentRestHandler(final String hostPort)
        {

            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi = this.brokerAgentRestHandlerByHostPort.get(hostPort);
            if (iRestHandlerBrokeragentapi == null) // avoid unnecessary and expensive synchronized calls
            {
                synchronized (this.brokerAgentRestHandlerByHostPort)
                {
                    iRestHandlerBrokeragentapi = this.brokerAgentRestHandlerByHostPort.computeIfAbsent(hostPort, k -> new RestHandlerBrokeragentapi(k, NovaJaxRsConfigUtils.DEFAULT_CLIENT_TIMEOUT));
                }
            }

            return iRestHandlerBrokeragentapi;
        }
    }


}
