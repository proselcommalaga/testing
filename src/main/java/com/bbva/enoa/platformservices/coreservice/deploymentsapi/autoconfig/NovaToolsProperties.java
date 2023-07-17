package com.bbva.enoa.platformservices.coreservice.deploymentsapi.autoconfig;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Class for properties
 */
@Getter
@Setter
@ConfigurationProperties(prefix = NovaToolsProperties.PREFIX)
public class NovaToolsProperties
{
    /**
     * Properties prefix
     */
    public static final String PREFIX = "nova.tools";

    private Map<String, String> atenea;
}
