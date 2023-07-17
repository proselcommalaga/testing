package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

/**
 * Archive release version service
 */
public interface IArchiveReleaseVersionService
{
    /**
     * Archive release version
     *
     * @param ivUser    user
     * @param versionId version id
     */
    void archiveReleaseVersion(String ivUser, Integer versionId);
}
