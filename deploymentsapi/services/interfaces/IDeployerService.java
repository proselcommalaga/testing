package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Deployer service interface
 */
public interface IDeployerService
{
    /**
     * Order execution of a plan, checking all validations.
     * <ul>
     * <li>Budget</li>
     * <li>Free slots</li>
     * <li>Plan status</li>
     * <li>Release frozen</li>
     * <li>Deployment requesting by environment</li>
     * <li>SQA and SS approval.</li>
     * </ul>
     *
     * @param ivUser       User code.
     * @param deploymentId {@link DeploymentPlan} ID.
     * @param force        True to force deployment.
     * @throws NovaException on task created
     * @return todoTask response
     */
    TodoTaskResponseDTO deployOnEnvironment(String ivUser, Integer deploymentId, Boolean force) throws NovaException;

    /**
     * Deploys a {@link DeploymentPlan} on {@link Environment}.INT.
     * Checks if the {@link Product} has any pending GB
     * of type NOVA canon. If so, will throw and exception.
     *
     * @param userCode the usercode
     * @param plan     {@link DeploymentPlan} to deploy.
     * @param force    True to force.
     * @throws NovaException error
     * @return todoTask response
     */
    TodoTaskResponseDTO deployOnInt(String userCode, DeploymentPlan plan, Boolean force) throws NovaException;

    /**
     * Deploys a {@link DeploymentPlan} in {@link Environment}.PRE.
     * If related release is frozen a task will be created requesting deployment.
     * Else if autodeploy is active in {@link Environment}.PRE, deployment will
     * be executed. If not, a task will be created. All tasks will be assigned
     * to SQA.
     *
     * @param userCode the usercode
     * @param plan     {@link DeploymentPlan} to deploy.
     * @param force    True to force.
     * @return A {@link DeploymentTask} ID if any was created, 0 otherwise.
     * @throws NovaException error
     */
    TodoTaskResponseDTO deployOnPre(String userCode, DeploymentPlan plan, Boolean force) throws NovaException;

    /**
     * Deploys a {@link DeploymentPlan} in {@link Environment}.PRO.
     * If autodeploy is active in {@link Environment}.PRO and risk level
     * is OK, deployment will be executed.
     * If not, a task will be created.
     * All tasks will be assigned to Service Support.
     *
     * @param userCode the usercode
     * @param plan     {@link DeploymentPlan} to deploy.
     * @param force    True to force.
     * @return A {@link DeploymentTask} ID if any was created, 0 otherwise.
     * @throws NovaException error
     */
    TodoTaskResponseDTO deployOnPro(String userCode, DeploymentPlan plan, Boolean force) throws NovaException;

    /**
     *  Activate a nova planned scheduler deploy.
     *
     * @param userCode the user Code
     * @param plan deployment plan
     */
    void activateNovaPlannedDeployment(
            String userCode,
            DeploymentPlan plan);

    /**
     * Launch the execution of a {@link DeploymentPlan},
     * checking its {@link Product} budget, storing
     * configurations and checking available slots
     * and finally invoking the deployment manager to
     * really execute the plan.
     *
     * @param plan     deployment plan
     * @param force    force deployment
     * @param userCode theusercode
     * @throws NovaException on conflict
     */
    void deploy(String userCode, DeploymentPlan plan, Boolean force) throws NovaException;

    /**
     * Promote plan
     *
     * @param plan    plan
     * @param newPlan newPlan
     */
    void promotePlan(DeploymentPlan plan, DeploymentPlan newPlan);

    /**
     * Update plan status
     *
     * @param service service
     */
    void updatePlanStatus(DeploymentService service);

    /**
     * Update plan status
     *
     * @param subsystem subsystem
     */
    void updatePlanStatus(DeploymentSubsystem subsystem);

    /**
     * UnSchedule a nova deployment
     *
     * @param ivUser the user
     * @param plan  the DeploymentPlan
     */
    void unscheduleDeployment(String ivUser, DeploymentPlan plan);
}
