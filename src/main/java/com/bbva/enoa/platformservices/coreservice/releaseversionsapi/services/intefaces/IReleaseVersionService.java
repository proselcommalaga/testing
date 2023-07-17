package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;

/**
 * Release Version Service
 * Process all the logic of the Release Version API
 */
public interface IReleaseVersionService
{
    /**
     * Stores the new release version in DB.
     *
     * @param versionToAdd ReleaseVersion to create and add to a Release.
     * @param ivUser       BBVA user code.
     * @param releaseId    The ID of the Release to validateFilesystemDeletion.
     * @deprecated
     */
    @Deprecated
    void addReleaseVersion(
            RVReleaseVersionDTO versionToAdd,
            String ivUser,
            Integer releaseId);

    /**
     * Stores the new release version in DB.
     *
     * @param versionDTO ReleaseVersion to create and add to a Release.
     * @param ivUser     BBVA user code.
     * @param releaseId  The ID of the Release to validateFilesystemDeletion.
     */
    void createReleaseVersion(RVVersionDTO versionDTO, String ivUser, Integer releaseId);

    /**
     * Builds subsystem status.
     *
     * @param subsystemId                - subsystem id
     * @param ivUser                     - user requester
     * @param jobName                    - job name associated to the subsystem
     * @param jenkinsJobGroupMessageInfo - message with the results of the all jenkins jobs of this group (the group associated to subsystem id or jenkins job name)
     * @param status                     - status of the jenkins job group. Can be SUCCESS or FAILE
     */
    void subsystemBuildStatus(String ivUser, Integer subsystemId, String jobName, String jenkinsJobGroupMessageInfo,
                              String status);

    /**
     * Storage a {@link ReleaseVersion}. Can be storaged only when
     * was deployed or has any {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}
     * with configurations or task attached. Final com.bbva.enoa.platformservices.historicalloaderservice.step is release memory at Docker Registry.
     *
     * @param ivUser    BBVA user code.
     * @param versionId The ID of the release version.
     */
    void archiveReleaseVersion(String ivUser, Integer versionId);

    /**
     * Get a detailed DTO for a release version.
     *
     * @param ivUser           BBVA user code.
     * @param releaseVersionId The ID of the release version.
     * @return Success
     */
    RVReleaseVersionDTO getReleaseVersion(
            String ivUser,
            Integer releaseVersionId);

    /**
     * Updates a {@link ReleaseVersion} - by now only its description.
     *
     * @param ivUser      BBVA user code.
     * @param versionId   The ID of the ReleaseVersion.
     * @param description Description of the release version.
     * @return ReleaseVersionDto
     */
    RVReleaseVersionDTO updateReleaseVersion(
            String ivUser, Integer versionId,
            String description);

    /**
     * Deletes a {@link ReleaseVersion}. Can be deleted only when
     * never was deployed or doesn't have any {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}
     * with configurations or task attached.
     *
     * @param ivUser    BBVA user code.
     * @param versionId The ID of the release version.
     */
    void deleteReleaseVersion(String ivUser, Integer versionId);

    /**
     * Updates a {@link ReleaseVersion} - by now only its description.
     *
     * @param ivUser    BBVA user code.
     * @param versionId The ID of the ReleaseVersion.
     * @param issueId   issueId of the JIRA created
     */
    void updateReleaseVersionIssue(String ivUser, Integer versionId, String issueId);

    /**
     * Builds a clean DTO for editing a new ReleaseVersion with data from
     * a Release, Product, ReleaseSubsystems and services, including
     * their validation.
     *
     * @param ivUser    BBVA user code.
     * @param releaseId The ID of the Release to validateFilesystemDeletion.
     * @return NewReleaseVersionDto with the data for the new release version.
     */
    NewReleaseVersionDto newReleaseVersion(
            String ivUser,
            Integer releaseId);

    /**
     * Get a detailed DTO for all release version subsystems.
     *
     * @param subystemId subsystem identifier
     * @return Success
     */
    RVReleaseVersionSubsystemDTO[] getReleaseVersionSubsystems(final Integer subystemId);

    /**
     * Builds a DTO for create a new ReleaseVersion with data from
     * a release and subsystems with their tags
     *
     * @param ivUser    BBVA user code.
     * @param releaseId release identifier
     * @return RVRequestDTO with the data for create a new release version.
     */
    RVRequestDTO releaseVersionRequest(final String ivUser, final Integer releaseId);

    /**
     * Build a DTO with the response of validate one or many given tags
     *
     * @param ivUser          BBVA user code
     * @param releaseId       release identifier
     * @param rvValidationDTO DTO with all tags to validate.
     * @return RVValidationResponseDTO with all validation
     */
    RVValidationResponseDTO validateAllTags(final String ivUser, final Integer releaseId, final RVValidationDTO rvValidationDTO);

    /**
     * Get all the possible values that can take the field status ({@link ReleaseVersionStatus}).
     *
     * @return An array with all the possible values that can take the field status.
     */
    String[] getStatuses();
}
