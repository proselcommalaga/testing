package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatus;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentChangeDtoPage;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDtoPage;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentInstanceStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardStatusesDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSubsystemStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSummaryDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentTypeChangeDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.LMLibraryRequirementsFulfilledDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.MinimalDeploymentSubsystemStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemServicesStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.SubsystemStateDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.TaskRequestDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * Interface of DeploymentsAPI
 */
public interface IDeploymentsApiService
{
    /**
     * Get all deployments plans of a product on an environment.
     *
     * @param ivUser      User code.
     * @param productId   The ID of the product the deployments plans belongs to.
     * @param environment The environment such as INT, PRE and PRO.
     * @param status      If true, get only plans deployed on the environment. If false,                    get all.
     * @return array of DeploymentDto
     */
    DeploymentDto[] getDeploymentPlansByEnvironment(String ivUser, Integer productId, String environment, String status);

    /**
     * Get all deployments plans of a product,
     * filtered by environments and statuses,
     * sorted by environment and creation date desc,
     * and paginated
     *
     * @param ivUser       User code
     * @param productId    The ID of the product the deployments plans belong to.
     * @param environments The environments to search
     * @param statuses     The plan statuses to search
     * @param pageSize     Page size
     * @param pageNumber   Page number
     * @return DeploymentDtoPage object
     */
    DeploymentDtoPage getDeploymentPlansByEnvironmentsAndStatuses(String ivUser, Integer productId, String environments, String statuses, Integer pageSize, Integer pageNumber);

    /**
     * Gets a {@link DeploymentService} from its ID.
     *
     * @param deploymentServiceId - the service Id
     * @param deploymentId        - Deployment plan Id
     * @return - a {@link DeploymentServiceDto}
     * @throws NovaException error
     */
    DeploymentServiceDto getDeploymentService(Integer deploymentServiceId, Integer deploymentId) throws NovaException;


    /**
     * Get the status of a service as DTO
     *
     * @param subsystemId  subsystem ID
     * @param serviceId    service ID
     * @param deploymentId deployment ID
     * @param environment  environment
     * @return Service status
     * @throws NovaException Errors
     */
    ServiceStatusDTO getServiceStatus(Integer subsystemId, Integer serviceId, Integer deploymentId, String environment);

    /**
     * Changes the {@link com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType} of a {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}
     * and creates a {@link com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask}.
     *
     * @param ivUser       User code.
     * @param changeType   DTO with the change type.
     * @param deploymentId The ID of the deployment plan to getFilesystemToDelete.
     * @return todoTask response
     */
    TodoTaskResponseDTO changeDeploymentType(String ivUser, DeploymentTypeChangeDto changeType, Integer deploymentId);

    /**
     * Get deployment plan status
     *
     * @param deploymentId deployment id
     * @return action status
     * @throws NovaException on error
     */
    ActionStatus getDeploymentPlanStatus(Integer deploymentId) throws NovaException;

    /**
     * Promotes a plan to another environment. Promotion creates a copy of the
     * original plan into the given environment and modifies it following some
     * business rules, depending on the environment. <br>
     * Promote from INT to PRE:
     * <ul>
     * <li>Copy will loose hardware and filesystem.</li>
     * <li>Environment dependant properties will be cleared.</li>
     * </ul>
     * <p>
     * Promote from PRE to PRO:
     * <ul>
     * <li>Hardware will be the same as in PRE.</li>
     * <li>Filesystem will be the one in PRO, cloned from PRE.</li>
     * <li>Environment dependant properties will be cleared.</li>
     * </ul>
     *
     * @param ivUser       user
     * @param deploymentId The ID of the {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}.
     * @param environment  Environment to promote plan to.
     */
    void promotePlanToEnvironment(String ivUser, Integer deploymentId, String environment);

    /**
     * Método invocado por el TaskService cuando se ejecute/rechace una acción.
     *
     * @param taskId       - Task ID
     * @param deploymentId - {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}
     * @param status       - the new status
     */
    void onTaskReply(Integer taskId, Integer deploymentId, String status);

    /**
     * Get a DeploymentServiceDTO by Name
     *
     * @param environment   Environment
     * @param serviceName   Service name
     * @param subsystemName Subsystem name
     * @param releaseName   Release name
     * @param productName   Product name
     * @return Deployment service
     */
    DeploymentServiceDto getDeploymentServiceByName(String environment, String serviceName,
                                                    String subsystemName, String releaseName, String productName);

