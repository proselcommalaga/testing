package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

/**
 * Delete Release Version Service
 */
public interface IDeleteReleaseVersionService
{

    /**
     * Delete release version service
     *
     * @param versionId version id
     * @param ivUser    user
     */
    void deleteReleaseVersion(String ivUser, Integer versionId);
}
