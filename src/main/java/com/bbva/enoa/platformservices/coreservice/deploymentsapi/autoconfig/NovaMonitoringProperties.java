package com.bbva.enoa.platformservices.coreservice.deploymentsapi.autoconfig;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Class for properties
 */
@Getter
@Setter
@ConfigurationProperties(prefix = NovaMonitoringProperties.PREFIX)
public class NovaMonitoringProperties
{
    /**
     * Properties prefix
     */
    public static final String PREFIX = "nova.mappings.monitoring";

    private Map<Environment, String> products;

    private Map<Environment, String> containers;
}