    /**
     * get a SubsystemServiceStatusDTO
     *
     * @param subsystemId  id of subsystem
     * @param deploymentId id of deploymentPlan
     * @param environment  environment
     * @return SubsystemServicesStatusDTO deployment subsystem services status
     */
    SubsystemServicesStatusDTO getDeploymentSubsystemServicesStatus(Integer subsystemId, Integer deploymentId, String environment);

    /**
     * Updates a {@link DeploymentService} from a {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}.
     *
     * @param ivUser              user
     * @param service             DTO with the values to update.
     * @param deploymentServiceId The ID of the service from this deployment plan.
     * @param deploymentId        The ID of this deployment plan.
     */
    void updateDeploymentService(String ivUser, DeploymentServiceDto service, Integer deploymentServiceId, Integer deploymentId);

    /**
     * Get the deployment plan card statuses
     * Get the running / stopped services for the deployment plan
     *
     * @param deploymentPlanId the deployment id
     * @param userCode         the iv user for checking permissions
     * @return a Deployment plan card statuses dto
     * @throws NovaException if any error or validation
     */
    DeploymentPlanCardStatusesDto getDeploymentPlanCardStatuses(Integer deploymentPlanId, String userCode) throws NovaException;

    /**
     * Get the deployment subsystem status dto
     *
     * @param deploymentPlanId the deployment plan id
     * @param userCode         the iv user for checking permissions
     * @return a deployment subsystem status dto array
     * @throws NovaException if any error
     */
    DeploymentSubsystemStatusDto[] getDeploymentSubsystemServiceCardStatuses(Integer deploymentPlanId, String userCode) throws NovaException;

    /**
     * Get the deployment instance status
     *
     * @param deploymentServiceId the deployment service id
     * @param userCode            the iv user for checking permissions
     * @return a deployment instance status
     * @throws NovaException if any error
     */
    DeploymentInstanceStatusDto[] getDeploymentInstanceCardStatuses(Integer deploymentServiceId, String userCode) throws NovaException;

    /**
     * Get the action status of the deployment plan id
     *
     * @param deploymentPlanId the deployment plan id
     * @return a Action status of deployment plan
     * @throws NovaException if any error
     */
    ActionStatusDTO getDeploymentPlanCardActionStatus(Integer deploymentPlanId) throws NovaException;

    /**
     * Get the action status of the deployment subsystem id
     *
     * @param deploymentSubsystemId the deployment subsystem id
     * @return a Action status of deployment subsystem
     * @throws NovaException if any error
     */
    ActionStatusDTO getDeploymentSubsystemCardActionStatus(Integer deploymentSubsystemId) throws NovaException;

    /**
     * Get the action status of the deployment subsystem id
     *
     * @param deploymentServiceId the deployment service id
     * @return a Action status of deployment service
     * @throws NovaException if any error
     */
    ActionStatusDTO getDeploymentServiceCardActionStatus(Integer deploymentServiceId) throws NovaException;

    /**
     * Get the action status of the deployment instance id
     *
     * @param deploymentInstanceId a deployment instance id
     * @return a Action status of deployment instance
     * @throws NovaException if any error
     */
    ActionStatusDTO getDeploymentInstanceCardActionStatus(Integer deploymentInstanceId) throws NovaException;

    /**
     * Get a deployment subsystem card status after refreshing
     *
     * @param userCode              the user code
     * @param deploymentSubsystemId the deployment subsystem id
     * @return a Minimal deployment subsystem status dto
     */
    MinimalDeploymentSubsystemStatusDto getDeploymentSubsystemCardRefresh(String userCode, Integer deploymentSubsystemId) throws NovaException;

    /**
     * Get deployment service card status after refreshing
     *
     * @param deploymentServiceId the deployment service id
     * @param userCode            the user code
     * @return a DeploymentServiceStatusDto
     * @throws NovaException if any error
     */
    DeploymentServiceStatusDto getDeploymentServiceCardRefresh(String userCode, Integer deploymentServiceId) throws NovaException;

    /**
     * Get deployment instance card status after refreshing
     *
     * @param deploymentInstanceId deploymentInstanceId
     * @param userCode             the user code
     * @return a Deployment instance status dto
     * @throws NovaException if any error
     */
    DeploymentInstanceStatusDto getDeploymentInstanceCardRefresh(String userCode, Integer deploymentInstanceId) throws NovaException;

