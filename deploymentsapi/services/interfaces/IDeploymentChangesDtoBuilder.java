package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDtoPage;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import org.springframework.data.domain.Page;

/**
 * Builds an array of {@link DeploymentChangeDto} from the {@link DeploymentChange}
 * of a {@link DeploymentPlan}.
 */
public interface IDeploymentChangesDtoBuilder
{
    /**
     * Builds an array of {@link DeploymentChangeDto} from the {@link DeploymentChange}
     * of a {@link DeploymentPlan}.
     *
     * @param deploymentChangePageable with the Deployment Change Pageable
     * @return DeploymentChangeDto[]
     */
    DeploymentChangeDtoPage build(Page<DeploymentChange> deploymentChangePageable);
}
