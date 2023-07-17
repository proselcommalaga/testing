package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.release.entities.Release;

/**
 * Validates if subsystem tag is NOVA compliant.
 * <p>
 * Created by xe52580 on 01/03/2017.
 */
public interface ISubsystemValidator
{
    /**
     * Validate the subsystem tag DTO
     *
     * @param tag           Subsystem tag.
     * @param subsystemType the subsystem type
     */
    void validateSubsystemTagDto(SubsystemTagDto tag, SubsystemType subsystemType);

    /**
     * Check release subsystems
     *
     * @param release release
     */
    void checkReleaseSubsystems(Release release);
}
