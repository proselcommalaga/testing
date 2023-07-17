package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;

import java.util.Map;


/**
 * The interface Async api service.
 */
public interface IAsyncApiService
{
    /**
     * Gets async api broker properties map.
     *
     * @param releaseVersionService the release version service
     * @return the async api broker properties map
     */
    Map<String, String> getReleaseVersionServiceBrokerProperties(final ReleaseVersionService releaseVersionService);

}