    /**
     * Uncshedule a nova planned deployment
     *
     * @param ivUser       user requester
     * @param deploymentId The id of deployment
     */
    void unschedule(String ivUser, Integer deploymentId);


    /**
     * Updates a {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}. Only will update set data.
     *
     * @param ivUser        user
     * @param deploymentDto {@link DeploymentDto} with the values to update.
     * @param deploymentId  The ID of the {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}.
     */
    void updateDeploymentPlan(String ivUser, DeploymentDto deploymentDto, Integer deploymentId);

    /**
     * Update Service state
     *
     * @param serviceStateDTO service status to update
     * @param serviceId       serviceId to update
     */
    void updateServiceState(ServiceStateDTO serviceStateDTO, Integer serviceId);

    /**
     * Update deployment subsystem state
     *
     * @param subsystemStateDTO new subsystem state
     * @param subsystemId       id of the deployment subsystem to update
     */
    void updateSubsystemState(SubsystemStateDTO subsystemStateDTO, Integer subsystemId);

    /**
     * Update deploymentPlan state
     *
     * @param deploymentStatusAction deployment status to update
     * @param deploymentId           deploymentId to update
     */
    void updateDeploymentPlanState(DeploymentStateDTO deploymentStatusAction, Integer deploymentId);

    /**
     * Migrates an original {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan} from a {@link com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion}
     * to a new {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan} which the same {@link com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion} or
     * a different one.
     *
     * @param ivUser       user
     * @param deploymentId The ID of the deployment plan to migrate from.
     * @param versionId    The ID of the release version of the new plan.
     * @return DeploymentMigrationDto deployment migration dto
     */
    DeploymentMigrationDto migratePlan(String ivUser, Integer deploymentId, Integer versionId);

    /**
     * Retrieves the changes history from a {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}.
     *
     * @param deploymentId The ID of the deployment plan.
     * @param pageSize     with the page size
     * @param pageNumber   with the page number
     * @return a {@link DeploymentChangeDtoPage}
     */
    DeploymentChangeDtoPage getHistory(Integer deploymentId, Long pageSize, Long pageNumber);

    /**
     * Add a deployment change
     *
     * @param change       change to add
     * @param deploymentId id of deployment plan
     */
    void addDeploymentChange(DeploymentChangeDto change, Integer deploymentId);

    /**
     * Deletes a {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan}
     *
     * @param user         user
     * @param deploymentId The ID of the deployment plan to getFilesystemToDelete.
     */
    void deletePlan(String user, Integer deploymentId);

    /**
     * Get deployment subsystem status
     *
     * @param subsystemId subsystem id
     * @return action status
     */
    ActionStatus getDeploymentSubsystemStatus(Integer subsystemId);

    /**
     * Get deployment plan by environment and filter
     *
     * @param ivUser      user
     * @param productId   id of the product
     * @param environment environment
     * @param endDate     end date
     * @param startDate   start date
     * @param status      status
     * @return array of {@link DeploymentSummaryDto}
     */
    DeploymentSummaryDto[] getDeploymentPlansByEnvironmentAndFilters(String ivUser, Integer productId,
                                                                     String environment, String endDate,
                                                                     String startDate, String status);

    /**
     * Archive a {@link com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan} that was never deployed.
     *
     * @param user         user
     * @param deploymentId The ID of the deployment plan.
     */
    void archiveDeploymentPlan(String user, Integer deploymentId);

    /**
     * Copies a given plan into a new one. The plan copy is identical
     * to the original plan. It will have the same services, resources,
     * and filesystems.
     *
     * @param user         user
     * @param deploymentId The ID of the deployment plan to copy from.
     * @return Success deployment dto
     */
    DeploymentDto copyPlan(String user, Integer deploymentId);

    /**
     * Gets all data from a DeploymentPlan given its ID.
     *
     * @param user         user
     * @param deploymentId The deployment plan ID.
     * @return {@link DeploymentDto}
     */
    DeploymentDto getDeploymentPlan(String user, Integer deploymentId);

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
     * @param user         user
     * @param deploymentId The ID of the deployment plan to deploy.
     * @param force        True to force deployment.
     * @return todoTask response
     * @throws NovaException on error or when to do task created
     */
    TodoTaskResponseDTO deploy(String user, Integer deploymentId, Boolean force) throws NovaException;

