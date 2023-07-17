package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.enoa.apirestgen.behavioragentapi.client.jaxrs.rest.IRestHandlerBehavioragentapi;
import com.bbva.enoa.apirestgen.behavioragentapi.client.jaxrs.rest.impl.RestHandlerBehavioragentapi;
import com.bbva.enoa.apirestgen.brokeragentapi.client.jaxrs.rest.IRestHandlerBrokeragentapi;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
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
public class BehaviorAgentCaller implements INovaAgentCaller<NovaJaxRsConfigUtils, IRestHandlerBehavioragentapi>
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(BehaviorAgentCaller.class);
    /**
     * The constant BEHAVIOR_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL.
     */
    public static final String BEHAVIOR_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL = "[BehaviorAgentCaller] -> [callOnEnvironment]: Error detail: ";
    /**
     * The constant FAILED_CALLING_TO_BEHAVIOR_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2.
     */
    public static final String FAILED_CALLING_TO_BEHAVIOR_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2 = "Failed calling to Behavior Agent hostPort [{0}], environment [{1}]. Error: [{2}] ";


    private final BehaviorAgentProvider behaviorAgentProvider;
    private final NovaAgentClientProperties novaAgentClientProperties;
    private final NovaAgentUtils novaAgentUtils;


    @Autowired
    public BehaviorAgentCaller(final BehaviorAgentProvider behaviorAgentProvider,
                               final NovaAgentClientProperties novaAgentClientProperties,
                               final NovaAgentUtils novaAgentUtils)
    {
        this.behaviorAgentProvider = behaviorAgentProvider;
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
    public <R> R callOnEnvironmentWithReturn(final String environment, final CallableReturnable<NovaJaxRsConfigUtils, IRestHandlerBehavioragentapi, R> methodCallable)
    {

        // ALWAYS PRE ENVIRONMENT NEEDED
        NovaAgentClientProperties.NovaAgentByEnvironment behaviorAgentByEnvironment = this.novaAgentClientProperties.getNovaAgentByEnvironment(environment);
        Iterator<String> iterator = behaviorAgentByEnvironment.getHostPort().iterator();
        boolean anySuccess = false;
        String lastExceptionMessage = "";
        NovaJaxRsConfigUtils novaConfig = new NovaJaxRsConfigUtils();
        R res = null;
        do
        {
            String hostPort = iterator.next();
            IRestHandlerBehavioragentapi restHandlerBehavioragentapi = this.behaviorAgentProvider.getBehaviorAgentRestHandler(hostPort);
            try
            {
                res = methodCallable.call(novaConfig, restHandlerBehavioragentapi);
                anySuccess = true;
            }
            catch (NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BEHAVIOR_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironmentWithReturn]: Unexpected NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException - {}", logMessage);
                log.debug(BEHAVIOR_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }
            catch (Exception e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BEHAVIOR_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BrokerAgentCaller] -> [callOnEnvironmentWithReturn]: Unexpected Exception {}", logMessage);
                log.debug(BEHAVIOR_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
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
    public void callOnEnvironment(final String environment, final Callable<NovaJaxRsConfigUtils, IRestHandlerBehavioragentapi> methodCallable)
    {
        NovaAgentClientProperties.NovaAgentByEnvironment behaviorAgentByEnvironment = this.novaAgentClientProperties.getNovaAgentByEnvironment(environment);
        Iterator<String> iterator = behaviorAgentByEnvironment.getHostPort().iterator();
        boolean anySuccess = false;
        String lastExceptionMessage = "";
        NovaJaxRsConfigUtils novaConfig = new NovaJaxRsConfigUtils();
        do
        {
            String hostPort = iterator.next();
            IRestHandlerBehavioragentapi restHandlerBehavioragentapi = this.behaviorAgentProvider.getBehaviorAgentRestHandler(hostPort);
            try
            {
                methodCallable.call(novaConfig, restHandlerBehavioragentapi);
                anySuccess = true;
            }
            catch (NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BEHAVIOR_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BehaviorAgentCaller] -> [callOnEnvironment]: Unexpected NovaApiClientResponseTimeoutException | NovaApiClientRequestException | NovaApiClientResponseException - {}",
                        logMessage);
                log.debug(BEHAVIOR_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }
            catch (Exception e)
            {
                lastExceptionMessage = e.getMessage();
                String logMessage = MessageFormat.format(FAILED_CALLING_TO_BEHAVIOR_AGENT_HOST_PORT_0_ENVIRONMENT_1_ERROR_2, hostPort, environment, e.getMessage());
                LOG.warn("[BehaviorAgentCaller] -> [callOnEnvironment]: Unexpected Exception {}", logMessage);
                log.debug(BEHAVIOR_AGENT_CALLER_CALL_ON_ENVIRONMENT_ERROR_DETAIL, e);
            }

        }
        while (iterator.hasNext() && !anySuccess);

        if (!anySuccess)
        {
            try
            {
                ErrorMessage errorMessage = this.novaAgentUtils.getErrorMessageFromJson(lastExceptionMessage);
                throw new NovaException(BehaviorError.getBehaviorGenericNovaAgentError(errorMessage.getCode(), errorMessage.getMessage()));
            }
            catch (JsonSyntaxException e)
            {
                LOG.warn("[BehaviorAgentCaller] -> [callOnEnvironment]: errorMessage [{}] can´t be transformed to Nova exception", lastExceptionMessage);
                throw new NovaException(BrokerError.getErrorCallingBrokerAgentOnEnvironment(environment, lastExceptionMessage), "There was an error trying to call Behavior Agent or trying to " +
                        "execute operation over specific broker. Review the LOG traces of CoreService and NovaAgent Services.");
            }
        }
    }

    /**
     * The type Broker agent provider, this class is use to provide a RestHandlerBrokerAgentApi on a given host port in a synchronized way
     */
    @Component
    protected static class BehaviorAgentProvider
    {
        private final Map<String, IRestHandlerBehavioragentapi> behaviorAgentRestHandlerByHostPort = new HashMap<>();

        /**
         * Gets the BehaviorAgent rest handler for the URL (host:port) indicated by the parameter
         * Creates a new rest handler if it does not exists previously into the broker agent handlers map
         *
         * @param hostPort The host:port to get the broker agent rest handler
         * @return a RestHandlerBrokeragentapi instance
         */
        public IRestHandlerBehavioragentapi getBehaviorAgentRestHandler(final String hostPort)
        {

            IRestHandlerBehavioragentapi iRestHandlerBehaviorgentapi = this.behaviorAgentRestHandlerByHostPort.get(hostPort);
            if (iRestHandlerBehaviorgentapi == null) // avoid unnecessary and expensive synchronized calls
            {
                synchronized (this.behaviorAgentRestHandlerByHostPort)
                {
                    iRestHandlerBehaviorgentapi = this.behaviorAgentRestHandlerByHostPort.computeIfAbsent(hostPort, k -> new RestHandlerBehavioragentapi(k,
                            NovaJaxRsConfigUtils.DEFAULT_CLIENT_TIMEOUT));
                }
            }

            return iRestHandlerBehaviorgentapi;
        }
    }


}
