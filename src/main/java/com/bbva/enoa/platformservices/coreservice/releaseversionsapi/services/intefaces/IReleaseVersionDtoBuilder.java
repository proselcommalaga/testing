package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseVersionDTO;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;

/**
 * Builds a DTO from a ReleaseVersion entity.
 * <p>
 * Created by xe52580 on 27/02/2017.
 */
public interface IReleaseVersionDtoBuilder
{
    /**
     * Builds a DTO for creating a new release version of a product.
     *
     * @param releaseVersion Release version entity.
     * @return ReleaseVersionDto
     */
    RVReleaseVersionDTO build(ReleaseVersion releaseVersion);
}
