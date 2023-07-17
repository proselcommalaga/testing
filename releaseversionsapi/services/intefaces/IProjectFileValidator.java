package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;

import java.util.List;

/**
 * Validator of services by the NOVA rules.
 * <p>
 * Created by xe52580 on 17/02/2017.
 */
public interface IProjectFileValidator
{
    /**
     * Check if a all the files service (pom.xml, application.yml,
     * bootstrap.yml...) are valid following the NOVA rules.
     *
     * @param validatorInputs             Set of variable to be validated
     * @param subsystemNovaType           Type of subsystem the service belongs to.
     * @param newReleaseVersionServiceDto The service DTO.
     * @param repoId                      repository id
     * @param tag                         tag
     * @param ivUser                      user requester
     * @param releaseName                 Name of the release
     * @param product                     Product this service belongs to
     * @param subsystemName               Subsystem name
     */
    void validateServiceProjectFiles(ValidatorInputs validatorInputs, final SubsystemType subsystemNovaType,
                                     NewReleaseVersionServiceDto newReleaseVersionServiceDto, final int repoId,
                                     final String tag, final String ivUser, final String releaseName, Product product, String subsystemName);

    /**
     * Check if a all the files service (pom.xml, application.yml,
     * bootstrap.yml...) are valid following the NOVA rules.
     *
     * @param validatorInputs  Set of variable to be validated
     * @param bvServiceInfoDTO The service DTO.
     * @param repoId           repository id
     * @param tag              tag
     * @param ivUser           user requester
     * @param product          Product this service belongs to
     * @param subsystemName    Subsystem name
     */
    void validateBehaviorServiceProjectFiles(List<ValidationErrorDto> errorList, ValidatorInputs validatorInputs, BVServiceInfoDTO bvServiceInfoDTO, final int repoId,
                                             final String tag, final String ivUser, Product product, String subsystemName, String behaviorVersionName);

}
