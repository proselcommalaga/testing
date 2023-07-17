package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;

import java.util.List;

/**
 * Validator interface to check libraries used by sercicezs
 */
public interface ILibraryValidator
{
    /**
     * Validate the list of libraries used by a service from its nova.yml file
     *
     * @param validatorInputs             validatorInputs with Nova.yml and other files
     * @param newReleaseVersionServiceDto service
     * @param tag                         tag
     * @param releaseVersionName          Release Version Name
     * @param errorList                   error list
     */
    void validateLibrariesFromNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto newReleaseVersionServiceDto, String tag,
                                      String releaseVersionName, List<ValidationErrorDto> errorList);

    /**
     * Validate the specific parameters of nova.yml for a NOVA library service
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     */
    void validateNovaLibraryNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList);

    /**
     * Validate a pom.xml of a library
     *
     * @param validatorInputs validatorInputs with Nova.yml and other files
     * @param dto             service
     * @param errorList       List of validation errors.
     */
    void validateNovaLibraryByPomXml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList);

    /**
     * Check if current version is already publish and properties or requirements are overrided
     *
     * @param validatorInputs validatorInputs with Nova.yml and other files
     * @param dto             service
     * @param errorList       validation errors list
     */
    void validateNovaLibraryNewVersion(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList);
}
