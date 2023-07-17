package com.bbva.enoa.platformservices.coreservice.deploymentsapi.autoconfig;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Class for configuration
 */
@Configuration
@EnableConfigurationProperties({NovaToolsProperties.class, NovaMonitoringProperties.class})
public class DeploymentsAutoconfiguration
{
}
