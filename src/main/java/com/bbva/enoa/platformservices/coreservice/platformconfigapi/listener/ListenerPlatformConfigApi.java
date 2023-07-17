package com.bbva.enoa.platformservices.coreservice.platformconfigapi.listener;

import com.bbva.enoa.apirestgen.platformconfigapi.server.spring.nova.rest.IRestListenerPlatformconfigapi;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.platformconfigapi.services.interfaces.IPlatformConfigService;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener of the PlatformConfigAPI.
 */
@Log
@Service
public class ListenerPlatformConfigApi implements IRestListenerPlatformconfigapi
{
    /**
     * Name of API PlatformConfig
     */
    private static final String PLATFORM_CONFIG_API_NAME = "PlatformConfigApi";

    /**
     * Platform Config service
     */
    private final IPlatformConfigService platformConfigService;

    /**
     * Public constructor
     *
     * @param platformConfigService platform config service
     */
    @Autowired
    public ListenerPlatformConfigApi(final IPlatformConfigService platformConfigService)
    {
        this.platformConfigService = platformConfigService;
    }

    @Override
    @LogAndTrace(apiName = PLATFORM_CONFIG_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public String getPlatformProperty(final NovaMetadata novaMetadata, final String name) throws Errors
    {
        return this.platformConfigService.getPlatformProperty(name);
    }

    @Override
    @LogAndTrace(apiName = PLATFORM_CONFIG_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public String configureDiscoverPlatform(final NovaMetadata novaMetadata, final String name, final String environment, final String platform) throws Errors
    {
        return this.platformConfigService.getPlatformDiscoveryConfig(name, environment, platform);
    }
}
