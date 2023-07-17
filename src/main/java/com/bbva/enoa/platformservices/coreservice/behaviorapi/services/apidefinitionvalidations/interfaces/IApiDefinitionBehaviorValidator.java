package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;

import java.util.List;

/**
 * YML (Swagger | AsyncApi) validator interface
 */
public interface IApiDefinitionBehaviorValidator
{

    /**
     * Validate yml
     *
     * @param novaYml          Nova.yml with the api's info
     * @param bvServiceInfoDTO service
     * @param repoId           repository id
     * @param tag              tag
     */
    List<ValidationErrorDto> validateAndAssociateApiBehavior(NovaYml novaYml, BVServiceInfoDTO bvServiceInfoDTO,
                                                             int repoId, String tag);

    /**
     * Check if it is the resolver of validators or a real validator
     *
     * @return true if it is a real validator. False if it is the resolver
     */
    boolean isValidator();
}
