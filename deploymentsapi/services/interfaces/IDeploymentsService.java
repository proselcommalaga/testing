package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentTypeChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.TaskRequestDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.Calendar;
import java.util.List;

public interface IDeploymentsService
{
    /**
     * Adds a change to a deploymentplan
     *
     * @param change           Change to add
     * @param deploymentPlanId Id of the deploymentPlan to add change to
     */
    void addDeploymentChange(DeploymentChangeDto change, int deploymentPlanId);

    /**
     * Archive a never deployed {@link DeploymentPlan}.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     */
    void archivePlan(int deploymentId);

    /**
     * Updates the deployment type of a {@link DeploymentPlan}.
     *  @param userCode     - BBVA user code
     * @param deploymentId {@link DeploymentPlan} ID
     * @param changeType   {@link DeploymentTypeChangeDto}
     * @return todoTask response
     */
    TodoTaskResponseDTO changeDeploymentType(String userCode, int deploymentId, DeploymentTypeChangeDto changeType);

    /**
     * Copy plan
     *
     * @param originalDeploymentPlan original plan
     * @return new plan
     */
    DeploymentPlan copyPlan(DeploymentPlan originalDeploymentPlan);

    /**
     * Create deployment.
     *
     * @param releaseVersionId {@link ReleaseVersion}
     * @param environment      {@link Environment}
     * @param isMigrated       if is migrated
     * @return plan id
     * @throws NovaException {@link NovaException} on error
     */
    DeploymentPlan createDeployment(int releaseVersionId, String environment, boolean isMigrated) throws NovaException;

    /**
     * Deletes a never deployed {@link DeploymentPlan}.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     */
    void deletePlan(int deploymentId) throws NovaException;

    /**
     * Deploy a plan.
     *
     * @param ivUser       iv user
     * @param deploymentId deployment id
     * @param force        force deployment
     * @throws NovaException conflict exception or on task created
     * @return todotaskersponsedto
     */
    TodoTaskResponseDTO deploy(String ivUser, Integer deploymentId, Boolean force) throws NovaException;

    /**
     * Gets deployments plan between dates and in specific statuses
     *
     * @param environment Envrionment to search deployments for
     * @param productId   Product id to search deploymentPlans in
     * @param deploymentStatusList Statuses to filter by
     * @param startDate   starting date for the query
     * @param endDate     end date for the query
     * @return List of found DeploymentPlans
     */
    List<DeploymentPlan> getDeploymentPlansBetween(String environment, int productId, Calendar startDate, Calendar endDate, String deploymentStatusList);

    /**
     * Gets a dpeloymentService by all its names
     *
     * @param productName   the product name of the service
     * @param environment   the environment service is deployed on
     * @param releaseName   release service belongs to
     * @param subsystemName subsytem service belongs to
     * @param serviceName   name of the service
     * @return Deploymentservice that meets all the conditions
     * @throws NovaException deployment exception
     */
    DeploymentService getDeploymentServiceByName(String productName, String environment, String releaseName, String subsystemName, String serviceName) throws NovaException;

    /**
     * Migrate a {@link DeploymentPlan} to a new one with the same or a
     * different {@link ReleaseVersion}.
     *
     * @param deploymentId deployment id
     * @param versionId    version id
     * @return dto Data from the migrated plan
     * @throws NovaException on error
     */
    DeploymentMigrationDto migratePlan(Integer deploymentId, Integer versionId) throws NovaException;

    /**
     * Copies a plan from an environment to another, performing validations.
     * A {@link DeploymentPlan} from {@link Environment}.INT can only be promoted to {@link Environment}.PRE.
     * A plan from {@link Environment}.PRE can only be copied to {@link Environment}.PRO.
     *
     * @param originalPlan {@link DeploymentPlan} to copy.
     * @param environment  {@link Environment} where to copy the plan.
     */
    void promotePlanToEnvironment(DeploymentPlan originalPlan, Environment environment);

    /**
     * Undeploy Plan
     *
     * @param ivUser       User code.
     * @param deploymentId deployment id
     * @throws NovaException on task created
     * @return todoTask response
     */
    TodoTaskResponseDTO undeployPlan(String ivUser, Integer deploymentId) throws NovaException;

    /**
     * Updates a {@link DeploymentPlan} using the values from a {@link DeploymentDto}.
     * Will update only values which are not null.
     *
     * @param deploymentDto {@link DeploymentDto}
     * @param env           environment
     */
    void updatePlanFromDto(DeploymentDto deploymentDto, String env);

    /**
     * Updates a {@link DeploymentService} using data from a {@link DeploymentServiceDto},
     * such as be number of instances, hardware pack, filesystem from product, service definition.
     *
     * @param deploymentServiceDto {@link DeploymentServiceDto}
     * @param env           environment
     */
    void updateServiceFromDto(DeploymentServiceDto deploymentServiceDto, String env);

    /**
     * Update service action dto
     * @param serviceStateDTO dto to update
     * @param serviceId serviceId to update
     */
    void updateServiceState(ServiceStateDTO serviceStateDTO, Integer serviceId);

    /**
     * Update subsystem state
     *
     * @param subsystemStateDTO dto with the state
     * @param subsystemId       id of the deployment subsystem to update
     * @throws NovaException thrown if deployment subsystem does not exist
     */
    void updateSubsystemState(SubsystemStateDTO subsystemStateDTO, Integer subsystemId) throws NovaException;

    /**
     * Update deploymentPlan state
     *
     * @param deploymentStatusAction deployment status to update
     * @param deploymentId deploymentId to update
     */
    void updateDeploymentPlanState(DeploymentStateDTO deploymentStatusAction, Integer deploymentId);

    /**
     * Gets possible configuration management options
     *
     * @param deploymentPlanId deployment plan identifier
     * @return get all possible configuration management
     */
    TaskRequestDTO[] getAllConfigurationManagement(Integer deploymentPlanId);

    /**
     * Get all release version service id of given deployment plan
     *
     * @param deploymentPlan deployment plan identifier
     * @return list with all identifiers
     */
    List<Integer> getAllReleaseVersionService(DeploymentPlan deploymentPlan);

    /**
     * Unschedule a nova planned
     *
     * @param ivUser       the User
     * @param deploymentId the deploypment plan Id
     */
    void unschedule(String ivUser, Integer deploymentId);

}
