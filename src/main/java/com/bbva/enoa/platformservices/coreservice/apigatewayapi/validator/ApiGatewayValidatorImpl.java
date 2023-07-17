package com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import org.springframework.stereotype.Service;

/**
 * API Gateway validator
 */
@Service
public class ApiGatewayValidatorImpl implements IApiGatewayValidator
{

    /**
     * check if Release Version has at least one API_REST_****
     *
     * @param releaseVersion Release version to check
     * @return boolean with the result of the check
     */
    @Override
    public boolean checkReleaseVersionHasRESTServices(final ReleaseVersion releaseVersion)
    {
        boolean hasRESTServices = false;
        for (ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
        {
            for (ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                // only REST Services
                hasRESTServices = this.checkReleaseVersionServiceHasMicroGateway(releaseVersionService.getServiceType());
            }
        }
        return hasRESTServices;
    }

    /**
     * check if Release Version Service has mgw****
     *
     * @param serviceType Release version Service to check
     * @return boolean with the result of the check
     */
    @Override
    public boolean checkReleaseVersionServiceHasMicroGateway(final String serviceType)
    {
        return ServiceType.valueOf(serviceType).isMicrogateway();
    }
}
