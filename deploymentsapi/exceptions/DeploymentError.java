package com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants.DeployErrors;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Deployment error.
 *
 * @author xe30000
 */
@ExposeErrorCodes
public class DeploymentError
{

    /**
     * Unexpected error
     *
     * @return a NovaError
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.UNEXPECTED_ERROR,
                "Unexpected internal error",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * NovaError for a wrong release version
     *
     * @return a NovaError
     */
    public static NovaError getWrongReleaseVersionError()
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.WRONG_RELEASE_VERSION_ERROR,
                "The release version has errors and can't be deployed",
                "Use a right release version",
                HttpStatus.NOT_ACCEPTABLE,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for not ready release version
     *
     * @return a NovaError
     */
    public static NovaError getNotReadyReleaseVersionError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NOT_READY_RELEASE_VERSION,
                "The release version is not ready to deploy",
                "Wait the version to be ready",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for no found deployment plan
     *
     * @return a NovaError
     */
    public static NovaError getNoSuchDeploymentError()
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.NO_SUCH_DEPLOYMENT,
                "The deployment plan couldn't be found. This deployment plan has been moved or deleted.",
                "Please, refresh and update the page for loading the current deployment plan into the environment",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for Tried to update deployed plan
     *
     * @return a NovaError
     */
    public static NovaError getTriedToUpdateDeployedPlanError()
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.TRIED_TO_UPDATE_DEPLOYED_PLAN,
                "Cannot modify or update a deployment plan that is already deployed, archived or undeployed.",
                "Try to create an copy of the plan and make changes to a newly created plan.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for Wrong Task Type
     *
     * @return a NovaError
     */
    public static NovaError getWrongTaskTypeError()
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.WRONG_TASK_TYPE,
                "Error trying to create a TodoTask. The Task Type is not a Deployment Task Type",
                "Use a right task Type",
                HttpStatus.NOT_ACCEPTABLE,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for create task
     *
     * @return a NovaError
     */
    public static NovaError getCreateTaskError(String errorMessage)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.ERROR_IN_CREATETASK,
                "Error trying to create a TodoTask due to the to do task service has failed. Error message: [" + errorMessage + "]",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError for AlreadyPendingTaskInCreateTaskError
     *
     * @return a NovaError
     */
    public static NovaError getAlreadyPendingTaskInCreateTaskError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.ALREADY_PENDINGTASK_IN_CREATETASK,
                "Trying to create a new task when there is a previous task of same type pending to answer",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for free slots
     *
     * @param deploymentPlanId the deployment plan id
     * @param environment      the environment
     * @param releaseName      the release name
     * @return a NovaError
     */
    public static NovaError getNoSlotsError(final Integer deploymentPlanId, final String environment, final String releaseName)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SLOTS,
                "All slots are busy. There are no slots available for deploying a new deployment plan with id: [" + deploymentPlanId + "] in the environment: [" + environment + "] of the release " +
                        "name: [" + releaseName + "]",
                "Please, before deploying, you must undeploy one of the deployment plan deployed in this environment and on this way you will get free one slot",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for Budget error
     *
     * @return a NovaError
     */
    public static NovaError getBudgetError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.BUDGET_ERROR,
                "The plan cannot be deployed because of its budget",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for deployment plan not ready
     *
     * @param currentStatus    the current status
     * @param deploymentPlanId the deployment plan id
     * @return a NovaError
     */
    public static NovaError getPlanNotReadyError(final Integer deploymentPlanId, final String currentStatus)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.PLAN_NOT_READY,
                "The deployment plan with id: [" + deploymentPlanId + "] is not ready for deploying. Current status: [" + currentStatus + "]. ",
                "Please, refresh and update the page for loading the current deployment plan into the environment. For deploying a deployment plan, the deployment plan status can only be: [DEFINITION] or [SCHEDULED]",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for service not Found
     *
     * @param deploymentServiceId the deployment service id
     * @return a NovaError
     */
    public static NovaError getNoSuchServiceError(final Integer deploymentServiceId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SUCH_SERVICE,
                "The deployment service id: [" + deploymentServiceId + "] couldn't be found into NOVA Platform.",
                "Please, refresh and update the page for loading the current deployment plan and deployment services into the environment.",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for Tried to copy plan from INT environment to a wrong environment
     *
     * @return a NovaError
     */
    public static NovaError getTriedToPromoteIntPlanToWrongEnvironmentError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_PROMOTE_INT_PLAN_TO_WRONG_ENVIRONMENT,
                "Tried to copy plan from INT environment to a wrong environment",
                "Plans from INT can only be copied to PRE",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for tried to promote plan from PRE to wrong environment
     *
     * @return a NovaError
     */
    public static NovaError getTriedToPromotePrePlanToWrongEnvironmentError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_PROMOTE_PRE_PLAN_TO_WRONG_ENVIRONMENT,
                "Tried to copy plan from PRE environment to a wrong environment",
                "Plans from PRE can only be copied to PRO",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for tried to pomoted not deployed plan
     *
     * @return a NovaError
     */
    public static NovaError getTriedToPromoteNotDeployedPlanError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_PROMOTE_NOT_DEPLOYED_PLAN,
                "Tried to promote a plan that is not deployed",
                "Only deployed plans can be promoted to another environment",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for tried to deleted deployed plan
     *
     * @return a NovaError
     */
    public static NovaError getTriedToDeleteDeployedPlanError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_DELETE_DEPLOYED_PLAN,
                "Tried to delete a plan that is not archived",
                "Only archived plans can be deleted",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for tried to delete plan which has a child in process
     *
     * @param deletingPlanId          Id of plan that is being deleted
     * @param childrenInProcessAsText String containing the children plans in process
     * @return a NovaError
     */
    public static NovaError getTriedToDeleteDeploymentPlanWithProcessingChildren(final Integer deletingPlanId, final String childrenInProcessAsText)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_DELETE_PLAN_WITH_PROCESSING_CHILDREN,
                String.format("You cannot delete the deployment plan id: [%s] due to it has 'children plans' with some action in progress: %s", deletingPlanId, childrenInProcessAsText),
                "Please, wait until the children deployment plans (for intance, copied or promoted deployment plans from this) finish their actions. Once finished, you can already delete this deployment plan",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for saving configuration failed
     *
     * @return a NovaError
     */
    public static NovaError getSavingConfigurationsError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.ERROR_SAVING_CONFIGURATIONS,
                "Error saving configurations of a service to the configuration manager",
                "There was an error on the configuration manager or it is down",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for deployment type change
     *
     * @return a NovaError
     */
    public static NovaError getDeploymentTypeChangePendingError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_TYPE_CHANGE_PENDING,
                "There's already an unresolved task of deployment type change",
                "A new request of changing the deployment type cannot be made while there is another still unresolved",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for deployment request pending
     *
     * @param deploymentPlanId the deployment plan id
     * @param environment      the environment
     * @param todoTaskType     the to do task type
     * @return a NovaError
     */
    public static NovaError getDeploymentRequestPendingError(final Integer deploymentPlanId, final String todoTaskType, final String environment)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_REQUEST_PENDING,
                "There's already an unresolved to do task of type: [" + todoTaskType + "] of deployment request on the environment: [" + environment + "] for this deployment plan id: [" + deploymentPlanId + "]",
                "A new request of deployment cannot be made while there is another still unresolved",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for undeployment request pending
     *
     * @return a NovaError
     */
    public static NovaError getUndeploymentRequestPendingError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.UNDEPLOYMENT_REQUEST_PENDING,
                "There's already an unresolved task of undeployment request on the environment",
                "A new request of undeployment cannot be made while there is another still unresolved",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for trying to update service in pro
     *
     * @return a NovaError
     */
    public static NovaError getTriedToUpdateServiceInProError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_UPDATE_SERVICE_IN_PRO,
                "Tried to update service of a plan in PRO",
                "A service from a plan in PRO environment cannot be changed",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for trying to copy plan in pro when release platform has changed
     *
     * @return a NovaError
     */
    public static NovaError getPlatformConflictError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PLATFORM_CONFLICT,
                "Tried to copy plan in production environment after having changed the selected platform of the release",
                "It's not allowed to copy a plan from a platform to a different platform in the production environment. Change release configuration in order to have the same platform.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for plan without current revision
     *
     * @return a NovaError
     */
    public static NovaError getPlanWithoutCurrentVersionError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PLAN_WITHOUT_CURRENT_REVISION,
                "The given plan has no current configuration revision",
                "All plans must have a current revision",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for product without sqa canon
     *
     * @return a NovaError
     */
    public static NovaError getProductWithoutSQACanonError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PRODUCT_WITHOUT_SQA_CANON,
                "The operation cannot be done because the product has no SQA services GB", "Add the SQA services GBs to the budget of the product", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for product without canon
     *
     * @return a NovaError
     */
    public static NovaError getProductWithoutNovaServicesCanonError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PRODUCT_WITHOUT_NOVA_SERVICES_CANON,
                "The operation cannot be done because the product has no NOVA services GB", "Add the NOVA services GBs to the budget of the product", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for env not found
     *
     * @return a NovaError
     */
    public static NovaError getEnvironmentNotFoundError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.ENVIRONMENT_NOT_FOUND,
                "Environment not found", "Check environment name", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for plan without fs in pro
     *
     * @return a NovaError
     */
    public static NovaError getTriedToPromotePlanWithoutFilesystemInProError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_PROMOTE_PLAN_WITHOUT_FILESYSTEM_IN_PRO,
                "Tried to promote a plan from PRE that has no filesystems in PRO", "At least a service from the plan has a filesystem in PRE that doesn't exist in PRO", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for release version error storing
     *
     * @return a NovaError
     */
    public static NovaError getStoredReleaseVersionError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.STORED_RELEASE_VERSION,
                "Deployment plan belongs to a stored release version", "Restore the release version to copy the plan", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for release version error storing
     *
     * @return a NovaError
     */
    public static NovaError getPlanWithEphoenixDevelopmentEnvironmentNotPromotable()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PROMOTE_PLAN_DEPLOYMENT_DEVELOPMENT_EPHOENIX_NOT_PROMOTABLE,
                "Promotion not allowed. Cannot promote due of the plan contains ePhoenix services only for development environment.", "Promote a plan without ePhoenix services with development environment", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for max plan reached
     *
     * @param environment        the environment
     * @param maxDeploymentsPlan the maximun deployments plans allowed in the environment
     * @param productId          the product id
     * @return a NovaError
     */
    public static NovaError getMaxPlanReachedError(final Integer productId, final String environment, final Integer maxDeploymentsPlan)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.MAX_PLAN_REACHED,
                "Cannot create a new deployment plan due to the product id: [" + productId + " has reached the maximum allowed deployments plan: [" + maxDeploymentsPlan + "] in the environment: [" + environment + "]",
                "Please, for creating or copying a new deployment plan in the environment: [" + environment + "], you must first storage or delete some deployment plan of this environment",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for scheduled plan error
     *
     * @return a NovaError
     */
    public static NovaError getScheduledPlanError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.SCHEDULED_PLAN,
                "The plan is already scheduled", "Wait till the plan is deployed", HttpStatus.CONFLICT, ErrorMessageType.WARNING);
    }

    /**
     * NovaError for undeployed plan is not valid
     *
     * @return a NovaError
     */
    public static NovaError getUndeployPlanIsNotValidError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.UNDEPLOY_PLAN_IS_NOT_VALID,
                "The selected plan to undeploy is a not valid plan to undeploy. It is not a PRO plan or does not belongs to the selected plan", "Choose another plan and ensure that is valid to undeploy", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for trying to promote a plan without logical connector
     *
     * @return a NovaError
     */
    public static NovaError getTriedToPromotePlanWithoutLogicalConnectorInProError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_PROMOTE_PLAN_WITHOUT_LOGICAL_CONNECTOR_IN_PRO,
                "Tried to promote a plan from PRE that has no logical connector in PRO", "At least a service from the plan has a logical connector in PRE that doesn't exist in PRO", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for undeploy plan not found
     *
     * @return a NovaError
     */
    public static NovaError getUndeployPlanNotFoundError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.UNDEPLOY_PlAN_NOT_FOUND,
                "The selected plan to undeploy could not be found", "Choose a valid plan", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for priority level not valid
     *
     * @return a NovaError
     */
    public static NovaError getPriorityLevelNotValidError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PRIORITY_LEVEL_NOT_VALID,
                "The actual priority level for the schedule planning is not valid", "Set a valid priority level", HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for PLAN not found
     *
     * @param deploymentPlanId the deployment plan id
     * @return a NovaError
     */
    public static NovaError getPlanNotFoundError(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PLAN_NOT_FOUND,
                "The deployment plan with id: [" + deploymentPlanId + "] has not been found into NOVA BBDD.",
                "The deployment plan has been deleted. Please, refresh and update the page for loading the current deployment plan into the environment",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for PLAN not found for getting status
     * Throw an warning error
     *
     * @param deploymentPlanId the deployment plan id
     * @return a NovaError
     */
    public static NovaError getPlanNotFoundWarning(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PLAN_NOT_FOUND,
                "The deployment plan with id: [" + deploymentPlanId + "] has not been found. This deployment plan has been moved or deleted.",
                "Please, refresh and update the page for loading the current deployment plan into the environment",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for PLAN in NOT DEPLOYED status
     *
     * @param deploymentPlanId the deployment plan id
     * @return a NovaError
     */
    public static NovaError getPlanDeployedNotFoundError(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_PLAN_DEPLOYED_NOT_FOUND,
                "The status of the deployment plan id: [" + deploymentPlanId + "] has been changed and the deployment plan is NOT ready.",
                "Please, refresh/update the page for loading the current deployment plan into the environment and wait until the deployment plan ends the current action.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError for invalid date
     *
     * @return a NovaError
     */
    public static NovaError getInvalidDateFormatError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.INVALID_DATE_FORMAT,
                "Format date not valid or empty", "Check the date format", HttpStatus.NOT_ACCEPTABLE, ErrorMessageType.WARNING);
    }

    /**
     * NovaError for project jira key not found
     *
     * @return a NovaError
     */
    public static NovaError getNoSuchJiraProjectKeyError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SUCH_JIRA_PROJECT_KEY,
                "The product does not have a Jira Project Key",
                "Contact to JIRA team for getting a Jira project key and set it into this NOVA product",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for tried to archive plan error
     *
     * @return a NovaError
     */
    public static NovaError getTriedToArchivePlanError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_ARCHIVE_PLAN,
                "The plan is deployed or ready to deploy", "Undeploy the plan or delete the plan instead of archive",
                HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }


    /**
     * NovaError for date denied error
     *
     * @param currentDate            the current date
     * @param deploymentPlanId       the deployment plan id
     * @param expectedDeploymentDate the expected deployment date
     * @return a NovaError
     */
    public static NovaError getPlanDateDeniedError(final Integer deploymentPlanId, final String expectedDeploymentDate, final String currentDate)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PLAN_DATE_DENIED,
                "The deployment plan scheduled with Control-M with id: [" + deploymentPlanId + "] cannot be deployed due to the current date: [" + currentDate + "] is earlier than the expected deployment date: [" + expectedDeploymentDate + "]" +
                        " configured on the deployment plan.",
                "The expected deployment date must be equal or greater than the current date.",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for Cannot promote due is mandatory to start it all your services before promoting
     *
     * @return a NovaError
     */
    public static NovaError getNotAllStartedServicesForPromotingError(String notStartedServices)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.SERVICES_NOT_STARTED,
                "Cannot promote due is mandatory to start these services before promoting: " + notStartedServices, "First, run all specified services in the environment before trying to promote", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getNotAllStartedServicesForCopyError(String notStartedServices, Environment environment)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PENDING_TO_START_SERVICES_IN_COPY_OPERATION,
                "Cannot copy due is mandatory to start the release version services [" + notStartedServices + "] in the " + environment + " environment before copying the plan", "First, run all the specified services before trying to copy", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getNotPromotingError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.TRIED_TO_COPY_IN_PRO_WITHOUT_PROMOTING_FIRST,
                "Cannot copy due is mandatory to promote the deployment plan first. And remember that in Production environment, copies of deployment plans, is only allowed for releases version of the same release version",
                "First at all, copy de deployment plan of desired release version in PRE environment and after, you can be able to promote it to Production environment", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    /**
     * NovaError for subsystem not found
     *
     * @param deploymentSubsystemId deployment subsystem id
     * @return a NovaError
     */
    public static NovaError getSubsystemNotFoundError(final Integer deploymentSubsystemId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.SUBSYSTEM_NOT_FOUND,
                "The deployment subsystem id: [" + deploymentSubsystemId + "] could not found into NOVA Platform. The deployment plan associated could have been deleted.",
                "Please, refresh and update the page for loading the current deployment plan and deployment subsystem into the environment.",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for deployment instance not found
     *
     * @param deploymentInstanceId the deployment instance id
     * @return a NovaError
     */
    public static NovaError getDeploymentInstanceNotFoundError(final Integer deploymentInstanceId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_INSTANCE_NOT_FOUND,
                "Deployment instance id: [" + deploymentInstanceId + "] has not been found into NOVA BBDD. The deployment plan associated could have been deleted.",
                "Please, refresh and update the page for loading the current deployment plan into the environment",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for deployment context params
     *
     * @return a NovaError
     */
    public static NovaError getDeploymentContextParamsError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_CONTEXT_PARAMS_ERROR,
                "The context params for the batch scheduler service have not been created", "Check the Scheduler Manager service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for deployment plan for batch scheduler service not created
     *
     * @return a NovaError
     */
    public static NovaError getDeploymentBatchScheduleError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_BATCH_SCHEDULE_ERROR,
                "The deployment plan for batch scheduler service have not been created", "Check the Scheduler Manager service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for updating of deployment plan for batch scheduler service have failed
     *
     * @return a NovaError
     */
    public static NovaError getUpdateDeploymentBatchScheduleError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.UPDATE_DEPLOYMENT_BATCH_SCHEDULE_ERROR,
                "The update of deployment plan for batch scheduler service have failed", "Check the Scheduler Manager service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for removing of deployment plan for batch scheduler service have failed
     *
     * @return a NovaError
     */
    public static NovaError getRemoveDeploymentBatchScheduleError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.REMOVE_DEPLOYMENT_BATCH_SCHEDULE_ERROR,
                "The remove of deployment plan for batch scheduler service have failed", "Check the Scheduler Manager service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for removing deployment context param failed
     *
     * @return a NovaError
     */
    public static NovaError getRemoveDeploymentContextParamsError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.REMOVE_DEPLOYMENT_CONTEXT_PARAMS_ERROR,
                "The remove of deployment context params of the plan have failed", "Check the Scheduler Manager service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for error copy context params
     *
     * @return a NovaError
     */
    public static NovaError getCopyDeploymentContextParamsError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.COPY_DEPLOYMENT_CONTEXT_PARAMS_ERROR,
                "The copy of deployment context params of the plan have failed", "Check the Scheduler Manager service", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for Generating docker key
     *
     * @return a NovaError
     */
    public static NovaError getGeneratingDockerKeyError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.ERROR_GENERATING_DOCKER_KEY,
                "Error from ApiGateway service when generating the docker key", Constants.MSG_CONTACT_XMAS, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError for product not found
     *
     * @return a NovaError
     */
    public static NovaError getNoSuchProductError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SUCH_PRODUCT,
                "Error trying to get a product from NOVA DB", Constants.MSG_CONTACT_NOVA, HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }


    /**
     * NovaError: it is validates a future deployment date while scheduling, unlike PLAN_DATE_DENIED (064),
     * that validates a non-future deployment date while deploying
     *
     * @param currentDate            the current date
     * @param deploymentPlanId       the deployment plan id
     * @param expectedDeploymentDate the expected deployment date
     * @return a NovaError
     */
    public static NovaError getPlanDateDeniedForScheduleError(final Integer deploymentPlanId, final String expectedDeploymentDate, final String currentDate)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PLAN_DATE_DENIED_FOR_SCHEDULE,
                "The deployment plan scheduled with NOVA Planned with id: [" + deploymentPlanId + "] cannot be deployed due to the current date: [" + currentDate + "] does not match with the expected deployment date: [" + expectedDeploymentDate + "] configured on the deployment plan.",
                "The expected deployment date must be equal or greater than the current date.",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for invalid nova planned error
     *
     * @param deploymentPlanDate   the deployment date
     * @param deploymentPlanId     the deployment plan id
     * @param deploymentPlanStatus the deployment plan status
     * @param deploymentType       the deployment type
     * @return a NovaError
     */
    public static NovaError getInvalidNovaPlannedPlanError(final Integer deploymentPlanId, final String deploymentType, final String deploymentPlanStatus, final String deploymentPlanDate)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.INVALID_NOVA_PLANNED_PASS,
                "The deployment plan id: [" + deploymentPlanId + "] is not properly configured or the deployment plan is not NOVA Planned type. Deployment type: [" + deploymentType + "] - Deployment status: [" + deploymentPlanStatus + "] and " +
                        "deployment plan date set: [" + deploymentPlanDate + "] of this deployment plan",
                "Please, review and set the deployment configuration or this deployment plan",
                HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    /**
     * NovaError for service type error
     *
     * @param serviceType the service type
     * @return a NovaError
     */
    public static NovaError getServiceTypeError(final String serviceType)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.SERVICE_TYPE_NOT_ALLOWED,
                "This option is now allowed for service type: [" + serviceType + "]. This service does not have information for starting, restarting or stopping.",
                "The extra information about instances of this service is not compatible. Change NOVA Dashboard call",
                HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    /**
     * NovaError for invalid nova planned error
     *
     * @param cluster a cluster
     * @param cpd     the cpd. Could be N/D
     * @return a NovaError
     */
    public static NovaError getReportFromDeploymentManagerError(final String cpd, final String cluster)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.GET_REPORT_DEPLOYMENT_MANAGER_ERROR,
                "Error trying to get a report from deployment manager from CPD: [" + cpd + "]. Cluster: [" + cluster + "]",
                "The deployment manager service has failed. Wait and try it again later.",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Nova Error when fails calling scheduler manager to get batch scheduler service information in the undeploy use case
     *
     * @param deploymentPlanId          the deployment plan id
     * @param environment               the environment where the batch scheduler service is
     * @param batchSchedulerServiceName the service name of batch scheduler service
     * @param errorMessage              the error message with the reason of issue
     * @return a {@link NovaError}
     */
    public static NovaError getErrorBatchSchedulerServiceWhenUndeployPlan(final Integer deploymentPlanId, final String environment, final String batchSchedulerServiceName, final String errorMessage)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.INVALID_UNDEPLOY_WITH_BATCH_SCHEDULER_SERVICES,
                "Unable to get batch scheduler instance to undeploy de deployment plan id: [" + deploymentPlanId + "] on the environment: [" + environment + "] for batch scheduler service name: [" + batchSchedulerServiceName + "]" +
                        " due to: [" + errorMessage + "].",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.NOT_ACCEPTABLE, ErrorMessageType.ERROR);
    }

    public static NovaError getCreatePublicationError(String uuaa, String release, String environment, Errors outcome)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.CREATE_PUBLICATION_ERROR,
                MessageFormat.format("Error creating a publication from uuaa {0} with release {1} in environment {2}. Outcome: {3}",
                        uuaa, release, environment, outcome != null ? outcome.getBodyExceptionMessage() : ""),
                Constants.MSG_CONTACT_XMAS, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getUpdatePublicationError(String uuaa, String release, String environment, Errors outcome)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.UPDATE_PUBLICATION_ERROR,
                MessageFormat.format("Error updating a publication from uuaa {0} with release {1} in environment {2}. Outcome: {3}",
                        uuaa, release, environment, outcome != null ? outcome.getBodyExceptionMessage() : ""),
                Constants.MSG_CONTACT_XMAS, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getRemovePublicationError(String uuaa, String release, String environment, Errors outcome)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.REMOVE_PUBLICATION_ERROR,
                MessageFormat.format("Error removing a publication from uuaa {0} with release {1} in environment {2}. Outcome: {3}",
                        uuaa, release, environment, outcome != null ? outcome.getBodyExceptionMessage() : ""),
                Constants.MSG_CONTACT_XMAS, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getCreateProfilingError(String uuaa, String environment)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.CREATE_PROFILING_ERROR,
                MessageFormat.format("Error creating profiling for uuaa {0} in environment {1}", uuaa, environment),
                Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getRemoveProfilingError(String uuaa, String environment)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.REMOVE_PROFILING_ERROR,
                MessageFormat.format("Error removing profiling for uuaa {0} in environment {1}", uuaa, environment),
                Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    /**
     * NovaError for container not Found
     *
     * @return a NovaError
     */
    public static NovaError getNoSuchContainerError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SUCH_CONTAINER,
                "The deployment container couldn't be found",
                "Check if the deployment container does exist in the deployment plan",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError when trying to copy deployment logical connector
     *
     * @return a NovaError
     */
    public static NovaError getNoCopyDeploymentLogicalConnector()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SUCH_CONTAINER,
                "Error trying to copy a deployment plan, just when it is trying to copy the deployment logical connector. It does not found the same deployment service.",
                "Update and refresh the request due to the deployment plan was removed. Review the original plan to copy.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError when trying to replace a deployment plan
     *
     * @param newDeploymentPlanId new plan id
     * @param oldDeploymentPlanId replaced plan id
     * @return a NovaError
     */
    public static NovaError getReplacePlanChangingDeploymentPlatformError(final Integer newDeploymentPlanId, final String newDeploymentPlatform, final Integer oldDeploymentPlanId, final String oldDeploymentPlatform)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.REPLACE_PLAN_ERROR,
                "Impossible to replace into old deployment plan id: [" + oldDeploymentPlanId + "]:DeploymentPlatform selected: [" + oldDeploymentPlatform + "] " +
                        "to new one deployment plan id: [" + newDeploymentPlanId + "]:DeploymentPlatform selected: [" + newDeploymentPlatform + "] with different platforms selected",
                "Undeploy the old deployment plan with id: [" + oldDeploymentPlanId + "] and then deploy the new deployment plan with id: [" + newDeploymentPlanId + "].",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError when trying to replace a deployment plan
     *
     * @param newDeploymentPlanId new plan id
     * @param oldDeploymentPlanId replaced plan id
     * @return a NovaError
     */
    public static NovaError getForceReplacePlan(final Integer newDeploymentPlanId, final Integer oldDeploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.FORCE_REPLACE_PLAN,
                "Forcing to replace a deployment plan with id: [" + newDeploymentPlanId + "] over the old plan with id: [" + oldDeploymentPlanId + "] due the variable deploy:force = false.",
                "Update the web and try again later. Review if both plan exists.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }


    /**
     * NovaError when trying to promote to different deployment platform
     *
     * @param deploymentPlanId   that deployment plan id we want to promote
     * @param platformDeployType selected deployment platform
     * @return a new NovaError with the associated messages
     */
    public static NovaError getTriedToPromotePlanChangingDeploymentPlatform(final Integer deploymentPlanId, final String originalPlatformSelected, final String platformDeployType)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.PROMOTE_PLAN_ERROR,
                "You can not promote from PRE to PRO because the deployment infrastructure is not the same (it should be: NOVA-NOVA or ETHER-ETHER). It means, the deployment plan with id: [" + deploymentPlanId + "] (on PRE) has deployment infrastructure selected: [" + originalPlatformSelected + "] and PRO environment's infrastructure is: [" + platformDeployType + "].",
                "Please, check and ensure that the selected infrastructures are the same in both environments. Review: Your landing product section -> product configuration -> deployment infrastructure. If they are not the same, please contact to NOVA Team via JIRA:PNOVA and request to change the deployment infrastructure.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for user without enough authorization level
     *
     * @return a NovaError
     */
    public static NovaError getForbiddenError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.USER_PERMISSION_DENIED,
                "User does not have permission for the action requested",
                "Check if the user belongs to the group authorized for this action",
                HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * NovaError when trying to copy deployment logical connector
     *
     * @return a NovaError
     */
    public static NovaError getDestinationPlatformWithStatusKo()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DESTINATION_PLATFORM_KO,
                "Unable to deploy because the destination platform is not configured.",
                "Configure the platform and try again.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError when trying to use bad grammar in a search text
     *
     * @return NovaError
     */
    public static NovaError getBadGrammarError()
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                DeployErrors.DB_BAD_GRAMMAR,
                "The deployment plan filtered search could't be done. Bad grammar",
                "Check if condition has a suitable format. Example: myreleaseversion & (myservicename | myreleasename) or IDs separated by commas",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError when trying to use a text too long in a search text
     *
     * @return NovaError
     */
    public static NovaError getSearchTextTooLongError()
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                DeployErrors.SEARCH_TEXT_TOO_LONG,
                "The text to search is too long",
                "You only can search strings with less than 255 chars",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError when trying to use an invalid search in a search text
     *
     * @return NovaError
     */
    public static NovaError getInvalidSearchError()
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                DeployErrors.INVALID_SEARCH,
                "The deployment plan filtered search could't be done",
                "You only can search IDs OR one text condition at a time",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError when a null ID has been returned after generating a task
     *
     * @return NovaError
     */
    public static NovaError getTaskIdIsNullError()
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                DeployErrors.TASK_ID_IS_NULL,
                "The task id can not be null if task has been generated",
                "Task id should be set if task has been generated, contact with NOVA administrator",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }


    /**
     * NovaError when status is invalid
     *
     * @param status the status
     * @return NovaError
     */
    public static NovaError getDeploymentStatusNotValid(String status)
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                DeployErrors.INVALID_DEPLOYMENT_STATUS,
                MessageFormat.format("The deployment status [{0}] is not valid", status),
                "Set a correct status (DEPLOYED, REJECTED, UNDEPLOYED, DEFINITION, STORAGED, SCHEDULED)",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError when trying to copy deployment logical connector
     *
     * @param filesystemId The ID of the filesystem
     * @return a NovaError
     */
    public static NovaError getFilesystemNotFoundError(final Integer filesystemId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.FILESYSTEM_NOT_FOUND,
                MessageFormat.format("Filesystem {0} not found", filesystemId),
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError when there are duplicated volume binds
     *
     * @return a NovaError
     */
    public static NovaError getDuplicatedVolumeBindsError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DUPLICATED_VOLUME_BINDS,
                "There are duplicated mount points",
                "Check that there are not duplicated mount points",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError when status of deployment plan being replaced is not the expected one
     *
     * @param deploymentPlanId DeploymentPlanId
     * @return a NovaError
     */
    public static NovaError getReplacePlanConflictDeployedStatusError(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.REPLACE_PLAN_CONFLICT_DEPLOYED_STATUS,
                "Impossible to replace a plan [" + deploymentPlanId + "] that is not ready to be replaced.",
                "Wait until the action that is being executed over the replaced plan finished before trying again.",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError when the deployment plan being deployed is the same as the deployment plan being replaced
     *
     * @param deploymentPlanId DeploymentPlanId
     * @return a NovaError
     */
    public static NovaError getReplacePlanConflictSamePlanError(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.REPLACE_PLAN_CONFLICT_SAME_PLAN,
                "Impossible to replace a plan [" + deploymentPlanId + "] with itself.",
                "Create a new plan to replace the current one.",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError when the status of deployment plan being deployed is not the expected one
     *
     * @param deploymentPlanId     DeploymentPlanId
     * @param deploymentPlanStatus the deployment plan status
     * @param environment          environment
     * @return a NovaError
     */
    public static NovaError getReplacePlanConflictDeployingStatusError(final Integer deploymentPlanId, final String environment, final String deploymentPlanStatus)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.REPLACE_PLAN_CONFLICT_DEPLOYING_STATUS,
                "Impossible to deploy a plan  with id: [" + deploymentPlanId + "]- Env: [" + environment + "] due to the deployment plan is not in the correct status. Current deployment plan status: [" + deploymentPlanStatus + "]",
                "Deployment plan status expected/accepted: [DEFINITION or SCHEDULED]. For solving this, create a new plan to replace the current one.",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError where user selects a disabled date for scheduled deployment (NOVA Planned)
     *
     * @return a NovaError
     */
    public static NovaError getSelectedDateForDeploymentIsDisabledError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DISABLED_DEPLOYMENT_DATE,
                "Chosen date for deployment is disabled",
                "Select a different date for this scheduled deployment",
                HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    /**
     * NovaError when is impossible to get the list of disabled deployment dates for an UUAA in a given date
     *
     * @return a NovaError
     */
    public static NovaError getImpossibleToGetDisabledDatesForDeploymentError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.ERROR_GETTING_DISABLED_DEPLOYMENT_DATES,
                "Impossible to get the list of disabled deployment dates",
                "Everything is ok, but the NOVA platform service that gets the information about disabled dates is failing. Please, wait until the NOVA service status recovery UP",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError where deployment manager subsystem status was error
     *
     * @return a NovaError
     */
    public static NovaError getDeploymentSubsystemStatusError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_SUBSYSTEM_STATUS,
                "There was an error trying to get the deployment subsystem status. Deployment Manager or Swarm Manager service are down.",
                "Your services of the subsystem are fine, but the NOVA platform service that getting the information about your services are down. Please, wait until the NOVA service status recovery UP",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError where deployment manager service status was error
     *
     * @return a NovaError
     */
    public static NovaError getDeploymentServiceStatusError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_SERVICE_STATUS,
                "There was an error trying to get the deployment service status. Deployment Manager or Swarm Manager services are down.",
                "Your services are fine, but the NOVA platform service that getting the information about your services are down. Please, wait until the NOVA service status recovery UP",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError where ether manager service status was error
     *
     * @return a NovaError
     */
    public static NovaError getEtherDeploymentServiceStatusError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_ETHER_SERVICE_STATUS,
                "There was an error trying to get the ether deployment service status. Ether Manager or Swarm Manager services are down.",
                "Your services are fine, but the NOVA platform service that getting the information about your services are down. Please, wait until the NOVA service status recovery UP",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError where deployment manager instance status was error
     *
     * @return a NovaError
     */
    public static NovaError getDeploymentInstanceStatusError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_INSTANCE_STATUS,
                "There was an error trying to get the deployment instance status. Deployment Manager or Swarm Manager services are down.",
                "Your instances are fine, but the NOVA platform service that getting the information about your services are down. Please, wait until the NOVA service status recovery UP",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError where deployment manager instance status was error for ETHER instances
     *
     * @return a NovaError
     */
    public static NovaError getEtherDeploymentInstanceStatusError()
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_ETHER_INSTANCE_STATUS,
                "There was an error trying to get the deployment instance status in ether platform. Ether Manager or Swarm Manager services are down.",
                "Your instances are fine, but the NOVA platform service that getting the information about your services are down. Please, wait until the NOVA service status recovery UP",
                HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    /**
     * NovaError when trying to configure services to log/trace in Ether
     *
     * @param deploymentPlanId deployment plan id
     * @return a NovaError
     */
    public static NovaError getConfigureServicesForLoggingInEtherError(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.CONFIGURE_RESOURCES_FOR_LOGGING_IN_ETHER,
                "Error trying to configure services of deployment plan with id [" + deploymentPlanId + "] to log/trace in Ether.",
                "Update the web and try again later. Check if Ether platform is up.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.WARNING);
    }

    /**
     * NovaError the service is not deployed
     *
     * @param serviceId Release version name.
     * @return a NovaError
     */
    public static NovaError getServiceIsNotDeployedError(final Integer serviceId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.DEPLOYMENT_SERVICE_NOT_JDK_PARAMETERIZABLE,
                "Service with id: [" + serviceId + "] is not deployed",
                "Choose a service that is deployed",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Get a Resources HaveNo Documentation Error.
     *
     * @param resourcesWithoutDocumentation An structure with the resources without documentation.
     * @return A NovaError
     */
    public static NovaError getResourcesHaveNoDocumentationError(Map<String, List<Integer>> resourcesWithoutDocumentation)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("The following resources have no documentation ->");
        if (resourcesWithoutDocumentation != null)
        {
            resourcesWithoutDocumentation.forEach((key, value) -> {
                String ids = value.stream().map(String::valueOf).collect(Collectors.joining(", "));
                if (!Strings.isNullOrEmpty(ids))
                {
                    stringBuilder.append(String.format(" [%s]: [%s]", key, ids));
                }
            });
        }
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.RESOURCES_HAVE_NO_DOCUMENTATION,
                stringBuilder.toString(),
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Error invalid instances number for multiCPD
     *
     * @param serviceNames services with non-correct number of instances
     * @return error
     */
    public static NovaError getInvalidInstancesNumberError(final List<String> serviceNames)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.INVALID_INSTANCE_NUMBER_ERROR_CODE,
                "You are trying to promote a deployment plan in MonoCPD mode into a MultiCPD configured environment. We can't copy the service/s " + serviceNames + " because they don't have the correct instance's number.",
                "You need to configure a new plan in PRE environment that meets the requirements of MultiCPD mode and promote it to PRO environment.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSameCPDConfigurationInPROEnvironment(final @NotNull Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME, DeployErrors.NO_SAME_CPD_CONFIG_IN_PRO,
                "You are trying to copy a deployment plan [" + deploymentPlanId + "] in MonoCPD mode while PRO environment is on MultiCPD mode. This is an operation that is not allowed",
                "You need to configure a new plan in PRE environment that meets the requirements of MultiCPD mode and promote it to PRO environment.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    /**
     * Error no broker in pro with the same name
     *
     * @param brokerName broker name
     * @return error
     */
    public static NovaError getNoBrokerWithNameOnEnvironment(final String brokerName)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.IMPOSSIBLE_TO_PROMOTE_TO_PRO_DUE_TO_NO_SAME_BROKER,
                "No broker with name: [" + brokerName + "] in Production, is mandatory to have a broker with the same name in both environments",
                "Before promote, create a broker in pro environment with same name as you are using in pre",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError for deploy plan error from deployment manager
     *
     * @param deploymentPlanId the deployment plan id
     * @return a NovaError
     */
    public static NovaError getDeployFromDeploymentManagerError(final Integer deploymentPlanId)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.DEPLOY_PLAN_DEPLOYMENT_MANAGER_ERROR,
                "Deployment manager failed to deploy plan with id: [" + deploymentPlanId + "]",
                "The deployment manager service has failed. Try it again later.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }


    /**
     * NovaError when trying to choose a wrong types of services Grouping Names
     *
     * @return NovaError
     */
    public static NovaError getInvalidServicesTypes()
    {
        return new NovaError(DeploymentConstants.DeployErrors.CLASS_NAME,
                DeployErrors.ERROR_CHOICE_TYPE_SERVICE_GROUPING_NAMES,
                "Error to choosing the service types option Grouping Names",
                "Choose the right option [all , deployed , not_deployed]",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Error no deployment label for an Ephoenix service
     *
     * @param environment the environment
     * @param serviceName the service name
     * @return a NovaError
     */
    public static NovaError getNoLabelEphoenixServiceError(final String environment, final String serviceName)
    {
        return new NovaError(DeployErrors.CLASS_NAME,
                DeployErrors.NO_LABEL_EPHOENIX_SERVICE_ERROR,
                "Ephoenix service name: [" + serviceName + "] doesn't have the deployment labels parameterized for environment [" + environment + "] in NOVA DB",
                "It is mandatory to parameterize the deployment_label table. " + Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }
}
