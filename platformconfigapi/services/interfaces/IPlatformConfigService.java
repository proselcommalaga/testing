package com.bbva.enoa.platformservices.coreservice.platformconfigapi.services.interfaces;

/**
 * Interface of PlatformConfigAPI
 */
public interface IPlatformConfigService
{
    /**
     * Get platform discovery configuration
     *
     * @param name         name of the configuration
     * @param environment  environment of the configuration
     * @param platformType platform od the configuration (NOVA or ETHER)
     * @return The value of the configuration
     */
    String getPlatformDiscoveryConfig(String name, String environment, String platformType);

    /**
     * Get the platform property
     *
     * @param name name of the property
     * @return The value of the property
     */
    String getPlatformProperty(String name);
}
