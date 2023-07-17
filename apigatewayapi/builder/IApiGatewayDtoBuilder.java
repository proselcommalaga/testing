package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.*;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.platformservices.coreservice.common.model.PublicationParams;
import com.bbva.enoa.platformservices.coreservice.common.model.ServicePublicationParams;

import java.util.List;

/**
 * Builds ApiGateway Service DTO from a ReleaseVersion entity.
 */
public interface IApiGatewayDtoBuilder
{

    /**
     * Transforms releaseVersionSubsystem Details into ApPublicationDto
     *
     * @param publicationParams deployment plan
     * @return ApiPublicationDto
     */
    AGMCreatePublicationDTO buildCreatePublicationDto(PublicationParams publicationParams);

    /**
     * Transforms ReleaseVersion Details into ReleaseDetailDto
     *
     * @param deploymentPlan deployment plan
     * @return ReleaseDetailDto
     */
    AGMRemovePublicationDTO buildRemovePublicationDetailDto(DeploymentPlan deploymentPlan);

    /**
     * Transforms releaseVersionSubsystem Details into ApPublicationDto
     *
     * @param createdServices created services
     * @param removedServices removed services
     * @param newPlan         new plan
     * @param oldPlan         old plan
     * @return AGMCreatePublicationDTO
     */
    AGMUpdatePublicationDTO buildUpdatePublicationDto(final List<DeploymentService> createdServices, final List<DeploymentService> removedServices, final DeploymentPlan newPlan, final DeploymentPlan oldPlan);

    /**
     * Transforms a {@link DeploymentService} into a {@link AGMDockerServiceDTO}
     *
     * @param deploymentService Deployment Service
     * @return {@link AGMDockerServiceDTO}
     */
    AGMDockerServiceDTO buildDockerServiceDto(DeploymentService deploymentService);

    /**
     * Transforms a {@link BehaviorService} into a {@link AGMDockerServiceDTO}
     *
     * @param behaviorService Deployment Service
     * @return {@link AGMDockerServiceDTO}
     */
    AGMDockerServiceDTO buildDockerServiceDto(BehaviorService behaviorService);

    /**
     * Create CES profiling DTO for given plan
     *
     * @param deploymentPlan plan
     * @return profiling dto
     */
    AGMCreateProfilingDTO buildCreateProfilingDto(DeploymentPlan deploymentPlan);

    /**
     * Build DTO to remove profiling of given plan
     *
     * @param deploymentPlan plan
     * @return dto for remove profiling
     */
    AGMRemoveProfilingDTO buildRemoveProfilingDto(DeploymentPlan deploymentPlan);

    /**
     * Build DTO with all necessary information for create a publication or a docker key
     *
     * @param deploymentPlan plan
     * @return dto
     */
    PublicationParams buildPublicationParams(DeploymentPlan deploymentPlan);

    /**
     * Build DTO with all necessary information for create a publication or a docker key
     *
     * @param behaviorService behaviorService
     * @return dto
     */
    PublicationParams buildPublicationParams(BehaviorService behaviorService);

    /**
     * Build DTO with all necessary information for create a publication or a docker key
     *
     * @param behaviorService behaviorService
     * @return dto
     */
    List<ServicePublicationParams> buildServicePublicationParamsList(BehaviorService behaviorService);

}
