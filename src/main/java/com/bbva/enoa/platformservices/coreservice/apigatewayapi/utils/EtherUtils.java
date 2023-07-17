package com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils;

import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlatformDiscoveryConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EtherUtils
{
    /**
     * Name of the configuration for the base domain
     */
    private static final String BASE_DOMAIN = "baseDomain";

    /**
     * Platform discovery repository
     */
    private final PlatformDiscoveryConfigRepository platformDiscoveryRepository;
    private final String etherProtocol;

    /**
     * Public constructor
     *
     * @param platformDiscoveryRepository Platform discovery repository
     */
    @Autowired
    public EtherUtils(final PlatformDiscoveryConfigRepository platformDiscoveryRepository, @Value("${nova.ether.protocol:http}") final String etherProtocol)
    {
        this.platformDiscoveryRepository = platformDiscoveryRepository;
        this.etherProtocol = etherProtocol;
    }

    /**
     * Get the URL for Ether service
     *
     * @param environment    The environment
     * @param groupId        The group Id
     * @param serviceName    The service name
     * @param releaseName    The release name
     * @param serviceVersion The release version
     * @return the URL for Ether service
     */
    public String getEAEServiceURL(final String environment, final String groupId, final String serviceName, final String releaseName, final String serviceVersion)
    {
        // get base domain
        final String baseDomain = this.platformDiscoveryRepository.findByNameAndEnvironmentAndPlatform(BASE_DOMAIN, environment, Platform.ETHER);

        return EtherServiceNamingUtils.getEAEServiceURL(this.etherProtocol, baseDomain, environment, groupId, serviceName, releaseName, serviceVersion);
    }
}
