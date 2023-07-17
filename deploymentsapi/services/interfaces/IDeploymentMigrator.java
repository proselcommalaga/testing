package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;

public interface IDeploymentMigrator
{
    /**
     * Migrate a {@link DeploymentPlan} to a target one.
     *
     * @param originalPlan original plan
     * @param targetPlan   target plan
     * @return dto Data from the migrated {@link DeploymentPlan}.
     */
    DeploymentMigrationDto migratePlan(DeploymentPlan originalPlan, DeploymentPlan targetPlan);
}
