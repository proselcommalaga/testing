package com.bbva.enoa.platformservices.coreservice.consumers.autoconfig;

import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@ConfigurationProperties(prefix = NovaAgentClientProperties.PREFIX)
// TODO@Santi eliminar combinacion component/configruationProperties en siguiente iteracion
public class NovaAgentClientProperties
{
    public static final String PREFIX = "nova";

    @Getter
    @Setter
    private List<NovaAgentByEnvironment> novaAgentList;

    public NovaAgentByEnvironment getNovaAgentByEnvironment(final String environment) throws NovaException
    {
        log.trace("[NovaAgentClientProperties] -> [getNovaAgentByEnvironment]: environment = {}", environment);

        NovaAgentByEnvironment novaAgent;

        if (this.novaAgentList == null || this.novaAgentList.isEmpty())
        {
            String msg = "No properties set for Nova Agent in app config. " +
                    "Please, set the appropriate Nova Agent environment and host-port properties.";
            log.error("[NovaAgentByEnvironment] -> [getNovaAgentByEnvironment]:" + msg);
            throw new NovaException(CommonError.getWrongNovaAgentConfigurationError(NovaAgentClientProperties.class.getSimpleName(), msg));
        }
        else
        {
            novaAgent = this.novaAgentList
                    .stream()
                    .filter(hostPort -> environment.equalsIgnoreCase(hostPort.getEnvironment()))
                    .findFirst()
                    .orElseThrow(() -> {
                        String msg = "No properties set for Nova Agent in app config for the environment [" + environment + "]. " +
                                "Please, set the appropriate Nova Agent environment and host-port properties.";
                        log.error("[NovaAgentByEnvironment] -> [getNovaAgentByEnvironment]:" + msg);
                        throw new NovaException(CommonError.getWrongNovaAgentConfigurationError(NovaAgentClientProperties.class.getSimpleName(), msg));
                    });
        }

        return novaAgent;
    }

    /**
     * NovaAgentByEnvironment instance
     */
    public static class NovaAgentByEnvironment
    {
        /**
         * Name of the NovaAgentByEnvironment
         */
        @Getter
        @Setter
        private String environment;

        /**
         * List of 'host:port' in this environment for Nova Agent
         */
        @Getter
        @Setter
        private List<String> hostPort;
    }
}
