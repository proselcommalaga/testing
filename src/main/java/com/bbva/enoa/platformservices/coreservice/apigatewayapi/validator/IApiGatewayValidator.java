package com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;

/**
 * ApiGateway service validator
 */
public interface IApiGatewayValidator
{
    /**
     * Check whether rest services
     *
     * @param releaseVersion release version
     * @return check
     */
    boolean checkReleaseVersionHasRESTServices(ReleaseVersion releaseVersion);


    /**
     * Check whether service is rest
     *
     * @param serviceType service type
     * @return check
     */
    boolean checkReleaseVersionServiceHasMicroGateway(String serviceType);

}
