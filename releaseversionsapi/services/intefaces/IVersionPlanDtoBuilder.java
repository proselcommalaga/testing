package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVVersionPlanDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl.VersionPlanDtoBuilderImpl;

/**
 * {@link VersionPlanDtoBuilderImpl} builder.
 */
public interface IVersionPlanDtoBuilder
{
    /**
     * Builds a {@link RVVersionPlanDTO} from a {@link DeploymentPlan}
     * if the release version has a plan the given environment.
     * If not, return null.
     *
     * @param releaseVersionId Release version ID.
     * @param environment      Environment where the plan is deployed.
     * @param deploymentStatus Status of the plan.
     * @return VersionPlanDto
     */
    RVVersionPlanDTO build(int releaseVersionId, Environment environment, DeploymentStatus deploymentStatus);
}
