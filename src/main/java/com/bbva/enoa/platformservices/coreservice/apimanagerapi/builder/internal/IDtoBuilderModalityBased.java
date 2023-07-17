package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.IApiModalitySegregatable;

/**
 * Interface for builder for api detail dto model
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface IDtoBuilderModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
    extends IApiModalitySegregatable
{

    ApiPlanDto buildApiPlanDto(final AV apiVersion, final String releaseName, final PlanProfile planProfile);

    /**
     * Builds an Api Detail Dto from a Sync Api entity
     *
     * @param api the sync api
     * @return built dto
     */
    ApiDetailDto buildApiDetailDto(final A api);
}