    /**
     * Create a new version deployment plan for the release version.
     *
     * @param user             - user
     * @param releaseVersionId - the release version Id.
     * @param environment      - the environment
     * @return deploymentSummaryDto   - deployment plan created
     */
    DeploymentSummaryDto createDeploymentPlan(String user, Integer releaseVersionId, String environment);

    /**
     * Get a Service Status by deployment plan into an environment
     *
     * @param deploymentId deployment plan
     * @param environment  environment
     * @return StatusDTO deployment plan services status
     */
    StatusDTO getDeploymentPlanServicesStatus(Integer deploymentId, String environment);

    /**
     * Get all configuration of a deployment plan
     *
     * @param deploymentId deployment id
     * @return TaskRequestDTO[] task request dto [ ]
     */
    TaskRequestDTO[] getAllConfigurationManagement(Integer deploymentId);

    /**
     * Request a configuration property task in deployment
     *
     * @param user         user
     * @param task         - {@link com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask}
     * @param deploymentId - {@link DeploymentPlan} ID.
     */
    void taskRequest(String user, TaskRequestDTO[] task, Integer deploymentId);

    /**
     * Get deployment instance status
     *
     * @param instanceId instance id
     * @return action status
     */
    ActionStatus getDeploymentInstanceStatus(Integer instanceId);

    /**
     * update status of a service
     *
     * @param deploymentServiceId id of service
     * @param statusMessage       status message
     * @param status              status
     */
    void instanceDeployStatus(Integer deploymentServiceId, String statusMessage, String status);

    /**
     * get status of a DeploymentPlanSubsystem
     *
     * @param deploymentPlanId deployment plan
     * @param environment      environment
     * @return an array of {@link SubsystemServicesStatusDTO}
     */
    SubsystemServicesStatusDTO[] getDeploymentPlanSubsystemsServicesStatus(Integer deploymentPlanId, String environment);

    /**
     * Get deployment service status
     *
     * @param serviceId service id
     * @return action status
     */
    ActionStatus getDeploymentServiceStatus(Integer serviceId);

    /**
     * Remove (undeploy) a deployment plan.
     *
     * @param user         user
     * @param deploymentId The ID of the deployment plan to undeploy.
     * @return todoTask response
     * @throws NovaException error
     */
    TodoTaskResponseDTO remove(String user, Integer deploymentId) throws NovaException;

    /**
     * Given a failed deploy plan, treat it generating a TodoTask of DeployError.
     *
     * @param user         user who requests deploy
     * @param deploymentId id of plan which deploy fails
     * @param exception    exception catch during deploy
     */
    void treatDeployError(String user, Integer deploymentId, Exception exception);

    /**
     * Gets deployment plans by search text filter.
     *
     * @param ivUser     the iv user
     * @param productId  the product id
     * @param statuses   the statuses
     * @param searchText The text to search
     * @param pageSize   the page size
     * @param pageNumber the page number
     * @return the deployment plans by search text filter
     */
    DeploymentDtoPage getDeploymentPlansBySearchTextFilter(String ivUser, Integer productId, String statuses, String searchText, Integer pageSize, Integer pageNumber);

    /**
     * Get the list of available platforms (NOVA, ETHER).
     *
     * @return An array of String containing the list of available platforms (NOVA, ETHER).
     */
    String[] getPlatforms();

    /**
     * Get minimum requirements of a list of deployment services for libraries or services
     *
     * @param deploymentServiceIds deployment service id list of libraries or services
     * @return DTO with minimum requirements
     */
    LMLibraryRequirementsFulfilledDTO[] getAllRequirementsOfUsedLibraries(int[] deploymentServiceIds);

    /**
     * Get minimum requirements of all the services of a deployment plan ids array
     *
     * @param deploymentIds deployment plan ids array
     * @return DTO with minimum requirements for all the deployment plans
     */
    DeploymentPlanLibraryRequirementsDTO[] getAllRequirementsOfUsedLibrariesForPlans(int[] deploymentIds);

    /**
     * Get Service Type
     *
     * @return An array containing Service Types
     */
    String[] getServiceGroupingNames(final String filterByDeployed);

    /**
     * Get Subsystem Type
     *
     * @return An array containing Subsystem Types
     */
    String[] getSubsystemTypes();

    /**
     * Get Deployment Status
     *
     * @return An array containing Deployment Status
     */
    String[] getDeploymentStatus();

    /**
     * Get Language versions
     *
     * @return An array containing languages versions
     */
    String[] getLanguagesVersions();

}
