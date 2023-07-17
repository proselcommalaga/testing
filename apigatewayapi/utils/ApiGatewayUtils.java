package com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils;

import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ApiGatewayUtils
{
    private final EtherUtils etherUtils;

    public String buildServiceLocation(ReleaseVersionService releaseVersionService, final String deploymentMode, final String releaseName,
                                       final String environment)
    {
        return this.buildServiceLocationByParams(releaseVersionService.getGroupId(), releaseVersionService.getArtifactId(), releaseVersionService.getVersion(), deploymentMode, releaseName, environment);
    }

    public String buildServiceLocationByParams(final String groupId, final String artifactId, final String version, final String deploymentMode, final String releaseName,
                                               final String environment)
    {

        if (Platform.NOVA.name().equalsIgnoreCase(deploymentMode))
        {
            return ServiceNamingUtils.getNovaServiceName(groupId,
                    artifactId, releaseName,
                    version.split("\\.")[0]);
        }
        else if (Platform.ETHER.name().equalsIgnoreCase(deploymentMode))
        {
            return etherUtils.getEAEServiceURL(environment, groupId,
                    artifactId, releaseName, version);
        }

        return "";
    }
}
