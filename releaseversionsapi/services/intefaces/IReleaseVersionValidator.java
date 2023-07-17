package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;

/**
 * Validator for release versions api.
 */
public interface IReleaseVersionValidator
{
    /**
     * Checks if a release version does exist or no.
     *
     * @param releaseVersion Release version.
     */
    void checkReleaseVersionExistance(ReleaseVersion releaseVersion);

    /**
     * Checks if a release version does exist or no.
     *
     * @param releaseVersion Release version.
     */
    void checkRelaseVersionStatusNotCompiling(ReleaseVersion releaseVersion);

    /**
     * Checks if there is another Release Version with the same name
     * on the same Release.
     *
     * @param release            - the release.
     * @param releaseVersionName Release version name.
     */
    void existsReleaseVersionWithSameName(Release release, String releaseVersionName);

    /**
     * Checks if a {@link ReleaseVersion} can be storaged.
     * <p>
     * Fails when the version was NOT deployed at least once
     * on any NOVA environment no matters which one of if
     * it is still deployed or not.
     *
     * @param versionId {@link ReleaseVersion} ID.
     * @param releaseVersion release version
     */
    void checkIfReleaseVersionCanBeStored(int versionId, ReleaseVersion releaseVersion);

    /**
     * Check maximum number opf release versions
     *
     * @param productId product Id
     * @param releaseSlots release slots of product
     */
    void checkMaxReleaseVersions(int productId, int releaseSlots);

    /**
     * Check maximum number opf release versions
     *
     * @param productId product Id
     */
    void checkCompilingReleaseVersions(int productId);

    /**
     * Check if the release version has plan
     *
     * @param releaseVersion release version
     */
    void checkReleaseVersionHasPlan(ReleaseVersion releaseVersion);

    /**
     * Check if the release verison has any log event
     *
     * @param releaseVersion release version
     */
    void checkReleaseVersionHasLogEvent(ReleaseVersion releaseVersion);

    /**
     * Save and flush a release version subsystem
     *
     * @param releaseVersionSubsystem the release version subsystem
     * @return a saved release version subsystem
     */
    ReleaseVersionSubsystem saveAndFlushReleaseVersionSubsystem(ReleaseVersionSubsystem releaseVersionSubsystem);

    /**
     * Save and flush a release version
     *
     * @param releaseVersion the release version to save
     * @return a saved release version
     */
    ReleaseVersion saveAndFlushReleaseVersion(ReleaseVersion releaseVersion);
}
