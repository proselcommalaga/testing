package com.bbva.enoa.platformservices.coreservice.platformconfigapi.services.impl;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlatformDiscoveryConfigRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlatformPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.platformconfigapi.services.interfaces.IPlatformConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Platform config service.
 */
@Service
public class PlatformConfigServiceImpl implements IPlatformConfigService
{
    /**
     * Platform discovery repository
     */
    private final PlatformDiscoveryConfigRepository platformDiscoveryRepository;

    /**
     * Platform property repository
     */
    private final PlatformPropertyRepository platformPropertyRepository;

    /**
     * Public constructor
     *
     * @param platformDiscoveryRepository Platform discovery repository
     * @param platformPropertyRepository  Platform property repository
     */
    @Autowired
    public PlatformConfigServiceImpl(final PlatformDiscoveryConfigRepository platformDiscoveryRepository, final PlatformPropertyRepository platformPropertyRepository)
    {
        this.platformDiscoveryRepository = platformDiscoveryRepository;
        this.platformPropertyRepository  = platformPropertyRepository;
    }

    @Override
    public String getPlatformDiscoveryConfig(final String name, final String environment, final String platformType)
    {
        // get the configuration
        return this.platformDiscoveryRepository.findByNameAndEnvironmentAndPlatform(name, environment,
                Platform.valueOf(platformType));
    }

    @Override
    public String getPlatformProperty(final String name)
    {
        // get the property
        return this.platformPropertyRepository.findByPropertyName(name);
    }
}
