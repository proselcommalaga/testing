package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVBehaviorParamsDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMBehaviorParamsDTO;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;

/**
 * Service DTO builder
 * Builds the DTO of a service
 */
public interface IBehaviorServiceDtoBuilder
{

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
     * Builds a BVServiceInfoDTO from the data of its pom.xml.
     * <p>
     * This includes:
     * <p>
     * - Maven coordinates.
     * - Service type in NOVA.
     * - Properties.
     * - Metadata.
     *
     * @param validatorInputs validator inputs with the pom.xml and nova.yml parameters
     * @return BVServiceInfoDTO.
     */
    BVServiceInfoDTO buildServiceGeneralInfo(ValidatorInputs validatorInputs);

    /**
     * Build DTO with generic info about service
     *
     * @param tagUrl      tag url
     * @param projectPath project path
     * @param serviceDto  DTO with some information about service
     */
    void buildServiceGenerics(String tagUrl, String projectPath, BVServiceInfoDTO serviceDto);

    /**
     * Build DTO with behavior test params
     *
     * @param behaviorParamsDTO behavior params DTO
     * @return DTO
     */
    BMBehaviorParamsDTO buildBMBehaviorParamsDTO(BVBehaviorParamsDTO behaviorParamsDTO);

}
