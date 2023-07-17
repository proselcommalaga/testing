package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder;

import com.bbva.enoa.apirestgen.apimanagerapi.model.*;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface for builder for api detail dto model
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface IDtoBuilder
{
    /**
     * Builds the served APIs served in a plan
     *
     *
     * @param deploymentPlan @return Dto for APIs tab in plan
     * @param roles
     */
    ApiPlanDetailDto buildApiPlanDetailDto(final DeploymentPlan deploymentPlan, final CesRole[] roles) throws NovaException;

    ApiPlanDto buildApiPlanDto(final ApiVersion<?,?,?> apiVersion, final String releaseName, final PlanProfile planProfile);

    /**
     * Builds an Api Detail Dto from a Sync Api entity
     *
     * @param api the sync api
     * @return built dto
     */
    ApiDetailDto buildApiDetailDto(final Api<?,?,?> api);

    /**
     * Builds an Api Version Detail Dto from a Sync Api Version entity
     *
     * @param apiVersion the sync api version
     * @return built dto
     */
    ApiVersionDetailDto buildApiVersionDetailDto(final ApiVersion<?,?,?> apiVersion, final String filterByDeploymentStatus);

    /**
     * Builds the list of APIs Dto for a product
     *
     * @param productId Product Id
     * @return array of {@link ApiDto} of the product
     */
    ApiDto[] buildApiDtoArray(final Integer productId);

}
