package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;

import java.util.List;

/**
 * Service DTO builder
 * Builds the DTO of a service
 */
public interface IServiceDtoBuilder
{

    /**
     * Builds an array of NewReleaseVersionServiceDto from the services of a
     * given subsystem tag.
     *
     * @param repoId            ID del repositorio en el VCS.
     * @param tag               Tag.
     * @param subsystemNovaType subsystem NOVA type
     * @param ivUser            user requester
     * @param releaseName       Name of the release
     * @param product           Product this subsystem belongs to
     * @param subsystemName     Subsystem name
     * @return List of services DTO.
     */
    List<NewReleaseVersionServiceDto> buildServicesFromSubsystemTag(
            int repoId,
            SubsystemTagDto tag,
            SubsystemType subsystemNovaType,
            String ivUser,
            String releaseName,
            Product product,
            String subsystemName);


    /**
     * Read and fulfill the values under validation into a buildValidationFileInputs object
     *
     * @param projectPath Project Path
     * @param repoId      Repository Id
     * @param tag         Tag
     * @return validationInput with the values to be validated
     */
    ValidatorInputs buildValidationFileInputs(String projectPath, int repoId, String tag);


    /**
     * Builds a NewReleaseVersionServiceDto from the data of its pom.xml.
     * <p>
     * This includes:
     * <p>
     * - Maven coordinates.
     * - Service type in NOVA.
     * - Properties.
     * - Metadata.
     *
     * @param releaseName     Name of the release
     * @param validatorInputs validator inputs with the pom.xml and nova.yml parameters
     * @return NewReleaseVersionServiceDto.
     */
    NewReleaseVersionServiceDto buildServiceGeneralInfo(String releaseName, ValidatorInputs validatorInputs);

    /**
     * Build DTO with generic info about service
     *
     * @param modules     modules
     * @param tagUrl      tag url
     * @param projectPath project path
     * @param serviceDto  DTO with some information about service
     */
    void buildServiceGenerics(String[] modules, String tagUrl, String projectPath, NewReleaseVersionServiceDto serviceDto);
}
