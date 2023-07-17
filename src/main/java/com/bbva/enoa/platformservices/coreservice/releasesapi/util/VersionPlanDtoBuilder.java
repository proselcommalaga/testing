package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

import com.bbva.enoa.apirestgen.releasesapi.model.RELVersionPlanDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * {@link VersionPlanDtoBuilder} builder.
 */
@Service(value = "releasesapi.VersionPlanDtoBuilderImpl")
public class VersionPlanDtoBuilder
{
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;


    /**
     * Builds a {@link RELVersionPlanDto} from a {@link DeploymentPlan}
     * if the release version has a plan the given environment.
     * If not, return null.
     *
     * @param releaseVersionId Release version ID.
     * @param environment Environment where the plan is deployed.
     * @param deploymentStatus Status of the plan.
     * @return VersionPlanDto
     */
    public RELVersionPlanDto build(
            int releaseVersionId,
            Environment environment,
            DeploymentStatus deploymentStatus )
    {
        // Get the plan on the environment.
        List<DeploymentPlan> planList =
            this.deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(
                    releaseVersionId,
                    environment.getEnvironment(),
                    deploymentStatus );

        // If any, build a DTO with it.
        if ( !planList.isEmpty() )
        {
            // There should be only one plan.
            return this.buildDtoFromPlan( planList.get( 0 ) );
        }
        else
        {
            // If there were no plans of the product
            // deployed on the environment, return null.
            return null;
        }
    }


    /**
     * Builds a {@link RELVersionPlanDto} from a {@link DeploymentPlan}.
     *
     * @param plan {@link DeploymentPlan}
     * @return VersionPlanDto
     */
    private RELVersionPlanDto buildDtoFromPlan(DeploymentPlan plan)
    {
        RELVersionPlanDto dto = new RELVersionPlanDto();

        dto.setId( plan.getId() );
        dto.setEnvironment( plan.getEnvironment());

        if ( plan.getCreationDate() != null ) {
            dto.setCreationDate(plan.getCreationDate().getTimeInMillis());
        }

        if ( plan.getExecutionDate() != null ) {
            dto.setExecutionDate(plan.getExecutionDate().getTimeInMillis());
        }

        return dto;
    }

}
