package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVVersionPlanDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IVersionPlanDtoBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * {@link VersionPlanDtoBuilderImpl} builder.
 */
@Service
public class VersionPlanDtoBuilderImpl implements IVersionPlanDtoBuilder
{
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    @Override
    public RVVersionPlanDTO build(
            int releaseVersionId,
            Environment environment,
            DeploymentStatus deploymentStatus)
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
            // There should be only one plan deployed in the environment.
            return this.buildDtoFromPlan( planList.get( 0 ) );
        }
        else
        {
            // If there were no plans of the product
            // deployed on the environment, return null.
            return null;
        }
    }


    ///////////////////////////////////////////  PRIVATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    /**
     * Builds a {@link RVVersionPlanDTO} from a {@link DeploymentPlan}.
     *
     * @param plan {@link DeploymentPlan}
     * @return VersionPlanDto
     */
    private RVVersionPlanDTO buildDtoFromPlan(DeploymentPlan plan)
    {
        RVVersionPlanDTO dto = new RVVersionPlanDTO();

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
