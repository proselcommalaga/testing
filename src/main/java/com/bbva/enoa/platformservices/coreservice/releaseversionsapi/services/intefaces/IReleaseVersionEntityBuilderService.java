package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseVersionDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseVersionSubsystemDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVVersionDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVVersionSubsystemDTO;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;

import java.util.Set;

/**
 * Build a new {@link ReleaseVersion} entity.
 */
public interface IReleaseVersionEntityBuilderService
{
    /**
     * Create release version from release version dto
     *
     * @param release               release
     * @param versionVersionToAdd   release version to add
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return release version
     * @deprecated
     */
    @Deprecated
    ReleaseVersion buildReleaseVersionEntityFromDto(Release release, RVReleaseVersionDTO versionVersionToAdd, String ivUser, boolean isIvUserPlatformAdmin);

    /**
     * Create release version from release version dto supporting multitag
     *
     * @param release               release
     * @param versionDTO            release version to add
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return release version
     */
    ReleaseVersion buildReleaseVersionEntityFromDto(Release release, RVVersionDTO versionDTO, String ivUser, boolean isIvUserPlatformAdmin);

    /**
     * Creates a new ReleaseVersionSubsystem from a ReleaseVersionSubsystemDto.
     *
     * @param dto                   ReleaseVersionSubsystemDto
     * @param releaseName           - name of the release. Used to generate de image name
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return a {@code ReleaseVersionSubsystem} entity.
     * @deprecated
     */
    @Deprecated
    ReleaseVersionSubsystem buildReleaseVersionSubsystemFromDTO(RVReleaseVersionSubsystemDTO dto, String releaseName, String ivUser, boolean isIvUserPlatformAdmin);


    /**
     * Creates a new ReleaseVersionSubsystem from a ReleaseVersionSubsystemDto.
     *
     * @param dto                   ReleaseVersionSubsystemDto
     * @param releaseName           name of the release. Used to generate de image name
     * @param serviceVersionSet     set of Services (with version) already present to filter in multi-tag ReleaseVersion
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return a {@code ReleaseVersionSubsystem} entity.
     */
    ReleaseVersionSubsystem buildReleaseVersionSubsystemFromDTO(RVVersionSubsystemDTO dto, String releaseName, Set<String> serviceVersionSet, String ivUser, boolean isIvUserPlatformAdmin);
}
