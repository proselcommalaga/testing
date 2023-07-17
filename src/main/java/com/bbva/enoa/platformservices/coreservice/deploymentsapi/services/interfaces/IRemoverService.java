package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface of RemoverService
 */
public interface IRemoverService
{
    /**
     * Order to undeploy plan (a plan, checking all validations.
     *
     * @param ivUser       User code.
     * @param deploymentId deployment id
     * @throws NovaException exception
     * @return todoTask response
     */
    TodoTaskResponseDTO undeployPlanOnEnvironment(String ivUser, Integer deploymentId) throws NovaException;

    /**
     * Undeploy plan
     *
     * @param deploymentPlan   deployment plan
     * @param ivUser User code
     * @throws NovaException exception
     */
    void undeployPlan(DeploymentPlan deploymentPlan, String ivUser) throws NovaException;

    /**
     * Removes a {@link DeploymentPlan} from PRE.
     *
     * @param ivUser User code.
     * @param plan   {@link DeploymentPlan}.
     * @return A {@link DeploymentTask} ID.
     * @throws NovaException {@link NovaException}.
     */
    TodoTaskResponseDTO undeployPlanInPre(String ivUser, DeploymentPlan plan) throws NovaException;

    /**
     * Removes a {@link DeploymentPlan} from PRO.
     *
     * @param ivUser User code.
     * @param plan   {@link DeploymentPlan}.
     * @return A {@link DeploymentTask} ID.
     * @throws NovaException {@link NovaException}.
     */
    TodoTaskResponseDTO undeployPlanInPro(String ivUser, DeploymentPlan plan) throws NovaException;

}
